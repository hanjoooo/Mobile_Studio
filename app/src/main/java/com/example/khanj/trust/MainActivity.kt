package com.example.khanj.trust

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_google_map_test.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null

    internal var mRootRef = FirebaseDatabase.getInstance().reference
    internal var mConditionRef = mRootRef.child("test")
    internal var mchild1Ref: DatabaseReference?=null
    internal var mchild2Ref: DatabaseReference?=null

    var longitude:Double = 0.0
    var latitude:Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Handler().postDelayed({
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록하기~!!!
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1.toFloat(), mLocationListener)
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1.toFloat(), mLocationListener)

        } catch (ex: SecurityException) {
            ;
        }
        }, 60000)
        bt_route.setOnClickListener{
            val intent = Intent(this,GoogleMapTestActivity::class.java)
            startActivity(intent)
        }
        bt_location.setOnClickListener{
            val intent = Intent(this,PresentLocation::class.java)
            startActivity(intent)
        }

        bt_chatting.setOnClickListener{
            val intent = Intent(this,ChattingActivity::class.java)
            startActivity(intent)
        }
    }

    private val mLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.
            longitude = location.getLongitude() //경도
            latitude = location.getLatitude()   //위도
            mchild1Ref = mConditionRef.child("경도")
            mchild2Ref = mConditionRef.child("위도")
            mchild1Ref?.setValue(latitude)
            mchild2Ref?.setValue(longitude)



        }


        override fun onProviderDisabled(provider: String) {
            // Disabled시
            Log.d("test", "onProviderDisabled, provider:" + provider)
        }

        override fun onProviderEnabled(provider: String) {
            // Enabled시
            Log.d("test", "onProviderEnabled, provider:" + provider)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            // 변경시
            Log.d("test", "onStatusChanged, provider:$provider, status:$status ,Bundle:$extras")
        }
    }

    override fun onStart() {
        super.onStart()

        mConditionRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    override fun onStop() {
        super.onStop()
        if (mAuthListener != null) {
            mAuth!!.removeAuthStateListener(mAuthListener!!)
        }
    }
}
