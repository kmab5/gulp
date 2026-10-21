package app.gulp.android.core.navigation

/** User-facing names are intentionally plain. The 4 Rs are internal principles only. */
enum class GulpTab(val route: String, val label: String) {
    HOME("home", "Home"),
    REMINDERS("reminders", "Reminders"),
    HISTORY("history", "History"),
    REVIEW("review", "Review"),
    SETTINGS("settings", "Settings");

    companion object {
        fun fromRoute(value: String?): GulpTab = entries.firstOrNull { it.route == value } ?: HOME
    }
}
