package io.github.alansanchezp.gnomy.ui.account;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.github.alansanchezp.gnomy.util.ISpinnerItem;

import static io.github.alansanchezp.gnomy.database.account.Account.BANK;
import static io.github.alansanchezp.gnomy.database.account.Account.CREDIT_CARD;
import static io.github.alansanchezp.gnomy.database.account.Account.INFORMAL;
import static io.github.alansanchezp.gnomy.database.account.Account.INVERSIONS;
import static io.github.alansanchezp.gnomy.database.account.Account.OTHER;
import static io.github.alansanchezp.gnomy.database.account.Account.SAVINGS;

public class AccountTypeItem implements ISpinnerItem {
    private final int type;
    private final String name;
    public AccountTypeItem(int type, String name) {
        this.type = type;
        this.name = name;
    }

    @Nullable
    @Override
    public String getDrawableResourceName() {
        switch (type) {
            case BANK:
                return "ic_account_balance_black_24dp";
            case INFORMAL:
                return "ic_account_balance_piggy_black_24dp";
            case SAVINGS:
                return "ic_account_balance_savings_black_24dp";
            case INVERSIONS:
                return "ic_account_balance_inversion_black_24dp";
            case CREDIT_CARD:
                return "ic_account_balance_credit_card_black_24dp";
            case OTHER:
            default:
                return "ic_account_balance_wallet_black_24dp";
        }
    }

    @Override
    public int getDrawableColor() {
        return Color.BLACK;
    }

    @Override
    public int getId() {
        return type;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof AccountTypeItem)) return false;
        return this.type == ((AccountTypeItem) obj).type;
    }
}
