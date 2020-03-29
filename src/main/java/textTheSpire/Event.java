package textTheSpire;

import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.ui.DialogWord;
import communicationmod.ChoiceScreenUtils;
import communicationmod.CommandExecutor;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;

public class Event extends AbstractWindow{


    public Event(Display display){
        isVisible = true;
        window = new Window(display,"Event",400,500);
    }

    public String getText(){

        if(window.shell.isDisposed()){
            Display.getDefault().dispose();
            Gdx.app.exit();
        }

        StringBuilder s = new StringBuilder();
        s.append("\r\n");

        //Not in dungeon
        if(CardCrawlGame.dungeon == null || ChoiceScreenUtils.getCurrentChoiceType() != ChoiceScreenUtils.ChoiceType.EVENT){
            return "";
        }

        s.append(AbstractDungeon.getCurrRoom().event.getClass().getSimpleName()).append("\r\n");



        //return s.toString();
        return "";
    }

}
