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

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performScrollTo
import org.junit.Test
import io.rover.testbench.test.R
import org.junit.Before

class ChargersGDGTests: BaseRoverTest() {
    //Test Setup
    @Before
    @OptIn(ExperimentalTestApi::class)
    fun loadTestFile() {
        val fileUri = loadTestFile("${R.raw.chargers_gdg}")

        loadLightModeContent(fileUri)

        //wait for it to load
        composeRule.waitUntilExactlyOneExists(hasContentDescription("player image"))
    }

    @Test
    fun rendersInitialView() {
        compareScreenshot(composeRule)
    }

    @Test
    fun rendersTicketInformation() {
        composeRule.onNodeWithContentDescription("Ticket Information")
                .assertExists()
                .performScrollTo()
                .assertIsDisplayed()

        Thread.sleep(500)
        compareScreenshot(composeRule)
    }

    @Test
    fun rendersLiveMultiView() {
        composeRule.onNodeWithContentDescription("Live Multi-View")
                .assertExists()
                .performScrollTo()
                .assertIsDisplayed()

        Thread.sleep(500)
        compareScreenshot(composeRule)
    }

    @Test
    fun rendersGamedayTimeline() {
        //Scroll to the node below the scrollable timeline
        composeRule.onNodeWithContentDescription("Injury Report")
                .assertExists()
                .performScrollTo()
                .assertIsDisplayed()

        Thread.sleep(500)
        compareScreenshot(composeRule)
    }

    @Test
    fun rendersInjuryReport() {
        composeRule.onNodeWithContentDescription("Injury")
                .assertExists()
                .performScrollTo()
                .assertIsDisplayed()

        Thread.sleep(500)
        compareScreenshot(composeRule)
    }

    @Test
    fun rendersGameday() {
        //Scroll to the node below the scrollable gameday
        composeRule.onNodeWithContentDescription("Latest Videos")
                .assertExists()
                .performScrollTo()
                .assertIsDisplayed()

        Thread.sleep(500)
        compareScreenshot(composeRule)
    }

    @Test
    fun rendersLatestVideo() {
        //Scroll to the node below follow us
        composeRule.onNodeWithContentDescription("Follow Us")
                .assertExists()
                .performScrollTo()
                .assertIsDisplayed()

        Thread.sleep(500)
        compareScreenshot(composeRule)
    }

    /*
    Unable to test these screens due to issues with modals

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersPlayerArrival() {
        composeRule.onNodeWithContentDescription("Player Arrivals Button")
                .assertExists()
                .performClick()

        composeRule.waitUntilExactlyOneExists(hasContentDescription("Player Arrivals"))

        compareScreenshot(composeRule)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersPregameWarmup() {
        composeRule.onNodeWithContentDescription("Pre Game Button")
                .assertExists()
                .performClick()

        composeRule.waitUntilExactlyOneExists(hasContentDescription("Pre Game"))

        compareScreenshot(composeRule)
    }
    */
}