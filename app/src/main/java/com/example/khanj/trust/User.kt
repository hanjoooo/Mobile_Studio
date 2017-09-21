package com.example.khanj.trust

/**
 * Created by khanj on 2017-09-21.
 */
class User(){
    private var Email: String=" "
    private var Password: String=" "
    private var Nickname:String=" "
    private var Name:String=" "
    private var PhoneNumber:String=" "
    constructor(Email: String, Password: String,Nickname:String,Name:String,PhoneNumber:String) : this(){
        this.Email=Email
        this.Password=Password
        this.Nickname=Nickname
        this.Name=Name
        this.PhoneNumber=PhoneNumber
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

}