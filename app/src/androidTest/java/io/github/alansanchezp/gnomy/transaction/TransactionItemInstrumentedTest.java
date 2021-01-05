package io.github.alansanchezp.gnomy.transaction;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.ViewScenarioRule;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.database.transaction.TransactionDisplayData;
import io.github.alansanchezp.gnomy.databinding.LayoutTransactionCardBinding;
import io.github.alansanchezp.gnomy.ui.transaction.TransactionItem;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasTextColor;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static io.github.alansanchezp.gnomy.EspressoTestUtil.assertThrows;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class TransactionItemInstrumentedTest {
    static final TransactionDisplayData testItem = mock(TransactionDisplayData.class);
    @Rule
    public final ViewScenarioRule viewRule = new ViewScenarioRule(
            LayoutTransactionCardBinding.class);

    @BeforeClass
    public static void init_test_account() {
        testItem.transaction = mock(MoneyTransaction.class);
        testItem.accountName = "Test account name";
        testItem.transferDestinationAccountName = "Test destination name";
        testItem.categoryColor = 0XFFFF0000;
        testItem.categoryName = "Test category name";
        testItem.categoryResourceName = "ic_baseline_arrow_back_24";
    }

    @Test
    public void transaction_data_is_displayed_in_card() {
        LayoutTransactionCardBinding viewBinding = Objects.requireNonNull(viewRule.retrieveViewBinding());

        when(testItem.transaction.getType()).thenReturn(MoneyTransaction.INCOME);
        when(testItem.transaction.getConcept()).thenReturn("Test concept");
        when(testItem.transaction.isConfirmed()).thenReturn(false);
        final TransactionItem item = new TransactionItem(testItem, true);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                item.bind(viewBinding, 1));

        onView(withId(R.id.transaction_card_concept))
                .check(matches(withText(testItem.transaction.getConcept())));
        onView(withId(R.id.transaction_card_alert_icon))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.transaction_card_account))
                .check(matches(withText(testItem.accountName)));


        final TransactionItem limitedDataTransaction = new TransactionItem(testItem, false);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                limitedDataTransaction.bind(viewBinding, 1));

        onView(withId(R.id.transaction_card_concept))
                .check(matches(withText(testItem.transaction.getConcept())));
        onView(withId(R.id.transaction_card_alert_icon))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.transaction_card_account))
                .check(matches(withText("")));


        when(testItem.transaction.getType()).thenReturn(MoneyTransaction.TRANSFER);
        when(testItem.transaction.isConfirmed()).thenReturn(true);
        final TransactionItem transfer = new TransactionItem(testItem, true);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                transfer.bind(viewBinding, 1));

        onView(withId(R.id.transaction_card_concept))
                .check(matches(withText(testItem.transaction.getConcept())));
        onView(withId(R.id.transaction_card_alert_icon))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.transaction_card_account))
                .check(matches(withText(testItem.accountName + " \u203A " + testItem.transferDestinationAccountName)));

        final TransactionItem limitedDataTransfer = new TransactionItem(testItem, false);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                limitedDataTransfer.bind(viewBinding, 1));

        onView(withId(R.id.transaction_card_concept))
                .check(matches(withText(testItem.transaction.getConcept())));
        onView(withId(R.id.transaction_card_alert_icon))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.transaction_card_account))
                .check(matches(withText(" \u203A " + testItem.transferDestinationAccountName)));
    }

    @Test
    public void category_icon_is_correct() {
        LayoutTransactionCardBinding viewBinding = Objects.requireNonNull(viewRule.retrieveViewBinding());
        final TransactionItem item = new TransactionItem(testItem, true);
        testItem.categoryResourceName = ""; // Faulty resource
        assertThrows(RuntimeException.class, () -> InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                item.bind(viewBinding, 1)));

        testItem.categoryResourceName = "ic_baseline_arrow_back_24";
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                item.bind(viewBinding, 1));
        onView(withId(R.id.transaction_card_icon))
                .check(matches(withTagValue(equalTo(R.drawable.ic_baseline_arrow_back_24))));

        testItem.categoryResourceName = "ic_close_black_24dp";
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                item.bind(viewBinding, 1));
        onView(withId(R.id.transaction_card_icon))
                .check(matches(withTagValue(equalTo(R.drawable.ic_close_black_24dp))));

        testItem.categoryResourceName = "ic_calculate_24";
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                item.bind(viewBinding, 1));
        onView(withId(R.id.transaction_card_icon))
                .check(matches(withTagValue(equalTo(R.drawable.ic_calculate_24))));

        when(testItem.transaction.getType()).thenReturn(MoneyTransaction.TRANSFER);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                item.bind(viewBinding, 1));
        onView(withId(R.id.transaction_card_icon))
                .check(matches(withTagValue(equalTo(R.drawable.ic_compare_arrows_black_24dp))));
    }

    @Test
    public void dynamic_color_of_amount_text_view() {
        LayoutTransactionCardBinding viewBinding = Objects.requireNonNull(viewRule.retrieveViewBinding());
        final TransactionItem item = new TransactionItem(testItem, true);

        when(testItem.transaction.getCalculatedValue()).thenReturn(BigDecimalUtil.fromString("10"));
        when(testItem.transaction.getType()).thenReturn(MoneyTransaction.INCOME);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                item.bind(viewBinding, 1));
        onView(withId(R.id.transaction_card_amount))
                .check(matches(hasTextColor(R.color.colorIncomesDark)));

        when(testItem.transaction.getType()).thenReturn(MoneyTransaction.TRANSFER);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                item.bind(viewBinding, 1));
        onView(withId(R.id.transaction_card_amount))
                .check(matches(hasTextColor(R.color.colorTextSecondary)));

        when(testItem.transaction.getType()).thenReturn(MoneyTransaction.EXPENSE);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                item.bind(viewBinding, 1));
        onView(withId(R.id.transaction_card_amount))
                .check(matches(hasTextColor(R.color.colorExpensesDark)));
    }
}
