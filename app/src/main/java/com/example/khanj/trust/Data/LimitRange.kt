package com.example.khanj.trust.Data

/**
 * Created by khanj on 2017-10-16.
 */
class LimitRange(){
    var radius :Double = 0.0
    var latitude : Double = 0.0
    var longitude : Double = 0.0

    constructor(range:Double ,lat:Double,lon:Double) : this(){
        this.radius=range
        this.latitude=lat
        this.longitude=lon
    }
    fun toMap():Map<String,Any>{
        var result:HashMap<String,Any> = HashMap<String,Any>()
        result.put("반경",radius)
        result.put("위도",latitude)
        result.put("경도",longitude)
        return result
    }
}