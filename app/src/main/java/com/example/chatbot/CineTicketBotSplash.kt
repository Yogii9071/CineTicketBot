package com.example.chatbot

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CineTicketBotSplash : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    // Views
    private lateinit var dotLeft: View
    private lateinit var dotMid: View
    private lateinit var dotRight: View

    private lateinit var dot1: View
    private lateinit var dot2: View
    private lateinit var dot3: View
    private lateinit var dot4: View

    private lateinit var dotContainer: View
    private lateinit var fourDotContainer: View

    private lateinit var leftPerson: View
    private lateinit var rightMascot: View

    private lateinit var bubbleTicket: View
    private lateinit var bubbleCinema: View

    private lateinit var cinemaFinal: View
    private lateinit var appNameText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cine_ticket_bot_splash)

        supportActionBar?.hide()

        initViews()
        // Start with dots invisible, then show and animate
        handler.postDelayed({
            startDotWaveAnimation()
        }, 300)
    }

    private fun initViews() {
        dotLeft = findViewById(R.id.dotLeft)
        dotMid = findViewById(R.id.dotMid)
        dotRight = findViewById(R.id.dotRight)

        dot1 = findViewById(R.id.dot1)
        dot2 = findViewById(R.id.dot2)
        dot3 = findViewById(R.id.dot3)
        dot4 = findViewById(R.id.dot4)

        dotContainer = findViewById(R.id.dotContainer)
        fourDotContainer = findViewById(R.id.fourDotContainer)

        leftPerson = findViewById(R.id.leftPerson)
        rightMascot = findViewById(R.id.rightMascot)

        bubbleTicket = findViewById(R.id.bubbleTicket)
        bubbleCinema = findViewById(R.id.bubbleCinema)

        cinemaFinal = findViewById(R.id.cinemaFinal)
        appNameText = findViewById(R.id.appNameText)

        // Initially make three dots invisible
        dotContainer.visibility = View.INVISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    // ---------------------------------------------------------
    // 1️⃣ DOT WAVY ANIMATION (3 Dots)
    // ---------------------------------------------------------
    private fun startDotWaveAnimation() {
        // Make dots visible first
        dotContainer.visibility = View.VISIBLE
        dotContainer.alpha = 0f
        dotContainer.animate()
            .alpha(1f)
            .setDuration(300)
            .start()

        val dots = listOf(dotLeft, dotMid, dotRight)
        startUpDownWave(dots)

        handler.postDelayed({
            transformToFourDots()
        }, 1000) // Reduced from 1500 to 1000
    }

    private fun startUpDownWave(dots: List<View>) {
        val duration = 400L // Reduced from 600L
        val delay = 100L // Reduced from 150L

        dots.forEachIndexed { index, dot ->
            val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.duration = duration
            animator.repeatCount = ValueAnimator.INFINITE
            animator.repeatMode = ValueAnimator.REVERSE
            animator.startDelay = index * delay

            animator.addUpdateListener {
                val progress = it.animatedValue as Float
                dot.translationY = -20f * progress
            }

            animator.start()
        }
    }

    // ---------------------------------------------------------
    // 2️⃣ TRANSFORM 3 DOTS → 4 DOTS (Middle dot splits)
    // ---------------------------------------------------------
    private fun transformToFourDots() {
        // Stop the wave animation
        val threeDots = listOf(dotLeft, dotMid, dotRight)
        threeDots.forEach { it.animate().cancel() }

        // Position the 4 dots exactly where the 3 dots are
        fourDotContainer.x = dotContainer.x
        fourDotContainer.y = dotContainer.y

        // Make dot2 and dot3 start from the same position (middle dot)
        dot2.scaleX = 0f
        dot2.scaleY = 0f
        dot3.scaleX = 0f
        dot3.scaleY = 0f

        fourDotContainer.visibility = View.VISIBLE
        fourDotContainer.alpha = 1f

        // Hide original dots and show the transformation
        threeDots.forEach { it.animate().alpha(0f).setDuration(200).start() } // Reduced from 300

        // Animate dot2 and dot3 splitting from middle
        dot2.animate()
            .scaleX(1f)
            .scaleY(1f)
            .translationX(-20f) // Move left
            .setDuration(300) // Reduced from 400
            .start()

        dot3.animate()
            .scaleX(1f)
            .scaleY(1f)
            .translationX(20f) // Move right
            .setDuration(300) // Reduced from 400
            .withEndAction {
                // Start wave animation on 4 dots
                val fourDots = listOf(dot1, dot2, dot3, dot4)
                startUpDownWave(fourDots)

                handler.postDelayed({
                    transformFourDotsIntoCharacters()
                }, 500) // Reduced from 800
            }
            .start()
    }

    // ---------------------------------------------------------
    // 3️⃣ 4 DOTS → CHARACTERS TRANSFORMATION
    // ---------------------------------------------------------
    private fun transformFourDotsIntoCharacters() {
        val fourDots = listOf(dot1, dot2, dot3, dot4)
        fourDots.forEach { it.animate().cancel() }

        // Use post to ensure we get the correct positions after layout
        dot2.post {
            // Calculate positions for side-by-side placement
            val screenCenterX = resources.displayMetrics.widthPixels / 2f
            val screenCenterY = resources.displayMetrics.heightPixels / 2f

            val personX = screenCenterX - 150f // Left side
            val mascotX = screenCenterX + 150f // Right side
            val characterY = screenCenterY

            // DOT 1 & 2 → LEFT PERSON (Head & Body)
            dot1.animate()
                .scaleX(1.5f).scaleY(1.5f)
                .translationY(-20f)
                .setDuration(300) // Reduced from 400
                .start()

            dot2.animate()
                .scaleX(2f).scaleY(2f)
                .translationY(20f)
                .setDuration(300) // Reduced from 400
                .withEndAction {
                    // Position and reveal person on LEFT side
                    leftPerson.visibility = View.VISIBLE
                    leftPerson.alpha = 0f
                    leftPerson.scaleX = 0.1f
                    leftPerson.scaleY = 0.1f
                    leftPerson.x = personX - leftPerson.width / 2
                    leftPerson.y = characterY - leftPerson.height / 2

                    leftPerson.animate()
                        .alpha(1f)
                        .scaleX(1f).scaleY(1f)
                        .setDuration(300) // Reduced from 400
                        .start()

                    // Hide dots
                    dot1.animate().alpha(0f).setDuration(150).start() // Reduced from 200
                    dot2.animate().alpha(0f).setDuration(150).start() // Reduced from 200
                }
                .start()

            // DOT 3 & 4 → RIGHT MASCOT (Head & Body)
            dot3.animate()
                .scaleX(1.5f).scaleY(1.5f)
                .translationY(-20f)
                .setDuration(300) // Reduced from 400
                .start()

            dot4.animate()
                .scaleX(2f).scaleY(2f)
                .translationY(20f)
                .setDuration(300) // Reduced from 400
                .withEndAction {
                    // Position and reveal mascot on RIGHT side
                    rightMascot.visibility = View.VISIBLE
                    rightMascot.alpha = 0f
                    rightMascot.scaleX = 0.1f
                    rightMascot.scaleY = 0.1f
                    rightMascot.x = mascotX - rightMascot.width / 2
                    rightMascot.y = characterY - rightMascot.height / 2

                    rightMascot.animate()
                        .alpha(1f)
                        .scaleX(1f).scaleY(1f)
                        .setDuration(300) // Reduced from 400
                        .start()

                    // Hide dots
                    dot3.animate().alpha(0f).setDuration(150).start() // Reduced from 200
                    dot4.animate().alpha(0f).setDuration(150).start() // Reduced from 200

                    handler.postDelayed({
                        showTicketBubble()
                    }, 200) // Reduced from 300
                }
                .start()
        }
    }

    // ---------------------------------------------------------
    // 4️⃣ TICKET BUBBLE APPEARS ABOVE LEFT PERSON
    // ---------------------------------------------------------
    private fun showTicketBubble() {
        bubbleTicket.visibility = View.VISIBLE
        bubbleTicket.alpha = 0f
        bubbleTicket.scaleX = 0.8f
        bubbleTicket.scaleY = 0.8f
        bubbleTicket.translationY = 20f

        // Position bubble above left person
        bubbleTicket.x = leftPerson.x
        bubbleTicket.y = leftPerson.y - bubbleTicket.height - 40f

        bubbleTicket.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0f)
            .setDuration(400) // Reduced from 500
            .withEndAction {
                handler.postDelayed({
                    showCinemaBubble()
                }, 200) // Reduced from 300
            }
            .start()
    }

    // ---------------------------------------------------------
    // 5️⃣ CINEMA BUBBLE APPEARS ABOVE RIGHT MASCOT
    // ---------------------------------------------------------
    private fun showCinemaBubble() {
        bubbleCinema.visibility = View.VISIBLE
        bubbleCinema.alpha = 0f
        bubbleCinema.scaleX = 0.8f
        bubbleCinema.scaleY = 0.8f
        bubbleCinema.translationY = 20f

        // Position bubble above right mascot
        bubbleCinema.x = rightMascot.x
        bubbleCinema.y = rightMascot.y - bubbleCinema.height - 40f

        bubbleCinema.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0f)
            .setDuration(400) // Reduced from 500
            .withEndAction {
                handler.postDelayed({
                    transitionFromBubbleToLogo()
                }, 300) // Reduced from 500
            }
            .start()
    }

    // ---------------------------------------------------------
    // 6️⃣ TRANSITION FROM BUBBLE TO FINAL LOGO
    // ---------------------------------------------------------
    private fun transitionFromBubbleToLogo() {
        // Hide left person, ticket bubble, AND right mascot
        leftPerson.animate().alpha(0f).setDuration(200).start() // Reduced from 300
        bubbleTicket.animate().alpha(0f).setDuration(200).start() // Reduced from 300
        rightMascot.animate().alpha(0f).setDuration(200).start() // Reduced from 300
        fourDotContainer.animate().alpha(0f).setDuration(200).start() // Reduced from 300

        // Get bubble position for transition
        val location = IntArray(2)
        bubbleCinema.getLocationOnScreen(location)

        // Position final logo at bubble location initially
        cinemaFinal.x = bubbleCinema.x - (cinemaFinal.width - bubbleCinema.width) / 2f
        cinemaFinal.y = bubbleCinema.y - (cinemaFinal.height - bubbleCinema.height) / 2f

        cinemaFinal.visibility = View.VISIBLE
        cinemaFinal.alpha = 0f
        cinemaFinal.scaleX = 0.1f
        cinemaFinal.scaleY = 0.1f

        // Animate bubble shrinking while logo emerges
        bubbleCinema.animate()
            .scaleX(0f)
            .scaleY(0f)
            .alpha(0f)
            .setDuration(300) // Reduced from 400
            .start()

        // Animate logo emerging from bubble
        cinemaFinal.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .rotationBy(360f)
            .setDuration(600) // Reduced from 800
            .withEndAction {
                // Center the final logo
                cinemaFinal.animate()
                    .x((resources.displayMetrics.widthPixels - cinemaFinal.width) / 2f)
                    .y((resources.displayMetrics.heightPixels - cinemaFinal.height) / 2f)
                    .setDuration(200) // Reduced from 300
                    .withEndAction {
                        // Show app name animation after clapper centers
                        showAppNameAnimation()
                    }
                    .start()
            }
            .start()
    }

    // NEW FUNCTION: Show app name with left-to-right animation
    private fun showAppNameAnimation() {
        // Use the existing TextView from XML
        appNameText.visibility = View.VISIBLE
        appNameText.alpha = 0f

        // Set initial position (off-screen to the left)
        val finalX = appNameText.x
        appNameText.x = -appNameText.width.toFloat()

        // Animate text sliding in from left to right with fade in
        appNameText.animate()
            .x(finalX)
            .alpha(1f)
            .setDuration(400) // Reduced from 700
            .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
            .withEndAction {
                // After text animation completes, continue with your existing zoom animation
                handler.postDelayed({
                    // First fade out the text
                    appNameText.animate()
                        .alpha(0f)
                        .setDuration(200) // Reduced from 300
                        .withEndAction {
                            // Continue with your existing zoom animation
                            startZoomAnimation()
                        }
                        .start()
                }, 400) // Reduced from 800
            }
            .start()
    }

    // EXTRACTED FUNCTION: Your existing zoom animation logic
    private fun startZoomAnimation() {

        cinemaFinal.animate()
            .scaleX(15f)
            .scaleY(15f)
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                goToNextScreen()
            }
            .start()
    }

    private fun goToNextScreen() {

        val intent = Intent(this, CineTicketBotLogin::class.java)
        startActivity(intent)

        // Smooth fade transition — fixes flashing
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        finish()
    }
}
