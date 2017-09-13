package com.example.khanj.trust

/**
 * Created by khanj on 2017-09-13.
 */
class location(times:String,lat:Double,lon:Double){
    var times=times
    var latitude=lat
    var longitude=lon
    fun toMap():Map<String,Any>{
        var result:HashMap<String,Any> = HashMap<String,Any>()
        result.put("time",times)
        result.put("경도",latitude)
        result.put("위도",longitude)
        return result
    }
}