package c0d3.vitreen.app.fragments.product

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.Consultation
import c0d3.vitreen.app.utils.Constants.Companion.VTAG
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_statistics.*
import java.util.*
import kotlin.math.roundToInt

class StatisticsFragment : VFragment(
    layoutId = R.layout.fragment_statistics,
    topIcon = R.drawable.bigicon_adding,
    hasOptionsMenu = true,
    topMenuId = R.menu.menu_statistics,
    requireAuth = true,
    loginNavigationId = R.id.from_statistics_to_login
) {

    private lateinit var consultations: List<Consultation>
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

        consultations = viewModel.product.consultations
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

        chartViewTimeline.labelsFormatter = { label -> label.roundToInt().toString() }
        chartViewCities.labelsFormatter = { label -> label.roundToInt().toString() }

        orderBy(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.this_week -> { item.isChecked = true; orderBy(true) }
            R.id.this_year -> { item.isChecked = true; orderBy(false) }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun orderBy(week: Boolean): Boolean {
        val scale = resources.displayMetrics.density
        val timelineParams: ViewGroup.LayoutParams = chartViewTimeline.layoutParams
        val citiesParams: ViewGroup.LayoutParams = chartViewCities.layoutParams
        val (perDate, perCity) = if(week) orderByWeek() else orderByYear()

        if(perDate.size < 4) {
            chartViewTimeline.visibility = GONE
            textViewEmptyTimeline.visibility = VISIBLE
        } else {
            textViewEmptyTimeline.visibility = GONE
            chartViewTimeline.visibility = VISIBLE

            timelineParams.height = ((24 * perDate.size + 12) * scale + 0.5f).toInt()
            chartViewTimeline.layoutParams = timelineParams
            chartViewTimeline.show(perDate)
        }

        if(perCity.size < 4) {
            chartViewCities.visibility = GONE
            textViewEmptyCities.visibility = VISIBLE
        } else {
            textViewEmptyCities.visibility = GONE
            chartViewCities.visibility = VISIBLE

            citiesParams.height = ((24 * perCity.size + 12) * scale + 0.5f).toInt()
            chartViewCities.layoutParams = citiesParams
            chartViewCities.show(perCity)
        }

         return true
    }

    private fun orderByWeek(): Pair<ArrayList<Pair<String, Float>>, ArrayList<Pair<String, Float>>> {
        val perDate = arrayListOf<Pair<String, Float>>()

        val consultations = consultations.filter { consultation ->
            val consultationDate = Calendar.getInstance()
            consultationDate.time = consultation.date

            consultationDate.get(Calendar.WEEK_OF_YEAR) == currentDate.get(Calendar.WEEK_OF_YEAR)
        }

        for(number in 6 downTo 0) {
            val label = dayLabels[number]
            val date = if(number == 6) 1 else number+2

            val consultationsSize = consultations.filter { consultation ->
                val consultationDate = Calendar.getInstance()
                consultationDate.time = consultation.date

                consultationDate.get(Calendar.DAY_OF_WEEK) == date
            }.size.toFloat()

            perDate.add(label to consultationsSize)
        }

        return perDate to orderByCity(consultations)
    }

    private fun orderByYear(): Pair<ArrayList<Pair<String, Float>>, ArrayList<Pair<String, Float>>> {
        val perDate = arrayListOf<Pair<String, Float>>()

        val consultations = arrayListOf<Consultation>()
        consultations.addAll(this.consultations)

        for(number in 11 downTo 0) {
            val label = monthLabels[number]

            val consultationsSize = consultations.filter { consultation ->
                val consultationDate = Calendar.getInstance()
                consultationDate.time = consultation.date

                consultationDate.get(Calendar.MONTH) == number
            }.size.toFloat()

            perDate.add(label to consultationsSize)
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