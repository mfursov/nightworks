
ANATOLIAMUD -- Realms of Anatolia -- Version 3.1.1               07/06/2004

Introduction 
----------------------------

Anatolia MUD is a roleplaying, playerkilling and adventurous multi user
dungeon game. It is text based game network game. 

Anatolia Mud is based on Rom 2.4 and Rom is based on Merc Diku Mud. Merc
Diku Mud is a Diku Mud with many enhancements and contributions.  See the
doc/ directory for all details. 

Enjoy our mud! For for information please visit:
  http://www.anatoliamud.org

This is the 3.1-patch-1 release of Anatolia MUD.

To get more information please see the following directories and documents:
  doc/Updates/FROM30TO31   Updates from version 3.0 to version 3.1
  doc/Updates/FROM22TO30   Updates from version 2.2 to version 3.0
  doc/Updates/FROM21TO22   Updates from version 2.1 to version 2.2
  doc/Updates/FROM20TO21   Updates from version 2.0 to version 2.1
  doc/Updates/WHATSNEW     Updates from Rom 2.4beta to Anatolia version 2.0


Copyright and License
----------------------------

  Diku Mud License Information: doc/License/license.diku
  Merc Mud License Information: doc/License/license.merc
  Rom Mud License Information:  doc/License/license.rom
  Anatolia Mud License Information: doc/License/license.anatolia

Contents of the Release
----------------------------

  PREFIX/bin/	:  binary files and start/stop script
  PREFIX/doc/	:  documentation and license information
  PREFIX/etc/	:  configuration files for the game
  PREFIX/lib/	:  area and player files.
  PREFIX/src/	:  source files of the anatolia binary
  PREFIX/var/	:  intermediary files generated by the game: pid, log files, etc

How to Install
----------------------------

Refer to INSTALL file in the same directory as this file or doc/Install.txt or
http://www.anatoliamud.org


How to Play
----------------------------

* Connect to the game by using any telnet client or TinTin:

	telnet localhost 4000
	
* To make your first immortal character, just start as a mortal character,
play at least as far as level 2 so that you can save your character. Then
edit the player file and change your level.  (After your first immortal,
you can advance other future immortal players to immortal levels).

* If you haven't already done so, read all the license docs listed in the
"Copyright and License" section. Anatolia is a derivative of ROM, and ROM
is a derivative of Merc and Merc is a derivative work of Diku Mud. You must
register your mud with the original Diku implementors.
  
* Of course you're going to change the title screen, help files, and so on.
Don't just globally erase the 'Merc', 'Diku', 'Rom' or 'Anatolia'
references, ok?  You wouldn't like it if we did that to your work,
so don't do it to ours.

Support
----------------------------

Also check the 'wizhelp' command and read the 'help' descriptions for the
individual immortal commands.


Future Plans
----------------------------

* MySQL DB Support
* Built-in OLC
* High Availability/Load Balancing
* Localization support with language translation
* Better A.I. for NPCs


Contact Information
----------------------------

Currently developed by :
  Serdar Bulut     : bulut@anatoliamud.org

Previous developers :
  Ibrahim Canpunar : canpunar@anatoliamud.org
  Murat Bicer      : murat@anatoliamud.org
  Devrim B Acar    : dba@anatoliamud.org

