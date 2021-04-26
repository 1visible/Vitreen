package c0d3.vitreen.app.models

import java.util.ArrayList

data class User(
    val fullname: String,
    val emailAddress: String,
    val phoneNumber: String,
    val contactByPhone: Boolean = true,
    @field:JvmField
    val isProfessional: Boolean = false,
    val locationId: String,
    val companyName: String? = null,
    val siretNumber: String? = null,
    val productsId: ArrayList<String>? = null,
    val favoriteProductsId: ArrayList<String>? = null
)
