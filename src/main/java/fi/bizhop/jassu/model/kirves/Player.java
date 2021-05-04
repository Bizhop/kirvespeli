package fi.bizhop.jassu.model.kirves;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;
import fi.bizhop.jassu.model.Card;
import fi.bizhop.jassu.model.Cards;

import java.util.Collections;
import java.util.List;

public class Player {

    private final Cards hand = new Cards();
    private final Cards invisibleCards = new Cards();
    private Card extraCard;
    private final Cards playedCards = new Cards();
    private Player next;
    private Player previous;

    private final PlayerPOJO data;

    /**
     * Recreate player from pojo data. Links previous if available.
     *
     * @param pojo
     * @param previous
     * @throws CardException
     */
    public Player(PlayerPOJO pojo, Player previous) throws CardException {
        this.data = pojo;
        this.hand.add(Cards.fromAbbrs(pojo.hand));
        this.invisibleCards.add(Cards.fromAbbrs(pojo.invisibleCards));
        this.extraCard = Card.fromAbbr(pojo.extraCard);
        this.playedCards.add(Cards.fromAbbrs(pojo.playedCards));
        if(previous != null) {
            this.previous = previous;
            this.previous.setNext(this);
        }
    }

    /**
     * Create new player linking to previous and next players
     * @param user
     */
    public Player(UserPOJO user, Player next, Player previous) {
        this.data = new PlayerPOJO(user);
        this.next = next;
        this.previous = previous;
        next.previous = this;
        previous.next = this;
    }

    /**
     * Create new player (first) linking only to self
     * @param user
     */
    public Player(UserPOJO user) {
        this.data = new PlayerPOJO(user);
        this.next = this;
        this.previous = this;
    }

    public String getUserEmail() {
        return this.getUser() == null
                ? ""
                : this.getUser().email;
    }

    public String getUserNickname() {
        return this.getUser() == null ? "" : getUser().getNickname();
    }

    public UserPOJO getUser() {
        return this.data.user;
    }

    public int cardsInHand() {
        return this.hand.size();
    }

    public void playCard(int index) throws CardException {
        this.playedCards.add(this.hand.remove(index));
    }

    public Cards getPlayedCards() {
        return this.playedCards;
    }

    public void addCards(Cards newCards) {
        this.hand.add(newCards);
    }

    public Cards getHand() {
        return this.hand;
    }

    public void addRoundWon() {
        this.data.roundsWon.add(this.playedCards.size() - 1);
    }

    public void resetWonRounds() {
        this.data.roundsWon.clear();
    }

    public void resetAvailableActions() {
        this.data.availableActions.clear();
    }

    public List<Integer> getRoundsWon() {
        return Collections.unmodifiableList(this.data.roundsWon);
    }

    public List<Game.Action> getAvailableActions() {
        return Collections.unmodifiableList(this.data.availableActions);
    }

    public void setAvailableActions(List<Game.Action> availableActions) {
        this.data.availableActions.clear();
        this.data.availableActions.addAll(availableActions);
    }

    public Card getExtraCard() {
        return extraCard;
    }

    public Card getLastPlayedCard() {
        return this.playedCards.last();
    }

    public boolean hasInvisibleCards() {
        return this.invisibleCards.size() > 0;
    }

    public void hideCards(int numberOfCards) throws CardException {
        this.invisibleCards.add(this.hand.deal(numberOfCards));
    }

    public void moveInvisibleCardsToHand() {
        this.hand.add(this.invisibleCards);
        this.invisibleCards.clear();
    }

    public void setExtraCard(Card extraCard) {
        this.extraCard = extraCard;
    }

    public void discard(int index) throws KirvesGameException, CardException {
        if(extraCard == null) {
            throw new KirvesGameException("Cannot discard without extra card");
        }
        this.hand.remove(index);
        this.hand.add(this.extraCard);
        this.extraCard = null;
    }

    public Player getNext() {
       return next.data.inGame ? next : next.getNext();
    }

    public void setNext(Player next) {
        this.next = next;
    }

    public Player getPrevious() {
        return previous.data.inGame ? previous : previous.getPrevious();
    }

    public void setPrevious(Player previous) {
        this.previous = previous;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Player)) return false;
        Player other = (Player)o;

        if(this.extraCard == null && other.extraCard != null) return false;

        return this.data.equals(other.data)
                && this.hand.equals(other.hand)
                && this.invisibleCards.equals(other.invisibleCards)
                && (this.extraCard == null || this.extraCard.equals(other.extraCard))
                && this.playedCards.equals(other.playedCards)
                && this.next.getUserEmail().equals(other.next.getUserEmail())
                && this.previous.getUserEmail().equals(other.previous.getUserEmail());
    }

    @Override
    public String toString() {
        return this.getUserEmail();
    }

    public boolean isDeclaredPlayer() {
        return data.declaredPlayer;
    }

    public void setDeclaredPlayer(boolean declaredPlayer) {
        this.data.declaredPlayer = declaredPlayer;
    }

    public void clearHand() {
        this.hand.clear();
    }

    public boolean isInGame() {
        return data.inGame;
    }

    public void activate() {
        this.data.inGame = true;
        this.data.folded = false;
    }

    public void inactivate() {
        this.hand.clear();
        this.playedCards.clear();
        this.extraCard = null;
        this.invisibleCards.clear();
        this.data.inGame = false;
    }

    public void fold() {
        this.inactivate();
        this.data.folded = true;
    }

    public boolean isFolded() {
        return this.data.folded;
    }

    public PlayerPOJO toPojo() {
        this.data.hand = this.hand.getCardsOut();
        this.data.invisibleCards = this.invisibleCards.getCardsOut();
        this.data.extraCard = this.extraCard == null ? null : this.extraCard.toString();
        this.data.playedCards = this.playedCards.getCardsOut();
        this.data.next = this.next == null ? null : this.next.getUserEmail();
        this.data.previous = this.previous == null ? null : this.previous.getUserEmail();

        return this.data;
    }

    public void setSpeak(Game.Speak speak) {
        this.data.speak = speak;
    }

    public Game.Speak getSpeak() {
        return this.data.speak;
    }
}
