package com.hcdev.recipe.data


sealed class RecipeExceptions : Exception()
data object EmptyRecipeImageException : RecipeExceptions()
data object EmptyRecipeNameException : RecipeExceptions()
data object EmptyRecipeTypeException : RecipeExceptions()
data object EmptyIngredientException : RecipeExceptions()
data object EmptyStepException : RecipeExceptions()