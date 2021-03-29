package c0d3.vitreen.app.fragments.signup.step2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import c0d3.vitreen.app.R


/**
 * A simple [Fragment] subclass.
 * Use the [Signup2Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Signup2Fragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_signup2, container, false)
        return root
    }

}