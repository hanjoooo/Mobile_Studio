package com.example.khanj.trust.view

/**
 * Created by khanj on 2017-09-30.
 */


import java.util.ArrayList

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Button
import com.example.khanj.trust.Data.ChannelData
import com.example.khanj.trust.R

/*
 * 채널 팝업의 리스트뷰에서 사용하는 Adapter class
 * BaseAdapter를 확장 구현
 *
 * List<ChannelData> channelList, 채널 데이터 리스트
 *
 * 리스트의 채널 입장 버튼 틀릭 시 채널 데이터를 전달하기 위해 IChannelListAdapter를 정의
 *
 * IChannelListAdapter interface
 * - void onSelectListItem(ChannelData data)
 *
 *
 * @see android.widget.BaseAdapter
 * @see ChannelData
 */
class ChannelListAdapter
/**
 * 생성자
 *
 * @param activity Activity
 * @param l        IChannelListAdapter, 리스트의 채널 입장 버튼 틀릭 시 채널 데이터를 전달하기 위한 Interface 구현 개체
 * @see ChannelListAdapter.IChannelListAdapter
 */
(activity: Activity, l: IChannelListAdapter) : BaseAdapter() {
    private var activity: Activity? = null
    /**
     * List&lt;ChannelData&gt; channelList, 채널 데이터 리스트
     */
    private var channelList = ArrayList<ChannelData>()
    private var inflater: LayoutInflater? = null
    private var listener: IChannelListAdapter? = null

    /**
     * 리스트의 채널 입장 버튼 틀릭 시 채널 데이터를 전달하기 위한 Interface class
     *
     * @author ds3grk
     * <pre>
     * - void onSelectListItem(ChannelData data)
    </pre> *
     */
    interface IChannelListAdapter {
        /**
         * 리스트의 채널 입장 버튼 틀릭 시 채널 데이터를 전달
         *
         * @param data ChannelData
         * @see ChannelData
         */
        fun onSelectListItem(data: ChannelData)
    }

    init {
        this.activity = activity
        this.channelList = ArrayList()
        this.listener = l
    }

    /**
     * 채널 목록 리스트를 지정한다.
     *
     * @param list List&lt;ChannelData&gt;
     * @see ChannelData
     */
    fun setListItems(list: List<ChannelData>) {
        Log.e("LIST_VIEW", "setListItems list")
        synchronized(this.channelList) {
            channelList.clear()
            channelList.addAll(list)
        }
    }


    /**
     * 채널 목록 전체 갯수 반환 <br></br>
     * BaseAdapter 인터페이스
     *
     * @return int
     */
    override fun getCount(): Int {
        return this.channelList.size
    }

    /**
     * 특정 채널 데이터 반환 <br></br>
     * BaseAdapter 인터페이스
     *
     * @return Object, ChannelData
     * @see ChannelData
     */
    override fun getItem(location: Int): Any {
        return this.channelList[location]
    }

    /**
     * 특정 채널 데이터 위치값 반환 <br></br>
     * BaseAdapter 인터페이스
     *
     * @param position int
     * @return long
     */
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * 리스트 뷰에 특정 위치의 데이터를 지정한 Row UI 객체를 반환 <br></br>
     * BaseAdapter 인터페이스
     *
     * @param position    int
     * @param convertView View
     * @param parent      ViewGroup
     */
    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        // layout/list_row.xml
        if (inflater == null)
            inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var v: View? = convertView
        if (v == null) {
            // ROW 초기 객체 생성 시 버튼 이벤트 설정
            v = inflater!!.inflate(R.layout.list_row, null)

            (v!!.findViewById<View>(R.id.row_btn) as Button).setOnClickListener { bv ->
                // 채널 목록의 입장 버튼을 누르면 채널 정보를 전달한다.
                val data = bv.tag as ChannelData
                Log.d("LIST", "setOnClickListener channelId=" + data.channelId!!)
                if (this@ChannelListAdapter.listener != null) {
                    this@ChannelListAdapter.listener!!.onSelectListItem(data)
                }
            }
        }

        // 채널 입장 버튼
        val btn = v.findViewById<View>(R.id.row_btn) as Button
        val txtChannelId = v.findViewById<View>(R.id.row_channel_id) as TextView

        // 채널이름 TextView
        val txtChannelName = v.findViewById<View>(R.id.row_channel_name) as TextView

        // 데이터 리스트에서 특정 위치의 데이터를 조회
        val item = this.channelList[position]

        //버튼에 채널 데이터 등록
        btn.tag = item

        // 채널 아이디 표시
        txtChannelId.text = item.channelId

        // 채널이름 표시
        if (TextUtils.isEmpty(item.channelName) == false) {
            txtChannelName.text = item.channelName
        } else {
            txtChannelName.text = ""
        }

        return v
    }
}
