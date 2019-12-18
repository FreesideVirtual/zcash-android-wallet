package cash.z.ecc.android.integration

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IntegrationTest {

    private lateinit var appContext: Context

    @Before
    fun start() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
    }
}