package fi.bizhop.jassu.db;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="games")
public class KirvesGameDB extends TimestampBase {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name="admin_user_id")
    public UserDB admin;

    public Integer players;
    public Boolean active;
    public Boolean canJoin;
    public String gameData;
    public Long lastHandId;
}
