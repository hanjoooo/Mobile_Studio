package com.example.khanj.trust.view

/*
* Created by khanj on 2017-09-30.
*/

import android.content.Context
import android.text.Layout
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.example.khanj.trust.R

/*
 * PlayRTC 로그를 출력하기위해 TextView를 확장한 Class
 *
 *
 * - public void appendLog(final String msg)
 *    로그창 하단에 로그 문자열울 추가하고 스크롤를 하단으로 이동한다.
 * - public void progressLog(final String msg)
 *   로그창 하단의 마지막 라인을 갱신하고 스크롤를 하단으로 이동한다.
 *   데이터 채널 데이터 전송/수신 등의 진척도를 표시하기 위해 사용
 *
 */
class PlayRTCLogView : TextView {

    private var prevText: String? = null
    private var hasPrevText = false

    /**
     * 생성자
     * @param context Context
     */
    constructor(context: Context) : super(context) {
        // 스크롤 갱신을 위해
        this.movementMethod = ScrollingMovementMethod()
    }

    /**
     * 생성자
     * @param context Context
     * @param attrs AttributeSet
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        // 스크롤 갱신을 위해
        this.movementMethod = ScrollingMovementMethod()
    }

    /**
     * 생성자
     * @param context Context
     * @param attrs AttributeSet
     * @param defStyleAttr int
     */
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        // 스크롤 갱신을 위해
        this.movementMethod = ScrollingMovementMethod()
    }

    /**
     * 로그뷰를 화면에 출력
     */
    fun show() {
        val animation = AnimationUtils.loadAnimation(this.context, R.anim.log_show)
        animation.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationEnd(anim: Animation) {

            }

            override fun onAnimationRepeat(anim: Animation) {

            }

            override fun onAnimationStart(anim: Animation) {
                this@PlayRTCLogView.visibility = View.VISIBLE
            }

        })
        this.startAnimation(animation)
        this@PlayRTCLogView.bringToFront()

    }

    /**
     * 로그뷰를 화면에서 숨긴다.
     */
    fun hide() {
        val animation = AnimationUtils.loadAnimation(this.context, R.anim.log_hide)
        animation.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationEnd(anim: Animation) {
                this@PlayRTCLogView.visibility = View.INVISIBLE
            }

            override fun onAnimationRepeat(anim: Animation) {

            }

            override fun onAnimationStart(anim: Animation) {

            }

        })
        this.startAnimation(animation)
    }

    /**
     * 로그 창 초기화
     */
    fun clear() {
        this.post { text = "" }
    }

    /**
     * 로그창 하단에 로그 문자열울 추가하고 스크롤를 하단으로 이동한다.
     * @param message String, 로그 출력 메세지
     */
    fun appnedLogMessage(message: String) {


        this.post {
            if (hasPrevText == true) {
                hasPrevText = false
                prevText = null
            }
            append(message + "\n")
            val layout = this@PlayRTCLogView.layout
            if (layout != null) {
                val scrollDelta = layout.getLineBottom(lineCount - 1) - scrollY - height
                if (scrollDelta > 0) {
                    scrollBy(0, scrollDelta)
                }
            }
        }

    }

    /**
     * 로그창 하단의 마지막 라인을 갱신하고 스크롤를 하단으로 이동한다.<br></br>
     * 데이터 채널 데이터 전송/수신 등의 진척도를 표시하기 위해 사용
     * @param message String, 로그 출력 메세지
     */
    fun progressLogMessage(message: String) {

        this.post {
            if (hasPrevText == false) {
                hasPrevText = true
                prevText = text.toString()
            }
            text = prevText + message + "\n"

            val layout = layout
            if (layout != null) {
                val scrollDelta = layout.getLineBottom(lineCount - 1) - scrollY - height
                if (scrollDelta > 0) {
                    scrollBy(0, scrollDelta)
                }
            }
        }

    }
}
