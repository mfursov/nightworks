package net.sf.nightworks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Formatter;

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
import static net.sf.nightworks.Nightworks.DO_FUN;
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
import static net.sf.nightworks.Nightworks.POS_STANDING;
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
import static net.sf.nightworks.util.TextUtils.isdigit;
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

    static final int COM_INGORE = 1;

/*
* Structure for a command in the command lookupRace table.
*/

    static class cmd_type implements DO_FUN {
        final String name;
        final Method do_fun;
        final int position;
        final int level;
        final int log;
        final int show;
        final int extra;

        private static final Class[] do_fun_classes =
                {
                        ActMove.class, ActInfo.class, ActWiz.class,
                        ActComm.class, ActHera.class, ActObj.class,
                        ActSkill.class, DB.class, MartialArt.class, Magic.class,
                        Note.class, Interp.class, Fight.class,
                        Quest.class, Healer.class, Ban.class, Flags.class
                };
        private static final Class[] do_fun_signature = {CHAR_DATA.class, String.class};

        public cmd_type(String name, String do_fun_name, int position, int level, int log, int show, int extra) {
            this.name = name;
            this.do_fun = lookup_dofun(do_fun_name);
            this.position = position;
            this.level = level;
            this.log = log;
            this.show = show;
            this.extra = extra;
        }

        private static Method lookup_dofun(String do_fun_name) {
            for (Class clz : do_fun_classes) {
                try {
                    return clz.getDeclaredMethod(do_fun_name, do_fun_signature);
                } catch (NoSuchMethodException e) {
                    //ignore
                }
            }
            throw new RuntimeException("DO_FUN not found:" + do_fun_name);
        }

        public void run(CHAR_DATA ch, String argument) {
            try {
                do_fun.invoke(null, ch, argument);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    * Command logging types.
    */
    static final int LOG_NORMAL = 0;
    static final int LOG_ALWAYS = 1;
    static final int LOG_NEVER = 2;

/*
* Command table.
*/

    private static cmd_type[] commandsTable;

    static void initCommandsTable() {
        assert (commandsTable == null);
        commandsTable = new cmd_type[]{
                /*
                * Common movement commands.
                */
                new cmd_type("north", "do_north", POS_STANDING, 0, LOG_NEVER, 0, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("east", "do_east", POS_STANDING, 0, LOG_NEVER, 0, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("south", "do_south", POS_STANDING, 0, LOG_NEVER, 0, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("west", "do_west", POS_STANDING, 0, LOG_NEVER, 0, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("up", "do_up", POS_STANDING, 0, LOG_NEVER, 0, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("down", "do_down", POS_STANDING, 0, LOG_NEVER, 0, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("run", "do_run", POS_STANDING, ML, LOG_NEVER, 0, CMD_KEEP_HIDE | CMD_GHOST),

                /*
                * Common other commands.
                * Placed here so one and two letter abbreviations work.
                */
                new cmd_type("at", "do_at", POS_DEAD, L6, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("cast", "do_cast", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("claw", "do_claw", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("crecall", "do_crecall", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("auction", "do_auction", POS_SLEEPING, 0, LOG_NORMAL, 1, CMD_GHOST),
                new cmd_type("buy", "do_buy", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("channels", "do_channels", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("exits", "do_exits", POS_RESTING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("estimate", "do_estimate", POS_RESTING, 0, LOG_NORMAL, 1, CMD_GHOST),
                new cmd_type("get", "do_get", POS_RESTING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE),

                new cmd_type("goto", "do_goto", POS_DEAD, L8, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("glist", "do_glist", POS_DEAD, 0, LOG_NEVER, 1, 0),
                new cmd_type("group", "do_group", POS_SLEEPING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE),
                new cmd_type("hit", "do_kill", POS_FIGHTING, 0, LOG_NORMAL, 0, 0),
                new cmd_type("inventory", "do_inventory", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("kill", "do_kill", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("look", "do_look", POS_RESTING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("order", "do_order", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("practice", "do_pracnew", POS_SLEEPING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE),
                new cmd_type("prac_old", "do_practice", POS_SLEEPING, ML, LOG_NORMAL, 1, CMD_KEEP_HIDE),
                new cmd_type("rest", "do_rest", POS_SLEEPING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE),
                new cmd_type("repair", "do_repair", POS_SLEEPING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE),
                new cmd_type("sit", "do_sit", POS_SLEEPING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("smithing", "do_smithing", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("sockets", "do_sockets", POS_DEAD, L4, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("stand", "do_stand", POS_SLEEPING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("tell", "do_tell", POS_RESTING, 0, LOG_NORMAL, 1, CMD_GHOST),
                new cmd_type("unlock", "do_unlock", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("wield", "do_wear", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("wizhelp", "do_wizhelp", POS_DEAD, IM, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),

                /*
                * Informational commands.
                */
                new cmd_type("affects", "do_affects", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("areas", "do_areas", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("balance", "do_balance", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("bug", "do_bug", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("changes", "do_changes", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("commands", "do_commands", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("compare", "do_compare", POS_RESTING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE),
                new cmd_type("consider", "do_consider", POS_RESTING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE),
                new cmd_type("concentrate", "do_concentrate", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("count", "do_count", POS_SLEEPING, HE, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("credits", "do_credits", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("deposit", "do_deposit", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("equipment", "do_equipment", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("escape", "do_escape", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("examine", "do_examine", POS_RESTING, 0, LOG_NORMAL, 1, CMD_GHOST),
                new cmd_type("help", "do_help", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("idea", "do_idea", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("motd", "do_motd", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("news", "do_news", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("raffects", "do_raffects", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("read", "do_read", POS_RESTING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("report", "do_report", POS_RESTING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("rules", "do_rules", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("scan", "do_scan", POS_RESTING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("score", "do_nscore", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("skills", "do_skills", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("speak", "do_speak", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("socials", "do_socials", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("show", "do_show", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("spells", "do_spells", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("time", "do_time", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("typo", "do_typo", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("weather", "do_weather", POS_RESTING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("who", "do_who", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("whois", "do_whois", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("withdraw", "do_withdraw", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("wizlist", "do_wizlist", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("worth", "do_worth", POS_SLEEPING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),

                /*
                * Configuration commands.
                */
                new cmd_type("alia", "do_alia", POS_DEAD, 0, LOG_NORMAL, 0, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("alias", "do_alias", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("clear", "do_clear", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("cls", "do_clear", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("autolist", "do_autolist", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("color", "do_color", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("autoassist", "do_autoassist", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("autoexit", "do_autoexit", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("autogold", "do_autogold", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("autoloot", "do_autoloot", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("autosac", "do_autosac", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("autosplit", "do_autosplit", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("brief", "do_brief", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("combine", "do_combine", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("compact", "do_compact", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("description", "do_description", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("delet", "do_delet", POS_DEAD, 0, LOG_ALWAYS, 0, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("delete", "do_delete", POS_STANDING, 0, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("identify", "do_identify", POS_STANDING, 0, LOG_NORMAL, 1, CMD_GHOST),
                new cmd_type("nocancel", "do_nocancel", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("nofollow", "do_nofollow", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("noloot", "do_noloot", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("nosummon", "do_nosummon", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("outfit", "do_outfit", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("tick", "do_tick", POS_DEAD, ML, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("password", "do_password", POS_DEAD, 0, LOG_NEVER, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("prompt", "do_prompt", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("quest", "do_quest", POS_RESTING, 0, LOG_NORMAL, 1, CMD_GHOST),
                new cmd_type("scroll", "do_scroll", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("title", "do_title", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("unalias", "do_unalias", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("wimpy", "do_wimpy", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),

                /*
                * Communication commands.
                */
                new cmd_type("bearcall", "do_bear_call", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("cb", "do_cb", POS_SLEEPING, 0, LOG_NORMAL, 1, CMD_GHOST),
                new cmd_type("deaf", "do_deaf", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("emote", "do_emote", POS_RESTING, 0, LOG_NORMAL, 1, CMD_GHOST),
                new cmd_type("pmote", "do_pmote", POS_RESTING, 0, LOG_NORMAL, 1, CMD_GHOST),
                new cmd_type(",", "do_emote", POS_RESTING, 0, LOG_NORMAL, 0, CMD_GHOST),
                new cmd_type("gtell", "do_gtell", POS_DEAD, 0, LOG_NORMAL, 1, CMD_GHOST),
                new cmd_type(";", "do_gtell", POS_DEAD, 0, LOG_NORMAL, 0, CMD_GHOST),
                new cmd_type("note", "do_note", POS_SLEEPING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("pose", "do_pose", POS_RESTING, 0, LOG_NORMAL, 1, CMD_GHOST),
                new cmd_type("pray", "do_pray", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("quiet", "do_quiet", POS_SLEEPING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("reply", "do_reply", POS_SLEEPING, 0, LOG_NORMAL, 1, CMD_GHOST),
                new cmd_type("replay", "do_replay", POS_SLEEPING, 0, LOG_NORMAL, 1, CMD_GHOST),
                new cmd_type("say", "do_say", POS_RESTING, 0, LOG_NORMAL, 1, CMD_GHOST),
                new cmd_type("'", "do_say", POS_RESTING, 0, LOG_NORMAL, 0, CMD_GHOST),
                new cmd_type("shout", "do_shout", POS_RESTING, 3, LOG_NORMAL, 1, CMD_GHOST),
                new cmd_type("warcry", "do_warcry", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("unread", "do_unread", POS_SLEEPING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("yell", "do_yell", POS_RESTING, 0, LOG_NORMAL, 1, CMD_GHOST),

                /*
                * Object manipulation commands.
                */
                new cmd_type("brandish", "do_brandish", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("bury", "do_bury", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("butcher", "do_butcher", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("close", "do_close", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("detect", "do_detect_hidden", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("drag", "do_drag", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("drink", "do_drink", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("drop", "do_drop", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("eat", "do_eat", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("enchant", "do_enchant", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("envenom", "do_envenom", POS_RESTING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE),
                new cmd_type("fill", "do_fill", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("fly", "do_fly", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("give", "do_give", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("heal", "do_heal", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("hold", "do_wear", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("layhands", "do_layhands", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("list", "do_list", POS_RESTING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE),
                new cmd_type("lock", "do_lock", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("lore", "do_lore", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("open", "do_open", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("pick", "do_pick", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("pour", "do_pour", POS_RESTING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE),
                new cmd_type("put", "do_put", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("quaff", "do_quaff", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("recite", "do_recite", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("remove", "do_remove", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("request", "do_request", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("sell", "do_sell", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("take", "do_get", POS_RESTING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE),
                new cmd_type("sacrifice", "do_sacrifice", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("junk", "do_sacrifice", POS_RESTING, 0, LOG_NORMAL, 0, 0),
                new cmd_type("trophy", "do_trophy", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("value", "do_value", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("wear", "do_wear", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("zap", "do_zap", POS_RESTING, 0, LOG_NORMAL, 1, 0),

                /*
                * Combat commands.
                */
                new cmd_type("ambush", "do_ambush", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("assassinate", "do_assassinate", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("backstab", "do_backstab", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("bash", "do_bash", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("bashdoor", "do_bash_door", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("bs", "do_backstab", POS_STANDING, 0, LOG_NORMAL, 0, 0),
                new cmd_type("bite", "do_vbite", POS_STANDING, 0, LOG_NORMAL, 0, 0),
                new cmd_type("blindness", "do_blindness_dust", POS_FIGHTING, 0, LOG_ALWAYS, 1, 0),
                new cmd_type("touch", "do_vtouch", POS_STANDING, 0, LOG_NORMAL, 0, 0),
                new cmd_type("berserk", "do_berserk", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("bloodthirst", "do_bloodthirst", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("blackjack", "do_blackjack", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("caltraps", "do_caltraps", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("explode", "do_explode", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("camouflage", "do_camouflage", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("circle", "do_circle", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("cleave", "do_cleave", POS_STANDING, 0, LOG_NORMAL, 1, 0),

                new cmd_type("dirt", "do_dirt", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("disarm", "do_disarm", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("dishonor", "do_dishonor", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("dismount", "do_dismount", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("flee", "do_flee", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("guard", "do_guard", POS_STANDING, 0, LOG_NORMAL, 1, 0),

                new cmd_type("kick", "do_kick", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("lash", "do_lash", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("lioncall", "do_lion_call", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("make", "do_make", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("mount", "do_mount", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("murde", "do_murde", POS_FIGHTING, 0, LOG_NORMAL, 0, 0),
                new cmd_type("murder", "do_murder", POS_FIGHTING, 0, LOG_ALWAYS, 1, 0),
                new cmd_type("nerve", "do_nerve", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("poison", "do_poison_smoke", POS_FIGHTING, 0, LOG_ALWAYS, 1, 0),
                new cmd_type("rescue", "do_rescue", POS_FIGHTING, 0, LOG_NORMAL, 0, 0),
                new cmd_type("resistance", "do_resistance", POS_FIGHTING, 0, LOG_NORMAL, 0, 0),
                new cmd_type("truesight", "do_truesight", POS_FIGHTING, 0, LOG_NORMAL, 0, 0),
                new cmd_type("shield", "do_shield", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("spellbane", "do_spellbane", POS_FIGHTING, 0, LOG_NORMAL, 0, 0),
                new cmd_type("strangle", "do_strangle", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("tame", "do_tame", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("throw", "do_throw", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("tiger", "do_tiger", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("trip", "do_trip", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("target", "do_target", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("vampire", "do_vampire", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("vanish", "do_vanish", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("weapon", "do_weapon", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("blink", "do_blink", POS_FIGHTING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE),

                /*
                * Miscellaneous commands.
                */
                new cmd_type("endure", "do_endure", POS_STANDING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE),
                new cmd_type("enter", "do_enter", POS_STANDING, 0, LOG_NORMAL, 1, CMD_GHOST),
                new cmd_type("follow", "do_follow", POS_RESTING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE),
                new cmd_type("gain", "do_gain", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("go", "do_enter", POS_STANDING, 0, LOG_NORMAL, 0, CMD_GHOST),
                new cmd_type("fade", "do_fade", POS_RESTING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE),
                new cmd_type("herbs", "do_herbs", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("hara", "do_hara", POS_STANDING, 0, LOG_NORMAL, 1, 0),

                new cmd_type("hide", "do_hide", POS_RESTING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE),
                new cmd_type("human", "do_human", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("hunt", "do_hunt", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("leave", "do_enter", POS_STANDING, 0, LOG_NORMAL, 1, CMD_GHOST),
                new cmd_type("qui", "do_qui", POS_DEAD, 0, LOG_NORMAL, 0, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("quit", "do_quit", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("recall", "do_recall", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("/", "do_recall", POS_FIGHTING, 0, LOG_NORMAL, 0, 0),
                new cmd_type("rent", "do_rent", POS_DEAD, 0, LOG_NORMAL, 0, 0),
                new cmd_type("save", "do_save", POS_DEAD, 0, LOG_NORMAL, 1, CMD_GHOST),
                new cmd_type("sleep", "do_sleep", POS_SLEEPING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE),
                new cmd_type("slist", "do_slist", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("sneak", "do_sneak", POS_STANDING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE),
                new cmd_type("split", "do_split", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("steal", "do_steal", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("train", "do_train", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("visible", "do_visible", POS_SLEEPING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("wake", "do_wake", POS_SLEEPING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE),
                new cmd_type("wanted", "do_wanted", POS_STANDING, 0, LOG_ALWAYS, 1, 0),
                new cmd_type("where", "do_where", POS_RESTING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),

                /*
                * Immortal commands.
                */
                new cmd_type("advance", "do_advance", POS_DEAD, ML, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("set", "do_set", POS_DEAD, ML, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("dump", "do_dump", POS_DEAD, ML, LOG_ALWAYS, 0, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("rename", "do_rename", POS_DEAD, L5, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("violate", "do_violate", POS_DEAD, ML, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("track", "do_track", POS_STANDING, 0, LOG_NORMAL, 0, 0),
                new cmd_type("trust", "do_trust", POS_DEAD, ML, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),

                new cmd_type("allow", "do_allow", POS_DEAD, L2, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("ban", "do_ban", POS_DEAD, L2, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("deny", "do_deny", POS_DEAD, L1, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("disconnect", "do_disconnect", POS_DEAD, L3, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("flag", "do_flag", POS_DEAD, L4, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("freeze", "do_freeze", POS_DEAD, L7, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("permban", "do_permban", POS_DEAD, L1, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("premort", "do_premort", POS_DEAD, L8, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("protect", "do_protect", POS_DEAD, L1, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("reboo", "do_reboo", POS_DEAD, L1, LOG_NORMAL, 0, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("reboot", "do_reboot", POS_DEAD, L1, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("induct", "do_induct", POS_DEAD, 0, LOG_ALWAYS, 1, 0),
                new cmd_type("grant", "do_grant", POS_DEAD, L2, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("smite", "do_smite", POS_DEAD, L7, LOG_ALWAYS, 1, 0),
                new cmd_type("limited", "do_limited", POS_DEAD, L4, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("lookupRace", "do_slookup", POS_DEAD, L2, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("popularity", "do_popularity", POS_DEAD, L2, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("shutdow", "do_shutdow", POS_DEAD, L1, LOG_NORMAL, 0, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("shutdown", "do_shutdown", POS_DEAD, L1, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("sockets", "do_sockets", POS_DEAD, L4, LOG_NORMAL, 1, 0),
                new cmd_type("wizlock", "do_wizlock", POS_DEAD, L2, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("affrooms", "do_affrooms", POS_DEAD, L4, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("force", "do_force", POS_DEAD, L5, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("load", "do_load", POS_DEAD, L4, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("newlock", "do_newlock", POS_DEAD, L4, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("noaffect", "do_noaffect", POS_DEAD, L5, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("nochannels", "do_nochannels", POS_DEAD, L5, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("notitle", "do_notitle", POS_DEAD, L7, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("noemote", "do_noemote", POS_DEAD, L5, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("noshout", "do_noshout", POS_DEAD, L5, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("notell", "do_notell", POS_DEAD, L5, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("pecho", "do_pecho", POS_DEAD, L4, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("purge", "do_purge", POS_DEAD, L4, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("restore", "do_restore", POS_DEAD, L4, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("sla", "do_sla", POS_DEAD, L3, LOG_NORMAL, 0, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("slay", "do_slay", POS_DEAD, L3, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("teleport", "do_transfer", POS_DEAD, L7, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("transfer", "do_transfer", POS_DEAD, L7, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("poofin", "do_bamfin", POS_DEAD, L8, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("poofout", "do_bamfout", POS_DEAD, L8, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("gecho", "do_echo", POS_DEAD, L4, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("holylight", "do_holylight", POS_DEAD, IM, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("incognito", "do_incognito", POS_DEAD, IM, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("invis", "do_invis", POS_DEAD, IM, LOG_NORMAL, 0, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("log", "do_log", POS_DEAD, L1, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("memory", "do_memory", POS_DEAD, IM, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("mwhere", "do_mwhere", POS_DEAD, IM, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("owhere", "do_owhere", POS_DEAD, IM, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("peace", "do_peace", POS_DEAD, L5, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("penalty", "do_penalty", POS_DEAD, L7, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("echo", "do_recho", POS_DEAD, L6, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("return", "do_return", POS_DEAD, L6, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("snoop", "do_snoop", POS_DEAD, L5, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("stat", "do_stat", POS_DEAD, IM, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("string", "do_string", POS_DEAD, L5, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("switch", "do_switch", POS_DEAD, L6, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("wizinvis", "do_invis", POS_DEAD, IM, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("vnum", "do_vnum", POS_DEAD, L4, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("zecho", "do_zecho", POS_DEAD, L4, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("cabal_scan", "do_cabal_scan", POS_STANDING, 0, LOG_NEVER, 0, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("clone", "do_clone", POS_DEAD, L5, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("wiznet", "do_wiznet", POS_DEAD, IM, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("immtalk", "do_immtalk", POS_DEAD, IM, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("imotd", "do_imotd", POS_DEAD, IM, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type(":", "do_immtalk", POS_DEAD, IM, LOG_NORMAL, 0, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("smote", "do_smote", POS_DEAD, IM, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("prefi", "do_prefi", POS_DEAD, IM, LOG_NORMAL, 0, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("prefix", "do_prefix", POS_DEAD, IM, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("objlist", "do_objlist", POS_DEAD, ML, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("settraps", "do_settraps", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("slook", "do_slook", POS_SLEEPING, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("learn", "do_learn", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("teach", "do_teach", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("camp", "do_camp", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("dig", "do_dig", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("tail", "do_tail", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("push", "do_push", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("demand", "do_demand", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("bandage", "do_bandage", POS_FIGHTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("shoot", "do_shoot", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("maximum", "do_maximum", POS_DEAD, ML, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("find", "do_find", POS_DEAD, ML, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("nscore", "do_score", POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("katana", "do_katana", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("control", "do_control", POS_STANDING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("ititle", "do_ititle", POS_DEAD, IM, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("sense", "do_sense", POS_RESTING, 0, LOG_NORMAL, 1, 0),
                new cmd_type("judge", "do_judge", POS_RESTING, 0, LOG_ALWAYS, 1, CMD_KEEP_HIDE),
                new cmd_type("remor", "do_remor", POS_STANDING, 0, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST),
                new cmd_type("remort", "do_remort", POS_STANDING, 0, LOG_ALWAYS, 1, CMD_KEEP_HIDE | CMD_GHOST)
        };
    }

    static cmd_type[] getCommandsTable() {
        assert (commandsTable != null);
        return commandsTable;
    }

/*
* The main entry point for executing commands.
* Can be recursively called from 'at', 'order', 'force'.
*/

    static void interpret(CHAR_DATA ch, String argument, boolean is_order) {
        /*
        * Strip leading spaces.
        */
        argument = smash_tilde(argument);
        argument = trimSpaces(argument, 0);
        if (argument.length() == 0) {
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
        String logline = argument;
        StringBuilder command = new StringBuilder();
        int pos = 0;
        char c = argument.charAt(0);
        if (!Character.isLetter(c) && !isdigit(c)) {
            command.append(c);
            pos++;
            argument = trimSpaces(argument, pos);
        } else {
            argument = one_argument(argument, command);
        }

        /*
        * Look for command in command table.
        */
        int trust = get_trust(ch);

        cmd_type[] cmd_table = getCommandsTable();
        cmd_type cmd = null;
        for (cmd_type tcmd : cmd_table) {
            if (!str_prefix(command.toString(), tcmd.name) && tcmd.level <= trust) {
                /*
                * Implement charmed mobs commands.
                */
                if (!is_order && IS_AFFECTED(ch, AFF_CHARM)) {
                    send_to_char("First ask to your beloved master!\n", ch);
                    return;
                }

                if (IS_AFFECTED(ch, AFF_STUN) && (tcmd.extra & CMD_KEEP_HIDE) == 0) {
                    send_to_char("You are STUNNED to do that.\n", ch);
                    return;
                }
                /* Come out of hiding for most commands */
                if (IS_AFFECTED(ch, AFF_HIDE) && !IS_NPC(ch) && (tcmd.extra & CMD_KEEP_HIDE) == 0) {
                    ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_HIDE);
                    send_to_char("You step out of the shadows.\n", ch);
                    act("$n steps out of the shadows.", ch, null, null, TO_ROOM);
                }

                if (IS_AFFECTED(ch, AFF_FADE) && !IS_NPC(ch) && (tcmd.extra & CMD_KEEP_HIDE) == 0) {
                    ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_FADE);
                    send_to_char("You step out of the shadows.\n", ch);
                    act("$n steps out of the shadows.", ch, null, null, TO_ROOM);
                }

                if (IS_AFFECTED(ch, AFF_IMP_INVIS) && !IS_NPC(ch) && (tcmd.position == POS_FIGHTING)) {
                    affect_strip(ch, gsn_imp_invis);
                    ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_IMP_INVIS);
                    send_to_char("You fade into existence.\n", ch);
                    act("$n fades into existence.", ch, null, null, TO_ROOM);
                }

                if (IS_AFFECTED(ch, AFF_EARTHFADE) && !IS_NPC(ch) && (tcmd.position == POS_FIGHTING)) {
                    affect_strip(ch, gsn_earthfade);
                    ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_EARTHFADE);
                    WAIT_STATE(ch, (PULSE_VIOLENCE / 2));
                    send_to_char("You slowly fade to your neutral form.\n", ch);
                    act("Earth forms $n in front of you.", ch, null, null, TO_ROOM);
                }

                /* prevent ghosts from doing a bunch of commands */
                if (IS_SET(ch.act, PLR_GHOST) && !IS_NPC(ch) && (tcmd.extra & CMD_GHOST) == 0) {
                    continue;
                }

                cmd = tcmd;
                break;
            }
        }

        /*
        * Log and snoop.
        */
        if (cmd == null || cmd.log == LOG_NEVER) {
            logline = "";
        }

        if (((!IS_NPC(ch) && IS_SET(ch.act, PLR_LOG)) || fLogAll
                || (cmd != null && cmd.log == LOG_ALWAYS) && logline.length() != 0 && logline.charAt(0) != '\n')) {
            String log_buf = "Log " + ch.name + ": " + logline;
            wiznet(log_buf, ch, null, WIZ_SECURE, 0, get_trust(ch));
            log_string(log_buf);
        }

        if (ch.desc != null && ch.desc.snoop_by != null) {
            write_to_buffer(ch.desc.snoop_by, "# ");
            write_to_buffer(ch.desc.snoop_by, logline);
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
                affect_strip(ch, gsn_earthfade);
                ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_EARTHFADE);
                WAIT_STATE(ch, (PULSE_VIOLENCE / 2));
                send_to_char("You slowly fade to your neutral form.\n", ch);
                act("Earth forms $n in front of you.", ch, null, null, TO_ROOM);
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
                case POS_DEAD:
                    send_to_char("Lie still; you are DEAD.\n", ch);
                    break;

                case POS_MORTAL:
                case POS_INCAP:
                    send_to_char("You are hurt far too bad for that.\n", ch);
                    break;

                case POS_STUNNED:
                    send_to_char("You are too stunned to do that.\n", ch);
                    break;

                case POS_SLEEPING:
                    send_to_char("In your dreams, or what?\n", ch);
                    break;

                case POS_RESTING:
                    send_to_char("Nah... You feel too relaxed...\n", ch);
                    break;

                case POS_SITTING:
                    send_to_char("Better stand up first.\n", ch);
                    break;

                case POS_FIGHTING:
                    send_to_char("No way!  You are still fighting!\n", ch);
                    break;

            }
            return;
        }
        if (soc != null) {
            interpret_social(ch, argument, soc);
        } else {
            /* Dispatch the command.*/
            cmd.run(ch, argument);
        }
        tail_chain();
    }


    static social_type lookup_social(String name) {
        for (social_type soc : social_table) {
            if (!str_prefix(name, soc.name)) {
                return soc;
            }
        }
        return null;
    }

    static boolean interpret_social(CHAR_DATA ch, String argument, social_type soc) {
        CHAR_DATA victim;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);
        victim = null;
        if (arg.length() == 0) {
            act(soc.noarg_room, ch, null, victim, TO_ROOM);
            act(soc.noarg_char, ch, null, victim, TO_CHAR);
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
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        act(soc.found_novictim, victim, null, ch, TO_NOTVICT);
                        act(soc.found_char, victim, null, ch, TO_CHAR);
                        act(soc.found_victim, victim, null, ch, TO_VICT);
                        break;
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                        act("$n slaps $N.", victim, null, ch, TO_NOTVICT);
                        act("You slap $N.", victim, null, ch, TO_CHAR);
                        act("$n slaps you.", victim, null, ch, TO_VICT);
                        break;
                }
            }
        }
        return true;
    }

/*
* Given a string like 14.foo, return 14 and 'foo'
*/

    static int number_argument(String argument, StringBuilder arg) {
        return int_argument(argument, arg, '.');
    }

/*
 * Given a string like 14*foo, return 14 and 'foo'
*/

    static int mult_argument(String argument, StringBuilder arg) {
        return int_argument(argument, arg, '*');
    }

    static int int_argument(String argument, StringBuilder arg, char c) {
        int pdot = argument.indexOf(c);
        int number = 1;
        if (pdot > 0) {
            try {
                number = Integer.parseInt(argument.substring(0, pdot));
            } catch (NumberFormatException e) {
                number = 0;
            }
        }
        arg.append(argument, pdot + 1, argument.length());
        return number;
    }

    
/*
 * Contributed by Alander.
 */

    static void do_commands(CHAR_DATA ch, String argument) {
        StringBuilder buf = new StringBuilder();
        Formatter f = null;
        int col = 0;
        cmd_type[] cmd_table = getCommandsTable();
        for (char letter = 'a'; letter <= 'z'; letter++) {
            for (int cmd = 0; cmd_table[cmd].name.length() != 0; cmd++) {
                if (cmd_table[cmd].name.charAt(0) == letter
                        && cmd_table[cmd].level < LEVEL_HERO
                        && cmd_table[cmd].level <= get_trust(ch)
                        && cmd_table[cmd].show != 0) {
                    if (f == null) {
                        f = new Formatter(buf);
                    }
                    f.format("%-12s", cmd_table[cmd].name);
                    if (++col % 6 == 0) {
                        buf.append("\n");
                    }
                }
            }
        }

        if (col % 6 != 0) {
            buf.append("\n");
        }

        page_to_char(buf.toString(), ch);
    }

    static void do_wizhelp(CHAR_DATA ch, String argument) {
        StringBuilder buf = new StringBuilder();
        Formatter f = null;
        int col = 0;
        cmd_type[] cmd_table = getCommandsTable();
        for (char letter = 'a'; letter <= 'z'; letter++) {
            for (int cmd = 0; cmd_table[cmd].name.length() != 0; cmd++) {
                if (cmd_table[cmd].name.charAt(0) == letter
                        && cmd_table[cmd].level >= LEVEL_HERO
                        && cmd_table[cmd].level <= get_trust(ch)
                        && cmd_table[cmd].show != 0) {
                    if (f == null) {
                        f = new Formatter(buf);
                    }
                    f.format("%-12s", cmd_table[cmd].name);
                    if (++col % 6 == 0) {
                        buf.append("\n");
                    }
                }
            }
        }

        if (col % 6 != 0) {
            buf.append("\n");
        }

        page_to_char(buf.toString(), ch);
    }


    static void do_reture(CHAR_DATA ch, String argument) {
        send_to_char("Ok.\n", ch);
    }

/* does aliasing and other fun stuff */

    static void substitute_alias(DESCRIPTOR_DATA d, String argument) {
        int alias;
        CHAR_DATA ch = d.original != null ? d.original : d.character;

        /* check for prefix */
        if (ch.prefix.length() != 0 && str_prefix("prefix", argument)) {
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

        StringBuilder buf = new StringBuilder(argument);

        /* go through the aliases */
        for (alias = 0; alias < nw_config.max_alias; alias++) {
            if (ch.pcdata.alias[alias] == null) {
                break;
            }

            if (!str_prefix(ch.pcdata.alias[alias], argument)) {
                StringBuilder name = new StringBuilder();
                String point = one_argument(argument, name);
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

    static void do_alia(CHAR_DATA ch, String argument) {
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

        StringBuilder argb = new StringBuilder();
        argument = one_argument(argument, argb);


        if (argb.length() == 0) {

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

                String buf = "    " + rch.pcdata.alias[pos] + ":  " + rch.pcdata.alias_sub[pos] + "\n";
                send_to_char(buf, ch);
            }
            return;
        }
        String arg = argb.toString();

        if (!str_prefix("una", arg) || !str_cmp("alias", arg)) {
            send_to_char("Sorry, that word is reserved.\n", ch);
            return;
        }

        if (argument.length() == 0) {
            for (pos = 0; pos < nw_config.max_alias; pos++) {
                if (rch.pcdata.alias[pos] == null
                        || rch.pcdata.alias_sub[pos] == null) {
                    break;
                }

                if (!str_cmp(arg, rch.pcdata.alias[pos])) {
                    String buf = rch.pcdata.alias[pos] + " aliases to '" + rch.pcdata.alias_sub[pos] + "'.\n";
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
                send_to_char(arg + " is now realiased to '" + argument + "'.\n", ch);
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
        boolean found = false;
        CHAR_DATA rch;
        if (ch.desc == null) {
            rch = ch;
        } else {
            rch = ch.desc.original != null ? ch.desc.original : ch;
        }

        if (IS_NPC(rch)) {
            return;
        }

        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0) {
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

            if (arg.equals(rch.pcdata.alias[pos])) {
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
