package io.github.alansanchezp.gnomy.ui.category;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.androidUtil.GnomyFragmentFactory;
import io.github.alansanchezp.gnomy.data.category.Category;
import io.github.alansanchezp.gnomy.databinding.FragmentCategoriesBinding;
import io.github.alansanchezp.gnomy.ui.ConfirmationDialogFragment;
import io.github.alansanchezp.gnomy.viewmodel.category.CategoriesListViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CategoriesFragment
        extends Fragment
        implements CategoryRecyclerViewAdapter.OnListItemInteractionListener,
                    ConfirmationDialogFragment.OnConfirmationDialogListener {

    public static final String ARG_CATEGORY_TYPE = "CategoriesFragment.CategoryType";
    public static final String TAG_DELETE_CATEGORY_DIALOG = "CategoriesFragment.DeleteCategoryDialog";
    protected final CompositeDisposable mCompositeDisposable
            = new CompositeDisposable();
    private CategoryRecyclerViewAdapter mAdapter;
    private CategoriesListViewModel mListViewModel;
    private int categoriesType = Category.BOTH_CATEGORY;
    private FragmentCategoriesBinding $;

    private GnomyFragmentFactory getFragmentFactory() {
        return new GnomyFragmentFactory()
                .addMapElement(ConfirmationDialogFragment.class, this);
    }

    /* ANDROID LIFECYCLE METHODS */

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        CategoryRecyclerViewAdapter.OnListItemInteractionListener listener = this;
        mAdapter = new CategoryRecyclerViewAdapter(listener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getChildFragmentManager().setFragmentFactory(getFragmentFactory());
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            categoriesType = args.getInt(ARG_CATEGORY_TYPE, Category.BOTH_CATEGORY);
        }
        mListViewModel = new ViewModelProvider(this)
                .get(CategoriesListViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        $ = FragmentCategoriesBinding.inflate(inflater, container, false);
        return $.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = $.itemsList;
        recyclerView.setLayoutManager(new LinearLayoutManager($.getRoot().getContext()));
        recyclerView.setAdapter(mAdapter);
        recyclerView.setNestedScrollingEnabled(false);

        if (categoriesType == Category.EXPENSE_CATEGORY)
            mListViewModel.getExpenseCategories().observe(getViewLifecycleOwner(), this::onCategoriesListChanged);
        else if (categoriesType == Category.INCOME_CATEGORY)
            mListViewModel.getIncomeCategories().observe(getViewLifecycleOwner(), this::onCategoriesListChanged);
        else if (categoriesType == Category.BOTH_CATEGORY)
            mListViewModel.getBothCategories().observe(getViewLifecycleOwner(), this::onCategoriesListChanged);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.enableClicks();
    }

    /* CONCRETE LISTENERS INHERITED FROM ABSTRACT CLASS */
    private void onCategoriesListChanged(List<Category> categories) {
        // TODO: Display some helpful information if list is empty, current behavior
        //  just doesn't display any data in recyclerview (not a bug, but UX can be improved)
        mAdapter.setValues(categories);
    }

    /* INTERFACE METHODS */

    public void onItemInteraction(Category category) {
        Intent editCategoryIntent = new Intent(getContext(), AddEditCategoryActivity.class);
        editCategoryIntent.putExtra(AddEditCategoryActivity.EXTRA_CATEGORY_ID, category.getId());

        requireActivity().startActivity(editCategoryIntent);
    }

    public boolean onItemMenuItemInteraction(final Category category, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.category_card_modify:
                onItemInteraction(category);
                break;
            case R.id.category_card_delete:
                deleteCategory(category);
                break;
            default:
                mAdapter.enableClicks();
                return false;
        }

        return true;
    }

    /* FRAGMENT-SPECIFIC METHODS */

    public void deleteCategory(Category account) {
        FragmentManager fm = getChildFragmentManager();
        if (fm.findFragmentByTag(TAG_DELETE_CATEGORY_DIALOG) != null) return;
        mListViewModel.setTargetIdToDelete(account.getId());
        ConfirmationDialogFragment dialog = (ConfirmationDialogFragment)
                fm.getFragmentFactory().instantiate(
                        requireContext().getClassLoader(), ConfirmationDialogFragment.class.getName());
        Bundle args = new Bundle();
        args.putString(ConfirmationDialogFragment.ARG_TITLE, getString(R.string.category_card_delete_modal));
        args.putString(ConfirmationDialogFragment.ARG_MESSAGE, getString(R.string.category_card_delete_warning));
        dialog.setArguments(args);
        dialog.show(fm, TAG_DELETE_CATEGORY_DIALOG);
    }

    private void effectiveDeleteCategory(Category category) {
        mCompositeDisposable.add(
                mListViewModel.delete(category.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                integer ->
                                    mListViewModel.setTargetIdToDelete(0),
                                throwable ->
                                    Toast.makeText(getContext(), R.string.generic_data_error, Toast.LENGTH_LONG).show()));
    }

    @Override
    public void onConfirmationDialogYes(DialogInterface dialog, String dialogTag, int which) {
        if (dialogTag.equals(TAG_DELETE_CATEGORY_DIALOG)) {
            int idToDelete = mListViewModel.getTargetIdToDelete();
            if (idToDelete == 0)  {
                Log.wtf("CategoriesFragment", "onConfirmationDialogYes: Trying to delete null object.");
                return;
            }
            effectiveDeleteCategory(new Category(idToDelete));
        }

    }

    @Override
    public void onConfirmationDialogNo(DialogInterface dialog, String dialogTag, int which) {
        if (dialogTag.equals(TAG_DELETE_CATEGORY_DIALOG)) {
            mListViewModel.setTargetIdToDelete(0);
        }
    }

    @Override
    public void onConfirmationDialogCancel(DialogInterface dialog, String dialogTag) {
    }

    @Override
    public void onConfirmationDialogDismiss(DialogInterface dialog, String dialogTag) {
        mAdapter.enableClicks();
    }

}