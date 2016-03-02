package net.sf.nightworks;

import net.sf.nightworks.util.TextBuffer;

import java.lang.reflect.Method;

import static net.sf.nightworks.ActComm.do_cb;
import static net.sf.nightworks.ActComm.do_say;
import static net.sf.nightworks.ActComm.do_yell;
import static net.sf.nightworks.ActHera.find_path;
import static net.sf.nightworks.ActMove.do_close;
import static net.sf.nightworks.ActMove.do_lock;
import static net.sf.nightworks.ActMove.do_open;
import static net.sf.nightworks.ActMove.do_sleep;
import static net.sf.nightworks.ActMove.do_unlock;
import static net.sf.nightworks.ActMove.move_char;
import static net.sf.nightworks.ActObj.do_drop;
import static net.sf.nightworks.ActObj.do_get;
import static net.sf.nightworks.ActObj.do_give;
import static net.sf.nightworks.ActWiz.do_load;
import static net.sf.nightworks.ActWiz.do_smite;
import static net.sf.nightworks.Comm.act;
import static net.sf.nightworks.Const.hometown_table;
import static net.sf.nightworks.Const.religion_table;
import static net.sf.nightworks.DB.bug;
import static net.sf.nightworks.DB.create_object;
import static net.sf.nightworks.DB.get_obj_index;
import static net.sf.nightworks.DB.number_bits;
import static net.sf.nightworks.DB.number_percent;
import static net.sf.nightworks.DB.number_range;
import static net.sf.nightworks.Fight.do_murder;
import static net.sf.nightworks.Fight.do_slay;
import static net.sf.nightworks.Handler.can_see;
import static net.sf.nightworks.Handler.extract_obj;
import static net.sf.nightworks.Handler.get_obj_carry;
import static net.sf.nightworks.Handler.is_name;
import static net.sf.nightworks.Handler.obj_from_char;
import static net.sf.nightworks.Handler.obj_to_char;
import static net.sf.nightworks.Healer.heal_battle;
import static net.sf.nightworks.Interp.interpret;
import static net.sf.nightworks.Magic.say_spell;
import static net.sf.nightworks.MartialArt.do_rescue;
import static net.sf.nightworks.Nightworks.CABAL_BATTLE;
import static net.sf.nightworks.Nightworks.CABAL_CHAOS;
import static net.sf.nightworks.Nightworks.CABAL_HUNTER;
import static net.sf.nightworks.Nightworks.CABAL_INVADER;
import static net.sf.nightworks.Nightworks.CABAL_KNIGHT;
import static net.sf.nightworks.Nightworks.CABAL_LIONS;
import static net.sf.nightworks.Nightworks.CABAL_RULER;
import static net.sf.nightworks.Nightworks.CABAL_SHALAFI;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.DICE_BONUS;
import static net.sf.nightworks.Nightworks.DICE_NUMBER;
import static net.sf.nightworks.Nightworks.DICE_TYPE;
import static net.sf.nightworks.Nightworks.ETHOS_CHAOTIC;
import static net.sf.nightworks.Nightworks.ETHOS_LAWFUL;
import static net.sf.nightworks.Nightworks.ETHOS_NEUTRAL;
import static net.sf.nightworks.Nightworks.EXTRA_DESCR_DATA;
import static net.sf.nightworks.Nightworks.IS_EVIL;
import static net.sf.nightworks.Nightworks.IS_GOOD;
import static net.sf.nightworks.Nightworks.IS_IMMORTAL;
import static net.sf.nightworks.Nightworks.IS_NEUTRAL;
import static net.sf.nightworks.Nightworks.IS_NPC;
import static net.sf.nightworks.Nightworks.IS_SET;
import static net.sf.nightworks.Nightworks.MAX_RELIGION;
import static net.sf.nightworks.Nightworks.MAX_STATS;
import static net.sf.nightworks.Nightworks.MOB_INDEX_DATA;
import static net.sf.nightworks.Nightworks.MPROG_AREA;
import static net.sf.nightworks.Nightworks.MPROG_BRIBE;
import static net.sf.nightworks.Nightworks.MPROG_DEATH;
import static net.sf.nightworks.Nightworks.MPROG_ENTRY;
import static net.sf.nightworks.Nightworks.MPROG_FIGHT;
import static net.sf.nightworks.Nightworks.MPROG_FUN_AREA;
import static net.sf.nightworks.Nightworks.MPROG_FUN_BRIBE;
import static net.sf.nightworks.Nightworks.MPROG_FUN_DEATH;
import static net.sf.nightworks.Nightworks.MPROG_FUN_ENTRY;
import static net.sf.nightworks.Nightworks.MPROG_FUN_FIGHT;
import static net.sf.nightworks.Nightworks.MPROG_FUN_GIVE;
import static net.sf.nightworks.Nightworks.MPROG_FUN_GREET;
import static net.sf.nightworks.Nightworks.MPROG_FUN_SPEECH;
import static net.sf.nightworks.Nightworks.MPROG_GIVE;
import static net.sf.nightworks.Nightworks.MPROG_GREET;
import static net.sf.nightworks.Nightworks.MPROG_SPEECH;
import static net.sf.nightworks.Nightworks.OBJ_DATA;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_EYED_SWORD;
import static net.sf.nightworks.Nightworks.OFF_AREA_ATTACK;
import static net.sf.nightworks.Nightworks.PERS;
import static net.sf.nightworks.Nightworks.POS_SLEEPING;
import static net.sf.nightworks.Nightworks.QUEST_EYE;
import static net.sf.nightworks.Nightworks.RELIGION_AHRUMAZDA;
import static net.sf.nightworks.Nightworks.RELIGION_APOLLON;
import static net.sf.nightworks.Nightworks.RELIGION_DEIMOS;
import static net.sf.nightworks.Nightworks.RELIGION_EHRUMEN;
import static net.sf.nightworks.Nightworks.RELIGION_MARS;
import static net.sf.nightworks.Nightworks.RELIGION_ODIN;
import static net.sf.nightworks.Nightworks.RELIGION_PHOBOS;
import static net.sf.nightworks.Nightworks.RELIGION_SIEBELE;
import static net.sf.nightworks.Nightworks.RELIGION_ZEUS;
import static net.sf.nightworks.Nightworks.SET_BIT;
import static net.sf.nightworks.Nightworks.TARGET_CHAR;
import static net.sf.nightworks.Nightworks.TO_CHAR;
import static net.sf.nightworks.Nightworks.TO_NOTVICT;
import static net.sf.nightworks.Nightworks.TO_ROOM;
import static net.sf.nightworks.Nightworks.TO_VICT;
import static net.sf.nightworks.Nightworks.char_list;
import static net.sf.nightworks.Nightworks.current_time;
import static net.sf.nightworks.Nightworks.exit;
import static net.sf.nightworks.Nightworks.object_list;
import static net.sf.nightworks.Nightworks.sprintf;
import static net.sf.nightworks.Skill.lookupSkill;
import static net.sf.nightworks.util.TextUtils.one_argument;
import static net.sf.nightworks.util.TextUtils.str_cmp;
import static net.sf.nightworks.util.TextUtils.str_prefix;

class MobProg {
    public static final int GIVE_HELP_RELIGION = 16;
    public static final int RELIG_CHOSEN = 17;


    static void mprog_set(MOB_INDEX_DATA mobindex, String progtype, String name) {
        boolean found = true;
        try {
            if (!str_cmp(progtype, "bribe_prog")) {
                mobindex.mprogs.bribe_prog = create_bribe_prog(name);
                mobindex.progtypes = SET_BIT(mobindex.progtypes, MPROG_BRIBE);
            } else if (!str_cmp(progtype, "entry_prog")) {
                mobindex.mprogs.entry_prog = create_entry_prog(name);
                mobindex.progtypes = SET_BIT(mobindex.progtypes, MPROG_ENTRY);
            } else if (!str_cmp(progtype, "greet_prog")) {
                mobindex.mprogs.greet_prog = create_greet_prog(name);
                mobindex.progtypes = SET_BIT(mobindex.progtypes, MPROG_GREET);
            } else if (!str_cmp(progtype, "fight_prog")) {
                mobindex.mprogs.fight_prog = create_fight_prog(name);
                mobindex.progtypes = SET_BIT(mobindex.progtypes, MPROG_FIGHT);
            } else if (!str_cmp(progtype, "death_prog")) /* returning true prevents death */ {
                mobindex.mprogs.death_prog = create_death_prog(name);
                mobindex.progtypes = SET_BIT(mobindex.progtypes, MPROG_DEATH);
            } else if (!str_cmp(progtype, "area_prog")) {
                mobindex.mprogs.area_prog = create_area_prog(name);
                mobindex.progtypes = SET_BIT(mobindex.progtypes, MPROG_AREA);
            } else if (!str_cmp(progtype, "speech_prog")) {
                mobindex.mprogs.speech_prog = create_speech_prog(name);
                mobindex.progtypes = SET_BIT(mobindex.progtypes, MPROG_SPEECH);
            } else if (!str_cmp(progtype, "give_prog")) {
                mobindex.mprogs.give_prog = create_give_prog(name);
                mobindex.progtypes = SET_BIT(mobindex.progtypes, MPROG_GIVE);
            } else {
                found = false;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } finally {
            if (!found) {
                bug("Load_mprogs: 'M': invalid program type for vnum %d", mobindex.vnum);
                exit(1);
            }
        }
    }

    private static Method resolveMethod(String name, Class... params) throws NoSuchMethodException {
        return MobProg.class.getDeclaredMethod(name, params);
    }

    private static MPROG_FUN_BRIBE create_bribe_prog(String name) throws NoSuchMethodException {
        return new MPROG_FUN_BRIBE() {
            final Method m = resolveMethod(name, CHAR_DATA.class, CHAR_DATA.class, Integer.class);

            public void run(CHAR_DATA mob, CHAR_DATA ch, int amount) {
                try {
                    m.invoke(null, mob, ch, amount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private static MPROG_FUN_ENTRY create_entry_prog(final String name) throws NoSuchMethodException {
        return new MPROG_FUN_ENTRY() {
            final Method m = resolveMethod(name, String.class);

            public void run(CHAR_DATA mob) {
                try {
                    m.invoke(null, mob);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private static MPROG_FUN_GREET create_greet_prog(final String name) throws NoSuchMethodException {
        return new MPROG_FUN_GREET() {
            final Method m = resolveMethod(name, CHAR_DATA.class, CHAR_DATA.class);

            public void run(CHAR_DATA mob, CHAR_DATA ch) {
                try {
                    m.invoke(null, mob, ch);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private static MPROG_FUN_FIGHT create_fight_prog(final String name) throws NoSuchMethodException {
        return new MPROG_FUN_FIGHT() {
            final Method m = resolveMethod(name, CHAR_DATA.class, CHAR_DATA.class);

            public void run(CHAR_DATA mob, CHAR_DATA victim) {
                try {
                    m.invoke(null, mob, victim);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private static MPROG_FUN_DEATH create_death_prog(final String name) throws NoSuchMethodException {
        return new MPROG_FUN_DEATH() {
            final Method m = resolveMethod(name, CHAR_DATA.class);

            public boolean run(CHAR_DATA mob) {
                try {
                    return (Boolean) m.invoke(null, mob);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };
    }

    private static MPROG_FUN_AREA create_area_prog(final String name) throws NoSuchMethodException {
        return new MPROG_FUN_AREA() {
            final Method m = resolveMethod(name, CHAR_DATA.class);

            public void run(CHAR_DATA mob) {
                try {
                    m.invoke(null, mob);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private static MPROG_FUN_SPEECH create_speech_prog(final String name) throws NoSuchMethodException {
        return new MPROG_FUN_SPEECH() {
            final Method m = resolveMethod(name, CHAR_DATA.class, CHAR_DATA.class, String.class);

            public void run(CHAR_DATA mob, CHAR_DATA ch, String speech) {
                try {
                    m.invoke(null, mob, ch, speech);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private static MPROG_FUN_GIVE create_give_prog(final String name) throws NoSuchMethodException {
        return new MPROG_FUN_GIVE() {
            final Method m = resolveMethod(name, CHAR_DATA.class, CHAR_DATA.class, OBJ_DATA.class);

            public void run(CHAR_DATA mob, CHAR_DATA ch, OBJ_DATA obj) {
                try {
                    m.invoke(null, mob, ch, obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    static void bribe_prog_cityguard(CHAR_DATA mob, CHAR_DATA ch, Integer amount) {
        if (amount < 100) {
            do_say(mob, "You cheapskate!!!");
            do_murder(mob, ch.name);
        } else if (amount >= 5000) {
            interpret(mob, "smile", false);
            do_sleep(mob, "");
        } else {
            do_say(mob, "Trying to bribe me, eh? It'll cost ya more than that.");
        }
    }

    static void greet_prog_shalafi(CHAR_DATA mob, CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }

        mob.cabal = CABAL_SHALAFI;
        mob.off_flags = SET_BIT(mob.off_flags, OFF_AREA_ATTACK);

        if (ch.cabal == CABAL_SHALAFI) {
            do_say(mob, "Greetings, wise one.");
            return;
        }
        if (ch.last_death_time != -1 && current_time - ch.last_death_time < 600) {
            do_say(mob, "Ghosts are not allowed in this place.");
            do_slay(mob, ch.name);
            return;
        }

        if (IS_IMMORTAL(ch)) {
            return;
        }

        do_cb(mob, "Intruder! Intruder!");
        do_say(mob, "You should never disturb my cabal!");
    }

    static void greet_prog_invader(CHAR_DATA mob, CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }

        mob.cabal = CABAL_INVADER;
        mob.off_flags = SET_BIT(mob.off_flags, OFF_AREA_ATTACK);

        if (ch.cabal == CABAL_INVADER) {
            do_say(mob, "Greetings, dark one.");
            return;
        }
        if (ch.last_death_time != -1 && current_time - ch.last_death_time < 600) {
            do_say(mob, "Ghosts are not allowed in this place.");
            do_slay(mob, ch.name);
            return;
        }

        if (IS_IMMORTAL(ch)) {
            return;
        }

        do_cb(mob, "Intruder! Intruder!");
        if (!IS_NPC(ch)) {
            do_say(mob, "You should never disturb my cabal!");
        }
    }

    static void greet_prog_ruler_pre(CHAR_DATA mob, CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }

        if (ch.cabal == CABAL_RULER) {
            TextBuffer buf = new TextBuffer();
            buf.sprintf("bow %s", ch.name);
            interpret(mob, buf.toString(), false);
            return;
        }

        do_say(mob, "Do not go further and leave the square.");
        do_say(mob, "This place is private.");
    }

    static void greet_prog_ruler(CHAR_DATA mob, CHAR_DATA ch) {

        if (IS_NPC(ch)) {
            return;
        }

        mob.cabal = CABAL_RULER;
        mob.off_flags = SET_BIT(mob.off_flags, OFF_AREA_ATTACK);

        if (ch.cabal == CABAL_RULER) {
            TextBuffer buf = new TextBuffer();
            buf.sprintf("bow %s", ch.name);
            interpret(mob, buf.toString(), false);
            return;
        }
        if (ch.last_death_time != -1 && current_time - ch.last_death_time < 600) {
            do_say(mob, "Ghosts are not allowed in this place.");
            do_slay(mob, ch.name);
            return;
        }

        if (IS_IMMORTAL(ch)) {
            return;
        }

        do_cb(mob, "Intruder! Intruder!");
        do_say(mob, "You should never disturb my cabal!");
    }

    static void greet_prog_chaos(CHAR_DATA mob, CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }

        mob.cabal = CABAL_CHAOS;
        mob.off_flags = SET_BIT(mob.off_flags, OFF_AREA_ATTACK);

        if (ch.cabal == CABAL_CHAOS) {
            do_say(mob, "Greetings, chaotic one.");
            return;
        }
        if (ch.last_death_time != -1 && current_time - ch.last_death_time < 600) {
            do_say(mob, "Ghosts are not allowed in this place.");
            do_slay(mob, ch.name);
            return;
        }

        if (IS_IMMORTAL(ch)) {
            return;
        }

        do_cb(mob, "Intruder! Intruder!");
        do_say(mob, "You should never disturb my cabal!");
    }

    static void greet_prog_battle(CHAR_DATA mob, CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }

        mob.cabal = CABAL_BATTLE;
        mob.off_flags = SET_BIT(mob.off_flags, OFF_AREA_ATTACK);

        if (ch.cabal == CABAL_BATTLE) {
            do_say(mob, "Welcome, great warrior.");
            return;
        }
        if (ch.last_death_time != -1 && current_time - ch.last_death_time < 600) {
            do_say(mob, "Ghosts are not allowed in this place.");
            do_slay(mob, ch.name);
            return;
        }

        if (IS_IMMORTAL(ch)) {
            return;
        }

        do_cb(mob, "Intruder! Intruder!");
        do_say(mob, "You should never disturb my cabal!");
    }


    static void give_prog_keeper(CHAR_DATA mob, CHAR_DATA ch, OBJ_DATA obj) {

        if (obj.pIndexData.vnum == 90) {
            do_say(mob, "Finally, the dress I sent for!");
            act("$n tucks the dress away under her desk.", mob, null, null, TO_ROOM);
            obj_from_char(obj);
            extract_obj(obj);
            if (get_obj_carry(ch, "rug") != null) {
                do_say(mob, "I suppose you'll want to see the FireFlash now");
                do_say(mob, "Be careful, she's been in a nasty mood.");
                do_unlock(mob, "door");
                do_open(mob, "door");
            } else {
                do_say(mob, "It doesn't look like you have any business with the FireFlash.");
                do_say(mob, "I suggest you leave and find some before coming here again.");
            }
        } else {
            do_give(mob, obj.name + " " + ch.name);
            do_say(mob, "Why do i need this?.");
        }
    }


    static void speech_prog_keeper(CHAR_DATA mob, CHAR_DATA ch, String speech) {
        OBJ_DATA obj;

        if (!str_cmp(speech, "keeper") && !IS_NPC(ch)) {
            obj = create_object(get_obj_index(90), 0);
            obj.name = "keeper dress";
            act("$n fashions a white gown out of the bolt of silk.", mob, null, null, TO_ROOM);
            act("You make a white gown for the Keeper.", mob, null, null, TO_CHAR);
            do_say(mob, "Here is the dress for the keeper.");
            obj_to_char(obj, ch);
        }
    }

    static void greet_prog_fireflash(CHAR_DATA mob, CHAR_DATA ch) {

        if (!can_see(mob, ch) || IS_NPC(ch) || IS_IMMORTAL(ch)) {
            return;
        }

        if (get_obj_carry(ch, "rug") == null) {
            do_say(mob, "I don't want to see that worthless rug anywhere near me.");
            do_say(mob, "Why don't you give it to that silly Green sister from Tear.");
            do_unlock(mob, "box");
            do_open(mob, "box");
            do_get(mob, "papers box");
            do_say(mob, "These papers might help you.");
            act("$n sneers at you.", mob, null, ch, TO_VICT);
            act("You sneer at $N.", mob, null, ch, TO_CHAR);
            act("$n sneers at $N.", mob, null, ch, TO_NOTVICT);
            TextBuffer buf = new TextBuffer();
            buf.sprintf("papers %s", ch.name);
            do_give(mob, buf.toString());
            do_close(mob, "box");
            do_lock(mob, "box");
        }
    }

    static void give_prog_fireflash(CHAR_DATA mob, CHAR_DATA ch, OBJ_DATA obj) {
        if (!can_see(mob, ch)) {
            do_say(mob, "Is someone there?");
        } else if (IS_NPC(ch)) {
            do_say(mob, "How strange, an animal delivering something.");
        } else if (obj.pIndexData.vnum != 91) {
            do_say(mob, "How interesting!  ...what's it for?");
            interpret(mob, "giggle", false);
            do_give(mob, obj.name + " " + ch.name);
        } else {
            do_say(mob, "What a wonderful rug!  Let's see....where shall I put it?");
            act("$n starts wandering about the room, mumbling to $mself.", mob,
                    null, null, TO_ROOM);
            act("$n sticks $s hands in $s pockets.", mob, null, null, TO_ROOM);
            do_load(mob, "obj 2438");
            do_say(mob, "What's this?  A key?  Here, you can have it.");
            do_give(mob, "xxx " + ch.name);
            act("$n absently pushes the rug under a chair.", mob, null, null, TO_ROOM);
            obj_from_char(obj);
            extract_obj(obj);
        }
    }

    static void greet_prog_solamnia(CHAR_DATA mob, CHAR_DATA ch) {

        if (!can_see(mob, ch) || IS_NPC(ch) || IS_IMMORTAL(ch)) {
            return;
        }

        if (get_obj_carry(ch, "xxx") != null) {
            do_say(mob, "I think you bring something for me....");
            interpret(mob, "smile", false);
        }
    }

    static void give_prog_solamnia(CHAR_DATA mob, CHAR_DATA ch, OBJ_DATA obj)

    {
        OBJ_DATA kassandra;

        if (obj.pIndexData.vnum == 2438) {
            do_say(mob, "Here is your reward!");
            kassandra = create_object(get_obj_index(89), 0);
            kassandra.timer = 500;
            obj_to_char(kassandra, mob);
            TextBuffer buf = new TextBuffer();
            buf.sprintf("kassandra %s", ch.name);
            do_give(mob, buf.toString());
            do_say(mob, "This stone has some special powers, use it well.");
            obj_from_char(obj);
            extract_obj(obj);
        }
    }

    static boolean death_prog_stalker(CHAR_DATA mob) {
        mob.cabal = CABAL_RULER;
        TextBuffer buf = new TextBuffer();
        buf.sprintf("I have failed trying to kill %s, I gasp my last breath.", mob.last_fought.name);
        do_cb(mob, buf.toString());
        return false;
    }

    static void greet_prog_knight(CHAR_DATA mob, CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }

        mob.cabal = CABAL_KNIGHT;
        mob.off_flags = SET_BIT(mob.off_flags, OFF_AREA_ATTACK);

        if (ch.cabal == CABAL_KNIGHT) {
            do_say(mob, "Welcome, honorable one.");
            return;
        }
        if (ch.last_death_time != -1 && current_time - ch.last_death_time < 600) {
            do_say(mob, "Ghosts are not allowed in this place.");
            do_slay(mob, ch.name);
            return;
        }

        if (IS_IMMORTAL(ch)) {
            return;
        }

        do_cb(mob, "Intruder! Intruder!");
        do_say(mob, "You should never disturb my cabal!");
    }

    static void give_prog_dressmaker(CHAR_DATA mob, CHAR_DATA ch, OBJ_DATA obj) {
        if (IS_NPC(ch)) {
            return;
        }
        if (!can_see(mob, ch)) {
            do_say(mob, "Where did this come from?");
            return;
        }
        if (obj.pIndexData.vnum != 2436) {
            do_say(mob, "I can't do anything with this, I need silk.");
            do_drop(mob, obj.name);
        } else {
            do_say(mob, "Who am I making this dress for?");
            obj_from_char(obj);
            extract_obj(obj);
        }
    }

    static void greet_prog_keeper(CHAR_DATA mob, CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }
        if (!can_see(mob, ch)) {
            return;
        }
        do_say(mob, "What business do you have here?  Is it that dress I ordered?");
    }

    static void speech_prog_templeman(CHAR_DATA mob, CHAR_DATA ch, String speech) {
        int chosen = 0;
        boolean correct = true;

        if (!str_cmp(speech, "religion")) {
            mob.status = GIVE_HELP_RELIGION;
        } else if ((chosen = lookup_religion_leader(speech)) != 0) {
            mob.status = RELIG_CHOSEN;
        } else {
            return;
        }
        if (mob.status == RELIG_CHOSEN) {
            if ((ch.religion > 0) && (ch.religion < MAX_RELIGION)) {
                TextBuffer buf = new TextBuffer();
                buf.sprintf("You are already in the way of %s", religion_table[ch.religion].leader);
                do_say(mob, buf.toString());
                return;
            }
            switch (chosen) {
                case RELIGION_APOLLON:
                    if (!IS_GOOD(ch) && ch.ethos != ETHOS_LAWFUL) {
                        correct = false;
                    }
                    break;
                case RELIGION_ZEUS:
                    if (!IS_GOOD(ch) && ch.ethos != ETHOS_NEUTRAL) {
                        correct = false;
                    }
                    break;
                case RELIGION_SIEBELE:
                    if (!IS_NEUTRAL(ch) && ch.ethos != ETHOS_NEUTRAL) {
                        correct = false;
                    }
                    break;
                case RELIGION_EHRUMEN:
                    if (!IS_GOOD(ch) && ch.ethos != ETHOS_CHAOTIC) {
                        correct = false;
                    }
                    break;
                case RELIGION_AHRUMAZDA:
                    if (!IS_EVIL(ch) && ch.ethos != ETHOS_CHAOTIC) {
                        correct = false;
                    }
                    break;
                case RELIGION_DEIMOS:
                    if (!IS_EVIL(ch) && ch.ethos != ETHOS_LAWFUL) {
                        correct = false;
                    }
                    break;
                case RELIGION_PHOBOS:
                    if (!IS_EVIL(ch) && ch.ethos != ETHOS_NEUTRAL) {
                        correct = false;
                    }
                    break;
                case RELIGION_ODIN:
                    if (!IS_NEUTRAL(ch) && ch.ethos != ETHOS_LAWFUL) {
                        correct = false;
                    }
                    break;
                case RELIGION_MARS:
                    if (!IS_NEUTRAL(ch) && ch.ethos != ETHOS_CHAOTIC) {
                        correct = false;
                    }
                    break;
            }

            if (!correct) {
                do_say(mob, "That religion doesn't match your ethos and alignment.");
                return;
            }

            ch.religion = chosen;
            TextBuffer buf = new TextBuffer();
            buf.sprintf("From now on and forever, you are in the way of %s", religion_table[ch.religion].leader);
            do_say(mob, buf.toString());
            return;
        }
        do_say(mob, "Himm yes, religion. Do you really interested in that?.");
        do_say(mob, "Read the help first.Type 'help religion'");
        do_say(mob, "Do not forget that once you choose your religion. ");
        do_say(mob, "You have to complete some quests in order to change your religion.");
    }

    static void greet_prog_templeman(CHAR_DATA mob, CHAR_DATA ch) {


        if (!can_see(mob, ch) || IS_NPC(ch) || IS_IMMORTAL(ch)) {
            return;
        }
        TextBuffer buf = new TextBuffer();
        sprintf(buf, "smile %s", ch.name);
        interpret(mob, buf.toString(), false);
    }


    static int lookup_religion_leader(String name) {
        int value;

        for (value = 0; value < MAX_RELIGION; value++) {
            if (!str_prefix(name, religion_table[value].leader)) {
                return value;
            }
        }

        return 0;
    }

    static void greet_prog_lions(CHAR_DATA mob, CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }

        mob.cabal = CABAL_LIONS;
        mob.off_flags = SET_BIT(mob.off_flags, OFF_AREA_ATTACK);

        if (ch.cabal == CABAL_LIONS) {
            do_say(mob, "Welcome, my Lions.");
            return;
        }
        if (ch.last_death_time != -1 && current_time - ch.last_death_time < 600) {
            do_say(mob, "Ghosts are not allowed in this place.");
            do_slay(mob, ch.name);
            return;
        }

        if (IS_IMMORTAL(ch)) {
            return;
        }

        do_cb(mob, "Intruder! Intruder!");
        do_say(mob, "You should never disturb my cabal!");
    }

    static void greet_prog_hunter_old(CHAR_DATA mob, CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }

        mob.cabal = CABAL_HUNTER;
        mob.off_flags = SET_BIT(mob.off_flags, OFF_AREA_ATTACK);

        if (ch.cabal == CABAL_HUNTER) {
            do_say(mob, "Welcome, my dear hunter.");
            return;
        }
        if (ch.last_death_time != -1 && current_time - ch.last_death_time < 600) {
            do_say(mob, "Ghosts are not allowed in this place.");
            do_slay(mob, ch.name);
            return;
        }

        if (IS_IMMORTAL(ch)) {
            return;
        }

        do_cb(mob, "Intruder! Intruder!");
        do_say(mob, "You should never disturb my cabal!");
    }


    static void greet_prog_hunter(CHAR_DATA mob, CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }

        mob.cabal = CABAL_HUNTER;
        mob.off_flags = SET_BIT(mob.off_flags, OFF_AREA_ATTACK);

        if (ch.cabal == CABAL_HUNTER) {
            OBJ_DATA eyed;
            int i;

            do_say(mob, "Welcome, my dear hunter.");
            if (IS_SET(ch.quest, QUEST_EYE)) {
                return;
            }

            ch.quest = SET_BIT(ch.quest, QUEST_EYE);

            if (IS_GOOD(ch)) {
                i = 0;
            } else if (IS_EVIL(ch)) {
                i = 2;
            } else {
                i = 1;
            }

            eyed = create_object(get_obj_index(OBJ_VNUM_EYED_SWORD), 0);
            eyed.owner = ch.name;
            eyed.from = ch.name;
            eyed.altar = hometown_table[ch.hometown].altar[i];
            eyed.pit = hometown_table[ch.hometown].pit[i];
            eyed.level = ch.level;
            TextBuffer buf = new TextBuffer();
            buf.sprintf(eyed.short_descr, ch.name);
            eyed.short_descr = buf.toString();

            buf.sprintf(eyed.pIndexData.extra_descr.description, ch.name);
            eyed.extra_descr = new EXTRA_DESCR_DATA();
            eyed.extra_descr.keyword = eyed.pIndexData.extra_descr.keyword;
            eyed.extra_descr.description = buf.toString();
            eyed.extra_descr.next = null;

            eyed.value[2] = (ch.level / 10) + 3;
            eyed.level = ch.level;
            eyed.cost = 0;
            obj_to_char(eyed, mob);
            interpret(mob, "emote creates the Hunter's Sword.", false);
            do_say(mob, "I gave you the hunter's sword to you.");
            buf.sprintf("give eyed %s", ch.name);
            interpret(mob, buf.toString(), false);
            do_say(mob, "Remember that if you lose that, you can want it from cabal cleric!");
            do_say(mob, "Simple say to him that 'trouble'");
            return;
        }
        if (ch.last_death_time != -1 && current_time - ch.last_death_time < 600) {
            do_say(mob, "Ghosts are not allowed in this place.");
            do_slay(mob, ch.name);
            return;
        }

        if (IS_IMMORTAL(ch)) {
            return;
        }

        do_cb(mob, "Intruder! Intruder!");
        do_say(mob, "You should never disturb my cabal!");
    }


    static void fight_prog_diana(CHAR_DATA mob, CHAR_DATA ch) {
        CHAR_DATA ach, ach_next;
        int door;

        if (mob.in_room == null || number_percent() < 25) {
            return;
        }
        if (mob.in_room.area != mob.zone) {
            return;
        }

        do_yell(mob, "Help my guards.");
        for (ach = char_list; ach != null; ach = ach_next) {
            ach_next = ach.next;
            if (ach.in_room == null || ach.in_room.area != ch.in_room.area || !IS_NPC(ach)) {
                continue;
            }
            if (ach.pIndexData.vnum == 600 || ach.pIndexData.vnum == 603) {
                if (ach.fighting != null || ach.last_fought != null) {
                    continue;
                }
                if (mob.in_room == ach.in_room) {
                    int i;

                    act("{b$n call the gods for help.{x", ach, null, null, TO_ROOM, POS_SLEEPING);
                    act("{gGods advance $n to help Diana.{x", ach, null, null, TO_ROOM, POS_SLEEPING);
                    ach.max_hit = 6000;
                    ach.hit = 6000;
                    ach.level = 60;
                    ach.timer = 0;
                    ach.damage[DICE_NUMBER] = number_range(3, 5);
                    ach.damage[DICE_TYPE] = number_range(12, 22);
                    ach.damage[DICE_BONUS] = number_range(6, 8);
                    for (i = 0; i < MAX_STATS; i++) {
                        ach.perm_stat[i] = 23;
                    }
                    do_say(ach, "Diana, I came.");
                    do_murder(ach, ch.name);
                    continue;
                }
                door = find_path(ach.in_room.vnum, mob.in_room.vnum, ach, -40, true);
                if (door == -1) {
                    bug("Couldn't find a path with -40");
                } else {
                    if (number_percent() < 25) {
                        do_yell(ach, " Keep on Diana!.I am coming.");
                    } else {
                        do_say(ach, "I must go diana to help.");
                    }
                    move_char(ach, door);
                }
            }
        }
    }

    static void fight_prog_ofcol_guard(CHAR_DATA mob, CHAR_DATA ch) {
        CHAR_DATA ach, ach_next;
        int door;

        if (number_percent() < 25) {
            return;
        }
        TextBuffer buf = new TextBuffer();
        buf.sprintf("Help guards. %s is fighting with me.", ch.name);
        do_yell(mob, buf.toString());
        for (ach = char_list; ach != null; ach = ach_next) {
            ach_next = ach.next;
            if (ach.in_room.area != ch.in_room.area || !IS_NPC(ach)) {
                continue;
            }
            if (ach.pIndexData.vnum == 600) {
                if (ach.fighting != null) {
                    continue;
                }
                if (mob.in_room == ach.in_room) {
                    buf.sprintf("Now %s , you will pay for attacking a guard.", ch.name);
                    do_say(ach, buf.toString());
                    do_murder(ach, ch.name);
                    continue;
                }
                door = find_path(ach.in_room.vnum, mob.in_room.vnum, ach, -40, true);
                if (door == -1) {
                    bug("Couldn't find a path with -40");
                } else {
                    if (number_percent() < 25) {
                        do_yell(ach, " Keep on Guard! I am coming.");
                    } else {
                        do_say(ach, "I must go the guard to help.");
                    }
                    move_char(ach, door);
                }
            }
        }
    }

    static void speech_prog_wiseman(CHAR_DATA mob, CHAR_DATA ch, String speech) {
        StringBuilder arg = new StringBuilder();
        one_argument(speech, arg);
        if (arg.length() == 0) {
            return;
        }
        if (!str_cmp(speech, "aid me wiseman")) {
            heal_battle(mob, ch);
        }
    }

    static void greet_prog_armourer(CHAR_DATA mob, CHAR_DATA ch) {

        if (!can_see(mob, ch) || IS_NPC(ch) || IS_IMMORTAL(ch)) {
            return;
        }
        interpret(mob, "smile", false);
        TextBuffer buf = new TextBuffer();
        buf.sprintf("Welcome to my Armoury, %s",
                str_cmp(mob.in_room.area.name, hometown_table[ch.hometown].name) ? "traveler" : ch.name);
        do_say(mob, buf.toString());
        do_say(mob, "What can I interest you in?");
        do_say(mob, "I have only the finest armor in my store.");
        interpret(mob, "emote beams with pride.", false);
    }

    static void greet_prog_baker(CHAR_DATA mob, CHAR_DATA ch) {

        if (!can_see(mob, ch) || IS_NPC(ch) || IS_IMMORTAL(ch)) {
            return;
        }
        interpret(mob, "smile", false);
        TextBuffer buf = new TextBuffer();
        buf.sprintf("Welcome to my Bakery, %s",
                str_cmp(mob.in_room.area.name, hometown_table[ch.hometown].name) ? "traveler" : ch.name);
        do_say(mob, buf.toString());
    }

    static void greet_prog_beggar(CHAR_DATA mob, CHAR_DATA ch) {

        if (!can_see(mob, ch) || IS_NPC(ch) || IS_IMMORTAL(ch)) {
            return;
        }
        TextBuffer buf = new TextBuffer();
        buf.sprintf("Beg %s", str_cmp(mob.in_room.area.name, hometown_table[ch.hometown].name) ? "traveler" : ch.name);
        do_say(mob, buf.toString());
        do_say(mob, "Spare some gold?");
    }

    static void greet_prog_drunk(CHAR_DATA mob, CHAR_DATA ch) {
        if (!can_see(mob, ch) || IS_NPC(ch) || IS_IMMORTAL(ch)) {
            return;
        }
        if (number_percent() < 5) {
            do_yell(mob, "Monster! I found a monster! Kill! Banzai!");
            do_murder(mob, ch.name);
        }
    }

    static void greet_prog_grocer(CHAR_DATA mob, CHAR_DATA ch) {
        if (!can_see(mob, ch) || IS_NPC(ch) || IS_IMMORTAL(ch)) {
            return;
        }
        TextBuffer buf = new TextBuffer();
        buf.sprintf("Welcome to my Store, %s",
                str_cmp(mob.in_room.area.name, hometown_table[ch.hometown].name) ? "traveler" : ch.name);
        do_say(mob, buf.toString());
    }


    static void bribe_prog_beggar(CHAR_DATA mob, CHAR_DATA ch, Integer _amount) {
        if (_amount < 10) {
            TextBuffer buf = new TextBuffer();
            buf.sprintf("thank %s",
                    str_cmp(mob.in_room.area.name, hometown_table[ch.hometown].name) ? "traveler" : ch.name);
            interpret(mob, buf.toString(), false);
        } else if (_amount < 100) {
            do_say(mob, "Wow! Thank you! Thank you!");
        } else if (_amount < 500) {
            TextBuffer buf = new TextBuffer();
            do_say(mob, "Oh my God! Thank you! Thank you!");
            buf.sprintf("french %s", ch.name);
            interpret(mob, buf.toString(), false);
        } else {
            TextBuffer buf = new TextBuffer();
            buf.sprintf("dance %s", ch.name);
            interpret(mob, buf.toString(), false);
            buf.sprintf("french %s", ch.name);
            interpret(mob, buf.toString(), false);
        }
    }


    static void bribe_prog_drunk(CHAR_DATA mob, CHAR_DATA ch, Integer amount) {
        do_say(mob, "Ahh! More Spirits!  Good Spirits!");
        interpret(mob, "sing", false);
    }


    static void fight_prog_beggar(CHAR_DATA mob, CHAR_DATA ch) {
        if (mob.hit < (mob.max_hit * 0.45) && mob.hit > (mob.max_hit * 0.55)) {
            do_say(mob, "Halfway to death...");
        }
    }


    static boolean death_prog_beggar(CHAR_DATA mob) {
        if (number_percent() < 50) {
            do_say(mob, "Now I go to a better place.");
        } else {
            do_say(mob, "Forgive me God for I have sinned...");
        }
        return false;
    }

    static boolean death_prog_vagabond(CHAR_DATA mob) {
        interpret(mob, "emote throws back his head and cackles with insane glee!", false);
        return false;
    }


    static void speech_prog_crier(CHAR_DATA mob, CHAR_DATA ch, String speech) {
        StringBuilder arg = new StringBuilder();
        one_argument(speech, arg);
        if (is_name(arg.toString(), "what")) {
            do_say(mob, "My girlfriend left me.");
        }
    }


    static void area_prog_drunk(CHAR_DATA mob) {
        if (number_percent() < 5) {
            interpret(mob, "dance", false);
        } else if (number_percent() < 10) {
            interpret(mob, "sing", false);
        }
    }

    static void area_prog_janitor(CHAR_DATA mob) {
        if (number_percent() < 20) {
            interpret(mob, "grumble", false);
            do_say(mob, "Litterbugs");
            if (number_percent() < 20) {
                do_say(mob, "All I do each day is cleanup other people's messes.");
                if (number_percent() < 20) {
                    do_say(mob, "I do not get paid enough.");
                } else if (number_percent() < 20) {
                    do_say(mob, "Day in. Day out. This is all I do in 24 hours a day.");
                    if (number_percent() < 10) {
                        do_yell(mob, "I want a vacation!");
                    }
                }
            }
        }
    }

    static void area_prog_vagabond(CHAR_DATA mob) {
        if (number_percent() < 10) {
            do_say(mob, "Kill! Blood! Gore!");
        }
    }

    static void area_prog_baker(CHAR_DATA mob) {
        if (number_percent() < 5) {
            do_say(mob, "Would you like to try some tasty pies?");
        }
    }

    static void area_prog_grocer(CHAR_DATA mob) {
        if (number_percent() < 5) {
            do_say(mob, "Can I interest you in a lantern today?");
        }
    }


    static void speech_prog_hunter_cleric(CHAR_DATA mob, CHAR_DATA ch, String speech) {
        OBJ_DATA obj, in_obj;

        if (str_cmp(speech, "trouble")) {
            return;
        }

        if (ch.cabal != CABAL_HUNTER) {
            do_say(mob, "You must try hard!");
            return;
        }

        if (!IS_SET(ch.quest, QUEST_EYE)) {
            do_say(mob, "What do you mean?");
            return;
        }

        boolean matched = false;
        TextBuffer buf = new TextBuffer();
        for (obj = object_list; obj != null; obj = obj.next) {
            if (obj.pIndexData.vnum != OBJ_VNUM_EYED_SWORD ||
                    !obj.short_descr.contains(ch.name)) {
                continue;
            }

            matched = true;
            for (in_obj = obj; in_obj.in_obj != null; in_obj = in_obj.in_obj) {
            }

            if (in_obj.carried_by != null) {
                if (in_obj.carried_by == ch) {
                    do_say(mob, "Are you kidding me? Your sword is already carried by you!");
                    do_smite(mob, ch.name);
                    return;
                }

                buf.sprintf("Your sword is carried by %s!", PERS(in_obj.carried_by, ch));
                do_say(mob, buf.toString());
                if (in_obj.carried_by.in_room != null) {
                    buf.sprintf("%s is in general area of %s at %s!",
                            PERS(in_obj.carried_by, ch),
                            in_obj.carried_by.in_room.area.name,
                            in_obj.carried_by.in_room.name);
                    do_say(mob, buf.toString());
                    return;
                } else {
                    extract_obj(obj);
                    do_say(mob, "But i will give you a new one.");
                }
            } else {
                if (in_obj.in_room != null) {
                    buf.sprintf("Your sword is in general area of %s at %s!", in_obj.in_room.area.name, in_obj.in_room.name);
                    do_say(mob, buf.toString());
                    return;
                } else {
                    extract_obj(obj);
                    do_say(mob, "I will give you a new one.");
                }
            }
            break;
        }

        if (!matched) {
            do_say(mob, "Your sword is completely lost!");
        }

        int i;
        if (IS_GOOD(ch)) {
            i = 0;
        } else if (IS_EVIL(ch)) {
            i = 2;
        } else {
            i = 1;
        }

        obj = create_object(get_obj_index(OBJ_VNUM_EYED_SWORD), 0);
        obj.owner = ch.name;
        obj.from = ch.name;
        obj.altar = hometown_table[ch.hometown].altar[i];
        obj.pit = hometown_table[ch.hometown].pit[i];
        obj.level = ch.level;

        buf.sprintf(obj.short_descr, ch.name);
        obj.short_descr = buf.toString();

        buf.sprintf(obj.pIndexData.extra_descr.description, ch.name);
        obj.extra_descr = new EXTRA_DESCR_DATA();
        obj.extra_descr.keyword = obj.pIndexData.extra_descr.keyword;
        obj.extra_descr.description = buf.toString();
        obj.extra_descr.next = null;

        obj.value[2] = (ch.level / 10) + 3;
        obj.level = ch.level;
        obj.cost = 0;
        interpret(mob, "emote creates the Hunter's Sword.", false);
        do_say(mob, "I gave you another hunter's sword to you.");
        act("$N gives $p to $n.", ch, obj, mob, TO_ROOM);
        act("$N gives you $p.", ch, obj, mob, TO_CHAR);
        obj_to_char(obj, ch);
        do_say(mob, "Don't lose again!");
    }


    static void fight_prog_golem(CHAR_DATA mob, CHAR_DATA ch) {
        CHAR_DATA master;
        CHAR_DATA m_next;
        String spell;

        for (master = mob.in_room.people; master != null; master = m_next) {
            m_next = master.next_in_room;
            if (!IS_NPC(master) && mob.master == master && master.clazz == Clazz.NECROMANCER) {
                break;
            }
        }

        if (master == null) {
            return;
        }

        if (master.fighting == null) {
            return;
        }
        TextBuffer buf = new TextBuffer();
        if (master.fighting.fighting == master) {
            buf.sprintf("%s", master.name);
            do_rescue(mob, buf.toString());
        }

        switch (number_bits(4)) {
            case 0:
                spell = "curse";
                break;
            case 1:
                spell = "weaken";
                break;
            case 2:
                spell = "chill touch";
                break;
            case 3:
                spell = "blindness";
                break;
            case 4:
                spell = "poison";
                break;
            case 5:
                spell = "energy drain";
                break;
            case 6:
                spell = "harm";
                break;
            case 7:
                spell = "teleport";
                break;
            case 8:
                spell = "plague";
                break;
            default:
                spell = null;
                break;
        }
        Skill sn;
        if (spell == null || (sn = lookupSkill(spell)) == null) {
            return;
        }
        m_next = (mob.fighting != null) ? mob.fighting : master.fighting;
        if (m_next != null) {
            say_spell(mob, sn);
            sn.spell_fun(mob.level, mob, m_next, TARGET_CHAR);
        }
    }
}
