package com.example.background_location_tracking.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.background_location_tracking.model.LocationDataList
import com.example.background_location_tracking.R
import com.example.background_location_tracking.database.DBHelper
import com.example.background_location_tracking.database.RoomDatabase
import com.example.background_location_tracking.databinding.FragmentPreviousTrackingBinding
import com.example.background_location_tracking.viewmodel.LocationViewModel
import com.example.background_location_tracking.viewmodel.LocationViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PreviousTrackingFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentPreviousTrackingBinding? = null
    private val binding: FragmentPreviousTrackingBinding
        get() = _binding!!
    private lateinit var viewModel: LocationViewModel
    private var googleMap: GoogleMap? = null
    private var currentMarker: Marker? = null
    private var mapFragment: SupportMapFragment? = null
    private lateinit var navHostFragment: NavHostFragment


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPreviousTrackingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val locationDao = DBHelper.getInstance(requireContext()).locationDao()
        val roomDatabase = RoomDatabase(locationDao)
        viewModel = ViewModelProvider(this,
            LocationViewModelFactory(roomDatabase))[LocationViewModel::class.java]

        navHostFragment = requireActivity().supportFragmentManager.findFragmentById(
            R.id.nav_graph_host_fragment
        ) as NavHostFragment

        mapFragment = SupportMapFragment.newInstance()
        parentFragmentManager
            .beginTransaction()
            .add(R.id.mapView, mapFragment!!, null)
            .setReorderingAllowed(true)
            .commit()
        mapFragment?.getMapAsync(this)

        binding.back.setOnClickListener {
            navHostFragment.navController.navigateUp()
        }

    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        var locationDataList: List<LocationDataList>
        val waypoints: MutableList<LatLng> = arrayListOf()

        // fetch All location tracking data from db
        CoroutineScope(Dispatchers.IO).launch {
            locationDataList = viewModel.fetchAllLocationTracking()
            CoroutineScope(Dispatchers.Main).launch {
                if (locationDataList.isNotEmpty()) {
                    var count = 0;
                    for (locationData in locationDataList) {
                        if (locationData.locationData.isNotEmpty()) {
                            for (data in locationData.locationData) {
                                // add latLng in list
                                data.latLng?.let { waypoints.add(it) }
                            }
                            // get first and last marker
                            val firstValue = locationData.locationData.first()
                            val lastValue = locationData.locationData.last()
                            val firstLocation: LatLng = firstValue.latLng!!
                            val lastLocation: LatLng = lastValue.latLng!!
                            count++
                            // google camera and zoom animate
                            googleMap?.apply {
                                moveCamera(CameraUpdateFactory.newLatLng(firstLocation))
                                animateCamera(CameraUpdateFactory.zoomTo(23F), 1500, null)

                                //add count for maker
                                currentMarker =
                                    addMarker(MarkerOptions().position(firstLocation)
                                        .title("$count Start Marker"))
                                currentMarker =
                                    addMarker(MarkerOptions().position(lastLocation)
                                        .title("$count End Marker"))
                            }
                        }
                    }
                }
                // polyline added in the map
                val polylineOptions = PolylineOptions().apply {
                    width(18f)
                    color(Color.BLUE)
                    geodesic(true)
                    addAll(waypoints)
                }
                googleMap?.addPolyline(polylineOptions)
            }
        }

    }

}