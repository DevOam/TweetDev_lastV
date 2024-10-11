package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Adapter.CommentAdapter
import com.example.myapplication.model.Comment
import com.example.myapplication.model.Post
import com.google.firebase.firestore.FirebaseFirestore

class CommentsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private val comments = mutableListOf<Comment>()

    companion object {
        const val EDIT_COMMENT_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        recyclerView = findViewById(R.id.comments_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val postId = intent.getStringExtra("postId") ?: ""
        val loggedInUserId = intent.getStringExtra("loggedInUserId") ?: ""

        fetchComments(postId, loggedInUserId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_COMMENT_REQUEST && resultCode == RESULT_OK) {
            val postId = data?.getStringExtra("postId") ?: ""
            val loggedInUserId = data?.getStringExtra("loggedInUserId") ?: ""
            fetchComments(postId, loggedInUserId) // Rafraîchir les commentaires
        }
    }


    private fun fetchComments(postId: String, loggedInUserId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts").document(postId).get()
            .addOnSuccessListener { document ->
                val post = document.toObject(Post::class.java)
                post?.comments?.let {
                    comments.clear()
                    comments.addAll(it)
                    commentAdapter = CommentAdapter(comments, loggedInUserId, postId)
                    recyclerView.adapter = commentAdapter
                }
            }
    }


}
