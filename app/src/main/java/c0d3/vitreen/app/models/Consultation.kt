package c0d3.vitreen.app.models

import java.io.Serializable
import java.util.*

data class Consultation(
    var date: Date = Calendar.getInstance().time,
    var city: String = "",
): Serializable