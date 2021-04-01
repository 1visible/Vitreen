package c0d3.vitreen.app.models.dto

import c0d3.vitreen.app.models.User
import java.util.ArrayList

data class UserDTO(
        val id: String,
        val fullname: String,
        val emailAddress: String,
        val phoneNumber: String,
        val contactByPhone: Boolean = true,
        @field:JvmField
    val isProfessional: Boolean = false,
        val companyName: String? = null,
        val siretNumber: String? = null,
        var advertsId: ArrayList<String>? = null,
        val favoriteAdvertsId: ArrayList<String>? = null
) {
    fun userDTOtoModel(): User {
        return User(
            fullname,
            emailAddress,
            phoneNumber,
            contactByPhone,
            isProfessional,
            companyName,
            siretNumber,
            advertsId,
            favoriteAdvertsId
        )
    }
}
