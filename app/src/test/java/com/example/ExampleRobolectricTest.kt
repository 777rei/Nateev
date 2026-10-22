package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
    assertEquals("Nateev", appName)
  }

  @Test
  fun `verify presets list contains flagship and dslr profiles`() {
    val presets = com.example.domain.models.CameraPreset.getAllPresets()
    org.junit.Assert.assertTrue(presets.isNotEmpty())
    org.junit.Assert.assertTrue(presets.any { it.id == "bionic_13" })
    org.junit.Assert.assertTrue(presets.any { it.id == "redmi_matrix" })
    org.junit.Assert.assertTrue(presets.any { it.id == "dslr_pro_50" })
  }
}
