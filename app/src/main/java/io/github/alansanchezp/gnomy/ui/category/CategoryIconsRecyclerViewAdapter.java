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
        "category_art_salary_1",
        "category_art_salary_2",
        "category_art_salary_3",
        "category_art_sale_1",
        "category_art_sale_2",
        "category_art_sale_3",
        "category_art_funding_1",
        "category_art_govsupport_1",
        "category_art_award_1",
        "category_art_gift_1",
        "category_art_gift_2",
        "category_art_refund_1",
        "category_art_misc_1",
        "category_art_house_1",
        "category_art_house_2",
        "category_art_transport_1",
        "category_art_transport_2",
        "category_art_food_1",
        "category_art_food_2",
        "category_art_food_3",
        "category_art_services_1",
        "category_art_services_2",
        "category_art_services_3",
        "category_art_services_4",
        "category_art_clothes_1",
        "category_art_clothes_2",
        "category_art_health_1",
        "category_art_health_2",
        "category_art_health_3",
        "category_art_health_4",
        "category_art_health_5",
        "category_art_household_1",
        "category_art_household_2",
        "category_art_activities_1",
        "category_art_activities_2",
        "category_art_activities_3",
        "category_art_activities_4",
        "category_art_activities_5",
        "category_art_activities_6",
        "category_art_activities_7",
        "category_art_activities_8",
        "category_art_activities_9",
        "category_art_activities_10",
        "category_art_activities_11",
        "category_art_activities_12",
        "category_art_activities_13",
        "category_art_activities_14",
        "category_art_activities_15",
        "category_art_activities_16",
        "category_art_activities_17",
        "category_art_activities_18",
        "category_art_activities_19",
        "category_art_travels_1",
        "category_art_travels_2",
        "category_art_travels_3",
        "category_art_tech_1",
        "category_art_tech_2",
        "category_art_getloan_1",
        "category_art_getloan_2",
        "category_art_payloan_1",
        "category_art_insurance_1",
        "category_art_insurance_2",
        "category_art_insurance_3",
        "category_art_insurance_4",
        "category_art_insurance_5",
        "category_art_family_1",
        "category_art_family_2",
        "category_art_family_3",
        "category_art_games_1",
        "category_art_games_2",
        "category_art_retirement_1",
        "category_art_investments_1",
        "category_art_nightjob_1",
        "category_art_informalsale_1",
        "category_art_taxes_1",
        "category_art_taxes_2",
        "category_art_tips_1",
        "category_art_school_1",
        "category_art_school_2",
        "category_art_savings_1",
        "category_art_savings_2",
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
