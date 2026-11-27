package cl.apiavengers.lince.datos.backend

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import cl.apiavengers.lince.datos.local.ColaSincronizacion
import cl.apiavengers.lince.datos.local.EstadoOperacion
import cl.apiavengers.lince.datos.local.LinceDatabase
import java.util.concurrent.TimeUnit

class SincronizacionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val dao = LinceDatabase.obtener(applicationContext).operacionPendienteDao()
        val cola = ColaSincronizacion(dao)
        val sincronizador = SincronizadorBackend()
        return try {
            cola.sincronizar(sincronizador)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val NOMBRE_TRABAJO = "lince-sincronizacion"

        fun programar(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val solicitud = PeriodicWorkRequestBuilder<SincronizacionWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                NOMBRE_TRABAJO,
                ExistingPeriodicWorkPolicy.KEEP,
                solicitud
            )
        }
    }
}
