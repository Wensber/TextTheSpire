##Text the Spire
###Screen Reader Assistance

This is a mod designed to help those who are visually impaired to play Slay the Spire. It will only display necessary information to avoid clutter so its recommended to use the wiki as well.

It requires the use of CommunicationMod.

*This mod has not yet been tested with an actual screen reader and still needs to undergo test runs to ensure there are no overlooked special cases.*

*This is still a work in progress. If you use this mod and run into any issues please let me know.*

This mod works by opening various windows to represent information in various zones. Most are self explanatory.

The Prompt window is used to input commands.

The Event window displays event information and available choices that can be made. This includes choices during events, card selection, and collecting rewards.

The Map window displays an interpretation of the map. The format for map nodes is:

[Type]-[xPos]{[xPos of connected nodes on next lower floor]}

Type can be:

M: Monster

U: Unknown

R: Rest

T: Treasure

E: Elite

EK: Elite with Emerald Key

I chose this format to display the map because it would allow someone to find a specific node they want to reach, say a late shop or the Emerald Elite and then trace the lower connections down.

Available Commands:

quit - Self explanatory

play [hand pos] [monster pos] - Plays the card at hand pos targeting monster pos. Monster pos is not needed for spells without targets. The pos can be found in the Hand and Monster windows.

potion [use/discard] [potion slot] [monster pos] - Uses or discards the potion are potion slot targeting monster pos. The pos can be found in the Hand and Monster windows.

All other commands can be found in the Event screen. Choices with a number are chosen by inputting the number while commands like "continue" you enter "continue".