package com.kmab.gulp.core.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kmab.gulp.core.domain.DrinkKind
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GulpRepositoryInstrumentedTest {
    private lateinit var db: GulpDatabase
    private lateinit var repo: GulpRepository

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext, GulpDatabase::class.java).build()
        repo = GulpRepository(db.drinks())
    }
    @After fun shutdown() = db.close()

    @Test fun createEditDeleteUndoPreservesPreviousRecord() = runBlocking {
        val insert = repo.add(DrinkKind.WATER, 250, 1729411200000L)
        var records = repo.records.first { it.size == 1 }
        assertEquals(250, records.single().amountMl)
        val edit = repo.edit(records.single().id, DrinkKind.TEA, 360, records.single().timeMillis)
        assertEquals(360, repo.records.first { it.singleOrNull()?.amountMl == 360 }.single().amountMl)
        assertTrue(repo.undo(edit!!))
        records = repo.records.first { it.singleOrNull()?.amountMl == 250 }
        val deletion = repo.delete(records.single().id)
        assertEquals(0, repo.records.first { it.isEmpty() }.size)
        assertTrue(repo.undo(deletion!!))
        assertEquals(250, repo.records.first { it.size == 1 }.single().amountMl)
    }

    @Test fun exportDeleteAndKeepSettingsSeparate() = runBlocking {
        repo.add(DrinkKind.WATER, 300, 1729411200000L)
        repo.add(DrinkKind.COFFEE, 200, 1729414800000L)
        val csv = repo.exportCsv()
        assertEquals(3, csv.trim().lines().size)
        assertTrue(csv.contains("COFFEE,200"))
        assertEquals(2, repo.deleteAll())
        assertTrue(repo.records.first().isEmpty())
        assertEquals(0, repo.deleteAll())
    }

    @Test fun undoAdditionAndDontReuseTicket() = runBlocking {
        val added = repo.add(DrinkKind.COFFEE, 200)
        assertTrue(repo.undo(added))
        assertTrue(repo.records.first { it.isEmpty() }.isEmpty())
        assertTrue(!repo.undo(added))
    }
}
