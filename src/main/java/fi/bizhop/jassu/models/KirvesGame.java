package fi.bizhop.jassu.models;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class KirvesGame {
    private static final int NUM_OF_CARD_TO_DEAL = 5;

    private User admin;
    private Cards deck;
    private List<KirvesPlayer> players = new ArrayList<>();
    private boolean active;
    private User turn;
    private User dealer;
    private boolean canDeal;
    private String message;

    public KirvesGame(User admin) throws CardException {
        this.admin = admin;
        this.deck = new KirvesDeck().shuffle();
        this.active = true;
        this.players.add(new KirvesPlayer(admin));
        this.turn = admin;
        this.dealer = admin;
        this.canDeal = true;
    }

    public KirvesGameOut out(User user) {
        List<String> myCards = new ArrayList<>();
        if(user != null) {
            Optional<KirvesPlayer> me = getPlayer(user);
            if(me.isPresent()) {
                myCards = me.get().getHand().getCardsOut();
            }
        }
        return new KirvesGameOut(
                this.getAdmin(),
                this.players.stream().map(KirvesPlayerOut::new).collect(Collectors.toList()),
                this.deck.size(),
                this.dealer.getEmail(),
                this.turn.getEmail(),
                myCards,
                this.message
        );
    }

    private Optional<KirvesPlayer> getPlayer(User user) {
        return this.players.stream().filter(player -> player.getUser().equals(user)).findFirst();
    }

    public void addPlayer(User newPlayer) {
        if(this.players.stream()
                .filter(player -> newPlayer.getEmail().equals(player.getUserEmail()))
                .count() == 0) {
            this.players.add(new KirvesPlayer(newPlayer));
        }
    }

    public void inactivate() {
        this.active = false;
    }

    public boolean isActive() {
        return this.active;
    }

    public String getAdmin() {
        return this.admin == null
                ? ""
                : this.admin.getEmail();
    }

    public boolean userCanDeal(User user) {
        return user.equals(this.turn) && user.equals(this.dealer) && this.canDeal;
    }

    public boolean isMyTurn(User user) {
        return this.turn.equals(user);
    }

    public void deal(User user) throws CardException, KirvesGameException {
        for(KirvesPlayer player : players) {
            player.addCards(this.deck.deal(NUM_OF_CARD_TO_DEAL));
        }
        nextPlayer(user);
        this.canDeal = false;
    }

    public void playCard(User user, int index) throws KirvesGameException {
        Optional<KirvesPlayer> me = getPlayer(user);
        if(me.isPresent()) {
            KirvesPlayer player = me.get();
            try {
                player.playCard(index);
            } catch (CardException e) {
                this.message = String.format("Can't play card with index: %d", index);
            }
        } else {
            throw new KirvesGameException("Not a player in this game");
        }
    }

    private void nextPlayer(User user) throws KirvesGameException {
        if(players.size() > 1) {
            int myIndex = findIndex(user);
            if (myIndex < 0) {
                throw new KirvesGameException("Not a player in this game");
            } else {
                int newIndex = myIndex == players.size() ? 0 : myIndex + 1;
                this.turn = players.get(newIndex).getUser();
            }
        }
    }

    private int findIndex(User user) {
        for(int i=0; i < this.players.size(); i++) {
            KirvesPlayer player = players.get(i);
            if(player.getUser().equals(user)) {
                return i;
            }
        }
        return -1;
    }

    public boolean hasPlayer(User user) {
        return this.players.stream().anyMatch(kirvesPlayer -> user.equals(kirvesPlayer.getUser()));
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public enum Action {
        DEAL, PLAY_CARD, FOLD
    }
}
