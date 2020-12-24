package io.github.alansanchezp.gnomy.dummy;

import androidx.appcompat.app.AppCompatActivity;
import io.github.alansanchezp.gnomy.R;

import android.os.Bundle;

public class DummyActivity extends AppCompatActivity {
    public static final String EXTRA_LAYOUT_TAG = "LAYOUT_RES_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layoutId = getIntent().getIntExtra(EXTRA_LAYOUT_TAG, R.layout.activity_main);

        // Keeping as legacy support. Views are expected to be manually inflated by Test class.
        if (layoutId == R.layout.d_activity_month_toolbar)
            setContentView(layoutId);
        else
            setContentView(R.layout.d_activity_empty);
    }
}