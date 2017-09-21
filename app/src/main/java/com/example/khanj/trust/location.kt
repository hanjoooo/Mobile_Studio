package com.example.khanj.trust

/*
 * Created by khanj on 2017-09-13.
 */
class location(){
    var times :String = " "
    var latitude : Double = 0.0
    var longitude : Double = 0.0

    constructor(times:String,lat:Double,lon:Double) : this(){
        this.times=times
        this.latitude=lat
        this.longitude=lon
    }
    fun toMap():Map<String,Any>{
        var result:HashMap<String,Any> = HashMap<String,Any>()
        result.put("time",times)
        result.put("경도",latitude)
        result.put("위도",longitude)
        return result
    }
}