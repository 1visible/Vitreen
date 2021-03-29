package c0d3.vitreen.app.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import c0d3.vitreen.app.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MenuFragment : Fragment() {
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)
        bottomNavigation = view.findViewById(R.id.bottom_navigation)

        /* POUR AJOUTER UN BADGE SUR LES MESSAGES
        var badge = bottomNavigation.getOrCreateBadge(R.id.menu_item_2)
        badge.isVisible = true
        */

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.menu_item_1 -> {
                    Log.d("BOB", "Menu 1")
                    true
                }
                R.id.menu_item_2 -> {
                    Log.d("BOB", "Menu 2")
                    true
                }
                R.id.menu_item_3 -> {
                    Log.d("BOB", "Menu 3")
                    true
                }
                R.id.menu_item_4 -> {
                    Log.d("BOB", "Menu 4")
                    true
                }
                R.id.menu_item_5 -> {
                    Log.d("BOB", "Menu 5")
                    true
                }
                else -> false
            }
        }

        return view
    }

}