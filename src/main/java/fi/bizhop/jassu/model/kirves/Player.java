package fi.bizhop.jassu.model.kirves;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;
import fi.bizhop.jassu.model.Card;
import fi.bizhop.jassu.model.Cards;
import fi.bizhop.jassu.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class Player {

    private final User user;
    private final Cards hand = new Cards();
    private final Cards invisibleCards = new Cards();
    private Card extraCard;
    private final Cards playedCards = new Cards();
    private final List<Integer> roundsWon = new ArrayList<>();
    private final List<Game.Action> availableActions = new ArrayList<>();
    private Player next;
    private Player previous;
    private boolean declaredPlayer = false;
    private boolean inGame = true;

    /**
     * Recreate player from pojo data. Links previous if available.
     *
     * @param pojo
     * @param previous
     * @throws CardException
     */
    public Player(PlayerPOJO pojo, Player previous) throws CardException {
        this.user = new User(pojo.user);
        this.hand.add(Cards.fromAbbrs(pojo.hand));
        this.invisibleCards.add(Cards.fromAbbrs(pojo.invisibleCards));
        this.extraCard = Card.fromAbbr(pojo.extraCard);
        this.playedCards.add(Cards.fromAbbrs(pojo.playedCards));
        this.roundsWon.addAll(pojo.roundsWon);
        this.availableActions.addAll(pojo.availableActions.stream().map(Game.Action::valueOf).collect(toList()));
        if(previous != null) {
            this.previous = previous;
            this.previous.setNext(this);
        }
        this.declaredPlayer = pojo.declaredPlayer;
        this.inGame = pojo.inGame;
    }

    /**
     * Create new player linking to previous and next players
     * @param user
     */
    public Player(User user, Player next, Player previous) {
        this.user = user;
        this.next = next;
        this.previous = previous;
        next.previous = this;
        previous.next = this;
    }

    /**
     * Create new player (first) linking only to self
     * @param user
     */
    public Player(User user) {
        this.user = user;
        this.next = this;
        this.previous = this;
    }

    public String getUserEmail() {
        return this.user == null
                ? ""
                : this.user.getEmail();
    }

    public String getUserNickname() {
        return this.user == null
                ? ""
                : this.user.getNickname();
    }

    public User getUser() {
        return this.user;
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
        this.roundsWon.add(this.playedCards.size() - 1);
    }

    public void resetWonRounds() {
        this.roundsWon.clear();
    }

    public void resetAvailableActions() {
        this.availableActions.clear();
    }

    public List<Integer> getRoundsWon() {
        return Collections.unmodifiableList(this.roundsWon);
    }

    public List<Game.Action> getAvailableActions() {
        return Collections.unmodifiableList(this.availableActions);
    }

    public void setAvailableActions(List<Game.Action> availableActions) {
        this.availableActions.clear();
        this.availableActions.addAll(availableActions);
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
       return next.inGame ? next : next.getNext();
    }

    public void setNext(Player next) {
        this.next = next;
    }

    public Player getPrevious() {
        return previous.inGame ? previous : previous.getPrevious();
    }

    public void setPrevious(Player previous) {
        this.previous = previous;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Player)) return false;
        Player other = (Player)o;
        if(this.extraCard == null && other.extraCard != null) return false;
        if(this.roundsWon.size() != other.roundsWon.size()) return false;
        for(int i=0; i < this.roundsWon.size(); i++) {
            if(!this.roundsWon.get(i).equals(other.roundsWon.get(i))) return false;
        }
        if(this.availableActions.size() != other.availableActions.size()) return false;
        for(int i=0; i < this.availableActions.size(); i++) {
            if(this.availableActions.get(i) != other.availableActions.get(i)) return false;
        }
        return this.user.equals(other.user)
                && this.hand.equals(other.hand)
                && this.invisibleCards.equals(other.invisibleCards)
                && (this.extraCard == null || this.extraCard.equals(other.extraCard))
                && this.playedCards.equals(other.playedCards)
                && this.next.getUserEmail().equals(other.next.getUserEmail())
                && this.previous.getUserEmail().equals(other.previous.getUserEmail())
                && this.declaredPlayer == other.declaredPlayer
                && this.inGame == other.inGame;
    }

    @Override
    public String toString() {
        return this.getUserEmail();
    }

    public boolean isDeclaredPlayer() {
        return declaredPlayer;
    }

    public void setDeclaredPlayer(boolean declaredPlayer) {
        this.declaredPlayer = declaredPlayer;
    }

    public void clearHand() {
        this.hand.clear();
    }

    public boolean isInGame() {
        return inGame;
    }

    public void activate() {
        this.inGame = true;
    }

    public void inactivate() {
        this.inGame = false;
    }

    public PlayerPOJO toPojo() {
        return new PlayerPOJO(
                new UserPOJO(this.user.getEmail(), this.user.getNickname()),
                this.hand.getCardsOut(),
                this.invisibleCards.getCardsOut(),
                this.extraCard == null ? null : this.extraCard.toString(),
                this.playedCards.getCardsOut(),
                this.roundsWon,
                this.availableActions.stream().map(Game.Action::name).collect(toList()),
                this.next == null ? null : this.next.getUserEmail(),
                this.previous == null ? null : this.previous.getUserEmail(),
                this.declaredPlayer,
                this.inGame
        );
    }
}
