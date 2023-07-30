package net.sf.nightworks;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.function.Function;

import static net.sf.nightworks.ActWiz.wiznet;
import static net.sf.nightworks.Comm.act;
import static net.sf.nightworks.Comm.page_to_char;
import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.Comm.write_to_buffer;
import static net.sf.nightworks.DB.log_string;
import static net.sf.nightworks.DB.number_bits;
import static net.sf.nightworks.DB.tail_chain;
import static net.sf.nightworks.Handler.affect_strip;
import static net.sf.nightworks.Handler.get_char_room;
import static net.sf.nightworks.Handler.get_trust;
import static net.sf.nightworks.Nightworks.AFF_CHARM;
import static net.sf.nightworks.Nightworks.AFF_EARTHFADE;
import static net.sf.nightworks.Nightworks.AFF_FADE;
import static net.sf.nightworks.Nightworks.AFF_HIDE;
import static net.sf.nightworks.Nightworks.AFF_IMP_INVIS;
import static net.sf.nightworks.Nightworks.AFF_STUN;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.CMD_GHOST;
import static net.sf.nightworks.Nightworks.CMD_KEEP_HIDE;
import static net.sf.nightworks.Nightworks.COMM_NOEMOTE;
import static net.sf.nightworks.Nightworks.DESCRIPTOR_DATA;
import static net.sf.nightworks.Nightworks.IS_AFFECTED;
import static net.sf.nightworks.Nightworks.IS_AWAKE;
import static net.sf.nightworks.Nightworks.IS_NPC;
import static net.sf.nightworks.Nightworks.IS_SET;
import static net.sf.nightworks.Nightworks.LEVEL_HERO;
import static net.sf.nightworks.Nightworks.LEVEL_IMMORTAL;
import static net.sf.nightworks.Nightworks.MAX_INPUT_LENGTH;
import static net.sf.nightworks.Nightworks.MAX_LEVEL;
import static net.sf.nightworks.Nightworks.PLR_FREEZE;
import static net.sf.nightworks.Nightworks.PLR_GHOST;
import static net.sf.nightworks.Nightworks.PLR_LOG;
import static net.sf.nightworks.Nightworks.POS_DEAD;
import static net.sf.nightworks.Nightworks.POS_FIGHTING;
import static net.sf.nightworks.Nightworks.POS_INCAP;
import static net.sf.nightworks.Nightworks.POS_MORTAL;
import static net.sf.nightworks.Nightworks.POS_RESTING;
import static net.sf.nightworks.Nightworks.POS_SITTING;
import static net.sf.nightworks.Nightworks.POS_SLEEPING;
import static net.sf.nightworks.Nightworks.POS_STUNNED;
import static net.sf.nightworks.Nightworks.PULSE_VIOLENCE;
import static net.sf.nightworks.Nightworks.REMOVE_BIT;
import static net.sf.nightworks.Nightworks.TO_CHAR;
import static net.sf.nightworks.Nightworks.TO_NOTVICT;
import static net.sf.nightworks.Nightworks.TO_ROOM;
import static net.sf.nightworks.Nightworks.TO_VICT;
import static net.sf.nightworks.Nightworks.WAIT_STATE;
import static net.sf.nightworks.Nightworks.WIZ_SECURE;
import static net.sf.nightworks.Nightworks.fLogAll;
import static net.sf.nightworks.Nightworks.nw_config;
import static net.sf.nightworks.Nightworks.social_table;
import static net.sf.nightworks.Nightworks.social_type;
import static net.sf.nightworks.Skill.gsn_earthfade;
import static net.sf.nightworks.Skill.gsn_imp_invis;
import static net.sf.nightworks.util.TextUtils.isDigit;
import static net.sf.nightworks.util.TextUtils.one_argument;
import static net.sf.nightworks.util.TextUtils.smash_tilde;
import static net.sf.nightworks.util.TextUtils.str_cmp;
import static net.sf.nightworks.util.TextUtils.str_prefix;
import static net.sf.nightworks.util.TextUtils.trimSpaces;

class Interp {
/* this is a listing of all the commands and command related data */

    /* for command types */
    static final int ML = MAX_LEVEL;/* implementor */
    static final int L1 = MAX_LEVEL - 1;  /* creator */
    static final int L2 = MAX_LEVEL - 2;  /* supreme being */
    static final int L3 = MAX_LEVEL - 3;  /* deity */
    static final int L4 = MAX_LEVEL - 4;  /* god */
    static final int L5 = MAX_LEVEL - 5;  /* immortal */
    static final int L6 = MAX_LEVEL - 6;  /* demigod */
    static final int L7 = MAX_LEVEL - 7;  /* angel */
    static final int L8 = MAX_LEVEL - 8;  /* avatar */
    static final int IM = LEVEL_IMMORTAL; /* angel */
    static final int HE = LEVEL_HERO; /* hero */

    /**
     * Command logging types.
     */
    static final int LOG_NORMAL = 0;
    static final int LOG_ALWAYS = 1;
    static final int LOG_NEVER = 2;

    /**
     * Command table.
     */
    private static final CmdType[] cmd_type_table = CmdType.values();

    static CmdType[] getCommandsTable() {
        return cmd_type_table;
    }

    /**
     * The main entry point for executing commands.
     * Can be recursively called from 'at', 'order', 'force'.
     */

    static void interpret(CHAR_DATA ch, String argument, boolean is_order) {
        /*
        * Strip leading spaces.
        */
        argument = smash_tilde(argument);
        argument = trimSpaces(argument, 0);
        if (argument.isEmpty()) {
            return;
        }

        /*
        * Implement freeze command.
        */
        if (!IS_NPC(ch) && IS_SET(ch.act, PLR_FREEZE)) {
            send_to_char("You're totally frozen!\n", ch);
            return;
        }

        /*
        * Grab the command word.
        * Special parsing so ' can be a command,
        * also no spaces needed after punctuation.
        */
        var logLine = argument;
        var command = new StringBuilder();
        var pos = 0;
        var c0 = argument.charAt(0);
        if (!Character.isLetter(c0) && !isDigit(c0)) {
            command.append(c0);
            pos++;
            argument = trimSpaces(argument, pos);
        } else {
            argument = one_argument(argument, command);
        }

        /*
        * Look for command in command table.
        */
        var trust = get_trust(ch);

        var commandsTable = getCommandsTable();
        CmdType cmd = null;
        var commandStr = command.toString();
        for (var c : commandsTable) {
            if (!matches(commandStr, c.names) || c.level > trust) {
                continue;
            }
            if (!is_order && IS_AFFECTED(ch, AFF_CHARM)) {
                send_to_char("First ask to your beloved master!\n", ch);
                return;
            }

            if (IS_AFFECTED(ch, AFF_STUN) && (c.extra & CMD_KEEP_HIDE) == 0) {
                send_to_char("You are STUNNED to do that.\n", ch);
                return;
            }
                /* Come out of hiding for most commands */
            if (IS_AFFECTED(ch, AFF_HIDE) && !IS_NPC(ch) && (c.extra & CMD_KEEP_HIDE) == 0) {
                ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_HIDE);
                send_to_char("You step out of the shadows.\n", ch);
                act("$n steps out of the shadows.", ch, null, null, TO_ROOM);
            }

            if (IS_AFFECTED(ch, AFF_FADE) && !IS_NPC(ch) && (c.extra & CMD_KEEP_HIDE) == 0) {
                ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_FADE);
                send_to_char("You step out of the shadows.\n", ch);
                act("$n steps out of the shadows.", ch, null, null, TO_ROOM);
            }

            if (IS_AFFECTED(ch, AFF_IMP_INVIS) && !IS_NPC(ch) && (c.position == POS_FIGHTING)) {
                affect_strip(ch, gsn_imp_invis);
                ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_IMP_INVIS);
                send_to_char("You fade into existence.\n", ch);
                act("$n fades into existence.", ch, null, null, TO_ROOM);
            }

            if (IS_AFFECTED(ch, AFF_EARTHFADE) && !IS_NPC(ch) && (c.position == POS_FIGHTING)) {
                fadeOutToNormal(ch);
            }

                /* prevent ghosts from doing a bunch of commands */
            if (IS_SET(ch.act, PLR_GHOST) && !IS_NPC(ch) && (c.extra & CMD_GHOST) == 0) {
                continue;
            }

            cmd = c;
            break;
        }

        /*
        * Log and snoop.
        */
        if (cmd == null || cmd.log == LOG_NEVER) {
            logLine = "";
        }

        if (((!IS_NPC(ch) && IS_SET(ch.act, PLR_LOG)) || fLogAll
                || (cmd != null && cmd.log == LOG_ALWAYS) && !logLine.isEmpty() && logLine.charAt(0) != '\n')) {
            var log_buf = "Log " + ch.name + ": " + logLine;
            wiznet(log_buf, ch, null, WIZ_SECURE, 0, get_trust(ch));
            log_string(log_buf);
        }

        if (ch.desc != null && ch.desc.snoop_by != null) {
            write_to_buffer(ch.desc.snoop_by, "# ");
            write_to_buffer(ch.desc.snoop_by, logLine);
            write_to_buffer(ch.desc.snoop_by, "\n");
        }

        social_type soc = null;
        int minPos, cmdFlags;

        if (cmd == null) {
            //Look for command in socials table.
            soc = lookup_social(command.toString());
            if (soc == null) {
                send_to_char("Huh?\n", ch);
                return;
            }
            if (!IS_NPC(ch) && IS_SET(ch.comm, COMM_NOEMOTE)) {
                send_to_char("You are anti-social!\n", ch);
                return;
            }

            if (IS_AFFECTED(ch, AFF_EARTHFADE) && !IS_NPC(ch) && soc.minPos == POS_FIGHTING) {
                fadeOutToNormal(ch);
            }
            minPos = soc.minPos;
            cmdFlags = 0;
        } else {
            minPos = cmd.position;
            cmdFlags = cmd.extra;
        }

        if (!IS_NPC(ch)) {
            /* Come out of hiding for most commands */
            if (IS_AFFECTED(ch, AFF_HIDE | AFF_FADE) && !IS_SET(cmdFlags, CMD_KEEP_HIDE)) {
                ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_HIDE | AFF_FADE);
                send_to_char("You step out of shadows.\n", ch);
                act("$n steps out of shadows.", ch, null, null, TO_ROOM);
            }

            if (IS_AFFECTED(ch, AFF_IMP_INVIS) && !IS_NPC(ch) && minPos == POS_FIGHTING) {
                affect_strip(ch, gsn_imp_invis);
                ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_IMP_INVIS);
                send_to_char("You fade into existence.\n", ch);
                act("$n fades into existence.", ch, null, null, TO_ROOM);
            }
        }

        /*
        * Character not in position for command?
        */
        if (ch.position < minPos) {
            switch (ch.position) {
                case POS_DEAD -> send_to_char("Lie still; you are DEAD.\n", ch);
                case POS_MORTAL, POS_INCAP -> send_to_char("You are hurt far too bad for that.\n", ch);
                case POS_STUNNED -> send_to_char("You are too stunned to do that.\n", ch);
                case POS_SLEEPING -> send_to_char("In your dreams, or what?\n", ch);
                case POS_RESTING -> send_to_char("Nah... You feel too relaxed...\n", ch);
                case POS_SITTING -> send_to_char("Better stand up first.\n", ch);
                case POS_FIGHTING -> send_to_char("No way!  You are still fighting!\n", ch);
            }
            return;
        }
        if (soc != null) {
            interpret_social(ch, argument, soc);
        } else {
            // Dispatch the command
            cmd.do_fun.accept(ch, argument);
        }
        tail_chain();
    }

    private static void fadeOutToNormal(CHAR_DATA ch) {
        affect_strip(ch, gsn_earthfade);
        ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_EARTHFADE);
        WAIT_STATE(ch, (PULSE_VIOLENCE / 2));
        send_to_char("You slowly fade to your neutral form.\n", ch);
        act("Earth forms $n in front of you.", ch, null, null, TO_ROOM);
    }

    private static boolean matches(String command, String[] names) {
        for (var name : names) {
            if (!str_prefix(command, name)) {
                return true;
            }
        }
        return false;
    }

    static social_type lookup_social(String name) {
        for (var soc : social_table) {
            if (!str_prefix(name, soc.name)) {
                return soc;
            }
        }
        return null;
    }

    static boolean interpret_social(CHAR_DATA ch, String argument, social_type soc) {
        CHAR_DATA victim;
        var arg = new StringBuilder();
        one_argument(argument, arg);
        if (arg.isEmpty()) {
            act(soc.noarg_room, ch, null, null, TO_ROOM);
            act(soc.noarg_char, ch, null, null, TO_CHAR);
        } else if ((victim = get_char_room(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
        } else if (victim == ch) {
            act(soc.self_room, ch, null, victim, TO_ROOM);
            act(soc.self_char, ch, null, victim, TO_CHAR);
        } else {
            act(soc.found_novictim, ch, null, victim, TO_NOTVICT);
            act(soc.found_char, ch, null, victim, TO_CHAR);
            act(soc.found_novictim, ch, null, victim, TO_VICT);

            if (!IS_NPC(ch) && IS_NPC(victim) && !IS_AFFECTED(victim, AFF_CHARM) && IS_AWAKE(victim) && victim.desc == null) {
                switch (number_bits(4)) {
                    case 0, 1, 2, 3, 4, 5, 6, 7, 8 -> {
                        act(soc.found_novictim, victim, null, ch, TO_NOTVICT);
                        act(soc.found_char, victim, null, ch, TO_CHAR);
                        act(soc.found_victim, victim, null, ch, TO_VICT);
                    }
                    case 9, 10, 11, 12 -> {
                        act("$n slaps $N.", victim, null, ch, TO_NOTVICT);
                        act("You slap $N.", victim, null, ch, TO_CHAR);
                        act("$n slaps you.", victim, null, ch, TO_VICT);
                    }
                }
            }
        }
        return true;
    }

    /**
     * Given a string like 14.foo, return 14 and 'foo'
     */
    static int number_argument(String argument, StringBuilder arg) {
        return int_argument(argument, arg, '.');
    }

    /**
     * Given a string like 14*foo, return 14 and 'foo'
     */
    static int multiply_argument(String argument, StringBuilder arg) {
        return int_argument(argument, arg, '*');
    }

    static int int_argument(String argument, StringBuilder arg, char c) {
        var dot = argument.indexOf(c);
        var number = 1;
        if (dot > 0) {
            try {
                number = Integer.parseInt(argument.substring(0, dot));
            } catch (NumberFormatException e) {
                number = 0;
            }
        }
        arg.append(argument, dot + 1, argument.length());
        return number;
    }


    static void do_commands(CHAR_DATA ch) {
        showAvailableCommands(ch, (cmd) -> cmd.level < LEVEL_HERO);
    }

    private static void showAvailableCommands(CHAR_DATA ch, Function<CmdType, Boolean> cmdCheckFn) {
        var cmd_table = getCommandsTable();
        List<String> names = new ArrayList<>();
        for (var cmd : cmd_table) {
            if (cmdCheckFn.apply(cmd) && cmd.level <= get_trust(ch)) {
                names.add(cmd.names[0]);
            }
        }
        names.sort(String::compareTo);
        var buf = new StringBuilder();
        var f = new Formatter(buf);
        var col = 0;
        for (var name : names) {
            f.format("%-12s", name);
            if (++col % 6 == 0) {
                buf.append("\n");
            }
        }

        if (col % 6 != 0) {
            buf.append("\n");
        }
        page_to_char(buf.toString(), ch);
    }

    static void do_wizhelp(CHAR_DATA ch) {
        showAvailableCommands(ch, (cmd) -> cmd.level >= LEVEL_HERO);
    }


    /**
     * Does aliasing and other fun stuff
     */
    static void substitute_alias(DESCRIPTOR_DATA d, String argument) {
        int alias;
        var ch = d.original != null ? d.original : d.character;

        /* check for prefix */
        if (!ch.prefix.isEmpty() && str_prefix("prefix", argument)) {
            if (ch.prefix.length() + argument.length() > MAX_INPUT_LENGTH) {
                send_to_char("Line to long, prefix not processed.\n", ch);
            } else {
                argument = ch.prefix + " " + argument;
            }
        }

        if (IS_NPC(ch) || ch.pcdata.alias[0] == null
                || !str_prefix("alias", argument) || !str_prefix("una", argument)
                || !str_prefix("prefix", argument)) {
            interpret(d.character, argument, false);
            return;
        }

        var buf = new StringBuilder(argument);

        /* go through the aliases */
        for (alias = 0; alias < nw_config.max_alias; alias++) {
            if (ch.pcdata.alias[alias] == null) {
                break;
            }

            if (!str_prefix(ch.pcdata.alias[alias], argument)) {
                var name = new StringBuilder();
                var point = one_argument(argument, name);
                if (ch.pcdata.alias[alias].matches(name.toString())) {
                    buf.setLength(0);
                    buf.append(ch.pcdata.alias_sub[alias]);
                    buf.append(" ");
                    buf.append(point);
                    break;
                }
                if (buf.length() > MAX_INPUT_LENGTH) {
                    send_to_char("Alias substitution too long. Truncated.\n", ch);
                    buf.delete(MAX_INPUT_LENGTH, buf.length());
                }
            }
        }
        interpret(d.character, buf.toString(), false);
    }

    static void do_alia(CHAR_DATA ch) {
        send_to_char("I'm sorry, alias must be entered in full.\n", ch);
    }

    static void do_alias(CHAR_DATA ch, String argument) {
        CHAR_DATA rch;
        int pos;

        argument = smash_tilde(argument);

        if (ch.desc == null) {
            rch = ch;
        } else {
            rch = ch.desc.original != null ? ch.desc.original : ch;
        }

        if (IS_NPC(rch)) {
            return;
        }

        var argb = new StringBuilder();
        argument = one_argument(argument, argb);


        if (argb.isEmpty()) {

            if (rch.pcdata.alias[0] == null) {
                send_to_char("You have no aliases defined.\n", ch);
                return;
            }
            send_to_char("Your current aliases are:\n", ch);

            for (pos = 0; pos < nw_config.max_alias; pos++) {
                if (rch.pcdata.alias[pos] == null
                        || rch.pcdata.alias_sub[pos] == null) {
                    break;
                }

                var buf = "    " + rch.pcdata.alias[pos] + ":  " + rch.pcdata.alias_sub[pos] + "\n";
                send_to_char(buf, ch);
            }
            return;
        }
        var arg = argb.toString();

        if (!str_prefix("una", arg) || !str_cmp("alias", arg)) {
            send_to_char("Sorry, that word is reserved.\n", ch);
            return;
        }

        if (argument.isEmpty()) {
            for (pos = 0; pos < nw_config.max_alias; pos++) {
                if (rch.pcdata.alias[pos] == null
                        || rch.pcdata.alias_sub[pos] == null) {
                    break;
                }

                if (!str_cmp(arg, rch.pcdata.alias[pos])) {
                    var buf = rch.pcdata.alias[pos] + " aliases to '" + rch.pcdata.alias_sub[pos] + "'.\n";
                    send_to_char(buf, ch);
                    return;
                }
            }

            send_to_char("That alias is not defined.\n", ch);
            return;
        }

        if (!str_prefix(argument, "delete") || !str_prefix(argument, "prefix")) {
            send_to_char("That shall not be done!\n", ch);
            return;
        }

        for (pos = 0; pos < nw_config.max_alias; pos++) {
            if (rch.pcdata.alias[pos] == null) {
                break;
            }

            if (!str_cmp(arg, rch.pcdata.alias[pos])) /* redefine an alias */ {
                rch.pcdata.alias_sub[pos] = argument;
                send_to_char(arg + " is now aliased to '" + argument + "'.\n", ch);
                return;
            }
        }

        if (pos >= nw_config.max_alias) {
            send_to_char("Sorry, you have reached the alias limit.\n", ch);
            return;
        }

        /* make a new alias */
        rch.pcdata.alias[pos] = arg;
        rch.pcdata.alias_sub[pos] = argument;
        send_to_char(arg + " is now aliased to '" + argument + "'.\n", ch);
    }


    static void do_unalias(CHAR_DATA ch, String argument) {

        int pos;
        var found = false;
        CHAR_DATA rch;
        if (ch.desc == null) {
            rch = ch;
        } else {
            rch = ch.desc.original != null ? ch.desc.original : ch;
        }

        if (IS_NPC(rch)) {
            return;
        }

        var arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.isEmpty()) {
            send_to_char("Unalias what?\n", ch);
            return;
        }

        for (pos = 0; pos < nw_config.max_alias; pos++) {
            if (rch.pcdata.alias[pos] == null) {
                break;
            }

            if (found) {
                rch.pcdata.alias[pos - 1] = rch.pcdata.alias[pos];
                rch.pcdata.alias_sub[pos - 1] = rch.pcdata.alias_sub[pos];
                rch.pcdata.alias[pos] = null;
                rch.pcdata.alias_sub[pos] = null;
                continue;
            }

            if (arg.toString().equals(rch.pcdata.alias[pos])) {
                send_to_char("Alias removed.\n", ch);
                rch.pcdata.alias[pos] = null;
                rch.pcdata.alias_sub[pos] = null;
                found = true;
            }
        }

        if (!found) {
            send_to_char("No alias of that name to remove.\n", ch);
        }
    }
}
