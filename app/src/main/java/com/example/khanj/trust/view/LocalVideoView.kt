package com.example.khanj.trust.view

/*
 * Created by khanj on 2017-09-30.
 */

import com.sktelecom.playrtc.util.ui.PlayRTCVideoView

import android.content.Context
import android.util.AttributeSet

/*
 * 로컬 영상 출력 뷰
 * PlayRTCVideoView(SurfaceView)를 상속
 */
class LocalVideoView : PlayRTCVideoView {

    constructor(context: Context) : super(context) {
        // 레이어 중첩 시 리모트 영상 뷰 위에 출력 되도록 렌더링 우선순위를 높게 지정
        super.setZOrderMediaOverlay(true)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        // 레이어 중첩 시 리모트 영상 뷰 위에 출력 되도록 렌더링 우선순위를 높게 지정
        super.setZOrderMediaOverlay(true)
    }

}
