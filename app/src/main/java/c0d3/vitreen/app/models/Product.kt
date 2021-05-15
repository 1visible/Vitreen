package c0d3.vitreen.app.models

import android.graphics.Bitmap
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

@IgnoreExtraProperties
data class Product(
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val brand: String? = null,
    val size: String? = null,
    val consultations: ArrayList<Consultation> = ArrayList(),
    val reporters: ArrayList<String> = ArrayList(),
    val location: Location = Location(),
    val category: Category = Category(),
    var imagesPaths: ArrayList<String> = ArrayList(),
    val ownerId: String = "",
    val modifiedAt: Date = Calendar.getInstance().time,
    @Exclude
    @set:Exclude
    @get:Exclude
    var images: ArrayList<Bitmap> = ArrayList()
) : Entity(), Serializable
