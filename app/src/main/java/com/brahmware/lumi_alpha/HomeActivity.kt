package com.brahmware.lumi_alpha

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setupRecyclerView()
        setupCollectionTiles()
        setupBottomNavigation()
        setupSearch()

        // Only show disclaimer once per app install
        val prefs = getSharedPreferences("lumi_prefs", MODE_PRIVATE)
        val shown = prefs.getBoolean("disclaimer_shown", false)
        if (!shown) {
            showDisclaimerDialog()
            prefs.edit().putBoolean("disclaimer_shown", true).apply()
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.productsRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.isNestedScrollingEnabled = false
        adapter = ProductAdapter(ProductRepository.getAll()) { product ->
            navigateTo(Intent(this, ProductDetailActivity::class.java).apply {
                putExtra("product", product)
            })
        }
        recyclerView.adapter = adapter
    }

    private fun setupCollectionTiles() {
        findViewById<View>(R.id.collectionGowns).setOnClickListener {
            adapter.updateProducts(ProductRepository.getByCategory(GownSelector.ItemType.GOWN))
        }
        findViewById<View>(R.id.collectionSuits).setOnClickListener {
            adapter.updateProducts(ProductRepository.getByCategory(GownSelector.ItemType.MALE_OUTFIT))
        }
        findViewById<View>(R.id.collectionAccessories).setOnClickListener {
            adapter.updateProducts(ProductRepository.getByCategory(GownSelector.ItemType.NECKLACE))
        }
    }

    private fun setupSearch() {
        findViewById<View>(R.id.searchButton).setOnClickListener {
            // Wire up SearchActivity later
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home    -> true
                R.id.nav_cart    -> { navigateTo(Intent(this, CartActivity::class.java)); false }
                R.id.nav_account -> { navigateTo(Intent(this, AccountActivity::class.java)); false }
                else -> false
            }
        }
    }

    private fun showDisclaimerDialog() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this, R.style.LumiDialog)
            .setTitle("Project Lumi — Alpha")
            .setMessage("This app is a prototype currently in development.\n\nThank you for testing Project Lumi!")
            .setPositiveButton("Got it") { d, _ -> d.dismiss() }
            .setCancelable(false)
            .create()
            .show()
    }
}