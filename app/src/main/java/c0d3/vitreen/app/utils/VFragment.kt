package c0d3.vitreen.app.utils

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import c0d3.vitreen.app.activities.MainActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

abstract class VFragment(
        private val layoutId: Int,
        private val topIcon: Int,
        private val topTitleId: Int = -1,
        private val hasOptionsMenu: Boolean = false,
        private val topMenuId: Int = -1,
        private val requireAuth: Boolean = false,
        private val loginNavigationId: Int = -1
) : Fragment() {

    val db = Firebase.firestore
    val auth = Firebase.auth
    var user: FirebaseUser? = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(hasOptionsMenu)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (requireAuth && (user == null || user.isAnonymous))
            findNavController().navigate(loginNavigationId)
        return inflater.inflate(layoutId, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val topTitle: String = if(topTitleId == -1) "" else getString(topTitleId)
        (activity as MainActivity).setTopViewAttributes(topTitle, topIcon)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if(hasOptionsMenu)
            inflater.inflate(topMenuId, menu)
    }

    fun highlightMenuIcon(id: Int) {
        (activity as MainActivity).highlightMenuIcon(id)
    }

}