package com.example.khanj.trust

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.example.khanj.trust.Data.Messege
import com.example.khanj.trust.Data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class MessegeService : Service() {
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null

    internal var mRootRef = FirebaseDatabase.getInstance().reference
    internal var mConditionRef = mRootRef.child("partner")
    internal var muserRef=mRootRef.child("users")

    internal var mchildUserRef: DatabaseReference?=null
    internal var mchildRef: DatabaseReference?=null


    private var users:User?=null
    private var messeges:Messege?=null

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.

        throw UnsupportedOperationException("Not yet implemented")


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mAuthListener = FirebaseAuth.AuthStateListener {
            firebaseAuth ->
            val user = firebaseAuth.currentUser
            updateUI(user)
        }
        mchildUserRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                users=dataSnapshot.getValue(User::class.java)
                mchildRef=mConditionRef.child(users!!.getNickname())
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }

        })

        mchildRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.equals(null)){

                }
                else{

                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
        mConditionRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val datas = dataSnapshot.getValue().toString()
                val mBuilder = Notification.Builder(this@MessegeService)
                        .setSmallIcon(R.drawable.background2)
                        .setContentTitle("메시지가 왔습니다")
                        .setContentText("내용 : "+datas+"로부터 연결신청이 왔습니다")
                        .setTicker("알림!!!")
                        .setAutoCancel(true)

                // Creates an explicit intent for an Activity in your app
                val resultIntent = Intent(this@MessegeService, MainActivity::class.java)

                // The stack builder object will contain an artificial back stack for the
                // started Activity.
                // This ensures that navigating backward from the Activity leads out of
                // your application to the Home screen.
                val stackBuilder = TaskStackBuilder.create(this@MessegeService)
                // Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(MainActivity::class.java)
                // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent)
                val resultPendingIntent = stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                )
                mBuilder.setContentIntent(resultPendingIntent)
                val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                // mId allows you to update the notification later on.
                mNotificationManager.notify(1, mBuilder.build())
                Toast.makeText(this@MessegeService, "Trust : 알림", Toast.LENGTH_LONG).show()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        return super.onStartCommand(intent, flags, startId)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            mchildUserRef=muserRef.child(user.uid)
        } else {

        }
    }


}


