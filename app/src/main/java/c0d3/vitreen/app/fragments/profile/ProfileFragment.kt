package c0d3.vitreen.app.fragments.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import c0d3.vitreen.app.R
import c0d3.vitreen.app.fragments.addadvert.step2.AddAdvert2Fragment
import c0d3.vitreen.app.fragments.home.HomeFragment

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_signup1, container, false)
        // Exemple de switch entre fragments
        val b: Button = root.findViewById(R.id.nextButton)
        b.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.nav_host_fragment, AddAdvert2Fragment())
                .commit()
        }
        return root
    }
}