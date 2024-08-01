package com.example.myapplication


import AdapterHubs
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityHubScreenBinding
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.model.Hub
import com.google.firebase.firestore.FirebaseFirestore

class HubScreen : AppCompatActivity() {
    private lateinit var binding: ActivityHubScreenBinding
    private lateinit var hubAdapter: AdapterHubs
    private var userId: String? = null

    private val hubs = mutableListOf<Hub>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHubScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userId = intent.getStringExtra("idUser")

        setupRecyclerView(userId.toString())
        fetchDataFromFirestore()


    }

    private fun setupRecyclerView(userId: String) {
        Toast.makeText(this, userId, Toast.LENGTH_SHORT).show()

        hubAdapter = AdapterHubs(this, hubs, userId)  // Pass userId here
        binding.listItemHubs.layoutManager = LinearLayoutManager(this)
        binding.listItemHubs.adapter = hubAdapter
    }

    private fun fetchDataFromFirestore() {
        db.collection("hubs")
            .get()
            .addOnSuccessListener { result ->
                hubs.clear()
                for (document in result) {
                    val hub = document.toObject(Hub::class.java)
                    hubs.add(hub)
                }
                hubAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("HubsScreen", "Error getting documents.", exception)
            }
    }
}
