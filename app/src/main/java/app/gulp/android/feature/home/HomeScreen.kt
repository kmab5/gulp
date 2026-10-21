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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gulp.android.core.design.FrostPanel
import app.gulp.android.core.design.GulpPalette
import app.gulp.android.core.design.Note
import app.gulp.android.core.design.PrimaryAction

/** Navigation/layout scaffold only. No data writes in Phase 1. */
@Composable
fun HomeScreen(onPreviewAction: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 23.dp, vertical = 24.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("NEXT REMINDER", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, fontSize = 10.sp, color = GulpPalette.SoftWhite)
                    Text("Not set", fontWeight = FontWeight.Bold, fontSize = 25.sp, color = GulpPalette.White)
                }
                Text("gulp", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = GulpPalette.White)
            }
            Spacer(Modifier.height(4.dp))
            PrimaryAction("Add a drink", onClick = onPreviewAction)
            FrostPanel(modifier = Modifier.fillMaxWidth().clickable(onClick = onPreviewAction), padding = 15.dp) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("250 mL", color = GulpPalette.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Note("Water")
                    }
                    Text("+", fontSize = 25.sp, color = GulpPalette.White)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickTile("Coffee", "200 mL", Modifier.weight(1f), onPreviewAction)
                QuickTile("Custom", "Any amount", Modifier.weight(1f), onPreviewAction)
            }
        }
        Column(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("Preview total", color = GulpPalette.Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("0", fontWeight = FontWeight.ExtraBold, fontSize = 43.sp, lineHeight = 44.sp, color = GulpPalette.Ink)
                Text("mL", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = GulpPalette.Ink, modifier = Modifier.padding(bottom = 5.dp))
            }
            Note("Demo layout · nothing is saved", color = GulpPalette.Ink.copy(alpha = .85f))
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
