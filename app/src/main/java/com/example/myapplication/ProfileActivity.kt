package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.myapplication.model.User
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var usernameTextView: TextView
    private lateinit var followersCountTextView: TextView
    private lateinit var joinDtae: TextView
    private lateinit var img_profile: ImageView
    private lateinit var image_couverture: ImageView
    private lateinit var edit_profile: ImageView
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize the TextViews
        usernameTextView = findViewById(R.id.usernameTextView)
        followersCountTextView = findViewById(R.id.nbr_folowers)
        joinDtae = findViewById(R.id.since)
        img_profile = findViewById(R.id.image_profile)
        image_couverture = findViewById(R.id.image_couverture)
        edit_profile = findViewById(R.id.edit)

        // Retrieve the user ID from the intent
        userId = intent.getStringExtra("idUser")
        // Fetch user data if userId is available
            getUserData(userId!!)

        edit_profile.setOnClickListener{
            showEditNameDialog()
        }
    }

    // Function to get user data from Firestore
    private fun getUserData(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    if (user?._id == userId) {
                        // Utilisateur trouvé
                        usernameTextView.text = user.username
                        followersCountTextView.text = user.followers.size.toString()
                        joinDtae.text = formatJoinDate(user.joinDate)
                        Glide.with(this)
                            .load(user.profileImageUrl)
                            .into(img_profile)
                        Glide.with(this)
                            .load(user.profileImageUrl)
                            .into(image_couverture)
                        break // Sortir de la boucle une fois l'utilisateur trouvé
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

        val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is zero-based
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
    private fun showEditNameDialog() {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_name, null)
        val editNameInput = dialogView.findViewById<EditText>(R.id.editNameInput)

        builder.setTitle("Modifier le nom")
        builder.setView(dialogView)
        builder.setPositiveButton("Enregistrer") { dialog, which ->
            val newName = editNameInput.text.toString().trim() // Trim to remove extra spaces
            if (newName.isNotEmpty()) {
                userId?.let { updateUserName(it, newName)
//                    Toast.makeText(this, ""+it, Toast.LENGTH_SHORT).show()
                }
            } else {

//                Toast.makeText(this, "Le nom ne peut pas être vide", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Annuler") { dialog, which ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun updateUserName(userId: String, newName: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    if (user?._id == userId) {
                        // Utilisateur trouvé
                        document.reference.update("username", newName)
                            .addOnSuccessListener {
                                usernameTextView.text = newName // Mettre à jour la TextView
//                                Toast.makeText(this, "Nom mis à jour", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.w("ProfileActivity", "Error updating document", e)
//                                Toast.makeText(this, "Erreur lors de la mise à jour du nom", Toast.LENGTH_SHORT).show()
                            }
                        break // Sortir de la boucle une fois la mise à jour effectuée
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("ProfileActivity", "Error getting documents", e)
//                Toast.makeText(this, "Erreur lors de la recherche du document", Toast.LENGTH_SHORT).show()
            }
    }


}
