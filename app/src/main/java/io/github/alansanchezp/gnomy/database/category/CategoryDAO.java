package io.github.alansanchezp.gnomy.database.category;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface CategoryDAO {
    @Query("SELECT * FROM categories")
    LiveData<List<Category>> getAll();

    @Query("SELECT * FROM categories WHERE category_id = :id")
    LiveData<Category> find(int id);

    @Insert
    void insert(Category... categories);

    @Update
    void update(Category... categories);

    @Delete
    void delete(Category... categories);
}
