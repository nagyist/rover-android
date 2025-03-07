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

package io.rover.testbench.push

import android.view.ContextThemeWrapper
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.rover.sdk.notifications.ui.InboxListView
import io.rover.testbench.R
import io.rover.testbench.RoverSDKViewModel
import kotlinx.coroutines.launch

@Composable
fun PushSection(viewModel: RoverSDKViewModel, padding: PaddingValues) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .padding(padding)
            .consumeWindowInsets(padding)
            .padding(8.dp),
    ) {
        FilledTonalButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                coroutineScope.launch {
                    viewModel.fireCampaign()
                }
            },
        ) {
            Text("Fire Push")
        }

        RoverInbox(modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun RoverInbox(modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            // Note: InboxListView requires the Theme.AppCompat.Light theme.
            InboxListView(ContextThemeWrapper(context, R.style.Theme_AppCompat_Light))
        },
        modifier = modifier,
    )
}
