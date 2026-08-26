package com.audreyRetournayDiet.femSante.features.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.UserStore
import com.google.android.material.button.MaterialButton

/**
 * Écrans de bienvenue affichés une seule fois, avant la première connexion
 * ([UserStore.hasSeenOnboarding]). Purement présentationnel : « Suivant »/« Passer »
 * marquent l'onboarding vu et redirigent vers [LoginActivity].
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var flipper: ViewFlipper
    private lateinit var buttonNext: MaterialButton
    private lateinit var dots: MutableList<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        flipper = findViewById(R.id.viewFlipperOnboarding)
        buttonNext = findViewById(R.id.buttonNext)
        val layoutDots = findViewById<android.widget.LinearLayout>(R.id.layoutDots)

        OnboardingSlide.ALL.forEach { slide ->
            val view = layoutInflater.inflate(R.layout.item_onboarding_slide, flipper, false)
            view.findViewById<ImageView>(R.id.imageSlideIcon).setImageResource(slide.icon)
            view.findViewById<TextView>(R.id.textSlideTitle).setText(slide.title)
            view.findViewById<TextView>(R.id.textSlideDescription).setText(slide.description)
            flipper.addView(view)
        }

        dots = OnboardingSlide.ALL.indices.map { buildDot(layoutDots) }.toMutableList()
        updateDots()

        findViewById<View>(R.id.buttonSkip).setOnClickListener { finishOnboarding() }
        buttonNext.setOnClickListener {
            if (flipper.displayedChild == OnboardingSlide.ALL.lastIndex) {
                finishOnboarding()
            } else {
                flipper.showNext()
                updateDots()
            }
        }
    }

    private fun buildDot(container: android.widget.LinearLayout): View {
        val dot = View(this)
        val size = (10 * resources.displayMetrics.density).toInt()
        val margin = (4 * resources.displayMetrics.density).toInt()
        val params = android.widget.LinearLayout.LayoutParams(size, size).apply {
            marginStart = margin
            marginEnd = margin
        }
        dot.layoutParams = params
        container.addView(dot)
        return dot
    }

    /** Pastille pleine pour l'écran affiché, vide pour les autres ; bouton « Commencer » sur le dernier écran. */
    private fun updateDots() {
        val current = flipper.displayedChild
        dots.forEachIndexed { index, dot ->
            dot.setBackgroundResource(
                if (index == current) R.drawable.dot_indicator_active else R.drawable.dot_indicator_inactive
            )
        }
        buttonNext.setText(
            if (current == OnboardingSlide.ALL.lastIndex) R.string.onboarding_start else R.string.onboarding_next
        )
    }

    private fun finishOnboarding() {
        UserStore(this).setOnboardingSeen()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
