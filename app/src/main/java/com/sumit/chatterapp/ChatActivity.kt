package com.sumit.chatterapp


//import com.sumit.chatterapp.model.VideoMessage
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BasicGridItem
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.gridItems
import com.bumptech.glide.Glide
import com.example.chatterapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.sumit.chatterapp.item.MessageItem
import com.sumit.chatterapp.model.ImageMessage
import com.sumit.chatterapp.model.TextMessage
import com.sumit.chatterapp.model.User
import com.sumit.chatterapp.util.FirestoreUtil
import com.sumit.chatterapp.util.StorageUtil
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_chat.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


private const val RC_SELECT_IMAGE = 2
//private const val RC_SELECT_VIDEO = 3


class ChatActivity : AppCompatActivity() {


    private lateinit var currentChannelId: String
    private lateinit var currentUser: User
    private lateinit var otherUserId: String

    private lateinit var messagesListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var messagesSection: Section

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        user_name.text=intent.getStringExtra(AppConstants.USER_NAME)

        val profilePic:String? = intent.getStringExtra(AppConstants.USER_PROFILE)

//        intent.getStringExtra(AppConstants.USER_PROFILE).let {profilePic->
        if (profilePic!=null){
            Glide.with(this)
                .load(StorageUtil.pathToReference(profilePic))
                .placeholder(R.drawable.ic_account_circle_black_24dp)
                .into(user_profile_pic)
        }
//        }
        user_name.setOnClickListener{
            startActivity<OtherUserProfile>(
                AppConstants.USER_NAME  to intent.getStringExtra(AppConstants.USER_NAME),
                AppConstants.USER_PROFILE to intent.getStringExtra(AppConstants.USER_PROFILE),
                AppConstants.USER_BIO to intent.getStringExtra(AppConstants.USER_BIO)
            )
        }
        toolbar.setNavigationOnClickListener {
         onBackPressed()
        }

        FirestoreUtil.getCurrentUser {
            currentUser = it
        }

        otherUserId = intent.getStringExtra(AppConstants.USER_ID)
        FirestoreUtil.getOrCreateChatChannel(otherUserId) { channelId ->
            currentChannelId = channelId
            messagesListenerRegistration =
                FirestoreUtil.addChatMessagesListener(channelId, this, this::updateRecyclerView)


            imageView_send.setOnClickListener {
                val text = editText_message.text.toString()
                if (text.isNotBlank())
                {
                    val messageToSend = TextMessage(
                        text, Calendar.getInstance().time,
                        FirebaseAuth.getInstance().currentUser!!.uid,
                        otherUserId, currentUser.name
                    )
                    editText_message.setText("")
                    FirestoreUtil.sendMessage(messageToSend, channelId)
                }
            }
            val items = listOf(
                BasicGridItem(R.drawable.ic_image_black_24dp, "Gallary")
             // ,  BasicGridItem(R.drawable.ic_personal_video_black_24dp, "Video ")
                )


            fab_send_image.setOnClickListener {
                MaterialDialog(this, BottomSheet()).show {
                    gridItems(items) { _, index, item ->

                        when (index) {

                            0 -> {
                                val intent = Intent().apply {
                                    type = "image/*"
                                    action = Intent.ACTION_GET_CONTENT
                                    putExtra(
                                        Intent.EXTRA_MIME_TYPES,
                                        arrayOf("image/jpeg", "image/png")
                                    )
                                }
                                startActivityForResult(Intent.createChooser(intent,"Select Image"),
                                    RC_SELECT_IMAGE)
                            }

                        }

                        toast("Selected item ${item.title} at index $index")
                    }
                }

            }


        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK &&
            data != null && data.data != null
        ) {

            val selectedImagePath = data.data

            val selectedImageBmp = MediaStore.Images.Media.getBitmap(contentResolver, selectedImagePath)
            val outputStream = ByteArrayOutputStream()

            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

            val selectedImageBytes = outputStream.toByteArray()

            StorageUtil.uploadMessageImage(selectedImageBytes) { imagePath ->
                val messageToSend =
                    ImageMessage(
                        imagePath, Calendar.getInstance().time,
                        FirebaseAuth.getInstance().currentUser!!.uid, otherUserId, currentUser.name
                    )
                FirestoreUtil.sendMessage(messageToSend, currentChannelId)
            }

        }

      /*  else if(requestCode== RC_SELECT_VIDEO && resultCode == Activity.RESULT_OK &&
            data != null && data.data !=null){

            val selectedVideoPath = data.data
            //  val selectedVideoPath = data.data

            //val selectedImageBmp = MediaStore.Video.Media.getContentUri(contentResolver.getType())
            val outputStream = ByteArrayOutputStream()




            val selectedVideoBytes = outputStream.toByteArray()

            StorageUtil.uploadMessageVideo(selectedVideoBytes){ videoPath ->
                 val messageToSend =
                     VideoMessage(videoPath,Calendar.getInstance().time,
                         FirebaseAuth.getInstance().currentUser!!.uid,otherUserId,currentUser.name)
                 FirestoreUtil.sendMessage(messageToSend,currentChannelId)
             }




        }
        */


    }

    fun getDifferenceDays(d1: Date, d2: Date): Long {
        val diff = d2.time - d1.time
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
    }
    private fun updateRecyclerView(messages: List<Item>) {
        fun init() {
            recycler_view_messages.apply {
                layoutManager = LinearLayoutManager(this@ChatActivity)
                adapter = GroupAdapter<GroupieViewHolder>().apply {
                    messagesSection = Section(messages)
                    this.add(messagesSection)

                }
                val layoutManagerRV= layoutManager as LinearLayoutManager
                addOnScrollListener(object  : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        Log.d("scroll", "scrolling")
                        //added here

                        val pos = layoutManagerRV.findFirstVisibleItemPosition()
                        if (pos >= 0 && pos< messages.size) {
                        val item = messages[pos] as MessageItem
                        val differenceDays = getDifferenceDays(Date(0), item.msg.time)


                            if (differenceDays > 1) {
                                date_view.visibility = View.VISIBLE
                                val df2 = SimpleDateFormat("dd MMM yyyy")
                                date_view.text = df2.format(item.msg.time)
                            } else {
                                date_view.visibility = View.INVISIBLE
                            }

                        }
                        }

                })
            }
            shouldInitRecyclerView = false
        }

        fun updateItems() = messagesSection.update(messages)

        if (shouldInitRecyclerView) init()
        else {
            updateItems()
        }

        recycler_view_messages.scrollToPosition(recycler_view_messages.adapter!!.itemCount - 1)


    }


}


