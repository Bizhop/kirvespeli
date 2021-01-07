package fi.bizhop.jassu.models;

import fi.bizhop.jassu.db.UserDB;

import java.math.BigDecimal;

public class User {
    private String email;
    private String jwt;

    private BigDecimal money;

    public User(String email, String jwt) {
        this.email = email;
        this.jwt = jwt;
        this.money = BigDecimal.valueOf(100L);
    }

    public User(UserDB userDB) {
        this.email = userDB.email;
        this.money = userDB.money;
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
}
