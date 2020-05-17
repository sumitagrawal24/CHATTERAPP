package com.example.chatterapp.service

import com.example.chatterapp.util.FirestoreUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId

import com.google.firebase.messaging.FirebaseMessagingService

import kotlin.NullPointerException

class MyFirebaseInstanceIDService: FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        val newRegistrationToken = FirebaseInstanceId.getInstance().token

        if(FirebaseAuth.getInstance().currentUser!=null)
            addTokenToFirestore(newRegistrationToken)

    }

    companion object{

        fun addTokenToFirestore(newRegistrationToken:String?){
            if(newRegistrationToken==null)throw NullPointerException("FCM token is null.")
            
            FirestoreUtil.getFCMRegistrationTokens { tokens ->
                if(tokens.contains(newRegistrationToken))
                    return@getFCMRegistrationTokens

                tokens.add(newRegistrationToken)
                FirestoreUtil.setFCMRegistrationTokens(tokens)
            }

        }
    }
}




