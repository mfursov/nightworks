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

import net.sf.nightworks.util.TextBuffer;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import static net.sf.nightworks.Const.dex_app;
import static net.sf.nightworks.Const.str_app;
import static net.sf.nightworks.Handler.can_see;
import static net.sf.nightworks.Handler.get_curr_stat;
import static net.sf.nightworks.Handler.get_trust;
import static net.sf.nightworks.Skill.MAX_SKILLS;

/**
 * Last mofified:           $Date: 2006-07-20 07:41:52 -0700 (Thu, 20 Jul 2006) $
 * Revision of last commit: $Revision: 25 $
 */
final class Nightworks {


    static final int MAX_ALIAS = 50;
    static final int MAX_TIME_LOG = 30;
    static final int MAX_TIME_LIMIT = 43200;    /* 720 Hours */

    static final String DEFAULT_PROMPT = "<{c%n{x: %h/%Hhp %m/%Mm %v/%Vmv tnl:%X {c%e{x Opp:%o> ";
/*
 * Function types.
 */

    interface SPEC_FUN {
        boolean run(CHAR_DATA ch);

        String getName();
    }

    interface DO_FUN {
        void run(CHAR_DATA ch, String argument);
    }

    interface MPROG_FUN_BRIBE {
        void run(CHAR_DATA mob, CHAR_DATA ch, int amount);
    }

    interface MPROG_FUN_ENTRY {
        void run(CHAR_DATA mob);
    }

    interface MPROG_FUN_GREET {
        void run(CHAR_DATA mob, CHAR_DATA ch);
    }

    interface MPROG_FUN_GIVE {
        void run(CHAR_DATA mob, CHAR_DATA ch, OBJ_DATA obj);
    }


    interface MPROG_FUN_FIGHT {
        void run(CHAR_DATA mob, CHAR_DATA victim);
    }


    interface MPROG_FUN_DEATH {
        boolean run(CHAR_DATA mob);
    }

    interface MPROG_FUN_AREA {
        void run(CHAR_DATA mob);
    }

    interface MPROG_FUN_SPEECH {
        void run(CHAR_DATA mob, CHAR_DATA ch, String speech);
    }


    interface OPROG_FUN_WEAR {
        void run(OBJ_DATA obj, CHAR_DATA ch);
    }

    interface OPROG_FUN_REMOVE {
        void run(OBJ_DATA obj, CHAR_DATA ch);
    }

    interface OPROG_FUN_GET {
        void run(OBJ_DATA obj, CHAR_DATA ch);
    }

    interface OPROG_FUN_DROP {
        void run(OBJ_DATA obj, CHAR_DATA ch);
    }

    interface OPROG_FUN_SAC {
        boolean run(OBJ_DATA obj, CHAR_DATA ch);
    }

    interface OPROG_FUN_ENTRY {
        void run(OBJ_DATA obj);
    }

    interface OPROG_FUN_GIVE {
        void run(OBJ_DATA obj, CHAR_DATA from, CHAR_DATA to);
    }

    interface OPROG_FUN_GREET {
        void run(OBJ_DATA obj, CHAR_DATA ch);
    }

    interface OPROG_FUN_FIGHT {
        void run(OBJ_DATA obj, CHAR_DATA ch);
    }

    interface OPROG_FUN_DEATH {
        boolean run(OBJ_DATA obj, CHAR_DATA ch);
    }

    interface OPROG_FUN_SPEECH {
        void run(OBJ_DATA obj, CHAR_DATA ch, String speech);
    }

    interface OPROG_FUN_AREA {
        void run(OBJ_DATA obj);
    }

    /*
    *  COMMAND extra bits..
    */
    static final int CMD_KEEP_HIDE = 1;
    static final int CMD_GHOST = 2;

    /*
    * String and memory management parameters.
    */
    static final int MAX_KEY_HASH = 1024;
    static final int MAX_STRING_LENGTH = 4608;
    static final int MAX_INPUT_LENGTH = 256;
    static final int PAGELEN = 22;

/* RT ASCII conversions -- used so we can have letters in this file */

    static final int A = 1;
    static final int B = 1 << 1;
    static final int C = 1 << 2;
    static final int D = 1 << 3;
    static final int E = 1 << 4;
    static final int F = 1 << 5;
    static final int G = 1 << 6;
    static final int H = 1 << 7;

    static final int I = 1 << 8;
    static final int J = 1 << 9;
    static final int K = 1 << 10;
    static final int L = 1 << 11;
    static final int M = 1 << 12;
    static final int N = 1 << 13;
    static final int O = 1 << 14;
    static final int P = 1 << 15;

    static final int Q = 1 << 16;
    static final int R = 1 << 17;
    static final int S = 1 << 18;
    static final int T = 1 << 19;
    static final int U = 1 << 20;
    static final int V = 1 << 21;
    static final int W = 1 << 22;
    static final int X = 1 << 23;

    static final int Y = 1 << 24;
    static final int Z = 1 << 25;
    static final int BIT_26 = 1 << 26;
    static final int BIT_27 = 1 << 27;
    static final int BIT_28 = 1 << 28;
    static final int BIT_29 = 1 << 29;
    static final int BIT_30 = 1 << 30;
    static final long BIT_31 = 1L << 31;
    static final long BIT_32 = 1L << 32;
    static final long BIT_33 = 1L << 33;
    static final long BIT_34 = 1L << 34;
    static final long BIT_35 = 1L << 35;
    static final long BIT_36 = 1L << 36;
    static final long BIT_37 = 1L << 37;
    static final long BIT_38 = 1L << 38;
    static final long BIT_39 = 1L << 39;
    static final long BIT_40 = 1L << 40;
    static final long BIT_41 = 1L << 41;
    static final long BIT_42 = 1L << 42;
    static final long BIT_43 = 1L << 43;
    static final long BIT_44 = 1L << 44;
    static final long BIT_45 = 1L << 45;
    static final long BIT_46 = 1L << 46;
    static final long BIT_47 = 1L << 47;
    static final long BIT_48 = 1L << 48;
    static final long BIT_49 = 1L << 49;
    static final long BIT_50 = 1L << 50;
    static final long BIT_51 = 1L << 51;
    static final long BIT_52 = 1L << 52;
    static final long BIT_53 = 1L << 53;
    static final long BIT_54 = 1L << 54;
    static final long BIT_55 = 1L << 55;
    static final long BIT_56 = 1L << 56;
    static final long BIT_57 = 1L << 57;
    static final long BIT_58 = 1L << 58;
    static final long BIT_59 = 1L << 59;

    static final int MPROG_BRIBE = (A);
    static final int MPROG_ENTRY = (B);
    static final int MPROG_GREET = (C);
    static final int MPROG_GIVE = (D);
    static final int MPROG_FIGHT = (E);
    static final int MPROG_DEATH = (F);
    static final int MPROG_AREA = (G);
    static final int MPROG_SPEECH = (H);

    static final int OPROG_WEAR = (A);
    static final int OPROG_REMOVE = (B);
    static final int OPROG_DROP = (C);
    static final int OPROG_SAC = (D);
    static final int OPROG_GIVE = (E);
    static final int OPROG_GREET = (F);
    static final int OPROG_FIGHT = (G);
    static final int OPROG_DEATH = (H);
    static final int OPROG_SPEECH = (I);
    static final int OPROG_ENTRY = (J);
    static final int OPROG_GET = (K);
    static final int OPROG_AREA = (L);

    /*
    * Game parameters.
    */
    static final int NIGHTWORKS_REBOOT = 0;
    static final int NIGHTWORKS_SHUTDOWN = 1;

    private static final int MAX_SOCIALS = 256;
    static final int MAX_SKILL = 426;
    static final int MAX_CLASS = 13;
    static final int MAX_CABAL = 9;
    static final int MAX_RELIGION = 18;
    static final int MAX_LEVEL = 100;
    static final int LEVEL_HERO = (MAX_LEVEL - 9);
    static final int LEVEL_IMMORTAL = (MAX_LEVEL - 8);

    static final int PULSE_PER_SCD = 6  /* 6 for comm.c */;
    static final int PULSE_PER_SECOND = 4  /* for update.c */;
    static final int PULSE_VIOLENCE = (2 * PULSE_PER_SECOND);


    static final int PULSE_MOBILE = (4 * PULSE_PER_SECOND);
    static final int PULSE_WATER_FLOAT = (4 * PULSE_PER_SECOND);
    static final int PULSE_MUSIC = (6 * PULSE_PER_SECOND);
    static final int PULSE_TRACK = (6 * PULSE_PER_SECOND);
    static final int PULSE_TICK = (50 * PULSE_PER_SECOND) /* 36 seconds */;

    /* room_affect_update (not room_update) */
    static final int PULSE_RAFFECT = (3 * PULSE_MOBILE);
    static final int PULSE_AREA = (110 * PULSE_PER_SECOND) /* 97 seconds */;
    static final int FIGHT_DELAY_TIME = (20 * PULSE_PER_SECOND);
    static final int PULSE_AUCTION = (10 * PULSE_PER_SECOND) /* 10 seconds */;

    static final int IMPLEMENTOR = MAX_LEVEL;
    static final int CREATOR = (MAX_LEVEL - 1);
    static final int SUPREME = (MAX_LEVEL - 2);
    static final int DEITY = (MAX_LEVEL - 3);
    static final int GOD = (MAX_LEVEL - 4);
    static final int IMMORTAL = (MAX_LEVEL - 5);
    static final int DEMI = (MAX_LEVEL - 6);
    static final int ANGEL = (MAX_LEVEL - 7);
    static final int AVATAR = (MAX_LEVEL - 8);
    static final int HERO = LEVEL_HERO;


    static final int CABAL_NONE = 0;
    static final int CABAL_RULER = 1;
    static final int CABAL_INVADER = 2;
    static final int CABAL_CHAOS = 3;
    static final int CABAL_SHALAFI = 4;
    static final int CABAL_BATTLE = 5;
    static final int CABAL_KNIGHT = 6;
    static final int CABAL_LIONS = 7;
    static final int CABAL_HUNTER = 8;

    static final int ETHOS_ANY = 0;
    static final int ETHOS_LAWFUL = 1;
    static final int ETHOS_NEUTRAL = 2;
    static final int ETHOS_CHAOTIC = 3;


/*
 * Cabal structure
 */

    static final class cabal_type {
        final String long_name;
        final String short_name;
        final int obj_vnum;
        final int room_vnum;
        OBJ_DATA obj_ptr;

        cabal_type(String long_name, String short_name, int obj_vnum, int room_vnum, OBJ_DATA obj_ptr) {
            this.long_name = long_name;
            this.short_name = short_name;
            this.obj_vnum = obj_vnum;
            this.room_vnum = room_vnum;
            this.obj_ptr = obj_ptr;
        }
    }

    static final class color_type {
        final String name;
        final String code;

        color_type(String name, String code) {
            this.name = name;
            this.code = code;
        }
    }

    static final int RELIGION_NONE = 0;
    static final int RELIGION_APOLLON = 1;
    static final int RELIGION_ZEUS = 2;
    static final int RELIGION_SIEBELE = 3;
    static final int RELIGION_HEPHAESTUS = 4;
    static final int RELIGION_EHRUMEN = 5;
    static final int RELIGION_AHRUMAZDA = 6;
    static final int RELIGION_DEIMOS = 7;
    static final int RELIGION_PHOBOS = 8;
    static final int RELIGION_ODIN = 9;
    static final int RELIGION_MARS = 10;
    static final int RELIGION_ATHENA = 11;
    static final int RELIGION_GOKTENGRI = 12;
    static final int RELIGION_HERA = 13;
    static final int RELIGION_VENUS = 14;
    static final int RELIGION_ARES = 15;
    static final int RELIGION_PROMETHEUS = 16;
    static final int RELIGION_EROS = 17;

/* Religion structure */

    static final class religion_type {
        final String leader;
        final String name;
        final int tattoo_vnum;

        religion_type(String leader, String name, int tattoo_vnum) {
            this.leader = leader;
            this.name = name;
            this.tattoo_vnum = tattoo_vnum;
        }
    }

    /*
    *  minimum pk level
    */
    static final int PK_MIN_LEVEL = 5;

    static final int MAX_NEWBIES = 120;   /* number of newbies allowed */
    static final int MAX_OLDIES = 999;  /* number of oldies allowed */

/*
 * Site ban structure.
 */

    static final int BAN_SUFFIX = A;
    static final int BAN_PREFIX = B;
    static final int BAN_NEWBIES = C;
    static final int BAN_ALL = D;
    static final int BAN_PERMIT = E;
    static final int BAN_PERMANENT = F;
    static final int BAN_PLAYER = G;

    static final class hometown_type {
        final String name;
        final int[] altar;
        final int[] recall;
        final int[] pit;
        /* good, neutral, evil */

        hometown_type(String name, int[] altar, int[] recall, int[] pit) {
            this.name = name;
            this.altar = altar;
            this.recall = recall;
            this.pit = pit;
        }

    }

    static final class BAN_DATA {
        BAN_DATA next;
        boolean valid;
        int ban_flags;
        int level;
        String name = "";
    }

    /*
    * Time and weather stuff.
    */
    static final int SUN_DARK = 0;
    static final int SUN_RISE = 1;
    static final int SUN_LIGHT = 2;
    static final int SUN_SET = 3;

    static final int SKY_CLOUDLESS = 0;
    static final int SKY_CLOUDY = 1;
    static final int SKY_RAINING = 2;
    static final int SKY_LIGHTNING = 3;

    static final class TIME_INFO_DATA {
        int bmin;
        int hour;
        int day;
        int month;
        int year;
    }

    static final class WEATHER_DATA {
        int mmhg;
        int change;
        int sky;
        int sunlight;
    }

    /*
    * Connected state for a channel.
    */
    static final int CON_PLAYING = 0;
    static final int CON_GET_NAME = 1;
    static final int CON_GET_OLD_PASSWORD = 2;
    static final int CON_CONFIRM_NEW_NAME = 3;
    static final int CON_GET_NEW_PASSWORD = 4;
    static final int CON_CONFIRM_NEW_PASSWORD = 5;
    static final int CON_GET_NEW_RACE = 6;
    static final int CON_GET_NEW_SEX = 7;
    static final int CON_GET_NEW_CLASS = 8;
    static final int CON_GET_ALIGNMENT = 9;
    static final int CON_DEFAULT_CHOICE = 10;
    static final int CON_GEN_GROUPS = 11;
    static final int CON_PICK_WEAPON = 12;
    static final int CON_READ_IMOTD = 13;
    static final int CON_READ_MOTD = 14;
    static final int CON_BREAK_CONNECT = 15;
    static final int CON_ROLL_STATS = 16;
    static final int CON_ACCEPT_STATS = 17;
    static final int CON_PICK_HOMETOWN = 18;
    static final int CON_GET_ETHOS = 19;
    static final int CON_CREATE_DONE = 20;
    static final int CON_READ_NEWBIE = 21;
    static final int CON_REMORTING = 22;

/*
* Descriptor (channel) structure.
*/

    static final class DESCRIPTOR_DATA {
        DESCRIPTOR_DATA next;
        DESCRIPTOR_DATA snoop_by;
        CHAR_DATA character;
        CHAR_DATA original;

        boolean valid;
        String host;
        SocketChannel descriptor;
        int connected;
        boolean fcommand;

        final StringBuilder inbuf = new StringBuilder();
        /**
         * command to execute
         */
        String incomm = "";
        /**
         * the last executed command
         */
        String inlast = "";
        int repeat;

        ArrayList<Object> outbuf = new ArrayList<Object>();
        String showstr_head;
        /**
         * pointer inside showstr_head
         */
        int showstr_point;
    }

/*
* Attribute bonus structures.
*/

    static final class str_app_type {
        final int tohit;
        final int todam;
        final int carry;
        final int wield;

        public str_app_type(int tohit, int todam, int carry, int wield) {
            this.tohit = tohit;
            this.todam = todam;
            this.carry = carry;
            this.wield = wield;
        }
    }

    static final class int_app_type {
        final int learn;

        public int_app_type(int learn) {
            this.learn = learn;
        }
    }

    static final class wis_app_type {
        final int practice;

        public wis_app_type(int practice) {
            this.practice = practice;
        }
    }

    static final class dex_app_type {
        final int defensive;

        public dex_app_type(int defensive) {
            this.defensive = defensive;
        }
    }

    static final class con_app_type {
        final int hitp;
        final int shock;

        public con_app_type(int hitp, int shock) {
            this.hitp = hitp;
            this.shock = shock;
        }
    }

    /*
    * TO types for act.
    */
    static final int TO_ROOM = 0;
    static final int TO_NOTVICT = 1;
    static final int TO_VICT = 2;
    static final int TO_CHAR = 3;
    static final int TO_ALL = 4;

/*
* Help table types.
*/

    static final class HELP_DATA {
        HELP_DATA next;
        int level;
        String keyword;
        String text;
    }

    /*
    * Shop types.
    */
    static final int MAX_TRADE = 5;

    static final class SHOP_DATA {
        SHOP_DATA next;           /* Next shop in list        */
        int keeper;         /* Vnum of shop keeper mob  */
        int buy_type[] = new int[MAX_TRADE];   /* Item types shop will buy */
        int profit_buy;     /* Cost multiplier for buying   */
        int profit_sell;        /* Cost multiplier for selling  */
        int open_hour;      /* First opening hour       */
        int close_hour;     /* First closing hour       */
    }

/*
* Per-class stuff.
*/

    static final int MAX_GUILD = 6;
    static final int MAX_STATS = 6;
    static final int STAT_STR = 0;
    static final int STAT_INT = 1;
    static final int STAT_WIS = 2;
    static final int STAT_DEX = 3;
    static final int STAT_CON = 4;
    static final int STAT_CHA = 5;


    static final int LANG_COMMON = 0;
    static final int LANG_HUMAN = 1;
    static final int LANG_ELVISH = 2;
    static final int LANG_DWARVISH = 3;
    static final int LANG_GNOMISH = 4;
    static final int LANG_GIANT = 5;
    static final int LANG_TROLLISH = 6;
    static final int LANG_CAT = 7;
    static final int MAX_LANGUAGE = 8;

    static final class item_type {
        final int type;
        final String name;

        item_type(int type, String name) {
            this.type = type;
            this.name = name;
        }
    }


    static final class wiznet_type {
        final String name;
        final int flag;
        final int level;

        wiznet_type(String name, int flag, int level) {
            this.name = name;
            this.flag = flag;
            this.level = level;
        }
    }

    static final class attack_type {
        final String name;           /* name */
        final String noun;           /* message */
        final int damage;         /* damage class */

        attack_type(String name, String noun, int damage) {
            this.name = name;
            this.noun = noun;
            this.damage = damage;
        }
    }

    static final class prac_type {
        final String sh_name;
        final String name;
        final int number;

        prac_type(String sh_name, String name, int number) {
            this.sh_name = sh_name;
            this.name = name;
            this.number = number;
        }
    }

/*
* Data structure for notes.
*/

    static final int NOTE_NOTE = 0;
    static final int NOTE_IDEA = 1;
    static final int NOTE_PENALTY = 2;
    static final int NOTE_NEWS = 3;
    static final int NOTE_CHANGES = 4;
    static final int NOTE_INVALID = 100;

    static final class NOTE_DATA {
        NOTE_DATA next;
        boolean valid;
        int type;
        String sender;
        String date;
        String to_list;
        String subject;
        String text;
        int date_stamp;
    }

/*
* An affect.
*/

    static final class AFFECT_DATA {
        AFFECT_DATA next;
        boolean valid;
        int where;
        Skill type;
        int level;
        int duration;
        int location;
        int modifier;
        Object objModifier;
        long bitvector;

        final void assignValuesFrom(AFFECT_DATA source) {
            next = source.next;
            valid = source.valid;
            where = source.where;
            type = source.type;
            level = source.level;
            duration = source.duration;
            location = source.location;
            modifier = source.modifier;
            objModifier = source.objModifier;
            bitvector = source.bitvector;

        }


    }


    /* where definitions */
    static final int TO_AFFECTS = 0;
    static final int TO_OBJECT = 1;
    static final int TO_IMMUNE = 2;
    static final int TO_RESIST = 3;
    static final int TO_VULN = 4;
    static final int TO_WEAPON = 5;
    static final int TO_ACT_FLAG = 6;
    static final int TO_RACE = 8;

    /* where definitions for room */
    static final int TO_ROOM_AFFECTS = 0;
    static final int TO_ROOM_CONST = 1;
    static final int TO_ROOM_FLAGS = 2;

    /* room applies */
    static final int APPLY_ROOM_NONE = 0;
    static final int APPLY_ROOM_HEAL = 1;
    static final int APPLY_ROOM_MANA = 2;

/*
 * A kill structure (indexed by level).
 */

    static final class KILL_DATA {
        int number;
        int killed;
    }


    /**
     * ************************************************************************
     * *
     * VALUES OF INTEREST TO AREA BUILDERS                   *
     * (Start of section ... start here)                     *
     * *
     * *************************************************************************
     */

/*
 * Well known mob virtual numbers.
 * Defined in #MOBILES.
 */
    static final int MOB_VNUM_CITYGUARD = 3060;
    static final int MOB_VNUM_VAGABOND = 3063;
    static final int MOB_VNUM_CAT = 3066;
    static final int MOB_VNUM_FIDO = 3062;
    static final int MOB_VNUM_SAGE = 3162;

    static final int MOB_VNUM_VAMPIRE = 3404;

    static final int MOB_VNUM_SHADOW = 10;
    static final int MOB_VNUM_SPECIAL_GUARD = 11;
    static final int MOB_VNUM_BEAR = 12;
    static final int MOB_VNUM_DEMON = 13;
    static final int MOB_VNUM_NIGHTWALKER = 14;
    static final int MOB_VNUM_STALKER = 15;
    static final int MOB_VNUM_SQUIRE = 16;
    static final int MOB_VNUM_MIRROR_IMAGE = 17;
    static final int MOB_VNUM_UNDEAD = 18;
    static final int MOB_VNUM_LION = 19;
    static final int MOB_VNUM_WOLF = 20;

    static final int MOB_VNUM_LESSER_GOLEM = 21;
    static final int MOB_VNUM_STONE_GOLEM = 22;
    static final int MOB_VNUM_IRON_GOLEM = 23;
    static final int MOB_VNUM_ADAMANTITE_GOLEM = 24;

    static final int MOB_VNUM_HUNTER = 25;
    static final int MOB_VNUM_SUM_SHADOW = 26;
    static final int MOB_VNUM_DOG = 27;

    static final int MOB_VNUM_ELM_EARTH = 28;
    static final int MOB_VNUM_ELM_AIR = 29;
    static final int MOB_VNUM_ELM_FIRE = 30;
    static final int MOB_VNUM_ELM_WATER = 31;
    static final int MOB_VNUM_ELM_LIGHT = 32;

    static final int MOB_VNUM_WEAPON = 33;
    static final int MOB_VNUM_ARMOR = 34;

    static final int MOB_VNUM_PATROLMAN = 2106;
    static final int GROUP_VNUM_TROLLS = 2100;
    static final int GROUP_VNUM_OGRES = 2101;


    /* general align */
    static final int ALIGN_NONE = -1;
    static final int ALIGN_GOOD = 1000;
    static final int ALIGN_NEUTRAL = 0;
    static final int ALIGN_EVIL = -1000;

    /* binary align for race and class */
    static final int CR_ALL = (0);
    static final int CR_GOOD = (A);
    static final int CR_NEUTRAL = (B);
    static final int CR_EVIL = (C);

    /* number align */
    static final int N_ALIGN_ALL = 0;
    static final int N_ALIGN_GOOD = 1;
    static final int N_ALIGN_NEUTRAL = 2;
    static final int N_ALIGN_EVIL = 3;

    /* group number for mobs */
    static final int GROUP_NONE = 0;
    static final int GROUP_WEAPONSMASTER = 1;
    static final int GROUP_ATTACK = 2;
    static final int GROUP_BEGUILING = 3;
    static final int GROUP_BENEDICTIONS = 4;
    static final int GROUP_COMBAT = 5;
    static final int GROUP_CREATION = 6;
    static final int GROUP_CURATIVE = 7;
    static final int GROUP_DETECTION = 8;
    static final int GROUP_DRACONIAN = 9;
    static final int GROUP_ENCHANTMENT = 10;
    static final int GROUP_ENHANCEMENT = 11;
    static final int GROUP_HARMFUL = 12;
    static final int GROUP_HEALING = 13;
    static final int GROUP_ILLUSION = 14;
    static final int GROUP_MALADICTIONS = 15;
    static final int GROUP_PROTECTIVE = 16;
    static final int GROUP_TRANSPORTATION = 17;
    static final int GROUP_WEATHER = 18;
    static final int GROUP_FIGHTMASTER = 19;
    static final int GROUP_SUDDENDEATH = 20;
    static final int GROUP_MEDITATION = 21;
    static final int GROUP_CABAL = 22;
    static final int GROUP_DEFENSIVE = 23;
    static final int GROUP_WIZARD = 24;

    /* group bits for mobs */
    static final int GFLAG_NONE = 0;
    static final int GFLAG_WEAPONSMASTER = (A);
    static final int GFLAG_ATTACK = (B);
    static final int GFLAG_BEGUILING = (C);
    static final int GFLAG_BENEDICTIONS = (D);
    static final int GFLAG_COMBAT = (E);
    static final int GFLAG_CREATION = (F);
    static final int GFLAG_CURATIVE = (G);
    static final int GFLAG_DETECTION = (H);
    static final int GFLAG_DRACONIAN = (I);
    static final int GFLAG_ENCHANTMENT = (J);
    static final int GFLAG_ENHANCEMENT = (K);
    static final int GFLAG_HARMFUL = (L);
    static final int GFLAG_HEALING = (M);
    static final int GFLAG_ILLUSION = (N);
    static final int GFLAG_MALADICTIONS = (O);
    static final int GFLAG_PROTECTIVE = (P);
    static final int GFLAG_TRANSPORTATION = (Q);
    static final int GFLAG_WEATHER = (R);
    static final int GFLAG_FIGHTMASTER = (S);
    static final int GFLAG_SUDDENDEATH = (T);
    static final int GFLAG_MEDITATION = (U);
    static final int GFLAG_CABAL = (V);
    static final int GFLAG_DEFENSIVE = (W);
    static final int GFLAG_WIZARD = (X);

    /*
    * AREA FLAGS
    */
    static final int AREA_HOMETOWN = (A);
    static final int AREA_PROTECTED = (B);

    /*
    * ACT bits for mobs.  *ACT*
    * Used in #MOBILES.
    */
    static final int ACT_IS_NPC = (A);/* Auto set for mobs    */
    static final int ACT_SENTINEL = (B);    /* Stays in one room    */
    static final int ACT_SCAVENGER = (C);     /* Picks up objects */
    static final int ACT_AGGRESSIVE = (F);      /* Attacks PC's     */
    static final int ACT_STAY_AREA = (G);   /* Won't leave area */
    static final int ACT_WIMPY = (H);
    static final int ACT_PET = (I);/* Auto set for pets    */
    static final int ACT_TRAIN = (J); /* Can train PC's   */
    static final int ACT_PRACTICE = (K);     /* Can practice PC's    */
    static final int ACT_HUNTER = (L);
    static final int ACT_UNDEAD = (O);
    static final int ACT_CLERIC = (Q);
    static final int ACT_MAGE = (R);
    static final int ACT_THIEF = (S);
    static final int ACT_WARRIOR = (T);
    static final int ACT_NOALIGN = (U);
    static final int ACT_NOPURGE = (V);
    static final int ACT_OUTDOORS = (W);
    static final int ACT_INDOORS = (Y);
    static final int ACT_RIDEABLE = (Z);
    static final int ACT_IS_HEALER = BIT_26;
    static final int ACT_GAIN = BIT_27;
    static final int ACT_UPDATE_ALWAYS = BIT_28;
    static final int ACT_IS_CHANGER = BIT_29;
    static final int ACT_NOTRACK = BIT_30;
    ;

    /* damage classes */
    static final int DAM_NONE = 0;
    static final int DAM_BASH = 1;
    static final int DAM_PIERCE = 2;
    static final int DAM_SLASH = 3;
    static final int DAM_FIRE = 4;
    static final int DAM_COLD = 5;
    static final int DAM_LIGHTNING = 6;
    static final int DAM_ACID = 7;
    static final int DAM_POISON = 8;
    static final int DAM_NEGATIVE = 9;
    static final int DAM_HOLY = 10;
    static final int DAM_ENERGY = 11;
    static final int DAM_MENTAL = 12;
    static final int DAM_DISEASE = 13;
    static final int DAM_DROWNING = 14;
    static final int DAM_LIGHT = 15;
    static final int DAM_OTHER = 16;
    static final int DAM_HARM = 17;
    static final int DAM_CHARM = 18;
    static final int DAM_SOUND = 19;
    static final int DAM_THIRST = 20;
    static final int DAM_HUNGER = 21;
    static final int DAM_LIGHT_V = 22;
    static final int DAM_TRAP_ROOM = 23;

    /* OFF bits for mobiles *OFF  */
    static final int OFF_AREA_ATTACK = (A);
    static final int OFF_BACKSTAB = (B);
    static final int OFF_BASH = (C);
    static final int OFF_BERSERK = (D);
    static final int OFF_DISARM = (E);
    static final int OFF_DODGE = (F);
    static final int OFF_FADE = (G);
    static final int OFF_FAST = (H);
    static final int OFF_KICK = (I);
    static final int OFF_KICK_DIRT = (J);
    static final int OFF_PARRY = (K);
    static final int OFF_RESCUE = (L);
    static final int OFF_TAIL = (M);
    static final int OFF_TRIP = (N);
    static final int OFF_CRUSH = (O);
    static final int ASSIST_ALL = (P);
    static final int ASSIST_ALIGN = (Q);
    static final int ASSIST_RACE = (R);
    static final int ASSIST_PLAYERS = (S);
    static final int ASSIST_GUARD = (T);
    static final int ASSIST_VNUM = (U);

    /* return values for check_imm */
    static final int IS_NORMAL = 0;
    static final int IS_IMMUNE = 1;
    static final int IS_RESISTANT = 2;
    static final int IS_VULNERABLE = 3;

    /* IMM bits for mobs */
    static final int IMM_SUMMON = (A);
    static final int IMM_CHARM = (B);
    static final int IMM_MAGIC = (C);
    static final int IMM_WEAPON = (D);
    static final int IMM_BASH = (E);
    static final int IMM_PIERCE = (F);
    static final int IMM_SLASH = (G);
    static final int IMM_FIRE = (H);
    static final int IMM_COLD = (I);
    static final int IMM_LIGHTNING = (J);
    static final int IMM_ACID = (K);
    static final int IMM_POISON = (L);
    static final int IMM_NEGATIVE = (M);
    static final int IMM_HOLY = (N);
    static final int IMM_ENERGY = (O);
    static final int IMM_MENTAL = (P);
    static final int IMM_DISEASE = (Q);
    static final int IMM_DROWNING = (R);
    static final int IMM_LIGHT = (S);
    static final int IMM_SOUND = (T);
    static final int IMM_WOOD = (X);
    static final int IMM_SILVER = (Y);
    static final int IMM_IRON = (Z);

    /* RES bits for mobs *RES */
    static final int RES_SUMMON = (A);
    static final int RES_CHARM = (B);
    static final int RES_MAGIC = (C);
    static final int RES_WEAPON = (D);
    static final int RES_BASH = (E);
    static final int RES_PIERCE = (F);
    static final int RES_SLASH = (G);
    static final int RES_FIRE = (H);
    static final int RES_COLD = (I);
    static final int RES_LIGHTNING = (J);
    static final int RES_ACID = (K);
    static final int RES_POISON = (L);
    static final int RES_NEGATIVE = (M);
    static final int RES_HOLY = (N);
    static final int RES_ENERGY = (O);
    static final int RES_MENTAL = (P);
    static final int RES_DISEASE = (Q);
    static final int RES_DROWNING = (R);
    static final int RES_LIGHT = (S);
    static final int RES_SOUND = (T);
    static final int RES_WOOD = (X);
    static final int RES_SILVER = (Y);
    static final int RES_IRON = (Z);

    /* VULN bits for mobs */
    static final int VULN_SUMMON = (A);
    static final int VULN_CHARM = (B);
    static final int VULN_MAGIC = (C);
    static final int VULN_WEAPON = (D);
    static final int VULN_BASH = (E);
    static final int VULN_PIERCE = (F);
    static final int VULN_SLASH = (G);
    static final int VULN_FIRE = (H);
    static final int VULN_COLD = (I);
    static final int VULN_LIGHTNING = (J);
    static final int VULN_ACID = (K);
    static final int VULN_POISON = (L);
    static final int VULN_NEGATIVE = (M);
    static final int VULN_HOLY = (N);
    static final int VULN_ENERGY = (O);
    static final int VULN_MENTAL = (P);
    static final int VULN_DISEASE = (Q);
    static final int VULN_DROWNING = (R);
    static final int VULN_LIGHT = (S);
    static final int VULN_SOUND = (T);
    static final int VULN_WOOD = (X);
    static final int VULN_SILVER = (Y);
    static final int VULN_IRON = (Z);

    /* body form */
    static final int FORM_EDIBLE = (A);
    static final int FORM_POISON = (B);
    static final int FORM_MAGICAL = (C);
    static final int FORM_INSTANT_DECAY = (D);
    static final int FORM_OTHER = (E);  /* defined by material bit */

    /* actual form */
    static final int FORM_ANIMAL = (G);
    static final int FORM_SENTIENT = (H);
    static final int FORM_UNDEAD = (I);
    static final int FORM_CONSTRUCT = (J);
    static final int FORM_MIST = (K);
    static final int FORM_INTANGIBLE = (L);

    static final int FORM_BIPED = (M);
    static final int FORM_CENTAUR = (N);
    static final int FORM_INSECT = (O);
    static final int FORM_SPIDER = (P);
    static final int FORM_CRUSTACEAN = (Q);
    static final int FORM_WORM = (R);
    static final int FORM_BLOB = (S);

    static final int FORM_MAMMAL = (V);
    static final int FORM_BIRD = (W);
    static final int FORM_REPTILE = (X);
    static final int FORM_SNAKE = (Y);
    static final int FORM_DRAGON = (Z);
    static final int FORM_AMPHIBIAN = BIT_26;
    static final int FORM_FISH = BIT_27;
    static final int FORM_COLD_BLOOD = BIT_28;

    /* body parts */
    static final int PART_HEAD = (A);
    static final int PART_ARMS = (B);
    static final int PART_LEGS = (C);
    static final int PART_HEART = (D);
    static final int PART_BRAINS = (E);
    static final int PART_GUTS = (F);
    static final int PART_HANDS = (G);
    static final int PART_FEET = (H);
    static final int PART_FINGERS = (I);
    static final int PART_EAR = (J);
    static final int PART_EYE = (K);
    static final int PART_LONG_TONGUE = (L);
    static final int PART_EYESTALKS = (M);
    static final int PART_TENTACLES = (N);
    static final int PART_FINS = (O);
    static final int PART_WINGS = (P);
    static final int PART_TAIL = (Q);
    /* for combat */
    static final int PART_CLAWS = (U);
    static final int PART_FANGS = (V);
    static final int PART_HORNS = (W);
    static final int PART_SCALES = (X);
    static final int PART_TUSKS = (Y);

    /*
    * Bits for 'affected_by'.  *AFF*
    * Used in #MOBILES.
    */
    static final long AFF_BLIND = (A);
    static final long AFF_INVISIBLE = (B);
    static final long AFF_IMP_INVIS = (C);
    static final long AFF_FADE = (D);
    static final long AFF_SCREAM = (E);
    static final long AFF_BLOODTHIRST = (F);
    static final long AFF_STUN = (G);
    static final long AFF_SANCTUARY = (H);
    static final long AFF_FAERIE_FIRE = (I);
    static final long AFF_INFRARED = (J);
    static final long AFF_CURSE = (K);
    static final long AFF_CORRUPTION = (L);
    static final long AFF_POISON = (M);
    static final long AFF_PROTECT_EVIL = (N);
    static final long AFF_PROTECT_GOOD = (O);
    static final long AFF_SNEAK = (P);
    static final long AFF_HIDE = (Q);
    static final long AFF_SLEEP = (R);
    static final long AFF_CHARM = (S);
    static final long AFF_FLYING = (T);
    static final long AFF_PASS_DOOR = (U);
    static final long AFF_HASTE = (V);
    static final long AFF_CALM = (W);
    static final long AFF_PLAGUE = (X);
    static final long AFF_WEAKEN = (Y);
    static final long AFF_WEAK_STUN = (Z);
    static final long AFF_BERSERK = BIT_26;
    static final long AFF_SWIM = BIT_27;
    static final long AFF_REGENERATION = BIT_28;
    static final long AFF_SLOW = BIT_29;
    static final long AFF_CAMOUFLAGE = BIT_30;

    static final long AFF_DETECT_IMP_INVIS = BIT_31;
    static final long AFF_DETECT_FADE = BIT_32;
    static final long AFF_DETECT_EVIL = BIT_33;
    static final long AFF_DETECT_INVIS = BIT_34;
    static final long AFF_DETECT_MAGIC = BIT_35;
    static final long AFF_DETECT_HIDDEN = BIT_36;
    static final long AFF_DETECT_GOOD = BIT_37;
    static final long AFF_DETECT_SNEAK = BIT_38;
    static final long AFF_DETECT_UNDEAD = BIT_39;

    static final long AFF_AURA_CHAOS = BIT_40;
    static final long AFF_PROTECTOR = BIT_41;
    static final long AFF_SUFFOCATE = BIT_42;
    static final long AFF_EARTHFADE = BIT_43;
    static final long AFF_FEAR = BIT_44;
    static final long AFF_FORM_TREE = BIT_45;
    static final long AFF_FORM_GRASS = BIT_46;
    static final long AFF_WEB = BIT_47;
    static final long AFF_LION = BIT_48;
    static final long AFF_GROUNDING = BIT_49;
    static final long AFF_ABSORB = BIT_50;
    static final long AFF_SPELLBANE = BIT_51;

    static final long AFF_DETECT_LIFE = BIT_52;
    static final long AFF_DARK_VISION = BIT_53;
    static final long AFF_ACUTE_VISION = BIT_54;

    /*
    * *AFF* bits for rooms
    */
    static final int AFF_ROOM_SHOCKING = (A);
    static final int AFF_ROOM_L_SHIELD = (B);
    static final int AFF_ROOM_THIEF_TRAP = (C);
    static final int AFF_ROOM_RANDOMIZER = (D);
    static final int AFF_ROOM_ESPIRIT = (E);
    static final int AFF_ROOM_PREVENT = (F);
    static final int AFF_ROOM_CURSE = (K);
    static final int AFF_ROOM_POISON = (M);
    static final int AFF_ROOM_SLEEP = (R);
    static final int AFF_ROOM_PLAGUE = (X);
    static final int AFF_ROOM_SLOW = BIT_29;


    /*
    * Sex.
    * Used in #MOBILES.
    */
    static final int SEX_MALE = 1;
    static final int SEX_FEMALE = 2;

    /* AC types */
    static final int AC_PIERCE = 0;
    static final int AC_BASH = 1;
    static final int AC_SLASH = 2;
    static final int AC_EXOTIC = 3;

    /* dice */
    static final int DICE_NUMBER = 0;
    static final int DICE_TYPE = 1;
    static final int DICE_BONUS = 2;

    /* size */
    static final int SIZE_TINY = 0;
    static final int SIZE_SMALL = 1;
    static final int SIZE_MEDIUM = 2;
    static final int SIZE_LARGE = 3;
    static final int SIZE_HUGE = 4;
    static final int SIZE_GIANT = 5;
    static final int SIZE_GARGANTUAN = 6;

    /*
    * Well known object virtual numbers.
    * Defined in #OBJECTS.
    */
    static final int OBJ_VNUM_SILVER_ONE = 1;
    static final int OBJ_VNUM_GOLD_ONE = 2;
    static final int OBJ_VNUM_GOLD_SOME = 3;
    static final int OBJ_VNUM_SILVER_SOME = 4;
    static final int OBJ_VNUM_COINS = 5;

    static final int OBJ_VNUM_CORPSE_NPC = 10;
    static final int OBJ_VNUM_CORPSE_PC = 11;
    static final int OBJ_VNUM_SEVERED_HEAD = 12;
    static final int OBJ_VNUM_TORN_HEART = 13;
    static final int OBJ_VNUM_SLICED_ARM = 14;
    static final int OBJ_VNUM_SLICED_LEG = 15;
    static final int OBJ_VNUM_GUTS = 16;
    static final int OBJ_VNUM_BRAINS = 17;

    static final int OBJ_VNUM_GRAVE_STONE = 19;
    static final int OBJ_VNUM_MUSHROOM = 20;
    static final int OBJ_VNUM_LIGHT_BALL = 21;
    static final int OBJ_VNUM_SPRING = 22;
    static final int OBJ_VNUM_DISC = 23;
    static final int OBJ_VNUM_PORTAL = 25;
    static final int OBJ_VNUM_ROSE = 1001;
    static final int OBJ_VNUM_PIT = 3010;

    static final int OBJ_VNUM_SCHOOL_MACE = 3700;
    static final int OBJ_VNUM_SCHOOL_DAGGER = 3701;
    static final int OBJ_VNUM_SCHOOL_SWORD = 3702;
    static final int OBJ_VNUM_SCHOOL_SPEAR = 3717;
    static final int OBJ_VNUM_SCHOOL_STAFF = 3718;
    static final int OBJ_VNUM_SCHOOL_AXE = 3719;
    static final int OBJ_VNUM_SCHOOL_FLAIL = 3720;
    static final int OBJ_VNUM_SCHOOL_WHIP = 3721;
    static final int OBJ_VNUM_SCHOOL_POLEARM = 3722;
    static final int OBJ_VNUM_SCHOOL_BOW = 3723;
    static final int OBJ_VNUM_SCHOOL_LANCE = 3724;

    static final int OBJ_VNUM_SCHOOL_VEST = 3703;
    static final int OBJ_VNUM_SCHOOL_SHIELD = 3704;
    static final int OBJ_VNUM_SCHOOL_BANNER = 3716;

    static final int OBJ_VNUM_MAP = 3162;
    static final int OBJ_VNUM_NMAP1 = 3385;
    static final int OBJ_VNUM_NMAP2 = 3386;
    static final int OBJ_VNUM_MAP_NT = 3167;
    static final int OBJ_VNUM_MAP_OFCOL = 3162;
    static final int OBJ_VNUM_MAP_SM = 3164;
    static final int OBJ_VNUM_MAP_TITAN = 3382;
    static final int OBJ_VNUM_MAP_OLD = 5333;

    static final int OBJ_VNUM_WHISTLE = 2116;

    static final int OBJ_VNUM_POTION_VIAL = 42;
    static final int OBJ_VNUM_STEAK = 27;
    static final int OBJ_VNUM_RANGER_STAFF = 28;
    static final int OBJ_VNUM_RANGER_ARROW = 6;
    static final int OBJ_VNUM_RANGER_BOW = 7;

    static final int OBJ_VNUM_DEPUTY_BADGE = 70;
    static final int OBJ_VNUM_RULER_BADGE = 70;
    static final int OBJ_VNUM_RULER_SHIELD1 = 71;
    static final int OBJ_VNUM_RULER_SHIELD2 = 72;
    static final int OBJ_VNUM_RULER_SHIELD3 = 73;
    static final int OBJ_VNUM_RULER_SHIELD4 = 74;

    static final int OBJ_VNUM_LION_SHIELD = 31;

    static final int OBJ_VNUM_CHAOS_BLADE = 87;

    static final int OBJ_VNUM_DRAGONDAGGER = 80;
    static final int OBJ_VNUM_DRAGONMACE = 81;
    static final int OBJ_VNUM_PLATE = 82;
    static final int OBJ_VNUM_DRAGONSWORD = 83;
    static final int OBJ_VNUM_DRAGONLANCE = 99;

    static final int OBJ_VNUM_BATTLE_PONCHO = 26;

    static final int OBJ_VNUM_BATTLE_THRONE = 542;
    static final int OBJ_VNUM_SHALAFI_ALTAR = 530;
    static final int OBJ_VNUM_CHAOS_ALTAR = 551;
    static final int OBJ_VNUM_INVADER_SKULL = 560;
    static final int OBJ_VNUM_KNIGHT_ALTAR = 521;
    static final int OBJ_VNUM_RULER_STAND = 510;
    static final int OBJ_VNUM_LIONS_ALTAR = 501;
    static final int OBJ_VNUM_HUNTER_ALTAR = 570;

    static final int OBJ_VNUM_POTION_SILVER = 43;
    static final int OBJ_VNUM_POTION_GOLDEN = 44;
    static final int OBJ_VNUM_POTION_SWIRLING = 45;
    static final int OBJ_VNUM_KATANA_SWORD = 98;

    static final int OBJ_VNUM_EYED_SWORD = 88;
    static final int OBJ_VNUM_FIRE_SHIELD = 92;
    static final int OBJ_VNUM_MAGIC_JAR = 93;
    static final int OBJ_VNUM_HAMMER = 6522;

    static final int OBJ_VNUM_CHUNK_IRON = 6521;

    /* vnums for tattoos */
    static final int OBJ_VNUM_TATTOO_APOLLON = 51;
    static final int OBJ_VNUM_TATTOO_ZEUS = 52;
    static final int OBJ_VNUM_TATTOO_SIEBELE = 53;
    static final int OBJ_VNUM_TATTOO_HEPHAESTUS = 54;
    static final int OBJ_VNUM_TATTOO_EHRUMEN = 55;
    static final int OBJ_VNUM_TATTOO_AHRUMAZDA = 56;
    static final int OBJ_VNUM_TATTOO_DEIMOS = 57;
    static final int OBJ_VNUM_TATTOO_PHOBOS = 58;
    static final int OBJ_VNUM_TATTOO_ODIN = 59;
    static final int OBJ_VNUM_TATTOO_MARS = 60;
    static final int OBJ_VNUM_TATTOO_ATHENA = 61;
    static final int OBJ_VNUM_TATTOO_GOKTENGRI = 62;
    static final int OBJ_VNUM_TATTOO_HERA = 63;
    static final int OBJ_VNUM_TATTOO_VENUS = 64;
    static final int OBJ_VNUM_TATTOO_ARES = 65;
    static final int OBJ_VNUM_TATTOO_PROMETHEUS = 66;
    static final int OBJ_VNUM_TATTOO_EROS = 67;

    /* quest rewards */
    static final int QUEST_ITEM1 = 94;
    static final int QUEST_ITEM2 = 95;
    static final int QUEST_ITEM3 = 96;
    static final int QUEST_ITEM4 = 30;
    static final int QUEST_ITEM5 = 29;

    /*
    * Item types.
    * Used in #OBJECTS.
    */
    static final int ITEM_LIGHT = 1;
    static final int ITEM_SCROLL = 2;
    static final int ITEM_WAND = 3;
    static final int ITEM_STAFF = 4;
    static final int ITEM_WEAPON = 5;
    static final int ITEM_TREASURE = 8;
    static final int ITEM_ARMOR = 9;
    static final int ITEM_POTION = 10;
    static final int ITEM_CLOTHING = 11;
    static final int ITEM_FURNITURE = 12;
    static final int ITEM_TRASH = 13;
    static final int ITEM_CONTAINER = 15;
    static final int ITEM_DRINK_CON = 17;
    static final int ITEM_KEY = 18;
    static final int ITEM_FOOD = 19;
    static final int ITEM_MONEY = 20;
    static final int ITEM_BOAT = 22;
    static final int ITEM_CORPSE_NPC = 23;
    static final int ITEM_CORPSE_PC = 24;
    static final int ITEM_FOUNTAIN = 25;
    static final int ITEM_PILL = 26;
    static final int ITEM_PROTECT = 27;
    static final int ITEM_MAP = 28;
    static final int ITEM_PORTAL = 29;
    static final int ITEM_WARP_STONE = 30;
    static final int ITEM_ROOM_KEY = 31;
    static final int ITEM_GEM = 32;
    static final int ITEM_JEWELRY = 33;
    static final int ITEM_JUKEBOX = 34;
    static final int ITEM_TATTOO = 35;

    /*
    * Extra flags.  *EXT*
    * Used in #OBJECTS.
    */
    static final int ITEM_GLOW = (A);
    static final int ITEM_HUM = (B);
    static final int ITEM_DARK = (C);
    static final int ITEM_LOCK = (D);
    static final int ITEM_EVIL = (E);
    static final int ITEM_INVIS = (F);
    static final int ITEM_MAGIC = (G);
    static final int ITEM_NODROP = (H);
    static final int ITEM_BLESS = (I);
    static final int ITEM_ANTI_GOOD = (J);
    static final int ITEM_ANTI_EVIL = (K);
    static final int ITEM_ANTI_NEUTRAL = (L);
    static final int ITEM_NOREMOVE = (M);
    static final int ITEM_INVENTORY = (N);
    static final int ITEM_NOPURGE = (O);
    static final int ITEM_ROT_DEATH = (P);
    static final int ITEM_VIS_DEATH = (Q);
    static final int ITEM_NOSAC = (R);
    static final int ITEM_NONMETAL = (S);
    static final int ITEM_NOLOCATE = (T);
    static final int ITEM_MELT_DROP = (U);
    static final int ITEM_HAD_TIMER = (V);
    static final int ITEM_SELL_EXTRACT = (W);
    static final int ITEM_BURN_PROOF = (Y);
    static final int ITEM_NOUNCURSE = (Z);
    static final int ITEM_NOSELL = BIT_26;
    static final int ITEM_BURIED = BIT_27;

    /*
    * Wear flags.   *WEAR*
    * Used in #OBJECTS.
    */
    static final int ITEM_TAKE = (A);
    static final int ITEM_WEAR_FINGER = (B);
    static final int ITEM_WEAR_NECK = (C);
    static final int ITEM_WEAR_BODY = (D);
    static final int ITEM_WEAR_HEAD = (E);
    static final int ITEM_WEAR_LEGS = (F);
    static final int ITEM_WEAR_FEET = (G);
    static final int ITEM_WEAR_HANDS = (H);
    static final int ITEM_WEAR_ARMS = (I);
    static final int ITEM_WEAR_SHIELD = (J);
    static final int ITEM_WEAR_ABOUT = (K);
    static final int ITEM_WEAR_WAIST = (L);
    static final int ITEM_WEAR_WRIST = (M);
    static final int ITEM_WIELD = (N);
    static final int ITEM_HOLD = (O);
    static final int ITEM_NO_SAC = (P);
    static final int ITEM_WEAR_FLOAT = (Q);
    static final int ITEM_WEAR_TATTOO = (R);

    /* weapon class */
    static final int WEAPON_EXOTIC = 0;
    static final int WEAPON_SWORD = 1;
    static final int WEAPON_DAGGER = 2;
    static final int WEAPON_SPEAR = 3;
    static final int WEAPON_MACE = 4;
    static final int WEAPON_AXE = 5;
    static final int WEAPON_FLAIL = 6;
    static final int WEAPON_WHIP = 7;
    static final int WEAPON_POLEARM = 8;
    static final int WEAPON_BOW = 9;
    static final int WEAPON_ARROW = 10;
    static final int WEAPON_LANCE = 11;

    /* weapon types */
    static final int WEAPON_FLAMING = (A);
    static final int WEAPON_FROST = (B);
    static final int WEAPON_VAMPIRIC = (C);
    static final int WEAPON_SHARP = (D);
    static final int WEAPON_VORPAL = (E);
    static final int WEAPON_TWO_HANDS = (F);
    static final int WEAPON_SHOCKING = (G);
    static final int WEAPON_POISON = (H);
    static final int WEAPON_HOLY = (I);
    static final int WEAPON_KATANA = (J);

    /* gate flags */
    static final int GATE_NORMAL_EXIT = (A);
    static final int GATE_NOCURSE = (B);
    static final int GATE_GOWITH = (C);
    static final int GATE_BUGGY = (D);
    static final int GATE_RANDOM = (E);

    /* furniture flags */
    static final int STAND_AT = (A);
    static final int STAND_ON = (B);
    static final int STAND_IN = (C);
    static final int SIT_AT = (D);
    static final int SIT_ON = (E);
    static final int SIT_IN = (F);
    static final int REST_AT = (G);
    static final int REST_ON = (H);
    static final int REST_IN = (I);
    static final int SLEEP_AT = (J);
    static final int SLEEP_ON = (K);
    static final int SLEEP_IN = (L);
    static final int PUT_AT = (M);
    static final int PUT_ON = (N);
    static final int PUT_IN = (O);
    static final int PUT_INSIDE = (P);

    /*
    * Apply types (for affects).
    * Used in #OBJECTS.
    */
    static final int APPLY_NONE = 0;
    static final int APPLY_STR = 1;
    static final int APPLY_DEX = 2;
    static final int APPLY_INT = 3;
    static final int APPLY_WIS = 4;
    static final int APPLY_CON = 5;
    static final int APPLY_CHA = 6;
    static final int APPLY_CLASS = 7;
    static final int APPLY_LEVEL = 8;
    static final int APPLY_AGE = 9;
    static final int APPLY_HEIGHT = 10;
    static final int APPLY_WEIGHT = 11;
    static final int APPLY_MANA = 12;
    static final int APPLY_HIT = 13;
    static final int APPLY_MOVE = 14;
    static final int APPLY_GOLD = 15;
    static final int APPLY_EXP = 16;
    static final int APPLY_AC = 17;
    static final int APPLY_HITROLL = 18;
    static final int APPLY_DAMROLL = 19;
    static final int APPLY_SAVES = 20;
    static final int APPLY_SAVING_PARA = 20;
    static final int APPLY_SAVING_ROD = 21;
    static final int APPLY_SAVING_PETRI = 22;
    static final int APPLY_SAVING_BREATH = 23;
    static final int APPLY_SAVING_SPELL = 24;
    static final int APPLY_SPELL_AFFECT = 25;
    static final int APPLY_SIZE = 26;

    /*
    * Values for containers (value[1]).
    * Used in #OBJECTS.
    */
    static final int CONT_CLOSEABLE = 1;
    static final int CONT_PICKPROOF = 2;
    static final int CONT_CLOSED = 4;
    static final int CONT_LOCKED = 8;
    static final int CONT_PUT_ON = 16;
    static final int CONT_FOR_ARROW = 32;
    static final int CONT_ST_LIMITED = 64;


    /*
    * Well known room virtual numbers.
    * Defined in #ROOMS.
    */
    static final int ROOM_VNUM_LIMBO = 2;
    static final int ROOM_VNUM_CHAT = 1200;
    static final int ROOM_VNUM_TEMPLE = 3001;
    static final int ROOM_VNUM_ALTAR = 3054;
    static final int ROOM_VNUM_SCHOOL = 3700;
    static final int ROOM_VNUM_BALANCE = 4500;
    static final int ROOM_VNUM_CIRCLE = 4400;
    static final int ROOM_VNUM_DEMISE = 4201;
    static final int ROOM_VNUM_HONOR = 4300;
    static final int ROOM_VNUM_BATTLE = 541;

    /*
    * Room flags.
    * Used in #ROOMS.
    */
    static final int ROOM_DARK = (A);
    static final int ROOM_NO_MOB = (C);
    static final int ROOM_INDOORS = (D);
    static final int ROOM_PRIVATE = (J);
    static final int ROOM_SAFE = (K);
    static final int ROOM_SOLITARY = (L);
    static final int ROOM_PET_SHOP = (M);
    static final int ROOM_NO_RECALL = (N);
    static final int ROOM_IMP_ONLY = (O);
    static final int ROOM_GODS_ONLY = (P);
    static final int ROOM_HEROES_ONLY = (Q);
    static final int ROOM_NEWBIES_ONLY = (R);
    static final int ROOM_LAW = (S);
    static final int ROOM_NOWHERE = (T);
    static final int ROOM_BANK = (U);
    static final int ROOM_NO_MAGIC = (W);
    static final int ROOM_NOSUMMON = (X);
    static final int ROOM_REGISTRY = BIT_27;

    /*
    * Directions.
    * Used in #ROOMS.
    */
    static final int DIR_NORTH = 0;
    static final int DIR_EAST = 1;
    static final int DIR_SOUTH = 2;
    static final int DIR_WEST = 3;
    static final int DIR_UP = 4;
    static final int DIR_DOWN = 5;

    /*
    * Exit flags.
    * Used in #ROOMS.
    */
    static final int EX_ISDOOR = (A);
    static final int EX_CLOSED = (B);
    static final int EX_LOCKED = (C);
    static final int EX_NOFLEE = (D);
    static final int EX_PICKPROOF = (F);
    static final int EX_NOPASS = (G);
    static final int EX_EASY = (H);
    static final int EX_HARD = (I);
    static final int EX_INFURIATING = (J);
    static final int EX_NOCLOSE = (K);
    static final int EX_NOLOCK = (L);

    /*
    * Sector types.
    * Used in #ROOMS.
    */
    static final int SECT_INSIDE = 0;
    static final int SECT_CITY = 1;
    static final int SECT_FIELD = 2;
    static final int SECT_FOREST = 3;
    static final int SECT_HILLS = 4;
    static final int SECT_MOUNTAIN = 5;
    static final int SECT_WATER_SWIM = 6;
    static final int SECT_WATER_NOSWIM = 7;
    static final int SECT_UNUSED = 8;
    static final int SECT_AIR = 9;
    static final int SECT_DESERT = 10;
    static final int SECT_MAX = 11;

    /*
    * Equpiment wear locations.
    * Used in #RESETS.
    */
    static final int OWEAR_NONE = -1;
    static final int OWEAR_LIGHT = 0;
    static final int OWEAR_FINGER_L = 1;
    static final int OWEAR_FINGER_R = 2;
    static final int OWEAR_NECK_1 = 3;
    static final int OWEAR_NECK_2 = 4;
    static final int OWEAR_BODY = 5;
    static final int OWEAR_HEAD = 6;
    static final int OWEAR_LEGS = 7;
    static final int OWEAR_FEET = 8;
    static final int OWEAR_HANDS = 9;
    static final int OWEAR_ARMS = 10;
    static final int OWEAR_SHIELD = 11;
    static final int OWEAR_ABOUT = 12;
    static final int OWEAR_WAIST = 13;
    static final int OWEAR_WRIST_L = 14;
    static final int OWEAR_WRIST_R = 15;
    static final int OWEAR_WIELD = 16;
    static final int OWEAR_HOLD = 17;
    static final int OWEAR_FLOAT = 18;
    static final int OWEAR_TATTOO = 19;
    static final int OWEAR_SECOND_WIELD = 20;
    static final int OWEAR_STUCK_IN = 21;
    static final int OMAX_WEAR = 22;

    static final int WEAR_NONE = -1;
    static final int WEAR_FINGER = 0;
    static final int WEAR_NECK = 1;
    static final int WEAR_BODY = 2;
    static final int WEAR_HEAD = 3;
    static final int WEAR_LEGS = 4;
    static final int WEAR_FEET = 5;
    static final int WEAR_HANDS = 6;
    static final int WEAR_ARMS = 7;
    static final int WEAR_ABOUT = 8;
    static final int WEAR_WAIST = 9;
    static final int WEAR_WRIST = 10;
    static final int WEAR_LEFT = 11;
    static final int WEAR_RIGHT = 12;
    static final int WEAR_BOTH = 13;
    static final int WEAR_FLOAT = 14;
    static final int WEAR_TATTOO = 15;
    static final int WEAR_STUCK_IN = 16;
    static final int MAX_WEAR = 17;


    static final int MAX_FINGER = 4;
    static final int MAX_NECK = 2;
    static final int MAX_WRIST = 2;
    static final int MAX_TATTOO = 2;
    static final int MAX_STUCK_IN = 99 /* infinite :) */;

    /**
     * ************************************************************************
     * *
     * VALUES OF INTEREST TO AREA BUILDERS                   *
     * (End of this section ... stop here)                   *
     * *
     * *************************************************************************
     */

/*
 * Conditions.
 */
    static final int COND_DRUNK = 0;
    static final int COND_FULL = 1;
    static final int COND_THIRST = 2;
    static final int COND_HUNGER = 3;
    static final int COND_BLOODLUST = 4;
    static final int COND_DESIRE = 5;

    static final int MAX_COND = 6;

    /*
    * Positions.
    */
    static final int POS_DEAD = 0;
    static final int POS_MORTAL = 1;
    static final int POS_INCAP = 2;
    static final int POS_STUNNED = 3;
    static final int POS_SLEEPING = 4;
    static final int POS_RESTING = 5;
    static final int POS_SITTING = 6;
    static final int POS_FIGHTING = 7;
    static final int POS_STANDING = 8;

    /*
    * ACT bits for players.
    */
    static final int PLR_IS_NPC = (A)     /* Don't EVER set.  */;
    static final int PLR_BOUGHT_PET = (B);

    /* RT auto flags */
    static final int PLR_AUTOASSIST = (C);
    static final int PLR_AUTOEXIT = (D);
    static final int PLR_AUTOLOOT = (E);
    static final int PLR_AUTOSAC = (F);
    static final int PLR_AUTOGOLD = (G);
    static final int PLR_AUTOSPLIT = (H);
    static final int PLR_COLOR = (I);
    static final int PLR_WANTED = (J);
    static final int PLR_NO_TITLE = (K);
    /* RT personal flags */
    static final int PLR_NO_EXP = (L);
    static final int PLR_CHANGED_AFF = (M);
    static final int PLR_HOLYLIGHT = (N);
    static final int PLR_NOCANCEL = (O);
    static final int PLR_CANLOOT = (P);
    static final int PLR_NOSUMMON = (Q);
    static final int PLR_NOFOLLOW = (R);
    static final int PLR_CANINDUCT = (S);
    static final int PLR_GHOST = (T);

    /* penalty flags */
    static final int PLR_PERMIT = (U);
    static final int PLR_REMORTED = (V);
    static final int PLR_LOG = (W);
    static final int PLR_DENY = (X);
    static final int PLR_FREEZE = (Y);
    static final int PLR_LEFTHAND = (Z);
    static final int PLR_CANREMORT = BIT_26;
    static final int PLR_QUESTOR = BIT_27;
    static final int PLR_VAMPIRE = BIT_28;
    static final int PLR_HARA_KIRI = BIT_29;
    static final int PLR_BLINK_ON = BIT_30;


    static boolean IS_QUESTOR(CHAR_DATA ch) {
        return IS_SET(ch.act, PLR_QUESTOR);
    }

    static boolean IS_VAMPIRE(CHAR_DATA ch) {
        return !IS_NPC(ch) && IS_SET(ch.act, PLR_VAMPIRE);
    }

    static boolean IS_HARA_KIRI(CHAR_DATA ch) {
        return IS_SET(ch.act, PLR_HARA_KIRI);
    }

    static boolean CANT_CHANGE_TITLE(CHAR_DATA ch) {
        return IS_SET(ch.act, PLR_NO_TITLE);
    }

    static boolean IS_BLINK_ON(CHAR_DATA ch) {
        return IS_SET(ch.act, PLR_BLINK_ON);
    }

    static boolean CANT_GAIN_EXP(CHAR_DATA ch) {
        return IS_SET(ch.act, PLR_NO_EXP);
    }

    static boolean RIGHT_HANDER(CHAR_DATA ch) {
        return IS_NPC(ch) || !IS_SET(ch.act, PLR_LEFTHAND);
    }

    static boolean LEFT_HANDER(CHAR_DATA ch) {
        return !IS_NPC(ch) && IS_SET(ch.act, PLR_LEFTHAND);
    }

    /* The Quests */
    static final int QUEST_EYE = (B);
    static final int QUEST_WEAPON = (C);
    static final int QUEST_GIRTH = (D);
    static final int QUEST_RING = (E);
    static final int QUEST_WEAPON2 = (F);
    static final int QUEST_GIRTH2 = (G);
    static final int QUEST_RING2 = (H);
    static final int QUEST_WEAPON3 = (I);
    static final int QUEST_GIRTH3 = (J);
    static final int QUEST_RING3 = (K);
    static final int QUEST_BACKPACK = (L);
    static final int QUEST_BACKPACK2 = (M);
    static final int QUEST_BACKPACK3 = (N);
    static final int QUEST_DECANTER = (O);
    static final int QUEST_DECANTER2 = (P);
    static final int QUEST_DECANTER3 = (Q);

    static final int QUEST_PRACTICE = (S);

    /* time log problems */
    static final int TLP_NOLOG = (A);
    static final int TLP_BOOT = (B);

    /* RT comm flags -- may be used on both mobs and chars */
    static final int COMM_QUIET = (A);
    static final int COMM_DEAF = (B);
    static final int COMM_NOWIZ = (C);
    static final int COMM_NOAUCTION = (D);
    static final int COMM_NOGOSSIP = (E);
    static final int COMM_NOQUESTION = (F);
    static final int COMM_NOMUSIC = (G);
    static final int COMM_NOQUOTE = (I);
    static final int COMM_SHOUTSOFF = (J);

    /* display flags */
    static final int COMM_true_TRUST = (K);
    static final int COMM_COMPACT = (L);
    static final int COMM_BRIEF = (M);
    static final int COMM_PROMPT = (N);
    static final int COMM_COMBINE = (O);
    static final int COMM_TELNET_GA = (P);
    static final int COMM_SHOW_AFFECTS = (Q);
    static final int COMM_NOGRATS = (R);

    /* penalties */
    static final int COMM_NOEMOTE = (T);
    static final int COMM_NOSHOUT = (U);
    static final int COMM_NOTELL = (V);
    static final int COMM_NOCHANNELS = (W);
    static final int COMM_SNOOP_PROOF = (Y);
    static final int COMM_AFK = (Z);

    /* WIZnet flags */
    static final int WIZ_ON = (A);
    static final int WIZ_TICKS = (B);
    static final int WIZ_LOGINS = (C);
    static final int WIZ_SITES = (D);
    static final int WIZ_LINKS = (E);
    static final int WIZ_DEATHS = (F);
    static final int WIZ_RESETS = (G);
    static final int WIZ_MOBDEATHS = (H);
    static final int WIZ_FLAGS = (I);
    static final int WIZ_PENALTIES = (J);
    static final int WIZ_SACCING = (K);
    static final int WIZ_LEVELS = (L);
    static final int WIZ_SECURE = (M);
    static final int WIZ_SWITCHES = (N);
    static final int WIZ_SNOOPS = (O);
    static final int WIZ_RESTORE = (P);
    static final int WIZ_LOAD = (Q);
    static final int WIZ_NEWBIE = (R);
    static final int WIZ_PREFIX = (S);
    static final int WIZ_SPAM = (T);

/*
 * language staff
 */

    static final class language_type {
        final String name;
        final int type;

        language_type(String name, int type) {
            this.name = name;
            this.type = type;
        }

    }

    static final class translation_type {
        final char common;
        final char language;

        translation_type(char common, char language) {
            this.common = common;
            this.language = language;
        }
    }

/*
 * auction data
 */

    static final class AUCTION_DATA {
        OBJ_DATA item;   /* a pointer to the item */
        CHAR_DATA seller; /* a pointer to the seller - which may NOT quit */
        CHAR_DATA buyer;  /* a pointer to the buyer - which may NOT quit */
        int bet;    /* last bet - or 0 if noone has bet anything */
        int going;  /* 1,2, sold */
        int pulse;  /* how many pulses (.25 sec) until another call-out ? */
    }

/*
 * Prototype for a mob.
 * This is the in-memory version of #MOBILES.
 */

    static final class MOB_INDEX_DATA {
        MOB_INDEX_DATA next;
        SPEC_FUN spec_fun;
        MPROG_DATA mprogs;
        int progtypes;
        SHOP_DATA pShop;
        int vnum;
        int group;
        boolean new_format;
        int count;
        int killed;
        String player_name;
        String short_descr;
        String long_descr;
        String description;
        long act;
        long affected_by;
        int alignment;
        int level;
        int hitroll;
        final int[] hit = new int[3];
        final int[] mana = new int[3];
        final int[] damage = new int[3];
        final int[] ac = new int[4];
        int dam_type;
        long off_flags;
        long imm_flags;
        long res_flags;
        long vuln_flags;
        int start_pos;
        int default_pos;
        int sex;
        Race race;
        int wealth;
        int form;
        int parts;
        int size;
        String material;
        int practicer;
    }

    /* memory settings */
    static final int MEM_CUSTOMER = A;
    static final int MEM_SELLER = B;
    static final int MEM_HOSTILE = C;
    static final int MEM_AFRAID = D;

/* memory for mobs */

    static final class MEM_DATA {
        MEM_DATA next;
        boolean valid;
        int id;
        int reaction;
        int when;
    }

    static final class MPROG_DATA {
        MPROG_FUN_BRIBE bribe_prog;
        MPROG_FUN_ENTRY entry_prog;
        MPROG_FUN_GIVE give_prog;
        MPROG_FUN_GREET greet_prog;
        MPROG_FUN_FIGHT fight_prog;
        MPROG_FUN_DEATH death_prog;
        MPROG_FUN_AREA area_prog;
        MPROG_FUN_SPEECH speech_prog;

    }

    static final class OPROG_DATA {
        OPROG_FUN_WEAR wear_prog;
        OPROG_FUN_REMOVE remove_prog;
        OPROG_FUN_GET get_prog;
        OPROG_FUN_DROP drop_prog;
        OPROG_FUN_SAC sac_prog;
        OPROG_FUN_ENTRY entry_prog;
        OPROG_FUN_GIVE give_prog;
        OPROG_FUN_GREET greet_prog;
        OPROG_FUN_FIGHT fight_prog;
        OPROG_FUN_DEATH death_prog;
        OPROG_FUN_SPEECH speech_prog;
        OPROG_FUN_AREA area_prog;
    }

/*
* One character (PC or NPC). *CHAR_DATA*
*/

    static final class CHAR_DATA {
        CHAR_DATA next;
        CHAR_DATA next_in_room;
        CHAR_DATA master;
        CHAR_DATA leader;
        CHAR_DATA fighting;
        CHAR_DATA reply;
        CHAR_DATA last_fought;
        long last_fight_time;
        long last_death_time;
        CHAR_DATA pet;
        CHAR_DATA doppel;
        CHAR_DATA guarding;
        CHAR_DATA guarded_by;
        MEM_DATA memory;
        SPEC_FUN spec_fun;
        MOB_INDEX_DATA pIndexData;
        DESCRIPTOR_DATA desc;
        AFFECT_DATA affected;
        NOTE_DATA pnote;
        OBJ_DATA carrying;
        OBJ_DATA on;
        ROOM_INDEX_DATA in_room;
        ROOM_INDEX_DATA was_in_room;
        AREA_DATA zone;
        PC_DATA pcdata;
        boolean valid;
        String name;
        int id;
        String short_descr;
        String long_descr;
        String description;
        String prompt;
        String prefix;
        int group;
        int sex;
        Clazz clazz;
        Race race;
        int cabal;
        int hometown;
        int ethos;
        int level;
        int trust;
        int played;
        int lines;  /* for the pager */
        long logon;
        int timer;
        int wait;
        int daze;
        int hit;
        int max_hit;
        int mana;
        int max_mana;
        int move;
        int max_move;
        int gold;
        int silver;
        int exp;
        long act;
        int comm;   /* RT added to pad the vector */
        int wiznet; /* wiz stuff */
        long imm_flags;
        long res_flags;
        long vuln_flags;
        int invis_level;
        int incog_level;
        long affected_by;
        int position;
        int practice;
        int train;
        int carry_weight;
        int carry_number;
        int saving_throw;
        int alignment;
        int hitroll;
        int damroll;
        final int[] armor = new int[4];
        int wimpy;
        /* stats */
        final int[] perm_stat = new int[MAX_STATS];
        final int[] mod_stat = new int[MAX_STATS];
        /* parts stuff */
        int form;
        int parts;
        int size;
        String material;
        /* mobile stuff */
        long off_flags;
        final int[] damage = new int[3];
        int dam_type;
        int start_pos;
        int default_pos;
        int status;
        int progtypes;
        boolean extracted;
        String in_mind;
        int quest;
        int religion;
        CHAR_DATA hunting;    /* hunt data */
        int endur;
        boolean riding; /* mount data */
        CHAR_DATA mount;
        int language;
    }

/*
* Data which only PC's have.
*/

    static final class PC_DATA {
        PC_DATA next;
        StringBuilder buffer = new StringBuilder();
        boolean valid;
        String pwd;
        String bamfin;
        String bamfout;
        String title;
        int last_note;
        int last_idea;
        int last_penalty;
        int last_news;
        int last_changes;
        int perm_hit;
        int perm_mana;
        int perm_move;
        int true_sex;
        int last_level;
        final int[] condition = new int[MAX_SKILLS];
        final int[] learned = new int[MAX_SKILLS];
        int points;
        boolean confirm_delete;
        boolean confirm_remort;
        final String[] alias = new String[MAX_ALIAS];
        final String[] alias_sub = new String[MAX_ALIAS];
        int bank_s;
        int bank_g;
        int death;
        int played;
        int anti_killed;
        int has_killed;
        int questgiver; /* quest */
        int questpoints;    /* quest */
        int nextquest;  /* quest */
        int countdown;  /* quest */
        int questobj;   /* quest */
        int questmob;       /* quest */
        Race race;       /* orginal race for polymorph */
        int time_flag;  /* time log problem */
        final int[] log_time = new int[MAX_TIME_LOG]; /* min.s of playing each day */
        /* 0th day is the current day  */
    }

    /*
    * Liquids.
    */
    static final int LIQ_WATER = 0;

    static final class liq_type {
        final String liq_name;
        final String liq_color;
        final int[] liq_affect;

        liq_type(String liq_name, String liq_color, int[] liq_affect) {
            this.liq_name = liq_name;
            this.liq_color = liq_color;
            this.liq_affect = liq_affect;
        }
    }

/*
* Extra description data for a room or object.
*/

    static final class EXTRA_DESCR_DATA {
        EXTRA_DESCR_DATA next; /* Next in list                     */
        boolean valid;
        String keyword = "";              /* Keyword in look/examine          */
        String description = "";          /* What to see                      */
    }

/*
* Prototype for an object.  *OID*
*/

    static final class OBJ_INDEX_DATA {
        OBJ_INDEX_DATA next;
        EXTRA_DESCR_DATA extra_descr;
        AFFECT_DATA affected;
        boolean new_format;
        String name;
        String short_descr;
        String description;
        int vnum;
        int reset_num;
        String material;
        int item_type;
        int extra_flags;
        int wear_flags;
        int level;
        int condition;
        int count;
        int weight;
        int cost;
        final int[] value = new int[5];
        int progtypes;
        int limit;
        OPROG_DATA oprogs;
    }

/*
* One object.  *OD*
*/

    static final class OBJ_DATA {
        OBJ_DATA next;
        OBJ_DATA next_content;
        OBJ_DATA contains;
        OBJ_DATA in_obj;
        OBJ_DATA on;
        CHAR_DATA carried_by;
        EXTRA_DESCR_DATA extra_descr;
        AFFECT_DATA affected;
        OBJ_INDEX_DATA pIndexData;
        ROOM_INDEX_DATA in_room;
        boolean valid;
        boolean enchanted;
        String owner;
        String name;
        String short_descr;
        String description;
        int item_type;
        long extra_flags;
        int wear_flags;
        int wear_loc;
        int weight;
        int cost;
        int level;
        int condition;
        String material;
        int timer;
        final int[] value = new int[5];
        int progtypes;
        String from;
        int altar;
        int pit;
        boolean extracted;
        int water_float;
    }

/*
* Exit data.
*/

    static final class EXIT_DATA {
        ROOM_INDEX_DATA to_room;
        int vnum;

        int exit_info;
        int key;
        String keyword;
        String description;
    }

/*
* Reset commands:
*   '*': comment
*   'M': read a mobile
*   'O': read an object
*   'P': put object in object
*   'G': give object to mobile
*   'E': equip object to mobile
*   'D': set state of door
*   'R': randomize room exits
*   'S': stop (end of list)
*/

/*
 * Area-reset definition.
 */

    static final class RESET_DATA {
        RESET_DATA next;
        char command;
        int arg1;
        int arg2;
        int arg3;
        int arg4;
    }

/*
* Area definition.
*/

    static final class AREA_DATA {
        AREA_DATA next;
        RESET_DATA reset_first;
        RESET_DATA reset_last;
        String file_name;
        String name;
        String writer;
        String credits;
        int age;
        int nplayer;
        int low_range;
        int high_range;
        int min_vnum;
        int max_vnum;
        boolean empty;
        int count;
        String resetmsg;
        int area_flag;
    }

    static final class ROOM_HISTORY_DATA {
        String name;
        int went;
        ROOM_HISTORY_DATA next;
    }

/*
 * Room type.
 */

    static final class ROOM_INDEX_DATA {
        ROOM_INDEX_DATA next;
        ROOM_INDEX_DATA aff_next;
        CHAR_DATA people;
        OBJ_DATA contents;
        EXTRA_DESCR_DATA extra_descr;
        AREA_DATA area;
        final EXIT_DATA[] exit = new EXIT_DATA[6];
        final EXIT_DATA[] old_exit = new EXIT_DATA[6];
        String name;
        String description;
        String owner;
        int vnum;
        long room_flags;
        int light;
        int sector_type;
        int heal_rate;
        int mana_rate;
        ROOM_HISTORY_DATA history;
        AFFECT_DATA affected;
        long affected_by;
    }


    /*
    *  Target types.
    */
    static final int TAR_IGNORE = 0;
    static final int TAR_CHAR_OFFENSIVE = 1;
    static final int TAR_CHAR_DEFENSIVE = 2;
    static final int TAR_CHAR_SELF = 3;
    static final int TAR_OBJ_INV = 4;
    static final int TAR_OBJ_CHAR_DEF = 5;
    static final int TAR_OBJ_CHAR_OFF = 6;

    static final int TARGET_CHAR = 0;
    static final int TARGET_OBJ = 1;
    static final int TARGET_ROOM = 2;
    static final int TARGET_NONE = 3;

/*
* ActSkill include spells as a particular case.
*/

/*
* Utility macros.
*/

    static int UMIN(int a, int b) {
        return ((a) < (b) ? (a) : (b));
    }

    static int UMAX(int a, int b) {
        return ((a) > (b) ? (a) : (b));
    }

    static int URANGE(int a, int b, int c) {
        return ((b) < (a) ? (a) : ((b) > (c) ? (c) : (b)));
    }


    static boolean IS_SET(long flag, long bit) {
        return (flag & bit) != 0;
    }

    static boolean IS_SET(int flag, int bit) {
        return (flag & bit) != 0;
    }

    static long SET_BIT(long var, long bit) {
        return var | bit;
    }

    static int SET_BIT(int var, int bit) {
        return var | bit;
    }

    static long REMOVE_BIT(long var, long bit) {
        return var & (~bit);
    }

    static int REMOVE_BIT(int var, int bit) {
        return var & (~bit);
    }

    static boolean IS_WATER(ROOM_INDEX_DATA var) {
        return ((var.sector_type == SECT_WATER_SWIM) || (var.sector_type == SECT_WATER_NOSWIM));
    }

    static int PERCENT(int cur, int max) {
        return max == 0 ? 0 : (cur * 100) / max;
    }

/*
 * Character macros.
 */

    static boolean IS_NPC(CHAR_DATA ch) {
        return IS_SET(ch.act, ACT_IS_NPC);
    }

    static boolean IS_IMMORTAL(CHAR_DATA ch) {
        return get_trust(ch) >= LEVEL_IMMORTAL;
    }

    static boolean IS_HERO(CHAR_DATA ch) {
        return get_trust(ch) >= LEVEL_HERO;
    }

    static boolean IS_TRUSTED(CHAR_DATA ch, int level) {
        return get_trust(ch) >= level;
    }

    static boolean IS_AFFECTED(CHAR_DATA ch, long sn) {
        return IS_SET(ch.affected_by, sn);
    }

    static boolean IS_AFFECTED(MOB_INDEX_DATA m, long sn) {
        return IS_SET(m.affected_by, sn);
    }

    static boolean IS_PK(CHAR_DATA ch, CHAR_DATA vt) {
        return !IS_NPC(ch) & !IS_NPC(vt);
    }

    static void SET_ORG_RACE(CHAR_DATA ch, Race race) {
        if (IS_NPC(ch)) {
            ch.pIndexData.race = race;
        } else {
            ch.pcdata.race = race;
        }
    }

    static Race ORG_RACE(CHAR_DATA ch) {
        return IS_NPC(ch) ? ch.pIndexData.race : ch.pcdata.race;
    }

    static int GET_AGE(CHAR_DATA ch) {
        return (int) (17 + (ch.played + current_time - ch.logon) / 72000);
    }

    static boolean IS_GOOD(CHAR_DATA ch) {
        return ch.alignment >= 350;
    }

    static boolean IS_EVIL(CHAR_DATA ch) {
        return ch.alignment <= -350;
    }

    static boolean IS_NEUTRAL(CHAR_DATA ch) {
        return !IS_GOOD(ch) && !IS_EVIL(ch);
    }

    static boolean IS_GOOD(MOB_INDEX_DATA ch) {
        return ch.alignment >= 350;
    }

    static boolean IS_EVIL(MOB_INDEX_DATA ch) {
        return ch.alignment <= -350;
    }

    static boolean IS_NEUTRAL(MOB_INDEX_DATA ch) {
        return !IS_GOOD(ch) && !IS_EVIL(ch);
    }

    static boolean IS_AWAKE(CHAR_DATA ch) {
        return ch.position > POS_SLEEPING;
    }

    static int GET_AC(CHAR_DATA ch, int type) {
        return ch.armor[type] + (IS_AWAKE(ch) ? dex_app[get_curr_stat(ch, STAT_DEX)].defensive : 0);
    }

    static int GET_HITROLL(CHAR_DATA ch) {
        return ch.hitroll + str_app[get_curr_stat(ch, STAT_STR)].tohit;
    }

    static int GET_DAMROLL(CHAR_DATA ch) {
        return ch.damroll + str_app[get_curr_stat(ch, STAT_STR)].todam;
    }

    static boolean IS_OUTSIDE(CHAR_DATA ch) {
        return !IS_SET(ch.in_room.room_flags, ROOM_INDOORS);
    }

    static void WAIT_STATE(CHAR_DATA ch, int npulse) {
        ch.wait = (IS_IMMORTAL(ch) ? 1 : UMAX(ch.wait, npulse));
    }

    static void DAZE_STATE(CHAR_DATA ch, int npulse) {
        ch.daze = UMAX(ch.daze, npulse);
    }

    static int get_carry_weight(CHAR_DATA ch) {
        return ch.carry_weight + ch.silver / 10 + ch.gold * 2 / 5;
    }
/*
 * room macros
 */

    static boolean IS_ROOM_AFFECTED(ROOM_INDEX_DATA room, int sn) {
        return IS_SET(room.affected_by, sn);
    }

    static boolean IS_RAFFECTED(ROOM_INDEX_DATA room, int sn) {
        return IS_SET(room.affected_by, sn);
    }

    static CHAR_DATA MOUNTED(CHAR_DATA ch) {
        return !IS_NPC(ch) && ch.mount != null && ch.riding ? ch.mount : null;
    }

    static CHAR_DATA RIDDEN(CHAR_DATA ch) {
        return IS_NPC(ch) && ch.mount != null && ch.riding ? ch.mount : null;
    }

    static boolean IS_DRUNK(CHAR_DATA ch) {
        return !IS_NPC(ch) && ch.pcdata.condition[COND_DRUNK] > 10;
    }

    static boolean IS_GOLEM(CHAR_DATA ch) {
        return IS_NPC(ch) && (ch.pIndexData.vnum == MOB_VNUM_LESSER_GOLEM
                || ch.pIndexData.vnum == MOB_VNUM_STONE_GOLEM
                || ch.pIndexData.vnum == MOB_VNUM_IRON_GOLEM
                || ch.pIndexData.vnum == MOB_VNUM_ADAMANTITE_GOLEM);
    }
/*
 * Object macros.
 */

    static boolean CAN_WEAR(OBJ_INDEX_DATA obj, int part) {
        return IS_SET(obj.wear_flags, part);
    }

    static boolean CAN_WEAR(OBJ_DATA obj, int part) {
        return IS_SET(obj.wear_flags, part);
    }

    static boolean IS_OBJ_STAT(OBJ_INDEX_DATA obj, int stat) {
        return IS_SET(obj.extra_flags, stat);
    }

    static boolean IS_OBJ_STAT(OBJ_DATA obj, int stat) {
        return IS_SET(obj.extra_flags, stat);
    }

    static boolean IS_WEAPON_STAT(OBJ_DATA obj, int stat) {
        return IS_SET(obj.value[4], stat);
    }

    static int WEIGHT_MULT(OBJ_DATA obj) {
        return obj.item_type == ITEM_CONTAINER ? obj.value[4] : 100;
    }

/* skill defines */

    static boolean CLEVEL_OK(CHAR_DATA ch, Skill skill) {
        return ch.level >= skill.skill_level[ch.clazz.id];
    }

    static boolean RACE_OK(CHAR_DATA ch, Skill skill) {
        return skill.race == null || skill.race == ch.race;
    }

    static boolean CABAL_OK(CHAR_DATA ch, Skill skill) {
        return skill.cabal == CABAL_NONE || skill.cabal == ch.cabal;
    }

    static boolean ALIGN_OK(CHAR_DATA ch, Skill skill) {
        return skill.align == -1 || skill.align == ch.alignment;
    }

/*
 * Description macros.
 */

    static String PERS(CHAR_DATA ch, CHAR_DATA looker) {
        return can_see(looker, ch) ?
                IS_NPC(ch) ? ch.short_descr : ((IS_VAMPIRE(ch) && !IS_IMMORTAL(looker)) ? "An ugly creature" : ch.name)
                : (!IS_NPC(ch) && ch.level > LEVEL_HERO) ? "an immortal" : "someone";
    }
/* new defines */

    static int MAX_CHARM(CHAR_DATA ch) {
        return (get_curr_stat(ch, STAT_INT) / 6) + ch.level / 45;
    }

/*
 * Structure for a social in the socials table.
 */

    static final class social_type {
        String name;
        int minPos;

        String found_char;
        String found_victim;
        String found_novictim;

        String noarg_char;
        String noarg_room;

        String self_char;
        String self_room;

        String not_found_char;
    }


    /*
    * Global constants.
    */
    static final ArrayList<social_type> social_table = new ArrayList<social_type>(MAX_SOCIALS);

    /*
    * Global variables.
    */
    static final Configuration nw_config = new Configuration();

    static AREA_DATA area_first;
    static HELP_DATA help_first;
    static SHOP_DATA shop_first;

    static CHAR_DATA char_list;
    static DESCRIPTOR_DATA descriptor_list;
    static OBJ_DATA object_list;

    static AUCTION_DATA auction;
    static ROOM_INDEX_DATA top_affected_room;

    static int current_time;
    static int limit_time;
    static int boot_time;
    static boolean fLogAll;
    static int total_levels;
    static int reboot_counter;
    static int time_sync;
    static int max_newbies;
    static int max_oldies;
    static int iNumPlayers;


    static void exit(int code) {
        new Exception("Exiting with code " + code + " ...").printStackTrace();
        System.exit(1);
    }

    static void perror(String msg) {
        System.err.println("PERROR" + msg);
    }


    static int currentTimeSeconds() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    static int atoi(String num) {
        try {
            return Integer.parseInt(num);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static void sprintf(TextBuffer tb, String text, Object... args) {
        tb.sprintf(text, args);
    }

    static String crypt(String s1, String s2) {
        if (s1.length() == 0) {
            return s1;
        }
        return s1 + s2.hashCode();
    }

}
