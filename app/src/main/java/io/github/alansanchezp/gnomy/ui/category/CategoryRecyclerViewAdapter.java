package io.github.alansanchezp.gnomy.ui.category;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.data.category.Category;
import io.github.alansanchezp.gnomy.databinding.LayoutCategoryCardBinding;
import io.github.alansanchezp.gnomy.util.ColorUtil;

/**
 * {@link RecyclerView.Adapter} that can display a {@link io.github.alansanchezp.gnomy.data.category.Category} and makes a call to the
 * specified {@link OnListItemInteractionListener}.
 */
public class CategoryRecyclerViewAdapter extends RecyclerView.Adapter<CategoryRecyclerViewAdapter.ViewHolder> {

    private List<Category> mValues;
    private final OnListItemInteractionListener mListener;
    private boolean mAllowClicks = true;

    public CategoryRecyclerViewAdapter(OnListItemInteractionListener listener) {
        mListener = listener;
    }

    public void setValues(List<Category> accumulates) {
        mValues = accumulates;
        notifyDataSetChanged();
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
        LayoutCategoryCardBinding viewBinding = LayoutCategoryCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(viewBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (mValues != null) {
            Category category = mValues.get(position);
            holder.setCategoryData(category);
            // ClickDisablerInterface is needed: SingleClickViewHolder cannot be used here
            //  because blocked actions go beyond individual views scope.
            holder.setEventListeners(mListener, new ClickDisablerInterface() {
                @Override
                public void disableClicks() {
                    CategoryRecyclerViewAdapter.this.disableClicks();
                }

                @Override
                public boolean clicksEnabled() {
                    return mAllowClicks;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (mValues != null)
            return mValues.size();
        else return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private Category mItem;
        private final PopupMenu popup;
        private final LayoutCategoryCardBinding $;

        public ViewHolder(LayoutCategoryCardBinding viewBinding) {
            super(viewBinding.getRoot());
            $ = viewBinding;

            popup = new PopupMenu($.getRoot().getContext(), $.categoryCardButton);
            popup.inflate(R.menu.category_card);
        }

        public void setCategoryData(@NonNull Category category) {
            Context context = $.getRoot().getContext();
            mItem = category;
            $.categoryCardName.setText(mItem.getName());

            if (mItem.isDeletable()) {
                $.categoryCardButton.setVisibility(View.VISIBLE);
            } else {
                $.categoryCardButton.setVisibility(View.GONE);
            }

            int categoryColor = mItem.getBackgroundColor();
            int iconColor = ColorUtil.getTextColor(categoryColor);
            int iconResId = context.getResources().getIdentifier(
                    mItem.getDrawableResourceName(), "drawable", context.getPackageName());
            Drawable icon = ContextCompat.getDrawable(context, iconResId);
            ((GradientDrawable) $.categoryCardIcon.getBackground()).setColor(categoryColor);
            $.categoryCardIcon.setImageDrawable(icon);
            $.categoryCardIcon.setColorFilter(iconColor);
            $.categoryCardIcon.setTag(iconResId);
        }

        private void setEventListeners(OnListItemInteractionListener listener,
                                       ClickDisablerInterface clickInterface) {
            // TODO: Find a way to test if clicks are effectively disabled
            $.getRoot().setOnClickListener(v -> {
                if (clickInterface.clicksEnabled()) {
                    clickInterface.disableClicks();
                    listener.onItemInteraction(mItem);
                }
            });

            popup.setOnMenuItemClickListener(item -> {
                if (clickInterface.clicksEnabled()) {
                    clickInterface.disableClicks();
                    return listener.onItemMenuItemInteraction(mItem, item);
                }
                return false;
            });
            $.categoryCardButton.setOnClickListener(v -> popup.show());
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
    public interface OnListItemInteractionListener {
        void onItemInteraction(Category category);
        boolean onItemMenuItemInteraction(Category category, MenuItem menuItem);
    }

    private interface ClickDisablerInterface {
        void disableClicks();
        boolean clicksEnabled();
    }
}
