package textTheSpire;

import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;

public class Hand {

    public Window hand;


    public Hand(Display display){
        hand = new Window(display,"Hand",300,300);
    }

    public void update(){

        if(hand.shell.isDisposed()){
            Display.getDefault().dispose();
            Gdx.app.exit();
        }

        StringBuilder s = new StringBuilder();

        //If not in dungeon
        if(CardCrawlGame.dungeon == null || !CardCrawlGame.isInARun()){
            hand.setText(s.toString());
            hand.setVisible(false);
            return;
        }

        //If in combat
        if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT){

            CardGroup h = AbstractDungeon.player.hand;

            int count = 1;
            for(AbstractCard c : h.group){
                s.append(count).append(":").append(c.name).append(" ").append(handCost(c)).append("\r\n");
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

            hand.setText(s.toString());
            hand.setVisible(true);

        }else{
            //If not in combat
            hand.setText(s.toString());
            hand.setVisible(false);
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
