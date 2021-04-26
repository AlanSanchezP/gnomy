package io.github.alansanchezp.gnomy.ui.category;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import io.github.alansanchezp.gnomy.data.category.Category;
import io.github.alansanchezp.gnomy.databinding.LayoutIconPickerItemBinding;

import static android.graphics.Color.TRANSPARENT;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Category} and makes a call to the
 * specified {@link OnIconSelectedListener}.
 */
public class CategoryIconsRecyclerViewAdapter extends RecyclerView.Adapter<CategoryIconsRecyclerViewAdapter.ViewHolder> {

    private final OnIconSelectedListener mListener;
    // TODO: Update with final set of icons
    private final static String[] ICON_RES_NAMES = {
      "ic_minus_24",
      "ic_add_black_24dp",
      "ic_plus_and_minus",
      "ic_calculate_24"
    };
    public final static String DEFAULT_RES_NAME = ICON_RES_NAMES[0];
    private boolean mAllowClicks = true;
    private int mColor = 1;
    private int mSelectedItemIndex = -1;
    private boolean mHasBeenInit = false;

    public CategoryIconsRecyclerViewAdapter(OnIconSelectedListener listener) {
        mListener = listener;
    }

    private void onItemSelected(int position) {
        if (position == mSelectedItemIndex) return;
        int oldSelectedItemIndex = mSelectedItemIndex;
        mSelectedItemIndex = position;
        this.notifyItemChanged(oldSelectedItemIndex);
    }

    public void alterColor(int color) {
        if (color == 1 || mColor == color) return;
        mColor = color;
        this.notifyItemChanged(mSelectedItemIndex);
    }

    public void setInitialIcon(String itemResName) {
        if (mHasBeenInit) return;
        int index = -1;
        for (int i = 0; i < ICON_RES_NAMES.length; i++) {
            if (ICON_RES_NAMES[i].equals(itemResName)) {
                index = i;
                break;
            }
        }
        if (index != 1) {
            onItemSelected(index);
            mHasBeenInit = true;
        }
    }

    public void enableClicks() {
        mAllowClicks = true;
    }

    public void disableClicks() {
        mAllowClicks = false;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutIconPickerItemBinding viewBinding = LayoutIconPickerItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(viewBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        String iconResName = ICON_RES_NAMES[position];
        holder.setRecyclerViewInterface(new ParentChildInterface() {
            @Override
            public boolean clicksEnabled() {
                return mAllowClicks;
            }

            @Override
            public int getColor() {
                return mColor;
            }

            @Override
            public boolean isSelected(int position) {
                return position == mSelectedItemIndex;
            }
        });
        holder.setIconData(iconResName);
        // ClickDisablerInterface is needed: SingleClickViewHolder cannot be used here
        //  because blocked actions go beyond individual views scope.
        holder.setEventListeners(iconResName1 -> {
            if (mAllowClicks) CategoryIconsRecyclerViewAdapter.this.onItemSelected(position);
            mListener.onIconSelected(iconResName1);
        });
    }

    @Override
    public int getItemCount() {
        return ICON_RES_NAMES.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ParentChildInterface mInterface;
        private String mIconResName;
        private final LayoutIconPickerItemBinding $;

        public ViewHolder(LayoutIconPickerItemBinding viewBinding) {
            super(viewBinding.getRoot());
            $ = viewBinding;
        }

        private void setRecyclerViewInterface(ParentChildInterface _interface) {
            mInterface = _interface;
        }

        public void setIconData(@NonNull String iconResName) {
            Context context = $.getRoot().getContext();
            mIconResName = iconResName;
            int iconResId = context.getResources().getIdentifier(
                    iconResName, "drawable", context.getPackageName());
            Drawable icon = ContextCompat.getDrawable(context, iconResId);
            $.iconPickerItem.setImageDrawable(icon);
            $.iconPickerItem.setTag(iconResId);
            if (mInterface != null) {
                if (mInterface.isSelected(getAdapterPosition())) tintBorder(mInterface.getColor());
                else tintBorder(TRANSPARENT);
            }
        }

        private void tintBorder(int color) {
            ((GradientDrawable) $.iconPickerItemBackground.getBackground()).setStroke(8, color);
        }

        private void setEventListeners(OnIconSelectedListener listener) {
            // TODO: Find a way to test if clicks are effectively disabled
            $.getRoot().setOnClickListener(v -> {
                if (mInterface.clicksEnabled()) {
                    tintBorder(mInterface.getColor());
                    listener.onIconSelected(mIconResName);
                }
            });
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnIconSelectedListener {
        void onIconSelected(String iconResName);
    }

    private interface ParentChildInterface {
        boolean clicksEnabled();
        int getColor();
        boolean isSelected(int position);
    }
}
