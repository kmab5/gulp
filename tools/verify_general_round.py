#!/usr/bin/env python3
"""General-round source guardrails. Native Android build and device testing still required."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
def source(path): return (root / path).read_text(encoding="utf-8")
def require(ok, message):
    assert ok, message
base = "app/src/main/java/com/kmab/gulp/"
glass = source(base + "core/design/GlassEffects.kt")
main = source(base + "MainActivity.kt")
repo = source(base + "core/data/GulpRepository.kt")
db = source(base + "core/data/GulpDatabase.kt")
require("CompositingStrategy.Offscreen" in glass, "offscreen fade composition")
require("canScrollBack()" in glass and "canScrollForward()" in glass, "conditional scroll sides")
require("BlendMode.DstIn" in glass and "blurLayer.renderEffect" not in glass, "feathered content edges")
require("PointerEventPass.Initial" in main and "scene.pushFromSwipe(" in main, "full-screen swipe coordination")
require("DrinkTypeCatalog" in main and "DrinkTypeEditor(" in main, "catalog UI")
require(".addMigrations(MIGRATION_1_2)" in repo, "Room upgrade registered")
require("@Database(entities = [DrinkEntity::class, DrinkTypeEntity::class], version = 2" in db, "Room schema")
require("val drinkTypes: Flow<List<DrinkType>>" in repo, "observable catalog")
require("byId[builtin.id] ?: builtin" in repo, "built-in edits preserved")
require("onWidgetPreset" in source(base + "feature/settings/SettingsScreen.kt"), "widget selection")
require("GulpWaterText as Text" in source(base + "feature/review/ReviewScreen.kt"), "water-aware dashboard text")
require("GulpSwipe.nextIndex" in main, "swipe navigation remains wired")
require("MIN_VISUAL_FILL = 0f" in source(base + "core/graphics/WaterMath.kt"), "water may recede beneath navigation")
print("PASS: shared scroll, swipe, adaptive text, drink catalog and scope guards")
