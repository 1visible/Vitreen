package c0d3.vitreen.app.fragments.addadvert.step1

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import c0d3.vitreen.app.R
import c0d3.vitreen.app.fragments.addadvert.step2.AddAdvert2Fragment
import c0d3.vitreen.app.listeners.FetchLocation
import c0d3.vitreen.app.listeners.OnLocationFetchListner
import c0d3.vitreen.app.models.Location
import c0d3.vitreen.app.models.dto.CategoryDTO
import c0d3.vitreen.app.utils.Constants
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 * Use the [AddAdvert1Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddAdvert1Fragment : Fragment() {

    private lateinit var category: TextInputLayout
    private lateinit var title: EditText
    private lateinit var price: EditText
    private lateinit var location: EditText
    private lateinit var description: EditText
    private lateinit var nextButton: Button

    private val user = Firebase.auth.currentUser
    private val DB = Firebase.firestore
    private val categories = DB.collection("Categories")
    private val categoriesList = ArrayList<CategoryDTO>()
    private val locations = DB.collection("locations")

    private lateinit var locationManager: LocationManager

    private var categoryId = ""
    private lateinit var cityName: String
    private lateinit var zipCode: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_add_advert1, container, false)
        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        cityName = ""
        zipCode = ""
        category = root.findViewById(R.id.categories)
        title = root.findViewById<EditText>(R.id.editTextTitle)
        price = root.findViewById<EditText>(R.id.editTextPrix)
        location = root.findViewById<EditText>(R.id.editTextLocalisation)
        description = root.findViewById<EditText>(R.id.editTextDescription)
        nextButton = root.findViewById<Button>(R.id.nextButton2)
        categories.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val categoryDTO = CategoryDTO(document.id, document.get("name").toString())
                    categoriesList.add(categoryDTO)
                }
                initializeLocation()
                val adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.list_item,
                    categoriesList.map { it.DtoToModel().name })
                (category.editText as? AutoCompleteTextView)?.setAdapter(adapter)

                nextButton.setOnClickListener {
                    if (
                        !category.editText?.text.toString().replace("\\s", "").equals("")
                        &&
                        !title.text.toString().replace("\\s", "").equals("")
                        &&
                        !price.text.toString().replace("\\s", "").equals("")
                        &&
                        !location.text.toString().replace("\\s", "").equals("")
                        &&
                        !description.text.toString().replace("\\s", "").equals("")
                    ) {
                        val currentLocation = Location(
                            location.text.toString(),
                            if (zipCode == "") null else zipCode.toInt()
                        )
                        locations
                            .whereEqualTo("name", currentLocation.name)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (documents.size() == 1) {
                                    for (document in documents) {
                                        if (document.get("zipCode") == null) {
                                            locations
                                                .document(document.id)
                                                .update("zipCode", currentLocation.zipCode)
                                        }

                                        categoriesList.forEach { it ->
                                            if (it.name.equals(category.editText?.text.toString())) {
                                                categoryId = it.id
                                            }
                                        }

                                        println("------------------------------------------------------------")
                                        println("categorie = $categoryId")
                                        println("titre = ${title.text.toString()}")
                                        println("prix = ${price.text.toString()}")
                                        println("localisation = ${document.id}")
                                        println("description = ${description.text.toString()}")
                                        println("------------------------------------------------------------")

                                        val fragmentManager =
                                            requireActivity().supportFragmentManager
                                        val fragmentTransaction = fragmentManager.beginTransaction()
                                        if (container != null) {
                                            fragmentTransaction.replace(
                                                container.id,
                                                AddAdvert2Fragment()
                                            )
                                            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                            fragmentTransaction.addToBackStack(null)
                                            fragmentTransaction.commit()

                                        }
//                                        val intent1 = Intent(this, DropAdvert2Activity::class.java)
//                                        var categoId: String? = null
//                                        for (cat in categoriesList) {
//                                            if (cat.name.equals(category.editText?.text.toString())) {
//                                                categoId = cat.id
//                                            }
//                                        }
//                                        intent1.putExtra(
//                                            Constants.KEYADDADVERTS[0],
//                                            categoId
//                                        )
//                                        intent1.putExtra(
//                                            Constants.KEYADDADVERTS[1],
//                                            title.text.toString()
//                                        )
//                                        intent1.putExtra(
//                                            Constants.KEYADDADVERTS[2],
//                                            price.text.toString()
//                                        )
//                                        intent1.putExtra(
//                                            Constants.KEYADDADVERTS[3],
//                                            document.id
//                                        )
//                                        intent1.putExtra(
//                                            Constants.KEYADDADVERTS[4],
//                                            description.text.toString()
//                                        )
//                                        startActivity(intent1)
//                                        finish()
                                    }
                                } else {
                                    locations.add(currentLocation)
                                        .addOnSuccessListener { document ->
//                                            val intent1 =
//                                                Intent(this, DropAdvert2Activity::class.java)
//                                            var categoId: String? = null
//                                            for (cat in categoriesList) {
//                                                if (cat.name.equals(category.editText?.text.toString())) {
//                                                    categoId = cat.id
//                                                }
//                                            }
//                                            intent1.putExtra(
//                                                Constants.KEYADDADVERTS[0],
//                                                categoId
//                                            )
//                                            intent1.putExtra(
//                                                Constants.KEYADDADVERTS[1],
//                                                title.text.toString()
//                                            )
//                                            intent1.putExtra(
//                                                Constants.KEYADDADVERTS[2],
//                                                price.text.toString()
//                                            )
//                                            intent1.putExtra(
//                                                Constants.KEYADDADVERTS[3],
//                                                document.id
//                                            )
//                                            intent1.putExtra(
//                                                Constants.KEYADDADVERTS[4],
//                                                description.text.toString()
//                                            )
//                                            startActivity(intent1)
//                                            finish()

                                        }
                                        .addOnFailureListener { exception ->
                                            Toast.makeText(
                                                requireContext(),
                                                getString(R.string.ErrorMessage),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }.addOnFailureListener { exception ->
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.ErrorMessage),
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }

                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.emptyFields),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        return root
    }

    inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> Unit) {
        val fragmentTransaction = beginTransaction()
        fragmentTransaction.func()
        fragmentTransaction.commit()
    }

    private fun initializeLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    Constants.LocalisationCode
                )
            }
        }
        val fetchLocation = FetchLocation();
        val listern = object : OnLocationFetchListner {
            override fun OnComplete(location: android.location.Location?) {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                try {
                    val adresse = location?.let {
                        geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )
                    }
                    if (adresse != null) {
                        cityName = adresse[0].locality
                        zipCode = adresse[0].postalCode
                        this@AddAdvert1Fragment.location.text.clear()
                        this@AddAdvert1Fragment.location.text = adresse[0].locality.toEditable()
                    }
                } catch (e: IOException) {
                    Toast.makeText(context, getString(R.string.ErrorMessage), Toast.LENGTH_SHORT)
                        .show()

                }
            }

            override fun OnFailed(e: String?) {
                Toast.makeText(context, getString(R.string.noLocation), Toast.LENGTH_SHORT).show()
            }

        }
        fetchLocation.setOnLocationFetchListner(listern, requireContext())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.LocalisationCode -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    location.text.clear()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.locationDenied),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
//                    startActivity(intent)
                }
            }
        }
    }

    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    companion object {
        fun newInstance(): AddAdvert1Fragment = AddAdvert1Fragment()
    }
}