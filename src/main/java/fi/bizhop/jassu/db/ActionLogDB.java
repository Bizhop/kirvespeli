package fi.bizhop.jassu.db;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name="action_logs")
public class ActionLogDB {
    //key is composite of gameId and handId
    @Id
    public String key;

    public String initialState;

    @OneToMany(mappedBy = "actionLog")
    public List<ActionLogItemDB> items;
}
