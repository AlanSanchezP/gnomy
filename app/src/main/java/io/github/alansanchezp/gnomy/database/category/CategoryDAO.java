package io.github.alansanchezp.gnomy.database.category;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface CategoryDAO {
    @Query("SELECT * from categories")
    List<Category> getAll();

    @Insert
    void insertAll(Category... categories);
}
