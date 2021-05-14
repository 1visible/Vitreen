package c0d3.vitreen.app.fragments.auth

import android.os.Bundle
import android.view.View
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.observeOnce
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_login.*
import java.util.*

class LoginFragment : VFragment(
    layoutId = R.layout.fragment_login,
    topIcon = R.drawable.bigicon_authentification
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // On submit button click, try to login the user
        buttonSubmitLogin.setOnClickListener {
            // Check if required inputs are filled
            if(isAnyRequiredInputEmpty(editTextEmail, editTextPassword))
                return@setOnClickListener

            val email = inputToString(editTextEmail)
            val password = inputToString(editTextPassword)

            // Check email and password after conversion
            if (email == null || password == null) {
                showSnackbarMessage(R.string.error_placeholder)
                return@setOnClickListener
            }

            // If the user is signed out, sign in
            if (!viewModel.isUserSignedIn)
                viewModel.signIn(email, password).observeOnce(viewLifecycleOwner, { exception ->
                    // TODO : Gérer les différents erreurs
                    if(exception != -1) {
                        showSnackbarMessage(exception)
                        return@observeOnce
                    }

                    navigateTo(R.id.from_login_to_home)
                })
            // Else (the user is signed in), navigate to home (to refresh everything)
            else
                navigateTo(R.id.from_login_to_home)
        }

        // On register button click, navigate to Register1 fragment
        buttonToRegister1.setOnClickListener {
            navigateTo(R.id.from_login_to_register1)
        }
    }

}