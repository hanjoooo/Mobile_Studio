package com.example.khanj.trust

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Range
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class LocationTrakingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null

    internal var mRootRef = FirebaseDatabase.getInstance().reference
    internal var mConditionRef = mRootRef.child("test")
    internal var mConditionRef1 = FirebaseDatabase.getInstance().getReference("test").child("time")


    var longitude:Double = 0.0
    var latitude:Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_traking)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onStart() {
        super.onStart()

        mConditionRef1.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var x = ArrayList<LatLng>()
                var dotime=ArrayList<String>()
                for ( snapshot in dataSnapshot.getChildren()) {
                    var loc = snapshot.getValue(location::class.java)
                    var latitude = loc.latitude
                    var longitude = loc.longitude
                    val y =LatLng(latitude,longitude)
                    x.add(y)
                    dotime.add(loc.times)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(y,16.toFloat()))
                }
                Handler().postDelayed({

                    if (x.size<20 && x.size>0) {
                        mMap.addMarker(MarkerOptions().position(x[0]).title(dotime[0]))
                        mMap . addMarker (MarkerOptions().position(x[x.size - 1]).title(dotime[x.size - 1]))
                        for (i in 0..x.size - 2)
                            mMap.addPolyline(PolylineOptions().add(x[i], x[i + 1]).width(15.toFloat()).color(Color.RED))
                    }
                    else if(x.size>=20) {
                        mMap.addMarker(MarkerOptions().position(x[x.size-20]).title(dotime[x.size-20]))
                        mMap . addMarker (MarkerOptions().position(x[x.size - 1]).title(dotime[x.size - 1]))
                        for (i in x.size - 20..x.size - 2)
                            mMap.addPolyline(PolylineOptions().add(x[i], x[i + 1]).width(15.toFloat()).color(Color.RED))
                    }
                }, 1500)




            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val x=LatLng(37.6007195267265,126.86528900355972)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(x,14.toFloat()))
        // Add a marker in Sydney and move the camera
    }
    override fun onStop() {
        super.onStop()
        if (mAuthListener != null) {
            mAuth!!.removeAuthStateListener(mAuthListener!!)
        }
    }

}
