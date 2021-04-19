package c0d3.vitreen.app.fragments.favorites

import android.view.MenuItem
import c0d3.vitreen.app.R
import c0d3.vitreen.app.utils.VFragment

class FavoritesFragment :VFragment(
    R.layout.fragment_favorites,
    R.drawable.bigicon_favorites,
    -1,
    true,
    R.menu.menu_favorites,
    true,
    R.id.action_navigation_favorites_to_navigation_login
) {

    // TODO : Ajouter les items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Put things here
            else -> super.onOptionsItemSelected(item)
        }
    }

}