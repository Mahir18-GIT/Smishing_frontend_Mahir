package com.example.smishingdetectionapp.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.cardview.widget.CardView
import com.example.smishingdetectionapp.R
import com.example.smishingdetectionapp.network.ApiClient
import com.example.smishingdetectionapp.network.MessageRequest
import com.example.smishingdetectionapp.network.TextCheckerResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.widget.doOnTextChanged


class AnalyzeMessageActivity : AppCompatActivity() {

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
        val resultLabelCard = findViewById<CardView>(R.id.simple_result_card)
        val resultLabelText = findViewById<TextView>(R.id.result_text)

        resultCard.visibility = View.GONE
        resultLabelCard.visibility = View.GONE

        btnAnalyze.setOnClickListener {
            val message = inputMessage.text.toString().trim()

            if (message.isEmpty()) {
                Toast.makeText(this, "Please enter a message.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Hide old results
            resultCard.visibility = View.GONE
            resultLabelCard.visibility = View.GONE

            val request = MessageRequest(message)

            ApiClient.apiService.checkMessage(request).enqueue(object : Callback<TextCheckerResponse> {
                override fun onResponse(call: Call<TextCheckerResponse>, response: Response<TextCheckerResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()!!
                        val isSpam = result.messageIsSpam
                        val reason = result.reason
                        val fullMessage = result.message

                        // Label Card
                        resultLabelText.text = if (isSpam) "Phishing Detected ⚠️\n$reason"
                        else "Safe Message ✅\n$reason"
                        resultLabelCard.setCardBackgroundColor(ContextCompat.getColor(
                            this@AnalyzeMessageActivity,
                            if (isSpam) android.R.color.holo_red_light else android.R.color.holo_green_light
                        ))
                        resultLabelCard.visibility = View.VISIBLE

                        // Risk Level Card
                        textRiskLevel.text = if (isSpam) "Risk Level: High ❌" else "Risk Level: Safe ✅"
                        textHighlighted.text = fullMessage
                        resultCard.setBackgroundResource(
                            if (isSpam) R.drawable.card_background_high else R.drawable.card_background_safe
                        )
                        showResultCard(resultCard)

                    } else {
                        Toast.makeText(this@AnalyzeMessageActivity, "Server Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<TextCheckerResponse>, t: Throwable) {
                    Toast.makeText(this@AnalyzeMessageActivity, "API Failure: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            })
        }

        inputMessage.doOnTextChanged { text: CharSequence?, _: Int, _: Int, _: Int ->

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
