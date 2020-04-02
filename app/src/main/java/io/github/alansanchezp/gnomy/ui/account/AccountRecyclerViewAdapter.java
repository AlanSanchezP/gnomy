package io.github.alansanchezp.gnomy.ui.account;

import io.github.alansanchezp.gnomy.R;

import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.ui.account.AccountsFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Account} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class AccountRecyclerViewAdapter extends RecyclerView.Adapter<AccountRecyclerViewAdapter.ViewHolder> {

    private List<Account> mValues;
    private final OnListFragmentInteractionListener mListener;

    public AccountRecyclerViewAdapter(OnListFragmentInteractionListener listener) {
        mListener = listener;
    }

    public void setValues(List<Account> accounts) {
        mValues = accounts;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_account_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (mValues != null) {
            // TODO handle currency symbol and decimal separator
            holder.mItem = mValues.get(position);
            holder.mNameView.setText(holder.mItem.getName());
            holder.mCurrentView.setText(holder.mItem.getInitialValue().toString());
            holder.mProjectedView.setText(holder.mItem.getInitialValue().toString());
            Drawable background = holder.mIconView.getBackground();


            // TODO Logic for icon color
            // check https://stackoverflow.com/questions/1855884/determine-font-color-based-on-background-color

            // TODO make this an util or integrate it on Account class
            String colorString = String.format("#%06X", (0xFFFFFF & mValues.get(position).getBackgroundColor()));
            if (background instanceof ShapeDrawable) {
                // cast to 'ShapeDrawable'
                ShapeDrawable shapeDrawable = (ShapeDrawable) background;
                shapeDrawable.getPaint().setColor(Color.parseColor(colorString));
            } else if (background instanceof GradientDrawable) {
                // cast to 'GradientDrawable'
                GradientDrawable gradientDrawable = (GradientDrawable) background;
                gradientDrawable.setColor(Color.parseColor(colorString));
            } else if (background instanceof ColorDrawable) {
                // alpha value may need to be set again after this call
                ColorDrawable colorDrawable = (ColorDrawable) background;
                colorDrawable.setColor(Color.parseColor(colorString));
            }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.
                        mListener.onListFragmentInteraction(holder.mItem);
                    }
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mCurrentView;
        public final TextView mProjectedView;
        public final ImageView mIconView;
        public Account mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.account_card_name);
            mCurrentView = (TextView) view.findViewById(R.id.account_card_current);
            mProjectedView = (TextView) view.findViewById(R.id.account_card_projected);
            mIconView = (ImageView) view.findViewById(R.id.account_card_icon);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
