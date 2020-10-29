package io.github.alansanchezp.gnomy.ui;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;

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
        CustomDialogFragmentInterface interfaceToAttach = mClassToInterfaceMapping.get(loadFragmentClass(classLoader, className));
        Fragment instance;
        if (interfaceToAttach == null) {
            instance = super.instantiate(classLoader, className);
        } else if (interfaceToAttach instanceof ConfirmationDialogFragment.OnConfirmationDialogListener) {
            instance = new ConfirmationDialogFragment((ConfirmationDialogFragment.OnConfirmationDialogListener) interfaceToAttach);
        } else {
            throw new UnsupportedOperationException("No interface was provided for the requested DialogFragment class.");
        }
        return instance;
    }
}
