package net.sf.nightworks;

import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.Handler.get_char_world;
import static net.sf.nightworks.Lookup.flag_lookup;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.IS_NPC;
import static net.sf.nightworks.Nightworks.IS_SET;
import static net.sf.nightworks.Nightworks.REMOVE_BIT;
import static net.sf.nightworks.Nightworks.SET_BIT;
import static net.sf.nightworks.Tables.act_flags;
import static net.sf.nightworks.Tables.affect_flags;
import static net.sf.nightworks.Tables.comm_flags;
import static net.sf.nightworks.Tables.flag_type;
import static net.sf.nightworks.Tables.form_flags;
import static net.sf.nightworks.Tables.imm_flags;
import static net.sf.nightworks.Tables.part_flags;
import static net.sf.nightworks.Tables.plr_flags;
import static net.sf.nightworks.util.TextUtils.one_argument;
import static net.sf.nightworks.util.TextUtils.str_prefix;

class Flags {

    static void do_flag(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        flag_type[] flag_table;

        StringBuilder arg1 = new StringBuilder();
        StringBuilder arg2 = new StringBuilder();
        StringBuilder arg3 = new StringBuilder();
        argument = one_argument(argument, arg1);
        argument = one_argument(argument, arg2);
        argument = one_argument(argument, arg3);

        char type = argument.charAt(0);
        StringBuilder word = new StringBuilder();
        if (type == '=' || type == '-' || type == '+') {
            argument = one_argument(argument, word);
        }

        if (arg1.isEmpty()) {
            send_to_char("Syntax:\n", ch);
            send_to_char("  flag mob  <name> <field> <flags>\n", ch);
            send_to_char("  flag char <name> <field> <flags>\n", ch);
            send_to_char("  flag obj  <name> <field> <flags>\n", ch);
            send_to_char("  flag room <room> <field> <flags>\n", ch);
            send_to_char("  mob  flags: act,aff,off,imm,res,vuln,form,part\n", ch);
            send_to_char("  char flags: plr,comm,aff,imm,res,vuln,\n", ch);
            send_to_char("  obj  flags: extra,wear,weap,cont,gate,exit\n", ch);
            send_to_char("  room flags: room\n", ch);
            send_to_char("  +: add flag, -: remove flag, = set equal to\n", ch);
            send_to_char("  otherwise flag toggles the flags listed.\n", ch);
            return;
        }

        if (arg2.isEmpty()) {
            send_to_char("What do you wish to set flags on?\n", ch);
            return;
        }

        if (arg3.isEmpty()) {
            send_to_char("You need to specify a flag to set.\n", ch);
            return;
        }

        if (argument.isEmpty()) {
            send_to_char("Which flags do you wish to change?\n", ch);
            return;
        }

        String arg1Str = arg1.toString();

        if (str_prefix(arg1Str, "mob") && str_prefix(arg1Str, "char")) {
            return;
        }

        String arg2Str = arg2.toString();
        victim = get_char_world(ch, arg2Str);
        if (victim == null) {
            send_to_char("You can't find them.\n", ch);
            return;
        }
        String arg3Str = arg3.toString();
        /* select a flag to set */
        long flag;
        if (!str_prefix(arg3Str, "act")) {
            if (!IS_NPC(victim)) {
                send_to_char("Use plr for PCs.\n", ch);
                return;
            }
            flag = victim.act;
            flag_table = act_flags;
        } else if (!str_prefix(arg3Str, "plr")) {
            if (IS_NPC(victim)) {
                send_to_char("Use act for NPCs.\n", ch);
                return;
            }
            flag = victim.act;
            flag_table = plr_flags;
        } else if (!str_prefix(arg3Str, "aff")) {
            flag = victim.affected_by;
            flag_table = affect_flags;
        } else if (!str_prefix(arg3Str, "immunity")) {
            flag = victim.imm_flags;
            flag_table = imm_flags;
        } else if (!str_prefix(arg3Str, "resist")) {
            flag = victim.res_flags;
            flag_table = imm_flags;
        } else if (!str_prefix(arg3Str, "vuln")) {
            flag = victim.vuln_flags;
            flag_table = imm_flags;
        } else if (!str_prefix(arg3Str, "form")) {
            if (!IS_NPC(victim)) {
                send_to_char("Form can't be set on PCs.\n", ch);
                return;
            }
            flag = victim.form;
            flag_table = form_flags;
        } else if (!str_prefix(arg3Str, "parts")) {
            if (!IS_NPC(victim)) {
                send_to_char("Parts can't be set on PCs.\n", ch);
                return;
            }

            flag = victim.parts;
            flag_table = part_flags;
        } else if (!str_prefix(arg3.toString(), "comm")) {
            if (IS_NPC(victim)) {
                send_to_char("Comm can't be set on NPCs.\n", ch);
                return;
            }

            flag = victim.comm;
            flag_table = comm_flags;
        } else {
            send_to_char("That's not an acceptable flag.\n", ch);
            return;
        }

        long old = flag;
        long newFlag = 0, marked = 0;
        victim.zone = null;

        if (type != '=') {
            newFlag = old;
        }
        /* mark the words */
        for (; ; ) {
            argument = one_argument(argument, word);

            if (word.isEmpty()) {
                break;
            }

            int pos = flag_lookup(word.toString(), flag_table);
            if (pos == 0) {
                send_to_char("That flag doesn't exist!\n", ch);
                return;
            } else {
                marked = SET_BIT(marked, pos);
            }
        }

        for (int pos = 0; flag_table[pos].name != null; pos++) {
            if (!flag_table[pos].settable && IS_SET(old, flag_table[pos].bit)) {
                newFlag = SET_BIT(newFlag, flag_table[pos].bit);
                continue;
            }
            if (IS_SET(marked, flag_table[pos].bit)) {
                switch (type) {
                    case '=', '+' -> newFlag = SET_BIT(newFlag, flag_table[pos].bit);
                    case '-' -> newFlag = REMOVE_BIT(newFlag, flag_table[pos].bit);
                    default -> {
                        if (IS_SET(newFlag, flag_table[pos].bit)) {
                            newFlag = REMOVE_BIT(newFlag, flag_table[pos].bit);
                        } else {
                            newFlag = SET_BIT(newFlag, flag_table[pos].bit);
                        }
                    }
                }
            }
        }
        if (!str_prefix(arg3Str, "act")) {
            victim.act = newFlag;
        } else if (!str_prefix(arg3Str, "plr")) {
            victim.act = newFlag;
        } else if (!str_prefix(arg3Str, "aff")) {
            victim.affected_by = newFlag;
        } else if (!str_prefix(arg3Str, "immunity")) {
            victim.imm_flags = newFlag;
        } else if (!str_prefix(arg3Str, "resist")) {
            victim.res_flags = newFlag;
        } else if (!str_prefix(arg3Str, "vuln")) {
            victim.vuln_flags = newFlag;
        } else if (!str_prefix(arg3Str, "form")) {
            victim.form = (int) newFlag;
        } else if (!str_prefix(arg3Str, "parts")) {
            victim.parts = (int) newFlag;
        } else if (!str_prefix(arg3.toString(), "comm")) {
            victim.comm = (int) newFlag;
        } else {
            assert false;
        }
    }
}
