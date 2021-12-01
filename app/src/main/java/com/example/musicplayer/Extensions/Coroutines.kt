package com.example.musicplayer.Extensions

import kotlinx.coroutines.*

val job = SupervisorJob()
val backgroundscope = CoroutineScope(Dispatchers.Default + job)
val mainscope = CoroutineScope(Dispatchers.Main + job)


fun cancelallwork(){
    backgroundscope.coroutineContext.cancelChildren()
    mainscope.coroutineContext.cancelChildren()
}
