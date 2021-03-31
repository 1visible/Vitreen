package c0d3.vitreen.app.models.dto

import c0d3.vitreen.app.models.User
import java.util.ArrayList

data class UserDTO(
    val id: String,
    val lastName: String,
    val firstName: String,
    val email: String,
    @field:JvmField
    val isProfessional: Boolean = false,
    val company: String? = null,
    val siret: String? = null,
    val phone: String,
    val contactMethod: String? = null,
    var advertsId: ArrayList<String>? = null,
    val favoriteAdvertsId: ArrayList<String>? = null,
) {
    fun userDTOtoModel(): User {
        return User(
            lastName,
            firstName,
            email,
            isProfessional,
            company,
            siret,
            phone,
            contactMethod,
            advertsId,
            favoriteAdvertsId
        )
    }
}
