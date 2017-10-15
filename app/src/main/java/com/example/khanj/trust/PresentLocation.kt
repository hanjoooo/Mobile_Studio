package com.example.khanj.trust

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.example.khanj.trust.Data.User
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
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


    var longitude:Double = 0.0
    var latitude:Double = 0.0

    private var userInfo: User?=null

    var point=LatLng(37.6007195267265,126.86528900355972)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_present_location)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.


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
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
            Handler().postDelayed({
                mchild1Ref!!.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val datas = dataSnapshot.getValue().toString()
                        latitude= datas.toDouble()
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
                mchild2Ref!!.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val datas = dataSnapshot.getValue().toString()
                        longitude= datas.toDouble()
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
        Handler().postDelayed({
            val sydney = LatLng(latitude, longitude)
            val circleOptions=CircleOptions()
                    .center(point)
                    .radius(1000.0)
                    .strokeColor(Color.RED)
            mMap.addCircle(circleOptions)
            mMap.addMarker(MarkerOptions().position(sydney).title("현재 위치"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,15.toFloat()))
            var laa=Location("a")
            laa.setLatitude(x)
            laa.setLongitude(y)
            var lab = Location("b")
            lab.setLatitude(latitude)
            lab.setLongitude(longitude)
            var dist = laa.distanceTo(lab)
            if(Math.pow(dist.toDouble()/1000.0,2.0)<1.0){
                val msg = "상대방이 설정한 위치범위 안에 있습니다"
                Log.d(PresentLocation.TAG, msg)
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
            else {
                val msg = "상대방이 설정한 위치범위를 벗어났습니다"
                Log.d(PresentLocation.TAG, msg)
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
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
