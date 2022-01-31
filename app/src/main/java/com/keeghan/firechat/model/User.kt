package com.keeghan.firechat.model

import java.io.Serializable

data class User(
    var id: String = "",
    var name: String = "",
    var image: String = "",
    var email: String = "",
    var token: String = ""
) : Serializable {
}