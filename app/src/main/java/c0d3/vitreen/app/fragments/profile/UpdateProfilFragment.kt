package c0d3.vitreen.app.fragments.profile

import android.os.Bundle
import android.view.View
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.observeOnce
import c0d3.vitreen.app.models.User
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_update_profil.*

class UpdateProfilFragment : VFragment(
    layoutId = R.layout.fragment_update_profil,
    topIcon = R.drawable.bigicon_profile,
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
        buttonUpdate.setOnClickListener {
            if (isAnyRequiredInputEmpty(
                    editTextUsername,
                    editTextPhoneNumber,
                    editTextLocation
                )
            ) return@setOnClickListener
            if ((switchProfessionalAccount.isChecked) && (isAnyRequiredInputEmpty(
                    editTextCompany,
                    editTextSiret
                ))
            ) return@setOnClickListener
            if (hasAnyChanged(oldUser!!, this.user!!)) {
                if (!inputToString(editTextUsername)!!.equals(this.user!!.username)) this.user!!.username =
                    inputToString(editTextUsername)!!
                if (!inputToString(editTextPhoneNumber)!!.equals(this.user!!.phoneNumber)) this.user!!.phoneNumber =
                    inputToString(editTextPhoneNumber)!!
                if (!inputToString(editTextLocation)!!.equals(this.user!!.location.city)) {
                    viewModel.getLocation(inputToString(editTextLocation)!!)
                        .observeOnce(viewLifecycleOwner, { (exception, location) ->
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
                if (radioButtonPhone.isChecked != this.user!!.contactByPhone) this.user!!.contactByPhone =
                    radioButtonPhone.isChecked
                if (switchProfessionalAccount.isChecked != this.user!!.isProfessional) this.user!!.isProfessional =
                    switchProfessionalAccount.isChecked
                if (!switchProfessionalAccount.isChecked) {
                    if (!inputToString(editTextCompany)!!.equals(this.user!!.companyName)) this.user!!.companyName =
                        null
                    if (!inputToString(editTextSiret)!!.equals(this.user!!.siretNumber)) this.user!!.siretNumber =
                        null
                } else {
                    if (!inputToString(editTextCompany)!!.equals(this.user!!.companyName)) this.user!!.companyName =
                        inputToString(editTextCompany)!!
                    if (!inputToString(editTextSiret)!!.equals(this.user!!.siretNumber)) this.user!!.siretNumber =
                        inputToString(editTextSiret)!!
                }

                viewModel.updateUser(this.user!!).observeOnce(viewLifecycleOwner, { exception ->
                    if (exception != -1) {
                        showSnackbarMessage(R.string.update_profil_failed)
                        return@observeOnce
                    }
                    showSnackbarMessage(R.string.update_profil_OK)
                })
            }
            navigateTo(R.id.from_update_profile_to_profile)
        }
    }

    private fun hasAnyChanged(oldUser: User, newUser: User): Boolean {
        return (oldUser.equals(newUser))
    }

    private fun fillFields() {
        editTextUsername.editText?.setText(this.user!!.username)
        editTextPhoneNumber.editText?.setText(this.user!!.phoneNumber)
        editTextLocation.editText?.setText(this.user!!.location.city)
        if (this.user!!.contactByPhone) radioButtonPhone.isChecked = true
        else radioButtonEmail.isChecked = true
        if (this.user!!.isProfessional) {
            switchProfessionalAccount.isChecked = true
            editTextCompany.visibility = View.VISIBLE
            editTextSiret.visibility = View.VISIBLE
            editTextCompany.editText?.setText(this.user!!.companyName)
            editTextSiret.editText?.setText(this.user!!.siretNumber)
        } else {
            switchProfessionalAccount.isChecked = false
            editTextCompany.visibility = View.GONE
            editTextSiret.visibility = View.GONE
        }
        switchProfessionalAccount.setOnClickListener {
            if (switchProfessionalAccount.isChecked) {
                editTextCompany.visibility = View.VISIBLE
                editTextSiret.visibility = View.VISIBLE
            } else {
                editTextCompany.visibility = View.GONE
                editTextSiret.visibility = View.GONE
            }
        }
    }
}