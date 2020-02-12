package textTheSpire;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.RunicDome;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;

public class Monster {

    public Window monster;
    public boolean haveRunic = false;

    public Monster(Display display){
        monster = new Window(display,"Monster", 400, 600);
    }

    public void update(){

        StringBuilder s = new StringBuilder();

        //If not in dungeon
        if(CardCrawlGame.dungeon == null || !CardCrawlGame.isInARun()){
            monster.setText(s.toString());
            return;
        }

        //If in combat
        if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT){


            int count = 0;

            for(AbstractMonster m : AbstractDungeon.getCurrRoom().monsters.monsters){

                if(m.currentHealth > 0) {
                    s.append(count).append(": ").append(m.name).append("\r\n");
                    s.append("Block: ").append(m.currentBlock).append(" ");
                    s.append("HP: ").append(m.currentHealth).append("/").append(m.maxHealth).append("\r\n");

                    if (!haveRunic && !runicDome())
                        s.append(monsterIntent(m));

                    ArrayList<AbstractPower> p = m.powers;
                    if(p.size() > 0) {
                        s.append("Powers: ");
                        for (AbstractPower ap : p) {
                            s.append(ap.name).append("-").append(ap.amount).append(", ");
                        }
                        s = new StringBuilder(s.substring(0, s.length() - 2));
                    }

                    s.append("\r\n\r\n");
                }
                count++;

            }

            monster.setText(s.toString());

        }else{
            //If not in combat
            monster.setText(s.toString());
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
            if(multi > 1)
                return "Intent: Attack " + m.getIntentDmg() + "x" + multi + "\r\n";
            else
                return "Intent: Attack " + m.getIntentDmg() + "\r\n";
        } else if (i == AbstractMonster.Intent.ATTACK_BUFF) {
            multi = getMulti(m);
            if(multi > 1)
                return "Intent: Attack/Buff " + m.getIntentDmg() + "x" + multi + "\r\n";
            else
                return "Intent: Attack/Buff " + m.getIntentDmg() + "\r\n";
        } else if (i == AbstractMonster.Intent.ATTACK_DEFEND) {
            multi = getMulti(m);
            if(multi > 1)
                return "Intent: Attack/Defend " + m.getIntentDmg() + "x" + multi + "\r\n";
            else
                return "Intent: Attack/Defend " + m.getIntentDmg() + "\r\n";
        } else if (i == AbstractMonster.Intent.ATTACK_DEBUFF) {
            multi = getMulti(m);
            if(multi > 1)
                return "Intent: Attack/Debuff " + m.getIntentDmg() + "x" + multi + "\r\n";
            else
                return "Intent: Attack/Debuff " + m.getIntentDmg() + "\r\n";
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
            return "Intent: Debug (Shouldn't Happen)" + "\r\n";
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
