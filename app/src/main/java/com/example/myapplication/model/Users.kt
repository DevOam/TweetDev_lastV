package com.example.myapplication.model

data class User(
    val _id: String = "",
    val login: String = "",
    val password: String = "",
    val username: String = "",
    val roles: List<String> = listOf(),
    val profileImageUrl: String = "",
    val backgroundImageUrl: String = "",
    val joinDate: String = "",
    val followers: List<String> = listOf(),
    val following: List<String> = listOf(),
    val description: String = ""
)