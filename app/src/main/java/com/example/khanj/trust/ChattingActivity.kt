package com.example.khanj.trust

import android.app.ListActivity
import android.database.DataSetObservable
import android.database.DataSetObserver
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.example.khanj.trust.Data.Chat
import com.example.khanj.trust.Data.User
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chatting.*
import java.text.SimpleDateFormat
import java.util.*


class ChattingActivity :AppCompatActivity(){
    private val FIREBASE_URL : String = "https://trust-cd479.firebaseio.com/ "
    private var mUsername:String=" "
    private var mUserUid:String=" "

    private var mChatListAdapter : ChatListAdapter?=null

    private var mAuth:FirebaseAuth=FirebaseAuth.getInstance()
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null

    internal var mRootRef = FirebaseDatabase.getInstance().reference
    internal var mConditionRef = mRootRef.child("chat")
    internal var mConditionRef1 = mRootRef.child("users")
    internal var mchildRef: DatabaseReference?=null
    internal var mchild1Ref: DatabaseReference?=null



    private  var datas =  ArrayList<Chat>()
    lateinit var adpater:com.example.khanj.trust.ChatListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatting)

        sendButton.setOnClickListener(){
            val now:Long = System.currentTimeMillis()
            val date:Date=Date(now)
            val sdfNow2: SimpleDateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA)
            val sdfNow:SimpleDateFormat= SimpleDateFormat("yy.MM.dd HH:mm", Locale.KOREA)
            val strNow:String = sdfNow.format(date)
            val strNow2:String=sdfNow2.format(date)
            val mChatRef=mConditionRef.child(strNow2)
            val message = messageInput.getText().toString()
            val chatMessage= Chat(message,mUsername,strNow,mUserUid)
            mChatRef.setValue(chatMessage)
            messageInput.setText("")
        }
        Handler().postDelayed({
        mConditionRef.addValueEventListener(postListener)

        adpater = com.example.khanj.trust.ChatListAdapter(datas,mUserUid,this@ChattingActivity)
        chatlist.setAdapter(adpater)
        }, 800)

        mAuthListener = FirebaseAuth.AuthStateListener {
            firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in
                Log.d(ChattingActivity.TAG, "onAuthStateChanged:signed_in:" + user.uid)
            } else {
                // User is signed out
                Log.d(ChattingActivity.TAG, "onAuthStateChanged:signed_out")
            }
            // [START_EXCLUDE]
            updateUI(user)
            // [END_EXCLUDE]
        }
    }
    private val postListener = object : ValueEventListener {
        override fun onDataChange(datasnapshot: DataSnapshot) {
            datas.clear()
            for(snapshot in datasnapshot.getChildren()) {
                val chat = snapshot.getValue(Chat::class.java)
                datas.add(chat!!)
            }
            adpater.notifyDataSetChanged()
            adpater = com.example.khanj.trust.ChatListAdapter(datas,mUserUid,this@ChattingActivity)
            chatlist.setAdapter(adpater)
        }
        override fun onCancelled(p0: DatabaseError?) {
        }
    }
    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener!!)
        Handler().postDelayed({
        mchildRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val datas = dataSnapshot.getValue(User::class.java)
                mUsername=datas.getName()
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
        }, 500)

    }
    override fun onStop() {
        super.onStop()
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener!!)
        }
    }
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            mchildRef = mConditionRef1.child(user.uid)
            mUserUid = user.uid
        } else {
        }
    }

    companion object {
        private val TAG = "EmailPassword"
    }
}
