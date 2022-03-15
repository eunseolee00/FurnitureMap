package com.example.furnituremap

import android.Manifest
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.furnituremap.databinding.ActivityMapsBinding
import android.location.LocationListener

import android.location.LocationManager
import android.location.Location
import android.content.pm.PackageManager

import androidx.core.content.ContextCompat

import androidx.annotation.NonNull
import android.content.Intent
import android.icu.text.SimpleDateFormat
import androidx.core.app.ActivityCompat
import android.widget.Toast

import android.location.Address

import android.location.Geocoder
import java.util.*
import android.content.SharedPreferences
import java.util.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap //set up google
    private lateinit var binding: ActivityMapsBinding//set up by google
    var sentLoc = -1

    var locationManager: LocationManager? = null
    var locationListener: LocationListener? = null


    fun centerMapOnLocation(location: Location?, title: String?) {

        if (location != null) {
            val userLocation = LatLng(location.latitude, location.longitude)
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(userLocation).title(title))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12f))
        }
    }//centerMapOnLocation
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
                                            grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            {
                locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f,
                    locationListener!!)

                val lastKnownLocation = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                centerMapOnLocation(lastKnownLocation, "Your Location")
            }//if
        }//outter if
    }//onRequestPermissionsResult


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        sentLoc = intent.getIntExtra("placeNumber",-1)

    }//onCreate


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)

        if (sentLoc == 0) {
            // Zoom in on user location
            locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager?;
            locationListener = object : LocationListener {
                @Override
                override fun onLocationChanged(location: Location) {
                    centerMapOnLocation(location, "Your Location")
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(s: String) {}
                override fun onProviderDisabled(s: String) {}

            }// locationListener = object

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0F, locationListener!!)
                val lastKnownLocation = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                centerMapOnLocation(lastKnownLocation, "Your Location")

            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION ), 1 )
            }
        } else {
            val placeLocation =  Location(LocationManager.GPS_PROVIDER);

            placeLocation.latitude = locations[sentLoc].latitude
            placeLocation.longitude = locations[sentLoc].longitude
            centerMapOnLocation(placeLocation, places[sentLoc]);

        }//else

    }//onMapReady

    override fun onMapLongClick(latLng: LatLng) {
        val geocoder = Geocoder(applicationContext, Locale.getDefault())
        mMap.clear()
        var address = ""


        val listAdddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        if (listAdddresses != null && listAdddresses.size > 0) {
            if (listAdddresses[0].thoroughfare != null) {
                if (listAdddresses[0].subThoroughfare != null) {
                    address += listAdddresses[0].subThoroughfare + " "
                }
                address += listAdddresses[0].thoroughfare
            }
        }//if (listAdddresses


        if (address == "") {
            val sdf = SimpleDateFormat("HH:mm yyyy-MM-dd")
            address += sdf.format(Date())
        }
        mMap.addMarker(MarkerOptions().position(latLng).title(address))
        places.add(address)
        locations.add(latLng)



        val sharedPreferences =
            getSharedPreferences("com.example.memorableplaces", MODE_PRIVATE)

        for (coord in locations) {
            latitudes.add(coord.latitude.toString())
            longitudes.add(coord.longitude.toString())
        }
        sharedPreferences.edit().putString("places", ObjectSerializer.serialize(places)).apply();
        sharedPreferences.edit().putString("lats", ObjectSerializer.serialize(latitudes)).apply();
        sharedPreferences.edit().putString("lons", ObjectSerializer.serialize(longitudes)).apply();
        arrayAdapter!!.notifyDataSetChanged();
        Toast.makeText(this, "$latLng saved", Toast.LENGTH_SHORT).show()

    }//onMapLongClick
}//class MapsActivity