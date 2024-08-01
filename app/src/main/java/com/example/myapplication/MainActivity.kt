package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.Toast
import com.example.myapplication.model.User
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private var userId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        userId = intent.getStringExtra("idUser") ?: ""

        if(userId != ""){
            var intent = Intent(this, Home::class.java)
            intent.putExtra("userInfo",userId)
            startActivity(intent)
        }

        // Get references to the views
        val emailInputLayout: TextInputLayout = findViewById(R.id.email)
        val passwordInputLayout: TextInputLayout = findViewById(R.id.password)
        val emailEditText: TextInputEditText = findViewById(R.id.emailEditText)
        val passwordEditText: TextInputEditText = findViewById(R.id.passwordEditText)
        val loginButton: TextView = findViewById(R.id.loginButton)

        // Set up a click listener for the login button
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Check if the user exists
            getUserData(
                email = email,
                password = password,
                onSuccess = { user ->
                    // User exists, handle successful login
                    showMessage("Login successful! Welcome, ${user.username}")
                    var intent = Intent(this, Home::class.java)
                    intent.putExtra("userInfo",user._id)
                    startActivity(intent)
                    // Navigate to another activity if needed
                },
                onFailure = { exception ->
                    // User does not exist or error occurred
                    showMessage("Login failed: ${exception.message}")
                }
            )
        }
    }

    // Function to get user data from Firestore
    fun getUserData(email: String, password: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("login", email)
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val user = result.documents[0].toObject(User::class.java)
                    user?.let { onSuccess(it) }
                } else {
                    onFailure(Exception("User not found"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Function to show a simple message
    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}

// User data class

