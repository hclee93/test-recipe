package com.hcdev.recipe.domain

import com.hcdev.recipe.data.EmptyRecipeNameException
import org.koin.core.annotation.Singleton

@Singleton
class RecipeNameValidationUseCase {

    operator fun invoke(recipeName: String): Throwable? {
        if (recipeName.isBlank()) {
            return EmptyRecipeNameException
        }
        return null
    }
}