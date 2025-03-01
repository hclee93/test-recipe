package com.hcdev.recipe.data

import kotlinx.serialization.Serializable

@Serializable
data class RecipeType(
    val id: Long,
    val name: String, // support localization later
)
