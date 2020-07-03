package textTheSpire;

import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.GremlinMatchGame;
import com.megacrit.cardcrawl.events.shrines.GremlinWheelGame;
import com.megacrit.cardcrawl.helpers.TipTracker;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.custom.CustomMod;
import com.megacrit.cardcrawl.screens.custom.CustomModeCharacterButton;
import com.megacrit.cardcrawl.screens.custom.CustomModeScreen;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import com.megacrit.cardcrawl.screens.stats.StatsScreen;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import com.megacrit.cardcrawl.ui.buttons.LargeDialogOptionButton;
import communicationmod.ChoiceScreenUtils;
import communicationmod.CommandExecutor;
import communicationmod.patches.GremlinMatchGamePatch;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;

public class Custom extends AbstractWindow{

    public Custom(Display display){
        isVisible = true;
        window = new Window(display,"Custom", 300, 300);
    }

    @SuppressWarnings("unchecked")
    public String getText(){

        if(window.shell.isDisposed()){
            Display.getDefault().dispose();
            Gdx.app.exit();
        }

        if(CardCrawlGame.mainMenuScreen == null || CardCrawlGame.mainMenuScreen.screen != MainMenuScreen.CurScreen.CUSTOM || CardCrawlGame.mainMenuScreen.customModeScreen == null || CommandExecutor.isInDungeon()){
            return "";
        }

        StringBuilder s = new StringBuilder();
        s.append("\r\nCustom Mode\r\n");

        s.append("Character Selected ");

        StringBuilder options = new StringBuilder("\r\nOptions\r\n");

        for(int i=0; i < CardCrawlGame.mainMenuScreen.customModeScreen.options.size(); i++){
            CustomModeCharacterButton c = CardCrawlGame.mainMenuScreen.customModeScreen.options.get(i);
            if(c.selected){
                s.append(c.c.getClass().getSimpleName());
            }
            options.append(i).append(" ").append(c.c.getClass().getSimpleName()).append("\r\n");
        }

        s.append(options);

        s.append("Ascension ");
        if(CardCrawlGame.mainMenuScreen.customModeScreen.isAscensionMode){
            s.append(CardCrawlGame.mainMenuScreen.customModeScreen.ascensionLevel).append("\r\n");
        }else{
            s.append("Off\r\n");
        }

        s.append("Seed");

        String seed = (String) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.customModeScreen, CustomModeScreen.class, "currentSeed");

        if(seed == null || seed.isEmpty()) {
            s.append(" none\r\n");
        }else{
            s.append("\r\n").append(seed).append("\r\n");
        }

        s.append("Mods\r\n");

        ArrayList<CustomMod> modList = (ArrayList<CustomMod>) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.customModeScreen, CustomModeScreen.class, "modList");

        for(int i=0; i<modList.size();i++){
            s.append(i).append(" ").append(modList.get(i).name).append(" ");
            if(modList.get(i).selected){
                s.append("on\r\n");
            }else{
                s.append("off\r\n");
            }
        }

        return s.toString();
    }

    @SuppressWarnings("unchecked")
    public String getSimpleText(){

        if(CardCrawlGame.mainMenuScreen == null || CardCrawlGame.mainMenuScreen.screen != MainMenuScreen.CurScreen.CUSTOM || CardCrawlGame.mainMenuScreen.customModeScreen == null || CommandExecutor.isInDungeon()){
            return "";
        }

        StringBuilder s = new StringBuilder();
        s.append("\r\n");

        for(int i=0; i < CardCrawlGame.mainMenuScreen.customModeScreen.options.size(); i++){
            CustomModeCharacterButton c = CardCrawlGame.mainMenuScreen.customModeScreen.options.get(i);
            if(c.selected){
                s.append(c.c.getClass().getSimpleName()).append("\r\n");
                break;
            }
        }

        s.append("Ascension ");
        if(CardCrawlGame.mainMenuScreen.customModeScreen.isAscensionMode){
            s.append(CardCrawlGame.mainMenuScreen.customModeScreen.ascensionLevel).append("\r\n");
        }else{
            s.append("Off\r\n");
        }

        String seed = (String) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.customModeScreen, CustomModeScreen.class, "currentSeed");

        if(seed == null || seed.isEmpty()) {
            s.append("Seed none\r\n");
        }else{
            s.append("Seed\r\n").append(seed).append("\r\n");
        }

        s.append("Mods\r\n");

        ArrayList<CustomMod> modList = (ArrayList<CustomMod>) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.customModeScreen, CustomModeScreen.class, "modList");

        for(CustomMod c : modList){
            if(c.selected){
                s.append(c.name).append("\r\n");
            }
        }

        return s.toString();
    }

}













