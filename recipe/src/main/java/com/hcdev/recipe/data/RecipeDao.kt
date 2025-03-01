package com.hcdev.recipe.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes")
    fun getAllRecipesFlow(): Flow<List<Recipe>>

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Query("SELECT * FROM recipes WHERE type IN (:recipeTypes)")
    fun getAllRecipesByRecipeTypeFlow(recipeTypes: List<Long>): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Long?): Recipe?

    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getRecipeByIdFlow(id: Long?): Flow<Recipe?>

    @Upsert
    suspend fun upsertRecipe(recipe: Recipe): Long
}