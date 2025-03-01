package com.hcdev.recipe.data

import android.content.Context
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Singleton
import java.io.IOException
import java.io.InputStream
import kotlin.concurrent.read

@Singleton
class RecipeTypeLocalSource(
    private val context: Context,
    private val json: Json,
) {

    suspend fun getRecipeTypes(): List<RecipeType> {
        val jsonString: String
        try {
            val inputStream: InputStream = context.assets.open("recipetypes.json")
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            jsonString = String(buffer, Charsets.UTF_8)
            return json.decodeFromString(jsonString)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return emptyList()
    }
}