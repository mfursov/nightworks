package net.sf.nightworks;

import java.util.Formatter;

import static net.sf.nightworks.ActComm.do_quit;
import static net.sf.nightworks.ActComm.do_yell;
import static net.sf.nightworks.ActHera.auction_update;
import static net.sf.nightworks.ActHera.hunt_victim;
import static net.sf.nightworks.ActInfo.set_title;
import static net.sf.nightworks.ActMove.*;
import static net.sf.nightworks.ActObj.can_loot;
import static net.sf.nightworks.ActObj.do_quaff;
import static net.sf.nightworks.ActSkill.*;
import static net.sf.nightworks.ActWiz.reboot_nightworks;
import static net.sf.nightworks.ActWiz.wiznet;
import static net.sf.nightworks.Comm.*;
import static net.sf.nightworks.Const.con_app;
import static net.sf.nightworks.Const.wis_app;
import static net.sf.nightworks.DB.*;
import static net.sf.nightworks.Fight.*;
import static net.sf.nightworks.Handler.*;
import static net.sf.nightworks.Magic.saves_spell;
import static net.sf.nightworks.MartialArt.check_guard;
import static net.sf.nightworks.MartialArt.do_spellbane;
import static net.sf.nightworks.Nightworks.*;
import static net.sf.nightworks.Quest.quest_update;
import static net.sf.nightworks.Save.save_char_obj;
import static net.sf.nightworks.Skill.*;

public class Update {
    /* used for saving */

    private static int save_number = 0;

    /*
     * Advancement stuff.
     */


    static void advance_level(CHAR_DATA ch) {

        int add_hp;
        int add_mana;
        int add_move;
        int add_prac;

        if (IS_NPC(ch)) {
            bug("Advance_level: a mob to advance!");
            return;
        }

        ch.pcdata.last_level =
                (ch.played + (int) (current_time - ch.logon)) / 3600;

        if (ch.pcdata.title.contains(ch.clazz.getTitle(ch.level - 1, ch.sex)) || CANT_CHANGE_TITLE(ch)) {
            set_title(ch, "the" + ch.clazz.getTitle(ch.level, ch.sex));
        }

        add_hp = (con_app[get_curr_stat(ch, STAT_CON)].hitp + number_range(1, 5)) - 3;
        add_hp = (add_hp * ch.clazz.hp_rate) / 100;
        add_mana = number_range(get_curr_stat(ch, STAT_INT) / 2, (2 * get_curr_stat(ch, STAT_INT) + get_curr_stat(ch, STAT_WIS) / 5));
        add_mana = (add_mana * ch.clazz.mana_rate) / 100;

        add_move = number_range(1, (get_curr_stat(ch, STAT_CON) + 2 * get_curr_stat(ch, STAT_DEX)) / 6);
        add_prac = wis_app[get_curr_stat(ch, STAT_WIS)].practice;

        add_hp = UMAX(3, add_hp);
        add_mana = UMAX(3, add_mana);
        add_move = UMAX(6, add_move);

        if (ch.sex == SEX_FEMALE) {
            add_hp -= 1;
            add_mana += 2;
        }

        ch.max_hit += add_hp;
        ch.max_mana += add_mana;
        ch.max_move += add_move;
        ch.practice += add_prac;
        ch.train += ch.level % 5 == 0 ? 1 : 0;

        ch.pcdata.perm_hit += add_hp;
        ch.pcdata.perm_mana += add_mana;
        ch.pcdata.perm_move += add_move;

        var f = new Formatter().format("You gain: {W%d{x hp, {W%d{x mana, {W%d{x mv {W%d{x prac.\n", add_hp, add_mana, add_move, add_prac);
        send_to_char(f.toString(), ch);
    }


    static void gain_exp(CHAR_DATA ch, int gain) {

        if (IS_NPC(ch) || ch.level >= LEVEL_HERO) {
            return;
        }
        if (IS_SET(ch.act, PLR_NO_EXP)) {
            send_to_char("You can't gain exp without your spirit.\n", ch);
            return;
        }
/*
    ch.exp = UMAX( exp_per_level(ch,ch.pcdata.points), ch.exp + gain );
    while ( ch.level < LEVEL_HERO && ch.exp >=
    exp_per_level(ch,ch.pcdata.points) * (ch.level+1) )
*/

        ch.exp = UMAX(base_exp(ch, ch.pcdata.points), ch.exp + gain);
        while (ch.level < LEVEL_HERO &&
                exp_to_level(ch, ch.pcdata.points) <= 0) {
            send_to_char("You raise a level!!  ", ch);
            ch.level += 1;

            /* added for samurais by chronos */
            if ((ch.clazz == Clazz.SAMURAI) && (ch.level == 10)) {
                ch.wimpy = 0;
            }

            /* Level counting */
            if (ch.level > 5) {
                total_levels++;
            }

            if (ch.level == 90) {
                log_string(ch.name + " made level 90.");
            }

            wiznet("$N has attained level " + ch.level + "!", ch, null, WIZ_LEVELS, 0, 0);
            advance_level(ch);
            save_char_obj(ch);
        }
    }

    /*
     * Regeneration stuff.
     */

    static int hit_gain(CHAR_DATA ch) {
        int gain;
        int number;

        if (ch.in_room == null) {
            return 0;
        }

        if (IS_NPC(ch)) {
            gain = 5 + ch.level;
            if (IS_AFFECTED(ch, AFF_REGENERATION)) {
                gain *= 2;
            }

            switch (ch.position) {
                default -> gain /= 2;
                case POS_SLEEPING -> gain = 3 * gain / 2;
                case POS_RESTING -> {
                }
                case POS_FIGHTING -> gain /= 3;
            }


        } else {
            gain = UMAX(3, 2 * get_curr_stat(ch, STAT_CON) + (7 * ch.level) / 4);
            gain = (gain * ch.clazz.hp_rate) / 100;
            number = number_percent();
            if (number < get_skill(ch, gsn_fast_healing)) {
                gain += number * gain / 100;
                if (ch.hit < ch.max_hit) {
                    check_improve(ch, gsn_fast_healing, true, 8);
                }
            }

            if (number < get_skill(ch, gsn_trance)) {
                gain += number * gain / 150;
                if (ch.mana < ch.max_mana) {
                    check_improve(ch, gsn_trance, true, 8);
                }
            }
            switch (ch.position) {
                default -> gain /= 4;
                case POS_SLEEPING -> {
                }
                case POS_RESTING -> gain /= 2;
                case POS_FIGHTING -> gain /= 6;
            }

            if (ch.pcdata.condition[COND_HUNGER] < 0) {
                gain = 0;
            }

            if (ch.pcdata.condition[COND_THIRST] < 0) {
                gain = 0;
            }

        }

        gain = gain * ch.in_room.heal_rate / 100;

        if (ch.on != null && ch.on.item_type == ITEM_FURNITURE) {
            gain = gain * ch.on.value[3] / 100;
        }

        if (IS_AFFECTED(ch, AFF_POISON)) {
            gain /= 4;
        }

        if (IS_AFFECTED(ch, AFF_PLAGUE)) {
            gain /= 8;
        }

        if (IS_AFFECTED(ch, AFF_HASTE)) {
            gain /= 2;
        }

        if (IS_AFFECTED(ch, AFF_SLOW)) {
            gain *= 2;
        }

        if (get_curr_stat(ch, STAT_CON) > 20) {
            gain = (gain * 14) / 10;
        }

        if (IS_HARA_KIRI(ch)) {
            gain *= 3;
        }

        return UMIN(gain, ch.max_hit - ch.hit);
    }


    static int mana_gain(CHAR_DATA ch) {
        int gain;
        int number;

        if (ch.in_room == null) {
            return 0;
        }

        if (IS_NPC(ch)) {
            gain = 5 + ch.level;
            switch (ch.position) {
                default -> gain /= 2;
                case POS_SLEEPING -> gain = 3 * gain / 2;
                case POS_RESTING -> {
                }
                case POS_FIGHTING -> gain /= 3;
            }
        } else {
            gain = get_curr_stat(ch, STAT_WIS) + (2 * get_curr_stat(ch, STAT_INT)) + ch.level;
            gain = (gain * ch.clazz.mana_rate) / 100;
            number = number_percent();
            if (number < get_skill(ch, gsn_meditation)) {
                gain += number * gain / 100;
                if (ch.mana < ch.max_mana) {
                    check_improve(ch, gsn_meditation, true, 8);
                }
            }

            if (number < get_skill(ch, gsn_trance)) {
                gain += number * gain / 100;
                if (ch.mana < ch.max_mana) {
                    check_improve(ch, gsn_trance, true, 8);
                }
            }

            if (!ch.clazz.fMana) {
                gain /= 2;
            }

            switch (ch.position) {
                default -> gain /= 4;
                case POS_SLEEPING -> {
                }
                case POS_RESTING -> gain /= 2;
                case POS_FIGHTING -> gain /= 6;
            }

            if (ch.pcdata.condition[COND_HUNGER] < 0) {
                gain = 0;
            }

            if (ch.pcdata.condition[COND_THIRST] < 0) {
                gain = 0;
            }

        }

        gain = gain * ch.in_room.mana_rate / 100;

        if (ch.on != null && ch.on.item_type == ITEM_FURNITURE) {
            gain = gain * ch.on.value[4] / 100;
        }

        if (IS_AFFECTED(ch, AFF_POISON)) {
            gain /= 4;
        }

        if (IS_AFFECTED(ch, AFF_PLAGUE)) {
            gain /= 8;
        }

        if (IS_AFFECTED(ch, AFF_HASTE)) {
            gain /= 2;
        }
        if (IS_AFFECTED(ch, AFF_SLOW)) {
            gain *= 2;
        }
        if (get_curr_stat(ch, STAT_INT) > 20) {
            gain = (gain * 13) / 10;
        }
        if (get_curr_stat(ch, STAT_WIS) > 20) {
            gain = (gain * 11) / 10;
        }
        if (IS_HARA_KIRI(ch)) {
            gain *= 3;
        }

        return UMIN(gain, ch.max_mana - ch.mana);
    }


    static int move_gain(CHAR_DATA ch) {
        int gain;

        if (ch.in_room == null) {
            return 0;
        }

        if (IS_NPC(ch)) {
            gain = ch.level;
        } else {
            gain = UMAX(15, 2 * ch.level);

            switch (ch.position) {
                case POS_SLEEPING -> gain += 2 * (get_curr_stat(ch, STAT_DEX));
                case POS_RESTING -> gain += get_curr_stat(ch, STAT_DEX);
            }

            if (ch.pcdata.condition[COND_HUNGER] < 0) {
                gain = 3;
            }

            if (ch.pcdata.condition[COND_THIRST] < 0) {
                gain = 3;
            }
        }

        gain = gain * ch.in_room.heal_rate / 100;

        if (ch.on != null && ch.on.item_type == ITEM_FURNITURE) {
            gain = gain * ch.on.value[3] / 100;
        }

        if (IS_AFFECTED(ch, AFF_POISON)) {
            gain /= 4;
        }

        if (IS_AFFECTED(ch, AFF_PLAGUE)) {
            gain /= 8;
        }

        if (IS_AFFECTED(ch, AFF_HASTE) || IS_AFFECTED(ch, AFF_SLOW)) {
            gain /= 2;
        }
        if (get_curr_stat(ch, STAT_DEX) > 20) {
            gain *= (14 / 10);
        }
        if (IS_HARA_KIRI(ch)) {
            gain *= 3;
        }

        return UMIN(gain, ch.max_move - ch.move);
    }


    static void gain_condition(CHAR_DATA ch, int iCond, int value) {
        int condition;
        int damage_hunger;
        CHAR_DATA vch, vch_next;

        if (value == 0 || IS_NPC(ch) || ch.level >= LEVEL_IMMORTAL) {
            return;
        }

        condition = ch.pcdata.condition[iCond];

        ch.pcdata.condition[iCond] = URANGE(-6, condition + value, 96);

        if (iCond == COND_FULL && (ch.pcdata.condition[COND_FULL] < 0)) {
            ch.pcdata.condition[COND_FULL] = 0;
        }

        if ((iCond == COND_DRUNK) && (condition < 1)) {
            ch.pcdata.condition[COND_DRUNK] = 0;
        }

        if (ch.pcdata.condition[iCond] < 1 && ch.pcdata.condition[iCond] > -6) {
            switch (iCond) {
                case COND_HUNGER -> send_to_char("You are hungry.\n", ch);
                case COND_THIRST -> send_to_char("You are thirsty.\n", ch);
                case COND_DRUNK -> {
                    if (condition != 0) {
                        send_to_char("You are sober.\n", ch);
                    }
                }
                case COND_BLOODLUST -> {
                    if (condition != 0) {
                        send_to_char("You are hungry for blood.\n", ch);
                    }
                }
                case COND_DESIRE -> {
                    if (condition != 0) {
                        send_to_char("You have missed your home.\n", ch);
                    }
                }
            }
        }

        if (ch.pcdata.condition[iCond] == -6 && ch.level >= PK_MIN_LEVEL) {
            switch (iCond) {
                case COND_HUNGER -> {
                    send_to_char("You are starving!\n", ch);
                    act("$n is starving!", ch, null, null, TO_ROOM);
                    damage_hunger = ch.max_hit * number_range(2, 4) / 100;
                    if (damage_hunger == 0) {
                        damage_hunger = 1;
                    }
                    damage(ch, ch, damage_hunger, gsn_x_hunger, DAM_HUNGER, true);
                    if (ch.position == POS_SLEEPING) {
                    }
                }
                case COND_THIRST -> {
                    send_to_char("You are dying of thrist!\n", ch);
                    act("$n is dying of thirst!", ch, null, null, TO_ROOM);
                    damage_hunger = ch.max_hit * number_range(2, 4) / 100;
                    if (damage_hunger == 0) {
                        damage_hunger = 1;
                    }
                    damage(ch, ch, damage_hunger, gsn_x_hunger, DAM_THIRST, true);
                    if (ch.position == POS_SLEEPING) {
                    }
                }
                case COND_BLOODLUST -> {
                    var fdone = false;
                    send_to_char("You are suffering from thrist of blood!\n", ch);
                    act("$n is suffering from thirst of blood!", ch, null, null, TO_ROOM);
                    if (ch.in_room != null && ch.in_room.people != null && ch.fighting != null) {
                        if (!IS_AWAKE(ch)) {
                            do_stand(ch, "");
                        }
                        for (vch = ch.in_room.people;
                             vch != null && ch.fighting == null; vch = vch_next) {
                            vch_next = vch.next_in_room;
                            if (ch != vch && can_see(ch, vch) && !is_safe_nomessage(ch, vch)) {
                                do_yell(ch, "BLOOD! I NEED BLOOD!");
                                do_murder(ch, vch.name);
                                fdone = true;
                                break;
                            }
                        }
                    }
                    if (fdone) {
                        break;
                    }
                    damage_hunger = ch.max_hit * number_range(2, 4) / 100;
                    if (damage_hunger == 0) {
                        damage_hunger = 1;
                    }
                    damage(ch, ch, damage_hunger, gsn_x_hunger, DAM_THIRST, true);
                    if (ch.position == POS_SLEEPING) {
                    }
                }
                case COND_DESIRE -> {
                    send_to_char("You want to go your home!\n", ch);
                    act("$n desires for $s home!", ch, null, null, TO_ROOM);
                    if (ch.position >= POS_STANDING) {
                        move_char(ch, number_door());
                    }
                }
            }
        }


    }

    /*
     * Mob autonomous action.
     * This function takes 25% to 35% of ALL Merc cpu time.
     * -- Furey
     */

    static void mobile_update() {
        CHAR_DATA ch;
        CHAR_DATA ch_next;
        EXIT_DATA pexit;
        int door;
        OBJ_DATA obj;

        /* Examine all mobs. */
        for (ch = char_list; ch != null; ch = ch_next) {
            ch_next = ch.next;

            if (IS_AFFECTED(ch, AFF_REGENERATION) && ch.in_room != null) {
                ch.hit = UMIN(ch.hit + ch.level / 10, ch.max_hit);
                if (ch.race == Race.TROLL) {
                    ch.hit = UMIN(ch.hit + ch.level / 10, ch.max_hit);
                }
                if (ch.cabal == CABAL_BATTLE && is_affected(ch, gsn_bandage)) {
                    ch.hit = UMIN(ch.hit + ch.level / 10, ch.max_hit);
                }
                if (ch.hit != ch.max_hit) {
                    send_to_char("", ch);
                }
            }

            if (IS_AFFECTED(ch, AFF_CORRUPTION) && ch.in_room != null) {
                ch.hit -= ch.level / 10;
                if (ch.hit < 1) {
                    ch.hit = 1;
                    damage(ch, ch, 16, Skill.gsn_corruption, DAM_NONE, false);
                    continue;
                } else {
                    send_to_char("", ch);
                }
            }

            if (IS_AFFECTED(ch, AFF_SUFFOCATE) && ch.in_room != null) {
                ch.hit -= ch.level / 5;
                if (ch.hit < 1) {
                    ch.hit = 1;
                    damage(ch, ch, 16, Skill.gsn_suffocate, DAM_NONE, false);
                    continue;
                } else {
                    if (number_percent() < 30) {
                        send_to_char("You cannot breath!", ch);
                    }
                }
            }

            if (ch.spec_fun == Special.spec_lookup("spec_special_guard")) {
                if (ch.spec_fun.run(ch)) {
                    continue;
                }
            }

            if (!IS_NPC(ch) || ch.in_room == null || IS_AFFECTED(ch, AFF_CHARM)) {
                continue;
            }

            if (IS_SET(ch.act, ACT_HUNTER) && ch.hunting != null && ch.fighting == null) {
                hunt_victim(ch);
            }

            if (ch.in_room.area.empty && !IS_SET(ch.act, ACT_UPDATE_ALWAYS)) {
                continue;
            }

            /* Examine call for special procedure */
            if (ch.spec_fun != null) {
                if (ch.spec_fun.run(ch)) {
                    continue;
                }
            }

            if (ch.pIndexData.pShop != null) {/* give him some gold */
                if ((ch.gold * 100 + ch.silver) < ch.pIndexData.wealth) {
                    ch.gold += ch.pIndexData.wealth * number_range(1, 20) / 5000000;
                    ch.silver += ch.pIndexData.wealth * number_range(1, 20) / 50000;
                }
            }

            /*
             *  Potion using and stuff for intelligent mobs
             */

            if (ch.position == POS_STANDING || ch.position == POS_RESTING || ch.position == POS_FIGHTING) {
                if (get_curr_stat(ch, STAT_INT) > 15 &&
                        (ch.hit < ch.max_hit * 0.9 ||
                                IS_AFFECTED(ch, AFF_BLIND) ||
                                IS_AFFECTED(ch, AFF_POISON) ||
                                IS_AFFECTED(ch, AFF_PLAGUE) || ch.fighting != null)) {
                    for (obj = ch.carrying; obj != null; obj = obj.next_content) {
                        if (obj.item_type == ITEM_POTION) {
                            if (ch.hit < ch.max_hit * 0.9)  /* hp curies */ {
                                int cl;
                                cl = potion_cure_level(obj);
                                if (cl > 0) {
                                    if (ch.hit < ch.max_hit * 0.5 && cl > 3) {
                                        do_quaff(ch, obj.name);
                                        continue;
                                    } else if (ch.hit < ch.max_hit * 0.7) {
                                        do_quaff(ch, obj.name);
                                        continue;
                                    }
                                }
                            }
                            if (IS_AFFECTED(ch, AFF_POISON) && potion_cure_poison(obj)) {
                                do_quaff(ch, obj.name);
                                continue;
                            }
                            if (IS_AFFECTED(ch, AFF_PLAGUE) && potion_cure_disease(obj)) {
                                do_quaff(ch, obj.name);
                                continue;
                            }
                            if (IS_AFFECTED(ch, AFF_BLIND) && potion_cure_blind(obj)) {
                                do_quaff(ch, obj.name);
                                continue;
                            }
                            if (ch.fighting != null) {
                                int al;
                                al = potion_arm_level(obj);
                                if (ch.level - ch.fighting.level < 7 && al > 3) {
                                    do_quaff(ch, obj.name);
                                    continue;
                                }
                                if (ch.level - ch.fighting.level < 8 && al > 2) {
                                    do_quaff(ch, obj.name);
                                    continue;
                                }
                                if (ch.level - ch.fighting.level < 9 && al > 1) {
                                    do_quaff(ch, obj.name);
                                    continue;
                                }
                                if (ch.level - ch.fighting.level < 10 && al > 0) {
                                    do_quaff(ch, obj.name);
                                }
                            }
                        }
                    }
                }
            }

            /* That's all for sleeping / busy monster, and empty zones */
            if (ch.position != POS_STANDING) {
                continue;
            }

            if (IS_SET(ch.progtypes, MPROG_AREA) && (ch.in_room.area.nplayer > 0)) {
                ch.pIndexData.mprogs.area_prog.run(ch);
            }


            if (ch.position < POS_STANDING) {
                continue;
            }

            /* Scavenge */
            if (IS_SET(ch.act, ACT_SCAVENGER) && ch.in_room.contents != null && number_bits(6) == 0) {
                OBJ_DATA obj_best = null;
                var max = 1;
                for (var tobj = ch.in_room.contents; tobj != null; tobj = tobj.next_content) {
                    if (CAN_WEAR(tobj, ITEM_TAKE) && can_loot(ch, tobj)
                            && tobj.cost > max && tobj.cost > 0) {
                        obj_best = tobj;
                        max = tobj.cost;
                    }
                }

                if (obj_best != null) {
                    obj_from_room(obj_best);
                    obj_to_char(obj_best, ch);
                    act("$n gets $p.", ch, obj_best, null, TO_ROOM);
                    if (IS_SET(obj_best.progtypes, OPROG_GET)) {
                        obj_best.pIndexData.oprogs.get_prog.run(obj_best, ch);
                    }
                }
            }

            /* Wander */
            if (!IS_SET(ch.act, ACT_SENTINEL)
                    && number_bits(3) == 0
                    && (door = number_bits(5)) <= 5
                    && RIDDEN(ch) == null
                    && (pexit = ch.in_room.exit[door]) != null
                    && pexit.to_room != null
                    && !IS_SET(pexit.exit_info, EX_CLOSED)
                    && !IS_SET(pexit.to_room.room_flags, ROOM_NO_MOB)
                    && (!IS_SET(ch.act, ACT_STAY_AREA)
                    || pexit.to_room.area == ch.in_room.area)
                    && (!IS_SET(ch.act, ACT_OUTDOORS)
                    || !IS_SET(pexit.to_room.room_flags, ROOM_INDOORS))
                    && (!IS_SET(ch.act, ACT_INDOORS)
                    || IS_SET(pexit.to_room.room_flags, ROOM_INDOORS))) {
                move_char(ch, door);
            }
        }

    }

    static int potion_cure_level(OBJ_DATA potion) {
        int cl;
        int i;
        cl = 0;
        for (i = 1; i < 5; i++) {
            if (Skill.gsn_cure_critical.ordinal() == potion.value[i]) {
                cl += 3;
            }
            if (Skill.gsn_cure_light.ordinal() == potion.value[i]) {
                cl += 1;
            }
            if (Skill.gsn_cure_serious.ordinal() == potion.value[i]) {
                cl += 2;
            }
            if (Skill.gsn_heal.ordinal() == potion.value[i]) {
                cl += 4;
            }
        }
        return cl;
    }

    static int potion_arm_level(OBJ_DATA potion) {
        var al = 0;
        for (var i = 1; i < 5; i++) {
            if (Skill.gsn_armor.ordinal() == potion.value[i]) {
                al += 1;
            }
            if (Skill.gsn_shield.ordinal() == potion.value[i]) {
                al += 1;
            }
            if (Skill.gsn_stone_skin.ordinal() == potion.value[i]) {
                al += 2;
            }
            if (Skill.gsn_sanctuary.ordinal() == potion.value[i]) {
                al += 4;
            }
            /*TODO: if (Skill.gsn_protection.ordinal() == potion.value[i]) {
                al += 3;
            }*/
        }
        return al;
    }

    static boolean potion_cure_blind(OBJ_DATA potion) {
        for (var i = 0; i < 5; i++) {
            if (Skill.gsn_cure_blindness.ordinal() == potion.value[i]) {
                return true;
            }
        }
        return false;
    }

    static boolean potion_cure_poison(OBJ_DATA potion) {
        for (var i = 0; i < 5; i++) {
            if (Skill.gsn_cure_poison.ordinal() == potion.value[i]) {
                return true;
            }
        }
        return false;
    }

    static boolean potion_cure_disease(OBJ_DATA potion) {
        for (var i = 0; i < 5; i++) {
            if (Skill.gsn_cure_disease.ordinal() == potion.value[i]) {
                return true;
            }
        }
        return false;
    }

    /*
     * Update the weather.
     */

    static void weather_update() {
        DESCRIPTOR_DATA d;
        int diff;

        var buf = new StringBuilder();
        if (++time_info.bmin == 2) {
            time_info.bmin = 0;
            time_info.hour++;
        }

        switch (time_info.hour) {
            case 5 -> {
                weather_info.sunlight = SUN_LIGHT;
                buf.append("The day has begun.\n");
            }
            case 6 -> {
                weather_info.sunlight = SUN_RISE;
                buf.append("The sun rises in the east.\n");
            }
            case 19 -> {
                weather_info.sunlight = SUN_SET;
                buf.append("The sun slowly disappears in the west.\n");
            }
            case 20 -> {
                weather_info.sunlight = SUN_DARK;
                buf.append("The night has begun.\n");
            }
            case 24 -> {
                time_info.hour = 0;
                time_info.day++;
            }
        }

        if (time_info.day >= 35) {
            time_info.day = 0;
            time_info.month++;
        }

        if (time_info.month >= 17) {
            time_info.month = 0;
            time_info.year++;
        }

        /*
         * Weather change.
         */
        if (time_info.month >= 9 && time_info.month <= 16) {
            diff = weather_info.mmhg > 985 ? -2 : 2;
        } else {
            diff = weather_info.mmhg > 1015 ? -2 : 2;
        }

        weather_info.change += diff * dice(1, 4) + dice(2, 6) - dice(2, 6);
        weather_info.change = UMAX(weather_info.change, -12);
        weather_info.change = UMIN(weather_info.change, 12);

        weather_info.mmhg += weather_info.change;
        weather_info.mmhg = UMAX(weather_info.mmhg, 960);
        weather_info.mmhg = UMIN(weather_info.mmhg, 1040);

        switch (weather_info.sky) {
            default -> {
                bug("Weather_update: bad sky %d.", weather_info.sky);
                weather_info.sky = SKY_CLOUDLESS;
            }
            case SKY_CLOUDLESS -> {
                if (weather_info.mmhg < 990
                        || (weather_info.mmhg < 1010 && number_bits(2) == 0)) {
                    buf.append("The sky is getting cloudy.\n");
                    weather_info.sky = SKY_CLOUDY;
                }
            }
            case SKY_CLOUDY -> {
                if (weather_info.mmhg < 970
                        || (weather_info.mmhg < 990 && number_bits(2) == 0)) {
                    buf.append("It starts to rain.\n");
                    weather_info.sky = SKY_RAINING;
                }
                if (weather_info.mmhg > 1030 && number_bits(2) == 0) {
                    buf.append("The clouds disappear.\n");
                    weather_info.sky = SKY_CLOUDLESS;
                }
            }
            case SKY_RAINING -> {
                if (weather_info.mmhg < 970 && number_bits(2) == 0) {
                    buf.append("Lightning flashes in the sky.\n");
                    weather_info.sky = SKY_LIGHTNING;
                }
                if (weather_info.mmhg > 1030 || (weather_info.mmhg > 1010 && number_bits(2) == 0)) {
                    buf.append("The rain stopped.\n");
                    weather_info.sky = SKY_CLOUDY;
                }
            }
            case SKY_LIGHTNING -> {
                if (weather_info.mmhg > 1010 || (weather_info.mmhg > 990 && number_bits(2) == 0)) {
                    buf.append("The lightning has stopped.\n");
                    weather_info.sky = SKY_RAINING;
                }
            }
        }

        if (!buf.isEmpty()) {
            for (d = descriptor_list; d != null; d = d.next) {
                if (d.connected == CON_PLAYING
                        && IS_OUTSIDE(d.character)
                        && IS_AWAKE(d.character)) {
                    send_to_char(buf, d.character);
                }
            }
        }

    }

    /*
     * Update all chars, including mobs.
     */
    private static int char_update_last_save_time = -1;

    static void char_update() {
        CHAR_DATA ch;
        CHAR_DATA ch_next;
        CHAR_DATA ch_quit;


        boolean fTimeSync;
        int l;

        ch_quit = null;

        /* update save counter */
        save_number++;

        if (save_number > 29) {
            save_number = 0;
        }

        fTimeSync = check_time_sync();

        for (ch = char_list; ch != null; ch = ch_next) {
            AFFECT_DATA paf;
            AFFECT_DATA paf_next;

            ch_next = ch.next;

            /* reset hunters path find */
            if (!IS_NPC(ch)) {
                /* Time Sync due Midnight */
                if (fTimeSync) {
                    for (l = nw_config.max_time_log - 1; l > 0; l--) {
                        ch.pcdata.log_time[l] = ch.pcdata.log_time[l - 1];
                    }

                    /* Nothing for today */
                    ch.pcdata.log_time[0] = 0;
                }

                if (ch.cabal == CABAL_HUNTER) {
                    if (number_percent() < get_skill(ch, gsn_path_find)) {
                        ch.endur += (get_skill(ch, gsn_path_find) / 2);
                        check_improve(ch, gsn_path_find, true, 8);
                    } else {
                        check_improve(ch, gsn_path_find, false, 16);
                    }
                }

                if (ch.cabal == CABAL_BATTLE && !is_affected(ch, gsn_spellbane)) {
                    do_spellbane(ch);
                }
            }

            /* Remove caltraps effect after fight off */
            if (is_affected(ch, gsn_caltraps) && ch.fighting == null) {
                affect_strip(ch, gsn_caltraps);
            }

            /* Remove vampire effect when morning. */
            if (IS_VAMPIRE(ch) &&
                    (weather_info.sunlight == SUN_LIGHT ||
                            weather_info.sunlight == SUN_RISE)) {
                do_human(ch);
            }

            /* Reset sneak for vampire */
            if (ch.fighting == null && !IS_AFFECTED(ch, AFF_SNEAK) &&
                    IS_VAMPIRE(ch) && MOUNTED(ch) == null) {
                send_to_char("You begin to sneak again.\n", ch);
                ch.affected_by = SET_BIT(ch.affected_by, AFF_SNEAK);
            }

            if (ch.fighting == null && !IS_AFFECTED(ch, AFF_SNEAK) &&
                    (ch.race.aff & AFF_SNEAK) != 0 && MOUNTED(ch) == null) {
                send_to_char("You begin to sneak again.\n", ch);
            }

            if (ch.fighting == null && !IS_AFFECTED(ch, AFF_HIDE) &&
                    (ch.race.aff & AFF_HIDE) != 0 && MOUNTED(ch) == null) {
                send_to_char("You step back into the shadows.\n", ch);
            }

            ch.affected_by = SET_BIT(ch.affected_by, ch.race.aff);

            if (!IS_NPC(ch) && IS_SET(ch.act, PLR_CHANGED_AFF)) {
                ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_FLYING);
            }

            if (MOUNTED(ch) != null) {
                ch.affected_by = REMOVE_BIT(ch.affected_by, (AFF_IMP_INVIS | AFF_FADE | AFF_SNEAK | AFF_HIDE | AFF_CAMOUFLAGE));
            }

            if (ch.timer > 20 && !IS_NPC(ch)) {
                ch_quit = ch;
            }

            if (ch.position >= POS_STUNNED) {
                /* check to see if we need to go home */
                if (IS_NPC(ch) && ch.zone != null && ch.zone != ch.in_room.area
                        && ch.desc == null && ch.fighting == null && ch.progtypes == 0
                        && !IS_AFFECTED(ch, AFF_CHARM) && ch.last_fought == null
                        && RIDDEN(ch) == null && number_percent() < 15) {
                    if (ch.in_mind != null
                            && ch.pIndexData.vnum > 100) {
                        back_home(ch);
                    } else {
                        act("$n wanders on home.", ch, null, null, TO_ROOM);
                        extract_char(ch, true);
                    }
                    continue;
                }

                if (ch.hit < ch.max_hit) {
                    ch.hit += hit_gain(ch);
                } else {
                    ch.hit = ch.max_hit;
                }

                if (ch.mana < ch.max_mana) {
                    ch.mana += mana_gain(ch);
                } else {
                    ch.mana = ch.max_mana;
                }

                if (ch.move < ch.max_move) {
                    ch.move += move_gain(ch);
                } else {
                    ch.move = ch.max_move;
                }
            }

            if (ch.position == POS_STUNNED) {
                update_pos(ch);
            }

            if (!IS_NPC(ch) && ch.level < LEVEL_IMMORTAL) {
                OBJ_DATA obj;

                if ((obj = get_light_char(ch)) != null
                        && obj.item_type == ITEM_LIGHT
                        && obj.value[2] > 0) {
                    if (--obj.value[2] == 0 && ch.in_room != null) {
                        unequip_char(ch, obj);
                        if (get_light_char(ch) == null) {
                            --ch.in_room.light;
                        }
                        act("$p goes out.", ch, obj, null, TO_ROOM);
                        act("$p flickers and goes out.", ch, obj, null, TO_CHAR);
                        extract_obj(obj);
                    } else if (obj.value[2] <= 5 && ch.in_room != null) {
                        act("$p flickers.", ch, obj, null, TO_CHAR);
                    }
                }

                if (IS_IMMORTAL(ch)) {
                    ch.timer = 0;
                }

                if (++ch.timer >= 12) {
                    if (ch.was_in_room == null && ch.in_room != null) {
                        ch.was_in_room = ch.in_room;
                        if (ch.fighting != null) {
                            stop_fighting(ch, true);
                        }
                        act("$n disappears into the void.", ch, null, null, TO_ROOM);
                        send_to_char("You disappear into the void.\n", ch);
                        if (ch.level > 1) {
                            save_char_obj(ch);
                        }
                        if (ch.level < 10) {
                            char_from_room(ch);
                            char_to_room(ch, get_room_index(ROOM_VNUM_LIMBO));
                        }
                    }
                }

                gain_condition(ch, COND_DRUNK, -1);
                if (ch.clazz == Clazz.VAMPIRE && ch.level > 10) {
                    gain_condition(ch, COND_BLOODLUST, -1);
                }
                gain_condition(ch, COND_FULL, ch.size > SIZE_MEDIUM ? -4 : -2);
                if (ch.in_room.sector_type == SECT_DESERT) {
                    gain_condition(ch, COND_THIRST, -3);
                } else {
                    gain_condition(ch, COND_THIRST, -1);
                }
                gain_condition(ch, COND_HUNGER, ch.size > SIZE_MEDIUM ? -2 : -1);
            }

            for (paf = ch.affected; paf != null; paf = paf_next) {
                paf_next = paf.next;
                if (paf.duration > 0) {
                    paf.duration--;

                    if (number_range(0, 4) == 0 && paf.level > 0) {
                        paf.level--;
                    }
                    /* spell strength fades with time */
                } else if (paf.duration < 0) {
                } else {
                    if (paf_next == null
                            || paf_next.type != paf.type
                            || paf_next.duration > 0) {
                        if (paf.type != null && paf.type.msg_off != null) {
                            send_to_char(paf.type.msg_off, ch);
                            send_to_char("\n", ch);
                        }
                    }

                    if (paf.type == gsn_strangle) {
                        ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_SLEEP);
                        do_wake(ch, "");
                        var neck_af = new AFFECT_DATA();
                        neck_af.type = gsn_neckguard;
                        neck_af.where = TO_AFFECTS;
                        neck_af.level = ch.level;
                        neck_af.duration = (2 + ch.level / 50);
                        neck_af.modifier = 0;
                        neck_af.bitvector = 0;
                        neck_af.location = APPLY_NONE;
                        affect_join(ch, neck_af);
                    } else if (paf.type == gsn_blackjack) {
                        ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_SLEEP);
                        do_wake(ch, "");
                        var head_af = new AFFECT_DATA();
                        head_af.type = gsn_headguard;
                        head_af.where = TO_AFFECTS;
                        head_af.level = ch.level;
                        head_af.duration = 2 + ch.level / 50;
                        head_af.modifier = 0;
                        head_af.bitvector = 0;
                        head_af.location = APPLY_NONE;
                        affect_join(ch, head_af);
                    } else if (paf.type == gsn_vampiric_touch) {
                        ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_SLEEP);
                        do_wake(ch, "");
                        var b_af = new AFFECT_DATA();
                        b_af.type = gsn_blackguard;
                        b_af.where = TO_AFFECTS;
                        b_af.level = ch.level;
                        b_af.duration = (2 + ch.level / 50);
                        b_af.modifier = 0;
                        b_af.bitvector = 0;
                        b_af.location = APPLY_NONE;
                        affect_join(ch, b_af);
                    }

                    affect_remove(ch, paf);
                }
            }

            /*
             * Careful with the damages here,
             *   MUST NOT refer to ch after damage taken,
             *   as it may be lethal damage (on NPC).
             */

            if (is_affected(ch, gsn_witch_curse)) {
                AFFECT_DATA af;

                if (ch.in_room == null) {
                    continue;
                }

                act("The witch curse makes $n feel $s life slipping away.\n",
                        ch, null, null, TO_ROOM);
                send_to_char("The witch curse makes you feeling your life slipping away.\n", ch);

                for (af = ch.affected; af != null; af = af.next) {
                    if (af.type == gsn_witch_curse) {
                        break;
                    }
                }

                if (af == null) {
                    continue;
                }

                if (af.level == 1) {
                    continue;
                }

                if (af.modifier > -16001) {
                    var witch = new AFFECT_DATA();
                    witch.where = af.where;
                    witch.type = af.type;
                    witch.level = af.level;
                    witch.duration = af.duration;
                    witch.location = af.location;
                    witch.modifier = af.modifier * 2;
                    witch.bitvector = 0;

                    affect_remove(ch, af);
                    affect_to_char(ch, witch);
                    ch.hit = UMIN(ch.hit, ch.max_hit);
                    if (ch.hit < 1) {
                        affect_strip(ch, gsn_witch_curse);
                        ch.hit = 1;
                        damage(ch, ch, 16, gsn_witch_curse, DAM_NONE, false);
                        continue;
                    }
                } else {
                    affect_strip(ch, gsn_witch_curse);
                    ch.hit = 1;
                    damage(ch, ch, 16, gsn_witch_curse, DAM_NONE, false);
                    continue;
                }
            }

            if (IS_AFFECTED(ch, AFF_PLAGUE) && ch != null) {
                AFFECT_DATA af;
                CHAR_DATA vch;
                int dam;

                if (ch.in_room == null) {
                    continue;
                }

                act("$n writhes in agony as plague sores erupt from $s skin.",
                        ch, null, null, TO_ROOM);
                send_to_char("You writhe in agony from the plague.\n", ch);
                for (af = ch.affected; af != null; af = af.next) {
                    if (af.type == gsn_plague) {
                        break;
                    }
                }

                if (af == null) {
                    ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_PLAGUE);
                    continue;
                }

                if (af.level == 1) {
                    continue;
                }
                var plague = new AFFECT_DATA();
                plague.where = TO_AFFECTS;
                plague.type = gsn_plague;
                plague.level = (af.level - 1);
                plague.duration = number_range(1, 2 * plague.level);
                plague.location = APPLY_STR;
                plague.modifier = -5;
                plague.bitvector = AFF_PLAGUE;

                for (vch = ch.in_room.people; vch != null; vch = vch.next_in_room) {
                    if (!saves_spell(plague.level + 2, vch, DAM_DISEASE)
                            && !IS_IMMORTAL(vch)
                            && !IS_AFFECTED(vch, AFF_PLAGUE) && number_bits(2) == 0) {
                        send_to_char("You feel hot and feverish.\n", vch);
                        act("$n shivers and looks very ill.", vch, null, null, TO_ROOM);
                        affect_join(vch, plague);
                    }
                }

                dam = UMIN(ch.level, af.level / 5 + 1);
                ch.mana -= dam;
                ch.move -= dam;
                if (number_percent() < 70) {
                    damage(ch, ch, dam + UMAX(ch.max_hit / 20, 50), gsn_plague, DAM_DISEASE, true);
                } else {
                    damage(ch, ch, dam, gsn_plague, DAM_DISEASE, false);
                }
            } else if (IS_AFFECTED(ch, AFF_POISON) && ch != null
                    && !IS_AFFECTED(ch, AFF_SLOW)) {
                var poison = affect_find(ch.affected, gsn_poison);

                if (poison != null) {
                    act("$n shivers and suffers.", ch, null, null, TO_ROOM);
                    send_to_char("You shiver and suffer.\n", ch);
                    damage(ch, ch, poison.level / 10 + 1, gsn_poison,
                            DAM_POISON, true);
                }
            } else if (ch.position == POS_INCAP && number_range(0, 1) == 0) {
                damage(ch, ch, 1, null, DAM_NONE, false);
            } else if (ch.position == POS_MORTAL) {
                damage(ch, ch, 1, null, DAM_NONE, false);
            }
        }

        /*
         * Autosave and autoquit.
         * Check that these chars still exist.
         */

        if (char_update_last_save_time == -1 || current_time - char_update_last_save_time > 300) {
            char_update_last_save_time = current_time;
            for (ch = char_list; ch != null; ch = ch_next) {
                ch_next = ch.next;
                if (!IS_NPC(ch)) {
                    save_char_obj(ch);
                }
                if (ch == ch_quit || ch.timer > 20) {
                    do_quit(ch);
                }
            }
        }

        if (fTimeSync) {
            limit_time = current_time;
        }

    }


    static void water_float_update() {
        OBJ_DATA obj_next;
        OBJ_DATA obj;
        CHAR_DATA ch;
        boolean fChar;

        for (obj = object_list; obj != null; obj = obj_next) {
            obj_next = obj.next;


            if (obj.in_room == null) {
                continue;
            }


            if (IS_WATER(obj.in_room)) {

                fChar = false;
                ch = obj.in_room.people;
                if (ch != null) {
                    fChar = true;
                }
                if (obj.water_float != -1) {
                    obj.water_float--;
                }

                if (obj.water_float < 0) {
                    obj.water_float = -1;
                }

                if (obj.item_type == ITEM_DRINK_CON) {
                    obj.value[1] = URANGE(1, obj.value[1] + 8, obj.value[0]);
                    if (fChar) {
                        act("$p makes bubbles on the water.", ch, obj, null, TO_CHAR);
                        act("$p makes bubbles on the water.", ch, obj, null, TO_ROOM);
                    }
                    obj.water_float = obj.value[0] - obj.value[1];
                    obj.value[2] = 0;
                }
                if (obj.water_float == 0) {
                    if (((obj.item_type == ITEM_CORPSE_NPC) ||
                            (obj.item_type == ITEM_CORPSE_PC) ||
                            (obj.item_type == ITEM_CONTAINER)) &&
                            fChar) {
                        act("$p sinks down the water releasing some bubbles behind.", ch, obj, null, TO_CHAR);
                        act("$p sinks down the water releasing some bubbles behind.", ch, obj, null, TO_ROOM);
                    } else if (fChar) {
                        act("$p sinks down the water.", ch, obj, null, TO_CHAR);
                        act("$p sinks down the water.", ch, obj, null, TO_ROOM);
                    }
                    extract_obj(obj);
                }
            }
        }

    }

    /*
     * Update all objs.
     * This function is performance sensitive.
     */
    private static int obj_update_pit_count = 1;

    static void obj_update() {
        OBJ_DATA obj;
        OBJ_DATA obj_next;
        OBJ_DATA t_obj, pit, next_obj;

        AFFECT_DATA paf, paf_next;

        for (obj = object_list; obj != null; obj = obj_next) {
            CHAR_DATA rch;
            String message;

            obj_next = obj.next;

            /* go through affects and decrement */
            for (paf = obj.affected; paf != null; paf = paf_next) {
                paf_next = paf.next;
                if (paf.duration > 0) {
                    paf.duration--;
                    if (number_range(0, 4) == 0 && paf.level > 0) {
                        paf.level--;  /* spell strength fades with time */
                    }
                } else if (paf.duration < 0) {
                } else {
                    if (paf_next == null
                            || paf_next.type != paf.type
                            || paf_next.duration > 0) {
                        if (paf.type != null && paf.type.msg_obj != null) {
                            if (obj.carried_by != null) {
                                rch = obj.carried_by;
                                act(paf.type.msg_obj,
                                        rch, obj, null, TO_CHAR);
                            }
                            if (obj.in_room != null
                                    && obj.in_room.people != null) {
                                rch = obj.in_room.people;
                                act(paf.type.msg_obj,
                                        rch, obj, null, TO_ALL);
                            }
                        }
                    }

                    affect_remove_obj(obj, paf);
                }
            }


            for (t_obj = obj; t_obj.in_obj != null; t_obj = t_obj.in_obj) ;

            if (IS_SET(obj.progtypes, OPROG_AREA)) {
                if ((t_obj.in_room != null &&
                        (t_obj.in_room.area.nplayer > 0))
                        ||
                        (t_obj.carried_by != null && t_obj.carried_by.in_room != null && t_obj.carried_by.in_room.area.nplayer > 0)) {
                    obj.pIndexData.oprogs.area_prog.run(obj);
                }
            }

            if (check_material(obj, "ice")) {
                if (obj.carried_by != null) {
                    if (obj.carried_by.in_room.sector_type == SECT_DESERT) {
                        if (number_percent() < 40) {
                            act("The extreme heat melts $p.", obj.carried_by, obj, null, TO_CHAR);
                            extract_obj(obj);
                            continue;
                        }
                    }
                } else if (obj.in_room != null) {
                    if (obj.in_room.sector_type == SECT_DESERT) {
                        if (number_percent() < 50) {
                            if (obj.in_room.people != null) {
                                act("The extreme heat melts $p.", obj.in_room.people, obj, null, TO_ROOM);
                                act("The extreme heat melts $p.", obj.in_room.people, obj, null, TO_CHAR);
                            }
                            extract_obj(obj);
                            continue;
                        }
                    }
                }
            }

            if (!check_material(obj, "glass") && obj.item_type == ITEM_POTION) {
                if (obj.carried_by != null) {
                    if (obj.carried_by.in_room.sector_type == SECT_DESERT &&
                            !IS_NPC(obj.carried_by)) {
                        if (number_percent() < 20) {
                            act("$p evaporates.", obj.carried_by, obj, null, TO_CHAR);
                            extract_obj(obj);
                            continue;
                        }
                    }
                } else if (obj.in_room != null) {
                    if (obj.in_room.sector_type == SECT_DESERT) {
                        if (number_percent() < 30) {
                            if (obj.in_room.people != null) {
                                act("$p evaporates by the extream heat.", obj.in_room.people, obj, null, TO_ROOM);
                                act("$p evaporates by the extream heat.", obj.in_room.people, obj, null, TO_CHAR);
                            }
                            extract_obj(obj);
                            continue;
                        }
                    }
                }
            }

            if (obj.condition > -1 && (obj.timer <= 0 || --obj.timer > 0)) {
                continue;
            }

            switch (obj.item_type) {
                default -> message = "$p crumbles into dust.";
                case ITEM_FOUNTAIN -> message = "$p dries up.";
                case ITEM_CORPSE_NPC -> message = "$p decays into dust.";
                case ITEM_CORPSE_PC -> message = "$p decays into dust.";
                case ITEM_FOOD -> message = "$p decomposes.";
                case ITEM_POTION -> message = "$p has evaporated from disuse.";
                case ITEM_PORTAL -> message = "$p fades out of existence.";
                case ITEM_CONTAINER -> {
                    if (CAN_WEAR(obj, ITEM_WEAR_FLOAT)) {
                        if (obj.contains != null) {
                            message = "$p flickers and vanishes, spilling its contents on the floor.";
                        } else {
                            message = "$p flickers and vanishes.";
                        }
                    } else {
                        message = "$p crumbles into dust.";
                    }
                }
            }

            if (obj.carried_by != null) {
                if (IS_NPC(obj.carried_by)
                        && obj.carried_by.pIndexData.pShop != null) {
                    obj.carried_by.silver += obj.cost / 5;
                } else {
                    act(message, obj.carried_by, obj, null, TO_CHAR);
                    if (obj.wear_loc == WEAR_FLOAT) {
                        act(message, obj.carried_by, obj, null, TO_ROOM);
                    }
                }
            } else if (obj.in_room != null
                    && (rch = obj.in_room.people) != null) {
                if (!(obj.in_obj != null && obj.in_obj.pIndexData.vnum == OBJ_VNUM_PIT
                        && !CAN_WEAR(obj.in_obj, ITEM_TAKE))) {
                    act(message, rch, obj, null, TO_ROOM);
                    act(message, rch, obj, null, TO_CHAR);
                }
            }

            obj_update_pit_count = ++obj_update_pit_count % 120; /* more or less an hour */
            if (obj.pIndexData.vnum == OBJ_VNUM_PIT && obj_update_pit_count == 121) {
                for (t_obj = obj.contains; t_obj != null; t_obj = next_obj) {
                    next_obj = t_obj.next_content;
                    obj_from_obj(t_obj);
                    extract_obj(t_obj);
                }
            }


            if ((obj.item_type == ITEM_CORPSE_PC || obj.wear_loc == WEAR_FLOAT) && obj.contains != null) {   /* save the contents */
                for (t_obj = obj.contains; t_obj != null; t_obj = next_obj) {
                    next_obj = t_obj.next_content;
                    obj_from_obj(t_obj);

                    if (obj.in_obj != null) /* in another object */ {
                        obj_to_obj(t_obj, obj.in_obj);
                    } else if (obj.carried_by != null)  /* carried */ {
                        if (obj.wear_loc == WEAR_FLOAT) {
                            if (obj.carried_by.in_room == null) {
                                extract_obj(t_obj);
                            } else {
                                obj_to_room(t_obj, obj.carried_by.in_room);
                            }
                        } else {
                            obj_to_char(t_obj, obj.carried_by);
                        }
                    } else if (obj.in_room == null)  /* destroy it */ {
                        extract_obj(t_obj);
                    } else { /* to the pit */
                        for (pit = get_room_index(obj.altar).contents;
                             pit != null && pit.pIndexData.vnum != obj.pit;
                             pit = pit.next) {
                        }

                        if (pit == null) {
                            obj_to_room(t_obj, obj.in_room);
                        } else {
                            obj_to_obj(t_obj, pit);
                        }
                    }
                }
            }

            extract_obj(obj);
        }
    }

    /*
     * Aggress.
     *
     * for each mortal PC
     *     for each mob in room
     *         aggress on some random PC
     *
     * This function takes 25% to 35% of ALL Merc cpu time.
     * Unfortunately, checking on each PC move is too tricky,
     *   because we don't the mob to just attack the first PC
     *   who leads the party into the room.
     *
     * -- Furey
     */

    static void aggr_update() {
        CHAR_DATA wch;
        CHAR_DATA wch_next;
        CHAR_DATA ch;
        CHAR_DATA ch_next;
        CHAR_DATA vch;
        CHAR_DATA vch_next;
        CHAR_DATA victim;

        for (wch = char_list; wch != null; wch = wch_next) {
//            if (!IS_VALID(wch)) {
//                bug("Aggr_update: Invalid char.", 0);
//                break;
//            }

            wch_next = wch.next;

            if (IS_AFFECTED(wch, AFF_BLOODTHIRST) &&
                    IS_AWAKE(wch) && wch.fighting == null) {
                for (vch = wch.in_room.people;
                     vch != null && wch.fighting == null; vch = vch_next) {
                    vch_next = vch.next_in_room;
                    if (wch != vch && can_see(wch, vch) &&
                            !is_safe_nomessage(wch, vch)) {
                        act("{rMORE BLOOD! MORE BLOOD! MORE BLOOD!!!{x",
                                wch, null, null, TO_CHAR, POS_RESTING);
                        do_murder(wch, vch.name);
                    }
                }
            }

            if (wch.cabal != CABAL_NONE && IS_NPC(wch)) {
                for (ch = wch.in_room.people; ch != null; ch = ch_next) {
                    ch_next = ch.next_in_room;
                    if (!IS_NPC(ch)
                            && !IS_IMMORTAL(ch)
                            && ch.cabal != wch.cabal
                            && ch.fighting == null) {
                        multi_hit(wch, ch, null);
                    }

                }
                continue;
            }

            if (IS_NPC(wch)
                    || wch.level >= LEVEL_IMMORTAL
                    || wch.in_room == null
                    || wch.in_room.area.empty) {
                continue;
            }

            for (ch = wch.in_room.people; ch != null; ch = ch_next) {
                int count;

                ch_next = ch.next_in_room;

                if (!IS_NPC(ch)
                        || (!IS_SET(ch.act, ACT_AGGRESSIVE) && (ch.last_fought == null))
                        || IS_SET(ch.in_room.room_flags, ROOM_SAFE)
                        || IS_AFFECTED(ch, AFF_CALM)
                        || ch.fighting != null
                        || RIDDEN(ch) != null
                        || IS_AFFECTED(ch, AFF_CHARM)
                        || IS_AFFECTED(ch, AFF_SCREAM)
                        || !IS_AWAKE(ch)
                        || (IS_SET(ch.act, ACT_WIMPY) && IS_AWAKE(wch))
                        || !can_see(ch, wch)
                        || number_bits(1) == 0
                        || is_safe_nomessage(ch, wch)) {
                    continue;
                }

                /* Mad mob attacks! */
                if (ch.last_fought == wch) {
                    var buf = ((is_affected(wch, gsn_doppelganger) &&
                            !IS_SET(ch.act, PLR_HOLYLIGHT)) ?
                            PERS(wch.doppel, ch) : PERS(wch, ch)) + "! Now you die!";
                    do_yell(ch, buf);
                    wch = check_guard(wch, ch);

                    multi_hit(ch, wch, null);
                    continue;
                }

                if (ch.last_fought != null) {
                    continue;
                }

                /*
                 * Ok we have a 'wch' player character and a 'ch' npc aggressor.
                 * Now make the aggressor fight a RANDOM pc victim in the room,
                 *   giving each 'vch' an equal chance of selection.
                 */
                count = 0;
                victim = null;
                for (vch = wch.in_room.people; vch != null; vch = vch_next) {
                    vch_next = vch.next_in_room;

                    if (!IS_NPC(vch)
                            && vch.level < LEVEL_IMMORTAL
                            && ch.level >= vch.level - 5
                            && (!IS_SET(ch.act, ACT_WIMPY) || !IS_AWAKE(vch))
                            && can_see(ch, vch)
                            && vch.clazz != Clazz.VAMPIRE /* do not attack vampires */
                            && !(IS_GOOD(ch) && IS_GOOD(vch))) /* good vs good :( */ {
                        if (number_range(0, count) == 0) {
                            victim = vch;
                        }
                        count++;
                    }
                }

                if (victim == null) {
                    continue;
                }

                if (!is_safe_nomessage(ch, victim)) {
                    victim = check_guard(victim, ch);
                    if (IS_SET(ch.off_flags, OFF_BACKSTAB) && get_wield_char(ch, false) != null) {
                        multi_hit(ch, victim, gsn_backstab);
                    } else {
                        multi_hit(ch, victim, null);
                    }
                }
            }
        }
    }


    private static int pulse_area;
    private static int pulse_mobile;
    private static int pulse_violence;
    private static int pulse_point;
    private static int pulse_music;
    private static int pulse_water_float;
    private static int pulse_raffect;
    private static int pulse_track;

    /*
     * Handle all kinds of updates.
     * Called once per pulse from game loop.
     * Random times to defeat tick-timing clients and players.
     */

    static void update_handler() {
//TODO: Moved from COMM
//
//                if (d.character != null && d.character.daze > 0) {
//                    --d.character.daze;
//                }
//
//                if (d.character != null && d.character.wait > 0) {
//                    --d.character.wait;
//                    continue;
//                }

        if (--pulse_area <= 0) {
            wiznet("AREA & ROOM TICK!", null, null, WIZ_TICKS, 0, 0);
            pulse_area = PULSE_AREA;
            area_update();
            room_update();
        }

        if (--pulse_music <= 0) {
            pulse_music = PULSE_MUSIC;
            /*  song_update(); */
        }

        if (--pulse_mobile <= 0) {
            pulse_mobile = PULSE_MOBILE;
            mobile_update();
            light_update();
        }

        if (--pulse_violence <= 0) {
            pulse_violence = PULSE_VIOLENCE;
            violence_update();
        }

        if (--pulse_water_float <= 0) {
            pulse_water_float = PULSE_WATER_FLOAT;
            water_float_update();
        }

        if (--pulse_raffect <= 0) {
            pulse_raffect = PULSE_RAFFECT;
            room_affect_update();
        }

        if (--pulse_track <= 0) {
            pulse_track = PULSE_TRACK;
            track_update();
        }

        if (--pulse_point <= 0) {
            wiznet("CHAR TICK!", null, null, WIZ_TICKS, 0, 0);
            pulse_point = PULSE_TICK;
            weather_update();
            char_update();
            quest_update();
            obj_update();
            check_reboot();

            /* room counting */
            {
                CHAR_DATA ch;

                for (ch = char_list; ch != null; ch = ch.next) {
                    if (!IS_NPC(ch) && ch.in_room != null) {
                        ch.in_room.area.count = UMIN(ch.in_room.area.count + 1, 5000000);
                    }
                }
            }

        }

        aggr_update();
        auction_update();

        tail_chain();
    }

    static void light_update() {
        CHAR_DATA ch;
        int dam_light;
        DESCRIPTOR_DATA d, d_next;


        for (d = descriptor_list; d != null; d = d_next) {
            d_next = d.next;
            if (d.connected != CON_PLAYING) {
                continue;
            }

            ch = (d.original != null) ? d.original : d.character;

            if (IS_IMMORTAL(ch)) {
                continue;
            }

            if (ch.clazz != Clazz.VAMPIRE) {
                continue;
            }

            /* also checks vampireness */
            if ((dam_light = isn_dark_safe(ch)) == 0) {
                continue;
            }

            if (dam_light != 2 && number_percent() < get_skill(ch, gsn_light_res)) {
                check_improve(ch, gsn_light_res, true, 32);
                continue;
            }

            if (dam_light == 1) {
                send_to_char("The light in the room disturbs you.\n", ch);
            } else {
                send_to_char("Sun light disturbs you.\n", ch);
            }

            dam_light = (ch.max_hit * 4) / 100;
            if (dam_light == 0) {
                dam_light = 1;
            }
            damage(ch, ch, dam_light, gsn_x_hunger, DAM_LIGHT_V, true);

            if (ch.position == POS_STUNNED) {
                update_pos(ch);
            }

            if (number_percent() < 10) {
                gain_condition(ch, COND_DRUNK, -1);
            }
        }
    }


    static void room_update() {
        ROOM_INDEX_DATA room;
        ROOM_INDEX_DATA room_next;

        for (room = top_affected_room; room != null; room = room_next) {
            AFFECT_DATA paf;
            AFFECT_DATA paf_next;

            room_next = room.aff_next;

            for (paf = room.affected; paf != null; paf = paf_next) {
                paf_next = paf.next;
                if (paf.duration > 0) {
                    paf.duration--;
/*
        if (number_range(0,4) == 0 && paf.level > 0)
          paf.level--;
     spell strength shouldn't fade with time
     because checks safe_rpsell with af.level */
                } else if (paf.duration < 0) {
                } else {
                    if (paf_next == null
                            || paf_next.type != paf.type
                            || paf_next.duration > 0) {
/*
            if ( paf.type > 0 && paf.type.msg_off )
            {
            act( paf.type.msg_off, ch );
            send_to_char( "\n", ch );
            }
*/
                    }

                    affect_remove_room(room, paf);
                }
            }

        }
    }

    static void room_affect_update() {
        ROOM_INDEX_DATA room;
        ROOM_INDEX_DATA room_next;

        for (room = top_affected_room; room != null; room = room_next) {
            room_next = room.aff_next;

            if (IS_ROOM_AFFECTED(room, AFF_ROOM_PLAGUE) && room.people != null) {
                AFFECT_DATA af;
                CHAR_DATA vch;

                for (af = room.affected; af != null; af = af.next) {
                    if (af.type == gsn_black_death) {
                        break;
                    }
                }

                if (af == null) {
                    room.affected_by = REMOVE_BIT(room.affected_by, AFF_ROOM_PLAGUE);
                } else {
                    if (af.level == 1) {
                        af.level = 2;
                    }
                    var plague = new AFFECT_DATA();
                    plague.where = TO_AFFECTS;
                    plague.type = gsn_plague;
                    plague.level = (af.level - 1);
                    plague.duration = number_range(1, ((plague.level / 2) + 1));
                    plague.location = APPLY_NONE;
                    plague.modifier = -5;
                    plague.bitvector = AFF_PLAGUE;

                    for (vch = room.people; vch != null; vch = vch.next_in_room) {
                        if (!saves_spell(plague.level, vch, DAM_DISEASE)
                                && !IS_IMMORTAL(vch)
                                && !is_safe_rspell(af.level, vch)
                                && !IS_AFFECTED(vch, AFF_PLAGUE) && number_bits(3) == 0) {
                            send_to_char("You feel hot and feverish.\n", vch);
                            act("$n shivers and looks very ill.", vch, null, null, TO_ROOM);
                            affect_join(vch, plague);
                        }
                    }

                }
            }
            if (IS_ROOM_AFFECTED(room, AFF_ROOM_POISON) && room.people != null) {
                AFFECT_DATA af;
                CHAR_DATA vch;

                for (af = room.affected; af != null; af = af.next) {
                    if (af.type == gsn_deadly_venom) {
                        break;
                    }
                }

                if (af == null) {
                    room.affected_by = REMOVE_BIT(room.affected_by, AFF_ROOM_POISON);
                } else {

                    if (af.level == 1) {
                        af.level = 2;
                    }

                    var paf = new AFFECT_DATA();
                    paf.where = TO_AFFECTS;
                    paf.type = gsn_poison;
                    paf.level = (af.level - 1);
                    paf.duration = number_range(1, ((paf.level / 5) + 1));
                    paf.location = APPLY_NONE;
                    paf.modifier = -5;
                    paf.bitvector = AFF_POISON;

                    for (vch = room.people; vch != null; vch = vch.next_in_room) {
                        if (!saves_spell(paf.level, vch, DAM_POISON)
                                && !IS_IMMORTAL(vch)
                                && !is_safe_rspell(af.level, vch)
                                && !IS_AFFECTED(vch, AFF_POISON) && number_bits(3) == 0) {
                            send_to_char("You feel very sick.\n", vch);
                            act("$n looks very ill.", vch, null, null, TO_ROOM);
                            affect_join(vch, paf);
                        }
                    }
                }
            }

            if (IS_ROOM_AFFECTED(room, AFF_ROOM_SLOW) && room.people != null) {
                AFFECT_DATA af;
                CHAR_DATA vch;

                for (af = room.affected; af != null; af = af.next) {
                    if (af.type == gsn_lethargic_mist) {
                        break;
                    }
                }

                if (af == null) {
                    room.affected_by = REMOVE_BIT(room.affected_by, AFF_ROOM_SLOW);
                } else {

                    if (af.level == 1) {
                        af.level = 2;
                    }

                    var paf = new AFFECT_DATA();
                    paf.where = TO_AFFECTS;
                    paf.type = gsn_slow;
                    paf.level = (af.level - 1);
                    paf.duration = number_range(1, ((paf.level / 5) + 1));
                    paf.location = APPLY_NONE;
                    paf.modifier = -5;
                    paf.bitvector = AFF_SLOW;

                    for (vch = room.people; vch != null; vch = vch.next_in_room) {
                        if (!saves_spell(paf.level, vch, DAM_OTHER)
                                && !IS_IMMORTAL(vch)
                                && !is_safe_rspell(af.level, vch)
                                && !IS_AFFECTED(vch, AFF_SLOW) && number_bits(3) == 0) {
                            send_to_char("You start to move less quickly.\n", vch);
                            act("$n is moving less quickly.", vch, null, null, TO_ROOM);
                            affect_join(vch, paf);
                        }
                    }
                }
            }

            if (IS_ROOM_AFFECTED(room, AFF_ROOM_SLEEP) && room.people != null) {
                AFFECT_DATA af;
                CHAR_DATA vch;

                for (af = room.affected; af != null; af = af.next) {
                    if (af.type == gsn_mysterious_dream) {
                        break;
                    }
                }

                if (af == null) {
                    room.affected_by = REMOVE_BIT(room.affected_by, AFF_ROOM_SLEEP);
                } else {

                    if (af.level == 1) {
                        af.level = 2;
                    }
                    var paf = new AFFECT_DATA();
                    paf.where = TO_AFFECTS;
                    paf.type = gsn_sleep;
                    paf.level = (af.level - 1);
                    paf.duration = number_range(1, ((paf.level / 10) + 1));
                    paf.location = APPLY_NONE;
                    paf.modifier = -5;
                    paf.bitvector = AFF_SLEEP;

                    for (vch = room.people; vch != null; vch = vch.next_in_room) {
                        if (!saves_spell(paf.level - 4, vch, DAM_CHARM)
                                && !IS_IMMORTAL(vch)
                                && !is_safe_rspell(af.level, vch)
                                && !(IS_NPC(vch) && IS_SET(vch.act, ACT_UNDEAD))
                                && !IS_AFFECTED(vch, AFF_SLEEP) && number_bits(3) == 0) {
                            if (IS_AWAKE(vch)) {
                                send_to_char("You feel very sleepy.......zzzzzz.\n", vch);
                                act("$n goes to sleep.", vch, null, null, TO_ROOM);
                                vch.position = POS_SLEEPING;
                            }
                            affect_join(vch, paf);
                        }
                    }
                }
            }


            if (IS_ROOM_AFFECTED(room, AFF_ROOM_ESPIRIT) && room.people != null) {
                AFFECT_DATA af;
                CHAR_DATA vch;

                for (af = room.affected; af != null; af = af.next) {
                    if (af.type == gsn_evil_spirit) {
                        break;
                    }
                }

                if (af == null) {
                    room.affected_by = REMOVE_BIT(room.affected_by, AFF_ROOM_ESPIRIT);
                } else {

                    if (af.level == 1) {
                        af.level = 2;
                    }

                    var paf = new AFFECT_DATA();
                    paf.where = TO_AFFECTS;
                    paf.type = gsn_evil_spirit;
                    paf.level = af.level;
                    paf.duration = number_range(1, (paf.level / 30));
                    paf.location = APPLY_NONE;
                    paf.modifier = 0;
                    paf.bitvector = 0;

                    for (vch = room.people; vch != null; vch = vch.next_in_room) {
                        if (!saves_spell(paf.level + 2, vch, DAM_MENTAL)
                                && !IS_IMMORTAL(vch)
                                && !is_safe_rspell(af.level, vch)
                                && !is_affected(vch, gsn_evil_spirit) && number_bits(3) == 0) {
                            send_to_char("You feel worse than ever.\n", vch);
                            act("$n looks more evil.", vch, null, null, TO_ROOM);
                            affect_join(vch, paf);
                        }
                    }
                }
            }

/* new ones here
        while (IS_ROOM_AFFECTED(room, AFF_ROOM_) && room.people != null)
        {
            AFFECT_DATA *af, paf;
            CHAR_DATA vch;

            for ( af = room.affected; af != null; af = af.next )
            {
                if (af.type == gsn_)
                    break;
            }

            if (af == null)
            {
room.affected_by=                REMOVE_BIT(room.affected_by,AFF_ROOM_);
                break;
            }

            if (af.level == 1)
                af.level = 2;

        paf.where       = TO_AFFECTS;
            paf.type        = gsn_;
            paf.level       = af.level - 1;
            paf.duration    = number_range(1,((paf.level/5)+1));
            paf.location    = APPLY_NONE;
            paf.modifier    = -5;
            paf.bitvector   = AFF_;

            for ( vch = room.people; vch != null; vch = vch.next_in_room)
            {
                if (!saves_spell(paf.level + 2,vch,DAM_)
        &&  !IS_IMMORTAL(vch)
        &&  !is_safe_rspell(af.level,vch)
                &&  !IS_AFFECTED(vch,AFF_) && number_bits(3) == 0)
                {
                    send_to_char("You feel hot and feverish.\n",vch);
                    act("$n shivers and looks very ill.",vch,null,null,TO_ROOM);
                    affect_join(vch,paf);
                }
            }
     break;
        }
*/
        }
    }


    static void check_reboot() {
        DESCRIPTOR_DATA d;

        switch (reboot_counter) {
            case -1:
                break;
            case 0:
                reboot_nightworks(true, NIGHTWORKS_REBOOT);
                return;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 10:
            case 15:
                var buf = "\007***** REBOOT IN " + reboot_counter + " MINUTES *****\007\n";
                for (d = descriptor_list; d != null; d = d.next) {
                    write_to_buffer(d, buf);
                }
            default:
                reboot_counter--;
                break;
        }
    }

    static void track_update() {
        CHAR_DATA ch, ch_next;
        CHAR_DATA vch, vch_next;

        for (ch = char_list; ch != null; ch = ch_next) {
            ch_next = ch.next;

            if (IS_NPC(ch) && !IS_AFFECTED(ch, AFF_CALM)
                    && !IS_AFFECTED(ch, AFF_CHARM)
                    && ch.fighting == null
                    && ch.in_room != null
                    && IS_AWAKE(ch)
                    && !IS_SET(ch.act, ACT_NOTRACK)
                    && RIDDEN(ch) == null
                    && !IS_AFFECTED(ch, AFF_SCREAM)) {
                if (ch.last_fought != null && ch.in_room != ch.last_fought.in_room) {
                    do_track(ch, ch.last_fought.name);
                } else if (ch.in_mind != null) {
                    for (vch = ch.in_room.people; vch != null; vch = vch_next) {
                        vch_next = vch.next_in_room;

                        if (ch == vch) {
                            continue;
                        }
                        if (!IS_IMMORTAL(vch) && can_see(ch, vch) && !is_safe_nomessage(ch, vch) && is_name(vch.name, ch.in_mind)) {
                            do_yell(ch, "So we meet again, " + vch.name);
                            do_murder(ch, vch.name);
                            break; /* one fight at a time */
                        }
                    }
                }
            }
        }
    }
}
