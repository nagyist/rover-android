package io.rover.campaigns.experiences.ui.toolbar

import io.rover.campaigns.experiences.streams.PublishSubject
import io.rover.campaigns.experiences.streams.share

internal class ExperienceToolbarViewModel(
    override val configuration: ToolbarConfiguration
) : ExperienceToolbarViewModelInterface {

    override fun pressedBack() {
        actions.onNext(ExperienceToolbarViewModelInterface.Event.PressedBack())
    }

    override fun pressedClose() {
        actions.onNext(ExperienceToolbarViewModelInterface.Event.PressedClose())
    }

    private val actions = PublishSubject<ExperienceToolbarViewModelInterface.Event>()

    override val toolbarEvents = actions.share()
}
