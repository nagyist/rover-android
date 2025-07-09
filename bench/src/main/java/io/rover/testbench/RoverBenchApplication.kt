/*
 * Copyright (c) 2023, Rover Labs, Inc. All rights reserved.
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Rover.
 *
 * This copyright notice shall be included in all copies or substantial portions of
 * the software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.rover.testbench

import android.app.AlertDialog
import android.app.Application
import android.util.Log
import androidx.compose.ui.graphics.toArgb
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.storage.CredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.google.firebase.messaging.FirebaseMessaging
import io.rover.sdk.adobeExperience.AdobeExperienceAssembler
import io.rover.sdk.axs.AxsAssembler
import io.rover.sdk.core.CoreAssembler
import io.rover.sdk.core.Rover
import io.rover.sdk.core.authenticationContext
import io.rover.sdk.core.userInfoManager
import io.rover.sdk.debug.DebugAssembler
import io.rover.sdk.experiences.AppThemeDescription
import io.rover.sdk.experiences.ExperiencesAssembler
import io.rover.sdk.experiences.registerButtonTappedCallback
import io.rover.sdk.experiences.registerCustomActionCallback
import io.rover.sdk.experiences.registerScreenViewedCallback
import io.rover.sdk.location.LocationAssembler
import io.rover.sdk.notifications.NotificationsAssembler
import io.rover.sdk.seatgeek.SeatGeekAssembler
import io.rover.sdk.ticketmaster.TicketmasterAssembler
import io.rover.testbench.ui.theme.DarkColorPalette
import io.rover.testbench.ui.theme.LightColorPalette

class RoverBenchApplication : Application() {
    var roverSDKViewModel: RoverSDKViewModel? = null

    lateinit var auth0: Auth0
    lateinit var auth0authentication: AuthenticationAPIClient
    lateinit var auth0storage: SharedPreferencesStorage
    lateinit var auth0manager: CredentialsManager

    override fun onCreate() {
        super.onCreate()

        auth0 = Auth0.getInstance(
            "WSb8XVNJUnxyfXRXhLuT7D1ov4OA2ldW",
            getString(R.string.com_auth0_domain)
        )

        auth0authentication = AuthenticationAPIClient(auth0)
        auth0storage = SharedPreferencesStorage(this)
        auth0manager = CredentialsManager(auth0authentication, auth0storage)

        Rover.initialize(
            CoreAssembler(
                accountToken = "0b55835fdab4e2042514807c4b89d8a6388255ac",
                application = this,
                urlSchemes = listOf("rv-testbench"),
                associatedDomains = listOf("rover.judo.app", "testbench.rover.io"),
            ),
            ExperiencesAssembler(
                AppThemeDescription(
                    lightColors = AppThemeDescription.ThemeColors(
                        primary = LightColorPalette.primary.toArgb(),
                        onPrimary = LightColorPalette.onPrimary.toArgb(),
                    ),
                    darkColors = AppThemeDescription.ThemeColors(
                        primary = DarkColorPalette.primary.toArgb(),
                        onPrimary = DarkColorPalette.onPrimary.toArgb(),
                    ),
                ),
            ),
            NotificationsAssembler(
                applicationContext = this,
                smallIconResId = R.mipmap.rover_notification_icon,
                requestPushToken = { tokenCallback ->
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) tokenCallback(task.result)
                    }
                },
            ),
            LocationAssembler(),
            DebugAssembler(),
            TicketmasterAssembler(),
            SeatGeekAssembler(),
            AdobeExperienceAssembler(),
            AxsAssembler()
        )

        Rover.installSaneGlobalHttpCache(this)

        // To avoid adding an extra endpoint to api.rover.io, to enable testing the behaviour
        // of SDK authentication we'll add an endpoint to the datasource testing app.

        // we want to match datasource-testing.vercel.app, and this wildcard tests our wildcard
        // matching support. Under normal conditions pattern matching all of vercel and every
        // random person's apps for including a token header to would be very bad, but in this
        // case we are only sending an ID token (which is not particularly valuable) *and* this
        // is a testing app.
        Rover.shared.authenticationContext.enableSdkAuthIdTokenRefreshForDomain("*.vercel.app")

        Rover.shared.registerCustomActionCallback { customActionActivation ->
            val activity = customActionActivation.activity

            // create an alert dialog and present it:
            AlertDialog.Builder(activity)
                .setTitle("Custom Action")
                .setMessage("You just activated a custom action!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        Rover.shared.registerScreenViewedCallback { screenViewedEvent ->
            Log.d("Bench", "Screen viewed: ${screenViewedEvent.screenName} from Experience ${screenViewedEvent.experienceUrl}")
        }

        Rover.shared.registerButtonTappedCallback { buttonTappedEvent ->
            Log.d(
                "Bench",
                "Button tapped: ${buttonTappedEvent.nodeId} from Experience ${buttonTappedEvent.experienceUrl}"
            )
        }

        Rover.shared.userInfoManager.update {
            it["name"] = "John Roverio"
        }

        // So, in order to get the Testbench app's [RoverSDKViewModel] live as early as possible,
        // in order to capture any events before the activity goes live, we're going to instantiate
        // it here.
        roverSDKViewModel = RoverSDKViewModel(this)
    }
}
