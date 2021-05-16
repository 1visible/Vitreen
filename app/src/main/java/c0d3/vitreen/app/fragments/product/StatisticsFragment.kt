package c0d3.vitreen.app.fragments.product

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat.getColor
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.Consultation
import c0d3.vitreen.app.models.Product
import c0d3.vitreen.app.utils.Constants.Companion.VTAG
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_statistics.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class StatisticsFragment : VFragment(
    layoutId = R.layout.fragment_statistics,
    topIcon = R.drawable.bigicon_adding,
    hasOptionsMenu = true,
    topMenuId = R.menu.menu_statistics,
    requireAuth = true,
    loginNavigationId = R.id.from_statistics_to_login
) {

    private lateinit var dayLabels: ArrayList<String>
    private lateinit var monthLabels: ArrayList<String>
    private val currentDate = Calendar.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If user is not signed in, skip this part
        if (!viewModel.isUserSignedIn)
            return

        dayLabels = getDays()
        monthLabels = getMonths()

        var consultations: List<Consultation> = viewModel.product.consultations
        val reports = viewModel.product.reporters.size

        textViewConsultations.text = getString(R.string.total_consultations, consultations.size)
        textViewReports.text = getString(R.string.total_reports, reports)

        context?.let { context ->
            if(reports > 0)
                textViewReports.setTextColor(getColor(context, R.color.red))
            else
                textViewReports.setTextColor(getColor(context, R.color.white))
        }

        consultations = consultations.filter { consultation ->
            val consultationDate = Calendar.getInstance()
            consultationDate.time = consultation.date

            consultationDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR)
        }

        val (perDate, perCity) = orderByWeek(consultations)

        chartViewTimeline.labelsFormatter = { label -> label.roundToInt().toString() }
        chartViewCities.labelsFormatter = { label -> label.roundToInt().toString() }

        chartViewTimeline.show(perDate)
        chartViewCities.show(perCity)
    }

    // TODO : Faire les items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.this_week -> true
            R.id.this_month -> true
            R.id.this_year -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun orderByWeek(consults: List<Consultation>): Pair<ArrayList<Pair<String, Float>>, ArrayList<Pair<String, Float>>> {
        val perDate = arrayListOf<Pair<String, Float>>()

        val consultations = consults.filter { consultation ->
            val consultationDate = Calendar.getInstance()
            consultationDate.time = consultation.date

            consultationDate.get(Calendar.WEEK_OF_YEAR) == currentDate.get(Calendar.WEEK_OF_YEAR)
        }

        for(day in 6 downTo 0) {
            val dayLabel = dayLabels[day]
            val dayDate = if(day == 6) 1 else day+2

            val consultationsSize = consultations.filter { consultation ->
                val consultationDate = Calendar.getInstance()
                consultationDate.time = consultation.date

                consultationDate.get(Calendar.DAY_OF_WEEK) == dayDate
            }.size.toFloat()

            perDate.add(dayLabel to consultationsSize)
        }

        return perDate to orderByCity(consultations)
    }

    private fun orderByCity(consultations: List<Consultation>): ArrayList<Pair<String, Float>> {
        val perCity = arrayListOf<Pair<String, Float>>()
        val cities = arrayListOf<String>()

        cities.addAll(consultations.map { consultation -> consultation.city }.distinct())

        if(!cities.contains(""))
            cities.add("")

        cities.forEach { city ->
            val cityLabel = if(city.isBlank()) getString(R.string.other_label) else city
            val consultationsSize = consultations.filter { consultation ->
                consultation.city == city
            }.size.toFloat()

            perCity.add(cityLabel to consultationsSize)
        }

        perCity.sortBy { pair -> pair.second }

        return perCity
    }

    private fun getMonths(): ArrayList<String> {
        return arrayListOf(
            getString(R.string.month_january),
            getString(R.string.month_february),
            getString(R.string.month_march),
            getString(R.string.month_april),
            getString(R.string.month_may),
            getString(R.string.month_june),
            getString(R.string.month_july),
            getString(R.string.month_august),
            getString(R.string.month_september),
            getString(R.string.month_october),
            getString(R.string.month_november),
            getString(R.string.month_december)
        )
    }

    private fun getDays(): ArrayList<String> {
        return arrayListOf(
            getString(R.string.day_monday),
            getString(R.string.day_tuesday),
            getString(R.string.day_wednesday),
            getString(R.string.day_thursday),
            getString(R.string.day_friday),
            getString(R.string.day_saturday),
            getString(R.string.day_sunday)
        )
    }

}