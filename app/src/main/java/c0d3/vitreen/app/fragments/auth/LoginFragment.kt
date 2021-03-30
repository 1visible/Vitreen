package c0d3.vitreen.app.fragments.auth

import android.os.Bundle
import android.view.*
import c0d3.vitreen.app.R
import c0d3.vitreen.app.utils.ChildFragment

class LoginFragment : ChildFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    // TODO: Remove this if not needed
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Put things here
    }

}