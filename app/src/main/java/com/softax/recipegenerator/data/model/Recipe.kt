package com.softax.recipegenerator.data.model

data class Recipe(
    val name: String,
    val description: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val cookingTimeMinutes: Int,
    val servings: Int,
    val difficulty: DifficultyLevel,
    val cuisineType: CuisineType,
    val dietaryRestrictions: List<DietaryRestriction>,
    val missingIngredients: List<String> = emptyList(),
    val matchType: RecipeMatchType = RecipeMatchType.EXACT_MATCH
)

enum class RecipeMatchType(val displayName: String) {
    EXACT_MATCH("Can make now"),
    NEEDS_ONE_EXTRA("Needs 1 extra item"),
    NEEDS_TWO_EXTRA("Needs 2 extra items")
}
