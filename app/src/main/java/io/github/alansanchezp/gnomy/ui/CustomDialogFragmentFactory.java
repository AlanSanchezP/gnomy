package io.github.alansanchezp.gnomy.ui;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import io.github.alansanchezp.gnomy.ui.account.ArchivedAccountsDialogFragment;

public class CustomDialogFragmentFactory extends FragmentFactory {
    private Map<Class<? extends Fragment>, CustomDialogFragmentInterface> mClassToInterfaceMapping;

    public interface CustomDialogFragmentInterface {
    }

    public CustomDialogFragmentFactory(Map<Class<? extends Fragment>, CustomDialogFragmentInterface> classToInterfaceMapping) {
        super();
        mClassToInterfaceMapping = classToInterfaceMapping;
    }

    @NonNull
    @Override
    public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
        Fragment instance;
        Class<? extends Fragment> fragmentClass
                = loadFragmentClass(classLoader, className);
        CustomDialogFragmentInterface interfaceToAttach
                = mClassToInterfaceMapping.get(loadFragmentClass(classLoader, className));

        if (interfaceToAttach == null) {
            instance = super.instantiate(classLoader, className);
        } else if (fragmentClass.equals(ArchivedAccountsDialogFragment.class)) {
            instance = new ArchivedAccountsDialogFragment((ArchivedAccountsDialogFragment.ArchivedAccountsDialogInterface) interfaceToAttach);
        } else if (fragmentClass.equals(ConfirmationDialogFragment.class)) {
            instance = new ConfirmationDialogFragment((ConfirmationDialogFragment.OnConfirmationDialogListener) interfaceToAttach);
        } else {
            throw new UnsupportedOperationException("No interface was provided for the requested DialogFragment class.");
        }

        return instance;
    }
}
