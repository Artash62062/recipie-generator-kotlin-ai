package com.softax.recipegenerator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun IngredientConfirmationDialog(
    detectedIngredients: List<String>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var editableIngredients by remember {
        mutableStateOf(detectedIngredients.toMutableList())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detected Ingredients") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Review and edit the ingredients we found:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    itemsIndexed(editableIngredients) { index, ingredient ->
                        IngredientEditItem(
                            ingredient = ingredient,
                            onIngredientChange = { newValue ->
                                editableIngredients[index] = newValue
                            },
                            onDelete = {
                                editableIngredients = editableIngredients.toMutableList().apply {
                                    removeAt(index)
                                }
                            }
                        )
                    }
                }

                // Add new ingredient button
                TextButton(
                    onClick = {
                        editableIngredients = editableIngredients.toMutableList().apply {
                            add("")
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add ingredient")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validIngredients = editableIngredients
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                    onConfirm(validIngredients)
                }
            ) {
                val validCount = editableIngredients.count { it.trim().isNotBlank() }
                Text("Add $validCount ingredient${if (validCount != 1) "s" else ""}")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun IngredientEditItem(
    ingredient: String,
    onIngredientChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = ingredient,
            onValueChange = onIngredientChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            placeholder = { Text("Ingredient name") }
        )
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
