package net.sf.nightworks;

import net.sf.nightworks.util.TextBuffer;

import static net.sf.nightworks.ActComm.add_follower;
import static net.sf.nightworks.ActComm.cabal_area_check;
import static net.sf.nightworks.ActComm.do_yell;
import static net.sf.nightworks.ActComm.is_at_cabal_area;
import static net.sf.nightworks.ActComm.is_same_group;
import static net.sf.nightworks.ActComm.stop_follower;
import static net.sf.nightworks.ActHera.get_random_room;
import static net.sf.nightworks.ActInfo.do_look;
import static net.sf.nightworks.ActMove.do_visible;
import static net.sf.nightworks.ActMove.get_weapon_char;
import static net.sf.nightworks.ActObj.remove_obj;
import static net.sf.nightworks.ActObj.wear_obj;
import static net.sf.nightworks.ActSkill.check_improve;
import static net.sf.nightworks.Comm.act;
import static net.sf.nightworks.Comm.page_to_char;
import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.Const.hometown_table;
import static net.sf.nightworks.Const.liq_table;
import static net.sf.nightworks.DB.bug;
import static net.sf.nightworks.DB.create_mobile;
import static net.sf.nightworks.DB.create_object;
import static net.sf.nightworks.DB.dice;
import static net.sf.nightworks.DB.fBootDb;
import static net.sf.nightworks.DB.get_mob_index;
import static net.sf.nightworks.DB.get_obj_index;
import static net.sf.nightworks.DB.get_room_index;
import static net.sf.nightworks.DB.interpolate;
import static net.sf.nightworks.DB.number_fuzzy;
import static net.sf.nightworks.DB.number_percent;
import static net.sf.nightworks.DB.number_range;
import static net.sf.nightworks.DB.time_info;
import static net.sf.nightworks.DB.weather_info;
import static net.sf.nightworks.Effects.acid_effect;
import static net.sf.nightworks.Effects.cold_effect;
import static net.sf.nightworks.Effects.fire_effect;
import static net.sf.nightworks.Effects.poison_effect;
import static net.sf.nightworks.Effects.shock_effect;
import static net.sf.nightworks.Fight.damage;
import static net.sf.nightworks.Fight.is_safe;
import static net.sf.nightworks.Fight.is_safe_nomessage;
import static net.sf.nightworks.Fight.is_safe_spell;
import static net.sf.nightworks.Fight.multi_hit;
import static net.sf.nightworks.Fight.stop_fighting;
import static net.sf.nightworks.Fight.update_pos;
import static net.sf.nightworks.Handler.add_mind;
import static net.sf.nightworks.Handler.affect_bit_name;
import static net.sf.nightworks.Handler.affect_find;
import static net.sf.nightworks.Handler.affect_join;
import static net.sf.nightworks.Handler.affect_loc_name;
import static net.sf.nightworks.Handler.affect_remove_obj;
import static net.sf.nightworks.Handler.affect_strip;
import static net.sf.nightworks.Handler.affect_to_char;
import static net.sf.nightworks.Handler.affect_to_obj;
import static net.sf.nightworks.Handler.affect_to_room;
import static net.sf.nightworks.Handler.cabal_ok;
import static net.sf.nightworks.Handler.can_drop_obj;
import static net.sf.nightworks.Handler.can_see;
import static net.sf.nightworks.Handler.can_see_obj;
import static net.sf.nightworks.Handler.can_see_room;
import static net.sf.nightworks.Handler.char_from_room;
import static net.sf.nightworks.Handler.char_to_room;
import static net.sf.nightworks.Handler.check_immune;
import static net.sf.nightworks.Handler.cont_bit_name;
import static net.sf.nightworks.Handler.count_charmed;
import static net.sf.nightworks.Handler.extra_bit_name;
import static net.sf.nightworks.Handler.extract_obj;
import static net.sf.nightworks.Handler.get_char_room;
import static net.sf.nightworks.Handler.get_char_spell;
import static net.sf.nightworks.Handler.get_char_world;
import static net.sf.nightworks.Handler.get_curr_stat;
import static net.sf.nightworks.Handler.get_eq_char;
import static net.sf.nightworks.Handler.get_obj_carry;
import static net.sf.nightworks.Handler.get_obj_here;
import static net.sf.nightworks.Handler.get_skill;
import static net.sf.nightworks.Handler.imm_bit_name;
import static net.sf.nightworks.Handler.is_affected;
import static net.sf.nightworks.Handler.is_affected_room;
import static net.sf.nightworks.Handler.is_metal;
import static net.sf.nightworks.Handler.is_name;
import static net.sf.nightworks.Handler.item_type_name;
import static net.sf.nightworks.Handler.obj_from_char;
import static net.sf.nightworks.Handler.obj_from_obj;
import static net.sf.nightworks.Handler.obj_to_char;
import static net.sf.nightworks.Handler.obj_to_room;
import static net.sf.nightworks.Handler.path_to_track;
import static net.sf.nightworks.Handler.skill_failure_nomessage;
import static net.sf.nightworks.Handler.weapon_bit_name;
import static net.sf.nightworks.Nightworks.ACT_AGGRESSIVE;
import static net.sf.nightworks.Nightworks.ACT_NOTRACK;
import static net.sf.nightworks.Nightworks.ACT_UNDEAD;
import static net.sf.nightworks.Nightworks.AFFECT_DATA;
import static net.sf.nightworks.Nightworks.AFF_ABSORB;
import static net.sf.nightworks.Nightworks.AFF_BERSERK;
import static net.sf.nightworks.Nightworks.AFF_BLIND;
import static net.sf.nightworks.Nightworks.AFF_CALM;
import static net.sf.nightworks.Nightworks.AFF_CHARM;
import static net.sf.nightworks.Nightworks.AFF_CORRUPTION;
import static net.sf.nightworks.Nightworks.AFF_CURSE;
import static net.sf.nightworks.Nightworks.AFF_DETECT_EVIL;
import static net.sf.nightworks.Nightworks.AFF_DETECT_GOOD;
import static net.sf.nightworks.Nightworks.AFF_DETECT_HIDDEN;
import static net.sf.nightworks.Nightworks.AFF_DETECT_INVIS;
import static net.sf.nightworks.Nightworks.AFF_DETECT_MAGIC;
import static net.sf.nightworks.Nightworks.AFF_DETECT_UNDEAD;
import static net.sf.nightworks.Nightworks.AFF_EARTHFADE;
import static net.sf.nightworks.Nightworks.AFF_FADE;
import static net.sf.nightworks.Nightworks.AFF_FAERIE_FIRE;
import static net.sf.nightworks.Nightworks.AFF_FLYING;
import static net.sf.nightworks.Nightworks.AFF_GROUNDING;
import static net.sf.nightworks.Nightworks.AFF_HASTE;
import static net.sf.nightworks.Nightworks.AFF_HIDE;
import static net.sf.nightworks.Nightworks.AFF_IMP_INVIS;
import static net.sf.nightworks.Nightworks.AFF_INFRARED;
import static net.sf.nightworks.Nightworks.AFF_INVISIBLE;
import static net.sf.nightworks.Nightworks.AFF_PASS_DOOR;
import static net.sf.nightworks.Nightworks.AFF_PLAGUE;
import static net.sf.nightworks.Nightworks.AFF_POISON;
import static net.sf.nightworks.Nightworks.AFF_PROTECT_EVIL;
import static net.sf.nightworks.Nightworks.AFF_PROTECT_GOOD;
import static net.sf.nightworks.Nightworks.AFF_ROOM_CURSE;
import static net.sf.nightworks.Nightworks.AFF_ROOM_L_SHIELD;
import static net.sf.nightworks.Nightworks.AFF_ROOM_PREVENT;
import static net.sf.nightworks.Nightworks.AFF_ROOM_SHOCKING;
import static net.sf.nightworks.Nightworks.AFF_SANCTUARY;
import static net.sf.nightworks.Nightworks.AFF_SLEEP;
import static net.sf.nightworks.Nightworks.AFF_SLOW;
import static net.sf.nightworks.Nightworks.AFF_SNEAK;
import static net.sf.nightworks.Nightworks.AFF_WEAKEN;
import static net.sf.nightworks.Nightworks.APPLY_AC;
import static net.sf.nightworks.Nightworks.APPLY_DAMROLL;
import static net.sf.nightworks.Nightworks.APPLY_DEX;
import static net.sf.nightworks.Nightworks.APPLY_HITROLL;
import static net.sf.nightworks.Nightworks.APPLY_NONE;
import static net.sf.nightworks.Nightworks.APPLY_ROOM_HEAL;
import static net.sf.nightworks.Nightworks.APPLY_SAVES;
import static net.sf.nightworks.Nightworks.APPLY_SAVING_SPELL;
import static net.sf.nightworks.Nightworks.APPLY_STR;
import static net.sf.nightworks.Nightworks.CABAL_BATTLE;
import static net.sf.nightworks.Nightworks.CABAL_NONE;
import static net.sf.nightworks.Nightworks.CABAL_SHALAFI;
import static net.sf.nightworks.Nightworks.CAN_WEAR;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.DAM_ACID;
import static net.sf.nightworks.Nightworks.DAM_BASH;
import static net.sf.nightworks.Nightworks.DAM_CHARM;
import static net.sf.nightworks.Nightworks.DAM_COLD;
import static net.sf.nightworks.Nightworks.DAM_DISEASE;
import static net.sf.nightworks.Nightworks.DAM_DROWNING;
import static net.sf.nightworks.Nightworks.DAM_ENERGY;
import static net.sf.nightworks.Nightworks.DAM_FIRE;
import static net.sf.nightworks.Nightworks.DAM_HARM;
import static net.sf.nightworks.Nightworks.DAM_HOLY;
import static net.sf.nightworks.Nightworks.DAM_LIGHT;
import static net.sf.nightworks.Nightworks.DAM_LIGHTNING;
import static net.sf.nightworks.Nightworks.DAM_MENTAL;
import static net.sf.nightworks.Nightworks.DAM_NEGATIVE;
import static net.sf.nightworks.Nightworks.DAM_OTHER;
import static net.sf.nightworks.Nightworks.DAM_PIERCE;
import static net.sf.nightworks.Nightworks.DAM_POISON;
import static net.sf.nightworks.Nightworks.DICE_BONUS;
import static net.sf.nightworks.Nightworks.DICE_NUMBER;
import static net.sf.nightworks.Nightworks.DICE_TYPE;
import static net.sf.nightworks.Nightworks.ETHOS_CHAOTIC;
import static net.sf.nightworks.Nightworks.ETHOS_LAWFUL;
import static net.sf.nightworks.Nightworks.ETHOS_NEUTRAL;
import static net.sf.nightworks.Nightworks.IMM_CHARM;
import static net.sf.nightworks.Nightworks.IMM_FIRE;
import static net.sf.nightworks.Nightworks.IMM_MAGIC;
import static net.sf.nightworks.Nightworks.IMM_SUMMON;
import static net.sf.nightworks.Nightworks.IS_AFFECTED;
import static net.sf.nightworks.Nightworks.IS_AWAKE;
import static net.sf.nightworks.Nightworks.IS_EVIL;
import static net.sf.nightworks.Nightworks.IS_GOOD;
import static net.sf.nightworks.Nightworks.IS_IMMORTAL;
import static net.sf.nightworks.Nightworks.IS_IMMUNE;
import static net.sf.nightworks.Nightworks.IS_NEUTRAL;
import static net.sf.nightworks.Nightworks.IS_NPC;
import static net.sf.nightworks.Nightworks.IS_OBJ_STAT;
import static net.sf.nightworks.Nightworks.IS_OUTSIDE;
import static net.sf.nightworks.Nightworks.IS_RAFFECTED;
import static net.sf.nightworks.Nightworks.IS_RESISTANT;
import static net.sf.nightworks.Nightworks.IS_ROOM_AFFECTED;
import static net.sf.nightworks.Nightworks.IS_SET;
import static net.sf.nightworks.Nightworks.IS_VAMPIRE;
import static net.sf.nightworks.Nightworks.IS_VULNERABLE;
import static net.sf.nightworks.Nightworks.IS_WEAPON_STAT;
import static net.sf.nightworks.Nightworks.ITEM_ARMOR;
import static net.sf.nightworks.Nightworks.ITEM_BLESS;
import static net.sf.nightworks.Nightworks.ITEM_BURIED;
import static net.sf.nightworks.Nightworks.ITEM_BURN_PROOF;
import static net.sf.nightworks.Nightworks.ITEM_CONTAINER;
import static net.sf.nightworks.Nightworks.ITEM_DRINK_CON;
import static net.sf.nightworks.Nightworks.ITEM_EVIL;
import static net.sf.nightworks.Nightworks.ITEM_FOOD;
import static net.sf.nightworks.Nightworks.ITEM_GLOW;
import static net.sf.nightworks.Nightworks.ITEM_HUM;
import static net.sf.nightworks.Nightworks.ITEM_INVIS;
import static net.sf.nightworks.Nightworks.ITEM_LIGHT;
import static net.sf.nightworks.Nightworks.ITEM_MAGIC;
import static net.sf.nightworks.Nightworks.ITEM_NODROP;
import static net.sf.nightworks.Nightworks.ITEM_NOLOCATE;
import static net.sf.nightworks.Nightworks.ITEM_NOREMOVE;
import static net.sf.nightworks.Nightworks.ITEM_NOUNCURSE;
import static net.sf.nightworks.Nightworks.ITEM_PILL;
import static net.sf.nightworks.Nightworks.ITEM_POTION;
import static net.sf.nightworks.Nightworks.ITEM_SCROLL;
import static net.sf.nightworks.Nightworks.ITEM_STAFF;
import static net.sf.nightworks.Nightworks.ITEM_TREASURE;
import static net.sf.nightworks.Nightworks.ITEM_WAND;
import static net.sf.nightworks.Nightworks.ITEM_WEAPON;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_BODY;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_HANDS;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_SHIELD;
import static net.sf.nightworks.Nightworks.LEVEL_HERO;
import static net.sf.nightworks.Nightworks.LEVEL_IMMORTAL;
import static net.sf.nightworks.Nightworks.LIQ_WATER;
import static net.sf.nightworks.Nightworks.MAX_SKILL;
import static net.sf.nightworks.Nightworks.MAX_STATS;
import static net.sf.nightworks.Nightworks.MOB_VNUM_ARMOR;
import static net.sf.nightworks.Nightworks.MOB_VNUM_ELM_AIR;
import static net.sf.nightworks.Nightworks.MOB_VNUM_ELM_EARTH;
import static net.sf.nightworks.Nightworks.MOB_VNUM_ELM_FIRE;
import static net.sf.nightworks.Nightworks.MOB_VNUM_ELM_LIGHT;
import static net.sf.nightworks.Nightworks.MOB_VNUM_ELM_WATER;
import static net.sf.nightworks.Nightworks.MOB_VNUM_WEAPON;
import static net.sf.nightworks.Nightworks.MOUNTED;
import static net.sf.nightworks.Nightworks.OBJ_DATA;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_CORPSE_PC;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_DISC;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_GRAVE_STONE;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_LIGHT_BALL;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_MUSHROOM;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_ROSE;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SPRING;
import static net.sf.nightworks.Nightworks.OFF_FAST;
import static net.sf.nightworks.Nightworks.PERS;
import static net.sf.nightworks.Nightworks.PLR_NOCANCEL;
import static net.sf.nightworks.Nightworks.PLR_NOSUMMON;
import static net.sf.nightworks.Nightworks.POS_DEAD;
import static net.sf.nightworks.Nightworks.POS_FIGHTING;
import static net.sf.nightworks.Nightworks.POS_SLEEPING;
import static net.sf.nightworks.Nightworks.POS_STANDING;
import static net.sf.nightworks.Nightworks.PULSE_VIOLENCE;
import static net.sf.nightworks.Nightworks.REMOVE_BIT;
import static net.sf.nightworks.Nightworks.ROOM_INDEX_DATA;
import static net.sf.nightworks.Nightworks.ROOM_NOSUMMON;
import static net.sf.nightworks.Nightworks.ROOM_NO_MAGIC;
import static net.sf.nightworks.Nightworks.ROOM_NO_RECALL;
import static net.sf.nightworks.Nightworks.ROOM_PRIVATE;
import static net.sf.nightworks.Nightworks.ROOM_SAFE;
import static net.sf.nightworks.Nightworks.ROOM_SOLITARY;
import static net.sf.nightworks.Nightworks.SECT_AIR;
import static net.sf.nightworks.Nightworks.SECT_WATER_NOSWIM;
import static net.sf.nightworks.Nightworks.SECT_WATER_SWIM;
import static net.sf.nightworks.Nightworks.SET_BIT;
import static net.sf.nightworks.Nightworks.SEX_FEMALE;
import static net.sf.nightworks.Nightworks.SIZE_HUGE;
import static net.sf.nightworks.Nightworks.SIZE_LARGE;
import static net.sf.nightworks.Nightworks.SIZE_MEDIUM;
import static net.sf.nightworks.Nightworks.SIZE_SMALL;
import static net.sf.nightworks.Nightworks.SIZE_TINY;
import static net.sf.nightworks.Nightworks.SKY_RAINING;
import static net.sf.nightworks.Nightworks.STAT_CON;
import static net.sf.nightworks.Nightworks.STAT_DEX;
import static net.sf.nightworks.Nightworks.STAT_INT;
import static net.sf.nightworks.Nightworks.STAT_STR;
import static net.sf.nightworks.Nightworks.TARGET_CHAR;
import static net.sf.nightworks.Nightworks.TARGET_NONE;
import static net.sf.nightworks.Nightworks.TARGET_OBJ;
import static net.sf.nightworks.Nightworks.TARGET_ROOM;
import static net.sf.nightworks.Nightworks.TAR_CHAR_DEFENSIVE;
import static net.sf.nightworks.Nightworks.TAR_CHAR_OFFENSIVE;
import static net.sf.nightworks.Nightworks.TAR_CHAR_SELF;
import static net.sf.nightworks.Nightworks.TAR_IGNORE;
import static net.sf.nightworks.Nightworks.TAR_OBJ_CHAR_DEF;
import static net.sf.nightworks.Nightworks.TAR_OBJ_CHAR_OFF;
import static net.sf.nightworks.Nightworks.TAR_OBJ_INV;
import static net.sf.nightworks.Nightworks.TO_AFFECTS;
import static net.sf.nightworks.Nightworks.TO_ALL;
import static net.sf.nightworks.Nightworks.TO_CHAR;
import static net.sf.nightworks.Nightworks.TO_IMMUNE;
import static net.sf.nightworks.Nightworks.TO_NOTVICT;
import static net.sf.nightworks.Nightworks.TO_OBJECT;
import static net.sf.nightworks.Nightworks.TO_RESIST;
import static net.sf.nightworks.Nightworks.TO_ROOM;
import static net.sf.nightworks.Nightworks.TO_ROOM_AFFECTS;
import static net.sf.nightworks.Nightworks.TO_ROOM_CONST;
import static net.sf.nightworks.Nightworks.TO_VICT;
import static net.sf.nightworks.Nightworks.TO_VULN;
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
import static net.sf.nightworks.Nightworks.WEAPON_VAMPIRIC;
import static net.sf.nightworks.Nightworks.WEAPON_VORPAL;
import static net.sf.nightworks.Nightworks.WEAPON_WHIP;
import static net.sf.nightworks.Nightworks.WEAR_FLOAT;
import static net.sf.nightworks.Nightworks.WEAR_NONE;
import static net.sf.nightworks.Nightworks.char_list;
import static net.sf.nightworks.Nightworks.current_time;
import static net.sf.nightworks.Nightworks.exit;
import static net.sf.nightworks.Nightworks.object_list;
import static net.sf.nightworks.Nightworks.sprintf;
import static net.sf.nightworks.Skill.find_spell;
import static net.sf.nightworks.Skill.gsn_absorb;
import static net.sf.nightworks.Skill.gsn_blindness;
import static net.sf.nightworks.Skill.gsn_curse;
import static net.sf.nightworks.Skill.gsn_deafen;
import static net.sf.nightworks.Skill.gsn_doppelganger;
import static net.sf.nightworks.Skill.gsn_garble;
import static net.sf.nightworks.Skill.gsn_imp_invis;
import static net.sf.nightworks.Skill.gsn_invis;
import static net.sf.nightworks.Skill.gsn_mass_invis;
import static net.sf.nightworks.Skill.gsn_mastering_spell;
import static net.sf.nightworks.Skill.gsn_plague;
import static net.sf.nightworks.Skill.gsn_poison;
import static net.sf.nightworks.Skill.gsn_shielding;
import static net.sf.nightworks.Skill.gsn_sneak;
import static net.sf.nightworks.Skill.gsn_spell_craft;
import static net.sf.nightworks.Skill.gsn_spellbane;
import static net.sf.nightworks.Skill.lookupSkill;
import static net.sf.nightworks.Update.gain_exp;
import static net.sf.nightworks.util.TextUtils.capitalize;
import static net.sf.nightworks.util.TextUtils.one_argument;
import static net.sf.nightworks.util.TextUtils.str_cmp;

class Magic {

/*
 * Lookup a skill by slot number.
 * Used for object loading.
 */

    static int slot_lookup_skill_num(int slot) {
        if (slot >= 0) {

            for (Skill sn : Skill.skills) {
                if (slot == sn.slot) {
                    return sn.ordinal();
                }
            }
        }
        if (fBootDb) {
            bug("Slot_lookup: bad slot %d.", slot);
            exit(1);
        }
        return -1;
    }

    /**
     * Utter mystical words for an sn.
     */
    static class syl_type {
        String _old;
        String _new;

        public syl_type(String _old, String _new) {
            this._old = _old;
            this._new = _new;
        }
    }

    static final syl_type[] syl_table = new syl_type[]{
            new syl_type(" ", " "),
            new syl_type("ar", "abra"),
            new syl_type("au", "kada"),
            new syl_type("bless", "fido"),
            new syl_type("blind", "nose"),
            new syl_type("bur", "mosa"),
            new syl_type("cu", "judi"),
            new syl_type("de", "oculo"),
            new syl_type("en", "unso"),
            new syl_type("light", "dies"),
            new syl_type("lo", "hi"),
            new syl_type("mor", "zak"),
            new syl_type("move", "sido"),
            new syl_type("ness", "lacri"),
            new syl_type("ning", "illa"),
            new syl_type("per", "duda"),
            new syl_type("ra", "gru"),
            new syl_type("fresh", "ima"),
            new syl_type("re", "candus"),
            new syl_type("son", "sabru"),
            new syl_type("tect", "infra"),
            new syl_type("tri", "cula"),
            new syl_type("ven", "nofo"),
            new syl_type("a", "a"), new syl_type("b", "b"), new syl_type("c", "q"), new syl_type("d", "e"),
            new syl_type("e", "z"), new syl_type("f", "y"), new syl_type("g", "o"), new syl_type("h", "p"),
            new syl_type("i", "u"), new syl_type("j", "y"), new syl_type("k", "t"), new syl_type("l", "r"),
            new syl_type("m", "w"), new syl_type("n", "i"), new syl_type("o", "a"), new syl_type("p", "s"),
            new syl_type("q", "d"), new syl_type("r", "f"), new syl_type("s", "g"), new syl_type("t", "h"),
            new syl_type("u", "j"), new syl_type("v", "z"), new syl_type("w", "x"), new syl_type("x", "n"),
            new syl_type("y", "l"), new syl_type("z", "k"),
            new syl_type("", "")
    };

    static void say_spell(CHAR_DATA ch, Skill sn) {
        CHAR_DATA rch;
        int iSyl;
        int length = 0;
        int skill;

        TextBuffer buf = new TextBuffer();
        for (int pos = 0; pos < sn.name.length(); pos += length) {
            for (iSyl = 0; iSyl < syl_table.length; iSyl++) {
                String prefix = syl_table[iSyl]._old;
                int len = prefix.length();
                if (sn.name.regionMatches(pos, prefix, 0, len)) {
                    buf.append(syl_table[iSyl]._new);
                    length = len;
                    break;
                }
            }
            assert (length > 0);
        }

        TextBuffer buf2 = new TextBuffer();
        buf.sprintf("$n utters the words, '%s'.", buf);
        buf.sprintf("$n utters the words, '%s'.", sn.name);

        for (rch = ch.in_room.people; rch != null; rch = rch.next_in_room) {
            if (rch != ch) {
                skill = (get_skill(rch, gsn_spell_craft) * 9) / 10;
                if (skill < number_percent()) {
                    act(buf2, ch, null, rch, TO_VICT);
                    check_improve(rch, gsn_spell_craft, true, 5);
                } else {
                    act(buf.toString(), ch, null, rch, TO_VICT);
                    check_improve(rch, gsn_spell_craft, true, 5);
                }
            }
        }
    }

/*
 * Compute a saving throw.
 * Negative apply's make saving throw better.
 */

    static boolean saves_spell(int level, CHAR_DATA victim, int dam_type) {
        int save;

        save = 40 + (victim.level - level) * 4 - (victim.saving_throw * 90) / UMAX(45, victim.level);

        if (IS_AFFECTED(victim, AFF_BERSERK)) {
            save += victim.level / 5;
        }

        switch (check_immune(victim, dam_type)) {
            case IS_IMMUNE:
                return true;
            case IS_RESISTANT:
                save += victim.level / 5;
                break;
            case IS_VULNERABLE:
                save -= victim.level / 5;
                break;
        }

        if (!IS_NPC(victim) && victim.clazz.fMana) {
            save = 9 * save / 10;
        }
        save = URANGE(5, save, 95);
        return number_percent() < save;
    }

/* RT configuration smashed */

    static boolean saves_dispel(int dis_level, int spell_level, int duration) {
        int save;

        /* impossible to dispel permanent effects */
        if (duration == -2) {
            return true;
        }
        if (duration == -1) {
            spell_level += 5;
        }

        save = 50 + (spell_level - dis_level) * 5;
        save = URANGE(5, save, 95);
        return number_percent() < save;
    }

/* co-routine for dispel magic and cancellation */

    static boolean check_dispel(int dis_level, CHAR_DATA victim, Skill sn) {

        if (is_affected(victim, sn)) {
            for (AFFECT_DATA af = victim.affected; af != null; af = af.next) {
                if (af.type == sn) {
                    if (!saves_dispel(dis_level, af.level, af.duration)) {
                        affect_strip(victim, sn);
                        if (sn.msg_off != null) {
                            send_to_char(sn.msg_off, victim);
                            send_to_char("\n", victim);
                        }
                        return true;
                    } else {
                        af.level--;
                    }
                }
            }
        }
        return false;
    }

    /**
     * for finding mana costs -- temporary version
     */
    static int mana_cost(CHAR_DATA ch, int min_mana, int level) {
        if (ch.level + 2 == level) {
            return 1000;
        }
        return UMAX(min_mana, (100 / (2 + ch.level - level)));
    }

    /**
     * for casting different rooms
     * returned value is the range
     */
    static int allowed_other(CHAR_DATA ch, Skill sn) {
        if (sn.minimum_position == POS_STANDING || sn.skill_level[ch.clazz.id] < 26
                || sn == find_spell(ch, "chain lightning")) {
            return 0;
        } else {
            return sn.skill_level[ch.clazz.id] / 10;
        }
    }

    /*
     * The kludgy global is for spells who want more stuff from command line.
     */
    static String target_name;
    static int door[] = new int[]{0};

    static void do_cast(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        OBJ_DATA obj;
        Object vo;
        int mana;
        Skill sn;
        int target;
        int cast_far = 0, range;

        /*
         * Switched NPC's can cast spells, but others can't.
         */
        if (IS_NPC(ch) && ch.desc == null) {
            return;
        }

        if (is_affected(ch, gsn_shielding)) {
            send_to_char("You reach for the true Source and feel something stopping you.\n", ch);
            return;
        }

        if (is_affected(ch, gsn_garble) || is_affected(ch, gsn_deafen)) {
            send_to_char("You can't get the right intonations.\n", ch);
            return;
        }

        StringBuilder arg1 = new StringBuilder();
        StringBuilder arg2 = new StringBuilder();
        String target_name = one_argument(argument, arg1);
        one_argument(target_name, arg2);

        if (arg1.length() == 0) {
            send_to_char("Cast which what where?\n", ch);
            return;
        }

        if (ch.cabal == CABAL_BATTLE && !IS_IMMORTAL(ch)) {
            send_to_char("You are a BattleRager, not a filthy magician!\n", ch);
            return;
        }

        if ((sn = find_spell(ch, arg1.toString())) == null || skill_failure_nomessage(ch, sn, 1) != 0) {
            send_to_char("You don't know any spells of that name.\n", ch);
            return;
        }

        if (ch.clazz == Clazz.VAMPIRE && !IS_VAMPIRE(ch) && sn.cabal == CABAL_NONE) {
            send_to_char("You must transform to vampire before casting!\n", ch);
            return;
        }

        if (!sn.isSpell()) {
            send_to_char("That's not a spell.\n", ch);
            return;
        }

        if (ch.position < sn.minimum_position) {
            send_to_char("You can't concentrate enough.\n", ch);
            return;
        }

        if (!cabal_ok(ch, sn)) {
            return;
        }

        if (IS_SET(ch.in_room.room_flags, ROOM_NO_MAGIC)) {
            send_to_char("Your spell fizzles out and fails.\n", ch);
            act("$n's spell fizzles out and fails.", ch, null, null, TO_ROOM);
            return;
        }

        if (ch.level + 2 == sn.skill_level[ch.clazz.id]) {
            mana = 50;
        } else {
            mana = UMAX(
                    sn.min_mana,
                    100 / (2 + ch.level - sn.skill_level[ch.clazz.id]));
        }

        /*
         * Locate targets.
         */
        victim = null;
        vo = null;
        target = TARGET_NONE;

        switch (sn.target) {
            default:
                bug("Do_cast: bad target for sn %d.", sn.ordinal());
                return;

            case TAR_IGNORE:
                if (is_affected(ch, gsn_spellbane)) {
                    WAIT_STATE(ch, sn.beats);
                    act("Your spellbane deflects the spell!", ch, null, null, TO_CHAR);
                    act("$n's spellbane deflects the spell!", ch, null, null, TO_ROOM);
                    check_improve(ch, gsn_spellbane, true, 1);
                    damage(ch, ch, 3 * ch.level, gsn_spellbane, DAM_NEGATIVE, true);
                    return;
                }
                break;

            case TAR_CHAR_OFFENSIVE:
                if (arg2.length() == 0) {
                    if ((victim = ch.fighting) == null) {
                        send_to_char("Cast the spell on whom?\n", ch);
                        return;
                    }
                } else {
                    if ((range = allowed_other(ch, sn)) > 0) {
                        if ((victim = get_char_spell(ch, target_name, door, range)) == null) {
                            return;
                        }

                        if (victim.in_room != ch.in_room
                                && ((IS_NPC(victim) && IS_SET(victim.act, ACT_NOTRACK))
                                || is_at_cabal_area(ch) || is_at_cabal_area(victim))) {
                            act("You can't cast this spell to $N at this distance.",
                                    ch, null, victim, TO_CHAR);
                            return;
                        }

                        cast_far = 1;
                    } else if ((victim = get_char_room(ch, target_name)) == null) {
                        send_to_char("They aren't here.\n", ch);
                        return;
                    }
                }

                if (!IS_NPC(ch) && is_safe(ch, victim)) {
                    return;
                }
/*
    if ( IS_AFFECTED(ch, AFF_CHARM) && ch.master == victim )
    {
        send_to_char( "You can't do that on your own follower.\n",
        ch );
        return;
    }
*/
                vo = victim;
                target = TARGET_CHAR;
                if (!IS_NPC(ch) && victim != ch &&
                        ch.fighting != victim && victim.fighting != ch &&
                        (IS_SET(victim.affected_by, AFF_CHARM) || !IS_NPC(victim))) {
                    if (!can_see(victim, ch)) {
                        do_yell(victim, "Help someone is attacking me!");
                    } else {
                        TextBuffer buf = new TextBuffer();
                        buf.sprintf("Die, %s, you sorcerous dog!",
                                (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(victim)) ? ch.doppel.name : ch.name);
                        do_yell(victim, buf.toString());
                    }
                }
                if (is_affected(victim, gsn_spellbane) &&
                        (number_percent() < 2 * get_skill(victim, gsn_spellbane) / 3)
                        && sn != Skill.gsn_mental_knife && sn != Skill.gsn_lightning_breath) {
                    WAIT_STATE(ch, sn.beats);
                    if (ch == victim) {
                        act("Your spellbane deflects the spell!", ch, null, null, TO_CHAR);
                        act("$n's spellbane deflects the spell!", ch, null, null, TO_ROOM);
                        check_improve(victim, gsn_spellbane, true, 1);
                        damage(ch, ch, 3 * ch.level, gsn_spellbane, DAM_NEGATIVE, true);
                    } else {
                        act("$N deflects your spell!", ch, null, victim, TO_CHAR);
                        act("You deflect $n's spell!", ch, null, victim, TO_VICT);
                        act("$N deflects $n's spell!", ch, null, victim, TO_NOTVICT);
                        check_improve(victim, gsn_spellbane, true, 1);
                        damage(victim, ch, 3 * victim.level, gsn_spellbane, DAM_NEGATIVE, true);
                        multi_hit(victim, ch, null);
                    }
                    return;
                }
                if (ch != victim && IS_AFFECTED(victim, AFF_ABSORB) &&
                        (number_percent() < 2 * get_skill(victim, gsn_absorb) / 3)
                        && sn != Skill.gsn_mental_knife && sn != Skill.gsn_lightning_breath) {
                    act("Your spell fails to pass $N's energy field!", ch, null, victim, TO_CHAR);
                    act("You absorb $n's spell!", ch, null, victim, TO_VICT);
                    act("$N absorbs $n's spell!", ch, null, victim, TO_NOTVICT);
                    check_improve(victim, gsn_absorb, true, 1);
                    victim.mana += mana;
                    return;
                }
                break;

            case TAR_CHAR_DEFENSIVE:
                if (arg2.length() == 0) {
                    victim = ch;
                } else {
                    if ((victim = get_char_room(ch, target_name)) == null) {
                        send_to_char("They aren't here.\n", ch);
                        return;
                    }
                }

                vo = victim;
                target = TARGET_CHAR;
                if (is_affected(victim, gsn_spellbane)) {
                    WAIT_STATE(ch, sn.beats);
                    if (ch == victim) {
                        act("Your spellbane deflects the spell!", ch, null, null, TO_CHAR);
                        act("$n's spellbane deflects the spell!", ch, null, null, TO_ROOM);
                        check_improve(victim, gsn_spellbane, true, 1);
                        damage(victim, ch, 3 * victim.level, gsn_spellbane, DAM_NEGATIVE, true);
                    } else {
                        act("$N deflects your spell!", ch, null, victim, TO_CHAR);
                        act("You deflect $n's spell!", ch, null, victim, TO_VICT);
                        act("$N deflects $n's spell!", ch, null, victim, TO_NOTVICT);
                        check_improve(victim, gsn_spellbane, true, 1);
                        damage(victim, ch, 3 * victim.level, gsn_spellbane, DAM_NEGATIVE, true);
                    }
                    return;
                }
                if (ch != victim && IS_AFFECTED(victim, AFF_ABSORB) &&
                        (number_percent() < 2 * get_skill(victim, gsn_absorb) / 3)) {
                    act("Your spell fails to pass $N's energy field!", ch, null, victim, TO_CHAR);
                    act("You absorb $n's spell!", ch, null, victim, TO_VICT);
                    act("$N absorbs $n's spell!", ch, null, victim, TO_NOTVICT);
                    check_improve(victim, gsn_absorb, true, 1);
                    victim.mana += mana;
                    return;
                }
                break;

            case TAR_CHAR_SELF:
                if (arg2.length() != 0 && !is_name(target_name, ch.name)) {
                    send_to_char("You cannot cast this spell on another.\n", ch);
                    return;
                }

                vo = ch;
                target = TARGET_CHAR;

                if (is_affected(ch, gsn_spellbane)) {
                    WAIT_STATE(ch, sn.beats);
                    act("Your spellbane deflects the spell!", ch, null, null, TO_CHAR);
                    act("$n's spellbane deflects the spell!", ch, null, null, TO_ROOM);
                    check_improve(ch, gsn_spellbane, true, 1);
                    damage(ch, ch, 3 * ch.level, gsn_spellbane, DAM_NEGATIVE, true);
                    return;
                }

                break;

            case TAR_OBJ_INV:
                if (arg2.length() == 0) {
                    send_to_char("What should the spell be cast upon?\n", ch);
                    return;
                }

                if ((obj = get_obj_carry(ch, target_name)) == null) {
                    send_to_char("You are not carrying that.\n", ch);
                    return;
                }

                vo = obj;
                target = TARGET_OBJ;
                if (is_affected(ch, gsn_spellbane)) {
                    WAIT_STATE(ch, sn.beats);
                    act("Your spellbane deflects the spell!", ch, null, null, TO_CHAR);
                    act("$n's spellbane deflects the spell!", ch, null, null, TO_ROOM);
                    check_improve(ch, gsn_spellbane, true, 1);
                    damage(ch, ch, 3 * ch.level, gsn_spellbane, DAM_NEGATIVE, true);
                    return;
                }
                break;

            case TAR_OBJ_CHAR_OFF:
                if (arg2.length() == 0) {
                    if ((victim = ch.fighting) == null) {
                        send_to_char("Cast the spell on whom or what?\n", ch);
                        return;
                    }

                    target = TARGET_CHAR;
                } else if ((victim = get_char_room(ch, target_name)) != null) {
                    target = TARGET_CHAR;
                }

                if (target == TARGET_CHAR) /* check the sanity of the attack */ {
                    if (is_safe_spell(ch, victim, false) && victim != ch) {
                        send_to_char("Your spell didn't work.\n", ch);
                        return;
                    }

                    if (IS_AFFECTED(ch, AFF_CHARM) && ch.master == victim) {
                        send_to_char("You can't do that on your own follower.\n",
                                ch);
                        return;
                    }

                    if (is_safe(ch, victim)) {
                        return;
                    }

                    vo = victim;
                } else if ((obj = get_obj_here(ch, target_name)) != null) {
                    vo = obj;
                    target = TARGET_OBJ;
                } else {
                    send_to_char("You don't see that here.\n", ch);
                    return;
                }
                break;

            case TAR_OBJ_CHAR_DEF:
                if (arg2.length() == 0) {
                    vo = ch;
                    target = TARGET_CHAR;
                } else if ((victim = get_char_room(ch, target_name)) != null) {
                    vo = victim;
                    target = TARGET_CHAR;
                } else if ((obj = get_obj_carry(ch, target_name)) != null) {
                    vo = obj;
                    target = TARGET_OBJ;
                } else {
                    send_to_char("You don't see that here.\n", ch);
                    return;
                }
                break;
        }

        if (!IS_NPC(ch) && ch.mana < mana) {
            send_to_char("You don't have enough mana.\n", ch);
            return;
        }

        if (str_cmp(sn.name, "ventriloquate")) {
            say_spell(ch, sn);
        }

        WAIT_STATE(ch, sn.beats);

        if (number_percent() > get_skill(ch, sn)) {
            send_to_char("You lost your concentration.\n", ch);
            check_improve(ch, sn, false, 1);
            ch.mana -= mana / 2;
            if (cast_far != 0) {
                cast_far = 2;
            }
        } else {
            int slevel;

            if (ch.clazz.fMana) {
                slevel = ch.level - UMAX(0, (ch.level / 20));
            } else {
                slevel = ch.level - UMAX(5, (ch.level / 10));
            }

            if (sn.cabal != CABAL_NONE) {
                slevel = ch.level;
            }

            if (ch.level > gsn_spell_craft.skill_level[ch.clazz.id]) {
                if (number_percent() < get_skill(ch, gsn_spell_craft)) {
                    slevel = ch.level;
                    check_improve(ch, gsn_spell_craft, true, 1);
                }
                check_improve(ch, gsn_spell_craft, false, 1);
            }

            if (ch.cabal == CABAL_SHALAFI &&
                    ch.level > gsn_mastering_spell.skill_level[ch.clazz.id]
                    && cabal_ok(ch, gsn_mastering_spell)) {
                if (number_percent() < get_skill(ch, gsn_mastering_spell)) {
                    slevel += number_range(1, 4);
                    check_improve(ch, gsn_mastering_spell, true, 1);
                }
            }

            ch.mana -= mana;
            if (get_curr_stat(ch, STAT_INT) > 21) {
                slevel = UMAX(1, (slevel + (get_curr_stat(ch, STAT_INT) - 21)));
            } else {
                slevel = UMAX(1, slevel);
            }

            if (IS_NPC(ch)) {
                sn.spell_fun(ch.level, ch, vo, target);
            } else {
                sn.spell_fun(slevel, ch, vo, target);
            }
            check_improve(ch, sn, true, 1);
        }

        if (cast_far == 1 && door[0] != -1) {
            path_to_track(ch, victim, door[0]);
        } else if ((sn.target == TAR_CHAR_OFFENSIVE || (sn.target == TAR_OBJ_CHAR_OFF && target == TARGET_CHAR)) && victim != ch && victim.master != ch) {
            CHAR_DATA vch;
            CHAR_DATA vch_next;

            for (vch = ch.in_room.people; vch != null; vch = vch_next) {
                vch_next = vch.next_in_room;
                if (victim == vch && victim.fighting == null) {
                    if (victim.position != POS_SLEEPING) {
                        multi_hit(victim, ch, null);
                    }

                    break;
                }
            }
        }

    }

    /**
     * Cast spells at targets using a magical object.
     */
    static void obj_cast_spell(Skill sn, int level, CHAR_DATA ch, CHAR_DATA victim, OBJ_DATA obj) {
        Object vo = null;
        int target = TARGET_NONE;

        if (sn == null) {
            return;
        }

        if (!sn.isSpell()) {
            bug("Obj_cast_spell: bad sn %d.", sn.ordinal());
            return;
        }

        if ((IS_NPC(ch) && ch.position == POS_DEAD) ||
                (!IS_NPC(ch) && (current_time - ch.last_death_time) < 10)) {
            bug("Obj_cast_spell: Ch is dead! But it is ok.", sn.ordinal());
            return;
        }

        if (victim != null) {
            if ((IS_NPC(victim) && victim.position == POS_DEAD) ||
                    (!IS_NPC(victim) && (current_time - victim.last_death_time) < 10)) {
                bug("Obj_cast_spell: Victim is dead! But it is ok.. ", sn.ordinal());
                return;
            }
        }

        switch (sn.target) {
            default:
                bug("Obj_cast_spell: bad target for sn %d.", sn.ordinal());
                return;

            case TAR_IGNORE:
                vo = null;
                break;

            case TAR_CHAR_OFFENSIVE:
                if (victim == null) {
                    victim = ch.fighting;
                }
                if (victim == null) {
                    send_to_char("You can't do that.\n", ch);
                    return;
                }
                if (is_safe(ch, victim) && ch != victim) {
                    send_to_char("Something isn't right...\n", ch);
                    return;
                }
                vo = victim;
                target = TARGET_CHAR;
                if (is_affected(victim, gsn_spellbane) && (/*IS_NPC(victim) ||*/
                        number_percent() < 2 * get_skill(victim, gsn_spellbane) / 3)) {
                    if (ch == victim) {
                        act("Your spellbane deflects the spell!", ch, null, null, TO_CHAR);
                        act("$n's spellbane deflects the spell!", ch, null, null, TO_ROOM);
                        check_improve(victim, gsn_spellbane, true, 1);
                        damage(victim, ch, 10 * level, gsn_spellbane, DAM_NEGATIVE, true);
                    } else {
                        act("$N deflects your spell!", ch, null, victim, TO_CHAR);
                        act("You deflect $n's spell!", ch, null, victim, TO_VICT);
                        act("$N deflects $n's spell!", ch, null, victim, TO_NOTVICT);
                        check_improve(victim, gsn_spellbane, true, 1);
                        damage(victim, ch, 10 * victim.level, gsn_spellbane, DAM_NEGATIVE, true);
                    }
                    return;
                }

                if (ch != victim && IS_AFFECTED(victim, AFF_ABSORB) &&
                        (number_percent() < 2 * get_skill(victim, gsn_absorb) / 3)
                        && sn != Skill.gsn_mental_knife && sn != Skill.gsn_lightning_breath) {
                    act("Your spell fails to pass $N's energy field!", ch, null, victim, TO_CHAR);
                    act("You absorb $n's spell!", ch, null, victim, TO_VICT);
                    act("$N absorbs $n's spell!", ch, null, victim, TO_NOTVICT);
                    check_improve(victim, gsn_absorb, true, 1);
                    victim.mana += sn.min_mana;
                    return;
                }
                break;

            case TAR_CHAR_DEFENSIVE:
            case TAR_CHAR_SELF:
                if (victim == null) {
                    victim = ch;
                }
                vo = victim;
                target = TARGET_CHAR;
                if (is_affected(victim, gsn_spellbane)) {
                    if (ch == victim) {
                        act("Your spellbane deflects the spell!", ch, null, null, TO_CHAR);
                        act("$n's spellbane deflects the spell!", ch, null, null, TO_ROOM);
                        check_improve(victim, gsn_spellbane, true, 1);
                        damage(victim, ch, 10 * victim.level, gsn_spellbane, DAM_NEGATIVE, true);
                    } else {
                        act("$N deflects your spell!", ch, null, victim, TO_CHAR);
                        act("You deflect $n's spell!", ch, null, victim, TO_VICT);
                        act("$N deflects $n's spell!", ch, null, victim, TO_NOTVICT);
                        check_improve(victim, gsn_spellbane, true, 1);
                        damage(victim, ch, 10 * victim.level, gsn_spellbane, DAM_NEGATIVE, true);
                    }
                    return;
                }
                break;

            case TAR_OBJ_INV:
                if (obj == null) {
                    send_to_char("You can't do that.\n", ch);
                    return;
                }
                vo = obj;
                target = TARGET_OBJ;
                if (is_affected(ch, gsn_spellbane)) {
                    act("Your spellbane deflects the spell!", ch, null, null, TO_CHAR);
                    act("$n's spellbane deflects the spell!", ch, null, null, TO_ROOM);
                    check_improve(ch, gsn_spellbane, true, 1);
                    damage(ch, ch, 3 * ch.level, gsn_spellbane, DAM_NEGATIVE, true);
                    return;
                }

                break;

            case TAR_OBJ_CHAR_OFF:
                if (victim == null && obj == null) {
                    if (ch.fighting != null) {
                        victim = ch.fighting;
                    } else {
                        send_to_char("You can't do that.\n", ch);
                        return;
                    }

                    if (victim != null) {
                        if (is_safe_spell(ch, victim, false) && ch != victim) {
                            send_to_char("Somehting isn't right...\n", ch);
                            return;
                        }

                        vo = victim;
                        target = TARGET_CHAR;
                    } else {
                        vo = obj;
                        target = TARGET_OBJ;
                    }
                }
                break;


            case TAR_OBJ_CHAR_DEF:
                if (victim == null && obj == null) {
                    vo = ch;
                    target = TARGET_CHAR;
                } else if (victim != null) {
                    vo = victim;
                    target = TARGET_CHAR;
                } else {
                    vo = obj;
                    target = TARGET_OBJ;
                }

                break;
        }

        target_name = "";
        sn.spell_fun(level, ch, vo, target);

        if ((sn.target == TAR_CHAR_OFFENSIVE || (sn.target == TAR_OBJ_CHAR_OFF && target == TARGET_CHAR)) && victim != ch && victim.master != ch) {
            CHAR_DATA vch;
            CHAR_DATA vch_next;

            for (vch = ch.in_room.people; vch != null; vch = vch_next) {
                vch_next = vch.next_in_room;
                if (victim == vch && victim.fighting == null) {
                    multi_hit(victim, ch, null);
                    break;
                }
            }
        }
    }

/*
* Spell functions.
*/

    static void spell_acid_blast(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 18);
        if (saves_spell(level, victim, DAM_ACID)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_ACID, true);
    }


    static void spell_armor(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(victim, sn)) {
            if (victim == ch) {
                send_to_char("You are already armored.\n", ch);
            } else {
                act("$N is already armored.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 7 + level / 6;
        af.modifier = -1 * UMAX(20, 10 + level / 4); /* af.modifier  = -20;*/
        af.location = APPLY_AC;
        af.bitvector = 0;
        affect_to_char(victim, af);
        send_to_char("You feel someone protecting you.\n", victim);
        if (ch != victim) {
            act("$N is protected by your magic.", ch, null, victim, TO_CHAR);
        }
    }


    static void spell_bless(Skill sn, int level, CHAR_DATA ch, Object vo, int target) {
        CHAR_DATA victim;
        OBJ_DATA obj;

        /* deal with the object case first */
        if (target == TARGET_OBJ) {
            obj = (OBJ_DATA) vo;
            if (IS_OBJ_STAT(obj, ITEM_BLESS)) {
                act("$p is already blessed.", ch, obj, null, TO_CHAR);
                return;
            }

            if (IS_OBJ_STAT(obj, ITEM_EVIL)) {

                AFFECT_DATA paf = affect_find(obj.affected, gsn_curse);
                if (!saves_dispel(level, paf != null ? paf.level : obj.level, 0)) {
                    if (paf != null) {
                        affect_remove_obj(obj, paf);
                    }
                    act("$p glows a pale blue.", ch, obj, null, TO_ALL);
                    obj.extra_flags = REMOVE_BIT(obj.extra_flags, ITEM_EVIL);
                    return;
                } else {
                    act("The evil of $p is too powerful for you to overcome.",
                            ch, obj, null, TO_CHAR);
                    return;
                }
            }
            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_OBJECT;
            af.type = sn;
            af.level = level;
            af.duration = (6 + level / 2);
            af.location = APPLY_SAVES;
            af.modifier = -1;
            af.bitvector = ITEM_BLESS;
            affect_to_obj(obj, af);

            act("$p glows with a holy aura.", ch, obj, null, TO_ALL);
            return;
        }

        /* character target */
        victim = (CHAR_DATA) vo;


        if (victim.position == POS_FIGHTING || is_affected(victim, sn)) {
            if (victim == ch) {
                send_to_char("You are already blessed.\n", ch);
            } else {
                act("$N already has divine favor.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = (6 + level / 2);
        af.location = APPLY_HITROLL;
        af.modifier = level / 8;
        af.bitvector = 0;
        affect_to_char(victim, af);

        af.location = APPLY_SAVING_SPELL;
        af.modifier = 0 - level / 8;
        affect_to_char(victim, af);
        send_to_char("You feel righteous.\n", victim);
        if (ch != victim) {
            act("You grant $N the favor of your god.", ch, null, victim, TO_CHAR);
        }
    }


    static void spell_blindness(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_BLIND) ||
                saves_spell(level, victim, DAM_OTHER)) {
            send_to_char("You failed.\n", ch);
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.location = APPLY_HITROLL;
        af.modifier = -4;
        af.duration = 3 + level / 15;
        af.bitvector = AFF_BLIND;
        affect_to_char(victim, af);
        send_to_char("You are blinded!\n", victim);
        act("$n appears to be blinded.", victim, null, null, TO_ROOM);
    }


    static void spell_burning_hands(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 2) + 7;
        if (saves_spell(level, victim, DAM_FIRE)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_FIRE, true);
    }


    static void spell_call_lightning(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA vch;
        CHAR_DATA vch_next;
        int dam;

        if (!IS_OUTSIDE(ch)) {
            send_to_char("You must be out of doors.\n", ch);
            return;
        }

        if (weather_info.sky < SKY_RAINING) {
            send_to_char("You need bad weather.\n", ch);
            return;
        }

        dam = dice(level, 14);

        send_to_char("Gods' lightning strikes your foes!\n", ch);
        act("$n calls lightning to strike $s foes!", ch, null, null, TO_ROOM);

        for (vch = char_list; vch != null; vch = vch_next) {
            vch_next = vch.next;
            if (vch.in_room == null) {
                continue;
            }
            if (vch == ch) {
                continue;
            }
            if (vch.in_room == ch.in_room) {
                if (!is_same_group(ch, vch)) {
                    if (is_safe(ch, vch)) {
                        continue;
                    }
                }

                if (IS_AFFECTED(vch, AFF_GROUNDING)) {
                    send_to_char("The electricity fizzles at your foes.\n", vch);
                    act("A lightning bolt fizzles at $N's foes.\n", ch, null, vch, TO_ROOM);
                    continue;
                }

                if (saves_spell(level, vch, DAM_LIGHTNING)) {
                    dam /= 2;
                }
                damage(ch, vch, dam, sn, DAM_LIGHTNING, true);
                continue;
            }

            if (vch.in_room.area == ch.in_room.area
                    && IS_OUTSIDE(vch)
                    && IS_AWAKE(vch)) {
                send_to_char("Lightning flashes in the sky.\n", vch);
            }
        }

    }

/* RT calm spell stops all fighting in the room */

    static void spell_calm(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA vch;
        int mlevel = 0;
        int count = 0;
        int high_level = 0;
        int chance;

        /* get sum of all mobile levels in the room */
        for (vch = ch.in_room.people; vch != null; vch = vch.next_in_room) {
            if (vch.position == POS_FIGHTING) {
                count++;
                if (IS_NPC(vch)) {
                    mlevel += vch.level;
                } else {
                    mlevel += vch.level / 2;
                }
                high_level = UMAX(high_level, vch.level);
            }
        }

        /* compute chance of stopping combat */
        chance = 4 * level - high_level + 2 * count;

        if (IS_IMMORTAL(ch)) /* always works */ {
            mlevel = 0;
        }

        if (number_range(0, chance) >= mlevel)  /* hard to stop large fights */ {
            for (vch = ch.in_room.people; vch != null; vch = vch.next_in_room) {
                if (IS_NPC(vch) && (IS_SET(vch.imm_flags, IMM_MAGIC) || IS_SET(vch.act, ACT_UNDEAD))) {
                    return;
                }

                if (IS_AFFECTED(vch, AFF_CALM) || IS_AFFECTED(vch, AFF_BERSERK) || is_affected(vch, Skill.gsn_frenzy)) {
                    return;
                }

                send_to_char("A wave of calm passes over you.\n", vch);

                if (vch.fighting != null || vch.position == POS_FIGHTING) {
                    stop_fighting(vch, false);
                }

                AFFECT_DATA af = new AFFECT_DATA();
                af.where = TO_AFFECTS;
                af.type = sn;
                af.level = level;
                af.duration = level / 4;
                af.location = APPLY_HITROLL;
                if (!IS_NPC(vch)) {
                    af.modifier = -5;
                } else {
                    af.modifier = -2;
                }
                af.bitvector = AFF_CALM;
                affect_to_char(vch, af);

                af.location = APPLY_DAMROLL;
                affect_to_char(vch, af);
            }
        }
    }

    static void spell_cancellation(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        boolean found = false;

        level += 2;

        if ((!IS_NPC(ch) && IS_NPC(victim) &&
                !(IS_AFFECTED(ch, AFF_CHARM) && ch.master == victim)) ||
                (IS_NPC(ch) && !IS_NPC(victim))) {
            send_to_char("You failed, try dispel magic.\n", ch);
            return;
        }

        if (!is_same_group(ch, victim) && ch != victim
                && (IS_NPC(victim) || IS_SET(victim.act, PLR_NOCANCEL))) {
            act("You cannot cast this spell to $N.", ch, null, victim, TO_CHAR);
            return;
        }

        /* unlike dispel magic, the victim gets NO save */

        /* begin running through the spells */

        if (check_dispel(level, victim, Skill.gsn_armor)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_enhanced_armor)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_bless)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_blindness)) {
            found = true;
            act("$n is no longer blinded.", victim, null, null, TO_ROOM);
        }

        if (check_dispel(level, victim, Skill.gsn_calm)) {
            found = true;
            act("$n no longer looks so peaceful...", victim, null, null, TO_ROOM);
        }

        if (check_dispel(level, victim, Skill.lookupSkill("change sex"))) {
            found = true;
            act("$n looks more like $mself again.", victim, null, null, TO_ROOM);
        }

        if (check_dispel(level, victim, Skill.gsn_charm_person)) {
            found = true;
            act("$n regains $s free will.", victim, null, null, TO_ROOM);
        }

        if (check_dispel(level, victim, Skill.gsn_chill_touch)) {
            found = true;
            act("$n looks warmer.", victim, null, null, TO_ROOM);
        }

        if (check_dispel(level, victim, Skill.gsn_curse)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_detect_evil)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_detect_good)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_detect_hidden)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_detect_invis)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_detect_hidden)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_detect_magic)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_faerie_fire)) {
            act("$n's outline fades.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_fly)) {
            act("$n falls to the ground!", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_frenzy)) {
            act("$n no longer looks so wild.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_giant_strength)) {
            act("$n no longer looks so mighty.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_haste)) {
            act("$n is no longer moving so quickly.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_infravision)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_invis)) {
            act("$n fades into existance.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_mass_invis)) {
            act("$n fades into existance.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_pass_door)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_protection_evil)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_protection_good)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_sanctuary)) {
            act("The white aura around $n's body vanishes.",
                    victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_shield)) {
            act("The shield protecting $n vanishes.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_sleep)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_slow)) {
            act("$n is no longer moving so slowly.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_stone_skin)) {
            act("$n's skin regains its normal texture.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_weaken)) {
            act("$n looks stronger.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_shielding)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_web)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_fear)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_protection_heat)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_protection_cold)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_magic_resistance)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_hallucination)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_terangreal)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_power_stun)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_corruption)) {
            act("$n looks healthier.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_web)) {
            act("The webs around $n dissolve.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (found) {
            send_to_char("Ok.\n", ch);
        } else {
            send_to_char("Spell failed.\n", ch);
        }
    }

    static void spell_cause_light(Skill sn, int level, CHAR_DATA ch, Object vo) {
        damage(ch, (CHAR_DATA) vo, dice(1, 8) + level / 3, sn, DAM_HARM, true);
    }


    static void spell_cause_critical(Skill sn, int level, CHAR_DATA ch, Object vo) {
        damage(ch, (CHAR_DATA) vo, dice(3, 8) + level - 6, sn, DAM_HARM, true);
    }


    static void spell_cause_serious(Skill sn, int level, CHAR_DATA ch, Object vo) {
        damage(ch, (CHAR_DATA) vo, dice(2, 8) + level / 2, sn, DAM_HARM, true);
    }

    static void spell_chain_lightning(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        CHAR_DATA tmp_vict, last_vict, next_vict;
        boolean found;
        int dam;

        /* first strike */

        act("A lightning bolt leaps from $n's hand and arcs to $N.", ch, null, victim, TO_ROOM);
        act("A lightning bolt leaps from your hand and arcs to $N.", ch, null, victim, TO_CHAR);
        act("A lightning bolt leaps from $n's hand and hits you!", ch, null, victim, TO_VICT);

        dam = dice(level, 6);

        if (IS_AFFECTED(victim, AFF_GROUNDING)) {
            send_to_char("The electricity fizzles at your foes.\n", victim);
            act("A lightning bolt fizzles at $N's foes.\n", ch, null, victim, TO_ROOM);
        } else {
            if (saves_spell(level, victim, DAM_LIGHTNING)) {
                dam /= 3;
            }
            damage(ch, victim, dam, sn, DAM_LIGHTNING, true);
        }

        if (!IS_NPC(ch) && victim != ch && ch.fighting != victim && victim.fighting != ch && (IS_SET(victim.affected_by, AFF_CHARM) || !IS_NPC(victim))) {
            if (!can_see(victim, ch)) {
                do_yell(victim, "Help someone is attacking me!");
            } else {
                TextBuffer buf = new TextBuffer();
                buf.sprintf("Die, %s, you sorcerous dog!",
                        (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(victim)) ? ch.doppel.name : ch.name);
                do_yell(victim, buf.toString());
            }
        }


        last_vict = victim;
        level -= 4;   /* decrement damage */

        /* new targets */
        while (level > 0) {
            found = false;
            for (tmp_vict = ch.in_room.people; tmp_vict != null; tmp_vict = next_vict) {
                next_vict = tmp_vict.next_in_room;
                if (!is_safe_spell(ch, tmp_vict, true) && tmp_vict != last_vict) {
                    found = true;
                    last_vict = tmp_vict;
                    if (is_safe(ch, tmp_vict)) {
                        act("The bolt passes around $n's body.", ch, null, null, TO_ROOM);
                        act("The bolt passes around your body.", ch, null, null, TO_CHAR);
                    } else {
                        act("The bolt arcs to $n!", tmp_vict, null, null, TO_ROOM);
                        act("The bolt hits you!", tmp_vict, null, null, TO_CHAR);
                        dam = dice(level, 6);

                        if (!IS_NPC(ch) && tmp_vict != ch &&
                                ch.fighting != tmp_vict && tmp_vict.fighting != ch &&
                                (IS_SET(tmp_vict.affected_by, AFF_CHARM) || !IS_NPC(tmp_vict))) {
                            if (!can_see(tmp_vict, ch)) {
                                do_yell(tmp_vict, "Help someone is attacking me!");
                            } else {
                                TextBuffer buf = new TextBuffer();
                                buf.sprintf("Die, %s, you sorcerous dog!",
                                        (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(tmp_vict)) ? ch.doppel.name : ch.name);
                                do_yell(tmp_vict, buf.toString());
                            }
                        }

                        if (IS_AFFECTED(tmp_vict, AFF_GROUNDING)) {
                            send_to_char("The electricity fizzles at your foes.\n", tmp_vict);
                            act("A lightning bolt fizzles at $N's foes.\n", ch, null, tmp_vict, TO_ROOM);
                        } else {
                            if (saves_spell(level, tmp_vict, DAM_LIGHTNING)) {
                                dam /= 3;
                            }
                            damage(ch, tmp_vict, dam, sn, DAM_LIGHTNING, true);
                        }
                        level -= 4;  /* decrement damage */
                    }
                }
            }   /* end target searching loop */

            if (!found) /* no target found, hit the caster */ {
                if (last_vict == ch) /* no double hits */ {
                    act("The bolt seems to have fizzled out.", ch, null, null, TO_ROOM);
                    act("The bolt grounds out through your body.", ch, null, null, TO_CHAR);
                    return;
                }

                last_vict = ch;
                act("The bolt arcs to $n...whoops!", ch, null, null, TO_ROOM);
                send_to_char("You are struck by your own lightning!\n", ch);
                dam = dice(level, 6);

                if (IS_AFFECTED(ch, AFF_GROUNDING)) {
                    send_to_char("The electricity fizzles at your foes.\n", ch);
                    act("A lightning bolt fizzles at $N's foes.\n", ch, null, ch, TO_ROOM);
                } else {
                    if (saves_spell(level, ch, DAM_LIGHTNING)) {
                        dam /= 3;
                    }
                    damage(ch, ch, dam, sn, DAM_LIGHTNING, true);
                }
                level -= 4;  /* decrement damage */
            }
            /* now go back and find more targets */
        }
    }


    static void spell_healing_light(Skill sn, int level, CHAR_DATA ch) {


        if (is_affected_room(ch.in_room, sn)) {
            send_to_char("This room has already been healed by light.\n", ch);
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_ROOM_CONST;
        af.type = sn;
        af.level = level;
        af.duration = level / 25;
        af.location = APPLY_ROOM_HEAL;
        af.modifier = level;
        af.bitvector = 0;
        affect_to_room(ch.in_room, af);

        AFFECT_DATA af2 = new AFFECT_DATA();
        af2.where = TO_AFFECTS;
        af2.type = sn;
        af2.level = level;
        af2.duration = level / 10;
        af2.modifier = 0;
        af2.location = APPLY_NONE;
        af2.bitvector = 0;
        affect_to_char(ch, af2);
        send_to_char("The room starts to be filled with healing light.\n", ch);
        act("The room starts to be filled with $n's healing light.", ch, null, null, TO_ROOM);
    }


    static void spell_charm_person(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_safe(ch, victim)) {
            return;
        }

        if (count_charmed(ch) != 0) {
            return;
        }

        if (victim == ch) {
            send_to_char("You like yourself even better!\n", ch);
            return;
        }

        if (IS_AFFECTED(victim, AFF_CHARM)
                || IS_AFFECTED(ch, AFF_CHARM)
                || (ch.sex != SEX_FEMALE && level < victim.level)
                || (ch.sex == SEX_FEMALE && level < (victim.level - 2))
                || IS_SET(victim.imm_flags, IMM_CHARM)
                || saves_spell(level, victim, DAM_CHARM)
                || (IS_NPC(victim) && victim.pIndexData.pShop != null)) {
            return;
        }

        if (victim.master != null) {
            stop_follower(victim);
        }
        add_follower(victim, ch);
        victim.leader = ch;
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = number_fuzzy(level / 5);
        af.location = 0;
        af.modifier = 0;
        af.bitvector = AFF_CHARM;
        affect_to_char(victim, af);
        act("Isn't $n just so nice?", ch, null, victim, TO_VICT);
        act("$N looks at you with adoring eyes.", ch, null, victim, TO_CHAR);

        if (IS_NPC(victim) && !IS_NPC(ch)) {
            if (number_percent() < (4 + (victim.level - ch.level)) * 10) {
                add_mind(victim, ch.name);
            } else if (victim.in_mind == null) {
                victim.in_mind = String.valueOf(victim.in_room.vnum);
            }
        }
    }


    static void spell_chill_touch(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = number_range(1, level);
        if (!saves_spell(level, victim, DAM_COLD)) {
            act("$n turns blue and shivers.", victim, null, null, TO_ROOM);
            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = sn;
            af.level = level;
            af.duration = 6;
            af.location = APPLY_STR;
            af.modifier = -1;
            af.bitvector = 0;
            affect_join(victim, af);
        } else {
            dam /= 2;
        }

        damage(ch, victim, dam, sn, DAM_COLD, true);
    }


    static void spell_colour_spray(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 3) + 13;
        if (saves_spell(level, victim, DAM_LIGHT)) {
            dam /= 2;
        } else {
            spell_blindness(Skill.gsn_blindness, level / 2, ch, victim);
        }

        damage(ch, victim, dam, sn, DAM_LIGHT, true);

    }


    static void spell_continual_light(CHAR_DATA ch) {
        OBJ_DATA light;

        if (target_name.length() != 0)  /* do a glow on some object */ {
            light = get_obj_carry(ch, target_name);

            if (light == null) {
                send_to_char("You don't see that here.\n", ch);
                return;
            }

            if (IS_OBJ_STAT(light, ITEM_GLOW)) {
                act("$p is already glowing.", ch, light, null, TO_CHAR);
                return;
            }

            light.extra_flags = SET_BIT(light.extra_flags, ITEM_GLOW);
            act("$p glows with a white light.", ch, light, null, TO_ALL);
            return;
        }

        light = create_object(get_obj_index(OBJ_VNUM_LIGHT_BALL), 0);
        obj_to_room(light, ch.in_room);
        act("$n twiddles $s thumbs and $p appears.", ch, light, null, TO_ROOM);
        act("You twiddle your thumbs and $p appears.", ch, light, null, TO_CHAR);
    }


    static void spell_control_weather(int level, CHAR_DATA ch) {
        if (!str_cmp(target_name, "better")) {
            weather_info.change += dice(level / 3, 4);
        } else if (!str_cmp(target_name, "worse")) {
            weather_info.change -= dice(level / 3, 4);
        } else {
            send_to_char("Do you want it to get better or worse?\n", ch);
            return;
        }

        send_to_char("Ok.\n", ch);
    }


    static void spell_create_food(int level, CHAR_DATA ch) {
        OBJ_DATA mushroom;

        mushroom = create_object(get_obj_index(OBJ_VNUM_MUSHROOM), 0);
        mushroom.value[0] = level / 2;
        mushroom.value[1] = level;
        obj_to_room(mushroom, ch.in_room);
        act("$p suddenly appears.", ch, mushroom, null, TO_ROOM);
        act("$p suddenly appears.", ch, mushroom, null, TO_CHAR);
    }

    static void spell_create_rose(CHAR_DATA ch) {
        OBJ_DATA rose;
        rose = create_object(get_obj_index(OBJ_VNUM_ROSE), 0);
        act("$n has created a beautiful red rose.", ch, rose, null, TO_ROOM);
        send_to_char("You create a beautiful red rose.\n", ch);
        obj_to_char(rose, ch);
    }

    static void spell_create_spring(int level, CHAR_DATA ch) {
        OBJ_DATA spring;

        spring = create_object(get_obj_index(OBJ_VNUM_SPRING), 0);
        spring.timer = level;
        obj_to_room(spring, ch.in_room);
        act("$p flows from the ground.", ch, spring, null, TO_ROOM);
        act("$p flows from the ground.", ch, spring, null, TO_CHAR);
    }


    static void spell_create_water(int level, CHAR_DATA ch, Object vo) {
        OBJ_DATA obj = (OBJ_DATA) vo;
        int water;

        if (obj.item_type != ITEM_DRINK_CON) {
            send_to_char("It is unable to hold water.\n", ch);
            return;
        }

        if (obj.value[2] != LIQ_WATER && obj.value[1] != 0) {
            send_to_char("It contains some other liquid.\n", ch);
            return;
        }

        water = UMIN(
                level * (weather_info.sky >= SKY_RAINING ? 4 : 2),
                obj.value[0] - obj.value[1]
        );

        if (water > 0) {
            obj.value[2] = LIQ_WATER;
            obj.value[1] += water;
            if (!is_name("water", obj.name)) {
                TextBuffer buf = new TextBuffer();
                buf.sprintf("%s water", obj.name);
                obj.name = buf.toString();
            }
            act("$p is filled.", ch, obj, null, TO_CHAR);
        }

    }


    static void spell_cure_blindness(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (!is_affected(victim, gsn_blindness)) {
            if (victim == ch) {
                send_to_char("You aren't blind.\n", ch);
            } else {
                act("$N doesn't appear to be blinded.", ch, null, victim, TO_CHAR);
            }
            return;
        }

        if (check_dispel(level, victim, gsn_blindness)) {
            send_to_char("Your vision returns!\n", victim);
            act("$n is no longer blinded.", victim, null, null, TO_ROOM);
        } else {
            send_to_char("Spell failed.\n", ch);
        }
    }


    static void spell_cure_critical(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int heal;

        heal = dice(3, 8) + level / 2;
        victim.hit = UMIN(victim.hit + heal, victim.max_hit);
        update_pos(victim);
        send_to_char("You feel better!\n", victim);
        if (ch != victim) {
            send_to_char("Ok.\n", ch);
        }
    }

/* RT added to cure plague */

    static void spell_cure_disease(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (!is_affected(victim, gsn_plague)) {
            if (victim == ch) {
                send_to_char("You aren't ill.\n", ch);
            } else {
                act("$N doesn't appear to be diseased.", ch, null, victim, TO_CHAR);
            }
            return;
        }

        if (check_dispel(level, victim, gsn_plague)) {
            send_to_char("Your sores vanish.\n", victim);
            act("$n looks relieved as $s sores vanish.", victim, null, null, TO_ROOM);
        } else {
            send_to_char("Spell failed.\n", ch);
        }
    }


    static void spell_cure_light(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int heal;

        heal = dice(1, 8) + level / 4 + 5;
        victim.hit = UMIN(victim.hit + heal, victim.max_hit);
        update_pos(victim);
        send_to_char("You feel better!\n", victim);
        if (ch != victim) {
            send_to_char("Ok.\n", ch);
        }
    }


    static void spell_cure_poison(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (!is_affected(victim, gsn_poison)) {
            if (victim == ch) {
                send_to_char("You aren't poisoned.\n", ch);
            } else {
                act("$N doesn't appear to be poisoned.", ch, null, victim, TO_CHAR);
            }
            return;
        }

        if (check_dispel(level, victim, gsn_poison)) {
            send_to_char("A warm feeling runs through your body.\n", victim);
            act("$n looks much better.", victim, null, null, TO_ROOM);
        } else {
            send_to_char("Spell failed.\n", ch);
        }
    }

    static void spell_cure_serious(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int heal;

        heal = dice(2, 8) + level / 3 + 10;
        victim.hit = UMIN(victim.hit + heal, victim.max_hit);
        update_pos(victim);
        send_to_char("You feel better!\n", victim);
        if (ch != victim) {
            send_to_char("Ok.\n", ch);
        }
    }


    static void spell_curse(Skill sn, int level, CHAR_DATA ch, Object vo, int target) {
        CHAR_DATA victim;
        OBJ_DATA obj;

        /* deal with the object case first */
        if (target == TARGET_OBJ) {
            obj = (OBJ_DATA) vo;

            if (obj.wear_loc != WEAR_NONE) {
                act("You must remove $p first.", ch, obj, null, TO_CHAR);
                return;
            }

            if (IS_OBJ_STAT(obj, ITEM_EVIL)) {
                act("$p is already filled with evil.", ch, obj, null, TO_CHAR);
                return;
            }

            if (IS_OBJ_STAT(obj, ITEM_BLESS)) {
                AFFECT_DATA paf = affect_find(obj.affected, Skill.gsn_bless);
                if (!saves_dispel(level, paf != null ? paf.level : obj.level, 0)) {
                    if (paf != null) {
                        affect_remove_obj(obj, paf);
                    }
                    act("$p glows with a red aura.", ch, obj, null, TO_ALL);
                    obj.extra_flags = REMOVE_BIT(obj.extra_flags, ITEM_BLESS);
                    return;
                } else {
                    act("The holy aura of $p is too powerful for you to overcome.",
                            ch, obj, null, TO_CHAR);
                    return;
                }
            }
            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_OBJECT;
            af.type = sn;
            af.level = level;
            af.duration = (8 + level / 5);
            af.location = APPLY_SAVES;
            af.modifier = +1;
            af.bitvector = ITEM_EVIL;
            affect_to_obj(obj, af);

            act("$p glows with a malevolent aura.", ch, obj, null, TO_ALL);
            return;
        }

        /* character curses */
        victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_CURSE) || saves_spell(level, victim, DAM_NEGATIVE)) {
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = (8 + level / 10);
        af.location = APPLY_HITROLL;
        af.modifier = -1 * (level / 8);
        af.bitvector = AFF_CURSE;
        affect_to_char(victim, af);

        af.location = APPLY_SAVING_SPELL;
        af.modifier = level / 8;
        affect_to_char(victim, af);

        send_to_char("You feel unclean.\n", victim);
        if (ch != victim) {
            act("$N looks very uncomfortable.", ch, null, victim, TO_CHAR);
        }
    }

/* RT replacement demonfire spell */

    static void spell_demonfire(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        if (!IS_NPC(ch) && !IS_EVIL(ch)) {
            victim = ch;
            send_to_char("The demons turn upon you!\n", ch);
        }

        if (victim != ch) {
            act("$n calls forth the demons of Hell upon $N!",
                    ch, null, victim, TO_ROOM);
            act("$n has assailed you with the demons of Hell!",
                    ch, null, victim, TO_VICT);
            send_to_char("You conjure forth the demons of hell!\n", ch);
        }
        dam = dice(level, 10);
        if (saves_spell(level, victim, DAM_NEGATIVE)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_NEGATIVE, true);
        spell_curse(gsn_curse, 3 * level / 4, ch, victim, TARGET_CHAR);
    }

/* added by chronos */

    static void spell_bluefire(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        if (!IS_NPC(ch) && !IS_NEUTRAL(ch)) {
            victim = ch;
            send_to_char("Your blue fire turn upon you!\n", ch);
        }

        if (victim != ch) {
            act("$n calls forth the blue fire of earth $N!",
                    ch, null, victim, TO_ROOM);
            act("$n has assailed you with the neutrals of earth!",
                    ch, null, victim, TO_VICT);
            send_to_char("You conjure forth the blue fire!\n", ch);
        }

        dam = dice(level, 10);
        if (saves_spell(level, victim, DAM_FIRE)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_FIRE, true);
    }


    static void spell_detect_evil(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_DETECT_EVIL)) {
            if (victim == ch) {
                send_to_char("You can already sense evil.\n", ch);
            } else {
                act("$N can already detect evil.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = (5 + level / 3);
        af.modifier = 0;
        af.location = APPLY_NONE;
        af.bitvector = AFF_DETECT_EVIL;
        affect_to_char(victim, af);
        send_to_char("Your eyes tingle.\n", victim);
        if (ch != victim) {
            send_to_char("Ok.\n", ch);
        }
    }


    static void spell_detect_good(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_DETECT_GOOD)) {
            if (victim == ch) {
                send_to_char("You can already sense good.\n", ch);
            } else {
                act("$N can already detect good.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = (5 + level / 3);
        af.modifier = 0;
        af.location = APPLY_NONE;
        af.bitvector = AFF_DETECT_GOOD;
        affect_to_char(victim, af);
        send_to_char("Your eyes tingle.\n", victim);
        if (ch != victim) {
            send_to_char("Ok.\n", ch);
        }
    }


    static void spell_detect_hidden(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;


        if (IS_AFFECTED(victim, AFF_DETECT_HIDDEN)) {
            if (victim == ch) {
                send_to_char("You are already as alert as you can be. \n", ch);
            } else {
                act("$N can already sense hidden lifeforms.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = (5 + level / 3);
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_DETECT_HIDDEN;
        affect_to_char(victim, af);
        send_to_char("Your awareness improves.\n", victim);
        if (ch != victim) {
            send_to_char("Ok.\n", ch);
        }
    }


    static void spell_detect_invis(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;


        if (IS_AFFECTED(victim, AFF_DETECT_INVIS)) {
            if (victim == ch) {
                send_to_char("You can already see invisible.\n", ch);
            } else {
                act("$N can already see invisible things.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = (5 + level / 3);
        af.modifier = 0;
        af.location = APPLY_NONE;
        af.bitvector = AFF_DETECT_INVIS;
        affect_to_char(victim, af);
        send_to_char("Your eyes tingle.\n", victim);
        if (ch != victim) {
            send_to_char("Ok.\n", ch);
        }
    }


    static void spell_detect_magic(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_DETECT_MAGIC)) {
            if (victim == ch) {
                send_to_char("You can already sense magical auras.\n", ch);
            } else {
                act("$N can already detect magic.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = (5 + level / 3);
        af.modifier = 0;
        af.location = APPLY_NONE;
        af.bitvector = AFF_DETECT_MAGIC;
        affect_to_char(victim, af);
        send_to_char("Your eyes tingle.\n", victim);
        if (ch != victim) {
            send_to_char("Ok.\n", ch);
        }
    }


    static void spell_detect_poison(CHAR_DATA ch, Object vo) {
        OBJ_DATA obj = (OBJ_DATA) vo;

        if (obj.item_type == ITEM_DRINK_CON || obj.item_type == ITEM_FOOD) {
            if (obj.value[3] != 0) {
                send_to_char("You smell poisonous fumes.\n", ch);
            } else {
                send_to_char("It looks delicious.\n", ch);
            }
        } else {
            send_to_char("It doesn't look poisoned.\n", ch);
        }

    }


    static void spell_dispel_evil(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        if (!IS_NPC(ch) && IS_EVIL(ch)) {
            victim = ch;
        }

        if (IS_GOOD(victim)) {
            act("Gods protects $N.", ch, null, victim, TO_ROOM);
            return;
        }

        if (IS_NEUTRAL(victim)) {
            act("$N does not seem to be affected.", ch, null, victim, TO_CHAR);
            return;
        }

        if (victim.hit > (ch.level * 4)) {
            dam = dice(level, 4);
        } else {
            dam = UMAX(victim.hit, dice(level, 4));
        }
        if (saves_spell(level, victim, DAM_HOLY)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_HOLY, true);
    }


    static void spell_dispel_good(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        if (!IS_NPC(ch) && IS_GOOD(ch)) {
            victim = ch;
        }

        if (IS_EVIL(victim)) {
            act("$N is protected by $S evil.", ch, null, victim, TO_ROOM);
            return;
        }

        if (IS_NEUTRAL(victim)) {
            act("$N does not seem to be affected.", ch, null, victim, TO_CHAR);
            return;
        }

        if (victim.hit > (ch.level * 4)) {
            dam = dice(level, 4);
        } else {
            dam = UMAX(victim.hit, dice(level, 4));
        }
        if (saves_spell(level, victim, DAM_NEGATIVE)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_NEGATIVE, true);
    }

/* modified for enhanced use */

    static void spell_dispel_magic(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        boolean found = false;

        if (saves_spell(level, victim, DAM_OTHER)) {
            send_to_char("You feel a brief tingling sensation.\n", victim);
            send_to_char("You failed.\n", ch);
            return;
        }

        /* begin running through the spells */

        if (check_dispel(level, victim, Skill.gsn_armor)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_enhanced_armor)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_bless)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_blindness)) {
            found = true;
            act("$n is no longer blinded.", victim, null, null, TO_ROOM);
        }

        if (check_dispel(level, victim, Skill.gsn_calm)) {
            found = true;
            act("$n no longer looks so peaceful...", victim, null, null, TO_ROOM);
        }

        if (check_dispel(level, victim, lookupSkill("change sex"))) {
            found = true;
            act("$n looks more like $mself again.", victim, null, null, TO_ROOM);
        }

        if (check_dispel(level, victim, Skill.gsn_charm_person)) {
            found = true;
            act("$n regains $s free will.", victim, null, null, TO_ROOM);
        }

        if (check_dispel(level, victim, Skill.gsn_chill_touch)) {
            found = true;
            act("$n looks warmer.", victim, null, null, TO_ROOM);
        }

        if (check_dispel(level, victim, Skill.gsn_curse)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_detect_evil)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_detect_good)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_detect_hidden)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_detect_invis)) {
            found = true;
        }

//TODO???            found = true;

        if (check_dispel(level, victim, Skill.gsn_detect_hidden)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_detect_magic)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_faerie_fire)) {
            act("$n's outline fades.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_fly)) {
            act("$n falls to the ground!", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_frenzy)) {
            act("$n no longer looks so wild.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_giant_strength)) {
            act("$n no longer looks so mighty.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_haste)) {
            act("$n is no longer moving so quickly.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_infravision)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_invis)) {
            act("$n fades into existance.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_mass_invis)) {
            act("$n fades into existance.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_pass_door)) {
            found = true;
        }


        if (check_dispel(level, victim, Skill.gsn_protection_evil)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_protection_good)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_sanctuary)) {
            act("The white aura around $n's body vanishes.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (IS_AFFECTED(victim, AFF_SANCTUARY) && !saves_dispel(level, victim.level, -1)
                && !is_affected(victim, Skill.gsn_sanctuary)
                && !(victim.spec_fun.getName().equals("spec_special_guard")
                || victim.spec_fun.getName().equals("spec_stalker"))) {
            victim.affected_by = REMOVE_BIT(victim.affected_by, AFF_SANCTUARY);
            act("The white aura around $n's body vanishes.",
                    victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_shield)) {
            act("The shield protecting $n vanishes.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_sleep)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_slow)) {
            act("$n is no longer moving so slowly.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_stone_skin)) {
            act("$n's skin regains its normal texture.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_weaken)) {
            act("$n looks stronger.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_shielding)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_web)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_fear)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_protection_heat)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_protection_cold)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_magic_resistance)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_hallucination)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_terangreal)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_power_stun)) {
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_corruption)) {
            act("$n looks healthier.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (check_dispel(level, victim, Skill.gsn_web)) {
            act("The webs around $n dissolve.", victim, null, null, TO_ROOM);
            found = true;
        }

        if (found) {
            send_to_char("Ok.\n", ch);
        } else {
            send_to_char("Spell failed.\n", ch);
        }
    }

    static void spell_earthquake(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA vch;
        CHAR_DATA vch_next;
        OBJ_DATA obj, obj_next, corpse;

        send_to_char("The earth trembles beneath your feet!\n", ch);
        act("$n makes the earth tremble and shiver.", ch, null, null, TO_ROOM);

        for (vch = char_list; vch != null; vch = vch_next) {
            vch_next = vch.next;
            if (vch.in_room == null) {
                continue;
            }
            if (vch.in_room == ch.in_room) {
                if (vch != ch && !is_safe_spell(ch, vch, true) && !is_same_group(ch, vch)) {
                    if (is_safe(ch, vch)) {
                        continue;
                    }
                }
                if (ch == vch) {
                    continue;
                }
                if (IS_AFFECTED(vch, AFF_FLYING)) {
                    damage(ch, vch, 0, sn, DAM_BASH, true);
                } else {
                    damage(ch, vch, level + dice(2, 8), sn, DAM_BASH, true);
                }
                continue;
            }

            if (vch.in_room.area == ch.in_room.area) {
                send_to_char("The earth trembles and shivers.\n", vch);
            }
        }

        for (obj = ch.in_room.contents; obj != null; obj = obj_next) {
            obj_next = obj.next_content;
            if (obj.pIndexData.vnum == OBJ_VNUM_GRAVE_STONE
                    && (corpse = obj.contains) != null
                    && number_percent() < get_skill(ch, sn)) {
                obj_from_obj(corpse);
                corpse.extra_flags = REMOVE_BIT(corpse.extra_flags, ITEM_BURIED);
                obj_to_room(corpse, ch.in_room);
                extract_obj(obj);
                corpse.timer = number_range(25, 40);
                act("The earthquake reveals $p.\n", ch, corpse, null, TO_ALL);
            }
        }

    }

    static void spell_enchant_armor(Skill sn, int level, CHAR_DATA ch, Object vo) {
        OBJ_DATA obj = (OBJ_DATA) vo;

        int result, fail;
        int ac_bonus, added;
        boolean ac_found = false;

        if (obj.item_type != ITEM_ARMOR) {
            send_to_char("That isn't an armor.\n", ch);
            return;
        }

        if (obj.wear_loc != -1) {
            send_to_char("The item must be carried to be enchanted.\n", ch);
            return;
        }

        /* this means they have no bonus */
        fail = 25;  /* base 25% chance of failure */

        /* find the bonuses */

        if (!obj.enchanted) {
            for (AFFECT_DATA paf = obj.pIndexData.affected; paf != null; paf = paf.next) {
                if (paf.location == APPLY_AC) {
                    ac_bonus = paf.modifier;
                    ac_found = true;
                    fail += 5 * (ac_bonus * ac_bonus);
                } else  /* things get a little harder */ {
                    fail += 20;
                }
            }
        }

        for (AFFECT_DATA paf = obj.affected; paf != null; paf = paf.next) {
            if (paf.location == APPLY_AC) {
                ac_bonus = paf.modifier;
                ac_found = true;
                fail += 5 * (ac_bonus * ac_bonus);
            } else /* things get a little harder */ {
                fail += 20;
            }
        }

        /* apply other modifiers */
        fail -= level;

        if (IS_OBJ_STAT(obj, ITEM_BLESS)) {
            fail -= 15;
        }
        if (IS_OBJ_STAT(obj, ITEM_GLOW)) {
            fail -= 5;
        }

        fail = URANGE(5, fail, 85);

        result = number_percent();

        /* the moment of truth */
        if (result < (fail / 5))  /* item destroyed */ {
            act("$p flares blindingly... and evaporates!", ch, obj, null, TO_CHAR);
            act("$p flares blindingly... and evaporates!", ch, obj, null, TO_ROOM);
            extract_obj(obj);
            return;
        }

        if (result < (fail / 3)) /* item disenchanted */ {

            act("$p glows brightly, then fades...oops.", ch, obj, null, TO_CHAR);
            act("$p glows brightly, then fades.", ch, obj, null, TO_ROOM);
            obj.enchanted = true;

            /* remove all affects */
            for (AFFECT_DATA paf = obj.affected, paf_next; paf != null; paf = paf_next) {
                paf_next = paf.next;
            }
            obj.affected = null;

            /* clear all flags */
            obj.extra_flags = 0;
            return;
        }

        if (result <= fail)  /* failed, no bad result */ {
            send_to_char("Nothing seemed to happen.\n", ch);
            return;
        }

        /* okay, move all the old flags into new vectors if we have to */
        if (!obj.enchanted) {
            obj.enchanted = true;

            for (AFFECT_DATA paf = obj.pIndexData.affected; paf != null; paf = paf.next) {
                AFFECT_DATA af_new = new AFFECT_DATA();

                af_new.next = obj.affected;
                obj.affected = af_new;

                af_new.where = paf.where;
                af_new.type = paf.type;
                af_new.level = paf.level;
                af_new.duration = paf.duration;
                af_new.location = paf.location;
                af_new.modifier = paf.modifier;
                af_new.bitvector = paf.bitvector;
            }
        }

        if (result <= (90 - level / 5))  /* success! */ {
            act("$p shimmers with a gold aura.", ch, obj, null, TO_CHAR);
            act("$p shimmers with a gold aura.", ch, obj, null, TO_ROOM);
            obj.extra_flags = SET_BIT(obj.extra_flags, ITEM_MAGIC);
            added = -1;
        } else  /* exceptional enchant */ {
            act("$p glows a brillant gold!", ch, obj, null, TO_CHAR);
            act("$p glows a brillant gold!", ch, obj, null, TO_ROOM);
            obj.extra_flags = SET_BIT(obj.extra_flags, ITEM_MAGIC);
            obj.extra_flags = SET_BIT(obj.extra_flags, ITEM_GLOW);
            added = -2;
        }

        /* now add the enchantments */

        if (obj.level < LEVEL_HERO) {
            obj.level = UMIN(LEVEL_HERO - 1, obj.level + 1);
        }

        if (ac_found) {
            for (AFFECT_DATA paf = obj.affected; paf != null; paf = paf.next) {
                if (paf.location == APPLY_AC) {
                    paf.type = sn;
                    paf.modifier += added;
                    paf.level = UMAX(paf.level, level);
                }
            }
        } else /* add a new affect */ {
            AFFECT_DATA paf = new AFFECT_DATA();

            paf.where = TO_OBJECT;
            paf.type = sn;
            paf.level = level;
            paf.duration = -1;
            paf.location = APPLY_AC;
            paf.modifier = added;
            paf.bitvector = 0;
            paf.next = obj.affected;
            obj.affected = paf;
        }

    }


    static void spell_enchant_weapon(Skill sn, int level, CHAR_DATA ch, Object vo) {
        OBJ_DATA obj = (OBJ_DATA) vo;
        int result, fail;
        int hit_bonus, dam_bonus, added;
        boolean hit_found = false, dam_found = false;

        if (obj.item_type != ITEM_WEAPON) {
            send_to_char("That isn't a weapon.\n", ch);
            return;
        }

        if (obj.wear_loc != -1) {
            send_to_char("The item must be carried to be enchanted.\n", ch);
            return;
        }

        /* this means they have no bonus */
        fail = 25;  /* base 25% chance of failure */

        /* find the bonuses */

        if (!obj.enchanted) {
            for (AFFECT_DATA paf = obj.pIndexData.affected; paf != null; paf = paf.next) {
                if (paf.location == APPLY_HITROLL) {
                    hit_bonus = paf.modifier;
                    hit_found = true;
                    fail += 2 * (hit_bonus * hit_bonus);
                } else if (paf.location == APPLY_DAMROLL) {
                    dam_bonus = paf.modifier;
                    dam_found = true;
                    fail += 2 * (dam_bonus * dam_bonus);
                } else  /* things get a little harder */ {
                    fail += 25;
                }
            }
        }

        for (AFFECT_DATA paf = obj.affected; paf != null; paf = paf.next) {
            if (paf.location == APPLY_HITROLL) {
                hit_bonus = paf.modifier;
                hit_found = true;
                fail += 2 * (hit_bonus * hit_bonus);
            } else if (paf.location == APPLY_DAMROLL) {
                dam_bonus = paf.modifier;
                dam_found = true;
                fail += 2 * (dam_bonus * dam_bonus);
            } else /* things get a little harder */ {
                fail += 25;
            }
        }

        /* apply other modifiers */
        fail -= 3 * level / 2;

        if (IS_OBJ_STAT(obj, ITEM_BLESS)) {
            fail -= 15;
        }
        if (IS_OBJ_STAT(obj, ITEM_GLOW)) {
            fail -= 5;
        }

        fail = URANGE(5, fail, 95);

        result = number_percent();

        /* the moment of truth */
        if (result < (fail / 5))  /* item destroyed */ {
            act("$p shivers violently and explodes!", ch, obj, null, TO_CHAR);
            act("$p shivers violently and explodeds!", ch, obj, null, TO_ROOM);
            extract_obj(obj);
            return;
        }

        if (result < (fail / 2)) /* item disenchanted */ {

            act("$p glows brightly, then fades...oops.", ch, obj, null, TO_CHAR);
            act("$p glows brightly, then fades.", ch, obj, null, TO_ROOM);
            obj.enchanted = true;

            /* remove all affects */
            for (AFFECT_DATA paf = obj.affected, paf_next; paf != null; paf = paf_next) {
                paf_next = paf.next;
            }
            obj.affected = null;

            /* clear all flags */
            obj.extra_flags = 0;
            return;
        }

        if (result <= fail)  /* failed, no bad result */ {
            send_to_char("Nothing seemed to happen.\n", ch);
            return;
        }

        /* okay, move all the old flags into new vectors if we have to */
        if (!obj.enchanted) {
            AFFECT_DATA af_new;
            obj.enchanted = true;

            for (AFFECT_DATA paf = obj.pIndexData.affected; paf != null; paf = paf.next) {
                af_new = new AFFECT_DATA();

                af_new.next = obj.affected;
                obj.affected = af_new;

                af_new.where = paf.where;
                af_new.type = paf.type;
                af_new.level = paf.level;
                af_new.duration = paf.duration;
                af_new.location = paf.location;
                af_new.modifier = paf.modifier;
                af_new.bitvector = paf.bitvector;
            }
        }

        if (result <= (100 - level / 5))  /* success! */ {
            act("$p glows blue.", ch, obj, null, TO_CHAR);
            act("$p glows blue.", ch, obj, null, TO_ROOM);
            obj.extra_flags = SET_BIT(obj.extra_flags, ITEM_MAGIC);
            added = 1;
        } else  /* exceptional enchant */ {
            act("$p glows a brillant blue!", ch, obj, null, TO_CHAR);
            act("$p glows a brillant blue!", ch, obj, null, TO_ROOM);
            obj.extra_flags = SET_BIT(obj.extra_flags, ITEM_MAGIC);
            obj.extra_flags = SET_BIT(obj.extra_flags, ITEM_GLOW);
            added = 2;
        }

        /* now add the enchantments */

        if (obj.level < LEVEL_HERO - 1) {
            obj.level = UMIN(LEVEL_HERO - 1, obj.level + 1);
        }

        if (dam_found) {
            for (AFFECT_DATA paf = obj.affected; paf != null; paf = paf.next) {
                if (paf.location == APPLY_DAMROLL) {
                    paf.type = sn;
                    paf.modifier += added;
                    paf.level = UMAX(paf.level, level);
                    if (paf.modifier > 4) {
                        obj.extra_flags = SET_BIT(obj.extra_flags, ITEM_HUM);
                    }
                }
            }
        } else /* add a new affect */ {
            AFFECT_DATA paf = new AFFECT_DATA();

            paf.where = TO_OBJECT;
            paf.type = sn;
            paf.level = level;
            paf.duration = -1;
            paf.location = APPLY_DAMROLL;
            paf.modifier = added;
            paf.bitvector = 0;
            paf.next = obj.affected;
            obj.affected = paf;
        }

        if (hit_found) {
            for (AFFECT_DATA paf = obj.affected; paf != null; paf = paf.next) {
                if (paf.location == APPLY_HITROLL) {
                    paf.type = sn;
                    paf.modifier += added;
                    paf.level = UMAX(paf.level, level);
                    if (paf.modifier > 4) {
                        obj.extra_flags = SET_BIT(obj.extra_flags, ITEM_HUM);
                    }
                }
            }
        } else /* add a new affect */ {
            AFFECT_DATA paf = new AFFECT_DATA();

            paf.type = sn;
            paf.level = level;
            paf.duration = -1;
            paf.location = APPLY_HITROLL;
            paf.modifier = added;
            paf.bitvector = 0;
            paf.next = obj.affected;
            obj.affected = paf;
        }

    }

/*
* Drain XP, MANA, HP.
* Caster gains HP.
*/

    static void spell_energy_drain(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        if (saves_spell(level, victim, DAM_NEGATIVE)) {
            send_to_char("You feel a momentary chill.\n", victim);
            return;
        }


        if (victim.level <= 2) {
            dam = ch.hit + 1;
        } else {
            gain_exp(victim, 0 - number_range(level / 5, 3 * level / 5));
            victim.mana /= 2;
            victim.move /= 2;
            dam = dice(1, level);
            ch.hit += dam;
        }

        send_to_char("You feel your life slipping away!\n", victim);
        send_to_char("Wow....what a rush!\n", ch);
        damage(ch, victim, dam, sn, DAM_NEGATIVE, true);

    }

    static void spell_hellfire(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 7);

        damage(ch, victim, dam, sn, DAM_FIRE, true);

    }

    static void spell_iceball(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA tmp_vict;
        CHAR_DATA tmp_next;
        int dam;
        int movedam;

        dam = dice(level, 12);
        movedam = number_range(ch.level, 2 * ch.level);

        for (tmp_vict = ch.in_room.people; tmp_vict != null;
             tmp_vict = tmp_next) {
            tmp_next = tmp_vict.next_in_room;

            if (!is_safe_spell(ch, tmp_vict, true)) {
                if (!IS_NPC(ch) && tmp_vict != ch &&
                        ch.fighting != tmp_vict && tmp_vict.fighting != ch &&
                        (IS_SET(tmp_vict.affected_by, AFF_CHARM) || !IS_NPC(tmp_vict))) {
                    if (!can_see(tmp_vict, ch)) {
                        do_yell(tmp_vict, "Help someone is attacking me!");
                    } else {
                        TextBuffer buf = new TextBuffer();
                        buf.sprintf("Die, %s, you sorcerous dog!",
                                (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(tmp_vict)) ? ch.doppel.name : ch.name);
                        do_yell(tmp_vict, buf.toString());
                    }
                }

                if (saves_spell(level, tmp_vict, DAM_COLD)) {
                    dam /= 2;
                }
                damage(ch, tmp_vict, dam, sn, DAM_COLD, true);
                tmp_vict.move -= UMIN(tmp_vict.move, movedam);

            }
        }
    }

    static void spell_fireball(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA tmp_vict;
        CHAR_DATA tmp_next;
        int dam;
        int movedam;

        dam = dice(level, 12);
        movedam = number_range(ch.level, 2 * ch.level);

        for (tmp_vict = ch.in_room.people; tmp_vict != null;
             tmp_vict = tmp_next) {
            tmp_next = tmp_vict.next_in_room;

            if (!is_safe_spell(ch, tmp_vict, true)) {
                if (!IS_NPC(ch) && tmp_vict != ch &&
                        ch.fighting != tmp_vict && tmp_vict.fighting != ch &&
                        (IS_SET(tmp_vict.affected_by, AFF_CHARM) || !IS_NPC(tmp_vict))) {
                    if (!can_see(tmp_vict, ch)) {
                        do_yell(tmp_vict, "Help someone is attacking me!");
                    } else {
                        TextBuffer buf = new TextBuffer();
                        buf.sprintf("Die, %s, you sorcerous dog!",
                                (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(tmp_vict)) ? ch.doppel.name : ch.name);
                        do_yell(tmp_vict, buf.toString());
                    }
                }

                if (saves_spell(level, tmp_vict, DAM_FIRE)) {
                    dam /= 2;
                }
                damage(ch, tmp_vict, dam, sn, DAM_FIRE, true);
                tmp_vict.move -= UMIN(tmp_vict.move, movedam);

            }
        }
    }


    static void spell_fireproof(Skill sn, int level, CHAR_DATA ch, Object vo) {
        OBJ_DATA obj = (OBJ_DATA) vo;

        if (IS_OBJ_STAT(obj, ITEM_BURN_PROOF)) {
            act("$p is already protected from burning.", ch, obj, null, TO_CHAR);
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_OBJECT;
        af.type = sn;
        af.level = level;
        af.duration = number_fuzzy(level / 4);
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = ITEM_BURN_PROOF;

        affect_to_obj(obj, af);

        act("You protect $p from fire.", ch, obj, null, TO_CHAR);
        act("$p is surrounded by a protective aura.", ch, obj, null, TO_ROOM);
    }


    static void spell_flamestrike(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 10);
        if (saves_spell(level, victim, DAM_FIRE)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_FIRE, true);
    }


    static void spell_faerie_fire(Skill sn, int level, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_FAERIE_FIRE)) {
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 10 + level / 5;
        af.location = APPLY_AC;
        af.modifier = 2 * level;
        af.bitvector = AFF_FAERIE_FIRE;
        affect_to_char(victim, af);
        send_to_char("You are surrounded by a pink outline.\n", victim);
        act("$n is surrounded by a pink outline.", victim, null, null, TO_ROOM);
    }


    static void spell_faerie_fog(int level, CHAR_DATA ch) {
        CHAR_DATA ich;

        act("$n conjures a cloud of purple smoke.", ch, null, null, TO_ROOM);
        send_to_char("You conjure a cloud of purple smoke.\n", ch);

        for (ich = ch.in_room.people; ich != null; ich = ich.next_in_room) {
            if (ich.invis_level > 0) {
                continue;
            }

            if (ich == ch || saves_spell(level, ich, DAM_OTHER)) {
                continue;
            }

            affect_strip(ich, gsn_invis);
            affect_strip(ich, gsn_mass_invis);
            affect_strip(ich, gsn_imp_invis);
            ich.affected_by = REMOVE_BIT(ich.affected_by, AFF_HIDE);
            ich.affected_by = REMOVE_BIT(ich.affected_by, AFF_FADE);
            ich.affected_by = REMOVE_BIT(ich.affected_by, AFF_INVISIBLE);
            ich.affected_by = REMOVE_BIT(ich.affected_by, AFF_IMP_INVIS);

            /* An elf sneaks eternally */
            if (IS_NPC(ich) || !IS_SET(ich.race.aff, AFF_SNEAK)) {
                affect_strip(ich, gsn_sneak);
                ich.affected_by = REMOVE_BIT(ich.affected_by, AFF_SNEAK);
            }

            act("$n is revealed!", ich, null, null, TO_ROOM);
            send_to_char("You are revealed!\n", ich);
        }

    }

    static void spell_floating_disc(int level, CHAR_DATA ch) {
        OBJ_DATA disc, floating;

        floating = get_eq_char(ch, WEAR_FLOAT);
        if (floating != null && IS_OBJ_STAT(floating, ITEM_NOREMOVE)) {
            act("You can't remove $p.", ch, floating, null, TO_CHAR);
            return;
        }

        disc = create_object(get_obj_index(OBJ_VNUM_DISC), 0);
        disc.value[0] = ch.level * 10; /* 10 pounds per level capacity */
        disc.value[3] = ch.level * 5; /* 5 pounds per level max per item */
        disc.timer = ch.level * 2 - number_range(0, level / 2);

        act("$n has created a floating black disc.", ch, null, null, TO_ROOM);
        send_to_char("You create a floating disc.\n", ch);
        obj_to_char(disc, ch);
        wear_obj(ch, disc, true);
    }


    static void spell_fly(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_FLYING)) {
            if (victim == ch) {
                send_to_char("You are already airborne.\n", ch);
            } else {
                act("$N doesn't need your help to fly.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = level + 3;
        af.location = 0;
        af.modifier = 0;
        af.bitvector = AFF_FLYING;
        affect_to_char(victim, af);
        send_to_char("Your feet rise off the ground.\n", victim);
        act("$n's feet rise off the ground.", victim, null, null, TO_ROOM);
    }

/* RT clerical berserking spell */

    static void spell_frenzy(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(victim, sn) || IS_AFFECTED(victim, AFF_BERSERK)) {
            if (victim == ch) {
                send_to_char("You are already in a frenzy.\n", ch);
            } else {
                act("$N is already in a frenzy.", ch, null, victim, TO_CHAR);
            }
            return;
        }

        if (is_affected(victim, Skill.gsn_calm)) {
            if (victim == ch) {
                send_to_char("Why don't you just relax for a while?\n", ch);
            } else {
                act("$N doesn't look like $e wants to fight anymore.",
                        ch, null, victim, TO_CHAR);
            }
            return;
        }

        if ((IS_GOOD(ch) && !IS_GOOD(victim)) ||
                (IS_NEUTRAL(ch) && !IS_NEUTRAL(victim)) ||
                (IS_EVIL(ch) && !IS_EVIL(victim))
                ) {
            act("Your god doesn't seem to like $N", ch, null, victim, TO_CHAR);
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = level / 3;
        af.modifier = level / 6;
        af.bitvector = 0;

        af.location = APPLY_HITROLL;
        affect_to_char(victim, af);

        af.location = APPLY_DAMROLL;
        affect_to_char(victim, af);

        af.modifier = 10 * (level / 12);
        af.location = APPLY_AC;
        affect_to_char(victim, af);

        send_to_char("You are filled with holy wrath!\n", victim);
        act("$n gets a wild look in $s eyes!", victim, null, null, TO_ROOM);
    }

    static void spell_gate(int level, CHAR_DATA ch) {
        CHAR_DATA victim;
        boolean gate_pet;


        if ((victim = get_char_world(ch, target_name)) == null
                || victim == ch
                || victim.in_room == null
                || !can_see_room(ch, victim.in_room)
                || IS_SET(victim.in_room.room_flags, ROOM_SAFE)
                || IS_SET(victim.in_room.room_flags, ROOM_PRIVATE)
                || IS_SET(victim.in_room.room_flags, ROOM_SOLITARY)
                || IS_SET(ch.in_room.room_flags, ROOM_NOSUMMON)
                || IS_SET(victim.in_room.room_flags, ROOM_NOSUMMON)
                || victim.level >= level + 3
                || saves_spell(level, victim, DAM_OTHER)
/*    ||   (!IS_NPC(victim) && victim.level >= LEVEL_HERO)  * NOT trust */
                || (IS_NPC(victim) && is_safe_nomessage(ch, victim) && IS_SET(victim.imm_flags, IMM_SUMMON))
                || (!IS_NPC(victim) && is_safe_nomessage(ch, victim) && IS_SET(victim.act, PLR_NOSUMMON))
                || (!IS_NPC(victim) && ch.in_room.area != victim.in_room.area)
                || (IS_NPC(victim) && saves_spell(level, victim, DAM_OTHER))) {
            send_to_char("You failed.\n", ch);
            return;
        }
        gate_pet = !(ch.pet == null || ch.in_room != ch.pet.in_room);

        act("$n steps through a gate and vanishes.", ch, null, null, TO_ROOM);
        send_to_char("You step through a gate and vanish.\n", ch);
        char_from_room(ch);
        char_to_room(ch, victim.in_room);

        act("$n has arrived through a gate.", ch, null, null, TO_ROOM);
        do_look(ch, "auto");

        if (gate_pet) {
            act("$n steps through a gate and vanishes.", ch.pet, null, null, TO_ROOM);
            send_to_char("You step through a gate and vanish.\n", ch.pet);
            char_from_room(ch.pet);
            char_to_room(ch.pet, victim.in_room);
            act("$n has arrived through a gate.", ch.pet, null, null, TO_ROOM);
            do_look(ch.pet, "auto");
        }
    }


    static void spell_giant_strength(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(victim, sn)) {
            if (victim == ch) {
                send_to_char("You are already as strong as you can get!\n", ch);
            } else {
                act("$N can't get any stronger.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = (10 + level / 3);
        af.location = APPLY_STR;
        af.modifier = UMAX(2, level / 10);
        af.bitvector = 0;
        affect_to_char(victim, af);
        send_to_char("Your muscles surge with heightened power!\n", victim);
        act("$n's muscles surge with heightened power.", victim, null, null, TO_ROOM);
    }


    static void spell_harm(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = UMAX(20, victim.hit - dice(1, 4));
        if (saves_spell(level, victim, DAM_HARM)) {
            dam = UMIN(50, dam / 2);
        }
        dam = UMIN(100, dam);
        damage(ch, victim, dam, sn, DAM_HARM, true);
    }

/* RT haste spell */

    static void spell_haste(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(victim, sn) || IS_AFFECTED(victim, AFF_HASTE)
                || IS_SET(victim.off_flags, OFF_FAST)) {
            if (victim == ch) {
                send_to_char("You can't move any faster!\n", ch);
            } else {
                act("$N is already moving as fast as $E can.",
                        ch, null, victim, TO_CHAR);
            }
            return;
        }

        if (IS_AFFECTED(victim, AFF_SLOW)) {
            if (!check_dispel(level, victim, Skill.gsn_slow)) {
                if (victim != ch) {
                    send_to_char("Spell failed.\n", ch);
                }
                send_to_char("You feel momentarily faster.\n", victim);
                return;
            }
            act("$n is moving less slowly.", victim, null, null, TO_ROOM);
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        if (victim == ch) {
            af.duration = level / 2;
        } else {
            af.duration = level / 4;
        }
        af.location = APPLY_DEX;
        af.modifier = UMAX(2, level / 12);
        af.bitvector = AFF_HASTE;
        affect_to_char(victim, af);
        send_to_char("You feel yourself moving more quickly.\n", victim);
        act("$n is moving more quickly.", victim, null, null, TO_ROOM);
        if (ch != victim) {
            send_to_char("Ok.\n", ch);
        }
    }


    static void spell_heal(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        victim.hit = UMIN(victim.hit + 100 + level / 10, victim.max_hit);
        update_pos(victim);
        send_to_char("A warm feeling fills your body.\n", victim);
        if (ch != victim) {
            send_to_char("Ok.\n", ch);
        }
    }

    static void spell_heat_metal(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        OBJ_DATA obj_lose, obj_next;
        int dam = 0;
        boolean fail = true;

        if (!saves_spell(level + 2, victim, DAM_FIRE)
                && !IS_SET(victim.imm_flags, IMM_FIRE)) {
            for (obj_lose = victim.carrying; obj_lose != null; obj_lose = obj_next) {
                obj_next = obj_lose.next_content;
                if (number_range(1, 2 * level) > obj_lose.level
                        && !saves_spell(level, victim, DAM_FIRE)
                        && is_metal(obj_lose)
                        && !IS_OBJ_STAT(obj_lose, ITEM_BURN_PROOF)) {
                    switch (obj_lose.item_type) {
                        case ITEM_ARMOR:
                            if (obj_lose.wear_loc != -1) /* remove the item */ {
                                if (can_drop_obj(victim, obj_lose)
                                        && (obj_lose.weight / 10) <
                                        number_range(1, 2 * get_curr_stat(victim, STAT_DEX))
                                        && remove_obj(victim, obj_lose, true)) {
                                    act("$n yelps and throws $p to the ground!",
                                            victim, obj_lose, null, TO_ROOM);
                                    act("You remove and drop $p before it burns you.",
                                            victim, obj_lose, null, TO_CHAR);
                                    dam += (number_range(1, obj_lose.level) / 3);
                                    obj_from_char(obj_lose);
                                    obj_to_room(obj_lose, victim.in_room);
                                    fail = false;
                                } else /* stuck on the body! ouch! */ {
                                    act("Your skin is seared by $p!",
                                            victim, obj_lose, null, TO_CHAR);
                                    dam += (number_range(1, obj_lose.level));
                                    fail = false;
                                }

                            } else /* drop it if we can */ {
                                if (can_drop_obj(victim, obj_lose)) {
                                    act("$n yelps and throws $p to the ground!",
                                            victim, obj_lose, null, TO_ROOM);
                                    act("You and drop $p before it burns you.",
                                            victim, obj_lose, null, TO_CHAR);
                                    dam += (number_range(1, obj_lose.level) / 6);
                                    obj_from_char(obj_lose);
                                    obj_to_room(obj_lose, victim.in_room);
                                    fail = false;
                                } else /* cannot drop */ {
                                    act("Your skin is seared by $p!",
                                            victim, obj_lose, null, TO_CHAR);
                                    dam += (number_range(1, obj_lose.level) / 2);
                                    fail = false;
                                }
                            }
                            break;
                        case ITEM_WEAPON:
                            if (obj_lose.wear_loc != -1) /* try to drop it */ {
                                if (IS_WEAPON_STAT(obj_lose, WEAPON_FLAMING)) {
                                    continue;
                                }

                                if (can_drop_obj(victim, obj_lose)
                                        && remove_obj(victim, obj_lose, true)) {
                                    act("$n is burned by $p, and throws it to the ground.",
                                            victim, obj_lose, null, TO_ROOM);
                                    send_to_char(
                                            "You throw your red-hot weapon to the ground!\n",
                                            victim);
                                    dam += 1;
                                    obj_from_char(obj_lose);
                                    obj_to_room(obj_lose, victim.in_room);
                                    fail = false;
                                } else /* YOWCH! */ {
                                    send_to_char("Your weapon sears your flesh!\n",
                                            victim);
                                    dam += number_range(1, obj_lose.level);
                                    fail = false;
                                }
                            } else /* drop it if we can */ {
                                if (can_drop_obj(victim, obj_lose)) {
                                    act("$n throws a burning hot $p to the ground!",
                                            victim, obj_lose, null, TO_ROOM);
                                    act("You and drop $p before it burns you.",
                                            victim, obj_lose, null, TO_CHAR);
                                    dam += (number_range(1, obj_lose.level) / 6);
                                    obj_from_char(obj_lose);
                                    obj_to_room(obj_lose, victim.in_room);
                                    fail = false;
                                } else /* cannot drop */ {
                                    act("Your skin is seared by $p!",
                                            victim, obj_lose, null, TO_CHAR);
                                    dam += (number_range(1, obj_lose.level) / 2);
                                    fail = false;
                                }
                            }
                            break;
                    }
                }
            }
        }
        if (fail) {
            send_to_char("Your spell had no effect.\n", ch);
            send_to_char("You feel momentarily warmer.\n", victim);
        } else /* damage! */ {
            if (saves_spell(level, victim, DAM_FIRE)) {
                dam = 2 * dam / 3;
            }
            damage(ch, victim, dam, sn, DAM_FIRE, true);
        }
    }

/* RT really nasty high-level attack spell */

    static void spell_holy_word(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA vch;
        CHAR_DATA vch_next;
        int dam;
        Skill bless_num, curse_num, frenzy_num;

        bless_num = Skill.gsn_bless;
        curse_num = Skill.gsn_curse;
        frenzy_num = Skill.gsn_frenzy;

        act("$n utters a word of divine power!", ch, null, null, TO_ROOM);
        send_to_char("You utter a word of divine power.\n", ch);

        for (vch = ch.in_room.people; vch != null; vch = vch_next) {
            vch_next = vch.next_in_room;

            if ((IS_GOOD(ch) && IS_GOOD(vch)) ||
                    (IS_EVIL(ch) && IS_EVIL(vch)) ||
                    (IS_NEUTRAL(ch) && IS_NEUTRAL(vch))) {
                send_to_char("You feel full more powerful.\n", vch);
                spell_frenzy(frenzy_num, level, ch, vch);
                spell_bless(bless_num, level, ch, vch, TARGET_CHAR);
            } else if ((IS_GOOD(ch) && IS_EVIL(vch)) ||
                    (IS_EVIL(ch) && IS_GOOD(vch))) {
                if (!is_safe_spell(ch, vch, true)) {
                    if (!IS_NPC(ch) && vch != ch &&
                            ch.fighting != vch && vch.fighting != ch &&
                            (IS_SET(vch.affected_by, AFF_CHARM) || !IS_NPC(vch))) {
                        if (!can_see(vch, ch)) {
                            do_yell(vch, "Help someone is attacking me!");
                        } else {
                            TextBuffer buf = new TextBuffer();
                            buf.sprintf("Die, %s, you sorcerous dog!",
                                    (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(vch)) ? ch.doppel.name : ch.name);
                            do_yell(vch, buf.toString());
                        }
                    }

                    spell_curse(curse_num, level, ch, vch, TARGET_CHAR);
                    send_to_char("You are struck down!\n", vch);
                    dam = dice(level, 6);
                    damage(ch, vch, dam, sn, DAM_ENERGY, true);
                }
            } else if (IS_NEUTRAL(ch)) {
                if (!is_safe_spell(ch, vch, true)) {
                    if (!IS_NPC(ch) && vch != ch &&
                            ch.fighting != vch && vch.fighting != ch &&
                            (IS_SET(vch.affected_by, AFF_CHARM) || !IS_NPC(vch))) {
                        if (!can_see(vch, ch)) {
                            do_yell(vch, "Help someone is attacking me!");
                        } else {
                            TextBuffer buf = new TextBuffer();
                            buf.sprintf("Die, %s, you sorcerous dog!",
                                    (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(vch)) ? ch.doppel.name : ch.name);
                            do_yell(vch, buf.toString());
                        }
                    }

                    spell_curse(curse_num, level / 2, ch, vch, TARGET_CHAR);
                    send_to_char("You are struck down!\n", vch);
                    dam = dice(level, 4);
                    damage(ch, vch, dam, sn, DAM_ENERGY, true);
                }
            }
        }

        send_to_char("You feel drained.\n", ch);
        gain_exp(ch, -1 * number_range(1, 10) * 5);
        ch.move /= (4 / 3);
        ch.hit /= (4 / 3);
    }

    static void spell_identify(CHAR_DATA ch, Object vo) {
        OBJ_DATA obj = (OBJ_DATA) vo;
        TextBuffer buf = new TextBuffer();
        buf.sprintf("Object '%s' is type %s, extra flags %s.\nWeight is %d, value is %d, level is %d.\n",
                obj.name,
                item_type_name(obj),
                extra_bit_name(obj.extra_flags),
                obj.weight / 10,
                obj.cost,
                obj.level
        );
        send_to_char(buf, ch);

        if (obj.pIndexData.limit != -1) {
            buf.sprintf(
                    "This equipment has been LIMITED by number %d \n",
                    obj.pIndexData.limit);
            send_to_char(buf, ch);
        }

        switch (obj.item_type) {
            case ITEM_SCROLL:
            case ITEM_POTION:
            case ITEM_PILL:
                buf.sprintf("Level %d spells of:", obj.value[0]);
                send_to_char(buf, ch);

                if (obj.value[1] >= 0 && obj.value[1] < MAX_SKILL) {
                    send_to_char(" '", ch);
                    send_to_char(Skill.skills[obj.value[1]].name, ch);
                    send_to_char("'", ch);
                }

                if (obj.value[2] >= 0 && obj.value[2] < MAX_SKILL) {
                    send_to_char(" '", ch);
                    send_to_char(Skill.skills[obj.value[2]].name, ch);
                    send_to_char("'", ch);
                }

                if (obj.value[3] >= 0 && obj.value[3] < MAX_SKILL) {
                    send_to_char(" '", ch);
                    send_to_char(Skill.skills[obj.value[3]].name, ch);
                    send_to_char("'", ch);
                }

                if (obj.value[4] >= 0 && obj.value[4] < MAX_SKILL) {
                    send_to_char(" '", ch);
                    send_to_char(Skill.skills[obj.value[4]].name, ch);
                    send_to_char("'", ch);
                }

                send_to_char(".\n", ch);
                break;

            case ITEM_WAND:
            case ITEM_STAFF:
                buf.sprintf("Has %d charges of level %d", obj.value[2], obj.value[0]);
                send_to_char(buf, ch);

                if (obj.value[3] >= 0 && obj.value[3] < MAX_SKILL) {
                    send_to_char(" '", ch);
                    send_to_char(Skill.skills[obj.value[3]].name, ch);
                    send_to_char("'", ch);
                }

                send_to_char(".\n", ch);
                break;

            case ITEM_DRINK_CON:
                buf.sprintf("It holds %s-colored %s.\n", liq_table[obj.value[2]].liq_color, liq_table[obj.value[2]].liq_name);
                send_to_char(buf, ch);
                break;

            case ITEM_CONTAINER:
                buf.sprintf("Capacity: %d#  Maximum weight: %d#  flags: %s\n",
                        obj.value[0], obj.value[3], cont_bit_name(obj.value[1]));
                send_to_char(buf, ch);
                if (obj.value[4] != 100) {
                    buf.sprintf("Weight multiplier: %d%%\n",
                            obj.value[4]);
                    send_to_char(buf, ch);
                }
                break;

            case ITEM_WEAPON:
                send_to_char("Weapon type is ", ch);
                switch (obj.value[0]) {
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
                    buf.sprintf("Damage is %dd%d (average %d).\n", obj.value[1], obj.value[2], (1 + obj.value[2]) * obj.value[1] / 2);
                } else {
                    buf.sprintf("Damage is %d to %d (average %d).\n", obj.value[1], obj.value[2], (obj.value[1] + obj.value[2]) / 2);
                }
                send_to_char(buf, ch);
                if (obj.value[4] != 0)  /* weapon flags */ {
                    buf.sprintf("Weapons flags: %s\n", weapon_bit_name(obj.value[4]));
                    send_to_char(buf, ch);
                }
                break;

            case ITEM_ARMOR:
                buf.sprintf("Armor class is %d pierce, %d bash, %d slash, and %d vs. magic.\n",
                        obj.value[0], obj.value[1], obj.value[2], obj.value[3]);
                send_to_char(buf, ch);
                break;
        }

        if (!obj.enchanted) {
            for (AFFECT_DATA paf = obj.pIndexData.affected; paf != null; paf = paf.next) {
                if (paf.location != APPLY_NONE && paf.modifier != 0) {
                    buf.sprintf("Affects %s by %d.\n", affect_loc_name(paf.location), paf.modifier);
                    send_to_char(buf, ch);
                    if (paf.bitvector != 0) {
                        switch (paf.where) {
                            case TO_AFFECTS:
                                buf.sprintf("Adds %s affect.\n", affect_bit_name(paf.bitvector));
                                break;
                            case TO_OBJECT:
                                buf.sprintf("Adds %s object flag.\n", extra_bit_name(paf.bitvector));
                                break;
                            case TO_IMMUNE:
                                buf.sprintf("Adds immunity to %s.\n", imm_bit_name(paf.bitvector));
                                break;
                            case TO_RESIST:
                                buf.sprintf("Adds resistance to %s.\n", imm_bit_name(paf.bitvector));
                                break;
                            case TO_VULN:
                                buf.sprintf("Adds vulnerability to %s.\n", imm_bit_name(paf.bitvector));
                                break;
                            default:
                                buf.sprintf("Unknown bit %d: %d\n", paf.where, paf.bitvector);
                                break;
                        }
                        send_to_char(buf, ch);
                    }
                }
            }
        }

        for (AFFECT_DATA paf = obj.affected; paf != null; paf = paf.next) {
            if (paf.location != APPLY_NONE && paf.modifier != 0) {
                buf.sprintf("Affects %s by %d", affect_loc_name(paf.location), paf.modifier);
                send_to_char(buf, ch);
                if (paf.duration > -1) {
                    buf.sprintf(", %d hours.\n", paf.duration);
                } else {
                    buf.sprintf(".\n");
                }
                send_to_char(buf, ch);

                if (paf.bitvector != 0) {
                    switch (paf.where) {
                        case TO_AFFECTS:
                            buf.sprintf("Adds %s affect.\n", affect_bit_name(paf.bitvector));
                            break;
                        case TO_OBJECT:
                            buf.sprintf("Adds %s object flag.\n", extra_bit_name(paf.bitvector));
                            break;
                        case TO_WEAPON:
                            buf.sprintf("Adds %s weapon flags.\n", weapon_bit_name(paf.bitvector));
                            break;
                        case TO_IMMUNE:
                            buf.sprintf("Adds immunity to %s.\n", imm_bit_name(paf.bitvector));
                            break;
                        case TO_RESIST:
                            buf.sprintf("Adds resistance to %s.\n", imm_bit_name(paf.bitvector));
                            break;
                        case TO_VULN:
                            buf.sprintf("Adds vulnerability to %s.\n", imm_bit_name(paf.bitvector));
                            break;
                        default:
                            buf.sprintf("Unknown bit %d: %d\n", paf.where, paf.bitvector);
                            break;
                    }
                    send_to_char(buf, ch);
                }
            }
        }

    }


    static void spell_infravision(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_INFRARED)) {
            if (victim == ch) {
                send_to_char("You can already see in the dark.\n", ch);
            } else {
                act("$N already has infravision.\n", ch, null, victim, TO_CHAR);
            }
            return;
        }
        act("$n's eyes glow red.\n", ch, null, null, TO_ROOM);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 2 * level;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_INFRARED;
        affect_to_char(victim, af);
        send_to_char("Your eyes glow red.\n", victim);
    }


    static void spell_invis(Skill sn, int level, CHAR_DATA ch, Object vo, int target) {
        CHAR_DATA victim;
        OBJ_DATA obj;

        /* object invisibility */
        if (target == TARGET_OBJ) {
            obj = (OBJ_DATA) vo;

            if (IS_OBJ_STAT(obj, ITEM_INVIS)) {
                act("$p is already invisible.", ch, obj, null, TO_CHAR);
                return;
            }

            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_OBJECT;
            af.type = sn;
            af.level = level;
            af.duration = level / 4 + 12;
            af.location = APPLY_NONE;
            af.modifier = 0;
            af.bitvector = ITEM_INVIS;
            affect_to_obj(obj, af);

            act("$p fades out of sight.", ch, obj, null, TO_ALL);
            return;
        }

        /* character invisibility */
        victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_INVISIBLE)) {
            return;
        }

        act("$n fades out of existence.", victim, null, null, TO_ROOM);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = (level / 8 + 10);
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_INVISIBLE;
        affect_to_char(victim, af);
        send_to_char("You fade out of existence.\n", victim);
    }


    static void spell_know_alignment(CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        String msg;

        if (IS_GOOD(victim)) {
            msg = "$N has a pure and good aura.";
        } else if (IS_NEUTRAL(victim)) {
            msg = "$N act as no align.";
        } else {
            msg = "$N is the embodiment of pure evil!.";
        }

        act(msg, ch, null, victim, TO_CHAR);

        if (!IS_NPC(victim)) {
            if (victim.ethos == ETHOS_LAWFUL) {
                msg = "$N upholds the laws.";
            } else if (victim.ethos == ETHOS_NEUTRAL) {
                msg = "$N seems ambivalent to society.";
            } else if (victim.ethos == ETHOS_CHAOTIC) {
                msg = "$N seems very chaotic.";
            } else {
                msg = "$N doesn't know where they stand on the laws.";
            }
            act(msg, ch, null, victim, TO_CHAR);
        }
    }


    static void spell_lightning_bolt(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        if (IS_AFFECTED(victim, AFF_GROUNDING)) {
            send_to_char("The electricity fizzles at your foes.\n", victim);
            act("A lightning bolt fizzles at $N's foes.\n",
                    ch, null, victim, TO_ROOM);
            return;
        }
        dam = dice(level, 4) + 12;
        if (saves_spell(level, victim, DAM_LIGHTNING)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_LIGHTNING, true);
    }


    static void spell_locate_object(int level, CHAR_DATA ch) {
        OBJ_DATA obj;
        OBJ_DATA in_obj;
        boolean found;
        int number, max_found;

        found = false;
        number = 0;
        max_found = IS_IMMORTAL(ch) ? 200 : 2 * level;

        TextBuffer buf = new TextBuffer();

        for (obj = object_list; obj != null; obj = obj.next) {
            if (!can_see_obj(ch, obj) || !is_name(target_name, obj.name)
                    || IS_OBJ_STAT(obj, ITEM_NOLOCATE) || number_percent() > 2 * level
                    || ch.level < obj.level) {
                continue;
            }

            found = true;
            number++;

            for (in_obj = obj; in_obj.in_obj != null; in_obj = in_obj.in_obj) {
            }

            if (in_obj.carried_by != null && can_see(ch, in_obj.carried_by)) {
                buf.sprintf("one is carried by %s\n", PERS(in_obj.carried_by, ch));
            } else {
                if (IS_IMMORTAL(ch) && in_obj.in_room != null) {
                    buf.sprintf("one is in %s [Room %d]\n", in_obj.in_room.name, in_obj.in_room.vnum);
                } else {
                    buf.sprintf("one is in %s\n", in_obj.in_room == null ? "somewhere" : in_obj.in_room.name);
                }
            }

            buf.upfirst();

            if (number >= max_found) {
                break;
            }
        }

        if (!found) {
            send_to_char("Nothing like that in heaven or earth.\n", ch);
        } else {
            page_to_char(buf, ch);
        }


    }

    private static final int dam_each_mm[] = {
            0,
            3, 3, 4, 4, 5, 6, 6, 6, 6, 6,
            7, 7, 7, 7, 7, 8, 8, 8, 8, 8,
            9, 9, 9, 9, 9, 10, 10, 10, 10, 10,
            11, 11, 11, 11, 11, 12, 12, 12, 12, 12,
            13, 13, 13, 13, 13, 14, 14, 14, 14, 14
    };


    static void spell_magic_missile(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;


        int dam;

        if (is_affected(ch, Skill.gsn_protective_shield)) {
            if (ch.level > 4) {
                send_to_char("Your magic missiles fizzle out near your victim.\n", ch);
                act("Your shield blocks $N's magic missiles.", victim, null, ch, TO_CHAR);
            } else {
                send_to_char("Your magic missile fizzle out near your victim.\n", ch);
                act("Your shield blocks $N's magic missile.", victim, null, ch, TO_CHAR);
            }
            return;
        }


        level = UMIN(level, dam_each_mm.length - 1);
        level = UMAX(0, level);
        if (ch.level > 50) {
            dam = level / 4;
        } else {
            dam = number_range(dam_each_mm[level] / 2, dam_each_mm[level] * 2);
        }

        if (saves_spell(level, victim, DAM_ENERGY)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_ENERGY, true);
        if (ch.level > 4) {
            dam = number_range(dam_each_mm[level] / 2, dam_each_mm[level] * 2);
            if (saves_spell(level, victim, DAM_ENERGY)) {
                dam /= 2;
            }
            damage(ch, victim, dam, sn, DAM_ENERGY, true);
        }
        if (ch.level > 8) {
            dam = number_range(dam_each_mm[level] / 2, dam_each_mm[level] * 2);
            if (saves_spell(level, victim, DAM_ENERGY)) {
                dam /= 2;
            }
            damage(ch, victim, dam, sn, DAM_ENERGY, true);
        }
        if (ch.level > 12) {
            dam = number_range(dam_each_mm[level] / 2, dam_each_mm[level] * 2);
            if (saves_spell(level, victim, DAM_ENERGY)) {
                dam /= 2;
            }
            damage(ch, victim, dam, sn, DAM_ENERGY, true);
        }
        if (ch.level > 16) {
            dam = number_range(dam_each_mm[level] / 2, dam_each_mm[level] * 2);
            if (saves_spell(level, victim, DAM_ENERGY)) {
                dam /= 2;
            }
            damage(ch, victim, dam, sn, DAM_ENERGY, true);
        }

    }

    static void spell_mass_healing(int level, CHAR_DATA ch) {
        CHAR_DATA gch;
        for (gch = ch.in_room.people; gch != null; gch = gch.next_in_room) {
            if ((IS_NPC(ch) && IS_NPC(gch)) || (!IS_NPC(ch) && !IS_NPC(gch))) {
                spell_heal(level, ch, gch);
                spell_refresh(level, ch, gch);
            }
        }
    }


    static void spell_mass_invis(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA gch;

        for (gch = ch.in_room.people; gch != null; gch = gch.next_in_room) {
            if (!is_same_group(gch, ch) || IS_AFFECTED(gch, AFF_INVISIBLE)) {
                continue;
            }
            act("$n slowly fades out of existence.", gch, null, null, TO_ROOM);
            send_to_char("You slowly fade out of existence.\n", gch);

            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = sn;
            af.level = level / 2;
            af.duration = 24;
            af.location = APPLY_NONE;
            af.modifier = 0;
            af.bitvector = AFF_INVISIBLE;
            affect_to_char(gch, af);
        }
        send_to_char("Ok.\n", ch);

    }


    static void spell_null(CHAR_DATA ch) {
        send_to_char("That's not a spell!\n", ch);
    }


    static void spell_pass_door(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;


        if (IS_AFFECTED(victim, AFF_PASS_DOOR)) {
            if (victim == ch) {
                send_to_char("You are already out of phase.\n", ch);
            } else {
                act("$N is already shifted out of phase.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = number_fuzzy(level / 4);
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_PASS_DOOR;
        affect_to_char(victim, af);
        act("$n turns translucent.", victim, null, null, TO_ROOM);
        send_to_char("You turn translucent.\n", victim);
    }

/* RT plague spell, very nasty */

    static void spell_plague(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (saves_spell(level, victim, DAM_DISEASE) ||
                (IS_NPC(victim) && IS_SET(victim.act, ACT_UNDEAD))) {
            if (ch == victim) {
                send_to_char("You feel momentarily ill, but it passes.\n", ch);
            } else {
                act("$N seems to be unaffected.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level * 3 / 4;
        af.duration = (10 + level / 10);
        af.location = APPLY_STR;
        af.modifier = -1 * UMAX(1, 3 + level / 15);
        af.bitvector = AFF_PLAGUE;
        affect_join(victim, af);

        send_to_char
                ("You scream in agony as plague sores erupt from your skin.\n", victim);
        act("$n screams in agony as plague sores erupt from $s skin.",
                victim, null, null, TO_ROOM);
    }

    static void spell_poison(Skill sn, int level, CHAR_DATA ch, Object vo, int target) {
        CHAR_DATA victim;
        OBJ_DATA obj;


        if (target == TARGET_OBJ) {
            obj = (OBJ_DATA) vo;

            if (obj.item_type == ITEM_FOOD || obj.item_type == ITEM_DRINK_CON) {
                if (IS_OBJ_STAT(obj, ITEM_BLESS) || IS_OBJ_STAT(obj, ITEM_BURN_PROOF)) {
                    act("Your spell fails to corrupt $p.", ch, obj, null, TO_CHAR);
                    return;
                }
                obj.value[3] = 1;
                act("$p is infused with poisonous vapors.", ch, obj, null, TO_ALL);
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

                if (IS_WEAPON_STAT(obj, WEAPON_POISON)) {
                    act("$p is already envenomed.", ch, obj, null, TO_CHAR);
                    return;
                }
                AFFECT_DATA af = new AFFECT_DATA();
                af.where = TO_WEAPON;
                af.type = sn;
                af.level = level / 2;
                af.duration = level / 8;
                af.location = 0;
                af.modifier = 0;
                af.bitvector = WEAPON_POISON;
                affect_to_obj(obj, af);

                act("$p is coated with deadly venom.", ch, obj, null, TO_ALL);
                return;
            }

            act("You can't poison $p.", ch, obj, null, TO_CHAR);
            return;
        }

        victim = (CHAR_DATA) vo;

        if (saves_spell(level, victim, DAM_POISON)) {
            act("$n turns slightly green, but it passes.", victim, null, null, TO_ROOM);
            send_to_char("You feel momentarily ill, but it passes.\n", victim);
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = (10 + level / 10);
        af.location = APPLY_STR;
        af.modifier = -2;
        af.bitvector = AFF_POISON;
        affect_join(victim, af);
        send_to_char("You feel very sick.\n", victim);
        act("$n looks very ill.", victim, null, null, TO_ROOM);
    }


    static void spell_protection_evil(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_PROTECT_EVIL)
                || IS_AFFECTED(victim, AFF_PROTECT_GOOD)) {
            if (victim == ch) {
                send_to_char("You are already protected.\n", ch);
            } else {
                act("$N is already protected.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = (10 + level / 5);
        af.location = APPLY_SAVING_SPELL;
        af.modifier = -1;
        af.bitvector = AFF_PROTECT_EVIL;
        affect_to_char(victim, af);
        send_to_char("You feel holy and pure.\n", victim);
        if (ch != victim) {
            act("$N is protected from evil.", ch, null, victim, TO_CHAR);
        }
    }

    static void spell_protection_good(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_PROTECT_GOOD)
                || IS_AFFECTED(victim, AFF_PROTECT_EVIL)) {
            if (victim == ch) {
                send_to_char("You are already protected.\n", ch);
            } else {
                act("$N is already protected.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = (10 + level / 5);
        af.location = APPLY_SAVING_SPELL;
        af.modifier = -1;
        af.bitvector = AFF_PROTECT_GOOD;
        affect_to_char(victim, af);
        send_to_char("You feel aligned with darkness.\n", victim);
        if (ch != victim) {
            act("$N is protected from good.", ch, null, victim, TO_CHAR);
        }

    }


    static void spell_ray_of_truth(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam, align;

        if (IS_EVIL(ch)) {
            victim = ch;
            send_to_char("The energy explodes inside you!\n", ch);
        }

        if (victim != ch) {
            act("$n raises $s hand, and a blinding ray of light shoots forth!",
                    ch, null, null, TO_ROOM);
            send_to_char(
                    "You raise your hand and a blinding ray of light shoots forth!\n",
                    ch);
        }

        if (IS_GOOD(victim)) {
            act("$n seems unharmed by the light.", victim, null, victim, TO_ROOM);
            send_to_char("The light seems powerless to affect you.\n", victim);
            return;
        }

        dam = dice(level, 10);
        if (saves_spell(level, victim, DAM_HOLY)) {
            dam /= 2;
        }

        align = victim.alignment;
        align -= 350;

        if (align < -1000) {
            align = -1000 + (align + 1000) / 3;
        }

        dam = (dam * align * align) / 1000000;

        damage(ch, victim, dam, sn, DAM_HOLY, true);
        spell_blindness(gsn_blindness, 3 * level / 4, ch, victim);
    }


    static void spell_recharge(int level, CHAR_DATA ch, Object vo) {
        OBJ_DATA obj = (OBJ_DATA) vo;
        int chance, percent;

        if (obj.item_type != ITEM_WAND && obj.item_type != ITEM_STAFF) {
            send_to_char("That item does not carry charges.\n", ch);
            return;
        }

        if (obj.value[3] >= 3 * level / 2) {
            send_to_char("Your skills are not great enough for that.\n", ch);
            return;
        }

        if (obj.value[1] == 0) {
            send_to_char("That item has already been recharged once.\n", ch);
            return;
        }

        chance = 40 + 2 * level;

        chance -= obj.value[3]; /* harder to do high-level spells */
        chance -= (obj.value[1] - obj.value[2]) *
                (obj.value[1] - obj.value[2]);

        chance = UMAX(level / 2, chance);

        percent = number_percent();

        if (percent < chance / 2) {
            act("$p glows softly.", ch, obj, null, TO_CHAR);
            act("$p glows softly.", ch, obj, null, TO_ROOM);
            obj.value[2] = UMAX(obj.value[1], obj.value[2]);
            obj.value[1] = 0;
        } else if (percent <= chance) {
            int chargeback, chargemax;

            act("$p glows softly.", ch, obj, null, TO_CHAR);
            act("$p glows softly.", ch, obj, null, TO_CHAR);

            chargemax = obj.value[1] - obj.value[2];

            if (chargemax > 0) {
                chargeback = UMAX(1, chargemax * percent / 100);
            } else {
                chargeback = 0;
            }

            obj.value[2] += chargeback;
            obj.value[1] = 0;
        } else if (percent <= UMIN(95, 3 * chance / 2)) {
            send_to_char("Nothing seems to happen.\n", ch);
            if (obj.value[1] > 1) {
                obj.value[1]--;
            }
        } else /* whoops! */ {
            act("$p glows brightly and explodes!", ch, obj, null, TO_CHAR);
            act("$p glows brightly and explodes!", ch, obj, null, TO_ROOM);
            extract_obj(obj);
        }
    }

    static void spell_refresh(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        victim.move = UMIN(victim.move + level, victim.max_move);
        if (victim.max_move == victim.move) {
            send_to_char("You feel fully refreshed!\n", victim);
        } else {
            send_to_char("You feel less tired.\n", victim);
        }
        if (ch != victim) {
            send_to_char("Ok.\n", ch);
        }
    }

    static void spell_remove_curse(int level, CHAR_DATA ch, Object vo, int target) {
        CHAR_DATA victim;
        OBJ_DATA obj;
        boolean found = false;

        /* do object cases first */
        if (target == TARGET_OBJ) {
            obj = (OBJ_DATA) vo;

            if (IS_OBJ_STAT(obj, ITEM_NODROP) || IS_OBJ_STAT(obj, ITEM_NOREMOVE)) {
                if (!IS_OBJ_STAT(obj, ITEM_NOUNCURSE)
                        && !saves_dispel(level + 2, obj.level, 0)) {
                    obj.extra_flags = REMOVE_BIT(obj.extra_flags, ITEM_NODROP);
                    obj.extra_flags = REMOVE_BIT(obj.extra_flags, ITEM_NOREMOVE);
                    act("$p glows blue.", ch, obj, null, TO_ALL);
                    return;
                }

                act("The curse on $p is beyond your power.", ch, obj, null, TO_CHAR);
                return;
            } else {
                send_to_char("Nothing happens...\n", ch);
                return;
            }
        }

        /* characters */
        victim = (CHAR_DATA) vo;

        if (check_dispel(level, victim, gsn_curse)) {
            send_to_char("You feel better.\n", victim);
            act("$n looks more relaxed.", victim, null, null, TO_ROOM);
        }

        for (obj = victim.carrying; (obj != null && !found); obj = obj.next_content) {
            if ((IS_OBJ_STAT(obj, ITEM_NODROP) || IS_OBJ_STAT(obj, ITEM_NOREMOVE))
                    && !IS_OBJ_STAT(obj, ITEM_NOUNCURSE)) {   /* attempt to remove curse */
                if (!saves_dispel(level, obj.level, 0)) {
                    found = true;
                    obj.extra_flags = REMOVE_BIT(obj.extra_flags, ITEM_NODROP);
                    obj.extra_flags = REMOVE_BIT(obj.extra_flags, ITEM_NOREMOVE);
                    act("Your $p glows blue.", victim, obj, null, TO_CHAR);
                    act("$n's $p glows blue.", victim, obj, null, TO_ROOM);
                }
            }
        }
    }

    static void spell_sanctuary(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_SANCTUARY)) {
            if (victim == ch) {
                send_to_char("You are already in sanctuary.\n", ch);
            } else {
                act("$N is already in sanctuary.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = level / 6;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_SANCTUARY;
        affect_to_char(victim, af);
        act("$n is surrounded by a white aura.", victim, null, null, TO_ROOM);
        send_to_char("You are surrounded by a white aura.\n", victim);
    }


    static void spell_shield(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(victim, sn)) {
            if (victim == ch) {
                send_to_char("You are already shielded from harm.\n", ch);
            } else {
                act("$N is already protected by a shield.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = (8 + level / 3);
        af.location = APPLY_AC;
        af.modifier = -1 * UMAX(20, 10 + level / 3); /* af.modifier  = -20;*/
        af.bitvector = 0;
        affect_to_char(victim, af);
        act("$n is surrounded by a force shield.", victim, null, null, TO_ROOM);
        send_to_char("You are surrounded by a force shield.\n", victim);
    }


    private static int dam_each_sg[] =
            {
                    6,
                    8, 10, 12, 14, 16, 18, 20, 25, 29, 33,
                    36, 39, 39, 39, 40, 40, 41, 41, 42, 42,
                    43, 43, 44, 44, 45, 45, 46, 46, 47, 47,
                    48, 48, 49, 49, 50, 50, 51, 51, 52, 52,
                    53, 53, 54, 54, 55, 55, 56, 56, 57, 57
            };

    static void spell_shocking_grasp(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;


        int dam;

        level = UMIN(level, dam_each_sg.length - 1);
        level = UMAX(0, level);
        if (ch.level > 50) {
            dam = level / 2;
        } else {
            dam = number_range(dam_each_sg[level] / 2, dam_each_sg[level] * 2);
        }
        if (saves_spell(level, victim, DAM_LIGHTNING)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_LIGHTNING, true);
    }


    static void spell_sleep(Skill sn, int level, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_SLEEP)
                || (IS_NPC(victim) && IS_SET(victim.act, ACT_UNDEAD))
                || level < victim.level
                || saves_spell(level - 4, victim, DAM_CHARM)) {
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 1 + level / 10;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_SLEEP;
        affect_join(victim, af);

        if (IS_AWAKE(victim)) {
            send_to_char("You feel very sleepy ..... zzzzzz.\n", victim);
            act("$n goes to sleep.", victim, null, null, TO_ROOM);
            victim.position = POS_SLEEPING;
        }
    }

    static void spell_slow(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(victim, sn) || IS_AFFECTED(victim, AFF_SLOW)) {
            if (victim == ch) {
                send_to_char("You can't move any slower!\n", ch);
            } else {
                act("$N can't get any slower than that.",
                        ch, null, victim, TO_CHAR);
            }
            return;
        }

        if (saves_spell(level, victim, DAM_OTHER) || IS_SET(victim.imm_flags, IMM_MAGIC)) {
            if (victim != ch) {
                send_to_char("Nothing seemed to happen.\n", ch);
            }
            send_to_char("You feel momentarily lethargic.\n", victim);
            return;
        }

        if (IS_AFFECTED(victim, AFF_HASTE)) {
            if (!check_dispel(level, victim, Skill.gsn_haste)) {
                if (victim != ch) {
                    send_to_char("Spell failed.\n", ch);
                }
                send_to_char("You feel momentarily slower.\n", victim);
                return;
            }

            act("$n is moving less quickly.", victim, null, null, TO_ROOM);
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = (4 + level / 12);
        af.location = APPLY_DEX;
        af.modifier = -UMAX(2, level / 12);
        af.bitvector = AFF_SLOW;
        affect_to_char(victim, af);
        send_to_char("You feel yourself slowing d o w n...\n", victim);
        act("$n starts to move in slow motion.", victim, null, null, TO_ROOM);
    }


    static void spell_stone_skin(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(ch, sn)) {
            if (victim == ch) {
                send_to_char("Your skin is already as hard as a rock.\n", ch);
            } else {
                act("$N is already as hard as can be.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = (10 + level / 5);
        af.location = APPLY_AC;
        af.modifier = -1 * UMAX(40, 20 + level / 2);  /*af.modifier=-40;*/
        af.bitvector = 0;
        affect_to_char(victim, af);
        act("$n's skin turns to stone.", victim, null, null, TO_ROOM);
        send_to_char("Your skin turns to stone.\n", victim);
    }


    static void spell_summon(int level, CHAR_DATA ch) {
        CHAR_DATA victim;

        if ((victim = get_char_world(ch, target_name)) == null
                || victim == ch
                || victim.in_room == null
                || IS_SET(ch.in_room.room_flags, ROOM_SAFE)
                || IS_SET(victim.in_room.room_flags, ROOM_SAFE)
                || IS_SET(victim.in_room.room_flags, ROOM_PRIVATE)
                || IS_SET(victim.in_room.room_flags, ROOM_SOLITARY)
                || IS_SET(ch.in_room.room_flags, ROOM_NOSUMMON)
                || IS_SET(victim.in_room.room_flags, ROOM_NOSUMMON)
                || (IS_NPC(victim) && IS_SET(victim.act, ACT_AGGRESSIVE))
                || victim.level >= level + 3
                || (!IS_NPC(victim) && victim.level >= LEVEL_IMMORTAL)
                || victim.fighting != null
                || (IS_NPC(victim) && IS_SET(victim.imm_flags, IMM_SUMMON))
                || (IS_NPC(victim) && victim.pIndexData.pShop != null)
                || (!IS_NPC(victim) && is_safe_nomessage(ch, victim) && IS_SET(victim.act, PLR_NOSUMMON))
                || (saves_spell(level, victim, DAM_OTHER))
                || (ch.in_room.area != victim.in_room.area && !IS_NPC(victim))
                || (victim.in_room.exit[0] == null &&
                victim.in_room.exit[1] == null &&
                victim.in_room.exit[2] == null &&
                victim.in_room.exit[3] == null &&
                victim.in_room.exit[4] == null && victim.in_room.exit[5] == null)) {
            send_to_char("You failed.\n", ch);
            return;
        }

        if (IS_NPC(victim) && victim.in_mind == null) {
            victim.in_mind = String.valueOf(victim.in_room.vnum);
        }

        act("$n disappears suddenly.", victim, null, null, TO_ROOM);
        char_from_room(victim);
        char_to_room(victim, ch.in_room);
        act("$n arrives suddenly.", victim, null, null, TO_ROOM);
        act("$n has summoned you!", ch, null, victim, TO_VICT);
        do_look(victim, "auto");
    }


    static void spell_teleport(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        ROOM_INDEX_DATA pRoomIndex;

        if (!IS_NPC(ch)) {
            victim = ch;
        }

        if (victim.in_room == null
                || IS_SET(victim.in_room.room_flags, ROOM_NO_RECALL)
                || (victim != ch && IS_SET(victim.imm_flags, IMM_SUMMON))
                || (!IS_NPC(ch) && victim.fighting != null)
                || (victim != ch
                && (saves_spell(level - 5, victim, DAM_OTHER)))) {
            send_to_char("You failed.\n", ch);
            return;
        }

        pRoomIndex = get_random_room(victim);

        if (victim != ch) {
            send_to_char("You have been teleported!\n", victim);
        }

        act("$n vanishes!", victim, null, null, TO_ROOM);
        char_from_room(victim);
        char_to_room(victim, pRoomIndex);
        act("$n slowly fades into existence.", victim, null, null, TO_ROOM);
        do_look(victim, "auto");
    }


    static void spell_ventriloquate(int level, CHAR_DATA ch) {
        CHAR_DATA vch;

        StringBuilder speaker = new StringBuilder();
        target_name = one_argument(target_name, speaker);
        TextBuffer buf1 = new TextBuffer();
        TextBuffer buf2 = new TextBuffer();
        sprintf(buf1, "%s says '%s'.\n", speaker, target_name);
        sprintf(buf2, "Someone makes %s say '%s'.\n", speaker, target_name);
        buf1.upfirst();

        String speakerName = speaker.toString();
        for (vch = ch.in_room.people; vch != null; vch = vch.next_in_room) {
            if (!is_name(speakerName, vch.name)) {
                send_to_char(saves_spell(level, vch, DAM_OTHER) ? buf2 : buf1, vch);
            }
        }

    }


    static void spell_weaken(Skill sn, int level, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(victim, sn) || saves_spell(level, victim, DAM_OTHER)) {
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = (4 + level / 12);
        af.location = APPLY_STR;
        af.modifier = -1 * (2 + level / 12);
        af.bitvector = AFF_WEAKEN;
        affect_to_char(victim, af);
        send_to_char("You feel your strength slip away.\n", victim);
        act("$n looks tired and weak.", victim, null, null, TO_ROOM);
    }

/* RT recall spell is back */

    static void spell_word_of_recall(CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        ROOM_INDEX_DATA location;
        int to_room_vnum;

        if ((ch.clazz == Clazz.SAMURAI) && (ch.fighting != null) && (victim == null)) {
            send_to_char("Your honour doesn't let you recall!.\n", ch);
            return;
        }

        if (victim != null) {
            if ((victim.fighting != null) && (victim.clazz == Clazz.SAMURAI)) {
                send_to_char("You can't cast this spell to a honourable fighting Samurai!.\n", ch);
                return;
            }
        }

        if (victim == null || IS_NPC(victim)) {
            return;
        }
        to_room_vnum = hometown_table[victim.hometown].recall[IS_GOOD(victim) ? 0 : IS_NEUTRAL(victim) ? 1 : IS_EVIL(victim) ? 2 : 1];

        if ((location = get_room_index(to_room_vnum)) == null) {
            send_to_char("You are completely lost.\n", victim);
            return;
        }
/*
    if (victim.desc != null &&
    (current_time - victim.last_fight_time) < FIGHT_DELAY_TIME)
      {
    send_to_char("You are too pumped to pray now.\n",victim);
    return;
      }
*/
        if (IS_SET(victim.in_room.room_flags, ROOM_NO_RECALL) ||
                IS_AFFECTED(victim, AFF_CURSE) ||
                IS_RAFFECTED(victim.in_room, AFF_ROOM_CURSE)) {
            send_to_char("Spell failed.\n", victim);
            return;
        }

        if (victim.fighting != null) {
            if (victim == ch) {
                gain_exp(victim, 0 - (victim.level + 25));
            }
            stop_fighting(victim, true);
        }

        ch.move /= 2;
        act("$n disappears.", victim, null, null, TO_ROOM);
        char_from_room(victim);
        char_to_room(victim, location);
        do_visible(ch);
        act("$n appears in the room.", victim, null, null, TO_ROOM);
        do_look(victim, "auto");

        if (victim.pet != null) {
            char_from_room(victim.pet);
            char_to_room(victim.pet, location);
            do_look(victim.pet, "auto");
        }

    }

/*
 * Draconian spells.
 */

    static void spell_acid_breath(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam, hp_dam, dice_dam, hpch;

        act("$n spits acid at $N.", ch, null, victim, TO_NOTVICT);
        act("$n spits a stream of corrosive acid at you.", ch, null, victim, TO_VICT);
        act("You spit acid at $N.", ch, null, victim, TO_CHAR);

        hpch = UMAX(12, ch.hit);
        hp_dam = number_range(hpch / 11 + 1, hpch / 6);
        dice_dam = dice(level, 16);

        dam = UMAX(hp_dam + dice_dam / 10, dice_dam + hp_dam / 10);

        if (saves_spell(level, victim, DAM_ACID)) {
            acid_effect(victim, level / 2, dam / 4, TARGET_CHAR);
            damage(ch, victim, dam / 2, sn, DAM_ACID, true);
        } else {
            acid_effect(victim, level, dam, TARGET_CHAR);
            damage(ch, victim, dam, sn, DAM_ACID, true);
        }
    }


    static void spell_fire_breath(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        CHAR_DATA vch, vch_next;
        int dam, hp_dam, dice_dam;
        int hpch;

        act("$n breathes forth a cone of fire.", ch, null, victim, TO_NOTVICT);
        act("$n breathes a cone of hot fire over you!", ch, null, victim, TO_VICT);
        act("You breath forth a cone of fire.", ch, null, null, TO_CHAR);

        hpch = UMAX(10, ch.hit);
        hp_dam = number_range(hpch / 9 + 1, hpch / 5);
        dice_dam = dice(level, 20);

        dam = UMAX(hp_dam + dice_dam / 10, dice_dam + hp_dam / 10);
        fire_effect(victim.in_room, level, dam / 2, TARGET_ROOM);

        for (vch = victim.in_room.people; vch != null; vch = vch_next) {
            vch_next = vch.next_in_room;

            if (is_safe_spell(ch, vch, true)
                    || (IS_NPC(vch) && IS_NPC(ch)
                    && (ch.fighting != vch /*|| vch.fighting != ch */))) {
                continue;
            }
            if (is_safe(ch, vch)) {
                continue;
            }

            if (vch == victim) /* full damage */ {
                if (saves_spell(level, vch, DAM_FIRE)) {
                    fire_effect(vch, level / 2, dam / 4, TARGET_CHAR);
                    damage(ch, vch, dam / 2, sn, DAM_FIRE, true);
                } else {
                    fire_effect(vch, level, dam, TARGET_CHAR);
                    damage(ch, vch, dam, sn, DAM_FIRE, true);
                }
            } else /* partial damage */ {
                if (saves_spell(level - 2, vch, DAM_FIRE)) {
                    fire_effect(vch, level / 4, dam / 8, TARGET_CHAR);
                    damage(ch, vch, dam / 4, sn, DAM_FIRE, true);
                } else {
                    fire_effect(vch, level / 2, dam / 4, TARGET_CHAR);
                    damage(ch, vch, dam / 2, sn, DAM_FIRE, true);
                }
            }
        }
    }

    static void spell_frost_breath(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        CHAR_DATA vch, vch_next;
        int dam, hp_dam, dice_dam, hpch;

        act("$n breathes out a freezing cone of frost!", ch, null, victim, TO_NOTVICT);
        act("$n breathes a freezing cone of frost over you!",
                ch, null, victim, TO_VICT);
        act("You breath out a cone of frost.", ch, null, null, TO_CHAR);

        hpch = UMAX(12, ch.hit);
        hp_dam = number_range(hpch / 11 + 1, hpch / 6);
        dice_dam = dice(level, 16);

        dam = UMAX(hp_dam + dice_dam / 10, dice_dam + hp_dam / 10);
        cold_effect(victim.in_room, level, dam / 2, TARGET_ROOM);

        for (vch = victim.in_room.people; vch != null; vch = vch_next) {
            vch_next = vch.next_in_room;

            if (is_safe_spell(ch, vch, true)
                    || (IS_NPC(vch) && IS_NPC(ch)
                    && (ch.fighting != vch /*|| vch.fighting != ch*/))) {
                continue;
            }
            if (is_safe(ch, vch)) {
                continue;
            }


            if (vch == victim) /* full damage */ {
                if (saves_spell(level, vch, DAM_COLD)) {
                    cold_effect(vch, level / 2, dam / 4, TARGET_CHAR);
                    damage(ch, vch, dam / 2, sn, DAM_COLD, true);
                } else {
                    cold_effect(vch, level, dam, TARGET_CHAR);
                    damage(ch, vch, dam, sn, DAM_COLD, true);
                }
            } else {
                if (saves_spell(level - 2, vch, DAM_COLD)) {
                    cold_effect(vch, level / 4, dam / 8, TARGET_CHAR);
                    damage(ch, vch, dam / 4, sn, DAM_COLD, true);
                } else {
                    cold_effect(vch, level / 2, dam / 4, TARGET_CHAR);
                    damage(ch, vch, dam / 2, sn, DAM_COLD, true);
                }
            }
        }
    }


    static void spell_gas_breath(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA vch;
        CHAR_DATA vch_next;
        int dam, hp_dam, dice_dam, hpch;

        act("$n breathes out a cloud of poisonous gas!", ch, null, null, TO_ROOM);
        act("You breath out a cloud of poisonous gas.", ch, null, null, TO_CHAR);

        hpch = UMAX(16, ch.hit);
        hp_dam = number_range(hpch / 15 + 1, 8);
        dice_dam = dice(level, 12);

        dam = UMAX(hp_dam + dice_dam / 10, dice_dam + hp_dam / 10);
        poison_effect(ch.in_room, level, dam, TARGET_ROOM);

        for (vch = ch.in_room.people; vch != null; vch = vch_next) {
            vch_next = vch.next_in_room;

            if (is_safe_spell(ch, vch, true)
                    || (IS_NPC(ch) && IS_NPC(vch)
                    && (ch.fighting == vch || vch.fighting == ch))) {
                continue;
            }
            if (is_safe(ch, vch)) {
                continue;
            }
            if (!IS_NPC(ch) && vch != ch &&
                    ch.fighting != vch && vch.fighting != ch &&
                    (IS_SET(vch.affected_by, AFF_CHARM) || !IS_NPC(vch))) {
                if (!can_see(vch, ch)) {
                    do_yell(vch, "Help someone is attacking me!");
                } else {
                    TextBuffer buf = new TextBuffer();
                    buf.sprintf("Die, %s, you sorcerous dog!",
                            (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(vch)) ? ch.doppel.name : ch.name);
                    do_yell(vch, buf.toString());
                }
            }

            if (saves_spell(level, vch, DAM_POISON)) {
                poison_effect(vch, level / 2, dam / 4, TARGET_CHAR);
                damage(ch, vch, dam / 2, sn, DAM_POISON, true);
            } else {
                poison_effect(vch, level, dam, TARGET_CHAR);
                damage(ch, vch, dam, sn, DAM_POISON, true);
            }
        }
    }

    static void spell_lightning_breath(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam, hp_dam, dice_dam, hpch;

        act("$n breathes a bolt of lightning at $N.", ch, null, victim, TO_NOTVICT);
        act("$n breathes a bolt of lightning at you!", ch, null, victim, TO_VICT);
        act("You breathe a bolt of lightning at $N.", ch, null, victim, TO_CHAR);

        hpch = UMAX(10, ch.hit);
        hp_dam = number_range(hpch / 9 + 1, hpch / 5);
        dice_dam = dice(level, 20);

        dam = UMAX(hp_dam + dice_dam / 10, dice_dam + hp_dam / 10);

        if (IS_AFFECTED(victim, AFF_GROUNDING)) {
            send_to_char("The electricity fizzles at your foes.\n", victim);
            act("A lightning bolt fizzles at $N's foes.\n",
                    ch, null, victim, TO_ROOM);
            return;
        }

        if (saves_spell(level, victim, DAM_LIGHTNING)) {
            shock_effect(victim, level / 2, dam / 4, TARGET_CHAR);
            damage(ch, victim, dam / 2, sn, DAM_LIGHTNING, true);
        } else {
            shock_effect(victim, level, dam, TARGET_CHAR);
            damage(ch, victim, dam, sn, DAM_LIGHTNING, true);
        }
    }

    /**
     * Spells for mega1.are from Glop/Erkenbrand.
     */
    static void spell_general_purpose(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = number_range(25, 100);
        if (saves_spell(level, victim, DAM_PIERCE)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_PIERCE, true);
    }

    static void spell_high_explosive(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = number_range(30, 120);
        if (saves_spell(level, victim, DAM_PIERCE)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_PIERCE, true);
    }


    static void spell_find_object(int level, CHAR_DATA ch) {
        OBJ_DATA obj;
        OBJ_DATA in_obj;
        boolean found;
        int number, max_found;

        found = false;
        number = 0;
        max_found = IS_IMMORTAL(ch) ? 200 : 2 * level;

        TextBuffer buf = new TextBuffer();
        StringBuilder buffer = new StringBuilder();

        for (obj = object_list; obj != null; obj = obj.next) {
            if (!can_see_obj(ch, obj) || !is_name(target_name, obj.name)
                    || number_percent() > 2 * level
                    || ch.level < obj.level) {
                continue;
            }

            found = true;
            number++;

            for (in_obj = obj; in_obj.in_obj != null; in_obj = in_obj.in_obj) {
            }

            if (in_obj.carried_by != null && can_see(ch, in_obj.carried_by)) {
                buf.sprintf("one is carried by %s\n", PERS(in_obj.carried_by, ch));
            } else {
                if (IS_IMMORTAL(ch) && in_obj.in_room != null) {
                    buf.sprintf("one is in %s [Room %d]\n", in_obj.in_room.name, in_obj.in_room.vnum);
                } else {
                    buf.sprintf("one is in %s\n", in_obj.in_room == null ? "somewhere" : in_obj.in_room.name);
                }
            }

            buf.upfirst();
            buffer.append(buf);

            if (number >= max_found) {
                break;
            }
        }

        if (!found) {
            send_to_char("Nothing like that in heaven or earth.\n", ch);
        } else {
            page_to_char(buffer, ch);
        }


    }

    static void spell_lightning_shield(Skill sn, int level, CHAR_DATA ch) {

        if (is_affected_room(ch.in_room, sn)) {
            send_to_char("This room has already shielded.\n", ch);
            return;
        }

        if (is_affected(ch, sn)) {
            send_to_char("This spell is used too recently.\n", ch);
            return;
        }


        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_ROOM_AFFECTS;
        af.type = sn;
        af.level = ch.level;
        af.duration = level / 40;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_ROOM_L_SHIELD;
        affect_to_room(ch.in_room, af);

        AFFECT_DATA af2 = new AFFECT_DATA();
        af2.where = TO_AFFECTS;
        af2.type = sn;
        af2.level = ch.level;
        af2.duration = level / 10;
        af2.modifier = 0;
        af2.location = APPLY_NONE;
        af2.bitvector = 0;
        affect_to_char(ch, af2);

        ch.in_room.owner = ch.name;
        send_to_char("The room starts to be filled with lightnings.\n", ch);
        act("The room starts to be filled with $n's lightnings.", ch, null, null, TO_ROOM);
    }

    static void spell_shocking_trap(Skill sn, int level, CHAR_DATA ch) {

        if (is_affected_room(ch.in_room, sn)) {
            send_to_char("This room has already trapped with shocks waves.\n", ch);
            return;
        }

        if (is_affected(ch, sn)) {
            send_to_char("This spell is used too recently.\n", ch);
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_ROOM_AFFECTS;
        af.type = sn;
        af.level = ch.level;
        af.duration = level / 40;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_ROOM_SHOCKING;
        affect_to_room(ch.in_room, af);

        AFFECT_DATA af2 = new AFFECT_DATA();
        af2.where = TO_AFFECTS;
        af2.type = sn;
        af2.level = level;
        af2.duration = ch.level / 10;
        af2.modifier = 0;
        af2.location = APPLY_NONE;
        af2.bitvector = 0;
        affect_to_char(ch, af2);
        send_to_char("The room starts to be filled with shock waves.\n", ch);
        act("The room starts to be filled with $n's shock waves.", ch, null, null, TO_ROOM);
    }

    static void spell_acid_arrow(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 12);
        if (saves_spell(level, victim, DAM_ACID)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_ACID, true);
    }

/* energy spells */

    static void spell_etheral_fist(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 12);
        if (saves_spell(level, victim, DAM_ENERGY)) {
            dam /= 2;
        }
        act("A fist of black, otherworldly ether rams into $N, leaving $M looking stunned!"
                , ch, null, victim, TO_NOTVICT);
        damage(ch, victim, dam, sn, DAM_ENERGY, true);
    }

    static void spell_spectral_furor(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 8);
        if (saves_spell(level, victim, DAM_ENERGY)) {
            dam /= 2;
        }
        act("The fabric of the cosmos strains in fury about $N!",
                ch, null, victim, TO_NOTVICT);
        damage(ch, victim, dam, sn, DAM_ENERGY, true);
    }

    static void spell_disruption(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 9);
        if (saves_spell(level, victim, DAM_ENERGY)) {
            dam /= 2;
        }
        act("A weird energy encompasses $N, causing you to question $S continued existence.",
                ch, null, victim, TO_NOTVICT);
        damage(ch, victim, dam, sn, DAM_ENERGY, true);
    }


    static void spell_sonic_resonance(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 7);
        if (saves_spell(level, victim, DAM_ENERGY)) {
            dam /= 2;
        }
        act("A cylinder of kinetic energy enshrouds $N causing $S to resonate.",
                ch, null, victim, TO_NOTVICT);
        damage(ch, victim, dam, sn, DAM_ENERGY, true);
        WAIT_STATE(victim, sn.beats);
    }
/* mental */

    static void spell_mind_wrack(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 7);
        if (saves_spell(level, victim, DAM_MENTAL)) {
            dam /= 2;
        }
        act("$n stares intently at $N, causing $N to seem very lethargic.",
                ch, null, victim, TO_NOTVICT);
        damage(ch, victim, dam, sn, DAM_MENTAL, true);
    }

    static void spell_mind_wrench(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 9);
        if (saves_spell(level, victim, DAM_MENTAL)) {
            dam /= 2;
        }
        act("$n stares intently at $N, causing $N to seem very hyperactive.",
                ch, null, victim, TO_NOTVICT);
        damage(ch, victim, dam, sn, DAM_MENTAL, true);
    }
/* acid */

    static void spell_sulfurus_spray(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 7);
        if (saves_spell(level, victim, DAM_ACID)) {
            dam /= 2;
        }
        act("A stinking spray of sulfurous liquid rains down on $N.",
                ch, null, victim, TO_NOTVICT);
        damage(ch, victim, dam, sn, DAM_ACID, true);
    }

    static void spell_caustic_font(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 9);
        if (saves_spell(level, victim, DAM_ACID)) {
            dam /= 2;
        }
        act("A fountain of caustic liquid forms below $N.  The smell of $S degenerating tissues is revolting! ",
                ch, null, victim, TO_NOTVICT);
        damage(ch, victim, dam, sn, DAM_ACID, true);
    }

    static void spell_acetum_primus(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 8);
        if (saves_spell(level, victim, DAM_ACID)) {
            dam /= 2;
        }
        act("A cloak of primal acid enshrouds $N, sparks form as it consumes all it touches. ",
                ch, null, victim, TO_NOTVICT);
        damage(ch, victim, dam, sn, DAM_ACID, true);
    }

/*  Electrical  */

    static void spell_galvanic_whip(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 7);
        if (saves_spell(level, victim, DAM_LIGHTNING)) {
            dam /= 2;
        }
        act("$n conjures a whip of ionized particles, which lashes ferociously at $N.",
                ch, null, victim, TO_NOTVICT);
        damage(ch, victim, dam, sn, DAM_LIGHTNING, true);
    }


    static void spell_magnetic_trust(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 8);
        if (saves_spell(level, victim, DAM_LIGHTNING)) {
            dam /= 2;
        }
        act("An unseen energy moves nearby, causing your hair to stand on end!",
                ch, null, victim, TO_NOTVICT);
        damage(ch, victim, dam, sn, DAM_LIGHTNING, true);
    }

    static void spell_quantum_spike(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 9);
        if (saves_spell(level, victim, DAM_LIGHTNING)) {
            dam /= 2;
        }
        act("$N seems to dissolve into tiny unconnected particles, then is painfully reassembled.",
                ch, null, victim, TO_NOTVICT);
        damage(ch, victim, dam, sn, DAM_LIGHTNING, true);
    }

/* negative */

    static void spell_hand_of_undead(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        if (saves_spell(level, victim, DAM_NEGATIVE)) {
            send_to_char("You feel a momentary chill.\n", victim);
            return;
        }

        if ((IS_NPC(victim) && IS_SET(victim.act, ACT_UNDEAD))
                || IS_VAMPIRE(victim)) {
            send_to_char("Your victim is unaffected by hand of undead.\n", ch);
            return;
        }
        if (victim.level <= 2) {
            dam = ch.hit + 1;
        } else {
            dam = dice(level, 10);
            victim.mana /= 2;
            victim.move /= 2;
            ch.hit += dam / 2;
        }

        send_to_char("You feel your life slipping away!\n", victim);
        act("$N is grasped by an incomprehensible hand of undead!",
                ch, null, victim, TO_NOTVICT);
        damage(ch, victim, dam, sn, DAM_NEGATIVE, true);
    }

/* travel via astral plains */

    static void spell_astral_walk(int level, CHAR_DATA ch) {
        CHAR_DATA victim;
        boolean gate_pet;


        if ((victim = get_char_world(ch, target_name)) == null
                || victim == ch
                || victim.in_room == null
                || !can_see_room(ch, victim.in_room)
                || IS_SET(victim.in_room.room_flags, ROOM_SAFE)
                || IS_SET(victim.in_room.room_flags, ROOM_PRIVATE)
                || IS_SET(victim.in_room.room_flags, ROOM_SOLITARY)
                || IS_SET(ch.in_room.room_flags, ROOM_NOSUMMON)
                || IS_SET(victim.in_room.room_flags, ROOM_NOSUMMON)
                || victim.level >= level + 3
/*    ||   (!IS_NPC(victim) && victim.level >= LEVEL_HERO)  * NOT trust */
                || saves_spell(level, victim, DAM_OTHER)
                || (IS_NPC(victim) && is_safe_nomessage(ch, victim) && IS_SET(victim.imm_flags, IMM_SUMMON))
                || (!IS_NPC(victim) && is_safe_nomessage(ch, victim) && IS_SET(victim.act, PLR_NOSUMMON))
                || (!IS_NPC(victim) && ch.in_room.area != victim.in_room.area)
                || (IS_NPC(victim) && saves_spell(level, victim, DAM_OTHER))) {
            send_to_char("You failed.\n", ch);
            return;
        }
        gate_pet = ch.pet != null && ch.in_room == ch.pet.in_room;

        TextBuffer buf = new TextBuffer();
        act("$n disappears in a flash of light!", ch, null, null, TO_ROOM);
        buf.sprintf("You travel via astral planes and go to %s.\n", victim.name);
        send_to_char(buf, ch);
        char_from_room(ch);
        char_to_room(ch, victim.in_room);

        act("$n appears in a flash of light!", ch, null, null, TO_ROOM);
        do_look(ch, "auto");

        if (gate_pet) {
            act("$n disappears in a flash of light!", ch.pet, null, null, TO_ROOM);
            send_to_char(buf, ch.pet);
            char_from_room(ch.pet);
            char_to_room(ch.pet, victim.in_room);
            act("$n appears in a flash of light!", ch.pet, null, null, TO_ROOM);
            do_look(ch.pet, "auto");
        }
    }

/* vampire version astral walk */

    static void spell_mist_walk(int level, CHAR_DATA ch) {
        CHAR_DATA victim;


        if ((victim = get_char_world(ch, target_name)) == null
                || victim == ch
                || !IS_VAMPIRE(ch)
                || victim.in_room == null
                || !can_see_room(ch, victim.in_room)
                || IS_SET(victim.in_room.room_flags, ROOM_SAFE)
                || IS_SET(victim.in_room.room_flags, ROOM_PRIVATE)
                || IS_SET(victim.in_room.room_flags, ROOM_SOLITARY)
                || IS_SET(ch.in_room.room_flags, ROOM_NOSUMMON)
                || IS_SET(victim.in_room.room_flags, ROOM_NOSUMMON)
                || victim.level >= level - 5
/*    ||   (!IS_NPC(victim) && victim.level >= LEVEL_HERO)  * NOT trust */
                || saves_spell(level, victim, DAM_OTHER)
                || (IS_NPC(victim) && is_safe_nomessage(ch, victim) && IS_SET(victim.imm_flags, IMM_SUMMON))
                || (!IS_NPC(victim) && is_safe_nomessage(ch, victim) && IS_SET(victim.act, PLR_NOSUMMON))
                || (!IS_NPC(victim) && ch.in_room.area != victim.in_room.area)
                || (IS_NPC(victim) && saves_spell(level, victim, DAM_OTHER))) {
            send_to_char("You failed.\n", ch);
            return;
        }

        act("$n dissolves into a cloud of glowing mist, then vanishes!", ch, null, null, TO_ROOM);
        send_to_char("You dissolve into a cloud of glowing mist, then flow to your target.\n", ch);

        char_from_room(ch);
        char_to_room(ch, victim.in_room);

        act("A cloud of glowing mist engulfs you, then withdraws to unveil $n!", ch, null, null, TO_ROOM);
        do_look(ch, "auto");

    }

/*  Cleric version of astra_walk  */

    static void spell_solar_flight(int level, CHAR_DATA ch) {
        CHAR_DATA victim;


        if (time_info.hour > 18 || time_info.hour < 8) {
            send_to_char("You need sunlight for solar flight.\n", ch);
            return;
        }

        if ((victim = get_char_world(ch, target_name)) == null
                || victim == ch
                || victim.in_room == null
                || !can_see_room(ch, victim.in_room)
                || IS_SET(victim.in_room.room_flags, ROOM_SAFE)
                || IS_SET(victim.in_room.room_flags, ROOM_PRIVATE)
                || IS_SET(victim.in_room.room_flags, ROOM_SOLITARY)
                || IS_SET(ch.in_room.room_flags, ROOM_NOSUMMON)
                || IS_SET(victim.in_room.room_flags, ROOM_NOSUMMON)
                || victim.level >= level + 1
/*    ||   (!IS_NPC(victim) && victim.level >= LEVEL_HERO)  * NOT trust */
                || saves_spell(level, victim, DAM_OTHER)
                || (IS_NPC(victim) && is_safe_nomessage(ch, victim) && IS_SET(victim.imm_flags, IMM_SUMMON))
                || (!IS_NPC(victim) && is_safe_nomessage(ch, victim) && IS_SET(victim.act, PLR_NOSUMMON))
                || (!IS_NPC(victim) && ch.in_room.area != victim.in_room.area)
                || (IS_NPC(victim) && saves_spell(level, victim, DAM_OTHER))) {
            send_to_char("You failed.\n", ch);
            return;
        }

        act("$n disappears in a blinding flash of light!", ch, null, null, TO_ROOM);
        send_to_char("You dissolve in a blinding flash of light!.\n", ch);

        char_from_room(ch);
        char_to_room(ch, victim.in_room);

        act("$n appears in a blinding flash of light!", ch, null, null, TO_ROOM);
        do_look(ch, "auto");

    }

/* travel via astral plains */

    static void spell_helical_flow(int level, CHAR_DATA ch) {
        CHAR_DATA victim;


        if ((victim = get_char_world(ch, target_name)) == null
                || victim == ch
                || victim.in_room == null
                || !can_see_room(ch, victim.in_room)
                || IS_SET(victim.in_room.room_flags, ROOM_SAFE)
                || IS_SET(victim.in_room.room_flags, ROOM_PRIVATE)
                || IS_SET(victim.in_room.room_flags, ROOM_SOLITARY)
                || IS_SET(ch.in_room.room_flags, ROOM_NOSUMMON)
                || IS_SET(victim.in_room.room_flags, ROOM_NOSUMMON)
                || victim.level >= level + 3
/*    ||   (!IS_NPC(victim) && victim.level >= LEVEL_HERO)  * NOT trust */
                || saves_spell(level, victim, DAM_OTHER)
                || (IS_NPC(victim) && is_safe_nomessage(ch, victim) && IS_SET(victim.imm_flags, IMM_SUMMON))
                || (!IS_NPC(victim) && is_safe_nomessage(ch, victim) && IS_SET(victim.act, PLR_NOSUMMON))
                || (!IS_NPC(victim) && ch.in_room.area != victim.in_room.area)
                || (IS_NPC(victim) && saves_spell(level, victim, DAM_OTHER))) {
            send_to_char("You failed.\n", ch);
            return;
        }

        act("$n coils into an ascending column of colour, vanishing into thin air.", ch, null, null, TO_ROOM);
        send_to_char("You coils into an ascending column of colour, and vanishing into thin air.\n", ch);

        char_from_room(ch);
        char_to_room(ch, victim.in_room);

        act("A coil of colours descends from above, revealing $n as it dissipates.", ch, null, null, TO_ROOM);
        do_look(ch, "auto");

    }


    static void spell_corruption(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_CORRUPTION)) {
            act("$N is already corrupting.\n", ch, null, victim, TO_CHAR);
            return;
        }

        if (saves_spell(level, victim, DAM_NEGATIVE) ||
                (IS_NPC(victim) && IS_SET(victim.act, ACT_UNDEAD))) {
            if (ch == victim) {
                send_to_char("You feel momentarily ill, but it passes.\n", ch);
            } else {
                act("$N seems to be unaffected.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level * 3 / 4;
        af.duration = (10 + level / 5);
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_CORRUPTION;
        affect_join(victim, af);

        send_to_char
                ("You scream in agony as you start to decay into dust.\n", victim);
        act("$n screams in agony as $n start to decay into dust.",
                victim, null, null, TO_ROOM);
    }


    static void spell_hurricane(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA vch;
        CHAR_DATA vch_next;
        int dam, hp_dam, dice_dam, hpch;

        act("$n prays the gods of the storm for help.", ch, null, null, TO_NOTVICT);
        act("You pray the gods of the storm to help you.", ch, null, null, TO_CHAR);

        hpch = UMAX(16, ch.hit);
        hp_dam = number_range(hpch / 15 + 1, 8);
        dice_dam = dice(level, 12);

        dam = UMAX(hp_dam + dice_dam / 10, dice_dam + hp_dam / 10);

        for (vch = ch.in_room.people; vch != null; vch = vch_next) {
            vch_next = vch.next_in_room;

            if (is_safe_spell(ch, vch, true)
                    || (IS_NPC(ch) && IS_NPC(vch)
                    && (ch.fighting == vch || vch.fighting == ch))) {
                continue;
            }
            if (is_safe(ch, vch)) {
                continue;
            }
            if (!IS_NPC(ch) && vch != ch &&
                    ch.fighting != vch && vch.fighting != ch &&
                    (IS_SET(vch.affected_by, AFF_CHARM) || !IS_NPC(vch))) {
                if (!can_see(vch, ch)) {
                    do_yell(vch, "Help someone is attacking me!");
                } else {
                    TextBuffer buf = new TextBuffer();
                    buf.sprintf("Die, %s, you sorcerous dog!",
                            (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(vch)) ? ch.doppel.name : ch.name);
                    do_yell(vch, buf.toString());
                }
            }

            if (!IS_AFFECTED(vch, AFF_FLYING)) {
                dam /= 2;
            }

            if (vch.size == SIZE_TINY) {
                dam *= 1.5;
            } else if (vch.size == SIZE_SMALL) {
                dam *= 1.3;
            } else if (vch.size == SIZE_MEDIUM) {
                dam *= 1;
            } else if (vch.size == SIZE_LARGE) {
                dam *= 0.9;
            } else if (vch.size == SIZE_HUGE) {
                dam *= 0.7;
            } else {
                dam *= 0.5;
            }

            if (saves_spell(level, vch, DAM_OTHER)) {
                damage(ch, vch, dam / 2, sn, DAM_OTHER, true);
            } else {
                damage(ch, vch, dam, sn, DAM_OTHER, true);
            }
        }
    }


    static void spell_detect_undead(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_DETECT_UNDEAD)) {
            if (victim == ch) {
                send_to_char("You can already sense undead.\n", ch);
            } else {
                act("$N can already detect undead.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = (5 + level / 3);
        af.modifier = 0;
        af.location = APPLY_NONE;
        af.bitvector = AFF_DETECT_UNDEAD;
        affect_to_char(victim, af);
        send_to_char("Your eyes tingle.\n", victim);
        if (ch != victim) {
            send_to_char("Ok.\n", ch);
        }
    }


    static void spell_take_revenge(CHAR_DATA ch) {
        OBJ_DATA obj;
        OBJ_DATA in_obj;
        ROOM_INDEX_DATA room = null;
        boolean found = false;

        if (IS_NPC(ch)
                || ch.last_death_time == -1
                || current_time - ch.last_death_time > 600) {
            send_to_char("It is too late to take revenge.\n", ch);
            return;
        }

        for (obj = object_list; obj != null; obj = obj.next) {
            if (obj.pIndexData.vnum != OBJ_VNUM_CORPSE_PC
                    || !is_name(ch.name, obj.short_descr)) {
                continue;
            }

            found = true;
            for (in_obj = obj; in_obj.in_obj != null; in_obj = in_obj.in_obj) {
            }

            if (in_obj.carried_by != null) {
                room = in_obj.carried_by.in_room;
            } else {
                room = in_obj.in_room;
            }
            break;
        }

        if (!found || room == null) {
            send_to_char("Unluckily your corpse is devoured.\n", ch);
        } else {
            ROOM_INDEX_DATA prev_room;

            prev_room = ch.in_room;
            act("$n prays for transportation.", ch, null, null, TO_ROOM);
            char_from_room(ch);
            char_to_room(ch, room);
            if (cabal_area_check(ch)
                    || IS_ROOM_AFFECTED(ch.in_room, AFF_ROOM_PREVENT)) {
                send_to_char(" You failed to go to your corpse.\n", ch);
                char_from_room(ch);
                char_to_room(ch, prev_room);
            } else {
                do_look(ch, "auto");
            }
        }
    }


    static void spell_firestream(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        act("$n throws a stream of searing flames.", ch, null, victim, TO_NOTVICT);
        act("$n throws a stream of hot flames over you!", ch, null, victim, TO_VICT);
        act("You throw a stream of searing flames to $N.", ch, null, victim, TO_CHAR);

        dam = dice(level, 8);

        if (saves_spell(level, victim, DAM_FIRE)) {
            fire_effect(victim, level / 2, dam / 4, TARGET_CHAR);
            damage(ch, victim, dam / 2, sn, DAM_FIRE, true);
        } else {
            fire_effect(victim, level, dam, TARGET_CHAR);
            damage(ch, victim, dam, sn, DAM_FIRE, true);
        }
    }

    static void spell_summon_earth_elm(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA gch;
        CHAR_DATA elm;
        int i = 0;

        if (is_affected(ch, sn)) {
            send_to_char("You lack the power to create another elemental right now.\n",
                    ch);
            return;
        }

        send_to_char("You attempt to create an earth elemental.\n", ch);
        act("$n attempts to create an earth elemental.", ch, null, null, TO_ROOM);

        for (gch = char_list; gch != null; gch = gch.next) {
            if (IS_NPC(gch) && IS_AFFECTED(gch, AFF_CHARM) && gch.master == ch &&
                    (gch.pIndexData.vnum == MOB_VNUM_ELM_EARTH)) {
                i++;
                if (i > 2) {
                    send_to_char("More earth elementals are more than you can control!\n", ch);
                    return;
                }
            }
        }

        if (count_charmed(ch) != 0) {
            return;
        }

        elm = create_mobile(get_mob_index(MOB_VNUM_ELM_EARTH));


        for (i = 0; i < MAX_STATS; i++) {
            elm.perm_stat[i] = UMIN(25, 15 + ch.level / 10);
        }

        elm.perm_stat[STAT_STR] += 3;
        elm.perm_stat[STAT_INT] -= 1;
        elm.perm_stat[STAT_CON] += 2;

        elm.max_hit = IS_NPC(ch) ? URANGE(ch.max_hit, ch.max_hit, 30000)
                : UMIN((2 * ch.pcdata.perm_hit) + 400, 30000);
        elm.hit = elm.max_hit;
        elm.max_mana = IS_NPC(ch) ? ch.max_mana : ch.pcdata.perm_mana;
        elm.mana = elm.max_mana;
        elm.level = ch.level;
        for (i = 0; i < 3; i++) {
            elm.armor[i] = interpolate(elm.level, 100, -100);
        }
        elm.armor[3] = interpolate(elm.level, 100, 0);
        elm.gold = 0;
        elm.timer = 0;
        elm.damage[DICE_NUMBER] = 3;
        elm.damage[DICE_TYPE] = 10;
        elm.damage[DICE_BONUS] = ch.level / 2;

        char_to_room(elm, ch.in_room);
        send_to_char("You created an earth elemental!\n", ch);
        act("$n creates an earth elemental!", ch, null, null, TO_ROOM);
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

        elm.affected_by = SET_BIT(elm.affected_by, AFF_CHARM);
        elm.master = elm.leader = ch;

    }

    static void spell_frostbolt(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 10);
        if (saves_spell(level, victim, DAM_COLD)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_COLD, true);
    }

    static void spell_summon_air_elm(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA gch;
        CHAR_DATA elm;
        int i = 0;

        if (is_affected(ch, sn)) {
            send_to_char("You lack the power to create another elemental right now.\n",
                    ch);
            return;
        }

        send_to_char("You attempt to create an air elemental.\n", ch);
        act("$n attempts to create an air elemental.", ch, null, null, TO_ROOM);

        for (gch = char_list; gch != null; gch = gch.next) {
            if (IS_NPC(gch) && IS_AFFECTED(gch, AFF_CHARM) && gch.master == ch &&
                    (gch.pIndexData.vnum == MOB_VNUM_ELM_AIR)) {
                i++;
                if (i > 2) {
                    send_to_char("More air elementals are more than you can control!\n", ch);
                    return;
                }
            }
        }

        if (count_charmed(ch) != 0) {
            return;
        }

        elm = create_mobile(get_mob_index(MOB_VNUM_ELM_AIR));


        for (i = 0; i < MAX_STATS; i++) {
            elm.perm_stat[i] = UMIN(25, 15 + ch.level / 10);
        }

        elm.perm_stat[STAT_STR] += 3;
        elm.perm_stat[STAT_INT] -= 1;
        elm.perm_stat[STAT_CON] += 2;

        elm.max_hit = IS_NPC(ch) ? URANGE(ch.max_hit, ch.max_hit, 30000)
                : UMIN((4 * ch.pcdata.perm_hit) + 1000, 30000);
        elm.hit = elm.max_hit;
        elm.max_mana = IS_NPC(ch) ? ch.max_mana : ch.pcdata.perm_mana;
        elm.mana = elm.max_mana;
        elm.level = ch.level;
        for (i = 0; i < 3; i++) {
            elm.armor[i] = interpolate(elm.level, 100, -100);
        }
        elm.armor[3] = interpolate(elm.level, 100, 0);
        elm.gold = 0;
        elm.timer = 0;
        elm.damage[DICE_NUMBER] = 7;
        elm.damage[DICE_TYPE] = 4;
        elm.damage[DICE_BONUS] = ch.level / 2;

        char_to_room(elm, ch.in_room);
        send_to_char("You created an air elemental!\n", ch);
        act("$n creates an air elemental!", ch, null, null, TO_ROOM);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

        elm.affected_by = SET_BIT(elm.affected_by, AFF_CHARM);
        elm.master = elm.leader = ch;

    }

    static void spell_summon_water_elm(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA gch;
        CHAR_DATA elm;
        int i = 0;

        if (is_affected(ch, sn)) {
            send_to_char("You lack the power to create another elemental right now.\n",
                    ch);
            return;
        }

        send_to_char("You attempt to create a water elemental.\n", ch);
        act("$n attempts to create a water elemental.", ch, null, null, TO_ROOM);

        for (gch = char_list; gch != null; gch = gch.next) {
            if (IS_NPC(gch) && IS_AFFECTED(gch, AFF_CHARM) && gch.master == ch &&
                    (gch.pIndexData.vnum == MOB_VNUM_ELM_WATER)) {
                i++;
                if (i > 2) {
                    send_to_char("More water elementals are more than you can control!\n", ch);
                    return;
                }
            }
        }

        if (count_charmed(ch) != 0) {
            return;
        }

        elm = create_mobile(get_mob_index(MOB_VNUM_ELM_WATER));


        for (i = 0; i < MAX_STATS; i++) {
            elm.perm_stat[i] = UMIN(25, 15 + ch.level / 10);
        }

        elm.perm_stat[STAT_STR] += 3;
        elm.perm_stat[STAT_INT] -= 1;
        elm.perm_stat[STAT_CON] += 2;

        elm.max_hit = IS_NPC(ch) ? URANGE(ch.max_hit, ch.max_hit, 30000)
                : UMIN((5 * ch.pcdata.perm_hit) + 2000, 30000);
        elm.hit = elm.max_hit;
        elm.max_mana = IS_NPC(ch) ? ch.max_mana : ch.pcdata.perm_mana;
        elm.mana = elm.max_mana;
        elm.level = ch.level;
        for (i = 0; i < 3; i++) {
            elm.armor[i] = interpolate(elm.level, 100, -100);
        }
        elm.armor[3] = interpolate(elm.level, 100, 0);
        elm.gold = 0;
        elm.timer = 0;
        elm.damage[DICE_NUMBER] = 8;
        elm.damage[DICE_TYPE] = 4;
        elm.damage[DICE_BONUS] = ch.level / 2;

        char_to_room(elm, ch.in_room);
        send_to_char("You created a water elemental!\n", ch);
        act("$n creates a water elemental!", ch, null, null, TO_ROOM);
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

        elm.affected_by = SET_BIT(elm.affected_by, AFF_CHARM);
        elm.master = elm.leader = ch;

    }

    static void spell_summon_fire_elm(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA gch;
        CHAR_DATA elm;
        int i = 0;

        if (is_affected(ch, sn)) {
            send_to_char("You lack the power to create another elemental right now.\n",
                    ch);
            return;
        }

        send_to_char("You attempt to create a fire elemental.\n", ch);
        act("$n attempts to create a fire elemental.", ch, null, null, TO_ROOM);

        for (gch = char_list; gch != null; gch = gch.next) {
            if (IS_NPC(gch) && IS_AFFECTED(gch, AFF_CHARM) && gch.master == ch &&
                    (gch.pIndexData.vnum == MOB_VNUM_ELM_FIRE)) {
                i++;
                if (i > 2) {
                    send_to_char("More fire elementals are more than you can control!\n", ch);
                    return;
                }
            }
        }

        if (count_charmed(ch) != 0) {
            return;
        }

        elm = create_mobile(get_mob_index(MOB_VNUM_ELM_FIRE));


        for (i = 0; i < MAX_STATS; i++) {
            elm.perm_stat[i] = UMIN(25, 15 + ch.level / 10);
        }

        elm.perm_stat[STAT_STR] += 3;
        elm.perm_stat[STAT_INT] -= 1;
        elm.perm_stat[STAT_CON] += 2;

        elm.max_hit = IS_NPC(ch) ? URANGE(ch.max_hit, ch.max_hit, 30000)
                : UMIN((10 * ch.pcdata.perm_hit) + 1000, 30000);
        elm.hit = elm.max_hit;
        elm.max_mana = IS_NPC(ch) ? ch.max_mana : ch.pcdata.perm_mana;
        elm.mana = elm.max_mana;
        elm.level = ch.level;
        for (i = 0; i < 3; i++) {
            elm.armor[i] = interpolate(elm.level, 100, -100);
        }
        elm.armor[3] = interpolate(elm.level, 100, 0);
        elm.gold = 0;
        elm.timer = 0;
        elm.damage[DICE_NUMBER] = 11;
        elm.damage[DICE_TYPE] = 5;
        elm.damage[DICE_BONUS] = ch.level / 2 + 10;

        char_to_room(elm, ch.in_room);
        send_to_char("You created a fire elemental!\n", ch);
        act("$n creates a fire elemental!", ch, null, null, TO_ROOM);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

        elm.affected_by = SET_BIT(elm.affected_by, AFF_CHARM);
        elm.master = elm.leader = ch;

    }

    static void spell_summon_light_elm(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA gch;
        CHAR_DATA elm;
        int i = 0;

        if (is_affected(ch, sn)) {
            send_to_char("You lack the power to create another elemental right now.\n",
                    ch);
            return;
        }

        send_to_char("You attempt to create a lightning elemental.\n", ch);
        act("$n attempts to create a lightning elemental.", ch, null, null, TO_ROOM);

        for (gch = char_list; gch != null; gch = gch.next) {
            if (IS_NPC(gch) && IS_AFFECTED(gch, AFF_CHARM) && gch.master == ch &&
                    (gch.pIndexData.vnum == MOB_VNUM_ELM_LIGHT)) {
                i++;
                if (i > 2) {
                    send_to_char("More lightning elementals are more than you can control!\n", ch);
                    return;
                }
            }
        }

        if (count_charmed(ch) != 0) {
            return;
        }

        elm = create_mobile(get_mob_index(MOB_VNUM_ELM_LIGHT));


        for (i = 0; i < MAX_STATS; i++) {
            elm.perm_stat[i] = UMIN(25, 15 + ch.level / 10);
        }

        elm.perm_stat[STAT_STR] += 3;
        elm.perm_stat[STAT_INT] -= 1;
        elm.perm_stat[STAT_CON] += 2;

        elm.max_hit = IS_NPC(ch) ? URANGE(ch.max_hit, ch.max_hit, 30000)
                : UMIN((10 * ch.pcdata.perm_hit) + 4000, 30000);
        elm.hit = elm.max_hit;
        elm.max_mana = IS_NPC(ch) ? ch.max_mana : ch.pcdata.perm_mana;
        elm.mana = elm.max_mana;
        elm.level = ch.level;
        for (i = 0; i < 3; i++) {
            elm.armor[i] = interpolate(elm.level, 100, -100);
        }
        elm.armor[3] = interpolate(elm.level, 100, 0);
        elm.gold = 0;
        elm.timer = 0;
        elm.damage[DICE_NUMBER] = 13;
        elm.damage[DICE_TYPE] = 9;
        elm.damage[DICE_BONUS] = ch.level / 2 + 10;

        char_to_room(elm, ch.in_room);
        send_to_char("You created a lightning elemental!\n", ch);
        act("$n creates a lightning elemental!", ch, null, null, TO_ROOM);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

        elm.affected_by = SET_BIT(elm.affected_by, AFF_CHARM);
        elm.master = elm.leader = ch;

    }


    static void spell_fire_and_ice(int level, CHAR_DATA ch) {
        CHAR_DATA tmp_vict;
        CHAR_DATA tmp_next;
        int dam;
        Skill dam_sn;
        int movedam;

        dam = dice(level, 8);
        movedam = number_range(ch.level, 2 * ch.level);

        for (tmp_vict = ch.in_room.people; tmp_vict != null;
             tmp_vict = tmp_next) {
            tmp_next = tmp_vict.next_in_room;

            if (!is_safe_spell(ch, tmp_vict, true)) {
                if (!IS_NPC(ch) && tmp_vict != ch &&
                        ch.fighting != tmp_vict && tmp_vict.fighting != ch &&
                        (IS_SET(tmp_vict.affected_by, AFF_CHARM) || !IS_NPC(tmp_vict))) {
                    if (!can_see(tmp_vict, ch)) {
                        do_yell(tmp_vict, "Help someone is attacking me!");
                    } else {
                        TextBuffer buf = new TextBuffer();
                        buf.sprintf("Die, %s, you sorcerous dog!",
                                (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(tmp_vict)) ? ch.doppel.name : ch.name);
                        do_yell(tmp_vict, buf.toString());
                    }
                }

                if (saves_spell(level, tmp_vict, DAM_FIRE)) {
                    dam /= 2;
                }
                dam_sn = Skill.gsn_fireball;
                damage(ch, tmp_vict, dam, dam_sn, DAM_FIRE, true);
                tmp_vict.move -= UMIN(tmp_vict.move, movedam);

                if ((IS_NPC(tmp_vict) && tmp_vict.position == POS_DEAD) ||
                        (!IS_NPC(tmp_vict) && (current_time - tmp_vict.last_death_time) < 10)) {
                    if (saves_spell(level, tmp_vict, DAM_COLD)) {
                        dam /= 2;
                    }
                    dam_sn = Skill.gsn_iceball;
                    damage(ch, tmp_vict, dam, dam_sn, DAM_COLD, true);
                }

            }
        }
    }

    static void spell_grounding(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(victim, sn)) {
            if (victim == ch) {
                send_to_char("You are already at ground potential.\n", ch);
            } else {
                act("$N is already at ground potential.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 5 + level / 8;
        af.modifier = 0;
        af.location = APPLY_NONE;
        af.bitvector = AFF_GROUNDING;
        affect_to_char(victim, af);
        send_to_char("Your body is electrically grounded.\n", victim);
        if (ch != victim) {
            act("$N is grounded by your magic.", ch, null, victim, TO_CHAR);
        }
    }

    static void spell_tsunami(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        if ((ch.in_room.sector_type != SECT_WATER_SWIM)
                && (ch.in_room.sector_type != SECT_WATER_NOSWIM)) {
            send_to_char("You can't reach a water source to create a tsunami.\n", ch);
            ch.wait = 0;
            return;
        }

        act("An existing parcel of water rises up and forms a fist and pummels $n.",
                victim, null, null, TO_ROOM);
        act("An existing parcel of water rises up and forms a fist and pummels you.",
                victim, null, null, TO_CHAR);
        dam = dice(level, 16);
        damage(ch, victim, dam, sn, DAM_DROWNING, true);
    }

    static void spell_disenchant_armor(int level, CHAR_DATA ch, Object vo) {
        OBJ_DATA obj = (OBJ_DATA) vo;
        int result, fail;

        if (obj.wear_loc != -1) {
            send_to_char("The item must be carried to be enchanted.\n", ch);
            return;
        }

        /* find the bonuses */
        fail = 75;
        fail -= (level - obj.level) * 5;
        if (IS_SET(obj.extra_flags, ITEM_MAGIC)) {
            fail += 25;
        }

        fail = URANGE(5, fail, 95);

        result = number_percent();

        /* the moment of truth */
        if (result < (fail / 5))  /* item destroyed */ {
            act("$p flares blindingly... and evaporates!", ch, obj, null, TO_CHAR);
            act("$p flares blindingly... and evaporates!", ch, obj, null, TO_ROOM);
            extract_obj(obj);
            return;
        }

        if (result > (fail / 2)) /* item disenchanted */ {

            act("$p glows brightly, then fades.", ch, obj, null, TO_CHAR);
            act("$p glows brightly, then fades.", ch, obj, null, TO_ROOM);
            obj.enchanted = true;

            /* remove all affects */
            for (AFFECT_DATA paf = obj.affected, paf_next; paf != null; paf = paf_next) {
                paf_next = paf.next;
            }
            obj.affected = null;

            obj.enchanted = false;

            /* clear some flags */
            obj.extra_flags = REMOVE_BIT(obj.extra_flags, ITEM_GLOW);
            obj.extra_flags = REMOVE_BIT(obj.extra_flags, ITEM_HUM);
            obj.extra_flags = REMOVE_BIT(obj.extra_flags, ITEM_MAGIC);
            obj.extra_flags = REMOVE_BIT(obj.extra_flags, ITEM_INVIS);
            obj.extra_flags = REMOVE_BIT(obj.extra_flags, ITEM_NODROP);
            obj.extra_flags = REMOVE_BIT(obj.extra_flags, ITEM_NOREMOVE);

            return;
        }

        send_to_char("Nothing seemed to happen.\n", ch);
    }

    static void spell_disenchant_weapon(int level, CHAR_DATA ch, Object vo) {
        OBJ_DATA obj = (OBJ_DATA) vo;
        int result, fail;

        if (obj.item_type != ITEM_WEAPON) {
            send_to_char("That isn't a weapon.\n", ch);
            return;
        }

        if (obj.wear_loc != -1) {
            send_to_char("The item must be carried to be enchanted.\n", ch);
            return;
        }

        /* find the bonuses */
        fail = 75;
        fail -= (level - obj.level) * 5;
        if (IS_SET(obj.extra_flags, ITEM_MAGIC)) {
            fail += 25;
        }

        fail = URANGE(5, fail, 95);

        result = number_percent();

        /* the moment of truth */
        if (result < (fail / 5))  /* item destroyed */ {
            act("$p shivers violently and explodes!", ch, obj, null, TO_CHAR);
            act("$p shivers violently and explodeds!", ch, obj, null, TO_ROOM);
            extract_obj(obj);
            return;
        }

        if (result > (fail / 2)) /* item disenchanted */ {

            act("$p glows brightly, then fades.", ch, obj, null, TO_CHAR);
            act("$p glows brightly, then fades.", ch, obj, null, TO_ROOM);
            obj.enchanted = true;

            /* remove all affects */
            for (AFFECT_DATA paf = obj.affected, paf_next; paf != null; paf = paf_next) {
                paf_next = paf.next;
            }
            obj.affected = null;

            obj.enchanted = false;

            /* clear some flags */
            obj.extra_flags = REMOVE_BIT(obj.extra_flags, ITEM_GLOW);
            obj.extra_flags = REMOVE_BIT(obj.extra_flags, ITEM_HUM);
            obj.extra_flags = REMOVE_BIT(obj.extra_flags, ITEM_MAGIC);
            obj.extra_flags = REMOVE_BIT(obj.extra_flags, ITEM_INVIS);
            obj.extra_flags = REMOVE_BIT(obj.extra_flags, ITEM_NODROP);
            obj.extra_flags = REMOVE_BIT(obj.extra_flags, ITEM_NOREMOVE);

            return;
        }

        send_to_char("Nothing seemed to happen.\n", ch);
    }

    static void spell_absorb(Skill sn, int level, CHAR_DATA ch) {

        if (is_affected(ch, sn)) {
            send_to_char("You are already absorbing magic surrounding you.\n", ch);
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = gsn_absorb;
        af.level = level;
        af.duration = 3 + level / 10;
        af.modifier = 0;
        af.location = APPLY_NONE;
        af.bitvector = AFF_ABSORB;
        affect_to_char(ch, af);
        send_to_char("Your body is surrounded by an energy field.\n", ch);
    }

    static void spell_transfer_object(CHAR_DATA ch) {
        send_to_char("Not implemented!\n", ch);
    }

    static void spell_animate_object(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA mob;
        OBJ_DATA obj = (OBJ_DATA) vo;
        int i;

        if (!(obj.item_type == ITEM_WEAPON ||
                obj.item_type == ITEM_ARMOR)) {
            send_to_char("You can animate only armors and weapons.\n", ch);
            return;
        }

        if (is_affected(ch, sn)) {
            send_to_char("You cannot summon the strength to handle more undead bodies.\n", ch);
            return;
        }

        if (obj.level > level) {
            act("$p is too powerful for you to animate it.", ch, obj, null, TO_CHAR);
            return;
        }

        if (count_charmed(ch) != 0) {
            return;
        }

        if (obj.item_type == ITEM_ARMOR
                && !(CAN_WEAR(obj, ITEM_WEAR_BODY)
                || CAN_WEAR(obj, ITEM_WEAR_HANDS)
                || CAN_WEAR(obj, ITEM_WEAR_SHIELD))) {
            send_to_char("You can only animate that type of armor.\n", ch);
            return;
        }

        if (number_percent() > get_skill(ch, sn)) {
            act("$p violately explodes!", ch, obj, null, TO_CHAR);
            act("$p violately explodes!", ch, obj, null, TO_ROOM);
            extract_obj(obj);
            return;
        }

        if (obj.item_type == ITEM_WEAPON) {
            mob = create_mobile(get_mob_index(MOB_VNUM_WEAPON));
        } else {
            mob = create_mobile(get_mob_index(MOB_VNUM_ARMOR));
        }

        TextBuffer buf = new TextBuffer();
        buf.sprintf("animate %s", obj.name);
        mob.name = buf.toString();

        buf.sprintf(mob.short_descr, obj.short_descr);
        mob.short_descr = buf.toString();

        buf.sprintf("%s is here, staring at you!.\n", capitalize(obj.short_descr));
        mob.long_descr = buf.toString();

        char_to_room(mob, ch.in_room);
        mob.level = obj.level;

        for (i = 0; i < MAX_STATS; i++) {
            mob.perm_stat[i] = UMIN(25, ch.perm_stat[i]);
        }
        for (i = 0; i < 3; i++) {
            mob.armor[i] = interpolate(mob.level, 100, -100);
        }
        mob.armor[3] = interpolate(mob.level, 100, 0);

        if (obj.item_type == ITEM_WEAPON) {
            mob.hit = IS_NPC(ch) ? 100 :
                    UMIN((25 * mob.level) + 1000, 30000);
            mob.max_hit = mob.hit;
            mob.mana = ch.level * 40;
            mob.max_mana = mob.mana;
            mob.move = ch.level * 40;
            mob.max_move = mob.move;
            mob.timer = 0;
            mob.damage[DICE_NUMBER] = obj.value[1];
            mob.damage[DICE_TYPE] = obj.value[2];
            mob.damage[DICE_BONUS] = number_range(level / 10, level / 8);
        }

        if (obj.item_type == ITEM_ARMOR) {
            mob.hit = IS_NPC(ch) ? 100 :
                    UMIN((100 * mob.level) + 2000, 30000);
            mob.max_hit = mob.hit;
            mob.mana = ch.level * 40;
            mob.max_mana = mob.mana;
            mob.move = ch.level * 40;
            mob.max_move = mob.move;
            mob.timer = 0;
            mob.damage[DICE_NUMBER] = number_range(level / 15, level / 12);
            mob.damage[DICE_TYPE] = number_range(level / 3, level / 2);
            mob.damage[DICE_BONUS] = number_range(level / 10, level / 8);
        }
        mob.sex = ch.sex;
        mob.gold = 0;
        mob.master = mob.leader = ch;
        mob.affected_by = SET_BIT(mob.affected_by, AFF_CHARM);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = ch.level;
        af.duration = 1 + (obj.level / 30);
        af.modifier = 0;
        af.location = APPLY_NONE;
        af.bitvector = 0;
        affect_to_char(ch, af);

        act("You give life to $p with your power!\n", ch, obj, null, TO_CHAR);
        act("$n gives life to $p with $s power!\n", ch, obj, null, TO_ROOM);

        extract_obj(obj);
    }

    static void spell_windwall(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA vch;
        CHAR_DATA vch_next;
        int dam, hp_dam, dice_dam, hpch;

        act("$n raise a wall of wind striking everyone.", ch, null, null, TO_ROOM);
        act("You raise a wall of wind.", ch, null, null, TO_CHAR);

        hpch = UMAX(16, ch.hit);
        hp_dam = number_range(hpch / 15 + 1, 8);
        dice_dam = dice(level, 12);

        dam = UMAX(hp_dam + dice_dam / 10, dice_dam + hp_dam / 10);

        for (vch = ch.in_room.people; vch != null; vch = vch_next) {
            vch_next = vch.next_in_room;

            if (is_safe_spell(ch, vch, true)
                    || (IS_NPC(ch) && IS_NPC(vch)
                    && (ch.fighting == vch || vch.fighting == ch))) {
                continue;
            }
            if (is_safe(ch, vch)) {
                continue;
            }
            if (!IS_NPC(ch) && vch != ch &&
                    ch.fighting != vch && vch.fighting != ch &&
                    (IS_SET(vch.affected_by, AFF_CHARM) || !IS_NPC(vch))) {
                if (!can_see(vch, ch)) {
                    do_yell(vch, "Help someone is attacking me!");
                } else {
                    TextBuffer buf = new TextBuffer();
                    buf.sprintf("Die, %s, you sorcerous dog!",
                            (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(vch)) ?
                                    ch.doppel.name : ch.name);
                    do_yell(vch, buf.toString());
                }
            }

            if (!IS_AFFECTED(vch, AFF_FLYING)) {
                dam /= 2;
            }

            if (vch.size == SIZE_TINY) {
                dam *= 1.5;
            } else if (vch.size == SIZE_SMALL) {
                dam *= 1.3;
            } else if (vch.size == SIZE_MEDIUM) {
                dam *= 1;
            } else if (vch.size == SIZE_LARGE) {
                dam *= 0.9;
            } else if (vch.size == SIZE_HUGE) {
                dam *= 0.7;
            } else {
                dam *= 0.5;
            }

            if (saves_spell(level, vch, DAM_OTHER)) {
                damage(ch, vch, dam / 2, sn, DAM_OTHER, true);
            } else {
                damage(ch, vch, dam, sn, DAM_OTHER, true);
            }
        }
    }

    static void spell_earthfade(Skill sn, int level, CHAR_DATA ch) {

        if (IS_AFFECTED(ch, AFF_EARTHFADE)) {
            return;
        }

        if (ch.in_room.sector_type == SECT_AIR
                || ch.in_room.sector_type == SECT_WATER_SWIM
                || ch.in_room.sector_type == SECT_WATER_NOSWIM) {
            send_to_char("You cannot reach the earth to fade.\n", ch);
            return;
        }

        if (MOUNTED(ch) != null) {
            send_to_char("You can't fade to earth while mounted.\n", ch);
            return;
        }

        act("$n fades into earth.", ch, null, null, TO_ROOM);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = (level / 8 + 10);
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_EARTHFADE;
        affect_to_char(ch, af);
        send_to_char("You fade into earth.\n", ch);
    }

    static void spell_earthmaw(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        TextBuffer buf = new TextBuffer();
        buf.sprintf("You tremble the earth underneath the %s.\n", victim.name);
        send_to_char(buf, ch);
        act("$n trembles the earth underneath you!.", ch, null, victim, TO_VICT);
        if (IS_AFFECTED(victim, AFF_FLYING)) {
            dam = 0;
        } else {
            dam = dice(level, 16);
        }
        damage(ch, victim, dam, sn, DAM_BASH, true);
    }

    static void spell_drain(Skill sn, CHAR_DATA ch, Object vo) {
        OBJ_DATA obj = (OBJ_DATA) vo;
        int drain;

        if (!IS_SET(obj.extra_flags, ITEM_MAGIC)) {
            send_to_char("That item is not magical.\n", ch);
            return;
        }

        switch (obj.item_type) {
            default:
                drain = 1;
                break;
            case ITEM_ARMOR:
                drain = obj.value[3];
                break;
            case ITEM_TREASURE:
                drain = 4;
                break;
            case ITEM_POTION:
                drain = 8;
                break;
            case ITEM_SCROLL:
            case ITEM_STAFF:
            case ITEM_WAND:
                drain = 12;
                break;
            case ITEM_WEAPON:
                drain = obj.value[1] + obj.value[2] / 2;
                break;

            case ITEM_LIGHT:
                if (obj.value[2] == -1) {
                    drain = 10;
                } else {
                    drain = 4;
                }
                break;
        }

        for (AFFECT_DATA paf = obj.affected; paf != null; paf = paf.next) {
            drain += 5;
        }

        drain *= dice(2, 5);
        drain += obj.level / 2;

        if (number_percent() > get_skill(ch, sn)) {
            act("$p evaporates!", ch, obj, null, TO_ROOM);
            act("$p evaporates, but you fail to channel the energy.", ch, obj, null, TO_CHAR);
        } else {
            act("$p evaporates as $n drains its energy!", ch, obj, null, TO_ROOM);
            act("$p evaporates as you drain its energy!", ch, obj, null, TO_CHAR);
            ch.mana = UMIN(ch.mana + drain, ch.max_mana);
        }
        extract_obj(obj);

    }

    static void spell_soften(Skill sn, int level, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(victim, sn)) {
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 5 + level / 10;
        af.location = APPLY_AC;
        af.modifier = 4 * level;
        af.bitvector = AFF_FAERIE_FIRE;
        affect_to_char(victim, af);

        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = (10 + level / 5);
        af.location = APPLY_SAVING_SPELL;
        af.modifier = -1;
        af.bitvector = 0;
        affect_to_char(victim, af);

        send_to_char("Your skin starts to wrinkle.\n", victim);
        act("$n skin starts to wrinkle.", victim, null, null, TO_ROOM);
    }


    static void spell_fumble(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        OBJ_DATA obj;

        if (is_affected(victim, sn)) {
            if (victim == ch) {
                send_to_char("You can't be more fumble!\n", ch);
            } else {
                act("$N can't get any more fumble than that.",
                        ch, null, victim, TO_CHAR);
            }
            return;
        }

        if (saves_spell(level, victim, DAM_OTHER)
                || IS_SET(victim.imm_flags, IMM_MAGIC)) {
            if (victim != ch) {
                send_to_char("Nothing seemed to happen.\n", ch);
            }
            send_to_char("You feel momentarily lethargic.\n", victim);
            return;
        }

        if (IS_AFFECTED(victim, AFF_HASTE)) {
            if (!check_dispel(level, victim, Skill.gsn_haste)) {
                if (victim != ch) {
                    send_to_char("Spell failed.\n", ch);
                }
                send_to_char("You feel momentarily slower.\n", victim);
                return;
            }

            act("$n is moving less quickly.", victim, null, null, TO_ROOM);
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = (4 + level / 12);
        af.location = APPLY_DEX;
        af.modifier = -UMAX(2, level / 6);
        af.bitvector = 0;
        affect_to_char(victim, af);

        if ((obj = get_weapon_char(victim, 1)) != null) {
            if (can_drop_obj(victim, obj)
                    && remove_obj(victim, obj, true)) {
                act("$n cannot carry $p anymore and drops it.", victim, obj, null, TO_ROOM);
                send_to_char("You cannot carry your dual weapon anymore and drop it!\n", victim);
                obj_from_char(obj);
                obj_to_room(obj, victim.in_room);
            }
        }
        if ((obj = get_weapon_char(victim, 0)) != null) {
            if (can_drop_obj(victim, obj)
                    && remove_obj(victim, obj, true)) {
                act("$n cannot carry $p anymore and drops it.", victim, obj, null, TO_ROOM);
                send_to_char("You cannot carry your weapon anymore and drop it!\n", victim);
                obj_from_char(obj);
                obj_to_room(obj, victim.in_room);
            }
        }

        WAIT_STATE(victim, PULSE_VIOLENCE);
        send_to_char("You feel yourself very  f u m b l e...\n", victim);
        act("$n starts to move in a fumble way.", victim, null, null, TO_ROOM);
    }

}
