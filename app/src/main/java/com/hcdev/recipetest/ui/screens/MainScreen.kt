package com.hcdev.recipetest.ui.screens

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.hcdev.core.isRouteSelected
import com.hcdev.core.navigateTab
import com.hcdev.recipetest.R
import kotlinx.serialization.Serializable
import org.koin.android.annotation.KoinViewModel
import org.koin.compose.viewmodel.koinViewModel


@Serializable
object MainRoute

enum class MainTabs(val route: Any) {
    Home(HomeRoute),
    Profile(ProfileRoute);

    val title: String
        @Composable get() = when (this) {
            Home -> stringResource(R.string.home)
            Profile -> stringResource(R.string.profile)
        }

    val icon: ImageVector
        get() =
            when (this) {
                Home -> Icons.Default.Home
                Profile -> Icons.Default.AccountCircle
            }

}

private val mainTabs = MainTabs.entries


@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {

    val navController = rememberNavController()
    Scaffold(
        modifier = modifier,
//        bottomBar = {
//            NavigationBar {
//                val navBackStackEntry by navController.currentBackStackEntryAsState()
//                val currentDestination = navBackStackEntry?.destination
//                mainTabs.forEach { tab ->
//                    NavigationBarItem(
//                        selected = currentDestination.isRouteSelected(tab.route),
//                        onClick = { navController.navigateTab(tab.route) },
//                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
//                        label = { Text(tab.title) }
//                    )
//                }
//            }
//        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute, enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }, modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {

            composable<HomeRoute> {
                HomeScreen()
            }

            /**
             * Add authentication later
             */
//            composable<ProfileRoute> {
//                ProfileScreen()
//            }
        }
    }
}
