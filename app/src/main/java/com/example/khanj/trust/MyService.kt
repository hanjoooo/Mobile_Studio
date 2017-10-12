package com.example.khanj.trust



import android.app.Notification

import android.app.NotificationManager

import android.app.PendingIntent

import android.app.Service

import android.content.Context

import android.content.Intent

import android.os.Handler

import android.os.IBinder

import android.widget.Toast
import com.example.khanj.trust.Data.Messege
import com.example.khanj.trust.Data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*


class MyService : Service() {

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null

    internal var mRootRef = FirebaseDatabase.getInstance().reference
    internal var mConditionRef = mRootRef.child("partner")
    internal var muserRef=mRootRef.child("users")

    internal var mchildUserRef: DatabaseReference?=null
    internal var mchildRef: DatabaseReference?=null
    internal var mchild1Ref: DatabaseReference?=null
    internal var mchild2Ref: DatabaseReference?=null
    internal var mchild3Ref: DatabaseReference?=null

    internal var Notifi_M: NotificationManager?=null

    internal var thread: ServiceThread? = null

    internal var Notifi: Notification?=null
    private var users:User?=null
    private var mes:Messege= Messege()

    private var lastime=""
    private var curtime=""
    private var username=""
    private var usernick=""
    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Notifi_M = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mAuthListener = FirebaseAuth.AuthStateListener {
            firebaseAuth ->
            val user = firebaseAuth.currentUser
            updateUI(user)
        }
        mAuth.addAuthStateListener(mAuthListener!!)
        Handler().postDelayed({
            mchildUserRef!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    users=dataSnapshot.getValue(User::class.java)
                    mchildRef=mConditionRef.child(users!!.getNickname()).child("nickname")
                    mchild1Ref=mConditionRef.child(users!!.getNickname()).child("name")
                    mchild2Ref=mConditionRef.child(users!!.getNickname()).child("lastime")
                    mchild3Ref=mConditionRef.child(users!!.getNickname()).child("times")

                }
                override fun onCancelled(databaseError: DatabaseError) {
                }

            })
            Handler().postDelayed({
                mchildRef!!.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        usernick=dataSnapshot.getValue().toString()
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
                mchild1Ref!!.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        username=dataSnapshot.getValue().toString()
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
                mchild2Ref!!.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        lastime=dataSnapshot.getValue().toString()
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
                mchild3Ref!!.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        curtime=dataSnapshot.getValue().toString()
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
                Handler().postDelayed({
                if(lastime==curtime || usernick ==" ")
                    ;
                else {
                    val intent = Intent(this@MyService, MainActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(this@MyService, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    Notifi = Notification.Builder(applicationContext)
                            .setContentTitle(curtime)
                            .setContentText(usernick+"("+username+")님과의 연결 요청")
                            .setSmallIcon(R.drawable.background2)
                            .setTicker("알림!!!")
                            .setContentIntent(pendingIntent)
                            .build()
                    //소리추가
                    Notifi!!.defaults = Notification.DEFAULT_SOUND
                    //알림 소리를 한번만 내도록
                    Notifi!!.flags = Notification.FLAG_ONLY_ALERT_ONCE
                    //확인하면 자동으로 알림이 제거 되도록
                    Notifi!!.flags = Notification.FLAG_AUTO_CANCEL
                    Notifi_M!!.notify(777, Notifi)
                    //토스트 띄우기
                    Toast.makeText(this@MyService, "알림!!", Toast.LENGTH_LONG).show()
                    mchild2Ref!!.setValue(curtime)
                }
                }, 3000)
            }, 3000)
        }, 1000)

        return Service.START_STICKY
    }

    //서비스가 종료될 때 할 작업
    override fun onDestroy() {

        thread!!.stopForever()

        thread = null//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.

    }


    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            mchildUserRef = muserRef.child(user.uid)
        } else {

        }
    }

}


