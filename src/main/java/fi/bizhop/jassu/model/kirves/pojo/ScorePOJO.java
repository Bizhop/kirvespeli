package fi.bizhop.jassu.model.kirves.pojo;

public class ScorePOJO {
    public String nickname = "";
    public int score = 0;

    public ScorePOJO() {}

    public ScorePOJO(String nickname, int score) {
        this.nickname = nickname;
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof ScorePOJO)) return false;
        var other = (ScorePOJO) o;

        return this.nickname.equals(other.nickname)
                && this.score == other.score;
    }
}
