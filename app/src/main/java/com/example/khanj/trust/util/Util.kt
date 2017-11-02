package com.example.khanj.trust.util

import android.content.Context
import android.net.ConnectivityManager
import android.widget.Toast

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

/**
 * Created by Alessandro Barreto on 23/06/2016.
 */
object Util {

    val URL_STORAGE_REFERENCE = "gs://trust-cd479.appspot.com"
    val FOLDER_STORAGE_IMG = "images"

    fun initToast(c: Context, message: String) {
        Toast.makeText(c, message, Toast.LENGTH_SHORT).show()
    }

    fun verificaConexao(context: Context): Boolean {
        val conectado: Boolean
        val conectivtyManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        conectado = conectivtyManager.activeNetworkInfo != null
                && conectivtyManager.activeNetworkInfo.isAvailable
                && conectivtyManager.activeNetworkInfo.isConnected
        return conectado
    }

    fun local(latitudeFinal: String, longitudeFinal: String): String {
        return "https://maps.googleapis.com/maps/api/staticmap?center=$latitudeFinal,$longitudeFinal&zoom=18&size=280x280&markers=color:red|$latitudeFinal,$longitudeFinal"
    }

}