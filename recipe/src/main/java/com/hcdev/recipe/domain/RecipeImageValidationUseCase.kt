package com.hcdev.recipe.domain

import com.hcdev.recipe.data.EmptyRecipeImageException
import org.koin.core.annotation.Singleton

@Singleton
class RecipeImageValidationUseCase {

    operator fun invoke(image: String): Throwable? {
        if (image.isBlank()) {
            return EmptyRecipeImageException
        }
        return null
    }
}