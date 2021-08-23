package io.github.alansanchezp.gnomy.viewmodel.category;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import io.github.alansanchezp.gnomy.data.RepositoryBuilder;
import io.github.alansanchezp.gnomy.data.category.Category;
import io.github.alansanchezp.gnomy.data.category.CategoryRepository;
import io.reactivex.Single;

public class CategoriesListViewModel extends AndroidViewModel {
    private static final String TAG_TARGET_TO_DELETE = "CategoriesListVM.IdToDelete";
    private final CategoryRepository mRepository;
    private final SavedStateHandle mState;
    private LiveData<List<Category>> mIncomeCategories,
            mExpenseCategories,
            mBothCategories;

    public CategoriesListViewModel(@NonNull Application application,
                                   SavedStateHandle savedStateHandle) {
        super(application);
        mRepository = RepositoryBuilder.getRepository(CategoryRepository.class, application);
        mState = savedStateHandle;
    }

    public LiveData<List<Category>> getExpenseCategories(){
        if (mExpenseCategories == null) {
            mExpenseCategories = mRepository.getByStrictCategory(Category.EXPENSE_CATEGORY);
        }
        return mExpenseCategories;
    }

    public LiveData<List<Category>> getIncomeCategories(){
        if (mIncomeCategories == null) {
            mIncomeCategories = mRepository.getByStrictCategory(Category.INCOME_CATEGORY);
        }
        return mIncomeCategories;
    }

    public LiveData<List<Category>> getBothCategories(){
        if (mBothCategories == null) {
            mBothCategories = mRepository.getByStrictCategory(Category.SHARED_CATEGORY);
        }
        return mBothCategories;
    }

    public Single<Integer> delete(int categoryId) {
        return mRepository.delete(categoryId);
    }

    /**** Helper methods for dialogs to confirm operations over some category. ****/

    public int getTargetIdToDelete() {
        if (mState.get(TAG_TARGET_TO_DELETE) == null) {
            setTargetIdToDelete(0);
        }
        return mState.get(TAG_TARGET_TO_DELETE);
    }

    public void setTargetIdToDelete(int targetId) {
        mState.set(TAG_TARGET_TO_DELETE, targetId);
    }
}
