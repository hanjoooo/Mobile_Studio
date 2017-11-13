package com.example.khanj.trust

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.location.*
import android.support.v4.app.FragmentActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import com.example.khanj.trust.Data.LimitRange
import com.example.khanj.trust.Data.User
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_google_map_test.*
import kotlinx.android.synthetic.main.layout.*
import java.io.IOException
import java.util.*

class GoogleMapTestActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private var mGoogleApiClient: GoogleApiClient? = null

    internal var mRootRef = FirebaseDatabase.getInstance().reference
    internal var mConditionRef = mRootRef.child("users")
    internal var mchildRef: DatabaseReference?=null
    internal var mrangeLocRef:DatabaseReference?=null


    private var mMap: GoogleMap? = null
    private var userInfo:User?=null

    var longitude:Double = 0.0
    var latitude:Double = 0.0
    var m:Marker?=null
    var mapCircle:Circle?=null
    var edRange:EditText?=null

    var nowAddress="위치를 확인할 수 없습니다"
    var currentlocation =LatLng(37.6007195267265,126.86528900355972)
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

        // LocationManager 객체를 얻어온다
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
                // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록하기~!!!
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1.toFloat(), mLocationListener)
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1.toFloat(), mLocationListener)
                val mapFragment = supportFragmentManager
                        .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)

        } catch (ex: SecurityException) {
            ;
        }
        mAuthListener = FirebaseAuth.AuthStateListener {
            firebaseAuth ->
            val user = firebaseAuth.currentUser
            // [START_EXCLUDE]
            updateUI(user)
            // [END_EXCLUDE]
        }

        bt_range.setOnClickListener{
            val mContext=applicationContext
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout: View = inflater.inflate(R.layout.custom_dialog2, findViewById(R.id.layout_root));
            val aDialog: AlertDialog.Builder= AlertDialog.Builder(this)
            aDialog.setTitle("제한반경을 입력해주세요(숫자만 입력 예)1.5 =>1.5km)");
            aDialog.setView(layout);

            aDialog.setPositiveButton("확인") { dialog, which ->
                if(mapCircle!=null){
                    mapCircle!!.remove()
                }
                edRange =layout.findViewById(R.id.Edrange)
                val circleOptions= CircleOptions()
                        .center(currentlocation)
                        .radius(edRange!!.text.toString().toDouble()*1000.0)
                        .strokeColor(Color.GREEN)
                        .fillColor(Color.argb(128, 0, 255, 0))
                mapCircle= mMap!!.addCircle(circleOptions)
            }
            aDialog.setNegativeButton("취소") { dialog, which ->

            }

            val ad = aDialog.create()
            ad.show()
        }
        bt_confirm.setOnClickListener{
            val limitRange=LimitRange(edRange!!.text.toString().toDouble(),currentlocation.latitude,currentlocation.longitude)
            mrangeLocRef?.setValue(limitRange)
            Toast.makeText(this, "제한범위가 등록되었습니다", Toast.LENGTH_SHORT).show()
        }
    } // end of onCreate


    private val mLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.

            Log.d("test", "onLocationChanged, location:" + location)
            longitude = location.getLongitude() //경도
            latitude = location.getLatitude()   //위도

            if(currentlocation==LatLng(37.6007195267265,126.86528900355972)){
                currentlocation = LatLng(latitude, longitude)
                val altitude = location.getAltitude()   //고도
                val accuracy = location.getAccuracy()    //정확도
                val provider = location.getProvider()   //위치제공자
                //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
                //Network 위치제공자에 의한 위치변화
                //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
                textView2.setText("위치 : " + nowAddress + "\n위도 : " + latitude + "\n경도 : " + longitude+ "\n고도 : " + altitude + "\n정확도 : " + accuracy)
            }

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

        Handler().postDelayed({
            m=mMap!!.addMarker(MarkerOptions().position(currentlocation).title("현재 위치"))
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(currentlocation,15.toFloat()))
        }, 1500)
        mMap!!.setOnMapClickListener(object:GoogleMap.OnMapClickListener{
            override fun onMapClick(point: LatLng?) {
                if(mapCircle!=null){
                    mapCircle!!.remove()
                }
                m!!.remove()
                m = mMap!!.addMarker(MarkerOptions().position(point!!).title("선택한 위치"))
                val geocoder=Geocoder(this@GoogleMapTestActivity,Locale.KOREA)
                var address:List<Address>
                try{
                    if(geocoder!=null)
                    {
                        address = geocoder.getFromLocation(point.latitude,point.longitude,1)
                        if(address !=null && address.size > 0){
                            val currentLocationAddress=address.get(0).getAddressLine(0).toString()
                            nowAddress = currentLocationAddress
                            currentlocation=point
                        }
                    }
                }catch (e:IOException){
                    e.printStackTrace()
                }
                textView1.setText("현재 선택한 위치")
                textView2.setText("위치 : "+nowAddress +"\n위도 : " + point.latitude + "\n경도 : " + point.longitude )
            }
        })
    }

    internal var timertask: TimerTask = object : TimerTask() {
        override fun run() {
        }
    }
    // 1초 후에 최초 실행하고, 이후 1초 간격으로 계속 반복해서 실행


    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener!!)
        Handler().postDelayed({
            mchildRef!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userInfo=dataSnapshot.getValue(User::class.java)
                    mrangeLocRef=mRootRef.child("location").child(userInfo!!.getOtherUid()).child("LimitRange")
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }, 1000)

    }

    override fun onStop() {
        super.onStop()
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener!!)
        }
    }
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            mchildRef = mConditionRef.child(user.uid)
        } else {

        }
    }

}