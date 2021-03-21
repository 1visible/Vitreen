package c0d3.vitreen.app.models

data class Advert(
    val title: String,
    val description: String,
    val price: Float,
    val brand: String,
    val size: String?,
    val numberOfConsultations: Int?,
    val reported: Boolean,
    val locationId: String,
    val categoryId: String,
    val ownerId: String,
    val createdAt: String,
    val modifiedAt: String,
)
