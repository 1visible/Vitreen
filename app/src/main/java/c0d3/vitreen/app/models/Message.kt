package c0d3.vitreen.app.models

import com.google.type.DateTime

data class Message(
    val senderId: String,
    val date: DateTime,
    val contentId: String
)
