package c0d3.vitreen.app.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.MenuItem
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.os.postDelayed
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import c0d3.vitreen.app.R
import c0d3.vitreen.app.utils.Constants
import c0d3.vitreen.app.utils.Constants.Companion.LOCALISATION_REQUEST
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_content.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_adding1.*


class MainActivity : AppCompatActivity() {
    private var backPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Firebase.firestore.firestoreSettings = firestoreSettings { isPersistenceEnabled = false }

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
                topLevelDestinationIds = setOf(
                        R.id.navigation_home,
                        R.id.navigation_messages,
                        R.id.navigation_adding1,
                        R.id.navigation_favorites,
                        R.id.navigation_profile,
                )
        )

        navView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
        requestLocationPermission()

    }

    // Handle toolbar back button
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId != android.R.id.home)
            return super.onOptionsItemSelected(item)

        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment)

        // Check if the current destination is actually the start destination (Home screen)
        when {
            navController.graph.startDestination == navController.currentDestination?.id -> {
                // Check if back is already pressed. If yes, then exit the app.
                if (backPressedOnce) {
                    super.onBackPressed()
                    return
                }

                backPressedOnce = true
                showMessage(R.string.press_back)

                Handler().postDelayed(2000) {
                    backPressedOnce = false
                }
                // TODO : Gérer les cas de login, register 1 et register 2 avec comportement de back button (si nécessaire)
            }
            navController.currentDestination?.id == R.id.navigation_login -> {
                navController.navigate(R.id.action_navigation_login_to_navigation_home)
            }
            else -> super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCALISATION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED)
                    showMessage(R.string.errorMessage) // TODO : Remplacer le message
            }
        }
    }

    fun setTopViewAttributes(title: String, @DrawableRes icon: Int) {
        topView.setAttributes(title, icon)
    }

    fun showMessage(@StringRes errorId: Int) {
        val snackbar = Snackbar.make(activityLayout, errorId, Snackbar.LENGTH_LONG)
        val layoutParams = snackbar.view.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.anchorId = R.id.snackbarGuideline
        layoutParams.gravity = Gravity.TOP
        snackbar.view.layoutParams = layoutParams
        snackbar.show()
    }

    fun setSpinnerVisibility(visibility: Int) {
        spinner.visibility = visibility
    }

    // Demande de permission pour la récupération de la localisation
    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
        )
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCALISATION_REQUEST)
    }

}