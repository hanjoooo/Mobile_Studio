package com.example.khanj.trust

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mAuthListener: FirebaseAuth.AuthStateListener?=null
    private var userOn=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val intent=Intent(this,MyService::class.java)
        startService(intent)
        startLoading()
        mAuthListener = FirebaseAuth.AuthStateListener {
            firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                userOn=true
            } else {
                // User is signed out
            }
            // [START_EXCLUDE]
            // [END_EXCLUDE]
        }
    }
    private fun startLoading() {
        val handler = Handler()
        handler.postDelayed({
            if(userOn==true){
                val intent = Intent(baseContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            else {
                val intent = Intent(baseContext, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 2000)
    }
    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener!!)
    }

}
