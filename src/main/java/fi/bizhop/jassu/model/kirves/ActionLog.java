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
    private final String OWNER;
    private final List<ActionLogItem> ITEMS = new ArrayList<>();

    public ActionLog(String initialState, String owner) {
        this.INITIAL_STATE = initialState;
        this.OWNER = owner;
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

    public void removeItemsAfter(int index) {
        if(this.ITEMS.size() > index) {
            this.ITEMS.subList(index + 1, this.ITEMS.size()).clear();
        }
    }

    public static ActionLog of(ActionLogDB db) throws KirvesGameException {
        List<ActionLogItem> items = new ArrayList<>();
        if(db.items == null || db.items.isEmpty()) throw new KirvesGameException("ActionLogDB.items was null or empty");
        ActionLog actionLog = new ActionLog(db.initialState, db.owner.email);
        for(ActionLogItemDB itemDB : db.items) {
            actionLog.addItem(ActionLogItem.of(itemDB));
        }
        actionLog.addItems(items);
        return actionLog;
    }

    public String getOwner() {
        return this.OWNER;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Initial state:\n ").append(this.INITIAL_STATE);
        sb.append("\nOwner: ").append(this.OWNER);

        this.ITEMS.forEach(item -> {
            sb.append("\n");
            sb.append("Item: ");
            sb.append("\n");
            sb.append(item);
        });

        return sb.toString();
    }
}
