package fi.bizhop.kirves.model.kirves.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Final scores of one finished game. The loser is the only player left below 3 points.
 * axe is true when this was the loser's second loss in the match, which ends the match.
 */
public class ScoreHistoryPOJO {
    public List<ScorePOJO> scores = new ArrayList<>();
    public String loser;
    public boolean axe = false;

    public ScoreHistoryPOJO() {}

    public ScoreHistoryPOJO(List<ScorePOJO> scores, String loser, boolean axe) {
        this.scores = scores;
        this.loser = loser;
        this.axe = axe;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof ScoreHistoryPOJO)) return false;
        var other = (ScoreHistoryPOJO) o;

        return Objects.equals(this.scores, other.scores)
                && Objects.equals(this.loser, other.loser)
                && this.axe == other.axe;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.scores, this.loser, this.axe);
    }
}
