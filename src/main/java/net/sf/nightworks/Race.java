package net.sf.nightworks;

/*
 * ************************************************************************ *
 *   Nightworks MUD is copyright 2006 Mikhail Fursov                        *
 *       Mikhail Fursov {fmike@mail.ru}                                     *
 * ************************************************************************ *
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static net.sf.nightworks.Nightworks.MAX_STATS;

/**
 * Last mofified:           $Date$
 * Revision of last commit: $Revision$
 */

class Race {

    private static final HashMap<String, Race> racesMap = new HashMap<String, Race>();
    private static final ArrayList<Race> races = new ArrayList<Race>();

    // set of predefined races.
    // these races are the same as from configuration file
    // except 2 difference:
    // 1. Predefined race can be referenced from code by a constant
    // 2. Predefined race must be found in configuration file during startup
    static final Race
            HUMAN = new Race("human"),
            ELF = new Race("elf"),
            HALF_ELF = new Race("half-elf"),
            DARK_ELF = new Race("dark-elf"),
            ROCKSEER = new Race("rockseer"),
            DWARF = new Race("dwarf"),
            SVIRFNEBLI = new Race("svirfnebli"),
            DUERGAR = new Race("duergar"),
            ARIAL = new Race("arial"),
            GNOME = new Race("gnome"),
            STORM_GIANT = new Race("storm giant"),
            CLOUD_GIANT = new Race("cloud giant"),
            FIRE_GIANT = new Race("fire giant"),
            FROST_GIANT = new Race("frost giant"),
            FELAR = new Race("felar"),
            GITHYANKI = new Race("githyanki"),
            SATYR = new Race("satyr"),
            TROLL = new Race("troll");

    //TODO: add getters and make fields private -> this is Java :)
    final String name;           /* call name of the race */
    String fileName;
    long act;            /* act bits for the race */
    long aff;            /* aff bits for the race */
    int off;            /* off bits for the race */
    int imm;            /* imm bits for the race */
    int res;            /* res bits for the race */
    int vuln;           /* vuln bits for the race */
    int form;           /* default form flag for the race */
    int parts;          /* default parts for the race */
    PCRace pcRace;

    private Race(String raceName) {
        assert (!racesMap.containsKey(raceName));
        this.name = raceName;
        racesMap.put(name, this);
        races.add(this);
    }

    private boolean validate() {
        return name.length() > 0;
    }

    static Race createRace(String raceName) {
        Race race = lookupRace(raceName, false);
        if (race == null) {
            race = new Race(raceName);
        }
        return race;
    }

    static Race lookupRace(String raceName) {
        return lookupRace(raceName, true);
    }

    static Race lookupRace(String raceName, boolean throwExceptionIfNotFound) {
        Race res = racesMap.get(raceName);
        if (res == null && throwExceptionIfNotFound) {
            throw new RuntimeException("Race not found:" + raceName);
        }
        return res;
    }

    static Race lookupRaceByPrefix(String raceNamePrefix) {
        Race r = lookupRace(raceNamePrefix, false);
        if (r == null) {
            for (int i = 0; i < races.size(); i++) {
                Race race = races.get(i);
                if (race.name.startsWith(raceNamePrefix)) {
                    r = race;
                    break;
                }
            }
        }
        return r;
    }


    static ArrayList<Race> listRaces() {
        return races;
    }

    static void doValidationCheck() {
        for (Race race : races) {
            if (!race.validate()) {
                throw new RuntimeException("Misconfigired race: " + race.name);
            }
        }
    }

    public static int getNumberOfPCRaces() {
        int sum = 0;
        for (Race race : races) {
            if (race.pcRace != null) {
                sum++;
            }
        }
        return sum;
    }
}

class PCRace {
    Race race;
    String who_name;
    int points;       /* cost in exp of the race */
    private Map<Clazz, RaceToClassModifier> rclass = new HashMap<Clazz, RaceToClassModifier>();/* race affect to class*/
    Skill[] skills = new Skill[0];  /* bonus skills for the race */
    int[] stats = new int[MAX_STATS];   /* starting stats   */
    int[] max_stats = new int[MAX_STATS];  /* maximum stats    */
    int size;         /* aff bits for the race*/
    int hp_bonus;     /* Initial hp bonus     */
    int mana_bonus;   /* Initial mana bonus   */
    int prac_bonus;   /* Initial practice bonus */
    int align;        /* Alignment        */
    int language;     /* language     */

    private static final RaceToClassModifier defaultMod = new RaceToClassModifier(null, -1);

    RaceToClassModifier getClassModifier(Clazz clazz) {
        RaceToClassModifier mod = rclass.get(clazz);
        if (mod == null) {
            return defaultMod;
        }
        return mod;
    }

    public void addClassModifier(Clazz clazz, RaceToClassModifier mod) {
        assert (!rclass.containsKey(clazz));
        rclass.put(clazz, mod);
    }
}

class RaceToClassModifier {
    final Clazz clazz;
    final int expMult;

    RaceToClassModifier(Clazz clazz, int expMult) {
        this.clazz = clazz;
        this.expMult = expMult;
    }
}
