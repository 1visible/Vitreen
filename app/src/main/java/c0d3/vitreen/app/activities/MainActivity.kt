package c0d3.vitreen.app.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import android.view.Gravity
import android.view.MenuItem
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
import c0d3.vitreen.app.models.Discussion
import c0d3.vitreen.app.utils.Constants.Companion.LOCALISATION_REQUEST
import c0d3.vitreen.app.utils.FirestoreViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {
    private var backPressedOnce = false
    private var discussionsSize = -2
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
            val messages: List<Discussion> = viewModel.discussions.value?.second ?: ArrayList()
            viewModel.discussions.value = exception to messages

            if (exception != -1) {
                discussionsSize = -2
                return@observe
            }

             try {
                 if (discussionsSize == -2) {
                     discussionsSize = -1
                     viewModel.getDiscussions(userId = user.id!!)
                 }
             } catch(_: NullPointerException) {
                 viewModel.discussions.value = R.string.NotFoundException to ArrayList()
             }
        })

        viewModel.discussions.observe(this, { (exception, discussions) ->
            if(exception != -1 || discussionsSize == -2)
                return@observe

            if(discussionsSize == -1) {
                discussionsSize = discussions.size
                return@observe
            }

            if(discussionsSize == discussions.size
                && navController.currentDestination?.id != R.id.navigation_discussions
                && navController.currentDestination?.id != R.id.navigation_messages) {

                val badge = navView.getOrCreateBadge(R.id.navigation_discussions)
                badge.isVisible = true

                vibratePhone()
            }

            discussionsSize = discussions.size
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
            }
            navController.currentDestination?.id == R.id.navigation_login -> {
                navController.navigate(R.id.from_login_to_home)
            }
            else -> super.onBackPressed()
        }
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
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
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

    fun hideBadge() {
        val badge = navView.getBadge(R.id.navigation_discussions)
        if (badge != null)
            badge.isVisible = false
    }

    private fun vibratePhone() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= 26)
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        else
            vibrator.vibrate(200)
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