package io.github.alansanchezp.gnomy.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.viewbinding.ViewBinding;
import io.github.alansanchezp.gnomy.R;
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
    protected B $;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportFragmentManager().setFragmentFactory(getFragmentFactory());
        super.onCreate(savedInstanceState);
        try {
            Class<B> clazz = getBindingClass();
            Method inflate = clazz.getMethod("inflate", LayoutInflater.class);
            //noinspection unchecked
            $ = (B) inflate.invoke(null, getLayoutInflater());
            View v = Objects.requireNonNull($).getRoot();
            setContentView(v);
        } catch (NullPointerException |
                NoSuchMethodException |
                IllegalAccessException |
                InvocationTargetException e) {
            Log.w("GnomyActivity", "onCreate: Failed to initialize ViewBinding object. ", e);
            if (getLayoutResourceId() == null) throw new IllegalStateException("No layout id was provided as an alternative to view binding.");
            setContentView(getLayoutResourceId());
        }

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

    /**
     * Returns a layout resource id to use only when view binding is,
     * for some reason, not desired in the concrete Activity.
     *
     * @return  Layout id to use.
     */
    protected Integer getLayoutResourceId() {
        return null;
    }

    private Class<B> getBindingClass() {
        Class<B> result = null;
        Type type = this.getClass().getGenericSuperclass();

        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Type[] fieldArgTypes = pt.getActualTypeArguments();
            //noinspection unchecked
            result = (Class<B>) fieldArgTypes[0];
        }
        return result;
    }
}
