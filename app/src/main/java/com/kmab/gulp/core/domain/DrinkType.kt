package com.kmab.gulp.core.domain

/** Editable user-facing drink definition. Stable IDs are independent of mutable names. */
data class DrinkType(
    val id: String,
    val name: String,
    val iconId: String,
    val defaultAmountMl: Int,
    val legacyKind: DrinkKind = DrinkKind.OTHER,
    val archived: Boolean = false
)

/** Curated built-in vector IDs; no fonts, bitmap packs, or network dependency. */
object DrinkTypeCatalog {
    val iconIds = listOf("water", "coffee", "tea", "bottle", "glass", "milk", "juice", "sparkle", "cup", "leaf", "other")
    val builtins: List<DrinkType> = listOf(
        DrinkType("water", "Water", "water", 250, DrinkKind.WATER),
        DrinkType("coffee", "Coffee", "coffee", 200, DrinkKind.COFFEE),
        DrinkType("tea", "Tea", "tea", 200, DrinkKind.TEA),
        DrinkType("other", "Other", "other", 250, DrinkKind.OTHER)
    )
    fun defaultIcon(kind: DrinkKind): String = kind.name.lowercase()
    fun validId(id: String): Boolean = id.length in 1..64 && id.all { it.isLetterOrDigit() || it == '-' || it == '_' }
    fun sanitizeName(name: String): String = name.trim().replace(Regex("\\s+"), " ").take(32)
    fun checked(name: String, iconId: String, amount: Int) {
        require(sanitizeName(name).isNotBlank()) { "Enter a drink name" }
        require(iconId in iconIds) { "Choose a supported icon" }
        DrinkRules.checkAmount(amount)
    }
    fun resolve(id: String, catalog: List<DrinkType>): DrinkType =
        catalog.firstOrNull { it.id == id } ?: builtins.firstOrNull { it.id == id }
        ?: builtins.last()
    fun visible(saved: List<DrinkType>): List<DrinkType> {
        val byId = saved.associateBy { it.id }
        return (builtins.map { byId[it.id] ?: it } +
            saved.filter { type -> type.id !in builtins.map { it.id } })
            .filterNot { it.archived }
    }
}
