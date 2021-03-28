package c0d3.vitreen.app.activities.adverts.drop

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import c0d3.vitreen.app.R
import com.google.android.material.textfield.TextInputLayout

class DropAdvertActivity : AppCompatActivity() {

    private lateinit var category:TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drop_advert)
    }
}