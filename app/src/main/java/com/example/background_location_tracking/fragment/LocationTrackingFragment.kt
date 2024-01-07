package com.example.background_location_tracking.fragment

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.background_location_tracking.*
import com.example.background_location_tracking.database.DBHelper
import com.example.background_location_tracking.database.RoomDatabase
import com.example.background_location_tracking.databinding.FragmentLocationTrackingBinding
import com.example.background_location_tracking.model.LocationData
import com.example.background_location_tracking.model.LocationDataList
import com.example.background_location_tracking.model.LocationEvent
import com.example.background_location_tracking.service.LocationService
import com.example.background_location_tracking.utils.Constant
import com.example.background_location_tracking.utils.PermissionUtils
import com.example.background_location_tracking.viewmodel.LocationViewModel
import com.example.background_location_tracking.viewmodel.LocationViewModelFactory
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*


class LocationTrackingFragment : Fragment(), OnMapReadyCallback, MainActivity.ResultGpsCallBack {

    private var service: Intent? = null
    private var _binding: FragmentLocationTrackingBinding? = null
    private val binding: FragmentLocationTrackingBinding
        get() = _binding!!
    private var mapFragment: SupportMapFragment? = null
    private var googleMap: GoogleMap? = null
    private var currentMarker: Marker? = null
    private val locationData: MutableList<LocationData> = arrayListOf()
    private lateinit var viewModel: LocationViewModel
    private lateinit var navHostFragment: NavHostFragment


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLocationTrackingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isServiceRunning(requireContext(), LocationService::class.java)) {
            binding.muteSwitch.isChecked = true
        }
        val locationDao = DBHelper.getInstance(requireContext()).locationDao()
        val roomDatabase = RoomDatabase(locationDao)
        viewModel = ViewModelProvider(this,
            LocationViewModelFactory(roomDatabase))[LocationViewModel::class.java]

        navHostFragment = requireActivity().supportFragmentManager.findFragmentById(
            R.id.nav_graph_host_fragment
        ) as NavHostFragment

        (activity as MainActivity?)?.setActivityListener(this)

        service = Intent(requireContext(), LocationService::class.java)
        mapFragment = SupportMapFragment.newInstance()

        parentFragmentManager
            .beginTransaction()
            .add(R.id.mapView, mapFragment!!, null)
            .setReorderingAllowed(true)
            .commit()

        mapFragment?.getMapAsync(this)

        // switch click listener
        binding.muteSwitch.setOnCheckedChangeListener(onCheckedChangeListener)
        // checking db null if null don't show previous button
        checkDbLocationList()

        binding.previousLocationBtn.setOnClickListener {
            navHostFragment.navController.navigate(R.id.previousTrackingFragment)
        }
    }

    private fun checkDbLocationList() {
        var locationDataList: List<LocationDataList>
        CoroutineScope(Dispatchers.IO).launch {
            locationDataList = viewModel.fetchAllLocationTracking()
            CoroutineScope(Dispatchers.Main).launch {
                if (locationDataList.isNotEmpty()) {
                    binding.previousLocationBtn.visibility = View.VISIBLE
                }
            }
        }
    }

    // (Event bus) if service is running then it will get the location latLng continue
    @Subscribe
    fun receiveLocationEvent(location: LocationEvent) {
        if (!binding.muteSwitch.isChecked) {
            binding.muteSwitch.isChecked = true
        }
        val mapLocation = LatLng(location.latitude!!, location.longitude!!)
        val data = LocationData(mapLocation, getCurrentDateTime())

        // add latLng and timestamp
        locationData.add(data)

        googleMap?.apply {
            currentMarker?.remove()
            currentMarker = addMarker(MarkerOptions().position(mapLocation).title("Marker"))
            moveCamera(CameraUpdateFactory.newLatLng(mapLocation))
            animateCamera(CameraUpdateFactory.zoomTo(15F), 1500, null)
        }
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isCompassEnabled = false
    }

    // checking the permission allowed
    private val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        if (isChecked) {
            if (!PermissionUtils.isAccessFineLocationGranted(requireContext())) {
                locationPermissions.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } else {
                showGPSOffDialog()
            }
        } else {
            serviceStopLogic()
        }
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceStopLogic()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    // dialog for gps enable
    private fun showGPSOffDialog() {
        val locationRequest = LocationRequest.create()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val result = LocationServices.getSettingsClient(requireActivity())
            .checkLocationSettings(builder.build())

        result.addOnCompleteListener { task ->
            try {
                task.getResult(ApiException::class.java)
                requireActivity().startService(service)
            } catch (e: ApiException) {
                e.printStackTrace()
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolveApiException = e as ResolvableApiException
                        resolveApiException.startResolutionForResult(requireActivity(),
                            Constant.REQUEST_CODE_1)

                    } catch (sendIntentException: IntentSender.SendIntentException) {
                    }
                }
            }
        }
    }


    private val backgroundLocation =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                showGPSOffDialog()
            }
        }

    private val locationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            when {
                it.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {

                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        backgroundLocation.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }

                }
                it.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {

                }
            }
        }

    // timestamp
    private fun getCurrentDateTime(): String {
        val currentTimestamp = System.currentTimeMillis()
        val currentDate = Date(currentTimestamp)
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(currentDate)
    }

    // check service is running
    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = manager.getRunningServices(Int.MAX_VALUE)

        for (service in services) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }

        return false
    }

    // when service is stop then all list data add in the db after clear the list
    private fun serviceStopLogic() {
        Toast.makeText(requireContext(),"Location Tracking Stop", Toast.LENGTH_LONG).show()
        requireActivity().stopService(service)
        currentMarker?.remove()
        if (locationData.isNotEmpty()) {
            val locationDataList = LocationDataList(locationData = locationData)
            viewModel.insertLocationList(locationDataList)
        }
        if (binding.previousLocationBtn.visibility == View.GONE) {
            checkDbLocationList()
        }
        locationData.clear()
    }

    // gps dialog callback
    override fun gpsCallBack() {
        requireActivity().startService(service)
    }


}