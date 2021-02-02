# ASDFmod-BPS
## Client Side Block Placing Sound Mod _by asdfcube_
### Background
If you have high ping on some servers and play Minecraft, you would probably notice that the sounds for placing blocks are delayed.<br>
Mojang has bigbrain and decided to make those sound server-side, meaning that if the server never tells your client to play them, they will never be played.<br>
This is especially annoying when you have high ping and play minigames that place blocks like Bedwars. For me personally, I speedbridge by listening to the block sounds, so I created this mod to help whoever with high ping.
<br>
### The Mod
This mod is designed to be lite and has as little impact on the game as possible.<br>
And it is so small that all codes can be fit into [one singular file](src/main/java/asdf/mod/BPS/BPS.java).<br>
You will need at least Java 8 and Minecraft Forge 1.8.9.<br>
*Note:<br>*
*1. The mod might not work on servers with custom sounds for block placing, do "/bps toggle" on those servers.<br>*
*2. The codes are compatible with basically all Forge versions but you will need to change [mcmod.info](src/main/resources/mcmod.info) and [build.gradle](build.gradle) a bit, and compile them yourself.*
<br>
### Usage
"/bps" - check the current toggle state.<br>
"/bps toggle" - toggle bps on or off, server reconnecting required for the toggle to take effect.
<br>
### Download/Building & Installation
Just go to [Releases](https://github.com/asdfcube/ASDFmod-BPS/releases) and download the `asdfbps-1.jar` in Assets tab.<br>
<br>
If you are a nerd and want to compile it yourself, just clone this repository, then run<br>
`./gradlew build` if you are on Windows;<br>
`gradlew build` if you are on macOS or Linux.<br>
The built mod jar will be in the `build/libs` folder, with name asdfbps-1.jar<br>
<br>
To install, just move the mod file into your mods folder in `.minecraft` like every other mod.<br>
### Bugs Reporting
Coding Minecraft mods is always amazing, because you never know what stupid mistake Mojang made can crash your mod, so far before the release I have not encountered any bugs, but if you do, please report it by [creating an issue](https://github.com/asdfcube/ASDFmod-BPS/issues/new).
<br>
### License
WTFPL - see [LICENSE.txt](LICENSE.txt)<br>
~~yes~~<br>
<br><br>
Hello if you are a developer from other clients like Badlion or Lunar, if you want to implement this into your client please feel free to do so, just remember to put my name in the credit. :P 
