package net.sf.nightworks;

import net.sf.nightworks.util.DikuTextFile;
import net.sf.nightworks.util.TextBuffer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Formatter;

import static net.sf.nightworks.Comm.page_to_char;
import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.DB.bug;
import static net.sf.nightworks.Handler.get_trust;
import static net.sf.nightworks.Handler.is_name;
import static net.sf.nightworks.Nightworks.ANGEL;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.CREATOR;
import static net.sf.nightworks.Nightworks.IS_IMMORTAL;
import static net.sf.nightworks.Nightworks.IS_NPC;
import static net.sf.nightworks.Nightworks.IS_SET;
import static net.sf.nightworks.Nightworks.IS_TRUSTED;
import static net.sf.nightworks.Nightworks.MAX_LEVEL;
import static net.sf.nightworks.Nightworks.NOTE_CHANGES;
import static net.sf.nightworks.Nightworks.NOTE_DATA;
import static net.sf.nightworks.Nightworks.NOTE_IDEA;
import static net.sf.nightworks.Nightworks.NOTE_INVALID;
import static net.sf.nightworks.Nightworks.NOTE_NEWS;
import static net.sf.nightworks.Nightworks.NOTE_NOTE;
import static net.sf.nightworks.Nightworks.NOTE_PENALTY;
import static net.sf.nightworks.Nightworks.PLR_CANINDUCT;
import static net.sf.nightworks.Nightworks.UMAX;
import static net.sf.nightworks.Nightworks.atoi;
import static net.sf.nightworks.Nightworks.current_time;
import static net.sf.nightworks.Nightworks.exit;
import static net.sf.nightworks.Nightworks.nw_config;
import static net.sf.nightworks.Nightworks.perror;
import static net.sf.nightworks.Tables.cabal_table;
import static net.sf.nightworks.util.TextUtils.is_number;
import static net.sf.nightworks.util.TextUtils.isSpace;
import static net.sf.nightworks.util.TextUtils.one_argument;
import static net.sf.nightworks.util.TextUtils.smash_tilde;
import static net.sf.nightworks.util.TextUtils.str_cmp;
import static net.sf.nightworks.util.TextUtils.str_prefix;


class Note {

    private static NOTE_DATA note_list = null;
    private static NOTE_DATA idea_list = null;
    private static NOTE_DATA penalty_list = null;
    private static NOTE_DATA news_list = null;
    private static NOTE_DATA changes_list = null;

    static int count_spool(CHAR_DATA ch, NOTE_DATA spool) {
        int count = 0;
        for (NOTE_DATA pnote = spool; pnote != null; pnote = pnote.next) {
            if (!hide_note(ch, pnote)) {
                count++;
            }
        }

        return count;
    }

    static void do_unread(CHAR_DATA ch, String argument) {
        int count;
        boolean found = false;

        if (IS_NPC(ch)) {
            return;
        }

        if ((count = count_spool(ch, news_list)) > 0) {
            found = true;
            send_to_char("There " + (count > 1 ? "are" : "is") + " " + count + " new news article" + (count > 1 ? "s" : "") + " waiting.\n", ch);
        }
        if ((count = count_spool(ch, changes_list)) > 0) {
            found = true;
            send_to_char("There " + (count > 1 ? "are" : "is") + " " + count + " change" + (count > 1 ? "s" : "") + " waiting to be read.\n", ch);
        }
        if ((count = count_spool(ch, note_list)) > 0) {
            found = true;
            send_to_char("You have " + count + " new note" + (count > 1 ? "s" : "") + " waiting.\n", ch);
        }
        if ((count = count_spool(ch, idea_list)) > 0) {
            found = true;
            send_to_char("You have " + count + " unread idea" + (count > 1 ? "s" : "") + " to peruse.\n", ch);
        }
        if (IS_TRUSTED(ch, ANGEL) && (count = count_spool(ch, penalty_list)) > 0) {
            found = true;
            send_to_char(count + " " + (count > 1 ? "penalties have" : "penalty has") + " been added.\n", ch);
        }

        if (!found && str_cmp(argument, "login")) {
            send_to_char("You have no unread notes.\n", ch);
        }
    }

    static void do_note(CHAR_DATA ch, String argument) {
        parse_note(ch, argument, NOTE_NOTE);
    }

    static void do_idea(CHAR_DATA ch, String argument) {
        parse_note(ch, argument, NOTE_IDEA);
    }

    static void do_penalty(CHAR_DATA ch, String argument) {
        parse_note(ch, argument, NOTE_PENALTY);
    }

    static void do_news(CHAR_DATA ch, String argument) {
        parse_note(ch, argument, NOTE_NEWS);
    }

    static void do_changes(CHAR_DATA ch, String argument) {
        parse_note(ch, argument, NOTE_CHANGES);
    }

    static void save_notes(int type) {
        String name;
        NOTE_DATA list;

        switch (type) {
            default:
                return;
            case NOTE_NOTE:
                name = nw_config.note_note_file;
                list = note_list;
                break;
            case NOTE_IDEA:
                name = nw_config.note_idea_file;
                list = idea_list;
                break;
            case NOTE_PENALTY:
                name = nw_config.note_penalty_file;
                list = penalty_list;
                break;
            case NOTE_NEWS:
                name = nw_config.note_news_file;
                list = news_list;
                break;
            case NOTE_CHANGES:
                name = nw_config.note_changes_file;
                list = changes_list;
                break;
        }
        try {
            BufferedWriter fw = new BufferedWriter(new FileWriter(name));
            Formatter fp = new Formatter(fw);
            try {
                for (NOTE_DATA pnote = list; pnote != null; pnote = pnote.next) {
                    prepareNote(fp, pnote);
                }
            } finally {
                fw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void prepareNote(Formatter fp, NOTE_DATA note) {
        fp.format("Sender  %s~\n", note.sender);
        fp.format("Date    %s~\n", note.date);
        fp.format("Stamp   %d\n", note.date_stamp);
        fp.format("To      %s~\n", note.to_list);
        fp.format("Subject %s~\n", note.subject);
        fp.format("Text\n%s~\n", note.text);
    }

    static void load_notes() {
        note_list = load_thread(nw_config.note_note_file, NOTE_NOTE, 14 * 24 * 60 * 60);
        idea_list = load_thread(nw_config.note_idea_file, NOTE_IDEA, 28 * 24 * 60 * 60);
        penalty_list = load_thread(nw_config.note_penalty_file, NOTE_PENALTY, 0);
        news_list = load_thread(nw_config.note_news_file, NOTE_NEWS, 0);
        changes_list = load_thread(nw_config.note_changes_file, NOTE_CHANGES, 0);
    }

    static NOTE_DATA load_thread(String name, int type, int free_time) {
        File file = new File(name);
        if (!file.exists()) {
            return null;
        }
        try {
            DikuTextFile fp = new DikuTextFile(name);
            NOTE_DATA pnotelast = null, list = null;
            for (; ; ) {
                char letter;
                do {
                    if (fp.feof()) {
                        break;
                    }
                    letter = fp.read();
                } while (isSpace(letter));
                if (fp.feof()) {
                    return list;
                }
                fp.ungetc();

                NOTE_DATA pnote = new NOTE_DATA();

                if (str_cmp(fp.fread_word(), "sender")) {
                    break;
                }
                pnote.sender = fp.fread_string();

                if (str_cmp(fp.fread_word(), "date")) {
                    break;
                }
                pnote.date = fp.fread_string();

                if (str_cmp(fp.fread_word(), "stamp")) {
                    break;
                }
                pnote.date_stamp = fp.fread_number();

                if (str_cmp(fp.fread_word(), "to")) {
                    break;
                }
                pnote.to_list = fp.fread_string();

                if (str_cmp(fp.fread_word(), "subject")) {
                    break;
                }
                pnote.subject = fp.fread_string();

                if (str_cmp(fp.fread_word(), "text")) {
                    break;
                }
                pnote.text = fp.fread_string();

                if (free_time != 0 && pnote.date_stamp < current_time - free_time) {
                    continue;
                }

                pnote.type = type;

                if (list == null) {
                    list = pnote;
                } else {
                    pnotelast.next = pnote;
                }

                pnotelast = pnote;
            }
            bug("Load_notes: bad key word.");
            exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            perror("login thread:" + name);
        }
        return null;
    }

    static void append_note(NOTE_DATA pnote) {
        String name;
        switch (pnote.type) {
            default:
                return;
            case NOTE_NOTE:
                name = nw_config.note_note_file;
                note_list = append_note(note_list, pnote);
                break;
            case NOTE_IDEA:
                name = nw_config.note_idea_file;
                idea_list = append_note(idea_list, pnote);
                break;
            case NOTE_PENALTY:
                name = nw_config.note_penalty_file;
                penalty_list = append_note(penalty_list, pnote);
                break;
            case NOTE_NEWS:
                name = nw_config.note_news_file;
                news_list = append_note(news_list, pnote);
                break;
            case NOTE_CHANGES:
                name = nw_config.note_changes_file;
                changes_list = append_note(changes_list, pnote);
                break;
        }

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(name, true));
            Formatter fp = new Formatter(bw);
            try {
                prepareNote(fp, pnote);
            } finally {
                bw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            perror(name);
        }
    }

    private static NOTE_DATA append_note(NOTE_DATA note_list, NOTE_DATA pnote) {
        if (note_list == null) {
            note_list = pnote;
        } else {
            NOTE_DATA last = note_list;
            while (last.next != null) {
                last = last.next;
            }
            last.next = pnote;
        }
        return note_list;
    }

    static boolean is_note_to(CHAR_DATA ch, NOTE_DATA pnote) {
        if (!str_cmp(ch.name, pnote.sender)) {
            return true;
        }

        if (!str_cmp("all", pnote.to_list)) {
            return true;
        }

        if (IS_IMMORTAL(ch) && is_name("immortal", pnote.to_list)) {
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (is_name(ch.name, pnote.to_list)) {
            return true;
        }

        return is_name(cabal_table[ch.cabal].short_name, pnote.to_list);

    }


    static void note_attach(CHAR_DATA ch, int type) {
        NOTE_DATA pnote;

        if (ch.pnote != null) {
            return;
        }

        pnote = new NOTE_DATA();

        pnote.next = null;
        pnote.sender = ch.name;
        pnote.date = "";
        pnote.to_list = "";
        pnote.subject = "";
        pnote.text = "";
        pnote.type = type;
        ch.pnote = pnote;
    }


    static void note_remove(CHAR_DATA ch, NOTE_DATA pnote, boolean delete) {

        if (!delete) {
            /* make a new list */
            StringBuilder to_new = new StringBuilder();
            StringBuilder to_one = new StringBuilder();
            String to_list = pnote.to_list;
            while (to_list != null && to_list.length() != 0) {
                to_one.setLength(0);
                to_list = one_argument(to_list, to_one);
                if (to_one.length() != 0 && str_cmp(ch.name, to_one.toString())) {
                    to_new.append(" ");
                    to_new.append(to_one);
                }
            }
            /* Just a simple recipient removal? */
            if (str_cmp(ch.name, pnote.sender) && to_new.length() != 0) {
                pnote.to_list = to_new.substring(1);
                return;
            }
        }
        /* nuke the whole note */
        switch (pnote.type) {
            default:
                return;
            case NOTE_NOTE:
                note_list = note_remove(note_list, pnote);
                break;
            case NOTE_IDEA:
                idea_list = note_remove(idea_list, pnote);
                break;
            case NOTE_PENALTY:
                penalty_list = note_remove(penalty_list, pnote);
                break;
            case NOTE_NEWS:
                news_list = note_remove(news_list, pnote);
                break;
            case NOTE_CHANGES:
                changes_list = note_remove(changes_list, pnote);
                break;
        }

        // Remove note from linked list.
        if (pnote.type != NOTE_INVALID) {
            bug("Note_remove: pnote not found.");
            return;
        }
        save_notes(pnote.type);
    }

    private static NOTE_DATA note_remove(NOTE_DATA note_list, NOTE_DATA pnote) {
        if (note_list == pnote) {
            note_list = note_list.next;
            pnote.next = null;
            pnote.type = NOTE_INVALID;
        } else {
            for (NOTE_DATA note = note_list.next, prev = note_list; note != null; prev = note, note = note.next) {
                if (note == pnote) {
                    prev.next = note.next;
                    note.next = null;
                    pnote.type = NOTE_INVALID;
                    break;
                }
            }
        }
        return note_list;
    }

    static boolean hide_note(CHAR_DATA ch, NOTE_DATA pnote) {
        int last_read;

        if (IS_NPC(ch)) {
            return true;
        }

        switch (pnote.type) {
            default:
                return true;
            case NOTE_NOTE:
                last_read = ch.pcdata.last_note;
                break;
            case NOTE_IDEA:
                last_read = ch.pcdata.last_idea;
                break;
            case NOTE_PENALTY:
                last_read = ch.pcdata.last_penalty;
                break;
            case NOTE_NEWS:
                last_read = ch.pcdata.last_news;
                break;
            case NOTE_CHANGES:
                last_read = ch.pcdata.last_changes;
                break;
        }

        if (pnote.date_stamp <= last_read) {
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (!str_cmp(ch.name, pnote.sender)) {
            return true;
        }

        return !is_note_to(ch, pnote);

    }

    static void update_read(CHAR_DATA ch, NOTE_DATA pnote) {
        int stamp;

        if (IS_NPC(ch)) {
            return;
        }

        stamp = pnote.date_stamp;

        switch (pnote.type) {
            default:
                return;
            case NOTE_NOTE:
                ch.pcdata.last_note = UMAX(ch.pcdata.last_note, stamp);
                break;
            case NOTE_IDEA:
                ch.pcdata.last_idea = UMAX(ch.pcdata.last_idea, stamp);
                break;
            case NOTE_PENALTY:
                ch.pcdata.last_penalty = UMAX(ch.pcdata.last_penalty, stamp);
                break;
            case NOTE_NEWS:
                ch.pcdata.last_news = UMAX(ch.pcdata.last_news, stamp);
                break;
            case NOTE_CHANGES:
                ch.pcdata.last_changes = UMAX(ch.pcdata.last_changes, stamp);
                break;
        }
    }

    static void parse_note(CHAR_DATA ch, String argument, int type) {
        if (IS_NPC(ch)) {
            return;
        }

        NOTE_DATA list;
        String list_name;
        switch (type) {
            default:
                return;
            case NOTE_NOTE:
                list = note_list;
                list_name = "notes";
                break;
            case NOTE_IDEA:
                list = idea_list;
                list_name = "ideas";
                break;
            case NOTE_PENALTY:
                list = penalty_list;
                list_name = "penalties";
                break;
            case NOTE_NEWS:
                list = news_list;
                list_name = "news";
                break;
            case NOTE_CHANGES:
                list = changes_list;
                list_name = "changes";
                break;
        }

        StringBuilder arg = new StringBuilder();
        argument = one_argument(argument, arg);
        argument = smash_tilde(argument);
        int vnum;
        int anum;
        if (arg.length() == 0 || !str_prefix(arg.toString(), "read")) {
            boolean fAll;

            if (!str_cmp(argument, "all")) {
                fAll = true;
                anum = 0;
            } else if (argument.length() == 0 || !str_prefix(argument, "next"))
                /* read next unread note */ {
                vnum = 0;
                for (NOTE_DATA pnote = list; pnote != null; pnote = pnote.next) {
                    if (!hide_note(ch, pnote)) {
                        TextBuffer buf = new TextBuffer();
                        buf.sprintf("[%3d] %s: %s\n%s\nTo: %s\n",
                                vnum,
                                pnote.sender,
                                pnote.subject,
                                pnote.date,
                                pnote.to_list);
                        send_to_char(buf, ch);
                        page_to_char(pnote.text, ch);
                        update_read(ch, pnote);
                        return;
                    } else if (is_note_to(ch, pnote)) {
                        vnum++;
                    }
                }
                TextBuffer buf = new TextBuffer();
                buf.sprintf("You have no unread %s.\n", list_name);
                send_to_char(buf, ch);
                return;
            } else if (is_number(argument)) {
                fAll = false;
                anum = atoi(argument);
            } else {
                send_to_char("Read which number?\n", ch);
                return;
            }

            vnum = 0;
            for (NOTE_DATA pnote = list; pnote != null; pnote = pnote.next) {
                if (is_note_to(ch, pnote) && (vnum++ == anum || fAll)) {
                    TextBuffer buf = new TextBuffer();
                    buf.sprintf("[%3d] %s: %s\n%s\nTo: %s\n",
                            vnum - 1,
                            pnote.sender,
                            pnote.subject,
                            pnote.date,
                            pnote.to_list
                    );
                    send_to_char(buf, ch);
                    page_to_char(pnote.text, ch);
                    update_read(ch, pnote);
                    return;
                }
            }

            TextBuffer buf = new TextBuffer();
            buf.sprintf("There aren't that many %s.\n", list_name);
            send_to_char(buf, ch);
            return;
        }

        if (!str_prefix(arg.toString(), "list")) {
            vnum = 0;
            TextBuffer buf = new TextBuffer();
            for (NOTE_DATA pnote = list; pnote != null; pnote = pnote.next) {
                if (is_note_to(ch, pnote)) {
                    buf.sprintf("[%3d%s] %s: %s\n",
                            vnum, hide_note(ch, pnote) ? " " : "N",
                            pnote.sender, pnote.subject);
                    send_to_char(buf, ch);
                    vnum++;
                }
            }
            return;
        }

        if (!str_prefix(arg.toString(), "remove")) {
            if (!is_number(argument)) {
                send_to_char("Note remove which number?\n", ch);
                return;
            }

            anum = atoi(argument);
            vnum = 0;
            for (NOTE_DATA pnote = list; pnote != null; pnote = pnote.next) {
                if (is_note_to(ch, pnote) && vnum++ == anum) {
                    note_remove(ch, pnote, false);
                    send_to_char("Ok.\n", ch);
                    return;
                }
            }

            TextBuffer buf = new TextBuffer();
            buf.sprintf("There aren't that many %s.", list_name);
            send_to_char(buf, ch);
            return;
        }

        if (!str_prefix(arg.toString(), "delete") && get_trust(ch) >= MAX_LEVEL - 1) {
            if (!is_number(argument)) {
                send_to_char("Note delete which number?\n", ch);
                return;
            }

            anum = atoi(argument);
            vnum = 0;
            for (NOTE_DATA pnote = list; pnote != null; pnote = pnote.next) {
                if (is_note_to(ch, pnote) && vnum++ == anum) {
                    note_remove(ch, pnote, true);
                    send_to_char("Ok.\n", ch);
                    return;
                }
            }

            TextBuffer buf = new TextBuffer();
            buf.sprintf("There aren't that many %s.", list_name);
            send_to_char(buf, ch);
            return;
        }

        if (!str_prefix(arg.toString(), "catchup")) {
            switch (type) {
                case NOTE_NOTE:
                    ch.pcdata.last_note = current_time;
                    break;
                case NOTE_IDEA:
                    ch.pcdata.last_idea = current_time;
                    break;
                case NOTE_PENALTY:
                    ch.pcdata.last_penalty = current_time;
                    break;
                case NOTE_NEWS:
                    ch.pcdata.last_news = current_time;
                    break;
                case NOTE_CHANGES:
                    ch.pcdata.last_changes = current_time;
                    break;
            }
            return;
        }

        /* below this point only certain people can edit notes */
        if ((type == NOTE_NEWS && !IS_TRUSTED(ch, ANGEL))
                || (type == NOTE_CHANGES && !IS_TRUSTED(ch, CREATOR))) {
            TextBuffer buf = new TextBuffer();
            buf.sprintf("You aren't high enough level to write %s.", list_name);
            return;
        }

        if (!str_cmp(arg.toString(), "+")) {
            note_attach(ch, type);
            if (ch.pnote.type != type) {
                send_to_char("You already have a different note in progress.\n", ch);
                return;
            }


            if (ch.pnote.text.length() + argument.length() >= 4096) {
                send_to_char("Note too long.\n", ch);
                return;
            }

            TextBuffer buffer = new TextBuffer();
            buffer.append(ch.pnote.text);
            buffer.append(argument);
            buffer.append("\n");
            ch.pnote.text = buffer.toString();
            send_to_char("Ok.\n", ch);
            return;
        }

        if (!str_cmp(arg.toString(), "-")) {
            int len;
            boolean found = false;

            note_attach(ch, type);
            if (ch.pnote.type != type) {
                send_to_char("You already have a different note in progress.\n", ch);
                return;
            }

            if (ch.pnote.text == null || ch.pnote.text.length() == 0) {
                send_to_char("No lines left to remove.\n", ch);
                return;
            }
            String buf = ch.pnote.text;
            for (len = buf.length(); len > 0; len--) {
                if (buf.charAt(len) == '\r') {
                    if (!found)  /* back it up */ {
                        if (len > 0) {
                            len--;
                        }
                        found = true;
                    } else /* found the second one */ {
                        ch.pnote.text = buf.substring(0, len);
                        return;
                    }
                }
            }
            ch.pnote.text = buf;
            return;
        }

        if (!str_prefix(arg.toString(), "subject")) {
            note_attach(ch, type);
            if (ch.pnote.type != type) {
                send_to_char("You already have a different note in progress.\n", ch);
                return;
            }

            ch.pnote.subject = argument;
            send_to_char("Ok.\n", ch);
            return;
        }

        if (!str_prefix(arg.toString(), "to")) {
            if (is_name(argument, "all") && !(IS_IMMORTAL(ch) || IS_SET(ch.act, PLR_CANINDUCT))) {
                send_to_char("Only immortals and cabal leaders can send notes to all.\n", ch);
                return;
            }
            note_attach(ch, type);
            if (ch.pnote.type != type) {
                send_to_char("You already have a different note in progress.\n", ch);
                return;
            }
            ch.pnote.to_list = argument;
            send_to_char("Ok.\n", ch);
            return;
        }

        if (!str_prefix(arg.toString(), "clear")) {
            if (ch.pnote != null) {
                ch.pnote = null;
            }

            send_to_char("Ok.\n", ch);
            return;
        }

        if (!str_prefix(arg.toString(), "show")) {
            if (ch.pnote == null) {
                send_to_char("You have no note in progress.\n", ch);
                return;
            }

            if (ch.pnote.type != type) {
                send_to_char("You aren't working on that kind of note.\n", ch);
                return;
            }

            TextBuffer buf = new TextBuffer();
            buf.sprintf("%s: %s\nTo: %s\n", ch.pnote.sender, ch.pnote.subject, ch.pnote.to_list);
            send_to_char(buf, ch);
            send_to_char(ch.pnote.text, ch);
            return;
        }

        if (!str_prefix(arg.toString(), "post") || !str_prefix(arg.toString(), "send")) {
            String strtime;

            if (ch.pnote == null) {
                send_to_char("You have no note in progress.\n", ch);
                return;
            }

            if (ch.pnote.type != type) {
                send_to_char("You aren't working on that kind of note.\n", ch);
                return;
            }

            if (!str_cmp(ch.pnote.to_list, "")) {
                send_to_char("You need to provide a recipient (name, all, or immortal).\n", ch);
                return;
            }

            if (!str_cmp(ch.pnote.subject, "")) {
                send_to_char("You need to provide a subject.\n", ch);
                return;
            }

            ch.pnote.next = null;
            strtime = new Date(current_time * 1000L).toString();
            ch.pnote.date = strtime;
            ch.pnote.date_stamp = current_time;

            append_note(ch.pnote);
            ch.pnote = null;
            return;
        }
        send_to_char("You can't do that.\n", ch);
    }
}
