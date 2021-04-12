package c0d3.vitreen.app.fragments.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import c0d3.vitreen.app.R
import c0d3.vitreen.app.utils.Constants
import c0d3.vitreen.app.utils.Constants.Companion.KEYADVERTID

/**
 * A simple [Fragment] subclass.
 * Use the [AdvertFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdvertFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var advertId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            advertId = it.getString(KEYADVERTID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_advert, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("----------------------------------")
        println(advertId)
        println("----------------------------------")
    }

    companion object {
        @JvmStatic
        fun newInstance(advertId: String) =
            AdvertFragment().apply {
                arguments = Bundle().apply {
                    putString(KEYADVERTID, advertId)
                }
            }
    }
}