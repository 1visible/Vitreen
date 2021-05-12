package c0d3.vitreen.app.models.dto

import c0d3.vitreen.app.models.Category
import c0d3.vitreen.app.models.Entity
import c0d3.vitreen.app.models.Location

data class ProductDTO(
    val title: String = "",
    val price: Double = 0.0,
    val imagePath: String? = null,
    val location: Location = Location(),
    val category: Category = Category(),
): Entity()
