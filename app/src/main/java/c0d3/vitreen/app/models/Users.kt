package c0d3.vitreen.app.models

import java.util.ArrayList

data class Users(
    val Name: String,
    val First_name: String,
    val Email: String,
    val Password: String,
    val RoleId: String,
    val Company: String? = null,
    val SIRET: String? = null,
    val Phone: String,
    val contactMethod: String? = null,
    val ListAnnounces: ArrayList<String>? = null,
    val ListSavedAnnounces: ArrayList<String>? = null,
)
