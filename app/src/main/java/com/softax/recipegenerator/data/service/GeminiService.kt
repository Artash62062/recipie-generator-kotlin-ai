package com.softax.recipegenerator.data.service

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.softax.recipegenerator.BuildConfig
import com.softax.recipegenerator.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class GeminiService {
    // Try gemini-1.5-flash-latest first, fallback to gemini-pro if needed
    private val modelName = "gemini-robotics-er-1.5-preview" // Alternative: "gemini-pro", "gemini-1.5-pro-latest"

    private val generativeModel = GenerativeModel(
        modelName = modelName,
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 8192  // Increased from 2048 to allow longer responses
        }
    )

    suspend fun generateRecipes(
        availableIngredients: List<String>,
        filters: RecipeFilters,
        maxMissingIngredients: Int = 2
    ): Result<List<Recipe>> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildPrompt(availableIngredients, filters, maxMissingIngredients)
            val response = generativeModel.generateContent(prompt)
            val recipes = parseRecipesFromResponse(response.text ?: "")

            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildPrompt(
        ingredients: List<String>,
        filters: RecipeFilters,
        maxMissingIngredients: Int
    ): String {
        val ingredientList = ingredients.joinToString(", ")

        val dietaryText = if (filters.dietaryRestrictions.isNotEmpty()) {
            "Dietary restrictions: ${filters.dietaryRestrictions.joinToString(", ") { it.displayName }}"
        } else ""

        val cuisineText = if (filters.cuisineType != CuisineType.ANY) {
            "Cuisine type: ${filters.cuisineType.displayName}"
        } else ""

        val timeText = filters.maxCookingTime?.let { "Maximum cooking time: $it minutes" } ?: ""

        val difficultyText = if (filters.difficulty != DifficultyLevel.ANY) {
            "Difficulty level: ${filters.difficulty.displayName}"
        } else ""

        return """
            You are a professional chef AI. Generate 3-5 diverse recipe suggestions based on the following:

            Available ingredients: $ingredientList
            $dietaryText
            $cuisineText
            $timeText
            $difficultyText

            IMPORTANT RECIPE GENERATION ORDER:
            1. FIRST: Generate at least 1 recipe that uses ONLY the available ingredients (no additional ingredients needed) if possible
            2. THEN: Generate additional recipes that may need up to $maxMissingIngredients extra ingredients
            3. This ensures users always see what they can make right now before seeing recipes requiring shopping

            For each recipe:
            - You can use some or all of the available ingredients
            - Clearly indicate which additional ingredients are needed in the "missingIngredients" field
            - If a recipe uses only available ingredients, leave "missingIngredients" as an empty array []

            Return ONLY a valid JSON array with this exact structure (no markdown, no code blocks, just pure JSON):
            [
              {
                "name": "Recipe Name",
                "description": "Brief description",
                "ingredients": ["ingredient 1 with quantity", "ingredient 2 with quantity"],
                "instructions": ["step 1", "step 2", "step 3"],
                "cookingTimeMinutes": 30,
                "servings": 4,
                "difficulty": "EASY|MEDIUM|HARD",
                "cuisineType": "ITALIAN|CHINESE|MEXICAN|etc",
                "dietaryRestrictions": ["VEGETARIAN", "GLUTEN_FREE"],
                "missingIngredients": []
              }
            ]

            Important:
            - Return ONLY valid JSON, no additional text
            - PRIORITIZE generating at least 1 recipe with missingIngredients: [] (can make now)
            - Then include recipes with 1 or 2 missing ingredients for variety
            - Make ingredients list specific with quantities (e.g., "2 cups rice", "3 tomatoes")
            - Keep instructions CONCISE - 4-6 steps maximum per recipe
            - Keep descriptions brief - 1-2 sentences maximum
            - Difficulty must be exactly: EASY, MEDIUM, or HARD
            - Ensure all recipes match the dietary restrictions if specified
        """.trimIndent()
    }

    private fun parseRecipesFromResponse(responseText: String): List<Recipe> {
        try {
            // Clean response - remove markdown code blocks if present
            val cleanedText = responseText
                .trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val jsonArray = JSONArray(cleanedText)
            val recipes = mutableListOf<Recipe>()

            for (i in 0 until jsonArray.length()) {
                val jsonRecipe = jsonArray.getJSONObject(i)
                val recipe = parseRecipe(jsonRecipe)
                recipe?.let { recipes.add(it) }
            }

            // Sort recipes: exact matches first, then by number of missing ingredients
            return recipes.sortedBy { recipe ->
                when (recipe.matchType) {
                    RecipeMatchType.EXACT_MATCH -> 0
                    RecipeMatchType.NEEDS_ONE_EXTRA -> 1
                    RecipeMatchType.NEEDS_TWO_EXTRA -> 2
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to parse recipes: ${e.message}")
        }
    }

    private fun parseRecipe(json: JSONObject): Recipe? {
        return try {
            val ingredients = mutableListOf<String>()
            val ingredientsArray = json.getJSONArray("ingredients")
            for (i in 0 until ingredientsArray.length()) {
                ingredients.add(ingredientsArray.getString(i))
            }

            val instructions = mutableListOf<String>()
            val instructionsArray = json.getJSONArray("instructions")
            for (i in 0 until instructionsArray.length()) {
                instructions.add(instructionsArray.getString(i))
            }

            val missingIngredients = mutableListOf<String>()
            if (json.has("missingIngredients")) {
                val missingArray = json.getJSONArray("missingIngredients")
                for (i in 0 until missingArray.length()) {
                    missingIngredients.add(missingArray.getString(i))
                }
            }

            val dietaryRestrictions = mutableListOf<DietaryRestriction>()
            if (json.has("dietaryRestrictions")) {
                val dietaryArray = json.getJSONArray("dietaryRestrictions")
                for (i in 0 until dietaryArray.length()) {
                    try {
                        dietaryRestrictions.add(
                            DietaryRestriction.valueOf(dietaryArray.getString(i))
                        )
                    } catch (e: Exception) {
                        // Skip invalid dietary restrictions
                    }
                }
            }

            val matchType = when (missingIngredients.size) {
                0 -> RecipeMatchType.EXACT_MATCH
                1 -> RecipeMatchType.NEEDS_ONE_EXTRA
                else -> RecipeMatchType.NEEDS_TWO_EXTRA
            }

            Recipe(
                name = json.getString("name"),
                description = json.getString("description"),
                ingredients = ingredients,
                instructions = instructions,
                cookingTimeMinutes = json.getInt("cookingTimeMinutes"),
                servings = json.getInt("servings"),
                difficulty = try {
                    DifficultyLevel.valueOf(json.getString("difficulty"))
                } catch (e: Exception) {
                    DifficultyLevel.MEDIUM
                },
                cuisineType = try {
                    CuisineType.valueOf(json.getString("cuisineType"))
                } catch (e: Exception) {
                    CuisineType.ANY
                },
                dietaryRestrictions = dietaryRestrictions,
                missingIngredients = missingIngredients,
                matchType = matchType
            )
        } catch (e: Exception) {
            null
        }
    }
}
