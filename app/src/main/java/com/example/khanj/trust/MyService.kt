package com.example.khanj.trust



import android.app.Notification

import android.app.NotificationManager

import android.app.PendingIntent

import android.app.Service

import android.content.Context

import android.content.Intent

import android.os.Handler

import android.os.IBinder

import android.widget.Toast


class MyService : Service() {

    internal var Notifi_M: NotificationManager?=null

    internal var thread: ServiceThread? = null

    internal var Notifi: Notification?=null


    override fun onBind(intent: Intent): IBinder? {

        return null

    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        Notifi_M = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val handler = myServiceHandler()

        thread = ServiceThread(handler)

        thread!!.start()

        return Service.START_STICKY

    }


    //서비스가 종료될 때 할 작업


    override fun onDestroy() {

        thread!!.stopForever()

        thread = null//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.

    }


    internal inner class myServiceHandler : Handler() {

        override fun handleMessage(msg: android.os.Message) {

            val intent = Intent(this@MyService, MainActivity::class.java)

            val pendingIntent = PendingIntent.getActivity(this@MyService, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)



            Notifi = Notification.Builder(applicationContext)

                    .setContentTitle("Content Title")

                    .setContentText("Content Text")

                    .setSmallIcon(R.drawable.background2)

                    .setTicker("알림!!!")

                    .setContentIntent(pendingIntent)

                    .build()


            //소리추가

            Notifi!!.defaults = Notification.DEFAULT_SOUND


            //알림 소리를 한번만 내도록

            Notifi!!.flags = Notification.FLAG_ONLY_ALERT_ONCE


            //확인하면 자동으로 알림이 제거 되도록

            Notifi!!.flags = Notification.FLAG_AUTO_CANCEL





            Notifi_M!!.notify(777, Notifi)


            //토스트 띄우기

            Toast.makeText(this@MyService, "뜸?", Toast.LENGTH_LONG).show()

        }

    }

}


