package c0d3.vitreen.app.fragments.profile

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.MainActivity
import c0d3.vitreen.app.fragments.auth.Register1Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class ProfileFragment : Fragment() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val user = auth.currentUser

    override fun onStart() {
        super.onStart()
        if (user == null) {
            Toast.makeText(requireContext(), getString(R.string.ErrorMessage), Toast.LENGTH_SHORT)
                .show()
        } else if (user.isAnonymous) {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.nav_host_fragment, Register1Fragment.newInstance())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
        }
    }

    // TODO: Remove this if not needed
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    // TODO: Remove this if not needed
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Put things here
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity).setTopViewAttributes(
            getString(R.string.signup),
            R.drawable.bigicon_user
        )
    }

    // TODO: Remove this if not needed
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_profile, menu)
    }

    // TODO: Remove this if not needed
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Put things here
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): ProfileFragment = ProfileFragment()
    }

}