package io.github.alansanchezp.gnomy.database.category;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public abstract class CategoryDAO {
    @Query("SELECT * FROM categories")
    protected abstract LiveData<List<Category>> getAll();

    @Query("SELECT * FROM categories WHERE category_id = :id")
    protected abstract LiveData<Category> find(int id);

    @Insert
    protected abstract void insert(Category... categories);

    @Update
    protected abstract void update(Category... categories);

    @Delete
    protected abstract void delete(Category... categories);
}
