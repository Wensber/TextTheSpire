package textTheSpire;

import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;

import basemod.BaseMod;
import basemod.interfaces.PostUpdateSubscriber;
import basemod.interfaces.PreUpdateSubscriber;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.rooms.*;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;

import communicationmod.ChoiceScreenUtils;
import communicationmod.CommandExecutor;
import org.eclipse.swt.widgets.Display;

import javax.swing.*;


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

            while(!display.isDisposed()){
                while(display.readAndDispatch()){
                    System.out.println("Read and Dispatched");
                }

                if(iter < 50){
                    iter++;
                    return;
                }
                iter = 0;

                System.out.println("Update");

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
    public void parsePrompt(String input){

        //Dispose of windows and then exit
        if(input.equals("quit")){

            dispose();

            Gdx.app.exit();
        }

        //Continue command. Only usable when not in dungeon and save file exists
        if(!CommandExecutor.isInDungeon() && CardCrawlGame.characterManager.anySaveFileExists() && input.equals("continue")){

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
                CommandExecutor.executeCommand(input);
                return;
            } catch (Exception e) {
                return;
            }
        }else if(tokens[0].equals("restart") && CardCrawlGame.characterManager.anySaveFileExists()){
            try {
                CommandExecutor.executeCommand(input.substring(2));
                return;
            } catch (Exception e) {
                return;
            }
        }

        //Commands below are only usable in a dungeon
        if(!CommandExecutor.isInDungeon())
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

    //Update displays every 30 update cycles
    @Override
    public void receivePostUpdate() {

        /*
        if(iter < 30){
            iter++;
            return;
        }
        iter = 0;


        updateDeck();
        updateDiscard();
        updateHand();
        updateMonsters();
        updatePlayer();
        updateRelic();
        updateMap();

        updateEvent();

        specialUpdates();

         */

    }

    public void dispose(){
        Display.getDefault().dispose();
    }

    //Match and Keep can go die in a hole
    public void specialUpdates(){
        AbstractDungeon.shrineList.remove("Match and Keep!");
    }

}



















