package com.example.myapplication

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ActivityHubDetailScreenBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class Hub_detail_screen : AppCompatActivity() {
    private lateinit var binding: ActivityHubDetailScreenBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var loginUsername: String
    private lateinit var nbr_followers: TextView
    private var isFollowing = false
    private var hubId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHubDetailScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        nbr_followers = findViewById(R.id.nbr_folowers)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Retrieve hub data from the intent
        hubId = intent.getStringExtra("HUB_ID")
        val hubName = intent.getStringExtra("HUB_NAME")
        val hubDescription = intent.getStringExtra("HUB_DESCRIPTION")
        val hubProfileImageUrl = intent.getStringExtra("HUB_PROFILE_IMAGE_URL")
        val hubCoverImageUrl = intent.getStringExtra("HUB_COVER_IMAGE_URL")
        val hubCreationDate = intent.getStringExtra("HUB_CREATION_DATE")
        val userId = intent.getStringExtra("USER_ID")  // Retrieve userId here

        // Display hub data
        binding.usernameTextView.text = hubName
        binding.description.text = hubDescription
        binding.since.text = hubCreationDate
        Glide.with(this).load(hubProfileImageUrl).into(binding.imageProfile)
        Glide.with(this).load(hubCoverImageUrl).into(binding.imageCouverture)

        // Fetch username from Firestore
        if (userId != null) {
            fetchUsername(userId)
        }

        // Fetch number of followers
        fetchNumberOfFollowers()

        // Set click listener for the follow button
        binding.follow.setOnClickListener {
            if (isFollowing) {
                unfollowHub()
            } else {
                followHub()
            }
        }
    }

    private fun fetchUsername(userId: String) {
        firestore.collection("users")
            .whereEqualTo("_id", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val username = document.getString("username")
                    if (username != null) {
                        Toast.makeText(this, username, Toast.LENGTH_SHORT).show()
                        loginUsername = username
                        // After getting the loginUsername, check if it exists in the hub's users array
                        if (hubId != null) {
                            checkHubUsers(hubId!!, loginUsername)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkHubUsers(hubId: String, username: String) {
        firestore.collection("hubs")
            .whereEqualTo("_id", hubId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val users = document.get("users") as? List<*>
                    isFollowing = users != null && users.contains(username)
                    updateFollowButton()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting hub documents: $exception", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateFollowButton() {
        binding.follow.text = if (isFollowing) "unfollow" else "follow"
    }

    private fun followHub() {
        if (hubId != null) {
            firestore.collection("hubs")
                .whereEqualTo("_id", hubId)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val documentId = documents.documents[0].id
                        firestore.collection("hubs").document(documentId)
                            .update("users", FieldValue.arrayUnion(loginUsername))
                            .addOnSuccessListener {
                                isFollowing = true
                                updateFollowButton()
                                Toast.makeText(this, "Followed successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Error following hub: $exception", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Hub not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error finding hub: $exception", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun unfollowHub() {
        if (hubId != null) {
            firestore.collection("hubs")
                .whereEqualTo("_id", hubId)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val documentId = documents.documents[0].id
                        firestore.collection("hubs").document(documentId)
                            .update("users", FieldValue.arrayRemove(loginUsername))
                            .addOnSuccessListener {
                                isFollowing = false
                                updateFollowButton()
                                Toast.makeText(this, "Unfollowed successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Error unfollowing hub: $exception", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Hub not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error finding hub: $exception", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchNumberOfFollowers() {
        hubId?.let { hubId ->
            firestore.collection("hubs")
                .whereEqualTo("_id", hubId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val users = document.get("users") as? List<*>
                        val numberOfFollowers = users?.size ?:0

                        nbr_followers.text = numberOfFollowers.toString()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error getting hub documents: $exception", Toast.LENGTH_SHORT).show()
                }
        }
    }


}
