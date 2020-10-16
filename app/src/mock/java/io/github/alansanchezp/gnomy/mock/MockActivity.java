package io.github.alansanchezp.gnomy.mock;

import androidx.appcompat.app.AppCompatActivity;
import io.github.alansanchezp.gnomy.R;
;
import android.os.Bundle;
import android.util.Log;

public class MockActivity extends AppCompatActivity {
    public static String EXTRA_LAYOUT_TAG = "LAYOUT_RES_ID";
    private int layoutId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            layoutId = getIntent().getIntExtra(EXTRA_LAYOUT_TAG, R.layout.activity_main);
            setContentView(layoutId);
        } catch (Exception e) {
            setContentView(R.layout.activity_main);
            Log.e("MockActivity", "onCreate: ", e);
        }
    }
}