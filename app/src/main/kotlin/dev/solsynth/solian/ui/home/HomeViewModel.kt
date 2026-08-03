package dev.solsynth.solian.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.SnCheckInStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _checkInStatus = MutableStateFlow<SnCheckInStatus?>(null)
    val checkInStatus: StateFlow<SnCheckInStatus?> = _checkInStatus

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Fetch check-in status
                _checkInStatus.value = ApiClient.api.getCheckInStatus()

                // Fetch chat summary to calculate total unread count
                val summaries = ApiClient.api.getChatSummary()
                _unreadCount.value = summaries.values.sumOf { it.unreadCount }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
