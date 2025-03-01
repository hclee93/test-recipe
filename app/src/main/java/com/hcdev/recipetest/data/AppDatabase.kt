package com.hcdev.recipetest.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hcdev.recipe.data.Recipe
import com.hcdev.recipe.data.RecipeDao

@Database(entities = [Recipe::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
}