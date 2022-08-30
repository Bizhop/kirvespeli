package fi.bizhop.kirves.db;

import fi.bizhop.kirves.model.User;

import javax.persistence.*;

@Entity
@Table(name="users")
public class UserDB extends TimestampBase {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    public Long id;
    public String email;
    public String nickname;

    public UserDB() {}

    public UserDB(User user) {
        this.email = user.getEmail();
        this.nickname = user.getNickname();
    }
}
