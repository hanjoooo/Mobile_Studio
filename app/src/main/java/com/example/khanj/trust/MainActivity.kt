package com.example.khanj.trust

import android.annotation.SuppressLint
import android.app.*
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
import android.media.RingtoneManager
import android.media.session.MediaSession
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.support.annotation.IntegerRes
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import com.example.khanj.trust.Data.Messege
import com.example.khanj.trust.Data.User
import com.example.khanj.trust.Data.location
import com.example.khanj.trust.handler.BackPressCloseHandler

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserInfo
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
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
    internal var muserRef=mRootRef.child("users")
    internal var mnotifiyRef=mRootRef.child("partner")
    internal var mnotifiyChildRef:DatabaseReference?=null
    internal var mchildRef: DatabaseReference?=null
    internal var mchildsRef: DatabaseReference?=null
    internal var mtimeRef: DatabaseReference?=null
    internal var mchild1Ref: DatabaseReference?=null
    internal var mchild2Ref: DatabaseReference?=null
    internal var muserChildRef: DatabaseReference?=null

    internal var mMesLastime: DatabaseReference?=null
    internal var mMesCurtime: DatabaseReference?=null
    internal var mMesUid: DatabaseReference?=null
    internal var mMesNick: DatabaseReference?=null
    internal var mState:DatabaseReference?=null
    private var backPressCloseHandler: BackPressCloseHandler? = null

    var longitude:Double = 0.0
    var latitude:Double = 0.0
    var ConnectUser:String=""
    var userInfo:User?=null

    var mesLastime=" "
    var mesCurtime=" "
    private var State=" "
    private var mesUid=" "
    private var mesNick=" "
    private var myUid =  " "

    var dialogOn=false
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
        }, 60000)

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
        bt_setting.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View) {
                /*
            signOut()
            finish()
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)*/
                val p = PopupMenu(
                        applicationContext, v)
                menuInflater.inflate(R.menu.menu2_main, p.menu)

                p.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {

                    override fun onMenuItemClick(item: MenuItem): Boolean {
                        val intent = Intent(this@MainActivity,GoogleMapTestActivity::class.java)
                        when (item.getItemId()) {
                            R.id.subMenu -> "Sg"
                            R.id.subMenu2 -> setState()
                            R.id.logout -> LogOut()
                            R.id.subRegist->
                                Toast.makeText(applicationContext, "등록", Toast.LENGTH_SHORT).show()
                            R.id.subChange -> startActivity(intent)
                            R.id.subDelete ->
                                Toast.makeText(applicationContext, "삭제", Toast.LENGTH_SHORT).show()

                        }
                        return false
                    }
                })
                p.show()
            }
        })
        bt_alarm.setOnClickListener{
            /*
            val uri:Uri=Uri.parse("smsto:01053445913")
            val i=Intent(Intent.ACTION_SENDTO,uri)
            i.putExtra("sms_body","연결하실거임?")
            startActivity(i)
            */
        }
        bt_facechatting.setOnClickListener{
            val intent = Intent(this,RTCFaceActivity::class.java)
            startActivity(intent)
        }
        bt_plus.setOnClickListener{
            val mContext=applicationContext
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout:View = inflater.inflate(R.layout.custom_dialog, findViewById(R.id.layout_root));
            val aDialog:AlertDialog.Builder=AlertDialog.Builder(this)
             aDialog.setTitle("연결하실 상대방 닉네임을 입력하세요.");
             aDialog.setView(layout);

            aDialog.setPositiveButton("연결") { dialog, which ->
                var x:EditText=layout.findViewById(R.id.EdConnectUser)
                val now:Long = System.currentTimeMillis()
                val date:Date=Date(now)
                val sdfNow:SimpleDateFormat= SimpleDateFormat("dd일HH시mm분ss초", Locale.KOREA)
                val strNow:String = sdfNow.format(date)
                val messege=Messege(userInfo!!.getNickname(),userInfo!!.getName(),userInfo!!.getMyUid(),strNow," ")
                ConnectUser=x.text.toString()
                mnotifiyChildRef=mnotifiyRef.child(ConnectUser)
                mnotifiyChildRef!!.setValue(messege)
            }
            aDialog.setNegativeButton("취소") { dialog, which ->

            }

            val ad = aDialog.create()
            ad.show()
        }

        bt_status.setOnClickListener{

            var alertDialogBuilder=AlertDialog.Builder(this)
            alertDialogBuilder.setMessage(State)
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
        backPressCloseHandler = BackPressCloseHandler(this)

        val intent=Intent(this,MyService::class.java)
        startService(intent)
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
            mchild1Ref = mchildRef!!.child("현재위치").child("위도")
            mchild2Ref = mchildRef!!.child("현재위치").child("경도")
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

        Handler().postDelayed({
            muserChildRef!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userInfo=dataSnapshot.getValue(User::class.java)
                    mnotifiyChildRef=mnotifiyRef.child(userInfo!!.getNickname())
                    mMesLastime=mnotifiyChildRef!!.child("lastime")
                    mMesCurtime=mnotifiyChildRef!!.child("times")
                    mMesUid=mnotifiyChildRef!!.child("uid")
                    mMesNick=mnotifiyChildRef!!.child("nickname")
                    mState=muserRef.child(userInfo!!.getOtherUid()).child("state")

                }
                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
            Handler().postDelayed({
                mMesLastime!!.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        mesLastime=dataSnapshot.getValue().toString()
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
                mMesUid!!.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        mesUid=dataSnapshot.getValue().toString()
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
                mMesNick!!.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        mesNick=dataSnapshot.getValue().toString()
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
                mState!!.addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val data=dataSnapshot.getValue().toString()
                        if(data==" ")
                            State="현재 여유로운 상태입니다"
                        else
                            State=data
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }

                })
                Handler().postDelayed({
                    mMesCurtime!!.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            mesCurtime=dataSnapshot.getValue().toString()
                            if(mesCurtime==mesLastime || mesUid == " " )
                                ;
                            else{
                                if(dialogOn){

                                }
                                else{
                                    dialogOn=true
                                    val aDialog:AlertDialog.Builder=AlertDialog.Builder(this@MainActivity)
                                    aDialog.setTitle(mesNick+"님과 연결하시겠습니까?");

                                    aDialog.setPositiveButton("연결") { dialog, which ->
                                        mMesLastime!!.setValue(mesCurtime)
                                        muserChildRef!!.child("otherUid").setValue(mesUid)
                                        muserRef.child(mesUid).child("otherUid").setValue(myUid)
                                        dialogOn=false
                                    }
                                    aDialog.setNegativeButton("취소") { dialog, which ->
                                        mMesLastime!!.setValue(mesCurtime)
                                        dialogOn=false
                                    }
                                    val ad = aDialog.create()
                                    ad.show()
                                }
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                        }
                    })
                }, 1000)
            }, 2000)
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
            muserChildRef=muserRef.child(user.uid)
            mchildsRef=mchildRef!!.child("time")
            myUid=user.uid
        } else {

        }
    }
    public fun setState(){
        val mContext=applicationContext
        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout:View = inflater.inflate(R.layout.custom_dialog, findViewById(R.id.layout_root));
        val aDialog:AlertDialog.Builder=AlertDialog.Builder(this)
        aDialog.setTitle("지금 현재 상태를 입력해주세요.");
        aDialog.setView(layout);

        aDialog.setPositiveButton("확인") { dialog, which ->
            var x:EditText=layout.findViewById(R.id.EdConnectUser)
            muserChildRef!!.child("state").setValue(x.text.toString())
        }
        aDialog.setNegativeButton("취소") { dialog, which ->

        }

        val ad = aDialog.create()
        ad.show()
    }

    private fun LogOut(){
        signOut()
        finish()
        val intent = Intent(this,LoginActivity::class.java)
        startActivity(intent)
    }
    private fun signOut() {
        mAuth.signOut()
    }

    override fun onBackPressed() {
        backPressCloseHandler!!.onBackPressed()
    }
    companion object {
        private val TAG = "EmailPassword"
    }

}
