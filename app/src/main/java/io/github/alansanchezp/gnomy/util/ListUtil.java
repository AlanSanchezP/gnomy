package io.github.alansanchezp.gnomy.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;

public class ListUtil {

    /**
     * Finds an object based on its id in a list. The object MUST implement
     * a getId() method that returns an integer, otherwise a {@link RuntimeException}
     * will be thrown. If various objects have the same id, then the first matching
     * index is returned.
     *
     * @param list      List to scan.
     * @param itemId    Id to be found.
     * @return          Index of the first object that has the given id.
     */
    public static int getItemIndexById(@NonNull List<?> list, int itemId) {
        try {
            return getItemIndexByPropertyGetter(list, itemId, "getId");
        } catch (NoSuchMethodException |
                InvocationTargetException |
                IllegalAccessException e) {
            // Rethrowing as RuntimeException because default getId() method is not
            //  supposed to throw the above exceptions, meaning it's a programmer's mistake
            throw new RuntimeException(e);
        }
    }

    /**
     * Finds an object based on the returned value of a getter method.
     * If various objects return the same value when calling the getter
     * method, then the first matching index is returned.
     *
     * @param list                      List to scan
     * @param valueToMatch              Returned value by the given getter method
     * @param getterMethodName          Name of the getter method to use
     * @return                          Index of the first object that satisfies
     *                                  object.getterMethodName().equals(valueToMatch)
     *
     * @throws NoSuchMethodException        If the object doesn't have a method that matches
     *                                      getterMethodName.
     * @throws InvocationTargetException    If the underlying getterMethodName()
     *                                      throws an exception.
     * @throws IllegalAccessException       If getterMethodName() is inaccessible for any reason.
     */
    public static int getItemIndexByPropertyGetter(@NonNull List<?> list,
                                             @NonNull Object valueToMatch,
                                             @NonNull String getterMethodName)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (list.isEmpty()) return -1;
        Class<?> listElementsClass = list.get(0).getClass();
        Method getterMethod = listElementsClass.getMethod(getterMethodName);
        for (int i = 0; i < list.size(); i++) {
            Object result = getterMethod.invoke(list.get(i));
            if (Objects.equals(result, valueToMatch)) {
                return i;
            }
        }
        return -1;
    }
}
