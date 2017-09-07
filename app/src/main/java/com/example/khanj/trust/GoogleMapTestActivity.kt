package com.example.khanj.trust

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.support.v4.app.FragmentActivity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_google_map_test.*
import java.util.*

class GoogleMapTestActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private var mGoogleApiClient: GoogleApiClient? = null

    internal var mRootRef = FirebaseDatabase.getInstance().reference
    internal var mConditionRef = mRootRef.child("test")
    internal var mchild1Ref: DatabaseReference?=null
    internal var mchild2Ref: DatabaseReference?=null

    internal var timer: Timer? = null


    private var mMap: GoogleMap? = null
    var longitude:Double = 0.0
    var latitude:Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_map_test)


        // Location 제공자에서 정보를 얻어오기(GPS)
        // 1. Location을 사용하기 위한 권한을 얻어와야한다 AndroidManifest.xml
        //     ACCESS_FINE_LOCATION : NETWORK_PROVIDER, GPS_PROVIDER
        //     ACCESS_COARSE_LOCATION : NETWORK_PROVIDER
        // 2. LocationManager 를 통해서 원하는 제공자의 리스너 등록
        // 3. GPS 는 에뮬레이터에서는 기본적으로 동작하지 않는다
        // 4. 실내에서는 GPS_PROVIDER 를 요청해도 응답이 없다.  특별한 처리를 안하면 아무리 시간이 지나도
        //    응답이 없다.
        //    해결방법은
        //     ① 타이머를 설정하여 GPS_PROVIDER 에서 일정시간 응답이 없는 경우 NETWORK_PROVIDER로 전환
        //     ② 혹은, 둘다 한꺼번헤 호출하여 들어오는 값을 사용하는 방식.


        textView2.setText("위치정보 미수신중")


        // LocationManager 객체를 얻어온다
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager



        toggle1.setOnClickListener{
            try {
                if (toggle1.isChecked()) {
                    textView2.setText("수신중..")
                    // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록하기~!!!
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1.toFloat(), mLocationListener)
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1.toFloat(), mLocationListener)
                    val mapFragment = supportFragmentManager
                            .findFragmentById(R.id.map) as SupportMapFragment
                    mapFragment.getMapAsync(this)
                } else {
                    textView2.setText("위치정보 미수신중")
                    lm.removeUpdates(mLocationListener)  //  미수신할때는 반드시 자원해체를 해주어야 한다.
                }
            } catch (ex: SecurityException) {
                ;
            }
        }

        toggle1.performClick()

    } // end of onCreate


    private val mLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.

            Log.d("test", "onLocationChanged, location:" + location)
            longitude = location.getLongitude() //경도
            latitude = location.getLatitude()   //위도
            val altitude = location.getAltitude()   //고도
            val accuracy = location.getAccuracy()    //정확도
            val provider = location.getProvider()   //위치제공자
            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
            //Network 위치제공자에 의한 위치변화
            //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
            textView2.setText("위치정보 : " + provider + "\n위도 : " + longitude + "\n경도 : " + latitude+ "\n고도 : " + altitude + "\n정확도 : " + accuracy)
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
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val currentlocation = LatLng(latitude, longitude)
        mMap!!.addMarker(MarkerOptions().position(currentlocation).title("현재 위치"))
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(currentlocation))


        mchild1Ref = mConditionRef.child("위도")
        mchild2Ref = mConditionRef.child("경도")

        mchild1Ref?.setValue(latitude)
        mchild2Ref?.setValue(longitude)


    }

    internal var timertask: TimerTask = object : TimerTask() {
        override fun run() {
            toggle1.performClick()
        }
    }
    // 1초 후에 최초 실행하고, 이후 1초 간격으로 계속 반복해서 실행
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