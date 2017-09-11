package com.example.khanj.trust

/**
 * Created by khanj on 2017-09-08.
 */

class ChatData {
    var userName: String? = null
    var message: String? = null

    constructor() {}

    constructor(userName: String, message: String) {
        this.userName = userName
        this.message = message
    }

}
