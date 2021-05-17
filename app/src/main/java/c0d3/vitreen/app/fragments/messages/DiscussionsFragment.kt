package c0d3.vitreen.app.fragments.messages

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import c0d3.vitreen.app.R
import c0d3.vitreen.app.adapter.DiscussionAdapter
import c0d3.vitreen.app.models.Discussion
import c0d3.vitreen.app.models.Product
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.empty_view.*
import kotlinx.android.synthetic.main.fragment_discussions.*
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.loading_spinner.*

class DiscussionsFragment: VFragment(
    layoutId = R.layout.fragment_discussions,
    topIcon = R.drawable.bigicon_messages
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set elements visibility (while loading)
        emptyView.visibility = GONE
        recyclerViewDiscussions.visibility = GONE

        if(!viewModel.isUserSignedIn) {
            // Set elements visibility
            loadingSpinner.visibility = GONE
            emptyView.visibility = VISIBLE
            recyclerViewDiscussions.visibility = GONE
            // Show error message
            showSnackbarMessage(R.string.SignedOutException)
            return
        }

        viewModel.discussions.observe(viewLifecycleOwner, { (exception, discussions) ->
            // When the call finishes, hide loading spinner
            loadingSpinner.visibility = GONE

            // If the call failed: show error message and show empty view
            if(exception != -1) {
                showSnackbarMessage(exception)
                recyclerViewDiscussions.visibility = GONE
                emptyView.visibility = VISIBLE
                return@observe
            }

            // If there are no discussions: show empty view
            if(discussions.isEmpty()) {
                recyclerViewDiscussions.visibility = GONE
                emptyView.visibility = VISIBLE
                return@observe
            }

            // Else, display discussions in the recycler view
            val adapter = DiscussionAdapter { discussion -> adapterOnClick(discussion) }
            adapter.submitList(discussions)
            recyclerViewDiscussions.adapter = adapter
            emptyView.visibility = GONE
            recyclerViewDiscussions.visibility = VISIBLE
        })
    }

    private fun adapterOnClick(discussion: Discussion) {
        discussion.id?.let { id ->
            viewModel.discussionId = id
            navigateTo(R.id.from_discussions_to_messages)
        }
    }

}