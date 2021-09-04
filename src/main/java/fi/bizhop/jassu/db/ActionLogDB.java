package fi.bizhop.jassu.db;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name="action_logs")
public class ActionLogDB {
    //key is composite of gameId and handId
    @Id
    public String key;

    @NotNull
    @ManyToOne
    @JoinColumn(name="owner_user_id")
    public UserDB owner;

    public String initialState;

    @OneToMany(mappedBy = "actionLog")
    public List<ActionLogItemDB> items;
}
