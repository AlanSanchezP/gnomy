package io.github.alansanchezp.gnomy.database.category;

import android.content.Context;
import android.util.Log;

import java.util.List;

import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.database.GnomyDatabase;
import io.github.alansanchezp.gnomy.database.GnomyIllegalQueryException;

public class CategoryRepository {
    private CategoryDAO categoryDAO;

    public CategoryRepository(Context context) {
        GnomyDatabase db;
        db = GnomyDatabase.getInstance(context, "");
        categoryDAO = db.categoryDAO();
    }

    /**
     * Retrieves categories of type {@link Category#BOTH_CATEGORY}
     * and either {@link Category#INCOME_CATEGORY} or {@link Category#EXPENSE_CATEGORY}.
     *
     * @param categoryType  Type to get in addition to BOTH_CATEGORY.
     * @return              LiveData containing a list of categories.
     */
    public LiveData<List<Category>> getSharedAndCategory(int categoryType) {
        if (categoryType != Category.INCOME_CATEGORY && categoryType != Category.EXPENSE_CATEGORY)
            throw new GnomyIllegalQueryException("Invalid category type to retrieve.");
        return categoryDAO.getSharedAndCategory(categoryType);
    }

    /**
     * Retrieves categories that match the specific given category.
     *
     * @param categoryType  Category type to match.
     * @return              LiveData containing a list of categories.
     */
    public LiveData<List<Category>> getByStrictCategory(int categoryType) {
        if (categoryType == Category.HIDDEN_CATEGORY)
            Log.w("CategoryRepository", "Hidden categories are not meant to be queried.");
        return categoryDAO.getByStrictCategory(categoryType);
    }

    /**
     * Finds a given category based on its id.
     *
     * @param categoryId    Category id.
     * @return              LiveData containing the category.
     */
    public LiveData<Category> find(int categoryId) {
        return categoryDAO.find(categoryId);
    }
}
