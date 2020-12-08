package io.github.alansanchezp.gnomy;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;

import static io.github.alansanchezp.gnomy.util.ListUtil.getItemIndexById;
import static io.github.alansanchezp.gnomy.util.ListUtil.getItemIndexByPropertyGetter;
import static org.junit.Assert.assertEquals;

public class ListUtilTest {
    @Test
    public void finds_Account_in_list_by_id() {
        List<Account> testList = new ArrayList<>();

        Account obj = new Account();
        obj.setId(10);
        testList.add(obj);

        obj = new Account();
        obj.setId(2);
        testList.add(obj);

        obj = new Account();
        obj.setId(15);
        testList.add(obj);

        obj = new Account();
        obj.setId(1);
        testList.add(obj);

        obj = new Account();
        obj.setId(8);
        testList.add(obj);

        obj = new Account();
        obj.setId(40);
        testList.add(obj);

        assertEquals(-1, getItemIndexById(testList, 0));
        assertEquals(3, getItemIndexById(testList, 1));
        assertEquals(5, getItemIndexById(testList, 40));
    }

    @Test
    public void finds_Account_in_list_by_initial_value()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        List<Account> testList = new ArrayList<>();

        Account obj = new Account();
        obj.setInitialValue("10");
        testList.add(obj);

        obj = new Account();
        obj.setInitialValue("521");
        testList.add(obj);

        obj = new Account();
        obj.setInitialValue("4382");
        testList.add(obj);

        obj = new Account();
        obj.setInitialValue("138");
        testList.add(obj);

        obj = new Account();
        obj.setInitialValue("90");
        testList.add(obj);

        obj = new Account();
        obj.setInitialValue("1000");
        testList.add(obj);

        assertEquals(-1, getItemIndexByPropertyGetter(
                testList, BigDecimalUtil.fromString("999"), "getInitialValue"));
        assertEquals(2, getItemIndexByPropertyGetter(
                testList, BigDecimalUtil.fromString("4382"), "getInitialValue"));
        assertEquals(0, getItemIndexByPropertyGetter(
                testList, BigDecimalUtil.fromString("10"), "getInitialValue"));
        assertEquals(1, getItemIndexByPropertyGetter(
                testList, BigDecimalUtil.fromString("521"), "getInitialValue"));
        assertEquals(4, getItemIndexByPropertyGetter(
                testList, BigDecimalUtil.fromString("90"), "getInitialValue"));
    }
}
