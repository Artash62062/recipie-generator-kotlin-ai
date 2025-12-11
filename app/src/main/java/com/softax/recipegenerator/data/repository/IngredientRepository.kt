package com.softax.recipegenerator.data.repository

import com.softax.recipegenerator.data.database.IngredientDao
import com.softax.recipegenerator.data.model.Ingredient
import kotlinx.coroutines.flow.Flow

class IngredientRepository(private val ingredientDao: IngredientDao) {

    fun getAllIngredients(): Flow<List<Ingredient>> = ingredientDao.getAllIngredients()

    fun getAvailableIngredients(): Flow<List<Ingredient>> = ingredientDao.getAvailableIngredients()

    fun getAvailableIngredientsCount(): Flow<Int> = ingredientDao.getAvailableIngredientsCount()

    suspend fun getIngredientById(id: Int): Ingredient? = ingredientDao.getIngredientById(id)

    suspend fun addIngredient(ingredient: Ingredient) {
        ingredientDao.insertIngredient(ingredient)
    }

    suspend fun addIngredients(ingredients: List<Ingredient>) {
        ingredientDao.insertIngredients(ingredients)
    }

    suspend fun updateIngredient(ingredient: Ingredient) {
        ingredientDao.updateIngredient(ingredient)
    }

    suspend fun deleteIngredient(ingredient: Ingredient) {
        ingredientDao.deleteIngredient(ingredient)
    }

    suspend fun deleteAllIngredients() {
        ingredientDao.deleteAllIngredients()
    }

    suspend fun toggleIngredientAvailability(ingredient: Ingredient) {
        ingredientDao.updateIngredient(ingredient.copy(isAvailable = !ingredient.isAvailable))
    }
}
