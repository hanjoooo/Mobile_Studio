package com.example.khanj.trust


import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Point
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.View

import com.sktelecom.playrtc.PlayRTC
import com.sktelecom.playrtc.PlayRTCFactory
//playrtc v2.2.0
import com.sktelecom.playrtc.config.PlayRTCConfig
//playrtc v2.2.0
import com.sktelecom.playrtc.config.PlayRTCVideoConfig.CameraType

import com.sktelecom.playrtc.exception.RequiredConfigMissingException
import com.sktelecom.playrtc.exception.RequiredParameterMissingException
import com.sktelecom.playrtc.exception.UnsupportedPlatformVersionException
import com.sktelecom.playrtc.observer.PlayRTCObserver
import com.sktelecom.playrtc.stream.PlayRTCMedia
import com.sktelecom.playrtc.util.ui.PlayRTCVideoView
import com.sktelecom.playrtc.config.PlayRTCAudioConfig.AudioCodec
import com.sktelecom.playrtc.config.PlayRTCVideoConfig.VideoCodec

import org.json.JSONException
import org.json.JSONObject

import java.io.File

import android.util.Log
import android.Manifest
import android.content.pm.PackageManager
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.widget.*
import com.example.khanj.trust.Data.Messege
import com.example.khanj.trust.Data.User
import com.example.khanj.trust.handler.BackPressCloseHandler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class RTCFaceActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null

    internal var mRootRef = FirebaseDatabase.getInstance().reference
    internal var mConditionRef = mRootRef.child("users")

    internal var mMyFaceChatRef:DatabaseReference?=null
    internal var mOhterFaceChatRef:DatabaseReference?=null
    internal var mchildUserFaceChatRef: DatabaseReference?=null

    // Please change this key for your own project.
    private val T_DEVELOPERS_PROJECT_KEY = "60ba608a-e228-4530-8711-fa38004719c1"

    private var toolbar: Toolbar? = null
    private var closeAlertDialog: AlertDialog? = null

    private var playrtc: PlayRTC? = null
    private var playrtcObserver: PlayRTCObserver? = null

    private var isCloseActivity = true
    private var isChannelConnected = false
    private var localView: PlayRTCVideoView? = null
    private var remoteView: PlayRTCVideoView? = null
    private var localMedia: PlayRTCMedia? = null
    private var remoteMedia: PlayRTCMedia? = null
    private var channelId=" "

    private val videoViewGroup: RelativeLayout? = null

    private var UserUid:String?=null
    private var UserInfo:User?=null

    private var backPressCloseHandler: BackPressCloseHandler? = null



    override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)
       setContentView(R.layout.activity_rtcface)

       // Application permission 23
       if (Build.VERSION.SDK_INT >= 23) {

           checkPermission(MANDATORY_PERMISSIONS)
       }
       createPlayRTCObserverInstance()
       // use sdk v2.2.0
       createPlayRTCInstance()
       setToolbar()
       setFragmentNavigationDrawer()
       setOnClickEventListenerToButton()
        mAuthListener = FirebaseAuth.AuthStateListener {
            firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in
                Log.d(RTCFaceActivity.TAG, "onAuthStateChanged:signed_in:" + user.uid)
            } else {
                // User is signed out
                Log.d(RTCFaceActivity.TAG, "onAuthStateChanged:signed_out")
            }
            // [START_EXCLUDE]
            updateUI(user)
            // [END_EXCLUDE]
        }
        val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE)as NotificationManager
        notificationManager.cancel(777)
        backPressCloseHandler = BackPressCloseHandler(this)
    }

    // Application permission 23
    private val MY_PERMISSION_REQUEST_STORAGE = 100

    @SuppressLint("NewApi")
    private fun checkPermission(permissions: Array<String>) {

        requestPermissions(permissions, MY_PERMISSION_REQUEST_STORAGE)
    }

    // Application permission 23
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSION_REQUEST_STORAGE -> {
                val cnt = permissions.size
                for (i in 0..cnt - 1) {

                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                        Log.i(LOG_TAG, "Permission[" + permissions[i] + "] = PERMISSION_GRANTED")

                    } else {

                        Log.i(LOG_TAG, "permission[" + permissions[i] + "] always deny")
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        // Make the videoView at the onWindowFocusChanged time.
        if (hasFocus && this.localView == null) {
            createVideoView()
        }
    }

    override protected fun onDestroy() {

        // instance release
        if (playrtc != null) {
            // If you does not call playrtc.close(), playrtc instence is remaining every new call.
            // playrtc instence can not used again
            playrtc!!.close()
            playrtc = null
        }
        // new v2.2.6
        if (localView != null) {
            localView!!.release()
        }
        // new v2.2.6
        if (remoteView != null) {
            remoteView!!.release()
        }
        playrtcObserver = null
        //Process.killProcess(Process.myPid())
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (isCloseActivity) {
            finish()
        } else {
            createCloseAlertDialog()
            closeAlertDialog!!.show()
        }
    }


    private fun createPlayRTCObserverInstance() {
        playrtcObserver = object : PlayRTCObserver() {
            override fun onConnectChannel(obj: PlayRTC?, channelId: String?, channelCreateReason: String?, channelType: String?) {
                Log.i(LOG_TAG, "onConnectChannel")
                isChannelConnected = true

                // Fill the channelId to the channel_id TextView.
                val channelIdTextView = findViewById<View>(R.id.channel_id) as TextView
                channelIdTextView.text = channelId
                if(UserInfo!!.getOtherUid()==" "){
                    val msg = "연결된 상대방이 없습니다."
                    Toast.makeText(this@RTCFaceActivity, msg, Toast.LENGTH_SHORT).show()
                }
                else{
                    mOhterFaceChatRef!!.child("faceChatChannel").setValue(channelId)
                }

            }

            override fun onAddLocalStream(obj: PlayRTC?, playRTCMedia: PlayRTCMedia?) {
                Log.i(LOG_TAG, "onAddLocalStream")
                localMedia = playRTCMedia

                // Link the media stream to the view.
                playRTCMedia!!.setVideoRenderer(localView!!.videoRenderer)
            }

            override fun onAddRemoteStream(obj: PlayRTC?, peerId: String?, peerUserId: String?, playRTCMedia: PlayRTCMedia?) {

                Log.i(LOG_TAG, "onAddRemoteStream")
                remoteMedia = playRTCMedia

                // Link the media stream to the view.
                playRTCMedia!!.setVideoRenderer(remoteView!!.videoRenderer)

            }

            override fun onDisconnectChannel(obj: PlayRTC?, disconnectReason: String?) {
                Log.i(LOG_TAG, "onDisconnectChannel")
                isChannelConnected = false
                if(UserInfo!!.getOtherUid()==" ");
                else {
                    mOhterFaceChatRef!!.child("faceChatChannel").setValue(" ")
                }
                // v2.2.5
                localView!!.bgClearColor()
                remoteView!!.bgClearColor()

                // Clean the channel_id TextView.
                val ChannelIdTextView = findViewById<View>(R.id.channel_id) as TextView
                ChannelIdTextView.text = null

                // Create PlayRTC instance again.
                // Because at the disconnect moment, the PlayRTC instance has removed.
                createPlayRTCInstance()
            }

            //            @Override
            //            public void onOtherDisconnectChannel(final PlayRTC obj, final String peerId, final String peerUserId) {
            //
            //                // v2.2.5
            //                remoteView.bgClearColor();
            //
            //
            //            }
        }
    }

    private fun createPlayRTCInstance() {
        try {
            Log.i(LOG_TAG, "createPlayRTCInstance")
            //function for sdk v2.2.0
            val config = createPlayRTCConfig()
            playrtc = PlayRTCFactory.createPlayRTC(config, playrtcObserver!!)


        } catch (e: UnsupportedPlatformVersionException) {
            e.printStackTrace()
        } catch (e: RequiredParameterMissingException) {
            e.printStackTrace()
        }

    }

    //function for sdk v2.2.0
    private fun createPlayRTCConfig(): PlayRTCConfig {
        val config = PlayRTCFactory.createConfig()

        // PlayRTC instance have to get the application context.
        config.setAndroidContext(getApplicationContext())

        // T Developers Project Key.
        config.setProjectId(T_DEVELOPERS_PROJECT_KEY)

        config.video.isEnable = true /* send video stream */

        /*
         * enum CameraType
         * - Front
         * - Back
         */
        config.video.setCameraType(CameraType.Front)

        /*
         * enum VideoCodec
         * - VP8
         * - VP9
         * - H264 : You can use the device must support.
         */
        config.video.setPreferCodec(VideoCodec.VP8)

        // default resolution 640x480
        config.video.setMaxFrameSize(640, 480)
        config.video.setMinFrameSize(640, 480)


        config.audio.isEnable = true   /* send audio stream */
        /* use PlayRTCAudioManager */
        config.audio.setAudioManagerEnable(true)

        /*
         * enum AudioCodec
         * - ISAC
         * - OPUS
         */
        config.audio.setPreferCodec(AudioCodec.OPUS)


        config.data.setEnable(true)    /* use datachannel stream */

        // Console logging setting
        config.log.console.setLevel(PlayRTCConfig.DEBUG)

        // File logging setting
        val logPath = File(Environment.getExternalStorageDirectory().absolutePath +
                "/Android/data/" + getPackageName() + "/files/log/")
        if (logPath.exists() == false) {
            logPath.mkdirs()
        }
        config.log.file.setLogPath(logPath.absolutePath)
        config.log.file.setLevel(PlayRTCConfig.DEBUG)

        return config
    }

    private fun createVideoView() {
        // Set the videoViewGroup which is contained local and remote video views.
        val myVideoViewGroup = findViewById<View>(R.id.video_view_group) as RelativeLayout

        if (localView != null) {
            return
        }

        // Give my screen size to child view.
        val myViewDimensions = Point()
        myViewDimensions.x = myVideoViewGroup.width
        myViewDimensions.y = myVideoViewGroup.height

        if (remoteView == null) {
            createRemoteVideoView(myViewDimensions, myVideoViewGroup)
        }

        if (localView == null) {
            createLocalVideoView(myViewDimensions, myVideoViewGroup)
        }
    }

    private fun createLocalVideoView(parentViewDimensions: Point, parentVideoViewGroup: RelativeLayout) {
        if (localView == null) {
            // Create the video size variable.
            val myVideoSize = Point()
            myVideoSize.x = (parentViewDimensions.x * 0.3).toInt()
            myVideoSize.y = (parentViewDimensions.y * 0.3).toInt()

            // Create the view parameter.
            val param = RelativeLayout.LayoutParams(myVideoSize.x, myVideoSize.y)
            param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            param.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            param.setMargins(30, 30, 30, 30)

            // Create the localViews.
            // new v2.2.6
            localView = PlayRTCVideoView(parentVideoViewGroup.context)
            // Set the z-order.
            localView!!.setZOrderMediaOverlay(true)
            // Background color
            // v2.2.5
            localView!!.setBgClearColor(225, 225, 225, 255)
            // Set the layout parameters.
            localView!!.layoutParams = param

            // new v2.2.6
            localView!!.initRenderer()

            localView!!.setMirror(true)

            // Add the view to the parentVideoViewGrop.
            parentVideoViewGroup.addView(localView)


        }
    }

    private fun createRemoteVideoView(parentViewDimensions: Point, parentVideoViewGroup: RelativeLayout) {
        if (remoteView == null) {
            // Create the video size variable.
            val myVideoSize = Point()
            myVideoSize.x = parentViewDimensions.x
            myVideoSize.y = parentViewDimensions.y

            // Create the view parameters.
            val param = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)

            // Create the remoteView.
            // new v2.2.6
            remoteView = PlayRTCVideoView(parentVideoViewGroup.context)
            // Background color
            // v2.2.5
            remoteView!!.setBgClearColor(200, 200, 200, 255)
            // Set the layout parameters.
            remoteView!!.layoutParams = param

            // new v2.2.6
            remoteView!!.initRenderer()

            // Add the view to the videoViewGroup.
            parentVideoViewGroup.addView(remoteView)
        }
    }


    private fun setOnClickEventListenerToButton() {
        // Add a create channel event listener.
        val createButton = findViewById<View>(R.id.create_button) as Button
        createButton.setOnClickListener {
            try {
                //                    JSONObject obj = new JSONObject();
                //                    JSONObject peer = new JSONObject();
                //
                //                    peer.put("uid", "userId");
                //                    obj.put("peer", peer);
                //
                //                    playrtc.createChannel(obj);
                playrtc!!.createChannel(JSONObject())
                isCloseActivity = false
            } catch (e: RequiredConfigMissingException) {
                e.printStackTrace()
            }
            //                catch (JSONException e){
            //                    e.printStackTrace();;
            //                }


        }

        // Add a connect channel event listener.
        val connectButton = findViewById<View>(R.id.connect_button) as Button
        connectButton.setOnClickListener {
            /*
            try {
                playrtc!!.connectChannel(channelId, JSONObject())
                isCloseActivity = false
            } catch (e: RequiredConfigMissingException) {
                e.printStackTrace()
            }
            */
        }

        // Add a exit channel event listener.
        val exitButton = findViewById<View>(R.id.exit_button) as Button
        exitButton.setOnClickListener {
            playrtc!!.deleteChannel()
            if(UserInfo!!.getOtherUid()==" ");
            else {
                mOhterFaceChatRef!!.child("faceChatChannel").setValue(" ")
                mMyFaceChatRef!!.child("faceChatChannel").setValue(" ")

            }
        }
    }

    private fun setToolbar() {
        toolbar = findViewById<View>(R.id.app_bar) as Toolbar
        setSupportActionBar(toolbar)
    }

    private fun setFragmentNavigationDrawer() {
        val drawerFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer) as NavigationDrawerFragment
        drawerFragment.setUp(R.id.fragment_navigation_drawer, findViewById<View>(R.id.drawer_layout) as DrawerLayout, toolbar!!)
    }

    private fun createCloseAlertDialog() {
        // Create the Alert Builder.
        val alertDialogBuilder = AlertDialog.Builder(this)

        // Set a Alert.
        alertDialogBuilder.setTitle(R.string.alert_title)
        alertDialogBuilder.setMessage(R.string.alert_message)
        alertDialogBuilder.setPositiveButton(R.string.alert_positive) { dialogInterface, id ->
            dialogInterface.dismiss()
            if (isChannelConnected == true) {
                isCloseActivity = true
                // null means my user id.
                playrtc!!.disconnectChannel(null)
            } else {
                isCloseActivity = true
                onBackPressed()
            }
        }
        alertDialogBuilder.setNegativeButton(R.string.alert_negative) { dialogInterface, id ->
            dialogInterface.dismiss()
            isCloseActivity = false
        }

        // Create the Alert.
        closeAlertDialog = alertDialogBuilder.create()
    }
    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener!!)
        Handler().postDelayed({
            mMyFaceChatRef!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    UserInfo=dataSnapshot.getValue(User::class.java)
                    if(UserInfo!!.getOtherUid() == " "){
                        Toast.makeText(this@RTCFaceActivity, "....", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        mOhterFaceChatRef=mConditionRef.child(UserInfo!!.getOtherUid())
                        mchildUserFaceChatRef=mMyFaceChatRef!!.child("faceChatChannel")

                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
            Handler().postDelayed({
                if(UserInfo!!.getOtherUid()==" " || channelId != " "){
                    Toast.makeText(this@RTCFaceActivity, "....", Toast.LENGTH_SHORT).show()
                }
                else{
                    mchildUserFaceChatRef!!.addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            channelId=dataSnapshot.getValue().toString()
                            Toast.makeText(this@RTCFaceActivity, channelId, Toast.LENGTH_SHORT).show()
                            if(channelId==" "){
                                if (isChannelConnected == true) {
                                    isCloseActivity = true
                                    // null means my user id.
                                    playrtc!!.disconnectChannel(null)
                                } else {
                                }
                            }
                            else if(isChannelConnected == true){
                                ;
                            }
                            else{
                                val alertDialogBuilder = AlertDialog.Builder(this@RTCFaceActivity)
                                // Set a Alert.
                                alertDialogBuilder.setTitle(R.string.alert_title)
                                alertDialogBuilder.setMessage("화상통화를 받으시겠습니까?")
                                alertDialogBuilder.setPositiveButton(R.string.alert_positive) { dialogInterface, id ->
                                    dialogInterface.dismiss()
                                    try {
                                        playrtc!!.connectChannel(channelId, JSONObject())
                                        isCloseActivity = false
                                    } catch (e: RequiredConfigMissingException) {
                                        e.printStackTrace()
                                    }
                                    mMyFaceChatRef!!.child("faceChatChannel").setValue(" ")
                                }
                                alertDialogBuilder.setNegativeButton(R.string.alert_negative) { dialogInterface, id ->
                                    dialogInterface.dismiss()
                                    mMyFaceChatRef!!.child("faceChatChannel").setValue(" ")
                                }
                                // Create the Alert.

                                closeAlertDialog = alertDialogBuilder.create()
                                closeAlertDialog!!.show()
                            }
                        }
                        override fun onCancelled(p0: DatabaseError?) {
                        }

                    })
                }
            }, 2000)
        }, 1000)

    }

    override fun onStop() {
        super.onStop()
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener!!)

        }
    }
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            UserUid=user.uid
            mMyFaceChatRef=mConditionRef.child(UserUid)

        } else {

        }
    }

    companion object {
        private val LOG_TAG = "RTCFaceActivity"
        private val TAG = "EmailPassword"

        val MANDATORY_PERMISSIONS = arrayOf("android.permission.INTERNET", "android.permission.CAMERA", "android.permission.RECORD_AUDIO", "android.permission.MODIFY_AUDIO_SETTINGS", "android.permission.ACCESS_NETWORK_STATE", "android.permission.CHANGE_WIFI_STATE", "android.permission.ACCESS_WIFI_STATE", "android.permission.READ_PHONE_STATE", "android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN", "android.permission.WRITE_EXTERNAL_STORAGE")
    }
}

