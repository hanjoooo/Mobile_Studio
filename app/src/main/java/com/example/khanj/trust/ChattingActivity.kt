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


    private  var datas =  ArrayList<Chat>()
    var adpater:ChatListAdapter?=null

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
            adpater = com.example.khanj.trust.ChatListAdapter(datas,mUserUid,this@ChattingActivity)
            chatlist.adapter=adpater
        }, 500)

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
    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener!!)
        Handler().postDelayed({
            mchildRef!!.child("name").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val data = dataSnapshot.getValue().toString()
                    mUsername=data
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })

            Handler().postDelayed({
                mRootRef.child("chat").addValueEventListener(object:ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        datas.clear()
                        for(snapshot in dataSnapshot.children){
                            datas.add(snapshot.getValue(Chat::class.java))
                        }
                        adpater = com.example.khanj.trust.ChatListAdapter(datas,mUserUid,this@ChattingActivity)
                        chatlist.adapter=adpater
                        adpater!!.notifyDataSetChanged()
                    }
                    override fun onCancelled(p0: DatabaseError?) {
                    }

                })
                /*
                mRootRef.child("chat").addChildEventListener(object:ChildEventListener{
                    //child에서 일어나는 change들을 감지.
                    // message는 child의 이벤트를 수신
                    // RealTime DB 사용해서 채팅 얻기
                    override fun onChildAdded(dataSnapshot: DataSnapshot?, s: String?) {
                        // 리스트의 아이템을 검색하거나 아이템의 추가가 있을때 수신
                        val chat = dataSnapshot!!.getValue(Chat::class.java)
                        datas.add(chat)
                        adpater = com.example.khanj.trust.ChatListAdapter(datas,mUserUid,this@ChattingActivity)
                        chatlist.adapter=adpater
                        //adpater!!.notifyDataSetChanged()
                    }
                    override fun onChildRemoved(p0: DataSnapshot?) {
                    }
                    override fun onChildChanged(dataSnapshot: DataSnapshot?, p1: String?) {
                    }
                    override fun onChildMoved(p0: DataSnapshot?, p1: String?) {

                    }
                    override fun onCancelled(p0: DatabaseError?) {

                    }
                })
                */
            }, 500)

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