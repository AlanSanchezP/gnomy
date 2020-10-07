package io.github.alansanchezp.gnomy.mock;

import androidx.appcompat.app.AppCompatActivity;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.ui.MonthToolbarView;

import android.os.Bundle;

import java.time.YearMonth;

public class MockMonthToolbarActivity extends AppCompatActivity {
    private YearMonth month = YearMonth.now();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MonthToolbarView mtv = (MonthToolbarView) findViewById(R.id.monthtoolbar);
        mtv.setMonth(month);
        mtv.setListeners(new MonthToolbarView.MonthToolbarClickListener() {
            @Override
            public void onPreviousMonthClick() {
                month = month.minusMonths(1);
                mtv.setMonth(month);
            }

            @Override
            public void onNextMonthClick() {
                month = month.plusMonths(1);
                mtv.setMonth(month);
            }

            @Override
            public void onReturnToCurrentMonthClick() {
                month = YearMonth.now();
                mtv.setMonth(month);
            }

            @Override
            public YearMonth onGetYearMonth() {
                return month;
            }

            @Override
            public void onDateSet(int year, int monthOfYear) {
                month = YearMonth.of(year, monthOfYear+1);
                mtv.setMonth(month);
            }
        });
    }
}