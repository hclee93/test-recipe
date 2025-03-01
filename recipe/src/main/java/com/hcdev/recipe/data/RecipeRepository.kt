package com.hcdev.recipe.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Singleton

@Singleton
class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val recipeTypeLocalSource: RecipeTypeLocalSource,
) {
    private val _recipeTypes = MutableStateFlow<List<RecipeType>>(emptyList())
    val recipeTypes = _recipeTypes.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    init {
        scope.launch {
            val types = recipeTypeLocalSource.getRecipeTypes()
            if (types.isNotEmpty()) {
                _recipeTypes.update { types }
            }
        }
    }

    suspend fun getRecipeById(id: Long): Recipe? {
        return recipeDao.getRecipeById(id)
    }

    suspend fun deleteRecipe(recipe: Recipe) = recipeDao.deleteRecipe(recipe)

    /**
     * currently support offline online so only called dao, will need to add call api
     */
    suspend fun upsertRecipe(recipe: Recipe): Long {
        return recipeDao.upsertRecipe(recipe)
    }

    fun getAllRecipesFlow(): Flow<List<Recipe>> = recipeDao.getAllRecipesFlow()
    fun getAllRecipesByRecipeTypeFlow(recipeTypes: List<Long>): Flow<List<Recipe>> =
        recipeDao.getAllRecipesByRecipeTypeFlow(recipeTypes)

    fun getRecipeByIdFlow(
        id: Long?,
    ) = recipeDao.getRecipeByIdFlow(id)
}