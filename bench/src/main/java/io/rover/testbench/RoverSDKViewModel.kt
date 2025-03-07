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

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import io.rover.sdk.adobeExperience.adobeExperienceAuthorizer
import io.rover.sdk.axs.axsAuthorizer
import io.rover.sdk.core.Rover
import io.rover.sdk.core.authenticationContext
import io.rover.sdk.core.clearSdkAuthorizationIdToken
import io.rover.sdk.core.data.domain.EventSnapshot
import io.rover.sdk.core.deviceIdentification
import io.rover.sdk.core.eventQueue
import io.rover.sdk.core.logging.log
import io.rover.sdk.core.registerSdkAuthorizationIdTokenRefreshCallback
import io.rover.sdk.core.setSdkAuthorizationIdToken
import io.rover.sdk.core.userInfoManager
import io.rover.sdk.seatgeek.seatGeekAuthorizer
import io.rover.sdk.ticketmaster.ticketmasterAuthorizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Base64
import java.util.Date

/**
 * This object handles the Rover SDK, and also ensures that mutable state is updated whenever
 * there is an SDK change to reflect.
 */
class RoverSDKViewModel(
    private var application: RoverBenchApplication
) : ViewModel() {
    var tmId: String? by mutableStateOf(null)
        private set

    var sgId: String? by mutableStateOf(null)
        private set

    var ecId: String? by mutableStateOf(null)
        private set

    var axsId: String? by mutableStateOf(null)
        private set

    var flashMemberId: String? by mutableStateOf(null)
        private set

    var flashMobileId: String? by mutableStateOf(null)
        private set

    var userInfoString: String by mutableStateOf("")

    var receivedEvents: List<EventSnapshot> by mutableStateOf(emptyList())

    var sdkAuthenticationToken_: String? by mutableStateOf(null)

    init {
        refresh()

        val eventFlow = Rover.shared.eventQueue.enqueuedEvents.asFlow()
        this.viewModelScope.launch {
            eventFlow.collectLatest { event ->
                receivedEvents = listOf(event) + receivedEvents
            }
        }

        Rover.shared.registerSdkAuthorizationIdTokenRefreshCallback {
            Log.i("Rover Bench", "Rover SDK has told us it needs a refreshed SDK auth ID token. Asking auth0 for one...")

            // launch coroutine on dispatchers.main:
            viewModelScope.launch(Dispatchers.Main) {
                if (application.auth0manager.hasValidCredentials()) {
                    val credentials = application.auth0manager.awaitCredentials()

                    val idTokenExpiry = getJwtExpiry(credentials.idToken)

                    if (idTokenExpiry == null) {
                        log.w("ID token from auth0 is not a valid JWT token.")
                        Rover.shared.clearSdkAuthorizationIdToken()
                        return@launch
                    }

                    // work around for odd auth0 behaviour: if ID Token is expired, they don't
                    // consider it necessary to refresh. If so, we'll force a refresh.
                    if (idTokenExpiry < Date(Date().time + 60 * 1000)) {
                        log.w("Known issue: auth0's ID Token is still expired (or about to expire) after awaitCredentials(), we have to explicitly ask for a refresh.")
                        val forciblyRefreshedCredentials = application.auth0manager.awaitCredentials(
                            scope = null,
                            minTtl = 0,
                            parameters = emptyMap(),
                            forceRefresh = true
                        )
                        log.i("Obtained new credentials from auth0. Giving new ID token to the Rover SDK.")
                        Rover.shared.setSdkAuthorizationIdToken(forciblyRefreshedCredentials.idToken)
                        refresh()

                    } else {
                        log.i("Obtained new credentials from auth0. Giving new ID token to the Rover SDK.")
                        Rover.shared.setSdkAuthorizationIdToken(credentials.idToken)
                        refresh()
                    }
                }
            }
        }
    }

    fun openExperience(context: Context, url: Uri) {
        val intent = Rover.shared.intentForLink(url)
        if (intent == null) {
            Log.w("Bench", "No intent for URL: $url")
            return
        }
        context.startActivity(intent)
    }

    val deviceID = Rover.shared.deviceIdentification.installationIdentifier

    suspend fun fireCampaign(campaignID: String = "213224"): CampaignSendRequestResult = withContext(
        Dispatchers.IO,
    ) {
        // Define the URL
        val url = "https://api.rover.io/graphql"

        // Define the request
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .addHeader("x-rover-api-key", serverToken)
            .post(buildRequestBody(campaignID))
            .build()

        // Create the OkHttpClient
        val client = OkHttpClient()

        try {
            // Execute the request
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonString = response.body?.string() ?: return@withContext CampaignSendRequestResult.Failure("Unknown server error")
                val jsonResponse = JSONObject(jsonString)

                if (jsonResponse.has("errors")) {
                    CampaignSendRequestResult.Failure("GraphQL errors: $jsonString")
                } else {
                    CampaignSendRequestResult.Success(jsonString)
                }
            } else {
                val serverMessage = response.body?.string() ?: "Unknown server error"
                CampaignSendRequestResult.Failure(serverMessage)
            }
        } catch (e: Exception) {
            CampaignSendRequestResult.Failure(e.localizedMessage ?: "Error")
        }
    }

    private fun buildRequestBody(campaignID: String): RequestBody {
        val json = JSONObject().apply {
            put(
                "variables",
                JSONObject().apply {
                    put("id", campaignID)
                    put("to", JSONArray().apply { put(deviceID) })
                },
            )
            put("operationName", "CampaignSendNotification")
            put("query", "mutation CampaignSendNotification(${'$'}id:ID!,${'$'}to:[String!]!){campaignSendNotification(id:${'$'}id,to:${'$'}to)}")
        }
        return json.toString().toRequestBody("application/json".toMediaType())
    }

    sealed class CampaignSendRequestResult {
        data class Success(val data: String) : CampaignSendRequestResult()
        data class Failure(val error: String) : CampaignSendRequestResult()
    }


    fun signInRover(activity: Activity) {
        WebAuthProvider.login(application.auth0)
            .withScheme("demo")
            .withScope("openid profile email offline_access")
            // Launch the authentication passing the callback where the results will be received
            .start(activity, object : Callback<Credentials, AuthenticationException> {
                // Called when there is an authentication failure
                override fun onFailure(exception: AuthenticationException) {
                    // Something went wrong!
                    Log.e("Testbench", "Problem signing in with auth0: $exception")
                    Rover.shared.clearSdkAuthorizationIdToken()
                    refresh()
                }

                // Called when authentication completed successfully
                override fun onSuccess(credentials: Credentials) {
                    // Get the access token from the credentials object.
                    // This can be used to call APIs
                    application.auth0manager.saveCredentials(credentials)
                    Log.i("Testbench", "auth0 sign on successful, setting ID token on Rover SDK")
                    Rover.shared.setSdkAuthorizationIdToken(credentials.idToken)
                    refresh()

                    // does auth0 expect me to do something with credentials?

                }
            })
    }

    fun signOutRover(activity: Activity) {
        WebAuthProvider.logout(application.auth0)
            .withScheme("demo")
            .start(activity, object: Callback<Void?, AuthenticationException> {
                override fun onSuccess(payload: Void?) {
                    application.auth0manager.clearCredentials()
                    Rover.shared.clearSdkAuthorizationIdToken()
                    refresh()
                }

                override fun onFailure(exception: AuthenticationException) {
                    // Something went wrong!
                    Log.e("Testbench", "Problem signing out with auth0: $exception")
                }
            })
    }

    // Ticketmaster:

    fun signInTm(tmId: String) {
        Rover.shared.ticketmasterAuthorizer.setTicketmasterId(tmId)

        this.refresh()
    }

    fun signOutTm() {
        Rover.shared.ticketmasterAuthorizer.clearCredentials()

        this.refresh()
    }

    // SeatGeek:

    fun signInSg(sgId: String) {
        Rover.shared.seatGeekAuthorizer.setSeatGeekId(sgId)

        this.refresh()
    }

    fun signOutSg() {
        Rover.shared.seatGeekAuthorizer.clearCredentials()

        this.refresh()
    }

    fun signInAdobe(ecid: String) {
        Rover.shared.adobeExperienceAuthorizer.setECID(ecid)

        this.refresh()
    }

    fun signOutAdobe() {
        Rover.shared.adobeExperienceAuthorizer.clearCredentials()

        this.refresh()
    }

    fun signInAxs(userId: String, flashMemberId: String, flashMobileId: String) {
        // replace blank fields with nil, to allow testbench user to test the nil cases.
        Rover.shared.axsAuthorizer.setUserId(
            userId.ifBlank { null },
            flashMemberId.ifBlank { null },
            flashMobileId.ifBlank { null }
        )

        this.refresh()
    }

    fun signOutAxs() {
        Rover.shared.axsAuthorizer.clearCredentials()

        this.refresh()
    }

    fun refresh() {
        this.sdkAuthenticationToken_ = runBlocking {
            // when checkValidity is false this method returns immediately.
            Rover.shared.authenticationContext.obtainSdkAuthenticationIdToken(checkValidity = false)
        }

        val tmInfo = Rover.shared.userInfoManager.currentUserInfo["ticketmaster"] as? Map<*, *>
        this.tmId = tmInfo?.get("ticketmasterID") as? String

        val sgInfo = Rover.shared.userInfoManager.currentUserInfo["seatGeek"] as? Map<*, *>
        this.sgId = sgInfo?.get("seatGeekID") as? String

        this.ecId = Rover.shared.userInfoManager.currentUserInfo["ecid"] as? String

        val axsInfo = Rover.shared.userInfoManager.currentUserInfo["axs"] as? Map<*, *>
        this.axsId = axsInfo?.get("userID") as? String
        this.flashMemberId = axsInfo?.get("flashMemberID") as? String
        this.flashMobileId = axsInfo?.get("flashMobileID") as? String

        this.userInfoString = Rover.shared.userInfoManager.currentUserInfo.map { (key, value) ->
            "$key = $value"
        }.joinToString("\n")
    }

    companion object {
        private const val serverToken = "400a064c1489204f97bf10e4a67509b43c0e5bfa"
    }
}

/**
 * Decode a JWT token and return the expiry time. This is being used to work around an issue
 * with auth0, where if the ID token is expired but the access token is not, their
 * CredentialsManager will not prompt a refresh, and will happily give you an expired token.
 *
 * A client doing an integration with auth0 might well need to borrow this as a snippet.
 *
 * Note this does not check the signature. The only task is to check if we should request a new
 * token.
 */
private fun getJwtExpiry(jwt: String): Date? {
    val parts = jwt.split(".")
    if (parts.size != 3) {
        return null
    }

    val decoded = try {
        Base64.getUrlDecoder().decode(parts[1])
    } catch (e: IllegalArgumentException) {
        return null
    }

    val obj = try {
        JSONObject(String(decoded))
    } catch (e: JSONException) {
        return null
    }

    if (obj.has("exp")) {
        return Date(obj.getLong("exp") * 1000)
    } else {
        return null
    }
}