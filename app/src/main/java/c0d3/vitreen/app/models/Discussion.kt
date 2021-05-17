package c0d3.vitreen.app.models

import java.io.Serializable

data class Discussion(
    val userId: String = "",
    val ownerId: String = "",
    val productId: String = "",
    val productName: String = "",
    val haveMessages: Boolean = false,
    val messages: ArrayList<Message> = ArrayList(),
    val usersIds: ArrayList<String> = arrayListOf(userId, ownerId)
) : Entity(), Serializable
