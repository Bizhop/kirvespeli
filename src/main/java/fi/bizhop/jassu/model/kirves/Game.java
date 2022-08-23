package fi.bizhop.jassu.model.kirves;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;
import fi.bizhop.jassu.model.Card;
import fi.bizhop.jassu.model.Cards;
import fi.bizhop.jassu.model.User;
import fi.bizhop.jassu.model.kirves.out.GameOut;
import fi.bizhop.jassu.model.kirves.out.PlayerOut;
import fi.bizhop.jassu.model.kirves.pojo.GameDataPOJO;
import fi.bizhop.jassu.model.kirves.pojo.ScorePOJO;
import fi.bizhop.jassu.model.kirves.pojo.UserPOJO;
import fi.bizhop.jassu.util.JsonUtil;
import fi.bizhop.jassu.util.RandomUtil;

import java.util.*;

import static fi.bizhop.jassu.exception.KirvesGameException.Type.BAD_REQUEST;
import static fi.bizhop.jassu.model.Card.Rank.*;
import static fi.bizhop.jassu.model.Card.Suit.*;
import static fi.bizhop.jassu.model.kirves.Game.Action.*;
import static fi.bizhop.jassu.model.kirves.Game.Speak.CHANGE;
import static fi.bizhop.jassu.model.kirves.Game.Speak.KEEP;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class Game {
    private static final int NUM_OF_CARD_TO_DEAL = 5;

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
                .players(this.getPlayersStartingFrom(user == null ? null : user.getEmail()).stream().map(PlayerOut::new).collect(toList()))
                .messages(this.data.messages)
                .cardsInDeck(this.deck.size())
                .turn(this.turn.getUserEmail())
                .dealer(this.dealer.getUserEmail())
                .myCardsInHand(myCards)
                .myExtraCard(myExtraCard)
                .myAvailableActions(myActions)
                .canJoin(this.data.canJoin)
                .canDeclineCut(this.data.canDeclineCut)
                .trumpCard(this.trumpCard == null ? "" : this.trumpCard.toString())
                .trump(this.trump == null ? "" : this.trump.toString())
                .cutCard(this.cutCard == null ? "" : this.cutCard.toString())
                .secondCutCard(this.secondCutCard == null ? "" : this.secondCutCard.toString())
                .playersTotal(this.players.size())
                .firstCardSuit(this.getFirstCardSuit())
                .scores(this.getScoreOutput(this.data.scores))
                .scoresHistory(this.data.scoresHistory.stream().map(this::getScoreOutput).collect(toList()))
                .build();
    }

    private Map<String, Integer> getScoreOutput(Map<String, ScorePOJO> input) {
        return input.entrySet().stream().collect(toMap(entry -> entry.getValue().nickname, entry -> entry.getValue().score));
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

    public void addPlayer(User user) throws KirvesGameException {
        if(this.data.canJoin) {
            if (this.players.stream()
                    .noneMatch(player -> user.getEmail().equals(player.getUserEmail()))) {
                var player = this.addPlayerInternal(user.toPOJO());
                this.resetActions();
                player.setAvailableActions(List.of(CUT));
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
        this.data.scores.put(user.email, new ScorePOJO(user.getNickname(), 0));
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
        if(decline && !this.data.canDeclineCut) {
            throw new KirvesGameException("Nostosta ei voi kieltäytyä", BAD_REQUEST);
        }
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
        this.getPlayersStartingFrom(this.dealer.getUserEmail()).forEach(player -> {
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
        this.determinePossibleRoundWinner();
    }

    public void fold(User user) throws KirvesGameException {
        var player = this.getPlayer(user.getEmail()).orElseThrow(() -> new KirvesGameException("Pelaajaa ei löytynyt"));
        player.fold();
        if(this.firstPlayerOfRound.equals(player)) {
            this.firstPlayerOfRound = player.getNext(this.players.size());
        }
        this.setCardPlayer(player.getNext(this.players.size()));
        this.determinePossibleRoundWinner();
        this.data.messages.add(String.format("%s meni pakkaan", user.getNickname()));
    }

    private void determinePossibleRoundWinner() throws KirvesGameException {
        var players = this.getPlayersStartingFrom(this.firstPlayerOfRound.getUserEmail());
        if(players.size() == 0) throw new KirvesGameException("Virhe: 0 pelaajaa jäljellä");
        if(players.size() == 1) {
            var winner = players.get(0);
            this.handleScoring(Set.of(winner));
        } else if(this.turn.equals(this.firstPlayerOfRound)) {
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

    private void handleScoring(Set<Player> winners) throws KirvesGameException {
        var label = winners.size() == 1 ? "Voittaja" : "Voittajat";
        var winnerNicks = winners.stream().map(Player::getUserNickname).collect(toList());
        this.data.messages.add(String.format("%s: %s", label, String.join(",", winnerNicks)));
        for(var winner : winners) {
            var previousScore = this.data.scores.get(winner.getUserEmail());
            previousScore.score++;
            if(previousScore.score == 3) {
                winner.inactivate();
            }
        }
        this.startNextRound();
        if(this.getNumberOfPlayers(true) < 2) {
            this.data.scoresHistory.add(new HashMap<>(this.data.scores));
            this.data.scores.clear();
            this.players.forEach(player -> {
                player.activate();
                this.data.scores.put(player.getUserEmail(), new ScorePOJO(player.getUserNickname(), 0));
            });
            this.setDealer(this.dealer);
        }
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
        DEAL, PLAY_CARD, FOLD, CUT, ACE_OR_TWO_DECISION, SPEAK, SPEAK_SUIT, DISCARD
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
