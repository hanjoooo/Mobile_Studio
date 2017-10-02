package com.example.khanj.trust.Data

/**
 * Created by khanj on 2017-09-21.
 */
class User(){
    private var Email: String=" "
    private var Password: String=" "
    private var Nickname:String=" "
    private var Name:String=" "
    private var PhoneNumber:String=" "
    private var MyUid :String=""
    private var OtherUid :String=" "
    constructor(Email: String, Password: String,Nickname:String,Name:String,PhoneNumber:String,MyUid:String) : this(){
        this.Email=Email
        this.Password=Password
        this.Nickname=Nickname
        this.Name=Name
        this.PhoneNumber=PhoneNumber
        this.MyUid=MyUid
    }

    public fun getEmail():String{
        return Email
    }
    public fun getPassword():String{
        return Password
    }
    public fun getNickname():String {
        return Nickname
    }
    public fun getName():String{
        return Name
    }
    public fun getPhoneNumber():String{
        return PhoneNumber
    }
    public fun getMyUid():String{
        return MyUid
    }
    public fun getOtherUid():String{
        return OtherUid
    }

    public fun setPassword(p:String){
        this.Password=p
    }
    public fun setNickname(p:String){
        this.Nickname=p
    }
    public fun setName(p:String){
        this.Name=p
    }
    public fun setPhoneNumber(p:String){
        this.PhoneNumber=p
    }
    public fun setMyUid(p:String){
        this.MyUid=p
    }
    public fun setOtherUid(p:String){
        this.OtherUid=p
    }

}