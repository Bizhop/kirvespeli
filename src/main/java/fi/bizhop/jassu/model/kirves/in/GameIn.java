package fi.bizhop.jassu.model.kirves.in;

import fi.bizhop.jassu.model.Card;
import fi.bizhop.jassu.model.kirves.Game;

public class GameIn {
    public Game.Action action;
    public int index;
    public boolean keepExtraCard;
    public Card.Suit valtti;
    public boolean declineCut;
    public Game.Speak speak;
}
