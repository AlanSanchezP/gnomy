package io.github.alansanchezp.gnomy.ui;

import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.util.ColorUtil;

public abstract class GnomyActivity
        extends AppCompatActivity {

    protected Toolbar mAppbar;
    protected Menu mMenu;
    protected int mThemeColor;
    protected int mThemeTextColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        mAppbar = findViewById(R.id.custom_appbar);
        setSupportActionBar(mAppbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    protected void setThemeColor(@ColorInt int themeColor) {
        mThemeColor = themeColor;
        mThemeTextColor = ColorUtil.getTextColor(themeColor);
        tintWindowElements();
    }

    protected void tintStatusBar() {
        getWindow().setStatusBarColor(ColorUtil.getDarkVariant(mThemeColor));
    }

    protected void tintAppbar() {
        mAppbar.setBackgroundColor(mThemeColor);
        mAppbar.setTitleTextColor(mThemeTextColor);
        tintMenuItems();
    }

    protected void tintMenuItems() {
        // @Override to implement custom actions
    }

    protected void tintWindowElements() {
        tintStatusBar();
        tintAppbar();
    }

    protected void disableActions() {
        // @Override to implement custom actions
    }

    protected void enableActions() {
        // @Override to implement custom actions
    }

    protected abstract int getLayoutResourceId();
}
