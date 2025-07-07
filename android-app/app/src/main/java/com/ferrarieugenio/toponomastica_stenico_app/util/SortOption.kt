package com.ferrarieugenio.toponomastica_stenico_app.util

enum class SortField {
    NAME, QUOTA
}

enum class SortDirection {
    ASCENDING, DESCENDING
}

data class SortOption(
    val field: SortField = SortField.NAME,
    val direction: SortDirection = SortDirection.ASCENDING
)