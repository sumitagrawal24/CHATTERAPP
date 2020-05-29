package com.sumit.chatterapp.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.sumit.chatterapp.AppConstants
import com.sumit.chatterapp.ChatActivity

import com.example.chatterapp.R
import com.sumit.chatterapp.util.FirestoreUtil
import com.sumit.chatterapp.item.PersonItem
import com.google.firebase.firestore.ListenerRegistration
import com.sumit.chatterapp.util.StorageUtil
import com.xwray.groupie.Section
import kotlinx.android.synthetic.main.fragment_people.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_person.*
import org.jetbrains.anko.support.v4.startActivity


class PeopleFragment : Fragment() {

    private lateinit var userListenerRegistration: ListenerRegistration

    private var shouldInitRecycleView = true

    private lateinit var peopleSection: Section

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        userListenerRegistration = FirestoreUtil.addUsersListener(this.requireActivity(),::updateRecyclerView)
        return inflater.inflate(R.layout.fragment_people, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        FirestoreUtil.removeListener(userListenerRegistration)
        shouldInitRecycleView = true
    }

private fun updateRecyclerView(items: List<Item>){

        fun init(){
            recycler_view_people.apply{
                layoutManager = LinearLayoutManager(this@PeopleFragment.context)
                adapter = GroupAdapter<GroupieViewHolder>().apply{
                    peopleSection = Section(items)
                    add(peopleSection)
                    setOnItemClickListener(onItemClick)
                }


            }
            shouldInitRecycleView = false
        }
        fun updateItems() = peopleSection.update(items)
        if(shouldInitRecycleView)
            init()
        else
            updateItems()

    }

    private val onItemClick = OnItemClickListener{ item,view->
        if(item is PersonItem){
            startActivity<ChatActivity>(
                AppConstants.USER_NAME to item.person.name,
                AppConstants.USER_ID to item.userId,
                AppConstants.USER_BIO to item.person.bio,
                AppConstants.USER_PROFILE to item.person.profilePicturePath

            )

        }
    }


}
