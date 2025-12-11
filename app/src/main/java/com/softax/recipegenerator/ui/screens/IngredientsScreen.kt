package com.softax.recipegenerator.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.softax.recipegenerator.data.model.Ingredient
import com.softax.recipegenerator.ui.viewmodel.IngredientViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsScreen(
    viewModel: IngredientViewModel = viewModel(),
    onNavigateToRecipes: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Ingredients") },
                actions = {
                    Text(
                        text = "${uiState.availableCount} available",
                        modifier = Modifier.padding(end = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Ingredient")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.ingredients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ingredients yet.\nTap + to add some!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.ingredients, key = { it.id }) { ingredient ->
                        IngredientItem(
                            ingredient = ingredient,
                            onToggle = { viewModel.toggleIngredientAvailability(ingredient) },
                            onDelete = { viewModel.deleteIngredient(ingredient) }
                        )
                    }
                }

                Button(
                    onClick = onNavigateToRecipes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = uiState.availableCount > 0
                ) {
                    Text("Generate Recipes (${uiState.availableCount} ingredients)")
                }
            }
        }

        if (showAddDialog) {
            AddIngredientDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, quantity, unit, category ->
                    viewModel.addIngredient(name, quantity, unit, category)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun IngredientItem(
    ingredient: Ingredient,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(
            containerColor = if (ingredient.isAvailable) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (ingredient.isAvailable) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.ShoppingCart
                    },
                    contentDescription = null,
                    tint = if (ingredient.isAvailable) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Column {
                    Text(
                        text = ingredient.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (ingredient.quantity.isNotEmpty() || ingredient.unit.isNotEmpty()) {
                        Text(
                            text = "${ingredient.quantity} ${ingredient.unit}".trim(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row {
                Checkbox(
                    checked = ingredient.isAvailable,
                    onCheckedChange = { onToggle() }
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Ingredient") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(name, quantity, unit, category) },
                enabled = name.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
