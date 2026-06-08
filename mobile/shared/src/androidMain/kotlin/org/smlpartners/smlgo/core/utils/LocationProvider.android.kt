package org.smlpartners.smlgo.core.utils

import android.annotation.SuppressLint
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import org.smlpartners.smlgo.appContext
import kotlin.coroutines.resume

// Resultado de la solicitud de ubicación
sealed class LocationResult {
    data class Success(val latitude: Double, val longitude: Double) : LocationResult()
    data class Error(val message: String)                           : LocationResult()
    object PermissionDenied                                         : LocationResult()
}

// Proveedor de ubicación usando FusedLocationProvider
class LocationProvider {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResult =
        suspendCancellableCoroutine { cont ->
            val client = LocationServices.getFusedLocationProviderClient(appContext)
            
            // Intentamos primero la última ubicación conocida (es casi instantáneo)
            client.lastLocation.addOnSuccessListener { lastLocation ->
                if (lastLocation != null) {
                    cont.resume(LocationResult.Success(lastLocation.latitude, lastLocation.longitude))
                } else {
                    // Si no hay última ubicación, pedimos la actual de forma balanceada para mayor rapidez
                    val cts = CancellationTokenSource()
                    client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                        .addOnSuccessListener { location ->
                            if (location != null) {
                                cont.resume(LocationResult.Success(location.latitude, location.longitude))
                            } else {
                                cont.resume(LocationResult.Error("No se pudo obtener la ubicación"))
                            }
                        }
                        .addOnFailureListener { e ->
                            cont.resume(LocationResult.Error(e.message ?: "Error desconocido"))
                        }
                    cont.invokeOnCancellation { cts.cancel() }
                }
            }.addOnFailureListener {
                // Si falla lastLocation, intentamos getCurrentLocation
                val cts = CancellationTokenSource()
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            cont.resume(LocationResult.Success(location.latitude, location.longitude))
                        } else {
                            cont.resume(LocationResult.Error("No se pudo obtener la ubicación"))
                        }
                    }
                    .addOnFailureListener { e ->
                        cont.resume(LocationResult.Error(e.message ?: "Error desconocido"))
                    }
                cont.invokeOnCancellation { cts.cancel() }
            }
        }
}