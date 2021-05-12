package c0d3.vitreen.app.utils

import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.MainActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.empty_view.*


abstract class VFragment(
    @LayoutRes private val layoutId: Int,
    @DrawableRes private val topIcon: Int,
    @StringRes private val topTitleId: Int = -1,
    private val hasOptionsMenu: Boolean = false,
    @MenuRes private val topMenuId: Int = -1,
    private val requireAuth: Boolean = false,
    @IdRes private val loginNavigationId: Int = -1
) : Fragment() {

    private lateinit var menu: Menu
    lateinit var viewModel: FirestoreViewModel
    lateinit var auth: FirebaseAuth
    var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(hasOptionsMenu)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        viewModel = ViewModelProvider(this).get(FirestoreViewModel::class.java)
        auth = Firebase.auth
        user = auth.currentUser

        if (requireAuth && !isUserSignedIn())
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

        this.menu = menu
    }

    fun isUserSignedIn(): Boolean {
        try {
            if (user == null || user!!.isAnonymous)
                return false
        } catch (_: NullPointerException) {
            return false
        }
        return true
    }

    fun navigateTo(@IdRes destinationId: Int, vararg args: Pair<String, Any?>) {
        val bundle = bundleOf(*args)
        findNavController().navigate(destinationId, bundle)
    }

    private fun isAnyInputEmpty(vararg inputs: TextInputLayout?): Boolean {
        inputs.forEach { input ->
            if (input?.editText?.text.isNullOrBlank())
                return true
        }
        return false
    }

    fun areAllInputsEmpty(vararg inputs: TextInputLayout?): Boolean {
        inputs.forEach { input ->
            if (input != null && input.editText?.text.isNullOrBlank()) {
                return false
            }
        }

        val input = inputs.last()

        if(input != null)
            input.error = if(input.editText?.text.isNullOrBlank()) "" else null

        return true
    }

    fun isAnyRequiredInputEmpty(vararg inputs: TextInputLayout?): Boolean {
        var result = false
        inputs.forEach { input ->
            if(input != null)
                if (input.editText?.text.isNullOrBlank()) {
                    input.error = getString(R.string.required_input)
                    result = true
                } else
                    input.error = null
        }
        return result
    }

    fun inputToString(input: TextInputLayout): String? {
        return if(isAnyInputEmpty(input)) null else input.editText?.text?.trim().toString()
    }

    fun showMessage(@StringRes messageId: Int = R.string.error_placeholder) {
        (activity as? MainActivity)?.showMessage(messageId)
    }

    fun goBack(): Boolean {
        activity?.let { activity ->
            activity.onBackPressed()
            return true
        }

        return false
    }

    fun setEmptyView(visibility: Int, @StringRes messageId: Int = R.string.error_placeholder) {
        emptyView.visibility = visibility
        if(visibility == VISIBLE)
            textViewEmpty.text = getString(messageId)
    }

    fun setSpinnerVisibility(visibility: Int) {
        (activity as? MainActivity)?.setSpinnerVisibility(visibility)
    }

    fun handleError(@StringRes exception: Int, @StringRes messageId: Int = -1): Boolean {
        if(exception == -1) return false
        showMessage(exception)
        setSpinnerVisibility(GONE)
        if(messageId != -1) setEmptyView(VISIBLE, messageId)
        return true
    }

    fun setIconVisibility(@IdRes id: Int, visible: Boolean) {
        menu.findItem(id)?.isVisible = visible
    }

}