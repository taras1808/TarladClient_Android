package com.tarlad.client.helpers

sealed class TarladResult<T>

class OnComplete<T>(val data: T) : TarladResult<T>()

class OnError<T>(val t: Throwable): TarladResult<T>()