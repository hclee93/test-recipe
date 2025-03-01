package com.hcdev.recipetest

import android.content.Context
import androidx.room.Room
import com.hcdev.core.AppNavigator
import com.hcdev.core.Navigator
import com.hcdev.recipe.RecipeModule
import com.hcdev.recipetest.data.AppDatabase
import com.hcdev.recipe.data.RecipeTypeConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton


@Module(includes = [RecipeModule::class])
@ComponentScan
class AppModule {

    @AppNavigator
    @Singleton
    fun appNavigator() = Navigator()

    @Singleton
    fun recipeDao(database: AppDatabase) = database.recipeDao()

    @Singleton
    fun recipeTypeConverter(json: Json) = RecipeTypeConverter(json)


    @Singleton
    fun appDatabase(
        context: Context,
        recipeTypeConverter: RecipeTypeConverter,
    ): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "app.db")
        .createFromAsset("database/app_db.db")
        .fallbackToDestructiveMigration(true)
        .addTypeConverter(recipeTypeConverter)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

    @Singleton
    fun json() = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
}
