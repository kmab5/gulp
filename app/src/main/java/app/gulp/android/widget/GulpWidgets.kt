package app.gulp.android.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.width
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import app.gulp.android.core.data.GulpRepository
import app.gulp.android.core.domain.DrinkKind
import app.gulp.android.core.domain.DrinkRules
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import android.os.SystemClock

/** Widgets are lightweight RemoteViews, never active fluid simulations. */
private val dark = Color(0xFF091B34)
private val aqua = Color(0xFF80E1EB)
private val pale = Color(0xFFF4F9FF)

private suspend fun todayTotal(context: Context): Int {
    val records = GulpRepository.create(context).records.first()
    return DrinkRules.loggedForDay(records, LocalDate.now(), ZoneId.systemDefault())
}

class GulpQuickWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val total = todayTotal(context)
        provideContent { QuickContent(total) }
    }
}

class GulpSummaryWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val total = todayTotal(context)
        provideContent { SummaryContent(total) }
    }
}

class GulpQuickWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GulpQuickWidget()
}

class GulpSummaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GulpSummaryWidget()
}

@Composable
private fun Brand() {
    Text("gulp", style = TextStyle(color = ColorProvider(pale), fontWeight = FontWeight.Bold, fontSize = 22.sp))
}

@Composable
private fun QuickContent(total: Int) {
    Column(modifier = GlanceModifier.fillMaxSize().background(dark).cornerRadius(22.dp).padding(14.dp)) {
        Brand()
        Spacer(GlanceModifier.height(5.dp))
        Text("${total} mL logged today", style = TextStyle(color = ColorProvider(aqua), fontSize = 12.sp))
        Spacer(GlanceModifier.height(6.dp))
        Box(GlanceModifier.fillMaxWidth().height(2.dp).background(aqua)) {}
        Spacer(GlanceModifier.height(9.dp))
        WidgetAddButton(250, "+ 250 mL")
    }
}

@Composable
private fun SummaryContent(total: Int) {
    Column(modifier = GlanceModifier.fillMaxSize().background(dark).cornerRadius(22.dp).padding(14.dp)) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Brand()
            Spacer(GlanceModifier.defaultWeight())
            Text("Today", style = TextStyle(color = ColorProvider(aqua), fontSize = 12.sp))
        }
        Spacer(GlanceModifier.height(6.dp))
        Text("$total mL", style = TextStyle(color = ColorProvider(pale), fontSize = 27.sp,
            fontWeight = FontWeight.Bold))
        Text("Recorded drinks", style = TextStyle(color = ColorProvider(aqua), fontSize = 12.sp))
        Spacer(GlanceModifier.height(8.dp))
        Row {
            WidgetAddButton(250, "+ 250 mL")
            Spacer(GlanceModifier.width(8.dp))
            WidgetAddButton(500, "+ 500 mL")
        }
    }
}

@Composable
private fun WidgetAddButton(amount: Int, label: String) {
    Box(modifier = GlanceModifier.background(aqua).cornerRadius(12.dp)
        .clickable(actionRunCallback<GulpWidgetLogAction>(actionParametersOf(GulpWidgetLogAction.AMOUNT to amount)))
        .padding(horizontal = 13.dp, vertical = 8.dp)) {
        Text(label, style = TextStyle(color = ColorProvider(dark), fontSize = 14.sp, fontWeight = FontWeight.Bold))
    }
}

/** Both widget designs use the same Room writer as Home and the reminder action. */
class GulpWidgetLogAction : ActionCallback {
    companion object { val AMOUNT = ActionParameters.Key<Int>("gulp_widget_amount_ml") }

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val amount = parameters[AMOUNT] ?: return
        if (amount != 250 && amount != 500) return
        WidgetTapGate.record(context, amount)
        // Repository.afterWrite refreshes all placed widgets, including this instance.
    }
}

/** Guards accidental sub-second repeated widget taps without preventing later logs. */
private object WidgetTapGate {
    private val mutex = Mutex()
    private var lastAmount: Int = 0
    private var lastAt: Long = -10_000L
    suspend fun record(context: Context, amount: Int) = mutex.withLock {
        val now = SystemClock.elapsedRealtime()
        if (lastAmount == amount && now - lastAt in 0 until 700L) return@withLock
        lastAmount = amount
        lastAt = now
        GulpRepository.create(context).add(DrinkKind.WATER, amount)
    }
}

object GulpWidgetRefresh {
    suspend fun refresh(context: Context) {
        // There may be no widgets installed. updateAll is harmless in that case.
        GulpQuickWidget().updateAll(context)
        GulpSummaryWidget().updateAll(context)
    }
}

/** Date/time changes refresh the 'today' boundary even when nobody opens the app. */
class GulpWidgetDateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in setOf(Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_MY_PACKAGE_REPLACED)) return
        val pending = goAsync()
        val app = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try { GulpWidgetRefresh.refresh(app) }
            catch (e: Exception) { Log.w("gulp-widgets", "Date refresh failed", e) }
            finally { pending.finish() }
        }
    }
}
