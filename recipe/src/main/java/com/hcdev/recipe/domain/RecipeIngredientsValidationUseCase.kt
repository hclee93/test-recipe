package com.hcdev.recipe.domain

import com.hcdev.recipe.data.EmptyIngredientException
import org.koin.core.annotation.Singleton

@Singleton
class RecipeIngredientsValidationUseCase {

    operator fun invoke(ingredients: List<String>): Throwable? {
        if (ingredients.isEmpty()) {
            return EmptyIngredientException
        }
        return null
    }
}