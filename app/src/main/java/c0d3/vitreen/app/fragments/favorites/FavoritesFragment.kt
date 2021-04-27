package c0d3.vitreen.app.fragments.favorites

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import android.view.MenuItem
import c0d3.vitreen.app.R
import c0d3.vitreen.app.utils.VFragment

class FavoritesFragment :VFragment(
    R.layout.fragment_favorites,
    R.drawable.bigicon_favorites,
    -1,
    true,
    R.menu.menu_favorites,
    true,
    R.id.action_navigation_favorites_to_navigation_login
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (user == null || user.isAnonymous) {
            favoriteHaveToConnect.visibility = View.VISIBLE
            favoriteRecyclerView.visibility = View.GONE
            favoriteNoFavorite.visibility = View.GONE
        } else {
            favoriteHaveToConnect.visibility = View.GONE
            favoriteRecyclerView.visibility = View.VISIBLE
            favoriteNoFavorite.visibility = View.GONE
            //Récupération de la liste d'annonces en favori de l'utilisateur courant
            userDb
                .whereEqualTo("emailAddress", user.email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.size() == 1) {
                        var favAdvertIds: ArrayList<String>? = null
                        for (document in documents) {
                            favAdvertIds =
                                document.get("favoriteAdvertsId") as java.util.ArrayList<String>?
                        }
                        //Si la liste existe et possède des éléments
                        if (favAdvertIds != null) {
                            if (favAdvertIds.size > 0) {
                                var advertList: ArrayList<AdvertMini> = ArrayList()
                                val advertAdapter: AdvertAdapter =
                                    AdvertAdapter { advert -> adapterOnClick(advert) }
                                favoriteRecyclerView.adapter = advertAdapter
                                //Récupération des infos des annonces présentes dans cette liste
                                favAdvertIds.forEach {
                                    advertDB
                                        .document(it)
                                        .get()
                                        .addOnSuccessListener { advert ->
                                            advertList.add(AdvertMini(advert.id, advert.get("title") as String, advert.get("description") as String, advert.get("price") as Long))
                                            if (advertList.size == favAdvertIds.size) {
                                                advertAdapter.submitList(advertList)
                                            }
                                        }
                                }
                            } else {
                                //Affichage du text "Aucun favori"
                                favoriteHaveToConnect.visibility = View.GONE
                                favoriteRecyclerView.visibility = View.GONE
                                favoriteNoFavorite.visibility = View.VISIBLE
                            }
                        } else {
                            //Affichage du text "Aucun favori"
                            favoriteHaveToConnect.visibility = View.GONE
                            favoriteRecyclerView.visibility = View.GONE
                            favoriteNoFavorite.visibility = View.VISIBLE
                        }
                    }
                }
        }
    }
    private val userDb = db.collection("Users")
    private val locationDB = db.collection("locations")
    private val advertDB = db.collection("Adverts")

    // TODO : Ajouter les items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Put things here
            else -> super.onOptionsItemSelected(item)
        }
    }

    /* Opens Advert  when RecyclerView item is clicked. */
    private fun adapterOnClick(advert: AdvertMini) {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.nav_host_fragment, AdvertFragment.newInstance(advert.id))
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }
}