package com.tarlad.client.enums

object Events {
    const val CHATS = "chats"

    const val CHATS_CREATE = "chats/create"
    const val CHATS_UPDATE = "chats/update"
    const val CHATS_TITLE = "chats/title"

    const val CHATS_USERS = "chats/users"
    const val CHATS_USERS_ADD = "chats/users/add"
    const val CHATS_USERS_DELETE = "chats/users/delete"
    const val CHATS_USERS_SEARCH = "chats/users/search"

    const val CHATS_MESSAGES_LAST = "chats/messages/last"


    const val USERS = "users"

    const val USERS_UPDATE = "users/update"

    const val USERS_SEARCH = "users/search"

    const val USERS_IMAGES = "users/images"
    const val USERS_IMAGES_DELETE = "users/images/delete"


    const val MESSAGES = "messages"
    const val MESSAGES_UPDATE = "messages/update"
    const val MESSAGES_DELETE = "messages/delete"
    const val MESSAGES_LAST = "messages/last"
}