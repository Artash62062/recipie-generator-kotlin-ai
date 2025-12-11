package com.softax.recipegenerator.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedRecipeDao {
    @Query("SELECT * FROM saved_recipes ORDER BY savedAt DESC")
    fun getAllSavedRecipes(): Flow<List<SavedRecipeEntity>>

    @Query("SELECT * FROM saved_recipes WHERE id = :id")
    suspend fun getSavedRecipeById(id: Int): SavedRecipeEntity?

    @Query("SELECT * FROM saved_recipes WHERE name LIKE '%' || :query || '%' ORDER BY savedAt DESC")
    fun searchSavedRecipes(query: String): Flow<List<SavedRecipeEntity>>

    @Query("SELECT * FROM saved_recipes WHERE cuisineType = :cuisine ORDER BY savedAt DESC")
    fun getSavedRecipesByCuisine(cuisine: String): Flow<List<SavedRecipeEntity>>

    @Query("SELECT * FROM saved_recipes WHERE difficulty = :difficulty ORDER BY savedAt DESC")
    fun getSavedRecipesByDifficulty(difficulty: String): Flow<List<SavedRecipeEntity>>

    @Query("SELECT * FROM saved_recipes ORDER BY cookingTimeMinutes ASC")
    fun getSavedRecipesSortedByTime(): Flow<List<SavedRecipeEntity>>

    @Query("SELECT * FROM saved_recipes ORDER BY name ASC")
    fun getSavedRecipesSortedByName(): Flow<List<SavedRecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedRecipe(recipe: SavedRecipeEntity): Long

    @Update
    suspend fun updateSavedRecipe(recipe: SavedRecipeEntity)

    @Delete
    suspend fun deleteSavedRecipe(recipe: SavedRecipeEntity)

    @Query("DELETE FROM saved_recipes WHERE id = :id")
    suspend fun deleteSavedRecipeById(id: Int)

    @Query("SELECT COUNT(*) FROM saved_recipes")
    fun getSavedRecipesCount(): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_recipes WHERE name = :recipeName LIMIT 1)")
    fun isRecipeSaved(recipeName: String): Flow<Boolean>
}
