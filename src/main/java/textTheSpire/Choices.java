package textTheSpire;

import ascensionMod.AscensionMod;
import ascensionMod.UI.AscModScreen;
import ascensionMod.UI.CharSelectScreenUI;
import basemod.CustomCharacterSelectScreen;
import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.characters.TheSilent;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.daily.DailyScreen;
import com.megacrit.cardcrawl.daily.TimeHelper;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.GenericEventDialog;
import com.megacrit.cardcrawl.events.shrines.GremlinMatchGame;
import com.megacrit.cardcrawl.events.shrines.GremlinWheelGame;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.integrations.DistributorFactory;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.metrics.Metrics;
import com.megacrit.cardcrawl.mod.replay.monsters.replay.FadingForestBoss;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.ShopRoom;
import com.megacrit.cardcrawl.screens.charSelect.CharacterOption;
import com.megacrit.cardcrawl.screens.charSelect.CharacterSelectScreen;
import com.megacrit.cardcrawl.screens.leaderboards.LeaderboardEntry;
import com.megacrit.cardcrawl.screens.mainMenu.*;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryScreen;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;
import com.megacrit.cardcrawl.screens.stats.*;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import com.megacrit.cardcrawl.ui.buttons.LargeDialogOptionButton;
import com.megacrit.cardcrawl.ui.panels.SeedPanel;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import communicationmod.ChoiceScreenUtils;
import communicationmod.CommandExecutor;
import communicationmod.CommunicationMod;
import communicationmod.patches.GremlinMatchGamePatch;
import conspire.events.MimicChestEvent;
import downfall.patches.EvilModeCharacterSelect;
import downfall.patches.MainMenuEvilMode;
import downfall.rooms.HeartShopRoom;
import downfall.util.RemoveCardReward;
import downfall.util.TransformCardReward;
import downfall.util.UpgradeCardReward;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import replayTheSpire.patches.ReplayShopInitCardsPatch;
import shopmod.relics.MerchantsRug;

import javax.smartcardio.Card;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class Choices extends AbstractWindow{

    boolean disableTips;

    public enum HistoryScreen{
        NONE, MAIN, LIST, INSPECT, DECK, RELIC, PATH;
    }

    static class Floor{
        public String type;
        public boolean wasUnknown;
        public ArrayList<CardChoiceStats> cardChoices;
        public ArrayList<String> potions;
        public ArrayList<String> relics;
        public ArrayList<String> purges;
        public ArrayList<String> purchases;
        public EventStats event;
        public BattleStats battle;
        public CampfireChoice campfire;
        public int currentHP;
        public int maxHP;
        public BossRelicChoiceStats boss;
        public int gold;
    };

    HistoryScreen screen;
    RunData inspectRun;
    String savedFilter;

    SimpleDateFormat dateFormat;
    boolean setFormat;

    public static int doubleIndex;

    public boolean includeGold;
    public boolean includeHealth;
    public boolean includeCard;
    public boolean includeRelics;
    public boolean includePotions;
    public boolean includePurges;
    public boolean includePurchases;
    public boolean includeEvents;
    public boolean includeBattles;
    public boolean includeCampfire;
    public boolean includeBosses;

    public Choices(Display display){

        disableTips = false;
        screen = HistoryScreen.NONE;
        savedFilter = "";
        setFormat = false;
        isVisible = true;
        window = new Window(display,"Choices", 300, 400);

        doubleIndex = -1;

        includeGold = true;
        includeHealth = true;
        includeCard = true;
        includeRelics = true;
        includePotions = true;
        includePurges = true;
        includePurchases = true;
        includeEvents = true;
        includeBattles = true;
        includeCampfire = true;
        includeBosses = true;
    }

    @SuppressWarnings("unchecked")
    public String getText(){

        if(window.shell.isDisposed()){
            Display.getDefault().dispose();
            Gdx.app.exit();
        }

        doubleIndex = -1;

        if(!setFormat && CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.runHistoryScreen != null){
            if (Settings.language == Settings.GameLanguage.JPN) {
                dateFormat = new SimpleDateFormat(RunHistoryScreen.TEXT[34], Locale.JAPAN);
            } else {
                dateFormat = new SimpleDateFormat(RunHistoryScreen.TEXT[34]);
            }
            setFormat = true;
        }

        StringBuilder s = new StringBuilder();
        s.append("\r\n");

        if(screen != HistoryScreen.NONE) {


            switch(screen){


                case MAIN:

                    s.append("Run History\r\nMost commands are disabled when viewing run history.\r\n");
                    s.append("Total runs: ").append(TextTheSpire.runList.size()).append("\r\n");
                    s.append("Filters:\r\n");
                    s.append("1. Wins ").append(TextTheSpire.include_win).append("\r\n");
                    s.append("2. Loses ").append(TextTheSpire.include_lose).append("\r\n");
                    s.append("3. Ironclad ").append(TextTheSpire.include_iron).append("\r\n");
                    s.append("4. Silent ").append(TextTheSpire.include_silent).append("\r\n");
                    s.append("5. Defect ").append(TextTheSpire.include_defect).append("\r\n");
                    s.append("6. Watcher ").append(TextTheSpire.include_watch).append("\r\n");
                    s.append("7. Normal Runs ").append(TextTheSpire.include_normal).append("\r\n");
                    s.append("8. Ascension ").append(TextTheSpire.include_asc).append("\r\n");
                    s.append("9. Daily ").append(TextTheSpire.include_daily).append("\r\n");
                    s.append("Input a single number to toggle a filter.\r\n");
                    s.append("Input view to view list of runs.\r\n");
                    s.append("At any point input close to exit Run History and return to main menu.\r\n");
                    break;

                case LIST:

                    if(!savedFilter.isEmpty()){
                        return savedFilter;
                    }
                    s.append("back\r\nFiltered runs: ").append(TextTheSpire.runFiltered.size()).append("\r\n");
                    s.append("Run are displayed with date followed by score.\r\n");
                    s.append("Inspect a run with its index.\r\n");
                    s.append("Runs:\r\n");
                    for(int i=0;i<TextTheSpire.runFiltered.size();i++){
                        RunData run = TextTheSpire.runFiltered.get(i);
                        String dateTimeStr;
                        if (run.local_time != null) {
                            try {
                                dateTimeStr = dateFormat.format(Metrics.timestampFormatter.parse(run.local_time));
                            } catch (Exception ignored) {
                                dateTimeStr = "date error";
                            }
                        } else {
                            dateTimeStr = dateFormat.format(Long.parseLong(run.timestamp) * 1000L);
                        }
                        s.append(i).append(". ").append(dateTimeStr).append(", ").append(run.score).append("\r\n");
                    }
                    savedFilter = s.toString();
                    break;

                case INSPECT:

                    String dateTimeStr;
                    if (inspectRun.local_time != null) {
                        try {
                            dateTimeStr = dateFormat.format(Metrics.timestampFormatter.parse(inspectRun.local_time));
                        } catch (Exception ignored) {
                            dateTimeStr = "date error";
                        }
                    } else {
                        dateTimeStr = dateFormat.format(Long.parseLong(inspectRun.timestamp) * 1000L);
                    }
                    s.append(dateTimeStr).append("\r\n").append("Score: ").append(inspectRun.score).append("\r\n");
                    s.append(inspectRun.character_chosen).append("\r\n");
                    s.append("Seed: ").append(SeedHelper.getString(Long.parseLong(inspectRun.seed_played))).append("\r\n");
                    if(inspectRun.is_daily){
                        s.append("Daily Mods:\r\n");
                        for(String mod : inspectRun.daily_mods){
                            s.append(mod).append("\r\n");
                        }
                    }
                    if(inspectRun.is_ascension_mode){
                        s.append("Ascension: ").append(inspectRun.ascension_level).append("\r\n");
                    }
                    s.append("Victory: ").append(inspectRun.victory).append("\r\n");
                    if(inspectRun.victory) {
                        s.append("Time: ").append((inspectRun.playtime / 3600)).append("hr ").append((inspectRun.playtime / 60)).append("min ").append((inspectRun.playtime % 60)).append("sec\r\n");
                    }else{
                        s.append("Killed by: ").append(inspectRun.killed_by).append("\r\n");
                    }
                    s.append("Floor Reached: ").append(inspectRun.floor_reached).append("\r\n");
                    s.append("Neow Bonus: ").append(inspectRun.neow_bonus).append("\r\n");
                    s.append("Neow Cost: ").append(inspectRun.neow_cost).append("\r\n");
                    s.append("Options:\r\n");
                    s.append("1. Master Deck\r\n");
                    s.append("2. Relics\r\n");
                    s.append("3. Path\r\n");
                    s.append("Path Options:\r\n");
                    s.append("4. Gold per turn ").append(includeGold).append("\r\n");
                    s.append("5. Health per turn ").append(includeHealth).append("\r\n");
                    s.append("6. Card Choices ").append(includeCard).append("\r\n");
                    s.append("7. Relics Obtained ").append(includeRelics).append("\r\n");
                    s.append("8. Potions Obtained ").append(includePotions).append("\r\n");
                    s.append("9. Items Purchased ").append(includePurchases).append("\r\n");
                    s.append("10. Cards Purged ").append(includePurges).append("\r\n");
                    s.append("11. Events ").append(includeEvents).append("\r\n");
                    s.append("12. Battle Details ").append(includeBattles).append("\r\n");
                    s.append("13. Campfire Choices ").append(includeCampfire).append("\r\n");
                    s.append("14. Boss Relic Choices").append(includeBosses).append("\r\n");

                    break;

                case DECK:

                    s.append("back\r\nMaster Deck\r\n");
                    for(String card : inspectRun.master_deck){
                        s.append(card).append("\r\n");
                    }
                    break;

                case RELIC:

                    s.append("back\r\nRelics\r\n");
                    for(String relic : inspectRun.relics){
                        s.append(relic).append("\r\n");
                    }
                    break;


                case PATH:

                    ArrayList<Floor> every = new ArrayList<>();
                    for(int i=0;i<inspectRun.gold_per_floor.size();i++){
                        Floor newFloor = new Floor();
                        newFloor.gold = inspectRun.gold_per_floor.get(i);
                        newFloor.currentHP = inspectRun.current_hp_per_floor.get(i);
                        newFloor.maxHP = inspectRun.max_hp_per_floor.get(i);
                        newFloor.type = inspectRun.path_per_floor.get(i);
                        newFloor.relics = new ArrayList<>();
                        newFloor.potions = new ArrayList<>();
                        newFloor.cardChoices = new ArrayList<>();
                        newFloor.purchases = new ArrayList<>();
                        newFloor.purges = new ArrayList<>();
                        every.add(newFloor);
                    }
                    for(int i=0;i<inspectRun.path_taken.size();i++){
                        every.get(i + i/16).wasUnknown = inspectRun.path_taken.get(i).equals("?");
                    }
                    for(ObtainStats p : inspectRun.relics_obtained){
                        every.get(p.floor-1).relics.add(p.key);
                    }
                    for(ObtainStats p : inspectRun.potions_obtained){
                        every.get(p.floor-1).potions.add(p.key);
                    }
                    for(int i=0;i<inspectRun.items_purged_floors.size();i++){
                        every.get(inspectRun.items_purged_floors.get(i)-1).purges.add(inspectRun.items_purged.get(i));
                    }
                    for(int i=0;i<inspectRun.item_purchase_floors.size();i++){
                        every.get(inspectRun.item_purchase_floors.get(i)-1).purchases.add(inspectRun.items_purchased.get(i));
                    }
                    for(CardChoiceStats c : inspectRun.card_choices){
                        every.get(c.floor-1).cardChoices.add(c);
                    }
                    for(BattleStats b : inspectRun.damage_taken){
                        every.get(b.floor-1).battle = b;
                    }
                    for(EventStats e : inspectRun.event_choices){
                        every.get(e.floor-1).event = e;
                    }
                    for(CampfireChoice c : inspectRun.campfire_choices){
                        every.get(c.floor-1).campfire = c;
                    }
                    for(int i=0;i<inspectRun.boss_relics.size();i++){
                        every.get(((i+1)*17)-1).boss = inspectRun.boss_relics.get(i);
                    }
                    s.append("back\r\nPath\r\n");
                    for(int i=0;i<every.size();i++){
                        s.append("Floor: ").append(i+1).append("\r\n");
                        if(!every.get(i).wasUnknown)
                            s.append("Type: ").append(mapType(every.get(i).type));
                        else
                            s.append("Type: Unknown to ").append(mapType(every.get(i).type));
                        if(every.get(i).battle != null)
                            s.append(": ").append(every.get(i).battle.enemies);
                        s.append("\r\n");
                        if(includeHealth)
                            s.append("Health: ").append(every.get(i).currentHP).append("/").append(every.get(i).maxHP).append("\r\n");
                        if(includeGold)
                            s.append("Gold: ").append(every.get(i).gold).append("\r\n");
                        if(every.get(i).battle != null && includeBattles){
                            s.append("Damage: ").append(every.get(i).battle.damage);
                            s.append(", Turns: ").append(every.get(i).battle.turns).append("\r\n");
                        }
                        if(every.get(i).event != null && includeEvents){
                            s.append(every.get(i).event.event_name).append("\r\n");
                            if(!every.get(i).event.player_choice.isEmpty())
                                s.append("Choice: ").append(every.get(i).event.player_choice).append("\r\n");
                            if(every.get(i).event.cards_obtained != null && every.get(i).event.cards_obtained.size() > 0){
                                s.append("Cards Obtained: ");
                                for(String obt : every.get(i).event.cards_obtained){
                                    s.append(obt).append(", ");
                                }
                                s.append("\r\n");
                            }
                            if(every.get(i).event.cards_removed != null && every.get(i).event.cards_removed.size() > 0){
                                s.append("Cards Removed: ");
                                for(String obt : every.get(i).event.cards_removed){
                                    s.append(obt).append(", ");
                                }
                                s.append("\r\n");
                            }
                            if(every.get(i).event.cards_transformed != null && every.get(i).event.cards_transformed.size() > 0){
                                s.append("Cards Transformed: ");
                                for(String obt : every.get(i).event.cards_transformed){
                                    s.append(obt).append(", ");
                                }
                                s.append("\r\n");
                            }
                            if(every.get(i).event.cards_upgraded != null && every.get(i).event.cards_upgraded.size() > 0){
                                s.append("Cards Upgraded: ");
                                for(String obt : every.get(i).event.cards_upgraded){
                                    s.append(obt).append(", ");
                                }
                                s.append("\r\n");
                            }
                            if(every.get(i).event.relics_obtained != null && every.get(i).event.relics_obtained.size() > 0){
                                s.append("Relics Obtained: ");
                                for(String obt : every.get(i).event.relics_obtained){
                                    s.append(obt).append(", ");
                                }
                                s.append("\r\n");
                            }
                            if(every.get(i).event.relics_lost != null && every.get(i).event.relics_lost.size() > 0){
                                s.append("Relics Lost: ");
                                for(String obt : every.get(i).event.relics_lost){
                                    s.append(obt).append(", ");
                                }
                                s.append("\r\n");
                            }
                            if(every.get(i).event.potions_obtained != null && every.get(i).event.potions_obtained.size() > 0){
                                s.append("Potions Obtained: ");
                                for(String obt : every.get(i).event.potions_obtained){
                                    s.append(obt).append(", ");
                                }
                                s.append("\r\n");
                            }
                            if(every.get(i).event.damage_taken > 0)
                                s.append("Damaged Taken: ").append(every.get(i).event.damage_taken).append("\r\n");
                            if(every.get(i).event.damage_healed > 0)
                                s.append("Damaged Healed: ").append(every.get(i).event.damage_healed).append("\r\n");
                            if(every.get(i).event.max_hp_loss > 0)
                                s.append("Max HP Loss: ").append(every.get(i).event.max_hp_loss).append("\r\n");
                            if(every.get(i).event.max_hp_gain > 0)
                                s.append("Max HP Gain: ").append(every.get(i).event.max_hp_gain).append("\r\n");
                            if(every.get(i).event.gold_gain > 0)
                                s.append("Gold Gain: ").append(every.get(i).event.gold_gain).append("\r\n");
                            if(every.get(i).event.gold_loss > 0)
                                s.append("Gold Loss: ").append(every.get(i).event.gold_loss).append("\r\n");
                        }
                        if(every.get(i).campfire != null && includeCampfire){
                            s.append("Campfire: ").append(every.get(i).campfire.key);
                            if(every.get(i).campfire.data != null)
                                s.append(": ").append(every.get(i).campfire.data);
                            s.append("\r\n");
                        }
                        if(every.get(i).boss != null && includeBosses){
                            s.append("Picked: ").append(every.get(i).boss.picked).append("\r\n");
                            s.append("Rejected: ");
                            for(String rej : every.get(i).boss.not_picked){
                                s.append(rej).append(", ");
                            }
                            s.append("\r\n");
                        }
                        if(every.get(i).purchases.size() > 0 && includePurchases){
                            s.append("Purchases: ");
                            for(String p : every.get(i).purchases){
                                s.append(p).append(", ");
                            }
                            s.append("\r\n");
                        }
                        if(every.get(i).purges.size() > 0 && includePurges){
                            s.append("Purges: ");
                            for(String p : every.get(i).purges){
                                s.append(p).append(", ");
                            }
                            s.append("\r\n");
                        }
                        if(every.get(i).cardChoices.size() > 0 && includeCard){
                            s.append("Card Choices\r\n");
                            for(CardChoiceStats c : every.get(i).cardChoices){
                                s.append("Picked: ").append(c.picked).append("\r\n");
                                s.append("Rejected: ");
                                for(String rej : c.not_picked){
                                    s.append(rej).append(", ");
                                }
                                s.append("\r\n");
                            }
                        }
                        if(every.get(i).relics.size() > 0 && includeRelics){
                            s.append("Relics Obtained: ");
                            for(String p : every.get(i).relics){
                                s.append(p).append(", ");
                            }
                            s.append("\r\n");
                        }
                        if(every.get(i).potions.size() > 0 && includePotions){
                            s.append("Potions Obtained: ");
                            for(String p : every.get(i).potions){
                                s.append(p).append(", ");
                            }
                            s.append("\r\n");
                        }
                    }
                    break;

            }

            return s.toString();

        } else if(CommandExecutor.isInDungeon()){

            ChoiceScreenUtils.ChoiceType currChoice = ChoiceScreenUtils.getCurrentChoiceType();

            if(TextTheSpire.downfall && EvilModeCharacterSelect.evilMode && AbstractDungeon.getCurrRoom() instanceof HeartShopRoom && ChoiceScreenUtils.getCurrentChoiceType() == ChoiceScreenUtils.ChoiceType.SHOP_ROOM && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT){
                return "";
            }

            if(currChoice == ChoiceScreenUtils.ChoiceType.HAND_SELECT){
                s.append("Hand Selection\r\n");
                s.append(AbstractDungeon.handCardSelectScreen.selectionReason + "\r\n");
                s.append("Select " + AbstractDungeon.handCardSelectScreen.numCardsToSelect + "\r\n");
                s.append("Number Selected: " + AbstractDungeon.handCardSelectScreen.numSelected + "\r\n");
            }else if(currChoice == ChoiceScreenUtils.ChoiceType.GRID){
                s.append("Grid Selection\r\n");
                s.append("Number Selected: " + AbstractDungeon.gridSelectScreen.selectedCards.size() + "\r\n");
                if(AbstractDungeon.gridSelectScreen.forUpgrade && AbstractDungeon.gridSelectScreen.upgradePreviewCard != null){
                    AbstractCard preview = AbstractDungeon.gridSelectScreen.upgradePreviewCard;
                    s.append("Upgrade Preview : " + TextTheSpire.inspectCard(preview));
                }
            }

            //If in combat check if choices exists, otherwise remove window
            if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT){

                if (ChoiceScreenUtils.isConfirmButtonAvailable()) {
                    s.append(ChoiceScreenUtils.getConfirmButtonText()).append("\r\n");
                }
                if (ChoiceScreenUtils.isCancelButtonAvailable()) {
                    s.append(ChoiceScreenUtils.getCancelButtonText()).append("\r\n");
                }
                int count = 1;

                if(TextTheSpire.replayTheSpire && AbstractDungeon.getCurrRoom().monsters.monsters.get(AbstractDungeon.getCurrRoom().monsters.monsters.size()-1) instanceof FadingForestBoss){
                    boolean show = (boolean)basemod.ReflectionHacks.getPrivateStatic(GenericEventDialog.class, "show");
                    if(show){
                        String name = (String) ReflectionHacks.getPrivate(((FadingForestBoss) AbstractDungeon.getCurrRoom().monsters.monsters.get(AbstractDungeon.getCurrRoom().monsters.monsters.size()-1)).imageEventText, GenericEventDialog.class, "title");
                        s.append(name).append("\r\n");
                        ArrayList<LargeDialogOptionButton> buttons = ((FadingForestBoss) AbstractDungeon.getCurrRoom().monsters.monsters.get(AbstractDungeon.getCurrRoom().monsters.monsters.size()-1)).imageEventText.optionList;
                        ArrayList<LargeDialogOptionButton> activeButtons = new ArrayList<>();
                        for(LargeDialogOptionButton b : buttons){
                            if(!b.isDisabled){
                                activeButtons.add(b);
                            }
                        }
                        if (activeButtons.size() > 0) {
                            for(LargeDialogOptionButton button : activeButtons) {
                                s.append(count).append(": ").append(stripColor(button.msg).toLowerCase()).append("\r\n");
                                count++;
                            }
                            return s.toString();
                        }
                    }
                }

                ArrayList<String> cards = ChoiceScreenUtils.getCurrentChoiceList();

                if (cards.size() == 0) {
                    return s.toString();
                }

                for (String c : cards) {

                    s.append(count).append(":").append(c).append("\r\n");
                    count++;

                }

                return s.toString();

            }else{

                //If not in combat, check and display choices

                int count = 1;

                if (ChoiceScreenUtils.isConfirmButtonAvailable()) {
                    s.append(ChoiceScreenUtils.getConfirmButtonText()).append("\r\n");
                }
                if (ChoiceScreenUtils.isCancelButtonAvailable()) {
                    s.append(ChoiceScreenUtils.getCancelButtonText()).append("\r\n");
                }

                //Event choices
                if (ChoiceScreenUtils.getCurrentChoiceType() == ChoiceScreenUtils.ChoiceType.EVENT) {

                    if(TextTheSpire.conspire && AbstractDungeon.getCurrRoom().event instanceof MimicChestEvent) {
                        s.append("\r\nproceed\r\n1:open\r\n");
                        return s.toString();
                    }

                    s.append(AbstractDungeon.getCurrRoom().event.getClass().getSimpleName()).append("\r\n");

                    ArrayList<LargeDialogOptionButton> activeButtons = ChoiceScreenUtils.getActiveEventButtons();

                    if (activeButtons.size() > 0) {
                        for(LargeDialogOptionButton button : activeButtons) {
                            s.append(count).append(": ").append(stripColor(button.msg).toLowerCase()).append("\r\n");
                            count++;
                        }
                    } else if(AbstractDungeon.getCurrRoom().event instanceof GremlinWheelGame) {
                        s.append(count).append(": ").append("spin").append("\r\n");
                    } else if(AbstractDungeon.getCurrRoom().event instanceof GremlinMatchGame) {
                        GremlinMatchGame event = (GremlinMatchGame) (AbstractDungeon.getCurrRoom().event);
                        CardGroup gameCardGroup = (CardGroup) ReflectionHacks.getPrivate(event, GremlinMatchGame.class, "cards");
                        for (AbstractCard c : gameCardGroup.group) {
                            if (c.isFlipped) {
                                s.append(count).append(": ").append(String.format("card%d", GremlinMatchGamePatch.cardPositions.get(c.uuid))).append("\r\n");
                                count++;
                            }
                        }
                    }

                } else if (ChoiceScreenUtils.getCurrentChoiceType() == ChoiceScreenUtils.ChoiceType.SHOP_SCREEN) {

                    for (String c : priceShopScreenChoices()) {
                        s.append(count).append(":").append(c).append("\r\n");
                        count++;
                    }
                    if(TextTheSpire.shopMod && MerchantsRug.forSale && AbstractDungeon.player.gold >= MerchantsRug.price){
                        s.append("rug:MerchantsRug.price");
                    }

                } else if (ChoiceScreenUtils.getCurrentChoiceType() == ChoiceScreenUtils.ChoiceType.MAP){

                    //Also shows current position
                    if (AbstractDungeon.firstRoomChosen) {
                        s.append("Floor:").append(AbstractDungeon.currMapNode.y + 1).append(", X:").append(AbstractDungeon.currMapNode.x).append("\r\n");
                    }else {
                        if(TextTheSpire.downfall && EvilModeCharacterSelect.evilMode)
                            s.append("Floor 16\r\n");
                        else
                            s.append("Floor:0\r\n");
                    }

                    ArrayList<MapRoomNode> choices;

                    if(TextTheSpire.downfall && EvilModeCharacterSelect.evilMode)
                        choices = Map.getMapScreenNodeChoices();
                    else
                        choices = ChoiceScreenUtils.getMapScreenNodeChoices();

                    if ((ChoiceScreenUtils.bossNodeAvailable() && !TextTheSpire.downfall) || (TextTheSpire.downfall && AbstractDungeon.getCurrMapNode().y == 0)) {

                        s.append(count).append(":");
                        s.append("boss").append("\r\n");

                    } else if(!Inspect.has_inspected) {

                        //Displays node type and xPos for each choice
                        for (MapRoomNode n : choices) {
                            s.append(count).append(":");
                            if(AbstractDungeon.player.hasRelic("WingedGreaves") && (AbstractDungeon.player.getRelic("WingedGreaves")).counter > 0 && !AbstractDungeon.getCurrMapNode().isConnectedTo(n)) {
                                s.append(Map.nodeType(n)).append("Winged ").append(n.x).append("\r\n");
                            } else {
                                s.append(Map.nodeType(n)).append(n.x).append("\r\n");
                            }
                            count++;
                        }

                    }else{

                        s.append("Inspected ").append(Map.nodeType(Inspect.destination)).append(Inspect.destination.y + 1).append(" ").append(Inspect.destination.x).append("\r\n");

                        for (MapRoomNode n : choices) {
                            s.append(count).append(":");
                            s.append(Map.nodeType(n));
                            if (Inspect.inspected_map.contains(n)) {
                                s.append("On Track ").append(n.x).append("\r\n");
                            }else if(AbstractDungeon.player.hasRelic("WingedGreaves") && (AbstractDungeon.player.getRelic("WingedGreaves")).counter > 0 && !AbstractDungeon.getCurrMapNode().isConnectedTo(n)){
                                s.append("Winged ").append(n.x).append("\r\n");
                            }else{
                                s.append("Diverge ").append(n.x).append("\r\n");
                            }

                            count++;
                        }

                    }

                } else if(ChoiceScreenUtils.getCurrentChoiceType() == ChoiceScreenUtils.ChoiceType.COMBAT_REWARD) {
                    for(RewardItem reward : AbstractDungeon.combatRewardScreen.rewards) {
                        if(TextTheSpire.downfall){
                            if(reward instanceof RemoveCardReward){
                                s.append(count).append(":").append("remove\r\n");
                                count++;
                                continue;
                            } else if(reward instanceof UpgradeCardReward){
                                s.append(count).append(":").append("upgrade\r\n");
                                count++;
                                continue;
                            } else if(reward instanceof TransformCardReward){
                                s.append(count).append(":").append("transform\r\n");
                                count++;
                                continue;
                            }
                        }
                        if(reward.type == RewardItem.RewardType.POTION)
                            s.append(count).append(":").append(reward.potion.name).append("\r\n");
                        else if(reward.type == RewardItem.RewardType.RELIC)
                            s.append(count).append(":").append(reward.relic.name).append("\r\n");
                        else
                            s.append(count).append(":").append(reward.type.name().toLowerCase()).append("\r\n");
                        count++;
                    }
                } else {
                    //Catch all for all remaining choices. They are usually displayed in a list with numbers a simple name
                    for (String c : ChoiceScreenUtils.getCurrentChoiceList()) {
                        s.append(count).append(":").append(c).append("\r\n");
                        count++;
                    }
                }

                return s.toString();

            }

        }else {

            //Not in dungeon. Check if save exists. checkedSave so we don't check each time.
            if (CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.MAIN_MENU) {

                if (!disableTips) {
                    TipTracker.disableAllFtues();
                    disableTips = true;
                }

                s.append("Text The Spire v" + TextTheSpire.VERSION +  "\r\ntts : patch notes\r\n");

                s.append("Slot ").append(CardCrawlGame.saveSlot).append(" ").append(CardCrawlGame.playerName).append("\r\n");

                for (AbstractPlayer.PlayerClass p : AbstractPlayer.PlayerClass.values()) {

                    s.append(p.name().toLowerCase()).append(" ");

                    if (TextTheSpire.characterUnlocked(p.name().toLowerCase())) {
                        s.append(TextTheSpire.ascensionLevel(p)).append("\r\n");
                    } else
                        s.append("locked\r\n");

                }

                s.append("Commands\r\n");

                if (CardCrawlGame.mainMenuScreen.buttons.get(CardCrawlGame.mainMenuScreen.buttons.size() - 2).result == MenuButton.ClickResult.ABANDON_RUN) {
                    s.append("abandon\r\n");
                    s.append("continue\r\n");
                } else {
                    s.append("play\r\n");
                    if (DistributorFactory.isLeaderboardEnabled()) {
                        s.append("leader\r\n");
                    }
                }

                s.append("history\r\nslot\r\npatch\r\nquit");

            }else if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.PANEL_MENU){
                MenuPanelScreen.PanelScreen ps = (MenuPanelScreen.PanelScreen) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.panelScreen, MenuPanelScreen.class, "screen");
                if(TextTheSpire.downfall && ps == MainMenuEvilMode.Enums.EVIL){
                    s.append("standard\r\ndownfall\r\n");
                } else {
                    s.append("standard\r\n");
                    if (CardCrawlGame.mainMenuScreen.statsScreen.statScreenUnlocked()) {
                        s.append("daily\r\n");
                    }
                    if (StatsScreen.all.highestDaily > 0) {
                        s.append("custom\r\n");
                    }
                    s.append("back\r\n");
                }
            }else if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.CHAR_SELECT){

                if(CardCrawlGame.mainMenuScreen.charSelectScreen instanceof CustomCharacterSelectScreen) {
                    ArrayList<CharacterOption> allOptions = ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CustomCharacterSelectScreen.class, "allOptions");
                    if (CardCrawlGame.mainMenuScreen.charSelectScreen.options.size() != allOptions.size()) {
                        CardCrawlGame.mainMenuScreen.charSelectScreen.options = allOptions;
                    }
                }

                s.append("Input character name to select.\r\nasc to toggle ascension.\r\nasc [number] to set ascension level.\r\nIf that fails you can use + and - to manually change ascension level.\r\n");
                s.append("back\r\n");
                ArrayList<CharacterOption> options = CardCrawlGame.mainMenuScreen.charSelectScreen.options;
                for(CharacterOption co : options){
                    s.append(co.c.getClass().getSimpleName().toLowerCase());
                    if(co.selected) {
                        s.append(" Selected");
                    }
                    s.append("\r\n");
                }
                SeedPanel sp = (SeedPanel) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CharacterSelectScreen.class, "seedPanel");
                if(sp.shown){
                    s.append("seed\r\n");
                }
                boolean ready = (boolean) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CharacterSelectScreen.class, "anySelected");
                boolean asc = (boolean) basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.charSelectScreen, CharacterSelectScreen.class, "isAscensionModeUnlocked");
                if(asc && ready){
                    s.append("asc ");
                    if(CardCrawlGame.mainMenuScreen.charSelectScreen.isAscensionMode)
                        s.append(CardCrawlGame.mainMenuScreen.charSelectScreen.ascensionLevel).append("\r\n");
                    else
                        s.append("off\r\n");
                    if(TextTheSpire.ascensionReborn){
                        s.append("c_asc ").append(AscensionMod.customAscensionRun).append("\r\n");
                        if(AscensionMod.customAscensionRun){
                            s.append("open\r\n");
                        }
                    }
                }
                if(ready)
                    s.append("embark\r\n");
            }else if(TextTheSpire.ascensionReborn && CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == AscModScreen.Enum.ASC_MOD){
                s.append("Custom Ascension Screen\r\nback\r\nInput number to toggle ascension.\r\nUse ascension command to check what the modifiers do.\r\n");
                for(int i=CharSelectScreenUI.ascScreen.posAscButtons.size()-1; i>=0; i--){
                    s.append((i+1)).append(" : ").append(CharSelectScreenUI.ascScreen.posAscButtons.get(i).toggledOn).append("\r\n");
                }
                for(int i=0; i<CharSelectScreenUI.ascScreen.negAscButtons.size(); i++){
                    s.append(((i*-1)-1)).append(" : ").append(CharSelectScreenUI.ascScreen.negAscButtons.get(i).toggledOn).append("\r\n");
                }
            }else if(CardCrawlGame.mainMenuScreen != null && (CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.DAILY || CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.CUSTOM)){
                s.append("embark\r\nback\r\n" +
                        "Explanation:\r\n" +
                        "Use the Custom window to toggle custom mode options.\r\n" +
                        "char [number] to toggle character\r\n" +
                        "asc to toggle ascension\r\n" +
                        "asc [number] to select ascension level\r\n" +
                        "seed to open the seed panel. Go to main gain window, paste the seed, and hit enter.\r\n" +
                        "mod [number] to toggle a custom modifier\r\n" +
                        "mod i [number] to inspect what a custom modifer is\r\n" +
                        "simple to display a simplified version of current settings to Output\r\n");
                if(CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.DAILY){
                    long day = (long)basemod.ReflectionHacks.getPrivate(CardCrawlGame.mainMenuScreen.dailyScreen, DailyScreen.class, "currentDay");
                    s.append("Daily Leaderboard ").append(TimeHelper.getDate(day)).append("\r\n");
                    for(LeaderboardEntry e : CardCrawlGame.mainMenuScreen.dailyScreen.entries){
                        s.append(e.rank).append(". ").append(e.name).append(" : ").append(e.score).append("\r\n");
                    }
                }
            }else if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.PATCH_NOTES){
                s.append("Patch Notes are displayed in the Event window.\r\nback");
            }else if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.LEADERBOARD ){
                s.append("Leaderboard\r\nDue to technical difficulties you cannot back out of this screen.\r\n");
                s.append("char ");
                for(int i=0;i<CardCrawlGame.mainMenuScreen.leaderboardsScreen.charButtons.size();i++){
                    if(CardCrawlGame.mainMenuScreen.leaderboardsScreen.charButtons.get(i).active){
                        s.append(CardCrawlGame.mainMenuScreen.leaderboardsScreen.charButtons.get(i).label).append("\r\n");
                        break;
                    }
                }
                for(int i=0;i<CardCrawlGame.mainMenuScreen.leaderboardsScreen.charButtons.size();i++){
                    s.append(i).append(": ").append(CardCrawlGame.mainMenuScreen.leaderboardsScreen.charButtons.get(i).label).append("\r\n");
                }
                s.append("region ");
                for(int i=0;i<CardCrawlGame.mainMenuScreen.leaderboardsScreen.regionButtons.size();i++){
                    if(CardCrawlGame.mainMenuScreen.leaderboardsScreen.regionButtons.get(i).active){
                        s.append(CardCrawlGame.mainMenuScreen.leaderboardsScreen.regionButtons.get(i).label).append("\r\n");
                        break;
                    }
                }
                for(int i=0;i<CardCrawlGame.mainMenuScreen.leaderboardsScreen.regionButtons.size();i++){
                    s.append(i).append(": ").append(CardCrawlGame.mainMenuScreen.leaderboardsScreen.regionButtons.get(i).label).append("\r\n");
                }
                s.append("type ");
                for(int i=0;i<CardCrawlGame.mainMenuScreen.leaderboardsScreen.typeButtons.size();i++){
                    if(CardCrawlGame.mainMenuScreen.leaderboardsScreen.typeButtons.get(i).active){
                        s.append(CardCrawlGame.mainMenuScreen.leaderboardsScreen.typeButtons.get(i).label).append("\r\n");
                        break;
                    }
                }
                for(int i=0;i<CardCrawlGame.mainMenuScreen.leaderboardsScreen.typeButtons.size();i++){
                    s.append(i).append(": ").append(CardCrawlGame.mainMenuScreen.leaderboardsScreen.typeButtons.get(i).label).append("\r\n");
                }
            }else if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.ABANDON_CONFIRM){
                s.append("Abandon Confirm\r\nyes\r\nno");
            }else if(CardCrawlGame.mainMenuScreen != null && CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.SAVE_SLOT){
                if(CardCrawlGame.mainMenuScreen.saveSlotScreen.curPop == SaveSlotScreen.CurrentPopup.NONE){
                    s.append("Save Slots\r\n");
                    if(!CardCrawlGame.mainMenuScreen.saveSlotScreen.cancelButton.isHidden){
                        s.append("back\r\n");
                    }
                    int slot_index = 0;
                    for(SaveSlot slot : CardCrawlGame.mainMenuScreen.saveSlotScreen.slots){
                        if(slot.emptySlot){
                            s.append(slot_index).append(" Empty\r\n");
                        }else{
                            s.append(slot_index).append(" ").append(slot.getName()).append("\r\n");
                            s.append("Completion ").append(CardCrawlGame.saveSlotPref.getFloat(SaveHelper.slotName("COMPLETION", slot_index), 0.0F)).append("%\r\n");
                        }
                        slot_index++;
                    }
                    s.append("Possible commands:\r\nnew\r\ndelete\r\nrename\r\nopen\r\nInclude save slot index after command.\r\nExample:\r\nrename 2\r\n");
                }else if(CardCrawlGame.mainMenuScreen.saveSlotScreen.curPop == SaveSlotScreen.CurrentPopup.RENAME){
                    s.append("Go to main game window, type a name, and hit enter.\r\nName cannot be empty.\r\nHit esc to cancel.\r\n");
                }else if(CardCrawlGame.mainMenuScreen.saveSlotScreen.curPop == SaveSlotScreen.CurrentPopup.DELETE){
                    s.append("Delete Confirm\r\nyes\r\nno\r\n");
                }
            }

            return s.toString();

        }

    }

    //Returns a list of all shop items with prices
    public static ArrayList<String> priceShopScreenChoices(){
        ArrayList<String> choices = new ArrayList<>();
        ArrayList<Object> shopItems = getAvailableShopItems();
        for (Object item : shopItems) {
            if (item instanceof String) {
                choices.add((String) item);
            } else if (item instanceof AbstractCard) {
                if(TextTheSpire.replayTheSpire && ((AbstractCard) item).equals(ReplayShopInitCardsPatch.doubleCard)) {
                    choices.add(((AbstractCard) item).name.toLowerCase() + "-" + ((AbstractCard) item).price + " Two for One");
                    doubleIndex = choices.size() - 1;
                }else {
                    choices.add(((AbstractCard) item).name.toLowerCase() + "-" + ((AbstractCard) item).price);
                }
            } else if (item instanceof StoreRelic) {
                choices.add(((StoreRelic)item).relic.name + "-" + ((StoreRelic) item).price);
            } else if (item instanceof StorePotion) {
                choices.add(((StorePotion)item).potion.name + "-" + ((StorePotion) item).price);
            }
        }
        return choices;
    }

    /*
    Gets a list of all shop items.
    Copied from CommunicationMod
     */
    public static ArrayList<Object> getAvailableShopItems() {
        ArrayList<Object> choices = new ArrayList<>();
        ShopScreen screen = AbstractDungeon.shopScreen;
        if(screen.purgeAvailable && AbstractDungeon.player.gold >= ShopScreen.actualPurgeCost) {
            choices.add("purge-" + ShopScreen.actualPurgeCost);
        }
        for(AbstractCard card : ChoiceScreenUtils.getShopScreenCards()) {
            if(card.price <= AbstractDungeon.player.gold) {
                choices.add(card);
            }
        }
        for(StoreRelic relic : ChoiceScreenUtils.getShopScreenRelics()) {
            if(relic.price <= AbstractDungeon.player.gold) {
                choices.add(relic);
            }
        }
        for(StorePotion potion : ChoiceScreenUtils.getShopScreenPotions()) {
            if(potion.price <= AbstractDungeon.player.gold) {
                choices.add(potion);
            }
        }
        return choices;
    }

    /*
    Params:
        input - an Event choice
    Returns:
        A String with color mods removed from input
     */
    public static String stripColor(String input) {
        input = input.replace("#r", "");
        input = input.replace("#g", "");
        input = input.replace("#b", "");
        input = input.replace("#y", "");
        input = input.replace("#p", "");
        input = input.replace("~", "");
        input = input.replace("@", "");
        input = input.replace("[#2aecd7]", "");
        input = input.replace("NL", "\r\n");
        return input;
    }

    public static String mapType(String input){
        if(input == null){
            return "Boss Chest";
        }
        switch(input){
            case "M":
                return "Monster";
            case "?":
                return "Event";
            case "E":
                return "Elite";
            case "$":
                return "Shop";
            case "R":
                return "Rest";
            case "T":
                return "Treasure";
            case "B":
                return "Boss";
        }
        return "Boss Chest";
    }

}
