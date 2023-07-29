package net.sf.nightworks;

import net.sf.nightworks.util.TextBuffer;

import static net.sf.nightworks.ActComm.add_follower;
import static net.sf.nightworks.ActComm.do_say;
import static net.sf.nightworks.ActComm.do_split;
import static net.sf.nightworks.ActComm.do_yell;
import static net.sf.nightworks.ActComm.is_same_group;
import static net.sf.nightworks.ActMove.dir_name;
import static net.sf.nightworks.ActMove.do_mount;
import static net.sf.nightworks.ActMove.find_exit;
import static net.sf.nightworks.ActMove.get_weapon_char;
import static net.sf.nightworks.ActMove.move_char;
import static net.sf.nightworks.ActSkill.check_improve;
import static net.sf.nightworks.ActWiz.wiznet;
import static net.sf.nightworks.Comm.act;
import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.Const.attack_table;
import static net.sf.nightworks.Const.liq_table;
import static net.sf.nightworks.Const.str_app;
import static net.sf.nightworks.DB.bug;
import static net.sf.nightworks.DB.clone_object;
import static net.sf.nightworks.DB.create_mobile;
import static net.sf.nightworks.DB.create_object;
import static net.sf.nightworks.DB.get_obj_index;
import static net.sf.nightworks.DB.get_room_index;
import static net.sf.nightworks.DB.number_bits;
import static net.sf.nightworks.DB.number_fuzzy;
import static net.sf.nightworks.DB.number_percent;
import static net.sf.nightworks.DB.number_range;
import static net.sf.nightworks.DB.time_info;
import static net.sf.nightworks.Fight.is_safe;
import static net.sf.nightworks.Fight.multi_hit;
import static net.sf.nightworks.Handler.affect_join;
import static net.sf.nightworks.Handler.affect_loc_name;
import static net.sf.nightworks.Handler.affect_strip;
import static net.sf.nightworks.Handler.affect_to_char;
import static net.sf.nightworks.Handler.affect_to_obj;
import static net.sf.nightworks.Handler.can_carry_n;
import static net.sf.nightworks.Handler.can_carry_w;
import static net.sf.nightworks.Handler.can_drop_obj;
import static net.sf.nightworks.Handler.can_see;
import static net.sf.nightworks.Handler.can_see_obj;
import static net.sf.nightworks.Handler.can_see_room;
import static net.sf.nightworks.Handler.cant_float;
import static net.sf.nightworks.Handler.char_to_room;
import static net.sf.nightworks.Handler.count_worn;
import static net.sf.nightworks.Handler.create_money;
import static net.sf.nightworks.Handler.deduct_cost;
import static net.sf.nightworks.Handler.equip_char;
import static net.sf.nightworks.Handler.extra_bit_name;
import static net.sf.nightworks.Handler.extract_obj;
import static net.sf.nightworks.Handler.get_char_room;
import static net.sf.nightworks.Handler.get_char_world;
import static net.sf.nightworks.Handler.get_curr_stat;
import static net.sf.nightworks.Handler.get_eq_char;
import static net.sf.nightworks.Handler.get_hold_char;
import static net.sf.nightworks.Handler.get_obj_carry;
import static net.sf.nightworks.Handler.get_obj_here;
import static net.sf.nightworks.Handler.get_obj_list;
import static net.sf.nightworks.Handler.get_obj_number;
import static net.sf.nightworks.Handler.get_obj_wear;
import static net.sf.nightworks.Handler.get_obj_weight;
import static net.sf.nightworks.Handler.get_skill;
import static net.sf.nightworks.Handler.get_true_weight;
import static net.sf.nightworks.Handler.get_weapon_skill;
import static net.sf.nightworks.Handler.get_weapon_sn;
import static net.sf.nightworks.Handler.get_wield_char;
import static net.sf.nightworks.Handler.is_affected;
import static net.sf.nightworks.Handler.is_name;
import static net.sf.nightworks.Handler.item_type_name;
import static net.sf.nightworks.Handler.max_can_wear;
import static net.sf.nightworks.Handler.may_float;
import static net.sf.nightworks.Handler.obj_from_char;
import static net.sf.nightworks.Handler.obj_from_obj;
import static net.sf.nightworks.Handler.obj_from_room;
import static net.sf.nightworks.Handler.obj_to_char;
import static net.sf.nightworks.Handler.obj_to_obj;
import static net.sf.nightworks.Handler.obj_to_room;
import static net.sf.nightworks.Handler.skill_failure_check;
import static net.sf.nightworks.Handler.unequip_char;
import static net.sf.nightworks.Interp.multiply_argument;
import static net.sf.nightworks.Interp.number_argument;
import static net.sf.nightworks.Magic.check_dispel;
import static net.sf.nightworks.Magic.obj_cast_spell;
import static net.sf.nightworks.Magic.spell_enchant_weapon;
import static net.sf.nightworks.Nightworks.ACT_IS_CHANGER;
import static net.sf.nightworks.Nightworks.ACT_PET;
import static net.sf.nightworks.Nightworks.ACT_RIDEABLE;
import static net.sf.nightworks.Nightworks.AFFECT_DATA;
import static net.sf.nightworks.Nightworks.AFF_CHARM;
import static net.sf.nightworks.Nightworks.AFF_POISON;
import static net.sf.nightworks.Nightworks.AFF_SLEEP;
import static net.sf.nightworks.Nightworks.AFF_SNEAK;
import static net.sf.nightworks.Nightworks.APPLY_NONE;
import static net.sf.nightworks.Nightworks.AREA_HOMETOWN;
import static net.sf.nightworks.Nightworks.CABAL_BATTLE;
import static net.sf.nightworks.Nightworks.CABAL_KNIGHT;
import static net.sf.nightworks.Nightworks.CAN_WEAR;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.COMM_NOCHANNELS;
import static net.sf.nightworks.Nightworks.COMM_NOSHOUT;
import static net.sf.nightworks.Nightworks.COMM_NOTELL;
import static net.sf.nightworks.Nightworks.COND_DRUNK;
import static net.sf.nightworks.Nightworks.COND_FULL;
import static net.sf.nightworks.Nightworks.COND_HUNGER;
import static net.sf.nightworks.Nightworks.COND_THIRST;
import static net.sf.nightworks.Nightworks.CONT_CLOSED;
import static net.sf.nightworks.Nightworks.CONT_PUT_ON;
import static net.sf.nightworks.Nightworks.CONT_ST_LIMITED;
import static net.sf.nightworks.Nightworks.CON_PLAYING;
import static net.sf.nightworks.Nightworks.DAM_BASH;
import static net.sf.nightworks.Nightworks.DESCRIPTOR_DATA;
import static net.sf.nightworks.Nightworks.EXIT_DATA;
import static net.sf.nightworks.Nightworks.FIGHT_DELAY_TIME;
import static net.sf.nightworks.Nightworks.IS_AFFECTED;
import static net.sf.nightworks.Nightworks.IS_AWAKE;
import static net.sf.nightworks.Nightworks.IS_EVIL;
import static net.sf.nightworks.Nightworks.IS_GOOD;
import static net.sf.nightworks.Nightworks.IS_IMMORTAL;
import static net.sf.nightworks.Nightworks.IS_NEUTRAL;
import static net.sf.nightworks.Nightworks.IS_NPC;
import static net.sf.nightworks.Nightworks.IS_OBJ_STAT;
import static net.sf.nightworks.Nightworks.IS_SET;
import static net.sf.nightworks.Nightworks.IS_WATER;
import static net.sf.nightworks.Nightworks.IS_WEAPON_STAT;
import static net.sf.nightworks.Nightworks.ITEM_ANTI_EVIL;
import static net.sf.nightworks.Nightworks.ITEM_ANTI_GOOD;
import static net.sf.nightworks.Nightworks.ITEM_ANTI_NEUTRAL;
import static net.sf.nightworks.Nightworks.ITEM_ARMOR;
import static net.sf.nightworks.Nightworks.ITEM_BLESS;
import static net.sf.nightworks.Nightworks.ITEM_BURIED;
import static net.sf.nightworks.Nightworks.ITEM_BURN_PROOF;
import static net.sf.nightworks.Nightworks.ITEM_CONTAINER;
import static net.sf.nightworks.Nightworks.ITEM_CORPSE_NPC;
import static net.sf.nightworks.Nightworks.ITEM_CORPSE_PC;
import static net.sf.nightworks.Nightworks.ITEM_DRINK_CON;
import static net.sf.nightworks.Nightworks.ITEM_FOOD;
import static net.sf.nightworks.Nightworks.ITEM_FOUNTAIN;
import static net.sf.nightworks.Nightworks.ITEM_HAD_TIMER;
import static net.sf.nightworks.Nightworks.ITEM_HOLD;
import static net.sf.nightworks.Nightworks.ITEM_INVENTORY;
import static net.sf.nightworks.Nightworks.ITEM_LIGHT;
import static net.sf.nightworks.Nightworks.ITEM_MELT_DROP;
import static net.sf.nightworks.Nightworks.ITEM_MONEY;
import static net.sf.nightworks.Nightworks.ITEM_NOREMOVE;
import static net.sf.nightworks.Nightworks.ITEM_NOSELL;
import static net.sf.nightworks.Nightworks.ITEM_NO_SAC;
import static net.sf.nightworks.Nightworks.ITEM_PILL;
import static net.sf.nightworks.Nightworks.ITEM_POTION;
import static net.sf.nightworks.Nightworks.ITEM_SCROLL;
import static net.sf.nightworks.Nightworks.ITEM_SELL_EXTRACT;
import static net.sf.nightworks.Nightworks.ITEM_STAFF;
import static net.sf.nightworks.Nightworks.ITEM_TAKE;
import static net.sf.nightworks.Nightworks.ITEM_TATTOO;
import static net.sf.nightworks.Nightworks.ITEM_TRASH;
import static net.sf.nightworks.Nightworks.ITEM_WAND;
import static net.sf.nightworks.Nightworks.ITEM_WEAPON;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_ABOUT;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_ARMS;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_BODY;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_FEET;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_FINGER;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_FLOAT;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_HANDS;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_HEAD;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_LEGS;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_NECK;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_SHIELD;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_TATTOO;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_WAIST;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_WRIST;
import static net.sf.nightworks.Nightworks.ITEM_WIELD;
import static net.sf.nightworks.Nightworks.LEVEL_IMMORTAL;
import static net.sf.nightworks.Nightworks.MAX_LEVEL;
import static net.sf.nightworks.Nightworks.MAX_SKILL;
import static net.sf.nightworks.Nightworks.MAX_TRADE;
import static net.sf.nightworks.Nightworks.MOUNTED;
import static net.sf.nightworks.Nightworks.MPROG_BRIBE;
import static net.sf.nightworks.Nightworks.MPROG_GIVE;
import static net.sf.nightworks.Nightworks.OBJ_DATA;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_BATTLE_THRONE;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_CHAOS_ALTAR;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_COINS;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_GOLD_ONE;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_GOLD_SOME;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_GRAVE_STONE;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_HUNTER_ALTAR;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_INVADER_SKULL;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_KNIGHT_ALTAR;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_LIONS_ALTAR;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_PIT;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_POTION_VIAL;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_RULER_STAND;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SHALAFI_ALTAR;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SILVER_ONE;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SILVER_SOME;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_STEAK;
import static net.sf.nightworks.Nightworks.OPROG_DROP;
import static net.sf.nightworks.Nightworks.OPROG_GET;
import static net.sf.nightworks.Nightworks.OPROG_GIVE;
import static net.sf.nightworks.Nightworks.OPROG_SAC;
import static net.sf.nightworks.Nightworks.PLR_AUTOSPLIT;
import static net.sf.nightworks.Nightworks.PLR_CANLOOT;
import static net.sf.nightworks.Nightworks.PLR_WANTED;
import static net.sf.nightworks.Nightworks.POS_DEAD;
import static net.sf.nightworks.Nightworks.POS_FIGHTING;
import static net.sf.nightworks.Nightworks.POS_SLEEPING;
import static net.sf.nightworks.Nightworks.POS_STANDING;
import static net.sf.nightworks.Nightworks.PULSE_VIOLENCE;
import static net.sf.nightworks.Nightworks.REMOVE_BIT;
import static net.sf.nightworks.Nightworks.ROOM_BANK;
import static net.sf.nightworks.Nightworks.ROOM_INDEX_DATA;
import static net.sf.nightworks.Nightworks.ROOM_PET_SHOP;
import static net.sf.nightworks.Nightworks.SECT_AIR;
import static net.sf.nightworks.Nightworks.SECT_CITY;
import static net.sf.nightworks.Nightworks.SECT_DESERT;
import static net.sf.nightworks.Nightworks.SECT_FIELD;
import static net.sf.nightworks.Nightworks.SECT_FOREST;
import static net.sf.nightworks.Nightworks.SECT_INSIDE;
import static net.sf.nightworks.Nightworks.SECT_WATER_NOSWIM;
import static net.sf.nightworks.Nightworks.SECT_WATER_SWIM;
import static net.sf.nightworks.Nightworks.SET_BIT;
import static net.sf.nightworks.Nightworks.SHOP_DATA;
import static net.sf.nightworks.Nightworks.SIZE_LARGE;
import static net.sf.nightworks.Nightworks.STAT_STR;
import static net.sf.nightworks.Nightworks.TAR_CHAR_DEFENSIVE;
import static net.sf.nightworks.Nightworks.TAR_CHAR_OFFENSIVE;
import static net.sf.nightworks.Nightworks.TAR_CHAR_SELF;
import static net.sf.nightworks.Nightworks.TAR_IGNORE;
import static net.sf.nightworks.Nightworks.TO_AFFECTS;
import static net.sf.nightworks.Nightworks.TO_ALL;
import static net.sf.nightworks.Nightworks.TO_CHAR;
import static net.sf.nightworks.Nightworks.TO_NOTVICT;
import static net.sf.nightworks.Nightworks.TO_ROOM;
import static net.sf.nightworks.Nightworks.TO_VICT;
import static net.sf.nightworks.Nightworks.TO_WEAPON;
import static net.sf.nightworks.Nightworks.UMAX;
import static net.sf.nightworks.Nightworks.UMIN;
import static net.sf.nightworks.Nightworks.URANGE;
import static net.sf.nightworks.Nightworks.WAIT_STATE;
import static net.sf.nightworks.Nightworks.WEAPON_ARROW;
import static net.sf.nightworks.Nightworks.WEAPON_AXE;
import static net.sf.nightworks.Nightworks.WEAPON_BOW;
import static net.sf.nightworks.Nightworks.WEAPON_DAGGER;
import static net.sf.nightworks.Nightworks.WEAPON_EXOTIC;
import static net.sf.nightworks.Nightworks.WEAPON_FLAIL;
import static net.sf.nightworks.Nightworks.WEAPON_FLAMING;
import static net.sf.nightworks.Nightworks.WEAPON_FROST;
import static net.sf.nightworks.Nightworks.WEAPON_HOLY;
import static net.sf.nightworks.Nightworks.WEAPON_LANCE;
import static net.sf.nightworks.Nightworks.WEAPON_MACE;
import static net.sf.nightworks.Nightworks.WEAPON_POISON;
import static net.sf.nightworks.Nightworks.WEAPON_POLEARM;
import static net.sf.nightworks.Nightworks.WEAPON_SHARP;
import static net.sf.nightworks.Nightworks.WEAPON_SHOCKING;
import static net.sf.nightworks.Nightworks.WEAPON_SPEAR;
import static net.sf.nightworks.Nightworks.WEAPON_SWORD;
import static net.sf.nightworks.Nightworks.WEAPON_TWO_HANDS;
import static net.sf.nightworks.Nightworks.WEAPON_VAMPIRIC;
import static net.sf.nightworks.Nightworks.WEAPON_VORPAL;
import static net.sf.nightworks.Nightworks.WEAPON_WHIP;
import static net.sf.nightworks.Nightworks.WEAR_ABOUT;
import static net.sf.nightworks.Nightworks.WEAR_ARMS;
import static net.sf.nightworks.Nightworks.WEAR_BODY;
import static net.sf.nightworks.Nightworks.WEAR_BOTH;
import static net.sf.nightworks.Nightworks.WEAR_FEET;
import static net.sf.nightworks.Nightworks.WEAR_FINGER;
import static net.sf.nightworks.Nightworks.WEAR_FLOAT;
import static net.sf.nightworks.Nightworks.WEAR_HANDS;
import static net.sf.nightworks.Nightworks.WEAR_HEAD;
import static net.sf.nightworks.Nightworks.WEAR_LEFT;
import static net.sf.nightworks.Nightworks.WEAR_LEGS;
import static net.sf.nightworks.Nightworks.WEAR_NECK;
import static net.sf.nightworks.Nightworks.WEAR_NONE;
import static net.sf.nightworks.Nightworks.WEAR_RIGHT;
import static net.sf.nightworks.Nightworks.WEAR_STUCK_IN;
import static net.sf.nightworks.Nightworks.WEAR_TATTOO;
import static net.sf.nightworks.Nightworks.WEAR_WAIST;
import static net.sf.nightworks.Nightworks.WEAR_WRIST;
import static net.sf.nightworks.Nightworks.WEIGHT_MULT;
import static net.sf.nightworks.Nightworks.WIZ_SACCING;
import static net.sf.nightworks.Nightworks.atoi;
import static net.sf.nightworks.Nightworks.char_list;
import static net.sf.nightworks.Nightworks.current_time;
import static net.sf.nightworks.Nightworks.descriptor_list;
import static net.sf.nightworks.Nightworks.get_carry_weight;
import static net.sf.nightworks.Skill.gsn_arrow;
import static net.sf.nightworks.Skill.gsn_bury;
import static net.sf.nightworks.Skill.gsn_butcher;
import static net.sf.nightworks.Skill.gsn_doppelganger;
import static net.sf.nightworks.Skill.gsn_enchant_sword;
import static net.sf.nightworks.Skill.gsn_enchant_weapon;
import static net.sf.nightworks.Skill.gsn_envenom;
import static net.sf.nightworks.Skill.gsn_haggle;
import static net.sf.nightworks.Skill.gsn_herbs;
import static net.sf.nightworks.Skill.gsn_lore;
import static net.sf.nightworks.Skill.gsn_plague;
import static net.sf.nightworks.Skill.gsn_poison;
import static net.sf.nightworks.Skill.gsn_scrolls;
import static net.sf.nightworks.Skill.gsn_spear;
import static net.sf.nightworks.Skill.gsn_staves;
import static net.sf.nightworks.Skill.gsn_steal;
import static net.sf.nightworks.Skill.gsn_wands;
import static net.sf.nightworks.Skill.gsn_wanted;
import static net.sf.nightworks.Tables.cabal_table;
import static net.sf.nightworks.Update.gain_condition;
import static net.sf.nightworks.util.TextUtils.is_number;
import static net.sf.nightworks.util.TextUtils.one_argument;
import static net.sf.nightworks.util.TextUtils.smash_tilde;
import static net.sf.nightworks.util.TextUtils.str_cmp;
import static net.sf.nightworks.util.TextUtils.str_prefix;

class ActObj {
/* RT part of the corpse looting code */

    private static boolean _loot = true;

    static boolean can_loot(CHAR_DATA ch, OBJ_DATA obj) {
        CHAR_DATA owner, wch;
        if (_loot) {
            return true;
        }
        if (IS_IMMORTAL(ch)) {
            return true;
        }

        if (obj.owner == null || obj.owner == null) {
            return true;
        }

        owner = null;
        for (wch = char_list; wch != null; wch = wch.next) {
            if (!str_cmp(wch.name, obj.owner)) {
                owner = wch;
            }
        }

        if (owner == null) {
            return true;
        }

        if (!str_cmp(ch.name, owner.name)) {
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (!IS_NPC(owner) && IS_SET(owner.act, PLR_CANLOOT)) {
            return true;
        }

        return is_same_group(ch, owner);

    }


    static void get_obj(CHAR_DATA ch, OBJ_DATA obj, OBJ_DATA container) {
        /* variables for AUTOSPLIT */
        CHAR_DATA gch;
        int members;


        if (!CAN_WEAR(obj, ITEM_TAKE)) {
            send_to_char("You can't take that.\n", ch);
            return;
        }

        if (obj.pIndexData.limit != -1) {
            if ((IS_OBJ_STAT(obj, ITEM_ANTI_EVIL) && IS_EVIL(ch))
                    || (IS_OBJ_STAT(obj, ITEM_ANTI_GOOD) && IS_GOOD(ch))
                    || (IS_OBJ_STAT(obj, ITEM_ANTI_NEUTRAL) && IS_NEUTRAL(ch))) {
                act("You are zapped by $p and drop it.", ch, obj, null, TO_CHAR);
                act("$n is zapped by $p and drops it.", ch, obj, null, TO_ROOM);
                return;
            }
        }

        if (ch.carry_number + get_obj_number(obj) > can_carry_n(ch)) {
            act("$d: you can't carry that many items.",
                    ch, null, obj.name, TO_CHAR);
            return;
        }


        if (get_carry_weight(ch) + get_obj_weight(obj) > can_carry_w(ch)) {
            act("$d: you can't carry that much weight.",
                    ch, null, obj.name, TO_CHAR);
            return;
        }

        if (obj.in_room != null) {
            for (gch = obj.in_room.people; gch != null; gch = gch.next_in_room) {
                if (gch.on == obj) {
                    act("$N appears to be using $p.",
                            ch, obj, gch, TO_CHAR);
                    return;
                }
            }
        }


        if (container != null) {
            if (container.pIndexData.vnum == OBJ_VNUM_INVADER_SKULL
                    || container.pIndexData.vnum == OBJ_VNUM_RULER_STAND
                    || container.pIndexData.vnum == OBJ_VNUM_BATTLE_THRONE
                    || container.pIndexData.vnum == OBJ_VNUM_CHAOS_ALTAR
                    || container.pIndexData.vnum == OBJ_VNUM_SHALAFI_ALTAR
                    || container.pIndexData.vnum == OBJ_VNUM_KNIGHT_ALTAR
                    || container.pIndexData.vnum == OBJ_VNUM_LIONS_ALTAR
                    || container.pIndexData.vnum == OBJ_VNUM_HUNTER_ALTAR) {
                DESCRIPTOR_DATA d;

                act("You get $p from $P.", ch, obj, container, TO_CHAR);
                if (!IS_AFFECTED(ch, AFF_SNEAK)) {
                    act("$n gets $p from $P.", ch, obj, container, TO_ROOM);
                }
                obj_from_obj(obj);
                act("$p fades to black, then dissapears!", ch, container, null, TO_ROOM);
                act("$p fades to black, then dissapears!", ch, container, null, TO_CHAR);
                extract_obj(container);
                obj_to_char(obj, ch);

                for (d = descriptor_list; d != null; d = d.next) {
                    if (d.connected == CON_PLAYING && d.character != null && cabal_table[d.character.cabal].obj_ptr == obj) {
                        act("{gYou feel a shudder in your Cabal Power!{x", d.character, null, null, TO_CHAR, POS_DEAD);
                    }
                }

                if (IS_SET(obj.progtypes, OPROG_GET)) {
                    obj.pIndexData.oprogs.get_prog.run(obj, ch);
                }
                return;
            }

            if (container.pIndexData.vnum == OBJ_VNUM_PIT
                    && !CAN_WEAR(container, ITEM_TAKE)
                    && !IS_OBJ_STAT(obj, ITEM_HAD_TIMER)) {
                obj.timer = 0;
            }
            act("You get $p from $P.", ch, obj, container, TO_CHAR);
            if (!IS_AFFECTED(ch, AFF_SNEAK)) {
                act("$n gets $p from $P.", ch, obj, container, TO_ROOM);
            }
            obj.extra_flags = REMOVE_BIT(obj.extra_flags, ITEM_HAD_TIMER);
            obj_from_obj(obj);
        } else {
            act("You get $p.", ch, obj, container, TO_CHAR);
            if (!IS_AFFECTED(ch, AFF_SNEAK)) {
                act("$n gets $p.", ch, obj, container, TO_ROOM);
            }
            obj_from_room(obj);
        }

        if (obj.item_type == ITEM_MONEY) {
            ch.silver += obj.value[0];
            ch.gold += obj.value[1];
            if (IS_SET(ch.act, PLR_AUTOSPLIT)) { /* AUTOSPLIT code */
                members = 0;
                for (gch = ch.in_room.people; gch != null; gch = gch.next_in_room) {
                    if (!IS_AFFECTED(gch, AFF_CHARM) && is_same_group(gch, ch)) {
                        members++;
                    }
                }

                if (members > 1 && (obj.value[0] > 1 || obj.value[1] != 0)) {
                    do_split(ch, obj.value[0] + " " + obj.value[1]);
                }
            }

            extract_obj(obj);
        } else {
            obj_to_char(obj, ch);
            if (IS_SET(obj.progtypes, OPROG_GET)) {
                obj.pIndexData.oprogs.get_prog.run(obj, ch);
            }
        }

    }


    static void do_get(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        OBJ_DATA obj_next;
        OBJ_DATA container;
        boolean found;

        StringBuilder arg1b = new StringBuilder();
        StringBuilder arg2b = new StringBuilder();
        argument = one_argument(argument, arg1b);
        argument = one_argument(argument, arg2b);

        String arg1 = arg1b.toString();
        String arg2 = arg2b.toString();
        if (!str_cmp(arg2, "from")) {
            one_argument(argument, arg2b);
        }
        arg2 = arg2b.toString();

        /* Get type. */
        if (arg1.isEmpty()) {
            send_to_char("Get what?\n", ch);
            return;
        }

        if (is_number(arg1)) {
            int amount, weight, gold = 0, silver = 0;

            amount = atoi(arg1);
            if (amount <= 0
                    || (str_cmp(arg2, "coins") && str_cmp(arg2, "coin") &&
                    str_cmp(arg2, "gold") && str_cmp(arg2, "silver"))) {
                send_to_char("Usage: <get> <number> <silver|gold|coin|coins>\n", ch);
                return;
            }

            if (!str_cmp(arg2, "gold")) {
                weight = amount * 2 / 5;
            } else {
                weight = amount / 10;
            }

            if (get_carry_weight(ch) + weight > can_carry_w(ch)) {
                act("You can't carry that much weight.", ch, null, null, TO_CHAR);
                return;
            }


            for (obj = ch.in_room.contents; obj != null; obj = obj_next) {
                obj_next = obj.next_content;

                switch (obj.pIndexData.vnum) {
                    case OBJ_VNUM_SILVER_ONE -> silver += 1;
                    case OBJ_VNUM_GOLD_ONE -> gold += 1;
                    case OBJ_VNUM_SILVER_SOME -> silver += obj.value[0];
                    case OBJ_VNUM_GOLD_SOME -> gold += obj.value[1];
                    case OBJ_VNUM_COINS -> {
                        silver += obj.value[0];
                        gold += obj.value[1];
                    }
                }
            }

            if ((!str_cmp(arg2, "gold") && amount > gold)
                    || (str_cmp(arg2, "gold") && amount > silver)) {
                send_to_char("There's not that much coins there.\n", ch);
                return;
            }

            if (!str_cmp(arg2, "gold")) {
                gold = amount;
                silver = 0;
            } else {
                silver = amount;
                gold = 0;
            }

            for (obj = ch.in_room.contents; obj != null; obj = obj_next) {
                obj_next = obj.next_content;

                switch (obj.pIndexData.vnum) {
                    case OBJ_VNUM_SILVER_ONE -> {
                        if (silver != 0) {
                            silver -= 1;
                            extract_obj(obj);
                        }
                    }
                    case OBJ_VNUM_GOLD_ONE -> {
                        if (gold != 0) {
                            gold -= 1;
                            extract_obj(obj);
                        }
                    }
                    case OBJ_VNUM_SILVER_SOME -> {
                        if (silver != 0) {
                            if (silver >= obj.value[0]) {
                                silver -= obj.value[0];
                                extract_obj(obj);
                            } else {
                                obj.value[0] -= silver;
                                silver = 0;
                            }
                        }
                    }
                    case OBJ_VNUM_GOLD_SOME -> {
                        if (gold != 0) {
                            if (gold >= obj.value[1]) {
                                gold -= obj.value[1];
                                extract_obj(obj);
                            } else {
                                obj.value[1] -= gold;
                                gold = 0;
                            }
                        }
                    }
                    case OBJ_VNUM_COINS -> {
                        if (silver != 0) {
                            if (silver >= obj.value[0]) {
                                silver -= obj.value[0];
                                gold = obj.value[1];
                                extract_obj(obj);
                                obj = create_money(gold, 0);
                                obj_to_room(obj, ch.in_room);
                                gold = 0;
                            } else {
                                obj.value[0] -= silver;
                                silver = 0;
                            }
                        }
                        if (gold != 0) {
                            if (gold >= obj.value[1]) {
                                gold -= obj.value[1];
                                silver = obj.value[0];
                                extract_obj(obj);
                                obj = create_money(0, silver);
                                obj_to_room(obj, ch.in_room);
                                silver = 0;
                            } else {
                                obj.value[1] -= gold;
                                gold = 0;
                            }
                        }
                    }
                }
                if (silver == 0 && gold == 0) {
                    break;
                }
            }

            /* restore the amount */
            if (!str_cmp(arg2, "gold")) {
                gold = amount;
                silver = 0;
            } else {
                silver = amount;
                gold = 0;
            }

            if (silver != 0) {
                ch.silver += amount;
            } else {
                ch.gold += amount;
            }

            act("You get some money from the floor.", ch, null, null, TO_CHAR);
            if (!IS_AFFECTED(ch, AFF_SNEAK)) {
                act("$n gets some money from the floor.", ch, null, null, TO_ROOM);
            }

            if (IS_SET(ch.act, PLR_AUTOSPLIT)) { /* AUTOSPLIT code */
                int members = 0;
                CHAR_DATA gch;

                for (gch = ch.in_room.people; gch != null; gch = gch.next_in_room) {
                    if (!IS_AFFECTED(gch, AFF_CHARM) && is_same_group(gch, ch)) {
                        members++;
                    }
                }

                if (members > 1 && (amount > 1)) {
                    do_split(ch, silver + " " + gold);
                }
            }


            return;
        }

        if (arg2.isEmpty()) {
            if (str_cmp(arg1, "all") && str_prefix("all.", arg1)) {
                /* 'get obj' */
                obj = get_obj_list(ch, arg1, ch.in_room.contents);
                if (obj == null) {
                    act("I see no $T here.", ch, null, arg1, TO_CHAR);
                    return;
                }

                get_obj(ch, obj, null);
            } else {
                /* 'get all' or 'get all.obj' */
                found = false;
                for (obj = ch.in_room.contents; obj != null; obj = obj_next) {
                    obj_next = obj.next_content;
                    if ((arg1.length() < 4 || is_name(arg1.substring(4), obj.name)) && can_see_obj(ch, obj)) {
                        found = true;
                        get_obj(ch, obj, null);
                    }
                }

                if (!found) {
                    if (arg1.length() < 4) {
                        send_to_char("I see nothing here.\n", ch);
                    } else {
                        act("I see no $T here.", ch, null, arg1.substring(4), TO_CHAR);
                    }
                }
            }
        } else {
            /* 'get ... container' */
            if (!str_cmp(arg2, "all") || !str_prefix("all.", arg2)) {
                send_to_char("You can't do that.\n", ch);
                return;
            }

            if ((container = get_obj_here(ch, arg2)) == null) {
                act("I see no $T here.", ch, null, arg2, TO_CHAR);
                return;
            }

            switch (container.item_type) {
                default -> {
                    send_to_char("That's not a container.\n", ch);
                    return;
                }
                case ITEM_CONTAINER, ITEM_CORPSE_NPC -> {
                }
                case ITEM_CORPSE_PC -> {

                    if (!can_loot(ch, container)) {
                        send_to_char("You can't do that.\n", ch);
                        return;
                    }
                }
            }

            if (IS_SET(container.value[1], CONT_CLOSED)) {
                act("The $d is closed.", ch, null, container.name, TO_CHAR);
                return;
            }

            if (str_cmp(arg1, "all") && str_prefix("all.", arg1)) {
                /* 'get obj container' */
                obj = get_obj_list(ch, arg1, container.contains);
                if (obj == null) {
                    act("I see nothing like that in the $T.",
                            ch, null, arg2, TO_CHAR);
                    return;
                }
                get_obj(ch, obj, container);
            } else {
                /* 'get all container' or 'get all.obj container' */
                found = false;
                for (obj = container.contains; obj != null; obj = obj_next) {
                    obj_next = obj.next_content;
                    if ((arg1.length() == 3 || is_name(arg1.substring(4), obj.name))
                            && can_see_obj(ch, obj)) {
                        found = true;
                        if (container.pIndexData.vnum == OBJ_VNUM_PIT
                                && !IS_IMMORTAL(ch)) {
                            send_to_char("Don't be so greedy!\n", ch);
                            return;
                        }
                        get_obj(ch, obj, container);
                    }
                }

                if (!found) {
                    if (arg1.length() < 4) {
                        act("I see nothing in the $T.",
                                ch, null, arg2, TO_CHAR);
                    } else {
                        act("I see nothing like that in the $T.",
                                ch, null, arg2, TO_CHAR);
                    }
                }
            }
        }

    }


    static void do_put(CHAR_DATA ch, String argument) {
        OBJ_DATA container;
        OBJ_DATA obj;
        OBJ_DATA obj_next;
        OBJ_DATA objc;
        int pcount;
        StringBuilder arg1b = new StringBuilder();
        StringBuilder arg2b = new StringBuilder();
        argument = one_argument(argument, arg1b);
        argument = one_argument(argument, arg2b);
        String arg2 = arg2b.toString();

        if (!str_cmp(arg2, "in") || !str_cmp(arg2, "on")) {
            one_argument(argument, arg2b);
        }

        if (arg1b.isEmpty() || arg2b.isEmpty()) {
            send_to_char("Put what in what?\n", ch);
            return;
        }
        String arg1 = arg1b.toString();
        arg2 = arg2b.toString();

        if (!str_cmp(arg2, "all") || !str_prefix("all.", arg2)) {
            send_to_char("You can't do that.\n", ch);
            return;
        }

        if ((container = get_obj_here(ch, arg2)) == null) {
            act("I see no $T here.", ch, null, arg2, TO_CHAR);
            return;
        }

        if (container.item_type != ITEM_CONTAINER) {
            send_to_char("That's not a container.\n", ch);
            return;
        }

        if (IS_SET(container.value[1], CONT_CLOSED)) {
            act("The $d is closed.", ch, null, container.name, TO_CHAR);
            return;
        }

        if (str_cmp(arg1, "all") && str_prefix("all.", arg1)) {
            /* 'put obj container' */
            if ((obj = get_obj_carry(ch, arg1)) == null) {
                send_to_char("You do not have that item.\n", ch);
                return;
            }

            if (obj == container) {
                send_to_char("You can't fold it into itself.\n", ch);
                return;
            }

            if (!can_drop_obj(ch, obj)) {
                send_to_char("You can't let go of it.\n", ch);
                return;
            }

            if (WEIGHT_MULT(obj) != 100) {
                send_to_char("You have a feeling that would be a bad idea.\n", ch);
                return;
            }

            if (obj.pIndexData.limit != -1
                    && !IS_SET(container.value[1], CONT_ST_LIMITED)) {
                act("This unworthy container won't hold $p.", ch, obj, null, TO_CHAR);
                return;
            }
/*
    if ( IS_SET(container.value[1],CONT_FOR_ARROW)
        && (obj.item_type != ITEM_WEAPON
        || obj.value[0]  != WEAPON_ARROW ) )
    {
     act("You can only put arrows in $p.",ch,container,null,TO_CHAR);
     return;
    }
*/
            if (get_obj_weight(obj) + get_true_weight(container)
                    > (container.value[0] * 10)
                    || get_obj_weight(obj) > (container.value[3] * 10)) {
                send_to_char("It won't fit.\n", ch);
                return;
            }

            if (obj.item_type == ITEM_POTION &&
                    IS_SET(container.wear_flags, ITEM_TAKE)) {
                pcount = 0;
                for (objc = container.contains; objc != null; objc = objc.next_content) {
                    if (objc.item_type == ITEM_POTION) {
                        pcount++;
                    }
                }
                if (pcount > 15) {
                    act("It's not safe to put more potions into $p.", ch, container, null, TO_CHAR);
                    return;
                }
            }

            pcount = 0;
            for (objc = container.contains; objc != null; objc = objc.next_content) {
                pcount++;
            }
            if (pcount > container.value[0]) {
                act("It's not safe to put that much item into $p.", ch, container, null, TO_CHAR);
                return;
            }

            if (container.pIndexData.vnum == OBJ_VNUM_PIT
                    && !CAN_WEAR(container, ITEM_TAKE)) {
                if (obj.timer != 0) {
                    obj.extra_flags = SET_BIT(obj.extra_flags, ITEM_HAD_TIMER);
                } else {
                    obj.timer = number_range(100, 200);
                }
            }

            obj_from_char(obj);
            obj_to_obj(obj, container);

            if (IS_SET(container.value[1], CONT_PUT_ON)) {
                act("$n puts $p on $P.", ch, obj, container, TO_ROOM);
                act("You put $p on $P.", ch, obj, container, TO_CHAR);
            } else {
                act("$n puts $p in $P.", ch, obj, container, TO_ROOM);
                act("You put $p in $P.", ch, obj, container, TO_CHAR);
            }
        } else {
            pcount = 0;
            for (objc = container.contains; objc != null; objc = objc.next_content) {
                pcount++;
            }

            /* 'put all container' or 'put all.obj container' */
            for (obj = ch.carrying; obj != null; obj = obj_next) {
                obj_next = obj.next_content;

                if ((arg1.length() < 4 || is_name(arg1.substring(4), obj.name))
                        && can_see_obj(ch, obj)
                        && WEIGHT_MULT(obj) == 100
                        && obj.wear_loc == WEAR_NONE
                        && obj != container
                        && can_drop_obj(ch, obj)
                        && get_obj_weight(obj) + get_true_weight(container)
                        <= (container.value[0] * 10)
                        && get_obj_weight(obj) < (container.value[3] * 10)) {
                    if (container.pIndexData.vnum == OBJ_VNUM_PIT
                            && !CAN_WEAR(obj, ITEM_TAKE)) {
                        if (obj.timer != 0) {
                            obj.extra_flags = SET_BIT(obj.extra_flags, ITEM_HAD_TIMER);
                        } else {
                            obj.timer = number_range(100, 200);
                        }
                    }

                    if (obj.pIndexData.limit != -1) {
                        act("This unworthy container won't hold $p.", ch, obj, null, TO_CHAR);
                        continue;
                    }

                    if (obj.item_type == ITEM_POTION &&
                            IS_SET(container.wear_flags, ITEM_TAKE)) {
                        pcount = 0;
                        for (objc = container.contains; objc != null; objc = objc.next_content) {
                            if (objc.item_type == ITEM_POTION) {
                                pcount++;
                            }
                        }
                        if (pcount > 15) {
                            act("It's not safe to put more potions into $p.", ch, container, null, TO_CHAR);
                            continue;
                        }
                    }

                    pcount++;
                    if (pcount > container.value[0]) {
                        act("It's not safe to put that much item into $p.", ch, container, null, TO_CHAR);
                        return;
                    }
                    obj_from_char(obj);
                    obj_to_obj(obj, container);

                    if (IS_SET(container.value[1], CONT_PUT_ON)) {
                        act("$n puts $p on $P.", ch, obj, container, TO_ROOM);
                        act("You put $p on $P.", ch, obj, container, TO_CHAR);
                    } else {
                        act("$n puts $p in $P.", ch, obj, container, TO_ROOM);
                        act("You put $p in $P.", ch, obj, container, TO_CHAR);
                    }
                }
            }
        }

    }


    static void do_drop(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        OBJ_DATA obj_next;
        boolean found;

        StringBuilder argb = new StringBuilder();
        argument = one_argument(argument, argb);

        if (argb.isEmpty()) {
            send_to_char("Drop what?\n", ch);
            return;
        }
        String arg = argb.toString();

        if (is_number(arg)) {
            /* 'drop NNNN coins' */
            int amount, gold = 0, silver = 0;

            amount = atoi(arg);
            one_argument(argument, argb);
            arg = argb.toString();
            if (amount <= 0
                    || (str_cmp(arg, "coins") && str_cmp(arg, "coin") &&
                    str_cmp(arg, "gold") && str_cmp(arg, "silver"))) {
                send_to_char("Sorry, you can't do that.\n", ch);
                return;
            }

            if (!str_cmp(arg, "coins") || !str_cmp(arg, "coin")
                    || !str_cmp(arg, "silver")) {
                if (ch.silver < amount) {
                    send_to_char("You don't have that much silver.\n", ch);
                    return;
                }

                ch.silver -= amount;
                silver = amount;
            } else {
                if (ch.gold < amount) {
                    send_to_char("You don't have that much gold.\n", ch);
                    return;
                }

                ch.gold -= amount;
                gold = amount;
            }

            for (obj = ch.in_room.contents; obj != null; obj = obj_next) {
                obj_next = obj.next_content;

                switch (obj.pIndexData.vnum) {
                    case OBJ_VNUM_SILVER_ONE -> {
                        silver += 1;
                        extract_obj(obj);
                    }
                    case OBJ_VNUM_GOLD_ONE -> {
                        gold += 1;
                        extract_obj(obj);
                    }
                    case OBJ_VNUM_SILVER_SOME -> {
                        silver += obj.value[0];
                        extract_obj(obj);
                    }
                    case OBJ_VNUM_GOLD_SOME -> {
                        gold += obj.value[1];
                        extract_obj(obj);
                    }
                    case OBJ_VNUM_COINS -> {
                        silver += obj.value[0];
                        gold += obj.value[1];
                        extract_obj(obj);
                    }
                }
            }

            obj = create_money(gold, silver);
            obj_to_room(obj, ch.in_room);
            if (!IS_AFFECTED(ch, AFF_SNEAK)) {
                act("$n drops some coins.", ch, null, null, TO_ROOM);
            }
            send_to_char("OK.\n", ch);
            if (IS_WATER(ch.in_room)) {
                extract_obj(obj);
                if (!IS_AFFECTED(ch, AFF_SNEAK)) {
                    act("The coins sink down, and disapear in the water.", ch, null, null, TO_ROOM);
                }
                act("The coins sink down, and disapear in the water.", ch, null, null, TO_CHAR);
            }
            return;
        }

        if (str_cmp(arg, "all") && str_prefix("all.", arg)) {
            /* 'drop obj' */
            if ((obj = get_obj_carry(ch, arg)) == null) {
                send_to_char("You do not have that item.\n", ch);
                return;
            }

            if (!can_drop_obj(ch, obj)) {
                send_to_char("You can't let go of it.\n", ch);
                return;
            }

            obj_from_char(obj);
            obj_to_room(obj, ch.in_room);
            if (!IS_AFFECTED(ch, AFF_SNEAK)) {
                act("$n drops $p.", ch, obj, null, TO_ROOM);
            }
            act("You drop $p.", ch, obj, null, TO_CHAR);
            if (obj.pIndexData.vnum == OBJ_VNUM_POTION_VIAL &&
                    number_percent() < 40) {
                if (!IS_SET(ch.in_room.sector_type, SECT_FOREST) &&
                        !IS_SET(ch.in_room.sector_type, SECT_DESERT) &&
                        !IS_SET(ch.in_room.sector_type, SECT_AIR) &&
                        !IS_WATER(ch.in_room)) {
                    act("$p cracks and shaters into tiny pieces.", ch, obj, null, TO_ROOM);
                    act("$p cracks and shaters into tiny pieces.", ch, obj, null, TO_CHAR);
                    extract_obj(obj);
                    return;
                }
            }
            if (IS_SET(obj.progtypes, OPROG_DROP)) {
                obj.pIndexData.oprogs.drop_prog.run(obj, ch);
            }

            if (!may_float(obj) && cant_float(obj) && IS_WATER(ch.in_room)) {
                if (!IS_AFFECTED(ch, AFF_SNEAK)) {
                    act("$p sinks down the water.", ch, obj, null, TO_ROOM);
                }
                act("$p sinks down the water.", ch, obj, null, TO_CHAR);
                extract_obj(obj);
            } else if (IS_OBJ_STAT(obj, ITEM_MELT_DROP)) {
                if (!IS_AFFECTED(ch, AFF_SNEAK)) {
                    act("$p dissolves into smoke.", ch, obj, null, TO_ROOM);
                }
                act("$p dissolves into smoke.", ch, obj, null, TO_CHAR);
                extract_obj(obj);
            }
        } else {
            /* 'drop all' or 'drop all.obj' */
            found = false;
            for (obj = ch.carrying; obj != null; obj = obj_next) {
                obj_next = obj.next_content;

                if ((arg.length() < 4 || is_name(arg.substring(4), obj.name))
                        && can_see_obj(ch, obj)
                        && obj.wear_loc == WEAR_NONE
                        && can_drop_obj(ch, obj)) {
                    found = true;
                    obj_from_char(obj);
                    obj_to_room(obj, ch.in_room);
                    if (!IS_AFFECTED(ch, AFF_SNEAK)) {
                        act("$n drops $p.", ch, obj, null, TO_ROOM);
                    }
                    act("You drop $p.", ch, obj, null, TO_CHAR);
                    if (obj.pIndexData.vnum == OBJ_VNUM_POTION_VIAL &&
                            number_percent() < 70) {
                        if (!IS_SET(ch.in_room.sector_type, SECT_FOREST) &&
                                !IS_SET(ch.in_room.sector_type, SECT_DESERT) &&
                                !IS_SET(ch.in_room.sector_type, SECT_AIR) &&
                                !IS_WATER(ch.in_room))

                        {
                            if (!IS_AFFECTED(ch, AFF_SNEAK)) {
                                act("$p cracks and shaters into tiny pieces.", ch, obj, null, TO_ROOM);
                            }
                            act("$p cracks and shaters into tiny pieces.", ch, obj, null, TO_CHAR);
                            extract_obj(obj);
                            continue;
                        }
                    }

                    if (IS_SET(obj.progtypes, OPROG_DROP)) {
                        obj.pIndexData.oprogs.drop_prog.run(obj, ch);
                    }

                    if (!may_float(obj) && cant_float(obj) && IS_WATER(ch.in_room)) {
                        if (!IS_AFFECTED(ch, AFF_SNEAK)) {
                            act("$p sinks down the water.", ch, obj, null, TO_ROOM);
                        }
                        act("$p sinks down the water.", ch, obj, null, TO_CHAR);
                        extract_obj(obj);
                    } else if (IS_OBJ_STAT(obj, ITEM_MELT_DROP)) {
                        if (!IS_AFFECTED(ch, AFF_SNEAK)) {
                            act("$p dissolves into smoke.", ch, obj, null, TO_ROOM);
                        }
                        act("$p dissolves into smoke.", ch, obj, null, TO_CHAR);
                        extract_obj(obj);
                    }
                }
            }

            if (!found) {
                if (arg.length() < 4) {
                    act("You are not carrying anything.",
                            ch, null, arg, TO_CHAR);
                } else {
                    act("You are not carrying any $T.",
                            ch, null, arg.substring(4), TO_CHAR);
                }
            }
        }

    }


    static void do_drag(CHAR_DATA ch, String argument) {
        CHAR_DATA gch;
        OBJ_DATA obj;
        EXIT_DATA pexit;
        ROOM_INDEX_DATA was_in_room;
        int direction;


        StringBuilder arg1b = new StringBuilder();
        StringBuilder arg2b = new StringBuilder();
        argument = one_argument(argument, arg1b);
        one_argument(argument, arg2b);

        /* Get type. */
        if (arg1b.isEmpty() || arg2b.isEmpty()) {
            send_to_char("Drag what to which direction?\n", ch);
            return;
        }

        String arg1 = arg1b.toString();
        String arg2 = arg2b.toString();
        if (!str_cmp(arg1, "all") || !str_prefix("all.", arg1)) {
            send_to_char("You can't do that.\n", ch);
            return;
        }

        obj = get_obj_list(ch, arg1, ch.in_room.contents);
        if (obj == null) {
            act("I see no $T here.", ch, null, arg1, TO_CHAR);
            return;
        }

        if (!CAN_WEAR(obj, ITEM_TAKE)) {
            send_to_char("You can't take that.\n", ch);
            return;
        }

        if (obj.pIndexData.limit != -1) {
            if ((IS_OBJ_STAT(obj, ITEM_ANTI_EVIL) && IS_EVIL(ch))
                    || (IS_OBJ_STAT(obj, ITEM_ANTI_GOOD) && IS_GOOD(ch))
                    || (IS_OBJ_STAT(obj, ITEM_ANTI_NEUTRAL) && IS_NEUTRAL(ch))) {
                act("You are zapped by $p and drop it.", ch, obj, null, TO_CHAR);
                act("$n is zapped by $p and drops it.", ch, obj, null, TO_ROOM);
                return;
            }
        }

        if (obj.in_room != null) {
            for (gch = obj.in_room.people; gch != null; gch = gch.next_in_room) {
                if (gch.on == obj) {
                    act("$N appears to be using $p.", ch, obj, gch, TO_CHAR);
                    return;
                }
            }
        }

        if ((get_carry_weight(ch) + get_obj_weight(obj)) > (2 * can_carry_w(ch))) {
            act("$d: you can't drag that much weight.",
                    ch, null, obj.name, TO_CHAR);
            return;
        }

        if (get_eq_char(ch, WEAR_LEFT) != null
                || get_eq_char(ch, WEAR_RIGHT) != null
                || get_eq_char(ch, WEAR_BOTH) != null) {
            send_to_char("You need your both hands free.\n", ch);
            return;
        }

        if ((direction = find_exit(ch, arg2)) < 0) {
            return;
        }

        if ((pexit = ch.in_room.exit[direction]) == null
                || pexit.to_room == null
                || !can_see_room(ch, pexit.to_room)) {
            send_to_char("Alas, you cannot go that way.\n", ch);
            return;
        }
        TextBuffer buf = new TextBuffer();
        buf.sprintf("You grab $p to drag towards %s.", dir_name[direction]);
        act(buf.toString(), ch, obj, null, TO_CHAR);
        if (!IS_AFFECTED(ch, AFF_SNEAK)) {
            buf.sprintf("$n grabs $p to drag towards %s.", dir_name[direction]);
            act(buf.toString(), ch, obj, null, TO_ROOM);
        }

        obj_from_room(obj);
        obj_to_char(obj, ch);

        if (IS_SET(obj.progtypes, OPROG_GET)) {
            obj.pIndexData.oprogs.get_prog.run(obj, ch);
        }

        if (obj.carried_by != ch) {
            return;
        }

        was_in_room = ch.in_room;
        move_char(ch, direction);

        if (was_in_room == ch.in_room) {
            send_to_char("You cannot drag that way.\n", ch);
        } else {
            if (!IS_AFFECTED(ch, AFF_SNEAK)) {
                act("$n drops $p.", ch, obj, null, TO_ROOM);
            }
            act("You drop $p.", ch, obj, null, TO_CHAR);
            WAIT_STATE(ch, 2 * PULSE_VIOLENCE);
        }

        obj_from_char(obj);
        obj_to_room(obj, ch.in_room);

    }


    static void do_give(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        OBJ_DATA obj;

        StringBuilder arg1b = new StringBuilder();
        StringBuilder arg2b = new StringBuilder();
        argument = one_argument(argument, arg1b);
        argument = one_argument(argument, arg2b);

        if (arg1b.isEmpty() || arg2b.isEmpty()) {
            send_to_char("Give what to whom?\n", ch);
            return;
        }
        String arg1 = arg1b.toString();
        String arg2 = arg2b.toString();

        if (is_number(arg1)) {
            /* 'give NNNN coins victim' */
            int amount;
            boolean silver;
            int weight;

            amount = atoi(arg1);
            if (amount <= 0
                    || (str_cmp(arg2, "coins") && str_cmp(arg2, "coin") &&
                    str_cmp(arg2, "gold") && str_cmp(arg2, "silver"))) {
                send_to_char("Sorry, you can't do that.\n", ch);
                return;
            }

            silver = str_cmp(arg2, "gold");

            one_argument(argument, arg2b);
            if (arg2b.isEmpty()) {
                send_to_char("Give what to whom?\n", ch);
                return;
            }

            arg2 = arg2b.toString();
            if ((victim = get_char_room(ch, arg2)) == null) {
                send_to_char("They aren't here.\n", ch);
                return;
            }

            if ((!silver && ch.gold < amount) || (silver && ch.silver < amount)) {
                send_to_char("You haven't got that much.\n", ch);
                return;
            }

            if (!silver) {
                weight = amount * 2 / 5;
            } else {
                weight = amount / 10;
            }

            if (!IS_NPC(victim)
                    && get_carry_weight(victim) + weight > can_carry_w(victim)) {
                act("$N can't carry that much weight.", ch, null, victim, TO_CHAR);
                return;
            }

            if (silver) {
                ch.silver -= amount;
                victim.silver += amount;
            } else {
                ch.gold -= amount;
                victim.gold += amount;
            }
            TextBuffer buf = new TextBuffer();
            buf.sprintf("$n gives you %d %s.", amount, silver ? "silver" : "gold");
            act(buf.toString(), ch, null, victim, TO_VICT);
            act("$n gives $N some coins.", ch, null, victim, TO_NOTVICT);
            buf.sprintf("You give $N %d %s.", amount, silver ? "silver" : "gold");
            act(buf.toString(), ch, null, victim, TO_CHAR);
            if (IS_SET(victim.progtypes, MPROG_BRIBE)) {
                victim.pIndexData.mprogs.bribe_prog.run(victim, ch, amount);
            }

            if (IS_NPC(victim) && IS_SET(victim.act, ACT_IS_CHANGER)) {
                int change;

                change = (silver ? 95 * amount / 100 / 100
                        : 95 * amount);


                if (silver) {
                    weight = change * 2 / 5;
                } else {
                    weight = change / 10;
                }

                if (!silver) {
                    weight -= amount * 2 / 5;
                } else {
                    weight -= amount / 10;
                }

                if (!IS_NPC(ch)
                        && get_carry_weight(ch) + weight > can_carry_w(ch)) {
                    act("You can't carry that much weight.", ch, null, null, TO_CHAR);
                    return;
                }

                if (!silver && change > victim.silver) {
                    victim.silver += change;
                }

                if (silver && change > victim.gold) {
                    victim.gold += change;
                }

                if (change < 1 && can_see(victim, ch)) {
                    act("$n tells you 'I'm sorry, you did not give me enough to change.'", victim, null, ch, TO_VICT);
                    ch.reply = victim;
                    buf.sprintf("%d %s %s", amount, silver ? "silver" : "gold", ch.name);
                    do_give(victim, buf.toString());
                } else if (can_see(victim, ch)) {
                    buf.sprintf("%d %s %s", change, silver ? "gold" : "silver", ch.name);
                    do_give(victim, buf.toString());
                    if (silver) {
                        buf.sprintf("%d silver %s",
                                (95 * amount / 100 - change * 100), ch.name);
                        do_give(victim, buf.toString());
                    }
                    act("$n tells you 'Thank you, come again.'",
                            victim, null, ch, TO_VICT);
                    ch.reply = victim;
                }
            }
            return;
        }

        if ((obj = get_obj_carry(ch, arg1)) == null) {
            send_to_char("You do not have that item.\n", ch);
            return;
        }

        if (obj.wear_loc != WEAR_NONE) {
            send_to_char("You must remove it first.\n", ch);
            return;
        }

        if ((victim = get_char_room(ch, arg2)) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }


        if ((IS_NPC(victim) && victim.pIndexData.pShop != null) &&
                !IS_SET(victim.progtypes, MPROG_GIVE)) {
            act("$N tells you 'Sorry, you'll have to sell that.'",
                    ch, null, victim, TO_CHAR);
            ch.reply = victim;
            return;
        }

        if (!can_drop_obj(ch, obj)) {
            send_to_char("You can't let go of it.\n", ch);
            return;
        }

        if (victim.carry_number + get_obj_number(obj) > can_carry_n(victim)) {
            act("$N has $S hands full.", ch, null, victim, TO_CHAR);
            return;
        }

        if (get_carry_weight(victim) + get_obj_weight(obj) > can_carry_w(victim)) {
            act("$N can't carry that much weight.", ch, null, victim, TO_CHAR);
            return;
        }

        if (!can_see_obj(victim, obj)) {
            act("$N can't see it.", ch, null, victim, TO_CHAR);
            return;
        }

        if (obj.pIndexData.limit != -1) {
            if ((IS_OBJ_STAT(obj, ITEM_ANTI_EVIL) && IS_EVIL(victim))
                    || (IS_OBJ_STAT(obj, ITEM_ANTI_GOOD) && IS_GOOD(victim))
                    || (IS_OBJ_STAT(obj, ITEM_ANTI_NEUTRAL) && IS_NEUTRAL(victim))) {
                send_to_char("Your victim's alignment doesn't match with the objects align.", ch);
                return;
            }
        }

        obj_from_char(obj);
        obj_to_char(obj, victim);
        act("$n gives $p to $N.", ch, obj, victim, TO_NOTVICT);
        act("$n gives you $p.", ch, obj, victim, TO_VICT);
        act("You give $p to $N.", ch, obj, victim, TO_CHAR);

        if (IS_SET(obj.progtypes, OPROG_GIVE)) {
            obj.pIndexData.oprogs.give_prog.run(obj, ch, victim);
        }

        if (IS_SET(obj.progtypes, OPROG_GET)) {
            obj.pIndexData.oprogs.get_prog.run(obj, victim);
        }

        if (IS_SET(victim.progtypes, MPROG_GIVE)) {
            victim.pIndexData.mprogs.give_prog.run(victim, ch, obj);
        }

    }


    static void do_bury(CHAR_DATA ch, String argument) {
        String bufp;
        OBJ_DATA obj, shovel, stone;
        int move;
        StringBuilder arg = new StringBuilder();

        one_argument(argument, arg);

        if (skill_failure_check(ch, gsn_bury, false, 0, "You can't do that.\n")) {
            return;
        }

        if (arg.isEmpty()) {
            send_to_char("Bury whose corpse?\n", ch);
            return;
        }

        if ((shovel = get_weapon_char(ch, WEAPON_MACE)) == null || !is_name("shovel", shovel.name)) {
            send_to_char("You don't have shovel do dig!\n", ch);
            return;
        }

        obj = get_obj_list(ch, arg.toString(), ch.in_room.contents);
        if (obj == null) {
            act("I see no $T here.", ch, null, arg, TO_CHAR);
            return;
        }

        if (obj.item_type != ITEM_CORPSE_PC && obj.item_type != ITEM_CORPSE_NPC) {
            send_to_char("Why do you want to bury that?\n", ch);
            return;
        }

        switch (ch.in_room.sector_type) {
            case SECT_CITY, SECT_INSIDE -> {
                send_to_char("The floor is too hard to dig through.\n", ch);
                return;
            }
            case SECT_WATER_SWIM, SECT_WATER_NOSWIM -> {
                send_to_char("You cannot bury something here.\n", ch);
                return;
            }
            case SECT_AIR -> {
                send_to_char("What?  In the air?!\n", ch);
                return;
            }
        }

        move = (obj.weight * 5) / get_curr_stat(ch, STAT_STR);
        move = URANGE(2, move, 1000);
        if (move > ch.move) {
            send_to_char("You don't have enough energy to bury something of that size.\n", ch);
            return;
        }
        ch.move -= move;

        act("You solemnly bury $p...", ch, obj, null, TO_CHAR);
        act("$n solemnly buries $p...", ch, obj, null, TO_ROOM);

        obj.extra_flags = SET_BIT(obj.extra_flags, ITEM_BURIED);
        WAIT_STATE(ch, 4 * PULSE_VIOLENCE);

        obj.timer = -1;

        TextBuffer buf = new TextBuffer();
        bufp = obj.short_descr;
        while (!bufp.isEmpty()) {
            bufp = one_argument(bufp, arg);
            String argstr = arg.toString();
            if (!(!str_cmp(argstr, "The") || !str_cmp(argstr, "undead")
                    || !str_cmp(argstr, "body") || !str_cmp(argstr, "corpse")
                    || !str_cmp(argstr, "of"))) {
                if (buf.isEmpty()) {
                    buf.append(arg);
                } else {
                    buf.append(" ");
                    buf.append(arg);
                }
            }
        }
        arg.setLength(0);
        arg.append(buf);

        stone = create_object(get_obj_index(OBJ_VNUM_GRAVE_STONE), ch.level);

        buf.sprintf(stone.description, arg);
        stone.description = buf.toString();

        buf.sprintf(stone.short_descr, arg);
        stone.short_descr = buf.toString();

        obj_to_room(stone, ch.in_room);

        /*
         * a little trick here... :)
         * although grave stone is not a container....
         * protects corpse from area affect attacks.
         * but what about earthquake
         */
        obj_from_room(obj);
        obj_to_obj(obj, stone);

    }

    static void do_dig(CHAR_DATA ch, String argument) {
        OBJ_DATA obj, shovel, corpse;
        int move;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.isEmpty()) {
            send_to_char("Dig which grave?\n", ch);
            return;
        }

        if ((shovel = get_weapon_char(ch, WEAPON_MACE)) == null
                || !is_name("shovel", shovel.name)) {
            send_to_char("You don't have shovel do dig!\n", ch);
            return;
        }

        obj = get_obj_list(ch, arg.toString(), ch.in_room.contents);
        if (obj == null) {
            act("I see no $T here.", ch, null, arg, TO_CHAR);
            return;
        }

        if (obj.pIndexData.vnum != OBJ_VNUM_GRAVE_STONE) {
            send_to_char("I don't think that it is a grave.\n", ch);
            return;
        }

        move = (obj.weight * 5) / get_curr_stat(ch, STAT_STR);
        move = URANGE(2, move, 1000);
        if (move > ch.move) {
            send_to_char("You don't have enough energy to dig something of that size.\n", ch);
            return;
        }
        ch.move -= move;

        act("You start digging $p...", ch, obj, null, TO_CHAR);
        act("$n starts diggin $p...", ch, obj, null, TO_ROOM);

        WAIT_STATE(ch, 4 * PULSE_VIOLENCE);

        if ((corpse = obj.contains) == null) {
            act("Digging reveals nothing.\n", ch, null, null, TO_ALL);
            return;
        }

        corpse.extra_flags = REMOVE_BIT(corpse.extra_flags, ITEM_BURIED);
        obj_from_obj(corpse);
        obj_to_room(corpse, ch.in_room);
        extract_obj(obj);
        corpse.timer = number_range(25, 40);
        act("Digging reveals $p.\n", ch, corpse, null, TO_ALL);

    }

/* for poisoning weapons and food/drink */

    static void do_envenom(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        int percent, skill;

        /* find out what */
        if (argument.isEmpty()) {
            send_to_char("Envenom what item?\n", ch);
            return;
        }

        obj = get_obj_list(ch, argument, ch.carrying);

        if (obj == null) {
            send_to_char("You don't have that item.\n", ch);
            return;
        }

        if ((skill = get_skill(ch, gsn_envenom)) < 1) {
            send_to_char("Are you crazy? You'd poison yourself!\n", ch);
            return;
        }

        if (obj.item_type == ITEM_FOOD || obj.item_type == ITEM_DRINK_CON) {
            if (IS_OBJ_STAT(obj, ITEM_BLESS) || IS_OBJ_STAT(obj, ITEM_BURN_PROOF)) {
                act("You fail to poison $p.", ch, obj, null, TO_CHAR);
                return;
            }

            if (number_percent() < skill)  /* success! */ {
                act("$n treats $p with deadly poison.", ch, obj, null, TO_ROOM);
                act("You treat $p with deadly poison.", ch, obj, null, TO_CHAR);
                if (obj.value[3] == 0) {
                    obj.value[3] = 1;
                    check_improve(ch, gsn_envenom, true, 4);
                }
                WAIT_STATE(ch, gsn_envenom.beats);
                return;
            }

            act("You fail to poison $p.", ch, obj, null, TO_CHAR);
            if (obj.value[3] == 0) {
                check_improve(ch, gsn_envenom, false, 4);
            }
            WAIT_STATE(ch, gsn_envenom.beats);
            return;
        }

        if (obj.item_type == ITEM_WEAPON) {
            if (IS_WEAPON_STAT(obj, WEAPON_FLAMING)
                    || IS_WEAPON_STAT(obj, WEAPON_FROST)
                    || IS_WEAPON_STAT(obj, WEAPON_VAMPIRIC)
                    || IS_WEAPON_STAT(obj, WEAPON_SHARP)
                    || IS_WEAPON_STAT(obj, WEAPON_VORPAL)
                    || IS_WEAPON_STAT(obj, WEAPON_SHOCKING)
                    || IS_WEAPON_STAT(obj, WEAPON_HOLY)
                    || IS_OBJ_STAT(obj, ITEM_BLESS) || IS_OBJ_STAT(obj, ITEM_BURN_PROOF)) {
                act("You can't seem to envenom $p.", ch, obj, null, TO_CHAR);
                return;
            }

            if (obj.value[3] < 0
                    || attack_table[obj.value[3]].damage == DAM_BASH) {
                send_to_char("You can only envenom edged weapons.\n", ch);
                return;
            }

            if (IS_WEAPON_STAT(obj, WEAPON_POISON)) {
                act("$p is already envenomed.", ch, obj, null, TO_CHAR);
                return;
            }

            percent = number_percent();
            if (percent < skill) {
                AFFECT_DATA af = new AFFECT_DATA();
                af.where = TO_WEAPON;
                af.type = gsn_poison;
                af.level = ch.level * percent / 100;
                af.duration = ch.level * percent / 100;
                af.location = 0;
                af.modifier = 0;
                af.bitvector = WEAPON_POISON;
                affect_to_obj(obj, af);

                if (!IS_AFFECTED(ch, AFF_SNEAK)) {
                    act("$n coats $p with deadly venom.", ch, obj, null, TO_ROOM);
                }
                act("You coat $p with venom.", ch, obj, null, TO_CHAR);
                check_improve(ch, gsn_envenom, true, 3);
                WAIT_STATE(ch, gsn_envenom.beats);
                return;
            } else {
                act("You fail to envenom $p.", ch, obj, null, TO_CHAR);
                check_improve(ch, gsn_envenom, false, 3);
                WAIT_STATE(ch, gsn_envenom.beats);
                return;
            }
        }

        act("You can't poison $p.", ch, obj, null, TO_CHAR);
    }

    static void do_fill(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        OBJ_DATA fountain;
        boolean found;

        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.isEmpty()) {
            send_to_char("Fill what?\n", ch);
            return;
        }

        if ((obj = get_obj_carry(ch, arg.toString())) == null) {
            send_to_char("You do not have that item.\n", ch);
            return;
        }

        found = false;
        for (fountain = ch.in_room.contents; fountain != null;
             fountain = fountain.next_content) {
            if (fountain.item_type == ITEM_FOUNTAIN) {
                found = true;
                break;
            }
        }

        if (!found) {
            send_to_char("There is no fountain here!\n", ch);
            return;
        }

        if (obj.item_type != ITEM_DRINK_CON) {
            send_to_char("You can't fill that.\n", ch);
            return;
        }

        if (obj.value[1] != 0 && obj.value[2] != fountain.value[2]) {
            send_to_char("There is already another liquid in it.\n", ch);
            return;
        }

        if (obj.value[1] >= obj.value[0]) {
            send_to_char("Your container is full.\n", ch);
            return;
        }
        TextBuffer buf = new TextBuffer();
        buf.sprintf("You fill $p with %s from $P.",
                liq_table[fountain.value[2]].liq_name);
        act(buf.toString(), ch, obj, fountain, TO_CHAR);
        buf.sprintf("$n fills $p with %s from $P.",
                liq_table[fountain.value[2]].liq_name);
        act(buf.toString(), ch, obj, fountain, TO_ROOM);
        obj.value[2] = fountain.value[2];
        obj.value[1] = obj.value[0];

    }

    static void do_pour(CHAR_DATA ch, String argument) {
        OBJ_DATA out, in;
        CHAR_DATA vch = null;
        int amount;
        StringBuilder arg = new StringBuilder();
        argument = one_argument(argument, arg);

        if (arg.isEmpty() || argument.isEmpty()) {
            send_to_char("Pour what into what?\n", ch);
            return;
        }


        if ((out = get_obj_carry(ch, arg.toString())) == null) {
            send_to_char("You don't have that item.\n", ch);
            return;
        }

        if (out.item_type != ITEM_DRINK_CON) {
            send_to_char("That's not a drink container.\n", ch);
            return;
        }

        if (!str_cmp(argument, "out")) {
            if (out.value[1] == 0) {
                send_to_char("It's already empty.\n", ch);
                return;
            }

            out.value[1] = 0;
            out.value[3] = 0;
            TextBuffer buf = new TextBuffer();
            if (!IS_WATER(ch.in_room)) {
                buf.sprintf("You invert $p, spilling %s all over the ground.",
                        liq_table[out.value[2]].liq_name);
                act(buf.toString(), ch, out, null, TO_CHAR);

                buf.sprintf("$n inverts $p, spilling %s all over the ground.",
                        liq_table[out.value[2]].liq_name);
                act(buf.toString(), ch, out, null, TO_ROOM);
            } else {
                buf.sprintf("You invert $p, spilling %s in to the water.",
                        liq_table[out.value[2]].liq_name);
                act(buf.toString(), ch, out, null, TO_CHAR);

                buf.sprintf("$n inverts $p, spilling %s in to the water.",
                        liq_table[out.value[2]].liq_name);
                act(buf.toString(), ch, out, null, TO_ROOM);
            }
            return;
        }

        if ((in = get_obj_here(ch, argument)) == null) {
            vch = get_char_room(ch, argument);

            if (vch == null) {
                send_to_char("Pour into what?\n", ch);
                return;
            }

            in = get_hold_char(vch);

            if (in == null) {
                send_to_char("They aren't holding anything.", ch);
                return;
            }
        }

        if (in.item_type != ITEM_DRINK_CON) {
            send_to_char("You can only pour into other drink containers.\n", ch);
            return;
        }

        if (in == out) {
            send_to_char("You cannot change the laws of physics!\n", ch);
            return;
        }

        if (in.value[1] != 0 && in.value[2] != out.value[2]) {
            send_to_char("They don't hold the same liquid.\n", ch);
            return;
        }

        if (out.value[1] == 0) {
            act("There's nothing in $p to pour.", ch, out, null, TO_CHAR);
            return;
        }

        if (in.value[1] >= in.value[0]) {
            act("$p is already filled to the top.", ch, in, null, TO_CHAR);
            return;
        }

        amount = UMIN(out.value[1], in.value[0] - in.value[1]);

        in.value[1] += amount;
        out.value[1] -= amount;
        in.value[2] = out.value[2];

        TextBuffer buf = new TextBuffer();

        if (vch == null) {
            buf.sprintf("You pour %s from $p into $P.",
                    liq_table[out.value[2]].liq_name);
            act(buf, ch, out, in, TO_CHAR);
            buf.sprintf("$n pours %s from $p into $P.",
                    liq_table[out.value[2]].liq_name);
            act(buf, ch, out, in, TO_ROOM);
        } else {
            buf.sprintf("You pour some %s for $N.",
                    liq_table[out.value[2]].liq_name);
            act(buf, ch, null, vch, TO_CHAR);
            buf.sprintf("$n pours you some %s.",
                    liq_table[out.value[2]].liq_name);
            act(buf, ch, null, vch, TO_VICT);
            buf.sprintf("$n pours some %s for $N.",
                    liq_table[out.value[2]].liq_name);
            act(buf, ch, null, vch, TO_NOTVICT);
        }

    }

    static void do_drink(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        int amount;
        int liquid;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.isEmpty()) {
            for (obj = ch.in_room.contents; obj != null; obj = obj.next_content) {
                if (obj.item_type == ITEM_FOUNTAIN) {
                    break;
                }
            }

            if (obj == null) {
                send_to_char("Drink what?\n", ch);
                return;
            }
        } else {
            if ((obj = get_obj_here(ch, arg.toString())) == null) {
                send_to_char("You can't find it.\n", ch);
                return;
            }
        }

        if (!IS_NPC(ch) && ch.pcdata.condition[COND_DRUNK] > 10) {
            send_to_char("You fail to reach your mouth.  *Hic*\n", ch);
            return;
        }

        switch (obj.item_type) {
            default -> {
                send_to_char("You can't drink from that.\n", ch);
                return;
            }
            case ITEM_FOUNTAIN -> {
                if ((liquid = obj.value[2]) < 0) {
                    bug("Do_drink: bad liquid number %d.", liquid);
                    liquid = obj.value[2] = 0;
                }
                amount = liq_table[liquid].liq_affect[4] * 3;
            }
            case ITEM_DRINK_CON -> {
                if (obj.value[1] <= 0) {
                    send_to_char("It is already empty.\n", ch);
                    return;
                }
                if ((liquid = obj.value[2]) < 0) {
                    bug("Do_drink: bad liquid number %d.", liquid);
                    liquid = obj.value[2] = 0;
                }
                amount = liq_table[liquid].liq_affect[4];
                amount = UMIN(amount, obj.value[1]);
            }
        }
        if (!IS_NPC(ch) && !IS_IMMORTAL(ch)
                && ch.pcdata.condition[COND_FULL] > 80) {
            send_to_char("You're too full to drink more.\n", ch);
            return;
        }

        act("$n drinks $T from $p.",
                ch, obj, liq_table[liquid].liq_name, TO_ROOM);
        act("You drink $T from $p.",
                ch, obj, liq_table[liquid].liq_name, TO_CHAR);

        if (ch.fighting != null) {
            WAIT_STATE(ch, 3 * PULSE_VIOLENCE);
        }

        gain_condition(ch, COND_DRUNK,
                amount * liq_table[liquid].liq_affect[COND_DRUNK] / 36);
        gain_condition(ch, COND_FULL,
                amount * liq_table[liquid].liq_affect[COND_FULL] / 2);
        gain_condition(ch, COND_THIRST,
                amount * liq_table[liquid].liq_affect[COND_THIRST] / 5);
        gain_condition(ch, COND_HUNGER, amount * liq_table[liquid].liq_affect[COND_HUNGER]);

        if (!IS_NPC(ch) && ch.pcdata.condition[COND_DRUNK] > 10) {
            send_to_char("You feel drunk.\n", ch);
        }
        if (!IS_NPC(ch) && ch.pcdata.condition[COND_FULL] > 60) {
            send_to_char("You are full.\n", ch);
        }
        if (!IS_NPC(ch) && ch.pcdata.condition[COND_THIRST] > 60) {
            send_to_char("Your thirst is quenched.\n", ch);
        }

        if (obj.value[3] != 0) {
            /* The drink was poisoned ! */

            act("$n chokes and gags.", ch, null, null, TO_ROOM);
            send_to_char("You choke and gag.\n", ch);
            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_poison;
            af.level = number_fuzzy(amount);
            af.duration = 3 * amount;
            af.location = APPLY_NONE;
            af.modifier = 0;
            af.bitvector = AFF_POISON;
            affect_join(ch, af);
        }

        if (obj.value[0] > 0) {
            obj.value[1] -= amount;
        }
    }


    static void do_eat(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;

        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);
        if (arg.isEmpty()) {
            send_to_char("Eat what?\n", ch);
            return;
        }

        if ((obj = get_obj_carry(ch, arg.toString())) == null) {
            send_to_char("You do not have that item.\n", ch);
            return;
        }

        if (!IS_IMMORTAL(ch)) {
            if (obj.item_type != ITEM_FOOD && obj.item_type != ITEM_PILL) {
                send_to_char("That's not edible.\n", ch);
                return;
            }

            if (!IS_NPC(ch) && ch.pcdata.condition[COND_FULL] > 80) {
                send_to_char("You are too full to eat more.\n", ch);
                return;
            }
        }

        act("$n eats $p.", ch, obj, null, TO_ROOM);
        act("You eat $p.", ch, obj, null, TO_CHAR);
        if (ch.fighting != null) {
            WAIT_STATE(ch, 3 * PULSE_VIOLENCE);
        }

        switch (obj.item_type) {
            case ITEM_FOOD -> {
                if (!IS_NPC(ch)) {
                    int condition;

                    condition = ch.pcdata.condition[COND_HUNGER];
                    gain_condition(ch, COND_FULL, obj.value[0] * 2);
                    gain_condition(ch, COND_HUNGER, obj.value[1] * 2);
                    if (condition == 0 && ch.pcdata.condition[COND_HUNGER] > 0) {
                        send_to_char("You are no longer hungry.\n", ch);
                    } else if (ch.pcdata.condition[COND_FULL] > 60) {
                        send_to_char("You are full.\n", ch);
                    }
                }
                if (obj.value[3] != 0) {
                    /* The food was poisoned! */


                    act("$n chokes and gags.", ch, 0, 0, TO_ROOM);
                    send_to_char("You choke and gag.\n", ch);
                    AFFECT_DATA af = new AFFECT_DATA();
                    af.where = TO_AFFECTS;
                    af.type = gsn_poison;
                    af.level = number_fuzzy(obj.value[0]);
                    af.duration = 2 * obj.value[0];
                    af.location = APPLY_NONE;
                    af.modifier = 0;
                    af.bitvector = AFF_POISON;
                    affect_join(ch, af);
                }
            }
            case ITEM_PILL -> {
                obj_cast_spell(Skill.skills[obj.value[1]], obj.value[0], ch, ch, null);
                obj_cast_spell(Skill.skills[obj.value[2]], obj.value[0], ch, ch, null);
                obj_cast_spell(Skill.skills[obj.value[3]], obj.value[0], ch, ch, null);
            }
        }

        extract_obj(obj);
    }

/*
* Remove an object. Only for non-multi-wear locations
*/

    static boolean remove_obj_loc(CHAR_DATA ch, int iWear, boolean fReplace) {
        OBJ_DATA obj;

        if ((obj = get_eq_char(ch, iWear)) == null) {
            return true;
        }

        if (!fReplace) {
            return false;
        }

        if (IS_SET(obj.extra_flags, ITEM_NOREMOVE)) {
            act("You can't remove $p.", ch, obj, null, TO_CHAR);
            return false;
        }

        if ((obj.item_type == ITEM_TATTOO) && (!IS_IMMORTAL(ch))) {
            act("You must scratch it to remove $p.", ch, obj, null, TO_CHAR);
            return false;
        }

        if (iWear == WEAR_STUCK_IN) {
            unequip_char(ch, obj);

            if (get_eq_char(ch, WEAR_STUCK_IN) == null) {
                if (is_affected(ch, gsn_arrow)) {
                    affect_strip(ch, gsn_arrow);
                }
                if (is_affected(ch, gsn_spear)) {
                    affect_strip(ch, gsn_spear);
                }
            }
            act("You remove $p, in pain.", ch, obj, null, TO_CHAR);
            act("$n remove $p, in pain.", ch, obj, null, TO_ROOM);
            WAIT_STATE(ch, 4);
            return true;
        }

        unequip_char(ch, obj);
        act("$n stops using $p.", ch, obj, null, TO_ROOM);
        act("You stop using $p.", ch, obj, null, TO_CHAR);

        return true;
    }

/*
 * Remove an object.
 */

    static boolean remove_obj(CHAR_DATA ch, OBJ_DATA obj, boolean fReplace) {
        if (obj == null) {
            return true;
        }

        if (!fReplace) {
            return false;
        }

        if (IS_SET(obj.extra_flags, ITEM_NOREMOVE)) {
            act("You can't remove $p.", ch, obj, null, TO_CHAR);
            return false;
        }

        if ((obj.item_type == ITEM_TATTOO) && (!IS_IMMORTAL(ch))) {
            act("You must scratch it to remove $p.", ch, obj, null, TO_CHAR);
            return false;
        }

        if (obj.wear_loc == WEAR_STUCK_IN) {
            unequip_char(ch, obj);

            if (get_eq_char(ch, WEAR_STUCK_IN) == null) {
                if (is_affected(ch, gsn_arrow)) {
                    affect_strip(ch, gsn_arrow);
                }
                if (is_affected(ch, gsn_spear)) {
                    affect_strip(ch, gsn_spear);
                }
            }
            act("You remove $p, in pain.", ch, obj, null, TO_CHAR);
            act("$n remove $p, in pain.", ch, obj, null, TO_ROOM);
            WAIT_STATE(ch, 4);
            return true;
        }

        unequip_char(ch, obj);
        act("$n stops using $p.", ch, obj, null, TO_ROOM);
        act("You stop using $p.", ch, obj, null, TO_CHAR);

        return true;
    }

/*
* Wear one object.
* Optional replacement of existing objects.
* Big repetitive code, ick.
*/

    static void wear_obj(CHAR_DATA ch, OBJ_DATA obj, boolean fReplace) {
        int wear_level;

        wear_level = ch.level;

        if ((ch.clazz.fMana && obj.item_type == ITEM_ARMOR)
                || (!ch.clazz.fMana && obj.item_type == ITEM_WEAPON)) {
            wear_level += 3;
        }

        if (wear_level < obj.level) {
            TextBuffer buf = new TextBuffer();
            buf.sprintf("You must be level %d to use this object.\n", obj.level);
            send_to_char(buf, ch);
            act("$n tries to use $p, but is too inexperienced.",
                    ch, obj, null, TO_ROOM);
            return;
        }

        if (obj.item_type == ITEM_LIGHT) {
            if (get_eq_char(ch, WEAR_BOTH) != null) {
                if (!remove_obj_loc(ch, WEAR_BOTH, fReplace)) {
                    return;
                }
                hold_a_light(ch, obj, WEAR_LEFT);
            } else if (get_eq_char(ch, WEAR_LEFT) == null) {
                hold_a_light(ch, obj, WEAR_LEFT);
            } else if (get_eq_char(ch, WEAR_RIGHT) == null) {
                hold_a_light(ch, obj, WEAR_RIGHT);
            } else if (remove_obj_loc(ch, WEAR_LEFT, fReplace)) {
                hold_a_light(ch, obj, WEAR_LEFT);
            } else if (remove_obj_loc(ch, WEAR_RIGHT, fReplace)) {
                hold_a_light(ch, obj, WEAR_RIGHT);
            } else {
                send_to_char("You can't hold a light right now.\n", ch);
            }
            return;
        }

        if (CAN_WEAR(obj, ITEM_WEAR_FINGER)) {
            wear_multi(ch, obj, WEAR_FINGER, fReplace);
            return;
        }

        if (CAN_WEAR(obj, ITEM_WEAR_NECK)) {
            wear_multi(ch, obj, WEAR_NECK, fReplace);
            return;
        }

        if (CAN_WEAR(obj, ITEM_WEAR_BODY)) {
            if (!remove_obj_loc(ch, WEAR_BODY, fReplace)) {
                return;
            }
            act("$n wears $p on $s torso.", ch, obj, null, TO_ROOM);
            act("You wear $p on your torso.", ch, obj, null, TO_CHAR);
            equip_char(ch, obj, WEAR_BODY);
            return;
        }

        if (CAN_WEAR(obj, ITEM_WEAR_HEAD)) {
            if (!remove_obj_loc(ch, WEAR_HEAD, fReplace)) {
                return;
            }
            act("$n wears $p on $s head.", ch, obj, null, TO_ROOM);
            act("You wear $p on your head.", ch, obj, null, TO_CHAR);
            equip_char(ch, obj, WEAR_HEAD);
            return;
        }

        if (CAN_WEAR(obj, ITEM_WEAR_LEGS)) {
            if (!remove_obj_loc(ch, WEAR_LEGS, fReplace)) {
                return;
            }
            act("$n wears $p on $s legs.", ch, obj, null, TO_ROOM);
            act("You wear $p on your legs.", ch, obj, null, TO_CHAR);
            equip_char(ch, obj, WEAR_LEGS);
            return;
        }

        if (CAN_WEAR(obj, ITEM_WEAR_FEET)) {
            if (!remove_obj_loc(ch, WEAR_FEET, fReplace)) {
                return;
            }
            act("$n wears $p on $s feet.", ch, obj, null, TO_ROOM);
            act("You wear $p on your feet.", ch, obj, null, TO_CHAR);
            equip_char(ch, obj, WEAR_FEET);
            return;
        }

        if (CAN_WEAR(obj, ITEM_WEAR_HANDS)) {
            if (!remove_obj_loc(ch, WEAR_HANDS, fReplace)) {
                return;
            }
            act("$n wears $p on $s hands.", ch, obj, null, TO_ROOM);
            act("You wear $p on your hands.", ch, obj, null, TO_CHAR);
            equip_char(ch, obj, WEAR_HANDS);
            return;
        }

        if (CAN_WEAR(obj, ITEM_WEAR_ARMS)) {
            if (!remove_obj_loc(ch, WEAR_ARMS, fReplace)) {
                return;
            }
            act("$n wears $p on $s arms.", ch, obj, null, TO_ROOM);
            act("You wear $p on your arms.", ch, obj, null, TO_CHAR);
            equip_char(ch, obj, WEAR_ARMS);
            return;
        }

        if (CAN_WEAR(obj, ITEM_WEAR_ABOUT)) {
            if (!remove_obj_loc(ch, WEAR_ABOUT, fReplace)) {
                return;
            }
            act("$n wears $p about $s torso.", ch, obj, null, TO_ROOM);
            act("You wear $p about your torso.", ch, obj, null, TO_CHAR);
            equip_char(ch, obj, WEAR_ABOUT);
            return;
        }

        if (CAN_WEAR(obj, ITEM_WEAR_WAIST)) {
            if (!remove_obj_loc(ch, WEAR_WAIST, fReplace)) {
                return;
            }
            act("$n wears $p about $s waist.", ch, obj, null, TO_ROOM);
            act("You wear $p about your waist.", ch, obj, null, TO_CHAR);
            equip_char(ch, obj, WEAR_WAIST);
            return;
        }

        if (CAN_WEAR(obj, ITEM_WEAR_WRIST)) {
            wear_multi(ch, obj, WEAR_WRIST, fReplace);
            return;
        }

        if (CAN_WEAR(obj, ITEM_WEAR_SHIELD)) {
            if (get_eq_char(ch, WEAR_BOTH) != null) {
                if (!remove_obj_loc(ch, WEAR_BOTH, fReplace)) {
                    return;
                }
                hold_a_shield(ch, obj, WEAR_LEFT);
            } else if (get_eq_char(ch, WEAR_LEFT) == null) {
                hold_a_shield(ch, obj, WEAR_LEFT);
            } else if (get_eq_char(ch, WEAR_RIGHT) == null) {
                hold_a_shield(ch, obj, WEAR_RIGHT);
            } else if (remove_obj_loc(ch, WEAR_LEFT, fReplace)) {
                hold_a_shield(ch, obj, WEAR_LEFT);
            } else if (remove_obj_loc(ch, WEAR_RIGHT, fReplace)) {
                hold_a_shield(ch, obj, WEAR_RIGHT);
            } else {
                send_to_char("You can't hold a shield right now.\n", ch);
            }
            return;
        }

        if (CAN_WEAR(obj, ITEM_WIELD)) {
            wear_a_wield(ch, obj, fReplace);
            return;
        }

        if (CAN_WEAR(obj, ITEM_HOLD)) {
            if (get_eq_char(ch, WEAR_BOTH) != null) {
                if (!remove_obj_loc(ch, WEAR_BOTH, fReplace)) {
                    return;
                }
                hold_a_thing(ch, obj, WEAR_LEFT);
            } else if (get_eq_char(ch, WEAR_LEFT) == null) {
                hold_a_thing(ch, obj, WEAR_LEFT);
            } else if (get_eq_char(ch, WEAR_RIGHT) == null) {
                hold_a_thing(ch, obj, WEAR_RIGHT);
            } else if (remove_obj_loc(ch, WEAR_LEFT, fReplace)) {
                hold_a_thing(ch, obj, WEAR_LEFT);
            } else if (remove_obj_loc(ch, WEAR_RIGHT, fReplace)) {
                hold_a_thing(ch, obj, WEAR_RIGHT);
            } else {
                send_to_char("You can't hold a thing right now.\n", ch);
            }
            return;
        }


        if (CAN_WEAR(obj, ITEM_WEAR_FLOAT)) {
            if (!remove_obj_loc(ch, WEAR_FLOAT, fReplace)) {
                return;
            }
            act("$n releases $p to float next to $m.", ch, obj, null, TO_ROOM);
            act("You release $p and it floats next to you.", ch, obj, null, TO_CHAR);
            equip_char(ch, obj, WEAR_FLOAT);
            return;
        }

        if (CAN_WEAR(obj, ITEM_WEAR_TATTOO) && IS_IMMORTAL(ch)) {
            wear_multi(ch, obj, WEAR_TATTOO, fReplace);
            return;
        }

        if (fReplace) {
            send_to_char("You can't wear, wield, or hold that.\n", ch);
        }

    }


    static void do_wear(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        StringBuilder arg = new StringBuilder();

        one_argument(argument, arg);

        if (arg.isEmpty()) {
            send_to_char("Wear, wield, or hold what?\n", ch);
            return;
        }

        if (!str_cmp(arg.toString(), "all")) {
            OBJ_DATA obj_next;

            for (obj = ch.carrying; obj != null; obj = obj_next) {
                obj_next = obj.next_content;
                if (obj.wear_loc == WEAR_NONE && can_see_obj(ch, obj)) {
                    wear_obj(ch, obj, false);
                }
            }
        } else {
            if ((obj = get_obj_carry(ch, arg.toString())) == null) {
                send_to_char("You do not have that item.\n", ch);
                return;
            }

            wear_obj(ch, obj, true);
        }

    }


    static void do_remove(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.isEmpty()) {
            send_to_char("Remove what?\n", ch);
            return;
        }


        if (!str_cmp(arg.toString(), "all")) {
            OBJ_DATA obj_next;

            for (obj = ch.carrying; obj != null; obj = obj_next) {
                obj_next = obj.next_content;
                if (obj.wear_loc != WEAR_NONE && can_see_obj(ch, obj)) {
                    remove_obj(ch, obj, true);
                }
            }
            return;
        }

        if ((obj = get_obj_wear(ch, arg.toString())) == null) {
            send_to_char("You do not have that item.\n", ch);
            return;
        }

        remove_obj(ch, obj, true);
    }


    static void do_sacrifice(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        OBJ_DATA obj_content;
        OBJ_DATA obj_next;
        int silver;
        int iScatter;
        boolean fScatter;

        /* variables for AUTOSPLIT */
        CHAR_DATA gch;
        int members;

        StringBuilder argb = new StringBuilder();
        one_argument(argument, argb);
        String arg = argb.toString();

        if (arg.isEmpty() || !str_cmp(arg, ch.name)) {
            act("$n offers $mself to gods, who graciously declines.",
                    ch, null, null, TO_ROOM);
            send_to_char(
                    "Gods appreciates your offer and may accept it later.\n", ch);
            return;
        }

        obj = get_obj_list(ch, arg, ch.in_room.contents);
        if (obj == null) {
            send_to_char("You can't find it.\n", ch);
            return;
        }

        if (obj.item_type == ITEM_CORPSE_PC && ch.level < MAX_LEVEL) {
            send_to_char("Gods wouldn't like that.\n", ch);
            return;
        }


        if (!CAN_WEAR(obj, ITEM_TAKE) || CAN_WEAR(obj, ITEM_NO_SAC)) {
            act("$p is not an acceptable sacrifice.", ch, obj, 0, TO_CHAR);
            return;
        }

        silver = UMAX(1, number_fuzzy(obj.level));

        if (obj.item_type != ITEM_CORPSE_NPC && obj.item_type != ITEM_CORPSE_PC) {
            silver = UMIN(silver, obj.cost);
        }

        TextBuffer buf = new TextBuffer();
        if (silver == 1) {
            send_to_char("Gods give you one silver coin for your sacrifice.\n", ch);
        } else {

            buf.sprintf("Gods give you %d silver coins for your sacrifice.\n", silver);
            send_to_char(buf, ch);
        }

        ch.silver += silver;

        if (IS_SET(ch.act, PLR_AUTOSPLIT)) { /* AUTOSPLIT code */
            members = 0;
            for (gch = ch.in_room.people; gch != null; gch = gch.next_in_room) {
                if (is_same_group(gch, ch)) {
                    members++;
                }
            }

            if (members > 1 && silver > 1) {
                do_split(ch, "" + silver);
            }
        }

        act("$n sacrifices $p to gods.", ch, obj, null, TO_ROOM);

        if (IS_SET(obj.progtypes, OPROG_SAC)) {
            if (obj.pIndexData.oprogs.sac_prog.run(obj, ch)) {
                return;
            }
        }

        wiznet("$N sends up $p as a burnt offering.", ch, obj, WIZ_SACCING, 0, 0);
        fScatter = true;
        if ((obj.item_type == ITEM_CORPSE_NPC) ||
                (obj.item_type == ITEM_CORPSE_PC)) {
            iScatter = 0;
            OBJ_DATA[] two_objs = new OBJ_DATA[2];
            for (obj_content = obj.contains; obj_content != null; obj_content = obj_next) {
                obj_next = obj_content.next_content;
                two_objs[iScatter < 1 ? 0 : 1] = obj_content;
                obj_from_obj(obj_content);
                obj_to_room(obj_content, ch.in_room);
                iScatter++;
            }
            if (iScatter == 1) {
                act("Your sacrifice reveals $p.", ch, two_objs[0], null, TO_CHAR);
                act("$p is revealed by $n's sacrifice.", ch, two_objs[0], null, TO_ROOM);
            }
            if (iScatter == 2) {
                act("Your sacrifice reveals $p and $P.", ch, two_objs[0], two_objs[1], TO_CHAR);
                act("$p and $P are revealed by $n's sacrifice.", ch, two_objs[0], two_objs[1], TO_ROOM);
            }
            buf.sprintf("As you sacrifice the corpse, ");
            StringBuilder buf2 = new StringBuilder("As $n sacrifices the corpse, ");
            if (iScatter < 3) {
                fScatter = false;
            } else if (iScatter < 5) {
                buf.append("few things ");
                buf2.append("few things ");
            } else if (iScatter < 9) {
                buf.append("a bunch of objects ");
                buf2.append("a bunch of objects ");
            } else if (iScatter < 15) {
                buf.append("many things ");
                buf2.append("many things ");
            } else {
                buf.append("a lot of objects ");
                buf2.append("a lot of objects ");
            }
            buf.append("on it, ");
            buf2.append("on it, ");

            switch (ch.in_room.sector_type) {
                case SECT_FIELD -> {
                    buf.append("scatter on the dirt.");
                    buf2.append("scatter on the dirt.");
                }
                case SECT_FOREST -> {
                    buf.append("scatter on the dirt.");
                    buf2.append("scatter on the dirt.");
                }
                case SECT_WATER_SWIM -> {
                    buf.append("scatter over the water.");
                    buf2.append("scatter over the water.");
                }
                case SECT_WATER_NOSWIM -> {
                    buf.append("scatter over the water.");
                    buf2.append("scatter over the water.");
                }
                default -> {
                    buf.append("scatter around.");
                    buf2.append("scatter around.");
                }
            }
            if (fScatter) {
                act(buf.toString(), ch, null, null, TO_CHAR);
                act(buf2, ch, null, null, TO_ROOM);
            }

        }

        extract_obj(obj);
    }


    static void do_quaff(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (ch.cabal == CABAL_BATTLE && !IS_IMMORTAL(ch)) {
            send_to_char("You are a BattleRager, not a filthy magician!\n", ch);
            return;
        }

        if (arg.isEmpty()) {
            send_to_char("Quaff what?\n", ch);
            return;
        }

        if ((obj = get_obj_carry(ch, arg.toString())) == null) {
            send_to_char("You do not have that potion.\n", ch);
            return;
        }

        if (obj.item_type != ITEM_POTION) {
            send_to_char("You can quaff only potions.\n", ch);
            return;
        }

        if (ch.level < obj.level) {
            send_to_char("This liquid is too powerful for you to drink.\n", ch);
            return;
        }


        act("$n quaffs $p.", ch, obj, null, TO_ROOM);
        act("You quaff $p.", ch, obj, null, TO_CHAR);

        obj_cast_spell(Skill.skills[obj.value[1]], obj.value[0], ch, ch, null);
        obj_cast_spell(Skill.skills[obj.value[2]], obj.value[0], ch, ch, null);
        obj_cast_spell(Skill.skills[obj.value[3]], obj.value[0], ch, ch, null);

        if ((ch.last_fight_time != -1 &&
                (current_time - ch.last_fight_time) < FIGHT_DELAY_TIME) ||
                (ch.fighting != null)) {
            WAIT_STATE(ch, 2 * PULSE_VIOLENCE);
        }

        extract_obj(obj);
        obj_to_char(create_object(get_obj_index(OBJ_VNUM_POTION_VIAL), 0), ch);

        if (IS_NPC(ch)) {
            do_drop(ch, "vial");
        }

    }


    static void do_recite(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        OBJ_DATA scroll;
        OBJ_DATA obj;

        if (ch.cabal == CABAL_BATTLE) {
            send_to_char(
                    "RECITE?!  You are a battle rager, not a filthy magician!\n", ch);
            return;
        }

        StringBuilder arg1 = new StringBuilder();
        StringBuilder arg2 = new StringBuilder();
        argument = one_argument(argument, arg1);
        one_argument(argument, arg2);

        if ((scroll = get_obj_carry(ch, arg1.toString())) == null) {
            send_to_char("You do not have that scroll.\n", ch);
            return;
        }

        if (scroll.item_type != ITEM_SCROLL) {
            send_to_char("You can recite only scrolls.\n", ch);
            return;
        }


        if (ch.level < scroll.level) {
            send_to_char(
                    "This scroll is too complex for you to comprehend.\n", ch);
            return;
        }

        obj = null;
        if (arg2.isEmpty()) {
            victim = ch;
        } else {
            if ((victim = get_char_room(ch, arg2.toString())) == null
                    && (obj = get_obj_here(ch, arg2.toString())) == null) {
                send_to_char("You can't find it.\n", ch);
                return;
            }
        }

        act("$n recites $p.", ch, scroll, null, TO_ROOM);
        act("You recite $p.", ch, scroll, null, TO_CHAR);

        if (number_percent() >= get_skill(ch, gsn_scrolls) * 4 / 5) {
            send_to_char("You mispronounce a syllable.\n", ch);
            check_improve(ch, gsn_scrolls, false, 2);
        } else {
            obj_cast_spell(Skill.skills[scroll.value[1]], scroll.value[0], ch, victim, obj);
            obj_cast_spell(Skill.skills[scroll.value[2]], scroll.value[0], ch, victim, obj);
            obj_cast_spell(Skill.skills[scroll.value[3]], scroll.value[0], ch, victim, obj);
            check_improve(ch, gsn_scrolls, true, 2);

            if ((ch.last_fight_time != -1 &&
                    (current_time - ch.last_fight_time) < FIGHT_DELAY_TIME) ||
                    (ch.fighting != null)) {
                WAIT_STATE(ch, 2 * PULSE_VIOLENCE);
            }
        }

        extract_obj(scroll);
    }


    static void do_brandish(CHAR_DATA ch) {
        CHAR_DATA vch;
        CHAR_DATA vch_next;
        OBJ_DATA staff;
        int sn;

        if (ch.cabal == CABAL_BATTLE) {
            send_to_char("You are not a filthy magician!\n", ch);
            return;
        }

        if ((staff = get_hold_char(ch)) == null) {
            send_to_char("You hold nothing in your hand.\n", ch);
            return;
        }

        if (staff.item_type != ITEM_STAFF) {
            send_to_char("You can brandish only with a staff.\n", ch);
            return;
        }

        if ((sn = staff.value[3]) < 0 || sn >= MAX_SKILL || !Skill.skills[sn].isSpell()) {
            bug("Do_brandish: bad sn %s.", sn);
            return;
        }
        Skill skill = Skill.skills[sn];

        WAIT_STATE(ch, 2 * PULSE_VIOLENCE);

        if (staff.value[2] > 0) {
            act("$n brandishes $p.", ch, staff, null, TO_ROOM);
            act("You brandish $p.", ch, staff, null, TO_CHAR);
            if (ch.level + 3 < staff.level
                    || number_percent() >= 10 + get_skill(ch, gsn_staves) * 4 / 5) {
                act("You fail to invoke $p.", ch, staff, null, TO_CHAR);
                act("...and nothing happens.", ch, null, null, TO_ROOM);
                check_improve(ch, gsn_staves, false, 2);
            } else {
                for (vch = ch.in_room.people; vch != null; vch = vch_next) {
                    vch_next = vch.next_in_room;

                    switch (skill.target) {
                        default -> {
                            bug("Do_brandish: bad target for sn %d.", sn);
                            return;
                        }
                        case TAR_IGNORE -> {
                            if (vch != ch) {
                                continue;
                            }
                        }
                        case TAR_CHAR_OFFENSIVE -> {
                            if (IS_NPC(ch) == IS_NPC(vch)) {
                                continue;
                            }
                        }
                        case TAR_CHAR_DEFENSIVE -> {
                            if (IS_NPC(ch) != IS_NPC(vch)) {
                                continue;
                            }
                        }
                        case TAR_CHAR_SELF -> {
                            if (vch != ch) {
                                continue;
                            }
                        }
                    }

                    obj_cast_spell(Skill.skills[staff.value[3]], staff.value[0], ch, vch, null);
                    check_improve(ch, gsn_staves, true, 2);
                }
            }
        }

        if (--staff.value[2] <= 0) {
            act("$n's $p blazes bright and is gone.", ch, staff, null, TO_ROOM);
            act("Your $p blazes bright and is gone.", ch, staff, null, TO_CHAR);
            extract_obj(staff);
        }

    }


    static void do_zap(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        OBJ_DATA wand;
        OBJ_DATA obj;

        if (ch.cabal == CABAL_BATTLE) {
            send_to_char("You'd destroy the magic, not use it!\n", ch);
            return;
        }

        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);
        if (arg.isEmpty() && ch.fighting == null) {
            send_to_char("Zap whom or what?\n", ch);
            return;
        }

        if ((wand = get_hold_char(ch)) == null) {
            send_to_char("You hold nothing in your hand.\n", ch);
            return;
        }

        if (wand.item_type != ITEM_WAND) {
            send_to_char("You can zap only with a wand.\n", ch);
            return;
        }

        obj = null;
        if (arg.isEmpty()) {
            if (ch.fighting != null) {
                victim = ch.fighting;
            } else {
                send_to_char("Zap whom or what?\n", ch);
                return;
            }
        } else {
            if ((victim = get_char_room(ch, arg.toString())) == null
                    && (obj = get_obj_here(ch, arg.toString())) == null) {
                send_to_char("You can't find it.\n", ch);
                return;
            }
        }

        WAIT_STATE(ch, 2 * PULSE_VIOLENCE);

        if (wand.value[2] > 0) {
            if (victim != null) {
                act("$n zaps $N with $p.", ch, wand, victim, TO_ROOM);
                act("You zap $N with $p.", ch, wand, victim, TO_CHAR);
            } else {
                act("$n zaps $P with $p.", ch, wand, obj, TO_ROOM);
                act("You zap $P with $p.", ch, wand, obj, TO_CHAR);
            }

            if (ch.level + 5 < wand.level
                    || number_percent() >= 20 + get_skill(ch, gsn_wands) * 4 / 5) {
                act("Your efforts with $p produce only smoke and sparks.",
                        ch, wand, null, TO_CHAR);
                act("$n's efforts with $p produce only smoke and sparks.",
                        ch, wand, null, TO_ROOM);
                check_improve(ch, gsn_wands, false, 2);
            } else {
                obj_cast_spell(Skill.skills[wand.value[3]], wand.value[0], ch, victim, obj);
                check_improve(ch, gsn_wands, true, 2);
            }
        }

        if (--wand.value[2] <= 0) {
            act("$n's $p explodes into fragments.", ch, wand, null, TO_ROOM);
            act("Your $p explodes into fragments.", ch, wand, null, TO_CHAR);
            extract_obj(wand);
        }

    }


    static void do_steal(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        CHAR_DATA tmp_ch;
        OBJ_DATA obj;
        OBJ_DATA obj_inve;
        int percent, number;

        StringBuilder arg1 = new StringBuilder();
        StringBuilder arg2 = new StringBuilder();
        argument = one_argument(argument, arg1);
        one_argument(argument, arg2);

        if (arg1.isEmpty() || arg2.length() == 0) {
            send_to_char("Steal what from whom?\n", ch);
            return;
        }

        if (IS_NPC(ch) && IS_SET(ch.affected_by, AFF_CHARM)
                && (ch.master != null)) {
            send_to_char("You are to dazed to steal anything.\n", ch);
            return;
        }

        if ((victim = get_char_room(ch, arg2.toString())) == null) {
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
            send_to_char("You'd better not -- you might get hit.\n", ch);
            return;
        }

        ch.last_death_time = -1;

        tmp_ch = (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(victim)) ?
                ch.doppel : ch;

        WAIT_STATE(ch, gsn_steal.beats);
        percent = get_skill(ch, gsn_steal) + (IS_AWAKE(victim) ? -10 : 30);
        percent -= can_see(victim, ch) ? 10 : 0;
        percent -= (IS_NPC(victim) && victim.pIndexData.pShop != null) ? 25 : 0;
        percent -= (victim.level > ch.level) ?
                ((victim.level - ch.level) * 2) : 0;

        obj = null;
        String arg1str = arg1.toString();
        if (str_cmp(arg1str, "coin")
                && str_cmp(arg1str, "coins")
                && str_cmp(arg1str, "silver")
                && str_cmp(arg1str, "gold")) {
            if ((obj = get_obj_carry(victim, arg1str)) == null) {
                send_to_char("You can't find it.\n", ch);
                return;
            }

        }

        if (obj != null && obj.pIndexData.limit != -1) {
            if ((IS_OBJ_STAT(obj, ITEM_ANTI_EVIL) && IS_EVIL(ch))
                    || (IS_OBJ_STAT(obj, ITEM_ANTI_GOOD) && IS_GOOD(ch))
                    || (IS_OBJ_STAT(obj, ITEM_ANTI_NEUTRAL) && IS_NEUTRAL(ch))) {
                act("You are zapped by $p.", ch, obj, null, TO_CHAR);
                act("$n is zapped by $p.", ch, obj, null, TO_ROOM);
                percent = 0;
            }

            if (obj.pIndexData.limit < obj.pIndexData.count) {
                act("Gods doesn't allow $p to be stolen.", ch, obj, null, TO_CHAR);
                act("Gods doesn't approve $n's behaviour.", ch, obj, null, TO_ROOM);
                percent = 0;
            }
        }

        number = (obj != null) ? get_obj_number(obj) : 0;

        if (ch.carry_number + number > can_carry_n(ch)) {
            send_to_char("You can't carry that much item.\n", ch);
            return;
        }

        if (victim.position == POS_FIGHTING
                || number_percent() > percent) {
            /*
            * Failure.
            */

            send_to_char("Oops.\n", ch);
            if (!IS_AFFECTED(victim, AFF_SLEEP)) {
                victim.position = victim.position == POS_SLEEPING ? POS_STANDING :
                        victim.position;
                act("$n tried to steal from you.\n", ch, null, victim, TO_VICT);
            }
            act("$n tried to steal from $N.\n", ch, null, victim, TO_NOTVICT);

            TextBuffer buf = new TextBuffer();
            switch (number_range(0, 3)) {
                case 0 -> buf.sprintf("%s is a lousy thief!", tmp_ch.name);
                case 1 -> buf.sprintf("%s couldn't rob %s way out of a paper bag!",
                        tmp_ch.name, (tmp_ch.sex == 2) ? "her" : "his");
                case 2 -> buf.sprintf("%s tried to rob me!", tmp_ch.name);
                case 3 -> buf.sprintf("Keep your hands out of there, %s!", tmp_ch.name);
            }
            if (IS_AWAKE(victim)) {
                do_yell(victim, buf.toString());
            }
            if (!IS_NPC(ch)) {
                if (IS_NPC(victim)) {
                    check_improve(ch, gsn_steal, false, 2);
                    multi_hit(victim, ch, null);
                }
            }

            return;
        }

        if (!str_cmp(arg1str, "coin")
                || !str_cmp(arg1str, "coins")
                || !str_cmp(arg1str, "silver")
                || !str_cmp(arg1str, "gold")) {
            int amount_s = 0;
            int amount_g = 0;
            if (!str_cmp(arg1str, "silver") ||
                    !str_cmp(arg1str, "coin") ||
                    !str_cmp(arg1str, "coins")) {
                amount_s = victim.silver * number_range(1, 20) / 100;
            } else if (!str_cmp(arg1str, "gold")) {
                amount_g = victim.gold * number_range(1, 7) / 100;
            }

            if (amount_s <= 0 && amount_g <= 0) {
                send_to_char("You couldn't get any coins.\n", ch);
                return;
            }

            ch.gold += amount_g;
            victim.gold -= amount_g;
            ch.silver += amount_s;
            victim.silver -= amount_s;
            TextBuffer buf = new TextBuffer();
            buf.sprintf("Bingo!  You got %d %s coins.\n",
                    amount_s != 0 ? amount_s : amount_g,
                    amount_s != 0 ? "silver" : "gold");

            send_to_char(buf, ch);
            check_improve(ch, gsn_steal, true, 2);
            return;
        }

        if (!can_drop_obj(ch, obj)
            /* ||   IS_SET(obj.extra_flags, ITEM_INVENTORY)*/
            /* ||  obj.level > ch.level */) {
            send_to_char("You can't pry it away.\n", ch);
            return;
        }

        if (ch.carry_number + get_obj_number(obj) > can_carry_n(ch)) {
            send_to_char("You have your hands full.\n", ch);
            return;
        }

        if (ch.carry_weight + get_obj_weight(obj) > can_carry_w(ch)) {
            send_to_char("You can't carry that much weight.\n", ch);
            return;
        }

        if (!IS_SET(obj.extra_flags, ITEM_INVENTORY)) {
            obj_from_char(obj);
            obj_to_char(obj, ch);
            send_to_char("You got it!\n", ch);
            check_improve(ch, gsn_steal, true, 2);
            if (IS_SET(obj.progtypes, OPROG_GET)) {
                obj.pIndexData.oprogs.get_prog.run(obj, ch);
            }
        } else {
            obj_inve = create_object(obj.pIndexData, 0);
            clone_object(obj, obj_inve);
            obj_inve.extra_flags = REMOVE_BIT(obj_inve.extra_flags, ITEM_INVENTORY);
            obj_to_char(obj_inve, ch);
            send_to_char("You got one of them!\n", ch);
            check_improve(ch, gsn_steal, true, 1);
            if (IS_SET(obj_inve.progtypes, OPROG_GET)) {
                obj_inve.pIndexData.oprogs.get_prog.run(obj_inve, ch);
            }
        }

    }

/*
 * Shopping commands.
 */

    static CHAR_DATA find_keeper(CHAR_DATA ch) {
        CHAR_DATA keeper;
        SHOP_DATA pShop = null;
        for (keeper = ch.in_room.people; keeper != null; keeper = keeper.next_in_room) {
            if (IS_NPC(keeper) && (pShop = keeper.pIndexData.pShop) != null) {
                break;
            }
        }

        if (pShop == null) {
            send_to_char("You can't do that here.\n", ch);
            return null;
        }

        if (IS_SET(keeper.in_room.area.area_flag, AREA_HOMETOWN)
                && !IS_NPC(ch) && IS_SET(ch.act, PLR_WANTED)) {
            do_say(keeper, "Criminals are not welcome!");
            TextBuffer buf = new TextBuffer();
            buf.sprintf("%s the CRIMINAL is over here!\n", ch.name);
            do_yell(keeper, buf.toString());
            return null;
        }

        /*
         * Shop hours.
         */
        if (time_info.hour < pShop.open_hour) {
            do_say(keeper, "Sorry, I am closed. Come back later.");
            return null;
        }

        if (time_info.hour > pShop.close_hour) {
            do_say(keeper, "Sorry, I am closed. Come back tomorrow.");
            return null;
        }

        /*
         * Invisible or hidden people.
         */
        if (!can_see(keeper, ch) && !IS_IMMORTAL(ch)) {
            do_say(keeper, "I don't trade with folks I can't see.");
            return null;
        }

        return keeper;
    }

/* insert an object at the right spot for the keeper */

    static void obj_to_keeper(OBJ_DATA obj, CHAR_DATA ch) {
        OBJ_DATA t_obj, t_obj_next;

        /* see if any duplicates are found */
        for (t_obj = ch.carrying; t_obj != null; t_obj = t_obj_next) {
            t_obj_next = t_obj.next_content;

            if (obj.pIndexData == t_obj.pIndexData
                    && !str_cmp(obj.short_descr, t_obj.short_descr)) {
                if (IS_OBJ_STAT(t_obj, ITEM_INVENTORY)) {
                    extract_obj(obj);
                    return;
                }
                obj.cost = t_obj.cost; /* keep it standard */
                break;
            }
        }

        if (t_obj == null) {
            obj.next_content = ch.carrying;
            ch.carrying = obj;
        } else {
            obj.next_content = t_obj.next_content;
            t_obj.next_content = obj;
        }

        obj.carried_by = ch;
        obj.in_room = null;
        obj.in_obj = null;
        ch.carry_number += get_obj_number(obj);
        ch.carry_weight += get_obj_weight(obj);
    }

/* get an object from a shopkeeper's list */

    static OBJ_DATA get_obj_keeper(CHAR_DATA ch, CHAR_DATA keeper, String argument) {
        OBJ_DATA obj;
        int number;
        int count;
        StringBuilder arg = new StringBuilder();
        number = number_argument(argument, arg);
        count = 0;
        for (obj = keeper.carrying; obj != null; obj = obj.next_content) {
            if (obj.wear_loc == WEAR_NONE
                    && can_see_obj(keeper, obj)
                    && can_see_obj(ch, obj)
                    && is_name(arg.toString(), obj.name)) {
                if (++count == number) {
                    return obj;
                }

                /* skip other objects of the same name */
                while (obj.next_content != null
                        && obj.pIndexData == obj.next_content.pIndexData
                        && !str_cmp(obj.short_descr, obj.next_content.short_descr)) {
                    obj = obj.next_content;
                }
            }
        }

        return null;
    }

    static int get_cost(CHAR_DATA keeper, OBJ_DATA obj, boolean fBuy) {
        SHOP_DATA pShop;
        int cost;

        if (obj == null || (pShop = keeper.pIndexData.pShop) == null) {
            return 0;
        }

        if (IS_OBJ_STAT(obj, ITEM_NOSELL)) {
            return 0;
        }

        if (fBuy) {
            cost = obj.cost * pShop.profit_buy / 100;
        } else {
            OBJ_DATA obj2;
            int itype;

            cost = 0;
            for (itype = 0; itype < MAX_TRADE; itype++) {
                if (obj.item_type == pShop.buy_type[itype]) {
                    cost = obj.cost * pShop.profit_sell / 100;
                    break;
                }
            }

            if (!IS_OBJ_STAT(obj, ITEM_SELL_EXTRACT)) {
                for (obj2 = keeper.carrying; obj2 != null; obj2 = obj2.next_content) {
                    if (obj.pIndexData == obj2.pIndexData
                            && !str_cmp(obj.short_descr, obj2.short_descr)) {
                        return 0;
                    }
/*
            if (IS_OBJ_STAT(obj2,ITEM_INVENTORY))
            cost /= 2;
            else
                        cost = cost * 3 / 4;
*/
                }
            }
        }

        if (obj.item_type == ITEM_STAFF || obj.item_type == ITEM_WAND) {
            if (obj.value[1] == 0) {
                cost /= 4;
            } else {
                cost = cost * obj.value[2] / obj.value[1];
            }
        }

        return cost;
    }


    static void do_buy(CHAR_DATA ch, String argument) {
        int cost, roll;

        if (argument.isEmpty()) {
            send_to_char("Buy what?\n", ch);
            return;
        }

        if (IS_SET(ch.in_room.room_flags, ROOM_PET_SHOP)) {
            CHAR_DATA pet;
            ROOM_INDEX_DATA pRoomIndexNext;
            ROOM_INDEX_DATA in_room;

            /* added by kio */
            argument = smash_tilde(argument);

            if (IS_NPC(ch)) {
                return;
            }

            StringBuilder arg = new StringBuilder();
            argument = one_argument(argument, arg);

            /* hack to make new thalos pets work */

            if (ch.in_room.vnum == 9621) {
                pRoomIndexNext = get_room_index(9706);
            } else {
                pRoomIndexNext = get_room_index(ch.in_room.vnum + 1);
            }
            if (pRoomIndexNext == null) {
                bug("Do_buy: bad pet shop at vnum %d.", ch.in_room.vnum);
                send_to_char("Sorry, you can't buy that here.\n", ch);
                return;
            }

            in_room = ch.in_room;
            ch.in_room = pRoomIndexNext;
            pet = get_char_room(ch, arg.toString());
            ch.in_room = in_room;

            if (pet == null || !IS_SET(pet.act, ACT_PET) || !IS_NPC(pet)) {
                send_to_char("Sorry, you can't buy that here.\n", ch);
                return;
            }

            if (IS_SET(pet.act, ACT_RIDEABLE)
                    && ch.cabal == CABAL_KNIGHT
                    && MOUNTED(ch) == null) {
                cost = 10 * pet.level * pet.level;

                if ((ch.silver + 100 * ch.gold) < cost) {
                    send_to_char("You can't afford it.\n", ch);
                    return;
                }

                if (ch.level < pet.level + 5) {
                    send_to_char(
                            "You're not powerful enough to master this pet.\n", ch);
                    return;
                }

                deduct_cost(ch, cost);
                pet = create_mobile(pet.pIndexData);
                pet.comm = COMM_NOTELL | COMM_NOSHOUT | COMM_NOCHANNELS;

                char_to_room(pet, ch.in_room);
                do_mount(ch, pet.name);
                send_to_char("Enjoy your mount.\n", ch);
                act("$n bought $N as a mount.", ch, null, pet, TO_ROOM);
                return;
            }

            if (ch.pet != null) {
                send_to_char("You already own a pet.\n", ch);
                return;
            }

            cost = 10 * pet.level * pet.level;

            if ((ch.silver + 100 * ch.gold) < cost) {
                send_to_char("You can't afford it.\n", ch);
                return;
            }

            if (ch.level < pet.level) {
                send_to_char(
                        "You're not powerful enough to master this pet.\n", ch);
                return;
            }

            /* haggle */
            TextBuffer buf = new TextBuffer();
            roll = number_percent();
            if (roll < get_skill(ch, gsn_haggle)) {
                cost -= cost / 2 * roll / 100;
                buf.sprintf("You haggle the price down to %d coins.\n", cost);
                send_to_char(buf, ch);
                check_improve(ch, gsn_haggle, true, 4);

            }

            deduct_cost(ch, cost);
            pet = create_mobile(pet.pIndexData);
            pet.act = SET_BIT(pet.act, ACT_PET);
            pet.affected_by = SET_BIT(pet.affected_by, AFF_CHARM);
            pet.comm = COMM_NOTELL | COMM_NOSHOUT | COMM_NOCHANNELS;

            one_argument(argument, arg);
            if (!arg.isEmpty()) {
                pet.name = pet.name + " " + arg;
            }

            buf.sprintf("%sA neck tag says 'I belong to %s'.\n",
                    pet.description, ch.name);
            pet.description = buf.toString();

            char_to_room(pet, ch.in_room);
            add_follower(pet, ch);
            pet.leader = ch;
            ch.pet = pet;
            send_to_char("Enjoy your pet.\n", ch);
            act("$n bought $N as a pet.", ch, null, pet, TO_ROOM);
        } else {
            CHAR_DATA keeper;
            OBJ_DATA obj, t_obj;
            int number, count = 1;

            if ((keeper = find_keeper(ch)) == null) {
                return;
            }

            StringBuilder arg = new StringBuilder();
            number = multiply_argument(argument, arg);
            if (number < -1 || number > 100) {
                act("$n tells you 'Get real!", keeper, null, ch, TO_VICT);
                ch.reply = keeper;
                return;
            }

            obj = get_obj_keeper(ch, keeper, arg.toString());
            cost = get_cost(keeper, obj, true);

            if (cost <= 0 || !can_see_obj(ch, obj)) {
                act("$n tells you 'I don't sell that -- try 'list''.",
                        keeper, null, ch, TO_VICT);
                ch.reply = keeper;
                return;
            }

            if (!IS_OBJ_STAT(obj, ITEM_INVENTORY)) {
                for (t_obj = obj.next_content;
                     count < number && t_obj != null;
                     t_obj = t_obj.next_content) {
                    if (t_obj.pIndexData == obj.pIndexData
                            && !str_cmp(t_obj.short_descr, obj.short_descr)) {
                        count++;
                    } else {
                        break;
                    }
                }

                if (count < number) {
                    act("$n tells you 'I don't have that many in stock.'",
                            keeper, null, ch, TO_VICT);
                    ch.reply = keeper;
                    return;
                }
            } else if (obj.pIndexData.limit != -1) {
                count = 1 + obj.pIndexData.limit - obj.pIndexData.count;
                if (count < 1) {
                    act("$n tells you 'Gods will not approve me to sell that.'",
                            keeper, null, ch, TO_VICT);
                    ch.reply = keeper;
                    return;
                }
                if (count < number) {
                    act("$n tells you 'I don't have that many in stock.'",
                            keeper, null, ch, TO_VICT);
                    ch.reply = keeper;
                    return;
                }
            }

            if ((ch.silver + ch.gold * 100) < cost * number) {
                if (number > 1) {
                    act("$n tells you 'You can't afford to buy that many.",
                            keeper, obj, ch, TO_VICT);
                } else {
                    act("$n tells you 'You can't afford to buy $p'.",
                            keeper, obj, ch, TO_VICT);
                }
                ch.reply = keeper;
                return;
            }

            if (obj.level > ch.level) {
                act("$n tells you 'You can't use $p yet'.",
                        keeper, obj, ch, TO_VICT);
                ch.reply = keeper;
                return;
            }

            if (ch.carry_number + number * get_obj_number(obj) > can_carry_n(ch)) {
                send_to_char("You can't carry that many items.\n", ch);
                return;
            }

            if (ch.carry_weight + number * get_obj_weight(obj) > can_carry_w(ch)) {
                send_to_char("You can't carry that much weight.\n", ch);
                return;
            }

            /* haggle */
            roll = number_percent();
            if (!IS_OBJ_STAT(obj, ITEM_SELL_EXTRACT)
                    && roll < get_skill(ch, gsn_haggle)) {
                cost -= obj.cost / 2 * roll / 100;
                act("You haggle with $N.", ch, null, keeper, TO_CHAR);
                check_improve(ch, gsn_haggle, true, 4);
            }

            TextBuffer buf = new TextBuffer();
            if (number > 1) {
                buf.sprintf("$n buys $p[%d].", number);
                act(buf, ch, obj, null, TO_ROOM);
                buf.sprintf("You buy $p[%d] for %d silver.", number, cost * number);
                act(buf, ch, obj, null, TO_CHAR);
            } else {
                act("$n buys $p.", ch, obj, null, TO_ROOM);
                buf.sprintf("You buy $p for %d silver.", cost);
                act(buf.toString(), ch, obj, null, TO_CHAR);
            }
            deduct_cost(ch, cost * number);
            keeper.gold += cost * number / 100;
            keeper.silver += cost * number - (cost * number / 100) * 100;

            for (count = 0; count < number; count++) {
                if (IS_SET(obj.extra_flags, ITEM_INVENTORY)) {
                    t_obj = create_object(obj.pIndexData, obj.level);
                } else {
                    t_obj = obj;
                    obj = obj.next_content;
                    obj_from_char(t_obj);
                }

                if (t_obj.timer > 0 && !IS_OBJ_STAT(t_obj, ITEM_HAD_TIMER)) {
                    t_obj.timer = 0;
                }
                t_obj.extra_flags = REMOVE_BIT(t_obj.extra_flags, ITEM_HAD_TIMER);
                obj_to_char(t_obj, ch);
                if (cost < t_obj.cost) {
                    t_obj.cost = cost;
                }
            }
        }
    }


    static void do_list(CHAR_DATA ch, String argument) {

        if (IS_SET(ch.in_room.room_flags, ROOM_PET_SHOP)) {
            ROOM_INDEX_DATA pRoomIndexNext;
            CHAR_DATA pet;
            boolean found;

            /* hack to make new thalos pets work */

            if (ch.in_room.vnum == 9621) {
                pRoomIndexNext = get_room_index(9706);
            } else {
                pRoomIndexNext = get_room_index(ch.in_room.vnum + 1);
            }

            if (pRoomIndexNext == null) {
                bug("Do_list: bad pet shop at vnum %d.", ch.in_room.vnum);
                send_to_char("You can't do that here.\n", ch);
                return;
            }

            found = false;
            TextBuffer buf = new TextBuffer();
            for (pet = pRoomIndexNext.people; pet != null; pet = pet.next_in_room) {
                if (!IS_NPC(pet)) {
                    continue;     /* :) */
                }
                if (IS_SET(pet.act, ACT_PET)) {
                    if (!found) {
                        found = true;
                        send_to_char("Pets for sale:\n", ch);
                    }
                    buf.sprintf("[%2d] %8d - %s\n",
                            pet.level,
                            10 * pet.level * pet.level,
                            pet.short_descr);
                    send_to_char(buf, ch);
                }
            }
            if (!found) {
                send_to_char("Sorry, we're out of pets right now.\n", ch);
            }
        } else {
            CHAR_DATA keeper;
            OBJ_DATA obj;
            int cost, count;
            boolean found;

            if ((keeper = find_keeper(ch)) == null) {
                return;
            }
            StringBuilder arg = new StringBuilder();

            one_argument(argument, arg);

            found = false;
            TextBuffer buf = new TextBuffer();

            for (obj = keeper.carrying; obj != null; obj = obj.next_content) {
                if (obj.wear_loc == WEAR_NONE
                        && can_see_obj(ch, obj)
                        && (cost = get_cost(keeper, obj, true)) > 0
                        && (arg.isEmpty()
                        || is_name(arg.toString(), obj.name))) {
                    if (!found) {
                        found = true;
                        send_to_char("[Lv Price Qty] Item\n", ch);
                    }

                    if (IS_OBJ_STAT(obj, ITEM_INVENTORY)) {
                        buf.sprintf("[%2d %5d -- ] %s%s\n",
                                obj.level, cost, obj.short_descr,
                                (obj.pIndexData.limit != -1) ? (obj.pIndexData.count > obj.pIndexData.limit) ? " (NOT AVAILABLE NOW)" : " (AVAILABLE)" : "");
                    } else {
                        count = 1;

                        while (obj.next_content != null
                                && obj.pIndexData == obj.next_content.pIndexData
                                && !str_cmp(obj.short_descr,
                                obj.next_content.short_descr)) {
                            obj = obj.next_content;
                            count++;
                        }
                        buf.sprintf("[%2d %5d %2d ] %s\n",
                                obj.level, cost, count, obj.short_descr);
                    }
                    send_to_char(buf, ch);
                }
            }

            if (!found) {
                send_to_char("You can't buy anything here.\n", ch);
            }
        }
    }


    static void do_sell(CHAR_DATA ch, String argument) {
        CHAR_DATA keeper;
        OBJ_DATA obj;
        int cost, roll;
        int gold, silver;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.isEmpty()) {
            send_to_char("Sell what?\n", ch);
            return;
        }

        if ((keeper = find_keeper(ch)) == null) {
            return;
        }

        if ((obj = get_obj_carry(ch, arg.toString())) == null) {
            act("$n tells you 'You don't have that item'.",
                    keeper, null, ch, TO_VICT);
            ch.reply = keeper;
            return;
        }

        if (!can_drop_obj(ch, obj)) {
            send_to_char("You can't let go of it.\n", ch);
            return;
        }

        if (!can_see_obj(keeper, obj)) {
            act("$n doesn't see what you are offering.", keeper, null, ch, TO_VICT);
            return;
        }

        if ((cost = get_cost(keeper, obj, false)) <= 0) {
            act("$n looks uninterested in $p.", keeper, obj, ch, TO_VICT);
            return;
        }
        if (cost > (keeper.silver + 100 * keeper.gold)) {
            act("$n tells you 'I'm afraid I don't have enough wealth to buy $p.",
                    keeper, obj, ch, TO_VICT);
            return;
        }

        act("$n sells $p.", ch, obj, null, TO_ROOM);
        /* haggle */
        roll = number_percent();
        if (!IS_OBJ_STAT(obj, ITEM_SELL_EXTRACT) && roll < get_skill(ch, gsn_haggle)) {
            roll = get_skill(ch, gsn_haggle) + number_range(1, 20) - 10;
            send_to_char("You haggle with the shopkeeper.\n", ch);
            cost += obj.cost / 2 * roll / 100;
            cost = UMIN(cost, 95 * get_cost(keeper, obj, true) / 100);
            cost = UMIN(cost, (keeper.silver + 100 * keeper.gold));
            check_improve(ch, gsn_haggle, true, 4);
        }

        silver = cost - (cost / 100) * 100;
        gold = cost / 100;

        TextBuffer buf = new TextBuffer();
        TextBuffer buf2 = new TextBuffer();
        buf2.sprintf("You sell $p for %s %s%spiece%s.",
                silver != 0 ? "%d silver" : "",                         /* silvers  */
                (silver != 0 && gold != 0) ? "and " : "",       /*   and    */
                gold != 0 ? "%d gold " : "",                /*  golds   */
                silver + gold > 1 ? "s" : "");               /* piece(s) */
        buf.sprintf(buf2.toString(), silver, gold);

        act(buf.toString(), ch, obj, null, TO_CHAR);
        ch.gold += gold;
        ch.silver += silver;
        deduct_cost(keeper, cost);
        if (keeper.gold < 0) {
            keeper.gold = 0;
        }
        if (keeper.silver < 0) {
            keeper.silver = 0;
        }

        if (obj.item_type == ITEM_TRASH || IS_OBJ_STAT(obj, ITEM_SELL_EXTRACT)) {
            extract_obj(obj);
        } else {
            obj_from_char(obj);
            if (obj.timer != 0) {
                obj.extra_flags = SET_BIT(obj.extra_flags, ITEM_HAD_TIMER);
            } else {
                obj.timer = number_range(50, 100);
            }
            obj_to_keeper(obj, keeper);
        }

    }


    static void do_value(CHAR_DATA ch, String argument) {
        CHAR_DATA keeper;
        OBJ_DATA obj;
        int cost;

        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.isEmpty()) {
            send_to_char("Value what?\n", ch);
            return;
        }

        if ((keeper = find_keeper(ch)) == null) {
            return;
        }

        if ((obj = get_obj_carry(ch, arg.toString())) == null) {
            act("$n tells you 'You don't have that item'.",
                    keeper, null, ch, TO_VICT);
            ch.reply = keeper;
            return;
        }

        if (!can_see_obj(keeper, obj)) {
            act("$n doesn't see what you are offering.", keeper, null, ch, TO_VICT);
            return;
        }

        if (!can_drop_obj(ch, obj)) {
            send_to_char("You can't let go of it.\n", ch);
            return;
        }

        if ((cost = get_cost(keeper, obj, false)) <= 0) {
            act("$n looks uninterested in $p.", keeper, obj, ch, TO_VICT);
            return;
        }

        TextBuffer buf = new TextBuffer();
        buf.sprintf("$n tells you 'I'll give you %d silver and %d gold coins for $p'.",
                cost - (cost / 100) * 100, cost / 100);
        act(buf.toString(), keeper, obj, ch, TO_VICT);
        ch.reply = keeper;

    }

    static void do_wanted(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;

        if (skill_failure_check(ch, gsn_wanted, false, 0, null)) {
            return;
        }

        StringBuilder arg1 = new StringBuilder();
        StringBuilder arg2 = new StringBuilder();
        argument = one_argument(argument, arg1);
        one_argument(argument, arg2);

        if (arg1.isEmpty() || arg2.isEmpty()) {
            send_to_char("Usage: wanted <player> <Y|N>\n", ch);
            return;
        }

        victim = get_char_world(ch, arg1.toString());

        if ((victim == null) ||
                !(can_see(ch, victim))) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (victim.level >= LEVEL_IMMORTAL && (ch.level < victim.level)) {
            act("You do not have the power to arrest $N.", ch, null, victim,
                    TO_CHAR);
            return;
        }

        if (victim == ch) {
            send_to_char("You cannot do that to yourself.\n", ch);
            return;
        }

        switch (arg2.charAt(0)) {
            case 'Y', 'y' -> {
                if (IS_SET(victim.act, PLR_WANTED)) {
                    act("$n is already wanted.", ch, null, null, TO_CHAR);
                } else {
                    victim.act = SET_BIT(victim.act, PLR_WANTED);
                    act("$n is now WANTED!!!", victim, null, ch, TO_NOTVICT);
                    send_to_char("You are now WANTED!!!\n", victim);
                    send_to_char("Ok.\n", ch);
                }
            }
            case 'N', 'n' -> {
                if (!IS_SET(victim.act, PLR_WANTED)) {
                    act("$N is not wanted.", ch, null, victim, TO_CHAR);
                } else {
                    victim.act = REMOVE_BIT(victim.act, PLR_WANTED);
                    act("$n is no longer wanted.", victim, null, ch, TO_NOTVICT);
                    send_to_char("You are no longer wanted.\n", victim);
                    send_to_char("Ok.\n", ch);
                }
            }
            default -> send_to_char("Usage: wanted <player> <Y|N>\n", ch);
        }
    }

    static void do_herbs(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;

        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (skill_failure_check(ch, gsn_herbs, false, 0, null)) {
            return;
        }

        if (is_affected(ch, gsn_herbs)) {
            send_to_char("You can't find any more herbs.\n", ch);
            return;
        }

        if (arg.isEmpty()) {
            victim = ch;
        } else if ((victim = get_char_room(ch, arg.toString())) == null) {
            send_to_char("They're not here.\n", ch);
            return;
        }

        WAIT_STATE(ch, gsn_herbs.beats);

        if (ch.in_room.sector_type != SECT_INSIDE &&
                ch.in_room.sector_type != SECT_CITY &&
                (IS_NPC(ch) || number_percent() < get_skill(ch, gsn_herbs))) {
            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_herbs;
            af.level = ch.level;
            af.duration = 5;
            af.location = APPLY_NONE;
            af.modifier = 0;
            af.bitvector = 0;

            affect_to_char(ch, af);

            send_to_char("You gather some beneficial herbs.\n", ch);
            act("$n gathers some herbs.", ch, null, null, TO_ROOM);

            if (ch != victim) {
                act("$n gives you some herbs to eat.", ch, null, victim, TO_VICT);
                act("You give the herbs to $N.", ch, null, victim, TO_CHAR);
                act("$n gives the herbs to $N.", ch, null, victim, TO_NOTVICT);
            }

            if (victim.hit < victim.max_hit) {
                send_to_char("You feel better.\n", victim);
                act("$n looks better.", victim, null, null, TO_ROOM);
            }
            victim.hit = UMIN(victim.max_hit, victim.hit + 5 * ch.level);
            check_improve(ch, gsn_herbs, true, 1);
            if (is_affected(victim, gsn_plague)) {
                if (check_dispel(ch.level, victim, gsn_plague)) {
                    send_to_char("Your sores vanish.\n", victim);
                    act("$n looks relieved as $s sores vanish.", victim, null, null, TO_ROOM);
                }
            }
        } else {
            send_to_char("You search for herbs but find none here.\n", ch);
            act("$n looks around for herbs.", ch, null, null, TO_ROOM);
            check_improve(ch, gsn_herbs, false, 1);
        }
    }

    private static boolean orig_lore = true;

    static void do_lore(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        int chance;
        int value0, value1, value2, value3;

        StringBuilder arg1 = new StringBuilder();
        one_argument(argument, arg1);

        if (skill_failure_check(ch, gsn_lore, true, 0, "The meaning of this object escapes you for the moment.\n")) {
            return;
        }

        if ((obj = get_obj_carry(ch, arg1.toString())) == null) {
            send_to_char("You do not have that object.\n", ch);
            return;
        }

        if (ch.mana < 30) {
            send_to_char("You don't have enough mana.\n", ch);
            return;
        }

        /* a random lore */
        chance = number_percent();

        TextBuffer buf = new TextBuffer();
        if (get_skill(ch, gsn_lore) < 20) {
            buf.sprintf("Object '%s'.\n", obj.name);
            send_to_char(buf, ch);
            ch.mana -= 30;
            check_improve(ch, gsn_lore, true, 8);
            return;
        } else if (get_skill(ch, gsn_lore) < 40) {
            buf.sprintf(
                    "Object '%s'.  Weight is %d, value is %d.\n",
                    obj.name,
                    chance < 60 ? obj.weight : number_range(1, 2 * obj.weight),
                    chance < 60 ? number_range(1, 2 * obj.cost) : obj.cost
            );
            send_to_char(buf, ch);
            if (str_cmp(obj.material, "oldstyle")) {
                buf.sprintf("Material is %s.\n", obj.material);
                send_to_char(buf, ch);
            }
            ch.mana -= 30;
            check_improve(ch, gsn_lore, true, 7);
            return;
        } else if (get_skill(ch, gsn_lore) < 60) {
            buf.sprintf(
                    "Object '%s' has weight %d.\nValue is %d, level is %d.\nMaterial is %s.\n",
                    obj.name,
                    obj.weight,
                    chance < 60 ? number_range(1, 2 * obj.cost) : obj.cost,
                    chance < 60 ? obj.level : number_range(1, 2 * obj.level),
                    str_cmp(obj.material, "oldstyle") ? obj.material : "unknown"
            );
            send_to_char(buf, ch);
            ch.mana -= 30;
            check_improve(ch, gsn_lore, true, 6);
            return;
        } else if (get_skill(ch, gsn_lore) < 80) {
            buf.sprintf(
                    "Object '%s' is type %s, extra flags %s.\nWeight is %d, value is %d, level is %d.\nMaterial is %s.\n",
                    obj.name,
                    item_type_name(obj),
                    extra_bit_name(obj.extra_flags),
                    obj.weight,
                    chance < 60 ? number_range(1, 2 * obj.cost) : obj.cost,
                    chance < 60 ? obj.level : number_range(1, 2 * obj.level),
                    str_cmp(obj.material, "oldstyle") ? obj.material : "unknown"
            );
            send_to_char(buf, ch);
            ch.mana -= 30;
            check_improve(ch, gsn_lore, true, 5);
            return;
        } else if (get_skill(ch, gsn_lore) < 85) {
            buf.sprintf(
                    "Object '%s' is type %s, extra flags %s.\nWeight is %d, value is %d, level is %d.\nMaterial is %s.\n",
                    obj.name,
                    item_type_name(obj),
                    extra_bit_name(obj.extra_flags),
                    obj.weight,
                    obj.cost,
                    obj.level,
                    str_cmp(obj.material, "oldstyle") ? obj.material : "unknown"
            );
            send_to_char(buf, ch);
        } else {
            buf.sprintf(
                    "Object '%s' is type %s, extra flags %s.\nWeight is %d, value is %d, level is %d.\nMaterial is %s.\n",
                    obj.name,
                    item_type_name(obj),
                    extra_bit_name(obj.extra_flags),
                    obj.weight,
                    obj.cost,
                    obj.level,
                    str_cmp(obj.material, "oldstyle") ? obj.material : "unknown"
            );
            send_to_char(buf, ch);
        }

        ch.mana -= 30;

        value0 = obj.value[0];
        value1 = obj.value[1];
        value2 = obj.value[2];
        value3 = obj.value[3];

        switch (obj.item_type) {
            case ITEM_SCROLL, ITEM_POTION, ITEM_PILL -> {
                if (get_skill(ch, gsn_lore) < 85) {
                    value0 = number_range(1, 60);
                    if (chance > 40) {
                        value1 = number_range(1, (MAX_SKILL - 1));
                        if (chance > 60) {
                            value2 = number_range(1, (MAX_SKILL - 1));
                            if (chance > 80) {
                                value3 = number_range(1, (MAX_SKILL - 1));
                            }
                        }
                    }
                } else {
                    if (chance > 60) {
                        value1 = number_range(1, (MAX_SKILL - 1));
                        if (chance > 80) {
                            value2 = number_range(1, (MAX_SKILL - 1));
                            if (chance > 95) {
                                value3 = number_range(1, (MAX_SKILL - 1));
                            }
                        }
                    }
                }
                buf.sprintf("Level %d spells of:", value0);
                send_to_char(buf, ch);
                if (value1 >= 0 && value1 < MAX_SKILL) {
                    send_to_char(" '", ch);
                    send_to_char(Skill.skills[value1].name, ch);
                    send_to_char("'", ch);
                }
                if (value2 >= 0 && value2 < MAX_SKILL) {
                    send_to_char(" '", ch);
                    send_to_char(Skill.skills[value2].name, ch);
                    send_to_char("'", ch);
                }
                if (value3 >= 0 && value3 < MAX_SKILL) {
                    send_to_char(" '", ch);
                    send_to_char(Skill.skills[value3].name, ch);
                    send_to_char("'", ch);
                }
                send_to_char(".\n", ch);
            }
            case ITEM_WAND, ITEM_STAFF -> {
                if (get_skill(ch, gsn_lore) < 85) {
                    value0 = number_range(1, 60);
                    if (chance > 40) {
                        value3 = number_range(1, (MAX_SKILL - 1));
                        if (chance > 60) {
                            value2 = number_range(0, 2 * obj.value[2]);
                            if (chance > 80) {
                                value1 = number_range(0, value2);
                            }
                        }
                    }
                } else {
                    if (chance > 60) {
                        value3 = number_range(1, (MAX_SKILL - 1));
                        if (chance > 80) {
                            value2 = number_range(0, 2 * obj.value[2]);
                            if (chance > 95) {
                                value1 = number_range(0, value2);
                            }
                        }
                    }
                }
                buf.sprintf("Has %d(%d) charges of level %d", value1, value2, value0);
                send_to_char(buf, ch);
                if (value3 >= 0 && value3 < MAX_SKILL) {
                    send_to_char(" '", ch);
                    send_to_char(Skill.skills[value3].name, ch);
                    send_to_char("'", ch);
                }
                send_to_char(".\n", ch);
            }
            case ITEM_WEAPON -> {
                send_to_char("Weapon type is ", ch);
                if (get_skill(ch, gsn_lore) < 85) {
                    value0 = number_range(0, 8);
                    if (chance > 33) {
                        value1 = number_range(1, 2 * obj.value[1]);
                        if (chance > 66) {
                            value2 = number_range(1, 2 * obj.value[2]);
                        }
                    }
                } else {
                    if (chance > 50) {
                        value1 = number_range(1, 2 * obj.value[1]);
                        if (chance > 75) {
                            value2 = number_range(1, 2 * obj.value[2]);
                        }
                    }
                }
                switch (value0) {
                    case (WEAPON_EXOTIC):
                        send_to_char("exotic.\n", ch);
                        break;
                    case (WEAPON_SWORD):
                        send_to_char("sword.\n", ch);
                        break;
                    case (WEAPON_DAGGER):
                        send_to_char("dagger.\n", ch);
                        break;
                    case (WEAPON_SPEAR):
                        send_to_char("spear/staff.\n", ch);
                        break;
                    case (WEAPON_MACE):
                        send_to_char("mace/club.\n", ch);
                        break;
                    case (WEAPON_AXE):
                        send_to_char("axe.\n", ch);
                        break;
                    case (WEAPON_FLAIL):
                        send_to_char("flail.\n", ch);
                        break;
                    case (WEAPON_WHIP):
                        send_to_char("whip.\n", ch);
                        break;
                    case (WEAPON_POLEARM):
                        send_to_char("polearm.\n", ch);
                        break;
                    case (WEAPON_BOW):
                        send_to_char("bow.\n", ch);
                        break;
                    case (WEAPON_ARROW):
                        send_to_char("arrow.\n", ch);
                        break;
                    case (WEAPON_LANCE):
                        send_to_char("lance.\n", ch);
                        break;
                    default:
                        send_to_char("unknown.\n", ch);
                        break;
                }
                if (obj.pIndexData.new_format) {
                    buf.sprintf("Damage is %dd%d (average %d).\n",
                            value1, value2,
                            (1 + value2) * value1 / 2);
                } else {
                    buf.sprintf("Damage is %d to %d (average %d).\n",
                            value1, value2,
                            (value1 + value2) / 2);
                }
                send_to_char(buf, ch);
            }
            case ITEM_ARMOR -> {
                if (get_skill(ch, gsn_lore) < 85) {
                    if (chance > 25) {
                        value2 = number_range(0, 2 * obj.value[2]);
                        if (chance > 45) {
                            value0 = number_range(0, 2 * obj.value[0]);
                            if (chance > 65) {
                                value3 = number_range(0, 2 * obj.value[3]);
                                if (chance > 85) {
                                    value1 = number_range(0, 2 * obj.value[1]);
                                }
                            }
                        }
                    }
                } else {
                    if (chance > 45) {
                        value2 = number_range(0, 2 * obj.value[2]);
                        if (chance > 65) {
                            value0 = number_range(0, 2 * obj.value[0]);
                            if (chance > 85) {
                                value3 = number_range(0, 2 * obj.value[3]);
                                if (chance > 95) {
                                    value1 = number_range(0, 2 * obj.value[1]);
                                }
                            }
                        }
                    }
                }
                buf.sprintf("Armor class is %d pierce, %d bash, %d slash, and %d vs. magic.\n",
                        value0, value1, value2, value3);
                send_to_char(buf, ch);
            }
        }

        if (get_skill(ch, gsn_lore) < 87) {
            check_improve(ch, gsn_lore, true, 5);
        }
        if (orig_lore) {
            return;
        }

        if (!obj.enchanted) {
            for (AFFECT_DATA paf = obj.pIndexData.affected; paf != null; paf = paf.next) {
                if (paf.location != APPLY_NONE && paf.modifier != 0) {
                    buf.sprintf("Affects %s by %d.\n", affect_loc_name(paf.location), paf.modifier);
                    send_to_char(buf, ch);
                }
            }
        }

        for (AFFECT_DATA paf = obj.affected; paf != null; paf = paf.next) {
            if (paf.location != APPLY_NONE && paf.modifier != 0) {
                buf.sprintf("Affects %s by %d.\n", affect_loc_name(paf.location), paf.modifier);
                send_to_char(buf, ch);
            }
        }
        check_improve(ch, gsn_lore, true, 5);
    }


    static void do_butcher(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        OBJ_DATA tmp_obj;
        OBJ_DATA tmp_next;

        if (skill_failure_check(ch, gsn_butcher, true, 0, "You don't have the precision instruments for that.\n")) {
            return;
        }

        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.isEmpty()) {
            send_to_char("Butcher what?\n", ch);
            return;
        }
        if ((obj = get_obj_here(ch, arg.toString())) == null) {
            send_to_char("You do not see that here.\n", ch);
            return;
        }

        if (obj.item_type != ITEM_CORPSE_PC && obj.item_type != ITEM_CORPSE_NPC) {
            send_to_char("You can't butcher that.\n", ch);
            return;
        }

        if (obj.carried_by != null) {
            send_to_char("Put it down first.\n", ch);
            return;
        }

        obj_from_room(obj);

        for (tmp_obj = obj.contains; tmp_obj != null;
             tmp_obj = tmp_next) {
            tmp_next = tmp_obj.next_content;
            obj_from_obj(tmp_obj);
            obj_to_room(tmp_obj, ch.in_room);
        }


        if (IS_NPC(ch) || number_percent() < get_skill(ch, gsn_butcher)) {
            int numsteaks;
            int i;
            OBJ_DATA steak;

            numsteaks = number_bits(2) + 1;
            TextBuffer buf = new TextBuffer();
            if (numsteaks > 1) {
                buf.sprintf("$n butchers $p and creates %i steaks.", numsteaks);
                act(buf, ch, obj, null, TO_ROOM);

                buf.sprintf("You butcher $p and create %i steaks.", numsteaks);
                act(buf, ch, obj, null, TO_CHAR);
            } else {
                act("$n butchers $p and creates a steak."
                        , ch, obj, null, TO_ROOM);

                act("You butcher $p and create a steak."
                        , ch, obj, null, TO_CHAR);
            }
            check_improve(ch, gsn_butcher, true, 1);

            for (i = 0; i < numsteaks; i++) {
                steak = create_object(get_obj_index(OBJ_VNUM_STEAK), 0);
                buf.sprintf(steak.short_descr, obj.short_descr);
                steak.short_descr = buf.toString();

                buf.sprintf(steak.description, obj.short_descr);
                steak.description = buf.toString();

                obj_to_room(steak, ch.in_room);
            }
        } else {
            act("You fail and destroy $p.", ch, obj, null, TO_CHAR);
            act("$n fails to butcher $p and destroys it.",
                    ch, obj, null, TO_ROOM);

            check_improve(ch, gsn_butcher, false, 1);
        }
        extract_obj(obj);
    }


    static void do_balance(CHAR_DATA ch) {
        long bank_g;
        long bank_s;

        if (IS_NPC(ch)) {
            send_to_char("You don't have a bank account.\n", ch);
            return;
        }

        if (!IS_SET(ch.in_room.room_flags, ROOM_BANK)) {
            send_to_char("You are not in a bank.\n", ch);
            return;
        }


        if (ch.pcdata.bank_s + ch.pcdata.bank_g == 0) {
            send_to_char("You don't have any money in the bank.\n", ch);
            return;
        }

        bank_g = ch.pcdata.bank_g;
        bank_s = ch.pcdata.bank_s;
        TextBuffer buf = new TextBuffer();
        TextBuffer buf2 = new TextBuffer();
        buf.sprintf("You have %s%s%s coin%s in the bank.\n",
                bank_g != 0 ? "%d gold" : "",
                (bank_g != 0) && (bank_s != 0) ? " and " : "",
                bank_s != 0 ? "%d silver" : "",
                bank_s + bank_g > 1 ? "s" : "");

        if (bank_g == 0) {
            buf2.sprintf(buf.toString(), bank_s);
        } else {
            buf2.sprintf(buf.toString(), bank_g, bank_s);
        }

        send_to_char(buf2, ch);
    }

    static void do_withdraw(CHAR_DATA ch, String argument) {
        int amount_s;
        int amount_g;
        int weight;

        if (IS_NPC(ch)) {
            send_to_char("You don't have a bank account.\n", ch);
            return;
        }

        if (!IS_SET(ch.in_room.room_flags, ROOM_BANK)) {
            send_to_char("The mosquito by your feet will not give you any money.\n", ch);
            return;
        }

        StringBuilder arg = new StringBuilder();
        argument = one_argument(argument, arg);
        if (arg.isEmpty()) {
            send_to_char("Withdraw how much?\n", ch);
            return;
        }

        amount_s = Math.abs(atoi(arg.toString()));
        if (!str_cmp(argument, "silver") || argument.isEmpty()) {
            amount_g = 0;
        } else if (!str_cmp(argument, "gold")) {
            amount_g = amount_s;
            amount_s = 0;
        } else {
            send_to_char("You can withdraw gold and silver coins only.", ch);
            return;
        }

        if (amount_g > ch.pcdata.bank_g) {
            send_to_char("Sorry, we don't give loans.\n", ch);
            return;
        }

        if (amount_s > ch.pcdata.bank_s) {
            send_to_char("Sorry, we don't give loans.\n", ch);
            return;
        }

        weight = amount_g * 2 / 5;
        weight += amount_s / 10;

        if (get_carry_weight(ch) + weight > can_carry_w(ch)) {
            act("You can't carry that much weight.", ch, null, null, TO_CHAR);
            return;
        }

        ch.pcdata.bank_g -= amount_g;
        ch.pcdata.bank_s -= amount_s;
        ch.gold += 0.98 * amount_g;
        ch.silver += 0.90 * amount_s;
        TextBuffer buf = new TextBuffer();
        if (amount_s > 0 && amount_s < 10) {
            if (amount_s == 1) {
                buf.sprintf("One coin??? You cheapskate!\n");
            } else {
                buf.sprintf("%d coins??? You cheapskate!\n", amount_s);
            }
        } else {
            buf.sprintf(
                    "Here are your %d %s coins, minus a %d coin withdrawal fee.\n",
                    amount_s != 0 ? amount_s : amount_g,
                    amount_s != 0 ? "silver" : "gold",
                    amount_s != 0 ? (long) UMAX(1, (int) (0.10 * amount_s)) :
                            (long) UMAX(1, (int) (0.02 * amount_g)));
        }
        send_to_char(buf, ch);
        act("$n steps up to the teller window.", ch, null, null, TO_ROOM);
    }

    static void do_deposit(CHAR_DATA ch, String argument) {
        long amount_s;
        long amount_g;

        if (IS_NPC(ch)) {
            send_to_char("You don't have a bank account.\n", ch);
            return;
        }

        if (!IS_SET(ch.in_room.room_flags, ROOM_BANK)) {
            send_to_char("The ant by your feet can't carry your gold.\n", ch);
            return;
        }

        StringBuilder arg = new StringBuilder();
        argument = one_argument(argument, arg);
        if (arg.isEmpty()) {
            send_to_char("Deposit how much?\n", ch);
            return;
        }
        amount_s = Math.abs(atoi(arg.toString()));
        if (!str_cmp(argument, "silver") || argument.isEmpty()) {
            amount_g = 0;
        } else if (!str_cmp(argument, "gold")) {
            amount_g = amount_s;
            amount_s = 0;
        } else {
            send_to_char("You can deposit gold and silver coins only.", ch);
            return;
        }

        if (amount_g > ch.gold) {
            send_to_char("That's more than you've got.\n", ch);
            return;
        }
        if (amount_s > ch.silver) {
            send_to_char("That's more than you've got.\n", ch);
            return;
        }

        if ((amount_g + ch.pcdata.bank_g) > 400000) {
            send_to_char("Bank cannot accept more than 400,000 gold.\n", ch);
            return;
        }

        ch.pcdata.bank_s += amount_s;
        ch.pcdata.bank_g += amount_g;
        ch.gold -= amount_g;
        ch.silver -= amount_s;
        TextBuffer buf = new TextBuffer();
        if (amount_s == 1) {
            buf.sprintf("Oh boy! One gold coin!\n");
        } else {
            buf.sprintf("%d %s coins deposited. Come again soon!\n",
                    amount_s != 0 ? amount_s : amount_g,
                    amount_s != 0 ? "silver" : "gold");
        }

        send_to_char(buf, ch);
        act("$n steps up to the teller window.", ch, null, null, TO_ROOM);
    }


    static void do_enchant(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        int wear_level;

        if (skill_failure_check(ch, gsn_enchant_sword, false, 0, null)) {
            return;
        }

        if (argument.isEmpty()) /* empty */ {
            send_to_char("Wear which weapon to enchant?\n", ch);
            return;
        }

        obj = get_obj_carry(ch, argument);

        if (obj == null) {
            send_to_char("You don't have that item.\n", ch);
            return;
        }


        wear_level = ch.level;

        if ((ch.clazz.fMana && obj.item_type == ITEM_ARMOR)
                || (!ch.clazz.fMana && obj.item_type == ITEM_WEAPON)) {
            wear_level += 3;
        }

        if (wear_level < obj.level) {
            TextBuffer buf = new TextBuffer();
            buf.sprintf("You must be level %d to be able to enchant this object.\n",
                    obj.level);
            send_to_char(buf, ch);
            act("$n tries to enchant $p, but is too inexperienced.",
                    ch, obj, null, TO_ROOM);
            return;
        }

        if (ch.mana < 100) {
            send_to_char("You don't have enough mana.\n", ch);
            return;
        }

        if (number_percent() > get_skill(ch, gsn_enchant_sword)) {
            send_to_char("You lost your concentration.\n", ch);
            act("$n tries to enchant $p, but he forgets how for a moment.",
                    ch, obj, null, TO_ROOM);
            WAIT_STATE(ch, gsn_enchant_sword.beats);
            check_improve(ch, gsn_enchant_sword, false, 6);
            ch.mana -= 50;
            return;
        }
        ch.mana -= 100;
        spell_enchant_weapon(gsn_enchant_weapon, ch.level, ch, obj);
        check_improve(ch, gsn_enchant_sword, true, 2);
        WAIT_STATE(ch, gsn_enchant_sword.beats);
    }


    static void hold_a_light(CHAR_DATA ch, OBJ_DATA obj, int iWear) {
        act("$n lights $p and holds it.", ch, obj, null, TO_ROOM);
        act("You light $p and hold it.", ch, obj, null, TO_CHAR);
        equip_char(ch, obj, iWear);
    }

    static void hold_a_shield(CHAR_DATA ch, OBJ_DATA obj, int iWear) {
        act("$n wears $p as a shield.", ch, obj, null, TO_ROOM);
        act("You wear $p as a shield.", ch, obj, null, TO_CHAR);
        equip_char(ch, obj, iWear);
    }

    static void hold_a_thing(CHAR_DATA ch, OBJ_DATA obj, int iWear) {
        act("$n holds $p in $s hand.", ch, obj, null, TO_ROOM);
        act("You hold $p in your hand.", ch, obj, null, TO_CHAR);
        equip_char(ch, obj, iWear);
    }

/* wear object as a secondary weapon */

    static void hold_a_wield(CHAR_DATA ch, OBJ_DATA obj, int iWear) {
        Skill sn;
        int skill;

        if (obj == null) {
            bug("Hold_a_wield: Obj null");
            return;
        }

        if (obj.item_type != ITEM_WEAPON) {
            hold_a_thing(ch, obj, iWear);
            return;
        }

        act("$n wields $p.", ch, obj, null, TO_ROOM);
        act("You wield $p.", ch, obj, null, TO_CHAR);
        equip_char(ch, obj, iWear);
        if (get_wield_char(ch, true) == obj) {
            sn = get_weapon_sn(ch, true);
        } else {
            sn = get_weapon_sn(ch, false);
        }

        if (sn != null) {
            skill = get_weapon_skill(ch, sn);

            if (skill >= 100) {
                act("$p feels like a part of you!", ch, obj, null, TO_CHAR);
            } else if (skill > 85) {
                act("You feel quite confident with $p.", ch, obj, null, TO_CHAR);
            } else if (skill > 70) {
                act("You are skilled with $p.", ch, obj, null, TO_CHAR);
            } else if (skill > 50) {
                act("Your skill with $p is adequate.", ch, obj, null, TO_CHAR);
            } else if (skill > 25) {
                act("$p feels a little clumsy in your hands.", ch, obj, null, TO_CHAR);
            } else if (skill > 1) {
                act("You fumble and almost drop $p.", ch, obj, null, TO_CHAR);
            } else {
                act("You don't even know which end is up on $p.",
                        ch, obj, null, TO_CHAR);
            }
        }

    }


    static void wear_a_wield(CHAR_DATA ch, OBJ_DATA obj, boolean fReplace) {
        if (!IS_NPC(ch)
                && get_obj_weight(obj) > str_app[get_curr_stat(ch, STAT_STR)].carry) {
            send_to_char("It is too heavy for you to wield.\n", ch);
            return;
        }

        if (IS_WEAPON_STAT(obj, WEAPON_TWO_HANDS) &&
                (!IS_NPC(ch) && ch.size < SIZE_LARGE)) {
            if (get_eq_char(ch, WEAR_BOTH) != null) {
                if (!remove_obj_loc(ch, WEAR_BOTH, fReplace)) {
                    return;
                }
                hold_a_wield(ch, obj, WEAR_BOTH);
            } else {
                if (get_eq_char(ch, WEAR_RIGHT) != null) {
                    if (!remove_obj_loc(ch, WEAR_RIGHT, fReplace)) {
                        return;
                    }
                }
                if (get_eq_char(ch, WEAR_LEFT) != null) {
                    if (!remove_obj_loc(ch, WEAR_LEFT, fReplace)) {
                        return;
                    }
                }
                hold_a_wield(ch, obj, WEAR_BOTH);
            }
        } else {
            if (get_eq_char(ch, WEAR_BOTH) != null) {
                if (!remove_obj_loc(ch, WEAR_BOTH, fReplace)) {
                    return;
                }
                hold_a_wield(ch, obj, WEAR_RIGHT);
            } else if (get_eq_char(ch, WEAR_RIGHT) == null) {
                hold_a_wield(ch, obj, WEAR_RIGHT);
            } else if (get_eq_char(ch, WEAR_LEFT) == null) {
                hold_a_wield(ch, obj, WEAR_LEFT);
            } else if (remove_obj_loc(ch, WEAR_RIGHT, fReplace)) {
                hold_a_wield(ch, obj, WEAR_RIGHT);
            } else if (remove_obj_loc(ch, WEAR_LEFT, fReplace)) {
                hold_a_wield(ch, obj, WEAR_LEFT);
            } else {
                send_to_char("You found your hands full.\n", ch);
            }
        }
    }


    static void wear_multi(CHAR_DATA ch, OBJ_DATA obj, int iWear, boolean fReplace) {
        if (count_worn(ch, iWear) < max_can_wear(ch, iWear)) {
            switch (iWear) {
                case WEAR_FINGER -> {
                    act("$n wears $p on one of $s finger.", ch, obj, null, TO_ROOM);
                    act("You wear $p on one of your finger.", ch, obj, null, TO_CHAR);
                }
                case WEAR_NECK -> {
                    act("$n wears $p around $s neck.", ch, obj, null, TO_ROOM);
                    act("You wear $p around your neck.", ch, obj, null, TO_CHAR);
                }
                case WEAR_WRIST -> {
                    act("$n wears $p around one of $s wrist.", ch, obj, null, TO_ROOM);
                    act("You wear $p around one of your wrist.", ch, obj, null, TO_CHAR);
                }
                case WEAR_TATTOO -> {
                    act("$n now uses $p as tattoo of $s religion.", ch, obj, null, TO_ROOM);
                    act("You now use $p as the tattoo of your religion.", ch, obj, null, TO_CHAR);
                }
                default -> {
                    act("$n wears $p around somewhere.", ch, obj, null, TO_ROOM);
                    act("You wear $p around somewhere.", ch, obj, null, TO_CHAR);
                }
            }
            equip_char(ch, obj, iWear);
        } else if (fReplace) {
            OBJ_DATA w;
            boolean not_worn = true;

            for (w = ch.carrying; w != null; w = w.next_content) {
                if (w.wear_loc == iWear
                        && !IS_SET(w.extra_flags, ITEM_NOREMOVE)
                        && (w.item_type != ITEM_TATTOO || IS_IMMORTAL(ch))) {
                    unequip_char(ch, w);
                    act("$n stops using $p.", ch, w, null, TO_ROOM);
                    act("You stop using $p.", ch, w, null, TO_CHAR);
                    wear_multi(ch, obj, iWear, true);
                    not_worn = false;
                    break;
                }
            }

            if (not_worn) {
                act("You couldn't remove anything to replace with $p.",
                        ch, obj, null, TO_CHAR);
            }
        }

    }
}
