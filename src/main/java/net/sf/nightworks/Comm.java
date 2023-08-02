package net.sf.nightworks;

import net.sf.nightworks.util.NotNull;
import net.sf.nightworks.util.TextBuffer;
import net.sf.nightworks.util.TextUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;

import static net.sf.nightworks.ActComm.cabal_area_check;
import static net.sf.nightworks.ActInfo.*;
import static net.sf.nightworks.ActSkill.*;
import static net.sf.nightworks.ActWiz.do_outfit;
import static net.sf.nightworks.ActWiz.wiznet;
import static net.sf.nightworks.Ban.check_ban;
import static net.sf.nightworks.Const.hometown_table;
import static net.sf.nightworks.DB.*;
import static net.sf.nightworks.Handler.*;
import static net.sf.nightworks.Interp.substitute_alias;
import static net.sf.nightworks.Nightworks.*;
import static net.sf.nightworks.Note.do_unread;
import static net.sf.nightworks.Recycle.free_char;
import static net.sf.nightworks.Save.load_char_obj;
import static net.sf.nightworks.Save.save_char_obj;
import static net.sf.nightworks.Skill.gsn_doppelganger;
import static net.sf.nightworks.Skill.gsn_recall;
import static net.sf.nightworks.Tables.ethos_table;
import static net.sf.nightworks.Telnet.*;
import static net.sf.nightworks.Update.update_handler;
import static net.sf.nightworks.util.Logger.logError;
import static net.sf.nightworks.util.TextUtils.*;

/**
 * This file contains all of the OS-dependent stuff:
 * startup, signals, BSD sockets for tcp/ip, i/o, timing.
 * <p/>
 * The data flow for input is:
 * Game_loop --. Read_from_descriptor --. Read
 * Game_loop --. Read_from_buffer
 * <p/>
 * The data flow for output is:
 * Game_loop --. Process_Output --. Write_to_descriptor . Write
 * <p/>
 * The OS-dependent functions are Read_from_descriptor and Write_to_descriptor.
 * -- Furey  26 Jan 1993
 */
class Comm {
    private static final byte[] echo_off_telnet_command = new byte[]{IAC, WILL, TELOPT_ECHO};
    private static final byte[] echo_on_telnet_command = new byte[]{IAC, WONT, TELOPT_ECHO};
    private static final byte[] go_ahead_telnet_command = new byte[]{IAC, GA};

    /*
     * Global variables.
     */
    private static DESCRIPTOR_DATA d_next;     /* Next descriptor in loop  */
    private static int boot_time;      /* time of boot */
    static boolean wizlock;        /* Game is wizlocked        */
    static boolean newlock;        /* Game is newlocked        */
    static boolean nw_down;  /* Shutdown         */
    static int nw_exit;  /* Exit Code */


    private static Selector selector = null;
    private static ServerSocketChannel serverChannel = null;

    public static void main(String[] args) {
        /*
         * Init time.
         */
        limit_time = boot_time = current_time = currentTimeSeconds();

        // Run the game.
        try {
            init_server_socket(nw_config.port_num);
            try {
                boot_db();
                log_string("Nightworks has launched on port " + nw_config.port_num + ".");
                nightworks_engine();
            } finally {
                serverChannel.close();
            }
        } catch (IOException e) {
            logError(e);
            System.exit(-1);
        }
        /*
         * That's all, folks.
         */

        log_area_popularity();
        log_string("Normal termination of game.");
        System.exit(nw_exit);
    }


    static void init_server_socket(int port) throws IOException {
        selector = SelectorProvider.provider().openSelector();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        var isa = new InetSocketAddress(port);
        serverChannel.socket().bind(isa);
    }


    static void send_help_greeting(DESCRIPTOR_DATA d) {
        write_to_buffer(d, help_greeting);
    }


    static void nightworks_engine() {
        current_time = currentTimeSeconds();
        try {
            /* Main loop */
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (!nw_down) {
                Set<SelectionKey> readyKeys;
                Iterator<SelectionKey> it;
                var nKeys = selector.selectNow();
                if (nKeys > 0) {
                    System.out.println("Selector returned " + nKeys + " ready for IO operations");
                    readyKeys = selector.selectedKeys();

                    // accept new connections
                    it = readyKeys.iterator();
                    while (it.hasNext()) {
                        var key = it.next();
                        if (key.isAcceptable()) {
                            it.remove();
                            var nextReady = (ServerSocketChannel) key.channel();
                            var channel = nextReady.accept();
                            init_descriptor(channel);
                        }
                    }

                    // process input from all connections
                    it = readyKeys.iterator();
                    while (it.hasNext()) {
                        var key = it.next();
                        if (key.isReadable()) {
                            it.remove();
                            var connectionOk = read_from_descriptor((DESCRIPTOR_DATA) key.attachment());
                            if (!connectionOk) {
                                key.channel().close();
                            }
                        }
                    }
                    assert (readyKeys.isEmpty());
                }

                //process input from all buffers of all chars
                for (DESCRIPTOR_DATA d = descriptor_list, d_next; d != null; d = d_next) {
                    d_next = d.next;
                    d.fcommand = false;
                    if (d.character != null && d.character.daze > 0) {
                        --d.character.daze;
                    }
                    if (d.character != null && d.character.wait > 0) {
                        --d.character.wait;
                        continue;
                    }
                    read_from_buffer(d);
                    if (!d.incomm.isEmpty()) {
                        d.fcommand = true;
                        if (d.character != null) {
                            stop_idling(d.character);
                        }
                        if (d.showstr_point != 0) {
                            show_string(d, d.incomm);
                        } else if (d.connected == CON_PLAYING) {
                            substitute_alias(d, d.incomm);
                        } else {
                            nanny(d, d.incomm);
                        }
                        d.incomm = "";
                    }
                }

                // Autonomous game motion.
                update_handler();

                // process output
                for (DESCRIPTOR_DATA d = descriptor_list, d_next; d != null; d = d_next) {
                    d_next = d.next;
                    var close = !d.descriptor.isConnected();
                    if (!close && (d.fcommand || !d.outbuf.isEmpty())) {
                        if (!process_output(d, true)) {
                            close = true;
                        }
                    }
                    if (close) {
                        if (d.character != null && d.character.level > 1) {
                            save_char_obj(d.character);
                        }
                        close_socket(d);
                    }

                }

                // TODO: Synchronize to a clock.
                current_time = currentTimeSeconds();
                try {
                    Thread.sleep(1000 / PULSE_PER_SECOND);
                } catch (InterruptedException e) {
                    logError(e);
                }
            }
        } catch (IOException e) {
            logError(e);
        }
    }

    static void init_descriptor(SocketChannel channel) throws IOException {

        channel.configureBlocking(false);
        var readKey = channel.register(selector, SelectionKey.OP_READ);

        //Cons a new descriptor.
        var dnew = new DESCRIPTOR_DATA();

        dnew.descriptor = channel;
        dnew.connected = CON_GET_NAME;
        dnew.showstr_head = null;
        dnew.showstr_point = 0;

        dnew.host = channel.socket().getInetAddress().getHostAddress();
//TODO: if (nw_config.dns_enabled!=0) {
//          from = gethostbyaddr((String ) &sock.sin_addr, sizeof(sock.sin_addr), AF_INET );
//      }

        /*
         * Swiftest: I added the following to ban sites.  I don't
         * endorse banning of sites, but Copper has few descriptors now
         * and some people from certain sites keep abusing access by
         * using automated 'autodialers' and leaving connections hanging.
         *
         * Furey: added suffix check by request of Nickel of HiddenWorlds.
         */
        if (check_ban(dnew.host, BAN_ALL)) {
            write_to_descriptor(channel, "Your site has been banned from this mud.\n");
            channel.close();
            return;
        }

        // Init descriptor data.
        readKey.attach(dnew);
        dnew.next = descriptor_list;
        descriptor_list = dnew;

        // Send the greeting.
        send_help_greeting(dnew);
    }


    static void close_socket(DESCRIPTOR_DATA dclose) {
        CHAR_DATA ch;

        if (!dclose.outbuf.isEmpty()) {
            process_output(dclose, false);
        }

        if (dclose.snoop_by != null) {
            write_to_buffer(dclose.snoop_by, "Your victim has left the game.\n");
        }

        for (var d = descriptor_list; d != null; d = d.next) {
            if (d.snoop_by == dclose) {
                d.snoop_by = null;
            }
        }

        if ((ch = dclose.character) != null) {
            log_string("Closing link to " + ch.name + ".");

            if (ch.pet != null && (ch.pet.in_room == null || ch.pet.in_room == get_room_index(ROOM_VNUM_LIMBO))) {
                char_to_room(ch.pet, get_room_index(ROOM_VNUM_LIMBO));
                extract_char(ch.pet, true);
            }

            if (dclose.connected == CON_PLAYING) {
                if (!IS_IMMORTAL(ch)) {
                    act("$n has lost $s link.", ch, null, null, TO_ROOM);
                }
                wiznet("Net death has claimed $N.", ch, null, WIZ_LINKS, 0, 0);
                ch.desc = null;
            } else {
                free_char(dclose.character);
            }
        }

        if (d_next == dclose) {
            d_next = d_next.next;
        }

        if (dclose == descriptor_list) {
            descriptor_list = descriptor_list.next;
        } else {
            DESCRIPTOR_DATA d;

            for (d = descriptor_list; d != null && d.next != dclose; d = d.next) {
            }
            if (d != null) {
                d.next = dclose.next;
            } else {
                bug("Close_socket: dclose not found.");
            }
        }

        try {
            dclose.descriptor.close();
        } catch (IOException e) {
            logError(e);
        }
    }


    private static final int BUFSIZE = 1024;
    private static final ByteBuffer byteBuffer = ByteBuffer.allocate(BUFSIZE);

    static boolean read_from_descriptor(DESCRIPTOR_DATA d) {
        try {
            byteBuffer.clear();
            var nbytes = d.descriptor.read(byteBuffer);
            if (nbytes == -1) {
                return false;
            }
            if (nbytes == 0) {
                return true;
            }
            // Check for overflow
            if (d.inbuf.length() >= MAX_INPUT_LENGTH) {
                log_string(d.host + " input overflow!");
                write_to_descriptor(d.descriptor, "\n*** PUT A LID ON IT!!! ***\n");
                return true;
            }
            byteBuffer.flip();
            while (byteBuffer.remaining() > 0) {
                var c = byteBuffer.get();
                if (c == IAC) { //Interpret As Command
                    if (byteBuffer.remaining() == 0) {
                        break;
                    }
                    c = byteBuffer.get();
                    if (c != IAC) { // this is a real command
                        if (byteBuffer.remaining() == 0) {
                            break;
                        }
                        c = byteBuffer.get();
                        if (c == WILL) { // if command is WILL echo DON'T
                            write_to_buffer(d, new byte[]{IAC, WONT, c}); // we do not want any command (do not support)
                        } else if (c == DO) {// if command is DO echo WON'T
                            write_to_buffer(d, new byte[]{IAC, DONT, c});
                        }
                        continue;
                    }
                }
                if (c == '\r' || c == '\b') {
                    continue;
                }
                d.inbuf.append((char) c);
            }
        } catch (IOException e) {
            logError(e);
        }
        return true;
    }

    /*
     * Transfer one line from input buffer to input line.
     */

    static void read_from_buffer(DESCRIPTOR_DATA d) {
        /*
         * Hold horses if pending command already.
         */
        if (d.incomm.length() != 0) {
            return;
        }

        /*
         * Look for at least one new line.
         */
        var lineEnd = d.inbuf.indexOf("\n");
        if (lineEnd == -1) {
            return;
        }
        d.incomm = d.inbuf.substring(0, lineEnd);
        d.inbuf.delete(0, lineEnd + 1);

        if (d.incomm.length() == 0) {
            d.incomm = " ";
        }
        if (d.incomm.length() > MAX_INPUT_LENGTH) {
            d.incomm = d.incomm.substring(0, MAX_INPUT_LENGTH);
            write_to_descriptor(d.descriptor, "Line too long.\n");
        }

        /*
         * Deal with bozos with #repeat 1000 ...
         */
        if (d.incomm.length() > 1 || d.incomm.charAt(0) == '!') {
            if (d.incomm.charAt(0) != '!' && !d.incomm.equals(d.inlast)) {
                d.repeat = 0;
            } else {
                if (++d.repeat >= 25)    /* corrected by chronos */ {
                    log_string(d.host + " input spamming!");
                    if (d.character != null) {
                        var buf = new TextBuffer();
                        buf.sprintf("SPAM SPAM SPAM %s spamming, and OUT!", d.character.name);
                        wiznet(buf, d.character, null, WIZ_SPAM, 0, get_trust(d.character));
                        buf.sprintf("[%s]'s  Inlast:[%s] Incomm:[%s]!", d.character.name, d.inlast, d.incomm);
                        wiznet(buf, d.character, null, WIZ_SPAM, 0, get_trust(d.character));

                        d.repeat = 0;

                        write_to_descriptor(d.descriptor, "\n*** PUT A LID ON IT!!! ***\n");
                        d.incomm = "quit";
                        close_socket(d);
                        return;
                    }
                }
            }
        }

        /*
         * Do '!' substitution.
         */
        if (d.incomm.charAt(0) == '!') {
            d.incomm = d.inlast;
        } else {
            d.inlast = d.incomm;
        }
    }

    /*
     * Low level output function.
     */

    /*
     * Some specials added by KIO
     */

    static boolean process_output(DESCRIPTOR_DATA d, boolean fPrompt) {
        // Bust a prompt.
        if (!nw_down && d.showstr_point != 0) {
            write_to_buffer(d, "\r[Hit Return to continue]\n");
        } else if (fPrompt && !nw_down && d.connected == CON_PLAYING) {
            CHAR_DATA ch;
            CHAR_DATA victim;

            ch = d.character;

            /* battle prompt */
            if ((victim = ch.fighting) != null && can_see(ch, victim)) {
                int percent;

                if (victim.max_hit > 0) {
                    percent = victim.hit * 100 / victim.max_hit;
                } else {
                    percent = -1;
                }

                String wound;
                if (percent >= 100) {
                    wound = "is in perfect health.";
                } else if (percent >= 90) {
                    wound = "has a few scratches.";
                } else if (percent >= 75) {
                    wound = "has some small but disgusting cuts.";
                } else if (percent >= 50) {
                    wound = "is covered with bleeding wounds.";
                } else if (percent >= 30) {
                    wound = "is gushing blood.";
                } else if (percent >= 15) {
                    wound = "is writhing in agony.";
                } else if (percent >= 0) {
                    wound = "is convulsing on the ground.";
                } else {
                    wound = "is nearly dead.";
                }
                write_to_buffer(d, DB.capitalize(IS_NPC(victim) ? victim.short_descr : victim.name) + " " + wound + "\n");
            }


            ch = d.original != null ? d.original : d.character;
            if (!IS_SET(ch.comm, COMM_COMPACT)) {
                write_to_buffer(d, "\n");
            }


            if (IS_SET(ch.comm, COMM_PROMPT)) {
                bust_a_prompt(d.character);
            }

            if (IS_SET(ch.comm, COMM_TELNET_GA)) {
                write_to_buffer(d, go_ahead_telnet_command);
            }
        }

        // Snoop-o-rama.
        if (d.snoop_by != null) {
            if (d.character != null) {
                write_to_buffer(d.snoop_by, d.character.name);
            }
            write_to_buffer(d.snoop_by, "> ");
            write_to_buffer(d.snoop_by, d.outbuf);
        }
        for (var o : d.outbuf) {
            if (!write_to_descriptor(d.descriptor, o)) {
                break;
            }
        }
        d.outbuf.clear();//todo:
        return d.outbuf.isEmpty();
    }


    static final char[] dir_name_char = {'N', 'E', 'S', 'W', 'U', 'D'};

    /**
     * Bust a prompt (player settable prompt) coded by Morgenes for Aldara Mud
     */
    static void bust_a_prompt(@NotNull CHAR_DATA ch) {
        var str = ch.prompt;

        if (str == null || str.length() == 0) {
            send_to_char("<" + ch.hit + "hp " + ch.mana + "m " + ch.move + "mv> " + ch.prefix, ch);
            return;
        }


        var buf = new StringBuilder();
        String i;
        for (var p = 0; p < str.length(); p++) {
            var c = str.charAt(p);
            if (c != '%') {
                buf.append(c);
                continue;
            }
            p++;
            c = p < str.length() ? str.charAt(p) : 0;
            switch (c) {
                default -> i = " ";
                case 'e' -> {
                    var doors = new StringBuilder();
                    for (var door = 0; door < 6; door++) {
                        var pexit = ch.in_room.exit[door];
                        if (pexit != null && pexit.to_room != null && (can_see_room(ch, pexit.to_room) || (IS_AFFECTED(ch, AFF_INFRARED) && !IS_AFFECTED(ch, AFF_BLIND))) && !IS_SET(pexit.exit_info, EX_CLOSED)) {
                            doors.append(dir_name_char[door]);
                        }
                    }
                    i = doors.isEmpty() ? "none" : doors.toString();
                }
                case 'c' -> i = "\n";

/** added from here by KIO   **/
                case 'n' -> i = ch.name;
                case 'S' -> i = (ch.sex == SEX_MALE ? "Male" : (ch.sex == 0 ? "None" : "Female"));
                case 'y' -> {
                    if (ch.hit >= 0) {
                        i = ((100 * ch.hit) / UMAX(1, ch.max_hit)) + "%%";
                    } else {
                        i = "BAD!!";
                    }
                }
                case 'o' -> {
                    var victim = ch.fighting;
                    if (victim != null) {
                        if (victim.hit >= 0) {
                            i = "{Y" + ((100 * victim.hit) / UMAX(1, victim.max_hit)) + "%{x";
                        } else {
                            i = "{RBAD!!{x";
                        }
                    } else {
                        i = "None";
                    }
                }
/***** FInished ****/

                /* Thanx to zihni:  T for time */
                case 'T' -> i = ((time_info.hour % 12 == 0) ? 12 : time_info.hour % 12) + " " + (time_info.hour >= 12 ? "pm" : "am");
                case 'h' -> i = String.valueOf(ch.hit);
                case 'H' -> i = String.valueOf(ch.max_hit);
                case 'm' -> i = String.valueOf(ch.mana);
                case 'M' -> i = String.valueOf(ch.max_mana);
                case 'v' -> i = String.valueOf(ch.move);
                case 'V' -> i = String.valueOf(ch.max_move);
                case 'x' -> i = String.valueOf(ch.exp);
                case 'X' -> i = "" + (IS_NPC(ch) ? 0 : exp_to_level(ch, ch.pcdata.points));
                case 'g' -> i = String.valueOf(ch.gold);
                case 's' -> i = String.valueOf(ch.silver);
                case 'a' -> i = IS_GOOD(ch) ? "good" : IS_EVIL(ch) ? "evil" : "neutral";
                case 'r' -> {
                    if (ch.in_room != null) {
                        i = ((!IS_NPC(ch) && IS_SET(ch.act, PLR_HOLYLIGHT)) || (!IS_AFFECTED(ch, AFF_BLIND) && !room_is_dark(ch))) ? ch.in_room.name : "darkness";
                    } else {
                        i = " ";
                    }
                }
                case 'R' -> {
                    if (IS_IMMORTAL(ch) && ch.in_room != null) {
                        i = String.valueOf(ch.in_room.vnum);
                    } else {
                        i = " ";
                    }
                }
                case 'z' -> {
                    if (IS_IMMORTAL(ch) && ch.in_room != null) {
                        i = ch.in_room.area.name;
                    } else {
                        i = " ";
                    }
                }
                case '%' -> i = "%%";
            }
            buf.append(i);
        }
        write_to_buffer(ch.desc, buf);

        if (ch.prefix.length() != 0) {
            write_to_buffer(ch.desc, ch.prefix);
        }
    }

    /*
     * Append onto an output buffer.
     */

    private static void write_to_buffer(@NotNull DESCRIPTOR_DATA snoop_by, @NotNull ArrayList outbuf) {
        snoop_by.outbuf.addAll(outbuf);
    }

    static void write_to_buffer(@NotNull DESCRIPTOR_DATA d, byte @NotNull [] data) {
        d.outbuf.add(data);
    }

    static void write_to_buffer(@NotNull DESCRIPTOR_DATA d, @NotNull CharSequence txt) {
        // Initial \n if needed.
        if (d.outbuf.isEmpty() && !d.fcommand) {
            d.outbuf.add("\n");
        }

        // Expand the buffer as needed.
        if (d.outbuf.size() > 100) {
            bug("Buffer overflow. Closing.");
            close_socket(d);
            return;
        }
        d.outbuf.add(txt.toString());//create a copy if textbuffer
    }

    /*
     * Lowest level output function.
     * Write a block of text to the file descriptor.
     * If this gives errors on very long blocks (like 'ofind all'),
     *   try lowering the max block size.
     */


    private static final StringBuilder outBuf = new StringBuilder(1000);

    static boolean write_to_descriptor(@NotNull SocketChannel desc, @NotNull Object data) {
        try {
            ByteBuffer buf;
            if (data instanceof CharSequence) {
                var text = data.toString();
                for (var i = 0; i < text.length(); i++) {
                    var c = text.charAt(i);
                    if (c == '{') {
                        i++;
                        if (i < text.length()) {
                            var c2 = text.charAt(i);
                            if (c2 == '{') {
                                outBuf.append("{");
                            } else {
                                var colorMask = Telnet.toColor(c2);
                                outBuf.append(colorMask);
                            }
                        }
                    } else if (c == '\n') {
                        outBuf.append("\r\n");
                    } else {
                        outBuf.append(c);
                    }
                }
                buf = ByteBuffer.wrap(outBuf.toString().getBytes());
                outBuf.setLength(0);
            } else {
                buf = ByteBuffer.wrap((byte[]) data);
            }
            desc.write(buf);
//            desc.socket().getOutputStream().flush();
        } catch (IOException e) {
            perror("Write_to_descriptor:" + e.getMessage());
            return false;
        }
        return true;
    }


    static int search_sockets(@NotNull DESCRIPTOR_DATA inp) {
        DESCRIPTOR_DATA d;

        if (IS_IMMORTAL(inp.character)) {
            return 0;
        }

        for (d = descriptor_list; d != null; d = d.next) {
            if (inp.host.equals(d.host)) {
                if (d.character != null && inp.character != null) {
                    if (inp.character.name.equals(d.character.name)) {
                        continue;
                    }
                }
                return 1;
            }
        }
        return 0;
    }


    static boolean check_name_connected(DESCRIPTOR_DATA inp, String argument) {
        for (var d = descriptor_list; d != null; d = d.next) {
            if (inp != d && d.character != null && inp.character != null) {
                if (argument.equals(d.character.name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * Deal with sockets that haven't logged in yet.
     */

    static void nanny(DESCRIPTOR_DATA d, String argument) {
        boolean fOld;
        argument = argument.trim();

        var ch = d.character;
        var arg = new StringBuilder();
        Clazz iClass;

        switch (d.connected) {
            default:
                bug("Nanny: bad d.connected %d.", d.connected);
                close_socket(d);
                return;

            case CON_GET_NAME:
                if (argument.length() == 0) {
                    close_socket(d);
                    return;
                }
                argument = DB.capitalize(argument);
                if (!check_parse_name(argument)) {
                    write_to_buffer(d, "Illegal name, try another.\nName: ");
                    return;
                }

                fOld = load_char_obj(d, argument);
                ch = d.character;

                if (get_trust(ch) < LEVEL_IMMORTAL) {
                    if (check_ban(d.host, BAN_PLAYER)) {
                        write_to_buffer(d, "Your site has been banned for players.\n");
                        close_socket(d);
                        return;
                    }
                }


                if (IS_SET(ch.act, PLR_DENY)) {
                    log_string("Denying access to " + argument + "@" + d.host + ".");
                    write_to_buffer(d, "You are denied access.\n");
                    close_socket(d);
                    return;
                }

                if (check_reconnect(d, argument, false)) {
                    fOld = true;
                } else {
                    if (wizlock && !IS_HERO(ch)) {
                        write_to_buffer(d, "The game is wizlocked.\n");
                        close_socket(d);
                        return;
                    }

                    if (!IS_IMMORTAL(ch) && !IS_SET(ch.act, PLR_CANINDUCT)) {
                        if (iNumPlayers >= max_oldies && fOld) {
                            var buf = "\nThere are currently " + iNumPlayers + " players mudding out of a maximum of " + max_oldies + ".\n" + "Please try again soon.\n";
                            write_to_buffer(d, buf);
                            close_socket(d);
                            return;
                        }

                        if (iNumPlayers >= max_newbies && !fOld) {
                            var buf = "\nThere are currently " + iNumPlayers + " players mudding." + "New player creation is limited to \n" + "when there are less than " + max_newbies + " players. Please try again soon.\n";
                            write_to_buffer(d, buf);
                            close_socket(d);
                            return;
                        }
                    }

                }

                if (fOld) {
                    /* Old player */
                    write_to_buffer(d, "Password: ");
                    write_to_buffer(d, echo_off_telnet_command);
                    d.connected = CON_GET_OLD_PASSWORD;
                } else {
                    /* New player */
                    if (newlock) {
                        write_to_buffer(d, "The game is newlocked.\n");
                        close_socket(d);
                        return;
                    }

                    if (check_ban(d.host, BAN_NEWBIES)) {
                        write_to_buffer(d, "New players are not allowed from your site.\n");
                        close_socket(d);
                        return;
                    }

                    if (check_name_connected(d, argument)) {
                        write_to_buffer(d, "That player is already playing, try another.\nName: ");
                        free_char(d.character);
                        d.character = null;
                        d.connected = CON_GET_NAME;
                        return;
                    }
                    do_help(ch, "NAME");
                    d.connected = CON_CONFIRM_NEW_NAME;
                }
                return;
            case CON_GET_OLD_PASSWORD:
                write_to_buffer(d, "\n");

                if (!crypt(argument, ch.name).equals(ch.pcdata.pwd)) {
                    write_to_buffer(d, "Wrong password.\n");
                    log_string("Wrong password by " + ch.name + "@" + d.host);
                    if (ch.endur == 2) {
                        close_socket(d);
                    } else {
                        write_to_buffer(d, "Password: ");
                        write_to_buffer(d, echo_off_telnet_command);
                        d.connected = CON_GET_OLD_PASSWORD;
                        ch.endur++;
                    }
                    return;
                }


                if (ch.pcdata.pwd.length() == 0) {
                    write_to_buffer(d, "Warning! null password!\n");
                    write_to_buffer(d, "Please report old password with bug.\n");
                    write_to_buffer(d, "Type 'password null <new password>' to fix.\n");
                }


                write_to_buffer(d, echo_on_telnet_command);

                if (check_reconnect(d, ch.name, true)) {
                    return;
                }

                if (check_playing(d, ch.name)) {
                    return;
                }

                /* Count objects in loaded player file */
                var obj_count = 0;
                for (var obj = ch.carrying; obj != null; obj = obj.next_content) {
                    obj_count += get_obj_realnumber(obj);
                }

                var buf = ch.name;

                free_char(ch);
                fOld = load_char_obj(d, buf);
                ch = d.character;


                if (!fOld) {
                    write_to_buffer(d, "Please login again to create a new character.\n");
                    close_socket(d);
                    return;
                }

                /* Count objects in refreshed player file */
                var obj_count2 = 0;
                for (var obj = ch.carrying; obj != null; obj = obj.next_content) {
                    obj_count2 += get_obj_realnumber(obj);
                }

                log_string(ch.name + "@" + d.host + " has connected.");

                if (IS_HERO(ch)) {
                    do_help(ch, "imotd");
                    d.connected = CON_READ_IMOTD;
                } else {
                    do_help(ch, "motd");
                    d.connected = CON_READ_MOTD;
                }

                /* This player tried to use the clone cheat --
                 * Log in once, connect a second time and enter only name,
                 * drop all and quit with first character, finish login with second.
                 * This clones the player's inventory.
                 */
                if (obj_count != obj_count2) {
                    log_string(ch.name + "@" + d.host + " tried to use the clone cheat.");
                    send_to_char("The gods frown upon your actions.\n", ch);
                }
                break;
            /* RT code for breaking link */

            case CON_BREAK_CONNECT:
                switch (argument.length() == 0 ? 'x' : argument.charAt(0)) {
                    case 'y', 'Y' -> {
                        for (var d_old = descriptor_list; d_old != null; d_old = d_next) {
                            d_next = d_old.next;
                            if (d_old == d || d_old.character == null) {
                                continue;
                            }

                            if (str_cmp(ch.name, d_old.character.name)) {
                                continue;
                            }
                            close_socket(d_old);
                        }
                        if (check_reconnect(d, ch.name, true)) {
                            return;
                        }
                        write_to_buffer(d, "Reconnect attempt failed.\nName: ");
                        if (d.character != null) {
                            free_char(d.character);
                            d.character = null;
                        }
                        d.connected = CON_GET_NAME;
                    }
                    case 'n', 'N' -> {
                        write_to_buffer(d, "Name: ");
                        if (d.character != null) {
                            free_char(d.character);
                            d.character = null;
                        }
                        d.connected = CON_GET_NAME;
                    }
                    default -> write_to_buffer(d, "Please type Y or N? ");
                }
                break;

            case CON_CONFIRM_NEW_NAME:
                switch (argument.length() == 0 ? 'x' : argument.charAt(0)) {
                    case 'y', 'Y' -> {
                        write_to_buffer(d, "New character.\nGive me a password for " + ch.name + ": ");
                        write_to_buffer(d, echo_off_telnet_command);
                        d.connected = CON_GET_NEW_PASSWORD;
                    }
                    case 'n', 'N' -> {
                        write_to_buffer(d, "Ok, what IS it, then? ");
                        free_char(d.character);
                        d.character = null;
                        d.connected = CON_GET_NAME;
                    }
                    default -> write_to_buffer(d, "Please type Yes or No? ");
                }
                break;

            case CON_GET_NEW_PASSWORD:
                write_to_buffer(d, "\n");

                if (argument.length() < 5) {
                    write_to_buffer(d, "Password must be at least five characters long.\nPassword: ");
                    return;
                }

                var pwdnew = crypt(argument, ch.name);
                if (pwdnew.indexOf('~') != -1) {
                    write_to_buffer(d, "New password not acceptable, try again.\nPassword: ");
                    return;
                }

                ch.pcdata.pwd = pwdnew;
                write_to_buffer(d, "Please retype password: ");
                d.connected = CON_CONFIRM_NEW_PASSWORD;
                break;

            case CON_CONFIRM_NEW_PASSWORD:
                write_to_buffer(d, "\n");

                if (!crypt(argument, ch.name).equals(ch.pcdata.pwd)) {
                    write_to_buffer(d, "Passwords don't match.\nRetype password: ");
                    d.connected = CON_GET_NEW_PASSWORD;
                    return;
                }

                write_to_buffer(d, echo_on_telnet_command);
                write_to_buffer(d, "The Nightworks MUD is home to " + (Race.getNumberOfPCRaces() - 1) + " different races with brief descriptions below:\n");
                do_help(ch, "RACETABLE");
                d.connected = CON_GET_NEW_RACE;
                break;

            case CON_REMORTING:
                ch.act = SET_BIT(ch.act, PLR_CANREMORT);
                ch.act = SET_BIT(ch.act, PLR_REMORTED);
                write_to_buffer(d, "As you know, the Nightworks MUD is home to " + (Race.getNumberOfPCRaces() - 1) + " different races...\n");
                do_help(ch, "RACETABLE");
                d.connected = CON_GET_NEW_RACE;
                break;

            case CON_GET_NEW_RACE:
                arg.setLength(0);
                one_argument(argument, arg);

                if (!str_cmp(arg.toString(), "help")) {
                    arg.setLength(0);
                    argument = one_argument(argument, arg);
                    if (argument.length() == 0) {
                        write_to_buffer(d, "The Nightworks MUD is home to " + (Race.getNumberOfPCRaces() - 1) + " different races with brief descriptions below:\n");
                        do_help(ch, "RACETABLE");
                        break;
                    } else {
                        do_help(ch, argument);
                        write_to_buffer(d, "What is your race? (help for more information) ");
                    }
                    break;
                }

                var race = Race.lookupRaceByPrefix(argument);

                if (race == null || race.pcRace == null) {
                    write_to_buffer(d, "That is not a valid race.\n");
                    write_to_buffer(d, "The following races are available:\n  ");
                    var races = Race.listRaces();
                    for (var i = 0; i < races.size(); i++) {
                        race = races.get(i);
                        if (race.pcRace == null) {
                            break;
                        }
                        if (i == 9 || i == 15) {
                            write_to_buffer(d, "\n  ");
                        }
                        write_to_buffer(d, "(");
                        write_to_buffer(d, race.name);
                        write_to_buffer(d, ") ");
                    }
                    write_to_buffer(d, "\n");
                    write_to_buffer(d, "What is your race? (help for more information) ");
                    break;
                }

                SET_ORG_RACE(ch, race);
                ch.race = race;
                for (var i = 0; i < MAX_STATS; i++) {
                    ch.mod_stat[i] = 0;
                }

                /* Add race stat modifiers
      for (i = 0; i < MAX_STATS; i++)
          ch.mod_stat[i] += pc_race_table[race].stats[i];    */

                /* Add race modifiers */
                var pc_race = race.pcRace;
                ch.max_hit += pc_race.hp_bonus;
                ch.hit = ch.max_hit;
                ch.max_mana += pc_race.mana_bonus;
                ch.mana = ch.max_mana;
                ch.practice += pc_race.prac_bonus;

                ch.affected_by = ch.affected_by | race.aff;
                ch.imm_flags = ch.imm_flags | race.imm;
                ch.res_flags = ch.res_flags | race.res;
                ch.vuln_flags = ch.vuln_flags | race.vuln;
                ch.form = race.form;
                ch.parts = race.parts;

                /* add skills */
                for (var skill : pc_race.skills) {
                    var sn = skill.ordinal();
                    ch.pcdata.learned[sn] = 100;
                }
                /* add cost */

                ch.pcdata.points = pc_race.points;

                ch.size = pc_race.size;

                write_to_buffer(d, "What is your sex (M/F)? ");
                d.connected = CON_GET_NEW_SEX;
                break;


            case CON_GET_NEW_SEX:
                switch (argument.length() == 0 ? 'x' : argument.charAt(0)) {
                    case 'm', 'M' -> {
                        ch.sex = SEX_MALE;
                        ch.pcdata.true_sex = SEX_MALE;
                    }
                    case 'f', 'F' -> {
                        ch.sex = SEX_FEMALE;
                        ch.pcdata.true_sex = SEX_FEMALE;
                    }
                    default -> {
                        write_to_buffer(d, "That's not a sex.\nWhat IS your sex? ");
                        return;
                    }
                }

                do_help(ch, "class help");

                var bbuf = new StringBuilder("Select a class:\n[ ");
                var buf1 = new StringBuilder("(Continuing:)  ");
                var classes = Clazz.getClasses();
                for (var i = 0; i < classes.size(); i++) {
                    var c = classes.get(i);
                    if (class_ok(ch, c)) {
                        if (i < 7) {
                            bbuf.append(c.name).append(" ");
                        } else {
                            buf1.append(c.name).append(" ");
                        }
                    }
                }
                bbuf.append("\n ");
                buf1.append("]:\n ");
                write_to_buffer(d, bbuf);
                write_to_buffer(d, buf1);
                write_to_buffer(d, "What is your class (help for more information)? ");
                d.connected = CON_GET_NEW_CLASS;
                break;

            case CON_GET_NEW_CLASS:
                iClass = Clazz.lookupClassByPrefix(argument);
                arg.setLength(0);
                argument = one_argument(argument, arg);

                if (!str_cmp(arg.toString(), "help")) {
                    if (argument.length() == 0) {
                        do_help(ch, "class help");
                    } else {
                        do_help(ch, argument);
                    }
                    write_to_buffer(d, "What is your class (help for more information)? ");
                    return;
                }
                if (iClass == null) {
                    write_to_buffer(d, "That's not a class.\nWhat IS your class? ");
                    return;
                }

                if (!class_ok(ch, iClass)) {
                    write_to_buffer(d, "That class is not available for your race or sex.\nChoose again: ");
                    return;
                }
                ch.clazz = iClass;
                ch.pcdata.points += iClass.points;
                write_to_buffer(d, "You are now " + iClass.name + ".\n");

                for (var i = 0; i < MAX_STATS; i++) {
                    ch.perm_stat[i] = number_range(10, (20 + ORG_RACE(ch).pcRace.stats[i] + ch.clazz.stats[i]));
                    ch.perm_stat[i] = UMIN(25, ch.perm_stat[i]);
                }
            {
                var statsf = new Formatter();
                statsf.format("Str:%s  Int:%s  Wis:%s  Dex:%s  Con:%s Cha:%s \n Accept (Y/N)? ", get_stat_alias(ch, STAT_STR), get_stat_alias(ch, STAT_INT), get_stat_alias(ch, STAT_WIS), get_stat_alias(ch, STAT_DEX), get_stat_alias(ch, STAT_CON), get_stat_alias(ch, STAT_CHA));


                do_help(ch, "stats");
                write_to_buffer(d, "\nNow rolling for your stats (10-20+).\n");
                write_to_buffer(d, "You don't get many trains, so choose well.\n");
                write_to_buffer(d, statsf.toString());
                d.connected = CON_ACCEPT_STATS;
            }
            break;

            case CON_ACCEPT_STATS:
                switch (argument.length() == 0 ? 'x' : argument.charAt(0)) {
                    case 'H', 'h', '?' -> do_help(ch, "stats");
                    case 'y', 'Y' -> {
                        for (var i = 0; i < MAX_STATS; i++) {
                            ch.mod_stat[i] = 0;
                        }
                        write_to_buffer(d, "\n");
                        if (align_restrict(ch) == 0) {
                            write_to_buffer(d, "You may be good, neutral, or evil.\n");
                            write_to_buffer(d, "Which alignment (G/N/E)? ");
                            d.connected = CON_GET_ALIGNMENT;
                        } else {
                            write_to_buffer(d, "[Hit Return to Continue]\n");
                            ch.endur = 100;
                            d.connected = CON_PICK_HOMETOWN;
                        }
                    }
                    case 'n', 'N' -> {
                        for (var i = 0; i < MAX_STATS; i++) {
                            ch.perm_stat[i] = number_range(10, (20 + ORG_RACE(ch).pcRace.stats[i] + ch.clazz.stats[i]));
                            ch.perm_stat[i] = UMIN(25, ch.perm_stat[i]);
                        }
                        {
                            var statsf = new Formatter();
                            statsf.format("Str:%s  Int:%s  Wis:%s  Dex:%s  Con:%s Cha:%s \n Accept (Y/N)? ", get_stat_alias(ch, STAT_STR), get_stat_alias(ch, STAT_INT), get_stat_alias(ch, STAT_WIS), get_stat_alias(ch, STAT_DEX), get_stat_alias(ch, STAT_CON), get_stat_alias(ch, STAT_CHA));

                            write_to_buffer(d, statsf.toString());
                            d.connected = CON_ACCEPT_STATS;
                        }
                    }
                    default -> write_to_buffer(d, "Please answer (Y/N)? ");
                }
                break;

            case CON_GET_ALIGNMENT:
                switch (argument.length() == 0 ? 'x' : argument.charAt(0)) {
                    case 'g', 'G' -> {
                        ch.alignment = 1000;
                        write_to_buffer(d, "Now your character is good.\n");
                    }
                    case 'n', 'N' -> {
                        ch.alignment = 0;
                        write_to_buffer(d, "Now your character is neutral.\n");
                    }
                    case 'e', 'E' -> {
                        ch.alignment = -1000;
                        write_to_buffer(d, "Now your character is evil.\n");
                    }
                    default -> {
                        write_to_buffer(d, "That's not a valid alignment.\n");
                        write_to_buffer(d, "Which alignment (G/N/E)? ");
                        return;
                    }
                }
                write_to_buffer(d, "\n[Hit Return to Continue]\n");
                ch.endur = 100;
                d.connected = CON_PICK_HOMETOWN;
                break;

            case CON_PICK_HOMETOWN:
                buf = "[M]idgaard, [N]ew Thalos" + (IS_NEUTRAL(ch) ? ", [O]fcol" : "") + "?";
                if (ch.endur != 0) {
                    ch.endur = 0;
                    if (!hometown_check(ch)) {
                        do_help(ch, "hometown");
                        write_to_buffer(d, buf);
                        d.connected = CON_PICK_HOMETOWN;
                        return;
                    } else {
                        write_to_buffer(d, "[Hit Return to Continue]\n");
                        ch.endur = 100;
                        d.connected = CON_GET_ETHOS;
                    }
                    break;
                }
                switch (argument.length() == 0 ? 'x' : argument.charAt(0)) {
                    case 'H':
                    case 'h':
                    case '?':
                        do_help(ch, "hometown");
                        write_to_buffer(d, buf);
                        return;
                    case 'M':
                    case 'm':
                        if (hometown_ok(ch, 0)) {
                            ch.hometown = 0;
                            write_to_buffer(d, "Now your hometown is Midgaard.\n");
                            break;
                        }
                    case 'N':
                    case 'n':
                        if (hometown_ok(ch, 1)) {
                            ch.hometown = 1;
                            write_to_buffer(d, "Now your hometown is New Thalos.\n");
                            break;
                        }
                    case 'O':
                    case 'o':
                        if (hometown_ok(ch, 3)) {
                            ch.hometown = 3;
                            write_to_buffer(d, "Now your hometown is Ofcol.\n");
                            break;
                        }
                    default:
                        write_to_buffer(d, "\nThat is not a valid hometown.\n");
                        write_to_buffer(d, "Which hometown do you want <type help for more info>? ");
                        return;
                }
                ch.endur = 100;
                write_to_buffer(d, "\n[Hit Return to Continue]\n");
                d.connected = CON_GET_ETHOS;
                break;

            case CON_GET_ETHOS:
                if (ch.endur == 0) {
                    switch (argument.length() == 0 ? 'x' : argument.charAt(0)) {
                        case 'H', 'h', '?' -> {
                            do_help(ch, "alignment");
                            return;
                        }
                        case 'L', 'l' -> {
                            write_to_buffer(d, "\nNow you are lawful-" + (IS_GOOD(ch) ? "good" : IS_EVIL(ch) ? "evil" : "neutral") + ".\n");
                            ch.ethos = ETHOS_LAWFUL;
                        }
                        case 'N', 'n' -> {
                            write_to_buffer(d, "\nNow you are neutral-" + (IS_GOOD(ch) ? "good" : IS_EVIL(ch) ? "evil" : "neutral") + ".\n");
                            ch.ethos = ETHOS_NEUTRAL;
                        }
                        case 'C', 'c' -> {
                            write_to_buffer(d, "\nNow you are chaotic-" + (IS_GOOD(ch) ? "good" : IS_EVIL(ch) ? "evil" : "neutral") + ".\n");
                            ch.ethos = ETHOS_CHAOTIC;
                        }
                        default -> {
                            write_to_buffer(d, "\nThat is not a valid ethos.\n");
                            write_to_buffer(d, "What ethos do you want, (L/N/C) <type help for more info> ?");
                            return;
                        }
                    }
                } else {
                    ch.endur = 0;
                    var d1 = ch.desc;
                    var chooseEthos = true;
                    if (ch.clazz.ethos != ETHOS_ANY) {
                        ch.ethos = ch.clazz.ethos;
                        write_to_buffer(d1, "You are " + DB.capitalize(ethos_table[ch.ethos].name) + ".\n");
                        chooseEthos = false;
                    }
                    if (chooseEthos) {
                        write_to_buffer(d, "What ethos do you want, (L/N/C) <type help for more info> ?");
                        d.connected = CON_GET_ETHOS;
                        return;
                    }
                }
                write_to_buffer(d, "\n[Hit Return to Continue]\n");
                d.connected = CON_CREATE_DONE;
                break;

            case CON_CREATE_DONE:
                log_string(ch.name + "@" + d.host + " new player.");
                //TOOD: group_add(ch);
                ch.pcdata.learned[gsn_recall.ordinal()] = 75;
                write_to_buffer(d, "\n");
                do_help(ch, "GENERAL");
                write_to_buffer(d, "[Hit Return to Continue]\n");
                d.connected = CON_READ_NEWBIE;
                return;
            case CON_READ_NEWBIE:
                write_to_buffer(d, "\n");
                do_help(ch, "motd");
                d.connected = CON_READ_MOTD;
                return;
            case CON_READ_IMOTD:
                write_to_buffer(d, "\n");
                do_help(ch, "motd");
                d.connected = CON_READ_MOTD;
                break;

            case CON_READ_MOTD:
                write_to_buffer(d, "\nWelcome to Multi User Dungeon of Nightworks. Enjoy!!...\n");
                ch.next = char_list;
                char_list = ch;
                d.connected = CON_PLAYING;

            {
                int count;
                count = 0;
                for (d = descriptor_list; d != null; d = d.next) {
                    if (d.connected == CON_PLAYING) {
                        count++;
                    }
                }
                max_on = UMAX(count, max_on);
            }
            iNumPlayers++;


            if (ch.level == 0) {
                int l;

                ch.level = 1;
                ch.exp = base_exp(ch, ch.pcdata.points);
                ch.hit = ch.max_hit;
                ch.mana = ch.max_mana;
                ch.move = ch.max_move;
                ch.pcdata.perm_hit = ch.max_hit;
                ch.pcdata.perm_mana = ch.max_mana;
                ch.pcdata.perm_move = ch.max_move;
                ch.train += 3;
                ch.practice += 5;
                ch.pcdata.death = 0;

                set_title(ch, "the " + ch.clazz.getTitle(ch.level, ch.sex));

                obj_to_char(create_object(get_obj_index(OBJ_VNUM_MAP), 0), ch);
                obj_to_char(create_object(get_obj_index(OBJ_VNUM_NMAP1), 0), ch);
                obj_to_char(create_object(get_obj_index(OBJ_VNUM_NMAP2), 0), ch);

                if (ch.hometown == 0 && IS_EVIL(ch)) {
                    obj_to_char(create_object(get_obj_index(OBJ_VNUM_MAP_SM), 0), ch);
                }

                if (ch.hometown == 1) {
                    obj_to_char(create_object(get_obj_index(OBJ_VNUM_MAP_NT), 0), ch);
                } else if (ch.hometown == 2) {
                    obj_to_char(create_object(get_obj_index(OBJ_VNUM_MAP_TITAN), 0), ch);
                } else if (ch.hometown == 3) {
                    obj_to_char(create_object(get_obj_index(OBJ_VNUM_MAP_OFCOL), 0), ch);
                } else if (ch.hometown == 4) {
                    obj_to_char(create_object(get_obj_index(OBJ_VNUM_MAP_OLD), 0), ch);
                }

                ch.pcdata.learned[get_weapon_sn(ch, false).ordinal()] = 40;

                char_to_room(ch, get_room_index(ROOM_VNUM_SCHOOL));
                send_to_char("\n", ch);
                do_help(ch, "NEWBIE INFO");
                send_to_char("\n", ch);

                /* give some bonus time */
                for (l = 0; l < MAX_TIME_LOG; l++) {
                    ch.pcdata.log_time[l] = 60;
                }

                do_outfit(ch);
            } else if (ch.in_room != null) {
                if (cabal_area_check(ch)) {
                    var i = 1;

                    if (IS_GOOD(ch)) {
                        i = 0;
                    } else if (IS_EVIL(ch)) {
                        i = 2;
                    }
                    char_to_room(ch, get_room_index(hometown_table[ch.hometown].altar[i]));
                } else {
                    char_to_room(ch, ch.in_room);
                }
            } else if (IS_IMMORTAL(ch)) {
                char_to_room(ch, get_room_index(ROOM_VNUM_CHAT));
            } else {
                char_to_room(ch, get_room_index(ROOM_VNUM_TEMPLE));
            }

            reset_char(ch);
            if (!IS_IMMORTAL(ch)) {
                act("$n has entered the game.", ch, null, null, TO_ROOM);
            }
            wiznet("$N entered the realms.", ch, null, WIZ_LOGINS, 0, 0);

            if (ch.exp < (exp_per_level(ch, ch.pcdata.points) * ch.level)) {
                ch.exp = (ch.level) * (exp_per_level(ch, ch.pcdata.points));
            } else if (ch.exp > (exp_per_level(ch, ch.pcdata.points) * (ch.level + 1))) {
                ch.exp = (ch.level + 1) * (exp_per_level(ch, ch.pcdata.points));
                ch.exp -= 10;
            }

            if (IS_QUESTOR(ch) && ch.pcdata.questmob == 0) {
                ch.pcdata.nextquest = ch.pcdata.countdown;
                ch.pcdata.questobj = 0;
                ch.act = REMOVE_BIT(ch.act, PLR_QUESTOR);
            }

            if (IS_SET(ch.act, PLR_NO_EXP)) {
                ch.act = REMOVE_BIT(ch.act, PLR_NO_EXP);
            }
            if (IS_SET(ch.act, PLR_CHANGED_AFF)) {
                ch.act = REMOVE_BIT(ch.act, PLR_CHANGED_AFF);
            }

            for (var i = 0; i < MAX_STATS; i++) {
                if (ch.perm_stat[i] > (20 + ORG_RACE(ch).pcRace.stats[i] + ch.clazz.stats[i])) {
                    ch.train += (ch.perm_stat[i] - (20 + ORG_RACE(ch).pcRace.stats[i] + ch.clazz.stats[i]));
                    ch.perm_stat[i] = (20 + ORG_RACE(ch).pcRace.stats[i] + ch.clazz.stats[i]);
                }
            }

            do_look(ch, "auto");

            if (ch.gold > 10000 && !IS_IMMORTAL(ch)) {
                send_to_char("You are taxed " + ((ch.gold - 10000) / 2) + " gold to pay for the Mayor's bar.\n", ch);
                ch.gold -= (ch.gold - 10000) / 2;
            }


            if (ch.pcdata.bank_g > 400000 && !IS_IMMORTAL(ch)) {
                send_to_char("You are taxed " + (ch.pcdata.bank_g - 400000) + " gold to pay for war expenses of Sultan.\n", ch);
                ch.pcdata.bank_g = 400000;
            }


            if (ch.pet != null) {
                char_to_room(ch.pet, ch.in_room);
                act("$n has entered the game.", ch.pet, null, null, TO_ROOM);
            }

            if (ch.pcdata.confirm_delete) {
                send_to_char("You are given some bonus played time per week.\n", ch);
                ch.pcdata.confirm_delete = false;
            }

            do_unread(ch, "login");

            break;
        }
    }

    /*
     * Parse a name for acceptability.
     */

    static boolean check_parse_name(String name) {
        /*
         * Reserved words.
         */
        if (is_name(name, "all auto immortal self someone something the you demise balance circle loner honor")) {
            return false;
        }

        if (!str_cmp(TextUtils.capitalize(name), "Chronos") || !str_prefix("Chro", name) || !str_suffix("ronos", name)) {
            return false;
        }

        /*
         * Length restrictions.
         */

        if (name.length() < 2) {
            return false;
        }

        if (name.length() > 12) {
            return false;
        }

        /*
         * Alphanumerics only.
         */
        {
            var total_caps = 0;
            for (var i = 0; i < name.length(); i++) {
                var c = name.charAt(i);
                if (!Character.isLetter(c)) {
                    return false;
                }

                if (Character.isUpperCase(c)) {/* ugly anti-caps hack */
                    total_caps++;
                }
            }
            if ((total_caps > name.length() / 2 && name.length() < 3)) {
                return false;
            }
        }

        /*
         * Prevent players from naming themselves after mobs.
         */
        {
            MOB_INDEX_DATA pMobIndex;
            int iHash;

            for (iHash = 0; iHash < MAX_KEY_HASH; iHash++) {
                for (pMobIndex = mob_index_hash[iHash]; pMobIndex != null; pMobIndex = pMobIndex.next) {
                    if (is_name(name, pMobIndex.player_name)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /*
     * Look for link-dead player to reconnect.
     */

    static boolean check_reconnect(@NotNull DESCRIPTOR_DATA d, String name, boolean fConn) {
        for (var ch = char_list; ch != null; ch = ch.next) {
            if (!IS_NPC(ch) && (!fConn || ch.desc == null) && !str_cmp(d.character.name, ch.name)) {
                if (!fConn) {
                    d.character.pcdata.pwd = ch.pcdata.pwd;
                } else {
                    free_char(d.character);
                    d.character = ch;
                    ch.desc = d;
                    ch.timer = 0;
                    send_to_char("Reconnecting. Type replay to see missed tells.\n", ch);
                    if (!IS_IMMORTAL(ch)) {
                        act("$n has reconnected.", ch, null, null, TO_ROOM);
                    }
                    if (get_light_char(ch) != null) {
                        --ch.in_room.light;
                    }

                    log_string(ch.name + "@" + d.host + " reconnected.");
                    wiznet("$N groks the fullness of $S link.", ch, null, WIZ_LINKS, 0, 0);
                    d.connected = CON_PLAYING;
                }
                return true;
            }
        }

        return false;
    }

    /*
     * Check if already playing.
     */

    static boolean check_playing(DESCRIPTOR_DATA d, String name) {
        for (var dold = descriptor_list; dold != null; dold = dold.next) {
            if (dold != d && dold.character != null && dold.connected != CON_GET_NAME && dold.connected != CON_GET_OLD_PASSWORD && !str_cmp(name, dold.original != null ? dold.original.name : dold.character.name)) {
                write_to_buffer(d, "That character is already playing.\n");
                write_to_buffer(d, "Do you wish to connect anyway (Y/N)?");
                d.connected = CON_BREAK_CONNECT;
                return true;
            }
        }
        return false;
    }


    static void stop_idling(@NotNull CHAR_DATA ch) {
        if (ch.desc == null || ch.desc.connected != CON_PLAYING || ch.was_in_room == null || ch.in_room != get_room_index(ROOM_VNUM_LIMBO)) {
            return;
        }

        ch.timer = 0;
        char_from_room(ch);
        char_to_room(ch, ch.was_in_room);
        ch.was_in_room = null;
        act("$n has returned from the void.", ch, null, null, TO_ROOM);
    }

    /*
     * Write to one char.
     */
    static void send_to_char(@NotNull CharSequence txt, @NotNull CHAR_DATA ch) {
        if (ch.desc != null) {
            write_to_buffer(ch.desc, txt);
        }
    }

    /*
     * Send a page to one char.
     */
    static void page_to_char(@NotNull CharSequence txt, @NotNull CHAR_DATA ch) {
        if (ch.desc == null) {
            return;
        }
        if (ch.lines == 0) {
            send_to_char(txt, ch);
            return;
        }

        ch.desc.showstr_head = txt.toString();
        ch.desc.showstr_point = 0;
        show_string(ch.desc, "");
    }

    /* string pager */

    static void show_string(@NotNull DESCRIPTOR_DATA d, @NotNull String input) {
        var buf = new StringBuilder();
        one_argument(input, buf);
        if (!buf.isEmpty()) {
            d.showstr_head = null;
            d.showstr_point = 0;
            return;
        }
        int show_lines;
        if (d.character != null) {
            show_lines = d.character.lines;
        } else {
            show_lines = 0;
        }

        int toggle = 1, lines = 0;
        var buffer = new StringBuilder();
        for (; ; d.showstr_point++) {
            char c;
            if (d.showstr_head.length() == d.showstr_point) {
                c = 0;
            } else {
                c = d.showstr_head.charAt(d.showstr_point);
                buffer.append(c);
                if ((c == '\n' || c == '\r') && (toggle = -toggle) < 0) {
                    lines++;
                }
            }
            if (c == 0 || (show_lines > 0 && lines >= show_lines)) {
                write_to_buffer(d, buffer);
                var chk = d.showstr_point;
                while (chk < d.showstr_head.length() && isSpace(d.showstr_head.charAt(chk))) {
                    chk++;
                }
                if (chk == d.showstr_head.length()) {
                    d.showstr_head = null;
                    d.showstr_point = 0;
                }
                return;
            }
        }
    }

    static void act(@NotNull CharSequence seq, @NotNull CHAR_DATA ch, Object arg1, Object arg2, int type) {
        if (seq.isEmpty()) {
            return;
        }
        act(seq.toString(), ch, arg1, arg2, type, POS_RESTING);
    }

    static final String[] he_she = {"it", "he", "she"};

    static final String[] him_her = {"it", "him", "her"};
    static final String[] his_her = {"its", "his", "her"};

    static void act(@NotNull String actStr, @NotNull CHAR_DATA ch, Object arg1, Object arg2, int type, int min_pos) {
        // Discard null and zero-length messages: useful to avoid null-checks in clients
        if (actStr.isEmpty()) {
            return;
        }

        /* discard null rooms and chars */
        if (ch.in_room == null) {
            return;
        }

        var vch = arg2 instanceof CHAR_DATA ? (CHAR_DATA) arg2 : null;
        var to = ch.in_room.people;
        if (type == TO_VICT) {
            if (vch == null) {
                bug("Act: null vch with TO_VICT.");
                return;
            }
            if (vch.in_room == null) {
                return;
            }
            to = vch.in_room.people;
        }

        var buf = new StringBuilder();
        var fname = new StringBuilder();
        for (; to != null; to = to.next_in_room) {
            buf.setLength(0);
            if (to.desc == null || to.position < min_pos) {
                continue;
            }

            if (type == TO_CHAR && to != ch) {
                continue;
            }
            if (type == TO_VICT && (to != arg2/*victim*/ || to == ch)) {
                continue;
            }
            if (type == TO_ROOM && to == ch) {
                continue;
            }
            if (type == TO_NOTVICT && (to == ch || to == arg2/*victim*/)) {
                continue;
            }

            for (var ii = 0; ii < actStr.length(); ii++) {
                var c = actStr.charAt(ii);
                if (c != '$') {
                    buf.append(c);
                    continue;
                }
                c = actStr.charAt(++ii);
                String i;
                switch (c) {
                    default -> {
                        bug("Act: bad code %d.", c);
                        i = " <@@@> ";
                    }
                    /* Thx alex for 't' idea */
                    case 't' -> i = (String) arg1;
                    case 'T' -> i = (String) arg2;
                    case 'n' -> i = (is_affected(ch, gsn_doppelganger) && !IS_SET(to.act, PLR_HOLYLIGHT)) ? PERS(ch.doppel, to) : PERS(ch, to);
                    case 'N' -> i = (is_affected(vch, gsn_doppelganger) && !IS_SET(to.act, PLR_HOLYLIGHT)) ? PERS(vch.doppel, to) : PERS(vch, to);
                    case 'e' ->
                            i = (is_affected(ch, gsn_doppelganger) && !IS_SET(to.act, PLR_HOLYLIGHT)) ? he_she[URANGE(0, ch.doppel.sex, 2)] : he_she[URANGE(0, ch.sex, 2)];
                    case 'E' ->
                            i = (is_affected(vch, gsn_doppelganger) && !IS_SET(to.act, PLR_HOLYLIGHT)) ? he_she[URANGE(0, vch.doppel.sex, 2)] : he_she[URANGE(0, vch.sex, 2)];
                    case 'm' ->
                            i = (is_affected(ch, gsn_doppelganger) && !IS_SET(to.act, PLR_HOLYLIGHT)) ? him_her[URANGE(0, ch.doppel.sex, 2)] : him_her[URANGE(0, ch.sex, 2)];
                    case 'M' ->
                            i = (is_affected(vch, gsn_doppelganger) && !IS_SET(to.act, PLR_HOLYLIGHT)) ? him_her[URANGE(0, vch.doppel.sex, 2)] : him_her[URANGE(0, vch.sex, 2)];
                    case 's' ->
                            i = (is_affected(ch, gsn_doppelganger) && !IS_SET(to.act, PLR_HOLYLIGHT)) ? his_her[URANGE(0, ch.doppel.sex, 2)] : his_her[URANGE(0, ch.sex, 2)];
                    case 'S' ->
                            i = (is_affected(vch, gsn_doppelganger) && !IS_SET(to.act, PLR_HOLYLIGHT)) ? his_her[URANGE(0, vch.doppel.sex, 2)] : his_her[URANGE(0, vch.sex, 2)];
                    case 'p' -> i = can_see_obj(to, (OBJ_DATA) arg1) ? ((OBJ_DATA) arg1).short_descr : "something";
                    case 'P' -> i = can_see_obj(to, (OBJ_DATA) arg2) ? ((OBJ_DATA) arg2).short_descr : "something";
                    case 'd' -> {
                        if (arg2 == null || ((String) arg2).length() == 0) {
                            i = "door";
                        } else {
                            fname.setLength(0);
                            one_argument((String) arg2, fname);
                            i = fname.toString();
                        }
                    }
                }
                buf.append(i);
            }
            buf.append("\n");
            /* fix for color prefix and capitalization */
            if (buf.charAt(0) == 0x1B) {
                var n = buf.indexOf("m", 1);
                buf.setCharAt(n + 1, UPPER(buf.charAt(n + 1)));
            } else {
                buf.setCharAt(0, UPPER(buf.charAt(0)));
            }

            write_to_buffer(to.desc, buf);
        }
    }


    static void log_area_popularity() {
        var file = new File("area_stat.txt");
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
        try {
            try (var fp = new FileWriter(nw_config.var_astat_file, true)) {
                var f = new Formatter(fp);
                f.format("\nBooted %s Area popularity statistics (in String  ticks)\n", new Date(boot_time));
                for (AREA_DATA area = area_first; area != null; area = area.next) {
                    if (area.count >= 5000000) {
                        f.format("%-60s overflow\n", area.name);
                    } else {
                        f.format("%-60s %d\n", area.name, area.count);
                    }
                }
            }
        } catch (IOException e) {
            logError(e);
        }
    }

    /*
     * Function for save processes.
     */

    static String get_stat_alias(@NotNull CHAR_DATA ch, int where) {
        String stat;
        if (where == STAT_STR) {
            int istat = get_curr_stat(ch, STAT_STR);
            if (istat > 22) {
                stat = "Titantic";
            } else if (istat >= 20) {
                stat = "Herculian";
            } else if (istat >= 18) {
                stat = "Strong";
            } else if (istat >= 14) {
                stat = "Average";
            } else if (istat >= 10) {
                stat = "Poor";
            } else {
                stat = "Weak";
            }
            return stat;
        }

        if (where == STAT_WIS) {
            int istat = get_curr_stat(ch, STAT_WIS);
            if (istat > 22) {
                stat = "Excellent";
            } else if (istat >= 20) {
                stat = "Wise";
            } else if (istat >= 18) {
                stat = "Good";
            } else if (istat >= 14) {
                stat = "Average";
            } else if (istat >= 10) {
                stat = "Dim";
            } else {
                stat = "Fool";
            }
            return stat;
        }

        if (where == STAT_CON) {
            int istat = get_curr_stat(ch, STAT_CON);
            if (istat > 22) {
                stat = "Iron";
            } else if (istat >= 20) {
                stat = "Hearty";
            } else if (istat >= 18) {
                stat = "Healty";
            } else if (istat >= 14) {
                stat = "Average";
            } else if (istat >= 10) {
                stat = "Poor";
            } else {
                stat = "Fragile";
            }
            return (stat);
        }

        if (where == STAT_INT) {
            int istat = get_curr_stat(ch, STAT_INT);
            if (istat > 22) {
                stat = "Genious";
            } else if (istat >= 20) {
                stat = "Clever";
            } else if (istat >= 18) {
                stat = "Good";
            } else if (istat >= 14) {
                stat = "Average";
            } else if (istat >= 10) {
                stat = "Poor";
            } else {
                stat = "Hopeless";
            }
            return stat;
        }

        if (where == STAT_DEX) {
            int istat = get_curr_stat(ch, STAT_DEX);
            if (istat > 22) {
                stat = "Fast";
            } else if (istat >= 20) {
                stat = "Quick";
            } else if (istat >= 18) {
                stat = "Dextrous";
            } else if (istat >= 14) {
                stat = "Average";
            } else if (istat >= 10) {
                stat = "Clumsy";
            } else {
                stat = "Slow";
            }
            return stat;
        }

        if (where == STAT_CHA) {
            int istat = get_curr_stat(ch, STAT_CHA);
            if (istat > 22) {
                stat = "Charismatic";
            } else if (istat >= 20) {
                stat = "Familier";
            } else if (istat >= 18) {
                stat = "Good";
            } else if (istat >= 14) {
                stat = "Average";
            } else if (istat >= 10) {
                stat = "Poor";
            } else {
                stat = "Mongol";
            }
            return stat;
        }
        bug("stat_alias: Bad stat number.");
        return null;
    }

    static boolean class_ok(@NotNull CHAR_DATA ch, @NotNull Clazz clazz) {
        return ORG_RACE(ch).pcRace.getClassModifier(clazz).expMult != -1 && (ch.sex & clazz.sex) > 0;
    }

    static int align_restrict(@NotNull CHAR_DATA ch) {
        var d = ch.desc;

        if (IS_SET(ORG_RACE(ch).pcRace.align, CR_GOOD) || IS_SET(ch.clazz.align, CR_GOOD)) {
            write_to_buffer(d, "Your character has good tendencies.\n");
            ch.alignment = 1000;
            return N_ALIGN_GOOD;
        }

        if (IS_SET(ORG_RACE(ch).pcRace.align, CR_NEUTRAL) || IS_SET(ch.clazz.align, CR_NEUTRAL)) {
            write_to_buffer(d, "Your character has neutral tendencies.\n");
            ch.alignment = 0;
            return N_ALIGN_NEUTRAL;
        }

        if (IS_SET(ORG_RACE(ch).pcRace.align, CR_EVIL) || IS_SET(ch.clazz.align, CR_EVIL)) {
            write_to_buffer(d, "Your character has evil tendencies.\n");
            ch.alignment = -1000;
            return N_ALIGN_EVIL;
        }
        return N_ALIGN_ALL;
    }

    static boolean hometown_check(@NotNull CHAR_DATA ch) {
        var d = ch.desc;

        if (ch.clazz == Clazz.NECROMANCER || ch.clazz == Clazz.VAMPIRE) {
            write_to_buffer(d, "\n");
            write_to_buffer(d, "Your hometown is Old Midgaard, permanently.\n");
            ch.hometown = 4;
            write_to_buffer(d, "\n");
            return true;
        }

        var race = ORG_RACE(ch);
        if (race == Race.STORM_GIANT || race == Race.CLOUD_GIANT || race == Race.FIRE_GIANT || race == Race.FROST_GIANT) {
            write_to_buffer(d, "\n");
            write_to_buffer(d, "Your hometown is Valley of Titans, permanently.\n");
            ch.hometown = 2;
            write_to_buffer(d, "\n");
            return true;
        }
        return false;
    }

    static boolean hometown_ok(@NotNull CHAR_DATA ch, int home) {
        return !(!IS_NEUTRAL(ch) && home == 3);
    }
}
