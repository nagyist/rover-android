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

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.rover.sdk.core.Rover
import io.rover.sdk.core.deviceIdentification
import io.rover.sdk.core.permissions.PermissionsNotifierInterface
import io.rover.sdk.debug.RoverDebugActivity
import io.rover.sdk.notifications.notificationStore
import io.rover.sdk.notifications.ui.containers.InboxActivity
import io.rover.testbench.data.DataSection
import io.rover.testbench.events.EventsSection
import io.rover.testbench.experiences.ExperiencesSection
import io.rover.testbench.push.PushSection
import io.rover.testbench.ui.theme.Cyan500
import io.rover.testbench.ui.theme.RoverAndroidTheme
import kotlinx.coroutines.reactive.asFlow

class BenchActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RoverAndroidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val viewModel: RoverSDKViewModel = (this.application as? RoverBenchApplication)?.roverSDKViewModel ?: return@Surface

                    var selectedTab: SelectedTab by remember { mutableStateOf(SelectedTab.Home) }

                    Scaffold(
                        topBar = {
                            TopAppBar(title = {
                                Text(
                                    text = when (selectedTab) {
                                        SelectedTab.Home -> "Rover Bench \uD83E\uDD16"
                                        SelectedTab.Experiences -> "Experiences"
                                        SelectedTab.Push -> "Push"
                                        SelectedTab.Data -> "Data"
                                        SelectedTab.Events -> "Events"
                                    },
                                )
                            })
                        },
                        bottomBar = {
                            NavigationBar() {
                                NavigationBarItem(
                                    selected = selectedTab == SelectedTab.Home,
                                    icon = {
                                        Icon(Icons.Filled.Home, contentDescription = "Home")
                                    },
                                    label = {
                                        Text("Home", maxLines = 1)
                                    },
                                    onClick = {
                                        selectedTab = SelectedTab.Home
                                    },
                                )
                                NavigationBarItem(
                                    selected = selectedTab == SelectedTab.Experiences,
                                    icon = {
                                        Icon(painter = painterResource(id = R.drawable.palette48px), contentDescription = "Experiences", modifier = Modifier.size(24.dp))
                                    },
                                    label = {
                                        Text("Experiences", maxLines = 1)
                                    },
                                    onClick = {
                                        selectedTab = SelectedTab.Experiences
                                    },
                                )
                                NavigationBarItem(
                                    selected = selectedTab == SelectedTab.Push,
                                    icon = {
                                        Icon(painter = painterResource(id = R.drawable.inboxfilled48), contentDescription = "Push", modifier = Modifier.size(24.dp))
                                    },
                                    label = {
                                        Text("Push", maxLines = 1)
                                    },
                                    onClick = {
                                        selectedTab = SelectedTab.Push
                                    },
                                )
                                NavigationBarItem(
                                    selected = selectedTab == SelectedTab.Data,
                                    icon = {
                                        Icon(painter = painterResource(id = R.drawable.drive_folder_upload_filled48px), contentDescription = "Data", modifier = Modifier.size(24.dp))
                                    },
                                    label = {
                                        Text("Data", maxLines = 1)
                                    },
                                    onClick = {
                                        viewModel.refresh()
                                        selectedTab = SelectedTab.Data
                                    },
                                )
                                NavigationBarItem(
                                    selected = selectedTab == SelectedTab.Events,
                                    icon = {
                                        Icon(painter = painterResource(id = R.drawable.list_filled_48px), contentDescription = "Events", modifier = Modifier.size(24.dp))
                                    },
                                    label = {
                                        Text("Events", maxLines = 1)
                                    },
                                    onClick = {
                                        selectedTab = SelectedTab.Events
                                    },
                                )
                            }
                        },
                    ) { padding ->
                        when (selectedTab) {
                            SelectedTab.Home -> MainTab(padding)
                            SelectedTab.Experiences -> ExperiencesSection(viewModel, padding)
                            SelectedTab.Push -> PushSection(viewModel, padding)
                            SelectedTab.Data -> DataSection(viewModel, this@BenchActivity, padding)
                            SelectedTab.Events -> EventsSection(viewModel, padding)
                        }
                    }
                }
            }
        }
    }
}

private enum class SelectedTab {
    Home,
    Experiences,
    Push,
    Data,
    Events,
}

@Composable
fun AppIcon() {
    Box(
        modifier = Modifier
            .size(128.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Cyan500,
                    ),
                ),
            ),
    ) {
        Image(
            painterResource(id = R.mipmap.ic_launcher),
            contentDescription = "Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize(),
//                .scale(1.6f)
        )
    }
}

@Composable
private fun MainTab(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .verticalScroll(
                rememberScrollState(),
            )
            // apply the padding and consume window insets required by the Scaffold() used as
            // a parent.
            .padding(paddingValues = padding)
            .consumeWindowInsets(padding)
            // and inside there some padding of our own:
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppIcon()
            Text(Rover.sdkVersion, fontWeight = FontWeight.Bold)

            Column(modifier = Modifier.fillMaxWidth()) {
                CopyableTextField("Device ID", Rover.shared.deviceIdentification.installationIdentifier)
            }

            val context = LocalContext.current

            // Notification permission only a thing on Android 13.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        if (isGranted) {
                            Rover.shared.resolve(PermissionsNotifierInterface::class.java)?.permissionGranted(
                                android.Manifest.permission.POST_NOTIFICATIONS,
                            )
                        }
                    },
                )

                LaunchedEffect(true) {
                    // request notification on startup.
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }

                OutlinedButton(onClick = {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }) {
                    Text("Request Notification Permission")
                }
            }

            val locationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    if (isGranted) {
                        Rover.shared.resolve(PermissionsNotifierInterface::class.java)?.permissionGranted(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                        )
                    }
                },
            )

            OutlinedButton(onClick = {
                locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }) {
                Text("Request Location Permission")
            }

            OutlinedButton(onClick = {
                locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }) {
                Text("Request Background Location Permission")
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(onClick = {
                    context.startActivity(RoverDebugActivity.makeIntent(context))
                }) {
                    Text("Open Settings")
                }

                OutlinedButton(onClick = {
                    context.startActivity(InboxActivity.makeIntent(context))
                }) {
                    Text("Open Inbox")
                }
            }

            val unreadPublisher = remember {
                Rover.shared.notificationStore.unreadCount()
            }
            val unreadCount by unreadPublisher.asFlow().collectAsState(-1)

            Text("Unread notifications: $unreadCount")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    RoverAndroidTheme {
        MainTab(padding = PaddingValues.Absolute(16.dp))
    }
}

