package com.softax.recipegenerator.data.database

import androidx.room.*
import com.softax.recipegenerator.data.model.Ingredient
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE isAvailable = 1 ORDER BY name ASC")
    fun getAvailableIngredients(): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getIngredientById(id: Int): Ingredient?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<Ingredient>)

    @Update
    suspend fun updateIngredient(ingredient: Ingredient)

    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)

    @Query("DELETE FROM ingredients")
    suspend fun deleteAllIngredients()

    @Query("SELECT COUNT(*) FROM ingredients WHERE isAvailable = 1")
    fun getAvailableIngredientsCount(): Flow<Int>
}
