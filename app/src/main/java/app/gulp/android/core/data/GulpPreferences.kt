package app.gulp.android.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.gulp.android.core.domain.HomeTile
import app.gulp.android.core.domain.HomeTileRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.gulpDataStore by preferencesDataStore(name = "gulp_settings")

data class GulpPreferences(
    val tiles: List<HomeTile> = HomeTileRules.defaults,
    val visualCapacityMl: Int = 2000
)

/** The water display scale is a visual choice, not a recommended fluid target. */
class GulpPreferencesStore(private val context: Context) {
    private val tileKey = stringPreferencesKey("home_tiles_v1")
    private val displayCapacityKey = intPreferencesKey("water_display_capacity_ml_v1")

    val settings: Flow<GulpPreferences> = context.gulpDataStore.data.map { p ->
        GulpPreferences(
            tiles = HomeTileRules.decode(p[tileKey]),
            visualCapacityMl = (p[displayCapacityKey] ?: 2000).coerceIn(250, 6000)
        )
    }

    suspend fun saveTiles(tiles: List<HomeTile>) {
        require(tiles.size in HomeTileRules.MIN_TILES..HomeTileRules.MAX_TILES)
        require(tiles.distinctBy { it.id }.size == tiles.size)
        require(tiles.all { it.amountMl in 1..5000 })
        context.gulpDataStore.edit { it[tileKey] = HomeTileRules.encode(tiles) }
    }

    suspend fun changeVisualCapacity(ml: Int) {
        require(ml in 250..6000)
        context.gulpDataStore.edit { it[displayCapacityKey] = ml }
    }
}
