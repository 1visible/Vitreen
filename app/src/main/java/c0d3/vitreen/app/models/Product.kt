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
    val consultations: ArrayList<Consultation> = ArrayList(),
    val reported: ArrayList<String> = ArrayList(),
    val location: Location = Location(),
    val category: Category = Category(),
    val nbImages: Long = 0,
    val ownerId: String = "",
    val modifiedAt: String = Calendar.getInstance().time.toString(),
) : Entity() {

    fun toDTO(): ProductDTO {
        val productDTO = ProductDTO(title, price, location, category)
        productDTO.id = this.id

        return productDTO
    }

}
