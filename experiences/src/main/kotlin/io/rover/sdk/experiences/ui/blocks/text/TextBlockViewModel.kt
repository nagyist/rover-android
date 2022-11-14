package io.rover.sdk.experiences.ui.blocks.text

import io.rover.sdk.experiences.ui.layout.ViewType
import io.rover.sdk.experiences.ui.blocks.concerns.background.BackgroundViewModelInterface
import io.rover.sdk.experiences.ui.blocks.concerns.layout.BlockViewModelInterface
import io.rover.sdk.experiences.ui.blocks.concerns.border.BorderViewModelInterface
import io.rover.sdk.experiences.ui.blocks.concerns.text.TextViewModelInterface

internal class TextBlockViewModel(
        private val blockViewModel: BlockViewModelInterface,
        private val textViewModel: TextViewModelInterface,
        private val backgroundViewModel: BackgroundViewModelInterface,
        private val borderViewModel: BorderViewModelInterface
) : TextBlockViewModelInterface,
    BlockViewModelInterface by blockViewModel,
    BackgroundViewModelInterface by backgroundViewModel,
    TextViewModelInterface by textViewModel,
    BorderViewModelInterface by borderViewModel {

    override val viewType: ViewType = ViewType.Text
}
