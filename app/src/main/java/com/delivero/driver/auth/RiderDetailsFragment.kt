package com.delivero.driver.auth

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.delivero.driver.BuildConfig
import com.delivero.driver.MainActivity
import com.delivero.driver.R
import com.delivero.driver.adapters.ArrayAdapterWithIcons
import com.delivero.driver.adapters.AutoCompleteAdapter
import com.delivero.driver.adapters.IconListAdapter
import com.delivero.driver.databinding.FragmentDriverDetailsBinding
import com.delivero.driver.helpers.MapUtils
import com.delivero.driver.helpers.Preference
import com.delivero.driver.helpers.Utils
import com.delivero.driver.helpers.toast
import com.delivero.driver.interfaces.Collections
import com.delivero.driver.interfaces.CompleteListener
import com.delivero.driver.models.MyLocation
import com.delivero.driver.models.ServiceType
import com.delivero.driver.models.User
import com.delivero.driver.models.VehicleTypes
import com.delivero.driver.services.MyLocationService
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jianastrero.capiche.iNeed
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RiderDetailsFragment:Fragment() {
    private var flag=0
    private lateinit var placesClient: PlacesClient
    private lateinit var pickupAdapter: AutoCompleteAdapter
    private lateinit var binding:FragmentDriverDetailsBinding
    private var utils=Utils()
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user=RiderDetailsFragmentArgs.fromBundle(requireArguments()).user
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=FragmentDriverDetailsBinding.inflate(inflater, container, false)
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity().applicationContext, BuildConfig.GOOGLE_API_KEY)
        }

        placesClient = Places.createClient(requireActivity())

        binding.pickupValue.threshold = 1

        pickupAdapter = AutoCompleteAdapter(requireContext(), placesClient)


        binding.serviceType.setOnClickListener {
            getServices()
        }
        binding.addService.setOnClickListener {
            getInput()
        }

        binding.vehicleType.setOnClickListener {
            getVehicleTypes()
        }
 binding.pickupValue.setOnItemClickListener { _, _, position, _ ->
            try {
                binding.progress.show()
                val item = pickupAdapter.getItem(position)
                val placeID: String = item.placeId

//                To specify which data types to return, pass an array of Place.Fields in your FetchPlaceRequest
//                Use only those fields which are required.
                val placeFields = listOf(
                    Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG
                )
                val request: FetchPlaceRequest? = FetchPlaceRequest.builder(placeID, placeFields)
                    .build()
                if (request != null) {
                    placesClient.fetchPlace(request).addOnSuccessListener { task ->
                        binding.progress.hide()

                        user.serviceLatitude=task.place.latLng!!.latitude
                        user.serviceLongitude=task.place.latLng!!.longitude
                        user.serviceLocation= task.place.name!!
                       /* trip.fromName = task.place.name!!
                        fromAddress = task.place.address!!
                       */
                    }
                        .addOnFailureListener { e ->
                            binding.progress.hide()
                            e.printStackTrace()
                            requireActivity().toast("Failed to retrieve location")
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        binding.pickupValue.setAdapter(pickupAdapter)

        checkLocationPermission()

        return binding.root
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun myLocationReady(myLocation: MyLocation) {
        if(!isAdded){
            return
        }
        user.serviceLongitude=myLocation.longitude
        user.serviceLatitude=myLocation.latitude

        MapUtils.reverseGeocode(object : CompleteListener {
            override fun onComplete(value: String) {
                //trip.fromName = value
                binding.pickupValue.setText(value)
                user.serviceLocation=value
                //fromAddress = value
            }
        }, LatLng(user.serviceLatitude,user.serviceLongitude))

    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (flag == 1) {
                flag = 0
                checkGpsEnabled()
            }
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

    private fun getInput() {
        val serviceType=binding.serviceType.text.toString()
        val vehicleType=binding.vehicleType.text.toString()
        val capacity=binding.capacity.text.toString()
        val regNumber=binding.regNumber.text.toString()
        val location=binding.pickupValue.text.toString()

        if (serviceType.isEmpty()){
            utils.showMessageDialog(requireContext(),"You missed something","Please select the service type")
            return
        }

        if (vehicleType.isEmpty()){
            utils.showMessageDialog(requireContext(),"You missed something","Please provide the vehicle type")
            return
        }
        if (capacity.isEmpty()){
            utils.showMessageDialog(requireContext(),"You missed something","Please select the carriage capacity")
            return
        }

        if (regNumber.isEmpty()){
            utils.showMessageDialog(requireContext(),"You missed something","Please provide the vehicle registration number")
            return
        }

        if (location.isEmpty()){
            utils.showMessageDialog(requireContext(),"You missed something","Please provide your service location")
            return
        }

        var roles=user.roles.toMutableList()
        roles.add(vehicleType)
        roles=roles.distinct().toMutableList()
        user.roles=roles

        user.capacity=capacity;
        user.serviceLocation=location
        user.regNumber=regNumber;

        addGeoFire(serviceType,vehicleType)

        binding.progress.show()
        Firebase.firestore.collection(Collections.USERS)
            .document(Firebase.auth.currentUser!!.uid)
            .set(user)
            .addOnSuccessListener {
                binding.progress.hide()
                Preference.saveUser(requireContext(),user)
                startActivity(Intent(requireContext(),MainActivity::class.java))
            }.addOnFailureListener {
                binding.progress.hide()
                utils.showMessageDialog(requireContext(),"Failed to add service","${it.message}")
            }

    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun addGeoFire(serviceType: String, vehicleType: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val georef=Firebase.database.getReference(serviceType)
            val georef2=Firebase.database.getReference(vehicleType)
            val georef3=Firebase.database.getReference("Drivers")

            GeoFire(georef).setLocation(Firebase.auth.currentUser!!.uid, GeoLocation(user.serviceLatitude,user.serviceLongitude))
            GeoFire(georef2).setLocation(Firebase.auth.currentUser!!.uid, GeoLocation(user.serviceLatitude,user.serviceLongitude))
            GeoFire(georef3).setLocation(Firebase.auth.currentUser!!.uid, GeoLocation(user.serviceLatitude,user.serviceLongitude))

        }
    }

    private fun getServices() {
        val t1=ServiceType(R.drawable.delivery,"Delivery");
        val t2=ServiceType(R.drawable.breakdown,"Breakdown");
        val t3=ServiceType(R.drawable.emergency,"Emergency");



        val services= mutableListOf(t1,t2,t3);
        val items= mutableListOf<String>()
        val icons= mutableListOf<Int>()
        services.forEach {
            items.add(it.typeName)
            icons.add(it.icon)
        }


        val adapter=ArrayAdapterWithIcons(requireContext(),items,icons)


        MaterialAlertDialogBuilder(requireContext())
            .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_white_rounded_10))
            .setTitle("Please Select A Service Type")
            .setAdapter(adapter){
                    dialog,which->
                binding.serviceType.setText(services[which].typeName)
                var roles=user.roles.toMutableList()
                roles.add(services[which].typeName)
                roles.add("Driver")
                roles=roles.distinct().toMutableList()
                user.roles=roles
                dialog?.dismiss()

            }
            .create()
            .show()

    }

    private fun getVehicleTypes(){
        var serviceType=binding.serviceType.text.toString()
        if (serviceType.isEmpty()){
            utils.showMessageDialog(requireContext(),"Missing info","Please select the service type first")
        return
        }
        binding.progress.show()
        Firebase.firestore.collection(Collections.TYPES)
            .whereEqualTo("purpose",serviceType)
            .get()
            .addOnSuccessListener { snapshot ->
                binding.progress.hide()
                if (snapshot.isEmpty){
                    return@addOnSuccessListener
                }
                val types=snapshot.toObjects(VehicleTypes::class.java)
                val typesArray=types.map {
                    it.typeName
                }.toTypedArray();
               MaterialAlertDialogBuilder(requireContext())
                   .setBackground(ContextCompat.getDrawable(requireContext(),R.drawable.bg_white_rounded_10))
                   .setTitle("Select a vehicle type")
                   .setSingleChoiceItems(typesArray,0){
                       dialog,which->
                       binding.vehicleType.setText(typesArray[which])
                       dialog?.dismiss()
                   }.create()
                   .show()

            }
    }


}