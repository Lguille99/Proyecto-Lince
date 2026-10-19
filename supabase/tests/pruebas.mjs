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
for (const archivo of ['migrations/202610190001_estructura.sql','migrations/202610190002_permisos_y_operaciones.sql','seed.sql']) {
  await db.exec(await readFile(new URL('../'+archivo, import.meta.url),'utf8'));
}
await db.exec(await readFile(new URL('../seed.sql',import.meta.url),'utf8'));
const perfiles = (await db.query('select * from perfiles order by codigo')).rows;
const porRol = Object.fromEntries(perfiles.map(p=>[p.rol,p]));
for (const [n,p] of perfiles.entries()) {
  p.auth = `00000000-0000-0000-0000-00000000000${n+1}`;
  await db.query('insert into auth.users values ($1)',[p.auth]);
  await db.query('update perfiles set auth_user_id=$1 where id=$2',[p.auth,p.id]);
}
const ids = Object.fromEntries((await db.query('select s.codigo,a.id from asignaciones a join servicios s on s.id=a.servicio_id')).rows.map(r=>[r.codigo,r.id]));
let cantidad = 0;
async function verificar(nombre,accion) { await accion(); cantidad++; console.log('OK '+nombre); }
async function identidad(rol) {
  await db.exec('reset role');
  await db.query("select set_config('request.jwt.claim.sub',$1,false)",[porRol[rol]?.auth ?? '']);
  await db.exec('set role authenticated');
}
async function falla(sql,params=[]) { await assert.rejects(()=>db.query(sql,params)); }
await verificar('seed repetible sin duplicados',async()=>assert.equal((await db.query('select count(*)::int n from asignaciones')).rows[0].n,5));
await db.exec('set role anon');
await verificar('anónimo no lee perfiles',()=>falla('select * from perfiles'));
await verificar('anónimo no ejecuta operaciones',()=>falla('select public.simular_sincronizacion()'));
await identidad('CONDUCTOR');
await verificar('conductor ve tres servicios',async()=>assert.equal((await db.query('select * from servicios')).rows.length,3));
await verificar('conductor solo ve su perfil',async()=>assert.equal((await db.query('select * from perfiles')).rows.length,1));
await verificar('no puede elevar su rol',()=>falla("update perfiles set rol='COORDINADOR'"));
await verificar('no puede modificar asignaciones directamente',()=>falla("update asignaciones set estado='CONFIRMADO'"));
await verificar('no puede responder por guía',()=>falla('select responder_asignacion($1,true)',[ids['LIN-004']]));
await verificar('rechazo requiere motivo',()=>falla('select responder_asignacion($1,false,$2)',[ids['LIN-001'],' ']));
await verificar('rechazo rechaza motivo demasiado largo',()=>falla('select responder_asignacion($1,false,$2)',[ids['LIN-001'],'x'.repeat(301)]));
await verificar('check-out sin check-in rechazado',()=>falla('select registrar_check_out($1)',[ids['LIN-001']]));
await verificar('no inicia servicio pendiente',()=>falla('select registrar_check_in($1)',[ids['LIN-001']]));
await verificar('confirma servicio propio',async()=>{
 const r=await db.query('select (responder_asignacion($1,true)).estado estado',[ids['LIN-001']]);assert.equal(r.rows[0].estado,'CONFIRMADO');
});
await verificar('no admite segunda respuesta',()=>falla('select responder_asignacion($1,true)',[ids['LIN-001']]));
await verificar('registra check-in',()=>db.query('select registrar_check_in($1)',[ids['LIN-001']]));
await verificar('no repite check-in',()=>falla('select registrar_check_in($1)',[ids['LIN-001']]));
await verificar('registra check-out',()=>db.query('select registrar_check_out($1)',[ids['LIN-001']]));
await verificar('no repite check-out',()=>falla('select registrar_check_out($1)',[ids['LIN-001']]));
await verificar('registra rechazo',()=>db.query('select responder_asignacion($1,false,$2)',[ids['LIN-002'],'No disponible']));
await verificar('no inicia servicio rechazado',()=>falla('select registrar_check_in($1)',[ids['LIN-002']]));
await verificar('rechaza disponibilidad pasada',()=>falla("select guardar_disponibilidad((now() at time zone 'America/Santiago')::date-1,true)"));
await verificar('guarda y actualiza disponibilidad sin duplicados',async()=>{
 await db.exec("select guardar_disponibilidad((now() at time zone 'America/Santiago')::date+1,true)");
 await db.exec("select guardar_disponibilidad((now() at time zone 'America/Santiago')::date+1,false)");
 const r=await db.query('select * from disponibilidad');assert.equal(r.rows.length,1);assert.equal(r.rows[0].bloqueado,false);
});
await verificar('no puede falsificar historial',()=>falla("insert into historial(usuario_id,accion,detalle) values($1,'DISPONIBILIDAD','falso')",[porRol.CONDUCTOR.id]));
await verificar('historial identifica usuario y orden de eventos',async()=>{
 const r=await db.query('select accion,usuario_id from historial where asignacion_id=$1 order by fecha_hora',[ids['LIN-001']]);
 assert.deepEqual(r.rows.map(x=>x.accion),['CONFIRMACION','CHECK_IN','CHECK_OUT']);assert(r.rows.every(x=>x.usuario_id===porRol.CONDUCTOR.id));
});
await verificar('conductor no simula sincronización',()=>falla('select simular_sincronizacion()'));
await verificar('actualiza solo su perfil',()=>db.exec("select actualizar_mi_perfil('Nombre actualizado','demo@example.com')"));
await identidad('GUIA');
await verificar('guía ve dos servicios y no disponibilidad ajena',async()=>{
 assert.equal((await db.query('select * from servicios')).rows.length,2);assert.equal((await db.query('select * from disponibilidad')).rows.length,0);
});
await verificar('guía confirma pero no ejecuta check-in',async()=>{
 await db.query('select responder_asignacion($1,true)',[ids['LIN-004']]);await falla('select registrar_check_in($1)',[ids['LIN-004']]);
});
await identidad('COORDINADOR');
await verificar('coordinador consulta conjunto',async()=>{
 assert.equal((await db.query('select * from perfiles')).rows.length,3);assert.equal((await db.query('select * from servicios')).rows.length,5);
 assert.equal((await db.query('select * from disponibilidad')).rows.length,1);
});
await verificar('coordinador no responde por conductor',()=>falla('select responder_asignacion($1,true)',[ids['LIN-003']]));
await verificar('coordinador no tiene disponibilidad personal',()=>falla("select guardar_disponibilidad((now() at time zone 'America/Santiago')::date+1,true)"));
await verificar('sincronización queda marcada como simulada',async()=>{
 const r=await db.query('select (simular_sincronizacion()).simulada s');assert.equal(r.rows[0].s,true);
});
await db.exec('reset role');
await db.query('update perfiles set activo=false where id=$1',[porRol.CONDUCTOR.id]);
await identidad('CONDUCTOR');
await verificar('usuario desactivado no ve servicios ni modifica datos',async()=>{
 assert.equal((await db.query('select * from servicios')).rows.length,0);await falla("select guardar_disponibilidad(current_date+1,true)");
});
await identidad('SIN_PERFIL');
await verificar('sesión sin perfil no obtiene acceso',async()=>{
 assert.equal((await db.query('select * from perfiles')).rows.length,0);await falla("select actualizar_mi_perfil('Intruso','x@example.com')");
});
console.log(`${cantidad} comprobaciones correctas`);
await db.close();
