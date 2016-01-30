package net.sf.nightworks;

import net.sf.nightworks.util.TextBuffer;

import static net.sf.nightworks.ActComm.do_say;
import static net.sf.nightworks.ActComm.do_yell;
import static net.sf.nightworks.ActComm.is_same_group;
import static net.sf.nightworks.ActHera.get_char_area;
import static net.sf.nightworks.ActHera.hunt_victim;
import static net.sf.nightworks.ActInfo.do_look;
import static net.sf.nightworks.ActMove.find_door;
import static net.sf.nightworks.ActMove.move_char;
import static net.sf.nightworks.Comm.act;
import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.Const.hometown_table;
import static net.sf.nightworks.Const.religion_table;
import static net.sf.nightworks.DB.bug;
import static net.sf.nightworks.DB.clone_mobile;
import static net.sf.nightworks.DB.create_mobile;
import static net.sf.nightworks.DB.create_object;
import static net.sf.nightworks.DB.dice;
import static net.sf.nightworks.DB.get_mob_index;
import static net.sf.nightworks.DB.get_obj_index;
import static net.sf.nightworks.DB.get_room_index;
import static net.sf.nightworks.DB.interpolate;
import static net.sf.nightworks.DB.kill_table;
import static net.sf.nightworks.DB.log_string;
import static net.sf.nightworks.DB.number_bits;
import static net.sf.nightworks.DB.number_fuzzy;
import static net.sf.nightworks.DB.number_percent;
import static net.sf.nightworks.DB.number_range;
import static net.sf.nightworks.DB.weather_info;
import static net.sf.nightworks.Effects.acid_effect;
import static net.sf.nightworks.Effects.cold_effect;
import static net.sf.nightworks.Effects.fire_effect;
import static net.sf.nightworks.Effects.poison_effect;
import static net.sf.nightworks.Effects.sand_effect;
import static net.sf.nightworks.Effects.scream_effect;
import static net.sf.nightworks.Effects.shock_effect;
import static net.sf.nightworks.Fight.damage;
import static net.sf.nightworks.Fight.do_murder;
import static net.sf.nightworks.Fight.is_safe;
import static net.sf.nightworks.Fight.is_safe_nomessage;
import static net.sf.nightworks.Fight.is_safe_spell;
import static net.sf.nightworks.Fight.multi_hit;
import static net.sf.nightworks.Fight.raw_kill;
import static net.sf.nightworks.Fight.stop_fighting;
import static net.sf.nightworks.Fight.update_pos;
import static net.sf.nightworks.Handler.affect_join;
import static net.sf.nightworks.Handler.affect_remove;
import static net.sf.nightworks.Handler.affect_strip_room;
import static net.sf.nightworks.Handler.affect_to_char;
import static net.sf.nightworks.Handler.affect_to_obj;
import static net.sf.nightworks.Handler.affect_to_room;
import static net.sf.nightworks.Handler.can_see;
import static net.sf.nightworks.Handler.can_see_room;
import static net.sf.nightworks.Handler.char_from_room;
import static net.sf.nightworks.Handler.char_to_room;
import static net.sf.nightworks.Handler.check_exit;
import static net.sf.nightworks.Handler.count_charmed;
import static net.sf.nightworks.Handler.count_worn;
import static net.sf.nightworks.Handler.equip_char;
import static net.sf.nightworks.Handler.extract_char;
import static net.sf.nightworks.Handler.extract_obj;
import static net.sf.nightworks.Handler.get_char_world;
import static net.sf.nightworks.Handler.get_curr_stat;
import static net.sf.nightworks.Handler.get_eq_char;
import static net.sf.nightworks.Handler.get_hold_char;
import static net.sf.nightworks.Handler.get_light_char;
import static net.sf.nightworks.Handler.get_skill;
import static net.sf.nightworks.Handler.get_trust;
import static net.sf.nightworks.Handler.is_affected;
import static net.sf.nightworks.Handler.is_affected_room;
import static net.sf.nightworks.Handler.max_can_wear;
import static net.sf.nightworks.Handler.obj_from_char;
import static net.sf.nightworks.Handler.obj_from_obj;
import static net.sf.nightworks.Handler.obj_to_char;
import static net.sf.nightworks.Handler.obj_to_room;
import static net.sf.nightworks.Handler.room_dark;
import static net.sf.nightworks.Handler.room_is_private;
import static net.sf.nightworks.Interp.interpret;
import static net.sf.nightworks.Interp.number_argument;
import static net.sf.nightworks.Magic.check_dispel;
import static net.sf.nightworks.Magic.saves_spell;
import static net.sf.nightworks.Magic.spell_bless;
import static net.sf.nightworks.Magic.spell_blindness;
import static net.sf.nightworks.Magic.spell_charm_person;
import static net.sf.nightworks.Magic.spell_cure_blindness;
import static net.sf.nightworks.Magic.spell_cure_disease;
import static net.sf.nightworks.Magic.spell_cure_poison;
import static net.sf.nightworks.Magic.spell_heal;
import static net.sf.nightworks.Magic.spell_poison;
import static net.sf.nightworks.Magic.spell_refresh;
import static net.sf.nightworks.Magic.spell_remove_curse;
import static net.sf.nightworks.Magic.spell_slow;
import static net.sf.nightworks.Magic.spell_weaken;
import static net.sf.nightworks.Magic.target_name;
import static net.sf.nightworks.Nightworks.A;
import static net.sf.nightworks.Nightworks.ACT_AGGRESSIVE;
import static net.sf.nightworks.Nightworks.ACT_HUNTER;
import static net.sf.nightworks.Nightworks.ACT_UNDEAD;
import static net.sf.nightworks.Nightworks.AFFECT_DATA;
import static net.sf.nightworks.Nightworks.AFF_ACUTE_VISION;
import static net.sf.nightworks.Nightworks.AFF_AURA_CHAOS;
import static net.sf.nightworks.Nightworks.AFF_BERSERK;
import static net.sf.nightworks.Nightworks.AFF_BLIND;
import static net.sf.nightworks.Nightworks.AFF_BLOODTHIRST;
import static net.sf.nightworks.Nightworks.AFF_CHARM;
import static net.sf.nightworks.Nightworks.AFF_CURSE;
import static net.sf.nightworks.Nightworks.AFF_DETECT_IMP_INVIS;
import static net.sf.nightworks.Nightworks.AFF_FEAR;
import static net.sf.nightworks.Nightworks.AFF_FLYING;
import static net.sf.nightworks.Nightworks.AFF_FORM_GRASS;
import static net.sf.nightworks.Nightworks.AFF_FORM_TREE;
import static net.sf.nightworks.Nightworks.AFF_HASTE;
import static net.sf.nightworks.Nightworks.AFF_IMP_INVIS;
import static net.sf.nightworks.Nightworks.AFF_INFRARED;
import static net.sf.nightworks.Nightworks.AFF_LION;
import static net.sf.nightworks.Nightworks.AFF_PLAGUE;
import static net.sf.nightworks.Nightworks.AFF_POISON;
import static net.sf.nightworks.Nightworks.AFF_PROTECTOR;
import static net.sf.nightworks.Nightworks.AFF_PROTECT_EVIL;
import static net.sf.nightworks.Nightworks.AFF_ROOM_CURSE;
import static net.sf.nightworks.Nightworks.AFF_ROOM_ESPIRIT;
import static net.sf.nightworks.Nightworks.AFF_ROOM_PLAGUE;
import static net.sf.nightworks.Nightworks.AFF_ROOM_POISON;
import static net.sf.nightworks.Nightworks.AFF_ROOM_PREVENT;
import static net.sf.nightworks.Nightworks.AFF_ROOM_RANDOMIZER;
import static net.sf.nightworks.Nightworks.AFF_ROOM_SLEEP;
import static net.sf.nightworks.Nightworks.AFF_ROOM_SLOW;
import static net.sf.nightworks.Nightworks.AFF_SANCTUARY;
import static net.sf.nightworks.Nightworks.AFF_SLEEP;
import static net.sf.nightworks.Nightworks.AFF_SLOW;
import static net.sf.nightworks.Nightworks.AFF_SNEAK;
import static net.sf.nightworks.Nightworks.AFF_STUN;
import static net.sf.nightworks.Nightworks.AFF_SUFFOCATE;
import static net.sf.nightworks.Nightworks.AFF_WEB;
import static net.sf.nightworks.Nightworks.APPLY_AC;
import static net.sf.nightworks.Nightworks.APPLY_CHA;
import static net.sf.nightworks.Nightworks.APPLY_DAMROLL;
import static net.sf.nightworks.Nightworks.APPLY_DEX;
import static net.sf.nightworks.Nightworks.APPLY_HIT;
import static net.sf.nightworks.Nightworks.APPLY_HITROLL;
import static net.sf.nightworks.Nightworks.APPLY_INT;
import static net.sf.nightworks.Nightworks.APPLY_MANA;
import static net.sf.nightworks.Nightworks.APPLY_NONE;
import static net.sf.nightworks.Nightworks.APPLY_ROOM_MANA;
import static net.sf.nightworks.Nightworks.APPLY_SAVING_SPELL;
import static net.sf.nightworks.Nightworks.APPLY_SIZE;
import static net.sf.nightworks.Nightworks.APPLY_STR;
import static net.sf.nightworks.Nightworks.APPLY_WIS;
import static net.sf.nightworks.Nightworks.AREA_DATA;
import static net.sf.nightworks.Nightworks.AREA_HOMETOWN;
import static net.sf.nightworks.Nightworks.B;
import static net.sf.nightworks.Nightworks.BIT_26;
import static net.sf.nightworks.Nightworks.BIT_28;
import static net.sf.nightworks.Nightworks.BIT_30;
import static net.sf.nightworks.Nightworks.C;
import static net.sf.nightworks.Nightworks.CABAL_HUNTER;
import static net.sf.nightworks.Nightworks.CABAL_NONE;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.COND_BLOODLUST;
import static net.sf.nightworks.Nightworks.COND_DESIRE;
import static net.sf.nightworks.Nightworks.COND_FULL;
import static net.sf.nightworks.Nightworks.COND_HUNGER;
import static net.sf.nightworks.Nightworks.COND_THIRST;
import static net.sf.nightworks.Nightworks.D;
import static net.sf.nightworks.Nightworks.DAM_ACID;
import static net.sf.nightworks.Nightworks.DAM_BASH;
import static net.sf.nightworks.Nightworks.DAM_CHARM;
import static net.sf.nightworks.Nightworks.DAM_COLD;
import static net.sf.nightworks.Nightworks.DAM_ENERGY;
import static net.sf.nightworks.Nightworks.DAM_FIRE;
import static net.sf.nightworks.Nightworks.DAM_HOLY;
import static net.sf.nightworks.Nightworks.DAM_LIGHT;
import static net.sf.nightworks.Nightworks.DAM_LIGHTNING;
import static net.sf.nightworks.Nightworks.DAM_MENTAL;
import static net.sf.nightworks.Nightworks.DAM_NEGATIVE;
import static net.sf.nightworks.Nightworks.DAM_NONE;
import static net.sf.nightworks.Nightworks.DAM_OTHER;
import static net.sf.nightworks.Nightworks.DAM_PIERCE;
import static net.sf.nightworks.Nightworks.DAM_POISON;
import static net.sf.nightworks.Nightworks.DICE_BONUS;
import static net.sf.nightworks.Nightworks.DICE_NUMBER;
import static net.sf.nightworks.Nightworks.DICE_TYPE;
import static net.sf.nightworks.Nightworks.E;
import static net.sf.nightworks.Nightworks.EXIT_DATA;
import static net.sf.nightworks.Nightworks.EXTRA_DESCR_DATA;
import static net.sf.nightworks.Nightworks.EX_CLOSED;
import static net.sf.nightworks.Nightworks.EX_LOCKED;
import static net.sf.nightworks.Nightworks.EX_NOPASS;
import static net.sf.nightworks.Nightworks.F;
import static net.sf.nightworks.Nightworks.G;
import static net.sf.nightworks.Nightworks.H;
import static net.sf.nightworks.Nightworks.IMM_CHARM;
import static net.sf.nightworks.Nightworks.IMM_NEGATIVE;
import static net.sf.nightworks.Nightworks.IMM_SUMMON;
import static net.sf.nightworks.Nightworks.IS_AFFECTED;
import static net.sf.nightworks.Nightworks.IS_AWAKE;
import static net.sf.nightworks.Nightworks.IS_EVIL;
import static net.sf.nightworks.Nightworks.IS_GOOD;
import static net.sf.nightworks.Nightworks.IS_IMMORTAL;
import static net.sf.nightworks.Nightworks.IS_NEUTRAL;
import static net.sf.nightworks.Nightworks.IS_NPC;
import static net.sf.nightworks.Nightworks.IS_OBJ_STAT;
import static net.sf.nightworks.Nightworks.IS_OUTSIDE;
import static net.sf.nightworks.Nightworks.IS_RAFFECTED;
import static net.sf.nightworks.Nightworks.IS_SET;
import static net.sf.nightworks.Nightworks.IS_WEAPON_STAT;
import static net.sf.nightworks.Nightworks.ITEM_ANTI_EVIL;
import static net.sf.nightworks.Nightworks.ITEM_ANTI_GOOD;
import static net.sf.nightworks.Nightworks.ITEM_ANTI_NEUTRAL;
import static net.sf.nightworks.Nightworks.ITEM_BLESS;
import static net.sf.nightworks.Nightworks.ITEM_BURN_PROOF;
import static net.sf.nightworks.Nightworks.ITEM_CORPSE_NPC;
import static net.sf.nightworks.Nightworks.ITEM_CORPSE_PC;
import static net.sf.nightworks.Nightworks.ITEM_GLOW;
import static net.sf.nightworks.Nightworks.ITEM_KEY;
import static net.sf.nightworks.Nightworks.ITEM_LIGHT;
import static net.sf.nightworks.Nightworks.ITEM_MAGIC;
import static net.sf.nightworks.Nightworks.ITEM_TRASH;
import static net.sf.nightworks.Nightworks.ITEM_TREASURE;
import static net.sf.nightworks.Nightworks.ITEM_WARP_STONE;
import static net.sf.nightworks.Nightworks.ITEM_WEAPON;
import static net.sf.nightworks.Nightworks.J;
import static net.sf.nightworks.Nightworks.LEVEL_HERO;
import static net.sf.nightworks.Nightworks.LEVEL_IMMORTAL;
import static net.sf.nightworks.Nightworks.MAX_LEVEL;
import static net.sf.nightworks.Nightworks.MAX_RELIGION;
import static net.sf.nightworks.Nightworks.MAX_SKILL;
import static net.sf.nightworks.Nightworks.MAX_STATS;
import static net.sf.nightworks.Nightworks.MOB_VNUM_ADAMANTITE_GOLEM;
import static net.sf.nightworks.Nightworks.MOB_VNUM_BEAR;
import static net.sf.nightworks.Nightworks.MOB_VNUM_DEMON;
import static net.sf.nightworks.Nightworks.MOB_VNUM_DOG;
import static net.sf.nightworks.Nightworks.MOB_VNUM_HUNTER;
import static net.sf.nightworks.Nightworks.MOB_VNUM_IRON_GOLEM;
import static net.sf.nightworks.Nightworks.MOB_VNUM_LESSER_GOLEM;
import static net.sf.nightworks.Nightworks.MOB_VNUM_MIRROR_IMAGE;
import static net.sf.nightworks.Nightworks.MOB_VNUM_NIGHTWALKER;
import static net.sf.nightworks.Nightworks.MOB_VNUM_SHADOW;
import static net.sf.nightworks.Nightworks.MOB_VNUM_SPECIAL_GUARD;
import static net.sf.nightworks.Nightworks.MOB_VNUM_SQUIRE;
import static net.sf.nightworks.Nightworks.MOB_VNUM_STALKER;
import static net.sf.nightworks.Nightworks.MOB_VNUM_STONE_GOLEM;
import static net.sf.nightworks.Nightworks.MOB_VNUM_SUM_SHADOW;
import static net.sf.nightworks.Nightworks.MOB_VNUM_UNDEAD;
import static net.sf.nightworks.Nightworks.MOB_VNUM_WOLF;
import static net.sf.nightworks.Nightworks.MOUNTED;
import static net.sf.nightworks.Nightworks.N;
import static net.sf.nightworks.Nightworks.O;
import static net.sf.nightworks.Nightworks.OBJ_DATA;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_CHAOS_BLADE;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_DEPUTY_BADGE;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_DRAGONDAGGER;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_DRAGONLANCE;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_DRAGONMACE;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_DRAGONSWORD;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_EYED_SWORD;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_FIRE_SHIELD;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_LION_SHIELD;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_MAGIC_JAR;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_PLATE;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_PORTAL;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_POTION_GOLDEN;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_POTION_SILVER;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_POTION_SWIRLING;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_POTION_VIAL;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_RANGER_STAFF;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_RULER_BADGE;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_RULER_SHIELD1;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_RULER_SHIELD2;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_RULER_SHIELD3;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_RULER_SHIELD4;
import static net.sf.nightworks.Nightworks.PK_MIN_LEVEL;
import static net.sf.nightworks.Nightworks.PLR_BOUGHT_PET;
import static net.sf.nightworks.Nightworks.PLR_CANINDUCT;
import static net.sf.nightworks.Nightworks.PLR_NOSUMMON;
import static net.sf.nightworks.Nightworks.PLR_NO_EXP;
import static net.sf.nightworks.Nightworks.PLR_VAMPIRE;
import static net.sf.nightworks.Nightworks.PLR_WANTED;
import static net.sf.nightworks.Nightworks.POS_DEAD;
import static net.sf.nightworks.Nightworks.POS_RESTING;
import static net.sf.nightworks.Nightworks.POS_SLEEPING;
import static net.sf.nightworks.Nightworks.PULSE_VIOLENCE;
import static net.sf.nightworks.Nightworks.REMOVE_BIT;
import static net.sf.nightworks.Nightworks.RES_ENERGY;
import static net.sf.nightworks.Nightworks.RES_MAGIC;
import static net.sf.nightworks.Nightworks.RES_NEGATIVE;
import static net.sf.nightworks.Nightworks.ROOM_INDEX_DATA;
import static net.sf.nightworks.Nightworks.ROOM_LAW;
import static net.sf.nightworks.Nightworks.ROOM_NOSUMMON;
import static net.sf.nightworks.Nightworks.ROOM_NO_MOB;
import static net.sf.nightworks.Nightworks.ROOM_NO_RECALL;
import static net.sf.nightworks.Nightworks.ROOM_PRIVATE;
import static net.sf.nightworks.Nightworks.ROOM_SAFE;
import static net.sf.nightworks.Nightworks.ROOM_SOLITARY;
import static net.sf.nightworks.Nightworks.SECT_AIR;
import static net.sf.nightworks.Nightworks.SECT_CITY;
import static net.sf.nightworks.Nightworks.SECT_DESERT;
import static net.sf.nightworks.Nightworks.SECT_FIELD;
import static net.sf.nightworks.Nightworks.SECT_FOREST;
import static net.sf.nightworks.Nightworks.SECT_HILLS;
import static net.sf.nightworks.Nightworks.SECT_INSIDE;
import static net.sf.nightworks.Nightworks.SECT_MOUNTAIN;
import static net.sf.nightworks.Nightworks.SECT_WATER_NOSWIM;
import static net.sf.nightworks.Nightworks.SECT_WATER_SWIM;
import static net.sf.nightworks.Nightworks.SET_BIT;
import static net.sf.nightworks.Nightworks.SIZE_GARGANTUAN;
import static net.sf.nightworks.Nightworks.SKY_RAINING;
import static net.sf.nightworks.Nightworks.STAT_CON;
import static net.sf.nightworks.Nightworks.STAT_DEX;
import static net.sf.nightworks.Nightworks.STAT_INT;
import static net.sf.nightworks.Nightworks.STAT_STR;
import static net.sf.nightworks.Nightworks.TARGET_CHAR;
import static net.sf.nightworks.Nightworks.TARGET_OBJ;
import static net.sf.nightworks.Nightworks.TARGET_ROOM;
import static net.sf.nightworks.Nightworks.TO_ACT_FLAG;
import static net.sf.nightworks.Nightworks.TO_AFFECTS;
import static net.sf.nightworks.Nightworks.TO_ALL;
import static net.sf.nightworks.Nightworks.TO_CHAR;
import static net.sf.nightworks.Nightworks.TO_IMMUNE;
import static net.sf.nightworks.Nightworks.TO_NOTVICT;
import static net.sf.nightworks.Nightworks.TO_OBJECT;
import static net.sf.nightworks.Nightworks.TO_RACE;
import static net.sf.nightworks.Nightworks.TO_RESIST;
import static net.sf.nightworks.Nightworks.TO_ROOM;
import static net.sf.nightworks.Nightworks.TO_ROOM_AFFECTS;
import static net.sf.nightworks.Nightworks.TO_ROOM_CONST;
import static net.sf.nightworks.Nightworks.TO_VICT;
import static net.sf.nightworks.Nightworks.TO_WEAPON;
import static net.sf.nightworks.Nightworks.U;
import static net.sf.nightworks.Nightworks.UMAX;
import static net.sf.nightworks.Nightworks.UMIN;
import static net.sf.nightworks.Nightworks.URANGE;
import static net.sf.nightworks.Nightworks.V;
import static net.sf.nightworks.Nightworks.WAIT_STATE;
import static net.sf.nightworks.Nightworks.WEAPON_FLAMING;
import static net.sf.nightworks.Nightworks.WEAPON_FROST;
import static net.sf.nightworks.Nightworks.WEAPON_HOLY;
import static net.sf.nightworks.Nightworks.WEAPON_SHARP;
import static net.sf.nightworks.Nightworks.WEAPON_SHOCKING;
import static net.sf.nightworks.Nightworks.WEAPON_VAMPIRIC;
import static net.sf.nightworks.Nightworks.WEAPON_VORPAL;
import static net.sf.nightworks.Nightworks.WEAR_NECK;
import static net.sf.nightworks.Nightworks.WEAR_TATTOO;
import static net.sf.nightworks.Nightworks.char_list;
import static net.sf.nightworks.Nightworks.current_time;
import static net.sf.nightworks.Nightworks.sprintf;
import static net.sf.nightworks.Skill.gsn_black_death;
import static net.sf.nightworks.Skill.gsn_bless;
import static net.sf.nightworks.Skill.gsn_blindness;
import static net.sf.nightworks.Skill.gsn_charm_person;
import static net.sf.nightworks.Skill.gsn_confuse;
import static net.sf.nightworks.Skill.gsn_cursed_lands;
import static net.sf.nightworks.Skill.gsn_deadly_venom;
import static net.sf.nightworks.Skill.gsn_doppelganger;
import static net.sf.nightworks.Skill.gsn_entangle;
import static net.sf.nightworks.Skill.gsn_fear;
import static net.sf.nightworks.Skill.gsn_fire_shield;
import static net.sf.nightworks.Skill.gsn_lethargic_mist;
import static net.sf.nightworks.Skill.gsn_mend;
import static net.sf.nightworks.Skill.gsn_mirror;
import static net.sf.nightworks.Skill.gsn_mysterious_dream;
import static net.sf.nightworks.Skill.gsn_poison;
import static net.sf.nightworks.Skill.gsn_protection_cold;
import static net.sf.nightworks.Skill.gsn_protection_heat;
import static net.sf.nightworks.Skill.gsn_weaken;
import static net.sf.nightworks.Skill.gsn_witch_curse;
import static net.sf.nightworks.Skill.lookupSkill;
import static net.sf.nightworks.util.TextUtils.one_argument;
import static net.sf.nightworks.util.TextUtils.str_cmp;

class Magic2 {

    static ROOM_INDEX_DATA check_place(CHAR_DATA ch, String argument) {
        EXIT_DATA pExit;
        ROOM_INDEX_DATA dest_room;
        int number, door;
        int range = (ch.level / 10) + 1;

        StringBuilder arg = new StringBuilder();

        number = number_argument(argument, arg);
        if ((door = check_exit(arg.toString())) == -1) {
            return null;
        }

        dest_room = ch.in_room;
        while (number > 0) {
            number--;
            if (--range < 1) {
                return null;
            }
            if ((pExit = dest_room.exit[door]) == null || (dest_room = pExit.to_room) == null || IS_SET(pExit.exit_info, EX_CLOSED)) {
                break;
            }
            if (number < 1) {
                return dest_room;
            }
        }
        return null;
    }

    static void spell_portal(int level, CHAR_DATA ch) {
        CHAR_DATA victim;
        OBJ_DATA portal, stone;

        if ((victim = get_char_world(ch, target_name)) == null
                || victim == ch
                || victim.in_room == null
                || !can_see_room(ch, victim.in_room)
                || IS_SET(victim.in_room.room_flags, ROOM_SAFE)
                || IS_SET(victim.in_room.room_flags, ROOM_PRIVATE)
                || IS_SET(victim.in_room.room_flags, ROOM_SOLITARY)
                || IS_SET(victim.in_room.room_flags, ROOM_NOSUMMON)
                || IS_SET(ch.in_room.room_flags, ROOM_NOSUMMON)
                || IS_SET(ch.in_room.room_flags, ROOM_NO_RECALL)
                || victim.level >= level + 3
                || (!IS_NPC(victim) && victim.level >= LEVEL_HERO)  /* NOT trust */
                || (IS_NPC(victim) && is_safe_nomessage(ch, victim) && IS_SET(victim.imm_flags, IMM_SUMMON))
                || (IS_NPC(victim) && saves_spell(level, victim, DAM_NONE))) {
            send_to_char("You failed.\n", ch);
            return;
        }

        stone = get_hold_char(ch);
        if (!IS_IMMORTAL(ch)
                && (stone == null || stone.item_type != ITEM_WARP_STONE)) {
            send_to_char("You lack the proper component for this spell.\n", ch);
            return;
        }

        if (stone != null && stone.item_type == ITEM_WARP_STONE) {
            act("You draw upon the power of $p.", ch, stone, null, TO_CHAR);
            act("It flares brightly and vanishes!", ch, stone, null, TO_CHAR);
            extract_obj(stone);
        }

        portal = create_object(get_obj_index(OBJ_VNUM_PORTAL), 0);
        portal.timer = 2 + level / 25;
        portal.value[3] = victim.in_room.vnum;

        obj_to_room(portal, ch.in_room);

        act("$p rises up from the ground.", ch, portal, null, TO_ROOM);
        act("$p rises up before you.", ch, portal, null, TO_CHAR);
    }

    static void spell_nexus(int level, CHAR_DATA ch) {
        CHAR_DATA victim;
        OBJ_DATA portal, stone;
        ROOM_INDEX_DATA to_room, from_room;

        from_room = ch.in_room;

        if ((victim = get_char_world(ch, target_name)) == null
                || victim == ch
                || (to_room = victim.in_room) == null
                || !can_see_room(ch, to_room) || !can_see_room(ch, from_room)
                || IS_SET(to_room.room_flags, ROOM_SAFE)
                || IS_SET(from_room.room_flags, ROOM_SAFE)
                || IS_SET(to_room.room_flags, ROOM_PRIVATE)
                || IS_SET(to_room.room_flags, ROOM_SOLITARY)
                || IS_SET(to_room.room_flags, ROOM_NOSUMMON)
                || victim.level >= level + 3
                || (!IS_NPC(victim) && victim.level >= LEVEL_HERO)  /* NOT trust */
                || (IS_NPC(victim) && is_safe_nomessage(ch, victim) && IS_SET(victim.imm_flags, IMM_SUMMON))
                || (IS_NPC(victim) && saves_spell(level, victim, DAM_NONE))) {
            send_to_char("You failed.\n", ch);
            return;
        }

        stone = get_hold_char(ch);
        if (!IS_IMMORTAL(ch)
                && (stone == null || stone.item_type != ITEM_WARP_STONE)) {
            send_to_char("You lack the proper component for this spell.\n", ch);
            return;
        }

        if (stone != null && stone.item_type == ITEM_WARP_STONE) {
            act("You draw upon the power of $p.", ch, stone, null, TO_CHAR);
            act("It flares brightly and vanishes!", ch, stone, null, TO_CHAR);
            extract_obj(stone);
        }

        /* portal one */
        portal = create_object(get_obj_index(OBJ_VNUM_PORTAL), 0);
        portal.timer = 1 + level / 10;
        portal.value[3] = to_room.vnum;

        obj_to_room(portal, from_room);

        act("$p rises up from the ground.", ch, portal, null, TO_ROOM);
        act("$p rises up before you.", ch, portal, null, TO_CHAR);

        /* no second portal if rooms are the same */
        if (to_room == from_room) {
            return;
        }

        /* portal two */
        portal = create_object(get_obj_index(OBJ_VNUM_PORTAL), 0);
        portal.timer = 1 + level / 10;
        portal.value[3] = from_room.vnum;

        obj_to_room(portal, to_room);

        if (to_room.people != null) {
            act("$p rises up from the ground.", to_room.people, portal, null, TO_ROOM);
            act("$p rises up from the ground.", to_room.people, portal, null, TO_CHAR);
        }
    }


    static void spell_disintegrate(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        CHAR_DATA tmp_ch;
        OBJ_DATA obj;
        OBJ_DATA obj_next;
        int i, dam;
        OBJ_DATA tattoo;


        if (saves_spell(level, victim, DAM_MENTAL) || number_bits(1) == 0) {
            dam = dice(level, 24);
            damage(ch, victim, dam, sn, DAM_MENTAL, true);
            return;
        }

        act("$N's thin light ray {r###DISINTEGRATES###{x you!", victim, null, ch, TO_CHAR, POS_RESTING);
        act("$n's thin light ray {r###DISINTEGRATES###{x $N!", ch, null, victim, TO_NOTVICT, POS_RESTING);
        act("Your thin light ray {r###DISINTEGRATES###{x $N!", ch, null, victim, TO_CHAR, POS_RESTING);
        send_to_char("You have been KILLED!\n", victim);

        act("$N does not exist anymore!\n", ch, null, victim, TO_CHAR);
        act("$N does not exist anymore!\n", ch, null, victim, TO_ROOM);

        send_to_char("You turn into an invincible ghost for a few minutes.\n", victim);
        send_to_char("As long as you don't attack anything.\n", victim);

        /*  disintegrate the objects... */
        tattoo = get_eq_char(victim, WEAR_TATTOO); /* keep tattoos for later */
        if (tattoo != null) {
            obj_from_char(tattoo);
        }

        victim.gold = 0;
        victim.silver = 0;

        for (obj = victim.carrying; obj != null; obj = obj_next) {
            obj_next = obj.next_content;
            extract_obj(obj);
        }

        if (IS_NPC(victim)) {
            victim.pIndexData.killed++;
            kill_table[URANGE(0, victim.level, MAX_LEVEL - 1)].killed++;
            extract_char(victim, true);
            return;
        }

        extract_char(victim, false);

        while (victim.affected != null) {
            affect_remove(victim, victim.affected);
        }
        victim.affected_by = 0;
        for (i = 0; i < 4; i++) {
            victim.armor[i] = 100;
        }
        victim.position = POS_RESTING;
        victim.hit = 1;
        victim.mana = 1;

        victim.act = REMOVE_BIT(victim.act, PLR_WANTED);
        victim.act = REMOVE_BIT(victim.act, PLR_BOUGHT_PET);

        victim.pcdata.condition[COND_THIRST] = 40;
        victim.pcdata.condition[COND_HUNGER] = 40;
        victim.pcdata.condition[COND_FULL] = 40;
        victim.pcdata.condition[COND_BLOODLUST] = 40;
        victim.pcdata.condition[COND_DESIRE] = 40;

        victim.last_death_time = current_time;

        if (tattoo != null) {
            obj_to_char(tattoo, victim);
            equip_char(victim, tattoo, WEAR_TATTOO);
        }

        for (tmp_ch = char_list; tmp_ch != null; tmp_ch = tmp_ch.next) {
            if (tmp_ch.last_fought == victim) {
                tmp_ch.last_fought = null;
            }
        }

    }

    static void spell_poison_smoke(CHAR_DATA ch) {

        CHAR_DATA tmp_vict;

        send_to_char("A cloud of poison smoke fills the room.\n", ch);
        act("A cloud of poison smoke fills the room.", ch, null, null, TO_ROOM);

        for (tmp_vict = ch.in_room.people; tmp_vict != null;
             tmp_vict = tmp_vict.next_in_room) {
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

                spell_poison(gsn_poison, ch.level, ch, tmp_vict, TARGET_CHAR);
                /*  poison_effect(ch.in_room,level,level,TARGET_CHAR);  */
                if (tmp_vict != ch) {
                    multi_hit(tmp_vict, ch, null);
                }
            }
        }
    }

    static void spell_blindness_dust(CHAR_DATA ch) {
        CHAR_DATA tmp_vict;

        send_to_char("A cloud of dust fills in the room.\n", ch);
        act("A cloud of dust fills the room.", ch, null, null, TO_ROOM);


        for (tmp_vict = ch.in_room.people; tmp_vict != null;
             tmp_vict = tmp_vict.next_in_room) {
            if (!is_safe_spell(ch, tmp_vict, true)) {
                if (!IS_NPC(ch) && tmp_vict != ch &&
                        ch.fighting != tmp_vict && tmp_vict.fighting != ch &&
                        (IS_SET(tmp_vict.affected_by, AFF_CHARM) || !IS_NPC(tmp_vict))) {
                    if (!can_see(tmp_vict, ch)) {
                        do_yell(tmp_vict, "Help someone is attacking me!");
                    } else {
                        TextBuffer buf = new TextBuffer();
                        buf.sprintf("Die, %s, you sorcerous dog!",
                                (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(tmp_vict)) ?
                                        ch.doppel.name : ch.name);
                        do_yell(tmp_vict, buf);
                    }
                }

                spell_blindness(gsn_blindness, ch.level, ch, tmp_vict);
                if (tmp_vict != ch) {
                    multi_hit(tmp_vict, ch, null);
                }


            }
        }

    }

    static void spell_bark_skin(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(ch, sn)) {
            if (victim == ch) {
                send_to_char("Your skin is already covered in bark.\n", ch);
            } else {
                act("$N is already as hard as can be.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = level;
        af.location = APPLY_AC;
        af.modifier = (int) -(level * 1.5);
        af.bitvector = 0;
        affect_to_char(victim, af);
        act("$n's skin becomes covered in bark.", victim, null, null, TO_ROOM);
        send_to_char("Your skin becomes covered in bark.\n", victim);
    }

    static void spell_bear_call(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA gch;
        CHAR_DATA bear;
        CHAR_DATA bear2;
        int i;

        send_to_char("You call for bears help you.\n", ch);
        act("$n shouts a bear call.", ch, null, null, TO_ROOM);

        if (is_affected(ch, sn)) {
            send_to_char("You cannot summon the strength to handle more bears right now.\n", ch);
            return;
        }
        for (gch = char_list; gch != null; gch = gch.next) {
            if (IS_NPC(gch) && IS_AFFECTED(gch, AFF_CHARM) && gch.master == ch &&
                    gch.pIndexData.vnum == MOB_VNUM_BEAR) {
                send_to_char("What's wrong with the bear you've got?", ch);
                return;
            }
        }

        if (count_charmed(ch) != 0) {
            return;
        }

        bear = create_mobile(get_mob_index(MOB_VNUM_BEAR));

        for (i = 0; i < MAX_STATS; i++) {
            bear.perm_stat[i] = UMIN(25, 2 * ch.perm_stat[i]);
        }

        bear.max_hit = IS_NPC(ch) ? ch.max_hit : ch.pcdata.perm_hit;
        bear.hit = bear.max_hit;
        bear.max_mana = IS_NPC(ch) ? ch.max_mana : ch.pcdata.perm_mana;
        bear.mana = bear.max_mana;
        bear.alignment = ch.alignment;
        bear.level = UMIN(70, ch.level);
        for (i = 0; i < 3; i++) {
            bear.armor[i] = interpolate(bear.level, 100, -100);
        }
        bear.armor[3] = interpolate(bear.level, 100, 0);
        bear.sex = ch.sex;
        bear.gold = 0;

        bear2 = create_mobile(bear.pIndexData);
        clone_mobile(bear, bear2);

        bear.affected_by = SET_BIT(bear.affected_by, AFF_CHARM);
        bear2.affected_by = SET_BIT(bear2.affected_by, AFF_CHARM);
        bear.master = bear2.master = ch;
        bear.leader = bear2.leader = ch;

        char_to_room(bear, ch.in_room);
        char_to_room(bear2, ch.in_room);
        send_to_char("Two bears come to your rescue!\n", ch);
        act("Two bears come to $n's rescue!", ch, null, null, TO_ROOM);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

    }

    static void spell_ranger_staff(Skill sn, int level, CHAR_DATA ch) {
        OBJ_DATA staff;

        staff = create_object(get_obj_index(OBJ_VNUM_RANGER_STAFF), level);
        send_to_char("You create a ranger's staff!\n", ch);
        act("$n creates a ranger's staff!", ch, null, null, TO_ROOM);

        if (ch.level < 50) {
            staff.value[2] = (ch.level / 10);
        } else {
            staff.value[2] = (ch.level / 6) - 3;
        }
        staff.level = ch.level;

        AFFECT_DATA tohit = new AFFECT_DATA();
        tohit.where = TO_OBJECT;
        tohit.type = sn;
        tohit.level = ch.level;
        tohit.duration = -1;
        tohit.location = APPLY_HITROLL;
        tohit.modifier = 2 + level / 5;
        tohit.bitvector = 0;
        affect_to_obj(staff, tohit);

        AFFECT_DATA todam = new AFFECT_DATA();
        todam.where = TO_OBJECT;
        todam.type = sn;
        todam.level = ch.level;
        todam.duration = -1;
        todam.location = APPLY_DAMROLL;
        todam.modifier = 2 + level / 5;
        todam.bitvector = 0;
        affect_to_obj(staff, todam);


        staff.timer = level;

        obj_to_char(staff, ch);
    }

    static void spell_vanish(CHAR_DATA ch, Object vo) {
        ROOM_INDEX_DATA pRoomIndex = null;
        CHAR_DATA victim = (CHAR_DATA) vo;
        int i;

        if (victim.in_room == null || IS_SET(victim.in_room.room_flags, ROOM_NO_RECALL)) {
            send_to_char("You failed.\n", ch);
            return;
        }

        for (i = 0; i < 65535; i++) {
            pRoomIndex = get_room_index(number_range(0, 65535));
            if (pRoomIndex != null) {
                if (can_see_room(victim, pRoomIndex) && !room_is_private(pRoomIndex) && victim.in_room.area == pRoomIndex.area) {
                    break;
                }
            }
        }

        if (pRoomIndex == null) {
            send_to_char("You failed.\n", ch);
            return;
        }


        act("$n throws down a small globe.", ch, null, null, TO_ROOM);

        if (!IS_NPC(ch) && ch.fighting != null && number_bits(1) == 1) {
            send_to_char("You failed.\n", ch);
            return;
        }

        act("$n is gone!", victim, null, null, TO_ROOM);

        char_from_room(victim);
        char_to_room(victim, pRoomIndex);
        act("$n appears from nowhere.", victim, null, null, TO_ROOM);
        do_look(victim, "auto");
        stop_fighting(victim, true);
    }

    static void spell_transform(Skill sn, int level, CHAR_DATA ch) {

        if (is_affected(ch, sn) || ch.hit > ch.max_hit) {
            send_to_char("You are already overflowing with health.\n", ch);
            return;
        }

        ch.hit += UMIN(30000 - ch.max_hit, ch.max_hit);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.location = APPLY_HIT;
        af.modifier = UMIN(30000 - ch.max_hit, ch.max_hit);
        af.bitvector = 0;
        affect_to_char(ch, af);


        send_to_char("Your mind clouds as your health increases.\n", ch);
    }

    static void spell_mana_transfer(Skill sn, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (victim == ch) {
            send_to_char("You would implode if you tried to transfer mana to yourself.\n", ch);
            return;
        }

        if (ch.cabal != victim.cabal) {
            send_to_char("You may only cast this spell on fellow cabal members.\n", ch);
            return;
        }

        if (ch.hit < 50) {
            damage(ch, ch, 50, sn, DAM_NONE, true);
        } else {
            damage(ch, ch, 50, sn, DAM_NONE, true);
            victim.mana = UMIN(victim.max_mana, victim.mana + number_range(20, 120));
        }
    }

    static void spell_mental_knife(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        if (ch.level < 40) {
            dam = dice(level, 8);
        } else if (ch.level < 65) {
            dam = dice(level, 11);
        } else {
            dam = dice(level, 14);
        }

        if (saves_spell(level, victim, DAM_MENTAL)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_MENTAL, true);

        if (!is_affected(victim, sn) && !saves_spell(level, victim, DAM_MENTAL)) {
            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = sn;
            af.level = level;
            af.duration = level;
            af.location = APPLY_INT;
            af.modifier = -7;
            af.bitvector = 0;
            affect_to_char(victim, af);

            af.location = APPLY_WIS;
            affect_to_char(victim, af);
            act("Your mental knife sears $N's mind!", ch, null, victim, TO_CHAR);
            act("$n's mental knife sears your mind!", ch, null, victim, TO_VICT);
            act("$n's mental knife sears $N's mind!", ch, null, victim, TO_NOTVICT);
        }
    }

    static void spell_demon_summon(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA gch;
        CHAR_DATA demon;
        int i;

        if (is_affected(ch, sn)) {
            send_to_char("You lack the power to summon another demon right now.\n",
                    ch);
            return;
        }

        send_to_char("You attempt to summon a demon.\n", ch);
        act("$n attempts to summon a demon.", ch, null, null, TO_ROOM);

        for (gch = char_list; gch != null; gch = gch.next) {
            if (IS_NPC(gch) && IS_AFFECTED(gch, AFF_CHARM) && gch.master == ch &&
                    gch.pIndexData.vnum == MOB_VNUM_DEMON) {
                send_to_char("Two demons are more than you can control!\n", ch);
                return;
            }
        }

        if (count_charmed(ch) != 0) {
            return;
        }

        demon = create_mobile(get_mob_index(MOB_VNUM_DEMON));

        for (i = 0; i < MAX_STATS; i++) {
            demon.perm_stat[i] = ch.perm_stat[i];
        }

        demon.max_hit = IS_NPC(ch) ? URANGE(ch.max_hit, ch.max_hit, 30000) : URANGE(ch.pcdata.perm_hit, ch.hit, 30000);
        demon.hit = demon.max_hit;
        demon.max_mana = IS_NPC(ch) ? ch.max_mana : ch.pcdata.perm_mana;
        demon.mana = demon.max_mana;
        demon.level = ch.level;
        for (i = 0; i < 3; i++) {
            demon.armor[i] = interpolate(demon.level, 100, -100);
        }
        demon.armor[3] = interpolate(demon.level, 100, 0);
        demon.gold = 0;
        demon.timer = 0;
        demon.damage[DICE_NUMBER] = number_range(level / 15, level / 12);
        demon.damage[DICE_TYPE] = number_range(level / 3, level / 2);
        demon.damage[DICE_BONUS] = number_range(level / 10, level / 8);

        char_to_room(demon, ch.in_room);
        send_to_char("A demon arrives from the underworld!\n", ch);
        act("A demon arrives from the underworld!", ch, null, null, TO_ROOM);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

        if (number_percent() < 40) {
            if (can_see(demon, ch)) {
                do_say(demon, "You dare disturb me?!?!!!");
            } else {
                do_say(demon, "Who dares disturb me?!?!!!");
            }
            do_murder(demon, ch.name);
        } else {
            demon.affected_by = SET_BIT(demon.affected_by, AFF_CHARM);
            demon.master = demon.leader = ch;
        }

    }

    static void spell_scourge(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA tmp_vict;
        CHAR_DATA tmp_next;
        int dam;

        if (ch.level < 40) {
            dam = dice(level, 6);
        } else if (ch.level < 65) {
            dam = dice(level, 9);
        } else {
            dam = dice(level, 12);
        }

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
                        do_yell(tmp_vict, buf);
                    }
                }

                if (!is_affected(tmp_vict, sn)) {


                    if (number_percent() < level) {
                        spell_poison(gsn_poison, level, ch, tmp_vict, TARGET_CHAR);
                    }

                    if (number_percent() < level) {
                        spell_blindness(gsn_blindness, level, ch, tmp_vict);
                    }

                    if (number_percent() < level) {
                        spell_weaken(gsn_weaken, level, tmp_vict);
                    }

                    if (saves_spell(level, tmp_vict, DAM_FIRE)) {
                        dam /= 2;
                    }
                    damage(ch, tmp_vict, dam, sn, DAM_FIRE, true);
                }

            }
        }
    }

    static void spell_doppelganger(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if ((ch == victim) ||
                (is_affected(ch, sn) && (ch.doppel == victim))) {
            act("You already look like $M.", ch, null, victim, TO_CHAR);
            return;
        }

        if (IS_NPC(victim)) {
            act("$N is too different from yourself to mimic.", ch, null, victim, TO_CHAR);
            return;
        }

        if (IS_IMMORTAL(victim)) {
            send_to_char("Yeah, sure. And I'm the Pope.\n", ch);
            return;
        }

        if (saves_spell(level, victim, DAM_CHARM)) {
            send_to_char("You failed.\n", ch);
            return;
        }

        act("You change form to look like $N.", ch, null, victim, TO_CHAR);
        act("$n changes form to look like YOU!", ch, null, victim, TO_VICT);
        act("$n changes form to look like $N!", ch, null, victim, TO_NOTVICT);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = (2 * level) / 3;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = 0;

        affect_to_char(ch, af);
        ch.doppel = victim;

    }

    static void spell_manacles(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (!IS_SET(victim.act, PLR_WANTED)) {
            act("But $N is not wanted.", ch, null, victim, TO_CHAR);
            return;
        }

        if (!is_affected(victim, sn) && !saves_spell(ch.level, victim, DAM_CHARM)) {
            AFFECT_DATA af = new AFFECT_DATA();

            af.where = TO_AFFECTS;
            af.type = sn;
            af.level = level;
            af.duration = 5 + level / 5;
            af.bitvector = 0;

            af.modifier = 0 - (get_curr_stat(victim, STAT_DEX) - 4);
            af.location = APPLY_DEX;
            affect_to_char(victim, af);

            af.modifier = -5;
            af.location = APPLY_HITROLL;
            affect_to_char(victim, af);


            af.modifier = -10;
            af.location = APPLY_DAMROLL;
            affect_to_char(victim, af);

            spell_charm_person(gsn_charm_person, level, ch, vo);
        }
    }

    static void spell_shield_ruler(Skill sn, int level, CHAR_DATA ch) {
        int shield_vnum;
        OBJ_DATA shield;

        if (level >= 71) {
            shield_vnum = OBJ_VNUM_RULER_SHIELD4;
        } else if (level >= 51) {
            shield_vnum = OBJ_VNUM_RULER_SHIELD3;
        } else if (level >= 31) {
            shield_vnum = OBJ_VNUM_RULER_SHIELD2;
        } else {
            shield_vnum = OBJ_VNUM_RULER_SHIELD1;
        }

        shield = create_object(get_obj_index(shield_vnum), level);
        shield.timer = level;
        shield.level = ch.level;
        shield.cost = 0;
        obj_to_char(shield, ch);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_OBJECT;
        af.type = sn;
        af.level = level;
        af.duration = -1;
        af.modifier = level / 8;
        af.bitvector = 0;

        af.location = APPLY_HITROLL;
        affect_to_obj(shield, af);

        af.location = APPLY_DAMROLL;
        affect_to_obj(shield, af);


        af.where = TO_OBJECT;
        af.type = sn;
        af.level = level;
        af.duration = -1;
        af.modifier = -level / 2;
        af.bitvector = 0;
        af.location = APPLY_AC;
        affect_to_obj(shield, af);

        af.where = TO_OBJECT;
        af.type = sn;
        af.level = level;
        af.duration = -1;
        af.modifier = -level / 9;
        af.bitvector = 0;
        af.location = APPLY_SAVING_SPELL;
        affect_to_obj(shield, af);

        af.where = TO_OBJECT;
        af.type = sn;
        af.level = level;
        af.duration = -1;
        af.modifier = UMAX(1, level / 30);
        af.bitvector = 0;
        af.location = APPLY_CHA;
        affect_to_obj(shield, af);

        act("You create $p!", ch, shield, null, TO_CHAR);
        act("$n creates $p!", ch, shield, null, TO_ROOM);
    }

    static void spell_guard_call(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA gch;
        CHAR_DATA guard;
        CHAR_DATA guard2;
        CHAR_DATA guard3;
        String buf = "Guards! Guards!";
        int i;

        if (is_affected(ch, sn)) {
            send_to_char("You lack the power to call another three guards now.\n", ch);
            return;
        }

        do_yell(ch, buf);

        for (gch = char_list; gch != null; gch = gch.next) {
            if (IS_NPC(gch) && IS_AFFECTED(gch, AFF_CHARM) && gch.master == ch
                    && gch.pIndexData.vnum == MOB_VNUM_SPECIAL_GUARD) {
                do_say(gch, "What? I'm not good enough?");
                return;
            }
        }

        if (count_charmed(ch) != 0) {
            return;
        }

        guard = create_mobile(get_mob_index(MOB_VNUM_SPECIAL_GUARD));

        for (i = 0; i < MAX_STATS; i++) {
            guard.perm_stat[i] = ch.perm_stat[i];
        }

        guard.max_hit = 2 * ch.max_hit;
        guard.hit = guard.max_hit;
        guard.max_mana = ch.max_mana;
        guard.mana = guard.max_mana;
        guard.alignment = ch.alignment;
        guard.level = ch.level;
        for (i = 0; i < 3; i++) {
            guard.armor[i] = interpolate(guard.level, 100, -200);
        }
        guard.armor[3] = interpolate(guard.level, 100, -100);
        guard.sex = ch.sex;
        guard.gold = 0;
        guard.timer = 0;

        guard.damage[DICE_NUMBER] = number_range(level / 16, level / 12);
        guard.damage[DICE_TYPE] = number_range(level / 3, level / 2);
        guard.damage[DICE_BONUS] = number_range(level / 9, level / 6);

        guard.affected_by = SET_BIT(guard.affected_by, (A | C | D | E | F | G | H | BIT_30));
        guard.affected_by = SET_BIT(guard.affected_by, AFF_CHARM);
        guard.affected_by = SET_BIT(guard.affected_by, AFF_SANCTUARY);

        guard2 = create_mobile(guard.pIndexData);
        clone_mobile(guard, guard2);

        guard3 = create_mobile(guard.pIndexData);
        clone_mobile(guard, guard3);

        guard.master = guard2.master = guard3.master = ch;
        guard.leader = guard2.leader = guard3.leader = ch;

        char_to_room(guard, ch.in_room);
        char_to_room(guard2, ch.in_room);
        char_to_room(guard3, ch.in_room);
        send_to_char("Three guards come to your rescue!\n", ch);
        act("Three guards come to $n's rescue!", ch, null, null, TO_ROOM);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 6;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

    }

    static void spell_nightwalker(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA gch;
        CHAR_DATA walker;
        int i;

        if (is_affected(ch, sn)) {
            send_to_char("You feel too weak to summon a Nightwalker now.\n",
                    ch);
            return;
        }

        send_to_char("You attempt to summon a Nightwalker.\n", ch);
        act("$n attempts to summon a Nightwalker.", ch, null, null, TO_ROOM);

        for (gch = char_list; gch != null; gch = gch.next) {
            if (IS_NPC(gch) && IS_AFFECTED(gch, AFF_CHARM) && gch.master == ch &&
                    gch.pIndexData.vnum == MOB_VNUM_NIGHTWALKER) {
                send_to_char("Two Nightwalkers are more than you can control!\n", ch);
                return;
            }
        }

        if (count_charmed(ch) != 0) {
            return;
        }

        walker = create_mobile(get_mob_index(MOB_VNUM_NIGHTWALKER));

        for (i = 0; i < MAX_STATS; i++) {
            walker.perm_stat[i] = ch.perm_stat[i];
        }

        walker.max_hit = IS_NPC(ch) ? URANGE(ch.max_hit, ch.max_hit, 30000)
                : URANGE(ch.pcdata.perm_hit, ch.pcdata.perm_hit, 30000);
        walker.hit = walker.max_hit;
        walker.max_mana = ch.max_mana;
        walker.mana = walker.max_mana;
        walker.level = ch.level;
        for (i = 0; i < 3; i++) {
            walker.armor[i] = interpolate(walker.level, 100, -100);
        }
        walker.armor[3] = interpolate(walker.level, 100, 0);
        walker.gold = 0;
        walker.timer = 0;
        walker.damage[DICE_NUMBER] = number_range(level / 15, level / 10);
        walker.damage[DICE_TYPE] = number_range(level / 3, level / 2);
        walker.damage[DICE_BONUS] = 0;

        char_to_room(walker, ch.in_room);
        send_to_char("A Nightwalker rises from the shadows!\n", ch);
        act("A Nightwalker rises from the shadows!", ch, null, null, TO_ROOM);
        TextBuffer buf = new TextBuffer();
        buf.sprintf("A Nightwalker kneels before you.");
        send_to_char(buf, ch);
        buf.sprintf("A Nightwalker kneels before %s!", ch.name);
        act(buf, ch, null, null, TO_ROOM);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

        walker.affected_by = SET_BIT(walker.affected_by, AFF_CHARM);
        walker.master = walker.leader = ch;

    }

    static void spell_eyes(CHAR_DATA ch) {
        CHAR_DATA victim;
        ROOM_INDEX_DATA ori_room;

        if ((victim = get_char_world(ch, target_name)) == null) {
            send_to_char("Your spy network reveals no such player.\n", ch);
            return;
        }

        if ((victim.level > ch.level + 7)
                || saves_spell((ch.level + 9), victim, DAM_NONE)) {
            send_to_char("Your spy network cannot find that player.\n", ch);
            return;
        }

        if (ch == victim) {
            do_look(ch, "auto");
        } else {
            ori_room = ch.in_room;
            char_from_room(ch);
            char_to_room(ch, victim.in_room);
            do_look(ch, "auto");
            char_from_room(ch);
            char_to_room(ch, ori_room);
        }
    }

    static void spell_shadow_cloak(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (ch.cabal != victim.cabal) {
            send_to_char("You may only use this spell on fellow cabal members.\n", ch);
            return;
        }

        if (is_affected(victim, sn)) {
            if (victim == ch) {
                send_to_char("You are already protected by a shadow cloak.\n", ch);
            } else {
                act("$N is already protected by a shadow cloak.", ch, null, victim, TO_CHAR);
            }
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.modifier = -level;
        af.location = APPLY_AC;
        af.bitvector = 0;
        affect_to_char(victim, af);
        send_to_char("You feel the shadows protect you.\n", victim);
        if (ch != victim) {
            act("A cloak of shadows protect $N.", ch, null, victim, TO_CHAR);
        }
    }

    static void spell_nightfall(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA vch;
        OBJ_DATA light;

        if (is_affected(ch, sn)) {
            send_to_char("You can't find the power to control lights.\n", ch);
            return;
        }

        for (vch = ch.in_room.people; vch != null; vch = vch.next_in_room) {
            for (light = vch.carrying; light != null; light = light.next_content) {
                if (light.item_type == ITEM_LIGHT && light.value[2] != 0
                        && !is_same_group(ch, vch)) {
                    if ( /*light.value[2] != -1 ||*/ saves_spell(level, vch, DAM_ENERGY)) {
                        act("$p flickers and goes out!", ch, light, null, TO_CHAR);
                        act("$p flickers and goes out!", ch, light, null, TO_ROOM);
                        light.value[2] = 0;
                        if (get_light_char(ch) == null) {
                            ch.in_room.light--;
                        }
                    }
/*    else {
        act("$p momentarily dims.",ch,light,null,TO_CHAR);
        act("$p momentarily dims.",ch,light,null,TO_ROOM);
      } */
                }
            }
        }

        for (light = ch.in_room.contents; light != null; light = light.next_content) {
            if (light.item_type == ITEM_LIGHT && light.value[2] != 0) {
                act("$p flickers and goes out!", ch, light, null, TO_CHAR);
                act("$p flickers and goes out!", ch, light, null, TO_ROOM);
                light.value[2] = 0;
            }
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 2;
        af.modifier = 0;
        af.location = APPLY_NONE;
        af.bitvector = 0;
        affect_to_char(ch, af);
    }

    static void spell_mirror(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int mirrors, new_mirrors;
        CHAR_DATA gch;
        CHAR_DATA tmp_vict;
        int order;

        if (IS_NPC(victim)) {
            send_to_char("Only players can be mirrored.\n", ch);
            return;
        }

        for (mirrors = 0, gch = char_list; gch != null; gch = gch.next) {
            if (IS_NPC(gch) && is_affected(gch, gsn_mirror)
                    && is_affected(gch, gsn_doppelganger) && gch.doppel == victim) {
                mirrors++;
            }
        }

        if (mirrors >= level / 5) {
            if (ch == victim) {
                send_to_char("You cannot be further mirrored.\n", ch);
            } else {
                act("$N cannot be further mirrored.", ch, null, victim, TO_CHAR);
            }
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.level = level;
        af.modifier = 0;
        af.location = 0;
        af.bitvector = 0;

        for (tmp_vict = victim; is_affected(tmp_vict, gsn_doppelganger); tmp_vict = tmp_vict.doppel) {
        }

        TextBuffer long_buf = new TextBuffer();
        TextBuffer short_buf = new TextBuffer();
        sprintf(long_buf, "%s%s is here.\n", tmp_vict.name, tmp_vict.pcdata.title);
        sprintf(short_buf, tmp_vict.name);

        order = number_range(0, level / 5 - mirrors);

        for (new_mirrors = 0; mirrors + new_mirrors < level / 5; new_mirrors++) {
            gch = create_mobile(get_mob_index(MOB_VNUM_MIRROR_IMAGE));
            gch.name = tmp_vict.name;
            gch.short_descr = short_buf.toString();
            gch.long_descr = long_buf.toString();
            gch.description = (tmp_vict.description == null) ? null : tmp_vict.description;
            gch.sex = tmp_vict.sex;

            af.type = gsn_doppelganger;
            af.duration = level;
            affect_to_char(gch, af);
            af.type = gsn_mirror;
            af.duration = -1;
            affect_to_char(gch, af);

            gch.max_hit = gch.hit = 1;
            gch.level = 1;
            gch.doppel = victim;
            gch.master = victim;
            char_to_room(gch, victim.in_room);

            if (number_percent() < 20) {
                ROOM_INDEX_DATA ori_room;

                ori_room = victim.in_room;
                char_from_room(victim);
                char_to_room(victim, ori_room);
            }

            if (new_mirrors == order) {
                char_from_room(victim);
                char_to_room(victim, gch.in_room);
            }


            if (ch == victim) {
                send_to_char("A mirror image of yourself appears beside you!\n", ch);
                act("A mirror image of $n appears beside $M!", ch, null, victim, TO_ROOM);
            } else {
                act("A mirror of $N appears beside $M!", ch, null, victim, TO_CHAR);
                act("A mirror of $N appears beside $M!", ch, null, victim, TO_NOTVICT);
                send_to_char("A mirror image of yourself appears beside you!\n",
                        victim);
            }

        }
    }

    static void spell_garble(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (ch == victim) {
            send_to_char("Garble whose speech?\n", ch);
            return;
        }

        if (is_affected(victim, sn)) {
            act("$N's speech is already garbled.", ch, null, victim, TO_CHAR);
            return;
        }

        if (is_safe_nomessage(ch, victim)) {
            send_to_char("You cannot garble that person.\n", ch);
            return;
        }

        if ((victim.level > ch.level + 7) || saves_spell((ch.level + 9), victim, DAM_MENTAL)) {
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 10;
        af.modifier = 0;
        af.location = 0;
        af.bitvector = 0;
        affect_to_char(victim, af);

        act("You have garbled $N's speech!", ch, null, victim, TO_CHAR);
        send_to_char("You feel your tongue contort.\n", victim);
    }

    static void spell_confuse(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        CHAR_DATA rch;
        int count = 0;

        if (is_affected(victim, gsn_confuse)) {
            act("$N is already thoroughly confused.", ch, null, victim, TO_CHAR);
            return;
        }

        if (saves_spell(level, victim, DAM_MENTAL)) {
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 10;
        af.modifier = 0;
        af.location = 0;
        af.bitvector = 0;
        affect_to_char(victim, af);

        for (rch = ch.in_room.people; rch != null; rch = rch.next_in_room) {
            if (rch == ch
                    && !can_see(ch, rch)
                    && get_trust(ch) < rch.invis_level) {
                count++;
            }
        }

        for (rch = ch.in_room.people; rch != null; rch = rch.next_in_room) {
            if (rch != ch
                    && can_see(ch, rch)
                    && get_trust(ch) >= rch.invis_level
                    && number_range(1, count) == 1) {
                break;
            }
        }

        if (rch != null) {
            do_murder(victim, rch.name);
        }
        do_murder(victim, ch.name);
    }

    static void spell_terangreal(Skill sn, int level, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_NPC(victim)) {
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();

        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 10;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_SLEEP;
        affect_join(victim, af);

        if (IS_AWAKE(victim)) {
            send_to_char("You are overcome by a sudden surge of fatigue.\n",
                    victim);
            act("$n falls into a deep sleep.", victim, null, null, TO_ROOM);
            victim.position = POS_SLEEPING;
        }

    }

    static void spell_kassandra(Skill sn, int level, CHAR_DATA ch) {


        if (is_affected(ch, sn)) {
            send_to_char
                    ("The kassandra has been used for this purpose too recently.\n",
                            ch);
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 5;
        af.location = 0;
        af.modifier = 0;
        af.bitvector = 0;
        affect_to_char(ch, af);
        ch.hit = UMIN(ch.hit + 150, ch.max_hit);
        update_pos(ch);
        send_to_char("A warm feeling fills your body.\n", ch);
        act("$n looks better.", ch, null, null, TO_ROOM);
    }


    static void spell_sebat(Skill sn, int level, CHAR_DATA ch) {

        if (is_affected(ch, sn)) {
            send_to_char("The kassandra has been used for that too recently.\n"
                    , ch);
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = level;
        af.location = APPLY_AC;
        af.modifier = -30;
        af.bitvector = 0;
        affect_to_char(ch, af);
        act("$n is surrounded by a mystical shield.", ch, null, null, TO_ROOM);
        send_to_char("You are surrounded by a mystical shield.\n", ch);
    }


    static void spell_matandra(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        if (is_affected(ch, sn)) {
            send_to_char
                    ("The kassandra has been used for this purpose too recently.\n",
                            ch);
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 5;
        af.location = 0;
        af.modifier = 0;
        af.bitvector = 0;
        affect_to_char(ch, af);
        dam = dice(level, 7);

        damage(ch, victim, dam, sn, DAM_HOLY, true);

    }

    @SuppressWarnings("UnusedParameters")
    static void spell_amnesia(CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_NPC(victim)) {
            return;
        }

        for (int i = 0; i < MAX_SKILL; i++) {
            victim.pcdata.learned[i] /= 2;
        }

        act("You feel your memories slip away.", victim, null, null, TO_CHAR);
        act("$n gets a blank look on $s face.", victim, null, null, TO_ROOM);
    }


    static void spell_chaos_blade(Skill sn, int level, CHAR_DATA ch) {
        OBJ_DATA blade;

        blade = create_object(get_obj_index(OBJ_VNUM_CHAOS_BLADE), level);
        send_to_char("You create a blade of chaos!\n", ch);
        act("$n creates a blade of chaos!", ch, null, null, TO_ROOM);

        blade.timer = level * 2;
        blade.level = ch.level;

        if (ch.level <= 10) {
            blade.value[2] = 2;
        } else if (ch.level > 10 && ch.level <= 20) {
            blade.value[2] = 3;
        } else if (ch.level > 20 && ch.level <= 30) {
            blade.value[2] = 4;
        } else if (ch.level > 30 && ch.level <= 40) {
            blade.value[2] = 5;
        } else if (ch.level > 40 && ch.level <= 50) {
            blade.value[2] = 6;
        } else if (ch.level > 50 && ch.level <= 60) {
            blade.value[2] = 7;
        } else if (ch.level > 60 && ch.level <= 70) {
            blade.value[2] = 9;
        } else if (ch.level > 70 && ch.level <= 80) {
            blade.value[2] = 11;
        } else {
            blade.value[2] = 12;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_OBJECT;
        af.type = sn;
        af.level = level;
        af.duration = -1;
        af.modifier = level / 6;
        af.bitvector = 0;

        af.location = APPLY_HITROLL;
        affect_to_obj(blade, af);

        af.location = APPLY_DAMROLL;
        affect_to_obj(blade, af);

        obj_to_char(blade, ch);
    }

    static void spell_tattoo(CHAR_DATA ch, Object vo) {
        OBJ_DATA tattoo;
        CHAR_DATA victim = (CHAR_DATA) vo;
        int i;

        if (IS_NPC(victim)) {
            act("$N is too dumb to worship you!", ch, null, victim, TO_CHAR);
            return;
        }

        for (i = 0; i < MAX_RELIGION; i++) {
            if (!str_cmp(ch.name, religion_table[i].leader)) {
                tattoo = get_eq_char(victim, WEAR_TATTOO);
                if (tattoo != null) {
                    act("$N is already tattooed!  You'll have to remove it first.",
                            ch, null, victim, TO_CHAR);
                    act("$n tried to give you another tattoo but failed.",
                            ch, null, victim, TO_VICT);
                    act("$n tried to give $N another tattoo but failed.",
                            ch, null, victim, TO_NOTVICT);
                    return;
                } else {
                    tattoo = create_object(get_obj_index(religion_table[i].tattoo_vnum), 60);
                    act("You tattoo $N with $p!", ch, tattoo, victim, TO_CHAR);
                    act("$n tattoos $N with $p!", ch, tattoo, victim, TO_NOTVICT);
                    act("$n tattoos you with $p!", ch, tattoo, victim, TO_VICT);

                    obj_to_char(tattoo, victim);
                    equip_char(victim, tattoo, WEAR_TATTOO);
                    return;
                }
            }
        }
        send_to_char("You don't have a religious tattoo.\n", ch);
    }

    static void spell_remove_tattoo(CHAR_DATA ch, Object vo) {
        OBJ_DATA tattoo;
        CHAR_DATA victim = (CHAR_DATA) vo;

        tattoo = get_eq_char(victim, WEAR_TATTOO);
        if (tattoo != null) {
            extract_obj(tattoo);
            act("Through a painful process, your tattoo has been destroyed by $n.",
                    ch, null, victim, TO_VICT);
            act("You remove the tattoo from $N.", ch, null, victim, TO_CHAR);
            act("$N's tattoo is destroyed by $n.", ch, null, victim, TO_NOTVICT);
        } else {
            act("$N doesn't have any tattoos.", ch, null, victim, TO_CHAR);
        }
    }


    static void spell_wrath(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        if (!IS_NPC(ch) && IS_EVIL(ch)) {
            victim = ch;
        }

        if (IS_GOOD(victim)) {
            act("The gods protect $N.", ch, null, victim, TO_ROOM);
            return;
        }

        if (IS_NEUTRAL(victim)) {
            act("$N does not seem to be affected.", ch, null, victim, TO_CHAR);
            return;
        }

        dam = dice(level, 12);

        if (saves_spell(level, victim, DAM_HOLY)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_HOLY, true);

        if (IS_AFFECTED(victim, AFF_CURSE) || saves_spell(level, victim, DAM_HOLY)) {
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 2 * level;
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

    static void spell_old_randomizer(Skill sn, int level, CHAR_DATA ch) {
        ROOM_INDEX_DATA pRoomIndex;
        EXIT_DATA pexit;
        int d0;
        int d1;

        if (is_affected(ch, sn)) {
            send_to_char("Your power of randomness has been exhausted for now.\n", ch);
            return;
        }
        if (IS_SET(ch.in_room.room_flags, ROOM_LAW)) {
            send_to_char("This room is far too orderly for your powers to work on it.\n", ch);
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = UMIN(level + 15, MAX_LEVEL);
        af.location = 0;
        af.modifier = 0;
        af.bitvector = 0;

        pRoomIndex = get_room_index(ch.in_room.vnum);

        if (number_bits(1) == 0) {
            send_to_char("Despite your efforts, the universe resisted chaos.\n", ch);
            if (ch.trust >= 56) {
                af.duration = 1;
            } else {
                af.duration = level;
            }
            affect_to_char(ch, af);
            return;
        }
        for (d0 = 0; d0 < 5; d0++) {
            d1 = number_range(d0, 5);
            pexit = pRoomIndex.exit[d0];
            pRoomIndex.exit[d0] = pRoomIndex.exit[d1];
            pRoomIndex.exit[d1] = pexit;

        }
        if (ch.trust >= 56) {
            af.duration = 1;
        } else {
            af.duration = 2 * level;
        }
        affect_to_char(ch, af);
        send_to_char("The room was successfully randomized!\n", ch);
        send_to_char("You feel very drained from the effort.\n", ch);
        ch.hit -= UMIN(200, ch.hit / 2);

        TextBuffer buf = new TextBuffer();
        sprintf(buf, "%s used randomizer in room %d", ch.name, ch.in_room.vnum);
        log_string(buf.toString());

    }

    static void spell_stalker(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA stalker;
        int i;

        if ((victim = get_char_world(ch, target_name)) == null
                || victim == ch || victim.in_room == null
                || IS_NPC(victim) || !IS_SET(victim.act, PLR_WANTED)) {
            send_to_char("You failed.\n", ch);
            return;
        }

        if (is_affected(ch, sn)) {
            send_to_char("This power is used too recently.\n", ch);
            return;
        }

        if (!is_safe_nomessage(ch, victim) && !IS_SET(ch.act, PLR_CANINDUCT)) {
            send_to_char("You better use special guards for this purpose.\n", ch);
            return;
        }

        send_to_char("You attempt to summon a stalker.\n", ch);
        act("$n attempts to summon a stalker.", ch, null, null, TO_ROOM);

        stalker = create_mobile(get_mob_index(MOB_VNUM_STALKER));
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 6;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

        for (i = 0; i < MAX_STATS; i++) {
            stalker.perm_stat[i] = victim.perm_stat[i];
        }

        stalker.max_hit = UMIN(30000, 2 * victim.max_hit);
        stalker.hit = stalker.max_hit;
        stalker.max_mana = victim.max_mana;
        stalker.mana = stalker.max_mana;
        stalker.level = victim.level;

        stalker.damage[DICE_NUMBER] =
                number_range(victim.level / 8, victim.level / 6);
        stalker.damage[DICE_TYPE] =
                number_range(victim.level / 6, victim.level / 5);
        stalker.damage[DICE_BONUS] =
                number_range(victim.level / 10, victim.level / 8);

        stalker.hitroll = victim.level;
        stalker.damroll = victim.level;
        stalker.size = victim.size;
        for (i = 0; i < 3; i++) {
            stalker.armor[i] = interpolate(stalker.level, 100, -100);
        }
        stalker.armor[3] = interpolate(stalker.level, 100, 0);
        stalker.gold = 0;
        stalker.invis_level = LEVEL_IMMORTAL;
        stalker.affected_by = (H | J | N | O | U | V | BIT_26 | BIT_28) | (A | B | C | D | E | F | G | H | BIT_30);

        char_to_room(stalker, victim.in_room);
        stalker.last_fought = victim;
        send_to_char("An invisible stalker arrives to stalk you!\n", victim);
        act("An invisible stalker arrives to stalk $n!", victim, null, null, TO_ROOM);
        send_to_char("An invisible stalker has been sent.\n", ch);

        TextBuffer buf = new TextBuffer();
        sprintf(buf, "%s used stalker on %s", ch.name, victim.name);
        log_string(buf.toString());
    }


    static void spell_tesseract(int level, CHAR_DATA ch) {
        CHAR_DATA victim;
        CHAR_DATA wch;
        CHAR_DATA wch_next;
        boolean gate_pet;

        if ((victim = get_char_world(ch, target_name)) == null
                || victim == ch
                || victim.in_room == null
                || ch.in_room == null) {
            send_to_char("You failed.\n", ch);
            return;
        }


        if (!can_see_room(ch, victim.in_room)
                || (is_safe(ch, victim) && IS_SET(victim.act, PLR_NOSUMMON))
                || room_is_private(victim.in_room)
                || IS_SET(victim.in_room.room_flags, ROOM_NO_RECALL)
                || IS_SET(ch.in_room.room_flags, ROOM_NO_RECALL)
                || IS_SET(victim.in_room.room_flags, ROOM_NOSUMMON)
                || IS_SET(ch.in_room.room_flags, ROOM_NOSUMMON)
                || (!IS_NPC(victim) && victim.level >= LEVEL_HERO)  /* NOT trust */
                || (IS_NPC(victim) && IS_SET(victim.imm_flags, IMM_SUMMON))
                || (!IS_NPC(victim) && IS_SET(victim.act, PLR_NOSUMMON)
                && is_safe_nomessage(ch, victim))
                || (saves_spell(level, victim, DAM_NONE))) {
            send_to_char("You failed.\n", ch);
            return;
        }

        gate_pet = ch.pet != null && ch.in_room == ch.pet.in_room;

        for (wch = ch.in_room.people; wch != null; wch = wch_next) {
            wch_next = wch.next_in_room;
            if (is_same_group(wch, ch) && wch != ch) {
                act("$n utters some strange words and, with a sickening lurch, you feel time", ch, null, wch, TO_VICT);
                act("and space shift around you.", ch, null, wch, TO_VICT);
                if (victim.in_room == null) {
                    bug("Tesseract: victim room has become null!!!", 0);
                    return;
                }
                char_from_room(wch);
                char_to_room(wch, victim.in_room);
                act("$n arrives suddenly.", wch, null, null, TO_ROOM);
                if (wch.in_room == null) {
                    bug("Tesseract: other char sent to null room");
                } else {
                    do_look(wch, "auto");
                }
            }
        }

        act("With a sudden flash of light, $n and $s friends disappear!", ch, null, null, TO_ROOM);
        send_to_char("As you utter the words, time and space seem to blur.  You feel as though\nspace and time are shifting all around you while you remain motionless.\n", ch);
        char_from_room(ch);
        char_to_room(ch, victim.in_room);

        act("$n arrives suddenly.", ch, null, null, TO_ROOM);
        if (ch.in_room == null) {
            bug("Tesseract: char sent to null room");
        } else {
            do_look(ch, "auto");
        }

        if (gate_pet) {
            send_to_char("You feel time and space shift around you.\n", ch.pet);
            char_from_room(ch.pet);
            char_to_room(ch.pet, victim.in_room);
            act("$n arrives suddenly.", ch.pet, null, null, TO_ROOM);
            if (ch.pet.in_room == null) {
                bug("Tesseract: pet sent to null room");
            } else {
                do_look(ch.pet, "auto");
            }
        }
    }

    static void spell_brew(int level, CHAR_DATA ch, Object vo) {
        OBJ_DATA obj = (OBJ_DATA) vo;
        OBJ_DATA potion;
        OBJ_DATA vial;

        if (obj.item_type != ITEM_TRASH && obj.item_type != ITEM_TREASURE
                && obj.item_type != ITEM_KEY) {
            send_to_char("That can't be transformed into a potion.\n", ch);
            return;
        }

        if (obj.wear_loc != -1) {
            send_to_char("The item must be carried to be brewed.\n", ch);
            return;
        }

        for (vial = ch.carrying; vial != null; vial = vial.next_content) {
            if (vial.pIndexData.vnum == OBJ_VNUM_POTION_VIAL) {
                break;
            }
        }
        if (vial == null) {
            send_to_char("You don't have any vials to brew the potion into.\n"
                    , ch);
            return;
        }


        if (number_percent() < 50) {
            send_to_char("You failed and destroyed it.\n", ch);
            extract_obj(obj);
            return;
        }

        if (obj.item_type == ITEM_TRASH) {
            potion = create_object(get_obj_index(OBJ_VNUM_POTION_SILVER), level);
        } else if (obj.item_type == ITEM_TREASURE) {
            potion = create_object(get_obj_index(OBJ_VNUM_POTION_GOLDEN), level);
        } else {
            potion = create_object(get_obj_index(OBJ_VNUM_POTION_SWIRLING), level);
        }

        Skill spell = null;


        potion.value[0] = level;

        if (obj.item_type == ITEM_TRASH) {
            if (number_percent() < 20) {
                spell = Skill.gsn_fireball;
            } else if (number_percent() < 40) {
                spell = Skill.gsn_cure_poison;
            } else if (number_percent() < 60) {
                spell = Skill.gsn_cure_blindness;
            } else if (number_percent() < 80) {
                spell = Skill.gsn_cure_disease;
            } else {
                spell = Skill.gsn_word_of_recall;
            }
        } else if (obj.item_type == ITEM_TREASURE) {
            switch (number_bits(3)) {
                case 0:
                    spell = Skill.gsn_cure_critical;
                    break;
                case 1:
                    spell = Skill.gsn_haste;
                    break;
                case 2:
                    spell = Skill.gsn_frenzy;
                    break;
                case 3:
                    spell = Skill.gsn_create_spring;
                    break;
                case 4:
                    spell = Skill.gsn_holy_word;
                    break;
                case 5:
                    spell = Skill.gsn_invis;
                    break;
                case 6:
                    spell = Skill.gsn_cure_light;
                    break;
                case 7:
                    spell = Skill.gsn_cure_serious;
                    break;

            }
        } else {
            if (number_percent() < 20) {
                spell = Skill.gsn_detect_magic;
            } else if (number_percent() < 40) {
                spell = Skill.gsn_detect_invis;
            } else if (number_percent() < 65) {
                spell = Skill.gsn_pass_door;
            } else {
                spell = Skill.gsn_acute_vision;
            }
        }
        if (spell == null) {
            return;
        }
        potion.value[1] = spell.ordinal();
        extract_obj(obj);
        act("You brew $p from your resources!", ch, potion, null, TO_CHAR);
        act("$n brews $p from $s resources!", ch, potion, null, TO_ROOM);

        obj_to_char(potion, ch);
        extract_obj(vial);

    }


    static void spell_shadowlife(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        CHAR_DATA shadow;
        int i;
        String name;

        if (IS_NPC(victim)) {
            send_to_char("Now, why would you want to do that?!?\n", ch);
            return;
        }

        if (is_affected(ch, sn)) {
            send_to_char("You don't have the strength to raise a Shadow now.\n",
                    ch);
            return;
        }

        act("You give life to $N's shadow!", ch, null, victim, TO_CHAR);
        act("$n gives life to $N's shadow!", ch, null, victim, TO_NOTVICT);
        act("$n gives life to your shadow!", ch, null, victim, TO_VICT);

        shadow = create_mobile(get_mob_index(MOB_VNUM_SHADOW));

        for (i = 0; i < MAX_STATS; i++) {
            shadow.perm_stat[i] = ch.perm_stat[i];
        }

        shadow.max_hit = (3 * ch.max_hit) / 4;
        shadow.hit = shadow.max_hit;
        shadow.max_mana = (3 * ch.max_mana) / 4;
        shadow.mana = shadow.max_mana;
        shadow.alignment = ch.alignment;
        shadow.level = ch.level;
        for (i = 0; i < 3; i++) {
            shadow.armor[i] = interpolate(shadow.level, 100, -100);
        }
        shadow.armor[3] = interpolate(shadow.level, 100, 0);
        shadow.sex = victim.sex;
        shadow.gold = 0;

        name = IS_NPC(victim) ? victim.short_descr : victim.name;
        TextBuffer buf = new TextBuffer();
        buf.sprintf(shadow.short_descr, name);
        shadow.short_descr = buf.toString();

        buf.sprintf(shadow.long_descr, name);
        shadow.long_descr = buf.toString();

        buf.sprintf(shadow.description, name);
        shadow.description = buf.toString();

        char_to_room(shadow, ch.in_room);

        do_murder(shadow, victim.name);
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

    }

    static void spell_ruler_badge(Skill sn, int level, CHAR_DATA ch, Object vo) {
        OBJ_DATA badge;
        CHAR_DATA victim = (CHAR_DATA) vo;
        OBJ_DATA obj_next;

        if (count_worn(ch, WEAR_NECK) >= max_can_wear(ch, WEAR_NECK)) {
            send_to_char("But you are wearing something else.\n", ch);
            return;
        }

        for (badge = ch.carrying; badge != null;
             badge = obj_next) {
            obj_next = badge.next_content;
            if (badge.pIndexData.vnum == OBJ_VNUM_DEPUTY_BADGE || badge.pIndexData.vnum == OBJ_VNUM_RULER_BADGE) {
                act("Your $p vanishes.", ch, badge, null, TO_CHAR);
                obj_from_char(badge);
                extract_obj(badge);
            }
        }


        badge = create_object(get_obj_index(OBJ_VNUM_RULER_BADGE), level);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_OBJECT;
        af.type = sn;
        af.level = level;
        af.duration = -1;
        af.modifier = 100 + level / 2;
        af.bitvector = 0;

        af.location = APPLY_HIT;
        affect_to_obj(badge, af);

        af.location = APPLY_MANA;
        affect_to_obj(badge, af);

        af.where = TO_OBJECT;
        af.type = sn;
        af.level = level;
        af.duration = -1;
        af.modifier = level / 8;
        af.bitvector = 0;

        af.location = APPLY_HITROLL;
        affect_to_obj(badge, af);

        af.location = APPLY_DAMROLL;
        affect_to_obj(badge, af);


        badge.timer = 200;
        act("You wear the ruler badge!", ch, null, null, TO_CHAR);
        act("$n wears the $s ruler badge!", ch, null, null, TO_ROOM);

        obj_to_char(badge, victim);
        equip_char(ch, badge, WEAR_NECK);
        ch.hit = UMIN((ch.hit + 100 + level / 2), ch.max_hit);
        ch.mana = UMIN((ch.mana + 100 + level / 2), ch.max_mana);

    }

    static void spell_remove_badge(CHAR_DATA ch, Object vo) {
        OBJ_DATA badge;
        CHAR_DATA victim = (CHAR_DATA) vo;
        OBJ_DATA obj_next;

        for (badge = victim.carrying; badge != null;
             badge = obj_next) {
            obj_next = badge.next_content;
            if (badge.pIndexData.vnum == OBJ_VNUM_DEPUTY_BADGE
                    || badge.pIndexData.vnum == OBJ_VNUM_RULER_BADGE) {
                act("Your $p vanishes.", ch, badge, null, TO_CHAR);
                act("$n's $p vanishes.", ch, badge, null, TO_ROOM);

                obj_from_char(badge);
                extract_obj(badge);
            }
        }
    }

    static void spell_dragon_strength(Skill sn, int level, CHAR_DATA ch) {

        if (is_affected(ch, sn)) {
            send_to_char("You are already full of the strength of the dragon.\n",
                    ch);
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = level / 3;
        af.bitvector = 0;

        af.modifier = 2;
        af.location = APPLY_HITROLL;
        affect_to_char(ch, af);

        af.modifier = 2;
        af.location = APPLY_DAMROLL;
        affect_to_char(ch, af);

        af.modifier = 10;
        af.location = APPLY_AC;
        affect_to_char(ch, af);

        af.modifier = 2;
        af.location = APPLY_STR;
        affect_to_char(ch, af);

        af.modifier = -2;
        af.location = APPLY_DEX;
        affect_to_char(ch, af);

        send_to_char("The strength of the dragon enters you.\n", ch);
        act("$n looks a bit meaner now.", ch, null, null, TO_ROOM);
    }

    static void spell_dragon_breath(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 6);
        if (!is_safe_spell(ch, victim, true)) {
            if (saves_spell(level, victim, DAM_FIRE)) {
                dam /= 2;
            }
            damage(ch, victim, dam, sn, DAM_FIRE, true);
        }
    }

    static void spell_golden_aura(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA vch;

        for (vch = ch.in_room.people; vch != null; vch = vch.next_in_room) {
            if (!is_same_group(vch, ch)) {
                continue;
            }

            if (is_affected(vch, sn) || is_affected(vch, gsn_bless) ||
                    IS_AFFECTED(vch, AFF_PROTECT_EVIL)) {
                if (vch == ch) {
                    send_to_char("You are already protected by a golden aura.\n", ch);
                } else {
                    act("$N is already protected by a golden aura.", ch, null, vch, TO_CHAR);
                }
                continue;
            }

            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = sn;
            af.level = level;
            af.duration = 6 + level;
            af.modifier = 0;
            af.location = APPLY_NONE;
            af.bitvector = AFF_PROTECT_EVIL;
            affect_to_char(vch, af);

            af.modifier = level / 8;
            af.location = APPLY_HITROLL;
            af.bitvector = 0;
            affect_to_char(vch, af);

            af.modifier = 0 - level / 8;
            af.location = APPLY_SAVING_SPELL;
            affect_to_char(vch, af);

            send_to_char("You feel a golden aura around you.\n", vch);
            if (ch != vch) {
                act("A golden aura surrounds $N.", ch, null, vch, TO_CHAR);
            }

        }
    }

    static void spell_dragonplate(Skill sn, int level, CHAR_DATA ch) {
        int plate_vnum;
        OBJ_DATA plate;

        plate_vnum = OBJ_VNUM_PLATE;

        plate = create_object(get_obj_index(plate_vnum), level + 5);
        plate.timer = 2 * level;
        plate.cost = 0;
        plate.level = ch.level;

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_OBJECT;
        af.type = sn;
        af.level = level;
        af.duration = -1;
        af.modifier = level / 8;
        af.bitvector = 0;

        af.location = APPLY_HITROLL;
        affect_to_obj(plate, af);

        af.location = APPLY_DAMROLL;
        affect_to_obj(plate, af);

        obj_to_char(plate, ch);

        act("You create $p!", ch, plate, null, TO_CHAR);
        act("$n creates $p!", ch, plate, null, TO_ROOM);
    }

    static void spell_squire(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA gch;
        CHAR_DATA squire;
        int i;

        if (is_affected(ch, sn)) {
            send_to_char("You cannot command another squire right now.\n",
                    ch);
            return;
        }

        send_to_char("You attempt to summon a squire.\n", ch);
        act("$n attempts to summon a squire.", ch, null, null, TO_ROOM);

        for (gch = char_list; gch != null; gch = gch.next) {
            if (IS_NPC(gch) && IS_AFFECTED(gch, AFF_CHARM) && gch.master == ch &&
                    gch.pIndexData.vnum == MOB_VNUM_SQUIRE) {
                send_to_char("Two squires are more than you need!\n", ch);
                return;
            }
        }

        if (count_charmed(ch) != 0) {
            return;
        }

        squire = create_mobile(get_mob_index(MOB_VNUM_SQUIRE));

        for (i = 0; i < MAX_STATS; i++) {
            squire.perm_stat[i] = ch.perm_stat[i];
        }

        squire.max_hit = ch.max_hit;
        squire.hit = squire.max_hit;
        squire.max_mana = ch.max_mana;
        squire.mana = squire.max_mana;
        squire.level = ch.level;
        for (i = 0; i < 3; i++) {
            squire.armor[i] = interpolate(squire.level, 100, -100);
        }
        squire.armor[3] = interpolate(squire.level, 100, 0);
        squire.gold = 0;

        TextBuffer buf = new TextBuffer();
        buf.sprintf(squire.short_descr, ch.name);
        squire.short_descr = buf.toString();

        buf.sprintf(squire.long_descr, ch.name);
        squire.long_descr = buf.toString();

        buf.sprintf(squire.description, ch.name);
        squire.description = buf.toString();

        squire.damage[DICE_NUMBER] = number_range(level / 15, level / 12);
        squire.damage[DICE_TYPE] = number_range(level / 3, level / 2);
        squire.damage[DICE_BONUS] = number_range(level / 8, level / 6);

        char_to_room(squire, ch.in_room);
        send_to_char("A squire arrives from nowhere!\n", ch);
        act("A squire arrives from nowhere!", ch, null, null, TO_ROOM);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

        squire.affected_by = SET_BIT(squire.affected_by, AFF_CHARM);
        squire.master = squire.leader = ch;

    }


    static void spell_dragonsword(Skill sn, int level, CHAR_DATA ch) {
        int sword_vnum;
        OBJ_DATA sword;
        StringBuilder argb = new StringBuilder();
        target_name = one_argument(target_name, argb);
        String arg = argb.toString();

        if (!str_cmp(arg, "sword")) {
            sword_vnum = OBJ_VNUM_DRAGONSWORD;
        } else if (!str_cmp(arg, "mace")) {
            sword_vnum = OBJ_VNUM_DRAGONMACE;
        } else if (!str_cmp(arg, "dagger")) {
            sword_vnum = OBJ_VNUM_DRAGONDAGGER;
        } else if (!str_cmp(arg, "lance")) {
            sword_vnum = OBJ_VNUM_DRAGONLANCE;
        } else {
            send_to_char("You can't make a DragonSword like that!", ch);
            return;
        }

        sword = create_object(get_obj_index(sword_vnum), level);
        sword.timer = level * 2;
        sword.cost = 0;
        if (ch.level < 50) {
            sword.value[2] = (ch.level / 10);
        } else {
            sword.value[2] = (ch.level / 6) - 3;
        }
        sword.level = ch.level;

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_OBJECT;
        af.type = sn;
        af.level = level;
        af.duration = -1;
        af.modifier = level / 5;
        af.bitvector = 0;

        af.location = APPLY_HITROLL;
        affect_to_obj(sword, af);

        af.location = APPLY_DAMROLL;
        affect_to_obj(sword, af);

        if (IS_GOOD(ch)) {
            sword.extra_flags = SET_BIT(sword.extra_flags, (ITEM_ANTI_NEUTRAL | ITEM_ANTI_EVIL));
        } else if (IS_NEUTRAL(ch)) {
            sword.extra_flags = SET_BIT(sword.extra_flags, (ITEM_ANTI_GOOD | ITEM_ANTI_EVIL));
        } else if (IS_EVIL(ch)) {
            sword.extra_flags = SET_BIT(sword.extra_flags, (ITEM_ANTI_NEUTRAL | ITEM_ANTI_GOOD));
        }
        obj_to_char(sword, ch);

        act("You create $p!", ch, sword, null, TO_CHAR);
        act("$n creates $p!", ch, sword, null, TO_ROOM);
    }

    static void spell_entangle(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (ch.in_room.sector_type == SECT_INSIDE ||
                ch.in_room.sector_type == SECT_CITY ||
                ch.in_room.sector_type == SECT_DESERT ||
                ch.in_room.sector_type == SECT_AIR) {
            send_to_char("No plants can grow here.\n", ch);
            return;
        }

//        int dam = number_range(level, 4 * level);
//        if (saves_spell(level, victim, DAM_PIERCE)) {
//            dam /= 2;
//        }

        //todo: ch->level -> dam?
        damage(ch, victim, ch.level, gsn_entangle, DAM_PIERCE, true);

        act("The thorny plants spring up around $n, entangling $s legs!", victim, null, null, TO_ROOM);
        act("The thorny plants spring up around you, entangling your legs!", victim, null, null, TO_CHAR);

        victim.move -= dice(level, 6);
        victim.move = UMAX(0, victim.move);

        if (!is_affected(victim, gsn_entangle)) {
            AFFECT_DATA todex = new AFFECT_DATA();

            todex.type = gsn_entangle;
            todex.level = level;
            todex.duration = level / 10;
            todex.location = APPLY_DEX;
            todex.modifier = -(level / 10);
            todex.bitvector = 0;
            affect_to_char(victim, todex);

        }
    }

    static void spell_holy_armor(Skill sn, int level, CHAR_DATA ch) {
        if (is_affected(ch, sn)) {
            send_to_char("You are already protected from harm.", ch);
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = level;
        af.location = APPLY_AC;
        af.modifier = -UMAX(10, 10 * (level / 5));
        af.bitvector = 0;
        affect_to_char(ch, af);
        act("$n is protected from harm.", ch, null, null, TO_ROOM);
        send_to_char("Your are protected from harm.\n", ch);

    }

    static void spell_love_potion(Skill sn, int level, CHAR_DATA ch) {
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 50;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

        send_to_char("You feel like looking at people.\n", ch);
    }

    static void spell_protective_shield(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(victim, sn)) {
            if (victim == ch) {
                send_to_char("You are already surrounded by a protective shield.\n",
                        ch);
            } else {
                act("$N is already surrounded by a protective shield.", ch, null,
                        victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = number_fuzzy(level / 30) + 3;
        af.location = APPLY_AC;
        af.modifier = 20;
        af.bitvector = 0;
        affect_to_char(victim, af);
        act("$n is surrounded by a protective shield.", victim, null, null, TO_ROOM);
        send_to_char("You are surrounded by a protective shield.\n", victim);
    }

    static void spell_deafen(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (ch == victim) {
            send_to_char("Deafen who?\n", ch);
            return;
        }

        if (is_affected(victim, sn)) {
            act("$N is already deaf.", ch, null, victim, TO_CHAR);
            return;
        }

        if (is_safe_nomessage(ch, victim)) {
            send_to_char("You cannot deafen that person.\n", ch);
            return;
        }

        if (saves_spell(level, victim, DAM_NONE)) {
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 10;
        af.modifier = 0;
        af.location = 0;
        af.bitvector = 0;
        affect_to_char(victim, af);

        act("You have deafened $N!", ch, null, victim, TO_CHAR);
        send_to_char("A loud ringing fills your ears...you can't hear anything!\n",
                victim);
    }

    static void spell_disperse(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA vch;
        CHAR_DATA vch_next;
        ROOM_INDEX_DATA pRoomIndex;

        if (is_affected(ch, sn)) {
            send_to_char("You aren't up to dispersing this crowd.\n", ch);
            return;
        }

        for (vch = ch.in_room.people; vch != null; vch = vch_next) {
            vch_next = vch.next_in_room;

            if (vch.in_room != null
                    && !IS_SET(vch.in_room.room_flags, ROOM_NO_RECALL)
                    && !IS_IMMORTAL(vch)
                    && ((IS_NPC(vch) && !IS_SET(vch.act, ACT_AGGRESSIVE)) ||
/*      (!IS_NPC(vch) && vch.level > PK_MIN_LEVEL && (vch.level < level || */
                    (!IS_NPC(vch) && vch.level > PK_MIN_LEVEL && (
                            !is_safe_nomessage(ch, vch)))) && vch != ch
                    && !IS_SET(vch.imm_flags, IMM_SUMMON)) {
                for (; ; ) {
                    pRoomIndex = get_room_index(number_range(0, 65535));
                    if (pRoomIndex != null) {
                        if (can_see_room(ch, pRoomIndex)
                                && !room_is_private(pRoomIndex) &&
                                !IS_SET(pRoomIndex.room_flags, ROOM_NO_RECALL)) {
                            break;
                        }
                    }
                }

                send_to_char("The world spins around you!\n", vch);
                act("$n vanishes!", vch, null, null, TO_ROOM);
                char_from_room(vch);
                char_to_room(vch, pRoomIndex);
                act("$n slowly fades into existence.", vch, null, null, TO_ROOM);
                do_look(vch, "auto");
            }
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 10;
        af.modifier = 0;
        af.location = APPLY_NONE;
        af.bitvector = 0;
        affect_to_char(ch, af);

    }


    static void spell_honor_shield(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(victim, sn)) {
            if (victim == ch) {
                send_to_char("But you're already protected by your honor.\n", ch);
            } else {
                send_to_char("They're already protected by their honor.\n", ch);
            }
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.modifier = -30;
        af.location = APPLY_AC;
        af.bitvector = 0;
        affect_to_char(victim, af);

        spell_remove_curse(level, ch, victim, TARGET_CHAR);
        spell_bless(Skill.gsn_bless, level, ch, victim, TARGET_CHAR);

        send_to_char("Your honor protects you.\n", victim);
        act("$n's Honor protects $m.", victim, null, null, TO_ROOM);
    }

    static void spell_acute_vision(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_ACUTE_VISION)) {
            if (victim == ch) {
                send_to_char("Your vision is already acute. \n", ch);
            } else {
                act("$N already sees acutely.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = level;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_ACUTE_VISION;
        affect_to_char(victim, af);
        send_to_char("Your vision sharpens.\n", victim);
        if (ch != victim) {
            send_to_char("Ok.\n", ch);
        }
    }

    static void spell_dragons_breath(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        CHAR_DATA vch, vch_next;
        int dam, hp_dam, dice_dam;
        int hpch;


        act("You call the dragon lord to help you.", ch, null, null, TO_CHAR);
        act("$n start to breath like a dragon.", ch, null, victim, TO_NOTVICT);
        act("$n breath disturbs you!", ch, null, victim, TO_VICT);
        act("You breath the breath of lord of Dragons.", ch, null, null, TO_CHAR);

        hpch = UMAX(10, ch.hit);
        hp_dam = number_range(hpch / 9 + 1, hpch / 5);
        dice_dam = dice(level, 20);

        dam = UMAX(hp_dam + dice_dam / 5, dice_dam + hp_dam / 5);

        switch (dice(1, 5)) {
            case 1:
                fire_effect(victim.in_room, level, dam / 2, TARGET_ROOM);

                for (vch = victim.in_room.people; vch != null; vch = vch_next) {
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
                break;
            case 2:
                if (saves_spell(level, victim, DAM_ACID)) {
                    acid_effect(victim, level / 2, dam / 4, TARGET_CHAR);
                    damage(ch, victim, dam / 2, sn, DAM_ACID, true);
                } else {
                    acid_effect(victim, level, dam, TARGET_CHAR);
                    damage(ch, victim, dam, sn, DAM_ACID, true);
                }
                break;
            case 3:
                cold_effect(victim.in_room, level, dam / 2, TARGET_ROOM);

                for (vch = victim.in_room.people; vch != null; vch = vch_next) {
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
                break;
            case 4:
                poison_effect(ch.in_room, level, dam, TARGET_ROOM);

                TextBuffer buf = new TextBuffer();
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
                            buf.sprintf("Die, %s, you sorcerous dog!",
                                    (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(vch)) ? ch.doppel.name : ch.name);
                            do_yell(vch, buf);
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
                break;
            case 5:
                if (saves_spell(level, victim, DAM_LIGHTNING)) {
                    shock_effect(victim, level / 2, dam / 4, TARGET_CHAR);
                    damage(ch, victim, dam / 2, sn, DAM_LIGHTNING, true);
                } else {
                    shock_effect(victim, level, dam, TARGET_CHAR);
                    damage(ch, victim, dam, sn, DAM_LIGHTNING, true);
                }
                break;
        }
    }

    static void spell_sand_storm(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA vch, vch_next;
        int dam, hp_dam, dice_dam;
        int hpch;

        if ((ch.in_room.sector_type == SECT_AIR)
                || (ch.in_room.sector_type == SECT_WATER_SWIM)
                || (ch.in_room.sector_type == SECT_WATER_NOSWIM)) {
            send_to_char("You don't find any sand here to make storm.\n", ch);
            ch.wait = 0;
            return;
        }

        act("$n creates a storm with sands on the floor.", ch, null, null, TO_ROOM);
        act("You create the ..sand.. storm.", ch, null, null, TO_CHAR);

        hpch = UMAX(10, ch.hit);
        hp_dam = number_range(hpch / 9 + 1, hpch / 5);
        dice_dam = dice(level, 20);

        dam = UMAX(hp_dam + dice_dam / 10, dice_dam + hp_dam / 10);
        sand_effect(ch.in_room, level, dam / 2, TARGET_ROOM);

        for (vch = ch.in_room.people; vch != null; vch = vch_next) {
            vch_next = vch.next_in_room;

            if (is_safe_spell(ch, vch, true)
                    || (IS_NPC(vch) && IS_NPC(ch)
                    && (ch.fighting != vch /*|| vch.fighting != ch*/))) {
                continue;
            }
            if (is_safe(ch, vch)) {
                continue;
            }

            if (saves_spell(level, vch, DAM_COLD)) {
                sand_effect(vch, level / 2, dam / 4, TARGET_CHAR);
                damage(ch, vch, dam / 2, sn, DAM_COLD, true);
            } else {
                sand_effect(vch, level, dam, TARGET_CHAR);
                damage(ch, vch, dam, sn, DAM_COLD, true);
            }
        }
    }

    static void spell_scream(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA vch, vch_next;
        int dam, hp_dam, dice_dam;
        int hpch;

        act("$n screames with a disturbing NOISE!.", ch, null, null, TO_ROOM);
        act("You scream with a powerful sound.", ch, null, null, TO_CHAR);

        hpch = UMAX(10, ch.hit);
        hp_dam = number_range(hpch / 9 + 1, hpch / 5);
        dice_dam = dice(level, 20);
        dam = UMAX(hp_dam + dice_dam / 10, dice_dam + hp_dam / 10);

        scream_effect(ch.in_room, level, dam / 2, TARGET_ROOM);

        for (vch = ch.in_room.people; vch != null; vch = vch_next) {
            vch_next = vch.next_in_room;

            if (is_safe_spell(ch, vch, true)) {
                continue;
            }
            if (is_safe(ch, vch)) {
                continue;
            }

            if (saves_spell(level, vch, DAM_ENERGY)) {
                WAIT_STATE(vch, PULSE_VIOLENCE);
                scream_effect(vch, level / 2, dam / 4, TARGET_CHAR);
/*      damage(ch,vch,dam/2,sn,DAM_ENERGY,true);
         if (vch.fighting)  stop_fighting( vch , true ); */
            } else {
                WAIT_STATE(vch, (sn.beats + PULSE_VIOLENCE));
                scream_effect(vch, level, dam, TARGET_CHAR);
/*      damage(ch,vch,dam,sn,DAM_ENERGY,true); */
                if (vch.fighting != null) {
                    stop_fighting(vch, true);
                }
            }
        }
    }

    static void spell_attract_other(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (ch.sex == victim.sex) {
            send_to_char("You'd better try your chance on other sex!\n", ch);
            return;
        }
        spell_charm_person(sn, level, ch, vo);
    }


    static void spell_vampire(Skill sn, int level, CHAR_DATA ch) {

        if (is_affected(ch, sn)) {
            send_to_char("You can't be much more vampire!\n", ch);
            return;
        }
/* haste */
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = level / 2;
        af.location = APPLY_DEX;
        af.modifier = 1 + (level >= 18 ? 1 : 0) + (level >= 25 ? 1 : 0) + (level >= 32 ? 1 : 0);
        af.bitvector = AFF_HASTE;
        affect_to_char(ch, af);

/* giant strength */
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = level / 2;
        af.location = APPLY_STR;
        af.modifier = 1 + (level >= 18 ? 1 : 0) + (level >= 25 ? 1 : 0) + (level >= 32 ? 1 : 0);
        af.bitvector = 0;
        affect_to_char(ch, af);

/* cusse */
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = level / 2;
        af.location = APPLY_SIZE;
        af.modifier = 1 + (level >= 25 ? 1 : 0) + (level >= 50 ? 1 : 0) + (level >= 75 ? 1 : 0);
        af.bitvector = AFF_SNEAK;
        affect_to_char(ch, af);

/* damroll */
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = level / 2;
        af.location = APPLY_DAMROLL;
        af.modifier = ch.damroll;
        af.bitvector = AFF_BERSERK;
        affect_to_char(ch, af);

/* vampire flag */
        af.where = TO_ACT_FLAG;
        af.type = sn;
        af.level = level;
        af.duration = level / 2;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = PLR_VAMPIRE;
        affect_to_char(ch, af);

        send_to_char("You feel yourself getting greater and greater.\n", ch);
        act("You cannot recognize $n anymore.", ch, null, null, TO_ROOM);
    }


    static void spell_animate_dead(Skill sn, int level, CHAR_DATA ch, Object vo, int target) {
        CHAR_DATA victim;
        CHAR_DATA undead;
        OBJ_DATA obj, obj2, next;
        int i;

        /* deal with the object case first */
        if (target == TARGET_OBJ) {
            obj = (OBJ_DATA) vo;

            if (!(obj.item_type == ITEM_CORPSE_NPC ||
                    obj.item_type == ITEM_CORPSE_PC)) {
                send_to_char("You can animate only corpses!\n", ch);
                return;
            }
/*
     if (obj.item_type == ITEM_CORPSE_PC)
    {
    send_to_char("The magic fails abruptly!\n",ch);
    return;
    }
*/
            if (is_affected(ch, sn)) {
                send_to_char("You cannot summon the strength to handle more undead bodies.\n", ch);
                return;
            }

            if (count_charmed(ch) != 0) {
                return;
            }

            if (ch.in_room != null && IS_SET(ch.in_room.room_flags, ROOM_NO_MOB)) {
                send_to_char("You can't animate deads here.\n", ch);
                return;
            }

            if (IS_SET(ch.in_room.room_flags, ROOM_SAFE) ||
                    IS_SET(ch.in_room.room_flags, ROOM_PRIVATE) ||
                    IS_SET(ch.in_room.room_flags, ROOM_SOLITARY)) {
                send_to_char("You can't animate here.\n", ch);
                return;
            }

            undead = create_mobile(get_mob_index(MOB_VNUM_UNDEAD));
            char_to_room(undead, ch.in_room);
            for (i = 0; i < MAX_STATS; i++) {
                undead.perm_stat[i] = UMIN(25, 2 * ch.perm_stat[i]);
            }

            undead.max_hit = IS_NPC(ch) ? ch.max_hit : ch.pcdata.perm_hit;
            undead.hit = undead.max_hit;
            undead.max_mana = IS_NPC(ch) ? ch.max_mana : ch.pcdata.perm_mana;
            undead.mana = undead.max_mana;
            undead.alignment = ch.alignment;
            undead.level = UMIN(100, (ch.level - 2));

            for (i = 0; i < 3; i++) {
                undead.armor[i] = interpolate(undead.level, 100, -100);
            }
            undead.armor[3] = interpolate(undead.level, 50, -200);
            undead.damage[DICE_NUMBER] = number_range(level / 20, level / 15);
            undead.damage[DICE_TYPE] = number_range(level / 6, level / 3);
            undead.damage[DICE_BONUS] = number_range(level / 12, level / 10);
            undead.sex = ch.sex;
            undead.gold = 0;

            undead.act = SET_BIT(undead.act, ACT_UNDEAD);
            undead.affected_by = SET_BIT(undead.affected_by, AFF_CHARM);
            undead.master = ch;
            undead.leader = ch;

            TextBuffer buf = new TextBuffer();
            buf.sprintf("%s body undead", obj.name);
            undead.name = buf.toString();
            String argument = obj.short_descr;
            StringBuilder argb = new StringBuilder();
            StringBuilder buf3 = new StringBuilder();
            while (argument.length() != 0) {
                argb.setLength(0);
                argument = one_argument(argument, argb);
                String arg = argb.toString();
                if (!(!str_cmp(arg, "The") || !str_cmp(arg, "undead") || !str_cmp(arg, "body") ||
                        !str_cmp(arg, "corpse") || !str_cmp(arg, "of"))) {
                    if (buf3.length() == 0) {
                        buf3.append(arg);
                    } else {
                        buf3.append(" ");
                        buf3.append(arg);
                    }
                }
            }
            buf.sprintf("The undead body of %s", buf3);
            undead.short_descr = buf.toString();
            buf.sprintf("The undead body of %s slowly staggers around.\n", buf3);
            undead.long_descr = buf.toString();

            for (obj2 = obj.contains; obj2 != null; obj2 = next) {
                next = obj2.next_content;
                obj_from_obj(obj2);
                obj_to_char(obj2, undead);
            }
            interpret(undead, "wear all", true);

            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = sn;
            af.level = ch.level;
            af.duration = (ch.level / 10);
            af.modifier = 0;
            af.bitvector = 0;
            af.location = APPLY_NONE;
            affect_to_char(ch, af);

            send_to_char("With mystic power, you animate it!\n", ch);
            buf.sprintf("With mystic power, %s animates %s!", ch.name, obj.name);
            act(buf, ch, null, null, TO_ROOM);
            buf.sprintf("%s looks at you and plans to make you pay for distrurbing its rest!", obj.short_descr);
            act(buf, ch, null, null, TO_CHAR);
            extract_obj(obj);
            return;
        }

        victim = (CHAR_DATA) vo;

        if (victim == ch) {
            send_to_char("But you aren't dead!!\n", ch);
            return;
        }

        send_to_char("But it ain't dead!!\n", ch);
    }


    static void spell_enhanced_armor(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(victim, sn)) {
            if (victim == ch) {
                send_to_char("You are already enhancedly armored.\n", ch);
            } else {
                act("$N is already enhancedly armored.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.modifier = -60;
        af.location = APPLY_AC;
        af.bitvector = 0;
        affect_to_char(victim, af);
        send_to_char("You feel protected for all attacks.\n", victim);
        if (ch != victim) {
            act("$N is protected by your magic.", ch, null, victim, TO_CHAR);
        }
    }


    static void spell_meld_into_stone(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(victim, sn)) {
            if (victim == ch) {
                send_to_char("Your skin is already covered with stone.\n",
                        ch);
            } else {
                act("$N's skin is already covered with stone.", ch, null,
                        victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = level / 10;
        af.location = APPLY_AC;
        af.modifier = -100;
        af.bitvector = 0;
        affect_to_char(victim, af);
        act("$n's skin melds into stone.", victim, null, null, TO_ROOM);
        send_to_char("Your skin melds into stone.\n", victim);
    }

    static void spell_web(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (saves_spell(level, victim, DAM_OTHER)) {
            return;
        }

        if (is_affected(victim, sn)) {
            if (victim == ch) {
                send_to_char("You are already webbed.\n", ch);
            } else {
                act("$N is already webbed.", ch, null, victim, TO_CHAR);
            }
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.type = sn;
        af.level = level;
        af.duration = 1;
        af.location = APPLY_HITROLL;
        af.modifier = -1 * (level / 6);
        af.where = TO_AFFECTS;
        af.bitvector = AFF_WEB;
        affect_to_char(victim, af);

        af.location = APPLY_DEX;
        af.modifier = -2;
        affect_to_char(victim, af);

        af.location = APPLY_DAMROLL;
        af.modifier = -1 * (level / 6);
        affect_to_char(victim, af);
        send_to_char("You are emeshed in thick webs!\n", victim);
        if (ch != victim) {
            act("You emesh $N in a bundle of webs!", ch, null, victim, TO_CHAR);
        }
    }


    static void spell_group_defense(int level, CHAR_DATA ch) {
        CHAR_DATA gch;

        Skill shield_sn = Skill.gsn_shield;
        Skill armor_sn = Skill.gsn_armor;

        for (gch = ch.in_room.people; gch != null; gch = gch.next_in_room) {
            if (!is_same_group(gch, ch)) {
                continue;
            }
            if (is_affected(gch, armor_sn)) {
                if (gch == ch) {
                    send_to_char("You are already armored.\n", ch);
                } else {
                    act("$N is already armored.", ch, null, gch, TO_CHAR);
                }
                continue;
            }

            AFFECT_DATA af = new AFFECT_DATA();
            af.type = armor_sn;
            af.level = level;
            af.duration = level;
            af.location = APPLY_AC;
            af.modifier = -20;
            af.bitvector = 0;
            affect_to_char(gch, af);

            send_to_char("You feel someone protecting you.\n", gch);
            if (ch != gch) {
                act("$N is protected by your magic.",
                        ch, null, gch, TO_CHAR);
            }

            if (!is_same_group(gch, ch)) {
                continue;
            }
            if (is_affected(gch, shield_sn)) {
                if (gch == ch) {
                    send_to_char("You are already shielded.\n", ch);
                } else {
                    act("$N is already shielded.", ch, null, gch, TO_CHAR);
                }
                continue;
            }

            af.type = shield_sn;
            af.level = level;
            af.duration = level;
            af.location = APPLY_AC;
            af.modifier = -20;
            af.bitvector = 0;
            affect_to_char(gch, af);

            send_to_char("You are surrounded by a force shield.\n", gch);
            if (ch != gch) {
                act("$N is surrounded by a force shield.",
                        ch, null, gch, TO_CHAR);
            }

        }
    }


    static void spell_inspire(int level, CHAR_DATA ch) {
        CHAR_DATA gch;

        Skill bless_sn = Skill.gsn_bless;

        for (gch = ch.in_room.people; gch != null; gch = gch.next_in_room) {
            if (!is_same_group(gch, ch)) {
                continue;
            }
            if (is_affected(gch, bless_sn)) {
                if (gch == ch) {
                    send_to_char("You are already inspired.\n", ch);
                } else {
                    act("$N is already inspired.",
                            ch, null, gch, TO_CHAR);
                }
                continue;
            }
            AFFECT_DATA af = new AFFECT_DATA();
            af.type = bless_sn;
            af.level = level;
            af.duration = 6 + level;
            af.location = APPLY_HITROLL;
            af.modifier = level / 12;
            af.bitvector = 0;
            affect_to_char(gch, af);

            af.location = APPLY_SAVING_SPELL;
            af.modifier = 0 - level / 12;
            affect_to_char(gch, af);

            send_to_char("You feel inspired!\n", gch);
            if (ch != gch) {
                act("You inspire $N with the Creator's power!",
                        ch, null, gch, TO_CHAR);
            }

        }
    }


    static void spell_mass_sanctuary(int level, CHAR_DATA ch) {
        CHAR_DATA gch;

        Skill sanc_sn = Skill.gsn_sanctuary;

        for (gch = ch.in_room.people; gch != null; gch = gch.next_in_room) {
            if (!is_same_group(gch, ch)) {
                continue;
            }
            if (IS_AFFECTED(gch, AFF_SANCTUARY)) {
                if (gch == ch) {
                    send_to_char("You are already in sanctuary.\n", ch);
                } else {
                    act("$N is already in sanctuary.", ch, null, gch, TO_CHAR);
                }
                continue;
            }
            AFFECT_DATA af = new AFFECT_DATA();
            af.type = sanc_sn;
            af.level = level;
            af.duration = number_fuzzy(level / 6);
            af.location = APPLY_NONE;
            af.modifier = 0;
            af.bitvector = AFF_SANCTUARY;
            af.where = TO_AFFECTS;
            affect_to_char(gch, af);

            send_to_char("You are surrounded by a white aura.\n", gch);
            if (ch != gch) {
                act("$N is surrounded by a white aura.",
                        ch, null, gch, TO_CHAR);
            }
        }
    }

    static void spell_mend(CHAR_DATA ch, Object vo) {
        OBJ_DATA obj = (OBJ_DATA) vo;
        int result, skill;

        if (obj.condition > 99) {
            send_to_char("That item is not in need of mending.\n", ch);
            return;
        }

        if (obj.wear_loc != -1) {
            send_to_char("The item must be carried to be mended.\n", ch);
            return;
        }

        skill = get_skill(ch, gsn_mend) / 2;
        result = number_percent() + skill;

        if (IS_OBJ_STAT(obj, ITEM_GLOW)) {
            result -= 5;
        }
        if (IS_OBJ_STAT(obj, ITEM_MAGIC)) {
            result += 5;
        }

        if (result >= 50) {
            act("$p glows brightly, and is whole again.  Good Job!", ch, obj, null, TO_CHAR);
            act("$p glows brightly, and is whole again.", ch, obj, null, TO_ROOM);
            obj.condition += result;
            obj.condition = UMIN(obj.condition, 100);
        } else if (result >= 10) {
            send_to_char("Nothing seemed to happen.\n", ch);
        } else {
            act("$p flares blindingly... and evaporates!", ch, obj, null, TO_CHAR);
            act("$p flares blindingly... and evaporates!", ch, obj, null, TO_ROOM);
            extract_obj(obj);
        }
    }

    static void spell_shielding(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (saves_spell(level, victim, DAM_NONE)) {
            act("$N shivers slightly, but it passes quickly.",
                    ch, null, victim, TO_CHAR);
            send_to_char("You shiver slightly, but it passes quickly.\n", victim);
            return;
        }

        if (is_affected(victim, sn)) {
            AFFECT_DATA af = new AFFECT_DATA();
            af.type = sn;
            af.level = level;
            af.duration = level / 20;
            af.location = APPLY_NONE;
            af.modifier = 0;
            af.bitvector = 0;
            affect_to_char(victim, af);
            act("You wrap $N in more flows of Spirit.", ch, null, victim, TO_CHAR);
            send_to_char("You feel the shielding get stronger.\n", victim);
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.type = sn;
        af.level = level;
        af.duration = level / 15;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = 0;
        affect_join(victim, af);

        send_to_char("You feel as if you have lost touch with something.\n", victim);
        act("You shield $N from the true Source.", ch, null, victim, TO_CHAR);
    }


    static void spell_link(CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int random, tmpmana;

        random = number_percent();
        tmpmana = ch.mana;
        ch.mana = 0;
        ch.endur /= 2;
        tmpmana = (int) (0.5 * tmpmana);
        tmpmana = ((tmpmana + random) / 2);
        victim.mana = victim.mana + tmpmana;
    }

    static void spell_power_kill(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;


        act("{rA stream of darkness from your finger surrounds $N.{x", ch, null, victim, TO_CHAR, POS_RESTING);
        act("{rA stream of darkness from $n's finger surrounds $N.{x", ch, null, victim, TO_NOTVICT, POS_RESTING);
        act("{rA stream of darkness from $N's finger surrounds you.{x", victim, null, ch, TO_CHAR, POS_RESTING);

        if (saves_spell(level, victim, DAM_MENTAL) || number_bits(1) == 0) {
            dam = dice(level, 24);
            damage(ch, victim, dam, sn, DAM_MENTAL, true);
            return;
        }

        send_to_char("You have been KILLED!\n", victim);

        act("$N has been killed!\n", ch, null, victim, TO_CHAR);
        act("$N has been killed!\n", ch, null, victim, TO_ROOM);

        raw_kill(victim);
    }

    static void spell_eyed_sword(CHAR_DATA ch) {
        OBJ_DATA eyed;
        int i;
/*
    if (IS_SET(ch.quest,QUEST_EYE)
    {
     send_to_char("You created your sword ,before.\n",ch);
     return;
    }
ch.quest=    SET_BIT(ch.quest,QUEST_EYE);
*/
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

        buf.sprintf(eyed.description, ch.name);
        eyed.description = buf.toString();

        buf.sprintf(eyed.pIndexData.extra_descr.description, ch.name);
        eyed.extra_descr = new EXTRA_DESCR_DATA();
        eyed.extra_descr.keyword = eyed.pIndexData.extra_descr.keyword;
        eyed.extra_descr.description = buf.toString();
        eyed.extra_descr.next = null;

        eyed.value[2] = (ch.level / 10) + 3;
        eyed.level = ch.level;
        eyed.cost = 0;
        obj_to_char(eyed, ch);
        send_to_char("You create YOUR sword with your name.\n", ch);
        send_to_char("Don't forget that you won't be able to create this weapon anymore.\n", ch);
    }

    static void spell_lion_help(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA lion;
        CHAR_DATA victim;
        int i;
        StringBuilder arg = new StringBuilder();
        target_name = one_argument(target_name, arg);
        if (arg.length() == 0) {
            send_to_char("Whom do you want to have killed.\n", ch);
            return;
        }

        if ((victim = get_char_area(ch, arg.toString())) == null) {
            send_to_char("Noone around with that name.\n", ch);
            return;
        }
        if (is_safe_nomessage(ch, victim)) {
            send_to_char("God protects your victim.\n", ch);
            return;
        }

        send_to_char("You call for a hunter lion.\n", ch);
        act("$n shouts a hunter lion.", ch, null, null, TO_ROOM);

        if (is_affected(ch, sn)) {
            send_to_char("You cannot summon the strength to handle more lion right now.\n", ch);
            return;
        }

        if (ch.in_room != null && IS_SET(ch.in_room.room_flags, ROOM_NO_MOB)) {
            send_to_char("No lions can listen you.\n", ch);
            return;
        }

        if (IS_SET(ch.in_room.room_flags, ROOM_SAFE) ||
                IS_SET(ch.in_room.room_flags, ROOM_PRIVATE) ||
                IS_SET(ch.in_room.room_flags, ROOM_SOLITARY) ||
                (ch.in_room.exit[0] == null &&
                        ch.in_room.exit[1] == null &&
                        ch.in_room.exit[2] == null &&
                        ch.in_room.exit[3] == null &&
                        ch.in_room.exit[4] == null &&
                        ch.in_room.exit[5] == null) ||
                (ch.in_room.sector_type != SECT_FIELD &&
                        ch.in_room.sector_type != SECT_FOREST &&
                        ch.in_room.sector_type != SECT_MOUNTAIN &&
                        ch.in_room.sector_type != SECT_HILLS)) {
            send_to_char("No hunter lion can come to you.\n", ch);
            return;
        }

        lion = create_mobile(get_mob_index(MOB_VNUM_HUNTER));

        for (i = 0; i < MAX_STATS; i++) {
            lion.perm_stat[i] = UMIN(25, 2 * ch.perm_stat[i]);
        }

        lion.max_hit = UMIN(30000, (int) (ch.max_hit * 1.2));
        lion.hit = lion.max_hit;
        lion.max_mana = ch.max_mana;
        lion.mana = lion.max_mana;
        lion.alignment = ch.alignment;
        lion.level = UMIN(100, ch.level);
        for (i = 0; i < 3; i++) {
            lion.armor[i] = interpolate(lion.level, 100, -100);
        }
        lion.armor[3] = interpolate(lion.level, 100, 0);
        lion.sex = ch.sex;
        lion.gold = 0;
        lion.damage[DICE_NUMBER] = number_range(level / 15, level / 10);
        lion.damage[DICE_TYPE] = number_range(level / 3, level / 2);
        lion.damage[DICE_BONUS] = number_range(level / 8, level / 6);
        lion.affected_by = lion.affected_by | (A | B | C | D | E | F | G | H | BIT_30);

/*   SET_BIT(lion.affected_by, AFF_CHARM);
  lion.master = lion.leader = ch; */

        char_to_room(lion, ch.in_room);

        send_to_char("A hunter lion comes to kill your victim!\n", ch);
        act("A hunter lion comes to kill $n's victim!", ch, null, null, TO_ROOM);
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = ch.level;
        af.duration = 24;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);
        lion.act = SET_BIT(lion.act, ACT_HUNTER);
        lion.hunting = victim;
        hunt_victim(lion);

    }


    static void spell_magic_jar(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        OBJ_DATA vial;
        OBJ_DATA fire;
        int i;

        if (victim == ch) {
            send_to_char("You like yourself even better.\n", ch);
            return;
        }

        if (IS_NPC(victim)) {
            send_to_char("Your victim is a npc. Not necessary!.\n", ch);
            return;
        }

        if (saves_spell(level, victim, DAM_MENTAL)) {
            send_to_char("You failed.\n", ch);
            return;
        }

        for (vial = ch.carrying; vial != null; vial = vial.next_content) {
            if (vial.pIndexData.vnum == OBJ_VNUM_POTION_VIAL) {
                break;
            }
        }

        if (vial == null) {
            send_to_char("You don't have any vials to put your victim's spirit.\n"
                    , ch);
            return;
        }
        extract_obj(vial);
        if (IS_GOOD(ch)) {
            i = 0;
        } else if (IS_EVIL(ch)) {
            i = 2;
        } else {
            i = 1;
        }

        fire = create_object(get_obj_index(OBJ_VNUM_MAGIC_JAR), 0);
        fire.owner = ch.name;
        fire.from = ch.name;
        fire.altar = hometown_table[ch.hometown].altar[i];
        fire.pit = hometown_table[ch.hometown].pit[i];
        fire.level = ch.level;

        TextBuffer buf = new TextBuffer();
        buf.sprintf(fire.name, victim.name);
        fire.name = buf.toString();

        buf.sprintf(fire.short_descr, victim.name);
        fire.short_descr = buf.toString();

        buf.sprintf(fire.description, victim.name);
        fire.description = buf.toString();

        buf.sprintf(fire.pIndexData.extra_descr.description, victim.name);
        fire.extra_descr = new EXTRA_DESCR_DATA();
        fire.extra_descr.keyword = fire.pIndexData.extra_descr.keyword;
        fire.extra_descr.description = buf.toString();
        fire.extra_descr.next = null;

        fire.level = ch.level;
        fire.timer = ch.level;
        fire.cost = 0;
        obj_to_char(fire, ch);
        victim.act = SET_BIT(victim.act, PLR_NO_EXP);
        buf.sprintf("You catch %s's spirit in to your vial.\n", victim.name);
        send_to_char(buf, ch);
    }

    static void turn_spell(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam, align;

        if (victim != ch) {
            act("$n raises $s hand, and a blinding ray of light shoots forth!",
                    ch, null, null, TO_ROOM);
            send_to_char(
                    "You raise your hand and a blinding ray of light shoots forth!\n",
                    ch);
        }

        if (IS_GOOD(victim) || IS_NEUTRAL(victim)) {
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

        /* cabal guardians */
        if (IS_NPC(victim) && victim.cabal != CABAL_NONE) {
            return;
        }

        if ((IS_NPC(victim) && victim.position != POS_DEAD) ||
                (!IS_NPC(victim) && (current_time - victim.last_death_time) > 10)) {
            ROOM_INDEX_DATA was_in;
            ROOM_INDEX_DATA now_in;
            int door;

            was_in = victim.in_room;
            for (door = 0; door < 6; door++) {
                EXIT_DATA pexit;

                if ((pexit = was_in.exit[door]) == null
                        || pexit.to_room == null
                        || IS_SET(pexit.exit_info, EX_CLOSED)
                        || (IS_NPC(ch)
                        && IS_SET(pexit.to_room.room_flags, ROOM_NO_MOB))) {
                    continue;
                }

                move_char(victim, door);
                if ((now_in = victim.in_room) == was_in) {
                    continue;
                }

                victim.in_room = was_in;
                act("$n has fled!", victim, null, null, TO_ROOM);
                victim.in_room = now_in;

                if (IS_NPC(victim)) {
                    victim.last_fought = null;
                }

                stop_fighting(victim, true);
                return;
            }
        }
    }

    static void spell_turn(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA vch;
        CHAR_DATA vch_next;


        if (is_affected(ch, sn)) {
            send_to_char("This power is used too recently.", ch);
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 5;
        af.modifier = 0;
        af.location = 0;
        af.bitvector = 0;
        affect_to_char(ch, af);

        if (IS_EVIL(ch)) {
            send_to_char("The energy explodes inside you!\n", ch);
            turn_spell(sn, ch.level, ch, ch);
            return;
        }

        for (vch = ch.in_room.people; vch != null; vch = vch_next) {
            vch_next = vch.next_in_room;

            if (is_safe_spell(ch, vch, true)) {
                continue;
            }
            if (is_safe(ch, vch)) {
                continue;
            }
            turn_spell(sn, ch.level, ch, vch);
        }
    }


    static void spell_fear(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if ((victim.clazz == Clazz.SAMURAI) && (victim.level >= 10)) {
            send_to_char("Your victim is beyond this power.\n", ch);
            return;
        }

        if (is_affected(victim, gsn_fear) || saves_spell(level, victim, DAM_OTHER)) {
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = gsn_fear;
        af.level = level;
        af.duration = level / 10;
        af.location = 0;
        af.modifier = 0;
        af.bitvector = AFF_FEAR;
        affect_to_char(victim, af);
        send_to_char("You are afraid as much as a rabbit.\n", victim);
        act("$n looks with afraid eyes.", victim, null, null, TO_ROOM);
    }

    static void spell_protection_heat(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(victim, gsn_protection_heat)) {
            if (victim == ch) {
                send_to_char("You are already protected from heat.\n", ch);
            } else {
                act("$N is already protected from heat.", ch, null, victim, TO_CHAR);
            }
            return;
        }

        if (is_affected(victim, gsn_protection_cold)) {
            if (victim == ch) {
                send_to_char("You are already protected from cold.\n", ch);
            } else {
                act("$N is already protected from cold.", ch, null, victim, TO_CHAR);
            }
            return;
        }

        if (is_affected(victim, gsn_fire_shield)) {
            if (victim == ch) {
                send_to_char("You are already using fire shield.\n", ch);
            } else {
                act("$N is already using fire shield.", ch, null, victim, TO_CHAR);
            }
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = gsn_protection_heat;
        af.level = level;
        af.duration = 24;
        af.location = APPLY_SAVING_SPELL;
        af.modifier = -1;
        af.bitvector = 0;
        affect_to_char(victim, af);
        send_to_char("You feel strengthed against heat.\n", victim);
        if (ch != victim) {
            act("$N is protected against heat.", ch, null, victim, TO_CHAR);
        }
    }

    static void spell_protection_cold(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(victim, gsn_protection_cold)) {
            if (victim == ch) {
                send_to_char("You are already protected from cold.\n", ch);
            } else {
                act("$N is already protected from cold.", ch, null, victim, TO_CHAR);
            }
            return;
        }

        if (is_affected(victim, gsn_protection_heat)) {
            if (victim == ch) {
                send_to_char("You are already protected from heat.\n", ch);
            } else {
                act("$N is already protected from heat.", ch, null, victim, TO_CHAR);
            }
            return;
        }

        if (is_affected(victim, gsn_fire_shield)) {
            if (victim == ch) {
                send_to_char("You are already using fire shield.\n", ch);
            } else {
                act("$N is already using fire shield.", ch, null, victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = gsn_protection_cold;
        af.level = level;
        af.duration = 24;
        af.location = APPLY_SAVING_SPELL;
        af.modifier = -1;
        af.bitvector = 0;
        affect_to_char(victim, af);
        send_to_char("You feel strengthed against cold.\n", victim);
        if (ch != victim) {
            act("$N is protected against cold.", ch, null, victim, TO_CHAR);
        }
    }

    static void spell_fire_shield(CHAR_DATA ch) {
        OBJ_DATA fire;
        int i;

        StringBuilder arg = new StringBuilder();
        target_name = one_argument(target_name, arg);
        if (!(!str_cmp(arg.toString(), "cold") || !str_cmp(arg.toString(), "fire"))) {
            send_to_char("You must specify the type.\n", ch);
            return;
        }
        if (IS_GOOD(ch)) {
            i = 0;
        } else if (IS_EVIL(ch)) {
            i = 2;
        } else {
            i = 1;
        }

        fire = create_object(get_obj_index(OBJ_VNUM_FIRE_SHIELD), 0);
        fire.owner = ch.name;
        fire.from = ch.name;
        fire.altar = hometown_table[ch.hometown].altar[i];
        fire.pit = hometown_table[ch.hometown].pit[i];
        fire.level = ch.level;

        TextBuffer buf = new TextBuffer();
        buf.sprintf(fire.short_descr, arg);
        fire.short_descr = buf.toString();

        buf.sprintf(fire.description, arg);
        fire.description = buf.toString();

        buf.sprintf(fire.pIndexData.extra_descr.description, arg);
        fire.extra_descr = new EXTRA_DESCR_DATA();
        fire.extra_descr.keyword = fire.pIndexData.extra_descr.keyword;
        fire.extra_descr.description = buf.toString();
        fire.extra_descr.next = null;

        fire.level = ch.level;
        fire.cost = 0;
        fire.timer = 5 * ch.level;
        if (IS_GOOD(ch)) {
            fire.extra_flags = SET_BIT(fire.extra_flags, (ITEM_ANTI_NEUTRAL | ITEM_ANTI_EVIL));
        } else if (IS_NEUTRAL(ch)) {
            fire.extra_flags = SET_BIT(fire.extra_flags, (ITEM_ANTI_GOOD | ITEM_ANTI_EVIL));
        } else if (IS_EVIL(ch)) {
            fire.extra_flags = SET_BIT(fire.extra_flags, (ITEM_ANTI_NEUTRAL | ITEM_ANTI_GOOD));
        }
        obj_to_char(fire, ch);
        send_to_char("You create the fire shield.\n", ch);
    }

    static void spell_witch_curse(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(victim, gsn_witch_curse)) {
            send_to_char("It has already underflowing with health.\n", ch);
            return;
        }

        if (saves_spell((level + 5), victim, DAM_MENTAL) || number_bits(1) == 0) {
            send_to_char("You failed!\n", ch);
            return;
        }

        ch.hit -= (2 * level);
        ch.hit = UMAX(ch.hit, 1);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = gsn_witch_curse;
        af.level = level;
        af.duration = 24;
        af.location = APPLY_HIT;
        af.modifier = -level;
        af.bitvector = 0;
        affect_to_char(victim, af);


        send_to_char("Now he got the path to death.\n", ch);
    }

    private static final int rev_dir[] = {2, 3, 0, 1, 5, 4};

    static void spell_knock(Skill sn, CHAR_DATA ch) {
        int chance;
        int door;

        StringBuilder arg = new StringBuilder();
        target_name = one_argument(target_name, arg);

        if (arg.length() == 0) {
            send_to_char("Knock which door or direction.\n", ch);
            return;
        }

        if (ch.fighting != null) {
            send_to_char("Wait until the fight finishes.\n", ch);
            return;
        }

        if ((door = find_door(ch, arg.toString())) >= 0) {
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
            chance = ch.level / 5 + get_curr_stat(ch, STAT_INT) + get_skill(ch, sn) / 5;

            act("You knock $d, and try to open $d!",
                    ch, null, pexit.keyword, TO_CHAR);
            act("You knock $d, and try to open $d!",
                    ch, null, pexit.keyword, TO_ROOM);

            if (room_dark(ch.in_room)) {
                chance /= 2;
            }

            /* now the attack */
            if (number_percent() < chance) {
                pexit.exit_info = REMOVE_BIT(pexit.exit_info, EX_LOCKED);
                pexit.exit_info = REMOVE_BIT(pexit.exit_info, EX_CLOSED);
                act("$n knocks the the $d and opens the lock.", ch, null,
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
            } else {
                act("You couldn't knock the $d!",
                        ch, null, pexit.keyword, TO_CHAR);
                act("$n failed to knock $d.",
                        ch, null, pexit.keyword, TO_ROOM);
            }
            return;
        }

        send_to_char("You can't see that here.\n", ch);
    }


    static void spell_magic_resistance(Skill sn, int level, CHAR_DATA ch) {

        if (!is_affected(ch, sn)) {
            send_to_char("You are now resistive to magic.\n", ch);

            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_RESIST;
            af.type = sn;
            af.duration = level / 10;
            af.level = ch.level;
            af.bitvector = RES_MAGIC;
            af.location = 0;
            af.modifier = 0;
            affect_to_char(ch, af);
        } else {
            send_to_char("You are already resistive to magic.\n", ch);
        }
    }

    static void spell_hallucination(CHAR_DATA ch) {
        send_to_char("That spell is under construction.\n", ch);
    }

    static void spell_wolf(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA gch;
        CHAR_DATA demon;
        int i;

        if (is_affected(ch, sn)) {
            send_to_char("You lack the power to summon another wolf right now.\n", ch);
            return;
        }

        send_to_char("You attempt to summon a wolf.\n", ch);
        act("$n attempts to summon a wolf.", ch, null, null, TO_ROOM);

        for (gch = char_list; gch != null; gch = gch.next) {
            if (IS_NPC(gch) && IS_AFFECTED(gch, AFF_CHARM) && gch.master == ch &&
                    gch.pIndexData.vnum == MOB_VNUM_WOLF) {
                send_to_char("Two wolfs are more than you can control!\n", ch);
                return;
            }
        }

        if (count_charmed(ch) != 0) {
            return;
        }

        demon = create_mobile(get_mob_index(MOB_VNUM_WOLF));

        for (i = 0; i < MAX_STATS; i++) {
            demon.perm_stat[i] = ch.perm_stat[i];
        }

        demon.max_hit = IS_NPC(ch) ? URANGE(ch.max_hit, ch.max_hit, 30000)
                : URANGE(ch.pcdata.perm_hit, ch.hit, 30000);
        demon.hit = demon.max_hit;
        demon.max_mana = IS_NPC(ch) ? ch.max_mana : ch.pcdata.perm_mana;
        demon.mana = demon.max_mana;
        demon.level = ch.level;
        for (i = 0; i < 3; i++) {
            demon.armor[i] = interpolate(demon.level, 100, -100);
        }
        demon.armor[3] = interpolate(demon.level, 100, 0);
        demon.gold = 0;
        demon.timer = 0;
        demon.damage[DICE_NUMBER] = number_range(level / 15, level / 10);
        demon.damage[DICE_TYPE] = number_range(level / 3, level / 2);
        demon.damage[DICE_BONUS] = number_range(level / 6, level / 5);

        char_to_room(demon, ch.in_room);
        send_to_char("The wolf arrives and bows before you!\n", ch);
        act("A wolf arrives from somewhere and bows!", ch, null, null, TO_ROOM);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

        demon.affected_by = SET_BIT(demon.affected_by, AFF_CHARM);
        demon.master = demon.leader = ch;
    }

    static void spell_vam_blast(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 12);
        if (saves_spell(level, victim, DAM_ACID)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_ACID, true);
    }

    static void spell_dragon_skin(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(victim, sn)) {
            if (victim == ch) {
                send_to_char("Your skin is already hard as rock.\n",
                        ch);
            } else {
                act("$N's skin is already hard as rock.", ch, null,
                        victim, TO_CHAR);
            }
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = level;
        af.location = APPLY_AC;
        af.modifier = -(2 * level);
        af.bitvector = 0;
        affect_to_char(victim, af);
        act("$n's skin is now hard as rock.", victim, null, null, TO_ROOM);
        send_to_char("Your skin is now hard as rock.\n", victim);
    }


    static void spell_mind_light(Skill sn, int level, CHAR_DATA ch) {

        if (is_affected_room(ch.in_room, sn)) {
            send_to_char("This room has already had booster of mana.\n", ch);
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_ROOM_CONST;
        af.type = sn;
        af.level = level;
        af.duration = level / 30;
        af.location = APPLY_ROOM_MANA;
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
        send_to_char("The room starts to be filled with mind light.\n", ch);
        act("The room starts to be filled with $n's mind light.", ch, null, null, TO_ROOM);
    }

    static void spell_insanity(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_NPC(ch)) {
            send_to_char("This spell can cast on PC's only.\n", ch);
            return;
        }

        if (IS_AFFECTED(victim, AFF_BLOODTHIRST) || saves_spell(level, victim, DAM_OTHER)) {
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = level / 10;
        af.location = 0;
        af.modifier = 0;
        af.bitvector = AFF_BLOODTHIRST;
        affect_to_char(victim, af);
        send_to_char("You are as aggressive as a battlerager.\n", victim);
        act("$n looks with red eyes.", victim, null, null, TO_ROOM);
    }


    static void spell_power_stun(Skill sn, int level, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;


        if (is_affected(victim, sn) || saves_spell(level, victim, DAM_OTHER)) {
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = level / 90;
        af.location = APPLY_DEX;
        af.modifier = -3;
        af.bitvector = AFF_STUN;
        affect_to_char(victim, af);
        send_to_char("You are stunned.\n", victim);
        act("{r$n is stunned.{x", victim, null, null, TO_ROOM, POS_SLEEPING);
    }


    static void spell_improved_invis(Skill sn, int level, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_IMP_INVIS)) {
            return;
        }

        act("$n fades out of existence.", victim, null, null, TO_ROOM);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = level / 10;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_IMP_INVIS;
        affect_to_char(victim, af);
        send_to_char("You fade out of existence.\n", victim);
    }


    static void spell_improved_detection(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_DETECT_IMP_INVIS)) {
            if (victim == ch) {
                send_to_char("You can already see improved invisible.\n", ch);
            } else {
                act("$N can already see improved invisible mobiles.", ch, null, victim, TO_CHAR);
            }
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = level / 3;
        af.modifier = 0;
        af.location = APPLY_NONE;
        af.bitvector = AFF_DETECT_IMP_INVIS;
        affect_to_char(victim, af);
        send_to_char("Your eyes tingle.\n", victim);
        if (ch != victim) {
            send_to_char("Ok.\n", ch);
        }
    }

    static void spell_severity_force(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;
        TextBuffer buf = new TextBuffer();
        buf.sprintf("You cracked the ground towards the %s.\n", victim.name);
        send_to_char(buf, ch);
        act("$n cracked the ground towards you!.", ch, null, victim, TO_VICT);
        if (IS_AFFECTED(victim, AFF_FLYING)) {
            dam = 0;
        } else {
            dam = dice(level, 12);
        }
        damage(ch, victim, dam, sn, DAM_BASH, true);
    }

    static void spell_randomizer(Skill sn, int level, CHAR_DATA ch) {

        if (is_affected(ch, sn)) {
            send_to_char
                    ("Your power of randomness has been exhausted for now.\n",
                            ch);
            return;
        }

        if (IS_SET(ch.in_room.room_flags, ROOM_LAW)) {
            send_to_char(
                    "This room is far too orderly for your powers to work on it.\n",
                    ch);
            return;
        }
        if (is_affected_room(ch.in_room, sn)) {
            send_to_char("This room has already been randomized.\n", ch);
            return;
        }

        if (number_bits(1) == 0) {
            send_to_char("Despite your efforts, the universe resisted chaos.\n", ch);
            AFFECT_DATA af2 = new AFFECT_DATA();
            af2.where = TO_AFFECTS;
            af2.type = sn;
            af2.level = ch.level;
            af2.duration = level / 10;
            af2.modifier = 0;
            af2.location = APPLY_NONE;
            af2.bitvector = 0;
            affect_to_char(ch, af2);
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_ROOM_AFFECTS;
        af.type = sn;
        af.level = ch.level;
        af.duration = level / 15;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_ROOM_RANDOMIZER;
        affect_to_room(ch.in_room, af);

        AFFECT_DATA af2 = new AFFECT_DATA();
        af2.where = TO_AFFECTS;
        af2.type = sn;
        af2.level = ch.level;
        af2.duration = level / 5;
        af2.modifier = 0;
        af2.location = APPLY_NONE;
        af2.bitvector = 0;
        affect_to_char(ch, af2);
        send_to_char("The room was successfully randomized!\n", ch);
        send_to_char("You feel very drained from the effort.\n", ch);
        ch.hit -= UMIN(200, ch.hit / 2);
        act("The room starts to randomize exits.", ch, null, null, TO_ROOM);
    }

    static void spell_bless_weapon(Skill sn, int level, CHAR_DATA ch, Object vo) {
        OBJ_DATA obj = (OBJ_DATA) vo;

        if (obj.item_type != ITEM_WEAPON) {
            send_to_char("That isn't a weapon.\n", ch);
            return;
        }

        if (obj.wear_loc != -1) {
            send_to_char("The item must be carried to be blessed.\n", ch);
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
                    || IS_OBJ_STAT(obj, ITEM_BLESS)
                    || IS_OBJ_STAT(obj, ITEM_BURN_PROOF)) {
                act("You can't seem to bless $p.", ch, obj, null, TO_CHAR);
                return;
            }
        }
        if (IS_WEAPON_STAT(obj, WEAPON_HOLY)) {
            act("$p is already blessed for holy attacks.", ch, obj, null, TO_CHAR);
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_WEAPON;
        af.type = sn;
        af.level = level / 2;
        af.duration = level / 8;
        af.location = 0;
        af.modifier = 0;
        af.bitvector = WEAPON_HOLY;
        affect_to_obj(obj, af);

        act("$p is prepared for holy attacks.", ch, obj, null, TO_ALL);

    }

    static void spell_resilience(Skill sn, int level, CHAR_DATA ch) {

        if (!is_affected(ch, sn)) {
            send_to_char("You are now resistive to draining attacks.\n", ch);

            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_RESIST;
            af.type = sn;
            af.duration = level / 10;
            af.level = ch.level;
            af.bitvector = RES_ENERGY;
            af.location = 0;
            af.modifier = 0;
            affect_to_char(ch, af);
        } else {
            send_to_char("You are already resistive to draining attacks.\n", ch);
        }
    }

    static void spell_super_heal(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int bonus = 170 + level + dice(1, 20);

        victim.hit = UMIN(victim.hit + bonus, victim.max_hit);
        update_pos(victim);
        send_to_char("A warm feeling fills your body.\n", victim);
        if (ch != victim) {
            send_to_char("Ok.\n", ch);
        }
    }

    static void spell_master_heal(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int bonus = 300 + level + dice(1, 40);

        victim.hit = UMIN(victim.hit + bonus, victim.max_hit);
        update_pos(victim);
        send_to_char("A warm feeling fills your body.\n", victim);
        if (ch != victim) {
            send_to_char("Ok.\n", ch);
        }
    }

    static void spell_group_heal(int level, CHAR_DATA ch) {
        CHAR_DATA gch;

        Skill heal_num = Skill.gsn_mass_healing;
        Skill refresh_num = Skill.gsn_refresh;

        for (gch = ch.in_room.people; gch != null; gch = gch.next_in_room) {
            if ((IS_NPC(ch) && IS_NPC(gch)) ||
                    (!IS_NPC(ch) && !IS_NPC(gch))) {
                spell_heal(level, ch, gch);
                spell_refresh(level, ch, gch);
            }
        }
    }


    static void spell_restoring_light(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int mana_add;

        if (IS_AFFECTED(victim, AFF_BLIND)) {
            Skill nsn = Skill.gsn_cure_blindness;
            spell_cure_blindness(level, ch, victim);
        }
        if (IS_AFFECTED(victim, AFF_CURSE)) {
            Skill nsn = Skill.gsn_remove_curse;
            spell_remove_curse(level, ch, victim, TARGET_CHAR);
        }
        if (IS_AFFECTED(victim, AFF_POISON)) {
            spell_cure_poison(level, ch, victim);
        }
        if (IS_AFFECTED(victim, AFF_PLAGUE)) {
            Skill nsn = Skill.gsn_cure_disease;
            spell_cure_disease(level, ch, victim);
        }

        if (victim.hit != victim.max_hit) {
            mana_add = UMIN((victim.max_hit - victim.hit), ch.mana);
            victim.hit = UMIN(victim.hit + mana_add, victim.max_hit);
            ch.mana -= mana_add;
        }
        update_pos(victim);
        send_to_char("A warm feeling fills your body.\n", victim);
        if (ch != victim) {
            send_to_char("Ok.\n", ch);
        }
    }


    static void spell_lesser_golem(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA gch;
        CHAR_DATA golem;
        int i = 0;

        if (is_affected(ch, sn)) {
            send_to_char("You lack the power to create another golem right now.\n",
                    ch);
            return;
        }

        send_to_char("You attempt to create a lesser golem.\n", ch);
        act("$n attempts to create a lesser golem.", ch, null, null, TO_ROOM);

        for (gch = char_list; gch != null; gch = gch.next) {
            if (IS_NPC(gch) && IS_AFFECTED(gch, AFF_CHARM) && gch.master == ch &&
                    (gch.pIndexData.vnum == MOB_VNUM_LESSER_GOLEM)) {
                i++;
                if (i > 2) {
                    send_to_char("More golems are more than you can control!\n", ch);
                    return;
                }
            }
        }

        if (count_charmed(ch) != 0) {
            return;
        }

        golem = create_mobile(get_mob_index(MOB_VNUM_LESSER_GOLEM));


        for (i = 0; i < MAX_STATS; i++) {
            golem.perm_stat[i] = UMIN(25, 15 + ch.level / 10);
        }

        golem.perm_stat[STAT_STR] += 3;
        golem.perm_stat[STAT_INT] -= 1;
        golem.perm_stat[STAT_CON] += 2;

        golem.max_hit = IS_NPC(ch) ? URANGE(ch.max_hit, ch.max_hit, 30000)
                : UMIN((2 * ch.pcdata.perm_hit) + 400, 30000);
        golem.hit = golem.max_hit;
        golem.max_mana = IS_NPC(ch) ? ch.max_mana : ch.pcdata.perm_mana;
        golem.mana = golem.max_mana;
        golem.level = ch.level;
        for (i = 0; i < 3; i++) {
            golem.armor[i] = interpolate(golem.level, 100, -100);
        }
        golem.armor[3] = interpolate(golem.level, 100, 0);
        golem.gold = 0;
        golem.timer = 0;
        golem.damage[DICE_NUMBER] = 3;
        golem.damage[DICE_TYPE] = 10;
        golem.damage[DICE_BONUS] = ch.level / 2;

        char_to_room(golem, ch.in_room);
        send_to_char("You created a lesser golem!\n", ch);
        act("$n creates a lesser golem!", ch, null, null, TO_ROOM);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

        golem.affected_by = SET_BIT(golem.affected_by, AFF_CHARM);
        golem.master = golem.leader = ch;

    }


    static void spell_stone_golem(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA gch;
        CHAR_DATA golem;
        int i = 0;

        if (is_affected(ch, sn)) {
            send_to_char("You lack the power to create another golem right now.\n",
                    ch);
            return;
        }

        send_to_char("You attempt to create a stone golem.\n", ch);
        act("$n attempts to create a stone golem.", ch, null, null, TO_ROOM);

        for (gch = char_list; gch != null; gch = gch.next) {
            if (IS_NPC(gch) && IS_AFFECTED(gch, AFF_CHARM) && gch.master == ch &&
                    (gch.pIndexData.vnum == MOB_VNUM_STONE_GOLEM)) {
                i++;
                if (i > 2) {
                    send_to_char("More golems are more than you can control!\n", ch);
                    return;
                }
            }
        }

        if (count_charmed(ch) != 0) {
            return;
        }

        golem = create_mobile(get_mob_index(MOB_VNUM_STONE_GOLEM));


        for (i = 0; i < MAX_STATS; i++) {
            golem.perm_stat[i] = UMIN(25, 15 + ch.level / 10);
        }

        golem.perm_stat[STAT_STR] += 3;
        golem.perm_stat[STAT_INT] -= 1;
        golem.perm_stat[STAT_CON] += 2;

        golem.max_hit = IS_NPC(ch) ? URANGE(ch.max_hit, ch.max_hit, 30000)
                : UMIN((5 * ch.pcdata.perm_hit) + 2000, 30000);
        golem.hit = golem.max_hit;
        golem.max_mana = IS_NPC(ch) ? ch.max_mana : ch.pcdata.perm_mana;
        golem.mana = golem.max_mana;
        golem.level = ch.level;
        for (i = 0; i < 3; i++) {
            golem.armor[i] = interpolate(golem.level, 100, -100);
        }
        golem.armor[3] = interpolate(golem.level, 100, 0);
        golem.gold = 0;
        golem.timer = 0;
        golem.damage[DICE_NUMBER] = 8;
        golem.damage[DICE_TYPE] = 4;
        golem.damage[DICE_BONUS] = ch.level / 2;

        char_to_room(golem, ch.in_room);
        send_to_char("You created a stone golem!\n", ch);
        act("$n creates a stone golem!", ch, null, null, TO_ROOM);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

        golem.affected_by = SET_BIT(golem.affected_by, AFF_CHARM);
        golem.master = golem.leader = ch;

    }


    static void spell_iron_golem(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA gch;
        CHAR_DATA golem;
        int i;

        if (is_affected(ch, sn)) {
            send_to_char("You lack the power to create another golem right now.\n",
                    ch);
            return;
        }

        send_to_char("You attempt to create an iron golem.\n", ch);
        act("$n attempts to create an iron golem.", ch, null, null, TO_ROOM);

        for (gch = char_list; gch != null; gch = gch.next) {
            if (IS_NPC(gch) && IS_AFFECTED(gch, AFF_CHARM) && gch.master == ch &&
                    (gch.pIndexData.vnum == MOB_VNUM_IRON_GOLEM)) {
                send_to_char("More golems are more than you can control!\n", ch);
                return;
            }
        }

        if (count_charmed(ch) != 0) {
            return;
        }

        golem = create_mobile(get_mob_index(MOB_VNUM_IRON_GOLEM));


        for (i = 0; i < MAX_STATS; i++) {
            golem.perm_stat[i] = UMIN(25, 15 + ch.level / 10);
        }

        golem.perm_stat[STAT_STR] += 3;
        golem.perm_stat[STAT_INT] -= 1;
        golem.perm_stat[STAT_CON] += 2;

        golem.max_hit = IS_NPC(ch) ? URANGE(ch.max_hit, ch.max_hit, 30000)
                : UMIN((10 * ch.pcdata.perm_hit) + 1000, 30000);
        golem.hit = golem.max_hit;
        golem.max_mana = IS_NPC(ch) ? ch.max_mana : ch.pcdata.perm_mana;
        golem.mana = golem.max_mana;
        golem.level = ch.level;
        for (i = 0; i < 3; i++) {
            golem.armor[i] = interpolate(golem.level, 100, -100);
        }
        golem.armor[3] = interpolate(golem.level, 100, 0);
        golem.gold = 0;
        golem.timer = 0;
        golem.damage[DICE_NUMBER] = 11;
        golem.damage[DICE_TYPE] = 5;
        golem.damage[DICE_BONUS] = ch.level / 2 + 10;

        char_to_room(golem, ch.in_room);
        send_to_char("You created an iron golem!\n", ch);
        act("$n creates an iron golem!", ch, null, null, TO_ROOM);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

        golem.affected_by = SET_BIT(golem.affected_by, AFF_CHARM);
        golem.master = golem.leader = ch;

    }


    static void spell_adamantite_golem(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA gch;
        CHAR_DATA golem;

        if (is_affected(ch, sn)) {
            send_to_char("You lack the power to create another golem right now.\n",
                    ch);
            return;
        }

        send_to_char("You attempt to create an Adamantite golem.\n", ch);
        act("$n attempts to create an Adamantite golem.", ch, null, null, TO_ROOM);

        for (gch = char_list; gch != null; gch = gch.next) {
            if (IS_NPC(gch) && IS_AFFECTED(gch, AFF_CHARM) && gch.master == ch &&
                    (gch.pIndexData.vnum == MOB_VNUM_ADAMANTITE_GOLEM)) {
                send_to_char("More golems are more than you can control!\n", ch);
                return;
            }
        }

        if (count_charmed(ch) != 0) {
            return;
        }

        golem = create_mobile(get_mob_index(MOB_VNUM_ADAMANTITE_GOLEM));


        for (int i = 0; i < MAX_STATS; i++) {
            golem.perm_stat[i] = UMIN(25, 15 + ch.level / 10);
        }

        golem.perm_stat[STAT_STR] += 3;
        golem.perm_stat[STAT_INT] -= 1;
        golem.perm_stat[STAT_CON] += 2;

        golem.max_hit = IS_NPC(ch) ? URANGE(ch.max_hit, ch.max_hit, 30000)
                : UMIN((10 * ch.pcdata.perm_hit) + 4000, 30000);
        golem.hit = golem.max_hit;
        golem.max_mana = IS_NPC(ch) ? ch.max_mana : ch.pcdata.perm_mana;
        golem.mana = golem.max_mana;
        golem.level = ch.level;
        for (int i = 0; i < 3; i++) {
            golem.armor[i] = interpolate(golem.level, 100, -100);
        }
        golem.armor[3] = interpolate(golem.level, 100, 0);
        golem.gold = 0;
        golem.timer = 0;
        golem.damage[DICE_NUMBER] = 13;
        golem.damage[DICE_TYPE] = 9;
        golem.damage[DICE_BONUS] = ch.level / 2 + 10;

        char_to_room(golem, ch.in_room);
        send_to_char("You created an Adamantite golem!\n", ch);
        act("$n creates an Adamantite golem!", ch, null, null, TO_ROOM);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

        golem.affected_by = SET_BIT(golem.affected_by, AFF_CHARM);
        golem.master = golem.leader = ch;

    }


    static void spell_sanctify_lands(CHAR_DATA ch) {
        if (number_bits(1) == 0) {
            send_to_char("You failed.\n", ch);
            return;
        }

        if (IS_RAFFECTED(ch.in_room, AFF_ROOM_CURSE)) {
            affect_strip_room(ch.in_room, gsn_cursed_lands);
            send_to_char("The curse of the land wears off.\n", ch);
            act("The curse of the land wears off.\n", ch, null, null, TO_ROOM);
        }
        if (IS_RAFFECTED(ch.in_room, AFF_ROOM_POISON)) {
            affect_strip_room(ch.in_room, gsn_deadly_venom);
            send_to_char("The land seems more healthy.\n", ch);
            act("The land seems more healthy.\n", ch, null, null, TO_ROOM);
        }
        if (IS_RAFFECTED(ch.in_room, AFF_ROOM_SLEEP)) {
            send_to_char("The land wake up from mysterious dream.\n", ch);
            act("The land wake up from mysterious dream.\n", ch, null, null, TO_ROOM);
            affect_strip_room(ch.in_room, gsn_mysterious_dream);
        }
        if (IS_RAFFECTED(ch.in_room, AFF_ROOM_PLAGUE)) {
            send_to_char("The disease of the land has been treated.\n", ch);
            act("The disease of the land has been treated.\n", ch, null, null, TO_ROOM);
            affect_strip_room(ch.in_room, gsn_black_death);
        }
        if (IS_RAFFECTED(ch.in_room, AFF_ROOM_SLOW)) {
            send_to_char("The lethargic mist dissolves.\n", ch);
            act("The lethargic mist dissolves.\n", ch, null, null, TO_ROOM);
            affect_strip_room(ch.in_room, gsn_lethargic_mist);
        }
    }


    static void spell_deadly_venom(Skill sn, int level, CHAR_DATA ch) {

        if (IS_SET(ch.in_room.room_flags, ROOM_LAW)) {
            send_to_char("This room is protected by gods.\n", ch);
            return;
        }
        if (is_affected_room(ch.in_room, sn)) {
            send_to_char("This room has already been effected by deadly venom.\n", ch);
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_ROOM_AFFECTS;
        af.type = sn;
        af.level = ch.level;
        af.duration = level / 15;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_ROOM_POISON;
        affect_to_room(ch.in_room, af);

        send_to_char("The room starts to be filled by poison.\n", ch);
        act("The room starts to be filled by poison.\n", ch, null, null, TO_ROOM);

    }

    static void spell_cursed_lands(Skill sn, int level, CHAR_DATA ch) {

        if (IS_SET(ch.in_room.room_flags, ROOM_LAW)) {
            send_to_char("This room is protected by gods.\n", ch);
            return;
        }
        if (is_affected_room(ch.in_room, sn)) {
            send_to_char("This room has already been cursed.\n", ch);
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_ROOM_AFFECTS;
        af.type = sn;
        af.level = ch.level;
        af.duration = level / 15;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_ROOM_CURSE;
        affect_to_room(ch.in_room, af);

        send_to_char("The gods has forsaken the room.\n", ch);
        act("The gos has forsaken the room.\n", ch, null, null, TO_ROOM);

    }

    static void spell_lethargic_mist(Skill sn, int level, CHAR_DATA ch) {

        if (IS_SET(ch.in_room.room_flags, ROOM_LAW)) {
            send_to_char("This room is protected by gods.\n", ch);
            return;
        }
        if (is_affected_room(ch.in_room, sn)) {
            send_to_char("This room has already been full of lethargic mist.\n", ch);
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_ROOM_AFFECTS;
        af.type = sn;
        af.level = ch.level;
        af.duration = level / 15;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_ROOM_SLOW;
        affect_to_room(ch.in_room, af);

        send_to_char("The air in the room makes you slowing down.\n", ch);
        act("The air in the room makes you slowing down.\n", ch, null, null, TO_ROOM);

    }

    static void spell_black_death(Skill sn, int level, CHAR_DATA ch) {

        if (IS_SET(ch.in_room.room_flags, ROOM_LAW)) {
            send_to_char("This room is protected by gods.\n", ch);
            return;
        }
        if (is_affected_room(ch.in_room, sn)) {
            send_to_char("This room has already been diseased.\n", ch);
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_ROOM_AFFECTS;
        af.type = sn;
        af.level = ch.level;
        af.duration = level / 15;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_ROOM_PLAGUE;
        affect_to_room(ch.in_room, af);

        send_to_char("The room starts to be filled by disease.\n", ch);
        act("The room starts to be filled by disease.\n", ch, null, null, TO_ROOM);

    }

    static void spell_mysterious_dream(Skill sn, int level, CHAR_DATA ch) {

        if (IS_SET(ch.in_room.room_flags, ROOM_LAW)) {
            send_to_char("This room is protected by gods.\n", ch);
            return;
        }
        if (is_affected_room(ch.in_room, sn)) {
            send_to_char("This room has already been affected by sleep gas.\n", ch);
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_ROOM_AFFECTS;
        af.type = sn;
        af.level = ch.level;
        af.duration = level / 15;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_ROOM_SLEEP;
        affect_to_room(ch.in_room, af);

        send_to_char("The room starts to be seen good place to sleep.\n", ch);
        act("The room starts to be seen good place to you.\n", ch, null, null, TO_ROOM);

    }

    static void spell_polymorph(Skill sn, int level, CHAR_DATA ch) {
        if (is_affected(ch, sn)) {
            send_to_char("You are already polymorphed.\n", ch);
            return;
        }

        if (target_name == null) {
            send_to_char("Usage: cast 'polymorph' <pcracename>.\n", ch);
            return;
        }

        Race race = Race.lookupRace(target_name);

        if (race == null || race.pcRace == null) {
            send_to_char("That is not a valid race to polymorph.\n", ch);
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_RACE;
        af.type = sn;
        af.level = level;
        af.duration = level / 10;
        af.location = APPLY_NONE;
        af.objModifier = race;
        af.bitvector = 0;
        affect_to_char(ch, af);

        act("$n polymorphes $mself to $t.", ch, race.name, null, TO_ROOM);
        act("You polymorph yourself to $t.\n", ch, race.name, null, TO_CHAR);
    }


    static void spell_plant_form(int level, CHAR_DATA ch) {

        if (ch.in_room == null
                || IS_SET(ch.in_room.room_flags, ROOM_PRIVATE)
                || IS_SET(ch.in_room.room_flags, ROOM_SOLITARY)
                || (ch.in_room.sector_type != SECT_FOREST
                && ch.in_room.sector_type != SECT_FIELD)
                || IS_SET(ch.in_room.room_flags, ROOM_NO_RECALL)) {
            send_to_char("Not here.\n", ch);
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = gsn_fear;
        af.level = level;
        af.duration = level / 10;
        af.location = 0;
        af.modifier = 0;

        if (ch.in_room.sector_type == SECT_FOREST) {
            send_to_char("You starts to be seen a nearby tree!\n", ch);
            act("$n starts to be seen a nearby tree!", ch, null, null, TO_ROOM);
            af.bitvector = AFF_FORM_TREE;
        } else {
            send_to_char("You starts to be seen some grass!\n", ch);
            act("$n starts to be seen some grass!", ch, null, null, TO_ROOM);
            af.bitvector = AFF_FORM_GRASS;
        }
        affect_to_char(ch, af);
    }


    static void spell_blade_barrier(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        act("Many sharp blades appear around $n and crash $N.",
                ch, null, victim, TO_ROOM);
        act("Many sharp blades appear around you and crash $N.",
                ch, null, victim, TO_CHAR);
        act("Many sharp blades appear around $n and crash you!",
                ch, null, victim, TO_VICT);

        dam = dice(level, 7);
        if (saves_spell(level, victim, DAM_PIERCE)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_PIERCE, true);

        if (!IS_NPC(ch) && victim != ch &&
                ch.fighting != victim && victim.fighting != ch &&
                (IS_SET(victim.affected_by, AFF_CHARM) || !IS_NPC(victim))) {
            if (!can_see(victim, ch)) {
                do_yell(victim, "Help someone is attacking me!");
            } else {
                TextBuffer buf = new TextBuffer();
                buf.sprintf("Die, %s, you sorcerous dog!",
                        (is_affected(ch, gsn_doppelganger) && !IS_IMMORTAL(victim)) ? ch.doppel.name : ch.name);
                do_yell(victim, buf);
            }
        }

        act("The blade barriers crash $n!", victim, null, null, TO_ROOM);
        dam = dice(level, 5);
        if (saves_spell(level, victim, DAM_PIERCE)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_PIERCE, true);
        act("The blade barriers crash you!", victim, null, null, TO_CHAR);

        if (number_percent() < 50) {
            return;
        }

        act("The blade barriers crash $n!", victim, null, null, TO_ROOM);
        dam = dice(level, 4);
        if (saves_spell(level, victim, DAM_PIERCE)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_PIERCE, true);
        act("The blade barriers crash you!", victim, null, null, TO_CHAR);

        if (number_percent() < 50) {
            return;
        }

        act("The blade barriers crash $n!", victim, null, null, TO_ROOM);
        dam = dice(level, 2);
        if (saves_spell(level, victim, DAM_PIERCE)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_PIERCE, true);
        act("The blade barriers crash you!", victim, null, null, TO_CHAR);

    }


    static void spell_protection_negative(Skill sn, int level, CHAR_DATA ch) {

        if (!is_affected(ch, sn)) {
            send_to_char("You are now immune to negative attacks.\n", ch);

            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_IMMUNE;
            af.type = sn;
            af.duration = level / 4;
            af.level = ch.level;
            af.bitvector = IMM_NEGATIVE;
            af.location = 0;
            af.modifier = 0;
            affect_to_char(ch, af);
        } else {
            send_to_char("You are already immune to negative attacks.\n", ch);
        }
    }


    static void spell_ruler_aura(Skill sn, int level, CHAR_DATA ch) {

        if (!is_affected(ch, sn)) {
            send_to_char("You now feel more self confident in rulership.\n", ch);

            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_IMMUNE;
            af.type = sn;
            af.duration = level / 4;
            af.level = ch.level;
            af.bitvector = IMM_CHARM;
            af.location = 0;
            af.modifier = 0;
            affect_to_char(ch, af);
        } else {
            send_to_char("You are as much self confident as you can.\n", ch);
        }
    }


    static void spell_evil_spirit(Skill sn, int level, CHAR_DATA ch) {
        AREA_DATA pArea = ch.in_room.area;
        ROOM_INDEX_DATA room;
        int i;

        if (IS_RAFFECTED(ch.in_room, AFF_ROOM_ESPIRIT)
                || is_affected_room(ch.in_room, sn)) {
            send_to_char("The zone is already full of evil spirit.\n", ch);
            return;
        }

        if (is_affected(ch, sn)) {
            send_to_char("Your power of evil spirit is less for you, now.\n", ch);
            return;
        }

        if (IS_SET(ch.in_room.room_flags, ROOM_LAW)
                || IS_SET(ch.in_room.area.area_flag, AREA_HOMETOWN)) {
            send_to_char("Holy aura in this room prevents your powers to work on it.\n", ch);
            return;
        }

        AFFECT_DATA af2 = new AFFECT_DATA();
        af2.where = TO_AFFECTS;
        af2.type = sn;
        af2.level = ch.level;
        af2.duration = level / 5;
        af2.modifier = 0;
        af2.location = APPLY_NONE;
        af2.bitvector = 0;
        affect_to_char(ch, af2);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_ROOM_AFFECTS;
        af.type = sn;
        af.level = ch.level;
        af.duration = level / 25;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_ROOM_ESPIRIT;

        for (i = pArea.min_vnum; i < pArea.max_vnum; i++) {
            if ((room = get_room_index(i)) == null) {
                continue;
            }
            affect_to_room(room, af);
            if (room.people != null) {
                act("The zone is starts to be filled with evil spirit.", room.people, null, null, TO_ALL);
            }
        }

    }


    static void spell_disgrace(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (!is_affected(victim, sn) && !saves_spell(level, victim, DAM_MENTAL)) {
            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = sn;
            af.level = level;
            af.duration = level;
            af.location = APPLY_CHA;
            af.modifier = -(5 + level / 5);
            af.bitvector = 0;
            affect_to_char(victim, af);

            act("$N feels $M less confident!", ch, null, victim, TO_ALL);
        } else {
            send_to_char("You failed.\n", ch);
        }
    }


    static void spell_control_undead(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (!IS_NPC(victim) || !IS_SET(victim.act, ACT_UNDEAD)) {
            act("$N doesn't seem to be an undead.", ch, null, victim, TO_CHAR);
            return;
        }
        spell_charm_person(sn, level, ch, vo);
    }


    static void spell_assist(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(ch, sn)) {
            send_to_char("This power is used too recently.\n", ch);
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 1 + level / 50;
        af.location = 0;
        af.modifier = 0;
        af.bitvector = 0;
        affect_to_char(ch, af);

        victim.hit += 100 + level * 5;
        update_pos(victim);
        send_to_char("A warm feeling fills your body.\n", victim);
        act("$n looks better.", victim, null, null, TO_ROOM);
        if (ch != victim) {
            send_to_char("Ok.\n", ch);
        }
    }


    static void spell_aid(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(ch, sn)) {
            send_to_char("This power is used too recently.\n", ch);
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = level / 50;
        af.location = 0;
        af.modifier = 0;
        af.bitvector = 0;
        affect_to_char(ch, af);

        victim.hit += level * 5;
        update_pos(victim);
        send_to_char("A warm feeling fills your body.\n", victim);
        act("$n looks better.", victim, null, null, TO_ROOM);
        if (ch != victim) {
            send_to_char("Ok.\n", ch);
        }
    }


    static void spell_summon_shadow(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA gch;
        CHAR_DATA shadow;
        int i;

        if (is_affected(ch, sn)) {
            send_to_char("You lack the power to summon another shadow right now.\n",
                    ch);
            return;
        }

        send_to_char("You attempt to summon a shadow.\n", ch);
        act("$n attempts to summon a shadow.", ch, null, null, TO_ROOM);

        for (gch = char_list; gch != null; gch = gch.next) {
            if (IS_NPC(gch) && IS_AFFECTED(gch, AFF_CHARM) && gch.master == ch &&
                    gch.pIndexData.vnum == MOB_VNUM_SUM_SHADOW) {
                send_to_char("Two shadows are more than you can control!\n", ch);
                return;
            }
        }

        if (count_charmed(ch) != 0) {
            return;
        }

        shadow = create_mobile(get_mob_index(MOB_VNUM_SUM_SHADOW));

        for (i = 0; i < MAX_STATS; i++) {
            shadow.perm_stat[i] = ch.perm_stat[i];
        }

        shadow.max_hit = IS_NPC(ch) ? URANGE(ch.max_hit, ch.max_hit, 30000)
                : URANGE(ch.pcdata.perm_hit, ch.hit, 30000);
        shadow.hit = shadow.max_hit;
        shadow.max_mana = IS_NPC(ch) ? ch.max_mana : ch.pcdata.perm_mana;
        shadow.mana = shadow.max_mana;
        shadow.level = ch.level;
        for (i = 0; i < 3; i++) {
            shadow.armor[i] = interpolate(shadow.level, 100, -100);
        }
        shadow.armor[3] = interpolate(shadow.level, 100, 0);
        shadow.gold = 0;
        shadow.timer = 0;
        shadow.damage[DICE_NUMBER] = number_range(level / 15, level / 10);
        shadow.damage[DICE_TYPE] = number_range(level / 3, level / 2);
        shadow.damage[DICE_BONUS] = number_range(level / 8, level / 6);

        char_to_room(shadow, ch.in_room);
        act("A shadow conjures!", ch, null, null, TO_ALL);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

        shadow.affected_by = SET_BIT(shadow.affected_by, AFF_CHARM);
        shadow.master = shadow.leader = ch;

    }


    static void spell_farsight(CHAR_DATA ch) {
        ROOM_INDEX_DATA room, oldr;

        if ((room = check_place(ch, target_name)) == null) {
            send_to_char("You cannot see that much far.\n", ch);
            return;
        }

        if (ch.in_room == room) {
            do_look(ch, "auto");
        } else {
            boolean mount = MOUNTED(ch) != null;
            oldr = ch.in_room;
            char_from_room(ch);
            char_to_room(ch, room);
            do_look(ch, "auto");
            char_from_room(ch);
            char_to_room(ch, oldr);
            if (mount) {
                ch.riding = true;
                MOUNTED(ch).riding = true;
            }
        }
    }


    static void spell_remove_fear(int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (check_dispel(level, victim, gsn_fear)) {
            send_to_char("You feel more brave.\n", victim);
            act("$n looks more conscious.", victim, null, null, TO_ROOM);
        } else {
            send_to_char("You failed.\n", ch);
        }
    }

    static void spell_desert_fist(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        if ((ch.in_room.sector_type != SECT_HILLS)
                && (ch.in_room.sector_type != SECT_MOUNTAIN)
                && (ch.in_room.sector_type != SECT_DESERT)) {
            send_to_char("You don't find any sand here to create a fist.\n", ch);
            ch.wait = 0;
            return;
        }

        act("An existing parcel of sand rises up and forms a fist and pummels $n.",
                victim, null, null, TO_ROOM);
        act("An existing parcel of sand rises up and forms a fist and pummels you.",
                victim, null, null, TO_CHAR);
        dam = dice(level, 16);
        damage(ch, victim, dam, sn, DAM_OTHER, true);
        sand_effect(victim, level, dam, TARGET_CHAR);
    }

    static void spell_holy_aura(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(victim, sn)) {
            if (victim == ch) {
                send_to_char("You have already got a holy aura.\n", ch);
            } else {
                act("$N has already got a holy aura.", ch, null, victim, TO_CHAR);
            }
            return;
        }

        if (!IS_GOOD(victim)) {
            send_to_char("It doesn't worth to protect to with holy aura!", ch);
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 7 + level / 6;
        af.modifier = -(20 + level / 4);
        af.location = APPLY_AC;
        af.bitvector = 0;
        affect_to_char(victim, af);

        af.where = TO_RESIST;
        af.location = 0;
        af.modifier = 0;
        af.bitvector = RES_NEGATIVE;
        affect_to_char(ch, af);

        send_to_char("You feel ancient holy power protecting you.\n", victim);
        if (ch != victim) {
            act("$N is protected by ancient holy power.", ch, null, victim, TO_CHAR);
        }
    }

    static void spell_holy_fury(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(victim, sn) || IS_AFFECTED(victim, AFF_BERSERK)) {
            if (victim == ch) {
                send_to_char("You are already in a holy fury.\n", ch);
            } else {
                act("$N is already in a holy fury.", ch, null, victim, TO_CHAR);
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

        if (!IS_GOOD(victim)) {
            act("$N doesn't worth to influence with holy fury.", ch, null, victim, TO_CHAR);
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

        send_to_char("You are filled with holy fury!\n", victim);
        act("$n gets a wild look in $s eyes!", victim, null, null, TO_ROOM);
    }

    static void spell_light_arrow(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 12);
        if (saves_spell(level, victim, DAM_HOLY)) {
            dam /= 2;
        }
        damage(ch, victim, dam, sn, DAM_HOLY, true);
    }


    static void spell_hydroblast(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        if ((ch.in_room.sector_type != SECT_WATER_SWIM)
                && (ch.in_room.sector_type != SECT_WATER_NOSWIM)
                && (weather_info.sky != SKY_RAINING || !IS_OUTSIDE(ch))) {
            send_to_char("You couldn't reach any water molecule here.\n", ch);
            ch.wait = 0;
            return;
        }

        act("The water molecules around $n comes together and forms a fist.",
                ch, null, null, TO_ROOM);
        act("The water molecules around you comes together and forms a fist.",
                ch, null, null, TO_CHAR);
        dam = dice(level, 14);
        damage(ch, victim, dam, sn, DAM_BASH, true);
    }

    static void spell_wolf_spirit(Skill sn, int level, CHAR_DATA ch) {

        if (is_affected(ch, sn)) {
            send_to_char("The blood in your vains flowing as fast as it can!\n", ch);
            return;
        }

/* haste */
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 3 + level / 30;
        af.location = APPLY_DEX;
        af.modifier = 1 + (level > 40 ? 1 : 0) + (level > 60 ? 1 : 0);
        af.bitvector = AFF_HASTE;
        affect_to_char(ch, af);

/* damroll */
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 3 + level / 30;
        af.location = APPLY_DAMROLL;
        af.modifier = level / 2;
        af.bitvector = AFF_BERSERK;
        affect_to_char(ch, af);

/* infravision */
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 3 + level / 30;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_INFRARED;
        affect_to_char(ch, af);


        send_to_char("The blood in your vains start to flow faster.\n", ch);
        act("The eyes of $n turn to RED!", ch, null, null, TO_ROOM);
    }


    static void spell_sword_of_justice(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        if ((IS_GOOD(ch) && IS_GOOD(victim))
                || (IS_EVIL(ch) && IS_EVIL(victim))
                || IS_NEUTRAL(ch)) {
            if (IS_NPC(victim) || !IS_SET(victim.act, PLR_WANTED)) {
                send_to_char("You failed!\n", ch);
                return;
            }
        }

        if (!IS_NPC(victim) && IS_SET(victim.act, PLR_WANTED)) {
            dam = dice(level, 20);
        } else {
            dam = dice(level, 14);
        }

        if (saves_spell(level, victim, DAM_MENTAL)) {
            dam /= 2;
        }

        do_yell(ch, "The Sword of Justice!");
        act("The sword of justice appears and strikes $N!", ch, null, victim, TO_ALL);

        damage(ch, victim, dam, sn, DAM_MENTAL, true);
    }

    static void spell_guard_dogs(Skill sn, int level, CHAR_DATA ch) {
        CHAR_DATA gch;
        CHAR_DATA dog;
        CHAR_DATA dog2;
        int i;

        if (is_affected(ch, sn)) {
            send_to_char("You lack the power to summon another wolf right now.\n", ch);
            return;
        }

        send_to_char("You attempt to summon a dog.\n", ch);
        act("$n attempts to summon a dog.", ch, null, null, TO_ROOM);

        for (gch = char_list; gch != null; gch = gch.next) {
            if (IS_NPC(gch) && IS_AFFECTED(gch, AFF_CHARM) && gch.master == ch &&
                    gch.pIndexData.vnum == MOB_VNUM_DOG) {
                send_to_char("Two dogs are more than you can control!\n", ch);
                return;
            }
        }

        if (count_charmed(ch) != 0) {
            return;
        }

        dog = create_mobile(get_mob_index(MOB_VNUM_DOG));

        for (i = 0; i < MAX_STATS; i++) {
            dog.perm_stat[i] = ch.perm_stat[i];
        }

        dog.max_hit = IS_NPC(ch) ? URANGE(ch.max_hit, ch.max_hit, 30000)
                : URANGE(ch.pcdata.perm_hit, ch.hit, 30000);
        dog.hit = dog.max_hit;
        dog.max_mana = IS_NPC(ch) ? ch.max_mana : ch.pcdata.perm_mana;
        dog.mana = dog.max_mana;
        dog.level = ch.level;
        for (i = 0; i < 3; i++) {
            dog.armor[i] = interpolate(dog.level, 100, -100);
        }
        dog.armor[3] = interpolate(dog.level, 100, 0);
        dog.gold = 0;
        dog.timer = 0;
        dog.damage[DICE_NUMBER] = number_range(level / 15, level / 12);
        dog.damage[DICE_TYPE] = number_range(level / 3, level / 2);
        dog.damage[DICE_BONUS] = number_range(level / 10, level / 8);

        dog2 = create_mobile(dog.pIndexData);
        clone_mobile(dog, dog2);

        dog.affected_by = SET_BIT(dog.affected_by, AFF_CHARM);
        dog2.affected_by = SET_BIT(dog2.affected_by, AFF_CHARM);
        dog.master = dog2.master = ch;
        dog.leader = dog2.leader = ch;

        char_to_room(dog, ch.in_room);
        char_to_room(dog2, ch.in_room);
        send_to_char("Two dogs arrive and bows before you!\n", ch);
        act("Two dogs arrive from somewhere and bows!", ch, null, null, TO_ROOM);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

    }

    static void spell_eyes_of_tiger(CHAR_DATA ch) {
        CHAR_DATA victim;
        ROOM_INDEX_DATA ori_room;

        if ((victim = get_char_world(ch, target_name)) == null) {
            send_to_char("Your tiger eyes cannot see such a player.\n", ch);
            return;
        }

        if (IS_NPC(victim) || victim.cabal != CABAL_HUNTER) {
            send_to_char("Your tiger eyes sees only hunters!\n", ch);
            return;
        }

        if ((victim.level > ch.level + 7)
                || saves_spell((ch.level + 9), victim, DAM_NONE)) {
            send_to_char("Your tiger eyes cannot see that player.\n", ch);
            return;
        }

        if (ch == victim) {
            do_look(ch, "auto");
        } else {
            ori_room = ch.in_room;
            char_from_room(ch);
            char_to_room(ch, victim.in_room);
            do_look(ch, "auto");
            char_from_room(ch);
            char_to_room(ch, ori_room);
        }
    }

    static void spell_lion_shield(Skill sn, int level, CHAR_DATA ch) {
        OBJ_DATA shield;

        shield = create_object(get_obj_index(OBJ_VNUM_LION_SHIELD), level);
        shield.timer = level;
        shield.level = ch.level;
        shield.cost = 0;
        obj_to_char(shield, ch);


        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_OBJECT;
        af.type = sn;
        af.level = level;
        af.duration = -1;
        af.modifier = level / 8;
        af.bitvector = 0;

        af.location = APPLY_HITROLL;
        affect_to_obj(shield, af);

        af.location = APPLY_DAMROLL;
        affect_to_obj(shield, af);


        af.where = TO_OBJECT;
        af.type = sn;
        af.level = level;
        af.duration = -1;
        af.modifier = -(level * 2) / 3;
        af.bitvector = 0;
        af.location = APPLY_AC;
        affect_to_obj(shield, af);

        af.where = TO_OBJECT;
        af.type = sn;
        af.level = level;
        af.duration = -1;
        af.modifier = UMAX(1, level / 30);
        af.bitvector = 0;
        af.location = APPLY_CHA;
        affect_to_obj(shield, af);

        af.where = TO_OBJECT;
        af.type = sn;
        af.level = level;
        af.duration = -1;
        af.modifier = -level / 9;
        af.bitvector = 0;
        af.location = APPLY_SAVING_SPELL;
        affect_to_obj(shield, af);

        act("You create $p!", ch, shield, null, TO_CHAR);
        act("$n creates $p!", ch, shield, null, TO_ROOM);
    }

    static void spell_evolve_lion(Skill sn, int level, CHAR_DATA ch) {

        if (is_affected(ch, sn) || ch.hit > ch.max_hit) {
            send_to_char("You are already as lion as you can get.\n", ch);
            return;
        }

        ch.hit += ch.pcdata.perm_hit;

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 3 + level / 30;
        af.location = APPLY_HIT;
        af.modifier = ch.pcdata.perm_hit;
        af.bitvector = 0;
        affect_to_char(ch, af);

        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 3 + level / 30;
        af.location = APPLY_DEX;
        af.modifier = -(1 + level / 30);
        af.bitvector = AFF_SLOW;
        affect_to_char(ch, af);

        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 3 + level / 30;
        af.location = APPLY_DAMROLL;
        af.modifier = level / 2;
        af.bitvector = AFF_BERSERK;
        affect_to_char(ch, af);

        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 3 + level / 30;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_LION;
        affect_to_char(ch, af);

        send_to_char("You feel yourself more clumsy, but more strong.\n", ch);
        act("The skin of $n turns to grey!", ch, null, null, TO_ROOM);
    }

    static void spell_prevent(Skill sn, int level, CHAR_DATA ch) {

        if (is_affected_room(ch.in_room, sn)) {
            send_to_char("This room has already prevented from revenges!\n", ch);
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_ROOM_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = level / 30;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_ROOM_PREVENT;
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
        send_to_char("The room is now protected from hunter revenges!\n", ch);
        act("The room starts to be filled with $n's prevention from Hunters", ch, null, null, TO_ROOM);
    }


    static void spell_enlarge(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (is_affected(victim, sn)) {
            if (victim == ch) {
                send_to_char("You can't enlarge more!\n", ch);
            } else {
                act("$N is already as large as $N can get.", ch, null, victim, TO_CHAR);
            }
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = level / 2;
        af.location = APPLY_SIZE;
        af.modifier = 1 + (level >= 35 ? 1 : 0) + (level >= 65 ? 1 : 0);
        af.modifier = UMIN((SIZE_GARGANTUAN - victim.size), af.modifier);
        af.bitvector = 0;
        affect_to_char(victim, af);

        send_to_char("You feel yourself getting larger and larger.\n", victim);
        act("$n's body starts to enlarge.", victim, null, null, TO_ROOM);
    }

    static void spell_chromatic_orb(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;
        int dam;

        dam = dice(level, 14);
        if (saves_spell(level, victim, DAM_LIGHT)) {
            dam /= 2;
        }

        if (number_percent() < (0.7 * get_skill(ch, sn))) {
            spell_blindness(Skill.gsn_blindness, (level - 10), ch, victim);
            spell_slow(Skill.gsn_slow, (level - 10), ch, victim);
        }

        damage(ch, victim, dam, sn, DAM_LIGHT, true);

    }

    static void spell_suffocate(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_SUFFOCATE)) {
            act("$N already cannot breathe.\n", ch, null, victim, TO_CHAR);
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
        af.duration = 3 + level / 30;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_SUFFOCATE;
        affect_join(victim, af);

        send_to_char("You cannot breathe.\n", victim);
        act("$n tries to breathe, but cannot.", victim, null, null, TO_ROOM);

    }

    static void spell_mummify(Skill sn, int level, CHAR_DATA ch, Object vo, int target) {
        CHAR_DATA victim;
        CHAR_DATA undead;
        OBJ_DATA obj, obj2, next;

        int i;

        /* deal with the object case first */
        if (target == TARGET_OBJ) {
            obj = (OBJ_DATA) vo;

            if (!(obj.item_type == ITEM_CORPSE_NPC ||
                    obj.item_type == ITEM_CORPSE_PC)) {
                send_to_char("You can animate only corpses!\n", ch);
                return;
            }

            if (obj.level > level + 10) {
                send_to_char("The dead body is too strong for you to mummify!\n", ch);
                return;
            }

            if (is_affected(ch, sn)) {
                send_to_char("You cannot summon the strength to handle more undead bodies.\n", ch);
                return;
            }

            if (count_charmed(ch) != 0) {
                return;
            }

            if (ch.in_room != null && IS_SET(ch.in_room.room_flags, ROOM_NO_MOB)) {
                send_to_char("You can't mummify deads here.\n", ch);
                return;
            }

            if (IS_SET(ch.in_room.room_flags, ROOM_SAFE) ||
                    IS_SET(ch.in_room.room_flags, ROOM_PRIVATE) ||
                    IS_SET(ch.in_room.room_flags, ROOM_SOLITARY)) {
                send_to_char("You can't mummify here.\n", ch);
                return;
            }

            undead = create_mobile(get_mob_index(MOB_VNUM_UNDEAD));
            char_to_room(undead, ch.in_room);
            for (i = 0; i < MAX_STATS; i++) {
                undead.perm_stat[i] = UMIN(25, 2 * ch.perm_stat[i]);
            }

            undead.level = obj.level;
            undead.max_hit = (undead.level < 30) ? (undead.level * 30) :
                    (undead.level < 60) ? (undead.level * 60) :
                            (undead.level * 90);
            undead.hit = undead.max_hit;
            undead.max_mana = IS_NPC(ch) ? ch.max_mana : ch.pcdata.perm_mana;
            undead.mana = undead.max_mana;
            undead.alignment = ch.alignment;

            for (i = 0; i < 3; i++) {
                undead.armor[i] = interpolate(undead.level, 100, -100);
            }
            undead.armor[3] = interpolate(undead.level, 50, -200);
            undead.damage[DICE_NUMBER] = number_range(undead.level / 20, undead.level / 15);
            undead.damage[DICE_TYPE] = number_range(undead.level / 6, undead.level / 3);
            undead.damage[DICE_BONUS] = number_range(undead.level / 12, undead.level / 10);
            undead.sex = ch.sex;
            undead.gold = 0;

            undead.act = SET_BIT(undead.act, ACT_UNDEAD);
            undead.affected_by = SET_BIT(undead.affected_by, AFF_CHARM);
            undead.master = ch;
            undead.leader = ch;

            TextBuffer buf = new TextBuffer();
            buf.sprintf("%s body undead", obj.name);
            undead.name = buf.toString();
            String argument = obj.short_descr;
            StringBuilder buf3 = new StringBuilder();
            StringBuilder argb = new StringBuilder();
            while (argument.length() != 0) {
                argb.setLength(0);
                argument = one_argument(argument, argb);
                String arg = argb.toString();
                if (!(!str_cmp(arg, "The") || !str_cmp(arg, "undead") || !str_cmp(arg, "body") ||
                        !str_cmp(arg, "corpse") || !str_cmp(arg, "of"))) {
                    if (buf3.length() == 0) {
                        buf3.append(arg);
                    } else {
                        buf3.append(" ").append(arg);
                    }
                }
            }
            buf.sprintf("The mummified corpse of %s", buf3);
            undead.short_descr = buf.toString();
            buf.sprintf("The mummifed corpse of %s slowly staggers around.\n", buf3);
            undead.long_descr = buf.toString();

            for (obj2 = obj.contains; obj2 != null; obj2 = next) {
                next = obj2.next_content;
                obj_from_obj(obj2);
                obj_to_char(obj2, undead);
            }
            interpret(undead, "wear all", true);
            AFFECT_DATA af = new AFFECT_DATA();
            af.where = TO_AFFECTS;
            af.type = sn;
            af.level = ch.level;
            af.duration = (ch.level / 10);
            af.modifier = 0;
            af.bitvector = 0;
            af.location = APPLY_NONE;
            affect_to_char(ch, af);

            send_to_char("With mystic power, you mummify and give life to it!\n", ch);
            buf.sprintf("With mystic power, %s mummifies %s and give life to it!", ch.name, obj.name);
            act(buf, ch, null, null, TO_ROOM);
            buf.sprintf("%s looks at you and plans to make you pay for distrurbing its rest!", obj.short_descr);
            act(buf, ch, null, null, TO_CHAR);
            extract_obj(obj);
            return;
        }

        victim = (CHAR_DATA) vo;

        if (victim == ch) {
            send_to_char("But you aren't dead!!\n", ch);
            return;
        }

        send_to_char("But it ain't dead!!\n", ch);
    }

    static void spell_soul_bind(CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (ch.pet != null) {
            send_to_char("Your soul is already binded to someone else.\n", ch);
            return;
        }

        if (!IS_NPC(victim)
                || !(IS_AFFECTED(victim, AFF_CHARM) && victim.master == ch)) {
            send_to_char("You cannot bind that soul to you.\n", ch);
            return;
        }

        victim.leader = ch;
        ch.pet = victim;

        act("You bind $N's soul to yourself.", ch, null, victim, TO_CHAR);
        act("$n binds $N's soul to $mself.", ch, null, victim, TO_ROOM);

    }

    static void spell_forcecage(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_PROTECTOR)) {
            if (victim == ch) {
                send_to_char("You have already your forcecage around you.\n", ch);
            } else {
                act("$N has already a forcecage around $mself.", ch, null, victim, TO_CHAR);
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
        af.bitvector = AFF_PROTECTOR;
        affect_to_char(victim, af);
        act("$n calls in arcane powers to build a cage of power around $mself.", victim, null, null, TO_ROOM);
        send_to_char("You call in arcane powers to build a cage of power around you.\n", victim);
    }

    static void spell_iron_body(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_PROTECTOR)) {
            if (victim == ch) {
                send_to_char("Your skin is already as hard as iron.\n", ch);
            } else {
                act("$N's skin is already as hard as iron.", ch, null, victim, TO_CHAR);
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
        af.bitvector = AFF_PROTECTOR;
        affect_to_char(victim, af);
        act("$n skin is now as hard as iron.", victim, null, null, TO_ROOM);
        send_to_char("Your skin is now as hard as iron.\n", victim);
    }

    static void spell_elemental_sphere(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (IS_AFFECTED(victim, AFF_PROTECTOR)) {
            if (victim == ch) {
                send_to_char("An elemental sphere is already protecting you.\n", ch);
            } else {
                act("An elemental sphere is already protecting $N.", ch, null, victim, TO_CHAR);
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
        af.bitvector = AFF_PROTECTOR;
        affect_to_char(victim, af);
        act("$n uses all elemental powers to build a sphere around $mself.", victim, null, null, TO_ROOM);
        send_to_char("You use all elemental powers to build a sphere around you.\n", victim);
    }


    static void spell_aura_of_chaos(Skill sn, int level, CHAR_DATA ch, Object vo) {
        CHAR_DATA victim = (CHAR_DATA) vo;

        if (ch.cabal != victim.cabal) {
            send_to_char("You may only use this spell on fellow cabal members.\n", ch);
            return;
        }

        if (is_affected(victim, sn)) {
            if (victim == ch) {
                send_to_char("You are already protected by gods of chaos.\n", ch);
            } else {
                act("$N is already protected by aura of chaos.", ch, null, victim, TO_CHAR);
            }
            return;
        }

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = sn;
        af.level = level;
        af.duration = 24;
        af.modifier = 0;
        af.location = APPLY_NONE;
        af.bitvector = AFF_AURA_CHAOS;
        affect_to_char(victim, af);
        send_to_char("You feel the gods of chaos protect you.\n", victim);
        if (ch != victim) {
            act("An aura appears around $N.", ch, null, victim, TO_CHAR);
        }
    }
}
