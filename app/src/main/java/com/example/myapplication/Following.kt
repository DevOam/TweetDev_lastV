package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Adapter.FollowingAdapter
import com.example.myapplication.model.User
import com.google.firebase.firestore.FirebaseFirestore

class Following : AppCompatActivity() {
    lateinit var nbr: TextView
    private var username: String? = null
    lateinit var recyclerView: RecyclerView
    private val followingList = mutableListOf<String>() // List for following

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_following)

        username = intent.getStringExtra("username")
        nbr = findViewById(R.id.nbr)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = FollowingAdapter(followingList)

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
                        nbr.text = "Following  " +user.following.size.toString()

                        // Add the following users to the list and update the adapter
                        followingList.clear()
                        followingList.addAll(user.following)
                        recyclerView.adapter?.notifyDataSetChanged()

                        break
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("FollowingActivity", "Get failed with ", exception)
            }
    }
}
