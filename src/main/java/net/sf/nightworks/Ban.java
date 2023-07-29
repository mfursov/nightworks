package net.sf.nightworks;

import net.sf.nightworks.util.DikuTextFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Formatter;

import static net.sf.nightworks.Comm.dump_to_scr;
import static net.sf.nightworks.Comm.page_to_char;
import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.Handler.get_trust;
import static net.sf.nightworks.Nightworks.BAN_ALL;
import static net.sf.nightworks.Nightworks.BAN_DATA;
import static net.sf.nightworks.Nightworks.BAN_NEWBIES;
import static net.sf.nightworks.Nightworks.BAN_PERMANENT;
import static net.sf.nightworks.Nightworks.BAN_PERMIT;
import static net.sf.nightworks.Nightworks.BAN_PLAYER;
import static net.sf.nightworks.Nightworks.BAN_PREFIX;
import static net.sf.nightworks.Nightworks.BAN_SUFFIX;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.IS_SET;
import static net.sf.nightworks.Nightworks.SET_BIT;
import static net.sf.nightworks.Nightworks.nw_config;
import static net.sf.nightworks.Nightworks.perror;
import static net.sf.nightworks.Save.print_flags;
import static net.sf.nightworks.util.TextUtils.one_argument;
import static net.sf.nightworks.util.TextUtils.str_cmp;
import static net.sf.nightworks.util.TextUtils.str_prefix;
import static net.sf.nightworks.util.TextUtils.str_suffix;

class Ban {
    static BAN_DATA ban_list;

    static void save_bans() {
        BAN_DATA pban;
        boolean found = false;
        StringBuilder buf = new StringBuilder();
        RandomAccessFile fp;
        try {
            fp = new RandomAccessFile(nw_config.var_ban_file, "w");
            try {
                Formatter f = new Formatter(buf);
                for (pban = ban_list; pban != null; pban = pban.next) {
                    if (IS_SET(pban.ban_flags, BAN_PERMANENT)) {
                        found = true;
                        f.format("%-20s %-2d %s\n", pban.name, pban.level, print_flags(pban.ban_flags));
                        dump_to_scr(buf.toString());
                        buf.setLength(0);
                        f.format("%-20s %-2d %s\n", pban.name, pban.level, print_flags(pban.ban_flags));
                        fp.writeChars(buf.toString());
                    }
                }
                if (!found) {
                    //noinspection ResultOfMethodCallIgnored
                    new File(nw_config.var_ban_file).delete();
                }

            } finally {
                fp.close();
            }
        } catch (IOException e) {
            perror(nw_config.var_ban_file);
        }

    }

    static void load_bans() {
        try {
            DikuTextFile fp = new DikuTextFile(nw_config.var_ban_file);
            BAN_DATA ban_last = null;
            while (!fp.feof()) {
                BAN_DATA pban = new BAN_DATA();
                pban.name = fp.fread_word();
                pban.level = fp.fread_number();
                pban.ban_flags = fp.fread_flag();
                fp.fread_to_eol();

                if (ban_list == null) {
                    ban_list = pban;
                } else {
                    assert (ban_last != null);
                    ban_last.next = pban;
                }
                ban_last = pban;
            }
        } catch (IOException e) {
            perror(nw_config.var_ban_file);
        }
    }

    static boolean check_ban(String site, int type) {
        String host = site.toLowerCase();
        for (BAN_DATA pban = ban_list; pban != null; pban = pban.next) {
            if (!IS_SET(pban.ban_flags, type)) {
                continue;
            }

            if (IS_SET(pban.ban_flags, BAN_PREFIX) && IS_SET(pban.ban_flags, BAN_SUFFIX) && pban.name.contains(host)) {
                return true;
            }

            if (IS_SET(pban.ban_flags, BAN_PREFIX) && !str_suffix(pban.name, host)) {
                return true;
            }

            if (IS_SET(pban.ban_flags, BAN_SUFFIX) && !str_prefix(pban.name, host)) {
                return true;
            }
        }

        return false;
    }


    static void ban_site(CHAR_DATA ch, String argument, boolean fPerm) {
        BAN_DATA pban, prev;
        boolean prefix, suffix = false;
        int type;

        StringBuilder arg1 = new StringBuilder();
        argument = one_argument(argument, arg1);

        if (arg1.isEmpty()) {
            if (ban_list == null) {
                send_to_char("No sites banned at this time.\n", ch);
                return;
            }
            StringBuilder buffer = new StringBuilder("Banned sites  level  type     status\n");
            StringBuilder buf2 = new StringBuilder();
            StringBuilder buf = new StringBuilder();
            Formatter f = new Formatter(buf);
            for (pban = ban_list; pban != null; pban = pban.next) {
                buf2.delete(0, buf2.length());
                buf2.append(IS_SET(pban.ban_flags, BAN_PREFIX) ? "*" : "").append(pban.name).append(IS_SET(pban.ban_flags, BAN_SUFFIX) ? "*" : "");
                buf.setLength(0);
                f.format("%-12s    %-3d  %-7s  %s\n", buf2, pban.level, IS_SET(pban.ban_flags, BAN_NEWBIES) ? "newbies" :
                                IS_SET(pban.ban_flags, BAN_PLAYER) ? "player" :
                                        IS_SET(pban.ban_flags, BAN_PERMIT) ? "permit" :
                                                IS_SET(pban.ban_flags, BAN_ALL) ? "all" : "",
                        IS_SET(pban.ban_flags, BAN_PERMANENT) ? "perm" : "temp");
                buffer.append(buf);
            }

            page_to_char(buffer, ch);
            return;
        }

        StringBuilder arg2 = new StringBuilder();
        one_argument(argument, arg2);

        /* find out what type of ban */
        String arg2Str = arg2.toString();
        if (arg2.isEmpty() || !str_prefix(arg2Str, "all")) {
            type = BAN_ALL;
        } else if (!str_prefix(arg2Str, "newbies")) {
            type = BAN_NEWBIES;
        } else if (!str_prefix(arg2Str, "player")) {
            type = BAN_PLAYER;
        } else if (!str_prefix(arg2Str, "permit")) {
            type = BAN_PERMIT;
        } else {
            send_to_char("Acceptable ban types are all, newbies, player, and permit.\n", ch);
            return;
        }

        String name = arg1.charAt(0) == '*' ? arg1.substring(1) : arg1.toString();
        prefix = name.length() < arg1.length();
        if (name.charAt(name.length() - 1) == '*') {
            suffix = true;
            name = name.substring(0, name.length() - 1);
        }

        if (name.isEmpty()) {
            send_to_char("You have to ban SOMETHING.\n", ch);
            return;
        }

        prev = null;
        for (pban = ban_list; pban != null; prev = pban, pban = pban.next) {
            if (!str_cmp(name, pban.name)) {
                if (pban.level > get_trust(ch)) {
                    send_to_char("That ban was set by a higher power.\n", ch);
                    return;
                } else {
                    if (prev == null) {
                        ban_list = pban.next;
                    } else {
                        prev.next = pban.next;
                    }
                }
            }
        }

        pban = new BAN_DATA();
        pban.name = name;
        pban.level = get_trust(ch);

        /* set ban type */
        pban.ban_flags = type;

        if (prefix) {
            pban.ban_flags = SET_BIT(pban.ban_flags, BAN_PREFIX);
        }
        if (suffix) {
            pban.ban_flags = SET_BIT(pban.ban_flags, BAN_SUFFIX);
        }
        if (fPerm) {
            pban.ban_flags = SET_BIT(pban.ban_flags, BAN_PERMANENT);
        }

        pban.next = ban_list;
        ban_list = pban;
        save_bans();
        send_to_char(pban.name + " has been banned.\n", ch);
    }

    static void do_ban(CHAR_DATA ch, String argument) {
        ban_site(ch, argument, false);
    }

    static void do_permban(CHAR_DATA ch, String argument) {
        ban_site(ch, argument, true);
    }

    static void do_allow(CHAR_DATA ch, String argument) {
        BAN_DATA prev;
        BAN_DATA curr;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.isEmpty()) {
            send_to_char("Remove which site from the ban list?\n", ch);
            return;
        }

        prev = null;
        String argStr = arg.toString();
        for (curr = ban_list; curr != null; prev = curr, curr = curr.next) {
            if (!str_cmp(argStr, curr.name)) {
                if (curr.level > get_trust(ch)) {
                    send_to_char(
                            "You are not powerful enough to lift that ban.\n", ch);
                    return;
                }
                if (prev == null) {
                    ban_list = ban_list.next;
                } else {
                    prev.next = curr.next;
                }

                send_to_char("Ban on " + arg + " lifted.\n", ch);
                save_bans();
                return;
            }
        }

        send_to_char("Site is not banned.\n", ch);
    }
}
