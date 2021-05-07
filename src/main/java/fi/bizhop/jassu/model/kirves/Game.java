package fi.bizhop.jassu.model.kirves;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;
import fi.bizhop.jassu.model.Card;
import fi.bizhop.jassu.model.Cards;
import fi.bizhop.jassu.model.User;
import fi.bizhop.jassu.model.kirves.out.GameOut;
import fi.bizhop.jassu.model.kirves.out.PlayerOut;
import fi.bizhop.jassu.model.kirves.pojo.GameDataPOJO;
import fi.bizhop.jassu.model.kirves.pojo.PlayerPOJO;
import fi.bizhop.jassu.model.kirves.pojo.ScorePOJO;
import fi.bizhop.jassu.model.kirves.pojo.UserPOJO;
import fi.bizhop.jassu.util.RandomUtil;

import java.util.*;

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
    private Player turn;
    private Player dealer;
    private Player firstPlayerOfRound;
    private Card valttiCard = null;
    private Card.Suit valtti = null;
    private Card cutCard = null;
    
    private final GameDataPOJO data;

    public Game(GameDataPOJO pojo) throws CardException, KirvesGameException {
        if(pojo == null) throw new KirvesGameException("Game data can't be null");
        this.data = pojo;
        this.deck = Cards.fromAbbrs(pojo.deck);

        //map the players
        Player previous = null;
        Map<String, Player> playersMap = new LinkedHashMap<>();
        for(PlayerPOJO playerPOJO : pojo.players) {
            Player current = new Player(playerPOJO, previous);
            playersMap.put(playerPOJO.user.email, current);
            previous = current;
        }
        this.players.addAll(playersMap.values());
        Player last = this.players.get(this.players.size() - 1);
        this.players.get(0).setPrevious(last);
        last.setNext(this.players.get(0));

        this.turn = playersMap.get(pojo.turn);
        this.dealer = playersMap.get(pojo.dealer);
        this.firstPlayerOfRound = playersMap.get(pojo.firstPlayerOfRound);
        this.valttiCard = Card.fromAbbr(pojo.valttiCard);
        this.valtti = Card.Suit.fromAbbr(pojo.valtti);
        this.cutCard = Card.fromAbbr(pojo.cutCard);
    }

    public Game(User admin) throws CardException, KirvesGameException {
        this.data = new GameDataPOJO();
        this.deck = new Deck().shuffle();
        this.data.canJoin = true;

        Player player = addPlayerInternal(admin.toPOJO());
        setDealer(player);
    }

    public GameDataPOJO toPojo() {
        this.data.players = this.players.stream().map(Player::toPojo).collect(toList());
        this.data.deck = this.deck.getCardsOut();
        this.data.turn = this.turn == null ? null : this.turn.getUserEmail();
        this.data.dealer = this.dealer == null ? null : this.dealer.getUserEmail();
        this.data.firstPlayerOfRound = this.firstPlayerOfRound == null ? null : this.firstPlayerOfRound.getUserEmail();
        this.data.valttiCard = this.valttiCard == null ? null : this.valttiCard.toString();
        this.data.valtti = this.valtti == null ? null : this.valtti.getAbbr();
        this.data.cutCard = this.cutCard == null ? null : this.cutCard.toString();
        
        return this.data;
    }

    public GameOut out() throws KirvesGameException {
        return this.out(null);
    }

    public GameOut out(User user) throws KirvesGameException {
        List<String> myCards = new ArrayList<>();
        List<String> myActions = new ArrayList<>();
        String myExtraCard = null;
        if(user != null) {
            Optional<Player> me = getPlayer(user.getEmail());
            if(me.isPresent()) {
                Player player = me.get();
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

        return new GameOut(
                getPlayersStartingFrom(user == null ? null : user.getEmail()).stream().map(PlayerOut::new).collect(toList()),
                this.deck.size(),
                this.dealer.getUserEmail(),
                this.turn.getUserEmail(),
                myCards,
                myExtraCard,
                myActions,
                this.data.message,
                this.data.canJoin,
                this.valttiCard == null ? "" : this.valttiCard.toString(),
                this.valtti == null ? "" : this.valtti.toString(),
                this.data.canDeclineCut,
                this.cutCard == null ? "" : this.cutCard.toString(),
                this.players.size(),
                getFirstCardSuit(),
                getScoreOutput(this.data.scores),
                this.data.scoresHistory.stream().map(this::getScoreOutput).collect(toList())
        );
    }

    private Map<String, Integer> getScoreOutput(Map<String, ScorePOJO> input) {
        return input.entrySet().stream().collect(toMap(entry -> entry.getValue().nickname, entry -> entry.getValue().score));
    }

    private String getFirstCardSuit() {
        if(this.firstPlayerOfRound == null || this.valtti == null) return "";
        Card firstCard = this.firstPlayerOfRound.equals(this.turn) ? null : this.firstPlayerOfRound.getLastPlayedCard();
        if(firstCard == null) return "";
        if(firstCard.getSuit() == JOKER || firstCard.getRank() == JACK) {
            return this.valtti.name();
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
                Player player = addPlayerInternal(user.toPOJO());
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
        this.players.forEach(Player::resetAvailableActions);
    }

    private Player addPlayerInternal(UserPOJO user) {
        Player player;
        if(this.players.size() > 0) {
            Player last = this.players.get(this.players.size() - 1);
            player = new Player(user, this.dealer, last);
        } else {
            player = new Player(user);
        }
        this.players.add(player);
        this.data.scores.put(user.email, new ScorePOJO(user.getNickname(), 0));
        return player;
    }

    public void deal(User user) throws CardException, KirvesGameException {
        deal(user, null);
    }

    //use this method directly only when testing!
    public void deal(User user, List<Card> possibleValttiCards) throws CardException, KirvesGameException {
        if(!this.data.canDeal) throw new KirvesGameException("Jakaminen ei onnistu");
        List<Player> players = getPlayersStartingFrom(this.dealer.getUserEmail());
        for(Player player : players) {
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
            this.data.forcedGame = true;
        } else if (this.valttiCard.getRank() == TWO || this.valttiCard.getRank() == ACE) {
            this.dealer.hideCards(this.valttiCard.getRank() == TWO ? 2 : 3);
            this.dealer.setExtraCard(this.valttiCard);
            this.valttiCard = null;
        }
        this.data.canDeal = false;
        this.cutCard = null;
        this.data.speaking = true;
        Player player = getPlayer(user.getEmail()).orElseThrow(() -> new KirvesGameException(String.format("'%s' ei löytynyt pelaajista", user.getNickname())));
        Player nextPlayer = player.getNext(this.players.size());
        setCardPlayer(nextPlayer);
        this.firstPlayerOfRound = nextPlayer;
        players.forEach(Player::resetWonRounds);
    }

    //use this method directly only when testing!
    public void cut(User cutter, boolean decline, Card cutCard, Card second) throws CardException, KirvesGameException {
        if(decline && !this.data.canDeclineCut) {
            throw new KirvesGameException("Nostosta ei voi kieltäytyä");
        }
        this.deck.clear();
        this.deck.add(new Deck().shuffle());
        this.data.message = "";
        if(!decline) {
            this.cutCard = cutCard != null ? this.deck.removeCard(cutCard) : this.deck.remove(RandomUtil.getInt(this.deck.size()));
            if (this.cutCard.getRank() == JACK || this.cutCard.getSuit() == JOKER) {
                Card secondAfterCut = second != null ? this.deck.removeCard(second) : this.deck.remove(RandomUtil.getInt(this.deck.size()));
                this.data.message = String.format("Seuraava kortti on %s", secondAfterCut);
                if (secondAfterCut.getRank() == JACK || secondAfterCut.getSuit() == JOKER) {
                    this.data.message += String.format("\nUusi nosto, %s voi kieltäytyä nostamasta", cutter.getNickname());
                    this.data.canDeclineCut = true;
                    return;
                }
                Player cutterPlayer = getPlayer(cutter.getEmail()).orElseThrow(() -> new KirvesGameException("Nostajaa ei löydy pelaajista"));
                cutterPlayer.setExtraCard(this.cutCard);
                this.data.forcedGame = true;
            }
        } else {
            this.data.message = String.format("%s kieltäytyi nostosta", cutter.getNickname());
        }
        getPlayersStartingFrom(this.dealer.getUserEmail()).forEach(player -> {
            player.setDeclaredPlayer(false);
            player.resetAvailableActions();
            player.getPlayedCards().clear();
            player.setSpeak(null);
        });
        this.dealer.setAvailableActions(List.of(DEAL));
        this.turn = this.dealer;
        this.data.canDeal = true;
        this.data.canJoin = false;
    }

    public void cut(User cutter, boolean decline) throws CardException, KirvesGameException {
        cut(cutter, decline, null, null);
    }

    public void aceOrTwoDecision(User user, boolean keepExtraCard) throws KirvesGameException {
        Player player = getPlayer(user.getEmail()).orElseThrow(() -> new KirvesGameException("Pelaajaa ei löytynyt"));
        if(keepExtraCard) {
            this.data.speaking = false;
        } else {
            this.valttiCard = player.getExtraCard();
            player.setExtraCard(null);
        }
        player.moveInvisibleCardsToHand();
        setCardPlayer(this.dealer.getNext(this.players.size()));
    }

    public void discard(User user, int index) throws KirvesGameException, CardException {
        Player player = getPlayer(user.getEmail()).orElseThrow(() -> new KirvesGameException("Pelaajaa ei löytynyt"));
        player.discard(index);
        //anyone discarding is always declared player
        player.setDeclaredPlayer(true);
        setCardPlayer(this.dealer.getNext(this.players.size()));
    }

    public void speak(User user, Speak speak) throws KirvesGameException {
        if(speak == null) throw new KirvesGameException("Puhe ei voi olla tyhjä (null)");

        Player player = getPlayer(user.getEmail()).orElseThrow(() -> new KirvesGameException("Pelaajaa ei löytynyt"));
        if(speak == KEEP) {
            player.setDeclaredPlayer(true);
            player.setSpeak(KEEP);
            this.data.speaking = false;
            setCardPlayer(this.firstPlayerOfRound);
        } else {
            player.setSpeak(speak);
            Player next = player.getNext(this.players.size());
            if(this.firstPlayerOfRound.equals(next)) {
                Optional<Player> changer = getPlayersStartingFrom(this.firstPlayerOfRound.getUserEmail()).stream()
                        .filter(s -> s.getSpeak() == CHANGE)
                        .findFirst();
                if(changer.isPresent()) {
                    player.resetAvailableActions();
                    changer.get().setAvailableActions(List.of(SPEAK_SUIT));
                } else {
                    startNextRound();
                }
            } else {
                setCardPlayer(next);
            }
        }
    }

    public void speakSuit(User user, Card.Suit suit) throws KirvesGameException {
        if(suit == null) throw new KirvesGameException("Valtti ei voi olla tyhjä (null)");
        if(suit == this.valtti) throw new KirvesGameException(String.format("Pitää valita eri maa kuin %s", suit.toString()));

        Player player = getPlayer(user.getEmail()).orElseThrow(() -> new KirvesGameException("Pelaajaa ei löytynyt"));
        player.setDeclaredPlayer(true);
        this.valtti = suit;
        this.valttiCard = null;
        this.data.speaking = false;
        setCardPlayer(this.firstPlayerOfRound);
    }

    public void playCard(User user, int index) throws KirvesGameException, CardException {
        Player player = getPlayer(user.getEmail()).orElseThrow(() -> new KirvesGameException("Pelaajaa ei löytynyt"));
        player.playCard(index);
        setCardPlayer(player.getNext(this.players.size()));
        determinePossibleRoundWinner();
    }

    public void fold(User user) throws KirvesGameException {
        Player player = getPlayer(user.getEmail()).orElseThrow(() -> new KirvesGameException("Pelaajaa ei löytynyt"));
        player.fold();
        if(this.firstPlayerOfRound.equals(player)) {
            this.firstPlayerOfRound = player.getNext(this.players.size());
        }
        setCardPlayer(player.getNext(this.players.size()));
        determinePossibleRoundWinner();
    }

    private void determinePossibleRoundWinner() throws KirvesGameException {
        List<Player> players = getPlayersStartingFrom(this.firstPlayerOfRound.getUserEmail());
        if(players.size() == 0) throw new KirvesGameException("Virhe: 0 pelaajaa jäljellä");
        if(players.size() == 1) {
            Player winner = players.get(0);
            handleScoring(Set.of(winner.getUserEmail()));
        } else if(this.turn.equals(this.firstPlayerOfRound)) {
            List<Card> playedCards = players.stream()
                    .map(Player::getLastPlayedCard)
                    .collect(toList());

            Card winningCard = winningCard(playedCards, this.valtti);
            Player roundWinner = players.stream()
                    .filter(playerItem -> winningCard.equals(playerItem.getLastPlayedCard()))
                    .findFirst().orElseThrow(() -> new KirvesGameException("Voittokorttia ei löytynyt pelatuista korteista"));
            roundWinner.addRoundWon();

            if(roundWinner.cardsInHand() != 0) {
                setCardPlayer(roundWinner);
                this.firstPlayerOfRound = roundWinner;
            }
            else {
                Player handWinner = determineHandWinner(players);
                Set<String> winners = determineScoringWinners(players, handWinner);
                handleScoring(winners);
            }
        }
    }

    public static Set<String> determineScoringWinners(List<Player> players, Player handWinner) {
        Set<String> winners = new HashSet<>();
        for(Player player : players) {
            //case: player is handWinner
            if(player.equals(handWinner)) {
                winners.add(player.getUserEmail());
            } else {
                //case: other player didn't win as declared player
                if(!player.isDeclaredPlayer()) {
                    List<Player> otherPlayers = new ArrayList<>(players);
                    otherPlayers.remove(player);
                    for (Player other : otherPlayers) {
                        if (other.isDeclaredPlayer() && !other.equals(handWinner)) {
                            winners.add(player.getUserEmail());
                        }
                    }
                }
            }
        }
        return winners;
    }

    private void handleScoring(Set<String> winners) throws KirvesGameException {
        this.data.message = String.format("Voittajat: %s", String.join(",", winners));
        for(String winner : winners) {
            Player player = getPlayer(winner).orElseThrow(() -> new KirvesGameException("Pelaajaa ei löytynyt"));
            ScorePOJO previousScore = this.data.scores.get(player.getUserEmail());
            previousScore.score++;
            if(previousScore.score == 3) {
                player.inactivate();
            }
        }
        startNextRound();
        if(getNumberOfPlayers(true) < 2) {
            this.data.scoresHistory.add(this.data.scores);
            this.data.scores = new HashMap<>();
            this.players.forEach(player -> {
                player.activate();
                this.data.scores.put(player.getUserEmail(), new ScorePOJO(player.getUserNickname(), 0));
            });
            setDealer(this.dealer);
        }
    }

    public void startNextRound() throws KirvesGameException {
        this.players.stream()
                .filter(Player::isFolded)
                .forEach(Player::activate);
        setDealer(this.dealer.getNext(this.players.size()));
    }

    private List<Player> getPlayersStartingFrom(String userEmail) throws KirvesGameException {
        if(userEmail == null) {
            return this.players;
        }
        Player item = getPlayer(userEmail).orElseThrow(() -> new KirvesGameException("Pelaajaa ei löytynyt"));
        return getPlayersStartingFrom(item, userEmail, this.players.size());
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
        Optional<Player> needsToDiscard = getPlayersStartingFrom(player.getUserEmail()).stream()
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
                player = player.getNext(this.players.size());
            }
            this.turn = player;
            if(this.data.speaking && !this.data.forcedGame) {
                this.turn.setAvailableActions(List.of(SPEAK));
            }
            else {
                this.turn.setAvailableActions(canFold(this.turn, this.valtti, this.getNumberOfPlayers(true)) ? List.of(PLAY_CARD, FOLD) : List.of(PLAY_CARD));
            }
        }
    }

    public static boolean canFold(Player player, Card.Suit valtti, int numberOfPlayers) {
        if(numberOfPlayers < 2 || valtti == null || player.isDeclaredPlayer()) return false;
        if(player.cardsInHand() == 5) return true;
        return player.getHand().hasValtti(valtti);
    }

    private void setDealer(Player dealer) throws KirvesGameException {
        getPlayersStartingFrom(dealer.getUserEmail()).forEach(Player::clearHand);
        this.dealer = dealer;
        this.data.canDeal = false;
        this.valttiCard = null;
        this.valtti = null;
        this.data.speaking = false;
        this.data.forcedGame = false;
        this.data.canDeclineCut = false;
        this.resetActions();
        this.turn = dealer.getPrevious(this.players.size());
        this.turn.setAvailableActions(List.of(CUT));
    }

    public static Player determineHandWinner(List<Player> players) throws KirvesGameException {
        //three or more rounds is clear winner
        Optional<Player> threeOrMore = players.stream()
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
        return this.players.stream().anyMatch(player -> user.getEmail().equals(player.getUserEmail()));
    }

    public String getMessage() {
        return this.data.message;
    }

    public Card getCutCard() {
        return this.cutCard;
    }

    public Card.Suit getValtti() {
        return this.valtti;
    }

    public Card getExtraCard(User user) {
        if(user == null) return null;
        return getPlayer(user.getEmail())
                .map(Player::getExtraCard)
                .orElse(null);
    }

    public int getNumberOfPlayers() {
        return getNumberOfPlayers(false);
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

    public enum Action {
        DEAL, PLAY_CARD, FOLD, CUT, ACE_OR_TWO_DECISION, SPEAK, SPEAK_SUIT, DISCARD
    }

    public enum Speak {
        CHANGE, KEEP, PASS
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Game)) return false;
        Game other = (Game)o;
        if(this.players.size() != other.players.size()) return false;
        if(this.firstPlayerOfRound == null && other.firstPlayerOfRound != null) return false;
        for(int i=0; i < this.players.size(); i++) {
            if(!(this.players.get(0).equals(other.players.get(0)))) return false;
        }
        return this.data.equals(other.data)
                && this.deck.equals(other.deck)
                && this.turn.equals(other.turn)
                && this.dealer.equals(other.dealer)
                && (this.firstPlayerOfRound == null || this.firstPlayerOfRound.equals(other.firstPlayerOfRound))
                && this.valttiCard == other.valttiCard
                && this.valtti == other.valtti
                && this.cutCard == other.cutCard;
    }
}
