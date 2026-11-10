package fi.bizhop.kirves.model.kirves.out;

import fi.bizhop.kirves.model.kirves.pojo.ScoreHistoryPOJO;

import java.util.List;

public record ScoreHistoryOut(List<ScoreOut> scores, String loser, boolean axe) {
    public ScoreHistoryOut(ScoreHistoryPOJO pojo) {
        this(pojo.scores.stream().map(ScoreOut::new).toList(), pojo.loser, pojo.axe);
    }
}
