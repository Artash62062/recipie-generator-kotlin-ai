package com.softax.recipegenerator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.softax.recipegenerator.data.model.Recipe
import com.softax.recipegenerator.data.model.RecipeMatchType
import com.softax.recipegenerator.ui.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    viewModel: RecipeViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val recipe = uiState.selectedRecipe

    if (recipe == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("No recipe selected")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                RecipeHeader(recipe)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (recipe.missingIngredients.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "Missing Ingredients",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            recipe.missingIngredients.forEach { ingredient ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = ingredient,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item {
                Text(
                    text = "Ingredients",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            itemsIndexed(recipe.ingredients) { _, ingredient ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = ingredient,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Instructions",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            itemsIndexed(recipe.instructions) { index, instruction ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Text(
                            text = instruction,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun RecipeHeader(recipe: Recipe) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("⏱ ${recipe.cookingTimeMinutes} min") }
                )
                AssistChip(
                    onClick = {},
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(recipe.difficulty.displayName)
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("🍽 ${recipe.servings} servings") }
                )
                AssistChip(
                    onClick = {},
                    label = { Text("🌍 ${recipe.cuisineType.displayName}") }
                )
            }
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

    if (recipe.dietaryRestrictions.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Dietary: ${recipe.dietaryRestrictions.joinToString(", ") { it.displayName }}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
