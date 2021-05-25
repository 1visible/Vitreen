package c0d3.vitreen.app.fragments.profile

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.observeOnce
import c0d3.vitreen.app.models.User
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_update.*
import kotlinx.android.synthetic.main.fragment_update.editTextLocation

class UpdateFragment : VFragment(
    layoutId = R.layout.fragment_update,
    requireAuth = true,
    loginNavigationId = R.id.from_update_profile_to_login
) {

    private var oldUser: User? = null
    private var user: User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // If user is not signed in, skip this part
        if (!viewModel.isUserSignedIn)
            return

        try {
            viewModel.user.observe(viewLifecycleOwner, { (exception, user) ->
                if (exception != -1) {
                    this.user = null
                    showSnackbarMessage(exception)
                    goBack()
                    return@observe
                }

                this.user = user
                this.oldUser = User(
                    user.username,
                    user.emailAddress,
                    user.phoneNumber,
                    user.contactByPhone,
                    user.isProfessional,
                    user.location,
                    user.companyName,
                    user.siretNumber,
                    user.favoritesIds
                )

                fillFields()
            })
            // Fill locations in the search section
            viewModel.locations.observe(viewLifecycleOwner, { (exception, locations) ->
                // If the call failed: show error message
                if(exception != -1) {
                    showSnackbarMessage(exception)
                    return@observe
                }

                // Else, put locations as edit text choices
                val locationNames = locations.map { location -> location.city }
                val adapter = context?.let { context -> ArrayAdapter(context, R.layout.dropdown_menu_item, locationNames) }

                (editTextLocation.editText as? AutoCompleteTextView)?.setAdapter(adapter)
            })
            buttonUpdate.setOnClickListener {
                if (isAnyRequiredInputEmpty(
                        editTextUsername,
                        editTextPhoneNumber,
                        editTextLocation
                    )
                )
                    return@setOnClickListener

                if ((switchProfessionalAccount.isChecked) && (isAnyRequiredInputEmpty(
                        editTextCompany,
                        editTextSiret
                    ))
                )
                    return@setOnClickListener

                if (hasAnyChanged(oldUser!!, this.user!!)) {
                    if (inputToString(editTextUsername)!! != this.user!!.username)
                        this.user!!.username = inputToString(editTextUsername)!!

                    if (inputToString(editTextPhoneNumber)!! != this.user!!.phoneNumber)
                        this.user!!.phoneNumber = inputToString(editTextPhoneNumber)!!

                    if (inputToString(editTextLocation)!! != this.user!!.location.city) {
                        viewModel.getLocation(inputToString(editTextLocation)!!).observeOnce(viewLifecycleOwner, { (exception, location) ->
                                // If the call fails, show error message
                                if (exception != -1 && exception != R.string.NotFoundException) {
                                    showSnackbarMessage(exception)
                                    return@observeOnce
                                }

                                // Else if location could not be found, create new location
                                if (exception == R.string.NotFoundException) {
                                    location.city = inputToString(editTextLocation)!!
                                    location.zipCode = null
                                    viewModel.addLocation(location)
                                }

                                this.user!!.location = location
                            })
                    }

                    if (radioButtonPhone.isChecked != this.user!!.contactByPhone)
                        this.user!!.contactByPhone = radioButtonPhone.isChecked
                    if (switchProfessionalAccount.isChecked != this.user!!.isProfessional)
                        this.user!!.isProfessional = switchProfessionalAccount.isChecked
                    if (!switchProfessionalAccount.isChecked) {
                        if (inputToString(editTextCompany) != this.user!!.companyName)
                            this.user!!.companyName = null
                        if (inputToString(editTextSiret) != this.user!!.siretNumber)
                            this.user!!.siretNumber = null
                    } else {
                        if (inputToString(editTextCompany) != this.user!!.companyName)
                            this.user!!.companyName = inputToString(editTextCompany)!!
                        if (inputToString(editTextSiret) != this.user!!.siretNumber)
                            this.user!!.siretNumber = inputToString(editTextSiret)!!
                    }

                    viewModel.updateUser(this.user!!).observeOnce(viewLifecycleOwner, { exception ->
                        if (exception != -1) {
                            showSnackbarMessage(R.string.update_profile_failed)
                            return@observeOnce
                        }

                        showSnackbarMessage(R.string.profile_updated)
                    })
                }

                navigateTo(R.id.from_update_profile_to_profile)
            }
        } catch (_: NullPointerException) {
            showSnackbarMessage(R.string.error_placeholder)
            goBack()
            return
        }
    }

    private fun hasAnyChanged(oldUser: User, newUser: User): Boolean {
        return (oldUser == newUser)
    }

    private fun fillFields() {
        editTextUsername.editText?.setText(this.user!!.username)
        editTextPhoneNumber.editText?.setText(this.user!!.phoneNumber)
        editTextLocation.editText?.setText(this.user!!.location.city)

        if (this.user!!.contactByPhone)
            radioButtonPhone.isChecked = true
        else
            radioButtonEmail.isChecked = true

        if (this.user!!.isProfessional) {
            switchProfessionalAccount.isChecked = true
            editTextCompany.visibility = VISIBLE
            editTextSiret.visibility = VISIBLE
            editTextCompany.editText?.setText(this.user!!.companyName)
            editTextSiret.editText?.setText(this.user!!.siretNumber)
        } else {
            switchProfessionalAccount.isChecked = false
            editTextCompany.visibility = GONE
            editTextSiret.visibility = GONE
        }

        switchProfessionalAccount.setOnClickListener {
            if (switchProfessionalAccount.isChecked) {
                editTextCompany.visibility = VISIBLE
                editTextSiret.visibility = VISIBLE
            } else {
                editTextCompany.visibility = GONE
                editTextSiret.visibility = GONE
            }
        }
    }
}