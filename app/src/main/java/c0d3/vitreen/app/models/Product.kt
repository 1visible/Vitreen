package c0d3.vitreen.app.models

data class Product(
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val brand: String = "",
    val size: String? = null,
    val numberOfConsultations: Long = 0,
    val reported: ArrayList<String> = ArrayList(),
    val location: Location = Location(),
    val category: Category = Category(),
    val nbImages: Long = 0,
    val ownerId: String = "",
    val createdAt: String = "",
    val modifiedAt: String = "",
): Entity()
