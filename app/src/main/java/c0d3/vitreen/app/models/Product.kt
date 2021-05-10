package c0d3.vitreen.app.models

import c0d3.vitreen.app.models.dto.ProductDTO
import java.util.*
import kotlin.collections.ArrayList

data class Product(
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val brand: String? = null,
    val size: String? = null,
    val numberOfConsultations: Long = 0,
    val reported: ArrayList<String> = ArrayList(),
    val location: Location = Location(),
    val category: Category = Category(),
    val nbImages: Long = 0,
    val ownerId: String = "",
    val modifiedAt: String = Calendar.getInstance().time.toString(),
) : Entity() {
    fun productToDTO(): ProductDTO {
        return ProductDTO(id, title, price, location, category)
    }
}
