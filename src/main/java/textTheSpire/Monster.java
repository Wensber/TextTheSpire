package textTheSpire;

import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.RunicDome;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import communicationmod.CommandExecutor;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;

public class Monster extends AbstractWindow{

    public boolean haveRunic = false;
    public int totalDmg = 0;

    public Monster(Display display){
        isVisible = true;
        window = new Window(display,"Monster", 400, 600);
    }

    public String getText(){

        if(window.shell.isDisposed()){
            Display.getDefault().dispose();
            Gdx.app.exit();
        }

        StringBuilder s = new StringBuilder();

        //If not in dungeon
        if(CardCrawlGame.dungeon == null ||  !CommandExecutor.isInDungeon() || !CardCrawlGame.isInARun()){
            return "";
        }

        //If in combat
        if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT){

            int count = 0;
            int totalAlive = 0;
            totalDmg = 0;

            for(AbstractMonster m : AbstractDungeon.getCurrRoom().monsters.monsters){

                if(m.currentHealth > 0) {

                    totalAlive++;

                    s.append(count).append(": ").append(m.name).append("\r\n");
                    s.append("Block: ").append(m.currentBlock).append("\r\n");
                    s.append("HP: ").append(m.currentHealth).append("/").append(m.maxHealth).append("\r\n");

                    if (!haveRunic && !runicDome())
                        s.append(monsterIntent(m));

                    int powCount = 0;
                    ArrayList<AbstractPower> p = m.powers;
                    if(p.size() > 0) {
                        s.append("Powers:\r\n");
                        for (AbstractPower ap : p) {
                            s.append(powCount).append(": ").append(ap.name).append(" ").append(ap.amount).append("\r\n");
                            powCount++;
                        }
                    }
                    s.append("\r\n");
                }
                count++;

            }

            s.insert(0, "\r\nCount: " + totalAlive + "\r\n" + "Incoming: " + totalDmg + "\r\n");

            return s.toString();

        }else{
            //If not in combat
            return "";
        }
    }

    public boolean runicDome(){
        if(haveRunic)
            return true;
        for(AbstractRelic r : AbstractDungeon.player.relics){
            if(r instanceof RunicDome){
                haveRunic = true;
                return true;
            }
        }
        return false;
    }

    /*
    Params:
        m - any Monster on the field
    Returns:
        String containing m's intent
     */
    public String monsterIntent(AbstractMonster m){

        AbstractMonster.Intent i = m.intent;
        int multi;

        if (i == AbstractMonster.Intent.ATTACK) {
            multi = getMulti(m);
            if(multi > 1) {
                totalDmg += m.getIntentDmg() * multi;
                return "Intent: Attack " + m.getIntentDmg() + " x " + multi + "\r\n";
            } else {
                totalDmg += m.getIntentDmg();
                return "Intent: Attack " + m.getIntentDmg() + "\r\n";
            }
        } else if (i == AbstractMonster.Intent.ATTACK_BUFF) {
            multi = getMulti(m);
            if(multi > 1) {
                totalDmg += m.getIntentDmg() * multi;
                return "Intent: Attack/Buff " + m.getIntentDmg() + " x " + multi + "\r\n";
            } else {
                totalDmg += m.getIntentDmg();
                return "Intent: Attack/Buff " + m.getIntentDmg() + "\r\n";
            }
        } else if (i == AbstractMonster.Intent.ATTACK_DEFEND) {
            multi = getMulti(m);
            if(multi > 1) {
                totalDmg += m.getIntentDmg() * multi;
                return "Intent: Attack/Defend " + m.getIntentDmg() + " x " + multi + "\r\n";
            } else {
                totalDmg += m.getIntentDmg();
                return "Intent: Attack/Defend " + m.getIntentDmg() + "\r\n";
            }
        } else if (i == AbstractMonster.Intent.ATTACK_DEBUFF) {
            multi = getMulti(m);
            if(multi > 1) {
                totalDmg += m.getIntentDmg() * multi;
                return "Intent: Attack/Debuff " + m.getIntentDmg() + " x " + multi + "\r\n";
            } else {
                totalDmg += m.getIntentDmg();
                return "Intent: Attack/Debuff " + m.getIntentDmg() + "\r\n";
            }
        } else if (i == AbstractMonster.Intent.BUFF) {
            return "Intent: Buff" + "\r\n";
        } else if (i == AbstractMonster.Intent.DEBUFF) {
            return "Intent: Debuff" + "\r\n";
        } else if (i == AbstractMonster.Intent.STRONG_DEBUFF) {
            return "Intent: Strong Debuff" + "\r\n";
        } else if (i == AbstractMonster.Intent.DEFEND) {
            return "Intent: Defend" + "\r\n";
        } else if (i == AbstractMonster.Intent.DEFEND_DEBUFF) {
            return "Intent: Defend/Debuff" + "\r\n";
        } else if (i == AbstractMonster.Intent.DEFEND_BUFF) {
            return "Intent: Defend/Buff" + "\r\n";
        } else if (i == AbstractMonster.Intent.ESCAPE) {
            return "Intent: Escape" + "\r\n";
        } else if (i == AbstractMonster.Intent.MAGIC) {
            return "Intent: MAGIC" + "\r\n";
        } else if (i == AbstractMonster.Intent.NONE) {
            return "Intent: NONE" + "\r\n";
        } else if (i == AbstractMonster.Intent.SLEEP) {
            return "Intent: Sleep" + "\r\n";
        } else if (i == AbstractMonster.Intent.STUN) {
            return "Intent: Stun" + "\r\n";
        } else if (i == AbstractMonster.Intent.UNKNOWN) {
            return "Intent: Unknown" + "\r\n";
        } else{
            return "Intent: Loading" + "\r\n";
        }
    }

    /*
    Params:
        m - Monster on field
    Returns:
        The number of hits in m's intent
     */
    public int getMulti(AbstractMonster m){

        return (int) basemod.ReflectionHacks.getPrivate(m, AbstractMonster.class, "intentMultiAmt");

    }

}
