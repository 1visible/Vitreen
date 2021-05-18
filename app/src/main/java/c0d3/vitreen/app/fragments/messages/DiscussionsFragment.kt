package c0d3.vitreen.app.fragments.messages

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.MainActivity
import c0d3.vitreen.app.adapter.DiscussionAdapter
import c0d3.vitreen.app.models.Discussion
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.empty_view.*
import kotlinx.android.synthetic.main.fragment_discussions.*
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.loading_spinner.*

class DiscussionsFragment: VFragment(
    layoutId = R.layout.fragment_discussions
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

            val filteredDiscussions = discussions.filter { discussion -> discussion.messages.size > 0 }

            // If there are no discussions: show empty view
            if(filteredDiscussions.isEmpty()) {
                recyclerViewDiscussions.visibility = GONE
                emptyView.visibility = VISIBLE
                return@observe
            }

            // Else, display discussions in the recycler view
            val adapter = DiscussionAdapter { discussion -> adapterOnClick(discussion) }
            adapter.submitList(filteredDiscussions)
            recyclerViewDiscussions.adapter = adapter
            emptyView.visibility = GONE
            recyclerViewDiscussions.visibility = VISIBLE
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as? MainActivity)?.hideBadge()
    }

    private fun adapterOnClick(discussion: Discussion) {
        discussion.id?.let { id ->
            viewModel.discussionId = id
            navigateTo(R.id.from_discussions_to_messages)
        }
    }

}