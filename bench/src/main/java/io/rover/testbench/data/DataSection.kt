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

package io.rover.testbench.data

import android.app.Activity
import android.text.TextUtils
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.rover.testbench.CopyableTextField
import io.rover.testbench.RoverSDKViewModel

@Composable
fun DataSection(viewModel: RoverSDKViewModel, activity: Activity, padding: PaddingValues) {

    Column(
        modifier = Modifier
            .padding(padding)
            .consumeWindowInsets(padding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("User Info", style = MaterialTheme.typography.titleMedium)

        Text(viewModel.userInfoString)

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "App Account / SDK Authentication",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.signInRover(activity)
                        },
                        content = {
                            Text("Sign In")
                        },
                    )

                    OutlinedButton(
                        onClick = {
                            viewModel.signOutRover(activity)
                        },
                        content = {
                            Text("Sign Out")
                        },
                    )
                }

                viewModel.sdkAuthenticationToken_?.let { token ->
                    CopyableTextField(
                        label = "SDK Authentication (JWT) ID Token",
                        text = token
                    )
                } ?: Text("ID Token not set.")

                if (viewModel.sdkAuthenticationToken_ != null) {
                    Text(
                        "The ID token is set by signing in. Sign in with your @rover.io Google account.",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Text("Ticketing Integrations", style = MaterialTheme.typography.titleMedium)

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Adobe Experience Emulation", style = MaterialTheme.typography.titleMedium)

                viewModel.ecId?.let { ecId ->
                    CopyableTextField(
                        label = "Adobe ECID",
                        text = ecId
                    )
                }

                var adobeDialogOpen by remember { mutableStateOf(false) }

                var draftEcid by remember { mutableStateOf(viewModel.ecId) }

                LaunchedEffect(key1 = viewModel.ecId, block = {
                    draftEcid = viewModel.ecId
                })

                if (adobeDialogOpen) {
                    AlertDialog(
                            onDismissRequest = { adobeDialogOpen = false },
                            title = { Text("Adobe ECID") },
                            text = {
                                TextField(
                                        value = draftEcid ?: "",
                                        onValueChange = { draftEcid = it },
                                        maxLines = 1,
                                        keyboardOptions = KeyboardOptions(
                                                capitalization = KeyboardCapitalization.None,
                                                autoCorrect = false,
                                                keyboardType = KeyboardType.Ascii,
                                                imeAction = ImeAction.Done,
                                        ),
                                        keyboardActions = KeyboardActions(onDone = {
                                            adobeDialogOpen = false
                                            draftEcid?.let { viewModel.signInAdobe(it) }
                                        }),
                                )
                            },
                            confirmButton = {
                                TextButton(
                                        onClick = {
                                            adobeDialogOpen = false
                                            draftEcid?.let { viewModel.signInAdobe(it) }
                                        },
                                        content = {
                                            Text("Sign in")
                                        },
                                )
                            },
                            dismissButton = {
                                TextButton(
                                        onClick = {
                                            adobeDialogOpen = false
                                        },
                                        content = {
                                            Text("Cancel")
                                        },
                                )
                            },
                    )
                }

                Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(onClick = {
                        // open dialog box to prompt for an ECID
                        adobeDialogOpen = true
                    }) {
                        Text("Sign in")
                    }

                    OutlinedButton(onClick = {
                        adobeDialogOpen = false
                        viewModel.signOutAdobe()
                    }) {
                        Text("Sign out")
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("SeatGeek Emulation", style = MaterialTheme.typography.titleMedium)

                viewModel.sgId?.let { sgId ->
                    CopyableTextField(
                        label = "SeatGeek ID",
                        text = sgId
                    )
                }

                var sgDialogOpen by remember { mutableStateOf(false) }

                var draftSgId by remember { mutableStateOf(viewModel.sgId) }

                LaunchedEffect(key1 = viewModel.sgId, block = {
                    draftSgId = viewModel.sgId
                })

                if (sgDialogOpen) {
                    AlertDialog(
                        onDismissRequest = { sgDialogOpen = false },
                        title = { Text("SeatGeek ID") },
                        text = {
                            TextField(
                                value = draftSgId ?: "",
                                onValueChange = { draftSgId = it },
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.None,
                                    autoCorrect = false,
                                    keyboardType = KeyboardType.Ascii,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    sgDialogOpen = false
                                    draftSgId?.let { viewModel.signInSg(it) }
                                }),
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    sgDialogOpen = false
                                    draftSgId?.let { viewModel.signInSg(it) }
                                },
                                content = {
                                    Text("Sign in")
                                },
                            )
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    sgDialogOpen = false
                                },
                                content = {
                                    Text("Cancel")
                                },
                            )
                        },
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(onClick = {
                        // open dialog box to prompt for a SG ID
                        sgDialogOpen = true
                    }) {
                        Text("Sign in")
                    }

                    OutlinedButton(onClick = {
                        sgDialogOpen = false
                        viewModel.signOutSg()
                    }) {
                        Text("Sign out")
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ticketmaster Emulation", style = MaterialTheme.typography.titleMedium)

                viewModel.tmId?.let { tmId ->
                    CopyableTextField(
                        label = "Ticketmaster ID",
                        text = tmId
                    )
                }
                var tmDialogOpen by remember { mutableStateOf(false) }

                var draftTmId by remember { mutableStateOf(viewModel.tmId) }

                LaunchedEffect(key1 = viewModel.tmId, block = {
                    draftTmId = viewModel.tmId
                })

                if (tmDialogOpen) {
                    AlertDialog(
                        onDismissRequest = { tmDialogOpen = false },
                        title = { Text("Ticketmaster ID") },
                        text = {
                            TextField(
                                value = draftTmId ?: "",
                                onValueChange = { draftTmId = it },
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.None,
                                    autoCorrect = false,
                                    keyboardType = KeyboardType.Ascii,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    tmDialogOpen = false
                                    draftTmId?.let { viewModel.signInTm(it) }
                                }),
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    tmDialogOpen = false
                                    draftTmId?.let { viewModel.signInTm(it) }
                                },
                                content = {
                                    Text("Sign in")
                                },
                            )
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    tmDialogOpen = false
                                },
                                content = {
                                    Text("Cancel")
                                },
                            )
                        },
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(onClick = {
                        // open dialog box to prompt for a TM ID
                        tmDialogOpen = true
                    }) {
                        Text("Sign in")
                    }

                    OutlinedButton(onClick = {
                        tmDialogOpen = false
                        viewModel.signOutTm()
                    }) {
                        Text("Sign out")
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("AXS Emulation", style = MaterialTheme.typography.titleMedium)

                viewModel.axsId?.let { axsId ->
                    CopyableTextField(
                        label = "AXS User ID",
                        text = axsId
                    )
                }

                viewModel.flashMemberId?.let { flashMemberId ->
                    CopyableTextField(
                        label = "Flash Member ID",
                        text = flashMemberId
                    )
                }

                viewModel.flashMobileId?.let { flashMobileId ->
                    CopyableTextField(
                        label = "Flash Mobile ID",
                        text = flashMobileId
                    )
                }

                var axsDialogOpen by remember { mutableStateOf(false) }

                if (axsDialogOpen) {
                    var draftAxsId by remember { mutableStateOf(viewModel.axsId ?: "") }

                    var draftFlashMemberId by remember { mutableStateOf(viewModel.flashMemberId ?: "") }

                    var draftFlashMobileId by remember { mutableStateOf(viewModel.flashMobileId ?: "") }

                    LaunchedEffect(key1 = viewModel.axsId, key2 = viewModel.flashMemberId, key3 = viewModel.flashMobileId, block = {
                        draftAxsId = viewModel.axsId ?: ""
                        draftFlashMemberId = viewModel.flashMemberId ?: ""
                        draftFlashMobileId = viewModel.flashMobileId ?: ""
                    })

                    AlertDialog(
                        onDismissRequest = { axsDialogOpen = false },
                        title = { Text("AXS Sign In") },
                        text = {
                            Column {
                                Text("User ID")
                                TextField(
                                    value = draftAxsId,
                                    onValueChange = { draftAxsId = it },
                                    maxLines = 1,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.None,
                                        autoCorrect = false,
                                        keyboardType = KeyboardType.Ascii,
                                        imeAction = ImeAction.Done,
                                    ),
                                    keyboardActions = KeyboardActions(onDone = {
                                        axsDialogOpen = false
                                        viewModel.signInAxs(draftAxsId, draftFlashMemberId, draftFlashMobileId)
                                    }),
                                )

                                Text("Member ID")
                                TextField(
                                    value = draftFlashMemberId ?: "",
                                    onValueChange = { draftFlashMemberId = it },
                                    maxLines = 1,
                                    placeholder = { Text("Member ID")},
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.None,
                                        autoCorrect = false,
                                        keyboardType = KeyboardType.Ascii,
                                        imeAction = ImeAction.Done,
                                    ),
                                    keyboardActions = KeyboardActions(onDone = {
                                        axsDialogOpen = false
                                        viewModel.signInAxs(draftAxsId, draftFlashMemberId, draftFlashMobileId)
                                    }),
                                )
                                Text("Mobile ID")
                                TextField(
                                    value = draftFlashMobileId ?: "",
                                    onValueChange = { draftFlashMobileId = it },
                                    maxLines = 1,
                                    placeholder = { Text("Mobile ID")},
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.None,
                                        autoCorrect = false,
                                        keyboardType = KeyboardType.Ascii,
                                        imeAction = ImeAction.Done,
                                    ),
                                    keyboardActions = KeyboardActions(onDone = {
                                        axsDialogOpen = false
                                        viewModel.signInAxs(draftAxsId, draftFlashMemberId, draftFlashMobileId)
                                    }),
                                )
                            }

                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    axsDialogOpen = false
                                    viewModel.signInAxs(draftAxsId, draftFlashMemberId, draftFlashMobileId)
                                },
                                content = {
                                    Text("Sign in")
                                },
                            )
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    axsDialogOpen = false
                                },
                                content = {
                                    Text("Cancel")
                                },
                            )
                        },
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(onClick = {
                        // open dialog box to prompt for a AXS User ID
                        axsDialogOpen = true
                    }) {
                        Text("Sign in")
                    }

                    OutlinedButton(onClick = {
                        axsDialogOpen = false
                        viewModel.signOutAxs()
                    }) {
                        Text("Sign out")
                    }
                }
            }
        }
    }
}
