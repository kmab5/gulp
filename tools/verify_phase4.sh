#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
DIR="$(mktemp -d)"; trap 'rm -rf "$DIR"' EXIT
cat > "$DIR/Phase4Check.kt" <<'KT'
import app.gulp.android.core.domain.*
import java.time.*
fun main() {
 var checks=0
 fun ok(condition: Boolean, message:String="Assertion failed") { check(condition){message}; checks++ }
 val zones=listOf(ZoneId.of("Europe/Istanbul"),ZoneId.of("America/New_York"))
 val days=listOf(LocalDate.of(2026,10,20),LocalDate.of(2026,3,8),LocalDate.of(2026,11,1))
 for (zone in zones) for (day in days) for (hours in listOf(60,90,120,180,240)) {
  for (start in listOf(420,480,720,1320)) for (end in listOf(360,900,1320)) {
   val p=ReminderPlan(true,hours,start,end)
   if (!p.valid()) continue
   val after=day.atStartOfDay(zone).toInstant().toEpochMilli()
   val next=ReminderRules.nextOpportunity(after,zone,p)
   ok(next!=null,"Missing next slot")
   ok(next!!>after,"Non-future next slot")
   ok(next-after<=48*60*60_000L,"Alarm out of range")
  }
 }
 val zone=ZoneId.of("Europe/Istanbul")
 val day=LocalDate.of(2026,10,20)
 fun time(d:LocalDate,h:Int,m:Int=0) = d.atTime(h,m).atZone(zone).toInstant().toEpochMilli()
 val p=ReminderPlan(true,90,480,1320)
 ok(ReminderRules.nextAlarm(time(day,7,59),zone,p)==time(day,8))
 ok(ReminderRules.nextAlarm(time(day,8),zone,p)==time(day,9,30))
 ok(ReminderRules.nextAlarm(time(day,22),zone,p)==time(day.plusDays(1),8))
 ok(ReminderRules.nextAlarm(time(day,8),zone,p.copy(enabled=false))==null)
 ok(!ReminderRules.shouldNotify(time(day,9),time(day,9),zone,p,time(day,8,50)))
 ok(ReminderRules.shouldNotify(time(day,9),time(day,9),zone,p,time(day,8,0)))
 ok(!ReminderRules.shouldNotify(time(day,10),time(day,9),zone,p,null))
 val over=ReminderPlan(true,120,1320,360)
 ok(ReminderRules.nextOpportunity(time(day,23,59),zone,over)==time(day.plusDays(1),0))
 ok(ReminderRules.nextOpportunity(time(day,6,1),zone,over)==time(day,22))
 ok(!ReminderRules.shouldNotify(time(day,12),time(day,12),zone,over,null))
 ok(ReminderRules.nextAlarm(time(day,9),zone,p.copy(snoozeUntilMillis=time(day,9,15)))==time(day,9,15))
 val data=listOf(DrinkRecord(1,250,DrinkKind.WATER,time(day,11)),DrinkRecord(2,200,DrinkKind.COFFEE,time(day,16)),DrinkRecord(3,600,DrinkKind.WATER,time(day.minusDays(1),11)),DrinkRecord(4,600,DrinkKind.TEA,time(day.minusDays(7),12)))
 val r=ReviewRules.summarize(data,day,zone)
 ok(r.days.size==7)
 ok(r.totalMl==1050L)
 ok(r.averagePerDayMl==150L)
 ok(r.recordedDays==2)
 ok(r.entries==3)
 ok(r.previousWeekTotalMl==600L)
 ok(r.mostLoggedDrink==DrinkKind.WATER)
 val empty=ReviewRules.summarize(emptyList(),day,zone)
 ok(empty.entries==0 && empty.previousWeekTotalMl==null)
 ok(TrackingReference.sanitized(0)==0 && TrackingReference.sanitized(8000)==6000)
 println("PASS: $checks pure Kotlin reminder/date/DST/review/reference assertions")
}
KT
kotlinc app/src/main/java/app/gulp/android/core/domain/DrinkModels.kt \
  app/src/main/java/app/gulp/android/core/domain/ReminderRules.kt \
  app/src/main/java/app/gulp/android/core/domain/ReviewRules.kt \
  app/src/main/java/app/gulp/android/core/domain/TrackingReference.kt \
  "$DIR/Phase4Check.kt" -include-runtime -d "$DIR/phase4.jar"
java -jar "$DIR/phase4.jar"
