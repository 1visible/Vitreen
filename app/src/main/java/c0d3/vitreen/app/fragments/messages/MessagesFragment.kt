package c0d3.vitreen.app.fragments.messages

import android.view.MenuItem
import c0d3.vitreen.app.R
import c0d3.vitreen.app.utils.VFragment

class MessagesFragment : VFragment(
    layoutId = R.layout.fragment_messages,
    topIcon = R.drawable.bigicon_messages,
    hasOptionsMenu = true,
    topMenuId = R.menu.menu_messages,
    requireAuth = true,
    loginNavigationId = R.id.action_navigation_messages_to_navigation_login
) {

    // TODO : Ajouter les items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Put things here
            else -> super.onOptionsItemSelected(item)
        }
    }

}