package com.delivero.driver.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.annotation.Nullable
import com.delivero.driver.models.MyLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.greenrobot.eventbus.EventBus

class MyLocationService : Service() {
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1000
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private lateinit var levent: MyLocation

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
            levent.latitude = currentLocation.latitude
            levent.longitude = currentLocation.longitude
            processRequest()

        }
    }

    fun processRequest(){

            EventBus.getDefault().post(levent)
            mFusedLocationClient?.removeLocationUpdates(locationCallback)
            stopSelf()


}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        levent= MyLocation()
        startLocationUpdates()
        return START_NOT_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        mFusedLocationClient!!.requestLocationUpdates(
            locationRequest!!,
            locationCallback,
            Looper.myLooper()!!
        )
    }

    @Nullable
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