package fi.bizhop.jassu.model.kirves;

import java.util.ArrayList;
import java.util.List;

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
}
