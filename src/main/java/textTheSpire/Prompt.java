package textTheSpire;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;

//Currently unused
public class Prompt implements org.eclipse.swt.events.KeyListener {

    Shell shell;
    Text prompt;

    /*GetPrompt getPrompt;
    String command;
    boolean awaitCommand;*/

    Text label1;
    Text label2;
    Text label3;
    Text label4;
    Text label5;
    Text label6;
    Text label7;
    Text label8;
    Text label9;
    Text label10;

    boolean[] pinned;

    Choices choices;
    Deck deck;
    Discard discard;
    Event event;
    Hand hand;
    Map map;
    Monster monster;
    Orbs orbs;
    Player player;
    Relic relic;

    public Prompt(Display d, Choices c, Deck de, Discard di, Event e, Hand h, Map m, Monster mo, Orbs o, Player p, Relic r){
        shell = new Shell(d);
        shell.setSize(400,600);
        shell.setLocation(200,400);
        shell.setText("Pinned");

        //prompt = new Text(shell, SWT.NONE);
        //prompt.setSize(400,30);
        //prompt.setEditable(true);

        //prompt.addKeyListener(this);

        label1 = new Text(shell, SWT.MULTI);
        label1.setSize(400,300);
        label1.setEditable(false);

        label2 = new Text(shell, SWT.MULTI);
        label2.setSize(400,300);
        label2.setEditable(false);

        label3 = new Text(shell, SWT.MULTI);
        label3.setSize(400,300);
        label3.setEditable(false);

        label4 = new Text(shell, SWT.MULTI);
        label4.setSize(400,300);
        label4.setEditable(false);

        label5 = new Text(shell, SWT.MULTI);
        label5.setSize(400,300);
        label5.setEditable(false);

        label6 = new Text(shell, SWT.MULTI);
        label6.setSize(400,300);
        label6.setEditable(false);

        label7 = new Text(shell, SWT.MULTI);
        label7.setSize(400,300);
        label7.setEditable(false);

        label8 = new Text(shell, SWT.MULTI);
        label8.setSize(400,300);
        label8.setEditable(false);

        label9 = new Text(shell, SWT.MULTI);
        label9.setSize(400,300);
        label9.setEditable(false);

        label10 = new Text(shell, SWT.MULTI);
        label10.setSize(400,300);
        label10.setEditable(false);

        shell.setVisible(true);
        shell.open();

        pinned = new boolean[10];

        choices = c;
        deck = de;
        discard = di;
        event = e;
        hand = h;
        map = m;
        monster = mo;
        orbs = o;
        player = p;
        relic = r;

    }

    public void update(){
        int index = 0;
        for(int i=0;i<10;i++){
            if(pinned[i]){
                switch (i){
                    case 0 :
                        setText(choices.getText(), index);
                        index++;
                        break;
                    case 1 :
                        setText(deck.getText(), index);
                        index++;
                        break;
                    case 2 :
                        setText(discard.getText(), index);
                        index++;
                        break;
                    case 3 :
                        setText(event.getText(), index);
                        index++;
                        break;
                    case 4 :
                        setText(hand.getText(), index);
                        index++;
                        break;
                    case 5 :
                        setText(map.getText(), index);
                        index++;
                        break;
                    case 6 :
                        setText(monster.getText(), index);
                        index++;
                        break;
                    case 7 :
                        setText(orbs.getText(), index);
                        index++;
                        break;
                    case 8 :
                        setText(player.getText(), index);
                        index++;
                        break;
                    case 9 :
                        setText(relic.getText(), index);
                        index++;
                        break;
                }
            }
        }
    }

    public void setText(String s, int index){

        Text label;

        switch (index){
            case 1:
                label = label1;
                break;
            case 2:
                label = label2;
                break;
            case 3:
                label = label3;
                break;
            case 4:
                label = label4;
                break;
            case 5:
                label = label5;
                break;
            case 6:
                label = label6;
                break;
            case 7:
                label = label7;
                break;
            case 8:
                label = label8;
                break;
            case 9:
                label = label9;
                break;
            case 10:
                label = label10;
                break;
            default:
                return;
        }

        label.getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                if(!label.isDisposed() && !shell.isDisposed() && !s.equals(label.getText())) {
                    label.setText(s);
                }
            }
        });

    }

    public void pin(String window, boolean p){

        switch(window){
            case "choices" :
                pinned[0] = p;
                break;
            case "deck" :
                pinned[1] = p;
                break;
            case "discard" :
                pinned[2] = p;
                break;
            case "event" :
                pinned[3] = p;
                break;
            case "hand" :
                pinned[4] = p;
                break;
            case "map" :
                pinned[5] = p;
                break;
            case "monster" :
                pinned[6] = p;
                break;
            case "orbs" :
                pinned[7] = p;
                break;
            case "player" :
                pinned[8] = p;
                break;
            case "relic" :
                pinned[9] = p;
                break;
        }

    }

    /*public class GetPrompt implements Runnable{

        public volatile String command;

        @Override
        public void run() {
            if(!prompt.isDisposed() && !shell.isDisposed()) {
                command = prompt.getText();
                prompt.setText("");
            }
        }

        public String command(){
            return command;
        }

    }

    public void getCommand(){

        getPrompt = new GetPrompt();
        prompt.getDisplay().asyncExec(getPrompt);

        awaitCommand = true;

    }

    public String checkCommand(){
        if(awaitCommand){
            if(getPrompt.command() != null){
                awaitCommand = false;
                return getPrompt.command();
            }
        }
        return "";
    }*/

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        /*if(!awaitCommand && keyEvent.keyCode == 13){
            getCommand();
        }*/
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

    }
}
