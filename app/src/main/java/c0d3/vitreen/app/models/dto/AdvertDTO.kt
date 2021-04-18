package c0d3.vitreen.app.models.dto

import c0d3.vitreen.app.models.Advert

data class AdvertDTO(
    val id: String,
    val title: String,
    val description: String,
    val price: Long,
    val brand: String,
    val size: String? = null,
    val numberOfConsultations: Long = 0,
    val reported: ArrayList<String>? = null,
    val locationId: String,
    val categoryId: String,
    val nbImages: Long,
    val ownerId: String,
    val createdAt: String,
    val modifiedAt: String,
) {
    fun AdvertDTOToModel(): Advert {
        return Advert(
            title,
            description,
            price,
            brand,
            size,
            numberOfConsultations,
            reported,
            locationId,
            categoryId,
            nbImages,
            ownerId,
            createdAt,
            modifiedAt
        )
    }
}
