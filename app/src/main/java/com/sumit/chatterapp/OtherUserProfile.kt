package com.sumit.chatterapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.chatterapp.R
import com.sumit.chatterapp.util.StorageUtil
import kotlinx.android.synthetic.main.activity_other_user_profile.*

class OtherUserProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_user_profile)

        other_user_name.text=intent.getStringExtra(AppConstants.USER_NAME)
        other_user_bio.text=intent.getStringExtra(AppConstants.USER_BIO)

        val profilePic:String? = intent.getStringExtra(AppConstants.USER_PROFILE)
            if (profilePic!=null){
                Glide.with(this)
                .load(StorageUtil.pathToReference(profilePic))
                .placeholder(R.drawable.ic_account_circle_black_24dp)
                .into(other_user_profilepic)
        }
    }
}
