package com.kmab.gulp.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DrinkIconPickerTest {
    @Test fun thePaletteHasTwentyUniqueOptions() {
        assertEquals(23, DrinkTypeCatalog.iconIds.size)
        assertEquals(20, DrinkTypeCatalog.selectableIconIds.size)
        assertEquals(20, DrinkTypeCatalog.selectableIconIds.toSet().size)
        assertTrue(DrinkTypeCatalog.selectableIconIds.all { it in DrinkTypeCatalog.iconIds })
    }

    @Test fun duplicateShapesAreMappedWithoutChangingSavedIds() {
        assertEquals("tea", DrinkTypeCatalog.pickerIconId("leaf"))
        assertEquals("cup", DrinkTypeCatalog.pickerIconId("soda"))
        assertEquals("juice", DrinkTypeCatalog.pickerIconId("lemon"))
        assertEquals("tea", DrinkTypeCatalog.pickerIconId("tea"))
        val saved = DrinkType("custom_17", "Custom drink", "leaf", 250)
        assertEquals("custom_17", saved.id)
        assertEquals("leaf", saved.iconId)
        DrinkTypeCatalog.iconIds.forEach { DrinkTypeCatalog.checked("Drink", it, 250) }
    }
}
