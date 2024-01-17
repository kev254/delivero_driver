package com.delivero.driver.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import com.delivero.driver.helpers.Preference
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LocationUpdateService : Service() {
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 5000
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null

    override fun onCreate() {
        super.onCreate()
        initData()

    }


    //Location Callback
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if (locationResult.lastLocation==null){
                return
            }
            val currentLocation: Location = locationResult.lastLocation!!

            //Share/Publish Location
           if (Firebase.auth.currentUser==null){
               return
           }
            val georef1=GeoFire(Firebase.database.getReference("Drivers"))
            georef1.setLocation(Firebase.auth.currentUser!!.uid, GeoLocation(currentLocation.latitude,
                currentLocation.longitude))

            val currentRider=Preference.getUser(applicationContext)
            if (currentRider!=null){
                val excempted= listOf("User","Customer","Driver","Admin","Super Admin","Staff")
                currentRider.roles.forEach {
                    if (!excempted.contains(it)){
                        val georefx=GeoFire(Firebase.database.getReference(it))
                        georefx.setLocation(Firebase.auth.currentUser!!.uid, GeoLocation(currentLocation.latitude,
                            currentLocation.longitude))

                    }
                }
            }


        }
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLocationUpdates()
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        mFusedLocationClient!!.requestLocationUpdates(
            locationRequest!!,
            locationCallback,
            Looper.myLooper()!!
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun initData() {
        locationRequest = LocationRequest.create()
        locationRequest!!.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest!!.priority = Priority.PRIORITY_HIGH_ACCURACY
        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(baseContext)
    }
}