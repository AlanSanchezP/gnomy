package io.github.alansanchezp.gnomy.dummy;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * Helper class to test ViewHolders or custom Views in isolation, inserting them
 * into an empty {@link FrameLayout}. Use in tandem with ViewScenarioRule.
 *
 * This class expects a Class object to be provided via Intent extras
 * (using the EXTRA_HOSTED_CLASS_TAG tag). The provided class MUST be either
 * a View subclass or a ViewBinding subclass. If no Class is provided this class, or
 * if it is not a subclass of either View or ViewBinding, the onCreate method will
 * throw a RuntimeException.
 *
 * If the provided class is a View subclass, DummyActivity will create a new instance
 * of that View and insert it directly to the root layout, using the layout parameters
 * MATCH_PARENT for width and WRAP_CONTENT for height.
 *
 * If the provided class is a ViewBinding subclass, DummyActivity will generate the
 * hosted View using the ViewBinding inflate method, attaching it to the root layout.
 *
 */
public class DummyActivity extends AppCompatActivity {
    public static final String EXTRA_HOSTED_CLASS_TAG = "DummyActivity.HostedClass";
    private View hostedView;
    private ViewBinding hostedViewBinding = null;

    /**
     *
     * @param savedInstanceState    Saved Instance. Not used in this Activity.
     * @throws RuntimeException     If no class was provided or if it isn't a
     *                              View or ViewBinding subclass.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Class<?> clazz = (Class<?>) getIntent().getSerializableExtra(EXTRA_HOSTED_CLASS_TAG);
        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(rootLayout);

        if (clazz == null)
            throw new RuntimeException("No class was provided. Make sure you are passing a View or ViewBinding subclass in your Intent.");

        Class<? extends View> viewClass = null;
        Class<? extends ViewBinding> bindingClass = null;

        try {
            viewClass = clazz.asSubclass(View.class);
        } catch (ClassCastException e) {
            try {
                bindingClass = clazz.asSubclass(ViewBinding.class);
            } catch (ClassCastException ex) {
                throw new RuntimeException("Provided Class is neither a View subclass or a ViewBinding subclass.");
            }
        }

        try {
            if (bindingClass == null) { // class is a View subclass
                hostedView = viewClass.getConstructor(Context.class).newInstance(this);
                hostedView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                rootLayout.addView(hostedView);
            } else { // class is a ViewBinding subclass
                hostedViewBinding = (ViewBinding) bindingClass.getMethod("inflate", LayoutInflater.class, ViewGroup.class, Boolean.TYPE)
                        .invoke(null, getLayoutInflater(), rootLayout, true);
                hostedView = Objects.requireNonNull(hostedViewBinding).getRoot();
            }
        } catch (InflateException |
                NoSuchMethodException |
                IllegalAccessException |
                InstantiationException |
                InvocationTargetException e) {
            Log.e("DummyActivity", "onCreate: View creation failed. Ignoring.", e);
        }
    }

    /**
     * Retrieves the hosted View object of this class.
     *
     * @return      View object.
     */
    public View getHostedView() {
        return hostedView;
    }

    /**
     * Retrieves the hosted ViewBinding object of this class.
     *
     * @param <B>   Specific ViewBinding subclass to use. In order to
     *              avoid exceptions, it must match the Class value
     *              passed through EXTRA_HOSTED_CLASS_TAG Intent extra.
     * @return      ViewBinding object. Null if EXTRA_HOSTED_CLASS_TAG
     *              referenced a View subclass.
     */
    public @Nullable <B extends ViewBinding> B getHostedViewBinding() {
        //noinspection unchecked
        return (B) hostedViewBinding;
    }
}