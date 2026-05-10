package com.brahmware.lumi_alpha

object ProductRepository {

    fun getAll(): List<Product> = listOf(
        Product(
            id = 1,
            name = "Galaxy Gown",
            brand = "Brahmware Couture",
            description = "A stunning galaxy-themed ball gown adorned with sparkling crystals and a flowing skirt that captures the beauty of the cosmos. Perfect for galas and formal events.",
            rentalPricePerDay = 1500,
            imageRes = R.drawable.lumi_gown_1,
            itemType = GownSelector.ItemType.GOWN,
            availableSizes = listOf("XS", "S", "M", "L", "XL")
        ),
        Product(
            id = 2,
            name = "Crimson Elegance",
            brand = "Brahmware Couture",
            description = "A bold red evening gown with a dramatic silhouette. Features intricate draping and a sweetheart neckline that exudes confidence and grace.",
            rentalPricePerDay = 1200,
            imageRes = R.drawable.lumi_gown_2,
            itemType = GownSelector.ItemType.GOWN,
            availableSizes = listOf("S", "M", "L")
        ),
        Product(
            id = 3,
            name = "Blush Dream",
            brand = "Brahmware Couture",
            description = "A soft blush pink gown with delicate floral embroidery. Lightweight and ethereal, ideal for garden parties and debut celebrations.",
            rentalPricePerDay = 1000,
            imageRes = R.drawable.lumi_gown_3,
            itemType = GownSelector.ItemType.GOWN,
            availableSizes = listOf("XS", "S", "M", "L", "XL")
        ),
        Product(
            id = 4,
            name = "Pearl Necklace",
            brand = "Lumi Accessories",
            description = "Classic freshwater pearl necklace with a sterling silver clasp. Timeless elegance that complements any formal outfit.",
            rentalPricePerDay = 350,
            imageRes = R.drawable.necklace_1,
            itemType = GownSelector.ItemType.NECKLACE,
            availableSizes = listOf("One Size")
        ),
        Product(
            id = 5,
            name = "Diamond Choker",
            brand = "Lumi Accessories",
            description = "A dazzling choker featuring hand-set crystal diamonds in a gold-plated setting. The perfect statement piece for any occasion.",
            rentalPricePerDay = 500,
            imageRes = R.drawable.necklace_2,
            itemType = GownSelector.ItemType.NECKLACE,
            availableSizes = listOf("One Size")
        ),
        Product(
            id = 6,
            name = "Sapphire Strand",
            brand = "Lumi Accessories",
            description = "A luxurious multi-strand necklace featuring deep blue sapphire crystals. Pairs beautifully with both formal and semi-formal attire.",
            rentalPricePerDay = 400,
            imageRes = R.drawable.necklace_3,
            itemType = GownSelector.ItemType.NECKLACE,
            availableSizes = listOf("One Size")
        ),
        Product(
            id = 7,
            name = "Classic Barong",
            brand = "Brahmware Couture",
            description = "A premium embroidered Barong Tagalog crafted from fine pineapple fiber. Traditional Filipino formal wear reimagined for the modern gentleman.",
            rentalPricePerDay = 800,
            imageRes = R.drawable.male_outfit_1,
            itemType = GownSelector.ItemType.MALE_OUTFIT,
            availableSizes = listOf("S", "M", "L", "XL", "XXL")
        ),
        Product(
            id = 8,
            name = "Modern Suit Top",
            brand = "Brahmware Couture",
            description = "A slim-fit formal blazer in midnight navy. Features a two-button closure and structured shoulders for a sharp, contemporary look.",
            rentalPricePerDay = 900,
            imageRes = R.drawable.male_outfit_2,
            itemType = GownSelector.ItemType.MALE_OUTFIT,
            availableSizes = listOf("S", "M", "L", "XL")
        ),
        Product(
            id = 9,
            name = "Formal Vest",
            brand = "Brahmware Couture",
            description = "A tailored formal vest in charcoal gray. Versatile and stylish, pairs perfectly with dress shirts for semiformal occasions.",
            rentalPricePerDay = 600,
            imageRes = R.drawable.male_outfit_3,
            itemType = GownSelector.ItemType.MALE_OUTFIT,
            availableSizes = listOf("S", "M", "L", "XL")
        )
    )

    fun getByCategory(type: GownSelector.ItemType) = getAll().filter { it.itemType == type }
}