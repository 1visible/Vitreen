package c0d3.vitreen.app.utils

import c0d3.vitreen.app.models.Category
import c0d3.vitreen.app.models.Location
import java.util.ArrayList

class SearchQuery(
    val title: String? = null,
    val priceMin: Double? = null,
    val priceMax: Double? = null,
    val brand: String? = null,
    val location: Location? = null,
    val category: Category? = null,
    val ownerId: String? = null,
    val ids: ArrayList<String>? = null
) {
    operator fun component1(): String? { return title }
    operator fun component2(): Double? { return priceMin }
    operator fun component3(): Double? { return priceMax }
    operator fun component4(): String? { return brand }
    operator fun component5(): Location? { return location }
    operator fun component6(): Category? { return category }
    operator fun component7(): String? { return ownerId }
    operator fun component8(): ArrayList<String>? { return ids }
}