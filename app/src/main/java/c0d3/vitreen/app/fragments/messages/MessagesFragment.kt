package c0d3.vitreen.app.fragments.messages

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.MainActivity
import c0d3.vitreen.app.adapter.MessageAdapter
import c0d3.vitreen.app.models.Message
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.empty_view.*
import kotlinx.android.synthetic.main.fragment_discussions.*
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.fragment_messages.*
import kotlinx.android.synthetic.main.loading_spinner.*

class MessagesFragment: VFragment(
    layoutId = R.layout.fragment_messages,
    requireAuth = true,
    loginNavigationId = R.id.from_messages_to_login
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If user is not signed in, skip this part
        if (!viewModel.isUserSignedIn)
            return

        if(viewModel.discussionId.isBlank()) {
            showSnackbarMessage(R.string.NotFoundException)
            goBack()
            return
        }

        // Set elements visibility (while loading)
        recyclerViewMessages.visibility = GONE
        sendMessageField.visibility = GONE

        viewModel.user.observe(viewLifecycleOwner, { (exception, user) ->
            // If the call failed: show error message and go back
            if(exception != -1) {
                showSnackbarMessage(exception)
                goBack()
                return@observe
            }

            viewModel.getDiscussion(viewModel.discussionId).observe(viewLifecycleOwner, observe1@ { (exception, discussion) ->
                // When the call finishes, hide loading spinner
                loadingSpinner.visibility = GONE

                // If the call failed: show error message and go back
                if(exception != -1) {
                    showSnackbarMessage(exception)
                    goBack()
                    return@observe1
                }

                val messages = discussion.messages

                try {
                    // Else, display messages in the recycler view
                    val adapter = MessageAdapter(user.id!!, messages)
                    adapter.submitList(messages)
                    recyclerViewMessages.adapter = adapter
                    recyclerViewMessages.visibility = VISIBLE
                    sendMessageField.visibility = VISIBLE
                    recyclerViewMessages.scrollToPosition(adapter.itemCount - 1)
                } catch(_: NullPointerException) {
                    showSnackbarMessage(exception)
                    goBack()
                    return@observe1
                }

                buttonSendMessage.setOnClickListener {
                    if(areAllInputsEmpty(editTextMessage))
                        return@setOnClickListener

                    val content = inputToString(editTextMessage) ?: return@setOnClickListener
                    val message = Message(user.id!!, user.username, content)

                    viewModel.updateDiscussion(viewModel.discussionId, message)
                    editTextMessage.editText?.text?.clear()
                }
            })
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as? MainActivity)?.hideBadge()
    }

}