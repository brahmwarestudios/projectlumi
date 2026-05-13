package com.brahmware.lumi_alpha

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class AccountActivity : BaseActivity() {

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

        // Populate user info from session
        val session = SessionManager.getSession(this)
        if (session != null) {
            findViewById<TextView>(R.id.accountUserName).text = session.name
            findViewById<TextView>(R.id.accountMemberType).text =
                if (session.role == SessionManager.Role.ADMIN) "Administrator" else "Premium Member"
        }

        setupFaqs()
        setupButtons()
        setupBottomNav()
    }

    private fun setupFaqs() {
        val faqViewIds = listOf(R.id.faq1, R.id.faq2, R.id.faq3, R.id.faq4)
        faqViewIds.forEachIndexed { index, viewId ->
            val faqView    = findViewById<LinearLayout>(viewId)
            val faq        = faqs[index]
            val questionTv = faqView.findViewById<TextView>(R.id.faqQuestion)
            val answerTv   = faqView.findViewById<TextView>(R.id.faqAnswer)
            val chevron    = faqView.findViewById<ImageView>(R.id.faqChevron)
            val row        = faqView.findViewById<LinearLayout>(R.id.faqQuestionRow)

            questionTv.text = faq.question
            answerTv.text   = faq.answer

            row.setOnClickListener {
                val expanded = answerTv.visibility == View.VISIBLE
                answerTv.visibility = if (expanded) View.GONE else View.VISIBLE
                chevron.setImageResource(
                    if (expanded) R.drawable.ic_chevron_down else R.drawable.ic_chevron_up
                )
            }
        }
    }

    private fun setupButtons() {
        findViewById<LinearLayout>(R.id.rulesRow).setOnClickListener {
            navigateTo(Intent(this, RulesActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.logoutButton).setOnClickListener {
            SessionManager.clearSession(this)
            // Clear the entire back stack and go to login
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }
    }

    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavAccount)
        nav.selectedItemId = R.id.nav_account
        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Go back to existing HomeActivity on the stack — don't create a new one
                    val intent = Intent(this, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    finish()
                    true
                }
                R.id.nav_cart -> {
                    navigateTo(Intent(this, CartActivity::class.java))
                    false
                }
                R.id.nav_account -> true
                else -> false
            }
        }
    }
}