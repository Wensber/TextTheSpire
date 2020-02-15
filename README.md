TextTheSpire

Description:

This is a mod for the game Slay the Spire and provides screen reader accessibility.
It does so by providing lots of windows displaying the game information and a prompt window for inputting commands.
Descriptions of the windows and commands is provided in the sections Display and Controls.

Status of Completion:

This mod is almost done. I hesitate to call it done for 2 reasons.
The first is that there are still potentially unknown bugs that need to be fixed.
For example, I previously discovered that TextTheSpire did not behave correctly with the relic Runic Dome.
You are not supposed to be able to see enemy intents while owning Runic Dome but TextTheSpire displayed the intents anyway.
I have not been able to check every single relic, card, and potential interactions between them that could cause bugs that are game breaking or simply messes up the display.
The second reason is that I have only tested this mod on my personal computer and with only the screen reader NVDA.
I can't call this done until I confirm TextTheSpire works for other people too.

Requirements:

Communication Mod : https://github.com/ForgottenArbiter/CommunicationMod

Mod The Spire

Base Mod

A screen reader. Chances are if you are here you already have one, but if you do not NVDA is a free screen reader.
https://www.nvaccess.org/download/

Setup:

If you are visually impaired you will likely need help with setting this up.

Step 1: Make sure you have all of the requirements. Base Mod and Mod the Spire simply need to be subscribed to in the Steam Workshop.
CommunicationMod.jar and TextTheSpire.jar need to be placed in the Slay the Spire's mod directory.
It is located at Steam\steamapps\common\SlayTheSpire\mods. If the folder does not exist create it.

Step 2: You should see a file called mts-launcher.jar in the Slay the Spire main directory.
Create a shortcut of this. This is the easy way to open Slay the Spire with mods.
The other way involves navigating the Steam interface and is generally a pain using a screen reader.

Step 3: Double click mts-launcher.jar to open the mod interface.
Select Base Mod, Communication Mod, and TextTheSpire.
Other mods should be unselected as TextTheSpire is not compatible with other mods.

Step 4: Hit play to enter the game. It will take some time but it should load fine.
If it does not and you are certain you followed the instruction correctly then let me know so I can see if its a compatibility issue I can fix.

Step 5: Once you are in game, enter the settings. If you are visually impaired you will need help with this.
In the Sound section disable "Mute while in background". Due to basically never using the main game window it is always in the background.

Step 6: In the Preferences section select "Disable confirmation when choosing 1 card".
This is technically optional but highly recommended to cut down on required inputs.
In the same section there is "Fast Mode". This is truly optional and is up to personal preference.

Step 7: Make sure you are in the save slot you want to use.
TextTheSpire currently does not support changing save slots.
After this setup is complete and you are free to close all windows.

Starting the game:

First you need to log into Steam. Close the steam windows afterwards.
It is recommended you also close any other windows to reduce the numbers of windows you need to alt tab through.

Double click mts-launcher.jar. The previous settings are still selected so once the window appears all you need to do is hit enter.

Once you hear the main screen music you are able to start or continue runs.

Display:

There are numerous windows that are going to display information. Most represent a single zone.

Prompt:
This window displays no information. It is the only window you can input information.
Type a command and then hit enter to control the game. More details are in the Controls section.

Event:
This window is used to display all choices that can be made.
Choices that can be made are usually but not always displayed with a number followed by a description of the choice.
More details in the Controls section.
During a run you will be returning to this window very often and usually right after a non combat command unless you've memorized the choices to be made.
While in the shop screen choices will be followed by the price of the choice.
When choosing a map node to go to the choices include the node type and X coordinate.
When selecting a card from a grid or your hand it will show the number of already selected cards and which type of selection it is, hand or grid.
Hand selection will also show the number of selections you need to make and a brief reason for selection.
While on the main menu it will display choice on continuing, starting, or restarting a run.
It will also display information on the unlock status of characters and their ascensions.

Map:
This window is used to display the map.
Each floor is displayed with the line "Floor X" followed by all nodes on the floor.
Nodes are displayed with node type, X coordinate, and the X coordinate of connected nodes on the floor below.
You are not expected to use this to understand the map and plan a route.
An easier method is provided in the window Inspect.

Player:
This window is used to display information about the player.
The information displayed changes depending if you are in combat or not.
While in combat it will display your hp, block, energy, channeled orbs, stance if not neutral stance, and powers.
Powers include buffs and debuffs.
While out of combat it will also display current gold and your potion slots.

Monster:
This window is used to display enemy information while in combat.
It first displays the total number of enemies and total incoming damage.
Then it displays each enemy with its information.
The first line for each monster includes the monster's index and name.

Hand:
This window is used to display the cards in your hand.
Cards are displayed with the index number, card name, and current card cost.
This window is only open in combat.

Deck:
This window is used to display the cards in your deck.
It first displays the size of the deck followed by the name of each card.
While in combat it displays the current deck.
While out of combat it displays the master deck.

Discard:
This window is used to display cards in your discard pile.
It first displays the size of the pile followed by the name of each card.
It is only displayed during combat.

Relic:
This window is used to display relics you have.
Relics with a counter will display its count.

Inspect:
This window is used to display more details of certain things.
Currently this is limited to cards in hand and map nodes.
When you inspect a card in hand this window will display the cards damage, block, magic number, heal, draw, and discard values if the value is significant.
When you inspect a map node, this window will display a filtered version of the map with only nodes you can reach from your current position and can reach the inspected node.
The inspected node is effectively a destination and all displayed nodes are nodes you can pass on your way from your current node to this destination.
In this window nodes are not displayed with connections.
More details about inspection in Controls.

Controls:

To make a numbered choice, simply enter the given number into the prompt.
If the choice is not numbered you will have to type it out.

Starting a run:

The choice to start a run is displayed as "start [class] [ascension] [seed]".
You will have to type that out filling in class, ascension, and seed with the info you want.
Ascension and seed are optional.
If a save file exists you will instead see "restart [class] [ascension] [seed]" along with continue.

Confirm and Cancel:

Throughout the game there will be screens with confirm and cancel buttons.
They are not always named that way but function the same. You just need to type it out.

Potions:

Potion commands follow the format "potion [use/discard] [potion index] [target index]".
The target index is the monster's index and is optional if the potion has no target.
A monster's index does not change in the middle of combat.

Playing cards:

Play commands follow the format "play [card index] [target index]".
The target index is option if the card has no target.
The card index if the card's position in your hand.
A card's index will change as cards leave your hand.

Inspecting cards:

Inspecting a card in your hand has the format "inspect [card index]".
Can only be used on cards in your hand.

Inspecting map nodes:

Inspecting a map node has the format "map [floor] [X coordinate]".
Can be used when out of combat.

Quiting:

The command "quit" will close the game. The windows don't close on their own.
You can close them by either closing each one or closing the Mod the Spire window.

If you run into issues using the above commands, whether using them at designated times or not, please let me know.