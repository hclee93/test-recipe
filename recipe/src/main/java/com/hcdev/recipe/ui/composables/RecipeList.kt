package com.hcdev.recipe.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hcdev.recipe.data.Recipe

@Composable
fun RecipeList(
    recipes: List<Recipe>,
    onRecipeClick: (recipe: Recipe) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(recipes, key = { item -> item.id }) { item ->
            RecipeItem(
                recipe = item,
                onRecipeClick = {
                    onRecipeClick(item)
                }
            )
        }
    }
}