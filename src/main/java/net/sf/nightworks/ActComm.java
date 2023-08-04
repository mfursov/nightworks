package net.sf.nightworks;

import java.io.File;
import net.sf.nightworks.util.NotNull;
import net.sf.nightworks.util.TextBuffer;
import net.sf.nightworks.util.TextUtils;
import static net.sf.nightworks.ActWiz.wiznet;
import static net.sf.nightworks.Comm.act;
import static net.sf.nightworks.Comm.close_socket;
import static net.sf.nightworks.Comm.page_to_char;
import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.Comm.write_to_buffer;
import static net.sf.nightworks.Const.language_table;
import static net.sf.nightworks.Const.translation_table;
import static net.sf.nightworks.DB.append_file;
import static net.sf.nightworks.DB.bug;
import static net.sf.nightworks.DB.log_string;
import static net.sf.nightworks.DB.number_range;
import static net.sf.nightworks.Handler.affect_strip;
import static net.sf.nightworks.Handler.back_home;
import static net.sf.nightworks.Handler.can_see;
import static net.sf.nightworks.Handler.extract_char;
import static net.sf.nightworks.Handler.extract_char_nocount;
import static net.sf.nightworks.Handler.extract_obj;
import static net.sf.nightworks.Handler.get_char_room;
import static net.sf.nightworks.Handler.get_char_world;
import static net.sf.nightworks.Handler.get_total_played;
import static net.sf.nightworks.Handler.get_trust;
import static net.sf.nightworks.Handler.is_affected;
import static net.sf.nightworks.Handler.obj_from_char;
import static net.sf.nightworks.Handler.obj_to_room;
import static net.sf.nightworks.Handler.skill_failure_check;
import static net.sf.nightworks.Interp.getCommandsTable;
import static net.sf.nightworks.Interp.interpret;
import static net.sf.nightworks.Lookup.lang_lookup;
import static net.sf.nightworks.Nightworks.ACT_THIEF;
import static net.sf.nightworks.Nightworks.AFF_CHARM;
import static net.sf.nightworks.Nightworks.AFF_ROOM_ESPIRIT;
import static net.sf.nightworks.Nightworks.AFF_SLEEP;
import static net.sf.nightworks.Nightworks.CABAL_BATTLE;
import static net.sf.nightworks.Nightworks.CABAL_CHAOS;
import static net.sf.nightworks.Nightworks.CABAL_HUNTER;
import static net.sf.nightworks.Nightworks.CABAL_INVADER;
import static net.sf.nightworks.Nightworks.CABAL_KNIGHT;
import static net.sf.nightworks.Nightworks.CABAL_LIONS;
import static net.sf.nightworks.Nightworks.CABAL_RULER;
import static net.sf.nightworks.Nightworks.CABAL_SHALAFI;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.COMM_DEAF;
import static net.sf.nightworks.Nightworks.COMM_NOAUCTION;
import static net.sf.nightworks.Nightworks.COMM_NOCHANNELS;
import static net.sf.nightworks.Nightworks.COMM_NOEMOTE;
import static net.sf.nightworks.Nightworks.COMM_NOTELL;
import static net.sf.nightworks.Nightworks.COMM_NOWIZ;
import static net.sf.nightworks.Nightworks.COMM_QUIET;
import static net.sf.nightworks.Nightworks.COMM_SNOOP_PROOF;
import static net.sf.nightworks.Nightworks.CON_PLAYING;
import static net.sf.nightworks.Nightworks.CON_REMORTING;
import static net.sf.nightworks.Nightworks.DESCRIPTOR_DATA;
import static net.sf.nightworks.Nightworks.FIGHT_DELAY_TIME;
import static net.sf.nightworks.Nightworks.IS_AFFECTED;
import static net.sf.nightworks.Nightworks.IS_AWAKE;
import static net.sf.nightworks.Nightworks.IS_EVIL;
import static net.sf.nightworks.Nightworks.IS_GOOD;
import static net.sf.nightworks.Nightworks.IS_IMMORTAL;
import static net.sf.nightworks.Nightworks.IS_NPC;
import static net.sf.nightworks.Nightworks.IS_RAFFECTED;
import static net.sf.nightworks.Nightworks.IS_SET;
import static net.sf.nightworks.Nightworks.LANG_COMMON;
import static net.sf.nightworks.Nightworks.LEVEL_HERO;
import static net.sf.nightworks.Nightworks.LEVEL_IMMORTAL;
import static net.sf.nightworks.Nightworks.MAX_LANGUAGE;
import static net.sf.nightworks.Nightworks.MPROG_SPEECH;
import static net.sf.nightworks.Nightworks.OBJ_DATA;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_MAGIC_JAR;
import static net.sf.nightworks.Nightworks.OPROG_SPEECH;
import static net.sf.nightworks.Nightworks.ORG_RACE;
import static net.sf.nightworks.Nightworks.PAGELEN;
import static net.sf.nightworks.Nightworks.PERS;
import static net.sf.nightworks.Nightworks.PLR_CANREMORT;
import static net.sf.nightworks.Nightworks.PLR_NOFOLLOW;
import static net.sf.nightworks.Nightworks.PLR_NO_EXP;
import static net.sf.nightworks.Nightworks.PLR_REMORTED;
import static net.sf.nightworks.Nightworks.POS_DEAD;
import static net.sf.nightworks.Nightworks.POS_FIGHTING;
import static net.sf.nightworks.Nightworks.POS_RESTING;
import static net.sf.nightworks.Nightworks.POS_SLEEPING;
import static net.sf.nightworks.Nightworks.POS_STUNNED;
import static net.sf.nightworks.Nightworks.PULSE_VIOLENCE;
import static net.sf.nightworks.Nightworks.REMOVE_BIT;
import static net.sf.nightworks.Nightworks.SET_BIT;
import static net.sf.nightworks.Nightworks.TO_CHAR;
import static net.sf.nightworks.Nightworks.TO_NOTVICT;
import static net.sf.nightworks.Nightworks.TO_ROOM;
import static net.sf.nightworks.Nightworks.TO_VICT;
import static net.sf.nightworks.Nightworks.UMIN;
import static net.sf.nightworks.Nightworks.WAIT_STATE;
import static net.sf.nightworks.Nightworks.WIZ_LOGINS;
import static net.sf.nightworks.Nightworks.atoi;
import static net.sf.nightworks.Nightworks.auction;
import static net.sf.nightworks.Nightworks.char_list;
import static net.sf.nightworks.Nightworks.current_time;
import static net.sf.nightworks.Nightworks.descriptor_list;
import static net.sf.nightworks.Nightworks.iNumPlayers;
import static net.sf.nightworks.Nightworks.nw_config;
import static net.sf.nightworks.Nightworks.object_list;
import static net.sf.nightworks.Save.load_char_obj;
import static net.sf.nightworks.Save.save_char_obj;
import static net.sf.nightworks.Skill.gsn_charm_person;
import static net.sf.nightworks.Skill.gsn_deafen;
import static net.sf.nightworks.Skill.gsn_doppelganger;
import static net.sf.nightworks.Skill.gsn_evil_spirit;
import static net.sf.nightworks.Skill.gsn_garble;
import static net.sf.nightworks.Skill.gsn_judge;
import static net.sf.nightworks.Tables.cabal_table;
import static net.sf.nightworks.Tables.ethos_table;
import static net.sf.nightworks.util.TextUtils.one_argument;
import static net.sf.nightworks.util.TextUtils.str_cmp;
import static net.sf.nightworks.util.TextUtils.str_prefix;

@SuppressWarnings("unused")
class ActComm {
    /* RT code to delete yourself */

    static void do_delet(@NotNull CHAR_DATA ch) {
        send_to_char("You must type the full command to delete yourself.\n", ch);
    }

    static void do_delete(@NotNull CHAR_DATA ch, @NotNull String argument) {
        if (IS_NPC(ch)) {
            return;
        }
        if (ch.pcdata.confirm_delete) {
            if (!argument.isEmpty()) {
                send_to_char("Delete status removed.\n", ch);
                ch.pcdata.confirm_delete = false;
            } else {
                wiznet("$N turns $Mself into line noise.", ch, null, 0, 0, 0);
                ch.last_fight_time = -1;
                do_quit_count(ch);
                //noinspection ResultOfMethodCallIgnored
                new File(nw_config.lib_player_dir + "/" + TextUtils.capitalize(ch.name)).delete();
            }
            return;
        }

        if (!argument.isEmpty()) {
            send_to_char("Just type delete. No argument.\n", ch);
            return;
        }

        send_to_char("Type delete again to confirm this command.\n", ch);
        send_to_char("WARNING: this command is irreversible.\n", ch);
        send_to_char("Typing delete with an argument will undo delete status.\n", ch);
        ch.pcdata.confirm_delete = true;
        wiznet("$N is contemplating deletion.", ch, null, 0, 0, get_trust(ch));
    }

    /* RT code to display channel status */

    static void do_channels(@NotNull CHAR_DATA ch) {

        /* lists all channels and their status */
        send_to_char("   channel     status\n", ch);
        send_to_char("---------------------\n", ch);

        send_to_char("auction        ", ch);
        if (!IS_SET(ch.comm, COMM_NOAUCTION)) {
            send_to_char("ON\n", ch);
        } else {
            send_to_char("OFF\n", ch);
        }

        if (IS_IMMORTAL(ch)) {
            send_to_char("god channel    ", ch);
            if (!IS_SET(ch.comm, COMM_NOWIZ)) {
                send_to_char("ON\n", ch);
            } else {
                send_to_char("OFF\n", ch);
            }
        }

        send_to_char("tells          ", ch);
        if (!IS_SET(ch.comm, COMM_DEAF)) {
            send_to_char("ON\n", ch);
        } else {
            send_to_char("OFF\n", ch);
        }

        send_to_char("quiet mode     ", ch);
        if (IS_SET(ch.comm, COMM_QUIET)) {
            send_to_char("ON\n", ch);
        } else {
            send_to_char("OFF\n", ch);
        }

        if (IS_SET(ch.comm, COMM_SNOOP_PROOF)) {
            send_to_char("You are immune to snooping.\n", ch);
        }

        if (ch.lines != PAGELEN) {
            if (ch.lines != 0) {
                var buf = new TextBuffer();
                buf.sprintf("You display %d lines of scroll.\n", ch.lines + 2);
                send_to_char(buf, ch);
            } else {
                send_to_char("Scroll buffering is off.\n", ch);
            }
        }


        if (IS_SET(ch.comm, COMM_NOTELL)) {
            send_to_char("You cannot use tell.\n", ch);
        }

        if (IS_SET(ch.comm, COMM_NOCHANNELS)) {
            send_to_char("You cannot use channels.\n", ch);
        }

        if (IS_SET(ch.comm, COMM_NOEMOTE)) {
            send_to_char("You cannot show emotions.\n", ch);
        }

    }

    static void garble(@NotNull StringBuilder garbled, @NotNull String speech) {
        for (var i = 0; i < speech.length(); i++) {
            var c = speech.charAt(i);
            if (c >= 'a' && c <= 'z') {
                garbled.append('a' + number_range(0, 25));
            } else if (c >= 'A' && c <= 'Z') {
                garbled.append('A' + number_range(0, 25));
            } else {
                garbled.append(c);
            }
        }
    }

    /* RT deaf blocks out all shouts */

    static void do_deaf(@NotNull CHAR_DATA ch) {

        if (IS_SET(ch.comm, COMM_DEAF)) {
            send_to_char("You can now hear tells again.\n", ch);
            ch.comm = REMOVE_BIT(ch.comm, COMM_DEAF);
        } else {
            send_to_char("From now on, you won't hear tells.\n", ch);
            ch.comm = SET_BIT(ch.comm, COMM_DEAF);
        }
    }

    /* RT quiet blocks out all communication */

    static void do_quiet(@NotNull CHAR_DATA ch) {
        if (IS_SET(ch.comm, COMM_QUIET)) {
            send_to_char("Quiet mode removed.\n", ch);
            ch.comm = REMOVE_BIT(ch.comm, COMM_QUIET);
        } else {
            send_to_char("From now on, you will only hear says and emotes.\n", ch);
            ch.comm = SET_BIT(ch.comm, COMM_QUIET);
        }
    }

    static void do_replay(@NotNull CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            send_to_char("You can't replay.\n", ch);
            return;
        }

        page_to_char(ch.pcdata.buffer, ch);
        ch.pcdata.buffer.setLength(0);
    }

    static void do_immtalk(@NotNull CHAR_DATA ch, @NotNull String argument) {
        DESCRIPTOR_DATA d;

        if (argument.isEmpty()) {
            if (IS_SET(ch.comm, COMM_NOWIZ)) {
                send_to_char("Immortal channel is now ON\n", ch);
                ch.comm = REMOVE_BIT(ch.comm, COMM_NOWIZ);
            } else {
                send_to_char("Immortal channel is now OFF\n", ch);
                ch.comm = SET_BIT(ch.comm, COMM_NOWIZ);
            }
            return;
        }

        ch.comm = REMOVE_BIT(ch.comm, COMM_NOWIZ);

        if (!is_affected(ch, gsn_deafen)) {
            act("$n: {C$t{x", ch, argument, null, TO_CHAR, POS_DEAD);
        }
        for (d = descriptor_list; d != null; d = d.next) {
            if (d.connected == CON_PLAYING &&
                    IS_IMMORTAL(d.character) &&
                    !IS_SET(d.character.comm, COMM_NOWIZ)) {
                act("$n: {C$t{x", ch, argument, d.character, TO_VICT, POS_DEAD);
            }
        }

    }


    static void do_say(@NotNull CHAR_DATA ch, @NotNull String argument) {
        CHAR_DATA room_char;
        OBJ_DATA char_obj;
        CHAR_DATA vch;


        if (argument.isEmpty()) {
            send_to_char("Say what?\n", ch);
            return;
        }
        if (ch.in_room == null) {
            send_to_char("But, you are not in a room!\n", ch);
            return;
        }


        String buf;

        if (is_affected(ch, gsn_garble)) {
            var buff = new StringBuilder();
            garble(buff, argument);
            buf = buff.toString();
        } else {
            buf = argument;
        }

        for (vch = ch.in_room.people; vch != null; vch = vch.next_in_room) {
            if (!is_affected(vch, gsn_deafen)) {
                var trans = translate(ch, vch, buf);
                act("{g$n says '$t'{x", ch, trans, vch, TO_VICT, POS_RESTING);
            }
        }

        if (!is_affected(ch, gsn_deafen)) {
            act("{gYou say '$T'{x", ch, null, buf, TO_CHAR, POS_RESTING);
        }


        for (room_char = ch.in_room.people; room_char != null;
             room_char = room_char.next_in_room) {
            if (IS_SET(room_char.progtypes, MPROG_SPEECH) && room_char != ch) {
                room_char.pIndexData.mprogs.speech_prog.run(room_char, ch, buf);
            }
        }

        for (char_obj = ch.carrying; char_obj != null;
             char_obj = char_obj.next_content) {
            if (IS_SET(char_obj.progtypes, OPROG_SPEECH)) {
                char_obj.pIndexData.oprogs.speech_prog.run(char_obj, ch, buf);
            }
        }

        for (char_obj = ch.in_room.contents; char_obj != null;
             char_obj = char_obj.next_content) {
            if (IS_SET(char_obj.progtypes, OPROG_SPEECH)) {
                char_obj.pIndexData.oprogs.speech_prog.run(char_obj, ch, buf);
            }
        }
    }


    static void do_shout(@NotNull CHAR_DATA ch, @NotNull String argument) {
        DESCRIPTOR_DATA d;

        if (argument.isEmpty()) {
            send_to_char("Shout what?.\n", ch);
            return;
        }

        WAIT_STATE(ch, 12);

        String buf;
        if (is_affected(ch, gsn_garble)) {
            var buff = new StringBuilder();
            garble(buff, argument);
            buf = buff.toString();
        } else {
            buf = argument;
        }

        if (!is_affected(ch, gsn_deafen)) {
            act("You shout '{G$T{x'", ch, null, buf, TO_CHAR, POS_DEAD);
        }

        for (d = descriptor_list; d != null; d = d.next) {


            if (d.connected == CON_PLAYING &&
                    d.character != ch &&
                    d.character.in_room.area == ch.in_room.area &&
                    !is_affected(d.character, gsn_deafen)) {
                var trans = translate(ch, d.character, buf);
                act("$n shouts '{G$t{x'", ch, trans, d.character, TO_VICT, POS_DEAD);
            }
        }
    }


    static void do_tell(@NotNull CHAR_DATA ch, @NotNull String argument) {

        CHAR_DATA victim;

        if (IS_SET(ch.comm, COMM_NOTELL)) {
            send_to_char("Your message didn't get through.\n", ch);
            return;
        }

        if (IS_SET(ch.comm, COMM_QUIET)) {
            send_to_char("You must turn off quiet mode first.\n", ch);
            return;
        }

        if (IS_SET(ch.comm, COMM_DEAF)) {
            send_to_char("You must turn off deaf mode first.\n", ch);
            return;
        }

        var arg = new StringBuilder();
        argument = one_argument(argument, arg);

        if (arg.isEmpty() || argument.isEmpty()) {
            send_to_char("Tell whom what?\n", ch);
            return;
        }

        /*
         * Can tell to PC's anywhere, but NPC's only in the same room.
         * -- Furey
         */
        if ((victim = get_char_world(ch, arg.toString())) == null
                || (IS_NPC(victim) && victim.in_room != ch.in_room)) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (victim.desc == null && !IS_NPC(victim)) {
            act("$N seems to have misplaced $S link...try again later.", ch, null, victim, TO_CHAR);
            var buf = new TextBuffer();
            buf.sprintf("%s tells you '%s'\n", DB.capitalize(PERS(ch, victim)), argument);
            victim.pcdata.buffer.append(buf.getBuffer());
            return;
        }

        if (!(IS_IMMORTAL(ch) && ch.level > LEVEL_IMMORTAL) && !IS_AWAKE(victim)) {
            act("$E can't hear you.", ch, 0, victim, TO_CHAR);
            return;
        }

        if ((IS_SET(victim.comm, COMM_QUIET) || IS_SET(victim.comm, COMM_DEAF))
                && !IS_IMMORTAL(ch)) {
            act("$E is not receiving tells.", ch, 0, victim, TO_CHAR);
            return;
        }

        String buf;
        if (is_affected(ch, gsn_garble)) {
            var buff = new StringBuilder();
            garble(buff, argument);
            buf = buff.toString();
        } else {
            buf = argument;
        }

        if (!is_affected(ch, gsn_deafen)) {
            act("{rYou tell $N '$t'{x", ch, buf, victim, TO_CHAR, POS_SLEEPING);
        }
        act("{r$n tells you '$t'{x", ch, buf, victim, TO_VICT, POS_SLEEPING);

        victim.reply = ch;

    }


    static void do_reply(@NotNull CHAR_DATA ch, @NotNull String argument) {
        CHAR_DATA victim;

        if (IS_SET(ch.comm, COMM_NOTELL)) {
            send_to_char("Your message didn't get through.\n", ch);
            return;
        }

        if ((victim = ch.reply) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }


        if (victim.desc == null && !IS_NPC(victim)) {
            String buf;
            if (is_affected(ch, gsn_garble)) {
                var buff = new StringBuilder();
                garble(buff, argument);
                buf = buff.toString();
            } else {
                buf = argument;
            }
            act("$N seems to have misplaced $S link...try again later.", ch, null, victim, TO_CHAR);
            var textBuffer = new TextBuffer();
            textBuffer.sprintf("%s tells you '%s'\n", DB.capitalize(PERS(ch, victim)), buf);
            victim.pcdata.buffer.append(textBuffer.getBuffer());
            return;
        }

        if (!IS_IMMORTAL(ch) && !IS_AWAKE(victim)) {
            act("$E can't hear you.", ch, 0, victim, TO_CHAR);
            return;
        }

        if ((IS_SET(victim.comm, COMM_QUIET) || IS_SET(victim.comm, COMM_DEAF))
                && !IS_IMMORTAL(ch) && !IS_IMMORTAL(victim)) {
            act("$E is not receiving tells.", ch, 0, victim, TO_CHAR, POS_DEAD);
            return;
        }

        if (!IS_IMMORTAL(victim) && !IS_AWAKE(ch)) {
            send_to_char("In your dreams, or what?\n", ch);
            return;
        }

        if (!is_affected(ch, gsn_deafen)) {
            act("{RYou tell $N '$t'{x", ch, argument, victim, TO_CHAR, POS_SLEEPING);
        }
        act("{R$n tells you '$t'{x", ch, argument, victim, TO_VICT, POS_SLEEPING);

        victim.reply = ch;

    }

    static void do_yell(@NotNull CHAR_DATA ch, @NotNull String arg) {
        do_yell(ch, (CharSequence) arg);
    }

    static void do_yell(@NotNull CHAR_DATA ch, @NotNull CharSequence arg) {
        DESCRIPTOR_DATA d;


        if (arg.isEmpty()) {
            send_to_char("Yell what?\n", ch);
            return;
        }
        var argument = arg.toString();
        String buf;
        if (is_affected(ch, gsn_garble)) {
            var buff = new StringBuilder();
            garble(buff, argument);
            buf = buff.toString();
        } else {
            buf = argument;
        }

        if (!is_affected(ch, gsn_deafen)) {
            act("You yell '{y$t{x'", ch, buf, null, TO_CHAR, POS_DEAD);
        }

        for (d = descriptor_list; d != null; d = d.next) {
            if (d.connected == CON_PLAYING
                    && d.character != ch
                    && d.character.in_room != null
                    && d.character.in_room.area == ch.in_room.area
                    && !is_affected(d.character, gsn_deafen)) {
                var trans = translate(ch, d.character, buf);
                act("$n yells '{y$t{x'", ch, trans, d.character, TO_VICT, POS_DEAD);
            }
        }
    }

    static void do_emote(@NotNull CHAR_DATA ch, @NotNull String argument) {
        if (!IS_NPC(ch) && IS_SET(ch.comm, COMM_NOEMOTE)) {
            send_to_char("You can't show your emotions.\n", ch);
            return;
        }

        if (argument.isEmpty()) {
            send_to_char("Emote what?\n", ch);
            return;
        }

        String buf;
        if (is_affected(ch, gsn_garble)) {
            var buff = new StringBuilder();
            garble(buff, argument);
            buf = buff.toString();
        } else {
            buf = argument;
        }
        act("$n $T", ch, null, buf, TO_ROOM);
        act("$n $T", ch, null, buf, TO_CHAR);

    }


    static void do_pmote(@NotNull CHAR_DATA ch, @NotNull String argument) {
        CHAR_DATA vch;
        var matches = 0;

        if (!IS_NPC(ch) && IS_SET(ch.comm, COMM_NOEMOTE)) {
            send_to_char("You can't show your emotions.\n", ch);
            return;
        }

        if (argument.isEmpty()) {
            send_to_char("Emote what?\n", ch);
            return;
        }

        act("$n $t", ch, argument, null, TO_CHAR);

        for (vch = ch.in_room.people; vch != null; vch = vch.next_in_room) {
            if (vch.desc == null || vch == ch) {
                continue;
            }

            var letter = argument.indexOf(vch.name);
            if (letter == -1) {
                act("$N $t", vch, argument, ch, TO_CHAR);
                continue;
            }

            var temp = new StringBuilder(argument);
            var last = new StringBuilder();
            var name = vch.name;
            var namePos = 0;

            for (; letter < argument.length(); letter++) {
                var c = argument.charAt(letter);
                if (c == '\'' && matches == vch.name.length()) {
                    temp.append("r");
                    continue;
                }

                if (c == 's' && matches == vch.name.length()) {
                    matches = 0;
                    continue;
                }

                if (matches == vch.name.length()) {
                    matches = 0;
                }
                if (c == name.charAt(namePos)) {
                    matches++;
                    namePos++;
                    if (matches == vch.name.length()) {
                        temp.append("you");
                        name = vch.name;
                        continue;
                    }
                    last.append(c);
                    continue;
                }

                matches = 0;
                temp.append(last);
                temp.append(c);
                last.setLength(0);
                name = vch.name;
            }

            act("$N $t", vch, temp, ch, TO_CHAR);
        }

    }

    static void do_pose(@NotNull CHAR_DATA ch) {
        if (IS_NPC(ch) || ch.clazz.poses.isEmpty()) {
            return;
        }

        var poses = ch.clazz.poses;
        var level = UMIN(ch.level, poses.size() - 1);
        var poseIdx = number_range(0, level);

        var pose = poses.get(poseIdx);
        act(pose.message_to_char, ch, null, null, TO_CHAR);
        act(pose.message_to_room, ch, null, null, TO_ROOM);

    }


    static void do_bug(@NotNull CHAR_DATA ch, @NotNull String argument) {
        append_file(ch, nw_config.note_bug_file, argument);
        send_to_char("Bug logged.\n", ch);
    }

    static void do_typo(@NotNull CHAR_DATA ch, @NotNull String argument) {
        append_file(ch, nw_config.note_typo_file, argument);
        send_to_char("Typo logged.\n", ch);
    }

    static void do_rent(@NotNull CHAR_DATA ch) {
        send_to_char("There is no rent here.  Just save and quit.\n", ch);
    }


    static void do_qui(@NotNull CHAR_DATA ch) {
        send_to_char("If you want to QUIT, you have to spell it out.\n", ch);
    }


    static void do_quit(@NotNull CHAR_DATA ch) {
        quit_org(ch, false, false);
    }

    static void do_quit_count(@NotNull CHAR_DATA ch) {
        quit_org(ch, true, false);
    }

    static void do_quit_remort(@NotNull CHAR_DATA ch) {
        quit_org(ch, true, true);
    }

    static boolean quit_org(@NotNull CHAR_DATA ch, boolean count, boolean remort) {
        DESCRIPTOR_DATA d, dr, d_next;
        CHAR_DATA vch, vch_next;
        OBJ_DATA obj, obj_next;
        int id;

        if (IS_NPC(ch)) {
            return false;
        }

        if (ch.position == POS_FIGHTING) {
            send_to_char("No way! You are fighting.\n", ch);
            return false;
        }

        if (IS_AFFECTED(ch, AFF_SLEEP)) {
            send_to_char("Lie still! You are not awaken, yet.\n", ch);
            return false;
        }

        if (ch.position < POS_STUNNED) {
            send_to_char("You're not DEAD yet.\n", ch);
            return false;
        }

        if (ch.last_fight_time != -1 && !IS_IMMORTAL(ch) &&
                (current_time - ch.last_fight_time) < FIGHT_DELAY_TIME) {
            send_to_char("Your adrenalin is gushing! You can't quit yet.\n", ch);
            return false;
        }

        if (IS_AFFECTED(ch, AFF_CHARM)) {
            send_to_char("You don't want to leave your master.\n", ch);
            return false;
        }

        if (IS_SET(ch.act, PLR_NO_EXP)) {
            send_to_char("You don't want to lose your spirit.\n", ch);
            return false;
        }

        if (auction.item != null && ((ch == auction.buyer) || (ch == auction.seller))) {
            send_to_char("Wait till you have sold/bought the item on auction.\n", ch);
            return false;
        }

        if (!IS_IMMORTAL(ch) && ch.in_room != null && IS_RAFFECTED(ch.in_room, AFF_ROOM_ESPIRIT)) {
            send_to_char("Evil spirits in the area prevents you from leaving.\n", ch);
            return false;
        }

        if (!IS_IMMORTAL(ch) &&
                ch.cabal != CABAL_INVADER && is_affected(ch, gsn_evil_spirit)) {
            send_to_char("Evil spirits in you prevents you from leaving.\n", ch);
            return false;
        }

        if (cabal_area_check(ch)) {
            send_to_char("You cannot quit in other cabal's areas.\n", ch);
            return false;
        }

        if (!remort) {
            send_to_char("Alas, all good things must come to an end.\n", ch);
            act("{g$n has left the game.{x", ch, null, null, TO_ROOM, POS_DEAD);
            log_string(ch.name + " has quit.");
            wiznet("$N rejoins the real world.", ch, null, WIZ_LOGINS, 0, get_trust(ch));
        }

        for (obj = object_list; obj != null; obj = obj_next) {
            obj_next = obj.next;
            if (obj.pIndexData.vnum == 84
                    || obj.pIndexData.vnum == 85
                    || obj.pIndexData.vnum == 86
                    || obj.pIndexData.vnum == 97) {
                if (obj.extra_descr == null) {
                    extract_obj(obj);
                } else if (obj.extra_descr.description.contains(ch.name)) {
                    extract_obj(obj);
                }
            }
        }

        for (obj = ch.carrying; obj != null; obj = obj_next) {
            obj_next = obj.next_content;
            if (obj.pIndexData.vnum == OBJ_VNUM_MAGIC_JAR) {
                extract_obj(obj);
            }
            if (obj.pIndexData.vnum == 84
                    || obj.pIndexData.vnum == 85
                    || obj.pIndexData.vnum == 86
                    || obj.pIndexData.vnum == 97) {
                if (obj.extra_descr == null) {
                    extract_obj(obj);
                } else if (obj.extra_descr.description.contains(ch.name)) {
                    extract_obj(obj);
                } else {
                    obj_from_char(obj);
                    obj_to_room(obj, ch.in_room);
                }
            }
        }

        for (vch = char_list; vch != null; vch = vch_next) {
            vch_next = vch.next;
            if (is_affected(vch, gsn_doppelganger) && vch.doppel == ch) {
                send_to_char("You shift to your true form as your victim leaves.\n",
                        vch);
                affect_strip(vch, gsn_doppelganger);
            }

            if (vch.guarding == ch) {
                act("You stops guarding $N.", vch, null, ch, TO_CHAR);
                act("$n stops guarding you.", vch, null, ch, TO_VICT);
                act("$n stops guarding $N.", vch, null, ch, TO_NOTVICT);
                vch.guarding = null;
                ch.guarded_by = null;
            }

            if (vch.last_fought == ch) {
                vch.last_fought = null;
                back_home(vch);
            }

            if (vch.hunting == ch) {
                vch.hunting = null;
            }

        }

        if (ch.guarded_by != null) {
            ch.guarded_by.guarding = null;
            ch.guarded_by = null;
        }

        /*
         * After extract_char the ch is no longer valid!
         */
        save_char_obj(ch);
        id = ch.id;
        dr = d = ch.desc;
        if (count || get_total_played(ch) < nw_config.min_time_limit) {
            extract_char(ch, true);
        } else {
            extract_char_nocount(ch, true);
        }

        if (d != null && !remort) {
            close_socket(d);
        }

        /* toast evil cheating bastards    */
        for (d = descriptor_list; d != null; d = d_next) {
            CHAR_DATA tch;

            d_next = d.next;
            if (remort && dr == d) {
                continue;
            }
            tch = d.original != null ? d.original : d.character;
            if (tch != null && tch.id == id) {
                extract_char_nocount(tch, true);
                close_socket(d);
            }
        }

        iNumPlayers--;

        return true;
    }


    static void do_save(@NotNull CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }

        if (ch.level < 2 && !IS_SET(ch.act, PLR_REMORTED)) {
            send_to_char("You must be at least level 2 for saving.\n", ch);
            return;
        }
        save_char_obj(ch);
        send_to_char("Saving. Remember that Nightworks MUD has automatic saving.\n", ch);
        WAIT_STATE(ch, PULSE_VIOLENCE);
    }


    static void do_follow(@NotNull CHAR_DATA ch, @NotNull String argument) {
        /* RT changed to allow unlimited following and follow the NOFOLLOW rules */
        CHAR_DATA victim;
        var arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.isEmpty()) {
            send_to_char("Follow whom?\n", ch);
            return;
        }

        if ((victim = get_char_room(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (IS_AFFECTED(ch, AFF_CHARM) && ch.master != null) {
            act("But you'd rather follow $N!", ch, null, ch.master, TO_CHAR);
            return;
        }

        if (victim == ch) {
            if (ch.master == null) {
                send_to_char("You already follow yourself.\n", ch);
                return;
            }
            stop_follower(ch);
            return;
        }

        if (!IS_NPC(victim) && IS_SET(victim.act, PLR_NOFOLLOW) && !IS_IMMORTAL(ch)) {
            act("$N doesn't seem to want any followers.\n",
                    ch, null, victim, TO_CHAR);
            return;
        }

        ch.act = REMOVE_BIT(ch.act, PLR_NOFOLLOW);

        if (ch.master != null) {
            stop_follower(ch);
        }

        add_follower(ch, victim);
    }


    static void add_follower(@NotNull CHAR_DATA ch, @NotNull CHAR_DATA master) {
        if (ch.master != null) {
            bug("Add_follower: non-null master.");
            return;
        }

        ch.master = master;
        ch.leader = null;

        if (can_see(master, ch)) {
            act("{Y$n now follows you.{x", ch, null, master, TO_VICT, POS_RESTING);
        }
        act("{YYou now follow $N.{x", ch, null, master, TO_CHAR, POS_RESTING);

    }


    static void stop_follower(@NotNull CHAR_DATA ch) {
        if (ch.master == null) {
            bug("Stop_follower: null master.");
            return;
        }

        if (IS_AFFECTED(ch, AFF_CHARM)) {
            ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_CHARM);
            affect_strip(ch, gsn_charm_person);
        }

        if (can_see(ch.master, ch) && ch.in_room != null) {
            act("{b$n stops following you.{x", ch, null, ch.master, TO_VICT, POS_RESTING);
            act("{bYou stop following $N.{x", ch, null, ch.master, TO_CHAR, POS_RESTING);
        }
        if (ch.master.pet == ch) {
            ch.master.pet = null;
        }

        ch.master = null;
        ch.leader = null;
    }

    /* nukes charmed monsters and pets */

    static void nuke_pets(@NotNull CHAR_DATA ch) {
        CHAR_DATA pet;

        if ((pet = ch.pet) != null) {
            stop_follower(pet);
            if (pet.in_room != null) {
                act("$N slowly fades away.", ch, null, pet, TO_NOTVICT);
            }
            extract_char_nocount(pet, true);
        }
        ch.pet = null;

    }


    static void die_follower(@NotNull CHAR_DATA ch) {
        CHAR_DATA fch;
        CHAR_DATA fch_next;

        if (ch.master != null) {
            if (ch.master.pet == ch) {
                ch.master.pet = null;
            }
            stop_follower(ch);
        }

        ch.leader = null;

        for (fch = char_list; fch != null; fch = fch_next) {
            fch_next = fch.next;
            if (fch.master == ch) {
                stop_follower(fch);
            }
            if (fch.leader == ch) {
                fch.leader = fch;
            }
        }

    }


    static void do_order(@NotNull CHAR_DATA ch, @NotNull String argument) {
        CHAR_DATA victim;
        CHAR_DATA och;
        CHAR_DATA och_next;
        boolean found;
        boolean fAll;

        var arg = new StringBuilder();
        var arg2 = new StringBuilder();
        argument = one_argument(argument, arg);
        one_argument(argument, arg2);

        if (!str_cmp(arg2.toString(), "delete")) {
            send_to_char("That will NOT be done.\n", ch);
            return;
        }

        if (arg.isEmpty() || argument.isEmpty()) {
            send_to_char("Order whom to do what?\n", ch);
            return;
        }

        if (IS_AFFECTED(ch, AFF_CHARM)) {
            send_to_char("You feel like taking, not giving, orders.\n", ch);
            return;
        }

        if (!str_cmp(arg.toString(), "all")) {
            fAll = true;
            victim = null;
        } else {
            fAll = false;
            if ((victim = get_char_room(ch, arg.toString())) == null) {
                send_to_char("They aren't here.\n", ch);
                return;
            }

            if (victim == ch) {
                send_to_char("Aye aye, right away!\n", ch);
                return;
            }

            if (!IS_AFFECTED(victim, AFF_CHARM) || victim.master != ch
                    || (IS_IMMORTAL(victim) && victim.trust >= ch.trust)) {
                send_to_char("Do it yourself!\n", ch);
                return;
            }
        }

        found = false;
        for (och = ch.in_room.people; och != null; och = och_next) {
            och_next = och.next_in_room;

            if (IS_AFFECTED(och, AFF_CHARM) && och.master == ch && (fAll || och == victim)) {
                found = true;
                if (!proper_order(och, argument)) {
                    continue;
                }
                var buf = new TextBuffer();
                buf.sprintf("$n orders you to '%s', you do.", argument);
                act(buf.toString(), ch, null, och, TO_VICT);
                interpret(och, argument, true);
            }
        }

        if (found) {
            WAIT_STATE(ch, PULSE_VIOLENCE);
            send_to_char("Ok.\n", ch);
        } else {
            send_to_char("You have no followers here.\n", ch);
        }
    }

    static boolean proper_order(@NotNull CHAR_DATA ch, @NotNull String argument) {
        var command = new StringBuilder();
        one_argument(argument, command);

        var trust = get_trust(ch);
        var cmd_table = getCommandsTable();
        CmdType cmd = null;
        var commandStr = command.toString();
        for (var c : cmd_table) {
            for (var name : c.names) {
                if (commandStr.charAt(0) == name.charAt(0) && !str_prefix(commandStr, name) && c.level <= trust) {
                    cmd = c;
                    break;
                }
            }
            if (cmd != null) {
                break;
            }
        }
        if (cmd == null) {
            return true;
        }
        if (!IS_NPC(ch)) {
            return !(cmd == CmdType.do_delete || cmd == CmdType.do_remort || cmd == CmdType.do_induct
                    || cmd == CmdType.do_quest || cmd == CmdType.do_practice || cmd == CmdType.do_train);
        }

        if (((cmd == CmdType.do_bash) || (cmd == CmdType.do_dirt) || (cmd == CmdType.do_kick)
                || (cmd == CmdType.do_murder) || (cmd == CmdType.do_trip))
                && ch.fighting == null) {
            return false;
        }

        return switch (cmd) {
            case do_assassinate, do_ambush, do_blackjack, do_cleave, do_kill, do_murder, do_recall, do_strangle, do_vtouch -> false;
            case do_backstab, do_hide, do_pick, do_sneak -> IS_SET(ch.act, ACT_THIEF);
            default -> true;
        };
    }


    static void do_group(@NotNull CHAR_DATA ch, @NotNull String argument) {
        CHAR_DATA victim;
        var arg = new StringBuilder();

        one_argument(argument, arg);

        if (arg.isEmpty()) {
            CHAR_DATA gch;
            CHAR_DATA leader;

            leader = (ch.leader != null) ? ch.leader : ch;
            var buf = new TextBuffer();
            buf.sprintf("%s's group:\n", PERS(leader, ch));
            send_to_char(buf.toString(), ch);

            for (gch = char_list; gch != null; gch = gch.next) {
                if (is_same_group(gch, ch)) {
                    buf.clear();
                    buf.sprintf("[%2d %s] %-16s %d/%d hp %d/%d mana %d/%d mv   %5d xp\n",
                            gch.level,
                            IS_NPC(gch) ? "Mob" : gch.clazz.who_name,
                            TextUtils.capitalize(PERS(gch, ch)),
                            gch.hit, gch.max_hit,
                            gch.mana, gch.max_mana,
                            gch.move, gch.max_move,
                            gch.exp);
                    send_to_char(buf.toString(), ch);
                }
            }
            return;
        }

        if ((victim = get_char_room(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (ch.master != null || (ch.leader != null && ch.leader != ch)) {
            send_to_char("But you are following someone else!\n", ch);
            return;
        }

        if (victim.master != ch && ch != victim) {
            act("$N isn't following you.", ch, null, victim, TO_CHAR);
            return;
        }

        if (IS_AFFECTED(victim, AFF_CHARM)) {
            send_to_char("You can't remove charmed mobs from your group.\n", ch);
            return;
        }

        if (IS_AFFECTED(ch, AFF_CHARM)) {
            act("You like your master too much to leave $m!", ch, null, victim, TO_VICT);
            return;
        }


        if (is_same_group(victim, ch) && ch != victim) {
            if (ch.guarding == victim || victim.guarded_by == ch) {
                act("You stop guarding $N.", ch, null, victim, TO_CHAR);
                act("$n stops guarding you.", ch, null, victim, TO_VICT);
                act("$n stops guarding $N.", ch, null, victim, TO_NOTVICT);
                victim.guarded_by = null;
                ch.guarding = null;
            }

            victim.leader = null;
            act("{Y$n removes $N from $s group.{x", ch, null, victim, TO_NOTVICT, POS_SLEEPING);
            act("{Y$n removes you from $s group.{x", ch, null, victim, TO_VICT, POS_SLEEPING);
            act("{bYou remove $N from your group.{x", ch, null, victim, TO_CHAR, POS_SLEEPING);

            if (victim.guarded_by != null && !is_same_group(victim, victim.guarded_by)) {
                act("You stop guarding $N.", victim.guarded_by, null, victim, TO_CHAR);
                act("$n stops guarding you.", victim.guarded_by, null, victim, TO_VICT);
                act("$n stops guarding $N.", victim.guarded_by, null, victim, TO_NOTVICT);
                victim.guarded_by.guarding = null;
                victim.guarded_by = null;
            }
            return;
        }

        if (ch.level - victim.level < -8
                || ch.level - victim.level > 8) {
            act("{R$N cannot join $n's group.{x", ch, null, victim, TO_NOTVICT, POS_SLEEPING);
            act("{RYou cannot join $n's group.{x", ch, null, victim, TO_VICT, POS_SLEEPING);
            act("{R$N cannot join your group.{x", ch, null, victim, TO_CHAR, POS_SLEEPING);
            return;
        }

        if (IS_GOOD(ch) && IS_EVIL(victim)) {
            act("{rYou are too evil for $n's group.{x", ch, null, victim, TO_VICT, POS_SLEEPING);
            act("{r$N is too evil for your group!{x", ch, null, victim, TO_CHAR, POS_SLEEPING);
            return;
        }

        if (IS_GOOD(victim) && IS_EVIL(ch)) {
            act("{rYou are too pure to join $n's group!{x", ch, null, victim, TO_VICT, POS_SLEEPING);
            act("{r$N is too pure for your group!{x", ch, null, victim, TO_CHAR, POS_SLEEPING);
            return;
        }

        if ((ch.cabal == CABAL_RULER && victim.cabal == CABAL_CHAOS) ||
                (ch.cabal == CABAL_CHAOS && victim.cabal == CABAL_RULER) ||
                (ch.cabal == CABAL_KNIGHT && victim.cabal == CABAL_INVADER) ||
                (ch.cabal == CABAL_INVADER && victim.cabal == CABAL_KNIGHT) ||
                (ch.cabal == CABAL_SHALAFI && victim.cabal == CABAL_BATTLE) ||
                (ch.cabal == CABAL_BATTLE && victim.cabal == CABAL_SHALAFI)) {
            act("{rYou hate $n's cabal, how can you join $n's group?!{x", ch, null, victim, TO_VICT, POS_SLEEPING);
            act("{rYou hate $N's cabal, how can you want $N to join your group?!{x", ch, null, victim, TO_CHAR, POS_SLEEPING);
            return;
        }


        victim.leader = ch;
        act("{Y$N joins $n's group.{x", ch, null, victim, TO_NOTVICT, POS_SLEEPING);
        act("{YYou join $n's group.{x", ch, null, victim, TO_VICT, POS_SLEEPING);
        act("{b$N joins your group.{x", ch, null, victim, TO_CHAR, POS_SLEEPING);

    }

    /*
     * 'Split' originally by Gnort, God of Chaos.
     */

    static void do_split(@NotNull CHAR_DATA ch, @NotNull String argument) {
        CHAR_DATA gch;
        int members;
        int amount_gold = 0, amount_silver;
        int share_gold, share_silver;
        int extra_gold, extra_silver;

        var arg1 = new StringBuilder();
        var arg2 = new StringBuilder();
        argument = one_argument(argument, arg1);
        one_argument(argument, arg2);

        if (arg1.isEmpty()) {
            send_to_char("Split how much?\n", ch);
            return;
        }


        amount_silver = atoi(arg1.toString());

        if (!arg2.isEmpty()) {
            amount_gold = atoi(arg2.toString());
        }

        if (amount_gold < 0 || amount_silver < 0) {
            send_to_char("Your group wouldn't like that.\n", ch);
            return;
        }

        if (amount_gold == 0 && amount_silver == 0) {
            send_to_char("You hand out zero coins, but no one notices.\n", ch);
            return;
        }

        if (ch.gold < amount_gold || ch.silver < amount_silver) {
            send_to_char("You don't have that much to split.\n", ch);
            return;
        }

        members = 0;
        for (gch = ch.in_room.people; gch != null; gch = gch.next_in_room) {
            if (is_same_group(gch, ch) && !IS_AFFECTED(gch, AFF_CHARM)) {
                members++;
            }
        }

        if (members < 2) {
            send_to_char("Just keep it all.\n", ch);
            return;
        }

        share_silver = amount_silver / members;
        extra_silver = amount_silver % members;

        share_gold = amount_gold / members;
        extra_gold = amount_gold % members;

        if (share_gold == 0 && share_silver == 0) {
            send_to_char("Don't even bother, cheapskate.\n", ch);
            return;
        }

        ch.silver -= amount_silver;
        ch.silver += share_silver + extra_silver;
        ch.gold -= amount_gold;
        ch.gold += share_gold + extra_gold;

        var buf = new TextBuffer();

        if (share_silver > 0) {
            buf.sprintf(
                    "You split %d silver coins. Your share is %d silver.\n",
                    amount_silver, share_silver + extra_silver);
            send_to_char(buf, ch);
        }

        if (share_gold > 0) {
            buf.sprintf(
                    "You split %d gold coins. Your share is %d gold.\n",
                    amount_gold, share_gold + extra_gold);
            send_to_char(buf, ch);
        }

        if (share_gold == 0) {
            buf.sprintf("$n splits %d silver coins. Your share is %d silver.",
                    amount_silver, share_silver);
        } else if (share_silver == 0) {
            buf.sprintf("$n splits %d gold coins. Your share is %d gold.",
                    amount_gold, share_gold);
        } else {
            buf.sprintf(
                    "$n splits %d silver and %d gold coins, giving you %d silver and %d gold.\n",
                    amount_silver, amount_gold, share_silver, share_gold);
        }

        for (gch = ch.in_room.people; gch != null; gch = gch.next_in_room) {
            if (gch != ch && is_same_group(gch, ch) && !IS_AFFECTED(gch, AFF_CHARM)) {
                act(buf.toString(), ch, null, gch, TO_VICT);
                gch.gold += share_gold;
                gch.silver += share_silver;
            }
        }

    }


    static void do_gtell(@NotNull CHAR_DATA ch, @NotNull String argument) {
        CHAR_DATA gch;
        int i;

        if (argument.isEmpty()) {
            send_to_char("Tell your group what?\n", ch);
            return;
        }

        if (IS_SET(ch.comm, COMM_NOTELL)) {
            send_to_char("Your message didn't get through!\n", ch);
            return;
        }

        String buf;
        if (is_affected(ch, gsn_garble)) {
            var buff = new StringBuilder();
            garble(buff, argument);
            buf = buff.toString();
        } else {
            buf = argument;
        }
        /*
         * Note the use of send_to_char, so gtell works on sleepers.
         */

        for (i = 0, gch = char_list; gch != null; gch = gch.next) {
            if (is_same_group(gch, ch) && !is_affected(gch, gsn_deafen)) {
                act("{m$n tells the group '$t'{x", ch, buf, gch, TO_VICT, POS_DEAD);
                i++;
            }
        }

        if (i > 1 && !is_affected(ch, gsn_deafen)) {
            act("{cYou tell your group '$t'{x", ch, buf, null, TO_CHAR, POS_DEAD);
        } else {
            send_to_char("Quit talking to yourself. You are all alone.", ch);
        }

    }

    /*
     * It is very important that this be an equivalence relation:
     * (1) A ~ A
     * (2) if A ~ B then B ~ A
     * (3) if A ~ B  and B ~ C, then A ~ C
     */

    static boolean is_same_group_old(@NotNull CHAR_DATA ach, @NotNull CHAR_DATA bch) {
        if (ach.leader != null) {
            ach = ach.leader;
        }
        if (bch.leader != null) {
            bch = bch.leader;
        }
        return ach == bch;
    }

    /*
     * New is_same_group by chronos
     */

    static boolean is_same_group(@NotNull CHAR_DATA ach, @NotNull CHAR_DATA bch) {
        CHAR_DATA ch, vch, ch_next, vch_next;
        int count, vcount;

        count = vcount = 0;
        for (ch = ach; ch != null; ch = ch_next) {
            ch_next = ch.leader;
            for (vch = bch; vch != null; vch = vch_next) {
                vch_next = vch.leader;
                if (ch == vch) {
                    return true;
                }
                if (++vcount > 6) {
                    break;    /* cyclic loop! */
                }
            }
            if (++count > 6) {
                break;  /* cyclic loop! */
            }
        }
        return false;
    }


    static void do_cb(@NotNull CHAR_DATA ch, @NotNull String argument) {
        DESCRIPTOR_DATA d;

        if (ch.cabal == 0) {
            send_to_char("You are not in a Cabal.\n", ch);
            return;
        }
        var buf = new TextBuffer();
        buf.sprintf("[%s] $n: {y$t{x", cabal_table[ch.cabal].short_name);

        String buf2;
        if (is_affected(ch, gsn_garble)) {
            var buff = new StringBuilder();
            garble(buff, argument);
            buf2 = buff.toString();
        } else {
            buf2 = argument;
        }
        if (!is_affected(ch, gsn_deafen)) {
            act(buf.toString(), ch, argument, null, TO_CHAR, POS_DEAD);
        }
        for (d = descriptor_list; d != null; d = d.next) {
            if (d.connected == CON_PLAYING &&
                    (d.character.cabal == ch.cabal) &&
                    /*             !IS_SET(d.character.comm,COMM_NOCB) &&   */
                    !is_affected(d.character, gsn_deafen)) {
                act(buf.toString(), ch, buf2, d.character, TO_VICT, POS_DEAD);
            }
        }

    }

    static void do_pray(@NotNull CHAR_DATA ch, @NotNull String argument) {
        DESCRIPTOR_DATA d;

        if (IS_SET(ch.comm, COMM_NOCHANNELS)) {
            send_to_char("The gods refuse to listen to you right now.", ch);
            return;
        }

        send_to_char("You pray to the heavens for help!\n", ch);
        send_to_char("This is not an emote, but a channel to the immortals.\n",
                ch);

        for (d = descriptor_list; d != null; d = d.next) {
            if (d.connected == CON_PLAYING && IS_IMMORTAL(d.character) &&
                    !IS_SET(d.character.comm, COMM_NOWIZ)) {
                if (argument.isEmpty()) {
                    act("{c$n is PRAYING for: any god{x", ch, argument, d.character, TO_VICT, POS_DEAD);
                } else {
                    act("{c$n is PRAYING for: $t{x", ch, argument, d.character, TO_VICT, POS_DEAD);
                }
            }
        }
    }

    static char char_lang_lookup(char c) {
        for (var aTranslation_table : translation_table) {
            if (aTranslation_table.common == c) {
                return aTranslation_table.language;
            }
        }
        return c;
    }

    /**
     * ch says
     * the victim hears
     */
    static String translate(@NotNull CHAR_DATA ch, @NotNull CHAR_DATA victim, @NotNull String argument) {
        var trans = new TextBuffer();
        if (argument.isEmpty()
                || IS_NPC(ch) || IS_NPC(victim)
                || IS_IMMORTAL(ch) || IS_IMMORTAL(victim)
                || ch.language == LANG_COMMON
                || ch.language == ORG_RACE(victim).pcRace.language) {
            if (IS_IMMORTAL(victim)) {
                trans.sprintf("{%s} %s", language_table[ch.language].name, argument);
            } else {
                trans.append(argument);
            }
            return trans.toString();
        }

        var buf = new StringBuilder();
        for (var i = 0; i < argument.length(); i++) {
            var c = char_lang_lookup(argument.charAt(i));
            buf.append(c);
        }

        trans.sprintf("{%s} %s", language_table[ch.language].name, buf.toString());
        return trans.toString();
    }


    static void do_speak(@NotNull CHAR_DATA ch, @NotNull String argument) {
        int language;

        if (IS_NPC(ch)) {
            return;
        }
        var arg = new StringBuilder();

        one_argument(argument, arg);
        if (arg.isEmpty()) {
            var buf = new TextBuffer();
            buf.sprintf("You now speak %s.\n", language_table[ch.language].name);
            send_to_char(buf, ch);
            send_to_char("You can speak :\n", ch);
            buf.sprintf("       common, %s\n", language_table[ORG_RACE(ch).pcRace.language].name);
            send_to_char(buf, ch);
            return;
        }

        language = lang_lookup(arg.toString());

        if (language == -1) {
            send_to_char("You never heard of that language.\n", ch);
            return;
        }

        if (language >= MAX_LANGUAGE) {
            ch.language = ORG_RACE(ch).pcRace.language;
        } else {
            ch.language = language;
        }
        var buf = new TextBuffer();
        buf.sprintf("Now you speak %s.\n", language_table[ch.language].name);
        send_to_char(buf, ch);
    }

    /* Thanx zihni@karmi.emu.edu.tr for the code of do_judge */

    static void do_judge(@NotNull CHAR_DATA ch, @NotNull String argument) {
        CHAR_DATA victim;

        if (skill_failure_check(ch, gsn_judge, true, 0, null)) {
            return;
        }
        var arg = new StringBuilder();

        one_argument(argument, arg);

        if (arg.isEmpty()) {
            send_to_char("Judge whom?\n", ch);
            return;
        }

        /* judge through the world */
        if ((victim = get_char_world(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }


        if (IS_NPC(victim)) {
            send_to_char("Not a mobile, of course.\n", ch);
            return;
        }

        if (IS_IMMORTAL(victim) && !IS_IMMORTAL(ch)) {
            send_to_char("You do not have the power to judge Immortals.\n", ch);
            return;
        }
        var buf = new TextBuffer();
        buf.sprintf("%s's ethos is %s and aligment is %s.\n",
                victim.name,
                DB.capitalize(ethos_table[victim.ethos].name),
                IS_GOOD(victim) ? "Good" : IS_EVIL(victim) ? "Evil" : "Neutral");

        send_to_char(buf, ch);
    }

    static void do_remor(@NotNull CHAR_DATA ch) {
        send_to_char("If you want to REMORT, spell it out.\n", ch);
    }

    static void do_remort(@NotNull CHAR_DATA ch, @NotNull String argument) {
        int bankg, banks, qp, silver, gold;

        if (IS_NPC(ch) || ch.desc == null) {
            return;
        }

        if (ch.level != LEVEL_HERO) {
            send_to_char("You must be a HERO to remort.\n", ch);
            return;
        }

        if (!IS_SET(ch.act, PLR_CANREMORT) && !IS_SET(ch.act, PLR_REMORTED)) {
            send_to_char("You have to get permission from an immortal to remort.\n", ch);
            return;
        }

        if (!argument.isEmpty()) {
            if (!ch.pcdata.confirm_remort) {
                send_to_char("Just type remort. No argument.\n", ch);
            }
            ch.pcdata.confirm_remort = false;
            return;
        }

        if (ch.pcdata.confirm_remort) {
            ch.act = SET_BIT(ch.act, PLR_REMORTED);
            send_to_char("\nNOW YOU ARE REMORTING.\n", ch);
            send_to_char("You will create a new char with new race, class and new stats.\n", ch);
            send_to_char("If you are somehow disconnected from the mud or mud crashes:\n", ch);
            send_to_char("    CREATE A NEW CHARACTER WITH THE SAME NAME AND NOTE TO IMMORTALS\n", ch);
            send_to_char("Note that, the items below will be saved:\n", ch);
            send_to_char("        all of the gold and silver you have (also in bank)\n", ch);
            send_to_char("        your current questpoints.\n", ch);
            send_to_char("IN ADDITION, you will be able to wear two more rings.\n", ch);
            send_to_char("             You will have additional 10 trains.\n", ch);

            var pbuf = ch.pcdata.pwd;
            var remstr = nw_config.lib_player_dir + "/" + TextUtils.capitalize(ch.name);
//        String  mkstr =  nw_config.lib_remort_dir +"/"+ capitalize( ch.name ) ;
            var name = ch.name;
            var d = ch.desc;
            banks = ch.pcdata.bank_s;
            bankg = ch.pcdata.bank_g;
            qp = ch.pcdata.questpoints;
            silver = ch.silver;
            gold = ch.gold;

            if (!quit_org(ch, true, true)) {
                return;
            }

            //TODO: link( remstr, mkstr );
            //noinspection ResultOfMethodCallIgnored
            new File(remstr).delete();

            load_char_obj(d, name);

            ch = d.character;
            ch.pcdata.pwd = pbuf;
            d.connected = CON_REMORTING;

            /* give the remorting bonus */
            ch.pcdata.bank_s += banks;
            ch.pcdata.bank_g += bankg;
            ch.silver += silver;
            ch.gold += gold;
            ch.pcdata.questpoints += qp;
            ch.train += 10;

            write_to_buffer(d, "\n[Hit Return to Continue]\n");
            return;
        }

        send_to_char("Type remort again to confirm this command.\n", ch);
        send_to_char("WARNING: this command is irreversible.\n", ch);
        send_to_char("Typing remort with an argument will undo remort status.\n", ch);
        send_to_char("Note that, the items below will be saved:\n", ch);
        send_to_char("        all of the gold and silver you have (also in bank)\n", ch);
        send_to_char("        your current practice, train and questpoints\n", ch);
        send_to_char("IN ADDITION, you will be able to wear two more rings.\n", ch);
        ch.pcdata.confirm_remort = true;
        wiznet("$N is contemplating remorting.", ch, null, 0, 0, get_trust(ch));

    }


    static boolean cabal_area_check(@NotNull CHAR_DATA ch) {
        if (ch.in_room == null || IS_IMMORTAL(ch)) {
            return false;
        }

        if (ch.cabal != CABAL_RULER &&
                !str_cmp(ch.in_room.area.name, "Ruler")) {
            return true;
        } else if (ch.cabal != CABAL_INVADER &&
                !str_cmp(ch.in_room.area.name, "Invader")) {
            return true;
        } else if (ch.cabal != CABAL_CHAOS &&
                !str_cmp(ch.in_room.area.name, "Chaos")) {
            return true;
        } else if (ch.cabal != CABAL_SHALAFI &&
                !str_cmp(ch.in_room.area.name, "Shalafi")) {
            return true;
        } else if (ch.cabal != CABAL_BATTLE &&
                !str_cmp(ch.in_room.area.name, "Battlerager")) {
            return true;
        } else if (ch.cabal != CABAL_KNIGHT &&
                !str_cmp(ch.in_room.area.name, "Knight")) {
            return true;
        } else if (ch.cabal != CABAL_HUNTER &&
                !str_cmp(ch.in_room.area.name, "Hunter")) {
            return true;
        }
        return !(ch.cabal == CABAL_LIONS || str_cmp(ch.in_room.area.name, "Lions"));
    }

    static boolean is_at_cabal_area(@NotNull CHAR_DATA ch) {
        //noinspection SimplifiableIfStatement
        if (ch.in_room == null || IS_IMMORTAL(ch)) {
            return false;
        }

        return !str_cmp(ch.in_room.area.name, "Ruler") ||
                !str_cmp(ch.in_room.area.name, "Invader") ||
                !str_cmp(ch.in_room.area.name, "Chaos") ||
                !str_cmp(ch.in_room.area.name, "Shalafi") ||
                !str_cmp(ch.in_room.area.name, "Battlerager") ||
                !str_cmp(ch.in_room.area.name, "Knight") ||
                !str_cmp(ch.in_room.area.name, "Hunter") ||
                !str_cmp(ch.in_room.area.name, "Lions");
    }
}
