package fi.bizhop.jassu.models;

import fi.bizhop.jassu.exception.CardException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KirvesGame {
    private User admin;
    private Cards deck;
    private List<KirvesPlayer> players = new ArrayList<>();
    private boolean active;

    public KirvesGame(User admin) throws CardException {
        this.admin = admin;
        this.deck = new KirvesDeck();
        this.active = true;
        this.players.add(new KirvesPlayer(admin));
    }

    public KirvesGameOut out() {
        return new KirvesGameOut(
                this.getAdmin(),
                this.players.stream().map(KirvesPlayerOut::new).collect(Collectors.toList()),
                this.deck.size()
        );
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
}
