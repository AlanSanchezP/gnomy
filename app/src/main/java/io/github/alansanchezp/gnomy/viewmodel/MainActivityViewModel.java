package io.github.alansanchezp.gnomy.viewmodel;

import android.app.Application;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Observable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import io.github.alansanchezp.gnomy.ui.MainNavigationFragment;
import io.github.alansanchezp.gnomy.viewmodel.customView.MonthToolbarViewModel;

public class MainActivityViewModel extends MonthToolbarViewModel {
    private static final String TAG_THEME_COLOR = "MainActivityVM.ThemeColor";
    private static final String TAG_SHOW_OPTIONAL_ELEMENTS = "MainActivityVM.ShowOptionalElements";
    private static final String TAG_ACTIVITY_TITLE = "MainActivityVM.ActivityTitle";
    private final SavedStateHandle mState;
    private final ClickObservable mFABClick;

    public MainActivityViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application, savedStateHandle);
        mState = savedStateHandle;
        mFABClick = new ClickObservable();
    }

    public LiveData<Integer> getThemeToUse() {
        return mState.getLiveData(TAG_THEME_COLOR);
    }

    public void changeThemeColor(@ColorInt int color) {
        mState.set(TAG_THEME_COLOR, color);
    }

    public LiveData<Boolean> getShowOptionalNavigationElements() {
        return mState.getLiveData(TAG_SHOW_OPTIONAL_ELEMENTS);
    }

    public void toggleOptionalNavigationElements(boolean showOptionalElements) {
        mState.set(TAG_SHOW_OPTIONAL_ELEMENTS, showOptionalElements);
    }

    public LiveData<String> getTitle() {
        return mState.getLiveData(TAG_ACTIVITY_TITLE);
    }

    public void changeTitle(String title) {
        mState.set(TAG_ACTIVITY_TITLE, title);
    }

    public void observeFAB(MainNavigationFragment observer) {
        mFABClick.addObserver(observer);
    }

    public void removeFABObserver(MainNavigationFragment observer) {
        mFABClick.deleteObserver(observer);
    }

    public void notifyFABClick(FloatingActionButton fab) {
        mFABClick.click(fab);
    }

    private static class ClickObservable extends Observable {
        public void click(@NonNull FloatingActionButton button) {
            setChanged();
            notifyObservers(button);
        }
    }
}
