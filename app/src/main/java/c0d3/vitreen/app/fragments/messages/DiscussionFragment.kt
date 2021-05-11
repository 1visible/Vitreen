package c0d3.vitreen.app.fragments.messages

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import c0d3.vitreen.app.R
import c0d3.vitreen.app.utils.Constants
import c0d3.vitreen.app.utils.VFragment


/**
 * A simple [Fragment] subclass.
 * Use the [DiscussionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DiscussionFragment : VFragment(
    R.layout.fragment_discussion,
    R.drawable.bigicon_adding,
    -1,
    false,
    requireAuth = true,
    loginNavigationId = R.id.action_navigation_discussion_to_navigation_login
) {

    private var discussionId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        discussionId = arguments?.getString(Constants.KEY_DISCUSSION_ID).orEmpty()
        if (!discussionId!!.isEmpty()) {
            viewModel.getDiscussion(discussionId!!).observe(viewLifecycleOwner, { pair ->
                if(handleError(pair.first)) return@observe

            })
        }
    }

}