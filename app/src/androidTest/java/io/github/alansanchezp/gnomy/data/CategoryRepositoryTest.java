package io.github.alansanchezp.gnomy.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.platform.app.InstrumentationRegistry;
import io.github.alansanchezp.gnomy.data.category.Category;
import io.github.alansanchezp.gnomy.data.category.CategoryRepository;

import static io.github.alansanchezp.gnomy.EspressoTestUtil.assertThrows;

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
}
