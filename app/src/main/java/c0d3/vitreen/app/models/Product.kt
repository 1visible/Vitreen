package c0d3.vitreen.app.models

data class Product(
    val title: String,
    val description: String,
    val price: Long,
    val brand: String,
    val size: String? = null,
    val numberOfConsultations: Long = 0,
    val reported: ArrayList<String>? = null,
    val locationId: String,
    val categoryId: String,
    val nbImages: Long,
    val ownerId: String,
    val createdAt: String,
    val modifiedAt: String,
)