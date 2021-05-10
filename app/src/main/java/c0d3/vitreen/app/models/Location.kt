package c0d3.vitreen.app.models

data class Location(
    var city: String = "",
    var zipCode: Long? = null,
): Entity()
