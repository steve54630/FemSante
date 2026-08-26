package com.audreyRetournayDiet.femSante.features.login

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.audreyRetournayDiet.femSante.R

/** Un écran de l'onboarding (icône + titre + description), affiché une seule fois au premier lancement. */
data class OnboardingSlide(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val description: Int
) {
    companion object {
        val ALL = listOf(
            OnboardingSlide(
                icon = R.drawable.ic_onboarding_welcome,
                title = R.string.onboarding_welcome_title,
                description = R.string.onboarding_welcome_description
            ),
            OnboardingSlide(
                icon = R.drawable.ic_onboarding_content,
                title = R.string.onboarding_content_title,
                description = R.string.onboarding_content_description
            ),
            OnboardingSlide(
                icon = R.drawable.ic_onboarding_privacy,
                title = R.string.onboarding_privacy_title,
                description = R.string.onboarding_privacy_description
            )
        )
    }
}
