package io.github.alansanchezp.gnomy.transaction;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.ViewScenarioRule;
import io.github.alansanchezp.gnomy.databinding.LayoutTransactionGroupHeaderBinding;
import io.github.alansanchezp.gnomy.ui.transaction.TransactionGroupHeader;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasTextColor;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class TransactionGroupHeaderInstrumentedTest {
    @Rule
    public final ViewScenarioRule viewRule = new ViewScenarioRule(
            LayoutTransactionGroupHeaderBinding.class);

    @Test
    public void data_and_color_are_correct() {
        LayoutTransactionGroupHeaderBinding viewBinding = Objects.requireNonNull(viewRule.retrieveViewBinding());
        final TransactionGroupHeader positiveItem = new TransactionGroupHeader("TITLE", BigDecimalUtil.fromString("10"));
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
            positiveItem.bind(viewBinding, 1));

        onView(withId(R.id.transaction_group_header_text))
            .check(matches(withText("TITLE")));
        onView(withId(R.id.transaction_group_header_sum))
                .check(matches(hasTextColor(R.color.colorIncomesDark)));

        final TransactionGroupHeader neutralItem = new TransactionGroupHeader("TITLE 2", BigDecimalUtil.ZERO);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                neutralItem.bind(viewBinding, 1));

        onView(withId(R.id.transaction_group_header_text))
                .check(matches(withText("TITLE 2")));
        onView(withId(R.id.transaction_group_header_sum))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));

        final TransactionGroupHeader negativeItem = new TransactionGroupHeader("TITLE 3", BigDecimalUtil.fromString("-10"));
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                negativeItem.bind(viewBinding, 1));

        onView(withId(R.id.transaction_group_header_text))
                .check(matches(withText("TITLE 3")));
        onView(withId(R.id.transaction_group_header_sum))
                .check(matches(hasTextColor(R.color.colorExpensesDark)));

    }
}
