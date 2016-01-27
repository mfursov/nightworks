package net.sf.nightworks;

import net.sf.nightworks.util.TextBuffer;

import static net.sf.nightworks.ActComm.do_say;
import static net.sf.nightworks.ActInfo.do_look;
import static net.sf.nightworks.ActMove.dir_name;
import static net.sf.nightworks.ActMove.do_open;
import static net.sf.nightworks.ActMove.do_stand;
import static net.sf.nightworks.ActMove.move_char;
import static net.sf.nightworks.ActSkill.check_improve;
import static net.sf.nightworks.Comm.act;
import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.DB.get_room_index;
import static net.sf.nightworks.DB.log_string;
import static net.sf.nightworks.DB.number_door;
import static net.sf.nightworks.DB.number_percent;
import static net.sf.nightworks.DB.number_range;
import static net.sf.nightworks.Fight.multi_hit;
import static net.sf.nightworks.Handler.affect_to_char;
import static net.sf.nightworks.Handler.affect_to_room;
import static net.sf.nightworks.Handler.can_see;
import static net.sf.nightworks.Handler.can_see_room;
import static net.sf.nightworks.Handler.char_from_room;
import static net.sf.nightworks.Handler.char_to_room;
import static net.sf.nightworks.Handler.check_material;
import static net.sf.nightworks.Handler.extract_obj;
import static net.sf.nightworks.Handler.get_char_room;
import static net.sf.nightworks.Handler.get_char_world;
import static net.sf.nightworks.Handler.get_curr_stat;
import static net.sf.nightworks.Handler.get_eq_char;
import static net.sf.nightworks.Handler.get_hold_char;
import static net.sf.nightworks.Handler.get_obj_carry;
import static net.sf.nightworks.Handler.get_obj_list;
import static net.sf.nightworks.Handler.get_shield_char;
import static net.sf.nightworks.Handler.get_skill;
import static net.sf.nightworks.Handler.get_weapon_sn;
import static net.sf.nightworks.Handler.get_wield_char;
import static net.sf.nightworks.Handler.is_affected;
import static net.sf.nightworks.Handler.is_affected_room;
import static net.sf.nightworks.Handler.is_metal;
import static net.sf.nightworks.Handler.is_name;
import static net.sf.nightworks.Handler.item_type_name;
import static net.sf.nightworks.Handler.obj_from_char;
import static net.sf.nightworks.Handler.obj_from_room;
import static net.sf.nightworks.Handler.obj_to_char;
import static net.sf.nightworks.Handler.obj_to_room;
import static net.sf.nightworks.Handler.room_is_private;
import static net.sf.nightworks.Handler.skill_failure_check;
import static net.sf.nightworks.Handler.unequip_char;
import static net.sf.nightworks.Interp.number_argument;
import static net.sf.nightworks.Magic.do_cast;
import static net.sf.nightworks.Magic.spell_identify;
import static net.sf.nightworks.Nightworks.ACT_AGGRESSIVE;
import static net.sf.nightworks.Nightworks.ACT_IS_HEALER;
import static net.sf.nightworks.Nightworks.AFFECT_DATA;
import static net.sf.nightworks.Nightworks.AFF_CHARM;
import static net.sf.nightworks.Nightworks.AFF_CURSE;
import static net.sf.nightworks.Nightworks.AFF_ROOM_CURSE;
import static net.sf.nightworks.Nightworks.AFF_ROOM_THIEF_TRAP;
import static net.sf.nightworks.Nightworks.ANGEL;
import static net.sf.nightworks.Nightworks.APPLY_NONE;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.COMM_NOAUCTION;
import static net.sf.nightworks.Nightworks.CON_PLAYING;
import static net.sf.nightworks.Nightworks.DESCRIPTOR_DATA;
import static net.sf.nightworks.Nightworks.EX_CLOSED;
import static net.sf.nightworks.Nightworks.FIGHT_DELAY_TIME;
import static net.sf.nightworks.Nightworks.GATE_BUGGY;
import static net.sf.nightworks.Nightworks.GATE_GOWITH;
import static net.sf.nightworks.Nightworks.GATE_NOCURSE;
import static net.sf.nightworks.Nightworks.GATE_NORMAL_EXIT;
import static net.sf.nightworks.Nightworks.GATE_RANDOM;
import static net.sf.nightworks.Nightworks.IMPLEMENTOR;
import static net.sf.nightworks.Nightworks.IS_AFFECTED;
import static net.sf.nightworks.Nightworks.IS_IMMORTAL;
import static net.sf.nightworks.Nightworks.IS_NPC;
import static net.sf.nightworks.Nightworks.IS_OBJ_STAT;
import static net.sf.nightworks.Nightworks.IS_RAFFECTED;
import static net.sf.nightworks.Nightworks.IS_SET;
import static net.sf.nightworks.Nightworks.IS_TRUSTED;
import static net.sf.nightworks.Nightworks.IS_WEAPON_STAT;
import static net.sf.nightworks.Nightworks.ITEM_ANTI_EVIL;
import static net.sf.nightworks.Nightworks.ITEM_ANTI_NEUTRAL;
import static net.sf.nightworks.Nightworks.ITEM_ARMOR;
import static net.sf.nightworks.Nightworks.ITEM_BLESS;
import static net.sf.nightworks.Nightworks.ITEM_MAGIC;
import static net.sf.nightworks.Nightworks.ITEM_PORTAL;
import static net.sf.nightworks.Nightworks.ITEM_SCROLL;
import static net.sf.nightworks.Nightworks.ITEM_STAFF;
import static net.sf.nightworks.Nightworks.ITEM_WAND;
import static net.sf.nightworks.Nightworks.ITEM_WEAPON;
import static net.sf.nightworks.Nightworks.MAX_CABAL;
import static net.sf.nightworks.Nightworks.MAX_WEAR;
import static net.sf.nightworks.Nightworks.MOUNTED;
import static net.sf.nightworks.Nightworks.OBJ_DATA;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_HAMMER;
import static net.sf.nightworks.Nightworks.POS_RESTING;
import static net.sf.nightworks.Nightworks.POS_SLEEPING;
import static net.sf.nightworks.Nightworks.POS_STANDING;
import static net.sf.nightworks.Nightworks.PULSE_AUCTION;
import static net.sf.nightworks.Nightworks.PULSE_VIOLENCE;
import static net.sf.nightworks.Nightworks.REMOVE_BIT;
import static net.sf.nightworks.Nightworks.ROOM_INDEX_DATA;
import static net.sf.nightworks.Nightworks.ROOM_LAW;
import static net.sf.nightworks.Nightworks.ROOM_NO_RECALL;
import static net.sf.nightworks.Nightworks.ROOM_PRIVATE;
import static net.sf.nightworks.Nightworks.ROOM_SAFE;
import static net.sf.nightworks.Nightworks.ROOM_SOLITARY;
import static net.sf.nightworks.Nightworks.SET_BIT;
import static net.sf.nightworks.Nightworks.STAT_STR;
import static net.sf.nightworks.Nightworks.TO_AFFECTS;
import static net.sf.nightworks.Nightworks.TO_CHAR;
import static net.sf.nightworks.Nightworks.TO_NOTVICT;
import static net.sf.nightworks.Nightworks.TO_ROOM;
import static net.sf.nightworks.Nightworks.TO_ROOM_AFFECTS;
import static net.sf.nightworks.Nightworks.TO_VICT;
import static net.sf.nightworks.Nightworks.UMAX;
import static net.sf.nightworks.Nightworks.WAIT_STATE;
import static net.sf.nightworks.Nightworks.WEAPON_SHARP;
import static net.sf.nightworks.Nightworks.WEAR_BOTH;
import static net.sf.nightworks.Nightworks.WEAR_LEFT;
import static net.sf.nightworks.Nightworks.WEAR_RIGHT;
import static net.sf.nightworks.Nightworks.WEAR_STUCK_IN;
import static net.sf.nightworks.Nightworks.WEAR_TATTOO;
import static net.sf.nightworks.Nightworks.atoi;
import static net.sf.nightworks.Nightworks.auction;
import static net.sf.nightworks.Nightworks.char_list;
import static net.sf.nightworks.Nightworks.current_time;
import static net.sf.nightworks.Nightworks.descriptor_list;
import static net.sf.nightworks.Nightworks.sprintf;
import static net.sf.nightworks.Skill.gsn_axe;
import static net.sf.nightworks.Skill.gsn_hunt;
import static net.sf.nightworks.Skill.gsn_settraps;
import static net.sf.nightworks.Skill.gsn_smithing;
import static net.sf.nightworks.Skill.gsn_world_find;
import static net.sf.nightworks.Special.spec_lookup;
import static net.sf.nightworks.Tables.cabal_table;
import static net.sf.nightworks.util.TextUtils.isDigit;
import static net.sf.nightworks.util.TextUtils.one_argument;
import static net.sf.nightworks.util.TextUtils.smash_tilde;
import static net.sf.nightworks.util.TextUtils.str_cmp;
import static net.sf.nightworks.util.TextUtils.str_prefix;

class ActHera {
    /**
     * random room generation procedure
     */
    static ROOM_INDEX_DATA get_random_room(CHAR_DATA ch) {
        ROOM_INDEX_DATA room;

        for (; ; ) {
            room = get_room_index(number_range(0, 65535));
            if (room != null) {
                if (can_see_room(ch, room)
                        && !room_is_private(room)
                        && !IS_SET(room.room_flags, ROOM_PRIVATE)
                        && !IS_SET(room.room_flags, ROOM_SOLITARY)
                        && !IS_SET(room.room_flags, ROOM_SAFE)
                        && (IS_NPC(ch) || IS_SET(ch.act, ACT_AGGRESSIVE)
                        || !IS_SET(room.room_flags, ROOM_LAW))) {
                    break;
                }
            }
        }

        return room;
    }

/* RT Enter portals */

    static void do_enter(CHAR_DATA ch, String argument) {
        ROOM_INDEX_DATA location;

        if (ch.fighting != null) {
            return;
        }

        /* nifty portal stuff */
        if (argument.length() != 0) {
            ROOM_INDEX_DATA old_room;
            OBJ_DATA portal;
            CHAR_DATA fch, fch_next, mount;

            old_room = ch.in_room;

            portal = get_obj_list(ch, argument, ch.in_room.contents);

            if (portal == null) {
                send_to_char("You don't see that here.\n", ch);
                return;
            }

            if (portal.item_type != ITEM_PORTAL
                    || (IS_SET(portal.value[1], EX_CLOSED) && !IS_TRUSTED(ch, ANGEL))) {
                send_to_char("You can't seem to find a way in.\n", ch);
                return;
            }

            if (!IS_TRUSTED(ch, ANGEL) && !IS_SET(portal.value[2], GATE_NOCURSE)
                    && (IS_AFFECTED(ch, AFF_CURSE)
                    || IS_SET(old_room.room_flags, ROOM_NO_RECALL)
                    || IS_RAFFECTED(old_room, AFF_ROOM_CURSE))) {
                send_to_char("Something prevents you from leaving...\n", ch);
                return;
            }

            if (IS_SET(portal.value[2], GATE_RANDOM) || portal.value[3] == -1) {
                location = get_random_room(ch);
                portal.value[3] = location.vnum; /* keeps record */
            } else if (IS_SET(portal.value[2], GATE_BUGGY) && (number_percent() < 5)) {
                location = get_random_room(ch);
            } else {
                location = get_room_index(portal.value[3]);
            }

            if (location == null
                    || location == old_room
                    || !can_see_room(ch, location)
                    || (room_is_private(location) && !IS_TRUSTED(ch, IMPLEMENTOR))) {
                act("$p doesn't seem to go anywhere.", ch, portal, null, TO_CHAR);
                return;
            }

            if (IS_NPC(ch) && IS_SET(ch.act, ACT_AGGRESSIVE)
                    && IS_SET(location.room_flags, ROOM_LAW)) {
                send_to_char("Something prevents you from leaving...\n", ch);
                return;
            }

            TextBuffer buf = new TextBuffer();
            if (MOUNTED(ch) != null) {
                buf.sprintf("$n steps into $p, riding on %s.", MOUNTED(ch).short_descr);
            } else {
                buf.sprintf("$n steps into $p.");
            }
            act(buf.toString(), ch, portal, null, TO_ROOM);

            if (IS_SET(portal.value[2], GATE_NORMAL_EXIT)) {
                act("You enter $p.", ch, portal, null, TO_CHAR);
            } else {
                act("You walk through $p and find yourself somewhere else...",
                        ch, portal, null, TO_CHAR);
            }

            mount = MOUNTED(ch);
            char_from_room(ch);
            char_to_room(ch, location);

            if (IS_SET(portal.value[2], GATE_GOWITH)) /* take the gate along */ {
                obj_from_room(portal);
                obj_to_room(portal, location);
            }

            if (IS_SET(portal.value[2], GATE_NORMAL_EXIT)) {
                if (mount != null) {
                    act("$n has arrived, riding $N", ch, portal, mount, TO_ROOM);
                } else {
                    act("$n has arrived.", ch, portal, null, TO_ROOM);
                }
            } else {
                if (mount != null) {
                    act("$n has arrived through $p, riding $N.", ch, portal, mount, TO_ROOM);
                } else {
                    act("$n has arrived through $p.", ch, portal, null, TO_ROOM);
                }
            }

            do_look(ch, "auto");

            if (mount != null) {
                char_from_room(mount);
                char_to_room(mount, location);
                ch.riding = true;
                mount.riding = true;
            }

            /* charges */
            if (portal.value[0] > 0) {
                portal.value[0]--;
                if (portal.value[0] == 0) {
                    portal.value[0] = -1;
                }
            }

            /* protect against circular follows */

            for (fch = old_room.people; fch != null; fch = fch_next) {
                fch_next = fch.next_in_room;

                if (portal.value[0] == -1)
                    /* no following through dead portals */ {
                    continue;
                }

                if (fch.master == ch && IS_AFFECTED(fch, AFF_CHARM)
                        && fch.position < POS_STANDING) {
                    do_stand(fch, "");
                }

                if (fch.master == ch && fch.position == POS_STANDING) {

                    if (IS_SET(ch.in_room.room_flags, ROOM_LAW)
                            && (IS_NPC(fch) && IS_SET(fch.act, ACT_AGGRESSIVE))) {
                        act("You can't bring $N into the city.",
                                ch, null, fch, TO_CHAR);
                        act("You aren't allowed in the city.",
                                fch, null, null, TO_CHAR);
                        continue;
                    }

                    act("You follow $N.", fch, null, ch, TO_CHAR);
                    do_enter(fch, argument);
                }
            }

            if (portal.value[0] == -1) {
                act("$p fades out of existence.", ch, portal, null, TO_CHAR);
                if (ch.in_room == old_room) {
                    act("$p fades out of existence.", ch, portal, null, TO_ROOM);
                } else if (old_room.people != null) {
                    act("$p fades out of existence.",
                            old_room.people, portal, null, TO_CHAR);
                    act("$p fades out of existence.",
                            old_room.people, portal, null, TO_ROOM);
                }
                extract_obj(portal);
            }
            return;
        }

        send_to_char("Nope, can't do it.\n", ch);
    }

    static void do_settraps(CHAR_DATA ch) {
        if (skill_failure_check(ch, gsn_settraps, false, 0, "You don't know how to set traps.\n")) {
            return;
        }

        if (ch.in_room == null) {
            return;
        }

        if (IS_SET(ch.in_room.room_flags, ROOM_LAW)) {
            send_to_char("A mystical power protects the room.\n", ch);
            return;
        }

        WAIT_STATE(ch, gsn_settraps.beats);

        if (IS_NPC(ch)
                || number_percent() < (get_skill(ch, gsn_settraps) * 0.7)) {

            check_improve(ch, gsn_settraps, true, 1);

            if (is_affected_room(ch.in_room, gsn_settraps)) {
                send_to_char("This room has already trapped.\n", ch);
                return;
            }

            if (is_affected(ch, gsn_settraps)) {
                send_to_char("This skill is used too recently.\n", ch);
                return;
            }
            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_ROOM_AFFECTS;
            af.type = gsn_settraps;
            af.level = ch.level;
            af.duration = ch.level / 40;
            af.location = APPLY_NONE;
            af.modifier = 0;
            af.bitvector = AFF_ROOM_THIEF_TRAP;
            affect_to_room(ch.in_room, af);

            AFFECT_DATA af2 = new AFFECT_DATA();
            af2.where = TO_AFFECTS;
            af2.type = gsn_settraps;
            af2.level = ch.level;

            if (ch.last_fight_time != -1 && !IS_IMMORTAL(ch) && (current_time - ch.last_fight_time) < FIGHT_DELAY_TIME) {
                af2.duration = 1;
            } else {
                af2.duration = ch.level / 10;
            }

            af2.modifier = 0;
            af2.location = APPLY_NONE;
            af2.bitvector = 0;
            affect_to_char(ch, af2);
            send_to_char("You set the room with your trap.\n", ch);
            act("$n set the room with $s trap.", ch, null, null, TO_ROOM);
        } else {
            check_improve(ch, gsn_settraps, false, 1);
        }

    }

    static CHAR_DATA get_char_area(CHAR_DATA ch, String argument) {
        CHAR_DATA ach;
        int number;
        int count;

        if (argument.length() == 0) {
            return null;
        }

        StringBuilder arg = new StringBuilder();
        number = number_argument(argument, arg);
        if (arg.length() == 0) {
            return null;
        }
        count = 0;

        if ((ach = get_char_room(ch, argument)) != null) {
            return ach;
        }


        for (ach = char_list; ach != null; ach = ach.next) {
            if (ach.in_room != null && (ach.in_room.area != ch.in_room.area
                    || !can_see(ch, ach) || !is_name(arg.toString(), ach.name))) {
                continue;
            }
            if (++count == number) {
                return ach;
            }
        }
        return null;
    }


    static int find_path(int in_room_vnum, int out_room_vnum, CHAR_DATA ch, int depth, boolean in_zone) {
        //TODO:
        return -1;
    }


    static void do_hunt(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        int direction, i;
        boolean fArea, ok;


        if (skill_failure_check(ch, gsn_hunt, false, 0, null)) {
            return;
        }
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0) {
            send_to_char("Whom are you trying to hunt?\n", ch);
            return;
        }

/*  fArea = ( get_trust(ch) < MAX_LEVEL ); */
        fArea = !(IS_IMMORTAL(ch));

        if (number_percent() < get_skill(ch, gsn_world_find)) {
            fArea = false;
            check_improve(ch, gsn_world_find, true, 1);
        } else {
            check_improve(ch, gsn_world_find, false, 1);
        }

        if (fArea) {
            victim = get_char_area(ch, arg.toString());
        } else {
            victim = get_char_world(ch, arg.toString());
        }

        if (victim == null) {
            send_to_char("No-one around by that name.\n", ch);
            return;
        }

        if (ch.in_room == victim.in_room) {
            act("$N is here!", ch, null, victim, TO_CHAR);
            return;
        }

        if (IS_NPC(ch)) {
            ch.hunting = victim;
            hunt_victim(ch);
            return;
        }

        /*
        * Deduct some movement.
        */
        if (!IS_IMMORTAL(ch)) {
            if (ch.endur > 2) {
                ch.endur -= 3;
            } else {
                send_to_char("You're too exhausted to hunt anyone!\n", ch);
                return;
            }
        }

        act("$n stares intently at the ground.", ch, null, null, TO_ROOM);

        WAIT_STATE(ch, gsn_hunt.beats);
        direction = find_path(ch.in_room.vnum, victim.in_room.vnum, ch, -40000, fArea);

        if (direction == -1) {
            act("You couldn't find a path to $N from here.",
                    ch, null, victim, TO_CHAR);
            return;
        }

        if (direction < 0 || direction > 5) {
            send_to_char("Hmm... Something seems to be wrong.\n", ch);
            return;
        }

        /*
        * Give a random direction if the player misses the die roll.
        */
        if (IS_NPC(ch) && number_percent() > 75)        /* NPC @ 25% */ {
            log_string("Do PC hunt");
            ok = false;
            for (i = 0; i < 6; i++) {
                if (ch.in_room.exit[direction] != null) {
                    ok = true;
                    break;
                }
            }
            if (ok) {
                do {
                    direction = number_door();
                }
                while ((ch.in_room.exit[direction] == null)
                        || (ch.in_room.exit[direction].to_room == null));
            } else {
                log_string("Do hunt, player hunt, no exits from room!");
                ch.hunting = null;
                send_to_char("Your room has not exits!!!!\n", ch);
                return;
            }
            /*
            * Display the results of the search.
            */
        }
        TextBuffer buf = new TextBuffer();
        buf.sprintf("$N is %s from here.", dir_name[direction]);
        act(buf, ch, null, victim, TO_CHAR);
    }

/*
* revised by chronos.
*/

    static void hunt_victim(CHAR_DATA ch) {
        int dir;
        boolean found;
        CHAR_DATA tmp;

        /*
        * Make sure the victim still exists.
        */
        TextBuffer buf = new TextBuffer();
        for (found = false, tmp = char_list; tmp != null && !found; tmp = tmp.next) {
            if (ch.hunting == tmp) {
                found = true;
            }
        }

        if (!found || !can_see(ch, ch.hunting)) {
            if (get_char_area(ch, ch.hunting.name) != null) {
                sprintf(buf, "portal %s", ch.hunting.name);
                log_string("mob portal");
                do_cast(ch, buf.toString());
                log_string("do_enter1");
                do_enter(ch, "portal");
                if (ch.in_room == null || ch.hunting == null) {
                    return;
                }
                if (ch.in_room == ch.hunting.in_room) {
                    act("$n glares at $N and says, 'Ye shall DIE!'",
                            ch, null, ch.hunting, TO_NOTVICT);
                    act("$n glares at you and says, 'Ye shall DIE!'",
                            ch, null, ch.hunting, TO_VICT);
                    act("You glare at $N and say, 'Ye shall DIE!",
                            ch, null, ch.hunting, TO_CHAR);
                    multi_hit(ch, ch.hunting, null);
                    ch.hunting = null; /* No more hunting, now tracking */
                    return;
                }
                log_string("done1");
                return;
            } else {
                do_say(ch, "Ahhhh!  My prey is gone!!");
                ch.hunting = null;
                return;
            }
        }   /* end if !found or !can_see */


        dir = find_path(ch.in_room.vnum, ch.hunting.in_room.vnum, ch, -40000, true);

        if (dir < 0 || dir > 5) {
/* 1 */
            if (get_char_area(ch, ch.hunting.name) != null && ch.level > 35) {
                sprintf(buf, "portal %s", ch.hunting.name);
                log_string("mob portal");
                do_cast(ch, buf.toString());
                log_string("do_enter2");
                do_enter(ch, "portal");
                if (ch.in_room == null || ch.hunting == null) {
                    return;
                }
                if (ch.in_room == ch.hunting.in_room) {
                    act("$n glares at $N and says, 'Ye shall DIE!'",
                            ch, null, ch.hunting, TO_NOTVICT);
                    act("$n glares at you and says, 'Ye shall DIE!'",
                            ch, null, ch.hunting, TO_VICT);
                    act("You glare at $N and say, 'Ye shall DIE!",
                            ch, null, ch.hunting, TO_CHAR);
                    multi_hit(ch, ch.hunting, null);
                    ch.hunting = null;
                    return;
                }
                log_string("done2");
                return;
            } else {
                act("$n says 'I have lost $M!'", ch, null, ch.hunting, TO_ROOM);
                ch.hunting = null;
                return;
            }
        } /* if dir < 0 or > 5 */


        if (ch.in_room.exit[dir] != null && IS_SET(ch.in_room.exit[dir].exit_info, EX_CLOSED)) {
            do_open(ch, dir_name[dir]);
            return;
        }
        if (ch.in_room.exit[dir] == null) {
            log_string("BUG:  hunt through null door");
            ch.hunting = null;
            return;
        }
        move_char(ch, dir);
        if (ch.in_room == null || ch.hunting == null) {
            return;
        }
        if (ch.in_room == ch.hunting.in_room) {
            act("$n glares at $N and says, 'Ye shall DIE!'",
                    ch, null, ch.hunting, TO_NOTVICT);
            act("$n glares at you and says, 'Ye shall DIE!'",
                    ch, null, ch.hunting, TO_VICT);
            act("You glare at $N and say, 'Ye shall DIE!",
                    ch, null, ch.hunting, TO_CHAR);
            multi_hit(ch, ch.hunting, null);
            ch.hunting = null;
        }
    }


    /**
     * ************************************************************************
     * ***********************      repair.c       ******************************
     * *************************************************************************
     */


    static void damage_to_obj(CHAR_DATA ch, OBJ_DATA wield, OBJ_DATA worn, int damage) {

        if (damage == 0) {
            return;
        }
        worn.condition -= damage;

        act("{gThe $p inflicts damage on $P.{x", ch, wield, worn, TO_ROOM, POS_RESTING);

        if (worn.condition < 1) {
            act("{WThe $P breaks into pieces.{x", ch, wield, worn, TO_ROOM, POS_RESTING);
            extract_obj(worn);
            return;
        }


        if ((IS_SET(wield.extra_flags, ITEM_ANTI_EVIL)
                && IS_SET(wield.extra_flags, ITEM_ANTI_NEUTRAL))
                && (IS_SET(worn.extra_flags, ITEM_ANTI_EVIL)
                && IS_SET(worn.extra_flags, ITEM_ANTI_NEUTRAL))) {
            act("{g$p doesn't want to fight against $P.{x", ch, wield, worn, TO_ROOM, POS_RESTING);
            act("{g$p removes itself from you!{x.", ch, wield, worn, TO_CHAR, POS_RESTING);
            act("{g$p removes itself from $n{x.", ch, wield, worn, TO_ROOM, POS_RESTING);
            unequip_char(ch, wield);
            return;
        }

        if (IS_SET(wield.extra_flags, ITEM_ANTI_EVIL) && IS_SET(worn.extra_flags, ITEM_ANTI_EVIL)) {
            act("{gThe $p worries for the damage to $P.{x", ch, wield, worn, TO_ROOM, POS_RESTING);
        }
    }


    static void check_weapon_destroy(CHAR_DATA ch, CHAR_DATA victim, boolean second) {
        OBJ_DATA wield, destroy;
        int chance = 0;

        if (IS_NPC(victim) || number_percent() < 94) {
            return;
        }

        if ((wield = get_wield_char(ch, second)) == null) {
            return;
        }
        Skill sn = get_weapon_sn(ch, second);
        int skill = get_skill(ch, sn);

        if (is_metal(wield)) {
            for (int i = 0; i < MAX_WEAR; i++) {
                if ((destroy = get_eq_char(victim, i)) == null
                        || number_percent() > 95
                        || number_percent() > 94
                        || number_percent() > skill
                        || ch.level < (victim.level - 10)
                        || check_material(destroy, "platinum")
                        || destroy.pIndexData.limit != -1
                        || (i == WEAR_LEFT || i == WEAR_RIGHT || i == WEAR_BOTH
                        || i == WEAR_TATTOO || i == WEAR_STUCK_IN)) {
                    continue;
                }

                chance += 20;
                if (check_material(wield, "platinium") ||
                        check_material(wield, "titanium")) {
                    chance += 5;
                }

                if (is_metal(destroy)) {
                    chance -= 20;
                } else {
                    chance += 20;
                }

                chance += ((ch.level - victim.level) / 5);

                chance += ((wield.level - destroy.level) / 2);

                /* sharpness    */
                if (IS_WEAPON_STAT(wield, WEAPON_SHARP)) {
                    chance += 10;
                }

                if (sn == gsn_axe) {
                    chance += 10;
                }
                /* spell affects */
                if (IS_OBJ_STAT(destroy, ITEM_BLESS)) {
                    chance -= 10;
                }
                if (IS_OBJ_STAT(destroy, ITEM_MAGIC)) {
                    chance -= 20;
                }

                chance += skill - 85;
                chance += get_curr_stat(ch, STAT_STR);

/*   chance /= 2;   */
                if (number_percent() < chance && chance > 50) {
                    damage_to_obj(ch, wield, destroy, (chance / 5));
                    break;
                }
            }
        } else {
            for (int i = 0; i < MAX_WEAR; i++) {
                if ((destroy = get_eq_char(victim, i)) == null
                        || number_percent() > 95
                        || number_percent() > 94
                        || number_percent() < skill
                        || ch.level < (victim.level - 10)
                        || check_material(destroy, "platinum")
                        || destroy.pIndexData.limit != -1
                        || (i == WEAR_LEFT || i == WEAR_RIGHT || i == WEAR_BOTH
                        || i == WEAR_TATTOO || i == WEAR_STUCK_IN)) {
                    continue;
                }

                chance += 10;

                if (is_metal(destroy)) {
                    chance -= 20;
                }

                chance += (ch.level - victim.level);

                chance += (wield.level - destroy.level);

                /* sharpness    */
                if (IS_WEAPON_STAT(wield, WEAPON_SHARP)) {
                    chance += 10;
                }

                if (sn == gsn_axe) {
                    chance += 10;
                }

                /* spell affects */
                if (IS_OBJ_STAT(destroy, ITEM_BLESS)) {
                    chance -= 10;
                }
                if (IS_OBJ_STAT(destroy, ITEM_MAGIC)) {
                    chance -= 20;
                }

                chance += skill - 85;
                chance += get_curr_stat(ch, STAT_STR);

/*   chance /= 2;   */
                if (number_percent() < chance && chance > 50) {
                    damage_to_obj(ch, wield, destroy, chance / 5);
                    break;
                }
            }
        }

    }


    static void do_repair(CHAR_DATA ch, String argument) {
        CHAR_DATA mob;
        OBJ_DATA obj;
        int cost;

        for (mob = ch.in_room.people; mob != null; mob = mob.next_in_room) {
            if (!IS_NPC(mob)) {
                continue;
            }
            if (mob.spec_fun == spec_lookup("spec_repairman")) {
                break;
            }
        }

        if (mob == null) {
            send_to_char("You can't do that here.\n", ch);
            return;
        }

        StringBuilder argb = new StringBuilder();
        one_argument(argument, argb);
        String arg = argb.toString();

        if (arg.length() == 0) {
            do_say(mob, "I will repair a weapon for you, for a price.");
            send_to_char("Type estimate <weapon> to be assessed for damage.\n", ch);
            return;
        }
        if ((obj = get_obj_carry(ch, arg)) == null) {
            do_say(mob, "You don't have that item");
            return;
        }

        if (obj.pIndexData.vnum == OBJ_VNUM_HAMMER) {
            do_say(mob, "That hammer is beyond my power.");
            return;
        }

        if (obj.condition >= 100) {
            do_say(mob, "But that item is not broken.");
            return;
        }

        TextBuffer buf = new TextBuffer();
        if (obj.cost == 0) {
            sprintf(buf, "%s is beyond repair.\n", obj.short_descr);
            do_say(mob, buf.toString());
            return;
        }

        cost = ((obj.level * 10) + ((obj.cost * (100 - obj.condition)) / 100));
        cost /= 100;

        if (cost > ch.gold) {
            do_say(mob, "You do not have enough gold for my services.");
            return;
        }

        WAIT_STATE(ch, PULSE_VIOLENCE);

        ch.gold -= cost;
        mob.gold += cost;
        buf.sprintf("$N takes %s from $n, repairs it, and returns it to $n", obj.short_descr);
        act(buf, ch, null, mob, TO_ROOM);
        buf.sprintf("%s takes %s, repairs it, and returns it\n", mob.short_descr, obj.short_descr);
        send_to_char(buf, ch);
        obj.condition = 100;
    }

    static void do_estimate(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        CHAR_DATA mob;
        int cost;

        for (mob = ch.in_room.people; mob != null; mob = mob.next_in_room) {
            if (!IS_NPC(mob)) {
                continue;
            }
            if (mob.spec_fun == spec_lookup("spec_repairman")) {
                break;
            }
        }

        if (mob == null) {
            send_to_char("You can't do that here.\n", ch);
            return;
        }
        StringBuilder argb = new StringBuilder();
        one_argument(argument, argb);
        String arg = argb.toString();

        if (arg.length() == 0) {
            do_say(mob, "Try estimate <item>");
            return;
        }
        if ((obj = (get_obj_carry(ch, arg))) == null) {
            do_say(mob, "You don't have that item");
            return;
        }
        if (obj.pIndexData.vnum == OBJ_VNUM_HAMMER) {
            do_say(mob, "That hammer is beyond my power.");
            return;
        }
        if (obj.condition >= 100) {
            do_say(mob, "But that item's not broken");
            return;
        }
        if (obj.cost == 0) {
            do_say(mob, "That item is beyond repair");
            return;
        }

        cost = ((obj.level * 10) +
                ((obj.cost * (100 - obj.condition)) / 100));
        cost /= 100;
        TextBuffer buf = new TextBuffer();
        buf.sprintf("It will cost %d to fix that item", cost);
        do_say(mob, buf.toString());
    }

    static void do_restring(CHAR_DATA ch, String argument) {
        CHAR_DATA mob;
        OBJ_DATA obj;
        int cost = 2000;

        for (mob = ch.in_room.people; mob != null; mob = mob.next_in_room) {
            if (IS_NPC(mob) && IS_SET(mob.act, ACT_IS_HEALER)) {
                break;
            }
        }

        if (mob == null) {
            send_to_char("You can't do that here.\n", ch);
            return;
        }

        argument = smash_tilde(argument);
        StringBuilder arg = new StringBuilder();
        StringBuilder arg1 = new StringBuilder();
        argument = one_argument(argument, arg);
        argument = one_argument(argument, arg1);
        String arg2 = argument;

        if (arg.length() == 0 || arg1.length() == 0 || arg2.length() == 0) {
            send_to_char("Syntax:\n", ch);
            send_to_char("  restring <obj> <field> <string>\n", ch);
            send_to_char("    fields: name int long\n", ch);
            return;
        }

        if ((obj = (get_obj_carry(ch, arg.toString()))) == null) {
            send_to_char("The Stringer says '`sYou don't have that item``.'\n", ch);
            return;
        }

        cost += (obj.level * 1500);

        if (cost > ch.gold) {
            act("$N says 'You do not have enough gold for my services.'",
                    ch, null, mob, TO_CHAR);
            return;
        }

        String arg1str = arg1.toString();
        if (!str_prefix(arg1str, "name")) {
            obj.name = arg2;
        } else if (!str_prefix(arg1str, "short")) {
            obj.short_descr = arg2;
        } else if (!str_prefix(arg1str, "long")) {
            obj.description = arg2;
        } else {
            send_to_char("That's not a valid Field.\n", ch);
            return;
        }

        WAIT_STATE(ch, PULSE_VIOLENCE);

        ch.gold -= cost;
        mob.gold += cost;
        TextBuffer buf = new TextBuffer();
        buf.sprintf("$N takes $n's item, tinkers with it, and returns it to $n.");
        act(buf, ch, null, mob, TO_ROOM);
        buf.sprintf("%s takes your item, tinkers with it, and returns %s to you.\n", mob.short_descr, obj.short_descr);
        send_to_char(buf, ch);
        send_to_char("Remember, if we find your new string offensive, we will not be happy.\n", ch);
        send_to_char(" This is your ONE AND ONLY Warning.\n", ch);
    }

    static void check_shield_destroyed(CHAR_DATA ch, CHAR_DATA victim, boolean second) {
        OBJ_DATA wield, destroy;
        int chance = 0;

        if (IS_NPC(victim) || number_percent() < 94) {
            return;
        }

        if ((wield = get_wield_char(ch, second)) == null) {
            return;
        }
        Skill sn = get_weapon_sn(ch, second);
        int skill = get_skill(ch, sn);

        destroy = get_shield_char(victim);

        if (destroy == null) {
            return;
        }

        if (is_metal(wield)) {
            if (number_percent() > 94
                    || number_percent() > skill
                    || ch.level < (victim.level - 10)
                    || check_material(destroy, "platinum")
                    || destroy.pIndexData.limit != -1) {
                return;
            }

            chance += 20;
            if (check_material(wield, "platinium") ||
                    check_material(wield, "titanium")) {
                chance += 5;
            }

            if (is_metal(destroy)) {
                chance -= 20;
            } else {
                chance += 20;
            }

            chance += ((ch.level - victim.level) / 5);

            chance += ((wield.level - destroy.level) / 2);

            /* sharpness    */
            if (IS_WEAPON_STAT(wield, WEAPON_SHARP)) {
                chance += 10;
            }

            if (sn == gsn_axe) {
                chance += 10;
            }
            /* spell affects */
            if (IS_OBJ_STAT(destroy, ITEM_BLESS)) {
                chance -= 10;
            }
            if (IS_OBJ_STAT(destroy, ITEM_MAGIC)) {
                chance -= 20;
            }

            chance += skill - 85;
            chance += get_curr_stat(ch, STAT_STR);

/*   chance /= 2;   */
            if (number_percent() < chance && chance > 20) {
                damage_to_obj(ch, wield, destroy, (chance / 4));
            }
        } else {
            if (number_percent() > 94
                    || number_percent() < skill
                    || ch.level < (victim.level - 10)
                    || check_material(destroy, "platinum")
                    || destroy.pIndexData.limit != -1) {
                return;
            }

            chance += 10;

            if (is_metal(destroy)) {
                chance -= 20;
            }

            chance += (ch.level - victim.level);

            chance += (wield.level - destroy.level);

            /* sharpness    */
            if (IS_WEAPON_STAT(wield, WEAPON_SHARP)) {
                chance += 10;
            }

            if (sn == gsn_axe) {
                chance += 10;
            }

            /* spell affects */
            if (IS_OBJ_STAT(destroy, ITEM_BLESS)) {
                chance -= 10;
            }
            if (IS_OBJ_STAT(destroy, ITEM_MAGIC)) {
                chance -= 20;
            }

            chance += skill - 85;
            chance += get_curr_stat(ch, STAT_STR);

/*   chance /= 2;   */
            if (number_percent() < chance && chance > 20) {
                damage_to_obj(ch, wield, destroy, (chance / 4));
            }
        }
    }

    static void check_weapon_destroyed(CHAR_DATA ch, CHAR_DATA victim, boolean second) {
        OBJ_DATA wield, destroy;
        int chance = 0;

        if (IS_NPC(victim) || number_percent() < 94) {
            return;
        }

        if ((wield = get_wield_char(ch, second)) == null) {
            return;
        }
        Skill sn = get_weapon_sn(ch, second);
        int skill = get_skill(ch, sn);

        destroy = get_wield_char(victim, false);
        if (destroy == null) {
            return;
        }

        if (is_metal(wield)) {
            if (number_percent() > 94
                    || number_percent() > skill
                    || ch.level < (victim.level - 10)
                    || check_material(destroy, "platinum")
                    || destroy.pIndexData.limit != -1) {
                return;
            }

            chance += 20;
            if (check_material(wield, "platinium") ||
                    check_material(wield, "titanium")) {
                chance += 5;
            }

            if (is_metal(destroy)) {
                chance -= 20;
            } else {
                chance += 20;
            }

            chance += ((ch.level - victim.level) / 5);

            chance += ((wield.level - destroy.level) / 2);

            /* sharpness    */
            if (IS_WEAPON_STAT(wield, WEAPON_SHARP)) {
                chance += 10;
            }

            if (sn == gsn_axe) {
                chance += 10;
            }
            /* spell affects */
            if (IS_OBJ_STAT(destroy, ITEM_BLESS)) {
                chance -= 10;
            }
            if (IS_OBJ_STAT(destroy, ITEM_MAGIC)) {
                chance -= 20;
            }

            chance += skill - 85;
            chance += get_curr_stat(ch, STAT_STR);

/*   chance /= 2;   */
            if (number_percent() < (chance / 2) && chance > 20) {
                damage_to_obj(ch, wield, destroy, (chance / 4));
            }
        } else {
            if (number_percent() > 94
                    || number_percent() < skill
                    || ch.level < (victim.level - 10)
                    || check_material(destroy, "platinum")
                    || destroy.pIndexData.limit != -1) {
                return;
            }

            chance += 10;

            if (is_metal(destroy)) {
                chance -= 20;
            }

            chance += (ch.level - victim.level);

            chance += (wield.level - destroy.level);

            /* sharpness    */
            if (IS_WEAPON_STAT(wield, WEAPON_SHARP)) {
                chance += 10;
            }

            if (sn == gsn_axe) {
                chance += 10;
            }

            /* spell affects */
            if (IS_OBJ_STAT(destroy, ITEM_BLESS)) {
                chance -= 10;
            }
            if (IS_OBJ_STAT(destroy, ITEM_MAGIC)) {
                chance -= 20;
            }

            chance += skill - 85;
            chance += get_curr_stat(ch, STAT_STR);

/*   chance /= 2;   */
            if (number_percent() < (chance / 2) && chance > 20) {
                damage_to_obj(ch, wield, destroy, chance / 4);
            }
        }
    }


    static void do_smithing(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        OBJ_DATA hammer;

        if (skill_failure_check(ch, gsn_smithing, false, 0, null)) {
            return;
        }

        if (ch.fighting != null) {
            send_to_char("Wait until the fight finishes.\n", ch);
            return;
        }

        StringBuilder argb = new StringBuilder();
        one_argument(argument, argb);
        String arg = argb.toString();

        if (arg.length() == 0) {
            send_to_char("Which object do you want to repair.\n", ch);
            return;
        }

        if ((obj = get_obj_carry(ch, arg)) == null) {
            send_to_char("You are not carrying that.\n", ch);
            return;
        }

        if (obj.condition >= 100) {
            send_to_char("But that item is not broken.\n", ch);
            return;
        }

        if ((hammer = get_hold_char(ch)) == null) {
            send_to_char("You are not holding a hammer.\n", ch);
            return;
        }

        if (hammer.pIndexData.vnum != OBJ_VNUM_HAMMER) {
            send_to_char("That is not the correct hammer.\n", ch);
            return;
        }

        WAIT_STATE(ch, 2 * PULSE_VIOLENCE);
        TextBuffer buf = new TextBuffer();
        if (number_percent() > get_skill(ch, gsn_smithing)) {
            check_improve(ch, gsn_smithing, false, 8);
            buf.sprintf("$n try to repair %s with the hammer.But $n fails.", obj.short_descr);
            act(buf, ch, null, obj, TO_ROOM);
            buf.sprintf("You failed to repair %s\n", obj.short_descr);
            send_to_char(buf, ch);
            hammer.condition -= 25;
        } else {
            check_improve(ch, gsn_smithing, true, 4);
            buf.sprintf("$n repairs %s with the hammer.", obj.short_descr);
            act(buf, ch, null, null, TO_ROOM);
            buf.sprintf("You repair %s\n", obj.short_descr);
            send_to_char(buf, ch);
            obj.condition = UMAX(100,
                    obj.condition + (get_skill(ch, gsn_smithing) / 2));
            hammer.condition -= 25;
        }
        if (hammer.condition < 1) {
            extract_obj(hammer);
        }
    }

    /***************************************************************************
     ************************      auction.c      ******************************
     ***************************************************************************/

    /**
     * ************************************************************************
     * This snippet was orginally written by Erwin S. Andreasen.              *
     * erwin@pip.dknet.dk, http://pip.dknet.dk/~pip1773/                  *
     * Adopted to Nightworks MUD by chronos.                                    *
     * *************************************************************************
     */


    static void talk_auction(String argument) {
        DESCRIPTOR_DATA d;
        CHAR_DATA original;
        TextBuffer buf = new TextBuffer();
        sprintf(buf, "AUCTION: %s", argument);

        for (d = descriptor_list; d != null; d = d.next) {
            original = d.original != null ? d.original : d.character; /* if switched */
            if ((d.connected == CON_PLAYING) && !IS_SET(original.comm, COMM_NOAUCTION)) {
                act(buf, original, null, null, TO_CHAR);
            }

        }
    }

/*
  This function allows the following kinds of bets to be made:

  Absolute bet
  ============

  bet 14k, bet 50m66, bet 100k

  Relative bet
  ============

  These bets are calculated relative to the current bet. The '+' symbol adds
  a certain number of percent to the current bet. The default is 25, so
  with a current bet of 1000, bet + gives 1250, bet +50 gives 1500 etc.
  Please note that the number must follow exactly after the +, without any
  spaces!

  The '*' or 'x' bet multiplies the current bet by the number specified,
  defaulting to 2. If the current bet is 1000, bet x  gives 2000, bet x10
  gives 10,000 etc.

*/

    static int advatoi(String s) {
/*
  util function, converts an 'advanced' ASCII-number-string into a number.
  Used by parsebet() but could also be used by do_give or do_wimpy.

  Advanced strings can contain 'k' (or 'K') and 'm' ('M') in them, not just
  numbers. The letters multiply whatever is left of them by 1,000 and
  1,000,000 respectively. Example:

  14k = 14 * 1,000 = 14,000
  23m = 23 * 1,000,0000 = 23,000,000

  If any digits follow the 'k' or 'm', the are also added, but the number
  which they are multiplied is divided by ten, each time we get one left. This
  is best illustrated in an example :)

  14k42 = 14 * 1000 + 14 * 100 + 2 * 10 = 14420

  Of course, it only pays off to use that notation when you can skip many 0's.
  There is not much point in writing 66k666 instead of 66666, except maybe
  when you want to make sure that you get 66,666.

  More than 3 (in case of 'k') or 6 ('m') digits after 'k'/'m' are automatically
  disregarded. Example:

  14k1234 = 14,123

  If the number contains any other characters than digits, 'k' or 'm', the
  function returns 0. It also returns 0 if 'k' or 'm' appear more than
  once.

*/

/* the pointer to buffer stuff is not really necessary, but originally I
   modified the buffer, so I had to make a copy of it. What the hell, it
   works:) (read: it seems to work:)
*/

        int number = 0;           /* number to be returned */
        int multiplier;       /* multiplier used to get the extra digits right */


        int pos = 0;
        while (pos < s.length() && isDigit(s.charAt(pos))) {/* as long as the current character is a digit */
            number = (number * 10) + atoi(s.substring(pos, pos + 1)); /* add to current number */
            pos++;                                /* advance */
        }
        if (pos >= s.length() - 1) {
            return number;
        }

        switch (s.charAt(pos)) {
            case 'k':
            case 'K':
                multiplier = 1000;
                number *= multiplier;
                pos++;
                break;
            case 'm':
            case 'M':
                multiplier = 1000000;
                number *= multiplier;
                pos++;
                break;
            default:
                return 0; /* not k nor m nor NUL - return 0! */
        }

        while (pos < s.length() && isDigit(s.charAt(pos)) && multiplier > 1) /* if any digits follow k/m, add those too */ {
            multiplier = multiplier / 10;  /* the further we get to right, the less are the digit 'worth' */
            number = number + atoi(s.substring(pos, pos + 1)) * multiplier;
            pos++;
        }
        if (pos != s.length() - 1) {
            return 0;
        }
        return number;
    }


    static int parsebet(int currentbet, String argument) {
        int newbet = 0;                /* a variable to temporarily hold the new bet */
        if (argument.length() > 0)               /* check for an empty string */ {

            if (Character.isDigit(argument.charAt(0))) /* first char is a digit assume e.g. 433k */ {
                newbet = advatoi(argument); /* parse and set newbet to that value */
            } else if (argument.charAt(0) == '+') /* add ?? percent */ {
                if (argument.length() == 1) /* only + specified, assume default */ {
                    newbet = (currentbet * 125) / 100; /* default: add 25% */
                } else {
                    newbet = (currentbet * (100 + atoi(argument.substring(1)))) / 100; /* cut off the first char */
                }
            } else {
                TextBuffer buf = new TextBuffer();
                buf.sprintf("considering: * x \n");
                if ((argument.charAt(0) == '*') || (argument.charAt(0) == 'x')) /* multiply */ {
                    if (argument.length() == 1) /* only x specified, assume default */ {
                        newbet = currentbet * 2; /* default: twice */
                    } else /* user specified a number */ {
                        newbet = currentbet * atoi(argument.substring(1)); /* cut off the first char */
                    }
                }
            }
        }
        return newbet;        /* return the calculated bet */
    }


    static void auction_update() {

        TextBuffer buf = new TextBuffer();
        if (auction.item != null) {
            if (--auction.pulse <= 0) /* decrease pulse */ {
                auction.pulse = PULSE_AUCTION;
                switch (++auction.going) /* increase the going state */ {
                    case 1: /* going once */
                    case 2: /* going twice */
                        if (auction.bet > 0) {
                            sprintf(buf, "%s: going %s for %d.", auction.item.short_descr,
                                    ((auction.going == 1) ? "once" : "twice"), auction.bet);
                        } else {
                            sprintf(buf, "%s: going %s (not bet received yet).", auction.item.short_descr,
                                    ((auction.going == 1) ? "once" : "twice"));
                        }
                        talk_auction("{c" + buf + "{x");
                        break;

                    case 3: /* SOLD! */

                        if (auction.bet > 0) {
                            sprintf(buf, "%s sold to %s for %d.",
                                    auction.item.short_descr,
                                    IS_NPC(auction.buyer) ? auction.buyer.short_descr : auction.buyer.name,
                                    auction.bet);
                            sprintf(buf, "{c%s{x", buf);
                            talk_auction(buf.toString());
                            obj_to_char(auction.item, auction.buyer);
                            act("The auctioneer appears before you in a puff of smoke and hands you $p.",
                                    auction.buyer, auction.item, null, TO_CHAR);
                            act("The auctioneer appears before $n, and hands $m $p",
                                    auction.buyer, auction.item, null, TO_ROOM);

                            auction.seller.gold += auction.bet; /* give him the money */

                            auction.item = null; /* reset item */

                        } else /* not sold */ {
                            sprintf(buf, "No bets received for %s - object has been removed.", auction.item.short_descr);
                            sprintf(buf, "{c%s{x", buf);
                            talk_auction(buf.toString());
                            sprintf(buf, "The auctioneer puts the unsold item to his pit.");
                            sprintf(buf, "{c%s{x", buf);
                            talk_auction(buf.toString());
                            extract_obj(auction.item);
                            auction.item = null; /* clear auction */

                        } /* else */

                } /* switch */
            } /* if */
        }
    } /* func */


    static void do_auction(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        int i;
        StringBuilder arg1 = new StringBuilder();
        argument = one_argument(argument, arg1);

        if (IS_NPC(ch))    /* NPC extracted can't auction! */ {
            return;
        }

        if (IS_SET(ch.comm, COMM_NOAUCTION)) {
            if (!str_cmp(arg1.toString(), "on")) {
                send_to_char("Auction channel is now ON.\n", ch);
                ch.comm = REMOVE_BIT(ch.comm, COMM_NOAUCTION);
                return;
            } else {
                send_to_char("Your auction channel is OFF.\n", ch);
                send_to_char("You must first change auction channel ON.\n", ch);
                return;
            }
        }
        TextBuffer buf = new TextBuffer();
        if (arg1.length() == 0) {
            if (auction.item != null) {
                /* show item data here */
                if (auction.bet > 0) {
                    sprintf(buf, "Current bet on this item is %d gold.\n", auction.bet);
                } else {
                    sprintf(buf, "No bets on this item have been received.\n");
                }
                send_to_char("{g" + buf + "{x", ch);
                spell_identify(Skill.gsn_identify, 0, ch, auction.item, 0);
                return;
            } else {

                sprintf(buf, "{rAuction WHAT?{x\n");
                send_to_char(buf, ch);
                return;
            }
        }

        if (!str_cmp(arg1.toString(), "off")) {
            send_to_char("Auction channel is now OFF.\n", ch);
            ch.comm = SET_BIT(ch.comm, COMM_NOAUCTION);
            return;
        }

        if (IS_IMMORTAL(ch) && !str_cmp(arg1.toString(), "stop")) {
            if (auction.item == null) {
                send_to_char("There is no auction going on you can stop.\n", ch);
                return;
            } else /* stop the auction */ {
                buf.sprintf("Sale of %s has been stopped by God. Item confiscated.",
                        auction.item.short_descr);
                sprintf(buf, "{W%s{x", buf);
                talk_auction(buf.toString());
                obj_to_char(auction.item, auction.seller);
                auction.item = null;
                if (auction.buyer != null) /* return money to the buyer */ {
                    auction.buyer.gold += auction.bet;
                    send_to_char("Your money has been returned.\n", auction.buyer);
                }
                return;
            }
        }

        if (!str_cmp(arg1.toString(), "bet")) {
            if (auction.item != null) {
                int newbet;

                if (ch == auction.seller) {
                    send_to_char("You cannot bet on your own selling equipment.\n", ch);
                    return;
                }

                /* make - perhaps - a bet now */
                if (argument.length() == 0) {
                    send_to_char("Bet how much?\n", ch);
                    return;
                }

                newbet = parsebet(auction.bet, argument);
                sprintf(buf, "Bet: %d\n", newbet);

                if (newbet < (auction.bet + 1)) {
                    send_to_char("You must at least bid 1 gold over the current bet.\n", ch);
                    return;
                }

                if (newbet > ch.gold) {
                    send_to_char("You don't have that much money!\n", ch);
                    return;
                }

                /* the actual bet is OK! */

                /* return the gold to the last buyer, if one exists */
                if (auction.buyer != null) {
                    auction.buyer.gold += auction.bet;
                }

                ch.gold -= newbet; /* substract the gold - important :) */
                auction.buyer = ch;
                auction.bet = newbet;
                auction.going = 0;
                auction.pulse = PULSE_AUCTION; /* start the auction over again */

                sprintf(buf, "A bet of %d gold has been received on %s.\n",
                        newbet, auction.item.short_descr);
                sprintf(buf, "{m%s{x", buf);
                talk_auction(buf.toString());
                return;
            } else {
                send_to_char("There isn't anything being auctioned right now.\n", ch);
                return;
            }
        }

        /* finally... */
        obj = get_obj_carry(ch, arg1.toString()); /* does char have the item ? */

        if (obj == null) {
            send_to_char("You aren't carrying that.\n", ch);
            return;
        }

        if (obj.pIndexData.vnum < 100) {
            send_to_char("You cannot auction that item.\n", ch);
            return;
        }

        for (i = 1; i < MAX_CABAL; i++) {
            if (obj.pIndexData.vnum == cabal_table[i].obj_vnum) {
                send_to_char("Gods are furied upon your request.\n", ch);
                return;
            }
        }

        if (auction.item == null) {


            switch (obj.item_type) {
                default:
                    act("{rYou cannot auction $Ts.{x", ch, null, item_type_name(obj), TO_CHAR, POS_SLEEPING);
                    return;

                case ITEM_WEAPON:
                case ITEM_ARMOR:
                case ITEM_STAFF:
                case ITEM_WAND:
                case ITEM_SCROLL:
                    obj_from_char(obj);
                    auction.item = obj;
                    auction.bet = 0;   /* obj.cost / 100 */
                    auction.buyer = null;
                    auction.seller = ch;
                    auction.pulse = PULSE_AUCTION;
                    auction.going = 0;
                    buf.sprintf("A new item has been received: %s.", obj.short_descr);
                    talk_auction("{r" + buf.toString() + "{x");

            } /* switch */
        } else {
            act("Try again later - $p is being auctioned right now!",
                    ch, auction.item, null, TO_CHAR);
        }
    }

}
