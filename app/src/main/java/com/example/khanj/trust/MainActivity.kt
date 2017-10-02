package com.example.khanj.trust

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.support.annotation.IntegerRes
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.khanj.trust.Data.location
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_google_map_test.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.custom_dialog.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null

    internal var mRootRef = FirebaseDatabase.getInstance().reference
    internal var mConditionRef = mRootRef.child("location")
    internal var mCoupleRef=mRootRef.child("couples")
    internal var mcallTime=mRootRef.child("state")
    internal var mchildRef: DatabaseReference?=null
    internal var mchildsRef: DatabaseReference?=null
    internal var mtimeRef: DatabaseReference?=null
    internal var mchild1Ref: DatabaseReference?=null
    internal var mchild2Ref: DatabaseReference?=null


    var longitude:Double = 0.0
    var latitude:Double = 0.0

    var state:String=" "
    var ConnectUser:String=""

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val notificationManager = getSystemService<NotificationManager>(NotificationManager::class.java!!)
            notificationManager!!.createNotificationChannel(NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW))
        }
        if (getIntent().getExtras() != null) {
            for (key in getIntent().getExtras()!!.keySet()) {
                val value = getIntent().getExtras()!!.get(key)
                Log.d(TAG, "Key: $key Value: $value")
            }
        }
        Handler().postDelayed({
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록하기~!!!
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1.toFloat(), mLocationListener)
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1.toFloat(), mLocationListener)

        } catch (ex: SecurityException) {
            ;
        }
        }, 10000)

        bt_route.setOnClickListener{
            val intent = Intent(this,LocationTrakingActivity::class.java)
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
        bt_setting.setOnClickListener{
            signOut()
            finish()
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }
        bt_alarm.setOnClickListener{
            // [START subscribe_topics]
            FirebaseMessaging.getInstance().subscribeToTopic("news")
            // [END subscribe_topics]

            // Log and toast
            val msg = getString(R.string.msg_subscribed)
            Log.d(TAG, msg)
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
        }
        bt_facechatting.setOnClickListener{
            val intent = Intent(this,RTCFaceActivity::class.java)
            startActivity(intent)
        }

        val token = FirebaseInstanceId.getInstance().getToken()

        // Log and toast
        val msg = getString(R.string.msg_token_fmt, token)
        Log.d(TAG, msg)
        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
        bt_plus.setOnClickListener{
            val mContext=applicationContext
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout:View = inflater.inflate(R.layout.custom_dialog, findViewById(R.id.layout_root));
            val aDialog:AlertDialog.Builder=AlertDialog.Builder(this)
             aDialog.setTitle("연결하실 상대방 전화번호를 입력하세요.");
             aDialog.setView(layout);

            aDialog.setPositiveButton("연결") { dialog, which ->
                var x:EditText=layout.findViewById(R.id.EdConnectUser)
                ConnectUser=x.text.toString()
            }
            aDialog.setNegativeButton("취소") { dialog, which -> }

            val ad = aDialog.create()
            ad.show()
            mCoupleRef.setValue(ConnectUser)

        }

        bt_status.setOnClickListener{
            var alertDialogBuilder=AlertDialog.Builder(this)
            alertDialogBuilder.setMessage(state)
            alertDialogBuilder.setPositiveButton("확인",null)
            var alert:AlertDialog=alertDialogBuilder.create()
            alert.setTitle("상태")
            alert.window.setBackgroundDrawable(ColorDrawable(Color.YELLOW))
            alert.window.setBackgroundDrawable(ColorDrawable(R.color.pure))
            alert.show()
        }

        mAuthListener = FirebaseAuth.AuthStateListener {
            firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in
                Log.d(MainActivity.TAG, "onAuthStateChanged:signed_in:" + user.uid)
            } else {
                // User is signed out
                Log.d(MainActivity.TAG, "onAuthStateChanged:signed_out")
            }
            // [START_EXCLUDE]
            updateUI(user)
            // [END_EXCLUDE]
        }


    }

    private val mTouchEvent = object : View.OnTouchListener {

        override fun onTouch(v: View, event: MotionEvent): Boolean {

            val image = v as ImageView

            when (v.getId()) {

                R.id.bt_location ->

                    if (event.getAction() === MotionEvent.ACTION_DOWN) {

                        image.setColorFilter(Color.RED.toInt(), PorterDuff.Mode.SRC_OVER)

                    } else if (event.getAction() === MotionEvent.ACTION_UP) {

                        image.setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_OVER)

                    }
            }
            return true
        }
    }

    private val mLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.
            longitude = location.getLongitude() //경도
            latitude = location.getLatitude()   //위도
            mchild1Ref = mchildRef!!.child("경도")
            mchild2Ref = mchildRef!!.child("위도")
            mchild1Ref?.setValue(latitude)
            mchild2Ref?.setValue(longitude)
            val now:Long = System.currentTimeMillis()
            val date:Date=Date(now)
            val sdfNow:SimpleDateFormat= SimpleDateFormat("dd일HH시mm분", Locale.KOREA)
            val sdfNow2:SimpleDateFormat= SimpleDateFormat("MMddHHmm", Locale.KOREA)
            val strNow:String = sdfNow.format(date)
            val strNow2:String=sdfNow2.format(date)
            val loc: location = location(strNow,latitude,longitude)
            mtimeRef=mchildsRef!!.child(strNow2)
            mtimeRef?.setValue(loc)
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
        mAuth.addAuthStateListener(mAuthListener!!)

        mcallTime.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                state=dataSnapshot.getValue().toString()
            }
            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
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
            mchildsRef=mchildRef!!.child("time")

        } else {

        }
    }
    private fun signOut() {
        mAuth.signOut()
    }

    companion object {
        private val TAG = "EmailPassword"
    }

}
