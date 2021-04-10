package fi.bizhop.jassu.model;

import fi.bizhop.jassu.db.KirvesGameDB;

import java.time.LocalDateTime;

public class KirvesGameBrief {
    public Long id;
    public User admin;
    public Integer players;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public KirvesGameBrief() {}

    public KirvesGameBrief(KirvesGameDB db) {
        this.id = db.id;
        this.admin = new User(db.admin);
        this.players = db.players;
        this.createdAt = db.createdAt;
        this.updatedAt = db.updatedAt;
    }
}
