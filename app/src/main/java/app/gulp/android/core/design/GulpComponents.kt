package app.gulp.android.core.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(title, modifier = modifier, fontWeight = FontWeight.Bold, color = GulpPalette.White, fontSize = 23.sp)
}

@Composable
fun Note(text: String, modifier: Modifier = Modifier, color: Color = GulpPalette.SoftWhite) {
    Text(text, modifier = modifier, fontSize = 12.sp, color = color, lineHeight = 17.sp)
}

@Composable
fun FrostPanel(
    modifier: Modifier = Modifier,
    padding: Dp = 18.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(GulpPalette.Surface.copy(alpha = 0.86f), RoundedCornerShape(23.dp))
            .border(BorderStroke(1.dp, GulpPalette.Edge.copy(alpha = 0.18f)), RoundedCornerShape(23.dp))
            .padding(padding)
    ) { content() }
}

@Composable
fun PrimaryAction(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .background(GulpPalette.Glacier, RoundedCornerShape(21.dp))
            .clickable(onClickLabel = label, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                Modifier.background(GulpPalette.Ink, RoundedCornerShape(13.dp)).padding(horizontal = 12.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = GulpPalette.White, fontSize = 26.sp, lineHeight = 26.sp)
            }
            Text(label, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = GulpPalette.Ink)
        }
    }
}

@Composable
fun PreferenceRow(label: String, supporting: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable(onClickLabel = label, onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = GulpPalette.White)
            if (supporting != null) Note(supporting)
        }
        Text("›", fontSize = 25.sp, color = GulpPalette.SoftWhite, maxLines = 1, overflow = TextOverflow.Clip)
    }
}
