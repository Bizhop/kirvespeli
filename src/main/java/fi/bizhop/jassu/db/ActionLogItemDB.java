package fi.bizhop.jassu.db;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import static javax.persistence.CascadeType.ALL;

@Entity
@Table(name = "action_log_items")
public class ActionLogItemDB {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    public UserDB user;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "action_log_id")
    public ActionLogDB actionLog;

    @NotNull
    public String input;
}
