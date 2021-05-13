package c0d3.vitreen.app.fragments.product

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.VISIBLE
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.Consultation
import c0d3.vitreen.app.models.Product
import c0d3.vitreen.app.utils.Constants.Companion.KEY_PRODUCT
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_statistics.*
import java.util.*

class StatisticsFragment : VFragment(
    layoutId = R.layout.fragment_statistics,
    topIcon = R.drawable.bigicon_adding,
    hasOptionsMenu = true,
    topMenuId = R.menu.menu_statistics,
    requireAuth = true,
    loginNavigationId = R.id.from_statistics_to_login
) {

    private var product: Product? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        product = arguments?.get(KEY_PRODUCT) as? Product?
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If user is not signed in, skip this part
        if (!isUserSignedIn())
            return

        // Show loading spinner and hide empty view
        setSpinnerVisibility(VISIBLE)

        // Check if argument could be retrieved
        if(product == null) {
            showSnackbarMessage()
            goBack()
            return
        }

        initResValues()

        val consultations = product!!.consultations
        val values = orderBy(Calendar.MONTH, consultations)

        myChart.show(values)

        // TODO
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_favorite -> true // TODO
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initResValues() {
        Res.MON.get = getString(R.string.day_monday)
        Res.TUE.get = getString(R.string.day_tuesday)
        Res.WED.get = getString(R.string.day_wednesday)
        Res.THU.get = getString(R.string.day_thursday)
        Res.FRI.get = getString(R.string.day_friday)
        Res.SAT.get = getString(R.string.day_saturday)
        Res.SUN.get = getString(R.string.day_sunday)

        Res.JAN.get = getString(R.string.month_january)
        Res.FEB.get = getString(R.string.month_february)
        Res.MAR.get = getString(R.string.month_march)
        Res.APR.get = getString(R.string.month_april)
        Res.MAY.get = getString(R.string.month_may)
        Res.JUN.get = getString(R.string.month_june)
        Res.JUL.get = getString(R.string.month_july)
        Res.AUG.get = getString(R.string.month_august)
        Res.SEP.get = getString(R.string.month_september)
        Res.OCT.get = getString(R.string.month_october)
        Res.NOV.get = getString(R.string.month_november)
        Res.DEC.get = getString(R.string.month_december)
    }

    private fun orderBy(order: Int, consultations: List<Consultation>): ArrayList<Pair<String, Float>> {
        var values = arrayListOf<Pair<String, Float>>()

        if(order == Calendar.MONTH)
            values = arrayListOf(
                Res.DEC.get to filter(consultations, order, Calendar.DECEMBER),
                Res.NOV.get to filter(consultations, order, Calendar.NOVEMBER),
                Res.OCT.get to filter(consultations, order, Calendar.OCTOBER),
                Res.SEP.get to filter(consultations, order, Calendar.SEPTEMBER),
                Res.AUG.get to filter(consultations, order, Calendar.AUGUST),
                Res.JUL.get to filter(consultations, order, Calendar.JULY),
                Res.JUN.get to filter(consultations, order, Calendar.JUNE),
                Res.MAY.get to filter(consultations, order, Calendar.MAY),
                Res.APR.get to filter(consultations, order, Calendar.APRIL),
                Res.MAR.get to filter(consultations, order, Calendar.MARCH),
                Res.FEB.get to filter(consultations, order, Calendar.FEBRUARY),
                Res.JAN.get to filter(consultations, order, Calendar.JANUARY),
            )
        else if(order == Calendar.DAY_OF_WEEK)
            values = arrayListOf(
                Res.MON.get to filter(consultations, order, Calendar.MONDAY),
                Res.TUE.get to filter(consultations, order, Calendar.TUESDAY),
                Res.WED.get to filter(consultations, order, Calendar.WEDNESDAY),
                Res.THU.get to filter(consultations, order, Calendar.THURSDAY),
                Res.FRI.get to filter(consultations, order, Calendar.FRIDAY),
                Res.SAT.get to filter(consultations, order, Calendar.SATURDAY),
                Res.SUN.get to filter(consultations, order, Calendar.SUNDAY)
            )

        return values
    }

    fun filter(consultations: List<Consultation>, order: Int, value: Int): Float {
        return consultations.filter { consultation ->
            val date = Calendar.getInstance()
            date.time = consultation.date
            date.get(order) == value
        }.size.toFloat()
    }

    enum class Res(var get: String = "") {
        JAN, FEB, MAR, APR, MAY, JUN, JUL, AUG, SEP, OCT, NOV, DEC,
        MON, TUE, WED, THU, FRI, SAT, SUN
    }

}