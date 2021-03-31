package c0d3.vitreen.app.fragments.profile

import android.annotation.SuppressLint
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
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val user = auth.currentUser

    override fun onStart() {
        super.onStart()
        if ((user == null)) {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.nav_host_fragment, Register1Fragment.newInstance())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
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
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (user != null) {
            db
                .collection("Users")
                .whereEqualTo("email", user.email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.size() == 1) {
                        for (document in documents) {
                            welcomeMessage.text = "${getString(R.string.welcomeUser)} ${
                                document.get(
                                    "lastName"
                                )
                            } ${document.get("firstName")}"
                        }
                        signOutButton.visibility = View.VISIBLE
                        signOutButton.setOnClickListener {
                            auth
                                .signOut()
                            parentFragmentManager
                                .beginTransaction()
                                .replace(R.id.nav_host_fragment, ProfileFragment.newInstance())
                                .commit()
                        }
                    } else {
                        println("--------------------------------documents size > 1")
                    }
                }
                .addOnFailureListener {
                    println("-------------------------problème")
                }
        } else {
            signOutButton.visibility = View.INVISIBLE
        }
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