package io.github.alansanchezp.gnomy.ui.account;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;
import io.github.alansanchezp.gnomy.viewmodel.account.AccountViewModel;

public class AccountDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "account_id";
    private Toolbar mAppbar;
    private Drawable mUpArrow;
    private TextView mNameTV;
    private Menu mMenu;
    private FloatingActionButton mFAB;
    private Button mSeeMoreBtn;
    private AccountViewModel mAccountViewModel;
    private LiveData<BigDecimal> mLatestBalanceSum;
    private Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        mAccountViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(
                        this.getApplication()))
                .get(AccountViewModel.class);

        mAppbar = findViewById(R.id.custom_appbar);
        // Prevent potential noticeable blink in color
        mAppbar.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        setSupportActionBar(mAppbar);
        setTitle(getString(R.string.account_details));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUpArrow = ContextCompat.getDrawable(this, R.drawable.abc_vector_test);

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
        // We hold a reference to the menu in case onCreateOptionsMenu gets called
        // before account query returns.
        // TODO: Can (or should) we abstract this logic ?
        mMenu = menu;

        if (mAccount == null) {
            menu.findItem(R.id.action_account_actions)
                    .setEnabled(false);
            menu.findItem(R.id.action_archive_account)
                    .setEnabled(false);
        } else {
            tintMenuItems(ColorUtil.getTextColor(mAccount.getBackgroundColor()));
        }

        return super.onCreateOptionsMenu(menu);
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
    protected void onResume() {
        super.onResume();
        enableActions();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void onFABClick(View v) {
        disableActions();

        Intent modifyAccountIntent = new Intent(this, AddEditAccountActivity.class);
        modifyAccountIntent.putExtra(AddEditAccountActivity.EXTRA_ID, mAccount.getId());

        startActivity(modifyAccountIntent);
    }

    public void onSeeMoreClick(View v) {
        disableActions();

        Intent accountHistoryIntent = new Intent(this, AccountHistoryActivity.class);
        accountHistoryIntent.putExtra(AccountHistoryActivity.EXTRA_ID, mAccount.getId());
        accountHistoryIntent.putExtra(AccountHistoryActivity.EXTRA_BG_COLOR, mAccount.getBackgroundColor());
        accountHistoryIntent.putExtra(AccountHistoryActivity.EXTRA_NAME, mAccount.getName());
        accountHistoryIntent.putExtra(AccountHistoryActivity.EXTRA_CURRENCY, mAccount.getDefaultCurrency());

        startActivity(accountHistoryIntent);
    }

    public void onAccountChanged(Account account) {
        if (account == null) {
            Log.e("AccountDetails", "onAccountChanged: No account found. Finishing activity.");
            finish();
            return;
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

    private void tintMenuItems(@ColorInt int color) {
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
        if (bgColor > 0) return;

        int textColor = ColorUtil.getTextColor(bgColor);
        mAppbar.setBackgroundColor(bgColor);
        mAppbar.setTitleTextColor(textColor);

        mUpArrow.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(mUpArrow);
        getWindow().setStatusBarColor(ColorUtil.getDarkVariant(bgColor));

        tintMenuItems(textColor);

        LinearLayout container = findViewById(R.id.account_details_container);
        container.setBackgroundColor(bgColor);

        mNameTV.setTextColor(textColor);

        int fabBgColor = ColorUtil.getVariantByFactor(bgColor, 0.86f);
        int fabTextColor = ColorUtil.getTextColor(fabBgColor);

        mFAB.setBackgroundTintList(ColorStateList.valueOf(fabBgColor));
        mFAB.getDrawable().mutate().setTint(fabTextColor);
        mFAB.setRippleColor(textColor);

        TextView balanceHistoryTV = findViewById(R.id.account_balance_history_title);
        balanceHistoryTV.setTextColor(textColor);

        mSeeMoreBtn.setBackgroundColor(fabBgColor);
        mSeeMoreBtn.setTextColor(fabTextColor);
    }

    // TODO: Implement an interface and/or abstract class that defines
    //  disableActions() and enableActions() for multiple activities. It probably
    //  will also implement back button logic to avoid repeating the same code.
    private void disableActions() {
        mSeeMoreBtn.setEnabled(false);
        mFAB.setEnabled(false);
        mFAB.setElevation(6f);
    }

    private void enableActions() {
        mSeeMoreBtn.setEnabled(true);
        mFAB.setEnabled(true);
    }
}
