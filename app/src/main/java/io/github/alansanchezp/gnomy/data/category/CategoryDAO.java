package io.github.alansanchezp.gnomy.data.category;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import io.github.alansanchezp.gnomy.data.transaction.MoneyTransaction;

import static io.github.alansanchezp.gnomy.data.category.Category.BOTH_CATEGORY;
import static io.github.alansanchezp.gnomy.data.category.Category.HIDDEN_CATEGORY;

@Dao
public abstract class CategoryDAO {
    @Query("SELECT * FROM categories WHERE category_type != " + HIDDEN_CATEGORY)
    protected abstract LiveData<List<Category>> getAll();

    @Query("SELECT * FROM categories WHERE category_type == :categoryType OR category_type ==" + BOTH_CATEGORY)
    protected abstract LiveData<List<Category>> getSharedAndCategory(int categoryType);

    @Query("SELECT * FROM categories WHERE category_type == :categoryType")
    protected abstract LiveData<List<Category>> getByStrictCategory(int categoryType);

    @Query("SELECT * FROM categories WHERE category_id = :id")
    protected abstract LiveData<Category> find(int id);

    /**
     * !!!  NEVER USE DIRECTLY FROM REPOSITORY
     *      WITHOUT WRAPPING IN Single<> METHOD  !!!
     * @param id    Category id to find.
     * @return      Category object that matched the given id.
     */
    @Query("SELECT * FROM categories WHERE category_id = :id;")
    protected abstract Category _find(int id);

    /**
     * !!!  NEVER USE DIRECTLY FROM REPOSITORY
     *      WITHOUT WRAPPING IN Single<> METHOD  !!!
     *
     */
    @Insert
    protected abstract long _insert(Category category);

    /**
     * !!!  NEVER USE DIRECTLY FROM REPOSITORY
     *      WITHOUT WRAPPING IN Single<> METHOD  !!!
     *
     */
    @Update
    protected abstract int _update(Category category);

    /**
     * !!!  NEVER USE DIRECTLY FROM REPOSITORY
     *      WITHOUT WRAPPING IN Single<> METHOD  !!!
     *
     */
    @Delete
    protected abstract int _delete(Category... categories);
}
