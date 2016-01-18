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

import static net.sf.nightworks.DB.mobile_count;
import static net.sf.nightworks.Handler.affect_remove;
import static net.sf.nightworks.Handler.extract_obj_nocount;
import static net.sf.nightworks.Nightworks.AFFECT_DATA;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.ETHOS_ANY;
import static net.sf.nightworks.Nightworks.IS_NPC;
import static net.sf.nightworks.Nightworks.LANG_COMMON;
import static net.sf.nightworks.Nightworks.MAX_STATS;
import static net.sf.nightworks.Nightworks.OBJ_DATA;
import static net.sf.nightworks.Nightworks.PAGELEN;
import static net.sf.nightworks.Nightworks.POS_STANDING;
import static net.sf.nightworks.Nightworks.current_time;

/**
 * Last mofified:           $Date: 2006-07-20 07:41:52 -0700 (Thu, 20 Jul 2006) $
 * Revision of last commit: $Revision: 25 $
 */
class Recycle {


    static CHAR_DATA new_char() {
        CHAR_DATA ch = new CHAR_DATA();
        ch.name = "";
        ch.short_descr = "";
        ch.long_descr = "";
        ch.description = "";
        ch.prompt = "";
        ch.prefix = "";
        ch.logon = current_time;
        ch.lines = PAGELEN;
        for (int i = 0; i < 4; i++) {
            ch.armor[i] = 100;
        }
        ch.position = POS_STANDING;
        ch.hit = 20;
        ch.max_hit = 20;
        ch.mana = 100;
        ch.max_mana = 100;
        ch.move = 100;
        ch.max_move = 100;

        ch.ethos = ETHOS_ANY;
        ch.cabal = 0;
        ch.hometown = 0;
        ch.guarded_by = null;
        ch.guarding = null;
        ch.doppel = null;
        ch.language = LANG_COMMON;

        for (int i = 0; i < MAX_STATS; i++) {
            ch.perm_stat[i] = 13;
            ch.mod_stat[i] = 0;
        }

        return ch;
    }


    static void free_char(CHAR_DATA ch) {
        OBJ_DATA obj;
        OBJ_DATA obj_next;
        AFFECT_DATA paf;
        AFFECT_DATA paf_next;


        if (IS_NPC(ch)) {
            mobile_count--;
        }

        ch.extracted = true;
        for (obj = ch.carrying; obj != null; obj = obj_next) {
            obj_next = obj.next_content;
            extract_obj_nocount(obj);
        }

        for (paf = ch.affected; paf != null; paf = paf_next) {
            paf_next = paf.next;
            affect_remove(ch, paf);
        }

        if (ch.pcdata != null) {
        }
        ch.extracted = false;
    }

    /* stuff for setting ids */
    private static int last_pc_id;
    private static int last_mob_id;

    static int get_pc_id() {
        int val = (current_time <= last_pc_id) ? last_pc_id + 1 : current_time;
        last_pc_id = val;
        return val;
    }

    static int get_mob_id() {
        last_mob_id++;
        return last_mob_id;
    }
}
