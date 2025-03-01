package com.hcdev.core

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Qualifier


interface NavigationEvent

data object BackNavigationEvent : NavigationEvent

class Navigator {
    private val _navigationEvents = MutableStateFlow(emptyList<NavigationEvent>())
    val navigationEvents = _navigationEvents.asStateFlow()

    fun navigate(event: NavigationEvent) {
        _navigationEvents.update { it + event }
    }

    fun back() = navigate(BackNavigationEvent)

    fun clearEvents() {
        _navigationEvents.update { emptyList() }
    }
}


@Qualifier
annotation class AppNavigator

fun NavController.navigateTab(
    route: Any
) {
    navigate(route) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        graph.findStartDestination().route?.let {
            popUpTo(it) {
                saveState = true
            }
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }
}

fun NavDestination?.isRouteSelected(route: Any): Boolean {
    return this?.hasRoute(route::class) == true
}

fun NavController.back() = navigateUp()