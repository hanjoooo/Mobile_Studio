package com.example.khanj.trust

/**
 * Created by khanj on 2017-09-15.
 */
class Chat() {

    private var message: String=" "
    private var author: String=" "
    private var times:String=" "

    constructor(message: String, author: String,times:String) : this(){
        this.message=message
        this.author=author
        this.times=times
    }
    public fun getMessage():String{
        return message
    }
    public fun getAuthor():String{
        return author
    }
    public fun getTimes():String{
        return times
    }


}