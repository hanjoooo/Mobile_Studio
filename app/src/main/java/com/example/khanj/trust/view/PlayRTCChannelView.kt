package com.example.khanj.trust.view

/**
 * Created by khanj on 2017-09-30.
 */

import java.util.ArrayList

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import com.sktelecom.playrtc.PlayRTC
import com.sktelecom.playrtc.connector.servicehelper.PlayRTCServiceHelperListener


import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.khanj.trust.Data.ChannelData
import com.example.khanj.trust.PlayRTCActivity
import com.example.khanj.trust.R
import com.example.khanj.trust.util.Utils

/*
 * 채널을 생성하거나 만들어진 채널 목록을 조회하여 채널에 입장하는 UI를 제공하는 RelativeLayout 확장 Class
 * 내부적으로 채널 목록 리스트의 채널 입장 버튼을 눌렀을 때 해당 채널 정보를 전달 받기 위한 IChannelListAdapter구현.
 * 채널 목록을 조회하기 위해 PlayRTC의 getChannelList메소드를 사용하며, 응답 결과를 받기 위해
 * PlayRTCServiceHelperListener 구현체가 필요하다.
 * 채널 생성/입장 버튼 선택 시 선택 정보를 전달하기 위해 PlayRTCChannelViewListener Interface를 정의.
 *
 * 채널 생성 버튼 선택 시
 * - void onClickCreateChannel(String channelName, String userId, String userName)
 * 채널 입장 버튼 선택 시
 * - void onClickConnectChannel(String channelId, String userId, String userName)
 *
 *
 * @see com.sktelecom.playrtc.connector.servicehelper.PlayRTCServiceHelperListener
 * @see ChannelListAdapter.IChannelListAdapter
 */
class PlayRTCChannelView : RelativeLayout, ChannelListAdapter.IChannelListAdapter {

    /**
     * 채널 생성 탭 Layout
     */
    private var tabCreate: LinearLayout? = null

    /**
     * 채널 입장 탭 Layout
     */
    private var tabConnect: LinearLayout? = null

    /**
     * 창 닫기 버튼
     */
    private var tabClose: Button? = null

    /**
     * 채널 생성 화면 영역
     */
    private var createContents: LinearLayout? = null

    /**
     * 채널 생성 화면 - 채널 이름 입력
     */
    private var txtCrChannelName: EditText? = null

    /**
     * 채널 생성 화면 - 사용자 아이디(Application 사용자) 입력
     */
    private var txtCrUserId: EditText? = null

    /**
     * 채널 생성 화면 - 사용자 이름 입력
     */
    private var txtCrUserName: EditText? = null

    /**
     * 채널 생성 화면 입력 컨트롤 초기화
     */
    private var btnCrClear: Button? = null

    /**
     * 채널 생성 버튼
     */
    private var btnCrCreate: Button? = null

    /**
     * 채널 생성 후 발급 빋은 채널 아이디 출력
     */
    private var labelChannelId: TextView? = null

    /**
     * 채널 입장 화면 영역
     */
    private var connectContents: LinearLayout? = null

    /**
     * 채널 입장 화면 - 채널 목록 출력 리스트
     */
    private var chList: ListView? = null

    /**
     * 채널 입장 화면 - 사용자 아이디(Application 사용자) 입력
     */
    private var txtCnUserId: EditText? = null

    /**
     * 채널 입장 화면 - 사용자 이름 입력
     */
    private var txtCnUserName: EditText? = null

    /**
     * 채널 입장 화면 입력 컨트롤 초기화
     */
    private var btnCnClear: Button? = null

    /**
     * 채널 목록 리스트 조회 버튼
     */
    private var btnCnList: Button? = null

    private var playRTC: PlayRTC? = null

    private var listAdapter: ChannelListAdapter? = null

    // 채널 생성/입장 버튼 선택 시 선택 정보를 전달하기 위한 PlayRTCChannelViewListener Interface 구현 개체.
    private var listener: PlayRTCChannelViewListener? = null

    private var channelId = ""

    /**
     * 채널 생성/입장 버튼 선택 시 선택 정보를 전달하기 위해 PlayRTCChannelViewListener Interface를 정의.
     * <pre>
     * 채널 생성 버튼 선택 시
     * - void onClickCreateChannel(String channelName, String userId, String userName)
     * 채널 입장 버튼 선택 시
     * - void onClickConnectChannel(String channelId, String userId, String userName)
    </pre> *
     */
    interface PlayRTCChannelViewListener {
        /**
         * 채널 생성 버튼 선택 시 채널 생성 관련 정보를 전달
         *
         * @param channelName String, 생성할 채널의 별칭을 지정
         * @param userId      String, 채널을 생성하는 사용자의 Application에서 사용하는 아이디 지정
         * @param userName    userName, 채널을 생성하는 사용자의 이름을 지정
         */
        fun onClickCreateChannel(channelName: String, userId: String, userName: String)

        /**
         * 채널 입장 버튼 선택 시 채널 생성 관련 정보를 전달
         *
         * @param channelId String, 입장 할 채널의 아이디를 지정
         * @param userId    String, 채널에 입장하는 사용자의 Application에서 사용하는 아이디 지정
         * @param userName, 채널에 입장하는 사용자의 이름을 지정
         */
        fun onClickConnectChannel(channelId: String?, userId: String, userName: String)
    }

    /**
     * 생성자
     *
     * @param context Context
     */
    constructor(context: Context) : super(context) {}

    /**
     * 생성자
     *
     * @param context Context
     * @param attrs   AttributeSet
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    /**
     * 생성자
     *
     * @param context  Context
     * @param attrs    AttributeSet
     * @param defStyle int
     */
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    /**
     * PlayRTCChannelView를 초기화한다.
     *
     * @param activity PlayRTCActivity
     * @param playRTC  PlayRTC
     * @param l        PlayRTCChannelViewListener,  채널 생성/입장 버튼 선택 시 선택 정보를 전달하기 위한 PlayRTCChannelViewListener Interface 구현 개체.
     * @see com.playrtc.sample.PlayRTCActivity
     *
     * @see com.playrtc.sample.view.PlayRTCChannelView.PlayRTCChannelViewListener
     *
     * @see ChannelListAdapter
     */
    fun init(activity: PlayRTCActivity, playRTC: PlayRTC, l: PlayRTCChannelViewListener) {
        this.playRTC = playRTC
        this.listener = l
        this.listAdapter = ChannelListAdapter(activity, this)
        initLayout()
    }

    /**
     * IChannelListAdapter Interface<br></br>
     * 채널 목록 리스트의 채널 입장 버튼을 눌렀을 때 해당 채널 정보를 전달 받기 위한 IChannelListAdapter 구현
     *
     * @param data ChannelData, 채널 정보
     * @see ChannelData
     */
    override fun onSelectListItem(data: ChannelData) {
        Log.d("LIST", "onSelectListItem channelId=" + data.channelId!!)
        if (TextUtils.isEmpty(data.channelId) == false) {
            val userId = this.txtCnUserId!!.text.toString()
            val userName = this.txtCnUserName!!.text.toString()
            // 채널 입장 버튼 선택 시 채널 생성 관련 정보를 전달
            this.listener!!.onClickConnectChannel(data.channelId, userId, userName)
        }
    }

    /**
     * PlayRTCChannelView를 화면에 보여준다.
     *
     * @param delayed long, 화면에 보여주는 Fade-in 시간을 지정. 0이면 바로 보여준다. msec 기준
     */
    fun show(delayed: Long) {

        val crUserId = txtCrUserId!!.text.toString()
        if (TextUtils.isEmpty(crUserId)) {
            val userId = Utils.randomServiceMailId
            txtCrUserId!!.setText(userId)
            txtCrChannelName!!.setText("Android::" + userId + "의 채널입니다.")
        }
        val cnUserId = txtCnUserId!!.text.toString()
        if (TextUtils.isEmpty(cnUserId)) {
            txtCnUserId!!.setText(Utils.randomServiceMailId)
        }
        val animation = AnimationUtils.loadAnimation(this.context, R.anim.channel_show)
        animation.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationEnd(anim: Animation) {}

            override fun onAnimationRepeat(anim: Animation) {}

            override fun onAnimationStart(anim: Animation) {
                this@PlayRTCChannelView.visibility = View.VISIBLE
                this@PlayRTCChannelView.bringToFront()
            }
        })
        if (delayed.equals(0)) {
            this.startAnimation(animation)
        } else {
            this.postDelayed({ startAnimation(animation) }, delayed)
        }
    }

    /**
     * PlayRTCChannelView를 화면에서 숨긴다.
     *
     * @param delayed long, 화면을 순기는 Fade-out 시간을 지정. 0이면 바로 숨긴다. msec 기준
     */
    fun hide(delayed: Long) {
        val animation = AnimationUtils.loadAnimation(this.context, R.anim.channel_hide)
        animation.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationEnd(anim: Animation) {
                this@PlayRTCChannelView.visibility = View.INVISIBLE
            }

            override fun onAnimationRepeat(anim: Animation) {}

            override fun onAnimationStart(anim: Animation) {}

        })
        if (delayed.equals(0)) {
            this.startAnimation(animation)
        } else {
            this.postDelayed({ startAnimation(animation) }, delayed)
        }
    }

    /**
     * 채널아이디를 반환한다. <br></br>
     * 채널 생성 또는 채널입장 시 획득한 채널의 아이디
     *
     * @return String
     */
    fun getChannelId(): String {
        return this.channelId
    }

    /**
     * 외부에서 채널아이디를 전달 받아 채널 생성 탭의 출력영역에 표시한다.<br></br>
     * PlayRTC의 createChannel의 결과로 채널아이디를 발급받아 아이디가 전달 된다.
     *
     * @param channelId String, 채널아이디
     */
    fun setChannelId(channelId: String) {
        this.channelId = channelId
        this.labelChannelId!!.post { labelChannelId!!.text = channelId }
    }

    /**
     * 외부에서 전달받은 버튼에 PlayRTCChannelView에 show/hide 이벤트를 지정한다.
     *
     * @param btn Button
     */
    fun setTargetButton(btn: Button) {
        val refThis = this
        btn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (refThis.isShown) {
                    refThis.hide(0)
                } else {
                    refThis.showChannelList()
                    refThis.show(0)
                }
            }
        })
    }

    /**
     * PlayRTC의 getChannelList메소드를 호출하여 채널 목록을 조회하고 리스트에 출력한다.<br></br>
     * 채널 목록을 전달 받기 위해 PlayRTCServiceHelperListener 구현 개체가 필요
     *
     * @see com.sktelecom.playrtc.connector.servicehelper.PlayRTCServiceHelperListener
     */
    fun showChannelList() {
        //채널 목록을 전달 받기 위해 PlayRTCServiceHelperListener 구현
        playRTC!!.getChannelList(object : PlayRTCServiceHelperListener {

            // 서비스 조회 결과를 전달 받는다.
            // 서비스 응답 시 오류 여부를 검사해야 한다.
            override fun onServiceHelperResponse(code: Int, statusMsg: String, returnParam: Any, oData: JSONObject) {
                try {
                    // 서비스 오류 여부 검사
                    if (oData.has("error")) {
                        val error = oData.getJSONObject("error")
                        val errCode = error.getString("code")
                        val errMsg = error.getString("message")
                        Log.d(LOG_TAG, "getChannelList httpCode[$code] err[$errCode] $errMsg")
                        return
                    } else if (code != 200) {
                        Log.d(LOG_TAG, "getChannelList error httpCode[$code] err[$statusMsg]")
                    } else {
                        // 채널 데이터 리스트를 생성하여 ChannelListAdapter에 전달한다.
                        val channels = oData.getJSONArray("channels")
                        val cnt = channels.length()
                        val list = ArrayList<ChannelData>()
                        for (i in 0..cnt - 1) {
                            val channel = channels.getJSONObject(i)
                            val channelId = channel.getString("channelId")
                            val channelName = if (channel.has("channelName")) channel.getString("channelName") else ""
                            val item = ChannelData()
                            item.channelId = channelId
                            item.channelName = channelName
                            list.add(item)
                        }
                        // 리스트 데이터 전달
                        listAdapter!!.setListItems(list)
                        // 리스트 갱신
                        listAdapter!!.notifyDataSetChanged()
                    }
                } catch (e: JSONException) {
                    Log.d(LOG_TAG, "getChannelList httpCode[" + code + "] err[" + statusMsg + "] " + e.localizedMessage)
                }

            }

            // 통신 오류 발생
            override fun onServiceHelperFail(code: Int, statusMsg: String, returnParam: Any) {
                Log.d(LOG_TAG, "getChannelList httpCode[$code] err[$statusMsg]")
            }

        })

    }


    private fun initLayout() {
        // PlayRTCChannelView 자체의 클릭 이벤트를 걸고 onClick를 구현하지 않는다. 클릭 동작 방지를 위해
        this.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {}
        })
        // 채널 생성 탭 버튼
        this.tabCreate = this.findViewById<View>(R.id.tab_btn_creator) as LinearLayout
        this.tabCreate!!.setOnTouchListener(OnTouchListener { view, event ->
            if (MotionEvent.ACTION_UP == event.action) {
                // 탭 전환
                // 0 : 채널 생성 탭
                setActivePanel(0)
                return@OnTouchListener true
            }
            true
        })
        // 채널 입장 탭 버튼
        this.tabConnect = this.findViewById<View>(R.id.tab_btn_connetor) as LinearLayout
        this.tabConnect!!.setOnTouchListener(OnTouchListener { view, event ->
            if (MotionEvent.ACTION_UP == event.action) {
                // 탭 전환
                // 0 : 채널 입장 탭
                setActivePanel(1)
                return@OnTouchListener true
            }
            true
        })

        // PlayRTCChannelView 닫기 버튼
        this.tabClose = this.findViewById<View>(R.id.btn_popup_close) as Button
        this.tabClose!!.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View) {
                this@PlayRTCChannelView.hide(0)
            }
        })
        // 채널 생성 탭 화면 영역
        this.createContents = this.findViewById<View>(R.id.tab_creator_contents) as LinearLayout
        // 채널 이름을 입력받는다.
        this.txtCrChannelName = this.findViewById<View>(R.id.txt_channel_name) as EditText
        // 사용자 아이디를 입력받는다.
        this.txtCrUserId = this.findViewById<View>(R.id.txt_cruser_id) as EditText
        // 사용자 이름을 입력 받는다.
        this.txtCrUserName = this.findViewById<View>(R.id.txt_cruser_name) as EditText
        // 입력 컨트롤 초기화 버튼
        this.btnCrClear = this.findViewById<View>(R.id.btn_creator_clear) as Button
        this.btnCrClear!!.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View) {
                this@PlayRTCChannelView.txtCrChannelName!!.setText("")
                this@PlayRTCChannelView.txtCrUserId!!.setText("")
                this@PlayRTCChannelView.txtCrUserName!!.setText("")
                this@PlayRTCChannelView.labelChannelId!!.text = "CHANNEL-ID"
                this@PlayRTCChannelView.channelId = ""
            }
        })

        // 채널 생성 버튼
        this.btnCrCreate = this.findViewById<View>(R.id.btn_create_channel) as Button
        this.btnCrCreate!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val channelName = txtCrChannelName!!.text.toString()
                val userId = txtCrUserId!!.text.toString()
                val userName = txtCrUserName!!.text.toString()
                if (this@PlayRTCChannelView.listener != null) {
                    this@PlayRTCChannelView.listener!!.onClickCreateChannel(channelName, userId, userName)
                }
            }
        })

        // 채널 입장 탭 화면 영역
        this.connectContents = this.findViewById<View>(R.id.tab_connector_contents) as LinearLayout
        // 사용자 아이디를 입력받는다.
        this.txtCnUserId = this.findViewById<View>(R.id.txt_cnuser_id) as EditText
        // 사용자 이름을 입력받는다.
        this.txtCnUserName = this.findViewById<View>(R.id.txt_cnuser_name) as EditText
        // 입력 컨트롤 초기화 버튼
        this.btnCnClear = this.findViewById<View>(R.id.btn_connect_clear) as Button
        this.btnCnClear!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                this@PlayRTCChannelView.txtCnUserId!!.setText("")
                this@PlayRTCChannelView.txtCnUserName!!.setText("")
            }
        })
        // 채널 목록 조회 버튼
        this.btnCnList = this.findViewById<View>(R.id.btn_connect_channel_list) as Button
        this.btnCnList!!.setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View) {
                // 채널 목록을 조회한다.
                this@PlayRTCChannelView.showChannelList()

            }
        })

        // 채널 목록 출력을 위한 ListView
        this.chList = this.findViewById<View>(R.id.channel_list) as ListView
        this.chList!!.adapter = this.listAdapter

        // 채널 아이디를 출력
        this.labelChannelId = this.findViewById<View>(R.id.txt_create_channel_id) as TextView
    }

    /**
     * 탭 전환
     *
     * @param index int, 0: 채널 생성 탭  화성화, 1: 채널 입장 탭 활성화
     */
    private fun setActivePanel(index: Int) {
        if (index == 0) {
            this.tabCreate!!.setBackgroundResource(R.drawable.tab_btn_active)
            this.tabConnect!!.setBackgroundResource(R.drawable.tab_btn_normal)

            this.createContents!!.visibility = View.VISIBLE
            this.connectContents!!.visibility = View.INVISIBLE
        } else {
            this.tabConnect!!.setBackgroundResource(R.drawable.tab_btn_active)
            this.tabCreate!!.setBackgroundResource(R.drawable.tab_btn_normal)

            connectContents!!.visibility = View.VISIBLE
            createContents!!.visibility = View.INVISIBLE
            // 채널 입장 탭 활성화 시 채널 목록 갱신
            showChannelList()
        }
    }

    companion object {
        private val LOG_TAG = "CHANNEL_INFO"
    }
}
