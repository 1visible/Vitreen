package c0d3.vitreen.app.models

import java.util.ArrayList

data class User(
    val username: String = "",
    val emailAddress: String = "",
    val phoneNumber: String = "",
    val contactByPhone: Boolean = true,
    @field:JvmField
    val isProfessional: Boolean = false,
    val location: Location = Location(),
    val companyName: String? = null,
    val siretNumber: String? = null,
    val favoritesIds: ArrayList<String> = ArrayList(),
): Entity()