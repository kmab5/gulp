package app.gulp.android.feature.review

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gulp.android.core.design.FrostPanel
import app.gulp.android.core.design.GulpPalette
import app.gulp.android.core.design.Note
import app.gulp.android.core.design.SectionHeader

/** No fabricated bar data: the empty chart contains only a baseline. */
@Composable
fun ReviewScreen() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 31.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)) {
        SectionHeader("Review")
        FrostPanel(Modifier.fillMaxWidth(), padding = 20.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("This week", color = GulpPalette.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Note("Average logged fluids")
                Text("—", color = GulpPalette.Glacier, fontSize = 35.sp, fontWeight = FontWeight.Bold)
                Canvas(Modifier.fillMaxWidth().height(105.dp)) {
                    drawLine(GulpPalette.Edge.copy(alpha = .35f), Offset(0f, size.height - 1f), Offset(size.width, size.height - 1f), 2f)
                }
                Note("A chart will appear when records are available.")
            }
        }
        FrostPanel(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Your patterns", fontSize = 17.sp, color = GulpPalette.White, fontWeight = FontWeight.Bold)
                Note("Once you begin logging, this page will show weekly trends, favorite drinks, and how often you recorded a drink.")
            }
        }
        Note("Reports describe what you recorded, not a medical hydration score.")
    }
}
