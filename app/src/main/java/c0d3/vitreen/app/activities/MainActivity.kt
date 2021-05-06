package c0d3.vitreen.app.activities

import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.MenuItem
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.postDelayed
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import c0d3.vitreen.app.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_content.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var backPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

}