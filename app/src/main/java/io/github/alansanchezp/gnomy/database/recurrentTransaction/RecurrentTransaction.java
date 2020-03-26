package io.github.alansanchezp.gnomy.database.recurrentTransaction;

import android.content.ReceiverCallNotAllowedException;

import org.threeten.bp.OffsetDateTime;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;

@Entity(tableName = "recurrent_transactions",
        foreignKeys = @ForeignKey(
            entity = MoneyTransaction.class,
            parentColumns = "transaction_id",
            childColumns = "template_transaction_id",
            onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("template_transaction_id")
)
public class RecurrentTransaction {
    @Ignore
    public static String UNIT_DAYS = "days";
    public static String UNIT_WEEKS = "weeks";
    public static String UNIT_MONTHS = "months";
    public static String UNIT_YEARS = "years";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "recurrent_transaction_id")
    private int id;

    @ColumnInfo(name = "display_value")
    private int displayValue;

    @ColumnInfo(name = "display_unit")
    @NonNull
    private String displayUnit = UNIT_DAYS;

    @ColumnInfo(name = "total_repetition_times")
    private int totalRepetitionTimes;

    @ColumnInfo(name = "done_repetition_times")
    private int doneRepetitionTimes;

    @ColumnInfo(name = "template_transaction_id")
    private int templateTransactionId;

    @ColumnInfo(name = "next_transaction_date")
    @NonNull
    private OffsetDateTime nextTransactionDate = OffsetDateTime.now();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(int displayValue) {
        this.displayValue = displayValue;
    }

    @NonNull
    public String getDisplayUnit() {
        return displayUnit;
    }

    public void setDisplayUnit(@NonNull String displayUnit) {
        this.displayUnit = displayUnit;
    }

    public int getTotalRepetitionTimes() {
        return totalRepetitionTimes;
    }

    public void setTotalRepetitionTimes(int totalRepetitionTimes) {
        this.totalRepetitionTimes = totalRepetitionTimes;
    }

    public int getDoneRepetitionTimes() {
        return doneRepetitionTimes;
    }

    public void setDoneRepetitionTimes(int doneRepetitionTimes) {
        this.doneRepetitionTimes = doneRepetitionTimes;
    }

    public int getTemplateTransactionId() {
        return templateTransactionId;
    }

    public void setTemplateTransactionId(int templateTransactionId) {
        this.templateTransactionId = templateTransactionId;
    }

    @NonNull
    public OffsetDateTime getNextTransactionDate() {
        return nextTransactionDate;
    }

    public void setNextTransactionDate(@NonNull OffsetDateTime nextTransactionDate) {
        this.nextTransactionDate = nextTransactionDate;
    }
}
