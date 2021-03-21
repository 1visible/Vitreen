package c0d3.vitreen.app.models

data class Discussion(
    val senderId: String,
    val recipientId: String,
    val advertId: String,
    val createdAt: String,
    val content: String,
)
