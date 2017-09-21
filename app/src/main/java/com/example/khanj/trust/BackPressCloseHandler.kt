package com.example.khanj.trust

import android.app.Activity
import android.widget.Toast

/**
 * Created by khanj on 2017-09-21.
 */
class BackPressCloseHandler(private val activity: Activity) {
    private var backKeyPressedTime: Long = 0
    private var toast: Toast? = null

    fun onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis()
            showGuide()
            return
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            toast!!.cancel()

            activity.moveTaskToBack(true)
            activity.finish()
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    fun showGuide() {
        toast = Toast.makeText(activity, "한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT)
        toast!!.show()
    }
}