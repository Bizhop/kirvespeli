package fi.bizhop.jassu.models;

import java.util.List;

public class PokerGameIn {
    private PokerGame.Action action;
    private List<Integer> parameters;

    public PokerGameIn() {}

    public PokerGame.Action getAction() {
        return action;
    }

    public void setAction(PokerGame.Action action) {
        this.action = action;
    }

    public List<Integer> getParameters() {
        return parameters;
    }

    public void setParameters(List<Integer> parameters) {
        this.parameters = parameters;
    }
}
