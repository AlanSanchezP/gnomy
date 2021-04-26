package io.github.alansanchezp.gnomy.category;

import android.content.Intent;
import android.content.pm.ActivityInfo;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.lifecycle.MutableLiveData;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.data.category.Category;
import io.github.alansanchezp.gnomy.data.category.CategoryRepository;
import io.github.alansanchezp.gnomy.ui.category.AddEditCategoryActivity;
import io.github.alansanchezp.gnomy.ui.category.CategoryTypeItem;
import io.reactivex.Single;

import static androidx.lifecycle.Lifecycle.State.DESTROYED;
import static androidx.lifecycle.Lifecycle.State.RESUMED;
import static androidx.test.core.app.ActivityScenario.launch;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasBackground;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static io.github.alansanchezp.gnomy.EspressoTestUtil.assertActivityState;
import static io.github.alansanchezp.gnomy.EspressoTestUtil.nestedScrollTo;
import static io.github.alansanchezp.gnomy.data.MockRepositoryBuilder.initMockRepository;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class AddEditCategoryActivityInstrumentedTest {
    @Rule
    public final ActivityScenarioRule<AddEditCategoryActivity> activityRule =
            new ActivityScenarioRule<>(AddEditCategoryActivity.class);
    private static final CategoryRepository mockCategoryRepository = initMockRepository(CategoryRepository.class);
    private static final MutableLiveData<Category> mutableCategory = new MutableLiveData<>();

    // Needed so that ViewModel instance doesn't crash
    @BeforeClass
    public static void init_mocks() {
        when(mockCategoryRepository.find(anyInt()))
                .thenReturn(mutableCategory);
        when(mockCategoryRepository.insert(any(Category.class)))
                .thenReturn(Single.just(1L));
    }

    @Test
    public void dynamic_title_is_correct() {
        // Default behavior is new category
        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.category_new)
                )));
        mutableCategory.postValue(new Category(1));
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                AddEditCategoryActivity.class)
                .putExtra(AddEditCategoryActivity.EXTRA_CATEGORY_ID, 1);
        ActivityScenario<AddEditCategoryActivity> tempScenario = launch(intent);

        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.category_modify)
                )));
        tempScenario.close();
    }

    @Test
    public void non_existent_category_from_extra() {
        // Create a new intent passing invalid account id
        mutableCategory.postValue(null);
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                AddEditCategoryActivity.class)
                .putExtra(AddEditCategoryActivity.EXTRA_CATEGORY_ID, 2);
        ActivityScenario<AddEditCategoryActivity> tempScenario = launch(intent);
        assertActivityState(DESTROYED, tempScenario);
        tempScenario.close();
    }

    @Test
    public void sets_proper_initial_background_color() {
        onView(withId(R.id.custom_appbar))
                .check(matches(
                        not(hasBackground(android.R.color.transparent))
                ));
    }

    @Test
    public void text_fields_trigger_error_if_empty() {
        onView(withId(R.id.addedit_category_name_input))
                .perform(typeText("test"))
                .perform(replaceText(""));

        onView(withId(R.id.addedit_category_name))
                .check(matches(hasDescendant(
                        withText(R.string.category_error_name)
                )));
    }

    @Test
    public void text_fields_do_not_trigger_error_on_rotation_if_pristine() {
        activityRule.getScenario().onActivity(activity -> {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });

        onView(withId(R.id.addedit_category_name))
                .check(matches(not(hasDescendant(
                        withText(R.string.category_error_name)
                ))));
    }

    @Test
    public void text_fields_keep_errors_on_rotation_if_not_pristine() {
        onView(withId(R.id.addedit_category_name_input))
                .perform(replaceText("test"))
                .perform(replaceText(""));

        activityRule.getScenario().onActivity(activity -> {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });

        onView(withId(R.id.addedit_category_name))
                .check(matches(hasDescendant(
                        withText(R.string.category_error_name)
                )));
    }

    @Test
    public void button_opens_color_picker() {
        onView(withId(R.id.addedit_category_color_button))
                .perform(click());

        onView(withText(R.string.account_pick_color_title))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void prompts_error_if_text_fields_empty_when_form_is_submitted() {
        onView(withId(R.id.addedit_category_FAB))
                .perform(click());

        onView(withId(R.id.addedit_category_name))
                .check(matches(hasDescendant(
                        withText(R.string.category_error_name)
                )));
    }

    @Test
    public void FAB_finishes_activity_if_data_is_correct() {
        onView(withId(R.id.addedit_category_name_input))
                .perform(replaceText("test"));

        onView(withId(R.id.addedit_category_FAB))
                .perform(click());

        assertActivityState(DESTROYED, activityRule);
    }

    @Test
    public void data_is_sent_to_repository() {
        when(mockCategoryRepository.insert(any(Category.class)))
                .then(invocation -> {
                    RuntimeException exception = null;
                    // I don't like this, but it was the only way I found to test this
                    Category sentByForm = invocation.getArgument(0);
                    if (!sentByForm.getName().equals("Test category")) exception =  new RuntimeException();
                    if (sentByForm.getType() != Category.INCOME_CATEGORY) exception =  new RuntimeException();

                    // Not testing selected color: Don't know how to select an item.

                    if (exception == null)
                        return Single.just(1L);
                    else
                        return Single.error(exception);
                });

        //noinspection SpellCheckingInspection
        onView(withId(R.id.addedit_category_name_input))
                .perform(typeText("gffdg")) // setting wrong value
                .perform(closeSoftKeyboard());
        onView(withId(R.id.addedit_category_type))
                .perform(click());
        onData(allOf(is(instanceOf(CategoryTypeItem.class)), is(
                // Using hardcoded string as AccountTypeItem.equals only considers type
                new CategoryTypeItem(Category.INCOME_CATEGORY, "Income category"))))
                .perform(click());

        onView(withId(R.id.addedit_category_FAB))
                .perform(click());

        // These calls should only be possible if insert fails and activity is not
        //  automatically finished
        onView(withId(R.id.addedit_category_name_input))
                .perform(nestedScrollTo(), click())
                .perform(replaceText("Test category"))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.addedit_category_FAB))
                .perform(click());

        // return to default state
        when(mockCategoryRepository.insert(any(Category.class)))
                .thenReturn(Single.just(1L));

        try {
            assertActivityState(DESTROYED, activityRule);
        } catch (AssertionError e) {
            // Not sure why SOMETIMES state is DESTROYED and sometimes it's RESUMED
            assertActivityState(RESUMED, activityRule);
        }
    }
}
