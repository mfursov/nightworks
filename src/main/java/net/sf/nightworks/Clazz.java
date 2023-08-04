package net.sf.nightworks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static net.sf.nightworks.Nightworks.ETHOS_ANY;
import static net.sf.nightworks.Nightworks.MAX_LEVEL;
import static net.sf.nightworks.Nightworks.MAX_STATS;
import static net.sf.nightworks.Nightworks.SEX_FEMALE;
import static net.sf.nightworks.Nightworks.SEX_MALE;

public class Clazz {

    private static int idGen;
    private static final Map<String, Clazz> clazzByName = new HashMap<>();
    private static final ArrayList<Clazz> classes = new ArrayList<>(10);

    public static final Clazz
            MOB_CLASS = new Clazz("mob"),
            NECROMANCER = new Clazz("necromancer"),
            NINJA = new Clazz("ninja"),
            SAMURAI = new Clazz("samurai"),
            THIEF = new Clazz("thief"),
            VAMPIRE = new Clazz("vampire"),
            WARRIOR = new Clazz("warrior");


    final String name;
    String who_name;    /* Three-letter name for 'who'  */
    int attr_prime;     /* Prime attribute      */
    int weapon;         /* First weapon         */
    ArrayList<Integer> guildVnums = new ArrayList<>();   /* Vnum of guild rooms      */
    int skill_adept;        /* Maximum skill level      */
    int thac0_00;       /* Thac0 for level  0       */
    int thac0_32;       /* Thac0 for level 32       */
    int hp_rate;        /* hp rate gained on leveling   */
    int mana_rate;      /* mana rate gained on leveling */
    boolean fMana;          /* Class gains mana on level    */
    int points;                 /* Cost in exp of class         */
    int[] stats = new int[MAX_STATS];       /* Stat modifiers       */
    int align;                  /* Alignment            */
    int sex = SEX_FEMALE | SEX_MALE;
    int ethos = ETHOS_ANY;
    final int id;
    final ArrayList<Pose> poses = new ArrayList<>();

    String[] femaleTitles = new String[MAX_LEVEL + 1];
    String[] maleTitles = new String[MAX_LEVEL + 1];

    Clazz(String className) {
        assert (!clazzByName.containsKey(className));
        id = idGen++;
        this.name = className;
        clazzByName.put(name, this);
        classes.add(this);
    }

    String getTitle(int level, int sex) {
        if (sex == SEX_FEMALE) {
            return femaleTitles[level];
        }
        return maleTitles[level];
    }

    static void doValidationCheck() {
        for (Clazz clazz : classes) {
            if (!clazz.validate()) {
                throw new RuntimeException("Misconfigired class:" + clazz.name);
            }
        }
    }

    private boolean validate() {
        return !name.isEmpty()
                && !who_name.isEmpty()
                && weapon != 0
                && !guildVnums.isEmpty();
    }

    static Clazz lookupClass(String className, boolean throwExceptionIfNotFound) {
        Clazz res = clazzByName.get(className);
        if (throwExceptionIfNotFound && res == null) {
            throw new RuntimeException("Clazz not found:" + className);
        }
        return res;
    }

    static Clazz lookupClass(String className) {
        return lookupClass(className, true);
    }

    static Clazz lookupClassByPrefix(String classNamePrefix) {
        Clazz cl = lookupClass(classNamePrefix, false);
        if (cl == null) {
            for (Clazz clazz : classes) {
                if (clazz.name.startsWith(classNamePrefix)) {
                    cl = clazz;
                    break;
                }
            }
        }
        return cl;
    }

    static ArrayList<Clazz> getClasses() {
        return classes;
    }
}

class Pose {
    String message_to_char;
    String message_to_room;
}
