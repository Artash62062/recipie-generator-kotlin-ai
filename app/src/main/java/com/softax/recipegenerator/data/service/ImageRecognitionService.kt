package com.softax.recipegenerator.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.softax.recipegenerator.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class ImageRecognitionService {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.4f
            topK = 32
            topP = 1f
            maxOutputTokens = 8000
        }
    )

    suspend fun recognizeIngredients(imageUri: Uri, context: Context): Result<List<String>> =
        withContext(Dispatchers.IO) {
            try {
                // Load bitmap from URI
                val bitmap = loadBitmapFromUri(context, imageUri)
                    ?: return@withContext Result.failure(
                        IOException("Failed to load image from URI")
                    )

                // Create prompt for ingredient detection
                val prompt = """
                    Analyze this image and identify all food ingredients visible.

                    Rules:
                    - List ONLY the ingredient names (e.g., "tomato", "onion", "chicken breast")
                    - One ingredient per line
                    - Use common/simple names
                    - Ignore non-food items
                    - If you see packaged items, identify the ingredient inside (e.g., "milk" not "milk carton")
                    - Be specific when needed (e.g., "chicken breast" not just "chicken")

                    Return ONLY a JSON array of ingredient names, like this example:
                    ["tomato", "onion", "garlic", "olive oil"]

                    Do not include any other text, just the JSON array.
                """.trimIndent()

                // Call Gemini Vision API
                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )

                // Parse JSON response
                val jsonText = response.text?.trim() ?: return@withContext Result.failure(
                    Exception("Empty response from Gemini")
                )

                val ingredients = parseIngredientsJson(jsonText)

                if (ingredients.isEmpty()) {
                    Result.failure(Exception("No ingredients detected in the image"))
                } else {
                    Result.success(ingredients)
                }

            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseIngredientsJson(jsonText: String): List<String> {
        return try {
            val gson = Gson()
            // Remove markdown code blocks if present
            val cleanJson = jsonText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val type = object : TypeToken<List<String>>() {}.type
            val ingredients: List<String> = gson.fromJson(cleanJson, type)

            // Filter out empty strings and clean up
            ingredients
                .map { it.trim() }
                .filter { it.isNotBlank() }

        } catch (e: Exception) {
            // If JSON parsing fails, try to extract ingredients from plain text
            parseIngredientsFromText(jsonText)
        }
    }

    private fun parseIngredientsFromText(text: String): List<String> {
        // Fallback: try to parse line by line
        return text
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("{") && !it.startsWith("[") }
            .filter { it.length > 2 && it.length < 50 }  // Reasonable ingredient name length
            .take(20)  // Limit to 20 ingredients max
    }
}
