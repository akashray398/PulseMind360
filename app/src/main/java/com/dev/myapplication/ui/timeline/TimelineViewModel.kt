package com.dev.myapplication.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.myapplication.data.local.dao.ActivityLogDao
import com.dev.myapplication.data.local.entity.ActivityLogEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val activityLogDao: ActivityLogDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val logsState: StateFlow<List<ActivityLogEntity>> = combine(
        _searchQuery,
        _selectedCategory
    ) { query, category ->
        query to category
    }.flatMapLatest { (query, category) ->
        when {
            query.isNotBlank() -> activityLogDao.searchLogs(query)
            category != "ALL" -> activityLogDao.getLogsByCategory(category)
            else -> activityLogDao.getAllLogs()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun clearHistory() {
        viewModelScope.launch {
            activityLogDao.clearLogs()
        }
    }
}
