package textTheSpire;

import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;

public class Inspect {

    public Window inspect;

    public static ArrayList<MapRoomNode> inspected_map;
    public static boolean has_inspected = false;
    public static MapRoomNode destination;

    public static String paths;

    public Inspect(Display display){
        inspect = new Window(display,"Output",450,525);
    }

    public void setText(String s){
        inspect.setText(s);
    }

    public static String inspectMap(String[] tokens){
        int floor;
        int x;

        StringBuilder s = new StringBuilder();
        s.append("\r\n");

        ArrayList<ArrayList<MapRoomNode>> map = AbstractDungeon.map;
        ArrayList<ArrayList<MapRoomNode>> m;
        boolean differentSource;
        MapRoomNode source = null;

        if(tokens.length == 3){
            differentSource = false;
            try{
                floor = Integer.parseInt(tokens[1]);
                x = Integer.parseInt(tokens[2]);
                source = AbstractDungeon.currMapNode;
            } catch (Exception e){
                return s.toString();
            }
        }else{
            differentSource = true;
            try{
                floor = Integer.parseInt(tokens[3]);
                x = Integer.parseInt(tokens[4]);

                int source_floor = Integer.parseInt(tokens[1]);
                int source_x = Integer.parseInt(tokens[2]);

                for(MapRoomNode child : map.get(source_floor - 1)) {
                    if(child.x == source_x){
                        source = child;
                    }
                }

            } catch (Exception e){
                return s.toString();
            }
        }

        if(floor < 1 || floor > 15 || x < 0 || x > 6 || source == null)
            return "";

        int current_y = source.y;
        if(current_y >= 15) {
            current_y = -1;
        }

        if(!(current_y == -1)) {

            m = new ArrayList<ArrayList<MapRoomNode>>();

            ArrayList<MapRoomNode> current = new ArrayList<MapRoomNode>();
            current.add(source);
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

        ArrayList<MapRoomNode> inspected = new ArrayList<MapRoomNode>();

        if(current_y == -1)
            current_y = 0;

        if(floor - current_y - 1 < 0){
            return "";
        }

        for(MapRoomNode child : m.get(floor - current_y - 1)){
            if(child.x == x){
                prev.add(child);
                inspected.add(child);
                if(!differentSource)
                    destination = child;
                s.append("Floor ").append(floor).append("\r\n");
                s.append(Map.nodeType(child)).append(x).append("\r\n");
                break;
            }
        }

        if(prev.size() == 0)
            return s.toString();

        for(int i = (floor - current_y - 2);i>=0;i--){

            s.append("Floor ").append(i + current_y + 1).append("\r\n");

            for(MapRoomNode node : m.get(i)){

                for(MapRoomNode parent : prev){
                    if(node.isConnectedTo(parent)){
                        s.append(Map.nodeType(node)).append(node.x).append("\r\n");
                        curr.add(node);
                        inspected.add(node);
                        break;
                    }
                }

            }

            prev.clear();
            prev.addAll(curr);
            curr.clear();

        }

        if(!differentSource) {
            inspected_map = inspected;
            has_inspected = true;
        }

        return s.toString();

    }

    public static String inspectPaths(String[] tokens){

        int floor;
        int x;

        ArrayList<ArrayList<MapRoomNode>> map = AbstractDungeon.map;
        ArrayList<ArrayList<MapRoomNode>> m;
        MapRoomNode source = null;

        if(tokens.length == 3){
            try{
                floor = Integer.parseInt(tokens[1]);
                x = Integer.parseInt(tokens[2]);
                source = AbstractDungeon.currMapNode;
            } catch (Exception e){
                return "";
            }
        }else{
            try{
                floor = Integer.parseInt(tokens[3]);
                x = Integer.parseInt(tokens[4]);

                int source_floor = Integer.parseInt(tokens[1]);
                int source_x = Integer.parseInt(tokens[2]);

                for(MapRoomNode child : map.get(source_floor - 1)) {
                    if(child.x == source_x){
                        source = child;
                    }
                }

            } catch (Exception e){
                return "";
            }
        }

        if(floor < 1 || floor > 15 || x < 0 || x > 6 || source == null)
            return "";


        int current_y = source.y;
        if(current_y >= 15) {
            current_y = -1;
        }


        if(!(current_y == -1)) {

            m = new ArrayList<ArrayList<MapRoomNode>>();

            ArrayList<MapRoomNode> current = new ArrayList<MapRoomNode>();
            current.add(source);
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

        if(current_y == -1)
            current_y = 0;

        paths = "";

        if(floor - current_y - 1 < 0){
            return "";
        }

        for(MapRoomNode child : m.get(floor - current_y - 1)){
            if(child.x == x){
                pathRecursion(child, m, current_y, "");

                int num_lines = paths.split("\r\n").length;

                paths = "\r\nUnique Paths " + (num_lines/2) + paths;

                return paths;
            }
        }

        return "";

    }

    public static void pathRecursion(MapRoomNode curr, ArrayList<ArrayList<MapRoomNode>> m, int source_y, String path){

        String newPath = Map.nodeType(curr) + (curr.y+1) + " " + curr.x + ", " + path;

        if(curr.y - source_y - 1 >= 0) {

            for (MapRoomNode child : m.get(curr.y - source_y - 1)) {
                if (child.isConnectedTo(curr)) {
                    pathRecursion(child, m, source_y, newPath);
                }
            }

        } else {

            String[] tokens = newPath.split("\\s+");
            int unknown = 0;
            int monster = 0;
            int elite = 0;
            int emerald = 0;
            int rest = 0;
            int shop = 0;

            for (String s : tokens) {
                switch (s) {
                    case "Emerald":
                        emerald++;
                        break;
                    case "Elite":
                        elite++;
                        break;
                    case "Monster":
                        monster++;
                        break;
                    case "Rest":
                        rest++;
                        break;
                    case "Shop":
                        shop++;
                        break;
                    case "Unknown":
                        unknown++;
                        break;
                }
            }

            String pathEntry = "";
            if (emerald > 0) {
                pathEntry += "Emerald, ";
            }
            if (elite > 0) {
                pathEntry += "Elite " + elite + ", ";
            }
            if (rest > 0) {
                pathEntry += "Rest " + rest + ", ";
            }
            if (shop > 0) {
                pathEntry += "Shop " + shop + ", ";
            }
            if (unknown > 0) {
                pathEntry += "Unknown " + unknown + ", ";
            }
            if (monster > 0) {
                pathEntry += "Monster " + monster + ", ";
            }
            pathEntry += "\r\n" + newPath;

            paths = paths + "\r\n" + pathEntry;

        }
    }


    public static String downfallInspect(String[] tokens){

        int floor;
        int x;

        StringBuilder s = new StringBuilder();
        s.append("\r\n");

        ArrayList<ArrayList<MapRoomNode>> map = AbstractDungeon.map;
        ArrayList<ArrayList<MapRoomNode>> m;
        boolean differentSource;
        MapRoomNode source = null;

        if(tokens.length == 3){
            differentSource = false;
            try{
                floor = Integer.parseInt(tokens[1]);
                x = Integer.parseInt(tokens[2]);
                source = AbstractDungeon.currMapNode;
            } catch (Exception e){
                return s.toString();
            }
        }else{
            differentSource = true;
            try{
                floor = Integer.parseInt(tokens[3]);
                x = Integer.parseInt(tokens[4]);

                int source_floor = Integer.parseInt(tokens[1]);
                int source_x = Integer.parseInt(tokens[2]);

                for(MapRoomNode child : map.get(source_floor - 1)) {
                    if(child.x == source_x){
                        source = child;
                    }
                }

            } catch (Exception e){
                return s.toString();
            }
        }

        if(floor < 1 || floor > 15 || x < 0 || x > 6 || source == null)
            return "";

        int current_y = source.y;
        if(current_y == -1) {
            current_y = 15;
        }

        if(!(current_y == 15)) {

            m = new ArrayList<ArrayList<MapRoomNode>>();

            ArrayList<MapRoomNode> current = new ArrayList<MapRoomNode>();
            current.add(source);
            m.add(current);

            for (int i = (current_y - 1); i >= 0; i--) {

                ArrayList<MapRoomNode> next_floor = new ArrayList<MapRoomNode>();

                for (MapRoomNode n : map.get(i)) {

                    for (MapRoomNode child : m.get(0)) {
                        if (child.isConnectedTo(n)) {
                            next_floor.add(n);
                            break;
                        }
                    }

                }

                m.add(0, next_floor);

            }
        }else{
            m = map;
        }

        ArrayList<MapRoomNode> curr = new ArrayList<MapRoomNode>();
        ArrayList<MapRoomNode> prev = new ArrayList<MapRoomNode>();

        ArrayList<MapRoomNode> inspected = new ArrayList<MapRoomNode>();

        if(floor >= current_y){
            return s.toString();
        }

        for(MapRoomNode child : m.get(floor - 1)){
            if(child.x == x){
                prev.add(child);
                inspected.add(child);
                if(!differentSource)
                    destination = child;
                s.append("Floor ").append(floor).append("\r\n");
                s.append(Map.nodeType(child)).append(x).append("\r\n");
                break;
            }
        }

        if(prev.size() == 0)
            return s.toString();

        for(int i = floor;i<current_y;i++){

            s.append("Floor ").append(i + 1).append("\r\n");

            for(MapRoomNode node : m.get(i)){

                for(MapRoomNode parent : prev){
                    if(node.isConnectedTo(parent)){
                        s.append(Map.nodeType(node)).append(node.x).append("\r\n");
                        curr.add(node);
                        inspected.add(node);
                        break;
                    }
                }

            }

            prev.clear();
            prev.addAll(curr);
            curr.clear();

        }

        if(!differentSource) {
            inspected_map = inspected;
            has_inspected = true;
        }

        return s.toString();

    }

    public static String downfallPaths(String[] tokens){

        int floor;
        int x;

        ArrayList<ArrayList<MapRoomNode>> map = AbstractDungeon.map;
        ArrayList<ArrayList<MapRoomNode>> m;
        MapRoomNode source = null;

        if(tokens.length == 3){
            try{
                floor = Integer.parseInt(tokens[1]);
                x = Integer.parseInt(tokens[2]);
                source = AbstractDungeon.currMapNode;
            } catch (Exception e){
                return "";
            }
        }else{
            try{
                floor = Integer.parseInt(tokens[3]);
                x = Integer.parseInt(tokens[4]);

                int source_floor = Integer.parseInt(tokens[1]);
                int source_x = Integer.parseInt(tokens[2]);

                for(MapRoomNode child : map.get(source_floor - 1)) {
                    if(child.x == source_x){
                        source = child;
                    }
                }

            } catch (Exception e){
                return "";
            }
        }

        if(floor < 1 || floor > 15 || x < 0 || x > 6 || source == null)
            return "";


        int current_y = source.y;
        if(current_y <= -1) {
            current_y = 15;
        }

        if(!(current_y == 15)) {

            m = new ArrayList<ArrayList<MapRoomNode>>();

            ArrayList<MapRoomNode> current = new ArrayList<MapRoomNode>();
            current.add(source);
            m.add(current);

            for (int i = (current_y - 1); i >= 0; i--) {

                ArrayList<MapRoomNode> next_floor = new ArrayList<MapRoomNode>();

                for (MapRoomNode n : map.get(i)) {

                    for (MapRoomNode child : m.get(0)) {
                        if (child.isConnectedTo(n)) {
                            next_floor.add(n);
                            break;
                        }
                    }

                }

                m.add(0, next_floor);

            }
        }else{
            m = map;
        }

        if(current_y == 15)
            current_y = 14;

        paths = "";

        if(floor >= current_y){
            return "";
        }

        for(MapRoomNode child : m.get(floor - 1)){
            if(child.x == x){
                downfallRecursion(child, m, current_y, "");

                int num_lines = paths.split("\r\n").length;

                paths = "\r\nUnique Paths " + (num_lines/2) + paths;

                return paths;
            }
        }

        return "";

    }

    public static void downfallRecursion(MapRoomNode curr, ArrayList<ArrayList<MapRoomNode>> m, int source_y, String path){

        String newPath = Map.nodeType(curr) + (curr.y+1) + " " + curr.x + ", " + path;

        if(source_y - curr.y - 1 >= 0) {

            for (MapRoomNode child : m.get(curr.y + 1)) {
                if (child.isConnectedTo(curr)) {
                    downfallRecursion(child, m, source_y, newPath);
                }
            }

        } else {

            String[] tokens = newPath.split("\\s+");
            int unknown = 0;
            int monster = 0;
            int elite = 0;
            int emerald = 0;
            int rest = 0;
            int shop = 0;

            for (String s : tokens) {
                switch (s) {
                    case "Emerald":
                        emerald++;
                        break;
                    case "Elite":
                        elite++;
                        break;
                    case "Monster":
                        monster++;
                        break;
                    case "Rest":
                        rest++;
                        break;
                    case "Shop":
                        shop++;
                        break;
                    case "Unknown":
                        unknown++;
                        break;
                }
            }

            String pathEntry = "";
            if (emerald > 0) {
                pathEntry += "Emerald, ";
            }
            if (elite > 0) {
                pathEntry += "Elite " + elite + ", ";
            }
            if (rest > 0) {
                pathEntry += "Rest " + rest + ", ";
            }
            if (shop > 0) {
                pathEntry += "Shop " + shop + ", ";
            }
            if (unknown > 0) {
                pathEntry += "Unknown " + unknown + ", ";
            }
            if (monster > 0) {
                pathEntry += "Monster " + monster + ", ";
            }
            pathEntry += "\r\n" + newPath;

            paths = paths + "\r\n" + pathEntry;

        }
    }


}












