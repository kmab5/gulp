package com.kmab.gulp.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import android.os.SystemClock
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.kmab.gulp.core.design.GulpPalette
import com.kmab.gulp.core.domain.HomeTile
import com.kmab.gulp.core.domain.HomeTileRules
import com.kmab.gulp.core.graphics.WaterAdaptiveText
import com.kmab.gulp.core.graphics.WaterScene
import java.util.UUID
import kotlin.math.roundToInt

/** Header stays fixed while an independently scrolling, feathered bento grid moves underneath. */
@Composable
fun HomeScreen(
    scene: WaterScene, viewport: IntSize, todayMl: Int,
    nextReminderLabel: String, editRequest: Int, onEditRequestHandled: () -> Unit,
    tiles: List<HomeTile>, onAddDrink: () -> Unit,
    onLogShortcut: (HomeTile) -> Unit, onSaveTiles: (List<HomeTile>) -> Unit
) {
    var editing by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf(tiles) }
    var tileToEdit by remember { mutableStateOf<HomeTile?>(null) }
    var addTile by remember { mutableStateOf(false) }
    val bounds = remember { mutableStateMapOf<String, Rect>() }
    LaunchedEffect(tiles) { if (!editing) draft = tiles }
    LaunchedEffect(editRequest) {
        if (editRequest > 0) { draft = tiles; editing = true; onEditRequestHandled() }
    }
    val displayed = if (editing) draft else tiles
    val gridUnits = displayed.fold(0) { total, tile ->
        val units = if (tile.wide) 2 else 1
        total + if (units == 2 && total % 2 == 1) 1 + units else units
    } + if (editing && draft.size < HomeTileRules.MAX_TILES) 1 else 0
    val rows = (gridUnits + 1) / 2
    val gridHeight = (rows * 112 - 12).coerceAtLeast(100)
    val gridGuideAlpha by animateFloatAsState(if (editing) .38f else 0f,
        animationSpec = tween(250), label = "edit grid guides")

    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp).padding(top = 23.dp, bottom = 7.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                WaterAdaptiveText("NEXT REMINDER", scene, viewport, fontWeight = FontWeight.Bold,
                    letterSpacing = 1.0.sp, fontSize = 11.sp)
                WaterAdaptiveText(nextReminderLabel, scene, viewport,
                    fontWeight = FontWeight.Bold, fontSize = 19.sp)
            }
            WaterAdaptiveText("gulp", scene, viewport, fontWeight = FontWeight.ExtraBold, fontSize = 25.sp)
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    // Destination-in fade removes abrupt scroll cutoffs without painting over the live water.
                    drawRect(Brush.verticalGradient(
                        0f to Color.Transparent, .065f to Color.White,
                        .90f to Color.White, 1f to Color.Transparent), blendMode = BlendMode.DstIn)
                }.padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(19.dp)) {
                Box(Modifier.fillMaxWidth()
                    .background(Brush.linearGradient(listOf(
                        Color(0xFFB8FFFF).copy(alpha = .40f),
                        Color(0xFF4CC4D0).copy(alpha = .24f))), RoundedCornerShape(22.dp))
                    .border(1.5.dp, Color.White.copy(alpha = .53f), RoundedCornerShape(22.dp))
                    .clickable(onClickLabel = "Add a drink", onClick = onAddDrink)
                    .padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                    WaterAdaptiveText("+   Add a drink", scene, viewport,
                        fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    WaterAdaptiveText("Quick add", scene, viewport, fontSize = 17.sp,
                        fontWeight = FontWeight.Bold)
                    Box(Modifier.background(if (editing) GulpPalette.Glacier else
                            GulpPalette.Surface.copy(alpha = .59f), RoundedCornerShape(14.dp))
                        .border(1.dp, Color.White.copy(alpha = .35f), RoundedCornerShape(14.dp))
                        .clickable(onClickLabel = if (editing) "Save Home layout" else "Edit Home") {
                            if (editing) onSaveTiles(draft) else draft = tiles
                            editing = !editing
                        }.padding(horizontal = 18.dp, vertical = 11.dp)) {
                        if (editing) Text("✓  Done", color = GulpPalette.Ink,
                            fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        else WaterAdaptiveText("Edit Home", scene, viewport,
                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Box(Modifier.fillMaxWidth().height(gridHeight.dp).animateContentSize()) {
                    // Visible bento slots stay fixed while the draggable tiles reflow over them.
                    Canvas(Modifier.matchParentSize()) {
                        if (gridGuideAlpha > .001f) {
                            val gap = 10.dp.toPx()
                            val rowStep = 112.dp.toPx()
                            val tileHeight = 100.dp.toPx()
                            val width = (size.width - gap) / 2f
                            for (row in 0 until rows) for (col in 0..1) {
                                drawRoundRect(color = GulpPalette.Glacier.copy(alpha = gridGuideAlpha),
                                    topLeft = Offset(col * (width + gap), row * rowStep),
                                    size = Size(width, tileHeight),
                                    cornerRadius = CornerRadius(19.dp.toPx()),
                                    style = Stroke(width = 1.dp.toPx()))
                            }
                        }
                    }
                    LazyVerticalGrid(
                    columns = GridCells.Fixed(2), userScrollEnabled = false,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().height(gridHeight.dp)
                ) {
                    items(items = displayed, key = { it.id },
                        span = { tile -> GridItemSpan(if (tile.wide) 2 else 1) }) { tile ->
                        HomeShortcutTile(tile, editing, scene, viewport,
                            Modifier.fillMaxWidth().animateItem(
                                placementSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                fadeInSpec = androidx.compose.animation.core.tween(180),
                                fadeOutSpec = androidx.compose.animation.core.tween(180)),
                            bounds = bounds,
                            onClick = { if (editing) tileToEdit = tile else onLogShortcut(tile) },
                            onMove = { id, destination ->
                                val reordered = HomeTileRules.reorder(draft, id, destination)
                                if (reordered != draft) draft = reordered
                            },
                            onFinishMove = { onSaveTiles(draft) })
                    }
                    if (editing && draft.size < HomeTileRules.MAX_TILES) {
                        item(key = "add-home-tile") {
                            Box(Modifier.fillMaxWidth().height(100.dp)
                                .background(GulpPalette.DeepBlue.copy(alpha = .15f), RoundedCornerShape(19.dp))
                                .border(1.dp, GulpPalette.Glacier.copy(alpha = .45f), RoundedCornerShape(19.dp))
                                .clickable { addTile = true }, contentAlignment = Alignment.Center) {
                                WaterAdaptiveText("+  Add tile", scene, viewport, fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                } // grid guide overlay
                AnimatedVisibility(editing, enter = fadeIn(), exit = fadeOut()) {
                    WaterAdaptiveText("Drag the handles to move tiles. Tap a tile to edit it.",
                        scene, viewport, fontSize = 12.sp)
                }
            }
        }
        Column(Modifier.fillMaxWidth().padding(top = 29.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            WaterAdaptiveText("Today", scene, viewport, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                WaterAdaptiveText(todayMl.toString(), scene, viewport, fontWeight = FontWeight.ExtraBold,
                    fontSize = 43.sp, lineHeight = 47.sp)
                WaterAdaptiveText("mL recorded", scene, viewport, fontWeight = FontWeight.Bold,
                    fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            }
        }
    }
    if (tileToEdit != null || addTile) {
        val current = tileToEdit
        TileEditorSheet(tile = current,
            onDismiss = { tileToEdit = null; addTile = false },
            onSave = { kind, ml, wide ->
                draft = if (current == null) {
                    (draft + HomeTile(UUID.randomUUID().toString().take(8), kind, ml, wide))
                        .take(HomeTileRules.MAX_TILES)
                } else draft.map { if (it.id == current.id) it.copy(kind = kind, amountMl = ml, wide = wide) else it }
                onSaveTiles(draft)
                tileToEdit = null; addTile = false
            },
            onRemove = if (current != null && draft.size > 1) ({
                draft = draft.filterNot { it.id == current.id }
                onSaveTiles(draft)
                tileToEdit = null
            }) else null)
    }
}

/** Live drag swaps slots during the gesture; the held item stays under the pointer. */
@Composable
private fun HomeShortcutTile(tile: HomeTile, editing: Boolean, scene: WaterScene, viewport: IntSize,
                             modifier: Modifier, bounds: MutableMap<String, Rect>,
                             onClick: () -> Unit, onMove: (String, String) -> Unit,
                             onFinishMove: () -> Unit) {
    var dragX by remember(tile.id) { mutableFloatStateOf(0f) }
    var dragY by remember(tile.id) { mutableFloatStateOf(0f) }
    var motionX by remember(tile.id) { mutableFloatStateOf(0f) }
    var motionY by remember(tile.id) { mutableFloatStateOf(0f) }
    var lastHover by remember(tile.id) { mutableStateOf<String?>(null) }
    var lastSwapTime by remember(tile.id) { androidx.compose.runtime.mutableLongStateOf(0L) }
    var dragging by remember(tile.id) { mutableStateOf(false) }
    var anchor by remember(tile.id) { mutableStateOf(Offset.Zero) }
    var layoutOrigin by remember(tile.id) { mutableStateOf<Offset?>(null) }
    val currentMove by rememberUpdatedState(onMove)
    val currentFinish by rememberUpdatedState(onFinishMove)
    val currentPositions by rememberUpdatedState(bounds)
    val dragHandler = if (editing) Modifier.pointerInput(tile.id, editing) {
        detectDragGesturesAfterLongPress(
            onDragStart = {
                dragging = true
                motionX = 0f; motionY = 0f; lastHover = null; lastSwapTime = 0L
                anchor = (currentPositions[tile.id]?.center ?: Offset.Zero)
            },
            onDrag = { change, delta ->
                change.consume()
                dragX += delta.x; dragY += delta.y
                motionX += delta.x; motionY += delta.y
                val target = anchor + Offset(motionX, motionY)
                val hovered = currentPositions.entries.firstOrNull { (id, rect) ->
                    // A center-inset hit zone avoids ping-pong swaps near tile borders.
                    val insetX = rect.width * .16f
                    val insetY = rect.height * .16f
                    id != tile.id && target.x >= rect.left + insetX &&
                        target.x <= rect.right - insetX &&
                        target.y >= rect.top + insetY && target.y <= rect.bottom - insetY
                }?.key
                val now = SystemClock.uptimeMillis()
                if (hovered != null && hovered != lastHover && now - lastSwapTime >= 85L) {
                    lastHover = hovered
                    lastSwapTime = now
                    currentMove(tile.id, hovered)
                }
            },
            onDragEnd = {
                dragging = false; dragX = 0f; dragY = 0f
                currentFinish()
            },
            onDragCancel = { dragging = false; dragX = 0f; dragY = 0f }
        )
    } else Modifier
    val shape = RoundedCornerShape(20.dp)
    Column(modifier
        .onGloballyPositioned { coordinates ->
            val newOrigin = coordinates.boundsInRoot().topLeft
            val previous = layoutOrigin
            if (dragging && previous != null) {
                dragX -= newOrigin.x - previous.x
                dragY -= newOrigin.y - previous.y
            }
            layoutOrigin = newOrigin
            bounds[tile.id] = Rect(newOrigin, Size(coordinates.size.width.toFloat(),
                coordinates.size.height.toFloat()))
        }
        .offset { IntOffset(dragX.roundToInt(), dragY.roundToInt()) }
        .zIndex(if (dragging) 3f else 0f)
        .height(100.dp)
        .background(Brush.linearGradient(listOf(
            Color(0xFFD5FFFF).copy(alpha = if (editing) .28f else .34f),
            Color(0xFF4C94A9).copy(alpha = .31f),
            Color(0xFF143C55).copy(alpha = .45f))), shape)
        .border(1.dp, Color.White.copy(alpha = if (editing) .59f else .35f), shape)
        .clickable(onClick = onClick)
        .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.SpaceBetween) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            WaterAdaptiveText(tile.kind.title, scene, viewport, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            if (editing) {
                Box(Modifier.size(42.dp).then(dragHandler)
                    .semantics { contentDescription = "Drag ${tile.kind.title} shortcut" },
                    contentAlignment = Alignment.Center) {
                    WaterAdaptiveText("⠿", scene, viewport, fontWeight = FontWeight.Bold,
                        fontSize = 27.sp, modifier = Modifier.padding(bottom = 2.dp))
                }
            } else WaterAdaptiveText("+", scene, viewport, fontWeight = FontWeight.Bold, fontSize = 26.sp)
        }
        WaterAdaptiveText("${tile.amountMl} mL", scene, viewport,
            fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
