package fi.bizhop.jassu.model.kirves.pojo;

import fi.bizhop.jassu.model.kirves.Game;

import java.util.ArrayList;
import java.util.List;

public class PlayerPOJO {
    //these values are only updated when generating save data
    public List<String> hand;
    public List<String> invisibleCards;
    public String extraCard;
    public List<String> playedCards;
    public String next;
    public String previous;

    //these values are updated running the game and evaluated for PlayerPOJO equality
    public UserPOJO user;
    public final List<Integer> roundsWon = new ArrayList<>();
    public final List<Game.Action> availableActions = new ArrayList<>();
    public boolean declaredPlayer = false;
    public boolean inGame = true;
    public boolean folded = false;
    public Game.Speak speak = null;

    public PlayerPOJO() {}

    public PlayerPOJO(UserPOJO user) {
        this.user = user;
    }
    
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof PlayerPOJO)) return false;
        var other = (PlayerPOJO)o;
        
        if(this.roundsWon.size() != other.roundsWon.size()) return false;
        for(int i=0; i < this.roundsWon.size(); i++) {
            if(!this.roundsWon.get(i).equals(other.roundsWon.get(i))) return false;
        }
        if(this.availableActions.size() != other.availableActions.size()) return false;
        for(int i=0; i < this.availableActions.size(); i++) {
            if(this.availableActions.get(i) != other.availableActions.get(i)) return false;
        }

        return this.user.equals(other.user)
                && this.declaredPlayer == other.declaredPlayer
                && this.inGame == other.inGame
                && this.folded == other.folded
                && this.speak == other.speak;
    }
}