package net.sf.nightworks;

import net.sf.nightworks.util.TextBuffer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;

import static net.sf.nightworks.ActComm.do_quit;
import static net.sf.nightworks.ActInfo.do_look;
import static net.sf.nightworks.ActInfo.get_cond_alias;
import static net.sf.nightworks.ActInfo.set_title;
import static net.sf.nightworks.ActMove.find_way;
import static net.sf.nightworks.ActSkill.exp_per_level;
import static net.sf.nightworks.ActSkill.exp_to_level;
import static net.sf.nightworks.Comm.act;
import static net.sf.nightworks.Comm.check_parse_name;
import static net.sf.nightworks.Comm.close_socket;
import static net.sf.nightworks.Comm.newlock;
import static net.sf.nightworks.Comm.nw_down;
import static net.sf.nightworks.Comm.nw_exit;
import static net.sf.nightworks.Comm.page_to_char;
import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.Comm.wizlock;
import static net.sf.nightworks.Comm.write_to_buffer;
import static net.sf.nightworks.Comm.write_to_descriptor;
import static net.sf.nightworks.Const.attack_table;
import static net.sf.nightworks.Const.liq_table;
import static net.sf.nightworks.Const.religion_table;
import static net.sf.nightworks.Const.wiznet_table;
import static net.sf.nightworks.DB.area_update;
import static net.sf.nightworks.DB.bug;
import static net.sf.nightworks.DB.clone_mobile;
import static net.sf.nightworks.DB.clone_object;
import static net.sf.nightworks.DB.create_mobile;
import static net.sf.nightworks.DB.create_object;
import static net.sf.nightworks.DB.get_mob_index;
import static net.sf.nightworks.DB.get_obj_index;
import static net.sf.nightworks.DB.get_room_index;
import static net.sf.nightworks.DB.log_string;
import static net.sf.nightworks.DB.top_mob_index;
import static net.sf.nightworks.DB.top_obj_index;
import static net.sf.nightworks.Fight.stop_fighting;
import static net.sf.nightworks.Fight.update_pos;
import static net.sf.nightworks.Handler.act_bit_name;
import static net.sf.nightworks.Handler.affect_bit_name;
import static net.sf.nightworks.Handler.affect_loc_name;
import static net.sf.nightworks.Handler.affect_remove;
import static net.sf.nightworks.Handler.affect_strip;
import static net.sf.nightworks.Handler.cabal_lookup;
import static net.sf.nightworks.Handler.can_carry_n;
import static net.sf.nightworks.Handler.can_see;
import static net.sf.nightworks.Handler.can_see_obj;
import static net.sf.nightworks.Handler.can_see_room;
import static net.sf.nightworks.Handler.char_from_room;
import static net.sf.nightworks.Handler.char_to_room;
import static net.sf.nightworks.Handler.comm_bit_name;
import static net.sf.nightworks.Handler.cont_bit_name;
import static net.sf.nightworks.Handler.extra_bit_name;
import static net.sf.nightworks.Handler.extract_char;
import static net.sf.nightworks.Handler.extract_obj;
import static net.sf.nightworks.Handler.flag_room_name;
import static net.sf.nightworks.Handler.form_bit_name;
import static net.sf.nightworks.Handler.get_age;
import static net.sf.nightworks.Handler.get_char_room;
import static net.sf.nightworks.Handler.get_char_world;
import static net.sf.nightworks.Handler.get_curr_stat;
import static net.sf.nightworks.Handler.get_eq_char;
import static net.sf.nightworks.Handler.get_light_char;
import static net.sf.nightworks.Handler.get_max_train;
import static net.sf.nightworks.Handler.get_obj_here;
import static net.sf.nightworks.Handler.get_obj_number;
import static net.sf.nightworks.Handler.get_obj_weight;
import static net.sf.nightworks.Handler.get_obj_world;
import static net.sf.nightworks.Handler.get_true_weight;
import static net.sf.nightworks.Handler.get_trust;
import static net.sf.nightworks.Handler.get_wield_char;
import static net.sf.nightworks.Handler.imm_bit_name;
import static net.sf.nightworks.Handler.is_name;
import static net.sf.nightworks.Handler.is_room_owner;
import static net.sf.nightworks.Handler.item_type_name;
import static net.sf.nightworks.Handler.obj_to_char;
import static net.sf.nightworks.Handler.obj_to_obj;
import static net.sf.nightworks.Handler.obj_to_room;
import static net.sf.nightworks.Handler.off_bit_name;
import static net.sf.nightworks.Handler.part_bit_name;
import static net.sf.nightworks.Handler.raffect_bit_name;
import static net.sf.nightworks.Handler.room_is_private;
import static net.sf.nightworks.Handler.weapon_bit_name;
import static net.sf.nightworks.Handler.wear_bit_name;
import static net.sf.nightworks.Handler.wiznet_lookup;
import static net.sf.nightworks.Interp.interpret;
import static net.sf.nightworks.Nightworks.ACT_AGGRESSIVE;
import static net.sf.nightworks.Nightworks.ACT_NOPURGE;
import static net.sf.nightworks.Nightworks.AC_BASH;
import static net.sf.nightworks.Nightworks.AC_EXOTIC;
import static net.sf.nightworks.Nightworks.AC_PIERCE;
import static net.sf.nightworks.Nightworks.AC_SLASH;
import static net.sf.nightworks.Nightworks.AFFECT_DATA;
import static net.sf.nightworks.Nightworks.ANGEL;
import static net.sf.nightworks.Nightworks.AREA_DATA;
import static net.sf.nightworks.Nightworks.AVATAR;
import static net.sf.nightworks.Nightworks.CABAL_BATTLE;
import static net.sf.nightworks.Nightworks.CABAL_CHAOS;
import static net.sf.nightworks.Nightworks.CABAL_HUNTER;
import static net.sf.nightworks.Nightworks.CABAL_INVADER;
import static net.sf.nightworks.Nightworks.CABAL_KNIGHT;
import static net.sf.nightworks.Nightworks.CABAL_LIONS;
import static net.sf.nightworks.Nightworks.CABAL_NONE;
import static net.sf.nightworks.Nightworks.CABAL_RULER;
import static net.sf.nightworks.Nightworks.CABAL_SHALAFI;
import static net.sf.nightworks.Nightworks.CANT_CHANGE_TITLE;
import static net.sf.nightworks.Nightworks.CAN_WEAR;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.COMM_NOCHANNELS;
import static net.sf.nightworks.Nightworks.COMM_NOEMOTE;
import static net.sf.nightworks.Nightworks.COMM_NOSHOUT;
import static net.sf.nightworks.Nightworks.COMM_NOTELL;
import static net.sf.nightworks.Nightworks.COMM_SNOOP_PROOF;
import static net.sf.nightworks.Nightworks.COND_BLOODLUST;
import static net.sf.nightworks.Nightworks.COND_DESIRE;
import static net.sf.nightworks.Nightworks.COND_DRUNK;
import static net.sf.nightworks.Nightworks.COND_FULL;
import static net.sf.nightworks.Nightworks.COND_HUNGER;
import static net.sf.nightworks.Nightworks.COND_THIRST;
import static net.sf.nightworks.Nightworks.CON_PLAYING;
import static net.sf.nightworks.Nightworks.DEMI;
import static net.sf.nightworks.Nightworks.DESCRIPTOR_DATA;
import static net.sf.nightworks.Nightworks.DICE_NUMBER;
import static net.sf.nightworks.Nightworks.DICE_TYPE;
import static net.sf.nightworks.Nightworks.ETHOS_CHAOTIC;
import static net.sf.nightworks.Nightworks.ETHOS_LAWFUL;
import static net.sf.nightworks.Nightworks.ETHOS_NEUTRAL;
import static net.sf.nightworks.Nightworks.EXIT_DATA;
import static net.sf.nightworks.Nightworks.EXTRA_DESCR_DATA;
import static net.sf.nightworks.Nightworks.GET_AC;
import static net.sf.nightworks.Nightworks.GET_DAMROLL;
import static net.sf.nightworks.Nightworks.GET_HITROLL;
import static net.sf.nightworks.Nightworks.GOD;
import static net.sf.nightworks.Nightworks.IMMORTAL;
import static net.sf.nightworks.Nightworks.IMPLEMENTOR;
import static net.sf.nightworks.Nightworks.IS_EVIL;
import static net.sf.nightworks.Nightworks.IS_GOOD;
import static net.sf.nightworks.Nightworks.IS_IMMORTAL;
import static net.sf.nightworks.Nightworks.IS_NEUTRAL;
import static net.sf.nightworks.Nightworks.IS_NPC;
import static net.sf.nightworks.Nightworks.IS_OBJ_STAT;
import static net.sf.nightworks.Nightworks.IS_SET;
import static net.sf.nightworks.Nightworks.IS_TRUSTED;
import static net.sf.nightworks.Nightworks.ITEM_ARMOR;
import static net.sf.nightworks.Nightworks.ITEM_CONTAINER;
import static net.sf.nightworks.Nightworks.ITEM_DRINK_CON;
import static net.sf.nightworks.Nightworks.ITEM_GLOW;
import static net.sf.nightworks.Nightworks.ITEM_NOPURGE;
import static net.sf.nightworks.Nightworks.ITEM_PILL;
import static net.sf.nightworks.Nightworks.ITEM_POTION;
import static net.sf.nightworks.Nightworks.ITEM_SCROLL;
import static net.sf.nightworks.Nightworks.ITEM_STAFF;
import static net.sf.nightworks.Nightworks.ITEM_TAKE;
import static net.sf.nightworks.Nightworks.ITEM_WAND;
import static net.sf.nightworks.Nightworks.ITEM_WEAPON;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_HEAD;
import static net.sf.nightworks.Nightworks.ITEM_WIELD;
import static net.sf.nightworks.Nightworks.LEVEL_HERO;
import static net.sf.nightworks.Nightworks.LEVEL_IMMORTAL;
import static net.sf.nightworks.Nightworks.MAX_CABAL;
import static net.sf.nightworks.Nightworks.MAX_LEVEL;
import static net.sf.nightworks.Nightworks.MAX_NEWBIES;
import static net.sf.nightworks.Nightworks.MAX_OLDIES;
import static net.sf.nightworks.Nightworks.MAX_SKILL;
import static net.sf.nightworks.Nightworks.MOB_INDEX_DATA;
import static net.sf.nightworks.Nightworks.MPROG_AREA;
import static net.sf.nightworks.Nightworks.MPROG_BRIBE;
import static net.sf.nightworks.Nightworks.MPROG_DEATH;
import static net.sf.nightworks.Nightworks.MPROG_ENTRY;
import static net.sf.nightworks.Nightworks.MPROG_FIGHT;
import static net.sf.nightworks.Nightworks.MPROG_GIVE;
import static net.sf.nightworks.Nightworks.MPROG_GREET;
import static net.sf.nightworks.Nightworks.MPROG_SPEECH;
import static net.sf.nightworks.Nightworks.NIGHTWORKS_REBOOT;
import static net.sf.nightworks.Nightworks.NIGHTWORKS_SHUTDOWN;
import static net.sf.nightworks.Nightworks.OBJ_DATA;
import static net.sf.nightworks.Nightworks.OBJ_INDEX_DATA;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SCHOOL_BANNER;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SCHOOL_SHIELD;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SCHOOL_VEST;
import static net.sf.nightworks.Nightworks.OPROG_AREA;
import static net.sf.nightworks.Nightworks.OPROG_DEATH;
import static net.sf.nightworks.Nightworks.OPROG_DROP;
import static net.sf.nightworks.Nightworks.OPROG_FIGHT;
import static net.sf.nightworks.Nightworks.OPROG_GET;
import static net.sf.nightworks.Nightworks.OPROG_GIVE;
import static net.sf.nightworks.Nightworks.OPROG_SAC;
import static net.sf.nightworks.Nightworks.OPROG_SPEECH;
import static net.sf.nightworks.Nightworks.ORG_RACE;
import static net.sf.nightworks.Nightworks.PERS;
import static net.sf.nightworks.Nightworks.PLR_CANINDUCT;
import static net.sf.nightworks.Nightworks.PLR_CANREMORT;
import static net.sf.nightworks.Nightworks.PLR_DENY;
import static net.sf.nightworks.Nightworks.PLR_FREEZE;
import static net.sf.nightworks.Nightworks.PLR_HOLYLIGHT;
import static net.sf.nightworks.Nightworks.PLR_LOG;
import static net.sf.nightworks.Nightworks.PLR_NO_TITLE;
import static net.sf.nightworks.Nightworks.PLR_QUESTOR;
import static net.sf.nightworks.Nightworks.POS_DEAD;
import static net.sf.nightworks.Nightworks.POS_SLEEPING;
import static net.sf.nightworks.Nightworks.POS_STANDING;
import static net.sf.nightworks.Nightworks.RACE_OK;
import static net.sf.nightworks.Nightworks.REMOVE_BIT;
import static net.sf.nightworks.Nightworks.ROOM_HISTORY_DATA;
import static net.sf.nightworks.Nightworks.ROOM_INDEX_DATA;
import static net.sf.nightworks.Nightworks.SET_BIT;
import static net.sf.nightworks.Nightworks.SET_ORG_RACE;
import static net.sf.nightworks.Nightworks.SEX_FEMALE;
import static net.sf.nightworks.Nightworks.SEX_MALE;
import static net.sf.nightworks.Nightworks.STAT_CHA;
import static net.sf.nightworks.Nightworks.STAT_CON;
import static net.sf.nightworks.Nightworks.STAT_DEX;
import static net.sf.nightworks.Nightworks.STAT_INT;
import static net.sf.nightworks.Nightworks.STAT_STR;
import static net.sf.nightworks.Nightworks.STAT_WIS;
import static net.sf.nightworks.Nightworks.TO_AFFECTS;
import static net.sf.nightworks.Nightworks.TO_CHAR;
import static net.sf.nightworks.Nightworks.TO_IMMUNE;
import static net.sf.nightworks.Nightworks.TO_NOTVICT;
import static net.sf.nightworks.Nightworks.TO_OBJECT;
import static net.sf.nightworks.Nightworks.TO_RESIST;
import static net.sf.nightworks.Nightworks.TO_ROOM;
import static net.sf.nightworks.Nightworks.TO_VICT;
import static net.sf.nightworks.Nightworks.TO_VULN;
import static net.sf.nightworks.Nightworks.TO_WEAPON;
import static net.sf.nightworks.Nightworks.UMIN;
import static net.sf.nightworks.Nightworks.WEAPON_ARROW;
import static net.sf.nightworks.Nightworks.WEAPON_AXE;
import static net.sf.nightworks.Nightworks.WEAPON_BOW;
import static net.sf.nightworks.Nightworks.WEAPON_DAGGER;
import static net.sf.nightworks.Nightworks.WEAPON_EXOTIC;
import static net.sf.nightworks.Nightworks.WEAPON_FLAIL;
import static net.sf.nightworks.Nightworks.WEAPON_LANCE;
import static net.sf.nightworks.Nightworks.WEAPON_MACE;
import static net.sf.nightworks.Nightworks.WEAPON_POLEARM;
import static net.sf.nightworks.Nightworks.WEAPON_SPEAR;
import static net.sf.nightworks.Nightworks.WEAPON_SWORD;
import static net.sf.nightworks.Nightworks.WEAPON_WHIP;
import static net.sf.nightworks.Nightworks.WEAR_BODY;
import static net.sf.nightworks.Nightworks.WIZ_LOAD;
import static net.sf.nightworks.Nightworks.WIZ_ON;
import static net.sf.nightworks.Nightworks.WIZ_PENALTIES;
import static net.sf.nightworks.Nightworks.WIZ_PREFIX;
import static net.sf.nightworks.Nightworks.WIZ_RESTORE;
import static net.sf.nightworks.Nightworks.WIZ_SECURE;
import static net.sf.nightworks.Nightworks.WIZ_SNOOPS;
import static net.sf.nightworks.Nightworks.WIZ_SWITCHES;
import static net.sf.nightworks.Nightworks.area_first;
import static net.sf.nightworks.Nightworks.atoi;
import static net.sf.nightworks.Nightworks.char_list;
import static net.sf.nightworks.Nightworks.current_time;
import static net.sf.nightworks.Nightworks.descriptor_list;
import static net.sf.nightworks.Nightworks.fLogAll;
import static net.sf.nightworks.Nightworks.get_carry_weight;
import static net.sf.nightworks.Nightworks.iNumPlayers;
import static net.sf.nightworks.Nightworks.max_newbies;
import static net.sf.nightworks.Nightworks.max_oldies;
import static net.sf.nightworks.Nightworks.nw_config;
import static net.sf.nightworks.Nightworks.object_list;
import static net.sf.nightworks.Nightworks.reboot_counter;
import static net.sf.nightworks.Nightworks.sprintf;
import static net.sf.nightworks.Nightworks.time_sync;
import static net.sf.nightworks.Nightworks.top_affected_room;
import static net.sf.nightworks.Nightworks.total_levels;
import static net.sf.nightworks.Save.save_char_obj;
import static net.sf.nightworks.Skill.gsn_blindness;
import static net.sf.nightworks.Skill.gsn_curse;
import static net.sf.nightworks.Skill.gsn_plague;
import static net.sf.nightworks.Skill.gsn_poison;
import static net.sf.nightworks.Skill.gsn_sleep;
import static net.sf.nightworks.Skill.lookupSkill;
import static net.sf.nightworks.Special.spec_lookup;
import static net.sf.nightworks.Special.spec_name;
import static net.sf.nightworks.Tables.cabal_table;
import static net.sf.nightworks.Tables.position_table;
import static net.sf.nightworks.Tables.sex_table;
import static net.sf.nightworks.Tables.size_table;
import static net.sf.nightworks.Update.advance_level;
import static net.sf.nightworks.Update.char_update;
import static net.sf.nightworks.Update.obj_update;
import static net.sf.nightworks.Update.room_update;
import static net.sf.nightworks.Update.track_update;
import static net.sf.nightworks.util.TextUtils.UPPER;
import static net.sf.nightworks.util.TextUtils.capitalize;
import static net.sf.nightworks.util.TextUtils.is_number;
import static net.sf.nightworks.util.TextUtils.one_argument;
import static net.sf.nightworks.util.TextUtils.smash_tilde;
import static net.sf.nightworks.util.TextUtils.str_cmp;
import static net.sf.nightworks.util.TextUtils.str_prefix;

class ActWiz {
    static void do_cabal_scan(CHAR_DATA ch) {
        int i;
        OBJ_DATA in_obj;
        int show;

        if (!IS_IMMORTAL(ch) && (ch.cabal == CABAL_NONE || IS_NPC(ch))) {
            send_to_char("You are not a cabal member yet.\n", ch);
            return;
        }
        TextBuffer buf1 = new TextBuffer();
        TextBuffer buf2 = new TextBuffer();
        for (i = 1; i < MAX_CABAL; i++) {
            if (IS_IMMORTAL(ch) || ch.cabal == i) {
                show = 1;
            } else {
                show = 0;
            }
            sprintf(buf1, " Cabal: %-11s, room %4d, item %4d, ptr: %-20s ",
                    cabal_table[i].short_name,
                    cabal_table[i].room_vnum,
                    cabal_table[i].obj_vnum,
                    cabal_table[i].obj_ptr != null ?
                            cabal_table[i].obj_ptr.short_descr : "(NULL)");
            if (cabal_table[i].obj_ptr != null) {
                for (in_obj = cabal_table[i].obj_ptr; in_obj.in_obj != null; in_obj = in_obj.in_obj) {
                }
                if (in_obj.carried_by != null) {
                    sprintf(buf2, "\n\t\tcarried_by: %s\n", PERS(in_obj.carried_by, ch));
                } else {
                    sprintf(buf2, "\n\t\t\t\t\tin_room: %s\n", in_obj.in_room != null ?
                            in_obj.in_room.name : "BUG!!");
                    if (in_obj.in_room != null
                            && in_obj.in_room.vnum == cabal_table[ch.cabal].room_vnum) {
                        show = 1;
                    }
                }
            }

            if (show != 0) {
                send_to_char(buf1, ch);
                send_to_char(buf2, ch);
            }
        }
    }

    static void do_objlist(CHAR_DATA ch) {
        OBJ_DATA obj;
        try {
            try (FileWriter fp = new FileWriter("objlist.txt")) {
                Formatter ff = new Formatter(fp);
                for (obj = object_list; obj != null; obj = obj.next) {
                    if (CAN_WEAR(obj, ITEM_WIELD) && (obj.level < 25 && obj.level > 15)) {
                        ff.format("\n#Obj: %s (Vnum : %d) \n", obj.short_descr, obj.pIndexData.vnum);
                        ff.format("Object '%s' is type %s, extra flags %s.\nWeight is %d, value is %d, level is %d.\n",

                                obj.name,
                                item_type_name(obj),
                                extra_bit_name(obj.extra_flags),
                                obj.weight / 10,
                                obj.cost,
                                obj.level
                        );

                        switch (obj.item_type) {
                            case ITEM_SCROLL:
                            case ITEM_POTION:
                            case ITEM_PILL:
                                ff.format("Level %d spells of:", obj.value[0]);

                                if (obj.value[1] >= 0 && obj.value[1] < MAX_SKILL) {
                                    ff.format(" '%s'", Skill.skills[obj.value[1]].name);
                                }

                                if (obj.value[2] >= 0 && obj.value[2] < MAX_SKILL) {
                                    ff.format(" '%s'", Skill.skills[obj.value[2]].name);
                                }

                                if (obj.value[3] >= 0 && obj.value[3] < MAX_SKILL) {
                                    ff.format(" '%s'", Skill.skills[obj.value[3]].name);
                                }

                                if (obj.value[4] >= 0 && obj.value[4] < MAX_SKILL) {
                                    ff.format(" '%s'", Skill.skills[obj.value[4]].name);
                                }

                                ff.format(".\n");
                                break;

                            case ITEM_WAND:
                            case ITEM_STAFF:
                                ff.format("Has %d charges of level %d", obj.value[2], obj.value[0]);

                                if (obj.value[3] >= 0 && obj.value[3] < MAX_SKILL) {
                                    ff.format(" '%s'", Skill.skills[obj.value[3]].name);
                                }

                                ff.format(".\n");
                                break;

                            case ITEM_DRINK_CON:
                                ff.format("It holds %s-colored %s.\n",
                                        liq_table[obj.value[2]].liq_color,
                                        liq_table[obj.value[2]].liq_name);
                                break;

                            case ITEM_CONTAINER:
                                ff.format("Capacity: %d#  Maximum weight: %d#  flags: %s\n",
                                        obj.value[0], obj.value[3], cont_bit_name(obj.value[1]));
                                if (obj.value[4] != 100) {
                                    ff.format("Weight multiplier: %d%%\n",
                                            obj.value[4]);
                                }
                                break;

                            case ITEM_WEAPON:
                                ff.format("Weapon type is ");
                                switch (obj.value[0]) {
                                    case (WEAPON_EXOTIC):
                                        ff.format("exotic.\n");
                                        break;
                                    case (WEAPON_SWORD):
                                        ff.format("sword.\n");
                                        break;
                                    case (WEAPON_DAGGER):
                                        ff.format("dagger.\n");
                                        break;
                                    case (WEAPON_SPEAR):
                                        ff.format("spear/staff.\n");
                                        break;
                                    case (WEAPON_MACE):
                                        ff.format("mace/club.\n");
                                        break;
                                    case (WEAPON_AXE):
                                        ff.format("axe.\n");
                                        break;
                                    case (WEAPON_FLAIL):
                                        ff.format("flail.\n");
                                        break;
                                    case (WEAPON_WHIP):
                                        ff.format("whip.\n");
                                        break;
                                    case (WEAPON_POLEARM):
                                        ff.format("polearm.\n");
                                        break;
                                    case (WEAPON_BOW):
                                        ff.format("bow.\n");
                                        break;
                                    case (WEAPON_ARROW):
                                        ff.format("arrow.\n");
                                        break;
                                    case (WEAPON_LANCE):
                                        ff.format("lance.\n");
                                        break;
                                    default:
                                        ff.format("unknown.\n");
                                        break;
                                }
                                if (obj.pIndexData.new_format) {
                                    ff.format("Damage is %dd%d (average %d).\n",
                                            obj.value[1], obj.value[2],
                                            (1 + obj.value[2]) * obj.value[1] / 2);
                                } else {
                                    ff.format("Damage is %d to %d (average %d).\n",
                                            obj.value[1], obj.value[2],
                                            (obj.value[1] + obj.value[2]) / 2);
                                }
                                if (obj.value[4] != 0)  /* weapon flags */ {
                                    ff.format("Weapons flags: %s\n", weapon_bit_name(obj.value[4]));
                                }
                                break;

                            case ITEM_ARMOR:
                                ff.format("Armor class is %d pierce, %d bash, %d slash, and %d vs. magic.\n",
                                        obj.value[0], obj.value[1], obj.value[2], obj.value[3]);
                                break;
                        }
                        for (AFFECT_DATA paf = obj.pIndexData.affected; paf != null; paf = paf.next) {
                            ff.format("  Affects %s by %d.\n", affect_loc_name(paf.location), paf.modifier);
                            if (paf.bitvector != 0) {
                                switch (paf.where) {
                                    case TO_AFFECTS:
                                        ff.format("   Adds %s affect.\n", affect_bit_name(paf.bitvector));
                                        break;
                                    case TO_OBJECT:
                                        ff.format("   Adds %s object flag.\n", extra_bit_name(paf.bitvector));
                                        break;
                                    case TO_IMMUNE:
                                        ff.format("   Adds immunity to %s.\n", imm_bit_name(paf.bitvector));
                                        break;
                                    case TO_RESIST:
                                        ff.format("   Adds resistance to %s.\n", imm_bit_name(paf.bitvector));
                                        break;
                                    case TO_VULN:
                                        ff.format("   Adds vulnerability to %s.\n", imm_bit_name(paf.bitvector));
                                        break;
                                    default:
                                        ff.format("   Unknown bit %d: %d\n", paf.where, paf.bitvector);
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            send_to_char("File error.\n", ch);
            e.printStackTrace();
        }
    }


    static void do_limited(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        OBJ_INDEX_DATA obj_index;
        int lCount = 0;
        int ingameCount;
        int vnum;
        int nMatch;

        TextBuffer buf = new TextBuffer();
        if (argument.length() != 0) {
            obj_index = get_obj_index(atoi(argument));
            if (obj_index == null) {
                send_to_char("Not found.\n", ch);
                return;
            }
            if (obj_index.limit == -1) {
                send_to_char("Thats not a limited item.\n", ch);
                return;
            }

            buf.sprintf("%-35s [%5d]  Limit: %3d  Current: %3d\n",
                    obj_index.short_descr,
                    obj_index.vnum,
                    obj_index.limit,
                    obj_index.count);
            buf.getBuffer().setCharAt(0, buf.getBuffer().charAt(0));
            send_to_char(buf, ch);
            ingameCount = 0;
            for (obj = object_list; obj != null; obj = obj.next) {
                if (obj.pIndexData.vnum == obj_index.vnum) {
                    ingameCount++;
                    if (obj.carried_by != null) {
                        buf.sprintf("Carried by %-30s\n", obj.carried_by.name);
                    }
                    if (obj.in_room != null) {
                        buf.sprintf("At %-20s [%d]\n", obj.in_room.name, obj.in_room.vnum);
                    }
                    if (obj.in_obj != null) {
                        buf.sprintf("In %-20s [%d] \n", obj.in_obj.short_descr, obj.in_obj.pIndexData.vnum);
                    }
                    send_to_char(buf, ch);
                }
            }
            buf.sprintf("  %d found in game. %d should be in pFiles.\n", ingameCount, obj_index.count - ingameCount);
            send_to_char(buf, ch);
            return;
        }

        nMatch = 0;
        TextBuffer output = new TextBuffer();
        for (vnum = 0; nMatch < top_obj_index; vnum++) {
            if ((obj_index = get_obj_index(vnum)) != null) {
                nMatch++;
                if (obj_index.limit != -1) {
                    lCount++;
                    buf.sprintf("%-37s [%5d]  Limit: %3d  Current: %3d\n",
                            obj_index.short_descr,
                            obj_index.vnum,
                            obj_index.limit,
                            obj_index.count);
                    buf.getBuffer().setCharAt(0, buf.getBuffer().charAt(0));
                    output.append(buf.getBuffer());
                }
            }
        }
        buf.sprintf("\n%d of %d objects are limited.\n", lCount, nMatch);
        output.append(buf.getBuffer());
        page_to_char(output, ch);
    }

    static void do_wiznet(CHAR_DATA ch, String argument) {

        if (argument.length() == 0) {
            if (IS_SET(ch.wiznet, WIZ_ON)) {
                send_to_char("Signing off of Wiznet.\n", ch);
                ch.wiznet = REMOVE_BIT(ch.wiznet, WIZ_ON);
            } else {
                send_to_char("Welcome to Wiznet!\n", ch);
                ch.wiznet = SET_BIT(ch.wiznet, WIZ_ON);
            }
            return;
        }

        if (!str_prefix(argument, "on")) {
            send_to_char("Welcome to Wiznet!\n", ch);
            ch.wiznet = SET_BIT(ch.wiznet, WIZ_ON);
            return;
        }

        if (!str_prefix(argument, "off")) {
            send_to_char("Signing off of Wiznet.\n", ch);
            ch.wiznet = REMOVE_BIT(ch.wiznet, WIZ_ON);
            return;
        }

        TextBuffer buf = new TextBuffer();

        /* show wiznet status */
        if (!str_prefix(argument, "status")) {
            if (!IS_SET(ch.wiznet, WIZ_ON)) {
                buf.append("off ");
            }

            for (int flag = 0; wiznet_table[flag].name != null; flag++) {
                if (IS_SET(ch.wiznet, wiznet_table[flag].flag)) {
                    buf.append(wiznet_table[flag].name);
                    buf.append(" ");
                }
            }

            buf.append("\n");

            send_to_char("Wiznet status:\n", ch);
            send_to_char(buf, ch);
            return;
        }

        if (!str_prefix(argument, "show"))
            /* list of all wiznet options */ {
            buf.clear();

            for (int flag = 0; wiznet_table[flag].name != null; flag++) {
                if (wiznet_table[flag].level <= get_trust(ch)) {
                    buf.append(wiznet_table[flag].name);
                    buf.append(" ");
                }
            }

            buf.append("\n");

            send_to_char("Wiznet options available to you are:\n", ch);
            send_to_char(buf, ch);
            return;
        }

        int flag = wiznet_lookup(argument);

        if (flag == -1 || get_trust(ch) < wiznet_table[flag].level) {
            send_to_char("No such option.\n", ch);
            return;
        }

        if (IS_SET(ch.wiznet, wiznet_table[flag].flag)) {
            buf.sprintf("You will no longer see %s on wiznet.\n", wiznet_table[flag].name);
            send_to_char(buf, ch);
            ch.wiznet = REMOVE_BIT(ch.wiznet, wiznet_table[flag].flag);
        } else {
            buf.sprintf("You will now see %s on wiznet.\n", wiznet_table[flag].name);
            send_to_char(buf, ch);
            ch.wiznet = SET_BIT(ch.wiznet, wiznet_table[flag].flag);
        }

    }

    static void wiznet(TextBuffer buf, CHAR_DATA ch, OBJ_DATA obj, int flag, int flag_skip, int min_level) {
        wiznet(buf.toString(), ch, obj, flag, flag_skip, min_level);
    }

    static void wiznet(StringBuilder buf, CHAR_DATA ch, OBJ_DATA obj, int flag, int flag_skip, int min_level) {
        wiznet(buf.toString(), ch, obj, flag, flag_skip, min_level);
    }

    static void wiznet(String string, CHAR_DATA ch, OBJ_DATA obj, int flag, int flag_skip, int min_level) {
        DESCRIPTOR_DATA d;

        for (d = descriptor_list; d != null; d = d.next) {
            if (d.connected == CON_PLAYING
                    && IS_IMMORTAL(d.character)
                    && IS_SET(d.character.wiznet, WIZ_ON)
                    && (flag == 0 || IS_SET(d.character.wiznet, flag))
                    && (flag_skip == 0 || !IS_SET(d.character.wiznet, flag_skip))
                    && get_trust(d.character) >= min_level
                    && d.character != ch) {
                if (IS_SET(d.character.wiznet, WIZ_PREFIX)) {
                    send_to_char("-. ", d.character);
                }
                act(string, d.character, obj, ch, TO_CHAR, POS_DEAD);
            }
        }

    }

    static void do_tick(CHAR_DATA ch, String argument) {
        StringBuilder argb = new StringBuilder();
        one_argument(argument, argb);
        String arg = argb.toString();
        if (arg.length() == 0) {
            send_to_char("tick area : area update\n", ch);
            send_to_char("tick char : char update\n", ch);
            send_to_char("tick obj  : obj  update\n", ch);
            send_to_char("tick room : room update\n", ch);
            send_to_char("tick track: track update\n", ch);
            return;
        }
        if (is_name(arg, "area")) {
            area_update();
            send_to_char("Area updated.\n", ch);
            return;
        }
        if (is_name(arg, "char player")) {
            char_update();
            send_to_char("Players updated.\n", ch);
            return;
        }
        if (is_name(arg, "obj")) {
            obj_update();
            send_to_char("Obj updated.\n", ch);
            return;
        }
        if (is_name(arg, "room")) {
            room_update();
            send_to_char("Room updated.\n", ch);
            return;
        }
        if (is_name(arg, "track")) {
            track_update();
            send_to_char("Track updated.\n", ch);
            return;
        }
        do_tick(ch, "");
    }

/* equips a character */

    static void do_outfit(CHAR_DATA ch) {
        OBJ_DATA obj;
        int vnum;

        if ((ch.level > 5 || IS_NPC(ch)) && !IS_IMMORTAL(ch)) {
            send_to_char("Find it yourself!\n", ch);
            return;
        }

        if (ch.carry_number + 1 > can_carry_n(ch)) {
            send_to_char("You can't carry that many items.\n", ch);
            return;
        }

        if (get_light_char(ch) == null) {
            obj = create_object(get_obj_index(OBJ_VNUM_SCHOOL_BANNER), 0);
            obj.cost = 0;
            obj.condition = 100;
            obj_to_char(obj, ch);
        }

        if (ch.carry_number + 1 > can_carry_n(ch)) {
            send_to_char("You can't carry that many items.\n", ch);
            return;
        }

        if (get_eq_char(ch, WEAR_BODY) == null) {
            obj = create_object(get_obj_index(OBJ_VNUM_SCHOOL_VEST), 0);
            obj.cost = 0;
            obj.condition = 100;
            obj_to_char(obj, ch);
        }


        if (ch.carry_number + 1 > can_carry_n(ch)) {
            send_to_char("You can't carry that many items.\n", ch);
            return;
        }

        /* do the weapon thing */
        if (get_wield_char(ch, false) == null) {
            vnum = ch.clazz.weapon;
            obj = create_object(get_obj_index(vnum), 0);
            obj.condition = 100;
            obj_to_char(obj, ch);
        }

        obj = create_object(get_obj_index(OBJ_VNUM_SCHOOL_SHIELD), 0);
        obj.cost = 0;
        obj.condition = 100;
        obj_to_char(obj, ch);

        send_to_char("You have been given some equipments by gods.\n", ch);
        send_to_char("Type 'inventory' to see the list of the objects that you are carrying.\n", ch);
        send_to_char("Try 'wear <object name>' to wear the object.\r\n\n", ch);
    }

/* RT nochannels command, for those spammers */

    static void do_nochannels(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0) {
            send_to_char("Nochannel whom?", ch);
            return;
        }

        if ((victim = get_char_world(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (get_trust(victim) >= get_trust(ch)) {
            send_to_char("You failed.\n", ch);
            return;
        }

        TextBuffer buf = new TextBuffer();
        if (IS_SET(victim.comm, COMM_NOCHANNELS)) {
            victim.comm = REMOVE_BIT(victim.comm, COMM_NOCHANNELS);
            send_to_char("The gods have restored your channel priviliges.\n",
                    victim);
            send_to_char("NOCHANNELS removed.\n", ch);
            buf.sprintf("$N restores channels to %s", victim.name);
            wiznet(buf, ch, null, WIZ_PENALTIES, WIZ_SECURE, 0);
        } else {
            victim.comm = SET_BIT(victim.comm, COMM_NOCHANNELS);
            send_to_char("The gods have revoked your channel priviliges.\n",
                    victim);
            send_to_char("NOCHANNELS set.\n", ch);
            buf.sprintf("$N revokes %s's channels.", victim.name);
            wiznet(buf, ch, null, WIZ_PENALTIES, WIZ_SECURE, 0);
        }

    }


    static void do_smote(CHAR_DATA ch, String argument) {
        CHAR_DATA vch;
        int matches = 0;

        if (!IS_NPC(ch) && IS_SET(ch.comm, COMM_NOEMOTE)) {
            send_to_char("You can't show your emotions.\n", ch);
            return;
        }

        if (argument.length() == 0) {
            send_to_char("Emote what?\n", ch);
            return;
        }

        if (argument.contains(ch.name)) {
            send_to_char("You must include your name in an smote.\n", ch);
            return;
        }

        send_to_char(argument, ch);
        send_to_char("\n", ch);

        for (vch = ch.in_room.people; vch != null; vch = vch.next_in_room) {
            if (vch.desc == null || vch == ch) {
                continue;
            }
            int letter = argument.indexOf(vch.name);
            if (letter == -1) {
                send_to_char(argument, vch);
                send_to_char("\n", vch);
                continue;
            }

            StringBuilder temp = new StringBuilder(argument.substring(0, letter));
            int namePos = 0;
            StringBuilder last = new StringBuilder();
            for (; letter < argument.length(); letter++) {
                char c = argument.charAt(letter);
                if (c == '\'' && matches == vch.name.length()) {
                    temp.append("r");
                    continue;
                }

                if (c == 's' && matches == vch.name.length()) {
                    matches = 0;
                    continue;
                }

                if (matches == vch.name.length()) {
                    matches = 0;
                }

                if (c == vch.name.charAt(namePos)) {
                    matches++;
                    namePos++;
                    if (matches == vch.name.length()) {
                        temp.append("you");
                        last.setLength(0);
                        namePos = 0;
                        continue;
                    }
                    last.append(letter);
                    continue;
                }

                matches = 0;
                temp.append(last).append(letter);
                last.setLength(0);
                namePos = 0;
            }

            send_to_char(temp, vch);
            send_to_char("\n", vch);
        }

    }

    static void do_bamfin(CHAR_DATA ch, String argument) {

        if (!IS_NPC(ch)) {
            argument = smash_tilde(argument);
            TextBuffer buf = new TextBuffer();
            if (argument.length() == 0) {
                buf.sprintf("Your poofin is %s\n", ch.pcdata.bamfin);
                send_to_char(buf, ch);
                return;
            }

            if (argument.contains(ch.name)) {
                send_to_char("You must include your name.\n", ch);
                return;
            }

            ch.pcdata.bamfin = argument;

            buf.sprintf("Your poofin is now %s\n", ch.pcdata.bamfin);
            send_to_char(buf, ch);
        }
    }


    static void do_bamfout(CHAR_DATA ch, String argument) {

        if (!IS_NPC(ch)) {
            TextBuffer buf = new TextBuffer();
            argument = smash_tilde(argument);

            if (argument.length() == 0) {
                buf.sprintf("Your poofout is %s\n", ch.pcdata.bamfout);
                send_to_char(buf, ch);
                return;
            }

            if (argument.contains(ch.name)) {
                send_to_char("You must include your name.\n", ch);
                return;
            }

            ch.pcdata.bamfout = argument;

            buf.sprintf("Your poofout is now %s\n", ch.pcdata.bamfout);
            send_to_char(buf, ch);
        }
    }


    static void do_deny(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);
        if (arg.length() == 0) {
            send_to_char("Deny whom?\n", ch);
            return;
        }

        if ((victim = get_char_world(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (IS_NPC(victim)) {
            send_to_char("Not on NPC's.\n", ch);
            return;
        }

        if (get_trust(victim) >= get_trust(ch)) {
            send_to_char("You failed.\n", ch);
            return;
        }

        victim.act = SET_BIT(victim.act, PLR_DENY);
        send_to_char("You are denied access!\n", victim);
        TextBuffer buf = new TextBuffer();
        buf.sprintf("$N denies access to %s", victim.name);
        wiznet(buf, ch, null, WIZ_PENALTIES, WIZ_SECURE, 0);
        send_to_char("OK.\n", ch);
        save_char_obj(victim);
        stop_fighting(victim, true);
        do_quit(victim);
    }


    static void do_disconnect(CHAR_DATA ch, String argument) {
        DESCRIPTOR_DATA d, d_next;
        CHAR_DATA victim;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);
        if (arg.length() == 0) {
            send_to_char("Disconnect whom?\n", ch);
            return;
        }

        if (is_number(arg)) {
            int desc;

            desc = atoi(arg.toString());
            for (d = descriptor_list; d != null; d = d_next) {
                d_next = d.next;
                if (d.descriptor.hashCode() == desc) {
                    close_socket(d);
                    send_to_char("Ok.\n", ch);
                    return;
                }
            }
        }

        if ((victim = get_char_world(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (victim.desc == null) {
            act("$N doesn't have a descriptor.", ch, null, victim, TO_CHAR);
            return;
        }

        for (d = descriptor_list; d != null; d = d_next) {
            d_next = d.next;
            if (d == victim.desc) {
                close_socket(d);
                send_to_char("Ok.\n", ch);
                return;
            }
        }

        bug("Do_disconnect: desc not found.");
        send_to_char("Descriptor not found!\n", ch);
    }


    static void do_echo(CHAR_DATA ch, String argument) {
        DESCRIPTOR_DATA d;

        if (argument.length() == 0) {
            send_to_char("Global echo what?\n", ch);
            return;
        }

        for (d = descriptor_list; d != null; d = d.next) {
            if (d.connected == CON_PLAYING) {
                if (get_trust(d.character) >= get_trust(ch)) {
                    send_to_char("global> ", d.character);
                }
                send_to_char(argument, d.character);
                send_to_char("\n", d.character);
            }
        }

    }


    static void do_recho(CHAR_DATA ch, String argument) {
        DESCRIPTOR_DATA d;

        if (argument.length() == 0) {
            send_to_char("Local echo what?\n", ch);

            return;
        }

        for (d = descriptor_list; d != null; d = d.next) {
            if (d.connected == CON_PLAYING
                    && d.character.in_room == ch.in_room) {
                if (get_trust(d.character) >= get_trust(ch)) {
                    send_to_char("local> ", d.character);
                }
                send_to_char(argument, d.character);
                send_to_char("\n", d.character);
            }
        }

    }

    static void do_zecho(CHAR_DATA ch, String argument) {
        DESCRIPTOR_DATA d;

        if (argument.length() == 0) {
            send_to_char("Zone echo what?\n", ch);
            return;
        }

        for (d = descriptor_list; d != null; d = d.next) {
            if (d.connected == CON_PLAYING
                    && d.character.in_room != null && ch.in_room != null
                    && d.character.in_room.area == ch.in_room.area) {
                if (get_trust(d.character) >= get_trust(ch)) {
                    send_to_char("zone> ", d.character);
                }
                send_to_char(argument, d.character);
                send_to_char("\n", d.character);
            }
        }
    }

    static void do_pecho(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        StringBuilder arg = new StringBuilder();
        argument = one_argument(argument, arg);

        if (argument.length() == 0 || arg.length() == 0) {
            send_to_char("Personal echo what?\n", ch);
            return;
        }

        if ((victim = get_char_world(ch, arg.toString())) == null) {
            send_to_char("Target not found.\n", ch);
            return;
        }

        if (get_trust(victim) >= get_trust(ch) && get_trust(ch) != MAX_LEVEL) {
            send_to_char("personal> ", victim);
        }

        send_to_char(argument, victim);
        send_to_char("\n", victim);
        send_to_char("personal> ", ch);
        send_to_char(argument, ch);
        send_to_char("\n", ch);
    }


    static ROOM_INDEX_DATA find_location(CHAR_DATA ch, String arg) {
        CHAR_DATA victim;
        OBJ_DATA obj;

        if (is_number(arg)) {
            return get_room_index(atoi(arg));
        }

        if ((victim = get_char_world(ch, arg)) != null) {
            return victim.in_room;
        }

        if ((obj = get_obj_world(ch, arg)) != null) {
            return obj.in_room;
        }

        return null;
    }


    static void do_transfer(CHAR_DATA ch, String argument) {
        ROOM_INDEX_DATA location;
        DESCRIPTOR_DATA d, d_next;
        CHAR_DATA victim;

        StringBuilder arg1 = new StringBuilder();
        StringBuilder arg2 = new StringBuilder();
        argument = one_argument(argument, arg1);
        one_argument(argument, arg2);

        if (arg1.length() == 0) {
            send_to_char("Transfer whom (and where)?\n", ch);
            return;
        }

        if (!str_cmp(arg1.toString(), "all")) {
            for (d = descriptor_list; d != null; d = d_next) {
                d_next = d.next;
                if (d.connected == CON_PLAYING
                        && d.character != ch
                        && d.character.in_room != null
                        && can_see(ch, d.character)) {
                    do_transfer(ch, d.character.name + " " + arg2);
                }
            }
            return;
        }

        /*
         * Thanks to Grodyn for the optional location parameter.
         */
        if (arg2.length() == 0) {
            location = ch.in_room;
        } else {
            if ((location = find_location(ch, arg2.toString())) == null) {
                send_to_char("No such location.\n", ch);
                return;
            }

/*  if ( !is_room_owner(ch,location) && room_is_private( location ) */
            if (room_is_private(location)
                    && get_trust(ch) < MAX_LEVEL) {
                send_to_char("That room is private right now.\n", ch);
                return;
            }
        }

        if ((victim = get_char_world(ch, arg1.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (victim.in_room == null) {
            send_to_char("They are in limbo.\n", ch);
            return;
        }

        if (victim.fighting != null) {
            stop_fighting(victim, true);
        }
        act("$n disappears in a mushroom cloud.", victim, null, null, TO_ROOM);
        char_from_room(victim);
        char_to_room(victim, location);
        act("$n arrives from a puff of smoke.", victim, null, null, TO_ROOM);
        if (ch != victim) {
            act("$n has transferred you.", ch, null, victim, TO_VICT);
        }
        do_look(victim, "auto");
        send_to_char("Ok.\n", ch);
    }


    static void do_at(CHAR_DATA ch, String argument) {
        ROOM_INDEX_DATA location;
        ROOM_INDEX_DATA original;
        OBJ_DATA on;
        CHAR_DATA wch, wch_next;

        StringBuilder arg = new StringBuilder();
        argument = one_argument(argument, arg);

        if (arg.length() == 0 || argument.length() == 0) {
            send_to_char("At where what?\n", ch);
            return;
        }

        if ((location = find_location(ch, arg.toString())) == null) {
            send_to_char("No such location.\n", ch);
            return;
        }

/*    if (!is_room_owner(ch,location) && room_is_private( location ) */
        if (room_is_private(location)
                && get_trust(ch) < MAX_LEVEL) {
            send_to_char("That room is private right now.\n", ch);
            return;
        }

        original = ch.in_room;
        on = ch.on;
        char_from_room(ch);
        char_to_room(ch, location);
        interpret(ch, argument, false);

        /*
         * See if 'ch' still exists before continuing!
         * Handles 'at XXXX quit' case.
         */
        for (wch = char_list; wch != null; wch = wch_next) {
            wch_next = wch.next;
            if (wch == ch) {
                char_from_room(ch);
                char_to_room(ch, original);
                ch.on = on;
                break;
            }
        }

    }


    static void do_goto(CHAR_DATA ch, String argument) {
        ROOM_INDEX_DATA location;
        CHAR_DATA rch;

        if (argument.length() == 0) {
            send_to_char("Goto where?\n", ch);
            return;
        }

        if ((location = find_location(ch, argument)) == null) {
            send_to_char("No such location.\n", ch);
            return;
        }

/*        int count = 0;
        for ( rch = location.people; rch != null; rch = rch.next_in_room )
            count++;

    if (!is_room_owner(ch,location) && room_is_private(location)
    &&  (count > 1 || get_trust(ch) < MAX_LEVEL))
    {
    send_to_char( "That room is private right now.\n", ch );
    return;
    } */

        if (ch.fighting != null) {
            stop_fighting(ch, true);
        }

        for (rch = ch.in_room.people; rch != null; rch = rch.next_in_room) {
            if (get_trust(rch) >= ch.invis_level) {
                if (ch.pcdata != null && ch.pcdata.bamfout.length() != 0) {
                    act("$t", ch, ch.pcdata.bamfout, rch, TO_VICT);
                } else {
                    act("$n leaves in a swirling mist.", ch, null, rch, TO_VICT);
                }
            }
        }

        char_from_room(ch);
        char_to_room(ch, location);


        for (rch = ch.in_room.people; rch != null; rch = rch.next_in_room) {
            if (get_trust(rch) >= ch.invis_level) {
                if (ch.pcdata != null && ch.pcdata.bamfin.length() != 0) {
                    act("$t", ch, ch.pcdata.bamfin, rch, TO_VICT);
                } else {
                    act("$n appears in a swirling mist.", ch, null, rch, TO_VICT);
                }
            }
        }

        do_look(ch, "auto");
    }

    static void do_violate(CHAR_DATA ch, String argument) {
        ROOM_INDEX_DATA location;
        CHAR_DATA rch;

        if (argument.length() == 0) {
            send_to_char("Goto where?\n", ch);
            return;
        }

        if ((location = find_location(ch, argument)) == null) {
            send_to_char("No such location.\n", ch);
            return;
        }

        if (!room_is_private(location)) {
            send_to_char("That room isn't private, use goto.\n", ch);
            return;
        }

        if (ch.fighting != null) {
            stop_fighting(ch, true);
        }

        for (rch = ch.in_room.people; rch != null; rch = rch.next_in_room) {
            if (get_trust(rch) >= ch.invis_level) {
                if (ch.pcdata != null && ch.pcdata.bamfout.length() != 0) {
                    act("$t", ch, ch.pcdata.bamfout, rch, TO_VICT);
                } else {
                    act("$n leaves in a swirling mist.", ch, null, rch, TO_VICT);
                }
            }
        }

        char_from_room(ch);
        char_to_room(ch, location);


        for (rch = ch.in_room.people; rch != null; rch = rch.next_in_room) {
            if (get_trust(rch) >= ch.invis_level) {
                if (ch.pcdata != null && ch.pcdata.bamfin.length() != 0) {
                    act("$t", ch, ch.pcdata.bamfin, rch, TO_VICT);
                } else {
                    act("$n appears in a swirling mist.", ch, null, rch, TO_VICT);
                }
            }
        }

        do_look(ch, "auto");
    }

/* RT to replace the 3 stat commands */

    static void do_stat(CHAR_DATA ch, String argument) {
        String string;
        OBJ_DATA obj;
        ROOM_INDEX_DATA location;
        CHAR_DATA victim;
        StringBuilder argb = new StringBuilder();
        string = one_argument(argument, argb);
        if (argb.length() == 0) {
            send_to_char("Syntax:\n", ch);
            send_to_char("  stat <name>\n", ch);
            send_to_char("  stat obj <name>\n", ch);
            send_to_char("  stat mob <name>\n", ch);
            send_to_char("  stat room <number>\n", ch);
            return;
        }
        String arg = argb.toString();

        if (!str_cmp(arg, "room")) {
            do_rstat(ch, string);
            return;
        }

        if (!str_cmp(arg, "obj")) {
            do_ostat(ch, string);
            return;
        }

        if (!str_cmp(arg, "char") || !str_cmp(arg, "mob")) {
            do_mstat(ch, string);
            return;
        }

        /* do it the old way */

        obj = get_obj_world(ch, argument);
        if (obj != null) {
            do_ostat(ch, argument);
            return;
        }

        victim = get_char_world(ch, argument);
        if (victim != null) {
            do_mstat(ch, argument);
            return;
        }

        location = find_location(ch, argument);
        if (location != null) {
            do_rstat(ch, argument);
            return;
        }

        send_to_char("Nothing by that name found anywhere.\n", ch);
    }


    static void do_rstat(CHAR_DATA ch, String argument) {
        ROOM_INDEX_DATA location;
        ROOM_HISTORY_DATA rh;
        OBJ_DATA obj;
        CHAR_DATA rch;
        int door;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);
        location = (arg.length() == 0) ? ch.in_room : find_location(ch, arg.toString());
        if (location == null) {
            send_to_char("No such location.\n", ch);
            return;
        }

/*    if (!is_room_owner(ch,location) && ch.in_room != location  */
        if (ch.in_room != location
                && room_is_private(location) && !IS_TRUSTED(ch, IMPLEMENTOR)) {
            send_to_char("That room is private right now.\n", ch);
            return;
        }
        TextBuffer buf = new TextBuffer();
        if (ch.in_room.affected_by != 0) {
            buf.sprintf("Affected by %s\n", raffect_bit_name(ch.in_room.affected_by));
            send_to_char(buf, ch);
        }

        if (ch.in_room.room_flags != 0) {
            buf.sprintf("Roomflags %s\n", flag_room_name(ch.in_room.room_flags));
            send_to_char(buf, ch);
        }

        buf.sprintf("Name: '%s'\nArea: '%s'\nOwner: '%s'\n",
                location.name,
                location.area.name,
                location.owner);
        send_to_char(buf, ch);

        buf.sprintf(
                "Vnum: %d  Sector: %d  Light: %d  Healing: %d  Mana: %d\n",
                location.vnum,
                location.sector_type,
                location.light,
                location.heal_rate,
                location.mana_rate);
        send_to_char(buf, ch);

        buf.sprintf(
                "Room flags: %d.\nDescription:\n%s",
                location.room_flags,
                location.description);
        send_to_char(buf, ch);

        if (location.extra_descr != null) {
            EXTRA_DESCR_DATA ed;

            send_to_char("Extra description keywords: '", ch);
            for (ed = location.extra_descr; ed != null; ed = ed.next) {
                send_to_char(ed.keyword, ch);
                if (ed.next != null) {
                    send_to_char(" ", ch);
                }
            }
            send_to_char("'.\n", ch);
        }

        send_to_char("Characters:", ch);
        for (rch = location.people; rch != null; rch = rch.next_in_room) {
            if (can_see(ch, rch)) {
                send_to_char(" ", ch);
                arg.setLength(0);
                one_argument(rch.name, arg);
                send_to_char(arg, ch);
            }
        }

        send_to_char(".\nObjects:   ", ch);
        for (obj = location.contents; obj != null; obj = obj.next_content) {
            send_to_char(" ", ch);
            arg.setLength(0);
            one_argument(obj.name, arg);
            send_to_char(arg, ch);
        }
        send_to_char(".\n", ch);

        for (door = 0; door <= 5; door++) {
            EXIT_DATA pexit;

            if ((pexit = location.exit[door]) != null) {
                buf.sprintf(
                        "Door: %d.  To: %d.  Key: %d.  Exit flags: %d.\nKeyword: '%s'.  Description: %s",

                        door,
                        (pexit.to_room == null ? -1 : pexit.to_room.vnum),
                        pexit.key,
                        pexit.exit_info,
                        pexit.keyword,
                        pexit.description.length() != 0
                                ? pexit.description : "(none).\n");
                send_to_char(buf, ch);
            }
        }
        send_to_char("Tracks:\n", ch);
        for (rh = location.history; rh != null; rh = rh.next) {
            buf.sprintf("%s took door %i.\n", rh.name, rh.went);
            send_to_char(buf, ch);
        }
    }


    static void do_ostat(CHAR_DATA ch, String argument) {

        OBJ_DATA obj;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0) {
            send_to_char("Stat what?\n", ch);
            return;
        }

        if ((obj = get_obj_world(ch, argument)) == null) {
            send_to_char("Nothing like that in hell, earth, or heaven.\n", ch);
            return;
        }
        TextBuffer buf = new TextBuffer();
        buf.sprintf("Name(s): %s\n",
                obj.name);
        send_to_char(buf, ch);

        buf.sprintf("Vnum: %d  Format: %s  Type: %s  Resets: %d\n",
                obj.pIndexData.vnum, obj.pIndexData.new_format ? "new" : "old",
                item_type_name(obj), obj.pIndexData.reset_num);
        send_to_char(buf, ch);

        buf.sprintf("Short description: %s\nLong description: %s\n",
                obj.short_descr, obj.description);
        send_to_char(buf, ch);

        buf.sprintf("Wear bits: %s\nExtra bits: %s\n",
                wear_bit_name(obj.wear_flags), extra_bit_name(obj.extra_flags));
        send_to_char(buf, ch);

        buf.sprintf("Number: %d/%d  Weight: %d/%d/%d (10th pounds)\n", 1, get_obj_number(obj),
                obj.weight, get_obj_weight(obj), get_true_weight(obj));
        send_to_char(buf, ch);

        buf.sprintf("Level: %d  Cost: %d  Condition: %d  Timer: %d Count: %d\n",
                obj.level, obj.cost, obj.condition, obj.timer, obj.pIndexData.count);
        send_to_char(buf, ch);

        buf.sprintf(
                "In room: %d  In object: %s  Carried by: %s  Wear_loc: %d\n",
                obj.in_room == null ? 0 : obj.in_room.vnum,
                obj.in_obj == null ? "(none)" : obj.in_obj.short_descr,
                obj.carried_by == null ? "(none)" :
                        can_see(ch, obj.carried_by) ? obj.carried_by.name
                                : "someone",
                obj.wear_loc);
        send_to_char(buf, ch);

        buf.sprintf("Values: %d %d %d %d %d\n",
                obj.value[0], obj.value[1], obj.value[2], obj.value[3],
                obj.value[4]);
        send_to_char(buf, ch);

        /* now give out vital statistics as per identify */
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
                buf.sprintf("Has %d(%d) charges of level %d",
                        obj.value[1], obj.value[2], obj.value[0]);
                send_to_char(buf, ch);

                if (obj.value[3] >= 0 && obj.value[3] < MAX_SKILL) {
                    send_to_char(" '", ch);
                    send_to_char(Skill.skills[obj.value[3]].name, ch);
                    send_to_char("'", ch);
                }

                send_to_char(".\n", ch);
                break;

            case ITEM_DRINK_CON:
                buf.sprintf("It holds %s-colored %s.\n",
                        liq_table[obj.value[2]].liq_color,
                        liq_table[obj.value[2]].liq_name);
                send_to_char(buf, ch);
                break;


            case ITEM_WEAPON:
                send_to_char("Weapon type is ", ch);
                switch (obj.value[0]) {
                    case (WEAPON_EXOTIC):
                        send_to_char("exotic\n", ch);
                        break;
                    case (WEAPON_SWORD):
                        send_to_char("sword\n", ch);
                        break;
                    case (WEAPON_DAGGER):
                        send_to_char("dagger\n", ch);
                        break;
                    case (WEAPON_SPEAR):
                        send_to_char("spear/staff\n", ch);
                        break;
                    case (WEAPON_MACE):
                        send_to_char("mace/club\n", ch);
                        break;
                    case (WEAPON_AXE):
                        send_to_char("axe\n", ch);
                        break;
                    case (WEAPON_FLAIL):
                        send_to_char("flail\n", ch);
                        break;
                    case (WEAPON_WHIP):
                        send_to_char("whip\n", ch);
                        break;
                    case (WEAPON_POLEARM):
                        send_to_char("polearm\n", ch);
                        break;
                    case (WEAPON_BOW):
                        send_to_char("bow\n", ch);
                        break;
                    case (WEAPON_ARROW):
                        send_to_char("arrow\n", ch);
                        break;
                    case (WEAPON_LANCE):
                        send_to_char("lance\n", ch);
                        break;
                    default:
                        send_to_char("unknown\n", ch);
                        break;
                }
                if (obj.pIndexData.new_format) {
                    buf.sprintf("Damage is %dd%d (average %d)\n",
                            obj.value[1], obj.value[2],
                            (1 + obj.value[2]) * obj.value[1] / 2);
                } else {
                    buf.sprintf("Damage is %d to %d (average %d)\n",
                            obj.value[1], obj.value[2],
                            (obj.value[1] + obj.value[2]) / 2);
                }
                send_to_char(buf, ch);

                buf.sprintf("Damage noun is %s.\n",
                        attack_table[obj.value[3]].noun);
                send_to_char(buf, ch);

                if (obj.value[4] != 0)  /* weapon flags */ {
                    buf.sprintf("Weapons flags: %s\n",
                            weapon_bit_name(obj.value[4]));
                    send_to_char(buf, ch);
                }
                break;

            case ITEM_ARMOR:
                buf.sprintf(
                        "Armor class is %d pierce, %d bash, %d slash, and %d vs. magic\n",
                        obj.value[0], obj.value[1], obj.value[2], obj.value[3]);
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
        }


        if (obj.extra_descr != null || obj.pIndexData.extra_descr != null) {
            EXTRA_DESCR_DATA ed;

            send_to_char("Extra description keywords: '", ch);

            for (ed = obj.extra_descr; ed != null; ed = ed.next) {
                send_to_char(ed.keyword, ch);
                if (ed.next != null) {
                    send_to_char(" ", ch);
                }
            }

            for (ed = obj.pIndexData.extra_descr; ed != null; ed = ed.next) {
                send_to_char(ed.keyword, ch);
                if (ed.next != null) {
                    send_to_char(" ", ch);
                }
            }

            send_to_char("'\n", ch);
        }

        for (AFFECT_DATA paf = obj.affected; paf != null; paf = paf.next) {
            buf.sprintf("Affects %s by %d, level %d",
                    affect_loc_name(paf.location), paf.modifier, paf.level);
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
                    case TO_WEAPON:
                        buf.sprintf("Adds %s weapon flags.\n", weapon_bit_name(paf.bitvector));
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
                        buf.sprintf("Unknown bit %d: %d\n",
                                paf.where, paf.bitvector);
                        break;
                }
                send_to_char(buf, ch);
            }
        }

        if (!obj.enchanted) {
            for (AFFECT_DATA paf = obj.pIndexData.affected; paf != null; paf = paf.next) {
                buf.sprintf("Affects %s by %d, level %d.\n",
                        affect_loc_name(paf.location), paf.modifier, paf.level);
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
        buf.sprintf("Object progs: ");
        if (obj.pIndexData.progtypes != 0) {
            if (IS_SET(obj.progtypes, OPROG_GET)) {
                buf.append("get ");
            }
            if (IS_SET(obj.progtypes, OPROG_DROP)) {
                buf.append("drop ");
            }
            if (IS_SET(obj.progtypes, OPROG_SAC)) {
                buf.append("sacrifice ");
            }
            if (IS_SET(obj.progtypes, OPROG_GIVE)) {
                buf.append("give ");
            }
            if (IS_SET(obj.progtypes, OPROG_FIGHT)) {
                buf.append("fight ");
            }
            if (IS_SET(obj.progtypes, OPROG_DEATH)) {
                buf.append("death ");
            }
            if (IS_SET(obj.progtypes, OPROG_SPEECH)) {
                buf.append("speech ");
            }
            if (IS_SET(obj.progtypes, OPROG_AREA)) {
                buf.append("area ");
            }
        }
        buf.append("\n");
        send_to_char(buf, ch);
        buf.sprintf("Damage condition : %d (%s) ", obj.condition,
                get_cond_alias(obj));
        send_to_char(buf, ch);
        send_to_char("\n", ch);
    }


    static void do_mstat(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0) {
            send_to_char("Stat whom?\n", ch);
            return;
        }

        if ((victim = get_char_room(ch, argument)) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }
        TextBuffer buf = new TextBuffer();
        buf.sprintf("Name: [%s] Reset Zone: [%s] Logon: %s\r",
                victim.name,
                (IS_NPC(victim) && victim.zone != null) ? victim.zone.name : "?",
                new Date(ch.logon * 1000L));
        send_to_char(buf, ch);

        buf.sprintf("Vnum: %d  Format: %s  Race: %s(%s)  Group: %d  Sex: %s  Room: %d\n",
                IS_NPC(victim) ? victim.pIndexData.vnum : 0,
                IS_NPC(victim) ? victim.pIndexData.new_format ? "new" : "old" : "pc",
                victim.race.name, ORG_RACE(victim).name,
                IS_NPC(victim) ? victim.group : 0, sex_table[victim.sex].name,
                victim.in_room == null ? 0 : victim.in_room.vnum
        );
        send_to_char(buf, ch);

        if (IS_NPC(victim)) {
            buf.sprintf("Count: %d  Killed: %d  ---  Status: %d  Cabal: %d\n",
                    victim.pIndexData.count, victim.pIndexData.killed,
                    victim.status, victim.cabal);
            send_to_char(buf, ch);
        }

        buf.sprintf(
                "Str: %d(%d)  Int: %d(%d)  Wis: %d(%d)  Dex: %d(%d)  Con: %d(%d) Cha: %d(%d)\n",
                victim.perm_stat[STAT_STR],
                get_curr_stat(victim, STAT_STR),
                victim.perm_stat[STAT_INT],
                get_curr_stat(victim, STAT_INT),
                victim.perm_stat[STAT_WIS],
                get_curr_stat(victim, STAT_WIS),
                victim.perm_stat[STAT_DEX],
                get_curr_stat(victim, STAT_DEX),
                victim.perm_stat[STAT_CON],
                get_curr_stat(victim, STAT_CON),
                victim.perm_stat[STAT_CHA],
                get_curr_stat(victim, STAT_CHA));
        send_to_char(buf, ch);


        buf.sprintf("Hp: %d/%d  Mana: %d/%d  Move: %d/%d  Practices: %d\n",
                victim.hit, victim.max_hit,
                victim.mana, victim.max_mana,
                victim.move, victim.max_move,
                IS_NPC(ch) ? 0 : victim.practice);
        send_to_char(buf, ch);

        TextBuffer buf2 = new TextBuffer();
        if (IS_NPC(victim)) {
            sprintf(buf2, "%d", victim.alignment);
        } else {
            sprintf(buf2, "%s",
                    victim.ethos == ETHOS_LAWFUL ? "Law-" :
                            victim.ethos == ETHOS_NEUTRAL ? "Neut-" :
                                    victim.ethos == ETHOS_CHAOTIC ? "Cha-" : "none-");
            buf2.append(IS_GOOD(victim) ? "Good" : IS_NEUTRAL(victim) ? "Neut" : IS_EVIL(victim) ? "Evil" : "Other");
        }
        buf.sprintf("It belives the religion of %s.\n",
                IS_NPC(victim) ? "Chronos" : religion_table[victim.religion].leader);
        send_to_char(buf, ch);
        buf.sprintf(
                "Lv: %d  Class: %s  Align: %s  Gold: %d  Silver: %d  Exp: %d\n",
                victim.level,
                IS_NPC(victim) ? "mobile" : victim.clazz.name,
                buf2,
                victim.gold, victim.silver, victim.exp);
        send_to_char(buf, ch);

        buf.sprintf("Armor: pierce: %d  bash: %d  slash: %d  magic: %d\n",
                GET_AC(victim, AC_PIERCE), GET_AC(victim, AC_BASH),
                GET_AC(victim, AC_SLASH), GET_AC(victim, AC_EXOTIC));
        send_to_char(buf, ch);

        buf.sprintf(
                "Hit: %d  Dam: %d  Saves: %d  Size: %s  Position: %s  Wimpy: %d\n",
                GET_HITROLL(victim), GET_DAMROLL(victim), victim.saving_throw,
                size_table[victim.size].name, position_table[victim.position].name,
                victim.wimpy);
        send_to_char(buf, ch);

        if (IS_NPC(victim) && victim.pIndexData.new_format) {
            buf.sprintf("Damage: %dd%d  Message:  %s\n",
                    victim.damage[DICE_NUMBER], victim.damage[DICE_TYPE],
                    attack_table[victim.dam_type].noun);
            send_to_char(buf, ch);
        }
        buf.sprintf("Fighting: %s Death: %d Carry number: %d  Carry weight: %d\n",
                victim.fighting != null ? victim.fighting.name : "(none)"
                , IS_NPC(victim) ? 0 : victim.pcdata.death,
                victim.carry_number, get_carry_weight(victim) / 10);
        send_to_char(buf, ch);

        if (!IS_NPC(victim)) {
            buf.sprintf(
                    "Thirst: %d  Hunger: %d  Full: %d  Drunk: %d Bloodlust: %d Desire: %d\n",
                    victim.pcdata.condition[COND_THIRST],
                    victim.pcdata.condition[COND_HUNGER],
                    victim.pcdata.condition[COND_FULL],
                    victim.pcdata.condition[COND_DRUNK],
                    victim.pcdata.condition[COND_BLOODLUST],
                    victim.pcdata.condition[COND_DESIRE]);
            send_to_char(buf, ch);
        }


        if (!IS_NPC(victim)) {
            buf.sprintf(
                    "Age: %d  Played: %d  Last Level: %d  Timer: %d\n",
                    get_age(victim),
                    (int) (victim.played + current_time - victim.logon) / 3600,
                    victim.pcdata.last_level,
                    victim.timer);
            send_to_char(buf, ch);
        }

        buf.sprintf("Act: %s\n", act_bit_name(victim.act));
        send_to_char(buf, ch);

        if (victim.comm != 0) {
            buf.sprintf("Comm: %s\n", comm_bit_name(victim.comm));
            send_to_char(buf, ch);
        }

        if (IS_NPC(victim) && victim.off_flags != 0) {
            buf.sprintf("Offense: %s\n", off_bit_name(victim.off_flags));
            send_to_char(buf, ch);
        }

        if (victim.imm_flags != 0) {
            buf.sprintf("Immune: %s\n", imm_bit_name(victim.imm_flags));
            send_to_char(buf, ch);
        }

        if (victim.res_flags != 0) {
            buf.sprintf("Resist: %s\n", imm_bit_name(victim.res_flags));
            send_to_char(buf, ch);
        }

        if (victim.vuln_flags != 0) {
            buf.sprintf("Vulnerable: %s\n", imm_bit_name(victim.vuln_flags));
            send_to_char(buf, ch);
        }


        buf.sprintf("Form: %s\nParts: %s\n", form_bit_name(victim.form), part_bit_name(victim.parts));
        send_to_char(buf, ch);

        if (victim.affected_by != 0) {
            buf.sprintf("Affected by %s\n", affect_bit_name(victim.affected_by));
            send_to_char(buf, ch);
        }

        buf.sprintf("Master: %s  Leader: %s  Pet: %s\n",
                victim.master != null ? victim.master.name : "(none)",
                victim.leader != null ? victim.leader.name : "(none)",
                victim.pet != null ? victim.pet.name : "(none)");
        send_to_char(buf, ch);

        buf.sprintf("Short description: %s\nLong  description: %s",
                victim.short_descr,
                victim.long_descr.length() != 0 ? victim.long_descr : "(none)\n");
        send_to_char(buf, ch);

        if (IS_NPC(victim) && victim.spec_fun != null) {
            buf.sprintf("Mobile has special procedure %s.\n", spec_name(victim.spec_fun));
            send_to_char(buf, ch);
        }

        for (AFFECT_DATA paf = victim.affected; paf != null; paf = paf.next) {
            buf.sprintf("Spell: '%s' modifies %s by %d for %d hours with bits %s, level %d.\n",
                    paf.type.name,
                    affect_loc_name(paf.location),
                    paf.modifier,
                    paf.duration,
                    affect_bit_name(paf.bitvector),
                    paf.level
            );
            send_to_char(buf, ch);
        }

        if (!(IS_NPC(victim))) {
            if (IS_SET(victim.act, PLR_QUESTOR)) {
                buf.sprintf("Questgiver: %d QuestPnts: %d  Questnext: %d\n",
                        victim.pcdata.questgiver, victim.pcdata.questpoints,
                        victim.pcdata.nextquest);
                send_to_char(buf, ch);
                buf.sprintf("QuestCntDown: %d  QuestObj: %d    Questmob: %d\n",
                        victim.pcdata.countdown, victim.pcdata.questobj,
                        victim.pcdata.questmob);
                send_to_char(buf, ch);
            }
            if (!IS_SET(victim.act, PLR_QUESTOR)) {
                buf.sprintf("QuestPnts: %d Questnext: %d    NOT QUESTING\n",
                        victim.pcdata.questpoints, victim.pcdata.nextquest);
                send_to_char(buf, ch);
            }
        }

        if (IS_NPC(victim)) {
            if (victim.pIndexData.progtypes != 0) {
                buf.sprintf("Mobile progs: ");
                if (IS_SET(victim.progtypes, MPROG_BRIBE)) {
                    buf.append("bribe ");
                }
                if (IS_SET(victim.progtypes, MPROG_SPEECH)) {
                    buf.append("speech ");
                }
                if (IS_SET(victim.progtypes, MPROG_GIVE)) {
                    buf.append("give ");
                }
                if (IS_SET(victim.progtypes, MPROG_DEATH)) {
                    buf.append("death ");
                }
                if (IS_SET(victim.progtypes, MPROG_GREET)) {
                    buf.append("greet ");
                }
                if (IS_SET(victim.progtypes, MPROG_ENTRY)) {
                    buf.append("entry ");
                }
                if (IS_SET(victim.progtypes, MPROG_FIGHT)) {
                    buf.append("fight ");
                }
                if (IS_SET(victim.progtypes, MPROG_AREA)) {
                    buf.append("area ");
                }
                buf.append("\n");
                send_to_char(buf, ch);
            }
        }
        buf.sprintf("Last fought: %10s  Last fight time: %s",
                victim.last_fought != null ? victim.last_fought.name : "none",
                new Date(victim.last_fight_time * 1000L));
        send_to_char(buf, ch);
        buf.sprintf("In_mind: [%s] Hunting: [%s]\n",
                victim.in_mind != null ? victim.in_mind : "none",
                victim.hunting != null ? victim.hunting.name : "none");
        send_to_char(buf, ch);
    }

    static void do_vnum(CHAR_DATA ch, String argument) {
        String string;
        StringBuilder argb = new StringBuilder();
        string = one_argument(argument, argb);

        if (argb.length() == 0) {
            send_to_char("Syntax:\n", ch);
            send_to_char("  vnum obj <name>\n", ch);
            send_to_char("  vnum mob <name>\n", ch);
            return;
        }
        String arg = argb.toString();
        if (!str_cmp(arg, "obj")) {
            do_ofind(ch, string);
            return;
        }

        if (!str_cmp(arg, "mob") || !str_cmp(arg, "char")) {
            do_mfind(ch, string);
            return;
        }

        /* do both */
        do_mfind(ch, argument);
        do_ofind(ch, argument);
    }


    static void do_mfind(CHAR_DATA ch, String argument) {
        MOB_INDEX_DATA pMobIndex;
        int vnum;
        int nMatch;
//        boolean fAll;
        boolean found;

        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);
        if (arg.length() == 0) {
            send_to_char("Find whom?\n", ch);
            return;
        }

//        fAll    = false;  !str_cmp( arg, "all" );
        found = false;
        nMatch = 0;

        /*
         * Yeah, so iterating over all vnum's takes 10,000 loops.
         * Get_mob_index is fast, and I don't feel like threading another link.
         * Do you?
         * -- Furey
         */
        for (vnum = 0; nMatch < top_mob_index; vnum++) {
            if ((pMobIndex = get_mob_index(vnum)) != null) {
                nMatch++;
                if ( /*fAll || */is_name(argument, pMobIndex.player_name)) {
                    found = true;
                    TextBuffer buf = new TextBuffer();
                    buf.sprintf("[%5d] %s\n", pMobIndex.vnum, pMobIndex.short_descr);
                    send_to_char(buf, ch);
                }
            }
        }

        if (!found) {
            send_to_char("No mobiles by that name.\n", ch);
        }

    }


    static void do_ofind(CHAR_DATA ch, String argument) {
        OBJ_INDEX_DATA pObjIndex;
        int vnum;
        int nMatch;
//        boolean fAll;
        boolean found;

        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);
        if (arg.length() == 0) {
            send_to_char("Find what?\n", ch);
            return;
        }

//        fAll    = false; !str_cmp( arg, "all" );
        found = false;
        nMatch = 0;

        /*
         * Yeah, so iterating over all vnum's takes 10,000 loops.
         * Get_obj_index is fast, and I don't feel like threading another link.
         * Do you?
         * -- Furey
         */
        TextBuffer buf = new TextBuffer();
        for (vnum = 0; nMatch < top_obj_index; vnum++) {
            if ((pObjIndex = get_obj_index(vnum)) != null) {
                nMatch++;
                if ( /*fAll ||*/ is_name(argument, pObjIndex.name)) {
                    found = true;
                    buf.sprintf("[%5d] %s%s\n", pObjIndex.vnum, pObjIndex.short_descr,
                            (IS_OBJ_STAT(pObjIndex, ITEM_GLOW) && CAN_WEAR(pObjIndex, ITEM_WEAR_HEAD)) ? " (Glowing)" : "");
                    send_to_char(buf, ch);
                }
            }
        }

        if (!found) {
            send_to_char("No objects by that name.\n", ch);
        }

    }


    static void do_owhere(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        OBJ_DATA in_obj;
        boolean found;
        int number, max_found;

        found = false;
        number = 0;
        max_found = 200;


        if (argument.length() == 0) {
            send_to_char("Find what?\n", ch);
            return;
        }

        TextBuffer buf = new TextBuffer();
        StringBuilder buffer = new StringBuilder();
        for (obj = object_list; obj != null; obj = obj.next) {
            if (!can_see_obj(ch, obj) || !is_name(argument, obj.name)
                    || ch.level < obj.level) {
                continue;
            }

            found = true;
            number++;

            for (in_obj = obj; in_obj.in_obj != null; in_obj = in_obj.in_obj) {
            }

            if (in_obj.carried_by != null && can_see(ch, in_obj.carried_by)
                    && in_obj.carried_by.in_room != null) {
                buf.sprintf("%3d) %s is carried by %s [Room %d]\n",
                        number, obj.short_descr, PERS(in_obj.carried_by, ch),
                        in_obj.carried_by.in_room.vnum);
            } else if (in_obj.in_room != null && can_see_room(ch, in_obj.in_room)) {
                buf.sprintf("%3d) %s is in %s [Room %d]\n",
                        number, obj.short_descr, in_obj.in_room.name,
                        in_obj.in_room.vnum);
            } else {
                buf.sprintf("%3d) %s is somewhere\n", number, obj.short_descr);
            }

            buf.getBuffer().setCharAt(0, UPPER(buf.getBuffer().charAt(0)));
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


    static void do_mwhere(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        boolean found;
        int count = 0;

        StringBuilder buffer = new StringBuilder();
        TextBuffer buf = new TextBuffer();

        if (argument.length() == 0) {
            DESCRIPTOR_DATA d;

            /* show characters logged */
            for (d = descriptor_list; d != null; d = d.next) {
                if (d.character != null && d.connected == CON_PLAYING
                        && d.character.in_room != null && can_see(ch, d.character)
                        && can_see_room(ch, d.character.in_room)) {
                    victim = d.character;
                    count++;
                    if (d.original != null) {
                        buf.sprintf("%3d) %s (in the body of %s) is in %s [%d]\n",
                                count, d.original.name, victim.short_descr,
                                victim.in_room.name, victim.in_room.vnum);
                    } else {
                        buf.sprintf("%3d) %s is in %s [%d]\n", count, victim.name, victim.in_room.name, victim.in_room.vnum);
                    }
                    buffer.append(buf);
                }
            }

            page_to_char(buffer, ch);
            return;
        }

        found = false;
        for (victim = char_list; victim != null; victim = victim.next) {
            if (victim.in_room != null && is_name(argument, victim.name)) {
                found = true;
                count++;
                buf.sprintf("%3d) [%5d] %-28s [%5d] %s\n", count,
                        IS_NPC(victim) ? victim.pIndexData.vnum : 0,
                        IS_NPC(victim) ? victim.short_descr : victim.name,
                        victim.in_room.vnum,
                        victim.in_room.name);
                buffer.append(buf);
            }
        }

        if (!found) {
            act("You didn't find any $T.", ch, null, argument, TO_CHAR);
        } else {
            page_to_char(buffer, ch);
        }


    }


    static void do_reboo(CHAR_DATA ch) {
        send_to_char("If you want to REBOOT, spell it out.\n", ch);
    }


    static void do_shutdow(CHAR_DATA ch) {
        send_to_char("If you want to SHUTDOWN, spell it out.\n", ch);
    }


    static void do_shutdown(CHAR_DATA ch) {
        TextBuffer buf = new TextBuffer();
        if (ch.invis_level < LEVEL_HERO) {
            buf.sprintf("Shutdown by %s.", ch.name);
        }
        buf.append("\n");
        if (ch.invis_level < LEVEL_HERO) {
            do_echo(ch, buf.toString());
        }
        reboot_nightworks(false, NIGHTWORKS_SHUTDOWN);
    }

    static void do_protect(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;

        if (argument.length() == 0) {
            send_to_char("Protect whom from snooping?\n", ch);
            return;
        }

        if ((victim = get_char_world(ch, argument)) == null) {
            send_to_char("You can't find them.\n", ch);
            return;
        }

        if (IS_SET(victim.comm, COMM_SNOOP_PROOF)) {
            act("$N is no longer snoop-proof.", ch, null, victim, TO_CHAR, POS_DEAD);
            send_to_char("Your snoop-proofing was just removed.\n", victim);
            victim.comm = REMOVE_BIT(victim.comm, COMM_SNOOP_PROOF);
        } else {
            act("$N is now snoop-proof.", ch, null, victim, TO_CHAR, POS_DEAD);
            send_to_char("You are now immune to snooping.\n", victim);
            victim.comm = SET_BIT(victim.comm, COMM_SNOOP_PROOF);
        }
    }


    static void do_snoop(CHAR_DATA ch, String argument) {
        DESCRIPTOR_DATA d;
        CHAR_DATA victim;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0) {
            send_to_char("Snoop whom?\n", ch);
            return;
        }

        if ((victim = get_char_world(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (victim.desc == null) {
            send_to_char("No descriptor to snoop.\n", ch);
            return;
        }

        if (victim == ch) {
            send_to_char("Cancelling all snoops.\n", ch);
            wiznet("$N stops being such a snoop.",
                    ch, null, WIZ_SNOOPS, WIZ_SECURE, get_trust(ch));
            for (d = descriptor_list; d != null; d = d.next) {
                if (d.snoop_by == ch.desc) {
                    d.snoop_by = null;
                }
            }
            return;
        }

        if (victim.desc.snoop_by != null) {
            send_to_char("Busy already.\n", ch);
            return;
        }

        if (!is_room_owner(ch, victim.in_room) && ch.in_room != victim.in_room
                && room_is_private(victim.in_room) && !IS_TRUSTED(ch, IMPLEMENTOR)) {
            send_to_char("That character is in a private room.\n", ch);
            return;
        }

        if (get_trust(victim) >= get_trust(ch)
                || IS_SET(victim.comm, COMM_SNOOP_PROOF)) {
            send_to_char("You failed.\n", ch);
            return;
        }

        if (ch.desc != null) {
            for (d = ch.desc.snoop_by; d != null; d = d.snoop_by) {
                if (d.character == victim || d.original == victim) {
                    send_to_char("No snoop loops.\n", ch);
                    return;
                }
            }
        }

        TextBuffer buf = new TextBuffer();
        victim.desc.snoop_by = ch.desc;
        buf.sprintf("$N starts snooping on %s",
                (IS_NPC(ch) ? victim.short_descr : victim.name));
        wiznet(buf, ch, null, WIZ_SNOOPS, WIZ_SECURE, get_trust(ch));
        send_to_char("Ok.\n", ch);
    }


    static void do_switch(CHAR_DATA ch, String argument) {

        CHAR_DATA victim;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0) {
            send_to_char("Switch into whom?\n", ch);
            return;
        }

        if (ch.desc == null) {
            return;
        }

        if (ch.desc.original != null) {
            send_to_char("You are already switched.\n", ch);
            return;
        }

        if ((victim = get_char_world(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (victim == ch) {
            send_to_char("Ok.\n", ch);
            return;
        }

        if (!IS_NPC(victim)) {
            send_to_char("You can only switch into mobiles.\n", ch);
            return;
        }

        if (!is_room_owner(ch, victim.in_room) && ch.in_room != victim.in_room
                && room_is_private(victim.in_room) && !IS_TRUSTED(ch, IMPLEMENTOR)) {
            send_to_char("That character is in a private room.\n", ch);
            return;
        }

        if (victim.desc != null) {
            send_to_char("Character in use.\n", ch);
            return;
        }
        TextBuffer buf = new TextBuffer();
        buf.sprintf("$N switches into %s", victim.short_descr);
        wiznet(buf, ch, null, WIZ_SWITCHES, WIZ_SECURE, get_trust(ch));

        ch.desc.character = victim;
        ch.desc.original = ch;
        victim.desc = ch.desc;
        ch.desc = null;
        /* change communications to match */
        if (ch.prompt != null) {
            victim.prompt = ch.prompt;
        }
        victim.comm = ch.comm;
        victim.lines = ch.lines;
        send_to_char("Ok.\n", victim);
    }


    static void do_return(CHAR_DATA ch) {

        if (ch.desc == null) {
            return;
        }

        if (ch.desc.original == null) {
            send_to_char("You aren't switched.\n", ch);
            return;
        }

        send_to_char(
                "You return to your original body. Type replay to see any missed tells.\n",
                ch);
        if (ch.prompt != null) {
            ch.prompt = null;
        }
        TextBuffer buf = new TextBuffer();
        buf.sprintf("$N returns from %s.", ch.short_descr);
        wiznet(buf, ch.desc.original, null, WIZ_SWITCHES, WIZ_SECURE, get_trust(ch));
        ch.desc.character = ch.desc.original;
        ch.desc.original = null;
        ch.desc.character.desc = ch.desc;
        ch.desc = null;
    }

/* trust levels for load and clone */

    static boolean obj_check(CHAR_DATA ch, OBJ_DATA obj) {
        return IS_TRUSTED(ch, GOD)
                || (IS_TRUSTED(ch, IMMORTAL) && obj.level <= 20 && obj.cost <= 1000)
                || (IS_TRUSTED(ch, DEMI) && obj.level <= 10 && obj.cost <= 500)
                || (IS_TRUSTED(ch, ANGEL) && obj.level <= 5 && obj.cost <= 250)
                || (IS_TRUSTED(ch, AVATAR) && obj.level == 0 && obj.cost <= 100);
    }

/* for clone, to insure that cloning goes many levels deep */

    static void recursive_clone(CHAR_DATA ch, OBJ_DATA obj, OBJ_DATA clone) {
        OBJ_DATA c_obj, t_obj;


        for (c_obj = obj.contains; c_obj != null; c_obj = c_obj.next_content) {
            if (obj_check(ch, c_obj)) {
                t_obj = create_object(c_obj.pIndexData, 0);
                clone_object(c_obj, t_obj);
                obj_to_obj(t_obj, clone);
                recursive_clone(ch, c_obj, t_obj);
            }
        }
    }

/* command that is similar to load */

    static void do_clone(CHAR_DATA ch, String argument) {
        CHAR_DATA mob;
        OBJ_DATA obj;

        StringBuilder argb = new StringBuilder();
        String rest = one_argument(argument, argb);
        String arg = argb.toString();

        if (arg.length() == 0) {
            send_to_char("Clone what?\n", ch);
            return;
        }

        if (!str_prefix(arg, "object")) {
            mob = null;
            obj = get_obj_here(ch, rest);
            if (obj == null) {
                send_to_char("You don't see that here.\n", ch);
                return;
            }
        } else if (!str_prefix(arg, "mobile") || !str_prefix(arg, "character")) {
            obj = null;
            mob = get_char_room(ch, rest);
            if (mob == null) {
                send_to_char("You don't see that here.\n", ch);
                return;
            }
        } else /* find both */ {
            mob = get_char_room(ch, argument);
            obj = get_obj_here(ch, argument);
            if (mob == null && obj == null) {
                send_to_char("You don't see that here.\n", ch);
                return;
            }
        }

        /* clone an object */
        if (obj != null) {
            OBJ_DATA clone;

            if (!obj_check(ch, obj)) {
                send_to_char(
                        "Your powers are not great enough for such a task.\n", ch);
                return;
            }

            clone = create_object(obj.pIndexData, 0);
            clone_object(obj, clone);
            if (obj.carried_by != null) {
                obj_to_char(clone, ch);
            } else {
                obj_to_room(clone, ch.in_room);
            }
            recursive_clone(ch, obj, clone);

            act("$n has created $p.", ch, clone, null, TO_ROOM);
            act("You clone $p.", ch, clone, null, TO_CHAR);
            wiznet("$N clones $p.", ch, clone, WIZ_LOAD, WIZ_SECURE, get_trust(ch));
        } else // always true: if (mob != null)
        {
            CHAR_DATA clone;
            OBJ_DATA new_obj;

            if (!IS_NPC(mob)) {
                send_to_char("You can only clone mobiles.\n", ch);
                return;
            }

            if ((mob.level > 20 && !IS_TRUSTED(ch, GOD))
                    || (mob.level > 10 && !IS_TRUSTED(ch, IMMORTAL))
                    || (mob.level > 5 && !IS_TRUSTED(ch, DEMI))
                    || (mob.level > 0 && !IS_TRUSTED(ch, ANGEL))
                    || !IS_TRUSTED(ch, AVATAR)) {
                send_to_char(
                        "Your powers are not great enough for such a task.\n", ch);
                return;
            }

            clone = create_mobile(mob.pIndexData);
            clone_mobile(mob, clone);

            for (obj = mob.carrying; obj != null; obj = obj.next_content) {
                if (obj_check(ch, obj)) {
                    new_obj = create_object(obj.pIndexData, 0);
                    clone_object(obj, new_obj);
                    recursive_clone(ch, obj, new_obj);
                    obj_to_char(new_obj, clone);
                    new_obj.wear_loc = obj.wear_loc;
                }
            }
            char_to_room(clone, ch.in_room);
            act("$n has created $N.", ch, null, clone, TO_ROOM);
            act("You clone $N.", ch, null, clone, TO_CHAR);
            TextBuffer buf = new TextBuffer();
            buf.sprintf("$N clones %s.", clone.short_descr);
            wiznet(buf, ch, null, WIZ_LOAD, WIZ_SECURE, get_trust(ch));
        }
    }

/* RT to replace the two load commands */

    static void do_load(CHAR_DATA ch, String argument) {
        StringBuilder argb = new StringBuilder();
        argument = one_argument(argument, argb);

        if (argb.length() == 0) {
            send_to_char("Syntax:\n", ch);
            send_to_char("  load mob <vnum>\n", ch);
            send_to_char("  load obj <vnum> <level>\n", ch);
            return;
        }
        String arg = argb.toString();

        if (!str_cmp(arg, "mob") || !str_cmp(arg, "char")) {
            do_mload(ch, argument);
            return;
        }

        if (!str_cmp(arg, "obj")) {
            do_oload(ch, argument);
            return;
        }
        /* echo syntax */
        do_load(ch, "");
    }


    static void do_mload(CHAR_DATA ch, String argument) {
        MOB_INDEX_DATA pMobIndex;
        CHAR_DATA victim;

        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0 || !is_number(arg)) {
            send_to_char("Syntax: load mob <vnum>.\n", ch);
            return;
        }

        if ((pMobIndex = get_mob_index(atoi(arg.toString()))) == null) {
            send_to_char("No mob has that vnum.\n", ch);
            return;
        }

        victim = create_mobile(pMobIndex);
        char_to_room(victim, ch.in_room);
        act("$n has created $N!", ch, null, victim, TO_ROOM);
        TextBuffer buf = new TextBuffer();
        buf.sprintf("$N loads %s.", victim.short_descr);
        wiznet(buf, ch, null, WIZ_LOAD, WIZ_SECURE, get_trust(ch));
        send_to_char("Ok.\n", ch);
    }


    static void do_oload(CHAR_DATA ch, String argument) {
        OBJ_INDEX_DATA pObjIndex;
        OBJ_DATA obj;
        int level;

        StringBuilder arg1 = new StringBuilder();
        StringBuilder arg2 = new StringBuilder();
        argument = one_argument(argument, arg1);
        one_argument(argument, arg2);

        if (arg1.length() == 0 || !is_number(arg1)) {
            send_to_char("Syntax: load obj <vnum> <level>.\n", ch);
            return;
        }

        level = get_trust(ch); /* default */

        if (arg2.length() != 0)  /* load with a level */ {
            if (!is_number(arg2)) {
                send_to_char("Syntax: oload <vnum> <level>.\n", ch);
                return;
            }
            level = atoi(arg2.toString());
            if (level < 0 || level > get_trust(ch)) {
                send_to_char("Level must be be between 0 and your level.\n", ch);
                return;
            }
        }

        if ((pObjIndex = get_obj_index(atoi(arg1.toString()))) == null) {
            send_to_char("No object has that vnum.\n", ch);
            return;
        }

        obj = create_object(pObjIndex, level);
        if (CAN_WEAR(obj, ITEM_TAKE)) {
            obj_to_char(obj, ch);
        } else {
            obj_to_room(obj, ch.in_room);
        }
        act("$n has created $p!", ch, obj, null, TO_ROOM);
        wiznet("$N loads $p.", ch, obj, WIZ_LOAD, WIZ_SECURE, get_trust(ch));
        send_to_char("Ok.\n", ch);
    }


    static void do_purge(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        OBJ_DATA obj;
        DESCRIPTOR_DATA d;

        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0) {
            /* 'purge' */
            CHAR_DATA vnext;
            OBJ_DATA obj_next;

            for (victim = ch.in_room.people; victim != null; victim = vnext) {
                vnext = victim.next_in_room;
                if (IS_NPC(victim) && !IS_SET(victim.act, ACT_NOPURGE)
                        && victim != ch /* safety precaution */) {
                    extract_char(victim, true);
                }
            }

            for (obj = ch.in_room.contents; obj != null; obj = obj_next) {
                obj_next = obj.next_content;
                if (!IS_OBJ_STAT(obj, ITEM_NOPURGE)) {
                    extract_obj(obj);
                }
            }

            act("$n purges the room!", ch, null, null, TO_ROOM);
            send_to_char("Ok.\n", ch);
            return;
        }

        if ((victim = get_char_world(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (!IS_NPC(victim)) {

            if (ch == victim) {
                send_to_char("Ho ho ho.\n", ch);
                return;
            }

            if (get_trust(ch) <= get_trust(victim)) {
                send_to_char("Maybe that wasn't a good idea...\n", ch);
                TextBuffer buf = new TextBuffer();
                buf.sprintf("%s tried to purge you!\n", ch.name);
                send_to_char(buf, victim);
                return;
            }

            act("$n disintegrates $N.", ch, 0, victim, TO_NOTVICT);

            if (victim.level > 1) {
                save_char_obj(victim);
            }
            d = victim.desc;
            extract_char(victim, true);
            if (d != null) {
                close_socket(d);
            }
            return;
        }

        act("$n purges $N.", ch, null, victim, TO_NOTVICT);
        extract_char(victim, true);
    }


    static void do_trust(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        int level;
        StringBuilder arg1 = new StringBuilder();
        StringBuilder arg2 = new StringBuilder();
        argument = one_argument(argument, arg1);
        one_argument(argument, arg2);

        if (arg1.length() == 0 || arg2.length() == 0 || !is_number(arg2)) {
            send_to_char("Syntax: trust <char> <level>.\n", ch);
            return;
        }

        if ((victim = get_char_world(ch, arg1.toString())) == null) {
            send_to_char("That player is not here.\n", ch);
            return;
        }

        if ((level = atoi(arg2.toString())) < 0 || level > 100) {
            send_to_char("Level must be 0 (reset) or 1 to 100.\n", ch);
            return;
        }

        if (level > get_trust(ch)) {
            send_to_char("Limited to your trust.\n", ch);
            return;
        }

        victim.trust = level;
    }


    static void do_restore(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        CHAR_DATA vch;
        DESCRIPTOR_DATA d;
        StringBuilder argb = new StringBuilder();
        one_argument(argument, argb);
        String arg = argb.toString();
        if (arg.length() == 0 || !str_cmp(arg, "room")) {
            /* cure room */

            for (vch = ch.in_room.people; vch != null; vch = vch.next_in_room) {
                affect_strip(vch, gsn_plague);
                affect_strip(vch, gsn_poison);
                affect_strip(vch, gsn_blindness);
                affect_strip(vch, gsn_sleep);
                affect_strip(vch, gsn_curse);

                vch.hit = vch.max_hit;
                vch.mana = vch.max_mana;
                vch.move = vch.max_move;
                update_pos(vch);
                act("$n has restored you.", ch, null, vch, TO_VICT);
            }
            TextBuffer buf = new TextBuffer();
            buf.sprintf("$N restored room %d.", ch.in_room.vnum);
            wiznet(buf, ch, null, WIZ_RESTORE, WIZ_SECURE, get_trust(ch));

            send_to_char("Room restored.\n", ch);
            return;

        }

        if (get_trust(ch) >= MAX_LEVEL - 1 && !str_cmp(arg, "all")) {
            /* cure all */

            for (d = descriptor_list; d != null; d = d.next) {
                victim = d.character;

                if (victim == null || IS_NPC(victim)) {
                    continue;
                }

                affect_strip(victim, gsn_plague);
                affect_strip(victim, gsn_poison);
                affect_strip(victim, gsn_blindness);
                affect_strip(victim, gsn_sleep);
                affect_strip(victim, gsn_curse);

                victim.hit = victim.max_hit;
                victim.mana = victim.max_mana;
                victim.move = victim.max_move;
                update_pos(victim);
                if (victim.in_room != null) {
                    act("$n has restored you.", ch, null, victim, TO_VICT);
                }
            }
            send_to_char("All active players restored.\n", ch);
            return;
        }

        if ((victim = get_char_world(ch, arg)) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        affect_strip(victim, gsn_plague);
        affect_strip(victim, gsn_poison);
        affect_strip(victim, gsn_blindness);
        affect_strip(victim, gsn_sleep);
        affect_strip(victim, gsn_curse);
        victim.hit = victim.max_hit;
        victim.mana = victim.max_mana;
        victim.move = victim.max_move;
        update_pos(victim);
        act("$n has restored you.", ch, null, victim, TO_VICT);
        TextBuffer buf = new TextBuffer();
        buf.sprintf("$N restored %s",
                IS_NPC(victim) ? victim.short_descr : victim.name);
        wiznet(buf, ch, null, WIZ_RESTORE, WIZ_SECURE, get_trust(ch));
        send_to_char("Ok.\n", ch);
    }


    static void do_freeze(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0) {
            send_to_char("Freeze whom?\n", ch);
            return;
        }

        if ((victim = get_char_world(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (IS_NPC(victim)) {
            send_to_char("Not on NPC's.\n", ch);
            return;
        }

        if (get_trust(victim) >= get_trust(ch)) {
            send_to_char("You failed.\n", ch);
            return;
        }

        TextBuffer buf = new TextBuffer();
        if (IS_SET(victim.act, PLR_FREEZE)) {
            victim.act = REMOVE_BIT(victim.act, PLR_FREEZE);
            send_to_char("You can play again.\n", victim);
            send_to_char("FREEZE removed.\n", ch);
            buf.sprintf("$N thaws %s.", victim.name);
            wiznet(buf, ch, null, WIZ_PENALTIES, WIZ_SECURE, 0);
        } else {
            victim.act = SET_BIT(victim.act, PLR_FREEZE);
            send_to_char("You can't do ANYthing!\n", victim);
            send_to_char("FREEZE set.\n", ch);
            buf.sprintf("$N puts %s in the deep freeze.", victim.name);
            wiznet(buf, ch, null, WIZ_PENALTIES, WIZ_SECURE, 0);
        }

        save_char_obj(victim);

    }


    static void do_log(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        StringBuilder argb = new StringBuilder();
        one_argument(argument, argb);

        if (argb.length() == 0) {
            send_to_char("Log whom?\n", ch);
            return;
        }

        String arg = argb.toString();
        if (!str_cmp(arg, "all")) {
            if (fLogAll) {
                fLogAll = false;
                send_to_char("Log ALL off.\n", ch);
            } else {
                fLogAll = true;
                send_to_char("Log ALL on.\n", ch);
            }
            return;
        }

        if ((victim = get_char_world(ch, arg)) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (IS_NPC(victim)) {
            send_to_char("Not on NPC's.\n", ch);
            return;
        }

        /*
         * No level check, gods can log anyone.
         */
        if (IS_SET(victim.act, PLR_LOG)) {
            victim.act = REMOVE_BIT(victim.act, PLR_LOG);
            send_to_char("LOG removed.\n", ch);
        } else {
            victim.act = SET_BIT(victim.act, PLR_LOG);
            send_to_char("LOG set.\n", ch);
        }

    }


    static void do_noemote(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0) {
            send_to_char("Noemote whom?\n", ch);
            return;
        }

        if ((victim = get_char_world(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }


        if (get_trust(victim) >= get_trust(ch)) {
            send_to_char("You failed.\n", ch);
            return;
        }
        TextBuffer buf = new TextBuffer();
        if (IS_SET(victim.comm, COMM_NOEMOTE)) {
            victim.comm = REMOVE_BIT(victim.comm, COMM_NOEMOTE);
            send_to_char("You can emote again.\n", victim);
            send_to_char("NOEMOTE removed.\n", ch);
            buf.sprintf("$N restores emotes to %s.", victim.name);
            wiznet(buf, ch, null, WIZ_PENALTIES, WIZ_SECURE, 0);
        } else {
            victim.comm = SET_BIT(victim.comm, COMM_NOEMOTE);
            send_to_char("You can't emote!\n", victim);
            send_to_char("NOEMOTE set.\n", ch);
            buf.sprintf("$N revokes %s's emotes.", victim.name);
            wiznet(buf, ch, null, WIZ_PENALTIES, WIZ_SECURE, 0);
        }

    }


    static void do_noshout(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0) {
            send_to_char("Noshout whom?\n", ch);
            return;
        }

        if ((victim = get_char_world(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (IS_NPC(victim)) {
            send_to_char("Not on NPC's.\n", ch);
            return;
        }

        if (get_trust(victim) >= get_trust(ch)) {
            send_to_char("You failed.\n", ch);
            return;
        }

        TextBuffer buf = new TextBuffer();
        if (IS_SET(victim.comm, COMM_NOSHOUT)) {
            victim.comm = REMOVE_BIT(victim.comm, COMM_NOSHOUT);
            send_to_char("You can shout again.\n", victim);
            send_to_char("NOSHOUT removed.\n", ch);
            buf.sprintf("$N restores shouts to %s.", victim.name);
            wiznet(buf, ch, null, WIZ_PENALTIES, WIZ_SECURE, 0);
        } else {
            victim.comm = SET_BIT(victim.comm, COMM_NOSHOUT);
            send_to_char("You can't shout!\n", victim);
            send_to_char("NOSHOUT set.\n", ch);
            buf.sprintf("$N revokes %s's shouts.", victim.name);
            wiznet(buf, ch, null, WIZ_PENALTIES, WIZ_SECURE, 0);
        }


    }


    static void do_notell(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0) {
            send_to_char("Notell whom?", ch);
            return;
        }

        if ((victim = get_char_world(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (get_trust(victim) >= get_trust(ch)) {
            send_to_char("You failed.\n", ch);
            return;
        }
        TextBuffer buf = new TextBuffer();
        if (IS_SET(victim.comm, COMM_NOTELL)) {
            victim.comm = REMOVE_BIT(victim.comm, COMM_NOTELL);
            send_to_char("You can tell again.\n", victim);
            send_to_char("NOTELL removed.\n", ch);
            buf.sprintf("$N restores tells to %s.", victim.name);
            wiznet(buf, ch, null, WIZ_PENALTIES, WIZ_SECURE, 0);
        } else {
            victim.comm = SET_BIT(victim.comm, COMM_NOTELL);
            send_to_char("You can't tell!\n", victim);
            send_to_char("NOTELL set.\n", ch);
            buf.sprintf("$N revokes %s's tells.", victim.name);
            wiznet(buf, ch, null, WIZ_PENALTIES, WIZ_SECURE, 0);
        }

    }


    static void do_peace(CHAR_DATA ch) {
        CHAR_DATA rch;

        for (rch = ch.in_room.people; rch != null; rch = rch.next_in_room) {
            if (rch.fighting != null) {
                stop_fighting(rch, true);
            }
            if (IS_NPC(rch) && IS_SET(rch.act, ACT_AGGRESSIVE)) {
                rch.act = REMOVE_BIT(rch.act, ACT_AGGRESSIVE);
            }
        }

        send_to_char("Ok.\n", ch);
    }

    static void do_wizlock(CHAR_DATA ch) {
        wizlock = !wizlock;

        if (wizlock) {
            wiznet("$N has wizlocked the game.", ch, null, 0, 0, 0);
            send_to_char("Game wizlocked.\n", ch);
        } else {
            wiznet("$N removes wizlock.", ch, null, 0, 0, 0);
            send_to_char("Game un-wizlocked.\n", ch);
        }

    }

/* RT anti-newbie code */


    static void do_newlock(CHAR_DATA ch) {
        newlock = !newlock;

        if (newlock) {
            wiznet("$N locks out new characters.", ch, null, 0, 0, 0);
            send_to_char("New characters have been locked out.\n", ch);
        } else {
            wiznet("$N allows new characters back in.", ch, null, 0, 0, 0);
            send_to_char("Newlock removed.\n", ch);
        }

    }


    static void do_slookup(CHAR_DATA ch, String argument) {
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);
        if (arg.length() == 0) {
            send_to_char("Lookup which skill or spell?\n", ch);
            return;
        }
        TextBuffer buf = new TextBuffer();
        if (!str_cmp(arg.toString(), "all")) {
            for (Skill sn : Skill.skills) {
                if (sn.name == null) {
                    break;
                }
                buf.sprintf("Sn: %3d  Slot: %3d  Skill/spell: '%s'\n", sn, sn.slot, sn.name);
                send_to_char(buf, ch);
            }
        } else {
            Skill sn = lookupSkill(arg.toString());
            if (sn == null) {
                send_to_char("No such skill or spell.\n", ch);
                return;
            }

            buf.sprintf("Sn: %3d  Slot: %3d  Skill/spell: '%s'\n", sn, sn.slot, sn.name);
            send_to_char(buf, ch);
        }

    }

/* RT set replaces sset, mset, oset, and rset */

    static void do_set(CHAR_DATA ch, String argument) {
        StringBuilder argb = new StringBuilder();
        argument = one_argument(argument, argb);

        if (argb.length() == 0) {
            send_to_char("Syntax:\n", ch);
            send_to_char("  set mob   <name> <field> <value>\n", ch);
            send_to_char("  set obj   <name> <field> <value>\n", ch);
            send_to_char("  set room  <room> <field> <value>\n", ch);
            send_to_char("  set skill <name> <spell or skill> <value>\n", ch);
            return;
        }

        String arg = argb.toString();
        if (!str_prefix(arg, "mobile") || !str_prefix(arg, "character")) {
            do_mset(ch, argument);
            return;
        }

        if (!str_prefix(arg, "skill") || !str_prefix(arg, "spell")) {
            do_sset(ch, argument);
            return;
        }

        if (!str_prefix(arg, "object")) {
            do_oset(ch, argument);
            return;

        }

        if (!str_prefix(arg, "room")) {
            do_rset(ch, argument);
            return;
        }
        /* echo syntax */
        do_set(ch, "");
    }


    static void do_sset(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        int value;
        boolean fAll;

        StringBuilder arg1 = new StringBuilder();
        StringBuilder arg2 = new StringBuilder();
        StringBuilder arg3 = new StringBuilder();
        argument = one_argument(argument, arg1);
        argument = one_argument(argument, arg2);
        one_argument(argument, arg3);

        if (arg1.length() == 0 || arg2.length() == 0 || arg3.length() == 0) {
            send_to_char("Syntax:\n", ch);
            send_to_char("  set skill <name> <spell or skill> <value>\n", ch);
            send_to_char("  set skill <name> all <value>\n", ch);
            send_to_char("   (use the name of the skill, not the number)\n", ch);
            return;
        }

        if ((victim = get_char_world(ch, arg1.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (IS_NPC(victim)) {
            send_to_char("Not on NPC's.\n", ch);
            return;
        }

        fAll = !str_cmp(arg2.toString(), "all");
        Skill sn = null;
        if (!fAll && (sn = lookupSkill(arg2.toString())) == null) {
            send_to_char("No such skill or spell.\n", ch);
            return;
        }

        /*
         * Snarf the value.
         */
        if (!is_number(arg3.toString())) {
            send_to_char("Value must be numeric.\n", ch);
            return;
        }

        value = atoi(arg3.toString());
        if (value < 0 || value > 100) {
            send_to_char("Value range is 0 to 100.\n", ch);
            return;
        }

        if (fAll) {
            for (Skill s : Skill.skills) {
                if ((s.name != null) && ((victim.cabal == s.cabal) || (s.cabal == CABAL_NONE)) && (RACE_OK(victim, s))) {
                    victim.pcdata.learned[s.ordinal()] = value;
                }
            }
        } else {
            victim.pcdata.learned[sn.ordinal()] = value;
        }

    }


    static void do_string(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        OBJ_DATA obj;

        StringBuilder arg1 = new StringBuilder();
        StringBuilder arg2b = new StringBuilder();
        StringBuilder typeb = new StringBuilder();
        argument = smash_tilde(argument);
        argument = one_argument(argument, typeb);
        argument = one_argument(argument, arg1);
        argument = one_argument(argument, arg2b);
        String arg3 = argument;
        String type = typeb.toString();


        if (type.length() == 0 || arg1.length() == 0 || arg2b.length() == 0 || arg3.length() == 0) {
            send_to_char("Syntax:\n", ch);
            send_to_char("  string char <name> <field> <string>\n", ch);
            send_to_char("    fields: name int long desc title spec\n", ch);
            send_to_char("  string obj  <name> <field> <string>\n", ch);
            send_to_char("    fields: name int long extended\n", ch);
            return;
        }

        String arg2 = arg2b.toString();

        if (!str_prefix(type, "character") || !str_prefix(type, "mobile")) {
            if ((victim = get_char_world(ch, arg1.toString())) == null) {
                send_to_char("They aren't here.\n", ch);
                return;
            }

            /* clear zone for mobs */
            victim.zone = null;

            /* string something */

            if (!str_prefix(arg2, "name")) {
                if (!IS_NPC(victim)) {
                    send_to_char("Not on PC's.\n", ch);
                    return;
                }
                victim.name = arg3;
                return;
            }

            if (!str_prefix(arg2, "description")) {
                victim.description = arg3;
                return;
            }

            if (!str_prefix(arg2, "short")) {
                victim.short_descr = arg3;
                return;
            }

            if (!str_prefix(arg2, "long")) {
                arg3 += "\n";
                victim.long_descr = arg3;
                return;
            }

            if (!str_prefix(arg2, "title")) {
                if (IS_NPC(victim)) {
                    send_to_char("Not on NPC's.\n", ch);
                    return;
                }

                set_title(victim, arg3);
                return;
            }

            if (!str_prefix(arg2, "spec")) {
                if (!IS_NPC(victim)) {
                    send_to_char("Not on PC's.\n", ch);
                    return;
                }

                if ((victim.spec_fun = spec_lookup(arg3)) == null) {
                    send_to_char("No such spec fun.\n", ch);
                    return;
                }

                return;
            }
        }

        if (!str_prefix(type, "object")) {
            /* string an obj */

            if ((obj = get_obj_world(ch, arg1.toString())) == null) {
                send_to_char("Nothing like that in heaven or earth.\n", ch);
                return;
            }

            if (!str_prefix(arg2, "name")) {
                obj.name = arg3;
                return;
            }

            if (!str_prefix(arg2, "short")) {
                obj.short_descr = arg3;
                return;
            }

            if (!str_prefix(arg2, "long")) {
                obj.description = arg3;
                return;
            }

            if (!str_prefix(arg2, "ed") || !str_prefix(arg2, "extended")) {


                arg1.setLength(0);
                argument = one_argument(argument, arg1);
                if (argument == null) {
                    send_to_char("Syntax: oset <object> ed <keyword> <string>\n", ch);
                    return;
                }

                argument += "\n";

                EXTRA_DESCR_DATA ed = new EXTRA_DESCR_DATA();

                ed.keyword = arg3;
                ed.description = argument;
                ed.next = obj.extra_descr;
                obj.extra_descr = ed;
                return;
            }
        }

        /* echo bad use message */
        do_string(ch, "");
    }


    static void do_oset(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        int value;
        StringBuilder arg1 = new StringBuilder();
        StringBuilder arg2b = new StringBuilder();

        argument = smash_tilde(argument);
        argument = one_argument(argument, arg1);
        argument = one_argument(argument, arg2b);
        String arg3 = argument;

        if (arg1.length() == 0 || arg2b.length() == 0 || arg3.length() == 0) {
            send_to_char("Syntax:\n", ch);
            send_to_char("  set obj <object> <field> <value>\n", ch);
            send_to_char("  Field being one of:\n", ch);
            send_to_char("    value0 value1 value2 value3 value4 (v1-v4)\n", ch);
            send_to_char("    extra wear level weight cost timer\n", ch);
            return;
        }

        if ((obj = get_obj_world(ch, arg1.toString())) == null) {
            send_to_char("Nothing like that in heaven or earth.\n", ch);
            return;
        }

        /*
         * Snarf the value (which need not be numeric).
         */
        value = atoi(arg3);

        /*
         * Set something.
         */
        String arg2 = arg2b.toString();
        if (!str_cmp(arg2, "value0") || !str_cmp(arg2, "v0")) {
            obj.value[0] = UMIN(50, value);
            return;
        }

        if (!str_cmp(arg2, "value1") || !str_cmp(arg2, "v1")) {
            obj.value[1] = value;
            return;
        }

        if (!str_cmp(arg2, "value2") || !str_cmp(arg2, "v2")) {
            obj.value[2] = value;
            return;
        }

        if (!str_cmp(arg2, "value3") || !str_cmp(arg2, "v3")) {
            obj.value[3] = value;
            return;
        }

        if (!str_cmp(arg2, "value4") || !str_cmp(arg2, "v4")) {
            obj.value[4] = value;
            return;
        }

        if (!str_prefix(arg2, "extra")) {
            obj.extra_flags = value;
            return;
        }

        if (!str_prefix(arg2, "wear")) {
            obj.wear_flags = value;
            return;
        }

        if (!str_prefix(arg2, "level")) {
            obj.level = value;
            return;
        }

        if (!str_prefix(arg2, "weight")) {
            obj.weight = value;
            return;
        }

        if (!str_prefix(arg2, "cost")) {
            obj.cost = value;
            return;
        }

        if (!str_prefix(arg2, "timer")) {
            obj.timer = value;
            return;
        }

        /*
         * Generate usage message.
         */
        do_oset(ch, "");
    }


    static void do_rset(CHAR_DATA ch, String argument) {
        ROOM_INDEX_DATA location;
        int value;
        StringBuilder arg1 = new StringBuilder();
        StringBuilder arg2 = new StringBuilder();
        argument = smash_tilde(argument);
        argument = one_argument(argument, arg1);
        argument = one_argument(argument, arg2);
        String arg3 = argument;

        if (arg1.length() == 0 || arg2.length() == 0 || arg3.length() == 0) {
            send_to_char("Syntax:\n", ch);
            send_to_char("  set room <location> <field> <value>\n", ch);
            send_to_char("  Field being one of:\n", ch);
            send_to_char("    flags sector\n", ch);
            return;
        }

        if ((location = find_location(ch, arg1.toString())) == null) {
            send_to_char("No such location.\n", ch);
            return;
        }

/*    if (!is_room_owner(ch,location) && ch.in_room != location  */
        if (ch.in_room != location
                && room_is_private(location) && !IS_TRUSTED(ch, IMPLEMENTOR)) {
            send_to_char("That room is private right now.\n", ch);
            return;
        }

        /*
         * Snarf the value.
         */
        if (!is_number(arg3)) {
            send_to_char("Value must be numeric.\n", ch);
            return;
        }
        value = atoi(arg3);

        /*
         * Set something.
         */
        if (!str_prefix(arg2.toString(), "flags")) {
            location.room_flags = value;
            return;
        }

        if (!str_prefix(arg2.toString(), "sector")) {
            location.sector_type = value;
            return;
        }

        /*
         * Generate usage message.
         */
        do_rset(ch, "");
    }


    static void do_sockets(CHAR_DATA ch, String argument) {
        DESCRIPTOR_DATA d;
        int count;

        count = 0;
        StringBuilder argb = new StringBuilder();
        one_argument(argument, argb);
        String arg = argb.toString();
        TextBuffer buf = new TextBuffer();
        for (d = descriptor_list; d != null; d = d.next) {
            if (d.character != null && can_see(ch, d.character)
                    && (arg.length() == 0 || is_name(arg, d.character.name)
                    || (d.original != null && is_name(arg, d.original.name)))) {
                count++;
                buf.sprintf(false, "[%3d %2d] %s@%s\n",
                        d.descriptor,
                        d.connected,
                        d.original != null ? d.original.name :
                                d.character != null ? d.character.name : "(none)",
                        d.host
                );
            }
        }
        if (count == 0) {
            send_to_char("No one by that name is connected.\n", ch);
            return;
        }

        buf.sprintf(false, "%d user%s\n", count, count == 1 ? "" : "s");
        page_to_char(buf, ch);
    }

/*
* Thanks to Grodyn for pointing out bugs in this function.
*/

    static void do_force(CHAR_DATA ch, String argument) {
        StringBuilder arg = new StringBuilder();
        argument = one_argument(argument, arg);

        if (arg.length() == 0 || argument.length() == 0) {
            send_to_char("Force whom to do what?\n", ch);
            return;
        }

        StringBuilder arg2 = new StringBuilder();
        one_argument(argument, arg2);

        if (!str_cmp(arg2.toString(), "delete")) {
            send_to_char("That will NOT be done.\n", ch);
            return;
        }

        TextBuffer buf = new TextBuffer();
        buf.sprintf("$n forces you to '%s'.", argument);

        if (!str_cmp(arg.toString(), "all")) {
            CHAR_DATA vch;
            CHAR_DATA vch_next;

            if (get_trust(ch) < MAX_LEVEL - 3) {
                send_to_char("Not at your level!\n", ch);
                return;
            }

            for (vch = char_list; vch != null; vch = vch_next) {
                vch_next = vch.next;

                if (!IS_NPC(vch) && get_trust(vch) < get_trust(ch)) {
                    act(buf.toString(), ch, null, vch, TO_VICT);
                    interpret(vch, argument, true);
                }
            }
        } else if (!str_cmp(arg.toString(), "players")) {
            CHAR_DATA vch;
            CHAR_DATA vch_next;

            if (get_trust(ch) < MAX_LEVEL - 2) {
                send_to_char("Not at your level!\n", ch);
                return;
            }

            for (vch = char_list; vch != null; vch = vch_next) {
                vch_next = vch.next;

                if (!IS_NPC(vch) && get_trust(vch) < get_trust(ch)
                        && vch.level < LEVEL_HERO) {
                    act(buf.toString(), ch, null, vch, TO_VICT);
                    interpret(vch, argument, false);
                }
            }
        } else if (!str_cmp(arg.toString(), "gods")) {
            CHAR_DATA vch;
            CHAR_DATA vch_next;

            if (get_trust(ch) < MAX_LEVEL - 2) {
                send_to_char("Not at your level!\n", ch);
                return;
            }

            for (vch = char_list; vch != null; vch = vch_next) {
                vch_next = vch.next;

                if (!IS_NPC(vch) && get_trust(vch) < get_trust(ch)
                        && vch.level >= LEVEL_HERO) {
                    act(buf.toString(), ch, null, vch, TO_VICT);
                    interpret(vch, argument, false);
                }
            }
        } else {
            CHAR_DATA victim;

            if ((victim = get_char_world(ch, arg.toString())) == null) {
                send_to_char("They aren't here.\n", ch);
                return;
            }

            if (victim == ch) {
                send_to_char("Aye aye, right away!\n", ch);
                return;
            }

            if (!is_room_owner(ch, victim.in_room)
                    && ch.in_room != victim.in_room
                    && room_is_private(victim.in_room) && !IS_TRUSTED(ch, IMPLEMENTOR)) {
                send_to_char("That character is in a private room.\n", ch);
                return;
            }

            if (get_trust(victim) >= get_trust(ch)) {
                send_to_char("Do it yourself!\n", ch);
                return;
            }

            if (!IS_NPC(victim) && get_trust(ch) < MAX_LEVEL - 3) {
                send_to_char("Not at your level!\n", ch);
                return;
            }

            act(buf.toString(), ch, null, victim, TO_VICT);
            interpret(victim, argument, false);
        }

        send_to_char("Ok.\n", ch);
    }

/*
* New routines by Dionysos.
*/

    static void do_invis(CHAR_DATA ch, String argument) {
        int level;

        /* RT code for taking a level argument */
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0)
            /* take the default path */

        {
            if (ch.invis_level != 0) {
                ch.invis_level = 0;
                act("$n slowly fades into existence.", ch, null, null, TO_ROOM);
                send_to_char("You slowly fade back into existence.\n", ch);
            } else {
                ch.invis_level = get_trust(ch);
                act("$n slowly fades into thin air.", ch, null, null, TO_ROOM);
                send_to_char("You slowly vanish into thin air.\n", ch);
            }
        } else
            /* do the level thing */ {
            level = atoi(arg.toString());
            if (level < 2 || level > get_trust(ch)) {
                send_to_char("Invis level must be between 2 and your level.\n", ch);
            } else {
                ch.reply = null;
                ch.invis_level = level;
                act("$n slowly fades into thin air.", ch, null, null, TO_ROOM);
                send_to_char("You slowly vanish into thin air.\n", ch);
            }
        }

    }


    static void do_incognito(CHAR_DATA ch, String argument) {
        int level;

        /* RT code for taking a level argument */
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0)
            /* take the default path */

        {
            if (ch.incog_level != 0) {
                ch.incog_level = 0;
                act("$n is no longer cloaked.", ch, null, null, TO_ROOM);
                send_to_char("You are no longer cloaked.\n", ch);
            } else {
                ch.incog_level = get_trust(ch);
                act("$n cloaks $s presence.", ch, null, null, TO_ROOM);
                send_to_char("You cloak your presence.\n", ch);
            }
        } else
            /* do the level thing */ {
            level = atoi(arg.toString());
            if (level < 2 || level > get_trust(ch)) {
                send_to_char("Incog level must be between 2 and your level.\n", ch);
            } else {
                ch.reply = null;
                ch.incog_level = level;
                act("$n cloaks $s presence.", ch, null, null, TO_ROOM);
                send_to_char("You cloak your presence.\n", ch);
            }
        }

    }


    static void do_holylight(CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }

        if (IS_SET(ch.act, PLR_HOLYLIGHT)) {
            ch.act = REMOVE_BIT(ch.act, PLR_HOLYLIGHT);
            send_to_char("Holy light mode off.\n", ch);
        } else {
            ch.act = SET_BIT(ch.act, PLR_HOLYLIGHT);
            send_to_char("Holy light mode on.\n", ch);
        }

    }

/* prefix command: it will put the string typed on each line typed */

    static void do_prefi(CHAR_DATA ch) {
        send_to_char("You cannot abbreviate the prefix command.\n", ch);
    }

    static void do_prefix(CHAR_DATA ch, String argument) {

        if (argument.length() == 0) {
            if (ch.prefix.length() == 0) {
                send_to_char("You have no prefix to clear.\n", ch);
                return;
            }

            send_to_char("Prefix removed.\n", ch);
            ch.prefix = "";
            return;
        }

        TextBuffer buf = new TextBuffer();
        if (ch.prefix.length() != 0) {
            buf.sprintf("Prefix changed to %s.\n", argument);
        } else {
            buf.sprintf("Prefix set to %s.\n", argument);
        }

        ch.prefix = argument;
        send_to_char(buf, ch);

    }

/* RT nochannels command, for those spammers */

    static void do_grant(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;

        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0) {
            send_to_char("Grant whom induct privileges?", ch);
            return;
        }

        if ((victim = get_char_world(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (IS_SET(victim.act, PLR_CANINDUCT)) {
            victim.act = REMOVE_BIT(victim.act, PLR_CANINDUCT);
            send_to_char("You have the lost the power to INDUCT.\n", victim);
            send_to_char("INDUCT powers removed.\n", ch);
        } else {
            victim.act = SET_BIT(victim.act, PLR_CANINDUCT);
            send_to_char("You have been given the power to INDUCT.\n", victim);
            send_to_char("INDUCT powers given.\n", ch);
        }

    }

    static void do_advance(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        int level;
        int iLevel;

        StringBuilder arg1 = new StringBuilder();
        StringBuilder arg2 = new StringBuilder();
        argument = one_argument(argument, arg1);
        one_argument(argument, arg2);

        if (arg1.length() == 0 || arg2.length() == 0 || !is_number(arg2)) {
            send_to_char("Syntax: advance <char> <level>.\n", ch);
            return;
        }

        if ((victim = get_char_room(ch, arg1.toString())) == null) {
            send_to_char("That player is not here.\n", ch);
            return;
        }

        if (IS_NPC(victim)) {
            send_to_char("Not on NPC's.\n", ch);
            return;
        }

        if ((level = atoi(arg2.toString())) < 1 || level > 100) {
            send_to_char("Level must be 1 to 100.\n", ch);
            return;
        }

        if (level > get_trust(ch)) {
            send_to_char("Limited to your trust level.\n", ch);
            return;
        }

        /* Level counting */
        if (ch.level <= 5 || ch.level > LEVEL_HERO) {
            if (5 < level && level <= LEVEL_HERO) {
                total_levels += level - 5;
            }
        } else {
            if (5 < level && level <= LEVEL_HERO) {
                total_levels += level - ch.level;
            } else {
                total_levels -= (ch.level - 5);
            }
        }

        /*
         * Lower level:
         *   Reset to level 1.
         *   Then raise again.
         *   Currently, an imp can lower another imp.
         *   -- Swiftest
         */
        if (level <= victim.level) {
            int temp_prac;

            send_to_char("Lowering a player's level!\n", ch);
            send_to_char("**** OOOOHHHHHHHHHH  NNNNOOOO ****\n", victim);
            temp_prac = victim.practice;
            victim.level = 1;
            victim.exp = exp_to_level(victim, victim.pcdata.points);
            victim.max_hit = 10;
            victim.max_mana = 100;
            victim.max_move = 100;
            victim.practice = 0;
            victim.hit = victim.max_hit;
            victim.mana = victim.max_mana;
            victim.move = victim.max_move;
            advance_level(victim);
            victim.practice = temp_prac;
        } else {
            send_to_char("Raising a player's level!\n", ch);
            send_to_char("**** OOOOHHHHHHHHHH  YYYYEEEESSS ****\n", victim);
        }

        for (iLevel = victim.level; iLevel < level; iLevel++) {
            send_to_char("You raise a level!!  ", victim);
            victim.exp += exp_to_level(victim, victim.pcdata.points);
            victim.level += 1;
            advance_level(victim);
        }
        victim.trust = 0;
        save_char_obj(victim);
    }

    static void do_mset(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        int value;
        StringBuilder arg1 = new StringBuilder();
        StringBuilder arg2 = new StringBuilder();
        argument = smash_tilde(argument);
        argument = one_argument(argument, arg1);
        argument = one_argument(argument, arg2);
        String arg3 = argument;

        if (arg1.length() == 0 || arg2.length() == 0 || arg3.length() == 0) {
            send_to_char("Syntax:\n", ch);
            send_to_char("  set char <name> <field> <value>\n", ch);
            send_to_char("  Field being one of:\n", ch);
            send_to_char("    str int wis dex con cha sex class level\n", ch);
            send_to_char("    race gold hp mana move practice align\n", ch);
            send_to_char("    train thirst drunk full hometown ethos\n", ch);

            /*** Added By KIO ***/
            send_to_char("    questp questt relig bloodlust desire\n", ch);
            /*** Added By KIO ***/
            return;
        }

        if ((victim = get_char_world(ch, arg1.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        /*
         * Snarf the value (which need not be numeric).
         */
        value = is_number(arg3) ? atoi(arg3) : -1;

        /*
         * Set something.
         */
        TextBuffer buf = new TextBuffer();
        String arg2str = arg2.toString();
        if (!str_cmp(arg2str, "str")) {
            if (value < 3 || value > get_max_train(victim, STAT_STR)) {
                buf.sprintf("Strength range is 3 to %d\n.", get_max_train(victim, STAT_STR));
                send_to_char(buf, ch);
                return;
            }

            victim.perm_stat[STAT_STR] = value;
            return;
        }

        if (!str_cmp(arg2str, "int")) {
            if (value < 3 || value > get_max_train(victim, STAT_INT)) {
                buf.sprintf(
                        "Intelligence range is 3 to %d.\n",
                        get_max_train(victim, STAT_INT));
                send_to_char(buf, ch);
                return;
            }

            victim.perm_stat[STAT_INT] = value;
            return;
        }

        if (!str_cmp(arg2str, "wis")) {
            if (value < 3 || value > get_max_train(victim, STAT_WIS)) {
                buf.sprintf("Wisdom range is 3 to %d.\n", get_max_train(victim, STAT_WIS));
                send_to_char(buf, ch);
                return;
            }

            victim.perm_stat[STAT_WIS] = value;
            return;
        }
        /*** Added By KIO  ***/
        if (!str_cmp(arg2str, "questp")) {
            if (value == -1) {
                value = 0;
            }
            if (!IS_NPC(victim)) {
                victim.pcdata.questpoints = value;
            }
            return;
        }
        if (!str_cmp(arg2str, "questt")) {
            if (value == -1) {
                value = 30;
            }
            if (!IS_NPC(victim)) {
                victim.pcdata.nextquest = value;
            }
            return;
        }
        if (!str_cmp(arg2str, "relig")) {
            if (value == -1) {
                value = 0;
            }
            victim.religion = value;
            return;
        }
        /*** Added By KIO ***/


        if (!str_cmp(arg2str, "dex")) {
            if (value < 3 || value > get_max_train(victim, STAT_DEX)) {
                buf.sprintf("Dexterity ranges is 3 to %d.\n",
                        get_max_train(victim, STAT_DEX));
                send_to_char(buf, ch);
                return;
            }

            victim.perm_stat[STAT_DEX] = value;
            return;
        }

        if (!str_cmp(arg2str, "con")) {
            if (value < 3 || value > get_max_train(victim, STAT_CON)) {
                buf.sprintf("Constitution range is 3 to %d.\n",
                        get_max_train(victim, STAT_CON));
                send_to_char(buf, ch);
                return;
            }

            victim.perm_stat[STAT_CON] = value;
            return;
        }
        if (!str_cmp(arg2str, "cha")) {
            if (value < 3 || value > get_max_train(victim, STAT_CHA)) {
                buf.sprintf("Constitution range is 3 to %d.\n",
                        get_max_train(victim, STAT_CHA));
                send_to_char(buf, ch);
                return;
            }

            victim.perm_stat[STAT_CHA] = value;
            return;
        }

        if (!str_prefix(arg2str, "sex")) {
            if (value < 0 || value > 2) {
                send_to_char("Sex range is 0 to 2.\n", ch);
                return;
            }
            if ((victim.sex | SEX_FEMALE) == 0 || (victim.sex | SEX_MALE) == 0) {
                send_to_char("You can't change their sex.\n", ch);
                return;
            }
            victim.sex = value;
            if (!IS_NPC(victim)) {
                victim.pcdata.true_sex = value;
            }
            return;
        }

        if (!str_prefix(arg2str, "class")) {
            if (IS_NPC(victim)) {
                send_to_char("Mobiles have no class.\n", ch);
                return;
            }

            Clazz clazz = Clazz.lookupClass(arg3);
            if (clazz == null) {
                buf.sprintf("Possible classes are: ");
                ArrayList<Clazz> classes = Clazz.getClasses();
                for (int i = 0; i < classes.size(); i++) {
                    Clazz c = classes.get(i);
                    if (i > 0) {
                        buf.append(" ");
                    }
                    buf.append(c.name);
                }
                buf.append(".\n");
                send_to_char(buf, ch);
                return;
            }

            victim.clazz = clazz;
            victim.exp = victim.level * exp_per_level(victim, 0);
            return;
        }

        if (!str_prefix(arg2str, "level")) {
            if (!IS_NPC(victim)) {
                send_to_char("Not on PC's.\n", ch);
                return;
            }

            if (value < 0 || value > 100) {
                send_to_char("Level range is 0 to 100.\n", ch);
                return;
            }
            victim.level = value;
            return;
        }

        if (!str_prefix(arg2str, "gold")) {
            victim.gold = value;
            return;
        }

        if (!str_prefix(arg2str, "hp")) {
            if (value < -10 || value > 30000) {
                send_to_char("Hp range is -10 to 30,000 hit points.\n", ch);
                return;
            }
            victim.max_hit = value;
            if (!IS_NPC(victim)) {
                victim.pcdata.perm_hit = value;
            }
            return;
        }

        if (!str_prefix(arg2str, "mana")) {
            if (value < 0 || value > 60000) {
                send_to_char("Mana range is 0 to 60,000 mana points.\n", ch);
                return;
            }
            victim.max_mana = value;
            if (!IS_NPC(victim)) {
                victim.pcdata.perm_mana = value;
            }
            return;
        }

        if (!str_prefix(arg2str, "move")) {
            if (value < 0 || value > 60000) {
                send_to_char("Move range is 0 to 60,000 move points.\n", ch);
                return;
            }
            victim.max_move = value;
            if (!IS_NPC(victim)) {
                victim.pcdata.perm_move = value;
            }
            return;
        }

        if (!str_prefix(arg2str, "practice")) {
            if (value < 0 || value > 250) {
                send_to_char("Practice range is 0 to 250 sessions.\n", ch);
                return;
            }
            victim.practice = value;
            return;
        }

        if (!str_prefix(arg2str, "train")) {
            if (value < 0 || value > 50) {
                send_to_char("Training session range is 0 to 50 sessions.\n", ch);
                return;
            }
            victim.train = value;
            return;
        }

        if (!str_prefix(arg2str, "align")) {
            if (value < -1000 || value > 1000) {
                send_to_char("Alignment range is -1000 to 1000.\n", ch);
                return;
            }
            victim.alignment = value;
            send_to_char("Remember to check their hometown.\n", ch);
            return;
        }

        if (!str_prefix(arg2str, "ethos")) {
            if (IS_NPC(victim)) {
                send_to_char("Mobiles don't have an ethos.\n", ch);
                return;
            }
            if (value < 0 || value > 3) {
                send_to_char("The values are Lawful - 1, Neutral - 2, Chaotic - 3\n", ch);
                return;
            }

            victim.ethos = value;
            return;
        }

        if (!str_prefix(arg2str, "hometown")) {
            if (IS_NPC(victim)) {
                send_to_char("Mobiles don't have hometowns.\n", ch);
                return;
            }
            if (value < 0 || value > 4) {
                send_to_char("Please choose one of the following :.\n", ch);
                send_to_char("Town        Alignment       Value\n", ch);
                send_to_char("----        ---------       -----\n", ch);
                send_to_char("Midgaard     Any              0\n", ch);
                send_to_char("New Thalos   Any              1\n", ch);
                send_to_char("Titan        Any              2\n", ch);
                send_to_char("Ofcol        Neutral          3\n", ch);
                send_to_char("Old Midgaard Evil             4\n", ch);
                return;
            }

            if ((value == 2 && !IS_GOOD(victim)) || (value == 3 &&
                    !IS_NEUTRAL(victim)) || (value == 4 && !IS_EVIL(victim))) {
                send_to_char("The hometown doesn't match this character's alignment.\n", ch);
                return;
            }

            victim.hometown = value;
            return;
        }

        if (!str_prefix(arg2str, "thirst")) {
            if (IS_NPC(victim)) {
                send_to_char("Not on NPC's.\n", ch);
                return;
            }

            if (value < -1 || value > 100) {
                send_to_char("Thirst range is -1 to 100.\n", ch);
                return;
            }

            victim.pcdata.condition[COND_THIRST] = value;
            return;
        }

        if (!str_prefix(arg2str, "drunk")) {
            if (IS_NPC(victim)) {
                send_to_char("Not on NPC's.\n", ch);
                return;
            }

            if (value < -1 || value > 100) {
                send_to_char("Drunk range is -1 to 100.\n", ch);
                return;
            }

            victim.pcdata.condition[COND_DRUNK] = value;
            return;
        }

        if (!str_prefix(arg2str, "full")) {
            if (IS_NPC(victim)) {
                send_to_char("Not on NPC's.\n", ch);
                return;
            }

            if (value < -1 || value > 100) {
                send_to_char("Full range is -1 to 100.\n", ch);
                return;
            }

            victim.pcdata.condition[COND_FULL] = value;
            return;
        }

        if (!str_prefix(arg2str, "bloodlust")) {
            if (IS_NPC(victim)) {
                send_to_char("Not on NPC's.\n", ch);
                return;
            }

            if (value < -1 || value > 100) {
                send_to_char("Full range is -1 to 100.\n", ch);
                return;
            }

            victim.pcdata.condition[COND_BLOODLUST] = value;
            return;
        }

        if (!str_prefix(arg2str, "desire")) {
            if (IS_NPC(victim)) {
                send_to_char("Not on NPC's.\n", ch);
                return;
            }

            if (value < -1 || value > 100) {
                send_to_char("Full range is -1 to 100.\n", ch);
                return;
            }

            victim.pcdata.condition[COND_DESIRE] = value;
            return;
        }

        if (!str_prefix(arg2str, "race")) {
            Race race = Race.lookupRace(arg3);

            if (race == null) {
                send_to_char("That is not a valid race.\n", ch);
                return;
            }

            if (!IS_NPC(victim) && race.pcRace == null) {
                send_to_char("That is not a valid player race.\n", ch);
                return;
            }

            if (!IS_NPC(victim)) {
                for (Skill sn : Skill.skills) {
                    if ((sn.name != null) && !RACE_OK(victim, sn)) {
                        victim.pcdata.learned[sn.ordinal()] = 0;
                    }

                    if ((sn.name != null) && (ORG_RACE(victim) == sn.race)) {
                        victim.pcdata.learned[sn.ordinal()] = 70;
                    }
                }
            }

            if (ORG_RACE(victim) == victim.race) {
                victim.race = race;
            }
            SET_ORG_RACE(victim, race);

            victim.exp = victim.level * exp_per_level(victim, 0);
            return;
        }

        /*
        * Generate usage message.
        */
        do_mset(ch, "");
    }

    static void do_induct(CHAR_DATA ch, String argument) {
        String cabal;
        CHAR_DATA victim;
        int i, prev_cabal;

        StringBuilder arg1 = new StringBuilder();
        argument = one_argument(argument, arg1);
        String arg2 = argument;

        if (arg1.length() == 0 || arg2.length() == 0) {
            send_to_char("Usage: induct <player> <cabal>\n", ch);
            return;
        }


        if ((victim = get_char_world(ch, arg1.toString())) == null) {
            send_to_char("That player istat_buf.setLength(0)'t on.\n", ch);
            return;
        }

        if (IS_NPC(victim)) {
            act("$N is not smart enough to join a cabal.", ch, null, victim, TO_CHAR);
            return;
        }

        if (CANT_CHANGE_TITLE(victim)) {
            act("$N has tried to join a cabal, but failed.", ch, null, victim, TO_CHAR);
            return;
        }

        if ((i = cabal_lookup(arg2)) == -1) {
            send_to_char("I've never heard of that cabal.\n", ch);
            return;
        }

        if (victim.clazz == Clazz.WARRIOR && i == CABAL_SHALAFI) {
            act("But $N is a filthy warrior!", ch, null, victim, TO_CHAR);
            return;
        }

        if (i == CABAL_RULER && get_curr_stat(victim, STAT_INT) < 20) {
            act("$N is not clever enough to become a Ruler!", ch, null, victim, TO_CHAR);
            return;
        }

        if (IS_TRUSTED(ch, LEVEL_IMMORTAL) ||
                (IS_SET(ch.act, PLR_CANINDUCT) &&
                        ((i == CABAL_NONE && (ch.cabal == victim.cabal))
                                ||
                                (i != CABAL_NONE && ch.cabal == i && victim.cabal == CABAL_NONE)))) {
            prev_cabal = victim.cabal;
            victim.cabal = i;
            victim.act = REMOVE_BIT(victim.act, PLR_CANINDUCT);
            cabal = cabal_table[i].long_name;
        } else {
            send_to_char("You do not have that power.\n", ch);
            return;
        }

        /* set cabal skills to 70, remove other cabal skills */
        for (Skill sn : Skill.skills) {
            if ((victim.cabal != 0) && (sn.cabal == victim.cabal)) {
                victim.pcdata.learned[sn.ordinal()] = 70;
            } else if (sn.cabal != CABAL_NONE) {
                victim.pcdata.learned[sn.ordinal()] = 0;
            }
        }

        TextBuffer buf = new TextBuffer();
        buf.sprintf("$n has been inducted into %s.", cabal);
        act(buf, victim, null, null, TO_NOTVICT);
        buf.sprintf("You have been inducted into %s.", cabal);
        act(buf, victim, null, null, TO_CHAR);
        if (ch.in_room != victim.in_room) {
            buf.sprintf("%s has been inducted into %s.\n",
                    IS_NPC(victim) ? victim.short_descr : victim.name, cabal);
            send_to_char(buf, ch);
        }
        if (victim.cabal == CABAL_NONE && prev_cabal != CABAL_NONE) {
            TextBuffer name = new TextBuffer();
            switch (prev_cabal) {
                default:
                    return;
                case CABAL_BATTLE:
                    sprintf(name, "The LOVER OF MAGIC.");
                    break;
                case CABAL_SHALAFI:
                    sprintf(name, "The HATER OF MAGIC.");
                    break;
                case CABAL_KNIGHT:
                    sprintf(name, "The UNHONOURABLE FIGHTER.");
                    break;
                case CABAL_INVADER:
                case CABAL_CHAOS:
                case CABAL_LIONS:
                case CABAL_HUNTER:
                case CABAL_RULER:
                    sprintf(name, "NO MORE CABALS.");
                    break;
            }
            set_title(victim, name.toString());
            victim.act = SET_BIT(victim.act, PLR_NO_TITLE);
        }
    }

    static void do_desocket(CHAR_DATA ch, String argument) {
        DESCRIPTOR_DATA d, d_next;
        int socket;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (!is_number(arg)) {
            send_to_char("The argument must be a number.\n", ch);
            return;
        }

        if (arg.length() == 0) {
            send_to_char("Disconnect which socket?\n", ch);
            return;
        }

        socket = atoi(arg.toString());
        for (d = descriptor_list; d != null; d = d_next) {
            d_next = d.next;
            if (d.descriptor.hashCode() == socket) {
                if (d.character == ch) {
                    send_to_char("It would be foolish to disconnect yourself.\n", ch);
                    return;
                }
                if (d.connected == CON_PLAYING) {
                    send_to_char("Why don't you just use disconnect?\n", ch);
                    return;
                }
                write_to_descriptor(d.descriptor, "You are being disconnected by an immortal.");
                close_socket(d);
                send_to_char("Done.\n", ch);
                return;
            }
        }
        send_to_char("No such socket is connected.\n", ch);

    }

    static void do_smite(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;

        if (argument.length() == 0) {
            send_to_char("You are so frustrated you smite yourself!  OWW!\n",
                    ch);
            return;
        }

        if ((victim = get_char_world(ch, argument)) == null) {
            send_to_char("You'll have to smite them some other day.\n", ch);
            return;
        }

        if (IS_NPC(victim)) {
            send_to_char("That poor mob never did anything to you.\n", ch);
            return;
        }

        if (victim.trust > ch.trust) {
            send_to_char("How dare you!\n", ch);
            return;
        }

        if (victim.position < POS_SLEEPING) {
            send_to_char("Take pity on the poor thing.\n", ch);
            return;
        }

        act("A bolt comes down out of the heavens and smites you!", victim, null,
                ch, TO_CHAR);
        act("You reach down and smite $n!", victim, null, ch, TO_VICT);
        act("A bolt from the heavens smites $n!", victim, null, ch, TO_NOTVICT);
        victim.hit = victim.hit / 2;
    }

    static void do_popularity(CHAR_DATA ch) {
        AREA_DATA area;
        int i;
        TextBuffer buf = new TextBuffer();

        buf.sprintf("Area popularity statistics (in String  ticks)\n");

        for (area = area_first, i = 0; area != null; area = area.next, i++) {
            if (area.count >= 5000000) {
                buf.sprintf(false, "%-20s overflow       ", area.name);
            } else {
                buf.sprintf(false, "%-20s %-8lu       ", area.name, area.count);
            }
            if (i % 2 == 0) {
                buf.append("\n");
            }
        }
        buf.append("\r\n\n");
        page_to_char(buf, ch);
    }

    static void do_ititle(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        StringBuilder arg = new StringBuilder();
        argument = one_argument(argument, arg);

        if (arg.length() == 0) {
            send_to_char("Change whose title to what?\n", ch);
            return;
        }

        victim = get_char_world(ch, arg.toString());
        if (victim == null) {
            send_to_char("Nobody is playing with that name.\n", ch);
            return;
        }

        if (IS_NPC(ch)) {
            return;
        }

        if (argument.length() == 0) {
            send_to_char("Change the title to what?\n", ch);
            return;
        }

        if (argument.length() > 45) {
            argument = argument.substring(45);
        }

        argument = smash_tilde(argument);
        set_title(victim, argument);
        send_to_char("Ok.\n", ch);
    }


    static void do_rename(CHAR_DATA ch, String argument) {

        CHAR_DATA victim;
        StringBuilder old_name = new StringBuilder();
        StringBuilder new_name = new StringBuilder();
        argument = one_argument(argument, old_name);
        one_argument(argument, new_name);

        if (old_name.length() == 0) {
            send_to_char("Rename who?\n", ch);
            return;
        }

        victim = get_char_world(ch, old_name.toString());

        if (victim == null) {
            send_to_char("There is no such a person online.\n", ch);
            return;
        }

        if (IS_NPC(victim)) {
            send_to_char("You cannot use Rename on NPCs.\n", ch);
            return;
        }

        if ((victim != ch) && (get_trust(victim) >= get_trust(ch))) {
            send_to_char("You failed.\n", ch);
            return;
        }

        if (victim.desc == null || (victim.desc.connected != CON_PLAYING)) {
            send_to_char("This player has lost his link or is inside a pager or the like.\n", ch);
            return;
        }

        if (new_name.length() == 0) {
            send_to_char("Rename to what new name?\n", ch);
            return;
        }

/*
    if (victim.cabal)
    {
     send_to_char ("This player is member of a cabal, remove him from there first.\n",ch);
     return;
    }
*/

        new_name.setCharAt(0, UPPER(new_name.charAt(0)));
        if (!check_parse_name(new_name.toString())) {
            send_to_char("The new name is illegal.\n", ch);
            return;
        }

        String strsave = nw_config.lib_player_dir + "/" + capitalize(new_name.toString());
        if (new File(strsave).exists()) {
            send_to_char("A player with that name already exists!\n", ch);
            return;
        }

        if (get_char_world(ch, new_name.toString()) != null) {
            send_to_char("A player with the name you specified already exists!\n", ch);
            return;
        }

        strsave = nw_config.lib_player_dir + "/" + capitalize(victim.name);

/*
 * NOTE: Players who are level 1 do NOT get saved under a new name
 */
        victim.name = capitalize(new_name.toString());

        save_char_obj(victim);

        //noinspection ResultOfMethodCallIgnored
        new File(strsave).delete();
        send_to_char("Character renamed.\n", ch);
        victim.position = POS_STANDING;
        act("$n has renamed you to $N!", ch, null, victim, TO_VICT);

    }

    static void do_notitle(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;

        if (!IS_IMMORTAL(ch)) {
            return;
        }
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0) {
            send_to_char("Usage:\n  notitle <player>\n", ch);
            return;
        }

        if ((victim = get_char_world(ch, arg.toString())) == null) {
            send_to_char("He is not currently playing.\n", ch);
            return;
        }

        if (IS_SET(victim.act, PLR_NO_TITLE)) {
            victim.act = REMOVE_BIT(victim.act, PLR_NO_TITLE);
            send_to_char("You can change your title again.\n", victim);
            send_to_char("Ok.\n", ch);
        } else {
            victim.act = SET_BIT(victim.act, PLR_NO_TITLE);
            send_to_char("You won't be able to change your title anymore.\n", victim);
            send_to_char("Ok.\n", ch);
        }
    }


    static void do_noaffect(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;

        if (!IS_IMMORTAL(ch)) {
            return;
        }
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);


        if (arg.length() == 0) {
            send_to_char("Usage:\n  noaffect <player>\n", ch);
            return;
        }

        if ((victim = get_char_world(ch, arg.toString())) == null) {
            send_to_char("He is not currently playing.\n", ch);
            return;
        }


        for (AFFECT_DATA paf = victim.affected, paf_next; paf != null; paf = paf_next) {
            paf_next = paf.next;
            if (paf.duration >= 0) {
                if (paf.type != null && paf.type.msg_off != null) {
                    send_to_char(paf.type.msg_off, victim);
                    send_to_char("\n", victim);
                }

                affect_remove(victim, paf);
            }
        }

    }

    static void do_affrooms(CHAR_DATA ch) {
        ROOM_INDEX_DATA room;
        ROOM_INDEX_DATA room_next;
        int count = 0;

        if (top_affected_room == null) {
            send_to_char("No affected room.\n", ch);
        }
        for (room = top_affected_room; room != null; room = room_next) {
            room_next = room.aff_next;
            count++;
            TextBuffer buf = new TextBuffer();
            buf.sprintf("%d) [Vnum : %5d] %s\n",
                    count, room.vnum, room.name);
            send_to_char(buf, ch);
        }
    }

    static void do_find(CHAR_DATA ch, String argument) {
        ROOM_INDEX_DATA location;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);
        if (arg.length() == 0) {
            send_to_char("Ok. But what I should find?\n", ch);
            return;
        }

        if ((location = find_location(ch, arg.toString())) == null) {
            send_to_char("No such location.\n", ch);
            return;
        }

        TextBuffer buf = new TextBuffer();
        buf.sprintf("%s.\n", find_way(ch, ch.in_room, location));
        send_to_char(buf, ch);
        buf.sprintf("From %d to %d: %s", ch.in_room.vnum, location.vnum, buf);
        log_string(buf.toString());
    }


    static void do_reboot(CHAR_DATA ch, String argument) {
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0) {
            send_to_char("Usage: reboot now\n", ch);
            send_to_char("Usage: reboot <ticks to reboot>\n", ch);
            send_to_char("Usage: reboot cancel\n", ch);
            send_to_char("Usage: reboot status\n", ch);
            return;
        }

        if (is_name(arg.toString(), "cancel")) {
            if (time_sync != 0) {
                send_to_char("Time synchronization is activated, you cannot cancel the reboot.\n", ch);
                return;
            }
            reboot_counter = -1;
            send_to_char("Reboot canceled.\n", ch);
            return;
        }

        if (is_name(arg.toString(), "now")) {
            reboot_nightworks(true, NIGHTWORKS_REBOOT);
            return;
        }

        if (is_name(arg.toString(), "status")) {
            if (time_sync != 0) {
                send_to_char("Time synchronization is activated.\n", ch);
                return;
            }
            TextBuffer buf = new TextBuffer();
            if (reboot_counter == -1) {
                buf.sprintf("Only time synchronization reboot is activated.\n");
            } else {
                buf.sprintf("Reboot in %i minutes.\n", reboot_counter);
            }
            send_to_char(buf, ch);
            return;
        }

        if (is_number(arg)) {
            if (time_sync != 0) {
                send_to_char("Time synchronization is activated, you cannot change the reboot.\n", ch);
                return;
            }
            reboot_counter = atoi(arg.toString());
            TextBuffer buf = new TextBuffer();
            buf.sprintf("Nightworks will reboot in %i ticks.\n", reboot_counter);
            send_to_char(buf, ch);
            return;
        }

        do_reboot(ch, "");
    }


    static void reboot_nightworks(boolean fMessage, int fType) {
        DESCRIPTOR_DATA d, d_next;
        String buf;
        String log_buf;
        if (fType == NIGHTWORKS_REBOOT) {
            log_buf = "Rebooting NIGHTWORKS.";
            buf = "Nightworks is going down for reboot NOW!\n";
        } else {
            log_buf = "Shutting down NIGHTWORKS.";
            buf = "Nightworks is going down for halt NOW!\n";
        }
        log_string(log_buf);

        nw_down = true;
        nw_exit = fType;

        for (d = descriptor_list; d != null; d = d_next) {
            d_next = d.next;
            if (fMessage) {
                write_to_buffer(d, buf);
            }
            if (d.character != null) {
                save_char_obj(d.character);
            }
            close_socket(d);
        }
    }


    static void do_premort(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;

        if (!IS_IMMORTAL(ch)) {
            return;
        }
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0) {
            send_to_char("Usage:\n  premort <player>\n", ch);
            return;
        }

        if ((victim = get_char_world(ch, arg.toString())) == null) {
            send_to_char("He is not currently playing.\n", ch);
            return;
        }

        if (IS_NPC(victim) || victim.level < LEVEL_HERO) {
            send_to_char("You cannot give remorting permissions to that!.\n", ch);
            return;
        }
        if (IS_SET(victim.act, PLR_CANREMORT)) {
            victim.act = REMOVE_BIT(victim.act, PLR_CANREMORT);
            send_to_char("You have lost your remorting permission.\n", victim);
            send_to_char("Ok.\n", ch);
        } else {
            victim.act = SET_BIT(victim.act, PLR_CANREMORT);
            send_to_char("You are given the permission to remort.\n", victim);
            send_to_char("Ok.\n", ch);
        }
    }

    static void do_maximum(CHAR_DATA ch, String argument) {
        StringBuilder argb = new StringBuilder();
        argument = one_argument(argument, argb);

        if (argb.length() == 0) {
            send_to_char("Usage: maximum status\n", ch);
            send_to_char("Usage: maximum reset\n", ch);
            send_to_char("Usage: maximum newbies <number of newbies>\n", ch);
            send_to_char("Usage: maximum oldies <number of oldies>\n", ch);
            return;
        }

        String arg = argb.toString();
        TextBuffer buf = new TextBuffer();

        if (is_name(arg, "status")) {
            buf.sprintf("Maximum oldies allowed: %d.\n", max_oldies);
            send_to_char(buf, ch);
            buf.sprintf("Maximum newbies allowed: %d.\n", max_newbies);
            send_to_char(buf, ch);
            buf.sprintf("Current number of players: %d.\n", iNumPlayers);
            send_to_char(buf, ch);
            return;
        }

        if (is_name(arg, "reset")) {
            max_newbies = MAX_NEWBIES;
            max_oldies = MAX_OLDIES;
            buf.sprintf("Maximum newbies and oldies have been reset.\n");
            send_to_char(buf, ch);
            do_maximum(ch, "status");
            return;
        }

        if (is_name(arg, "newbies")) {
            argb.setLength(0);
            one_argument(argument, argb);
            arg = argb.toString();
            if (!is_number(arg)) {
                do_maximum(ch, "");
                return;
            }
            max_newbies = atoi(arg);
            if (max_newbies < 0) {
                buf.sprintf("No newbies are allowed!!!\n");
            } else {
                buf.sprintf("Now maximum newbies allowed: %d.\n", max_newbies);
            }
            send_to_char(buf, ch);
            return;
        }

        if (is_name(arg, "oldies")) {
            argb.setLength(0);
            one_argument(argument, argb);
            arg = argb.toString();
            if (!is_number(arg)) {
                do_maximum(ch, "");
                return;
            }
            max_oldies = atoi(arg);
            if (max_oldies < 0) {
                buf.sprintf("No oldies are allowed!!!\n");
            } else {
                buf.sprintf("Now maximum oldies allowed: %d.\n", max_oldies);
            }
            send_to_char(buf, ch);
            return;
        }

        do_maximum(ch, "");
    }
}
