package fi.bizhop.kirves.model.kirves;

import fi.bizhop.kirves.exception.CardException;
import fi.bizhop.kirves.exception.KirvesGameException;
import fi.bizhop.kirves.model.Card;
import fi.bizhop.kirves.model.Cards;
import fi.bizhop.kirves.model.User;
import fi.bizhop.kirves.model.kirves.out.GameOut;
import fi.bizhop.kirves.model.kirves.out.PlayerOut;
import fi.bizhop.kirves.model.kirves.out.ScoreHistoryOut;
import fi.bizhop.kirves.model.kirves.out.ScoreOut;
import fi.bizhop.kirves.model.kirves.pojo.GameDataPOJO;
import fi.bizhop.kirves.model.kirves.pojo.ScoreHistoryPOJO;
import fi.bizhop.kirves.model.kirves.pojo.ScorePOJO;
import fi.bizhop.kirves.model.kirves.pojo.UserPOJO;
import fi.bizhop.kirves.util.JsonUtil;
import fi.bizhop.kirves.util.RandomUtil;

import java.util.*;

import static fi.bizhop.kirves.exception.KirvesGameException.Type.BAD_REQUEST;
import static fi.bizhop.kirves.model.Card.Rank.*;
import static fi.bizhop.kirves.model.Card.Suit.*;
import static fi.bizhop.kirves.model.kirves.Game.Action.*;
import static fi.bizhop.kirves.model.kirves.Game.Speak.CHANGE;
import static fi.bizhop.kirves.model.kirves.Game.Speak.KEEP;
import static java.util.stream.Collectors.toList;

public class Game {
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 6;
    private static final int NUM_OF_CARD_TO_DEAL = 5;
    public static final long DEPARTURE_TIMEOUT_MS = 60 * 1000;

    private final Cards deck;
    private final List<Player> players = new ArrayList<>();
    private final String admin;
    private Player turn;
    private Player dealer;
    private Player firstPlayerOfRound;
    private Card trumpCard = null;
    private Card.Suit trump = null;
    private Card cutCard = null;
    private Card secondCutCard = null;
    
    private final GameDataPOJO data;

    public Game(GameDataPOJO pojo) throws KirvesGameException {
        if(pojo == null) throw new KirvesGameException("GameDataPOJO ei voi olla tyhjä (null)", BAD_REQUEST);
        this.data = pojo;
        this.admin = pojo.admin;
        this.deck = Cards.fromAbbreviations(pojo.deck);

        //map the players
        Player previous = null;
        var playersMap = new LinkedHashMap<String, Player>();
        for(var playerPOJO : pojo.players) {
            var current = new Player(playerPOJO, previous);
            playersMap.put(playerPOJO.user.email, current);
            previous = current;
        }
        this.players.addAll(playersMap.values());
        var last = this.players.get(this.players.size() - 1);
        this.players.get(0).setPrevious(last);
        last.setNext(this.players.get(0));

        this.turn = playersMap.get(pojo.turn);
        this.dealer = playersMap.get(pojo.dealer);
        this.firstPlayerOfRound = playersMap.get(pojo.firstPlayerOfRound);
        this.trumpCard = Card.fromAbbreviation(pojo.trumpCard);
        this.trump = Card.Suit.fromAbbreviation(pojo.trump);
        this.cutCard = Card.fromAbbreviation(pojo.cutCard);
        this.secondCutCard = Card.fromAbbreviation(pojo.secondCutCard);
    }

    public Game(User admin) throws CardException, KirvesGameException {
        this.data = new GameDataPOJO();
        this.deck = new Deck().shuffle();
        this.data.canJoin = true;
        this.admin = admin.getEmail();

        var player = this.addPlayerInternal(admin.toPOJO());
        this.setDealer(player);
        //nobody can cut before the second player joins
        this.resetActions();
    }

    public String toJson() throws KirvesGameException {
        this.data.players = this.players.stream().map(Player::toPojo).collect(toList());
        this.data.admin = this.admin;
        this.data.deck = this.deck.getCardsOut();
        this.data.turn = this.turn == null ? null : this.turn.getUserEmail();
        this.data.dealer = this.dealer == null ? null : this.dealer.getUserEmail();
        this.data.firstPlayerOfRound = this.firstPlayerOfRound == null ? null : this.firstPlayerOfRound.getUserEmail();
        this.data.trumpCard = this.trumpCard == null ? null : this.trumpCard.toString();
        this.data.trump = this.trump == null ? null : this.trump.getAbbreviation();
        this.data.cutCard = this.cutCard == null ? null : this.cutCard.toString();
        this.data.secondCutCard = this.secondCutCard == null ? null : this.secondCutCard.toString();

        return JsonUtil.getJson(this.data)
                .orElseThrow(() -> new KirvesGameException("Muunnos GameDataPOJO -> json ei onnistunut"));
    }

    public GameOut out() throws KirvesGameException {
        return this.out(null);
    }

    public GameOut out(User user) throws KirvesGameException {
        List<String> myCards = new ArrayList<>();
        List<String> myActions = new ArrayList<>();
        String myExtraCard = null;
        if(user != null) {
            var me = this.getPlayer(user.getEmail());
            if(me.isPresent()) {
                var player = me.get();
                myCards = player.getHand().getCardsOut();
                myActions = player.getAvailableActions().stream()
                        .map(Enum::name)
                        .collect(toList());
                var extraCard = player.getExtraCard();
                if(extraCard != null) {
                    myExtraCard = extraCard.toString();
                }
            } else {
                user = null;
            }
        }

        return GameOut.builder()
                .players(this.getSeatsStartingFrom(user).stream().map(PlayerOut::new).collect(toList()))
                .messages(this.data.messages)
                .cardsInDeck(this.deck.size())
                .turn(this.turn.getUserEmail())
                .dealer(this.dealer.getUserEmail())
                .myCardsInHand(myCards)
                .myExtraCard(myExtraCard)
                .myAvailableActions(myActions)
                .canJoin(this.data.canJoin && (user == null || !this.hasPlayer(user)))
                .canDeclineCut(this.data.canDeclineCut)
                .trumpCard(this.trumpCard == null ? "" : this.trumpCard.toString())
                .trump(this.trump == null ? "" : this.trump.toString())
                .cutCard(this.cutCard == null ? "" : this.cutCard.toString())
                .secondCutCard(this.secondCutCard == null ? "" : this.secondCutCard.toString())
                .playersTotal(this.players.size())
                .firstCardSuit(this.getFirstCardSuit())
                .scores(this.data.scores.stream().map(ScoreOut::new).toList())
                .scoresHistory(this.data.scoresHistory.stream().map(ScoreHistoryOut::new).toList())
                .departureSecondsLeft(this.getDepartureSecondsLeft(System.currentTimeMillis()))
                .build();
    }

    //every seat, including players who are out of the hand, rotated to start from the viewer
    private List<Player> getSeatsStartingFrom(User user) {
        int start = 0;
        while(user != null && start < this.players.size() && !this.players.get(start).getUserEmail().equals(user.getEmail())) {
            start++;
        }
        if(user == null || start == 0 || start == this.players.size()) return this.players;
        var seats = new ArrayList<>(this.players.subList(start, this.players.size()));
        seats.addAll(this.players.subList(0, start));
        return seats;
    }

    private ScorePOJO getScore(String email) throws KirvesGameException {
        return this.data.scores.stream()
                .filter(score -> score.email.equals(email))
                .findFirst()
                .orElseThrow(() -> new KirvesGameException(String.format("Pisteitä ei löytynyt pelaajalle %s", email)));
    }

    private String getFirstCardSuit() {
        if(this.firstPlayerOfRound == null || this.trump == null) return "";
        var firstCard = this.firstPlayerOfRound.equals(this.turn) ? null : this.firstPlayerOfRound.getLastPlayedCard();
        if(firstCard == null) return "";
        if(firstCard.getSuit() == JOKER || firstCard.getRank() == JACK) {
            return this.trump.name();
        } else {
            return firstCard.getSuit().name();
        }
    }

    public Optional<Player> getPlayer(String email) {
        return this.players.stream().filter(player -> player.getUserEmail().equals(email)).findFirst();
    }

    public Optional<Player> getRoundWinner(int round) {
        return this.players.stream().filter(player -> player.getRoundsWon().contains(round)).findFirst();
    }

    public synchronized void addPlayer(User user) throws KirvesGameException {
        if(this.players.size() >= MAX_PLAYERS) {
            throw new KirvesGameException("Peli on täynnä", BAD_REQUEST);
        }
        if(this.data.canJoin) {
            if (this.players.stream()
                    .noneMatch(player -> user.getEmail().equals(player.getUserEmail()))) {
                var player = this.addPlayerInternal(user.toPOJO());
                this.resetActions();
                player.setAvailableActions(List.of(CUT));
                if(this.players.size() == MAX_PLAYERS) {
                    this.data.canJoin = false;
                }
            } else {
                throw new KirvesGameException(String.format("Pelaaja %s on jo pelissä", user.getNickname()), BAD_REQUEST);
            }
        } else {
            throw new KirvesGameException("Tähän peliin ei voi liittyä nyt", BAD_REQUEST);
        }
    }

    private void resetActions() {
        this.players.forEach(Player::resetAvailableActions);
    }

    private Player addPlayerInternal(UserPOJO user) {
        Player player;
        if(this.players.size() > 0) {
            var last = this.players.get(this.players.size() - 1);
            player = new Player(user, this.dealer, last);
        } else {
            player = new Player(user);
        }
        this.players.add(player);
        this.data.scores.add(new ScorePOJO(user.email, user.getNickname(), 0));
        return player;
    }

    public void deal(User user) throws CardException, KirvesGameException {
        this.deal(user, null);
    }

    //use this method directly only when testing!
    public void deal(User user, List<Card> possibleTrumpCards) throws CardException, KirvesGameException {
        if(!this.data.canDeal) throw new KirvesGameException("Jakaminen ei onnistu", BAD_REQUEST);
        var players = this.getPlayersStartingFrom(this.dealer.getUserEmail());
        for(var player : players) {
            player.getPlayedCards().clear();
            player.addCards(this.deck.deal(NUM_OF_CARD_TO_DEAL));
        }
        if(possibleTrumpCards == null) {
            //normal flow
            this.trumpCard = this.deck.remove(0);
        }
        else {
            //test flow
            while(true) {
                var candidate = this.deck.get(RandomUtil.getInt(this.deck.size()));
                if(possibleTrumpCards.contains(candidate)) {
                    this.trumpCard = this.deck.removeCard(candidate);
                    break;
                }
            }
        }
        if(this.trumpCard.getSuit() == JOKER) {
            this.trump = this.trumpCard.getRank() == BLACK ? SPADES : HEARTS;
        } else {
            this.trump = this.trumpCard.getSuit();
        }
        //yhteinen tai väkyri
        if(     players.stream().anyMatch(player -> player.getExtraCard() != null) ||
                this.trumpCard.getSuit() == JOKER || this.trumpCard.getRank() == JACK
        ) {
            this.dealer.setExtraCard(this.trumpCard);
            this.dealer.setDeclaredPlayer(true);
            this.trumpCard = null;
            this.data.forcedGame = true;
        } else if (this.trumpCard.getRank() == TWO || this.trumpCard.getRank() == ACE) {
            this.dealer.hideCards(this.trumpCard.getRank() == TWO ? 2 : 3);
            this.dealer.setExtraCard(this.trumpCard);
            this.trumpCard = null;
        }
        this.data.canDeal = false;
        this.cutCard = null;
        this.secondCutCard = null;
        this.data.speaking = true;
        var player = this.getPlayer(user.getEmail()).orElseThrow(() -> new KirvesGameException(String.format("'%s' ei löytynyt pelaajista", user.getNickname())));
        var nextPlayer = player.getNext(this.players.size());
        this.setCardPlayer(nextPlayer);
        this.firstPlayerOfRound = nextPlayer;
    }

    //use this method directly only when testing!
    public void cut(User cutter, boolean decline, Card cutCard, Card second) throws CardException, KirvesGameException {
        if(this.players.size() < MIN_PLAYERS) {
            throw new KirvesGameException(String.format("Peliin tarvitaan vähintään %d pelaajaa", MIN_PLAYERS), BAD_REQUEST);
        }
        if(decline && !this.data.canDeclineCut) {
            throw new KirvesGameException("Nostosta ei voi kieltäytyä", BAD_REQUEST);
        }
        //the dealer can't be changed after the cut
        this.players.forEach(player -> player.removeAvailableAction(SET_DEALER));
        this.deck.clear();
        this.deck.add(new Deck().shuffle());
        if(!decline) {
            this.cutCard = cutCard != null ? this.deck.removeCard(cutCard) : this.deck.remove(RandomUtil.getInt(this.deck.size()));
            if (this.cutCard.getRank() == JACK || this.cutCard.getSuit() == JOKER) {
                var secondAfterCut = second != null ? this.deck.removeCard(second) : this.deck.remove(RandomUtil.getInt(this.deck.size()));
                this.secondCutCard = secondAfterCut;
                if (secondAfterCut.getRank() == JACK || secondAfterCut.getSuit() == JOKER) {
                    this.data.messages.add(String.format("Uusi nosto, %s voi kieltäytyä nostamasta", cutter.getNickname()));
                    this.data.canDeclineCut = true;
                    return;
                }
                var cutterPlayer = this.getPlayer(cutter.getEmail()).orElseThrow(() -> new KirvesGameException("Nostajaa ei löydy pelaajista"));
                cutterPlayer.setExtraCard(this.cutCard);
                this.data.forcedGame = true;
            }
        } else {
            this.data.messages.add(String.format("%s kieltäytyi nostosta", cutter.getNickname()));
        }
        //all seats, so that departed players' cards from the previous hand are cleared too
        this.players.forEach(player -> {
            player.setDeclaredPlayer(false);
            player.resetAvailableActions();
            player.getPlayedCards().clear();
            player.setSpeak(null);
            player.resetWonRounds();
        });
        this.dealer.setAvailableActions(List.of(DEAL));
        this.turn = this.dealer;
        this.data.canDeal = true;
        this.data.canJoin = false;
    }

    public void cut(User cutter, boolean decline) throws CardException, KirvesGameException {
        this.cut(cutter, decline, null, null);
    }

    public void aceOrTwoDecision(User user, boolean keepExtraCard) throws KirvesGameException {
        var player = this.getPlayer(user.getEmail()).orElseThrow(() -> new KirvesGameException("Pelaajaa ei löytynyt"));
        if(keepExtraCard) {
            this.data.speaking = false;
        } else {
            this.trumpCard = player.getExtraCard();
            player.setExtraCard(null);
        }
        player.moveInvisibleCardsToHand();
        this.setCardPlayer(this.dealer.getNext(this.players.size()));
    }

    public void discard(User user, int index) throws KirvesGameException, CardException {
        var player = this.getPlayer(user.getEmail()).orElseThrow(() -> new KirvesGameException("Pelaajaa ei löytynyt"));
        player.discard(index);
        //anyone discarding is always declared player
        player.setDeclaredPlayer(true);
        this.setCardPlayer(this.dealer.getNext(this.players.size()));
    }

    public void speak(User user, Speak speak) throws KirvesGameException {
        if(speak == null) throw new KirvesGameException("Puhe ei voi olla tyhjä (null)", BAD_REQUEST);

        var player = this.getPlayer(user.getEmail()).orElseThrow(() -> new KirvesGameException("Pelaajaa ei löytynyt"));
        if(speak == KEEP) {
            player.setDeclaredPlayer(true);
            player.setSpeak(KEEP);
            this.data.speaking = false;
            this.setCardPlayer(this.firstPlayerOfRound);
        } else {
            player.setSpeak(speak);
            var next = player.getNext(this.players.size());
            if(this.firstPlayerOfRound.equals(next)) {
                var changer = this.getPlayersStartingFrom(this.firstPlayerOfRound.getUserEmail()).stream()
                        .filter(s -> s.getSpeak() == CHANGE)
                        .findFirst();
                if(changer.isPresent()) {
                    player.resetAvailableActions();
                    changer.orElseThrow().setAvailableActions(List.of(SPEAK_SUIT));
                } else {
                    this.startNextRound();
                }
            } else {
                this.setCardPlayer(next);
            }
        }
    }

    public void speakSuit(User user, Card.Suit suit) throws KirvesGameException {
        if(suit == null) throw new KirvesGameException("Valttimaa ei voi olla tyhjä (null)", BAD_REQUEST);
        if(suit == this.trump) throw new KirvesGameException(String.format("Pitää valita eri maa kuin %s", suit), BAD_REQUEST);

        var player = this.getPlayer(user.getEmail()).orElseThrow(() -> new KirvesGameException("Pelaajaa ei löytynyt"));
        player.setDeclaredPlayer(true);
        this.trump = suit;
        this.trumpCard = null;
        this.data.speaking = false;
        this.setCardPlayer(this.firstPlayerOfRound);
    }

    public void playCard(User user, int index) throws KirvesGameException, CardException {
        var player = this.getPlayer(user.getEmail()).orElseThrow(() -> new KirvesGameException("Pelaajaa ei löytynyt"));
        player.playCard(index);
        this.setCardPlayer(player.getNext(this.players.size()));
        this.determinePossibleRoundWinner(true);
    }

    public void fold(User user) throws KirvesGameException {
        var player = this.getPlayer(user.getEmail()).orElseThrow(() -> new KirvesGameException("Pelaajaa ei löytynyt"));
        //folding is also possible out of turn at the start of a round, when no cards have been played yet
        var roundInProgress = !this.turn.equals(this.firstPlayerOfRound);
        var wasTurn = this.turn.equals(player);
        player.fold();
        if(this.firstPlayerOfRound.equals(player)) {
            this.firstPlayerOfRound = player.getNext(this.players.size());
        }
        if(wasTurn) {
            this.setCardPlayer(player.getNext(this.players.size()));
        }
        this.determinePossibleRoundWinner(roundInProgress);
        this.data.messages.add(String.format("%s meni pakkaan", user.getNickname()));
    }

    private void determinePossibleRoundWinner(boolean roundInProgress) throws KirvesGameException {
        var players = this.getPlayersStartingFrom(this.firstPlayerOfRound.getUserEmail());
        if(players.size() == 0) throw new KirvesGameException("Virhe: 0 pelaajaa jäljellä");
        if(players.size() == 1) {
            var winner = players.get(0);
            this.handleScoring(Set.of(winner));
        } else if(roundInProgress && this.turn.equals(this.firstPlayerOfRound)) {
            var playedCards = players.stream()
                    .map(Player::getLastPlayedCard)
                    .collect(toList());

            var winningCard = winningCard(playedCards, this.trump);
            var roundWinner = players.stream()
                    .filter(playerItem -> winningCard.equals(playerItem.getLastPlayedCard()))
                    .findFirst().orElseThrow(() -> new KirvesGameException("Voittokorttia ei löytynyt pelatuista korteista"));
            roundWinner.addRoundWon();

            if(roundWinner.cardsInHand() != 0) {
                this.setCardPlayer(roundWinner);
                this.firstPlayerOfRound = roundWinner;
            }
            else {
                var handWinner = determineHandWinner(players);
                var winners = determineScoringWinners(players, handWinner);
                this.handleScoring(winners);
            }
        }
    }

    public static Set<Player> determineScoringWinners(List<Player> players, Player handWinner) {
        Set<Player> winners = new HashSet<>();
        for(var player : players) {
            //case: player is handWinner
            if(player.equals(handWinner)) {
                winners.add(player);
            } else {
                //case: other player didn't win as declared player
                if(!player.isDeclaredPlayer()) {
                    var otherPlayers = new ArrayList<>(players);
                    otherPlayers.remove(player);
                    for (var other : otherPlayers) {
                        if (other.isDeclaredPlayer() && !other.equals(handWinner)) {
                            winners.add(player);
                        }
                    }
                }
            }
        }
        return winners;
    }

    //package-private for tests
    void handleScoring(Set<Player> winners) throws KirvesGameException {
        var label = winners.size() == 1 ? "Voittaja" : "Voittajat";
        var winnerNicks = winners.stream().map(Player::getUserNickname).collect(toList());
        this.data.messages.add(String.format("%s: %s", label, String.join(",", winnerNicks)));
        var departing = new ArrayList<Player>();
        for(var winner : winners) {
            var score = this.getScore(winner.getUserEmail());
            score.score++;
            if(score.score == 3) {
                departing.add(winner);
            }
        }
        if(departing.isEmpty()) {
            this.continueAfterHand();
        } else {
            //the table stays as it is until every departing player has acknowledged or the timeout expires
            this.resetActions();
            departing.forEach(Player::depart);
            this.restartDepartureTimeout();
            this.updateContinueAction();
        }
    }

    public void restartDepartureTimeout() {
        if(this.isDeparturePending()) {
            this.data.departureDeadline = System.currentTimeMillis() + DEPARTURE_TIMEOUT_MS;
        }
    }

    public Long getDepartureDeadline() {
        return this.data.departureDeadline;
    }

    private Integer getDepartureSecondsLeft(long now) {
        if(this.data.departureDeadline == null) return null;
        return (int) Math.max(0, (this.data.departureDeadline - now + 999) / 1000);
    }

    //the admin can continue the game for players who have not acknowledged their departure
    private void updateContinueAction() {
        this.players.forEach(player -> player.removeAvailableAction(CONTINUE_GAME));
        var admin = this.getPlayer(this.admin);
        if(admin.isPresent() && !admin.get().isDeparturePending() && this.isDeparturePending()) {
            admin.get().addAvailableAction(CONTINUE_GAME);
        }
    }

    public void continueGame(User user) throws KirvesGameException {
        if(!user.getEmail().equals(this.admin) || !this.isDeparturePending()) {
            throw new KirvesGameException("Peliä ei voi jatkaa nyt", BAD_REQUEST);
        }
        this.data.messages.add(String.format("%s jatkoi peliä", this.getPlayer(user.getEmail()).map(Player::getUserNickname).orElse(user.getNickname())));
        this.acknowledgeAllDepartures();
    }

    /**
     * Acknowledge pending departures once their timeout has expired.
     *
     * @return true if the game continued
     */
    public boolean expireDepartures(long now) throws KirvesGameException {
        if(this.data.departureDeadline == null || now < this.data.departureDeadline || !this.isDeparturePending()) {
            return false;
        }
        this.data.messages.add("Poistuminen kuitattiin automaattisesti");
        this.acknowledgeAllDepartures();
        return true;
    }

    private void acknowledgeAllDepartures() throws KirvesGameException {
        for(var player : this.players) {
            if(player.isDeparturePending()) {
                player.acknowledgeDeparture();
            }
        }
        this.finishDepartures();
    }

    public void acknowledgeDeparture(User user) throws KirvesGameException {
        var player = this.getPlayer(user.getEmail()).orElseThrow(() -> new KirvesGameException("Pelaajaa ei löytynyt"));
        if(!player.isDeparturePending()) {
            throw new KirvesGameException("Poistumista ei voi kuitata nyt", BAD_REQUEST);
        }
        player.acknowledgeDeparture();
        if(this.isDeparturePending()) {
            this.updateContinueAction();
            return;
        }
        this.finishDepartures();
    }

    private void finishDepartures() throws KirvesGameException {
        this.data.departureDeadline = null;
        for(var departed : this.players) {
            if(!departed.isInGame() && !departed.isFolded()) {
                departed.discardHand();
            }
        }
        this.continueAfterHand();
    }

    private boolean isDeparturePending() {
        return this.players.stream().anyMatch(Player::isDeparturePending);
    }

    private void continueAfterHand() throws KirvesGameException {
        this.startNextRound();
        if(this.getNumberOfPlayers(true) < 2) {
            this.endGame();
        }
    }

    /**
     * The only player left in game (below 3 points) is the loser. Their second loss in a match is
     * the axe, which ends the match. The loser deals the next game by default, but may choose
     * another dealer before the first cut.
     */
    private void endGame() throws KirvesGameException {
        var loser = this.players.stream().filter(Player::isInGame).findFirst()
                .orElseThrow(() -> new KirvesGameException("Häviäjää ei löytynyt"));
        var loserEmail = loser.getUserEmail();
        var axe = this.getLossesInMatch(loserEmail) == 1;
        this.data.scoresHistory.add(new ScoreHistoryPOJO(
                this.data.scores.stream().map(ScorePOJO::copy).collect(toList()), loserEmail, axe));
        this.data.scores = this.players.stream()
                .map(player -> new ScorePOJO(player.getUserEmail(), player.getUserNickname(), 0))
                .collect(toList());
        this.players.forEach(Player::activate);
        this.data.messages.add(String.format("Hävisi: %s", loser.getUserNickname()));
        if(axe) {
            this.data.messages.add(String.format("%s sai kirveen", loser.getUserNickname()));
        }
        this.setDealer(loser);
        loser.addAvailableAction(SET_DEALER);
    }

    private int getLossesInMatch(String email) {
        int losses = 0;
        for(int i = this.data.scoresHistory.size() - 1; i >= 0; i--) {
            var entry = this.data.scoresHistory.get(i);
            if(entry.axe) break;
            if(email.equals(entry.loser)) losses++;
        }
        return losses;
    }

    public void chooseDealer(User user, String dealerEmail) throws KirvesGameException {
        var chooser = this.getPlayer(user.getEmail()).orElseThrow(() -> new KirvesGameException("Pelaajaa ei löytynyt"));
        var dealer = this.getPlayer(dealerEmail).orElseThrow(() -> new KirvesGameException("Jakajaa ei löytynyt pelaajista", BAD_REQUEST));
        this.setDealer(dealer);
        chooser.addAvailableAction(SET_DEALER);
        this.data.messages.add(String.format("%s valitsi jakajaksi pelaajan %s", chooser.getUserNickname(), dealer.getUserNickname()));
    }

    public void startNextRound() throws KirvesGameException {
        this.players.stream()
                .filter(Player::isFolded)
                .forEach(Player::activate);
        this.setDealer(this.dealer.getNext(this.players.size()));
    }

    private List<Player> getPlayersStartingFrom(String userEmail) throws KirvesGameException {
        if(userEmail == null) {
            return this.players;
        }
        var player = this.getPlayer(userEmail).orElseThrow(() -> new KirvesGameException("Pelaajaa ei löytynyt"));
        return getPlayersStartingFrom(player, userEmail, this.players.size());
    }

    public static List<Player> getPlayersStartingFrom(Player item, String userEmail, int playersCount) throws KirvesGameException {
        List<Player> players = new ArrayList<>();
        if(!item.isInGame()) {
            item = item.getNext(playersCount);
            if(userEmail.equals(item.getUserEmail())) return List.of(item);
            userEmail = item.getUserEmail();
        }
        int safetyCounter = 0;
        do {
            safetyCounter++;
            if(safetyCounter > playersCount) throw new KirvesGameException("Virhe pelaajien linkityksessä");
            players.add(item);
            item = item.getNext(playersCount);
        } while (!item.getUserEmail().equals(userEmail));

        return players;
    }

    private void setCardPlayer(Player player) throws KirvesGameException {
        this.resetActions();
        var needsToDiscard = this.getPlayersStartingFrom(player.getUserEmail()).stream()
                .filter(item -> item.getExtraCard() != null)
                .findFirst();
        if(this.dealer.hasInvisibleCards()) {
            this.turn = this.dealer;
            this.turn.setAvailableActions(List.of(ACE_OR_TWO_DECISION));
        }
        else if(needsToDiscard.isPresent()) {
            needsToDiscard.orElseThrow().setAvailableActions(List.of(DISCARD));
            this.turn = needsToDiscard.get();
        }
        else {
            if(!player.isInGame()) {
                player = player.getNext(this.players.size());
            }
            this.turn = player;
            if(this.data.speaking && !this.data.forcedGame) {
                this.turn.setAvailableActions(List.of(SPEAK));
            }
            else {
                this.turn.setAvailableActions(List.of(PLAY_CARD));

                //set folding possibility to players
                int numOfActivePlayers = this.getNumberOfPlayers(true);
                this.getPlayersStartingFrom(player.getUserEmail()).stream()
                        .filter(p -> canFold(p, this.firstPlayerOfRound, this.turn, this.trump, numOfActivePlayers))
                        .forEach(p -> p.addAvailableAction(FOLD));
            }
        }
    }

    public static boolean canFold(Player player, Player firstPlayerOfRound, Player turn, Card.Suit trump, int numberOfPlayers) {
        if(numberOfPlayers < 2 || trump == null) return false;
        if(!firstPlayerOfRound.equals(turn)) return false;
        if(player.cardsInHand() == 5) return !player.equals(firstPlayerOfRound);
        return player.getHand().hasNoTrumpCard(trump);
    }

    private void setDealer(Player dealer) throws KirvesGameException {
        this.getPlayersStartingFrom(dealer.getUserEmail()).forEach(Player::clearHand);
        this.dealer = dealer;
        this.data.canDeal = false;
        this.trumpCard = null;
        this.trump = null;
        this.data.speaking = false;
        this.data.forcedGame = false;
        this.data.canDeclineCut = false;
        this.resetActions();
        this.turn = dealer.getPrevious(this.players.size());
        this.turn.setAvailableActions(List.of(CUT));
    }

    public static Player determineHandWinner(List<Player> players) throws KirvesGameException {
        //three or more rounds is clear winner
        var threeOrMore = players.stream()
                .filter(player -> player.getRoundsWon().size() >= 3)
                .findFirst();
        if(threeOrMore.isPresent()) {
            return threeOrMore.get();
        }

        //two rounds
        List<Player> two = players.stream()
                .filter(player -> player.getRoundsWon().size() == 2)
                .collect(toList());

        if(two.size() == 1) {
            //only one player with two rounds is winner
            return two.get(0);
        } else if(two.size() == 2) {
            //two players with two rounds, first two wins
            Player first = two.get(0);
            Player second = two.get(1);

            return first.getRoundsWon().get(1) > second.getRoundsWon().get(1) ? second : first;
        }

        //if these cases don't return anything, there should be five single round winners
        List<Player> one = players.stream()
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

    public static Card winningCard(List<Card> playedCards, Card.Suit trump) {
        int leader = 0;
        for(int i = 1; i < playedCards.size(); i++) {
            var leaderCard = playedCards.get(leader);
            var candidate = playedCards.get(i);
            if(candidateWins(leaderCard, candidate, trump)) {
                leader = i;
            }
        }
        return playedCards.get(leader);
    }

    public boolean userHasActionAvailable(User user, Action action) {
        return this.getPlayer(user.getEmail())
                .map(player -> player.getAvailableActions().contains(action))
                .orElse(false);
    }

    public Optional<User> getUserWithAction(Action action) {
        return this.players.stream()
                .map(player -> player.getAvailableActions().contains(action) ? player.getUser() : null)
                .filter(Objects::nonNull)
                .map(User::new)
                .findFirst();
    }

    private static boolean candidateWins(Card leader, Card candidate, Card.Suit trump) {
        int leaderRank = getConvertedRank(leader);
        int candidateRank = getConvertedRank(candidate);
        var leaderSuit = leader.getSuit() == JOKER || leader.getRank() == JACK ? trump : leader.getSuit();
        var candidateSuit = candidate.getSuit() == JOKER || candidate.getRank() == JACK ? trump : candidate.getSuit();

        if(candidateSuit == trump && leaderSuit != trump) {
            return true;
        }
        else return candidateSuit == leaderSuit &&
                candidateRank > leaderRank;
    }

    private static int getConvertedRank(Card card) {
        if(card.getRank() == JACK) {
            switch (card.getSuit()) {
                case DIAMONDS: return 15;
                case HEARTS: return 16;
                case SPADES: return 17;
                case CLUBS: return 18;
            }
        }
        return card.getRank().getValue();
    }

    public boolean hasPlayer(User user) {
        return this.players.stream().anyMatch(player -> user.getEmail().equals(player.getUserEmail()));
    }

    public Card getCutCard() {
        return this.cutCard;
    }

    public Card.Suit getTrump() {
        return this.trump;
    }

    public Card getExtraCard(User user) {
        if(user == null) return null;
        return this.getPlayer(user.getEmail())
                .map(Player::getExtraCard)
                .orElse(null);
    }

    public int getNumberOfPlayers() {
        return this.getNumberOfPlayers(false);
    }

    private int getNumberOfPlayers(boolean onlyActive) {
        if(onlyActive) {
            return (int) this.players.stream().filter(Player::isInGame).count();
        } else {
            return this.players.size();
        }
    }

    public Boolean getCanJoin() {
        return this.data.canJoin;
    }

    public Long getCurrentHandId() { return this.data.currentHandId; }

    public Long incrementHandId() {
        this.data.currentHandId = this.data.currentHandId == null ? 0L : this.data.currentHandId + 1L;
        return this.data.currentHandId;
    }

    public String getAdmin() {
        return this.admin;
    }

    public enum Action {
        DEAL, PLAY_CARD, FOLD, CUT, ACE_OR_TWO_DECISION, SPEAK, SPEAK_SUIT, DISCARD, SET_DEALER, ACKNOWLEDGE_DEPARTURE, CONTINUE_GAME
    }

    public enum Speak {
        CHANGE, KEEP, PASS
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Game)) return false;
        var other = (Game)o;
        if(this.players.size() != other.players.size()) return false;
        if(this.firstPlayerOfRound == null && other.firstPlayerOfRound != null) return false;
        if(this.cutCard == null && other.cutCard != null) return false;
        if(this.secondCutCard == null && other.secondCutCard != null) return false;
        for(int i=0; i < this.players.size(); i++) {
            if(!(this.players.get(0).equals(other.players.get(0)))) return false;
        }
        return this.data.equals(other.data)
                && this.deck.equals(other.deck)
                && this.turn.equals(other.turn)
                && this.dealer.equals(other.dealer)
                && (this.firstPlayerOfRound == null || this.firstPlayerOfRound.equals(other.firstPlayerOfRound))
                && this.trumpCard == other.trumpCard
                && this.trump == other.trump
                && (this.cutCard == null || this.cutCard.equals(other.cutCard))
                && (this.secondCutCard == null || this.secondCutCard.equals(other.secondCutCard));
    }
}
