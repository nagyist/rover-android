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

package io.rover.sdk.experiences.rich.compose.ui.layers

import android.util.Log
import android.util.Size
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import io.rover.sdk.experiences.rich.compose.model.nodes.ScrollContainer
import io.rover.sdk.experiences.rich.compose.model.values.Alignment
import io.rover.sdk.experiences.rich.compose.model.values.Axis
import io.rover.sdk.experiences.rich.compose.model.values.ColorReference
import io.rover.sdk.experiences.rich.compose.model.values.Fill
import io.rover.sdk.experiences.rich.compose.ui.layers.stacks.HStackLayer
import io.rover.sdk.experiences.rich.compose.ui.layers.stacks.VStackLayer
import io.rover.sdk.experiences.rich.compose.ui.layout.*
import io.rover.sdk.experiences.rich.compose.ui.layout.fallbackMeasure
import io.rover.sdk.experiences.rich.compose.ui.layout.mapMaxIntrinsicWidthAsMeasure
import io.rover.sdk.experiences.rich.compose.ui.modifiers.LayerModifiers
import io.rover.sdk.experiences.rich.compose.ui.layout.ifInfinity

@Composable
internal fun ScrollContainerLayer(node: ScrollContainer, modifier: Modifier = Modifier) {
    ScrollContainerLayer(axis = node.axis, layerModifiers = LayerModifiers(node), modifier) {
        Children(children = node.children, modifier = Modifier)
    }
}

@Composable
internal fun ScrollContainerLayer(axis: Axis = Axis.VERTICAL, layerModifiers: LayerModifiers = LayerModifiers(), modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    // These are all the responsibilities, in order:
    // 1. expanding box (that falls back to child size if proposed infinity constraints) + align scrollable at leading edge of axis if smaller
    // 2. scroll itself
    // 3. expand around content cross-axis so you can drag beside it if it is narrow
    // 4. implicit stack
    // 5. content itself

    ApplyLayerModifiers(layerModifiers, modifier) { modifier ->
        Layout(
            {
                when (axis) {
                    Axis.VERTICAL ->
                        VStackLayer(
                            alignment = Alignment.CENTER,
                            spacing = 0f,
                            modifier = Modifier
                                .then(NestedScrollProtector(Axis.VERTICAL))
                                .verticalScroll(rememberScrollState())
                                .then(ScrollContainerInnerLayoutModifier(Axis.VERTICAL)),
                        ) {
                            content()
                        }

                    Axis.HORIZONTAL -> {
                        HStackLayer(
                            alignment = Alignment.CENTER,
                            spacing = 0f,
                            modifier = Modifier
                                .then(NestedScrollProtector(Axis.HORIZONTAL))
                                .horizontalScroll(rememberScrollState())
                                .then(ScrollContainerInnerLayoutModifier(Axis.HORIZONTAL)),
                        ) {
                            content()
                        }
                    }
                }
            },
            modifier = modifier,
            measurePolicy = ScrollContainerOuterMeasurePolicy(axis),
        )
    }
}

/**
 * This special measure policy is here to expand to maximum space (unless an infinity is the
 * proposed constraint, in which case it adopts the intrinsic size of the child, ie., the scroll
 * container's content.)
 */
internal fun ScrollContainerOuterMeasurePolicy(
    axis: Axis,
): MeasurePolicy {
    return object : MeasurePolicy {

        override fun MeasureScope.measure(
            measurables: List<Measurable>,
            constraints: Constraints,
        ): MeasureResult {
            // measure children with the existing constraints. (note: the scrollable modifier
            // inside will replace the max constraint on the axis of direction with infinity,
            // so we can't and don't need to do it here.)
            val placeables = measurables.map {
                it.measure(
                    Constraints(
                        maxWidth = constraints.maxWidth,
                        maxHeight = constraints.maxHeight,
                    ),
                )
            }
            val width = constraints.maxWidth.ifInfinity { placeables.maxOf { it.measuredWidth } }
            val height = constraints.maxHeight.ifInfinity { placeables.maxOf { it.measuredHeight } }

            return layout(
                // if the scroll container itself was given an infinity, fall back to the size
                // selected by the content rather than expanding to fill space.
                width,
                height,
            ) {
                placeables.forEach { placeable ->
                    placeable.place(0, 0)
                }
            }
        }

        override fun IntrinsicMeasureScope.maxIntrinsicWidth(
            measurables: List<IntrinsicMeasurable>,
            height: Int,
        ): Int {
            return mapMaxIntrinsicWidthAsMeasure(height) { (proposedWidth, proposedHeight) ->
                if (proposedWidth != Constraints.Infinity && proposedHeight != Constraints.Infinity) {
                    // if not a nested scroll container, then no fallback behaviour is needed to
                    // compensate for a proposed infinity, so just expand to fill proposed size
                    // as is default.
                    return@mapMaxIntrinsicWidthAsMeasure Size(proposedWidth, proposedHeight)
                }

                val sizes = measurables.map {
                    // one difference here vs measure() above: the scrollable modifier
                    // will replace the maxWidth/Height dimension with infinity before
                    // measuring its children as appropriate.

                    // so, that difference will be that we will replace proposed dim with
                    // infinity on our scroll dimension whereas measure() above does not.

                    // we do, however, have to use a custom version of Jetpack Compose's
                    // Scroll modifier in order to ensure safe pass-through of packed intrinsics.
                    val size = Size(
                        if (axis == Axis.HORIZONTAL) Constraints.Infinity else proposedWidth,
                        if (axis == Axis.VERTICAL) Constraints.Infinity else proposedHeight,
                    )

                    Size(it.maxIntrinsicWidth(size.height), it.maxIntrinsicHeight(size.width))
                }

                val scrollWidth = proposedWidth.ifInfinity { sizes.maxOf { it.width } }
                val scrollHeight = proposedHeight.ifInfinity { sizes.maxOf { it.height } }
                return@mapMaxIntrinsicWidthAsMeasure Size(scrollWidth, scrollHeight)
            }
        }

        override fun IntrinsicMeasureScope.minIntrinsicWidth(
            measurables: List<IntrinsicMeasurable>,
            height: Int,
        ): Int {
            return mapMinIntrinsicAsFlex {
                // scroll containers are always flexible, and only case where they may not be
                // (proposed an infinity and shrink to child size) happens then flexibility
                // doesn't matter because a containing stack has infinite space to allocate.
                IntRange(0, Constraints.Infinity)
            }
        }

        override fun IntrinsicMeasureScope.minIntrinsicHeight(
            measurables: List<IntrinsicMeasurable>,
            width: Int,
        ): Int {
            return mapMinIntrinsicAsFlex {
                // scroll containers are always flexible, and only case where they may not be
                // (proposed an infinity and shrink to child size) happens then flexibility
                // doesn't matter because a containing stack has infinite space to allocate.
                IntRange(0, Constraints.Infinity)
            }
        }

        override fun IntrinsicMeasureScope.maxIntrinsicHeight(
            measurables: List<IntrinsicMeasurable>,
            width: Int,
        ): Int {
            throw IllegalStateException("Only call the Rover overloaded packed intrinsics methods on Rover measurables, maxIntrinsicHeight is not used")
        }
    }
}

/**
 * This is used in lieu of [Modifier.fillMaxWidth]/[Modifier.fillMaxHeight] because those
 * *force* the child to fill the space using minimum constraints.
 *
 * Rather, we want to expand to fill the space on the sides ourselves, and then place the child in
 * the middle (with no minimum constraint placed on it).
 *
 * (This is used to take up the orthogonal space within a scroll container to make the negative space
 * grabbable.)
 */
private class ScrollContainerInnerLayoutModifier(private val axis: Axis) : LayoutModifier {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        // fill up to max constraints, place measurable in the middle. filling space on the
        // opposing dimension.
        when (axis) {
            Axis.HORIZONTAL -> {
                // fill space on the top and bottom
                val placeable = measurable.measure(constraints)

                val startPos = if (constraints.maxHeight != Constraints.Infinity) {
                    maxOf((constraints.maxHeight / 2) - (placeable.measuredHeight / 2), 0)
                } else {
                    0
                }

                return layout(placeable.measuredWidth, constraints.maxHeight.ifInfinity { placeable.measuredHeight }) {
                    placeable.place(0, startPos)
                }
            }
            Axis.VERTICAL -> {
                // fill space on the sides
                val placeable = measurable.measure(constraints)

                val startPos = if (constraints.maxWidth != Constraints.Infinity) {
                    maxOf((constraints.maxWidth / 2) - (placeable.measuredWidth / 2), 0)
                } else {
                    0
                }

                return layout(constraints.maxWidth.ifInfinity { placeable.measuredWidth }, placeable.measuredHeight) {
                    placeable.place(startPos, 0)
                }
            }
        }
    }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int,
    ): Int {
        // this is being called in the context of normal Jetpack Compose intrinsics, particularly
        // the MeasuringIntrinsics builtins being used by the scrollable modifier.

        // We want to return a correct result for the content, which is Rover Experiences content
        // using Packed Intrinsics.

        // Use fallbackMeasure to get the width, and then we'll return that as the result.
        val proposedSize = Size(
            width,
            Constraints.Infinity
        )

        return measurable.fallbackMeasure(proposedSize).height
    }

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int,
    ): Int {
        // this is being called in the context of normal Jetpack Compose intrinsics, particularly
        // the MeasuringIntrinsics builtins being used by the scrollable modifier.

        // We want to return a correct result for the content, which is Rover Experiences content
        // using Packed Intrinsics.

        // Use fallbackMeasure to get the width, and then we'll return that as the result.
        val proposedSize = Size(
            Constraints.Infinity,
            height
        )

        return measurable.fallbackMeasure(proposedSize).width
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int,
    ): Int = measurable.minIntrinsicHeight(width)

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int,
    ): Int = measurable.minIntrinsicWidth(height)
}

@Preview
@Composable
private fun ScrollContainerSmallWideContent() {
    // this content won't scroll, so should just be aligned at the top and scrolling
    // should have no effect.
    ScrollContainerLayer {
        RectangleLayer(
            fill = Fill.FlatFill(ColorReference.SystemColor("blue")),
            modifier = Modifier.requiredHeight(50.dp),
        )
    }
}

@Preview
@Composable
private fun ScrollContainerLargeContent() {
    // this content is much larger than the preview/test device area, and so will scroll.
    ScrollContainerLayer {
        TextLayer("Lorem ipsum dolor sit amet ".repeat(500))
    }
}

@Preview
@Composable
private fun ScrollContainerLargeNarrowContent() {
    // this content is much larger than the preview/test device area, and so will scroll.
    ScrollContainerLayer {
        Text("Lorem ipsum dolor sit amet ".repeat(500), modifier = Modifier.requiredWidth(50.dp))
    }
}

@Preview
@Composable
private fun ScrollContainerSmallLong() {
    ScrollContainerLayer {
        RectangleLayer(
            fill = Fill.FlatFill(ColorReference.SystemColor("blue")),
            modifier = Modifier.requiredSize(50.dp),
        )
    }
}

@Preview
@Composable
private fun ScrollContainerInStack() {
    VStackLayer {
        RectangleLayer(fill = Fill.FlatFill(ColorReference.SystemColor("blue")))
        ScrollContainerLayer() {
            TextLayer("Lorem ipsum dolor sit amet ".repeat(500))
        }
    }
}

@Preview
@Composable
private fun CrossAxisNestedScrollContainers() {
    ScrollContainerLayer(axis = Axis.VERTICAL) {
        // this scroll container gets infinite height proposed, which instead of defaulting to 8 dp it should measure the child's height instead.
        ScrollContainerLayer(axis = Axis.HORIZONTAL) {
            TextLayer(text = "Hello I am some text.\n\nYou should be able to read all of me.")
            TextLayer("I am some more text")
            TextLayer("And another")
        }
    }
}

/**
 * Guard a nested scroll modifier from crashing if it has been embedded in a scroll container
 * of the same axis. While this is something of an illegal use case, users can build it in
 * the Mac app, and we should produce the same results without crashing.
 */
private class NestedScrollProtector(
    private val axis: Axis,
) : LayoutModifier {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        // here the goal is to maintain the sizing/measurement behaviour of the child,
        // except in one case: if we are proposed infinity on axis of expansion, that means
        // the scroll container has been nested within a scroll container of same dimension.

        // in the event of nested scroll, instead of proposing infinity to child, instead
        // use fallbackMeasure (which bypasses .scrollable) to determine the nominal
        // height of the child and we'll fall back to that.
        val childConstraints = when (axis) {
            Axis.HORIZONTAL -> {
                constraints.copy(
                    maxWidth = constraints.maxWidth.ifInfinity {
                        measurable.fallbackMeasure(
                            Size(constraints.maxWidth, constraints.maxHeight),
                        ).width
                    },
                )
            }
            Axis.VERTICAL -> {
                constraints.copy(
                    maxHeight = constraints.maxHeight.ifInfinity {
                        measurable.fallbackMeasure(
                            Size(constraints.maxWidth, constraints.maxHeight),
                        ).height
                    },
                )
            }
        }

        val placeable =
            measurable.measure(childConstraints)

        return layout(placeable.measuredWidth, placeable.measuredHeight) {
            placeable.place(0, 0)
        }
    }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int,
    ): Int {
        return measurable.maxIntrinsicHeight(width)
    }

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int,
    ): Int {
        return measurable.maxIntrinsicWidth(height)
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int,
    ): Int {
        return measurable.minIntrinsicHeight(width)
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int,
    ): Int {
        return measurable.minIntrinsicWidth(height)
    }
}

@Preview
@Composable
private fun BugInvestigate() {
    VStackLayer {
        ScrollContainerLayer() {
            TextLayer("Heo", modifier = Modifier.background(Color.Green))
        }
    }
}
