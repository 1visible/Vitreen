package c0d3.vitreen.app.models.dto

import c0d3.vitreen.app.models.User
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.util.ArrayList

data class UserDTO(
        val id: String,
        val fullname: String,
        val emailAddress: String,
        val phoneNumber: String,
        val contactByPhone: Boolean = true,
        @field:JvmField
        val isProfessional: Boolean = false,
        val locationId: String,
        val companyName: String? = null,
        val siretNumber: String? = null,
        var productsId: ArrayList<String>? = null,
        val favoriteProductsId: ArrayList<String>? = null
) {
    constructor(document: QueryDocumentSnapshot) : this(
            document.id,
            document.get("fullname") as String,
            document.get("emailAddress") as String,
            document.get("phoneNumber") as String,
            document.get("contactByPhone") as Boolean,
            document.get("isProfessional") as Boolean,
            document.get("locationId") as String,
            document.get("companyName") as String?,
            document.get("siretNumber") as String?,
            document.get("productsId") as ArrayList<String>?,
            document.get("favoriteProductsId") as ArrayList<String>?
    )

    fun userDTOtoModel(): User {
        return User(
                fullname,
                emailAddress,
                phoneNumber,
                contactByPhone,
                isProfessional,
                locationId,
                companyName,
                siretNumber,
                productsId,
                favoriteProductsId
        )
    }
}
