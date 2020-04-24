package textTheSpire;

import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;

import basemod.BaseMod;
import basemod.interfaces.PostUpdateSubscriber;
import basemod.interfaces.PreUpdateSubscriber;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.red.Whirlwind;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.characters.CharacterManager;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.*;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;

import com.megacrit.cardcrawl.screens.mainMenu.MenuCancelButton;
import com.megacrit.cardcrawl.screens.select.BossRelicSelectScreen;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import com.megacrit.cardcrawl.ui.buttons.CancelButton;
import com.megacrit.cardcrawl.ui.buttons.ProceedButton;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import communicationmod.ChoiceScreenUtils;
import communicationmod.CommandExecutor;
import communicationmod.GameStateListener;
import communicationmod.InvalidCommandException;
import org.eclipse.swt.widgets.Display;

import javax.swing.*;
import java.lang.reflect.Array;
import java.util.ArrayList;


@SpireInitializer
public class TextTheSpire implements PostUpdateSubscriber, PreUpdateSubscriber{

    //Used to only update display every number of update cycles
    int iter;

    private boolean setSettings = false;

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

    private Inspect inspect;

    private JTextField promptFrame;

    private String queuedCommand = "";
    private boolean hasQueuedCommand = false;

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

            inspect = new Inspect(display);

            while(!display.isDisposed()){
                if(!display.readAndDispatch()){
                    display.sleep();
                }
            }
        });

        ui.start();

        iter = 0;
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
        if (hasQueuedCommand) {
            parsePrompt(queuedCommand);
            hasQueuedCommand = false;
        }
    }

    //Queues input for correct timing
    public void queueInput(String input){
        queuedCommand = input;
        hasQueuedCommand = true;
    }

    //TODO Create help commands
    //Parse a command to see if its an allowed command and send to CommunicationMod to execute
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

        AbstractDungeon d = CardCrawlGame.dungeon;
        String[] tokens = input.split("\\s+");

        if(tokens[0].equals("help")){
            inspect.setText(displayHelp(tokens));
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
            }

        }


        //Start a new run. Only does anything if not in dungeon.
        if (tokens[0].equals("start") && !CardCrawlGame.characterManager.anySaveFileExists()) {
            try {
                if (CardCrawlGame.mode == CardCrawlGame.GameMode.CHAR_SELECT && isUnlocked(tokens))
                    CommandExecutor.executeCommand(input);
                return;
            } catch (Exception e) {
                return;
            }
        } else if (tokens[0].equals("restart") && CardCrawlGame.characterManager.anySaveFileExists()) {
            try {
                if (CardCrawlGame.mode == CardCrawlGame.GameMode.CHAR_SELECT && isUnlocked(tokens)) {
                    //tokens = (input.substring(2)).split("\\s+");
                    CommandExecutor.executeCommand(input.substring(2));
                }
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
        if (tokens[0].equals("potion")) {
            try {
                if(tokens.length >= 3 && tokens[1].equals("inspect")){

                    int in = Integer.parseInt(tokens[2]);
                    if(in >= 0 && in < AbstractDungeon.player.potions.size()) {
                        AbstractPotion p = AbstractDungeon.player.potions.get(in);

                        inspect.setText(inspectPotion(p));

                    }
                    return;
                }else {
                    CommandExecutor.executeCommand(input);
                }
                return;
            } catch (Exception e) {
                return;
            }
        }

        if (tokens[0].equals("choice")) {
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
                        CommandExecutor.executeCommand(input);
                    } catch (Exception e) {
                        return;
                    }
                    break;
                case "end":
                    try {
                        CommandExecutor.executeCommand(tokens[0]);
                    } catch (Exception e) {
                        return;
                    }
                    break;
                case "hand": {
                    int in;
                    try {
                        in = Integer.parseInt(tokens[1]);
                        choiceCard(in, AbstractDungeon.player.hand.group);
                    } catch (Exception e) {
                        return;
                    }
                    break;
                }
                default: {
                    int in;
                    try {
                        in = Integer.parseInt(input) - 1;
                        ChoiceScreenUtils.executeChoice(in);
                    } catch (Exception e) {
                        return;
                    }
                    break;
                }
            }
        } else if (tokens[0].equals("map")){

            if(tokens.length >= 3){
                inspect.setText(Inspect.inspectMap(tokens));
            }

        } else {
            //Everything else is a choice screen command. Only a number is needed.
            int in;
            try {
                in = Integer.parseInt(input) - 1;
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

    public String displayHelp(String[] tokens){

        if(tokens.length == 1){
            return  "\r\nHere are a list of commands you can perform." +
                    "\r\nFor more details enter help followed by a command." +
                    "\r\nAll commands are input in the prompt window." +
                    "\r\nstart" +
                    "\r\nrestart" +
                    "\r\ncontinue" +
                    "\r\nquit" +
                    "\r\nseed" +
                    "\r\nplay" +
                    "\r\npotion" +
                    "\r\nchoice" +
                    "\r\nend" +
                    "\r\nshow" +
                    "\r\nhide" +
                    "\r\nChoices" +
                    "\r\nDeck" +
                    "\r\nDiscard" +
                    "\r\nEvent" +
                    "\r\nHand" +
                    "\r\nOutput" +
                    "\r\nMap" +
                    "\r\nMonster" +
                    "\r\nOrbs" +
                    "\r\nPlayer" +
                    "\r\nRelic";
        }else{
            switch(tokens[1].toLowerCase()){
                case "start":
                case "restart":
                case "continue":
                    return  "\r\nstart, restart, and continue" +
                            "\r\nThese commands let you enter a run. You see these options while on the start menu." +
                            "\r\nIf there is no save file, you can start a run." +
                            "\r\nThe format is" +
                            "\r\nstart, class name, ascension level, seed" +
                            "\r\nThe second 2 are optional." +
                            "\r\nIf you want to enter a seed you will need to enter an ascension level." +
                            "\r\nIf there is a save file, you will have the continue and restart options." +
                            "\r\nrestart follows the same format as start." +
                            "\r\nThe only reason for the command difference is to help make sure you don't overwrite a file on accident." +
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
                case "play":
                    return  "\r\nplay" +
                            "\r\nThis command lets you play cards from your hand." +
                            "\r\nThe format is" +
                            "\r\nplay, card number, enemy number" +
                            "\r\nEnemy number is optional for cards without targets." +
                            "\r\nNote that the card number is the index in your hand." +
                            "\r\nIt changes as cards are played." +
                            "\r\nEnemy number does not change.";
                case "potion":
                    return  "\r\npotion" +
                            "\r\nThis command lets you interact with your potions." +
                            "\r\nThere are 3 different options." +
                            "\r\nuse, discard, and inspect." +
                            "\r\nThe format for use is" +
                            "\r\npotion, use, potion number, enemy number" +
                            "\r\nEnemy number is optional for potions without targets." +
                            "\r\nThe format for discard is" +
                            "\r\npotion, discard, potion number" +
                            "\r\nThe format for inspect is" +
                            "\r\npotion, inspect, potion number" +
                            "\r\nInspect displays what the potion does to the output window.";
                case "choice":
                    return  "\r\nchoice" +
                            "\r\nNot to be confused with choices, which is one of the windows." +
                            "\r\nchoice displays the info for one of the choices in the choices window in the output window." +
                            "\r\nThe format is" +
                            "\r\nchoice, choice number.";
                case "end":
                    return  "\r\nend" +
                            "\r\nThis command ends your turn.";
                case "show":
                case "hide":
                    return  "\r\nshow and hide" +
                            "\r\nThese commands allow you to hide and unhide windows." +
                            "\r\nThe format is" +
                            "\r\nshow/hide, window name";
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
                            "\r\nThis command displays the text in the event window in the output window." +
                            "\r\nThe event window is currently a work in progress." +
                            "\r\nCurrent it displays the event name while in an event and run score on death or victory." +
                            "\r\nIn a future update it is planned to also display event dialogue.";
                case "hand":
                    return  "\r\nhand" +
                            "\r\nThis command has two options." +
                            "\r\nThe first option displays the text in the hand window in the output window." +
                            "\r\nThe hand window contains the cards in your hand and your potions." +
                            "\r\nThe second option displays the info of a card in your hand in the output window." +
                            "\r\nThe format is" +
                            "\r\nhand, card number.";
                case "output":
                    return  "\r\ninspect" +
                            "\r\nThis window displays output from various sources." +
                            "\r\nThis is the only window that cannot display its text to the output window." +
                            "\r\nIt also cannot be hidden.";
                case "map":
                    return  "\r\nmap" +
                            "\r\nThis command has two options." +
                            "\r\nThe first option displays the text in the map window in the output window." +
                            "\r\nThe map displays the map nodes." +
                            "\r\nThe nodes include its floor, x coordinate, and what kind of node it is." +
                            "\r\nThe map only displays nodes you can reach from where you are." +
                            "\r\nThe second options lets you inspect a map node." +
                            "\r\nInspecting the map selects the node as a destination." +
                            "\r\nThe inspect window will display a filtered map with only nodes you can reach and on the path to the destination." +
                            "\r\nThe choices window will also display if a given map choice is on track or diverging." +
                            "\r\nIf you have the relic Winged Greaves which allow you to travel to map nodes ignoring connections twice, nodes will display Winged if it needs Winged Greaves to reach.";
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
                            "\r\nrelic, relic number";

            }
        }

        return "";

    }

    public String inspectPotion(AbstractPotion p){

        String s = "\r\n";

        s += "Potion\r\n";
        s += p.name + "\r\n";
        s += Choices.stripColor(p.description) + "\r\n";

        return s;

    }

    public String inspectRelic(AbstractRelic r){

        String s = "\r\n";

        s += "Relic\r\n";
        s += r.name + "\r\n";
        s += Choices.stripColor(r.description) + "\r\n";

        return s;

    }

    public String inspectCard(AbstractCard card){

        String s = "\r\n";

        int cost = Hand.handCost(card);

        s += card.name + "\r\n";

        if(cost == -1)
            s += "Cost : X"+ "\r\n";
        else if(cost != -2)
            s += "Cost : " + cost + "\r\n";

        s += "Type : " + card.type.toString() + "\r\n";

        s += cardText(card) + "\r\n";

        return s;

    }

    public String cardText(AbstractCard c){

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
        switch(p){
            case "ironclad" :
                return CardCrawlGame.characterManager.getCharacter(AbstractPlayer.PlayerClass.IRONCLAD).getPrefs().getInteger("ASCENSION_LEVEL", 0);
            case "the_silent" :
            case "silent" :
                return CardCrawlGame.characterManager.getCharacter(AbstractPlayer.PlayerClass.THE_SILENT).getPrefs().getInteger("ASCENSION_LEVEL", 0);
            case "defect" :
                return CardCrawlGame.characterManager.getCharacter(AbstractPlayer.PlayerClass.DEFECT).getPrefs().getInteger("ASCENSION_LEVEL", 0);
            case "watcher" :
                return CardCrawlGame.characterManager.getCharacter(AbstractPlayer.PlayerClass.WATCHER).getPrefs().getInteger("ASCENSION_LEVEL", 0);
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

        specialUpdates();

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

    //Match and Keep can go die in a hole
    public void specialUpdates(){
        //AbstractDungeon.shrineList.remove("Match and Keep!");

    }

}



















