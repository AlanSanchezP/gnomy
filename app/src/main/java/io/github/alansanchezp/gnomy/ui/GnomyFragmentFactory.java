package io.github.alansanchezp.gnomy.ui;

import android.app.Activity;
import android.app.Dialog;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.app.DialogCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentFactory;
import io.github.alansanchezp.gnomy.ui.account.AccountsFragment;
import io.github.alansanchezp.gnomy.ui.account.ArchivedAccountsDialogFragment;
import io.github.alansanchezp.gnomy.ui.transaction.TransactionsFragment;

public class GnomyFragmentFactory extends FragmentFactory {
    private Map<Class<? extends Fragment>, Object> mClassToInterfaceMapping;

    public GnomyFragmentFactory() {
        super();
        mClassToInterfaceMapping = new HashMap<>();
    }

    public GnomyFragmentFactory addMapElement(Class<? extends Fragment> fragmentClass, Object _interface) {
        Class<?> clazz = _interface.getClass();
        Object effectiveInterface = null;
        while (true) {
            if (clazz.getInterfaces().length > 0) {
                effectiveInterface = _interface;
                break;
            } else {
                clazz = clazz.getSuperclass();
                if (clazz == null) break;
                // Reject base Android superclasses
                if (clazz.equals(Activity.class)) break;
                if (clazz.equals(Fragment.class)) break;
                if (clazz.equals(DialogFragment.class)) break;
                if (clazz.equals(AppCompatActivity.class)) break;
                if (clazz.equals(AppCompatDialogFragment.class)) break;
                if (clazz.equals(FragmentActivity.class)) break;
                if (clazz.equals(android.app.Fragment.class)) break;
                if (clazz.equals(DialogCompat.class)) break;
                if (clazz.equals(Dialog.class)) break;
            }
        }
        if (effectiveInterface == null)
            throw new RuntimeException("Provided object does not represent an interface.");

        mClassToInterfaceMapping.put(fragmentClass, _interface);
        return this;
    }

    @NonNull
    @Override
    public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
        Fragment instance;
        Class<? extends Fragment> fragmentClass
                = loadFragmentClass(classLoader, className);
        Object interfaceToAttach
                = mClassToInterfaceMapping.get(loadFragmentClass(classLoader, className));

        if (interfaceToAttach == null) {
            instance = super.instantiate(classLoader, className);
        } else if (fragmentClass.equals(DatePickerDialog.class)) {
            instance = DatePickerDialog.newInstance((DatePickerDialog.OnDateSetListener) interfaceToAttach,
                    Calendar.getInstance());
        } else if (fragmentClass.equals(TimePickerDialog.class)) {
            instance = TimePickerDialog.newInstance((TimePickerDialog.OnTimeSetListener) interfaceToAttach, false);
        } else if (fragmentClass.equals(ArchivedAccountsDialogFragment.class)) {
            instance = new ArchivedAccountsDialogFragment((ArchivedAccountsDialogFragment.ArchivedAccountsDialogInterface) interfaceToAttach);
        } else if (fragmentClass.equals(ConfirmationDialogFragment.class)) {
            instance = new ConfirmationDialogFragment((ConfirmationDialogFragment.OnConfirmationDialogListener) interfaceToAttach);
        } else if (fragmentClass.equals(AccountsFragment.class)) {
            instance = new AccountsFragment((MainNavigationFragment.MainNavigationInteractionInterface) interfaceToAttach);
        } else if(fragmentClass.equals(TransactionsFragment.class)) {
            instance = new TransactionsFragment((MainNavigationFragment.MainNavigationInteractionInterface) interfaceToAttach);
        } else {
            throw new UnsupportedOperationException("No interface was provided for the requested Fragment class.");
        }

        return instance;
    }
}
