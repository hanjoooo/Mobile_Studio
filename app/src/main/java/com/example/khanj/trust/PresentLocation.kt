package com.example.khanj.trust

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import com.example.khanj.trust.Data.LimitRange
import com.example.khanj.trust.Data.User
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_present_location.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PresentLocation : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private var mGoogleApiClient: GoogleApiClient? = null

    internal var mRootRef = FirebaseDatabase.getInstance().reference
    internal var mConditionRef = mRootRef.child("location")
    internal var muserRef=mRootRef.child("users")
    internal var muserChildRef:DatabaseReference?=null
    internal var mchildRef: DatabaseReference?=null

    internal var mchild1Ref: DatabaseReference?=null
    internal var mchild2Ref: DatabaseReference?=null

    internal var mState:DatabaseReference?=null
    internal var mBattery:DatabaseReference?=null
    internal var mNetwork:DatabaseReference?=null
    internal var mGps:DatabaseReference?=null
    internal var mTime:DatabaseReference?=null


    var longitude:Double = 0.0
    var latitude:Double = 0.0
    var mapCircle: Circle?=null
    var Battery="100"
    var Network=" "
    var Gps=" "
    var lasttime=" "
    private var userInfo: User?=null
    private var State=" "

    var point=LatLng(37.6007195267265,126.86528900355972)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_present_location)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE)as NotificationManager
        notificationManager.cancel(778)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mAuthListener = FirebaseAuth.AuthStateListener {
            firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in
                Log.d(PresentLocation.TAG, "onAuthStateChanged:signed_in:" + user.uid)
            } else {
                // User is signed out
                Log.d(PresentLocation.TAG, "onAuthStateChanged:signed_out")
            }
            // [START_EXCLUDE]
            updateUI(user)
            // [END_EXCLUDE]
        }
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
            muserChildRef!!.addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userInfo = dataSnapshot.getValue(User::class.java)
                    mchildRef=mConditionRef.child(userInfo!!.getOtherUid())
                    mchild1Ref=mchildRef!!.child("현재위치").child("위도")
                    mchild2Ref=mchildRef!!.child("현재위치").child("경도")
                    mState=muserRef.child(userInfo!!.getOtherUid()).child("state")
                    mBattery = mchildRef!!.child("현재위치").child("베터리상태")
                    mNetwork = mchildRef!!.child("현재위치").child("네트워크")
                    mTime = mchildRef!!.child("현재위치").child("수신시각")
                    mGps = mchildRef!!.child("현재위치").child("GPS")
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
            Handler().postDelayed({
                if(userInfo?.getOtherUid()!= " ") {
                    mState?.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if(dataSnapshot.exists()){
                                val data = dataSnapshot.getValue().toString()
                                if (data == " ")
                                    State = "현재 여유로운 상태입니다"
                                else
                                    State = data
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                        }

                    })
                    mBattery?.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if(dataSnapshot.exists()){
                                val data = dataSnapshot.getValue().toString()
                                Battery=data
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                        }

                    })
                    mNetwork?.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if(dataSnapshot.exists()){
                                val data = dataSnapshot.getValue().toString()
                                Network=data
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                        }

                    })
                    mGps?.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if(dataSnapshot.exists()){
                                val data = dataSnapshot.getValue().toString()
                                Gps=data
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                        }

                    })
                    mTime?.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if(dataSnapshot.exists()){
                                val data = dataSnapshot.getValue().toString()
                                lasttime=data
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                        }

                    })
                }
                mchildRef?.child("LimitRange")?.addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if(dataSnapshot.exists()){
                            val data = dataSnapshot.getValue().toString()
                            if(data== " ") ;
                            else{
                                val limitrange=dataSnapshot.getValue(LimitRange::class.java)
                                if(limitrange!=null){
                                    var laa= Location("a")
                                    laa.setLatitude(limitrange!!.latitude)
                                    laa.setLongitude(limitrange!!.longitude)
                                    var lab = Location("b")
                                    var dist = laa.distanceTo(lab)
                                    var Range=limitrange.radius
                                    if(mapCircle!=null){
                                        mapCircle!!.remove()
                                    }
                                    val circleOptions= CircleOptions()
                                            .center(LatLng(limitrange!!.latitude,limitrange!!.longitude))
                                            .radius(Range*1000.0)
                                            .strokeColor(Color.GREEN)
                                            .fillColor(Color.argb(78, 0, 255, 0))
                                    mapCircle= mMap!!.addCircle(circleOptions)
                                    if(Math.pow(dist.toDouble()/1000.0,2.0)<limitrange!!.radius){
                                    }
                                    else {
                                    }
                                }
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
                mchild1Ref?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if(dataSnapshot.exists()){
                            val datas = dataSnapshot.getValue().toString()
                            latitude= datas.toDouble()
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
                mchild2Ref?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if(dataSnapshot.exists()){
                            val datas = dataSnapshot.getValue().toString()
                            longitude= datas.toDouble()
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
            }, 1000)
        }, 1000)
    }
    //위도 1당 110km 0.001
    //경도 1당 88km
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val lat=LatLng(37.6007195267265,126.86528900355972)
        val x = 37.6007195267265
        val y = 126.86528900355972
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lat,14.toFloat()))
        var nowAddress=""

        Handler().postDelayed({
            val geocoder= Geocoder(this@PresentLocation,Locale.KOREA)
            var address:List<Address>
            try{
                if(geocoder!=null)
                {
                    address = geocoder.getFromLocation(latitude,longitude,1)
                    if(address !=null && address.size > 0){
                        val currentLocationAddress=address.get(0).getAddressLine(0).toString()
                        nowAddress = currentLocationAddress
                    }
                }
            }catch (e: IOException){
                e.printStackTrace()
            }


            textLocation.setText(nowAddress)
            textState.setText(State)
            textBattery.setText(Battery+"%")
            textNetwork.setText(Network)
            textGps.setText(Gps)
            textTime.setText(lasttime)
            val sydney = LatLng(latitude, longitude)
            mMap.addMarker(MarkerOptions().position(sydney).title("현재 위치").icon(BitmapDescriptorFactory.fromResource(R.drawable.now)).snippet(nowAddress))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney,16F.toFloat()))
        }, 2500)
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
            muserChildRef = muserRef.child(user.uid)

        } else {

        }
    }

    companion object {
        private val TAG = "EmailPassword"
    }

}
