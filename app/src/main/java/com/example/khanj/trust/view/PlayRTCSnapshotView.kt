package com.example.khanj.trust.view


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout

/*
 * PlayRTCVideoView의 Snapshot 기능과 이미지 배치 Layout을 구성하기 위한 부모뷰 그룹
 * RelativeLayout를 확장하여 Local뷰와 Remote뷰의 Snapshot 버튼과 이미지 출력뷰등을 생성하여 화면을 구성한다.
 */
class PlayRTCSnapshotView : RelativeLayout {

    /**
     * Snapshot 이미지 출력 뷰
     */
    private var displayView: ImageView? = null

    /**
     * SnapshotLayerObserver 인스턴스 객체
     */
    private var snapshotObserver: SnapshotLayerObserver? = null

    /**
     * Local뷰와 Remote뷰의 Snapshot 버튼 이벤트를 전달하기 위한 인터페이스 Class
     */
    interface SnapshotLayerObserver {
        /**
         * Local뷰와 Remote뷰의 Snapshot 버튼 이벤트를 전달
         * @param local boolean, Snapshot 대상이 Local뷰 인지  Remote뷰 인지 구분
         */
        fun onClickSnapshotImage(local: Boolean)
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}


    /**
     * Snapshot 뷰의 Layout 크기를 PlayRTCVideoViewGroup과 같은 크기로 지정한다.
     */
    fun resetViewSize() {

        val screenDimensions = Point()
        val height = this.height

        // ViewGroup의 사이즈 재조정, 높이 기준으로 4(폭):3(높이)으로 재 조정
        // 4:3 = width:height ,  width = ( 4 * height) / 3
        val width = 4.0f * height / 3.0f

        val param = this.layoutParams as RelativeLayout.LayoutParams
        param.width = width.toInt()
        param.height = height.toInt()
        this.layoutParams = param
    }

    @SuppressLint("ResourceType")
            /**
     * Snapshot 버튼과 이미지 배치등의 자식 요소를 동적으로 생성하여 Lauout 구성
     * @param observer SnapshotLayerObserver
     */
    fun createControls(observer: SnapshotLayerObserver) {
        this.snapshotObserver = observer

        /**
         * Snapshot 이미지 출력 뷰 생성, 화면의 중간에 위치
         */
        displayView = ImageView(this.context)
        val image_params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        image_params.addRule(RelativeLayout.CENTER_HORIZONTAL)
        image_params.addRule(RelativeLayout.CENTER_VERTICAL)
        displayView!!.layoutParams = image_params
        displayView!!.setBackgroundColor(Color.argb(100, 255, 255, 255))
        this.addView(displayView)

        /**
         * Local Snapshot 버튼 생성
         */
        val btnLocal = Button(this.context)
        btnLocal.setText("Local")
        btnLocal.setId(1)
        val local_params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        local_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        local_params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        local_params.setMargins(20, 20, 10, 20)
        btnLocal.layoutParams = local_params
        this.addView(btnLocal)
        btnLocal.setOnClickListener {
            if (snapshotObserver != null) {
                snapshotObserver!!.onClickSnapshotImage(true)
            }
        }

        /**
         * Remote Snapshot 버튼 생성
         */
        val btnRemote = Button(this.context)
        btnRemote.text = "Remote"
        btnRemote.id = 2
        val remote_params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        remote_params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        remote_params.addRule(RelativeLayout.RIGHT_OF, 1)
        remote_params.setMargins(10, 20, 10, 20)
        btnRemote.layoutParams = remote_params
        this.addView(btnRemote)
        btnRemote.setOnClickListener {
            if (snapshotObserver != null) {
                snapshotObserver!!.onClickSnapshotImage(false)
            }
        }

        /**
         * Snapshot 이미지 Clear 버튼 생성
         */
        val btnClear = Button(this.context)
        btnClear.text = "Clear"
        btnClear.id = 3
        val clear_params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        clear_params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        clear_params.addRule(RelativeLayout.RIGHT_OF, 2)
        clear_params.setMargins(10, 20, 10, 20)
        btnClear.layoutParams = clear_params
        this.addView(btnClear)
        btnClear.setOnClickListener { clear() }

        /**
         * Snapshot 창 닫기 버튼 생성
         */
        val btnClose = Button(this.context)
        btnClose.text = "Close"
        btnClose.bringToFront()
        val close_params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)

        close_params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        close_params.addRule(RelativeLayout.RIGHT_OF, 3)
        close_params.setMargins(10, 20, 10, 20)
        btnClose.layoutParams = close_params
        this.addView(btnClose)
        btnClose.setOnClickListener {
            this@PlayRTCSnapshotView.visibility = View.INVISIBLE
            clear()
        }

    }

    /**
     * Snapshot 이미지를 뷰에 출력한다.
     */
    fun setSnapshotImage(image: Bitmap) {
        displayView!!.setImageBitmap(image)
    }

    /**
     * Snapshot 이미지 Clear
     */
    private fun clear() {
        displayView!!.setImageResource(android.R.color.transparent)
    }
}
