package textTheSpire;

import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import org.eclipse.swt.widgets.Display;

public class Deck {

    public Window deck;

    public Deck(Display display){
        deck = new Window(display,"Deck",300,300);
    }

    public void update(){

        if(deck.shell.isDisposed()){
            Display.getDefault().dispose();
            Gdx.app.exit();
        }

        StringBuilder s = new StringBuilder();

        //Not in dungeon
        if(CardCrawlGame.dungeon == null || !CardCrawlGame.isInARun()){
            deck.setText(s.toString());
            return;
        }

        //Show remaining deck in combat
        if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT){

            CardGroup h = AbstractDungeon.player.drawPile;

            s.append("Size: ").append(h.size()).append("\r\n");

            if(h.size() > 0) {
                for (AbstractCard c : h.group) {
                    s.append(c.name).append(", ");
                }
                s = new StringBuilder(s.substring(0, s.length() - 2));
            }

            deck.setText(s.toString());

        }else{
            //Show master deck out of combat
            CardGroup h = AbstractDungeon.player.masterDeck;

            s.append("Size: ").append(h.size()).append("\r\n");

            if(h.size() > 0) {
                for (AbstractCard c : h.group) {
                    s.append(c.name).append(", ");
                }
                s = new StringBuilder(s.substring(0, s.length() - 2));
            }

            deck.setText(s.toString());
        }
    }

}
