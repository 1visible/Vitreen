package c0d3.vitreen.app.models

import java.io.Serializable
import java.util.ArrayList

data class User(
    var username: String = "",
    var emailAddress: String = "",
    var phoneNumber: String = "",
    var contactByPhone: Boolean = true,
    @field:JvmField
    var isProfessional: Boolean = false,
    var location: Location = Location(),
    var companyName: String? = null,
    var siretNumber: String? = null,
    val favoritesIds: ArrayList<String> = ArrayList(),
): Entity(), Serializable