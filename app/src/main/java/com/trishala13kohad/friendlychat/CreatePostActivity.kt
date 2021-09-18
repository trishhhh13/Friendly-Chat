package com.trishala13kohad.friendlychat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.trishala13kohad.friendlychat.daos.PostDao
import kotlinx.coroutines.DelicateCoroutinesApi

class CreatePostActivity : AppCompatActivity() {

    private lateinit var postDao: PostDao
    @DelicateCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        postDao = PostDao()

        val postButton = findViewById<Button>(R.id.postButton)
        val postInput = findViewById<EditText>(R.id.postInput)

        postButton.setOnClickListener {
            val input = postInput.text.toString().trim()

            if(input.isNotEmpty()){
                postDao.addPost(input)
            }
            finish()
        }
    }
}