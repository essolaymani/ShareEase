package com.sharease.app.data.model

data class ReceivedItem(
    val name: String,
    val size: Long,
    val type: ItemType,
    val path: String? = null
) {
    enum class ItemType {
        FILE, TEXT
    }
}
