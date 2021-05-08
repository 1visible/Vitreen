package c0d3.vitreen.app.models.dto

import c0d3.vitreen.app.models.Location

data class LocationDTO(
    val id: String,
    val name: String,
    val zipCode: Long?
) {
    fun DtoToModel(): Location {
        return Location(name, zipCode)
    }
}
