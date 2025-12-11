package com.softax.recipegenerator.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val quantity: String = "",
    val unit: String = "",
    val category: String = "",
    val isAvailable: Boolean = true
)
