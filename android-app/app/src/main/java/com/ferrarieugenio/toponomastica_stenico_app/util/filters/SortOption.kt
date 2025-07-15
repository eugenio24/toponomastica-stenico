package com.ferrarieugenio.toponomastica_stenico_app.util.filters

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