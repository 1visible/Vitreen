package c0d3.vitreen.app.models

import java.util.*

abstract class Entity (
    val id: String = UUID.randomUUID().toString()
)