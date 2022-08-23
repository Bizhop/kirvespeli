package fi.bizhop.jassu.model.kirves.pojo;

public class UserPOJO {
    public String email;
    public String nickname;

    public UserPOJO() {}

    public UserPOJO(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
    }

    public String getNickname() {
        return this.nickname == null ? this.email : this.nickname;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof UserPOJO)) return false;
        var other = (UserPOJO)o;

        if(this.email != null && other.email == null) return false;
        if(this.nickname != null && other.nickname == null) return false;

        return (this.email == null || this.email.equals(other.email))
                && (this.nickname == null || this.nickname.equals(other.nickname));
    }
}
