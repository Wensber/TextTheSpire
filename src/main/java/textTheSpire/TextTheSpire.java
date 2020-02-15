package textTheSpire;

import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;

import basemod.BaseMod;
import basemod.interfaces.PostUpdateSubscriber;
import basemod.interfaces.PreUpdateSubscriber;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.*;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;

import com.megacrit.cardcrawl.unlock.UnlockTracker;
import communicationmod.ChoiceScreenUtils;
import communicationmod.CommandExecutor;
import org.eclipse.swt.widgets.Display;

import javax.swing.*;
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

    private Window inspect;

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

            inspect = new Window(display,"Inspect" , 550, 425);

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

        //Dispose of windows and then exit
        if (input.equals("quit")) {

            dispose();

            Gdx.app.exit();
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
                CommandExecutor.executeCommand(input);
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
            } else if (tokens[0].equals("inspect")) {
                int in;
                try {
                    in = Integer.parseInt(tokens[1]) - 1;

                    String s = "";

                    if (in < 0 || in >= AbstractDungeon.player.hand.group.size())
                        return;

                    AbstractCard c = AbstractDungeon.player.hand.group.get(in);

                    s += c.name + "\r\n";
                    s += "Cost : " + Hand.handCost(c) + "\r\n";
                    if (c.damage > 0)
                        s += "Damage : " + c.damage + "\r\n";
                    if (c.block > 0)
                        s += "Block : " + c.block + "\r\n";
                    if (c.magicNumber > 0)
                        s += "Magic Number : " + c.magicNumber + "\r\n";
                    if (c.heal > 0)
                        s += "Heal : " + c.heal + "\r\n";
                    if (c.draw > 0)
                        s += "Draw : " + c.draw + "\r\n";
                    if (c.discard > 0)
                        s += ("Discard : " + c.discard + "\r\n");

                    inspect.setText(s);

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
                inspect.setText(inspectMap(tokens));
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

    public String inspectMap(String[] tokens){
        int floor;
        int x;

        try{
            floor = Integer.parseInt(tokens[1]);
            x = Integer.parseInt(tokens[2]);
        } catch (Exception e){
            return "";
        }

        if(floor < 1 || floor > 15 || x < 0 || x > 6)
            return "";

        StringBuilder s = new StringBuilder();

        ArrayList<ArrayList<MapRoomNode>> map = AbstractDungeon.map;
        int current_y = AbstractDungeon.currMapNode.y;
        ArrayList<ArrayList<MapRoomNode>> m;

        if(!(AbstractDungeon.currMapNode.y == -1 || (AbstractDungeon.player.hasRelic("WingedGreaves") && (AbstractDungeon.player.getRelic("WingedGreaves")).counter > 0))) {

            m = new ArrayList<ArrayList<MapRoomNode>>();

            ArrayList<MapRoomNode> current = new ArrayList<MapRoomNode>();
            current.add(AbstractDungeon.currMapNode);
            m.add(current);

            for (int i = (current_y + 1); i < map.size(); i++) {

                ArrayList<MapRoomNode> next_floor = new ArrayList<MapRoomNode>();

                for (MapRoomNode n : map.get(i)) {

                    for (MapRoomNode child : m.get(i - current_y - 1)) {
                        if (child.isConnectedTo(n)) {
                            next_floor.add(n);

                            break;
                        }
                    }

                }

                m.add(next_floor);

            }
        }else{
            m = map;
        }

        ArrayList<MapRoomNode> curr = new ArrayList<MapRoomNode>();
        ArrayList<MapRoomNode> prev = new ArrayList<MapRoomNode>();

        if(current_y == -1)
            current_y = 0;

        for(MapRoomNode child : m.get(floor - current_y - 1)){
            if(child.x == x){
                prev.add(child);
                s.append("Floor " + floor + "\r\n");
                s.append(Map.nodeType(child) + x + "\r\n");
                break;
            }
        }

        if(prev.size() == 0)
            return "";

        for(int i = (floor - current_y - 2);i>=0;i--){

            s.append("Floor " + (i + current_y + 1) + "\r\n");

            for(MapRoomNode node : m.get(i)){

                for(MapRoomNode parent : prev){
                    if(node.isConnectedTo(parent)){
                        s.append(Map.nodeType(node) + node.x + "\r\n");
                        curr.add(node);
                        break;
                    }
                }

            }

            prev.clear();
            prev.addAll(curr);
            curr.clear();

        }

        return s.toString();

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



















