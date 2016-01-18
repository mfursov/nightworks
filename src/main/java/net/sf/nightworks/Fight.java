package net.sf.nightworks;

/*
 * ************************************************************************ *
 *   Nightworks MUD is copyright 2006 Mikhail Fursov                        *
 *       Mikhail Fursov {fmike@mail.ru}                                     *
 * ************************************************************************ *

 * ************************************************************************ *
 *   ANATOLIA MUD is copyright 1996-2002 Serdar BULUT, Ibrahim CANPUNAR     *
 *   ANATOLIA has been brought to you by ANATOLIA consortium		        *
 *	 Serdar BULUT {Chronos}		bulut@anatoliamud.org                       *
 *	 Ibrahim Canpunar  {Asena}	canpunar@anatoliamud.org                    *
 *	 Murat BICER  {KIO}		mbicer@anatoliamud.org       	                *
 *	 D.Baris ACAR {Powerman}	dbacar@anatoliamud.org       	            *
 *   By using this code, you have agreed to follow the terms of the         *
 *   ANATOLIA license, in the file Anatolia/doc/License/license.anatolia    *
 * ************************************************************************ *

 * ************************************************************************ *
 *	ROM 2.4 is copyright 1993-1995 Russ Taylor			                    *
 *	ROM has been brought to you by the ROM consortium	             	    *
 *	    Russ Taylor (rtaylor@pacinfo.com)				                    *
 *	    Gabrielle Taylor (gtaylor@pacinfo.com)			                    *
 *	    Brian Moore (rom@rom.efn.org)				                        *
 *	By using this code, you have agreed to follow the terms of the  	    *
 *	ROM license, in the file Rom24/doc/rom.license			                *
 * ************************************************************************ *

 * *********************************************************************** *
 *  Original Diku Mud copyright (C) 1990, 1991 by Sebastian Hammer,        *
 *  Michael Seifert, Hans Henrik St{rfeldt, Tom Madsen, and Katja Nyboe.   *
 *                                                                         *
 *  Merc Diku Mud improvments copyright (C) 1992, 1993 by Michael          *
 *  Chastain, Michael Quan, and Mitchell Tse.                              *
 *                                                                         *
 *  In order to use any part of this Merc Diku Mud, you must comply with   *
 *  both the original Diku license in 'license.doc' as well the Merc       *
 *  license in 'license.txt'.  In particular, you may not remove either of *
 *  these copyright notices.                                               *
 *                                                                         *
 *  Much time and thought has gone into this software and you are          *
 *  benefitting.  We hope that you share your changes too.  What goes      *
 *  around, comes around.                                                  *
 * *********************************************************************** *
 */

import net.sf.nightworks.util.TextBuffer;

import java.io.File;

import static net.sf.nightworks.ActComm.do_emote;
import static net.sf.nightworks.ActComm.do_quit_count;
import static net.sf.nightworks.ActComm.do_yell;
import static net.sf.nightworks.ActComm.is_same_group;
import static net.sf.nightworks.ActComm.stop_follower;
import static net.sf.nightworks.ActHera.check_shield_destroyed;
import static net.sf.nightworks.ActHera.check_weapon_destroy;
import static net.sf.nightworks.ActHera.check_weapon_destroyed;
import static net.sf.nightworks.ActMove.do_dismount;
import static net.sf.nightworks.ActMove.do_recall;
import static net.sf.nightworks.ActMove.do_visible;
import static net.sf.nightworks.ActMove.move_char;
import static net.sf.nightworks.ActObj.do_get;
import static net.sf.nightworks.ActObj.do_sacrifice;
import static net.sf.nightworks.ActSkill.check_improve;
import static net.sf.nightworks.ActSkill.exp_per_level;
import static net.sf.nightworks.ActWiz.wiznet;
import static net.sf.nightworks.Comm.act;
import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.Const.attack_table;
import static net.sf.nightworks.Const.hometown_table;
import static net.sf.nightworks.DB.bug;
import static net.sf.nightworks.DB.create_object;
import static net.sf.nightworks.DB.dice;
import static net.sf.nightworks.DB.get_obj_index;
import static net.sf.nightworks.DB.interpolate;
import static net.sf.nightworks.DB.kill_table;
import static net.sf.nightworks.DB.number_bits;
import static net.sf.nightworks.DB.number_door;
import static net.sf.nightworks.DB.number_percent;
import static net.sf.nightworks.DB.number_range;
import static net.sf.nightworks.DB.tail_chain;
import static net.sf.nightworks.Effects.cold_effect;
import static net.sf.nightworks.Effects.fire_effect;
import static net.sf.nightworks.Effects.shock_effect;
import static net.sf.nightworks.Handler.affect_find;
import static net.sf.nightworks.Handler.affect_join;
import static net.sf.nightworks.Handler.affect_remove;
import static net.sf.nightworks.Handler.affect_strip;
import static net.sf.nightworks.Handler.can_see;
import static net.sf.nightworks.Handler.check_immune;
import static net.sf.nightworks.Handler.create_money;
import static net.sf.nightworks.Handler.equip_char;
import static net.sf.nightworks.Handler.extract_char;
import static net.sf.nightworks.Handler.extract_obj;
import static net.sf.nightworks.Handler.get_char_room;
import static net.sf.nightworks.Handler.get_curr_stat;
import static net.sf.nightworks.Handler.get_eq_char;
import static net.sf.nightworks.Handler.get_max_train;
import static net.sf.nightworks.Handler.get_obj_list;
import static net.sf.nightworks.Handler.get_shield_char;
import static net.sf.nightworks.Handler.get_skill;
import static net.sf.nightworks.Handler.get_trust;
import static net.sf.nightworks.Handler.get_weapon_skill;
import static net.sf.nightworks.Handler.get_weapon_sn;
import static net.sf.nightworks.Handler.get_wield_char;
import static net.sf.nightworks.Handler.is_affected;
import static net.sf.nightworks.Handler.obj_from_char;
import static net.sf.nightworks.Handler.obj_to_char;
import static net.sf.nightworks.Handler.obj_to_obj;
import static net.sf.nightworks.Handler.obj_to_room;
import static net.sf.nightworks.Handler.remove_mind;
import static net.sf.nightworks.Handler.skill_failure_nomessage;
import static net.sf.nightworks.Magic.saves_spell;
import static net.sf.nightworks.Magic.say_spell;
import static net.sf.nightworks.MartialArt.critical_strike;
import static net.sf.nightworks.MartialArt.do_bash;
import static net.sf.nightworks.MartialArt.do_berserk;
import static net.sf.nightworks.MartialArt.do_crush;
import static net.sf.nightworks.MartialArt.do_dirt;
import static net.sf.nightworks.MartialArt.do_disarm;
import static net.sf.nightworks.MartialArt.do_kick;
import static net.sf.nightworks.MartialArt.do_tail;
import static net.sf.nightworks.MartialArt.do_trip;
import static net.sf.nightworks.MartialArt.ground_strike;
import static net.sf.nightworks.Nightworks.ACT_CLERIC;
import static net.sf.nightworks.Nightworks.ACT_HUNTER;
import static net.sf.nightworks.Nightworks.ACT_MAGE;
import static net.sf.nightworks.Nightworks.ACT_NOALIGN;
import static net.sf.nightworks.Nightworks.ACT_NOTRACK;
import static net.sf.nightworks.Nightworks.ACT_PET;
import static net.sf.nightworks.Nightworks.ACT_THIEF;
import static net.sf.nightworks.Nightworks.ACT_WARRIOR;
import static net.sf.nightworks.Nightworks.ACT_WIMPY;
import static net.sf.nightworks.Nightworks.AC_BASH;
import static net.sf.nightworks.Nightworks.AC_EXOTIC;
import static net.sf.nightworks.Nightworks.AC_PIERCE;
import static net.sf.nightworks.Nightworks.AC_SLASH;
import static net.sf.nightworks.Nightworks.AFFECT_DATA;
import static net.sf.nightworks.Nightworks.AFF_ABSORB;
import static net.sf.nightworks.Nightworks.AFF_AURA_CHAOS;
import static net.sf.nightworks.Nightworks.AFF_BERSERK;
import static net.sf.nightworks.Nightworks.AFF_CAMOUFLAGE;
import static net.sf.nightworks.Nightworks.AFF_CHARM;
import static net.sf.nightworks.Nightworks.AFF_EARTHFADE;
import static net.sf.nightworks.Nightworks.AFF_FADE;
import static net.sf.nightworks.Nightworks.AFF_FEAR;
import static net.sf.nightworks.Nightworks.AFF_FLYING;
import static net.sf.nightworks.Nightworks.AFF_HASTE;
import static net.sf.nightworks.Nightworks.AFF_HIDE;
import static net.sf.nightworks.Nightworks.AFF_IMP_INVIS;
import static net.sf.nightworks.Nightworks.AFF_INVISIBLE;
import static net.sf.nightworks.Nightworks.AFF_PASS_DOOR;
import static net.sf.nightworks.Nightworks.AFF_POISON;
import static net.sf.nightworks.Nightworks.AFF_PROTECTOR;
import static net.sf.nightworks.Nightworks.AFF_PROTECT_EVIL;
import static net.sf.nightworks.Nightworks.AFF_PROTECT_GOOD;
import static net.sf.nightworks.Nightworks.AFF_SANCTUARY;
import static net.sf.nightworks.Nightworks.AFF_SLEEP;
import static net.sf.nightworks.Nightworks.AFF_SNEAK;
import static net.sf.nightworks.Nightworks.AFF_SPELLBANE;
import static net.sf.nightworks.Nightworks.AFF_STUN;
import static net.sf.nightworks.Nightworks.AFF_WEAK_STUN;
import static net.sf.nightworks.Nightworks.ANGEL;
import static net.sf.nightworks.Nightworks.APPLY_STR;
import static net.sf.nightworks.Nightworks.ASSIST_ALIGN;
import static net.sf.nightworks.Nightworks.ASSIST_ALL;
import static net.sf.nightworks.Nightworks.ASSIST_PLAYERS;
import static net.sf.nightworks.Nightworks.ASSIST_RACE;
import static net.sf.nightworks.Nightworks.ASSIST_VNUM;
import static net.sf.nightworks.Nightworks.CABAL_BATTLE;
import static net.sf.nightworks.Nightworks.CABAL_CHAOS;
import static net.sf.nightworks.Nightworks.CABAL_NONE;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.CLEVEL_OK;
import static net.sf.nightworks.Nightworks.COND_BLOODLUST;
import static net.sf.nightworks.Nightworks.COND_DESIRE;
import static net.sf.nightworks.Nightworks.COND_FULL;
import static net.sf.nightworks.Nightworks.COND_HUNGER;
import static net.sf.nightworks.Nightworks.COND_THIRST;
import static net.sf.nightworks.Nightworks.DAM_BASH;
import static net.sf.nightworks.Nightworks.DAM_COLD;
import static net.sf.nightworks.Nightworks.DAM_FIRE;
import static net.sf.nightworks.Nightworks.DAM_HUNGER;
import static net.sf.nightworks.Nightworks.DAM_LIGHTNING;
import static net.sf.nightworks.Nightworks.DAM_LIGHT_V;
import static net.sf.nightworks.Nightworks.DAM_NEGATIVE;
import static net.sf.nightworks.Nightworks.DAM_NONE;
import static net.sf.nightworks.Nightworks.DAM_PIERCE;
import static net.sf.nightworks.Nightworks.DAM_POISON;
import static net.sf.nightworks.Nightworks.DAM_SLASH;
import static net.sf.nightworks.Nightworks.DAM_THIRST;
import static net.sf.nightworks.Nightworks.DAM_TRAP_ROOM;
import static net.sf.nightworks.Nightworks.DICE_NUMBER;
import static net.sf.nightworks.Nightworks.DICE_TYPE;
import static net.sf.nightworks.Nightworks.EXIT_DATA;
import static net.sf.nightworks.Nightworks.EX_CLOSED;
import static net.sf.nightworks.Nightworks.EX_NOFLEE;
import static net.sf.nightworks.Nightworks.EX_NOPASS;
import static net.sf.nightworks.Nightworks.FIGHT_DELAY_TIME;
import static net.sf.nightworks.Nightworks.FORM_EDIBLE;
import static net.sf.nightworks.Nightworks.FORM_INSTANT_DECAY;
import static net.sf.nightworks.Nightworks.FORM_POISON;
import static net.sf.nightworks.Nightworks.GET_AC;
import static net.sf.nightworks.Nightworks.GET_DAMROLL;
import static net.sf.nightworks.Nightworks.GET_HITROLL;
import static net.sf.nightworks.Nightworks.IS_AFFECTED;
import static net.sf.nightworks.Nightworks.IS_AWAKE;
import static net.sf.nightworks.Nightworks.IS_BLINK_ON;
import static net.sf.nightworks.Nightworks.IS_EVIL;
import static net.sf.nightworks.Nightworks.IS_GOLEM;
import static net.sf.nightworks.Nightworks.IS_GOOD;
import static net.sf.nightworks.Nightworks.IS_IMMORTAL;
import static net.sf.nightworks.Nightworks.IS_IMMUNE;
import static net.sf.nightworks.Nightworks.IS_NEUTRAL;
import static net.sf.nightworks.Nightworks.IS_NPC;
import static net.sf.nightworks.Nightworks.IS_OBJ_STAT;
import static net.sf.nightworks.Nightworks.IS_QUESTOR;
import static net.sf.nightworks.Nightworks.IS_RESISTANT;
import static net.sf.nightworks.Nightworks.IS_SET;
import static net.sf.nightworks.Nightworks.IS_TRUSTED;
import static net.sf.nightworks.Nightworks.IS_VAMPIRE;
import static net.sf.nightworks.Nightworks.IS_VULNERABLE;
import static net.sf.nightworks.Nightworks.IS_WEAPON_STAT;
import static net.sf.nightworks.Nightworks.ITEM_ANTI_EVIL;
import static net.sf.nightworks.Nightworks.ITEM_ANTI_GOOD;
import static net.sf.nightworks.Nightworks.ITEM_ANTI_NEUTRAL;
import static net.sf.nightworks.Nightworks.ITEM_FOOD;
import static net.sf.nightworks.Nightworks.ITEM_INVENTORY;
import static net.sf.nightworks.Nightworks.ITEM_POTION;
import static net.sf.nightworks.Nightworks.ITEM_ROT_DEATH;
import static net.sf.nightworks.Nightworks.ITEM_SCROLL;
import static net.sf.nightworks.Nightworks.ITEM_TRASH;
import static net.sf.nightworks.Nightworks.ITEM_VIS_DEATH;
import static net.sf.nightworks.Nightworks.ITEM_WEAPON;
import static net.sf.nightworks.Nightworks.LEVEL_IMMORTAL;
import static net.sf.nightworks.Nightworks.MAX_LEVEL;
import static net.sf.nightworks.Nightworks.MOB_VNUM_STALKER;
import static net.sf.nightworks.Nightworks.MOUNTED;
import static net.sf.nightworks.Nightworks.MPROG_DEATH;
import static net.sf.nightworks.Nightworks.MPROG_FIGHT;
import static net.sf.nightworks.Nightworks.OBJ_DATA;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_BRAINS;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_CORPSE_NPC;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_CORPSE_PC;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_GUTS;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SEVERED_HEAD;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SLICED_ARM;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SLICED_LEG;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_TORN_HEART;
import static net.sf.nightworks.Nightworks.OFF_AREA_ATTACK;
import static net.sf.nightworks.Nightworks.OFF_BASH;
import static net.sf.nightworks.Nightworks.OFF_BERSERK;
import static net.sf.nightworks.Nightworks.OFF_CRUSH;
import static net.sf.nightworks.Nightworks.OFF_DISARM;
import static net.sf.nightworks.Nightworks.OFF_FAST;
import static net.sf.nightworks.Nightworks.OFF_KICK;
import static net.sf.nightworks.Nightworks.OFF_KICK_DIRT;
import static net.sf.nightworks.Nightworks.OFF_TAIL;
import static net.sf.nightworks.Nightworks.OFF_TRIP;
import static net.sf.nightworks.Nightworks.OPROG_DEATH;
import static net.sf.nightworks.Nightworks.OPROG_FIGHT;
import static net.sf.nightworks.Nightworks.PART_ARMS;
import static net.sf.nightworks.Nightworks.PART_BRAINS;
import static net.sf.nightworks.Nightworks.PART_GUTS;
import static net.sf.nightworks.Nightworks.PART_HEAD;
import static net.sf.nightworks.Nightworks.PART_HEART;
import static net.sf.nightworks.Nightworks.PART_LEGS;
import static net.sf.nightworks.Nightworks.PLR_AUTOASSIST;
import static net.sf.nightworks.Nightworks.PLR_AUTOGOLD;
import static net.sf.nightworks.Nightworks.PLR_AUTOLOOT;
import static net.sf.nightworks.Nightworks.PLR_AUTOSAC;
import static net.sf.nightworks.Nightworks.PLR_BOUGHT_PET;
import static net.sf.nightworks.Nightworks.PLR_CANINDUCT;
import static net.sf.nightworks.Nightworks.PLR_CANLOOT;
import static net.sf.nightworks.Nightworks.PLR_GHOST;
import static net.sf.nightworks.Nightworks.PLR_WANTED;
import static net.sf.nightworks.Nightworks.POS_DEAD;
import static net.sf.nightworks.Nightworks.POS_FIGHTING;
import static net.sf.nightworks.Nightworks.POS_INCAP;
import static net.sf.nightworks.Nightworks.POS_MORTAL;
import static net.sf.nightworks.Nightworks.POS_RESTING;
import static net.sf.nightworks.Nightworks.POS_SITTING;
import static net.sf.nightworks.Nightworks.POS_SLEEPING;
import static net.sf.nightworks.Nightworks.POS_STANDING;
import static net.sf.nightworks.Nightworks.POS_STUNNED;
import static net.sf.nightworks.Nightworks.PULSE_VIOLENCE;
import static net.sf.nightworks.Nightworks.REMOVE_BIT;
import static net.sf.nightworks.Nightworks.RIDDEN;
import static net.sf.nightworks.Nightworks.ROOM_INDEX_DATA;
import static net.sf.nightworks.Nightworks.ROOM_NO_MOB;
import static net.sf.nightworks.Nightworks.SECT_INSIDE;
import static net.sf.nightworks.Nightworks.SET_BIT;
import static net.sf.nightworks.Nightworks.STAT_CHA;
import static net.sf.nightworks.Nightworks.STAT_CON;
import static net.sf.nightworks.Nightworks.STAT_DEX;
import static net.sf.nightworks.Nightworks.STAT_STR;
import static net.sf.nightworks.Nightworks.TARGET_CHAR;
import static net.sf.nightworks.Nightworks.TAR_CHAR_OFFENSIVE;
import static net.sf.nightworks.Nightworks.TAR_IGNORE;
import static net.sf.nightworks.Nightworks.TO_AFFECTS;
import static net.sf.nightworks.Nightworks.TO_CHAR;
import static net.sf.nightworks.Nightworks.TO_NOTVICT;
import static net.sf.nightworks.Nightworks.TO_ROOM;
import static net.sf.nightworks.Nightworks.TO_VICT;
import static net.sf.nightworks.Nightworks.UMAX;
import static net.sf.nightworks.Nightworks.UMIN;
import static net.sf.nightworks.Nightworks.URANGE;
import static net.sf.nightworks.Nightworks.WAIT_STATE;
import static net.sf.nightworks.Nightworks.WEAPON_FLAMING;
import static net.sf.nightworks.Nightworks.WEAPON_FROST;
import static net.sf.nightworks.Nightworks.WEAPON_HOLY;
import static net.sf.nightworks.Nightworks.WEAPON_KATANA;
import static net.sf.nightworks.Nightworks.WEAPON_POISON;
import static net.sf.nightworks.Nightworks.WEAPON_SHARP;
import static net.sf.nightworks.Nightworks.WEAPON_SHOCKING;
import static net.sf.nightworks.Nightworks.WEAPON_VAMPIRIC;
import static net.sf.nightworks.Nightworks.WEAR_NONE;
import static net.sf.nightworks.Nightworks.WEAR_TATTOO;
import static net.sf.nightworks.Nightworks.char_list;
import static net.sf.nightworks.Nightworks.current_time;
import static net.sf.nightworks.Nightworks.nw_config;
import static net.sf.nightworks.Nightworks.sprintf;
import static net.sf.nightworks.Save.save_char_obj;
import static net.sf.nightworks.Skill.gsn_absorb;
import static net.sf.nightworks.Skill.gsn_ambush;
import static net.sf.nightworks.Skill.gsn_area_attack;
import static net.sf.nightworks.Skill.gsn_armor_use;
import static net.sf.nightworks.Skill.gsn_assassinate;
import static net.sf.nightworks.Skill.gsn_backstab;
import static net.sf.nightworks.Skill.gsn_bash;
import static net.sf.nightworks.Skill.gsn_blind_fighting;
import static net.sf.nightworks.Skill.gsn_blink;
import static net.sf.nightworks.Skill.gsn_circle;
import static net.sf.nightworks.Skill.gsn_cleave;
import static net.sf.nightworks.Skill.gsn_counter;
import static net.sf.nightworks.Skill.gsn_cross_block;
import static net.sf.nightworks.Skill.gsn_deathblow;
import static net.sf.nightworks.Skill.gsn_dodge;
import static net.sf.nightworks.Skill.gsn_doppelganger;
import static net.sf.nightworks.Skill.gsn_dual_backstab;
import static net.sf.nightworks.Skill.gsn_enhanced_damage;
import static net.sf.nightworks.Skill.gsn_fifth_attack;
import static net.sf.nightworks.Skill.gsn_fourth_attack;
import static net.sf.nightworks.Skill.gsn_hand_block;
import static net.sf.nightworks.Skill.gsn_hand_to_hand;
import static net.sf.nightworks.Skill.gsn_katana;
import static net.sf.nightworks.Skill.gsn_lightning_breath;
import static net.sf.nightworks.Skill.gsn_master_hand;
import static net.sf.nightworks.Skill.gsn_master_sword;
import static net.sf.nightworks.Skill.gsn_mental_knife;
import static net.sf.nightworks.Skill.gsn_mirror;
import static net.sf.nightworks.Skill.gsn_mortal_strike;
import static net.sf.nightworks.Skill.gsn_parry;
import static net.sf.nightworks.Skill.gsn_plague;
import static net.sf.nightworks.Skill.gsn_poison;
import static net.sf.nightworks.Skill.gsn_power_stun;
import static net.sf.nightworks.Skill.gsn_protection_cold;
import static net.sf.nightworks.Skill.gsn_protection_heat;
import static net.sf.nightworks.Skill.gsn_second_attack;
import static net.sf.nightworks.Skill.gsn_second_weapon;
import static net.sf.nightworks.Skill.gsn_secondary_attack;
import static net.sf.nightworks.Skill.gsn_shield_block;
import static net.sf.nightworks.Skill.gsn_sleep;
import static net.sf.nightworks.Skill.gsn_spellbane;
import static net.sf.nightworks.Skill.gsn_sword;
import static net.sf.nightworks.Skill.gsn_third_attack;
import static net.sf.nightworks.Skill.gsn_trip;
import static net.sf.nightworks.Skill.gsn_vampiric_bite;
import static net.sf.nightworks.Skill.gsn_witch_curse;
import static net.sf.nightworks.Skill.gsn_x_hit;
import static net.sf.nightworks.Skill.gsn_x_hunger;
import static net.sf.nightworks.Skill.lookupSkill;
import static net.sf.nightworks.Update.gain_condition;
import static net.sf.nightworks.Update.gain_exp;
import static net.sf.nightworks.util.TextUtils.capitalize;
import static net.sf.nightworks.util.TextUtils.one_argument;

/**
 * Last mofified:           $Date: 2006-07-21 06:43:28 -0700 (Fri, 21 Jul 2006) $
 * Revision of last commit: $Revision: 27 $
 */
class Fight {
    public static final int MAX_DAMAGE_MESSAGE = 34;

/*
 * Control the fights going on.
 * Called periodically by update_handler.
 */

    static void violence_update() {
        CHAR_DATA ch;
        CHAR_DATA ch_next;
        CHAR_DATA victim;
        OBJ_DATA obj;

        for (ch = char_list; ch != null; ch = ch_next) {
            ch_next = ch.next;

            if ((victim = ch.fighting) == null || ch.in_room == null) {
                continue;
            }


            if (IS_AWAKE(ch) && ch.in_room == victim.in_room) {
                multi_hit(ch, victim, null);
            } else {
                stop_fighting(ch, false);
            }

            if ((victim = ch.fighting) == null) {
                continue;
            }

            if (!IS_NPC(victim)) {
                ch.last_fought = victim;
            }

/*  more efficient DOPPLEGANGER
    if (!IS_NPC(victim))
      ch.last_fought =
    (is_affected(victim,gsn_doppelganger) && victim.doppel ) ?
            victim.doppel : victim;
*/

            ch.last_fight_time = current_time;


            for (obj = ch.carrying; obj != null; obj = obj.next_content) {
                if (IS_SET(obj.progtypes, OPROG_FIGHT)) {
                    if (ch.fighting == null) {
                        break; /* previously death victims! */
                    }
                    obj.pIndexData.oprogs.fight_prog.run(obj, ch);
                }
            }

            if ((victim = ch.fighting) == null) {
                continue; /* death victim */
            }

            if (IS_SET(ch.progtypes, MPROG_FIGHT) && (ch.wait <= 0)) {
                ch.pIndexData.mprogs.fight_prog.run(ch, victim);
            }

            /*
            * Fun for the whole family!
            */
            check_assist(ch, victim);
        }

    }

/* for auto assisting */

    static void check_assist(CHAR_DATA ch, CHAR_DATA victim) {
        CHAR_DATA rch, rch_next;

        for (rch = ch.in_room.people; rch != null; rch = rch_next) {
            rch_next = rch.next_in_room;

            if (IS_AWAKE(rch) && rch.fighting == null) {

                /* quick check for ASSIST_PLAYER */
                if (!IS_NPC(ch) && IS_NPC(rch)
                        && IS_SET(rch.off_flags, ASSIST_PLAYERS)
                        && rch.level + 6 > victim.level) {
                    do_emote(rch, "screams and attacks!");
                    multi_hit(rch, victim, null);
                    continue;
                }

                /* PCs next */
                if (!IS_NPC(rch) || IS_AFFECTED(rch, AFF_CHARM)) {
                    if (((!IS_NPC(rch) && IS_SET(rch.act, PLR_AUTOASSIST))
                            || IS_AFFECTED(rch, AFF_CHARM))
                            && is_same_group(ch, rch)) {
                        multi_hit(rch, victim, null);
                    }

                    continue;
                }

                if (!IS_NPC(ch) && RIDDEN(rch) == ch) {
                    multi_hit(rch, victim, null);
                    continue;
                }

                /* now check the NPC cases */

                if (IS_NPC(ch))

                {
                    if ((IS_NPC(rch) && IS_SET(rch.off_flags, ASSIST_ALL))

                            || (IS_NPC(rch) && rch.race == ch.race
                            && IS_SET(rch.off_flags, ASSIST_RACE))

                            || (IS_NPC(rch) && IS_SET(rch.off_flags, ASSIST_ALIGN)
                            && ((IS_GOOD(rch) && IS_GOOD(ch))
                            || (IS_EVIL(rch) && IS_EVIL(ch))
                            || (IS_NEUTRAL(rch) && IS_NEUTRAL(ch))))

                            || (rch.pIndexData == ch.pIndexData
                            && IS_SET(rch.off_flags, ASSIST_VNUM)))

                    {
                        CHAR_DATA vch;
                        CHAR_DATA target;
                        int number;

                        if (number_bits(1) == 0) {
                            continue;
                        }

                        target = null;
                        number = 0;

                        for (vch = ch.in_room.people; vch != null; vch = vch.next_in_room) {
                            if (can_see(rch, vch)
                                    && is_same_group(vch, victim)
                                    && number_range(0, number) == 0) {
                                target = vch;
                                number++;
                            }
                        }

                        if (target != null) {
                            do_emote(rch, "screams and attacks!");
                            multi_hit(rch, target, null);
                        }
                    }
                }
            }
        }
    }

/*
* Do one group of attacks.
*/

    static void multi_hit(CHAR_DATA ch, CHAR_DATA victim, Skill dt) {
        int chance;

        /* decrement the wait */
        if (ch.desc == null) {
            ch.wait = UMAX(0, ch.wait - PULSE_VIOLENCE);
        }

        /* no attacks for stunnies -- just a check */
        if (ch.position < POS_RESTING) {
            return;
        }

        /* ridden's adjustment */
        if (RIDDEN(victim) != null && !IS_NPC(victim.mount)) {
            if (victim.mount.fighting == null
                    || victim.mount.fighting == ch) {
                victim = victim.mount;
            } else {
                do_dismount(victim.mount, "");
            }
        }

        /* no attacks on ghosts or attacks by ghosts */
        if ((!IS_NPC(victim) && IS_SET(victim.act, PLR_GHOST)) ||
                (!IS_NPC(ch) && IS_SET(ch.act, PLR_GHOST))) {
            return;
        }

        if (IS_AFFECTED(ch, AFF_WEAK_STUN)) {
            act("{WYou are too stunned to respond $N's attack.{x", ch, null, victim, TO_CHAR, POS_FIGHTING);
            act("{W$n is too stunned to respond your attack.{x", ch, null, victim, TO_VICT, POS_FIGHTING);
            act("{W$n seems to be stunned.{x", ch, null, victim, TO_NOTVICT, POS_FIGHTING);
            ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_WEAK_STUN);
            return;
        }

        if (IS_AFFECTED(ch, AFF_STUN)) {
            act("{WYou are too stunned to respond $N's attack.{x", ch, null, victim, TO_CHAR, POS_FIGHTING);
            act("{W$n is too stunned to respond your attack.{x", ch, null, victim, TO_VICT, POS_FIGHTING);
            act("{W$n seems to be stunned.{x", ch, null, victim, TO_NOTVICT, POS_FIGHTING);
            affect_strip(ch, gsn_power_stun);
            ch.affected_by = SET_BIT(ch.affected_by, AFF_WEAK_STUN);
            return;
        }

        if (IS_NPC(ch)) {
            mob_hit(ch, victim, dt);
            return;
        }

        one_hit(ch, victim, dt, false);

        if (ch.fighting != victim) {
            return;
        }

        if (CLEVEL_OK(ch, gsn_area_attack)
                && number_percent() < get_skill(ch, gsn_area_attack)) {
            int count = 0, max_count;
            CHAR_DATA vch, vch_next;

            check_improve(ch, gsn_area_attack, true, 6);

            if (ch.level < 70) {
                max_count = 1;
            } else if (ch.level < 80) {
                max_count = 2;
            } else if (ch.level < 90) {
                max_count = 3;
            } else {
                max_count = 4;
            }

            for (vch = ch.in_room.people; vch != null; vch = vch_next) {
                vch_next = vch.next_in_room;
                if ((vch != victim && vch.fighting == ch)) {
                    one_hit(ch, vch, dt, false);
                    count++;
                }
                if (count == max_count) {
                    break;
                }
            }
        }

        if (IS_AFFECTED(ch, AFF_HASTE)) {
            one_hit(ch, victim, dt, false);
        }

        if (ch.fighting != victim || dt == gsn_backstab || dt == gsn_cleave
                || dt == gsn_ambush || dt == gsn_dual_backstab || dt == gsn_circle
                || dt == gsn_assassinate || dt == gsn_vampiric_bite) {
            return;
        }

        chance = get_skill(ch, gsn_second_attack) / 2;
        if (number_percent() < chance) {
            one_hit(ch, victim, dt, false);
            check_improve(ch, gsn_second_attack, true, 5);
            if (ch.fighting != victim) {
                return;
            }
        }

        chance = get_skill(ch, gsn_third_attack) / 3;
        if (number_percent() < chance) {
            one_hit(ch, victim, dt, false);
            check_improve(ch, gsn_third_attack, true, 6);
            if (ch.fighting != victim) {
                return;
            }
        }


        chance = get_skill(ch, gsn_fourth_attack) / 5;
        if (number_percent() < chance) {
            one_hit(ch, victim, dt, false);
            check_improve(ch, gsn_fourth_attack, true, 7);
            if (ch.fighting != victim) {
                return;
            }
        }

        chance = get_skill(ch, gsn_fifth_attack) / 6;
        if (number_percent() < chance) {
            one_hit(ch, victim, dt, false);
            check_improve(ch, gsn_fifth_attack, true, 8);
            if (ch.fighting != victim) {
                return;
            }
        }

        chance = 20 + (int) (get_skill(ch, gsn_second_weapon) * 0.8);
        if (number_percent() < chance) {
            if (get_wield_char(ch, true) != null) {
                one_hit(ch, victim, dt, true);
                check_improve(ch, gsn_second_weapon, true, 2);
                if (ch.fighting != victim) {
                    return;
                }
            }
        }

        chance = get_skill(ch, gsn_secondary_attack) / 4;
        if (number_percent() < chance) {
            if (get_wield_char(ch, true) != null) {
                one_hit(ch, victim, dt, true);
                check_improve(ch, gsn_secondary_attack, true, 2);
            }
        }

    }

/* procedure for all mobile attacks */

    static void mob_hit(CHAR_DATA ch, CHAR_DATA victim, Skill dt) {
        int chance, number;
        CHAR_DATA vch, vch_next;

        /* no attacks on ghosts */
        if (!IS_NPC(victim) && IS_SET(victim.act, PLR_GHOST)) {
            return;
        }

        /* no attack by ridden mobiles except spec_casts */
        if (RIDDEN(ch) != null) {
            if (ch.fighting != victim) {
                set_fighting(ch, victim);
            }
            return;
        }

        one_hit(ch, victim, dt, false);

        if (ch.fighting != victim) {
            return;
        }

        /* Area attack -- BALLS nasty! */

        if (IS_SET(ch.off_flags, OFF_AREA_ATTACK)) {
            for (vch = ch.in_room.people; vch != null; vch = vch_next) {
                vch_next = vch.next_in_room;
                if ((vch != victim && vch.fighting == ch)) {
                    one_hit(ch, vch, dt, false);
                }
            }
        }

        if (IS_AFFECTED(ch, AFF_HASTE) || IS_SET(ch.off_flags, OFF_FAST)) {
            one_hit(ch, victim, dt, false);
        }

        if (ch.fighting != victim || dt == gsn_backstab || dt == gsn_circle ||
                dt == gsn_dual_backstab || dt == gsn_cleave || dt == gsn_ambush
                || dt == gsn_vampiric_bite) {
            return;
        }

        chance = get_skill(ch, gsn_second_attack) / 2;
        if (number_percent() < chance) {
            one_hit(ch, victim, dt, false);
            if (ch.fighting != victim) {
                return;
            }
        }

        chance = get_skill(ch, gsn_third_attack) / 4;
        if (number_percent() < chance) {
            one_hit(ch, victim, dt, false);
            if (ch.fighting != victim) {
                return;
            }
        }

        chance = get_skill(ch, gsn_fourth_attack) / 6;
        if (number_percent() < chance) {
            one_hit(ch, victim, dt, false);
            if (ch.fighting != victim) {
                return;
            }
        }

        chance = get_skill(ch, gsn_second_weapon) / 2;
        if (number_percent() < chance) {
            if (get_wield_char(ch, true) != null) {
                one_hit(ch, victim, dt, true);
                if (ch.fighting != victim) {
                    return;
                }
            }
        }

        if (IS_SET(ch.act, ACT_MAGE)) {
            if (number_percent() < 60 && ch.spec_fun == null) {
                mob_cast_mage(ch, victim);
                return;
            }
        }


        if (IS_SET(ch.act, ACT_CLERIC)) {
            if (number_percent() < 60 && ch.spec_fun == null) {
                mob_cast_cleric(ch, victim);
                return;
            }
        }

        /* PC waits */

        if (ch.wait > 0) {
            return;
        }

        /* now for the skills */

        number = number_range(0, 7);

        switch (number) {
            case (0):
                if (IS_SET(ch.off_flags, OFF_BASH)) {
                    do_bash(ch, "");
                }
                break;

            case (1):
                if (IS_SET(ch.off_flags, OFF_BERSERK) && !IS_AFFECTED(ch, AFF_BERSERK)) {
                    do_berserk(ch, "");
                }
                break;


            case (2):
                if (IS_SET(ch.off_flags, OFF_DISARM)
                        || (get_weapon_sn(ch, false) != gsn_hand_to_hand
                        && (IS_SET(ch.act, ACT_WARRIOR)
                        || IS_SET(ch.act, ACT_THIEF)))) {
                    do_disarm(ch, "");
                }
                break;

            case (3):
                if (IS_SET(ch.off_flags, OFF_KICK)) {
                    do_kick(ch, "");
                }
                break;

            case (4):
                if (IS_SET(ch.off_flags, OFF_KICK_DIRT)) {
                    do_dirt(ch, "");
                }
                break;

            case (5):
                if (IS_SET(ch.off_flags, OFF_TAIL)) {
                    do_tail(ch, "");
                }
                break;

            case (6):
                if (IS_SET(ch.off_flags, OFF_TRIP)) {
                    do_trip(ch, "");
                }
                break;
            case (7):
                if (IS_SET(ch.off_flags, OFF_CRUSH)) {
                    do_crush(ch, "");
                }
                break;
        }
    }

/*
 * Hit one guy once.
 */

    static void one_hit(CHAR_DATA ch, CHAR_DATA victim, Skill dt, boolean secondary) {
        OBJ_DATA wield;
        int victim_ac;
        int thac0;
        int thac0_00;
        int thac0_32;
        int dam;
        int diceroll;

        int dam_type = -1;
        boolean counter;
        boolean result;
        OBJ_DATA corpse;
        int sercount;

        counter = false;

        /* just in case */
        if (victim == ch || ch == null || victim == null) {
            return;
        }

        /* ghosts can't fight */
        if ((!IS_NPC(victim) && IS_SET(victim.act, PLR_GHOST)) ||
                (!IS_NPC(ch) && IS_SET(ch.act, PLR_GHOST))) {
            return;
        }

        /*
        * Can't beat a dead char!
        * Guard against weird room-leavings.
        */
        if (victim.position == POS_DEAD || ch.in_room != victim.in_room) {
            return;
        }

        /*
        * Figure out the type of damage message.
        */

        wield = get_wield_char(ch, secondary);

        /*
        * if there is no weapon held by pro-hand, and there is a weapon
        * in the other hand, than don't fight with punch.
        */
        if (!secondary && dt == null && wield == null && get_wield_char(ch, true) != null) {
            if (ch.fighting != victim) {
                secondary = true;
                wield = get_wield_char(ch, secondary);
            } else {
                return;
            }
        }

        if (dt == null) {
            dt = gsn_x_hit;
            if (wield != null && wield.item_type == ITEM_WEAPON) {
                dam_type = wield.value[3];
            } else {
                dam_type = ch.dam_type;
            }
            dam_type = attack_table[dam_type].damage;
        } else if (dt.ordinal() < gsn_x_hit.ordinal()) {
            if (wield != null) {
                dam_type = attack_table[wield.value[3]].damage;
            } else {
                dam_type = attack_table[ch.dam_type].damage;
            }
        }
        if (dam_type == -1) {
            dam_type = DAM_BASH;
        }

        /* get the weapon skill */
        Skill sn = get_weapon_sn(ch, secondary);
        int skill = 20 + get_weapon_skill(ch, sn);

        /*
        * Calculate to-hit-armor-class-0 versus armor.
        */
        if (IS_NPC(ch)) {
            thac0_00 = 20;
            thac0_32 = -4;   /* as good as a thief */
            if (IS_SET(ch.act, ACT_WARRIOR)) {
                thac0_32 = -10;
            } else if (IS_SET(ch.act, ACT_THIEF)) {
                thac0_32 = -4;
            } else if (IS_SET(ch.act, ACT_CLERIC)) {
                thac0_32 = 2;
            } else if (IS_SET(ch.act, ACT_MAGE)) {
                thac0_32 = 6;
            }
        } else {
            thac0_00 = ch.clazz.thac0_00;
            thac0_32 = ch.clazz.thac0_32;
        }

        thac0 = interpolate(ch.level, thac0_00, thac0_32);

        if (thac0 < 0) {
            thac0 = thac0 / 2;
        }

        if (thac0 < -5) {
            thac0 = -5 + (thac0 + 5) / 2;
        }

        thac0 -= GET_HITROLL(ch) * skill / 100;
        thac0 += 5 * (100 - skill) / 100;

        if (dt == gsn_backstab) {
            thac0 -= 10 * (100 - get_skill(ch, gsn_backstab));
        }

        if (dt == gsn_dual_backstab) {
            thac0 -= 10 * (100 - get_skill(ch, gsn_dual_backstab));
        }

        if (dt == gsn_cleave) {
            thac0 -= 10 * (100 - get_skill(ch, gsn_cleave));
        }

        if (dt == gsn_ambush) {
            thac0 -= 10 * (100 - get_skill(ch, gsn_ambush));
        }

        if (dt == gsn_vampiric_bite) {
            thac0 -= 10 * (100 - get_skill(ch, gsn_vampiric_bite));
        }

        switch (dam_type) {
            case (DAM_PIERCE):
                victim_ac = GET_AC(victim, AC_PIERCE) / 10;
                break;
            case (DAM_BASH):
                victim_ac = GET_AC(victim, AC_BASH) / 10;
                break;
            case (DAM_SLASH):
                victim_ac = GET_AC(victim, AC_SLASH) / 10;
                break;
            default:
                victim_ac = GET_AC(victim, AC_EXOTIC) / 10;
                break;
        }

        if (victim_ac < -15) {
            victim_ac = (victim_ac + 15) / 5 - 15;
        }

        if (get_skill(victim, gsn_armor_use) > 70) {
            check_improve(victim, gsn_armor_use, true, 8);
            victim_ac -= (victim.level) / 2;
        }

        if (!can_see(ch, victim)) {
            if (skill_failure_nomessage(ch, gsn_blind_fighting, 0) == 0 && number_percent() < get_skill(ch, gsn_blind_fighting)) {
                check_improve(ch, gsn_blind_fighting, true, 16);
            } else {
                victim_ac -= 4;
            }
        }

        if (victim.position < POS_FIGHTING) {
            victim_ac += 4;
        }

        if (victim.position < POS_RESTING) {
            victim_ac += 6;
        }

        /*
        * The moment of excitement!
        */
        while ((diceroll = number_bits(5)) >= 20) {
        }

        if (diceroll == 0 || (diceroll != 19 && diceroll < thac0 - victim_ac)) {
            // Miss
            damage(ch, victim, 0, dt, dam_type, true);
            tail_chain();
            return;
        }

        /*
        * Hit.
        * Calc damage.
        */


        if (IS_NPC(ch) && (!ch.pIndexData.new_format || wield == null)) {
            if (!ch.pIndexData.new_format) {
                dam = number_range(ch.level / 2, ch.level * 3 / 2);
                if (wield != null) {
                    dam += dam / 2;
                }
            } else {
                dam = dice(ch.damage[DICE_NUMBER], ch.damage[DICE_TYPE]);
            }
        } else {
            if (sn != null) {
                check_improve(ch, sn, true, 5);
            }
            if (wield != null) {
                if (wield.pIndexData.new_format) {
                    dam = dice(wield.value[1], wield.value[2]) * skill / 100;
                } else {
                    dam = number_range(wield.value[1] * skill / 100,
                            wield.value[2] * skill / 100);
                }

                if (get_shield_char(ch) == null)  /* no shield = more */ {
                    dam = dam * 21 / 20;
                }

                /* sharpness! */
                if (IS_WEAPON_STAT(wield, WEAPON_SHARP)) {
                    int percent;

                    if ((percent = number_percent()) <= (skill / 8)) {
                        dam = 2 * dam + (dam * 2 * percent / 100);
                    }
                }
                /* holy weapon */
                if (IS_WEAPON_STAT(wield, WEAPON_HOLY) &&
                        IS_GOOD(ch) && IS_EVIL(victim) && number_percent() < 30) {
                    act("{Y$p shines with a holy area.{x", ch, wield, null, TO_CHAR, POS_DEAD);
                    act("{Y$p shines with a holy area.{x", ch, wield, null, TO_ROOM, POS_DEAD);
                    dam += dam * 120 / 100;
                }
            } else {
                if (CLEVEL_OK(ch, gsn_hand_to_hand)) {
                    if (number_percent() < get_skill(ch, gsn_hand_to_hand)) {
                        dam = number_range(4 + ch.level / 10, 2 * ch.level / 3) * skill / 100;
                    } else {
                        dam = number_range(5, ch.level / 2) * skill / 100;
                        check_improve(ch, gsn_hand_to_hand, false, 5);
                    }
                } else {
                    dam = number_range(5, ch.level / 2) * skill / 100;
                }

                if (get_skill(ch, gsn_master_hand) > 0) {
                    int d;

                    if ((d = number_percent()) <= get_skill(ch, gsn_master_hand)) {
                        check_improve(ch, gsn_master_hand, true, 6);
                        dam *= 2;
                        if (d < 10) {
                            victim.affected_by = SET_BIT(victim.affected_by, AFF_WEAK_STUN);
                            act("{rYou hit $N with a stunning force!{x", ch, null, victim, TO_CHAR, POS_DEAD);
                            act("{r$n hit you with a stunning force!{x", ch, null, victim, TO_VICT, POS_DEAD);
                            act("{r$n hits $N with a stunning force!{x", ch, null, victim, TO_NOTVICT, POS_DEAD);
                            check_improve(ch, gsn_master_hand, true, 6);
                        }
                    }

                }

            }
        }

        /*
        * Bonuses.
        */
        int skillLevel = get_skill(ch, gsn_enhanced_damage);
        if (skillLevel > 0) {
            diceroll = number_percent();
            if (diceroll <= get_skill(ch, gsn_enhanced_damage)) {
                int div;
                check_improve(ch, gsn_enhanced_damage, true, 6);
                dam += dam * diceroll * skillLevel / 10000;
            }
        }

        if (get_skill(ch, gsn_master_sword) > 0 && sn == gsn_sword) {
            if (number_percent() <= get_skill(ch, gsn_master_sword)) {
                OBJ_DATA katana;

                check_improve(ch, gsn_master_sword, true, 6);
                dam += dam * 110 / 100;

                if ((katana = get_wield_char(ch, false)) != null) {
                    AFFECT_DATA paf;

                    if (IS_WEAPON_STAT(katana, WEAPON_KATANA) && katana.extra_descr.description.contains(ch.name)) {
                        katana.cost++;
                        if (katana.cost > 249) {
                            paf = affect_find(katana.affected, gsn_katana);
                            if (paf != null) {
                                int old_mod = paf.modifier;
                                paf.modifier = UMIN((paf.modifier + 1), (ch.level / 3));
                                ch.hitroll += paf.modifier - old_mod;
                                if (paf.next != null) {
                                    paf.next.modifier = paf.modifier;
                                    ch.damroll += paf.modifier - old_mod;
                                }
                                act("$n's katana glows blue.\n", ch, null, null, TO_ROOM);
                                send_to_char("Your katana glows blue.\n", ch);
                            }
                            katana.cost = 0;
                        }
                    }
                } else if ((katana = get_wield_char(ch, true)) != null) {
                    AFFECT_DATA paf;

                    if (IS_WEAPON_STAT(katana, WEAPON_KATANA)
                            && katana.extra_descr.description.contains(ch.name)) {
                        katana.cost++;
                        if (katana.cost > 249) {
                            paf = affect_find(katana.affected, gsn_katana);
                            if (paf != null) {
                                int old_mod = paf.modifier;
                                paf.modifier = UMIN((paf.modifier + 1), (ch.level / 3));
                                ch.hitroll += paf.modifier - old_mod;
                                if (paf.next != null) {
                                    paf.next.modifier = paf.modifier;
                                    ch.damroll += paf.modifier - old_mod;
                                }
                                act("$n's katana glows blue.\n", ch, null, null, TO_ROOM);
                                send_to_char("Your katana glows blue.\n", ch);
                            }
                            katana.cost = 0;
                        }
                    }
                }
            }
        }

        if (!IS_AWAKE(victim)) {
            dam *= 2;
        } else if (victim.position < POS_FIGHTING) {
            dam = dam * 3 / 2;
        }

        sercount = number_percent();

        if (dt == gsn_backstab || dt == gsn_vampiric_bite) {
            sercount += 40;  /* 80% chance decrease of counter */
        }

        if (victim.last_fight_time != -1 &&
                (current_time - victim.last_fight_time) < FIGHT_DELAY_TIME) {
            sercount += 25; /* 50% chance decrease of counter */
        }

        sercount *= 2;

        if (victim.fighting == null && !IS_NPC(victim) &&
                !is_safe_nomessage(victim, ch) && !is_safe_nomessage(ch, victim) &&
                (victim.position == POS_SITTING || victim.position == POS_STANDING)
                && dt != gsn_assassinate &&
                (sercount <= get_skill(victim, gsn_counter))) {
            counter = true;
            check_improve(victim, gsn_counter, true, 1);
            act("$N turns your attack against you!", ch, null, victim, TO_CHAR);
            act("You turn $n's attack against $m!", ch, null, victim, TO_VICT);
            act("$N turns $n's attack against $m!", ch, null, victim, TO_NOTVICT);
            ch.fighting = victim;
        } else if (victim.fighting == null) {
            check_improve(victim, gsn_counter, false, 1);
        }

        if (dt == gsn_backstab && wield != null) {
            dam = (1 + ch.level / 10) * dam + ch.level;
        } else if (dt == gsn_dual_backstab && wield != null) {
            dam = (1 + ch.level / 14) * dam + ch.level;
        } else if (dt == gsn_circle) {
            dam = (ch.level / 40 + 1) * dam + ch.level;
        } else if (dt == gsn_vampiric_bite && IS_VAMPIRE(ch)) {
            dam = (ch.level / 20 + 1) * dam + ch.level;
        } else if (dt == gsn_cleave && wield != null) {
            if (number_percent() < URANGE(4, 5 + (ch.level - victim.level), 10) && !counter) {
                act("Your cleave chops $N {rIN HALF!{x", ch, null, victim, TO_CHAR, POS_RESTING);
                act("$n's cleave chops you {rIN HALF!{x", ch, null, victim, TO_VICT, POS_RESTING);
                act("$n's cleave chops $N {rIN HALF!{x", ch, null, victim, TO_NOTVICT, POS_RESTING);
                send_to_char("You have been KILLED!\n", victim);
                act("$n is DEAD!", victim, null, null, TO_ROOM);
                WAIT_STATE(ch, 2);
                raw_kill(victim);
                if (!IS_NPC(ch) && IS_NPC(victim)) {
                    corpse = get_obj_list(ch, "corpse", ch.in_room.contents);

                    if (IS_SET(ch.act, PLR_AUTOLOOT) && corpse != null && corpse.contains != null) /* exists and not empty */ {
                        do_get(ch, "all corpse");
                    }

                    if (IS_SET(ch.act, PLR_AUTOGOLD) && corpse != null && corpse.contains != null && !IS_SET(ch.act, PLR_AUTOLOOT)) { /* exists and not empty */
                        do_get(ch, "gold corpse");
                        do_get(ch, "silver corpse");
                    }

                    if (IS_SET(ch.act, PLR_AUTOSAC)) {
                        if (IS_SET(ch.act, PLR_AUTOLOOT) && corpse != null && corpse.contains != null) {
                            return;  /* leave if corpse has treasure */
                        } else {
                            do_sacrifice(ch, "corpse");
                        }
                    }
                }
                return;
            } else {
                dam = (dam * 2 + ch.level);
            }
        }

        if (dt == gsn_assassinate) {
            if (number_percent() <= URANGE(10, 20 + (ch.level - victim.level) * 2, 50) && !counter) {
                act("You {r+++ASSASSINATE+++{x $N!", ch, null, victim, TO_CHAR, POS_RESTING);
                act("$N is DEAD!", ch, null, victim, TO_CHAR);
                act("$n {r+++ASSASSINATES+++{x $N!", ch, null, victim, TO_NOTVICT, POS_RESTING);
                act("$N is DEAD!", ch, null, victim, TO_NOTVICT);
                act("$n {r+++ASSASSINATES+++{x you!", ch, null, victim, TO_VICT, POS_DEAD);
                send_to_char("You have been KILLED!\n", victim);
                check_improve(ch, gsn_assassinate, true, 1);
                raw_kill(victim);
                if (!IS_NPC(ch) && IS_NPC(victim)) {
                    corpse = get_obj_list(ch, "corpse", ch.in_room.contents);

                    if (IS_SET(ch.act, PLR_AUTOLOOT) && corpse != null && corpse.contains != null) /* exists and not empty */ {
                        do_get(ch, "all corpse");
                    }

                    if (IS_SET(ch.act, PLR_AUTOGOLD) && corpse != null && corpse.contains != null && !IS_SET(ch.act, PLR_AUTOLOOT)) /* exists and not empty */ {
                        do_get(ch, "gold corpse");
                    }

                    if (IS_SET(ch.act, PLR_AUTOSAC)) {
                        if (IS_SET(ch.act, PLR_AUTOLOOT) && corpse != null && corpse.contains != null) {
                            return;  /* leave if corpse has treasure */
                        } else {
                            do_sacrifice(ch, "corpse");
                        }
                    }
                }
                return;

            } else {
                check_improve(ch, gsn_assassinate, false, 1);
                dam *= 2;
            }
        }


        dam += GET_DAMROLL(ch) * UMIN(100, skill) / 100;

        if (dt == gsn_ambush) {
            dam *= 3;
        }

        if (skill_failure_nomessage(ch, gsn_deathblow, 0) == 0 && get_skill(ch, gsn_deathblow) > 1) {
            if (number_percent() < 0.125 * get_skill(ch, gsn_deathblow)) {
                act("You deliver a blow of deadly force!", ch, null, null, TO_CHAR);
                act("$n delivers a blow of deadly force!", ch, null, null, TO_ROOM);

                dam *= ((float) ch.level) / 20;
                check_improve(ch, gsn_deathblow, true, 1);
            } else {
                check_improve(ch, gsn_deathblow, false, 3);
            }
        }

        if (dam <= 0) {
            dam = 1;
        }

        if (counter) {
            result = damage(ch, ch, 2 * dam, dt, dam_type, true);
            multi_hit(victim, ch, null);
        } else {
            result = damage(ch, victim, dam, dt, dam_type, true);
        }

        /* vampiric bite gives hp to ch from victim */
        if (dt == gsn_vampiric_bite) {

            ch.hit += UMIN((dam / 2), victim.max_hit);
            ch.hit = UMIN(ch.hit, ch.max_hit);
            update_pos(ch);
            send_to_char("Your health increases as you suck your victim's blood.\n", ch);
        }

        /* but do we have a funky weapon? */
        if (result && wield != null) {

            if (ch.fighting == victim && IS_WEAPON_STAT(wield, WEAPON_POISON)) {
                int level;
                AFFECT_DATA poison;

                if ((poison = affect_find(wield.affected, gsn_poison)) == null) {
                    level = wield.level;
                } else {
                    level = poison.level;
                }
                if (!saves_spell(level / 2, victim, DAM_POISON)) {
                    send_to_char("You feel poison coursing through your veins.\n",
                            victim);
                    act("$n is poisoned by the venom on $p.",
                            victim, wield, null, TO_ROOM);

                    AFFECT_DATA af = new AFFECT_DATA();
                    af.where = TO_AFFECTS;
                    af.type = gsn_poison;
                    af.level = level * 3 / 4;
                    af.duration = level / 2;
                    af.location = APPLY_STR;
                    af.modifier = -1;
                    af.bitvector = AFF_POISON;
                    affect_join(victim, af);
                }

                /* weaken the poison if it's temporary */
                if (poison != null) {
                    poison.level = UMAX(0, poison.level - 2);
                    poison.duration = UMAX(0, poison.duration - 1);
                    if (poison.level == 0 || poison.duration == 0) {
                        act("The poison on $p has worn off.", ch, wield, null, TO_CHAR);
                    }
                }
            }
            if (ch.fighting == victim && IS_WEAPON_STAT(wield, WEAPON_VAMPIRIC)) {
                dam = number_range(1, wield.level / 5 + 1);
                act("$p draws life from $n.", victim, wield, null, TO_ROOM);
                act("You feel $p drawing your life away.",
                        victim, wield, null, TO_CHAR);
                damage(ch, victim, dam, null, DAM_NEGATIVE, false);
                ch.hit += dam / 2;
            }
            if (ch.fighting == victim && IS_WEAPON_STAT(wield, WEAPON_FLAMING)) {
                dam = number_range(1, wield.level / 4 + 1);
                act("$n is burned by $p.", victim, wield, null, TO_ROOM);
                act("$p sears your flesh.", victim, wield, null, TO_CHAR);
                fire_effect(victim, wield.level / 2, dam, TARGET_CHAR);
                damage(ch, victim, dam, null, DAM_FIRE, false);
            }
            if (ch.fighting == victim && IS_WEAPON_STAT(wield, WEAPON_FROST)) {
                dam = number_range(1, wield.level / 6 + 2);
                act("$p freezes $n.", victim, wield, null, TO_ROOM);
                act("The cold touch of $p surrounds you with ice.",
                        victim, wield, null, TO_CHAR);
                cold_effect(victim, wield.level / 2, dam, TARGET_CHAR);
                damage(ch, victim, dam, null, DAM_COLD, false);
            }
            if (ch.fighting == victim && IS_WEAPON_STAT(wield, WEAPON_SHOCKING)) {
                dam = number_range(1, wield.level / 5 + 2);
                act("$n is struck by lightning from $p.", victim, wield, null, TO_ROOM);
                act("You are shocked by $p.", victim, wield, null, TO_CHAR);
                shock_effect(victim, wield.level / 2, dam, TARGET_CHAR);
                damage(ch, victim, dam, null, DAM_LIGHTNING, false);
            }
        }

        tail_chain();
    }

/*
* Inflict damage from a hit.
*/

    static boolean damage(CHAR_DATA ch, CHAR_DATA victim, int dam, Skill dt, int dam_type, boolean show) {
        OBJ_DATA corpse;
        boolean immune;
        int lost_exp;

        if (victim.position == POS_DEAD) {
            return false;
        }

        /*
        * Stop up any residual loopholes.
        */
        if (dam > 1000 && !IS_IMMORTAL(ch)) {
            TextBuffer buf = new TextBuffer();
            buf.sprintf("%s:Damage more than 1000 points :%d", ch.name, dam);
            bug(buf);
            if (IS_NPC(ch) && !IS_NPC(ch)) {
                dam = 1000;
            }

/*
 *  For a 100-leveled MUD?....

    dam = 1000;
    if (!IS_IMMORTAL(ch))
    {
        OBJ_DATA obj;
        obj = get_wield_char( ch );
        send_to_char("You really shouldn't cheat.\n",ch);
        if (obj)
          extract_obj(obj);
    }
*/
        }


        if (victim != ch) {
            /*
            * Certain attacks are forbidden.
            * Most other attacks are returned.
            */
            if (is_safe(ch, victim)) {
                return false;
            }

            if (victim.position > POS_STUNNED) {
                if (victim.fighting == null) {
                    set_fighting(victim, ch);
                }
                if (victim.timer <= 4) {
                    victim.position = POS_FIGHTING;
                }
            }

            if (victim.position > POS_STUNNED) {
                if (ch.fighting == null) {
                    set_fighting(ch, victim);
                }

                /*
                * If victim is charmed, ch might attack victim's master.
                */
                if (IS_NPC(ch)
                        && IS_NPC(victim)
                        && IS_AFFECTED(victim, AFF_CHARM)
                        && victim.master != null
                        && victim.master.in_room == ch.in_room
                        && number_bits(3) == 0) {
                    stop_fighting(ch, false);
                    multi_hit(ch, victim.master, null);
                    return false;
                }
            }

            /*
            * More charm and group stuff.
            */
            if (victim.master == ch) {
                stop_follower(victim);
            }

            if (MOUNTED(victim) == ch || RIDDEN(victim) == ch) {
                victim.riding = ch.riding = false;
            }
        }

        /*
        * No one in combat can sneak, hide, or be invis or camoed.
        */
        if (IS_SET(ch.affected_by, AFF_HIDE)
                || IS_SET(ch.affected_by, AFF_INVISIBLE)
                || IS_SET(ch.affected_by, AFF_SNEAK)
                || IS_SET(ch.affected_by, AFF_FADE)
                || IS_SET(ch.affected_by, AFF_CAMOUFLAGE)
                || IS_SET(ch.affected_by, AFF_IMP_INVIS)
                || IS_AFFECTED(ch, AFF_EARTHFADE)) {
            do_visible(ch, "");
        }

        /*
        * Damage modifiers.
        */
        if (IS_AFFECTED(victim, AFF_SANCTUARY) && !((dt == gsn_cleave) && (number_percent() < 50))) {
            dam /= 2;
        } else if (IS_AFFECTED(victim, AFF_PROTECTOR)) {
            dam = (3 * dam) / 5;
        }

        if (IS_AFFECTED(victim, AFF_PROTECT_EVIL) && IS_EVIL(ch)) {
            dam -= dam / 4;
        }

        if (IS_AFFECTED(victim, AFF_PROTECT_GOOD) && IS_GOOD(ch)) {
            dam -= dam / 4;
        }

        if (IS_AFFECTED(victim, AFF_AURA_CHAOS)
                && victim.cabal == CABAL_CHAOS
                && (!IS_NPC(victim) && IS_SET(victim.act, PLR_WANTED))) {
            dam -= dam / 4;
        }

        if (is_affected(victim, gsn_protection_heat) &&
                (dam_type == DAM_FIRE)) {
            dam -= dam / 4;
        }

        if (is_affected(victim, gsn_protection_cold) &&
                (dam_type == DAM_COLD)) {
            dam -= dam / 4;
        }

        immune = false;

        if (dt != null && dt.ordinal() < gsn_x_hit.ordinal()) {
            if (IS_AFFECTED(victim, AFF_ABSORB)
                    && dt.target == TAR_CHAR_OFFENSIVE
                    && dt.is_spell
                    && ch != victim
                    && (number_percent() < 2 * get_skill(victim, gsn_absorb) / 3)
                    /* update.c damages */
                    && dt != gsn_poison
                    && dt != gsn_plague
                    && dt != gsn_witch_curse
                    /* update.c damages */
                    && dt != gsn_mental_knife
                    && dt != gsn_lightning_breath) {
                act("Your spell fails to pass $N's energy field!", ch, null, victim, TO_CHAR);
                act("You absorb $n's spell!", ch, null, victim, TO_VICT);
                act("$N absorbs $n's spell!", ch, null, victim, TO_NOTVICT);
                check_improve(victim, gsn_absorb, true, 1);
                victim.mana += dt.min_mana;
                return false;
            }
            if (IS_AFFECTED(victim, AFF_SPELLBANE)
                    && dt.target != TAR_IGNORE
                    && dt.is_spell
                    && (number_percent() < 2 * get_skill(victim, gsn_spellbane) / 3)
                    /* update.c damages */
                    && dt != gsn_poison
                    && dt != gsn_plague
                    && dt != gsn_witch_curse
                    /* spellbane passing spell damages */
                    && dt != gsn_mental_knife
                    && dt != gsn_lightning_breath) {
                act("$N deflects your spell!", ch, null, victim, TO_CHAR);
                act("You deflect $n's spell!", ch, null, victim, TO_VICT);
                act("$N deflects $n's spell!", ch, null, victim, TO_NOTVICT);
                check_improve(victim, gsn_spellbane, true, 1);
                damage(victim, ch, 3 * victim.level, gsn_spellbane, DAM_NEGATIVE, true);
                return false;
            }
        }

        /*
        * Check for parry, and dodge.
        */
        if (dt == gsn_x_hit && ch != victim) {
            /*
            * Some funny stuf.
            */
            if (is_affected(victim, gsn_mirror)) {
                act("$n shatters into tiny fragments of glass.", victim, null, null, TO_ROOM);
                extract_char(victim, true);
                return false;
            }

            if (check_parry(ch, victim)) {
                return false;
            }
            if (check_cross(ch, victim)) {
                return false;
            }
            if (check_block(ch, victim)) {
                return false;
            }
            if (check_dodge(ch, victim)) {
                return false;
            }
            if (check_hand(ch, victim)) {
                return false;
            }
            if (check_blink(ch, victim)) {
                return false;
            }
        }

        switch (check_immune(victim, dam_type)) {
            case (IS_IMMUNE):
                immune = true;
                dam = 0;
                break;
            case (IS_RESISTANT):
                dam -= dam / 3;
                break;
            case (IS_VULNERABLE):
                dam += dam / 2;
                break;
        }

        if (dt == gsn_x_hit && ch != victim) {
            dam = critical_strike(ch, victim, dam);
            dam = ground_strike(ch, victim, dam);
        }

        if (show) {
            dam_message(ch, victim, dam, dt, immune, dam_type);
        }

        if (dam == 0) {
            return false;
        }

/* temporarily second wield doesn't inflict damage */

        if (dt == gsn_x_hit && ch != victim) {
            check_weapon_destroy(ch, victim, false);
        }

        /*
        * Hurt the victim.
        * Inform the victim of his new state.
        * make sure that negative overflow doesn't happen!
        */
        if (dam < 0 || dam > (victim.hit + 16)) {
            victim.hit = -16;
        } else {
            victim.hit -= dam;
        }

        if (!IS_NPC(victim)
                && victim.level >= LEVEL_IMMORTAL
                && victim.hit < 1) {
            victim.hit = 1;
        }

        update_pos(victim);

        switch (victim.position) {
            case POS_MORTAL:
                if (dam_type == DAM_HUNGER || dam_type == DAM_THIRST) {
                    break;
                }
                act("$n is mortally wounded, and will die soon, if not aided.",
                        victim, null, null, TO_ROOM);
                send_to_char(
                        "You are mortally wounded, and will die soon, if not aided.\n",
                        victim);
                break;

            case POS_INCAP:
                if (dam_type == DAM_HUNGER || dam_type == DAM_THIRST) {
                    break;
                }
                act("$n is incapacitated and will slowly die, if not aided.",
                        victim, null, null, TO_ROOM);
                send_to_char(
                        "You are incapacitated and will slowly die, if not aided.\n",
                        victim);
                break;

            case POS_STUNNED:
                if (dam_type == DAM_HUNGER || dam_type == DAM_THIRST) {
                    break;
                }
                act("$n is stunned, but will probably recover.",
                        victim, null, null, TO_ROOM);
                send_to_char("You are stunned, but will probably recover.\n",
                        victim);
                break;

            case POS_DEAD:
                act("$n is DEAD!!", victim, 0, 0, TO_ROOM);
                send_to_char("You have been KILLED!!\r\n\n", victim);
                break;

            default:
                if (dam_type == DAM_HUNGER || dam_type == DAM_THIRST) {
                    break;
                }
                if (dam > victim.max_hit / 4) {
                    send_to_char("That really did HURT!\n", victim);
                }
                if (victim.hit < victim.max_hit / 4) {
                    send_to_char("You sure are BLEEDING!\n", victim);
                }
                break;
        }

        /*
        * Sleep spells and extremely wounded folks.
        */
        if (!IS_AWAKE(victim)) {
            stop_fighting(victim, false);
        }

        /*
        * Payoff for killing things.
        */
        if (victim.position == POS_DEAD) {
            group_gain(ch, victim);

            if (!IS_NPC(victim)) {
                /*
                * Dying penalty:
                * 2/3 way back.
                */

                if (victim == ch || (IS_NPC(ch) && (ch.master == null &&
                        ch.leader == null)) || IS_SET(victim.act, PLR_WANTED)) {
                    if (victim.exp > exp_per_level(victim, victim.pcdata.points)
                            * victim.level) {
                        lost_exp = (2 * (exp_per_level(victim, victim.pcdata.points)
                                * victim.level - victim.exp) / 3) + 50;
                        gain_exp(victim, lost_exp);
                    }
                }

                /*
                *  Die too much and deleted ... :(
                */
                if (!IS_NPC(victim) && (victim == ch || (IS_NPC(ch) &&
                        (ch.master == null && ch.leader == null))
                        || IS_SET(victim.act, PLR_WANTED))) {
                    victim.pcdata.death++;
                    if (victim.clazz == Clazz.SAMURAI) {
                        if ((victim.pcdata.death % 3) == 2) {
                            victim.perm_stat[STAT_CHA]--;
                            if (victim.pcdata.death > 10) {

                                send_to_char("You became a ghost permanently and leave the earth realm.\n", victim);
                                act("$n is dead, and will not rise again.\n", victim, null, null, TO_ROOM);
                                victim.last_fight_time = -1;
                                victim.hit = 1;
                                victim.position = POS_STANDING;
                                String strsave = nw_config.lib_player_dir + "/" + capitalize(victim.name);
                                wiznet("$N is deleted due to 10 deaths limit of Samurai.", ch, null, 0, 0, 0);
                                do_quit_count(victim, "");
                                new File(strsave).delete();
                                return true;
                            }
                        }
                    } else if ((victim.pcdata.death % 3) == 2) {
                        victim.perm_stat[STAT_CON]--;
                        if (victim.perm_stat[STAT_CON] < 3) {
                            send_to_char("You became a ghost permanently and leave the earth realm.\n", victim);
                            act("$n is dead, and will not rise again.\n", victim, null, null, TO_ROOM);
                            victim.last_fight_time = -1;
                            victim.hit = 1;
                            victim.position = POS_STANDING;
                            String strsave = nw_config.lib_player_dir + "/" + capitalize(victim.name);
                            wiznet("$N is deleted due to lack of CON.", ch, null, 0, 0, 0);
                            do_quit_count(victim, "");
                            new File(strsave).delete();
                            return true;
                        } else {
                            send_to_char("You feel your life power has decreased with this death.\n", victim);
                        }
                    }
                }
            }

            raw_kill(victim);

            /* don't remember killed victims anymore */

            if (IS_NPC(ch)) {
                if (ch.pIndexData.vnum == MOB_VNUM_STALKER) {
                    ch.status = 10;
                }
                remove_mind(ch, victim.name);
                if (IS_SET(ch.act, ACT_HUNTER) && ch.hunting == victim) {
                    ch.hunting = null;
                    ch.act = REMOVE_BIT(ch.act, ACT_HUNTER);
                }
            }

            /* RT new auto commands */

            if (!IS_NPC(ch) && IS_NPC(victim)) {
                corpse = get_obj_list(ch, "corpse", ch.in_room.contents);

                if (IS_SET(ch.act, PLR_AUTOLOOT) && corpse != null && corpse.contains != null) /* exists and not empty */ {
                    do_get(ch, "all corpse");
                }

                if (IS_SET(ch.act, PLR_AUTOGOLD) && corpse != null && corpse.contains != null && !IS_SET(ch.act, PLR_AUTOLOOT)) /* exists and not empty */ {
                    do_get(ch, "gold corpse");
                }
                if (ch.clazz == Clazz.VAMPIRE && ch.level > 10 && corpse != null) {
                    act("{R$n suck blood from $N's corpse!!{x", ch, null, victim, TO_ROOM, POS_SLEEPING);
                    send_to_char("{RYou suck blood from the corpse!!{x\n", ch);
                    gain_condition(ch, COND_BLOODLUST, 3);
                }

                if (IS_SET(ch.act, PLR_AUTOSAC)) {
                    if (IS_SET(ch.act, PLR_AUTOLOOT) && corpse != null && corpse.contains != null) {
                        return true;  /* leave if corpse has treasure */
                    } else {
                        do_sacrifice(ch, "corpse");
                    }
                }
            }

            return true;
        }

        if (victim == ch) {
            return true;
        }

        /*
        * Take care of link dead people.
        */
        if (!IS_NPC(victim) && victim.desc == null) {
            if (number_range(0, victim.wait) == 0) {
                if (victim.level < 11) {
                    do_recall(victim, "");
                } else {
                    do_flee(victim, "");
                }
                return true;
            }
        }

        /*
        * Wimp out?
        */
        if (IS_NPC(victim) && dam > 0 && victim.wait < PULSE_VIOLENCE / 2) {
            if (((IS_SET(victim.act, ACT_WIMPY) && number_bits(2) == 0
                    && victim.hit < victim.max_hit / 5)
                    || (IS_AFFECTED(victim, AFF_CHARM) && victim.master != null
                    && victim.master.in_room != victim.in_room))
                    || (IS_AFFECTED(victim, AFF_FEAR) && !IS_SET(victim.act, ACT_NOTRACK))) {
                do_flee(victim, "");
                victim.last_fought = null;
            }
        }

        if (!IS_NPC(victim)
                && victim.hit > 0
                && (victim.hit <= victim.wimpy || IS_AFFECTED(victim, AFF_FEAR))
                && victim.wait < PULSE_VIOLENCE / 2) {
            do_flee(victim, "");
        }

        tail_chain();
        return true;
    }

    static boolean is_safe(CHAR_DATA ch, CHAR_DATA victim) {
        if (is_safe_nomessage(ch, victim)) {
            act("The gods protect $N.", ch, null, victim, TO_CHAR);
            act("The gods protect $N from $n.", ch, null, victim, TO_ROOM);
            return true;
        } else {
            return false;
        }
    }


    static boolean is_safe_nomessage(CHAR_DATA ch, CHAR_DATA victim) {
        if (victim.fighting == ch || ch == victim) {
            return false;
        }

        /* Ghosts are safe */
        if ((!IS_NPC(victim) && IS_SET(victim.act, PLR_GHOST)) ||
                (!IS_NPC(ch) && IS_SET(ch.act, PLR_GHOST))) {
            return true;
        }

        /* link dead players whose adrenalin is not gushing are safe */
        if (!IS_NPC(victim) && ((victim.last_fight_time == -1) ||
                ((current_time - victim.last_fight_time) > FIGHT_DELAY_TIME)) &&
                victim.desc == null) {
            return true;
        }

        if ((!IS_NPC(ch) && !IS_NPC(victim) && victim.level < 5) ||
                (!IS_NPC(ch) && !IS_NPC(victim) && ch.level < 5)) {
            return true;
        }

        /* newly death staff */
        if (!IS_IMMORTAL(ch) && !IS_NPC(victim) &&
                ((ch.last_death_time != -1 && current_time - ch.last_death_time < 600)
                        || (victim.last_death_time != -1 &&
                        current_time - victim.last_death_time < 600))) {
            return true;
        }

        /* level adjustement */
        return !IS_IMMORTAL(ch) && !IS_NPC(ch) && !IS_NPC(victim) &&
                (ch.level >= (victim.level + UMAX(4, ch.level / 10 + 2))
                        || ch.level <= (victim.level - UMAX(4, ch.level / 10 + 2))) &&
                (victim.level >= (ch.level + UMAX(4, victim.level / 10 + 2))
                        || victim.level <= (ch.level - UMAX(4, victim.level / 10 + 2)));

    }


    static boolean is_safe_spell(CHAR_DATA ch, CHAR_DATA victim, boolean area) {

        if (ch == victim && !area) {
            return true;
        }

        if (IS_IMMORTAL(victim) && area) {
            return true;
        }

        if (is_same_group(ch, victim) && area) {
            return true;
        }

        if (ch == victim && area && ch.in_room.sector_type == SECT_INSIDE) {
            return true;
        }

        if ((RIDDEN(ch) == victim || MOUNTED(ch) == victim) && area) {
            return true;
        }

        return is_safe(ch, victim);
    }

/*
* Check for parry.
*/

    static boolean check_parry(CHAR_DATA ch, CHAR_DATA victim) {
        int chance;

        if (!IS_AWAKE(victim)) {
            return false;
        }

        if (get_wield_char(victim, false) == null) {
            return false;
        }

        if (IS_NPC(victim)) {
            chance = UMIN(40, victim.level);
        } else {
            chance = get_skill(victim, gsn_parry) / 2;
            if (victim.clazz == Clazz.WARRIOR || victim.clazz == Clazz.SAMURAI) {
                chance *= 1.2;
            }
        }


        if (number_percent() >= chance + victim.level - ch.level) {
            return false;
        }

        act("You parry $n's attack.", ch, null, victim, TO_VICT);
        act("$N parries your attack.", ch, null, victim, TO_CHAR);
        check_weapon_destroyed(ch, victim, false);
        if (number_percent() > get_skill(victim, gsn_parry)) {
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
            if (number_percent() < (chance / 20)) {
                act("You couldn't manage to keep your position!",
                        ch, null, victim, TO_VICT);
                act("You fall down!", ch, null, victim, TO_VICT);
                act("$N couldn't manage to hold your attack and falls down!",
                        ch, null, victim, TO_CHAR);
                act("$n stunning force makes $N falling down.",
                        ch, null, victim, TO_NOTVICT);

                WAIT_STATE(victim, gsn_bash.beats);
                victim.position = POS_RESTING;
            }
        }
        check_improve(victim, gsn_parry, true, 6);
        return true;
    }

/*
 * check blink
 */

    static boolean check_blink(CHAR_DATA ch, CHAR_DATA victim) {
        int chance;

        if (!IS_BLINK_ON(victim)) {
            return false;
        }

        if (IS_NPC(victim)) {
            return false;
        } else {
            chance = victim.pcdata.learned[gsn_blink.ordinal()] / 2;
        }

        if ((number_percent() >= chance + victim.level - ch.level)
                || (number_percent() < 50)
                || (victim.mana < 10)) {
            return false;
        }

        victim.mana -= UMAX(victim.level / 10, 1);

        act("You blink out $n's attack.", ch, null, victim, TO_VICT);
        act("$N blinks out your attack.", ch, null, victim, TO_CHAR);
        check_improve(victim, gsn_blink, true, 6);
        return true;
    }

/*
* Check for shield block.
*/

    static boolean check_block(CHAR_DATA ch, CHAR_DATA victim) {
        int chance;

        if (!IS_AWAKE(victim)) {
            return false;
        }

        if (get_shield_char(victim) == null) {
            return false;
        }

        if (IS_NPC(victim)) {
            chance = 10;
        } else {
            if (get_skill(victim, gsn_shield_block) <= 1) {
                return false;
            }
            chance = get_skill(victim, gsn_shield_block) / 2;
            chance -= (victim.clazz == Clazz.WARRIOR) ? 0 : 10;
        }

        if (number_percent() >= chance + victim.level - ch.level) {
            return false;
        }

        act("Your shield blocks $n's attack.", ch, null, victim, TO_VICT);
        act("$N deflects your attack with $S shield.", ch, null, victim, TO_CHAR);
        check_shield_destroyed(ch, victim, false);
        check_improve(victim, gsn_shield_block, true, 6);
        return true;
    }

/*
* Check for dodge.
*/

    static boolean check_dodge(CHAR_DATA ch, CHAR_DATA victim) {
        int chance;

        if (!IS_AWAKE(victim)) {
            return false;
        }

        if (MOUNTED(victim) != null) {
            return false;
        }

        if (IS_NPC(victim)) {
            chance = UMIN(30, victim.level);
        } else {
            chance = get_skill(victim, gsn_dodge) / 2;
            /* chance for high dex. */
            chance += 2 * (get_curr_stat(victim, STAT_DEX) - 20);
            if (victim.clazz == Clazz.WARRIOR || victim.clazz == Clazz.SAMURAI) {
                chance *= 1.2;
            } else if (victim.clazz == Clazz.THIEF || victim.clazz == Clazz.NINJA) {
                chance *= 1.1;
            }
        }

        if (number_percent() >= chance + (victim.level - ch.level) / 2) {
            return false;
        }

        act("You dodge $n's attack.", ch, null, victim, TO_VICT);
        act("$N dodges your attack.", ch, null, victim, TO_CHAR);
        if (number_percent() < (get_skill(victim, gsn_dodge) / 20)
                && !(IS_AFFECTED(ch, AFF_FLYING) || ch.position < POS_FIGHTING)) {
            /* size */
            if (victim.size < ch.size) {
                chance += (victim.size - ch.size) * 10;  /* bigger = harder to trip */
            }

            /* dex */
            chance += get_curr_stat(victim, STAT_DEX);
            chance -= get_curr_stat(ch, STAT_DEX) * 3 / 2;

            if (IS_AFFECTED(victim, AFF_FLYING)) {
                chance -= 10;
            }

            /* speed */
            if (IS_SET(victim.off_flags, OFF_FAST) || IS_AFFECTED(victim, AFF_HASTE)) {
                chance += 10;
            }
            if (IS_SET(ch.off_flags, OFF_FAST) || IS_AFFECTED(ch, AFF_HASTE)) {
                chance -= 20;
            }

            /* level */
            chance += (victim.level - ch.level) * 2;

            /* now the attack */
            if (number_percent() < (chance / 20)) {
                act("$n lost his postion and fall down!", ch, null, victim, TO_VICT);
                act("As $N moves you lost your position fall down!", ch, null, victim, TO_CHAR);
                act("As $N dodges $N's attack ,$N lost his position and falls down.", ch, null, victim, TO_NOTVICT);

                WAIT_STATE(ch, gsn_trip.beats);
                ch.position = POS_RESTING;
            }
        }
        check_improve(victim, gsn_dodge, true, 6);
        return true;
    }

/*
* Check for cross.
*/

    static boolean check_cross(CHAR_DATA ch, CHAR_DATA victim) {
        int chance;

        if (!IS_AWAKE(victim)) {
            return false;
        }

        if (get_wield_char(victim, false) == null ||
                get_wield_char(victim, true) == null) {
            return false;
        }

        if (IS_NPC(victim)) {
            chance = UMIN(35, victim.level);
        } else {
            chance = get_skill(victim, gsn_cross_block) / 3;
            if (victim.clazz == Clazz.WARRIOR || victim.clazz == Clazz.SAMURAI) {
                chance *= 1.2;
            }
        }


        if (number_percent() >= chance + victim.level - ch.level) {
            return false;
        }

        act("Your cross blocks $n's attack.", ch, null, victim, TO_VICT);
        act("$N's cross blocks your attack.", ch, null, victim, TO_CHAR);
        check_weapon_destroyed(ch, victim, false);
        if (number_percent() > get_skill(victim, gsn_cross_block)) {
            /* size  and weight */
            chance += ch.carry_weight / 25;
            chance -= victim.carry_weight / 10;

            if (ch.size < victim.size) {
                chance += (ch.size - victim.size) * 25;
            } else {
                chance += (ch.size - victim.size) * 10;
            }

            /* stats */
            chance += get_curr_stat(ch, STAT_STR);
            chance -= get_curr_stat(victim, STAT_DEX) * 5 / 3;

            if (IS_AFFECTED(ch, AFF_FLYING)) {
                chance -= 20;
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
            if (number_percent() < (chance / 20)) {
                act("You couldn't manage to keep your position!",
                        ch, null, victim, TO_VICT);
                act("You fall down!", ch, null, victim, TO_VICT);
                act("$N couldn't manage to hold your attack and falls down!",
                        ch, null, victim, TO_CHAR);
                act("$n stunning force makes $N falling down.",
                        ch, null, victim, TO_NOTVICT);

                WAIT_STATE(victim, gsn_bash.beats);
                victim.position = POS_RESTING;
            }
        }
        check_improve(victim, gsn_cross_block, true, 6);
        return true;
    }

/*
 * Check for hand.
 */

    static boolean check_hand(CHAR_DATA ch, CHAR_DATA victim) {
        int chance;

        if (!IS_AWAKE(victim)) {
            return false;
        }

        if (get_wield_char(victim, false) != null) {
            return false;
        }

        if (IS_NPC(victim)) {
            chance = UMIN(35, victim.level);
        } else {
            chance = get_skill(victim, gsn_hand_block) / 3;
            if (victim.clazz == Clazz.NINJA) {
                chance *= 1.5;
            }
        }


        if (number_percent() >= chance + victim.level - ch.level) {
            return false;
        }

        act("Your hands block $n's attack.", ch, null, victim, TO_VICT);
        act("$N's hands block your attack.", ch, null, victim, TO_CHAR);
        check_improve(victim, gsn_hand_block, true, 6);
        return true;
    }

/*
* Set position of a victim.
*/

    static void update_pos(CHAR_DATA victim) {
        if (victim.hit > 0) {
            if (victim.position <= POS_STUNNED) {
                victim.position = POS_STANDING;
            }
            return;
        }

        if (IS_NPC(victim) && victim.hit < 1) {
            victim.position = POS_DEAD;
            return;
        }

        if (victim.hit <= -11) {
            victim.position = POS_DEAD;
            return;
        }

        if (victim.hit <= -6) {
            victim.position = POS_MORTAL;
        } else if (victim.hit <= -3) {
            victim.position = POS_INCAP;
        } else {
            victim.position = POS_STUNNED;
        }

    }

/*
* Start fights.
*/

    static void set_fighting(CHAR_DATA ch, CHAR_DATA victim) {
        if (ch.fighting != null) {
            bug("Set_fighting: already fighting");
            return;
        }

        if (IS_AFFECTED(ch, AFF_SLEEP)) {
            affect_strip(ch, gsn_sleep);
        }

        ch.fighting = victim;
        ch.position = POS_FIGHTING;

    }

/*
* Stop fights.
*/

    static void stop_fighting(CHAR_DATA ch, boolean fBoth) {
        CHAR_DATA fch;

        for (fch = char_list; fch != null; fch = fch.next) {
            if (fch == ch || (fBoth && fch.fighting == ch)) {
                fch.fighting = null;
                fch.position = IS_NPC(fch) ? ch.default_pos : POS_STANDING;
                update_pos(fch);
            }
        }

    }

/*
* Make a corpse out of a character.
*/

    static void make_corpse(CHAR_DATA ch) {
        OBJ_DATA corpse;
        OBJ_DATA obj;
        OBJ_DATA obj_next;
        String name;
        int i;

        if (IS_NPC(ch)) {
            name = ch.short_descr;
            corpse = create_object(get_obj_index(OBJ_VNUM_CORPSE_NPC), 0);
            corpse.timer = number_range(3, 6);
            if (ch.gold > 0 || ch.silver > 0) {
                if (IS_SET(ch.form, FORM_INSTANT_DECAY)) {
                    obj_to_room(create_money(ch.gold, ch.silver), ch.in_room);
                } else {
                    obj_to_obj(create_money(ch.gold, ch.silver), corpse);
                }
                ch.gold = 0;
            }
            corpse.from = ch.short_descr;
            corpse.cost = 0;
        } else {
            if (IS_GOOD(ch)) {
                i = 0;
            } else if (IS_EVIL(ch)) {
                i = 2;
            } else {
                i = 1;
            }

            name = ch.name;
            corpse = create_object(get_obj_index(OBJ_VNUM_CORPSE_PC), 0);
            corpse.timer = number_range(25, 40);
            ch.act = REMOVE_BIT(ch.act, PLR_CANLOOT);
            corpse.owner = ch.name;
            corpse.from = ch.name;
            corpse.altar = hometown_table[ch.hometown].altar[i];
            corpse.pit = hometown_table[ch.hometown].pit[i];

            if (ch.gold > 0 || ch.silver > 0) {
                obj_to_obj(create_money(ch.gold, ch.silver), corpse);
                ch.gold = 0;
                ch.silver = 0;
            }
            corpse.cost = 0;
        }

        corpse.level = ch.level;

        TextBuffer buf = new TextBuffer();
        buf.sprintf(corpse.short_descr, name);
        corpse.short_descr = buf.toString();

        buf.sprintf(corpse.description, name);
        corpse.description = buf.toString();

        for (obj = ch.carrying; obj != null; obj = obj_next) {
            obj_next = obj.next_content;
            obj_from_char(obj);
            if (obj.item_type == ITEM_POTION) {
                obj.timer = number_range(500, 1000);
            }
            if (obj.item_type == ITEM_SCROLL) {
                obj.timer = number_range(1000, 2500);
            }
            if (IS_SET(obj.extra_flags, ITEM_ROT_DEATH)) {
                obj.timer = number_range(5, 10);
                if (obj.item_type == ITEM_POTION) {
                    obj.timer += obj.level * 20;
                }
            }
            obj.extra_flags = REMOVE_BIT(obj.extra_flags, ITEM_VIS_DEATH);
            obj.extra_flags = REMOVE_BIT(obj.extra_flags, ITEM_ROT_DEATH);

            if (IS_SET(obj.extra_flags, ITEM_INVENTORY) ||
                    (obj.pIndexData.limit != -1 &&
                            (obj.pIndexData.count > obj.pIndexData.limit))) {
                extract_obj(obj);
            } else if (IS_SET(ch.form, FORM_INSTANT_DECAY)) {
                obj_to_room(obj, ch.in_room);
            } else {
                obj_to_obj(obj, corpse);
            }
        }

        obj_to_room(corpse, ch.in_room);
    }


    static void death_cry(CHAR_DATA ch) {
        death_cry_org(ch, -1);
    }

/*
 * Improved Death_cry contributed by Diavolo.
 */

    static void death_cry_org(CHAR_DATA ch, int part) {
        ROOM_INDEX_DATA was_in_room;
        String msg;
        int door;
        int vnum;

        vnum = 0;
        msg = "You hear $n's death cry.";

        if (part == -1) {
            part = number_bits(4);
        }

        switch (part) {

            case 0:
                msg = "$n hits the ground ... DEAD.";
                break;
            case 1:
                if (ch.material == null) {
                    msg = "$n splatters blood on your armor.";
                    break;
                }
            case 2:
                if (IS_SET(ch.parts, PART_GUTS)) {
                    msg = "$n spills $s guts all over the floor.";
                    vnum = OBJ_VNUM_GUTS;
                }
                break;
            case 3:
                if (IS_SET(ch.parts, PART_HEAD)) {
                    msg = "$n's severed head plops on the ground.";
                    vnum = OBJ_VNUM_SEVERED_HEAD;
                }
                break;
            case 4:
                if (IS_SET(ch.parts, PART_HEART)) {
                    msg = "$n's heart is torn from $s chest.";
                    vnum = OBJ_VNUM_TORN_HEART;
                }
                break;
            case 5:
                if (IS_SET(ch.parts, PART_ARMS)) {
                    msg = "$n's arm is sliced from $s dead body.";
                    vnum = OBJ_VNUM_SLICED_ARM;
                }
                break;
            case 6:
                if (IS_SET(ch.parts, PART_LEGS)) {
                    msg = "$n's leg is sliced from $s dead body.";
                    vnum = OBJ_VNUM_SLICED_LEG;
                }
                break;
            case 7:
                if (IS_SET(ch.parts, PART_BRAINS)) {
                    msg = "$n's head is shattered, and $s brains splash all over you.";
                    vnum = OBJ_VNUM_BRAINS;
                }
        }

        act(msg, ch, null, null, TO_ROOM);

        if (vnum != 0) {
            OBJ_DATA obj;
            String name;

            name = IS_NPC(ch) ? ch.short_descr : ch.name;
            obj = create_object(get_obj_index(vnum), 0);
            obj.timer = number_range(4, 7);

            TextBuffer buf = new TextBuffer();
            buf.sprintf(obj.short_descr, name);
            obj.short_descr = buf.toString();

            buf.sprintf(obj.description, name);
            obj.description = buf.toString();

            obj.from = name;

            if (obj.item_type == ITEM_FOOD) {
                if (IS_SET(ch.form, FORM_POISON)) {
                    obj.value[3] = 1;
                } else if (!IS_SET(ch.form, FORM_EDIBLE)) {
                    obj.item_type = ITEM_TRASH;
                }
            }

            obj_to_room(obj, ch.in_room);
        }

        if (IS_NPC(ch)) {
            msg = "You hear something's death cry.";
        } else {
            msg = "You hear someone's death cry.";
        }

        was_in_room = ch.in_room;
        for (door = 0; door <= 5; door++) {
            EXIT_DATA pexit;

            if ((pexit = was_in_room.exit[door]) != null
                    && pexit.to_room != null
                    && pexit.to_room != was_in_room) {
                ch.in_room = pexit.to_room;
                act(msg, ch, null, null, TO_ROOM);
            }
        }
        ch.in_room = was_in_room;

    }


    static void raw_kill(CHAR_DATA victim) {
        raw_kill_org(victim, -1);
    }

    static void raw_kill_org(CHAR_DATA victim, int part) {

        CHAR_DATA tmp_ch;
        OBJ_DATA obj, obj_next;
        int i;
        OBJ_DATA tattoo;

        stop_fighting(victim, true);

        for (obj = victim.carrying; obj != null; obj = obj_next) {
            obj_next = obj.next_content;
            if (IS_SET(obj.progtypes, OPROG_DEATH) && (obj.wear_loc != WEAR_NONE)) {
                if (obj.pIndexData.oprogs.death_prog.run(obj, victim)) {
                    victim.position = POS_STANDING;
                    return;
                }
            }
        }
        victim.last_fight_time = -1;
        if (IS_SET(victim.progtypes, MPROG_DEATH)) {
            if (victim.pIndexData.mprogs.death_prog.run(victim)) {
                victim.position = POS_STANDING;
                return;
            }
        }

        victim.last_death_time = current_time;

        tattoo = get_eq_char(victim, WEAR_TATTOO);
        if (tattoo != null) {
            obj_from_char(tattoo);
        }

        death_cry_org(victim, part);
        make_corpse(victim);


        if (IS_NPC(victim)) {
            victim.pIndexData.killed++;
            kill_table[URANGE(0, victim.level, MAX_LEVEL - 1)].killed++;
            extract_char(victim, true);
            return;
        }

        send_to_char("You turn into an invincible ghost for a few minutes.\n",
                victim);
        send_to_char("As long as you don't attack anything.\n", victim);

        extract_char(victim, false);

        while (victim.affected != null) {
            affect_remove(victim, victim.affected);
        }
        victim.affected_by = 0;
        for (i = 0; i < 4; i++) {
            victim.armor[i] = 100;
        }
        victim.position = POS_RESTING;
        victim.hit = victim.max_hit / 10;
        victim.mana = victim.max_mana / 10;
        victim.move = victim.max_move;

        /* RT added to prevent infinite deaths */
        victim.act = REMOVE_BIT(victim.act, PLR_WANTED);
        victim.act = REMOVE_BIT(victim.act, PLR_BOUGHT_PET);
/*  SET_BIT(victim.act, PLR_GHOST);    */

        victim.pcdata.condition[COND_THIRST] = 40;
        victim.pcdata.condition[COND_HUNGER] = 40;
        victim.pcdata.condition[COND_FULL] = 40;
        victim.pcdata.condition[COND_BLOODLUST] = 40;
        victim.pcdata.condition[COND_DESIRE] = 40;

        if (tattoo != null) {
            obj_to_char(tattoo, victim);
            equip_char(victim, tattoo, WEAR_TATTOO);
        }
        save_char_obj(victim);

        /*
        * Calm down the tracking mobiles
        */
        for (tmp_ch = char_list; tmp_ch != null; tmp_ch = tmp_ch.next) {
            if (tmp_ch.last_fought == victim) {
                tmp_ch.last_fought = null;
            }
        }

    }


    static void group_gain(CHAR_DATA ch, CHAR_DATA victim) {
        CHAR_DATA gch;
        CHAR_DATA lch;
        int xp;
        int members;
        int group_levels;

        if (victim == ch
                || (IS_NPC(victim) && victim.pIndexData.vnum < 100)) {
            return;
        }

/* quest */

        if (IS_GOLEM(ch) && ch.master != null
                && ch.master.clazz == Clazz.NECROMANCER) {
            gch = ch.master;
        } else {
            gch = ch;
        }
        if (!IS_NPC(gch) && IS_QUESTOR(gch) && IS_NPC(victim)) {
            if (gch.pcdata.questmob == victim.pIndexData.vnum) {
                send_to_char("You have almost completed your QUEST!\n", gch);
                send_to_char("Return to questmaster before your time runs out!\n", gch);
                gch.pcdata.questmob = -1;
            }
        }
/* end quest */

        if (!IS_NPC(victim)) {
            return;
        }

        if (IS_NPC(victim) && (victim.master != null || victim.leader != null)) {
            return;
        }

        members = 1;
        group_levels = 0;
        for (gch = ch.in_room.people; gch != null; gch = gch.next_in_room) {
            if (is_same_group(gch, ch)) {
                if (!IS_NPC(gch) && gch != ch) {
                    members++;
                }
                group_levels += gch.level;
            }
        }

        lch = (ch.leader != null) ? ch.leader : ch;

        for (gch = ch.in_room.people; gch != null; gch = gch.next_in_room) {
            OBJ_DATA obj;
            OBJ_DATA obj_next;

            if (!is_same_group(gch, ch) || IS_NPC(gch)) {
                continue;
            }


            if (gch.level - lch.level > 8) {
                send_to_char("You are too high for this group.\n", gch);
                continue;
            }

            if (gch.level - lch.level < -8) {
                send_to_char("You are too low for this group.\n", gch);
                continue;
            }


            xp = xp_compute(gch, victim, group_levels, members);
            TextBuffer buf = new TextBuffer();
            buf.sprintf("You receive %d experience points.\n", xp);
            send_to_char(buf, gch);
            gain_exp(gch, xp);

            for (obj = ch.carrying; obj != null; obj = obj_next) {
                obj_next = obj.next_content;
                if (obj.wear_loc == WEAR_NONE) {
                    continue;
                }

                if ((IS_OBJ_STAT(obj, ITEM_ANTI_EVIL) && IS_EVIL(ch))
                        || (IS_OBJ_STAT(obj, ITEM_ANTI_GOOD) && IS_GOOD(ch))
                        || (IS_OBJ_STAT(obj, ITEM_ANTI_NEUTRAL) && IS_NEUTRAL(ch))) {
                    act("You are zapped by $p.", ch, obj, null, TO_CHAR);
                    act("$n is zapped by $p.", ch, obj, null, TO_ROOM);
                    obj_from_char(obj);
                    obj_to_room(obj, ch.in_room);
                }
            }
        }

    }

/*
* Compute xp for a kill.
* Also adjust alignment of killer.
* Edit this function to change xp computations.
*/

    static int xp_compute(CHAR_DATA gch, CHAR_DATA victim, int total_levels, int members) {
        int xp;
        int base_exp;
        int level_range;
        int neg_cha = 0, pos_cha = 0;

        level_range = victim.level - gch.level;

        switch (level_range) {
            default:
                base_exp = 0;
                break;
            case -9:
                base_exp = 1;
                break;
            case -8:
                base_exp = 2;
                break;
            case -7:
                base_exp = 5;
                break;
            case -6:
                base_exp = 9;
                break;
            case -5:
                base_exp = 11;
                break;
            case -4:
                base_exp = 22;
                break;
            case -3:
                base_exp = 33;
                break;
            case -2:
                base_exp = 43;
                break;
            case -1:
                base_exp = 60;
                break;
            case 0:
                base_exp = 74;
                break;
            case 1:
                base_exp = 84;
                break;
            case 2:
                base_exp = 99;
                break;
            case 3:
                base_exp = 121;
                break;
            case 4:
                base_exp = 143;
                break;
        }

        if (level_range > 4) {
            base_exp = 140 + 20 * (level_range - 4);
        }

        /* calculate exp multiplier */
        if (IS_SET(victim.act, ACT_NOALIGN)) {
            xp = base_exp;
        }

        /* alignment */
        else if ((IS_EVIL(gch) && IS_GOOD(victim)) || (IS_EVIL(victim) && IS_GOOD(gch))) {
            xp = base_exp * 8 / 5;
        } else if (IS_GOOD(gch) && IS_GOOD(victim)) {
            xp = 0;
        } else if (!IS_NEUTRAL(gch) && IS_NEUTRAL(victim)) {
            xp = (int) (base_exp * 1.1);
        } else if (IS_NEUTRAL(gch) && !IS_NEUTRAL(victim)) {
            xp = (int) (base_exp * 1.3);
        } else {
            xp = base_exp;
        }

        /* more exp at the low levels */
        if (gch.level < 6) {
            xp = 15 * xp / (gch.level + 4);
        }

        /* randomize the rewards */
        xp = number_range(xp * 3 / 4, xp * 5 / 4);

        /* adjust for grouping */
        xp = xp * gch.level / total_levels;

        if (members == 2 || members == 3) {
            xp *= (3 / 2);
        }

        if (gch.level < 15) {
            xp = UMIN((250 + dice(1, 25)), xp);
        } else if (gch.level < 40) {
            xp = UMIN((225 + dice(1, 20)), xp);
        } else if (gch.level < 60) {
            xp = UMIN((200 + dice(1, 20)), xp);
        } else {
            xp = UMIN((180 + dice(1, 20)), xp);
        }

        xp += (xp * (gch.max_hit - gch.hit)) / (gch.max_hit * 5);

        if (IS_GOOD(gch)) {
            if (IS_GOOD(victim)) {
                gch.pcdata.anti_killed++;
                neg_cha = 1;
            } else if (IS_NEUTRAL(victim)) {
                gch.pcdata.has_killed++;
                pos_cha = 1;
            } else if (IS_EVIL(victim)) {
                gch.pcdata.has_killed++;
                pos_cha = 1;
            }
        }

        if (IS_NEUTRAL(gch)) {
            if (xp > 0) {
                if (IS_GOOD(victim)) {
                    gch.pcdata.has_killed++;
                    pos_cha = 1;
                } else if (IS_NEUTRAL(victim)) {
                    gch.pcdata.anti_killed++;
                    neg_cha = 1;
                } else if (IS_EVIL(victim)) {
                    gch.pcdata.has_killed++;
                    pos_cha = 1;
                }
            }
        }

        if (IS_EVIL(gch)) {
            if (xp > 0) {
                if (IS_GOOD(victim)) {
                    gch.pcdata.has_killed++;
                    pos_cha = 1;
                } else if (IS_NEUTRAL(victim)) {
                    gch.pcdata.has_killed++;
                    pos_cha = 1;
                } else if (IS_EVIL(victim)) {
                    gch.pcdata.anti_killed++;
                    neg_cha = 1;
                }
            }
        }

        if (neg_cha != 0) {
            if ((gch.pcdata.anti_killed % 100) == 99) {
                TextBuffer buf = new TextBuffer();
                buf.sprintf("You have killed %d %s up to now.\n", gch.pcdata.anti_killed,
                        IS_GOOD(gch) ? "goods" :
                                IS_NEUTRAL(gch) ? "neutrals" :
                                        IS_EVIL(gch) ? "evils" : "nones");
                send_to_char(buf, gch);
                if (gch.perm_stat[STAT_CHA] > 3 && IS_GOOD(gch)) {
                    send_to_char("So your charisma has reduced by one.\n", gch);
                    gch.perm_stat[STAT_CHA] -= 1;
                }
            }
        } else if (pos_cha != 0) {
            if ((gch.pcdata.has_killed % 200) == 199) {
                TextBuffer buf = new TextBuffer();
                buf.sprintf("You have killed %d %s up to now.\n",
                        gch.pcdata.anti_killed,
                        IS_GOOD(gch) ? "anti-goods" :
                                IS_NEUTRAL(gch) ? "anti-neutrals" :
                                        IS_EVIL(gch) ? "anti-evils" : "nones");
                send_to_char(buf, gch);
                if (gch.perm_stat[STAT_CHA] < get_max_train(gch, STAT_CHA)
                        && IS_GOOD(gch)) {
                    send_to_char("So your charisma has increased by one.\n", gch);
                    gch.perm_stat[STAT_CHA] += 1;
                }
            }
        }
        return xp;
    }


    static void dam_message(CHAR_DATA ch, CHAR_DATA victim, int dam, Skill dt, boolean immune, int dam_type) {
        char punct;
        String vp, vs;

        if (dam == 0) {
            vs = "miss";
            vp = "misses";
        } else if (dam <= 4) {
            vs = "{cscratch{x";
            vp = "{cscratches{x";
        } else if (dam <= 8) {
            vs = "{cgraze{x";
            vp = "{cgrazes{x";
        } else if (dam <= 12) {
            vs = "{chit{x";
            vp = "{chits{x";
        } else if (dam <= 16) {
            vs = "{cinjure{x";
            vp = "{cinjures{x";
        } else if (dam <= 20) {
            vs = "{cwound{x";
            vp = "{cwounds{x";
        } else if (dam <= 24) {
            vs = "{cmaul{x";
            vp = "{cmauls{x";
        } else if (dam <= 28) {
            vs = "{cdecimate{x";
            vp = "{cdecimates{x";
        } else if (dam <= 32) {
            vs = "{cdevastate{x";
            vp = "{cdevastates{x";
        } else if (dam <= 36) {
            vs = "{cmaim{x";
            vp = "{cmaims{x";
        } else if (dam <= 42) {
            vs = "{mMUTILATE{x";
            vp = "{mMUTILATES{x";
        } else if (dam <= 52) {
            vs = "{mDISEMBOWEL{x";
            vp = "{mDISEMBOWELS{x";
        } else if (dam <= 65) {
            vs = "{mDISMEMBER{x";
            vp = "{mDISMEMBERS{x";
        } else if (dam <= 80) {
            vs = "{mMASSACRE{x";
            vp = "{mMASSACRES{x";
        } else if (dam <= 100) {
            vs = "{mMANGLE{x";
            vp = "{mMANGLES{x";
        } else if (dam <= 130) {
            vs = "{y*** DEMOLISH ***{x";
            vp = "{y*** DEMOLISHES ***{x";
        } else if (dam <= 175) {
            vs = "{y*** DEVASTATE ***{x";
            vp = "{y*** DEVASTATES ***{x";
        } else if (dam <= 250) {
            vs = "{y=== OBLITERATE ==={x";
            vp = "{y=== OBLITERATES ==={x";
        } else if (dam <= 325) {
            vs = "{y==== ATOMIZE ===={x";
            vp = "{y==== ATOMIZES ===={x";
        } else if (dam <= 400) {
            vs = "{r<*> <*> ANNIHILATE <*> <*>{x";
            vp = "{r<*> <*> ANNIHILATES <*> <*>{x";
        } else if (dam <= 500) {
            vs = "{r<*>!<*> ERADICATE <*>!<*>{x";
            vp = "{r<*>!<*> ERADICATES <*>!<*>{x";
        } else if (dam <= 650) {
            vs = "{r<*><*><*> ELECTRONIZE <*><*><*>{x";
            vp = "{r<*><*><*> ELECTRONIZES <*><*><*>{x";
        } else if (dam <= 800) {
            vs = "{r(<*>)!(<*>) SKELETONIZE (<*>)!(<*>){x";
            vp = "{r(<*>)!(<*>) SKELETONIZES (<*>)!(<*>){x";
        } else if (dam <= 1000) {
            vs = "{r(*)!(*)!(*) NUKE (*)!(*)!(*){x";
            vp = "{r(*)!(*)!(*) NUKES (*)!(*)!(*){x";
        } else if (dam <= 1250) {
            vs = "{r(*)!<*>!(*) TERMINATE (*)!<*>!(*){x";
            vp = "{r(*)!<*>!(*) TERMINATES (*)!<*>!(*){x";
        } else if (dam <= 1500) {
            vs = "{r<*>!(*)!<*>> TEAR UP <<*)!(*)!<*>{x";
            vp = "{r<*>!(*)!<*>> TEARS UP <<*)!(*)!<*>{x";
        } else {
            vs = "{r=<*) (*>= ! POWER HIT ! =<*) (*>={*{x";
            vp = "{r=<*) (*>= ! POWER HITS ! =<*) (*>={*{x";
        }

        if (victim.level < 20) {
            punct = (dam <= 24) ? '.' : '!';
        } else if (victim.level < 50) {
            punct = (dam <= 50) ? '.' : '!';
        } else {
            punct = (dam <= 75) ? '.' : '!';
        }

        TextBuffer buf1 = new TextBuffer();
        TextBuffer buf2 = new TextBuffer();
        TextBuffer buf3 = new TextBuffer();
        if ((dt == gsn_x_hit) || (dt == gsn_x_hunger)) {
            if (ch == victim) {
                if (dam_type == DAM_HUNGER) {
                    sprintf(buf1, "$n's hunger %s $mself%c", vp, punct);
                    sprintf(buf2, "Your hunger %s yourself%c", vs, punct);
                } else if (dam_type == DAM_THIRST) {
                    sprintf(buf1, "$n's thirst %s $mself%c", vp, punct);
                    sprintf(buf2, "Your thirst %s yourself%c", vs, punct);
                } else if (dam_type == DAM_LIGHT_V) {
                    sprintf(buf1, "The light of room %s $n!%c", vp, punct);
                    sprintf(buf2, "The light of room %s you!%c", vs, punct);
                } else if (dam_type == DAM_TRAP_ROOM) {
                    sprintf(buf1, "The trap at room %s $n!%c", vp, punct);
                    sprintf(buf2, "The trap at room %s you!%c", vs, punct);
                } else {
                    sprintf(buf1, "$n %s $mself%c", vp, punct);
                    sprintf(buf2, "You %s yourself%c", vs, punct);
                }
            } else {
                sprintf(buf1, "$n %s $N%c", vp, punct);
                sprintf(buf2, "You %s $N%c", vs, punct);
                sprintf(buf3, "$n %s you%c", vp, punct);
            }
        } else {
            String attack;
            if (dt != null && dt.ordinal() < gsn_x_hit.ordinal()) {
                attack = dt.noun_damage;
            } else if (dt.ordinal() >= gsn_x_hit.ordinal() && dt.ordinal() <= gsn_x_hit.ordinal() + MAX_DAMAGE_MESSAGE) {
                attack = attack_table[dt.ordinal() - gsn_x_hit.ordinal()].noun;
            } else {
                bug("Dam_message: bad dt %d.", dt.ordinal());
                attack = attack_table[0].name;
            }

            if (immune) {
                if (ch == victim) {
                    sprintf(buf1, "$n is unaffected by $s own %s.", attack);
                    sprintf(buf2, "Luckily, you are immune to that.");
                } else {
                    sprintf(buf1, "$N is unaffected by $n's %s!", attack);
                    sprintf(buf2, "$N is unaffected by your %s!", attack);
                    sprintf(buf3, "$n's %s is powerless against you.", attack);
                }
            } else {
                if (ch == victim) {
                    sprintf(buf1, "$n's %s %s $m%c", attack, vp, punct);
                    sprintf(buf2, "Your %s %s you%c", attack, vp, punct);
                } else {
                    sprintf(buf1, "$n's %s %s $N%c", attack, vp, punct);
                    sprintf(buf2, "Your %s %s $N%c", attack, vp, punct);
                    sprintf(buf3, "$n's %s %s you%c", attack, vp, punct);
                }
            }
        }

        if (ch == victim) {
            act(buf1.toString(), ch, null, null, TO_ROOM, POS_RESTING);
            act(buf2.toString(), ch, null, null, TO_CHAR, POS_RESTING);
        } else {
            act(buf1.toString(), ch, null, victim, TO_NOTVICT, POS_RESTING);
            act(buf2.toString(), ch, null, victim, TO_CHAR, POS_RESTING);
            act(buf3.toString(), ch, null, victim, TO_VICT, POS_RESTING);
        }

    }


    static void do_kill(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        OBJ_DATA wield;

        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0) {
            send_to_char("Kill whom?\n", ch);
            return;
        }

        if ((victim = get_char_room(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (is_safe(ch, victim)) {
            return;
        }

        if (ch.position == POS_FIGHTING) {
            if (victim == ch.fighting) {
                send_to_char("You do the best you can!\n", ch);
            } else if (victim.fighting != ch) {
                send_to_char("One battle at a time, please.\n", ch);
            } else {
                act("You start aiming at $N.", ch, null, victim, TO_CHAR);
                ch.fighting = victim;
            }
            return;
        }

        if (!IS_NPC(victim)) {
            send_to_char("You must MURDER a player.\n", ch);
            return;
        }

        if (victim == ch) {
            send_to_char("You hit yourself.  Ouch!\n", ch);
            multi_hit(ch, ch, null);
            return;
        }


        if (IS_AFFECTED(ch, AFF_CHARM) && ch.master == victim) {
            act("$N is your beloved master.", ch, null, victim, TO_CHAR);
            return;
        }


        WAIT_STATE(ch, PULSE_VIOLENCE);

        if (skill_failure_nomessage(ch, gsn_mortal_strike, 0) == 0
                && (wield = get_wield_char(ch, false)) != null
                && wield.level > (victim.level - 5)) {
            int chance = 1 + get_skill(ch, gsn_mortal_strike) / 30;
            chance += (ch.level - victim.level) / 2;
            if (number_percent() < chance) {
                act("{rYour flash strike instantly slays $N!{x", ch, null, victim, TO_CHAR, POS_RESTING);
                act("{r$n flash strike instantly slays $N!{x", ch, null, victim, TO_NOTVICT, POS_RESTING);
                act("{r$n flash strike instantly slays you!{x", ch, null, victim, TO_VICT, POS_DEAD);
                damage(ch, victim, (victim.hit + 1), gsn_mortal_strike, DAM_NONE, true);
                check_improve(ch, gsn_mortal_strike, true, 1);
                return;
            } else {
                check_improve(ch, gsn_mortal_strike, false, 3);
            }
        }

        multi_hit(ch, victim, null);
    }


    static void do_murde(CHAR_DATA ch, String argument) {
        send_to_char("If you want to MURDER, spell it out.\n", ch);
    }


    static void do_murder(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        OBJ_DATA wield;

        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0) {
            send_to_char("Murder whom?\n", ch);
            return;
        }

        if (IS_AFFECTED(ch, AFF_CHARM) || (IS_NPC(ch) && IS_SET(ch.act, ACT_PET))) {
            return;
        }

        if ((victim = get_char_room(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (victim == ch) {
            send_to_char("Suicide is a mortal sin.\n", ch);
            return;
        }

        if (is_safe(ch, victim)) {
            return;
        }

        if (IS_AFFECTED(ch, AFF_CHARM) && ch.master == victim) {
            act("$N is your beloved master.", ch, null, victim, TO_CHAR);
            return;
        }

        if (ch.position == POS_FIGHTING) {
            send_to_char("You do the best you can!\n", ch);
            return;
        }

        WAIT_STATE(ch, PULSE_VIOLENCE);
        if (!can_see(victim, ch)) {
            do_yell(victim, "Help! I am being attacked by someone!");
        } else {
            TextBuffer buf = new TextBuffer();
            if (IS_NPC(ch)) {
                buf.sprintf("Help! I am being attacked by %s!", ch.short_descr);
            } else {
                buf.sprintf("Help!  I am being attacked by %s!",
                        (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(victim)) ?
                                ch.doppel.name : ch.name);
            }
            do_yell(victim, buf);
        }

        if (skill_failure_nomessage(ch, gsn_mortal_strike, 0) == 0
                && (wield = get_wield_char(ch, false)) != null
                && wield.level > (victim.level - 5)) {
            int chance = 1 + get_skill(ch, gsn_mortal_strike) / 30;
            chance += (ch.level - victim.level) / 2;
            if (number_percent() < chance) {
                act("{rYour flash strike instantly slays $N!{x", ch, null, victim, TO_CHAR, POS_RESTING);
                act("{r$n flash strike instantly slays $N!{x", ch, null, victim, TO_NOTVICT, POS_RESTING);
                act("{r$n flash strike instantly slays you!{x", ch, null, victim, TO_VICT, POS_DEAD);
                damage(ch, victim, (victim.hit + 1), gsn_mortal_strike, DAM_NONE, true);
                check_improve(ch, gsn_mortal_strike, true, 1);
                return;
            } else {
                check_improve(ch, gsn_mortal_strike, false, 3);
            }
        }

        multi_hit(ch, victim, null);
    }


    static void do_flee(CHAR_DATA ch, String argument) {
        ROOM_INDEX_DATA was_in;
        ROOM_INDEX_DATA now_in;
        int attempt;

        if (RIDDEN(ch) != null) {
            send_to_char("You should ask to your rider!\n", ch);
            return;
        }

        if (MOUNTED(ch) != null) {
            do_dismount(ch, "");
        }

        if (ch.fighting == null) {
            if (ch.position == POS_FIGHTING) {
                ch.position = POS_STANDING;
            }
            send_to_char("You aren't fighting anyone.\n", ch);
            return;
        }

        if ((ch.clazz == Clazz.SAMURAI) && (ch.level >= 10)) {
            send_to_char("Your honour doesn't let you flee, try dishonoring yourself.\n", ch);
            return;
        }

        was_in = ch.in_room;
        for (attempt = 0; attempt < 6; attempt++) {
            EXIT_DATA pexit;
            int door;

            door = number_door();
            if ((pexit = was_in.exit[door]) == null
                    || pexit.to_room == null
                    || (IS_SET(pexit.exit_info, EX_CLOSED)
                    && (!IS_AFFECTED(ch, AFF_PASS_DOOR) || IS_SET(pexit.exit_info, EX_NOPASS))
                    && !IS_TRUSTED(ch, ANGEL))
                    || (IS_SET(pexit.exit_info, EX_NOFLEE))
                    || (IS_NPC(ch)
                    && IS_SET(pexit.to_room.room_flags, ROOM_NO_MOB))) {
                continue;
            }

            move_char(ch, door, false);
            if ((now_in = ch.in_room) == was_in) {
                continue;
            }

            ch.in_room = was_in;
            act("$n has fled!", ch, null, null, TO_ROOM);
            ch.in_room = now_in;

            if (!IS_NPC(ch)) {
                send_to_char("You flee from combat!  You lose 10 exps.\n", ch);
                if ((ch.clazz == Clazz.SAMURAI) && (ch.level >= 10)) {
                    gain_exp(ch, (-1 * ch.level));
                } else {
                    gain_exp(ch, -10);
                }
            } else {
                ch.last_fought = null;
            }

            stop_fighting(ch, true);
            return;
        }

        send_to_char("PANIC! You couldn't escape!\n", ch);
    }


    static void do_sla(CHAR_DATA ch, String argument) {
        send_to_char("If you want to SLAY, spell it out.\n", ch);
    }


    static void do_slay(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);
        if (arg.length() == 0) {
            send_to_char("Slay whom?\n", ch);
            return;
        }

        if ((victim = get_char_room(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (ch == victim) {
            send_to_char("Suicide is a mortal sin.\n", ch);
            return;
        }

        if (!IS_NPC(victim) && victim.level >= get_trust(ch)
                && !(IS_NPC(ch) && ch.cabal != CABAL_NONE && !IS_IMMORTAL(victim))) {
            send_to_char("You failed.\n", ch);
            return;
        }

        act("You slay $M in cold blood!", ch, null, victim, TO_CHAR);
        act("$n slays you in cold blood!", ch, null, victim, TO_VICT);
        act("$n slays $N in cold blood!", ch, null, victim, TO_NOTVICT);
        raw_kill(victim);
    }

/*
 * Check for obj dodge.
 */

    static boolean check_obj_dodge(CHAR_DATA ch, CHAR_DATA victim, OBJ_DATA obj, int bonus) {
        int chance;

        if (!IS_AWAKE(victim) || MOUNTED(victim) != null) {
            return false;
        }

        if (IS_NPC(victim)) {
            chance = UMIN(30, victim.level);
        } else {
            chance = get_skill(victim, gsn_dodge) / 2;
            /* chance for high dex. */
            chance += 2 * (get_curr_stat(victim, STAT_DEX) - 20);
            if (victim.clazz == Clazz.WARRIOR || victim.clazz == Clazz.SAMURAI) {
                chance *= 1.2;
            }
            if (victim.clazz == Clazz.THIEF || victim.clazz == Clazz.NINJA) {
                chance *= 1.1;
            }
        }

        chance -= (bonus - 90);
        chance /= 2;
        if (number_percent() >= chance &&
                (IS_NPC(victim) || victim.cabal != CABAL_BATTLE)) {
            return false;
        }

        if (!IS_NPC(victim) && victim.cabal == CABAL_BATTLE
                && IS_SET(victim.act, PLR_CANINDUCT)) {
            act("You catch $p that had been shot to you.", ch, obj, victim, TO_VICT);
            act("$N catches $p that had been shot to $M.", ch, obj, victim, TO_CHAR);
            act("$n catches $p that had been shot to $m.", victim, obj, ch, TO_NOTVICT);
            obj_to_char(obj, victim);
            return true;
        }

        act("You dodge $p that had been shot to you.", ch, obj, victim, TO_VICT);
        act("$N dodges $p that had been shot to $M.", ch, obj, victim, TO_CHAR);
        act("$n dodges $p that had been shot to $m.", victim, obj, ch, TO_NOTVICT);
        obj_to_room(obj, victim.in_room);
        check_improve(victim, gsn_dodge, true, 6);

        return true;
    }


    static void do_dishonor(CHAR_DATA ch, String argument) {
        ROOM_INDEX_DATA was_in;
        ROOM_INDEX_DATA now_in;
        CHAR_DATA gch;
        int attempt, level = 0;

        if (RIDDEN(ch) != null) {
            send_to_char("You should ask to your rider!\n", ch);
            return;
        }

        if ((ch.clazz != Clazz.SAMURAI) || (ch.level < 10)) {
            send_to_char("Which honor?.\n", ch);
            return;
        }

        if (ch.fighting == null) {
            if (ch.position == POS_FIGHTING) {
                ch.position = POS_STANDING;
            }
            send_to_char("You aren't fighting anyone.\n", ch);
            return;
        }

        for (gch = char_list; gch != null; gch = gch.next) {
            if (is_same_group(gch, ch.fighting)
                    || gch.fighting == ch) {
                level += gch.level;
            }
        }

        if ((ch.fighting.level - ch.level) < 5
                && ch.level > (level / 3)) {
            send_to_char("Your fighting doesn't worth to dishonor yourself.\n", ch);
            return;
        }

        was_in = ch.in_room;
        for (attempt = 0; attempt < 6; attempt++) {
            EXIT_DATA pexit;
            int door;

            door = number_door();
            if ((pexit = was_in.exit[door]) == null
                    || pexit.to_room == null
                    || (IS_SET(pexit.exit_info, EX_CLOSED)
                    && (!IS_AFFECTED(ch, AFF_PASS_DOOR) || IS_SET(pexit.exit_info, EX_NOPASS))
                    && !IS_TRUSTED(ch, ANGEL))
                    || (IS_SET(pexit.exit_info, EX_NOFLEE))
                    || (IS_NPC(ch)
                    && IS_SET(pexit.to_room.room_flags, ROOM_NO_MOB))) {
                continue;
            }

            move_char(ch, door, false);
            if ((now_in = ch.in_room) == was_in) {
                continue;
            }

            ch.in_room = was_in;
            act("$n has dishonored $mself!", ch, null, null, TO_ROOM);
            ch.in_room = now_in;

            if (!IS_NPC(ch)) {
                send_to_char("You dishonored yourself and flee from combat.\n", ch);
                TextBuffer buf = new TextBuffer();
                buf.sprintf("You lose %d exps.\n", ch.level);
                send_to_char(buf, ch);
                gain_exp(ch, -(ch.level));
            } else {
                ch.last_fought = null;
            }

            stop_fighting(ch, true);
            if (MOUNTED(ch) != null) {
                do_dismount(ch, "");
            }

            return;
        }

        send_to_char("PANIC! You couldn't escape!\n", ch);
    }


    static boolean mob_cast_cleric(CHAR_DATA ch, CHAR_DATA victim) {
        String spell;

        for (; ; ) {
            int min_level;

            switch (number_bits(4)) {
                case 0:
                    min_level = 0;
                    spell = "blindness";
                    break;
                case 1:
                    min_level = 3;
                    spell = "cause serious";
                    break;
                case 2:
                    min_level = 7;
                    spell = "earthquake";
                    break;
                case 3:
                    min_level = 9;
                    spell = "cause critical";
                    break;
                case 4:
                    min_level = 10;
                    spell = "dispel evil";
                    break;
                case 5:
                    min_level = 12;
                    spell = "curse";
                    break;
                case 6:
                    min_level = 14;
                    spell = "cause critical";
                    break;
                case 7:
                    min_level = 18;
                    spell = "flamestrike";
                    break;
                case 8:
                case 9:
                case 10:
                    min_level = 20;
                    spell = "harm";
                    break;
                case 11:
                    min_level = 25;
                    spell = "plague";
                    break;
                case 12:
                case 13:
                    min_level = 45;
                    spell = "severity force";
                    break;
                default:
                    min_level = 26;
                    spell = "dispel magic";
                    break;
            }

            if (ch.level >= min_level) {
                break;
            }
        }

        Skill sn = lookupSkill(spell);
        if (sn == null) {
            return false;
        }
        say_spell(ch, sn);
        sn.spell_fun(ch.level, ch, victim, TARGET_CHAR);
        return true;
    }

    static boolean mob_cast_mage(CHAR_DATA ch, CHAR_DATA victim) {
        String spell;

        for (; ; ) {
            int min_level;

            switch (number_bits(4)) {
                case 0:
                    min_level = 0;
                    spell = "blindness";
                    break;
                case 1:
                    min_level = 3;
                    spell = "chill touch";
                    break;
                case 2:
                    min_level = 7;
                    spell = "weaken";
                    break;
                case 3:
                    min_level = 9;
                    spell = "teleport";
                    break;
                case 4:
                    min_level = 14;
                    spell = "colour spray";
                    break;
                case 5:
                    min_level = 19;
                    spell = "caustic font";
                    break;
                case 6:
                    min_level = 25;
                    spell = "energy drain";
                    break;
                case 7:
                case 8:
                case 9:
                    min_level = 35;
                    spell = "caustic font";
                    break;
                case 10:
                    min_level = 40;
                    spell = "plague";
                    break;
                case 11:
                case 12:
                case 13:
                    min_level = 40;
                    spell = "acid arrow";
                    break;
                default:
                    min_level = 55;
                    spell = "acid blast";
                    break;
            }

            if (ch.level >= min_level) {
                break;
            }
        }

        Skill sn = lookupSkill(spell);
        if (sn == null) {
            return false;
        }

        say_spell(ch, sn);
        sn.spell_fun(ch.level, ch, victim, TARGET_CHAR);
        return true;
    }
}
