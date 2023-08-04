package net.sf.nightworks;

import net.sf.nightworks.util.DikuTextFile;
import net.sf.nightworks.util.NotNull;
import net.sf.nightworks.util.Nullable;
import net.sf.nightworks.util.TextBuffer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static net.sf.nightworks.ActMove.rev_dir;
import static net.sf.nightworks.ActWiz.wiznet;
import static net.sf.nightworks.Ban.load_bans;
import static net.sf.nightworks.Comm.page_to_char;
import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.Handler.*;
import static net.sf.nightworks.Lookup.*;
import static net.sf.nightworks.Magic.slot_lookup_skill_num;
import static net.sf.nightworks.MobProg.mprog_set;
import static net.sf.nightworks.Nightworks.*;
import static net.sf.nightworks.Note.load_notes;
import static net.sf.nightworks.ObjProg.oprog_set;
import static net.sf.nightworks.Recycle.get_mob_id;
import static net.sf.nightworks.Recycle.new_char;
import static net.sf.nightworks.Save.wear_convert;
import static net.sf.nightworks.Skill.gsn_track;
import static net.sf.nightworks.Skill.skill_num_lookup;
import static net.sf.nightworks.Special.spec_lookup;
import static net.sf.nightworks.Tables.*;
import static net.sf.nightworks.util.Logger.log;
import static net.sf.nightworks.util.Logger.logError;
import static net.sf.nightworks.util.TextUtils.*;

@SuppressWarnings("unused")
class DB {
    /**
     * macro for flag swapping
     */
    static int GET_UNSET(int flag1, int flag2) {
        return (~(flag1) & ((flag1) | (flag2)));
    }

    /**
     * Magic number for memory allocation
     */
    static final int MAGIC_NUM = 52571214;

    /*
     * Globals.
     */
    /* TODO: move to Nightworks? */
    private static HELP_DATA help_last;
    private static SHOP_DATA shop_last;

    /*    private static NOTE_DATA    note_free;

        private static CHAR_DATA    char_list;
    */
    static String help_greeting;
/*
    private static AUCTION_DATA     auction;
    private static ROOM_INDEX_DATA  top_affected_room;
    private static int         reboot_counter;
    private static int         time_sync;
    private static int         max_newbies;
    private static int         max_oldies;
    private static int         iNumPlayers;

/*
 * for limited objects
 */
    /*    long                    total_levels; */

    /*
     * Locals.
     */
    static MOB_INDEX_DATA[] mob_index_hash = new MOB_INDEX_DATA[MAX_KEY_HASH];
    static OBJ_INDEX_DATA[] obj_index_hash = new OBJ_INDEX_DATA[MAX_KEY_HASH];
    static ROOM_INDEX_DATA[] room_index_hash = new ROOM_INDEX_DATA[MAX_KEY_HASH];

    private static AREA_DATA area_last;

    static final TIME_INFO_DATA time_info = new TIME_INFO_DATA();
    static final WEATHER_DATA weather_info = new WEATHER_DATA();
    static final KILL_DATA[] kill_table = new KILL_DATA[MAX_LEVEL];

    static {
        for (var i = 0; i < kill_table.length; i++) {
            kill_table[i] = new KILL_DATA();
        }
    }


    static int top_affect;
    private static int top_area;
    static int top_ed;
    private static int top_exit;
    private static int top_help;
    static int top_mob_index;
    static int top_obj_index;
    private static int top_reset;
    private static int top_room;
    private static int top_shop;
    static int mobile_count = 0;
    static int newmobs = 0;
    static int newobjs = 0;

    /*
     * Semi-locals.
     */
    static boolean fBootDb;
    private static DikuTextFile currentFile;
    private static AREA_DATA Serarea;   /* currently read area */

    /*
     * Local booting procedures.
     */

    /*
     * Big mama top level function.
     */
    private static DikuTextFile currentList = null;

    static void boot_db() {
        // Init some data space stuff.
        fBootDb = true;

        // Init random number generator.
        init_mm();

        // Set time and weather.
        setTimeAndWeather();

        /* auction */

        auction = new AUCTION_DATA();
        auction.item = null; /* nothing is being sold */

        /* room_affect_data */
        top_affected_room = null;

        /* reboot counter */
        reboot_counter = -1;   /* no default reboot */
        time_sync = 0;    /* time_sync is not set */

        max_newbies = MAX_NEWBIES;
        max_oldies = MAX_OLDIES;
        iNumPlayers = 0;
        try {
            read_classes();

            read_races();

            /* Read in all the area files.*/
            readAreas();
            /*
             * Fix up exits.
             * Declare db booting over.
             * Reset all areas once.
             * Load up the songs, notes and ban files.
             */
            fix_exits();

            load_limited_objects();

            fBootDb = false;
            area_update();
            load_notes();
            load_bans();
            load_socials();
        } catch (Exception e) {
            logError(e);
            if (currentList != null) {
                System.err.println("Current list file state:" + currentList.buildCurrentStateInfo());
            }
            if (currentFile != null) {
                System.err.println("Current file state:" + currentFile.buildCurrentStateInfo());
            }
            exit(1);
        }
    }

    private static void read_races() throws IOException {
        currentList = new DikuTextFile(nw_config.etc_races_list);
        for (; ; ) {
            var raceFile = currentList.read_word();
            if (raceFile.charAt(0) == '#') {
                continue;
            }
            if (raceFile.charAt(0) == '$') {
                break;
            }
            currentFile = new DikuTextFile(nw_config.lib_races_dir + "/" + raceFile);
            Race race = null;
            label:
            while (!currentFile.is_eof()) {
                var word = currentFile.read_word();
                switch (word) {
                    case "#RACE":
                        if (race != null) {
                            throw new RuntimeException("2 #RACE tokens in race file!");
                        }
                        race = read_race(currentFile);
                        break;
                    case "#PCRACE":
                        if (race == null) {
                            throw new RuntimeException("Error: #PCRACE before #RACE");
                        }
                        race.pcRace = read_pcrace(currentFile);
                        break;
                    case "#$":
                        break label;
                    default:
                        throw new RuntimeException("Unknown token in race file:" + word);
                }
            }
        }
        Race.doValidationCheck();
    }

    private static Race read_race(DikuTextFile fp) throws RuntimeException {
        Race race;
        var word = fp.read_word();
        fp.fMatch = false;
        if (!word.equals("Name")) {
            throw new RuntimeException("read_race: first token is not 'Name'");
        }
        var name = fp.read_string();
        race = Race.createRace(name);
        race.fileName = fp.getFile().getName();
        fp.fMatch = true;
        for (; ; ) {
            word = fp.is_eof() ? "End" : fp.read_word();
            fp.fMatch = false;
            switch (UPPER(word.charAt(0))) {
                case 'A' -> {
                    race.aff = fp.FLAG64_SKEY("Aff", word, race.aff, affect_flags);
                    race.act = fp.FLAG64_SKEY("Act", word, race.act, act_flags);
                }
                case 'E' -> {
                    if (!str_cmp(word, "End")) {
                        return race;
                    }
                }
                case 'F' -> {
                    race.form = fp.FLAG32_SKEY("Form", word, race.form, form_flags);
                    fp.FLAG32_SKEY("Flags", word, race.res, res_flags); //todo: SoG race
                }
                case 'I' -> race.imm = fp.FLAG32_SKEY("Imm", word, race.imm, imm_flags);
                case 'O' -> race.off = fp.FLAG32_SKEY("Off", word, race.off, off_flags);
                case 'P' -> race.parts = fp.FLAG32_SKEY("Parts", word, race.parts, part_flags);
                case 'R' -> {
                    race.res = fp.FLAG32_SKEY("Res", word, race.res, res_flags);
                    if (word.equals("Resist")) {
                        fp.read_string_eol();
                        fp.fMatch = true;
                    }
                }
                case 'V' -> race.vuln = fp.FLAG32_SKEY("Vuln", word, race.vuln, vuln_flags);
            }
            if (!fp.fMatch) {
                throw new RuntimeException("unknown keyword:" + word);
            }
        }
    }

    private static PCRace read_pcrace(DikuTextFile fp) {
        var pcRace = new PCRace();
        for (; ; ) {
            int i;
            var word = fp.is_eof() ? "End" : fp.read_word();
            fp.fMatch = false;
            switch (UPPER(word.charAt(0))) {
                case 'A':
                    pcRace.align = fp.FLAG32_SKEY("Align", word, pcRace.align, align_flags);
                    break;

                case 'B':
                    if (word.equals("BonusSkills")) {
                        var skills = fp.read_string();
                        List<Skill> skillsList = new ArrayList<>();
                        while (!skills.isEmpty()) {
                            var oneSkill = new StringBuilder();
                            skills = one_argument(skills, oneSkill);
                            var skill = Skill.lookupSkill(oneSkill.toString());
                            if (skill == null) {
                                throw new RuntimeException("not a skill:" + oneSkill);
                            }
                            skillsList.add(skill);
                        }
                        pcRace.skills = new Skill[skillsList.size()];
                        skillsList.toArray(pcRace.skills);
                        fp.fMatch = true;
                    }
                    break;
                case 'C':
                    if (!str_cmp(word, "Class")) {
                        var name = fp.read_word();
                        var expMult = fp.read_number();
                        var clazz = Clazz.lookupClass(name);
                        var mod = new RaceToClassModifier(clazz, expMult);
                        pcRace.addClassModifier(clazz, mod);
                        fp.fMatch = true;
                    }
                    break;
                case 'E':
                    if (!str_cmp(word, "End")) {
                        if (pcRace.who_name == null) {
                            throw new RuntimeException("pc_race who_name undefined");
                        }
                        //TODO: sort classes and skills -> better representation
                        return pcRace;
                    }
                    break;
                case 'H':
                    pcRace.hp_bonus = fp.NKEY("HPBonus", word, pcRace.hp_bonus);
                    break;

                case 'M':
                    pcRace.mana_bonus = fp.NKEY("ManaBonus", word, pcRace.mana_bonus);
                    if (!fp.fMatch && !str_cmp(word, "MaxStats")) {
                        for (i = 0; i < MAX_STATS; i++) {
                            pcRace.max_stats[i] = fp.read_number();
                        }
                        fp.fMatch = true;
                    }

                case 'P':
                    pcRace.points = fp.NKEY("Points", word, pcRace.points);
                    pcRace.prac_bonus = fp.NKEY("PracBonus", word, pcRace.prac_bonus);
                    break;

                case 'R': //TODO: SOG race file
                    fp.WKEY("RestrictAlign", word, "");
                    fp.WKEY("RestrictEthos", word, "");
                    break;

                case 'S':
                    pcRace.size = fp.FLAG32_WKEY("Size", word, pcRace.size, size_table);
                    pcRace.language = fp.FLAG32_WKEY("Slang", word, pcRace.language, slang_table);
                    pcRace.who_name = fp.SKEY("ShortName", word, pcRace.who_name);

                    if (word.equals("Skill")) {
                        fp.read_string_eol();
                        fp.fMatch = true;
                    }

                    if (!fp.fMatch && !str_cmp(word, "Stats")) {
                        for (i = 0; i < MAX_STATS; i++) {
                            pcRace.stats[i] = fp.read_number();
                        }
                        fp.fMatch = true;
                    }
                    break;
            }
            if (!fp.fMatch) {
                throw new RuntimeException("unknown word:" + word);
            }
        }
    }

    private static void read_classes() throws IOException {
        currentList = new DikuTextFile(nw_config.etc_classes_list);
        for (; ; ) {
            var classFile = currentList.read_word();
            if (classFile.charAt(0) == '$') {
                break;
            }
            currentFile = new DikuTextFile(nw_config.lib_classes_dir + "/" + classFile);
            Clazz clazz = null;
            label:
            while (!currentFile.is_eof()) {
                var word = currentFile.read_word();
                switch (word) {
                    case "#CLASS":
                        if (clazz != null) {
                            throw new RuntimeException("2 #CLASS tokens in clazz file!");
                        }
                        clazz = read_class(currentFile);
                        break;
                    case "#POSE":
                        if (clazz == null) {
                            throw new RuntimeException("Error: #POSE before #CLASS");
                        }
                        var pose = read_class_pose(currentFile);
                        clazz.poses.add(pose);
                        break;
                    case "#$":
                        break label;
                    default:
                        throw new RuntimeException("Unknown token in clazz file:" + word);
                }
            }
        }
        Clazz.doValidationCheck();
    }

    private static Clazz read_class(DikuTextFile fp) {
        Clazz clazz = null;
        for (; ; ) {
            int i;
            var word = fp.is_eof() ? "End" : fp.read_word();
            fp.fMatch = false;

            switch (UPPER(word.charAt(0))) {
                case 'A' -> {
                    clazz.points = fp.NKEY("AddExp", word, clazz.points);
                    clazz.align = fp.FLAG32_SKEY("Align", word, clazz.align, align_flags);
                }
                case 'E' -> {
                    if (!str_cmp(word, "End")) {
                        if (clazz == null) {
                            throw new RuntimeException("Class is null at 'End'");
                        }
                        //todo: sort skills
                        return clazz;
                    }
                    clazz.ethos = fp.FLAG32_WKEY("Ethos", word, clazz.ethos, ethos_table);
                }
                case 'G' -> {
                    if (!str_cmp(word, "GuildRoom")) {
                        var vnum = fp.read_number();
                        clazz.guildVnums.add(vnum);
                        fp.fMatch = true;
                    }
                }
                case 'H' -> clazz.hp_rate = fp.NKEY("HPRate", word, clazz.hp_rate);
                case 'M' -> clazz.mana_rate = fp.NKEY("ManaRate", word, clazz.mana_rate);
                case 'N' -> {
                    if (!str_cmp(word, "Name")) {
                        fp.fMatch = true;
                        var name = fp.read_string();
                        clazz = Clazz.lookupClass(name, false);
                        if (clazz == null) {
                            clazz = new Clazz(name);
                        }
                    }
                }
                case 'P' -> clazz.attr_prime = fp.FLAG32_WKEY("PrimeStat", word, clazz.attr_prime, stat_names);
                case 'S' -> {
                    if (!str_cmp(word, "Skill")) {
                        var skill = Skill.lookupSkill(fp.read_word(), true);
                        var level = fp.read_number();
                        var rating = fp.read_number();
                        var mod = fp.read_number();
                        skill.skill_level[clazz.id] = level;
                        skill.rating[clazz.id] = rating;
                        skill.mod[clazz.id] = mod;
                        fp.fMatch = true;
                        break;
                    }
                    clazz.sex = fp.FLAG32_WKEY("Sex", word, clazz.sex, sex_table);
                    clazz.skill_adept = fp.NKEY("SkillAdept", word, clazz.skill_adept);
                    clazz.weapon = fp.NKEY("SchoolWeapon", word, clazz.weapon);
                    clazz.who_name = fp.SKEY("ShortName", word, clazz.who_name);
                    if (!str_cmp(word, "StatMod")) {
                        for (i = 0; i < MAX_STATS; i++) {
                            clazz.stats[i] = fp.read_number();
                        }
                        fp.fMatch = true;
                    }
                }
                case 'T' -> {
                    clazz.thac0_00 = fp.NKEY("Thac0_00", word, clazz.thac0_00);
                    clazz.thac0_32 = fp.NKEY("Thac0_32", word, clazz.thac0_32);
                    if (!str_cmp(word, "Title")) {
                        int level;
                        int sex;

                        level = fp.read_number();
                        if (level < 0 || level > MAX_LEVEL) {
                            throw new RuntimeException("load_class: invalid level: " + level);
                        }
                        var sexStr = fp.read_word();
                        sex = (int) flag_type.parseFlagsValue(sexStr, sex_table);
                        if (sex != SEX_MALE && sex != SEX_FEMALE) {
                            throw new RuntimeException("load_class: invalid sex");
                        }
                        var title = fp.read_string();
                        if (sex == SEX_MALE) {
                            clazz.maleTitles[level] = title;
                        } else {
                            clazz.femaleTitles[level] = title;
                        }
                        fp.fMatch = true;
                    }
                }
            }

            if (!fp.fMatch) {
                throw new RuntimeException("load_class: Unknown keyword:" + word);
            }
        }
    }

    private static Pose read_class_pose(DikuTextFile fp) {
        var pose = new Pose();
        for (; ; ) {
            int i;
            var word = fp.is_eof() ? "End" : fp.read_word();
            fp.fMatch = false;
            switch (UPPER(word.charAt(0))) {
                case 'E' -> {
                    if (!str_cmp(word, "End")) {
                        return pose;
                    }
                }
                case 'O' -> pose.message_to_room = fp.SKEY("Others", word, pose.message_to_room);
                case 'S' -> pose.message_to_char = fp.SKEY("Self", word, pose.message_to_char);
            }
            if (!fp.fMatch) {
                throw new RuntimeException("load_class: Unknown keyword:" + word);
            }
        }
    }

    private static void setTimeAndWeather() {
        int lhour, lday, lmonth;

        lhour = (int) (current_time - 650336715L) / (PULSE_TICK / PULSE_PER_SCD);
        time_info.bmin = 0;
        time_info.hour = lhour % 24;
        lday = lhour / 24;
        time_info.day = lday % 35;
        lmonth = lday / 35;
        time_info.month = lmonth % 17;
        time_info.year = lmonth / 17;

        if (time_info.hour < 5) {
            weather_info.sunlight = SUN_DARK;
        } else if (time_info.hour < 6) {
            weather_info.sunlight = SUN_RISE;
        } else if (time_info.hour < 19) {
            weather_info.sunlight = SUN_LIGHT;
        } else if (time_info.hour < 20) {
            weather_info.sunlight = SUN_SET;
        } else {
            weather_info.sunlight = SUN_DARK;
        }

        weather_info.change = 0;
        weather_info.mmhg = 960;
        if (time_info.month >= 7 && time_info.month <= 12) {
            weather_info.mmhg += number_range(1, 50);
        } else {
            weather_info.mmhg += number_range(1, 80);
        }

        if (weather_info.mmhg <= 980) {
            weather_info.sky = SKY_LIGHTNING;
        } else if (weather_info.mmhg <= 1000) {
            weather_info.sky = SKY_RAINING;
        } else if (weather_info.mmhg <= 1020) {
            weather_info.sky = SKY_CLOUDY;
        } else {
            weather_info.sky = SKY_CLOUDLESS;
        }
    }

    private static void readAreas() throws IOException, NoSuchMethodException {
        currentList = new DikuTextFile(nw_config.etc_area_list);
        for (; ; ) {
            var strArea = currentList.read_word();
            if (strArea.charAt(0) == '$') {
                break;
            }

            if (strArea.charAt(0) == '-') {
                bug("unsupported mode '-'");
                exit(1);
            }

            currentFile = new DikuTextFile(nw_config.lib_area_dir + "/" + strArea);

            for (; ; ) {
                if (currentFile.read_letter() != '#') {
                    bug("Boot_db: # not found.");
                    exit(1);
                }

                var word = currentFile.read_word();

                if (word.charAt(0) == '$') {
                    break;
                } else if (!str_cmp(word, "AREA")) {
                    load_area(currentFile);
                } else if (!str_cmp(word, "HELPS")) {
                    load_helps(currentFile);
                } else if (!str_cmp(word, "MOBOLD")) {
                    load_old_mob(currentFile);
                } else if (!str_cmp(word, "MOBILES")) {
                    load_mobiles(currentFile);
                } else if (!str_cmp(word, "OBJOLD")) {
                    load_old_obj(currentFile);
                } else if (!str_cmp(word, "OBJECTS")) {
                    load_objects(currentFile);
                } else if (!str_cmp(word, "RESETS")) {
                    load_resets(currentFile);
                } else if (!str_cmp(word, "ROOMS")) {
                    load_rooms(currentFile);
                } else if (!str_cmp(word, "SHOPS")) {
                    load_shops(currentFile);
                } else if (!str_cmp(word, "OMPROGS")) {
                    load_omprogs(currentFile);
                } else if (!str_cmp(word, "OLIMITS")) {
                    load_olimits(currentFile);
                } else if (!str_cmp(word, "SPECIALS")) {
                    load_specials(currentFile);
                } else if (!str_cmp(word, "PRACTICERS")) {
                    load_practicer(currentFile);
                } else if (!str_cmp(word, "RESETMESSAGE")) {
                    load_resetmsg(currentFile);
                } else if (!str_cmp(word, "FLAG")) {
                    load_aflag(currentFile);
                } else if (!str_cmp(word, "SOCIALS")) {
                    perror("WARN: social definition in area file not supported more:" + currentFile.buildCurrentStateInfo());
                } else {
                    bug("Boot_db: bad section name.");
                    exit(1);
                }
            }
        }
        Serarea = null;
        currentList = null;
    }

    /*
     * Snarf an 'area' header line.
     */

    static void load_area(DikuTextFile fp) {
        var pArea = new AREA_DATA();
        pArea.reset_first = null;
        pArea.reset_last = null;
        pArea.file_name = fp.read_string();
        pArea.name = fp.read_string();
        fp.read_letter();
        pArea.low_range = fp.read_number();
        pArea.high_range = fp.read_number();
        fp.read_letter();
        pArea.writer = fp.read_word();
        pArea.credits = fp.read_string();
        pArea.min_vnum = fp.read_number();
        pArea.max_vnum = fp.read_number();
        pArea.age = 15;
        pArea.nplayer = 0;
        pArea.empty = false;
        pArea.count = 0;
        pArea.resetmsg = null;
        pArea.area_flag = 0;

        if (area_first == null) {
            area_first = pArea;
        }
        if (area_last != null) {
            area_last.next = pArea;
        }
        area_last = pArea;
        pArea.next = null;
        Serarea = pArea;

        top_area++;
    }

    /*
     * Snarf a help section.
     */

    static void load_helps(DikuTextFile fp) {
        HELP_DATA pHelp;
        for (; ; ) {
            pHelp = new HELP_DATA();
            pHelp.level = fp.read_number();
            pHelp.keyword = fp.read_string();
            if (pHelp.keyword.charAt(0) == '$') {
                break;
            }
            pHelp.text = fp.read_string();
            if (!pHelp.text.isEmpty() && pHelp.text.charAt(0) == '.') {
                pHelp.text = pHelp.text.substring(1);
            }
            if (!str_cmp(pHelp.keyword, "greeting")) {
                help_greeting = pHelp.text;
            }
            if (help_first == null) {
                help_first = pHelp;
            }
            if (help_last != null) {
                help_last.next = pHelp;
            }
            help_last = pHelp;
            pHelp.next = null;
            top_help++;
        }
    }

    /*
     * Snarf a mob section.  old style
     */

    static void load_old_mob(DikuTextFile fp) {
        MOB_INDEX_DATA pMobIndex;
        /* for race updating */
        for (; ; ) {
            int vnum;
            char letter;
            int iHash;

            letter = fp.read_letter();
            if (letter != '#') {
                bug("Load_mobiles: # not found.");
                exit(1);
            }

            vnum = fp.read_number();
            if (vnum == 0) {
                break;
            }

            fBootDb = false;
            if (get_mob_index(vnum) != null) {
                bug("Load_mobiles: vnum %d duplicated.", vnum);
                exit(1);
            }
            fBootDb = true;

            pMobIndex = new MOB_INDEX_DATA();
            pMobIndex.vnum = vnum;
            pMobIndex.new_format = false;
            pMobIndex.player_name = fp.read_string();
            pMobIndex.short_descr = fp.read_string();
            pMobIndex.long_descr = capitalize(fp.read_string());
            pMobIndex.description = capitalize(fp.read_string());


            pMobIndex.act = fp.read_flag() | ACT_IS_NPC;
            pMobIndex.affected_by = fp.read_flag();
            pMobIndex.practicer = 0;

            pMobIndex.affected_by = REMOVE_BIT(pMobIndex.affected_by, (C | D | E | F | G | Z | BIT_30));

            pMobIndex.pShop = null;
            pMobIndex.alignment = fp.read_number();
            letter = fp.read_letter();
            pMobIndex.level = fp.read_number();
            pMobIndex.mprogs = null;
            /*
             * The unused stuff is for imps who want to use the old-style
             * stats-in-files method.
             */
            fp.read_number();   /* Unused */
            fp.read_number();   /* Unused */
            fp.read_number();   /* Unused */
            /* 'd'      */
            fp.read_letter();   /* Unused */
            fp.read_number();   /* Unused */
            /* '+'      */
            fp.read_letter();   /* Unused */
            fp.read_number();   /* Unused */
            fp.read_number();   /* Unused */
            /* 'd'      */
            fp.read_letter();   /* Unused */
            fp.read_number();   /* Unused */
            /* '+'      */
            fp.read_letter();   /* Unused */
            fp.read_number();   /* Unused */
            pMobIndex.wealth = fp.read_number() / 20;
            /* xp can't be used! */
            fp.read_number();   /* Unused */
            pMobIndex.start_pos = fp.read_number();   /* Unused */
            pMobIndex.default_pos = fp.read_number();   /* Unused */

            if (pMobIndex.start_pos < POS_SLEEPING) {
                pMobIndex.start_pos = POS_STANDING;
            }
            if (pMobIndex.default_pos < POS_SLEEPING) {
                pMobIndex.default_pos = POS_STANDING;
            }

            /*
             * Back to meaningful values.
             */
            pMobIndex.sex = fp.read_number();

            /* compute the race BS */
            var name = new StringBuilder();
            one_argument(pMobIndex.player_name, name);
            var race = Race.lookupRace(name.toString());

//FIXME            if (race == null) {
//                /* fill in with blanks */
//                pMobIndex.race = Race.lookupRace("human");
//                pMobIndex.affected_by = pMobIndex.affected_by | pMobIndex.race.aff;
//                pMobIndex.off_flags = OFF_DODGE | OFF_DISARM | OFF_TRIP | ASSIST_VNUM;
//                pMobIndex.imm_flags = 0;
//                pMobIndex.res_flags = 0;
//                pMobIndex.vuln_flags = 0;
//                pMobIndex.form = FORM_EDIBLE | FORM_SENTIENT | FORM_BIPED | FORM_MAMMAL;
//                pMobIndex.parts = PART_HEAD | PART_ARMS | PART_LEGS | PART_HEART |
//                        PART_BRAINS | PART_GUTS;
//            } else {
            pMobIndex.race = race;
            pMobIndex.affected_by = pMobIndex.affected_by | race.aff;
            pMobIndex.off_flags = OFF_DODGE | OFF_DISARM | OFF_TRIP | ASSIST_RACE | race.off;
            pMobIndex.imm_flags = race.imm;
            pMobIndex.res_flags = race.res;
            pMobIndex.vuln_flags = race.vuln;
            pMobIndex.form = race.form;
            pMobIndex.parts = race.parts;
//            }

            if (letter != 'S') {
                bug("Load_mobiles: vnum %d non-S.", vnum);
                exit(1);
            }

            iHash = vnum % MAX_KEY_HASH;
            pMobIndex.next = mob_index_hash[iHash];
            mob_index_hash[iHash] = pMobIndex;
            top_mob_index++;
            kill_table[URANGE(0, pMobIndex.level, MAX_LEVEL - 1)].number++;
        }
    }

    /** Converts the first letter to uppercase. */
    static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
    /*
     * Snarf an obj section.  old style
     */

    static void load_old_obj(DikuTextFile fp) {
        OBJ_INDEX_DATA pObjIndex;

        for (; ; ) {
            int vnum;
            int letter;
            int iHash;

            letter = fp.read_letter();
            if (letter != '#') {
                bug("Load_objects: # not found.");
                exit(1);
            }

            vnum = fp.read_number();
            if (vnum == 0) {
                break;
            }

            fBootDb = false;
            if (get_obj_index(vnum) != null) {
                bug("Load_objects: vnum %d duplicated.", vnum);
                exit(1);
            }
            fBootDb = true;

            pObjIndex = new OBJ_INDEX_DATA();
            pObjIndex.vnum = vnum;
            pObjIndex.new_format = false;
            pObjIndex.reset_num = 0;
            pObjIndex.name = fp.read_string();
            pObjIndex.short_descr = capitalize(fp.read_string());
            pObjIndex.description = capitalize(fp.read_string());
            /* Action description */
            fp.read_string();

            pObjIndex.material = "";

            pObjIndex.item_type = fp.read_number();
            pObjIndex.extra_flags = fp.read_flag();
            pObjIndex.wear_flags = fp.read_flag();
            pObjIndex.value[0] = fp.read_number();
            pObjIndex.value[1] = fp.read_number();
            pObjIndex.value[2] = fp.read_number();
            pObjIndex.value[3] = fp.read_number();
            pObjIndex.value[4] = 0;
            pObjIndex.level = 0;
            pObjIndex.condition = 100;
            pObjIndex.weight = fp.read_number();
            pObjIndex.cost = fp.read_number();   /* Unused */
            /* Cost per day */
            fp.read_number();
            pObjIndex.limit = -1;
            pObjIndex.oprogs = null;

            if (pObjIndex.item_type == ITEM_WEAPON) {
                if (is_name("two", pObjIndex.name)
                        || is_name("two-handed", pObjIndex.name)
                        || is_name("claymore", pObjIndex.name)) {
                    pObjIndex.value[4] = SET_BIT(pObjIndex.value[4], WEAPON_TWO_HANDS);
                }
            }

            for (; ; ) {
                letter = fp.read_letter();
                if (letter == 'A') {
                    AFFECT_DATA paf;

                    paf = new AFFECT_DATA();
                    paf.where = TO_OBJECT;
                    paf.type = null;
                    paf.level = 20; /* RT temp fix */
                    paf.duration = -1;
                    paf.location = fp.read_number();
                    paf.modifier = fp.read_number();
                    paf.bitvector = 0;
                    paf.next = pObjIndex.affected;
                    pObjIndex.affected = paf;
                    top_affect++;
                } else if (letter == 'E') {
                    EXTRA_DESCR_DATA ed;

                    ed = new EXTRA_DESCR_DATA();
                    ed.keyword = fp.read_string();
                    ed.description = fp.read_string();
                    ed.next = pObjIndex.extra_descr;
                    pObjIndex.extra_descr = ed;
                    top_ed++;
                } else {
                    fp.ungetc();
                    break;
                }
            }

            /* fix armors */
            if (pObjIndex.item_type == ITEM_ARMOR) {
                pObjIndex.value[1] = pObjIndex.value[0];
                pObjIndex.value[2] = pObjIndex.value[1];
            }

            /*
             * Translate spell "slot numbers" to internal "skill numbers."
             */
            switch (pObjIndex.item_type) {
                case ITEM_PILL, ITEM_POTION, ITEM_SCROLL -> {
                    pObjIndex.value[1] = slot_lookup_skill_num(pObjIndex.value[1]);
                    pObjIndex.value[2] = slot_lookup_skill_num(pObjIndex.value[2]);
                    pObjIndex.value[3] = slot_lookup_skill_num(pObjIndex.value[3]);
                    pObjIndex.value[4] = slot_lookup_skill_num(pObjIndex.value[4]);
                }
                case ITEM_STAFF, ITEM_WAND -> pObjIndex.value[3] = slot_lookup_skill_num(pObjIndex.value[3]);
            }

            iHash = vnum % MAX_KEY_HASH;
            pObjIndex.next = obj_index_hash[iHash];
            obj_index_hash[iHash] = pObjIndex;
            top_obj_index++;
        }

    }

    /*
     * Snarf a reset section.
     */

    static void load_resets(DikuTextFile fp) {
        RESET_DATA pReset;

        if (area_last == null) {
            bug("Load_resets: no #AREA seen yet.");
            exit(1);
        }

        for (; ; ) {
            ROOM_INDEX_DATA pRoomIndex;
            EXIT_DATA pexit;
            int letter;
            OBJ_INDEX_DATA temp_index;

            if ((letter = fp.read_letter()) == 'S') {
                break;
            }

            if (letter == '*') {
                fp.read_to_eol();
                continue;
            }

            pReset = new RESET_DATA();
            pReset.command = (char) letter;
            /* if_flag */
            fp.read_number();
            pReset.arg1 = fp.read_number();
            pReset.arg2 = fp.read_number();
            pReset.arg3 = ((letter == 'G' || letter == 'R')
                    ? 0 : fp.read_number());
            pReset.arg4 = ((letter == 'P' || letter == 'M')
                    ? fp.read_number() : 0);
            fp.read_to_eol();

            /*
             * Validate parameters.
             * We're calling the index functions for the side effect.
             */
            switch (letter) {
                default -> {
                    bug("Load_resets: bad command '%c'.", letter);
                    exit(1);
                }
                case 'M' -> {
                    get_mob_index(pReset.arg1);
                    get_room_index(pReset.arg3);
                }
                case 'O' -> {
                    temp_index = get_obj_index(pReset.arg1);
                    temp_index.reset_num++;
                    get_room_index(pReset.arg3);
                }
                case 'P' -> {
                    temp_index = get_obj_index(pReset.arg1);
                    temp_index.reset_num++;
                    get_obj_index(pReset.arg3);
                }
                case 'G', 'E' -> {
                    temp_index = get_obj_index(pReset.arg1);
                    temp_index.reset_num++;
                }
                case 'D' -> {
                    pRoomIndex = get_room_index(pReset.arg1);
                    if (pReset.arg2 < 0
                            || pReset.arg2 > 5
                            || (pexit = pRoomIndex.exit[pReset.arg2]) == null
                            || !IS_SET(pexit.exit_info, EX_ISDOOR)) {
                        bug("Load_resets: 'D': exit %d not door.", pReset.arg2);
                        exit(1);
                    }
                    if (pReset.arg3 < 0 || pReset.arg3 > 2) {
                        bug("Load_resets: 'D': bad 'locks': %d.", pReset.arg3);
                        exit(1);
                    }
                }
                case 'R' -> {
                    get_room_index(pReset.arg1);
                    if (pReset.arg2 < 0 || pReset.arg2 > 6) {
                        bug("Load_resets: 'R': bad exit %d.", pReset.arg2);
                        exit(1);
                    }
                }
            }

            if (area_last.reset_first == null) {
                area_last.reset_first = pReset;
            }
            if (area_last.reset_last != null) {
                area_last.reset_last.next = pReset;
            }

            area_last.reset_last = pReset;
            pReset.next = null;
            top_reset++;
        }
    }

    /*
     * Snarf a room section.
     */

    static void load_rooms(DikuTextFile fp) {
        ROOM_INDEX_DATA pRoomIndex;

        if (area_last == null) {
            bug("Load_resets: no #AREA seen yet.");
            exit(1);
        }

        for (; ; ) {
            int vnum;
            int letter;
            int door;
            int iHash;

            letter = fp.read_letter();
            if (letter != '#') {
                bug("Load_rooms: # not found.");
                exit(1);
            }

            vnum = fp.read_number();
            if (vnum == 0) {
                break;
            }

            fBootDb = false;
            if (get_room_index(vnum) != null) {
                bug("Load_rooms: vnum %d duplicated.", vnum);
                exit(1);
            }
            fBootDb = true;

            pRoomIndex = new ROOM_INDEX_DATA();
            pRoomIndex.owner = null;
            pRoomIndex.people = null;
            pRoomIndex.contents = null;
            pRoomIndex.extra_descr = null;
            pRoomIndex.history = null;
            pRoomIndex.area = area_last;
            pRoomIndex.vnum = vnum;
            pRoomIndex.name = fp.read_string();
            pRoomIndex.description = fp.read_string();
            /* Area number */
            fp.read_number();
            pRoomIndex.room_flags = fp.read_flag();

            if (3000 <= vnum && vnum < 3400) {
                pRoomIndex.room_flags = SET_BIT(pRoomIndex.room_flags, ROOM_LAW);
            }

            pRoomIndex.sector_type = fp.read_number();
            if (pRoomIndex.sector_type < 0) {
                perror("Invalid room sector_type=" + pRoomIndex.sector_type + " room vnum:" + pRoomIndex.vnum);
                pRoomIndex.sector_type = 0;
            }

            pRoomIndex.light = 0;
            for (door = 0; door <= 5; door++) {
                pRoomIndex.exit[door] = null;
            }

            /* defaults */
            pRoomIndex.heal_rate = 100;
            pRoomIndex.mana_rate = 100;
            pRoomIndex.affected = null;
            pRoomIndex.affected_by = 0;
            pRoomIndex.aff_next = null;

            for (; ; ) {
                letter = fp.read_letter();

                if (letter == 'S') {
                    break;
                }

                if (letter == 'H') /* healing room */ {
                    pRoomIndex.heal_rate = fp.read_number();
                } else if (letter == 'M') /* mana room */ {
                    pRoomIndex.mana_rate = fp.read_number();
                } else if (letter == 'D') {
                    EXIT_DATA pexit;
                    int locks;

                    door = fp.read_number();
                    if (door < 0 || door > 5) {
                        bug("Fread_rooms: vnum %d has bad door number.", vnum);
                        exit(1);
                    }

                    pexit = new EXIT_DATA();
                    pexit.description = fp.read_string();
                    pexit.keyword = fp.read_string();
                    pexit.exit_info = 0;
                    locks = fp.read_number();
                    pexit.key = fp.read_number();
                    pexit.vnum = fp.read_number();

                    switch (locks) {
                        case 1 -> pexit.exit_info = EX_ISDOOR;
                        case 2 -> pexit.exit_info = EX_ISDOOR | EX_PICKPROOF;
                        case 3 -> pexit.exit_info = EX_ISDOOR | EX_NOPASS;
                        case 4 -> pexit.exit_info = EX_ISDOOR | EX_NOPASS | EX_PICKPROOF;
                        case 5 -> pexit.exit_info = EX_NOFLEE;
                    }

                    pRoomIndex.exit[door] = pexit;
                    pRoomIndex.old_exit[door] = pexit;
                    top_exit++;
                } else if (letter == 'E') {
                    EXTRA_DESCR_DATA ed;

                    ed = new EXTRA_DESCR_DATA();
                    ed.keyword = fp.read_string();
                    ed.description = fp.read_string();
                    ed.next = pRoomIndex.extra_descr;
                    pRoomIndex.extra_descr = ed;
                    top_ed++;
                } else if (letter == 'O') {
                    if (pRoomIndex.owner != null) {
                        bug("Load_rooms: duplicate owner.");
                        exit(1);
                    }

                    pRoomIndex.owner = fp.read_string();
                } else {
                    bug("Load_rooms: vnum %d has flag not 'DES'.", vnum);
                    exit(1);
                }
            }

            iHash = vnum % MAX_KEY_HASH;
            pRoomIndex.next = room_index_hash[iHash];
            room_index_hash[iHash] = pRoomIndex;
            top_room++;
        }
    }

    /*
     * Snarf a shop section.
     */

    static void load_shops(DikuTextFile fp) {
        SHOP_DATA pShop;

        for (; ; ) {
            MOB_INDEX_DATA pMobIndex;
            int iTrade;

            pShop = new SHOP_DATA();
            pShop.keeper = fp.read_number();
            if (pShop.keeper == 0) {
                break;
            }
            for (iTrade = 0; iTrade < MAX_TRADE; iTrade++) {
                pShop.buy_type[iTrade] = fp.read_number();
            }
            pShop.profit_buy = fp.read_number();
            pShop.profit_sell = fp.read_number();
            pShop.open_hour = fp.read_number();
            pShop.close_hour = fp.read_number();
            fp.read_to_eol();
            pMobIndex = get_mob_index(pShop.keeper);
            pMobIndex.pShop = pShop;

            if (shop_first == null) {
                shop_first = pShop;
            }
            if (shop_last != null) {
                shop_last.next = pShop;
            }

            shop_last = pShop;
            pShop.next = null;
            top_shop++;
        }
    }

    /*
     * Snarf spec proc declarations.
     */

    static void load_specials(DikuTextFile fp) {
        for (; ; ) {
            MOB_INDEX_DATA pMobIndex;
            char letter;

            switch (letter = fp.read_letter()) {
                default:
                    bug("Load_specials: letter '%c' not *MS.", letter);
                    exit(1);

                case 'S':
                    return;

                case '*':
                    break;

                case 'M':
                    pMobIndex = get_mob_index(fp.read_number());
                    pMobIndex.spec_fun = spec_lookup(fp.read_word());
                    if (pMobIndex.spec_fun == null) {
                        bug("Load_specials: 'M': vnum %d.", pMobIndex.vnum);
                        exit(1);
                    }
                    break;
            }

            fp.read_to_eol();
        }
    }

    /*
     * Translate all room exits from virtual to real.
     * Has to be done after all rooms are read in.
     * Check for bad reverse exits.
     */

    private static void fix_exits() {
        for (int iHash = 0; iHash < MAX_KEY_HASH; iHash++) {
            for (ROOM_INDEX_DATA roomIndex = room_index_hash[iHash]; roomIndex != null; roomIndex = roomIndex.next) {
                var hasExit = false;
                for (int door = 0; door <= 5; door++) {
                    EXIT_DATA exit = roomIndex.exit[door];
                    if (exit == null) {
                        continue;
                    }
                    if (exit.vnum <= 0 || get_room_index(exit.vnum) == null) {
                        exit.to_room = null;
                    } else {
                        hasExit = true;
                        exit.to_room = get_room_index(exit.vnum);
                    }
                }
                if (!hasExit) {
                    // Rooms with no exits are ROOM_NO_MOB.
                    roomIndex.room_flags = SET_BIT(roomIndex.room_flags, ROOM_NO_MOB);
                }
            }
        }

        for (int iHash = 0; iHash < MAX_KEY_HASH; iHash++) {
            for (ROOM_INDEX_DATA room = room_index_hash[iHash]; room != null; room = room.next) {
                for (int door = 0; door <= 5; door++) {
                    EXIT_DATA exit = room.exit[door];
                    if (exit == null) {
                        continue;
                    }
                    ROOM_INDEX_DATA toRoom = exit.to_room;
                    EXIT_DATA revExit = toRoom == null ? null : toRoom.exit[rev_dir[door]];
                    if (toRoom == null || revExit == null) {
                        continue;
                    }
                    if (revExit.to_room != room && revExit.to_room.area == room.area && !checkRoomCanHaveNonSymmetricExit(room.vnum)) {
                        var buf = new TextBuffer();
                        buf.sprintf("Fix_exits: %d:%d expected to have reverse exit %d:%d but the reverse exit room is %d .",
                                room.vnum, door,
                                toRoom.vnum, rev_dir[door],
                                (revExit.to_room == null ? 0 : revExit.to_room.vnum));
                        bug(buf, (DikuTextFile) null);
                    }
                }
            }
        }
    }

    private static boolean checkRoomCanHaveNonSymmetricExit(int vnum) {
        return (vnum >= 1200 && vnum <= 1299)
                || vnum == 20034 || vnum == 20044
                || vnum == 15091 || vnum == 15093
                || vnum == 12124 || vnum == 12125 || vnum == 12126 || vnum == 12127;
    }

    /*
     * Repopulate areas periodically.
     */

    static void area_update() {
        AREA_DATA pArea;
        DESCRIPTOR_DATA d;

        for (pArea = area_first; pArea != null; pArea = pArea.next) {

            if (++pArea.age < 3) {
                continue;
            }

            /*
             * Check age and reset.
             * Note: Mud School resets every 3 minutes (not 15).
             */
            if ((!pArea.empty && (pArea.nplayer == 0 || pArea.age >= 15)) || pArea.age >= 31) {
                ROOM_INDEX_DATA pRoomIndex;

                reset_area(pArea);
                var str = pArea.name + " has just been reset.";
                wiznet(str, null, null, WIZ_RESETS, 0, 0);

                if (pArea.resetmsg != null) {
                    str = pArea.resetmsg + "\n";
                } else {
                    str = "You hear some squeaking sounds...\n";
                }
                for (d = descriptor_list; d != null; d = d.next) {
                    if (d.connected == CON_PLAYING && IS_AWAKE(d.character) && d.character.in_room != null) {
                        if (d.character.in_room.area == pArea) {
                            send_to_char(str, d.character);
                        }
                    }
                }

                pArea.age = number_range(0, 3);
                pRoomIndex = get_room_index(200);
                if (pRoomIndex != null && pArea == pRoomIndex.area) {
                    pArea.age = 15 - 2;
                }
                pRoomIndex = get_room_index(210);
                if (pRoomIndex != null && pArea == pRoomIndex.area) {
                    pArea.age = 15 - 2;
                }
                pRoomIndex = get_room_index(220);
                if (pRoomIndex != null && pArea == pRoomIndex.area) {
                    pArea.age = 15 - 2;
                }
                pRoomIndex = get_room_index(230);
                if (pRoomIndex != null && pArea == pRoomIndex.area) {
                    pArea.age = 15 - 2;
                }
                pRoomIndex = get_room_index(ROOM_VNUM_SCHOOL);
                if (pRoomIndex != null && pArea == pRoomIndex.area) {
                    pArea.age = 15 - 2;
                } else if (pArea.nplayer == 0) {
                    pArea.empty = true;
                }
            }
        }
    }

    /*
     * Reset one area.
     */

    static void reset_area(AREA_DATA pArea) {
        RESET_DATA pReset;
        CHAR_DATA mob;
        boolean last;
        int level;
        int i;
        ROOM_INDEX_DATA room;
        DESCRIPTOR_DATA d;
        CHAR_DATA ch;

        if (weather_info.sky == SKY_RAINING) {
            for (d = descriptor_list; d != null; d = d.next) {
                if (d.connected != CON_PLAYING) {
                    continue;
                }
                ch = (d.original != null) ? d.original : d.character;
                if ((ch.in_room.area == pArea) && (get_skill(ch, gsn_track) > 50) &&
                        (!IS_SET(ch.in_room.room_flags, ROOM_INDOORS))) {
                    send_to_char("Rain devastates the tracks on the ground.\n", ch);
                }
            }
            for (i = pArea.min_vnum; i < pArea.max_vnum; i++) {
                room = get_room_index(i);
                if (room == null) {
                    continue;
                }
                if (IS_SET(room.room_flags, ROOM_INDOORS)) {
                    continue;
                }
                room_record("erased", room, -1);
                if (number_percent() < 50) {
                    room_record("erased", room, -1);
                }
            }
        }
        mob = null;
        last = true;
        level = 0;
        for (pReset = pArea.reset_first; pReset != null; pReset = pReset.next) {
            ROOM_INDEX_DATA pRoomIndex;
            MOB_INDEX_DATA pMobIndex;
            OBJ_INDEX_DATA pObjIndex;
            OBJ_INDEX_DATA pObjToIndex;
            OBJ_INDEX_DATA cabal_item;
            EXIT_DATA pexit;
            OBJ_DATA obj;
            OBJ_DATA obj_to;
            int count = 0, limit, ci_vnum = 0;

            switch (pReset.command) {
                default -> bug("Reset_area: bad command %c.", pReset.command);
                case 'M' -> {
                    if ((pMobIndex = get_mob_index(pReset.arg1)) == null) {
                        bug("Reset_area: 'M': bad vnum %d.", pReset.arg1);
                        continue;
                    }
                    if ((pRoomIndex = get_room_index(pReset.arg3)) == null) {
                        bug("Reset_area: 'R': bad vnum %d.", pReset.arg3);
                        continue;
                    }
                    if (pMobIndex.count >= pReset.arg2) {
                        last = false;
                        break;
                    }
                    count = 0;
                    for (mob = pRoomIndex.people; mob != null; mob = mob.next_in_room) {
                        if (mob.pIndexData == pMobIndex) {
                            count++;
                            if (count >= pReset.arg4) {
                                last = false;
                                break;
                            }
                        }
                    }
                    if (count >= pReset.arg4) {
                        break;
                    }
                    mob = create_mobile(pMobIndex);

                    /*
                     * Check for pet shop.
                     */
                    {
                        ROOM_INDEX_DATA pRoomIndexPrev;
                        pRoomIndexPrev = get_room_index(pRoomIndex.vnum - 1);
                        if (pRoomIndexPrev != null
                                && IS_SET(pRoomIndexPrev.room_flags, ROOM_PET_SHOP)) {
                            mob.act = SET_BIT(mob.act, ACT_PET);
                        }
                    }

                    /* set area */
                    mob.zone = pRoomIndex.area;
                    char_to_room(mob, pRoomIndex);
                    level = URANGE(0, mob.level - 2, LEVEL_HERO - 1);
                    last = true;
                }
                case 'O' -> {
                    if ((pObjIndex = get_obj_index(pReset.arg1)) == null) {
                        bug("Reset_area: 'O': bad vnum %d.", pReset.arg1);
                        continue;
                    }
                    if ((pRoomIndex = get_room_index(pReset.arg3)) == null) {
                        bug("Reset_area: 'R': bad vnum %d.", pReset.arg3);
                        continue;
                    }
                    if (pArea.nplayer > 0 || count_obj_list(pObjIndex, pRoomIndex.contents) > 0) {
                        last = false;
                        break;
                    }
                    switch (pObjIndex.vnum) {
                        case OBJ_VNUM_RULER_STAND -> ci_vnum = cabal_table[CABAL_RULER].obj_vnum;
                        case OBJ_VNUM_INVADER_SKULL -> ci_vnum = cabal_table[CABAL_INVADER].obj_vnum;
                        case OBJ_VNUM_SHALAFI_ALTAR -> ci_vnum = cabal_table[CABAL_SHALAFI].obj_vnum;
                        case OBJ_VNUM_CHAOS_ALTAR -> ci_vnum = cabal_table[CABAL_CHAOS].obj_vnum;
                        case OBJ_VNUM_KNIGHT_ALTAR -> ci_vnum = cabal_table[CABAL_KNIGHT].obj_vnum;
                        case OBJ_VNUM_LIONS_ALTAR -> ci_vnum = cabal_table[CABAL_LIONS].obj_vnum;
                        case OBJ_VNUM_BATTLE_THRONE -> ci_vnum = cabal_table[CABAL_BATTLE].obj_vnum;
                        case OBJ_VNUM_HUNTER_ALTAR -> ci_vnum = cabal_table[CABAL_HUNTER].obj_vnum;
                    }
                    cabal_item = get_obj_index(ci_vnum);
                    if (ci_vnum != 0 && cabal_item.count > 0) {
                        last = false;
                        break;
                    }
                    if ((pObjIndex.limit != -1) && (pObjIndex.count >= pObjIndex.limit)) {
                        last = false;
                        break;
                    }
                    obj = create_object(pObjIndex, UMIN(number_fuzzy(level),
                            LEVEL_HERO - 1));
                    obj.cost = 0;
                    obj_to_room(obj, pRoomIndex);
                    last = true;
                }
                case 'P' -> {
                    if ((pObjIndex = get_obj_index(pReset.arg1)) == null) {
                        bug("Reset_area: 'P': bad vnum %d.", pReset.arg1);
                        continue;
                    }
                    if ((pObjToIndex = get_obj_index(pReset.arg3)) == null) {
                        bug("Reset_area: 'P': bad vnum %d.", pReset.arg3);
                        continue;
                    }
                    if (pReset.arg2 > 50)      /* old format */ {
                        limit = 6;
                    } else if (pReset.arg2 == -1)    /* no limit */ {
                        limit = 999;
                    } else {
                        limit = pReset.arg2;
                    }
                    if (pArea.nplayer > 0
                            || (obj_to = get_obj_type(pObjToIndex)) == null
                            || (obj_to.in_room == null && !last)
                            || (pObjIndex.count >= limit && number_range(0, 4) != 0)
                            || (count = count_obj_list(pObjIndex, obj_to.contains)) > pReset.arg4
                    ) {
                        last = false;
                        break;
                    }
                    if ((pObjIndex.limit != -1) && (pObjIndex.count >= pObjIndex.limit)) {
                        last = false;
                        log("Reseting area: [P] OBJ limit reached\n");
                        break;
                    }
                    while (count < pReset.arg4) {
                        obj = create_object(pObjIndex, number_fuzzy(obj_to.level));
                        obj_to_obj(obj, obj_to);
                        count++;
                        if (pObjIndex.count >= limit) {
                            break;
                        }
                    }
                    /* fix object lock state! */
                    obj_to.value[1] = obj_to.pIndexData.value[1];
                    last = true;
                }
                case 'G', 'E' -> {
                    if ((pObjIndex = get_obj_index(pReset.arg1)) == null) {
                        bug("Reset_area: 'E' or 'G': bad vnum %d.", pReset.arg1);
                        continue;
                    }
                    if (!last) {
                        break;
                    }
                    if (mob == null) {
                        bug("Reset_area: 'E' or 'G': null mob for vnum %d.",
                                pReset.arg1);
                        last = false;
                        break;
                    }
                    if (mob.pIndexData.pShop != null) {
                        var olevel = 0;

                        if (!pObjIndex.new_format) {
                            switch (pObjIndex.item_type) {
                                case ITEM_PILL, ITEM_POTION, ITEM_SCROLL -> {
                                    olevel = MAX_LEVEL - 7;
                                    for (i = 1; i < 5; i++) {
                                        if (pObjIndex.value[i] > 0) {
                                            for (var j = 0; j < MAX_CLASS; j++) {
                                                olevel = UMIN(olevel, Skill.skills[pObjIndex.value[i]].skill_level[j]);
                                            }
                                        }
                                    }
                                    olevel = UMAX(0, (olevel * 3 / 4) - 2);
                                }
                                case ITEM_WAND -> olevel = number_range(10, 20);
                                case ITEM_STAFF -> olevel = number_range(15, 25);
                                case ITEM_ARMOR -> olevel = number_range(5, 15);
                                case ITEM_WEAPON -> olevel = number_range(5, 15);
                                case ITEM_TREASURE -> olevel = number_range(10, 20);
                            }
                        }

                        obj = create_object(pObjIndex, olevel);
                        obj.extra_flags = SET_BIT(obj.extra_flags, ITEM_INVENTORY);
                    } else {
                        if ((pObjIndex.limit == -1) || (pObjIndex.count < pObjIndex.limit)) {
                            obj = create_object(pObjIndex, UMIN(number_fuzzy(level),
                                    LEVEL_HERO - 1));
                        } else {
                            break;
                        }

                    }
                    obj_to_char(obj, mob);
                    if (pReset.command == 'E') {
                        var iWear = wear_convert(pReset.arg3);
                        if (iWear != WEAR_NONE) {
                            equip_char(mob, obj, iWear);
                        }
                    }
                    last = true;
                }
                case 'D' -> {
                    if ((pRoomIndex = get_room_index(pReset.arg1)) == null) {
                        bug("Reset_area: 'D': bad vnum %d.", pReset.arg1);
                        continue;
                    }
                    if ((pexit = pRoomIndex.exit[pReset.arg2]) == null) {
                        break;
                    }
                    switch (pReset.arg3) {
                        case 0 -> {
                            pexit.exit_info = REMOVE_BIT(pexit.exit_info, EX_CLOSED);
                            pexit.exit_info = REMOVE_BIT(pexit.exit_info, EX_LOCKED);
                        }
                        case 1 -> {
                            pexit.exit_info = SET_BIT(pexit.exit_info, EX_CLOSED);
                            pexit.exit_info = REMOVE_BIT(pexit.exit_info, EX_LOCKED);
                        }
                        case 2 -> {
                            pexit.exit_info = SET_BIT(pexit.exit_info, EX_CLOSED);
                            pexit.exit_info = SET_BIT(pexit.exit_info, EX_LOCKED);
                        }
                    }
                    last = true;
                }
                case 'R' -> {
                    if ((pRoomIndex = get_room_index(pReset.arg1)) == null) {
                        bug("Reset_area: 'R': bad vnum %d.", pReset.arg1);
                        continue;
                    }
                    {
                        int d0;
                        int d1;

                        for (d0 = 0; d0 < pReset.arg2 - 1; d0++) {
                            d1 = number_range(d0, pReset.arg2 - 1);
                            pexit = pRoomIndex.exit[d0];
                            pRoomIndex.exit[d0] = pRoomIndex.exit[d1];
                            pRoomIndex.exit[d1] = pexit;
                        }
                    }
                }
            }
        }
    }

    /*
     * Create an instance of a mobile.
     */

    static CHAR_DATA create_mobile(MOB_INDEX_DATA pMobIndex) {


        mobile_count++;

        if (pMobIndex == null) {
            bug("Create_mobile: null pMobIndex.");
            exit(1);
        }

        var mob = new_char();

        mob.pIndexData = pMobIndex;

        mob.name = pMobIndex.player_name;
        mob.id = get_mob_id();
        mob.short_descr = pMobIndex.short_descr;
        mob.long_descr = pMobIndex.long_descr;
        mob.description = pMobIndex.description;
        mob.spec_fun = pMobIndex.spec_fun;
        mob.prompt = null;
        mob.progtypes = pMobIndex.progtypes;
        mob.riding = false;
        mob.mount = null;
        mob.hunting = null;
        mob.endur = 0;
        mob.in_mind = null;
        mob.cabal = CABAL_NONE;
        mob.clazz = Clazz.MOB_CLASS;


        if (pMobIndex.wealth == 0) {
            mob.silver = 0;
            mob.gold = 0;
        } else {
            int wealth;

            wealth = number_range(pMobIndex.wealth / 2, 3 * pMobIndex.wealth / 2);
            mob.gold = number_range(wealth / 200, wealth / 100);
            mob.silver = wealth - (mob.gold * 100);
        }

        if (pMobIndex.new_format)
            /* load in new style */ {
            /* read from prototype */
            mob.group = pMobIndex.group;
            mob.act = pMobIndex.act | ACT_IS_NPC;
            mob.comm = COMM_NOCHANNELS | COMM_NOSHOUT | COMM_NOTELL;
            mob.affected_by = pMobIndex.affected_by;
            mob.alignment = pMobIndex.alignment;
            mob.level = pMobIndex.level;
            mob.hitroll = ((mob.level / 2) + pMobIndex.hitroll);
            mob.damroll = pMobIndex.damage[DICE_BONUS];
            mob.max_hit = dice(pMobIndex.hit[DICE_NUMBER],
                    pMobIndex.hit[DICE_TYPE])
                    + pMobIndex.hit[DICE_BONUS];
            mob.hit = mob.max_hit;
            mob.max_mana = dice(pMobIndex.mana[DICE_NUMBER],
                    pMobIndex.mana[DICE_TYPE])
                    + pMobIndex.mana[DICE_BONUS];
            mob.mana = mob.max_mana;
            mob.damage[DICE_NUMBER] = pMobIndex.damage[DICE_NUMBER];
            mob.damage[DICE_TYPE] = pMobIndex.damage[DICE_TYPE];
            mob.dam_type = pMobIndex.dam_type;

            mob.status = 0;
            if (mob.dam_type == 0) {
                switch (number_range(1, 3)) {
                    case (1) -> mob.dam_type = 3;
                    /* slash */
                    case (2) -> mob.dam_type = 7;
                    /* pound */
                    case (3) -> mob.dam_type = 11;
                    /* pierce */
                }
            }
            System.arraycopy(pMobIndex.ac, 0, mob.armor, 0, 4);

            mob.off_flags = pMobIndex.off_flags;
            mob.imm_flags = pMobIndex.imm_flags;
            mob.res_flags = pMobIndex.res_flags;
            mob.vuln_flags = pMobIndex.vuln_flags;
            mob.start_pos = pMobIndex.start_pos;
            mob.default_pos = pMobIndex.default_pos;
            mob.sex = pMobIndex.sex;
            if (mob.sex == 3) /* random sex */ {
                mob.sex = number_range(1, 2);
            }
            mob.race = pMobIndex.race;
            mob.form = pMobIndex.form;
            mob.parts = pMobIndex.parts;
            mob.size = pMobIndex.size;
            mob.material = pMobIndex.material;
            mob.progtypes = pMobIndex.progtypes;
            mob.extracted = false;

            /* computed on the spot */

            for (var i = 0; i < MAX_STATS; i++) {
                mob.perm_stat[i] = UMIN(25, 11 + mob.level / 4);
            }

            if (IS_SET(mob.act, ACT_WARRIOR)) {
                mob.perm_stat[STAT_STR] += 3;
                mob.perm_stat[STAT_INT] -= 1;
                mob.perm_stat[STAT_CON] += 2;
            }

            if (IS_SET(mob.act, ACT_THIEF)) {
                mob.perm_stat[STAT_DEX] += 3;
                mob.perm_stat[STAT_INT] += 1;
                mob.perm_stat[STAT_WIS] -= 1;
            }

            if (IS_SET(mob.act, ACT_CLERIC)) {
                mob.perm_stat[STAT_WIS] += 3;
                mob.perm_stat[STAT_DEX] -= 1;
                mob.perm_stat[STAT_STR] += 1;
            }

            if (IS_SET(mob.act, ACT_MAGE)) {
                mob.perm_stat[STAT_INT] += 3;
                mob.perm_stat[STAT_STR] -= 1;
                mob.perm_stat[STAT_DEX] += 1;
            }

            if (IS_SET(mob.off_flags, OFF_FAST)) {
                mob.perm_stat[STAT_DEX] += 2;
            }

            mob.perm_stat[STAT_STR] += mob.size - SIZE_MEDIUM;
            mob.perm_stat[STAT_CON] += (mob.size - SIZE_MEDIUM) / 2;


            var af = new AFFECT_DATA();
            /* let's get some spell action */
            if (IS_AFFECTED(mob, AFF_SANCTUARY)) {
                af.where = TO_AFFECTS;
                af.type = Skill.gsn_sanctuary;
                af.level = mob.level;
                af.duration = -1;
                af.location = APPLY_NONE;
                af.modifier = 0;
                af.bitvector = AFF_SANCTUARY;
                affect_to_char(mob, af);
            }

            if (IS_AFFECTED(mob, AFF_HASTE)) {
                af.where = TO_AFFECTS;
                af.type = Skill.gsn_haste;
                af.level = mob.level;
                af.duration = -1;
                af.location = APPLY_DEX;
                af.modifier = (1 + (mob.level >= 18 ? 1 : 0) + (mob.level >= 25 ? 1 : 0) + (mob.level >= 32 ? 1 : 0));
                af.bitvector = AFF_HASTE;
                affect_to_char(mob, af);
            }

            if (IS_AFFECTED(mob, AFF_PROTECT_EVIL)) {
                af.where = TO_AFFECTS;
                af.type = Skill.gsn_protection_evil;
                af.level = mob.level;
                af.duration = -1;
                af.location = APPLY_SAVES;
                af.modifier = -1;
                af.bitvector = AFF_PROTECT_EVIL;
                affect_to_char(mob, af);
            }

            if (IS_AFFECTED(mob, AFF_PROTECT_GOOD)) {
                af.where = TO_AFFECTS;
                af.type = Skill.gsn_protection_good;
                af.level = mob.level;
                af.duration = -1;
                af.location = APPLY_SAVES;
                af.modifier = -1;
                af.bitvector = AFF_PROTECT_GOOD;
                affect_to_char(mob, af);
            }
        } else /* read in old format and convert */ {
            mob.act = pMobIndex.act;
            mob.affected_by = pMobIndex.affected_by;
            mob.alignment = pMobIndex.alignment;
            mob.level = pMobIndex.level;
            mob.hitroll = UMAX(pMobIndex.hitroll, pMobIndex.level / 4);
            mob.damroll = (pMobIndex.level / 2);
            if (mob.level < 30) {
                mob.max_hit = mob.level * 20 + number_range(mob.level, mob.level * 5);
            } else if (mob.level < 60) {
                mob.max_hit = mob.level * 50 + number_range(mob.level * 10, mob.level * 50);
            } else {
                mob.max_hit = mob.level * 100 + number_range(mob.level * 20, mob.level * 100);
            }
            if (IS_SET(mob.act, ACT_MAGE | ACT_CLERIC)) {
                mob.max_hit *= 0.9;
            }
            mob.hit = mob.max_hit;
            mob.max_mana = 100 + dice(mob.level, 10);
            mob.mana = mob.max_mana;
            switch (number_range(1, 3)) {
                case (1) -> mob.dam_type = 3;
                /* slash */
                case (2) -> mob.dam_type = 7;
                /* pound */
                case (3) -> mob.dam_type = 11;
                /* pierce */
            }
            for (var i = 0; i < 3; i++) {
                mob.armor[i] = interpolate(mob.level, 100, -100);
            }
            mob.armor[3] = interpolate(mob.level, 100, 0);
            mob.race = pMobIndex.race;
            mob.off_flags = pMobIndex.off_flags;
            mob.imm_flags = pMobIndex.imm_flags;
            mob.res_flags = pMobIndex.res_flags;
            mob.vuln_flags = pMobIndex.vuln_flags;
            mob.start_pos = pMobIndex.start_pos;
            mob.default_pos = pMobIndex.default_pos;
            mob.sex = pMobIndex.sex;
            mob.form = pMobIndex.form;
            mob.parts = pMobIndex.parts;
            mob.size = SIZE_MEDIUM;
            mob.material = "";
            mob.extracted = false;
/*
        for (i = 0; i < MAX_STATS; i ++)
            mob.perm_stat[i] = 11 + mob.level/4;
 computed on the spot */

            for (var i = 0; i < MAX_STATS; i++) {
                mob.perm_stat[i] = UMIN(25, 11 + mob.level / 4);
            }

            if (IS_SET(mob.act, ACT_WARRIOR)) {
                mob.perm_stat[STAT_STR] += 3;
                mob.perm_stat[STAT_INT] -= 1;
                mob.perm_stat[STAT_CON] += 2;
            }

            if (IS_SET(mob.act, ACT_THIEF)) {
                mob.perm_stat[STAT_DEX] += 3;
                mob.perm_stat[STAT_INT] += 1;
                mob.perm_stat[STAT_WIS] -= 1;
            }

            if (IS_SET(mob.act, ACT_CLERIC)) {
                mob.perm_stat[STAT_WIS] += 3;
                mob.perm_stat[STAT_DEX] -= 1;
                mob.perm_stat[STAT_STR] += 1;
            }

            if (IS_SET(mob.act, ACT_MAGE)) {
                mob.perm_stat[STAT_INT] += 3;
                mob.perm_stat[STAT_STR] -= 1;
                mob.perm_stat[STAT_DEX] += 1;
            }

            if (IS_SET(mob.off_flags, OFF_FAST)) {
                mob.perm_stat[STAT_DEX] += 2;
            }
        }

        mob.position = mob.start_pos;


        if (mob.gold > mob.level) {
            mob.gold = dice(6, mob.level);
        }

        /* link the mob to the world list */
        mob.next = char_list;
        char_list = mob;
        pMobIndex.count++;
        return mob;
    }

    /* duplicate a mobile exactly -- except inventory */

    static void clone_mobile(CHAR_DATA parent, CHAR_DATA clone) {
        int i;
        AFFECT_DATA paf;

        if (parent == null || clone == null || !IS_NPC(parent)) {
            return;
        }

        /* start fixing values */
        clone.name = parent.name;
        clone.short_descr = parent.short_descr;
        clone.long_descr = parent.long_descr;
        clone.description = parent.description;
        clone.group = parent.group;
        clone.sex = parent.sex;
        clone.clazz = parent.clazz;
        clone.race = parent.race;
        clone.level = parent.level;
        clone.trust = 0;
        clone.timer = parent.timer;
        clone.wait = parent.wait;
        clone.hit = parent.hit;
        clone.max_hit = parent.max_hit;
        clone.mana = parent.mana;
        clone.max_mana = parent.max_mana;
        clone.move = parent.move;
        clone.max_move = parent.max_move;
        clone.gold = parent.gold;
        clone.silver = parent.silver;
        clone.exp = parent.exp;
        clone.act = parent.act;
        clone.comm = parent.comm;
        clone.imm_flags = parent.imm_flags;
        clone.res_flags = parent.res_flags;
        clone.vuln_flags = parent.vuln_flags;
        clone.invis_level = parent.invis_level;
        clone.affected_by = parent.affected_by;
        clone.position = parent.position;
        clone.practice = parent.practice;
        clone.train = parent.train;
        clone.saving_throw = parent.saving_throw;
        clone.alignment = parent.alignment;
        clone.hitroll = parent.hitroll;
        clone.damroll = parent.damroll;
        clone.wimpy = parent.wimpy;
        clone.form = parent.form;
        clone.parts = parent.parts;
        clone.size = parent.size;
        clone.material = parent.material;
        clone.extracted = parent.extracted;
        clone.off_flags = parent.off_flags;
        clone.dam_type = parent.dam_type;
        clone.start_pos = parent.start_pos;
        clone.default_pos = parent.default_pos;
        clone.spec_fun = parent.spec_fun;
        clone.progtypes = parent.progtypes;
        clone.status = parent.status;
        clone.hunting = null;
        clone.endur = 0;
        clone.in_mind = null;
        clone.cabal = CABAL_NONE;

        for (i = 0; i < 4; i++) {
            clone.armor[i] = parent.armor[i];
        }

        for (i = 0; i < MAX_STATS; i++) {
            clone.perm_stat[i] = parent.perm_stat[i];
            clone.mod_stat[i] = parent.mod_stat[i];
        }

        for (i = 0; i < 3; i++) {
            clone.damage[i] = parent.damage[i];
        }

        /* now add the affects */
        for (paf = parent.affected; paf != null; paf = paf.next) {
            affect_to_char(clone, paf);
        }

    }

    /*
     * Create an object with modifying the count
     */

    static OBJ_DATA create_object(OBJ_INDEX_DATA pObjIndex, int level) {
        return create_object_org(pObjIndex, level, true);
    }

    /*
     * for player load/quit
     * Create an object and do not modify the count
     */

    static OBJ_DATA create_object_nocount(OBJ_INDEX_DATA pObjIndex, int level) {
        return create_object_org(pObjIndex, level, false);
    }

    /*
     * Create an instance of an object.
     */

    static OBJ_DATA create_object_org(OBJ_INDEX_DATA pObjIndex, int level, boolean Count) {
        AFFECT_DATA paf;
        OBJ_DATA obj;
        int i;


        if (pObjIndex == null) {
            bug("Create_object: null pObjIndex.");
            exit(1);
            return null;
        }

        obj = new OBJ_DATA();

        obj.pIndexData = pObjIndex;
        obj.in_room = null;
        obj.enchanted = false;

        for (i = 1; i < MAX_CABAL; i++) {
            if (pObjIndex.vnum == cabal_table[i].obj_vnum) {
                /*
                if ( count_obj_list( pObjIndex, object_list) > 0 )
                  return(null);
                */
                cabal_table[i].obj_ptr = obj;
                break;
            }
        }
        if ((obj.pIndexData.limit != -1) && (obj.pIndexData.count >= obj.pIndexData.limit)) {
            if (pObjIndex.new_format) {
                log("");
            }
        }

        if (pObjIndex.new_format) {
            obj.level = pObjIndex.level;
        } else {
            obj.level = UMAX(0, level);
        }
        obj.wear_loc = -1;


        obj.name = pObjIndex.name;
        obj.short_descr = pObjIndex.short_descr;
        obj.description = pObjIndex.description;
        obj.material = pObjIndex.material;
        obj.item_type = pObjIndex.item_type;
        obj.extra_flags = pObjIndex.extra_flags;
        obj.wear_flags = pObjIndex.wear_flags;
        obj.value[0] = pObjIndex.value[0];
        obj.value[1] = pObjIndex.value[1];
        obj.value[2] = pObjIndex.value[2];
        obj.value[3] = pObjIndex.value[3];
        obj.value[4] = pObjIndex.value[4];
        obj.weight = pObjIndex.weight;
        obj.extracted = false;
        obj.progtypes = pObjIndex.progtypes;
        obj.from = ""; /* used with body parts */
        obj.pit = OBJ_VNUM_PIT; /* default for corpse decaying */
        obj.altar = ROOM_VNUM_ALTAR; /* default for corpses */
        obj.condition = pObjIndex.condition;

        if (level == -1 || pObjIndex.new_format) {
            obj.cost = pObjIndex.cost;
        } else {
            obj.cost = number_fuzzy(10) * number_fuzzy(level) * number_fuzzy(level);
        }

        /*
         * Mess with object properties.
         */
        switch (obj.item_type) {
            default -> bug("Read_object: vnum %d bad type.", pObjIndex.vnum);
            case ITEM_LIGHT -> {
                if (obj.value[2] == 999) {
                    obj.value[2] = -1;
                }
            }
            case ITEM_FURNITURE, ITEM_TRASH, ITEM_CONTAINER, ITEM_DRINK_CON, ITEM_KEY, ITEM_FOOD, ITEM_BOAT, ITEM_CORPSE_NPC, ITEM_CORPSE_PC, ITEM_FOUNTAIN, ITEM_MAP, ITEM_CLOTHING, ITEM_PORTAL -> {
                if (!pObjIndex.new_format) {
                    obj.cost /= 5;
                }
            }
            case ITEM_TREASURE, ITEM_WARP_STONE, ITEM_ROOM_KEY, ITEM_GEM, ITEM_JEWELRY, ITEM_TATTOO -> {
            }
            case ITEM_JUKEBOX -> {
                for (i = 0; i < 5; i++) {
                    obj.value[i] = -1;
                }
            }
            case ITEM_SCROLL -> {
                if (level != -1 && !pObjIndex.new_format) {
                    obj.value[0] = number_fuzzy(obj.value[0]);
                }
            }
            case ITEM_WAND, ITEM_STAFF -> {
                if (level != -1 && !pObjIndex.new_format) {
                    obj.value[0] = number_fuzzy(obj.value[0]);
                    obj.value[1] = number_fuzzy(obj.value[1]);
                    obj.value[2] = obj.value[1];
                }
                if (!pObjIndex.new_format) {
                    obj.cost *= 2;
                }
            }
            case ITEM_WEAPON -> {
                if (level != -1 && !pObjIndex.new_format) {
                    obj.value[1] = number_fuzzy(number_fuzzy(level / 4 + 2));
                    obj.value[2] = number_fuzzy(number_fuzzy(3 * level / 4 + 6));
                }
            }
            case ITEM_ARMOR -> {
                if (level != -1 && !pObjIndex.new_format) {
                    obj.value[0] = number_fuzzy(level / 5 + 3);
                    obj.value[1] = number_fuzzy(level / 5 + 3);
                    obj.value[2] = number_fuzzy(level / 5 + 3);
                }
            }
            case ITEM_POTION, ITEM_PILL -> {
                if (level != -1 && !pObjIndex.new_format) {
                    obj.value[0] = number_fuzzy(number_fuzzy(obj.value[0]));
                }
            }
            case ITEM_MONEY -> {
                if (!pObjIndex.new_format) {
                    obj.value[0] = obj.cost;
                }
            }
        }

        for (paf = pObjIndex.affected; paf != null; paf = paf.next) {
            if (paf.location == APPLY_SPELL_AFFECT) {
                affect_to_obj(obj, paf);
            }
        }

        obj.next = object_list;
        object_list = obj;
        if (Count) {
            pObjIndex.count++;
        }
        return obj;
    }

    /* duplicate an object exactly -- except contents */

    static void clone_object(OBJ_DATA parent, OBJ_DATA clone) {
        int i;
        AFFECT_DATA paf;
        EXTRA_DESCR_DATA ed, ed_new;

        if (parent == null || clone == null) {
            return;
        }

        /* start fixing the object */
        clone.name = parent.name;
        clone.short_descr = parent.short_descr;
        clone.description = parent.description;
        clone.item_type = parent.item_type;
        clone.extra_flags = parent.extra_flags;
        clone.wear_flags = parent.wear_flags;
        clone.weight = parent.weight;
        clone.cost = parent.cost;
        clone.level = parent.level;
        clone.condition = parent.condition;
        clone.material = parent.material;
        clone.timer = parent.timer;
        clone.from = parent.from;
        clone.extracted = parent.extracted;
        clone.pit = parent.pit;
        clone.altar = parent.altar;

        for (i = 0; i < 5; i++) {
            clone.value[i] = parent.value[i];
        }

        /* affects */
        clone.enchanted = parent.enchanted;

        for (paf = parent.affected; paf != null; paf = paf.next) {
            affect_to_obj(clone, paf);
        }

        /* extended desc */
        for (ed = parent.extra_descr; ed != null; ed = ed.next) {
            ed_new = new EXTRA_DESCR_DATA();
            ed_new.keyword = ed.keyword;
            ed_new.description = ed.description;
            ed_new.next = clone.extra_descr;
            clone.extra_descr = ed_new;
        }

    }

    /*
     * Clear a new character.
     */

    static void clear_char(@NotNull CHAR_DATA ch) {
        int i;

        ch.name = "";
        ch.short_descr = "";
        ch.long_descr = "";
        ch.description = "";
        ch.prompt = "";
        ch.logon = current_time;
        ch.lines = PAGELEN;
        for (i = 0; i < 4; i++) {
            ch.armor[i] = 100;
        }
        ch.position = POS_STANDING;
        ch.hit = 20;
        ch.max_hit = 20;
        ch.mana = 100;
        ch.max_mana = 100;
        ch.move = 100;
        ch.max_move = 100;
        ch.last_fought = null;
        ch.last_fight_time = -1;
        ch.last_death_time = -1;
        ch.on = null;
        for (i = 0; i < MAX_STATS; i++) {
            ch.perm_stat[i] = 13;
            ch.mod_stat[i] = 0;
        }
    }

    /*
     * Get an extra description from a list.
     */

    static String get_extra_descr(String name, EXTRA_DESCR_DATA ed) {
        for (; ed != null; ed = ed.next) {
            if (is_name(name, ed.keyword)) {
                return ed.description;
            }
        }
        return null;
    }

    /*
     * Translates mob virtual number to its mob index struct.
     * Hash table lookupRace.
     */

    static MOB_INDEX_DATA get_mob_index(int vnum) {
        MOB_INDEX_DATA pMobIndex;

        for (pMobIndex = mob_index_hash[vnum % MAX_KEY_HASH]; pMobIndex != null; pMobIndex = pMobIndex.next) {
            if (pMobIndex.vnum == vnum) {
                return pMobIndex;
            }
        }

        if (fBootDb) {
            bug("Get_mob_index: bad vnum %d.", vnum);
            exit(1);
        }

        return null;
    }

    /*
     * Translates mob virtual number to its obj index struct.
     * Hash table lookupRace.
     */
    @Nullable
    static OBJ_INDEX_DATA get_obj_index(int vnum) {
        OBJ_INDEX_DATA pObjIndex;
        for (pObjIndex = obj_index_hash[vnum % MAX_KEY_HASH];
             pObjIndex != null;
             pObjIndex = pObjIndex.next) {
            if (pObjIndex.vnum == vnum) {
                return pObjIndex;
            }
        }
        if (fBootDb) {
            bug("Get_obj_index: bad vnum %d.", vnum);
            exit(1);
        }
        return null;
    }

    /*
     * Translates mob virtual number to its room index struct.
     * Hash table lookupRace.
     */
    @Nullable
    static ROOM_INDEX_DATA get_room_index(int vnum) {
        ROOM_INDEX_DATA pRoomIndex;

        for (pRoomIndex = room_index_hash[vnum % MAX_KEY_HASH];
             pRoomIndex != null;
             pRoomIndex = pRoomIndex.next) {
            if (pRoomIndex.vnum == vnum) {
                return pRoomIndex;
            }
        }

        if (fBootDb) {
            bug("Get_room_index: bad vnum %d.", vnum);
            exit(1);
        }

        return null;
    }


    static void do_areas(@NotNull CHAR_DATA ch, String argument) {
        if (argument != null && !argument.isEmpty()) {
            send_to_char("No argument is used with this command.\n", ch);
            return;
        }

        AREA_DATA area1 = area_first;
        AREA_DATA area2 = area_first;
        int areaIndex;
        int halfAreasIndex = (top_area + 1) / 2;
        for (areaIndex = 0; areaIndex < halfAreasIndex; areaIndex++) {
            area2 = area2.next;
        }
        var bufpage = new StringBuilder(1024);
        bufpage.append("Current areas of Nightworks MUD: \n");
        var f = new Formatter(bufpage);
        for (areaIndex = 0; areaIndex < halfAreasIndex; areaIndex++) {
            var buf1 = formatAreaDetails(area1);
            var buf2 = area2 != null ? formatAreaDetails(area2) : "\n";
            if (IS_SET(ch.act, PLR_COLOR)) {
                f.format("%-69s %s\n", buf1, buf2);
            } else {
                f.format("%-39s %s\n", buf1, buf2);
            }
            area1 = area1.next;
            if (area2 != null) {
                area2 = area2.next;
            }
        }
        bufpage.append("\n");
        page_to_char(bufpage, ch);
    }

    private static String formatAreaDetails(@NotNull AREA_DATA pArea) {
        var f = new Formatter();
        f.format("{W%2d %3d{x} {b%s {c%s{x", pArea.low_range, pArea.high_range, pArea.writer, pArea.credits);
        return f.toString();
    }


    static void do_memory(@NotNull CHAR_DATA ch, String argument) {
        var buf = new StringBuilder(1024);
        var f = new Formatter(buf);
        f.format("Affects %5d\n", top_affect);
        f.format("Areas   %5d\n", top_area);
        f.format("ExDes   %5d\n", top_ed);
        f.format("Exits   %5d\n", top_exit);
        f.format("Helps   %5d\n", top_help);
        f.format("Socials %5d\n", social_table.size());
        f.format("Mobs    %5d(%d new format)\n", top_mob_index, newmobs);
        f.format("(in use)%5d\n", mobile_count);
        f.format("Objs    %5d(%d new format)\n", top_obj_index, newobjs);
        f.format("Resets  %5d\n", top_reset);
        f.format("Rooms   %5d\n", top_room);
        f.format("Shops   %5d\n", top_shop);
        send_to_char(f.toString(), ch);

    }

    static void do_dump(@NotNull CHAR_DATA ch, String argument) {
        /* open file */
        try {
            var fp = new FileWriter("mem.dmp", false);
            try {

                /* report use of data structures */

                var f = new Formatter(fp);
                /* mobile prototypes */
                f.format("MobProt %4d (%8d bytes)\n", top_mob_index, -1);

                /* mobs */
                int count = 0, num_pcs = 0, aff_count = 0;
                for (var fch = char_list; fch != null; fch = fch.next) {
                    count++;
                    if (fch.pcdata != null) {
                        num_pcs++;
                    }
                    for (var af = fch.affected; af != null; af = af.next) {
                        aff_count++;
                    }
                }
                f.format("Mobs    %4d (%8d bytes), %2d free (%d bytes)\n", count, -1, -1, -1);

                /* pcdata */
                f.format("Pcdata  %4d (%8d bytes), %2d free (%d bytes)\n", num_pcs, -1, -1, -1);

                /* descriptors */
                count = 0;
                for (var d = descriptor_list; d != null; d = d.next) {
                    count++;
                }

                f.format("Descs  %4d (%8d bytes), %2d free (%d bytes)\n", count, -1, -1, -1);

                /* object prototypes */
                var nMatch = 0;
                for (var vnum = 0; nMatch < top_obj_index; vnum++) {
                    OBJ_INDEX_DATA pObjIndex;
                    if ((pObjIndex = get_obj_index(vnum)) != null) {
                        for (var af = pObjIndex.affected; af != null; af = af.next) {
                            aff_count++;
                        }
                        nMatch++;
                    }
                }

                f.format("ObjProt %4d (%8d bytes)\n", top_obj_index, -1);

                /* objects */
                count = 0;
                for (var obj = object_list; obj != null; obj = obj.next) {
                    count++;
                    for (var af = obj.affected; af != null; af = af.next) {
                        aff_count++;
                    }
                }
                f.format("Objs    %4d (%8d bytes), %2d free (%d bytes)\n", count, -1, -1, -1);

                /* affects */

                f.format("Affects %4d (%8d bytes), %2d free (%d bytes)\n", aff_count, -1, -1, -1);

                /* rooms */
                f.format("Rooms   %4d (%8d bytes)\n", top_room, -1);

                /* exits */
                f.format("Exits   %4d (%8d bytes)\n", top_exit, -1);
            } finally {
                fp.close();
            }

            /* start printing out mobile data */
            fp = new FileWriter("mob.dmp", false);
            try {
                var f = new Formatter(fp);
                f.out().append("\nMobile Analysis\n");
                f.out().append("---------------\n");
                var nMatch = 0;
                for (var vnum = 0; nMatch < top_mob_index; vnum++) {
                    MOB_INDEX_DATA pMobIndex;
                    if ((pMobIndex = get_mob_index(vnum)) != null) {
                        nMatch++;
                        f.format("#%-4d %3d active %3d killed     %s\n", pMobIndex.vnum, pMobIndex.count, pMobIndex.killed, pMobIndex.short_descr);
                    }
                }
            } finally {
                fp.close();
            }

            /* start printing out object data */
            fp = new FileWriter("obj.dmp", false);
            try {
                var f = new Formatter(fp);
                f.out().append("\nObject Analysis\n");
                f.out().append("---------------\n");
                var nMatch = 0;
                for (var vnum = 0; nMatch < top_obj_index; vnum++) {
                    OBJ_INDEX_DATA pObjIndex;
                    if ((pObjIndex = get_obj_index(vnum)) != null) {
                        nMatch++;
                        f.format("#%-4d %3d active %3d reset      %s\n", pObjIndex.vnum, pObjIndex.count, pObjIndex.reset_num, pObjIndex.short_descr);
                    }
                }
            } finally {
                /* close file */
                fp.close();
            }
        } catch (IOException e) {
            logError(e);
        }
    }

    /*
     * Stick a little fuzz on a number.
     */

    static int number_fuzzy(int number) {
        switch (number_bits(2)) {
            case 0 -> number -= 1;
            case 3 -> number += 1;
        }
        return UMAX(1, number);
    }

    /*
     * Generate a random number.
     */

    static int number_range(int from, int to) {

        if (from == 0 && to == 0) {
            return 0;
        }

        if ((to = to - from + 1) <= 1) {
            return from;
        }

        var power = 2;
        while (power < to) {
            power <<= 1;
        }
        int number;
        while ((number = number_mm() & (power - 1)) >= to) ;
        return from + number;
    }

    /*
     * Generate a percentile roll.
     */

    static int number_percent() {
        int percent;
        while ((percent = number_mm() & (128 - 1)) > 99) ;
        return 1 + percent;
    }

    /*
     * Generate a random door.
     */

    static int number_door() {
        int door;
        while ((door = number_mm() & (8 - 1)) > 5) ;
        return door;
    }

    static int number_bits(int width) {
        return number_mm() & ((1 << width) - 1);
    }


    private static Random rnd;

    private static void init_mm() {
        rnd = new Random(System.currentTimeMillis());
    }

    static int number_mm() {
        return rnd.nextInt();
    }

    /*
     * Roll some dice.
     */

    static int dice(int number, int size) {
        switch (size) {
            case 0 -> {
                return 0;
            }
            case 1 -> {
                return number;
            }
        }

        var sum = 0;
        for (var idice = 0; idice < number; idice++) {
            sum += number_range(1, size);
        }

        return sum;
    }

    /*
     * Simple linear interpolation.
     */

    static int interpolate(int level, int value_00, int value_32) {
        return value_00 + level * (value_32 - value_00) / 32;
    }

    /*
     * Append a string to a file.
     */

    static void append_file(@NotNull CHAR_DATA ch, String file, String str) {
        if (IS_NPC(ch) || str == null) {
            return;
        }
        try (var fp = new FileWriter(file, true)) {
            var f = new Formatter();
            f.format("[%5d] %s: %s\n", (ch.in_room != null ? ch.in_room.vnum : 0), ch.name, str);
            fp.write(f.toString());
        } catch (IOException e) {
            perror(file);
            send_to_char("Could not open the file!\n", ch);
        }
    }

    /*
     * Reports a bug.
     */

    static void bug(CharSequence str, Object... params) {
        var str2 = new Formatter().format(str.toString(), params).toString();
        bug(str2, currentFile);
    }

    static void bug(CharSequence str) {
        bug(str, currentFile);
    }

    static void bug(@NotNull CharSequence str, @Nullable DikuTextFile fp) {
        if (fp != null) {
            str = str + "\n" + fp.buildCurrentStateInfo();
        }
        log_string(str);
    }

    /*
     * Writes a string to the log.
     */

    static void log_string(CharSequence str) {
        System.err.println(new Date() + "::" + str);
    }

    /*
     * This function is here to aid in debugging.
     * If the last expression in a function is another function call,
     *   gcc likes to generate a JMP instead of a CALL.
     * This is called "tail chaining."
     * It hoses the debugger call stack for that call.
     * So I make this the last call in certain critical functions,
     *   where I really need the call stack to be right for debugging!
     *
     * If you don't understand this, then LEAVE IT ALONE.
     * Don't remove any calls to tail_chain anywhere.
     *
     * -- Furey
     */

    static void tail_chain() {
    }

    static void load_olimits(DikuTextFile fp) {
        int vnum;
        int limit;
        char ch;
        OBJ_INDEX_DATA pIndex;

        for (ch = fp.read_letter(); ch != 'S'; ch = fp.read_letter()) {
            switch (ch) {
                case 'O' -> {
                    vnum = fp.read_number();
                    limit = fp.read_number();
                    if ((pIndex = get_obj_index(vnum)) == null) {
                        bug("Load_olimits: bad vnum %d", vnum);
                        exit(1);
                    } else {
                        pIndex.limit = limit;
                    }
                }
                case '*' -> fp.read_to_eol();
                default -> {
                    bug("Load_olimits: bad command '%c'", ch);
                    exit(1);
                }
            }
        }
    }

    /*
     * Add the objects in players not logged on to object count
     */

    private static void load_limited_objects() {
        var dir = new File(nw_config.lib_player_dir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        var files = dir.listFiles();
        if (!dir.exists() || !dir.isDirectory() || files == null) {
            bug("Load_limited_objects: unable to open player directory:" + dir.getAbsolutePath(), 0);
            exit(1);
            return;
        }
        for (var i = 0; i < files.length; i++) {
            var file = files[i];
            if (file.getName().length() < 3 || file.isDirectory()) {
                continue;
            }
            try {
                var fp = new DikuTextFile(file);
                var fReadLevel = false;
                var tplayed = 0;
                for (int letter = fp.read_letter(); !fp.is_eof(); letter = fp.read_letter()) {
                    if (letter == 'L') {
                        if (!fReadLevel) {
                            var word = fp.read_word();

                            if (!str_cmp(word, "evl") || !str_cmp(word, "ev") || !str_cmp(word, "evel")) {
                                i = fp.read_number();
                                fReadLevel = true;
                                total_levels += UMAX(0, i - 5);
                                log_string("[" + file.getName() + "]'s file +:" + UMAX(0, i - 5));
                            }
                        }
                    } else if (letter == 'P') {
                        var word = fp.read_word();

                        if (!str_cmp(word, "layLog")) {
                            int d, t, d_start, d_stop;

                            fp.read_number();    /* read the version */
                            while (true) {
                                d = fp.read_number();
                                if (d < 0) {
                                    break;
                                }
                                t = fp.read_number();

                                d_start = get_played_day(nw_config.max_time_log - 1);
                                d_stop = get_played_day(0);

                                if (d >= d_start && d <= d_stop) {
                                    tplayed += t;
                                }
                            }

                        }
                    } else if (letter == '#') {
                        var word = fp.read_word();
                        if (!str_cmp(word, "O") || !str_cmp(word, "OBJECT")) {
                            if (tplayed < nw_config.min_time_limit) {
                                log_string("Discarding the player " + file.getName() + "'s limited equipments.");
                                break;
                            }

                            fp.read_word();
                            fBootDb = false;
                            var vnum = fp.read_number();
                            OBJ_INDEX_DATA obj = get_obj_index(vnum);
                            if (obj != null) {
                                obj.count++;
                            }
                            fBootDb = true;
                        }
                    } else {
                        fp.read_to_eol();
                    }
                }
            } catch (IOException e) {
                bug("Load_limited_objects: Can't open player file: " + file.getAbsolutePath(), 0);
            }
        }
    }

    /*
     * Given a name, return the appropriate prac fun.
     */

    static int prac_lookup(String name) {
        for (var p : prac_table) {
            if (!str_prefix(name, p.name)) {
                return (1 << p.number);
            }
        }
        return 0;
    }

    /*
     * Snarf can prac declarations.
     */

    static void load_practicer(DikuTextFile fp) {
        for (; ; ) {
            MOB_INDEX_DATA pMobIndex;
            int letter = fp.read_letter();
            switch (letter) {
                default:
                    bug("Load_specials: letter '%c' not *MS.", letter);
                    exit(1);

                case 'S':
                    return;

                case '*':
                    break;

                case 'M':
                    pMobIndex = get_mob_index(fp.read_number());
                    pMobIndex.practicer = SET_BIT(pMobIndex.practicer, prac_lookup(fp.read_word()));
                    if (pMobIndex.practicer == 0) {
                        bug("Load_practicers: 'M': vnum %d.", pMobIndex.vnum);
                        exit(1);
                    }
                    break;
            }
            fp.read_to_eol();
        }
    }

    static void load_resetmsg(DikuTextFile fp) {
        Serarea.resetmsg = fp.read_string();
    }

    static void load_aflag(DikuTextFile fp) {
        Serarea.area_flag = fp.read_flag();
    }

    /*socials handling ported from SOG codebase, author: fjoe */
    static void load_socials() {
        DikuTextFile fp;
        try {
            fp = new DikuTextFile("etc" + "/" + "socials.conf");
            var social = new social_type();
            for (; ; ) {
                var word = fp.read_word();
                if (word.isEmpty()) {
                    break;
                }
                if (word.charAt(0) == '#') {
                    if (word.length() > 1 && word.charAt(1) == '$') {
                        break;
                    }
                    continue;
                }
                fp.fMatch = false;
                switch (word.charAt(0)) {
                    case 'e' -> {
                        if (!str_cmp(word, "end")) {
                            fp.fMatch = true;
                            if (social.name != null) {
                                social_table.add(social);
                            } else {
                                perror("social without name:" + fp.buildCurrentStateInfo());
                            }
                            social = new social_type();
                        }
                    }
                    case 'f' -> {
                        social.found_char = fp.SKEY("found_char", word, social.found_char);
                        social.found_victim = fp.SKEY("found_vict", word, social.found_victim);
                        social.found_novictim = fp.SKEY("found_notvict", word, social.found_novictim);
                    }
                    case 'n' -> {
                        social.name = fp.WKEY("name", word, social.name);
                        social.not_found_char = fp.SKEY("notfound_char", word, social.not_found_char);
                        social.noarg_char = fp.SKEY("noarg_char", word, social.noarg_char);
                        social.noarg_room = fp.SKEY("noarg_room", word, social.noarg_room);
                    }
                    case 'm' -> social.minPos = position_type.getIndexInTable((fp.WKEY("min_pos", word, null)));
                    case 's' -> {
                        social.self_char = fp.SKEY("self_char", word, social.self_char);
                        social.self_room = fp.SKEY("self_room", word, social.self_room);
                    }
                }
                if (!fp.fMatch) {
                    perror("Loading socials: unknown keyword:" + word + "Context:" + fp.buildCurrentStateInfo());
                }
            }
        } catch (IOException e) {
            perror("Error loading socials:" + e.getMessage());
            logError(e);
        }
    }

    /*
     * Snarf a mob section.  new style
     */

    static void load_mobiles(DikuTextFile fp) {
        MOB_INDEX_DATA pMobIndex;

        for (; ; ) {
            int vnum;
            char letter;
            int iHash;

            letter = fp.read_letter();
            if (letter != '#') {
                bug("Load_mobiles: # not found.");
                exit(1);
            }

            vnum = fp.read_number();
            if (vnum == 0) {
                break;
            }

            fBootDb = false;
            if (get_mob_index(vnum) != null) {
                bug("Load_mobiles: vnum %d duplicated.", vnum);
                exit(1);
            }
            fBootDb = true;

            pMobIndex = new MOB_INDEX_DATA();
            pMobIndex.vnum = vnum;
            pMobIndex.new_format = true;
            newmobs++;
            pMobIndex.player_name = fp.read_string();
            pMobIndex.short_descr = fp.read_string();
            pMobIndex.long_descr = capitalize(fp.read_string());
            pMobIndex.description = capitalize(fp.read_string());

            pMobIndex.race = Race.lookupRace(fp.read_string());


            pMobIndex.act = fp.read_flag() | ACT_IS_NPC | pMobIndex.race.act;

            pMobIndex.affected_by = fp.read_flag() | pMobIndex.race.aff;
            pMobIndex.practicer = 0;
            pMobIndex.affected_by = REMOVE_BIT(pMobIndex.affected_by, (C | D | E | F | G | Z | BIT_31));

            pMobIndex.pShop = null;
            pMobIndex.alignment = fp.read_number();
            pMobIndex.group = fp.read_number();

            pMobIndex.level = fp.read_number();
            pMobIndex.hitroll = fp.read_number();

            /* read hit dice */
            pMobIndex.hit[DICE_NUMBER] = fp.read_number();
            /* 'd'          */
            fp.read_letter();
            pMobIndex.hit[DICE_TYPE] = fp.read_number();
            /* '+'          */
            fp.read_letter();
            pMobIndex.hit[DICE_BONUS] = fp.read_number();

            /* read mana dice */
            pMobIndex.mana[DICE_NUMBER] = fp.read_number();
            fp.read_letter();
            pMobIndex.mana[DICE_TYPE] = fp.read_number();
            fp.read_letter();
            pMobIndex.mana[DICE_BONUS] = fp.read_number();

            /* read damage dice */
            pMobIndex.damage[DICE_NUMBER] = fp.read_number();
            fp.read_letter();
            pMobIndex.damage[DICE_TYPE] = fp.read_number();
            fp.read_letter();
            pMobIndex.damage[DICE_BONUS] = fp.read_number();
            pMobIndex.dam_type = attack_lookup(fp.read_word());

            /* read armor class */
            pMobIndex.ac[AC_PIERCE] = (fp.read_number() * 10);
            pMobIndex.ac[AC_BASH] = (fp.read_number() * 10);
            pMobIndex.ac[AC_SLASH] = (fp.read_number() * 10);
            pMobIndex.ac[AC_EXOTIC] = (fp.read_number() * 10);

            /* read flags and add in data from the race table */
            pMobIndex.off_flags = fp.read_flag() | pMobIndex.race.off;
            pMobIndex.imm_flags = fp.read_flag() | pMobIndex.race.imm;
            pMobIndex.res_flags = fp.read_flag() | pMobIndex.race.res;
            pMobIndex.vuln_flags = fp.read_flag() | pMobIndex.race.vuln;

            /* vital statistics */
            pMobIndex.start_pos = position_lookup(fp.read_word());
            pMobIndex.default_pos = position_lookup(fp.read_word());
            pMobIndex.sex = sex_lookup(fp.read_word());

            pMobIndex.wealth = fp.read_number();

            pMobIndex.form = fp.read_flag() | pMobIndex.race.form;
            pMobIndex.parts = fp.read_flag() | pMobIndex.race.parts;
            /* size */
            pMobIndex.size = size_lookup(fp.read_word());
            pMobIndex.material = fp.read_word();
            pMobIndex.mprogs = null;
            pMobIndex.progtypes = 0;

            for (; ; ) {
                letter = fp.read_letter();

                if (letter == 'F') {

                    int vector;

                    var word = fp.read_word();
                    vector = fp.read_flag();

                    if (!str_prefix(word, "act")) {
                        pMobIndex.act = REMOVE_BIT(pMobIndex.act, vector);
                    } else if (!str_prefix(word, "aff")) {
                        pMobIndex.affected_by = REMOVE_BIT(pMobIndex.affected_by, vector);
                    } else if (!str_prefix(word, "off")) {
                        pMobIndex.affected_by = REMOVE_BIT(pMobIndex.affected_by, vector);
                    } else if (!str_prefix(word, "imm")) {
                        pMobIndex.imm_flags = REMOVE_BIT(pMobIndex.imm_flags, vector);
                    } else if (!str_prefix(word, "res")) {
                        pMobIndex.res_flags = REMOVE_BIT(pMobIndex.res_flags, vector);
                    } else if (!str_prefix(word, "vul")) {
                        pMobIndex.vuln_flags = REMOVE_BIT(pMobIndex.vuln_flags, vector);
                    } else if (!str_prefix(word, "for")) {
                        pMobIndex.form = REMOVE_BIT(pMobIndex.form, vector);
                    } else if (!str_prefix(word, "par")) {
                        pMobIndex.parts = REMOVE_BIT(pMobIndex.parts, vector);
                    } else {
                        bug("Flag remove: flag not found.");
                        exit(1);
                    }
                } else {
                    fp.ungetc();
                    break;
                }
            }

            iHash = vnum % MAX_KEY_HASH;
            pMobIndex.next = mob_index_hash[iHash];
            mob_index_hash[iHash] = pMobIndex;
            top_mob_index++;
            kill_table[URANGE(0, pMobIndex.level, MAX_LEVEL - 1)].number++;
        }

    }

    /*
     * Snarf an obj section. new style
     */

    static void load_objects(DikuTextFile fp) {
        OBJ_INDEX_DATA pObjIndex;

        for (; ; ) {
            int vnum;
            char letter;
            int iHash;

            letter = fp.read_letter();
            if (letter != '#') {
                bug("Load_objects: # not found.");
                exit(1);
            }

            vnum = fp.read_number();
            if (vnum == 0) {
                break;
            }

            fBootDb = false;
            if (get_obj_index(vnum) != null) {
                bug("Load_objects: vnum %d duplicated.", vnum);
                exit(1);
            }
            fBootDb = true;

            pObjIndex = new OBJ_INDEX_DATA();
            pObjIndex.vnum = vnum;
            pObjIndex.new_format = true;
            pObjIndex.reset_num = 0;
            newobjs++;
            pObjIndex.name = fp.read_string();
            pObjIndex.short_descr = fp.read_string();
            pObjIndex.description = fp.read_string();
            pObjIndex.material = fp.read_string();

            pObjIndex.item_type = item_lookup(fp.read_word());
            pObjIndex.extra_flags = fp.read_flag();
            pObjIndex.wear_flags = fp.read_flag();
            switch (pObjIndex.item_type) {
                case ITEM_WEAPON -> {
                    pObjIndex.value[0] = weapon_type(fp.read_word());
                    pObjIndex.value[1] = fp.read_number();
                    pObjIndex.value[2] = fp.read_number();
                    pObjIndex.value[3] = attack_lookup(fp.read_word());
                    pObjIndex.value[4] = fp.read_flag();
                }
                case ITEM_CONTAINER -> {
                    pObjIndex.value[0] = fp.read_number();
                    pObjIndex.value[1] = fp.read_flag();
                    pObjIndex.value[2] = fp.read_number();
                    pObjIndex.value[3] = fp.read_number();
                    pObjIndex.value[4] = fp.read_number();
                }
                case ITEM_DRINK_CON, ITEM_FOUNTAIN -> {
                    pObjIndex.value[0] = fp.read_number();
                    pObjIndex.value[1] = fp.read_number();
                    pObjIndex.value[2] = liq_lookup(fp.read_word());
                    pObjIndex.value[3] = fp.read_number();
                    pObjIndex.value[4] = fp.read_number();
                }
                case ITEM_WAND, ITEM_STAFF -> {
                    pObjIndex.value[0] = fp.read_number();
                    pObjIndex.value[1] = fp.read_number();
                    pObjIndex.value[2] = fp.read_number();
                    pObjIndex.value[3] = skill_num_lookup(fp.read_word());
                    pObjIndex.value[4] = fp.read_number();
                }
                case ITEM_POTION, ITEM_PILL, ITEM_SCROLL -> {
                    pObjIndex.value[0] = fp.read_number();
                    pObjIndex.value[1] = skill_num_lookup(fp.read_word());
                    pObjIndex.value[2] = skill_num_lookup(fp.read_word());
                    pObjIndex.value[3] = skill_num_lookup(fp.read_word());
                    pObjIndex.value[4] = skill_num_lookup(fp.read_word());
                }
                default -> {
                    pObjIndex.value[0] = fp.read_flag();
                    pObjIndex.value[1] = fp.read_flag();
                    pObjIndex.value[2] = fp.read_flag();
                    pObjIndex.value[3] = fp.read_flag();
                    pObjIndex.value[4] = fp.read_flag();
                }
            }
            pObjIndex.level = fp.read_number();
            pObjIndex.weight = fp.read_number();
            pObjIndex.cost = fp.read_number();
            pObjIndex.progtypes = 0;
            pObjIndex.oprogs = null;
            pObjIndex.limit = -1;

            /* condition */
            letter = fp.read_letter();
            switch (letter) {
                case ('P') -> pObjIndex.condition = 100;
                case ('G') -> pObjIndex.condition = 90;
                case ('A') -> pObjIndex.condition = 75;
                case ('W') -> pObjIndex.condition = 50;
                case ('D') -> pObjIndex.condition = 25;
                case ('B') -> pObjIndex.condition = 10;
                case ('R') -> pObjIndex.condition = 0;
                default -> pObjIndex.condition = 100;
            }

            for (; ; ) {

                letter = fp.read_letter();

                if (letter == 'A') {
                    var paf = new AFFECT_DATA();
                    paf.where = TO_OBJECT;
                    paf.type = null;
                    paf.level = pObjIndex.level;
                    paf.duration = -1;
                    paf.location = fp.read_number();
                    paf.modifier = fp.read_number();
                    paf.bitvector = 0;
                    paf.next = pObjIndex.affected;
                    pObjIndex.affected = paf;
                    top_affect++;
                } else if (letter == 'F') {
                    var paf = new AFFECT_DATA();
                    letter = fp.read_letter();
                    switch (letter) {
                        case 'A' -> paf.where = TO_AFFECTS;
                        case 'I' -> paf.where = TO_IMMUNE;
                        case 'R' -> paf.where = TO_RESIST;
                        case 'V' -> paf.where = TO_VULN;
                        case 'D' -> paf.where = TO_AFFECTS;
                        default -> {
                            bug("Load_objects: Bad where on flag set.");
                            exit(1);
                        }
                    }
                    paf.type = null;
                    paf.level = pObjIndex.level;
                    paf.duration = -1;
                    paf.location = fp.read_number();
                    paf.modifier = fp.read_number();
                    paf.bitvector = fp.read_flag();
                    paf.next = pObjIndex.affected;
                    pObjIndex.affected = paf;
                    top_affect++;
                } else if (letter == 'E') {
                    var ed = new EXTRA_DESCR_DATA();
                    ed.keyword = fp.read_string();
                    ed.description = fp.read_string();
                    ed.next = pObjIndex.extra_descr;
                    pObjIndex.extra_descr = ed;
                    top_ed++;
                } else {
                    fp.ungetc();
                    break;
                }
            }

            iHash = vnum % MAX_KEY_HASH;
            pObjIndex.next = obj_index_hash[iHash];
            obj_index_hash[iHash] = pObjIndex;
            top_obj_index++;
        }
    }

    /*
     * Snarf a mprog section
     */

    static void load_omprogs(DikuTextFile fp) throws NoSuchMethodException {
        String progtype, progname;
        for (; ; ) {
            MOB_INDEX_DATA pMobIndex;
            OBJ_INDEX_DATA pObjIndex;
            char letter;


            switch (letter = fp.read_letter()) {
                default:
                    bug("Load_omprogs: letter '%c' not *IMS.", letter);
                    exit(1);

                case 'S':
                    return;

                case '*':
                    break;

                case 'O':
                    pObjIndex = get_obj_index(fp.read_number());
                    if (pObjIndex.oprogs == null) {
                        pObjIndex.oprogs = new OPROG_DATA();
                    }

                    progtype = fp.read_word();
                    progname = fp.read_word();
                    oprog_set(pObjIndex, progtype, progname);
                    break;

                case 'M':
                    pMobIndex = get_mob_index(fp.read_number());
                    if (pMobIndex.mprogs == null) {
                        pMobIndex.mprogs = new MPROG_DATA();
                    }

                    progtype = fp.read_word();
                    progname = fp.read_word();
                    mprog_set(pMobIndex, progtype, progname);
                    break;
            }

            fp.read_to_eol();
        }
    }

}
