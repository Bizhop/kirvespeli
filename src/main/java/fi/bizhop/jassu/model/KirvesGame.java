package fi.bizhop.jassu.model;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;
import fi.bizhop.jassu.util.RandomUtil;

import java.io.Serializable;
import java.util.*;

import static fi.bizhop.jassu.model.Card.Rank.*;
import static fi.bizhop.jassu.model.Card.Suit.*;
import static fi.bizhop.jassu.model.KirvesGame.Action.*;
import static java.util.stream.Collectors.toList;

public class KirvesGame implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int NUM_OF_CARD_TO_DEAL = 5;

    private Cards deck;
    private final List<KirvesPlayer> players = new ArrayList<>();
    private boolean canJoin;
    private KirvesPlayer turn;
    private KirvesPlayer dealer;
    private boolean canDeal;
    private String message;
    private KirvesPlayer firstPlayerOfRound;
    private Card valttiCard = null;
    private Card.Suit valtti = null;
    private Card cutCard = null;
    private boolean canSetValtti;
    private boolean forcedGame;
    private boolean canDeclineCut;

    public KirvesGame(User admin) throws CardException {
        this.deck = new KirvesDeck().shuffle();
        this.canJoin = true;

        KirvesPlayer player = addPlayerInternal(admin);
        setDealer(player);
    }

    public KirvesGameOut out() throws KirvesGameException {
        return this.out(null);
    }

    public KirvesGameOut out(User user) throws KirvesGameException {
        List<String> myCards = new ArrayList<>();
        List<String> myActions = new ArrayList<>();
        String myExtraCard = null;
        if(user != null) {
            Optional<KirvesPlayer> me = getPlayer(user);
            if(me.isPresent()) {
                KirvesPlayer player = me.get();
                myCards = player.getHand().getCardsOut();
                myActions = player.getAvailableActions().stream()
                        .map(Enum::name)
                        .collect(toList());
                Card extraCard = player.getExtraCard();
                if(extraCard != null) {
                    myExtraCard = extraCard.toString();
                }
            }
        }
        return new KirvesGameOut(
                getPlayersStartingFrom(user).stream().map(KirvesPlayerOut::new).collect(toList()),
                this.deck.size(),
                this.dealer.getUserEmail(),
                this.turn.getUserEmail(),
                myCards,
                myExtraCard,
                myActions,
                this.message,
                this.canJoin,
                this.valttiCard == null ? "" : this.valttiCard.toString(),
                this.valtti == null ? "" : this.valtti.toString(),
                this.canDeclineCut,
                this.cutCard == null ? "" : this.cutCard.toString(),
                this.players.size()
        );
    }

    private Optional<KirvesPlayer> getPlayer(User user) {
        return this.players.stream().filter(player -> player.getUser().equals(user)).findFirst();
    }

    public Optional<KirvesPlayer> getRoundWinner(int round) {
        return this.players.stream().filter(player -> player.getRoundsWon().contains(round)).findFirst();
    }

    public void addPlayer(User user) throws KirvesGameException {
        if(this.canJoin) {
            if (this.players.stream()
                    .noneMatch(player -> user.getEmail().equals(player.getUserEmail()))) {
                KirvesPlayer player = addPlayerInternal(user);
                this.resetActions();
                player.setAvailableActions(List.of(CUT));
            } else {
                throw new KirvesGameException(String.format("Pelaaja %s on jo pelissä", user.getNickname()));
            }
        } else {
            throw new KirvesGameException("Tähän peliin ei voi liittyä nyt");
        }
    }

    private void resetActions() {
        this.players.forEach(KirvesPlayer::resetAvailableActions);
    }

    private KirvesPlayer addPlayerInternal(User user) {
        if(this.players.size() > 0) {
            KirvesPlayer last = this.players.get(this.players.size() - 1);
            KirvesPlayer player = new KirvesPlayer(user, this.dealer, last);
            this.players.add(player);
            return player;
        } else {
            KirvesPlayer player = new KirvesPlayer(user);
            this.players.add(player);
            return player;
        }
    }

    public void deal(User user) throws CardException, KirvesGameException {
        deal(user, null);
    }

    //use this method directly only when testing!
    public void deal(User user, List<Card> possibleValttiCards) throws CardException, KirvesGameException {
        if(!this.canDeal) throw new KirvesGameException("Jakaminen ei onnistu");
        List<KirvesPlayer> players = getPlayersStartingFrom(this.dealer.getUser());
        for(KirvesPlayer player : players) {
            player.getPlayedCards().clear();
            player.addCards(this.deck.deal(NUM_OF_CARD_TO_DEAL));
        }
        if(possibleValttiCards == null) {
            //normal flow
            this.valttiCard = this.deck.remove(0);
        }
        else {
            //test flow
            while(true) {
                Card candidate = this.deck.get(RandomUtil.getInt(this.deck.size()));
                if(possibleValttiCards.contains(candidate)) {
                    this.valttiCard = this.deck.removeCard(candidate);
                    break;
                }
            }
        }
        if(this.valttiCard.getSuit() == JOKER) {
            this.valtti = this.valttiCard.getRank() == BLACK ? SPADES : HEARTS;
        } else {
            this.valtti = this.valttiCard.getSuit();
        }
        //yhteinen tai väkyri
        if(     players.stream().anyMatch(player -> player.getExtraCard() != null) ||
                this.valttiCard.getSuit() == JOKER || this.valttiCard.getRank() == JACK
        ) {
            this.dealer.setExtraCard(this.valttiCard);
            this.dealer.setDeclaredPlayer(true);
            this.valttiCard = null;
            this.forcedGame = true;
        } else if (this.valttiCard.getRank() == TWO || this.valttiCard.getRank() == ACE) {
            this.dealer.hideCards(this.valttiCard.getRank() == TWO ? 2 : 3);
            this.dealer.setExtraCard(this.valttiCard);
            this.valttiCard = null;
        }
        this.canDeal = false;
        this.cutCard = null;
        this.canSetValtti = true;
        KirvesPlayer player = getPlayer(user).orElseThrow(() -> new KirvesGameException(String.format("'%s' ei löytynyt pelaajista", user.getNickname())));
        KirvesPlayer nextPlayer = player.getNext();
        setCardPlayer(nextPlayer);
        this.firstPlayerOfRound = nextPlayer;
        players.forEach(KirvesPlayer::resetWonRounds);
    }

    //use this method directly only when testing!
    public void cut(User cutter, boolean decline, Card cutCard, Card second) throws CardException, KirvesGameException {
        if(decline && !this.canDeclineCut) {
            throw new KirvesGameException("Nostosta ei voi kieltäytyä");
        }
        this.deck = new KirvesDeck().shuffle();
        if(!decline) {
            this.cutCard = cutCard != null ? this.deck.removeCard(cutCard) : this.deck.remove(RandomUtil.getInt(this.deck.size()));
            if (this.cutCard.getRank() == JACK || this.cutCard.getSuit() == JOKER) {
                Card secondAfterCut = second != null ? this.deck.removeCard(second) : this.deck.remove(RandomUtil.getInt(this.deck.size()));
                this.message = String.format("Seuraava kortti on %s", secondAfterCut);
                if (secondAfterCut.getRank() == JACK || secondAfterCut.getSuit() == JOKER) {
                    this.message += String.format("\nUusi nosto, %s voi kieltäytyä nostamasta", cutter.getNickname());
                    this.canDeclineCut = true;
                    return;
                }
                KirvesPlayer cutterPlayer = getPlayer(cutter).orElseThrow(() -> new KirvesGameException("Nostajaa ei löydy pelaajista"));
                cutterPlayer.setExtraCard(this.cutCard);
                this.forcedGame = true;
            }
        } else {
            this.message = String.format("%s kieltäytyi nostosta", cutter.getNickname());
        }
        getPlayersStartingFrom(this.dealer.getUser()).forEach(player -> {
            player.setDeclaredPlayer(false);
            player.resetAvailableActions();
        });
        this.dealer.setAvailableActions(List.of(DEAL));
        this.turn = this.dealer;
        this.canDeal = true;
        this.canJoin = false;
    }

    public void cut(User cutter, boolean decline) throws CardException, KirvesGameException {
        cut(cutter, decline, null, null);
    }

    public void aceOrTwoDecision(User user, boolean keepExtraCard) throws KirvesGameException {
        Optional<KirvesPlayer> me = getPlayer(user);
        if(me.isPresent()) {
            KirvesPlayer player = me.get();
            if(keepExtraCard) {
                this.canSetValtti = false;
            } else {
                this.valttiCard = player.getExtraCard();
                player.setExtraCard(null);
            }
            player.moveInvisibleCardsToHand();
            setCardPlayer(this.dealer.getNext());
        }
    }

    public void discard(User user, int index) throws KirvesGameException, CardException {
        Optional<KirvesPlayer> me = getPlayer(user);
        if(me.isPresent()) {
            KirvesPlayer player = me.get();
            player.discard(index);
            //anyone discarding is always declared player
            player.setDeclaredPlayer(true);
            setCardPlayer(this.dealer.getNext());
        } else {
            throw new KirvesGameException(String.format("'%s' ei ole tässä pelissä", user.getNickname()));
        }
    }

    /**
     * Set valtti
     *
     * @param user User
     * @param suit Set valtti to this suit. If same as turned valttiCard, keep current valttiCard.
     */
    public void setValtti(User user, Card.Suit suit, User declareUser) throws KirvesGameException {
        if(suit == null) throw new KirvesGameException("Valtti ei voi olla tyhjä (null)");
        Optional<KirvesPlayer> me = getPlayer(user);
        if(me.isPresent()) {
            Optional<KirvesPlayer> declarePlayer = getPlayer(declareUser);
            if(declarePlayer.isPresent()) {
                declarePlayer.get().setDeclaredPlayer(true);
            } else {
                throw new KirvesGameException(String.format("'%s' ei ole tässä pelissä", user.getNickname()));
            }
            KirvesPlayer player = me.get();
            if(suit != this.valtti) {
                this.valttiCard = null;
                this.valtti = suit;
            }
            this.canSetValtti = false;
            setCardPlayer(player);
        }
    }

    public void playCard(User user, int index) throws KirvesGameException, CardException {
        Optional<KirvesPlayer> me = getPlayer(user);
        if(me.isPresent()) {
            KirvesPlayer player = me.get();
            player.playCard(index);
            setCardPlayer(player.getNext());
            if(this.turn.equals(this.firstPlayerOfRound)) {
                List<KirvesPlayer> players = getPlayersStartingFrom(this.firstPlayerOfRound.getUser());
                List<Card> playedCards = players.stream()
                        .map(KirvesPlayer::getLastPlayedCard)
                        .collect(toList());

                Card winningCard = winningCard(playedCards, this.valtti);
                KirvesPlayer roundWinner = players.stream()
                        .filter(playerItem -> winningCard.equals(playerItem.getLastPlayedCard()))
                        .findFirst().orElseThrow(() -> new KirvesGameException("Voittokorttia ei löytynyt pelatuista korteista"));
                roundWinner.addRoundWon();

                if(roundWinner.cardsInHand() != 0) {
                    setCardPlayer(roundWinner);
                    this.firstPlayerOfRound = roundWinner;
                }
                else {
                    KirvesPlayer handWinner = determineHandWinner();
                    this.message = String.format("Voittaja on %s", handWinner.getUserNickname());
                    setDealer(this.dealer.getNext());
                }
            }
        } else {
            throw new KirvesGameException("Käyttäjä ei ole tässä pelissä");
        }
    }

    public void startNextRound(User user) throws KirvesGameException {
        getPlayersStartingFrom(user).forEach(KirvesPlayer::clearHand);
        setDealer(this.dealer.getNext());
    }

    public void adjustPlayersInGame(User user, boolean resetActivePlayers, Set<String> inactivateByEmail) throws KirvesGameException {
        Optional<KirvesPlayer> playerOpt = getPlayer(user);
        if (playerOpt.isPresent()) {
            if (resetActivePlayers) {
                this.players.forEach(KirvesPlayer::activate);
            } else {
                this.players.stream()
                        .filter(item -> inactivateByEmail.contains(item.getUserEmail()))
                        .forEach(KirvesPlayer::inactivate);
                //if active players would be less than 2, reset all players to active
                if (this.players.stream().filter(KirvesPlayer::isInGame).count() < 2) {
                    this.players.forEach(KirvesPlayer::activate);
                }
            }
            if(!this.dealer.isInGame()) {
                this.dealer = this.dealer.getNext();
            }
            resetActions();
            KirvesPlayer cutter = this.dealer.getPrevious();
            cutter.setAvailableActions(List.of(CUT, ADJUST_PLAYERS_IN_GAME));
        } else {
            throw new KirvesGameException(String.format("'%s' ei ole tässä pelissä", user.getNickname()));
        }
    }

    private List<KirvesPlayer> getPlayersStartingFrom(User user) throws KirvesGameException {
        if(user == null) {
            return this.players;
        }
        Optional<KirvesPlayer> start = getPlayer(user);
        if(start.isPresent()) {
            List<KirvesPlayer> players = new ArrayList<>();
            KirvesPlayer item = start.get();
            if(!item.isInGame()) throw new KirvesGameException(String.format("'%s' ei ole tässä pelissä", item.getUserNickname()));
            do {
                players.add(item);
            } while(!(item = item.getNext()).equals(start.get()));

            return players;
        } else {
            throw new KirvesGameException(String.format("'%s' ei ole tässä pelissä", user.getNickname()));
        }
    }

    private void setCardPlayer(KirvesPlayer player) throws KirvesGameException {
        this.resetActions();
        Optional<KirvesPlayer> needsToDiscard = getPlayersStartingFrom(player.getUser()).stream()
                .filter(item -> item.getExtraCard() != null)
                .findFirst();
        if(this.dealer.hasInvisibleCards()) {
            this.turn = this.dealer;
            this.turn.setAvailableActions(List.of(ACE_OR_TWO_DECISION));
        }
        else if(needsToDiscard.isPresent()) {
            needsToDiscard.get().setAvailableActions(List.of(DISCARD));
            this.turn = needsToDiscard.get();
        }
        else {
            if(!player.isInGame()) {
                player = player.getNext();
            }
            this.turn = player;
            this.turn.setAvailableActions(this.canSetValtti && !this.forcedGame ? List.of(SET_VALTTI) : List.of(PLAY_CARD));
        }
    }

    private void setDealer(KirvesPlayer dealer) {
        this.dealer = dealer;
        this.canDeal = false;
        this.valttiCard = null;
        this.valtti = null;
        this.canSetValtti = false;
        this.forcedGame = false;
        this.canDeclineCut = false;
        this.resetActions();
        this.turn = dealer.getPrevious();
        this.turn.setAvailableActions(List.of(CUT, ADJUST_PLAYERS_IN_GAME));
    }

    public KirvesPlayer determineHandWinner() throws KirvesGameException {
        List<KirvesPlayer> players = getPlayersStartingFrom(this.dealer.getUser());
        //three or more rounds is clear winner
        Optional<KirvesPlayer> threeOrMore = players.stream()
                .filter(player -> player.getRoundsWon().size() >= 3)
                .findFirst();
        if(threeOrMore.isPresent()) {
            return threeOrMore.get();
        }

        //two rounds
        List<KirvesPlayer> two = players.stream()
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
        List<KirvesPlayer> one = players.stream()
                .filter(player -> player.getRoundsWon().size() == 1)
                .collect(toList());

        if(one.size() == 5) {
            //last round wins
            return players.stream()
                    .filter(player -> player.getRoundsWon().get(0) == 4)
                    .findFirst().orElseThrow(KirvesGameException::new);
        } else {
            throw new KirvesGameException("Voittajan määritys ei onnistunut");
        }
    }

    public static Card winningCard(List<Card> playedCards, Card.Suit valtti) {
        int leader = 0;
        for(int i = 1; i < playedCards.size(); i++) {
            Card leaderCard = playedCards.get(leader);
            Card candidate = playedCards.get(i);
            if(candidateWins(leaderCard, candidate, valtti)) {
                leader = i;
            }
        }
        return playedCards.get(leader);
    }

    public boolean userHasActionAvailable(User user, Action action) {
        return this.getPlayer(user)
                .map(player -> player.getAvailableActions().contains(action))
                .orElse(false);
    }

    public Optional<User> getUserWithAction(Action action) {
        return this.players.stream()
                .map(player -> player.getAvailableActions().contains(action) ? player.getUser() : null)
                .filter(Objects::nonNull)
                .findFirst();
    }

    private static boolean candidateWins(Card leader, Card candidate, Card.Suit valtti) {
        int leaderRank = getConvertedRank(leader);
        int candidateRank = getConvertedRank(candidate);
        Card.Suit leaderSuit = leader.getSuit().equals(JOKER) || leader.getRank().equals(JACK) ? valtti : leader.getSuit();
        Card.Suit candidateSuit = candidate.getSuit().equals(JOKER) || candidate.getRank().equals(JACK) ? valtti : candidate.getSuit();

        if(candidateSuit.equals(valtti) && !leaderSuit.equals(valtti)) {
            return true;
        }
        else return candidateSuit.equals(leaderSuit) &&
                candidateRank > leaderRank;
    }

    private static int getConvertedRank(Card card) {
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

    public boolean hasPlayer(User user) {
        return this.players.stream().anyMatch(kirvesPlayer -> user.equals(kirvesPlayer.getUser()));
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Card getCutCard() {
        return this.cutCard;
    }

    public Card.Suit getValtti() {
        return this.valtti;
    }

    public Card getExtraCard(User user) {
        return getPlayer(user)
                .map(KirvesPlayer::getExtraCard)
                .orElse(null);
    }

    public Card getValttiCard() {
        return this.valttiCard;
    }

    public int getNumberOfPlayers() {
        return this.players.size();
    }

    public Boolean getCanJoin() {
        return this.canJoin;
    }

    public enum Action {
        DEAL, PLAY_CARD, FOLD, CUT, ACE_OR_TWO_DECISION, SPEAK, DISCARD, SET_VALTTI, ADJUST_PLAYERS_IN_GAME
    }
}
