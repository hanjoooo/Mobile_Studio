package com.example.khanj.trust.Data

/*
 * Created by khanj on 2017-10-12.
 */
class Messege(){
    private var Nickname:String=""
    private var Name:String=" "
    private var Uid :String=" "
    private var Lastime:String=" "
    private var Curtime:String=" "
    constructor(Nickname:String,Name:String,Uid:String,time:String,last:String) : this(){
        this.Nickname=Nickname
        this.Name=Name
        this.Uid=Uid
        this.Curtime=time
        if(last.equals(" "))
            ;
        else
            this.Lastime=last
    }

    public fun getNickname():String {
        return this.Nickname
    }
    public fun getName():String{
        return this.Name
    }
    public fun getUid():String{
        return this.Uid
    }
    public fun getTimes():String{
        return this.Curtime
    }
    public fun getLastime():String{
        return this.Lastime
    }
}