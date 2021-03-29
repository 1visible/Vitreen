package c0d3.vitreen.app.listeners

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat


class FetchLocation : LocationListener {

    private var onLocationFetchListner: OnLocationFetchListner? = null
    private var locationManager: LocationManager? = null

    fun setOnLocationFetchListner(fetchListner: OnLocationFetchListner?, context: Context) {
        onLocationFetchListner = fetchListner
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onLocationFetchListner?.OnFailed("PERMISSION DENIED")
            return
        }
        val criteria = Criteria()
        val provider = locationManager!!.getBestProvider(criteria, false)
        Log.d("TAG", "BestProvider: $provider")
        if (provider != null) {
            locationManager!!.requestLocationUpdates(
                provider,
                1,
                0.0f,
                this,
                Looper.getMainLooper()
            )
        } else {
            onLocationFetchListner?.OnFailed("NO PROVIDERS")
        }
    }

    override fun onLocationChanged(location: Location) {
        if (location != null) {
            onLocationFetchListner?.OnComplete(location)
            locationManager!!.removeUpdates(this)
            locationManager = null
        }
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}