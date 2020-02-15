package textTheSpire;

import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.orbs.*;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.stances.NeutralStance;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;

public class Player {

    public Window player;

    public Player(Display display){
        player = new Window(display,"Player",300,300);
    }

    public void update(){

        if(player.shell.isDisposed()){
            Display.getDefault().dispose();
            Gdx.app.exit();
        }

        StringBuilder s = new StringBuilder();

        //Not in dungeon
        if(CardCrawlGame.dungeon == null || !CardCrawlGame.isInARun()){
            player.setText(s.toString());
            player.setVisible(false);
            return;
        }

        AbstractPlayer p = AbstractDungeon.player;

        //In combat show all player stats
        if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT){

            s.append("Block: ").append(p.currentBlock).append("\r\n");
            s.append("Health: ").append(p.currentHealth).append("/").append(p.maxHealth).append("\r\n");
            s.append("Energy: ").append(EnergyPanel.totalCount).append("\r\n");

            //Display orbs if Defect
            if (p.chosenClass == AbstractPlayer.PlayerClass.DEFECT) {
                ArrayList<AbstractOrb> ol = p.orbs;
                for (AbstractOrb o : ol) {
                    if (o instanceof Dark) {
                        s.append("Dark ").append(o.evokeAmount).append(" ");
                    } else if (o instanceof Lightning) {
                        s.append("Lightning ");
                    } else if (o instanceof Frost) {
                        s.append("Frost ");
                    } else if (o instanceof Plasma) {
                        s.append("Plasma ");
                    } else {
                        s.append("Empty ");
                    }
                }
                s.append("\r\n");
            }

            //If not neutral stance display it
            if (!(p.stance instanceof NeutralStance)) {
                s.append("Stance: ").append(p.stance.name).append("\r\n");
            }

            ArrayList<AbstractPower> po = p.powers;
            if(po.size() > 0) {
                s.append("Powers:\r\n");
                for (AbstractPower ap : po) {
                    s.append(ap.name).append(" ").append(ap.amount).append("\r\n");
                }
            }

        }else{

            //Out of combat show persistent stats
            s.append("Health: ").append(p.currentHealth).append("/").append(p.maxHealth).append("\r\n");

            s.append("Gold:").append(p.gold).append("\r\n");

            //Hand window is gone so show potions in player out of combat
            ArrayList<AbstractPotion> pl = p.potions;

            if (pl.size() > 0) {
                s.append("Potions:\r\n");
                for (AbstractPotion po : pl) {
                    s.append(po.name).append("\r\n");
                }
            }
        }

        player.setText(s.toString());
        player.setVisible(true);

    }

}
