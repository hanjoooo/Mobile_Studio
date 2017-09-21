package com.example.khanj.trust

    import android.app.Activity
    import android.content.Context
    import android.graphics.Color
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.BaseAdapter
    import android.widget.TextView
    import com.google.firebase.database.Query
    import kotlinx.android.synthetic.main.chat_message.view.*
    import kotlinx.android.synthetic.main.chat_message1.view.*

/*
 * Created by khanj on 2017-09-14.
 */

    class ChatListAdapter(var datas:ArrayList<Chat>, var context:Context) :BaseAdapter(){
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
            if(p1==null){
                val convert = inflater.inflate(R.layout.chat_message,null)
                val convert2= inflater.inflate(R.layout.chat_message1,null)
                val mTextViewDate : View = convert.findViewById(R.id.textTime)
                val mTextViewDate2 : View = convert2.findViewById(R.id.textTIme2)
                val mTextViewMessage : View = convert.findViewById(R.id.message)
                val mTextViewMessage2 : View = convert2.findViewById(R.id.message2)

                val mAuthorView :View = convert.findViewById(R.id.author)
                val mAuthorView2 :View = convert2.findViewById(R.id.author2)


                val chat : Chat= datas.get(p0)

                if(chat.getAuthor().equals("min")){
                    mTextViewDate2.textTIme2.setText(chat.getTimes())
                    mTextViewMessage2.message2.setText(chat.getMessage())
                    mAuthorView2.author2.setText(chat.getAuthor() + " : ")
                    return convert2
                }
                else {
                    mTextViewDate.textTime.setText(chat.getTimes())
                    mTextViewMessage.message.setText(chat.getMessage())
                    mAuthorView.author.setText(chat.getAuthor() + " : ")
                    return convert
                }
            }
            else {

                return p1
            }
        }

/*
    override fun populateView(v: View, model: Chat) {
        val author:String=model.getAuthor()
        val authorText:TextView= v.findViewById(R.id.author)
        authorText.setText(author+": ")

        if(author != null && author.equals(mUsername))
            authorText.setTextColor(Color.RED)
        else
            authorText.setTextColor(Color.BLUE)

        v.findViewById<TextView>(R.id.message).setText(model.getMessage())

    }
    */
}