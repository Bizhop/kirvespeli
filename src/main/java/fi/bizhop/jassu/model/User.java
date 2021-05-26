package fi.bizhop.jassu.model;

import fi.bizhop.jassu.db.UserDB;
import fi.bizhop.jassu.model.kirves.pojo.UserPOJO;

import java.math.BigDecimal;

public class User {
    private String email;
    private String jwt;
    private String nickname;

    private BigDecimal money;

    public User() {}

    public User(String email, String jwt) {
        this.email = email;
        this.jwt = jwt;
        this.money = BigDecimal.valueOf(100L);
    }

    public User(UserDB userDB) {
        this.email = userDB.email;
        this.money = userDB.money;
        this.nickname = userDB.nickname;
    }

    public User(UserPOJO pojo) {
        this.email = pojo.email;
        this.nickname = pojo.nickname;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getJwt() {
        return this.jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public BigDecimal getMoney() {
        return this.money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof User)) return false;
        return this.email.equals(((User) other).email);
    }

    public String getNickname() {
        if(this.nickname == null || this.nickname.isEmpty()) {
            return this.email;
        }
        else {
            return this.nickname;
        }
    }

    public UserPOJO toPOJO() {
        return new UserPOJO(this.email, this.nickname);
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
