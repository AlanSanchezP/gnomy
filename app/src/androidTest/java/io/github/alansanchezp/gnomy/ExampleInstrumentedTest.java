package io.github.alansanchezp.gnomy;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    // TODO: Remove this example test
    // TODO: Test MainActivity FAB
    // TODO: Test AccountsFragment
    // TODO: Test AddEditAccountActivity
    // TODO: Test AccountDetailsActivity
    // TODO: Test AccountHistoryActivity
    // TODO: Test RecyclerViewAdapters
    //  https://chelseatroy.com/2015/09/27/android-examples-a-test-driven-recyclerview/
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("io.github.alansanchezp.gnomy", appContext.getPackageName());
    }
}
