package c0d3.vitreen.app.fragments.profile

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.MainActivity
import c0d3.vitreen.app.fragments.home.HomeFragment
import c0d3.vitreen.app.models.dto.UserDTO
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {
    private val db = Firebase.firestore
    private val userDb = db.collection("Users")

    private val auth = Firebase.auth
    private val user = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (user == null || user.isAnonymous)
            findNavController().navigate(R.id.action_navigation_profile_to_navigation_register1)
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (user != null) {

            signOutButton.visibility = View.VISIBLE
            signOutButton.setOnClickListener {
                auth
                    .signOut()
                (activity as MainActivity).setBottomNavMenuIcon(R.id.navigation_home)
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, HomeFragment.newInstance())
                    .commit()
            }
            db
                .collection("Users")
                .whereEqualTo("emailAddress", user.email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.size() == 1) {
                        var userDTO: UserDTO? = null
                        for (document in documents) {
                            userDTO = UserDTO(
                                document.id,
                                document.get("fullname") as String,
                                document.get("emailAddress") as String,
                                document.get("phoneNumber") as String,
                                document.get("contactByPhone") as Boolean,
                                document.get("isProfessional") as Boolean,
                                document.get("locationId") as String,
                                document.get("companyName") as String?,
                                document.get("siretNumber") as String?,
                                document.get("advertsId") as ArrayList<String>?,
                                document.get("favoriteAdvertsId") as java.util.ArrayList<String>?
                            )
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
        (activity as MainActivity).setTopViewAttributes("", R.drawable.bigicon_user)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_profile, menu)
    }

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