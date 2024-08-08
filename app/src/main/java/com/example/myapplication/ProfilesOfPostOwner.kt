package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.Adapter.AdapterPostOwner
import com.example.myapplication.model.Post
import com.example.myapplication.model.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProfilesOfPostOwner : AppCompatActivity() {

    private lateinit var usernameTextView: TextView
    private lateinit var followersCountTextView: TextView
    private lateinit var joinDtae: TextView
    private lateinit var img_profile: ImageView
    private lateinit var image_couverture: ImageView
    private lateinit var backhome: ImageView
    private lateinit var followBtn: Button
    private var userID: String? = null
    private var userLoginId: String? = null
    private var username: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var followers:TextView
    private lateinit var following:TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profiles_of_post_owner)

        // Initialize the TextViews
        usernameTextView = findViewById(R.id.usernameTextView)
        followersCountTextView = findViewById(R.id.nbr_folowers)
        joinDtae = findViewById(R.id.since)
        img_profile = findViewById(R.id.image_profile)
        image_couverture = findViewById(R.id.image_couverture)
        followBtn = findViewById(R.id.follow)
        recyclerView = findViewById(R.id.recyclerView)
        following = findViewById(R.id.nbr_folowing)
        backhome = findViewById(R.id.backhome)
backhome.setOnClickListener{
     val intent = Intent(this, Home::class.java)
    startActivity(intent)
}

        username = intent.getStringExtra("username")
        userLoginId = intent.getStringExtra("userLoginId")

        if (username != null) {
            fetchUserIdByUsername(username!!) { userId ->
//                Toast.makeText(this, userId, Toast.LENGTH_SHORT).show()
                getUserData(userId.toString())
                fetchUsernameById(userLoginId.toString(), userId.toString()) { loginUsername ->
                    followBtn.setOnClickListener {
                        username?.let {

                            handleFollowButtonClick(it, userId.toString(), loginUsername)
                            handleFollowingButtonClick(userLoginId.toString(), username.toString())

                            val intent = Intent(this, ProfilesOfPostOwner::class.java)
                            intent.putExtra("username", username)
                            startActivity(intent)
                        }
                    }
                }
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch posts from Firestore
        fetchPostsFromFirestore(username.toString())

        followersCountTextView.setOnClickListener{
            val intent = Intent(this, Followers::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }
        following.setOnClickListener{
            val intent = Intent(this, Following::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }
    }

    private fun fetchPostsFromFirestore(username: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { result ->
                val fetchedPosts = mutableListOf<Post>()
                val fetchTasks = mutableListOf<Task<Void>>()

                for (document in result) {
                    val post = document.toObject(Post::class.java)
                    post.id = document.id // Set the ID manually since Firestore doesn't map it automatically

                    // Fetch the profile image URL and update the post object
                    val profileImageTask = TaskCompletionSource<Void>()
                    getProfileImageUrlByUsername(post.username ?: "") { profileImageUrl ->
                        post.profileImageUrl = profileImageUrl ?: "default_image_url" // Set a default image if none is found
                        profileImageTask.setResult(null)
                    }
                    fetchTasks.add(profileImageTask.task)

                    fetchedPosts.add(post)
                }

                // Wait for all profile image URL fetches to complete
                Tasks.whenAll(fetchTasks).addOnCompleteListener {
                    // Initialize the PostAdapter with the fetched data
                    val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(this)
                    recyclerView.adapter = AdapterPostOwner(this, fetchedPosts, userLoginId.toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Home", "Error getting documents: ", exception)
//                Toast.makeText(this, "Error fetching posts", Toast.LENGTH_SHORT).show()
            }
    }


    private fun getProfileImageUrlByUsername(username: String, callback: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    // No user found with the given username
                    callback(null)
                } else {
                    // Get the first document (assuming username is unique)
                    val document = result.documents.first()
                    val profileImageUrl = document.getString("profileImageUrl")
                    callback(profileImageUrl)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Home", "Error getting profile image URL: ", exception)
                callback(null)
            }
    }

    private fun fetchUserIdByUsername(username: String, callback: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val result = db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .await() // Await the result of the Firestore query

                val userId = if (result.isEmpty) {
                    null
                } else {
                    result.documents.first().getString("_id")
                }

                withContext(Dispatchers.Main) {
                    callback(userId)
//                    Toast.makeText(this@ProfilesOfPostOwner, userId ?: "No ID found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.w("ProfilesOfPostOwner", "Error getting user ID: ", e)
//                    Toast.makeText(this@ProfilesOfPostOwner, "Error fetching user ID", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getUserData(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    if (user?._id == userId) {
                        usernameTextView.text = user.username
                        followersCountTextView.text = user.followers.size.toString()
                        joinDtae.text = formatJoinDate(user.joinDate)
                        Glide.with(this)
                            .load(user.profileImageUrl)
                            .into(img_profile)
                        Glide.with(this)
                            .load(user.profileImageUrl)
                            .into(image_couverture)
                        break
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("ProfileActivity", "Get failed with ", exception)
            }
    }

    fun formatJoinDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault())
        val date = inputFormat.parse(dateString)

        val calendar = Calendar.getInstance()
        date?.let {
            calendar.time = it
        }

        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)

        val monthName = getFrenchMonthName(month)

        return "$monthName $year"
    }

    fun getFrenchMonthName(month: Int): String {
        return when (month) {
            1 -> "janvier"
            2 -> "février"
            3 -> "mars"
            4 -> "avril"
            5 -> "mai"
            6 -> "juin"
            7 -> "juillet"
            8 -> "août"
            9 -> "septembre"
            10 -> "octobre"
            11 -> "novembre"
            12 -> "décembre"
            else -> ""
        }
    }

    private fun fetchUsernameById(userId: String, anotherUserId: String, callback: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("_id", userId)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val document = result.documents.first()
                    val username = document.getString("username") ?: ""
                    checkIfUserIsFollower(username, anotherUserId)
                    callback(username)
                } else {
                    callback("")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("ProfilesOfPostOwner", "Error getting username: ", exception)
                callback("")
            }
    }

    private fun checkIfUserIsFollower(username: String, userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("_id", userId)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    return@addOnSuccessListener
                }

                val document = result.documents.first()
                val followers = document.get("followers") as? List<String>

                if (followers?.contains(username) == true) {
//                    Toast.makeText(this, "true", Toast.LENGTH_SHORT).show()
                    followBtn.text = "unfollow"
                } else {
                    followBtn.text = "follow"
                }
            }
            .addOnFailureListener { exception ->
                Log.w("ProfilesOfPostOwner", "Error checking followers: ", exception)
            }
    }

    private fun handleFollowButtonClick(userId: String, postOwnerId: String, username: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FirebaseFirestore.getInstance()

                // Search for the document with the user ID in the _id field
                val querySnapshot = db.collection("users")
                    .whereEqualTo("_id", postOwnerId)
                    .get()
                    .await()

                if (querySnapshot.isEmpty) {
                    withContext(Dispatchers.Main) {
//                        Toast.makeText(this@ProfilesOfPostOwner, "User not found", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // Get the document ID and followers list
                val userDoc = querySnapshot.documents.first()
                val docId = userDoc.id
                val followers = userDoc.get("followers") as? List<String> ?: emptyList()

                val isFollowing = followers.contains(username)

                if (isFollowing) {
                    // Unfollow the user
                    val updatedFollowers = followers.toMutableList().apply { remove(username) }
                    db.collection("users").document(docId).update("followers", updatedFollowers).await()

                    withContext(Dispatchers.Main) {
                        followBtn.text = "follow"
//                        Toast.makeText(this@ProfilesOfPostOwner, "Unfollowed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Follow the user
                    val updatedFollowers = followers.toMutableList().apply { add(username) }
                    db.collection("users").document(docId).update("followers", updatedFollowers).await()

                    withContext(Dispatchers.Main) {
                        followBtn.text = "unfollow"
//                        Toast.makeText(this@ProfilesOfPostOwner, "Followed", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.w("ProfilesOfPostOwner", "Error handling follow/unfollow: ", e)
//                    Toast.makeText(this@ProfilesOfPostOwner, "Error updating followers", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun handleFollowingButtonClick(currentUserId: String, targetUsername: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FirebaseFirestore.getInstance()

                // Rechercher l'utilisateur actuel par ID pour obtenir ses "following"
                val currentUserQuerySnapshot = db.collection("users")
                    .whereEqualTo("_id", currentUserId)
                    .get()
                    .await()

                if (currentUserQuerySnapshot.isEmpty) {
                    withContext(Dispatchers.Main) {
//                        Toast.makeText(this@ProfilesOfPostOwner, "Current user not found", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // Obtenir le document de l'utilisateur actuel et la liste des "following"
                val currentUserDoc = currentUserQuerySnapshot.documents.first()
                val docId = currentUserDoc.id
                val following = currentUserDoc.get("following") as? List<String> ?: emptyList()

                val isFollowing = following.contains(targetUsername)

                if (isFollowing) {
                    // Dé-following
                    val updatedFollowing = following.toMutableList().apply { remove(targetUsername) }
                    db.collection("users").document(docId).update("following", updatedFollowing).await()

                    withContext(Dispatchers.Main) {
                        followBtn.text = "follow"
//                        Toast.makeText(this@ProfilesOfPostOwner, "Unfollowed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Ajouter à la liste des "following"
                    val updatedFollowing = following.toMutableList().apply { add(targetUsername) }
                    db.collection("users").document(docId).update("following", updatedFollowing).await()

                    withContext(Dispatchers.Main) {
                        followBtn.text = "unfollow"
//                        Toast.makeText(this@ProfilesOfPostOwner, "Followed", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.w("ProfilesOfPostOwner", "Error handling follow/unfollow: ", e)
//                    Toast.makeText(this@ProfilesOfPostOwner, "Error updating following", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
