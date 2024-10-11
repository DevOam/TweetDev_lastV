package com.example.myapplication.model


data class Post(
    var id: String = "",
    var profileImageUrl: String? = null,
    var username: String? = null,
    var creationDate: String? = null,
    var content: String? = null,
    var likes: String = "0",
    var isLikedByCurrentUser: Boolean = false,
    var comments: List<Comment> = listOf(),
) {
    val likesCount: Int
        get() = likes.toIntOrNull() ?: 0

    val commentsCount: Int
        get() = comments.size
}






