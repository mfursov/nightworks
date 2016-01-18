package net.sf.nightworks.util;
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

/**
 * Last mofified:           $Date: 2006-07-19 06:45:57 -0700 (Wed, 19 Jul 2006) $
 * Revision of last commit: $Revision: 21 $
 */
public class TextUtils {
    public static final boolean[] spaces = new boolean[256];

    static {
        spaces[' '] = spaces['\t'] = spaces['\n'] = spaces['\r'] = true; //spaces['\''] = spaces['"']
    }

    public static boolean isspace(int c) {
        return spaces[c];
    }

    public static boolean isdigit(int c) {
        return c >= '0' && c <= '9';
    }

/*
* Removes the tildes from a string.
* Used for player-entered strings that go into disk files.
*/

    public static String smash_tilde(String str) {
        return str.replace('~', '-');
    }

    /**
     * Compare strings, case insensitive.
     * Return true if different
     * (compatibility with historical functions).
     */

    public static boolean str_cmp(String astr, String bstr) {
        return !astr.equalsIgnoreCase(bstr);
    }

    /**
     * Compare strings, case insensitive, for prefix matching.
     * Return true if astr not a prefix of bstr
     * (compatibility with historical functions).
     */

    //TODO: all such methods . CharSeqeuence!
    public static boolean str_prefix(String astr, String bstr) {
        boolean equals = astr.length() <= bstr.length();
        equals = equals && LOWER(astr.charAt(0)) == LOWER(bstr.charAt(0)) && bstr.subSequence(0, astr.length()).toString().equalsIgnoreCase(astr);
        return !equals;
    }

/*
* Compare strings, case insensitive, for match anywhere.
* Returns true is astr not part of bstr.
*   (compatibility with historical functions).
*/

    public static boolean str_infix(String astr, String bstr) {
        boolean matched;
        if (astr.length() > bstr.length()) {
            matched = false;
        } else {
            String al = astr.toLowerCase();
            String bl = bstr.toLowerCase();
            matched = bl.contains(al);
        }
        return !matched;
    }

/*
* Compare strings, case insensitive, for suffix matching.
* Return true if astr not a suffix of bstr
*   (compatibility with historical functions).
*/

    public static boolean str_suffix(String astr, String bstr) {
        int sstr1 = astr.length();
        int sstr2 = bstr.length();
        return !(sstr1 <= sstr2 && !str_cmp(astr, bstr.substring(sstr2 - sstr1)));
    }

/*
* Returns an initial-capped string.
*/

    public static String capitalize(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + (str.length() > 1 ? str.substring(1).toLowerCase() : "");
    }

    public static char LOWER(char c) {
        return (char) (c >= 'A' && c <= 'Z' ? c + 'a' - 'A' : c);
    }

    public static char UPPER(char c) {
        return (char) (c >= 'a' && c <= 'z' ? c + 'A' - 'a' : c);
    }

/*
* Return true if an argument is completely numeric.
*/

    public static boolean is_number(CharSequence arg) {
        if (arg.length() == 0) {
            return false;
        }
        int i = 0;
        if (arg.charAt(i) == '+' || arg.charAt(i) == '-') {
            i++;
        }
        for (; i < arg.length(); i++) {
            if (!isdigit(arg.charAt(i))) {
                return false;
            }
        }
        return true;
    }

/*
* Pick off one argument from a string and return the rest.
* Understands quotes.
*/

    public static String one_argument(String argument, StringBuilder arg_first) {
        if (argument.length() == 0) {
            return argument;
        }
        argument = trimSpaces(argument, 0);
        if (argument.length() == 0) {
            return argument;
        }

        char cEnd = argument.charAt(0);
        if (cEnd != '\'' && cEnd != '"') {
            arg_first.append(cEnd);
            cEnd = ' ';
        }

        int pos = 1;
        for (; pos < argument.length(); pos++) {
            char c = argument.charAt(pos);
            if (c == cEnd) {
                pos++;
                break;
            }
            arg_first.append(c);
        }
        argument = pos >= argument.length() ? "" : trimSpaces(argument, pos);
        return argument;
    }

    public static String trimSpaces(String argument, int fromPos) {
        int pos = fromPos;
        int len = argument.length();
        while ((pos < len) && (argument.charAt(pos) <= ' ')) {
            pos++;
        }
        if (pos > 0) {
            argument = argument.substring(pos);
        }
        return argument;
    }

}
