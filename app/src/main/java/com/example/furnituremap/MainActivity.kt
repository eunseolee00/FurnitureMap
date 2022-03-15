package com.example.furnituremap

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.google.android.gms.maps.model.LatLng
import java.util.ArrayList
import android.widget.ListView
import android.content.Intent

import android.view.View

import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.content.SharedPreferences
import android.widget.Toast
import java.lang.Exception


var places = ArrayList<String>()
var locations = ArrayList<LatLng>()
var arrayAdapter: ArrayAdapter<*>? = null
var latitudes = ArrayList<String>()
var longitudes = ArrayList<String>()

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val listView = findViewById<ListView>(R.id.listView)
        val sharedPreferences =
            getSharedPreferences("com.example.memorableplaces", Context.MODE_PRIVATE)

        places.clear()
        latitudes.clear()
        longitudes.clear()
        locations.clear()


        places = ObjectSerializer.deserialize(
            sharedPreferences
                .getString("places", ObjectSerializer.serialize(ArrayList<String>()))
        ) as ArrayList<String>
        latitudes = ObjectSerializer.deserialize(
            sharedPreferences
                .getString("lats", ObjectSerializer.serialize(ArrayList<String>()))
        ) as ArrayList<String>
        longitudes = ObjectSerializer.deserialize(
            sharedPreferences
                .getString("lons", ObjectSerializer.serialize(ArrayList<String>()))
        ) as ArrayList<String>

        if (places.size > 0 && latitudes.size > 0 && longitudes.size > 0) {
            for (i in latitudes.indices) {
                locations.add(LatLng(latitudes[i].toDouble(), longitudes[i].toDouble()))
            }
        } else {
            locations.add(LatLng(0.0, 0.0))
            places.add("Add a new place...")
            latitudes.add("0")
            longitudes.add("0")
        }//endif

        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, places)
        listView.adapter = arrayAdapter

        listView.onItemClickListener =
            OnItemClickListener { adapterView, view, position, l ->
                val intent = Intent(applicationContext, MapsActivity::class.java)
                intent.putExtra("placeNumber", position)
                startActivity(intent)
            }
        listView.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { adapterView, view, i, l ->

                val itemDelete = i

                AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Are you sure!")
                    .setMessage("Do you want to delete this note")
                    .setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->

                        places.removeAt(itemDelete)
                        latitudes.removeAt(itemDelete)
                        longitudes.removeAt(itemDelete)
                        arrayAdapter!!.notifyDataSetChanged()
                        sharedPreferences.edit()
                            .putString("places", ObjectSerializer.serialize(places))
                            .apply()
                        sharedPreferences.edit()
                            .putString("lats", ObjectSerializer.serialize(latitudes))
                            .apply()
                        sharedPreferences.edit()
                            .putString("lons", ObjectSerializer.serialize(longitudes))
                            .apply()
                    }//setPositiveButton
                    .setNegativeButton("No", null)
                    .show()
                return@OnItemLongClickListener true
            }
    }//onCreate
}//MainActivity