package c0d3.vitreen.app.models

import java.util.*

data class Message(
    val senderId: String = "",
    val content: String = "",
    val date: String = Calendar.getInstance().time.toString()
)
