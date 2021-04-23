package fi.bizhop.jassu.model.kirves;

import fi.bizhop.jassu.exception.CardException;
import fi.bizhop.jassu.exception.KirvesGameException;
import fi.bizhop.jassu.model.Card;
import fi.bizhop.jassu.model.Cards;
import fi.bizhop.jassu.model.User;
import fi.bizhop.jassu.util.RandomUtil;

import java.util.*;

import static fi.bizhop.jassu.model.Card.Rank.*;
import static fi.bizhop.jassu.model.Card.Suit.*;
import static fi.bizhop.jassu.model.kirves.Game.Action.*;
import static java.util.stream.Collectors.toList;

public class Game {
    private static final int NUM_OF_CARD_TO_DEAL = 5;

    private final Cards deck;
    private final List<Player> players = new ArrayList<>();
    private boolean canJoin;
    private Player turn;
    private Player dealer;
    private boolean canDeal;
    private String message;
    private Player firstPlayerOfRound;
    private Card valttiCard = null;
    private Card.Suit valtti = null;
    private Card cutCard = null;
    private boolean canSetValtti;
    private boolean forcedGame;
    private boolean canDeclineCut;

    public Game(GameDataPOJO pojo) throws CardException {
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

        this.canJoin = pojo.canJoin;
        this.turn = playersMap.get(pojo.turn);
        this.dealer = playersMap.get(pojo.dealer);
        this.canDeal = pojo.canDeal;
        this.message = pojo.message;
        this.firstPlayerOfRound = playersMap.get(pojo.firstPlayerOfRound);
        this.valttiCard = Card.fromAbbr(pojo.valttiCard);
        this.valtti = Card.Suit.fromAbbr(pojo.valtti);
        this.cutCard = Card.fromAbbr(pojo.cutCard);
        this.canSetValtti = pojo.canSetValtti;
        this.forcedGame = pojo.forcedGame;
        this.canDeclineCut = pojo.canDeclineCut;
    }

    public Game(User admin) throws CardException {
        this.deck = new Deck().shuffle();
        this.canJoin = true;

        Player player = addPlayerInternal(admin);
        setDealer(player);
    }

    public GameDataPOJO toPojo() {
        return new GameDataPOJO(
                this.players.stream().map(Player::toPojo).collect(toList()),
                this.deck.getCardsOut(),
                this.canJoin,
                this.turn == null ? null : this.turn.getUserEmail(),
                this.dealer == null ? null : this.dealer.getUserEmail(),
                this.canDeal,
                this.message,
                this.firstPlayerOfRound == null ? null : this.firstPlayerOfRound.getUserEmail(),
                this.valttiCard == null ? null : this.valttiCard.toString(),
                this.valtti == null ? null : this.valtti.getAbbr(),
                this.cutCard == null ? null : this.cutCard.toString(),
                this.canSetValtti,
                this.forcedGame,
                this.canDeclineCut
        );
    }

    public GameOut out() throws KirvesGameException {
        return this.out(null);
    }

    public GameOut out(User user) throws KirvesGameException {
        List<String> myCards = new ArrayList<>();
        List<String> myActions = new ArrayList<>();
        String myExtraCard = null;
        if(user != null) {
            Optional<Player> me = getPlayer(user);
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
                getPlayersStartingFrom(user).stream().map(PlayerOut::new).collect(toList()),
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
                this.players.size(),
                getFirstCardSuit()
        );
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

    public Optional<Player> getPlayer(User user) {
        return this.players.stream().filter(player -> player.getUser().equals(user)).findFirst();
    }

    public Optional<Player> getRoundWinner(int round) {
        return this.players.stream().filter(player -> player.getRoundsWon().contains(round)).findFirst();
    }

    public void addPlayer(User user) throws KirvesGameException {
        if(this.canJoin) {
            if (this.players.stream()
                    .noneMatch(player -> user.getEmail().equals(player.getUserEmail()))) {
                Player player = addPlayerInternal(user);
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

    private Player addPlayerInternal(User user) {
        if(this.players.size() > 0) {
            Player last = this.players.get(this.players.size() - 1);
            Player player = new Player(user, this.dealer, last);
            this.players.add(player);
            return player;
        } else {
            Player player = new Player(user);
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
        List<Player> players = getPlayersStartingFrom(this.dealer.getUser());
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
            this.forcedGame = true;
        } else if (this.valttiCard.getRank() == TWO || this.valttiCard.getRank() == ACE) {
            this.dealer.hideCards(this.valttiCard.getRank() == TWO ? 2 : 3);
            this.dealer.setExtraCard(this.valttiCard);
            this.valttiCard = null;
        }
        this.canDeal = false;
        this.cutCard = null;
        this.canSetValtti = true;
        Player player = getPlayer(user).orElseThrow(() -> new KirvesGameException(String.format("'%s' ei löytynyt pelaajista", user.getNickname())));
        Player nextPlayer = player.getNext();
        setCardPlayer(nextPlayer);
        this.firstPlayerOfRound = nextPlayer;
        players.forEach(Player::resetWonRounds);
    }

    //use this method directly only when testing!
    public void cut(User cutter, boolean decline, Card cutCard, Card second) throws CardException, KirvesGameException {
        if(decline && !this.canDeclineCut) {
            throw new KirvesGameException("Nostosta ei voi kieltäytyä");
        }
        this.deck.clear();
        this.deck.add(new Deck().shuffle());
        this.message = "";
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
                Player cutterPlayer = getPlayer(cutter).orElseThrow(() -> new KirvesGameException("Nostajaa ei löydy pelaajista"));
                cutterPlayer.setExtraCard(this.cutCard);
                this.forcedGame = true;
            }
        } else {
            this.message = String.format("%s kieltäytyi nostosta", cutter.getNickname());
        }
        getPlayersStartingFrom(this.dealer.getUser()).forEach(player -> {
            player.setDeclaredPlayer(false);
            player.resetAvailableActions();
            player.getPlayedCards().clear();
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
        Optional<Player> me = getPlayer(user);
        if(me.isPresent()) {
            Player player = me.get();
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
        Optional<Player> me = getPlayer(user);
        if(me.isPresent()) {
            Player player = me.get();
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
        Optional<Player> me = getPlayer(user);
        if(me.isPresent()) {
            Optional<Player> declarePlayer = getPlayer(declareUser);
            if(declarePlayer.isPresent()) {
                declarePlayer.get().setDeclaredPlayer(true);
            } else {
                throw new KirvesGameException(String.format("'%s' ei ole tässä pelissä", user.getNickname()));
            }
            Player player = me.get();
            if(suit != this.valtti) {
                this.valttiCard = null;
                this.valtti = suit;
            }
            this.canSetValtti = false;
            setCardPlayer(player);
        }
    }

    public void playCard(User user, int index) throws KirvesGameException, CardException {
        Optional<Player> me = getPlayer(user);
        if(me.isPresent()) {
            Player player = me.get();
            player.playCard(index);
            setCardPlayer(player.getNext());
            if(this.turn.equals(this.firstPlayerOfRound)) {
                List<Player> players = getPlayersStartingFrom(this.firstPlayerOfRound.getUser());
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
                    Player handWinner = determineHandWinner();
                    this.message = String.format("Voittaja on %s", handWinner.getUserNickname());
                    setDealer(this.dealer.getNext());
                }
            }
        } else {
            throw new KirvesGameException("Käyttäjä ei ole tässä pelissä");
        }
    }

    public void startNextRound(User user) throws KirvesGameException {
        getPlayersStartingFrom(user).forEach(Player::clearHand);
        setDealer(this.dealer.getNext());
    }

    public void adjustPlayersInGame(User user, boolean resetActivePlayers, Set<String> inactivateByEmail) throws KirvesGameException {
        Optional<Player> playerOpt = getPlayer(user);
        if (playerOpt.isPresent()) {
            if (resetActivePlayers) {
                this.players.forEach(Player::activate);
            } else {
                this.players.stream()
                        .filter(item -> inactivateByEmail.contains(item.getUserEmail()))
                        .forEach(Player::inactivate);
                //if active players would be less than 2, reset all players to active
                if (this.players.stream().filter(Player::isInGame).count() < 2) {
                    this.players.forEach(Player::activate);
                }
            }
            if(!this.dealer.isInGame()) {
                this.dealer = this.dealer.getNext();
            }
            resetActions();
            Player cutter = this.dealer.getPrevious();
            cutter.setAvailableActions(List.of(CUT, ADJUST_PLAYERS_IN_GAME));
        } else {
            throw new KirvesGameException(String.format("'%s' ei ole tässä pelissä", user.getNickname()));
        }
    }

    private List<Player> getPlayersStartingFrom(User user) throws KirvesGameException {
        if(user == null) {
            return this.players;
        }
        Optional<Player> start = getPlayer(user);
        if(start.isPresent()) {
            List<Player> players = new ArrayList<>();
            Player item = start.get();
            if(!item.isInGame()) throw new KirvesGameException(String.format("'%s' ei ole tässä pelissä", item.getUserNickname()));
            do {
                players.add(item);
            } while((item = item.getNext()) != start.get());

            return players;
        } else {
            throw new KirvesGameException(String.format("'%s' ei ole tässä pelissä", user.getNickname()));
        }
    }

    private void setCardPlayer(Player player) throws KirvesGameException {
        this.resetActions();
        Optional<Player> needsToDiscard = getPlayersStartingFrom(player.getUser()).stream()
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

    private void setDealer(Player dealer) {
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

    public Player determineHandWinner() throws KirvesGameException {
        List<Player> players = getPlayersStartingFrom(this.dealer.getUser());
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
        return this.players.stream().anyMatch(player -> user.equals(player.getUser()));
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
                .map(Player::getExtraCard)
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

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Game)) return false;
        Game other = (Game)o;
        if(this.players.size() != other.players.size()) return false;
        if(this.firstPlayerOfRound == null && other.firstPlayerOfRound != null) return false;
        if(this.message == null && other.message != null) return false;
        for(int i=0; i < this.players.size(); i++) {
            if(!(this.players.get(0).equals(other.players.get(0)))) return false;
        }
        return this.deck.equals(other.deck)
                && this.canJoin == other.canJoin
                && this.turn.equals(other.turn)
                && this.dealer.equals(other.dealer)
                && this.canDeal == other.canDeal
                && (this.message == null || this.message.equals(other.message))
                && (this.firstPlayerOfRound == null || this.firstPlayerOfRound.equals(other.firstPlayerOfRound))
                && this.valttiCard == other.valttiCard
                && this.valtti == other.valtti
                && this.cutCard == other.cutCard
                && this.canSetValtti == other.canSetValtti
                && this.forcedGame == other.forcedGame
                && this.canDeclineCut == other.canDeclineCut;
    }
}
