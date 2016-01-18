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

import static net.sf.nightworks.Const.language_table;
import static net.sf.nightworks.Nightworks.MAX_LANGUAGE;
import static net.sf.nightworks.Tables.flag_type;
import static net.sf.nightworks.Tables.position_table;
import static net.sf.nightworks.Tables.sex_table;
import static net.sf.nightworks.Tables.size_table;
import static net.sf.nightworks.util.TextUtils.str_prefix;

/**
 * Last mofified:           $Date: 2006-07-11 07:29:11 -0700 (Tue, 11 Jul 2006) $
 * Revision of last commit: $Revision: 18 $
 */
class Lookup {

    static int flag_lookup(String name, flag_type[] flag_table) {
        for (flag_type ft : flag_table) {
            if (!str_prefix(name, ft.name)) {
                return ft.bit;
            }
        }
        return 0;
    }


    static int position_lookup(String name) {
        for (int pos = 0; pos < position_table.length; pos++) {
            if (!str_prefix(name, position_table[pos].name)) {
                return pos;
            }
        }

        return -1;
    }

    static int sex_lookup(String name) {
        for (int sex = 0; sex < sex_table.length; sex++) {
            if (!str_prefix(name, sex_table[sex].name)) {
                return sex;
            }
        }

        return -1;
    }

    static int size_lookup(String name) {
        for (int size = 0; size < size_table.length; size++) {
            if (!str_prefix(name, size_table[size].name)) {
                return size;
            }
        }

        return 0;
    }

    static int lang_lookup(String name) {
        if ((!str_prefix(name, "mothertongue") || !str_prefix(name, "motherlanguage"))) {
            return MAX_LANGUAGE;
        }

        for (int lang = 0; lang < MAX_LANGUAGE; lang++) {
            if (!str_prefix(name, language_table[lang].name)) {
                return lang;
            }
        }

        return -1;
    }
}

