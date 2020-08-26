package textTheSpire;

import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.mod.replay.rooms.PsuedoBonfireRoom;
import com.megacrit.cardcrawl.mod.replay.rooms.TeleportRoom;
import com.megacrit.cardcrawl.rooms.*;
import communicationmod.ChoiceScreenUtils;
import communicationmod.CommandExecutor;
import org.eclipse.swt.widgets.Display;
import replayTheSpire.patches.BonfirePatches;

import java.util.ArrayList;

public class Map extends AbstractWindow{

    public Map(Display display){
        isVisible = true;
        window = new Window(display,"Map", 550, 425);
    }

    public String getText(){

        if(window.shell.isDisposed()){
            Display.getDefault().dispose();
            Gdx.app.exit();
        }

        if(Inspect.has_inspected && AbstractDungeon.currMapNode.y >= Inspect.destination.y){
            Inspect.has_inspected = false;
        }

        StringBuilder s = new StringBuilder();
        s.append("\r\n");

        //Is only displayed when on map screen
        if(CommandExecutor.isInDungeon() && !(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT)) {

            s.append(AbstractDungeon.bossKey).append("\r\n");

            ArrayList<ArrayList<MapRoomNode>> m = AbstractDungeon.map;
            //Current position
            s.append("Current= Floor:").append(AbstractDungeon.currMapNode.y + 1).append(" X:").append(AbstractDungeon.currMapNode.x).append("\r\n");
            s.append("\r\n");

            //Either display all nodes.
            if(AbstractDungeon.currMapNode.y == 15 || AbstractDungeon.currMapNode.y == -1) {

                int currFloor = AbstractDungeon.currMapNode.y;

                if(currFloor == 15)
                    currFloor = -1;

                for (int i = m.size() - 1; i >= (currFloor + 1); i--) {


                    for (MapRoomNode n : m.get(i)) {

                        if (n.hasEdges()) {
                            s.append(nodeType(n)).append("Floor:").append(i + 1).append(" X:").append(n.x).append("\r\n");
                        }

                    }

                    s.append("\r\n");

                }
            }else{

                //Or only display ones reachable from current node
                StringBuilder limitedMap = new StringBuilder();
                StringBuilder limitedFloor;
                StringBuilder limitedNode;

                ArrayList<MapRoomNode> current = new ArrayList<MapRoomNode>();
                ArrayList<MapRoomNode> prev = new ArrayList<MapRoomNode>();

                prev.add(AbstractDungeon.currMapNode);

                for(int i=(AbstractDungeon.currMapNode.y + 1);i<m.size();i++ ){
                    limitedFloor = new StringBuilder();

                    for(MapRoomNode n : m.get(i)){
                        limitedNode = new StringBuilder();

                        for (MapRoomNode child : prev) {
                            if (child.isConnectedTo(n)) {
                                limitedNode.append(nodeType(n)).append(" Floor:").append(i + 1).append(" X: ").append(n.x).append("\r\n");
                                break;
                            }
                        }
                        if(limitedNode.length() > 0) {
                            limitedFloor.append(limitedNode);
                            current.add(n);
                        } else if(AbstractDungeon.player.hasRelic("WingedGreaves") && (AbstractDungeon.player.getRelic("WingedGreaves")).counter > 0 && n.getParents().size() > 0){
                            limitedFloor.append("Winged ").append(nodeType(n)).append(" Floor:").append(i + 1).append(" X: ").append(n.x).append("\r\n");
                        }

                    }

                    limitedFloor.append("\r\n");

                    prev.clear();
                    prev.addAll(current);
                    current.clear();
                    limitedMap.insert(0, limitedFloor);

                }

                s.append(limitedMap);

            }

            return s.toString();

        }else{
            return "";
        }
    }

    /*
    Params:
        n - any MapRoomNode
    Returns:
        A String representing the type of node n is
     */
     public static String nodeType(MapRoomNode n){
        if(n.getRoom() instanceof MonsterRoomElite){
            if(n.hasEmeraldKey)
                return "Emerald Key ";
            else
                return "Elite ";
        }else if(TextTheSpire.replayTheSpire && n == BonfirePatches.bonfireNode){
            return "Bonfire ";
        }else{
            String s = n.getRoom().getClass().getSimpleName();
            if(s.equals("EventRoom"))
                return "Unknown ";
            if(s.substring(s.length()-4).equals("Room")){
                return s.substring(0, s.length()-4) + " ";
            }else{
                return s + " ";
            }
        }

        /*
        else if(n.getRoom() instanceof MonsterRoom){
            return "Monster ";
        }else if(n.getRoom() instanceof RestRoom){
            if(TextTheSpire.replayTheSpire && n.getRoom() instanceof PsuedoBonfireRoom)
                 return "Bonfire ";
            else
                return "Rest ";
        }else if(n.getRoom() instanceof ShopRoom){
            return "Shop ";
        }else if(n.getRoom() instanceof TreasureRoom){
            return "Treasure ";
        }else if(TextTheSpire.replayTheSpire) {
            if(n.getRoom() instanceof TeleportRoom){

            }
        }else{
             return "Unknown ";
        }
         */

    }

}
