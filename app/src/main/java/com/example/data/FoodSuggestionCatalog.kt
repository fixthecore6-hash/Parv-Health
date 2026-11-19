package com.example.data

data class FoodSuggestion(
    val id: String,
    val name: String,
    val mealType: MealType,
    val description: String,
    val calories: Int,
    val proteinG: Float,
    val carbsG: Float,
    val fatG: Float,
    val ingredients: List<String>,
    val isVegetarian: Boolean,
    val isHighProtein: Boolean
)

object FoodSuggestionCatalog {

    val allSuggestions: List<FoodSuggestion> = listOf(
        // ==================== BREAKFAST (9) ====================
        FoodSuggestion(
            id = "sug_oats_banana",
            name = "Warm Oatmeal & Banana Bowl",
            mealType = MealType.BREAKFAST,
            description = "Creamy rolled oats cooked in milk with sliced banana and toasted seeds.",
            calories = 340,
            proteinG = 13f,
            carbsG = 56f,
            fatG = 7f,
            ingredients = listOf("50 g rolled oats", "200 ml milk", "1 medium banana", "1 tsp pumpkin seeds"),
            isVegetarian = true,
            isHighProtein = false
        ),
        FoodSuggestion(
            id = "sug_eggs_toast",
            name = "Scrambled Eggs & Whole-Wheat Toast",
            mealType = MealType.BREAKFAST,
            description = "Two fluffy eggs scrambled lightly with spinach beside crisp whole-grain toast.",
            calories = 330,
            proteinG = 19f,
            carbsG = 28f,
            fatG = 15f,
            ingredients = listOf("2 whole eggs", "2 slices whole-wheat bread", "1 handful spinach", "1 tsp olive oil"),
            isVegetarian = false,
            isHighProtein = true
        ),
        FoodSuggestion(
            id = "sug_greek_yogurt_parfait",
            name = "Greek Yogurt Berry Parfait",
            mealType = MealType.BREAKFAST,
            description = "Thick Greek yogurt layered with crisp apple cubes, berries, and crunchy oats.",
            calories = 280,
            proteinG = 21f,
            carbsG = 35f,
            fatG = 5f,
            ingredients = listOf("180 g Greek yogurt", "1/2 diced apple", "30 g toasted oats", "Cinnamon"),
            isVegetarian = true,
            isHighProtein = true
        ),
        FoodSuggestion(
            id = "sug_besan_chilla",
            name = "Savory Chickpea Flour Pancakes",
            mealType = MealType.BREAKFAST,
            description = "Thin golden gram-flour crepes folded with finely chopped onion, tomato, and coriander.",
            calories = 310,
            proteinG = 14f,
            carbsG = 42f,
            fatG = 9f,
            ingredients = listOf("75 g chickpea flour", "1/4 chopped onion", "1/2 tomato", "1 tsp cooking oil"),
            isVegetarian = true,
            isHighProtein = false
        ),
        FoodSuggestion(
            id = "sug_egg_white_omelet",
            name = "High-Protein Egg & Cottage Cheese Omelet",
            mealType = MealType.BREAKFAST,
            description = "Egg omelet stuffed with crumbled paneer or cottage cheese and diced bell peppers.",
            calories = 370,
            proteinG = 31f,
            carbsG = 8f,
            fatG = 23f,
            ingredients = listOf("2 eggs + 2 whites", "60 g crumbled paneer", "Diced bell pepper", "Black pepper"),
            isVegetarian = false,
            isHighProtein = true
        ),
        FoodSuggestion(
            id = "sug_peanut_toast",
            name = "Peanut Butter Banana Toast",
            mealType = MealType.BREAKFAST,
            description = "Two toasted whole-wheat slices topped with smooth peanut butter and banana coins.",
            calories = 390,
            proteinG = 14f,
            carbsG = 49f,
            fatG = 16f,
            ingredients = listOf("2 slices whole-wheat bread", "2 tbsp peanut butter", "1 small banana"),
            isVegetarian = true,
            isHighProtein = false
        ),
        FoodSuggestion(
            id = "sug_poha_peas",
            name = "Vegetable Flattened Rice Poha",
            mealType = MealType.BREAKFAST,
            description = "Light steamed flattened rice tossed with green peas, peanuts, and fresh lemon.",
            calories = 320,
            proteinG = 9f,
            carbsG = 52f,
            fatG = 9f,
            ingredients = listOf("70 g poha", "40 g green peas", "1 tbsp roasted peanuts", "Lemon juice"),
            isVegetarian = true,
            isHighProtein = false
        ),
        FoodSuggestion(
            id = "sug_protein_smoothie",
            name = "Milk, Oats & Yogurt Smoothie",
            mealType = MealType.BREAKFAST,
            description = "Blended cold milk, Greek yogurt, oats, and banana for quick morning fuel.",
            calories = 360,
            proteinG = 24f,
            carbsG = 51f,
            fatG = 7f,
            ingredients = listOf("200 ml milk", "120 g Greek yogurt", "30 g oats", "1/2 banana"),
            isVegetarian = true,
            isHighProtein = true
        ),
        FoodSuggestion(
            id = "sug_chicken_egg_wrap",
            name = "Shredded Chicken & Egg Breakfast Wrap",
            mealType = MealType.BREAKFAST,
            description = "Warm flatbread rolled with scrambled egg, leftover chicken breast, and greens.",
            calories = 420,
            proteinG = 36f,
            carbsG = 32f,
            fatG = 15f,
            ingredients = listOf("1 flatbread", "80 g chicken breast", "1 egg", "Baby greens"),
            isVegetarian = false,
            isHighProtein = true
        ),

        // ==================== LUNCH (9) ====================
        FoodSuggestion(
            id = "sug_chicken_rice_bowl",
            name = "Grilled Chicken & Herb Rice Bowl",
            mealType = MealType.LUNCH,
            description = "Seasoned chicken breast sliced over steamed rice with crisp cucumbers and tomatoes.",
            calories = 490,
            proteinG = 42f,
            carbsG = 54f,
            fatG = 10f,
            ingredients = listOf("130 g chicken breast", "160 g cooked rice", "1 cup cucumber & tomato", "Lemon & herbs"),
            isVegetarian = false,
            isHighProtein = true
        ),
        FoodSuggestion(
            id = "sug_lentil_rice_plate",
            name = "Spiced Lentils & Steamed Rice",
            mealType = MealType.LUNCH,
            description = "Slow-simmered yellow or brown lentils served alongside warm rice and cucumber salad.",
            calories = 450,
            proteinG = 19f,
            carbsG = 76f,
            fatG = 7f,
            ingredients = listOf("220 g cooked lentils", "160 g cooked rice", "1 tsp oil & cumin", "Sliced cucumber"),
            isVegetarian = true,
            isHighProtein = false
        ),
        FoodSuggestion(
            id = "sug_paneer_wrap",
            name = "Grilled Paneer & Veggie Flatbread",
            mealType = MealType.LUNCH,
            description = "Lightly seared paneer cubes tucked into whole-wheat flatbread with crisp lettuce and yogurt sauce.",
            calories = 470,
            proteinG = 24f,
            carbsG = 42f,
            fatG = 22f,
            ingredients = listOf("100 g paneer", "1 whole-wheat flatbread", "2 tbsp Greek yogurt", "Lettuce & onion"),
            isVegetarian = true,
            isHighProtein = true
        ),
        FoodSuggestion(
            id = "sug_chickpea_salad",
            name = "Chickpea & Feta Garden Bowl",
            mealType = MealType.LUNCH,
            description = "Boiled chickpeas tossed with diced tomatoes, cucumber, herbs, and olive oil.",
            calories = 410,
            proteinG = 17f,
            carbsG = 52f,
            fatG = 14f,
            ingredients = listOf("180 g cooked chickpeas", "1 tomato", "1/2 cucumber", "1 tbsp olive oil & lemon"),
            isVegetarian = true,
            isHighProtein = false
        ),
        FoodSuggestion(
            id = "sug_egg_salad_sandwich",
            name = "Yogurt-Herb Egg Salad Sandwich",
            mealType = MealType.LUNCH,
            description = "Chopped hard-boiled eggs mixed with creamy Greek yogurt and mustard on whole-wheat bread.",
            calories = 390,
            proteinG = 23f,
            carbsG = 34f,
            fatG = 17f,
            ingredients = listOf("2 boiled eggs", "2 tbsp Greek yogurt", "2 slices whole-wheat bread", "Mustard & celery"),
            isVegetarian = false,
            isHighProtein = true
        ),
        FoodSuggestion(
            id = "sug_rajma_rice",
            name = "Kidney Bean Curry & Rice",
            mealType = MealType.LUNCH,
            description = "Comforting tomato-simmered kidney beans served over fluffy steamed rice.",
            calories = 480,
            proteinG = 18f,
            carbsG = 82f,
            fatG = 7f,
            ingredients = listOf("200 g cooked kidney beans", "160 g cooked rice", "Tomato & ginger", "1 tsp oil"),
            isVegetarian = true,
            isHighProtein = false
        ),
        FoodSuggestion(
            id = "sug_tuna_toast_bowl",
            name = "Seared Fish & Quinoa Greens",
            mealType = MealType.LUNCH,
            description = "Flaky pan-seared fish fillet over warm grains with steamed green beans.",
            calories = 440,
            proteinG = 35f,
            carbsG = 40f,
            fatG = 14f,
            ingredients = listOf("140 g white fish or salmon", "130 g cooked grains", "100 g green beans", "Lemon wedge"),
            isVegetarian = false,
            isHighProtein = true
        ),
        FoodSuggestion(
            id = "sug_tofu_stirfry",
            name = "Crispy Tofu & Broccoli Rice Bowl",
            mealType = MealType.LUNCH,
            description = "Pan-browned firm tofu cubes tossed with broccoli florets over steamed rice.",
            calories = 430,
            proteinG = 23f,
            carbsG = 51f,
            fatG = 14f,
            ingredients = listOf("150 g firm tofu", "120 g broccoli", "140 g cooked rice", "1 tsp soy sauce & sesame"),
            isVegetarian = true,
            isHighProtein = true
        ),
        FoodSuggestion(
            id = "sug_chicken_sandwich",
            name = "Herb Chicken & Avocado Sandwich",
            mealType = MealType.LUNCH,
            description = "Sliced chicken breast layered with tomato and greens between whole-grain bread.",
            calories = 450,
            proteinG = 37f,
            carbsG = 38f,
            fatG = 15f,
            ingredients = listOf("110 g chicken breast", "2 slices whole-wheat bread", "1/4 avocado", "Tomato & lettuce"),
            isVegetarian = false,
            isHighProtein = true
        ),

        // ==================== DINNER (9) ====================
        FoodSuggestion(
            id = "sug_baked_chicken_veggies",
            name = "Baked Chicken & Roasted Vegetables",
            mealType = MealType.DINNER,
            description = "Herbed chicken breast roasted beside carrots, zucchini, and baby potatoes.",
            calories = 460,
            proteinG = 40f,
            carbsG = 38f,
            fatG = 15f,
            ingredients = listOf("130 g chicken breast", "150 g baby potatoes", "150 g mixed veggies", "1 tbsp olive oil"),
            isVegetarian = false,
            isHighProtein = true
        ),
        FoodSuggestion(
            id = "sug_palak_paneer_roti",
            name = "Spinach Paneer & Two Whole-Wheat Rotis",
            mealType = MealType.DINNER,
            description = "Smooth pureed spinach simmered gently with soft paneer cubes and two thin rotis.",
            calories = 510,
            proteinG = 25f,
            carbsG = 48f,
            fatG = 24f,
            ingredients = listOf("100 g paneer", "150 g spinach puree", "2 whole-wheat rotis", "Garlic & spices"),
            isVegetarian = true,
            isHighProtein = true
        ),
        FoodSuggestion(
            id = "sug_lentil_veggie_soup",
            name = "Hearty Lentil & Vegetable Soup with Bread",
            mealType = MealType.DINNER,
            description = "Warm bowl of red lentils, carrots, and celery paired with toasted whole-wheat bread.",
            calories = 380,
            proteinG = 18f,
            carbsG = 61f,
            fatG = 6f,
            ingredients = listOf("250 g lentil vegetable soup", "1 slice whole-wheat bread", "1 tsp olive oil"),
            isVegetarian = true,
            isHighProtein = false
        ),
        FoodSuggestion(
            id = "sug_egg_curry_rice",
            name = "Spiced Egg Masala & Steamed Rice",
            mealType = MealType.DINNER,
            description = "Two boiled eggs simmered in light onion-tomato gravy with a moderate portion of rice.",
            calories = 460,
            proteinG = 21f,
            carbsG = 53f,
            fatG = 18f,
            ingredients = listOf("2 boiled eggs", "150 g cooked rice", "Tomato & onion base", "1 tsp oil"),
            isVegetarian = false,
            isHighProtein = true
        ),
        FoodSuggestion(
            id = "sug_paneer_tikka_salad",
            name = "Spiced Paneer & Bell Pepper Skillet",
            mealType = MealType.DINNER,
            description = "Yogurt-marinated paneer strips seared with crunchy bell peppers and red onions.",
            calories = 400,
            proteinG = 24f,
            carbsG = 16f,
            fatG = 26f,
            ingredients = listOf("120 g paneer", "1 bell pepper", "1/2 onion", "2 tbsp Greek yogurt"),
            isVegetarian = true,
            isHighProtein = true
        ),
        FoodSuggestion(
            id = "sug_veggie_khichdi",
            name = "Moong Lentil & Rice Khichdi",
            mealType = MealType.DINNER,
            description = "Soft comfort dish of rice and split lentils cooked with diced carrots and peas.",
            calories = 410,
            proteinG = 16f,
            carbsG = 68f,
            fatG = 7f,
            ingredients = listOf("50 g dry rice", "50 g split moong dal", "Mixed carrots & peas", "1 tsp ghee or oil"),
            isVegetarian = true,
            isHighProtein = false
        ),
        FoodSuggestion(
            id = "sug_fish_sweet_potato",
            name = "Pan-Seared Fish & Sweet Potato",
            mealType = MealType.DINNER,
            description = "Tender seasoned fish served beside roasted sweet potato cubes and steamed greens.",
            calories = 450,
            proteinG = 34f,
            carbsG = 42f,
            fatG = 15f,
            ingredients = listOf("140 g fish fillet", "160 g roasted sweet potato", "1 cup steamed broccoli", "1 tsp olive oil"),
            isVegetarian = false,
            isHighProtein = true
        ),
        FoodSuggestion(
            id = "sug_chicken_stirfry_noodles",
            name = "Chicken & Mixed Veggie Skillet",
            mealType = MealType.DINNER,
            description = "Quick stir-fried chicken strips with cabbage, carrots, and scallions over light rice.",
            calories = 430,
            proteinG = 38f,
            carbsG = 41f,
            fatG = 11f,
            ingredients = listOf("125 g chicken breast", "150 g stir-fry veggies", "110 g cooked rice", "Garlic & soy"),
            isVegetarian = false,
            isHighProtein = true
        ),
        FoodSuggestion(
            id = "sug_soya_roti_bowl",
            name = "Soy Chunk Curry & Greens",
            mealType = MealType.DINNER,
            description = "Protein-rich hydrated soy chunks cooked in light tomato gravy with one whole-wheat roti.",
            calories = 390,
            proteinG = 29f,
            carbsG = 46f,
            fatG = 8f,
            ingredients = listOf("50 g dry soy chunks", "1 whole-wheat roti", "Tomato gravy", "Fresh cilantro"),
            isVegetarian = true,
            isHighProtein = true
        ),

        // ==================== SNACKS (9) ====================
        FoodSuggestion(
            id = "sug_apple_almonds",
            name = "Crisp Apple & Roasted Almonds",
            mealType = MealType.SNACKS,
            description = "Fresh sliced apple paired with a small handful of unsalted crunchy almonds.",
            calories = 180,
            proteinG = 4f,
            carbsG = 26f,
            fatG = 8f,
            ingredients = listOf("1 medium apple", "12 roasted almonds"),
            isVegetarian = true,
            isHighProtein = false
        ),
        FoodSuggestion(
            id = "sug_yogurt_cup",
            name = "Plain Greek Yogurt & Cinnamon",
            mealType = MealType.SNACKS,
            description = "Cool protein-packed Greek yogurt dusted lightly with cinnamon.",
            calories = 115,
            proteinG = 17f,
            carbsG = 6f,
            fatG = 1f,
            ingredients = listOf("170 g Greek yogurt", "Pinch of cinnamon"),
            isVegetarian = true,
            isHighProtein = true
        ),
        FoodSuggestion(
            id = "sug_boiled_eggs",
            name = "Two Hard-Boiled Eggs",
            mealType = MealType.SNACKS,
            description = "Simple, portable hard-boiled eggs seasoned with black pepper.",
            calories = 155,
            proteinG = 13f,
            carbsG = 1f,
            fatG = 11f,
            ingredients = listOf("2 whole eggs", "Pinch of salt & pepper"),
            isVegetarian = false,
            isHighProtein = true
        ),
        FoodSuggestion(
            id = "sug_roasted_chickpeas",
            name = "Crunchy Roasted Chickpeas",
            mealType = MealType.SNACKS,
            description = "Oven-crisped chickpeas tossed lightly in cumin and paprika.",
            calories = 160,
            proteinG = 8f,
            carbsG = 24f,
            fatG = 3f,
            ingredients = listOf("85 g roasted chickpeas", "Cumin & paprika"),
            isVegetarian = true,
            isHighProtein = false
        ),
        FoodSuggestion(
            id = "sug_banana_milk",
            name = "Banana & Chilled Milk",
            mealType = MealType.SNACKS,
            description = "Simple pre- or post-workout snack combining natural carbs with dairy protein.",
            calories = 225,
            proteinG = 8f,
            carbsG = 36f,
            fatG = 6f,
            ingredients = listOf("1 medium banana", "200 ml milk"),
            isVegetarian = true,
            isHighProtein = false
        ),
        FoodSuggestion(
            id = "sug_paneer_cubes",
            name = "Lightly Seared Paneer Cubes",
            mealType = MealType.SNACKS,
            description = "Warm pan-toasted paneer bites dusted with black pepper and lemon.",
            calories = 210,
            proteinG = 15f,
            carbsG = 2f,
            fatG = 16f,
            ingredients = listOf("80 g paneer", "Black pepper & lemon juice"),
            isVegetarian = true,
            isHighProtein = true
        ),
        FoodSuggestion(
            id = "sug_cucumber_hummus",
            name = "Cucumber Sticks & Hummus",
            mealType = MealType.SNACKS,
            description = "Cool crunchy cucumber spears dipped in smooth chickpea hummus.",
            calories = 145,
            proteinG = 5f,
            carbsG = 14f,
            fatG = 8f,
            ingredients = listOf("1 cucumber", "60 g hummus"),
            isVegetarian = true,
            isHighProtein = false
        ),
        FoodSuggestion(
            id = "sug_chicken_bites",
            name = "Herb Grilled Chicken Strips",
            mealType = MealType.SNACKS,
            description = "Bite-sized seasoned chicken breast strips for a savory protein boost.",
            calories = 165,
            proteinG = 31f,
            carbsG = 0f,
            fatG = 4f,
            ingredients = listOf("100 g chicken breast", "Garlic herbs"),
            isVegetarian = false,
            isHighProtein = true
        ),
        FoodSuggestion(
            id = "sug_sprouts_chaat",
            name = "Moong Sprouts Bowl",
            mealType = MealType.SNACKS,
            description = "Steamed green moong sprouts tossed with tomato, cucumber, and fresh lemon.",
            calories = 140,
            proteinG = 10f,
            carbsG = 22f,
            fatG = 1f,
            ingredients = listOf("120 g steamed moong sprouts", "Tomato & cucumber", "Lemon juice"),
            isVegetarian = true,
            isHighProtein = false
        )
    )

    fun filterSuggestions(
        dietType: DietType,
        mealType: MealType? = null,
        remainingCalories: Int? = null,
        onlyFitRemaining: Boolean = false
    ): List<FoodSuggestion> {
        return allSuggestions.filter { suggestion ->
            val mealOk = mealType == null || suggestion.mealType == mealType
            val dietOk = when (dietType) {
                DietType.VEGETARIAN -> suggestion.isVegetarian
                DietType.HIGH_PROTEIN -> suggestion.isHighProtein || suggestion.proteinG >= 15f
                DietType.BALANCED, DietType.CUSTOM -> true
            }
            val calOk = if (onlyFitRemaining && remainingCalories != null) {
                suggestion.calories <= remainingCalories.coerceAtLeast(0)
            } else {
                true
            }
            mealOk && dietOk && calOk
        }
    }
}
