package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.PostAdapter
import com.example.myapplication.model.Post
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Home : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private var userId: String? = null
    private val posts = mutableListOf<Post>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Retrieve the user ID from the intent
        userId = intent.getStringExtra("userInfo")

        // Initialize the toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize the drawer layout
        drawerLayout = findViewById(R.id.drawer_layout)

        // Initialize the navigation view
        navView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        // Set up the ActionBarDrawerToggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch posts from Firestore
        fetchPostsFromFirestore()
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("idUser", userId)
        startActivity(intent)
        super.onBackPressed() // Call the superclass implementation
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // Handle the home action
            }
            R.id.nav_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("idUser", userId)
                startActivity(intent)
            }

            R.id.nav_hub -> {
                val intent = Intent(this, HubScreen::class.java)
                intent.putExtra("idUser", userId)
                startActivity(intent)
            }
            R.id.nav_logout -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("idUser", "")
                startActivity(intent)
            }
        }
        drawerLayout.closeDrawer(navView)
        return true
    }

    private fun fetchPostsFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts")
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
                    val adapter = PostAdapter(fetchedPosts, userId ?: "")
                    recyclerView.adapter = adapter
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Home", "Error getting documents: ", exception)
                Toast.makeText(this, "Error fetching posts", Toast.LENGTH_SHORT).show()
            }
    }


    // Function to get profileImageUrl by username
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
    private fun formatJoinDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault())
        val date = inputFormat.parse(dateString)

        val calendar = Calendar.getInstance()
        date?.let {
            calendar.time = it
        }

        val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is zero-based
        val year = calendar.get(Calendar.YEAR)

        val monthName = getFrenchMonthName(month)

        return "$monthName $year"
    }

    private fun getFrenchMonthName(month: Int): String {
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

}
