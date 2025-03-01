package com.hcdev.recipetest.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.hcdev.core.AppNavigator
import com.hcdev.core.Navigator
import com.hcdev.core.data.Selectable
import com.hcdev.core.plus
import com.hcdev.recipe.data.Recipe
import com.hcdev.recipe.data.RecipeRepository
import com.hcdev.recipe.data.RecipeType
import com.hcdev.recipe.ui.AddRecipeNavigationEvent
import com.hcdev.recipe.ui.ViewRecipeDetailNavigationEvent
import com.hcdev.recipe.ui.composables.RecipeList
import com.hcdev.recipetest.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.android.annotation.KoinViewModel
import org.koin.compose.viewmodel.koinViewModel

@KoinViewModel
class HomeViewModel(
    @AppNavigator private val navigator: Navigator,
    private val recipeRepository: RecipeRepository,
) : ViewModel() {

    val recipeTypes = recipeRepository.recipeTypes

    private val _appliedRecipeTypeFilter = MutableStateFlow<List<Long>>(emptyList())
    val appliedRecipeTypeFilter = _appliedRecipeTypeFilter.asStateFlow()

    val recipeTypesFilter = combine(recipeTypes, appliedRecipeTypeFilter) { types, filter ->
        types.map { Selectable(data = it, selected = filter.contains(it.id)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())


    val recipes = appliedRecipeTypeFilter.flatMapLatest { filter ->
        if (filter.isEmpty()) {
            recipeRepository.getAllRecipesFlow()
        } else {
            recipeRepository.getAllRecipesByRecipeTypeFlow(filter)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())


    fun onNavigateToRecipeDetails(recipe: Recipe) {
        navigator.navigate(ViewRecipeDetailNavigationEvent(id = recipe.id))
    }

    fun onFilterTypesChanged(types: List<Long>) {
        _appliedRecipeTypeFilter.update { types }
    }

    fun onNavigateToAddRecipe() {
        navigator.navigate(AddRecipeNavigationEvent)
    }
}

@Serializable
object HomeRoute

@Composable
fun HomeScreen(modifier: Modifier = Modifier, viewModel: HomeViewModel = koinViewModel()) {
    val recipeTypesFilter by viewModel.recipeTypesFilter.collectAsStateWithLifecycle()
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    HomeScreen(
        onNavigateToRecipeDetails = viewModel::onNavigateToRecipeDetails,
        recipes = recipes,
        recipeTypesFilter = recipeTypesFilter,
        onFilterTypesChanged = viewModel::onFilterTypesChanged,
        onNavigateToAddRecipe = viewModel::onNavigateToAddRecipe,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRecipeDetails: (Recipe) -> Unit,
    recipes: List<Recipe>,
    recipeTypesFilter: List<Selectable<RecipeType>>,
    onFilterTypesChanged: (types: List<Long>) -> Unit,
    onNavigateToAddRecipe: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val scope = rememberCoroutineScope()
    var showFilterBottomSheet by rememberSaveable { mutableStateOf(false) }
    val filterBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var tempFilterSelectedId by remember {
        mutableStateOf<List<Long>>(emptyList())
    }

    if (showFilterBottomSheet) {
        ModalBottomSheet(
            sheetState = filterBottomSheetState,
            onDismissRequest = {
                showFilterBottomSheet = false
            }, modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(56.dp)
                ) {
                    IconButton(onClick = {
                        scope.launch {
                            filterBottomSheetState.hide()
                        }.invokeOnCompletion {
                            if (!filterBottomSheetState.isVisible) {
                                showFilterBottomSheet = false
                            }
                        }
                    }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                    Text(
                        stringResource(R.string.select_category_for_filter_title),
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                LazyColumn(modifier = Modifier.weight(1f, false)) {
                    items(recipeTypesFilter, key = { it.data.id }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable(onClick = {
                                    if (tempFilterSelectedId.contains(it.data.id)) {
                                        tempFilterSelectedId -= it.data.id
                                    } else {
                                        tempFilterSelectedId += it.data.id
                                    }
                                })
                                .fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = tempFilterSelectedId.contains(it.data.id),
                                onClick = {
                                    if (tempFilterSelectedId.contains(it.data.id)) {
                                        tempFilterSelectedId -= it.data.id
                                    } else {
                                        tempFilterSelectedId += it.data.id
                                    }
                                }
                            )
                            Text(it.data.name)
                        }
                    }
                }
                Button(
                    onClick = {
                        onFilterTypesChanged(tempFilterSelectedId)
                        scope.launch {
                            filterBottomSheetState.hide()
                        }.invokeOnCompletion {
                            if (!filterBottomSheetState.isVisible) {
                                showFilterBottomSheet = false
                            }
                        }
                    }, modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(stringResource(R.string.apply))
                }
            }
        }
    }

    val filterSize by remember(recipeTypesFilter) {
        derivedStateOf {
            recipeTypesFilter.filter { it.selected }.size
        }
    }
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddRecipe) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_recipe))
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.recipes))
                },
                actions = {
                    BadgedBox(badge = {
                        if (filterSize > 0) {
                            Badge(modifier = Modifier.offset((-8).dp, 8.dp)) {
                                Text(filterSize.toString())
                            }
                        }
                    }) {
                        IconButton(onClick = {
                            tempFilterSelectedId =
                                recipeTypesFilter.filter { it.selected }.map { it.data.id }
                            showFilterBottomSheet = true
                        }) {
                            Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (recipes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_recipes_found))
                }
            }

        } else {
            RecipeList(
                recipes = recipes, onRecipeClick = onNavigateToRecipeDetails, modifier = Modifier
                    .consumeWindowInsets(innerPadding),
                contentPadding = innerPadding + PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp + 72.dp // fab height 56 + spacing between 16
                )
            )
        }
    }
}
