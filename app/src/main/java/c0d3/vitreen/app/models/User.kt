package c0d3.vitreen.app.models

import java.util.ArrayList

data class User(
    val lastName: String,
    val firstName: String,
    val email: String,
    val password: String,
    val roleId: String,
    val company: String? = null,
    val siret: String? = null,
    val phone: String,
    val contactMethod: String? = null,
    val listofAdverts: ArrayList<String>? = null,
    val listSavedAdverts: ArrayList<String>? = null,
)
