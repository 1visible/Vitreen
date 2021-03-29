package c0d3.vitreen.app.fragments.addadvert.step1

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import c0d3.vitreen.app.R


/**
 * A simple [Fragment] subclass.
 * Use the [AddAdvert1Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddAdvert1Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_add_advert1, container, false)
        return root
    }

}