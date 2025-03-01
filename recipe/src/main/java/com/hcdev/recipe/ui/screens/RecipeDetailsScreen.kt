package com.hcdev.recipe.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import coil3.compose.AsyncImage
import com.hcdev.core.AppNavigator
import com.hcdev.core.Navigator
import com.hcdev.recipe.R
import com.hcdev.recipe.data.Recipe
import com.hcdev.recipe.data.RecipeRepository
import com.hcdev.recipe.ui.EditRecipeNavigationEvent
import com.hcdev.recipe.ui.composables.RecipeCategoryChip
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.android.annotation.KoinViewModel
import org.koin.compose.viewmodel.koinViewModel

@KoinViewModel
class RecipeDetailsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository,
    @AppNavigator private val navigator: Navigator,
) : ViewModel() {

    private val args = savedStateHandle.toRoute<RecipeDetailsRoute>()
    private val id = args.id
    val recipe =
        recipeRepository.getRecipeByIdFlow(id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onNavigateToEditRecipe() {
        navigator.navigate(EditRecipeNavigationEvent(id = id))
    }

    fun onDeleteRecipe() {
        recipe.value?.let {
            viewModelScope.launch {
                recipeRepository.deleteRecipe(it)
                onBack()
            }
        }

    }

    fun onBack() {
        navigator.back()
    }
}

@Serializable
data class RecipeDetailsRoute(
    val id: Long,
)

@Composable
fun RecipeDetailsScreen(
    modifier: Modifier = Modifier, viewModel: RecipeDetailsViewModel = koinViewModel()
) {
    val recipe by viewModel.recipe.collectAsStateWithLifecycle()
    RecipeDetailsScreen(
        onBack = viewModel::onBack,
        onNavigateToEditRecipe = viewModel::onNavigateToEditRecipe,
        onDeleteRecipe = viewModel::onDeleteRecipe,
        recipe = recipe,
        modifier = modifier,
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailsScreen(
    onBack: () -> Unit,
    onNavigateToEditRecipe: () -> Unit,
    onDeleteRecipe: () -> Unit,
    recipe: Recipe?,
    modifier: Modifier = Modifier,
) {

    var showDeleteConfirmationDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            text = {
                Text(stringResource(R.string.delete_recipe_confirmation_msg))
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmationDialog = false
                    onDeleteRecipe()
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(title = {

            }, navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }, actions = {
                IconButton(onClick = {
                    showDeleteConfirmationDialog = true
                }) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
                IconButton(onClick = onNavigateToEditRecipe) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                }
            })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .verticalScroll(
                    rememberScrollState()
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = recipe?.image,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            Column(
                modifier = Modifier.padding(all = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = recipe?.name.orEmpty(),
                    style = MaterialTheme.typography.displaySmall
                )
                recipe?.let {
                    RecipeCategoryChip(it.typeDisplayName)
                }
                Column {
                    Text(
                        text = stringResource(R.string.ingredients),
                        modifier = Modifier.padding(vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    recipe?.ingredients?.forEach { ingredient ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                        ) {
                            Spacer(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurface)
                                    .size(6.dp)
                            )
                            Text(
                                ingredient,
                                modifier = Modifier
                                    .weight(1f)
                            )
                        }
                    }
                }
                Column {
                    Text(
                        text = stringResource(R.string.steps),
                        modifier = Modifier.padding(vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    recipe?.steps?.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                        ) {
                            Text("${index + 1}. ", modifier = Modifier)
                            Text(step, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

        }

    }
}
