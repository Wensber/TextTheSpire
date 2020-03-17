package textTheSpire;

import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import communicationmod.CommandExecutor;
import org.eclipse.swt.widgets.Display;

public class Deck extends AbstractWindow{

    public Deck(Display display){
        isVisible = true;
        window = new Window(display,"Deck",300,300);
    }

    public String getText(){

        if(window.shell.isDisposed()){
            Display.getDefault().dispose();
            Gdx.app.exit();
        }

        StringBuilder s = new StringBuilder();
        s.append("\r\n");

        //Not in dungeon
        if(CardCrawlGame.dungeon == null || !CommandExecutor.isInDungeon() || !CardCrawlGame.isInARun()){
            return "";
        }

        //Show remaining deck in combat
        if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT){

            CardGroup h = AbstractDungeon.player.drawPile;

            s.append("Size: ").append(h.size()).append("\r\n");

            if(h.size() > 0) {
                for (AbstractCard c : h.group) {
                    s.append(c.name).append("\r\n");
                }
            }

            return s.toString();

        }else{
            //Show master deck out of combat
            CardGroup h = AbstractDungeon.player.masterDeck;

            s.append("Size: ").append(h.size()).append("\r\n");

            if(h.size() > 0) {
                for (AbstractCard c : h.group) {
                    s.append(c.name).append("\r\n");
                }
            }

            return s.toString();
        }
    }

}
