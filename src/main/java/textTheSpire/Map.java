package textTheSpire;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.*;
import communicationmod.ChoiceScreenUtils;
import communicationmod.CommandExecutor;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;

public class Map {

    public Window map;

    public Map(Display display){
        map = new Window(display,"Map", 550, 425);
    }

    public void update(){

        StringBuilder s = new StringBuilder();

        //Is only displayed when on map screen
        if(CommandExecutor.isInDungeon() && ChoiceScreenUtils.getCurrentChoiceType() == ChoiceScreenUtils.ChoiceType.MAP) {

            ArrayList<ArrayList<MapRoomNode>> m = AbstractDungeon.map;
            //Current position
            s.append("Current= Floor:").append(AbstractDungeon.currMapNode.y + 1).append(" X:").append(AbstractDungeon.currMapNode.x).append("\r\n");

            //Either display all nodes.
            if(AbstractDungeon.currMapNode.y == -1 || (AbstractDungeon.player.hasRelic("WingedGreaves") && (AbstractDungeon.player.getRelic("WingedGreaves")).counter > 0)) {
                for (int i = m.size() - 1; i >= (AbstractDungeon.currMapNode.y + 1); i--) {

                    s.append("Floor:").append(i + 1).append(" ");
                    for (MapRoomNode n : m.get(i)) {

                        if (n.hasEdges()) {
                            if (i > 0) {

                                s.append(nodeType(n)).append(n.x).append("{");
                                for (MapRoomNode child : m.get(i - 1)) {
                                    if (child.hasEdges() && child.isConnectedTo(n)) {
                                        s.append(child.x).append(",");
                                    }
                                }
                                s = new StringBuilder(s.substring(0, s.length() - 1));
                                s.append("} ");

                            } else {
                                s.append(nodeType(n)).append(n.x).append(" ");
                            }
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
                    limitedFloor = new StringBuilder("Floor:" + (i + 1) + " ");

                    for(MapRoomNode n : m.get(i)){
                        limitedNode = new StringBuilder();

                        for (MapRoomNode child : prev) {
                            if (child.isConnectedTo(n)) {
                                limitedNode.append(child.x).append(",");
                            }
                        }
                        if(limitedNode.length() > 0) {
                            limitedNode = new StringBuilder(nodeType(n) + n.x + "{" + limitedNode.substring(0, limitedNode.length() - 1) + "} ");
                            limitedFloor.append(limitedNode);
                            current.add(n);
                        }

                    }

                    prev.clear();
                    prev.addAll(current);
                    current.clear();
                    limitedMap.insert(0, limitedFloor + "\r\n");

                }

                s.append(limitedMap);

            }

            map.setText(s.toString());

        }else{
            map.setText(s.toString());
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
                return "EK-";
            else
                return "E-";
        }else if(n.getRoom() instanceof MonsterRoom){
            return "M-";
        }else if(n.getRoom() instanceof RestRoom){
            return "R-";
        }else if(n.getRoom() instanceof ShopRoom){
            return "S-";
        }else if(n.getRoom() instanceof TreasureRoom){
            return "T-";
        }else{
            return "U-";
        }
    }

}
