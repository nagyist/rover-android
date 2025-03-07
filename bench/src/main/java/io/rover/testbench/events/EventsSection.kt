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

package io.rover.testbench.events

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.rover.sdk.core.data.domain.EventSnapshot
import io.rover.testbench.RoverSDKViewModel

@Composable()
fun EventsSection(viewModel: RoverSDKViewModel, padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .padding(paddingValues = padding)
            .consumeWindowInsets(padding)
            // and inside there some padding of our own:
            .padding(16.dp),
    ) {
        items(viewModel.receivedEvents) { event ->
            EventRow(event)
            Divider()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventRow(event: EventSnapshot) {
    var detailDisplayed by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.padding(PaddingValues(horizontal = 16.dp, vertical = 16.dp)).clickable {
            detailDisplayed = true
        },
    ) {
        Text(event.name, maxLines = 1)
        Spacer(Modifier.weight(1f).defaultMinSize(minWidth = 16.dp))
        val relativeTimeString = DateUtils.getRelativeTimeSpanString(
            event.timestamp.time,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE,
        ).toString()
        Text(relativeTimeString, maxLines = 1)
    }

    if (detailDisplayed) {
        ModalBottomSheet(onDismissRequest = {
            detailDisplayed = false
        }) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Attributes", style = MaterialTheme.typography.titleLarge)
                Text(event.debugAttributesDescription, fontFamily = FontFamily.Monospace)
                Text("Device Context", style = MaterialTheme.typography.titleLarge)
                Text(event.debugDeviceContextDescription, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
