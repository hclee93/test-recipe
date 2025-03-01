package com.hcdev.recipetest.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hcdev.core.ErrorFormatter
import com.hcdev.recipe.data.EmptyRecipeTypeException
import com.hcdev.recipe.data.EmptyIngredientException
import com.hcdev.recipe.data.EmptyRecipeImageException
import com.hcdev.recipe.data.EmptyRecipeNameException
import com.hcdev.recipe.data.EmptyStepException
import com.hcdev.recipe.data.RecipeExceptions
import com.hcdev.recipetest.R

val AppErrorFormatter = object : ErrorFormatter {
    @Composable
    override fun formatError(throwable: Throwable): String {
        return when (throwable) {
            is RecipeExceptions -> {
                when (throwable) {
                    EmptyRecipeTypeException -> stringResource(R.string.category_field_required)
                    EmptyIngredientException -> stringResource(R.string.ingredient_field_required)
                    EmptyRecipeImageException -> stringResource(R.string.recipe_image_field_required)
                    EmptyRecipeNameException -> stringResource(R.string.recipe_name_field_required)
                    EmptyStepException -> stringResource(R.string.recipe_step_field_required)
                }
            }

            else -> stringResource(com.hcdev.core.R.string.default_error_msg)
        }
    }
}