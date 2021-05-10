package c0d3.vitreen.app.models

data class Discussion(
    val userId: String = "",
    val productId: String = "",
    val messages: ArrayList<Message> = ArrayList(),
): Entity()
