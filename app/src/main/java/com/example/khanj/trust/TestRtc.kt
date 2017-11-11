package com.playrtc.simplechat


import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Point
import android.os.Bundle
import android.os.Environment
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView

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
import android.support.v7.app.AppCompatActivity
import com.example.khanj.trust.NavigationDrawerFragment
import com.example.khanj.trust.R

class TestRtc : AppCompatActivity()  {
    // Please change this key for your own project.
    private val T_DEVELOPERS_PROJECT_KEY = "60ba608a-e228-4530-8711-fa38004719c1"

    private var toolbar: Toolbar? = null
    private var closeAlertDialog: AlertDialog? = null

    private var playrtc: PlayRTC? = null
    private var playrtcObserver: PlayRTCObserver? = null

    private var isCloseActivity = false
    private var isChannelConnected = false
    private var localView: PlayRTCVideoView? = null
    private var remoteView: PlayRTCVideoView? = null
    private var localMedia: PlayRTCMedia? = null
    private var remoteMedia: PlayRTCMedia? = null
    private var channelId: String? = null

    private val videoViewGroup: RelativeLayout? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testrtc)

        // Application permission 23
        if (android.os.Build.VERSION.SDK_INT >= 23) {

            checkPermission(MANDATORY_PERMISSIONS)
        }

        createPlayRTCObserverInstance()

        // use sdk v2.2.0
        createPlayRTCInstance()

        setToolbar()

        setFragmentNavigationDrawer()

        setOnClickEventListenerToButton()

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
        android.os.Process.killProcess(android.os.Process.myPid())
        super.onDestroy()
    }


    override fun onBackPressed() {
        if (isCloseActivity) {
            super.onBackPressed()
        } else {
            createCloseAlertDialog()
            closeAlertDialog!!.show()
        }
    }

    private fun createPlayRTCObserverInstance() {
        playrtcObserver = object : PlayRTCObserver() {

            override fun onConnectChannel(obj: PlayRTC, channelId: String, channelCreateReason: String, channelType: String) {
                Log.i(LOG_TAG, "onConnectChannel")
                isChannelConnected = true

                // Fill the channelId to the channel_id TextView.
                val channelIdTextView = findViewById<View>(R.id.channel_id) as TextView
                channelIdTextView.setText(channelId)
            }


            override fun onAddLocalStream(obj: PlayRTC, playRTCMedia: PlayRTCMedia) {
                Log.i(LOG_TAG, "onAddLocalStream")
                localMedia = playRTCMedia

                // Link the media stream to the view.
                playRTCMedia.setVideoRenderer(localView!!.getVideoRenderer())
            }


            override fun onAddRemoteStream(obj: PlayRTC, peerId: String, peerUserId: String, playRTCMedia: PlayRTCMedia) {

                Log.i(LOG_TAG, "onAddRemoteStream")
                remoteMedia = playRTCMedia

                // Link the media stream to the view.
                playRTCMedia.setVideoRenderer(remoteView!!.getVideoRenderer())

            }


            override fun onDisconnectChannel(obj: PlayRTC, disconnectReason: String) {
                Log.i(LOG_TAG, "onDisconnectChannel")
                isChannelConnected = false

                // v2.2.5
                localView!!.bgClearColor()
                remoteView!!.bgClearColor()

                // Clean the channel_id TextView.
                val ChannelIdTextView = findViewById<View>(R.id.channel_id) as TextView
                ChannelIdTextView.setText(null)

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
            playrtc = PlayRTCFactory.createPlayRTC(config, playrtcObserver)


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

        config.video.setEnable(true) /* send video stream */

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




        config.audio.setEnable(true)   /* send audio stream */
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
        val logPath = File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" + getPackageName() + "/files/log/")
        if (logPath.exists() === false) {
            logPath.mkdirs()
        }
        config.log.file.setLogPath(logPath.getAbsolutePath())
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
        myViewDimensions.x = myVideoViewGroup.getWidth()
        myViewDimensions.y = myVideoViewGroup.getHeight()

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
            myVideoSize.x = parentViewDimensions.x * 0.3.toInt()
            myVideoSize.y = parentViewDimensions.y * 0.3.toInt()

            // Create the view parameter.
            val param = RelativeLayout.LayoutParams(myVideoSize.x, myVideoSize.y)
            param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            param.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            param.setMargins(30, 30, 30, 30)

            // Create the localViews.
            // new v2.2.6
            localView = PlayRTCVideoView(parentVideoViewGroup.getContext())
            // Set the z-order.
            localView!!.setZOrderMediaOverlay(true)
            // Background color
            // v2.2.5
            localView!!.setBgClearColor(225, 225, 225, 255)
            // Set the layout parameters.
            localView!!.setLayoutParams(param)

            // new v2.2.6
            localView!!.initRenderer()

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

            // Create the.
            // new v2.2.6
            remoteView = PlayRTCVideoView(parentVideoViewGroup.getContext())
            // Background color
            // v2.2.5
            remoteView!!.setBgClearColor(200, 200, 200, 255)
            // Set the layout parameters.
            remoteView!!.setLayoutParams(param)

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
            fun onClick(v: View) {
                try {
                    //                    JSONObject obj = new JSONObject();
                    //                    JSONObject peer = new JSONObject();
                    //
                    //                    peer.put("uid", "userId");
                    //                    obj.put("peer", peer);
                    //
                    //                    playrtc.createChannel(obj);
                    playrtc!!.createChannel(JSONObject())
                } catch (e: RequiredConfigMissingException) {
                    e.printStackTrace()
                }

                //                catch (JSONException e){
                //                    e.printStackTrace();;
                //                }
            }
        }

        // Add a connect channel event listener.
        val connectButton = findViewById<View>(R.id.connect_button) as Button
        connectButton.setOnClickListener{
            try {
                val ChannelIdInput = findViewById<View>(R.id.connect_channel_id) as TextView
                channelId = ChannelIdInput.getText().toString()
                playrtc!!.connectChannel(channelId, JSONObject())
            } catch (e: RequiredConfigMissingException) {
                e.printStackTrace()
            }

        }

        // Add a exit channel event listener.
        val exitButton = findViewById<View>(R.id.exit_button) as Button
        exitButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View) {
                playrtc!!.deleteChannel()
            }
        })
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
        alertDialogBuilder.setPositiveButton(R.string.alert_positive, object : DialogInterface.OnClickListener{
            override fun onClick(dialogInterface: DialogInterface, id: Int) {
                dialogInterface.dismiss()
                if (isChannelConnected == true) {
                    isCloseActivity = false

                    // null means my user id.
                    playrtc!!.disconnectChannel(null)
                } else {
                    isCloseActivity = true
                    onBackPressed()
                }
            }
        })
        alertDialogBuilder.setNegativeButton(R.string.alert_negative, object : DialogInterface.OnClickListener {
            override fun onClick(dialogInterface: DialogInterface, id: Int) {
                dialogInterface.dismiss()
                isCloseActivity = false
            }
        })

        // Create the Alert.
        closeAlertDialog = alertDialogBuilder.create()
    }

    companion object {
        private val LOG_TAG = "TestRtc"


        val MANDATORY_PERMISSIONS = arrayOf("android.permission.INTERNET", "android.permission.CAMERA", "android.permission.RECORD_AUDIO", "android.permission.MODIFY_AUDIO_SETTINGS", "android.permission.ACCESS_NETWORK_STATE", "android.permission.CHANGE_WIFI_STATE", "android.permission.ACCESS_WIFI_STATE", "android.permission.READ_PHONE_STATE", "android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN", "android.permission.WRITE_EXTERNAL_STORAGE")
    }
}
