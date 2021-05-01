package c0d3.vitreen.app.utils

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProductImageViewModel : ViewModel() {
    val advertImages: MutableLiveData<ArrayList<Bitmap>> by lazy {
        MutableLiveData<ArrayList<Bitmap>>()
    }

}