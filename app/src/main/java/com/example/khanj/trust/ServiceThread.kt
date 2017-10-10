package com.example.khanj.trust

/**
 * Created by khanj on 2017-10-10.
 */

import android.os.Handler

import android.os.Message

class ServiceThread(internal var handler: Handler) : Thread() {

    internal var isRun = true


    fun stopForever() {

        synchronized(this) {

            this.isRun = false

        }

    }


    override fun run() {

        //반복적으로 수행할 작업을 한다.

        while (isRun) {

            handler.sendEmptyMessage(0)//쓰레드에 있는 핸들러에게 메세지를 보냄

            try {

                Thread.sleep(10000) //10초씩 쉰다.

            } catch (e: Exception) {
            }

        }

    }

}

