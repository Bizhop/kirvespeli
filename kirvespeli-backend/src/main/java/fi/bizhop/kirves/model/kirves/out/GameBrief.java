package fi.bizhop.kirves.model.kirves.out;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.bizhop.kirves.db.KirvesGameDB;
import fi.bizhop.kirves.model.User;
import fi.bizhop.kirves.model.kirves.Game;
import lombok.*;

import java.time.LocalDateTime;

@Value
@Builder
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

    public int getMaxPlayers() {
        return Game.MAX_PLAYERS;
    }

    @JsonCreator
    public static GameBrief fromJson(
            @JsonProperty("id") Long id,
            @JsonProperty("admin") User admin,
            @JsonProperty("players") Integer players,
            @JsonProperty("lastHandId") Long lastHandId,
            @JsonProperty("canJoin") Boolean canJoin,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("updatedAt") LocalDateTime updatedAt) {
        return GameBrief.builder()
                .id(id)
                .admin(admin)
                .players(players)
                .lastHandId(lastHandId)
                .canJoin(canJoin)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static GameBrief fromDb(KirvesGameDB db, boolean canJoin) {
        return GameBrief.builder()
                .id(db.id)
                .admin(new User(db.admin))
                .players(db.players)
                .lastHandId(db.lastHandId)
                .canJoin(canJoin)
                .createdAt(db.createdAt)
                .updatedAt(db.updatedAt)
                .build();
    }
}
