package c0d3.vitreen.app.models

import android.graphics.Bitmap
import c0d3.vitreen.app.models.dto.ProductDTO
import com.google.firebase.firestore.Exclude
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

data class Product(
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val brand: String? = null,
    val size: String? = null,
    val consultations: ArrayList<Consultation> = ArrayList(),
    val reporters: ArrayList<String> = ArrayList(),
    val location: Location = Location(),
    val category: Category = Category(),
    var imagesPaths: ArrayList<String> = ArrayList(),
    val ownerId: String = "",
    val modifiedAt: Date = Calendar.getInstance().time,
    @Exclude
    @set:Exclude
    @get:Exclude
    var images: List<Bitmap> = ArrayList()
) : Entity(), Serializable {

    fun toDTO(): ProductDTO {
        val productDTO = ProductDTO(title, price, images.firstOrNull(), location, category)
        productDTO.id = this.id

        return productDTO
    }

}
