package com.example.background_location_tracking.fragment

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.NavHostFragment
import com.example.background_location_tracking.MyApplication
import com.example.background_location_tracking.R
import com.example.background_location_tracking.databinding.FragmentLocationTrackingBinding
import com.example.background_location_tracking.model.LocationData
import com.example.background_location_tracking.model.LocationDataList
import com.example.background_location_tracking.model.LocationEvent
import com.example.background_location_tracking.service.LocationService
import com.example.background_location_tracking.utils.PermissionUtils
import com.example.background_location_tracking.viewmodel.LocationViewModel
import com.example.background_location_tracking.viewmodel.LocationViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import javax.inject.Inject


class LocationTrackingFragment : Fragment(), OnMapReadyCallback {

    private var service: Intent? = null
    private var _binding: FragmentLocationTrackingBinding? = null
    private val binding: FragmentLocationTrackingBinding
        get() = _binding!!
    private var mapFragment: SupportMapFragment? = null
    private var googleMap: GoogleMap? = null
    private var currentMarker: Marker? = null
    private val locationData: MutableList<LocationData> = arrayListOf()

    @Inject
    lateinit var viewModel: LocationViewModel

    @Inject
    lateinit var viewModelFactory: LocationViewModelFactory

    private lateinit var navHostFragment: NavHostFragment
    private var checkLocation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLocationTrackingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity().application as MyApplication).appComponent.inject(this)
        if (::viewModel.isInitialized) {
            viewModel = ViewModelProvider(this, viewModelFactory)[LocationViewModel::class.java]
        }

        if (isServiceRunning(requireContext(), LocationService::class.java)) {
            trueMuteSwitch()
        }

        navHostFragment = requireActivity().supportFragmentManager.findFragmentById(
            R.id.nav_graph_host_fragment
        ) as NavHostFragment

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
        //calling db
        viewModel.fetchAllLocationTracking()

        viewModel.locationData.asLiveData().observe(this) { locationData ->
            if (locationData.isNotEmpty()) {
                binding.previousLocationBtn.visibility = View.VISIBLE
            }
        }

    }

    // (Event bus) if service is running then it will get the location latLng continue
    @Subscribe
    fun receiveLocationEvent(location: LocationEvent) {
        if (!binding.muteSwitch.isChecked) {
            trueMuteSwitch()
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
    private val onCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                permissionCheck()
            } else {
                serviceStopLogic()
            }
        }

    private fun permissionCheck() {
        if (PermissionUtils.isAccessFineLocationGranted(requireContext())) {
            if (PermissionUtils.isAccessBackgroundLocationGranted(requireContext())) {
                if (PermissionUtils.isLocationEnabled(requireContext())) {
                    requireActivity().startService(service)
                } else {
                    showGPSOffDialog()
                }
            } else {
                if (PermissionUtils.isCheckBackgroundLocationPermissionDenied(requireActivity())) {
                    falseMuteSwitch()
                    backgroundLocation.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    falseMuteSwitch()
                    openSettings()
                }
            }
        } else {
            PermissionUtils.requestLocationPermission(accessLocationResult)
        }
    }

    private fun openSettings() {
        Toast.makeText(requireContext(),
            "allow the all time location permission",
            Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        requireActivity().startActivity(intent)
    }


    private val accessLocationResult = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (PermissionUtils.isAccessFineLocationGranted(requireContext())) {
            if (PermissionUtils.isAccessBackgroundLocationGranted(requireContext())) {
                if (PermissionUtils.isLocationEnabled(requireContext())) {
                    requireActivity().startService(service)
                } else {
                    showGPSOffDialog()
                }
            } else {
                if (PermissionUtils.isCheckBackgroundLocationPermissionDenied(requireActivity())) {
                    falseMuteSwitch()
                    backgroundLocation.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    falseMuteSwitch()
                    openSettings()
                }
            }
        } else {
            falseMuteSwitch()
        }
    }

    private val backgroundLocation = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (PermissionUtils.isAccessFineLocationGranted(requireContext())) {
            if (PermissionUtils.isAccessBackgroundLocationGranted(requireContext())) {
                if (PermissionUtils.isLocationEnabled(requireContext())) {
                    trueMuteSwitch()
                    requireActivity().startService(service)
                } else {
                    showGPSOffDialog()
                }
            } else {
                falseMuteSwitch()
            }
        } else {
            falseMuteSwitch()
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


    override fun onResume() {
        super.onResume()
        if (checkLocation) {
            falseMuteSwitch()
        }
    }

    private fun falseMuteSwitch() {
        binding.muteSwitch.isChecked = false
    }

    private fun trueMuteSwitch() {
        binding.muteSwitch.isChecked = true
    }

    // dialog for gps enable
    private fun showGPSOffDialog() {
        falseMuteSwitch()
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        requireContext().startActivity(intent)
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
        requireActivity().stopService(service)
        currentMarker?.remove()
        if (locationData.isNotEmpty()) {
            Log.d("TAG", "serviceStopLogic: check the location $locationData")
            val locationDataList = LocationDataList(locationData = locationData)
            viewModel.insertLocationList(locationDataList)
        }
        if (binding.previousLocationBtn.visibility == View.GONE) {
            checkDbLocationList()
        }
        locationData.clear()
    }

}