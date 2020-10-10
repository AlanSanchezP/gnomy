package io.github.alansanchezp.gnomy.mock;

import androidx.appcompat.app.AppCompatActivity;
import io.github.alansanchezp.gnomy.R;
import android.os.Bundle;

public class MockMonthToolbarActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}