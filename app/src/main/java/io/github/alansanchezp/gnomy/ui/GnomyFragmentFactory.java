package io.github.alansanchezp.gnomy.ui;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import io.github.alansanchezp.gnomy.ui.account.ArchivedAccountsDialogFragment;
import io.github.alansanchezp.gnomy.ui.transaction.TransactionFiltersDialogFragment;

/**
 * Custom FragmentFactory that allows gnomy to handle {@link Fragment} subclasses
 * that require an interface to be provided in order to function optimally.
 *
 * Note that this class cannot handle fragment subclasses that require more than one
 * interface.
 */
public class GnomyFragmentFactory extends FragmentFactory {
    private final Map<Class<? extends Fragment>, InterfaceTuple<?>> FRAGMENT_INTERFACES_MAPPING
            = initFragmentInterfaceMapping();

    /**
     * Retrieves the map of {@link Fragment} classes and their correspondent interfaces,
     * using {@link InterfaceTuple} so that the map can store references to both the
     * interface class itself and an associated instance that implements such interface
     * that is expected to be added later.
     *
     * @return  Initial map.
     */
    private static Map<Class<? extends Fragment>, InterfaceTuple<?>> initFragmentInterfaceMapping() {
        Map<Class<? extends Fragment>, InterfaceTuple<?>> supportedClasses = new HashMap<>();
        supportedClasses.put(DatePickerDialog.class, new InterfaceTuple<>(DatePickerDialog.OnDateSetListener.class));
        supportedClasses.put(TimePickerDialog.class, new InterfaceTuple<>(TimePickerDialog.OnTimeSetListener.class));
        supportedClasses.put(ArchivedAccountsDialogFragment.class, new InterfaceTuple<>(ArchivedAccountsDialogFragment.ArchivedAccountsDialogInterface.class));
        supportedClasses.put(ConfirmationDialogFragment.class, new InterfaceTuple<>(ConfirmationDialogFragment.OnConfirmationDialogListener.class));
        supportedClasses.put(TransactionFiltersDialogFragment.class, new InterfaceTuple<>(TransactionFiltersDialogFragment.TransactionFiltersDialogInterface.class));

        return supportedClasses;
    }

    /**
     * Specifies the object to pass to new instances of the given {@link Fragment} subclass
     * as an interface.
     *
     * @param fragmentClass     Class to match when instantiate() is called.
     * @param _interface        Interface to pass to the fragment's constructor.
     * @return                  Current instance. Useful to add multiple map elements
     *                          one after another.
     */
    public GnomyFragmentFactory addMapElement(Class<? extends Fragment> fragmentClass, Object _interface) {
        InterfaceTuple<?> associatedInterfaceTuple = FRAGMENT_INTERFACES_MAPPING.get(fragmentClass);
        if (associatedInterfaceTuple == null) throw new IllegalArgumentException("Unsupported fragment class.");
        associatedInterfaceTuple.bindInstance(_interface);
        return this;
    }

    /**
     * Method that will get called to instantiate fragments from an
     * {@link android.app.Activity} or parent {@link Fragment}.
     *
     * If the className matches any element in FRAGMENT_INTERFACES_MAPPING,
     * a custom constructor will be used that matches the interface class specified
     * by the programmer on this class.
     *
     * Otherwise, it will just call {@link FragmentFactory#instantiate}
     *
     * @param classLoader   The default classloader to use for instantiation
     * @param className     The class name of the fragment to instantiate.
     * @return              Returns a new fragment instance.
     * @throws InvalidParameterException    If no interface instance was provided to
     * instantiate the requested fragment class.
     * @throws RuntimeException             If an error occurs while trying to instantiate
     * the requested fragment class (only if it matches one of those from FRAGMENT_INTERFACES_MAPPING)
     */
    @NonNull
    @Override
    public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
        Fragment instance;
        Class<? extends Fragment> fragmentClass
                = loadFragmentClass(classLoader, className);
        InterfaceTuple<?> associatedInterfaceTuple = FRAGMENT_INTERFACES_MAPPING.get(fragmentClass);

        if (associatedInterfaceTuple != null) {
            Class<?> interfaceClass = associatedInterfaceTuple.getInterfaceClass();
            Object interfaceInstance = associatedInterfaceTuple.getInterfaceInstance();
            if (interfaceInstance == null) throw new InvalidParameterException("Requested fragment class " + fragmentClass.getName() + " requires an interface of class " + interfaceClass.getName());
            try {
                return fragmentClass.getConstructor(interfaceClass).newInstance(interfaceInstance);
            } catch (NoSuchMethodException |
                    IllegalAccessException |
                    InstantiationException |
                    InvocationTargetException e) {
                // This should never happen
                throw new RuntimeException(e);
            }
        } else {
            instance = super.instantiate(classLoader, className);
        }

        return instance;
    }

    /**
     * Helper class to store both the interface class and the instance of
     * such interface on the same {@link Map} object.
     *
     * While it is possible to set interfaceClass and interfaceInstance attributes
     * directly (due to it being an inner class), the usage of setters and getters is preferred.
     *
     * @param <B>   Interface class.
     */
    private static class InterfaceTuple<B> {
        private final Class<B> interfaceClass;
        private B interfaceInstance = null;

        public InterfaceTuple(Class<B> interfaceClass) {
            this.interfaceClass = interfaceClass;
        }

        /**
         * Custom setter for interfaceInstance.
         *
         * @param interfaceInstance         Instance of the stored interface class.
         * @throws IllegalStateException    If an instance is already associated to this tuple.
         */
        public void bindInstance(Object interfaceInstance) {
            if (this.interfaceInstance != null) throw new IllegalStateException("Interface instance can only get bind once.");
            this.interfaceInstance = interfaceClass.cast(interfaceInstance);
        }

        public B getInterfaceInstance() {
            return interfaceInstance;
        }

        public Class<B> getInterfaceClass() {
            return interfaceClass;
        }
    }
}
