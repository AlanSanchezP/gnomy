package io.github.alansanchezp.gnomy.ui.account;

import androidx.appcompat.app.AppCompatActivity;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.filter.InputFilterMinMax;
import io.github.alansanchezp.gnomy.util.GraphicUtil;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class NewAccountActivity extends AppCompatActivity {
    protected int bgColor;
    protected int textColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);

        bgColor = GraphicUtil.getRandomColor();
        textColor = GraphicUtil.getTextColor(bgColor);
        setColors();

        TextInputEditText valueTIET = (TextInputEditText) findViewById(R.id.new_account_initial_value_input);
        valueTIET.setFilters(new InputFilter[]{new InputFilterMinMax(Account.MIN_INITIAL, Account.MAX_INITIAL)});
    }

    protected void setColors() {
        LinearLayout container = (LinearLayout) findViewById(R.id.new_account_container);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.new_account_ok);
        TextInputLayout nameTIL = (TextInputLayout) findViewById(R.id.new_account_name);
        TextInputEditText nameTIET = (TextInputEditText) findViewById(R.id.new_account_name_input);
        TextInputLayout valueTIL = (TextInputLayout) findViewById(R.id.new_account_initial_value);
        TextInputEditText valueTIET = (TextInputEditText) findViewById(R.id.new_account_initial_value_input);
        Switch includeInSwitch = (Switch) findViewById(R.id.new_account_show_in_home);
        ImageButton palette = (ImageButton) findViewById(R.id.new_account_color_button);

        // Custom ColorStateLists
        ColorStateList switchCSL = getSwitchColorStateList(bgColor);
        ColorStateList nameCSL = getStrokeColorStateList(textColor);

        container.setBackgroundColor(bgColor);
        fab.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        fab.getDrawable().mutate().setTint(textColor);
        fab.setRippleColor(textColor);

        nameTIL.setBoxStrokeColorStateList(nameCSL);
        nameTIL.setDefaultHintTextColor(ColorStateList.valueOf(textColor));
        nameTIET.setTextColor(textColor);

        valueTIL.setBoxStrokeColor(bgColor);
        valueTIL.setHintTextColor(ColorStateList.valueOf(bgColor));

        includeInSwitch.getThumbDrawable().setTintList(switchCSL);
        includeInSwitch.getTrackDrawable().setTintList(switchCSL);

        palette.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        palette.getDrawable().mutate().setTint(textColor);
    }

    protected ColorStateList getSwitchColorStateList(int color) {
        return new ColorStateList(
            new int[][]{
                new int[]{-android.R.attr.state_enabled},
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_enabled},
                new int[]{}
            },
            new int[]{
                Color.GRAY,
                Color.LTGRAY,
                color,
                color,
            }
        );
    }

    protected ColorStateList getStrokeColorStateList(int color) {
        return new ColorStateList(
            new int[][]{
                new int[]{-android.R.attr.state_enabled},
                new int[]{
                    -android.R.attr.state_focused,
                    android.R.attr.state_focused,
                },
                new int[]{}
            },
            new int[]{
                Color.GRAY,
                color,
                color,
            }
        );
    }
}
