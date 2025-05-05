package com.example.smishingdetectionapp.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.widget.doOnTextChanged
import androidx.cardview.widget.CardView // ✅ FIX: using the correct CardView import
import com.example.smishingdetectionapp.R

class AnalyzeMessageActivity : AppCompatActivity() {

    private val riskyWords = listOf(
        "click", "login", "account", "urgent", "verify", "link", "update", "suspend", "payment"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analyze_message)
        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }


        // UI Elements
        val inputMessage = findViewById<EditText>(R.id.input_message)
        val btnAnalyze = findViewById<Button>(R.id.btn_analyze)
        val resultCard = findViewById<LinearLayout>(R.id.result_card)
        val textRiskLevel = findViewById<TextView>(R.id.text_risk_level)
        val textHighlighted = findViewById<TextView>(R.id.text_highlighted_result)

        // Optional simple result card
        val resultLabelCard = findViewById<CardView>(R.id.simple_result_card) // ✅ FIX: use correct type
        val resultLabelText = findViewById<TextView>(R.id.result_text)

        resultCard.visibility = View.GONE
        resultLabelCard.visibility = View.GONE

        btnAnalyze.setOnClickListener {
            val message = inputMessage.text.toString().trim().lowercase()

            if (message.isBlank()) {
                textRiskLevel.text = "Risk Level: -"
                textHighlighted.text = "Please enter a message."
                resultCard.setBackgroundResource(R.drawable.result_card_background)
                resultLabelCard.visibility = View.GONE
                showResultCard(resultCard)
                return@setOnClickListener
            }

            val foundWords = riskyWords.filter { message.contains(it, ignoreCase = true) }

            // Determine severity
            val (riskLevel, backgroundDrawable) = when {
                foundWords.size >= 4 -> "High Risk ❌" to R.drawable.card_background_high
                foundWords.size >= 2 -> "Caution ⚠️" to R.drawable.card_background_caution
                foundWords.isNotEmpty() -> "Suspicious ⚠️" to R.drawable.card_background_caution
                else -> "Safe ✅" to R.drawable.card_background_safe
            }

            // Highlight risky words
            var highlighted = message
            foundWords.forEach {
                highlighted = highlighted.replace(
                    it,
                    "<b><font color='red'>$it</font></b>",
                    ignoreCase = true
                )
            }

            textRiskLevel.text = "Risk Level: $riskLevel"
            textHighlighted.text = HtmlCompat.fromHtml(highlighted, HtmlCompat.FROM_HTML_MODE_LEGACY)
            resultCard.setBackgroundResource(backgroundDrawable)
            showResultCard(resultCard)

            // Update label card
            if (foundWords.isNotEmpty()) {
                resultLabelText.text = "Phishing Detected ⚠️"
                resultLabelCard.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
            } else {
                resultLabelText.text = "Safe Message ✅"
                resultLabelCard.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
            }
            resultLabelCard.visibility = View.VISIBLE
        }

        inputMessage.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()) {
                resultCard.visibility = View.GONE
                resultLabelCard.visibility = View.GONE
            }
        }
    }

    private fun showResultCard(card: View) {
        card.visibility = View.VISIBLE
        card.alpha = 0f
        card.animate().alpha(1f).setDuration(400).start()
    }
}
