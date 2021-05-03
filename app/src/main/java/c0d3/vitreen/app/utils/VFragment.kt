package c0d3.vitreen.app.utils

import android.os.Bundle
import android.view.*
import android.view.View.GONE
import androidx.annotation.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.MainActivity
import c0d3.vitreen.app.utils.Constants.Companion.CATEGORY_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.LOCATION_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.PRODUCT_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.USER_COLLECTION
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.error_view.*


abstract class VFragment(
        @LayoutRes private val layoutId: Int,
        @DrawableRes private val topIcon: Int,
        @StringRes private val topTitleId: Int = -1,
        private val hasOptionsMenu: Boolean = false,
        @MenuRes private val topMenuId: Int = -1,
        private val requireAuth: Boolean = false,
        @IdRes private val loginNavigationId: Int = -1
) : Fragment() {

    private lateinit var db: FirebaseFirestore
    lateinit var storage: FirebaseStorage
    lateinit var auth: FirebaseAuth
    var user: FirebaseUser? = null

    lateinit var usersCollection: CollectionReference
    lateinit var categoriesCollection: CollectionReference
    lateinit var locationsCollection: CollectionReference
    lateinit var productsCollection: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(hasOptionsMenu)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        db = Firebase.firestore
        storage = Firebase.storage
        auth = Firebase.auth
        user = auth.currentUser

        usersCollection = db.collection(USER_COLLECTION)
        categoriesCollection = db.collection(CATEGORY_COLLECTION)
        locationsCollection = db.collection(LOCATION_COLLECTION)
        productsCollection = db.collection(PRODUCT_COLLECTION)

        if (requireAuth && user?.isAnonymous == true)
            navigateTo(loginNavigationId)

        return inflater.inflate(layoutId, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val topTitle: String = if (topTitleId == -1) "" else getString(topTitleId)
        (activity as? MainActivity)?.setTopViewAttributes(topTitle, topIcon)
        setSpinnerVisibility(GONE)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        if (hasOptionsMenu)
            inflater.inflate(topMenuId, menu)
    }

    fun navigateTo(@IdRes destinationId: Int, vararg args: Pair<String, Any?>) {
        val bundle = bundleOf(*args)
        findNavController().navigate(destinationId, bundle)
    }

    fun isAnyInputEmpty(vararg inputs: TextInputLayout?): Boolean {
        inputs.forEach { input ->
            if (input?.editText?.text.isNullOrBlank())
                return true
        }
        return false
    }

    fun isAnyRequiredInputEmpty(vararg inputs: TextInputLayout?): Boolean {
        var result = false
        inputs.forEach { input ->
            if (input != null && input.editText?.text.isNullOrBlank()) {
                input.error = getString(R.string.required_input)
                result = true
            }
        }
        return result
    }

    fun isAnyStringEmpty(vararg texts: String?): Boolean {
        texts.forEach { text ->
            if (text.isNullOrBlank())
                return true
        }
        return false
    }

    fun showError(@StringRes errorId: Int = R.string.error_placeholder) {
        (activity as? MainActivity)?.showError(errorId)
    }

    fun setErrorView(visibility: Int, @StringRes errorId: Int = R.string.error_placeholder) {
        errorView.visibility = visibility
        textViewError.text = getString(errorId)
    }

    fun setSpinnerVisibility(visibility: Int) {
        (activity as? MainActivity)?.setSpinnerVisibility(visibility)
    }

}