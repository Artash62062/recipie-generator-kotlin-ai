package com.softax.recipegenerator.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.softax.recipegenerator.data.model.*
import com.softax.recipegenerator.ui.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    viewModel: RecipeViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onRecipeClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilters by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (uiState.recipes.isEmpty() && !uiState.isLoading) {
            viewModel.generateRecipes()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipe Suggestions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Default.Settings, contentDescription = "Filters")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showFilters) {
                FiltersSection(
                    filters = uiState.filters,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider()
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Generating delicious recipes...")
                        }
                    }
                }

                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "An error occurred",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.generateRecipes() }) {
                                Text("Try Again")
                            }
                        }
                    }
                }

                uiState.recipes.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("No recipes found with current filters")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.generateRecipes() }) {
                                Text("Generate Recipes")
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(uiState.recipes) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                onClick = {
                                    viewModel.selectRecipe(recipe)
                                    onRecipeClick()
                                },
                                viewModel = viewModel
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.generateRecipes() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Regenerate Recipes")
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    viewModel: RecipeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSaved = uiState.savedRecipeNames.contains(recipe.name)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { viewModel.saveRecipe(recipe) }
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isSaved) "Saved" else "Save recipe",
                        tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AssistChip(
                    onClick = {},
                    label = { Text(recipe.matchType.displayName) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (recipe.matchType) {
                            RecipeMatchType.EXACT_MATCH -> MaterialTheme.colorScheme.primaryContainer
                            RecipeMatchType.NEEDS_ONE_EXTRA -> MaterialTheme.colorScheme.secondaryContainer
                            RecipeMatchType.NEEDS_TWO_EXTRA -> MaterialTheme.colorScheme.tertiaryContainer
                        }
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏱",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${recipe.cookingTimeMinutes} min",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = recipe.difficulty.displayName,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🍽",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${recipe.servings} servings",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (recipe.missingIngredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Missing: ${recipe.missingIngredients.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersSection(
    filters: RecipeFilters,
    viewModel: RecipeViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = "Filters",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Match Type", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = filters.showExactMatchOnly,
                onClick = { viewModel.toggleExactMatchOnly() },
                label = { Text("Exact match only") }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = filters.showNeedsOneExtra,
                onClick = { viewModel.toggleShowNeedsOneExtra() },
                label = { Text("1 extra item") },
                enabled = !filters.showExactMatchOnly
            )
            FilterChip(
                selected = filters.showNeedsTwoExtra,
                onClick = { viewModel.toggleShowNeedsTwoExtra() },
                label = { Text("2 extra items") },
                enabled = !filters.showExactMatchOnly
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Cuisine Type", style = MaterialTheme.typography.labelMedium)
        var expandedCuisine by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedCuisine,
            onExpandedChange = { expandedCuisine = it }
        ) {
            OutlinedTextField(
                value = filters.cuisineType.displayName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCuisine) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedCuisine,
                onDismissRequest = { expandedCuisine = false }
            ) {
                CuisineType.entries.forEach { cuisine ->
                    DropdownMenuItem(
                        text = { Text(cuisine.displayName) },
                        onClick = {
                            viewModel.setCuisineType(cuisine)
                            expandedCuisine = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Difficulty", style = MaterialTheme.typography.labelMedium)
        var expandedDifficulty by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedDifficulty,
            onExpandedChange = { expandedDifficulty = it }
        ) {
            OutlinedTextField(
                value = filters.difficulty.displayName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDifficulty) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedDifficulty,
                onDismissRequest = { expandedDifficulty = false }
            ) {
                DifficultyLevel.entries.forEach { difficulty ->
                    DropdownMenuItem(
                        text = { Text(difficulty.displayName) },
                        onClick = {
                            viewModel.setDifficulty(difficulty)
                            expandedDifficulty = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Dietary Restrictions", style = MaterialTheme.typography.labelMedium)
        DietaryRestriction.entries.filter { it != DietaryRestriction.NONE }.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { restriction ->
                    FilterChip(
                        selected = filters.dietaryRestrictions.contains(restriction),
                        onClick = { viewModel.toggleDietaryRestriction(restriction) },
                        label = { Text(restriction.displayName) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
