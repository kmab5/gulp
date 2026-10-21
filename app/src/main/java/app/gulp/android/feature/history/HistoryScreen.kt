package app.gulp.android.feature.history

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
import app.gulp.android.core.design.SectionHeader

@Composable
fun HistoryScreen() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 31.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)) {
        SectionHeader("History")
        FrostPanel(Modifier.fillMaxWidth(), padding = 24.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Drink records", fontWeight = FontWeight.Bold, color = GulpPalette.White, fontSize = 19.sp)
                Text("No entries yet", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = GulpPalette.Glacier)
                Note("Your recorded drinks will appear here once logging is available.")
            }
        }
        Note("A day with no entries doesn't mean you drank nothing.")
    }
}
