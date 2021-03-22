package c0d3.vitreen.app.models

data class Discussion(
    val userId: String,
    val advertId: String,
    val messages: ArrayList<Message>,
)
