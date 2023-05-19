package com.navio.sketches_and_location.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.navio.sketches_and_location.R
import com.navio.sketches_and_location.databinding.FragmentLocationLayoutBinding
import com.navio.sketches_and_location.databinding.FragmentSketchesLayoutBinding

class FragmentLocation : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, LocationListener {

    lateinit var binding: FragmentLocationLayoutBinding

    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentLocationLayoutBinding.inflate(layoutInflater)
        createFragment()
        launchToast("Mantén pulsado una ubicación para conocer sus coordenadas.")
    }

    private fun createFragment() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Modify top bar
        (activity as AppCompatActivity).supportActionBar?.apply {
//          hide()
            title = "Sketches and Location"
            subtitle = "Location"
        }
        return binding.root
    }

    private fun zoomToCurrentLocation() {
        // Get the location manager
        val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Define a location listener
        val locationListener = LocationListener { location -> focusOnLocation(location) }

        // If app have permissions, request a single location update
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } == PackageManager.PERMISSION_GRANTED
        ) {

            locationManager?.requestSingleUpdate(
                LocationManager.GPS_PROVIDER,
                locationListener,
                null
            )
        }
    }

    //Animates the camera to slowly zoom in a location passed
    private fun focusOnLocation(location: Location) {

        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 16f),
            2000,
            null
        )
    }

    //Interfaces
    //This method is called once the map has already been loaded
    override fun onMapReady(googleMap: GoogleMap) {

        //Initializes map
        map = googleMap
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)

        map.setOnMapLongClickListener { latLng ->
            launchToast("Latitud: ${latLng.latitude}\nLongitud: ${latLng.longitude}")
        }

        //Activate location tracking
        enableLocation()
        zoomToCurrentLocation()
    }

    //False -> Go to current position
    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    //When clicking on your postion show a message
    override fun onMyLocationClick(location: Location) {
        launchToast("Latitud: ${location.latitude}\nLongitud: ${location.longitude}")
    }

    private fun launchToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val REQUEST_CODE_LOCATION = 0
        fun newInstance() = FragmentLocation()
    }

    //Permissions
    //Is permission activated¿?
    private fun areLocationPermissionsGranted() =
        context?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } == PackageManager.PERMISSION_GRANTED

    override fun onResume() {
        super.onResume()
        enableLocation()
    }

    //Checks permissions in case user changes it while the application is running
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableLocation()
                } else {
                    map.isMyLocationEnabled = false
                    launchToast("Los permisos de localización fueron desactivados. Ve a ajustes y vuelva a activarlos.")
                }
            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun enableLocation() {
        // If map has not been initialized, return
        if (!::map.isInitialized) return

        // If permissions are granted
        if (areLocationPermissionsGranted()) {
            // Enable my location button and location click events
            map.isMyLocationEnabled = true
            map.setOnMyLocationButtonClickListener(this)
            map.setOnMyLocationClickListener(this)

            // Request location updates
            val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                this
            )
        } else {
            // Request location permissions
            requestLocationPermission()
        }
    }
    private fun requestLocationPermission() {
        //If permission where already asked¿?
        if (activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } == true
        ) {
            launchToast("Faltan los permisos de localización en ajustes.")
        } else {
            //Ask for them first time
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_LOCATION
                )
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        binding.linearLayoutCoordenates.visibility = VISIBLE
        binding.labelLatitude.text = "Latitud: ${location.latitude}"
        binding.labelLongitude.text = "Longitud: ${location.longitude}"
    }
}