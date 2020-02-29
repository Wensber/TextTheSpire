package textTheSpire;

import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.orbs.*;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;

public class Orbs extends AbstractWindow{

    public Orbs(Display display){
        isVisible = false;
        window = new Window(display,"Orbs",300,300);
    }

    public String getText(){

        if(window.shell.isDisposed()){
            Display.getDefault().dispose();
            Gdx.app.exit();
        }

        StringBuilder s = new StringBuilder();

        //If not in dungeon
        if(CardCrawlGame.dungeon == null || !CardCrawlGame.isInARun()){
            return "";
        }

        //If in combat
        if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT){

            AbstractPlayer p = AbstractDungeon.player;

            ArrayList<AbstractOrb> ol = p.orbs;

            s.append("Front\r\n");

            if (p.chosenClass == AbstractPlayer.PlayerClass.DEFECT) {
                for (AbstractOrb o : ol) {
                    if (o instanceof Dark) {
                        s.append("Dark ").append(o.evokeAmount).append("\r\n");
                    } else if (o instanceof Lightning) {
                        s.append("Lightning\r\n");
                    } else if (o instanceof Frost) {
                        s.append("Frost\r\n");
                    } else if (o instanceof Plasma) {
                        s.append("Plasma\r\n");
                    } else {
                        s.append("Empty\r\n");
                    }
                }
            }else if(ol.size() > 0 && !(ol.get(0) instanceof  EmptyOrbSlot)){
                for (AbstractOrb o : ol) {
                    if (o instanceof Dark) {
                        s.append("Dark ").append(o.evokeAmount).append("\r\n");
                    } else if (o instanceof Lightning) {
                        s.append("Lightning\r\n");
                    } else if (o instanceof Frost) {
                        s.append("Frost\r\n");
                    } else if (o instanceof Plasma) {
                        s.append("Plasma\r\n");
                    } else {
                        s.append("Empty\r\n");
                    }
                }
            } else {
                return "";
            }

            s.append("Back\r\n");
            return s.toString();

        }else{
            //If not in combat
            return "";
        }
    }

}
