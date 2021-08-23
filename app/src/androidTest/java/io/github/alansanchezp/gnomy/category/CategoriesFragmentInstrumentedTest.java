package io.github.alansanchezp.gnomy.category;

import android.view.MenuItem;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.data.category.Category;
import io.github.alansanchezp.gnomy.data.category.CategoryRepository;
import io.github.alansanchezp.gnomy.ui.category.CategoriesFragment;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static io.github.alansanchezp.gnomy.data.MockRepositoryBuilder.initMockRepository;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class CategoriesFragmentInstrumentedTest {
    private static MenuItem mockMenuItem;

    @BeforeClass
    public static void init_mocks() {
        final CategoryRepository mockCategoryRepository = initMockRepository(CategoryRepository.class);
        when(mockCategoryRepository.getByStrictCategory(anyInt()))
                .thenReturn(new MutableLiveData<>());
        when(mockCategoryRepository.find(anyInt()))
                .thenReturn(new MutableLiveData<>());

        mockMenuItem = mock(MenuItem.class);
    }

    @Test
    public void interface_method_opens_category_modify() {
        FragmentScenario<CategoriesFragment> scenario = launchInContainer(CategoriesFragment.class,
                null, R.style.AppTheme);
        scenario.onFragment(fragment -> {
            Category category = new Category(1);
            fragment.onItemInteraction(category);
        });
        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.category_modify))
                ));
    }

    @Test
    public void interface_method_opens_delete_dialog() {
        FragmentScenario<CategoriesFragment> scenario = launchInContainer(CategoriesFragment.class,
                null, R.style.AppTheme);
        when(mockMenuItem.getItemId()).thenReturn(R.id.category_card_delete);
        scenario.onFragment(fragment ->
                fragment.onItemMenuItemInteraction(new Category(), mockMenuItem));
        onView(withText(R.string.category_card_delete_modal))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        onView(withText(R.string.category_card_delete_warning))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }
}
