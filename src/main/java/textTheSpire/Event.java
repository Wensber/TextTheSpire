package textTheSpire;

import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.events.GenericEventDialog;
import com.megacrit.cardcrawl.events.RoomEventDialog;
import com.megacrit.cardcrawl.mod.replay.monsters.replay.FadingForestBoss;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.DeathScreen;
import com.megacrit.cardcrawl.screens.GameOverScreen;
import com.megacrit.cardcrawl.screens.GameOverStat;
import com.megacrit.cardcrawl.screens.VictoryScreen;
import com.megacrit.cardcrawl.screens.leaderboards.LeaderboardEntry;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import com.megacrit.cardcrawl.screens.mainMenu.PatchNotesScreen;
import com.megacrit.cardcrawl.ui.DialogWord;
import com.megacrit.cardcrawl.unlock.AbstractUnlock;
import communicationmod.ChoiceScreenUtils;
import communicationmod.CommandExecutor;
import org.eclipse.swt.widgets.Display;

import javax.smartcardio.Card;
import java.awt.*;
import java.util.ArrayList;

public class Event extends AbstractWindow{


    public Event(Display display){
        isVisible = true;
        window = new Window(display,"Event",400,500);
    }

    @SuppressWarnings("unchecked")
    public String getText(){

        if(window.shell.isDisposed()){
            Display.getDefault().dispose();
            Gdx.app.exit();
        }

        StringBuilder s = new StringBuilder();
        s.append("\r\n");

        if(AbstractDungeon.screen == AbstractDungeon.CurrentScreen.DEATH){
            s.append("Death\r\nScore ").append(DeathScreen.calcScore(false)).append("\r\n");
            for(GameOverStat g : ((ArrayList<GameOverStat>)ReflectionHacks.getPrivate(AbstractDungeon.deathScreen, GameOverScreen.class, "stats"))){
                if(g.label != null){
                    s.append(g.label).append(": ").append(g.value);
                    if(g.description != null){
                        s.append(", ").append(g.description);
                    }
                    s.append("\r\n");
                }
            }

            ArrayList<AbstractUnlock> unlockBundle = ((ArrayList<AbstractUnlock>)ReflectionHacks.getPrivate(AbstractDungeon.deathScreen, GameOverScreen.class, "unlockBundle"));

            if(unlockBundle != null){
                for(AbstractUnlock u : unlockBundle){
                    switch (u.type){
                        case CARD:
                            s.append("Unlock Card ").append(TextTheSpire.inspectCard(u.card)).append("\r\n");
                            break;
                        case RELIC:
                            s.append("Unlock Relic ").append(TextTheSpire.inspectRelic(u.relic)).append("\r\n");
                            break;
                    }
                }
            }

            return s.toString();
        }
        if(AbstractDungeon.screen == AbstractDungeon.CurrentScreen.VICTORY){
            s.append("Victory\r\nScore").append(VictoryScreen.calcScore(true)).append("\r\n");

            for(GameOverStat g : ((ArrayList<GameOverStat>)ReflectionHacks.getPrivate(AbstractDungeon.victoryScreen, GameOverScreen.class, "stats"))){
                if(g.label != null){
                    s.append(g.label).append(": ").append(g.value);
                    if(g.description != null){
                        s.append(", ").append(g.description);
                    }
                    s.append("\r\n");
                }
            }

            ArrayList<AbstractUnlock> unlockBundle = ((ArrayList<AbstractUnlock>)ReflectionHacks.getPrivate(AbstractDungeon.victoryScreen, GameOverScreen.class, "unlockBundle"));

            if(unlockBundle != null){
                for(AbstractUnlock u : unlockBundle){
                    switch (u.type){
                        case CARD:
                            s.append("Unlock Card ").append(TextTheSpire.inspectCard(u.card)).append("\r\n");
                            break;
                        case RELIC:
                            s.append("Unlock Relic ").append(TextTheSpire.inspectRelic(u.relic)).append("\r\n");
                            break;
                    }
                }
            }

            return s.toString();
        }
        if(AbstractDungeon.screen == AbstractDungeon.CurrentScreen.UNLOCK){
            if(AbstractDungeon.unlockScreen.unlock.type == AbstractUnlock.UnlockType.CHARACTER){
                s.append("Unlock Character\r\n").append(AbstractDungeon.unlockScreen.unlock.player.getClass().getSimpleName()).append("\r\n");
                return s.toString();
            }
        }

        if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.DAILY && !CommandExecutor.isInDungeon()){
            s.append(TextTheSpire.inspectDaily());
            return s.toString();
        }

        if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.LEADERBOARD && !CommandExecutor.isInDungeon()){
            s.append("Leaderboard\r\n");
            for(LeaderboardEntry e : CardCrawlGame.mainMenuScreen.leaderboardsScreen.entries){
                s.append(e.rank).append(". ").append(e.name).append(" : ");
                for(int i=0;i<CardCrawlGame.mainMenuScreen.leaderboardsScreen.typeButtons.size();i++){
                    if(CardCrawlGame.mainMenuScreen.leaderboardsScreen.typeButtons.get(i).active){
                        if(CardCrawlGame.mainMenuScreen.leaderboardsScreen.typeButtons.get(i).label.equals("Fastest Victory")){
                            s.append((e.score / 3600)).append("hr ").append((e.score / 60)).append("min ").append((e.score % 60)).append("sec\r\n");
                        }else{
                            s.append(e.score).append("\r\n");
                        }
                        break;
                    }
                }
            }
            return s.toString();
        }

        if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.PATCH_NOTES && !CommandExecutor.isInDungeon()){
            String p = (String)basemod.ReflectionHacks.getPrivateStatic(PatchNotesScreen.class, "text");
            s.append(p);
            return s.toString();
        }

        boolean fable = false;
        //Not in Event
        if(!CommandExecutor.isInDungeon() || ChoiceScreenUtils.getCurrentChoiceType() != ChoiceScreenUtils.ChoiceType.EVENT){
            if(TextTheSpire.replayTheSpire && CommandExecutor.isInDungeon() && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT && AbstractDungeon.getCurrRoom().monsters.monsters.get(AbstractDungeon.getCurrRoom().monsters.monsters.size()-1) instanceof FadingForestBoss){
                boolean show = (boolean)basemod.ReflectionHacks.getPrivateStatic(GenericEventDialog.class, "show");
                if(!show){
                    return "";
                }
                fable = true;
            }else {
                return "";
            }
        }

        if(!fable) {
            s.append(AbstractDungeon.getCurrRoom().event.getClass().getSimpleName()).append("\r\n");
        }else{
            String name = (String) ReflectionHacks.getPrivate(((FadingForestBoss) AbstractDungeon.getCurrRoom().monsters.monsters.get(AbstractDungeon.getCurrRoom().monsters.monsters.size()-1)).imageEventText, GenericEventDialog.class, "title");
            s.append(name).append("\r\n");
        }

        StringBuilder body = new StringBuilder();
        ArrayList<DialogWord> words;

        if (AbstractDungeon.getCurrRoom().event instanceof AbstractImageEvent || fable) {
            words = (ArrayList<DialogWord>) basemod.ReflectionHacks.getPrivateStatic(GenericEventDialog.class, "words");
        } else {
            words = (ArrayList<DialogWord>) basemod.ReflectionHacks.getPrivate(AbstractDungeon.getCurrRoom().event.roomEventText, RoomEventDialog.class, "words");
        }


        for(DialogWord w : words){
            body.append(w.word);
            char punctuation = w.word.charAt(w.word.length()-1);
            if(punctuation == '.' || punctuation == '?' || punctuation == '!'){
                body.append("\r\n");
            }else{
                body.append(" ");
            }
        }

        s.append(body.toString());

        return s.toString();
    }

}
