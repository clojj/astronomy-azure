import kotlinx.coroutines.*
import java.util.concurrent.Executors

@InternalCoroutinesApi
suspend fun <T> repeating(
    interval: Long,
    action: (T) -> T,
    initialValue: T,
    condition: (T) -> Boolean
) {
    suspend fun theLoop(value: T) {
        println(value)
        delay(interval)
        val newvalue = action(value)
        if (condition(newvalue) && NonCancellable.isActive) {
            theLoop(newvalue)
        }
    }

    theLoop(initialValue)
}

val scope = CoroutineScope(Job() + Executors.newSingleThreadExecutor().asCoroutineDispatcher())

@InternalCoroutinesApi
val schedulerJob = with(scope) {
    launch {
        repeating(1000, { i: Int -> i + 1 }, 42, { i: Int -> i < 45 })
    }
}
