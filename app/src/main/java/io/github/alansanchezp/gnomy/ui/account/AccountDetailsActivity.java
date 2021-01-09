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

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.data.account.Account;
import io.github.alansanchezp.gnomy.data.account.AccountWithAccumulated;
import io.github.alansanchezp.gnomy.data.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.databinding.ActivityAccountDetailsBinding;
import io.github.alansanchezp.gnomy.ui.BackButtonActivity;
import io.github.alansanchezp.gnomy.ui.ConfirmationDialogFragment;
import io.github.alansanchezp.gnomy.ui.transaction.AddEditTransactionActivity;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;
import io.github.alansanchezp.gnomy.androidUtil.SingleClickViewHolder;
import io.github.alansanchezp.gnomy.androidUtil.ViewTintingUtil;
import io.github.alansanchezp.gnomy.viewmodel.account.AccountViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static io.github.alansanchezp.gnomy.util.DateUtil.getOffsetDateTimeString;

public class AccountDetailsActivity
        extends BackButtonActivity<ActivityAccountDetailsBinding> {
    public static final String EXTRA_ACCOUNT_ID = "AccountDetailsActivity.AccountId";
    public static final String TAG_ARCHIVE_DIALOG = "AccountDetailsActivity.ArchiveDialog";
    private SingleClickViewHolder<FloatingActionButton> mFABVH;
    private SingleClickViewHolder<Button> mSeeMoreBtnVH;
    private AccountViewModel mAccountViewModel;
    private Account mAccount;

    public AccountDetailsActivity() {
        super(R.menu.account_details_menu, false, ActivityAccountDetailsBinding::inflate);
    }

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
        toggleMenuItems(true);
        tintMenuItems();

        return superResponse;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        Intent intent = new Intent(this, AddEditTransactionActivity.class);
        switch (item.getItemId()) {
            case R.id.action_archive_account:
                disableActions();
                intent = null;
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
            case R.id.action_new_expense:
                disableActions();
                intent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, MoneyTransaction.EXPENSE);
                intent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_ACCOUNT, mAccount.getId());
                break;
            case R.id.action_new_income:
                disableActions();
                intent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, MoneyTransaction.INCOME);
                intent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_ACCOUNT, mAccount.getId());
                break;
            case R.id.action_new_incoming_transfer:
                disableActions();
                intent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, MoneyTransaction.TRANSFER);
                intent.putExtra(AddEditTransactionActivity.EXTRA_TRANSFER_DESTINATION_ACCOUNT, mAccount.getId());
                break;
            case R.id.action_new_outgoing_transfer:
                disableActions();
                intent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, MoneyTransaction.TRANSFER);
                intent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_ACCOUNT, mAccount.getId());
                break;
            default:
                intent = null;
        }

        if (intent != null) startActivity(intent);

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
        if (mMenu != null) {
            toggleMenuItems(false);
        }
    }

    @Override
    protected void enableActions() {
        mSeeMoreBtnVH.allowClicks();
        mFABVH.allowClicks();
        toggleMenuItems(true);
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
        toggleMenuItems(true);
        updateInfo(awa);
    }

    private void updateInfo(AccountWithAccumulated awa) {
        $.accountName.setText(awa.account.getName());

        int iconResId = getResources().getIdentifier(
                awa.account.getDrawableResourceName(), "drawable", getPackageName());
        int typeStringResId = getResources().getIdentifier(
                awa.account.getTypeNameResourceName(), "string", getPackageName());
        Drawable typeIcon = ContextCompat.getDrawable(this, iconResId);
        String createdAtString = getOffsetDateTimeString(awa.account.getCreatedAt(), false);
        String typeString = getString(typeStringResId);
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

    private void toggleMenuItems(boolean preferredState) {
        if (mMenu == null) return;

        if (mAccount != null) {
            mMenu.findItem(R.id.action_account_actions)
                    .setEnabled(preferredState);
            mMenu.findItem(R.id.action_archive_account)
                    .setEnabled(preferredState);
        } else {
            mMenu.findItem(R.id.action_account_actions)
                    .setEnabled(false);
            mMenu.findItem(R.id.action_archive_account)
                    .setEnabled(false);
        }
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
