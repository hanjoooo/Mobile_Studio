package com.example.khanj.trust.view

/*
 * Created by khanj on 2017-09-30.
 */

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.SeekBar

/**
 * Created by ds3grk on 2017. 1. 4..
 */
class PlayRTCVerticalSeekBar : SeekBar {
    constructor(c: Context) : super(c) {}

    constructor(c: Context, attrs: AttributeSet) : super(c, attrs) {}


    constructor(c: Context, attrs: AttributeSet, defStyle: Int) : super(c, attrs, defStyle) {}

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(h, w, oldh, oldw)
    }

    @Synchronized override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(c: Canvas) {
        c.rotate(-90f)
        c.translate((-height).toFloat(), 0f)

        super.onDraw(c)
    }

    private var mChangeListener: SeekBar.OnSeekBarChangeListener? = null

    override fun setOnSeekBarChangeListener(onChangeListener: SeekBar.OnSeekBarChangeListener) {
        this.mChangeListener = onChangeListener
    }

    private var mLastProgress = 0

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (mChangeListener != null)
                    mChangeListener!!.onStartTrackingTouch(this)

                isPressed = true
                isSelected = true
            }

            MotionEvent.ACTION_MOVE -> {
                super.onTouchEvent(event)
                var nProgress = max - (max * event.y / height).toInt()
                if (nProgress < 0) {
                    nProgress = 0
                }
                if (nProgress > max) {
                    nProgress = max
                }
                progress = nProgress // Draw progress
                if (nProgress != mLastProgress) {
                    mLastProgress = nProgress
                    if (mChangeListener != null)
                        mChangeListener!!.onProgressChanged(this, nProgress, true)
                }
                onSizeChanged(width, height, 0, 0)
                isPressed = true
                isSelected = true
            }
            MotionEvent.ACTION_UP -> {
                if (mChangeListener != null)
                    mChangeListener!!.onStopTrackingTouch(this)
                isPressed = false
                isSelected = false
            }
            MotionEvent.ACTION_CANCEL -> {
                super.onTouchEvent(event)
                isPressed = false
                isSelected = false
            }
        }
        return true

    }

    @Synchronized
    fun setProgressAndThumb(progress: Int) {
        setProgress(progress)
        onSizeChanged(width, height, 0, 0)
        if (progress != mLastProgress) {
            mLastProgress = progress
            if (mChangeListener != null)
                mChangeListener!!.onProgressChanged(this, progress, true)
        }
    }

    var maximum: Int
        @Synchronized get() = max
        @Synchronized set(maximum) {
            max = maximum
        }
}
