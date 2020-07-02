package com.sumit.chatterapp.fragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.sumit.chatterapp.model.User

class MyAccountViewModel(application: Application):AndroidViewModel(application)
{
    val userLiveData= MutableLiveData<User>()

}