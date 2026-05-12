package com.brahmware.lumi_alpha

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setupRecyclerView()
        setupCollectionTiles()
        setupBottomNavigation()
        setupSearch()
        showDisclaimerDialog()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.productsRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.isNestedScrollingEnabled = false

        adapter = ProductAdapter(ProductRepository.getAll()) { product ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("product", product)
            startActivity(intent)
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
            // Wire up SearchActivity in a future step
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home    -> true
                R.id.nav_cart    -> { startActivity(Intent(this, CartActivity::class.java)); false }
                R.id.nav_account -> { startActivity(Intent(this, AccountActivity::class.java)); false }
                else -> false
            }
        }
    }

    private fun showDisclaimerDialog() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Project Lumi — Alpha")
            .setMessage("This app is a prototype currently in development.\n\nThank you for testing Project Lumi!")
            .setPositiveButton("Got it") { d, _ -> d.dismiss() }
            .setCancelable(false)
            .create()
            .show()
    }
}