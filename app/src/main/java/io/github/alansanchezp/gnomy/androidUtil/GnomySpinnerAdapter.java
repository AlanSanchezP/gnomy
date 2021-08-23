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
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import io.github.alansanchezp.gnomy.R;
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
        super(context, 0, objects);
    }

    @Override
    public long getItemId(int position) {
        return Objects.requireNonNull(getItem(position)).getId();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // TODO: Why did ViewBinding not work? Should attempt to bring it back?
        final I item = Objects.requireNonNull(getItem(position));
        final View view;
        final TextView text;
        Drawable icon;
        ImageView imageView;

        if (convertView == null) {
            view = LayoutInflater.from(getContext())
                    .inflate(R.layout.layout_gnomy_spinner_item, parent, false);
        } else {
            view = convertView;
        }

        text = view.findViewById(R.id.spinner_item_text);
        text.setText(item.toString());
        imageView = view.findViewById(R.id.spinner_item_drawable);

        icon = getDrawable(getContext(), item);
        if (icon != null) {
            imageView.setImageDrawable(icon);
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
        }

        view.setTag(this);

        return view;
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

}
