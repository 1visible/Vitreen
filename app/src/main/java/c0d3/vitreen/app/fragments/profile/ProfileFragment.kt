package c0d3.vitreen.app.fragments.profile

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import c0d3.vitreen.app.R
import c0d3.vitreen.app.adapter.ProductAdapter
import c0d3.vitreen.app.models.User
import c0d3.vitreen.app.models.dto.ProductDTO
import c0d3.vitreen.app.utils.Constants.Companion.KEY_PRODUCT_ID
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.recyclerViewProducts


class ProfileFragment : VFragment(
    layoutId = R.layout.fragment_profile,
    topIcon = R.drawable.bigicon_profile,
    hasOptionsMenu = true,
    topMenuId = R.menu.menu_profile,
    requireAuth = true,
    loginNavigationId = R.id.action_navigation_profile_to_navigation_login
) {

    private var userDTO: User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show loading spinner and hide empty view
        setSpinnerVisibility(VISIBLE)
        setEmptyView(GONE)

        // If user is not signed in, skip this part
        if (!isUserSignedIn())
            return

        // Get current user informations
        viewModel.getUser(user!!).observeOnce(viewLifecycleOwner, { pair ->
            val errorCode = pair.first
            val user = pair.second
            // If the call fails, show error message, hide loading spinner and show empty view
            if (handleError(errorCode, R.string.error_placeholder)) return@observeOnce

            // Else, fill the profile with user informations and store them
            showProducts(user.productsIds)
            fillProfile(user)
            userDTO = user
        })

        // On delete button click, delete user account
        buttonDeleteAccount.setOnClickListener {
            userDTO?.let { user -> deleteAccount(user) }
        }
    }

    // TODO : Ajouter les items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                navigateTo(R.id.action_navigation_profile_to_navigation_home)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /* Opens product detail when RecyclerView item is clicked. */
    private fun adapterOnClick(product: ProductDTO) {
        navigateTo(
            R.id.action_navigation_profile_to_navigation_product,
            KEY_PRODUCT_ID to product.id
        )
    }

    private fun fillProfile(user: User) {
        // Fill personal informations
        textViewFullname.text = user.fullname
        textViewEmailAddress.text = user.emailAddress
        textViewPhoneNumber.text = user.phoneNumber
        textViewPostalAddress.text =
            getString(R.string.location_template, user.location.name, user.location.zipCode)

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

        // Show personal informations section
        textViewPersonalInformations.visibility = VISIBLE
        textViewFullname.visibility = VISIBLE
        textViewEmailAddress.visibility = VISIBLE
        textViewPhoneNumber.visibility = VISIBLE
        textViewPostalAddress.visibility = VISIBLE

        if (!user.isProfessional)
            return

        // Fill professional informations (and show section)
        textViewCompanyName.text = user.companyName
        textViewSiretNumber.text = user.siretNumber
        textViewProfessionalInformations.visibility = VISIBLE
        textViewCompanyName.visibility = VISIBLE
        textViewSiretNumber.visibility = VISIBLE
    }

    private fun deleteAccount(user: User) {
        // If user is not signed in, skip this part
        if (!isUserSignedIn()) {
            showMessage(R.string.not_connected)
            return
        }

        viewModel.deleteProducts(user.productsId).observeOnce(viewLifecycleOwner, { errorCode ->
            // If the call fails, show error message and hide loading spinner
            if (handleError(errorCode)) return@observeOnce

            // Else, delete the user
            viewModel.deleteUser(auth.currentUser!!)
                .observeOnce(viewLifecycleOwner, observeOnce2@{ errorCode2 ->
                    // If the call fails, show error message and hide loading spinner
                    if (handleError(errorCode2)) return@observeOnce2
                    // Else, sign out from the app and return to home
                    auth.signOut()
                    navigateTo(R.id.action_navigation_profile_to_navigation_home)
                    showMessage(R.string.account_deleted)
                })
        })
    }

    private fun showProducts(ids: ArrayList<String>) {
        viewModel.getProducts(limit = false, ids = ids).observe(viewLifecycleOwner, { pair ->
            val errorCode = pair.first
            val products = pair.second

            // Show "My products" title
            textViewMyProducts.visibility = VISIBLE

            // If the call fails, show error message, hide loading spinner and show empty text
            if (handleError(errorCode)) {
                textViewNoProducts.visibility = VISIBLE
                return@observe
            }

            // Else if there is no products to display, hide loading spinner and show empty text
            if (products.isEmpty()) {
                setSpinnerVisibility(GONE)
                textViewNoProducts.visibility = VISIBLE
                return@observe
            }

            // Else, show products in recycler view
            val adapter = ProductAdapter { product -> adapterOnClick(product) }
            adapter.submitList(products.map { product -> product.toDTO() })
            recyclerViewProducts.adapter = adapter
            recyclerViewProducts.visibility = VISIBLE
        })
    }

}