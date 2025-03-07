package io.rover.testbench.utils

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft

internal fun SemanticsNodeInteraction.displayPage(index: Int) {
    performTouchInput {
        for (i in 1..index) {
            swipeLeft()
        }
    }
}
