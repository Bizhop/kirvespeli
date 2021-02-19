package fi.bizhop.jassu.model;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fi.bizhop.jassu.model.Card.Rank.BLACK;
import static fi.bizhop.jassu.model.Card.Rank.JACK;
import static fi.bizhop.jassu.model.Card.Suit.JOKER;
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

    public Optional<KirvesPlayer> getRoundWinner(int round) {
        return this.players.stream().filter(player -> player.getRoundsWon().contains(round)).findFirst();
    }

    public void addPlayer(User newPlayer) throws KirvesGameException {
        if(this.canJoin) {
            if (this.players.stream()
                    .filter(player -> newPlayer.getEmail().equals(player.getUserEmail()))
                    .count() == 0) {
                this.players.add(new KirvesPlayer(newPlayer));
            } else {
                throw new KirvesGameException(String.format("Player %s already joined game id=%d", newPlayer.getEmail(), this.id));
            }
        } else {
            throw new KirvesGameException(String.format("Can't join this game (id=%d) now", this.id));
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
        for(KirvesPlayer player : this.players) {
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
        this.players.forEach(KirvesPlayer::resetWonRounds);
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
                roundWinner.addRoundWon();

                //TODO: remove this message when winning logic is complete and tested
                this.message = String.format("Round %d winner is %s", round + 1, roundWinner.getUserEmail());

                if(round < NUM_OF_CARD_TO_DEAL - 1) {
                    this.turn = roundWinner.getUser();
                    this.firstPlayerOfRound = findIndex(this.turn);
                }
                else {
                    KirvesPlayer handWinner = determineHandWinner();
                    this.message = String.format("%s, hand winner is %s", this.message, handWinner.getUserEmail());

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

    public KirvesPlayer determineHandWinner() throws KirvesGameException {
        //three or more rounds is clear winner
        Optional<KirvesPlayer> threeOrMore = this.players.stream()
                .filter(player -> player.getRoundsWon().size() >= 3)
                .findFirst();
        if(threeOrMore.isPresent()) {
            return threeOrMore.get();
        }

        //two rounds
        List<KirvesPlayer> two = this.players.stream()
                .filter(player -> player.getRoundsWon().size() == 2)
                .collect(toList());

        if(two.size() == 1) {
            //only one player with two rounds is winner
            return two.get(0);
        } else if(two.size() == 2) {
            //two players with two rounds, first two wins
            KirvesPlayer first = two.get(0);
            KirvesPlayer second = two.get(1);

            return first.getRoundsWon().get(1) > second.getRoundsWon().get(1) ? second : first;
        }

        //if these cases don't return anything, there should be five single round winners
        List<KirvesPlayer> one = this.players.stream()
                .filter(player -> player.getRoundsWon().size() == 1)
                .collect(toList());

        if(one.size() == 5) {
            //last round wins
            return this.players.stream()
                    .filter(player -> player.getRoundsWon().get(0) == 4)
                    .findFirst().orElseThrow(KirvesGameException::new);
        } else {
            throw new KirvesGameException("Unable to determine hand winner");
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
        Card.Suit leaderSuit = leader.getSuit().equals(JOKER) || leader.getRank().equals(JACK) ? valtti : leader.getSuit();
        Card.Suit candidateSuit = candidate.getSuit().equals(JOKER) || candidate.getRank().equals(JACK) ? valtti : candidate.getSuit();

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
        if(this.players.size() > 1) {
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
            KirvesPlayer player = this.players.get(i);
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
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public enum Action {
        DEAL, PLAY_CARD, FOLD
    }
}
