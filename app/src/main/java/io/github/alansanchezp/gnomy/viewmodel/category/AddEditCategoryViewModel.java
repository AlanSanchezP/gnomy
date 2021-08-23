package io.github.alansanchezp.gnomy.viewmodel.category;

import android.app.Application;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import io.github.alansanchezp.gnomy.data.RepositoryBuilder;
import io.github.alansanchezp.gnomy.data.category.Category;
import io.github.alansanchezp.gnomy.data.category.CategoryRepository;
import io.reactivex.Single;

public class AddEditCategoryViewModel extends AndroidViewModel {
    private static final String TAG_SELECTED_COLOR = "AddEditCategoryVM.SelectedColor";
    private static final String TAG_IS_NAME_PRISTINE = "AddEditCategoryVM.IsNamePristine";
    private final CategoryRepository mRepository;
    private final SavedStateHandle mState;
    private final MutableLiveData<Integer> mutableCategoryColor;
    // TODO: [#54] Find a better way to achieve this
    public final LiveData<Integer> categoryColor;

    public AddEditCategoryViewModel(@NonNull Application application,
                                    SavedStateHandle savedStateHandle) {
        super(application);
        mRepository = RepositoryBuilder.getRepository(CategoryRepository.class, application);
        mState = savedStateHandle;

        mutableCategoryColor = mState.getLiveData(TAG_SELECTED_COLOR, 1);
        categoryColor = mutableCategoryColor;
    }

    public LiveData<Category> getCategory(int id) {
        if (id == 0) return null;
        return mRepository.find(id);
    }

    public Single<Long> insert(Category account) {
        return mRepository.insert(account);
    }

    public Single<Integer> update(Category account) {
        return mRepository.update(account);
    }

    /**** Helper methods for UI and error management. ****/

    public void setCategoryColor(@ColorInt int color) {
        mutableCategoryColor.setValue(color);
    }

    public boolean categoryNameIsPristine() {
        if (mState.get(TAG_IS_NAME_PRISTINE) == null) return true;
        return mState.get(TAG_IS_NAME_PRISTINE);
    }

    public void notifyCategoryNameChanged() {
        mState.set(TAG_IS_NAME_PRISTINE, false);
    }
}