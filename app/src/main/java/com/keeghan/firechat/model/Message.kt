package com.keeghan.firechat.model

import java.util.*

data class Message(
    var senderID: String = "",
    var receiverID: String = "",
    var message: String = "",
    var dateTime: String = "",
    var dateObj: Date = Date(),
    var conversationId: String = "",
    var conversationUserName: String = "",
    var conversationImage: String = "",
) {

}