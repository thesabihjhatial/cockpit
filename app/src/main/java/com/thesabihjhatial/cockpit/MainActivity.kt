package com.thesabihjhatial.cockpit

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.thesabihjhatial.cockpit.clock.rememberNow
import com.thesabihjhatial.cockpit.display.rememberHeadlightsOn
import com.thesabihjhatial.cockpit.home.HomeScreen
import com.thesabihjhatial.cockpit.home.HomeViewModel
import com.thesabihjhatial.cockpit.splash.SplashGate
import com.thesabihjhatial.cockpit.ui.CockpitTheme
import com.thesabihjhatial.cockpit.ui.DesignScale
import com.thesabihjhatial.cockpit.ui.cockpitColors
import com.thesabihjhatial.cockpit.ui.isDark
import com.thesabihjhatial.cockpit.ui.isNight
import java.time.LocalTime

class MainActivity : ComponentActivity() {
    private val viewModel: HomeViewModel by viewModels()

    private fun hideSystemBars() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemBars()
        paintWindow(isNight(LocalTime.now().hour))
        setContent {
            val now by rememberNow()
            val headlights by rememberHeadlightsOn()

            val night = isDark(now.hour, headlights)

            SideEffect { paintWindow(night) }

            CockpitTheme(night = night) {
                DesignScale {
                    SplashGate { ready -> HomeScreen(ready = ready, viewModel = viewModel) }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) viewModel.requestHome()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    private fun paintWindow(night: Boolean) {
        window.setBackgroundDrawable(ColorDrawable(cockpitColors(night).background.toArgb()))
    }
}
