package io.github.alansanchezp.gnomy;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.github.alansanchezp.gnomy.database.account.Account;

import static io.github.alansanchezp.gnomy.util.ISpinnerItem.getItemIndexById;
import static org.junit.Assert.assertEquals;

public class ISpinnerItemTest {
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
}
