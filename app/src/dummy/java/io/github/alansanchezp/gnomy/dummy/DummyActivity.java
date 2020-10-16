package io.github.alansanchezp.gnomy.dummy;

import androidx.appcompat.app.AppCompatActivity;
import io.github.alansanchezp.gnomy.R;
;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

public class DummyActivity extends AppCompatActivity {
    public static String EXTRA_LAYOUT_TAG = "LAYOUT_RES_ID";
    private int layoutId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            layoutId = getIntent().getIntExtra(EXTRA_LAYOUT_TAG, R.layout.activity_main);
            setContentView(layoutId);
        } catch (Resources.NotFoundException nfe) {
            Log.w("DummyActivity", "onCreate: Fallback to empty activity layout", nfe);
            setContentView(R.layout.d_activity_empty);
        }
    }
}