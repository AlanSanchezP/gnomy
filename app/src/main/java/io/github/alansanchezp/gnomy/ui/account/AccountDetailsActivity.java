package io.github.alansanchezp.gnomy.ui.account;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.math.BigDecimal;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.ui.BackButtonActivity;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;
import io.github.alansanchezp.gnomy.viewmodel.account.AccountViewModel;

public class AccountDetailsActivity
        extends BackButtonActivity {
    public static final String EXTRA_ID = "account_id";
    private TextView mNameTV;
    private FloatingActionButton mFAB;
    private Button mSeeMoreBtn;
    private AccountViewModel mAccountViewModel;
    private LiveData<BigDecimal> mLatestBalanceSum;
    private Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: Integrate MonthToolbarViewModel using inheritance
        //  Same for MainActivity. This will help to avoid
        //  creating the viewmodel directly in custom view
        mAccountViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(
                        this.getApplication()))
                .get(AccountViewModel.class);

        mAppbar.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        setTitle(getString(R.string.account_details));

        mNameTV = findViewById(R.id.account_name);
        mSeeMoreBtn = findViewById(R.id.account_see_more_button);
        mFAB = findViewById(R.id.account_floating_action_button);

        Intent intent = getIntent();
        int accountId = intent.getIntExtra(EXTRA_ID, 0);
        disableActions();

        LiveData<Account> accountLiveData = mAccountViewModel.getAccount(accountId);
        accountLiveData.observe(this, this::onAccountChanged);

        mLatestBalanceSum = mAccountViewModel.getAccumulatedFromMonth(accountId, DateUtil.now());
        mLatestBalanceSum.observe(this, balance ->
                updateBalanceSum(mAccount, balance));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account_details_menu, menu);

        if (mAccount == null || mAccount.getId() == 0) {
            menu.findItem(R.id.action_account_actions)
                    .setEnabled(false);
            menu.findItem(R.id.action_archive_account)
                    .setEnabled(false);
            return super.onCreateOptionsMenu(menu);
        }

        boolean parentResponse = super.onCreateOptionsMenu(menu);
        tintMenuItems(ColorUtil.getTextColor(mAccount.getBackgroundColor()));

        return parentResponse;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case R.id.action_archive_account:
                item.setEnabled(false);
                disableActions();
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.account_card_archive))
                        .setMessage(getString(R.string.account_card_archive_info))
                        .setPositiveButton(getString(R.string.confirmation_dialog_yes), (dialog, which) -> {
                            mAccountViewModel.archive(mAccount);
                            Toast.makeText(AccountDetailsActivity.this, getString(R.string.account_message_archived), Toast.LENGTH_LONG).show();
                            finish();
                        })
                        .setNegativeButton(getString(R.string.confirmation_dialog_no), null)
                        .setOnDismissListener(dialog -> {
                            item.setEnabled(true);
                            enableActions();
                        })
                        .show();
                break;
            default:
                // TODO: Implement other actions when Transactions module is ready
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void disableActions() {
        mSeeMoreBtn.setEnabled(false);
        mFAB.setEnabled(false);
        mFAB.setElevation(6f);
    }

    @Override
    protected void enableActions() {
        mSeeMoreBtn.setEnabled(true);
        mFAB.setEnabled(true);
    }

    protected int getLayoutResourceId() {
        return R.layout.activity_account_details;
    }

    protected boolean displayDialogOnBackPress() {
        return false;
    }

    public void onFABClick(View v) {
        disableActions();

        Intent modifyAccountIntent = new Intent(this, AddEditAccountActivity.class);
        modifyAccountIntent.putExtra(AddEditAccountActivity.EXTRA_ID, mAccount.getId());

        startActivity(modifyAccountIntent);
    }

    public void onSeeMoreClick(View v) {
        disableActions();

        Intent accountHistoryIntent = new Intent(this, AccountBalanceHistoryActivity.class);
        accountHistoryIntent.putExtra(AccountBalanceHistoryActivity.EXTRA_ID, mAccount.getId());
        accountHistoryIntent.putExtra(AccountBalanceHistoryActivity.EXTRA_BG_COLOR, mAccount.getBackgroundColor());
        accountHistoryIntent.putExtra(AccountBalanceHistoryActivity.EXTRA_NAME, mAccount.getName());
        accountHistoryIntent.putExtra(AccountBalanceHistoryActivity.EXTRA_CURRENCY, mAccount.getDefaultCurrency());

        startActivity(accountHistoryIntent);
    }

    public void onAccountChanged(Account account) {
        if (account == null) {
            try {
                // TODO: REALLY FIND A WAY TO INSERT TO TEST DATABASE TO AVOID DOING THIS
                Class.forName("io.github.alansanchezp.gnomy.MainNavigationInstrumentedTest");
                Log.d("AccountDetailsActivity", "onAccountChangedA: Test environment. Setting empty account to prevent errors.");
                account = new Account();
            } catch (ClassNotFoundException cnfe) {
                Log.e("AccountDetailsActivity", "onAccountChanged: No account found. Finishing activity.");
                finish();
                return;
            }
        }
        mAccount = account;
        enableActions();

        tintElements(account.getBackgroundColor());
        if (mMenu != null) {
            mMenu.findItem(R.id.action_account_actions)
                    .setEnabled(true);
            mMenu.findItem(R.id.action_archive_account)
                    .setEnabled(true);
        }

        updateInfo(account);
    }

    private void updateBalanceSum(Account account, BigDecimal balanceSum) {
        if (account == null || balanceSum == null) return;

        TextView currentBalanceTV = findViewById(R.id.account_latest_balance);

        try {
            currentBalanceTV.setText(CurrencyUtil.format(balanceSum, mAccount.getDefaultCurrency()));
        } catch (GnomyCurrencyException gce) {
            Log.wtf("AccountDetailsActivity", "updateBalance: ", gce);
        }
    }

    private void updateInfo(Account account) {
        mNameTV.setText(account.getName());

        TextView initialValueTV = findViewById(R.id.account_initial_value);
        ImageView typeImage = findViewById(R.id.account_type_icon);
        TextView typeTV = findViewById(R.id.account_type);
        ImageView includedInSumImage = findViewById(R.id.account_included_in_sum_icon);
        TextView includedInSumTV = findViewById(R.id.account_included_in_sum_text);
        Drawable typeIcon = ContextCompat.getDrawable(this,
                Account.getDrawableResourceId(account.getType()));
        String typeString = getString(
                Account.getTypeNameResourceId(account.getType())
        );
        Drawable includedInSumIcon;
        String includedInSumString;

        if (account.isShowInDashboard()) {
            includedInSumIcon = ContextCompat.getDrawable(this, R.drawable.ic_check_black_24dp);
            includedInSumImage.setTag(R.drawable.ic_check_black_24dp);
            includedInSumString = getString(R.string.account_is_included_in_sum);
        } else {
            includedInSumIcon = ContextCompat.getDrawable(this, R.drawable.ic_close_black_24dp);
            includedInSumImage.setTag(R.drawable.ic_close_black_24dp);
            includedInSumString = getString(R.string.account_is_not_included_in_sum);
        }

        typeImage.setImageDrawable(typeIcon);
        typeTV.setText(typeString);
        includedInSumImage.setImageDrawable(includedInSumIcon);
        includedInSumTV.setText(includedInSumString);

        try {
            updateBalanceSum(account, mLatestBalanceSum.getValue());
            initialValueTV.setText(CurrencyUtil.format(account.getInitialValue(), account.getDefaultCurrency()));
        } catch (GnomyCurrencyException gce) {
            Log.wtf("AccountDetailsActivity", "updateInfo: ", gce);
        }
    }

    protected void tintMenuItems(@ColorInt int color) {
        if (mMenu != null) {
            mMenu.findItem(R.id.action_archive_account)
                    .getIcon()
                    .setTint(color);
            mMenu.findItem(R.id.action_account_actions)
                    .getIcon()
                    .setTint(color);
        }
    }

    private void tintElements(@ColorInt int bgColor) {
        setThemeColor(bgColor);

        LinearLayout container = findViewById(R.id.account_details_container);
        container.setBackgroundColor(bgColor);

        mNameTV.setTextColor(mThemeTextColor);

        int fabBgColor = ColorUtil.getVariantByFactor(bgColor, 0.86f);
        int fabTextColor = ColorUtil.getTextColor(fabBgColor);

        mFAB.setBackgroundTintList(ColorStateList.valueOf(fabBgColor));
        mFAB.getDrawable().mutate().setTint(fabTextColor);
        mFAB.setRippleColor(mThemeTextColor);

        TextView balanceHistoryTV = findViewById(R.id.account_balance_history_title);
        balanceHistoryTV.setTextColor(mThemeTextColor);

        mSeeMoreBtn.setBackgroundColor(fabBgColor);
        mSeeMoreBtn.setTextColor(fabTextColor);
    }
}
