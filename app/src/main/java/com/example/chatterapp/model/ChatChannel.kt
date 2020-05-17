package com.example.chatterapp.model

data class ChatChannel(val userIds:MutableList<String>){
    constructor():this(mutableListOf())
}