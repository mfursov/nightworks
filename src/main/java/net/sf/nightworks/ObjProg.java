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

import java.lang.reflect.Method;

import static net.sf.nightworks.ActComm.cabal_area_check;
import static net.sf.nightworks.ActComm.do_say;
import static net.sf.nightworks.ActComm.do_yell;
import static net.sf.nightworks.Comm.act;
import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.DB.bug;
import static net.sf.nightworks.DB.create_object;
import static net.sf.nightworks.DB.dice;
import static net.sf.nightworks.DB.get_obj_index;
import static net.sf.nightworks.DB.get_room_index;
import static net.sf.nightworks.DB.number_bits;
import static net.sf.nightworks.DB.number_percent;
import static net.sf.nightworks.Effects.fire_effect;
import static net.sf.nightworks.Fight.damage;
import static net.sf.nightworks.Fight.one_hit;
import static net.sf.nightworks.Fight.raw_kill_org;
import static net.sf.nightworks.Handler.affect_remove;
import static net.sf.nightworks.Handler.affect_strip;
import static net.sf.nightworks.Handler.affect_to_char;
import static net.sf.nightworks.Handler.char_from_room;
import static net.sf.nightworks.Handler.char_to_room;
import static net.sf.nightworks.Handler.extract_obj;
import static net.sf.nightworks.Handler.get_eq_char;
import static net.sf.nightworks.Handler.get_hold_char;
import static net.sf.nightworks.Handler.get_shield_char;
import static net.sf.nightworks.Handler.get_skill;
import static net.sf.nightworks.Handler.get_wield_char;
import static net.sf.nightworks.Handler.is_affected;
import static net.sf.nightworks.Handler.is_equiped_char;
import static net.sf.nightworks.Handler.is_wielded_char;
import static net.sf.nightworks.Handler.obj_from_char;
import static net.sf.nightworks.Handler.obj_from_room;
import static net.sf.nightworks.Handler.obj_to_obj;
import static net.sf.nightworks.Handler.obj_to_room;
import static net.sf.nightworks.Handler.unequip_char;
import static net.sf.nightworks.Magic.obj_cast_spell;
import static net.sf.nightworks.Magic.spell_bluefire;
import static net.sf.nightworks.Magic.spell_cure_disease;
import static net.sf.nightworks.Magic.spell_cure_poison;
import static net.sf.nightworks.Magic.spell_curse;
import static net.sf.nightworks.Magic.spell_dispel_evil;
import static net.sf.nightworks.Magic.spell_dispel_good;
import static net.sf.nightworks.Magic.spell_holy_word;
import static net.sf.nightworks.Magic.spell_lightning_bolt;
import static net.sf.nightworks.Magic.spell_poison;
import static net.sf.nightworks.Magic.spell_remove_curse;
import static net.sf.nightworks.Magic2.spell_scream;
import static net.sf.nightworks.Magic2.spell_web;
import static net.sf.nightworks.Nightworks.AFFECT_DATA;
import static net.sf.nightworks.Nightworks.AFF_BERSERK;
import static net.sf.nightworks.Nightworks.AFF_CURSE;
import static net.sf.nightworks.Nightworks.AFF_FLYING;
import static net.sf.nightworks.Nightworks.AFF_HASTE;
import static net.sf.nightworks.Nightworks.AFF_PLAGUE;
import static net.sf.nightworks.Nightworks.AFF_POISON;
import static net.sf.nightworks.Nightworks.APPLY_AC;
import static net.sf.nightworks.Nightworks.APPLY_DAMROLL;
import static net.sf.nightworks.Nightworks.APPLY_DEX;
import static net.sf.nightworks.Nightworks.APPLY_HITROLL;
import static net.sf.nightworks.Nightworks.APPLY_NONE;
import static net.sf.nightworks.Nightworks.APPLY_STR;
import static net.sf.nightworks.Nightworks.CABAL_BATTLE;
import static net.sf.nightworks.Nightworks.CABAL_CHAOS;
import static net.sf.nightworks.Nightworks.CABAL_HUNTER;
import static net.sf.nightworks.Nightworks.CABAL_INVADER;
import static net.sf.nightworks.Nightworks.CABAL_KNIGHT;
import static net.sf.nightworks.Nightworks.CABAL_LIONS;
import static net.sf.nightworks.Nightworks.CABAL_RULER;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.DAM_FIRE;
import static net.sf.nightworks.Nightworks.DAM_HOLY;
import static net.sf.nightworks.Nightworks.IS_AFFECTED;
import static net.sf.nightworks.Nightworks.IS_EVIL;
import static net.sf.nightworks.Nightworks.IS_GOOD;
import static net.sf.nightworks.Nightworks.IS_NPC;
import static net.sf.nightworks.Nightworks.IS_WEAPON_STAT;
import static net.sf.nightworks.Nightworks.ITEM_WEAPON;
import static net.sf.nightworks.Nightworks.MAX_CABAL;
import static net.sf.nightworks.Nightworks.OBJ_DATA;
import static net.sf.nightworks.Nightworks.OBJ_INDEX_DATA;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_BATTLE_THRONE;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_CHAOS_ALTAR;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_HUNTER_ALTAR;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_INVADER_SKULL;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_KNIGHT_ALTAR;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_LIONS_ALTAR;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_RULER_STAND;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SHALAFI_ALTAR;
import static net.sf.nightworks.Nightworks.OPROG_AREA;
import static net.sf.nightworks.Nightworks.OPROG_DEATH;
import static net.sf.nightworks.Nightworks.OPROG_DROP;
import static net.sf.nightworks.Nightworks.OPROG_ENTRY;
import static net.sf.nightworks.Nightworks.OPROG_FIGHT;
import static net.sf.nightworks.Nightworks.OPROG_FUN_AREA;
import static net.sf.nightworks.Nightworks.OPROG_FUN_DEATH;
import static net.sf.nightworks.Nightworks.OPROG_FUN_DROP;
import static net.sf.nightworks.Nightworks.OPROG_FUN_ENTRY;
import static net.sf.nightworks.Nightworks.OPROG_FUN_FIGHT;
import static net.sf.nightworks.Nightworks.OPROG_FUN_GET;
import static net.sf.nightworks.Nightworks.OPROG_FUN_GIVE;
import static net.sf.nightworks.Nightworks.OPROG_FUN_GREET;
import static net.sf.nightworks.Nightworks.OPROG_FUN_REMOVE;
import static net.sf.nightworks.Nightworks.OPROG_FUN_SAC;
import static net.sf.nightworks.Nightworks.OPROG_FUN_SPEECH;
import static net.sf.nightworks.Nightworks.OPROG_FUN_WEAR;
import static net.sf.nightworks.Nightworks.OPROG_GET;
import static net.sf.nightworks.Nightworks.OPROG_GIVE;
import static net.sf.nightworks.Nightworks.OPROG_GREET;
import static net.sf.nightworks.Nightworks.OPROG_REMOVE;
import static net.sf.nightworks.Nightworks.OPROG_SAC;
import static net.sf.nightworks.Nightworks.OPROG_SPEECH;
import static net.sf.nightworks.Nightworks.OPROG_WEAR;
import static net.sf.nightworks.Nightworks.POS_DEAD;
import static net.sf.nightworks.Nightworks.POS_SLEEPING;
import static net.sf.nightworks.Nightworks.PULSE_VIOLENCE;
import static net.sf.nightworks.Nightworks.RES_COLD;
import static net.sf.nightworks.Nightworks.RES_FIRE;
import static net.sf.nightworks.Nightworks.SECT_FIELD;
import static net.sf.nightworks.Nightworks.SECT_FOREST;
import static net.sf.nightworks.Nightworks.SECT_HILLS;
import static net.sf.nightworks.Nightworks.SECT_MOUNTAIN;
import static net.sf.nightworks.Nightworks.SET_BIT;
import static net.sf.nightworks.Nightworks.TARGET_CHAR;
import static net.sf.nightworks.Nightworks.TO_AFFECTS;
import static net.sf.nightworks.Nightworks.TO_CHAR;
import static net.sf.nightworks.Nightworks.TO_NOTVICT;
import static net.sf.nightworks.Nightworks.TO_RESIST;
import static net.sf.nightworks.Nightworks.TO_ROOM;
import static net.sf.nightworks.Nightworks.TO_VICT;
import static net.sf.nightworks.Nightworks.UMIN;
import static net.sf.nightworks.Nightworks.WAIT_STATE;
import static net.sf.nightworks.Nightworks.WEAPON_KATANA;
import static net.sf.nightworks.Nightworks.WEAR_ARMS;
import static net.sf.nightworks.Nightworks.WEAR_FINGER;
import static net.sf.nightworks.Nightworks.WEAR_HANDS;
import static net.sf.nightworks.Nightworks.WEAR_TATTOO;
import static net.sf.nightworks.Nightworks.current_time;
import static net.sf.nightworks.Nightworks.exit;
import static net.sf.nightworks.Skill.gsn_acid_blast;
import static net.sf.nightworks.Skill.gsn_berserk;
import static net.sf.nightworks.Skill.gsn_blackguard;
import static net.sf.nightworks.Skill.gsn_bless;
import static net.sf.nightworks.Skill.gsn_blindness;
import static net.sf.nightworks.Skill.gsn_burning_hands;
import static net.sf.nightworks.Skill.gsn_confuse;
import static net.sf.nightworks.Skill.gsn_cure_critical;
import static net.sf.nightworks.Skill.gsn_cure_light;
import static net.sf.nightworks.Skill.gsn_cure_poison;
import static net.sf.nightworks.Skill.gsn_cure_serious;
import static net.sf.nightworks.Skill.gsn_curse;
import static net.sf.nightworks.Skill.gsn_demonfire;
import static net.sf.nightworks.Skill.gsn_dispel_evil;
import static net.sf.nightworks.Skill.gsn_dispel_good;
import static net.sf.nightworks.Skill.gsn_dragon_breath;
import static net.sf.nightworks.Skill.gsn_dragon_strength;
import static net.sf.nightworks.Skill.gsn_faerie_fire;
import static net.sf.nightworks.Skill.gsn_fire_breath;
import static net.sf.nightworks.Skill.gsn_fire_shield;
import static net.sf.nightworks.Skill.gsn_fly;
import static net.sf.nightworks.Skill.gsn_garble;
import static net.sf.nightworks.Skill.gsn_giant_strength;
import static net.sf.nightworks.Skill.gsn_haste;
import static net.sf.nightworks.Skill.gsn_headguard;
import static net.sf.nightworks.Skill.gsn_kassandra;
import static net.sf.nightworks.Skill.gsn_lightning_bolt;
import static net.sf.nightworks.Skill.gsn_lightning_breath;
import static net.sf.nightworks.Skill.gsn_matandra;
import static net.sf.nightworks.Skill.gsn_mirror;
import static net.sf.nightworks.Skill.gsn_neckguard;
import static net.sf.nightworks.Skill.gsn_plague;
import static net.sf.nightworks.Skill.gsn_poison;
import static net.sf.nightworks.Skill.gsn_ranger_staff;
import static net.sf.nightworks.Skill.gsn_remove_curse;
import static net.sf.nightworks.Skill.gsn_scream;
import static net.sf.nightworks.Skill.gsn_sebat;
import static net.sf.nightworks.Skill.gsn_shield;
import static net.sf.nightworks.Skill.gsn_slow;
import static net.sf.nightworks.Skill.gsn_weaken;
import static net.sf.nightworks.Skill.gsn_x_hit;
import static net.sf.nightworks.Skill.lookupSkill;
import static net.sf.nightworks.Tables.cabal_table;
import static net.sf.nightworks.util.TextUtils.str_cmp;

/**
 * Last mofified:           $Date: 2006-07-21 06:43:28 -0700 (Fri, 21 Jul 2006) $
 * Revision of last commit: $Revision: 27 $
 */
class ObjProg {
    static void oprog_set(OBJ_INDEX_DATA objindex, String progtype, String name) throws NoSuchMethodException {
        boolean found = true;
        try {
            if (!str_cmp(progtype, "wear_prog")) {
                objindex.oprogs.wear_prog = create_wear_prog(name);
                objindex.progtypes = SET_BIT(objindex.progtypes, OPROG_WEAR);
            } else if (!str_cmp(progtype, "remove_prog")) {
                objindex.oprogs.remove_prog = create_remove_prog(name);
                objindex.progtypes = SET_BIT(objindex.progtypes, OPROG_REMOVE);
            } else if (!str_cmp(progtype, "get_prog")) {
                objindex.oprogs.get_prog = create_get_prog(name);
                objindex.progtypes = SET_BIT(objindex.progtypes, OPROG_GET);
            } else if (!str_cmp(progtype, "drop_prog")) {
                objindex.oprogs.drop_prog = create_drop_prog(name);
                objindex.progtypes = SET_BIT(objindex.progtypes, OPROG_DROP);
            } else if (!str_cmp(progtype, "sac_prog")) {
                objindex.oprogs.sac_prog = create_sac_prog(name);
                objindex.progtypes = SET_BIT(objindex.progtypes, OPROG_SAC);
            } else if (!str_cmp(progtype, "entry_prog")) {
                objindex.oprogs.entry_prog = create_entry_prog(name);
                objindex.progtypes = SET_BIT(objindex.progtypes, OPROG_ENTRY);
            } else if (!str_cmp(progtype, "give_prog")) {
                objindex.oprogs.give_prog = create_give_prog(name);
                objindex.progtypes = SET_BIT(objindex.progtypes, OPROG_GIVE);
            } else if (!str_cmp(progtype, "greet_prog")) {
                objindex.oprogs.greet_prog = create_greet_prog(name);
                objindex.progtypes = SET_BIT(objindex.progtypes, OPROG_GREET);
            } else if (!str_cmp(progtype, "fight_prog")) {
                objindex.oprogs.fight_prog = create_fight_prog(name);
                objindex.progtypes = SET_BIT(objindex.progtypes, OPROG_FIGHT);
            } else if (!str_cmp(progtype, "death_prog")) /* returning true prevents death */ {
                objindex.oprogs.death_prog = create_death_prog(name);
                objindex.progtypes = SET_BIT(objindex.progtypes, OPROG_DEATH);
            } else if (!str_cmp(progtype, "speech_prog")) {
                objindex.oprogs.speech_prog = create_speech_prog(name);
                objindex.progtypes = SET_BIT(objindex.progtypes, OPROG_SPEECH);
            } else if (!str_cmp(progtype, "area_prog")) {
                objindex.oprogs.area_prog = create_area_prog(name);
                objindex.progtypes = SET_BIT(objindex.progtypes, OPROG_AREA);
            } else {
                found = false;
            }
        } finally {
            if (!found) {
                bug("Load_oprogs: 'O': invalid program type for vnum %d", objindex.vnum);
                exit(1);
            }
        }
    }

    private static Method resolveMethod(String name, Class... params) throws NoSuchMethodException {
        return ObjProg.class.getDeclaredMethod(name, params);
    }

    private static OPROG_FUN_REMOVE create_remove_prog(final String name) throws NoSuchMethodException {
        return new OPROG_FUN_REMOVE() {
            final Method m = resolveMethod(name, OBJ_DATA.class, CHAR_DATA.class);

            public void run(OBJ_DATA obj, CHAR_DATA ch) {
                try {
                    m.invoke(null, obj, ch);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private static OPROG_FUN_GET create_get_prog(final String name) throws NoSuchMethodException {
        return new OPROG_FUN_GET() {
            final Method m = resolveMethod(name, OBJ_DATA.class, CHAR_DATA.class);

            public void run(OBJ_DATA obj, CHAR_DATA ch) {
                try {
                    m.invoke(null, obj, ch);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private static OPROG_FUN_DROP create_drop_prog(final String name) throws NoSuchMethodException {
        return new OPROG_FUN_DROP() {
            final Method m = resolveMethod(name, OBJ_DATA.class, CHAR_DATA.class);

            public void run(OBJ_DATA obj, CHAR_DATA ch) {
                try {
                    m.invoke(null, obj, ch);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private static OPROG_FUN_SAC create_sac_prog(final String name) throws NoSuchMethodException {
        return new OPROG_FUN_SAC() {
            final Method m = resolveMethod(name, OBJ_DATA.class, CHAR_DATA.class);

            public boolean run(OBJ_DATA obj, CHAR_DATA ch) {
                try {
                    return (Boolean) m.invoke(null, obj, ch);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };
    }

    private static OPROG_FUN_ENTRY create_entry_prog(final String name) throws NoSuchMethodException {
        return new OPROG_FUN_ENTRY() {
            final Method m = resolveMethod(name, OBJ_DATA.class);

            public void run(OBJ_DATA obj) {
                try {
                    m.invoke(null, obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private static OPROG_FUN_GIVE create_give_prog(final String name) throws NoSuchMethodException {
        return new OPROG_FUN_GIVE() {
            final Method m = resolveMethod(name, OBJ_DATA.class, CHAR_DATA.class, CHAR_DATA.class);

            public void run(OBJ_DATA obj, CHAR_DATA from, CHAR_DATA to) {
                try {
                    m.invoke(null, obj, from, to);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private static OPROG_FUN_GREET create_greet_prog(final String name) throws NoSuchMethodException {
        return new OPROG_FUN_GREET() {
            final Method m = resolveMethod(name, OBJ_DATA.class);

            public void run(OBJ_DATA obj, CHAR_DATA ch) {
                try {
                    m.invoke(null, obj, ch);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private static OPROG_FUN_FIGHT create_fight_prog(final String name) throws NoSuchMethodException {
        return new OPROG_FUN_FIGHT() {
            final Method m = resolveMethod(name, OBJ_DATA.class, CHAR_DATA.class);

            public void run(OBJ_DATA obj, CHAR_DATA ch) {
                try {
                    m.invoke(null, obj, ch);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private static OPROG_FUN_DEATH create_death_prog(final String name) throws NoSuchMethodException {
        return new OPROG_FUN_DEATH() {
            final Method m = resolveMethod(name, OBJ_DATA.class, CHAR_DATA.class);

            public boolean run(OBJ_DATA obj, CHAR_DATA ch) {
                try {
                    return (Boolean) m.invoke(null, obj, ch);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };
    }

    private static OPROG_FUN_SPEECH create_speech_prog(String name) throws NoSuchMethodException {
        final Method m = resolveMethod(name, OBJ_DATA.class, CHAR_DATA.class, String.class);
        return new OPROG_FUN_SPEECH() {
            public void run(OBJ_DATA obj, CHAR_DATA ch, String speech) {
                try {
                    m.invoke(null, obj, ch);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private static OPROG_FUN_AREA create_area_prog(String name) throws NoSuchMethodException {
        final Method m = resolveMethod(name, OBJ_DATA.class);
        return new OPROG_FUN_AREA() {
            public void run(OBJ_DATA obj) {
                try {
                    m.invoke(null, obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private static OPROG_FUN_WEAR create_wear_prog(String name) throws NoSuchMethodException {
        final Method m = resolveMethod(name, OBJ_DATA.class, CHAR_DATA.class);
        return new OPROG_FUN_WEAR() {
            public void run(OBJ_DATA obj, CHAR_DATA ch) {
                try {
                    m.invoke(null, obj, ch);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }


    static void wear_prog_excalibur(OBJ_DATA obj, CHAR_DATA ch) {
        act("$p begins to shine a bright white.", ch, obj, null, TO_CHAR);
        act("$p begins to shine a bright white.", ch, obj, null, TO_ROOM);
        if (ch.level > 20 && ch.level <= 30) {
            obj.value[2] = 4;
        } else if (ch.level > 30 && ch.level <= 40) {
            obj.value[2] = 5;
        } else if (ch.level > 40 && ch.level <= 50) {
            obj.value[2] = 6;
        } else if (ch.level > 50 && ch.level <= 60) {
            obj.value[2] = 7;
        } else if (ch.level > 60 && ch.level <= 70) {
            obj.value[2] = 9;
        } else if (ch.level > 70 && ch.level <= 80) {
            obj.value[2] = 11;
        } else {
            obj.value[2] = 12;
        }
    }

    static void wear_prog_bracer(OBJ_DATA obj, CHAR_DATA ch) {

        if (!is_affected(ch, gsn_haste)) {
            send_to_char("As you slide your arms into these bracers, they mold to your skin.\n", ch);
            send_to_char("Your hands and arms feel incredibly light.\n", ch);
            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_haste;
            af.duration = -2;
            af.level = ch.level;
            af.bitvector = AFF_HASTE;
            af.location = APPLY_DEX;
            af.modifier = 1 + (ch.level >= 18 ? 1 : 0) + (ch.level >= 30 ? 1 : 0) + (ch.level >= 45 ? 1 : 0);
            affect_to_char(ch, af);
        }
    }

    static void remove_prog_bracer(OBJ_DATA obj, CHAR_DATA ch) {
        if (is_affected(ch, gsn_haste)) {
            affect_strip(ch, gsn_haste);
            send_to_char("Your hands and arms feel heavy again.\n", ch);
        }
    }


    static void remove_prog_excalibur(OBJ_DATA obj, CHAR_DATA ch) {
        act("$p stops glowing.", ch, obj, null, TO_CHAR);
        act("$p stops glowing.", ch, obj, null, TO_ROOM);
    }

    boolean death_prog_excalibur(OBJ_DATA obj, CHAR_DATA ch) {
        act("$p starts to glow with a blue aura.", ch, obj, null, TO_CHAR, POS_DEAD);
        act("$p starts to glow with a blue aura,", ch, obj, null, TO_ROOM);
        ch.hit = ch.max_hit;
        send_to_char("You feel much better.", ch);
        act("$n looks much better.", ch, null, null, TO_ROOM);
        return true;
    }

    static void speech_prog_excalibur(OBJ_DATA obj, CHAR_DATA ch, String speech) {

        if (!str_cmp(speech, "sword of acid") && (ch.fighting != null) && is_wielded_char(ch, obj)) {
            send_to_char("Acid sprays from the blade of Excalibur.\n", ch);
            act("Acid sprays from the blade of Excalibur.", ch, null, null, TO_ROOM);
            obj_cast_spell(gsn_acid_blast, ch.level, ch, ch.fighting, null);
            WAIT_STATE(ch, 2 * PULSE_VIOLENCE);
        }
    }

    boolean sac_prog_excalibur(OBJ_DATA obj, CHAR_DATA ch) {
        act("The gods are infuriated!", ch, null, null, TO_CHAR);
        act("The gods are infuriated!", ch, null, null, TO_ROOM);
        damage(ch, ch, (ch.hit - 1) > 1000 ? 1000 : (ch.hit - 1), gsn_x_hit, DAM_HOLY, true);
        ch.gold = 0;
        return true;
    }

    static void fight_prog_ranger_staff(OBJ_DATA obj, CHAR_DATA ch) {
        if (is_wielded_char(ch, obj) && number_percent() < 10) {
            send_to_char("Your ranger's staff glows blue!\n", ch);
            act("$n's ranger's staff glows blue!", ch, null, null, TO_ROOM);

            obj_cast_spell(gsn_cure_critical, ch.level, ch, ch, obj);
        }
    }

    static void fight_prog_sub_weapon(OBJ_DATA obj, CHAR_DATA ch) {
        if (is_wielded_char(ch, obj) && number_percent() < 30) {
            if (((float) ch.hit) / ((float) ch.max_hit) > 0.9) {
                send_to_char("Your weapon whispers, 'You're doing great!'\n", ch);
            } else if (((float) ch.hit) / ((float) ch.max_hit) > 0.6) {
                send_to_char("Your weapon whispers, 'Keep up the good work!'\n", ch);
            } else if (((float) ch.hit) / ((float) ch.max_hit) > 0.4) {
                send_to_char("Your weapon whispers, 'You can do it!'\n", ch);
            } else {
                send_to_char("Your weapon whispers, 'Run away! Run away!'\n", ch);
            }
        }
    }

    boolean death_prog_ranger_staff(OBJ_DATA obj, CHAR_DATA ch) {
        send_to_char("Your ranger's staff disappears.\n", ch);
        act("$n's ranger's staff disappears.", ch, null, null, TO_ROOM);
        extract_obj(obj);
        return false;
    }


    static void get_prog_spec_weapon(OBJ_DATA obj, CHAR_DATA ch) {

        if (obj.extra_descr.description.contains(ch.name)) {
            if (IS_AFFECTED(ch, AFF_POISON) && (dice(1, 5) == 1)) {
                send_to_char("Your weapon glows blue.", ch);
                act("$n's weapon glows blue.", ch, null, null, TO_ROOM);
                spell_cure_poison(gsn_cure_poison, 30, ch, ch, TARGET_CHAR);
                return;
            }
            if (IS_AFFECTED(ch, AFF_CURSE) && (dice(1, 5) == 1)) {
                send_to_char("Your weapon glows blue.", ch);
                act("$n's weapon glows blue.", ch, null, null, TO_ROOM);
                spell_remove_curse(gsn_remove_curse, 30, ch, ch, TARGET_CHAR);
                return;
            }
            send_to_char("Your weapon's humming gets lauder.\n", ch);
            return;
        }
        act("You are zapped by $p and drop it.", ch, obj, null, TO_CHAR);

        obj_from_char(obj);
        obj_to_room(obj, ch.in_room);

        switch (dice(1, 10)) {
            case 1:
                spell_curse(gsn_curse, ch.level < 10 ? 1 : ch.level - 9, ch, ch, TARGET_CHAR);
                break;
            case 2:
                spell_poison(gsn_poison, ch.level < 10 ? 1 : ch.level - 9, ch, ch, TARGET_CHAR);
                break;
        }

    }

    static void get_prog_quest_hreward(OBJ_DATA obj, CHAR_DATA ch) {

        if (obj.extra_descr.description.contains(ch.name)) {
            act("{bYour $p starts glowing.\n{x", ch, obj, null, TO_CHAR, POS_SLEEPING);
            return;
        }

        act("You are zapped by $p and drop it.", ch, obj, null, TO_CHAR);

        obj_from_char(obj);
        obj_to_room(obj, ch.in_room);

    }

    static void get_prog_quest_obj(OBJ_DATA obj, CHAR_DATA ch) {

        if (obj.extra_descr.description.contains(ch.name)) {
            if (IS_AFFECTED(ch, AFF_POISON) && (dice(1, 5) == 1)) {
                send_to_char("Your weapon glows blue.", ch);
                act("$n's weapon glows blue.", ch, null, null, TO_ROOM);
                spell_cure_poison(gsn_cure_poison, 30, ch, ch, TARGET_CHAR);
                return;
            }
            if (IS_AFFECTED(ch, AFF_CURSE) && (dice(1, 5) == 1)) {
                send_to_char("Your weapon glows blue.", ch);
                act("$n's weapon glows blue.", ch, null, null, TO_ROOM);
                spell_remove_curse(gsn_remove_curse, 30, ch, ch, TARGET_CHAR);
                return;
            }
            send_to_char("Quest staff waits patiently to return.\n", ch);
            return;
        }
        act("You are zapped by $p and drop it.", ch, obj, null, TO_CHAR);

        obj_from_char(obj);
        obj_to_room(obj, ch.in_room);

        switch (dice(1, 10)) {
            case 1:
                spell_curse(gsn_curse, ch.level < 10 ? 1 : ch.level - 9, ch, ch, TARGET_CHAR);
                break;
            case 2:
                spell_poison(gsn_poison, ch.level < 10 ? 1 : ch.level - 9, ch, ch, TARGET_CHAR);
                break;
        }

    }

    static void get_prog_cabal_item(OBJ_DATA obj, CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            act("You are not worthy to have $p and drop it.", ch, obj, null, TO_CHAR);
            act("$n is not worthy to have $p and drops it.", ch, obj, null, TO_ROOM);
            obj_from_char(obj);
            obj_to_room(obj, ch.in_room);
            return;
        }

        if (obj.timer < 1) {
            obj.timer = 30;
            act("$p becomes transparent.", ch, obj, null, TO_CHAR);
            act("$p becomes transparent.", ch, obj, null, TO_ROOM);
        }
    }

    boolean sac_prog_cabal_item(OBJ_DATA obj, CHAR_DATA ch) {
        OBJ_DATA container;
        int i;

        act("The gods are infuriated!", ch, null, null, TO_CHAR);
        act("The gods are infuriated!", ch, null, null, TO_ROOM);
        damage(ch, ch, ch.hit / 10, gsn_x_hit, DAM_HOLY, true);
        ch.gold = 0;

        obj_from_room(obj);
        for (i = 0; i < MAX_CABAL; i++) {
            if (cabal_table[i].obj_ptr == obj) {
                break;
            }
        }

        if (i < MAX_CABAL) {
            if (obj.pIndexData.vnum == cabal_table[CABAL_RULER].obj_vnum) {
                container = create_object(get_obj_index(OBJ_VNUM_RULER_STAND), 100);
            } else if (obj.pIndexData.vnum == cabal_table[CABAL_INVADER].obj_vnum) {
                container = create_object(get_obj_index(OBJ_VNUM_INVADER_SKULL), 100);
            } else if (obj.pIndexData.vnum == cabal_table[CABAL_BATTLE].obj_vnum) {
                container = create_object(get_obj_index(OBJ_VNUM_BATTLE_THRONE), 100);
            } else if (obj.pIndexData.vnum == cabal_table[CABAL_KNIGHT].obj_vnum) {
                container = create_object(get_obj_index(OBJ_VNUM_KNIGHT_ALTAR), 100);
            } else if (obj.pIndexData.vnum == cabal_table[CABAL_CHAOS].obj_vnum) {
                container = create_object(get_obj_index(OBJ_VNUM_CHAOS_ALTAR), 100);
            } else if (obj.pIndexData.vnum == cabal_table[CABAL_LIONS].obj_vnum) {
                container = create_object(get_obj_index(OBJ_VNUM_LIONS_ALTAR), 100);
            } else if (obj.pIndexData.vnum == cabal_table[CABAL_HUNTER].obj_vnum) {
                container = create_object(get_obj_index(OBJ_VNUM_HUNTER_ALTAR), 100);
            } else {
                container = create_object(get_obj_index(OBJ_VNUM_SHALAFI_ALTAR), 100);
            }

            obj_to_obj(obj, container);
            obj_to_room(container, get_room_index(cabal_table[i].room_vnum));
            TextBuffer buf = new TextBuffer();
            buf.sprintf("You see %s forming again slowly.\n", container.short_descr);
            if (get_room_index(cabal_table[i].room_vnum).people != null) {
                act(buf.toString(), get_room_index(cabal_table[i].room_vnum).people, null, null, TO_CHAR);
                act(buf.toString(), get_room_index(cabal_table[i].room_vnum).people, null, null, TO_ROOM);
            }
            return true;
        } else {
            extract_obj(obj);
            bug("oprog: Sac_cabal_item: Was not the cabal's item.");
        }

        return false;
    }

    static void speech_prog_kassandra(OBJ_DATA obj, CHAR_DATA ch, String speech) {
        if (!str_cmp(speech, "kassandra") && (get_hold_char(ch) == obj)
                && !IS_NPC(ch)) {
            obj_cast_spell(gsn_kassandra, ch.level, ch, ch, null);
        } else if (!str_cmp(speech, "sebat") && (get_hold_char(ch) == obj) && !IS_NPC(ch)) {
            obj_cast_spell(gsn_sebat, ch.level, ch, ch, null);
        } else if (!str_cmp(speech, "matandra") && (get_hold_char(ch) == obj) && (ch.fighting != null) && !IS_NPC(ch)) {
            act("A blast of energy bursts from your hand toward $N!", ch, null, ch.fighting, TO_CHAR);
            act("A blast of energy bursts from $n's hand toward you!", ch, null, ch.fighting, TO_VICT);
            act("A blast of energy bursts from $n's hand toward $N!", ch, null, ch.fighting, TO_NOTVICT);
            obj_cast_spell(gsn_matandra, ch.level, ch, ch.fighting, null);
        }
    }

    static void fight_prog_chaos_blade(OBJ_DATA obj, CHAR_DATA ch) {
        if (is_wielded_char(ch, obj)) {
            switch (number_bits(7)) {
                case 0:

                    act("The chaotic blade trembles violently!", ch, null, null, TO_ROOM);
                    send_to_char("Your chaotic blade trembles violently!\n", ch);
                    obj_cast_spell(gsn_mirror, ch.level, ch, ch, obj);
                    WAIT_STATE(ch, 2 * PULSE_VIOLENCE);
                    break;

                case 1:

                    act("The chaotic blade shakes a bit.", ch, null, null, TO_ROOM);
                    send_to_char("Your chaotic blade shakes a bit.\n", ch);
                    obj_cast_spell(gsn_garble, ch.level, ch, ch.fighting, obj);
                    WAIT_STATE(ch, 2 * PULSE_VIOLENCE);
                    break;

                case 2:

                    act("The chaotic blade shivers uncontrollably!", ch, null, null, TO_ROOM);
                    send_to_char("Your chaotic blade shivers uncontrollably!\n", ch);
                    obj_cast_spell(gsn_confuse, ch.level, ch, ch.fighting, obj);
                    WAIT_STATE(ch, 2 * PULSE_VIOLENCE);
                    break;

            }
        }
    }

    boolean death_prog_chaos_blade(OBJ_DATA obj, CHAR_DATA ch) {
        send_to_char("Your chaotic blade disappears.\n", ch);
        act("$n's chaotic blade disappears.", ch, null, null, TO_ROOM);
        extract_obj(obj);
        return false;
    }

    static void fight_prog_tattoo_apollon(OBJ_DATA obj, CHAR_DATA ch) {

        if (get_eq_char(ch, WEAR_TATTOO) == obj) {
            switch (number_bits(6)) {
                case 0:
                case 1:
                    act("{cThe tattoo on your shoulder glows blue.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_cure_serious, ch.level, ch, ch, obj);
                    break;
                case 2:
                    act("{rThe tattoo on your shoulder glows red.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    do_yell(ch, "Ever dance with good....");
                    Skill sn = lookupSkill("holy word");
                    spell_holy_word(sn, ch.level, ch, null, TARGET_CHAR);
                    break;
            }
        }
    }


    static void fight_prog_tattoo_zeus(OBJ_DATA obj, CHAR_DATA ch) {
        if (get_eq_char(ch, WEAR_TATTOO) == obj) {
            switch (number_bits(6)) {
                case 0:
                case 1:
                case 2:
                    act("{cThe tattoo on your shoulder glows blue.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_cure_critical, ch.level, ch, ch, obj);
                    break;
                case 3:
                    act("{cThe tattoo on your shoulder glows blue.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    if (IS_AFFECTED(ch, AFF_PLAGUE)) {
                        spell_cure_disease(Skill.gsn_cure_disease, 100, ch, ch, TARGET_CHAR);
                    }
                    if (IS_AFFECTED(ch, AFF_POISON)) {
                        spell_cure_poison(gsn_cure_poison, 100, ch, ch, TARGET_CHAR);
                    }
                    break;
            }
        }
    }

    static void fight_prog_tattoo_siebele(OBJ_DATA obj, CHAR_DATA ch) {
        if (get_eq_char(ch, WEAR_TATTOO) == obj) {
            switch (number_bits(6)) {
                case 0:
                    act("{cThe tattoo on your shoulder glows blue.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_cure_serious, ch.level, ch, ch, obj);
                    break;
                case 1:
                    act("{rThe tattoo on your shoulder glows red.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    spell_bluefire(gsn_dispel_good, ch.level, ch, ch.fighting, TARGET_CHAR);
                    break;
            }
        }
    }

    static void fight_prog_tattoo_ahrumazda(OBJ_DATA obj, CHAR_DATA ch) {
        if (get_eq_char(ch, WEAR_TATTOO) == obj) {
            switch (number_bits(6)) {
                case 0:
                    act("{bThe tattoo on your shoulder glows blue.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_cure_serious, ch.level, ch, ch, obj);
                    break;
                case 1:
                    act("{rThe tattoo on your shoulder glows red.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_demonfire, ch.level, ch, ch.fighting, obj);
                    break;
            }
        }
    }

    static void fight_prog_tattoo_hephaestus(OBJ_DATA obj, CHAR_DATA ch) {
        if (get_eq_char(ch, WEAR_TATTOO) == obj) {
            switch (number_bits(6)) {
                case 0:
                case 1:
                    act("{cThe tattoo on your shoulder glows blue.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_cure_serious, ch.level, ch, ch, obj);
                    break;
                case 2:
                    act("{rThe tattoo on your shoulder glows red.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    do_yell(ch, "And justice for all!....");
                    spell_scream(gsn_scream, ch.level, ch, ch.fighting, TARGET_CHAR);
                    break;
            }
        }
    }

    static void fight_prog_tattoo_ehrumen(OBJ_DATA obj, CHAR_DATA ch) {
        if (get_eq_char(ch, WEAR_TATTOO) == obj) {
            switch (number_bits(6)) {
                case 0:
                    act("{rThe tattoo on your shoulder glows red.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_cure_light, ch.level, ch, ch.fighting, obj);
                    break;
                case 1:
                    act("{cThe tattoo on your shoulder glows blue.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_cure_serious, ch.level, ch, ch, obj);
                    break;
                case 2:
                    act("{rThe tattoo on your shoulder glows red.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    spell_dispel_evil(gsn_dispel_evil, ch.level, ch, ch.fighting, TARGET_CHAR);
                    break;
            }
        }
    }

    static void fight_prog_tattoo_venus(OBJ_DATA obj, CHAR_DATA ch) {
        if (get_eq_char(ch, WEAR_TATTOO) == obj) {
            switch (number_bits(7)) {
                case 0:
                case 1:
                case 2:
                    act("{cThe tattoo on your shoulder glows blue.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_cure_light, ch.level, ch, ch, obj);
                    break;
                case 3:
                    act("{rThe tattoo on your shoulder glows red.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_plague, ch.level, ch, ch.fighting, obj);
                    break;
                case 4:
                    act("{cThe tattoo on your shoulder glows blue.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_bless, ch.level, ch, ch, obj);
                    break;
            }
        }
    }

    static void fight_prog_tattoo_ares(OBJ_DATA obj, CHAR_DATA ch) {
        if (get_eq_char(ch, WEAR_TATTOO) == obj) {
            switch (number_bits(5)) {
                case 0:
                    act("{bThe tattoo on your shoulder glows blue.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_dragon_strength, ch.level, ch, ch, obj);
                    break;
                case 1:
                    act("{rThe tattoo on your shoulder glows RED.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_dragon_breath, ch.level, ch, ch.fighting, obj);
                    break;
            }
        }
    }


    static void fight_prog_tattoo_odin(OBJ_DATA obj, CHAR_DATA ch) {
        if (get_eq_char(ch, WEAR_TATTOO) == obj) {
            switch (number_bits(5)) {
                case 0:
                    act("{cThe tattoo on your shoulder glows blue.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_cure_critical, ch.level, ch, ch, obj);
                    break;
                case 1:
                    act("{rThe tattoo on your shoulder glows red.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_faerie_fire, ch.level, ch, ch.fighting, obj);
                    break;
            }
        }
    }

    static void fight_prog_tattoo_phobos(OBJ_DATA obj, CHAR_DATA ch) {
        Skill sn;

        if (get_eq_char(ch, WEAR_TATTOO) == obj) {
            switch (number_bits(6)) {
                case 0:
                    act("{cThe tattoo on your shoulder glows blue.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_cure_serious, ch.level, ch, ch, obj);
                    break;
                case 1:
                    if ((sn = lookupSkill("colour spray")) == null) {
                        break;
                    }
                    act("{rThe tattoo on your shoulder glows red.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(sn, ch.level, ch, ch.fighting, obj);
                    break;
            }
        }
    }

    static void fight_prog_tattoo_mars(OBJ_DATA obj, CHAR_DATA ch) {
        if (get_eq_char(ch, WEAR_TATTOO) == obj) {
            switch (number_bits(7)) {
                case 0:
                    obj_cast_spell(gsn_blindness, ch.level, ch, ch.fighting, obj);
                    send_to_char("You send out a cloud of confusion!\n", ch);
                    break;
                case 1:
                    obj_cast_spell(gsn_poison, ch.level, ch, ch.fighting, obj);
                    send_to_char("Some of your insanity rubs off on your opponent.\n", ch);
                    break;
                case 2:
                    obj_cast_spell(gsn_haste, ch.level, ch, ch, obj);
                    send_to_char("You suddenly feel more hyperactive!\n", ch);
                    break;
                case 3:
                    obj_cast_spell(gsn_shield, ch.level, ch, ch, obj);
                    send_to_char("You feel even more paranoid!\n", ch);
                    break;
            }
        }
    }

    static void fight_prog_tattoo_athena(OBJ_DATA obj, CHAR_DATA ch) {
        if (get_eq_char(ch, WEAR_TATTOO) == obj) {
            if (number_percent() < 50) {
                switch (number_bits(4)) {
                    case 0:
                        if (IS_AFFECTED(ch, AFF_BERSERK) || is_affected(ch, gsn_berserk)
                                || is_affected(ch, lookupSkill("frenzy"))) {
                            send_to_char("You get a little madder.\n", ch);
                            return;
                        }

                        AFFECT_DATA af = new AFFECT_DATA();
                        af.where = TO_AFFECTS;
                        af.type = gsn_berserk;
                        af.level = ch.level;
                        af.duration = ch.level / 3;
                        af.modifier = ch.level / 5;
                        af.bitvector = AFF_BERSERK;

                        af.location = APPLY_HITROLL;
                        affect_to_char(ch, af);

                        af.location = APPLY_DAMROLL;
                        affect_to_char(ch, af);

                        af.modifier = 10 * (ch.level / 10);
                        af.location = APPLY_AC;
                        affect_to_char(ch, af);

                        ch.hit += ch.level * 2;
                        ch.hit = UMIN(ch.hit, ch.max_hit);

                        send_to_char("Your pulse races as you are consumned by rage!\n",
                                ch);
                        act("$n gets a wild look in $s eyes.", ch, null, null, TO_ROOM);

                        break;
                }
            } else {
                switch (number_bits(4)) {
                    case 0:
                        do_yell(ch, "Cry Havoc and Let Loose the Dogs of War!");
                        break;
                    case 1:
                        do_yell(ch, "No Mercy!");
                        break;
                    case 2:
                        do_yell(ch, "Los Valdar Cuebiyari!");
                        break;
                    case 3:
                        do_yell(ch, "Carai an Caldazar! Carai an Ellisande! Al Ellisande!");
                        break;
                    case 4:
                        do_yell(ch, "Siempre Vive el Riesgo!");
                        break;
                }
            }
        }
    }


    static void fight_prog_tattoo_hera(OBJ_DATA obj, CHAR_DATA ch) {
        if (get_eq_char(ch, WEAR_TATTOO) == obj) {
            switch (number_bits(5)) {
                case 0:
                    act("{rThe tattoo on your shoulder glows red.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_plague, ch.level, ch, ch.fighting, obj);
                    break;
                case 1:
                    act("{rThe tattoo on your shoulder glows red.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_poison, ch.level, ch, ch.fighting, obj);
                case 2:
                    act("{rThe tattoo on your shoulder glows red.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_weaken, ch.level, ch, ch.fighting, obj);
                case 3:
                    act("{rThe tattoo on your shoulder glows red.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_slow, ch.level, ch, ch.fighting, obj);
                    break;
            }
        }
    }


    static void fight_prog_tattoo_deimos(OBJ_DATA obj, CHAR_DATA ch) {
        Skill sn;
        if (get_eq_char(ch, WEAR_TATTOO) == obj) {
            switch (number_bits(6)) {
                case 0:
                case 1:
                    act("{cThe tattoo on your shoulder glows blue.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_cure_serious, ch.level, ch, ch, obj);
                    break;
                case 2:
                    act("{rThe tattoo on your shoulder glows red.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    sn = lookupSkill("web");
                    spell_web(sn, ch.level, ch, ch.fighting, TARGET_CHAR);
                    break;
            }
        }
    }


    static void fight_prog_tattoo_eros(OBJ_DATA obj, CHAR_DATA ch) {
        Skill sn;
        if (get_eq_char(ch, WEAR_TATTOO) == obj) {
            switch (number_bits(5)) {
                case 0:
                case 1:
                    if ((sn = lookupSkill("heal")) == null) {
                        break;
                    }
                    act("{cThe tattoo on your shoulder glows blue.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(sn, ch.level, ch, ch, obj);
                    break;
                case 2:
                    if ((sn = lookupSkill("mass healing")) == null) {
                        break;
                    }
                    act("{cThe tattoo on your shoulder glows blue.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(sn, ch.level, ch, ch, obj);
                    break;
            }
        }
    }


    boolean death_prog_golden_weapon(OBJ_DATA obj, CHAR_DATA ch) {
        send_to_char("Your golden weapon disappears.\n", ch);
        act("$n's golden weapon disappears.", ch, null, null, TO_ROOM);
        extract_obj(obj);
        ch.hit = 1;
        while (ch.affected != null) {
            affect_remove(ch, ch.affected);
        }
        ch.last_fight_time = -1;
        ch.last_death_time = current_time;
        if (cabal_area_check(ch)) {
            act("$n disappears.", ch, null, null, TO_ROOM);
            char_from_room(ch);
            char_to_room(ch, get_room_index(cabal_table[CABAL_KNIGHT].room_vnum));
            act("$n appears in the room.", ch, null, null, TO_ROOM);
        }
        return true;
    }

    static void fight_prog_golden_weapon(OBJ_DATA obj, CHAR_DATA ch) {
        if (is_wielded_char(ch, obj)) {
            if (number_percent() < 4) {
                act("Your $p glows bright blue!\n", ch, obj, null, TO_CHAR);
                act("$n's $p glows bright blue!", ch, obj, null, TO_ROOM);

                obj_cast_spell(gsn_cure_critical, ch.level, ch, ch, obj);
            } else if (number_percent() > 92) {
                act("Your $p glows bright blue!\n", ch, obj, null, TO_CHAR);
                act("$n's $p glows bright blue!", ch, obj, null, TO_ROOM);

                obj_cast_spell(gsn_cure_serious, ch.level, ch, ch, obj);
            }
        }
    }

    static void get_prog_heart(OBJ_DATA obj, CHAR_DATA ch) {
        if (obj.timer == 0) {
            obj.timer = 24;
        }
    }

    static void fight_prog_snake(OBJ_DATA obj, CHAR_DATA ch) {
        if (is_wielded_char(ch, obj)) {
            switch (number_bits(7)) {
                case 0:
                    act("One of the snake heads on your whip bites $N!", ch, null, ch.fighting, TO_CHAR);
                    act("A snake from $n's whip strikes out and bites you!", ch, null, ch.fighting, TO_VICT);
                    act("One of the snakes from $n's whip strikes at $N!", ch, null, ch.fighting, TO_NOTVICT);
                    obj_cast_spell(gsn_poison, ch.level, ch, ch.fighting, obj);
                    break;
                case 1:
                    act("One of the snake heads on your whip bites $N!", ch, null, ch.fighting, TO_CHAR);
                    act("A snake from $n's whip strikes out and bites you!", ch, null, ch.fighting, TO_VICT);
                    act("One of the snakes from $n's whip strikes at $N!", ch, null, ch.fighting, TO_NOTVICT);
                    obj_cast_spell(gsn_weaken, ch.level, ch, ch.fighting, obj);
                    break;
            }
        }
    }

    static void fight_prog_tattoo_prometheus(OBJ_DATA obj, CHAR_DATA ch) {
        if (get_eq_char(ch, WEAR_TATTOO) == obj) {
            switch (number_bits(5)) {
                case 0:
                    act("{cThe tattoo on your shoulder glows blue.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    obj_cast_spell(gsn_cure_critical, ch.level, ch, ch, obj);
                    break;
                case 1:
                case 2:
                    act("{cThe tattoo on your shoulder glows red.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    if (IS_EVIL(ch.fighting)) {
                        spell_dispel_evil(gsn_dispel_evil, (int) (1.2 * ch.level), ch, ch.fighting, TARGET_CHAR);
                    } else if (IS_GOOD(ch.fighting)) {
                        spell_dispel_good(gsn_dispel_good, (int) (1.2 * ch.level), ch, ch.fighting, TARGET_CHAR);
                    } else {
                        spell_lightning_bolt(Skill.gsn_lightning_bolt, (int) (1.2 * ch.level), ch, ch.fighting, TARGET_CHAR);
                    }
                    break;
            }
        }
    }


    static void fight_prog_shockwave(OBJ_DATA obj, CHAR_DATA ch) {
        if (is_wielded_char(ch, obj)) {
            switch (number_bits(5)) {
                case 0:
                    act("A bolt of lightning arcs out from your bolt, hitting $N!", ch, null, ch.fighting, TO_CHAR);
                    act("A bolt of lightning crackles along $n's bolt and arcs towards you!", ch, null, ch.fighting, TO_VICT);
                    act("A bolt of lightning shoots out from $n's bolt, arcing towards $N!", ch, null, ch.fighting, TO_NOTVICT);
                    obj_cast_spell(gsn_lightning_bolt, ch.level, ch, ch.fighting, null);
                    break;
            }
        }
    }

    static void wear_prog_ranger_staff(OBJ_DATA obj, CHAR_DATA ch) {

        if (get_skill(ch, gsn_ranger_staff) > 0) {
            send_to_char("You don't know to use this thing.\n", ch);
            unequip_char(ch, obj);
            send_to_char("Ranger staff slides off from your hand.\n", ch);
            obj_from_char(obj);
            obj_to_room(obj, ch.in_room);
        }

    }

    static void wear_prog_coconut(OBJ_DATA obj, CHAR_DATA ch) {
        act("You start to bang the coconut shells together.", ch, null, null, TO_CHAR);
        act("You hear a sound like horses galloping and you mount your steed.", ch, null, null, TO_CHAR);
        act("$n pretends to mount an invisible horse.", ch, null, null, TO_ROOM);
    }

    static void entry_prog_coconut(OBJ_DATA obj) {
        if (obj.carried_by != null) {
            if (get_hold_char(obj.carried_by) == obj) {
                act("$n gallops in on his invisible steed, banging two coconuts together.", obj.carried_by, null, null, TO_ROOM);
            }
        }
    }

    static void greet_prog_coconut(OBJ_DATA obj, CHAR_DATA ch) {
        if (obj.carried_by != null) {
            if (get_hold_char(obj.carried_by) == obj && obj.carried_by != ch) {
                act("You hear the sound of galloping horses.", ch, null, null, TO_CHAR);
            }
        } else {
            send_to_char("$p beckons with the faint sound of galloping horses.\n", ch);
        }
    }

    static void get_prog_coconut(OBJ_DATA obj, CHAR_DATA ch) {
        send_to_char("You hold the coconut up to your ear and suddenly you hear the faint\nroar of galloping horses.\n", ch);
        act("$n holds a coconut up to $s ear.", ch, null, null, TO_ROOM);
    }

    static void remove_prog_coconut(OBJ_DATA obj, CHAR_DATA ch) {
        send_to_char("The sounds of horses fade away.\n", ch);
        act("$n pretends to dismount a horse.", ch, null, null, TO_ROOM);
    }

    static void fight_prog_firegauntlets(OBJ_DATA obj, CHAR_DATA ch) {
        int dam;

        if (get_wield_char(ch, false) != null) {
            return;
        }

        if (get_eq_char(ch, WEAR_HANDS) != obj) {
            return;
        }

        if (number_percent() < 50) {
            dam = dice(ch.level, 8) + number_percent() / 2;
            act("Your gauntlets burns $N's face!", ch, null, ch.fighting, TO_CHAR);
            act("$n's gauntlets burns $N's face!", ch, null, ch.fighting, TO_NOTVICT);
            act("$N's gauntlets burns your face!", ch.fighting, null, ch, TO_CHAR);
            damage(ch, ch.fighting, dam / 2, gsn_burning_hands, DAM_FIRE, true);
            if (ch.fighting == null) {
                return;
            }
            fire_effect(ch.fighting, obj.level / 2, dam / 2, TARGET_CHAR);
        }
    }

    static void wear_prog_firegauntlets(OBJ_DATA obj, CHAR_DATA ch) {
        send_to_char("Your hands warm up by the gauntlets.\n", ch);
    }

    static void remove_prog_firegauntlets(OBJ_DATA obj, CHAR_DATA ch) {
        send_to_char("Your hands cool down.\n", ch);
    }

    static void fight_prog_armbands(OBJ_DATA obj, CHAR_DATA ch) {
        int dam;
        if (get_eq_char(ch, WEAR_ARMS) != obj) {
            return;
        }

        if (IS_NPC(ch)) {
            return;
        }

        if (number_percent() < 20) {
            dam = number_percent() / 2 + 30 + 5 * ch.level;
            act("Your armbands burns $N's face!", ch, null, ch.fighting, TO_CHAR);
            act("$n's armbands burns $N's face!", ch, null, ch.fighting, TO_NOTVICT);
            act("$N's armbands burns your face!", ch.fighting, null, ch, TO_CHAR);
            damage(ch, ch.fighting, dam, gsn_burning_hands, DAM_FIRE, true);
            if (ch.fighting == null) {
                return;
            }
            fire_effect(ch.fighting, obj.level / 2, dam, TARGET_CHAR);
        }
    }

    static void wear_prog_armbands(OBJ_DATA obj, CHAR_DATA ch) {
        send_to_char("Your arms warm up by the armbands of the volcanoes.\n", ch);
    }

    static void remove_prog_armbands(OBJ_DATA obj, CHAR_DATA ch) {
        send_to_char("Your arms cool down again.\n", ch);
    }

    static void fight_prog_demonfireshield(OBJ_DATA obj, CHAR_DATA ch) {
        int dam;

        if (get_shield_char(ch) != obj) {
            return;
        }

        if (number_percent() < 15) {
            dam = number_percent() / 2 + 5 * ch.level;
            act("A magical hole appears in your shield !", ch, null, ch.fighting, TO_CHAR);
            act("Your shield burns $N's face!", ch, null, ch.fighting, TO_CHAR);
            act("$n's shield burns $N's face!", ch, null, ch.fighting, TO_NOTVICT);
            act("$N's shield burns your face!", ch.fighting, null, ch, TO_CHAR);
            fire_effect(ch.fighting, obj.level, dam, TARGET_CHAR);
            damage(ch, ch.fighting, dam, gsn_demonfire, DAM_FIRE, true);
        }
    }

    static void wear_prog_demonfireshield(OBJ_DATA obj, CHAR_DATA ch) {
        send_to_char("Your hands warm up by the fire shield.\n", ch);
    }

    static void remove_prog_demonfireshield(OBJ_DATA obj, CHAR_DATA ch) {
        send_to_char("Your hands cool down.\n", ch);
    }

    static void fight_prog_vorbalblade(OBJ_DATA obj, CHAR_DATA ch) {
        CHAR_DATA victim;

        if (IS_NPC(ch)) {
            return;
        }

        if (!is_wielded_char(ch, obj)) {
            return;
        }

        victim = ch.fighting;

        if (!IS_EVIL(victim)) {
            return;
        }

        if (number_percent() < 10) {
            send_to_char("Your weapon swings at your victim's neck without your control!\n", ch);
            if (number_percent() < 20) {
                act("It makes an huge arc in the air, chopping $N's head OFF!", ch, null, victim, TO_CHAR);
                act("$N's weapon whistles in the air, chopping your head OFF!", ch, null, victim, TO_NOTVICT);
                act("$n's weapon whistles in the air, chopping $N's head OFF!", ch, null, victim, TO_ROOM);
                act("$n is DEAD!!", victim, null, null, TO_ROOM);
                act("$n is DEAD!!", victim, null, null, TO_CHAR);
                raw_kill_org(victim, 3);
                send_to_char("You have been KILLED!!\n", victim);
            }
        }
    }

    static void wear_prog_wind_boots(OBJ_DATA obj, CHAR_DATA ch) {

        if (!is_affected(ch, gsn_fly)) {
            send_to_char("As you wear wind boots on your feet, they hold you up.\n", ch);
            send_to_char("You start to fly.\n", ch);
            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_fly;
            af.duration = -2;
            af.level = ch.level;
            af.bitvector = AFF_FLYING;
            af.location = 0;
            af.modifier = 0;
            affect_to_char(ch, af);
        }
    }

    static void remove_prog_wind_boots(OBJ_DATA obj, CHAR_DATA ch) {
        if (is_affected(ch, gsn_fly)) {
            affect_strip(ch, gsn_fly);
            send_to_char("You fall down to the ground.\n", ch);
            send_to_char("Ouch!.\n", ch);
        }
    }

    static void wear_prog_boots_flying(OBJ_DATA obj, CHAR_DATA ch) {

        if (!is_affected(ch, gsn_fly)) {
            send_to_char("As you wear boots of flying on your feet, they hold you up.\n", ch);
            send_to_char("You start to fly.\n", ch);
            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_fly;
            af.duration = -2;
            af.level = ch.level;
            af.bitvector = AFF_FLYING;
            af.location = 0;
            af.modifier = 0;
            affect_to_char(ch, af);
        }
    }

    static void remove_prog_boots_flying(OBJ_DATA obj, CHAR_DATA ch) {
        if (is_affected(ch, gsn_fly)) {
            affect_strip(ch, gsn_fly);
            send_to_char("You fall down to the ground.\n", ch);
            send_to_char("You start to walk again instead of flying!.\n", ch);
        }
    }


    static void wear_prog_arm_hercules(OBJ_DATA obj, CHAR_DATA ch) {

        if (!is_affected(ch, gsn_giant_strength)) {
            send_to_char("As you wear your arms these plates, You feel your self getting stronger.\n", ch);
            send_to_char("Your muscles seems incredibly huge.\n", ch);

            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_giant_strength;
            af.duration = -2;
            af.level = ch.level;
            af.bitvector = 0;
            af.location = APPLY_STR;
            af.modifier = 1 + (ch.level >= 18 ? 1 : 0) + (ch.level >= 30 ? 1 : 0) + (ch.level >= 45 ? 1 : 0);
            affect_to_char(ch, af);
        }
    }

    static void remove_prog_arm_hercules(OBJ_DATA obj, CHAR_DATA ch) {
        if (is_affected(ch, gsn_giant_strength)) {
            affect_strip(ch, gsn_giant_strength);
            send_to_char("Your muscles regain its original value.\n", ch);
        }
    }

    static void wear_prog_girdle_giant(OBJ_DATA obj, CHAR_DATA ch) {

        if (!is_affected(ch, gsn_giant_strength)) {
            send_to_char("As you wear this girdle, You feel your self getting stronger.\n", ch);
            send_to_char("Your muscles seems incredibly huge.\n", ch);

            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_giant_strength;
            af.duration = -2;
            af.level = ch.level;
            af.bitvector = 0;
            af.location = APPLY_STR;
            af.modifier = 1 + (ch.level >= 18 ? 1 : 0) + (ch.level >= 30 ? 1 : 0) + (ch.level >= 45 ? 1 : 0);
            affect_to_char(ch, af);
        }
    }

    static void remove_prog_girdle_giant(OBJ_DATA obj, CHAR_DATA ch) {
        if (is_affected(ch, gsn_giant_strength)) {
            affect_strip(ch, gsn_giant_strength);
            send_to_char("Your muscles regain its original value.\n", ch);
        }
    }

    static void wear_prog_breastplate_strength(OBJ_DATA obj, CHAR_DATA ch) {

        if (!is_affected(ch, gsn_giant_strength)) {
            send_to_char("As you wear breastplate of strength, You feel yourself getting stronger.\n", ch);
            send_to_char("Your muscles seems incredibly huge.\n", ch);

            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_giant_strength;
            af.duration = -2;
            af.level = ch.level;
            af.bitvector = 0;
            af.location = APPLY_STR;
            af.modifier = 1 + (ch.level >= 18 ? 1 : 0) + (ch.level >= 30 ? 1 : 0) + (ch.level >= 45 ? 1 : 0);
            affect_to_char(ch, af);
        }
    }

    static void remove_prog_breastplate_strength(OBJ_DATA obj, CHAR_DATA ch) {
        if (is_affected(ch, gsn_giant_strength)) {
            affect_strip(ch, gsn_giant_strength);
            send_to_char("Your muscles regain its original value.\n", ch);
        }
    }


    static void fight_prog_rose_shield(OBJ_DATA obj, CHAR_DATA ch) {
        if (!((ch.in_room.sector_type != SECT_FIELD) ||
                (ch.in_room.sector_type != SECT_FOREST) ||
                (ch.in_room.sector_type != SECT_MOUNTAIN) ||
                (ch.in_room.sector_type != SECT_HILLS))) {
            return;
        }

        if (get_shield_char(ch) != obj) {
            return;
        }

        if (number_percent() < 90) {
            return;
        }

        send_to_char("The leaves of your shield grows suddenly.\n", ch);
        send_to_char("The leaves of shield surrounds you!.\n", ch.fighting);
        act("$n's shield of rose grows suddenly.", ch, null, null, TO_ROOM);
        obj_cast_spell(gsn_slow, ch.level, ch, ch.fighting, null);
    }

    static void fight_prog_lion_claw(OBJ_DATA obj, CHAR_DATA ch) {
        if (number_percent() < 90) {
            return;
        }

        if (is_wielded_char(ch, obj)) {
            send_to_char("The nails of your claw appears form its fingers.\n", ch);
            act("the nails of $n's claw appears for an instant.", ch, null, null, TO_ROOM, POS_DEAD);
            one_hit(ch, ch.fighting, gsn_x_hit, false);
            one_hit(ch, ch.fighting, gsn_x_hit, false);
            one_hit(ch, ch.fighting, gsn_x_hit, false);
            one_hit(ch, ch.fighting, gsn_x_hit, false);
            send_to_char("The nails of your claw disappears.\n", ch);
            act("the nails of $n's claw disappears suddenly.", ch, null, null, TO_ROOM, POS_DEAD);
        }
    }


    static void speech_prog_ring_ra(OBJ_DATA obj, CHAR_DATA ch, String speech) {

        if (!str_cmp(speech, "punish") && (ch.fighting != null) && is_equiped_char(ch, obj, WEAR_FINGER)) {
            send_to_char("An electrical arc sprays from the ring.\n", ch);
            act("An electrical arc sprays from the ring.", ch, null, null, TO_ROOM);
            obj_cast_spell(gsn_lightning_breath, ch.level, ch, ch.fighting, null);
            WAIT_STATE(ch, 2 * PULSE_VIOLENCE);
        }
    }

    static void wear_prog_eyed_sword(OBJ_DATA obj, CHAR_DATA ch) {
        act("$p's eye opens.", ch, obj, null, TO_CHAR);
        act("$p's eye opens.", ch, obj, null, TO_ROOM);
        if (ch.level <= 10) {
            obj.value[2] = 2;
        } else if (ch.level > 10 && ch.level <= 20) {
            obj.value[2] = 3;
        } else if (ch.level > 20 && ch.level <= 30) {
            obj.value[2] = 4;
        } else if (ch.level > 30 && ch.level <= 40) {
            obj.value[2] = 5;
        } else if (ch.level > 40 && ch.level <= 50) {
            obj.value[2] = 6;
        } else if (ch.level > 50 && ch.level <= 60) {
            obj.value[2] = 7;
        } else if (ch.level > 60 && ch.level <= 70) {
            obj.value[2] = 9;
        } else if (ch.level > 70 && ch.level <= 80) {
            obj.value[2] = 11;
        } else {
            obj.value[2] = 12;
        }
        obj.level = ch.level;
    }

    static void wear_prog_katana_sword(OBJ_DATA obj, CHAR_DATA ch) {
        obj.value[2] = 2;
        if (obj.item_type == ITEM_WEAPON && IS_WEAPON_STAT(obj, WEAPON_KATANA)
                && obj.extra_descr.description.contains(ch.name)) {
            if (ch.level <= 10) {
                obj.value[2] = 2;
            } else if (ch.level > 10 && ch.level <= 20) {
                obj.value[2] = 3;
            } else if (ch.level > 20 && ch.level <= 30) {
                obj.value[2] = 4;
            } else if (ch.level > 30 && ch.level <= 40) {
                obj.value[2] = 5;
            } else if (ch.level > 40 && ch.level <= 50) {
                obj.value[2] = 6;
            } else if (ch.level > 50 && ch.level <= 60) {
                obj.value[2] = 7;
            } else if (ch.level > 60 && ch.level <= 70) {
                obj.value[2] = 9;
            } else if (ch.level > 70 && ch.level <= 80) {
                obj.value[2] = 11;
            } else {
                obj.value[2] = 12;
            }
            obj.level = ch.level;
            send_to_char("You feel your katana like a part of you!\n", ch);
        }
    }

    static void fight_prog_tattoo_goktengri(OBJ_DATA obj, CHAR_DATA ch) {
        if (get_eq_char(ch, WEAR_TATTOO) == obj) {
            switch (number_bits(4)) {
                case 0:
                case 1:
                    act("{WThe tattoo on your shoulder glows white.{x", ch, null, null, TO_CHAR, POS_DEAD);
                    do_say(ch, "My honour is my life.");
                    one_hit(ch, ch.fighting, null, false);
                    break;
            }
        }
    }


    static void wear_prog_snake(OBJ_DATA obj, CHAR_DATA ch) {
        act("{gSnakes of whip starts to breath a poisonous air.{x", ch, obj, null, TO_CHAR, POS_DEAD);
        act("{gSnakes of whip starts to breath a poisonous air.{x", ch, obj, null, TO_ROOM, POS_DEAD);
        if (ch.level > 20 && ch.level <= 30) {
            obj.value[2] = 4;
        } else if (ch.level > 30 && ch.level <= 40) {
            obj.value[2] = 5;
        } else if (ch.level > 40 && ch.level <= 50) {
            obj.value[2] = 6;
        } else if (ch.level > 50 && ch.level <= 60) {
            obj.value[2] = 7;
        } else if (ch.level > 60 && ch.level <= 70) {
            obj.value[2] = 9;
        } else if (ch.level > 70 && ch.level <= 80) {
            obj.value[2] = 11;
        } else {
            obj.value[2] = 12;
        }

    }


    static void remove_prog_snake(OBJ_DATA obj, CHAR_DATA ch) {
        act("{rSnakes of whip slowly melds to non-living skin.{x", ch, obj, null, TO_CHAR, POS_DEAD);
        act("{rSnakes of whip slowy melds to non-living skin.{x", ch, obj, null, TO_ROOM, POS_DEAD);
    }

    static void get_prog_snake(OBJ_DATA obj, CHAR_DATA ch) {
        act("You feel as if snakes of whip moved.", ch, obj, null, TO_CHAR);
    }

    static void wear_prog_fire_shield(OBJ_DATA obj, CHAR_DATA ch) {

        if (obj.extra_descr.description.contains("cold")) {
            if (!is_affected(ch, gsn_fire_shield)) {
                send_to_char("As you wear shield, you become resistive to cold.\n", ch);

                AFFECT_DATA af = new AFFECT_DATA();
                af.where = TO_RESIST;
                af.type = gsn_fire_shield;
                af.duration = -2;
                af.level = ch.level;
                af.bitvector = RES_COLD;
                af.location = 0;
                af.modifier = 0;
                affect_to_char(ch, af);
            }
        } else {
            if (!is_affected(ch, gsn_fire_shield)) {
                send_to_char("As you wear shield, you become resistive to fire.\n", ch);
                AFFECT_DATA af = new AFFECT_DATA();
                af.where = TO_RESIST;
                af.type = gsn_fire_shield;
                af.duration = -2;
                af.level = ch.level;
                af.bitvector = RES_FIRE;
                af.location = 0;
                af.modifier = 0;
                affect_to_char(ch, af);
            }
        }
    }

    static void remove_prog_fire_shield(OBJ_DATA obj, CHAR_DATA ch) {
        if (is_affected(ch, gsn_fire_shield)) {
            affect_strip(ch, gsn_fire_shield);
            if (obj.extra_descr.description.contains("cold")) {
                send_to_char("You have become normal to cold attacks.\n", ch);
            } else {
                send_to_char("You have become normal to fire attacks.\n", ch);
            }
        }
    }


    static void wear_prog_quest_weapon(OBJ_DATA obj, CHAR_DATA ch) {
        if (obj.short_descr.contains(ch.name)) {
            send_to_char("{bYour weapon starts glowing.{x", ch);
            if (ch.level > 20 && ch.level <= 30) {
                obj.value[2] = 4;
            } else if (ch.level > 30 && ch.level <= 40) {
                obj.value[2] = 5;
            } else if (ch.level > 40 && ch.level <= 50) {
                obj.value[2] = 6;
            } else if (ch.level > 50 && ch.level <= 60) {
                obj.value[2] = 7;
            } else if (ch.level > 60 && ch.level <= 70) {
                obj.value[2] = 9;
            } else if (ch.level > 70 && ch.level <= 80) {
                obj.value[2] = 11;
            } else {
                obj.value[2] = 12;
            }
            obj.level = ch.level;
            return;
        }

        act("You are zapped by $p and drop it.", ch, obj, null, TO_CHAR);

        obj_from_char(obj);
        obj_to_room(obj, ch.in_room);
    }


    static void get_prog_quest_reward(OBJ_DATA obj, CHAR_DATA ch) {
        if (obj.short_descr.contains(ch.name)) {
            act("{bYour $p starts glowing.\n{x", ch, obj, null, TO_CHAR, POS_SLEEPING);
            return;
        }
        act("You are zapped by $p and drop it.", ch, obj, null, TO_CHAR);

        obj_from_char(obj);
        obj_to_room(obj, ch.in_room);
    }

    static void fight_prog_ancient_gloves(OBJ_DATA obj, CHAR_DATA ch) {
        int dam;

        if (get_eq_char(ch, WEAR_HANDS) != obj
                || get_wield_char(ch, false) != null) {
            return;
        }

        if (number_percent() < 20) {
            dam = number_percent() + dice(ch.level, 14);
            act("As you touch $N, the flame within your hands blows UP on $N!", ch, null, ch.fighting, TO_CHAR);
            act("As $n touches $N, the flame within $s hands blows UP on $N!", ch, null, ch.fighting, TO_NOTVICT);
            act("As $N touches you, the flame within $S hands blows UP on YOU!", ch.fighting, null, ch, TO_CHAR);
            fire_effect(ch.fighting, obj.level, dam, TARGET_CHAR);
            damage(ch, ch.fighting, dam, gsn_burning_hands, DAM_FIRE, true);
        }
    }


    static void remove_prog_ancient_gloves(OBJ_DATA obj, CHAR_DATA ch) {
        send_to_char("The flame within your hands disappears.\n", ch);
    }

    static void wear_prog_ancient_gloves(OBJ_DATA obj, CHAR_DATA ch) {
        send_to_char("A flame starts to burn within your hands!\n", ch);
    }

    static void fight_prog_ancient_shield(OBJ_DATA obj, CHAR_DATA ch) {
        int chance;
        int dam;

        if (get_shield_char(ch) != obj) {
            return;
        }

        if ((chance = number_percent()) < 5) {
            dam = dice(ch.level, 20);
            act("Your shield SHIMMERS brightly!", ch, null, ch.fighting, TO_CHAR);
            act("$n's shield SHIMMERS brightly!", ch, null, ch.fighting, TO_VICT);
            act("$n's shield SHIMMERS brightly!", ch, null, ch.fighting, TO_NOTVICT);
            fire_effect(ch.fighting, obj.level / 2, dam, TARGET_CHAR);
            damage(ch, ch.fighting, dam, gsn_fire_breath, DAM_FIRE, true);
        } else if (chance < 10) {
            act("Your shield shines with a bright red aura!", ch, null, ch.fighting, TO_CHAR);
            act("$n's shield shine with a bright red aura!", ch, null, ch.fighting, TO_VICT);
            act("$n's shield shines with a bright red aura!", ch, null, ch.fighting, TO_NOTVICT);
            obj_cast_spell(gsn_blindness, ch.level + 5, ch, ch.fighting, obj);
            obj_cast_spell(gsn_slow, ch.level + 5, ch, ch.fighting, obj);
        }
    }

    static void remove_prog_ancient_shield(OBJ_DATA obj, CHAR_DATA ch) {
        act("{rYour shield returns to its original form.{x", ch, obj, null, TO_CHAR, POS_DEAD);
        act("{rYour shield returns to its original form.{x", ch, obj, null, TO_ROOM, POS_DEAD);

    }

    static void wear_prog_ancient_shield(OBJ_DATA obj, CHAR_DATA ch) {
        act("{rYour shield changes its shape and surrounds itself with dragon skin.\n" +
                        "A dragon head gets born on the upper surface of the shield and opens its mouth!{x",
                ch, obj, null, TO_CHAR, POS_DEAD);
        act("{r$n's shield changes its shape and surrounds itself with dragon skin.\n" +
                        "A dragon head gets born on the upper surface of the shield and opens its mouth!{x",
                ch, obj, null, TO_ROOM, POS_DEAD);
    }


    static void wear_prog_neckguard(OBJ_DATA obj, CHAR_DATA ch) {

        if (!is_affected(ch, gsn_neckguard)) {
            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_neckguard;
            af.duration = -2;
            af.level = ch.level;
            af.bitvector = 0;
            af.location = APPLY_NONE;
            af.modifier = 0;
            affect_to_char(ch, af);
        }
    }

    static void remove_prog_neckguard(OBJ_DATA obj, CHAR_DATA ch) {
        if (is_affected(ch, gsn_neckguard)) {
            affect_strip(ch, gsn_neckguard);
        }
    }

    static void wear_prog_headguard(OBJ_DATA obj, CHAR_DATA ch) {

        if (!is_affected(ch, gsn_headguard)) {
            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_headguard;
            af.duration = -2;
            af.level = ch.level;
            af.bitvector = 0;
            af.location = APPLY_NONE;
            af.modifier = 0;
            affect_to_char(ch, af);
        }
    }

    static void remove_prog_headguard(OBJ_DATA obj, CHAR_DATA ch) {
        if (is_affected(ch, gsn_headguard)) {
            affect_strip(ch, gsn_headguard);
        }
    }

    static void wear_prog_blackguard(OBJ_DATA obj, CHAR_DATA ch) {
        if (!is_affected(ch, gsn_blackguard)) {
            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = gsn_blackguard;
            af.duration = -2;
            af.level = ch.level;
            af.bitvector = 0;
            af.location = APPLY_NONE;
            af.modifier = 0;
            affect_to_char(ch, af);
        }
    }

    static void remove_prog_blackguard(OBJ_DATA obj, CHAR_DATA ch) {
        if (is_affected(ch, gsn_blackguard)) {
            affect_strip(ch, gsn_blackguard);
        }
    }

}
