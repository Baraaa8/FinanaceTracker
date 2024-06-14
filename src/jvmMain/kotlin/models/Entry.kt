package models

data class Entry(
    val id: String?, val date: String?, val information: String? = "", val name: String = "", val value: Double = 0.0
)