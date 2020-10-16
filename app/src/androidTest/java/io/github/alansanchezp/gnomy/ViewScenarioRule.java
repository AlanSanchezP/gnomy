package io.github.alansanchezp.gnomy;

import android.content.Intent;

import org.junit.rules.ExternalResource;

import androidx.annotation.Nullable;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import io.github.alansanchezp.gnomy.dummy.DummyActivity;

import static androidx.test.internal.util.Checks.checkNotNull;

/**
 * ViewScenarioRule launches an empty Activity (DummyActivity) before the test starts and closes after the test.
 * This empty activity is inflated with a dynamic layout specified by its resource id.
 * The code of this class is based directly on ActivityScenarioRule and should behave the same way.
 *
 * <p>You can access the  ActivityScenario interface via {@link #getScenario()} method. You may finish your
 * activity manually in your test, it will not cause any problems and this rule does nothing after
 * the test in such cases.
 *
 * <pre>{@code
 * Example:
 *  }{@literal @Rule}{@code
 *   ViewScenarioRule rule = new ViewScenarioRule(R.layout.my_view_layout);
 *
 *  }{@literal @Test}{@code
 *   public void myTest() {
 *     ViewScenarioRule scenario = rule.getScenario();
 *     // Your test code goes here.
 *   }
 * }</pre>
 */
public class ViewScenarioRule extends ExternalResource {
    interface Supplier<T> {
        T get();
    }
    private final Supplier<ActivityScenario<DummyActivity>> scenarioSupplier;
    @Nullable
    private ActivityScenario<DummyActivity> scenario;
    public ViewScenarioRule(int resourceId) {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                DummyActivity.class)
                .putExtra(
                        DummyActivity.EXTRA_LAYOUT_TAG,
                        resourceId);

        scenarioSupplier = () -> ActivityScenario.launch(checkNotNull(intent));
    }

    @Override
    protected void before() throws Throwable {
        scenario = scenarioSupplier.get();
    }

    @Override
    protected void after() {
        scenario.close();
    }

    public ActivityScenario<DummyActivity> getScenario() {
        return checkNotNull(scenario);
    }
}
