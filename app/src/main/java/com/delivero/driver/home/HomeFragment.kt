package com.delivero.driver.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.delivero.driver.R
import com.delivero.driver.databinding.FragmentHomeBinding
import com.delivero.driver.helpers.MapUtils
import com.delivero.driver.helpers.Preference
import com.delivero.driver.helpers.Utils
import com.delivero.driver.interfaces.CompleteListener
import com.delivero.driver.models.LocationDTO
import com.delivero.driver.models.MyLocation
import com.delivero.driver.models.User
import com.delivero.driver.services.MyLocationService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jianastrero.capiche.iNeed
import com.serhatleventyavas.ripplemapview.RippleMapView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.roundToInt

class HomeFragment: Fragment(), OnMapReadyCallback {
    private lateinit var binding:FragmentHomeBinding
    private var rippleView: RippleMapView? = null
    private var grayPolyline: Polyline? = null
    private var destinationLatLng: LatLng? = null
    private var destinationMarker: Marker? = null
    private var originMarker: Marker? = null
    private var myLocationMarker: Marker?=null
    private var pickupLatLng: LatLng? = null
    private var flag=0
    private var defaultLocation: LatLng? = null
    private var googleMap: GoogleMap?=null

    private lateinit var bounds: LatLngBounds.Builder
    private lateinit var mapView: MapView

    private lateinit var currentUser: User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentUser=Preference.getUser(requireContext())!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
       binding=FragmentHomeBinding.inflate(inflater, container, false)
        mapView=binding.mapView

        setUpMap(savedInstanceState)
        return binding.root;
    }

    private fun setUpMap(savedInstanceState: Bundle?) {
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        // needed to get the map to display immediately
        try {
            MapsInitializer.initialize(
                requireActivity().applicationContext
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mapView.getMapAsync(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun myLocationReady(myLocation: MyLocation) {
        if(!isAdded){
            return
        }
        currentUser.latitude=myLocation.longitude
        currentUser.longitude=myLocation.latitude
        defaultLocation= LatLng(myLocation.latitude,myLocation.longitude)
        shoMyLocation(defaultLocation!!)
        animateToBounds()

        MapUtils.reverseGeocode(object : CompleteListener {
            override fun onComplete(value: String) {
                Utils().updateUserCoordinates(currentUser.latitude,currentUser.longitude,value)
            }
        }, LatLng(currentUser.serviceLatitude,currentUser.serviceLongitude))

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun locationChanged(myLocation:LocationDTO){
        Log.e("Location","Delivered")
        if(!isAdded){
            return
        }
        currentUser.latitude=myLocation.longitude
        currentUser.longitude=myLocation.latitude
        defaultLocation= LatLng(myLocation.latitude,myLocation.longitude)
        shoMyLocation(defaultLocation!!)
        animateToBounds()

        MapUtils.reverseGeocode(object : CompleteListener {
            override fun onComplete(value: String) {
                Utils().updateUserCoordinates(currentUser.latitude,currentUser.longitude,value)
            }
        }, LatLng(currentUser.serviceLatitude,currentUser.serviceLongitude))

    }
    private fun shoMyLocation(latLng: LatLng) {
        myLocationMarker?.remove()
        myLocationMarker = googleMap?.addMarker(
            MarkerOptions().icon(
                BitmapDescriptorFactory.fromBitmap(
                    MapUtils.getMyMarkerBitmap(
                        requireContext()
                    )
                )
            )
                .position(latLng)
                .title("Your Position")
        )
        rippleView?.withLatLng(defaultLocation)
        if (rippleView?.isAnimationRunning==false){
            rippleView?.startRippleMapAnimation()
        }
    }

    private fun animateToBounds() {
        bounds = LatLngBounds.builder()
        bounds.include(defaultLocation!!)
        if (pickupLatLng!=null){
            if (rippleView?.isAnimationRunning==true){
                rippleView?.stopRippleMapAnimation()
            }
            bounds.include(pickupLatLng)
        }
        if (destinationLatLng!=null){
            bounds.include(pickupLatLng)
        }
        val height = resources.displayMetrics.heightPixels.times(0.7).roundToInt()
        val width = resources.displayMetrics.widthPixels
        val padding = width.times(0.20).roundToInt()
        val update = CameraUpdateFactory.newLatLngBounds(bounds.build(), width, height, padding)
        googleMap?.animateCamera(update)
    }

    override fun onMapReady(p0: GoogleMap) {
        this.googleMap = p0
        defaultLocation = LatLng(0.0236, 37.9062)
        googleMap?.setIndoorEnabled(true)
        googleMap?.isTrafficEnabled=true
        rippleView= this.googleMap?.let {
            RippleMapView.Builder(requireContext(), it)
                .fillColor(ContextCompat.getColor(requireContext(), R.color.secondary))
                .strokeColor(ContextCompat.getColor(requireContext(),R.color.secondary))
                .latLng(LatLng(41.009146, 29.034022))
                .numberOfRipples(3)
                .build()
        }
        shoMyLocation(defaultLocation!!)
        rippleView?.startRippleMapAnimation()
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (isAdded) {
            requireActivity().iNeed(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                onGranted = {
                    checkGpsEnabled()

                },
                onDenied = {
                    MaterialAlertDialogBuilder(requireContext())
                        .setBackground(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.bg_white_rounded_10
                            )
                        )
                        .setTitle("Please allow location permissions")
                        .setMessage("We need your location permission in order to get you the nearest rider.")
                        .setPositiveButton("Allow in settings") { dialog, _ ->
                            dialog?.dismiss()
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", requireContext().packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }.setNegativeButton("Cancel") { dialog, _ ->
                            dialog?.dismiss()
                        }.create()
                        .show()

                })
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (flag == 1) {
                flag = 0
                checkGpsEnabled()
            }
        }

    private fun checkGpsEnabled() {
        val manager: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            ) && !manager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER) && !manager.isProviderEnabled(
                LocationManager.FUSED_PROVIDER
            )
        ) {
            flag = 1
            resultLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            Toast.makeText(requireContext(), "Please turn on GPS", Toast.LENGTH_LONG).show()
        } else {
            startLocationService()
        }

    }
    private fun startLocationService() {
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this)
        }
        val intent = Intent(requireContext(), MyLocationService::class.java)
        requireActivity().startService(intent)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }
}