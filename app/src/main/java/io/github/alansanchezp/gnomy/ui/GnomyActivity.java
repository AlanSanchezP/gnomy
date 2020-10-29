package io.github.alansanchezp.gnomy.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.ui.CustomDialogFragmentFactory.CustomDialogFragmentInterface;

public abstract class GnomyActivity
        extends AppCompatActivity
        implements ConfirmationDialogFragment.OnConfirmationDialogListener {

    protected Toolbar mAppbar;
    protected Menu mMenu;
    protected int mThemeColor;
    protected int mThemeTextColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportFragmentManager().setFragmentFactory(
                new CustomDialogFragmentFactory(getInterfacesMapping()));
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

    protected Map<Class<? extends Fragment>,CustomDialogFragmentInterface>
    getInterfacesMapping() {
        Map<Class<? extends Fragment>, CustomDialogFragmentInterface>
                interfacesMapping = new HashMap<>();
        interfacesMapping.put(
                ConfirmationDialogFragment.class,
                (ConfirmationDialogFragment.OnConfirmationDialogListener) this);
        return interfacesMapping;
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

    @Override
    public void onConfirmationDialogYes(DialogInterface dialog, String dialogTag, int which){
        // @Override to implement custom actions
    }
    @Override
    public void onConfirmationDialogNo(DialogInterface dialog, String dialogTag, int which){
        // @Override to implement custom actions
    }
    @Override
    public void onConfirmationDialogDismiss(DialogInterface dialog, String dialogTag) {
        // @Override to implement custom actions
    }

    protected abstract int getLayoutResourceId();
}
