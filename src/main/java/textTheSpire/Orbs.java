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
import communicationmod.CommandExecutor;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;

public class Orbs extends AbstractWindow{

    public Orbs(Display display){
        isVisible = true;
        window = new Window(display,"Orbs",300,300);
    }

    public String getText(){

        if(window.shell.isDisposed()){
            Display.getDefault().dispose();
            Gdx.app.exit();
        }

        StringBuilder s = new StringBuilder();
        s.append("\r\n");

        //If not in dungeon
        if(CardCrawlGame.dungeon == null || !CommandExecutor.isInDungeon() || !CardCrawlGame.isInARun()){
            return "";
        }

        //If in combat
        if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT){

            AbstractPlayer p = AbstractDungeon.player;

            ArrayList<AbstractOrb> ol = p.orbs;

            s.append("Front\r\n");

            int count = 0;

            for (AbstractOrb o : ol) {
                if (o instanceof Dark) {
                    s.append(count).append(". Dark ").append(o.evokeAmount).append("\r\n");
                } else{
                    s.append(count).append(". ").append(o.name).append("\r\n");
                }
                count++;
            }

            s.append("Back\r\n");
            return s.toString();

        }else{
            //If not in combat
            return "";
        }
    }

}
