package fi.bizhop.kirves.model.kirves.pojo;

import java.util.Objects;

public class ScorePOJO {
    public String email = "";
    public String nickname = "";
    public int score = 0;

    public ScorePOJO() {}

    public ScorePOJO(String email, String nickname, int score) {
        this.email = email;
        this.nickname = nickname;
        this.score = score;
    }

    public ScorePOJO copy() {
        return new ScorePOJO(this.email, this.nickname, this.score);
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof ScorePOJO)) return false;
        var other = (ScorePOJO) o;

        return Objects.equals(this.email, other.email)
                && Objects.equals(this.nickname, other.nickname)
                && this.score == other.score;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.email, this.nickname, this.score);
    }
}
