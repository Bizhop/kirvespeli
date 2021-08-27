package fi.bizhop.jassu.model.kirves.out;

import fi.bizhop.jassu.db.KirvesGameDB;
import fi.bizhop.jassu.model.User;

import java.time.LocalDateTime;

public class GameBrief {
    public Long id;
    public User admin;
    public Integer players;
    public Long lastHandId;
    public Boolean canJoin;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public GameBrief() {}

    public GameBrief(KirvesGameDB db) {
        this.id = db.id;
        this.admin = new User(db.admin);
        this.players = db.players;
        this.lastHandId = db.lastHandId;
        this.canJoin = db.canJoin;
        this.createdAt = db.createdAt;
        this.updatedAt = db.updatedAt;
    }
}
