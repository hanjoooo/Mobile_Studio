package com.example.khanj.trust.Data

class Chat() {
    private var message=" "
        set(value){
            field=value
        }
    private var author=" "
        set(value){
            field=value
        }
    private var times=" "
        set(value){
            field=value
        }
    private var authorUid=" "
        set(value){
            field=value
        }
    constructor(message: String,author: String,times: String,authorUid: String):this(){
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