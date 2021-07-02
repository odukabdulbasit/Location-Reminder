package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity.Companion.TAG
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.*
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.BuildConfig
import org.koin.android.ext.android.inject


private const val DEFAULT_GEOFENCE_RADIUS = 100F

class SaveReminderFragment : BaseFragment() {

    lateinit var geofencingClient: GeofencingClient
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private val runningQorLater =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    //private var isPermissionGrentedAndLocationOn = false

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this


        binding.fragmentSaveReminderSelectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.fragmentSaveReminderActionSaveReminder.setOnClickListener {
            checkPermissionsAndStartGeofencing()
        }
    }

    private fun startSetUpGeofence() {
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value ?: 0.0
        val longitude = _viewModel.longitude.value ?: 0.0
        var reminderDataItem = ReminderDataItem(
                title = title,
                description = description,
                latitude = latitude,
                longitude = longitude,
                location = location
        )

        if (_viewModel.validateEnteredData(reminderDataItem)) {

            //checkPermissionAndLocationOnToAddGeofencing(reminderDataItem)
                addGeofenceForClue(reminderDataItem)
            _viewModel.saveReminder(reminderDataItem)
        }
    }


    @SuppressLint("MissingPermission")
    private fun addGeofenceForClue(reminder: ReminderDataItem) {

        val geofence = Geofence.Builder()
            .setRequestId(reminder.id)
            .setCircularRegion(
                    reminder.latitude!!,
                    reminder.longitude!!,
                    DEFAULT_GEOFENCE_RADIUS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)

    }


    /**
     * Starts the permission check and Geofence process only if the Geofence associated with the
     * current hint isn't yet active.
     */
    private fun checkPermissionsAndStartGeofencing() {
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            checkDeviceLocationSettingsAndStartGeofence()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    /*
    * Determines if the App has the appropriate permission across Android Q and all other versions.
     */
    @TargetApi(Build.VERSION_CODES.Q)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ))
        val backgroundLocationApproved =
            if (runningQorLater) {
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundLocationApproved
    }

   /* *//**
     * Request ACCESS_FINE_LOCATION and on Android 10+ (Q) ACCESS_BACKGROUND_PERMISSION
     *//*
    @TargetApi(29)
    private fun checkAndRequestForegroundAndBackgroundPermissions() {
        //Check if the permission have been already approved, if so we don't ask again and return
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            checkDeviceLocationSettings()
            return
        }
        var permissionArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQorLater -> {
                permissionArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
        }
        requestPermissions(permissionArray, resultCode)
    }*/
    /*
    *  Requests ACCESS_FINE_LOCATION and (on Android 10+ (Q) ACCESS_BACKGROUND_LOCATION.
    */
    @TargetApi(29 )
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return

        // Else request the permission
        // this provides the result[LOCATION_PERMISSION_INDEX]
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = when {
            runningQorLater -> {
                // this provides the result[BACKGROUND_LOCATION_PERMISSION_INDEX]
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }

        Log.d(TAG, "Request foreground only location permission")
        requestPermissions(permissionsArray, resultCode)

    }

    //We check if the user has their Location Device Enabled, if not we ask to turn it ON
    //using the Location request
    @SuppressLint("MissingPermission")
    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {

            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_LOW_POWER
            }
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            //LocationServices to get the Settings Client and create a val
            // called locationSettingsResponseTask to check the location settings
            val settingsClient = LocationServices.getSettingsClient(requireActivity())
            val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())



            locationSettingsResponseTask.addOnFailureListener { exception ->
                if (exception is ResolvableApiException && resolve) {
                    try {
                       /* startIntentSenderForResult(exception.getResolution().getIntentSender(), REQUEST_TURN_DEVICE_LOCATION_ON,
                                null, 0, 0, 0,null)*/

                      /*  startIntentSenderForResult(
                                geofencePendingIntent.intentSender,
                                REQUEST_TURN_DEVICE_LOCATION_ON,
                                null,
                                0,
                                0,
                                0,
                                null
                        )*/
                        exception.startResolutionForResult(
                                requireActivity(),
                                REQUEST_TURN_DEVICE_LOCATION_ON
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Log.d(TAG, "Error getting location Settings" + sendEx.message)
                    }
                } else {
                    Snackbar.make(
                            binding.saveReminderLayout,
                            R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                    ).setAction(android.R.string.ok) {
                        checkDeviceLocationSettingsAndStartGeofence()
                    }.show()
                }
            }
            locationSettingsResponseTask.addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.v(TAG, "Location ON")
                    //isPermissionGrentedAndLocationOn = true

                    startSetUpGeofence()
                }
            }
    }

    /* In all cases we need to have location permission. On Android 10+ Q we need background permission as well.*/
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        Log.d(com.udacity.project4.locationreminders.TAG, "onRequestPermissionResult")
        if (
                grantResults.isEmpty() ||
                grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
                (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                        grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                        PackageManager.PERMISSION_DENIED)
        ) {
            Snackbar.make(
                    binding.saveReminderLayout,
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
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }
}
