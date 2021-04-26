package io.github.alansanchezp.gnomy.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.platform.app.InstrumentationRegistry;
import io.github.alansanchezp.gnomy.data.category.Category;
import io.github.alansanchezp.gnomy.data.category.CategoryRepository;
import io.github.alansanchezp.gnomy.util.ColorUtil;

import static io.github.alansanchezp.gnomy.EspressoTestUtil.assertThrows;
import static io.github.alansanchezp.gnomy.LiveDataTestUtil.getOrAwaitValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class CategoryRepositoryTest {
    private CategoryRepository categoryRepository;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() {
        categoryRepository = new CategoryRepository(
                InstrumentationRegistry.getInstrumentation().getContext());
    }

    @After
    public void cleanDatabase() {
        GnomyDatabase.cleanUp();
    }

    @Test
    public void getSharedAndCategory_rejects_invalid_categories()  {
        assertThrows(GnomyIllegalQueryException.class, () ->
            categoryRepository.getSharedAndCategory(Category.BOTH_CATEGORY));
        assertThrows(GnomyIllegalQueryException.class, () ->
                categoryRepository.getSharedAndCategory(4));
        assertThrows(GnomyIllegalQueryException.class, () ->
                categoryRepository.getSharedAndCategory(-1));
        assertThrows(GnomyIllegalQueryException.class, () ->
                categoryRepository.getSharedAndCategory(0));
        assertThrows(GnomyIllegalQueryException.class, () ->
                categoryRepository.getSharedAndCategory(10));
        assertThrows(GnomyIllegalQueryException.class, () ->
                categoryRepository.getSharedAndCategory(15));
    }

    @Test
    public void user_inserted_rows_are_always_deletable() throws InterruptedException {
        /*  Only system-generated ones are non-deletable
            These are created during migrations and bypass repository
            So repository must always mark categories as deletable=true
        */

        TestCategoryClass testCategory = new TestCategoryClass();
        testCategory.setName("Test category");
        testCategory.setIconResName("ic_account_balance_24");
        testCategory.setType(Category.INCOME_CATEGORY);
        testCategory.setBackgroundColor(ColorUtil.getRandomColor());

        Long generatedId = categoryRepository.insert(testCategory).blockingGet();
        Category insertedCategory = getOrAwaitValue(categoryRepository.find((int) (long) generatedId));
        assertTrue(insertedCategory.isDeletable());

        // App MUST always have at least one predefined category
        Category systemCategory = getOrAwaitValue(categoryRepository.find(1));
        assertFalse(systemCategory.isDeletable());
    }

    @Test
    public void cannot_alter_deletable_status() throws InterruptedException {
        // App MUST always have at least one predefined category, which are undeletable
        Category systemCategory = getOrAwaitValue(categoryRepository.find(1));
        TestCategoryClass alteredCategory = new TestCategoryClass(systemCategory);
        alteredCategory.forceDeletable();

        assertThrows(GnomyIllegalQueryException.class,
                () -> categoryRepository.update(alteredCategory).blockingGet());
    }

    @Test
    public void cannot_alter_category_type() throws InterruptedException {
        // App MUST always have at least one predefined category, which are undeletable
        Category systemCategory = getOrAwaitValue(categoryRepository.find(1));
        int newType;
        if (systemCategory.getType() == Category.INCOME_CATEGORY) newType = Category.BOTH_CATEGORY;
        else newType = Category.INCOME_CATEGORY;
        systemCategory.setType(newType);

        assertThrows(GnomyIllegalQueryException.class,
                () -> categoryRepository.update(systemCategory).blockingGet());
    }

    @Test
    public void cannot_delete_non_deletable_rows() {
        // App MUST always have at least one predefined category, which are undeletable
        assertThrows(GnomyIllegalQueryException.class,
                () -> categoryRepository.delete(1).blockingGet());
    }

    // Not testing case: Cannot delete categories that contain transactions
    //  Room should be already validating that

    private static class TestCategoryClass extends Category {
        public TestCategoryClass() {
            super();
        }
        public TestCategoryClass(Category original) {
            super.setBackgroundColor(original.getBackgroundColor());
            super.setDeletable(original.isDeletable());
            super.setId(original.getId());
            super.setIconResName(original.getIconResName());
            super.setName(original.getName());
            super.setType(original.getType());
        }
        public void forceDeletable() {
            super.setDeletable(true);
        }
    }
}
