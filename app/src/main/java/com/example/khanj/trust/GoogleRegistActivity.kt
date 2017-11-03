package com.example.khanj.trust

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.khanj.trust.Data.GoogleUser
import com.example.khanj.trust.Data.Messege
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_regist.*
import java.text.SimpleDateFormat
import java.util.*

class GoogleRegistActivity : BaseActivity() {
    private var mAuth : FirebaseAuth = FirebaseAuth.getInstance()
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null

    internal var mRootRef = FirebaseDatabase.getInstance().reference
    internal var mConditionRef = mRootRef.child("users")
    internal var mLocationRef = mRootRef.child("location")

    internal var mchildRef: DatabaseReference?=null
    internal var mnotifiyRef=mRootRef.child("partner")
    internal var mnotifiyChildRef:DatabaseReference?=null
    var Users: GoogleUser?=null
    private var userUid:String=" "
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_regist)

        mAuthListener = FirebaseAuth.AuthStateListener {
            firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in
                Log.d(GoogleRegistActivity.TAG, "onAuthStateChanged:signed_in:" + user.uid)
            } else {
                // User is signed out
                Log.d(GoogleRegistActivity.TAG, "onAuthStateChanged:signed_out")
            }
            // [START_EXCLUDE]
            updateUI(user)
            // [END_EXCLUDE]
        }

        btnCancel.setOnClickListener{finish()}
        btnDone.setOnClickListener(View.OnClickListener {
            // 이름 입력 확인
            if (edname!!.text.toString().length == 0) {
                Toast.makeText(this@GoogleRegistActivity, "이름 입력하세요!", Toast.LENGTH_SHORT).show()
                edname!!.requestFocus()
                return@OnClickListener
            }
            //닉네임 입력 확인
            if (nickname!!.text.toString().length == 0) {
                Toast.makeText(this@GoogleRegistActivity, "닉네임을 입력하세요!", Toast.LENGTH_SHORT).show()
                nickname!!.requestFocus()
                return@OnClickListener
            }
            //폰번호 입력 확인
            if (edphone!!.text.toString().length == 0) {
                Toast.makeText(this@GoogleRegistActivity, "폰번호를 입력하세요!", Toast.LENGTH_SHORT).show()
                edphone!!.requestFocus()
                return@OnClickListener
            }

            Users= GoogleUser(nickname!!.text.toString(),edname!!.text.toString(),edphone.text.toString(),userUid," "," "," ")

            mchildRef!!.setValue(Users)

            val now:Long = System.currentTimeMillis()
            val date: Date = Date(now)
            val sdfNow: SimpleDateFormat = SimpleDateFormat("dd일HH시mm분ss초", Locale.KOREA)
            val strNow:String = sdfNow.format(date)
            val messege= Messege(" "," "," "," ",strNow)

            mnotifiyChildRef=mnotifiyRef.child(nickname!!.text.toString())
            mnotifiyChildRef!!.setValue(messege)
            finish()
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        })
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener!!)

        mConditionRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    override fun onStop() {
        super.onStop()
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener!!)
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        hideProgressDialog()
        if (user != null) {
            mchildRef = mConditionRef.child(user.uid)
            userUid=user.uid
            mLocationRef.child(userUid).child("LimitRange").setValue(" ")
        } else {

        }
    }
    private fun signOut() {
        showProgressDialog()
        mAuth.signOut()
        hideProgressDialog()
    }

    companion object {
        private val TAG = "EmailPassword"
    }
}
