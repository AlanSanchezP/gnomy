package io.github.alansanchezp.gnomy.category;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static io.github.alansanchezp.gnomy.data.MockRepositoryBuilder.initMockRepository;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.data.category.CategoryRepository;
import io.github.alansanchezp.gnomy.ui.category.CategoriesActivity;
import io.reactivex.Single;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class CategoriesActivityInstrumentedTest {
    @Rule
    public final ActivityScenarioRule<CategoriesActivity> activityRule =
            new ActivityScenarioRule<>(CategoriesActivity.class);

    // Needed so that ViewModel instance doesn't crash
    @BeforeClass
    public static void init_mocks() {
        CategoryRepository mockCategoryRepository = initMockRepository(CategoryRepository.class);
        when(mockCategoryRepository.getByStrictCategory(anyInt()))
                .thenReturn(new MutableLiveData<>());
    }

    @Test
    public void opens_new_category_activity() {
        onView(withId(R.id.categories_fab))
            .perform(click());

        onView(withId(R.id.custom_appbar))
                .check(matches(
                        hasDescendant(withText(R.string.category_new))));
    }
}
