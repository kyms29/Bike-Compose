package com.ymsu.bike_compose

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng

class LocationHandler(private val context: Context) {
    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    fun getCurrentLocation(callBack: (location:Location?)->Unit){

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location : Location? ->
            callBack(location)
//            if (location != null) {
//                Log.d("[MapScreen]","[get lastLocation] = location : ${location?.latitude} , ${location?.longitude}")
//                currentLocation = LatLng(location.latitude,location.longitude)
//            }
        }
    }
}