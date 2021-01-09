package io.github.alansanchezp.gnomy.androidUtil;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Simple wrapper class for {@link TextWatcher} interface
 * that allows to declare empty methods when assigning
 * {@link com.google.android.material.textfield.TextInputEditText#addTextChangedListener(TextWatcher)}
 * listeners, and allowing the use of lambda statements and expressions.
 */
public class SimpleTextWatcherWrapper {
    /**
     * Adds a listener that only specifies {@link TextWatcher#onTextChanged(CharSequence, int, int, int)} method.
     *
     * @param listener  Listener to be called
     * @return          TextWatcher instance containing the listener
     */
    public static TextWatcher onlyOnTextChanged(OnTextChangedListener listener) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                listener.onTextChanged(s,start,before,count);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    /**
     * Bridge interface that allows the use of lambda statements and expressions.
     * Equivalent to {@link TextWatcher#onTextChanged(CharSequence, int, int, int)}
     */
    public interface OnTextChangedListener {
        void onTextChanged(CharSequence s, int start, int before, int count);
    }
}
