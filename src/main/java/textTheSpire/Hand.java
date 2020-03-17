package textTheSpire;

import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import communicationmod.CommandExecutor;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;

public class Hand extends AbstractWindow{


    public Hand(Display display){
        isVisible = true;
        window = new Window(display,"Hand",300,300);
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

            CardGroup h = AbstractDungeon.player.hand;

            int count = 1;
            for(AbstractCard c : h.group){
                int cost = handCost(c);
                if(cost == -2) {
                    s.append(count).append(":").append(c.name).append("\r\n");
                } else if(cost == -1) {
                    s.append(count).append(":").append(c.name).append(" X").append("\r\n");
                } else {
                    s.append(count).append(":").append(c.name).append(" ").append(cost).append("\r\n");
                }
                count++;
            }

            ArrayList<AbstractPotion> pl = AbstractDungeon.player.potions;
            count = 0;
            if (pl.size() > 0) {
                s.append("Potions:\r\n");
                for (AbstractPotion po : pl) {
                    s.append(count).append(":").append(po.name).append("\r\n");
                    count++;
                }
            }

            return s.toString();

        }else{
            //If not in combat
            return "";
        }
    }

    /*
    Params:
        c - Any card in your hand
    Returns:
        Current cost of c
     */
    public static int handCost(AbstractCard c){
        if (c.freeToPlay()) {
            return 0;
        } else{
            return c.costForTurn;
        }
    }


}
