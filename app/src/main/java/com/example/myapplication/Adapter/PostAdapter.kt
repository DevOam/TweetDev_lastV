package com.example.myapplication.adapter

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
import com.example.myapplication.model.Comment
import com.example.myapplication.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class PostAdapter(private val postList: List<Post>, private val userId: String) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgProfile: ImageView = view.findViewById(R.id.img_profile)
        val name: TextView = view.findViewById(R.id.name)
        val date: TextView = view.findViewById(R.id.date)
        val content: TextView = view.findViewById(R.id.content)
        val nbrLike: TextView = view.findViewById(R.id.nbr_like)
        val likeButton: ImageView = view.findViewById(R.id.like_button)
        val commentInput: EditText = view.findViewById(R.id.comment_input)
        val submitCommentButton: Button = view.findViewById(R.id.btn_submit_comment)
        val commentButton: ImageView = view.findViewById(R.id.comment_button)
        val nbrComment: TextView = view.findViewById(R.id.nbr_comment) // New TextView for number of comments
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]

        // Load the profile image
        Glide.with(holder.itemView.context)
            .load(post.profileImageUrl)
            .placeholder(R.drawable.icon_profile) // Default image if URL is null
            .into(holder.imgProfile)

        // Set the rest of the data
        holder.name.text = post.username
        holder.date.text = post.creationDate
        holder.content.text = post.content
        holder.nbrLike.text = post.likesCount.toString()
        holder.nbrComment.text = "${post.commentsCount} comments" // Set the number of comments

        // Set initial state of the like button
        var isLiked = post.isLikedByCurrentUser
        var currentLikes = post.likesCount

        // Handle like button click
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
                    Toast.makeText(holder.itemView.context, "User ID is missing", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return postList.size
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
                for (post in postList) {
                    if (post.id == postId) {
                        post.comments = post.comments + newComment // Add the new comment to the list
                        notifyItemChanged(postList.indexOf(post)) // Notify the adapter of the change
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
