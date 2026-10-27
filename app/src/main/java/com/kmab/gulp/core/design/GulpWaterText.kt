package com.kmab.gulp.core.design

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.kmab.gulp.core.graphics.WaterAdaptiveText

/**
 * Shared water-aware text for secondary tabs and ordinary controls. Each glyph's submerged
 * pixels take the contrasting ink from the same procedural waterline as the liquid renderer.
 * Native Compose text metrics stay intact, avoiding bitmap fonts and outlined text copies.
 */
@Composable
fun GulpWaterText(
    text: String, modifier: Modifier = Modifier,
    color: Color = GulpPalette.White,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified
) {
    val scene = LocalGlassScene.current
    val viewport = LocalGlassViewport.current
    if (scene == null || viewport.width == 0 || viewport.height == 0) {
        Text(text,modifier=modifier,color=color,fontSize=fontSize,fontWeight=fontWeight,
            textAlign=textAlign,maxLines=maxLines,lineHeight=lineHeight,letterSpacing=letterSpacing)
    } else {
        WaterAdaptiveText(text,scene,viewport,modifier=modifier,fontSize=fontSize,
            fontWeight=fontWeight,textAlign=textAlign,maxLines=maxLines,lineHeight=lineHeight,
            letterSpacing=letterSpacing,above=if(color==GulpPalette.Ink) GulpPalette.White else color,
            below=GulpPalette.Ink)
    }
}
