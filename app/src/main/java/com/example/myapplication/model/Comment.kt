package com.example.myapplication.model


data class Comment(
    val userId: String = "", // Default value for Firestore compatibility
    val text: String = ""    // Default value for Firestore compatibility
)
