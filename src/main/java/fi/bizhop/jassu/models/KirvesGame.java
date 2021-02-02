package fi.bizhop.jassu.models;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fi.bizhop.jassu.models.Card.Rank.BLACK;
import static fi.bizhop.jassu.models.Card.Rank.JACK;
import static fi.bizhop.jassu.models.Card.Suit.JOKER;
import static java.util.stream.Collectors.toList;

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
    private Card valttiCard = null;
    private Card.Suit valtti = null;

    public KirvesGame(User admin, Long id) throws CardException {
        this.id = id;
        this.admin = admin;
        this.deck = new KirvesDeck().shuffle();
        this.active = true;
        this.players.add(new KirvesPlayer(admin));
        this.turn = admin;
        this.dealer = admin;
        this.canDeal = true;
        this.canJoin = true;
    }

    public KirvesGameOut out() {
        return this.out(null);
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
                this.players.stream().map(KirvesPlayerOut::new).collect(toList()),
                this.deck.size(),
                this.dealer.getEmail(),
                this.turn.getEmail(),
                myCards,
                this.message,
                this.canJoin,
                userCanDeal(user),
                this.valttiCard == null ? "" : this.valttiCard.toString(),
                this.valtti == null ? "" : this.valtti.toString()
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
        return user != null && user.equals(this.turn) && user.equals(this.dealer) && this.canDeal;
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
        this.valttiCard = this.deck.remove(0);
        if(this.valttiCard.getSuit() == JOKER) {
            this.valtti = this.valttiCard.getRank() == BLACK ? Card.Suit.SPADES : Card.Suit.HEARTS;
        } else {
            this.valtti = this.valttiCard.getSuit();
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
                int winningCard = winningCard(playedCards, this.valtti);
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
                    this.valttiCard = null;
                }
            }
        } else {
            throw new KirvesGameException("Not a player in this game");
        }
    }

    public static int winningCard(List<Card> playedCards, Card.Suit valtti) {
        int leader = 0;
        for(int i = 1; i < playedCards.size(); i++) {
            Card leaderCard = playedCards.get(leader);
            Card candidate = playedCards.get(i);
            if(candidateWins(leaderCard, candidate, valtti)) {
                leader = i;
            }
        }
        return leader;
    }

    private static boolean candidateWins(Card leader, Card candidate, Card.Suit valtti) {
        int leaderRank = getConvertedRank(leader, valtti);
        int candidateRank = getConvertedRank(candidate, valtti);
        Card.Suit leaderSuit = leader.getSuit().equals(JOKER) ? valtti : leader.getSuit();
        Card.Suit candidateSuit = candidate.getSuit().equals(JOKER) ? valtti : candidate.getSuit();

        if(candidateSuit.equals(valtti) && !leaderSuit.equals(valtti)) {
            return true;
        }
        else return candidateSuit.equals(leaderSuit) &&
                candidateRank > leaderRank;
    }

    private static int getConvertedRank(Card card, Card.Suit valtti) {
        if(card.getRank().equals(JACK)) {
            switch (card.getSuit()) {
                case DIAMONDS:
                    return 15;
                case HEARTS:
                    return 16;
                case SPADES:
                    return 17;
                case CLUBS:
                    return 18;
            }
        }
        return card.getRank().getValue();
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
