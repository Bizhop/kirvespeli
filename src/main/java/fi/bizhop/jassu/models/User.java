package fi.bizhop.jassu.models;

public class User {
    private String email;
    private String jwt;

    public User(String email, String jwt) {
        this.email = email;
        this.jwt = jwt;
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
}
