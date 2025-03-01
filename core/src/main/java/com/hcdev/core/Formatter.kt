package com.hcdev.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.res.stringResource


@Composable
fun Throwable.formatAsLocalizedMessage(): String {
    return LocalErrorFormatter.current.formatError(this)
}

@Composable
fun ProvideLocalErrorFormatter(
    errorFormatter: ErrorFormatter,
    content: @Composable () -> Unit
) = CompositionLocalProvider(
    LocalErrorFormatter provides errorFormatter,
    content = content
)

interface ErrorFormatter {
    @Composable
    fun formatError(throwable: Throwable): String
}

private val LocalErrorFormatter = compositionLocalOf<ErrorFormatter> {
    object : ErrorFormatter {
        @Composable
        override fun formatError(throwable: Throwable): String {
            return stringResource(R.string.default_error_msg)
        }
    }
}