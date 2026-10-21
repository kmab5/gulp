package app.gulp.android.feature.reminders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
fun RemindersScreen(onPreviewAction: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 31.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)) {
        SectionHeader("Reminders")
        FrostPanel(Modifier.fillMaxWidth(), padding = 22.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("NEXT REMINDER", fontSize = 11.sp, letterSpacing = 1.sp, color = GulpPalette.SoftWhite)
                Text("Not set", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = GulpPalette.White)
                Note("Choose your schedule once reminder setup is available.")
            }
        }
        Spacer(Modifier.height(1.dp))
        Text("Your day", color = GulpPalette.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        FrostPanel(Modifier.fillMaxWidth(), padding = 16.dp) {
            Column {
                PreferenceRow("Reminder interval", "Choose how often to receive a prompt", onPreviewAction)
                PreferenceRow("Quiet hours", "No alerts while you're sleeping", onPreviewAction)
                PreferenceRow("Pause reminders", null, onPreviewAction)
            }
        }
        Note("Intervals are reminders, not required drinking times.")
    }
}
