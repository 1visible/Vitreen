package c0d3.vitreen.app.models

data class Advert(
    val title: String,
    val description: String,
    val price: Float,
    val brand: String,
    val size: Int,
    val numberOfConsultations: Int,
    val reported: Boolean,
)
