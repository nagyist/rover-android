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

package io.rover.testbench.experiences

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.rover.sdk.core.Rover
import io.rover.sdk.experiences.ExperienceComposable
import io.rover.testbench.LocalExperienceActivity
import io.rover.testbench.RoverSDKViewModel

@Composable
fun ExperiencesSection(viewModel: RoverSDKViewModel, padding: PaddingValues) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .padding(padding)
            .consumeWindowInsets(padding)
            .padding(16.dp),
    ) {
        FilledTonalButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                // This file lives in Dropbox, `Engineering/SDK Team/OVERALL TEST.rover`. When it needs to be updated,
                // open this Linked Experience Campaign and upload the updated file to it: https://app.rover.io/v2/accounts/385/teams/483/campaigns/linked/212881
                viewModel.openExperience(
                    context,
                    Uri.parse("rv-testbench://testbench.rover.io/testbench-overall-test")!!,
                )
            },
        ) {
            Text("Open Overall Test")
        }
        FilledTonalButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                // This is a classic experience. You can update it at:
                //   https://app.rover.io/experiences/edit/64516b7daad129000a121665
                viewModel.openExperience(
                    context,
                    Uri.parse("rv-testbench://testbench.rover.io/bfi6sD")!!,
                )
            },
        ) {
            Text("Open Overall Test (Classic)")
        }

        Card(modifier = Modifier.padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select Experience", style = MaterialTheme.typography.titleMedium)
                var enteredUri by remember { mutableStateOf("") }
                var error: String? by remember { mutableStateOf(null) }

                fun launchEnteredUri() {
                    val intent = try {
                        Rover.shared.intentForLink(Uri.parse(enteredUri))
                    } catch (e: Exception) {
                        Log.v("Bench", "Failed to parse URI: $enteredUri")
                        error = "Invalid URL"
                        return
                    }

                    if (intent == null) {
                        error = "That URL not handled by Rover."
                        return
                    }
                    context.startActivity(intent)
                    error = null
                }

                error?.let {
                    Text(it, color = Color.Red)
                }

                TextField(
                    value = enteredUri,
                    onValueChange = { enteredUri = it },
                    maxLines = 1,
                    label = { Text("URL") },
                    placeholder = { Text("https://testbench.rover.io/myexperience") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrect = false,
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        launchEnteredUri()
                    }),
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedButton(onClick = {
                    launchEnteredUri()
                }) {
                    Text("Launch URL")
                }

                val openDocument = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument(),
                    onResult = { uri ->
                        if (uri == null) {
                            Log.w("Bench", "No file selected.")
                            return@rememberLauncherForActivityResult
                        }
                        Log.i("Bench", "Opening experience from file: $uri")
                        val intent = LocalExperienceActivity.makeIntent(context, uri)
                        context.startActivity(intent)
                    },
                )

                OutlinedButton(onClick = {
                    openDocument.launch(
                        arrayOf("application/x-zip", "application/zip", "application/octet-stream"),
                    )
                }) {
                    Text("Open Experience from File")
                }
            }
        }

        Card(modifier = Modifier.padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // bold:
                Text("Embedded Experience (aka Takeover)", style = MaterialTheme.typography.titleMedium)
                ExperienceComposable(
                    url = Uri.parse("rv-testbench://testbench.rover.io/testbench-embed-example")!!,
                )
            }
        }
    }
}
