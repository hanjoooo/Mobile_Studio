package com.example.khanj.trust
/*
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
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chatting.*
import java.util.*


class ChattingActivity :AppCompatActivity(){
    private val FIREBASE_URL : String = "https://trust-cd479.firebaseio.com/ "
    private var mUsername:String=" "
    internal var mRootRef = FirebaseDatabase.getInstance().reference

    private var mConnectedListener:ValueEventListener?=null
    private val mConnectedListner:ValueEventListener?=null


    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val mFirebaseRef=firebaseDatabase.reference.child("chat")

    private var mChatListAdapter : ChatListAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatting)

        setupUsername()

        setTitle("Chatting as" + mUsername)

        var inputText:EditText=findViewById(R.id.messageInput)
        inputText.setOnEditorActionListener(object : TextView.OnEditorActionListener{

            override fun onEditorAction(textView: TextView,actionId:Int,keyEvent:KeyEvent):Boolean{
                if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() === KeyEvent.ACTION_DOWN) {
                    sendMessage()
                }
                return true
            }
        })
        var sendButton:ImageButton=findViewById(R.id.sendButton)
        sendButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                sendMessage()
            }

        })


    }

    override fun onStart() {
        super.onStart()

        val listview:ListView=getListView()
        mChatListAdapter= ChatListAdapter(mFirebaseRef.limitToLast(50),this,R.layout.chat_message,mUsername)
        listView.setAdapter(mChatListAdapter)
        mChatListAdapter!!.registerDataSetObserver(object :DataSetObserver(){
            override fun onChanged() {
                super.onChanged()
                listView.setSelection(mChatListAdapter!!.getCount()-1)
            }
        })


        mConnectedListener = mFirebaseRef!!.getRoot().child(".info/connected").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var connected:Boolean = dataSnapshot.getValue() as Boolean
                if(connected)
                    Toast.makeText(this@ChattingActivity, "Connected to Firebase", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this@ChattingActivity, "Disconnected from Firebase", Toast.LENGTH_SHORT).show()


            }

            override fun onCancelled(p0: DatabaseError?) {
            }


        })
    }

    override fun onStop() {
        super.onStop()
        mFirebaseRef!!.getRoot().child(".info/connected").removeEventListener(mConnectedListener)
        mChatListAdapter!!.cleanup()
    }

    private fun setupUsername() {
        val prefs = getApplication().getSharedPreferences("ChatPrefs", 0)
        mUsername = prefs.getString("username", null)
        if (mUsername == null) {
            val r = Random()
            // Assign a random user name if we don't have one saved.
            mUsername = "JavaUser" + r.nextInt(100000)
            prefs.edit().putString("username", mUsername).commit()
        }
    }

    private fun sendMessage() {
        val inputText:EditText = findViewById(R.id.messageInput)
        val input:String = inputText.getText().toString()
        if (!input.equals("")) {
            // Create our 'model', a Chat object
            val chat:Chat = Chat(input, mUsername)
            // Create a new, auto-generated child of that chat location, and save our chat data there
            mFirebaseRef!!.push().setValue(chat)
            inputText.setText("")
        }
    }

}
*/