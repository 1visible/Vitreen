package c0d3.vitreen.app.fragments.profile

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.MainActivity
import kotlinx.android.synthetic.main.activity_main.*

class ProfileFragment : Fragment() {

    // TODO: Remove this if not needed
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register1, container, false)
    }

    // TODO: Remove this if not needed
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Put things here
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity).setTopViewAttributes(getString(R.string.signup), R.drawable.bigicon_user)
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

}