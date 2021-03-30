package c0d3.vitreen.app.models.dto

import c0d3.vitreen.app.models.Category

data class CategoryDTO(
    val id: String,
    val name: String
) {
    constructor() : this(
        "", ""
    )

    fun DtoToModel(): Category {
        return Category(this.name)
    }

    override fun toString(): String {
        return "id:${id}," +
                "name: ${name}"
    }
}
