package com.sumit.chatterapp.model

import java.util.*

object MessageType{
    const val TEXT="TEXT"
    const val IMAGE="IMAGE"
    //const val VIDEO="VIDEO"
}
interface Message {
    val time: Date
    val senderId:String
    val recipientId: String
    val senderName:String
    val type:String
}