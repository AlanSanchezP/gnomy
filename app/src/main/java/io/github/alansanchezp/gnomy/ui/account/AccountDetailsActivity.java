package io.github.alansanchezp.gnomy.ui.account;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.YearMonth;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountWithAccumulated;
import io.github.alansanchezp.gnomy.databinding.ActivityAccountDetailsBinding;
import io.github.alansanchezp.gnomy.ui.BackButtonActivity;
import io.github.alansanchezp.gnomy.ui.ConfirmationDialogFragment;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;
import io.github.alansanchezp.gnomy.util.android.SingleClickViewHolder;
import io.github.alansanchezp.gnomy.util.android.ViewTintingUtil;
import io.github.alansanchezp.gnomy.viewmodel.account.AccountViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AccountDetailsActivity
        extends BackButtonActivity<ActivityAccountDetailsBinding> {
    public static final String EXTRA_ACCOUNT_ID = "AccountDetailsActivity.AccountId";
    public static final String TAG_ARCHIVE_DIALOG = "AccountDetailsActivity.ArchiveDialog";
    private SingleClickViewHolder<FloatingActionButton> mFABVH;
    private SingleClickViewHolder<Button> mSeeMoreBtnVH;
    private AccountViewModel mAccountViewModel;
    private Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccountViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(
                        this.getApplication()))
                .get(AccountViewModel.class);

        mAppbar.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        setTitle(getString(R.string.account_details));

        mSeeMoreBtnVH = new SingleClickViewHolder<>($.accountSeeMoreButton);
        mSeeMoreBtnVH.setOnClickListener(this::onSeeMoreClick);

        mFABVH = new SingleClickViewHolder<>($.accountFloatingActionButton);
        mFABVH.setOnClickListener(this::onFABClick);

        Intent intent = getIntent();
        int accountId = intent.getIntExtra(EXTRA_ACCOUNT_ID, 0);
        disableActions();

        mAccountViewModel.getAccountWithAccumulated(accountId)
                .observe(this, this::onDataChanged);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean superResponse = super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.account_details_menu, menu);
        toggleMenuItems();
        tintMenuItems();

        return superResponse;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case R.id.action_archive_account:
                disableActions();
                FragmentManager fm = getSupportFragmentManager();
                if (fm.findFragmentByTag(TAG_ARCHIVE_DIALOG) != null) {
                    enableActions();
                    break;
                }
                ConfirmationDialogFragment dialog = (ConfirmationDialogFragment)
                        fm.getFragmentFactory().instantiate(
                                getClassLoader(), ConfirmationDialogFragment.class.getName());
                Bundle args = new Bundle();
                args.putString(ConfirmationDialogFragment.ARG_TITLE, getString(R.string.account_card_archive));
                args.putString(ConfirmationDialogFragment.ARG_MESSAGE, getString(R.string.account_card_archive_info));
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), TAG_ARCHIVE_DIALOG);
                break;
            default:
                // TODO: Implement other actions when Transactions module is ready
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfirmationDialogYes(DialogInterface dialog, String dialogTag, int which) {
        if (dialogTag.equals(TAG_ARCHIVE_DIALOG)) {
            if (mAccount == null) {
                Log.wtf("AccountDetailsActivity", "onConfirmationDialogYes: MenuItems were enabled but no account was found");
            }
            mCompositeDisposable.add(
                mAccountViewModel.archive(mAccount.getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            integer -> {
                                Toast.makeText(AccountDetailsActivity.this, getString(R.string.account_message_archived), Toast.LENGTH_LONG).show();
                                finish();
                            },
                            throwable ->
                                Toast.makeText(this, R.string.generic_data_error, Toast.LENGTH_LONG).show()
                            ));

        } else {
            super.onConfirmationDialogYes(dialog, dialogTag, which);
        }
    }

    @Override
    public void onConfirmationDialogDismiss(DialogInterface dialog, String dialogTag) {
        if (dialogTag.equals(TAG_ARCHIVE_DIALOG)) {
            enableActions();
        } else {
            super.onConfirmationDialogDismiss(dialog, dialogTag);
        }
    }

    @Override
    protected void disableActions() {
        mSeeMoreBtnVH.blockClicks();
        mFABVH.blockClicks();
    }

    @Override
    protected void enableActions() {
        mSeeMoreBtnVH.allowClicks();
        mFABVH.allowClicks();
    }

    protected boolean displayDialogOnBackPress() {
        return false;
    }

    public void onFABClick(View v) {
        Intent modifyAccountIntent = new Intent(this, AddEditAccountActivity.class);
        modifyAccountIntent.putExtra(AddEditAccountActivity.EXTRA_ACCOUNT_ID, mAccount.getId());

        startActivity(modifyAccountIntent);
    }

    public void onSeeMoreClick(View v) {
        disableActions();

        Intent accountHistoryIntent = new Intent(this, AccountBalanceHistoryActivity.class);
        accountHistoryIntent.putExtra(AccountBalanceHistoryActivity.EXTRA_ACCOUNT_ID, mAccount.getId());
        accountHistoryIntent.putExtra(AccountBalanceHistoryActivity.EXTRA_BG_COLOR, mAccount.getBackgroundColor());
        accountHistoryIntent.putExtra(AccountBalanceHistoryActivity.EXTRA_NAME, mAccount.getName());
        accountHistoryIntent.putExtra(AccountBalanceHistoryActivity.EXTRA_CURRENCY, mAccount.getDefaultCurrency());
        accountHistoryIntent.putExtra(AccountBalanceHistoryActivity.EXTRA_ACCOUNT_CREATION_MONTH, YearMonth.from(mAccount.getCreatedAt()).toString());

        startActivity(accountHistoryIntent);
    }

    private void onDataChanged(AccountWithAccumulated awa) {
        if (awa.account == null) {
            Log.e("AccountDetailsActivity", "onAccountChanged: No account found. Finishing activity.");
            finish();
            return;
        }
        mAccount = awa.account;
        enableActions();

        tintElements(mAccount.getBackgroundColor());
        toggleMenuItems();

        updateInfo(awa);
    }

    private void updateInfo(AccountWithAccumulated awa) {
        $.accountName.setText(awa.account.getName());

        Drawable typeIcon = ContextCompat.getDrawable(this,
                Account.getDrawableResourceId(awa.account.getType()));
        String createdAtString = awa.account.getCreatedAt().toLocalDate().toString();
        String typeString = getString(
                Account.getTypeNameResourceId(awa.account.getType())
        );
        Drawable includedInSumIcon;
        String includedInSumString;

        if (awa.account.isShowInDashboard()) {
            includedInSumIcon = ContextCompat.getDrawable(this, R.drawable.ic_check_black_24dp);
            $.accountIncludedInSumIcon.setTag(R.drawable.ic_check_black_24dp);
            includedInSumString = getString(R.string.account_is_included_in_sum);
        } else {
            includedInSumIcon = ContextCompat.getDrawable(this, R.drawable.ic_close_black_24dp);
            $.accountIncludedInSumIcon.setTag(R.drawable.ic_close_black_24dp);
            includedInSumString = getString(R.string.account_is_not_included_in_sum);
        }

        $.accountCreatedAtText.setText(createdAtString);
        $.accountTypeIcon.setImageDrawable(typeIcon);
        $.accountType.setText(typeString);
        $.accountIncludedInSumIcon.setImageDrawable(includedInSumIcon);
        $.accountIncludedInSumText.setText(includedInSumString);

        try {
            $.accountLatestBalance.setText(CurrencyUtil.format(
                    awa.getConfirmedAccumulatedBalanceAtMonth(),
                    mAccount.getDefaultCurrency()));
            $.accountInitialValue.setText(CurrencyUtil.format(
                    awa.account.getInitialValue(),
                    awa.account.getDefaultCurrency()));
        } catch (GnomyCurrencyException gce) {
            Log.wtf("AccountDetailsActivity", "updateInfo: ", gce);
        }
    }

    private void toggleMenuItems() {
        if (mMenu == null) return;

        boolean enableActions = (mAccount != null);
        mMenu.findItem(R.id.action_account_actions)
                .setEnabled(enableActions);
        mMenu.findItem(R.id.action_archive_account)
                .setEnabled(enableActions);
    }

    @Override
    protected void tintMenuItems() {
        super.tintMenuItems();
        if (mMenu == null || mAccount == null) return;
        ViewTintingUtil.tintMenuItems(
                mMenu,
                new int[]{
                        R.id.action_archive_account,
                        R.id.action_account_actions},
                mThemeTextColor);
    }

    private void tintElements(@ColorInt int bgColor) {
        setThemeColor(bgColor);

        $.accountDetailsContainer.setBackgroundColor(bgColor);
        $.accountName.setTextColor(mThemeTextColor);

        int fabBgColor = ColorUtil.getVariantByFactor(bgColor, 0.86f);
        int fabTextColor = ColorUtil.getTextColor(fabBgColor);

        mFABVH.onView(this, v ->
                ViewTintingUtil.tintFAB(v, fabBgColor, fabTextColor, mThemeTextColor));

        $.accountBalanceHistoryTitle.setTextColor(mThemeTextColor);
        mSeeMoreBtnVH.onView(this, v -> {
            v.setBackgroundColor(fabBgColor);
            v.setTextColor(fabTextColor);
        });
    }
}
