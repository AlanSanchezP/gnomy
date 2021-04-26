package io.github.alansanchezp.gnomy.category;

import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.ViewScenarioRule;
import io.github.alansanchezp.gnomy.data.category.Category;
import io.github.alansanchezp.gnomy.databinding.LayoutAccountCardBinding;
import io.github.alansanchezp.gnomy.databinding.LayoutCategoryCardBinding;
import io.github.alansanchezp.gnomy.ui.category.CategoryRecyclerViewAdapter;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static io.github.alansanchezp.gnomy.EspressoTestUtil.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CategoryRecyclerViewHolderInstrumentedTest {
    static final Category testCategory = mock(Category.class);

    @Rule
    public final ViewScenarioRule viewRule = new ViewScenarioRule(
            LayoutCategoryCardBinding.class);

    @BeforeClass
    public static void init_test_category() {
        when(testCategory.getName()).thenReturn("Test category");
        when(testCategory.getId()).thenReturn(1);
        when(testCategory.getDrawableResourceName()).thenReturn("ic_close_black_24dp");
    }

    @Test
    public void category_name_is_displayed_in_card() {
        CategoryRecyclerViewAdapter.ViewHolder holder =
                new CategoryRecyclerViewAdapter.ViewHolder(Objects.requireNonNull(viewRule.retrieveViewBinding()));

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                holder.setCategoryData(testCategory));

        onView(withId(R.id.category_card_name))
                .check(matches(withText(testCategory.getName())));
    }

    @Test
    public void button_visibility_is_dynamic() {
        CategoryRecyclerViewAdapter.ViewHolder holder =
                new CategoryRecyclerViewAdapter.ViewHolder(Objects.requireNonNull(viewRule.retrieveViewBinding()));

        when(testCategory.isDeletable()).thenReturn(true);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                holder.setCategoryData(testCategory));

        onView(withId(R.id.category_card_button))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        when(testCategory.isDeletable()).thenReturn(false);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                holder.setCategoryData(testCategory));

        onView(withId(R.id.category_card_button))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void category_icon_in_card_is_correct() {
        CategoryRecyclerViewAdapter.ViewHolder holder =
                new CategoryRecyclerViewAdapter.ViewHolder(Objects.requireNonNull(viewRule.retrieveViewBinding()));
        when(testCategory.getDrawableResourceName()).thenReturn(""); // Faulty resource
        assertThrows(RuntimeException.class, () -> InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                holder.setCategoryData(testCategory)));

        when(testCategory.getDrawableResourceName()).thenReturn("ic_baseline_arrow_back_24");
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                holder.setCategoryData(testCategory));
        onView(withId(R.id.category_card_icon))
                .check(matches(withTagValue(Matchers.equalTo(R.drawable.ic_baseline_arrow_back_24))));

        when(testCategory.getDrawableResourceName()).thenReturn("ic_close_black_24dp");
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                holder.setCategoryData(testCategory));
        onView(withId(R.id.category_card_icon))
                .check(matches(withTagValue(Matchers.equalTo(R.drawable.ic_close_black_24dp))));

        when(testCategory.getDrawableResourceName()).thenReturn("ic_calculate_24");
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                holder.setCategoryData(testCategory));
        onView(withId(R.id.category_card_icon))
                .check(matches(withTagValue(Matchers.equalTo(R.drawable.ic_calculate_24))));
    }
}
