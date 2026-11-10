package fi.bizhop.kirves.model.kirves.out;

import fi.bizhop.kirves.model.kirves.pojo.ScorePOJO;

public record ScoreOut(String email, String nickname, int score) {
    public ScoreOut(ScorePOJO pojo) {
        this(pojo.email, pojo.nickname, pojo.score);
    }
}
