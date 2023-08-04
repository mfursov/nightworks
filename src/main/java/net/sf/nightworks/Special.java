package net.sf.nightworks;

import java.lang.reflect.Method;
import java.util.HashMap;
import net.sf.nightworks.util.NotNull;
import static net.sf.nightworks.ActComm.do_cb;
import static net.sf.nightworks.ActComm.do_say;
import static net.sf.nightworks.ActComm.do_yell;
import static net.sf.nightworks.ActMove.do_close;
import static net.sf.nightworks.ActMove.do_lock;
import static net.sf.nightworks.ActMove.do_open;
import static net.sf.nightworks.ActMove.do_track;
import static net.sf.nightworks.ActMove.do_unlock;
import static net.sf.nightworks.ActMove.move_char;
import static net.sf.nightworks.ActObj.can_loot;
import static net.sf.nightworks.ActObj.do_drop;
import static net.sf.nightworks.ActObj.do_get;
import static net.sf.nightworks.Comm.act;
import static net.sf.nightworks.Const.hometown_table;
import static net.sf.nightworks.DB.dice;
import static net.sf.nightworks.DB.get_room_index;
import static net.sf.nightworks.DB.number_bits;
import static net.sf.nightworks.DB.number_percent;
import static net.sf.nightworks.DB.number_range;
import static net.sf.nightworks.DB.time_info;
import static net.sf.nightworks.Fight.damage;
import static net.sf.nightworks.Fight.do_flee;
import static net.sf.nightworks.Fight.do_kill;
import static net.sf.nightworks.Fight.do_murder;
import static net.sf.nightworks.Fight.is_safe;
import static net.sf.nightworks.Fight.mob_cast_cleric;
import static net.sf.nightworks.Fight.mob_cast_mage;
import static net.sf.nightworks.Fight.multi_hit;
import static net.sf.nightworks.Fight.one_hit;
import static net.sf.nightworks.Handler.can_see;
import static net.sf.nightworks.Handler.char_from_room;
import static net.sf.nightworks.Handler.char_to_room;
import static net.sf.nightworks.Handler.extract_char;
import static net.sf.nightworks.Handler.extract_obj;
import static net.sf.nightworks.Handler.is_affected;
import static net.sf.nightworks.Handler.obj_from_obj;
import static net.sf.nightworks.Handler.obj_from_room;
import static net.sf.nightworks.Handler.obj_to_char;
import static net.sf.nightworks.Handler.obj_to_room;
import static net.sf.nightworks.Interp.interpret;
import static net.sf.nightworks.Magic.say_spell;
import static net.sf.nightworks.Magic.spell_armor;
import static net.sf.nightworks.Magic.spell_bless;
import static net.sf.nightworks.Magic.spell_cure_blindness;
import static net.sf.nightworks.Magic.spell_cure_disease;
import static net.sf.nightworks.Magic.spell_cure_light;
import static net.sf.nightworks.Magic.spell_cure_poison;
import static net.sf.nightworks.Magic.spell_poison;
import static net.sf.nightworks.Magic.spell_refresh;
import static net.sf.nightworks.MartialArt.do_backstab;
import static net.sf.nightworks.MartialArt.do_bandage;
import static net.sf.nightworks.MartialArt.do_resistance;
import static net.sf.nightworks.MartialArt.do_spellbane;
import static net.sf.nightworks.Nightworks.AFF_CALM;
import static net.sf.nightworks.Nightworks.AFF_CHARM;
import static net.sf.nightworks.Nightworks.AFF_REGENERATION;
import static net.sf.nightworks.Nightworks.AREA_HOMETOWN;
import static net.sf.nightworks.Nightworks.CABAL_BATTLE;
import static net.sf.nightworks.Nightworks.CABAL_RULER;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.COMM_NOSHOUT;
import static net.sf.nightworks.Nightworks.DAM_BASH;
import static net.sf.nightworks.Nightworks.ETHOS_LAWFUL;
import static net.sf.nightworks.Nightworks.GROUP_VNUM_OGRES;
import static net.sf.nightworks.Nightworks.GROUP_VNUM_TROLLS;
import static net.sf.nightworks.Nightworks.IS_AFFECTED;
import static net.sf.nightworks.Nightworks.IS_AWAKE;
import static net.sf.nightworks.Nightworks.IS_EVIL;
import static net.sf.nightworks.Nightworks.IS_GOOD;
import static net.sf.nightworks.Nightworks.IS_IMMORTAL;
import static net.sf.nightworks.Nightworks.IS_NPC;
import static net.sf.nightworks.Nightworks.IS_SET;
import static net.sf.nightworks.Nightworks.ITEM_CORPSE_NPC;
import static net.sf.nightworks.Nightworks.ITEM_DRINK_CON;
import static net.sf.nightworks.Nightworks.ITEM_TAKE;
import static net.sf.nightworks.Nightworks.ITEM_TRASH;
import static net.sf.nightworks.Nightworks.LEVEL_IMMORTAL;
import static net.sf.nightworks.Nightworks.MOB_VNUM_PATROLMAN;
import static net.sf.nightworks.Nightworks.OBJ_DATA;
import static net.sf.nightworks.Nightworks.OPROG_GET;
import static net.sf.nightworks.Nightworks.PLR_WANTED;
import static net.sf.nightworks.Nightworks.POS_FIGHTING;
import static net.sf.nightworks.Nightworks.POS_RESTING;
import static net.sf.nightworks.Nightworks.POS_SLEEPING;
import static net.sf.nightworks.Nightworks.POS_STANDING;
import static net.sf.nightworks.Nightworks.REMOVE_BIT;
import static net.sf.nightworks.Nightworks.RIDDEN;
import static net.sf.nightworks.Nightworks.SET_BIT;
import static net.sf.nightworks.Nightworks.SPEC_FUN;
import static net.sf.nightworks.Nightworks.TARGET_CHAR;
import static net.sf.nightworks.Nightworks.TO_ALL;
import static net.sf.nightworks.Nightworks.TO_CHAR;
import static net.sf.nightworks.Nightworks.TO_NOTVICT;
import static net.sf.nightworks.Nightworks.TO_ROOM;
import static net.sf.nightworks.Nightworks.TO_VICT;
import static net.sf.nightworks.Nightworks.UMIN;
import static net.sf.nightworks.Nightworks.char_list;
import static net.sf.nightworks.Nightworks.exit;
import static net.sf.nightworks.Skill.gsn_assassinate;
import static net.sf.nightworks.Skill.gsn_claw;
import static net.sf.nightworks.Skill.gsn_poison;
import static net.sf.nightworks.Skill.gsn_resistance;
import static net.sf.nightworks.Skill.gsn_spellbane;
import static net.sf.nightworks.Skill.lookupSkill;
import static net.sf.nightworks.util.Logger.logError;
import static net.sf.nightworks.util.TextUtils.str_cmp;

class Special {
    /*
     * Given a name, return the appropriate spec fun.
     */

    static class SPEC_FUN_IMPL implements SPEC_FUN {
        final Method m;

        public SPEC_FUN_IMPL(String name) {
            Method mm = null;
            try {
                mm = Special.class.getDeclaredMethod(name, CHAR_DATA.class);
                spec_table.put(name, this);
            } catch (NoSuchMethodException e) {
                logError(e);
                exit(1);
            }
            m = mm;
        }

        public boolean run(@NotNull CHAR_DATA ch) {
            boolean res = false;
            try {
                res = (Boolean) m.invoke(null, ch);
            } catch (Exception e) {
                logError(e);
                exit(1);
            }
            return res;
        }

        public String getName() {
            return m.getName();
        }

        private static void init() {
            new SPEC_FUN_IMPL("spec_breath_any");
            new SPEC_FUN_IMPL("spec_breath_acid");
            new SPEC_FUN_IMPL("spec_breath_fire");
            new SPEC_FUN_IMPL("spec_breath_frost");
            new SPEC_FUN_IMPL("spec_breath_gas");
            new SPEC_FUN_IMPL("spec_breath_lightning");
            new SPEC_FUN_IMPL("spec_cast_adept");
            new SPEC_FUN_IMPL("spec_cast_cleric");
            new SPEC_FUN_IMPL("spec_cast_judge");
            new SPEC_FUN_IMPL("spec_cast_mage");
            new SPEC_FUN_IMPL("spec_cast_beholder");
            new SPEC_FUN_IMPL("spec_cast_undead");
            new SPEC_FUN_IMPL("spec_executioner");
            new SPEC_FUN_IMPL("spec_fido");
            new SPEC_FUN_IMPL("spec_guard");
            new SPEC_FUN_IMPL("spec_janitor");
            new SPEC_FUN_IMPL("spec_mayor");
            new SPEC_FUN_IMPL("spec_poison");
            new SPEC_FUN_IMPL("spec_thief");
            new SPEC_FUN_IMPL("spec_nasty");
            new SPEC_FUN_IMPL("spec_troll_member");
            new SPEC_FUN_IMPL("spec_ogre_member");
            new SPEC_FUN_IMPL("spec_patrolman");
            new SPEC_FUN_IMPL("spec_cast_cabal");
            new SPEC_FUN_IMPL("spec_stalker");
            new SPEC_FUN_IMPL("spec_special_guard");
            new SPEC_FUN_IMPL("spec_questmaster");
            new SPEC_FUN_IMPL("spec_assassinater");
            new SPEC_FUN_IMPL("spec_repairman");
            new SPEC_FUN_IMPL("spec_captain");
            new SPEC_FUN_IMPL("spec_headlamia");
            new SPEC_FUN_IMPL("spec_fight_enforcer");
            new SPEC_FUN_IMPL("spec_fight_invader");
            new SPEC_FUN_IMPL("spec_fight_ivan");
            new SPEC_FUN_IMPL("spec_fight_seneschal");
            new SPEC_FUN_IMPL("spec_fight_powerman");
            new SPEC_FUN_IMPL("spec_fight_protector");
            new SPEC_FUN_IMPL("spec_fight_hunter");
            new SPEC_FUN_IMPL("spec_fight_lionguard");
        }
    }

    static SPEC_FUN spec_lookup(String name) {
        return spec_table.get(name);
    }

    static String spec_name(SPEC_FUN function) {
        return ((SPEC_FUN_IMPL) function).m.getName();
    }

    static boolean spec_troll_member(@NotNull CHAR_DATA ch) {
        CHAR_DATA vch, victim = null;
        int count = 0;
        String message;

        if (!IS_AWAKE(ch) || IS_AFFECTED(ch, AFF_CALM) || ch.in_room == null || IS_AFFECTED(ch, AFF_CHARM) || ch.fighting != null) {
            return false;
        }

        /* find an ogre to beat up */
        for (vch = ch.in_room.people; vch != null; vch = vch.next_in_room) {
            if (!IS_NPC(vch) || ch == vch) {
                continue;
            }
            if (vch.pIndexData.vnum == MOB_VNUM_PATROLMAN) {
                return false;
            }
            if (vch.pIndexData.group == GROUP_VNUM_OGRES && ch.level > vch.level - 2 && !is_safe(ch, vch)) {
                if (number_range(0, count) == 0) {
                    victim = vch;
                }
                count++;
            }
        }

        if (victim == null) {
            return false;
        }

        /* say something, then raise hell */
        message = switch (number_range(0, 6)) {
            default -> null;
            case 0 -> "$n yells 'I've been looking for you, punk!'";
            case 1 -> "With a scream of rage, $n attacks $N.";
            case 2 -> "$n says 'What's slimy Ogre trash like you doing around here?'";
            case 3 -> "$n cracks his knuckles and says 'Do ya feel lucky?'";
            case 4 -> "$n says 'There's no cops to save you this time!'";
            case 5 -> "$n says 'Time to join your brother, spud.'";
            case 6 -> "$n says 'Let's rock.'";
        };

        if (message != null) {
            act(message, ch, null, victim, TO_ALL);
        }
        multi_hit(ch, victim, null);
        return true;
    }


    static boolean spec_ogre_member(@NotNull CHAR_DATA ch) {
        CHAR_DATA vch, victim = null;
        int count = 0;
        String message;

        if (!IS_AWAKE(ch) || IS_AFFECTED(ch, AFF_CALM) || ch.in_room == null || IS_AFFECTED(ch, AFF_CHARM) || ch.fighting != null) {
            return false;
        }

        /* find an troll to beat up */
        for (vch = ch.in_room.people; vch != null; vch = vch.next_in_room) {
            if (!IS_NPC(vch) || ch == vch) {
                continue;
            }

            if (vch.pIndexData.vnum == MOB_VNUM_PATROLMAN) {
                return false;
            }

            if (vch.pIndexData.group == GROUP_VNUM_TROLLS && ch.level > vch.level - 2 && !is_safe(ch, vch)) {
                if (number_range(0, count) == 0) {
                    victim = vch;
                }

                count++;
            }
        }

        if (victim == null) {
            return false;
        }

        /* say something, then raise hell */
        message = switch (number_range(0, 6)) {
            default -> null;
            case 0 -> "$n yells 'I've been looking for you, punk!'";
            case 1 -> "With a scream of rage, $n attacks $N.'";
            case 2 -> "$n says 'What's Troll filth like you doing around here?'";
            case 3 -> "$n cracks his knuckles and says 'Do ya feel lucky?'";
            case 4 -> "$n says 'There's no cops to save you this time!'";
            case 5 -> "$n says 'Time to join your brother, spud.'";
            case 6 -> "$n says 'Let's rock.'";
        };

        if (message != null) {
            act(message, ch, null, victim, TO_ALL);
        }
        multi_hit(ch, victim, null);
        return true;
    }

    static boolean spec_patrolman(@NotNull CHAR_DATA ch) {
        CHAR_DATA vch, victim = null;
        /*    OBJ_DATA obj; */
        String message;
        int count = 0;

        if (!IS_AWAKE(ch) || IS_AFFECTED(ch, AFF_CALM) || ch.in_room == null || IS_AFFECTED(ch, AFF_CHARM) || ch.fighting != null) {
            return false;
        }

        /* look for a fight in the room */
        for (vch = ch.in_room.people; vch != null; vch = vch.next_in_room) {
            if (vch == ch) {
                continue;
            }

            if (vch.fighting != null)  /* break it up! */ {
                if (number_range(0, count) == 0) {
                    victim = (vch.level > vch.fighting.level) ? vch : vch.fighting;
                }
                count++;
            }
        }

        if (victim == null || (IS_NPC(victim) && victim.spec_fun == ch.spec_fun)) {
            return false;
        }
/*
    if (((obj = search_obj_char(ch,WEAR_NECK)) != null
    &&   obj.pIndexData.vnum == OBJ_VNUM_WHISTLE)
    ||  ((obj = get_eq_char(ch,WEAR_NECK_2)) != null
    &&   obj.pIndexData.vnum == OBJ_VNUM_WHISTLE))
    {
    act("You blow down hard on $p.",ch,obj,null,TO_CHAR);
    act("$n blows on $p, ***WHEEEEEEEEEEEET***",ch,obj,null,TO_ROOM);

        for ( vch = char_list; vch != null; vch = vch.next )
        {
            if ( vch.in_room == null )
                continue;

            if (vch.in_room != ch.in_room
        &&  vch.in_room.area == ch.in_room.area)
                send_to_char( "You hear a shrill whistling sound.\n", vch );
        }
    }
*/
        message = switch (number_range(0, 6)) {
            default -> null;
            case 0 -> "$n yells 'All roit! All roit! break it up!'";
            case 1 -> "$n says 'Society's to blame, but what's a bloke to do?'";
            case 2 -> "$n mumbles 'bloody kids will be the death of us all.'";
            case 3 -> "$n shouts 'Stop that! Stop that!' and attacks.";
            case 4 -> "$n pulls out his billy and goes to work.";
            case 5 -> "$n sighs in resignation and proceeds to break up the fight.";
            case 6 -> "$n says 'Settle down, you hooligans!'";
        };

        if (message != null) {
            act(message, ch, null, null, TO_ALL);
        }

        multi_hit(ch, victim, null);

        return true;
    }

    /*
     * Core procedure for dragons.
     */

    static boolean dragon(@NotNull CHAR_DATA ch, String spell_name) {
        CHAR_DATA victim;
        CHAR_DATA v_next;
        Skill sn;

        if (ch.position != POS_FIGHTING) {
            return false;
        }

        for (victim = ch.in_room.people; victim != null; victim = v_next) {
            v_next = victim.next_in_room;
            CHAR_DATA ridden = RIDDEN(ch);
            if (((ridden != null && ridden.fighting == victim) || victim.fighting == ch) && number_bits(3) == 0) {
                break;
            }
        }

        if (victim == null) {
            return false;
        }

        if ((sn = lookupSkill(spell_name)) == null) {
            return false;
        }

        sn.spell_fun(ch.level, ch, victim, TARGET_CHAR);
        return true;
    }

    /*
     * Special procedures for mobiles.
     */
    static boolean spec_breath_any(@NotNull CHAR_DATA ch) {
        if (ch.position != POS_FIGHTING) {
            return false;
        }

        return switch (number_bits(3)) {
            case 0 -> spec_breath_fire(ch);
            case 1, 2 -> spec_breath_lightning(ch);
            case 3 -> spec_breath_gas(ch);
            case 4 -> spec_breath_acid(ch);
            case 5, 6, 7 -> spec_breath_frost(ch);
            default -> false;
        };

    }

    static boolean spec_breath_acid(@NotNull CHAR_DATA ch) {
        return dragon(ch, "acid breath");
    }

    static boolean spec_breath_fire(@NotNull CHAR_DATA ch) {
        return dragon(ch, "fire breath");
    }

    static boolean spec_breath_frost(@NotNull CHAR_DATA ch) {
        return dragon(ch, "frost breath");
    }

    static boolean spec_breath_gas(@NotNull CHAR_DATA ch) {
        if (ch.position != POS_FIGHTING) {
            return false;
        }
        Skill.gsn_gas_breath.spell_fun(ch.level, ch, null, TARGET_CHAR);
        return true;
    }

    static boolean spec_breath_lightning(@NotNull CHAR_DATA ch) {
        return dragon(ch, "lightning breath");
    }

    static boolean spec_cast_adept(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA v_next;

        if (!IS_AWAKE(ch)) {
            return false;
        }

        for (victim = ch.in_room.people; victim != null; victim = v_next) {
            v_next = victim.next_in_room;
            if (victim != ch && can_see(ch, victim) && number_bits(1) == 0 && !IS_NPC(victim) && victim.level < 11) {
                break;
            }
        }

        if (victim == null) {
            return false;
        }

        switch (number_bits(4)) {
            case 0 -> {
                act("$n utters the word 'abrazak'.", ch, null, null, TO_ROOM);
                spell_armor(Skill.gsn_armor, ch.level, ch, victim);
                return true;
            }
            case 1 -> {
                act("$n utters the word 'fido'.", ch, null, null, TO_ROOM);
                spell_bless(Skill.gsn_bless, ch.level, ch, victim, TARGET_CHAR);
                return true;
            }
            case 2 -> {
                act("$n utters the words 'judicandus noselacri'.", ch, null, null, TO_ROOM);
                spell_cure_blindness(ch.level, ch, victim);
                return true;
            }
            case 3 -> {
                act("$n utters the words 'judicandus dies'.", ch, null, null, TO_ROOM);
                spell_cure_light(ch.level, ch, victim);
                return true;
            }
            case 4 -> {
                act("$n utters the words 'judicandus sausabru'.", ch, null, null, TO_ROOM);
                spell_cure_poison(ch.level, ch, victim);
                return true;
            }
            case 5 -> {
                act("$n utters the word 'candusima'.", ch, null, null, TO_ROOM);
                spell_refresh(ch.level, ch, victim);
                return true;
            }
            case 6 -> {
                act("$n utters the words 'judicandus eugzagz'.", ch, null, null, TO_ROOM);
                spell_cure_disease(ch.level, ch, victim);
            }
        }

        return false;
    }


    static boolean spec_cast_cleric(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA v_next;

        if (ch.position != POS_FIGHTING) {
            return false;
        }

        for (victim = ch.in_room.people; victim != null; victim = v_next) {
            v_next = victim.next_in_room;
            if (victim.fighting == ch && number_bits(2) == 0) {
                break;
            }
        }

        if (victim == null) {
            return false;
        }

        mob_cast_cleric(ch, victim);
        return true;
    }

    static boolean spec_cast_judge(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA v_next;
        String spell;
        Skill sn;

        if (ch.position != POS_FIGHTING) {
            return false;
        }

        for (victim = ch.in_room.people; victim != null; victim = v_next) {
            v_next = victim.next_in_room;
            if (victim.fighting == ch && number_bits(2) == 0) {
                break;
            }
        }

        if (victim == null) {
            return false;
        }

        spell = "high explosive";
        if ((sn = lookupSkill(spell)) == null) {
            return false;
        }
        sn.spell_fun(ch.level, ch, victim, TARGET_CHAR);
        return true;
    }


    static boolean spec_cast_mage(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA v_next;

        if (ch.position != POS_FIGHTING) {
            return false;
        }

        for (victim = ch.in_room.people; victim != null; victim = v_next) {
            v_next = victim.next_in_room;
            if (victim.fighting == ch && number_bits(2) == 0) {
                break;
            }
        }

        if (victim == null) {
            return false;
        }

        mob_cast_mage(ch, victim);
        return true;
    }

    static boolean spec_cast_undead(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA v_next;
        String spell;
        Skill sn;

        if (ch.position != POS_FIGHTING) {
            return false;
        }

        for (victim = ch.in_room.people; victim != null; victim = v_next) {
            v_next = victim.next_in_room;
            if (victim.fighting == ch && number_bits(2) == 0) {
                break;
            }
        }

        if (victim == null) {
            return false;
        }

        for (; ; ) {
            int min_level;

            spell = switch (number_bits(4)) {
                case 0 -> {
                    min_level = 0;
                    yield "curse";
                }
                case 1 -> {
                    min_level = 3;
                    yield "weaken";
                }
                case 2 -> {
                    min_level = 6;
                    yield "chill touch";
                }
                case 3 -> {
                    min_level = 9;
                    yield "blindness";
                }
                case 4 -> {
                    min_level = 12;
                    yield "poison";
                }
                case 5 -> {
                    min_level = 15;
                    yield "energy drain";
                }
                case 6 -> {
                    min_level = 18;
                    yield "harm";
                }
                case 7 -> {
                    min_level = 21;
                    yield "teleport";
                }
                case 8 -> {
                    min_level = 20;
                    yield "plague";
                }
                default -> {
                    min_level = 18;
                    yield "harm";
                }
            };

            if (ch.level >= min_level) {
                break;
            }
        }

        if ((sn = lookupSkill(spell)) == null) {
            return false;
        }
        say_spell(ch, sn);
        sn.spell_fun(ch.level, ch, victim, TARGET_CHAR);
        return true;
    }

    static boolean spec_executioner(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA v_next;
        String crime;

        if (!IS_AWAKE(ch) || ch.fighting != null) {
            return false;
        }

        crime = "";
        for (victim = ch.in_room.people; victim != null; victim = v_next) {
            v_next = victim.next_in_room;

            if (!IS_NPC(victim) && IS_SET(victim.act, PLR_WANTED) && can_see(ch, victim)) {
                crime = "CRIMINAL";
                break;
            }
        }

        if (victim == null) {
            return false;
        }

        ch.comm = REMOVE_BIT(ch.comm, COMM_NOSHOUT);
        do_yell(ch, victim.name + " is a " + crime + "!  PROTECT THE INNOCENT!  MORE BLOOOOD!!!");
        multi_hit(ch, victim, null);
        return true;
    }

    static boolean spec_fido(@NotNull CHAR_DATA ch) {
        OBJ_DATA corpse;
        OBJ_DATA c_next;
        OBJ_DATA obj;
        OBJ_DATA obj_next;

        if (!IS_AWAKE(ch)) {
            return false;
        }

        for (corpse = ch.in_room.contents; corpse != null; corpse = c_next) {
            c_next = corpse.next_content;
            if (corpse.item_type != ITEM_CORPSE_NPC) {
                continue;
            }

            act("$n savagely devours a corpse.", ch, null, null, TO_ROOM);
            for (obj = corpse.contains; obj != null; obj = obj_next) {
                obj_next = obj.next_content;
                obj_from_obj(obj);
                obj_to_room(obj, ch.in_room);
            }
            extract_obj(corpse);
            return true;
        }

        return false;
    }

    static boolean spec_janitor(@NotNull CHAR_DATA ch) {
        OBJ_DATA trash;
        OBJ_DATA trash_next;

        if (!IS_AWAKE(ch)) {
            return false;
        }

        for (trash = ch.in_room.contents; trash != null; trash = trash_next) {
            trash_next = trash.next_content;
            if (!IS_SET(trash.wear_flags, ITEM_TAKE) || !can_loot(ch, trash)) {
                continue;
            }
            if (trash.item_type == ITEM_DRINK_CON || trash.item_type == ITEM_TRASH || trash.cost < 10) {
                act("$n picks up some trash.", ch, null, null, TO_ROOM);
                obj_from_room(trash);
                obj_to_char(trash, ch);
                if (IS_SET(trash.progtypes, OPROG_GET)) {
                    trash.pIndexData.oprogs.get_prog.run(trash, ch);
                }
                return true;
            }
        }

        return false;
    }

    static class spec_mayor_data {
        static String open_path = "W3a3003b000c000d111Oe333333Oe22c222112212111a1S.";
        static String close_path = "W3a3003b000c000d111CE333333CE22c222112212111a1S.";

        static String path;
        static int pos;
        static boolean move;
    }

    static boolean spec_mayor(@NotNull CHAR_DATA ch) {

        OBJ_DATA key;

        if (!spec_mayor_data.move) {
            if (time_info.hour == 6) {
                spec_mayor_data.path = spec_mayor_data.open_path;
                spec_mayor_data.move = true;
                spec_mayor_data.pos = 0;
            }

            if (time_info.hour == 20) {
                spec_mayor_data.path = spec_mayor_data.close_path;
                spec_mayor_data.move = true;
                spec_mayor_data.pos = 0;
            }
        }

        if (!spec_mayor_data.move || ch.position < POS_SLEEPING) {
            return false;
        }

        switch (spec_mayor_data.path.charAt(spec_mayor_data.pos)) {
            case '0', '1', '2', '3' -> move_char(ch, spec_mayor_data.path.charAt(spec_mayor_data.pos) - '0');
            case 'W' -> {
                ch.position = POS_STANDING;
                act("$n awakens and groans loudly.", ch, null, null, TO_ROOM);
            }
            case 'S' -> {
                ch.position = POS_SLEEPING;
                act("$n lies down and falls asleep.", ch, null, null, TO_ROOM);
            }
            case 'a' -> do_say(ch, "Hello Honey!");
            case 'b' -> do_say(ch, "What a view!  I must do something about that dump!");
            case 'c' -> do_say(ch, "Vandals  Youngsters have no respect for anything!");
            case 'd' -> do_say(ch, "Good day, citizens!");
            case 'e' -> do_say(ch, "I hereby declare the city of Midgaard open!");
            case 'E' -> do_say(ch, "I hereby declare the city of Midgaard closed!");
            case 'O' -> {
                do_unlock(ch, "gate");
                do_open(ch, "gate");
                interpret(ch, "emote unlocks the gate key from the gate.", false);
                for (key = ch.in_room.contents; key != null; key = key.next_content) {
                    if (key.pIndexData.vnum == 3379) {
                        break;
                    }
                }
                if (key != null) {
                    key.wear_flags = SET_BIT(key.wear_flags, ITEM_TAKE);
                }
                do_get(ch, "gatekey");
            }
            case 'C' -> {
                do_close(ch, "gate");
                do_lock(ch, "gate");
                do_drop(ch, "key");
                interpret(ch, "emote locks the gate key to the gate, with chain.", false);
                for (key = ch.in_room.contents; key != null; key = key.next_content) {
                    if (key.pIndexData.vnum == 3379) {
                        break;
                    }
                }
                if (key != null) {
                    key.wear_flags = REMOVE_BIT(key.wear_flags, ITEM_TAKE);
                }
            }
            case '.' -> spec_mayor_data.move = false;
        }

        spec_mayor_data.pos++;
        return false;
    }


    static boolean spec_poison(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim;

        if (ch.position != POS_FIGHTING || (victim = ch.fighting) == null || number_percent() > 2 * ch.level) {
            return false;
        }

        act("You bite $N!", ch, null, victim, TO_CHAR);
        act("$n bites $N!", ch, null, victim, TO_NOTVICT);
        act("$n bites you!", ch, null, victim, TO_VICT);
        spell_poison(gsn_poison, ch.level, ch, victim, TARGET_CHAR);
        return true;
    }


    static boolean spec_thief(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA v_next;
        int gold, silver;

        if (ch.position != POS_STANDING) {
            return false;
        }

        for (victim = ch.in_room.people; victim != null; victim = v_next) {
            v_next = victim.next_in_room;

            if (IS_NPC(victim) || victim.level >= LEVEL_IMMORTAL || number_bits(5) != 0 || !can_see(ch, victim)) {
                continue;
            }

            if (IS_AWAKE(victim) && number_range(0, ch.level) == 0) {
                act("You discover $n's hands in your wallet!", ch, null, victim, TO_VICT);
                act("$N discovers $n's hands in $S wallet!", ch, null, victim, TO_NOTVICT);
                return true;
            } else {
                gold = victim.gold * UMIN(number_range(1, 20), ch.level / 2) / 100;
                gold = UMIN(gold, ch.level * ch.level * 10);
                ch.gold += gold;
                victim.gold -= gold;
                silver = victim.silver * UMIN(number_range(1, 20), ch.level / 2) / 100;
                silver = UMIN(silver, ch.level * ch.level * 25);
                ch.silver += silver;
                victim.silver -= silver;
                return true;
            }
        }

        return false;
    }


    static boolean spec_cast_cabal(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA v_next;

        if (!IS_AWAKE(ch)) {
            return false;
        }

        for (victim = ch.in_room.people; victim != null; victim = v_next) {
            v_next = victim.next_in_room;
            if (victim != ch && can_see(ch, victim) && number_bits(1) == 0) {
                break;
            }
        }

        if (victim == null) {
            return false;
        }


        switch (number_bits(4)) {
            case 0 -> {
                act("$n utters the word 'abracal'.", ch, null, null, TO_ROOM);
                spell_armor(Skill.gsn_armor, ch.level, ch, victim);
                return true;
            }
            case 1 -> {
                act("$n utters the word 'balc'.", ch, null, null, TO_ROOM);
                spell_bless(Skill.gsn_bless, ch.level, ch, victim, TARGET_CHAR);
                return true;
            }
            case 2 -> {
                act("$n utters the word 'judicandus noselacba'.", ch, null, null, TO_ROOM);
                spell_cure_blindness(ch.level, ch, victim);
                return true;
            }
            case 3 -> {
                act("$n utters the word 'judicandus bacla'.", ch, null, null, TO_ROOM);
                spell_cure_light(ch.level, ch, victim);
                return true;
            }
            case 4 -> {
                act("$n utters the words 'judicandus sausabcla'.", ch, null, null, TO_ROOM);
                spell_cure_poison(ch.level, ch, victim);
                return true;
            }
            case 5 -> {
                act("$n utters the words 'candabala'.", ch, null, null, TO_ROOM);
                spell_refresh(ch.level, ch, victim);
                return true;
            }
        }

        return false;
    }

    static boolean spec_guard(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA v_next;
        CHAR_DATA ech;
        String crime;

        if (!IS_AWAKE(ch) || ch.fighting != null) {
            return false;
        }

        ech = null;
        crime = "";

        for (victim = ch.in_room.people; victim != null; victim = v_next) {
            v_next = victim.next_in_room;

            if (!can_see(ch, victim)) {
                continue;
            }

            if (IS_SET(ch.in_room.area.area_flag, AREA_HOMETOWN) && number_percent() < 2) {
                do_say(ch, "Do i know you?.");
                if (str_cmp(ch.in_room.area.name, hometown_table[victim.hometown].name)) {
                    do_say(ch, "I don't remember you. Go away!");
                } else {
                    do_say(ch, "Ok, my dear. I have just remembered.");
                    interpret(ch, "smile", false);
                }
            }

            if (!IS_NPC(victim) && IS_SET(victim.act, PLR_WANTED)) {
                crime = "CRIMINAL";
                break;
            }

            if (victim.fighting != null && victim.fighting != ch && victim.ethos != ETHOS_LAWFUL && !IS_GOOD(victim) && !IS_EVIL(victim.fighting)) {
                ech = victim;
                victim = null;
                break;
            }
        }

        if (victim != null) {
            do_yell(ch, victim.name + " is a " + crime + "!  PROTECT THE INNOCENT!!  BANZAI!!");
            multi_hit(ch, victim, null);
            return true;
        }

        if (ech != null) {
            act("$n screams 'PROTECT THE INNOCENT!!  BANZAI!!", ch, null, null, TO_ROOM);
            multi_hit(ch, ech, null);
            return true;
        }

        return false;
    }


    static boolean spec_special_guard(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim, ech;
        CHAR_DATA v_next;
        String crime;

        if (!IS_AWAKE(ch) || ch.fighting != null) {
            return false;
        }

        crime = "";
        ech = null;

        for (victim = ch.in_room.people; victim != null; victim = v_next) {
            v_next = victim.next_in_room;

            if (!can_see(ch, victim)) {
                continue;
            }

            if (!IS_NPC(victim) && IS_SET(victim.act, PLR_WANTED)) {
                crime = "CRIMINAL";
                break;
            }

            if (victim.fighting != null && victim.fighting != ch && victim.fighting.cabal == CABAL_RULER) {
                ech = victim;
                victim = null;
                break;
            }
        }

        if (victim != null) {
            do_yell(ch, victim.name + " is a " + crime + "!  PROTECT THE INNOCENT!!  BANZAI!!");
            multi_hit(ch, victim, null);
            return true;
        }

        if (ech != null) {
            act("$n screams 'PROTECT THE INNOCENT!!  BANZAI!!", ch, null, null, TO_ROOM);
            multi_hit(ch, ech, null);
            return true;
        }

        return false;
    }

    static boolean spec_stalker(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA wch;
        CHAR_DATA wch_next;
        int i;

        victim = ch.last_fought;

        if (ch.fighting != null) {
            return false;
        }

        if (ch.status == 10) {
            ch.cabal = CABAL_RULER;
            do_cb(ch, "I have killed my victim, now I can leave the realms.");
            extract_char(ch, true);
            return true;
        }

        if (victim == null) {
            ch.cabal = CABAL_RULER;
            do_cb(ch, "To their shame, my victim has cowardly left the game. I must leave also.");
            extract_char(ch, true);
            return true;
        }

        if (IS_GOOD(victim)) {
            i = 0;
        } else if (IS_EVIL(victim)) {
            i = 2;
        } else {
            i = 1;
        }

        for (wch = ch.in_room.people; wch != null; wch = wch_next) {
            wch_next = wch.next_in_room;
            if (victim == wch) {
                do_yell(ch, victim.name + ", you criminal! Now you die!");
                multi_hit(ch, wch, null);
                return true;
            }
        }
        do_track(ch, victim.name);

        if (ch.status == 5) {
            if (ch.in_room != get_room_index(hometown_table[victim.hometown].recall[1])) {
                char_from_room(ch);
                char_to_room(ch, get_room_index(hometown_table[victim.hometown].recall[i]));
                do_track(ch, victim.name);
                return true;
            } else {
                ch.cabal = CABAL_RULER;
                do_cb(ch, "To my shame I have lost track of " + victim.name + ".  I must leave.");
                extract_char(ch, true);
                return true;
            }
        }
        return false;
    }

    static boolean spec_nasty(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim, v_next;
        long gold;

        if (!IS_AWAKE(ch)) {
            return false;
        }

        if (ch.position != POS_FIGHTING) {
            for (victim = ch.in_room.people; victim != null; victim = v_next) {
                v_next = victim.next_in_room;
                if (!IS_NPC(victim) && (victim.level > ch.level) && (victim.level < ch.level + 10)) {
                    do_backstab(ch, victim.name);
                    if (ch.position != POS_FIGHTING) {
                        do_murder(ch, victim.name);
                    }
                    /* should steal some coins right away? :) */
                    return true;
                }
            }
            return false;    /*  No one to attack */
        }

        /* okay, we must be fighting.... steal some coins and flee */
        if ((victim = ch.fighting) == null) {
            return false;   /* let's be paranoid.... */
        }

        switch (number_bits(2)) {
            case 0 -> {
                act("$n rips apart your coin purse, spilling your gold!", ch, null, victim, TO_VICT);
                act("You slash apart $N's coin purse and gather his gold.", ch, null, victim, TO_CHAR);
                act("$N's coin purse is ripped apart!", ch, null, victim, TO_NOTVICT);
                gold = victim.gold / 10;  /* steal 10% of his gold */
                victim.gold -= gold;
                ch.gold += gold;
                return true;
            }
            case 1 -> {
                do_flee(ch);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    static boolean spec_questmaster(@NotNull CHAR_DATA ch) {
        if (!IS_AWAKE(ch)) {
            return false;
        }
        if (number_range(0, 100) == 0) {
            do_say(ch, "Don't you want a quest???.");
            return true;
        }
        return false;
    }

    static boolean spec_assassinater(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA v_next;
        int rnd_say;

        if (ch.fighting != null) {
            return false;
        }

        for (victim = ch.in_room.people; victim != null; victim = v_next) {
            /* this should kill mobs as well as players */
            v_next = ch.next_in_room;
            if ((victim.clazz != Clazz.THIEF) && (victim.clazz != Clazz.NINJA))
                /* thieves & ninjas*/ {
                break;
            }
        }

        if (victim == null || victim == ch || IS_IMMORTAL(victim)) {
            return false;
        }
        if (victim.level > ch.level + 7 || IS_NPC(victim)) {
            return false;
        }
        if (victim.hit < victim.max_hit) {
            return false;
        }

        rnd_say = number_range(1, 40);

        String buf;
        switch (rnd_say) {
            case 5 -> buf = "Death to is the true end...";
            case 6 -> buf = "Time to die....";
            case 7 -> buf = "Cabrone....";
            case 8 -> buf = "Welcome to your fate....";
            case 9 -> buf = "A sacrifice to immortals.. ";
            case 10 -> buf = "Ever dance with the devil....";
            default -> {
                return false;
            }
        }
        do_say(ch, buf);
        multi_hit(ch, victim, gsn_assassinate);
        return true;
    }


    static boolean spec_repairman(@NotNull CHAR_DATA ch) {
        if (!IS_AWAKE(ch)) {
            return false;
        }
        if (number_range(0, 100) == 0) {
            do_say(ch, "Now it is time to repair the other equipments.");
            return true;
        }
        return false;
    }

    static class spec_captain_data {
        static String open_path = "Wn0onc0oe1f2212211s2tw3xw3xd3322a22b22yO00d00a0011e1fe1fn0o3300300w3xs2ts2tS.";
        static String close_path = "Wn0on0oe1f2212211s2twc3xw3x3322d22a22EC0a00d0b0011e1fe1fn0o3300300w3xs2ts2tS.";
        static String path;
        static int pos;
        static boolean move;
    }

    static boolean spec_captain(@NotNull CHAR_DATA ch) {

        if (!spec_captain_data.move) {
            if (time_info.hour == 6) {
                spec_captain_data.path = spec_captain_data.open_path;
                spec_captain_data.move = true;
                spec_captain_data.pos = 0;
            }

            if (time_info.hour == 20) {
                spec_captain_data.path = spec_captain_data.close_path;
                spec_captain_data.move = true;
                spec_captain_data.pos = 0;
            }
        }

        if (ch.fighting != null) {
            return spec_cast_cleric(ch);
        }

        if (!spec_captain_data.move || ch.position < POS_SLEEPING) {
            return false;
        }

        switch (spec_captain_data.path.charAt(spec_captain_data.pos)) {
            case '0', '1', '2', '3' -> move_char(ch, spec_captain_data.path.charAt(spec_captain_data.pos) - '0');
            case 'W' -> {
                ch.position = POS_STANDING;
                act("{W$n awakens suddenly and yawns.{x", ch, null, null, TO_ROOM, POS_RESTING);
            }
            case 'S' -> {
                ch.position = POS_SLEEPING;
                act("{W$n lies down and falls asleep.{x", ch, null, null, TO_ROOM, POS_RESTING);
            }
            case 'a' -> act("{Y$n says 'Greetings! Good Hunting to you!'{x", ch, null, null, TO_ROOM, POS_RESTING);
            case 'b' -> act("{Y$n says 'Keep the streets clean please. Keep Solace tidy.'{x", ch, null, null, TO_ROOM, POS_RESTING);
            case 'c' -> {
                act("{Y$n says 'I must do something about all these doors.{x", ch, null, null, TO_ROOM, POS_RESTING);
                act("{Y$n says, 'I will never get out of here.'{x", ch, null, null, TO_ROOM, POS_RESTING);
            }
            case 'd' -> act("{Y$n says 'Salutations Citizens of Solace!'{x", ch, null, null, TO_ROOM, POS_RESTING);
            case 'y' -> act("{Y$n says 'I hereby declare the city of Solace open!'{x", ch, null, null, TO_ROOM, POS_RESTING);
            case 'E' -> act("{Y$n says 'I hereby declare the city of Solace closed!'{x", ch, null, null, TO_ROOM, POS_RESTING);
            case 'O' -> {
                do_unlock(ch, "gate");
                do_open(ch, "gate");
            }
            case 'C' -> {
                do_close(ch, "gate");
                do_lock(ch, "gate");
            }
            case 'n' -> do_open(ch, "north");
            case 'o' -> do_close(ch, "south");
            case 's' -> do_open(ch, "south");
            case 't' -> do_close(ch, "north");
            case 'e' -> do_open(ch, "east");
            case 'f' -> do_close(ch, "west");
            case 'w' -> do_open(ch, "west");
            case 'x' -> do_close(ch, "east");
            case '.' -> spec_captain_data.move = false;
        }

        spec_captain_data.pos++;
        return false;
    }


    static class spec_headlamia_data {
        static String path = "T111111100003332222232211.";
        static int pos = 0;
        static boolean move;
        static int count = 0;
    }

    static boolean spec_headlamia(@NotNull CHAR_DATA ch) {
        CHAR_DATA vch, vch_next;

        if (!spec_headlamia_data.move) {
            if (spec_headlamia_data.count++ == 10000) {
                spec_headlamia_data.move = true;
            }
        }

        if (ch.position < POS_SLEEPING || ch.fighting != null) {
            return false;
        }

        for (vch = ch.in_room.people; vch != null; vch = vch.next_in_room) {
            if (!IS_NPC(vch) && vch.pIndexData.vnum == 3143) {
                do_kill(ch, vch.name);
                break;
            }
        }

        if (!spec_headlamia_data.move) {
            return false;
        }

        switch (spec_headlamia_data.path.charAt(spec_headlamia_data.pos)) {
            case '0', '1', '2', '3' -> {
                move_char(ch, spec_headlamia_data.path.charAt(spec_headlamia_data.pos) - '0');
                spec_headlamia_data.pos++;
            }
            case 'T' -> {
                spec_headlamia_data.pos++;
                for (vch = char_list; vch != null; vch = vch_next) {
                    vch_next = vch.next;
                    if (!IS_NPC(vch)) {
                        continue;
                    }
                    if (vch.pIndexData.vnum == 5201) {
                        if (vch.fighting == null && vch.last_fought == null) {
                            char_from_room(vch);
                            char_to_room(vch, ch.in_room);
                            vch.master = ch;
                            vch.leader = ch;
                        }
                    }
                }
            }
            case '.' -> {
                spec_headlamia_data.move = false;
                spec_headlamia_data.count = 0;
                spec_headlamia_data.pos = 0;
            }
        }

        return false;
    }


    static boolean spec_cast_beholder(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA v_next;
        String spell;
        Skill sn;

        if (ch.position != POS_FIGHTING) {
            return false;
        }

        for (victim = ch.in_room.people; victim != null; victim = v_next) {
            v_next = victim.next_in_room;
            if (victim.fighting == ch && number_bits(1) == 0) {
                break;
            }
        }


        if (victim == null) {
            return false;
        }

        spell = switch (dice(1, 16)) {
            case 0 -> "fear";
            case 1 -> "fear";
            case 2 -> "slow";
            case 3 -> "cause serious";
            case 4 -> "cause critical";
            case 5 -> "harm";
            case 6 -> "harm";
            case 7 -> "dispel magic";
            case 8 -> "dispel magic";
            default -> "";
        };

        if ((sn = lookupSkill(spell)) == null) {
            return false;
        }
        sn.spell_fun(ch.level, ch, victim, TARGET_CHAR);
        return true;
    }

    static boolean spec_fight_enforcer(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA v_next;
        String spell;
        Skill sn;

        if (ch.position != POS_FIGHTING) {
            return false;
        }

        for (victim = ch.in_room.people; victim != null; victim = v_next) {
            v_next = victim.next_in_room;
            if (victim.fighting == ch && number_bits(1) == 0) {
                break;
            }
        }


        if (victim == null) {
            return false;
        }

        spell = switch (dice(1, 16)) {
            case 0, 1 -> "dispel magic";
            case 2, 3 -> "acid arrow";
            case 4, 5 -> "caustic font";
            case 6, 7, 8, 9, 10 -> "acid blast";
            default -> "";
        };

        if ((sn = lookupSkill(spell)) == null) {
            return false;
        }
        say_spell(ch, sn);
        sn.spell_fun(ch.level, ch, victim, TARGET_CHAR);
        return true;
    }


    static boolean spec_fight_invader(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA v_next;
        String spell;
        Skill sn;

        if (ch.position != POS_FIGHTING) {
            return false;
        }

        for (victim = ch.in_room.people; victim != null; victim = v_next) {
            v_next = victim.next_in_room;
            if (victim.fighting == ch && number_bits(1) == 0) {
                break;
            }
        }


        if (victim == null) {
            return false;
        }

        switch (dice(1, 16)) {
            case 0, 1 -> spell = "blindness";
            case 2, 3 -> spell = "dispel magic";
            case 4, 5 -> spell = "weaken";
            case 6, 7 -> spell = "energy drain";
            case 8, 9 -> spell = "plague";
            case 10, 11 -> spell = "acid arrow";
            case 12, 13, 14 -> spell = "acid blast";
            case 15 -> {
                if (ch.hit < (ch.max_hit / 3)) {
                    spell = "shadow cloak";
                } else {
                    spell = "";
                }
            }
            default -> spell = "";
        }

        if ((sn = lookupSkill(spell)) == null) {
            return false;
        }
        say_spell(ch, sn);
        sn.spell_fun(ch.level, ch, victim, TARGET_CHAR);
        return true;
    }


    static boolean spec_fight_ivan(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA v_next;
        String spell;
        Skill sn;

        if (ch.position != POS_FIGHTING) {
            return false;
        }

        for (victim = ch.in_room.people; victim != null; victim = v_next) {
            v_next = victim.next_in_room;
            if (victim.fighting == ch && number_bits(1) == 0) {
                break;
            }
        }


        if (victim == null) {
            return false;
        }

        switch (dice(1, 16)) {
            case 0, 1 -> spell = "dispel magic";
            case 2, 3 -> spell = "acid arrow";
            case 4, 5 -> spell = "caustic font";
            case 6, 7, 8 -> spell = "acid blast";
            case 9 -> spell = "disgrace";
            case 10 -> {
                if (ch.hit < (ch.max_hit / 3)) {
                    spell = "garble";
                } else {
                    spell = "";
                }
            }
            default -> spell = "";
        }

        if ((sn = lookupSkill(spell)) == null) {
            return false;
        }
        say_spell(ch, sn);
        sn.spell_fun(ch.level, ch, victim, TARGET_CHAR);
        return true;
    }


    static boolean spec_fight_seneschal(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA v_next;
        String spell;
        Skill sn;

        if (ch.position != POS_FIGHTING) {
            return false;
        }

        for (victim = ch.in_room.people; victim != null; victim = v_next) {
            v_next = victim.next_in_room;
            if (victim.fighting == ch && number_bits(1) == 0) {
                break;
            }
        }


        if (victim == null) {
            return false;
        }

        spell = switch (dice(1, 16)) {
            case 0 -> "blindness";
            case 1 -> "dispel magic";
            case 2 -> "weaken";
            case 3 -> "blindness";
            case 4 -> "acid arrow";
            case 5 -> "caustic font";
            case 6 -> "energy drain";
            case 7, 8, 9 -> "acid blast";
            case 10 -> "plague";
            case 11 -> "acid blast";
            case 12, 13 -> "lightning breath";
            case 14, 15 -> "mental knife";
            default -> "";
        };

        if ((sn = lookupSkill(spell)) == null) {
            return false;
        }
        say_spell(ch, sn);
        sn.spell_fun(ch.level, ch, victim, TARGET_CHAR);
        return true;
    }

    static boolean spec_fight_powerman(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA v_next;

        if (ch.position != POS_FIGHTING) {
            return false;
        }

        ch.cabal = CABAL_BATTLE;

        if (!is_affected(ch, gsn_spellbane)) {
            do_spellbane(ch);
        }

        for (victim = ch.in_room.people; victim != null; victim = v_next) {
            v_next = victim.next_in_room;
            if (victim.fighting == ch && number_bits(1) == 0) {
                break;
            }
        }

        if (victim == null) {
            return false;
        }

        if (number_percent() < 33) {
            act("You deliver triple blows of deadly force!", ch, null, null, TO_CHAR);
            act("$n delivers triple blows of deadly force!", ch, null, null, TO_ROOM);
            one_hit(ch, victim, null, false);
            one_hit(ch, victim, null, false);
            one_hit(ch, victim, null, false);
        }

        if (!is_affected(ch, gsn_resistance)) {
            do_resistance(ch);
        }

        if (ch.hit < (ch.max_hit / 3) && !IS_AFFECTED(ch, AFF_REGENERATION)) {
            do_bandage(ch);
        }

        return true;
    }


    static boolean spec_fight_protector(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA v_next;
        String spell;
        Skill sn;

        if (ch.position != POS_FIGHTING) {
            return false;
        }

        for (victim = ch.in_room.people; victim != null; victim = v_next) {
            v_next = victim.next_in_room;
            if (victim.fighting == ch && number_bits(1) == 0) {
                break;
            }
        }


        if (victim == null) {
            return false;
        }

        spell = switch (dice(1, 16)) {
            case 0, 1 -> "dispel magic";
            case 2, 3 -> "acid arrow";
            case 4, 5 -> "caustic font";
            case 6, 7, 8, 9, 10 -> "acid blast";
            default -> "";
        };

        if ((sn = lookupSkill(spell)) == null) {
            return false;
        }
        say_spell(ch, sn);
        sn.spell_fun(ch.level, ch, victim, TARGET_CHAR);
        return true;
    }

    static boolean spec_fight_lionguard(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA v_next;
        String spell;
        Skill sn;

        if (ch.position != POS_FIGHTING) {
            return false;
        }

        for (victim = ch.in_room.people; victim != null; victim = v_next) {
            v_next = victim.next_in_room;
            if (victim.fighting == ch && number_bits(1) == 0) {
                break;
            }
        }
        if (victim == null) {
            return false;
        }

        if (number_percent() < 33) {
            int damage_claw;

            damage_claw = dice(ch.level, 24) + ch.damroll;
            damage(ch, victim, damage_claw, gsn_claw, DAM_BASH, true);
            return true;
        }

        spell = switch (dice(1, 16)) {
            case 0, 1 -> "dispel magic";
            case 2, 3 -> "acid blast";
            case 4, 5 -> "caustic font";
            case 6, 7, 8 -> "acid arrow";
            default -> "";
        };

        if ((sn = lookupSkill(spell)) == null) {
            return false;
        }
        say_spell(ch, sn);
        sn.spell_fun(ch.level, ch, victim, TARGET_CHAR);
        return true;
    }


    static boolean spec_fight_hunter(@NotNull CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA v_next;
        String spell;
        Skill sn;

        if (ch.position != POS_FIGHTING) {
            return false;
        }

        for (victim = ch.in_room.people; victim != null; victim = v_next) {
            v_next = victim.next_in_room;
            if (victim.fighting == ch && number_bits(1) == 0) {
                break;
            }
        }

        spell = switch (dice(1, 16)) {
            case 0, 1 -> "dispel magic";
            case 2, 3 -> "acid arrow";
            case 4, 5 -> "caustic font";
            case 6, 7, 8, 9 -> "acid blast";
            default -> "";
        };

        if (victim == null) {
            return false;
        }

        if ((sn = lookupSkill(spell)) == null) {
            return false;
        }
        say_spell(ch, sn);
        sn.spell_fun(ch.level, ch, victim, TARGET_CHAR);
        return true;
    }

    /* the function table */
    static final HashMap<String, SPEC_FUN> spec_table = new HashMap<>();

    static {
        SPEC_FUN_IMPL.init();
    }
}

