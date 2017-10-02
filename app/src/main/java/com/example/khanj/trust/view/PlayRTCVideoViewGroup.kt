package com.example.khanj.trust.view

/*
 * Created by khanj on 2017-09-30.
 */

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import com.example.khanj.trust.R


import com.sktelecom.playrtc.util.ui.PlayRTCVideoView

class PlayRTCVideoViewGroup : RelativeLayout {

    /*
     * 로컬 영상 출력 뷰
     */
    /*
     * Local 영상 뷰를 반환한다.
     * @return LocalVideoView
     */
    var localView: LocalVideoView? = null
        private set

    /*
     * 상대방 영상 출력 뷰
     */
    /*
     * Remote 영상 뷰를 반환한다.
     * @return RemoteVideoView
     */
    var remoteView: RemoteVideoView? = null
        private set

    /*
     * Layouy XML에 기술한 영상 출력을 위한 관련 뷰를 초기화 설정을 했는지 여부
     * Activity의 onWindowFocusChanged에서 화면에 보여 질 때(사이즈 확인 가능 시점) 호출(최초 1번) 한다.
     */
    /*
     * Layouy XML에 기술한 영상 출력을 위한 관련 뷰를 초기화 설정을 했는지 여부를 반환한다.
     * Activity의 onWindowFocusChanged에서 화면에 보여 질 때 최초 1번 initVideoView()를 호출하기 위해서 임.
     * @return boolean
     *
     */
    var isInitVideoView = false
        private set

    private var screenDimensions: Point? = null

    private var localViewSize = RTCViewSizeType.Full

    enum class RTCViewSizeType {
        Full,
        Small
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}


    /*
     * 영상 출력뷰의 내부 렌더링 객체를 해제한다.
     * new v2.2.5
     */
    fun releaseView() {
        if (localView != null) {
            localView!!.release()
        }
        if (remoteView != null) {
            remoteView!!.release()
        }
    }

    /*
     * Layouy XML을 사용하지 않고 직접 영상 출력을 위한 PlayRTCVideoView를 사이즈를 계산하여 생성한다.
     * PlayRTCVideoViewGroup 크기를 높이 기준으로 4(폭):3(높이)으로 재 조정하고,
     * Remote 뷰는 PlayRTCVideoViewGroup 크기에 맞게 생성하고
     * Local 뷰는 Remote 뷰 크기의 30%로 좌상단에 생성한다.
     * createVideoView는 Activity의 onWindowFocusChanged에서 화면에 보여 질 때(사이즈 확인 가능 시점) 호출(최초 1번) 한다.
     */
    fun createVideoView() {
        // 이미 뷰를 생성 했는지 체크
        if (isCreatedVideoView == true) {
            return
        }
        // PlayRTCVideoView의 부모 ViewGroup의 사이즈 확인
        screenDimensions = Point()
        val height = this.height

        // ViewGroup의 사이즈 재조정, 높이 기준으로 4(폭):3(높이)으로 재 조정
        // 4:3 = width:height ,  width = ( 4 * height) / 3
        val width = 4.0f * height / 3.0f

        val param = this.layoutParams as RelativeLayout.LayoutParams
        param.width = width.toInt()
        param.height = height
        this.layoutParams = param

        screenDimensions!!.x = param.width
        screenDimensions!!.y = param.height

        // big, remote, 4(폭):3(높이)
        createRemoteVideoView(screenDimensions!!)
        // small. local , 4(폭):3(높이) 30%
        createLocalVideoView(screenDimensions!!)
    }

    /*
     * Layouy XML에 기술한 영상 출력을 위한 관련 뷰를 설정한다.
     * PlayRTCVideoViewGroup 크기를 높이 기준으로 4(폭):3(높이)으로 재 조정하고,
     * Remote(RemoteVideoView) 뷰는 PlayRTCVideoViewGroup 크기에 맞게 생성하고
     * Local(LocalVideoView) 뷰는 Remote 뷰 크기의 30%로 좌상단에 생성한다.
     * initVideoView는 Activity의 onWindowFocusChanged에서 화면에 보여 질 때(사이즈 확인 가능 시점) 호출(최초 1번) 한다.
     * new v2.2.5
     * @see com.playrtc.sample.view.LocalVideoView
     * @see com.playrtc.sample.view.RemoteVideoView
     */
    fun initVideoView() {

        // 이미 뷰를 초기화 했는지 체크
        if (isInitVideoView == true) {
            return

        }

        isInitVideoView = true

        // PlayRTCVideoView의 부모 ViewGroup의 사이즈 확인
        screenDimensions = Point()
        val height = this.height

        // ViewGroup의 사이즈 재조정, 높이 기준으로 4(폭):3(높이)으로 재 조정
        // 4:3 = width:height ,  width = ( 4 * height) / 3
        val width = 4.0f * height / 3.0f

        val param = this.layoutParams as RelativeLayout.LayoutParams
        param.width = width.toInt()
        param.height = height.toInt()
        this.layoutParams = param

        screenDimensions!!.x = param.width
        screenDimensions!!.y = param.height


        initRemoteVideo()
        initLocalVideo()
    }

    /*
     * 영상 뷰를 생성했는지 여부를 반환한다.
     * 영상 뷰 샹성 여부를 체크하는 이유는 Sample App에서 영상뷰 생성 시 사이즈 지정을 위해
     * Activity의 onWindowFocusChanged에서 화면에 보여 질 때 최초 1번 createVideoView()를 호출하기 위해서 임.
     * @return boolean
     *
     */
    val isCreatedVideoView: Boolean
        get() = if (localView == null && remoteView == null) {
            false
        } else true


    fun resizeLocalVideoView(size: RTCViewSizeType) {

        if (size == localViewSize) {
            return
        }
        localViewSize = size

        val lp: RelativeLayout.LayoutParams
        if (size == RTCViewSizeType.Full) {
            lp = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            lp.setMargins(0, 0, 0, 0)
        } else {
            // 세로 모드임, 해상도는 가로 기준
            val smallWidth = (screenDimensions!!.x * 0.3).toInt()
            val samllHeight = (screenDimensions!!.y * 0.3).toInt()

            lp = RelativeLayout.LayoutParams(smallWidth, samllHeight)
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP)

            lp.setMargins(30, 30, 30, 30)
        }
        localView!!.layoutParams = lp
        //localView.requestLayout();
    }

    /*
     * Layouy XML을 사용하지 않고 직접 영상 출력을 위한 PlayRTCVideoView를 사이즈를 계산하여 생성한다.
     * Local 뷰는 PlayRTCVideoViewGroup 크기의 30%로 생성하여 좌상단에 배치한다.
     * @param screenDimensions Point, PlayRTCVideoViewGroup 크기
     */
    private fun createLocalVideoView(screenDimensions: Point) {
        // 자신의 스트림 출력을 위한 PlayRTCVideoView 객체 동적 생성
        if (localView == null) {
            // 부모 View의 30% 비율로 크기를 지정한다
            val displaySize = Point()
            displaySize.x = (screenDimensions.x * 0.3).toInt()
            displaySize.y = (screenDimensions.y * 0.3).toInt()

            /*
			 * PlayRTCVideoView 생성자
			 * @param context Context
			 * @param dimensions Point
			 * @param mirror boolean, 영상 출력을 거울 모드로 할지 여부를 지정한다.<br>
			 *        true로 지정하면 거울로 보는것처럼 오른쪽이 화면의 오른쪽으로 출력된다.<br>
			 *        주로 로컬 영상의 경우 거울 모드로 지정한다.
			 */
            //localView = new PlayRTCVideoView(this.getContext(), displaySize, true);
            localView = LocalVideoView(this.context)
            localView!!.isMirror = false
            /*
			 * 화면 배경색을 지정한다. R,G,B,A 0 ~ 255 정수
			 * 영상 스트림이 출력 되기 전, bgClearColor() 호출 시 지정한 색으로 배경을 칠한다.
			 * v2.2.5 추가
			 */
            localView!!.setBgClearColor(225, 225, 225, 255)
            localView!!.hide(0)
            val param = RelativeLayout.LayoutParams(displaySize.x, displaySize.y)
            param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            param.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            param.setMargins(30, 30, 30, 30)

            localView!!.layoutParams = param
            // 부모뷰에 PlayRTCVideoView 추가
            this.addView(localView)
            localView!!.setZOrderOnTop(true)
            //localView.setVisibility(View.INVISIBLE);
            localView!!.setVideoFrameObserver(object : PlayRTCVideoView.VideoRendererObserver {
                override fun onFrameResolutionChanged(view: PlayRTCVideoView, videoWidth: Int, videoHeight: Int, rotationDegree: Int) {
                    Log.i(LOG_TAG, "Local FrameResolution videoWidth[$videoWidth] videoHeight[$videoHeight] rotationDegree[$rotationDegree]")
                }

                override fun onFirstFrameRendered() {
                    // TODO Auto-generated method stub

                }
            })

            // new v2.2.6
            // Video Renderer 초기화
            localView!!.initRenderer()
        }
    }

    /*
     * Layouy XML을 사용하지 않고 직접 영상 출력을 위한 PlayRTCVideoView를 사이즈를 계산하여 생성한다.
     * Remote 뷰는 PlayRTCVideoViewGroup 크기에 맞게 생성하여 배치한다.
     * @param screenDimensions Point, PlayRTCVideoViewGroup 크기
     */
    private fun createRemoteVideoView(screenDimensions: Point) {
        // 상대방 스트림 출력을 위한 PlayRTCVideoView 객체 동적 생성
        if (remoteView == null) {
            val displaySize = Point()
            displaySize.x = screenDimensions.x
            displaySize.y = screenDimensions.y

            remoteView = RemoteVideoView(this.context)
            /*
             * 거울 모드를 지정한다.
             * @param mirror boolean, 영상 출력을 거울 모드로 지정한다.<br>
             *        true로 지정하면 거울로 보는것처럼 오른쪽이 화면의 오른쪽으로 출력된다.<br>
             *        주로 로컬 영상(전방 카메라 사용)의 경우 거울 모드로 지정한다.
             */
            remoteView!!.isMirror = false
            /*
			 * 화면 배경색을 지정한다. R,G,B,A 0 ~ 255 정수
			 * 영상 스트림이 출력 되기 전, bgClearColor() 호출 시 지정한 색으로 배경을 칠한다.
			 * v2.2.5 추가
			 */
            remoteView!!.setBgClearColor(200, 200, 200, 255)
            val param = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
            remoteView!!.layoutParams = param
            // 부모뷰에 PlayRTCVideoView 추가
            this.addView(remoteView)
            remoteView!!.setVideoFrameObserver(object : PlayRTCVideoView.VideoRendererObserver {
                override fun onFrameResolutionChanged(view: PlayRTCVideoView, videoWidth: Int, videoHeight: Int, rotationDegree: Int) {
                    Log.i(LOG_TAG, "Remote FrameResolution videoWidth[$videoWidth] videoHeight[$videoHeight] rotationDegree[$rotationDegree]")
                }

                override fun onFirstFrameRendered() {
                    // TODO Auto-generated method stub


                }
            })

            // new v2.2.6
            // Video Renderer 초기화
            remoteView!!.initRenderer()
        }
    }

    /*
     * Layouy XML에 기술한 Local 영상 뷰를 설정한다.
     * v2.2.6
     */
    private fun initLocalVideo() {


        // 부모 View의 30% 비율로 크기를 지정한다
        val width = (screenDimensions!!.x * 0.3).toInt()
        val height = (screenDimensions!!.y * 0.3).toInt()

        localView = findViewById<View>(R.id.local_video) as LocalVideoView


        /*
         * 거울 모드를 지정한다.
         * @param mirror boolean, 영상 출력을 거울 모드로 지정한다.
         *        true로 지정하면 거울로 보는것처럼 오른쪽이 화면의 오른쪽으로 출력된다.
         *        주로 로컬 영상(전방 카메라 사용)의 경우 거울 모드로 지정한다.
         */

        localView!!.isMirror = false
        localView!!.setBgClearColor(200, 200, 200, 255)
        localView!!.setVideoFrameObserver(object : PlayRTCVideoView.VideoRendererObserver {
            override fun onFrameResolutionChanged(view: PlayRTCVideoView, videoWidth: Int, videoHeight: Int, rotationDegree: Int) {
                Log.i(LOG_TAG, "Local FrameResolution videoWidth[$videoWidth] videoHeight[$videoHeight] rotationDegree[$rotationDegree]")
            }

            override fun onFirstFrameRendered() {
                Log.i(LOG_TAG, "Local onFirstFrameRendered....")
            }
        })

        // v2.2.6
        localView!!.initRenderer()
    }

    /*
     * Layouy XML에 기술한 Remote 영상 뷰를 설정한다.
     * v2.2.6
     */
    private fun initRemoteVideo() {

        remoteView = findViewById<View>(R.id.remote_video) as RemoteVideoView
        /*
         * 거울 모드를 지정한다.
         * @param mirror boolean, 영상 출력을 거울 모드로 지정한다.<br>
         *        true로 지정하면 거울로 보는것처럼 오른쪽이 화면의 오른쪽으로 출력된다.<br>
         *        주로 로컬 영상(전방 카메라 사용)의 경우 거울 모드로 지정한다.
         */
        remoteView!!.isMirror = false
        remoteView!!.setBgClearColor(127, 127, 127, 255)
        remoteView!!.setVideoFrameObserver(object : PlayRTCVideoView.VideoRendererObserver {
            override fun onFrameResolutionChanged(view: PlayRTCVideoView, videoWidth: Int, videoHeight: Int, rotationDegree: Int) {
                Log.i(LOG_TAG, "Remote FrameResolution videoWidth[$videoWidth] videoHeight[$videoHeight] rotationDegree[$rotationDegree]")

            }

            override fun onFirstFrameRendered() {
                Log.i(LOG_TAG, "Remote onFirstFrameRendered....")
                localView!!.post { resizeLocalVideoView(RTCViewSizeType.Small) }
            }

        })

        // v2.2.6
        remoteView!!.initRenderer()
    }

    companion object {

        private val LOG_TAG = "VIDEO-VIEW"
    }
}
