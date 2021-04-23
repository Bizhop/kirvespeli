package fi.bizhop.jassu.model.kirves;

import java.util.List;

public class PlayerPOJO {
    public UserPOJO user;
    public List<String> hand;
    public List<String> invisibleCards;
    public String extraCard;
    public List<String> playedCards;
    public List<Integer> roundsWon;
    public List<String> availableActions;
    public String next;
    public String previous;
    public boolean declaredPlayer;
    public boolean inGame;

    public PlayerPOJO() {}

    public PlayerPOJO(UserPOJO user, List<String> hand, List<String> invisibleCards, String extraCard, List<String> playedCards, List<Integer> roundsWon, List<String> availableActions, String next, String previous, boolean declaredPlayer, boolean inGame) {
        this.user = user;
        this.hand = hand;
        this.invisibleCards = invisibleCards;
        this.extraCard = extraCard;
        this.playedCards = playedCards;
        this.roundsWon = roundsWon;
        this.availableActions = availableActions;
        this.next = next;
        this.previous = previous;
        this.declaredPlayer = declaredPlayer;
        this.inGame = inGame;
    }
}