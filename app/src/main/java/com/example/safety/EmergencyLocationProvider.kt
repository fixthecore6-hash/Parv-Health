package com.example.safety

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import com.example.data.AlertLocationSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.roundToInt

enum class EmergencyLocationStatus(val label: String) {
    READY("Location ready"),
    OFF("Location off"),
    UNAVAILABLE("Location unavailable")
}

data class LocationFix(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float?,
    val timeMs: Long
)

data class LocationResolutionResult(
    val source: AlertLocationSource,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val timestampMs: Long = System.currentTimeMillis(),
    val mapsLink: String = ""
) {
    val isAvailable: Boolean
        get() = source != AlertLocationSource.UNAVAILABLE && latitude != null && longitude != null

    fun formattedCoordinates(): String? {
        val lat = latitude ?: return null
        val lon = longitude ?: return null
        return String.format(Locale.US, "%.6f, %.6f", lat, lon)
    }

    fun formattedAccuracy(): String? {
        val acc = accuracyMeters ?: return null
        return "±${acc.roundToInt()} m"
    }
}

class EmergencyLocationProvider(private val context: Context) {

    fun hasFineLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasCoarseLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasAnyLocationPermission(): Boolean {
        return hasFineLocationPermission() || hasCoarseLocationPermission()
    }

    fun isDeviceLocationEnabled(): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return runCatching { LocationManagerCompat.isLocationEnabled(manager) }.getOrDefault(false)
    }

    fun getLocationStatus(): EmergencyLocationStatus {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return EmergencyLocationStatus.UNAVAILABLE
        val locationOn = runCatching { LocationManagerCompat.isLocationEnabled(manager) }.getOrDefault(false)
        if (!locationOn) return EmergencyLocationStatus.OFF
        return if (hasAnyLocationPermission()) {
            EmergencyLocationStatus.READY
        } else {
            EmergencyLocationStatus.UNAVAILABLE
        }
    }

    suspend fun resolveEmergencyLocation(nowMs: Long = System.currentTimeMillis()): LocationResolutionResult {
        val hasPermission = hasAnyLocationPermission()
        val isLocationOn = isDeviceLocationEnabled()
        return resolveLocationWithFallback(
            hasPermission = hasPermission,
            isLocationEnabled = isLocationOn,
            nowMs = nowMs,
            fetchFresh = { fetchFreshFixFromProviders() },
            getLastKnown = { readBestLastKnownFix(nowMs) }
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchFreshFixFromProviders(): LocationFix? {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val hasFine = hasFineLocationPermission()
        val providers = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                runCatching { manager.isProviderEnabled(LocationManager.FUSED_PROVIDER) }.getOrDefault(false)
            ) {
                add(LocationManager.FUSED_PROVIDER)
            }
            if (hasFine && runCatching { manager.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false)) {
                add(LocationManager.GPS_PROVIDER)
            }
            if (runCatching { manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false)) {
                add(LocationManager.NETWORK_PROVIDER)
            }
        }
        if (providers.isEmpty()) return null

        return withContext(Dispatchers.Main) {
            val channel = Channel<LocationFix?>(capacity = providers.size)
            val jobs = providers.map { provider ->
                launch {
                    val fix = requestSingleProviderUpdate(manager, provider)
                    channel.trySend(fix)
                }
            }
            var bestFix: LocationFix? = null
            var responses = 0
            while (responses < providers.size) {
                val received = channel.receive()
                responses++
                if (received != null) {
                    bestFix = received
                    break
                }
            }
            jobs.forEach { it.cancel() }
            channel.close()
            bestFix
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleProviderUpdate(
        manager: LocationManager,
        provider: String
    ): LocationFix? = suspendCancellableCoroutine { cont ->
        val signal = CancellationSignal()
        cont.invokeOnCancellation { signal.cancel() }
        try {
            LocationManagerCompat.getCurrentLocation(
                manager,
                provider,
                signal,
                ContextCompat.getMainExecutor(context)
            ) { loc ->
                if (cont.isActive) {
                    cont.resume(loc?.toLocationFix())
                }
            }
        } catch (_: SecurityException) {
            if (cont.isActive) cont.resume(null)
        } catch (_: Exception) {
            if (cont.isActive) cont.resume(null)
        }
    }

    @SuppressLint("MissingPermission")
    private fun readBestLastKnownFix(nowMs: Long): LocationFix? {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val hasFine = hasFineLocationPermission()
        val candidateProviders = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(LocationManager.FUSED_PROVIDER)
            }
            if (hasFine) {
                add(LocationManager.GPS_PROVIDER)
            }
            add(LocationManager.NETWORK_PROVIDER)
            add(LocationManager.PASSIVE_PROVIDER)
        }

        val fixes = candidateProviders.mapNotNull { provider ->
            try {
                manager.getLastKnownLocation(provider)?.toLocationFix()
            } catch (_: SecurityException) {
                null
            } catch (_: Exception) {
                null
            }
        }.filter { fix ->
            fix.timeMs > 0L && (nowMs - fix.timeMs) < LAST_KNOWN_MAX_AGE_MS
        }

        return fixes.maxByOrNull { it.timeMs }
    }

    private fun Location.toLocationFix(): LocationFix {
        val acc = if (hasAccuracy()) accuracy else null
        val t = if (time > 0L) time else System.currentTimeMillis()
        return LocationFix(
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = acc,
            timeMs = t
        )
    }

    companion object {
        const val FRESH_TIMEOUT_MS = 10_000L
        const val LAST_KNOWN_MAX_AGE_MS = 30L * 60L * 1000L

        fun buildMapsLink(latitude: Double, longitude: Double): String {
            return String.format(Locale.US, "https://maps.google.com/?q=%.6f,%.6f", latitude, longitude)
        }

        fun buildLocationSettingsIntent(): Intent {
            return Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        }

        fun buildOpenMapsIntent(mapsLink: String): Intent {
            return Intent(Intent.ACTION_VIEW, android.net.Uri.parse(mapsLink))
        }

        fun buildEmergencySmsBody(
            alertLabel: String,
            timestampMs: Long,
            location: LocationResolutionResult
        ): String {
            val timeStr = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
                .format(Date(timestampMs))
            return if (location.isAvailable) {
                val accuracyPart = location.formattedAccuracy()?.let { " (Accuracy: $it)" }.orEmpty()
                "Emergency $alertLabel from parv-health at $timeStr. My location: ${location.mapsLink}$accuracyPart"
            } else {
                "Emergency $alertLabel from parv-health at $timeStr. Location unavailable."
            }
        }

        suspend fun resolveLocationWithFallback(
            hasPermission: Boolean,
            isLocationEnabled: Boolean,
            nowMs: Long = System.currentTimeMillis(),
            timeoutMs: Long = FRESH_TIMEOUT_MS,
            fetchFresh: suspend () -> LocationFix?,
            getLastKnown: suspend () -> LocationFix?
        ): LocationResolutionResult {
            if (!hasPermission || !isLocationEnabled) {
                return LocationResolutionResult(
                    source = AlertLocationSource.UNAVAILABLE,
                    timestampMs = nowMs
                )
            }

            val freshFix = withTimeoutOrNull(timeoutMs) {
                runCatching { fetchFresh() }.getOrNull()
            }
            if (freshFix != null) {
                return LocationResolutionResult(
                    source = AlertLocationSource.FRESH,
                    latitude = freshFix.latitude,
                    longitude = freshFix.longitude,
                    accuracyMeters = freshFix.accuracyMeters,
                    timestampMs = freshFix.timeMs,
                    mapsLink = buildMapsLink(freshFix.latitude, freshFix.longitude)
                )
            }

            val lastKnown = runCatching { getLastKnown() }.getOrNull()
            if (lastKnown != null && lastKnown.timeMs > 0L && (nowMs - lastKnown.timeMs) < LAST_KNOWN_MAX_AGE_MS) {
                return LocationResolutionResult(
                    source = AlertLocationSource.LAST_KNOWN,
                    latitude = lastKnown.latitude,
                    longitude = lastKnown.longitude,
                    accuracyMeters = lastKnown.accuracyMeters,
                    timestampMs = lastKnown.timeMs,
                    mapsLink = buildMapsLink(lastKnown.latitude, lastKnown.longitude)
                )
            }

            return LocationResolutionResult(
                source = AlertLocationSource.UNAVAILABLE,
                timestampMs = nowMs
            )
        }
    }
}
