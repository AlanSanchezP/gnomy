package io.github.alansanchezp.gnomy;

import android.content.Intent;

import org.junit.rules.ExternalResource;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.viewbinding.ViewBinding;
import io.github.alansanchezp.gnomy.dummy.DummyActivity;

import static androidx.test.internal.util.Checks.checkNotNull;

/**
 * ViewScenarioRule launches an empty Activity (DummyActivity) before the test starts and closes after the test.
 * This empty activity is inflated using the desired View as the only child of a root layout.
 * See {@link DummyActivity} for more details.
 *
 * The code of this class is based directly on ActivityScenarioRule and should behave the same way.
 *
 * <p>You can access the  ActivityScenario interface via {@link #getScenario()} method. You may finish your
 * activity manually in your test, it will not cause any problems and this rule does nothing after
 * the test in such cases.
 *
 * <pre>{@code
 * Example:
 *  }{@literal @Rule}{@code
 *   ViewScenarioRule rule = new ViewScenarioRule(MyCustomView.class);
 *
 *  }{@literal @Test}{@code
 *   public void myTest() {
 *     ViewScenarioRule scenario = rule.getScenario();
 *     // Your test code goes here.
 *   }
 * }</pre>
 */
@SuppressWarnings("ALL")
public class ViewScenarioRule extends ExternalResource {
    /* START OF COPIED ELEMENTS FROM ActivityTestScenario */
    interface Supplier<T> {
        T get();
    }
    private final Supplier<ActivityScenario<DummyActivity>> scenarioSupplier;
    @Nullable
    private ActivityScenario<DummyActivity> scenario;
    /* END OF COPIED ELEMENTS FROM ActivityTestScenario */

    /**
     * Initializes the ActivityRule, inserting the desired View object
     * as the only child of an empty layout.
     *
     * @param classToUse    Class to use as reference. It can be either
     *                      a View subclass or a ViewBinding subclass.
     */
    public ViewScenarioRule(Class<?> classToHost) {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                DummyActivity.class)
                .putExtra(
                        DummyActivity.EXTRA_HOSTED_CLASS_TAG,
                        classToHost);

        scenarioSupplier = () -> ActivityScenario.launch(checkNotNull(intent));
    }

    /* START OF COPIED METHODS FROM ActivityTestScenario */

    @Override
    protected void before() throws Throwable {
        scenario = scenarioSupplier.get();
    }

    @Override
    protected void after() {
        Objects.requireNonNull(scenario).close();
    }

    public ActivityScenario<DummyActivity> getScenario() {
        return checkNotNull(scenario);
    }

    /* END OF COPIED METHODS FROM ActivityTestScenario */

    /**
     * Retrieves the hosted ViewBinding object on DummyActivity. Returns null
     * if the hosted class that was specified in the ViewScenarioRule constructor
     * is a View subclass.
     *
     * @param <B>   ViewBinding class to retrieve. In order to prevent any
     *              exceptions, this must match the Class reference
     *              passed to the constructor of the ViewScenarioRule instance.
     * @return
     */
    public @Nullable <B extends ViewBinding> B retrieveViewBinding() {
        final ArrayList<B> helper = new ArrayList<>(1);
        getScenario().onActivity(activity -> {
            helper.add(activity.getHostedViewBinding());
        });
        return helper.get(0);
    }
}
