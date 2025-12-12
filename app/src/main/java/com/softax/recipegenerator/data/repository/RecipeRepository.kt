package com.softax.recipegenerator.data.repository

import com.softax.recipegenerator.data.model.Recipe
import com.softax.recipegenerator.data.model.RecipeFilters
import com.softax.recipegenerator.data.model.RecipeMatchType
import com.softax.recipegenerator.data.service.GeminiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class RecipeRepository(
    private val geminiService: GeminiService,
    private val ingredientRepository: IngredientRepository
) {

    suspend fun generateRecipes(filters: RecipeFilters): Result<List<Recipe>> =
        withContext(Dispatchers.IO) {
            try {

                val availableIngredients = ingredientRepository.getAvailableIngredients().first()
                val ingredients = availableIngredients.map { it.name }

                if (ingredients.isEmpty()) {
                    return@withContext Result.failure(Exception("No ingredients available. Please add some ingredients first."))
                }
                val maxMissing = when {
                    filters.showExactMatchOnly -> 0
                    filters.showNeedsTwoExtra -> 2
                    filters.showNeedsOneExtra -> 1
                    else -> 0
                }

                val result = geminiService.generateRecipes(ingredients, filters, maxMissing)

                if (result.isSuccess) {
                    val recipes = result.getOrNull() ?: emptyList()
                    val filteredRecipes = filterRecipes(recipes, filters)
                    Result.success(filteredRecipes)
                } else {
                    result
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun filterRecipes(recipes: List<Recipe>, filters: RecipeFilters): List<Recipe> {
        return recipes.filter { recipe ->
            val matchTypeOk = when (recipe.matchType) {
                RecipeMatchType.EXACT_MATCH -> true
                RecipeMatchType.NEEDS_ONE_EXTRA -> filters.showNeedsOneExtra
                RecipeMatchType.NEEDS_TWO_EXTRA -> filters.showNeedsTwoExtra
            }

            val timeOk = filters.maxCookingTime?.let { recipe.cookingTimeMinutes <= it } ?: true

            matchTypeOk && timeOk
        }
    }
}
