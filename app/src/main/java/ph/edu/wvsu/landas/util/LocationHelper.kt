package ph.edu.wvsu.landas.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

data class CampusLocation(
    val latitude: Double,
    val longitude: Double,
    val mapX: Float,
    val mapY: Float,
    val accuracyMeters: Float,
    val fromGps: Boolean
) {
    companion object {
        // Approximate WVSU-PC campus reference used by the prototype map.
        const val CAMPUS_CENTER_LAT = 10.9500
        const val CAMPUS_CENTER_LNG = 122.6330
        const val MAP_X_SCALE = 8500.0
        const val MAP_Y_SCALE = 8500.0

        val DEMO_CENTER = CampusLocation(
            latitude = CAMPUS_CENTER_LAT,
            longitude = CAMPUS_CENTER_LNG,
            mapX = 500f,
            mapY = 400f,
            accuracyMeters = 30f,
            fromGps = false
        )

        fun fromGps(location: Location): CampusLocation {
            val mapX = (500.0 + (location.longitude - CAMPUS_CENTER_LNG) * MAP_X_SCALE).toFloat()
                .coerceIn(40f, 960f)
            val mapY = (400.0 - (location.latitude - CAMPUS_CENTER_LAT) * MAP_Y_SCALE).toFloat()
                .coerceIn(40f, 720f)
            return CampusLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                mapX = mapX,
                mapY = mapY,
                accuracyMeters = location.accuracy,
                fromGps = true
            )
        }
    }
}

class LocationHelper(private val context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    }

    @Suppress("MissingPermission")
    suspend fun currentLocation(): CampusLocation? {
        if (!hasLocationPermission()) return null
        return try {
            withTimeoutOrNull(8_000) {
                val location = client.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).await()
                location?.let { CampusLocation.fromGps(it) }
            }
        } catch (_: Exception) {
            null
        }
    }
}
