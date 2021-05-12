package c0d3.vitreen.app.fragments.messages

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import c0d3.vitreen.app.R
import c0d3.vitreen.app.adapter.MessageAdapter
import c0d3.vitreen.app.models.dto.MessageDTO
import c0d3.vitreen.app.utils.Constants
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_messages.*

class MessagesFragment : VFragment(
    layoutId = R.layout.fragment_messages,
    topIcon = R.drawable.bigicon_messages,
    hasOptionsMenu = true,
    topMenuId = R.menu.menu_messages,
    requireAuth = true,
    loginNavigationId = R.id.from_messages_to_login
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getUser(user!!).observeOnce(viewLifecycleOwner, { userPair ->
            if (handleError(userPair.first)) return@observeOnce
            viewModel.getDiscussions(userPair.second.id)
                .observe(viewLifecycleOwner, { discussionsPair ->
                    if (handleError(discussionsPair.first)) {
                        textViewOther.visibility = View.GONE
                        recyclerViewMessages2.visibility = View.GONE
                        return@observe
                    }
                    textViewOther.visibility = View.VISIBLE
                    val adapter = MessageAdapter(
                        { messageDTO: MessageDTO -> adapterOnClick(messageDTO) },
                        viewLifecycleOwner,
                        this
                    )
                    adapter.submitList(discussionsPair.second.map { discussion -> discussion.toDTO() })
                    recyclerViewMessages2.visibility = View.VISIBLE
                    recyclerViewMessages2.adapter = adapter
                })

            viewModel.getDiscussions(productOwner = userPair.second.id)
                .observe(viewLifecycleOwner, { discussionsPair ->
                    if (handleError(discussionsPair.first)) {
                        textViewOwnerTitle.visibility = View.GONE
                        recyclerViewMessages1.visibility = View.GONE
                        return@observe
                    }
                    textViewOwnerTitle.visibility = View.VISIBLE
                    val adapter = MessageAdapter(
                        { messageDTO: MessageDTO -> adapterOnClick(messageDTO) },
                        viewLifecycleOwner,
                        this
                    )
                    adapter.submitList(discussionsPair.second.map { discussion -> discussion.toDTO() })
                    recyclerViewMessages1.visibility = View.VISIBLE
                    recyclerViewMessages1.adapter = adapter
                })

        })
    }

    private fun adapterOnClick(messageDTO: MessageDTO) {
        navigateTo(
            R.id.action_navigation_messages_to_navigation_discussion,
            Constants.KEY_DISCUSSION_ID to messageDTO.id
        )
    }

    // TODO : Ajouter les items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Put things here
            else -> super.onOptionsItemSelected(item)
        }
    }

}