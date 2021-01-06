package io.github.alansanchezp.gnomy.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;

import java.util.function.Function;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.viewbinding.ViewBinding;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.androidUtil.GnomyFragmentFactory;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Base Activity to use in the application.
 *
 * @param <B>   ViewBinding class to use. If null (not recommended),
 *              a layout ID must be provided by the concrete class instead.
 */
public abstract class GnomyActivity<B extends ViewBinding>
        extends AppCompatActivity
        implements ConfirmationDialogFragment.OnConfirmationDialogListener {

    protected Toolbar mAppbar;
    protected Menu mMenu;
    protected int mThemeColor;
    protected int mThemeTextColor;
    protected final CompositeDisposable mCompositeDisposable
            = new CompositeDisposable();
    protected final Integer mMenuResourceId;
    protected final Function<LayoutInflater, B> mViewBindingInflater;
    protected final Integer mLayoutResourceId;
    protected B $;

    /**
     * Creates a new instance, initializing class fields and specifying
     * that ViewBinding will be used.
     *
     * @param menuResourceId                    Menu to use in the appbar.
     *                                          Set to null if no menu is required.
     * @param viewBindingInflater               Inflater method to retrieve the ViewBinding object.
     *                                          In order to avoid using Java reflection, this must be
     *                                          provided by each child class. Lambda method
     *                                          references can be used as follows:
     *                                          ViewBindingClass::inflate
     */
    protected GnomyActivity(@Nullable Integer menuResourceId,
                            @NonNull Function<LayoutInflater, B> viewBindingInflater) {
        super();
        mMenuResourceId = menuResourceId;
        mViewBindingInflater = viewBindingInflater;
        mLayoutResourceId = null;
    }

    /**
     * Creates a new instance, initializing class fields and specifying
     * that traditional inflater method (with R.layout.ID) will be used.
     * Not recommended, but available if needed.
     *
     * @param menuResourceId                    Menu to use in the host appbar.
     *                                          Set to null if no menu is required.
     * @param layoutResourceId                  Layout resource id that will be used to
     *                                          inflate the Fragment View hierarchy.
     */
    @SuppressWarnings("unused")
    protected GnomyActivity(@Nullable Integer menuResourceId,
                            @NonNull Integer layoutResourceId) {
        super();
        mMenuResourceId = menuResourceId;
        mLayoutResourceId = layoutResourceId;
        mViewBindingInflater = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportFragmentManager().setFragmentFactory(getFragmentFactory());
        super.onCreate(savedInstanceState);

        if (mViewBindingInflater != null) {
            $ = mViewBindingInflater.apply(getLayoutInflater());
            setContentView($.getRoot());
        } else if (mLayoutResourceId != null) {
            setContentView(mLayoutResourceId);
        } else {
            throw new RuntimeException("Activity must provide either a ViewBinding inflater or a layout resource id.");
        }

        mAppbar = findViewById(R.id.custom_appbar);
        setSupportActionBar(mAppbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        if (mMenuResourceId != null)
            getMenuInflater().inflate(mMenuResourceId, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }

    protected GnomyFragmentFactory getFragmentFactory() {
        return new GnomyFragmentFactory()
                .addMapElement(ConfirmationDialogFragment.class, this);
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

    protected void tintNavigationBar() {
        getWindow().setNavigationBarColor(ColorUtil.getDarkVariant(mThemeColor));
    }

    protected void tintMenuItems() {
        // @Override to implement custom actions
    }

    protected void tintWindowElements() {
        tintStatusBar();
        tintAppbar();
        tintNavigationBar();
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
}
