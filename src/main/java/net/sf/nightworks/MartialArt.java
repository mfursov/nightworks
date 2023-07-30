package net.sf.nightworks;

import net.sf.nightworks.util.TextBuffer;

import static net.sf.nightworks.ActComm.do_yell;
import static net.sf.nightworks.ActComm.is_same_group;
import static net.sf.nightworks.ActMove.*;
import static net.sf.nightworks.ActObj.get_obj;
import static net.sf.nightworks.ActSkill.check_improve;
import static net.sf.nightworks.Comm.act;
import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.Const.attack_table;
import static net.sf.nightworks.DB.*;
import static net.sf.nightworks.Effects.fire_effect;
import static net.sf.nightworks.Fight.*;
import static net.sf.nightworks.Handler.*;
import static net.sf.nightworks.Magic.spell_blindness;
import static net.sf.nightworks.Magic.spell_poison;
import static net.sf.nightworks.Nightworks.*;
import static net.sf.nightworks.Skill.*;
import static net.sf.nightworks.util.TextUtils.one_argument;
import static net.sf.nightworks.util.TextUtils.str_cmp;

class MartialArt {
/*
 * Disarm a creature.
 * Caller must check for successful attack.
 */

    static void disarm(CHAR_DATA ch, CHAR_DATA victim, boolean disarm_second) {
        OBJ_DATA obj;

        if (disarm_second) {
            if ((obj = get_wield_char(victim, true)) == null) {
                bug("Disarm second with null DUAL_WIELD");
                return;
            }
        } else {
            if ((obj = get_wield_char(victim, false)) == null) {
                bug("Disarm first with null WEAR_WIELD");
                return;
            }
        }

        if (IS_OBJ_STAT(obj, ITEM_NOREMOVE)) {
            act("$S weapon won't budge!", ch, null, victim, TO_CHAR);
            act("$n tries to disarm you, but your weapon won't budge!", ch, null, victim, TO_VICT);
            act("$n tries to disarm $N, but fails.", ch, null, victim, TO_NOTVICT);
            return;
        }

        if (skill_failure_nomessage(victim, gsn_grip, 0) == 0) {
            var skill = get_skill(victim, gsn_grip);

            skill += (get_curr_stat(victim, STAT_STR) - get_curr_stat(ch, STAT_STR)) * 5;
            if (number_percent() < skill) {
                act("$N grips and prevent you to disarm $S!", ch, null, victim, TO_CHAR);
                act("$n tries to disarm you, but you grip and escape!", ch, null, victim, TO_VICT);
                act("$n tries to disarm $N, but fails.", ch, null, victim, TO_NOTVICT);
                check_improve(victim, gsn_grip, true, 1);
                return;
            } else {
                check_improve(victim, gsn_grip, false, 1);
            }
        }

        act("$n {cDISARMS{x you and sends your weapon flying!", ch, null, victim, TO_VICT, POS_FIGHTING);
        act("You {Cdisarm{x $N!", ch, null, victim, TO_CHAR, POS_FIGHTING);
        act("$n {Cdisarms{x $N!", ch, null, victim, TO_NOTVICT, POS_FIGHTING);

        obj_from_char(obj);
        if (IS_OBJ_STAT(obj, ITEM_NODROP) || IS_OBJ_STAT(obj, ITEM_INVENTORY)) {
            obj_to_char(obj, victim);
        } else {
            obj_to_room(obj, victim.in_room);
            if (IS_NPC(victim) && victim.wait == 0 && can_see_obj(victim, obj)) {
                get_obj(victim, obj, null);
            }
        }
/*
    if ( (obj2 = get_wield_char(victim,true)) != null)
    {
act( "$CYou wield your second weapon as your first!.{x", ch, null,
    victim,TO_VICT,POS_FIGHTING,CLR_CYAN);
act( "$C$N wields his second weapon as first!{x",  ch, null,
    victim,TO_CHAR ,POS_FIGHTING,CLR_CYAN_BOLD);
act( "$C$N wields his second weapon as first!{x",  ch, null, victim,
    TO_NOTVICT ,POS_FIGHTING,CLR_CYAN_BOLD);
    unequip_char( victim, obj2);
    equip_char( victim, obj2 , WEAR_WIELD);
    }
*/
    }

    static void do_berserk(CHAR_DATA ch) {
        int chance, hp_percent;

        if (skill_failure_check(ch, gsn_berserk, false, OFF_BERSERK, "You turn red in the face, but nothing happens.\n")) {
            return;
        }

        chance = get_skill(ch, gsn_berserk);

        if (IS_AFFECTED(ch, AFF_BERSERK) || is_affected(ch, gsn_berserk) || is_affected(ch, Skill.gsn_frenzy)) {
            send_to_char("You get a little madder.\n", ch);
            return;
        }

        if (IS_AFFECTED(ch, AFF_CALM)) {
            send_to_char("You're feeling too mellow to berserk.\n", ch);
            return;
        }

        if (ch.mana < 50) {
            send_to_char("You can't get up enough energy.\n", ch);
            return;
        }

        /* modifiers */

        /* fighting */
        if (ch.position == POS_FIGHTING) {
            chance += 10;
        }

        /* damage -- below 50% of hp helps, above hurts */
        hp_percent = 100 * ch.hit / ch.max_hit;
        chance += 25 - hp_percent / 2;

        if (number_percent() < chance) {


            WAIT_STATE(ch, PULSE_VIOLENCE);
            ch.mana -= 50;
            ch.move /= 2;

            /* heal a little damage */
            ch.hit += ch.level * 2;
            ch.hit = UMIN(ch.hit, ch.max_hit);

            send_to_char("Your pulse races as you are consumned by rage!\n", ch);
            act("{r$n gets a wild look in $s eyes.{x", ch, null, null, TO_ROOM, POS_FIGHTING);
            check_improve(ch, gsn_berserk, true, 2);
            var af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_berserk;
            af.level = ch.level;
            af.duration = number_fuzzy(ch.level / 8);
            af.modifier = UMAX(1, ch.level / 5);
            af.bitvector = AFF_BERSERK;

            af.location = APPLY_HITROLL;
            affect_to_char(ch, af);

            af.location = APPLY_DAMROLL;
            affect_to_char(ch, af);

            af.modifier = UMAX(10, 10 * (ch.level / 5));
            af.location = APPLY_AC;
            affect_to_char(ch, af);
        } else {
            WAIT_STATE(ch, 2 * PULSE_VIOLENCE);
            ch.mana -= 25;
            ch.move /= 2;

            send_to_char("Your pulse speeds up, but nothing happens.\n", ch);
            check_improve(ch, gsn_berserk, false, 2);
        }
    }

    static void do_bash(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        int chance, wait;
        int damage_bash;

        var FightingCheck = ch.fighting != null;

        var argb = new StringBuilder();
        argument = one_argument(argument, argb);

        var arg = argb.toString();
        if (!arg.isEmpty() && !str_cmp(arg, "door")) {
            do_bash_door(ch, argument);
            return;
        }

        if (skill_failure_check(ch, gsn_bash, false, OFF_BASH, "Bashing? What's that?\n")) {
            return;
        }

        chance = get_skill(ch, gsn_bash);

        if (arg.isEmpty()) {
            victim = ch.fighting;
            if (victim == null) {
                send_to_char("But you aren't fighting anyone!\n", ch);
                return;
            }
        } else if ((victim = get_char_room(ch, arg)) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (victim.position < POS_FIGHTING) {
            act("You'll have to let $M get back up first.", ch, null, victim, TO_CHAR);
            return;
        }

        if (victim == ch) {
            send_to_char("You try to bash your brains out, but fail.\n", ch);
            return;
        }

        if (MOUNTED(victim) != null) {
            send_to_char("You can't bash a riding one!\n", ch);
            return;
        }

        if (is_safe(ch, victim)) {
            return;
        }

        if (IS_AFFECTED(ch, AFF_CHARM) && ch.master == victim) {
            act("But $N is your friend!", ch, null, victim, TO_CHAR);
            return;
        }

        if (is_affected(victim, gsn_protective_shield)) {
            act("{YYour bash seems to slide around $N.{x", ch, null, victim, TO_CHAR, POS_FIGHTING);
            act("{Y$n's bash slides off your protective shield.{x", ch, null, victim, TO_VICT, POS_FIGHTING);
            act("{Y$n's bash seems to slide around $N.{x", ch, null, victim, TO_NOTVICT, POS_FIGHTING);
            return;
        }

        /* modifiers */

        /* size  and weight */
        chance += ch.carry_weight / 25;
        chance -= victim.carry_weight / 20;

        if (ch.size < victim.size) {
            chance += (ch.size - victim.size) * 25;
        } else {
            chance += (ch.size - victim.size) * 10;
        }

        /* stats */
        chance += get_curr_stat(ch, STAT_STR);
        chance -= get_curr_stat(victim, STAT_DEX) * 4 / 3;

        if (IS_AFFECTED(ch, AFF_FLYING)) {
            chance -= 10;
        }

        /* speed */
        if (IS_SET(ch.off_flags, OFF_FAST)) {
            chance += 10;
        }
        if (IS_SET(victim.off_flags, OFF_FAST)) {
            chance -= 20;
        }

        /* level */
        chance += (ch.level - victim.level) * 2;

        /* now the attack */
        if (number_percent() < chance) {

            act("$n sends you sprawling with a powerful bash!",
                    ch, null, victim, TO_VICT);
            act("You slam into $N, and send $M flying!", ch, null, victim, TO_CHAR);
            act("$n sends $N sprawling with a powerful bash.",
                    ch, null, victim, TO_NOTVICT);
            check_improve(ch, gsn_bash, true, 1);

            wait = 3;

            wait = switch (number_bits(2)) {
                case 0 -> 1;
                case 1 -> 2;
                case 2 -> 4;
                case 3 -> 3;
                default -> wait;
            };

            WAIT_STATE(victim, wait * PULSE_VIOLENCE);
            WAIT_STATE(ch, gsn_bash.beats);
            victim.position = POS_RESTING;
            damage_bash = (ch.damroll / 2) + number_range(4, 4 + 4 * ch.size + chance / 10);
            damage(ch, victim, damage_bash, gsn_bash, DAM_BASH, true);

        } else {
            damage(ch, victim, 0, gsn_bash, DAM_BASH, true);
            act("You fall flat on your face!",
                    ch, null, victim, TO_CHAR);
            act("$n falls flat on $s face.",
                    ch, null, victim, TO_NOTVICT);
            act("You evade $n's bash, causing $m to fall flat on $s face.",
                    ch, null, victim, TO_VICT);
            check_improve(ch, gsn_bash, false, 1);
            ch.position = POS_RESTING;
            WAIT_STATE(ch, gsn_bash.beats * 3 / 2);
        }
        if (!(IS_NPC(victim)) && !(IS_NPC(ch)) && victim.position > POS_STUNNED
                && !FightingCheck) {
            if (!can_see(victim, ch)) {
                do_yell(victim, "Help! Someone is bashing me!");
            } else {
                var buf = new TextBuffer();
                buf.sprintf("Help! %s is bashing me!",
                        (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(victim)) ? ch.doppel.name : ch.name);
                do_yell(victim, buf.toString());
            }
        }
    }

    static void do_dirt(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        int chance;

        if (skill_failure_check(ch, gsn_dirt, false, OFF_KICK_DIRT, "You get your feet dirty.\n")) {
            return;
        }

        var FightingCheck = ch.fighting != null;

        var argb = new StringBuilder();
        one_argument(argument, argb);

        chance = get_skill(ch, gsn_dirt);
        var arg = argb.toString();
        if (arg.isEmpty()) {
            victim = ch.fighting;
            if (victim == null) {
                send_to_char("But you aren't in combat!\n", ch);
                return;
            }
        } else if ((victim = get_char_room(ch, arg)) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (IS_AFFECTED(ch, AFF_FLYING)) {
            send_to_char("While flying?.\n", ch);
            return;
        }

        if (IS_AFFECTED(victim, AFF_BLIND)) {
            act("$e's already been blinded.", ch, null, victim, TO_CHAR);
            return;
        }

        if (victim == ch) {
            send_to_char("Very funny.\n", ch);
            return;
        }

        if (MOUNTED(victim) != null) {
            send_to_char("You can't dirt a riding one!\n", ch);
            return;
        }

        if (is_safe(ch, victim)) {
            return;
        }

        if (IS_AFFECTED(ch, AFF_CHARM) && ch.master == victim) {
            act("But $N is such a good friend!", ch, null, victim, TO_CHAR);
            return;
        }

        /* modifiers */

        /* dexterity */
        chance += get_curr_stat(ch, STAT_DEX);
        chance -= 2 * get_curr_stat(victim, STAT_DEX);

        /* speed  */
        if (IS_SET(ch.off_flags, OFF_FAST) || IS_AFFECTED(ch, AFF_HASTE)) {
            chance += 10;
        }
        if (IS_SET(victim.off_flags, OFF_FAST) || IS_AFFECTED(victim, OFF_FAST)) {
            chance -= 25;
        }

        /* level */
        chance += (ch.level - victim.level) * 2;

        if (chance % 5 == 0) {
            chance += 1;
        }

        /* terrain */

        switch (ch.in_room.sector_type) {
            case (SECT_INSIDE) -> chance -= 20;
            case (SECT_CITY) -> chance -= 10;
            case (SECT_FIELD) -> chance += 5;
            case (SECT_FOREST) -> {
            }
            case (SECT_HILLS) -> {
            }
            case (SECT_MOUNTAIN) -> chance -= 10;
            case (SECT_WATER_SWIM) -> chance = 0;
            case (SECT_WATER_NOSWIM) -> chance = 0;
            case (SECT_AIR) -> chance = 0;
            case (SECT_DESERT) -> chance += 10;
        }

        if (chance == 0) {
            send_to_char("There isn't any dirt to kick.\n", ch);
            return;
        }

        /* now the attack */
        if (number_percent() < chance) {
            act("$n is blinded by the dirt in $s eyes!", victim, null, null, TO_ROOM);
            damage(ch, victim, number_range(2, 5), gsn_dirt, DAM_NONE, true);
            send_to_char("You can't see a thing!\n", victim);
            check_improve(ch, gsn_dirt, true, 2);
            WAIT_STATE(ch, gsn_dirt.beats);

            var af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_dirt;
            af.level = ch.level;
            af.duration = 0;
            af.location = APPLY_HITROLL;
            af.modifier = -4;
            af.bitvector = AFF_BLIND;

            affect_to_char(victim, af);
        } else {
            damage(ch, victim, 0, gsn_dirt, DAM_NONE, true);
            check_improve(ch, gsn_dirt, false, 2);
            WAIT_STATE(ch, gsn_dirt.beats);
        }
        if (!(IS_NPC(victim)) && !(IS_NPC(ch)) && victim.position > POS_STUNNED
                && !FightingCheck) {
            if (!can_see(victim, ch)) {
                do_yell(victim, "Someone just kicked dirt in my eyes!");
            } else {
                var buf = new TextBuffer();
                buf.sprintf("Die, %s!  You dirty fool!",
                        (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(victim)) ? ch.doppel.name : ch.name);
                do_yell(victim, buf.toString());
            }
        }

    }

    static void do_trip(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        int chance;

        if (skill_failure_check(ch, gsn_trip, false, OFF_TRIP, "Tripping?  What's that?\n")) {
            return;
        }

        var FightingCheck = ch.fighting != null;

        var argb = new StringBuilder();
        one_argument(argument, argb);

        chance = get_skill(ch, gsn_dirt);
        var arg = argb.toString();
        if (argb.isEmpty()) {
            victim = ch.fighting;
            if (victim == null) {
                send_to_char("But you aren't fighting anyone!\n", ch);
                return;
            }
        } else if ((victim = get_char_room(ch, arg)) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (MOUNTED(victim) != null) {
            send_to_char("You can't trip a riding one!\n", ch);
            return;
        }

        if (is_safe(ch, victim)) {
            return;
        }

        if (IS_AFFECTED(victim, AFF_FLYING)) {
            act("$S feet aren't on the ground.", ch, null, victim, TO_CHAR);
            return;
        }

        if (victim.position < POS_FIGHTING) {
            act("$N is already down.", ch, null, victim, TO_CHAR);
            return;
        }

        if (victim == ch) {
            send_to_char("You fall flat on your face!\n", ch);
            WAIT_STATE(ch, 2 * gsn_trip.beats);
            act("$n trips over $s own feet!", ch, null, null, TO_ROOM);
            return;
        }

        if (IS_AFFECTED(ch, AFF_CHARM) && ch.master == victim) {
            act("$N is your beloved master.", ch, null, victim, TO_CHAR);
            return;
        }

        /* modifiers */

        /* size */
        if (ch.size < victim.size) {
            chance += (ch.size - victim.size) * 10;  /* bigger = harder to trip */
        }

        /* dex */
        chance += get_curr_stat(ch, STAT_DEX);
        chance -= get_curr_stat(victim, STAT_DEX) * 3 / 2;

        if (IS_AFFECTED(ch, AFF_FLYING)) {
            chance -= 10;
        }

        /* speed */
        if (IS_SET(ch.off_flags, OFF_FAST) || IS_AFFECTED(ch, AFF_HASTE)) {
            chance += 10;
        }
        if (IS_SET(victim.off_flags, OFF_FAST) || IS_AFFECTED(victim, AFF_HASTE)) {
            chance -= 20;
        }

        /* level */
        chance += (ch.level - victim.level) * 2;

        /* now the attack */
        if (number_percent() < chance) {
            act("$n trips you and you go down!", ch, null, victim, TO_VICT);
            act("You trip $N and $N goes down!", ch, null, victim, TO_CHAR);
            act("$n trips $N, sending $M to the ground.", ch, null, victim, TO_NOTVICT);
            check_improve(ch, gsn_trip, true, 1);

            WAIT_STATE(victim, 2 * PULSE_VIOLENCE);
            WAIT_STATE(ch, gsn_trip.beats);
            victim.position = POS_RESTING;
            damage(ch, victim, number_range(2, 2 + 2 * victim.size), gsn_trip,
                    DAM_BASH, true);
        } else {
            damage(ch, victim, 0, gsn_trip, DAM_BASH, true);
            WAIT_STATE(ch, gsn_trip.beats * 2 / 3);
            check_improve(ch, gsn_trip, false, 1);
        }
        if (!(IS_NPC(victim)) && !(IS_NPC(ch)) && victim.position > POS_STUNNED
                && !FightingCheck) {
            if (!can_see(victim, ch)) {
                do_yell(victim, " Help! Someone just tripped me!");
            } else {
                var buf = new TextBuffer();
                buf.sprintf("Help! %s just tripped me!",
                        (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(victim)) ? ch.doppel.name : ch.name);
                do_yell(victim, buf.toString());
            }
        }
    }


    static void do_backstab(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        OBJ_DATA obj;

        var arg = new StringBuilder();
        one_argument(argument, arg);

        if (skill_failure_check(ch, gsn_backstab, false, 0, "You don't know how to backstab.\n")) {
            return;
        }

        if (arg.isEmpty()) {
            send_to_char("Backstab whom?\n", ch);
            return;
        }

        if ((victim = get_char_room(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }
        if ((IS_NPC(ch)) && (!(IS_NPC(victim)))) {
            return;
        }


        if (victim == ch) {
            send_to_char("How can you sneak up on yourself?\n", ch);
            return;
        }

        if (is_safe(ch, victim)) {
            return;
        }

        if ((obj = get_wield_char(ch, false)) == null || attack_table[obj.value[3]].damage != DAM_PIERCE) {
            send_to_char("You need to wield a piercing weapon to backstab.\n", ch);
            return;
        }

        if (victim.fighting != null) {
            send_to_char("You can't backstab a fighting person.\n", ch);
            return;
        }


        WAIT_STATE(ch, gsn_backstab.beats);

        if (victim.hit < (0.7 * victim.max_hit) && (IS_AWAKE(victim))) {
            act("$N is hurt and suspicious ... you couldn't sneak up.", ch, null, victim, TO_CHAR);
            return;
        }

        if (current_time - victim.last_fight_time < 300 && IS_AWAKE(victim)) {
            act("$N is suspicious ... you couldn't sneak up.", ch, null, victim, TO_CHAR);
            return;
        }

        if (!IS_AWAKE(victim) || IS_NPC(ch) || number_percent() < get_skill(ch, gsn_backstab)) {
            check_improve(ch, gsn_backstab, true, 1);
            if (!IS_NPC(ch) && number_percent() < (get_skill(ch, gsn_dual_backstab) / 10) * 8) {
                check_improve(ch, gsn_dual_backstab, true, 1);
                one_hit(ch, victim, gsn_backstab, false);
                one_hit(ch, victim, gsn_dual_backstab, false);
            } else {
                check_improve(ch, gsn_dual_backstab, false, 1);
                multi_hit(ch, victim, gsn_backstab);
            }
        } else {
            check_improve(ch, gsn_backstab, false, 1);
            damage(ch, victim, 0, gsn_backstab, DAM_NONE, true);
        }
        /* Player shouts if he doesn't die */
        if (!(IS_NPC(victim)) && !(IS_NPC(ch)) && victim.position == POS_FIGHTING) {
            if (!can_see(victim, ch)) {
                do_yell(victim, "Help! I've been backstabbed!");
            } else {
                var buf = new TextBuffer();
                buf.sprintf("Die, %s, you backstabbing scum!",
                        (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(victim)) ? ch.doppel.name : ch.name);
                do_yell(victim, buf.toString());
            }
        }
    }

    static void do_cleave(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;

        if (skill_failure_check(ch, gsn_cleave, false, 0, "You don't know how to cleave.\n")) {
            return;
        }
        var arg = new StringBuilder();
        one_argument(argument, arg);

        if (ch.master != null && IS_NPC(ch)) {
            return;
        }

        if (arg.isEmpty()) {
            send_to_char("Cleave whom?\n", ch);
            return;
        }

        if ((victim = get_char_room(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (victim == ch) {
            send_to_char("How can you sneak up on yourself?\n", ch);
            return;
        }

        if (is_safe(ch, victim)) {
            return;
        }

        if (get_wield_char(ch, false) == null) {
            send_to_char("You need to wield a weapon to cleave.\n", ch);
            return;
        }


        if (victim.fighting != null) {
            send_to_char("You can't cleave a fighting person.\n", ch);
            return;
        }

        if ((victim.hit < (0.9 * victim.max_hit)) && (IS_AWAKE(victim))) {
            act("$N is hurt and suspicious ... you can't sneak up.", ch, null, victim, TO_CHAR);
            return;
        }

        WAIT_STATE(ch, gsn_cleave.beats);
        if (!IS_AWAKE(victim) || IS_NPC(ch) || number_percent() < get_skill(ch, gsn_cleave)) {
            check_improve(ch, gsn_cleave, true, 1);
            multi_hit(ch, victim, gsn_cleave);
        } else {
            check_improve(ch, gsn_cleave, false, 1);
            damage(ch, victim, 0, gsn_cleave, DAM_NONE, true);
        }
        /* Player shouts if he doesn't die */
        if (!(IS_NPC(victim)) && !(IS_NPC(ch)) && victim.position == POS_FIGHTING) {
            if (!can_see(victim, ch)) {
                do_yell(victim, "Help! Someone is attacking me!");
            } else {
                var buf = new TextBuffer();
                buf.sprintf("Die, %s, you butchering fool!",
                        (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(victim)) ? ch.doppel.name : ch.name);
                do_yell(victim, buf.toString());
            }
        }
    }

    static void do_ambush(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;

        if (skill_failure_check(ch, gsn_ambush, false, 0, "You don't know how to ambush.\n")) {
            return;
        }

        var arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.isEmpty()) {
            send_to_char("Ambush whom?\n", ch);
            return;
        }

        if ((victim = get_char_room(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (victim == ch) {
            send_to_char("How can you ambush yourself?\n", ch);
            return;
        }


        if (!IS_AFFECTED(ch, AFF_CAMOUFLAGE) || can_see(victim, ch)) {
            send_to_char("But they can see you.\n", ch);
            return;
        }

        if (is_safe(ch, victim)) {
            return;
        }

        WAIT_STATE(ch, gsn_ambush.beats);
        if (!IS_AWAKE(victim)
                || IS_NPC(ch)
                || number_percent() < get_skill(ch, gsn_ambush)) {
            check_improve(ch, gsn_ambush, true, 1);
            multi_hit(ch, victim, gsn_ambush);
        } else {
            check_improve(ch, gsn_ambush, false, 1);
            damage(ch, victim, 0, gsn_ambush, DAM_NONE, true);
        }

        /* Player shouts if he doesn't die */
        if (!(IS_NPC(victim)) && !(IS_NPC(ch))
                && victim.position == POS_FIGHTING) {
            if (!can_see(victim, ch)) {
                do_yell(victim, "Help! I've been ambushed by someone!");
            } else {
                var buf = new TextBuffer();
                buf.sprintf("Help! I've been ambushed by %s!",
                        (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(victim)) ? ch.doppel.name : ch.name);
                do_yell(victim, buf.toString());
            }
        }
    }


    static void do_rescue(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        CHAR_DATA fch;
        var arg = new StringBuilder();
        one_argument(argument, arg);
        if (arg.isEmpty()) {
            send_to_char("Rescue whom?\n", ch);
            return;
        }

        if ((victim = get_char_room(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (victim == ch) {
            send_to_char("What about fleeing instead?\n", ch);
            return;
        }

        if (!IS_NPC(ch) && IS_NPC(victim)) {
            send_to_char("Doesn't need your help!\n", ch);
            return;
        }

        if (ch.fighting == victim) {
            send_to_char("Too late.\n", ch);
            return;
        }

        if ((fch = victim.fighting) == null) {
            send_to_char("That person is not fighting right now.\n", ch);
            return;
        }
        if (IS_NPC(ch) && ch.master != null && IS_NPC(victim)) {
            return;
        }

        if (is_safe(ch, fch)) {
            return;
        }

        if (ch.master != null) {
            if (is_safe(ch.master, fch)) {
                return;
            }
        }

        WAIT_STATE(ch, gsn_rescue.beats);
        if ((!IS_NPC(ch)
                && number_percent() > get_skill(ch, gsn_rescue))
                || (victim.level > (ch.level + 30))) {
            send_to_char("You fail the rescue.\n", ch);
            check_improve(ch, gsn_rescue, false, 1);
            return;
        }

        act("You rescue $N!", ch, null, victim, TO_CHAR);
        act("$n rescues you!", ch, null, victim, TO_VICT);
        act("$n rescues $N!", ch, null, victim, TO_NOTVICT);
        check_improve(ch, gsn_rescue, true, 1);

        stop_fighting(fch, false);
        stop_fighting(victim, false);

        set_fighting(ch, fch);
        set_fighting(fch, ch);
    }


    static void do_kick(CHAR_DATA ch) {
        CHAR_DATA victim;
        int kick_dam;
        int chance;

        if (skill_failure_check(ch, gsn_kick, false, OFF_KICK, "You better leave the martial arts to fighters.\n")) {
            return;
        }

        if ((victim = ch.fighting) == null) {
            send_to_char("You aren't fighting anyone.\n", ch);
            return;
        }
        chance = number_percent();
        if (IS_AFFECTED(ch, AFF_FLYING)) {
            chance *= 1.1;
        }
        WAIT_STATE(ch, gsn_kick.beats);
        if (IS_NPC(ch) || chance < get_skill(ch, gsn_kick)) {
            kick_dam = number_range(1, ch.level);
            if ((ch.clazz == Clazz.SAMURAI) && (get_eq_char(ch, WEAR_FEET) == null)) {
                kick_dam *= 2;
            }
            kick_dam += ch.damroll / 2;
            damage(ch, victim, kick_dam, gsn_kick, DAM_BASH, true);
            check_improve(ch, gsn_kick, true, 1);
        } else {
            damage(ch, victim, 0, gsn_kick, DAM_BASH, true);
            check_improve(ch, gsn_kick, false, 1);
        }

    }

    static void do_circle(CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA person;
        boolean second;

        if (skill_failure_check(ch, gsn_circle, false, 0, null)) {
            return;
        }

        if ((victim = ch.fighting) == null) {
            send_to_char("You aren't fighting anyone.\n", ch);
            return;
        }

        second = false;
        if ((get_wield_char(ch, false) == null) ||
                attack_table[get_wield_char(ch, false).value[3]].damage != DAM_PIERCE) {
            if ((get_wield_char(ch, true) == null) ||
                    attack_table[get_wield_char(ch, true).value[3]].damage != DAM_PIERCE) {
                send_to_char("You must wield a piercing weapon to circle stab.\n", ch);
                return;
            }
            second = true;
        }

        if (is_safe(ch, victim)) {
            return;
        }

        WAIT_STATE(ch, gsn_circle.beats);

        for (person = ch.in_room.people; person != null;
             person = person.next_in_room) {
            if (person.fighting == ch) {
                send_to_char("You can't circle while defending yourself.\n", ch);
                return;
            }
        }

        if (IS_NPC(ch) || number_percent() < get_skill(ch, gsn_circle)) {
            one_hit(ch, victim, gsn_circle, second);
            check_improve(ch, gsn_circle, true, 1);
        } else {
            damage(ch, victim, 0, gsn_circle, 0, true);
            check_improve(ch, gsn_circle, false, 1);
        }

    }


    static void do_disarm(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        int chance, hth, ch_weapon, vict_weapon, ch_vict_weapon;
        var disarm_second = false;


        if (skill_failure_check(ch, gsn_disarm, false, OFF_DISARM, "You don't know how to disarm opponents.\n")) {
            return;
        }

        hth = 0;

        if (ch.master != null && IS_NPC(ch)) {
            return;
        }

        chance = get_skill(ch, gsn_disarm);

        if (get_wield_char(ch, false) == null
                && ((hth = get_skill(ch, gsn_hand_to_hand)) == 0
                || (IS_NPC(ch) && !IS_SET(ch.off_flags, OFF_DISARM)))) {
            send_to_char("You must wield a weapon to disarm.\n", ch);
            return;
        }

        if ((victim = ch.fighting) == null) {
            send_to_char("You aren't fighting anyone.\n", ch);
            return;
        }

        var arg = new StringBuilder();
        one_argument(argument, arg);
        if (!IS_NPC(ch) && !arg.isEmpty()) {
            disarm_second = is_name(arg.toString(), "second");
        }

        if (get_wield_char(victim, disarm_second) == null) {
            send_to_char("Your opponent is not wielding a weapon.\n", ch);
            return;
        }

        /* find weapon skills */
        ch_weapon = get_weapon_skill(ch, get_weapon_sn(ch, false));

        vict_weapon = get_weapon_skill(victim, get_weapon_sn(victim, disarm_second));
        ch_vict_weapon = get_weapon_skill(ch, get_weapon_sn(victim, disarm_second));

        /* modifiers */

        /* skill */
        if (get_wield_char(ch, false) == null) {
            chance = chance * hth / 150;
        } else {
            chance = chance * ch_weapon / 100;
        }

        chance += (ch_vict_weapon / 2 - vict_weapon) / 2;

        /* dex vs. strength */
        chance += get_curr_stat(ch, STAT_DEX);
        chance -= 2 * get_curr_stat(victim, STAT_STR);

        /* level */
        chance += (ch.level - victim.level) * 2;

        /* and now the attack */
        if (number_percent() < chance) {
            WAIT_STATE(ch, gsn_disarm.beats);
            disarm(ch, victim, disarm_second);
            check_improve(ch, gsn_disarm, true, 1);
        } else {
            WAIT_STATE(ch, gsn_disarm.beats);
            act("You fail to disarm $N.", ch, null, victim, TO_CHAR);
            act("$n tries to disarm you, but fails.", ch, null, victim, TO_VICT);
            act("$n tries to disarm $N, but fails.", ch, null, victim, TO_NOTVICT);
            check_improve(ch, gsn_disarm, false, 1);
        }
    }


    static void do_nerve(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        var arg = new StringBuilder();
        one_argument(argument, arg);

        if (skill_failure_check(ch, gsn_nerve, false, 0, null)) {
            return;
        }

        if (ch.fighting == null) {
            send_to_char("You aren't fighting anyone.\n", ch);
            return;
        }

        victim = ch.fighting;

        if (is_safe(ch, victim)) {
            return;
        }

        if (is_affected(ch, gsn_nerve)) {
            send_to_char("You cannot weaken that character any more.\n", ch);
            return;
        }
        WAIT_STATE(ch, gsn_nerve.beats);

        if (IS_NPC(ch) ||
                number_percent() < (get_skill(ch, gsn_nerve) + ch.level
                        + get_curr_stat(ch, STAT_DEX)) / 2) {
            var af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_nerve;
            af.level = ch.level;
            af.duration = ch.level * PULSE_VIOLENCE / PULSE_TICK;
            af.location = APPLY_STR;
            af.modifier = -3;
            af.bitvector = 0;

            affect_to_char(victim, af);
            act("You weaken $N with your nerve pressure.", ch, null, victim, TO_CHAR);
            act("$n weakens you with $s nerve pressure.", ch, null, victim, TO_VICT);
            act("$n weakens $N with $s nerve pressure.", ch, null, victim, TO_NOTVICT);
            check_improve(ch, gsn_nerve, true, 1);
        } else {
            send_to_char("You press the wrong points and fail.\n", ch);
            act("$n tries to weaken you with nerve pressure, but fails.",
                    ch, null, victim, TO_VICT);
            act("$n tries to weaken $N with nerve pressure, but fails.",
                    ch, null, victim, TO_NOTVICT);
            check_improve(ch, gsn_nerve, false, 1);
        }

        multi_hit(victim, ch, null);

        if (!(IS_NPC(victim)) && !(IS_NPC(ch))
                && victim.position != POS_FIGHTING) {
            if (!can_see(victim, ch)) {
                do_yell(victim, "Help! I'm being attacked by someone!");
            } else {
                var buf = new TextBuffer();
                buf.sprintf("Help! I'm being attacked by %s!",
                        (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(victim)) ? ch.doppel.name : ch.name);
                do_yell(victim, buf.toString());
            }
        }
    }

    static void do_endure(CHAR_DATA ch) {

        if (skill_failure_check(ch, gsn_endure, false, 0, "You lack the concentration.\n")) {
            return;
        }

        if (is_affected(ch, gsn_endure)) {
            send_to_char("You cannot endure more concentration.\n", ch);
            return;
        }


        WAIT_STATE(ch, gsn_endure.beats);
        var af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = gsn_endure;
        af.level = ch.level;
        af.duration = ch.level / 4;
        af.location = APPLY_SAVING_SPELL;
        af.modifier = -1 * (get_skill(ch, gsn_endure) / 10);
        af.bitvector = 0;

        affect_to_char(ch, af);

        send_to_char("You prepare yourself for magical encounters.\n", ch);
        act("$n concentrates for a moment, then resumes $s position.",
                ch, null, null, TO_ROOM);
        check_improve(ch, gsn_endure, true, 1);
    }

    static void do_tame(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        var arg = new StringBuilder();
        one_argument(argument, arg);

        if (skill_failure_check(ch, gsn_tame, true, 0, "You lack the skills to tame anyone.\n")) {
            return;
        }

        if (arg.isEmpty()) {
            send_to_char("You are beyond taming.\n", ch);
            act("$n tries to tame $mself but fails miserably.", ch, null, null, TO_ROOM);
            return;
        }

        if ((victim = get_char_room(ch, arg.toString())) == null) {
            send_to_char("They're not here.\n", ch);
            return;
        }

        if (!IS_NPC(victim)) {
            act("$N is beyond taming.", ch, null, victim, TO_CHAR);
            return;
        }

        if (!IS_SET(victim.act, ACT_AGGRESSIVE)) {
            act("$N is not usually aggressive.", ch, null, victim, TO_CHAR);
            return;
        }

        WAIT_STATE(ch, gsn_tame.beats);

        if (number_percent() < get_skill(ch, gsn_tame) + 15
                + 4 * (ch.level - victim.level)) {
            victim.act = REMOVE_BIT(victim.act, ACT_AGGRESSIVE);
            victim.affected_by = SET_BIT(victim.affected_by, AFF_CALM);
            send_to_char("You calm down.\n", victim);
            act("You calm $N down.", ch, null, victim, TO_CHAR);
            act("$n calms $N down.", ch, null, victim, TO_NOTVICT);
            stop_fighting(victim, true);
            check_improve(ch, gsn_tame, true, 1);
        } else {
            send_to_char("You failed.\n", ch);
            act("$n tries to calm down $N but fails.", ch, null, victim, TO_NOTVICT);
            act("$n tries to calm you down but fails.", ch, null, victim, TO_VICT);
            check_improve(ch, gsn_tame, false, 1);
        }
    }

    static void do_assassinate(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;

        if (skill_failure_check(ch, gsn_assassinate, false, 0, null)) {
            return;
        }

        var arg = new StringBuilder();

        one_argument(argument, arg);

        if (ch.master != null && IS_NPC(ch)) {
            return;
        }

        if (IS_AFFECTED(ch, AFF_CHARM)) {
            send_to_char("You don't want to kill your beloved master.\n", ch);
            return;
        }

        if (arg.isEmpty()) {
            send_to_char("Assassinate whom?\n", ch);
            return;
        }

        if ((victim = get_char_room(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (victim == ch) {
            send_to_char("Suicide is against your way.\n", ch);
            return;
        }

        if (is_safe(ch, victim)) {
            return;
        }

        if (IS_IMMORTAL(victim) && !IS_NPC(victim)) {
            send_to_char("Your hands pass through.\n", ch);
            return;
        }

        if (victim.fighting != null) {
            send_to_char("You can't assassinate a fighting person.\n", ch);
            return;
        }

        if ((get_wield_char(ch, false) != null) ||
                (get_hold_char(ch) != null)) {
            send_to_char(
                    "You need both hands free to assassinate somebody.\n", ch);
            return;
        }

        if ((victim.hit < victim.max_hit) &&
                (can_see(victim, ch)) &&
                (IS_AWAKE(victim))) {
            act("$N is hurt and suspicious ... you can't sneak up.",
                    ch, null, victim, TO_CHAR);
            return;
        }

/*
    if (IS_SET(victim.imm_flags, IMM_WEAPON))
      {
    act("$N seems immune to your assassination attempt.", ch, null,
         victim, TO_CHAR);
    act("$N seems immune to $n's assassination attempt.", ch, null,
        victim, TO_ROOM);
    return;
      }
*/
        WAIT_STATE(ch, gsn_assassinate.beats);
        if (IS_NPC(ch) ||
                !IS_AWAKE(victim)
                || number_percent() < get_skill(ch, gsn_assassinate)) {
            multi_hit(ch, victim, gsn_assassinate);
        } else {
            check_improve(ch, gsn_assassinate, false, 1);
            damage(ch, victim, 0, gsn_assassinate, DAM_NONE, true);
        }
        /* Player shouts if he doesn't die */
        if (!(IS_NPC(victim)) && !(IS_NPC(ch))
                && victim.position == POS_FIGHTING) {
            if (!can_see(victim, ch)) {
                do_yell(victim, "Help! Someone tried to assassinate me!");
            } else {
                var buf = new TextBuffer();
                buf.sprintf("Help! %s tried to assassinate me!",
                        (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(victim)) ? ch.doppel.name : ch.name);
                do_yell(victim, buf.toString());
            }
        }
    }


    static void do_caltraps(CHAR_DATA ch) {
        var victim = ch.fighting;

        if (skill_failure_check(ch, gsn_caltraps, false, 0, "Caltraps? Is that a dance step?\n")) {
            return;
        }

        if (victim == null) {
            send_to_char("You must be in combat.\n", ch);
            return;
        }

        if (is_safe(ch, victim)) {
            return;
        }

        act("You throw a handful of sharp spikes at the feet of $N.",
                ch, null, victim, TO_CHAR);
        act("$n throws a handful of sharp spikes at your feet!",
                ch, null, victim, TO_VICT);

        WAIT_STATE(ch, gsn_caltraps.beats);

        if (!IS_NPC(ch) && number_percent() >= get_skill(ch, gsn_caltraps)) {
            damage(ch, victim, 0, gsn_caltraps, DAM_PIERCE, true);
            check_improve(ch, gsn_caltraps, false, 1);
            return;
        }

        damage(ch, victim, ch.level, gsn_caltraps, DAM_PIERCE, true);

        if (!is_affected(victim, gsn_caltraps)) {
            var tohit = new AFFECT_DATA();

            tohit.where = TO_AFFECTS;
            tohit.type = gsn_caltraps;
            tohit.level = ch.level;
            tohit.duration = -1;
            tohit.location = APPLY_HITROLL;
            tohit.modifier = -5;
            tohit.bitvector = 0;
            affect_to_char(victim, tohit);

            var todam = new AFFECT_DATA();
            todam.where = TO_AFFECTS;
            todam.type = gsn_caltraps;
            todam.level = ch.level;
            todam.duration = -1;
            todam.location = APPLY_DAMROLL;
            todam.modifier = -5;
            todam.bitvector = 0;
            affect_to_char(victim, todam);

            var todex = new AFFECT_DATA();
            todex.type = gsn_caltraps;
            todex.level = ch.level;
            todex.duration = -1;
            todex.location = APPLY_DEX;
            todex.modifier = -5;
            todex.bitvector = 0;
            affect_to_char(victim, todex);

            act("$N starts limping.", ch, null, victim, TO_CHAR);
            act("You start to limp.", ch, null, victim, TO_VICT);
            check_improve(ch, gsn_caltraps, true, 1);
        }
    }


    static void do_throw(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        int chance;
        var arg = new StringBuilder();
        argument = one_argument(argument, arg);

        if (!str_cmp(arg.toString(), "spear")) {
            do_throw_spear(ch, argument);
            return;
        }

        if (skill_failure_check(ch, gsn_throw, false, 0, "A clutz like you couldn't throw down a worm.\n")) {
            return;
        }

        if (IS_AFFECTED(ch, AFF_FLYING)) {
            send_to_char("Your feet should touch the ground to balance\n", ch);
            return;
        }

        if ((victim = ch.fighting) == null) {
            send_to_char("You aren't fighting anyone.\n", ch);
            return;
        }

        if (IS_AFFECTED(ch, AFF_CHARM) && ch.master == victim) {
            act("But $N is your friend!", ch, null, victim, TO_CHAR);
            return;
        }

        if (is_safe(ch, victim)) {
            return;
        }

        WAIT_STATE(ch, gsn_throw.beats);

        if (is_affected(victim, gsn_protective_shield)) {
            act("{YYou fail to reach $s arm.{x", ch, null, victim, TO_CHAR, POS_FIGHTING);
            act("{Y$n fails to throw you.{x", ch, null, victim, TO_VICT, POS_FIGHTING);
            act("{Y$n fails to throw $N.{x", ch, null, victim, TO_NOTVICT, POS_FIGHTING);
            return;
        }

        chance = get_skill(ch, gsn_throw);

        if (ch.size < victim.size) {
            chance += (ch.size - victim.size) * 10;
        } else {
            chance += (ch.size - victim.size) * 25;
        }

        /* stats */
        chance += get_curr_stat(ch, STAT_STR);
        chance -= get_curr_stat(victim, STAT_DEX) * 4 / 3;

        if (IS_AFFECTED(victim, AFF_FLYING)) {
            chance += 10;
        }

        /* speed */
        if (IS_SET(ch.off_flags, OFF_FAST)) {
            chance += 10;
        }
        if (IS_SET(victim.off_flags, OFF_FAST)) {
            chance -= 20;
        }

        /* level */
        chance += (ch.level - victim.level) * 2;

        if (IS_NPC(ch) || number_percent() < chance) {
            act("You throw $N to the ground with stunning force.",
                    ch, null, victim, TO_CHAR);
            act("$n throws you to the ground with stunning force.",
                    ch, null, victim, TO_VICT);
            act("$n throws $N to the ground with stunning force.",
                    ch, null, victim, TO_NOTVICT);
            WAIT_STATE(victim, 2 * PULSE_VIOLENCE);

            damage(ch, victim, ch.level + get_curr_stat(ch, STAT_STR),
                    gsn_throw, DAM_BASH, true);
            check_improve(ch, gsn_throw, true, 1);
        } else {
            act("You fail to grab your opponent.", ch, null, null, TO_CHAR);
            act("$N tries to throw you, but fails.", victim, null, ch, TO_CHAR);
            act("$n tries to grab $N's arm.", ch, null, victim, TO_NOTVICT);
            check_improve(ch, gsn_throw, false, 1);
        }

    }

    static void do_strangle(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        int chance;

        if (skill_failure_check(ch, gsn_strangle, false, 0, "You lack the skill to strangle.\n")) {
            return;
        }

        if ((victim = get_char_room(ch, argument)) == null) {
            send_to_char("You do not see that person here.\n", ch);
            return;
        }

        if (ch == victim) {
            send_to_char("Even you are not that stupid.\n", ch);
            return;
        }

        if (IS_AFFECTED(ch, AFF_CHARM) && victim == ch.leader) {
            send_to_char("You don't want to grap your beloved masters' neck.\n", ch);
            return;
        }

        if (IS_AFFECTED(victim, AFF_SLEEP)) {
            act("$E is already asleep.", ch, null, victim, TO_CHAR);
            return;
        }

        if (is_affected(victim, gsn_neckguard)) {
            act("$N's guarding $S neck.", ch, null, victim, TO_CHAR);
            return;
        }

        if (is_safe(ch, victim)) {
            return;
        }

        victim.last_fight_time = current_time;
        ch.last_fight_time = current_time;

        WAIT_STATE(ch, gsn_strangle.beats);

        if (IS_NPC(ch)) {
            chance = UMIN(35, ch.level / 2);
        } else {
            chance = (int) (0.6 * get_skill(ch, gsn_strangle));
        }

        if (IS_NPC(victim) && victim.pIndexData.pShop != null) {
            chance -= 40;
        }

        if (number_percent() < chance) {
            act("You grab hold of $N's neck and put $M to sleep.",
                    ch, null, victim, TO_CHAR);
            act("$n grabs hold of your neck and puts you to sleep.",
                    ch, null, victim, TO_VICT);
            act("$n grabs hold of $N's neck and puts $M to sleep.",
                    ch, null, victim, TO_NOTVICT);
            check_improve(ch, gsn_strangle, true, 1);

            var af = new AFFECT_DATA();
            af.type = gsn_strangle;
            af.where = TO_AFFECTS;
            af.level = ch.level;
            af.duration = ch.level / 50 + 1;
            af.location = APPLY_NONE;
            af.modifier = 0;
            af.bitvector = AFF_SLEEP;
            affect_join(victim, af);

            if (IS_AWAKE(victim)) {
                victim.position = POS_SLEEPING;
            }
        } else {

            damage(ch, victim, 0, gsn_strangle, DAM_NONE, true);
            check_improve(ch, gsn_strangle, false, 1);
            if (!can_see(victim, ch)) {
                do_yell(victim, "Help! I'm being strangled by someone!");
            } else {
                var buf = new TextBuffer();
                buf.sprintf("Help! I'm being strangled by %s!",
                        (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(victim)) ? ch.doppel.name : ch.name);
                if (!IS_NPC(victim)) {
                    do_yell(victim, buf.toString());
                }
            }
            var af = new AFFECT_DATA();
            af.type = gsn_neckguard;
            af.where = TO_AFFECTS;
            af.level = victim.level;
            af.duration = 2 + victim.level / 50;
            af.modifier = 0;
            af.bitvector = 0;
            af.location = APPLY_NONE;
            affect_join(victim, af);
        }
    }

    static void do_blackjack(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        int chance;

        if (skill_failure_check(ch, gsn_blackjack, false, 0, null)) {
            return;
        }

        if ((victim = get_char_room(ch, argument)) == null) {
            send_to_char("You do not see that person here.\n", ch);
            return;
        }

        if (ch == victim) {
            send_to_char("You idiot?! Blackjack your self?!\n", ch);
            return;
        }


        if (IS_AFFECTED(ch, AFF_CHARM) && victim == ch.leader) {
            send_to_char("You don't want to hit your beloved masters' head with a full filled jack.\n", ch);
            return;
        }

        if (IS_AFFECTED(victim, AFF_SLEEP)) {
            act("$E is already asleep.", ch, null, victim, TO_CHAR);
            return;
        }

        if (is_affected(victim, gsn_headguard)) {
            act("$N's guarding $S head!.", ch, null, victim, TO_CHAR);
            return;
        }

        if (is_safe(ch, victim)) {
            return;
        }
        victim.last_fight_time = current_time;
        ch.last_fight_time = current_time;

        WAIT_STATE(ch, gsn_blackjack.beats);

        chance = (int) (0.5 * get_skill(ch, gsn_blackjack));
        chance += URANGE(0, (get_curr_stat(ch, STAT_DEX) - 20) * 2, 10);
        chance += can_see(victim, ch) ? 0 : 5;
        if (IS_NPC(victim)) {
            if (victim.pIndexData.pShop != null) {
                chance -= 40;
            }
        }

        if (IS_NPC(ch) ||
                number_percent() < chance) {
            act("You hit $N's head with a lead filled sack.",
                    ch, null, victim, TO_CHAR);
            act("You feel a sudden pain erupts through your skull!",
                    ch, null, victim, TO_VICT);
            act("$n whacks $N at the back of $S head with a heavy looking sack!  *OUCH*",
                    ch, null, victim, TO_NOTVICT);
            check_improve(ch, gsn_blackjack, true, 1);

            var af = new AFFECT_DATA();
            af.type = gsn_blackjack;
            af.where = TO_AFFECTS;
            af.level = ch.level;
            af.duration = ch.level / 50 + 1;
            af.location = APPLY_NONE;
            af.modifier = 0;
            af.bitvector = AFF_SLEEP;
            affect_join(victim, af);

            if (IS_AWAKE(victim)) {
                victim.position = POS_SLEEPING;
            }
        } else {

            damage(ch, victim, ch.level / 2, gsn_blackjack, DAM_NONE, true);
            check_improve(ch, gsn_blackjack, false, 1);

            if (!IS_NPC(victim)) {
                if (!can_see(victim, ch)) {
                    do_yell(victim, "Help! I'm being blackjacked by someone!");
                } else {
                    var buf = new TextBuffer();
                    buf.sprintf("Help! I'm being blackjacked by %s!",
                            (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(victim)) ? ch.doppel.name : ch.name);
                    if (!IS_NPC(victim)) {
                        do_yell(victim, buf.toString());
                    }
                }
            }

            var af = new AFFECT_DATA();
            af.type = gsn_headguard;
            af.where = TO_AFFECTS;
            af.level = victim.level;
            af.duration = 2 + victim.level / 50;
            af.modifier = 0;
            af.bitvector = 0;
            af.location = APPLY_NONE;
            affect_join(victim, af);
        }
    }


    static void do_bloodthirst(CHAR_DATA ch) {
        int chance, hp_percent;

        if (skill_failure_check(ch, gsn_bloodthirst, true, 0, "You're not that thirsty.\n")) {
            return;
        }

        chance = get_skill(ch, gsn_bloodthirst);

        if (IS_AFFECTED(ch, AFF_BLOODTHIRST)) {
            send_to_char("Your thirst for blood continues.\n", ch);
            return;
        }

        if (IS_AFFECTED(ch, AFF_CALM)) {
            send_to_char("You're feeling to mellow to be bloodthirsty.\n", ch);
            return;
        }

        if (ch.fighting == null) {
            send_to_char("You need to be fighting.\n", ch);
            return;
        }

        /* modifiers */

        hp_percent = 100 * ch.hit / ch.max_hit;
        chance += 25 - hp_percent / 2;

        if (number_percent() < chance) {

            WAIT_STATE(ch, PULSE_VIOLENCE);


            send_to_char("You hunger for blood!\n", ch);
            act("$n gets a bloodthirsty look in $s eyes.", ch, null, null, TO_ROOM);
            check_improve(ch, gsn_bloodthirst, true, 2);

            var af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_bloodthirst;
            af.level = ch.level;
            af.duration = 2 + ch.level / 18;
            af.modifier = 5 + ch.level / 4;
            af.bitvector = AFF_BLOODTHIRST;

            af.location = APPLY_HITROLL;
            affect_to_char(ch, af);

            af.location = APPLY_DAMROLL;
            affect_to_char(ch, af);

            af.modifier = -UMIN(ch.level - 5, 35);
            af.location = APPLY_AC;
            affect_to_char(ch, af);
        } else {
            WAIT_STATE(ch, 3 * PULSE_VIOLENCE);

            send_to_char("You feel bloodthirsty for a moment, but it passes.\n",
                    ch);
            check_improve(ch, gsn_bloodthirst, false, 2);
        }
    }


    static void do_spellbane(CHAR_DATA ch) {

        if (skill_failure_check(ch, gsn_spellbane, true, 0, null)) {
            return;
        }

        if (is_affected(ch, gsn_spellbane)) {
            send_to_char("You are already deflecting spells.\n", ch);
            return;
        }

        WAIT_STATE(ch, gsn_spellbane.beats);

        var af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = gsn_spellbane;
        af.level = ch.level;
        af.duration = ch.level / 3;
        af.location = APPLY_SAVING_SPELL;
        af.modifier = -ch.level / 4;
        af.bitvector = AFF_SPELLBANE;

        affect_to_char(ch, af);

        act("Your hatred of magic surrounds you.", ch, null, null, TO_CHAR);
        act("$n fills the air with $s hatred of magic.", ch, null, null, TO_ROOM);
        check_improve(ch, gsn_spellbane, true, 1);

    }

    static void do_resistance(CHAR_DATA ch) {
        if (skill_failure_check(ch, gsn_resistance, true, 0, null)) {
            return;
        }

        if (is_affected(ch, gsn_resistance)) {
            send_to_char("You are as resistant as you will get.\n", ch);
            return;
        }

        if (ch.mana < 50) {
            send_to_char("You cannot muster up the energy.\n", ch);
            return;
        }

        WAIT_STATE(ch, gsn_resistance.beats);

        if (IS_NPC(ch) || number_percent() < get_skill(ch, gsn_resistance)) {
            var af = new AFFECT_DATA();

            af.where = TO_AFFECTS;
            af.type = gsn_resistance;
            af.level = ch.level;
            af.duration = ch.level / 6;
            af.location = APPLY_NONE;
            af.modifier = 0;
            af.bitvector = AFF_PROTECTOR;

            affect_to_char(ch, af);
            ch.mana -= 50;

            act("You feel tough!", ch, null, null, TO_CHAR);
            act("$n looks tougher.", ch, null, null, TO_ROOM);
            check_improve(ch, gsn_resistance, true, 1);
        } else {
            ch.mana -= 25;

            send_to_char("You flex your muscles, but you don't feel tougher.\n", ch);
            act("$n flexes $s muscles, trying to look tough.",
                    ch, null, null, TO_ROOM);
            check_improve(ch, gsn_resistance, false, 1);
        }

    }

    static void do_trophy(CHAR_DATA ch, String argument) {
        int trophy_vnum;
        OBJ_DATA trophy;
        OBJ_DATA part;
        int level;
        var arg = new StringBuilder();
        one_argument(argument, arg);

        if (skill_failure_check(ch, gsn_trophy, false, 0, null)) {
            return;
        }

        if (is_affected(ch, gsn_trophy)) {
            send_to_char("But you've already got one trophy!\n", ch);
            return;
        }

        if (ch.mana < 30) {
            send_to_char("You feel too weak to concentrate on a trophy.\n", ch);
            return;
        }

        if (arg.isEmpty()) {
            send_to_char("Make a trophy of what?\n", ch);
            return;
        }

        if ((part = get_obj_carry(ch, arg.toString())) == null) {
            send_to_char("You do not have that body part.\n", ch);
            return;
        }

        if (number_percent() < (get_skill(ch, gsn_trophy) / 3) * 2) {
            send_to_char("You failed and destroyed it.\n", ch);
            extract_obj(part);
            return;
        }

        WAIT_STATE(ch, gsn_trophy.beats);

        if (part.pIndexData.vnum == OBJ_VNUM_SLICED_ARM) {
            trophy_vnum = OBJ_VNUM_BATTLE_PONCHO;
        } else if (part.pIndexData.vnum == OBJ_VNUM_SLICED_LEG) {
            trophy_vnum = OBJ_VNUM_BATTLE_PONCHO;
        } else if (part.pIndexData.vnum == OBJ_VNUM_SEVERED_HEAD) {
            trophy_vnum = OBJ_VNUM_BATTLE_PONCHO;
        } else if (part.pIndexData.vnum == OBJ_VNUM_TORN_HEART) {
            trophy_vnum = OBJ_VNUM_BATTLE_PONCHO;
        } else if (part.pIndexData.vnum == OBJ_VNUM_GUTS) {
            trophy_vnum = OBJ_VNUM_BATTLE_PONCHO;
        } else if (part.pIndexData.vnum == OBJ_VNUM_BRAINS) {
            send_to_char("Why don't you just eat those instead?\n", ch);
            return;
        } else {
            send_to_char("You can't make a trophy out of that!\n", ch);
            return;
        }

        if (part.from.isEmpty()) {
            send_to_char("Invalid body part.\n", ch);
            return;
        }

        if (!IS_NPC(ch) && number_percent() < ch.pcdata.learned[gsn_trophy.ordinal()]) {
            var af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_trophy;
            af.level = ch.level;
            af.duration = ch.level / 2;
            af.modifier = 0;
            af.bitvector = 0;

            af.location = 0;
            affect_to_char(ch, af);

            var buf = new TextBuffer();
            level = UMIN(part.level + 5, MAX_LEVEL);

            trophy = create_object(get_obj_index(trophy_vnum), level);
            trophy.timer = ch.level * 2;

            buf.sprintf(trophy.short_descr, part.from);
            trophy.short_descr = buf.toString();

            buf.sprintf(trophy.description, part.from);
            trophy.description = buf.toString();
            trophy.cost = 0;
            trophy.level = ch.level;
            ch.mana -= 30;

            af.where = TO_OBJECT;
            af.type = gsn_trophy;
            af.level = level;
            af.duration = -1;
            af.location = APPLY_DAMROLL;
            af.modifier = ch.level / 5;
            af.bitvector = 0;
            affect_to_obj(trophy, af);

            af.location = APPLY_HITROLL;
            af.modifier = ch.level / 5;
            af.bitvector = 0;
            affect_to_obj(trophy, af);

            af.location = APPLY_INT;
            af.modifier = level > 20 ? -2 : -1;
            affect_to_obj(trophy, af);

            af.location = APPLY_STR;
            af.modifier = level > 20 ? 2 : 1;
            affect_to_obj(trophy, af);

            trophy.value[0] = ch.level;
            trophy.value[1] = ch.level;
            trophy.value[2] = ch.level;
            trophy.value[3] = ch.level;


            obj_to_char(trophy, ch);
            check_improve(ch, gsn_trophy, true, 1);

            act("You make a poncho from $p!", ch, part, null, TO_CHAR);
            act("$n makes a poncho from $p!", ch, part, null, TO_ROOM);

            extract_obj(part);
        } else {
            send_to_char("You destroyed it.\n", ch);
            extract_obj(part);
            ch.mana -= 15;
            check_improve(ch, gsn_trophy, false, 1);
        }
    }


    static void do_truesight(CHAR_DATA ch) {
        if (skill_failure_check(ch, gsn_truesight, true, 0, null)) {
            return;
        }

        if (is_affected(ch, gsn_truesight)) {
            send_to_char("Your eyes are as sharp as they will get.\n", ch);
            return;
        }

        if (ch.mana < 50) {
            send_to_char("You cannot seem to focus enough.\n", ch);
            return;
        }

        WAIT_STATE(ch, gsn_truesight.beats);

        if (!IS_NPC(ch) && number_percent() < ch.pcdata.learned[gsn_truesight.ordinal()]) {
            var af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_truesight;
            af.level = ch.level;
            af.duration = ch.level / 2 + 5;
            af.location = APPLY_NONE;
            af.modifier = 0;
            af.bitvector = AFF_DETECT_HIDDEN;
            affect_to_char(ch, af);

            af.bitvector = AFF_DETECT_INVIS;
            affect_to_char(ch, af);

            af.bitvector = AFF_DETECT_IMP_INVIS;
            affect_to_char(ch, af);

            af.bitvector = AFF_ACUTE_VISION;
            affect_to_char(ch, af);

            af.bitvector = AFF_DETECT_MAGIC;
            affect_to_char(ch, af);

            ch.mana -= 50;

            act("You look around sharply!", ch, null, null, TO_CHAR);
            act("$n looks more enlightened.", ch, null, null, TO_ROOM);
            check_improve(ch, gsn_truesight, true, 1);
        } else {
            ch.mana -= 25;

            send_to_char("You look about sharply, but you don't see anything new.\n"
                    , ch);
            act("$n looks around sharply but doesn't seem enlightened.",
                    ch, null, null, TO_ROOM);
            check_improve(ch, gsn_truesight, false, 1);
        }

    }

    static void do_warcry(CHAR_DATA ch) {

        if (skill_failure_check(ch, gsn_warcry, true, 0, null)) {
            return;
        }

        if (is_affected(ch, gsn_bless) || is_affected(ch, gsn_warcry)) {
            send_to_char("You are already blessed.\n", ch);
            return;
        }

        if (ch.mana < 30) {
            send_to_char("You can't concentrate enough right now.\n", ch);
            return;
        }

        WAIT_STATE(ch, gsn_warcry.beats);

        if (number_percent() > ch.pcdata.learned[gsn_warcry.ordinal()]) {
            send_to_char("You grunt softly.\n", ch);
            act("$n makes some soft grunting noises.", ch, null, null, TO_ROOM);
            return;
        }

        ch.mana -= 30;
        var af = new AFFECT_DATA();

        af.where = TO_AFFECTS;
        af.type = gsn_warcry;
        af.level = ch.level;
        af.duration = 6 + ch.level;
        af.location = APPLY_HITROLL;
        af.modifier = ch.level / 8;
        af.bitvector = 0;
        affect_to_char(ch, af);

        af.location = APPLY_SAVING_SPELL;
        af.modifier = -ch.level / 8;
        affect_to_char(ch, af);
        send_to_char("You feel righteous as you yell out your warcry.\n", ch);
    }

    static void do_guard(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        var arg = new StringBuilder();
        one_argument(argument, arg);

        if (skill_failure_check(ch, gsn_guard, true, 0, null)) {
            return;
        }

        if (arg.isEmpty()) {
            send_to_char("Guard whom?\n", ch);
            return;
        }

        if ((victim = get_char_room(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (IS_NPC(victim)) {
            act("$N doesn't need any of your help!", ch, null, victim, TO_CHAR);
            return;
        }

        if (!str_cmp(arg.toString(), "none") || !str_cmp(arg.toString(), "self") || victim == ch) {
            if (ch.guarding == null) {
                send_to_char("You can't guard yourself!\n", ch);
                return;
            } else {
                act("You stop guarding $N.", ch, null, ch.guarding, TO_CHAR);
                act("$n stops guarding you.", ch, null, ch.guarding, TO_VICT);
                act("$n stops guarding $N.", ch, null, ch.guarding, TO_NOTVICT);
                ch.guarding.guarded_by = null;
                ch.guarding = null;
                return;
            }
        }

        if (ch.guarding == victim) {
            act("You're already guarding $N!", ch, null, victim, TO_CHAR);
            return;
        }

        if (ch.guarding != null) {
            send_to_char("But you're already guarding someone else!\n", ch);
            return;
        }

        if (victim.guarded_by != null) {
            act("$N is already being guarded by someone.", ch, null, victim, TO_CHAR);
            return;
        }

        if (victim.guarding == ch) {
            act("But $N is guarding you!", ch, null, victim, TO_CHAR);
            return;
        }

        if (!is_same_group(victim, ch)) {
            act("But you aren't in the same group as $N.", ch, null, victim, TO_CHAR);
            return;
        }

        if (IS_AFFECTED(ch, AFF_CHARM)) {
            act("You like your master too much to bother guarding $N!",
                    ch, null, victim, TO_VICT);
            return;
        }

        if (victim.fighting != null) {
            send_to_char("Why don't you let them stop fighting first?\n", ch);
            return;
        }

        if (ch.fighting != null) {
            send_to_char
                    ("Better finish your own battle before you worry about guarding someone else.\n",
                            ch);
            return;
        }

        act("You are now guarding $N.", ch, null, victim, TO_CHAR);
        act("You are being guarded by $n.", ch, null, victim, TO_VICT);
        act("$n is now guarding $N.", ch, null, victim, TO_NOTVICT);

        ch.guarding = victim;
        victim.guarded_by = ch;

    }

    static CHAR_DATA check_guard(CHAR_DATA ch, CHAR_DATA mob) {
        int chance;

        if (ch.guarded_by == null ||
                get_char_room(ch, ch.guarded_by.name) == null) {
            return ch;
        } else {
            chance = (int) (get_skill(ch.guarded_by, gsn_guard) - (1.5 * (ch.level - mob.level)));
            if (number_percent() < UMIN(100, chance)) {
                act("$n jumps in front of $N!", ch.guarded_by, null, ch, TO_NOTVICT);
                act("$n jumps in front of you!", ch.guarded_by, null, ch, TO_VICT);
                act("You jump in front of $N!", ch.guarded_by, null, ch, TO_CHAR);
                check_improve(ch.guarded_by, gsn_guard, true, 3);
                return ch.guarded_by;
            } else {
                check_improve(ch.guarded_by, gsn_guard, false, 3);
                return ch;
            }

        }
    }


    static void do_explode(CHAR_DATA ch, String argument) {
        var victim = ch.fighting;
        int dam = 0, hp_dam, dice_dam, mana;
        int hpch, level = ch.level;

        if (skill_failure_check(ch, gsn_explode, false, 0, "Flame? What is that?\n")) {
            return;
        }

        if (victim == null) {
            var arg = new StringBuilder();
            one_argument(argument, arg);
            if (arg.isEmpty()) {
                send_to_char("You play with the exploding material.\n", ch);
                return;
            }
            if ((victim = get_char_room(ch, arg.toString())) == null) {
                send_to_char("They aren't here.\n", ch);
                return;
            }
        }

        mana = gsn_explode.min_mana;

        if (ch.mana < mana) {
            send_to_char("You can't find that much energy to fire\n", ch);
            return;
        }
        ch.mana -= mana;

        act("$n burns something.", ch, null, victim, TO_NOTVICT);
        act("$n burns a cone of exploding material over you!", ch, null, victim, TO_VICT);
        act("Burn them all!.", ch, null, null, TO_CHAR);

        WAIT_STATE(ch, gsn_explode.beats);

        if (!IS_NPC(ch) && number_percent() >= ch.pcdata.learned[gsn_explode.ordinal()]) {
            damage(ch, victim, 0, gsn_explode, DAM_FIRE, true);
            check_improve(ch, gsn_explode, false, 1);
            return;
        }

        hpch = UMAX(10, ch.hit);
        hp_dam = number_range(hpch / 9 + 1, hpch / 5);
        dice_dam = dice(level, 20);

        if (!(is_safe(ch, victim))) {
            dam = UMAX(hp_dam + dice_dam / 10, dice_dam + hp_dam / 10);
            fire_effect(victim.in_room, level, dam / 2, TARGET_ROOM);
        }
        for (CHAR_DATA vch = victim.in_room.people, vch_next; vch != null; vch = vch_next) {
            vch_next = vch.next_in_room;

            if (is_safe_spell(ch, vch, true)
                    || (IS_NPC(vch) && IS_NPC(ch)
                    && (ch.fighting != vch || vch.fighting != ch))) {
                continue;
            }
            if (is_safe(ch, vch)) {
                continue;
            }

            if (vch == victim) /* full damage */ {
                fire_effect(vch, level, dam, TARGET_CHAR);
                damage(ch, vch, dam, gsn_explode, DAM_FIRE, true);
            } else /* partial damage */ {
                fire_effect(vch, level / 2, dam / 4, TARGET_CHAR);
                damage(ch, vch, dam / 2, gsn_explode, DAM_FIRE, true);
            }
        }
        if (!IS_NPC(ch) && number_percent() >= ch.pcdata.learned[gsn_explode.ordinal()]) {
            fire_effect(ch, level / 4, dam / 10, TARGET_CHAR);
            damage(ch, ch, (ch.hit / 10), gsn_explode, DAM_FIRE, true);
        }
    }


    static void do_target(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;


        if (skill_failure_check(ch, gsn_target, false, 0,
                "You don't know how to change the target while fighting a group.\n")) {
            return;
        }

        if (ch.fighting == null) {
            send_to_char("You aren't fighting yet.\n", ch);
            return;
        }

        if (argument.isEmpty()) {
            send_to_char("Change target to whom?\n", ch);
            return;
        }

        if ((victim = get_char_room(ch, argument)) == null) {
            send_to_char("You don't see that item.\n", ch);
            return;
        }

        /* check victim is fighting with him */

        if (victim.fighting != ch) {
            send_to_char("Target is not fighting with you.\n", ch);
            return;
        }


        WAIT_STATE(ch, gsn_target.beats);

        if (!IS_NPC(ch) && number_percent() <
                (get_skill(ch, gsn_target) / 2)) {
            check_improve(ch, gsn_target, false, 1);

            ch.fighting = victim;

            act("$n changes $s target to $N!", ch, null, victim, TO_NOTVICT);
            act("You change your target to $N!", ch, null, victim, TO_CHAR);
            act("$n changes target to you!", ch, null, victim, TO_VICT);
            return;
        }

        send_to_char("You tried, but you couldn't. But for honour try again!.\n", ch);
        check_improve(ch, gsn_target, false, 1);

    }


    static void do_tiger(CHAR_DATA ch) {
        int chance, hp_percent;

        if (skill_failure_check(ch, gsn_tiger_power, false, 0, null)) {
            return;
        }

        if ((chance = get_skill(ch, gsn_tiger_power)) == 0) {
            send_to_char("You called fizzled in the space...\n", ch);
            return;
        }

        act("$n calls the power of 10 tigers!.", ch, null, null, TO_ROOM);

        if (IS_AFFECTED(ch, AFF_BERSERK) || is_affected(ch, gsn_berserk) ||
                is_affected(ch, gsn_tiger_power) || is_affected(ch, Skill.gsn_frenzy)) {
            send_to_char("You get a little madder.\n", ch);
            return;
        }

        if (IS_AFFECTED(ch, AFF_CALM)) {
            send_to_char("You're feeling too mellow to call 10 tigers.\n", ch);
            return;
        }
        if (ch.in_room.sector_type != SECT_FIELD &&
                ch.in_room.sector_type != SECT_FOREST &&
                ch.in_room.sector_type != SECT_MOUNTAIN &&
                ch.in_room.sector_type != SECT_HILLS) {
            send_to_char("No tigers can hear your call.\n", ch);
            return;
        }


        if (ch.mana < 50) {
            send_to_char("You can't get up enough energy.\n", ch);
            return;
        }

        /* modifiers */

        /* fighting */
        if (ch.position == POS_FIGHTING) {
            chance += 10;
        }

        hp_percent = 100 * ch.hit / ch.max_hit;
        chance += 25 - hp_percent / 2;

        if (number_percent() < chance) {

            WAIT_STATE(ch, PULSE_VIOLENCE);
            ch.mana -= 50;
            ch.move /= 2;

            /* heal a little damage */
            ch.hit += ch.level * 2;
            ch.hit = UMIN(ch.hit, ch.max_hit);

            send_to_char("10 tigers come for your call, as you call them!\n", ch);
            act("10 tigers come across $n , and connect with $n.", ch, null, null, TO_ROOM);
            check_improve(ch, gsn_tiger_power, true, 2);
            var af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_tiger_power;
            af.level = ch.level;
            af.duration = number_fuzzy(ch.level / 8);
            af.modifier = UMAX(1, ch.level / 5);
            af.bitvector = AFF_BERSERK;

            af.location = APPLY_HITROLL;
            affect_to_char(ch, af);

            af.location = APPLY_DAMROLL;
            affect_to_char(ch, af);

            af.modifier = UMAX(10, 10 * (ch.level / 5));
            af.location = APPLY_AC;
            affect_to_char(ch, af);
        } else {
            WAIT_STATE(ch, 2 * PULSE_VIOLENCE);
            ch.mana -= 25;
            ch.move /= 2;

            send_to_char("Your feel stregthen up, but nothing happens.\n", ch);
            check_improve(ch, gsn_tiger_power, false, 2);
        }
    }

    static void do_hara(CHAR_DATA ch) {
        int chance;

        if (skill_failure_check(ch, gsn_hara_kiri, false, 0, null)) {
            return;
        }

        if ((chance = get_skill(ch, gsn_hara_kiri)) == 0) {
            send_to_char("You try to kill yourself, but you can't resist this ache.\n", ch);
            return;
        }

        /* fighting */
        if (ch.position == POS_FIGHTING) {
            send_to_char("Try your chance during fighting.\n", ch);
            return;
        }

        if (is_affected(ch, gsn_hara_kiri)) {
            send_to_char("One more try will kill you.\n", ch);
            return;
        }

        if (number_percent() < chance) {

            WAIT_STATE(ch, PULSE_VIOLENCE);

            ch.hit = 1;
            ch.mana = 1;
            ch.move = 1;

            if (ch.pcdata.condition[COND_HUNGER] < 40) {
                ch.pcdata.condition[COND_HUNGER] = 40;
            }
            if (ch.pcdata.condition[COND_THIRST] < 40) {
                ch.pcdata.condition[COND_THIRST] = 40;
            }

            send_to_char("Yo cut your finger and wait till all your blood finishes.\n", ch);
            act("{r$n cuts his body and look in a deadly figure.{x", ch, null, null, TO_ROOM, POS_FIGHTING);
            check_improve(ch, gsn_hara_kiri, true, 2);
            do_sleep(ch, "");
            ch.act = SET_BIT(ch.act, PLR_HARA_KIRI);
            var af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_hara_kiri;
            af.level = ch.level;
            af.duration = 10;
            af.location = APPLY_NONE;
            af.modifier = 0;
            af.bitvector = 0;
            affect_to_char(ch, af);
        } else {
            WAIT_STATE(ch, 2 * PULSE_VIOLENCE);
            var af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_hara_kiri;
            af.level = ch.level;
            af.duration = 0;
            af.location = APPLY_NONE;
            af.modifier = 0;
            af.bitvector = 0;
            affect_to_char(ch, af);

            send_to_char("You couldn't cut your finger.It is not so easy as you know.\n", ch);
            check_improve(ch, gsn_hara_kiri, false, 2);
        }
    }

/*
 * ground strike
*/

    static int ground_strike(CHAR_DATA ch, CHAR_DATA victim, int dam) {
        int diceroll;

        if (skill_failure_nomessage(ch, gsn_ground_strike, 0) != 0) {
            return dam;
        }

        if (ch.in_room.sector_type != SECT_HILLS
                && ch.in_room.sector_type != SECT_MOUNTAIN
                && ch.in_room.sector_type != SECT_FOREST
                && ch.in_room.sector_type != SECT_FIELD) {
            return dam;
        }

        diceroll = number_range(0, 100);
        if (victim.level > ch.level) {
            diceroll += (victim.level - ch.level) * 2;
        }
        if (victim.level < ch.level) {
            diceroll -= (ch.level - victim.level);
        }

        if (diceroll <= (get_skill(ch, gsn_ground_strike) / 3)) {
            check_improve(ch, gsn_ground_strike, true, 2);
            dam += dam * diceroll / 200;
        }

        if (diceroll <= (get_skill(ch, gsn_ground_strike) / 15)) {
            diceroll = number_percent();
            if (diceroll < 75) {
                act("{rThe ground underneath your feet starts moving!{x", ch, null, victim, TO_VICT, POS_RESTING);
                act("{rYou take the control of the ground underneath the feet of $N!{x", ch, null, victim, TO_CHAR, POS_RESTING);

                check_improve(ch, gsn_ground_strike, true, 3);
                WAIT_STATE(victim, 2 * PULSE_VIOLENCE);
                dam += (dam * number_range(2, 5)) / 5;
                return dam;
            } else if (diceroll > 75 && diceroll < 95) {
                act("{yYou are blinded by $n's attack!{x", ch, null, victim, TO_VICT, POS_RESTING);
                act("{yYou blind $N with your attack!{x", ch, null, victim, TO_CHAR, POS_RESTING);

                check_improve(ch, gsn_ground_strike, true, 4);
                if (!IS_AFFECTED(victim, AFF_BLIND)) {
                    var baf = new AFFECT_DATA();
                    baf.type = gsn_dirt;
                    baf.level = ch.level;
                    baf.location = APPLY_HITROLL;
                    baf.modifier = -4;
                    baf.duration = number_range(1, 5);
                    baf.bitvector = AFF_BLIND;
                    affect_to_char(victim, baf);
                }
                dam += dam * number_range(1, 2);
                return dam;
            } else if (diceroll > 95) {
                act("{r$C$n cuts out your heart! OUCH!!{x", ch, null, victim, TO_VICT, POS_RESTING);
                act("{rYou cut out $N's heart!  I bet that hurt!{x", ch, null, victim, TO_CHAR, POS_RESTING);

                check_improve(ch, gsn_ground_strike, true, 5);
                dam += dam * number_range(2, 5);
                return dam;
            }
        }

        return dam;
    }

/*
 * critical strike
*/

    static int critical_strike(CHAR_DATA ch, CHAR_DATA victim, int dam) {
        int diceroll;

        if (skill_failure_nomessage(ch, gsn_critical, 0) != 0) {
            return dam;
        }

        if (get_wield_char(ch, false) != null
                && get_wield_char(ch, true) != null
                && number_percent() > ((ch.hit * 100) / ch.max_hit)) {
            return dam;
        }

        diceroll = number_range(0, 100);
        if (victim.level > ch.level) {
            diceroll += (victim.level - ch.level) * 2;
        }
        if (victim.level < ch.level) {
            diceroll -= (ch.level - victim.level);
        }

        if (diceroll <= (get_skill(ch, gsn_critical) / 2)) {
            check_improve(ch, gsn_critical, true, 2);
            dam += dam * diceroll / 200;
        }

        if (diceroll <= (get_skill(ch, gsn_critical) / 13)) {
            diceroll = number_percent();
            if (diceroll < 75) {
                act("{r$n takes you down with a weird judo move!{x", ch, null, victim, TO_VICT, POS_RESTING);
                act("{rYou take $N down with a weird judo move!{x", ch, null, victim, TO_CHAR, POS_RESTING);

                check_improve(ch, gsn_critical, true, 3);
                WAIT_STATE(victim, 2 * PULSE_VIOLENCE);
                dam += (dam * number_range(2, 5)) / 5;
                return dam;
            } else if (diceroll > 75 && diceroll < 95) {
                act("{yYou are blinded by $n's attack!{x", ch, null, victim, TO_VICT, POS_RESTING);
                act("{yYou blind $N with your attack!{x", ch, null, victim, TO_CHAR, POS_RESTING);

                check_improve(ch, gsn_critical, true, 4);
                if (!IS_AFFECTED(victim, AFF_BLIND)) {
                    var baf = new AFFECT_DATA();
                    baf.type = gsn_dirt;
                    baf.level = ch.level;
                    baf.location = APPLY_HITROLL;
                    baf.modifier = -4;
                    baf.duration = number_range(1, 5);
                    baf.bitvector = AFF_BLIND;
                    affect_to_char(victim, baf);
                }
                dam += dam * number_range(1, 2);
                return dam;
            } else if (diceroll > 95) {
                act("{r$n cuts out your heart! OUCH!!{x", ch, null, victim, TO_VICT, POS_RESTING);
                act("{rYou cut out $N's heart!  I bet that hurt!{x", ch, null, victim, TO_CHAR, POS_RESTING);

                check_improve(ch, gsn_critical, true, 5);
                dam += dam * number_range(2, 5);
                return dam;
            }
        }

        return dam;
    }

    static void do_shield(CHAR_DATA ch) {
        CHAR_DATA victim;
        int chance, ch_weapon, vict_shield;
        OBJ_DATA shield, axe;

        if (skill_failure_check(ch, gsn_shield_cleave, false, 0,
                "You don't know how to cleave opponents's shield.\n")) {
            return;
        }

        chance = get_skill(ch, gsn_shield_cleave);

        if ((victim = ch.fighting) == null) {
            send_to_char("You aren't fighting anyone.\n", ch);
            return;
        }

        if ((axe = get_wield_char(ch, false)) == null) {
            send_to_char("You must be wielding a weapon.\n", ch);
            return;
        }

        if ((shield = get_shield_char(victim)) == null) {
            send_to_char("Your opponent must wield a shield.\n", ch);
            return;
        }

        if (axe.value[0] == WEAPON_AXE) {
            chance *= 1.2;
        } else if (axe.value[0] != WEAPON_SWORD) {
            send_to_char("Your weapon must be an axe or a sword.\n", ch);
            return;
        }

        /* find weapon skills */
        ch_weapon = get_weapon_skill(ch, get_weapon_sn(ch, false));
        vict_shield = get_skill(ch, gsn_shield_block);
        /* modifiers */

        /* skill */
        chance = chance * ch_weapon / 200;
        chance = chance * 100 / vict_shield;

        /* dex vs. strength */
        chance += get_curr_stat(ch, STAT_DEX);
        chance -= 2 * get_curr_stat(victim, STAT_STR);

        /* level */
/*    chance += (ch.level - victim.level) * 2; */
        chance += ch.level - victim.level;
        chance += axe.level - shield.level;

        /* cleave proofness */
        if (check_material(shield, "platinum") || shield.pIndexData.limit != -1) {
            chance = -1;
        }

        /* and now the attack */
        ch.affected_by = SET_BIT(ch.affected_by, AFF_WEAK_STUN);

        if (number_percent() < chance) {
            WAIT_STATE(ch, gsn_shield_cleave.beats);
            act("You cleaved $N's shield into two.", ch, null, victim, TO_CHAR);
            act("$n cleaved your shield into two.", ch, null, victim, TO_VICT);
            act("$n cleaved $N's shield into two.", ch, null, victim, TO_NOTVICT);
            check_improve(ch, gsn_shield_cleave, true, 1);
            extract_obj(get_shield_char(victim));
        } else {
            WAIT_STATE(ch, gsn_shield_cleave.beats);
            act("You fail to cleave $N's shield.", ch, null, victim, TO_CHAR);
            act("$n tries to cleave your shield, but fails.", ch, null, victim, TO_VICT);
            act("$n tries to cleave $N's shield, but fails.", ch, null, victim, TO_NOTVICT);
            check_improve(ch, gsn_shield_cleave, false, 1);
        }
    }

    static void do_weapon(CHAR_DATA ch) {
        CHAR_DATA victim;
        OBJ_DATA wield, axe;
        int chance, ch_weapon, vict_weapon;

        if (skill_failure_check(ch, gsn_shield_cleave, false, 0,
                "You don't know how to cleave opponents's weapon.\n")) {
            return;
        }

        chance = get_skill(ch, gsn_weapon_cleave);

        if ((victim = ch.fighting) == null) {
            send_to_char("You aren't fighting anyone.\n", ch);
            return;
        }

        if ((axe = get_wield_char(ch, false)) == null) {
            send_to_char("You must be wielding a weapon.\n", ch);
            return;
        }

        if ((wield = get_wield_char(victim, false)) == null) {
            send_to_char("Your opponent must wield a weapon.\n", ch);
            return;
        }

        if (axe.value[0] == WEAPON_AXE) {
            chance *= 1.2;
        } else if (axe.value[0] != WEAPON_SWORD) {
            send_to_char("Your weapon must be an axe or a sword.\n", ch);
            return;
        }

        /* find weapon skills */
        ch_weapon = get_weapon_skill(ch, get_weapon_sn(ch, false));
        vict_weapon = get_weapon_skill(victim, get_weapon_sn(victim, false));
        /* modifiers */

        /* skill */
        chance = chance * ch_weapon / 200;
        chance = chance * 100 / vict_weapon;

        /* dex vs. strength */
        chance += get_curr_stat(ch, STAT_DEX) + get_curr_stat(ch, STAT_STR);
        chance -= get_curr_stat(victim, STAT_STR) +
                2 * get_curr_stat(victim, STAT_DEX);

        chance += ch.level - victim.level;
        chance += axe.level - wield.level;

        if (check_material(wield, "platinum") || wield.pIndexData.limit != -1) {
            chance = -1;
        }

        /* and now the attack */
        ch.affected_by = SET_BIT(ch.affected_by, AFF_WEAK_STUN);

        if (number_percent() < chance) {
            WAIT_STATE(ch, gsn_weapon_cleave.beats);
            act("You cleaved $N's weapon into two.", ch, null, victim, TO_CHAR);
            act("$n cleaved your weapon into two.", ch, null, victim, TO_VICT);
            act("$n cleaved $N's weapon into two.", ch, null, victim, TO_NOTVICT);
            check_improve(ch, gsn_weapon_cleave, true, 1);
            extract_obj(get_wield_char(victim, false));
        } else {
            WAIT_STATE(ch, gsn_weapon_cleave.beats);
            act("You fail to cleave $N's weapon.", ch, null, victim, TO_CHAR);
            act("$n tries to cleave your weapon, but fails.", ch, null, victim, TO_VICT);
            act("$n tries to cleave $N's weapon, but fails.", ch, null, victim, TO_NOTVICT);
            check_improve(ch, gsn_weapon_cleave, false, 1);
        }
    }


    static void do_tail(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        int chance, wait;
        int damage_tail;

        if (skill_failure_check(ch, gsn_tail, false, OFF_TAIL, null)) {
            return;
        }

        var FightingCheck = ch.fighting != null;

        var arg = new StringBuilder();
        one_argument(argument, arg);

        if ((chance = get_skill(ch, gsn_tail)) == 0) {
            send_to_char("You waived your tail aimlessly.\n", ch);
            return;
        }

        if (arg.isEmpty()) {
            victim = ch.fighting;
            if (victim == null) {
                send_to_char("But you aren't fighting anyone!\n", ch);
                return;
            }
        } else if ((victim = get_char_room(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }
/*
    if (victim.position < POS_FIGHTING)
    {
    act("You'll have to let $M get back up first.",ch,null,victim,TO_CHAR);
    return;
    }
*/
        if (victim == ch) {
            send_to_char("You try to hit yourself by your tail, but failed.\n", ch);
            return;
        }

        if (is_safe(ch, victim)) {
            return;
        }

        if (IS_AFFECTED(ch, AFF_CHARM) && ch.master == victim) {
            act("But $N is your friend!", ch, null, victim, TO_CHAR);
            return;
        }

        if (is_affected(victim, gsn_protective_shield)) {
            act("{YYour tail seems to slide around $N.{x", ch, null, victim, TO_CHAR, POS_FIGHTING);
            act("{Y$n's tail slides off your protective shield.{x", ch, null, victim, TO_VICT, POS_FIGHTING);
            act("{Y$n's tail seems to slide around $N.{x", ch, null, victim, TO_NOTVICT, POS_FIGHTING);
            return;
        }

        /* modifiers */

        /* size  and weight */
        chance -= ch.carry_weight / 20;
        chance += victim.carry_weight / 25;

        if (ch.size < victim.size) {
            chance += (ch.size - victim.size) * 25;
        } else {
            chance += (ch.size - victim.size) * 10;
        }

        /* stats */
        chance += get_curr_stat(ch, STAT_STR) + get_curr_stat(ch, STAT_DEX);
        chance -= get_curr_stat(victim, STAT_DEX) * 2;

        if (IS_AFFECTED(ch, AFF_FLYING)) {
            chance -= 10;
        }

        /* speed */
        if (IS_SET(ch.off_flags, OFF_FAST)) {
            chance += 20;
        }
        if (IS_SET(victim.off_flags, OFF_FAST)) {
            chance -= 30;
        }

        /* level */
        chance += (ch.level - victim.level) * 2;

        /* now the attack */
        if (number_percent() < (chance / 4)) {

            act("$n sends you sprawling with a powerful tail!",
                    ch, null, victim, TO_VICT);
            act("You sprawle $N with your tail , and send $M flying!",
                    ch, null, victim, TO_CHAR);
            act("$n sends $N sprawling with a powerful tail.",
                    ch, null, victim, TO_NOTVICT);
            check_improve(ch, gsn_tail, true, 1);

            wait = 3;

            wait = switch (number_bits(2)) {
                case 0 -> 1;
                case 1 -> 2;
                case 2 -> 4;
                case 3 -> 3;
                default -> wait;
            };

            WAIT_STATE(victim, wait * PULSE_VIOLENCE);
            WAIT_STATE(ch, gsn_tail.beats);
            victim.position = POS_RESTING;
            damage_tail = ch.damroll +
                    (2 * number_range(4, 4 + 10 * ch.size + chance / 10));
            damage(ch, victim, damage_tail, gsn_tail, DAM_BASH, true);

        } else {
            damage(ch, victim, 0, gsn_tail, DAM_BASH, true);
            act("You lost your position and fall down!",
                    ch, null, victim, TO_CHAR);
            act("$n lost $s position and fall down!.",
                    ch, null, victim, TO_NOTVICT);
            act("You evade $n's tail, causing $m to fall down.",
                    ch, null, victim, TO_VICT);
            check_improve(ch, gsn_tail, false, 1);
            ch.position = POS_RESTING;
            WAIT_STATE(ch, gsn_tail.beats * 3 / 2);
        }
        if (!(IS_NPC(victim)) && !(IS_NPC(ch)) && victim.position > POS_STUNNED
                && !FightingCheck) {
            if (!can_see(victim, ch)) {
                do_yell(victim, "Help! Someone hit me!");
            } else {
                var buf = new TextBuffer();
                buf.sprintf("Help! %s try to hit me with its tail!",
                        (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(victim)) ? ch.doppel.name : ch.name);
                do_yell(victim, buf.toString());
            }
        }
    }

    static void do_concentrate(CHAR_DATA ch) {
        int chance;

        if (skill_failure_check(ch, gsn_concentrate, false, 0,
                "You try to concentrate on what is going on.\n")) {
            return;
        }

        if ((chance = get_skill(ch, gsn_concentrate)) == 0) {
            send_to_char("You try to concentrate on what is going on.\n", ch);
            return;
        }

        if (is_affected(ch, gsn_concentrate)) {
            send_to_char("You are already concentrated for the fight.\n", ch);
            return;
        }

        if (ch.mana < 50) {
            send_to_char("You can't get up enough energy.\n", ch);
            return;
        }

        /* fighting */
        if (ch.fighting != null) {
            send_to_char("Concentrate on your fighting!\n", ch);
            return;
        }


        if (number_percent() < chance) {

            WAIT_STATE(ch, 2 * PULSE_VIOLENCE);
            ch.mana -= 50;
            ch.move /= 2;

            do_sit(ch, "");
            send_to_char("You sit down and relax , concentrate on the next fight.!\n", ch);
            act("{r$n concentrates for the next fight.{x", ch, null, null, TO_ROOM, POS_FIGHTING);
            check_improve(ch, gsn_concentrate, true, 2);
            var af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_concentrate;
            af.level = ch.level;
            af.duration = number_fuzzy(ch.level / 8);
            af.modifier = UMAX(1, ch.level / 8);
            af.bitvector = 0;

            af.location = APPLY_HITROLL;
            affect_to_char(ch, af);

            af.location = APPLY_DAMROLL;
            affect_to_char(ch, af);

            af.modifier = UMAX(1, ch.level / 10);
            af.location = APPLY_AC;
            affect_to_char(ch, af);
        } else {
            send_to_char("You try to concentrate for the next fight but fail.\n", ch);
            check_improve(ch, gsn_concentrate, false, 2);
        }
    }


    static void do_bandage(CHAR_DATA ch) {
        int heal;

        if (skill_failure_check(ch, gsn_bandage, false, 0, null)) {
            return;
        }

        if (is_affected(ch, gsn_bandage)) {
            send_to_char("You have already using your bandage.\n", ch);
            return;
        }


        if (IS_NPC(ch) || number_percent() < get_skill(ch, gsn_bandage)) {

            WAIT_STATE(ch, PULSE_VIOLENCE);


            send_to_char("You place your bandage to your shoulder!\n", ch);
            act("$n places a bandage to $s shulder.", ch, null, null, TO_ROOM);
            check_improve(ch, gsn_bandage, true, 2);

            heal = dice(4, 8) + ch.level / 2;
            ch.hit = UMIN(ch.hit + heal, ch.max_hit);
            update_pos(ch);
            send_to_char("You feel better!\n", ch);

            var af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_bandage;
            af.level = ch.level;
            af.duration = ch.level / 10;
            af.modifier = UMIN(15, ch.level / 2);
            af.bitvector = AFF_REGENERATION;
            af.location = 0;
            affect_to_char(ch, af);

        } else {
            WAIT_STATE(ch, PULSE_VIOLENCE);

            send_to_char("You failed to place your bandage to your shoulder.\n", ch);
            check_improve(ch, gsn_bandage, false, 2);
        }
    }


    static void do_katana(CHAR_DATA ch, String argument) {
        OBJ_DATA katana;
        OBJ_DATA part;
        var arg = new StringBuilder();
        one_argument(argument, arg);

        if (skill_failure_check(ch, gsn_katana, false, 0, null)) {
            return;
        }

        if (is_affected(ch, gsn_katana)) {
            send_to_char("But you've already got one katana!\n", ch);
            return;
        }

        if (ch.mana < 300) {
            send_to_char("You feel too weak to concentrate on a katana.\n", ch);
            return;
        }

        if (arg.isEmpty()) {
            send_to_char("Make a katana from what?\n", ch);
            return;
        }

        if ((part = get_obj_carry(ch, arg.toString())) == null) {
            send_to_char("You do not have chunk of iron.\n", ch);
            return;
        }

        if (part.pIndexData.vnum != OBJ_VNUM_CHUNK_IRON) {
            send_to_char("You do not have the right material.\n", ch);
            return;
        }

        if (number_percent() < (get_skill(ch, gsn_katana) / 3) * 2) {
            send_to_char("You failed and destroyed it.\n", ch);
            extract_obj(part);
            return;
        }

        WAIT_STATE(ch, gsn_katana.beats);

        if (!IS_NPC(ch) && number_percent() < get_skill(ch, gsn_katana)) {
            var af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_katana;
            af.level = ch.level;
            af.duration = ch.level;
            af.modifier = 0;
            af.bitvector = 0;
            af.location = 0;
            affect_to_char(ch, af);

            katana = create_object(get_obj_index(OBJ_VNUM_KATANA_SWORD), ch.level);
            katana.cost = 0;
            katana.level = ch.level;
            ch.mana -= 300;

            af.where = TO_OBJECT;
            af.type = gsn_katana;
            af.level = ch.level;
            af.duration = -1;
            af.location = APPLY_DAMROLL;
            af.modifier = ch.level / 10;
            af.bitvector = 0;
            affect_to_obj(katana, af);

            af.location = APPLY_HITROLL;
            affect_to_obj(katana, af);

            katana.value[2] = ch.level / 10;
            var buf = new TextBuffer();
            buf.sprintf(katana.pIndexData.extra_descr.description, ch.name);
            katana.extra_descr = new EXTRA_DESCR_DATA();
            katana.extra_descr.keyword = katana.pIndexData.extra_descr.keyword;
            katana.extra_descr.description = buf.toString();
            katana.extra_descr.next = null;

            obj_to_char(katana, ch);
            check_improve(ch, gsn_katana, true, 1);

            act("You make a katana from $p!", ch, part, null, TO_CHAR);
            act("$n makes a katana from $p!", ch, part, null, TO_ROOM);

            extract_obj(part);
        } else {
            send_to_char("You destroyed it.\n", ch);
            extract_obj(part);
            ch.mana -= 150;
            check_improve(ch, gsn_katana, false, 1);
        }
    }


    static void do_crush(CHAR_DATA ch) {
        CHAR_DATA victim;
        int chance, wait;
        int damage_crush;

        if (skill_failure_check(ch, gsn_crush, false, OFF_CRUSH, null)) {
            return;
        }

        chance = get_skill(ch, gsn_crush);

        if ((victim = ch.fighting) == null) {
            send_to_char("You are not fighting anyone.\n", ch);
            return;
        }

        if (victim.position < POS_FIGHTING) {
            return;
        }

        if (is_safe(ch, victim)) {
            return;
        }

        if (IS_AFFECTED(ch, AFF_CHARM) && ch.master == victim) {
            return;
        }

        if (is_affected(victim, gsn_protective_shield)) {
            act("{YYour crush seems to slide around $N.{x", ch, null, victim, TO_CHAR, POS_FIGHTING);
            act("{Y$n's crush slides off your protective shield.{x", ch, null, victim, TO_VICT, POS_FIGHTING);
            act("{Y$n's crush seems to slide around $N.{x", ch, null, victim, TO_NOTVICT, POS_FIGHTING);
            return;
        }

        /* modifiers */

        /* size  and weight */
        chance += ch.carry_weight / 25;
        chance -= victim.carry_weight / 20;

        if (ch.size < victim.size) {
            chance += (ch.size - victim.size) * 25;
        } else {
            chance += (ch.size - victim.size) * 10;
        }

        /* stats */
        chance += get_curr_stat(ch, STAT_STR);
        chance -= get_curr_stat(victim, STAT_DEX) * 4 / 3;

        if (IS_AFFECTED(ch, AFF_FLYING)) {
            chance -= 10;
        }

        /* speed */
        if (IS_SET(ch.off_flags, OFF_FAST)) {
            chance += 10;
        }
        if (IS_SET(victim.off_flags, OFF_FAST)) {
            chance -= 20;
        }

        /* level */
        chance += (ch.level - victim.level) * 2;

        /* now the attack */
        if (number_percent() < chance) {

            act("$n squezes you with a powerful crush!",
                    ch, null, victim, TO_VICT);
            act("You slam into $N, and crushes $M!", ch, null, victim, TO_CHAR);
            act("$n squezes $N with a powerful crush.",
                    ch, null, victim, TO_NOTVICT);

            wait = 3;

            wait = switch (number_bits(2)) {
                case 0 -> 1;
                case 1 -> 2;
                case 2 -> 4;
                case 3 -> 3;
                default -> wait;
            };

            WAIT_STATE(victim, wait * PULSE_VIOLENCE);
            WAIT_STATE(ch, gsn_crush.beats);
            victim.position = POS_RESTING;
            damage_crush = ch.damroll + number_range(4, 4 + 4 * ch.size + chance / 2);
            if (ch.level < 5) {
                damage_crush = UMIN(ch.level, damage_crush);
            }
            damage(ch, victim, damage_crush, gsn_crush, DAM_BASH, true);

        } else {
            damage(ch, victim, 0, gsn_crush, DAM_BASH, true);
            act("You fall flat on your face!",
                    ch, null, victim, TO_CHAR);
            act("$n falls flat on $s face.",
                    ch, null, victim, TO_NOTVICT);
            act("You evade $n's crush, causing $m to fall flat on $s face.",
                    ch, null, victim, TO_VICT);
            ch.position = POS_RESTING;
            WAIT_STATE(ch, gsn_crush.beats * 3 / 2);
        }

    }


    static void do_sense(CHAR_DATA ch) {
        if (skill_failure_check(ch, gsn_sense_life, true, 0, null)) {
            return;
        }

        if (is_affected(ch, gsn_sense_life)) {
            send_to_char("You can already feel life forms.\n", ch);
            return;
        }

        if (ch.mana < 20) {
            send_to_char("You cannot seem to concentrate enough.\n", ch);
            return;
        }

        WAIT_STATE(ch, gsn_sense_life.beats);

        if (!IS_NPC(ch) && number_percent() < ch.pcdata.learned[gsn_sense_life.ordinal()]) {
            var af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_sense_life;
            af.level = ch.level;
            af.duration = ch.level;
            af.location = APPLY_NONE;
            af.modifier = 0;
            af.bitvector = AFF_DETECT_LIFE;
            affect_to_char(ch, af);

            ch.mana -= 20;

            act("You start to sense life forms in the room!", ch, null, null, TO_CHAR);
            act("$n looks more sensitive.", ch, null, null, TO_ROOM);
            check_improve(ch, gsn_sense_life, true, 1);
        } else {
            ch.mana -= 10;

            send_to_char("You failed.\n", ch);
            check_improve(ch, gsn_sense_life, false, 1);
        }

    }


    static void do_poison_smoke(CHAR_DATA ch) {
        CHAR_DATA tmp_vict;

        if (skill_failure_check(ch, gsn_poison_smoke, true, 0, null)) {
            return;
        }

        if (ch.mana < gsn_poison_smoke.min_mana) {
            send_to_char("You can't get up enough energy.\n", ch);
            return;
        }
        ch.mana -= gsn_poison_smoke.min_mana;
        WAIT_STATE(ch, gsn_poison_smoke.beats);

        if (number_percent() > get_skill(ch, gsn_poison_smoke)) {
            send_to_char("You failed.\n", ch);
            check_improve(ch, gsn_poison_smoke, false, 1);
            return;
        }

        send_to_char("A cloud of poison smoke fills the room.\n", ch);
        act("A cloud of poison smoke fills the room.", ch, null, null, TO_ROOM);

        check_improve(ch, gsn_poison_smoke, true, 1);
        var buf = new TextBuffer();
        for (tmp_vict = ch.in_room.people; tmp_vict != null;
             tmp_vict = tmp_vict.next_in_room) {
            if (!is_safe_spell(ch, tmp_vict, true)) {
                if (!IS_NPC(ch) && tmp_vict != ch &&
                        ch.fighting != tmp_vict && tmp_vict.fighting != ch &&
                        (IS_SET(tmp_vict.affected_by, AFF_CHARM) || !IS_NPC(tmp_vict))) {
                    if (!can_see(tmp_vict, ch)) {
                        do_yell(tmp_vict, "Help someone is attacking me!");
                    } else {

                        buf.sprintf("Die, %s, you sorcerous dog!",
                                (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(tmp_vict)) ?
                                        ch.doppel.name : ch.name);
                        do_yell(tmp_vict, buf.toString());
                    }
                }

                spell_poison(gsn_poison, ch.level, ch, tmp_vict, TARGET_CHAR);
                if (tmp_vict != ch) {
                    multi_hit(tmp_vict, ch, null);
                }

            }
        }

    }

    static void do_blindness_dust(CHAR_DATA ch) {
        CHAR_DATA tmp_vict;

        if (skill_failure_check(ch, gsn_blindness_dust, true, 0, null)) {
            return;
        }

        if (ch.mana < gsn_blindness_dust.min_mana) {
            send_to_char("You can't get up enough energy.\n", ch);
            return;
        }

        ch.mana -= gsn_blindness_dust.min_mana;
        WAIT_STATE(ch, gsn_blindness_dust.beats);

        if (number_percent() > get_skill(ch, gsn_blindness_dust)) {
            send_to_char("You failed.\n", ch);
            check_improve(ch, gsn_blindness_dust, false, 1);
            return;
        }

        send_to_char("A cloud of dust fills in the room.\n", ch);
        act("A cloud of dust fills the room.", ch, null, null, TO_ROOM);

        check_improve(ch, gsn_blindness_dust, true, 1);
        var buf = new TextBuffer();
        for (tmp_vict = ch.in_room.people; tmp_vict != null;
             tmp_vict = tmp_vict.next_in_room) {
            if (!is_safe_spell(ch, tmp_vict, true)) {
                if (!IS_NPC(ch) && tmp_vict != ch &&
                        ch.fighting != tmp_vict && tmp_vict.fighting != ch &&
                        (IS_SET(tmp_vict.affected_by, AFF_CHARM) || !IS_NPC(tmp_vict))) {
                    if (!can_see(tmp_vict, ch)) {
                        do_yell(tmp_vict, "Help someone is attacking me!");
                    } else {
                        buf.sprintf("Die, %s, you sorcerous dog!",
                                (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(tmp_vict)) ? ch.doppel.name : ch.name);
                        do_yell(tmp_vict, buf.toString());
                    }
                }

                spell_blindness(gsn_blindness, ch.level, ch, tmp_vict);
                if (tmp_vict != ch) {
                    multi_hit(tmp_vict, ch, null);
                }
            }
        }

    }


    static void do_lash(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        int chance;
        int damage_lash;

        if (skill_failure_check(ch, gsn_lash, true, 0, null)) {
            return;
        }

        var FightingCheck = ch.fighting != null;
        var arg = new StringBuilder();
        one_argument(argument, arg);
        chance = get_skill(ch, gsn_lash);

        if (arg.isEmpty()) {
            victim = ch.fighting;
            if (victim == null) {
                send_to_char("But you aren't fighting anyone!\n", ch);
                return;
            }
        } else if ((victim = get_char_room(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (get_weapon_char(ch, WEAPON_WHIP) == null) {
            send_to_char("You need a flail to lash.\n", ch);
            return;
        }

        if (victim == ch) {
            send_to_char("Are you that much stupid to lasy your body!\n", ch);
            return;
        }

        if (is_safe(ch, victim)) {
            return;
        }

        if (IS_AFFECTED(ch, AFF_CHARM) && ch.master == victim) {
            act("But $N is your friend!", ch, null, victim, TO_CHAR);
            return;
        }

        /* modifiers */

        /* stats */
        chance += get_curr_stat(ch, STAT_STR) / 2;
        chance -= get_curr_stat(victim, STAT_DEX) * 2;

        if (IS_AFFECTED(ch, AFF_FLYING)) {
            chance += 20;
        }

        /* speed */
        if (IS_AFFECTED(ch, AFF_HASTE)) {
            chance += 20;
        }
        if (IS_AFFECTED(victim, AFF_HASTE)) {
            chance -= 20;
        }

        if (IS_AFFECTED(ch, AFF_SLOW)) {
            chance -= 40;
        }
        if (IS_AFFECTED(victim, AFF_SLOW)) {
            chance += 20;
        }

        if (MOUNTED(ch) != null) {
            chance -= 20;
        }
        if (MOUNTED(victim) != null) {
            chance += 40;
        }

        /* level */
        chance += (ch.level - victim.level) * 2;

        /* now the attack */
        if (number_percent() < chance) {
            check_improve(ch, gsn_lash, true, 1);

            WAIT_STATE(ch, PULSE_VIOLENCE);
            WAIT_STATE(victim, gsn_lash.beats);
            damage_lash = number_range(4, 4 + chance / 10);
            damage(ch, victim, damage_lash, gsn_lash, DAM_BASH, true);
        } else {
            damage(ch, victim, 0, gsn_lash, DAM_BASH, true);
            act("You failed to lash $N!", ch, null, victim, TO_CHAR);
            act("$n tried to lash $N, but failed.", ch, null, victim, TO_NOTVICT);
            act("You escaped from $n's lash!", ch, null, victim, TO_VICT);
            check_improve(ch, gsn_lash, false, 1);
            WAIT_STATE(ch, PULSE_VIOLENCE);
        }

        if (!(IS_NPC(victim)) && !(IS_NPC(ch)) && victim.position > POS_STUNNED
                && !FightingCheck) {
            if (!can_see(victim, ch)) {
                do_yell(victim, "Help! Someone is lashing me!");
            } else {
                var buf = new TextBuffer();
                buf.sprintf("Help! %s is lashing me!",
                        (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(victim)) ? ch.doppel.name : ch.name);
                do_yell(victim, buf.toString());
            }
        }
    }


    static void do_claw(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        int chance;
        int damage_claw;

        if (skill_failure_check(ch, gsn_claw, true, 0, null)) {
            return;
        }

        var arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.isEmpty()) {
            victim = ch.fighting;
            if (victim == null) {
                send_to_char("But you aren't fighting anyone!\n", ch);
                return;
            }
        } else if ((victim = get_char_room(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (victim == ch) {
            send_to_char("You don't want to cut your head out.\n", ch);
            return;
        }

        if (is_safe(ch, victim)) {
            return;
        }

        if (IS_AFFECTED(ch, AFF_CHARM) && ch.master == victim) {
            act("But $N is your friend!", ch, null, victim, TO_CHAR);
            return;
        }

        if (ch.mana < 50) {
            send_to_char("You can't get up enough energy.\n", ch);
            return;
        }

        /* size  and weight */
        chance = get_skill(ch, gsn_claw);

        if (IS_AFFECTED(ch, AFF_LION)) {
            chance += 25;
        }

        /* stats */
        chance += get_curr_stat(ch, STAT_STR) + get_curr_stat(ch, STAT_DEX);
        chance -= get_curr_stat(victim, STAT_DEX) * 2;

        if (IS_AFFECTED(ch, AFF_FLYING)) {
            chance -= 10;
        }

        /* speed */
        if (IS_AFFECTED(ch, AFF_HASTE)) {
            chance += 10;
        }
        if (IS_SET(victim.off_flags, OFF_FAST) || IS_AFFECTED(victim, AFF_HASTE)) {
            chance -= 20;
        }

        /* level */
        chance += (ch.level - victim.level) * 2;

        /* now the attack */
        if (number_percent() < chance) {
            ch.mana -= 50;
            check_improve(ch, gsn_claw, true, 1);
            victim.position = POS_RESTING;

            damage_claw = ch.size * 10;
            if (IS_AFFECTED(ch, AFF_LION)) {
                WAIT_STATE(ch, gsn_claw.beats / 2);
                damage_claw += dice(ch.level, 12) + ch.damroll;
            } else {
                WAIT_STATE(ch, gsn_claw.beats);
                damage_claw += dice(ch.level, 24) + ch.damroll;
            }

            damage(ch, victim, damage_claw, gsn_claw, DAM_BASH, true);
        } else {
            ch.mana -= 25;
            damage(ch, victim, 0, gsn_claw, DAM_BASH, true);
            check_improve(ch, gsn_claw, false, 1);
            ch.position = POS_RESTING;
            WAIT_STATE(ch, gsn_claw.beats / 2);
        }
    }

}
