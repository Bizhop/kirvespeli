package fi.bizhop.jassu.model.kirves;

import fi.bizhop.jassu.db.ActionLogDB;
import fi.bizhop.jassu.db.ActionLogItemDB;
import fi.bizhop.jassu.db.UserDB;
import fi.bizhop.jassu.exception.KirvesGameException;
import fi.bizhop.jassu.model.User;
import fi.bizhop.jassu.model.kirves.in.GameIn;
import fi.bizhop.jassu.util.JsonUtil;

import static fi.bizhop.jassu.exception.KirvesGameException.Type.BAD_REQUEST;

public class ActionLogItem {
    private User user;
    private GameIn input;

    public ActionLogItem() {}

    public ActionLogItem(User user, GameIn input) {
        this.user = user;
        this.input = input;
    }

    public User getUser() {
        return this.user;
    }

    public GameIn getInput() {
        return this.input;
    }

    public static ActionLogItem of(User user, GameIn input) {
        return new ActionLogItem(user, input);
    }

    public static ActionLogItem of(ActionLogItemDB db) throws KirvesGameException {
        var user = new User(db.user);
        var input = JsonUtil.getJavaObject(db.input, GameIn.class)
                .orElseThrow(() -> new KirvesGameException("Unable to convert GameIn from ActionLogItem.input"));
        return new ActionLogItem(user, input);
    }

    public ActionLogItemDB getDB(UserDB userDB, ActionLogDB actionLogDB) throws KirvesGameException {
        if(userDB == null || userDB.email == null) throw new KirvesGameException("Invalid user (null or email null)", BAD_REQUEST);
        if(!userDB.email.equals(this.user.getEmail())) throw new KirvesGameException("Invalid user (differs from ActionLogItem.USER)", BAD_REQUEST);

        var logItemDB = new ActionLogItemDB();
        logItemDB.user = userDB;
        logItemDB.input = JsonUtil.getJson(this.input).orElseThrow(() -> new KirvesGameException("Unable to convert GameIn to json"));
        logItemDB.actionLog = actionLogDB;
        return logItemDB;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();

        sb.append(" User: ");
        sb.append(this.user.getEmail());
        sb.append("\n");
        sb.append(" Input: ");
        sb.append(JsonUtil.getJson(this.input).orElse("unknown"));

        return sb.toString();
    }
}
