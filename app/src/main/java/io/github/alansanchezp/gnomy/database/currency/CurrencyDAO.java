package io.github.alansanchezp.gnomy.database.currency;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface CurrencyDAO {
    @Query("SELECT * from currencies")
    List<Currency> getAll();
}
