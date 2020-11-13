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
import io.github.alansanchezp.gnomy.ui.GnomyFragmentFactory.GnomyFragmentInterface;
import io.reactivex.disposables.CompositeDisposable;

public abstract class GnomyActivity
        extends AppCompatActivity
        implements ConfirmationDialogFragment.OnConfirmationDialogListener {

    protected Toolbar mAppbar;
    protected Menu mMenu;
    protected int mThemeColor;
    protected int mThemeTextColor;
    protected final CompositeDisposable mCompositeDisposable
            = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportFragmentManager().setFragmentFactory(
                new GnomyFragmentFactory(getInterfacesMapping()));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }

    protected Map<Class<? extends Fragment>, GnomyFragmentInterface>
    getInterfacesMapping() {
        Map<Class<? extends Fragment>, GnomyFragmentInterface>
                interfacesMapping = new HashMap<>();
        interfacesMapping.put(
                ConfirmationDialogFragment.class, this);
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
    public void onConfirmationDialogCancel(DialogInterface dialog, String dialogTag) {
        // @Override to implement custom actions
    }
    @Override
    public void onConfirmationDialogDismiss(DialogInterface dialog, String dialogTag) {
        // @Override to implement custom actions
    }

    protected abstract int getLayoutResourceId();
}
