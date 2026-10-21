package app.gulp.android.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gulp.android.core.design.FrostPanel
import app.gulp.android.core.design.GulpPalette
import app.gulp.android.core.design.Note
import app.gulp.android.core.design.PrimaryAction
import app.gulp.android.core.graphics.WaterAdaptiveText
import app.gulp.android.core.graphics.WaterScene

/** Interactive water demonstration. Drink logging is deliberately not persistent in Phase 2. */
@Composable
fun HomeScreen(
    scene: WaterScene,
    viewport: IntSize,
    onPreviewAction: () -> Unit,
    onWaterLab: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 23.dp, vertical = 24.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    WaterAdaptiveText("NEXT REMINDER", scene, viewport,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, fontSize = 10.sp)
                    WaterAdaptiveText("Not set", scene, viewport,
                        fontWeight = FontWeight.Bold, fontSize = 25.sp)
                }
                WaterAdaptiveText("gulp", scene, viewport, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            }
            Spacer(Modifier.height(4.dp))
            PrimaryAction("Add a drink", onClick = onPreviewAction)
            FrostPanel(Modifier.fillMaxWidth().clickable(onClickLabel = "Preview 250 mL water") { onPreviewAction() }, padding = 15.dp) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("250 mL", color = GulpPalette.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Note("Water")
                    }
                    Text("+", fontSize = 25.sp, color = GulpPalette.White)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickTile("Coffee", "200 mL", Modifier.weight(1f), onPreviewAction)
                QuickTile("Custom", "Any amount", Modifier.weight(1f), onWaterLab)
            }
            Text("Try water effects", modifier = Modifier
                .background(GulpPalette.DeepBlue.copy(alpha = .42f), RoundedCornerShape(18.dp))
                .clickable(onClickLabel = "Adjust water demo") { onWaterLab() }
                .padding(horizontal = 15.dp, vertical = 11.dp),
                color = GulpPalette.Glacier, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
        Column(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)) {
            WaterAdaptiveText("Today", scene, viewport, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                WaterAdaptiveText("0", scene, viewport, fontWeight = FontWeight.ExtraBold,
                    fontSize = 43.sp, lineHeight = 44.sp)
                WaterAdaptiveText("mL", scene, viewport, fontWeight = FontWeight.Bold,
                    fontSize = 17.sp, modifier = Modifier.padding(bottom = 5.dp))
            }
            WaterAdaptiveText("Water is animated for preview only", scene, viewport, fontSize = 11.sp)
        }
    }
}

@Composable
private fun QuickTile(label: String, detail: String, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.height(94.dp)
            .background(GulpPalette.Glacier.copy(alpha = .36f), RoundedCornerShape(18.dp))
            .clickable(onClickLabel = label, onClick = onClick)
            .padding(15.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(label, color = GulpPalette.Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(detail, color = GulpPalette.Ink.copy(alpha = .9f), fontSize = 12.sp)
    }
}
