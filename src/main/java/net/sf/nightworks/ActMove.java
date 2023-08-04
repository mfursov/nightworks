package net.sf.nightworks;

import java.util.Formatter;
import net.sf.nightworks.util.NotNull;
import net.sf.nightworks.util.TextBuffer;
import static net.sf.nightworks.ActComm.cabal_area_check;
import static net.sf.nightworks.ActComm.do_yell;
import static net.sf.nightworks.ActComm.is_at_cabal_area;
import static net.sf.nightworks.ActHera.find_path;
import static net.sf.nightworks.ActInfo.do_look;
import static net.sf.nightworks.ActSkill.check_improve;
import static net.sf.nightworks.Comm.act;
import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.Const.hometown_table;
import static net.sf.nightworks.Const.str_app;
import static net.sf.nightworks.DB.bug;
import static net.sf.nightworks.DB.capitalize;
import static net.sf.nightworks.DB.dice;
import static net.sf.nightworks.DB.get_room_index;
import static net.sf.nightworks.DB.number_bits;
import static net.sf.nightworks.DB.number_percent;
import static net.sf.nightworks.DB.number_range;
import static net.sf.nightworks.DB.weather_info;
import static net.sf.nightworks.Effects.cold_effect;
import static net.sf.nightworks.Effects.fire_effect;
import static net.sf.nightworks.Effects.shock_effect;
import static net.sf.nightworks.Fight.check_obj_dodge;
import static net.sf.nightworks.Fight.damage;
import static net.sf.nightworks.Fight.is_safe;
import static net.sf.nightworks.Fight.multi_hit;
import static net.sf.nightworks.Fight.one_hit;
import static net.sf.nightworks.Fight.stop_fighting;
import static net.sf.nightworks.Fight.update_pos;
import static net.sf.nightworks.Handler.add_mind;
import static net.sf.nightworks.Handler.affect_check_obj;
import static net.sf.nightworks.Handler.affect_find;
import static net.sf.nightworks.Handler.affect_join;
import static net.sf.nightworks.Handler.affect_strip;
import static net.sf.nightworks.Handler.affect_to_char;
import static net.sf.nightworks.Handler.can_see;
import static net.sf.nightworks.Handler.can_see_obj;
import static net.sf.nightworks.Handler.can_see_room;
import static net.sf.nightworks.Handler.char_from_room;
import static net.sf.nightworks.Handler.char_to_room;
import static net.sf.nightworks.Handler.count_users;
import static net.sf.nightworks.Handler.equip_char;
import static net.sf.nightworks.Handler.find_char;
import static net.sf.nightworks.Handler.get_char_room;
import static net.sf.nightworks.Handler.get_curr_stat;
import static net.sf.nightworks.Handler.get_max_train2;
import static net.sf.nightworks.Handler.get_obj_here;
import static net.sf.nightworks.Handler.get_obj_list;
import static net.sf.nightworks.Handler.get_skill;
import static net.sf.nightworks.Handler.get_trust;
import static net.sf.nightworks.Handler.is_affected;
import static net.sf.nightworks.Handler.is_name;
import static net.sf.nightworks.Handler.obj_from_char;
import static net.sf.nightworks.Handler.obj_to_char;
import static net.sf.nightworks.Handler.obj_to_room;
import static net.sf.nightworks.Handler.path_to_track;
import static net.sf.nightworks.Handler.room_dark;
import static net.sf.nightworks.Handler.room_is_private;
import static net.sf.nightworks.Handler.room_record;
import static net.sf.nightworks.Handler.skill_failure_check;
import static net.sf.nightworks.Magic.saves_spell;
import static net.sf.nightworks.Nightworks.ACT_AGGRESSIVE;
import static net.sf.nightworks.Nightworks.ACT_GAIN;
import static net.sf.nightworks.Nightworks.ACT_NOTRACK;
import static net.sf.nightworks.Nightworks.ACT_PET;
import static net.sf.nightworks.Nightworks.ACT_PRACTICE;
import static net.sf.nightworks.Nightworks.ACT_RIDEABLE;
import static net.sf.nightworks.Nightworks.ACT_TRAIN;
import static net.sf.nightworks.Nightworks.AFFECT_DATA;
import static net.sf.nightworks.Nightworks.AFF_BERSERK;
import static net.sf.nightworks.Nightworks.AFF_CAMOUFLAGE;
import static net.sf.nightworks.Nightworks.AFF_CHARM;
import static net.sf.nightworks.Nightworks.AFF_CORRUPTION;
import static net.sf.nightworks.Nightworks.AFF_CURSE;
import static net.sf.nightworks.Nightworks.AFF_DETECT_SNEAK;
import static net.sf.nightworks.Nightworks.AFF_EARTHFADE;
import static net.sf.nightworks.Nightworks.AFF_FADE;
import static net.sf.nightworks.Nightworks.AFF_FAERIE_FIRE;
import static net.sf.nightworks.Nightworks.AFF_FLYING;
import static net.sf.nightworks.Nightworks.AFF_HASTE;
import static net.sf.nightworks.Nightworks.AFF_HIDE;
import static net.sf.nightworks.Nightworks.AFF_IMP_INVIS;
import static net.sf.nightworks.Nightworks.AFF_INFRARED;
import static net.sf.nightworks.Nightworks.AFF_INVISIBLE;
import static net.sf.nightworks.Nightworks.AFF_PASS_DOOR;
import static net.sf.nightworks.Nightworks.AFF_POISON;
import static net.sf.nightworks.Nightworks.AFF_ROOM_CURSE;
import static net.sf.nightworks.Nightworks.AFF_ROOM_RANDOMIZER;
import static net.sf.nightworks.Nightworks.AFF_SLEEP;
import static net.sf.nightworks.Nightworks.AFF_SLOW;
import static net.sf.nightworks.Nightworks.AFF_SNEAK;
import static net.sf.nightworks.Nightworks.AFF_WEB;
import static net.sf.nightworks.Nightworks.ANGEL;
import static net.sf.nightworks.Nightworks.APPLY_DAMROLL;
import static net.sf.nightworks.Nightworks.APPLY_DEX;
import static net.sf.nightworks.Nightworks.APPLY_HITROLL;
import static net.sf.nightworks.Nightworks.APPLY_NONE;
import static net.sf.nightworks.Nightworks.APPLY_SIZE;
import static net.sf.nightworks.Nightworks.APPLY_STR;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.CONT_CLOSEABLE;
import static net.sf.nightworks.Nightworks.CONT_CLOSED;
import static net.sf.nightworks.Nightworks.CONT_LOCKED;
import static net.sf.nightworks.Nightworks.CONT_PICKPROOF;
import static net.sf.nightworks.Nightworks.DAM_BASH;
import static net.sf.nightworks.Nightworks.DAM_NONE;
import static net.sf.nightworks.Nightworks.DAM_PIERCE;
import static net.sf.nightworks.Nightworks.DAM_POISON;
import static net.sf.nightworks.Nightworks.DIR_DOWN;
import static net.sf.nightworks.Nightworks.DIR_EAST;
import static net.sf.nightworks.Nightworks.DIR_NORTH;
import static net.sf.nightworks.Nightworks.DIR_SOUTH;
import static net.sf.nightworks.Nightworks.DIR_UP;
import static net.sf.nightworks.Nightworks.DIR_WEST;
import static net.sf.nightworks.Nightworks.EXIT_DATA;
import static net.sf.nightworks.Nightworks.EX_CLOSED;
import static net.sf.nightworks.Nightworks.EX_ISDOOR;
import static net.sf.nightworks.Nightworks.EX_LOCKED;
import static net.sf.nightworks.Nightworks.EX_NOCLOSE;
import static net.sf.nightworks.Nightworks.EX_NOFLEE;
import static net.sf.nightworks.Nightworks.EX_NOLOCK;
import static net.sf.nightworks.Nightworks.EX_NOPASS;
import static net.sf.nightworks.Nightworks.EX_PICKPROOF;
import static net.sf.nightworks.Nightworks.FIGHT_DELAY_TIME;
import static net.sf.nightworks.Nightworks.GET_HITROLL;
import static net.sf.nightworks.Nightworks.IMM_NEGATIVE;
import static net.sf.nightworks.Nightworks.IS_AFFECTED;
import static net.sf.nightworks.Nightworks.IS_AWAKE;
import static net.sf.nightworks.Nightworks.IS_BLINK_ON;
import static net.sf.nightworks.Nightworks.IS_DRUNK;
import static net.sf.nightworks.Nightworks.IS_EVIL;
import static net.sf.nightworks.Nightworks.IS_GOOD;
import static net.sf.nightworks.Nightworks.IS_HARA_KIRI;
import static net.sf.nightworks.Nightworks.IS_IMMORTAL;
import static net.sf.nightworks.Nightworks.IS_NPC;
import static net.sf.nightworks.Nightworks.IS_RAFFECTED;
import static net.sf.nightworks.Nightworks.IS_ROOM_AFFECTED;
import static net.sf.nightworks.Nightworks.IS_SET;
import static net.sf.nightworks.Nightworks.IS_TRUSTED;
import static net.sf.nightworks.Nightworks.IS_VAMPIRE;
import static net.sf.nightworks.Nightworks.IS_WATER;
import static net.sf.nightworks.Nightworks.IS_WEAPON_STAT;
import static net.sf.nightworks.Nightworks.ITEM_BOAT;
import static net.sf.nightworks.Nightworks.ITEM_CONTAINER;
import static net.sf.nightworks.Nightworks.ITEM_FURNITURE;
import static net.sf.nightworks.Nightworks.ITEM_PORTAL;
import static net.sf.nightworks.Nightworks.LEVEL_HERO;
import static net.sf.nightworks.Nightworks.MOUNTED;
import static net.sf.nightworks.Nightworks.MPROG_ENTRY;
import static net.sf.nightworks.Nightworks.MPROG_GREET;
import static net.sf.nightworks.Nightworks.OBJ_DATA;
import static net.sf.nightworks.Nightworks.OFF_BASH;
import static net.sf.nightworks.Nightworks.OPROG_ENTRY;
import static net.sf.nightworks.Nightworks.OPROG_GREET;
import static net.sf.nightworks.Nightworks.PERS;
import static net.sf.nightworks.Nightworks.PLR_BLINK_ON;
import static net.sf.nightworks.Nightworks.PLR_CHANGED_AFF;
import static net.sf.nightworks.Nightworks.PLR_HARA_KIRI;
import static net.sf.nightworks.Nightworks.PLR_HOLYLIGHT;
import static net.sf.nightworks.Nightworks.PLR_VAMPIRE;
import static net.sf.nightworks.Nightworks.POS_DEAD;
import static net.sf.nightworks.Nightworks.POS_FIGHTING;
import static net.sf.nightworks.Nightworks.POS_RESTING;
import static net.sf.nightworks.Nightworks.POS_SITTING;
import static net.sf.nightworks.Nightworks.POS_SLEEPING;
import static net.sf.nightworks.Nightworks.POS_STANDING;
import static net.sf.nightworks.Nightworks.POS_STUNNED;
import static net.sf.nightworks.Nightworks.PULSE_VIOLENCE;
import static net.sf.nightworks.Nightworks.REMOVE_BIT;
import static net.sf.nightworks.Nightworks.REST_AT;
import static net.sf.nightworks.Nightworks.REST_IN;
import static net.sf.nightworks.Nightworks.REST_ON;
import static net.sf.nightworks.Nightworks.RIDDEN;
import static net.sf.nightworks.Nightworks.ROOM_HISTORY_DATA;
import static net.sf.nightworks.Nightworks.ROOM_INDEX_DATA;
import static net.sf.nightworks.Nightworks.ROOM_LAW;
import static net.sf.nightworks.Nightworks.ROOM_NO_MOB;
import static net.sf.nightworks.Nightworks.ROOM_NO_RECALL;
import static net.sf.nightworks.Nightworks.ROOM_VNUM_BATTLE;
import static net.sf.nightworks.Nightworks.SECT_AIR;
import static net.sf.nightworks.Nightworks.SECT_CITY;
import static net.sf.nightworks.Nightworks.SECT_DESERT;
import static net.sf.nightworks.Nightworks.SECT_FIELD;
import static net.sf.nightworks.Nightworks.SECT_FOREST;
import static net.sf.nightworks.Nightworks.SECT_HILLS;
import static net.sf.nightworks.Nightworks.SECT_INSIDE;
import static net.sf.nightworks.Nightworks.SECT_MAX;
import static net.sf.nightworks.Nightworks.SECT_MOUNTAIN;
import static net.sf.nightworks.Nightworks.SECT_WATER_NOSWIM;
import static net.sf.nightworks.Nightworks.SET_BIT;
import static net.sf.nightworks.Nightworks.SEX_FEMALE;
import static net.sf.nightworks.Nightworks.SEX_MALE;
import static net.sf.nightworks.Nightworks.SIT_AT;
import static net.sf.nightworks.Nightworks.SIT_IN;
import static net.sf.nightworks.Nightworks.SIT_ON;
import static net.sf.nightworks.Nightworks.SLEEP_AT;
import static net.sf.nightworks.Nightworks.SLEEP_IN;
import static net.sf.nightworks.Nightworks.SLEEP_ON;
import static net.sf.nightworks.Nightworks.STAND_AT;
import static net.sf.nightworks.Nightworks.STAND_IN;
import static net.sf.nightworks.Nightworks.STAND_ON;
import static net.sf.nightworks.Nightworks.STAT_CHA;
import static net.sf.nightworks.Nightworks.STAT_CON;
import static net.sf.nightworks.Nightworks.STAT_DEX;
import static net.sf.nightworks.Nightworks.STAT_INT;
import static net.sf.nightworks.Nightworks.STAT_STR;
import static net.sf.nightworks.Nightworks.STAT_WIS;
import static net.sf.nightworks.Nightworks.SUN_LIGHT;
import static net.sf.nightworks.Nightworks.SUN_RISE;
import static net.sf.nightworks.Nightworks.TARGET_CHAR;
import static net.sf.nightworks.Nightworks.TO_ACT_FLAG;
import static net.sf.nightworks.Nightworks.TO_AFFECTS;
import static net.sf.nightworks.Nightworks.TO_ALL;
import static net.sf.nightworks.Nightworks.TO_CHAR;
import static net.sf.nightworks.Nightworks.TO_IMMUNE;
import static net.sf.nightworks.Nightworks.TO_NOTVICT;
import static net.sf.nightworks.Nightworks.TO_ROOM;
import static net.sf.nightworks.Nightworks.TO_VICT;
import static net.sf.nightworks.Nightworks.UMIN;
import static net.sf.nightworks.Nightworks.WAIT_STATE;
import static net.sf.nightworks.Nightworks.WEAPON_ARROW;
import static net.sf.nightworks.Nightworks.WEAPON_BOW;
import static net.sf.nightworks.Nightworks.WEAPON_FLAMING;
import static net.sf.nightworks.Nightworks.WEAPON_FROST;
import static net.sf.nightworks.Nightworks.WEAPON_POISON;
import static net.sf.nightworks.Nightworks.WEAPON_SHOCKING;
import static net.sf.nightworks.Nightworks.WEAPON_SPEAR;
import static net.sf.nightworks.Nightworks.WEAR_BOTH;
import static net.sf.nightworks.Nightworks.WEAR_LEFT;
import static net.sf.nightworks.Nightworks.WEAR_RIGHT;
import static net.sf.nightworks.Nightworks.WEAR_STUCK_IN;
import static net.sf.nightworks.Nightworks.current_time;
import static net.sf.nightworks.Nightworks.get_carry_weight;
import static net.sf.nightworks.Skill.gsn_arrow;
import static net.sf.nightworks.Skill.gsn_bash;
import static net.sf.nightworks.Skill.gsn_bash_door;
import static net.sf.nightworks.Skill.gsn_blackguard;
import static net.sf.nightworks.Skill.gsn_blink;
import static net.sf.nightworks.Skill.gsn_bow;
import static net.sf.nightworks.Skill.gsn_cabal_recall;
import static net.sf.nightworks.Skill.gsn_camouflage;
import static net.sf.nightworks.Skill.gsn_detect_sneak;
import static net.sf.nightworks.Skill.gsn_doppelganger;
import static net.sf.nightworks.Skill.gsn_earthfade;
import static net.sf.nightworks.Skill.gsn_escape;
import static net.sf.nightworks.Skill.gsn_fade;
import static net.sf.nightworks.Skill.gsn_fly;
import static net.sf.nightworks.Skill.gsn_hide;
import static net.sf.nightworks.Skill.gsn_imp_invis;
import static net.sf.nightworks.Skill.gsn_invis;
import static net.sf.nightworks.Skill.gsn_kick;
import static net.sf.nightworks.Skill.gsn_lay_hands;
import static net.sf.nightworks.Skill.gsn_mass_invis;
import static net.sf.nightworks.Skill.gsn_move_camf;
import static net.sf.nightworks.Skill.gsn_pick_lock;
import static net.sf.nightworks.Skill.gsn_poison;
import static net.sf.nightworks.Skill.gsn_push;
import static net.sf.nightworks.Skill.gsn_quiet_movement;
import static net.sf.nightworks.Skill.gsn_recall;
import static net.sf.nightworks.Skill.gsn_riding;
import static net.sf.nightworks.Skill.gsn_sneak;
import static net.sf.nightworks.Skill.gsn_spear;
import static net.sf.nightworks.Skill.gsn_track;
import static net.sf.nightworks.Skill.gsn_vampire;
import static net.sf.nightworks.Skill.gsn_vampiric_bite;
import static net.sf.nightworks.Skill.gsn_vampiric_touch;
import static net.sf.nightworks.Skill.gsn_vanish;
import static net.sf.nightworks.Skill.gsn_web;
import static net.sf.nightworks.Update.gain_exp;
import static net.sf.nightworks.util.TextUtils.one_argument;
import static net.sf.nightworks.util.TextUtils.str_cmp;

class ActMove {

    static final String[] dir_name = {"north", "east", "south", "west", "up", "down"};

    static final int[] rev_dir = {2, 3, 0, 1, 5, 4};

    static final int[] movement_loss = {1, 2, 2, 3, 4, 6, 4, 1, 6, 10, 6};

    static void move_char(@NotNull CHAR_DATA ch, int door) {
        CHAR_DATA fch;
        CHAR_DATA fch_next;
        ROOM_INDEX_DATA in_room;
        boolean room_has_pc;
        OBJ_DATA obj;

        if (RIDDEN(ch) != null && !IS_NPC(ch.mount)) {
            move_char(ch.mount, door);
            return;
        }

        var mount = MOUNTED(ch);
        if (IS_AFFECTED(ch, AFF_WEB) || (mount != null && IS_AFFECTED(ch.mount, AFF_WEB))) {
            WAIT_STATE(ch, PULSE_VIOLENCE);
            if (number_percent() < str_app[IS_NPC(ch) ? 20 : get_curr_stat(ch, STAT_STR)].tohit * 5) {
                affect_strip(ch, gsn_web);
                ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_WEB);
                send_to_char("When you attempt to leave the room, you break the webs holding you tight.\n", ch);
                act("$n struggles against the webs which hold $m in place, and break it.", ch, null, null, TO_ROOM);
            } else {
                send_to_char("You attempt to leave the room, but the webs hold you tight.\n", ch);
                act("$n struggles vainly against the webs which hold $m in place.", ch, null, null, TO_ROOM);
                return;
            }
        }

        if (door < 0 || door > 5) {
            bug("Do_move: bad door %d.", door);
            return;
        }

        if ((IS_AFFECTED(ch, AFF_HIDE) && !IS_AFFECTED(ch, AFF_SNEAK)) || (IS_AFFECTED(ch, AFF_FADE) && !IS_AFFECTED(ch, AFF_SNEAK))) {
            ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_HIDE);
            ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_FADE);
            send_to_char("You step out of shadows.\n", ch);
            act("$n steps out of shadows.", ch, null, null, TO_ROOM);
        }
        if (IS_AFFECTED(ch, AFF_CAMOUFLAGE)) {
            if (number_percent() < get_skill(ch, gsn_move_camf)) {
                check_improve(ch, gsn_move_camf, true, 5);
            } else {
                ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_CAMOUFLAGE);
                send_to_char("You step out from your cover.\n", ch);
                act("$n steps out from $m's cover.", ch, null, null, TO_ROOM);
                check_improve(ch, gsn_move_camf, false, 5);
            }
        }
        if (IS_AFFECTED(ch, AFF_EARTHFADE)) {
            send_to_char("You fade to your neutral form.\n", ch);
            act("Earth forms $n in front of you.", ch, null, null, TO_ROOM);
            affect_strip(ch, gsn_earthfade);
            WAIT_STATE(ch, (PULSE_VIOLENCE / 2));
            ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_EARTHFADE);
        }

        in_room = ch.in_room;
        var exit = in_room.exit[door];
        var to_room = exit == null ? null : exit.to_room;
        if (to_room == null || !can_see_room(ch, exit.to_room)) {
            send_to_char("Alas, you cannot go that way.\n", ch);
            return;
        }

        if (IS_ROOM_AFFECTED(in_room, AFF_ROOM_RANDOMIZER)) {
            for (var i = 0; i < 1000; i++) {
                var d0 = number_range(0, 5);
                var newExit = in_room.exit[d0];
                if (newExit == null || newExit.to_room == null || !can_see_room(ch, newExit.to_room)) {
                    continue;
                }
                exit = newExit;
                to_room = exit.to_room;
                door = d0;
                break;
            }
        }

        if (IS_SET(exit.exit_info, EX_CLOSED) && (!IS_AFFECTED(ch, AFF_PASS_DOOR) || IS_SET(exit.exit_info, EX_NOPASS)) && !IS_TRUSTED(ch, ANGEL)) {
            if (IS_AFFECTED(ch, AFF_PASS_DOOR) && IS_SET(exit.exit_info, EX_NOPASS)) {
                act("You failed to pass through the $d.", ch, null, exit.keyword, TO_CHAR);
                act("$n tries to pass through the $d, but $e fails", ch, null, exit.keyword, TO_ROOM);
            } else {
                act("The $d is closed.", ch, null, exit.keyword, TO_CHAR);
            }
            return;
        }

        if (IS_AFFECTED(ch, AFF_CHARM) && ch.master != null && in_room == ch.master.in_room) {
            send_to_char("What?  And leave your beloved master?\n", ch);
            return;
        }

        /*    if ( !is_room_owner(ch,to_room) && room_is_private( to_room ) )   */
        if (room_is_private(to_room)) {
            send_to_char("That room is private right now.\n", ch);
            return;
        }

        if (mount != null) {
            if (mount.position < POS_FIGHTING) {
                send_to_char("Your mount must be standing.\n", ch);
                return;
            }
            if (!mount_success(ch, mount, false)) {
                send_to_char("Your mount subbornly refuses to go that way.\n", ch);
                return;
            }
        }

        if (!IS_NPC(ch)) {
            int move;

            for (var c : Clazz.getClasses()) {
                for (int gvnum : c.guildVnums) {
                    if (to_room.vnum == gvnum && !IS_IMMORTAL(ch)) {
                        if (c != ch.clazz) {
                            send_to_char("You aren't allowed in there.\n", ch);
                            return;
                        }
                        if (ch.last_fight_time != -1 && current_time - ch.last_fight_time < FIGHT_DELAY_TIME) {
                            send_to_char("You feel too bloody to go in there now.\n", ch);
                            return;
                        }
                    }
                }
            }

            if (in_room.sector_type == SECT_AIR || to_room.sector_type == SECT_AIR) {
                if (mount != null) {
                    if (!IS_AFFECTED(mount, AFF_FLYING)) {
                        send_to_char("Your mount can't fly.\n", ch);
                        return;
                    }
                } else if (!IS_AFFECTED(ch, AFF_FLYING) && !IS_IMMORTAL(ch)) {
                    send_to_char("You can't fly.\n", ch);
                    return;
                }
            }

            if ((in_room.sector_type == SECT_WATER_NOSWIM || to_room.sector_type == SECT_WATER_NOSWIM) && (mount != null && !IS_AFFECTED(mount, AFF_FLYING))) {
                send_to_char("You can't take your mount there.\n", ch);
                return;
            }

            if ((in_room.sector_type == SECT_WATER_NOSWIM || to_room.sector_type == SECT_WATER_NOSWIM) && (mount == null && !IS_AFFECTED(ch, AFF_FLYING))) {
                /*
                 * Look for a boat.
                 */
                var found = IS_IMMORTAL(ch);
                for (obj = ch.carrying; obj != null; obj = obj.next_content) {
                    if (obj.item_type == ITEM_BOAT) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    send_to_char("You need a boat to go there.\n", ch);
                    return;
                }
            }

            move = movement_loss[UMIN(SECT_MAX - 1, in_room.sector_type)] + movement_loss[UMIN(SECT_MAX - 1, to_room.sector_type)];
            move /= 2;  /* i.e. the average */

            /* conditional effects */
            if (IS_AFFECTED(ch, AFF_FLYING) || IS_AFFECTED(ch, AFF_HASTE)) {
                move /= 2;
            }

            if (IS_AFFECTED(ch, AFF_SLOW)) {
                move *= 2;
            }

            if (mount == null && ch.move < move) {
                send_to_char("You are too exhausted.\n", ch);
                return;
            }

            if (mount == null && (ch.in_room.sector_type == SECT_DESERT || IS_WATER(ch.in_room))) {
                WAIT_STATE(ch, 2);
            } else {
                WAIT_STATE(ch, 1);
            }

            if (mount == null) {
                ch.move -= move;
            }
        }

        if (!IS_AFFECTED(ch, AFF_SNEAK) && !IS_AFFECTED(ch, AFF_CAMOUFLAGE) && ch.invis_level < LEVEL_HERO) {
            var buf = new Formatter();
            if (!IS_NPC(ch) && ch.in_room.sector_type != SECT_INSIDE &&
                    ch.in_room.sector_type != SECT_CITY &&
                    number_percent() < get_skill(ch, gsn_quiet_movement)) {
                if (mount != null) {
                    buf.format("$n leaves, riding on %s.", mount.short_descr);
                } else {
                    buf.format("$n leaves.");
                }
                check_improve(ch, gsn_quiet_movement, true, 1);
            } else {
                if (mount != null) {
                    buf.format("$n leaves $T, riding on %s.", mount.short_descr);
                } else {
                    buf.format("$n leaves $T.");
                }
            }
            act(buf.toString(), ch, null, dir_name[door], TO_ROOM);
        }

        if (IS_AFFECTED(ch, AFF_CAMOUFLAGE)
                && to_room.sector_type != SECT_FIELD
                && to_room.sector_type != SECT_FOREST
                && to_room.sector_type != SECT_MOUNTAIN
                && to_room.sector_type != SECT_HILLS) {
            ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_CAMOUFLAGE);
            send_to_char("You step out from your cover.\n", ch);
            act("$n steps out from $m's cover.", ch, null, null, TO_ROOM);
        }

        if ((IS_AFFECTED(ch, AFF_HIDE)) && (to_room.sector_type == SECT_FOREST || to_room.sector_type == SECT_FIELD)) {
            ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_HIDE);
            send_to_char("You step out of shadows.\n", ch);
            act("$n steps out of shadows.", ch, null, null, TO_ROOM);
        }

        char_from_room(ch);
        char_to_room(ch, to_room);

        if (ch.in_room != to_room) {
            bug("Is char dead!");
            return;
        }

        /* room record for tracking */
        if (!IS_NPC(ch) && ch.in_room != null) {
            room_record(ch.name, in_room, door);
        }


        if (!IS_AFFECTED(ch, AFF_SNEAK) && ch.invis_level < LEVEL_HERO) {
            if (mount != null) {
                act("$n has arrived, riding $N.", ch, null, mount, TO_ROOM);
            } else {
                act("$n has arrived.", ch, null, null, TO_ROOM);
            }
        }

        do_look(ch, "auto");

        if (mount != null) {
            char_from_room(mount);
            char_to_room(mount, to_room);
            ch.riding = true;
            mount.riding = true;
        }

        if (in_room == to_room) /* no circular follows */ {
            return;
        }


        for (fch = to_room.people, room_has_pc = false; fch != null; fch = fch_next) {
            fch_next = fch.next_in_room;
            if (!IS_NPC(fch)) {
                room_has_pc = true;
            }
        }

        for (fch = to_room.people; fch != null; fch = fch_next) {
            fch_next = fch.next_in_room;

            /* greet progs for items carried by people in room */
            for (obj = fch.carrying; room_has_pc && obj != null; obj = obj.next_content) {
                if (IS_SET(obj.progtypes, OPROG_GREET)) {
                    obj.pIndexData.oprogs.greet_prog.run(obj, ch);
                }
            }

            /* greet programs for npcs  */
            if (room_has_pc && IS_SET(fch.progtypes, MPROG_GREET)) {
                fch.pIndexData.mprogs.greet_prog.run(fch, ch);
            }
        }

        /* entry programs for items */
        for (obj = ch.carrying; room_has_pc && obj != null; obj = obj.next_content) {
            if (IS_SET(obj.progtypes, OPROG_ENTRY)) {
                obj.pIndexData.oprogs.entry_prog.run(obj);
            }
        }


        for (fch = in_room.people; fch != null; fch = fch_next) {
            fch_next = fch.next_in_room;

            if (fch.master == ch && IS_AFFECTED(fch, AFF_CHARM) && fch.position < POS_STANDING) {
                do_stand(fch, "");
            }

            if (fch.master == ch && fch.position == POS_STANDING
                    && can_see_room(fch, to_room)) {

                if (IS_SET(ch.in_room.room_flags, ROOM_LAW)
                        && (IS_NPC(fch) && IS_SET(fch.act, ACT_AGGRESSIVE))) {
                    act("You can't bring $N into the city.",
                            ch, null, fch, TO_CHAR);
                    act("You aren't allowed in the city.",
                            fch, null, null, TO_CHAR);
                    continue;
                }

                act("You follow $N.", fch, null, ch, TO_CHAR);
                move_char(fch, door);
            }
        }

        for (obj = ch.in_room.contents; room_has_pc && obj != null;
             obj = obj.next_content) {
            if (IS_SET(obj.progtypes, OPROG_GREET)) {
                obj.pIndexData.oprogs.greet_prog.run(obj, ch);
            }
        }

        if (IS_SET(ch.progtypes, MPROG_ENTRY)) {
            ch.pIndexData.mprogs.entry_prog.run(ch);
        }
    }


    static void do_north(@NotNull CHAR_DATA ch) {
        move_char(ch, DIR_NORTH);
    }


    static void do_east(@NotNull CHAR_DATA ch) {
        move_char(ch, DIR_EAST);
    }


    static void do_south(@NotNull CHAR_DATA ch) {
        move_char(ch, DIR_SOUTH);
    }


    static void do_west(@NotNull CHAR_DATA ch) {
        move_char(ch, DIR_WEST);
    }


    static void do_up(@NotNull CHAR_DATA ch) {
        move_char(ch, DIR_UP);
    }


    static void do_down(@NotNull CHAR_DATA ch) {
        move_char(ch, DIR_DOWN);
    }

    static void do_run(@NotNull CHAR_DATA ch, String argument) {
        var arg1 = new StringBuilder();
        var arg2 = new StringBuilder();
        argument = one_argument(argument, arg1);
        one_argument(argument, arg2);

        if (arg1.isEmpty() || arg2.isEmpty()) {
            send_to_char("run <direction> <count>\n", ch);
            return;
        }
        if (find_exit(ch, arg1.toString()) < -1) {
            return;
        }
        if (Integer.parseInt(arg2.toString()) < 1) {
            send_to_char("Count should be larger than 0.\n", ch);
        }
    }


    static int find_exit(@NotNull CHAR_DATA ch, String arg) {
        int door;

        if (!str_cmp(arg, "n") || !str_cmp(arg, "north")) {
            door = 0;
        } else if (!str_cmp(arg, "e") || !str_cmp(arg, "east")) {
            door = 1;
        } else if (!str_cmp(arg, "s") || !str_cmp(arg, "south")) {
            door = 2;
        } else if (!str_cmp(arg, "w") || !str_cmp(arg, "west")) {
            door = 3;
        } else if (!str_cmp(arg, "u") || !str_cmp(arg, "up")) {
            door = 4;
        } else if (!str_cmp(arg, "d") || !str_cmp(arg, "down")) {
            door = 5;
        } else {
            act("I see no exit $T here.", ch, null, arg, TO_CHAR);
            return -1;
        }

        return door;
    }


    static int find_door(@NotNull CHAR_DATA ch, String arg) {
        EXIT_DATA pexit;
        int door;

        if (!str_cmp(arg, "n") || !str_cmp(arg, "north")) {
            door = 0;
        } else if (!str_cmp(arg, "e") || !str_cmp(arg, "east")) {
            door = 1;
        } else if (!str_cmp(arg, "s") || !str_cmp(arg, "south")) {
            door = 2;
        } else if (!str_cmp(arg, "w") || !str_cmp(arg, "west")) {
            door = 3;
        } else if (!str_cmp(arg, "u") || !str_cmp(arg, "up")) {
            door = 4;
        } else if (!str_cmp(arg, "d") || !str_cmp(arg, "down")) {
            door = 5;
        } else {
            for (door = 0; door <= 5; door++) {
                if ((pexit = ch.in_room.exit[door]) != null
                        && IS_SET(pexit.exit_info, EX_ISDOOR)
                        && pexit.keyword != null
                        && is_name(arg, pexit.keyword)) {
                    return door;
                }
            }
            act("I see no $T here.", ch, null, arg, TO_CHAR);
            return -1;
        }

        if ((pexit = ch.in_room.exit[door]) == null) {
            act("I see no door $T here.", ch, null, arg, TO_CHAR);
            return -1;
        }

        if (!IS_SET(pexit.exit_info, EX_ISDOOR)) {
            send_to_char("You can't do that.\n", ch);
            return -1;
        }

        return door;
    }

    /* scan.c */

    static final String[] distance = {"right here.", "nearby to the %s.", "not far %s.", "off in the distance %s."};

    static void do_scan2(@NotNull CHAR_DATA ch) {
        act("$n looks all around.", ch, null, null, TO_ROOM);
        send_to_char("Looking around you see:\n", ch);
        scan_list(ch.in_room, ch, 0, -1);
        for (var door = 0; door < 6; door++) {
            var pExit = ch.in_room.exit[door];
            if (pExit == null || pExit.to_room == null || IS_SET(pExit.exit_info, EX_CLOSED)) {
                continue;
            }
            scan_list(pExit.to_room, ch, 1, door);
        }
    }

    static void scan_list(ROOM_INDEX_DATA scan_room, CHAR_DATA ch, int depth, int door) {
        if (scan_room == null) {
            return;
        }
        for (var rch = scan_room.people; rch != null; rch = rch.next_in_room) {
            if (rch == ch) {
                continue;
            }
            if (!IS_NPC(rch) && rch.invis_level > get_trust(ch)) {
                continue;
            }
            if (can_see(ch, rch)) {
                scan_char(rch, ch, depth, door);
            }
        }
    }

    static void scan_char(CHAR_DATA victim, CHAR_DATA ch, int depth, int door) {
        var buf = new TextBuffer();
        buf.append((is_affected(victim, gsn_doppelganger) && !IS_SET(ch.act, PLR_HOLYLIGHT))
                ? PERS(victim.doppel, ch) : PERS(victim, ch));
        buf.append(", ");
        if (depth == 0) {
            buf.sprintf(distance[depth]);
        } else {
            buf.sprintf(distance[depth], dir_name[door]);
        }
        buf.append("\n");
        send_to_char(buf, ch);
    }

    static void do_open(@NotNull CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        int door;

        var arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.isEmpty()) {
            send_to_char("Open what?\n", ch);
            return;
        }


        if ((obj = get_obj_here(ch, arg.toString())) != null) {
            /* open portal */
            if (obj.item_type == ITEM_PORTAL) {
                if (!IS_SET(obj.value[1], EX_ISDOOR)) {
                    send_to_char("You can't do that.\n", ch);
                    return;
                }

                if (!IS_SET(obj.value[1], EX_CLOSED)) {
                    send_to_char("It's already open.\n", ch);
                    return;
                }

                if (IS_SET(obj.value[1], EX_LOCKED)) {
                    send_to_char("It's locked.\n", ch);
                    return;
                }

                obj.value[1] = REMOVE_BIT(obj.value[1], EX_CLOSED);
                act("You open $p.", ch, obj, null, TO_CHAR);
                act("$n opens $p.", ch, obj, null, TO_ROOM);
                return;
            }

            /* 'open object' */
            if (obj.item_type != ITEM_CONTAINER) {
                send_to_char("That's not a container.\n", ch);
                return;
            }
            if (!IS_SET(obj.value[1], CONT_CLOSED)) {
                send_to_char("It's already open.\n", ch);
                return;
            }
            if (!IS_SET(obj.value[1], CONT_CLOSEABLE)) {
                send_to_char("You can't do that.\n", ch);
                return;
            }
            if (IS_SET(obj.value[1], CONT_LOCKED)) {
                send_to_char("It's locked.\n", ch);
                return;
            }

            obj.value[1] = REMOVE_BIT(obj.value[1], CONT_CLOSED);
            act("You open $p.", ch, obj, null, TO_CHAR);
            act("$n opens $p.", ch, obj, null, TO_ROOM);
            return;
        }
        if ((door = find_door(ch, arg.toString())) >= 0) {
            /* 'open door' */
            ROOM_INDEX_DATA to_room;
            EXIT_DATA pexit;
            EXIT_DATA pexit_rev;

            pexit = ch.in_room.exit[door];
            if (!IS_SET(pexit.exit_info, EX_CLOSED)) {
                send_to_char("It's already open.\n", ch);
                return;
            }
            if (IS_SET(pexit.exit_info, EX_LOCKED)) {
                send_to_char("It's locked.\n", ch);
                return;
            }

            pexit.exit_info = REMOVE_BIT(pexit.exit_info, EX_CLOSED);
            act("$n opens the $d.", ch, null, pexit.keyword, TO_ROOM);
            send_to_char("Ok.\n", ch);

            /* open the other side */
            if ((to_room = pexit.to_room) != null
                    && (pexit_rev = to_room.exit[rev_dir[door]]) != null
                    && pexit_rev.to_room == ch.in_room) {
                CHAR_DATA rch;

                pexit_rev.exit_info = REMOVE_BIT(pexit_rev.exit_info, EX_CLOSED);
                for (rch = to_room.people; rch != null; rch = rch.next_in_room) {
                    act("The $d opens.", rch, null, pexit_rev.keyword, TO_CHAR);
                }
            }
        }
    }


    static void do_close(@NotNull CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        int door;
        var arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.isEmpty()) {
            send_to_char("Close what?\n", ch);
            return;
        }


        if ((obj = get_obj_here(ch, arg.toString())) != null) {
            /* portal stuff */
            if (obj.item_type == ITEM_PORTAL) {

                if (!IS_SET(obj.value[1], EX_ISDOOR)
                        || IS_SET(obj.value[1], EX_NOCLOSE)) {
                    send_to_char("You can't do that.\n", ch);
                    return;
                }

                if (IS_SET(obj.value[1], EX_CLOSED)) {
                    send_to_char("It's already closed.\n", ch);
                    return;
                }

                obj.value[1] = SET_BIT(obj.value[1], EX_CLOSED);
                act("You close $p.", ch, obj, null, TO_CHAR);
                act("$n closes $p.", ch, obj, null, TO_ROOM);
                return;
            }

            /* 'close object' */
            if (obj.item_type != ITEM_CONTAINER) {
                send_to_char("That's not a container.\n", ch);
                return;
            }
            if (IS_SET(obj.value[1], CONT_CLOSED)) {
                send_to_char("It's already closed.\n", ch);
                return;
            }
            if (!IS_SET(obj.value[1], CONT_CLOSEABLE)) {
                send_to_char("You can't do that.\n", ch);
                return;
            }

            obj.value[1] = SET_BIT(obj.value[1], CONT_CLOSED);
            act("You close $p.", ch, obj, null, TO_CHAR);
            act("$n closes $p.", ch, obj, null, TO_ROOM);
            return;
        }

        if ((door = find_door(ch, arg.toString())) >= 0) {
            /* 'close door' */
            ROOM_INDEX_DATA to_room;
            EXIT_DATA pexit;
            EXIT_DATA pexit_rev;

            pexit = ch.in_room.exit[door];
            if (IS_SET(pexit.exit_info, EX_CLOSED)) {
                send_to_char("It's already closed.\n", ch);
                return;
            }

            pexit.exit_info = SET_BIT(pexit.exit_info, EX_CLOSED);
            act("$n closes the $d.", ch, null, pexit.keyword, TO_ROOM);
            send_to_char("Ok.\n", ch);

            /* close the other side */
            if ((to_room = pexit.to_room) != null
                    && (pexit_rev = to_room.exit[rev_dir[door]]) != null
                    && pexit_rev.to_room == ch.in_room) {
                CHAR_DATA rch;

                pexit_rev.exit_info = SET_BIT(pexit_rev.exit_info, EX_CLOSED);
                for (rch = to_room.people; rch != null; rch = rch.next_in_room) {
                    act("The $d closes.", rch, null, pexit_rev.keyword, TO_CHAR);
                }
            }
        }
    }

    /*
     * Added can_see check. Kio.
     */

    static boolean has_key(@NotNull CHAR_DATA ch, int key) {
        OBJ_DATA obj;

        for (obj = ch.carrying; obj != null; obj = obj.next_content) {
            if (obj.pIndexData.vnum == key) {
                if (can_see_obj(ch, obj)) {
                    return true;
                }
            }
        }

        return false;
    }

    static boolean has_key_ground(@NotNull CHAR_DATA ch, int key) {
        OBJ_DATA obj;

        for (obj = ch.in_room.contents; obj != null; obj = obj.next_content) {
            if (obj.pIndexData.vnum == key) {
                if (can_see_obj(ch, obj)) {
                    return true;
                }
            }
        }

        return false;
    }


    static void do_lock(@NotNull CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        int door;
        CHAR_DATA rch;

        var arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.isEmpty()) {
            send_to_char("Lock what?\n", ch);
            return;
        }


        if ((obj = get_obj_here(ch, arg.toString())) != null) {
            /* portal stuff */
            if (obj.item_type == ITEM_PORTAL) {
                if (!IS_SET(obj.value[1], EX_ISDOOR) || IS_SET(obj.value[1], EX_NOCLOSE)) {
                    send_to_char("You can't do that.\n", ch);
                    return;
                }
                if (!IS_SET(obj.value[1], EX_CLOSED)) {
                    send_to_char("It's not closed.\n", ch);
                    return;
                }

                if (obj.value[4] < 0 || IS_SET(obj.value[1], EX_NOLOCK)) {
                    send_to_char("It can't be locked.\n", ch);
                    return;
                }

                if (!has_key(ch, obj.value[4])) {
                    send_to_char("You lack the key.\n", ch);
                    return;
                }

                if (IS_SET(obj.value[1], EX_LOCKED)) {
                    send_to_char("It's already locked.\n", ch);
                    return;
                }

                obj.value[1] = SET_BIT(obj.value[1], EX_LOCKED);
                act("You lock $p.", ch, obj, null, TO_CHAR);
                act("$n locks $p.", ch, obj, null, TO_ROOM);
                return;
            }

            /* 'lock object' */
            if (obj.item_type != ITEM_CONTAINER) {
                send_to_char("That's not a container.\n", ch);
                return;
            }
            if (!IS_SET(obj.value[1], CONT_CLOSED)) {
                send_to_char("It's not closed.\n", ch);
                return;
            }
            if (obj.value[2] < 0) {
                send_to_char("It can't be locked.\n", ch);
                return;
            }
            if (!has_key(ch, obj.value[2])) {
                send_to_char("You lack the key.\n", ch);
                return;
            }
            if (IS_SET(obj.value[1], CONT_LOCKED)) {
                send_to_char("It's already locked.\n", ch);
                return;
            }

            obj.value[1] = SET_BIT(obj.value[1], CONT_LOCKED);
            act("You lock $p.", ch, obj, null, TO_CHAR);
            act("$n locks $p.", ch, obj, null, TO_ROOM);
            return;
        }

        if ((door = find_door(ch, arg.toString())) >= 0) {
            /* 'lock door' */
            ROOM_INDEX_DATA to_room;
            EXIT_DATA pexit;
            EXIT_DATA pexit_rev;

            pexit = ch.in_room.exit[door];
            if (!IS_SET(pexit.exit_info, EX_CLOSED)) {
                send_to_char("It's not closed.\n", ch);
                return;
            }
            if (pexit.key < 0) {
                send_to_char("It can't be locked.\n", ch);
                return;
            }
            if (!has_key(ch, pexit.key) && !has_key_ground(ch, pexit.key)) {
                send_to_char("You lack the key.\n", ch);
                return;
            }
            if (IS_SET(pexit.exit_info, EX_LOCKED)) {
                send_to_char("It's already locked.\n", ch);
                return;
            }

            pexit.exit_info = SET_BIT(pexit.exit_info, EX_LOCKED);
            send_to_char("*Click*\n", ch);
            act("$n locks the $d.", ch, null, pexit.keyword, TO_ROOM);

            /* lock the other side */
            if ((to_room = pexit.to_room) != null
                    && (pexit_rev = to_room.exit[rev_dir[door]]) != null
                    && pexit_rev.to_room == ch.in_room) {
                pexit_rev.exit_info = SET_BIT(pexit_rev.exit_info, EX_LOCKED);
                for (rch = to_room.people; rch != null; rch = rch.next_in_room) {
                    act("The $d clicks.", rch, null, pexit_rev.keyword, TO_CHAR);
                }
            }
        }
    }


    static void do_unlock(@NotNull CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        int door;
        CHAR_DATA rch;
        var arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.isEmpty()) {
            send_to_char("Unlock what?\n", ch);
            return;
        }


        if ((obj = get_obj_here(ch, arg.toString())) != null) {
            /* portal stuff */
            if (obj.item_type == ITEM_PORTAL) {
                if (IS_SET(obj.value[1], EX_ISDOOR)) {
                    send_to_char("You can't do that.\n", ch);
                    return;
                }

                if (!IS_SET(obj.value[1], EX_CLOSED)) {
                    send_to_char("It's not closed.\n", ch);
                    return;
                }

                if (obj.value[4] < 0) {
                    send_to_char("It can't be unlocked.\n", ch);
                    return;
                }

                if (!has_key(ch, obj.value[4])) {
                    send_to_char("You lack the key.\n", ch);
                    return;
                }

                if (!IS_SET(obj.value[1], EX_LOCKED)) {
                    send_to_char("It's already unlocked.\n", ch);
                    return;
                }

                obj.value[1] = REMOVE_BIT(obj.value[1], EX_LOCKED);
                act("You unlock $p.", ch, obj, null, TO_CHAR);
                act("$n unlocks $p.", ch, obj, null, TO_ROOM);
                return;
            }

            /* 'unlock object' */
            if (obj.item_type != ITEM_CONTAINER) {
                send_to_char("That's not a container.\n", ch);
                return;
            }
            if (!IS_SET(obj.value[1], CONT_CLOSED)) {
                send_to_char("It's not closed.\n", ch);
                return;
            }
            if (obj.value[2] < 0) {
                send_to_char("It can't be unlocked.\n", ch);
                return;
            }
            if (!has_key(ch, obj.value[2])) {
                send_to_char("You lack the key.\n", ch);
                return;
            }
            if (!IS_SET(obj.value[1], CONT_LOCKED)) {
                send_to_char("It's already unlocked.\n", ch);
                return;
            }

            obj.value[1] = REMOVE_BIT(obj.value[1], CONT_LOCKED);
            act("You unlock $p.", ch, obj, null, TO_CHAR);
            act("$n unlocks $p.", ch, obj, null, TO_ROOM);
            return;
        }

        if ((door = find_door(ch, arg.toString())) >= 0) {
            /* 'unlock door' */
            ROOM_INDEX_DATA to_room;
            EXIT_DATA pexit;
            EXIT_DATA pexit_rev;

            pexit = ch.in_room.exit[door];
            if (!IS_SET(pexit.exit_info, EX_CLOSED)) {
                send_to_char("It's not closed.\n", ch);
                return;
            }
            if (pexit.key < 0) {
                send_to_char("It can't be unlocked.\n", ch);
                return;
            }
            if (!has_key(ch, pexit.key) &&
                    !has_key_ground(ch, pexit.key)) {
                send_to_char("You lack the key.\n", ch);
                return;
            }
            if (!IS_SET(pexit.exit_info, EX_LOCKED)) {
                send_to_char("It's already unlocked.\n", ch);
                return;
            }

            pexit.exit_info = REMOVE_BIT(pexit.exit_info, EX_LOCKED);
            send_to_char("*Click*\n", ch);
            act("$n unlocks the $d.", ch, null, pexit.keyword, TO_ROOM);

            /* unlock the other side */
            if ((to_room = pexit.to_room) != null
                    && (pexit_rev = to_room.exit[rev_dir[door]]) != null
                    && pexit_rev.to_room == ch.in_room) {
                pexit_rev.exit_info = REMOVE_BIT(pexit_rev.exit_info, EX_LOCKED);
                for (rch = to_room.people; rch != null; rch = rch.next_in_room) {
                    act("The $d clicks.", rch, null, pexit_rev.keyword, TO_CHAR);
                }
            }
        }
    }


    static void do_pick(@NotNull CHAR_DATA ch, String argument) {
        CHAR_DATA gch;
        OBJ_DATA obj;
        int door;

        var arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.isEmpty()) {
            send_to_char("Pick what?\n", ch);
            return;
        }

        if (MOUNTED(ch) != null) {
            send_to_char("You can't pick while mounted.\n", ch);
            return;
        }

        WAIT_STATE(ch, gsn_pick_lock.beats);

        /* look for guards */
        for (gch = ch.in_room.people; gch != null; gch = gch.next_in_room) {
            if (IS_NPC(gch) && IS_AWAKE(gch) && ch.level + 5 < gch.level) {
                act("$N is standing too close to the lock.",
                        ch, null, gch, TO_CHAR);
                return;
            }
        }

        if (!IS_NPC(ch) && number_percent() > get_skill(ch, gsn_pick_lock)) {
            send_to_char("You failed.\n", ch);
            check_improve(ch, gsn_pick_lock, false, 2);
            return;
        }

        if ((obj = get_obj_here(ch, arg.toString())) != null) {
            /* portal stuff */
            if (obj.item_type == ITEM_PORTAL) {
                if (!IS_SET(obj.value[1], EX_ISDOOR)) {
                    send_to_char("You can't do that.\n", ch);
                    return;
                }

                if (!IS_SET(obj.value[1], EX_CLOSED)) {
                    send_to_char("It's not closed.\n", ch);
                    return;
                }

                if (obj.value[4] < 0) {
                    send_to_char("It can't be unlocked.\n", ch);
                    return;
                }

                if (IS_SET(obj.value[1], EX_PICKPROOF)) {
                    send_to_char("You failed.\n", ch);
                    return;
                }

                obj.value[1] = REMOVE_BIT(obj.value[1], EX_LOCKED);
                act("You pick the lock on $p.", ch, obj, null, TO_CHAR);
                act("$n picks the lock on $p.", ch, obj, null, TO_ROOM);
                check_improve(ch, gsn_pick_lock, true, 2);
                return;
            }

            /* 'pick object' */
            if (obj.item_type != ITEM_CONTAINER) {
                send_to_char("That's not a container.\n", ch);
                return;
            }
            if (!IS_SET(obj.value[1], CONT_CLOSED)) {
                send_to_char("It's not closed.\n", ch);
                return;
            }
            if (obj.value[2] < 0) {
                send_to_char("It can't be unlocked.\n", ch);
                return;
            }
            if (!IS_SET(obj.value[1], CONT_LOCKED)) {
                send_to_char("It's already unlocked.\n", ch);
                return;
            }
            if (IS_SET(obj.value[1], CONT_PICKPROOF)) {
                send_to_char("You failed.\n", ch);
                return;
            }

            obj.value[1] = REMOVE_BIT(obj.value[1], CONT_LOCKED);
            act("You pick the lock on $p.", ch, obj, null, TO_CHAR);
            act("$n picks the lock on $p.", ch, obj, null, TO_ROOM);
            check_improve(ch, gsn_pick_lock, true, 2);
            return;
        }

        if ((door = find_door(ch, arg.toString())) >= 0) {
            /* 'pick door' */
            ROOM_INDEX_DATA to_room;
            EXIT_DATA pexit;
            EXIT_DATA pexit_rev;

            pexit = ch.in_room.exit[door];
            if (!IS_SET(pexit.exit_info, EX_CLOSED) && !IS_IMMORTAL(ch)) {
                send_to_char("It's not closed.\n", ch);
                return;
            }
            if (pexit.key < 0 && !IS_IMMORTAL(ch)) {
                send_to_char("It can't be picked.\n", ch);
                return;
            }
            if (!IS_SET(pexit.exit_info, EX_LOCKED)) {
                send_to_char("It's already unlocked.\n", ch);
                return;
            }
            if (IS_SET(pexit.exit_info, EX_PICKPROOF) && !IS_IMMORTAL(ch)) {
                send_to_char("You failed.\n", ch);
                return;
            }

            pexit.exit_info = REMOVE_BIT(pexit.exit_info, EX_LOCKED);
            send_to_char("*Click*\n", ch);
            act("$n picks the $d.", ch, null, pexit.keyword, TO_ROOM);
            check_improve(ch, gsn_pick_lock, true, 2);

            /* pick the other side */
            if ((to_room = pexit.to_room) != null
                    && (pexit_rev = to_room.exit[rev_dir[door]]) != null
                    && pexit_rev.to_room == ch.in_room) {
                pexit_rev.exit_info = REMOVE_BIT(pexit_rev.exit_info, EX_LOCKED);
            }
        }
    }


    static void do_stand(@NotNull CHAR_DATA ch, String argument) {
        OBJ_DATA obj = null;

        if (!argument.isEmpty()) {
            if (ch.position == POS_FIGHTING) {
                send_to_char("Maybe you should finish fighting first?\n", ch);
                return;
            }
            obj = get_obj_list(ch, argument, ch.in_room.contents);
            if (obj == null) {
                send_to_char("You don't see that here.\n", ch);
                return;
            }
            if (obj.item_type != ITEM_FURNITURE
                    || (!IS_SET(obj.value[2], STAND_AT)
                    && !IS_SET(obj.value[2], STAND_ON)
                    && !IS_SET(obj.value[2], STAND_IN))) {
                send_to_char("You can't seem to find a place to stand.\n", ch);
                return;
            }
            if (ch.on != obj && count_users(obj) >= obj.value[0]) {
                act("There's no room to stand on $p.", ch, obj, null, TO_ROOM, POS_DEAD);
                return;
            }
        }
        switch (ch.position) {
            case POS_SLEEPING -> {
                if (IS_AFFECTED(ch, AFF_SLEEP)) {
                    send_to_char("You can't wake up!\n", ch);
                    return;
                }
                if (obj == null) {
                    send_to_char("You wake and stand up.\n", ch);
                    act("$n wakes and stands up.", ch, null, null, TO_ROOM);
                    ch.on = null;
                } else if (IS_SET(obj.value[2], STAND_AT)) {
                    act("You wake and stand at $p.", ch, obj, null, TO_CHAR, POS_DEAD);
                    act("$n wakes and stands at $p.", ch, obj, null, TO_ROOM);
                } else if (IS_SET(obj.value[2], STAND_ON)) {
                    act("You wake and stand on $p.", ch, obj, null, TO_CHAR, POS_DEAD);
                    act("$n wakes and stands on $p.", ch, obj, null, TO_ROOM);
                } else {
                    act("You wake and stand in $p.", ch, obj, null, TO_CHAR, POS_DEAD);
                    act("$n wakes and stands in $p.", ch, obj, null, TO_ROOM);
                }
                if (IS_HARA_KIRI(ch)) {
                    send_to_char("You feel your blood heats your body.\n", ch);
                    ch.act = REMOVE_BIT(ch.act, PLR_HARA_KIRI);
                }
                ch.position = POS_STANDING;
                do_look(ch, "auto");
            }
            case POS_RESTING, POS_SITTING -> {
                if (obj == null) {
                    send_to_char("You stand up.\n", ch);
                    act("$n stands up.", ch, null, null, TO_ROOM);
                    ch.on = null;
                } else if (IS_SET(obj.value[2], STAND_AT)) {
                    act("You stand at $p.", ch, obj, null, TO_CHAR);
                    act("$n stands at $p.", ch, obj, null, TO_ROOM);
                } else if (IS_SET(obj.value[2], STAND_ON)) {
                    act("You stand on $p.", ch, obj, null, TO_CHAR);
                    act("$n stands on $p.", ch, obj, null, TO_ROOM);
                } else {
                    act("You stand in $p.", ch, obj, null, TO_CHAR);
                    act("$n stands on $p.", ch, obj, null, TO_ROOM);
                }
                ch.position = POS_STANDING;
            }
            case POS_STANDING -> send_to_char("You are already standing.\n", ch);
            case POS_FIGHTING -> send_to_char("You are already fighting!\n", ch);
        }

    }


    static void do_rest(@NotNull CHAR_DATA ch, String argument) {
        OBJ_DATA obj;

        if (ch.position == POS_FIGHTING) {
            send_to_char("You are already fighting!\n", ch);
            return;
        }

        if (MOUNTED(ch) != null) {
            send_to_char("You can't rest while mounted.\n", ch);
            return;
        }
        if (RIDDEN(ch) != null) {
            send_to_char("You can't rest while being ridden.\n", ch);
            return;
        }


        if (IS_AFFECTED(ch, AFF_SLEEP)) {
            send_to_char("You are already sleeping.\n", ch);
            return;
        }

        /* okay, now that we know we can rest, find an object to rest on */
        if (!argument.isEmpty()) {
            obj = get_obj_list(ch, argument, ch.in_room.contents);
            if (obj == null) {
                send_to_char("You don't see that here.\n", ch);
                return;
            }
        } else {
            obj = ch.on;
        }

        if (obj != null) {
            if (!IS_SET(obj.item_type, ITEM_FURNITURE)
                    || (!IS_SET(obj.value[2], REST_ON)
                    && !IS_SET(obj.value[2], REST_IN)
                    && !IS_SET(obj.value[2], REST_AT))) {
                send_to_char("You can't rest on that.\n", ch);
                return;
            }

            if (ch.on != obj && count_users(obj) >= obj.value[0]) {
                act("There's no more room on $p.", ch, obj, null, TO_CHAR, POS_DEAD);
                return;
            }

            ch.on = obj;
        }

        switch (ch.position) {
            case POS_SLEEPING -> {
                if (obj == null) {
                    send_to_char("You wake up and start resting.\n", ch);
                    act("$n wakes up and starts resting.", ch, null, null, TO_ROOM);
                } else if (IS_SET(obj.value[2], REST_AT)) {
                    act("You wake up and rest at $p.",
                            ch, obj, null, TO_CHAR, POS_SLEEPING);
                    act("$n wakes up and rests at $p.", ch, obj, null, TO_ROOM);
                } else if (IS_SET(obj.value[2], REST_ON)) {
                    act("You wake up and rest on $p.",
                            ch, obj, null, TO_CHAR, POS_SLEEPING);
                    act("$n wakes up and rests on $p.", ch, obj, null, TO_ROOM);
                } else {
                    act("You wake up and rest in $p.",
                            ch, obj, null, TO_CHAR, POS_SLEEPING);
                    act("$n wakes up and rests in $p.", ch, obj, null, TO_ROOM);
                }
                ch.position = POS_RESTING;
            }
            case POS_RESTING -> send_to_char("You are already resting.\n", ch);
            case POS_STANDING -> {
                if (obj == null) {
                    send_to_char("You rest.\n", ch);
                    act("$n sits down and rests.", ch, null, null, TO_ROOM);
                } else if (IS_SET(obj.value[2], REST_AT)) {
                    act("You sit down at $p and rest.", ch, obj, null, TO_CHAR);
                    act("$n sits down at $p and rests.", ch, obj, null, TO_ROOM);
                } else if (IS_SET(obj.value[2], REST_ON)) {
                    act("You sit on $p and rest.", ch, obj, null, TO_CHAR);
                    act("$n sits on $p and rests.", ch, obj, null, TO_ROOM);
                } else {
                    act("You rest in $p.", ch, obj, null, TO_CHAR);
                    act("$n rests in $p.", ch, obj, null, TO_ROOM);
                }
                ch.position = POS_RESTING;
            }
            case POS_SITTING -> {
                if (obj == null) {
                    send_to_char("You rest.\n", ch);
                    act("$n rests.", ch, null, null, TO_ROOM);
                } else if (IS_SET(obj.value[2], REST_AT)) {
                    act("You rest at $p.", ch, obj, null, TO_CHAR);
                    act("$n rests at $p.", ch, obj, null, TO_ROOM);
                } else if (IS_SET(obj.value[2], REST_ON)) {
                    act("You rest on $p.", ch, obj, null, TO_CHAR);
                    act("$n rests on $p.", ch, obj, null, TO_ROOM);
                } else {
                    act("You rest in $p.", ch, obj, null, TO_CHAR);
                    act("$n rests in $p.", ch, obj, null, TO_ROOM);
                }
                ch.position = POS_RESTING;
                if (IS_HARA_KIRI(ch)) {
                    send_to_char("You feel your blood heats your body.\n", ch);
                    ch.act = REMOVE_BIT(ch.act, PLR_HARA_KIRI);
                }
            }
        }


    }


    static void do_sit(@NotNull CHAR_DATA ch, String argument) {
        OBJ_DATA obj;

        if (ch.position == POS_FIGHTING) {
            send_to_char("Maybe you should finish this fight first?\n", ch);
            return;
        }
        if (MOUNTED(ch) != null) {
            send_to_char("You can't sit while mounted.\n", ch);
            return;
        }
        if (RIDDEN(ch) != null) {
            send_to_char("You can't sit while being ridden.\n", ch);
            return;
        }

        if (IS_AFFECTED(ch, AFF_SLEEP)) {
            send_to_char("You are already sleeping.\n", ch);
            return;
        }

        /* okay, now that we know we can sit, find an object to sit on */
        if (!argument.isEmpty()) {
            obj = get_obj_list(ch, argument, ch.in_room.contents);
            if (obj == null) {
                if (IS_AFFECTED(ch, AFF_SLEEP)) {
                    send_to_char("You are already sleeping.\n", ch);
                    return;
                }
                send_to_char("You don't see that here.\n", ch);
                return;
            }
        } else {
            obj = ch.on;
        }

        if (obj != null) {
            if (!IS_SET(obj.item_type, ITEM_FURNITURE)
                    || (!IS_SET(obj.value[2], SIT_ON)
                    && !IS_SET(obj.value[2], SIT_IN)
                    && !IS_SET(obj.value[2], SIT_AT))) {
                send_to_char("You can't sit on that.\n", ch);
                return;
            }

            if (ch.on != obj && count_users(obj) >= obj.value[0]) {
                act("There's no more room on $p.", ch, obj, null, TO_CHAR, POS_DEAD);
                return;
            }

            ch.on = obj;
        }
        switch (ch.position) {
            case POS_SLEEPING -> {
                if (obj == null) {
                    send_to_char("You wake and sit up.\n", ch);
                    act("$n wakes and sits up.", ch, null, null, TO_ROOM);
                } else if (IS_SET(obj.value[2], SIT_AT)) {
                    act("You wake and sit at $p.", ch, obj, null, TO_CHAR, POS_DEAD);
                    act("$n wakes and sits at $p.", ch, obj, null, TO_ROOM);
                } else if (IS_SET(obj.value[2], SIT_ON)) {
                    act("You wake and sit on $p.", ch, obj, null, TO_CHAR, POS_DEAD);
                    act("$n wakes and sits at $p.", ch, obj, null, TO_ROOM);
                } else {
                    act("You wake and sit in $p.", ch, obj, null, TO_CHAR, POS_DEAD);
                    act("$n wakes and sits in $p.", ch, obj, null, TO_ROOM);
                }
                ch.position = POS_SITTING;
            }
            case POS_RESTING -> {
                if (obj == null) {
                    send_to_char("You stop resting.\n", ch);
                } else if (IS_SET(obj.value[2], SIT_AT)) {
                    act("You sit at $p.", ch, obj, null, TO_CHAR);
                    act("$n sits at $p.", ch, obj, null, TO_ROOM);
                } else if (IS_SET(obj.value[2], SIT_ON)) {
                    act("You sit on $p.", ch, obj, null, TO_CHAR);
                    act("$n sits on $p.", ch, obj, null, TO_ROOM);
                }
                ch.position = POS_SITTING;
            }
            case POS_SITTING -> send_to_char("You are already sitting down.\n", ch);
            case POS_STANDING -> {
                if (obj == null) {
                    send_to_char("You sit down.\n", ch);
                    act("$n sits down on the ground.", ch, null, null, TO_ROOM);
                } else if (IS_SET(obj.value[2], SIT_AT)) {
                    act("You sit down at $p.", ch, obj, null, TO_CHAR);
                    act("$n sits down at $p.", ch, obj, null, TO_ROOM);
                } else if (IS_SET(obj.value[2], SIT_ON)) {
                    act("You sit on $p.", ch, obj, null, TO_CHAR);
                    act("$n sits on $p.", ch, obj, null, TO_ROOM);
                } else {
                    act("You sit down in $p.", ch, obj, null, TO_CHAR);
                    act("$n sits down in $p.", ch, obj, null, TO_ROOM);
                }
                ch.position = POS_SITTING;
            }
        }
        if (IS_HARA_KIRI(ch)) {
            send_to_char("You feel your blood heats your body.\n", ch);
            ch.act = REMOVE_BIT(ch.act, PLR_HARA_KIRI);
        }
    }


    static void do_sleep(@NotNull CHAR_DATA ch, String argument) {
        OBJ_DATA obj;

        if (MOUNTED(ch) != null) {
            send_to_char("You can't sleep while mounted.\n", ch);
            return;
        }
        if (RIDDEN(ch) != null) {
            send_to_char("You can't sleep while being ridden.\n", ch);
            return;
        }

        switch (ch.position) {
            case POS_SLEEPING -> send_to_char("You are already sleeping.\n", ch);
            case POS_RESTING, POS_SITTING, POS_STANDING -> {
                if (argument.isEmpty() && ch.on == null) {
                    send_to_char("You go to sleep.\n", ch);
                    act("$n goes to sleep.", ch, null, null, TO_ROOM);
                    ch.position = POS_SLEEPING;
                } else  /* find an object and sleep on it */ {
                    if (argument.isEmpty()) {
                        obj = ch.on;
                    } else {
                        obj = get_obj_list(ch, argument, ch.in_room.contents);
                    }

                    if (obj == null) {
                        send_to_char("You don't see that here.\n", ch);
                        return;
                    }
                    if (obj.item_type != ITEM_FURNITURE
                            || (!IS_SET(obj.value[2], SLEEP_ON)
                            && !IS_SET(obj.value[2], SLEEP_IN)
                            && !IS_SET(obj.value[2], SLEEP_AT))) {
                        send_to_char("You can't sleep on that!\n", ch);
                        return;
                    }

                    if (ch.on != obj && count_users(obj) >= obj.value[0]) {
                        act("There is no room on $p for you.", ch, obj, null, TO_CHAR, POS_DEAD);
                        return;
                    }

                    ch.on = obj;
                    if (IS_SET(obj.value[2], SLEEP_AT)) {
                        act("You go to sleep at $p.", ch, obj, null, TO_CHAR);
                        act("$n goes to sleep at $p.", ch, obj, null, TO_ROOM);
                    } else if (IS_SET(obj.value[2], SLEEP_ON)) {
                        act("You go to sleep on $p.", ch, obj, null, TO_CHAR);
                        act("$n goes to sleep on $p.", ch, obj, null, TO_ROOM);
                    } else {
                        act("You go to sleep in $p.", ch, obj, null, TO_CHAR);
                        act("$n goes to sleep in $p.", ch, obj, null, TO_ROOM);
                    }
                    ch.position = POS_SLEEPING;
                }
            }
            case POS_FIGHTING -> send_to_char("You are already fighting!\n", ch);
        }

    }


    static void do_wake(@NotNull CHAR_DATA ch, String argument) {
        CHAR_DATA victim;

        var arg = new StringBuilder();
        one_argument(argument, arg);
        if (arg.isEmpty()) {
            do_stand(ch, argument);
            return;
        }

        if (!IS_AWAKE(ch)) {
            send_to_char("You are asleep yourself!\n", ch);
            return;
        }

        if ((victim = get_char_room(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (IS_AWAKE(victim)) {
            act("$N is already awake.", ch, null, victim, TO_CHAR);
            return;
        }

        if (IS_AFFECTED(victim, AFF_SLEEP)) {
            act("You can't wake $M!", ch, null, victim, TO_CHAR);
            return;
        }

        act("$n wakes you.", ch, null, victim, TO_VICT, POS_SLEEPING);
        do_stand(victim, "");
    }


    static void do_sneak(@NotNull CHAR_DATA ch) {

        if (MOUNTED(ch) != null) {
            send_to_char("You can't sneak while mounted.\n", ch);
            return;
        }

        if (RIDDEN(ch) != null) {
            send_to_char("You can't hide while being ridden.\n", ch);
            return;
        }

        send_to_char("You attempt to move silently.\n", ch);
        affect_strip(ch, gsn_sneak);

        if (IS_AFFECTED(ch, AFF_SNEAK)) {
            return;
        }

        if (number_percent() < get_skill(ch, gsn_sneak)) {
            check_improve(ch, gsn_sneak, true, 3);
            var af = new AFFECT_DATA();

            af.where = TO_AFFECTS;
            af.type = gsn_sneak;
            af.level = ch.level;
            af.duration = ch.level;
            af.location = APPLY_NONE;
            af.modifier = 0;
            af.bitvector = AFF_SNEAK;
            affect_to_char(ch, af);
        } else {
            check_improve(ch, gsn_sneak, false, 3);
        }

    }


    static void do_hide(@NotNull CHAR_DATA ch) {
        int forest;

        if (MOUNTED(ch) != null) {
            send_to_char("You can't hide while mounted.\n", ch);
            return;
        }

        if (RIDDEN(ch) != null) {
            send_to_char("You can't hide while being ridden.\n", ch);
            return;
        }

        if (IS_AFFECTED(ch, AFF_FAERIE_FIRE)) {
            send_to_char("You can not hide while glowing.\n", ch);
            return;
        }
        forest = 0;
        forest += ch.in_room.sector_type == SECT_FOREST ? 60 : 0;
        forest += ch.in_room.sector_type == SECT_FIELD ? 60 : 0;

        send_to_char("You attempt to hide.\n", ch);

        if (number_percent() < get_skill(ch, gsn_hide) - forest) {
            ch.affected_by = SET_BIT(ch.affected_by, AFF_HIDE);
            check_improve(ch, gsn_hide, true, 3);
        } else {
            if (IS_AFFECTED(ch, AFF_HIDE)) {
                ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_HIDE);
            }
            check_improve(ch, gsn_hide, false, 3);
        }

    }

    static void do_camouflage(@NotNull CHAR_DATA ch) {

        if (skill_failure_check(ch, gsn_camouflage, false, 0,
                "You don't know how to camouflage yourself.\n")) {
            return;
        }

        if (RIDDEN(ch) != null) {
            send_to_char("You can't camouflage while being ridden.\n", ch);
            return;
        }

        if (IS_AFFECTED(ch, AFF_FAERIE_FIRE)) {
            send_to_char("You can't camouflage yourself while glowing.\n", ch);
            return;
        }

        if (ch.in_room.sector_type != SECT_FOREST &&
                ch.in_room.sector_type != SECT_HILLS &&
                ch.in_room.sector_type != SECT_MOUNTAIN) {
            send_to_char("There is no cover here.\n", ch);
            act("$n tries to camouflage $mself against the lone leaf on the ground.", ch, null, null, TO_ROOM);
            return;
        }
        send_to_char("You attempt to camouflage yourself.\n", ch);
        WAIT_STATE(ch, gsn_camouflage.beats);

        if (IS_AFFECTED(ch, AFF_CAMOUFLAGE)) {
            ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_CAMOUFLAGE);
        }


        if (IS_NPC(ch) ||
                number_percent() < get_skill(ch, gsn_camouflage)) {
            ch.affected_by = SET_BIT(ch.affected_by, AFF_CAMOUFLAGE);
            check_improve(ch, gsn_camouflage, true, 1);
        } else {
            check_improve(ch, gsn_camouflage, false, 1);
        }

    }

    /*
     * Contributed by Alander
     */

    static void do_visible(@NotNull CHAR_DATA ch) {
        if (IS_SET(ch.affected_by, AFF_HIDE)) {
            send_to_char("You step out of the shadows.\n", ch);
            ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_HIDE);
            act("$n steps out of the shadows.", ch, null, null, TO_ROOM);
        }
        if (IS_SET(ch.affected_by, AFF_FADE)) {
            send_to_char("You step out of the shadows.\n", ch);
            ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_FADE);
            act("$n steps out of the shadows.", ch, null, null, TO_ROOM);
        }
        if (IS_SET(ch.affected_by, AFF_CAMOUFLAGE)) {
            send_to_char("You step out from your cover.\n", ch);
            ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_CAMOUFLAGE);
            act("$n steps out from $s cover.", ch, null, null, TO_ROOM);
        }
        if (IS_SET(ch.affected_by, AFF_INVISIBLE)) {
            send_to_char("You fade into existence.\n", ch);
            affect_strip(ch, gsn_invis);
            affect_strip(ch, gsn_mass_invis);
            ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_INVISIBLE);
            act("$n fades into existence.", ch, null, null, TO_ROOM);
        }
        if (IS_SET(ch.affected_by, AFF_IMP_INVIS)) {
            send_to_char("You fade into existence.\n", ch);
            affect_strip(ch, gsn_imp_invis);
            ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_IMP_INVIS);
            act("$n fades into existence.", ch, null, null, TO_ROOM);
        }
        if (IS_SET(ch.affected_by, AFF_SNEAK)
                && !IS_NPC(ch) && !IS_SET(ch.race.aff, AFF_SNEAK)) {
            send_to_char("You trample around loudly again.\n", ch);
            affect_strip(ch, gsn_sneak);
            ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_SNEAK);
        }

        affect_strip(ch, gsn_mass_invis);

        if (IS_AFFECTED(ch, AFF_EARTHFADE)) {
            send_to_char("You fade to your neutral form.\n", ch);
            act("Earth forms $n in front of you.", ch, null, null, TO_ROOM);
            affect_strip(ch, gsn_earthfade);
            WAIT_STATE(ch, (PULSE_VIOLENCE / 2));
            ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_EARTHFADE);
        }
    }

    static void do_recall(@NotNull CHAR_DATA ch) {
        ROOM_INDEX_DATA location;
        int point;
        int lose, skill;

        if (IS_GOOD(ch)) {
            point = hometown_table[ch.hometown].recall[0];
        } else if (IS_EVIL(ch)) {
            point = hometown_table[ch.hometown].recall[2];
        } else {
            point = hometown_table[ch.hometown].recall[1];
        }

        if (IS_NPC(ch) && !IS_SET(ch.act, ACT_PET)) {
            send_to_char("Only players can recall.\n", ch);
            return;
        }

        if (ch.level >= 11 && !IS_IMMORTAL(ch)) {
            send_to_char("Recall is for only levels below 10.\n", ch);
            return;
        }
/*
    if (ch.desc != null && current_time - ch.last_fight_time
    < FIGHT_DELAY_TIME)
      {
    send_to_char("You are too pumped to pray now.\n",ch);
    return;
      }
*/
        if (ch.desc == null && !IS_NPC(ch)) {
            point = hometown_table[number_range(0, 4)].recall[number_range(0, 2)];
        }

        act("$n prays for transportation!", ch, 0, 0, TO_ROOM);

        if ((location = get_room_index(point)) == null) {
            send_to_char("You are completely lost.\n", ch);
            return;
        }

        if (ch.in_room == location) {
            return;
        }

        if (IS_SET(ch.in_room.room_flags, ROOM_NO_RECALL)
                || IS_AFFECTED(ch, AFF_CURSE)
                || IS_RAFFECTED(ch.in_room, AFF_ROOM_CURSE)) {
            send_to_char("The gods have forsaken you.\n", ch);
            return;
        }

        if (ch.fighting != null) {
            send_to_char("You are still fighting!\n", ch);

            if (IS_NPC(ch)) {
                skill = 40 + ch.level;
            } else {
                skill = get_skill(ch, gsn_recall);
            }

            if (number_percent() < 80 * skill / 100) {
                check_improve(ch, gsn_recall, false, 6);
                WAIT_STATE(ch, 4);
                send_to_char("You failed!.\n", ch);
                return;
            }

            lose = 25;
            gain_exp(ch, -lose);
            check_improve(ch, gsn_recall, true, 4);
            var buf = new TextBuffer();
            buf.sprintf("You recall from combat!  You lose %d exps.\n", lose);
            send_to_char(buf, ch);
            stop_fighting(ch, true);
        }

        ch.move /= 2;
        act("$n disappears.", ch, null, null, TO_ROOM);
        char_from_room(ch);
        char_to_room(ch, location);
        act("$n appears in the room.", ch, null, null, TO_ROOM);
        do_look(ch, "auto");

        if (ch.pet != null) {
            char_from_room(ch.pet);
            char_to_room(ch.pet, location);
            do_look(ch.pet, "auto");
        }

    }


    static void do_train(@NotNull CHAR_DATA ch, String argument) {
        CHAR_DATA mob;
        var stat = -1;
        String pOutput = null;
        if (IS_NPC(ch)) {
            return;
        }

        /*
         * Check for trainer.
         */
        for (mob = ch.in_room.people; mob != null; mob = mob.next_in_room) {
            if (IS_NPC(mob) && (IS_SET(mob.act, ACT_PRACTICE)
                    || IS_SET(mob.act, ACT_TRAIN) || IS_SET(mob.act, ACT_GAIN))) {
                break;
            }
        }

        if (mob == null) {
            send_to_char("You can't do that here.\n", ch);
            return;
        }


        if (argument.isEmpty()) {
            var buf = new TextBuffer();

            buf.sprintf("You have %d training sessions.\n", ch.train);
            send_to_char(buf, ch);
            argument = "foo";
        }

        var cost = 1;

        if (!str_cmp(argument, "str")) {
            stat = STAT_STR;
            pOutput = "strength";
        } else if (!str_cmp(argument, "int")) {
            stat = STAT_INT;
            pOutput = "intelligence";
        } else if (!str_cmp(argument, "wis")) {
            stat = STAT_WIS;
            pOutput = "wisdom";
        } else if (!str_cmp(argument, "dex")) {
            stat = STAT_DEX;
            pOutput = "dexterity";
        } else if (!str_cmp(argument, "con")) {
            stat = STAT_CON;
            pOutput = "constitution";
        } else if (!str_cmp(argument, "cha")) {
            stat = STAT_CHA;
            pOutput = "charisma";
/*
    buf.sprintf(
 "You can't train charisma. That is about your behaviour.\n");
    send_to_char( buf, ch );
    return;
*/
        } else if (str_cmp(argument, "hp") && str_cmp(argument, "mana")) {
            var buf = new StringBuilder("You can train:");
            if (ch.perm_stat[STAT_STR] < get_max_train2(ch, STAT_STR)) {
                buf.append(" str");
            }
            if (ch.perm_stat[STAT_INT] < get_max_train2(ch, STAT_INT)) {
                buf.append(" int");
            }
            if (ch.perm_stat[STAT_WIS] < get_max_train2(ch, STAT_WIS)) {
                buf.append(" wis");
            }
            if (ch.perm_stat[STAT_DEX] < get_max_train2(ch, STAT_DEX)) {
                buf.append(" dex");
            }
            if (ch.perm_stat[STAT_CON] < get_max_train2(ch, STAT_CON)) {
                buf.append(" con");
            }
            if (ch.perm_stat[STAT_CHA] < get_max_train2(ch, STAT_CHA)) {
                buf.append(" cha");
            }
            buf.append(" hp mana");

            if (buf.charAt(buf.length() - 1) != ':') {
                buf.append(".\n");
                send_to_char(buf, ch);
            } else {
                /*
                 * This message dedicated to Jordan ... you big stud!
                 */
                act("You have nothing left to train, you $T!",
                        ch, null,
                        ch.sex == SEX_MALE ? "big stud" : ch.sex == SEX_FEMALE ? "hot babe" : "wild thing", TO_CHAR);
            }

            return;
        }

        if (!str_cmp("hp", argument)) {
            if (cost > ch.train) {
                send_to_char("You don't have enough training sessions.\n", ch);
                return;
            }

            ch.train -= cost;
            ch.pcdata.perm_hit += 10;
            ch.max_hit += 10;
            ch.hit += 10;
            act("Your durability increases!", ch, null, null, TO_CHAR);
            act("$n's durability increases!", ch, null, null, TO_ROOM);
            return;
        }

        if (!str_cmp("mana", argument)) {
            if (cost > ch.train) {
                send_to_char("You don't have enough training sessions.\n", ch);
                return;
            }

            ch.train -= cost;
            ch.pcdata.perm_mana += 10;
            ch.max_mana += 10;
            ch.mana += 10;
            act("Your power increases!", ch, null, null, TO_CHAR);
            act("$n's power increases!", ch, null, null, TO_ROOM);
            return;
        }

        if (ch.perm_stat[stat] >= get_max_train2(ch, stat)) {
            act("Your $T is already at maximum.", ch, null, pOutput, TO_CHAR);
            return;
        }

        if (cost > ch.train) {
            send_to_char("You don't have enough training sessions.\n", ch);
            return;
        }

        ch.train -= cost;

        ch.perm_stat[stat] += 1;
        act("Your $T increases!", ch, null, pOutput, TO_CHAR);
        act("$n's $T increases!", ch, null, pOutput, TO_ROOM);
    }


    static String[] door = {"north", "east", "south", "west", "up", "down", "that way"};

    static void do_track(@NotNull CHAR_DATA ch, String argument) {
        ROOM_HISTORY_DATA rh;
        EXIT_DATA pexit;
        int d;

        if (skill_failure_check(ch, gsn_track, false, 0, "There are no train tracks here.\n")) {
            return;
        }

        WAIT_STATE(ch, gsn_track.beats);
        act("$n checks the ground for tracks.", ch, null, null, TO_ROOM);

        if (IS_NPC(ch) || number_percent() < get_skill(ch, gsn_track)) {
            if (IS_NPC(ch)) {
                ch.status = 0;
                if (ch.last_fought != null && !IS_SET(ch.act, ACT_NOTRACK)) {
                    add_mind(ch, ch.last_fought.name);
                }
            }
            for (rh = ch.in_room.history; rh != null; rh = rh.next) {
                if (is_name(argument, rh.name)) {
                    check_improve(ch, gsn_track, true, 1);
                    if ((d = rh.went) == -1) {
                        continue;
                    }
                    var buf = new TextBuffer();
                    buf.sprintf("%s's tracks lead %s.\n", capitalize(rh.name), door[d]);
                    send_to_char(buf, ch);
                    if ((pexit = ch.in_room.exit[d]) != null && IS_SET(pexit.exit_info, EX_ISDOOR) && pexit.keyword != null) {
                        do_open(ch, door[d]);
                    }
                    move_char(ch, rh.went);
                    return;
                }
            }
        }
        send_to_char("You don't see any tracks.\n", ch);
        if (IS_NPC(ch)) {
            ch.status = 5; /* for stalker */
        }
        check_improve(ch, gsn_track, false, 1);
    }


    static void do_vampire(@NotNull CHAR_DATA ch) {
        int level, duration;

        if (skill_failure_check(ch, gsn_vampire, false, 0,
                "You try to show yourself even more uggly.\n")) {
            return;
        }

        if (is_affected(ch, gsn_vampire)) {
            send_to_char("You can't be much more vampire!\n", ch);
            return;
        }

        if (get_skill(ch, gsn_vampire) < 50) {
            send_to_char("Go and ask the questor to help you.\n", ch);
            return;
        }

        if (is_affected(ch, gsn_vampire)) {
            send_to_char("If you wan't to be more vampire go and kill a player.\n", ch);
            return;
        }

        if (weather_info.sunlight == SUN_LIGHT
                || weather_info.sunlight == SUN_RISE) {
            send_to_char(
                    "You should have waited the evening or night to tranform to a vampire.\n", ch);
        }

        level = ch.level;
        duration = level / 10;
        duration += 5;

        /* haste */
        var af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = gsn_vampire;
        af.level = level;
        af.duration = duration;
        af.location = APPLY_DEX;
        af.modifier = 1 + (level / 20);
        af.bitvector = AFF_HASTE;
        affect_to_char(ch, af);

        /* giant strength + infrared */
        af.where = TO_AFFECTS;
        af.type = gsn_vampire;
        af.level = level;
        af.duration = duration;
        af.location = APPLY_STR;
        af.modifier = 1 + (level / 20);
        af.bitvector = AFF_INFRARED;
        affect_to_char(ch, af);

        /* size */
        af.where = TO_AFFECTS;
        af.type = gsn_vampire;
        af.level = level;
        af.duration = duration;
        af.location = APPLY_SIZE;
        af.modifier = 1 + (level / 50);
        af.bitvector = AFF_SNEAK;
        affect_to_char(ch, af);

        /* damroll */
        af.where = TO_AFFECTS;
        af.type = gsn_vampire;
        af.level = level;
        af.duration = duration;
        af.location = APPLY_DAMROLL;
        af.modifier = ch.damroll;
        af.bitvector = AFF_BERSERK;
        affect_to_char(ch, af);

        /* negative immunity */
        af.where = TO_IMMUNE;
        af.type = gsn_vampire;
        af.duration = duration;
        af.level = level;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = IMM_NEGATIVE;
        affect_to_char(ch, af);

        /* vampire flag */
        af.where = TO_ACT_FLAG;
        af.type = gsn_vampire;
        af.level = level;
        af.duration = duration;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = PLR_VAMPIRE;
        affect_to_char(ch, af);

        send_to_char("You feel yourself getting greater and greater.\n", ch);
        act("You cannot recognize $n anymore.", ch, null, null, TO_ROOM);
    }

    static void do_vbite(@NotNull CHAR_DATA ch, String argument) {
        CHAR_DATA victim;

        var arg = new StringBuilder();
        one_argument(argument, arg);

        if (skill_failure_check(ch, gsn_vampiric_bite, false, 0, "You don't know how to bite creatures.\n")) {
            return;
        }

        if (!IS_VAMPIRE(ch)) {
            send_to_char("You must transform vampire before biting.\n", ch);
            return;
        }

        if (arg.isEmpty()) {
            send_to_char("Bite whom?\n", ch);
            return;
        }

        if ((victim = get_char_room(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (victim.position != POS_SLEEPING) {
            send_to_char("They must be sleeping.\n", ch);
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

        if (victim.fighting != null) {
            send_to_char("You can't bite a fighting person.\n", ch);
            return;
        }


        WAIT_STATE(ch, gsn_vampiric_bite.beats);

        if (victim.hit < (0.8 * victim.max_hit) &&
                (IS_AWAKE(victim))) {
            act("$N is hurt and suspicious ... doesn't worth up.",
                    ch, null, victim, TO_CHAR);
            return;
        }

        if (current_time - victim.last_fight_time < 300 && IS_AWAKE(victim)) {
            act("$N is suspicious ... it doesn't worth to do.",
                    ch, null, victim, TO_CHAR);
            return;
        }

        if (!IS_AWAKE(victim)
                && (IS_NPC(ch)
                || number_percent() <
                ((get_skill(ch, gsn_vampiric_bite) * 0.7) +
                        (2 * (ch.level - victim.level))))) {
            check_improve(ch, gsn_vampiric_bite, true, 1);
            one_hit(ch, victim, gsn_vampiric_bite, false);
        } else {
            check_improve(ch, gsn_vampiric_bite, false, 1);
            damage(ch, victim, 0, gsn_vampiric_bite, DAM_NONE, true);
        }
        /* Player shouts if he doesn't die */
        if (!(IS_NPC(victim)) && !(IS_NPC(ch))
                && victim.position == POS_FIGHTING) {
            do_yell(victim, "Help, an ugly creature tried to bite me!");
        }
    }

    static void do_bash_door(@NotNull CHAR_DATA ch, String argument) {
        CHAR_DATA gch;
        var chance = 0;
        int damage_bash, door;

        var arg = new StringBuilder();
        one_argument(argument, arg);

        if (skill_failure_check(ch, gsn_bash_door, false, OFF_BASH, "Bashing? What's that?\n")) {
            return;
        }

        if (RIDDEN(ch) != null) {
            send_to_char("You can't bash doors while being ridden.\n", ch);
            return;
        }

        if (arg.isEmpty()) {
            send_to_char("Bash which door or direction.\n", ch);
            return;
        }

        if (ch.fighting != null) {
            send_to_char("Wait until the fight finishes.\n", ch);
            return;
        }

        /* look for guards */
        for (gch = ch.in_room.people; gch != null; gch = gch.next_in_room) {
            if (IS_NPC(gch) && IS_AWAKE(gch) && ch.level + 5 < gch.level) {
                act("$N is standing too close to the door.",
                        ch, null, gch, TO_CHAR);
                return;
            }
        }

        if ((door = find_door(ch, arg.toString())) >= 0) {
            /* 'bash door' */
            ROOM_INDEX_DATA to_room;
            EXIT_DATA pexit;
            EXIT_DATA pexit_rev;

            pexit = ch.in_room.exit[door];
            if (!IS_SET(pexit.exit_info, EX_CLOSED)) {
                send_to_char("It's already open.\n", ch);
                return;
            }
            if (!IS_SET(pexit.exit_info, EX_LOCKED)) {
                send_to_char("Just try to open it.\n", ch);
                return;
            }
            if (IS_SET(pexit.exit_info, EX_NOPASS)) {
                send_to_char("A mystical shield protects the exit.\n", ch);
                return;
            }

            /* modifiers */

            /* size  and weight */
            chance += get_carry_weight(ch) / 100;

            chance += (ch.size - 2) * 20;

            /* stats */
            chance += get_curr_stat(ch, STAT_STR);

            if (IS_AFFECTED(ch, AFF_FLYING)) {
                chance -= 10;
            }

            /* level
            chance += ch.level / 10;
            */

            chance += (get_skill(ch, gsn_bash_door) - 90);

            act("You slam into $d, and try to break $d!",
                    ch, null, pexit.keyword, TO_CHAR);
            act("You slam into $d, and try to break $d!",
                    ch, null, pexit.keyword, TO_ROOM);

            if (room_dark(ch.in_room)) {
                chance /= 2;
            }

            /* now the attack */
            if (number_percent() < chance) {

                check_improve(ch, gsn_bash_door, true, 1);

                pexit.exit_info = REMOVE_BIT(pexit.exit_info, EX_LOCKED);
                pexit.exit_info = REMOVE_BIT(pexit.exit_info, EX_CLOSED);
                act("$n bashes the the $d and breaks the lock.", ch, null,
                        pexit.keyword, TO_ROOM);
                send_to_char("You successed to open the door.\n", ch);

                /* open the other side */
                if ((to_room = pexit.to_room) != null
                        && (pexit_rev = to_room.exit[rev_dir[door]]) != null
                        && pexit_rev.to_room == ch.in_room) {
                    CHAR_DATA rch;

                    pexit_rev.exit_info = REMOVE_BIT(pexit_rev.exit_info, EX_CLOSED);
                    pexit_rev.exit_info = REMOVE_BIT(pexit_rev.exit_info, EX_LOCKED);
                    for (rch = to_room.people; rch != null; rch = rch.next_in_room) {
                        act("The $d opens.", rch, null, pexit_rev.keyword, TO_CHAR);
                    }
                }


                if (number_percent() < chance) {
                    move_char(ch, door);
                    ch.position = POS_RESTING;
                }
                WAIT_STATE(ch, gsn_bash_door.beats);

            } else {
                act("You fall flat on your face!",
                        ch, null, null, TO_CHAR);
                act("$n falls flat on $s face.",
                        ch, null, null, TO_ROOM);
                check_improve(ch, gsn_bash_door, false, 1);
                ch.position = POS_RESTING;
                WAIT_STATE(ch, gsn_bash.beats * 3 / 2);
                damage_bash = ch.damroll + number_range(4, 4 + 4 * ch.size + chance / 5);
                damage(ch, ch, damage_bash, gsn_bash, DAM_BASH, true);
            }
        }
    }

    static void do_blink(@NotNull CHAR_DATA ch, String argument) {
        var arg = new StringBuilder();
        one_argument(argument, arg);

        if (skill_failure_check(ch, gsn_blink, false, 0, null)) {
            return;
        }
        if (arg.isEmpty()) {
            var buf = new TextBuffer();
            buf.sprintf("Your current blink status : %s.\n", IS_BLINK_ON(ch) ? "ON" : "OFF");
            send_to_char(buf, ch);
            return;
        }

        if (!str_cmp(arg.toString(), "ON")) {
            ch.act = SET_BIT(ch.act, PLR_BLINK_ON);
            send_to_char("Now ,your current blink status is ON.\n", ch);
            return;
        }

        if (!str_cmp(arg.toString(), "OFF")) {
            ch.act = REMOVE_BIT(ch.act, PLR_BLINK_ON);
            send_to_char("Now ,your current blink status is OFF.\n", ch);
            return;
        }

        var buf = new TextBuffer();
        buf.sprintf("What is that?.Is %s a status?\n", arg);
        send_to_char(buf, ch);
    }

    static void do_vanish(@NotNull CHAR_DATA ch) {
        int i;

        if (skill_failure_check(ch, gsn_vanish, false, 0, null)) {
            return;
        }

        if (ch.mana < 25) {
            send_to_char("You don't have enough power.\n", ch);
            return;
        }

        ch.mana -= 25;

        WAIT_STATE(ch, gsn_vanish.beats);

        if (number_percent() > get_skill(ch, gsn_vanish)) {
            send_to_char("You failed.\n", ch);
            check_improve(ch, gsn_vanish, false, 1);
            return;
        }

        if (ch.in_room == null
                || IS_SET(ch.in_room.room_flags, ROOM_NO_RECALL)
                || cabal_area_check(ch)) {
            send_to_char("You failed.\n", ch);
            return;
        }

        ROOM_INDEX_DATA pRoomIndex = null;
        for (i = 0; i < 65535; i++) {
            if ((pRoomIndex = get_room_index(number_range(0, 65535))) == null) {
                continue;
            }
            if (can_see_room(ch, pRoomIndex) && !room_is_private(pRoomIndex) && ch.in_room.area == pRoomIndex.area) {
                break;
            }
        }

        if (pRoomIndex == null) {
            send_to_char("You failed.\n", ch);
            return;
        }

        act("$n throws down a small globe.", ch, null, null, TO_ROOM);

        check_improve(ch, gsn_vanish, true, 1);

        if (!IS_NPC(ch) && ch.fighting != null && number_bits(1) == 1) {
            send_to_char("You failed.\n", ch);
            return;
        }

        act("$n is gone!", ch, null, null, TO_ROOM);

        char_from_room(ch);
        char_to_room(ch, pRoomIndex);
        act("$n appears from nowhere.", ch, null, null, TO_ROOM);
        do_look(ch, "auto");
        stop_fighting(ch, true);
    }

    static void do_detect_sneak(@NotNull CHAR_DATA ch) {

        if (skill_failure_check(ch, gsn_detect_sneak, false, 0, null)) {
            return;
        }

        if (RIDDEN(ch) != null) {
            if (is_affected(ch, gsn_detect_sneak)) {
                send_to_char("You can already detect sneaking.\n", ch);
            }
        }
        var af = new AFFECT_DATA();

        af.where = TO_AFFECTS;
        af.type = gsn_detect_sneak;
        af.level = ch.level;
        af.duration = ch.level / 10;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_DETECT_SNEAK;
        affect_to_char(ch, af);
        send_to_char("You can detect the sneaking.\n", ch);
    }


    static void do_fade(@NotNull CHAR_DATA ch) {
        if (skill_failure_check(ch, gsn_fade, false, 0, null)) {
            return;
        }

        if (RIDDEN(ch) != null) {
            send_to_char("You can't fade while being ridden.\n", ch);
            return;
        }

        send_to_char("You attempt to fade.\n", ch);

        ch.affected_by = SET_BIT(ch.affected_by, AFF_FADE);
        check_improve(ch, gsn_fade, true, 3);

    }

    static void do_vtouch(@NotNull CHAR_DATA ch, String argument) {
        CHAR_DATA victim;

        if (skill_failure_check(ch, gsn_vampiric_touch, false, 0, "You lack the skill to draining touch.\n")) {
            return;
        }

        if (!IS_VAMPIRE(ch)) {
            send_to_char("Let it be.\n", ch);
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
            send_to_char("You don't want to drain your master.\n", ch);
            return;
        }

        if (IS_AFFECTED(victim, AFF_CHARM)) {
            send_to_char("Your victim is already sleeping.\n", ch);
            return;
        }

        if (is_affected(victim, gsn_blackguard)) {
            act("$N's doesn't let you to go that much close.", ch, null, victim, TO_CHAR);
            return;
        }

        if (is_affected(victim, gsn_vampiric_touch)) {
            return;
        }

        if (is_safe(ch, victim)) {
            return;
        }

        victim.last_fight_time = current_time;
        ch.last_fight_time = current_time;

        WAIT_STATE(ch, gsn_vampiric_touch.beats);

        if (IS_NPC(ch) ||
                number_percent() < 0.85 * get_skill(ch, gsn_vampiric_touch)) {
            act("You deadly touch  $N's neck and put $M to nightmares.",
                    ch, null, victim, TO_CHAR);
            act("$n's deadly touch your neck and puts you to nightmares.",
                    ch, null, victim, TO_VICT);
            act("$n's deadly touch $N's neck and puts $M to nightmares.",
                    ch, null, victim, TO_NOTVICT);
            check_improve(ch, gsn_vampiric_touch, true, 1);

            var af = new AFFECT_DATA();

            af.type = gsn_vampiric_touch;
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

            damage(ch, victim, 0, gsn_vampiric_touch, DAM_NONE, true);
            check_improve(ch, gsn_vampiric_touch, false, 1);
            if (!can_see(victim, ch)) {
                do_yell(victim, "Help! I'm being strangled by someone!");
            } else {
                var buf = new TextBuffer();
                buf.sprintf("Help! I'm being attacked by %s!",
                        (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(victim)) ? ch.doppel.name : ch.name);
                if (!IS_NPC(victim)) {
                    do_yell(victim, buf.toString());
                }
            }
            var af = new AFFECT_DATA();
            af.type = gsn_blackguard;
            af.where = TO_AFFECTS;
            af.level = victim.level;
            af.duration = 2 + victim.level / 50;
            af.modifier = 0;
            af.bitvector = 0;
            af.location = APPLY_NONE;
            affect_join(victim, af);
        }
    }

    static void do_fly(@NotNull CHAR_DATA ch, String argument) {

        if (IS_NPC(ch)) {
            return;
        }

        var arg = new StringBuilder();
        one_argument(argument, arg);
        if (!str_cmp(arg.toString(), "up")) {
            if (IS_AFFECTED(ch, AFF_FLYING)) {
                send_to_char("You are already flying.\n", ch);
                return;
            }
            if (is_affected(ch, gsn_fly) || (ch.race.aff & AFF_FLYING) != 0 || affect_check_obj(ch, AFF_FLYING)) {
                ch.affected_by = SET_BIT(ch.affected_by, AFF_FLYING);
                ch.act = REMOVE_BIT(ch.act, PLR_CHANGED_AFF);
                send_to_char("You start to fly.\n", ch);
            } else {
                send_to_char("To fly , find a potion or wings.\n", ch);
            }
        } else if (!str_cmp(arg.toString(), "down")) {
            if (IS_AFFECTED(ch, AFF_FLYING)) {
                ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_FLYING);
                ch.act = SET_BIT(ch.act, PLR_CHANGED_AFF);
                send_to_char("You slowly touch the ground.\n", ch);
            } else {
                send_to_char("You are already on the ground.\n", ch);
                return;
            }
        } else {
            send_to_char("Type fly with 'up' or 'down'.\n", ch);
            return;
        }

        WAIT_STATE(ch, gsn_fly.beats);

    }


    static void do_push(@NotNull CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        int percent;
        int door;


        if (skill_failure_check(ch, gsn_push, false, 0, null)) {
            return;
        }

        var arg1 = new StringBuilder();
        var arg2 = new StringBuilder();
        argument = one_argument(argument, arg1);
        one_argument(argument, arg2);

        if (arg1.isEmpty() || arg2.isEmpty()) {
            send_to_char("Push whom to what diretion?\n", ch);
            return;
        }

        if (IS_NPC(ch) && IS_SET(ch.affected_by, AFF_CHARM)
                && (ch.master != null)) {
            send_to_char("You are to dazed to push anyone.\n", ch);
            return;
        }

        if ((victim = get_char_room(ch, arg1.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (!IS_NPC(victim) && victim.desc == null) {
            send_to_char("You can't do that.\n", ch);
            return;
        }

        if (victim == ch) {
            send_to_char("That's pointless.\n", ch);
            return;
        }

        if (is_safe(ch, victim)) {
            return;
        }

        if (victim.position == POS_FIGHTING) {
            send_to_char("Wait till the end of fight.\n", ch);
            return;
        }

        if ((door = find_exit(ch, arg2.toString())) >= 0) {
            /* 'push' */
            EXIT_DATA pexit;

            if ((pexit = ch.in_room.exit[door]) != null) {
                if (IS_SET(pexit.exit_info, EX_ISDOOR)) {
                    if (IS_SET(pexit.exit_info, EX_CLOSED)) {
                        send_to_char("Direction is closed.\n", ch);
                    } else if (IS_SET(pexit.exit_info, EX_LOCKED)) {
                        send_to_char("Direction is locked.\n", ch);
                    }
                    return;
                }
            }

            if (IS_AFFECTED(ch, AFF_WEB)) {
                send_to_char("You're webbed, and want to do WHAT?!?\n", ch);
                act("$n stupidly tries to push $N while webbed.", ch, null, victim, TO_ROOM);
                return;
            }

            if (IS_AFFECTED(victim, AFF_WEB)) {
                act("You attempt to push $N, but the webs hold $m in place.",
                        ch, null, victim, TO_CHAR);
                act("$n attempts to push $n, but fails as the webs hold $n in place.",
                        ch, null, victim, TO_ROOM);
                return;
            }

            ch.last_death_time = -1;

            WAIT_STATE(ch, gsn_push.beats);
            percent = number_percent() + (IS_AWAKE(victim) ? 10 : -50);
            percent += can_see(victim, ch) ? -10 : 0;

            var buf = new TextBuffer();

            if ( /* ch.level + 5 < victim.level || */
                    victim.position == POS_FIGHTING
                            || (!IS_NPC(ch) && percent > get_skill(ch, gsn_push))) {
                /*
                 * Failure.
                 */

                send_to_char("Oops.\n", ch);
                if (!IS_AFFECTED(victim, AFF_SLEEP)) {
                    victim.position = victim.position == POS_SLEEPING ? POS_STANDING : victim.position;
                    act("$n tried to push you.\n", ch, null, victim, TO_VICT);
                }
                act("$n tried to push $N.\n", ch, null, victim, TO_NOTVICT);
                buf.sprintf("Keep your hands out of me, %s!", ch.name);

                if (IS_AWAKE(victim)) {
                    do_yell(victim, buf.toString());
                }
                if (!IS_NPC(ch)) {
                    if (IS_NPC(victim)) {
                        check_improve(ch, gsn_push, false, 2);
                        multi_hit(victim, ch, null);
                    }
                }
                return;
            }


            buf.sprintf("{YYou push $N to %s.{x", dir_name[door]);
            act(buf.toString(), ch, null, victim, TO_CHAR, POS_SLEEPING);
            buf.clear();
            buf.sprintf("{Y$n pushes you to %s.{x", dir_name[door]);
            act(buf.toString(), ch, null, victim, TO_VICT, POS_SLEEPING);
            buf.clear();
            buf.sprintf("{Y$n pushes $N to %s.{x", dir_name[door]);
            act(buf.toString(), ch, null, victim, TO_NOTVICT, POS_SLEEPING);
            move_char(victim, door);

            check_improve(ch, gsn_push, true, 1);
        }
    }


    static void do_crecall(@NotNull CHAR_DATA ch) {
        ROOM_INDEX_DATA location;
        var point = ROOM_VNUM_BATTLE;

        if (skill_failure_check(ch, gsn_cabal_recall, false, 0, null)) {
            return;
        }

        if (is_affected(ch, gsn_cabal_recall)) {
            send_to_char("You can't pray now.\n", ch);
        }
        if (ch.desc != null && current_time - ch.last_fight_time < FIGHT_DELAY_TIME) {
            send_to_char("You are too pumped to pray now.\n", ch);
            return;
        }
        if (ch.desc == null && !IS_NPC(ch)) {
            point = ROOM_VNUM_BATTLE;
        }

        act("$n prays upper lord of Battleragers for transportation!", ch, 0, 0, TO_ROOM);

        if ((location = get_room_index(point)) == null) {
            send_to_char("You are completely lost.\n", ch);
            return;
        }

        if (ch.in_room == location) {
            return;
        }

        if (IS_SET(ch.in_room.room_flags, ROOM_NO_RECALL)
                || IS_AFFECTED(ch, AFF_CURSE)
                || IS_RAFFECTED(ch.in_room, AFF_ROOM_CURSE)) {
            send_to_char("The gods have forsaken you.\n", ch);
            return;
        }

        if (ch.fighting != null) {
            send_to_char("You are still fighting!.\n", ch);
            return;
        }

        if (ch.mana < (ch.max_mana * 0.3)) {
            send_to_char("You don't have enough power to pray now.\n", ch);
            return;
        }

        ch.move /= 2;
        ch.mana /= 10;
        act("$n disappears.", ch, null, null, TO_ROOM);
        char_from_room(ch);
        char_to_room(ch, location);
        act("$n appears in the room.", ch, null, null, TO_ROOM);
        do_look(ch, "auto");

        if (ch.pet != null) {
            char_from_room(ch.pet);
            char_to_room(ch.pet, location);
            do_look(ch.pet, "auto");
        }

        var af = new AFFECT_DATA();
        af.type = gsn_cabal_recall;
        af.level = ch.level;
        af.duration = ch.level / 6 + 15;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = 0;
        affect_to_char(ch, af);

    }

    static void do_escape(@NotNull CHAR_DATA ch, String argument) {
        ROOM_INDEX_DATA was_in;
        ROOM_INDEX_DATA now_in;
        int door;

        if (skill_failure_check(ch, gsn_escape, false, 0, "Try flee. It may suit you better.\n")) {
            return;
        }


        if (ch.fighting == null) {
            if (ch.position == POS_FIGHTING) {
                ch.position = POS_STANDING;
            }
            send_to_char("You aren't fighting anyone.\n", ch);
            return;
        }

        var arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.isEmpty()) {
            send_to_char("Escape to what diretion?\n", ch);
            return;
        }

        if (RIDDEN(ch) != null) {
            send_to_char("You can't escape while being ridden.\n", ch);
            return;
        }

        was_in = ch.in_room;
        if ((door = find_exit(ch, arg.toString())) >= 0) {
            EXIT_DATA pexit;

            if ((pexit = was_in.exit[door]) == null
                    || pexit.to_room == null
                    || (IS_SET(pexit.exit_info, EX_CLOSED)
                    && (!IS_AFFECTED(ch, AFF_PASS_DOOR) || IS_SET(pexit.exit_info, EX_NOPASS))
                    && !IS_TRUSTED(ch, ANGEL))
                    || (IS_SET(pexit.exit_info, EX_NOFLEE))
                    || (IS_NPC(ch)
                    && IS_SET(pexit.to_room.room_flags, ROOM_NO_MOB))) {
                send_to_char("Something prevents you to escape that direction.\n", ch);
                return;
            }

            if (number_percent() > get_skill(ch, gsn_escape)) {
                send_to_char("PANIC! You couldn't escape!\n", ch);
                check_improve(ch, gsn_escape, false, 1);
                return;
            }

            check_improve(ch, gsn_escape, true, 1);
            move_char(ch, door);
            if ((now_in = ch.in_room) == was_in) {
                send_to_char("You couldn't reach that direction, try another.\n", ch);
                return;
            }

            ch.in_room = was_in;
            act("$n has escaped!", ch, null, null, TO_ROOM);
            ch.in_room = now_in;

            if (!IS_NPC(ch)) {
                send_to_char("You escaped from combat!  You lose 10 exps.\n", ch);
                gain_exp(ch, -10);
            } else {
                ch.last_fought = null;  /* Once fled, the mob will not go after */
            }

            stop_fighting(ch, true);
        } else {
            send_to_char("You chose the wrong direction.\n", ch);
        }
    }

    static void do_layhands(@NotNull CHAR_DATA ch, String argument) {
        CHAR_DATA victim;

        if (skill_failure_check(ch, gsn_lay_hands, false, 0, "You lack the skill to heal others with touching.\n")) {
            return;
        }

        if ((victim = get_char_room(ch, argument)) == null) {
            send_to_char("You do not see that person here.\n", ch);
            return;
        }

        if (is_affected(ch, gsn_lay_hands)) {
            send_to_char("You can't concentrate enough.\n", ch);
            return;
        }
        WAIT_STATE(ch, gsn_lay_hands.beats);
        var af = new AFFECT_DATA();
        af.type = gsn_lay_hands;
        af.where = TO_AFFECTS;
        af.level = ch.level;
        af.duration = 2;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = 0;
        affect_to_char(ch, af);

        victim.hit = UMIN(victim.hit + ch.level * 2, victim.max_hit);
        update_pos(victim);
        send_to_char("A warm feeling fills your body.\n", victim);
        if (ch != victim) {
            send_to_char("Ok.\n", ch);
        }
        check_improve(ch, gsn_lay_hands, true, 1);

    }

    static boolean mount_success(@NotNull CHAR_DATA ch, CHAR_DATA mount, boolean canattack) {
        int percent;
        int success;

        percent = number_percent() + (ch.level < mount.level ?
                (mount.level - ch.level) * 3 :
                (mount.level - ch.level) * 2);

        if (ch.fighting == null) {
            percent -= 25;
        }

        if (!IS_NPC(ch) && IS_DRUNK(ch)) {
            percent += get_skill(ch, gsn_riding) / 2;
            send_to_char("Due to your being under the influence, riding seems a bit harder...\n", ch);
        }

        success = percent - get_skill(ch, gsn_riding);
        if (success <= 0) { /* Success */
            check_improve(ch, gsn_riding, true, 1);
            return true;
        } else {
            check_improve(ch, gsn_riding, false, 1);
            if (success >= 10 && MOUNTED(ch) == mount) {
                act("You lose control and fall off of $N.", ch, null, mount, TO_CHAR);
                act("$n loses control and falls off of $N.", ch, null, mount, TO_ROOM);
                act("$n loses control and falls off of you.", ch, null, mount, TO_VICT);

                ch.riding = false;
                mount.riding = false;
                if (ch.position > POS_STUNNED) {
                    ch.position = POS_SITTING;
                }

                /*  if (ch.hit > 2) { */
                ch.hit -= 5;
                update_pos(ch);

            }
            if (success >= 40 && canattack) {
                act("$N doesn't like the way you've been treating $M.", ch, null, mount, TO_CHAR);
                act("$N doesn't like the way $n has been treating $M.", ch, null, mount, TO_ROOM);
                act("You don't like the way $n has been treating you.", ch, null, mount, TO_VICT);

                act("$N snarls and attacks you!", ch, null, mount, TO_CHAR);
                act("$N snarls and attacks $n!", ch, null, mount, TO_ROOM);
                act("You snarl and attack $n!", ch, null, mount, TO_VICT);

                damage(mount, ch, number_range(1, mount.level), gsn_kick, DAM_BASH, true);

                /*      multi_hit( mount, ch, TYPE_UNDEFINED ); */
            }
        }
        return false;
    }

    /*
     * It is not finished yet to implement all.
     */

    static void do_mount(@NotNull CHAR_DATA ch, String argument) {

        var arg = new StringBuilder();
        one_argument(argument, arg);

        if (skill_failure_check(ch, gsn_riding, true, 0, "You don't know how to ride!\n")) {
            return;
        }

        if (arg.isEmpty()) {
            send_to_char("Mount what?\n", ch);
            return;
        }

        var mount = get_char_room(ch, arg.toString());
        if (mount == null) {
            send_to_char("You don't see that here.\n", ch);
            return;
        }

        if (!IS_NPC(mount)
                || !IS_SET(mount.act, ACT_RIDEABLE)
                || IS_SET(mount.act, ACT_NOTRACK)) {
            send_to_char("You can't ride that.\n", ch);
            return;
        }

        if (mount.level - 5 > ch.level) {
            send_to_char("That beast is too powerful for you to ride.", ch);
            return;
        }

        if ((mount.mount != null) && (!mount.riding) && (mount.mount != ch)) {
            var buf = new TextBuffer();
            buf.sprintf("%s belongs to %s, not you.\n", mount.short_descr, mount.mount.name);
            send_to_char(buf, ch);
            return;
        }

        if (mount.position < POS_STANDING) {
            send_to_char("Your mount must be standing.\n", ch);
            return;
        }

        if (RIDDEN(mount) != null) {
            send_to_char("This beast is already ridden.\n", ch);
            return;
        } else if (MOUNTED(ch) != null) {
            send_to_char("You are already riding.\n", ch);
            return;
        }

        if (!mount_success(ch, mount, true)) {
            send_to_char("You fail to mount the beast.\n", ch);
            return;
        }

        act("You hop on $N's back.", ch, null, mount, TO_CHAR);
        act("$n hops on $N's back.", ch, null, mount, TO_NOTVICT);
        act("$n hops on your back!", ch, null, mount, TO_VICT);

        ch.mount = mount;
        ch.riding = true;
        mount.mount = ch;
        mount.riding = true;

        /* No sneaky people on mounts */
        affect_strip(ch, gsn_sneak);
        ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_SNEAK);
        affect_strip(ch, gsn_hide);
        ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_HIDE);
        affect_strip(ch, gsn_fade);
        ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_FADE);
        affect_strip(ch, gsn_imp_invis);
        ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_IMP_INVIS);
    }

    static void do_dismount(@NotNull CHAR_DATA ch) {
        var mount = MOUNTED(ch);
        if (mount != null) {
            act("You dismount from $N.", ch, null, mount, TO_CHAR);
            act("$n dismounts from $N.", ch, null, mount, TO_NOTVICT);
            act("$n dismounts from you.", ch, null, mount, TO_VICT);

            ch.riding = false;
            mount.riding = false;
        } else {
            send_to_char("You aren't mounted.\n", ch);
        }
    }

    static int send_arrow(@NotNull CHAR_DATA ch, @NotNull CHAR_DATA victim, @NotNull OBJ_DATA arrow, int door, int chance, int bonus) {
        EXIT_DATA pExit;
        ROOM_INDEX_DATA dest_room;
        int damroll = 0, hitroll = 0;
        int i;
        var sn = arrow.value[0] == WEAPON_SPEAR ? gsn_spear : gsn_arrow;

        for (var paf = arrow.affected; paf != null; paf = paf.next) {
            if (paf.location == APPLY_DAMROLL) {
                damroll += paf.modifier;
            }
            if (paf.location == APPLY_HITROLL) {
                hitroll += paf.modifier;
            }
        }

        dest_room = ch.in_room;
        chance += (hitroll + str_app[get_curr_stat(ch, STAT_STR)].tohit
                + (get_curr_stat(ch, STAT_DEX) - 18)) * 2;
        damroll *= 10;
        for (i = 0; i < 1000; i++) {
            chance -= 10;
            if (victim.in_room == dest_room) {
                if (number_percent() < chance) {
                    if (check_obj_dodge(ch, victim, arrow, chance)) {
                        return 0;
                    }
                    act("$p strikes you!", victim, arrow, null, TO_CHAR);
                    act("Your $p strikes $N!", ch, arrow, victim, TO_CHAR);
                    if (ch.in_room == victim.in_room) {
                        act("$n's $p strikes $N!", ch, arrow, victim, TO_NOTVICT);
                    } else {
                        act("$n's $p strikes $N!", ch, arrow, victim, TO_ROOM);
                        act("$p strikes $n!", victim, arrow, null, TO_ROOM);
                    }
                    if (is_safe(ch, victim) ||
                            (IS_NPC(victim) && IS_SET(victim.act, ACT_NOTRACK))) {
                        act("$p falls from $n doing no visible damage...", victim, arrow, null, TO_ALL);
                        act("$p falls from $n doing no visible damage...", ch, arrow, null, TO_CHAR);
                        obj_to_room(arrow, victim.in_room);
                    } else {
                        int dam;

                        dam = dice(arrow.value[1], arrow.value[2]);
                        dam = number_range(dam, (3 * dam));
                        dam += damroll + bonus +
                                (10 * str_app[get_curr_stat(ch, STAT_STR)].todam);

                        if (IS_WEAPON_STAT(arrow, WEAPON_POISON)) {
                            int level;
                            var poison = affect_find(arrow.affected, gsn_poison);

                            if (poison == null) {
                                level = arrow.level;
                            } else {
                                level = poison.level;
                            }
                            if (!saves_spell(level, victim, DAM_POISON)) {
                                send_to_char("You feel poison coursing through your veins.",
                                        victim);
                                act("$n is poisoned by the venom on $p.",
                                        victim, arrow, null, TO_ROOM);

                                var af = new AFFECT_DATA();
                                af.where = TO_AFFECTS;
                                af.type = gsn_poison;
                                af.level = level * 3 / 4;
                                af.duration = level / 2;
                                af.location = APPLY_STR;
                                af.modifier = -1;
                                af.bitvector = AFF_POISON;
                                affect_join(victim, af);
                            }

                        }
                        if (IS_WEAPON_STAT(arrow, WEAPON_FLAMING)) {
                            act("$n is burned by $p.", victim, arrow, null, TO_ROOM);
                            act("$p sears your flesh.", victim, arrow, null, TO_CHAR);
                            fire_effect(victim, arrow.level, dam, TARGET_CHAR);
                        }
                        if (IS_WEAPON_STAT(arrow, WEAPON_FROST)) {
                            act("$p freezes $n.", victim, arrow, null, TO_ROOM);
                            act("The cold touch of $p surrounds you with ice.",
                                    victim, arrow, null, TO_CHAR);
                            cold_effect(victim, arrow.level, dam, TARGET_CHAR);
                        }
                        if (IS_WEAPON_STAT(arrow, WEAPON_SHOCKING)) {
                            act("$n is struck by lightning from $p.", victim, arrow, null, TO_ROOM);
                            act("You are shocked by $p.", victim, arrow, null, TO_CHAR);
                            shock_effect(victim, arrow.level, dam, TARGET_CHAR);
                        }

                        if (dam > victim.max_hit / 10
                                && number_percent() < 50) {
                            var af = new AFFECT_DATA();
                            af.where = TO_AFFECTS;
                            af.type = sn;
                            af.level = ch.level;
                            af.duration = -1;
                            af.location = APPLY_HITROLL;
                            af.modifier = -(dam / 20);
                            if (IS_NPC(victim)) {
                                af.bitvector = 0;
                            } else {
                                af.bitvector = AFF_CORRUPTION;
                            }

                            affect_join(victim, af);

                            obj_to_char(arrow, victim);
                            equip_char(victim, arrow, WEAR_STUCK_IN);
                        } else {
                            obj_to_room(arrow, victim.in_room);
                        }

                        damage(ch, victim, dam, sn, DAM_PIERCE, true);
                        path_to_track(ch, victim, door);

                    }
                    return 1;
                } else {
                    obj_to_room(arrow, victim.in_room);
                    act("$p sticks in the ground at your feet!", victim, arrow, null, TO_ALL);
                    return 0;
                }
            }
            pExit = dest_room.exit[door];
            if (pExit == null) {
                break;
            } else {
                dest_room = pExit.to_room;
                if (dest_room.people != null) {
                    var buf = new TextBuffer();
                    buf.sprintf("$p sails into the room from the %s!", dir_name[rev_dir[door]]);
                    act(buf.toString(), dest_room.people, arrow, null, TO_ALL);
                }
            }
        }
        return 0;
    }


    static void do_shoot(@NotNull CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        OBJ_DATA wield;
        OBJ_DATA arrow;
        int chance, direction;
        var range = (ch.level / 10) + 1;

        if (skill_failure_check(ch, gsn_bow, false, 0, "You don't know how to shoot.\n")) {
            return;
        }

        var arg1 = new StringBuilder();
        var arg2 = new StringBuilder();
        argument = one_argument(argument, arg1);
        one_argument(argument, arg2);

        if (arg1.isEmpty() || arg2.isEmpty()) {
            send_to_char("Shoot what diretion and whom?\n", ch);
            return;
        }

        if (ch.fighting != null) {
            send_to_char("You cannot concentrate on shooting arrows.\n", ch);
            return;
        }

        direction = find_exit(ch, arg1.toString());

        if (direction < 0 || direction > 5) {
            return;
        }

        if ((victim = find_char(ch, arg2.toString(), direction, range)) == null) {
            send_to_char("You can't see that one.\n", ch);
            return;
        }

        if (!IS_NPC(victim) && victim.desc == null) {
            send_to_char("You can't do that.\n", ch);
            return;
        }

        if (victim == ch) {
            send_to_char("That's pointless.\n", ch);
            return;
        }

        if (is_at_cabal_area(ch) || is_at_cabal_area(victim)) {
            send_to_char("It is not allowed near cabal areas.\n", ch);
            return;
        }

        if (is_safe(ch, victim)) {
            var buf = new TextBuffer();
            buf.sprintf("Gods protect %s.\n", victim.name);
            send_to_char(buf, ch);
            return;
        }

        if ((wield = get_weapon_char(ch, WEAPON_BOW)) == null) {
            send_to_char("You need a bow to shoot!\n", ch);
            return;
        }

        if ((arrow = get_weapon_char(ch, WEAPON_ARROW)) == null) {
            send_to_char("You need an arrow holding for your ammunition!\n", ch);
            return;
        }

        WAIT_STATE(ch, gsn_bow.beats);

        chance = (get_skill(ch, gsn_bow) - 50) * 2;
        if (ch.position == POS_SLEEPING) {
            chance += 40;
        }
        if (ch.position == POS_RESTING) {
            chance += 10;
        }
        if (victim.position == POS_FIGHTING) {
            chance -= 40;
        }
        chance += GET_HITROLL(ch);

        var buf = new TextBuffer();
        buf.sprintf("You shoot $p to %s.", dir_name[direction]);
        act(buf.toString(), ch, arrow, null, TO_CHAR);
        buf.sprintf("$n shoots $p to %s.", dir_name[direction]);
        act(buf.toString(), ch, arrow, null, TO_ROOM);

        obj_from_char(arrow);
        send_arrow(ch, victim, arrow, direction, chance, dice(wield.value[1], wield.value[2]));
        check_improve(ch, gsn_bow, true, 1);
    }


    static String find_way(@NotNull CHAR_DATA ch, ROOM_INDEX_DATA rstart, ROOM_INDEX_DATA rend) {
        int direction;
        EXIT_DATA pExit;
        int i;

        var buf = new StringBuilder("Find: ");
        for (i = 0; i < 65535; i++) {
            if ((rend == rstart)) {
                return buf.toString();
            }
            if ((direction = find_path(rstart.vnum, rend.vnum, ch, -40000, false)) == -1) {
                buf.append(" BUGGY");
                return buf.toString();
            }
            if (direction < 0 || direction > 5) {
                buf.append(" VERY BUGGY");
                return buf.toString();
            }
            buf.append(dir_name[direction].charAt(0));
            /* find target room */
            pExit = rstart.exit[direction];
            if (pExit == null) {
                buf.append(" VERY VERY BUGGY");
                return buf.toString();
            } else {
                rstart = pExit.to_room;
            }
        }
        return buf.toString();
    }

    static void do_human(@NotNull CHAR_DATA ch) {
        if (ch.clazz != Clazz.VAMPIRE) {
            send_to_char("Huh?\n", ch);
            return;
        }

        if (!IS_VAMPIRE(ch)) {
            send_to_char("You are already a human.\n", ch);
            return;
        }

        affect_strip(ch, gsn_vampire);
        ch.act = REMOVE_BIT(ch.act, PLR_VAMPIRE);
        send_to_char("You return to your original size.\n", ch);
    }


    static void do_throw_spear(@NotNull CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        OBJ_DATA spear;
        int chance, direction;
        var range = (ch.level / 10) + 1;

        if (skill_failure_check(ch, gsn_spear, true, 0, "You don't know how to throw a spear.\n")) {
            return;
        }

        var arg1 = new StringBuilder();
        var arg2 = new StringBuilder();
        argument = one_argument(argument, arg1);
        one_argument(argument, arg2);

        if (arg1.isEmpty() || arg2.isEmpty()) {
            send_to_char("Throw spear what diretion and whom?\n", ch);
            return;
        }

        if (ch.fighting != null) {
            send_to_char("You cannot concentrate on throwing spear.\n", ch);
            return;
        }

        direction = find_exit(ch, arg1.toString());

        if (direction < 0 || direction > 5) {
            send_to_char("Throw which direction and whom?\n", ch);
            return;
        }

        if ((victim = find_char(ch, arg2.toString(), direction, range)) == null) {
            send_to_char("You can't see that one.\n", ch);
            return;
        }

        if (!IS_NPC(victim) && victim.desc == null) {
            send_to_char("You can't do that.\n", ch);
            return;
        }

        if (victim == ch) {
            send_to_char("That's pointless.\n", ch);
            return;
        }

        if (is_at_cabal_area(ch) || is_at_cabal_area(victim)) {
            send_to_char("It is not allowed near cabal areas.\n", ch);
            return;
        }

        if (is_safe(ch, victim)) {
            var buf = new TextBuffer();
            buf.sprintf("Gods protect %s.\n", victim.name);
            send_to_char(buf, ch);
            return;
        }

        if ((spear = get_weapon_char(ch, WEAPON_SPEAR)) == null) {
            send_to_char("You need a spear to throw!\n", ch);
            return;
        }


        WAIT_STATE(ch, gsn_spear.beats);

        chance = (get_skill(ch, gsn_spear) - 50) * 2;
        if (ch.position == POS_SLEEPING) {
            chance += 40;
        }
        if (ch.position == POS_RESTING) {
            chance += 10;
        }
        if (victim.position == POS_FIGHTING) {
            chance -= 40;
        }
        chance += GET_HITROLL(ch);
        var buf = new TextBuffer();
        buf.sprintf("You throw $p to %s.", dir_name[direction]);
        act(buf.toString(), ch, spear, null, TO_CHAR);
        buf.sprintf("$n throws $p to %s.", dir_name[direction]);
        act(buf.toString(), ch, spear, null, TO_ROOM);

        obj_from_char(spear);
        send_arrow(ch, victim, spear, direction, chance, dice(spear.value[1], spear.value[2]));
        check_improve(ch, gsn_spear, true, 1);
    }

    static OBJ_DATA get_weapon_char(@NotNull CHAR_DATA ch, int wType) {
        for (OBJ_DATA obj = ch.carrying; obj != null; obj = obj.next_content) {
            if ((obj.wear_loc == WEAR_LEFT || obj.wear_loc == WEAR_RIGHT || obj.wear_loc == WEAR_BOTH)) {
                if (obj.value[0] == wType) {
                    return obj;
                }
            }
        }
        return null;
    }

}
