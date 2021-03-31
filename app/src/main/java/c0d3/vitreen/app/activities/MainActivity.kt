package c0d3.vitreen.app.activities

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import c0d3.vitreen.app.R
import c0d3.vitreen.app.fragments.auth.LoginFragment
import c0d3.vitreen.app.fragments.auth.Register1Fragment
import c0d3.vitreen.app.fragments.home.HomeFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var backPressedOnce = false
    private val auth = Firebase.auth
    private val user = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            topLevelDestinationIds = setOf(
                R.id.navigation_home,
                R.id.navigation_messages,
                R.id.navigation_adding,
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

        // Check if the current destination is actually the dtart sestination (Home screen)
        if (navController.graph.startDestination == navController.currentDestination?.id) {
            // Check if back is already pressed. If yes, then exit the app.
            if (backPressedOnce) {
                super.onBackPressed()
                return
            }

            backPressedOnce = true
            Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show()

            Handler().postDelayed(2000) {
                backPressedOnce = false
            }
        } else if(navController.currentDestination?.id == R.id.navigation_register1) {
            navController.navigate(R.id.action_navigation_register1_to_navigation_home)
            showNavigation()
        } else
            super.onBackPressed()
    }

    fun showNavigation() {
        navView.visibility = View.VISIBLE
        navBackground.visibility = View.VISIBLE
    }

    fun hideNavigation() {
        navView.visibility = View.GONE
        navBackground.visibility = View.GONE
    }

    fun setTopViewAttributes(title: String, icon: Int) {
        topView.setAttributes(title, icon)
    }

    fun setBottomNavMenuIcon(id: Int) {
        navView.menu.forEach { action -> action.isChecked = false }
        navView.menu.findItem(id).isChecked = true
    }

}