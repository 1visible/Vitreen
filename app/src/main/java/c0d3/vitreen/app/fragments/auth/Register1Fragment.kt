package c0d3.vitreen.app.fragments.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import c0d3.vitreen.app.R
import c0d3.vitreen.app.utils.ChildFragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_register1.*

class Register1Fragment : ChildFragment() {
    private val auth = Firebase.auth
    private val user = auth.currentUser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonToRegister2.setOnClickListener {
            if (editTextEmail.text.toString().trim() != "" &&
                editTextPassword.text.toString().trim() != "" &&
                editTextPasswordConfirmation.text.toString().trim() != "") {

                if (editTextPassword.text.toString() == editTextPasswordConfirmation.text.toString()) {
                    if (user == null)
                        registerUser()
                    else if (user.isAnonymous)
                        linkAnonymousToCredential()
                } else {
                    editTextPassword.text.clear()
                    editTextPasswordConfirmation.text.clear()
                    Toast.makeText(requireContext(), getString(R.string.NoMatchPassword), Toast.LENGTH_SHORT).show()
                }

            } else
                Toast.makeText(requireContext(), getString(R.string.emptyFields), Toast.LENGTH_SHORT).show()
        }

        buttonToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_register1_to_navigation_login)
        }

    }

    private fun registerUser() {
        auth
            .createUserWithEmailAndPassword(editTextEmail.text.toString(), editTextPassword.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // TODO: Replace this with navigation
                    parentFragmentManager
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment,
                            Register2Fragment.newInstance(editTextEmail.text.toString()))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.ErrorMessage), Toast.LENGTH_SHORT).show()
                    // TODO: Gérer les erreurs
                    //pour l'instant ne redirige nulle part, on va juste afficher un toast disant que l'inscription a échoué
                }
            }
    }

    private fun linkAnonymousToCredential() {
        val credential = EmailAuthProvider.getCredential(editTextEmail.text.toString(), editTextPassword.text.toString())
        user!!
            .linkWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // TODO: Replace this with navigation
                    parentFragmentManager
                        .beginTransaction()
                        .replace(
                            R.id.nav_host_fragment,
                            Register2Fragment.newInstance(editTextEmail.text.toString())
                        )
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.ErrorMessage), Toast.LENGTH_SHORT).show()
                    // TODO: Gérer les erreurs
                }
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(): Register1Fragment = Register1Fragment()
    }
}