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
    }

    public void addPlayer(User player) {
        players.add(new KirvesPlayer(player));
    }

    public List<String> getPlayers() {
        return players.stream()
                .map(KirvesPlayer::getUserEmail)
                .collect(Collectors.toList());
    }

    public String getAdmin() {
        return admin == null
                ? ""
                : admin.getEmail();
    }

    public void inactivate() {
        this.active = false;
    }

    public boolean isActive() {
        return this.active;
    }
}
