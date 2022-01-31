package com.keeghan.firechat.listeners

import com.keeghan.firechat.model.User

interface UserListener {
    fun onUserClicked(user: User)
}