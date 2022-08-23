package fi.bizhop.jassu.model.kirves.out;

import com.fasterxml.jackson.annotation.JsonFormat;
import fi.bizhop.jassu.db.KirvesGameDB;
import fi.bizhop.jassu.model.User;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Value
@Builder
@Jacksonized
public class GameBrief {
    Long id;
    User admin;
    Integer players;
    Long lastHandId;
    Boolean canJoin;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt;

    public static GameBrief fromDb(KirvesGameDB db) {
        return GameBrief.builder()
                .id(db.id)
                .admin(new User(db.admin))
                .players(db.players)
                .lastHandId(db.lastHandId)
                .canJoin(db.canJoin)
                .createdAt(db.createdAt)
                .updatedAt(db.updatedAt)
                .build();
    }
}
