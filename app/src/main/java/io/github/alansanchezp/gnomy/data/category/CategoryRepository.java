package io.github.alansanchezp.gnomy.data.category;

import android.content.Context;
import android.util.Log;

import java.util.List;

import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.data.GnomyDatabase;
import io.github.alansanchezp.gnomy.data.GnomyIllegalQueryException;
import io.reactivex.Single;

public class CategoryRepository {
    private final CategoryDAO categoryDAO;
    private final GnomyDatabase db;

    public CategoryRepository(Context context) {
        db = GnomyDatabase.getInstance(context, "");
        categoryDAO = db.categoryDAO();
    }

    /**
     * Retrieves categories of type {@link Category#SHARED_CATEGORY}
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

    /**
     * Inserts a given {@link Category} row from some input, ensuring
     * that inserted categories through repository (user-defined ones)
     * are never non-deletable.
     *
     * @param category      Category to be inserted
     * @return              Single object that can be observed on main thread, wrapping
     *                      the inserted transaction's generated id.
     */
    public Single<Long> insert(Category category) {
        return db.toSingleInTransaction(() -> {
            if (!category.isDeletable()) throw new GnomyIllegalQueryException("Attempting to insert a non-deletable category");
            return categoryDAO._insert(category);
        });
    }

    /**
     * Updates a given {@link Category} row from some input, ensuring
     * that deletable status is not changed.
     *
     * @param category      New category value to be used.
     * @return              Single object that can be observed on main thread.
     */
    public Single<Integer> update(Category category) {
        return db.toSingleInTransaction(() -> {
            try {
                Category originalCategory = categoryDAO._find(category.getId());
                if (originalCategory.isDeletable() != category.isDeletable())
                    throw new GnomyIllegalQueryException("Attempting to alter deletable status of category.");
                if (originalCategory.getType() != category.getType())
                    throw new GnomyIllegalQueryException("Cannot alter the type of a category.");
                return categoryDAO._update(category);
            } catch(NullPointerException e) {
                throw new GnomyIllegalQueryException(e);
            }
        });
    }

    /**
     * Deletes a {@link Category}, except if its database status
     * marks it as non-deletable.
     *
     * @param categoryId    Id of the category to delete.
     * @return              Single object that can be observed on main thread.
     */
    public Single<Integer> delete(int categoryId) {
        return db.toSingleInTransaction(() -> {
            try {
                Category targetCategory = categoryDAO._find(categoryId);
                if (!targetCategory.isDeletable())
                    throw new GnomyIllegalQueryException("Attempting to delete non-deletable category.");

                return categoryDAO._delete(targetCategory);
            } catch(NullPointerException e) {
                throw new GnomyIllegalQueryException(e);
            }
        });
    }
}
