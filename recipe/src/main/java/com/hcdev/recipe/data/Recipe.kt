package com.hcdev.recipe.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "recipes")
@TypeConverters(RecipeTypeConverter::class)
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val image: String,
    val name: String,
    val type: Long,
    val typeDisplayName: String,
    val steps: List<String>,
    val ingredients: List<String>,
) {
    companion object {
        val Empty = Recipe(0, "", "", 0, "", emptyList(), emptyList())
    }
}

