package fi.bizhop.jassu.model.kirves;

import fi.bizhop.jassu.model.User;
import fi.bizhop.jassu.model.kirves.in.GameIn;

public class ActionLogItem {
    private final User USER;
    private final GameIn INPUT;

    private ActionLogItem(User user, GameIn input) {
        this.USER = user;
        this.INPUT = input;
    }

    public User getUser() {
        return this.USER;
    }

    public GameIn getInput() {
        return this.INPUT;
    }

    public static ActionLogItem of(User user, GameIn input) {
        return new ActionLogItem(user, input);
    }
}
