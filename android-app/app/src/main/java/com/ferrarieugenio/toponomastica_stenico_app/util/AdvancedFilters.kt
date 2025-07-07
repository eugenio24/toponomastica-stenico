package com.ferrarieugenio.toponomastica_stenico_app.util

data class AdvancedFilters(
    val minQuota: Int? = null,
    val maxQuota: Int? = null,
    val selectedTags: List<String> = emptyList(),
    val selectedClusters: List<String> = emptyList(),
    val selectedHcClusters: List<String> = emptyList()
) {
    fun countActiveFilters(): Int {
        var count = 0
        if (minQuota != null) count++
        if (maxQuota != null) count++
        if (selectedTags.isNotEmpty()) count++
        if (selectedClusters.isNotEmpty()) count++
        if (selectedHcClusters.isNotEmpty()) count++
        return count
    }
}