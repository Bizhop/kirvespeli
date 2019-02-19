package fi.bizhop.jassu.db;

import fi.bizhop.jassu.models.User;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="users")
public class UserDB {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    public Long id;
    public String email;
    public BigDecimal money;

    public UserDB() {}

    public UserDB(User user) {
        this.email = user.getEmail();
        this.money = user.getMoney();
    }
}
