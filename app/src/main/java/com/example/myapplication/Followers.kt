package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.Adapter.FollowersAdapter
import com.example.myapplication.model.User
import com.google.firebase.firestore.FirebaseFirestore



class Followers : AppCompatActivity() {
    lateinit var nbr: TextView
    private var username: String? = null
    lateinit var recyclerView: RecyclerView
    private val followersList = mutableListOf<String>() // Liste pour les followers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_followers)

        username = intent.getStringExtra("username")
        nbr = findViewById(R.id.nbr)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = FollowersAdapter(followersList)

        Toast.makeText(this, "use" + username, Toast.LENGTH_SHORT).show()
        getUserData(username.toString())
    }

    @SuppressLint("SetTextI18n")
    private fun getUserData(uname: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    if (user?.username == uname) {
                        nbr.text = "Followers  " + user.followers.size.toString()

                        // Ajouter les followers à la liste et mettre à jour l'adapter
                        followersList.clear()
                        followersList.addAll(user.followers)
                        recyclerView.adapter?.notifyDataSetChanged()

                        break
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("ProfileActivity", "Get failed with ", exception)
            }
    }
}