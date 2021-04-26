package c0d3.vitreen.app.models.dto

import c0d3.vitreen.app.models.Product

data class ProductDTO(
        val id: String,
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
) {
    fun ProductDTOToModel(): Product {
        return Product(
                title,
                description,
                price,
                brand,
                size,
                numberOfConsultations,
                reported,
                locationId,
                categoryId,
                ownerId,
                createdAt,
                modifiedAt
        )
    }
}
