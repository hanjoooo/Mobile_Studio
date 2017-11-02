package com.example.khanj.trust.adapter

/**
 * Created by khanj on 2017-10-18.
 */
import android.view.View

/**
 * Created by Alessandro Barreto on 27/06/2016.
 */
interface ClickListenerChatFirebase {

    /**
     * Quando houver click na imagem do chat
     * @param view
     * @param position
     */
    fun clickImageChat(view: View, position: Int, nameUser: String, urlPhotoUser: String, urlPhotoClick: String)

    /**
     * Quando houver click na imagem de mapa
     * @param view
     * @param position
     */
    fun clickImageMapChat(view: View, position: Int, latitude: String, longitude: String)

}
