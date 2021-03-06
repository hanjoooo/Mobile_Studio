package com.example.khanj.trust

import android.app.Notification

import android.app.NotificationManager

import android.app.PendingIntent

import android.app.Service

import android.content.Context

import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

import android.os.Handler

import android.os.IBinder
import android.util.Log

import android.widget.Toast
import com.example.khanj.trust.Data.LimitRange
import com.example.khanj.trust.Data.Messege
import com.example.khanj.trust.Data.User
import com.example.khanj.trust.Data.location
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.playrtc.simplechat.TestRtc
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timerTask
import android.content.BroadcastReceiver
import android.os.BatteryManager
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo


class MyService : Service() {

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null

    internal var mRootRef = FirebaseDatabase.getInstance().reference
    internal var mConditionRef = mRootRef.child("partner")
    internal var muserRef=mRootRef.child("users")
    internal var mlocRef=mRootRef.child("location")



    internal var mchildUserRef: DatabaseReference?=null
    internal var mchildUserFaceChatRef: DatabaseReference?=null
    internal var mchildOtherFaceChatRef: DatabaseReference?=null
    internal var muserlatRef:DatabaseReference?=null
    internal var muserlongRef:DatabaseReference?=null
    internal var mlimitlocRef:DatabaseReference?=null

    internal var mchildRef: DatabaseReference?=null
    internal var mchild1Ref: DatabaseReference?=null
    internal var mchild2Ref: DatabaseReference?=null
    internal var mchild3Ref: DatabaseReference?=null
    internal var motherNickRef:DatabaseReference?=null
    internal var Notifi_M: NotificationManager?=null

    internal var mchildlocRef:DatabaseReference?=null
    internal var nowLatRef:DatabaseReference?=null
    internal var nowLongRef:DatabaseReference?=null
    internal var nowLatLang:DatabaseReference?=null
    internal var nowBattery:DatabaseReference?=null
    internal var nowNetwork:DatabaseReference?=null
    internal var nowGps:DatabaseReference?=null
    internal var nowtime:DatabaseReference?=null
    internal var mtimeRef: DatabaseReference?=null


    internal var thread: ServiceThread? = null

    internal var Notifi: Notification?=null
    private var users:User=User()

    private var lastime=""
    private var curtime=""
    private var username=""
    private var usernick=""
    private var connectnick=" "
    private var facechatchannel=" "
    private var otherfacechatchannel= " "

    private var nowLat=0.0
    private var nowLong=0.0
    var longitude:Double = 0.0
    var latitude:Double = 0.0
    private var limitrange:LimitRange?=null
    var percent=0
    var isLogin = false
    var isOnline:Boolean = false
    var gpsEnable=false
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    val mBatteryRecv = object : BroadcastReceiver() {
        // 이벤트를 수신하는 이벤트 함수
        override fun onReceive(context: Context, intent: Intent) {
            // 배터리 잔량을 화면에 표시
            showLevel(intent)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Notifi_M = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mAuthListener = FirebaseAuth.AuthStateListener {
            firebaseAuth ->
            val user = firebaseAuth.currentUser
            updateUI(user)
        }
        mAuth.addAuthStateListener(mAuthListener!!)
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록하기~!!!
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 600000, 1.toFloat(), mLocationListener)
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 600000, 1.toFloat(), mLocationListener)

        } catch (ex: SecurityException) {
            ;
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        filter.addAction(Intent.ACTION_BATTERY_LOW)  // 배터리양 낮음
        filter.addAction(Intent.ACTION_BATTERY_OKAY)  // 배터리양 정상
        filter.addAction(Intent.ACTION_POWER_CONNECTED)  // 전원케이블 연결
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED)  // 전원케이블 분리
        registerReceiver(mBatteryRecv, filter)


        try {
            val conMan = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val wifi = conMan.getNetworkInfo(1).getState()
            if (wifi === NetworkInfo.State.CONNECTED) {
                isOnline = true
            }
            val mobile = conMan.getNetworkInfo(0).getState()
            if (mobile === NetworkInfo.State.CONNECTED) {
                isOnline = true
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        val manager=this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            gpsEnable=true
        }





        Handler().postDelayed({
            mchildUserRef!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.exists()){
                        users=dataSnapshot.getValue(User::class.java)
                        mchildRef=mConditionRef.child(users.getNickname()).child("nickname")
                        mchild1Ref=mConditionRef.child(users.getNickname()).child("name")
                        mchild2Ref=mConditionRef.child(users.getNickname()).child("lastime")
                        mchild3Ref=mConditionRef.child(users.getNickname()).child("times")
                        mchildUserFaceChatRef=mchildUserRef!!.child("faceChatChannel")
                        motherNickRef=muserRef.child(users.getOtherUid()).child("nickname")


                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })

            Handler().postDelayed({
                if(users.getOtherUid()!=" ") {
                    muserlatRef = mlocRef.child(users.getOtherUid()).child("현재위치").child("위도")
                    muserlongRef = mlocRef.child(users.getOtherUid()).child("현재위치").child("경도")
                    mlimitlocRef = mlocRef.child(users.getOtherUid()).child("LimitRange")
                    mchildOtherFaceChatRef = muserRef.child(users.getOtherUid()).child("faceChatChannel")
                    mchildOtherFaceChatRef!!.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            //  otherfacechatchannel=dataSnapshot.getValue().toString()
                        }
                        override fun onCancelled(p0: DatabaseError) {
                        }
                    })
                    muserlatRef?.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(p: DataSnapshot) {
                            if(p.exists()) {
                                val v = p.getValue().toString().toDouble()
                                nowLat = v
                            }
                        }

                        override fun onCancelled(p: DatabaseError) {
                        }
                    })
                    muserlongRef?.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(p: DataSnapshot) {
                            if(p.exists()) {
                                val v = p.getValue().toString().toDouble()
                                nowLong = v
                            }
                        }

                        override fun onCancelled(p: DatabaseError) {
                        }
                    })
                    motherNickRef?.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if(dataSnapshot.exists()) {
                                connectnick = dataSnapshot.getValue().toString()
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                        }
                    })

                }
                mchildRef!!.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if(dataSnapshot.exists()) {
                            usernick = dataSnapshot.getValue().toString()
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
                mchild1Ref!!.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if(dataSnapshot.exists()) {
                            username = dataSnapshot.getValue().toString()
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
                mchild2Ref!!.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if(dataSnapshot.exists()) {
                            lastime = dataSnapshot.getValue().toString()
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })


                Handler().postDelayed({
                    if(users.getOtherUid()!=" ") {
                        mlimitlocRef?.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(p: DataSnapshot) {
                                if(p.exists())
                                {
                                    val data = p.getValue().toString()
                                    if(data == " "){

                                    }
                                    else{
                                        limitrange = p.getValue(LimitRange::class.java)
                                        if (limitrange != null) {
                                            val laa = Location("a")
                                            laa.setLatitude(limitrange!!.latitude)
                                            laa.setLongitude(limitrange!!.longitude)
                                            val lab = Location("b")
                                            lab.setLatitude(nowLat)
                                            lab.setLongitude(nowLong)
                                            val dist = laa.distanceTo(lab)
                                            if (Math.pow(dist.toDouble() / 1000.0, 2.0) < limitrange!!.radius) {
                                            } else {
                                                val now: Long = System.currentTimeMillis()
                                                val date: Date = Date(now)
                                                val sdfNow: SimpleDateFormat = SimpleDateFormat("dd일HH시mm분", Locale.KOREA)
                                                val strNow: String = sdfNow.format(date)
                                                val intent = Intent(this@MyService, PresentLocation::class.java)
                                                val push = Intent()
                                                val pendingIntent = PendingIntent.getActivity(this@MyService, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
                                                push.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                push.setClass(applicationContext, MyService::class.java)
                                                Notifi = Notification.Builder(applicationContext)
                                                        .setContentTitle("제한범위이탈 " + strNow)
                                                        .setContentText(connectnick + "님이 제한범위를 벗어났습니다.")
                                                        .setSmallIcon(R.drawable.location)
                                                        .setTicker("범위벗어남!!")
                                                        .setContentIntent(pendingIntent)
                                                        .setPriority(Notification.PRIORITY_MAX)
                                                        .addAction(android.R.drawable.star_on, "확인하기", pendingIntent)
                                                        .setAutoCancel(true)
                                                        .setFullScreenIntent(pendingIntent, true)
                                                        .build()

                                                //소리추가
                                                Notifi!!.defaults = Notification.DEFAULT_SOUND
                                                //확인하면 자동으로 알림이 제거 되도록
                                                Notifi!!.flags = Notification.FLAG_AUTO_CANCEL

                                                Notifi_M!!.notify(778, Notifi)
                                            }
                                        }
                                    }
                                }
                            }
                            override fun onCancelled(p: DatabaseError) {

                            }
                        })
                    }
                    mchildUserFaceChatRef?.addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(p0: DataSnapshot?) {
                            if(p0!!.exists()){
                                facechatchannel=p0!!.getValue().toString()
                                if(facechatchannel == " "  ){
                                }
                                else if(otherfacechatchannel != " " && facechatchannel!=" " );
                                else{
                                    val intent = Intent(this@MyService, RTCFaceActivity::class.java)
                                    val push=Intent()
                                    val pendingIntent = PendingIntent.getActivity(this@MyService, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
                                    push.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    push.setClass(applicationContext,MyService::class.java)
                                    Notifi = Notification.Builder(applicationContext)
                                            .setContentTitle("화상통화")
                                            .setContentText(usernick+"님과의 연결 요청")
                                            .setSmallIcon(R.drawable.facetalk)
                                            .setTicker("전화요청!!")
                                            .setContentIntent(pendingIntent)
                                            .setPriority(Notification.PRIORITY_MAX)
                                            .addAction(android.R.drawable.star_on,"확인하기",pendingIntent)
                                            .setAutoCancel(true)
                                            .setFullScreenIntent(pendingIntent,true)
                                            .build()

                                    //소리추가
                                    Notifi!!.defaults = Notification.DEFAULT_SOUND
                                    //확인하면 자동으로 알림이 제거 되도록
                                    Notifi!!.flags = Notification.FLAG_AUTO_CANCEL
                                    Notifi_M!!.notify(777, Notifi)
                                }
                            }
                        }
                        override fun onCancelled(p0: DatabaseError?) {
                        }
                    })


                    mchild3Ref?.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if(dataSnapshot.exists()) {
                                curtime = dataSnapshot.getValue().toString()
                                Handler().postDelayed({
                                    if (lastime.equals(curtime)|| usernick == " ")
                                        ;
                                    else {
                                        val intent = Intent(this@MyService, MainActivity::class.java)
                                        val push = Intent()
                                        val pendingIntent = PendingIntent.getActivity(this@MyService, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                        push.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        push.setClass(applicationContext, MyService::class.java)
                                        Notifi = Notification.Builder(applicationContext)
                                                .setContentTitle(curtime)
                                                .setContentText(usernick + "(" + username + ")님과의 연결 요청")
                                                .setSmallIcon(R.drawable.alarm)
                                                .setTicker("알림!!!")
                                                .setContentIntent(pendingIntent)
                                                .setPriority(Notification.PRIORITY_MAX)
                                                .addAction(android.R.drawable.star_on, "확인하기", pendingIntent)
                                                .setAutoCancel(true)
                                                .setFullScreenIntent(pendingIntent, true)
                                                .build()
                                        //소리추가
                                        Notifi!!.defaults = Notification.DEFAULT_SOUND
                                        //알림 소리를 한번만 내도록
                                        Notifi!!.flags = Notification.FLAG_ONLY_ALERT_ONCE
                                        //확인하면 자동으로 알림이 제거 되도록
                                        Notifi!!.flags = Notification.FLAG_AUTO_CANCEL
                                        Notifi_M!!.notify(787, Notifi)
                                    }
                                }, 1000)
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                        }
                    })
                }, 1500)
            }, 1200)
        }, 800)
        return Service.START_STICKY
    }

    //서비스가 종료될 때 할 작업
    override fun onDestroy() {

        thread!!.stopForever()

        thread = null//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.

    }

    fun showLevel(intent: Intent) {
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        percent = (level.toFloat() / scale.toFloat() * 100.0).toInt()

    }
    private val mLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.

            longitude = location.getLongitude() //경도
            latitude = location.getLatitude()   //위도
            nowLatRef = mchildlocRef!!.child("현재위치").child("위도")
            nowLongRef = mchildlocRef!!.child("현재위치").child("경도")
            nowBattery = mchildlocRef!!.child("현재위치").child("베터리상태")
            nowNetwork = mchildlocRef!!.child("현재위치").child("네트워크")
            nowtime = mchildlocRef!!.child("현재위치").child("수신시각")
            nowGps = mchildlocRef!!.child("현재위치").child("GPS")
            nowLatRef?.setValue(latitude)
            nowLongRef?.setValue(longitude)
            nowBattery?.setValue(percent)
            if(isOnline)
                nowNetwork?.setValue("네트워크가 연결되어 있습니다")
            else
                nowNetwork?.setValue("네트워크가 연결되어 있지 않습니다")
            if(gpsEnable){
                nowGps?.setValue("GPS가 연결되어 있습니다")
            }
            else{
                nowGps?.setValue("GPS가 연결되어 있지 않습니다")
            }
            val now:Long = System.currentTimeMillis()
            val date:Date=Date(now)
            val sdfNow:SimpleDateFormat= SimpleDateFormat("dd일 HH시mm분", Locale.KOREA)
            val sdfNow2:SimpleDateFormat= SimpleDateFormat("MMddHHmm", Locale.KOREA)
            val strNow:String = sdfNow.format(date)
            val strNow2:String=sdfNow2.format(date)
            val loc: location = location(strNow,latitude,longitude)
            nowtime?.setValue(strNow)
            mtimeRef=nowLatLang!!.child(strNow2)
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

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            mchildUserRef = muserRef.child(user.uid)
            mchildlocRef=mlocRef.child((user.uid))
            nowLatLang= mchildlocRef!!.child("time")
            isLogin=true
        } else {

        }
    }

}


