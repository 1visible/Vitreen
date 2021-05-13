package c0d3.vitreen.app.models.dto

import c0d3.vitreen.app.models.Entity
import c0d3.vitreen.app.models.Message


data class DiscussionDTO(
    val productId: String = "",
    val productName:String="",
    val lastMessage:Message
): Entity()
