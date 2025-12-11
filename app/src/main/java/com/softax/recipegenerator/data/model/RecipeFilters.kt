package com.softax.recipegenerator.data.model

data class RecipeFilters(
    val dietaryRestrictions: List<DietaryRestriction> = emptyList(),
    val cuisineType: CuisineType = CuisineType.ANY,
    val maxCookingTime: Int? = null,
    val difficulty: DifficultyLevel = DifficultyLevel.ANY,
    val showExactMatchOnly: Boolean = false,
    val showNeedsOneExtra: Boolean = true,
    val showNeedsTwoExtra: Boolean = true
)
