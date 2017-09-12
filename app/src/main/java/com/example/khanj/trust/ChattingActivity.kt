package com.example.khanj.trust

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Adapter
import android.widget.ArrayAdapter
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chatting.*
import java.util.*


class ChattingActivity : AppCompatActivity() {

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseReference = firebaseDatabase.reference
    var userName = "user" + "123";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatting)

        sendbutton.setOnClickListener(){
            var chatData:ChatData= ChatData(userName,editText.getText().toString())
            databaseReference.child("message").push().setValue(chatData)
            editText.setText(" ")
        }

        databaseReference.child("message").addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(dataSnapshot: DataSnapshot,string: String) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                var chaData:ChatData =dataSnapshot.getValue<ChatData>(ChatData::class.java)
            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }
}
