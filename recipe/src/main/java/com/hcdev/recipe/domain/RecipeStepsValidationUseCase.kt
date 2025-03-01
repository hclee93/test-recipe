package com.hcdev.recipe.domain

import com.hcdev.recipe.data.EmptyStepException
import org.koin.core.annotation.Singleton

@Singleton
class RecipeStepsValidationUseCase {
    operator fun invoke(steps: List<String>): Throwable? {
        if (steps.isEmpty()) {
            return EmptyStepException
        }
        return null
    }
}