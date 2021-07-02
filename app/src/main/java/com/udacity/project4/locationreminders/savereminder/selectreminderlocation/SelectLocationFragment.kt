package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.*
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.BuildConfig
import org.koin.android.ext.android.inject
import java.util.*




class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    companion object {
        const val DEFAULT_ZOOM = 17f
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap
    private val runningOnQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    var reminderSelectedLocationStr = ""
    lateinit var selectedPOI: PointOfInterest
    var latitude = 0.0
    var longitude = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        val mapFragment =
                childFragmentManager.findFragmentById(R.id.select_location_fragment_map_view) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient = this.requireContext().let { LocationServices.getFusedLocationProviderClient(it) }



        binding.selectLocationFragmentSaveLocation.setOnClickListener {
            if (reminderSelectedLocationStr.isNotBlank()) {
                onLocationSelected()
            } else
                _viewModel.showToast.value = "You must select location"
        }


        return binding.root
    }

    private fun onLocationSelected() {
        _viewModel.reminderSelectedLocationStr.value = reminderSelectedLocationStr
        _viewModel.selectedPOI.value = selectedPOI
        _viewModel.latitude.value = latitude
        _viewModel.longitude.value = longitude
        _viewModel.navigationCommand.value = NavigationCommand.Back

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        this.map = googleMap
        enableLocation()
        setMapStyle()
        setMapLongClickListener()
        setOnPoiClickListener()
        setOnMyLocationClickListener()
    }



    @SuppressLint("MissingPermission")
    private fun enableLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
            fusedLocationProviderClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    val zoomLevel = 15f
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, zoomLevel))
                }
            }
        } else {
            _viewModel.showErrorMessage.postValue(getString(R.string.err_select_location))
            var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            val resultCode = REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            requestPermissions(
                    permissionsArray,
                    resultCode
            )
        }

    }

    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
    }



    private fun setOnMyLocationClickListener() {
        map.setOnMyLocationButtonClickListener {
            val location = map.myLocation ?: return@setOnMyLocationButtonClickListener false
            val latLng = LatLng(location.latitude, location.longitude)
            updateCurrentLocation(latLng)
            return@setOnMyLocationButtonClickListener true
        }

        map.setOnMyLocationClickListener { location ->
            val latLng = LatLng(location.latitude, location.longitude)
            updateCurrentLocation(latLng)
        }
    }

    private fun updateCurrentLocation(latLng: LatLng) {
        reminderSelectedLocationStr = latLng.toString()
        selectedPOI = PointOfInterest(latLng, reminderSelectedLocationStr, "Current Location")
        latitude = latLng.latitude
        longitude = latLng.longitude
    }


    private fun addMarker(latLng: LatLng, zoomLevel: Float = DEFAULT_ZOOM) {
        val snippet = String.format(
            Locale.getDefault(),
            "Lat: %1$.5f, Long: %2$.5f",
            latLng.latitude,
            latLng.longitude
        )

        this.map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
        this.map.addMarker(
            MarkerOptions().position(latLng)
                .title(getString(R.string.dropped_pin))
                .snippet(snippet)
        )
        reminderSelectedLocationStr = latLng.toString()
        selectedPOI = PointOfInterest(latLng, reminderSelectedLocationStr, "Custom Location")
        latitude = latLng.latitude
        longitude = latLng.longitude

    }

    private fun setMapStyle() {
        try {
            map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.map_style
                )
            )
        } catch (e: Resources.NotFoundException) {
            Log.i(TAG, e.message.toString())
        }
    }


    private fun setMapLongClickListener() {
        this.map.setOnMapLongClickListener { latLng ->
            addMarker(latLng)
        }
    }

    private fun setOnPoiClickListener() {
        map?.setOnPoiClickListener { poi ->
            map?.clear()
            val poiMarker = map?.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
            reminderSelectedLocationStr = poi.name
            selectedPOI = poi
            latitude = poi.latLng.latitude
            longitude = poi.latLng.longitude
        }
    }




    /* In all cases we need to have location permission. On Android 10+ Q we need background permission as well.*/
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableLocation()
            }
        }else{
            Snackbar.make(
                    binding.rootLayout,
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_LONG
            )
                    .setAction(R.string.settings) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.LIBRARY_PACKAGE_NAME, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()
        }
    }

   /* override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")
        if (
                grantResults.isEmpty() ||
                grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
                (requestCode == REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE &&
                        grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                        PackageManager.PERMISSION_DENIED)
        ) {
            Snackbar.make(
                    binding.rootLayout,
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_LONG
            )
                    .setAction(R.string.settings) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.LIBRARY_PACKAGE_NAME, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()
        } else {
            enableLocation()
        }
    }*/

}


/*  *//*Determines if the App has the appropriate permission across Android Q and all other versions.*//*
    @TargetApi(Build.VERSION_CODES.Q)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ))
        val backgroundLocationApproved =
                if (runningOnQOrLater) {
                    PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                } else {
                    true
                }
        return foregroundLocationApproved && backgroundLocationApproved
    }*/
