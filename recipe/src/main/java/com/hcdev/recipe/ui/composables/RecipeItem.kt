package com.hcdev.recipe.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.hcdev.recipe.data.Recipe

@Composable
fun RecipeItem(
    recipe: Recipe,
    onRecipeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onRecipeClick, modifier = modifier.aspectRatio(16 / 9F)) {
        Box {
            AsyncImage(
                model = recipe.image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxSize()

                    // to cater image and the text contrast insufficient
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                Pair(0F, Color.Transparent),
                                Pair(0.6F, MaterialTheme.colorScheme.surface.copy(alpha = 0.23F)),
                                Pair(1F, MaterialTheme.colorScheme.surface.copy(alpha = 0.5F))
                            ),
                        )
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.Bottom),

                ) {
                RecipeCategoryChip(recipe.typeDisplayName)
                Text(
                    recipe.name,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 3,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}