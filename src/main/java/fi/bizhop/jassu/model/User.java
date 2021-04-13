package fi.bizhop.jassu.model;

import fi.bizhop.jassu.db.UserDB;

import java.io.Serializable;
import java.math.BigDecimal;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof User) {
            return this.email.equals(((User) other).email);
        } else {
            return false;
        }
    }

    public String getNickname() {
        if(this.nickname == null || this.nickname.isEmpty()) {
            return this.email;
        }
        else {
            return this.nickname;
        }
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
