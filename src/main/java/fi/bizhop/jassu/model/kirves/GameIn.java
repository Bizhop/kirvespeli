package fi.bizhop.jassu.model.kirves;

import fi.bizhop.jassu.model.Card;

import java.util.Set;

public class GameIn {
    public Game.Action action;
    public int index;
    public boolean keepExtraCard;
    public Card.Suit valtti;
    public String declarePlayerEmail;
    public boolean declineCut;
    public boolean resetActivePlayers;
    public Set<String> inactivateByEmail;
}
