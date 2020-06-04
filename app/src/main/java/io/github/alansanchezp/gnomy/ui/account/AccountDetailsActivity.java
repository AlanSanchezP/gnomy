package io.github.alansanchezp.gnomy.ui.account;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.util.ColorUtil;

public class AccountDetailsActivity extends AppCompatActivity {
    static final String EXTRA_ID = "account_id";
    static final String EXTRA_BGCOLOR = "bg_color";
    protected int mBgColor;
    protected int mTextColor;
    protected Toolbar mToolbar;
    protected Drawable mUpArrow;
    protected String mActivityTitle;
    protected int mAccountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUpArrow = getResources().getDrawable(R.drawable.abc_vector_test);

        Intent intent = getIntent();
        mAccountId = intent.getIntExtra(EXTRA_ID, 0);
        mBgColor = intent.getIntExtra(EXTRA_BGCOLOR, 0XFF);
        mTextColor = ColorUtil.getTextColor(mBgColor);

        setColors();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account_details_menu, menu);
        menu.findItem(R.id.action_account_actions)
                .getIcon()
                .setTint(mTextColor);
        return super.onCreateOptionsMenu(menu);
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

    private void setColors() {
        mToolbar.setBackgroundColor(mBgColor);
        mToolbar.setTitleTextColor(mTextColor);
        mUpArrow.setColorFilter(mTextColor, PorterDuff.Mode.SRC_ATOP);

        getSupportActionBar().setHomeAsUpIndicator(mUpArrow);
        getWindow().setStatusBarColor(ColorUtil.getDarkVariant(mBgColor));

        LinearLayout container = (LinearLayout) findViewById(R.id.account_details_container);
        container.setBackgroundColor(mBgColor);
    }
}
