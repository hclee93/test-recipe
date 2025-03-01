package com.hcdev.recipe.ui.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.hcdev.core.formatAsLocalizedMessage
import com.hcdev.recipe.R
import com.hcdev.recipe.data.Recipe
import com.hcdev.recipe.data.RecipeRepository
import com.hcdev.recipe.data.RecipeType
import com.hcdev.recipe.domain.RecipeImageValidationUseCase
import com.hcdev.recipe.domain.RecipeIngredientsValidationUseCase
import com.hcdev.recipe.domain.RecipeNameValidationUseCase
import com.hcdev.recipe.domain.RecipeStepsValidationUseCase
import com.hcdev.recipe.domain.RecipeTypeValidationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.android.annotation.KoinViewModel
import org.koin.compose.viewmodel.koinViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

enum class AddEditRecipeMode {
    Add,
    Edit
}

@KoinViewModel
class AddEditRecipeViewModel(
    @AppNavigator private val navigator: Navigator,
    private val savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository,
    private val recipeImageValidationUseCase: RecipeImageValidationUseCase,
    private val recipeNameValidationUseCase: RecipeNameValidationUseCase,
    private val recipeTypeValidationUseCase: RecipeTypeValidationUseCase,
    private val recipeIngredientsValidationUseCase: RecipeIngredientsValidationUseCase,
    private val recipeStepsValidationUseCase: RecipeStepsValidationUseCase,
) : ViewModel() {

    private val args = savedStateHandle.toRoute<AddEditRecipeRoute>()
    private val id = args.id

    val mode = if (id == null) AddEditRecipeMode.Add else AddEditRecipeMode.Edit

    private val _recipe = MutableStateFlow(Recipe.Empty)
    val recipe = _recipe.asStateFlow()

    private val _savedRecipe = MutableStateFlow<Recipe?>(null)
    val savedRecipe = _savedRecipe.asStateFlow()

    val recipeTypes = recipeRepository.recipeTypes

    private val _errorImage = MutableStateFlow<Throwable?>(null)
    private val _errorName = MutableStateFlow<Throwable?>(null)
    private val _errorTypes = MutableStateFlow<Throwable?>(null)
    private val _errorIngredients = MutableStateFlow<Throwable?>(null)
    private val _errorSteps = MutableStateFlow<Throwable?>(null)

    val errorImage = _errorImage.asStateFlow()
    val errorName = _errorName.asStateFlow()
    val errorTypes = _errorTypes.asStateFlow()
    val errorIngredients = _errorIngredients.asStateFlow()
    val errorSteps = _errorSteps.asStateFlow()

    private var autoValidate: Boolean = false

    init {
        if (mode == AddEditRecipeMode.Edit && id != null) {
            viewModelScope.launch {
                val recipe = recipeRepository.getRecipeById(id)
                recipe?.let { recipe ->
                    _recipe.update { recipe }
                    _savedRecipe.update { recipe }
                }
            }
        }
    }

    private fun validate(): Boolean {
        autoValidate = true
        val recipe = recipe.value
        _errorImage.update { recipeImageValidationUseCase(recipe.image) }
        _errorName.update { recipeNameValidationUseCase(recipe.name) }
        _errorTypes.update { recipeTypeValidationUseCase(recipe.type, recipeTypes.value) }
        _errorIngredients.update { recipeIngredientsValidationUseCase(recipe.ingredients) }
        _errorSteps.update { recipeStepsValidationUseCase(recipe.steps) }
        return errorImage.value == null && errorName.value == null && errorTypes.value == null && errorIngredients.value == null && errorSteps.value == null
    }

    fun onSaveRecipe() {
        if (validate()) {
            viewModelScope.launch {
                recipeRepository.upsertRecipe(recipe.value)
                onBack()
            }
        }
    }


    fun onUpdateImage(uri: Uri) {
        _recipe.update {
            it.copy(image = uri.toString())
        }
        if (autoValidate) {
            validate()
        }
    }

    fun onUpdateRecipeName(name: String) {
        _recipe.update {
            it.copy(name = name)
        }
        if (autoValidate) {
            validate()
        }
    }


    fun onUpdateRecipeType(value: RecipeType) {
        _recipe.update {
            it.copy(type = value.id, typeDisplayName = value.name)
        }
        if (autoValidate) {
            validate()
        }
    }

    fun onEditIngredient(ingredient: String, index: Int) {
        _recipe.update {
            it.copy(ingredients = it.ingredients.toMutableList().apply {
                set(index, ingredient)
            }.toList())
        }
        if (autoValidate) {
            validate()
        }
    }

    fun onAddIngredient(ingredient: String) {
        _recipe.update {
            it.copy(ingredients = it.ingredients + ingredient)
        }
        if (autoValidate) {
            validate()
        }
    }

    fun onDeleteIngredient(index: Int) {
        _recipe.update {
            it.copy(ingredients = it.ingredients.toMutableList().apply {
                removeAt(index)
            }.toList())
        }
        if (autoValidate) {
            validate()
        }
    }

    fun onEditStep(step: String, index: Int) {
        _recipe.update {
            it.copy(steps = it.steps.toMutableList().apply {
                set(index, step)
            }.toList())
        }
        if (autoValidate) {
            validate()
        }
    }

    fun onAddStep(step: String) {
        _recipe.update {
            it.copy(steps = it.steps + step)
        }
        if (autoValidate) {
            validate()
        }
    }

    fun onDeleteStep(index: Int) {
        _recipe.update {
            it.copy(steps = it.steps.toMutableList().apply {
                removeAt(index)
            }.toList())
        }
        if (autoValidate) {
            validate()
        }
    }

    fun onBack() {
        navigator.back()
    }
}

@Serializable
data class AddEditRecipeRoute(val id: Long?)

@Composable
fun AddEditRecipeScreen(
    modifier: Modifier = Modifier,
    viewModel: AddEditRecipeViewModel = koinViewModel()
) {
    val recipe by viewModel.recipe.collectAsStateWithLifecycle()
    val recipeTypes by viewModel.recipeTypes.collectAsStateWithLifecycle()
    val savedRecipe by viewModel.savedRecipe.collectAsStateWithLifecycle()
    val errorImage by viewModel.errorImage.collectAsStateWithLifecycle()
    val errorName by viewModel.errorName.collectAsStateWithLifecycle()
    val errorTypes by viewModel.errorTypes.collectAsStateWithLifecycle()
    val errorIngredients by viewModel.errorIngredients.collectAsStateWithLifecycle()
    val errorSteps by viewModel.errorSteps.collectAsStateWithLifecycle()

    AddEditRecipeScreen(
        onBack = viewModel::onBack,
        mode = viewModel.mode,
        savedRecipe = savedRecipe,
        recipe = recipe,
        errorImage = errorImage,
        errorName = errorName,
        errorTypes = errorTypes,
        errorIngredients = errorIngredients,
        errorSteps = errorSteps,
        onUpdateImage = viewModel::onUpdateImage,
        onUpdateRecipeName = viewModel::onUpdateRecipeName,
        onSaveRecipe = viewModel::onSaveRecipe,
        onEditIngredient = viewModel::onEditIngredient,
        onAddIngredient = viewModel::onAddIngredient,
        onEditStep = viewModel::onEditStep,
        onAddStep = viewModel::onAddStep,
        onDeleteIngredient = viewModel::onDeleteIngredient,
        onDeleteStep = viewModel::onDeleteStep,
        onUpdateRecipeType = viewModel::onUpdateRecipeType,
        recipeTypes = recipeTypes,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecipeScreen(
    onBack: () -> Unit,
    mode: AddEditRecipeMode,
    savedRecipe: Recipe?,
    recipe: Recipe,
    onUpdateImage: (uri: Uri) -> Unit,
    onUpdateRecipeName: (name: String) -> Unit,
    onSaveRecipe: () -> Unit,
    errorImage: Throwable?,
    errorName: Throwable?,
    errorTypes: Throwable?,
    errorIngredients: Throwable?,
    errorSteps: Throwable?,
    onEditIngredient: (ingredient: String, index: Int) -> Unit,
    onAddIngredient: (ingredient: String) -> Unit,
    onDeleteIngredient: (index: Int) -> Unit,
    onEditStep: (step: String, index: Int) -> Unit,
    onAddStep: (step: String) -> Unit,
    onDeleteStep: (index: Int) -> Unit,
    onUpdateRecipeType: (recipeType: RecipeType) -> Unit,
    recipeTypes: List<RecipeType>,
    modifier: Modifier = Modifier,
) {

    val context = LocalContext.current

    // Registers a photo picker activity launcher in single-select mode.
    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                onUpdateImage(uri)
            }
        }

    // take picture launcher
    var photoUri: Uri? by remember { mutableStateOf(null) }
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                onUpdateImage(photoUri!!)
            }
        }

    var showBackReminderDialog by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    var showImageOptionsBottomSheet by rememberSaveable { mutableStateOf(false) }
    val imageOptionsBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    if (showImageOptionsBottomSheet) {
        ModalBottomSheet(
            sheetState = imageOptionsBottomSheetState,
            onDismissRequest = {
                showImageOptionsBottomSheet = false
            }, modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Row(modifier = Modifier
                .clickable {
                    val timeStamp: String =
                        SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())


                    val file = File(
                        context.filesDir,
                        "JPEG_${timeStamp}.jpg", /* suffix */
                    )
                    photoUri = FileProvider.getUriForFile(
                        context,
                        context.packageName + ".provider",
                        file
                    )
                    cameraLauncher.launch(photoUri!!)
                    showImageOptionsBottomSheet = false
                }
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(R.string.camera))
            }
            Row(modifier = Modifier
                .clickable {
                    // Launch the photo picker and let the user choose only images.
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    showImageOptionsBottomSheet = false
                }
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(text = stringResource(R.string.gallery))
            }
        }
    }

    var showRecipeTypeBottomSheet by rememberSaveable { mutableStateOf(false) }
    val recipeTypeBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    if (showRecipeTypeBottomSheet) {
        ModalBottomSheet(
            sheetState = recipeTypeBottomSheetState,
            onDismissRequest = {
                showRecipeTypeBottomSheet = false
            }, modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(56.dp)
                ) {
                    IconButton(onClick = {
                        scope.launch {
                            recipeTypeBottomSheetState.hide()
                        }.invokeOnCompletion {
                            if (!recipeTypeBottomSheetState.isVisible) {
                                showRecipeTypeBottomSheet = false
                            }
                        }
                    }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                    Text(
                        stringResource(R.string.select_recipe_category_title),
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                LazyColumn(modifier = Modifier.weight(1f, false)) {
                    items(recipeTypes, key = { it.id }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable(onClick = {
                                    onUpdateRecipeType(it)
                                    scope.launch {
                                        recipeTypeBottomSheetState.hide()
                                    }.invokeOnCompletion {
                                        if (!recipeTypeBottomSheetState.isVisible) {
                                            showRecipeTypeBottomSheet = false
                                        }
                                    }
                                })
                                .fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = recipe.type == it.id,
                                onClick = {
                                    onUpdateRecipeType(it)
                                    scope.launch {
                                        recipeTypeBottomSheetState.hide()
                                    }.invokeOnCompletion {
                                        if (!recipeTypeBottomSheetState.isVisible) {
                                            showRecipeTypeBottomSheet = false
                                        }
                                    }
                                }
                            )
                            Text(it.name)
                        }
                    }
                }
            }
        }
    }

    if (showBackReminderDialog) {
        AlertDialog(onDismissRequest = {
            showBackReminderDialog = false
        }, confirmButton = {
            TextButton(onClick = {
                showBackReminderDialog = false
                onBack()
            }) {
                Text(text = stringResource(R.string.discard))
            }
        }, dismissButton = {
            TextButton(onClick = {
                showBackReminderDialog = false
            }) {
                Text(text = stringResource(R.string.cancel))
            }
        }, text = {
            Text(stringResource(R.string.exit_edit_recipe_reminder_msg))
        })
    }

    // Check whether have unsaved changes and display a reminder alert dialog
    // if there is unsaved changes and user attempts to quit editing
    val back = remember(mode, recipe, savedRecipe) {
        {
            if (recipe == (savedRecipe ?: Recipe.Empty)) {
                onBack()
            } else {
                showBackReminderDialog = true
            }
        }
    }

    val recipeType by remember(recipeTypes, recipe) {
        derivedStateOf {
            recipeTypes.find { it.id == recipe.type }
        }
    }

    BackHandler {
        back()
    }

    val isEnabledSave by remember(recipe, savedRecipe) {
        derivedStateOf { recipe != (savedRecipe ?: Recipe.Empty) }
    }

    var editingIngredientPos by rememberSaveable {
        mutableIntStateOf(-1)
    }
    var editingIngredient by rememberSaveable {
        mutableStateOf("")
    }
    var showEditingIngredient by rememberSaveable { mutableStateOf(false) }
    val editingIngredientBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    if (showEditingIngredient) {
        ModalBottomSheet(
            sheetState = editingIngredientBottomSheetState,
            onDismissRequest = {
                showEditingIngredient = false
            }, modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(56.dp)
                ) {
                    IconButton(onClick = {
                        scope.launch {
                            editingIngredientBottomSheetState.hide()
                        }.invokeOnCompletion {
                            if (!editingIngredientBottomSheetState.isVisible) {
                                showEditingIngredient = false
                            }
                        }
                    }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                    Text(
                        if (editingIngredientPos == -1) stringResource(R.string.add_ingredient) else stringResource(
                            R.string.edit_ingredient
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = editingIngredient,
                        onValueChange = {
                            editingIngredient = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(stringResource(R.string.ingredient))
                        },
                        placeholder = {
                            Text(stringResource(R.string.ingredient))
                        },
                        maxLines = 5,
                    )
                    Button(
                        onClick = {
                            if (editingIngredientPos == -1) {
                                onAddIngredient(editingIngredient)
                            } else {
                                onEditIngredient(editingIngredient, editingIngredientPos)
                            }
                            scope.launch {
                                editingIngredientBottomSheetState.hide()
                            }.invokeOnCompletion {
                                if (!editingIngredientBottomSheetState.isVisible) {
                                    showEditingIngredient = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        enabled = editingIngredient.isNotBlank()
                    ) {
                        Text(
                            if (editingIngredientPos == -1) stringResource(R.string.add) else stringResource(
                                R.string.edit
                            )
                        )
                    }
                }
            }
        }
    }

    var editingStepPos by rememberSaveable {
        mutableIntStateOf(-1)
    }
    var editingStep by rememberSaveable {
        mutableStateOf("")
    }
    var showEditingStep by rememberSaveable { mutableStateOf(false) }
    val editingStepBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    if (showEditingStep) {
        ModalBottomSheet(
            sheetState = editingStepBottomSheetState,
            onDismissRequest = {
                showEditingStep = false
            }, modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(56.dp)
                ) {
                    IconButton(onClick = {
                        scope.launch {
                            editingStepBottomSheetState.hide()
                        }.invokeOnCompletion {
                            if (!editingStepBottomSheetState.isVisible) {
                                showEditingStep = false
                            }
                        }
                    }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                    Text(
                        if (editingStepPos == -1) stringResource(R.string.add_step) else stringResource(
                            R.string.edit_step
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = editingStep,
                        onValueChange = {
                            editingStep = it
                        },
                        label = {
                            Text(stringResource(R.string.step))
                        },
                        placeholder = {
                            Text(stringResource(R.string.step))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5,
                        maxLines = 5,
                    )
                    Button(
                        onClick = {
                            if (editingStepPos == -1) {
                                onAddStep(editingStep)
                            } else {
                                onEditStep(editingStep, editingStepPos)
                            }
                            scope.launch {
                                editingStepBottomSheetState.hide()
                            }.invokeOnCompletion {
                                if (!editingStepBottomSheetState.isVisible) {
                                    showEditingStep = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        enabled = editingStep.isNotBlank()
                    ) {
                        Text(
                            if (editingStepPos == -1) stringResource(R.string.add) else stringResource(
                                R.string.edit
                            )
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (mode == AddEditRecipeMode.Add) stringResource(R.string.add_recipe) else stringResource(
                            R.string.edit_recipe
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = back) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onSaveRecipe, enabled = isEnabledSave) {
                        Text(stringResource(R.string.save))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(onClick = {
                showImageOptionsBottomSheet = true
            }, modifier = Modifier.padding(horizontal = 16.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = recipe.image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9F)
                    )
                    if (recipe.image.isEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, stringResource(R.string.add_image))
                            Text(stringResource(R.string.add_image))
                        }
                    }
                }
            }

            errorImage?.let {
                Text(
                    it.formatAsLocalizedMessage(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            OutlinedTextField(
                value = recipe.name,
                onValueChange = onUpdateRecipeName,
                label = {
                    Text(stringResource(R.string.recipe_name))
                },
                placeholder = {
                    Text(stringResource(R.string.recipe_name))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                isError = errorName != null,
                supportingText = {
                    errorName?.let {
                        Text(it.formatAsLocalizedMessage())
                    }
                },
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Column {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = stringResource(R.string.category),
                        modifier = Modifier.padding(vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .clickable {
                            showRecipeTypeBottomSheet = true
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (recipe.type == 0L) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_category)
                        )
                        Text(stringResource(R.string.add_category), modifier = Modifier.weight(1f))
                    }
                    recipeType?.let {
                        Text(it.name, modifier = Modifier.weight(1f, false))
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_category)
                        )
                    }
                }
                errorTypes?.let {
                    Text(
                        it.formatAsLocalizedMessage(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Column {
                Column {
                    Text(
                        text = stringResource(R.string.ingredients),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    recipe.ingredients.forEachIndexed { index, ingredient ->
                        Row(modifier = Modifier.clickable {
                            editingIngredientPos = index
                            editingIngredient = ingredient
                            showEditingIngredient = true
                        }) {
                            Spacer(modifier = Modifier.width(16.dp))
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp)
                            ) {
                                Spacer(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onSurface)
                                        .size(6.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    ingredient,
                                    modifier = Modifier
                                        .weight(1f)
                                )
                            }
                            IconButton(onClick = {
                                editingIngredientPos = index
                                editingIngredient = ingredient
                                showEditingIngredient = true
                            }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.edit_ingredient)
                                )
                            }
                            IconButton(onClick = {
                                onDeleteIngredient(index)
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete_ingredient)
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .clickable(onClick = {
                            editingIngredientPos = -1
                            editingIngredient = ""
                            showEditingIngredient = true
                        })
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_ingredient)
                    )
                    Text(stringResource(R.string.add_ingredient), modifier = Modifier.weight(1f))
                }
                errorIngredients?.let {
                    Text(
                        it.formatAsLocalizedMessage(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,

                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Column {
                Column {
                    Text(
                        text = stringResource(R.string.steps),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    recipe.steps.forEachIndexed { index, step ->
                        Row(modifier = Modifier
                            .clickable {
                                editingStepPos = index
                                editingStep = step
                                showEditingStep = true
                            }
                        ) {
                            Spacer(modifier = Modifier.width(16.dp))
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .weight(1f)
                            ) {
                                Text("${index + 1}. ", modifier = Modifier)
                                Text(step, modifier = Modifier.weight(1f))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    editingStepPos = index
                                    editingStep = step
                                    showEditingStep = true
                                }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = stringResource(R.string.edit_step)
                                    )
                                }
                                IconButton(onClick = {
                                    onDeleteStep(index)
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.delete_step)
                                    )
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .clickable(onClick = {
                            editingStepPos = -1
                            editingStep = ""
                            showEditingStep = true
                        })
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_step))
                    Text(stringResource(R.string.add_step), modifier = Modifier.weight(1f))
                }
                errorSteps?.let {
                    Text(
                        it.formatAsLocalizedMessage(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}
