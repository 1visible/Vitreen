package c0d3.vitreen.app.utils

import android.os.Bundle
import android.view.*
import android.view.View.VISIBLE
import androidx.annotation.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.MainActivity
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.loading_spinner.*

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
    val viewModel: FirestoreViewModel by activityViewModels()

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

        if (requireAuth && !viewModel.isUserSignedIn)
            navigateTo(loginNavigationId)

        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Show loading spinner
        loadingSpinner?.visibility = VISIBLE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val topTitle: String = if (topTitleId == -1) "" else getString(topTitleId)
        (activity as? MainActivity)?.setTopViewAttributes(topTitle, topIcon)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        if (hasOptionsMenu)
            inflater.inflate(topMenuId, menu)

        this.menu = menu
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

    fun showSnackbarMessage(@StringRes messageId: Int) {
        (activity as? MainActivity)?.showMessage(messageId)
    }

    fun goBack(): Boolean {
        activity?.let { activity ->
            activity.onBackPressed()
            return true
        }

        return false
    }

    fun setMenuItemVisibile(@IdRes id: Int, visible: Boolean) {
        menu.findItem(id)?.isVisible = visible
    }

}