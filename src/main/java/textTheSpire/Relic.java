package textTheSpire;

import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import communicationmod.CommandExecutor;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;

public class Relic extends AbstractWindow{


    public Relic(Display display){
        isVisible = true;
        window = new Window(display,"Relic",300,300);
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

        //Display all relics when in dungeon
        ArrayList<AbstractRelic> relics = AbstractDungeon.player.relics;

        for(int i=relics.size()-1; i>=0; i--){
            AbstractRelic r = relics.get(i);
            if(r.counter >= 0){
                s.append(i).append(":").append(r.name).append(" ").append(r.counter).append("\r\n");
            }else{
                s.append(i).append(":").append(r.name).append("\r\n");
            }
        }

        return s.toString();

    }

}
