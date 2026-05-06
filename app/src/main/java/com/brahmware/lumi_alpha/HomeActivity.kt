package com.brahmware.lumi_alpha

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.ChipGroup

class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setupRecyclerView()
        setupCategoryChips()
        setupBottomNavigation()
        showDisclaimerDialog()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.productsRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        adapter = ProductAdapter(ProductRepository.getAll()) { product ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("product", product)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    private fun setupCategoryChips() {
        val chipGroup = findViewById<ChipGroup>(R.id.categoryChipGroup)
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val filtered = when {
                checkedIds.contains(R.id.chipGowns) ->
                    ProductRepository.getByCategory(GownSelector.ItemType.GOWN)
                checkedIds.contains(R.id.chipNecklaces) ->
                    ProductRepository.getByCategory(GownSelector.ItemType.NECKLACE)
                checkedIds.contains(R.id.chipMale) ->
                    ProductRepository.getByCategory(GownSelector.ItemType.MALE_OUTFIT)
                else -> ProductRepository.getAll()
            }
            adapter.updateProducts(filtered)
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_categories -> true
                R.id.nav_wishlist -> true
                R.id.nav_account -> true
                else -> false
            }
        }
    }

    private fun showDisclaimerDialog() {
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(
            this, R.style.LumiDialog
        )
            .setTitle("Project Lumi — Alpha")
            .setMessage(
                "This app is a prototype project currently in development.\n\n" +
                        "Features and visuals are subject to change and may not reflect " +
                        "the final intended experience.\n\n" +
                        "Thank you for testing Project Lumi!"
            )
            .setPositiveButton("Got it") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()

        dialog.show()
    }
}