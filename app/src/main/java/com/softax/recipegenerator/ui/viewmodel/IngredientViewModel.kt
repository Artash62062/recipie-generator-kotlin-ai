package com.softax.recipegenerator.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.softax.recipegenerator.data.database.AppDatabase
import com.softax.recipegenerator.data.model.Ingredient
import com.softax.recipegenerator.data.repository.IngredientRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class IngredientUiState(
    val ingredients: List<Ingredient> = emptyList(),
    val availableCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class IngredientViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = IngredientRepository(database.ingredientDao())

    private val _uiState = MutableStateFlow(IngredientUiState())
    val uiState: StateFlow<IngredientUiState> = _uiState.asStateFlow()

    init {
        loadIngredients()
    }

    private fun loadIngredients() {
        viewModelScope.launch {
            combine(
                repository.getAllIngredients(),
                repository.getAvailableIngredientsCount()
            ) { ingredients, count ->
                IngredientUiState(
                    ingredients = ingredients,
                    availableCount = count,
                    isLoading = false
                )
            }.catch { e ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun addIngredient(name: String, quantity: String = "", unit: String = "", category: String = "") {
        if (name.isBlank()) return

        viewModelScope.launch {
            try {
                val ingredient = Ingredient(
                    name = name.trim(),
                    quantity = quantity.trim(),
                    unit = unit.trim(),
                    category = category.trim(),
                    isAvailable = true
                )
                repository.addIngredient(ingredient)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun updateIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            try {
                repository.updateIngredient(ingredient)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun toggleIngredientAvailability(ingredient: Ingredient) {
        viewModelScope.launch {
            try {
                repository.toggleIngredientAvailability(ingredient)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun deleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            try {
                repository.deleteIngredient(ingredient)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
