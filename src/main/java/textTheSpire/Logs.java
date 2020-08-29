package textTheSpire;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import communicationmod.CommandExecutor;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;

public class Logs {

    public Window logs;

    private String log;

    private int playerHp;
    private ArrayList<Integer> monsterHp;

    public Logs(Display display){
        logs = new Window(display,"Log",450,525);
        log = "";
        //logs.setText(log);
        playerHp = 0;
        monsterHp = new ArrayList<>();
    }

    public String getText(){
        return log;
    }

    public void output(String s){
        log = "\r\n" + s + log;
        logs.setText(log);
    }

    public void update(){

        if(!CommandExecutor.isInDungeon()){
            return;
        }

        if(AbstractDungeon.player.currentHealth != playerHp){
            if(playerHp < AbstractDungeon.player.currentHealth){
                output("Played healed " + (AbstractDungeon.player.currentHealth - playerHp));
            }else{
                output("Played took " + (playerHp - AbstractDungeon.player.currentHealth));
            }
            playerHp = AbstractDungeon.player.currentHealth;
        }

        if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT){
            if(AbstractDungeon.getCurrRoom().monsters.monsters.size() > monsterHp.size()){
                for(int i=monsterHp.size();i<AbstractDungeon.getCurrRoom().monsters.monsters.size();i++){
                    monsterHp.add(AbstractDungeon.getCurrRoom().monsters.monsters.get(i).currentHealth);
                    output("New Monster " + AbstractDungeon.getCurrRoom().monsters.monsters.get(i).name + " HP " + AbstractDungeon.getCurrRoom().monsters.monsters.get(i).currentHealth);
                }
            }
            for(int i=0;i<monsterHp.size();i++){
                if(i < AbstractDungeon.getCurrRoom().monsters.monsters.size() && monsterHp.get(i) < AbstractDungeon.getCurrRoom().monsters.monsters.get(i).currentHealth){
                    output("Monster " + i + " : " + AbstractDungeon.getCurrRoom().monsters.monsters.get(i).name + " healed " + (AbstractDungeon.getCurrRoom().monsters.monsters.get(i).currentHealth - monsterHp.get(i)));
                    monsterHp.set(i, AbstractDungeon.getCurrRoom().monsters.monsters.get(i).currentHealth);
                } else if (i < AbstractDungeon.getCurrRoom().monsters.monsters.size() && monsterHp.get(i) > AbstractDungeon.getCurrRoom().monsters.monsters.get(i).currentHealth){
                    output("Monster " + i + " : " + AbstractDungeon.getCurrRoom().monsters.monsters.get(i).name + " took " + (monsterHp.get(i) - AbstractDungeon.getCurrRoom().monsters.monsters.get(i).currentHealth));
                    monsterHp.set(i, AbstractDungeon.getCurrRoom().monsters.monsters.get(i).currentHealth);
                }
            }
        }else{
            monsterHp.clear();
        }

    }

}













