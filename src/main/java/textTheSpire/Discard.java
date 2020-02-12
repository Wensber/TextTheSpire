package textTheSpire;

import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import org.eclipse.swt.widgets.Display;

public class Discard {

    public Window discard;

    public Discard(Display display){
        discard = new Window(display,"Discard",300,300);
    }

    public void update(){

        if(discard.shell.isDisposed()) {
            Display.getDefault().dispose();
            Gdx.app.exit();
        }

        StringBuilder s = new StringBuilder();

        //Not in dungeon
        if(CardCrawlGame.dungeon == null || !CardCrawlGame.isInARun()){
            discard.setText(s.toString());
            discard.setVisible(false);
            return;
        }

        //In combat
        if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT){

            CardGroup h = AbstractDungeon.player.discardPile;


            s.append("Size: ").append(h.size()).append("\r\n");

            if(h.size() > 0) {
                for (AbstractCard c : h.group) {
                    s.append(c.name).append(", ");
                }
                s = new StringBuilder(s.substring(0, s.length() - 2));
            }

            discard.setText(s.toString());
            discard.setVisible(true);

        }else{
            //Not in combat
            discard.setText(s.toString());
            discard.setVisible(false);
        }
    }

}
