package c0d3.vitreen.app.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.os.postDelayed
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import c0d3.vitreen.app.R
import c0d3.vitreen.app.utils.Constants.Companion.LOCALISATION_REQUEST
import c0d3.vitreen.app.utils.Constants.Companion.VTAG
import c0d3.vitreen.app.utils.FirestoreViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_content.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {
    private var backPressedOnce = false
    private lateinit var viewModel: FirestoreViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Firebase.firestore.firestoreSettings = firestoreSettings { isPersistenceEnabled = false }
        viewModel = ViewModelProvider(this).get(FirestoreViewModel::class.java)

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
                topLevelDestinationIds = setOf(
                        R.id.navigation_home,
                        R.id.navigation_discussions,
                        R.id.navigation_adding1,
                        R.id.navigation_favorites,
                        R.id.navigation_profile,
                )
        )

        navView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
        requestLocationPermission()

        viewModel.user.observe(this, { (exception, user) ->
            if (exception != -1) {
                viewModel.discussions.value = exception to ArrayList()
                return@observe
            }

             try {
                 viewModel.getDiscussions(userId = user.id!!)
             } catch(_: Exception) {
                 viewModel.discussions.value = R.string.NotFoundException to ArrayList()
             }
        })
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
                showMessage(R.string.press_back_again)

                Handler(Looper.getMainLooper()).postDelayed(2000) {
                    backPressedOnce = false
                }
                // TODO : Gérer les cas de login, register 1 et register 2 avec comportement de back button (si nécessaire)
            }
            navController.currentDestination?.id == R.id.navigation_login -> {
                navController.navigate(R.id.from_login_to_home)
            }
            else -> super.onBackPressed()
        }
    }

    fun setTopViewAttributes(title: String, @DrawableRes icon: Int) {
        topView.setAttributes(title, icon)
    }

    fun showMessage(@StringRes messageId: Int) {
        val snackbar = Snackbar.make(activityLayout, messageId, Snackbar.LENGTH_LONG)
        val layoutParams = snackbar.view.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.anchorId = R.id.snackbarGuideline
        layoutParams.gravity = Gravity.TOP
        snackbar.view.layoutParams = layoutParams
        snackbar.show()
    }

    private fun requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCALISATION_REQUEST)
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(this)
    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(this)
    }

    // Update user (model and state) on auth state changes
    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        viewModel.setUserState(firebaseAuth.currentUser != null, firebaseAuth.currentUser?.email)
    }
}

fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
    observe(owner, object: Observer<T> {
        override fun onChanged(value: T) {
            removeObserver(this)
            observer(value)
        }
    })
}