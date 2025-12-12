package com.softax.recipegenerator.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.softax.recipegenerator.data.database.AppDatabase
import com.softax.recipegenerator.data.model.CuisineType
import com.softax.recipegenerator.data.model.DifficultyLevel
import com.softax.recipegenerator.data.model.Recipe
import com.softax.recipegenerator.data.repository.SavedRecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SavedRecipeUiState(
    val savedRecipes: List<Pair<Int, Recipe>> = emptyList(),  // Pair of (recipeId, recipe)
    val searchQuery: String = "",
    val filterCuisine: CuisineType? = null,
    val filterDifficulty: DifficultyLevel? = null,
    val sortOption: SortOption = SortOption.DATE_SAVED,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedRecipeIdForNotes: Int? = null,
    val selectedRecipeNotes: String = ""
)

enum class SortOption(val displayName: String) {
    DATE_SAVED("Date Saved"),
    NAME("Name"),
    COOKING_TIME("Cooking Time")
}

class SavedRecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val savedRecipeRepository: SavedRecipeRepository

    init {
        val database = AppDatabase.getDatabase(application)
        savedRecipeRepository = SavedRecipeRepository(database.savedRecipeDao())
    }

    private val _uiState = MutableStateFlow(SavedRecipeUiState())
    val uiState: StateFlow<SavedRecipeUiState> = _uiState.asStateFlow()

    init {
        loadSavedRecipes()
    }

    private fun loadSavedRecipes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                savedRecipeRepository.getAllSavedRecipes()
                    .collect { recipes ->
                        val filtered = applyFilters(recipes)
                        val sorted = applySorting(filtered)
                        _uiState.update {
                            it.copy(
                                savedRecipes = sorted,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()  // Print stack trace for debugging
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load saved recipes: ${e.message}\n\nPlease clear app data in Settings > Apps > Recipe Generator > Storage > Clear Data"
                    )
                }
            }
        }
    }

    fun saveRecipe(recipe: Recipe, notes: String = "") {
        viewModelScope.launch {
            savedRecipeRepository.saveRecipe(recipe, notes).fold(
                onSuccess = {
                    // Recipe saved successfully - state will update via Flow
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(errorMessage = "Failed to save recipe: ${error.message}")
                    }
                }
            )
        }
    }

    fun deleteSavedRecipe(recipeId: Int) {
        viewModelScope.launch {
            savedRecipeRepository.deleteSavedRecipe(recipeId).fold(
                onSuccess = {
                    // Recipe deleted successfully - state will update via Flow
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(errorMessage = "Failed to delete recipe: ${error.message}")
                    }
                }
            )
        }
    }

    fun showNotesDialog(recipeId: Int) {
        viewModelScope.launch {
            val notes = savedRecipeRepository.getRecipeNotes(recipeId) ?: ""
            _uiState.update {
                it.copy(
                    selectedRecipeIdForNotes = recipeId,
                    selectedRecipeNotes = notes
                )
            }
        }
    }

    fun hideNotesDialog() {
        _uiState.update {
            it.copy(
                selectedRecipeIdForNotes = null,
                selectedRecipeNotes = ""
            )
        }
    }

    fun updateNotesText(notes: String) {
        _uiState.update { it.copy(selectedRecipeNotes = notes) }
    }

    fun saveNotes() {
        val recipeId = _uiState.value.selectedRecipeIdForNotes ?: return
        val notes = _uiState.value.selectedRecipeNotes

        viewModelScope.launch {
            savedRecipeRepository.updateRecipeNotes(recipeId, notes).fold(
                onSuccess = {
                    hideNotesDialog()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(errorMessage = "Failed to save notes: ${error.message}")
                    }
                }
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        // Filter is applied in the collect block via applyFilters
    }

    fun setFilterCuisine(cuisine: CuisineType?) {
        _uiState.update { it.copy(filterCuisine = cuisine) }
    }

    fun setFilterDifficulty(difficulty: DifficultyLevel?) {
        _uiState.update { it.copy(filterDifficulty = difficulty) }
    }

    fun setSortOption(option: SortOption) {
        _uiState.update { it.copy(sortOption = option) }
    }

    private fun applyFilters(recipes: List<Pair<Int, Recipe>>): List<Pair<Int, Recipe>> {
        var filtered = recipes

        // Search filter
        val query = _uiState.value.searchQuery
        if (query.isNotEmpty()) {
            filtered = filtered.filter { (_, recipe) ->
                recipe.name.contains(query, ignoreCase = true)
            }
        }

        // Cuisine filter
        _uiState.value.filterCuisine?.let { cuisine ->
            filtered = filtered.filter { (_, recipe) ->
                recipe.cuisineType == cuisine
            }
        }

        // Difficulty filter
        _uiState.value.filterDifficulty?.let { difficulty ->
            filtered = filtered.filter { (_, recipe) ->
                recipe.difficulty == difficulty
            }
        }

        return filtered
    }

    private fun applySorting(recipes: List<Pair<Int, Recipe>>): List<Pair<Int, Recipe>> {
        return when (_uiState.value.sortOption) {
            SortOption.DATE_SAVED -> recipes  // Already sorted from DB
            SortOption.NAME -> recipes.sortedBy { (_, recipe) -> recipe.name }
            SortOption.COOKING_TIME -> recipes.sortedBy { (_, recipe) -> recipe.cookingTimeMinutes }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
