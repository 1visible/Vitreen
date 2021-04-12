package c0d3.vitreen.app.fragments.profile

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.navigation.fragment.findNavController
import c0d3.vitreen.app.R
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : VFragment(
    R.layout.fragment_profile,
    R.drawable.bigicon_user,
    -1,
    true,
    R.menu.menu_profile,
    true,
    R.id.action_navigation_profile_to_navigation_login
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signOutButton.visibility = if(user == null) View.INVISIBLE else View.VISIBLE

        if (user != null) {
            db.collection("Users")
                .whereEqualTo("emailAddress", user!!.email)
                .get()
                .addOnSuccessListener { documents ->
                    niy.text = "${getString(R.string.welcomeUser)} ${documents?.first()?.get("fullname")}"
                }
                .addOnFailureListener {
                    // TODO : Gestion d'erreurs
                }
        }

        signOutButton.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_navigation_profile_to_navigation_home)
        }

    }

    // TODO : Ajouter les items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Put things here
            else -> super.onOptionsItemSelected(item)
        }
    }

}