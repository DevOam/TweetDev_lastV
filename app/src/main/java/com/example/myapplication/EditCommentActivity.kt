package com.example.myapplication




import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.model.Comment
import com.google.firebase.firestore.FirebaseFirestore

class EditCommentActivity : AppCompatActivity() {

    private lateinit var userId: String
    private lateinit var postId: String
    private lateinit var commentText: EditText
    private lateinit var updateButton: Button
    private lateinit var deleteButton: Button

    private var currentText: String  = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_comment)

        commentText = findViewById(R.id.comment_text)
        updateButton = findViewById(R.id.update_button)
        deleteButton = findViewById(R.id.delete_button)


        // Get data from intent
        userId = intent.getStringExtra("userId") ?: ""
        postId = intent.getStringExtra("postId") ?: ""
        currentText = intent.getStringExtra("commentText") ?: ""


        commentText.setText(currentText)

        updateButton.setOnClickListener {
            updateComment()
        }

        deleteButton.setOnClickListener {
            deleteComment()
            val intent = Intent(this, Home
            ::class.java)
            intent.putExtra("idUser", userId)
            startActivity(intent)
        }
    }

    private fun deleteComment() {
        val db = FirebaseFirestore.getInstance()
        val postId = intent.getStringExtra("postId") ?: ""

        db.collection("posts").document(postId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val comments = document.get("comments") as? ArrayList<HashMap<String, Any>>
                    comments?.let {
                        val iterator = it.iterator()
                        while (iterator.hasNext()) {
                            val comment = iterator.next()
                            if (comment["text"] == currentText) {
                                iterator.remove()
                                break
                            }
                        }

                        db.collection("posts").document(postId)
                            .update("comments", comments)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Comment deleted successfully", Toast.LENGTH_SHORT).show()
                                setResult(RESULT_OK) // Add this line
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to delete comment", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Post not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to retrieve post", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateComment() {
        val newText = commentText.text.toString()
        if (newText.isEmpty()) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }



        val db = FirebaseFirestore.getInstance()
        val postId = intent.getStringExtra("postId") ?: ""

        db.collection("posts").document(postId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val comments = document.get("comments") as? ArrayList<HashMap<String, Any>>
                    comments?.let {

                        for (comment in it) {

                            if (comment["text"] == currentText) { // Ensure `id` exists in your comment map
                                Toast.makeText(this, "${comment["text"]}", Toast.LENGTH_SHORT).show()
                                comment["text"] = newText
                                break
                            }
                        }

                        db.collection("posts").document(postId)
                            .update("comments", comments)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Comment updated successfully", Toast.LENGTH_SHORT).show()
                                setResult(RESULT_OK) // Add this line
                                finish() // Close the activity
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to update comment", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Post not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to retrieve post", Toast.LENGTH_SHORT).show()
            }
    }



}
