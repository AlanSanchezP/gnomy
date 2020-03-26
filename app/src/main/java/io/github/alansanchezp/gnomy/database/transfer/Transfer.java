package io.github.alansanchezp.gnomy.database.transfer;

import org.threeten.bp.OffsetDateTime;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;

@Entity(tableName = "transfers",
        foreignKeys = {
            @ForeignKey(
                entity = MoneyTransaction.class,
                parentColumns = "transaction_id",
                childColumns = "origin_transaction_id",
                onDelete = ForeignKey.RESTRICT
            ),
            @ForeignKey(
                entity = MoneyTransaction.class,
                parentColumns = "transaction_id",
                childColumns = "destination_transaction_id",
                onDelete = ForeignKey.RESTRICT
            )
        },
        indices = {
            @Index("destination_transaction_id"),
            @Index("origin_transaction_id")
        }
)
public class Transfer {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "transference_id")
    private int id;

    @ColumnInfo(name = "origin_transaction_id")
    private int originTransactionId;

    @ColumnInfo(name = "destination_transaction_id")
    private int destinationTransactionId;

    @ColumnInfo(name = "transference_date")
    @NonNull
    private OffsetDateTime date = OffsetDateTime.now();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOriginTransactionId() {
        return originTransactionId;
    }

    public void setOriginTransactionId(int originTransactionId) {
        this.originTransactionId = originTransactionId;
    }

    public int getDestinationTransactionId() {
        return destinationTransactionId;
    }

    public void setDestinationTransactionId(int destinationTransactionId) {
        this.destinationTransactionId = destinationTransactionId;
    }

    @NonNull
    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(@NonNull OffsetDateTime date) {
        this.date = date;
    }
}
