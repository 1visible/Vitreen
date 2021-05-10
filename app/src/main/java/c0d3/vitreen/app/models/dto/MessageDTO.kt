package c0d3.vitreen.app.models.dto

import c0d3.vitreen.app.models.Message


data class MessageDTO(
    val id:String,
    val productId: String = "",
    val productName:String="",
    val lastMessage:Message
)
