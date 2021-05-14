package c0d3.vitreen.app.fragments.profile

import android.content.DialogInterface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.observeOnce
import c0d3.vitreen.app.adapter.ProductAdapter
import c0d3.vitreen.app.models.Product
import c0d3.vitreen.app.models.User
import c0d3.vitreen.app.utils.VFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.empty_view.*
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_product.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.recyclerViewProducts
import kotlinx.android.synthetic.main.loading_spinner.*

class ProfileFragment : VFragment(
    layoutId = R.layout.fragment_profile,
    topIcon = R.drawable.bigicon_profile,
    hasOptionsMenu = true,
    topMenuId = R.menu.menu_profile,
    requireAuth = true,
    loginNavigationId = R.id.from_profile_to_login
) {

    private var user: User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If user is not signed in, skip this part
        if (!viewModel.isUserSignedIn) {
            goBack() // TODO Vérifier si ça fout pas la merde avec navigateTo
            return
        }

        // Set elements visibility (while loading)
        profileDetails.visibility = GONE
        setMenuItemVisibile(R.id.logout, false)

        // Get current user informations
        viewModel.user.observe(viewLifecycleOwner, { (exception, user) ->
            if(exception != -1) {
                this.user = null
                showSnackbarMessage(exception)
                goBack()
                return@observe
            }

            this.user = user

            fillProfile(user)

            try {
                viewModel.getProducts(limit = false, ownerId = user.id!!)
            } catch (_: NullPointerException) {
                showSnackbarMessage(R.string.NetworkException)
                goBack()
                return@observe
            }
        })

        viewModel.products.observe(viewLifecycleOwner, { (exception, products) ->
            // When the call finishes, hide loading spinner
            loadingSpinner.visibility = GONE

            // If the call failed: show error message and show empty view
            if(exception != -1) {
                showSnackbarMessage(exception)
                goBack()
                return@observe
            }

            // If there are no products: show empty view
            if(products.isNullOrEmpty()) {
                recyclerViewProducts.visibility = GONE
                textViewNoProducts.visibility = VISIBLE
                profileDetails.visibility = VISIBLE
                return@observe
            }

            // Else, display products in the recycler view
            val adapter = ProductAdapter { product -> adapterOnClick(product) }
            adapter.submitList(products)
            recyclerViewProducts.adapter = adapter
            textViewNoProducts.visibility = GONE
            recyclerViewProducts.visibility = VISIBLE
            profileDetails.visibility = VISIBLE
        })



        // On delete button click, delete user account
        buttonDeleteAccount.setOnClickListener {
            try {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.delete_account))
                    .setMessage(getString(R.string.delete_account_question))
                    .setNeutralButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
                    .setPositiveButton(getString(R.string.delete)){ dialog, _ -> deleteAccount(dialog) }
                    .show()
            } catch (_: IllegalStateException) {
                showSnackbarMessage(R.string.error_placeholder)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                viewModel.signOut()
                goBack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun adapterOnClick(product: Product) {
        viewModel.select(product)
        navigateTo(R.id.from_profile_to_product)
    }

    private fun fillProfile(user: User) {
        // Fill personal informations
        textViewUsername.text = user.username
        textViewEmailAddress.text = user.emailAddress
        textViewPhoneNumber.text = user.phoneNumber
        val zipCode = if(user.location.zipCode == null) "?" else user.location.zipCode.toString()
        textViewPostalAddress.text = getString(R.string.location_template, user.location.city, zipCode)

        // Remove checkmark on least prefered contact method
        if (user.contactByPhone)
            textViewEmailAddress.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.icon_envelope,
                0,
                0,
                0
            )
        else
            textViewPhoneNumber.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.icon_phone,
                0,
                0,
                0
            )

        // Fill professional informations (and show section)
        if(user.isProfessional) {
            textViewCompanyName.text = user.companyName
            textViewSiretNumber.text = user.siretNumber
            textViewProfessionalInformations.visibility = VISIBLE
            textViewCompanyName.visibility = VISIBLE
            textViewSiretNumber.visibility = VISIBLE
        } else {
            textViewProfessionalInformations.visibility = GONE
            textViewCompanyName.visibility = GONE
            textViewSiretNumber.visibility = GONE
        }
    }

    private fun deleteAccount(dialog: DialogInterface) {
        // If user is not signed in, skip this part
        if(!viewModel.isUserSignedIn) {
            showSnackbarMessage(R.string.SignedOutException)
            try { dialog.dismiss() } catch (_: Exception) { }
            goBack()
            return
        }

        viewModel.deleteUser(user!!).observeOnce(viewLifecycleOwner, { exception ->
            if(exception == -1)
                showSnackbarMessage(R.string.account_deleted)
            else
                showSnackbarMessage(exception)

            try { dialog.dismiss() } catch (_: Exception) { }
            goBack()
        })
    }
}