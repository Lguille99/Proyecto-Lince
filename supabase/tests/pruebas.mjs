const { PGlite } = await import(process.env.PGLITE_MODULE || '@electric-sql/pglite');
import { readFile } from 'node:fs/promises';
import assert from 'node:assert/strict';
const db = new PGlite();
await db.exec(`create role anon; create role authenticated;
create schema auth; create table auth.users(id uuid primary key);
create function auth.uid() returns uuid language sql stable as $$
select nullif(current_setting('request.jwt.claim.sub',true),'')::uuid $$;
grant usage on schema public, auth to authenticated, anon;
grant execute on function auth.uid() to authenticated, anon;`);
for (const archivo of [
  'migrations/202610190001_estructura.sql',
  'migrations/202610190002_permisos_y_operaciones.sql',
  'migrations/202610280001_mvp_supabase.sql'
]) {
  await db.exec(await readFile(new URL('../'+archivo, import.meta.url),'utf8'));
}
const resetDemo = await readFile(new URL('../reset_demo.sql', import.meta.url), 'utf8');
const seedDemo = await readFile(new URL('../seed.sql', import.meta.url), 'utf8');
await db.exec(resetDemo);
await db.exec(seedDemo);
await db.exec(resetDemo);
await db.exec(seedDemo);
const perfiles = (await db.query('select * from perfiles order by codigo')).rows;
const porCodigo = Object.fromEntries(perfiles.map(p=>[p.codigo,p]));
for (const [n,p] of perfiles.entries()) {
  p.auth = `00000000-0000-0000-0000-00000000000${n+1}`;
  await db.query('insert into auth.users values ($1)',[p.auth]);
  await db.query('update perfiles set auth_user_id=$1 where id=$2',[p.auth,p.id]);
}
const ids = Object.fromEntries((await db.query('select s.codigo,a.id from asignaciones a join servicios s on s.id=a.servicio_id')).rows.map(r=>[r.codigo,r.id]));
let cantidad = 0;
async function verificar(nombre,accion) { await accion(); cantidad++; console.log('OK '+nombre); }
async function identidad(codigo) {
  await db.exec('reset role');
  await db.query("select set_config('request.jwt.claim.sub',$1,false)",[porCodigo[codigo]?.auth ?? '']);
  await db.exec('set role authenticated');
}
async function falla(sql,params=[]) { await assert.rejects(()=>db.query(sql,params)); }
const manana8 = "(now() at time zone 'America/Santiago')::date + 8";
const ubicacion = 'Hotel de prueba';

async function estadosAsignaciones() {
  const r = await db.query(`select s.codigo, a.estado
    from asignaciones a join servicios s on s.id = a.servicio_id`);
  return Object.fromEntries(r.rows.map(x => [x.codigo, x.estado]));
}

await verificar('reset y seed reproducen el estado demo',async()=>{
  assert.equal((await db.query('select count(*)::int n from perfiles')).rows[0].n,4);
  assert.equal((await db.query('select count(*)::int n from servicios')).rows[0].n,6);
  assert.equal((await db.query('select count(*)::int n from asignaciones')).rows[0].n,6);
  assert.deepEqual(await estadosAsignaciones(), {
    'LIN-001':'CONFIRMADO', 'LIN-002':'RECHAZADO', 'LIN-003':'PENDIENTE',
    'LIN-004':'CONFIRMADO', 'LIN-005':'PENDIENTE', 'LIN-006':'PENDIENTE'
  });
});
await verificar('seed incluye servicio de hoy e historial calificado',async()=>{
  const hoy=await db.query(`select count(*)::int n from servicios
    where codigo='LIN-001' and fecha=(now() at time zone 'America/Santiago')::date`);
  assert.equal(hoy.rows[0].n,1);
  assert.equal((await db.query('select count(*)::int n from ejecuciones')).rows[0].n,1);
  assert.equal((await db.query('select count(*)::int n from calificaciones')).rows[0].n,1);
});
await db.exec('set role anon');
await verificar('anónimo no lee perfiles',()=>falla('select * from perfiles'));
await verificar('anónimo no ejecuta operaciones',()=>falla('select public.simular_sincronizacion()'));
await identidad('DRV-001');
await verificar('conductor ve sus servicios y el que acompaña',async()=>assert.equal((await db.query('select * from servicios')).rows.length,5));
await verificar('conductor ve su perfil y el de su compañero',async()=>assert.equal((await db.query('select * from perfiles')).rows.length,2));
await verificar('no puede elevar su rol',()=>falla("update perfiles set rol='COORDINADOR'"));
await verificar('no puede modificar asignaciones directamente',()=>falla("update asignaciones set estado='CONFIRMADO'"));
await verificar('acompañante no responde por el titular',()=>falla('select responder_asignacion($1,true)',[ids['LIN-004']]));
await verificar('rechazo requiere motivo',()=>falla('select responder_asignacion($1,false,$2)',[ids['LIN-006'],' ']));
await verificar('rechazo rechaza motivo demasiado largo',()=>falla('select responder_asignacion($1,false,$2)',[ids['LIN-006'],'x'.repeat(301)]));
await verificar('check-out sin check-in rechazado',()=>falla('select registrar_check_out($1)',[ids['LIN-001']]));
await verificar('no inicia servicio pendiente',()=>falla('select registrar_check_in($1,$2)',[ids['LIN-006'],ubicacion]));
await verificar('confirma servicio propio',async()=>{
  const r=await db.query('select (responder_asignacion($1,true)).estado estado',[ids['LIN-003']]);
  assert.equal(r.rows[0].estado,'CONFIRMADO');
});
await verificar('no admite segunda respuesta',()=>falla('select responder_asignacion($1,true)',[ids['LIN-003']]));
await verificar('check-in exige ubicación',()=>falla('select registrar_check_in($1,$2)',[ids['LIN-003'],' ']));
await verificar('registra check-in con ubicación',async()=>{
  const r=await db.query('select (registrar_check_in($1,$2)).ubicacion_inicio ubicacion',[ids['LIN-003'],ubicacion]);
  assert.equal(r.rows[0].ubicacion,ubicacion);
});
await verificar('no repite check-in',()=>falla('select registrar_check_in($1,$2)',[ids['LIN-003'],ubicacion]));
await verificar('registra check-out',()=>db.query('select registrar_check_out($1)',[ids['LIN-003']]));
await verificar('no repite check-out',()=>falla('select registrar_check_out($1)',[ids['LIN-003']]));
await verificar('registra rechazo',()=>db.query('select responder_asignacion($1,false,$2)',[ids['LIN-006'],'No disponible']));
await verificar('no inicia servicio rechazado',()=>falla('select registrar_check_in($1,$2)',[ids['LIN-006'],ubicacion]));
await verificar('rechaza disponibilidad pasada',()=>falla("select guardar_disponibilidad((now() at time zone 'America/Santiago')::date-1,true)"));
await verificar('bloqueo exige siete días',()=>falla("select guardar_disponibilidad((now() at time zone 'America/Santiago')::date+1,true)"));
await verificar('guarda y actualiza disponibilidad sin duplicados',async()=>{
  await db.exec(`select guardar_disponibilidad(${manana8},true)`);
  await db.exec(`select guardar_disponibilidad(${manana8},false)`);
  const r=await db.query('select * from disponibilidad');
  assert.equal(r.rows.length,1);
  assert.equal(r.rows[0].bloqueado,false);
});
await verificar('no puede falsificar historial',()=>falla("insert into historial(usuario_id,accion,detalle) values($1,'DISPONIBILIDAD','falso')",[porCodigo['DRV-001'].id]));
await verificar('historial identifica usuario y orden de eventos',async()=>{
  const r=await db.query('select accion,usuario_id from historial where asignacion_id=$1 order by fecha_hora',[ids['LIN-003']]);
  assert.deepEqual(r.rows.map(x=>x.accion),['CONFIRMACION','CHECK_IN','CHECK_OUT']);
  assert(r.rows.every(x=>x.usuario_id===porCodigo['DRV-001'].id));
});
await verificar('conductor no simula sincronización',()=>falla('select simular_sincronizacion()'));
await verificar('actualiza solo su perfil',async()=>{
  await db.query("select actualizar_mi_perfil('Nombre actualizado','demo@example.com','+56 9 0000 0000','Santiago',array['ESPANOL','INGLES'])");
  const r=await db.query("select telefono,zona,idiomas from perfiles where codigo='DRV-001'");
  assert.equal(r.rows[0].telefono,'+56 9 0000 0000');
  assert.deepEqual(r.rows[0].idiomas,['ESPANOL','INGLES']);
});
await identidad('GUI-002');
await verificar('guía ve sus dos servicios y no disponibilidad ajena',async()=>{
  assert.equal((await db.query('select * from servicios')).rows.length,2);
  assert.equal((await db.query('select * from disponibilidad')).rows.length,0);
});
await verificar('guía titular confirma y ejecuta check-in',async()=>{
  await db.query('select responder_asignacion($1,true)',[ids['LIN-005']]);
  await db.query('select registrar_check_in($1,$2)',[ids['LIN-005'],'Museo de prueba']);
  await db.query('select registrar_check_out($1)',[ids['LIN-005']]);
});
await verificar('guía no califica',()=>falla('select calificar_asignacion($1,5)',[ids['LIN-005']]));
await identidad('COO-003');
await verificar('coordinador consulta conjunto',async()=>{
  assert.equal((await db.query('select * from perfiles')).rows.length,4);
  assert.equal((await db.query('select * from servicios')).rows.length,6);
  assert.equal((await db.query('select * from disponibilidad')).rows.length,1);
});
await verificar('coordinador no responde por conductor',()=>falla('select responder_asignacion($1,true)',[ids['LIN-006']]));
await verificar('coordinador no tiene disponibilidad personal',()=>falla(`select guardar_disponibilidad(${manana8},true)`));
await verificar('coordinador califica y puede corregir',async()=>{
  const primero=await db.query('select (calificar_asignacion($1,5)).puntaje puntaje',[ids['LIN-003']]);
  assert.equal(primero.rows[0].puntaje,5);
  const segundo=await db.query('select (calificar_asignacion($1,4)).puntaje puntaje',[ids['LIN-003']]);
  assert.equal(segundo.rows[0].puntaje,4);
});
await verificar('coordinador reasigna a conductor compatible',async()=>{
  const r=await db.query('select (reasignar_asignacion($1,$2)).estado estado',[ids['LIN-006'],porCodigo['DRV-002'].id]);
  assert.equal(r.rows[0].estado,'PENDIENTE');
  const asignado=await db.query('select usuario_id from asignaciones where id=$1',[ids['LIN-006']]);
  assert.equal(asignado.rows[0].usuario_id,porCodigo['DRV-002'].id);
});
await verificar('reasignación exige idioma y rol',()=>falla('select reasignar_asignacion($1,$2)',[ids['LIN-005'],porCodigo['DRV-002'].id]));
await verificar('calificación visible para el titular',async()=>{
  await identidad('DRV-001');
  assert.equal((await db.query('select * from calificaciones')).rows.length,2);
});
await identidad('COO-003');
await verificar('sincronización queda marcada como simulada',async()=>{
  const r=await db.query('select (simular_sincronizacion()).simulada s');
  assert.equal(r.rows[0].s,true);
});
await db.exec('reset role');
await db.query('update perfiles set activo=false where id=$1',[porCodigo['DRV-001'].id]);
await identidad('DRV-001');
await verificar('usuario desactivado no ve servicios ni modifica datos',async()=>{
  assert.equal((await db.query('select * from servicios')).rows.length,0);
  await falla(`select guardar_disponibilidad(${manana8},true)`);
});
await identidad('SIN_PERFIL');
await verificar('sesión sin perfil no obtiene acceso',async()=>{
  assert.equal((await db.query('select * from perfiles')).rows.length,0);
  await falla("select actualizar_mi_perfil('Intruso','x@example.com','+56 9 0000 0000','Santiago',array['ESPANOL'])");
});
await db.exec('reset role');
await db.exec(resetDemo);
await db.exec(seedDemo);
await verificar('reset restaura el estado y conserva vínculos Auth',async()=>{
  assert.equal((await db.query('select count(*)::int n from perfiles')).rows[0].n,4);
  assert.equal((await db.query('select count(*)::int n from perfiles where auth_user_id is not null')).rows[0].n,4);
  const drv=await db.query("select activo from perfiles where codigo='DRV-001'");
  assert.equal(drv.rows[0].activo,true);
  assert.deepEqual(await estadosAsignaciones(), {
    'LIN-001':'CONFIRMADO', 'LIN-002':'RECHAZADO', 'LIN-003':'PENDIENTE',
    'LIN-004':'CONFIRMADO', 'LIN-005':'PENDIENTE', 'LIN-006':'PENDIENTE'
  });
  assert.equal((await db.query('select count(*)::int n from ejecuciones')).rows[0].n,1);
  assert.equal((await db.query('select count(*)::int n from calificaciones')).rows[0].n,1);
  assert.equal((await db.query('select count(*)::int n from disponibilidad')).rows[0].n,0);
  assert.equal((await db.query('select count(*)::int n from historial')).rows[0].n,0);
});
console.log(`${cantidad} comprobaciones correctas`);
await db.close();
