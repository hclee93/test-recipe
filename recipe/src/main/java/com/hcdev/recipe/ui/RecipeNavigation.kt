package com.hcdev.recipe.ui

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.hcdev.core.NavigationEvent
import com.hcdev.core.Transitions
import com.hcdev.recipe.ui.screens.AddEditRecipeRoute
import com.hcdev.recipe.ui.screens.AddEditRecipeScreen
import com.hcdev.recipe.ui.screens.RecipeDetailsRoute
import com.hcdev.recipe.ui.screens.RecipeDetailsScreen


sealed interface RecipeNavigationEvent : NavigationEvent

data class ViewRecipeDetailNavigationEvent(val id: Long) : RecipeNavigationEvent
data object AddRecipeNavigationEvent : RecipeNavigationEvent
data class EditRecipeNavigationEvent(val id: Long) : RecipeNavigationEvent

fun NavGraphBuilder.recipeNavGraph() {
    composable<RecipeDetailsRoute> {
        RecipeDetailsScreen()
    }
    composable<AddEditRecipeRoute>(
        enterTransition = {
            slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(
                    durationMillis = Transitions.DEFAULT_TRANSITION_DURATION,
                    easing = LinearOutSlowInEasing
                )
            )
        },
        popExitTransition = {
            slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(
                    durationMillis = Transitions.DEFAULT_TRANSITION_DURATION,
                    easing = LinearOutSlowInEasing
                )
            )
        }
    ) {
        AddEditRecipeScreen()
    }
}