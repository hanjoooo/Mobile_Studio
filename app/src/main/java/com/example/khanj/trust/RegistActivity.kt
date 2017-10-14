package com.example.khanj.trust

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Spinner
import android.widget.Toast
import com.example.khanj.trust.Data.Messege
import com.example.khanj.trust.Data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import kotlinx.android.synthetic.main.activity_regist.*
import java.text.SimpleDateFormat
import java.util.*

class RegistActivity :  BaseActivity() {
    private var mAuth : FirebaseAuth = FirebaseAuth.getInstance()
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null

    internal var mRootRef = FirebaseDatabase.getInstance().reference
    internal var mConditionRef = mRootRef.child("users")
    internal var mchildRef: DatabaseReference?=null

    internal var mnotifiyRef=mRootRef.child("partner")
    internal var mnotifiyChildRef:DatabaseReference?=null

    var Users: User?=null
    private var userUid:String=" "
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_regist)
        //비밀번호 일치 검사
        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val password = etPassword.text.toString()
                val confirm = etPasswordConfirm.text.toString()

                if(password==confirm){
                    etPassword.setBackgroundColor(Color.GREEN)
                    etPasswordConfirm.setBackgroundColor(Color.GREEN)
                }
                else{
                    etPassword.setBackgroundColor(Color.RED)
                    etPasswordConfirm.setBackgroundColor(Color.RED)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })


        mAuthListener = FirebaseAuth.AuthStateListener {
            firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.uid)
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out")
            }
            // [START_EXCLUDE]
            updateUI(user)
            // [END_EXCLUDE]
        }

        btnCancel.setOnClickListener{finish()}
        btnDone.setOnClickListener(View.OnClickListener {
            // 이메일 입력 확인
            if (etEmail!!.text.toString().length == 0) {
                Toast.makeText(this@RegistActivity, "이메일(아이디) 입력하세요!", Toast.LENGTH_SHORT).show()
                etEmail!!.requestFocus()
                return@OnClickListener
            }

            // 비밀번호 입력 확인
            if (etPassword!!.text.toString().length == 0) {
                Toast.makeText(this@RegistActivity, "비밀번호를 입력하세요!", Toast.LENGTH_SHORT).show()
                etPassword!!.requestFocus()
                return@OnClickListener
            }

            // 비밀번호 확인 입력 확인
            if (etPasswordConfirm!!.text.toString().length == 0) {
                Toast.makeText(this@RegistActivity, "비밀번호 확인을 입력하세요!", Toast.LENGTH_SHORT).show()
                etPasswordConfirm!!.requestFocus()
                return@OnClickListener
            }

            // 비밀번호 일치 확인
            if (etPassword!!.text.toString() != etPasswordConfirm!!.text.toString()) {
                Toast.makeText(this@RegistActivity, "비밀번호가 일치하지 않습니다!", Toast.LENGTH_SHORT).show()
                etPassword!!.setText("")
                etPasswordConfirm!!.setText("")
                etPassword!!.requestFocus()
                return@OnClickListener
            }
            // 이름 입력 확인
            if (edname!!.text.toString().length == 0) {
                Toast.makeText(this@RegistActivity, "이름 입력하세요!", Toast.LENGTH_SHORT).show()
                edname!!.requestFocus()
                return@OnClickListener
            }
            //닉네임 입력 확인
            if (nickname!!.text.toString().length == 0) {
                Toast.makeText(this@RegistActivity, "닉네임을 입력하세요!", Toast.LENGTH_SHORT).show()
                nickname!!.requestFocus()
                return@OnClickListener
            }
            //폰번호 입력 확인
            if (edphone!!.text.toString().length == 0) {
                Toast.makeText(this@RegistActivity, "폰번호를 입력하세요!", Toast.LENGTH_SHORT).show()
                edphone!!.requestFocus()
                return@OnClickListener
            }

            val result = Intent()
            result.putExtra("ID", etEmail!!.text.toString())
            result.putExtra("password", etPassword!!.text.toString())
            result.putExtra("name", edname!!.text.toString())

            Users= User(etEmail.text.toString(),etPassword!!.text.toString(),
                    nickname!!.text.toString(),edname!!.text.toString(),edphone.text.toString(),userUid," "," ")
            createAccount(etEmail!!.text.toString(), etPassword!!.text.toString())

            val now:Long = System.currentTimeMillis()
            val date: Date = Date(now)
            val sdfNow: SimpleDateFormat = SimpleDateFormat("dd일HH시mm분ss초", Locale.KOREA)
            val strNow:String = sdfNow.format(date)
            val messege= Messege(" "," "," "," ",strNow)

            mnotifiyChildRef=mnotifiyRef.child(nickname!!.text.toString())
            mnotifiyChildRef!!.setValue(messege)
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
    private fun createAccount(email: String, password: String) {
        Log.d(TAG, "createAccount:" + email)
        if (!validateForm()) {
            return
        }
        showProgressDialog()
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful)
                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful) {
                        Toast.makeText(this@RegistActivity, "회원가입 실패",
                                Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@RegistActivity, "회원가입 성공", Toast.LENGTH_LONG).show()
                        mchildRef!!.setValue(Users)
                        signOut()
                        finish()
                    }
                    hideProgressDialog()
                    // [END_EXCLUDE]
                }
        // [END create_user_with_email]
    }
    private fun validateForm(): Boolean {
        var valid = true

        val email = etEmail!!.text.toString()
        if (TextUtils.isEmpty(email)) {
            etEmail!!.error = "Required."
            valid = false
        } else {
            etEmail!!.error = null
        }

        val password = etPassword!!.text.toString()
        if (TextUtils.isEmpty(password)) {
            etPassword!!.error = "Required."
            valid = false
        } else {
            etPassword!!.error = null
        }

        return valid
    }
    private fun updateUI(user: FirebaseUser?) {
        hideProgressDialog()
        if (user != null) {
            mchildRef = mConditionRef.child(user.uid)
            userUid=user.uid
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
