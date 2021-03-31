package c0d3.vitreen.app.fragments.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import c0d3.vitreen.app.R
import c0d3.vitreen.app.utils.ChildFragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_register1.*

class Register1Fragment : ChildFragment() {

    private val auth = Firebase.auth
    private var user = auth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register1, container, false)
    }

    // TODO: Remove this if not needed
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nextButton.setOnClickListener {
            if ((!(email.text.toString().replace("\\s+", "")
                    .equals(""))) && (!(password.text.toString().replace("\\s+", "")
                    .equals(""))) && (!(password_confirmation.text.toString().replace("\\s+", "")
                    .equals("")))
            ) {

                if (password.text.toString().equals(password_confirmation.text.toString())) {
                    linkAnonymousToCredential()
                } else {
                    password.text.clear()
                    password_confirmation.text.clear()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.NoMatchPassword),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.emptyFields),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun linkAnonymousToCredential() {
        if ((user != null) && (user.isAnonymous)) {
            val credential =
                EmailAuthProvider.getCredential(email.text.toString(), password.text.toString())
            user!!
                .linkWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        parentFragmentManager
                            .beginTransaction()
                            .replace(
                                R.id.nav_host_fragment,
                                Register2Fragment.newInstance(email.text.toString())
                            )
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.ErrorMessage),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): Register1Fragment = Register1Fragment()
    }
}