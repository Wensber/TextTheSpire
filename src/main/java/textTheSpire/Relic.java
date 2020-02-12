package textTheSpire;

import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;

public class Relic {

    public Window relic;

    public Relic(Display display){
        relic = new Window(display,"Relic",300,300);
    }

    public void update(){

        if(relic.shell.isDisposed()){
            Display.getDefault().dispose();
            Gdx.app.exit();
        }

        StringBuilder s = new StringBuilder();

        //Not in dungeon
        if(CardCrawlGame.dungeon == null || !CardCrawlGame.isInARun()){
            relic.setText(s.toString());
            return;
        }

        //Display all relics when in dungeon
        ArrayList<AbstractRelic> relics = AbstractDungeon.player.relics;

        for(AbstractRelic r : relics){

            if(r.counter != -1){
                s.append(r.name).append(":").append(r.counter).append(", ");
            }else{
                s.append(r.name).append(", ");
            }
        }
        s = new StringBuilder(s.substring(0, s.length() - 2));

        relic.setText(s.toString());

    }

}
