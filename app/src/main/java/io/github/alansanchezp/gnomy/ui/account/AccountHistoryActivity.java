package io.github.alansanchezp.gnomy.ui.account;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import io.github.alansanchezp.gnomy.R;

public class AccountHistoryActivity extends AppCompatActivity {

    protected Toolbar mToolbar;
    protected Drawable mUpArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_history);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUpArrow = getResources().getDrawable(R.drawable.abc_vector_test);
    }

    public void onPreviousMonthClick(View v) {
    }

    public void onNextMonthClick(View v) {
    }

    public void onMonthPickerClick(View v) {
    }

    public void onReturnToCurrentMonthClick(View v) {
    }
}
