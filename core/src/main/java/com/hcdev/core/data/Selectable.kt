package com.hcdev.core.data

data class Selectable<T>(
    val data: T,
    val selected: Boolean = false
)
