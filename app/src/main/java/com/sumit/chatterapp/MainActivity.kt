package com.sumit.chatterapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.chatterapp.R
import com.google.firebase.ktx.Firebase
import com.sumit.chatterapp.fragment.MyAccountFragment
import com.sumit.chatterapp.fragment.MyAccountViewModel
import com.sumit.chatterapp.fragment.PeopleFragment
import com.sumit.chatterapp.util.FirestoreUtil
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private  val viewModel by lazy {
        ViewModelProvider(this).get(MyAccountViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val peopleFragment=PeopleFragment()
        val accountFragment=MyAccountFragment()
        replaceFragment(peopleFragment)
        FirestoreUtil.getCurrentUser { user ->
            viewModel.userLiveData.postValue(user)
        }

        navigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.navigation_people -> {
                    replaceFragment(peopleFragment)
                    true
                }
                R.id.navigation_my_profile -> {
                    replaceFragment(accountFragment)
                    true
                }
                else -> false
            }
        }
    }
    @SuppressLint("CommitTransaction")
    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_layout,fragment)
            .commit()

    }
}
