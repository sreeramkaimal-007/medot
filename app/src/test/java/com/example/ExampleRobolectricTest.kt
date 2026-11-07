package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DoseStatus
import com.example.data.model.Schedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("medot", appName)
    }

    @Test
    fun `test schedule AM and PM formatting`() {
        val morningSchedule = Schedule(
            medicationId = 1L,
            hour = 8,
            minute = 30
        )
        assertTrue(morningSchedule.isAM)
        assertEquals("8:30 AM", morningSchedule.formattedTime)

        val eveningSchedule = Schedule(
            medicationId = 1L,
            hour = 20,
            minute = 0
        )
        assertTrue(!eveningSchedule.isAM)
        assertEquals("8:00 PM", eveningSchedule.formattedTime)
    }

    @Test
    fun `test dose status values`() {
        assertEquals("Taken", DoseStatus.TAKEN.displayName)
        assertEquals("Due Now", DoseStatus.DUE_NOW.displayName)
        assertEquals("Upcoming", DoseStatus.UPCOMING.displayName)
    }
}
