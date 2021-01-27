package fi.bizhop.jassu.models;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class KirvesGame {
    private static final int NUM_OF_CARD_TO_DEAL = 5;

    private Long id;
    private final User admin;
    private Cards deck;
    private final List<KirvesPlayer> players = new ArrayList<>();
    private boolean active;
    private boolean canJoin;
    private User turn;
    private User dealer;
    private boolean canDeal;
    private String message;
    private int firstPlayerOfRound;

    public KirvesGame(User admin, Long id) throws CardException {
        this.id = id;
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
                this.id,
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

    public void addPlayer(User newPlayer) throws KirvesGameException {
        if(this.canJoin) {
            if (this.players.stream()
                    .filter(player -> newPlayer.getEmail().equals(player.getUserEmail()))
                    .count() == 0) {
                this.players.add(new KirvesPlayer(newPlayer));
            }
        } else {
            throw new KirvesGameException("Can't join this game now");
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
        this.deck = new KirvesDeck().shuffle();
        for(KirvesPlayer player : players) {
            player.getPlayedCards().clear();
            player.addCards(this.deck.deal(NUM_OF_CARD_TO_DEAL));
        }
        this.canDeal = false;
        this.canJoin = false;
        this.turn = nextPlayer(user).orElseThrow(() -> new KirvesGameException("Unable to determine next player"));
        this.firstPlayerOfRound = findIndex(this.turn);
    }

    public void playCard(User user, int index) throws KirvesGameException, CardException {
        Optional<KirvesPlayer> me = getPlayer(user);
        if(me.isPresent()) {
            KirvesPlayer player = me.get();
            player.playCard(index);
            this.turn = nextPlayer(user).orElseThrow(() -> new KirvesGameException("Unable to determine next player"));
            if(findIndex(this.turn) == this.firstPlayerOfRound) {
                int round = player.getPlayedCards().size() - 1;
                List<Card> playedCards = new ArrayList<>();
                int offset = this.firstPlayerOfRound;
                for(int i = 0; i < this.players.size(); i++) {
                    int cardPlayerIndex = (offset + i) % this.players.size();
                    KirvesPlayer cardPlayer = this.players.get(cardPlayerIndex);
                    playedCards.add(cardPlayer.getPlayedCards().get(round));
                }
                int winningCard = winningCard(playedCards);
                KirvesPlayer roundWinner = this.players.get((winningCard + offset) % this.players.size());
                this.message = String.format("Round %d winner is %s", round + 1, roundWinner.getUserEmail());
                if(round < NUM_OF_CARD_TO_DEAL - 1) {
                    this.turn = roundWinner.getUser();
                    this.firstPlayerOfRound = findIndex(this.turn);
                }
                else {
                    this.dealer = nextPlayer(this.dealer).orElseThrow(() -> new KirvesGameException("Unable to determine next dealer"));
                    this.turn = this.dealer;
                    this.canDeal = true;
                }
            }
        } else {
            throw new KirvesGameException("Not a player in this game");
        }
    }

    public static int winningCard(List<Card> playedCards) {
        int leader = 0;
        for(int i = 1; i < playedCards.size(); i++) {
            Card leaderCard = playedCards.get(leader);
            Card candidate = playedCards.get(i);
            if(     candidate.getSuit() == leaderCard.getSuit() &&
                    candidate.getRank().getValue() > leaderCard.getRank().getValue()
            ) {
                leader = i;
            }
        }
        return leader;
    }

    private Optional<User> nextPlayer(User user) throws KirvesGameException {
        if(players.size() > 1) {
            int myIndex = findIndex(user);
            if (myIndex < 0) {
                throw new KirvesGameException("Not a player in this game");
            } else {
                int newIndex = myIndex == players.size() - 1 ? 0 : myIndex + 1;
                return Optional.of(players.get(newIndex).getUser());
            }
        }
        return Optional.of(user);
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
