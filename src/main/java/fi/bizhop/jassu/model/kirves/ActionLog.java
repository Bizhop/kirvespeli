package fi.bizhop.jassu.model.kirves;

import fi.bizhop.jassu.db.ActionLogDB;
import fi.bizhop.jassu.db.ActionLogItemDB;
import fi.bizhop.jassu.db.UserDB;
import fi.bizhop.jassu.exception.KirvesGameException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActionLog {
    private final String INITIAL_STATE;
    private final List<ActionLogItem> ITEMS = new ArrayList<>();

    public ActionLog(String initialState) {
        this.INITIAL_STATE = initialState;
    }

    public String getInitialState() {
        return this.INITIAL_STATE;
    }

    public List<ActionLogItem> getItems() {
        return new ArrayList<>(this.ITEMS);
    }

    public void addItem(ActionLogItem item) {
        this.ITEMS.add(item);
    }

    public void addItems(List<ActionLogItem> items) { this.ITEMS.addAll(items); }

    public static ActionLog of(ActionLogDB db) throws KirvesGameException {
        List<ActionLogItem> items = new ArrayList<>();
        if(db.items == null || db.items.isEmpty()) throw new KirvesGameException("ActionLogDB.items was null or empty");
        for(ActionLogItemDB itemDB : db.items) {
            items.add(ActionLogItem.of(itemDB));
        }
        ActionLog actionLog = new ActionLog(db.initialState);
        actionLog.addItems(items);
        return actionLog;
    }

    public ActionLogDB getDB(String key, Map<String, UserDB> users) throws KirvesGameException {
        ActionLogDB db = new ActionLogDB();
        db.key = key;
        db.initialState = this.INITIAL_STATE;
        db.items = new ArrayList<>();
        for(ActionLogItem item : this.ITEMS) {
            UserDB user = users.get(item.getUser().getEmail());
            db.items.add(item.getDB(user));
        }
        return db;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Initial state:\n ");
        sb.append(this.INITIAL_STATE);

        this.ITEMS.forEach(item -> {
            sb.append("\n");
            sb.append("Item: ");
            sb.append("\n");
            sb.append(item);
        });

        return sb.toString();
    }
}
