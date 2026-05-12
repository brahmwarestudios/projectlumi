package com.brahmware.lumi_alpha

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class AccountActivity : AppCompatActivity() {

    data class FaqItem(val question: String, val answer: String)

    private val faqs = listOf(
        FaqItem(
            "How long can I rent a gown?",
            "Standard rental is up to 7 days. Extended rental beyond 7 days is available upon request and may incur additional charges."
        ),
        FaqItem(
            "What if the gown doesn't fit?",
            "We offer size consultations before rental. If the item doesn't fit upon pickup, we will do our best to find an alternative from available stock."
        ),
        FaqItem(
            "Can I cancel my rental?",
            "Cancellations must be made at least 48 hours before the rental date for a full refund. Late cancellations may forfeit the deposit."
        ),
        FaqItem(
            "How does AR Try-On work?",
            "Our AR feature uses your device camera to show how the gown looks on you virtually. Simply tap 'Try On with AR' on any product page."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        setupFaqs()
        setupButtons()
        setupBottomNav()
    }

    private fun setupFaqs() {
        val faqViews = listOf(
            R.id.faq1, R.id.faq2, R.id.faq3, R.id.faq4
        )

        faqViews.forEachIndexed { index, viewId ->
            val faqView = findViewById<LinearLayout>(viewId)
            val faq = faqs[index]

            val questionText = faqView.findViewById<TextView>(R.id.faqQuestion)
            val answerText   = faqView.findViewById<TextView>(R.id.faqAnswer)
            val chevron      = faqView.findViewById<ImageView>(R.id.faqChevron)
            val questionRow  = faqView.findViewById<LinearLayout>(R.id.faqQuestionRow)

            questionText.text = faq.question
            answerText.text   = faq.answer

            questionRow.setOnClickListener {
                val isExpanded = answerText.visibility == View.VISIBLE
                answerText.visibility = if (isExpanded) View.GONE else View.VISIBLE
                chevron.setImageResource(
                    if (isExpanded) R.drawable.ic_chevron_down else R.drawable.ic_chevron_up
                )
            }
        }
    }

    private fun setupButtons() {
        findViewById<LinearLayout>(R.id.rulesRow).setOnClickListener {
            startActivity(Intent(this, RulesActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.logoutButton).setOnClickListener {
            // Will hook into Supabase auth later — for now go to home
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavAccount)
        nav.selectedItemId = R.id.nav_account
        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_cart    -> { startActivity(Intent(this, CartActivity::class.java)); false }
                R.id.nav_account -> true
                else -> false
            }
        }
    }
}