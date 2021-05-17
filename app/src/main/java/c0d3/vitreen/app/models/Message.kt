package c0d3.vitreen.app.models

import java.io.Serializable
import java.util.*

data class Message(
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val date: Date = Calendar.getInstance().time
): Serializable
