package com.hcdev.recipetest

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.ksp.generated.module

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(AppModule().module)
            androidContext(this@App)
        }
    }
}