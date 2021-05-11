package c0d3.vitreen.app.models

import com.google.firebase.firestore.Exclude

abstract class Entity (
    @Exclude
    var id: String? = null
)