package com.softax.recipegenerator.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.softax.recipegenerator.ui.screens.IngredientsScreen
import com.softax.recipegenerator.ui.screens.RecipeDetailScreen
import com.softax.recipegenerator.ui.screens.RecipesScreen
import com.softax.recipegenerator.ui.viewmodel.RecipeViewModel

sealed class Screen(val route: String) {
    object Ingredients : Screen("ingredients")
    object Recipes : Screen("recipes")
    object RecipeDetail : Screen("recipe_detail")
}

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

        composable(Screen.RecipeDetail.route) {
            val recipesBackStackEntry = remember {
                navController.getBackStackEntry(Screen.Recipes.route)
            }
            val sharedViewModel: RecipeViewModel = viewModel(
                viewModelStoreOwner = recipesBackStackEntry
            )
            RecipeDetailScreen(
                viewModel = sharedViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
