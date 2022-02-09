package com.keeghan.firechat.listeners

import com.keeghan.firechat.model.User

interface ConversationListener {
    fun onConversationClicked(user: User)
}