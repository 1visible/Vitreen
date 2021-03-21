package c0d3.vitreen.app.models

import java.util.ArrayList

data class Users(
    val name: String,
    val first_name: String,
    val email: String,
    val password: String,
    val roleId: String,
    val company: String? = null,
    val siret: String? = null,
    val phone: String,
    val contactMethod: String? = null,
    val listAnnounces: ArrayList<String>? = null,
    val listSavedAnnounces: ArrayList<String>? = null,
)
