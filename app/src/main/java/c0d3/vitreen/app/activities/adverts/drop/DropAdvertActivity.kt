package c0d3.vitreen.app.activities.adverts.drop

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import c0d3.vitreen.app.Constantes
import c0d3.vitreen.app.MainActivity
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.Category
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class DropAdvertActivity : AppCompatActivity() {

    private lateinit var category: TextInputLayout
    private lateinit var title: EditText
    private lateinit var price: EditText
    private lateinit var localisation: EditText
    private lateinit var description: EditText
    private lateinit var nextButton: Button

    private val user = Firebase.auth.currentUser
    private val DB = Firebase.firestore
    private val categories = DB.collection("Categories")
    private val categoriesList = ArrayList<String>()

    private lateinit var locationManager: LocationManager

    private lateinit var provider: String
    private lateinit var cityName: String
    private lateinit var countryCode: String

    private var flag = false

    override fun onStart() {
        super.onStart()

        if ((user == null)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        if ((user != null) && user.isAnonymous) {
            //redirige l'utilisateur vers une inscription non anonyme
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drop_advert)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        cityName = ""
        countryCode = ""
        category = findViewById(R.id.categories)
        title = findViewById<EditText>(R.id.editTextTitle)
        price = findViewById<EditText>(R.id.editTextPrix)
        localisation = findViewById<EditText>(R.id.editTextLocalisation)
        description = findViewById<EditText>(R.id.editTextDescription)
        nextButton = findViewById<Button>(R.id.nextButton2)
        getAllCategory()
        initializeLocation()
        val adapter = ArrayAdapter(this, R.layout.list_item, categoriesList)
        (category.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        if (!cityName.equals("")) {
            localisation.text = cityName.toEditable()
        }
    }

    private fun getAllCategory() {
        categories.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    categoriesList.add(document.get("name").toString())
                }
            }
    }

    private fun addSomeCategories() {
        categories.document().set(Category("Multimédia"))
        categories.document().set(Category("Vêtements"))
    }

    private fun initializeLocation() {
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.isAltitudeRequired = true
        criteria.isBearingRequired = true
        criteria.isSpeedRequired = false
        criteria.isCostAllowed = false
        criteria.powerRequirement = Criteria.POWER_LOW
        provider = locationManager.getBestProvider(criteria, true).toString()
        Log.d(Constantes.TAG, "GPS  meilleur fournisseur ${provider}")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    Constantes.LocalisationCode
                )
            }
            return
        }
        val lastKnownLocation = locationManager.getLastKnownLocation(provider)
        if (lastKnownLocation != null) {
            val geocoder = Geocoder(this, Locale.getDefault())
            try {
                val adresses = geocoder.getFromLocation(
                    lastKnownLocation.latitude,
                    lastKnownLocation.longitude,
                    1
                )
                cityName = adresses.get(0).locality
                countryCode = adresses.get(0).countryCode
            } catch (e: IOException) {
                Toast.makeText(this, getString(R.string.ErrorMessage), Toast.LENGTH_SHORT)
                    .show()

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constantes.LocalisationCode -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    localisation.text.clear()
                    Toast.makeText(this, getString(R.string.locationDenied), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    startActivity(intent)
                }
            }
        }
    }

    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)
}