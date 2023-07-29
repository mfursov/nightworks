package net.sf.nightworks;

import static net.sf.nightworks.Nightworks.A;
import static net.sf.nightworks.Nightworks.B;
import static net.sf.nightworks.Nightworks.BIT_26;
import static net.sf.nightworks.Nightworks.BIT_27;
import static net.sf.nightworks.Nightworks.BIT_28;
import static net.sf.nightworks.Nightworks.BIT_29;
import static net.sf.nightworks.Nightworks.BIT_30;
import static net.sf.nightworks.Nightworks.C;
import static net.sf.nightworks.Nightworks.COMM_AFK;
import static net.sf.nightworks.Nightworks.COMM_BRIEF;
import static net.sf.nightworks.Nightworks.COMM_COMBINE;
import static net.sf.nightworks.Nightworks.COMM_COMPACT;
import static net.sf.nightworks.Nightworks.COMM_DEAF;
import static net.sf.nightworks.Nightworks.COMM_NOCHANNELS;
import static net.sf.nightworks.Nightworks.COMM_NOEMOTE;
import static net.sf.nightworks.Nightworks.COMM_NOGOSSIP;
import static net.sf.nightworks.Nightworks.COMM_NOGRATS;
import static net.sf.nightworks.Nightworks.COMM_NOMUSIC;
import static net.sf.nightworks.Nightworks.COMM_NOQUESTION;
import static net.sf.nightworks.Nightworks.COMM_NOQUOTE;
import static net.sf.nightworks.Nightworks.COMM_NOSHOUT;
import static net.sf.nightworks.Nightworks.COMM_NOTELL;
import static net.sf.nightworks.Nightworks.COMM_NOWIZ;
import static net.sf.nightworks.Nightworks.COMM_PROMPT;
import static net.sf.nightworks.Nightworks.COMM_QUIET;
import static net.sf.nightworks.Nightworks.COMM_SHOUTSOFF;
import static net.sf.nightworks.Nightworks.COMM_SHOW_AFFECTS;
import static net.sf.nightworks.Nightworks.COMM_SNOOP_PROOF;
import static net.sf.nightworks.Nightworks.COMM_TELNET_GA;
import static net.sf.nightworks.Nightworks.COMM_true_TRUST;
import static net.sf.nightworks.Nightworks.CR_ALL;
import static net.sf.nightworks.Nightworks.CR_EVIL;
import static net.sf.nightworks.Nightworks.CR_GOOD;
import static net.sf.nightworks.Nightworks.CR_NEUTRAL;
import static net.sf.nightworks.Nightworks.D;
import static net.sf.nightworks.Nightworks.E;
import static net.sf.nightworks.Nightworks.ETHOS_ANY;
import static net.sf.nightworks.Nightworks.ETHOS_CHAOTIC;
import static net.sf.nightworks.Nightworks.ETHOS_LAWFUL;
import static net.sf.nightworks.Nightworks.ETHOS_NEUTRAL;
import static net.sf.nightworks.Nightworks.F;
import static net.sf.nightworks.Nightworks.FORM_AMPHIBIAN;
import static net.sf.nightworks.Nightworks.FORM_ANIMAL;
import static net.sf.nightworks.Nightworks.FORM_BIPED;
import static net.sf.nightworks.Nightworks.FORM_BIRD;
import static net.sf.nightworks.Nightworks.FORM_BLOB;
import static net.sf.nightworks.Nightworks.FORM_CENTAUR;
import static net.sf.nightworks.Nightworks.FORM_COLD_BLOOD;
import static net.sf.nightworks.Nightworks.FORM_CONSTRUCT;
import static net.sf.nightworks.Nightworks.FORM_CRUSTACEAN;
import static net.sf.nightworks.Nightworks.FORM_DRAGON;
import static net.sf.nightworks.Nightworks.FORM_EDIBLE;
import static net.sf.nightworks.Nightworks.FORM_FISH;
import static net.sf.nightworks.Nightworks.FORM_INSECT;
import static net.sf.nightworks.Nightworks.FORM_INSTANT_DECAY;
import static net.sf.nightworks.Nightworks.FORM_INTANGIBLE;
import static net.sf.nightworks.Nightworks.FORM_MAGICAL;
import static net.sf.nightworks.Nightworks.FORM_MAMMAL;
import static net.sf.nightworks.Nightworks.FORM_MIST;
import static net.sf.nightworks.Nightworks.FORM_OTHER;
import static net.sf.nightworks.Nightworks.FORM_POISON;
import static net.sf.nightworks.Nightworks.FORM_REPTILE;
import static net.sf.nightworks.Nightworks.FORM_SENTIENT;
import static net.sf.nightworks.Nightworks.FORM_SNAKE;
import static net.sf.nightworks.Nightworks.FORM_SPIDER;
import static net.sf.nightworks.Nightworks.FORM_UNDEAD;
import static net.sf.nightworks.Nightworks.FORM_WORM;
import static net.sf.nightworks.Nightworks.G;
import static net.sf.nightworks.Nightworks.GROUP_ATTACK;
import static net.sf.nightworks.Nightworks.GROUP_BEGUILING;
import static net.sf.nightworks.Nightworks.GROUP_BENEDICTIONS;
import static net.sf.nightworks.Nightworks.GROUP_CABAL;
import static net.sf.nightworks.Nightworks.GROUP_COMBAT;
import static net.sf.nightworks.Nightworks.GROUP_CREATION;
import static net.sf.nightworks.Nightworks.GROUP_CURATIVE;
import static net.sf.nightworks.Nightworks.GROUP_DEFENSIVE;
import static net.sf.nightworks.Nightworks.GROUP_DETECTION;
import static net.sf.nightworks.Nightworks.GROUP_DRACONIAN;
import static net.sf.nightworks.Nightworks.GROUP_ENCHANTMENT;
import static net.sf.nightworks.Nightworks.GROUP_ENHANCEMENT;
import static net.sf.nightworks.Nightworks.GROUP_FIGHTMASTER;
import static net.sf.nightworks.Nightworks.GROUP_HARMFUL;
import static net.sf.nightworks.Nightworks.GROUP_HEALING;
import static net.sf.nightworks.Nightworks.GROUP_ILLUSION;
import static net.sf.nightworks.Nightworks.GROUP_MALADICTIONS;
import static net.sf.nightworks.Nightworks.GROUP_MEDITATION;
import static net.sf.nightworks.Nightworks.GROUP_NONE;
import static net.sf.nightworks.Nightworks.GROUP_PROTECTIVE;
import static net.sf.nightworks.Nightworks.GROUP_SUDDENDEATH;
import static net.sf.nightworks.Nightworks.GROUP_TRANSPORTATION;
import static net.sf.nightworks.Nightworks.GROUP_WEAPONSMASTER;
import static net.sf.nightworks.Nightworks.GROUP_WEATHER;
import static net.sf.nightworks.Nightworks.GROUP_WIZARD;
import static net.sf.nightworks.Nightworks.H;
import static net.sf.nightworks.Nightworks.I;
import static net.sf.nightworks.Nightworks.J;
import static net.sf.nightworks.Nightworks.K;
import static net.sf.nightworks.Nightworks.L;
import static net.sf.nightworks.Nightworks.LANG_CAT;
import static net.sf.nightworks.Nightworks.LANG_COMMON;
import static net.sf.nightworks.Nightworks.LANG_DWARVISH;
import static net.sf.nightworks.Nightworks.LANG_ELVISH;
import static net.sf.nightworks.Nightworks.LANG_GIANT;
import static net.sf.nightworks.Nightworks.LANG_GNOMISH;
import static net.sf.nightworks.Nightworks.LANG_HUMAN;
import static net.sf.nightworks.Nightworks.LANG_TROLLISH;
import static net.sf.nightworks.Nightworks.M;
import static net.sf.nightworks.Nightworks.MAX_LANGUAGE;
import static net.sf.nightworks.Nightworks.N;
import static net.sf.nightworks.Nightworks.O;
import static net.sf.nightworks.Nightworks.P;
import static net.sf.nightworks.Nightworks.PART_ARMS;
import static net.sf.nightworks.Nightworks.PART_BRAINS;
import static net.sf.nightworks.Nightworks.PART_CLAWS;
import static net.sf.nightworks.Nightworks.PART_EAR;
import static net.sf.nightworks.Nightworks.PART_EYE;
import static net.sf.nightworks.Nightworks.PART_EYESTALKS;
import static net.sf.nightworks.Nightworks.PART_FANGS;
import static net.sf.nightworks.Nightworks.PART_FEET;
import static net.sf.nightworks.Nightworks.PART_FINGERS;
import static net.sf.nightworks.Nightworks.PART_FINS;
import static net.sf.nightworks.Nightworks.PART_GUTS;
import static net.sf.nightworks.Nightworks.PART_HANDS;
import static net.sf.nightworks.Nightworks.PART_HEAD;
import static net.sf.nightworks.Nightworks.PART_HEART;
import static net.sf.nightworks.Nightworks.PART_HORNS;
import static net.sf.nightworks.Nightworks.PART_LEGS;
import static net.sf.nightworks.Nightworks.PART_LONG_TONGUE;
import static net.sf.nightworks.Nightworks.PART_SCALES;
import static net.sf.nightworks.Nightworks.PART_TAIL;
import static net.sf.nightworks.Nightworks.PART_TENTACLES;
import static net.sf.nightworks.Nightworks.PART_TUSKS;
import static net.sf.nightworks.Nightworks.PART_WINGS;
import static net.sf.nightworks.Nightworks.Q;
import static net.sf.nightworks.Nightworks.R;
import static net.sf.nightworks.Nightworks.RES_ACID;
import static net.sf.nightworks.Nightworks.RES_BASH;
import static net.sf.nightworks.Nightworks.RES_CHARM;
import static net.sf.nightworks.Nightworks.RES_COLD;
import static net.sf.nightworks.Nightworks.RES_DISEASE;
import static net.sf.nightworks.Nightworks.RES_DROWNING;
import static net.sf.nightworks.Nightworks.RES_ENERGY;
import static net.sf.nightworks.Nightworks.RES_FIRE;
import static net.sf.nightworks.Nightworks.RES_HOLY;
import static net.sf.nightworks.Nightworks.RES_IRON;
import static net.sf.nightworks.Nightworks.RES_LIGHT;
import static net.sf.nightworks.Nightworks.RES_LIGHTNING;
import static net.sf.nightworks.Nightworks.RES_MAGIC;
import static net.sf.nightworks.Nightworks.RES_MENTAL;
import static net.sf.nightworks.Nightworks.RES_NEGATIVE;
import static net.sf.nightworks.Nightworks.RES_PIERCE;
import static net.sf.nightworks.Nightworks.RES_POISON;
import static net.sf.nightworks.Nightworks.RES_SILVER;
import static net.sf.nightworks.Nightworks.RES_SLASH;
import static net.sf.nightworks.Nightworks.RES_SOUND;
import static net.sf.nightworks.Nightworks.RES_SUMMON;
import static net.sf.nightworks.Nightworks.RES_WEAPON;
import static net.sf.nightworks.Nightworks.RES_WOOD;
import static net.sf.nightworks.Nightworks.S;
import static net.sf.nightworks.Nightworks.SEX_FEMALE;
import static net.sf.nightworks.Nightworks.SEX_MALE;
import static net.sf.nightworks.Nightworks.SIZE_GARGANTUAN;
import static net.sf.nightworks.Nightworks.SIZE_GIANT;
import static net.sf.nightworks.Nightworks.SIZE_HUGE;
import static net.sf.nightworks.Nightworks.SIZE_LARGE;
import static net.sf.nightworks.Nightworks.SIZE_MEDIUM;
import static net.sf.nightworks.Nightworks.SIZE_SMALL;
import static net.sf.nightworks.Nightworks.SIZE_TINY;
import static net.sf.nightworks.Nightworks.STAT_CHA;
import static net.sf.nightworks.Nightworks.STAT_CON;
import static net.sf.nightworks.Nightworks.STAT_DEX;
import static net.sf.nightworks.Nightworks.STAT_INT;
import static net.sf.nightworks.Nightworks.STAT_STR;
import static net.sf.nightworks.Nightworks.STAT_WIS;
import static net.sf.nightworks.Nightworks.T;
import static net.sf.nightworks.Nightworks.U;
import static net.sf.nightworks.Nightworks.V;
import static net.sf.nightworks.Nightworks.VULN_ACID;
import static net.sf.nightworks.Nightworks.VULN_BASH;
import static net.sf.nightworks.Nightworks.VULN_CHARM;
import static net.sf.nightworks.Nightworks.VULN_COLD;
import static net.sf.nightworks.Nightworks.VULN_DISEASE;
import static net.sf.nightworks.Nightworks.VULN_DROWNING;
import static net.sf.nightworks.Nightworks.VULN_ENERGY;
import static net.sf.nightworks.Nightworks.VULN_FIRE;
import static net.sf.nightworks.Nightworks.VULN_HOLY;
import static net.sf.nightworks.Nightworks.VULN_IRON;
import static net.sf.nightworks.Nightworks.VULN_LIGHT;
import static net.sf.nightworks.Nightworks.VULN_LIGHTNING;
import static net.sf.nightworks.Nightworks.VULN_MAGIC;
import static net.sf.nightworks.Nightworks.VULN_MENTAL;
import static net.sf.nightworks.Nightworks.VULN_NEGATIVE;
import static net.sf.nightworks.Nightworks.VULN_PIERCE;
import static net.sf.nightworks.Nightworks.VULN_POISON;
import static net.sf.nightworks.Nightworks.VULN_SILVER;
import static net.sf.nightworks.Nightworks.VULN_SLASH;
import static net.sf.nightworks.Nightworks.VULN_SOUND;
import static net.sf.nightworks.Nightworks.VULN_SUMMON;
import static net.sf.nightworks.Nightworks.VULN_WEAPON;
import static net.sf.nightworks.Nightworks.VULN_WOOD;
import static net.sf.nightworks.Nightworks.W;
import static net.sf.nightworks.Nightworks.X;
import static net.sf.nightworks.Nightworks.Y;
import static net.sf.nightworks.Nightworks.Z;
import static net.sf.nightworks.Nightworks.cabal_type;
import static net.sf.nightworks.Nightworks.prac_type;
import static net.sf.nightworks.util.TextUtils.one_argument;

public class Tables {

    public static class flag_type {
        String name;
        int bit;
        boolean settable;

        flag_type(String name, int bit, boolean settable) {
            this.name = name;
            this.bit = bit;
            this.settable = settable;
        }

        public static String getFlagName(int bit, flag_type[] table) {
            for (flag_type flag : table) {
                if (flag.bit == bit) {
                    return flag.name;
                }
            }
            return "<unknown flag>";
        }

        public static long parseFlagsValue(String flags, flag_type[] table) {
            long res = 0;
            while (!flags.isEmpty()) {
                StringBuilder nextFlag = new StringBuilder();
                flags = one_argument(flags, nextFlag);
                long flag = getFlagValue(nextFlag.toString(), table);
                res = res | flag;
            }
            return res;
        }


        private static long getFlagValue(String oneFlagName, flag_type[] table) {
            for (flag_type flag : table) {
                if (oneFlagName.equals(flag.name)) {
                    return flag.bit;
                }
            }
            return 0;
        }

    }

    static class position_type {
        String name;
        String short_name;

        position_type(String name, String short_name) {
            this.name = name;
            this.short_name = short_name;
        }

        public static int getIndexInTable(String short_name) {
            for (int i = 0; i < position_table.length; i++) {
                position_type position_type = position_table[i];
                if (position_type.short_name.equals(short_name)) {
                    return i;
                }
            }
            return -1;
        }
    }

    static final flag_type[] ethos_table = {
            new flag_type("unknown", ETHOS_ANY, true),
            new flag_type("lawful", ETHOS_LAWFUL, true),
            new flag_type("neutral", ETHOS_NEUTRAL, true),
            new flag_type("chaotic", ETHOS_CHAOTIC, true)
    };

    /* for position */
    static final position_type[] position_table = {
            new position_type("dead", "dead"),
            new position_type("mortally wounded", "mort"),
            new position_type("incapacitated", "incap"),
            new position_type("stunned", "stun"),
            new position_type("sleeping", "sleep"),
            new position_type("resting", "rest"),
            new position_type("sitting", "sit"),
            new position_type("fighting", "fight"),
            new position_type("standing", "stand"),
    };

    /* for sex */
    static final flag_type[] sex_table = {
            new flag_type("male", SEX_MALE, true),
            new flag_type("female", SEX_FEMALE, true),
    };

    static final flag_type[] stat_names = {
            new flag_type("str", STAT_STR, true),
            new flag_type("int", STAT_INT, true),
            new flag_type("dex", STAT_DEX, true),
            new flag_type("wiz", STAT_WIS, true),
            new flag_type("con", STAT_CON, true),
            new flag_type("cha", STAT_CHA, true),
    };

    /* for sizes */
    static final flag_type[] size_table = {
            new flag_type("tiny", SIZE_TINY, true),
            new flag_type("small", SIZE_SMALL, true),
            new flag_type("medium", SIZE_MEDIUM, true),
            new flag_type("large", SIZE_LARGE, true),
            new flag_type("huge", SIZE_HUGE, true),
            new flag_type("giant", SIZE_GIANT, true),
            new flag_type("gargantuan", SIZE_GARGANTUAN, true),
    };

    /* various flag tables */
    static final flag_type[] act_flags = {
            new flag_type("npc", A, false),
            new flag_type("sentinel", B, true),
            new flag_type("scavenger", C, true),
            new flag_type("aggressive", F, true),
            new flag_type("stay_area", G, true),
            new flag_type("wimpy", H, true),
            new flag_type("pet", I, true),
            new flag_type("train", J, true),
            new flag_type("practice", K, true),
            new flag_type("undead", O, true),
            new flag_type("cleric", Q, true),
            new flag_type("mage", R, true),
            new flag_type("thief", S, true),
            new flag_type("warrior", T, true),
            new flag_type("noalign", U, true),
            new flag_type("nopurge", V, true),
            new flag_type("outdoors", W, true),
            new flag_type("indoors", Y, true),
            new flag_type("healer", BIT_26, true),
            new flag_type("gain", BIT_27, true),
            new flag_type("update_always", BIT_28, true),
            new flag_type("changer", BIT_29, true),
    };

    static final flag_type[] plr_flags = {
            new flag_type("npc", A, false),
            new flag_type("autoassist", C, false),
            new flag_type("autoexit", D, false),
            new flag_type("autoloot", E, false),
            new flag_type("autosac", F, false),
            new flag_type("autogold", G, false),
            new flag_type("autosplit", H, false),
            new flag_type("holylight", N, false),
            new flag_type("can_loot", P, false),
            new flag_type("nosummon", Q, false),
            new flag_type("nofollow", R, false),
            new flag_type("permit", U, true),
            new flag_type("log", W, false),
            new flag_type("deny", X, false),
            new flag_type("freeze", Y, false),
            new flag_type("thief", Z, false),
            new flag_type("killer", BIT_26, false),
            new flag_type("questor", BIT_27, false),
            new flag_type("vampire", BIT_28, false),
    };

    static final flag_type[] affect_flags =
            {
                    new flag_type("blind", A, true),
                    new flag_type("invisible", B, true),
                    new flag_type("sanctuary", H, true),
                    new flag_type("faerie_fire", I, true),
                    new flag_type("infrared", J, true),
                    new flag_type("curse", K, true),
                    new flag_type("poison", M, true),
                    new flag_type("protect_evil", N, true),
                    new flag_type("protect_good", O, true),
                    new flag_type("sneak", P, true),
                    new flag_type("hide", Q, true),
                    new flag_type("sleep", R, true),
                    new flag_type("charm", S, true),
                    new flag_type("flying", T, true),
                    new flag_type("pass_door", U, true),
                    new flag_type("haste", V, true),
                    new flag_type("calm", W, true),
                    new flag_type("plague", X, true),
                    new flag_type("weaken", Y, true),
                    new flag_type("wstun", Z, true),
                    new flag_type("berserk", BIT_26, true),
                    new flag_type("swim", BIT_27, true),
                    new flag_type("regeneration", BIT_28, true),
                    new flag_type("slow", BIT_29, true),
                    new flag_type("camouflage", BIT_30, true),
            };

    static final flag_type[] off_flags =
            {
                    new flag_type("area_attack", A, true),
                    new flag_type("backstab", B, true),
                    new flag_type("bash", C, true),
                    new flag_type("berserk", D, true),
                    new flag_type("disarm", E, true),
                    new flag_type("dodge", F, true),
                    new flag_type("fade", G, true),
                    new flag_type("fast", H, true),
                    new flag_type("kick", I, true),
                    new flag_type("dirt_kick", J, true),
                    new flag_type("parry", K, true),
                    new flag_type("rescue", L, true),
                    new flag_type("tail", M, true),
                    new flag_type("trip", N, true),
                    new flag_type("crush", O, true),
                    new flag_type("assist_all", P, true),
                    new flag_type("assist_align", Q, true),
                    new flag_type("assist_race", R, true),
                    new flag_type("assist_players", S, true),
                    new flag_type("assist_guard", T, true),
                    new flag_type("assist_vnum", U, true),
            };

    static final flag_type[] imm_flags =
            {
                    new flag_type("summon", A, true),
                    new flag_type("charm", B, true),
                    new flag_type("magic", C, true),
                    new flag_type("weapon", D, true),
                    new flag_type("bash", E, true),
                    new flag_type("pierce", F, true),
                    new flag_type("slash", G, true),
                    new flag_type("fire", H, true),
                    new flag_type("cold", I, true),
                    new flag_type("lightning", J, true),
                    new flag_type("acid", K, true),
                    new flag_type("poison", L, true),
                    new flag_type("negative", M, true),
                    new flag_type("holy", N, true),
                    new flag_type("energy", O, true),
                    new flag_type("mental", P, true),
                    new flag_type("disease", Q, true),
                    new flag_type("drowning", R, true),
                    new flag_type("light", S, true),
                    new flag_type("sound", T, true),
                    new flag_type("wood", X, true),
                    new flag_type("silver", Y, true),
                    new flag_type("iron", Z, true),
            };

    static final flag_type[] form_flags =
            {
                    new flag_type("edible", FORM_EDIBLE, true),
                    new flag_type("poison", FORM_POISON, true),
                    new flag_type("magical", FORM_MAGICAL, true),
                    new flag_type("instant_decay", FORM_INSTANT_DECAY, true),
                    new flag_type("other", FORM_OTHER, true),
                    new flag_type("animal", FORM_ANIMAL, true),
                    new flag_type("sentient", FORM_SENTIENT, true),
                    new flag_type("undead", FORM_UNDEAD, true),
                    new flag_type("construct", FORM_CONSTRUCT, true),
                    new flag_type("mist", FORM_MIST, true),
                    new flag_type("intangible", FORM_INTANGIBLE, true),
                    new flag_type("biped", FORM_BIPED, true),
                    new flag_type("centaur", FORM_CENTAUR, true),
                    new flag_type("insect", FORM_INSECT, true),
                    new flag_type("spider", FORM_SPIDER, true),
                    new flag_type("crustacean", FORM_CRUSTACEAN, true),
                    new flag_type("worm", FORM_WORM, true),
                    new flag_type("blob", FORM_BLOB, true),
                    new flag_type("mammal", FORM_MAMMAL, true),
                    new flag_type("bird", FORM_BIRD, true),
                    new flag_type("reptile", FORM_REPTILE, true),
                    new flag_type("snake", FORM_SNAKE, true),
                    new flag_type("dragon", FORM_DRAGON, true),
                    new flag_type("amphibian", FORM_AMPHIBIAN, true),
                    new flag_type("fish", FORM_FISH, true),
                    new flag_type("cold_blood", FORM_COLD_BLOOD, true),
            };

    static final flag_type[] part_flags =
            {
                    new flag_type("head", PART_HEAD, true),
                    new flag_type("arms", PART_ARMS, true),
                    new flag_type("legs", PART_LEGS, true),
                    new flag_type("heart", PART_HEART, true),
                    new flag_type("brains", PART_BRAINS, true),
                    new flag_type("guts", PART_GUTS, true),
                    new flag_type("hands", PART_HANDS, true),
                    new flag_type("feet", PART_FEET, true),
                    new flag_type("fingers", PART_FINGERS, true),
                    new flag_type("ear", PART_EAR, true),
                    new flag_type("eye", PART_EYE, true),
                    new flag_type("long_tongue", PART_LONG_TONGUE, true),
                    new flag_type("eyestalks", PART_EYESTALKS, true),
                    new flag_type("tentacles", PART_TENTACLES, true),
                    new flag_type("fins", PART_FINS, true),
                    new flag_type("wings", PART_WINGS, true),
                    new flag_type("tail", PART_TAIL, true),
                    new flag_type("claws", PART_CLAWS, true),
                    new flag_type("fangs", PART_FANGS, true),
                    new flag_type("horns", PART_HORNS, true),
                    new flag_type("scales", PART_SCALES, true),
                    new flag_type("tusks", PART_TUSKS, true),
            };

    static final flag_type[] comm_flags =
            {
                    new flag_type("quiet", COMM_QUIET, true),
                    new flag_type("deaf", COMM_DEAF, true),
                    new flag_type("nowiz", COMM_NOWIZ, true),
                    new flag_type("nogossip", COMM_NOGOSSIP, true),
                    new flag_type("noquestion", COMM_NOQUESTION, true),
                    new flag_type("nomusic", COMM_NOMUSIC, true),
                    new flag_type("noquote", COMM_NOQUOTE, true),
                    new flag_type("shoutsoff", COMM_SHOUTSOFF, true),
                    new flag_type("true_trust", COMM_true_TRUST, true),
                    new flag_type("compact", COMM_COMPACT, true),
                    new flag_type("brief", COMM_BRIEF, true),
                    new flag_type("prompt", COMM_PROMPT, true),
                    new flag_type("combine", COMM_COMBINE, true),
                    new flag_type("telnet_ga", COMM_TELNET_GA, true),
                    new flag_type("show_affects", COMM_SHOW_AFFECTS, true),
                    new flag_type("nograts", COMM_NOGRATS, true),
                    new flag_type("noemote", COMM_NOEMOTE, false),
                    new flag_type("noshout", COMM_NOSHOUT, false),
                    new flag_type("notell", COMM_NOTELL, false),
                    new flag_type("nochannels", COMM_NOCHANNELS, false),
                    new flag_type("snoop_proof", COMM_SNOOP_PROOF, false),
                    new flag_type("afk", COMM_AFK, true),
            };

    /* <longname> <shortname> <cabal item> <cabal shrine> null */
    static final cabal_type[] cabal_table =
            {
                    new cabal_type("None", "None", 0, 0, null),
                    new cabal_type("the Rulers of Nightworks", "RULER", 511, 512, null),
                    new cabal_type("the Dark Raiders of Nightworks", "INVADER", 561, 568, null),
                    new cabal_type("the Barons of Chaos", "CHAOS", 552, 554, null),
                    new cabal_type("the Masters of the Arcane Arts", "SHALAFI", 531, 530, null),
                    new cabal_type("the Masters of the Martial Arts", "BATTLERAGER", 541, 548, null),
                    new cabal_type("the Knights of Nightworks", "KNIGHT", 522, 524, null),
                    new cabal_type("the Leaders of Forests", "LION", 502, 504, null),
                    new cabal_type("the Mercanary of Nightworks", "HUNTER", 571, 573, null)
            };

    static final prac_type[] prac_table =
            {
                    new prac_type("none", "group_none", GROUP_NONE),
                    new prac_type("weaponsmaster", "group_weaponsmaster", GROUP_WEAPONSMASTER),
                    new prac_type("attack", "group_attack", GROUP_ATTACK),
                    new prac_type("beguiling", "group_beguiling", GROUP_BEGUILING),
                    new prac_type("benedictions", "group_benedictions", GROUP_BENEDICTIONS),
                    new prac_type("combat", "group_combat", GROUP_COMBAT),
                    new prac_type("creation", "group_creation", GROUP_CREATION),
                    new prac_type("curative", "group_curative", GROUP_CURATIVE),
                    new prac_type("detection", "group_detection", GROUP_DETECTION),
                    new prac_type("draconian", "group_draconian", GROUP_DRACONIAN),
                    new prac_type("enchantment", "group_enchantment", GROUP_ENCHANTMENT),
                    new prac_type("enhancement", "group_enhancement", GROUP_ENHANCEMENT),
                    new prac_type("harmful", "group_harmful", GROUP_HARMFUL),
                    new prac_type("healing", "group_healing", GROUP_HEALING),
                    new prac_type("illusion", "group_illusion", GROUP_ILLUSION),
                    new prac_type("maladictions", "group_maladictions", GROUP_MALADICTIONS),
                    new prac_type("protective", "group_protective", GROUP_PROTECTIVE),
                    new prac_type("transportation", "group_transportation", GROUP_TRANSPORTATION),
                    new prac_type("weather", "group_weather", GROUP_WEATHER),
                    new prac_type("fightmaster", "group_fightmaster", GROUP_FIGHTMASTER),
                    new prac_type("suddendeath", "group_suddendeath", GROUP_SUDDENDEATH),
                    new prac_type("meditation", "group_meditation", GROUP_MEDITATION),
                    new prac_type("cabal", "group_cabal", GROUP_CABAL),
                    new prac_type("defensive", "group_defensive", GROUP_DEFENSIVE),
                    new prac_type("wizard", "group_wizard", GROUP_WIZARD),
            };

    static final flag_type[] vuln_flags =
            {
                    new flag_type("", 0, false),
                    new flag_type("summon", VULN_SUMMON, true),
                    new flag_type("charm", VULN_CHARM, true),
                    new flag_type("magic", VULN_MAGIC, true),
                    new flag_type("weapon", VULN_WEAPON, true),
                    new flag_type("bash", VULN_BASH, true),
                    new flag_type("pierce", VULN_PIERCE, true),
                    new flag_type("slash", VULN_SLASH, true),
                    new flag_type("fire", VULN_FIRE, true),
                    new flag_type("cold", VULN_COLD, true),
                    new flag_type("lightning", VULN_LIGHTNING, true),
                    new flag_type("acid", VULN_ACID, true),
                    new flag_type("poison", VULN_POISON, true),
                    new flag_type("negative", VULN_NEGATIVE, true),
                    new flag_type("holy", VULN_HOLY, true),
                    new flag_type("energy", VULN_ENERGY, true),
                    new flag_type("mental", VULN_MENTAL, true),
                    new flag_type("disease", VULN_DISEASE, true),
                    new flag_type("drowning", VULN_DROWNING, true),
                    new flag_type("light", VULN_LIGHT, true),
                    new flag_type("sound", VULN_SOUND, true),
                    new flag_type("wood", VULN_WOOD, true),
                    new flag_type("silver", VULN_SILVER, true),
                    new flag_type("iron", VULN_IRON, true),
            };


    static final flag_type[] res_flags = {
            new flag_type("", 0, false),
            new flag_type("summon", RES_SUMMON, true),
            new flag_type("charm", RES_CHARM, true),
            new flag_type("magic", RES_MAGIC, true),
            new flag_type("weapon", RES_WEAPON, true),
            new flag_type("bash", RES_BASH, true),
            new flag_type("pierce", RES_PIERCE, true),
            new flag_type("slash", RES_SLASH, true),
            new flag_type("fire", RES_FIRE, true),
            new flag_type("cold", RES_COLD, true),
            new flag_type("lightning", RES_LIGHTNING, true),
            new flag_type("acid", RES_ACID, true),
            new flag_type("poison", RES_POISON, true),
            new flag_type("negative", RES_NEGATIVE, true),
            new flag_type("holy", RES_HOLY, true),
            new flag_type("energy", RES_ENERGY, true),
            new flag_type("mental", RES_MENTAL, true),
            new flag_type("disease", RES_DISEASE, true),
            new flag_type("drowning", RES_DROWNING, true),
            new flag_type("light", RES_LIGHT, true),
            new flag_type("sound", RES_SOUND, true),
            new flag_type("wood", RES_WOOD, true),
            new flag_type("silver", RES_SILVER, true),
            new flag_type("iron", RES_IRON, true),
    };

    static final flag_type[] align_flags = {
            new flag_type("all", CR_ALL, true),
            new flag_type("good", CR_GOOD, true),
            new flag_type("neutral", CR_NEUTRAL, true),
            new flag_type("evil", CR_EVIL, true)
    };

    static final flag_type[] slang_table = {
            new flag_type("common", LANG_COMMON, true),
            new flag_type("human", LANG_HUMAN, true),
            new flag_type("elvish", LANG_ELVISH, true),
            new flag_type("dwarvish", LANG_DWARVISH, true),
            new flag_type("gnomish", LANG_GNOMISH, true),
            new flag_type("giant", LANG_GIANT, true),
            new flag_type("trollish", LANG_TROLLISH, true),
            new flag_type("cat", LANG_CAT, true),
            new flag_type("mothertongue", MAX_LANGUAGE, true),

    };

}
