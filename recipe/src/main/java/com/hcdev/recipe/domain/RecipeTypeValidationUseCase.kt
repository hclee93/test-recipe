package com.hcdev.recipe.domain

import com.hcdev.recipe.data.EmptyRecipeTypeException
import com.hcdev.recipe.data.RecipeType
import org.koin.core.annotation.Singleton

@Singleton
class RecipeTypeValidationUseCase {

    operator fun invoke(recipeType: Long, recipeTypes: List<RecipeType>): Throwable? {
        if (recipeType == 0L || !recipeTypes.any { it.id == recipeType }) {
            return EmptyRecipeTypeException
        }
        return null
    }
}