package dev.solsynth.solian.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.solsynth.solian.data.api.ApiClient
import dev.solsynth.solian.data.model.SnPost
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExploreViewModel : ViewModel() {
    private val _posts = MutableStateFlow<List<SnPost>>(emptyList())
    val posts: StateFlow<List<SnPost>> = _posts

    private val _isRefreshing = MutableStateFlow(value = false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentOffset = 0
    private val pageSize = 10

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            currentOffset = 0
            try {
                val allPosts = ApiClient.api.getTimeline(take = pageSize, offset = 0)
                _posts.value = filterTimeline(allPosts)
                currentOffset = pageSize
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadMore() {
        if (_isLoadingMore.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val morePosts = ApiClient.api.getTimeline(take = pageSize, offset = currentOffset)
                if (morePosts.isNotEmpty()) {
                    _posts.value += filterTimeline(morePosts)
                    currentOffset += pageSize
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    private fun filterTimeline(allPosts: List<SnPost>): List<SnPost> {
        return allPosts.filter { 
            (((it.parentId.isNullOrBlank() || it.parentId == "0" || it.parentId == "00000000-0000-0000-0000-000000000000"))) &&
            (it.repliedPostId.isNullOrBlank() || it.repliedPostId == "0") &&
            (it.replyToId.isNullOrBlank() || it.replyToId == "0") &&
            (it.rootId.isNullOrBlank() || it.rootId == "0") &&
            (it.parent == null && it.replyTo == null && it.repliedPost == null) &&
            (it.type == 0 || it.type == null)
        }
    }
}
