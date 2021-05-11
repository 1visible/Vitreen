package c0d3.vitreen.app.fragments.product

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.VISIBLE
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.Product
import c0d3.vitreen.app.utils.Constants.Companion.KEY_PRODUCT_ID
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_statistics.*

class StatisticsFragment : VFragment(
    layoutId = R.layout.fragment_statistics,
    topIcon = R.drawable.bigicon_adding,
    hasOptionsMenu = false,
    // topMenuId = R.menu.menu_statistics,
    requireAuth = true,
    loginNavigationId = R.id.from_statistics_to_login
) {

    private var product: Product? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        product = arguments?.getString(KEY_PRODUCT_ID) as? Product?
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If user is not signed in, skip this part
        if (!isUserSignedIn())
            return

        // Show loading spinner and hide empty view
        setSpinnerVisibility(VISIBLE)

        val firstColor = Color.parseColor("#33FFFFFF")
        val endColor = Color.parseColor("#00FFFFFF")
        myChart.gradientFillColors = intArrayOf(firstColor, endColor)
        val mySet = linkedMapOf("label1" to 4F, "label2" to 7F, "label3" to 2F, "t" to 5F, "a" to 3F, "v" to 6F, "z" to 1F)
        myChart.show(mySet)

        // TODO
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_favorite -> true // TODO
            else -> super.onOptionsItemSelected(item)
        }
    }

}