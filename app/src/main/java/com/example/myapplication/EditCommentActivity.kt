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
    private lateinit var deleteButton: Button

    private var currentText: String  = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_comment)

        deleteButton = findViewById(R.id.delete_button)


        // Get data from intent
        userId = intent.getStringExtra("userId") ?: ""
        postId = intent.getStringExtra("postId") ?: ""
        currentText = intent.getStringExtra("commentText") ?: ""





        deleteButton.setOnClickListener {
            deleteComment()
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
//                                Toast.makeText(this, "Comment deleted successfully", Toast.LENGTH_SHORT).show()
                                setResult(RESULT_OK) // Add this line
                                finish()
                            }
                            .addOnFailureListener {
//                                Toast.makeText(this, "Failed to delete comment", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
//                    Toast.makeText(this, "Post not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
//                Toast.makeText(this, "Failed to retrieve post", Toast.LENGTH_SHORT).show()
            }
    }





}
