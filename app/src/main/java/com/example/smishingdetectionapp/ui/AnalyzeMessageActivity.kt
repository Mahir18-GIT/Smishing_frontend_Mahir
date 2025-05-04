package com.example.smishingdetectionapp.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.widget.doOnTextChanged
import com.example.smishingdetectionapp.R

class AnalyzeMessageActivity : AppCompatActivity() {

    // List of risky keywords
    private val riskyWords = listOf(
        "click", "login", "account", "urgent", "verify", "link", "update", "suspend", "payment"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analyze_message)

        // UI Elements
        val inputMessage = findViewById<EditText>(R.id.input_message)
        val btnAnalyze = findViewById<Button>(R.id.btn_analyze)
        val resultCard = findViewById<LinearLayout>(R.id.result_card)
        val textRiskLevel = findViewById<TextView>(R.id.text_risk_level)
        val textHighlighted = findViewById<TextView>(R.id.text_highlighted_result)

        // Hide result card initially
        resultCard.visibility = View.GONE

        // Analyze button logic
        btnAnalyze.setOnClickListener {
            val message = inputMessage.text.toString().lowercase().trim()

            if (message.isBlank()) {
                textRiskLevel.text = "Risk Level: -"
                textHighlighted.text = "Please enter a message."
                resultCard.setBackgroundResource(R.drawable.result_card_background) // Default bg
                showResultCard(resultCard)
                return@setOnClickListener
            }

            val foundWords = riskyWords.filter { message.contains(it) }

            // Determine risk level and background color
            val (riskLevel, backgroundDrawable) = when {
                foundWords.size >= 4 -> "High Risk ❌" to R.drawable.card_background_high
                foundWords.size >= 2 -> "Caution ⚠️" to R.drawable.card_background_caution
                foundWords.isNotEmpty() -> "Suspicious ⚠️" to R.drawable.card_background_caution
                else -> "Safe ✅" to R.drawable.card_background_safe
            }

            // Highlight risky words in red
            var highlightedText = message
            foundWords.forEach {
                highlightedText = highlightedText.replace(
                    it,
                    "<b><font color='red'>$it</font></b>",
                    ignoreCase = true
                )
            }

            // Set final result
            textRiskLevel.text = "Risk Level: $riskLevel"
            textHighlighted.text = HtmlCompat.fromHtml(highlightedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
            resultCard.setBackgroundResource(backgroundDrawable)

            showResultCard(resultCard)
        }

        // Hide result card when input is cleared
        inputMessage.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()) {
                resultCard.visibility = View.GONE
            }
        }
    }

    // Helper for animated result reveal
    private fun showResultCard(card: View) {
        card.visibility = View.VISIBLE
        card.alpha = 0f
        card.animate().alpha(1f).setDuration(400).start()
    }
}
