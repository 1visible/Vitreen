package c0d3.vitreen.app.fragments.messages

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import c0d3.vitreen.app.R
import c0d3.vitreen.app.utils.VFragment

class MessagesFragment : VFragment(
    R.layout.fragment_messages,
    R.drawable.bigicon_messages,
    -1,
    true,
    R.menu.menu_messages,
    true,
    R.id.action_navigation_messages_to_navigation_login
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    // TODO : Ajouter les items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Put things here
            else -> super.onOptionsItemSelected(item)
        }
    }

}