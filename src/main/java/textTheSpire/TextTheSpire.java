package textTheSpire;

import basemod.ReflectionHacks;
import basemod.interfaces.*;
import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.ExhaustiveField;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.RefundFields;
import com.evacipated.cardcrawl.mod.stslib.relics.ClickableRelic;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;

import basemod.BaseMod;
import com.megacrit.cardcrawl.blights.AbstractBlight;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.characters.CharacterManager;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.daily.DailyScreen;
import com.megacrit.cardcrawl.daily.TimeHelper;
import com.megacrit.cardcrawl.daily.mods.AbstractDailyMod;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.GenericEventDialog;
import com.megacrit.cardcrawl.events.shrines.GremlinMatchGame;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.integrations.DistributorFactory;
import com.megacrit.cardcrawl.mod.replay.monsters.replay.FadingForestBoss;
import com.megacrit.cardcrawl.mod.replay.relics.WaxSeal;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rewards.chests.AbstractChest;
import com.megacrit.cardcrawl.rooms.*;
import com.megacrit.cardcrawl.screens.charSelect.CharacterOption;
import com.megacrit.cardcrawl.screens.charSelect.CharacterSelectScreen;
import com.megacrit.cardcrawl.screens.custom.CustomMod;
import com.megacrit.cardcrawl.screens.custom.CustomModeScreen;
import com.megacrit.cardcrawl.screens.leaderboards.FilterButton;
import com.megacrit.cardcrawl.screens.leaderboards.LeaderboardEntry;
import com.megacrit.cardcrawl.screens.leaderboards.LeaderboardScreen;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;

import com.megacrit.cardcrawl.screens.mainMenu.MenuButton;
import com.megacrit.cardcrawl.screens.mainMenu.MenuCancelButton;
import com.megacrit.cardcrawl.screens.mainMenu.SaveSlotScreen;
import com.megacrit.cardcrawl.screens.options.OptionsPanel;
import com.megacrit.cardcrawl.screens.options.Slider;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryScreen;
import com.megacrit.cardcrawl.screens.select.BossRelicSelectScreen;
import com.megacrit.cardcrawl.screens.stats.AchievementItem;
import com.megacrit.cardcrawl.screens.stats.CharStat;
import com.megacrit.cardcrawl.screens.stats.RunData;
import com.megacrit.cardcrawl.screens.stats.StatsScreen;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import com.megacrit.cardcrawl.ui.buttons.CancelButton;
import com.megacrit.cardcrawl.ui.buttons.ConfirmButton;
import com.megacrit.cardcrawl.ui.buttons.GridSelectConfirmButton;
import com.megacrit.cardcrawl.ui.buttons.LargeDialogOptionButton;
import com.megacrit.cardcrawl.ui.panels.DeleteSaveConfirmPopup;
import com.megacrit.cardcrawl.ui.panels.SeedPanel;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import communicationmod.ChoiceScreenUtils;
import communicationmod.CommandExecutor;
import communicationmod.GameStateListener;
import communicationmod.InvalidCommandException;
import communicationmod.patches.GremlinMatchGamePatch;
import communicationmod.patches.ShopScreenPatch;
import org.eclipse.swt.widgets.Display;
import replayTheSpire.patches.ReplayShopInitCardsPatch;

import javax.smartcardio.Card;
import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;


@SpireInitializer
public class TextTheSpire implements PostUpdateSubscriber, PreUpdateSubscriber, PostPowerApplySubscriber, OnCardUseSubscriber, PrePotionUseSubscriber, PostDrawSubscriber, PostExhaustSubscriber {

    //Used to only update display every number of update cycles
    int iter;
    int choiceTimeout;

    private boolean setSettings = false;

    public static boolean replayTheSpire;
    public static boolean stslib;
    public static boolean ascensionReborn;
    public static int maxAsc;
    public static boolean beaked;

    private Hand hand;
    private Map map;
    private Discard discard;
    private Deck deck;
    private Player player;
    private Monster monster;
    private Choices choice;
    private Relic relic;
    private Orbs orbs;
    private Event event;
    private Custom custom;

    private Logs logs;
    private Inspect inspect;

    private HashMap<String, String> savedOutput;

    private JTextField promptFrame;

    private static String queuedCommand = "";
    private static boolean hasQueuedCommand = false;

    public static ArrayList<RunData> runList;
    public static ArrayList<RunData> runFiltered;
    public static boolean include_win;
    public static boolean include_lose;
    public static boolean include_iron;
    public static boolean include_silent;
    public static boolean include_defect;
    public static boolean include_watch;
    public static boolean include_normal;
    public static boolean include_asc;
    public static boolean include_daily;

    public TextTheSpire() {

        replayTheSpire = Loader.isModLoaded("ReplayTheSpireMod");
        stslib = Loader.isModLoaded("stslib");
        ascensionReborn = Loader.isModLoaded("ascensionmod");
        if(ascensionReborn)
            maxAsc = 25;
        else
            maxAsc = 20;
        beaked = Loader.isModLoaded("beakedthecultist-sts");

        Thread ui = new Thread(() -> {
            Display display = new Display();

            hand = new Hand(display);
            map = new Map(display);
            choice = new Choices(display);
            monster = new Monster(display);
            deck = new Deck(display);
            discard = new Discard(display);
            relic = new Relic(display);
            player = new Player(display);
            orbs = new Orbs(display);
            event = new Event(display);
            custom = new Custom(display);

            logs = new Logs(display);
            inspect = new Inspect(display);

            while(!display.isDisposed()){
                if(!display.readAndDispatch()){
                    display.sleep();
                }
            }
        });

        ui.start();

        iter = 0;
        choiceTimeout = 0;

        runFiltered = new ArrayList<RunData>();
        include_win = true;
        include_lose = true;
        include_iron = true;
        include_silent = true;
        include_defect = true;
        include_watch = true;
        include_normal = true;
        include_asc = true;
        include_daily = true;

        savedOutput = new HashMap<>();

        BaseMod.subscribe(this);

        JFrame prompt = new JFrame("Prompt");
        prompt.setResizable(true);
        prompt.setSize(300, 100);
        prompt.setLocation(600, 800);
        promptFrame = new JTextField("");
        promptFrame.setEditable(true);
        prompt.setSize(300, 100);
        prompt.add(promptFrame);
        prompt.setVisible(true);

        //Sends commands to parser
        promptFrame.addActionListener(event -> {
            queueInput(promptFrame.getText());
            promptFrame.setText("");
            promptFrame.repaint();
        });

    }

    public static void initialize() {
        new TextTheSpire();
    }

    //Correct timing to execute commands
    @Override
    public void receivePreUpdate() {

        if (hasQueuedCommand && inspect != null && choiceTimeout == 0) {
            parsePrompt(queuedCommand);
            hasQueuedCommand = false;
            choiceTimeout = 70;
        }else if(choiceTimeout > 0){
            choiceTimeout--;
        }

    }

    //Queues input for correct timing
    public static void queueInput(String input){
        queuedCommand = input;
        hasQueuedCommand = true;
    }

    //Parse a command to see if its an allowed command and send to CommunicationMod to execute
    @SuppressWarnings("unchecked")
    public void parsePrompt(String input) {

        input = input.toLowerCase();
        logs.output(input);
        String[] tokens = input.split("\\s+");

        if(tokens.length == 0){
            return;
        }

        if(choice.screen != Choices.HistoryScreen.NONE){
            parseHistoryCommand(tokens);
            return;
        }else if(input.equals("history") && CardCrawlGame.mode == CardCrawlGame.GameMode.CHAR_SELECT && CardCrawlGame.mainMenuScreen != null){
            CardCrawlGame.mainMenuScreen.runHistoryScreen.refreshData();
            runList = (ArrayList<RunData>)basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.runHistoryScreen, RunHistoryScreen.class, "unfilteredRuns");
            choice.screen = Choices.HistoryScreen.MAIN;
            return;
        }

        switch(input){
            case "deck":
                inspect.setText(deck.getText());
                return;
            case "discard":
                inspect.setText(discard.getText());
                return;
            case "choices":
                inspect.setText(choice.getText());
                return;
            case "hand":
                inspect.setText(hand.getText());
                return;
            case "map":
                inspect.setText(map.getText());
                return;
            case "monster":
                inspect.setText(monster.getText());
                return;
            case "orbs":
                inspect.setText(orbs.getText());
                return;
            case "player":
                inspect.setText(player.getText());
                return;
            case "relic":
                inspect.setText(relic.getText());
                return;
            case "event":
                inspect.setText(event.getText());
                return;
            case "log":
                inspect.setText(logs.getText());
                return;
            case "tutorial":
                inspect.setText(getTutorial());
                return;
            case "mod":
                inspect.setText(modNotes());
                return;
            case "ascension":
                inspect.setText(ascensionNotes());
                return;
        }

        if(tokens[0].equals("show") && tokens.length >= 2){

            switch(tokens[1]){
                case "deck":
                    deck.isVisible = true;
                    return;
                case "discard":
                    discard.isVisible = true;
                    return;
                case "choices":
                    choice.isVisible = true;
                    return;
                case "hand":
                    hand.isVisible = true;
                    return;
                case "map":
                    map.isVisible = true;
                    return;
                case "monster":
                    monster.isVisible = true;
                    return;
                case "orbs":
                    orbs.isVisible = true;
                    return;
                case "player":
                    player.isVisible = true;
                    return;
                case "relic":
                    relic.isVisible = true;
                    return;
                case "event":
                    event.isVisible = true;
                    return;
                case "custom":
                    custom.isVisible = true;
                    return;
                case "log":
                    logs.logs.setVisible(true);
                    return;
                case "all":
                    deck.isVisible = true;
                    discard.isVisible = true;
                    choice.isVisible = true;
                    hand.isVisible = true;
                    map.isVisible = true;
                    monster.isVisible = true;
                    orbs.isVisible = true;
                    player.isVisible = true;
                    relic.isVisible = true;
                    event.isVisible = true;
                    custom.isVisible = true;
                    logs.logs.setVisible(true);
                    return;
            }

        }

        if(tokens[0].equals("hide") && tokens.length >= 2){

            switch(tokens[1]){
                case "deck":
                    deck.isVisible = false;
                    return;
                case "discard":
                    discard.isVisible = false;
                    return;
                case "choices":
                    choice.isVisible = false;
                    return;
                case "hand":
                    hand.isVisible = false;
                    return;
                case "map":
                    map.isVisible = false;
                    return;
                case "monster":
                    monster.isVisible = false;
                    return;
                case "orbs":
                    orbs.isVisible = false;
                    return;
                case "player":
                    player.isVisible = false;
                    return;
                case "relic":
                    relic.isVisible = false;
                    return;
                case "event":
                    event.isVisible = false;
                    return;
                case "custom":
                    custom.isVisible = false;
                    return;
                case "log":
                    logs.logs.setVisible(false);
                    return;
                case "all":
                    deck.isVisible = false;
                    discard.isVisible = false;
                    choice.isVisible = false;
                    hand.isVisible = false;
                    map.isVisible = false;
                    monster.isVisible = false;
                    orbs.isVisible = false;
                    player.isVisible = false;
                    relic.isVisible = false;
                    event.isVisible = false;
                    custom.isVisible = false;
                    logs.logs.setVisible(false);
                    return;
            }

        }

        if(tokens[0].equals("volume")){
            if(tokens.length == 1) {

                String s = "\r\nVolume\r\n";
                s += "Master " + Settings.MASTER_VOLUME + "\r\n";
                s += "Music " + Settings.MUSIC_VOLUME + "\r\n";
                s += "Sound " + Settings.SOUND_VOLUME + "\r\n";
                s += "Ambience " + Settings.AMBIANCE_ON;

                inspect.setText(s);

                return;
            }else if(tokens.length >= 3){
                try{
                    float volume = Float.parseFloat(tokens[2]);

                    if(volume < 0 || volume > 1){
                        return;
                    }

                    switch (tokens[1]){
                        case "master":
                            Settings.MASTER_VOLUME = volume;
                            Settings.soundPref.putFloat("Master Volume", volume);
                            CardCrawlGame.music.updateVolume();
                            if (CardCrawlGame.mode == CardCrawlGame.GameMode.CHAR_SELECT) {
                                CardCrawlGame.mainMenuScreen.updateAmbienceVolume(); break;
                            }  if (AbstractDungeon.scene != null) {
                            AbstractDungeon.scene.updateAmbienceVolume();
                        }
                            break;
                        case "music":
                            Settings.MUSIC_VOLUME = volume;
                            Settings.soundPref.putFloat("Music Volume", volume);
                            CardCrawlGame.music.updateVolume();
                            break;
                        case "sound":
                            Settings.SOUND_VOLUME = volume;
                            Settings.soundPref.putFloat("Sound Volume", volume);
                            if (CardCrawlGame.mode == CardCrawlGame.GameMode.CHAR_SELECT) {
                                CardCrawlGame.mainMenuScreen.updateAmbienceVolume();
                            } else if (AbstractDungeon.scene != null) {
                                AbstractDungeon.scene.updateAmbienceVolume();
                            }
                            break;
                    }

                }catch(Exception ignored){
                }
            }else if(tokens.length == 2 && tokens[1].equals("ambience")){
                Settings.soundPref.putBoolean("Ambience On", !Settings.AMBIANCE_ON);
                Settings.soundPref.flush();
                Settings.AMBIANCE_ON = !Settings.AMBIANCE_ON;
                if (CardCrawlGame.mode == CardCrawlGame.GameMode.CHAR_SELECT) {
                    CardCrawlGame.mainMenuScreen.updateAmbienceVolume();
                } else {
                    AbstractDungeon.scene.updateAmbienceVolume();
                }
            }
        }

        if(tokens[0].equals("lang")){
            language(tokens);
            return;
        }

        //Continue command. Only usable when not in dungeon and save file exists
        if (CardCrawlGame.mode == CardCrawlGame.GameMode.CHAR_SELECT && !CommandExecutor.isInDungeon() && CardCrawlGame.characterManager.anySaveFileExists() && input.equals("continue")) {

            CardCrawlGame.mainMenuScreen.screen = MainMenuScreen.CurScreen.NONE;
            CardCrawlGame.mainMenuScreen.hideMenuButtons();
            CardCrawlGame.mainMenuScreen.darken();

            CardCrawlGame.loadingSave = true;
            CardCrawlGame.chosenCharacter = (CardCrawlGame.characterManager.loadChosenCharacter()).chosenClass;
            CardCrawlGame.mainMenuScreen.isFadingOut = true;
            CardCrawlGame.mainMenuScreen.fadeOutMusic();
            Settings.isDailyRun = false;
            Settings.isTrial = false;
            ModHelper.setModsFalse();

            return;

        }

        if(input.equals("quit")){
            if(CommandExecutor.isInDungeon()) {
                CardCrawlGame.music.fadeAll();

                AbstractDungeon.getCurrRoom().clearEvent();
                AbstractDungeon.closeCurrentScreen();
                CardCrawlGame.startOver();

                if (RestRoom.lastFireSoundId != 0L) {
                    CardCrawlGame.sound.fadeOut("REST_FIRE_WET", RestRoom.lastFireSoundId);
                }
                if (AbstractDungeon.player.stance != null && !AbstractDungeon.player.stance.ID.equals("Neutral")) {
                    AbstractDungeon.player.stance.stopIdleSfx();
                }
            } else if (CardCrawlGame.mode == CardCrawlGame.GameMode.CHAR_SELECT){
                CardCrawlGame.mainMenuScreen.buttons.get(0).hb.clicked = true;
            }
            return;
        }

        if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.MAIN_MENU && !CommandExecutor.isInDungeon() && CardCrawlGame.mainMenuScreen.buttons.get(CardCrawlGame.mainMenuScreen.buttons.size()-2).result == MenuButton.ClickResult.ABANDON_RUN && input.equals("abandon")){
            CardCrawlGame.mainMenuScreen.buttons.get(CardCrawlGame.mainMenuScreen.buttons.size()-2).hb.clicked = true;
            return;
        }

        if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.ABANDON_CONFIRM){
            if(input.equals("yes")){
                CardCrawlGame.mainMenuScreen.abandonPopup.yesHb.clicked = true;
                return;
            }else if(input.equals("no")){
                CardCrawlGame.mainMenuScreen.abandonPopup.noHb.clicked = true;
                return;
            }
        }

        if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.MAIN_MENU && !CommandExecutor.isInDungeon() && CardCrawlGame.mainMenuScreen.buttons.get(CardCrawlGame.mainMenuScreen.buttons.size()-1).result == MenuButton.ClickResult.PLAY && input.equals("play")){
            CardCrawlGame.mainMenuScreen.buttons.get(CardCrawlGame.mainMenuScreen.buttons.size()-1).hb.clicked = true;
            return;
        }

        if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.PANEL_MENU){
            if(input.equals("standard")){
                CardCrawlGame.mainMenuScreen.panelScreen.panels.get(0).hb.clicked = true;
                return;
            } else if (input.equals("daily") && CardCrawlGame.mainMenuScreen.statsScreen.statScreenUnlocked()){
                CardCrawlGame.mainMenuScreen.panelScreen.panels.get(1).hb.clicked = true;
                return;
            } else if (input.equals("custom") && StatsScreen.all.highestDaily > 0){
                CardCrawlGame.mainMenuScreen.panelScreen.panels.get(2).hb.clicked = true;
                return;
            } else if (input.equals("back")){
                CardCrawlGame.mainMenuScreen.panelScreen.button.hb.clicked = true;
                return;
            }
        }

        if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.CHAR_SELECT){
            for(CharacterOption co : CardCrawlGame.mainMenuScreen.charSelectScreen.options){
                if(input.equals(co.c.getClass().getSimpleName().toLowerCase())){
                    co.hb.clicked = true;
                    return;
                }
            }
            if(input.equals("back")){
                CardCrawlGame.mainMenuScreen.charSelectScreen.cancelButton.hb.clicked = true;
                return;
            }
            if(input.equals("seed")){
                SeedPanel sp = (SeedPanel) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CharacterSelectScreen.class, "seedPanel");
                if(sp.shown){
                    Hitbox hb = (Hitbox) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CharacterSelectScreen.class, "seedHb");
                    hb.clicked = true;
                    inspect.setText("\r\nGo to main game window, paste the seed, then hit enter.\r\n");
                    return;
                }
            }
            boolean ready = (boolean) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CharacterSelectScreen.class, "anySelected");
            boolean asc = (boolean) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CharacterSelectScreen.class, "isAscensionModeUnlocked");
            if(tokens[0].equals("asc") && ready && asc){
                if(tokens.length == 1) {
                    Hitbox hb = (Hitbox) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CharacterSelectScreen.class, "ascensionModeHb");
                    hb.clicked = true;
                }else{
                    try{
                        int in = Integer.parseInt(tokens[1]);
                        CharacterOption select = null;
                        for(CharacterOption co : CardCrawlGame.mainMenuScreen.charSelectScreen.options){
                            if(co.selected)
                                select = co;
                        }
                        if(select == null)
                            return;
                        if(in < CardCrawlGame.mainMenuScreen.charSelectScreen.ascensionLevel){
                            select.decrementAscensionLevel(in);
                        }
                        if(in > CardCrawlGame.mainMenuScreen.charSelectScreen.ascensionLevel){
                            select.incrementAscensionLevel(in);
                        }
                    }catch (Exception ignored){
                    }
                }
                return;
            }
            if(input.equals("embark") && ready){
                CardCrawlGame.mainMenuScreen.charSelectScreen.confirmButton.hb.clicked = true;
                return;
            }
            if(input.equals("+") && ready){
                Hitbox hb = (Hitbox) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CharacterSelectScreen.class, "ascRightHb");
                hb.clicked = true;
                return;
            }
            if(input.equals("-") && ready){
                Hitbox hb = (Hitbox) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CharacterSelectScreen.class, "ascLeftHb");
                hb.clicked = true;
                return;
            }
        }

        if(input.equals("custom")){
            inspect.setText(custom.getText());
            return;
        }

        AbstractDungeon d = CardCrawlGame.dungeon;

        if(tokens[0].equals("help")){
            inspect.setText(displayHelp(tokens));
            return;
        }

        if(input.equals("stats") && CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.statsScreen.statScreenUnlocked()){
            inspect.setText(getStats());
            return;
        }

        if(tokens[0].equals("comp")){
            inspect.setText(compendium(tokens));
            return;
        }

        if(tokens[0].equals("achieve") || tokens[0].equals("a")){
            inspect.setText(inspectAchievements(tokens));
            return;
        }

        if(tokens[0].equals("save") && tokens.length == 2){
            savedOutput.put(tokens[1], inspect.inspect.getText());
            return;
        }

        if(tokens[0].equals("load") && tokens.length == 2 && savedOutput.containsKey(tokens[1])){
            inspect.setText(savedOutput.get(tokens[1]));
            return;
        }

        if(input.equals("leader") && CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.MAIN_MENU && DistributorFactory.isLeaderboardEnabled()){
            CardCrawlGame.mainMenuScreen.leaderboardsScreen.open();
            return;
        }

        if(input.equals("patch") && CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.MAIN_MENU){
            CardCrawlGame.mainMenuScreen.patchNotesScreen.open();
            return;
        }

        if(input.equals("slot") && CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.MAIN_MENU){
            CardCrawlGame.mainMenuScreen.saveSlotScreen.open(CardCrawlGame.playerName);
            CardCrawlGame.mainMenuScreen.screen = MainMenuScreen.CurScreen.SAVE_SLOT;
            return;
        }

        if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.PATCH_NOTES){
            if(input.equals("back")){
                CardCrawlGame.mainMenuScreen.patchNotesScreen.button.hb.clicked = true;
                return;
            }
        }

        if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.SAVE_SLOT){
            if(input.equals("back") && !CardCrawlGame.mainMenuScreen.saveSlotScreen.cancelButton.isHidden){
                CardCrawlGame.mainMenuScreen.saveSlotScreen.cancelButton.hb.clicked = true;
                return;
            }
            if(tokens[0].equals("new")){
                try{
                    int in = Integer.parseInt(tokens[1]);
                    if(CardCrawlGame.mainMenuScreen.saveSlotScreen.slots.get(in).emptySlot) {
                        CardCrawlGame.mainMenuScreen.saveSlotScreen.slots.get(in).slotHb.clicked = true;
                    }
                    return;
                }catch(Exception ignored){
                }
            }
            if(tokens[0].equals("rename")){
                try{
                    int in = Integer.parseInt(tokens[1]);
                    CardCrawlGame.mainMenuScreen.saveSlotScreen.openRenamePopup(in, false);
                    return;
                }catch(Exception ignored){
                }
            }
            if(tokens[0].equals("delete")){
                try{
                    int in = Integer.parseInt(tokens[1]);
                    CardCrawlGame.mainMenuScreen.saveSlotScreen.openDeletePopup(in);
                    return;
                }catch(Exception ignored){
                }
            }
            if(tokens[0].equals("open")){
                try{
                    int in = Integer.parseInt(tokens[1]);
                    if(!CardCrawlGame.mainMenuScreen.saveSlotScreen.slots.get(in).emptySlot) {
                        CardCrawlGame.mainMenuScreen.saveSlotScreen.slots.get(in).slotHb.clicked = true;
                    }
                    return;
                }catch(Exception ignored){
                }
            }

            if(CardCrawlGame.mainMenuScreen.saveSlotScreen.curPop == SaveSlotScreen.CurrentPopup.DELETE){
                if(input.equals("yes")){
                    DeleteSaveConfirmPopup delete = (DeleteSaveConfirmPopup)basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.saveSlotScreen, SaveSlotScreen.class, "deletePopup");
                    Hitbox hb = (Hitbox)basemod.ReflectionHacks.getPrivate(delete, DeleteSaveConfirmPopup.class, "yesHb");
                    hb.clicked = true;
                    return;
                }
                if(input.equals("no")){
                    DeleteSaveConfirmPopup delete = (DeleteSaveConfirmPopup)basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.saveSlotScreen, SaveSlotScreen.class, "deletePopup");
                    Hitbox hb = (Hitbox)basemod.ReflectionHacks.getPrivate(delete, DeleteSaveConfirmPopup.class, "noHb");
                    hb.clicked = true;
                    return;
                }
            }

        }

        if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.DAILY && !CommandExecutor.isInDungeon()){

            if(input.equals("back")){
                MenuCancelButton c = (MenuCancelButton) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.dailyScreen, DailyScreen.class, "cancelButton");
                c.hb.clicked = true;
                return;
            }else if(input.equals("embark")){
                ConfirmButton c = (ConfirmButton) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.dailyScreen, DailyScreen.class, "confirmButton");
                c.hb.clicked = true;
                return;
            }else if(input.equals("mine")){
                Hitbox hb = (Hitbox)basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.dailyScreen, DailyScreen.class, "viewMyScoreHb");
                hb.clicked = true;
                return;
            }else if(input.equals("prev")){
                Hitbox hb = (Hitbox)basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.dailyScreen, DailyScreen.class, "prevDayHb");
                hb.clicked = true;
                return;
            }else if(input.equals("next")){
                long day = (long)basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.dailyScreen, DailyScreen.class, "currentDay");
                if(day != 0L && day < TimeHelper.daySince1970()){
                    Hitbox hb = (Hitbox)basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.dailyScreen, DailyScreen.class, "nextDayHb");
                    hb.clicked = true;
                    return;
                }
            }else if(input.equals("+")){
                if(CardCrawlGame.mainMenuScreen.dailyScreen.entries.size() == 20){
                    Hitbox hb = (Hitbox)basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.dailyScreen, DailyScreen.class, "nextHb");
                    hb.clicked = true;
                    return;
                }
            }else if(input.equals("-")){
                if(CardCrawlGame.mainMenuScreen.dailyScreen.currentStartIndex != 1){
                    Hitbox hb = (Hitbox)basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.dailyScreen, DailyScreen.class, "prevHb");
                    hb.clicked = true;
                    return;
                }
            }

        }

        if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.CUSTOM && !CommandExecutor.isInDungeon()){

            if(input.equals("back")){
                MenuCancelButton c = (MenuCancelButton) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.customModeScreen, CustomModeScreen.class, "cancelButton");
                c.hb.clicked = true;
                return;
            }else if(input.equals("embark")){
                GridSelectConfirmButton c = (GridSelectConfirmButton) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.customModeScreen, CustomModeScreen.class, "confirmButton");
                c.hb.clicked = true;
                return;
            }

            switch(tokens[0]){

                case "char":
                    try{
                        int in = Integer.parseInt(tokens[1]);
                        CardCrawlGame.mainMenuScreen.customModeScreen.options.get(in).hb.clicked = true;
                    }catch(Exception ignored){
                    }
                    return;
                case "asc":
                    if(tokens.length == 1){
                        Hitbox hb = (Hitbox) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.customModeScreen, CustomModeScreen.class, "ascensionModeHb");
                        hb.clicked = true;
                    }else if(CardCrawlGame.mainMenuScreen.customModeScreen.isAscensionMode){
                        try{
                            int in = Integer.parseInt(tokens[1]);
                            if(in >= 1 && in <= 20) {
                                CardCrawlGame.mainMenuScreen.customModeScreen.ascensionLevel = in;
                            }
                        }catch(Exception ignored){
                        }
                    }
                    return;
                case "seed":
                    Hitbox hb = (Hitbox) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.customModeScreen, CustomModeScreen.class, "seedHb");
                    inspect.setText("\r\nGo to main game window, paste the seed, then hit enter.\r\n");
                    hb.clicked = true;
                    return;
                case "mod":

                    if(tokens.length > 2 && tokens[1].equals("i")){
                        ArrayList<CustomMod> modList = (ArrayList<CustomMod>) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.customModeScreen, CustomModeScreen.class, "modList");
                        try{
                            int in = Integer.parseInt(tokens[2]);
                            String s = modList.get(in).name + "\r\n" + modList.get(in).description;
                            inspect.setText(s);
                        }catch(Exception ignored){
                        }
                    } else if(tokens.length > 1) {
                        ArrayList<CustomMod> modList = (ArrayList<CustomMod>) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.customModeScreen, CustomModeScreen.class, "modList");
                        try{
                            int in = Integer.parseInt(tokens[1]);
                            modList.get(in).hb.clicked = true;
                        }catch(Exception ignored){
                        }
                    }
                    return;
                case "simple":
                    inspect.setText(custom.getSimpleText());
                    return;

            }

        }

        if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.LEADERBOARD && !CommandExecutor.isInDungeon()){

            if(input.equals("mine")) {
                if(((FilterButton)CardCrawlGame.mainMenuScreen.leaderboardsScreen.regionButtons.get(0)).active){
                    return;
                }
                Hitbox hb = (Hitbox) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.leaderboardsScreen, LeaderboardScreen.class, "viewMyScoreHb");
                hb.clicked = true;
                return;
            }else if(input.equals("+")){
                if(CardCrawlGame.mainMenuScreen.leaderboardsScreen.entries.size() == 20){
                    Hitbox hb = (Hitbox)basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.leaderboardsScreen, LeaderboardScreen.class, "nextHb");
                    hb.clicked = true;
                    return;
                }
            }else if(input.equals("-")){
                if(CardCrawlGame.mainMenuScreen.leaderboardsScreen.currentStartIndex != 1){
                    Hitbox hb = (Hitbox)basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.leaderboardsScreen, LeaderboardScreen.class, "prevHb");
                    hb.clicked = true;
                    return;
                }
            }
            if(tokens.length > 1) {
                if (tokens[0].equals("char")) {
                    try {
                        int in = Integer.parseInt(tokens[1]);
                        if(in >= 0 && in < CardCrawlGame.mainMenuScreen.leaderboardsScreen.charButtons.size()){
                            CardCrawlGame.mainMenuScreen.leaderboardsScreen.charButtons.get(in).hb.clicked = true;
                        }
                        return;
                    }catch (Exception ignored){
                    }
                }else if (tokens[0].equals("region")) {
                    try {
                        int in = Integer.parseInt(tokens[1]);
                        if(in >= 0 && in < CardCrawlGame.mainMenuScreen.leaderboardsScreen.regionButtons.size()){
                            CardCrawlGame.mainMenuScreen.leaderboardsScreen.regionButtons.get(in).hb.clicked = true;
                        }
                        return;
                    }catch (Exception ignored){
                    }
                }else if (tokens[0].equals("type")) {
                    try {
                        int in = Integer.parseInt(tokens[1]);
                        if(in >= 0 && in < CardCrawlGame.mainMenuScreen.leaderboardsScreen.typeButtons.size()){
                            CardCrawlGame.mainMenuScreen.leaderboardsScreen.typeButtons.get(in).hb.clicked = true;
                        }
                        return;
                    }catch (Exception ignored){
                    }
                }
            }

        }



        //Start a new run. Only does anything if not in dungeon.

        if (tokens[0].equals("start") && CardCrawlGame.mainMenuScreen != null && !CommandExecutor.isInDungeon() && CardCrawlGame.mainMenuScreen.buttons.get(CardCrawlGame.mainMenuScreen.buttons.size()-1).result == MenuButton.ClickResult.PLAY) {
            try {
                if (CardCrawlGame.mode == CardCrawlGame.GameMode.CHAR_SELECT && isUnlocked(tokens))
                    if(!ascensionReborn)
                        CommandExecutor.executeCommand(input);
                    else
                        ascensionRebornStart(tokens);
                return;
            } catch (Exception e) {
                return;
            }
        }

        //Commands below are only usable in a dungeon
        if (!CommandExecutor.isInDungeon())
            return;

        if(input.equals("seed")){
            inspect.setText("\r\n" + SeedHelper.getString(Settings.seed));
            return;
        }

        if(tokens[0].equals("deck") && tokens.length == 2){
            try{
                int in = Integer.parseInt(tokens[1]);
                inspect.setText(inspectCard(AbstractDungeon.player.masterDeck.group.get(in)));
                return;
            }catch (Exception ignored){
            }
        }

        //Potion Commands
        if (tokens[0].equals("potion") || tokens[0].equals("pot")) {
            try {
                if(tokens.length >= 3 && (tokens[1].equals("inspect") || tokens[1].equals("i"))){

                    int in = Integer.parseInt(tokens[2]);
                    if(in >= 0 && in < AbstractDungeon.player.potions.size()) {
                        AbstractPotion p = AbstractDungeon.player.potions.get(in);

                        inspect.setText(inspectPotion(p));

                    }
                    return;
                }else {
                    StringBuilder command = new StringBuilder("potion ");
                    switch(tokens[1]){
                        case "use":
                        case "u":
                            command.append("use ");
                            break;
                        case "discard":
                        case "d":
                            command.append("discard ");
                            break;
                    }
                    for(int i=2;i<tokens.length;i++){
                        command.append(tokens[i]).append(" ");
                    }
                    if(tokens.length == 3){
                        int index = singleMonster();
                        if(index >= 0){
                            command.append(index);
                        }
                    }
                    System.out.println(command.toString());
                    CommandExecutor.executeCommand(command.toString());
                }
                return;
            } catch (Exception e) {
                System.out.println(e.toString());
                return;
            }
        }

        if (tokens[0].equals("choice") || tokens[0].equals("c")) {
            executeChoice(tokens);
        }

        if (tokens[0].equals("relic")) {
            try {
                if(tokens.length == 2) {
                    int in = Integer.parseInt(tokens[1]);
                    if(in >= 0 && in < AbstractDungeon.player.relics.size()){

                        AbstractRelic r = AbstractDungeon.player.relics.get(in);

                        inspect.setText(inspectRelic(r));

                    } else if(in >= 0 && in >= AbstractDungeon.player.relics.size() && in < AbstractDungeon.player.relics.size() + AbstractDungeon.player.blights.size()){

                        AbstractBlight b = AbstractDungeon.player.blights.get(in - AbstractDungeon.player.relics.size());

                        inspect.setText(inspectBlight(b));

                    }
                }else if(TextTheSpire.stslib && tokens.length == 3 && tokens[2].equals("a")){
                    int in = Integer.parseInt(tokens[1]);
                    if(in >= 0 && in < AbstractDungeon.player.relics.size()){
                        AbstractRelic r = AbstractDungeon.player.relics.get(in);
                        if(r instanceof ClickableRelic){
                            ((ClickableRelic) r).onRightClick();
                        }
                    }
                }
                return;
            } catch (Exception e) {
                return;
            }
        }

        //Press a confirm or cancel button. Only usable if such a button exists
        if (ChoiceScreenUtils.isConfirmButtonAvailable() && input.equals(ChoiceScreenUtils.getConfirmButtonText())) {

            if(AbstractDungeon.screen == AbstractDungeon.CurrentScreen.DEATH){
                AbstractDungeon.deathScreen.returnButton.hb.clicked = true;
                return;
            }
            if(AbstractDungeon.screen == AbstractDungeon.CurrentScreen.VICTORY){
                AbstractDungeon.victoryScreen.returnButton.hb.clicked = true;
                return;
            }

            ChoiceScreenUtils.pressConfirmButton();
            return;
        }
        if (ChoiceScreenUtils.isCancelButtonAvailable() && input.equals(ChoiceScreenUtils.getCancelButtonText())) {

            if(input.equals("skip") && ChoiceScreenUtils.getCurrentChoiceType()==ChoiceScreenUtils.ChoiceType.BOSS_REWARD){
                MenuCancelButton button = (MenuCancelButton) ReflectionHacks.getPrivate(AbstractDungeon.bossRelicScreen, BossRelicSelectScreen.class, "cancelButton");
                button.hb.clicked = true;
                return;
            }

            ChoiceScreenUtils.pressCancelButton();
            return;
        }

        //Commands only usable during combat. Includes play and end.
        if (d != null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
            input = input.toLowerCase();

            switch (tokens[0]) {
                case "end":
                    try {
                        CommandExecutor.executeCommand(tokens[0]);
                    } catch (Exception e) {
                        return;
                    }
                    return;
                case "hand":
                case "h":
                    try {
                        int in;
                        in = Integer.parseInt(tokens[1]);
                        choiceCard(in, AbstractDungeon.player.hand.group);
                    } catch (Exception e) {
                        return;
                    }
                    return;
                case "power":
                case "pow":
                    parsePower(tokens);
                    return;
                case "orbs":
                    try {
                        int in;
                        in = Integer.parseInt(tokens[1]);
                        ArrayList<AbstractOrb> ol = AbstractDungeon.player.orbs;
                        if(in >= 0 && in < ol.size()) {
                            inspect.setText(inspectOrb(AbstractDungeon.player.orbs.get(in)));
                        }
                    } catch (Exception e) {
                        return;
                    }
                    return;
                case "exhaust":
                    CardGroup h = AbstractDungeon.player.exhaustPile;
                    StringBuilder s = new StringBuilder("\r\nExhaust\r\n");
                    s.append("Size: ").append(h.size()).append("\r\n");

                    if(h.size() > 0) {
                        for (AbstractCard c : h.group) {
                            s.append(c.name).append("\r\n");
                        }
                    }
                    inspect.setText(s.toString());
                    return;
                default:
                    try {
                        if(TextTheSpire.replayTheSpire && AbstractDungeon.getCurrRoom().monsters.monsters.get(AbstractDungeon.getCurrRoom().monsters.monsters.size()-1) instanceof FadingForestBoss){
                            boolean show = (boolean)basemod.ReflectionHacks.getPrivateStatic(GenericEventDialog.class, "show");
                            if(show){
                                ArrayList<LargeDialogOptionButton> buttons = ((FadingForestBoss) AbstractDungeon.getCurrRoom().monsters.monsters.get(AbstractDungeon.getCurrRoom().monsters.monsters.size()-1)).imageEventText.optionList;
                                ArrayList<LargeDialogOptionButton> activeButtons = new ArrayList<>();
                                for(LargeDialogOptionButton b : buttons){
                                    if(!b.isDisabled){
                                        activeButtons.add(b);
                                    }
                                }
                                int in;
                                in = Integer.parseInt(input) - 1;
                                activeButtons.get(in).hb.clicked = true;
                                return;
                            }
                        }
                        if(ChoiceScreenUtils.getCurrentChoiceType() == ChoiceScreenUtils.ChoiceType.NONE){
                            String playInput = "play " + input;
                            String[] playTokens = playInput.split("\\s+");
                            if(playTokens.length == 2){
                                int index = singleMonster();
                                if(index != -1){
                                    playInput = playInput + " " + index;
                                }
                            }
                            CommandExecutor.executeCommand(playInput);
                            return;
                        }
                        int in;
                        in = Integer.parseInt(input) - 1;
                        ChoiceScreenUtils.executeChoice(in);
                    } catch (Exception ignored) {
                    }
            }
        } else if (tokens[0].equals("map") || tokens[0].equals("m")){

            if(tokens.length >= 3){
                inspect.setText(Inspect.inspectMap(tokens));
            }

        }else if (tokens[0].equals("path")){

            if(tokens.length >= 3){
                inspect.setText(Inspect.inspectPaths(tokens));
            }

        } else if (AbstractDungeon.getCurrRoom() instanceof TreasureRoomBoss && ChoiceScreenUtils.getCurrentChoiceType() == ChoiceScreenUtils.ChoiceType.CHEST) {
            AbstractChest chest = ((TreasureRoomBoss) AbstractDungeon.getCurrRoom()).chest;
            if(!chest.isOpen) {
                chest.isOpen = true;
                chest.open(false);
            }
        } else {
            //Everything else is a choice screen command. Only a number is needed.
            int in;
            try {
                in = Integer.parseInt(input) - 1;

                if(AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom().event instanceof GremlinMatchGame){
                    ArrayList<AbstractCard> choiceList = new ArrayList<>();
                    GremlinMatchGame event = (GremlinMatchGame) (AbstractDungeon.getCurrRoom().event);
                    CardGroup gameCardGroup = (CardGroup) ReflectionHacks.getPrivate(event, GremlinMatchGame.class, "cards");
                    for (AbstractCard c : gameCardGroup.group) {
                        if (c.isFlipped) {
                            choiceList.add(c);
                        }
                    }
                    if(choiceList.size() > in){
                        inspect.setText("Goblin Match Card\r\nPosition " + GremlinMatchGamePatch.cardPositions.get(choiceList.get(in).uuid) + "\r\n" + inspectCard(choiceList.get(in)));
                    }
                }

                if(TextTheSpire.replayTheSpire && ChoiceScreenUtils.getCurrentChoiceType() == ChoiceScreenUtils.ChoiceType.SHOP_SCREEN && ReplayShopInitCardsPatch.doubleCard != null && in == Choices.doubleIndex){
                    ReplayShopInitCardsPatch.doubleCard.hb.clicked = true;
                    ReplayShopInitCardsPatch.doubleCard.hb.hovered = true;
                    return;
                }

                ChoiceScreenUtils.executeChoice(in);
            } catch (Exception ignored) {
            }
        }
    }

    private static void ascensionRebornStart(String[] tokens) throws InvalidCommandException {
        if (tokens.length < 2) {
            throw new InvalidCommandException(tokens, InvalidCommandException.InvalidCommandFormat.MISSING_ARGUMENT);
        }
        int ascensionLevel = 0;
        boolean seedSet = false;
        long seed = 0;
        AbstractPlayer.PlayerClass selectedClass = null;
        for(AbstractPlayer.PlayerClass playerClass : AbstractPlayer.PlayerClass.values()) {
            if(playerClass.name().equalsIgnoreCase(tokens[1])) {
                selectedClass = playerClass;
            }
        }
        // Better to allow people to specify the character as "silent" rather than requiring "the_silent"
        if(tokens[1].equalsIgnoreCase("silent")) {
            selectedClass = AbstractPlayer.PlayerClass.THE_SILENT;
        }
        if(selectedClass == null) {
            throw new InvalidCommandException(tokens, InvalidCommandException.InvalidCommandFormat.INVALID_ARGUMENT, tokens[1]);
        }
        if(tokens.length >= 3) {
            try {
                ascensionLevel = Integer.parseInt(tokens[2]);
            } catch (NumberFormatException e) {
                throw new InvalidCommandException(tokens, InvalidCommandException.InvalidCommandFormat.INVALID_ARGUMENT, tokens[2]);
            }
            if(ascensionLevel < -20 || ascensionLevel > 25) {
                throw new InvalidCommandException(tokens, InvalidCommandException.InvalidCommandFormat.OUT_OF_BOUNDS, tokens[2]);
            }
        }
        if(tokens.length >= 4) {
            String seedString = tokens[3].toUpperCase();
            if(!seedString.matches("^[A-Z0-9]+$")) {
                throw new InvalidCommandException(tokens, InvalidCommandException.InvalidCommandFormat.INVALID_ARGUMENT, seedString);
            }
            seedSet = true;
            seed = SeedHelper.getLong(seedString);
            boolean isTrialSeed = TrialHelper.isTrialSeed(seedString);
            if (isTrialSeed) {
                Settings.specialSeed = seed;
                Settings.isTrial = true;
                seedSet = false;
            }
        }
        if(!seedSet) {
            seed = SeedHelper.generateUnoffensiveSeed(new Random(System.nanoTime()));
        }
        Settings.seed = seed;
        Settings.seedSet = seedSet;
        AbstractDungeon.generateSeeds();
        AbstractDungeon.ascensionLevel = ascensionLevel;
        AbstractDungeon.isAscensionMode = ascensionLevel != 0;
        CardCrawlGame.startOver = true;
        CardCrawlGame.mainMenuScreen.isFadingOut = true;
        CardCrawlGame.mainMenuScreen.fadeOutMusic();
        CharacterManager manager = new CharacterManager();
        manager.setChosenCharacter(selectedClass);
        CardCrawlGame.chosenCharacter = selectedClass;
        GameStateListener.resetStateVariables();
    }

    public void executeChoice(String[] tokens){

        try {
            if(tokens.length >= 2) {
                int in = Integer.parseInt(tokens[1]);

                ChoiceScreenUtils.ChoiceType c = ChoiceScreenUtils.getCurrentChoiceType();

                if(c == ChoiceScreenUtils.ChoiceType.SHOP_SCREEN){
                    choiceShop(in);
                } else if (c == ChoiceScreenUtils.ChoiceType.GRID){
                    choiceCard(in, ChoiceScreenUtils.getGridScreenCards());
                } else if (c == ChoiceScreenUtils.ChoiceType.CARD_REWARD){
                    choiceCard(in, AbstractDungeon.cardRewardScreen.rewardGroup);
                } else if (c == ChoiceScreenUtils.ChoiceType.HAND_SELECT){
                    choiceCard(in, AbstractDungeon.handCardSelectScreen.selectedCards.group);
                } else if (c == ChoiceScreenUtils.ChoiceType.BOSS_REWARD){

                    in--;

                    if(in >= 0 && in < AbstractDungeon.bossRelicScreen.relics.size()){

                        AbstractRelic r = AbstractDungeon.bossRelicScreen.relics.get(in);

                        inspect.setText(inspectRelic(r));

                    }

                } else if (c == ChoiceScreenUtils.ChoiceType.COMBAT_REWARD){
                    choiceCombatReward(in);
                }

            }

        } catch (Exception ignored) {

        }

    }

    public void choiceCombatReward(int in){
        in--;

        ArrayList<RewardItem> rewards = AbstractDungeon.combatRewardScreen.rewards;

        if(in >= 0 && in < rewards.size()){

            RewardItem reward = rewards.get(in);

            if(reward.type == RewardItem.RewardType.RELIC){

                AbstractRelic r = reward.relic;

                inspect.setText(inspectRelic(r));

            }else if (reward.type == RewardItem.RewardType.POTION){

                AbstractPotion p = reward.potion;

                inspect.setText(inspectPotion(p));

            }

        }
    }

    public void choiceCard(int in, ArrayList<AbstractCard> list){
        in--;

        if(in >= 0 && in < list.size()){

            AbstractCard card = list.get(in);

            inspect.setText(inspectCard(card));

        }
    }

    public void choiceShop(int in){
        ArrayList<Object> shopItems = Choices.getAvailableShopItems();

        in--;

        if (in >= 0 && in < shopItems.size()) {

            Object item = shopItems.get(in);

            if (item instanceof String) {

                if(((String)item).equals("purge-" + ShopScreen.actualPurgeCost)){
                    inspect.setText("Remove a card from your deck.\r\n");
                }

            } else if (item instanceof AbstractCard) {

                AbstractCard card = (AbstractCard)item;

                inspect.setText(inspectCard(card));

            } else if (item instanceof StoreRelic) {

                AbstractRelic r = ((StoreRelic)item).relic;

                inspect.setText(inspectRelic(r));

            } else if (item instanceof StorePotion) {

                AbstractPotion p = ((StorePotion)item).potion;

                inspect.setText(inspectPotion(p));

            }
        }
    }

    public void parsePower(String[] tokens){

        if(tokens.length >= 3) {
            switch (tokens[1]){
                case "m":
                case "monster":
                    if(tokens.length >= 4){
                        try{
                            int mon = Integer.parseInt(tokens[2]);
                            int pow = Integer.parseInt(tokens[3]);
                            inspect.setText(inspectPower(AbstractDungeon.getCurrRoom().monsters.monsters.get(mon).powers.get(pow)));
                        }catch(Exception ignored){
                        }
                    }
                    return;
                case "player":
                case "p":
                    try{
                        int pow = Integer.parseInt(tokens[2]);
                        inspect.setText(inspectPower(AbstractDungeon.player.powers.get(pow)));
                    }catch(Exception ignored){
                    }
                    return;
            }
        }

    }

    public String ascensionNotes(){
        StringBuilder s = new StringBuilder("\r\n");
        s.append("Ascension Modifiers\r\nAscension levels include all modifiers from it to 0.\r\n");
        if(ascensionReborn){
            s.append(   "25: Choose a blight before every boss relic.\r\n" +
                        "24: Ascender's bane is no longer ethereal.\r\n" +
                        "23: Healing is less effective.\r\n" +
                        "22: Some of your gold is eaten (You'll see, trust me).\r\n" +
                        "21: Merchant's card removal now costs more.\r\n" );
        }
        s.append(   "20: Double boss. (Fight 2 bosses at the end of Act 3.)\r\n" +
                    "19: Boss enemies have more challenging movesets and abilities.\r\n" +
                    "18: Elite enemies have more challenging movesets and abilities.\r\n" +
                    "17: Normal enemies have more challenging movesets and abilities.\r\n" +
                    "16: Shops are more costly. (10% more)\r\n" +
                    "15: Unfavorable events.\r\n" +
                    "14: Lower max HP. (-5 for Ironclad, -4 for Silent, Defect, and Watcher)\r\n" +
                    "13: Bosses drop less gold. (25% less)\r\n" +
                    "12: Upgraded cards appear less often. (50% less)\r\n" +
                    "11: Start each run with 1 less potion slot.\r\n" +
                    "10: Start each run cursed. (Ascender's Bane)\r\n" +
                    "9: Bosses are tougher.\r\n" +
                    "8: Elites are tougher.\r\n" +
                    "7: Normal enemies are tougher.\r\n" +
                    "6: Start each run damaged (-10% health)\r\n" +
                    "5: Heal less after Boss battles (75% of missing health)\r\n" +
                    "4: Bosses are deadlier.\r\n" +
                    "3: Elites are deadlier.\r\n" +
                    "2: Normal enemies are deadlier.\r\n" +
                    "1: Elites spawn more often.\r\n" +
                    "0: No Modifiers.\r\n");
        if(ascensionReborn){
            s.append(   "-1: Normal enemies are less deadly.\r\n" +
                        "-2: Elites are less deadly.\r\n" +
                        "-3: Bosses are less deadly.\r\n" +
                        "-4: Normal enemies are less tough.\r\n" +
                        "-5: Elites are less tough.\r\n" +
                        "-6: Bosses are less tough.\r\n" +
                        "-7: Upgraded cards appear more often.\r\n" +
                        "-8: Richer bosses.\r\n" +
                        "-9: Higher Max HP.\r\n" +
                        "-10: Shops cost less.\r\n" +
                        "-11: Gain Max HP when you defeat a boss.\r\n" +
                        "-12: You gain more gold.\r\n" +
                        "-13: Healing is more effective.\r\n" +
                        "-14: Normal enemies now have chance to drop a relic.\r\n" +
                        "-15: Elites now drop 1 more card.\r\n" +
                        "-16: Bosses now drop a rare relic.\r\n" +
                        "-17: All combat rewards now contain additional potion.\r\n" +
                        "-18: Start each combat with one extra max energy.\r\n" +
                        "-19: Draw one more card each turn.\r\n" +
                        "-20: All starting cards are upgraded; all non-boss chests contain an extra relic.\r\n");
        }

        return s.toString();
    }

    public String modNotes(){
        StringBuilder s = new StringBuilder("\r\n");
        if(stslib){
            s.append("StSLib\r\nNew Keywords:\r\n");
            s.append("Exhaustive - Exhausts after that many uses.\r\n");
            s.append("Refund - Returns an amount of energy on use.\r\n");
            s.append("New Mechanics\r\n");
            s.append("Clickable Relics. Use the format \"relic [index] a\" to use a clickable relic\r\n");
        }
        if(replayTheSpire){
            s.append("Replay the Spire\r\nNew Keywords:\r\n");
            s.append("Fleeting - Purges itself on use.\r\n");
            s.append("Soulbound - You cannot remove it from your deck\r\n");
            s.append("Languid - Deal 1 less dmg per stack. Reduces by 1 each turn.\r\n");
            s.append("Decrepit - Takes 1 more dmg per stack. Reduces by 1 each turn.\r\n");
            s.append("Autoplay - Plays itself when drawn.\r\n");
            s.append("Grave - Starts in your discard.\r\n");
            s.append("Startup - Effects at start of game.\r\n");
            s.append("Reflection - Returns completely blocked damaged back to dealer. Reduces by 1 each turn and is removed at 0.\r\n");
            s.append("Necrotic Poison - Deals double damage and is reduced by half each turn rounding down. Normal poison does not count down while Necrotic is applied.\r\n");
            s.append("Shielding - A type of block that does not wear off between rounds.\r\n");
            s.append("Temporary HP - Max HP that is removed at end of combat.\r\n");
            s.append("New Mechanics\r\n");
            s.append("The boss Fading Forest uses events during combat. Between turns check the Event and Choices windows to proceed.\r\n");
            s.append("The shop can include 2 for 1 sales which will be noted after the name in the shop.\r\n");
        }
        if(ascensionReborn){
            s.append("Ascension Reborn\r\n");
            s.append("This adds new ascension levels from -20 to 25.\r\n");
            s.append("Check what ascension levels do with the command ascension.\r\n");
        }
        return s.toString();
    }

    public String displayHelp(String[] tokens){

        if(tokens.length == 1){
            return  "\r\nHere are a list of commands you can perform." +
                    "\r\nFor more details enter help followed by a command." +
                    "\r\nExample:" +
                    "\r\nhelp start" +
                    "\r\nAll commands are input in the prompt window." +
                    "\r\ntutorial" +
                    "\r\nascension" +
                    "\r\nstart" +
                    "\r\nabandon" +
                    "\r\ncontinue" +
                    "\r\nslot" +
                    "\r\nquit" +
                    "\r\nseed" +
                    "\r\nstats" +
                    "\r\ncomp" +
                    "\r\nhistory" +
                    "\r\nvolume" +
                    "\r\nlang" +
                    "\r\nachieve" +
                    "\r\nmod" +
                    "\r\nplay" +
                    "\r\ncustom" +
                    "\r\ndaily" +
                    "\r\nleader" +
                    "\r\npotion" +
                    "\r\nchoice" +
                    "\r\npower" +
                    "\r\nend" +
                    "\r\nshow" +
                    "\r\nhide" +
                    "\r\nChoices" +
                    "\r\nDeck" +
                    "\r\nDiscard" +
                    "\r\nEvent" +
                    "\r\nHand" +
                    "\r\nexhaust" +
                    "\r\nOutput" +
                    "\r\nLog" +
                    "\r\nSave" +
                    "\r\nLoad" +
                    "\r\nMap" +
                    "\r\npath" +
                    "\r\nMonster" +
                    "\r\nOrbs" +
                    "\r\nPlayer" +
                    "\r\nRelic";
        }else{
            switch(tokens[1].toLowerCase()){
                case "tutorial":
                    return  getTutorial();
                case "ascension":
                    return  "\r\nascension" +
                            "\r\nThis command lists all the ascension modifiers.";
                case "start":
                case "abandon":
                case "continue":
                    return  "\r\nstart, abandon, and continue" +
                            "\r\nThese commands let you enter a run. You see these options while on the start menu." +
                            "\r\nIf there is no save file, you can start a run." +
                            "\r\nThe format is" +
                            "\r\nstart, class name, ascension level, seed" +
                            "\r\nThe second 2 are optional." +
                            "\r\nExamples:" +
                            "\r\nstart ironclad" +
                            "\r\nstart defect 10" +
                            "\r\nDefault ascension is 0." +
                            "\r\nIf you want to enter a seed you will need to enter an ascension level." +
                            "\r\nIf there is a save file, you will have the continue and abandon options." +
                            "\r\nabandon will delete your current run after a confirmation popup." +
                            "\r\nThe choices window will display all of the classes and their unlocked ascension level." +
                            "\r\nIf the character is locked it will display locked.";
                case "slot":
                    return  "\r\nslot" +
                            "\r\nFrom the main menu this opens up the save slot screen." +
                            "\r\nDetails on how to navigate this screen will appear in the Choices window.";
                case "quit":
                    return  "\r\nquit" +
                            "\r\nWhile in a run, this command saves and quits to main menu." +
                            "\r\nWhile on the main menu, this command quits the game." +
                            "\r\nAnother easy way to close the game would be to close the ModTheSpire window.";
                case "seed":
                    return  "\r\nseed" +
                            "\r\nThis command displays the run's seed to the output window." +
                            "\r\nA seed is used for random number generation." +
                            "\r\nIt can be input when starting a run to have a set seed,";
                case "stats":
                    return  "\r\nstats" +
                            "\r\nThis command displays your save file's statistics to output." +
                            "\r\nWill not work on a fresh save file.";
                case "comp":
                    return  "\r\ncomp" +
                            "\r\nThis is the Compendium command." +
                            "\r\nThere are various categories and subcategories." +
                            "\r\nThe Categories are" +
                            "\r\ni for Ironclad" +
                            "\r\ns for Silent" +
                            "\r\nd for Defect" +
                            "\r\nw for Watcher" +
                            "\r\np for Potions" +
                            "\r\nr for Relics" +
                            "\r\nRelics has subcategories." +
                            "\r\nThese are i, s, d, w, and sh." +
                            "\r\nsh is shared and is all of the shared relics." +
                            "\r\nFollowing any command by a number will inspect the item at that index." +
                            "\r\nExamples" +
                            "\r\ncomp i" +
                            "\r\ncomp s 5" +
                            "\r\ncomp p" +
                            "\r\ncomp r w" +
                            "\r\ncomp r sh 10";
                case "history":
                    return  "\r\nhistory" +
                            "\r\nThis is the Run History command." +
                            "\r\nFrom the main menu you can active the Run History screen." +
                            "\r\nAlmost all commands will be disabled while on this screen." +
                            "\r\nYou can input close at any time to close Run History." +
                            "\r\nRead the Choices window to navigate Run History." +
                            "\r\nThere is currently an issues with Run History." +
                            "\r\nSome objects are named differently in the code than what they actually are." +
                            "\r\nAs Run History only uses text and not abstract objects, it would be difficult to grab the actual name." +
                            "\r\nFor now Run History is using these code names.";
                case "volume":
                    return  "\r\nvolume" +
                            "\r\nThis displays the current volume settings to output." +
                            "\r\nIt can also change the settings." +
                            "\r\nThe different volume types are master, music, and sound effects." +
                            "\r\nThere is an additional toggle for background ambience sounds." +
                            "\r\nThe format is" +
                            "\r\nvolume master/music/sound number" +
                            "\r\nThe number needs to be a decimal between 0 and 1." +
                            "\r\nExamples:" +
                            "\r\nvolume master 0.5" +
                            "\r\nvolume ambience";
                case "lang":
                    return  "\r\nlang" +
                            "\r\nThis displays the current language and available languages." +
                            "\r\nChange the language with lang followed by the index of the language you want." +
                            "\r\nExample:" +
                            "\r\nlang 0" +
                            "\r\nOnly game text like card descriptions and monster names will change." +
                            "\r\nThis does not affect the mod's text as it hasn't been translated yet." +
                            "\r\nRestart the game for the display to update.";
                case "achieve":
                    return  "\r\nachieve" +
                            "\r\nThis displays to output the list of locked and unlocked achievements." +
                            "\r\nachieve number displays the description of a given achievement." +
                            "\r\nachieve can be shortened to a.";
                case "mod":
                    return  "\r\nmod" +
                            "\r\nThis displays to output some notes on certain loaded content mods." +
                            "\r\nIf a mod isn't listed here then it either works out of the box or hasn't been implemented yet.";
                case "play":
                    return  "\r\nplay" +
                            "\r\nThis command lets you play cards from your hand." +
                            "\r\nThe format is" +
                            "\r\n[card number] [enemy number]" +
                            "\r\nEnemy number is optional for cards without targets or if there is only 1 target." +
                            "\r\nNote that the card number is the index in your hand." +
                            "\r\nIt changes as cards are played." +
                            "\r\nEnemy number does not change.";
                case "custom":
                    return  "\r\ncustom" +
                            "\r\nFrom the main menu this opens up the custom mode panel." +
                            "\r\nIt opens the Custom window which displays the options for a custom game." +
                            "\r\nYou may display to output a simplified version of the current settings with the command simple." +
                            "\r\nThe commands to set settings are char, asc, seed, and mod." +
                            "\r\nchar is the character command. The format is" +
                            "\r\nchar number" +
                            "\r\nasc is the ascension command." +
                            "\r\nasc on its own will toggle whether asc is on or off. The format for setting the level is" +
                            "\r\nasc number" +
                            "\r\nseed opens the seed panel." +
                            "\r\nYou will need to to go to the main game window, type or paste the seed, and hit enter." +
                            "\r\nAn empty seed generates a random seed when you embark." +
                            "\r\nmod lets you select and inspect modifiers." +
                            "\r\nThe format to select a mod is" +
                            "\r\nmod number" +
                            "\r\nThe format to inspect a mod is" +
                            "\r\nmod i number" +
                            "\r\nExample series of commands to start a custom game:" +
                            "\r\ncustom" +
                            "\r\nchar 2" +
                            "\r\nasc" +
                            "\r\nasc 15" +
                            "\r\nmod 10" +
                            "\r\nmod 15" +
                            "\r\nmod 13" +
                            "\r\nembark";
                case "daily":
                    return  "\r\ndaily" +
                            "\r\nFrom the main menu this opens up the daily climb screen." +
                            "\r\nOn that screen you can view the current daily mods and embark on a daily climb." +
                            "\r\nThere is also a Daily Climb Leaderboard." +
                            "\r\nUse the embark command to begin a Daily Climb." +
                            "\r\nUse the mine command to display personal scores." +
                            "\r\nUse the prev and next commands to change the date to display." +
                            "\r\nUse the + and - commands to shift the leaderboard by 20 ranks.";
                case "leader":
                    return  "\r\nleader" +
                            "\r\nFrom the main menu this opens up the leaderboard screen." +
                            "\r\nThe leaderboard is displayed in the Event window." +
                            "\r\nThe Choices window displays the leaderboard options." +
                            "\r\nOption 1 is char which is which Character." +
                            "\r\nOption 2 is region." +
                            "\r\nOption 3 is type." +
                            "\r\nFor all options used the format:" +
                            "\r\n[option] [number]" +
                            "\r\nExamples:" +
                            "\r\nchar 1" +
                            "\r\nregion 0" +
                            "\r\ntype 2" +
                            "\r\nUse the commands + and - to shift which ranks to display on the leaderboard." +
                            "\r\nUse the command mine while region is set to Global to display your scores.";
                case "potion":
                    return  "\r\npotion" +
                            "\r\nThis command lets you interact with your potions." +
                            "\r\nThere are 3 different options." +
                            "\r\nuse. discard, and inspect." +
                            "\r\nThe format for use is" +
                            "\r\npotion use [potion number] [enemy number]" +
                            "\r\nEnemy number is optional for potions without targets." +
                            "\r\nThe format for discard is" +
                            "\r\npotion discard [potion number]" +
                            "\r\nThe format for inspect is" +
                            "\r\npotion inspect [potion number]" +
                            "\r\nInspect displays what the potion does to the output window." +
                            "\r\npotion can be shortened to pot, use to u, discard to d, and inspect to i." +
                            "\r\nExamples:" +
                            "\r\npot u 1 1" +
                            "\r\npot d 2" +
                            "\r\npot i 1";
                case "choice":
                    return  "\r\nchoice" +
                            "\r\nNot to be confused with choices, which is one of the windows." +
                            "\r\nchoice displays the info for one of the choices in the choices window in the output window." +
                            "\r\nThe format is" +
                            "\r\nchoice [choice number]" +
                            "\r\nchoice can be shortened to c." +
                            "\r\nExample:" +
                            "\r\nc 1";
                case "power":
                    return  "\r\npower" +
                            "\r\nThis command inspects one of your or a monster's powers." +
                            "\r\nThe format to inspect one of your powers is" +
                            "\r\npower player [power number]" +
                            "\r\nThe format to inspect a monster's power is" +
                            "\r\npower monster [monster number] [power number]" +
                            "\r\npower can be shortened to pow, monster to m, and player to p." +
                            "\r\nExample:" +
                            "\r\npow p 1" +
                            "\r\npow m 1 2";
                case "end":
                    return  "\r\nend" +
                            "\r\nThis command ends your turn.";
                case "show":
                case "hide":
                    return  "\r\nshow and hide" +
                            "\r\nThese commands allow you to hide and unhide windows." +
                            "\r\nThe format is" +
                            "\r\n[show/hide] window name" +
                            "\r\nYou may also use all for window name to show or hide all windows besides output and prompt." +
                            "\r\nExamples:" +
                            "\r\nhide all" +
                            "\r\nshow map";
                case "choices":
                    return  "\r\nchoices" +
                            "\r\nThis command displays the text in the choices window in the output window." +
                            "\r\nThe choices window has all of the available choices you can make." +
                            "\r\nChoices are either numbered or are a single word." +
                            "\r\nEntering either the number or the word selects that choice.";
                case "deck":
                    return  "\r\ndeck" +
                            "\r\nThis command displays the text in the deck window in the output window." +
                            "\r\nThe deck window displays your deck size and all cards in the deck." +
                            "\r\nOut of combat it displays your master deck." +
                            "\r\nIn combat it displays your current deck.";
                case "discard":
                    return  "\r\ndiscard" +
                            "\r\nThis command displays the text in the discard window in the output window." +
                            "\r\nThe discard window displays your discard size and all cards in your discard.";
                case "event":
                    return  "\r\nevent" +
                            "\r\nThis command displays the event name and text in the event window in the output window." +
                            "\r\nThe event window displays the event dialogue." +
                            "\r\nThe dialogue does have some extra symbols and is often a single line of text." +
                            "\r\nI apologize for the inconvenience." +
                            "\r\nIt also displays the run score on death or victory.";
                case "hand":
                    return  "\r\nhand" +
                            "\r\nThis command has two options." +
                            "\r\nThe first option displays the text in the hand window in the output window." +
                            "\r\nThe hand window contains the cards in your hand and your potions." +
                            "\r\nThe second option displays the info of a card in your hand in the output window." +
                            "\r\nThe format is" +
                            "\r\nhand [card number]" +
                            "\r\nhand can be shortened to h." +
                            "\r\nExample:" +
                            "\r\nh 1";
                case "exhaust":
                    return  "\r\nexhaust" +
                            "\r\nThis command displays your exhaust pile in Output." +
                            "\r\nNote that exhaust does not have a window as there are very few reasons to need to check the exhaust pile.";
                case "output":
                    return  "\r\noutput" +
                            "\r\nThis window displays output from various sources." +
                            "\r\nThis is the only window that cannot display its text to the output window." +
                            "\r\nIt also cannot be hidden.";
                case "log":
                    return  "\r\nlog" +
                            "\r\nThis window displays the log." +
                            "\r\nYou can also display the log to output with the log command." +
                            "\r\nThis log is meant to be a combat log and is still quite basic." +
                            "\r\nIt will track:" +
                            "\r\nChanges in health and powers for the player and monsters." +
                            "\r\nUsed cards and potions." +
                            "\r\nThe user's inputs.";
                case "save":
                case "load":
                    return  "\r\nsave/load" +
                            "\r\nThese commands let you save and load the output window for later viewing." +
                            "\r\nFormat is" +
                            "\r\nsave/load [anything no spaces]" +
                            "\r\nExample that saves the output of a map command:" +
                            "\r\nmap 6 4" +
                            "\r\nsave 6-4" +
                            "\r\nload 6-4";
                case "map":
                    return  "\r\nmap" +
                            "\r\nThe map window displays the map that the user can reach." +
                            "\r\nThe map command has several uses." +
                            "\r\nThe first is to simply display the text in the map window to the output window." +
                            "\r\nThe next is to inspect the map." +
                            "\r\nThis lets you set a destination and display a map that the user can reach and can reach the destination." +
                            "\r\nThis destination will also be tracked in the choice window when moving on the map." +
                            "\r\nOptionally you can set a different starting position." +
                            "\r\nIf you do so the destination will not be tracked in the choice window." +
                            "\r\nmap can be shortened to m" +
                            "\r\nFormat is:" +
                            "\r\nmap [start floor] [start x] [floor] [x]" +
                            "\r\nExamples:" +
                            "\r\nmap 6 4" +
                            "\r\nmap 3 1 6 4" +
                            "\r\nBoth examples had destination 6 4." +
                            "\r\nFor a different way to inspect the map check the path command.";
                case "path":
                    return  "\r\npath" +
                            "\r\nThe path command is closely related to the map command." +
                            "\r\nIt displays all of the unique paths from point a to point b and tallies the types of nodes on each path." +
                            "\r\nIf point a is not set if defaults to where the user is." +
                            "\r\nFormat is:" +
                            "\r\npath [a floor] [a x] [b floor] [b x]" +
                            "\r\nExamples:" +
                            "\r\npath 6 4" +
                            "\r\npath 3 1 6 4" +
                            "\r\nBoth examples had destination 6 4.";
                case "monster":
                    return  "\r\nmonster" +
                            "\r\nThis command displays the text in the monster window in the output window." +
                            "\r\nThe monster window display the info of all alive monsters." +
                            "\r\nYou can view what number monsters are assigned in this window.";
                case "orbs":
                    return  "\r\nevent" +
                            "\r\nThis command displays the text in the orbs window in the output window." +
                            "\r\nThis window displays the orbs the player has." +
                            "\r\nThis is usually exclusive to the class Defect." +
                            "\r\nYou can inspect an orb with the format:" +
                            "\r\norbs index" +
                            "\r\nExample:" +
                            "\r\norbs 1";
                case "player":
                    return  "\r\nplayer" +
                            "\r\nThis command displays the text in the player window in the output window." +
                            "\r\nThis window displays your character's info." +
                            "\r\nOut of combat it also displays your potions.";
                case "relic":
                    return  "\r\nrelic" +
                            "\r\nThis command has two options." +
                            "\r\nThe first option displays the text in the relic window in the output window." +
                            "\r\nThe relic window contains all the relics you own." +
                            "\r\nThey are ordered in reverse acquired order." +
                            "\r\nThe second option displays a relic's info in the inspect window." +
                            "\r\nThe format is" +
                            "\r\nrelic [relic number]" +
                            "\r\nExample:" +
                            "\r\nrelic 5";
            }
        }

        return "";

    }

    public String getTutorial(){
        return  "\r\ntutorial" +
                "\r\nThe goal of each run is complete 3 acts." +
                "\r\nEach act consists of 15 floors and a boss." +
                "\r\nThe map will mention which boss is at the end of the current act." +
                "\r\nYou may only proceed forward and you cannot change your path once you take it." +
                "\r\nEach floor consists of various rooms and each node is connected to rooms on the next floor." +
                "\r\nUse the map and path commands to inspect and understand the map." +
                "\r\nEach room has a type that is viewable from the map." +
                "\r\nMonster rooms contain combat against normal monsters." +
                "\r\nElite rooms contain combat against elite monsters." +
                "\r\nCampfire rooms contain a campfire which provides a list of services you can pick from." +
                "\r\nTreasure rooms contain a treasure chest." +
                "\r\nShop rooms contain a shop." +
                "\r\nUnknown rooms can contain any of the above and can contain an event." +
                "\r\nThroughout the game you will need to make choices." +
                "\r\nMany of these choices will appear in the Choices window." +
                "\r\nThese choices can appear as a numbered list or as whole words." +
                "\r\nTo pick a numbered choice input the number." +
                "\r\nWhole word choices require inputting the word." +
                "\r\nCombat uses card game mechanics." +
                "\r\nUse cards from your hand to defeat enemies." +
                "\r\nCards cost energy to play." +
                "\r\nAt the start of a new turn you draw a fresh hand and your energy is replenished." +
                "\r\nPLay defensive block spells to prevent incoming damage." +
                "\r\nBlock reduces incoming damage but wears off at the start of the next turn." +
                "\r\nDuring your turn you can observe the enemy's intent." +
                "\r\nView cards in your hand using the hand window or the hand command." +
                "\r\nPlaying cards requires inputting the card number and the target number." +
                "\r\nEnemies are all assigned a number that does not change." +
                "\r\nCard numbers are its position in your hand so it can change as you play cards." +
                "\r\nExamples:" +
                "\r\n0 0" +
                "\r\n1 4" +
                "\r\nYou do not need to input a target if the card does not have a target or if there is only one enemy." +
                "\r\nPotions follow similar rules to cards." +
                "\r\nPotions have the options use, discard, and inspect." +
                "\r\nThe format to use them is:" +
                "\r\npot [u,d,i] [potion number] [target number]" +
                "\r\nAgain, target number is not needed if the potion has no target or if there is only one enemy." +
                "\r\nExamples:" +
                "\r\npot u 0 1" +
                "\r\npot d 1" +
                "\r\npot i 2" +
                "\r\npot u 3" +
                "\r\nThroughout the run you will be provided with various rewards." +
                "\r\nCard rewards add a card to your deck." +
                "\r\nDo note that you always use all the cards you have and ways to remove cards are rare." +
                "\r\nRelics are items with passive effects." +
                "\r\nYou can check and inspect relics you have in the relic window." +
                "\r\nYou can inspect choices too with the command:" +
                "\r\nc [number]" +
                "\r\nUse this to check out rewards before picking them." +
                "\r\nThis should be enough to get you started." +
                "\r\nBe sure to check out the rest of the commands in the help command.";
    }

    public void parseHistoryCommand(String[] tokens){
        if(tokens[0].equals("close")){
            choice.screen = Choices.HistoryScreen.NONE;
            return;
        }
        if(choice.screen == Choices.HistoryScreen.MAIN && tokens[0].equals("view")){
            filterList();
            choice.screen = Choices.HistoryScreen.LIST;
        }
        if(tokens[0].equals("back")){
            switch(choice.screen){
                case LIST:
                    choice.screen = Choices.HistoryScreen.MAIN;
                    break;
                case INSPECT:
                    choice.screen = Choices.HistoryScreen.LIST;
                    break;
                case DECK:
                case RELIC:
                case PATH:
                    choice.screen = Choices.HistoryScreen.INSPECT;
                    break;
            }
        }

        try{
            int in = Integer.parseInt(tokens[0]);

            switch(choice.screen){

                case MAIN:

                    switch (in) {
                        case 1:
                            include_win = !include_win;
                            break;
                        case 2:
                            include_lose = !include_lose;
                            break;
                        case 3:
                            include_iron = !include_iron;
                            break;
                        case 4:
                            include_silent = !include_silent;
                            break;
                        case 5:
                            include_defect = !include_defect;
                            break;
                        case 6:
                            include_watch = !include_watch;
                            break;
                        case 7:
                            include_normal = !include_normal;
                            break;
                        case 8:
                            include_asc = !include_asc;
                            break;
                        case 9:
                            include_daily = !include_daily;
                            break;
                        default:
                            return;
                    }
                    choice.savedFilter = "";
                    return;

                case LIST:

                    choice.inspectRun = runFiltered.get(in);
                    choice.screen = Choices.HistoryScreen.INSPECT;
                    return;

                case INSPECT:

                    switch (in){
                        case 1:
                            choice.screen = Choices.HistoryScreen.DECK;
                            break;
                        case 2:
                            choice.screen = Choices.HistoryScreen.RELIC;
                            break;
                        case 3:
                            choice.screen = Choices.HistoryScreen.PATH;
                            break;
                        case 4:
                            choice.includeGold = !choice.includeGold;
                            break;
                        case 5:
                            choice.includeHealth = !choice.includeHealth;
                            break;
                        case 6:
                            choice.includeCard = !choice.includeCard;
                            break;
                        case 7:
                            choice.includeRelics = !choice.includeRelics;
                            break;
                        case 8:
                            choice.includePotions = !choice.includePotions;
                            break;
                        case 9:
                            choice.includePurchases = !choice.includePurchases;
                            break;
                        case 10:
                            choice.includePurges = !choice.includePurges;
                            break;
                        case 11:
                            choice.includeEvents = !choice.includeEvents;
                            break;
                        case 12:
                            choice.includeBattles = !choice.includeBattles;
                            break;
                        case 13:
                            choice.includeCampfire = !choice.includeCampfire;
                            break;
                        case 14:
                            choice.includeBosses = !choice.includeBosses;
                            break;
                    }
            }

        }catch(Exception ignored){
        }

    }

    public void filterList(){
        runFiltered.clear();
        for(RunData d : runList){

            if(!include_win && d.victory){
                continue;
            }else if(!include_lose && !d.victory){
                continue;
            }else if(!include_iron && d.character_chosen.equals(AbstractPlayer.PlayerClass.IRONCLAD.name())){
                continue;
            }else if(!include_silent && d.character_chosen.equals(AbstractPlayer.PlayerClass.THE_SILENT.name())){
                continue;
            }else if(!include_defect && d.character_chosen.equals(AbstractPlayer.PlayerClass.DEFECT.name())){
                continue;
            }else if(!include_watch && d.character_chosen.equals(AbstractPlayer.PlayerClass.WATCHER.name())){
                continue;
            }else if(!include_normal && (!d.is_ascension_mode && !d.is_daily)){
                continue;
            }else if(!include_asc && d.is_ascension_mode){
                continue;
            }else if(!include_daily && d.is_daily){
                continue;
            }

            runFiltered.add(d);
        }
    }

    public void language(String[] tokens){
        StringBuilder s = new StringBuilder("\r\n");
        String[] langs = AbstractDungeon.settingsScreen.panel.languageLabels();
        if(tokens.length == 1){

            int index = -1;
            for(int i=0;i<langs.length;i++){
                Settings.GameLanguage[] languageOptions = AbstractDungeon.settingsScreen.panel.LanguageOptions();
                if(Settings.language == languageOptions[i]){
                    index = i;
                }
            }

            if(index == -1)
                return;

            s.append("Current ").append(langs[index]).append("\r\n");
            for(int i=0;i<langs.length;i++){
                s.append(i).append(": ").append(langs[i]).append("\r\n");
            }
            inspect.setText(s.toString());
        }else{
            try{
                int in = Integer.parseInt(tokens[1]);

                for(int i=0;i<langs.length;i++){
                    Settings.GameLanguage[] languageOptions = AbstractDungeon.settingsScreen.panel.LanguageOptions();
                    if(Settings.language == languageOptions[i] && i == in){
                        return;
                    }
                }

                Settings.setLanguage(AbstractDungeon.settingsScreen.panel.LanguageOptions()[in], false);
                Settings.gamePref.flush();
                AbstractDungeon.settingsScreen.panel.displayRestartRequiredText();

                inspect.setText("\r\nLanguage set to " + langs[in] + "\r\nOnly game text will change.\r\nThe mod's text and commands hasn't been translated yet and will remain English.\r\nRestart game to update display.");
            }catch(Exception ignored){
            }
        }
    }

    public String getStats(){

        if(CardCrawlGame.characterManager == null){
            return "";
        }

        StringBuilder s = new StringBuilder("\r\n");

        StringBuilder charStats = new StringBuilder("");

        ArrayList<CharStat> allCharStats = new ArrayList<>();
        for (AbstractPlayer c : CardCrawlGame.characterManager.getAllCharacters()) {
            CharStat stat = c.getCharStat();
            allCharStats.add(stat);

            charStats.append(c.chosenClass.name()).append("\r\n");
            String info = (String)basemod.ReflectionHacks.getPrivate(stat, CharStat.class, "info");
            String info2 = (String)basemod.ReflectionHacks.getPrivate(stat, CharStat.class, "info2");
            charStats.append(Choices.stripColor(info)).append(Choices.stripColor(info2));

        }

        CharStat overall = new CharStat(allCharStats);
        s.append("Overall").append("\r\n");
        String info = (String)basemod.ReflectionHacks.getPrivate(overall, CharStat.class, "info");
        String info2 = (String)basemod.ReflectionHacks.getPrivate(overall, CharStat.class, "info2");
        s.append(Choices.stripColor(info)).append(Choices.stripColor(info2));
        s.append(charStats.toString());

        return s.toString();
    }

    public String compendium(String[] tokens){
        String s = "";

        if(tokens.length == 1){
            return  "\r\nCompendium" +
                    "\r\nCategories are:" +
                    "\r\ni for Ironclad" +
                    "\r\ns for Silent" +
                    "\r\nd for Defect" +
                    "\r\nw for Watcher" +
                    "\r\ncl for Colorless" +
                    "\r\nc for Curses" +
                    "\r\np for Potions" +
                    "\r\nr for Relics";
        } else if(tokens.length == 2){
            ArrayList<AbstractCard> list;
            ArrayList<AbstractCard> fullList;
            switch(tokens[1]){
                case "i":
                    fullList = CardLibrary.getCardList(CardLibrary.LibraryType.RED);
                    list = seenCardList(fullList);
                    s = "\r\nIronclad\r\nSeen " + list.size() + "/" + fullList.size() + "\r\n" + cardListString(list);
                    return s;
                case "s":
                    fullList = CardLibrary.getCardList(CardLibrary.LibraryType.GREEN);
                    list = seenCardList(fullList);
                    s = "\r\nSilent\r\nSeen " + list.size() + "/" + fullList.size() + "\r\n" + cardListString(list);
                    return s;
                case "d":
                    fullList = CardLibrary.getCardList(CardLibrary.LibraryType.BLUE);
                    list = seenCardList(fullList);
                    s = "\r\nDefect\r\nSeen " + list.size() + "/" + fullList.size() + "\r\n" + cardListString(list);
                    return s;
                case "w":
                    fullList = CardLibrary.getCardList(CardLibrary.LibraryType.PURPLE);
                    list = seenCardList(fullList);
                    s = "\r\nWatcher\r\nSeen " + list.size() + "/" + fullList.size() + "\r\n" + cardListString(list);
                    return s;
                case "cl":
                    fullList = CardLibrary.getCardList(CardLibrary.LibraryType.COLORLESS);
                    list = seenCardList(fullList);
                    s = "\r\nColorless\r\nSeen " + list.size() + "/" + fullList.size() + "\r\n" + cardListString(list);
                    return s;
                case "c":
                    fullList = CardLibrary.getCardList(CardLibrary.LibraryType.CURSE);
                    list = seenCardList(fullList);
                    s = "\r\nCurse\r\nSeen " + list.size() + "/" + fullList.size() + "\r\n" + cardListString(list);
                    return s;
                case "p":
                    ArrayList<AbstractPotion> potList = getPotList();
                    s = "\r\nPotions\r\n" + potListString(potList);
                    return s;
                case "r":
                    s =     "\r\nRelics" +
                            "\r\nCategories are" +
                            "\r\ni for Ironclad" +
                            "\r\ns for Silent" +
                            "\r\nd for Defect" +
                            "\r\nw for Watcher" +
                            "\r\nsh for Shared";
                    return s;
            }
        } else {
            if(tokens[1].equals("r")){
                s = parseRelicLibrary(tokens);
                return s;
            }
            ArrayList<AbstractCard> list;
            ArrayList<AbstractCard> fullList;
            try {
                int in = Integer.parseInt(tokens[2]);
                if(in < 0){
                    return "";
                }
                switch (tokens[1]) {
                    case "i":
                        fullList = CardLibrary.getCardList(CardLibrary.LibraryType.RED);
                        list = seenCardList(fullList);
                        if(in < list.size()){
                            return inspectCard(list.get(in));
                        }
                        break;
                    case "s":
                        fullList = CardLibrary.getCardList(CardLibrary.LibraryType.GREEN);
                        list = seenCardList(fullList);
                        if(in < list.size()){
                            return inspectCard(list.get(in));
                        }
                        break;
                    case "d":
                        fullList = CardLibrary.getCardList(CardLibrary.LibraryType.BLUE);
                        list = seenCardList(fullList);
                        if(in < list.size()){
                            return inspectCard(list.get(in));
                        }
                        break;
                    case "w":
                        fullList = CardLibrary.getCardList(CardLibrary.LibraryType.PURPLE);
                        list = seenCardList(fullList);
                        if(in < list.size()){
                            return inspectCard(list.get(in));
                        }
                        break;
                    case "cl":
                        fullList = CardLibrary.getCardList(CardLibrary.LibraryType.COLORLESS);
                        list = seenCardList(fullList);
                        if(in < list.size()){
                            return inspectCard(list.get(in));
                        }
                        break;
                    case "c":
                        fullList = CardLibrary.getCardList(CardLibrary.LibraryType.CURSE);
                        list = seenCardList(fullList);
                        if(in < list.size()){
                            return inspectCard(list.get(in));
                        }
                        break;
                    case "p":
                        ArrayList<AbstractPotion> potList = getPotList();
                        if(in < potList.size()){
                            return inspectPotion(potList.get(in));
                        }
                        break;
                }
            }catch(Exception ignored){
            }
        }
        return s;
    }

    public ArrayList<AbstractCard> seenCardList(ArrayList<AbstractCard> fullList){
        ArrayList<AbstractCard> list = new ArrayList<>();

        for(AbstractCard c : fullList){
            if(UnlockTracker.isCardSeen(c.cardID)){
                list.add(c);
            }
        }

        return list;
    }

    public String cardListString(ArrayList<AbstractCard> list){
        int count = 0;
        String s = "";
        for(AbstractCard c : list){
            s = s + count + ". " + c.name + "\r\n";
            count++;
        }
        return s;
    }

    public ArrayList<AbstractPotion> getPotList(){
        ArrayList<String> potList = PotionHelper.getPotions(null, true);
        ArrayList<AbstractPotion> list = new ArrayList<>();
        for(String s : potList){
            list.add(PotionHelper.getPotion(s));
        }
        return list;
    }

    public String potListString(ArrayList<AbstractPotion> list){
        int count = 0;
        String s = "";
        for(AbstractPotion p : list){
            s = s + count + ". " + p.name + "\r\n";
            count++;
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    public String parseRelicLibrary(String[] tokens){
        String s = "\r\nRelics\r\n";
        HashMap<String, AbstractRelic> list;
        switch (tokens[2]){
            case "i":
                s = s + "Ironclad\r\n";
                list = (HashMap<String, AbstractRelic>)basemod.ReflectionHacks.getPrivateStatic(RelicLibrary.class, "redRelics");
                break;
            case "s":
                s = s + "Silent\r\n";
                list = (HashMap<String, AbstractRelic>)basemod.ReflectionHacks.getPrivateStatic(RelicLibrary.class, "greenRelics");
                break;
            case "d":
                s = s + "Defect\r\n";
                list = (HashMap<String, AbstractRelic>)basemod.ReflectionHacks.getPrivateStatic(RelicLibrary.class, "blueRelics");
                break;
            case "w":
                s = s + "Watcher\r\n";
                list = (HashMap<String, AbstractRelic>)basemod.ReflectionHacks.getPrivateStatic(RelicLibrary.class, "purpleRelics");
                break;
            case "sh":
                s = s + "Shared\r\n";
                list = (HashMap<String, AbstractRelic>)basemod.ReflectionHacks.getPrivateStatic(RelicLibrary.class, "sharedRelics");
                break;
            default:
                return "";
        }
        ArrayList<AbstractRelic> relicList = new ArrayList<>(list.values());
        int total = relicList.size();
        int count = 0;

        String relicString = "";
        for(AbstractRelic r : relicList){
            if(UnlockTracker.isRelicSeen(r.relicId)){
                relicString = relicString + count + ". " + r.name + "\r\n";
                count++;
            }
        }

        if(tokens.length == 3){
            s = s + "Seen " + count + "/" + total + "\r\n" + relicString;
            return s;
        }else{
            try{
                int in = Integer.parseInt(tokens[3]);
                if(in >= 0 && in < relicList.size()){
                    return inspectRelic(relicList.get(in));
                }
            }catch(Exception ignored){
            }
        }

        return s;
    }

    public String inspectPower(AbstractPower p){
        String s = "\r\n";

        s += "Power\r\n";
        s += p.name + "\r\n";
        s += Choices.stripColor(p.description) + "\r\n";

        return s;

    }

    public String inspectPotion(AbstractPotion p){

        String s = "\r\n";

        s += "Potion\r\n";
        s += p.name + "\r\n";
        s += p.rarity.name() + "\r\n";
        s += Choices.stripColor(p.description) + "\r\n";

        return s;

    }

    public static String inspectRelic(AbstractRelic r){

        String s = "\r\n";

        s += "Relic\r\n";
        s += r.name + "\r\n";
        s += r.tier.name() + "\r\n";
        s += "Charges " + r.counter + "\r\n";
        s += Choices.stripColor(r.description) + "\r\n";

        return s;

    }

    public static String inspectBlight(AbstractBlight r){

        String s = "\r\n";

        s += "Blight\r\n";
        s += r.name + "\r\n";
        s += "Charges " + r.counter + "\r\n";
        s += Choices.stripColor(r.description) + "\r\n";

        return s;

    }

    public static String inspectOrb(AbstractOrb o){
        String s = "\r\n";

        s = s + o.name + "\r\n";
        s = s + "Passive " + o.passiveAmount + "\r\n";
        s = s + "Evoke " + o.evokeAmount + "\r\n";

        return s;
    }

    public static String inspectCard(AbstractCard card){

        String s = "\r\n";

        int cost = Hand.handCost(card);

        s += card.name + "\r\n";
        s += card.color.name() + "\r\n";
        s += card.rarity.name() + "\r\n";

        if(cost == -1)
            s += "Cost : X"+ "\r\n";
        else if(cost != -2)
            s += "Cost : " + cost + "\r\n";

        s += "Type : " + card.type.toString() + "\r\n";

        s += cardText(card) + "\r\n";

        return s;

    }

    public static String cardText(AbstractCard c){

        String s = Choices.stripColor(c.rawDescription);

        s = s.replace("NL", " ");

        if(AbstractDungeon.getCurrRoom().phase != AbstractRoom.RoomPhase.COMBAT) {
            s = s.replace("!M!", "" + c.baseMagicNumber);
            s = s.replace("!D!", "" + c.baseDamage);
            s = s.replace("!B!", "" + c.baseBlock);
        } else {
            s = s.replace("!D!", "" + c.damage);
            s = s.replace("!B!", "" + c.block);
            if(c.magicNumber <= 0){
                s = s.replace("!M!", "" + c.baseMagicNumber);
            }else{
                s = s.replace("!M!", "" + c.magicNumber);
            }
        }

        if(stslib){
            if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                s = s.replace("!stslib:ex!", "" + (Integer) ExhaustiveField.ExhaustiveFields.exhaustive.get(c));
                s = s.replace("!stslib:refund!", "" + (Integer) RefundFields.refund.get(c));
            }else{
                s = s.replace("!stslib:ex!", "" + (Integer) ExhaustiveField.ExhaustiveFields.baseExhaustive.get(c));
                s = s.replace("!stslib:refund!", "" + (Integer) RefundFields.baseRefund.get(c));
            }
        }
        if(beaked){
            s = s.replace("!beaked:wI!", "" + c.misc);
            s = s.replace("!beaked:I!", "" + c.misc);
            if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT){
                s = s.replace("!beaked:B+M!", "" + c.block + c.magicNumber);
                s = s.replace("!beaked:wD!", "" + c.damage);
                s = s.replace("!beaked:wB!", "" + c.block);
            }else{
                s = s.replace("!beaked:B+M!", "" + c.baseBlock + c.baseMagicNumber);
                s = s.replace("!beaked:wD!", "" + c.baseDamage);
                s = s.replace("!beaked:wB!", "" + c.baseBlock);
            }
        }

        return s;

    }

    public static boolean characterUnlocked(String p){

        switch(p){
            case "the_silent" :
            case "silent" :
                return !UnlockTracker.isCharacterLocked("The Silent");
            case "defect" :
                return !UnlockTracker.isCharacterLocked("Defect");
            case "watcher" :
                return !UnlockTracker.isCharacterLocked("Watcher");
            default:
                return true;
        }

    }

    public static int ascensionLevel(AbstractPlayer.PlayerClass p){

        if(CardCrawlGame.characterManager.getCharacter(p) == null || CardCrawlGame.characterManager.getCharacter(p).getPrefs() == null){
            return maxAsc;
        }

        int asc = CardCrawlGame.characterManager.getCharacter(p).getPrefs().getInteger("ASCENSION_LEVEL", 1);
        if(asc > maxAsc)
            return maxAsc;
        return asc;
    }

    public boolean isUnlocked(String[] tokens){

        String character = tokens[1].toLowerCase();
        AbstractPlayer.PlayerClass pClass = null;

        for (AbstractPlayer.PlayerClass p : AbstractPlayer.PlayerClass.values()) {
            if(character.equals(p.name().toLowerCase())) {
                pClass = p;
                break;
            }
        }
        if(character.equals("silent"))
            pClass = AbstractPlayer.PlayerClass.THE_SILENT;
        if(pClass == null)
            return false;

        if(characterUnlocked(character)){
            try{
                if(tokens.length == 2 || Integer.parseInt(tokens[2]) <= ascensionLevel(pClass))
                    return true;
            }catch (Exception ignored){
            }
        }
        return false;

    }

    public int singleMonster() {
        if(AbstractDungeon.getCurrRoom().phase != AbstractRoom.RoomPhase.COMBAT)
            return  -1;
        int count = 0;
        int numAliveMonsters = 0;
        int index = -1;
        for (AbstractMonster m : AbstractDungeon.getCurrRoom().monsters.monsters) {
            if (m.currentHealth > 0) {
                if (numAliveMonsters == 0) {
                    index = count;
                    numAliveMonsters++;
                } else {
                    return -1;
                }
            }
            count++;
        }
        return index;
    }

    public static String inspectDaily(){
        StringBuilder s = new StringBuilder("Daily Climb\r\n");
        if(CardCrawlGame.mainMenuScreen.dailyScreen.todaysChar != null) {
            s.append(CardCrawlGame.mainMenuScreen.dailyScreen.todaysChar.getClass().getSimpleName());
            for (AbstractDailyMod m : ModHelper.enabledMods) {
                s.append("\r\n").append(m.modID).append("\r\n").append(m.description);
            }
        }
        return s.toString();
    }

    public String inspectAchievements(String[] tokens){
        StringBuilder s = new StringBuilder("\r\nAchievements");



        if(tokens.length < 2 && StatsScreen.achievements != null){

            ArrayList<String> locked = new ArrayList<>();
            ArrayList<String> unlocked = new ArrayList<>();

            for(int i = 0; i < StatsScreen.achievements.items.size() ; i ++ ){
                if(StatsScreen.achievements.items.get(i).isUnlocked){
                    unlocked.add("" + i + " : " + StatsScreen.achievements.items.get(i).key);
                }else{
                    locked.add("" + i + " : " + StatsScreen.achievements.items.get(i).key);
                }
            }

            s.append("\r\nUnlocked:");
            for(String un : unlocked){
                s.append("\r\n").append(un);
            }
            s.append("\r\nLocked:");
            for(String l : locked){
                s.append("\r\n").append(l);
            }

        }else{

            try{
                int in = Integer.parseInt(tokens[1]);
                if(in >= 0 && in < StatsScreen.achievements.items.size()){
                    AchievementItem item = StatsScreen.achievements.items.get(in);
                    s.append("\r\n").append(item.key).append("\r\n");
                    s.append((String)basemod.ReflectionHacks.getPrivate(item, AchievementItem.class, "desc"));
                }
            }catch (Exception ignored){
            }

        }

        return s.toString();
    }

    //Update displays every 30 update cycles
    @Override
    public void receivePostUpdate() {

        logs.update();

        if(iter < 30){
            iter++;
            return;
        }
        iter = 0;

        if(player == null)
            return;


        deck.update();
        discard.update();
        hand.update();
        monster.update();
        player.update();
        relic.update();
        map.update();
        choice.update();
        orbs.update();
        event.update();
        custom.update();

        if(!setSettings){

            Settings.soundPref.putBoolean("Mute in Bg", false);
            Settings.soundPref.flush();

            Settings.gamePref.putBoolean("Fast Mode", true);
            Settings.gamePref.putBoolean("Hand Confirmation", true);
            Settings.gamePref.flush();

            setSettings = true;

            Settings.FAST_MODE = Settings.gamePref.getBoolean("Fast Mode", false);
            Settings.FAST_HAND_CONF = Settings.gamePref.getBoolean("Hand Confirmation", false);
            CardCrawlGame.MUTE_IF_BG = Settings.soundPref.getBoolean("Mute in Bg", true);

        }

    }

    public void dispose(){
        Display.getDefault().dispose();
    }

    @Override
    public void receivePostPowerApplySubscriber(AbstractPower p, AbstractCreature target, AbstractCreature source) {
        logs.output("Apply " + p.name + " " + p.amount + " to " + target.name);
    }

    @Override
    public void receiveCardUsed(AbstractCard abstractCard) {
        logs.output("Played " + abstractCard.name);
    }

    @Override
    public void receivePrePotionUse(AbstractPotion abstractPotion) {
        logs.output("Used " + abstractPotion.name);
    }

    @Override
    public void receivePostDraw(AbstractCard abstractCard) {
        logs.output("Draw " + abstractCard.name);
    }

    @Override
    public void receivePostExhaust(AbstractCard abstractCard) {
        logs.output("Exhaust " + abstractCard.name);
    }
}



















