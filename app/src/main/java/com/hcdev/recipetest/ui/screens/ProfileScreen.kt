package com.hcdev.recipetest.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import kotlinx.serialization.Serializable
import org.koin.android.annotation.KoinViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 *  For authentication later
 */
@KoinViewModel
class ProfileViewModel() : ViewModel() {


}

@Serializable
object ProfileRoute


@Composable
fun ProfileScreen(modifier: Modifier = Modifier, viewModel: ProfileViewModel = koinViewModel()) {
    ProfileScreen(
        modifier = modifier,
    )
}

