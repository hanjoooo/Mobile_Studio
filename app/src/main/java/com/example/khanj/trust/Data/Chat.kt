package com.example.khanj.trust.Data

/**
 * Created by khanj on 2017-09-15.
 */
class Chat() {
    private var message: String=" "
    private var author: String=" "
    private var times:String=" "
    private var authorUid:String=" "

    constructor(message: String, author: String,times:String,authorUid:String) : this(){
        this.message=message
        this.author=author
        this.times=times
        this.authorUid=authorUid
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
    public fun getAuthorUid():String{
        return authorUid
    }
}