package modeltesting;
import model.LoginInfo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LoginInfoTest {

    @Test
    void constructorCreatesObjectSuccessfully() {
        LoginInfo loginInfo = new LoginInfo("user", "pass");

        assertNotNull(loginInfo);
    }

    @Test
    void objectEqualsItself() {
        LoginInfo loginInfo = new LoginInfo("user", "pass");

        assertEquals(loginInfo, loginInfo);
    }

    @Test
    void equalObjectsAreEqual() {
        LoginInfo a = new LoginInfo("user", "pass");
        LoginInfo b = new LoginInfo("user", "pass");

        assertEquals(a, b);
        assertEquals(b, a);
    }

    @Test
    void equalObjectsHaveSameHashCode() {
        LoginInfo a = new LoginInfo("user", "pass");
        LoginInfo b = new LoginInfo("user", "pass");

        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differentUsernamesMakeObjectsNotEqual() {
        LoginInfo a = new LoginInfo("user1", "pass");
        LoginInfo b = new LoginInfo("user2", "pass");

        assertNotEquals(a, b);
    }

    @Test
    void differentPasswordsMakeObjectsNotEqual() {
        LoginInfo a = new LoginInfo("user", "pass1");
        LoginInfo b = new LoginInfo("user", "pass2");

        assertNotEquals(a, b);
    }

    @Test
    void completelyDifferentObjectsAreNotEqual() {
        LoginInfo a = new LoginInfo("user1", "pass1");
        LoginInfo b = new LoginInfo("user2", "pass2");

        assertNotEquals(a, b);
    }

    @Test
    void objectIsNotEqualToNull() {
        LoginInfo loginInfo = new LoginInfo("user", "pass");

        assertNotEquals(null, loginInfo);
    }

    @Test
    void objectIsNotEqualToDifferentType() {
        LoginInfo loginInfo = new LoginInfo("user", "pass");

        assertNotEquals(loginInfo, "not a LoginInfo");
    }

    @Test
    void equalityIsTransitive() {
        LoginInfo a = new LoginInfo("user", "pass");
        LoginInfo b = new LoginInfo("user", "pass");
        LoginInfo c = new LoginInfo("user", "pass");

        assertEquals(a, b);
        assertEquals(b, c);
        assertEquals(a, c);
    }

    @Test
    void equalityIsConsistent() {
        LoginInfo a = new LoginInfo("user", "pass");
        LoginInfo b = new LoginInfo("user", "pass");

        assertEquals(a, b);
        assertEquals(a, b);
        assertEquals(a, b);
    }

    @Test
    void hashCodeIsConsistent() {
        LoginInfo loginInfo = new LoginInfo("user", "pass");

        int first = loginInfo.hashCode();
        int second = loginInfo.hashCode();
        int third = loginInfo.hashCode();

        assertEquals(first, second);
        assertEquals(second, third);
    }
}