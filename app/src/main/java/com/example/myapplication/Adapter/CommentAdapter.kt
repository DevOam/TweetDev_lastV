package com.example.myapplication.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.CommentsActivity
import com.example.myapplication.EditCommentActivity
import com.example.myapplication.R
import com.example.myapplication.model.Comment

class CommentAdapter(
    private val commentList: List<Comment>,
    private val loggedInUserId: String,
    private val postId: String,
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val commentText: TextView = view.findViewById(R.id.comment_text)
        val editButton: Button = view.findViewById(R.id.edit_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.commentText.text = comment.text

        // Show or hide the edit button based on the user ID
        if (comment.userId == loggedInUserId) {
            holder.editButton.visibility = View.VISIBLE
            holder.editButton.setOnClickListener {
                val intent = Intent(holder.itemView.context, EditCommentActivity::class.java)
                intent.putExtra("userId", comment.userId)
                intent.putExtra("postId", postId)
                intent.putExtra("commentText", comment.text)
                (holder.itemView.context as CommentsActivity).startActivityForResult(intent, CommentsActivity.EDIT_COMMENT_REQUEST)
            }
        } else {
            holder.editButton.visibility = View.GONE
        }
    }



    override fun getItemCount(): Int {
        return commentList.size
    }
}
