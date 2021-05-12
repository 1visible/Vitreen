package c0d3.vitreen.app.models

import java.io.Serializable

data class Location(
    var city: String = "",
    var zipCode: Long? = null,
): Entity(), Serializable
