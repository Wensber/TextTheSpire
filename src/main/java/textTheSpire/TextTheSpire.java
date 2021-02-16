package textTheSpire;

import ascensionMod.AscensionMod;
import ascensionMod.UI.AscModScreen;
import ascensionMod.UI.CharSelectScreenUI;
import ascensionMod.UI.buttons.AscButton;
import automaton.cardmods.EncodeAndShuffleMod;
import automaton.cardmods.EncodeMod;
import automaton.cards.AbstractBronzeCard;
import basemod.CustomCharacterSelectScreen;
import basemod.ReflectionHacks;
import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.CardModifierManager;
import basemod.interfaces.*;
import champ.cards.AbstractChampCard;
import charbosses.bosses.AbstractCharBoss;
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
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rewards.chests.AbstractChest;
import com.megacrit.cardcrawl.rooms.*;
import com.megacrit.cardcrawl.screens.DeathScreen;
import com.megacrit.cardcrawl.screens.GameOverScreen;
import com.megacrit.cardcrawl.screens.charSelect.CharacterOption;
import com.megacrit.cardcrawl.screens.charSelect.CharacterSelectScreen;
import com.megacrit.cardcrawl.screens.custom.CustomMod;
import com.megacrit.cardcrawl.screens.custom.CustomModeScreen;
import com.megacrit.cardcrawl.screens.leaderboards.FilterButton;
import com.megacrit.cardcrawl.screens.leaderboards.LeaderboardScreen;
import com.megacrit.cardcrawl.screens.mainMenu.*;

import com.megacrit.cardcrawl.screens.runHistory.RunHistoryScreen;
import com.megacrit.cardcrawl.screens.select.BossRelicSelectScreen;
import com.megacrit.cardcrawl.screens.stats.AchievementItem;
import com.megacrit.cardcrawl.screens.stats.CharStat;
import com.megacrit.cardcrawl.screens.stats.RunData;
import com.megacrit.cardcrawl.screens.stats.StatsScreen;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import com.megacrit.cardcrawl.ui.buttons.*;
import com.megacrit.cardcrawl.ui.panels.DeleteSaveConfirmPopup;
import com.megacrit.cardcrawl.ui.panels.SeedPanel;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import communicationmod.ChoiceScreenUtils;
import communicationmod.CommandExecutor;
import communicationmod.GameStateListener;
import communicationmod.InvalidCommandException;
import communicationmod.patches.GremlinMatchGamePatch;
import conspire.events.MimicChestEvent;
import downfall.events.GremlinWheelGame_Evil;
import downfall.events.GremlinWheelGame_Rest;
import downfall.patches.EvilModeCharacterSelect;
import downfall.patches.MainMenuEvilMode;
import downfall.rooms.HeartShopRoom;
import downfall.util.HeartMerchant;
import guardian.cards.AbstractGuardianCard;
import org.eclipse.swt.widgets.Display;
import relicstats.HasCustomStats;
import relicstats.RelicStats;
import replayTheSpire.patches.ReplayShopInitCardsPatch;
import shopmod.relics.MerchantsRug;
import slimebound.cards.AbstractSlimeboundCard;
import slimebound.orbs.SpawnedSlime;
import sneckomod.cards.AbstractSneckoCard;
import theHexaghost.GhostflameHelper;
import theHexaghost.cards.AbstractHexaCard;
import theHexaghost.ghostflames.AbstractGhostflame;

import javax.swing.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.*;


@SpireInitializer
public class TextTheSpire implements PostUpdateSubscriber, PreUpdateSubscriber, PostPowerApplySubscriber, OnCardUseSubscriber, PrePotionUseSubscriber, PostDrawSubscriber, PostExhaustSubscriber {

    public final static String VERSION = "1.22";

    //Used to only update display every number of update cycles
    int iter;
    int choiceTimeout;

    private boolean setSettings = false;

    public static boolean replayTheSpire;
    public static boolean stslib;
    public static boolean ascensionReborn;
    public static int maxAsc;
    public static boolean beaked;
    public static boolean conspire;
    public static boolean shopMod;
    public static boolean downfall;
    public static boolean thorton;
    public static boolean relicStats;

    private Hand hand;
    private textTheSpire.Map map;
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

    private static JSONObject localization;
    private static JSONObject help;
    private static String tutorial;

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

    @SuppressWarnings("Duplicates")
    public TextTheSpire() {

        replayTheSpire = Loader.isModLoaded("ReplayTheSpireMod");
        stslib = Loader.isModLoaded("stslib");
        ascensionReborn = Loader.isModLoaded("ascensionmod");
        if(ascensionReborn)
            maxAsc = 25;
        else
            maxAsc = 20;
        beaked = Loader.isModLoaded("beakedthecultist-sts");
        conspire = Loader.isModLoaded("conspire");
        shopMod = Loader.isModLoaded("ShopMod");
        downfall = Loader.isModLoaded("downfall");
        thorton = Loader.isModLoaded("thorton");
        relicStats = Loader.isModLoaded("RelicStats");

        Thread ui = new Thread(() -> {
            Display display = new Display();

            hand = new Hand(display);
            map = new textTheSpire.Map(display);
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

        try{
            InputStream in = getClass().getResourceAsStream("/localization.json");
            localization = (JSONObject) new JSONParser().parse(new InputStreamReader(in, "UTF-8"));
        }catch (Exception e){
            System.out.println("Error " + e.getMessage());
        }

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
            runList = (ArrayList<RunData>) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.runHistoryScreen, RunHistoryScreen.class, "unfilteredRuns");
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

        if(input.equals("tts")){
            inspect.setText(ttsPatchNotes());
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
            } else if (downfall && input.equals("downfall")){
                MenuPanelScreen.PanelScreen ps = (MenuPanelScreen.PanelScreen) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.panelScreen, MenuPanelScreen.class, "screen");
                if(ps == MainMenuEvilMode.Enums.EVIL){
                    CardCrawlGame.mainMenuScreen.panelScreen.panels.get(1).hb.clicked = true;
                }
            } else if (input.equals("back")){
                CardCrawlGame.mainMenuScreen.panelScreen.button.hb.clicked = true;
                return;
            }
        }

        if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.CHAR_SELECT){
            ArrayList<CharacterOption> options = CardCrawlGame.mainMenuScreen.charSelectScreen.options;
            for(CharacterOption co : options){
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
                SeedPanel sp = (SeedPanel) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CharacterSelectScreen.class, "seedPanel");
                if(sp.shown){
                    Hitbox hb = (Hitbox) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CharacterSelectScreen.class, "seedHb");
                    hb.clicked = true;
                    inspect.setText("\r\nGo to main game window, paste the seed, then hit enter.\r\n");
                    return;
                }
            }
            if(CardCrawlGame.mainMenuScreen.charSelectScreen instanceof CustomCharacterSelectScreen){
                if(input.equals("left")){
                    int selectIndex = ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CustomCharacterSelectScreen.class, "selectIndex");
                    int maxSelectIndex = ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CustomCharacterSelectScreen.class, "maxSelectIndex");
                    if(selectIndex > maxSelectIndex){
                        ReflectionHacks.setPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CustomCharacterSelectScreen.class, "selectIndex", selectIndex - 1);
                        selectIndex--;
                        int optionsIndex = ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CustomCharacterSelectScreen.class, "optionsIndex");
                        int optionsPerIndex = ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CustomCharacterSelectScreen.class, "optionsPerIndex");
                        ReflectionHacks.setPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CustomCharacterSelectScreen.class, "optionsIndex", optionsPerIndex*selectIndex);
                        optionsIndex = optionsPerIndex*selectIndex;
                    }
                }
                if(input.equals("right")){

                }
            }
            boolean ready = (boolean) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CharacterSelectScreen.class, "anySelected");
            boolean asc = (boolean) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CharacterSelectScreen.class, "isAscensionModeUnlocked");
            if(tokens[0].equals("asc") && ready && asc){
                if(tokens.length == 1) {
                    Hitbox hb = (Hitbox) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CharacterSelectScreen.class, "ascensionModeHb");
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
                Hitbox hb = (Hitbox) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CharacterSelectScreen.class, "ascRightHb");
                hb.clicked = true;
                return;
            }
            if(input.equals("-") && ready){
                Hitbox hb = (Hitbox) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CharacterSelectScreen.class, "ascLeftHb");
                hb.clicked = true;
                return;
            }
            if(ascensionReborn){
                if(input.equals("c_asc") && ready){
                    Hitbox hb = (Hitbox) ReflectionHacks.getPrivateStatic(CharSelectScreenUI.class, "customAscensionModeHb");
                    hb.clicked = true;
                    return;
                }
                if(input.equals("open") && ready && AscensionMod.customAscensionRun){
                    AscButton ascb = (AscButton) ReflectionHacks.getPrivateStatic(CharSelectScreenUI.class, "openAscMenuButton");
                    ascb.pressed = true;
                    return;
                }
            }
        }

        if(ascensionReborn && CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == AscModScreen.Enum.ASC_MOD){
            if(input.equals("back")){
                MenuCancelButton mcb = (MenuCancelButton) ReflectionHacks.getPrivate(CharSelectScreenUI.ascScreen, AscModScreen.class, "button");
                mcb.hb.clicked = true;
                return;
            }
            try{
                int in = Integer.parseInt(input);
                if(in > 0){
                    in = in - 1;
                    if(in < CharSelectScreenUI.ascScreen.posAscButtons.size()){
                        CharSelectScreenUI.ascScreen.posAscButtons.get(in).toggledOn = !CharSelectScreenUI.ascScreen.posAscButtons.get(in).toggledOn;
                        return;
                    }
                }
                if(in < 0){
                    in = Math.abs(in) - 1;
                    if(in < CharSelectScreenUI.ascScreen.negAscButtons.size()){
                        CharSelectScreenUI.ascScreen.negAscButtons.get(in).toggledOn = !CharSelectScreenUI.ascScreen.negAscButtons.get(in).toggledOn;
                        return;
                    }
                }
            }catch (Exception ignored){
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
                    DeleteSaveConfirmPopup delete = (DeleteSaveConfirmPopup) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.saveSlotScreen, SaveSlotScreen.class, "deletePopup");
                    Hitbox hb = (Hitbox) ReflectionHacks.getPrivate(delete, DeleteSaveConfirmPopup.class, "yesHb");
                    hb.clicked = true;
                    return;
                }
                if(input.equals("no")){
                    DeleteSaveConfirmPopup delete = (DeleteSaveConfirmPopup) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.saveSlotScreen, SaveSlotScreen.class, "deletePopup");
                    Hitbox hb = (Hitbox) ReflectionHacks.getPrivate(delete, DeleteSaveConfirmPopup.class, "noHb");
                    hb.clicked = true;
                    return;
                }
            }

        }

        if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.DAILY && !CommandExecutor.isInDungeon()){

            if(input.equals("back")){
                MenuCancelButton c = (MenuCancelButton) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.dailyScreen, DailyScreen.class, "cancelButton");
                c.hb.clicked = true;
                return;
            }else if(input.equals("embark")){
                ConfirmButton c = (ConfirmButton) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.dailyScreen, DailyScreen.class, "confirmButton");
                c.hb.clicked = true;
                return;
            }else if(input.equals("mine")){
                Hitbox hb = (Hitbox) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.dailyScreen, DailyScreen.class, "viewMyScoreHb");
                hb.clicked = true;
                return;
            }else if(input.equals("prev")){
                Hitbox hb = (Hitbox) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.dailyScreen, DailyScreen.class, "prevDayHb");
                hb.clicked = true;
                return;
            }else if(input.equals("next")){
                long day = (long) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.dailyScreen, DailyScreen.class, "currentDay");
                if(day != 0L && day < TimeHelper.daySince1970()){
                    Hitbox hb = (Hitbox) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.dailyScreen, DailyScreen.class, "nextDayHb");
                    hb.clicked = true;
                    return;
                }
            }else if(input.equals("+")){
                if(CardCrawlGame.mainMenuScreen.dailyScreen.entries.size() == 20){
                    Hitbox hb = (Hitbox) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.dailyScreen, DailyScreen.class, "nextHb");
                    hb.clicked = true;
                    return;
                }
            }else if(input.equals("-")){
                if(CardCrawlGame.mainMenuScreen.dailyScreen.currentStartIndex != 1){
                    Hitbox hb = (Hitbox) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.dailyScreen, DailyScreen.class, "prevHb");
                    hb.clicked = true;
                    return;
                }
            }

        }

        if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.CUSTOM && !CommandExecutor.isInDungeon()){

            if(input.equals("back")){
                MenuCancelButton c = (MenuCancelButton) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.customModeScreen, CustomModeScreen.class, "cancelButton");
                c.hb.clicked = true;
                return;
            }else if(input.equals("embark")){
                GridSelectConfirmButton c = (GridSelectConfirmButton) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.customModeScreen, CustomModeScreen.class, "confirmButton");
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
                        Hitbox hb = (Hitbox) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.customModeScreen, CustomModeScreen.class, "ascensionModeHb");
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
                    Hitbox hb = (Hitbox) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.customModeScreen, CustomModeScreen.class, "seedHb");
                    inspect.setText("\r\nGo to main game window, paste the seed, then hit enter.\r\n");
                    hb.clicked = true;
                    return;
                case "mod":

                    if(tokens.length > 2 && tokens[1].equals("i")){
                        ArrayList<CustomMod> modList = (ArrayList<CustomMod>) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.customModeScreen, CustomModeScreen.class, "modList");
                        try{
                            int in = Integer.parseInt(tokens[2]);
                            String s = modList.get(in).name + "\r\n" + modList.get(in).description;
                            inspect.setText(s);
                        }catch(Exception ignored){
                        }
                    } else if(tokens.length > 1) {
                        ArrayList<CustomMod> modList = (ArrayList<CustomMod>) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.customModeScreen, CustomModeScreen.class, "modList");
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
                Hitbox hb = (Hitbox) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.leaderboardsScreen, LeaderboardScreen.class, "viewMyScoreHb");
                hb.clicked = true;
                return;
            }else if(input.equals("+")){
                if(CardCrawlGame.mainMenuScreen.leaderboardsScreen.entries.size() == 20){
                    Hitbox hb = (Hitbox) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.leaderboardsScreen, LeaderboardScreen.class, "nextHb");
                    hb.clicked = true;
                    return;
                }
            }else if(input.equals("-")){
                if(CardCrawlGame.mainMenuScreen.leaderboardsScreen.currentStartIndex != 1){
                    Hitbox hb = (Hitbox) ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.leaderboardsScreen, LeaderboardScreen.class, "prevHb");
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

                        inspect.setText(inspectPotion(p, in));

                    }
                    return;
                } else if(shopMod && MerchantsRug.isSelling() && tokens.length == 3 && tokens[1].equals("sell")){
                    int in =Integer.parseInt(tokens[2]);
                    if(in >= 0 && in < AbstractDungeon.player.potions.size()){
                        AbstractPotion p = AbstractDungeon.player.potions.get(in);
                        MerchantsRug.sell(in, p);
                    }
                } else {
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
                }else if(TextTheSpire.shopMod && MerchantsRug.isSelling() && tokens.length == 3 && tokens[2].equals("sell")){
                    int in = Integer.parseInt(tokens[1]);
                    if(in >= 0 && in < AbstractDungeon.player.relics.size()){
                        AbstractRelic r = AbstractDungeon.player.relics.get(in);
                        MerchantsRug.sell(r);
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
                ((ReturnToMenuButton)ReflectionHacks.getPrivate(AbstractDungeon.deathScreen, GameOverScreen.class, "returnButton")).hb.clicked = true;
                return;
            }
            if(AbstractDungeon.screen == AbstractDungeon.CurrentScreen.VICTORY){
                ((ReturnToMenuButton)ReflectionHacks.getPrivate(AbstractDungeon.victoryScreen, GameOverScreen.class, "returnButton")).hb.clicked = true;
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
                case "gf":
                    if(!TextTheSpire.downfall)
                        return;
                    try {
                        int in;
                        in = Integer.parseInt(tokens[1]);
                        ArrayList<AbstractGhostflame> gfl = GhostflameHelper.hexaGhostFlames;
                        if(in >= 0 && in < gfl.size()){
                            AbstractGhostflame gf = gfl.get(in);
                            inspect.setText("\r\n" + gf.getName() + "\r\n" + Choices.stripColor(gf.getDescription()));
                        }
                    } catch (Exception e) {
                        return;
                    }
                    return;
                case "boss":
                case "b":
                    if(TextTheSpire.downfall && AbstractDungeon.getCurrRoom().monsters.monsters.get(0) instanceof AbstractCharBoss && tokens.length >= 3){
                        inspect.setText(inspectDownfallBoss(tokens, AbstractDungeon.getCurrRoom().monsters.monsters.get(0)));
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
                            boolean show = (boolean) ReflectionHacks.getPrivateStatic(GenericEventDialog.class, "show");
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
                        if(ChoiceScreenUtils.getCurrentChoiceType() == ChoiceScreenUtils.ChoiceType.NONE || (downfall && EvilModeCharacterSelect.evilMode && AbstractDungeon.getCurrRoom() instanceof ShopRoom && ChoiceScreenUtils.getCurrentChoiceType() == ChoiceScreenUtils.ChoiceType.SHOP_ROOM)){
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

            if(downfall && EvilModeCharacterSelect.evilMode){
                inspect.setText(Inspect.downfallInspect(tokens));
            } else if(tokens.length >= 3){
                inspect.setText(Inspect.inspectMap(tokens));
            }

        }else if (tokens[0].equals("path")){

            if(downfall && EvilModeCharacterSelect.evilMode){
                inspect.setText(Inspect.downfallPaths(tokens));
            } else if(tokens.length >= 3){
                inspect.setText(Inspect.inspectPaths(tokens));
            }

        } else if (AbstractDungeon.getCurrRoom() instanceof TreasureRoomBoss && ChoiceScreenUtils.getCurrentChoiceType() == ChoiceScreenUtils.ChoiceType.CHEST) {
            AbstractChest chest = ((TreasureRoomBoss) AbstractDungeon.getCurrRoom()).chest;
            if(!chest.isOpen) {
                chest.isOpen = true;
                chest.open(false);
            }
        } else if(conspire && AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom().event instanceof MimicChestEvent) {
            if (input.equals("1")) {

                (AbstractDungeon.getCurrRoom()).phase = AbstractRoom.RoomPhase.INCOMPLETE;
                ReflectionHacks.setPrivate(AbstractDungeon.getCurrRoom().event, MimicChestEvent.class, "inFight", true);
                if (Settings.isDailyRun) {
                    AbstractDungeon.getCurrRoom().addGoldToRewards(AbstractDungeon.eventRng.random(30));
                } else {
                    AbstractDungeon.getCurrRoom().addGoldToRewards(AbstractDungeon.eventRng.random(25, 35));
                }
                AbstractDungeon.getCurrRoom().addRelicToRewards(AbstractDungeon.returnRandomRelicTier());
                (AbstractDungeon.getCurrRoom()).monsters = MonsterHelper.getEncounter("conspire:MimicChest");
                AbstractDungeon.getCurrRoom().event.enterCombat();
                AbstractDungeon.lastCombatMetricKey = "conspire:MimicChest";
            } else if (input.equals("proceed")) {
                Hitbox hb = (Hitbox) ReflectionHacks.getPrivate(AbstractDungeon.overlayMenu.proceedButton, ProceedButton.class, "hb");
                hb.clicked = true;
            }
        } else if(shopMod && ChoiceScreenUtils.getCurrentChoiceType() == ChoiceScreenUtils.ChoiceType.SHOP_SCREEN && MerchantsRug.forSale && input.equals("rug")){
            MerchantsRug.rugHb.clicked = true;
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

                if(downfall && EvilModeCharacterSelect.evilMode && ChoiceScreenUtils.getCurrentChoiceType() == ChoiceScreenUtils.ChoiceType.MAP){
                    textTheSpire.Map.downfallMapChoice(in);
                    return;
                }

                if(downfall && EvilModeCharacterSelect.evilMode && AbstractDungeon.getCurrRoom() instanceof HeartShopRoom && ((HeartShopRoom) AbstractDungeon.getCurrRoom()).heartMerchantShown && !AbstractDungeon.isScreenUp && in == 0){
                    AbstractDungeon.overlayMenu.proceedButton.setLabel(HeartMerchant.NAMES[0]);
                    basemod.ReflectionHacks.setPrivate(((HeartShopRoom) AbstractDungeon.getCurrRoom()).heartMerchant, HeartMerchant.class, "saidWelcome", true);
                    (AbstractDungeon.getCurrRoom()).rewards.clear();
                    (AbstractDungeon.getCurrRoom()).rewardAllowed = false;
                    AbstractDungeon.shopScreen.open();
                    ((HeartShopRoom) AbstractDungeon.getCurrRoom()).heartMerchant.hb.hovered = false;
                    return;
                }

                if(downfall && EvilModeCharacterSelect.evilMode && AbstractDungeon.getCurrRoom().event instanceof GremlinWheelGame_Evil){
                    ReflectionHacks.setPrivate(AbstractDungeon.getCurrRoom().event, GremlinWheelGame_Evil.class, "buttonPressed", true);
                    CardCrawlGame.sound.play("WHEEL");
                }

                if(downfall && EvilModeCharacterSelect.evilMode && AbstractDungeon.getCurrRoom().event instanceof GremlinWheelGame_Rest){
                    ReflectionHacks.setPrivate(AbstractDungeon.getCurrRoom().event, GremlinWheelGame_Rest.class, "buttonPressed", true);
                    CardCrawlGame.sound.play("WHEEL");
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

    public String ttsPatchNotes(){
        StringBuilder s = new StringBuilder("\r\n");

        s.append("Current Version : v" + VERSION + "\r\n");
        s.append("v1.22\r\nUpdated the support for Downfall to fix the character select issues and the card text for the characters Champ and Automaton.");
        s.append("v1.21\r\nFixed victory and death screen crash introduced with new Slay the Spire version.\r\nUnable to reproduce abandon crash.\r\n");
        s.append("v1.20\r\nAdded support for The Thorton. Business Cards, Investments, and Run info are displayed in the Player Window.\r\n" +
                "Added support for RelicStats.\r\n");
        s.append("v1.19\r\nFixed an issue where Choices did not list embark in character select if ascension mode isn't unlocked.\r\n" +
                "Cleaned up the help menu and moved it to a json file to accommodate potential translations.\r\n");
        s.append("v1.18\r\nFixed some bugs in Downfall's support.\r\n" +
                "Boss's hand, orbs, and relics can be inspected.\r\n" +
                "The Explore Events choice in Winding Halls event seems to be broken in Downfall and should be avoided.\r\n" +
                "Experimenting with support for different OS.\r\n" +
                "Created different steam pages for Linux, Mac, and Win 32 bit.\r\n" +
                "Due to not owning devices with those OS, have not tested if they work.\r\n");
        s.append("v1.17\r\nAdded support for Downfall.\r\n" +
                "Due to time limitations was not able to test everything.\r\n" +
                "Use the command mod for more info.\r\n" +
                "Added in game patch notes for Text The Spire which can be accesses with command tts.\r\n");
        s.append("v1.16\r\nAdded support for Shop Mod.\r\n" +
                "Added support for the Mimic chest event in Conspire.\r\n");
        s.append("v1.15\r\nModified the main menu support so that starting a run navigates the menu.\r\n" +
                "This change is to make it easier to support content mods with new gamemodes or character select options.\r\n" +
                "The old command to start runs still works but isn't guaranteed to work for mods.\r\n" +
                "Ascension Reborn support is fixed with the menu navigation.\r\n" +
                "Hotfix: Fixed an issue where it didn't check if Ascension Reborn was loaded before using its classes.\r\n");
        s.append("v1.14\r\nAdded support for Ascension Reborn.\r\n" +
                "Ascension unlocks should track properly for mod characters.\r\n" +
                "Crashes when running mods should be less common.\r\n" +
                "Hotfix: Tentative fix for crash when checking ascension unlock for certain mod characters. Will set to max ascension instead of crashing.\r\n" +
                "Hotfix: Fixed issue where you have to use the_silent instead of silent to start a run.\r\n");
        s.append("v1.13\r\nStSLib's Clickable relics are now supported.\r\n" +
                "Replay the Spire's 2 for 1 sale tag is supported.\r\n" +
                "Orb window how totals end of turn Lightning and Frost passives.\r\n" +
                "Reverted Event nodes to Unknown. Run History now mentions if a node was Unknown at first.\r\n" +
                "Hotfix: Fixed Two for One support not showing prices.\r\n" +
                "Hotfix. Fixed Two for One support preventing you from buying anything but cards.\r\n");
        s.append("v1.12\r\nMasterdeck is now numbered and can be inspected.\r\n" +
                "Exhaust command added to view exhaust pile. Exhausted cards are tracked by Log.\r\n" +
                "While making the map more flexible with custom rooms, unknown rooms were renamed to Event.\r\n" +
                "Began implementing Replay the Spire. Fadding Forest boss, map nodes, and new variables in card inspect should all work. Have not been able to test everything so cannot confirm that everything works.\r\n" +
                "Hotfix: Fixed an issue where you couldn't play targeted cards.\r\n" +
                "Hotfix: Fixed an issue where you couldn't discard potions when out of combat.\r\n");
        s.append("v1.11\r\nAdded leaderboard support for both Daily Climb and general leaderboards.\r\n" +
                "Added a new window called Log that is a basic combat log.\r\n" +
                "Added a patch command to access game patch notes from the main menu.\r\n" +
                "Added a tutorial command that outputs a tutorial I put together.\r\n" +
                "Added a background ambience sounds toggle to the volume command.\r\n");
        s.append("v1.10\r\nAdded language support with the lang command.\r\n" +
                "Fixed a bug with map and path commands where the game would crash when you input a destination lower than the source.\r\n" +
                "You no longer need to input a target for potions with one enemy.\r\n" +
                "Map stops tracking after you reach the floor you are tracking to.\r\n" +
                "Blights now appear in the Relic window.\r\n");
        s.append("v1.9\r\nAdded Run History support.\r\n" +
                "Death and Victory screens now have a score breakdown.");
        s.append("v1.8\r\nAdded compendium support with the comp command.\r\n" +
                "Added save slot support with the slot command.\r\n" +
                "The quit command now saves and quits to main menu when in a run.\r\n" +
                "You can now inspect orbs.\r\n");
        s.append("v1.7\r\nAdded stats command.\r\n" +
                "Replaced restart command with abandon command.\r\n" +
                "Inspects now display rarity.\r\n" +
                "Relic inspects display charges.\r\n" +
                "Added some commas into path command output.\r\n");
        s.append("v1.6\r\nMap command can now set a different source.\r\n" +
                "Added a path command can displays all unique paths from point a to point b and tallies the node types for each path.\r\n" +
                "Added examples to some of the help commands.\r\n");
        s.append("v1.5\r\nAdded custom game support.\r\n");
        s.append("v1.4\r\nAdded volume support.\r\n" +
                "Fixed some daily climb bugs.\r\n" +
                "Fixed a typo in the help command.\r\n");
        s.append("v1.3\r\nAdded achievement display.\r\n" +
                "Allows shortened versions of some commands.\r\n" +
                "Fixed a boss chest room bug.\r\n");
        s.append("v1.2\r\nDaily climb is now available, though the modifiers Draft and Sealed deck are currently not supported.\r\n" +
                "Also, the return button does not work and is disabled as I bypassed a bit of menuing.\r\n" +
                "Event dialogue has been fixed.\r\n" +
                "Unlocks are displayed.\r\n");
        s.append("v1.1\r\nFixed some issues found after making the mod public and added some quality of life changes.\r\n" +
                "Goblin Match game now displays cards picked to output.\r\n" +
                "You can save and load the output window for future viewing.\r\n" +
                "Upgrade preview is now displayed.\r\n" +
                "You can show and hide all windows besides prompt and output at once.\r\n" +
                "You can skip typing play during combat when there are no choices.\r\n");

        return  s.toString();
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
        if(shopMod){
            s.append("Shop Mod\r\n");
            s.append("This lets you buy the shop's rug and then sell your relics and potions.\r\n");
            s.append("If the rug is for sale and you can afford it, the rug will appear at the bottom of the shop screen.\r\n");
            s.append("You can sell relics and potions on the shop screen.\r\n");
            s.append("Check their prices with inspect while on the shpp screen.\r\n");
            s.append("Sell relics with the format \"relic [number] sell\"\r\n");
            s.append("Sell potions with the format \"pot sell [number]\"");
        }
        if(downfall){
            s.append("Downfall\r\n");
            s.append("This mod adds a new gamemode with 4 new characters.\r\n");
            s.append("You will be playing as a villian and descending down the spire rather than up.\r\n");
            s.append("The bosses are the original playable characters.\r\n");
            s.append("They use cards, energy, and relics.\r\n");
            s.append("Their hand is sorted in the order they are played.\r\n");
            s.append("You can inspect the boss's zones with the command:\r\n");
            s.append("b [h/o/r] [index]\r\n");
            s.append("h is hand, o is orb, and r is relic.\r\n");
            s.append("Slimbound:\r\n");
            s.append("Can split off slimes with effects.\r\n");
            s.append("These slimes use orb slots and can be inspected like a normal orb.\r\n");
            s.append("New keywords:\r\n");
            s.append("Goop : The next attack deals 1 more damage per Goop and triggering Consume effects.\r\n");
            s.append("Consume : Bonus effects that trigger when an attack consumes Goop.\r\n");
            s.append("Split : Lose 4 HP and spawn a slime.\r\nWhile the slime exists you lose 4 Max HP.\r\nIf you have no empty orb slots the leading slime is Absorbed.\r\n");
            s.append("Absorb : Recombine with your leading slime regaining the Max HP and gaining 1 STR.\r\n");
            s.append("Command : Your leading slime attacks.\r\n");
            s.append("Potency : Increases the effects of all your slimes.\r\n");
            s.append("Guardian:\r\n");
            s.append("Can put cards in stasis which uses orb slots and can be inspected like one.\r\n");
            s.append("Has a gem system which lets you add gem cards to sockets to combine their effects while at a bonfire.\r\n");
            s.append("This is a free action and lets you use another action.\r\n");
            s.append("New Keywords:\r\n");
            s.append("Defensive Mode : While in this stance you gain 3 thorns and gain 2 block when you play a card.\r\n");
            s.append("Socket : While at a bonfire you can place a gem into this socket to gain the gem's effects.\r\nThe gem is removed from the deck.\r\n");
            s.append("Gem : While at a bonfire you can place this gem into a socket to add this gem's effects to the card with the socket.\r\nThis gem is removed from the deck.\r\n");
            s.append("Stasis : Place a card into stasis with turn counters equal to card cost plus 1.\r\nTurn counters are reduced by 1 at start of turn.\r\nWhen turn counters reach 0 the card is added to your hand costing 0 until played.\r\n");
            s.append("Accelerate : Reduce the turn counters of the oldest stasis card by 1.\r\n");
            s.append("Tick : This effect is triggered when a turn counter is removed.\r\n");
            s.append("Volatile : Exhaust the card when it leaves stasis.\r\n");
            s.append("Hexaghost:\r\n");
            s.append("The Hexaghost has 6 Ghostflames.\r\n");
            s.append("One of them is active at all times.\r\n");
            s.append("They can be either ignited or extinguished starting as extinguished.\r\n");
            s.append("The active Ghostflame can become ignited by fulfilling a condition.\r\n");
            s.append("Igniting a Ghostflame triggers effects.\r\n");
            s.append("Effects can cause the active Ghostflame to rotate.\r\n");
            s.append("Inspect a Ghostflame with command \"gf [number]\"\r\n");
            s.append("New Keywords:\r\n");
            s.append("Advance : Activte the next Ghostflame.\r\nIf it is ignited extinguish it.\r\n");
            s.append("Retract : Activate the previous Ghostflam.\r\nIf it is ignited extinguish it.\r\n");
            s.append("Soulburn : After 3 turns deal damage equal to Soulburn value.\r\n");
            s.append("Force-Ignite : Trigger the Ignition effect of the active Ghostflame even if its already ignited.\r\n");
            s.append("Afterlife : If this card is exhausted, its effects activate.\r\n");
            s.append("Intensity : Increases the effects of Ghostflames.\r\n");
            s.append("Seal : Can't be Upgraded.\r\nIf you have all 6 seals active, remove the seals from your deck and obtain the Broken Seal.\r\n");
            s.append("Snecko:\r\n");
            s.append("New Keywords:\r\n");
            s.append("Unknown : Unknown cards transform into a random card from any class during combat.\r\nThe pool of cards selected from can have limitations added.\r\n");
            s.append("Muddle : Randomize a card's cost between 0 and 3.\r\n");
            s.append("Snekproof : Unaffected by Muddle or Confusion.\r\n");
        }
        if(thorton){
            s.append("Thorton\r\n");
            s.append("Character mod that adds The Thorton.\r\n");
            s.append("New Keywords:\r\n");
            s.append("Run : After playing a certain number of Run cards you win the combat with full rewards.\r\nThe number of times you need to play Run cards is listed in the Player window and increases each combat you win by running.\r\n");
        }
        return s.toString();
    }

    public String displayHelp(String[] tokens){

        if(tokens.length == 1){
            return (String)help.get("base");
        }else{
            if(tokens[1].equals("tutorial")){
                return getTutorial();
            }else if(help.containsKey(tokens[1])){
                return (String)help.get(tokens[1]);
            }else{
                return "";
            }
        }
    }

    public String getTutorial(){
        return tutorial;
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

    public String inspectDownfallBoss(String[] tokens, AbstractMonster mon){
        StringBuilder s = new StringBuilder("");

        if(!(downfall && mon instanceof AbstractCharBoss))
            return "";

        AbstractCharBoss m = (AbstractCharBoss)mon;

        try{
            int in = Integer.parseInt(tokens[2]);
            switch (tokens[1]){
                case "hand":
                case "h":
                    s.append(inspectCard(m.hand.group.get(in)));
                    break;
                case "orb":
                case "o":
                    s.append(inspectOrb(m.orbs.get(in)));
                    break;
                case "relic":
                case "r":
                    s.append(inspectRelic(m.relics.get(in)));
                    break;
            }
        }catch (Exception ignored){
        }

        return s.toString();
    }

    public String inspectPower(AbstractPower p){
        String s = "\r\n";

        s += "Power\r\n";
        s += p.name + "\r\n";
        s += Choices.stripColor(p.description) + "\r\n";

        return s;

    }

    public String inspectPotion(AbstractPotion p) {
        return inspectPotion(p, -1);
    }

    public String inspectPotion(AbstractPotion p, int slot){

        String s = "\r\n";
        s += "Potion\r\n";
        s += p.name + "\r\n";
        if(shopMod && MerchantsRug.isSelling() && slot != -1)
            s += "Sale Price : " + MerchantsRug.potionSalePrice(slot, p) + "\r\n";
        s += p.rarity.name() + "\r\n";
        s += Choices.stripColor(p.description) + "\r\n";

        return s;

    }

    @SuppressWarnings("unchecked")
    public static String inspectRelic(AbstractRelic r){

        String s = "\r\n";

        s += "Relic\r\n";
        s += r.name + "\r\n";
        if(shopMod && MerchantsRug.isSelling())
            s += "Sale Price : " + MerchantsRug.relicSalePrice(r) + "\r\n";
        s += r.tier.name() + "\r\n";
        s += "Charges " + r.counter + "\r\n";
        s += Choices.stripColor(r.description) + "\r\n";

        if(relicStats){
            HashMap<String, HasCustomStats> stats = (HashMap<String, HasCustomStats>)basemod.ReflectionHacks.getPrivateStatic(RelicStats.class, "statsInfoHashMap");
            if(stats.containsKey(r.relicId)){
                s += stats.get(r.relicId).getStatsDescription();
            }
        }

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

        if(downfall && o instanceof SpawnedSlime){

            if(((SpawnedSlime) o).customDescription != null)
                s = s + ((SpawnedSlime) o).customDescription + "\r\n";
            if(((SpawnedSlime) o).description != null)
                s = s + ((SpawnedSlime) o).description + "\r\n";
            s = Choices.stripColor(s);
            return s;
        }

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
        if(downfall){

            if(c instanceof AbstractSlimeboundCard) {
                if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                    s = s.replace("!SlimeboundSlimed!", "" + ((AbstractSlimeboundCard) c).slimed);
                    s = s.replace("!SlimeboundSelfharm!", "" + ((AbstractSlimeboundCard) c).selfDamage);
                }else{
                    s = s.replace("!SlimeboundSlimed!", "" + ((AbstractSlimeboundCard) c).baseSlimed);
                    s = s.replace("!SlimeboundSelfharm!", "" + ((AbstractSlimeboundCard) c).baseSelfDamage);
                }
            }
            if(c instanceof AbstractGuardianCard) {
                s = s.replace("!GuardianMulti!", "" + ((AbstractGuardianCard) c).multihit);
                s = s.replace("!GuardianSecondM!", "" + ((AbstractGuardianCard) c).secondaryM);
            }
            if(c instanceof AbstractHexaCard){
                if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                    s = s.replace("!burny!","" + ((AbstractHexaCard) c).burn);
                }else{
                    s = s.replace("!burny!","" + ((AbstractHexaCard) c).baseBurn);
                }
            }
            if(c instanceof AbstractSneckoCard){
                if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                    s = s.replace("!qqq!","" + ((AbstractSneckoCard) c).silly);
                }else {
                    s = s.replace("!qqq!", "" + ((AbstractSneckoCard) c).baseSilly);
                }
            }
            if(c instanceof AbstractChampCard){
                if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                    s = s.replace("!cool!","" + ((AbstractChampCard) c).cool);
                }else {
                    s = s.replace("!cool!", "" + ((AbstractChampCard) c).baseCool);
                }
            }
            if(c instanceof AbstractBronzeCard){
                for(AbstractCardModifier m : CardModifierManager.modifiers(c)){
                    if(m instanceof EncodeMod){
                        s = s + "\r\nEncode";
                    }
                    if(m instanceof EncodeAndShuffleMod){
                        s = s + "\r\nEncode, then add a copy of this into your discard pile.";
                    }
                }
                if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                    s = s.replace("!bauto!","" + ((AbstractBronzeCard) c).auto);
                }else {
                    s = s.replace("!bauto!", "" + ((AbstractBronzeCard) c).baseAuto);
                }
            }
            s = s.replaceAll("slimeboundmod:", "");
            s = s.replaceAll("guardianmod:", "");
            s = s.replaceAll("hexamod:", "");
            s = s.replaceAll("sneckomod:", "");
            s = s.replaceAll("champ:", "");
            s = s.replaceAll("bronze:", "");
        }
        if(thorton){
            s = s.replace("thethorton:", "");
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

        if(help == null && Settings.language != null){
            String current_lang = Settings.language.toString();
            JSONObject pickedLang;
            if(localization.containsKey(current_lang)){
                pickedLang = (JSONObject) localization.get(current_lang);
            } else {
                pickedLang = (JSONObject) localization.get("ENG");
            }
            help = (JSONObject) pickedLang.get("help");
            tutorial = (String) pickedLang.get("tutorial");
        }

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



















