package c0d3.vitreen.app.fragments

import android.content.Context
import androidx.fragment.app.Fragment
import c0d3.vitreen.app.activities.MainActivity

abstract class ChildFragment : Fragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).hideNavigation()
    }

    override fun onDetach() {
        (activity as MainActivity).showNavigation()
        super.onDetach()
    }

}