package net.sf.nightworks;

/*
 * ************************************************************************ *
 *   Nightworks MUD is copyright 2006 Mikhail Fursov                        *
 *       Mikhail Fursov {fmike@mail.ru}                                     *
 * ************************************************************************ *

 * ************************************************************************ *
 *   ANATOLIA MUD is copyright 1996-2002 Serdar BULUT, Ibrahim CANPUNAR     *
 *   ANATOLIA has been brought to you by ANATOLIA consortium		        *
 *	 Serdar BULUT {Chronos}		bulut@anatoliamud.org                       *
 *	 Ibrahim Canpunar  {Asena}	canpunar@anatoliamud.org                    *
 *	 Murat BICER  {KIO}		mbicer@anatoliamud.org       	                *
 *	 D.Baris ACAR {Powerman}	dbacar@anatoliamud.org       	            *
 *   By using this code, you have agreed to follow the terms of the         *
 *   ANATOLIA license, in the file Anatolia/doc/License/license.anatolia    *
 * ************************************************************************ *

 * ************************************************************************ *
 *	ROM 2.4 is copyright 1993-1995 Russ Taylor			                    *
 *	ROM has been brought to you by the ROM consortium	             	    *
 *	    Russ Taylor (rtaylor@pacinfo.com)				                    *
 *	    Gabrielle Taylor (gtaylor@pacinfo.com)			                    *
 *	    Brian Moore (rom@rom.efn.org)				                        *
 *	By using this code, you have agreed to follow the terms of the  	    *
 *	ROM license, in the file Rom24/doc/rom.license			                *
 * ************************************************************************ *

 * *********************************************************************** *
 *  Original Diku Mud copyright (C) 1990, 1991 by Sebastian Hammer,        *
 *  Michael Seifert, Hans Henrik St{rfeldt, Tom Madsen, and Katja Nyboe.   *
 *                                                                         *
 *  Merc Diku Mud improvments copyright (C) 1992, 1993 by Michael          *
 *  Chastain, Michael Quan, and Mitchell Tse.                              *
 *                                                                         *
 *  In order to use any part of this Merc Diku Mud, you must comply with   *
 *  both the original Diku license in 'license.doc' as well the Merc       *
 *  license in 'license.txt'.  In particular, you may not remove either of *
 *  these copyright notices.                                               *
 *                                                                         *
 *  Much time and thought has gone into this software and you are          *
 *  benefitting.  We hope that you share your changes too.  What goes      *
 *  around, comes around.                                                  *
 * *********************************************************************** *
 */

import net.sf.nightworks.util.DikuTextFile;
import net.sf.nightworks.util.TextBuffer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

import static net.sf.nightworks.Comm.act;
import static net.sf.nightworks.DB.bug;
import static net.sf.nightworks.DB.create_mobile;
import static net.sf.nightworks.DB.create_object;
import static net.sf.nightworks.DB.create_object_nocount;
import static net.sf.nightworks.DB.get_mob_index;
import static net.sf.nightworks.DB.get_obj_index;
import static net.sf.nightworks.DB.get_room_index;
import static net.sf.nightworks.DB.log_string;
import static net.sf.nightworks.Handler.check_time_sync;
import static net.sf.nightworks.Handler.extract_obj;
import static net.sf.nightworks.Handler.extract_obj_nocount;
import static net.sf.nightworks.Handler.get_played_day;
import static net.sf.nightworks.Handler.get_played_time;
import static net.sf.nightworks.Handler.get_total_played;
import static net.sf.nightworks.Handler.get_trust;
import static net.sf.nightworks.Handler.obj_to_char;
import static net.sf.nightworks.Handler.obj_to_obj;
import static net.sf.nightworks.Nightworks.AFFECT_DATA;
import static net.sf.nightworks.Nightworks.AFF_CHARM;
import static net.sf.nightworks.Nightworks.AFF_PLAGUE;
import static net.sf.nightworks.Nightworks.AFF_POISON;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.COMM_COMBINE;
import static net.sf.nightworks.Nightworks.COMM_PROMPT;
import static net.sf.nightworks.Nightworks.COND_BLOODLUST;
import static net.sf.nightworks.Nightworks.COND_DESIRE;
import static net.sf.nightworks.Nightworks.COND_FULL;
import static net.sf.nightworks.Nightworks.COND_HUNGER;
import static net.sf.nightworks.Nightworks.COND_THIRST;
import static net.sf.nightworks.Nightworks.DEFAULT_PROMPT;
import static net.sf.nightworks.Nightworks.DESCRIPTOR_DATA;
import static net.sf.nightworks.Nightworks.DICE_BONUS;
import static net.sf.nightworks.Nightworks.ETHOS_ANY;
import static net.sf.nightworks.Nightworks.EXTRA_DESCR_DATA;
import static net.sf.nightworks.Nightworks.IS_AFFECTED;
import static net.sf.nightworks.Nightworks.IS_IMMORTAL;
import static net.sf.nightworks.Nightworks.IS_NPC;
import static net.sf.nightworks.Nightworks.IS_QUESTOR;
import static net.sf.nightworks.Nightworks.IS_SET;
import static net.sf.nightworks.Nightworks.ITEM_ARMOR;
import static net.sf.nightworks.Nightworks.ITEM_CONTAINER;
import static net.sf.nightworks.Nightworks.ITEM_KEY;
import static net.sf.nightworks.Nightworks.ITEM_MAP;
import static net.sf.nightworks.Nightworks.ITEM_PILL;
import static net.sf.nightworks.Nightworks.ITEM_POTION;
import static net.sf.nightworks.Nightworks.ITEM_SCROLL;
import static net.sf.nightworks.Nightworks.ITEM_STAFF;
import static net.sf.nightworks.Nightworks.ITEM_WAND;
import static net.sf.nightworks.Nightworks.ITEM_WEAPON;
import static net.sf.nightworks.Nightworks.LEVEL_IMMORTAL;
import static net.sf.nightworks.Nightworks.MAX_CABAL;
import static net.sf.nightworks.Nightworks.MAX_STATS;
import static net.sf.nightworks.Nightworks.MAX_TIME_LOG;
import static net.sf.nightworks.Nightworks.MOB_VNUM_FIDO;
import static net.sf.nightworks.Nightworks.OBJ_DATA;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_EYED_SWORD;
import static net.sf.nightworks.Nightworks.ORG_RACE;
import static net.sf.nightworks.Nightworks.OWEAR_ABOUT;
import static net.sf.nightworks.Nightworks.OWEAR_ARMS;
import static net.sf.nightworks.Nightworks.OWEAR_BODY;
import static net.sf.nightworks.Nightworks.OWEAR_FEET;
import static net.sf.nightworks.Nightworks.OWEAR_FINGER_L;
import static net.sf.nightworks.Nightworks.OWEAR_FINGER_R;
import static net.sf.nightworks.Nightworks.OWEAR_FLOAT;
import static net.sf.nightworks.Nightworks.OWEAR_HANDS;
import static net.sf.nightworks.Nightworks.OWEAR_HEAD;
import static net.sf.nightworks.Nightworks.OWEAR_HOLD;
import static net.sf.nightworks.Nightworks.OWEAR_LEGS;
import static net.sf.nightworks.Nightworks.OWEAR_LIGHT;
import static net.sf.nightworks.Nightworks.OWEAR_NECK_1;
import static net.sf.nightworks.Nightworks.OWEAR_NECK_2;
import static net.sf.nightworks.Nightworks.OWEAR_SECOND_WIELD;
import static net.sf.nightworks.Nightworks.OWEAR_SHIELD;
import static net.sf.nightworks.Nightworks.OWEAR_STUCK_IN;
import static net.sf.nightworks.Nightworks.OWEAR_TATTOO;
import static net.sf.nightworks.Nightworks.OWEAR_WAIST;
import static net.sf.nightworks.Nightworks.OWEAR_WIELD;
import static net.sf.nightworks.Nightworks.OWEAR_WRIST_L;
import static net.sf.nightworks.Nightworks.OWEAR_WRIST_R;
import static net.sf.nightworks.Nightworks.PC_DATA;
import static net.sf.nightworks.Nightworks.PLR_NOCANCEL;
import static net.sf.nightworks.Nightworks.PLR_NOSUMMON;
import static net.sf.nightworks.Nightworks.PLR_REMORTED;
import static net.sf.nightworks.Nightworks.POS_FIGHTING;
import static net.sf.nightworks.Nightworks.POS_STANDING;
import static net.sf.nightworks.Nightworks.QUEST_ITEM1;
import static net.sf.nightworks.Nightworks.QUEST_ITEM2;
import static net.sf.nightworks.Nightworks.QUEST_ITEM3;
import static net.sf.nightworks.Nightworks.RELIGION_NONE;
import static net.sf.nightworks.Nightworks.REMOVE_BIT;
import static net.sf.nightworks.Nightworks.ROOM_VNUM_LIMBO;
import static net.sf.nightworks.Nightworks.SET_BIT;
import static net.sf.nightworks.Nightworks.SET_ORG_RACE;
import static net.sf.nightworks.Nightworks.STAT_CHA;
import static net.sf.nightworks.Nightworks.STAT_CON;
import static net.sf.nightworks.Nightworks.STAT_DEX;
import static net.sf.nightworks.Nightworks.STAT_INT;
import static net.sf.nightworks.Nightworks.STAT_STR;
import static net.sf.nightworks.Nightworks.STAT_WIS;
import static net.sf.nightworks.Nightworks.TLP_NOLOG;
import static net.sf.nightworks.Nightworks.TO_CHAR;
import static net.sf.nightworks.Nightworks.UMIN;
import static net.sf.nightworks.Nightworks.WEAR_ABOUT;
import static net.sf.nightworks.Nightworks.WEAR_ARMS;
import static net.sf.nightworks.Nightworks.WEAR_BODY;
import static net.sf.nightworks.Nightworks.WEAR_FEET;
import static net.sf.nightworks.Nightworks.WEAR_FINGER;
import static net.sf.nightworks.Nightworks.WEAR_FLOAT;
import static net.sf.nightworks.Nightworks.WEAR_HANDS;
import static net.sf.nightworks.Nightworks.WEAR_HEAD;
import static net.sf.nightworks.Nightworks.WEAR_LEFT;
import static net.sf.nightworks.Nightworks.WEAR_LEGS;
import static net.sf.nightworks.Nightworks.WEAR_NECK;
import static net.sf.nightworks.Nightworks.WEAR_NONE;
import static net.sf.nightworks.Nightworks.WEAR_RIGHT;
import static net.sf.nightworks.Nightworks.WEAR_STUCK_IN;
import static net.sf.nightworks.Nightworks.WEAR_TATTOO;
import static net.sf.nightworks.Nightworks.WEAR_WAIST;
import static net.sf.nightworks.Nightworks.WEAR_WRIST;
import static net.sf.nightworks.Nightworks.current_time;
import static net.sf.nightworks.Nightworks.nw_config;
import static net.sf.nightworks.Nightworks.object_list;
import static net.sf.nightworks.Nightworks.perror;
import static net.sf.nightworks.Nightworks.sprintf;
import static net.sf.nightworks.Recycle.get_pc_id;
import static net.sf.nightworks.Recycle.new_char;
import static net.sf.nightworks.Skill.gsn_doppelganger;
import static net.sf.nightworks.Skill.lookupSkill;
import static net.sf.nightworks.Tables.cabal_table;
import static net.sf.nightworks.util.TextUtils.UPPER;
import static net.sf.nightworks.util.TextUtils.capitalize;
import static net.sf.nightworks.util.TextUtils.str_cmp;
import static net.sf.nightworks.util.TextUtils.str_prefix;

/**
 * Last mofified:           $Date: 2006-07-20 07:41:52 -0700 (Thu, 20 Jul 2006) $
 * Revision of last commit: $Revision: 25 $
 */
class Save {

    static String print_flags(long flag) {
        int count;
        StringBuilder buf = new StringBuilder();

        for (count = 0; count < 64; count++) {
            if (IS_SET(flag, 1 << count)) {
                if (count < 26) {
                    buf.append((char) ('A' + count));
                } else {
                    buf.append((char) ('a' + (count - 26)));
                }
            }
        }
        return buf.toString();
    }

    /*
    * Array of containers read for proper re-nesting of objects.
    */
    private static final int MAX_NEST = 100;
    private static OBJ_DATA rgObjNest[] = new OBJ_DATA[MAX_NEST];

/*
* Save a character and inventory.
* Would be cool to save NPC's too for quest purposes,
*   some of the infrastructure is provided.
*/

    static void save_char_obj(CHAR_DATA ch) {

        if (IS_NPC(ch) || (ch.level < 2 && !IS_SET(ch.act, PLR_REMORTED))) {
            return;
        }

        if (ch.desc != null && ch.desc.original != null) {
            ch = ch.desc.original;
        }

        /* create god log */
        if (IS_IMMORTAL(ch) || ch.level >= LEVEL_IMMORTAL) {
            String strsave = nw_config.lib_god_dir + "/" + capitalize(ch.name);
            try {
                FileWriter f = new FileWriter(strsave);
                Formatter fp = new Formatter(f);
                try {
                    fp.format("Lev %2d Trust %2d  %s%s\n", ch.level, get_trust(ch), ch.name, ch.pcdata.title);
                } finally {
                    f.close();
                }
            } catch (IOException e) {
                bug("Save_char_obj: fopen");
                perror(strsave);
            }
        }

        TextBuffer fp = new TextBuffer();
        fwrite_char(ch, fp);
        if (ch.carrying != null) {
            fwrite_obj(ch, ch.carrying, fp, 0);
        }
        /* save the pets */
        if (ch.pet != null && ch.pet.in_room == ch.in_room) {
            fwrite_pet(ch.pet, fp);
        }

        fp.append("#END\n");

        String fileName = nw_config.lib_player_dir + "/" + capitalize(ch.name);
        try {
            FileWriter f = new FileWriter(fileName);
            try {
                f.write(fp.toString());
            } finally {
                f.close();
            }
        } catch (IOException e) {
            bug("Save_char_obj: fopen");
            perror(fileName);
        }
        new File(nw_config.pl_temp_file).renameTo(new File(fileName));
    }

/*
* Write the char.
*/


    static void fwrite_char(CHAR_DATA ch, TextBuffer fp) {
        AFFECT_DATA paf;
        int pos, l, today, d_time;
        boolean fMidNight;

        fp.sprintf(false, "#%s\n", IS_NPC(ch) ? "MOB" : "PLAYER");

        fp.sprintf(false, "Name %s~\n", ch.name);
        fp.sprintf(false, "Id   %d\n", ch.id);
        fp.sprintf(false, "LogO %d\n", current_time);
        fp.sprintf(false, "Vers %d\n", 7);
        fp.sprintf(false, "Etho %d\n", ch.ethos);
        fp.sprintf(false, "Home %d\n", ch.hometown);
        fp.sprintf(false, "Cab  %d\n", ch.cabal);
        fp.sprintf(false, "Dead %d\n", ch.pcdata.death);
        fp.sprintf(false, "Ques %s\n", print_flags(ch.quest));

        if (ch.short_descr.length() != 0) {
            fp.sprintf(false, "ShD  %s~\n", ch.short_descr);
        }
        if (ch.long_descr.length() != 0) {
            fp.sprintf(false, "LnD  %s~\n", ch.long_descr);
        }
        if (ch.description.length() != 0) {
            fp.sprintf(false, "Desc %s~\n", ch.description);
        }
        if (ch.prompt != null || !str_cmp(ch.prompt, "<%hhp %mm %vmv> ")) {
            fp.sprintf(false, "Prom %s~\n", ch.prompt);
        }
        fp.sprintf(false, "Race %s~\n", ORG_RACE(ch).name);
        fp.sprintf(false, "Sex  %d\n", ch.sex);
        fp.sprintf(false, "Cla  %s\n", ch.clazz.name);
        fp.sprintf(false, "Levl %d\n", ch.level);
        if (ch.trust != 0) {
            fp.sprintf(false, "Tru  %d\n", ch.trust);
        }
        fp.sprintf(false, "Plyd %d\n",
                ch.pcdata.played + (int) (current_time - ch.logon));
        fp.sprintf(false, "Not  %d %d %d %d %d\n",
                ch.pcdata.last_note, ch.pcdata.last_idea, ch.pcdata.last_penalty,
                ch.pcdata.last_news, ch.pcdata.last_changes);
        fp.sprintf(false, "Scro %d\n", ch.lines);
        fp.sprintf(false, "Room %d\n",
                (ch.in_room == get_room_index(ROOM_VNUM_LIMBO)
                        && ch.was_in_room != null)
                        ? ch.was_in_room.vnum
                        : ch.in_room == null ? 3001 : ch.in_room.vnum);

        fp.sprintf(false, "HMV  %d %d %d\n", ch.hit, ch.mana, ch.move);

        if (ch.gold > 0) {
            fp.sprintf(false, "Gold %d\n", ch.gold);
        } else {
            fp.sprintf(false, "Gold %d\n", 0);
        }
        if (ch.silver > 0) {
            fp.sprintf(false, "Silv %d\n", ch.silver);
        } else {
            fp.sprintf(false, "Silv %d\n", 0);
        }
        if (ch.pcdata.bank_s > 0) {
            fp.sprintf(false, "Banks %d\n", ch.pcdata.bank_s);
        } else {
            fp.sprintf(false, "Banks %d\n", ch.pcdata.bank_s);
        }
        if (ch.pcdata.bank_g > 0) {
            fp.sprintf(false, "Bankg %d\n", ch.pcdata.bank_g);
        } else {
            fp.sprintf(false, "Bankg %d\n", ch.pcdata.bank_g);
        }
        fp.sprintf(false, "Exp  %d\n", ch.exp);
        if (ch.act != 0) {
            fp.sprintf(false, "Act  %s\n", print_flags(ch.act));
        }
/*
    if (ch.affected_by != 0)
    {
     if (IS_NPC(ch))
     fprintf( fp, "AfBy %s\n", print_flags(ch.affected_by) );
     else
     fprintf( fp, "AfBy %s\n",
        print_flags((ch.affected_by & (~AFF_CHARM))) );
    }
    if (ch.detection != 0)
    fprintf( fp, "Detect %s\n",   print_flags(ch.detection));
*/
        fp.sprintf(false, "Comm %s\n", print_flags(ch.comm));
        if (ch.wiznet != 0) {
            fp.sprintf(false, "Wizn %s\n", print_flags(ch.wiznet));
        }
        if (ch.invis_level != 0) {
            fp.sprintf(false, "Invi %d\n", ch.invis_level);
        }
        if (ch.incog_level != 0) {
            fp.sprintf(false, "Inco %d\n", ch.incog_level);
        }
        fp.sprintf(false, "Pos  %d\n",
                ch.position == POS_FIGHTING ? POS_STANDING : ch.position);
        if (ch.practice != 0) {
            fp.sprintf(false, "Prac %d\n", ch.practice);
        }
        if (ch.train != 0) {
            fp.sprintf(false, "Trai %d\n", ch.train);
        }
        fp.sprintf(false, "Alig  %d\n", ch.alignment);
/*
    if (ch.saving_throw != 0)
    fprintf( fp, "Save  %d\n",  ch.saving_throw);
    if (ch.hitroll != 0)
    fprintf( fp, "Hit   %d\n",  ch.hitroll );
    if (ch.damroll != 0)
    fprintf( fp, "Dam   %d\n",  ch.damroll );
    fprintf( fp, "ACs %d %d %d %d\n",
    ch.armor[0],ch.armor[1],ch.armor[2],ch.armor[3]);
*/
        if (ch.wimpy != 0) {
            fp.sprintf(false, "Wimp  %d\n", ch.wimpy);
        }

        fp.sprintf(false, "Attr %d %d %d %d %d %d\n",
                ch.perm_stat[STAT_STR],
                ch.perm_stat[STAT_INT],
                ch.perm_stat[STAT_WIS],
                ch.perm_stat[STAT_DEX],
                ch.perm_stat[STAT_CON],
                ch.perm_stat[STAT_CHA]);

/*
    fprintf (fp, "AMod %d %d %d %d %d %d\n",
    ch.mod_stat[STAT_STR],
    ch.mod_stat[STAT_INT],
    ch.mod_stat[STAT_WIS],
    ch.mod_stat[STAT_DEX],
    ch.mod_stat[STAT_CON],
    ch.mod_stat[STAT_CHA] );
*/

        fp.sprintf(false, "Pass %s~\n", ch.pcdata.pwd);
        if (ch.pcdata.bamfin.length() != 0) {
            fp.sprintf(false, "Bin  %s~\n", ch.pcdata.bamfin);
        }
        if (ch.pcdata.bamfout.length() != 0) {
            fp.sprintf(false, "Bout %s~\n", ch.pcdata.bamfout);
        }
        fp.sprintf(false, "Titl %s~\n", ch.pcdata.title);
        fp.sprintf(false, "Pnts %d\n", ch.pcdata.points);
        fp.sprintf(false, "TSex %d\n", ch.pcdata.true_sex);
        fp.sprintf(false, "LLev %d\n", ch.pcdata.last_level);
        fp.sprintf(false, "HMVP %d %d %d\n", ch.pcdata.perm_hit,
                ch.pcdata.perm_mana,
                ch.pcdata.perm_move);
        fp.sprintf(false, "CndC  %d %d %d %d %d %d\n",
                ch.pcdata.condition[0],
                ch.pcdata.condition[1],
                ch.pcdata.condition[2],
                ch.pcdata.condition[3],
                ch.pcdata.condition[4],
                ch.pcdata.condition[5]);

        /* write alias */
        for (pos = 0; pos < nw_config.max_alias; pos++) {
            if (ch.pcdata.alias[pos] == null
                    || ch.pcdata.alias_sub[pos] == null) {
                break;
            }

            fp.sprintf(false, "Alias %s %s~\n", ch.pcdata.alias[pos],
                    ch.pcdata.alias_sub[pos]);
        }

        for (Skill sn : Skill.skills) {
            if (sn.name != null && ch.pcdata.learned[sn.ordinal()] > 0) {
                fp.sprintf(false, "Sk %d '%s'\n",
                        ch.pcdata.learned[sn.ordinal()], sn.name);
            }
        }

        for (paf = ch.affected; paf != null; paf = paf.next) {
            if (paf.type == null || paf.type == gsn_doppelganger) {
                continue;
            }

            if (!IS_NPC(ch) &&
                    (paf.bitvector == AFF_CHARM || paf.duration < -1)) {
                continue;
            }

            fp.sprintf(false, "Affc '%s' %3d %3d %3d %3d %3d %10d\n",
                    paf.type.name,
                    paf.where,
                    paf.level,
                    paf.duration,
                    paf.modifier,
                    paf.location,
                    paf.bitvector
            );
        }

/* quest done by chronos */

        if (ch.pcdata.questpoints != 0) {
            fp.sprintf(false, "QuestPnts %d\n", ch.pcdata.questpoints);
        }
        if (ch.pcdata.nextquest != 0) {
            fp.sprintf(false, "QuestNext %d\n", ch.pcdata.nextquest);
        }
        if (IS_QUESTOR(ch)) {
            fp.sprintf(false, "QuestCnt %d\n", ch.pcdata.countdown);
            fp.sprintf(false, "QuestMob %d\n", ch.pcdata.questmob);
            fp.sprintf(false, "QuestObj %d\n", ch.pcdata.questobj);
            fp.sprintf(false, "QuestGiv %d\n", ch.pcdata.questgiver);
        }

        fp.sprintf(false, "Relig %d\n", ch.religion);
        fp.sprintf(false, "Haskilled %d\n", ch.pcdata.has_killed);
        fp.sprintf(false, "Antkilled %d\n", ch.pcdata.anti_killed);

        /* character log info */
        fMidNight = check_time_sync();
        fp.sprintf(false, "PlayLog 1\n");    /* 1 stands for version */
        for (l = 0; l < nw_config.max_time_log; l++) {
            today = get_played_day(l);
            d_time = get_played_time(ch, l);

            if (d_time != 0) {
                fp.sprintf(false, "%d %d\n",
                        (fMidNight) ? (today - 1) : today, d_time);
            }
        }
        fp.sprintf(false, "-1\n");

        fp.sprintf(false, "End\n\n");
    }

/* write a pet */

    static void fwrite_pet(CHAR_DATA pet, TextBuffer fp) {
        AFFECT_DATA paf;

        fp.sprintf(false, "#PET\n");

        fp.sprintf(false, "Vnum %d\n", pet.pIndexData.vnum);

        fp.sprintf(false, "Name %s~\n", pet.name);
        fp.sprintf(false, "LogO %d\n", current_time);
        fp.sprintf(false, "Cab  %d\n", pet.cabal);
        if (!pet.short_descr.equals(pet.pIndexData.short_descr)) {
            fp.sprintf(false, "ShD  %s~\n", pet.short_descr);
        }
        if (!pet.long_descr.equals(pet.pIndexData.long_descr)) {
            fp.sprintf(false, "LnD  %s~\n", pet.long_descr);
        }
        if (!pet.description.equals(pet.pIndexData.description)) {
            fp.sprintf(false, "Desc %s~\n", pet.description);
        }
        if (pet.race != pet.pIndexData.race) /* serdar ORG_RACE */ {
            fp.sprintf(false, "Race %s~\n", ORG_RACE(pet).name);
        }
        fp.sprintf(false, "Sex  %d\n", pet.sex);
        if (pet.level != pet.pIndexData.level) {
            fp.sprintf(false, "Levl %d\n", pet.level);
        }
        fp.sprintf(false, "HMV  %d %d %d %d %d %d\n",
                pet.hit, pet.max_hit, pet.mana, pet.max_mana, pet.move, pet.max_move);
        if (pet.gold > 0) {
            fp.sprintf(false, "Gold %d\n", pet.gold);
        }
        if (pet.silver > 0) {
            fp.sprintf(false, "Silv %d\n", pet.silver);
        }
        if (pet.exp > 0) {
            fp.sprintf(false, "Exp  %d\n", pet.exp);
        }
        if (pet.act != pet.pIndexData.act) {
            fp.sprintf(false, "Act  %s\n", print_flags(pet.act));
        }
        if (pet.affected_by != pet.pIndexData.affected_by) {
            fp.sprintf(false, "AfBy %s\n", print_flags(pet.affected_by));
        }
        if (pet.comm != 0) {
            fp.sprintf(false, "Comm %s\n", print_flags(pet.comm));
        }
        fp.sprintf(false, "Pos  %d\n", pet.position == POS_FIGHTING ? POS_STANDING : pet.position);
        if (pet.saving_throw != 0) {
            fp.sprintf(false, "Save %d\n", pet.saving_throw);
        }
        if (pet.alignment != pet.pIndexData.alignment) {
            fp.sprintf(false, "Alig %d\n", pet.alignment);
        }
        if (pet.hitroll != pet.pIndexData.hitroll) {
            fp.sprintf(false, "Hit  %d\n", pet.hitroll);
        }
        if (pet.damroll != pet.pIndexData.damage[DICE_BONUS]) {
            fp.sprintf(false, "Dam  %d\n", pet.damroll);
        }
        fp.sprintf(false, "ACs  %d %d %d %d\n",
                pet.armor[0], pet.armor[1], pet.armor[2], pet.armor[3]);
        fp.sprintf(false, "Attr %d %d %d %d %d %d\n",
                pet.perm_stat[STAT_STR], pet.perm_stat[STAT_INT],
                pet.perm_stat[STAT_WIS], pet.perm_stat[STAT_DEX],
                pet.perm_stat[STAT_CON], pet.perm_stat[STAT_CHA]);
        fp.sprintf(false, "AMod %d %d %d %d %d %d\n",
                pet.mod_stat[STAT_STR], pet.mod_stat[STAT_INT],
                pet.mod_stat[STAT_WIS], pet.mod_stat[STAT_DEX],
                pet.mod_stat[STAT_CON], pet.mod_stat[STAT_CHA]);

        for (paf = pet.affected; paf != null; paf = paf.next) {
            if (paf.type == null || paf.type == gsn_doppelganger) {
                continue;
            }
            fp.sprintf(false, "Affc '%s' %3d %3d %3d %3d %3d %10d\n",
                    paf.type.name,
                    paf.where, paf.level, paf.duration, paf.modifier, paf.location,
                    paf.bitvector);
        }

        fp.sprintf(false, "End\n");
    }

/*
 * Write an object and its contents.
 */

    static void fwrite_obj(CHAR_DATA ch, OBJ_DATA obj, TextBuffer fp, int iNest) {
        EXTRA_DESCR_DATA ed;
        AFFECT_DATA paf;
        int i;

        /*
        * Slick recursion to write lists backwards,
        *   so loading them will load in forwards order.
        */
        if (obj.next_content != null) {
            fwrite_obj(ch, obj.next_content, fp, iNest);
        }

        for (i = 1; i < MAX_CABAL; i++) {
            if (obj.pIndexData.vnum == cabal_table[i].obj_vnum) {
                return;
            }
        }

        /*
        * Castrate storage characters.
        */
        if (((ch.level < 10) && (obj.pIndexData.limit != -1))
                || (obj.item_type == ITEM_KEY && obj.value[0] == 0)
                || (obj.item_type == ITEM_MAP && obj.value[0] == 0)
                || ((ch.level < obj.level - 3) && (obj.item_type != ITEM_CONTAINER))
                || ((ch.level > obj.level + 35) && (obj.pIndexData.limit > 1))) {
            extract_obj(obj);
            return;
        }

        if (obj.pIndexData.vnum == QUEST_ITEM1
                || obj.pIndexData.vnum == QUEST_ITEM2
                || obj.pIndexData.vnum == QUEST_ITEM3
                || obj.pIndexData.vnum == OBJ_VNUM_EYED_SWORD) {
            if (!obj.short_descr.contains(ch.name)) {
                act("$p vanishes!", ch, obj, null, TO_CHAR);
                extract_obj(obj);
                return;
            }
        }

        fp.sprintf(false, "#O\n");
        fp.sprintf(false, "Vnum %d\n", obj.pIndexData.vnum);
        fp.sprintf(false, "Cond %d\n", obj.condition);

        if (!obj.pIndexData.new_format) {
            fp.sprintf(false, "Oldstyle\n");
        }
        if (obj.enchanted) {
            fp.sprintf(false, "Enchanted\n");
        }
        fp.sprintf(false, "Nest %d\n", iNest);

        /* these data are only used if they do not match the defaults */

        if (!obj.name.equals(obj.pIndexData.name)) {
            fp.sprintf(false, "Name %s~\n", obj.name);
        }
        if (!obj.short_descr.equals(obj.pIndexData.short_descr)) {
            fp.sprintf(false, "ShD  %s~\n", obj.short_descr);
        }
        if (!obj.description.equals(obj.pIndexData.description)) {
            fp.sprintf(false, "Desc %s~\n", obj.description);
        }
        if (obj.extra_flags != obj.pIndexData.extra_flags) {
            fp.sprintf(false, "ExtF %d\n", obj.extra_flags);
        }
        if (obj.wear_flags != obj.pIndexData.wear_flags) {
            fp.sprintf(false, "WeaF %d\n", obj.wear_flags);
        }
        if (obj.item_type != obj.pIndexData.item_type) {
            fp.sprintf(false, "Ityp %d\n", obj.item_type);
        }
        if (obj.weight != obj.pIndexData.weight) {
            fp.sprintf(false, "Wt   %d\n", obj.weight);
        }

        /* variable data */

        fp.sprintf(false, "WLoc %d\n", obj.wear_loc);
        if (obj.level != obj.pIndexData.level) {
            fp.sprintf(false, "Lev  %d\n", obj.level);
        }
        if (obj.timer != 0) {
            fp.sprintf(false, "Time %d\n", obj.timer);
        }
        fp.sprintf(false, "Cost %d\n", obj.cost);
        if (obj.value[0] != obj.pIndexData.value[0]
                || obj.value[1] != obj.pIndexData.value[1]
                || obj.value[2] != obj.pIndexData.value[2]
                || obj.value[3] != obj.pIndexData.value[3]
                || obj.value[4] != obj.pIndexData.value[4]) {
            fp.sprintf(false, "Val  %d %d %d %d %d\n",
                    obj.value[0], obj.value[1], obj.value[2], obj.value[3],
                    obj.value[4]);
        }

        switch (obj.item_type) {
            case ITEM_POTION:
            case ITEM_SCROLL:
                if (obj.value[1] > 0) {
                    fp.sprintf(false, "Spell 1 '%s'\n", Skill.skills[obj.value[1]].name);
                }

                if (obj.value[2] > 0) {
                    fp.sprintf(false, "Spell 2 '%s'\n", Skill.skills[obj.value[2]].name);
                }

                if (obj.value[3] > 0) {
                    fp.sprintf(false, "Spell 3 '%s'\n", Skill.skills[obj.value[3]].name);
                }

                break;

            case ITEM_PILL:
            case ITEM_STAFF:
            case ITEM_WAND:
                if (obj.value[3] > 0) {
                    fp.sprintf(false, "Spell 3 '%s'\n", Skill.skills[obj.value[3]].name);
                }

                break;
        }

        for (paf = obj.affected; paf != null; paf = paf.next) {
            if (paf.type == null) {
                continue;
            }
            fp.sprintf(false, "Affc '%s' %3d %3d %3d %3d %3d %10d\n",
                    paf.type.name,
                    paf.where,
                    paf.level,
                    paf.duration,
                    paf.modifier,
                    paf.location,
                    paf.bitvector
            );
        }

        for (ed = obj.extra_descr; ed != null; ed = ed.next) {
            fp.sprintf(false, "ExDe %s~ %s~\n",
                    ed.keyword, ed.description);
        }

        fp.sprintf(false, "End\n\n");

        if (obj.contains != null) {
            fwrite_obj(ch, obj.contains, fp, iNest + 1);
        }

    }

/*
* Load a char and inventory into a new ch structure.
*/

    static boolean load_char_obj(DESCRIPTOR_DATA d, String name) {
        CHAR_DATA ch;
        boolean found;
        int stat;

        ch = new_char();
        ch.pcdata = new PC_DATA();

        d.character = ch;
        ch.desc = d;
        ch.name = name;
        ch.id = get_pc_id();
        ch.race = Race.HUMAN;
        ch.pcdata.race = ch.race;
        ch.cabal = 0;
        ch.hometown = 0;
        ch.ethos = ETHOS_ANY;
        ch.affected_by = 0;
        ch.act = (PLR_NOSUMMON | PLR_NOCANCEL);
        ch.comm = COMM_COMBINE | COMM_PROMPT;

        ch.pcdata.perm_hit = 20;
        ch.pcdata.perm_mana = 100;
        ch.pcdata.perm_move = 100;

        ch.invis_level = 0;
        ch.practice = 0;
        ch.train = 0;
        ch.hitroll = 0;
        ch.damroll = 0;
        ch.trust = 0;
        ch.wimpy = 0;
        ch.saving_throw = 0;
        ch.progtypes = 0;
        ch.extracted = false;
        ch.pcdata.points = 0;
        ch.prompt = DEFAULT_PROMPT;
        ch.pcdata.confirm_delete = false;
        ch.pcdata.confirm_remort = false;
        ch.pcdata.pwd = "";
        ch.pcdata.bamfin = "";
        ch.pcdata.bamfout = "";
        ch.pcdata.title = "";

        ch.pcdata.time_flag = 0;

        for (stat = 0; stat < MAX_STATS; stat++) {
            ch.perm_stat[stat] = 13;
        }

        ch.pcdata.condition[COND_THIRST] = 48;
        ch.pcdata.condition[COND_FULL] = 48;
        ch.pcdata.condition[COND_HUNGER] = 48;
        ch.pcdata.condition[COND_BLOODLUST] = 48;
        ch.pcdata.condition[COND_DESIRE] = 48;

        ch.pcdata.nextquest = 0;
        ch.pcdata.questpoints = 0;
        ch.pcdata.questgiver = 0;
        ch.pcdata.countdown = 0;
        ch.pcdata.questobj = 0;
        ch.pcdata.questmob = 0;
        ch.religion = RELIGION_NONE;
        ch.pcdata.has_killed = 0;
        ch.pcdata.anti_killed = 0;
        ch.timer = 0;
        ch.hunting = null;
        ch.endur = 0;
        ch.riding = false;
        ch.mount = null;
        ch.in_mind = null;

        found = false;

        String strsave = nw_config.lib_player_dir + "/" + capitalize(name);
        try {
            DikuTextFile fp = new DikuTextFile(strsave);
            Arrays.fill(rgObjNest, null);
            found = true;
            for (; ; ) {
                char letter;
                String word;

                letter = fp.fread_letter();
                if (letter == '*') {
                    fp.fread_to_eol();
                    continue;
                }

                if (letter != '#') {
                    bug("Load_char_obj: # not found.");
                    break;
                }

                word = fp.fread_word();
                if (!str_cmp(word, "PLAYER")) {
                    fread_char(ch, fp);
                } else if (!str_cmp(word, "OBJECT")) {
                    fread_obj(ch, fp);
                } else if (!str_cmp(word, "O")) {
                    fread_obj(ch, fp);
                } else if (!str_cmp(word, "PET")) {
                    fread_pet(ch, fp);
                } else if (!str_cmp(word, "END")) {
                    break;
                } else {
                    bug("Load_char_obj: bad section.");
                    break;
                }
            }


        } catch (IOException e) {
            perror("load char obj error:" + e.getMessage());
        }

        /* initialize race */
        if (found) {
            if (ORG_RACE(ch) == null) {
                SET_ORG_RACE(ch, Race.HUMAN);
            }
            if (ch.race == null) {
                ch.race = Race.HUMAN;
            }

            ch.size = ORG_RACE(ch).pcRace.size;
            ch.dam_type = 17; /*punch */

            ch.affected_by = ch.affected_by | ch.race.aff;
            ch.imm_flags = ch.imm_flags | ch.race.imm;
            ch.res_flags = ch.res_flags | ch.race.res;
            ch.vuln_flags = ch.vuln_flags | ch.race.vuln;
            ch.form = ch.race.form;
            ch.parts = ch.race.parts;

            if (ch.pcdata.condition[COND_BLOODLUST] < 48 && ch.clazz != Clazz.VAMPIRE) {
                ch.pcdata.condition[COND_BLOODLUST] = 48;
            }

            /*
            * Add the bonus time now, because we don't allow old players
            * to be loaded with limited equipments!
            */
            if (IS_SET(ch.pcdata.time_flag, TLP_NOLOG)) {
                int l, add_time;

                add_time = nw_config.max_time_log + nw_config.min_time_limit / nw_config.max_time_log;

                for (l = 0; l < nw_config.max_time_log; l++) {
                    ch.pcdata.log_time[l] = add_time;
                }
                ch.pcdata.time_flag = REMOVE_BIT(ch.pcdata.time_flag, TLP_NOLOG);
            }

        }

        return found;
    }

/*
* Read in a char.
*/


    static void fread_char(CHAR_DATA ch, DikuTextFile fp) throws IOException {
        String word;
        boolean fPlayLog = false;
        int count = 0;
        int lastlogoff = current_time;


        TextBuffer buf = new TextBuffer();
        buf.sprintf("Loading %s.", ch.name);
        log_string(buf);
        ch.pcdata.bank_s = 0;
        ch.pcdata.bank_g = 0;

        for (; ; ) {
            word = fp.fread_word();
            if (word == null) {
                word = "End";
            }
            fp.fMatch = false;
            switch (UPPER(word.charAt(0))) {
                case '*':
                    fp.fMatch = true;
                    fp.fread_to_eol();
                    break;

                case 'A':
                    ch.act = fp.FLAG64_OLD("Act", word, ch.act);
                    fp.FLAG64_OLD("AffectedBy", word, 0);
                    fp.FLAG64_OLD("AfBy", word, 0);
                    ch.alignment = fp.NKEY("Alignment", word, ch.alignment);
                    ch.alignment = fp.NKEY("Alig", word, ch.alignment);
                    ch.pcdata.anti_killed = fp.NKEY("AntKilled", word, ch.pcdata.anti_killed);

                    if (!str_cmp(word, "Alia")) {
                        if (count >= nw_config.max_alias) {
                            fp.fread_to_eol();
                            fp.fMatch = true;
                            break;
                        }

                        ch.pcdata.alias[count] = fp.fread_word();
                        ch.pcdata.alias_sub[count] = fp.fread_word();
                        count++;
                        fp.fMatch = true;
                        break;
                    }

                    if (!str_cmp(word, "Alias")) {
                        if (count >= nw_config.max_alias) {
                            fp.fread_to_eol();
                            fp.fMatch = true;
                            break;
                        }

                        ch.pcdata.alias[count] = fp.fread_word();
                        ch.pcdata.alias_sub[count] = fp.fread_string();
                        count++;
                        fp.fMatch = true;
                        break;
                    }

                    if (!str_cmp(word, "AC") || !str_cmp(word, "Armor")) {
                        fp.fread_to_eol();
                        fp.fMatch = true;
                        break;
                    }

                    if (!str_cmp(word, "ACs")) {
                        int i;

                        for (i = 0; i < 4; i++) {
                            ch.armor[i] = fp.fread_number();
                        }
                        fp.fMatch = true;
                        break;
                    }

                    if (!str_cmp(word, "AffD")) {

                        AFFECT_DATA paf = new AFFECT_DATA();
                        Skill sn = lookupSkill(fp.fread_word());
                        if (sn == null) {
                            bug("Fread_char: unknown skill.");
                        } else {
                            paf.type = sn;
                        }

                        paf.level = fp.fread_number();
                        paf.duration = fp.fread_number();
                        paf.modifier = fp.fread_number();
                        paf.location = fp.fread_number();
                        paf.bitvector = fp.fread_number();
                        paf.next = ch.affected;
                        ch.affected = paf;
                        fp.fMatch = true;
                        break;
                    }

                    if (!str_cmp(word, "Affc")) {
                        AFFECT_DATA paf = new AFFECT_DATA();
                        Skill sn = lookupSkill(fp.fread_word());
                        if (sn == null) {
                            bug("Fread_char: unknown skill.");
                        } else {
                            paf.type = sn;
                        }

                        paf.where = fp.fread_number();
                        paf.level = fp.fread_number();
                        paf.duration = fp.fread_number();
                        paf.modifier = fp.fread_number();
                        paf.location = fp.fread_number();
                        paf.bitvector = fp.fread_number();
                        paf.next = ch.affected;
                        ch.affected = paf;
                        fp.fMatch = true;
                        break;
                    }

                    if (!str_cmp(word, "AttrMod") || !str_cmp(word, "AMod")) {
                        int stat;
                        for (stat = 0; stat < MAX_STATS; stat++) {
                            fp.fread_number();
                        }
                        fp.fMatch = true;
                        break;
                    }

                    if (!str_cmp(word, "AttrPerm") || !str_cmp(word, "Attr")) {
                        int stat;

                        for (stat = 0; stat < MAX_STATS; stat++) {
                            ch.perm_stat[stat] = fp.fread_number();
                        }
                        fp.fMatch = true;
                        break;
                    }
                    break;

                case 'B':
                    ch.pcdata.bamfin = fp.SKEY("Bamfin", word, ch.pcdata.bamfin);
                    ch.pcdata.bank_s = fp.NKEY("Banks", word, ch.pcdata.bank_s);
                    ch.pcdata.bank_g = fp.NKEY("Bankg", word, ch.pcdata.bank_g);
                    ch.pcdata.bamfout = fp.SKEY("Bamfout", word, ch.pcdata.bamfout);
                    ch.pcdata.bamfin = fp.SKEY("Bin", word, ch.pcdata.bamfin);
                    ch.pcdata.bamfout = fp.SKEY("Bout", word, ch.pcdata.bamfout);
                    break;

                case 'C':
                    if (!str_prefix(word, "Cla")) {
                        fp.fMatch = true;
                        String className = fp.fread_word();
                        ch.clazz = Clazz.lookupClass(className);
                        if (ch.clazz == null) {
                            throw new RuntimeException("Class is not found:" + className);
                        }
                    } else {
                        ch.cabal = fp.NKEY("Cab", word, ch.cabal);
                    }

                    if (!str_cmp(word, "Condition") || !str_cmp(word, "Cond")) {
                        ch.pcdata.condition[0] = fp.fread_number();
                        ch.pcdata.condition[1] = fp.fread_number();
                        ch.pcdata.condition[2] = fp.fread_number();
                        fp.fMatch = true;
                        break;
                    }
                    if (!str_cmp(word, "CndC")) {
                        ch.pcdata.condition[0] = fp.fread_number();
                        ch.pcdata.condition[1] = fp.fread_number();
                        ch.pcdata.condition[2] = fp.fread_number();
                        ch.pcdata.condition[3] = fp.fread_number();
                        ch.pcdata.condition[4] = fp.fread_number();
                        ch.pcdata.condition[5] = fp.fread_number();
                        fp.fMatch = true;
                        break;
                    }
                    if (!str_cmp(word, "Cnd")) {
                        ch.pcdata.condition[0] = fp.fread_number();
                        ch.pcdata.condition[1] = fp.fread_number();
                        ch.pcdata.condition[2] = fp.fread_number();
                        ch.pcdata.condition[3] = fp.fread_number();
                        fp.fMatch = true;
                        break;
                    }
                    ch.comm = (int) fp.FLAG64_OLD("Comm", word, ch.comm);

                    break;

                case 'D':
                    fp.NKEY("Damroll", word, 0);
                    fp.NKEY("Dam", word, 0);
                    ch.description = fp.SKEY("Description", word, ch.description);
                    ch.description = fp.SKEY("Desc", word, ch.description);
                    ch.pcdata.death = fp.NKEY("Dead", word, ch.pcdata.death);
                    fp.FLAG64_OLD("Detect", word, 0);
                    break;

                case 'E':
                    if (!str_cmp(word, "End")) {
                        /* adjust hp mana move up  -- here for speed's sake
                        int percent;

                            percent = (current_time - lastlogoff) * 25 / ( 2 * 60 * 60);

                        percent = UMIN(percent,100);

                            if (percent > 0 && !IS_AFFECTED(ch,AFF_POISON)
                            &&  !IS_AFFECTED(ch,AFF_PLAGUE))
                            {
                                ch.hit += (ch.max_hit - ch.hit) * percent / 100;
                                ch.mana    += (ch.max_mana - ch.mana) * percent / 100;
                                ch.move    += (ch.max_move - ch.move)* percent / 100;
                            }
                        */
                        ch.played = ch.pcdata.played;

                        /* if this is an old player, give some played time */
                        if (!fPlayLog) {
                            ch.pcdata.time_flag = SET_BIT(ch.pcdata.time_flag, TLP_NOLOG);
                        }
                        return;
                    }
                    ch.exp = fp.NKEY("Exp", word, ch.exp);
                    ch.ethos = fp.NKEY("Etho", word, ch.ethos);
                    break;

                case 'G':
                    ch.gold = fp.NKEY("Gold", word, ch.gold);
                    if (!str_cmp(word, "Group") || !str_cmp(word, "Gr")) {
                        fp.fread_word();
                        fp.fMatch = true;
                    }
                    break;

                case 'H':
                    fp.NKEY("Hitroll", word, 0);
                    fp.NKEY("Hit", word, 0);
                    ch.hometown = fp.NKEY("Home", word, ch.hometown);
                    ch.pcdata.has_killed = fp.NKEY("Haskilled", word, ch.pcdata.has_killed);

                    if (!str_cmp(word, "HpManaMove") || !str_cmp(word, "HMV")) {
                        ch.hit = fp.fread_number();
                        ch.mana = fp.fread_number();
                        ch.move = fp.fread_number();
                        fp.fMatch = true;
                        break;
                    }

                    if (!str_cmp(word, "HpManaMovePerm") || !str_cmp(word, "HMVP")) {
                        ch.pcdata.perm_hit = fp.fread_number();
                        ch.pcdata.perm_mana = fp.fread_number();
                        ch.pcdata.perm_move = fp.fread_number();
                        fp.fMatch = true;
                        break;
                    }

                    break;

                case 'I':
                    ch.id = fp.NKEY("Id", word, ch.id);
                    ch.invis_level = fp.NKEY("InvisLevel", word, ch.invis_level);
                    ch.incog_level = fp.NKEY("Inco", word, ch.incog_level);
                    ch.invis_level = fp.NKEY("Invi", word, ch.invis_level);
                    break;

                case 'L':
                    ch.pcdata.last_level = fp.NKEY("LastLevel", word, ch.pcdata.last_level);
                    ch.pcdata.last_level = fp.NKEY("LLev", word, ch.pcdata.last_level);
                    ch.level = fp.NKEY("Level", word, ch.level);
                    ch.level = fp.NKEY("Lev", word, ch.level);
                    ch.level = fp.NKEY("Levl", word, ch.level);
                    lastlogoff = fp.NKEY("LogO", word, lastlogoff);
                    ch.long_descr = fp.SKEY("LongDescr", word, ch.long_descr);
                    ch.long_descr = fp.SKEY("LnD", word, ch.long_descr);
                    break;

                case 'N':
                    ch.name = fp.SKEY("Name", word, ch.name);
                    ch.pcdata.last_note = fp.NKEY("Note", word, ch.pcdata.last_note);
                    if (!str_cmp(word, "Not")) {
                        ch.pcdata.last_note = fp.fread_number();
                        ch.pcdata.last_idea = fp.fread_number();
                        ch.pcdata.last_penalty = fp.fread_number();
                        ch.pcdata.last_news = fp.fread_number();
                        ch.pcdata.last_changes = fp.fread_number();
                        fp.fMatch = true;
                        break;
                    }
                    break;

                case 'P':
                    ch.pcdata.pwd = fp.SKEY("Password", word, ch.pcdata.pwd);
                    ch.pcdata.pwd = fp.SKEY("Pass", word, ch.pcdata.pwd);
                    ch.pcdata.played = fp.NKEY("Played", word, ch.pcdata.played);
                    ch.pcdata.played = fp.NKEY("Plyd", word, ch.pcdata.played);
                    ch.pcdata.points = fp.NKEY("Points", word, ch.pcdata.points);
                    ch.pcdata.points = fp.NKEY("Pnts", word, ch.pcdata.points);
                    ch.position = fp.NKEY("Position", word, ch.position);
                    ch.position = fp.NKEY("Pos", word, ch.position);
                    ch.practice = fp.NKEY("Practice", word, ch.practice);
                    ch.practice = fp.NKEY("Prac", word, ch.practice);
                    ch.prompt = fp.SKEY("Prompt", word, ch.prompt);
                    ch.prompt = fp.SKEY("Prom", word, ch.prompt);
                    if (!str_cmp(word, "PlayLog")) {
                        int l, d, t, d_start, d_stop;

                        d_start = get_played_day(nw_config.max_time_log - 1);
                        d_stop = get_played_day(0);

                        for (l = 0; l < MAX_TIME_LOG; l++) {
                            ch.pcdata.log_time[l] = 0;
                        }

                        fp.fread_number();   /* read the version */

                        while (!fp.feof()) {
                            if ((d = fp.fread_number()) < 0) {
                                break;
                            }
                            t = fp.fread_number();

                            if (d >= d_start && d <= d_stop) {
                                l = d_stop - d;
                                ch.pcdata.log_time[l] += t;
                            }
                        }
                        fPlayLog = true;
                        fp.fMatch = true;
                        break;
                    }
                    break;

                case 'Q':
                    ch.pcdata.countdown = fp.NKEY("QuestCnt", word, ch.pcdata.countdown);
                    ch.pcdata.questmob = fp.NKEY("QuestMob", word, ch.pcdata.questmob);
                    ch.pcdata.questobj = fp.NKEY("QuestObj", word, ch.pcdata.questobj);
                    ch.pcdata.questgiver = fp.NKEY("QuestGiv", word, ch.pcdata.questgiver);
                    ch.pcdata.questpoints = fp.NKEY("QuestPnts", word, ch.pcdata.questpoints);
                    ch.pcdata.nextquest = fp.NKEY("QuestNext", word, ch.pcdata.nextquest);
                    ch.quest = (int) fp.FLAG64_OLD("Ques", word, ch.quest);
                    break;

                case 'R':
                    ch.religion = fp.NKEY("Relig", word, ch.religion);

/*      KEY( "Race",        ch.race,  Race.lookupRace(fp.fread_string()) ); */
                    if (!str_cmp(word, "Race")) {
                        ch.race = Race.lookupRace(fp.fread_string());
                        SET_ORG_RACE(ch, ch.race);
                        fp.fMatch = true;
                        break;
                    }
                    if (!str_cmp(word, "Room")) {
                        ch.in_room = get_room_index(fp.fread_number());
                        if (ch.in_room == null) {
                            ch.in_room = get_room_index(ROOM_VNUM_LIMBO);
                        }
                        fp.fMatch = true;
                        break;
                    }

                    break;

                case 'S':
                    fp.NKEY("SavingThrow", word, 0);
                    fp.NKEY("Save", word, 0);
                    ch.lines = fp.NKEY("Scro", word, ch.lines);
                    ch.sex = fp.NKEY("Sex", word, ch.sex);
                    ch.short_descr = fp.SKEY("ShortDescr", word, ch.short_descr);
                    ch.short_descr = fp.SKEY("ShD", word, ch.short_descr);
                    ch.silver = fp.NKEY("Silv", word, ch.silver);


                    if (!str_cmp(word, "Skill") || !str_cmp(word, "Sk")) {
                        int value;
                        String temp;

                        value = fp.fread_number();
                        temp = fp.fread_word();
                        Skill sn = lookupSkill(temp);
                        /* sn    = lookupSkill( fread_word( fp ) ); */
                        if (sn == null) {
                            perror(temp);
                            bug("Fread_char: unknown skill. ");
                        } else {
                            ch.pcdata.learned[sn.ordinal()] = value;
                        }
                        fp.fMatch = true;
                    }

                    break;

                case 'T':
                    ch.pcdata.true_sex = fp.NKEY("trueSex", word, ch.pcdata.true_sex);
                    ch.pcdata.true_sex = fp.NKEY("TSex", word, ch.pcdata.true_sex);
                    ch.train = fp.NKEY("Trai", word, ch.train);
                    ch.trust = fp.NKEY("Trust", word, ch.trust);
                    ch.trust = fp.NKEY("Tru", word, ch.trust);

                    if (!str_cmp(word, "Title") || !str_cmp(word, "Titl")) {
                        ch.pcdata.title = fp.fread_string();
                        if (ch.pcdata.title.charAt(0) != '.' && ch.pcdata.title.charAt(0) != ','
                                && ch.pcdata.title.charAt(0) != '!' && ch.pcdata.title.charAt(0) != '?') {
                            ch.pcdata.title = " " + ch.pcdata.title;
                        }
                        fp.fMatch = true;
                        break;
                    }

                    break;

                case 'V':
                    if (!str_cmp(word, "Vnum")) {
                        ch.pIndexData = get_mob_index(fp.fread_number());
                        fp.fMatch = true;
                        break;
                    }
                    break;

                case 'W':
                    ch.wimpy = fp.NKEY("Wimpy", word, ch.wimpy);
                    ch.wimpy = fp.NKEY("Wimp", word, ch.wimpy);
                    ch.wiznet = (int) fp.FLAG64_OLD("Wizn", word, ch.wiznet);
                    break;
            }

            if (!fp.fMatch) {
                bug("Fread_char: no match.");
                fp.fread_to_eol();
            }
        }
    }

/* load a pet from the forgotten reaches */


    static void fread_pet(CHAR_DATA ch, DikuTextFile fp) throws IOException {
        String word;
        CHAR_DATA pet;
        int lastlogoff = current_time;
        int percent;

        /* first entry had BETTER be the vnum or we barf */
        word = fp.feof() ? "END" : fp.fread_word();
        if (!str_cmp(word, "Vnum")) {
            int vnum;

            vnum = fp.fread_number();
            if (get_mob_index(vnum) == null) {
                bug("Fread_pet: bad vnum %d.", vnum);
                pet = create_mobile(get_mob_index(MOB_VNUM_FIDO));
            } else {
                pet = create_mobile(get_mob_index(vnum));
            }
        } else {
            bug("Fread_pet: no vnum in file.");
            pet = create_mobile(get_mob_index(MOB_VNUM_FIDO));
        }

        for (; ; ) {
            word = fp.feof() ? "END" : fp.fread_word();
            fp.fMatch = false;

            switch (UPPER(word.charAt(0))) {
                case '*':
                    fp.fMatch = true;
                    fp.fread_to_eol();
                    break;

                case 'A':
                    pet.act = fp.FLAG64_OLD("Act", word, pet.act);
                    pet.affected_by = fp.FLAG64_OLD("AfBy", word, pet.affected_by);
                    pet.alignment = fp.NKEY("Alig", word, pet.alignment);

                    if (!str_cmp(word, "ACs")) {
                        int i;

                        for (i = 0; i < 4; i++) {
                            pet.armor[i] = fp.fread_number();
                        }
                        fp.fMatch = true;
                        break;
                    }

                    if (!str_cmp(word, "AffD")) {
                        AFFECT_DATA paf = new AFFECT_DATA();

                        Skill sn = lookupSkill(fp.fread_word());
                        if (sn == null) {
                            bug("Fread_char: unknown skill.");
                        } else {
                            paf.type = sn;
                        }

                        paf.level = fp.fread_number();
                        paf.duration = fp.fread_number();
                        paf.modifier = fp.fread_number();
                        paf.location = fp.fread_number();
                        paf.bitvector = fp.fread_number();
                        paf.next = pet.affected;
                        pet.affected = paf;
                        fp.fMatch = true;
                        break;
                    }

                    if (!str_cmp(word, "Affc")) {


                        AFFECT_DATA paf = new AFFECT_DATA();

                        Skill sn = lookupSkill(fp.fread_word());
                        if (sn == null) {
                            bug("Fread_char: unknown skill.");
                        } else {
                            paf.type = sn;
                        }

                        paf.where = fp.fread_number();
                        paf.level = fp.fread_number();
                        paf.duration = fp.fread_number();
                        paf.modifier = fp.fread_number();
                        paf.location = fp.fread_number();
                        paf.bitvector = fp.fread_number();
                        paf.next = pet.affected;
                        pet.affected = paf;
                        fp.fMatch = true;
                        break;
                    }

                    if (!str_cmp(word, "AMod")) {
                        int stat;

                        for (stat = 0; stat < MAX_STATS; stat++) {
                            pet.mod_stat[stat] = fp.fread_number();
                        }
                        fp.fMatch = true;
                        break;
                    }

                    if (!str_cmp(word, "Attr")) {
                        int stat;

                        for (stat = 0; stat < MAX_STATS; stat++) {
                            pet.perm_stat[stat] = fp.fread_number();
                        }
                        fp.fMatch = true;
                        break;
                    }
                    break;

                case 'C':
                    pet.cabal = fp.NKEY("Cab", word, pet.cabal);
                    pet.comm = (int) fp.FLAG64_OLD("Comm", word, pet.comm);
                    break;

                case 'D':
                    pet.damroll = fp.NKEY("Dam", word, pet.damroll);
                    pet.description = fp.SKEY("Desc", word, pet.description);
                    break;

                case 'E':
                    if (!str_cmp(word, "End")) {
                        pet.leader = ch;
                        pet.master = ch;
                        ch.pet = pet;
                        /* adjust hp mana move up  -- here for speed's sake */
                        percent = (current_time - lastlogoff) * 25 / (2 * 60 * 60);

                        if (percent > 0 && !IS_AFFECTED(ch, AFF_POISON) && !IS_AFFECTED(ch, AFF_PLAGUE)) {
                            percent = UMIN(percent, 100);
                            pet.hit += (pet.max_hit - pet.hit) * percent / 100;
                            pet.mana += (pet.max_mana - pet.mana) * percent / 100;
                            pet.move += (pet.max_move - pet.move) * percent / 100;
                        }
                        pet.in_room = get_room_index(ROOM_VNUM_LIMBO);
                        return;
                    }
                    pet.exp = fp.NKEY("Exp", word, pet.exp);
                    break;

                case 'G':
                    pet.gold = fp.NKEY("Gold", word, pet.gold);
                    break;

                case 'H':
                    pet.hitroll = fp.NKEY("Hit", word, pet.hitroll);

                    if (!str_cmp(word, "HMV")) {
                        pet.hit = fp.fread_number();
                        pet.max_hit = fp.fread_number();
                        pet.mana = fp.fread_number();
                        pet.max_mana = fp.fread_number();
                        pet.move = fp.fread_number();
                        pet.max_move = fp.fread_number();
                        fp.fMatch = true;
                        break;
                    }
                    break;

                case 'L':
                    pet.level = fp.NKEY("Levl", word, pet.level);
                    pet.long_descr = fp.SKEY("LnD", word, pet.long_descr);
                    lastlogoff = fp.NKEY("LogO", word, lastlogoff);
                    break;

                case 'N':
                    pet.name = fp.SKEY("Name", word, pet.name);
                    break;

                case 'P':
                    pet.position = fp.NKEY("Pos", word, pet.position);
                    break;

                case 'R':
                    if (!str_cmp(word, "Race")) {
                        pet.race = Race.lookupRace(fp.fread_string());
                        SET_ORG_RACE(pet, pet.race);
                        fp.fMatch = true;
                        break;
                    }
                    break;

                case 'S':
                    pet.saving_throw = fp.NKEY("Save", word, pet.saving_throw);
                    pet.sex = fp.NKEY("Sex", word, pet.sex);
                    pet.short_descr = fp.SKEY("ShD", word, pet.short_descr);
                    pet.silver = fp.NKEY("Silv", word, pet.silver);
                    break;


            }
            if (!fp.fMatch) {
                bug("Fread_pet: no match.");
                fp.fread_to_eol();
            }

        }
    }


    static void fread_obj(CHAR_DATA ch, DikuTextFile fp) throws IOException {
        OBJ_DATA obj;
        String word;
        int iNest;
        boolean fNest;
        boolean fVnum;
        boolean first;
        boolean new_format;  /* to prevent errors */
        boolean make_new;    /* update object */

        obj = null;
        first = true;  /* used to counter fp offset */
        new_format = false;
        make_new = false;

        word = fp.feof() ? "End" : fp.fread_word();
        if (!str_cmp(word, "Vnum")) {
            int vnum;
            first = false;  /* fp will be in right place */

            vnum = fp.fread_number();
            if (get_obj_index(vnum) == null) {
                bug("Fread_obj: bad vnum %d.", vnum);
            } else {
                obj = create_object_nocount(get_obj_index(vnum), -1);
                new_format = true;
            }

        }

        if (obj == null)  /* either not found or old style */ {
            obj = new OBJ_DATA();
            obj.name = "";
            obj.short_descr = "";
            obj.description = "";
        }

        fNest = false;
        fVnum = true;
        iNest = 0;
        TextBuffer buf = new TextBuffer();
        for (; ; ) {
            if (first) {
                first = false;
            } else {
                word = fp.feof() ? "End" : fp.fread_word();
            }
            fp.fMatch = false;

            switch (UPPER(word.charAt(0))) {
                case '*':
                    fp.fMatch = true;
                    fp.fread_to_eol();
                    break;

                case 'A':
                    if (!str_cmp(word, "AffD")) {

                        AFFECT_DATA paf = new AFFECT_DATA();

                        Skill sn = lookupSkill(fp.fread_word());
                        if (sn == null) {
                            bug("Fread_obj: unknown skill.");
                        } else {
                            paf.type = sn;
                        }

                        paf.level = fp.fread_number();
                        paf.duration = fp.fread_number();
                        paf.modifier = fp.fread_number();
                        paf.location = fp.fread_number();
                        paf.bitvector = fp.fread_number();
                        paf.next = obj.affected;
                        obj.affected = paf;
                        fp.fMatch = true;
                        break;
                    }
                    if (!str_cmp(word, "Affc")) {

                        AFFECT_DATA paf = new AFFECT_DATA();

                        Skill sn = lookupSkill(fp.fread_word());
                        if (sn == null) {
                            bug("Fread_obj: unknown skill.");
                        } else {
                            paf.type = sn;
                        }

                        paf.where = fp.fread_number();
                        paf.level = fp.fread_number();
                        paf.duration = fp.fread_number();
                        paf.modifier = fp.fread_number();
                        paf.location = fp.fread_number();
                        paf.bitvector = fp.fread_number();
                        paf.next = obj.affected;
                        obj.affected = paf;
                        fp.fMatch = true;
                        break;
                    }
                    break;

                case 'C':
                    if (!str_cmp(word, "Cond")) {
                        obj.condition = fp.fread_number();
                        if (obj.condition < 1) {
                            obj.condition = 100;
                        }
                        fp.fMatch = true;
                        break;
                    }
                    obj.cost = fp.NKEY("Cost", word, obj.cost);
                    break;

                case 'D':
                    obj.description = fp.SKEY("Description", word, obj.description);
                    obj.description = fp.SKEY("Desc", word, obj.description);
                    break;

                case 'E':

                    if (!str_cmp(word, "Enchanted")) {
                        obj.enchanted = true;
                        fp.fMatch = true;
                        break;
                    }

                    obj.extra_flags = fp.FLAG64_OLD("ExtraFlags", word, obj.extra_flags);
                    obj.extra_flags = fp.FLAG64_OLD("ExtF", word, obj.extra_flags);

                    if (!str_cmp(word, "ExtraDescr") || !str_cmp(word, "ExDe")) {
                        EXTRA_DESCR_DATA ed = new EXTRA_DESCR_DATA();

                        ed.keyword = fp.fread_string();
                        ed.description = fp.fread_string();
                        ed.next = obj.extra_descr;
                        obj.extra_descr = ed;
                        fp.fMatch = true;
                    }

                    if (!str_cmp(word, "End")) {
                        if (!fNest || !fVnum || obj.pIndexData == null) {
                            bug("Fread_obj: incomplete object.");
                            return;
                        } else if (obj.pIndexData.limit != -1 && get_total_played(ch) < nw_config.min_time_limit) {
                            sprintf(buf, "Ignoring limited %d for %s.", obj.pIndexData.vnum, ch.name);
                            log_string(buf);
                            extract_obj_nocount(obj);
                            rgObjNest[iNest] = null;
                            return;
                        }
                        {
                            if (!new_format) {
                                obj.next = object_list;
                                object_list = obj;
                                obj.pIndexData.count++;
                            }

                            if (!obj.pIndexData.new_format
                                    && obj.item_type == ITEM_ARMOR
                                    && obj.value[1] == 0) {
                                obj.value[1] = obj.value[0];
                                obj.value[2] = obj.value[0];
                            }
                            if (make_new) {
                                int wear;

                                wear = obj.wear_loc;
                                extract_obj(obj);

                                obj = create_object(obj.pIndexData, 0);
                                obj.wear_loc = wear;
                            }
                            if (iNest == 0 || rgObjNest[iNest - 1] == null) {
                                obj_to_char(obj, ch);
                            } else {
                                obj_to_obj(obj, rgObjNest[iNest - 1]);
                            }
                            return;
                        }
                    }
                    break;

                case 'I':
                    obj.item_type = fp.NKEY("ItemType", word, obj.item_type);
                    obj.item_type = fp.NKEY("Ityp", word, obj.item_type);
                    break;

                case 'L':
                    obj.level = fp.NKEY("Level", word, obj.level);
                    obj.level = fp.NKEY("Lev", word, obj.level);
                    break;

                case 'N':
                    obj.name = fp.SKEY("Name", word, obj.name);

                    if (!str_cmp(word, "Nest")) {
                        iNest = fp.fread_number();
                        if (iNest < 0 || iNest >= MAX_NEST) {
                            bug("Fread_obj: bad nest %d.", iNest);
                        } else {
                            rgObjNest[iNest] = obj;
                            fNest = true;
                        }
                        fp.fMatch = true;
                    }
                    break;

                case 'O':
                    if (!str_cmp(word, "Oldstyle")) {
                        if (obj.pIndexData != null && obj.pIndexData.new_format) {
                            make_new = true;
                        }
                        fp.fMatch = true;
                    }
                    break;


                case 'Q':
                    obj.condition = fp.NKEY("Quality", word, obj.condition);
                    break;

                case 'S':
                    obj.short_descr = fp.SKEY("ShortDescr", word, obj.short_descr);
                    obj.short_descr = fp.SKEY("ShD", word, obj.short_descr);

                    if (!str_cmp(word, "Spell")) {
                        int iValue;

                        iValue = fp.fread_number();
                        Skill sn = lookupSkill(fp.fread_word());
                        if (iValue < 0 || iValue > 3) {
                            bug("Fread_obj: bad iValue %d.", iValue);
                        } else if (sn == null) {
                            bug("Fread_obj: unknown skill.");
                        } else {
                            obj.value[iValue] = sn.ordinal();
                        }
                        fp.fMatch = true;
                        break;
                    }

                    break;

                case 'T':
                    obj.timer = fp.NKEY("Timer", word, obj.timer);
                    obj.timer = fp.NKEY("Time", word, obj.timer);
                    break;

                case 'V':
                    if (!str_cmp(word, "Values") || !str_cmp(word, "Vals")) {
                        obj.value[0] = fp.fread_number();
                        obj.value[1] = fp.fread_number();
                        obj.value[2] = fp.fread_number();
                        obj.value[3] = fp.fread_number();
                        if (obj.item_type == ITEM_WEAPON && obj.value[0] == 0) {
                            obj.value[0] = obj.pIndexData.value[0];
                        }
                        fp.fMatch = true;
                        break;
                    }

                    if (!str_cmp(word, "Val")) {
                        obj.value[0] = fp.fread_number();
                        obj.value[1] = fp.fread_number();
                        obj.value[2] = fp.fread_number();
                        obj.value[3] = fp.fread_number();
                        obj.value[4] = fp.fread_number();
                        fp.fMatch = true;
                        break;
                    }

                    if (!str_cmp(word, "Vnum")) {
                        int vnum;

                        vnum = fp.fread_number();
                        if ((obj.pIndexData = get_obj_index(vnum)) == null) {
                            bug("Fread_obj: bad vnum %d.", vnum);
                        } else {
                            fVnum = true;
                        }
                        fp.fMatch = true;
                        break;
                    }
                    break;

                case 'W':
                    obj.wear_flags = fp.NKEY("WearFlags", word, obj.wear_flags);
                    obj.wear_flags = fp.NKEY("WeaF", word, obj.wear_flags);
                    obj.weight = fp.NKEY("Weight", word, obj.weight);
                    obj.wear_loc = fp.NKEY("WLoc", word, obj.wear_loc);
                    obj.weight = fp.NKEY("Wt", word, obj.weight);
                    if (!str_cmp(word, "Wear")) {
                        obj.wear_loc = wear_convert(fp.fread_number());
                        fp.fMatch = true;
                        break;
                    }
                    if (!str_cmp(word, "Wearloc")) {
                        obj.wear_loc = wear_convert(fp.fread_number());
                        fp.fMatch = true;
                        break;
                    }
                    break;

            }

            if (!fp.fMatch) {
                bug("Fread_obj: no match.");
                fp.fread_to_eol();
            }
        }
    }


    static int wear_convert(int oldwear) {
        int iWear;

        switch (oldwear) {
            case OWEAR_FINGER_L:
            case OWEAR_FINGER_R:
                iWear = WEAR_FINGER;
                break;
            case OWEAR_NECK_1:
            case OWEAR_NECK_2:
                iWear = WEAR_NECK;
                break;
            case OWEAR_BODY:
                iWear = WEAR_BODY;
                break;
            case OWEAR_HEAD:
                iWear = WEAR_HEAD;
                break;
            case OWEAR_LEGS:
                iWear = WEAR_LEGS;
                break;
            case OWEAR_FEET:
                iWear = WEAR_FEET;
                break;
            case OWEAR_HANDS:
                iWear = WEAR_HANDS;
                break;
            case OWEAR_ARMS:
                iWear = WEAR_ARMS;
                break;
            case OWEAR_SHIELD:
                iWear = WEAR_LEFT;
                break;
            case OWEAR_ABOUT:
                iWear = WEAR_ABOUT;
                break;
            case OWEAR_WAIST:
                iWear = WEAR_WAIST;
                break;
            case OWEAR_WRIST_L:
            case OWEAR_WRIST_R:
                iWear = WEAR_WRIST;
                break;
            case OWEAR_WIELD:
                iWear = WEAR_RIGHT;
                break;
            case OWEAR_FLOAT:
                iWear = WEAR_FLOAT;
                break;
            case OWEAR_TATTOO:
                iWear = WEAR_TATTOO;
                break;
            case OWEAR_STUCK_IN:
                iWear = WEAR_STUCK_IN;
                break;
            case OWEAR_LIGHT:
            case OWEAR_HOLD:
            case OWEAR_SECOND_WIELD:
            default:
                iWear = WEAR_NONE;
                break;
        }
        return iWear;
    }

}
