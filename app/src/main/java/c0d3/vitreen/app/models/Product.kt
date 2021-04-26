package c0d3.vitreen.app.models

data class Product(
    val title: String,
    val description: String,
    val price: Float,
    val brand: String,
    val size: String? = null,
    val numberOfConsultations: Int = 0,
    val reported: ArrayList<String>? = null,
    val locationId: String,
    val categoryId: String,
    val ownerId: String,
    val createdAt: String,
    val modifiedAt: String,
)
