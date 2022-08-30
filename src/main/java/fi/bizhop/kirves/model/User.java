package fi.bizhop.kirves.model;

import fi.bizhop.kirves.db.UserDB;
import fi.bizhop.kirves.model.kirves.pojo.UserPOJO;

public class User {
    private String email;
    private String jwt;
    private String nickname;

    public User() {}

    public User(String email, String jwt) {
        this.email = email;
        this.jwt = jwt;
    }

    public User(UserDB userDB) {
        this.email = userDB.email;
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
