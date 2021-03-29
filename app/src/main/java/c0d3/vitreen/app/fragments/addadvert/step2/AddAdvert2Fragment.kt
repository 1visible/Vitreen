package c0d3.vitreen.app.fragments.addadvert.step2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import c0d3.vitreen.app.R

/**
 * A simple [Fragment] subclass.
 * Use the [AddAdvert2Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddAdvert2Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_add_advert2, container, false)
        return root
    }

}