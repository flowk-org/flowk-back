package com.example.flowkback.utils

inline fun <T, R> List<T>.mapOrEmpty(transform: (T) -> R): List<R> =
    if (isEmpty()) emptyList() else map(transform)

class ListUtils