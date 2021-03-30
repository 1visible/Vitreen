package c0d3.vitreen.app.fragments.addadvert.step2

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import c0d3.vitreen.app.R
import c0d3.vitreen.app.utils.Constants

/**
 * A simple [Fragment] subclass.
 * Use the [AddAdvert2Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddAdvert2Fragment : Fragment() {

    private var categoryId: String = ""
    private var title: String = ""
    private var price: String = ""
    private var locationId: String = ""
    private var description: String = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString(Constants.KEYADDADVERTS[0])?.let {
            categoryId = it
        }
        arguments?.getString(Constants.KEYADDADVERTS[1])?.let {
            title = it
        }
        arguments?.getString(Constants.KEYADDADVERTS[2])?.let {
            price = it
        }
        arguments?.getString(Constants.KEYADDADVERTS[3])?.let {
            locationId = it
        }
        arguments?.getString(Constants.KEYADDADVERTS[4])?.let {
            description = it
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_advert2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    companion object {
        @JvmStatic
        fun newInstance(
            categoryId: String,
            title: String,
            price: String,
            locationId: String,
            description: String
        ) = AddAdvert2Fragment().apply {
            arguments = Bundle().apply {
                putString(Constants.KEYADDADVERTS[0], categoryId)
                putString(Constants.KEYADDADVERTS[1], title)
                putString(Constants.KEYADDADVERTS[2], price)
                putString(Constants.KEYADDADVERTS[3], locationId)
                putString(Constants.KEYADDADVERTS[4], description)
            }
        }
    }

}