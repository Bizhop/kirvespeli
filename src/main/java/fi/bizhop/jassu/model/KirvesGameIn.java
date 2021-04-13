package fi.bizhop.jassu.model;

import java.util.Set;

public class KirvesGameIn {
    public KirvesGame.Action action;
    public int index;
    public boolean keepExtraCard;
    public Card.Suit valtti;
    public String declarePlayerEmail;
    public boolean declineCut;
    public boolean resetActivePlayers;
    public Set<String> inactivateByEmail;
}
