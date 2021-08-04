package fi.bizhop.jassu.model.kirves;

import fi.bizhop.jassu.db.ActionLogDB;
import fi.bizhop.jassu.db.ActionLogItemDB;
import fi.bizhop.jassu.db.UserDB;
import fi.bizhop.jassu.exception.KirvesGameException;
import fi.bizhop.jassu.model.User;
import fi.bizhop.jassu.model.kirves.in.GameIn;
import fi.bizhop.jassu.util.JsonUtil;

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

    public static ActionLogItem of(ActionLogItemDB db) throws KirvesGameException {
        User user = new User(db.user);
        GameIn input = JsonUtil.getJavaObject(db.input, GameIn.class)
                .orElseThrow(() -> new KirvesGameException("Unable to convert GameIn from ActionLogItem.input"));
        return new ActionLogItem(user, input);
    }

    public ActionLogItemDB getDB(UserDB userDB, ActionLogDB actionLogDB) throws KirvesGameException {
        if(userDB == null || userDB.email == null) throw new KirvesGameException("Invalid user (null or email null)");
        if(!userDB.email.equals(this.USER.getEmail())) throw new KirvesGameException("Invalid user (differs from ActionLogItem.USER)");

        ActionLogItemDB logItemDB = new ActionLogItemDB();
        logItemDB.user = userDB;
        logItemDB.input = JsonUtil.getJson(this.INPUT).orElseThrow(() -> new KirvesGameException("Unable to convert GameIn to json"));
        logItemDB.actionLog = actionLogDB;
        return logItemDB;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(" User: ");
        sb.append(this.USER.getEmail());
        sb.append("\n");
        sb.append(" Input: ");
        sb.append(JsonUtil.getJson(this.INPUT).orElse("unknown"));

        return sb.toString();
    }
}
