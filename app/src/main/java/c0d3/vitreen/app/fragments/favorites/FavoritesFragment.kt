package c0d3.vitreen.app.fragments.favorites

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import c0d3.vitreen.app.R
import c0d3.vitreen.app.adapter.AdvertAdapter
import c0d3.vitreen.app.fragments.home.AdvertFragment
import c0d3.vitreen.app.models.dto.UserDTO
import c0d3.vitreen.app.models.mini.AdvertMini
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.fragment_profile.*

class FavoritesFragment : Fragment() {
    private val db = Firebase.firestore
    private val userDb = db.collection("Users")
    private val locationDB = db.collection("locations")
    private val advertDB = db.collection("Adverts")

    private val auth = Firebase.auth
    private val user = auth.currentUser

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
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

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

    // TODO: Remove this if not needed
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_favorites, menu)
    }

    // TODO: Remove this if not needed
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