package com.softax.recipegenerator.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.softax.recipegenerator.ui.screens.IngredientsScreen
import com.softax.recipegenerator.ui.screens.RecipeDetailScreen
import com.softax.recipegenerator.ui.screens.RecipesScreen
import com.softax.recipegenerator.ui.screens.SavedRecipesScreen
import com.softax.recipegenerator.ui.viewmodel.RecipeViewModel

sealed class Screen(val route: String) {
    object Ingredients : Screen("ingredients")
    object Recipes : Screen("recipes")
    object RecipeDetail : Screen("recipe_detail")
    object SavedRecipes : Screen("saved_recipes")
}

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Ingredients.route
    ) {
        composable(Screen.Ingredients.route) {
            IngredientsScreen(
                onNavigateToRecipes = {
                    navController.navigate(Screen.Recipes.route)
                }
            )
        }

        composable(Screen.Recipes.route) { backStackEntry ->
            val sharedViewModel: RecipeViewModel = viewModel(
                viewModelStoreOwner = backStackEntry
            )
            RecipesScreen(
                viewModel = sharedViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRecipeClick = {
                    navController.navigate(Screen.RecipeDetail.route)
                }
            )
        }

        composable(Screen.RecipeDetail.route) { backStackEntry ->
            // Try to get ViewModel from recipes route, or saved recipes route as fallback
            val viewModelBackStackEntry = remember(navController) {
                try {
                    navController.getBackStackEntry(Screen.Recipes.route)
                } catch (e: IllegalArgumentException) {
                    try {
                        navController.getBackStackEntry(Screen.SavedRecipes.route)
                    } catch (e: IllegalArgumentException) {
                        backStackEntry
                    }
                }
            }
            val sharedViewModel: RecipeViewModel = viewModel(
                viewModelStoreOwner = viewModelBackStackEntry
            )
            RecipeDetailScreen(
                viewModel = sharedViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.SavedRecipes.route) { backStackEntry ->
            // Try to get or create the recipes ViewModel
            val recipesBackStackEntry = remember(navController) {
                try {
                    navController.getBackStackEntry(Screen.Recipes.route)
                } catch (e: IllegalArgumentException) {
                    // If recipes route doesn't exist, use current entry as fallback
                    backStackEntry
                }
            }
            val sharedViewModel: RecipeViewModel = viewModel(
                viewModelStoreOwner = recipesBackStackEntry
            )

            SavedRecipesScreen(
                onRecipeClick = { recipe ->
                    // Select the recipe and navigate to detail
                    sharedViewModel.selectRecipe(recipe)
                    navController.navigate(Screen.RecipeDetail.route)
                }
            )
        }
    }
}
