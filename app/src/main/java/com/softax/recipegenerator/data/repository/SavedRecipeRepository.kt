package com.softax.recipegenerator.data.repository

import com.softax.recipegenerator.data.database.SavedRecipeDao
import com.softax.recipegenerator.data.database.SavedRecipeEntity
import com.softax.recipegenerator.data.database.toEntity
import com.softax.recipegenerator.data.database.toRecipe
import com.softax.recipegenerator.data.model.CuisineType
import com.softax.recipegenerator.data.model.DifficultyLevel
import com.softax.recipegenerator.data.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SavedRecipeRepository(private val savedRecipeDao: SavedRecipeDao) {

    fun getAllSavedRecipes(): Flow<List<Pair<Int, Recipe>>> =
        savedRecipeDao.getAllSavedRecipes().map { entities ->
            entities.map { it.id to it.toRecipe() }
        }

    fun searchSavedRecipes(query: String): Flow<List<Pair<Int, Recipe>>> =
        savedRecipeDao.searchSavedRecipes(query).map { entities ->
            entities.map { it.id to it.toRecipe() }
        }

    // Filter by cuisine
    fun getSavedRecipesByCuisine(cuisine: CuisineType): Flow<List<Pair<Int, Recipe>>> =
        savedRecipeDao.getSavedRecipesByCuisine(cuisine.name).map { entities ->
            entities.map { it.id to it.toRecipe() }
        }

    // Filter by difficulty
    fun getSavedRecipesByDifficulty(difficulty: DifficultyLevel): Flow<List<Pair<Int, Recipe>>> =
        savedRecipeDao.getSavedRecipesByDifficulty(difficulty.name).map { entities ->
            entities.map { it.id to it.toRecipe() }
        }

    // Add in feature
    fun getSavedRecipesSortedByTime(): Flow<List<Pair<Int, Recipe>>> =
        savedRecipeDao.getSavedRecipesSortedByTime().map { entities ->
            entities.map { it.id to it.toRecipe() }
        }

    fun getSavedRecipesSortedByName(): Flow<List<Pair<Int, Recipe>>> =
        savedRecipeDao.getSavedRecipesSortedByName().map { entities ->
            entities.map { it.id to it.toRecipe() }
        }

    // CRUD operations
    suspend fun saveRecipe(recipe: Recipe, notes: String = ""): Result<Long> =
        withContext(Dispatchers.IO) {
            try {
                val entity = recipe.toEntity(notes = notes, savedAt = System.currentTimeMillis())
                val id = savedRecipeDao.insertSavedRecipe(entity)
                Result.success(id)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun updateRecipeNotes(recipeId: Int, notes: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val entity = savedRecipeDao.getSavedRecipeById(recipeId)
                entity?.let {
                    savedRecipeDao.updateSavedRecipe(it.copy(notes = notes))
                    Result.success(Unit)
                } ?: Result.failure(Exception("Recipe not found"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun deleteSavedRecipe(recipeId: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                savedRecipeDao.deleteSavedRecipeById(recipeId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun getSavedRecipesCount(): Flow<Int> = savedRecipeDao.getSavedRecipesCount()

    fun isRecipeSaved(recipeName: String): Flow<Boolean> = savedRecipeDao.isRecipeSaved(recipeName)

    suspend fun getRecipeNotes(recipeId: Int): String? =
        withContext(Dispatchers.IO) {
            savedRecipeDao.getSavedRecipeById(recipeId)?.notes
        }
}
