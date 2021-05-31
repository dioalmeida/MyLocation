package com.doa.mylocationapp

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*

//We need to add the onMapReadyCallback interface and override de onMapReady
// OnMarkerClickListener is to handle the marker clicks
class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private var showInfoWindow =false
    private var googleMap: GoogleMap? = null
    private var mapView: MapView? = null
    private val LOCATION_PERMISSION = 1
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var mLastLocation: Location? = null
    lateinit var mLocationRequest: LocationRequest
    private var mCurrLocationMarker: Marker? = null
    private var mLocationCallback: LocationCallback? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mv_map)
        mapView!!.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        askPermissions()
    }

    //Here we check and ask for the permissions needed
    private fun askPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.INTERNET)
                    == PackageManager.PERMISSION_GRANTED
            ) {
                //If permissions are ok then set the mapView callback and get start location updates
                mLocationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        val locationList = locationResult.locations
                        if (locationList.isNotEmpty()) {
                            //The last location in the list is the newest
                            val location = locationList.last()
                            mLastLocation = location
                            if (mCurrLocationMarker != null) {
                                mCurrLocationMarker?.remove()
                            }

                            //Place current location marker
                            val latLng = LatLng(location.latitude, location.longitude)
                            val markerOptions = MarkerOptions()
                            markerOptions.position(latLng)
                            markerOptions.title("Current Position:" + location.latitude + ";" + location.longitude)
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                            mCurrLocationMarker = googleMap?.addMarker(markerOptions)

                            if (showInfoWindow){
                                mCurrLocationMarker?.showInfoWindow()
                            }

                            //move map camera
                            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0F))
                        }
                    }
                }
                mapView!!.getMapAsync(this)
            } else { //if we don't have permissions we ask for them
                requestPermissions(
                        arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.INTERNET),
                        LOCATION_PERMISSION
                )
            }
        } else {
            //TODO deal with android api versions < M
        }
    }

    //Here we deal with the result of the permission request
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //If permissions are ok then set the mapView callback
                mapView!!.getMapAsync(this)
            }
        } else {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Attention")
            builder.setMessage("Permissions are needed for this work.")
            builder.setPositiveButton(android.R.string.ok, null)
            builder.setOnDismissListener(DialogInterface.OnDismissListener { })
            builder.show()
        }
        return
    }


    //after the permissions are asked we can load the map and simply se the property
    // setMyLocationEnabled(true)
    override fun onMapReady(map: GoogleMap?) {
        googleMap = map

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            askPermissions()
            return
        }

        //Location request properties, dealt with in milliseconds(confirm this!)
        mLocationRequest = LocationRequest.create().apply {
            interval = 100
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 100
        }
        googleMap!!.isMyLocationEnabled = true

        //show info window after update because after each update the infowindow is rebuilt.
        googleMap!!.setOnMarkerClickListener(this)
        googleMap!!.setOnMapClickListener {
            showInfoWindow = !showInfoWindow
        }

        fusedLocationClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())

        //It's possible to customize the maps, you can build and find presets on:
        //https://mapstyle.withgoogle.com/
        //https://snazzymaps.com/
        //They generate a json file, copy it and put it on your raw folder on your resources
        //NOTE: Android studio does not create that folder by default, don't be alarmed, just
        // add an Android Resource Directory for raw resources.
        val success = googleMap!!.setMapStyle( //we receive a success response for failure treatment
                MapStyleOptions.loadRawResourceStyle(
                        baseContext, R.raw.map_style
                )
        )
    }


    override fun onMarkerClick(marker: Marker?): Boolean {
        showInfoWindow = !showInfoWindow
        return false
    }
    //ATTENTION: YOU MUST OVERRIDE THESE IN ORDER FOR THE MAP TO WORK ACCORDING TO THE DOCUMENTATION

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to [Activity.onResume] of the containing
     * Activity's lifecycle.
     */
    override fun onResume() {
        super.onResume()
        if (mapView != null)
            mapView?.onResume()
    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to [Activity.onStart] of the containing
     * Activity's lifecycle.
     */
    override fun onStart() {
        super.onStart()
        if (mapView != null)
            mapView?.onStart()
    }

    /**
     * Called when the Fragment is no longer started.  This is generally
     * tied to [Activity.onStop] of the containing
     * Activity's lifecycle.
     */
    override fun onStop() {
        super.onStop()
        if (mapView != null)
            mapView?.onStop()
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after [.onStop] and before [.onDetach].
     */
    override fun onDestroy() {
        super.onDestroy()
        if (mapView != null)
            mapView?.onDestroy()
    }

//     NOTE: this one is needed in fragments, but not in activities
//    /**
//     * Called when the view previously created by [.onCreateView] has
//     * been detached from the fragment.  The next time the fragment needs
//     * to be displayed, a new view will be created.  This is called
//     * after [.onStop] and before [.onDestroy].  It is called
//     * *regardless* of whether [.onCreateView] returned a
//     * non-null view.  Internally it is called after the view's state has
//     * been saved but before it has been removed from its parent.
//     */
//    override fun onDestroyView() {
//        super.onDestroyView()
////        if (mapView != null)
////            mapView?.destro()
//    }

    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to [Activity.onPause] of the containing
     * Activity's lifecycle.
     */
    override fun onPause() {
        super.onPause()

        //No location updates when in the background comment this for background location updates
        fusedLocationClient?.removeLocationUpdates(mLocationCallback)
        if (mapView != null)
            mapView?.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mapView != null)
            mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (mapView != null)
            mapView?.onLowMemory()
    }

}