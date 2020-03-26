package io.github.alansanchezp.gnomy.database.transfer;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface TransferDAO {
    @Query("SELECT * from transfers")
    List<Transfer> getAll();

    @Insert
    void insertAll(Transfer... transfers);
}
