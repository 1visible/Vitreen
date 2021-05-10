package c0d3.vitreen.app.models

data class Location(
    val city: String = "",
    var zipCode: Long? = null,
): Entity()
