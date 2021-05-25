package c0d3.vitreen.app.models

import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

data class Product(
    var title: String = "",
    var description: String = "",
    var price: Double = 0.0,
    var brand: String? = null,
    var size: String? = null,
    val consultations: ArrayList<Consultation> = ArrayList(),
    val reporters: ArrayList<String> = ArrayList(),
    var location: Location = Location(),
    var category: Category = Category(),
    var imagesPaths: ArrayList<String> = ArrayList(),
    val ownerId: String = "",
    val modifiedAt: Date = Calendar.getInstance().time
) : Entity(), Serializable{
    override fun toString(): String {
        return "${title},${description},${price.toString()},${size},${location.city},${category.name}"
    }
}
