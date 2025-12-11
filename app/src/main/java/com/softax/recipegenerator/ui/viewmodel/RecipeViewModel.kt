package com.softax.recipegenerator.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.softax.recipegenerator.data.database.AppDatabase
import com.softax.recipegenerator.data.model.*
import com.softax.recipegenerator.data.repository.IngredientRepository
import com.softax.recipegenerator.data.repository.RecipeRepository
import com.softax.recipegenerator.data.service.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecipeUiState(
    val recipes: List<Recipe> = emptyList(),
    val filters: RecipeFilters = RecipeFilters(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedRecipe: Recipe? = null
)

class RecipeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val ingredientRepository = IngredientRepository(database.ingredientDao())
    private val geminiService = GeminiService()
    private val recipeRepository = RecipeRepository(geminiService, ingredientRepository)

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    fun generateRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = recipeRepository.generateRecipes(_uiState.value.filters)

            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    recipes = result.getOrNull() ?: emptyList(),
                    isLoading = false
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to generate recipes"
                )
            }
        }
    }

    fun updateFilters(newFilters: RecipeFilters) {
        _uiState.value = _uiState.value.copy(filters = newFilters)
    }

    fun selectRecipe(recipe: Recipe) {
        _uiState.value = _uiState.value.copy(selectedRecipe = recipe)
    }

    fun clearSelectedRecipe() {
        _uiState.value = _uiState.value.copy(selectedRecipe = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun toggleDietaryRestriction(restriction: DietaryRestriction) {
        val currentRestrictions = _uiState.value.filters.dietaryRestrictions.toMutableList()
        if (currentRestrictions.contains(restriction)) {
            currentRestrictions.remove(restriction)
        } else {
            currentRestrictions.add(restriction)
        }
        updateFilters(_uiState.value.filters.copy(dietaryRestrictions = currentRestrictions))
    }

    fun setCuisineType(cuisineType: CuisineType) {
        updateFilters(_uiState.value.filters.copy(cuisineType = cuisineType))
    }

    fun setDifficulty(difficulty: DifficultyLevel) {
        updateFilters(_uiState.value.filters.copy(difficulty = difficulty))
    }

    fun setMaxCookingTime(time: Int?) {
        updateFilters(_uiState.value.filters.copy(maxCookingTime = time))
    }

    fun toggleExactMatchOnly() {
        val current = _uiState.value.filters
        updateFilters(current.copy(
            showExactMatchOnly = !current.showExactMatchOnly,
            showNeedsOneExtra = current.showExactMatchOnly,
            showNeedsTwoExtra = current.showExactMatchOnly
        ))
    }

    fun toggleShowNeedsOneExtra() {
        val current = _uiState.value.filters
        updateFilters(current.copy(showNeedsOneExtra = !current.showNeedsOneExtra))
    }

    fun toggleShowNeedsTwoExtra() {
        val current = _uiState.value.filters
        updateFilters(current.copy(showNeedsTwoExtra = !current.showNeedsTwoExtra))
    }
}
