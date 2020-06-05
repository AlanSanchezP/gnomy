package io.github.alansanchezp.gnomy.ui.account;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.viewmodel.AccountViewModel;

public class AccountDetailsActivity extends AppCompatActivity {
    static final String EXTRA_ID = "account_id";
    static final String EXTRA_BG_COLOR = "bg_color";
    protected int mBgColor;
    protected int mTextColor;
    protected Toolbar mToolbar;
    protected Drawable mUpArrow;
    protected AccountViewModel mAccountViewModel;
    protected LiveData<Account> mAccount;
    private TextView mNameTV;
    private Menu mMenu;
    FloatingActionButton mFAB;
    protected Button mSeeMoreBtn;
    protected int mAccountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(getString(R.string.account_details));
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUpArrow = getResources().getDrawable(R.drawable.abc_vector_test);

        Intent intent = getIntent();
        mAccountId = intent.getIntExtra(EXTRA_ID, 0);
        mBgColor = intent.getIntExtra(EXTRA_BG_COLOR, 0XFF);
        mTextColor = ColorUtil.getTextColor(mBgColor);

        mNameTV = (TextView) findViewById(R.id.account_name);

        mSeeMoreBtn = (Button) findViewById(R.id.account_see_more_button);
        mSeeMoreBtn.setEnabled(false);

        mFAB = (FloatingActionButton) findViewById(R.id.account_floating_action_button);
        mFAB.setEnabled(false);

        setColors();

        mAccountViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(AccountViewModel.class);
        mAccount = mAccountViewModel.getAccount(mAccountId);
        mAccount.observe(this, new Observer<Account>() {
            @Override
            public void onChanged(Account account) {
                if (account.getBackgroundColor() != mBgColor) {
                    mBgColor = account.getBackgroundColor();
                    mTextColor = ColorUtil.getTextColor(mBgColor);
                    setColors();
                }
                mFAB.setEnabled(true);
                mSeeMoreBtn.setEnabled(true);
                if (mMenu != null) {
                    mMenu.findItem(R.id.action_account_actions)
                            .setEnabled(true);
                    mMenu.findItem(R.id.action_archive_account)
                            .setEnabled(true);
                }
                updateInfo(account);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account_details_menu, menu);
        mMenu = menu;

        menu.findItem(R.id.action_archive_account)
                .getIcon()
                .setTint(mTextColor);
        menu.findItem(R.id.action_account_actions)
                .getIcon()
                .setTint(mTextColor);

        if (mAccount.getValue() == null) {
            menu.findItem(R.id.action_account_actions)
                    .setEnabled(false);
            menu.findItem(R.id.action_archive_account)
                    .setEnabled(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_archive_account:
                item.setEnabled(false);
                mFAB.setEnabled(false);
                mFAB.setElevation(6f);
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.account_card_archive))
                        .setMessage(getString(R.string.account_card_archive_info))
                        .setPositiveButton(getString(R.string.confirmation_dialog_yes), new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mAccountViewModel.archive(mAccount.getValue());
                                Toast.makeText(AccountDetailsActivity.this, getString(R.string.account_message_archived), Toast.LENGTH_LONG).show();
                                finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.confirmation_dialog_no), null)
                        .setOnDismissListener(new DialogInterface.OnDismissListener()
                        {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                item.setEnabled(true);
                                mFAB.setEnabled(true);
                            }
                        })
                        .show();
                break;
            default:
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFAB.setEnabled(true);
        mSeeMoreBtn.setEnabled(true);
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
        mFAB.setEnabled(false);
        mFAB.setElevation(6f);
        Account account = mAccount.getValue();

        Intent modifyAccountIntent = new Intent(this, AddEditAccountActivity.class);
        modifyAccountIntent.putExtra(AddEditAccountActivity.EXTRA_ID, account.getId());
        modifyAccountIntent.putExtra(AddEditAccountActivity.EXTRA_BG_COLOR, account.getBackgroundColor());
        modifyAccountIntent.putExtra(AddEditAccountActivity.EXTRA_NAME, account.getName());
        modifyAccountIntent.putExtra(AddEditAccountActivity.EXTRA_INITIAL_VALUE, account.getInitialValue().toPlainString());
        modifyAccountIntent.putExtra(AddEditAccountActivity.EXTRA_INCLUDED_IN_SUM, account.isShowInDashboard());
        modifyAccountIntent.putExtra(AddEditAccountActivity.EXTRA_CURRENCY, account.getDefaultCurrency());
        modifyAccountIntent.putExtra(AddEditAccountActivity.EXTRA_TYPE, account.getType());

        startActivity(modifyAccountIntent);
    }

    private void updateInfo(Account account) {
        mNameTV.setText(account.getName());
    }

    private void setColors() {
        mToolbar.setBackgroundColor(mBgColor);
        mToolbar.setTitleTextColor(mTextColor);
        mUpArrow.setColorFilter(mTextColor, PorterDuff.Mode.SRC_ATOP);

        getSupportActionBar().setHomeAsUpIndicator(mUpArrow);
        getWindow().setStatusBarColor(ColorUtil.getDarkVariant(mBgColor));

        if (mMenu != null) {
            mMenu.findItem(R.id.action_account_actions)
                    .getIcon()
                    .setTint(mTextColor);
            mMenu.findItem(R.id.action_archive_account)
                    .getIcon()
                    .setTint(mTextColor);
        }

        LinearLayout container = (LinearLayout) findViewById(R.id.account_details_container);
        container.setBackgroundColor(mBgColor);

        mNameTV.setTextColor(mTextColor);

        int fabBgColor = ColorUtil.getVariantByFactor(mBgColor, 0.86f);
        int fabTextColor = ColorUtil.getTextColor(fabBgColor);

        mFAB.setBackgroundTintList(ColorStateList.valueOf(fabBgColor));
        mFAB.getDrawable().mutate().setTint(fabTextColor);
        mFAB.setRippleColor(mTextColor);

        TextView balanceHistoryTV = (TextView) findViewById(R.id.account_balance_history_title);
        balanceHistoryTV.setTextColor(mTextColor);

        mSeeMoreBtn.setBackgroundColor(mBgColor);
        mSeeMoreBtn.setTextColor(mTextColor);
    }
}
