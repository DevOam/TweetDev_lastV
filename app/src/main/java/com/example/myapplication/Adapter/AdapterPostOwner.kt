package com.example.myapplication.Adapter

// PostAdapter.kt

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.CommentsActivity
import com.example.myapplication.R
import com.example.myapplication.model.Post
import com.example.myapplication.ProfilesOfPostOwner
import com.example.myapplication.model.Comment
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AdapterPostOwner(private val context: Context, private val posts: List<Post>, private val userId: String) :
    RecyclerView.Adapter<AdapterPostOwner.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.img_profile)
        val name: TextView = itemView.findViewById(R.id.name)
        val date: TextView = itemView.findViewById(R.id.date)
        val content: TextView = itemView.findViewById(R.id.content)
        val likeButton: ImageView = itemView.findViewById(R.id.like_button)
        val nbrLike: TextView = itemView.findViewById(R.id.nbr_like)
        val commentButton: ImageView = itemView.findViewById(R.id.comment_button)
        val nbrComment: TextView = itemView.findViewById(R.id.nbr_comment)
        val submitCommentButton: Button = itemView.findViewById(R.id.btn_submit_comment)
        val commentInput: EditText = itemView.findViewById(R.id.comment_input)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.name.text = post.username
        holder.content.text = post.content
        holder.nbrLike.text = post.likesCount.toString()
        holder.nbrComment.text = "${post.commentsCount} comments"

        Glide.with(context).load(post.profileImageUrl).into(holder.profileImage)

        // Handle profile image click
        holder.profileImage.setOnClickListener {
            val intent = Intent(context, ProfilesOfPostOwner::class.java)
            intent.putExtra("username", post.username)
            context.startActivity(intent)
        }

        // Handle like button click
        // Handle like button click
        // Set initial state of the like button
        var isLiked = post.isLikedByCurrentUser
        var currentLikes = post.likesCount
        holder.likeButton.setOnClickListener {
            if (isLiked) {
                currentLikes -= 1
            } else {
                currentLikes += 1
            }
            isLiked = !isLiked

            // Update the post object
            post.likes = currentLikes.toString()
            post.isLikedByCurrentUser = isLiked

            // Update Firestore
            updatePostLikes(post.id, isLiked, currentLikes)

            // Update UI
            holder.nbrLike.text = currentLikes.toString()
        }
        // Handle comment button click
        holder.commentButton.setOnClickListener {
            val context = holder.itemView.context
            // Handle comment button click (e.g., navigate to a comments screen)
        }
        holder.profileImage.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ProfilesOfPostOwner::class.java)
            intent.putExtra("username", post.username)
            intent.putExtra("userLoginId", post.id)
            context.startActivity(intent)
        }

        holder.commentButton.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, CommentsActivity::class.java)
            intent.putExtra("postId", post.id)
            intent.putExtra("loggedInUserId", userId) // Pass the logged-in user's ID

            context.startActivity(intent)
        }

        // Handle comment submission
        holder.submitCommentButton.setOnClickListener {
            val comment = holder.commentInput.text.toString().trim()
            if (comment.isNotEmpty()) {
                if (userId.isNotEmpty()) { // Ensure userId is not empty
                    addCommentToPost(post.id, userId, comment) {
                        holder.commentInput.text.clear() // Clear the comment input field
                    }
                } else {
//                    Toast.makeText(holder.itemView.context, "User ID is missing", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }
    private fun updatePostLikes(postId: String, isLiked: Boolean, newLikes: Int) {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts")
            .document(postId)
            .update(
                mapOf(
                    "likes" to newLikes.toString(), // Update the number of likes
                    "isLikedByCurrentUser" to isLiked // Update the liked status
                )
            )
            .addOnSuccessListener {
                // Successfully updated likes
            }
            .addOnFailureListener { e ->
                // Handle the error
            }
    }

    private fun addCommentToPost(postId: String, userId: String, comment: String, onSuccess: () -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val newComment = Comment(userId, comment)
        db.collection("posts")
            .document(postId)
            .update("comments", FieldValue.arrayUnion(newComment))
            .addOnSuccessListener {
                // Find the post and update its comments count
                for (post in posts) {
                    if (post.id == postId) {
                        post.comments = post.comments + newComment // Add the new comment to the list
                        notifyItemChanged(posts.indexOf(post)) // Notify the adapter of the change
                        break
                    }
                }
                onSuccess()
            }
            .addOnFailureListener { e ->
                // Handle the error
            }
    }

}
