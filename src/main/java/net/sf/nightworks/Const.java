package net.sf.nightworks;

import static net.sf.nightworks.Interp.IM;
import static net.sf.nightworks.Interp.L1;
import static net.sf.nightworks.Interp.L2;
import static net.sf.nightworks.Interp.L4;
import static net.sf.nightworks.Interp.L5;
import static net.sf.nightworks.Interp.L7;
import static net.sf.nightworks.Nightworks.DAM_ACID;
import static net.sf.nightworks.Nightworks.DAM_BASH;
import static net.sf.nightworks.Nightworks.DAM_COLD;
import static net.sf.nightworks.Nightworks.DAM_ENERGY;
import static net.sf.nightworks.Nightworks.DAM_FIRE;
import static net.sf.nightworks.Nightworks.DAM_HOLY;
import static net.sf.nightworks.Nightworks.DAM_LIGHTNING;
import static net.sf.nightworks.Nightworks.DAM_NEGATIVE;
import static net.sf.nightworks.Nightworks.DAM_PIERCE;
import static net.sf.nightworks.Nightworks.DAM_SLASH;
import static net.sf.nightworks.Nightworks.ITEM_ARMOR;
import static net.sf.nightworks.Nightworks.ITEM_BOAT;
import static net.sf.nightworks.Nightworks.ITEM_CLOTHING;
import static net.sf.nightworks.Nightworks.ITEM_CONTAINER;
import static net.sf.nightworks.Nightworks.ITEM_CORPSE_NPC;
import static net.sf.nightworks.Nightworks.ITEM_CORPSE_PC;
import static net.sf.nightworks.Nightworks.ITEM_DRINK_CON;
import static net.sf.nightworks.Nightworks.ITEM_FOOD;
import static net.sf.nightworks.Nightworks.ITEM_FOUNTAIN;
import static net.sf.nightworks.Nightworks.ITEM_FURNITURE;
import static net.sf.nightworks.Nightworks.ITEM_GEM;
import static net.sf.nightworks.Nightworks.ITEM_JEWELRY;
import static net.sf.nightworks.Nightworks.ITEM_JUKEBOX;
import static net.sf.nightworks.Nightworks.ITEM_KEY;
import static net.sf.nightworks.Nightworks.ITEM_LIGHT;
import static net.sf.nightworks.Nightworks.ITEM_MAP;
import static net.sf.nightworks.Nightworks.ITEM_MONEY;
import static net.sf.nightworks.Nightworks.ITEM_PILL;
import static net.sf.nightworks.Nightworks.ITEM_PORTAL;
import static net.sf.nightworks.Nightworks.ITEM_POTION;
import static net.sf.nightworks.Nightworks.ITEM_PROTECT;
import static net.sf.nightworks.Nightworks.ITEM_ROOM_KEY;
import static net.sf.nightworks.Nightworks.ITEM_SCROLL;
import static net.sf.nightworks.Nightworks.ITEM_STAFF;
import static net.sf.nightworks.Nightworks.ITEM_TATTOO;
import static net.sf.nightworks.Nightworks.ITEM_TRASH;
import static net.sf.nightworks.Nightworks.ITEM_TREASURE;
import static net.sf.nightworks.Nightworks.ITEM_WAND;
import static net.sf.nightworks.Nightworks.ITEM_WARP_STONE;
import static net.sf.nightworks.Nightworks.ITEM_WEAPON;
import static net.sf.nightworks.Nightworks.LANG_CAT;
import static net.sf.nightworks.Nightworks.LANG_COMMON;
import static net.sf.nightworks.Nightworks.LANG_DWARVISH;
import static net.sf.nightworks.Nightworks.LANG_ELVISH;
import static net.sf.nightworks.Nightworks.LANG_GIANT;
import static net.sf.nightworks.Nightworks.LANG_GNOMISH;
import static net.sf.nightworks.Nightworks.LANG_HUMAN;
import static net.sf.nightworks.Nightworks.LANG_TROLLISH;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SCHOOL_AXE;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SCHOOL_BOW;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SCHOOL_DAGGER;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SCHOOL_FLAIL;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SCHOOL_LANCE;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SCHOOL_MACE;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SCHOOL_POLEARM;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SCHOOL_STAFF;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SCHOOL_SWORD;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SCHOOL_WHIP;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_TATTOO_AHRUMAZDA;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_TATTOO_APOLLON;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_TATTOO_ARES;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_TATTOO_ATHENA;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_TATTOO_DEIMOS;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_TATTOO_EHRUMEN;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_TATTOO_EROS;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_TATTOO_GOKTENGRI;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_TATTOO_HEPHAESTUS;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_TATTOO_HERA;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_TATTOO_MARS;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_TATTOO_ODIN;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_TATTOO_PHOBOS;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_TATTOO_PROMETHEUS;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_TATTOO_SIEBELE;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_TATTOO_VENUS;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_TATTOO_ZEUS;
import static net.sf.nightworks.Nightworks.WEAPON_ARROW;
import static net.sf.nightworks.Nightworks.WEAPON_AXE;
import static net.sf.nightworks.Nightworks.WEAPON_BOW;
import static net.sf.nightworks.Nightworks.WEAPON_DAGGER;
import static net.sf.nightworks.Nightworks.WEAPON_FLAIL;
import static net.sf.nightworks.Nightworks.WEAPON_LANCE;
import static net.sf.nightworks.Nightworks.WEAPON_MACE;
import static net.sf.nightworks.Nightworks.WEAPON_POLEARM;
import static net.sf.nightworks.Nightworks.WEAPON_SPEAR;
import static net.sf.nightworks.Nightworks.WEAPON_SWORD;
import static net.sf.nightworks.Nightworks.WEAPON_WHIP;
import static net.sf.nightworks.Nightworks.WIZ_DEATHS;
import static net.sf.nightworks.Nightworks.WIZ_FLAGS;
import static net.sf.nightworks.Nightworks.WIZ_LEVELS;
import static net.sf.nightworks.Nightworks.WIZ_LINKS;
import static net.sf.nightworks.Nightworks.WIZ_LOAD;
import static net.sf.nightworks.Nightworks.WIZ_LOGINS;
import static net.sf.nightworks.Nightworks.WIZ_MOBDEATHS;
import static net.sf.nightworks.Nightworks.WIZ_NEWBIE;
import static net.sf.nightworks.Nightworks.WIZ_ON;
import static net.sf.nightworks.Nightworks.WIZ_PENALTIES;
import static net.sf.nightworks.Nightworks.WIZ_PREFIX;
import static net.sf.nightworks.Nightworks.WIZ_RESETS;
import static net.sf.nightworks.Nightworks.WIZ_RESTORE;
import static net.sf.nightworks.Nightworks.WIZ_SACCING;
import static net.sf.nightworks.Nightworks.WIZ_SECURE;
import static net.sf.nightworks.Nightworks.WIZ_SITES;
import static net.sf.nightworks.Nightworks.WIZ_SNOOPS;
import static net.sf.nightworks.Nightworks.WIZ_SPAM;
import static net.sf.nightworks.Nightworks.WIZ_SWITCHES;
import static net.sf.nightworks.Nightworks.WIZ_TICKS;
import static net.sf.nightworks.Nightworks.attack_type;
import static net.sf.nightworks.Nightworks.con_app_type;
import static net.sf.nightworks.Nightworks.dex_app_type;
import static net.sf.nightworks.Nightworks.hometown_type;
import static net.sf.nightworks.Nightworks.int_app_type;
import static net.sf.nightworks.Nightworks.item_type;
import static net.sf.nightworks.Nightworks.language_type;
import static net.sf.nightworks.Nightworks.liq_type;
import static net.sf.nightworks.Nightworks.religion_type;
import static net.sf.nightworks.Nightworks.str_app_type;
import static net.sf.nightworks.Nightworks.translation_type;
import static net.sf.nightworks.Nightworks.wis_app_type;
import static net.sf.nightworks.Nightworks.wiznet_type;

/**
 * Definitions of different constants and tables
 */
class Const {
    /* language staff */
    static final translation_type[] translation_table =
            {
                    new translation_type('a', 'e'),
                    new translation_type('A', 'E'),
                    new translation_type('b', 'c'),
                    new translation_type('B', 'C'),
                    new translation_type('c', 'd'),
                    new translation_type('C', 'D'),
                    new translation_type('d', 'f'),
                    new translation_type('D', 'F'),
                    new translation_type('e', 'i'),
                    new translation_type('E', 'I'),
                    new translation_type('f', 'g'),
                    new translation_type('F', 'G'),
                    new translation_type('g', 'h'),
                    new translation_type('G', 'H'),
                    new translation_type('h', 'j'),
                    new translation_type('H', 'J'),
                    new translation_type('i', 'o'),
                    new translation_type('I', 'O'),
                    new translation_type('j', 'k'),
                    new translation_type('J', 'K'),
                    new translation_type('k', 'l'),
                    new translation_type('K', 'L'),
                    new translation_type('l', 'm'),
                    new translation_type('L', 'M'),
                    new translation_type('m', 'n'),
                    new translation_type('M', 'N'),
                    new translation_type('n', 'p'),
                    new translation_type('N', 'P'),
                    new translation_type('o', 'u'),
                    new translation_type('O', 'U'),
                    new translation_type('p', 'q'),
                    new translation_type('P', 'Q'),
                    new translation_type('q', 'r'),
                    new translation_type('Q', 'R'),
                    new translation_type('r', 's'),
                    new translation_type('R', 'S'),
                    new translation_type('s', 't'),
                    new translation_type('S', 'T'),
                    new translation_type('t', 'v'),
                    new translation_type('T', 'V'),
                    new translation_type('u', 'y'),
                    new translation_type('U', 'Y'),
                    new translation_type('v', 'w'),
                    new translation_type('V', 'W'),
                    new translation_type('w', 'x'),
                    new translation_type('W', 'X'),
                    new translation_type('x', 'z'),
                    new translation_type('X', 'Z'),
                    new translation_type('y', 'a'),
                    new translation_type('Y', 'A'),
                    new translation_type('z', 'b'),
                    new translation_type('Z', 'B'),
                    new translation_type('\0', '\0')
            };

    static final language_type[] language_table =
            {
                    new language_type("common", LANG_COMMON),
                    new language_type("human", LANG_HUMAN),
                    new language_type("elvish", LANG_ELVISH),
                    new language_type("dwarvish", LANG_DWARVISH),
                    new language_type("gnomish", LANG_GNOMISH),
                    new language_type("giant", LANG_GIANT),
                    new language_type("trollish", LANG_TROLLISH),
                    new language_type("cat", LANG_CAT)
            };

    /* item type list */
    static final item_type[] item_table =
            {
                    new item_type(ITEM_LIGHT, "light"),
                    new item_type(ITEM_SCROLL, "scroll"),
                    new item_type(ITEM_WAND, "wand"),
                    new item_type(ITEM_STAFF, "staff"),
                    new item_type(ITEM_WEAPON, "weapon"),
                    new item_type(ITEM_TREASURE, "treasure"),
                    new item_type(ITEM_ARMOR, "armor"),
                    new item_type(ITEM_POTION, "potion"),
                    new item_type(ITEM_CLOTHING, "clothing"),
                    new item_type(ITEM_FURNITURE, "furniture"),
                    new item_type(ITEM_TRASH, "trash"),
                    new item_type(ITEM_CONTAINER, "container"),
                    new item_type(ITEM_DRINK_CON, "drink"),
                    new item_type(ITEM_KEY, "key"),
                    new item_type(ITEM_FOOD, "food"),
                    new item_type(ITEM_MONEY, "money"),
                    new item_type(ITEM_BOAT, "boat"),
                    new item_type(ITEM_CORPSE_NPC, "npc_corpse"),
                    new item_type(ITEM_CORPSE_PC, "pc_corpse"),
                    new item_type(ITEM_FOUNTAIN, "fountain"),
                    new item_type(ITEM_PILL, "pill"),
                    new item_type(ITEM_PROTECT, "protect"),
                    new item_type(ITEM_MAP, "map"),
                    new item_type(ITEM_PORTAL, "portal"),
                    new item_type(ITEM_WARP_STONE, "warp_stone"),
                    new item_type(ITEM_ROOM_KEY, "room_key"),
                    new item_type(ITEM_GEM, "gem"),
                    new item_type(ITEM_JEWELRY, "jewelry"),
                    new item_type(ITEM_JUKEBOX, "jukebox"),
                    new item_type(ITEM_TATTOO, "tattoo"),
                    new item_type(0, null)
            };


    static class weapon_type {
        String name;
        int vnum;
        int type;
        Skill gsn;

        public weapon_type(String name, int vnum, int type, Skill gsn) {
            this.name = name;
            this.vnum = vnum;
            this.type = type;
            this.gsn = gsn;
        }
    }

    /* weapon selection table */
    static final weapon_type[] weapon_table = {
            new weapon_type("sword", OBJ_VNUM_SCHOOL_SWORD, WEAPON_SWORD, Skill.gsn_sword),
            new weapon_type("mace", OBJ_VNUM_SCHOOL_MACE, WEAPON_MACE, Skill.gsn_mace),
            new weapon_type("dagger", OBJ_VNUM_SCHOOL_DAGGER, WEAPON_DAGGER, Skill.gsn_dagger),
            new weapon_type("axe", OBJ_VNUM_SCHOOL_AXE, WEAPON_AXE, Skill.gsn_axe),
            new weapon_type("staff", OBJ_VNUM_SCHOOL_STAFF, WEAPON_SPEAR, Skill.gsn_spear),
            new weapon_type("flail", OBJ_VNUM_SCHOOL_FLAIL, WEAPON_FLAIL, Skill.gsn_flail),
            new weapon_type("whip", OBJ_VNUM_SCHOOL_WHIP, WEAPON_WHIP, Skill.gsn_whip),
            new weapon_type("polearm", OBJ_VNUM_SCHOOL_POLEARM, WEAPON_POLEARM, Skill.gsn_polearm),
            new weapon_type("bow", OBJ_VNUM_SCHOOL_BOW, WEAPON_BOW, Skill.gsn_bow),
            new weapon_type("arrow", OBJ_VNUM_SCHOOL_POLEARM, WEAPON_ARROW, Skill.gsn_arrow),
            new weapon_type("lance", OBJ_VNUM_SCHOOL_LANCE, WEAPON_LANCE, Skill.gsn_lance),
            new weapon_type(null, 0, 0, null)
    };

    /* wiznet table and prototype for future flag setting */
    static final wiznet_type[] wiznet_table = {
            new wiznet_type("on", WIZ_ON, IM),
            new wiznet_type("prefix", WIZ_PREFIX, IM),
            new wiznet_type("ticks", WIZ_TICKS, IM),
            new wiznet_type("logins", WIZ_LOGINS, IM),
            new wiznet_type("sites", WIZ_SITES, L4),
            new wiznet_type("links", WIZ_LINKS, L7),
            new wiznet_type("newbies", WIZ_NEWBIE, IM),
            new wiznet_type("spam", WIZ_SPAM, L5),
            new wiznet_type("deaths", WIZ_DEATHS, IM),
            new wiznet_type("resets", WIZ_RESETS, L4),
            new wiznet_type("mobdeaths", WIZ_MOBDEATHS, L4),
            new wiznet_type("flags", WIZ_FLAGS, L5),
            new wiznet_type("penalties", WIZ_PENALTIES, L5),
            new wiznet_type("saccing", WIZ_SACCING, L5),
            new wiznet_type("levels", WIZ_LEVELS, IM),
            new wiznet_type("load", WIZ_LOAD, L2),
            new wiznet_type("restore", WIZ_RESTORE, L2),
            new wiznet_type("snoops", WIZ_SNOOPS, L2),
            new wiznet_type("switches", WIZ_SWITCHES, L2),
            new wiznet_type("secure", WIZ_SECURE, L1),
            new wiznet_type(null, 0, 0)
    };

    /**
     * attack table
     */
    static final attack_type[] attack_table = {
            new attack_type("none", "hit", -1),  /*  0 */
            new attack_type("slice", "slice", DAM_SLASH),
            new attack_type("stab", "stab", DAM_PIERCE),
            new attack_type("slash", "slash", DAM_SLASH),
            new attack_type("whip", "whip", DAM_SLASH),
            new attack_type("claw", "claw", DAM_SLASH),  /*  5 */
            new attack_type("blast", "blast", DAM_BASH),
            new attack_type("pound", "pound", DAM_BASH),
            new attack_type("crush", "crush", DAM_BASH),
            new attack_type("grep", "grep", DAM_SLASH),
            new attack_type("bite", "bite", DAM_PIERCE),  /* 10 */
            new attack_type("pierce", "pierce", DAM_PIERCE),
            new attack_type("suction", "suction", DAM_BASH),
            new attack_type("beating", "beating", DAM_BASH),
            new attack_type("digestion", "digestion", DAM_ACID),
            new attack_type("charge", "charge", DAM_BASH),  /* 15 */
            new attack_type("slap", "slap", DAM_BASH),
            new attack_type("punch", "punch", DAM_BASH),
            new attack_type("wrath", "wrath", DAM_ENERGY),
            new attack_type("magic", "magic", DAM_ENERGY),
            new attack_type("divine", "divine power", DAM_HOLY),  /* 20 */
            new attack_type("cleave", "cleave", DAM_SLASH),
            new attack_type("scratch", "scratch", DAM_PIERCE),
            new attack_type("peck", "peck", DAM_PIERCE),
            new attack_type("peckb", "peck", DAM_BASH),
            new attack_type("chop", "chop", DAM_SLASH),  /* 25 */
            new attack_type("sting", "sting", DAM_PIERCE),
            new attack_type("smash", "smash", DAM_BASH),
            new attack_type("shbite", "shocking bite", DAM_LIGHTNING),
            new attack_type("flbite", "flaming bite", DAM_FIRE),
            new attack_type("frbite", "freezing bite", DAM_COLD),  /* 30 */
            new attack_type("acbite", "acidic bite", DAM_ACID),
            new attack_type("chomp", "chomp", DAM_PIERCE),
            new attack_type("drain", "life drain", DAM_NEGATIVE),
            new attack_type("thrust", "thrust", DAM_PIERCE),
            new attack_type("slime", "slime", DAM_ACID),
            new attack_type("shock", "shock", DAM_LIGHTNING),
            new attack_type("thwack", "thwack", DAM_BASH),
            new attack_type("flame", "flame", DAM_FIRE),
            new attack_type("chill", "chill", DAM_COLD),
    };

    /**
     * God's Name, name of religion, tattoo vnum
     */
    static final religion_type[] religion_table =
            {
                    new religion_type("", "None", 0),
                    new religion_type("Atum-Ra", "Lawful Good", OBJ_VNUM_TATTOO_APOLLON),
                    new religion_type("Zeus", "Neutral Good", OBJ_VNUM_TATTOO_ZEUS),
                    new religion_type("Siebele", "true Neutral", OBJ_VNUM_TATTOO_SIEBELE),
                    new religion_type("Shamash", "God of Justice", OBJ_VNUM_TATTOO_HEPHAESTUS),
                    new religion_type("Ahuramazda", "Chaotic Good", OBJ_VNUM_TATTOO_EHRUMEN),
                    new religion_type("Ehrumen", "Chaotic Evil", OBJ_VNUM_TATTOO_AHRUMAZDA),
                    new religion_type("Deimos", "Lawful Evil", OBJ_VNUM_TATTOO_DEIMOS),
                    new religion_type("Phobos", "Neutral Evil", OBJ_VNUM_TATTOO_PHOBOS),
                    new religion_type("Odin", "Lawful Neutral", OBJ_VNUM_TATTOO_ODIN),
                    new religion_type("Teshub", "Chaotic Neutral", OBJ_VNUM_TATTOO_MARS),
                    new religion_type("Ares", "God of War", OBJ_VNUM_TATTOO_ATHENA),
                    new religion_type("Goktengri", "God of Honor", OBJ_VNUM_TATTOO_GOKTENGRI),
                    new religion_type("Hera", "God of Hate", OBJ_VNUM_TATTOO_HERA),
                    new religion_type("Venus", "God of beauty", OBJ_VNUM_TATTOO_VENUS),
                    new religion_type("Seth", "God of Anger", OBJ_VNUM_TATTOO_ARES),
                    new religion_type("Enki", "God of Knowledge", OBJ_VNUM_TATTOO_PROMETHEUS),
                    new religion_type("Eros", "God of Love", OBJ_VNUM_TATTOO_EROS)
            };


    /**
     * altar good neut evil, recall good neut evil, pit good neut evil
     */
    static final hometown_type[] hometown_table =
            {
                    new hometown_type("Midgaard", new int[]{3070, 3054, 3072}, new int[]{3068, 3001, 3071}, new int[]{3069, 3054, 3072}),
                    new hometown_type("New Thalos", new int[]{9605, 9605, 9605}, new int[]{9609, 9609, 9609}, new int[]{9609, 9609, 9609}),
                    new hometown_type("Titans", new int[]{18127, 18127, 18127}, new int[]{18126, 18126, 18126}, new int[]{18127, 18127, 18127}),
                    new hometown_type("New Ofcol", new int[]{669, 669, 669}, new int[]{698, 698, 698}, new int[]{669, 669, 669}),
                    new hometown_type("Old Midgaard", new int[]{5386, 5386, 5386}, new int[]{5379, 5379, 5379}, new int[]{5386, 5386, 5386})
            };

    /**
     * Attribute bonus tables.
     */
    static final str_app_type[] str_app = //[26]
            {
                    new str_app_type(-5, -4, 0, 0),  /* 0  */
                    new str_app_type(-5, -4, 3, 1),  /* 1  */
                    new str_app_type(-3, -2, 3, 2),
                    new str_app_type(-3, -1, 10, 3),  /* 3  */
                    new str_app_type(-2, -1, 25, 4),
                    new str_app_type(-2, -1, 55, 5),  /* 5  */
                    new str_app_type(-1, 0, 80, 6),
                    new str_app_type(-1, 0, 90, 7),
                    new str_app_type(0, 0, 100, 8),
                    new str_app_type(0, 0, 100, 9),
                    new str_app_type(0, 0, 115, 10), /* 10  */
                    new str_app_type(0, 0, 115, 11),
                    new str_app_type(0, 0, 130, 12),
                    new str_app_type(0, 0, 130, 13), /* 13  */
                    new str_app_type(0, 1, 140, 14),
                    new str_app_type(1, 1, 150, 15), /* 15  */
                    new str_app_type(1, 2, 165, 16),
                    new str_app_type(2, 3, 180, 22),
                    new str_app_type(2, 3, 200, 25), /* 18  */
                    new str_app_type(3, 4, 225, 30),
                    new str_app_type(3, 5, 250, 35), /* 20  */
                    new str_app_type(4, 6, 300, 40),
                    new str_app_type(4, 6, 350, 45),
                    new str_app_type(5, 7, 400, 50),
                    new str_app_type(5, 8, 450, 55),
                    new str_app_type(6, 9, 500, 60)  /* 25   */
            };


    static final int_app_type[] int_app =
            {
                    new int_app_type(3), /*  0 */
                    new int_app_type(5), /*  1 */
                    new int_app_type(7),
                    new int_app_type(8), /*  3 */
                    new int_app_type(9),
                    new int_app_type(10), /*  5 */
                    new int_app_type(11),
                    new int_app_type(12),
                    new int_app_type(13),
                    new int_app_type(15),
                    new int_app_type(17), /* 10 */
                    new int_app_type(19),
                    new int_app_type(22),
                    new int_app_type(25),
                    new int_app_type(28),
                    new int_app_type(31), /* 15 */
                    new int_app_type(34),
                    new int_app_type(37),
                    new int_app_type(40), /* 18 */
                    new int_app_type(44),
                    new int_app_type(49), /* 20 */
                    new int_app_type(55),
                    new int_app_type(60),
                    new int_app_type(70),
                    new int_app_type(80),
                    new int_app_type(85)  /* 25 */
            };


    static final wis_app_type[] wis_app =
            {
                    new wis_app_type(0),  /*  0*/
                    new wis_app_type(0),  /*  1*/
                    new wis_app_type(0),
                    new wis_app_type(0),  /*  3*/
                    new wis_app_type(0),
                    new wis_app_type(1),  /*  5*/
                    new wis_app_type(1),
                    new wis_app_type(1),
                    new wis_app_type(1),
                    new wis_app_type(1),
                    new wis_app_type(1),  /* 10*/
                    new wis_app_type(1),
                    new wis_app_type(1),
                    new wis_app_type(1),
                    new wis_app_type(1),
                    new wis_app_type(2),  /* 15*/
                    new wis_app_type(2),
                    new wis_app_type(2),
                    new wis_app_type(3),  /* 18*/
                    new wis_app_type(3),
                    new wis_app_type(3),  /* 20*/
                    new wis_app_type(3),
                    new wis_app_type(4),
                    new wis_app_type(4),
                    new wis_app_type(4),
                    new wis_app_type(5)   /* 25*/
            };


    static final dex_app_type[] dex_app =
            {
                    new dex_app_type(60),   /* 0*/
                    new dex_app_type(50),   /* 1*/
                    new dex_app_type(50),
                    new dex_app_type(40),
                    new dex_app_type(30),
                    new dex_app_type(20),   /* 5*/
                    new dex_app_type(10),
                    new dex_app_type(0),
                    new dex_app_type(0),
                    new dex_app_type(0),
                    new dex_app_type(0),   /* 10*/
                    new dex_app_type(0),
                    new dex_app_type(0),
                    new dex_app_type(0),
                    new dex_app_type(0),
                    new dex_app_type(-10),   /* 15*/
                    new dex_app_type(-15),
                    new dex_app_type(-20),
                    new dex_app_type(-30),
                    new dex_app_type(-40),
                    new dex_app_type(-50),   /* 20*/
                    new dex_app_type(-60),
                    new dex_app_type(-75),
                    new dex_app_type(-90),
                    new dex_app_type(-105),
                    new dex_app_type(-120)    /* 25*/
            };


    static final con_app_type[] con_app =
            {
                    new con_app_type(0, 20),   /*  0*/
                    new con_app_type(1, 25),   /*  1*/
                    new con_app_type(1, 30),
                    new con_app_type(2, 35),   /*  3*/
                    new con_app_type(3, 40),
                    new con_app_type(4, 45),   /*  5*/
                    new con_app_type(5, 50),
                    new con_app_type(6, 55),
                    new con_app_type(7, 60),
                    new con_app_type(8, 65),
                    new con_app_type(9, 70),   /* 10*/
                    new con_app_type(10, 75),
                    new con_app_type(11, 80),
                    new con_app_type(12, 85),
                    new con_app_type(13, 88),
                    new con_app_type(14, 90),   /* 15*/
                    new con_app_type(15, 95),
                    new con_app_type(16, 97),
                    new con_app_type(17, 99),   /* 18*/
                    new con_app_type(18, 99),
                    new con_app_type(19, 99),   /* 20*/
                    new con_app_type(20, 99),
                    new con_app_type(21, 99),
                    new con_app_type(22, 99),
                    new con_app_type(23, 99),
                    new con_app_type(24, 99)    /* 25*/
            };


    /**
     * Liquid properties.
     * Used in world.obj.
     */
    static final liq_type[] liq_table =
            {
/*    name          color   proof, full, thirst, food, size*/
                    new liq_type("water", "clear", new int[]{0, 1, 10, 0, 16}),
                    new liq_type("beer", "amber", new int[]{12, 1, 8, 1, 12}),
                    new liq_type("red wine", "burgundy", new int[]{30, 1, 8, 1, 5}),
                    new liq_type("ale", "brown", new int[]{15, 1, 8, 1, 12}),
                    new liq_type("dark ale", "dark", new int[]{16, 1, 8, 1, 12}),

                    new liq_type("whisky", "golden", new int[]{120, 1, 5, 0, 2}),
                    new liq_type("lemonade", "pink", new int[]{0, 1, 9, 2, 12}),
                    new liq_type("firebreather", "boiling", new int[]{190, 0, 4, 0, 2}),
                    new liq_type("local specialty", "clear", new int[]{151, 1, 3, 0, 2}),
                    new liq_type("slime mold juice", "green", new int[]{0, 2, -8, 1, 2}),

                    new liq_type("milk", "white", new int[]{0, 2, 9, 3, 12}),
                    new liq_type("tea", "tan", new int[]{0, 1, 8, 0, 6}),
                    new liq_type("coffee", "black", new int[]{0, 1, 8, 0, 6}),
                    new liq_type("blood", "red", new int[]{0, 2, -1, 2, 6}),
                    new liq_type("salt water", "clear", new int[]{0, 1, -2, 0, 1}),

                    new liq_type("coke", "brown", new int[]{0, 2, 9, 2, 12}),
                    new liq_type("root beer", "brown", new int[]{0, 2, 9, 2, 12}),
                    new liq_type("elvish wine", "green", new int[]{35, 2, 8, 1, 5}),
                    new liq_type("white wine", "golden", new int[]{28, 1, 8, 1, 5}),
                    new liq_type("champagne", "golden", new int[]{32, 1, 8, 1, 5}),

                    new liq_type("mead", "honey-colored", new int[]{34, 2, 8, 2, 12}),
                    new liq_type("rose wine", "pink", new int[]{26, 1, 8, 1, 5}),
                    new liq_type("benedictine wine", "burgundy", new int[]{40, 1, 8, 1, 5}),
                    new liq_type("vodka", "clear", new int[]{130, 1, 5, 0, 2}),
                    new liq_type("cranberry juice", "red", new int[]{0, 1, 9, 2, 12}),

                    new liq_type("orange juice", "orange", new int[]{0, 2, 9, 3, 12}),
                    new liq_type("absinthe", "green", new int[]{200, 1, 4, 0, 2}),
                    new liq_type("brandy", "golden", new int[]{80, 1, 5, 0, 4}),
                    new liq_type("aquavit", "clear", new int[]{140, 1, 5, 0, 2}),
                    new liq_type("schnapps", "clear", new int[]{90, 1, 5, 0, 2}),

                    new liq_type("icewine", "purple", new int[]{50, 2, 6, 1, 5}),
                    new liq_type("amontillado", "burgundy", new int[]{35, 2, 8, 1, 5}),
                    new liq_type("sherry", "red", new int[]{38, 2, 7, 1, 5}),
                    new liq_type("framboise", "red", new int[]{50, 1, 7, 1, 5}),
                    new liq_type("rum", "amber", new int[]{151, 1, 4, 0, 2}),

                    new liq_type("cordial", "clear", new int[]{100, 1, 5, 0, 2}),
                    new liq_type(null, null, new int[]{0, 0, 0, 0, 0})
            };

/*
 * The skill and spell table.
 * Slot numbers must never be changed as they appear in #OBJECTS sections.
*/


}
