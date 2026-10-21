package app.gulp.android.feature.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.gulp.android.core.design.GulpPalette
import app.gulp.android.core.design.PrimaryAction
import app.gulp.android.core.domain.DrinkKind
import app.gulp.android.core.domain.HomeTile
import app.gulp.android.core.domain.HomeTileRules
import app.gulp.android.core.graphics.WaterAdaptiveText
import app.gulp.android.core.graphics.WaterScene
import java.util.UUID
import kotlin.math.roundToInt

/** Home's shortcuts persist in DataStore. Drag is only enabled in Edit mode. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    scene: WaterScene,
    viewport: IntSize,
    todayMl: Int,
    nextReminderLabel: String,
    editRequest: Int,
    onEditRequestHandled: () -> Unit,
    tiles: List<HomeTile>,
    onAddDrink: () -> Unit,
    onLogShortcut: (HomeTile) -> Unit,
    onSaveTiles: (List<HomeTile>) -> Unit
) {
    var editMode by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf(tiles) }
    var tileToEdit by remember { mutableStateOf<HomeTile?>(null) }
    var isAddingTile by remember { mutableStateOf(false) }
    val bounds = remember { mutableStateMapOf<String, Rect>() }
    LaunchedEffect(tiles) { if (!editMode) draft = tiles }
    LaunchedEffect(editRequest) {
        if (editRequest > 0) {
            draft = tiles
            editMode = true
            onEditRequestHandled()
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 23.dp, vertical = 24.dp)) {
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    WaterAdaptiveText("NEXT REMINDER", scene, viewport,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp, fontSize = 10.sp)
                    WaterAdaptiveText(nextReminderLabel, scene, viewport, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                WaterAdaptiveText("gulp", scene, viewport, fontWeight = FontWeight.Bold, fontSize = 23.sp)
            }
            Spacer(Modifier.height(5.dp))
            PrimaryAction("Add a drink", onClick = onAddDrink)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                WaterAdaptiveText("Quick add", scene, viewport, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = {
                    if (editMode) onSaveTiles(draft)
                    else draft = tiles
                    editMode = !editMode
                }) {
                    Text(if (editMode) "Done" else "Edit Home", color = GulpPalette.Glacier,
                        fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            FlowRow(Modifier.fillMaxWidth().animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val visible = if (editMode) draft else tiles
                visible.forEach { tile -> key(tile.id) {
                    HomeShortcutTile(tile = tile, editing = editMode,
                        modifier = Modifier.fillMaxWidth(if (tile.wide) 1f else 0.48f),
                        bounds = bounds,
                        onClick = { if (editMode) tileToEdit = tile else onLogShortcut(tile) },
                        onMove = { id, destination ->
                            draft = HomeTileRules.reorder(draft, id, destination)
                            onSaveTiles(draft)
                        })
                } }
                if (editMode && draft.size < HomeTileRules.MAX_TILES) {
                    Box(Modifier.fillMaxWidth(.48f).height(94.dp)
                        .background(GulpPalette.DeepBlue.copy(alpha = .54f), RoundedCornerShape(19.dp))
                        .clickable { isAddingTile = true }.padding(18.dp), contentAlignment = Alignment.Center) {
                        Text("+ Add tile", color = GulpPalette.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (editMode) WaterAdaptiveText("Hold a tile to move it. Tap to edit its amount or size.",
                scene, viewport, fontSize = 11.sp)
        }
        Column(Modifier.fillMaxWidth().padding(top = 9.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)) {
            WaterAdaptiveText("Today", scene, viewport, fontWeight = FontWeight.Medium, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                WaterAdaptiveText(todayMl.toString(), scene, viewport, fontWeight = FontWeight.ExtraBold,
                    fontSize = 43.sp, lineHeight = 46.sp)
                WaterAdaptiveText("mL recorded", scene, viewport, fontWeight = FontWeight.Bold,
                    fontSize = 13.sp, modifier = Modifier.padding(bottom = 7.dp))
            }
        }
    }
    if (tileToEdit != null || isAddingTile) {
        val current = tileToEdit
        TileEditorSheet(tile = current,
            onDismiss = { tileToEdit = null; isAddingTile = false },
            onSave = { kind, ml, wide ->
                draft = if (current == null) {
                    (draft + HomeTile(UUID.randomUUID().toString().substring(0, 8), kind, ml, wide))
                        .take(HomeTileRules.MAX_TILES)
                } else draft.map { if (it.id == current.id) it.copy(kind = kind, amountMl = ml, wide = wide) else it }
                onSaveTiles(draft)
                tileToEdit = null; isAddingTile = false
            },
            onRemove = if (current != null && draft.size > 1) ({
                draft = draft.filterNot { it.id == current.id }
                onSaveTiles(draft)
                tileToEdit = null
            }) else null)
    }
}

@Composable
private fun HomeShortcutTile(
    tile: HomeTile,
    editing: Boolean,
    modifier: Modifier,
    bounds: MutableMap<String, Rect>,
    onClick: () -> Unit,
    onMove: (String, String) -> Unit
) {
    var dragX by remember(tile.id) { mutableFloatStateOf(0f) }
    var dragY by remember(tile.id) { mutableFloatStateOf(0f) }
    var startRect by remember(tile.id) { mutableStateOf(Rect.Zero) }
    val positions = bounds
    val gestureModifier = if (editing) Modifier.pointerInput(tile.id, editing) {
        detectDragGesturesAfterLongPress(
            onDragStart = { startRect = positions[tile.id] ?: Rect.Zero },
            onDragEnd = {
                val target = Offset(startRect.center.x + dragX, startRect.center.y + dragY)
                if (dragX * dragX + dragY * dragY > 25f * 25f) {
                    val destination = positions.filterKeys { it != tile.id }.minByOrNull { (_, rect) ->
                        val dx = rect.center.x - target.x
                        val dy = rect.center.y - target.y
                        dx * dx + dy * dy
                    }?.key
                    if (destination != null) onMove(tile.id, destination)
                }
                dragX = 0f; dragY = 0f
            },
            onDragCancel = { dragX = 0f; dragY = 0f },
            onDrag = { change, amount ->
                change.consume()
                dragX += amount.x; dragY += amount.y
            }
        )
    } else Modifier
    Column(modifier
        .onGloballyPositioned { positions[tile.id] = it.boundsInRoot() }
        .offset { IntOffset(dragX.roundToInt(), dragY.roundToInt()) }
        .zIndex(if (dragX != 0f || dragY != 0f) 2f else 0f)
        .height(94.dp)
        .background(GulpPalette.Glacier.copy(alpha = if (editing) .44f else .35f), RoundedCornerShape(18.dp))
        .clickable(onClick = onClick)
        .padding(15.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(tile.kind.title, color = GulpPalette.Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (editing) {
                Box(Modifier.size(44.dp).then(gestureModifier)
                    .semantics { contentDescription = "Drag ${tile.kind.title} shortcut" },
                    contentAlignment = Alignment.Center) {
                    Text("⠿", color = GulpPalette.Ink, fontSize = 23.sp)
                }
            } else Text("+", color = GulpPalette.Ink, fontSize = 24.sp)
        }
        Text("${tile.amountMl} mL", color = GulpPalette.Ink.copy(alpha = .85f), fontSize = 13.sp)
    }
}
