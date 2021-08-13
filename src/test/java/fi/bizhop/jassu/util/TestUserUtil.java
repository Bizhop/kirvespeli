package fi.bizhop.jassu.util;

import fi.bizhop.jassu.db.UserDB;
import fi.bizhop.jassu.model.User;

public class TestUserUtil {
    public static final String TEST_USER_EMAIL = "user@mock.com";

    public static UserDB getTestUserDB(String email) {
        return new UserDB(getTestUser(email));
    }

    public static UserDB getTestUserDB() {
        return new UserDB(getTestUser(TEST_USER_EMAIL));
    }

    public static User getTestUser(String email) {
        return new User(email, "");
    }

    public static User getTestUser() {
        return new User(TEST_USER_EMAIL, "");
    }
}
