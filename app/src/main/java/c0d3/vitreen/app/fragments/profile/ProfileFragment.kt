package c0d3.vitreen.app.fragments.profile

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import c0d3.vitreen.app.R
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : VFragment(
    R.layout.fragment_profile,
    R.drawable.bigicon_profile,
    -1,
    true,
    R.menu.menu_profile,
    true,
    R.id.action_navigation_profile_to_navigation_login
) {

    private val userDb = db.collection("Users")
    private val locationDB = db.collection("locations")
    private val advertDB = db.collection("Adverts")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signOutButton.visibility = if(user == null) View.INVISIBLE else View.VISIBLE

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
            userDb
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
                        if (userDTO != null) {
                            println("-------------------${userDTO.locationId}")
                            locationDB
                                .document(userDTO.locationId)
                                .get()
                                .addOnSuccessListener {
                                    profilFullName.text = userDTO.fullname
                                    profilEmailAddress.text = userDTO.emailAddress
                                    profilPhoneNumber.text = userDTO.phoneNumber
                                    profilContactByPhone.text =
                                        if (userDTO.contactByPhone) "contactez moi par téléphone" else "Contactez moi par mail"
                                    profilLocation.text =
                                        "${it.get("name") as String}(${it.get("zipCode") as Long?})"
                                    profilIsProfessional.text =
                                        if (userDTO.isProfessional) "Je suis un professionnel" else "Je ne suis pas un professionnel"
                                    if (userDTO.isProfessional) {
                                        profilCompanyName.visibility = View.VISIBLE
                                        profilCompanyName.text = userDTO.companyName
                                        profilSiret.visibility = View.VISIBLE
                                        profilSiret.text = userDTO.siretNumber
                                        profilStatsButton.visibility = View.VISIBLE
                                    } else {
                                        profilCompanyName.visibility = View.GONE
                                        profilSiret.visibility = View.GONE
                                        profilStatsButton.visibility = View.GONE
                                    }
                                    if ((userDTO.advertsId != null) && (userDTO.advertsId!!.size > 0)) {
                                        profilRecyclerView.visibility = View.VISIBLE
                                        val advertAdapter: AdvertAdapter =
                                            AdvertAdapter { advert -> adapterOnClick(advert) }
                                        profilRecyclerView.adapter = advertAdapter
                                        userDTO.advertsId!!.forEach { advertId ->
                                            advertDB
                                                .document(advertId)
                                                .get()
                                                .addOnSuccessListener {
                                                    advertList.add(
                                                        AdvertMini(
                                                            it.id,
                                                            it.get("title") as String,
                                                            it.get("description") as String,
                                                            it.get("price") as Long
                                                        )
                                                    )
                                                    if (advertList.size == userDTO.advertsId!!.size) {
                                                        advertAdapter.submitList(advertList)
                                                    }
                                                }
                                        }
                                    }
                                }
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

        signOutButton.setOnClickListener {
            auth.signOut()
            navigateTo(R.id.action_navigation_profile_to_navigation_home)
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