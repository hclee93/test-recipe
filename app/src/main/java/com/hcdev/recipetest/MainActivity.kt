package com.hcdev.recipetest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hcdev.core.BackNavigationEvent
import com.hcdev.core.Navigator
import com.hcdev.core.ProvideLocalErrorFormatter
import com.hcdev.core.Transitions
import com.hcdev.core.back
import com.hcdev.recipe.ui.AddRecipeNavigationEvent
import com.hcdev.recipe.ui.EditRecipeNavigationEvent
import com.hcdev.recipe.ui.RecipeNavigationEvent
import com.hcdev.recipe.ui.ViewRecipeDetailNavigationEvent
import com.hcdev.recipe.ui.recipeNavGraph
import com.hcdev.recipe.ui.screens.AddEditRecipeRoute
import com.hcdev.recipe.ui.screens.RecipeDetailsRoute
import com.hcdev.recipetest.ui.AppErrorFormatter
import com.hcdev.recipetest.ui.screens.MainRoute
import com.hcdev.recipetest.ui.screens.MainScreen
import com.hcdev.recipetest.ui.theme.RecipeTestTheme
import kotlinx.coroutines.Dispatchers
import org.koin.compose.koinInject


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val navigator = koinInject<Navigator>()

            // use main immediate dispatchers to prevent event lost on edge cases
            val navigationEvents by navigator.navigationEvents.collectAsStateWithLifecycle(
                context = Dispatchers.Main.immediate
            )
            // handling navigation
            LaunchedEffect(navigationEvents) {
                if (navigationEvents.isNotEmpty()) {
                    with(navController) {
                        navigationEvents.forEach { event ->
                            when (event) {
                                is BackNavigationEvent -> back()
                                is RecipeNavigationEvent -> {
                                    when (event) {
                                        AddRecipeNavigationEvent -> navigate(AddEditRecipeRoute(id = null))
                                        is EditRecipeNavigationEvent -> navigate(
                                            AddEditRecipeRoute(
                                                id = event.id
                                            )
                                        )

                                        is ViewRecipeDetailNavigationEvent -> navigate(
                                            RecipeDetailsRoute(
                                                id = event.id
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    navigator.clearEvents()
                }
            }
            RecipeTestTheme {
                ProvideLocalErrorFormatter(errorFormatter = AppErrorFormatter) {
                    Surface {
                        NavHost(
                            navController = navController,
                            startDestination = MainRoute,
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(
                                        durationMillis = Transitions.DEFAULT_TRANSITION_DURATION,
                                        easing = LinearOutSlowInEasing
                                    )
                                )
                            },
                            exitTransition = {
                                scaleOut(
                                    targetScale = 0.9F,
                                    animationSpec = tween(
                                        durationMillis = Transitions.DEFAULT_TRANSITION_DURATION,
                                        easing = LinearOutSlowInEasing
                                    )
                                ) + fadeOut(
                                    targetAlpha = 0.8F,
                                    animationSpec = tween(
                                        durationMillis = Transitions.DEFAULT_TRANSITION_DURATION,
                                        easing = LinearOutSlowInEasing
                                    )
                                )
                            },
                            popEnterTransition = {
                                scaleIn(
                                    initialScale = 0.9F,
                                    animationSpec = tween(
                                        durationMillis = Transitions.DEFAULT_TRANSITION_DURATION,
                                        easing = LinearOutSlowInEasing
                                    )
                                ) + fadeIn(
                                    initialAlpha = 0.8F,
                                    animationSpec = tween(
                                        durationMillis = Transitions.DEFAULT_TRANSITION_DURATION,
                                        easing = LinearOutSlowInEasing
                                    )
                                )
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(
                                        durationMillis = Transitions.DEFAULT_TRANSITION_DURATION,
                                        easing = LinearOutSlowInEasing
                                    )
                                )
                            }
                        ) {
                            composable<MainRoute> {
                                MainScreen()
                            }
                            recipeNavGraph()
                        }
                    }
                }
            }
        }
    }
}
