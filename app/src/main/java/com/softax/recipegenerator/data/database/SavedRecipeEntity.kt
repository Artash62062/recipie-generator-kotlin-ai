package com.softax.recipegenerator.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.softax.recipegenerator.data.model.*

@Entity(tableName = "saved_recipes")
data class SavedRecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,
    val description: String,
    val ingredientsJson: String,
    val instructionsJson: String,
    val cookingTimeMinutes: Int,
    val servings: Int,
    val difficulty: String,
    val cuisineType: String,
    val dietaryRestrictionsJson: String,
    val missingIngredientsJson: String,


    val savedAt: Long,
    val notes: String = "",
    val isFavorite: Boolean = false
)

private val gson = Gson()

fun Recipe.toEntity(notes: String = "", savedAt: Long = System.currentTimeMillis()): SavedRecipeEntity {
    return SavedRecipeEntity(
        name = name,
        description = description,
        ingredientsJson = gson.toJson(ingredients),
        instructionsJson = gson.toJson(instructions),
        cookingTimeMinutes = cookingTimeMinutes,
        servings = servings,
        difficulty = difficulty.name,
        cuisineType = cuisineType.name,
        dietaryRestrictionsJson = gson.toJson(dietaryRestrictions.map { it.name }),
        missingIngredientsJson = gson.toJson(missingIngredients),
        savedAt = savedAt,
        notes = notes,
        isFavorite = false
    )
}

fun SavedRecipeEntity.toRecipe(): Recipe {
    val stringListType = object : TypeToken<List<String>>() {}.type
    val ingredients: List<String> = gson.fromJson(ingredientsJson, stringListType)
    val instructions: List<String> = gson.fromJson(instructionsJson, stringListType)
    val missingIngredients: List<String> = gson.fromJson(missingIngredientsJson, stringListType)

    val dietaryRestrictionNames: List<String> = gson.fromJson(dietaryRestrictionsJson, stringListType)
    val dietaryRestrictions = dietaryRestrictionNames.mapNotNull {
        try {
            DietaryRestriction.valueOf(it)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    val matchType = when (missingIngredients.size) {
        0 -> RecipeMatchType.EXACT_MATCH
        1 -> RecipeMatchType.NEEDS_ONE_EXTRA
        else -> RecipeMatchType.NEEDS_TWO_EXTRA
    }

    return Recipe(
        name = name,
        description = description,
        ingredients = ingredients,
        instructions = instructions,
        cookingTimeMinutes = cookingTimeMinutes,
        servings = servings,
        difficulty = DifficultyLevel.valueOf(difficulty),
        cuisineType = CuisineType.valueOf(cuisineType),
        dietaryRestrictions = dietaryRestrictions,
        missingIngredients = missingIngredients,
        matchType = matchType
    )
}
