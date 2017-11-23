package com.example.khanj.trust

import android.app.NotificationManager
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.EditText
import com.example.khanj.trust.Data.Messege
import com.example.khanj.trust.Data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_connect_other.*
import java.text.SimpleDateFormat
import java.util.*

class ConnectOtherActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    internal var mRootRef = FirebaseDatabase.getInstance().reference
    internal var mnotifiyRef=mRootRef.child("partner")
    internal var muserRef=mRootRef.child("users")
    internal var mnotifiyChildRef: DatabaseReference?=null
    internal var muserChildRef: DatabaseReference?=null
    internal var mMesLastime: DatabaseReference?=null
    internal var mMesCurtime: DatabaseReference?=null

    internal var mMesUid: DatabaseReference?=null
    internal var mMesNick: DatabaseReference?=null
    internal var mState:DatabaseReference?=null
    var mesLastime=" "
    var mesCurtime=" "
    var ConnectUser:String=""
    var userInfo: User?=null
    private var myUid =  " "
    private var mesUid=" "
    private var mesNick=" "
    var dialogOn=false
    private var State=" "

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_other)
        val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE)as NotificationManager
        notificationManager.cancel(787)
        btconnect.setOnClickListener{
            val now: Long = System.currentTimeMillis()
            val date: Date = Date(now)
            val sdfNow: SimpleDateFormat = SimpleDateFormat("dd일HH시mm분ss초", Locale.KOREA)
            val strNow: String = sdfNow.format(date)
            val messege = Messege(userInfo!!.getNickname(), userInfo!!.getName(), userInfo!!.getMyUid(), strNow, " ")
            ConnectUser = ednick.text.toString()
            mnotifiyChildRef = mnotifiyRef.child(ConnectUser)
            mnotifiyChildRef!!.setValue(messege)
        }
        mAuthListener = FirebaseAuth.AuthStateListener {
            firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
            } else {
            }
            updateUI(user)
        }

    }
    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener!!)

        Handler().postDelayed({
            muserChildRef?.addValueEventListener(object : ValueEventListener {
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
                mMesLastime?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        mesLastime=dataSnapshot.getValue().toString()
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
                mMesUid?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        mesUid=dataSnapshot.getValue().toString()
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
                mMesNick?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        mesNick=dataSnapshot.getValue().toString()
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })

                if(userInfo?.getOtherUid()!= " ") {
                    mState?.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val data = dataSnapshot.getValue().toString()
                            if (data == " ")
                                State = "현재 여유로운 상태입니다"
                            else
                                State = data
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                        }

                    })
                }
                Handler().postDelayed({
                    mMesCurtime?.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            mesCurtime=dataSnapshot.getValue().toString()
                            if(mesCurtime==mesLastime || mesUid == " " ) ;
                            else{
                                if(dialogOn);
                                else{
                                    dialogOn=true
                                    val aDialog:AlertDialog.Builder=AlertDialog.Builder(this@ConnectOtherActivity)
                                    aDialog.setTitle(mesNick+"님과 연결하시겠습니까?");

                                    aDialog.setPositiveButton("연결") { dialog, which ->
                                        mMesLastime!!.setValue(mesCurtime)
                                        muserChildRef!!.child("otherUid").setValue(mesUid)
                                        muserChildRef!!.child("chatChannel").setValue(mesUid)
                                        muserRef.child(mesUid).child("otherUid").setValue(myUid)
                                        muserRef.child(mesUid).child("chatChannel").setValue(mesUid)
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
                }, 500)
            }, 500)
        }, 100)
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
            myUid=user.uid
        } else {

        }
    }
    private fun signOut() {
        mAuth.signOut()
    }
}
