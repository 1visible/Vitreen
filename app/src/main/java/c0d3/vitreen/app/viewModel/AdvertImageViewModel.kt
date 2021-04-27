package c0d3.vitreen.app.viewModel

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AdvertImageViewModel : ViewModel() {
    val advertImages: MutableLiveData<ArrayList<Bitmap>> by lazy {
        MutableLiveData<ArrayList<Bitmap>>()
    }

}