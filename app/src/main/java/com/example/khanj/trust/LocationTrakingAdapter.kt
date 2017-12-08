package com.example.khanj.trust

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.khanj.trust.Data.Chat
import kotlinx.android.synthetic.main.chat_message.view.*
import kotlinx.android.synthetic.main.chat_message1.view.*
import kotlinx.android.synthetic.main.route_traking.view.*
import java.io.IOException
import java.util.*


class LocationTrakingAdapter(var datas:ArrayList<String>, var times:ArrayList<String>, var context: Context) : BaseAdapter(){

    var inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return datas.size
    }

    override fun getItem(p0: Int): Any {
        return datas.get(p0)
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val convert = inflater.inflate(R.layout.route_traking, null)
        val mTextViewDate: View = convert.findViewById(R.id.times)
        val mTextViewMessage: View = convert.findViewById(R.id.location)
        val mTextViewNumber:View=convert.findViewById(R.id.number)
        mTextViewNumber.number.setText((p0+1).toString())
        mTextViewDate.times.setText(times[p0])
        mTextViewMessage.location.setText(datas.get(p0))

        return convert
    }

}