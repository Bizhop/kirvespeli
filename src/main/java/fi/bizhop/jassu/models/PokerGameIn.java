package fi.bizhop.jassu.models;

import java.util.List;

public class PokerGameIn {
    private Long gameId;
    private PokerGame.Action action;
    private List<Integer> parameters;

    public PokerGameIn() {}

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

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
