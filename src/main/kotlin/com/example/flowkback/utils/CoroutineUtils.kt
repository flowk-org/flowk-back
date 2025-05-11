package com.example.flowkback.utils

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi

object CoroutineUtils {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T> Deferred<T>.then(onSuccess: (T) -> Unit): Deferred<T> {
        invokeOnCompletion { throwable ->
            if (throwable == null) {
                onSuccess(this.getCompleted())
            }
        }
        return this
    }

    fun <T> Deferred<T>.catch(onError: (Throwable) -> Unit) {
        invokeOnCompletion { throwable ->
            if (throwable != null) {
                onError(throwable)
            }
        }
    }
}
