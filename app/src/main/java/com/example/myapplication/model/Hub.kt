package com.example.myapplication.model


data class Hub(
    val _id: String = "",
    val name: String = "",
    val description: String = "",
    val posts: List<String> = emptyList(),
    val users: List<String> = emptyList(),
    val profileImageUrl: String = "",
    val coverImageUrl: String = "",
    val creationDate: String = "",
    val admins: List<String> = emptyList()
)