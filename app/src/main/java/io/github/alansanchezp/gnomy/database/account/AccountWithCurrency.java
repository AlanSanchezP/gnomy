package io.github.alansanchezp.gnomy.database.account;

import androidx.room.Embedded;
import androidx.room.Relation;
import io.github.alansanchezp.gnomy.database.currency.Currency;

public class AccountWithCurrency {
    @Embedded
    public Account account;
    @Relation(
            entity = Currency.class,
            parentColumn = "default_currency_id",
            entityColumn = "currency_id"
    )
    public Currency currency;
}
