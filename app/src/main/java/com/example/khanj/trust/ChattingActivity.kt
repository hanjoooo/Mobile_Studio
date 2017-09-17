package com.example.khanj.trust

import android.app.ListActivity
import android.database.DataSetObservable
import android.database.DataSetObserver
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chatting.*
import java.text.SimpleDateFormat
import java.util.*


class ChattingActivity :AppCompatActivity(){
    private val FIREBASE_URL : String = "https://trust-cd479.firebaseio.com/ "
    private var mUsername:String=" "

    private var mChatListAdapter : ChatListAdapter?=null

    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null

    internal var mRootRef = FirebaseDatabase.getInstance().reference
    internal var mConditionRef = mRootRef.child("chat")

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
            mUsername="hanjoo"
            val chatMessage=Chat(message,mUsername,strNow)
            mChatRef.setValue(chatMessage)
            messageInput.setText("")
        }

        adpater = com.example.khanj.trust.ChatListAdapter(datas,this)
        chatlist.setAdapter(adpater)
    }
    private val postListener = object : ValueEventListener {
        override fun onDataChange(datasnapshot: DataSnapshot) {
            datas.clear()
            for(snapshot in datasnapshot.getChildren()) {
                var chat = snapshot.getValue(Chat::class.java)
                datas.add(chat!!)
            }
            adpater.notifyDataSetChanged()

            adpater = com.example.khanj.trust.ChatListAdapter(datas,this@ChattingActivity)
            chatlist.setAdapter(adpater)


        }

        override fun onCancelled(p0: DatabaseError?) {

        }
    }
    override fun onStart() {
        super.onStart()
        mConditionRef.addValueEventListener(postListener)


    }

    override fun onStop() {
        super.onStop()

    }


}
