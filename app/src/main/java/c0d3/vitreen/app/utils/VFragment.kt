package c0d3.vitreen.app.utils

import android.os.Bundle
import android.view.*
import android.view.View.VISIBLE
import androidx.annotation.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.MainActivity
import c0d3.vitreen.app.activities.observeOnce
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.loading_spinner.*

/**
 * Generic class for fragment, extends this class to create a pre-made fragment
 *
 * @property layoutId
 * @property hasOptionsMenu
 * @property topMenuId
 * @property requireAuth
 * @property loginNavigationId
 */
abstract class VFragment(
    @LayoutRes private val layoutId: Int,
    private val hasOptionsMenu: Boolean = false,
    @MenuRes private val topMenuId: Int = -1,
    private val requireAuth: Boolean = false,
    @IdRes private val loginNavigationId: Int = -1
) : Fragment() {

    val viewModel: FirestoreViewModel by activityViewModels()
    lateinit var menu: MutableLiveData<Menu>

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
        menu = viewModel.getMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        if (hasOptionsMenu)
            inflater.inflate(topMenuId, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        this.menu.value = menu
    }

    /**
     * Navigate to another Fragment with eventually some args
     *
     * @param destinationId
     * @param args
     */
    fun navigateTo(@IdRes destinationId: Int, vararg args: Pair<String, Any?>) {
        val bundle = bundleOf(*args)
        findNavController().navigate(destinationId, bundle)
    }

    /**
     * Check if any input is empty
     *
     * @param inputs
     * @return if any input is empty
     */
    private fun isAnyInputEmpty(vararg inputs: TextInputLayout?): Boolean {
        inputs.forEach { input ->
            if (input?.editText?.text.isNullOrBlank())
                return true
        }
        return false
    }

    /**
     * Check if all inputs are empty
     *
     * @param inputs
     * @return if all inputs are empty
     */
    fun areAllInputsEmpty(vararg inputs: TextInputLayout?): Boolean {
        inputs.forEach { input ->
            if (input != null && !input.editText?.text.isNullOrBlank()) {
                return false
            }
        }

        val input = inputs.last()

        if(input != null)
            input.error = if(input.editText?.text.isNullOrBlank()) "" else null

        return true
    }

    /**
     * Check if any required input is empty
     *
     * @param inputs
     * @return if any required input is empty
     */
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

    /**
     * Convert an input editText to String
     *
     * @param input
     * @return String
     */
    fun inputToString(input: TextInputLayout): String? {
        return if(isAnyInputEmpty(input)) null else input.editText?.text?.trim().toString()
    }

    /**
     * Show a snackbar with a message
     *
     * @param messageId
     */
    fun showSnackbarMessage(@StringRes messageId: Int) {
        (activity as? MainActivity)?.showMessage(messageId)
    }

    /**
     * Go back using activity onBackPressed()
     *
     * @return boolean
     */
    fun goBack(): Boolean {
        activity?.let { activity ->
            activity.onBackPressed()
            return true
        }

        return false
    }

    /**
     * Set an menu item visibility
     *
     * @param id
     * @param visible
     */
    fun setMenuItemVisible(@IdRes id: Int, visible: Boolean) {
        menu.observeOnce(viewLifecycleOwner, { menu ->
            menu.findItem(id)?.isVisible = visible
        })
    }

}