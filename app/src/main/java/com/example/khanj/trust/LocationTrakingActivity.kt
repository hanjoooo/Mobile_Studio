package com.example.khanj.trust

import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.example.khanj.trust.Data.User
import com.example.khanj.trust.Data.location

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chatting.*
import kotlinx.android.synthetic.main.activity_location_traking.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class LocationTrakingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private var mAuth: FirebaseAuth= FirebaseAuth.getInstance()
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    internal var mRootRef = FirebaseDatabase.getInstance().reference
    internal var mConditionRef = mRootRef.child("location")
    internal var muserRef=mRootRef.child("users")
    internal var muserChildRef:DatabaseReference?=null
    internal var mlocRef: DatabaseReference?=null
    internal var mlocChildRef: DatabaseReference?=null


    var longitude:Double = 0.0
    var latitude:Double = 0.0
    var adpater:LocationTrakingAdapter?=null

    private var userinfo: User?=null
    var loc_tra = ArrayList<String>()
    var loc_time=ArrayList<String>()
    var loc_Address=ArrayList<String>()
    var loc_latlng=ArrayList<LatLng>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_traking)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mAuthListener = FirebaseAuth.AuthStateListener {
            firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in
                Log.d(LocationTrakingActivity.TAG, "onAuthStateChanged:signed_in:" + user.uid)
            } else {
                // User is signed out
                Log.d(LocationTrakingActivity.TAG, "onAuthStateChanged:signed_out")
            }
            // [START_EXCLUDE]
            updateUI(user)
            // [END_EXCLUDE]
        }
        Handler().postDelayed({
            adpater = com.example.khanj.trust.LocationTrakingAdapter(loc_tra,loc_time,this@LocationTrakingActivity)
            loclist.adapter=adpater
            loclist.setOnItemClickListener{parent,view,position,id->
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc_latlng[position],16.toFloat()))
            }
        }, 1000)
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
        mAuth.addAuthStateListener(mAuthListener!!)
        Handler().postDelayed({
            muserChildRef!!.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userinfo=dataSnapshot.getValue(User::class.java)
                    mlocRef=mConditionRef.child(userinfo!!.getOtherUid())
                    mlocChildRef=mlocRef!!.child("time")
                }

                override fun onCancelled(p0: DatabaseError?) {

                }
            })
            Handler().postDelayed({
                mlocChildRef?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var x = ArrayList<LatLng>()
                        var dotime=ArrayList<String>()
                        loc_Address.clear()
                        for ( snapshot in dataSnapshot.getChildren()) {
                            val loc = snapshot.getValue(location::class.java)
                            val latitude = loc.latitude
                            val longitude = loc.longitude
                            val y =LatLng(latitude,longitude)
                            x.add(y)
                            dotime.add(loc!!.times)
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(y,14.toFloat()))

                            val geocoder= Geocoder(this@LocationTrakingActivity, Locale.KOREA)
                            var address:List<Address>
                            try{
                                if(geocoder!=null)
                                {
                                    address = geocoder.getFromLocation(latitude,longitude,1)
                                    if(address !=null && address.size > 0){
                                        val currentLocationAddress=address.get(0).getAddressLine(0).toString()
                                        loc_Address.add(currentLocationAddress)
                                    }
                                }
                            }catch (e: IOException){
                                e.printStackTrace()
                            }
                        }
                        Handler().postDelayed({
                            if (x.size<20 && x.size>0) {
                                loc_time.clear()
                                loc_tra.clear()
                                loc_latlng.clear()
                                mMap.addMarker(MarkerOptions().position(x[0]).title(dotime[0]).icon(BitmapDescriptorFactory.fromResource(R.drawable.one)))
                                loc_tra.add(loc_Address[0])
                                loc_time.add(dotime[0])
                                loc_latlng.add(x[0])
                                for(i in 1..x.size-1){
                                    if(i==1){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.two)))
                                    }
                                    else if(i==2){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.three)))
                                    }
                                    else if(i==3){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.four)))
                                    }
                                    else if(i==4){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.five)))
                                    }
                                    else if(i==5){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.six)))
                                    }
                                    else if(i==6){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.seven)))
                                    }
                                    else if(i==7){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.eight)))
                                    }
                                    else if(i==8){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.nine)))
                                    }
                                    else if(i==9){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.ten)))
                                    }
                                    else if(i==10){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.eleven)))
                                    }
                                    else if(i==11){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.twelve)))
                                    }
                                    else if(i==12){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.thirteen)))
                                    }
                                    else if(i==13){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.fourteen)))
                                    }
                                    else if(i==14){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.fifthteen)))
                                    }
                                    else if(i==15){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.sixteen)))
                                    }
                                    else if(i==16){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.seventeen)))
                                    }
                                    else if(i==17){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.eighteen)))
                                    }
                                    else if(i==18){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.nineteen)))
                                    }
                                    else if(i==19){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.twenty)))
                                    }
                                    loc_tra.add(loc_Address[i])
                                    loc_time.add(dotime[i])
                                    loc_latlng.add(x[i])
                                }
                                for (i in 0..x.size - 2) {
                                    mMap.addPolyline(PolylineOptions().add(x[i], x[i + 1]).width(15.toFloat()).color(R.color.gold))
                                }

                            }
                            else if(x.size>=21) {
                                loc_time.clear()
                                loc_tra.clear()
                                loc_latlng.clear()
                                mMap.addMarker(MarkerOptions().position(x[x.size-20]).title(dotime[x.size-20]).icon(BitmapDescriptorFactory.fromResource(R.drawable.one)))
                                loc_tra.add(loc_Address[x.size-20])
                                loc_time.add(dotime[x.size-20])
                                loc_latlng.add(x[x.size-20])
                                for(i in x.size-19..x.size-1){
                                    if(i==x.size-19){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.two)))
                                    }
                                    else if(i==x.size-18){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.three)))
                                    }
                                    else if(i==x.size-17){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.four)))
                                    }
                                    else if(i==x.size-16){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.five)))
                                    }
                                    else if(i==x.size-15){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.six)))
                                    }
                                    else if(i==x.size-14){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.seven)))
                                    }
                                    else if(i==x.size-13){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.eight)))
                                    }
                                    else if(i==x.size-12){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.nine)))
                                    }
                                    else if(i==x.size-11){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.ten)))
                                    }
                                    else if(i==x.size-10){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.eleven)))
                                    }
                                    else if(i==x.size-9){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.twelve)))
                                    }
                                    else if(i==x.size-8){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.thirteen)))
                                    }
                                    else if(i==x.size-7){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.fourteen)))
                                    }
                                    else if(i==x.size-6){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.fifthteen)))
                                    }
                                    else if(i==x.size-5){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.sixteen)))
                                    }
                                    else if(i==x.size-4){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.seventeen)))
                                    }
                                    else if(i==x.size-3){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.eighteen)))
                                    }
                                    else if(i==x.size-2){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.nineteen)))
                                    }
                                    else if(i==x.size-1){
                                        mMap.addMarker(MarkerOptions().position(x[i]).title(dotime[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.twenty)))
                                    }
                                    loc_tra.add(loc_Address[i])
                                    loc_time.add(dotime[i])
                                    loc_latlng.add(x[i])
                                }
                                for (i in x.size - 11..x.size - 3){
                                    mMap.addPolyline(PolylineOptions().add(x[i], x[i + 1]).width(15.toFloat()).color(R.color.gold))
                                }
                            }
                            adpater!!.notifyDataSetChanged()
                        }, 1500)
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
            }, 1000)
        }, 1000)

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
            mAuth.removeAuthStateListener(mAuthListener!!)
        }
    }
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            muserChildRef=muserRef.child(user.uid)

        } else {

        }
    }

    companion object {
        private val TAG = "EmailPassword"
    }

}
