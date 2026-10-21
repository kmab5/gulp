package app.gulp.android.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gulp.android.core.design.FrostPanel
import app.gulp.android.core.design.GulpPalette
import app.gulp.android.core.design.Note
import app.gulp.android.core.design.PreferenceRow
import app.gulp.android.core.design.SectionHeader

@Composable
fun SettingsScreen(onPreviewAction: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 31.dp),
        verticalArrangement = Arrangement.spacedBy(19.dp)) {
        SectionHeader("Settings")
        FrostPanel(Modifier.fillMaxWidth(), padding = 20.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Your home screen", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = GulpPalette.White)
                Note("Move things around and choose what you see.")
                PreferenceRow("Customize Home", null, onPreviewAction)
            }
        }
        Text("Preferences", color = GulpPalette.SoftWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        FrostPanel(Modifier.fillMaxWidth(), padding = 17.dp) {
            Column {
                PreferenceRow("Bottles and cups", null, onPreviewAction)
                PreferenceRow("Reminder preferences", null, onPreviewAction)
                PreferenceRow("Colors and effects", null, onPreviewAction)
                PreferenceRow("Accessibility", null, onPreviewAction)
            }
        }
        Text("Data and privacy", color = GulpPalette.SoftWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        FrostPanel(Modifier.fillMaxWidth(), padding = 17.dp) {
            Column {
                PreferenceRow("Export your drinks", null, onPreviewAction)
                PreferenceRow("Delete your data", null, onPreviewAction)
            }
        }
        Note("gulp · Phase 1 foundation preview")
    }
}
