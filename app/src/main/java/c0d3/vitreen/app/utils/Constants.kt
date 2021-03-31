package c0d3.vitreen.app.utils

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Constants {
    companion object {
        const val KEYEMAIL = "KEYEMAIL"
        const val TAG = "C0D3"
        const val LocalisationCode = 15266
        val KEYADDADVERTS =
            listOf("KEYCATEGORY", "KEYTITLE", "KEYPRICE", "KEYLOCATION", "KEYDESCRIPTION")
    }
}