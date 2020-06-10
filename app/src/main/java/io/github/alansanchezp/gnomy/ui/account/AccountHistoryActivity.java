package io.github.alansanchezp.gnomy.ui.account;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import org.threeten.bp.YearMonth;

import java.math.BigDecimal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalance;
import io.github.alansanchezp.gnomy.util.ColorUtil;

public class AccountHistoryActivity extends AppCompatActivity {
    static final String EXTRA_ID = "account_id";
    static final String EXTRA_NAME = "account_name";
    static final String EXTRA_CURRENCY = "account_currency";
    static final String EXTRA_BG_COLOR = "bg_color";
    private Toolbar mSecondaryBar;
    protected Drawable mUpArrow;
    protected LiveData<BigDecimal> mBalanceSum;
    protected LiveData<MonthlyBalance> mBalance;
    protected int mToolbarBgColor;
    protected int mTitleTextColor;
    protected int mAccountId;
    protected String mAccountName;
    protected String mAccountCurrency;
    protected Toolbar mMainBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_history);

        Intent intent = getIntent();
        mAccountId = intent.getIntExtra(EXTRA_ID, 0);
        mAccountName = intent.getStringExtra(EXTRA_NAME);
        mAccountCurrency = intent.getStringExtra(EXTRA_CURRENCY);
        mToolbarBgColor = intent.getIntExtra(EXTRA_BG_COLOR, 0XFF);
        mTitleTextColor = ColorUtil.getTextColor(mToolbarBgColor);

        mMainBar = (Toolbar) findViewById(R.id.toolbar);
        mMainBar.setTitle(mAccountName + " " + getString(R.string.account_balance_history_legend));
        setSupportActionBar(mMainBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUpArrow = getResources().getDrawable(R.drawable.abc_vector_test);

        mSecondaryBar = (Toolbar) findViewById(R.id.toolbar2);

        setColors();
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

    public void onPreviousMonthClick(View v) {
    }

    public void onNextMonthClick(View v) {
    }

    public void onMonthPickerClick(View v) {
    }

    public void onReturnToCurrentMonthClick(View v) {
    }

    private void updateMonth(YearMonth month) {
    }

    private void setColors() {
        mMainBar.setBackgroundColor(mToolbarBgColor);
        mMainBar.setTitleTextColor(mTitleTextColor);
        mUpArrow.setColorFilter(mTitleTextColor, PorterDuff.Mode.SRC_ATOP);

        getSupportActionBar().setHomeAsUpIndicator(mUpArrow);
        getWindow().setStatusBarColor(ColorUtil.getDarkVariant(mToolbarBgColor));

        mSecondaryBar.setBackgroundColor(mToolbarBgColor);
    }
}
