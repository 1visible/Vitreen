package c0d3.vitreen.app.fragments.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.MainActivity
import c0d3.vitreen.app.adapter.AdvertAdapter
import c0d3.vitreen.app.models.mini.AdvertMini
import c0d3.vitreen.app.utils.Constants
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private val auth = Firebase.auth
    private var user = auth.currentUser
    private val db = Firebase.firestore

    private var locationId = ""
    private var listAdvert: ArrayList<AdvertMini> = ArrayList()

    override fun onStart() {
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (user == null) {
            auth.signInAnonymously()
            homeTextViewNoConnection.visibility = View.VISIBLE
            homeTextViewNPY.visibility = View.GONE
        } else {
            if (user.isAnonymous) {
                homeTextViewNoConnection.visibility = View.VISIBLE
                homeTextViewNPY.visibility = View.GONE
            } else {
                val advertAdapter: AdvertAdapter = AdvertAdapter { advert -> adapterOnClick(advert) }
                homeRecyclerView.adapter = advertAdapter
                db.collection("Users")
                        .whereEqualTo("emailAddress", user.email)
                        .get()
                        .addOnSuccessListener {
                            if (it.documents.size == 1) {
                                for (document in it.documents) {
                                    locationId = document.get("locationId") as String
                                }

                                db.collection("Adverts")
                                        .whereEqualTo("locationId", locationId)
                                        .limit(Constants.HomeLimit.toLong())
                                        .get()
                                        .addOnSuccessListener {
                                            if (it.documents.size > 0) {
                                                println("------------- on a trouvé des documents")
                                                for (document in it.documents) {
                                                    listAdvert.add(AdvertMini(
                                                            document.id,
                                                            document.get("title") as String,
                                                            document.get("description") as String,
                                                            document.get("price") as Long
                                                    ))
                                                }

                                                advertAdapter.submitList(listAdvert)

                                            } else {
                                                homeTextViewNoConnection.visibility = View.GONE
                                                homeTextViewNPY.visibility = View.VISIBLE
                                            }
                                        }
                            }
                        }

            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity).setTopViewAttributes(
                getString(R.string.welcome),
                R.drawable.bigicon_leaf
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                // navigate to search screen
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /* Opens Advert  when RecyclerView item is clicked. */
    private fun adapterOnClick(advert: AdvertMini) {
    }

    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
    }

}