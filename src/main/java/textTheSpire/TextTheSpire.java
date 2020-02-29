package textTheSpire;

import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;

import basemod.BaseMod;
import basemod.interfaces.PostUpdateSubscriber;
import basemod.interfaces.PreUpdateSubscriber;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.red.Whirlwind;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.*;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;

import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import communicationmod.ChoiceScreenUtils;
import communicationmod.CommandExecutor;
import org.eclipse.swt.widgets.Display;

import javax.swing.*;
import java.lang.reflect.Array;
import java.util.ArrayList;


@SpireInitializer
public class TextTheSpire implements PostUpdateSubscriber, PreUpdateSubscriber{

    //Used to only update display every number of update cycles
    int iter;

    private Hand hand;
    private Map map;
    private Discard discard;
    private Deck deck;
    private Player player;
    private Monster monster;
    private Event event;
    private Relic relic;
    private Orbs orbs;

    private Inspect inspect;

    private JTextField promptFrame;

    private String queuedCommand = "";
    private boolean hasQueuedCommand = false;

    public TextTheSpire() {


        Thread ui = new Thread(() -> {
            Display display = new Display();

            hand = new Hand(display);
            map = new Map(display);
            event = new Event(display);
            monster = new Monster(display);
            deck = new Deck(display);
            discard = new Discard(display);
            relic = new Relic(display);
            player = new Player(display);
            orbs = new Orbs(display);

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

    //Parse a command to see if its an allowed command and send to CommunicationMod to execute
    public void parsePrompt(String input) {

        switch(input){
            case "quit":
                dispose();
                Gdx.app.exit();
                break;
            case "deck":
                inspect.setText(deck.getText());
                break;
            case "discard":
                inspect.setText(discard.getText());
                break;
            case "event":
                inspect.setText(event.getText());
                break;
            case "hand":
                inspect.setText(hand.getText());
                break;
            case "map":
                inspect.setText(map.getText());
                break;
            case "monster":
                inspect.setText(monster.getText());
                break;
            case "orbs":
                inspect.setText(orbs.getText());
                break;
            case "player":
                inspect.setText(player.getText());
                break;
            case "relic":
                inspect.setText(relic.getText());
                break;
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

        if(tokens[0].equals("show") && tokens.length >= 2){

            switch(tokens[1]){
                case "deck":
                    deck.isVisible = true;
                    break;
                case "discard":
                    discard.isVisible = true;
                    break;
                case "event":
                    event.isVisible = true;
                    break;
                case "hand":
                    hand.isVisible = true;
                    break;
                case "map":
                    hand.isVisible = true;
                    break;
                case "monster":
                    monster.isVisible = true;
                    break;
                case "orbs":
                    orbs.isVisible = true;
                    break;
                case "player":
                    player.isVisible = true;
                    break;
                case "relic":
                    relic.isVisible = true;
                    break;
            }

        }

        if(tokens[0].equals("hide") && tokens.length >= 2){

            switch(tokens[1]){
                case "deck":
                    deck.isVisible = false;
                    break;
                case "discard":
                    discard.isVisible = false;
                    break;
                case "event":
                    event.isVisible = false;
                    break;
                case "hand":
                    hand.isVisible = false;
                    break;
                case "map":
                    hand.isVisible = false;
                    break;
                case "monster":
                    monster.isVisible = false;
                    break;
                case "orbs":
                    orbs.isVisible = false;
                    break;
                case "player":
                    player.isVisible = false;
                    break;
                case "relic":
                    relic.isVisible = false;
                    break;
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
                if (CardCrawlGame.mode == CardCrawlGame.GameMode.CHAR_SELECT && isUnlocked(tokens))
                    CommandExecutor.executeCommand(input.substring(2));
                return;
            } catch (Exception e) {
                return;
            }
        }

        //Commands below are only usable in a dungeon
        if (!CommandExecutor.isInDungeon())
            return;

        //Potion Command. If out of combat can only discard
        if (tokens[0].equals("potion")) {
            try {
                if(tokens.length >= 3 && tokens[1].equals("inspect")){

                    int in = Integer.parseInt(tokens[2]);
                    String s = " ";
                    if(in >= 0 && in < AbstractDungeon.player.potions.size()) {
                        AbstractPotion p = AbstractDungeon.player.potions.get(in);

                        s += p.name + "\r\n";
                        s += Event.stripColor(p.description) + "\r\n";

                        inspect.setText(s);

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
            try {
                if(tokens.length >= 2) {
                    int in = Integer.parseInt(tokens[1]);

                    ChoiceScreenUtils.ChoiceType c = ChoiceScreenUtils.getCurrentChoiceType();

                    if(c == ChoiceScreenUtils.ChoiceType.SHOP_SCREEN){
                        ArrayList<Object> shopItems = Event.getAvailableShopItems();

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
                                String s = "";

                                s += r.name + "\r\n";
                                s += "Counter: " + r.counter + "\r\n";
                                s += Event.stripColor(r.description) + "\r\n";

                                inspect.setText(s);

                            } else if (item instanceof StorePotion) {

                                AbstractPotion p = ((StorePotion)item).potion;
                                String s = "";

                                s += p.name + "\r\n";
                                s += Event.stripColor(p.description) + "\r\n";

                                inspect.setText(s);

                            }
                        }
                    } else if (c == ChoiceScreenUtils.ChoiceType.GRID){

                        in--;

                        ArrayList<AbstractCard> grid = ChoiceScreenUtils.getGridScreenCards();

                        if(in >= 0 && in < grid.size()){

                            AbstractCard card = grid.get(in);

                            inspect.setText(inspectCard(card));

                        }

                    } else if (c == ChoiceScreenUtils.ChoiceType.CARD_REWARD){

                        in--;

                        if(in >= 0 && in < AbstractDungeon.cardRewardScreen.rewardGroup.size()){

                            AbstractCard card = AbstractDungeon.cardRewardScreen.rewardGroup.get(in);

                            inspect.setText(inspectCard(card));

                        }

                    } else if (c == ChoiceScreenUtils.ChoiceType.HAND_SELECT){

                        in--;

                        if(in >= 0 && in < AbstractDungeon.handCardSelectScreen.selectedCards.group.size()){

                            AbstractCard card = AbstractDungeon.handCardSelectScreen.selectedCards.group.get(in);

                            inspect.setText(inspectCard(card));

                        }

                    } else if (c == ChoiceScreenUtils.ChoiceType.BOSS_REWARD){

                        in--;

                        if(in >= 0 && in < AbstractDungeon.bossRelicScreen.relics.size()){

                            AbstractRelic r = AbstractDungeon.bossRelicScreen.relics.get(in);

                            String s = "";

                            s += r.name + "\r\n";
                            s += "Counter: " + r.counter + "\r\n";
                            s += Event.stripColor(r.description) + "\r\n";

                            inspect.setText(s);

                        }

                    } else if (c == ChoiceScreenUtils.ChoiceType.COMBAT_REWARD){

                        in--;

                        ArrayList<RewardItem> rewards = AbstractDungeon.combatRewardScreen.rewards;

                        if(in >= 0 && in < rewards.size()){

                            RewardItem reward = rewards.get(in);

                            if(reward.type == RewardItem.RewardType.RELIC){

                                AbstractRelic r = reward.relic;

                                String s = "";

                                s += r.name + "\r\n";
                                s += "Counter: " + r.counter + "\r\n";
                                s += Event.stripColor(r.description) + "\r\n";

                                inspect.setText(s);

                            }else if (reward.type == RewardItem.RewardType.POTION){

                                AbstractPotion p = reward.potion;

                                String s = "";

                                s += p.name + "\r\n";
                                s += Event.stripColor(p.description) + "\r\n";

                                inspect.setText(s);

                            }

                        }

                    }

                }
                return;
            } catch (Exception e) {
                return;
            }
        }

        if (tokens[0].equals("relic")) {
            try {
                if(tokens.length >= 2) {
                    int in = Integer.parseInt(tokens[1]);
                    if(in >= 0 && in < AbstractDungeon.player.relics.size()){

                        AbstractRelic r = AbstractDungeon.player.relics.get(in);

                        String s = "";

                        s += r.name + "\r\n";
                        s += "Counter: " + r.counter + "\r\n";
                        s += Event.stripColor(r.description) + "\r\n";

                        inspect.setText(s);

                    }
                }
                return;
            } catch (Exception e) {
                return;
            }
        }

        //Press a confirm or cancel button. Only usable if such a button exists
        if (ChoiceScreenUtils.isConfirmButtonAvailable() && input.equals(ChoiceScreenUtils.getConfirmButtonText())) {
            ChoiceScreenUtils.pressConfirmButton();
            return;
        }
        if (ChoiceScreenUtils.isCancelButtonAvailable() && input.equals(ChoiceScreenUtils.getCancelButtonText())) {
            ChoiceScreenUtils.pressCancelButton();
            return;
        }

        //Commands only usable during combat. Includes play and end.
        if (d != null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
            input = input.toLowerCase();

            if (tokens[0].equals("play")) {
                try {
                    CommandExecutor.executeCommand(input);
                } catch (Exception e) {
                    return;
                }
            } else if (tokens[0].equals("end")) {
                try {
                    CommandExecutor.executeCommand(tokens[0]);
                } catch (Exception e) {
                    return;
                }
            } else if (tokens[0].equals("hand")) {
                int in;
                try {
                    in = Integer.parseInt(tokens[1]) - 1;

                    if (in < 0 || in >= AbstractDungeon.player.hand.group.size())
                        return;

                    AbstractCard c = AbstractDungeon.player.hand.group.get(in);

                    inspect.setText(inspectCard(c));

                } catch (Exception e) {
                    return;
                }
            } else {
                int in;
                try {
                    in = Integer.parseInt(input) - 1;
                    ChoiceScreenUtils.executeChoice(in);
                } catch (Exception e) {
                    return;
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
            } catch (Exception e) {
                return;
            }
        }
    }

    public String inspectCard(AbstractCard card){

        String s = "";

        int cost = Hand.handCost(card);

        s += card.name + "\r\n";

        if(cost == -1)
            s += "Cost : X"+ "\r\n";
        else if(cost != -2)
            s += "Cost : " + cost + "\r\n";

        s += cardText(card) + "\r\n";

        return s;

    }

    public String cardText(AbstractCard c){

        String s = Event.stripColor(c.rawDescription);

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

    public boolean isUnlocked(String[] tokens){
        String c = tokens[1].toLowerCase();
        switch(c){
            case "ironclad" :
                if(tokens.length > 2){
                    try {
                        if (Integer.parseInt(tokens[2]) > CardCrawlGame.characterManager.getCharacter(AbstractPlayer.PlayerClass.IRONCLAD).getPrefs().getInteger("ASCENSION_LEVEL", 0)){
                            return false;
                        }
                    }catch(Exception e){
                        return false;
                    }
                }
                return true;
            case "the_silent" :
            case "silent" :
                if(UnlockTracker.isCharacterLocked("The Silent")){
                    return false;
                }
                if(tokens.length > 2){
                    try {
                        if (Integer.parseInt(tokens[2]) > CardCrawlGame.characterManager.getCharacter(AbstractPlayer.PlayerClass.THE_SILENT).getPrefs().getInteger("ASCENSION_LEVEL", 0)){
                            return false;
                        }
                    }catch(Exception e){
                        return false;
                    }
                }
                return true;
            case "defect" :
                if(UnlockTracker.isCharacterLocked("Defect")){
                    return false;
                }
                if(tokens.length > 2){
                    try {
                        if (Integer.parseInt(tokens[2]) > CardCrawlGame.characterManager.getCharacter(AbstractPlayer.PlayerClass.DEFECT).getPrefs().getInteger("ASCENSION_LEVEL", 0)){
                            return false;
                        }
                    }catch(Exception e){
                        return false;
                    }
                }
                return true;
            case "watcher" :
                if(UnlockTracker.isCharacterLocked("Watcher")){
                    return false;
                }
                if(tokens.length > 2){
                    try {
                        if (Integer.parseInt(tokens[2]) > CardCrawlGame.characterManager.getCharacter(AbstractPlayer.PlayerClass.WATCHER).getPrefs().getInteger("ASCENSION_LEVEL", 0)){
                            return false;
                        }
                    }catch(Exception e){
                        return false;
                    }
                }
                return true;
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
        event.update();
        orbs.update();

        specialUpdates();



    }

    public void dispose(){
        Display.getDefault().dispose();
    }

    //Match and Keep can go die in a hole
    public void specialUpdates(){
        AbstractDungeon.shrineList.remove("Match and Keep!");
    }

}



















