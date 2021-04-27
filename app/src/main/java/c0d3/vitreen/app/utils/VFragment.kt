package c0d3.vitreen.app.utils

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import c0d3.vitreen.app.activities.MainActivity
import c0d3.vitreen.app.utils.Constants.Companion.CATEGORY_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.LOCATION_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.PRODUCT_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.USER_COLLECTION
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

abstract class VFragment(
    @LayoutRes private val layoutId: Int,
    @DrawableRes private val topIcon: Int,
    @StringRes private val topTitleId: Int = -1,
    private val hasOptionsMenu: Boolean = false,
    @MenuRes private val topMenuId: Int = -1,
    private val requireAuth: Boolean = false,
    @IdRes private val loginNavigationId: Int = -1
) : Fragment() {

    val db = Firebase.firestore
    val storage = Firebase.storage
    val auth = Firebase.auth
    var user: FirebaseUser? = auth.currentUser

    val usersCollection = db.collection(USER_COLLECTION)
    val categoriesCollection = db.collection(CATEGORY_COLLECTION)
    val locationsCollection = db.collection(LOCATION_COLLECTION)
    val productsCollection = db.collection(PRODUCT_COLLECTION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(hasOptionsMenu)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (requireAuth && (user == null || user!!.isAnonymous))
            navigateTo(loginNavigationId)
        return inflater.inflate(layoutId, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val topTitle: String = if (topTitleId == -1) "" else getString(topTitleId)
        (activity as MainActivity).setTopViewAttributes(topTitle, topIcon)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (hasOptionsMenu)
            inflater.inflate(topMenuId, menu)
    }

    fun navigateTo(@IdRes destinationId: Int, vararg args: Pair<String, Any?>) {
        val bundle = bundleOf(*args)
        findNavController().navigate(destinationId, bundle)
    }

    fun isAnyInputEmpty(vararg editTexts: EditText?): Boolean {
        editTexts.forEach { editText ->
            if (editText?.text.isNullOrBlank())
                return true
        }
        return false
    }

    fun isAnyStringEmpty(vararg texts: String?): Boolean {
        texts.forEach { text ->
            if (text.isNullOrBlank())
                return true
        }
        return false
    }

    fun showError(@StringRes errorId: Int) {
        Toast.makeText(context, getString(errorId), Toast.LENGTH_SHORT).show()
    }

}