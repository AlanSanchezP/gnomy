package io.github.alansanchezp.gnomy.androidUtil;

import androidx.annotation.Nullable;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.databinding.LayoutGnomySpinnerItemBinding;
import io.github.alansanchezp.gnomy.util.ISpinnerItem;

/**
 * Custom adapter that displays an element's associated icon.
 * @param <I>   Class of the spinner items.
 */
public class GnomySpinnerAdapter<I extends ISpinnerItem> extends ArrayAdapter<I> {

    /**
     * @param context   Context.
     * @param objects   List to use in the spinner.
     */
    public GnomySpinnerAdapter(@NonNull Context context, @NonNull List<I> objects) {
        super(context, R.layout.layout_gnomy_spinner_item, objects);
    }

    @Override
    public long getItemId(int position) {
        return Objects.requireNonNull(getItem(position)).getId();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutGnomySpinnerItemBinding binding = LayoutGnomySpinnerItemBinding
                    .inflate(LayoutInflater.from(getContext()), parent, false);
            SpinnerItemHolder<I> holder = new SpinnerItemHolder<>(binding, Objects.requireNonNull(getItem(position)));
            return holder.getView();
        } else {
            return convertView;
        }
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    /**
     * Gets the {@link Drawable} object of the item at the given position.
     *
     * @param position  Position in the adapter's list of objects.
     * @return          Drawable object.
     */
    public Drawable getItemDrawable(int position) {
        I item = getItem(position);
        if (item == null) return null;
        return getDrawable(getContext(), item);
    }

    /**
     * Gets the {@link Drawable} object of a given element.
     *
     * @param context   Context that contains application resources.
     * @param item      Target element.
     * @param <I>       {@link ISpinnerItem} subclass
     * @return          Drawable object.
     */
    private static <I extends ISpinnerItem> Drawable getDrawable(@NonNull Context context, @NonNull I item) {
        if (item.getDrawableResourceName() != null) {
            int iconResId = context.getResources().getIdentifier(item.getDrawableResourceName(), "drawable", context.getPackageName());
            Drawable drawable = ContextCompat.getDrawable(context, iconResId);
            Objects.requireNonNull(drawable).setColorFilter(new PorterDuffColorFilter(item.getDrawableColor(), PorterDuff.Mode.SRC_ATOP));
            return drawable;
        }
        return null;
    }

    /**
     * Helper class to hold every item's layout.
     * @param <I>   Item's class.
     */
    public static class SpinnerItemHolder<I extends ISpinnerItem> {
        protected final LayoutGnomySpinnerItemBinding $;
        protected final I item;

        public SpinnerItemHolder(@NonNull LayoutGnomySpinnerItemBinding viewBinding, @NonNull I item) {
            $ = viewBinding;
            Context context = $.getRoot().getContext();
            this.item = item;
            Drawable icon = getDrawable(context, item);
            if (icon != null) {
                $.spinnerItemDrawable.setImageDrawable(icon);
                $.spinnerItemDrawable.setVisibility(View.VISIBLE);
            } else {
                $.spinnerItemDrawable.setVisibility(View.GONE);
            }
            $.spinnerItemText.setText(item.toString());

            $.getRoot().setTag(this);
        }

        public View getView() {
            return $.getRoot();
        }
    }

}
