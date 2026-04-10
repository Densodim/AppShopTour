package com.example.appshoptour.presentation.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Базовый ViewModel для всех экранов.
 *
 * S — UiState: состояние экрана (одно значение, всегда актуальное)
 * E — UiEvent: одноразовые события (навигация, снекбар)
 *
 * Использует CoroutineScope напрямую (не AndroidViewModel),
 * чтобы работать на обеих платформах.
 */
abstract class BaseViewModel<S : Any, E : Any>(
    initialState: S
) : CoroutineScope {

    private val viewModelJob = SupervisorJob()

    // Main dispatcher — для обновления UI-состояния
    override val coroutineContext = Dispatchers.Main + viewModelJob

    // StateFlow — хранит текущее состояние экрана, отдаёт последнее значение новым подписчикам
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    // SharedFlow — одноразовые события (навигация, snackbar). extraBufferCapacity=1 чтобы не терять события
    private val _events = MutableSharedFlow<E>(extraBufferCapacity = 1)
    val events: SharedFlow<E> = _events.asSharedFlow()

    /**
     * Обновляет состояние экрана через функцию-трансформацию.
     * Использование: updateState { copy(isLoading = true) }
     */
    protected fun updateState(block: S.() -> S) {
        _state.update { it.block() }
    }

    /**
     * Отправляет одноразовое событие (навигация, snackbar).
     * tryEmit никогда не блокирует — safe to call from любого потока.
     */
    protected fun emitEvent(event: E) {
        _events.tryEmit(event)
    }

    /**
     * Вызывается когда экран уничтожается.
     * На Android — обернуть в AndroidViewModel и вызвать из onCleared().
     */
    open fun onCleared() {
        viewModelJob.cancel()
    }
}
