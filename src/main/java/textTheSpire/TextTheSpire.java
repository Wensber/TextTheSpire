package textTheSpire;

import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;

import basemod.BaseMod;
import basemod.interfaces.PostUpdateSubscriber;
import basemod.interfaces.PreUpdateSubscriber;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.characters.CharacterManager;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.daily.DailyScreen;
import com.megacrit.cardcrawl.daily.mods.AbstractDailyMod;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.GremlinMatchGame;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rewards.chests.AbstractChest;
import com.megacrit.cardcrawl.rooms.*;
import com.megacrit.cardcrawl.screens.custom.CustomMod;
import com.megacrit.cardcrawl.screens.custom.CustomModeScreen;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;

import com.megacrit.cardcrawl.screens.mainMenu.MenuButton;
import com.megacrit.cardcrawl.screens.mainMenu.MenuCancelButton;
import com.megacrit.cardcrawl.screens.options.OptionsPanel;
import com.megacrit.cardcrawl.screens.options.Slider;
import com.megacrit.cardcrawl.screens.select.BossRelicSelectScreen;
import com.megacrit.cardcrawl.screens.stats.AchievementItem;
import com.megacrit.cardcrawl.screens.stats.CharStat;
import com.megacrit.cardcrawl.screens.stats.StatsScreen;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import com.megacrit.cardcrawl.ui.buttons.CancelButton;
import com.megacrit.cardcrawl.ui.buttons.ConfirmButton;
import com.megacrit.cardcrawl.ui.buttons.GridSelectConfirmButton;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import communicationmod.ChoiceScreenUtils;
import communicationmod.CommandExecutor;
import communicationmod.patches.GremlinMatchGamePatch;
import org.eclipse.swt.widgets.Display;

import javax.smartcardio.Card;
import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;


@SpireInitializer
public class TextTheSpire implements PostUpdateSubscriber, PreUpdateSubscriber{

    //Used to only update display every number of update cycles
    int iter;
    int choiceTimeout;

    private boolean setSettings = false;
    boolean slotOnlyOnce = true;

    boolean enterDaily = false;

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

    private Inspect inspect;

    private HashMap<String, String> savedOutput;

    private JTextField promptFrame;

    private static String queuedCommand = "";
    private static boolean hasQueuedCommand = false;

    public TextTheSpire() {


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
            choiceTimeout = 50;
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
        switch(input){
            case "quit":
                dispose();
                Gdx.app.exit();
                return;
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

        if(CardCrawlGame.mode == CardCrawlGame.GameMode.CHAR_SELECT && !CommandExecutor.isInDungeon() && CardCrawlGame.mainMenuScreen.buttons.get(CardCrawlGame.mainMenuScreen.buttons.size()-2).result == MenuButton.ClickResult.ABANDON_RUN && input.equals("abandon")){
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

        AbstractDungeon d = CardCrawlGame.dungeon;
        String[] tokens = input.split("\\s+");

        if(tokens[0].equals("help")){
            inspect.setText(displayHelp(tokens));
            return;
        }

        if(input.equals("stats") && CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.statsScreen.statScreenUnlocked()){
            inspect.setText(getStats());
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

        if(tokens[0].equals("volume")){
            if(tokens.length == 1) {

                String s = "\r\nVolume\r\n";
                s += "Master " + Settings.MASTER_VOLUME + "\r\n";
                s += "Music " + Settings.MUSIC_VOLUME + "\r\n";
                s += "Sound " + Settings.SOUND_VOLUME;

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
            }
        }

        if(input.equals("daily") && CardCrawlGame.mainMenuScreen != null && !CardCrawlGame.characterManager.anySaveFileExists() && CardCrawlGame.mainMenuScreen.statsScreen.statScreenUnlocked()){
            CardCrawlGame.mainMenuScreen.dailyScreen.open();
            return;
        }

        if(input.equals("custom")){
            if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen != MainMenuScreen.CurScreen.CUSTOM && !CardCrawlGame.characterManager.anySaveFileExists() && StatsScreen.all.highestDaily > 0) {
                CardCrawlGame.mainMenuScreen.customModeScreen.open();
                return;
            }else{
                inspect.setText(custom.getText());
            }
        }

        if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.DAILY && !CommandExecutor.isInDungeon()){

            if(input.equals("embark")){
                ConfirmButton c = (ConfirmButton) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.dailyScreen, DailyScreen.class, "confirmButton");
                c.hb.clicked = true;
                return;
            }

        }

        if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.CUSTOM && !CommandExecutor.isInDungeon()){

            if(input.equals("embark")){
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
                    return;
            }

        }

        //Start a new run. Only does anything if not in dungeon.

        if (tokens[0].equals("start") && CardCrawlGame.mainMenuScreen != null && !CommandExecutor.isInDungeon() && CardCrawlGame.mainMenuScreen.buttons.get(CardCrawlGame.mainMenuScreen.buttons.size()-1).result == MenuButton.ClickResult.PLAY) {
            try {
                if (CardCrawlGame.mode == CardCrawlGame.GameMode.CHAR_SELECT && isUnlocked(tokens))
                    CommandExecutor.executeCommand(input);
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
        }

        //Potion Command. If out of combat can only discard
        if (tokens[0].equals("potion") || tokens[0].equals("pot")) {
            try {
                if(tokens.length >= 3 && tokens[1].equals("inspect") || tokens.length >= 3 && tokens[1].equals("i")){

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
                    System.out.println(command);
                    CommandExecutor.executeCommand(command.toString());
                }
                return;
            } catch (Exception e) {
                return;
            }
        }

        if (tokens[0].equals("choice") || tokens[0].equals("c")) {
            executeChoice(tokens);
        }

        if (tokens[0].equals("relic")) {
            try {
                if(tokens.length >= 2) {
                    int in = Integer.parseInt(tokens[1]);
                    if(in >= 0 && in < AbstractDungeon.player.relics.size()){

                        AbstractRelic r = AbstractDungeon.player.relics.get(in);

                        inspect.setText(inspectRelic(r));

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
                case "play":
                    try {
                        if(tokens.length == 2){
                            int index = singleMonster();
                            if(index != -1){
                                input = input + " " + index;
                            }
                        }
                        CommandExecutor.executeCommand(input);
                    } catch (Exception e) {
                        return;
                    }
                    return;
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
                default:
                    try {
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
                        }
                        int in;
                        in = Integer.parseInt(input) - 1;
                        ChoiceScreenUtils.executeChoice(in);
                    } catch (Exception e) {
                        return;
                    }
                    return;
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

                ChoiceScreenUtils.executeChoice(in);
            } catch (Exception ignored) {
            }
        }
    }

    /*
    private static void executeStartCommand(String[] tokens){
        if (tokens.length < 2) {
            return;
        }
        int ascensionLevel = 0;
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
            return;
        }
        if(tokens.length >= 3) {
            try {
                ascensionLevel = Integer.parseInt(tokens[2]);
            } catch (NumberFormatException e) {
                return;
            }
            if(ascensionLevel < 0 || ascensionLevel > 20) {
                return;
            }
        }

        Settings.seed = SeedHelper.generateUnoffensiveSeed(new Random(System.nanoTime()));;
        Settings.seedSet = false;
        Settings.isTrial = false;
        Settings.isDailyRun = false;
        AbstractDungeon.generateSeeds();
        AbstractDungeon.ascensionLevel = ascensionLevel;
        AbstractDungeon.isAscensionMode = ascensionLevel > 0;
        CardCrawlGame.startOver = true;
        CardCrawlGame.mainMenuScreen.isFadingOut = true;
        CardCrawlGame.mainMenuScreen.fadeOutMusic();
        CharacterManager manager = new CharacterManager();
        manager.setChosenCharacter(selectedClass);
        CardCrawlGame.chosenCharacter = selectedClass;
        GameStateListener.resetStateVariables();
    }*/

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

    public String displayHelp(String[] tokens){

        if(tokens.length == 1){
            return  "\r\nHere are a list of commands you can perform." +
                    "\r\nFor more details enter help followed by a command." +
                    "\r\nExample:" +
                    "\r\nhelp start" +
                    "\r\nAll commands are input in the prompt window." +
                    "\r\nstart" +
                    "\r\nabandon" +
                    "\r\ncontinue" +
                    "\r\nquit" +
                    "\r\nseed" +
                    "\r\bstats" +
                    "\r\nvolume" +
                    "\r\nachieve" +
                    "\r\nplay" +
                    "\r\ncustom" +
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
                    "\r\nOutput" +
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
                case "quit":
                    return  "\r\nquit" +
                            "\r\nThis command quits the game." +
                            "\r\nYou will still need to close the mod the spire window." +
                            "\r\nClosing the mod the spire window will close all the other mod windows.";
                case "seed":
                    return  "\r\nseed" +
                            "\r\nThis command displays the run's seed to the output window." +
                            "\r\nA seed is used for random number generation." +
                            "\r\nIt can be input when starting a run to have a set seed,";
                case "stats":
                    return  "\r\nstats" +
                            "\r\nThis command displays your save file's statistics to output." +
                            "\r\nWill not work on a fresh save file.";
                case "volume":
                    return  "\r\nvolume" +
                            "\r\nThis displays the current volume settings to output." +
                            "\r\nIt can also change the settings." +
                            "\r\nThe different volume types are master, music, and sound effects." +
                            "\r\nThe format is" +
                            "\r\nvolume master/music/sound number" +
                            "\r\nThe number needs to be a decimal between 0 and 1." +
                            "\r\nExample:" +
                            "\r\nvolume master 0.5";
                case "achieve":
                    return  "\r\nachieve" +
                            "\r\nThis displays to output the list of locked and unlocked achievements." +
                            "\r\nachieve number displays the description of a given achievement." +
                            "\r\nachieve can be shortened to a.";
                case "play":
                    return  "\r\nplay" +
                            "\r\nThis command lets you play cards from your hand." +
                            "\r\nThe format is" +
                            "\r\nplay [card number] [enemy number]" +
                            "\r\nEnemy number is optional for cards without targets or if there is only 1 target." +
                            "\r\nNote that the card number is the index in your hand." +
                            "\r\nIt changes as cards are played." +
                            "\r\nEnemy number does not change." +
                            "\r\nIf there are no active choices then you can skip play and just use the numbers.";
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
                case "output":
                    return  "\r\noutput" +
                            "\r\nThis window displays output from various sources." +
                            "\r\nThis is the only window that cannot display its text to the output window." +
                            "\r\nIt also cannot be hidden.";
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
                            "\r\nThis is usually exclusive to the class Defect.";
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

    public void language(String[] tokens){
        StringBuilder s = new StringBuilder("\r\n");
        String[] langs = {
                "English",
                "Brazilian Portuguese",
                "Chinese (Simplified)",
                "Chinese (Traditional)",
                "French",
                "German",
                "Greek",
                "Italian",
                "Indonesian",
                "Japanese",
                "Korean",
                "Norwegian",
                "Polish",
                "Russian",
                "Spanish",
                "Serbian-Cyrillic",
                "Serbian-Latin",
                "Thai",
                "Turkish",
                "Ukrainian",
                "Vietnamese"};
        if(tokens.length == 1){
            s.append("Current ").append(Settings.gamePref.getString("LANGUAGE")).append("\r\n");
            for(int i=0;i<langs.length;i++){
                s.append(i).append(": ").append(langs[i]).append("\r\n");
            }
            inspect.setText(s.toString());
        }else{
            try{
                int in = Integer.parseInt(tokens[1]);
                Settings.setLanguageLegacy(langs[in], false);
                inspect.setText("\r\nLanguage set to " + langs[in]);
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

    public static String inspectCard(AbstractCard card){

        String s = "\r\n";

        int cost = Hand.handCost(card);

        s += card.name + "\r\n";

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

        if(c.magicNumber < 0)
            s = s.replace("!M!", "" + c.baseMagicNumber);
        else
            s = s.replace("!M!", "" + c.magicNumber);

        if(c.damage < 0)
            s = s.replace("!D!", "" + c.baseDamage);
        else
            s = s.replace("!D!", "" + c.damage);

        if(c.block < 0)
            s = s.replace("!B!", "" + c.baseBlock);
        else
            s = s.replace("!B!", "" + c.block);

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

    public static int ascensionLevel(String p){

        int asc;

        switch(p){
            case "ironclad" :
                asc = CardCrawlGame.characterManager.getCharacter(AbstractPlayer.PlayerClass.IRONCLAD).getPrefs().getInteger("ASCENSION_LEVEL", 1);
                if(asc == 21)
                    return 20;
                return asc;
            case "the_silent" :
            case "silent" :
                asc = CardCrawlGame.characterManager.getCharacter(AbstractPlayer.PlayerClass.THE_SILENT).getPrefs().getInteger("ASCENSION_LEVEL", 1);
                if(asc == 21)
                    return 20;
                return asc;
            case "defect" :
                asc = CardCrawlGame.characterManager.getCharacter(AbstractPlayer.PlayerClass.DEFECT).getPrefs().getInteger("ASCENSION_LEVEL", 1);
                if(asc == 21)
                    return 20;
                return asc;
            case "watcher" :
                asc = CardCrawlGame.characterManager.getCharacter(AbstractPlayer.PlayerClass.WATCHER).getPrefs().getInteger("ASCENSION_LEVEL", 1);
                if(asc == 21)
                    return 20;
                return asc;
            default:
                return 20;
        }

    }

    public boolean isUnlocked(String[] tokens){

        String p = tokens[1].toLowerCase();

        if(characterUnlocked(p)){
            try{
                if(tokens.length == 2 || Integer.parseInt(tokens[2]) <= ascensionLevel(p))
                    return true;
            }catch (Exception ignored){
            }
        }
        return false;

    }

    public int singleMonster(){
        int count = 0;
        int numAliveMonsters = 0;
        int index = -1;
        for(AbstractMonster m : AbstractDungeon.getCurrRoom().monsters.monsters){
            if(m.currentHealth > 0){
                if(numAliveMonsters == 0){
                    index = count;
                    numAliveMonsters++;
                }else{
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

        specialUpdates();

        if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.SAVE_SLOT && slotOnlyOnce){
            slotOnlyOnce = false;
            if(CardCrawlGame.mainMenuScreen.saveSlotScreen.slots.get(2).emptySlot) {
                inspect.setText("\r\nYou are on the save slot screen.\r\nType a name into the Slay the Spire window and hit enter.");
                CardCrawlGame.mainMenuScreen.saveSlotScreen.openRenamePopup(2, true);
            }else{
                inspect.setText("\r\nSomething has gone wrong.\r\nYou are on the save select screen but the first slot has a save file.\r\nTry restarting the mod.");
            }

        }

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

    public void specialUpdates(){
        //AbstractDungeon.shrineList.remove("Match and Keep!");

    }

}



















