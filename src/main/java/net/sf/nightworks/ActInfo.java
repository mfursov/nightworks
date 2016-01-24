package net.sf.nightworks;

import net.sf.nightworks.util.TextBuffer;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static net.sf.nightworks.ActComm.add_follower;
import static net.sf.nightworks.ActComm.die_follower;
import static net.sf.nightworks.ActComm.do_say;
import static net.sf.nightworks.ActComm.stop_follower;
import static net.sf.nightworks.ActMove.dir_name;
import static net.sf.nightworks.ActMove.do_scan2;
import static net.sf.nightworks.ActSkill.check_improve;
import static net.sf.nightworks.ActSkill.exp_to_level;
import static net.sf.nightworks.Comm.act;
import static net.sf.nightworks.Comm.get_stat_alias;
import static net.sf.nightworks.Comm.page_to_char;
import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.Const.hometown_table;
import static net.sf.nightworks.Const.int_app;
import static net.sf.nightworks.Const.liq_table;
import static net.sf.nightworks.Const.religion_table;
import static net.sf.nightworks.DB.bug;
import static net.sf.nightworks.DB.clone_mobile;
import static net.sf.nightworks.DB.create_mobile;
import static net.sf.nightworks.DB.create_object;
import static net.sf.nightworks.DB.dice;
import static net.sf.nightworks.DB.get_extra_descr;
import static net.sf.nightworks.DB.get_mob_index;
import static net.sf.nightworks.DB.get_obj_index;
import static net.sf.nightworks.DB.interpolate;
import static net.sf.nightworks.DB.number_fuzzy;
import static net.sf.nightworks.DB.number_percent;
import static net.sf.nightworks.DB.time_info;
import static net.sf.nightworks.DB.upfirst;
import static net.sf.nightworks.DB.weather_info;
import static net.sf.nightworks.Fight.do_murder;
import static net.sf.nightworks.Fight.is_safe;
import static net.sf.nightworks.Fight.is_safe_nomessage;
import static net.sf.nightworks.Handler.affect_loc_name;
import static net.sf.nightworks.Handler.affect_strip;
import static net.sf.nightworks.Handler.affect_to_char;
import static net.sf.nightworks.Handler.affect_to_obj;
import static net.sf.nightworks.Handler.affect_to_room;
import static net.sf.nightworks.Handler.can_carry_n;
import static net.sf.nightworks.Handler.can_carry_w;
import static net.sf.nightworks.Handler.can_drop_obj;
import static net.sf.nightworks.Handler.can_see;
import static net.sf.nightworks.Handler.can_see_obj;
import static net.sf.nightworks.Handler.can_see_room;
import static net.sf.nightworks.Handler.char_to_room;
import static net.sf.nightworks.Handler.count_charmed;
import static net.sf.nightworks.Handler.get_age;
import static net.sf.nightworks.Handler.get_char_room;
import static net.sf.nightworks.Handler.get_curr_stat;
import static net.sf.nightworks.Handler.get_eq_char;
import static net.sf.nightworks.Handler.get_obj_carry;
import static net.sf.nightworks.Handler.get_obj_here;
import static net.sf.nightworks.Handler.get_obj_number;
import static net.sf.nightworks.Handler.get_obj_wear;
import static net.sf.nightworks.Handler.get_obj_weight;
import static net.sf.nightworks.Handler.get_played_day;
import static net.sf.nightworks.Handler.get_played_time;
import static net.sf.nightworks.Handler.get_skill;
import static net.sf.nightworks.Handler.get_total_played;
import static net.sf.nightworks.Handler.get_trust;
import static net.sf.nightworks.Handler.is_affected;
import static net.sf.nightworks.Handler.is_equiped_n_char;
import static net.sf.nightworks.Handler.is_name;
import static net.sf.nightworks.Handler.obj_from_char;
import static net.sf.nightworks.Handler.obj_to_char;
import static net.sf.nightworks.Handler.raffect_loc_name;
import static net.sf.nightworks.Handler.room_dark;
import static net.sf.nightworks.Handler.room_is_dark;
import static net.sf.nightworks.Handler.skill_failure_check;
import static net.sf.nightworks.Handler.skill_failure_nomessage;
import static net.sf.nightworks.Handler.unequip_char;
import static net.sf.nightworks.Interp.number_argument;
import static net.sf.nightworks.Magic.spell_identify;
import static net.sf.nightworks.Nightworks.ACT_PRACTICE;
import static net.sf.nightworks.Nightworks.ACT_UNDEAD;
import static net.sf.nightworks.Nightworks.AC_BASH;
import static net.sf.nightworks.Nightworks.AC_EXOTIC;
import static net.sf.nightworks.Nightworks.AC_PIERCE;
import static net.sf.nightworks.Nightworks.AC_SLASH;
import static net.sf.nightworks.Nightworks.AFFECT_DATA;
import static net.sf.nightworks.Nightworks.AFF_BLIND;
import static net.sf.nightworks.Nightworks.AFF_CAMOUFLAGE;
import static net.sf.nightworks.Nightworks.AFF_CHARM;
import static net.sf.nightworks.Nightworks.AFF_DETECT_EVIL;
import static net.sf.nightworks.Nightworks.AFF_DETECT_GOOD;
import static net.sf.nightworks.Nightworks.AFF_DETECT_HIDDEN;
import static net.sf.nightworks.Nightworks.AFF_DETECT_LIFE;
import static net.sf.nightworks.Nightworks.AFF_DETECT_MAGIC;
import static net.sf.nightworks.Nightworks.AFF_DETECT_UNDEAD;
import static net.sf.nightworks.Nightworks.AFF_EARTHFADE;
import static net.sf.nightworks.Nightworks.AFF_FADE;
import static net.sf.nightworks.Nightworks.AFF_FAERIE_FIRE;
import static net.sf.nightworks.Nightworks.AFF_HIDE;
import static net.sf.nightworks.Nightworks.AFF_IMP_INVIS;
import static net.sf.nightworks.Nightworks.AFF_INFRARED;
import static net.sf.nightworks.Nightworks.AFF_INVISIBLE;
import static net.sf.nightworks.Nightworks.AFF_PASS_DOOR;
import static net.sf.nightworks.Nightworks.AFF_SANCTUARY;
import static net.sf.nightworks.Nightworks.AFF_SNEAK;
import static net.sf.nightworks.Nightworks.APPLY_DAMROLL;
import static net.sf.nightworks.Nightworks.APPLY_HITROLL;
import static net.sf.nightworks.Nightworks.APPLY_NONE;
import static net.sf.nightworks.Nightworks.APPLY_ROOM_HEAL;
import static net.sf.nightworks.Nightworks.APPLY_ROOM_MANA;
import static net.sf.nightworks.Nightworks.AREA_PROTECTED;
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
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.COMM_BRIEF;
import static net.sf.nightworks.Nightworks.COMM_COMBINE;
import static net.sf.nightworks.Nightworks.COMM_COMPACT;
import static net.sf.nightworks.Nightworks.COMM_PROMPT;
import static net.sf.nightworks.Nightworks.COMM_SHOW_AFFECTS;
import static net.sf.nightworks.Nightworks.COND_BLOODLUST;
import static net.sf.nightworks.Nightworks.COND_DESIRE;
import static net.sf.nightworks.Nightworks.COND_DRUNK;
import static net.sf.nightworks.Nightworks.COND_HUNGER;
import static net.sf.nightworks.Nightworks.COND_THIRST;
import static net.sf.nightworks.Nightworks.CONT_CLOSED;
import static net.sf.nightworks.Nightworks.CON_PLAYING;
import static net.sf.nightworks.Nightworks.DEFAULT_PROMPT;
import static net.sf.nightworks.Nightworks.DESCRIPTOR_DATA;
import static net.sf.nightworks.Nightworks.EXIT_DATA;
import static net.sf.nightworks.Nightworks.EX_CLOSED;
import static net.sf.nightworks.Nightworks.EX_ISDOOR;
import static net.sf.nightworks.Nightworks.FIGHT_DELAY_TIME;
import static net.sf.nightworks.Nightworks.GET_AC;
import static net.sf.nightworks.Nightworks.GET_DAMROLL;
import static net.sf.nightworks.Nightworks.GET_HITROLL;
import static net.sf.nightworks.Nightworks.GROUP_CREATION;
import static net.sf.nightworks.Nightworks.GROUP_DETECTION;
import static net.sf.nightworks.Nightworks.GROUP_HARMFUL;
import static net.sf.nightworks.Nightworks.GROUP_NONE;
import static net.sf.nightworks.Nightworks.GROUP_PROTECTIVE;
import static net.sf.nightworks.Nightworks.GROUP_WEATHER;
import static net.sf.nightworks.Nightworks.HELP_DATA;
import static net.sf.nightworks.Nightworks.IMM_CHARM;
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
import static net.sf.nightworks.Nightworks.IS_SET;
import static net.sf.nightworks.Nightworks.IS_TRUSTED;
import static net.sf.nightworks.Nightworks.IS_VAMPIRE;
import static net.sf.nightworks.Nightworks.IS_WATER;
import static net.sf.nightworks.Nightworks.ITEM_ARMOR;
import static net.sf.nightworks.Nightworks.ITEM_BLESS;
import static net.sf.nightworks.Nightworks.ITEM_BURIED;
import static net.sf.nightworks.Nightworks.ITEM_CONTAINER;
import static net.sf.nightworks.Nightworks.ITEM_CORPSE_NPC;
import static net.sf.nightworks.Nightworks.ITEM_CORPSE_PC;
import static net.sf.nightworks.Nightworks.ITEM_DRINK_CON;
import static net.sf.nightworks.Nightworks.ITEM_EVIL;
import static net.sf.nightworks.Nightworks.ITEM_GLOW;
import static net.sf.nightworks.Nightworks.ITEM_HUM;
import static net.sf.nightworks.Nightworks.ITEM_INVENTORY;
import static net.sf.nightworks.Nightworks.ITEM_INVIS;
import static net.sf.nightworks.Nightworks.ITEM_MAGIC;
import static net.sf.nightworks.Nightworks.ITEM_MONEY;
import static net.sf.nightworks.Nightworks.ITEM_TAKE;
import static net.sf.nightworks.Nightworks.ITEM_WEAPON;
import static net.sf.nightworks.Nightworks.LEFT_HANDER;
import static net.sf.nightworks.Nightworks.LEVEL_HERO;
import static net.sf.nightworks.Nightworks.LEVEL_IMMORTAL;
import static net.sf.nightworks.Nightworks.MAX_CLASS;
import static net.sf.nightworks.Nightworks.MAX_LEVEL;
import static net.sf.nightworks.Nightworks.MAX_RELIGION;
import static net.sf.nightworks.Nightworks.MAX_STATS;
import static net.sf.nightworks.Nightworks.MAX_WEAR;
import static net.sf.nightworks.Nightworks.MOB_VNUM_BEAR;
import static net.sf.nightworks.Nightworks.MOB_VNUM_LION;
import static net.sf.nightworks.Nightworks.MOB_VNUM_SAGE;
import static net.sf.nightworks.Nightworks.MOUNTED;
import static net.sf.nightworks.Nightworks.MPROG_GIVE;
import static net.sf.nightworks.Nightworks.OBJ_DATA;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_RANGER_ARROW;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_RANGER_BOW;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_RULER_BADGE;
import static net.sf.nightworks.Nightworks.OPROG_GIVE;
import static net.sf.nightworks.Nightworks.ORG_RACE;
import static net.sf.nightworks.Nightworks.PERS;
import static net.sf.nightworks.Nightworks.PK_MIN_LEVEL;
import static net.sf.nightworks.Nightworks.PLR_AUTOASSIST;
import static net.sf.nightworks.Nightworks.PLR_AUTOEXIT;
import static net.sf.nightworks.Nightworks.PLR_AUTOGOLD;
import static net.sf.nightworks.Nightworks.PLR_AUTOLOOT;
import static net.sf.nightworks.Nightworks.PLR_AUTOSAC;
import static net.sf.nightworks.Nightworks.PLR_AUTOSPLIT;
import static net.sf.nightworks.Nightworks.PLR_CANINDUCT;
import static net.sf.nightworks.Nightworks.PLR_CANLOOT;
import static net.sf.nightworks.Nightworks.PLR_COLOR;
import static net.sf.nightworks.Nightworks.PLR_HOLYLIGHT;
import static net.sf.nightworks.Nightworks.PLR_NOCANCEL;
import static net.sf.nightworks.Nightworks.PLR_NOFOLLOW;
import static net.sf.nightworks.Nightworks.PLR_NOSUMMON;
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
import static net.sf.nightworks.Nightworks.RELIGION_NONE;
import static net.sf.nightworks.Nightworks.REMOVE_BIT;
import static net.sf.nightworks.Nightworks.REST_AT;
import static net.sf.nightworks.Nightworks.REST_ON;
import static net.sf.nightworks.Nightworks.RIDDEN;
import static net.sf.nightworks.Nightworks.RIGHT_HANDER;
import static net.sf.nightworks.Nightworks.ROOM_INDEX_DATA;
import static net.sf.nightworks.Nightworks.ROOM_INDOORS;
import static net.sf.nightworks.Nightworks.ROOM_NO_MOB;
import static net.sf.nightworks.Nightworks.ROOM_PRIVATE;
import static net.sf.nightworks.Nightworks.ROOM_REGISTRY;
import static net.sf.nightworks.Nightworks.ROOM_SAFE;
import static net.sf.nightworks.Nightworks.ROOM_SOLITARY;
import static net.sf.nightworks.Nightworks.SECT_FIELD;
import static net.sf.nightworks.Nightworks.SECT_FOREST;
import static net.sf.nightworks.Nightworks.SECT_HILLS;
import static net.sf.nightworks.Nightworks.SECT_MOUNTAIN;
import static net.sf.nightworks.Nightworks.SET_BIT;
import static net.sf.nightworks.Nightworks.SIT_AT;
import static net.sf.nightworks.Nightworks.SIT_ON;
import static net.sf.nightworks.Nightworks.SLEEP_AT;
import static net.sf.nightworks.Nightworks.SLEEP_ON;
import static net.sf.nightworks.Nightworks.STAND_AT;
import static net.sf.nightworks.Nightworks.STAND_ON;
import static net.sf.nightworks.Nightworks.STAT_CHA;
import static net.sf.nightworks.Nightworks.STAT_CON;
import static net.sf.nightworks.Nightworks.STAT_DEX;
import static net.sf.nightworks.Nightworks.STAT_INT;
import static net.sf.nightworks.Nightworks.STAT_STR;
import static net.sf.nightworks.Nightworks.STAT_WIS;
import static net.sf.nightworks.Nightworks.TO_AFFECTS;
import static net.sf.nightworks.Nightworks.TO_CHAR;
import static net.sf.nightworks.Nightworks.TO_NOTVICT;
import static net.sf.nightworks.Nightworks.TO_OBJECT;
import static net.sf.nightworks.Nightworks.TO_ROOM;
import static net.sf.nightworks.Nightworks.TO_ROOM_CONST;
import static net.sf.nightworks.Nightworks.TO_VICT;
import static net.sf.nightworks.Nightworks.TO_WEAPON;
import static net.sf.nightworks.Nightworks.UMAX;
import static net.sf.nightworks.Nightworks.UMIN;
import static net.sf.nightworks.Nightworks.WAIT_STATE;
import static net.sf.nightworks.Nightworks.WEAPON_FLAMING;
import static net.sf.nightworks.Nightworks.WEAPON_FROST;
import static net.sf.nightworks.Nightworks.WEAPON_POISON;
import static net.sf.nightworks.Nightworks.WEAPON_SHOCKING;
import static net.sf.nightworks.Nightworks.WEAR_FINGER;
import static net.sf.nightworks.Nightworks.WEAR_LEFT;
import static net.sf.nightworks.Nightworks.WEAR_NECK;
import static net.sf.nightworks.Nightworks.WEAR_NONE;
import static net.sf.nightworks.Nightworks.WEAR_RIGHT;
import static net.sf.nightworks.Nightworks.WEAR_STUCK_IN;
import static net.sf.nightworks.Nightworks.WEAR_TATTOO;
import static net.sf.nightworks.Nightworks.WEAR_WRIST;
import static net.sf.nightworks.Nightworks.atoi;
import static net.sf.nightworks.Nightworks.boot_time;
import static net.sf.nightworks.Nightworks.char_list;
import static net.sf.nightworks.Nightworks.crypt;
import static net.sf.nightworks.Nightworks.current_time;
import static net.sf.nightworks.Nightworks.descriptor_list;
import static net.sf.nightworks.Nightworks.get_carry_weight;
import static net.sf.nightworks.Nightworks.help_first;
import static net.sf.nightworks.Nightworks.nw_config;
import static net.sf.nightworks.Nightworks.social_table;
import static net.sf.nightworks.Nightworks.social_type;
import static net.sf.nightworks.Save.save_char_obj;
import static net.sf.nightworks.Skill.find_spell;
import static net.sf.nightworks.Skill.gsn_bear_call;
import static net.sf.nightworks.Skill.gsn_blue_arrow;
import static net.sf.nightworks.Skill.gsn_camp;
import static net.sf.nightworks.Skill.gsn_charm_person;
import static net.sf.nightworks.Skill.gsn_control_animal;
import static net.sf.nightworks.Skill.gsn_demand;
import static net.sf.nightworks.Skill.gsn_detect_hidden;
import static net.sf.nightworks.Skill.gsn_doppelganger;
import static net.sf.nightworks.Skill.gsn_green_arrow;
import static net.sf.nightworks.Skill.gsn_lion_call;
import static net.sf.nightworks.Skill.gsn_love_potion;
import static net.sf.nightworks.Skill.gsn_make_arrow;
import static net.sf.nightworks.Skill.gsn_make_bow;
import static net.sf.nightworks.Skill.gsn_peek;
import static net.sf.nightworks.Skill.gsn_perception;
import static net.sf.nightworks.Skill.gsn_red_arrow;
import static net.sf.nightworks.Skill.gsn_reserved;
import static net.sf.nightworks.Skill.gsn_white_arrow;
import static net.sf.nightworks.Tables.cabal_table;
import static net.sf.nightworks.Tables.ethos_table;
import static net.sf.nightworks.Tables.prac_table;
import static net.sf.nightworks.Update.gain_condition;
import static net.sf.nightworks.util.TextUtils.capitalize;
import static net.sf.nightworks.util.TextUtils.is_number;
import static net.sf.nightworks.util.TextUtils.one_argument;
import static net.sf.nightworks.util.TextUtils.smash_tilde;
import static net.sf.nightworks.util.TextUtils.str_cmp;
import static net.sf.nightworks.util.TextUtils.str_prefix;
import static net.sf.nightworks.util.TextUtils.str_suffix;
import static net.sf.nightworks.util.TextUtils.trimSpaces;

class ActInfo {
    static final String[] where_name = {
            "<worn on finger>    ",
            "<worn around neck>  ",
            "<worn on torso>     ",
            "<worn on head>      ",
            "<worn on legs>      ",
            "<worn on feet>      ",
            "<worn on hands>     ",
            "<worn on arms>      ",
            "<worn about body>   ",
            "<worn about waist>  ",
            "<worn around wrist> ",
            "<left hand holds>%c  ",
            "<right hand holds>%c ",
            "<both hands hold>   ",
            "<floating nearby>   ",
            "<scratched tattoo>  ",
            "<stuck in>          "
    };

    /* for do_count */
    static int max_on = 0;


    static boolean show_vwear_to_char(CHAR_DATA ch, OBJ_DATA obj) {

        if (can_see_obj(ch, obj)) {
            TextBuffer buf = new TextBuffer();
            buf.sprintf(where_name[obj.wear_loc], ' ');
            send_to_char(buf, ch);
            send_to_char(format_obj_to_char(obj, ch, true), ch);
            send_to_char("\n", ch);
            return true;
        }
        return false;
    }


    static boolean show_cwear_to_char(CHAR_DATA ch, OBJ_DATA obj) {
        TextBuffer buf = new TextBuffer();
        if ((obj.wear_loc == WEAR_LEFT && LEFT_HANDER(ch)) || (obj.wear_loc == WEAR_RIGHT && RIGHT_HANDER(ch))) {
            buf.sprintf(where_name[obj.wear_loc], '*');
        } else {
            buf.sprintf(where_name[obj.wear_loc], ' ');
        }
        send_to_char(buf, ch);
        if (can_see_obj(ch, obj)) {
            send_to_char(format_obj_to_char(obj, ch, true), ch);
        } else {
            send_to_char("something.\n", ch);
        }
        send_to_char("\n", ch);

        return true;
    }


    static String format_obj_to_char(OBJ_DATA obj, CHAR_DATA ch, boolean fShort) {

        if ((fShort && (obj.short_descr == null || obj.short_descr.length() == 0))
                || (obj.description == null || obj.description.length() == 0)) {
            return "";
        }

        String buf_con;

        if (obj.pIndexData.vnum > 5)  /* money, gold, etc */ {
            buf_con = "[{g"
                    + get_cond_alias(obj)
                    + "{x]";
        } else {
            buf_con = "";
        }


        TextBuffer buf = new TextBuffer();

        if (IS_OBJ_STAT(obj, ITEM_BURIED)) {
            buf.append("{W(Buried) ");
        }
        if (IS_OBJ_STAT(obj, ITEM_INVIS)) {
            buf.append("{W(Invis) ");
        }
        if (IS_AFFECTED(ch, AFF_DETECT_EVIL) && IS_OBJ_STAT(obj, ITEM_EVIL)) {
            buf.append("{r(Red Aura) ");
        }
        if (IS_AFFECTED(ch, AFF_DETECT_GOOD) && IS_OBJ_STAT(obj, ITEM_BLESS)) {
            buf.append("{b(Blue Aura) ");
        }
        if (IS_AFFECTED(ch, AFF_DETECT_MAGIC) && IS_OBJ_STAT(obj, ITEM_MAGIC)) {
            buf.append("{Y(Magical) ");
        }
        if (IS_OBJ_STAT(obj, ITEM_GLOW)) {
            buf.append("{c(Glowing) ");
        }
        if (IS_OBJ_STAT(obj, ITEM_HUM)) {
            buf.append("{y(Humming) ");
        }
        buf.append("{x");
        if (fShort) {
            if (obj.short_descr != null) {
                buf.append(obj.short_descr);
                buf.append(buf_con);
            }
        } else {
            if (obj.description != null) {
                if (obj.in_room != null) {
                    if (IS_WATER(obj.in_room)) {
                        buf.append(upfirst(obj.short_descr));
                        switch (dice(1, 3)) {
                            case 1:
                                buf.append(" is floating gently on the water.");
                                break;
                            case 2:
                                buf.append(" is making it's way on the water.");
                                break;
                            case 3:
                                buf.append(" is getting wet by the water.");
                                break;
                        }
                    } else {
                        buf.append(obj.description);
                    }
                } else {
                    buf.append(obj.description);
                }
            }
        }

        return buf.toString();
    }

/*
* Show a list to a character.
* Can coalesce duplicated items.
*/

    static void show_list_to_char(OBJ_DATA list, CHAR_DATA ch, boolean fShort, boolean fShowNothing) {

        if (ch.desc == null) {
            return;
        }

        /*
         * Alloc space for output lines.
         */
        int count = 0;
        OBJ_DATA obj;
        boolean fCombine;

        for (obj = list; obj != null; obj = obj.next_content) {
            count++;
        }
        int nShow = 0;

        /*
         * Format the list of objects.
         */
        int prgnShow[] = new int[count];
        String[] prgpstrShow = new String[count];

        for (obj = list; obj != null; obj = obj.next_content) {
            if (obj.wear_loc == WEAR_NONE && can_see_obj(ch, obj)) {
                String pstrShow = format_obj_to_char(obj, ch, fShort);

                fCombine = false;

                if (IS_NPC(ch) || IS_SET(ch.comm, COMM_COMBINE)) {
                    /*
                    * Look for duplicates, case sensitive.
                    * Matches tend to be near end so run loop backwords.
                    */
                    for (int iShow = nShow - 1; iShow >= 0; iShow--) {
                        if (prgpstrShow[iShow].equals(pstrShow)) {
                            prgnShow[iShow]++;
                            fCombine = true;
                            break;
                        }
                    }
                }

                /*
                * Couldn't combine, or didn't want to.
                */
                if (!fCombine) {
                    prgpstrShow[nShow] = pstrShow;
                    prgnShow[nShow] = 1;
                    nShow++;
                }
            }
        }

        /*
         * Output the formatted list.
         */
        TextBuffer buf = new TextBuffer();
        StringBuilder output = new StringBuilder();
        for (int iShow = 0; iShow < nShow; iShow++) {
            if (prgpstrShow[iShow].length() == 0) {
                continue;
            }

            if (IS_NPC(ch) || IS_SET(ch.comm, COMM_COMBINE)) {
                if (prgnShow[iShow] != 1) {
                    buf.sprintf("(%2d) ", prgnShow[iShow]);
                    output.append(buf.toString());
                } else {
                    output.append("     ");
                }
            }
            output.append(prgpstrShow[iShow]);
            output.append("\n");
        }

        if (fShowNothing && nShow == 0) {
            if (IS_NPC(ch) || IS_SET(ch.comm, COMM_COMBINE)) {
                send_to_char("     ", ch);
            }
            send_to_char("Nothing.\n", ch);
        }
        page_to_char(output.toString(), ch);

    }


    static void show_char_to_char_0(CHAR_DATA victim, CHAR_DATA ch) {
        StringBuilder buf = new StringBuilder();
        /*
        * Quest staff
        */
        if (!IS_NPC(ch) && IS_NPC(victim)
                && ch.pcdata.questmob > 0
                && victim.pIndexData.vnum == ch.pcdata.questmob) {
            buf.append("[TARGET] ");
        }
/*
    sprintf(message,"(%s) ",race_table[RACE(victim)].name);
    message[1] = UPPER( message[1]);
    buf.append(message);
*/
        if (RIDDEN(victim) != null) {
            buf.append("(Ridden) ");
        }
        if (IS_AFFECTED(victim, AFF_INVISIBLE)) {
            buf.append("(Invis) ");
        }
        if (IS_AFFECTED(victim, AFF_IMP_INVIS)) {
            buf.append("(Improved) ");
        }
        if (victim.invis_level >= LEVEL_HERO) {
            buf.append("(Wizi) ");
        }
        if (IS_AFFECTED(victim, AFF_HIDE)) {
            buf.append("(Hide) ");
        }
        if (IS_AFFECTED(victim, AFF_FADE)) {
            buf.append("(Fade) ");
        }
        if (IS_AFFECTED(victim, AFF_CAMOUFLAGE)) {
            buf.append("(Camf) ");
        }
        if (IS_AFFECTED(victim, AFF_EARTHFADE)) {
            buf.append("(Earth) ");
        }
        if (IS_AFFECTED(victim, AFF_CHARM)
                && victim.master == ch) {
            buf.append("(Charmed) ");
        }
        if (IS_AFFECTED(victim, AFF_PASS_DOOR)) {
            buf.append("(Translucent) ");
        }
        if (IS_AFFECTED(victim, AFF_FAERIE_FIRE)) {
            buf.append("(Pink Aura) ");
        }
        if (IS_NPC(victim) && IS_SET(victim.act, ACT_UNDEAD) && IS_AFFECTED(ch, AFF_DETECT_UNDEAD)) {
            buf.append("(Undead) ");
        }
        if (IS_EVIL(victim) && IS_AFFECTED(ch, AFF_DETECT_EVIL)) {
            buf.append("(Red Aura) ");
        }
        if (IS_GOOD(victim) && IS_AFFECTED(ch, AFF_DETECT_GOOD)) {
            buf.append("(Golden Aura) ");
        }
        if (IS_AFFECTED(victim, AFF_SANCTUARY)) {
            buf.append("(White Aura) ");
        }
        if (!IS_NPC(victim) && IS_SET(victim.act, PLR_WANTED)) {
            buf.append("(CRIMINAL) ");
        }

        if (victim.position == victim.start_pos && victim.long_descr.length() != 0) {
            buf.append(victim.long_descr);
            send_to_char(buf, ch);
            return;
        }

        if (IS_SET(ch.act, PLR_HOLYLIGHT) && is_affected(victim, gsn_doppelganger)) {
            buf.append("{");
            buf.append(PERS(victim, ch));
            buf.append("} ");
        }

        if (is_affected(victim, gsn_doppelganger) &&
                victim.doppel.long_descr.length() != 0) {
            buf.append(victim.doppel.long_descr);
            send_to_char(buf, ch);
            return;
        }

        if (victim.long_descr.length() != 0 && !is_affected(victim, gsn_doppelganger)) {
            buf.append(victim.long_descr);
            send_to_char(buf, ch);
            return;
        }

        if (is_affected(victim, gsn_doppelganger)) {
            buf.append(PERS(victim.doppel, ch));
            if (!IS_NPC(victim.doppel) && !IS_SET(ch.comm, COMM_BRIEF)) {
                buf.append(victim.doppel.pcdata.title);
            }
        } else {
            buf.append(PERS(victim, ch));
            if (!IS_NPC(victim) && !IS_SET(ch.comm, COMM_BRIEF)
                    && victim.position == POS_STANDING && ch.on == null) {
                buf.append(victim.pcdata.title);
            }
        }

        switch (victim.position) {
            case POS_DEAD:
                buf.append(" is DEAD!!");
                break;
            case POS_MORTAL:
                buf.append(" is mortally wounded.");
                break;
            case POS_INCAP:
                buf.append(" is incapacitated.");
                break;
            case POS_STUNNED:
                buf.append(" is lying here stunned.");
                break;
            case POS_SLEEPING:
                if (victim.on != null) {
                    TextBuffer message = new TextBuffer();
                    if (IS_SET(victim.on.value[2], SLEEP_AT)) {
                        message.sprintf(" is sleeping at %s.", victim.on.short_descr);
                        buf.append(message);
                    } else if (IS_SET(victim.on.value[2], SLEEP_ON)) {
                        message.sprintf(" is sleeping on %s.", victim.on.short_descr);
                        buf.append(message);
                    } else {
                        message.sprintf(" is sleeping in %s.", victim.on.short_descr);
                        buf.append(message);
                    }
                } else {
                    buf.append(" is sleeping here.");
                }
                break;
            case POS_RESTING:
                if (victim.on != null) {
                    TextBuffer message = new TextBuffer();
                    if (IS_SET(victim.on.value[2], REST_AT)) {
                        message.sprintf(" is resting at %s.", victim.on.short_descr);
                        buf.append(message);
                    } else if (IS_SET(victim.on.value[2], REST_ON)) {
                        message.sprintf(" is resting on %s.", victim.on.short_descr);
                        buf.append(message);
                    } else {
                        message.sprintf(" is resting in %s.", victim.on.short_descr);
                        buf.append(message);
                    }
                } else {
                    buf.append(" is resting here.");
                }
                break;
            case POS_SITTING:
                if (victim.on != null) {
                    TextBuffer message = new TextBuffer();
                    if (IS_SET(victim.on.value[2], SIT_AT)) {
                        message.sprintf(" is sitting at %s.", victim.on.short_descr);
                        buf.append(message);
                    } else if (IS_SET(victim.on.value[2], SIT_ON)) {
                        message.sprintf(" is sitting on %s.", victim.on.short_descr);
                        buf.append(message);
                    } else {
                        message.sprintf(" is sitting in %s.", victim.on.short_descr);
                        buf.append(message);
                    }
                } else {
                    buf.append(" is sitting here.");
                }
                break;
            case POS_STANDING:
                if (victim.on != null) {
                    TextBuffer message = new TextBuffer();
                    if (IS_SET(victim.on.value[2], STAND_AT)) {
                        message.sprintf(" is standing at %s.", victim.on.short_descr);
                        buf.append(message);
                    } else if (IS_SET(victim.on.value[2], STAND_ON)) {
                        message.sprintf(" is standing on %s.", victim.on.short_descr);
                        buf.append(message);
                    } else {
                        message.sprintf(" is standing in %s.", victim.on.short_descr);
                        buf.append(message);
                    }
                } else if (MOUNTED(victim) != null) {
                    TextBuffer message = new TextBuffer();
                    message.sprintf(" is here, riding %s.", PERS(MOUNTED(victim), ch));
                    buf.append(message);
                } else {
                    buf.append(" is here.");
                }
                break;
            case POS_FIGHTING:
                buf.append(" is here, fighting ");
                if (victim.fighting == null) {
                    buf.append("thin air??");
                } else if (victim.fighting == ch) {
                    buf.append("YOU!");
                } else if (victim.in_room == victim.fighting.in_room) {
                    buf.append(PERS(victim.fighting, ch));
                    buf.append(".");
                } else {
                    buf.append("somone who left??");
                }
                break;
        }

        buf.append("\n");

        send_to_char(upfirst(buf.toString()), ch);
    }


    static void show_char_to_char_1(CHAR_DATA victim, CHAR_DATA ch) {
        OBJ_DATA obj;
        int iWear;
        int percent;
        boolean found;
        CHAR_DATA vict;

        vict = is_affected(victim, gsn_doppelganger) ? victim.doppel : victim;

        if (can_see(victim, ch)) {
            if (ch == victim) {
                act("$n looks at $mself.", ch, null, null, TO_ROOM);
            } else {
                act("$n looks at you.", ch, null, victim, TO_VICT);
                act("$n looks at $N.", ch, null, victim, TO_NOTVICT);
            }
        }

        if (vict.description.length() != 0) {
            send_to_char(vict.description, ch);
        } else {
            act("You see nothing special about $M.", ch, null, victim, TO_CHAR);
        }

        TextBuffer buf = new TextBuffer();
        if (MOUNTED(victim) != null) {
            buf.sprintf("%s is riding %s.\n", PERS(victim, ch), PERS(MOUNTED(victim), ch));
            send_to_char(buf, ch);
        }
        if (RIDDEN(victim) != null) {
            buf.sprintf("%s is being ridden by %s.\n", PERS(victim, ch), PERS(RIDDEN(victim), ch));
            send_to_char(buf, ch);
        }

        if (victim.max_hit > 0) {
            percent = (100 * victim.hit) / victim.max_hit;
        } else {
            percent = -1;
        }

        buf.sprintf("(%s) %s", vict.race.name, PERS(vict, ch));

        if (percent >= 100) {
            buf.append(" is in perfect health.\n");
        } else if (percent >= 90) {
            buf.append(" has a few scratches.\n");
        } else if (percent >= 75) {
            buf.append(" has some small but disgusting cuts.\n");
        } else if (percent >= 50) {
            buf.append(" is covered with bleeding wounds.\n");
        } else if (percent >= 30) {
            buf.append(" is gushing blood.\n");
        } else if (percent >= 15) {
            buf.append(" is writhing in agony.\n");
        } else if (percent >= 0) {
            buf.append(" is convulsing on the ground.\n");
        } else {
            buf.append(" is nearly dead.\n");
        }

        /* vampire ... */
        if (percent < 90 && ch.clazz == Clazz.VAMPIRE && ch.level > 10) {
            gain_condition(ch, COND_BLOODLUST, -1);
        }

        send_to_char(upfirst(buf.toString()), ch);

        found = false;
        for (iWear = 0; iWear < MAX_WEAR; iWear++) {
            if (iWear == WEAR_FINGER || iWear == WEAR_NECK || iWear == WEAR_WRIST || iWear == WEAR_TATTOO || iWear == WEAR_STUCK_IN) {
                for (obj = vict.carrying; obj != null; obj = obj.next_content) {
                    if (obj.wear_loc == iWear) {
                        if (!found) {
                            act("$N is using:", ch, null, victim, TO_CHAR);
                            send_to_char("\n", ch);
                            found = true;
                        }
                        show_vwear_to_char(ch, obj);
                    }
                }
            } else {
                if ((obj = get_eq_char(vict, iWear)) != null) {
                    if (!found) {
                        act("$N is using:", ch, null, victim, TO_CHAR);
                        send_to_char("\n", ch);
                        found = true;
                    }
                    show_vwear_to_char(ch, obj);
                }
            }
        }

        if (victim != ch
                && !IS_NPC(ch)
                && number_percent() < get_skill(ch, gsn_peek)) {
            send_to_char("\nYou peek at the inventory:\n", ch);
            check_improve(ch, gsn_peek, true, 4);
            show_list_to_char(vict.carrying, ch, true, true);
        }

    }


    static void show_char_to_char(CHAR_DATA list, CHAR_DATA ch) {
        CHAR_DATA rch;
        int life_count = 0;

        for (rch = list; rch != null; rch = rch.next_in_room) {
            if (rch == ch) {
                continue;
            }

            if (get_trust(ch) < rch.invis_level) {
                continue;
            }

            if (can_see(ch, rch)) {
                show_char_to_char_0(rch, ch);
            } else if (room_is_dark(ch) && IS_AFFECTED(rch, AFF_INFRARED)) {
                send_to_char("You see glowing red eyes watching YOU!\n", ch);
                if (!IS_IMMORTAL(rch)) {
                    life_count++;
                }
            } else if (!IS_IMMORTAL(rch)) {
                life_count++;
            }
        }

        if (life_count != 0 && IS_AFFECTED(ch, AFF_DETECT_LIFE)) {
            TextBuffer buf = new TextBuffer();
            buf.sprintf("You feel %d more life %s in the room.\n", life_count, (life_count == 1) ? "form" : "forms");
            send_to_char(buf, ch);
        }
    }


    static boolean check_blind(CHAR_DATA ch) {

        if (!IS_NPC(ch) && IS_SET(ch.act, PLR_HOLYLIGHT)) {
            return true;
        }

        if (IS_AFFECTED(ch, AFF_BLIND)) {
            send_to_char("You can't see a thing!\n", ch);
            return false;
        }

        return true;
    }

    static void do_clear(CHAR_DATA ch) {
        if (!IS_NPC(ch)) {
            send_to_char("\033[0;0H\033[2J", ch);
        }
    }

/* changes your scroll */

    static void do_scroll(CHAR_DATA ch, String argument) {
        int lines;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0) {
            if (ch.lines == 0) {
                send_to_char("You do not page long messages.\n", ch);
            } else {
                TextBuffer buf = new TextBuffer();
                buf.sprintf("You currently display %d lines per page.\n", ch.lines + 2);
                send_to_char(buf, ch);
            }
            return;
        }

        if (!is_number(arg)) {
            send_to_char("You must provide a number.\n", ch);
            return;
        }

        lines = atoi(arg.toString());

        if (lines == 0) {
            send_to_char("Paging disabled.\n", ch);
            ch.lines = 0;
            return;
        }

        if (lines < 10 || lines > 100) {
            send_to_char("You must provide a reasonable number.\n", ch);
            return;
        }
        TextBuffer buf = new TextBuffer();
        buf.sprintf("Scroll set to %d lines.\n", lines);
        send_to_char(buf, ch);
        ch.lines = lines - 2;
    }

/* RT does socials */

    static void do_socials(CHAR_DATA ch) {
        int col = 0;
        TextBuffer buf = new TextBuffer();
        for (social_type soc : social_table) {
            buf.sprintf("%-12s", soc.name);
            send_to_char(buf, ch);
            if (++col % 6 == 0) {
                send_to_char("\n", ch);
            }
        }

        if (col % 6 != 0) {
            send_to_char("\n", ch);
        }
    }

/* RT Commands to replace news, motd, imotd, etc from ROM */

    static void do_motd(CHAR_DATA ch) {
        do_help(ch, "motd");
    }

    static void do_imotd(CHAR_DATA ch) {
        do_help(ch, "imotd");
    }

    static void do_rules(CHAR_DATA ch) {
        do_help(ch, "rules");
    }

    static void do_story(CHAR_DATA ch) {
        do_help(ch, "story");
    }

    static void do_wizlist(CHAR_DATA ch) {
        do_help(ch, "wizlist");
    }

/* RT this following section holds all the auto commands from ROM, as well as
   replacements for config */

    static void do_autolist(CHAR_DATA ch) {
        /* lists most player flags */
        if (IS_NPC(ch)) {
            return;
        }

        if (IS_SET(ch.act, PLR_COLOR)) {
            do_autolist_col(ch);
            return;
        }

        send_to_char("   action     status\n", ch);
        send_to_char("---------------------\n", ch);

        send_to_char("color          ", ch);
        if (IS_SET(ch.act, PLR_COLOR)) {
            send_to_char("ON\n", ch);
        } else {
            send_to_char("OFF\n", ch);
        }

        send_to_char("autoassist     ", ch);
        if (IS_SET(ch.act, PLR_AUTOASSIST)) {
            send_to_char("ON\n", ch);
        } else {
            send_to_char("OFF\n", ch);
        }

        send_to_char("autoexit       ", ch);
        if (IS_SET(ch.act, PLR_AUTOEXIT)) {
            send_to_char("ON\n", ch);
        } else {
            send_to_char("OFF\n", ch);
        }

        send_to_char("autogold       ", ch);
        if (IS_SET(ch.act, PLR_AUTOGOLD)) {
            send_to_char("ON\n", ch);
        } else {
            send_to_char("OFF\n", ch);
        }

        send_to_char("autoloot       ", ch);
        if (IS_SET(ch.act, PLR_AUTOLOOT)) {
            send_to_char("ON\n", ch);
        } else {
            send_to_char("OFF\n", ch);
        }

        send_to_char("autosac        ", ch);
        if (IS_SET(ch.act, PLR_AUTOSAC)) {
            send_to_char("ON\n", ch);
        } else {
            send_to_char("OFF\n", ch);
        }

        send_to_char("autosplit      ", ch);
        if (IS_SET(ch.act, PLR_AUTOSPLIT)) {
            send_to_char("ON\n", ch);
        } else {
            send_to_char("OFF\n", ch);
        }

        send_to_char("compact mode   ", ch);
        if (IS_SET(ch.comm, COMM_COMPACT)) {
            send_to_char("ON\n", ch);
        } else {
            send_to_char("OFF\n", ch);
        }

        send_to_char("prompt         ", ch);
        if (IS_SET(ch.comm, COMM_PROMPT)) {
            send_to_char("ON\n", ch);
        } else {
            send_to_char("OFF\n", ch);
        }

        send_to_char("combine items  ", ch);
        if (IS_SET(ch.comm, COMM_COMBINE)) {
            send_to_char("ON\n", ch);
        } else {
            send_to_char("OFF\n", ch);
        }


        if (IS_SET(ch.act, PLR_NOSUMMON)) {
            send_to_char("You can only be summoned players within your PK range.\n", ch);
        } else {
            send_to_char("You can be summoned by anyone.\n", ch);
        }

        if (IS_SET(ch.act, PLR_NOFOLLOW)) {
            send_to_char("You do not welcome followers.\n", ch);
        } else {
            send_to_char("You accept followers.\n", ch);
        }

        if (IS_SET(ch.act, PLR_NOCANCEL)) {
            send_to_char("You do not welcome others' cancellation spells.\n", ch);
        } else {
            send_to_char("You accept others' cancellation spells.\n", ch);
        }
    }

    static void do_autolist_col(CHAR_DATA ch) {
        /* lists most player flags */
        if (IS_NPC(ch)) {
            return;
        }

        send_to_char("  [1;33maction           status\n[0;37m", ch);
        send_to_char("[1;36m-------------------------\n[0;37m", ch);

        send_to_char("[1;34m|[0;37m [0;36mcolor          [0;37m", ch);
        if (IS_SET(ch.act, PLR_COLOR)) {
            send_to_char("[1;34m|  [1;32mON  [1;34m|\n[0;37m", ch);
        } else {
            send_to_char("[1;34m|  [1;31mOFF [1;34m|\n[0;37m", ch);
        }

        send_to_char("[1;34m|[0;37m [0;36mautoassist     ", ch);
        if (IS_SET(ch.act, PLR_AUTOASSIST)) {
            send_to_char("[1;34m|  [1;32mON  [1;34m|\n[0;37m", ch);
        } else {
            send_to_char("[1;34m|  [1;31mOFF [1;34m|\n[0;37m", ch);
        }

        send_to_char("[1;34m|[0;37m [0;36mautoexit       ", ch);
        if (IS_SET(ch.act, PLR_AUTOEXIT)) {
            send_to_char("[1;34m|  [1;32mON  [1;34m|\n[0;37m", ch);
        } else {
            send_to_char("[1;34m|  [1;31mOFF [1;34m|\n[0;37m", ch);
        }

        send_to_char("[1;34m|[0;37m [0;36mautogold       ", ch);
        if (IS_SET(ch.act, PLR_AUTOGOLD)) {
            send_to_char("[1;34m|  [1;32mON  [1;34m|\n[0;37m", ch);
        } else {
            send_to_char("[1;34m|  [1;31mOFF [1;34m|\n[0;37m", ch);
        }

        send_to_char("[1;34m|[0;37m [0;36mautoloot       ", ch);
        if (IS_SET(ch.act, PLR_AUTOLOOT)) {
            send_to_char("[1;34m|  [1;32mON  [1;34m|\n[0;37m", ch);
        } else {
            send_to_char("[1;34m|  [1;31mOFF [1;34m|\n[0;37m", ch);
        }

        send_to_char("[1;34m|[0;37m [0;36mautosac        ", ch);
        if (IS_SET(ch.act, PLR_AUTOSAC)) {
            send_to_char("[1;34m|  [1;32mON  [1;34m|\n[0;37m", ch);
        } else {
            send_to_char("[1;34m|  [1;31mOFF [1;34m|\n[0;37m", ch);
        }

        send_to_char("[1;34m|[0;37m [0;36mautosplit      ", ch);
        if (IS_SET(ch.act, PLR_AUTOSPLIT)) {
            send_to_char("[1;34m|  [1;32mON  [1;34m|\n[0;37m", ch);
        } else {
            send_to_char("[1;34m|  [1;31mOFF [1;34m|\n[0;37m", ch);
        }

        send_to_char("[1;34m|[0;37m [0;36mcompact mode   ", ch);
        if (IS_SET(ch.comm, COMM_COMPACT)) {
            send_to_char("[1;34m|  [1;32mON  [1;34m|\n[0;37m", ch);
        } else {
            send_to_char("[1;34m|  [1;31mOFF [1;34m|\n[0;37m", ch);
        }

        send_to_char("[1;34m|[0;37m [0;36mprompt         ", ch);
        if (IS_SET(ch.comm, COMM_PROMPT)) {
            send_to_char("[1;34m|  [1;32mON  [1;34m|\n[0;37m", ch);
        } else {
            send_to_char("[1;34m|  [1;31mOFF [1;34m|\n[0;37m", ch);
        }

        send_to_char("[1;34m|[0;37m [0;36mcombine items  ", ch);
        if (IS_SET(ch.comm, COMM_COMBINE)) {
            send_to_char("[1;34m|  [1;32mON  [1;34m|\n[0;37m", ch);
        } else {
            send_to_char("[1;34m|  [1;31mOFF [1;34m|\n[0;37m", ch);
        }
        send_to_char("[1;36m-------------------------\n[0;37m", ch);


        if (IS_SET(ch.act, PLR_NOSUMMON)) {
            send_to_char("You can only be summoned players within your PK range.\n", ch);
        } else {
            send_to_char("You can be summoned by anyone.\n", ch);
        }

        if (IS_SET(ch.act, PLR_NOFOLLOW)) {
            send_to_char("You do not welcome followers.\n", ch);
        } else {
            send_to_char("You accept followers.\n", ch);
        }

        if (IS_SET(ch.act, PLR_NOCANCEL)) {
            send_to_char("You do not welcome others' cancellation spells.\n", ch);
        } else {
            send_to_char("You accept others' cancellation spells.\n", ch);
        }
    }

    static void do_autoassist(CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }

        if (IS_SET(ch.act, PLR_AUTOASSIST)) {
            send_to_char("Autoassist removed.\n", ch);
            ch.act = REMOVE_BIT(ch.act, PLR_AUTOASSIST);
        } else {
            send_to_char("You will now assist when needed.\n", ch);
            ch.act = SET_BIT(ch.act, PLR_AUTOASSIST);
        }
    }

    static void do_autoexit(CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }

        if (IS_SET(ch.act, PLR_AUTOEXIT)) {
            send_to_char("Exits will no longer be displayed.\n", ch);
            ch.act = REMOVE_BIT(ch.act, PLR_AUTOEXIT);
        } else {
            send_to_char("Exits will now be displayed.\n", ch);
            ch.act = SET_BIT(ch.act, PLR_AUTOEXIT);
        }
    }

    static void do_autogold(CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }

        if (IS_SET(ch.act, PLR_AUTOGOLD)) {
            send_to_char("Autogold removed.\n", ch);
            ch.act = REMOVE_BIT(ch.act, PLR_AUTOGOLD);
        } else {
            send_to_char("Automatic gold looting set.\n", ch);
            ch.act = SET_BIT(ch.act, PLR_AUTOGOLD);
        }
    }

    static void do_autoloot(CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }

        if (IS_SET(ch.act, PLR_AUTOLOOT)) {
            send_to_char("Autolooting removed.\n", ch);
            ch.act = REMOVE_BIT(ch.act, PLR_AUTOLOOT);
        } else {
            send_to_char("Automatic corpse looting set.\n", ch);
            ch.act = SET_BIT(ch.act, PLR_AUTOLOOT);
        }
    }

    static void do_autosac(CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }

        if (IS_SET(ch.act, PLR_AUTOSAC)) {
            send_to_char("Autosacrificing removed.\n", ch);
            ch.act = REMOVE_BIT(ch.act, PLR_AUTOSAC);
        } else {
            send_to_char("Automatic corpse sacrificing set.\n", ch);
            ch.act = SET_BIT(ch.act, PLR_AUTOSAC);
        }
    }

    static void do_autosplit(CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }

        if (IS_SET(ch.act, PLR_AUTOSPLIT)) {
            send_to_char("Autosplitting removed.\n", ch);
            ch.act = REMOVE_BIT(ch.act, PLR_AUTOSPLIT);
        } else {
            send_to_char("Automatic gold splitting set.\n", ch);
            ch.act = SET_BIT(ch.act, PLR_AUTOSPLIT);
        }
    }

    static void do_color(CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }

        if (IS_SET(ch.act, PLR_COLOR)) {
            send_to_char("Your color is now OFF.\n", ch);
            ch.act = REMOVE_BIT(ch.act, PLR_COLOR);
        } else {
            send_to_char("Your color is now ON.\n", ch);
            ch.act = SET_BIT(ch.act, PLR_COLOR);
        }
    }

    static void do_brief(CHAR_DATA ch) {
        if (IS_SET(ch.comm, COMM_BRIEF)) {
            send_to_char("Full descriptions activated.\n", ch);
            ch.comm = REMOVE_BIT(ch.comm, COMM_BRIEF);
        } else {
            send_to_char("Short descriptions activated.\n", ch);
            ch.comm = SET_BIT(ch.comm, COMM_BRIEF);
        }
    }

    static void do_compact(CHAR_DATA ch) {
        if (IS_SET(ch.comm, COMM_COMPACT)) {
            send_to_char("Compact mode removed.\n", ch);
            ch.comm = REMOVE_BIT(ch.comm, COMM_COMPACT);
        } else {
            send_to_char("Compact mode set.\n", ch);
            ch.comm = SET_BIT(ch.comm, COMM_COMPACT);
        }
    }

    static void do_show(CHAR_DATA ch) {
        if (IS_SET(ch.comm, COMM_SHOW_AFFECTS)) {
            send_to_char("Affects will no longer be shown in score.\n", ch);
            ch.comm = REMOVE_BIT(ch.comm, COMM_SHOW_AFFECTS);
        } else {
            send_to_char("Affects will now be shown in score.\n", ch);
            ch.comm = SET_BIT(ch.comm, COMM_SHOW_AFFECTS);
        }
    }

    static void do_prompt(CHAR_DATA ch, String argument) {
        if (argument.length() == 0) {
            if (IS_SET(ch.comm, COMM_PROMPT)) {
                send_to_char("You will no longer see prompts.\n", ch);
                ch.comm = REMOVE_BIT(ch.comm, COMM_PROMPT);
            } else {
                send_to_char("You will now see prompts.\n", ch);
                ch.comm = SET_BIT(ch.comm, COMM_PROMPT);
            }
            return;
        }
        String buf;
        if (argument.equals("all")) {
            buf = DEFAULT_PROMPT;
        } else {
            if (argument.length() > 50) {
                argument = argument.substring(0, 50);
            }
            buf = smash_tilde(argument);
            if (str_suffix("%c", buf)) {
                buf += " ";
            }

        }

        ch.prompt = buf;
        TextBuffer buf2 = new TextBuffer();
        buf2.sprintf("Prompt set to %s\n", ch.prompt);
        send_to_char(buf2, ch);
    }

    static void do_combine(CHAR_DATA ch) {
        if (IS_SET(ch.comm, COMM_COMBINE)) {
            send_to_char("Long inventory selected.\n", ch);
            ch.comm = REMOVE_BIT(ch.comm, COMM_COMBINE);
        } else {
            send_to_char("Combined inventory selected.\n", ch);
            ch.comm = SET_BIT(ch.comm, COMM_COMBINE);
        }
    }

    static void do_noloot(CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }

        if (IS_SET(ch.act, PLR_CANLOOT)) {
            send_to_char("Your corpse is now safe from thieves.\n", ch);
            ch.act = REMOVE_BIT(ch.act, PLR_CANLOOT);
        } else {
            send_to_char("Your corpse may now be looted.\n", ch);
            ch.act = SET_BIT(ch.act, PLR_CANLOOT);
        }
    }

    static void do_nofollow(CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }
        if (IS_AFFECTED(ch, AFF_CHARM)) {
            send_to_char("You don't want to leave your beloved master.\n", ch);
            return;
        }

        if (IS_SET(ch.act, PLR_NOFOLLOW)) {
            send_to_char("You now accept followers.\n", ch);
            ch.act = REMOVE_BIT(ch.act, PLR_NOFOLLOW);
        } else {
            send_to_char("You no longer accept followers.\n", ch);
            ch.act = SET_BIT(ch.act, PLR_NOFOLLOW);
            die_follower(ch);
        }
    }

    static void do_nosummon(CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            if (IS_SET(ch.imm_flags, IMM_SUMMON)) {
                send_to_char("You are no longer immune to summoning.\n", ch);
                ch.imm_flags = REMOVE_BIT(ch.imm_flags, IMM_SUMMON);
            } else {
                send_to_char("You are now immune to summoning.\n", ch);
                ch.imm_flags = SET_BIT(ch.imm_flags, IMM_SUMMON);
            }
        } else {
            if (IS_SET(ch.act, PLR_NOSUMMON)) {
                send_to_char("You may now be summoned by anyone.\n", ch);
                ch.act = REMOVE_BIT(ch.act, PLR_NOSUMMON);
            } else {
                send_to_char("You may only be summoned by players within your PK range.\n", ch);
                ch.act = SET_BIT(ch.act, PLR_NOSUMMON);
            }
        }
    }

    static void do_look(CHAR_DATA ch, String argument) {
        EXIT_DATA pexit;
        CHAR_DATA victim;
        OBJ_DATA obj;
        String pdesc;
        int door;
        int number, count;

        if (ch.desc == null) {
            return;
        }

        if (ch.position < POS_SLEEPING) {
            send_to_char("You can't see anything but stars!\n", ch);
            return;
        }

        if (ch.position == POS_SLEEPING) {
            send_to_char("You can't see anything, you're sleeping!\n", ch);
            return;
        }

        if (!check_blind(ch)) {
            return;
        }

        if (!IS_NPC(ch) && !IS_SET(ch.act, PLR_HOLYLIGHT) && room_is_dark(ch)) {
            send_to_char("It is pitch black ... \n", ch);
            show_char_to_char(ch.in_room.people, ch);
            return;
        }

        StringBuilder arg1b = new StringBuilder();
        StringBuilder arg2b = new StringBuilder();
        StringBuilder arg3b = new StringBuilder();
        argument = one_argument(argument, arg1b);
        one_argument(argument, arg2b);
        number = number_argument(arg1b.toString(), arg3b);
        count = 0;

        String arg1 = arg1b.toString();
        if (arg1.length() == 0 || !str_cmp(arg1, "auto")) {
            /* 'look' or 'look auto' */
            send_to_char("{W" + ch.in_room.name + "{x", ch);

            if (IS_IMMORTAL(ch) && (IS_NPC(ch) || IS_SET(ch.act, PLR_HOLYLIGHT))) {
                TextBuffer buf = new TextBuffer();
                buf.sprintf(" {x[Room %d]{x", ch.in_room.vnum);
                send_to_char(buf, ch);
            }

            send_to_char("\n", ch);

            if (arg1.length() == 0 || (!IS_NPC(ch) && !IS_SET(ch.comm, COMM_BRIEF))) {
                send_to_char("  ", ch);
                send_to_char(ch.in_room.description, ch);
            }

            if (!IS_NPC(ch) && IS_SET(ch.act, PLR_AUTOEXIT)) {
                send_to_char("\n", ch);
                do_exits(ch, "auto");
            }

            show_list_to_char(ch.in_room.contents, ch, false, false);
            show_char_to_char(ch.in_room.people, ch);
            return;
        }

        String arg2 = arg2b.toString();
        if (!str_cmp(arg1, "i") || !str_cmp(arg1, "in") || !str_cmp(arg1, "on")) {
            /* 'look in' */
            if (arg2.length() == 0) {
                send_to_char("Look in what?\n", ch);
                return;
            }

            if ((obj = get_obj_here(ch, arg2)) == null) {
                send_to_char("You do not see that here.\n", ch);
                return;
            }

            switch (obj.item_type) {
                default:
                    send_to_char("That is not a container.\n", ch);
                    break;

                case ITEM_DRINK_CON:
                    if (obj.value[1] <= 0) {
                        send_to_char("It is empty.\n", ch);
                        break;
                    }
                {
                    TextBuffer buf = new TextBuffer();
                    buf.sprintf("It's %sfilled with  a %s liquid.\n",
                            obj.value[1] < obj.value[0] / 4
                                    ? "less than half-" :
                                    obj.value[1] < 3 * obj.value[0] / 4
                                            ? "about half-" : "more than half-",
                            liq_table[obj.value[2]].liq_color
                    );

                    send_to_char(buf, ch);
                }
                break;

                case ITEM_CONTAINER:
                case ITEM_CORPSE_NPC:
                case ITEM_CORPSE_PC:
                    if (IS_SET(obj.value[1], CONT_CLOSED)) {
                        send_to_char("It is closed.\n", ch);
                        break;
                    }

                    act("$p holds:", ch, obj, null, TO_CHAR);
                    show_list_to_char(obj.contains, ch, true, true);
                    break;
            }
            return;
        }

        if ((victim = get_char_room(ch, arg1)) != null) {
            show_char_to_char_1(victim, ch);

            /* Love potion */

            if (is_affected(ch, gsn_love_potion) && (victim != ch)) {

                affect_strip(ch, gsn_love_potion);

                if (ch.master != null) {
                    stop_follower(ch);
                }
                add_follower(ch, victim);
                ch.leader = victim;

                AFFECT_DATA af = new AFFECT_DATA();
                af.where = TO_AFFECTS;
                af.type = gsn_charm_person;
                af.level = ch.level;
                af.duration = number_fuzzy(victim.level / 4);
                af.bitvector = AFF_CHARM;
                af.modifier = 0;
                af.location = 0;
                affect_to_char(ch, af);

                act("Isn't $n just so nice?", victim, null, ch, TO_VICT);
                act("$N looks at you with adoring eyes.", victim, null, ch, TO_CHAR);
                act("$N looks at $n with adoring eyes.", victim, null, ch, TO_NOTVICT);
            }

            return;
        }

        String arg3 = arg3b.toString();
        for (obj = ch.carrying; obj != null; obj = obj.next_content) {
            if (can_see_obj(ch, obj)) {  /* player can see object */
                pdesc = get_extra_descr(arg3, obj.extra_descr);
                if (pdesc != null) {
                    if (++count == number) {
                        send_to_char(pdesc, ch);
                        return;
                    } else {
                        continue;
                    }
                }

                pdesc = get_extra_descr(arg3, obj.pIndexData.extra_descr);
                if (pdesc != null) {
                    if (++count == number) {
                        send_to_char(pdesc, ch);
                        return;
                    } else {
                        continue;
                    }
                }

                if (is_name(arg3, obj.name)) {
                    if (++count == number) {
                        send_to_char("You see nothing special about it.\n", ch);
                        return;
                    }
                }

            }
        }

        for (obj = ch.in_room.contents; obj != null; obj = obj.next_content) {
            if (can_see_obj(ch, obj)) {
                pdesc = get_extra_descr(arg3, obj.extra_descr);
                if (pdesc != null) {
                    if (++count == number) {
                        send_to_char(pdesc, ch);
                        return;
                    }
                }

                pdesc = get_extra_descr(arg3, obj.pIndexData.extra_descr);
                if (pdesc != null) {
                    if (++count == number) {
                        send_to_char(pdesc, ch);
                        return;
                    }
                }
            }

            if (is_name(arg3, obj.name)) {
                if (++count == number) {
                    send_to_char(obj.description, ch);
                    send_to_char("\n", ch);
                    return;
                }
            }
        }

        pdesc = get_extra_descr(arg3, ch.in_room.extra_descr);
        if (pdesc != null) {
            if (++count == number) {
                send_to_char(pdesc, ch);
                return;
            }
        }

        if (count > 0 && count != number) {
            TextBuffer buf = new TextBuffer();
            if (count == 1) {
                buf.sprintf("You only see one %s here.\n", arg3);
            } else {
                buf.sprintf("You only see %d of those here.\n", count);
            }

            send_to_char(buf, ch);
            return;
        }

        if (!str_cmp(arg1, "n") || !str_cmp(arg1, "north")) {
            door = 0;
        } else if (!str_cmp(arg1, "e") || !str_cmp(arg1, "east")) {
            door = 1;
        } else if (!str_cmp(arg1, "s") || !str_cmp(arg1, "south")) {
            door = 2;
        } else if (!str_cmp(arg1, "w") || !str_cmp(arg1, "west")) {
            door = 3;
        } else if (!str_cmp(arg1, "u") || !str_cmp(arg1, "up")) {
            door = 4;
        } else if (!str_cmp(arg1, "d") || !str_cmp(arg1, "down")) {
            door = 5;
        } else {
            send_to_char("You do not see that here.\n", ch);
            return;
        }

        /* 'look direction' */
        if ((pexit = ch.in_room.exit[door]) == null) {
            send_to_char("Nothing special there.\n", ch);
            return;
        }

        if (pexit.description != null && pexit.description.length() != 0) {
            send_to_char(pexit.description, ch);
        } else {
            send_to_char("Nothing special there.\n", ch);
        }

        if (pexit.keyword != null && pexit.keyword.length() != 0 && pexit.keyword.charAt(0) != ' ') {
            if (IS_SET(pexit.exit_info, EX_CLOSED)) {
                act("The $d is closed.", ch, null, pexit.keyword, TO_CHAR);
            } else if (IS_SET(pexit.exit_info, EX_ISDOOR)) {
                act("The $d is open.", ch, null, pexit.keyword, TO_CHAR);
            }
        }

    }

/* RT added back for the hell of it */

    static void do_read(CHAR_DATA ch, String argument) {
        do_look(ch, argument);
    }

    static void do_examine(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;

        StringBuilder argb = new StringBuilder();

        one_argument(argument, argb);

        if (argb.length() == 0) {
            send_to_char("Examine what?\n", ch);
            return;
        }
        String arg = argb.toString();
        do_look(ch, arg);

        if ((obj = get_obj_here(ch, arg)) != null) {
            TextBuffer buf = new TextBuffer();
            switch (obj.item_type) {
                default:
                    break;

                case ITEM_MONEY:
                    if (obj.value[0] == 0) {
                        if (obj.value[1] == 0) {
                            buf.append("Odd...there's no coins in the pile.\n");
                        } else if (obj.value[1] == 1) {
                            buf.append("Wow. One gold coin.\n");
                        } else {
                            buf.sprintf("There are %d gold coins in the pile.\n", obj.value[1]);
                        }
                    } else if (obj.value[1] == 0) {
                        if (obj.value[0] == 1) {
                            buf.append("Wow. One silver coin.\n");
                        } else {
                            buf.sprintf("There are %d silver coins in the pile.\n", obj.value[0]);
                        }
                    } else {
                        buf.sprintf("There are %d gold and %d silver coins in the pile.\n",
                                obj.value[1], obj.value[0]);
                    }
                    send_to_char(buf, ch);
                    break;

                case ITEM_DRINK_CON:
                case ITEM_CONTAINER:
                case ITEM_CORPSE_NPC:
                case ITEM_CORPSE_PC:
                    buf.sprintf("in %s", argument);
                    do_look(ch, buf.toString());
            }
        }

    }

/*
* Thanks to Zrin for auto-exit part.
*/

    static void do_exits(CHAR_DATA ch, String argument) {
        EXIT_DATA pexit;
        boolean found;
        boolean fAuto;
        int door;

        fAuto = !str_cmp(argument, "auto");

        if (!check_blind(ch)) {
            return;
        }

        TextBuffer buf = new TextBuffer();
        if (fAuto) {
            buf.append("{B[Exits:");

        } else if (IS_IMMORTAL(ch)) {
            buf.sprintf("Obvious exits from room %d:\n", ch.in_room.vnum);
        } else {
            buf.sprintf("Obvious exits:\n");
        }

        found = false;
        for (door = 0; door <= 5; door++) {
            if ((pexit = ch.in_room.exit[door]) != null
                    && pexit.to_room != null
                    && can_see_room(ch, pexit.to_room)
                    && !IS_SET(pexit.exit_info, EX_CLOSED)) {
                found = true;
                if (fAuto) {
                    buf.append(" ");
                    buf.append(dir_name[door]);
                } else {
                    buf.sprintf(false, "%-5s - %s", capitalize(dir_name[door]), room_dark(pexit.to_room)
                            ? "Too dark to tell"
                            : pexit.to_room.name
                    );
                    if (IS_IMMORTAL(ch)) {
                        buf.sprintf(false, " (room %d)\n", pexit.to_room.vnum);
                    } else {
                        buf.append("\n");
                    }
                }
            }

            if (number_percent() < get_skill(ch, gsn_perception)
                    && (pexit = ch.in_room.exit[door]) != null
                    && pexit.to_room != null
                    && can_see_room(ch, pexit.to_room)
                    && IS_SET(pexit.exit_info, EX_CLOSED)) {
                check_improve(ch, gsn_perception, true, 5);
                found = true;
                if (fAuto) {
                    buf.append(" ");
                    buf.append(dir_name[door]);
                    buf.append("*");
                } else {
                    buf.sprintf(false, "%-5s * (%s)", capitalize(dir_name[door]), pexit.keyword);
                    if (IS_IMMORTAL(ch)) {
                        buf.sprintf(false, " (room %d)\n", pexit.to_room.vnum);
                    } else {
                        buf.append("\n");
                    }
                }
            }

        }

        if (!found) {
            buf.append(fAuto ? " none" : "None.\n");
        }

        if (fAuto) {
            buf.append("]{x\n");
        }

        send_to_char(buf, ch);
    }

    static void do_worth(CHAR_DATA ch) {
        int total_played;
        TextBuffer buf = new TextBuffer();
        if (IS_NPC(ch)) {
            buf.sprintf("You have %d gold and %d silver.\n", ch.gold, ch.silver);
            send_to_char(buf, ch);
            return;
        }

        buf.sprintf("You have %d gold, %d silver, and %d experience (%d exp to level).\n",
                ch.gold, ch.silver, ch.exp, exp_to_level(ch, ch.pcdata.points));

        send_to_char(buf, ch);
        buf.sprintf("You have killed %d %s and %d %s.\n",
                ch.pcdata.has_killed,
                IS_GOOD(ch) ? "non-goods" :
                        IS_EVIL(ch) ? "non-evils" : "non-neutrals",
                ch.pcdata.anti_killed,
                IS_GOOD(ch) ? "goods" : IS_EVIL(ch) ? "evils" : "neutrals");
        send_to_char(buf, ch);

        total_played = get_total_played(ch);
        buf.sprintf("Within last %d days, you have played %d hour(s) and %d minute(s).\n"
                        + "In order to save limited objects, you need minimum %d hours and %d minute(s).\n",
                nw_config.max_time_log,
                total_played / 60,
                (total_played % 60),
                nw_config.min_time_limit / 60,
                (nw_config.min_time_limit % 60));
        send_to_char(buf, ch);

        if (IS_IMMORTAL(ch)) {
            int l, today, d_time;

            for (l = 0; l < nw_config.max_time_log; l++) {
                today = get_played_day(l);
                d_time = get_played_time(ch, l);
                buf.sprintf("  Day: %3d Playing Time: %3d min(s)\n", today, d_time);
                send_to_char(buf, ch);
            }
        }

    }


    static final String day_name[] = {"the Moon", "the Bull", "Deception", "Thunder", "Freedom",
            "the Great Gods", "the Sun"
    };

    static final String month_name[] = {
            "Winter", "the Winter Wolf", "the Frost Giant", "the Old Forces",
            "the Grand Struggle", "the Spring", "Nature", "Futility", "the Dragon",
            "the Sun", "the Heat", "the Battle", "the Dark Shades", "the Shadows",
            "the Long Shadows", "the Ancient Darkness", "the Great Evil"
    };


    static final String COLOR_DAWN = "{b";
    static final String COLOR_MORNING = "{W";
    static final String COLOR_DAY = "{Y";
    static final String COLOR_EVENING = "{r";
    static final String COLOR_NIGHT = "{w";

    static void do_time(CHAR_DATA ch) {
        String suf;
        int day = time_info.day + 1;
        if (day > 4 && day < 20) {
            suf = "th";
        } else if (day % 10 == 1) {
            suf = "st";
        } else if (day % 10 == 2) {
            suf = "nd";
        } else if (day % 10 == 3) {
            suf = "rd";
        } else {
            suf = "th";
        }

        TextBuffer buf = new TextBuffer();
        buf.sprintf("It is %d o'clock %s, Day of %s, %d%s the Month of %s.\n",
                (time_info.hour % 12 == 0) ? 12 : time_info.hour % 12,
                time_info.hour >= 12 ? "pm" : "am",
                day_name[day % 7],
                day, suf,
                month_name[time_info.month]);

        send_to_char(buf, ch);

        if (!IS_SET(ch.in_room.room_flags, ROOM_INDOORS) || IS_IMMORTAL(ch)) {
            String time = (time_info.hour >= 5 && time_info.hour < 9) ? "dawn" :
                    (time_info.hour >= 9 && time_info.hour < 12) ? "morning" :
                            (time_info.hour >= 12 && time_info.hour < 18) ? "mid-day" :
                                    (time_info.hour >= 18 && time_info.hour < 21) ? "evening" :
                                            "night";
            String color = (time_info.hour >= 5 && time_info.hour < 9) ? COLOR_DAWN :
                    (time_info.hour >= 9 && time_info.hour < 12) ? COLOR_MORNING :
                            (time_info.hour >= 12 && time_info.hour < 18) ? COLOR_DAY :
                                    (time_info.hour >= 18 && time_info.hour < 21) ? COLOR_EVENING :
                                            COLOR_NIGHT;

            buf.sprintf("It's %s%s. {x", color, time);
            act(buf.toString(), ch, null, null, TO_CHAR, POS_RESTING);
        }

        if (!IS_IMMORTAL(ch)) {
            return;
        }
        buf.sprintf("NIGHTWORKS started up at %s\nThe system time is %s.\n", new Date(boot_time * 1000L), new Date(current_time * 1000L));
        send_to_char(buf, ch);
    }


    static final String sky_look[] = {
            "cloudless",
            "cloudy",
            "rainy",
            "lit by flashes of lightning"
    };

    static void do_weather(CHAR_DATA ch) {

        if (!IS_OUTSIDE(ch)) {
            send_to_char("You can't see the weather indoors.\n", ch);
            return;
        }
        TextBuffer buf = new TextBuffer();
        buf.sprintf("The sky is %s and %s.\n",
                sky_look[weather_info.sky],
                weather_info.change >= 0
                        ? "a warm southerly breeze blows"
                        : "a cold northern gust blows"
        );
        send_to_char(buf, ch);
    }


    static void do_help(CHAR_DATA ch, String argument) {

        if (argument.length() == 0) {
            argument = "summary";
        }

        /* this parts handles help a b so that it returns help 'a b' */
        StringBuilder argall = new StringBuilder();
        StringBuilder argone = new StringBuilder();
        while (argument.length() != 0) {
            argone.setLength(0);
            argument = one_argument(argument, argone);
            if (argall.length() != 0) {
                argall.append(" ");
            }
            argall.append(argone);
        }

        String argallstr = argall.toString();
        for (HELP_DATA pHelp = help_first; pHelp != null; pHelp = pHelp.next) {
            if (pHelp.level > get_trust(ch)) {
                continue;
            }

            if (is_name(argallstr, pHelp.keyword)) {
                if (pHelp.level >= 0 && str_cmp(argallstr, "imotd")) {
                    send_to_char(pHelp.keyword, ch);
                    send_to_char("\n", ch);
                }

                /*
                * Strip leading '.' to allow initial blanks.
                */
                page_to_char(pHelp.text, ch);
                return;
            }
        }
        send_to_char("No help on that word.\n", ch);
    }

/* whois command */

    static void do_whois(CHAR_DATA ch, String argument) {
        DESCRIPTOR_DATA d;
        boolean found = false;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.length() == 0) {
            send_to_char("You must provide a name.\n", ch);
            return;
        }

        StringBuilder output = new StringBuilder();
        TextBuffer buf = new TextBuffer();

        for (d = descriptor_list; d != null; d = d.next) {
            CHAR_DATA wch;

            if (d.connected != CON_PLAYING || !can_see(ch, d.character)) {
                continue;
            }

            if (d.connected != CON_PLAYING || (IS_VAMPIRE(d.character) && !IS_IMMORTAL(ch) && (ch != d.character))) {
                continue;
            }

            wch = (d.original != null) ? d.original : d.character;

            if (!can_see(ch, wch)) {
                continue;
            }

            if (!str_prefix(arg.toString(), wch.name)) {
                found = true;

                /* work out the printing */

                String clazz = wch.clazz.who_name;
                switch (wch.level) {
                    case MAX_LEVEL:
                        clazz = "IMP";
                        break;
                    case MAX_LEVEL - 1:
                        clazz = "CRE";
                        break;
                    case MAX_LEVEL - 2:
                        clazz = "SUP";
                        break;
                    case MAX_LEVEL - 3:
                        clazz = "DEI";
                        break;
                    case MAX_LEVEL - 4:
                        clazz = "GOD";
                        break;
                    case MAX_LEVEL - 5:
                        clazz = "IMM";
                        break;
                    case MAX_LEVEL - 6:
                        clazz = "DEM";
                        break;
                    case MAX_LEVEL - 7:
                        clazz = "ANG";
                        break;
                    case MAX_LEVEL - 8:
                        clazz = "AVA";
                        break;
                }

                /* for cabals
                if ((wch.cabal && (ch.cabal == wch.cabal ||
                           IS_TRUSTED(ch,LEVEL_IMMORTAL))) ||
                                       wch.level >= LEVEL_HERO)
                  sprintf(cabalbuf, "[%s] ",cabal_table[wch.cabal].short_name);
                else cabalbuf[0] = '\0';
                */
                String cabalbuf = "";
                if ((wch.cabal != 0 && ch.cabal == wch.cabal) || IS_IMMORTAL(ch)
                        || (IS_SET(wch.act, PLR_CANINDUCT) && wch.cabal == 1)
                        || wch.cabal == CABAL_HUNTER
                        || (wch.cabal == CABAL_RULER
                        && is_equiped_n_char(wch, OBJ_VNUM_RULER_BADGE, WEAR_NECK))) {
                    cabalbuf = "[{c" + cabal_table[wch.cabal].short_name + "{x] ";
                }

                if (wch.cabal == 0) {
                    cabalbuf = "";
                }

                String pk_buf = "";
                if (!((ch == wch && ch.level < PK_MIN_LEVEL) || is_safe_nomessage(ch, wch))) {
                    pk_buf = "{r(PK){x";
                }
                String act_buf = IS_SET(wch.act, PLR_WANTED) ? "{W(WANTED) {x" : "";

                String titlebuf;
                if (IS_NPC(wch)) {
                    titlebuf = "Believer of Chronos.";
                } else {
                    titlebuf = wch.pcdata.title;
                }
                /*
                * Format it up.
                */
                TextBuffer level_buf = new TextBuffer();
                level_buf.sprintf("{c%2d{x", wch.level);
                String classbuf = "{Y" + clazz + "{x";
                /* a little formatting */
                if (IS_TRUSTED(ch, LEVEL_IMMORTAL) || ch == wch || wch.level >= LEVEL_HERO) {
                    buf.sprintf("[%2d %s %s] %s%s%s%s%s\n",
                            wch.level,
                            wch.race.pcRace != null ? wch.race.pcRace.who_name : "     ",
                            classbuf,
                            pk_buf,
                            cabalbuf,
                            act_buf,
                            wch.name,
                            titlebuf);
                } else {
                    buf.sprintf("[%s %s    ] %s%s%s%s%s\n",
                            (get_curr_stat(wch, STAT_CHA) < 18) ? level_buf : "  ",
                            wch.race.pcRace != null ? wch.race.pcRace.who_name : "     ",
                            ((ch == wch && ch.level < PK_MIN_LEVEL) || is_safe_nomessage(ch, wch)) ? "" : "(PK) ",
                            cabalbuf,
                            IS_SET(wch.act, PLR_WANTED) ? "(WANTED) " : "",
                            wch.name,
                            titlebuf);
                }
                output.append(buf);
            }
        }

        if (!found) {
            send_to_char("No one of that name is playing.\n", ch);
            return;
        }

        page_to_char(output.toString(), ch);
    }

/*
* New 'who' command originally by Alander of Rivers of Mud.
*/


    static void do_count(CHAR_DATA ch) {
        int count;
        DESCRIPTOR_DATA d;

        count = 0;

        for (d = descriptor_list; d != null; d = d.next) {
            if (d.connected == CON_PLAYING && can_see(ch, d.character)) {
                count++;
            }
        }

        max_on = UMAX(count, max_on);
        TextBuffer buf = new TextBuffer();
        if (max_on == count) {
            buf.sprintf("There are %d characters on, the most so far today.\n", count);
        } else {
            buf.sprintf("There are %d characters on, the most on today was %d.\n", count, max_on);
        }

        send_to_char(buf, ch);
    }

    static void do_inventory(CHAR_DATA ch) {
        send_to_char("You are carrying:\n", ch);
        show_list_to_char(ch.carrying, ch, true, true);
    }


    static void do_equipment(CHAR_DATA ch) {
        OBJ_DATA obj;
        int iWear;
        boolean found;

        send_to_char("You are using:\n", ch);
        found = false;
        for (iWear = 0; iWear < MAX_WEAR; iWear++) {
            if (iWear == WEAR_FINGER || iWear == WEAR_NECK || iWear == WEAR_WRIST
                    || iWear == WEAR_TATTOO || iWear == WEAR_STUCK_IN) {
                for (obj = ch.carrying; obj != null; obj = obj.next_content) {
                    if (obj.wear_loc == iWear
                            && show_cwear_to_char(ch, obj)) {
                        found = true;
                    }
                }
            } else {
                if ((obj = get_eq_char(ch, iWear)) != null
                        && show_cwear_to_char(ch, obj)) {
                    found = true;
                }
            }
        }

        if (!found) {
            send_to_char("Nothing.\n", ch);
        }

    }


    static void do_compare(CHAR_DATA ch, String argument) {
        OBJ_DATA obj1;
        OBJ_DATA obj2;
        int value1;
        int value2;
        String msg;

        StringBuilder arg1b = new StringBuilder();
        StringBuilder arg2b = new StringBuilder();
        argument = one_argument(argument, arg1b);
        one_argument(argument, arg2b);
        if (arg1b.length() == 0) {
            send_to_char("Compare what to what?\n", ch);
            return;
        }

        String arg1 = arg1b.toString();
        if ((obj1 = get_obj_carry(ch, arg1)) == null) {
            send_to_char("You do not have that item.\n", ch);
            return;
        }

        if (arg2b.length() == 0) {
            for (obj2 = ch.carrying; obj2 != null; obj2 = obj2.next_content) {
                if (obj2.wear_loc != WEAR_NONE
                        && can_see_obj(ch, obj2)
                        && obj1.item_type == obj2.item_type
                        && (obj1.wear_flags & obj2.wear_flags & ~ITEM_TAKE) != 0) {
                    break;
                }
            }

            if (obj2 == null) {
                send_to_char("You aren't wearing anything comparable.\n", ch);
                return;
            }
        } else if ((obj2 = get_obj_carry(ch, arg2b.toString())) == null) {
            send_to_char("You do not have that item.\n", ch);
            return;
        }

        msg = null;
        value1 = 0;
        value2 = 0;

        if (obj1 == obj2) {
            msg = "You compare $p to itself.  It looks about the same.";
        } else if (obj1.item_type != obj2.item_type) {
            msg = "You can't compare $p and $P.";
        } else {
            switch (obj1.item_type) {
                default:
                    msg = "You can't compare $p and $P.";
                    break;

                case ITEM_ARMOR:
                    value1 = obj1.value[0] + obj1.value[1] + obj1.value[2];
                    value2 = obj2.value[0] + obj2.value[1] + obj2.value[2];
                    break;

                case ITEM_WEAPON:
                    if (obj1.pIndexData.new_format) {
                        value1 = (1 + obj1.value[2]) * obj1.value[1];
                    } else {
                        value1 = obj1.value[1] + obj1.value[2];
                    }

                    if (obj2.pIndexData.new_format) {
                        value2 = (1 + obj2.value[2]) * obj2.value[1];
                    } else {
                        value2 = obj2.value[1] + obj2.value[2];
                    }
                    break;
            }
        }

        if (msg == null) {
            if (value1 == value2) {
                msg = "$p and $P look about the same.";
            } else if (value1 > value2) {
                msg = "$p looks better than $P.";
            } else {
                msg = "$p looks worse than $P.";
            }
        }

        act(msg, ch, obj1, obj2, TO_CHAR);
    }


    static void do_credits(CHAR_DATA ch) {
        do_help(ch, "diku");
    }

    static void do_where(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        DESCRIPTOR_DATA d;
        boolean found;
        boolean fPKonly = false;

        StringBuilder argb = new StringBuilder();
        one_argument(argument, argb);

        if (!check_blind(ch)) {
            return;
        }

        if (room_is_dark(ch) && !IS_SET(ch.act, PLR_HOLYLIGHT)) {
            send_to_char("It's too dark to see.\n", ch);
            return;
        }
        String arg = argb.toString();
        if (!str_cmp(arg, "protector")) {
            if (IS_SET(ch.in_room.area.area_flag, AREA_PROTECTED)) {
                send_to_char("This area is protected by Rulers!\n", ch);
            } else {
                send_to_char("This area is not protected.\n", ch);
            }
            return;
        }

        if (!str_cmp(arg, "pk")) {
            fPKonly = true;
        }

        String pkbuf = "{r(PK){x ";

        TextBuffer buf = new TextBuffer();
        if (arg.length() == 0 || fPKonly) {
            send_to_char("Players near you:\n", ch);
            found = false;
            for (d = descriptor_list; d != null; d = d.next) {
                if (d.connected == CON_PLAYING
                        && (victim = d.character) != null
                        && !IS_NPC(victim)
                        && !(fPKonly && is_safe_nomessage(ch, victim))
                        && victim.in_room != null
                        && victim.in_room.area == ch.in_room.area
                        && can_see(ch, victim))

                {
                    found = true;
                    buf.sprintf("%s%-28s %s\n",
                            (is_safe_nomessage(ch,
                                    (is_affected(victim, gsn_doppelganger) && victim.doppel != null) ?
                                            victim.doppel : victim) || IS_NPC(victim)) ?
                                    "  " : pkbuf,
                            (is_affected(victim, gsn_doppelganger)
                                    && !IS_SET(ch.act, PLR_HOLYLIGHT)) ?
                                    victim.doppel.name : victim.name,
                            victim.in_room.name);
                    send_to_char(buf, ch);
                }
            }
            if (!found) {
                send_to_char("None\n", ch);
            }
        } else {
            found = false;
            for (victim = char_list; victim != null; victim = victim.next) {
                if (victim.in_room != null
                        && victim.in_room.area == ch.in_room.area
                        && !IS_AFFECTED(victim, AFF_HIDE)
                        && !IS_AFFECTED(victim, AFF_FADE)
                        && !IS_AFFECTED(victim, AFF_SNEAK)
                        && can_see(ch, victim)
                        && is_name(arg, victim.name)) {
                    found = true;
                    buf.sprintf("%-28s %s\n", PERS(victim, ch), victim.in_room.name);
                    send_to_char(buf, ch);
                    break;
                }
            }
            if (!found) {
                act("You didn't find any $T.", ch, null, arg, TO_CHAR);
            }
        }

    }


    static void do_consider(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        String msg;
        String align;
        int diff;

        StringBuilder argb = new StringBuilder();
        one_argument(argument, argb);

        if (argb.length() == 0) {
            send_to_char("Consider killing whom?\n", ch);
            return;
        }

        String arg = argb.toString();
        if ((victim = get_char_room(ch, arg)) == null) {
            send_to_char("They're not here.\n", ch);
            return;
        }

        if (is_safe(ch, victim)) {
            send_to_char("Don't even think about it.\n", ch);
            return;
        }

        diff = victim.level - ch.level;

        if (diff <= -10) {
            msg = "You can kill $N naked and weaponless.";
        } else if (diff <= -5) {
            msg = "$N is no match for you.";
        } else if (diff <= -2) {
            msg = "$N looks like an easy kill.";
        } else if (diff <= 1) {
            msg = "The perfect match!";
        } else if (diff <= 4) {
            msg = "$N says 'Do you feel lucky, punk?'.";
        } else if (diff <= 9) {
            msg = "$N laughs at you mercilessly.";
        } else {
            msg = "Death will thank you for your gift.";
        }

        if (IS_EVIL(ch) && IS_EVIL(victim)) {
            align = "$N grins evilly with you.";
        } else if (IS_GOOD(victim) && IS_GOOD(ch)) {
            align = "$N greets you warmly.";
        } else if (IS_GOOD(victim) && IS_EVIL(ch)) {
            align = "$N smiles at you, hoping you will turn from your evil path.";
        } else if (IS_EVIL(victim) && IS_GOOD(ch)) {
            align = "$N grins evilly at you.";
        } else if (IS_NEUTRAL(ch) && IS_EVIL(victim)) {
            align = "$N grins evilly.";
        } else if (IS_NEUTRAL(ch) && IS_GOOD(victim)) {
            align = "$N smiles happily.";
        } else if (IS_NEUTRAL(ch) && IS_NEUTRAL(victim)) {
            align = "$N looks just as disinterested as you.";
        } else {
            align = "$N looks very disinterested.";
        }

        act(msg, ch, null, victim, TO_CHAR);
        act(align, ch, null, victim, TO_CHAR);
    }


    static void set_title(CHAR_DATA ch, String title) {
        if (IS_NPC(ch)) {
            bug("Set_title: NPC.");
            return;
        }

        char c = title.charAt(0);
        if (c == '.' || c == ',' || c == '!' || c == '?') {
            title = " " + title.substring(1);
        }
        if (title.length() > 45) {
            title = title.substring(0, 45);
        }
        ch.pcdata.title = title;
    }


    static void do_title(CHAR_DATA ch, String argument) {
        if (IS_NPC(ch)) {
            return;
        }

        if (CANT_CHANGE_TITLE(ch)) {
            send_to_char("You can't change your title.\n", ch);
            return;
        }

        if (argument.length() == 0) {
            send_to_char("Change your title to what?\n", ch);
            return;
        }

        if (argument.length() > 45) {
            argument = argument.substring(0, 45);
        }

        argument = smash_tilde(argument);
        set_title(ch, argument);
        send_to_char("Ok.\n", ch);
    }


    static void do_description(CHAR_DATA ch, String argument) {

        StringBuilder buf = new StringBuilder();
        if (argument.length() != 0) {
            argument = smash_tilde(argument);

            if (argument.charAt(0) == '-') {
                int len;
                boolean found = false;

                if (ch.description == null || ch.description.length() == 0) {
                    send_to_char("No lines left to remove.\n", ch);
                    return;
                }

                buf.append(ch.description);

                for (len = buf.length(); -len >= 0; ) {
                    if (buf.charAt(len) == '\r') {
                        if (!found)  /* back it up */ {
                            if (len > 0) {
                                len--;
                            }
                            found = true;
                        } else /* found the second one */ {
                            ch.description = buf.substring(0, len + 1);
                            send_to_char("Your description is:\n", ch);
                            send_to_char(ch.description.length() != 0 ? ch.description : "(None).\n", ch);
                            return;
                        }
                    }
                }
                ch.description = "";
                send_to_char("Description cleared.\n", ch);
                return;
            }
            if (argument.charAt(0) == '+') {
                if (ch.description != null) {
                    buf.append(ch.description);
                }
                argument = argument.substring(1).trim();
            }


            buf.append(argument);
            buf.append("\n");
            ch.description = buf.toString();
        }

        send_to_char("Your description is:\n", ch);
        send_to_char(ch.description.length() != 0 ? ch.description : "(None).\n", ch);
    }


    static void do_report(CHAR_DATA ch) {
        TextBuffer buf = new TextBuffer();
        buf.sprintf("I have %d/%d hp %d/%d mana %d/%d mv",
                ch.hit, ch.max_hit,
                ch.mana, ch.max_mana,
                ch.move, ch.max_move);
        do_say(ch, buf.toString());

    }


    static void do_practice(CHAR_DATA ch, String argument) {
        if (IS_NPC(ch)) {
            return;
        }

        TextBuffer buf = new TextBuffer();
        StringBuilder buf2 = new StringBuilder();
        if (argument.length() == 0) {
            int col;

            col = 0;
            for (Skill sn : Skill.skills) {
                if (sn.name == null) {
                    break;
                }
                if (skill_failure_nomessage(ch, sn, 0) != 0) {
                    continue;
                }

                buf.sprintf("%-18s %3d%%  ", sn.name, ch.pcdata.learned[sn.ordinal()]);
                buf.append(buf2);
                if (++col % 3 == 0) {
                    buf2.append("\n");
                }
            }

            if (col % 3 != 0) {
                buf2.append("\n");
            }

            buf.sprintf("You have {w%d{x practice sessions left.\n", ch.practice);
            buf2.append(buf);

            if (IS_IMMORTAL(ch)) {
                page_to_char(buf2, ch);
            } else {
                send_to_char(buf2, ch);
            }
        } else {
            CHAR_DATA mob;
            int adept;

            if (!IS_AWAKE(ch)) {
                send_to_char("In your dreams, or what?\n", ch);
                return;
            }

            for (mob = ch.in_room.people; mob != null; mob = mob.next_in_room) {
                if (IS_NPC(mob) && IS_SET(mob.act, ACT_PRACTICE)) {
                    break;
                }
            }

            if (mob == null) {
                send_to_char("You can't do that here.\n", ch);
                return;
            }

            if (ch.practice <= 0) {
                send_to_char("You have no practice sessions left.\n", ch);
                return;
            }
            Skill sn = find_spell(ch, argument);
            if (sn == null || skill_failure_nomessage(ch, sn, 0) != 0) {
                send_to_char("You can't practice that.\n", ch);
                return;
            }

            if (!str_cmp("vampire", sn.name)) {
                send_to_char("You can't practice that, only available at questor.\n", ch);
                return;
            }

            adept = IS_NPC(ch) ? 100 : ch.clazz.skill_adept;

            if (ch.pcdata.learned[sn.ordinal()] >= adept) {
                buf.sprintf("You are already learned at %s.\n",
                        sn.name);
                send_to_char(buf, ch);
            } else {
                if (ch.pcdata.learned[sn.ordinal()] == 0) {
                    ch.pcdata.learned[sn.ordinal()] = 1;
                }
                ch.practice--;
                ch.pcdata.learned[sn.ordinal()] +=
                        int_app[get_curr_stat(ch, STAT_INT)].learn /
                                UMAX(sn.rating[ch.clazz.id], 1);
                if (ch.pcdata.learned[sn.ordinal()] < adept) {
                    act("You practice $T.",
                            ch, null, sn.name, TO_CHAR);
                    act("$n practices $T.",
                            ch, null, sn.name, TO_ROOM);
                } else {
                    ch.pcdata.learned[sn.ordinal()] = adept;
                    act("You are now learned at $T.",
                            ch, null, sn.name, TO_CHAR);
                    act("$n is now learned at $T.",
                            ch, null, sn.name, TO_ROOM);
                }
            }
        }
    }

/*
* 'Wimpy' originally by Dionysos.
*/

    static void do_wimpy(CHAR_DATA ch, String argument) {
        int wimpy;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);


        if ((ch.clazz == Clazz.SAMURAI) && (ch.level >= 10)) {
            send_to_char("You don't deal with wimpies, or such feary things.\n", ch);
            if (ch.wimpy != 0) {
                ch.wimpy = 0;
            }
            return;
        }

        if (arg.length() == 0) {
            wimpy = ch.max_hit / 5;
        } else {
            wimpy = atoi(arg.toString());
        }

        if (wimpy < 0) {
            send_to_char("Your courage exceeds your wisdom.\n", ch);
            return;
        }

        if (wimpy > ch.max_hit / 2) {
            send_to_char("Such cowardice ill becomes you.\n", ch);
            return;
        }

        ch.wimpy = wimpy;
        TextBuffer buf = new TextBuffer();
        buf.sprintf("Wimpy set to %d hit points.\n", wimpy);
        send_to_char(buf, ch);
    }


    static void do_password(CHAR_DATA ch, String argument) {
        if (IS_NPC(ch)) {
            return;
        }

        /*
         * Can't use one_argument here because it smashes case.
         * So we just steal all its code.  Bleagh.
         */
        StringBuilder arg_first = new StringBuilder();
        argument = trimSpaces(argument, 0);
        int len = argument.length();
        char cEnd = len != 0 ? argument.charAt(0) : ' ';
        if (cEnd != '\'' && cEnd != '"') {
            arg_first.append(cEnd);
            cEnd = ' ';
        }

        int pos = 1;
        for (; pos < len; pos++) {
            char c = argument.charAt(pos);
            if (c == cEnd) {
                pos++;
                break;
            }
            arg_first.append(c);
        }
        argument = trimSpaces(argument, pos);

        String arg1 = arg_first.toString();
        String arg2 = argument;
        if (arg1.length() == 0 || arg2.length() == 0) {
            send_to_char("Syntax: password <old> <new>.\n", ch);
            return;
        }

        if (!crypt(arg1, ch.name).equals(ch.pcdata.pwd)) {
            WAIT_STATE(ch, 40);
            send_to_char("Wrong password.  Wait 10 seconds.\n", ch);
            return;
        }

        if (arg2.length() < 5) {
            send_to_char("New password must be at least five characters long.\n", ch);
            return;
        }

        /*
         * No tilde allowed because of player file format.
         */
        String pwdnew = crypt(arg2, ch.name);
        if (pwdnew.contains("~")) {
            send_to_char("New password not acceptable, try again.\n", ch);
            return;
        }

        ch.pcdata.pwd = pwdnew;
        save_char_obj(ch);
        send_to_char("Ok.\n", ch);
    }

/* RT configure command */

    static void do_scan(CHAR_DATA ch, String argument) {
        String dir2;
        ROOM_INDEX_DATA in_room;
        ROOM_INDEX_DATA to_room;
        EXIT_DATA exit;  /* pExit */
        int door;
        int range;
        int i;
        CHAR_DATA person;
        int numpeople;

        StringBuilder dir = new StringBuilder();
        one_argument(argument, dir);

        if (dir.length() == 0)

        {
            do_scan2(ch);
            return;
        }

        switch (dir.charAt(0)) {
            case 'N':
            case 'n':
                door = 0;
                dir2 = "north";
                break;
            case 'E':
            case 'e':
                door = 1;
                dir2 = "east";
                break;
            case 'S':
            case 's':
                door = 2;
                dir2 = "south";
                break;
            case 'W':
            case 'w':
                door = 3;
                dir2 = "west";
                break;
            case 'U':
            case 'u':
                door = 4;
                dir2 = "up";
                break;
            case 'D':
            case 'd':
                door = 5;
                dir2 = "down";
                break;
            default:
                send_to_char("That's not a direction.\n", ch);
                return;
        }
        TextBuffer buf = new TextBuffer();
        buf.sprintf("You scan %s.\n", dir2);
        send_to_char(buf, ch);
        buf.sprintf("$n scans %s.", dir2);
        act(buf.toString(), ch, null, null, TO_ROOM);

        if (!check_blind(ch)) {
            return;
        }

        range = 1 + (ch.level) / 10;

        in_room = ch.in_room;

        for (i = 1; i <= range; i++) {
            if ((exit = in_room.exit[door]) == null
                    || (to_room = exit.to_room) == null
                    || IS_SET(exit.exit_info, EX_CLOSED)) {
                return;
            }

            for (numpeople = 0, person = to_room.people; person != null;
                 person = person.next_in_room) {
                if (can_see(ch, person)) {
                    numpeople++;
                }
            }

            if (numpeople != 0) {
                buf.sprintf("***** Range %d *****\n", i);
                send_to_char(buf, ch);
                show_char_to_char(to_room.people, ch);
                send_to_char("\n", ch);
            }
            in_room = to_room;
        }
    }

    static void do_request(CHAR_DATA ch, String argument) {

        if (is_affected(ch, gsn_reserved)) {
            send_to_char("Wait for a while to request again.\n", ch);
            return;
        }

        StringBuilder arg1b = new StringBuilder();
        StringBuilder arg2b = new StringBuilder();
        argument = one_argument(argument, arg1b);
        one_argument(argument, arg2b);

        if (IS_NPC(ch)) {
            return;
        }

        if (arg1b.length() == 0 || arg2b.length() == 0) {
            send_to_char("Request what from whom?\n", ch);
            return;
        }

        String arg2 = arg2b.toString();
        CHAR_DATA victim = get_char_room(ch, arg2);
        if (victim == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (!IS_NPC(victim)) {
            send_to_char("Why don't you just ask the player?\n", ch);
            return;
        }

        if (!IS_GOOD(ch)) {
            do_say(victim, "I will not give anything to someone so impure.");
            return;
        }

        if (ch.move < (50 + ch.level)) {
            do_say(victim, "You look rather tired, why don't you rest a bit first?");
            return;
        }

        WAIT_STATE(ch, PULSE_VIOLENCE);
        ch.move -= 10;
        ch.move = UMAX(ch.move, 0);

        if (victim.level >= ch.level + 10 || victim.level >= ch.level * 2) {
            do_say(victim, "In good time, my child");
            return;
        }
        String arg1 = arg1b.toString();
        OBJ_DATA obj = get_obj_carry(victim, arg1);
        if ((obj == null && (obj = get_obj_wear(victim, arg1)) == null) || IS_SET(obj.extra_flags, ITEM_INVENTORY)) {
            do_say(victim, "Sorry, I don't have that.");
            return;
        }

        if (!IS_GOOD(victim)) {
            do_say(victim, "I'm not about to give you anything!");
            do_murder(victim, ch.name);
            return;
        }

        if (obj.wear_loc != WEAR_NONE) {
            unequip_char(victim, obj);
        }

        if (!can_drop_obj(ch, obj)) {
            do_say(victim, "Sorry, I can't let go of it.  It's cursed.");
            return;
        }

        if (ch.carry_number + get_obj_number(obj) > can_carry_n(ch)) {
            send_to_char("Your hands are full.\n", ch);
            return;
        }

        if (ch.carry_weight + get_obj_weight(obj) > can_carry_w(ch)) {
            send_to_char("You can't carry that much weight.\n", ch);
            return;
        }

        if (!can_see_obj(ch, obj)) {
            act("You don't see that.", ch, null, victim, TO_CHAR);
            return;
        }

        obj_from_char(obj);
        obj_to_char(obj, ch);
        act("$n requests $p from $N.", ch, obj, victim, TO_NOTVICT);
        act("You request $p from $N.", ch, obj, victim, TO_CHAR);
        act("$n requests $p from you.", ch, obj, victim, TO_VICT);


        if (IS_SET(obj.progtypes, OPROG_GIVE)) {
            obj.pIndexData.oprogs.give_prog.run(obj, ch, victim);
        }

        if (IS_SET(victim.progtypes, MPROG_GIVE)) {
            victim.pIndexData.mprogs.give_prog.run(victim, ch, obj);
        }


        ch.move -= (50 + ch.level);
        ch.move = UMAX(ch.move, 0);
        ch.hit -= 3 * (ch.level / 2);
        ch.hit = UMAX(ch.hit, 0);

        act("You feel grateful for the trust of $N.", ch, null, victim, TO_CHAR);
        send_to_char("and for the goodness you have seen in the world.\n", ch);

        AFFECT_DATA af = new AFFECT_DATA();
        af.type = gsn_reserved;
        af.where = TO_AFFECTS;
        af.level = ch.level;
        af.duration = ch.level / 10;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = 0;
        affect_to_char(ch, af);

    }


    static void do_hometown(CHAR_DATA ch, String argument) {
        int amount;

        if (IS_NPC(ch)) {
            send_to_char("You can't change your hometown!\n", ch);
            return;
        }

        Race race = ORG_RACE(ch);
        if (race == Race.STORM_GIANT || race == Race.CLOUD_GIANT || race == Race.FIRE_GIANT || race == Race.FROST_GIANT) {
            send_to_char("Your hometown is permanently Titan Valley!\n", ch);
            return;
        }

        if (ch.clazz == Clazz.VAMPIRE || ch.clazz == Clazz.NECROMANCER) {
            send_to_char("Your hometown is permanently Old Midgaard!\n", ch);
            return;
        }

        if (!IS_SET(ch.in_room.room_flags, ROOM_REGISTRY)) {
            send_to_char("You have to be in the Registry to change your hometown.\n",
                    ch);
            return;
        }

        amount = (ch.level * ch.level * 250) + 1000;

        if (argument.length() == 0) {
            TextBuffer buf = new TextBuffer();
            buf.sprintf("It will cost you %d gold.\n", amount);
            send_to_char(buf, ch);
            return;
        }

        if (ch.gold < amount) {
            send_to_char("You don't have enough money to change hometowns!\n", ch);
            return;
        }
        if (!str_prefix(argument, "midgaard")) {
            if (ch.hometown == 0) {
                send_to_char("But you already live in Midgaard!\n", ch);
                return;
            }
            ch.gold -= amount;
            send_to_char("Your hometown is changed to Midgaard.\n", ch);
            ch.hometown = 0;
        } else if (!str_prefix(argument, "newthalos")) {
            if (ch.hometown == 1) {
                send_to_char("But you already live in New Thalos!\n", ch);
                return;
            }
            ch.gold -= amount;
            send_to_char("Your hometown is changed to New Thalos.\n", ch);
            ch.hometown = 1;
        } else if (!str_prefix(argument, "titans")) {
            if (ch.hometown == 2) {
                send_to_char("But you already live in Titan!\n", ch);
                return;
            }
            ch.gold -= amount;
            send_to_char("Your hometown is changed to Titan.\n", ch);
            ch.hometown = 2;
        } else if (!str_prefix(argument, "ofcol")) {
            if (!IS_NEUTRAL(ch)) {
                send_to_char("Only neutral people can live in Ofcol.\n", ch);
                return;
            }
            if (ch.hometown == 3) {
                send_to_char("But you already live in Ofcol!\n", ch);
                return;
            }
            ch.gold -= amount;
            send_to_char("Your hometown is changed to Ofcol.\n", ch);
            ch.hometown = 3;
        } else if (!str_prefix(argument, "oldmidgaard")) {
            if (ch.clazz == Clazz.VAMPIRE || ch.clazz == Clazz.NECROMANCER) {
                send_to_char("Only vampires and necromancers live there.\n", ch);
                return;
            }
            if (ch.hometown == 4) {
                send_to_char("But you already live in Old Midgaard!\n", ch);
                return;
            }
            ch.gold -= amount;
            send_to_char("Your hometown is changed to Old Midgaard.\n", ch);
            ch.hometown = 4;
        } else {
            send_to_char("That is not a valid choice.\n", ch);
            send_to_char("Choose from Midgaard, Newthalos, Titans, Ofcol and Old Midgaard.\n", ch);
        }
    }


    static void do_detect_hidden(CHAR_DATA ch) {

        if (skill_failure_check(ch, gsn_detect_hidden, true, 0, null)) {
            return;
        }

        if (IS_AFFECTED(ch, AFF_DETECT_HIDDEN)) {
            send_to_char("You are already as alert as you can be. \n", ch);
            return;
        }
        if (number_percent() > get_skill(ch, gsn_detect_hidden)) {
            send_to_char(
                    "You peer intently at the shadows but they are unrevealing.\n",
                    ch);
            return;
        }
        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = gsn_detect_hidden;
        af.level = ch.level;
        af.duration = ch.level;
        af.location = APPLY_NONE;
        af.modifier = 0;
        af.bitvector = AFF_DETECT_HIDDEN;
        affect_to_char(ch, af);
        send_to_char("Your awareness improves.\n", ch);
    }


    static void do_bear_call(CHAR_DATA ch) {
        CHAR_DATA gch;
        CHAR_DATA bear;
        CHAR_DATA bear2;
        int i;

        if (skill_failure_check(ch, gsn_bear_call, true, 0, null)) {
            return;
        }

        send_to_char("You call for bears help you.\n", ch);
        act("$n shouts a bear call.", ch, null, null, TO_ROOM);

        if (is_affected(ch, gsn_bear_call)) {
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

        if (ch.in_room != null && IS_SET(ch.in_room.room_flags, ROOM_NO_MOB)) {
            send_to_char("No bears listen you.\n", ch);
            return;
        }

        if (number_percent() > get_skill(ch, gsn_bear_call)) {
            send_to_char("No bears listen you.\n", ch);
            check_improve(ch, gsn_bear_call, true, 1);
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
            send_to_char("No bears come to your rescue.\n", ch);
            return;
        }

        if (ch.mana < 125) {
            send_to_char("You don't have enough mana to shout a bear call.\n", ch);
            return;
        }
        ch.mana -= 125;

        check_improve(ch, gsn_bear_call, true, 1);
        bear = create_mobile(get_mob_index(MOB_VNUM_BEAR));

        for (i = 0; i < MAX_STATS; i++) {
            bear.perm_stat[i] = UMIN(25, 2 * ch.perm_stat[i]);
        }

        bear.max_hit = IS_NPC(ch) ? ch.max_hit : ch.pcdata.perm_hit;
        bear.hit = bear.max_hit;
        bear.max_mana = IS_NPC(ch) ? ch.max_mana : ch.pcdata.perm_mana;
        bear.mana = bear.max_mana;
        bear.alignment = ch.alignment;
        bear.level = UMIN(100, ch.level - 2);
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
        af.type = gsn_bear_call;
        af.level = ch.level;
        af.duration = 24;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

    }


    static void do_identify(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        CHAR_DATA rch;

        if ((obj = get_obj_carry(ch, argument)) == null) {
            send_to_char("You are not carrying that.\n", ch);
            return;
        }

        for (rch = ch.in_room.people; rch != null; rch = rch.next_in_room) {
            if (IS_NPC(rch) && rch.pIndexData.vnum == MOB_VNUM_SAGE) {
                break;
            }
        }

        if (rch == null) {
            send_to_char("No one here seems to know much about that.\n", ch);
            return;
        }

        if (IS_IMMORTAL(ch)) {
            act("$n looks at you!\n", rch, obj, ch, TO_VICT);
        } else if (ch.gold < 1) {
            act("$n resumes to identify by looking at $p.",
                    rch, obj, 0, TO_ROOM);
            send_to_char(" You need at least 1 gold.\n", ch);
            return;
        } else {
            ch.gold -= 1;
            send_to_char("Your purse feels lighter.\n", ch);
        }

        act("$n gives a wise look at $p.", rch, obj, 0, TO_ROOM);
        spell_identify(Skill.gsn_identify, 0, ch, obj, 0);
    }


    static void do_score(CHAR_DATA ch) {
        TextBuffer buf = new TextBuffer();
        buf.sprintf("You are {Y%s{x%s, level {Y%d{x, {W%d{x years old (%d hours).\n",
                ch.name, IS_NPC(ch) ? "" : ch.pcdata.title, ch.level, get_age(ch),
                (ch.played + (int) (current_time - ch.logon)) / 3600);
        send_to_char(buf, ch);

        if (get_trust(ch) != ch.level) {
            buf.sprintf("You are trusted at level %d.\n", get_trust(ch));
            send_to_char(buf, ch);
        }

        buf.sprintf("Race: {B%s{x  Sex: {B%s{x  Class: {B%s{x  Hometown: {B%s{x\n",
                ORG_RACE(ch).name, ch.sex == 0 ? "sexless" : ch.sex == 1 ? "male" : "female",
                IS_NPC(ch) ? "mobile" : ch.clazz.name,
                IS_NPC(ch) ? "Midgaard" : hometown_table[ch.hometown].name);
        send_to_char(buf, ch);

        buf.sprintf("You have %d/{w%d{x hit, %d/{w%d{x mana, %d/{w%d{x movement.\n",
                ch.hit, ch.max_hit, ch.mana, ch.max_mana, ch.move, ch.max_move);

        send_to_char(buf, ch);

        buf.sprintf("You have {B%d{x practices and {B%d{x training sessions.\n",
                ch.practice, ch.train);
        send_to_char(buf, ch);

        buf.sprintf("You are carrying {c%d{x/{B%d{x items with weight {c%d{x/{B%d{x pounds.\n",
                ch.carry_number, can_carry_n(ch), get_carry_weight(ch), can_carry_w(ch));
        send_to_char(buf, ch);

        if (ch.level > 20 || IS_NPC(ch)) {
            buf.sprintf("Str: {Y%d{b({Y%d{B)  Int: {Y%d{b({Y%d{b)  Wis: {Y%d{b({Y%d{b)  Dex: {Y%d{b({Y%d{b)  Con: {Y%d{b({Y%d{b) Cha: {Y%d{b({Y%d{b)\n"
                    , ch.perm_stat[STAT_STR]
                    , get_curr_stat(ch, STAT_STR)
                    , ch.perm_stat[STAT_INT]
                    , get_curr_stat(ch, STAT_INT)
                    , ch.perm_stat[STAT_WIS]
                    , get_curr_stat(ch, STAT_WIS)
                    , ch.perm_stat[STAT_DEX]
                    , get_curr_stat(ch, STAT_DEX)
                    , ch.perm_stat[STAT_CON]
                    , get_curr_stat(ch, STAT_CON)
                    , ch.perm_stat[STAT_CHA]
                    , get_curr_stat(ch, STAT_CHA));
            send_to_char(buf, ch);
        } else {
            buf.sprintf(
                    "Str: {Y%-9s{x Wis: {Y%-9s{x Con: {Y%-9s{x\nInt: {Y%-9s{x Dex: {Y%-9s{x Cha: {Y%-11s{x\n",
                    get_stat_alias(ch, STAT_STR),
                    get_stat_alias(ch, STAT_WIS),
                    get_stat_alias(ch, STAT_CON),
                    get_stat_alias(ch, STAT_INT),
                    get_stat_alias(ch, STAT_DEX),
                    get_stat_alias(ch, STAT_CHA));

            send_to_char(buf, ch);
        }

        buf.sprintf("You have scored {m%d{x exp, and have {B%s%s%s{x.\n",
                ch.exp, ch.gold + ch.silver == 0 ? "no money" : ch.gold != 0 ? "%d gold " : "",
                ch.silver != 0 ? "%d silver " : "", ch.gold + ch.silver != 0 ? ch.gold + ch.silver == 1 ? "coin" : "coins" : "");
        TextBuffer buf2 = new TextBuffer();
        if (ch.gold != 0) {
            buf2.sprintf(buf.toString(), ch.gold, ch.silver);
        } else {
            buf2.sprintf(buf.toString(), ch.silver);
        }

        send_to_char(buf2, ch);

        /* KIO shows exp to level */
        if (!IS_NPC(ch) && ch.level < LEVEL_HERO) {
            buf.sprintf("You need {c%d{x exp to level.\n", exp_to_level(ch, ch.pcdata.points));
            send_to_char(buf, ch);
        }

        if (!(IS_NPC(ch))) {
            buf.sprintf("Quest Points: {B%d{x.  Next Quest Time: {B%d{x.\n",
                    ch.pcdata.questpoints, ch.pcdata.nextquest);
            send_to_char(buf, ch);
        }

        if (ch.clazz != Clazz.SAMURAI) {
            buf.sprintf("Wimpy set to {r%d{x hit points.  ", ch.wimpy);
            send_to_char(buf, ch);
        } else {
            buf.sprintf("Total {r%d{x deaths up to now.", ch.pcdata.death);
            send_to_char(buf, ch);
        }
        if (ch.guarding != null) {
            buf.sprintf("You are guarding: {g%s{x  ", ch.guarding.name);
            send_to_char(buf, ch);
        }

        if (ch.guarded_by != null) {
            buf.sprintf("You are guarded by: {g%s{x", ch.guarded_by.name);
            send_to_char(buf, ch);
        }

        send_to_char("\n", ch);
        if (!IS_NPC(ch) && ch.pcdata.condition[COND_DRUNK] > 10) {
            send_to_char("You are drunk.\n", ch);
        }
        if (!IS_NPC(ch) && ch.pcdata.condition[COND_THIRST] <= 0) {
            send_to_char("You are thirsty.\n", ch);
        }
/*    if ( !IS_NPC(ch) && ch.pcdata.condition[COND_FULL]   ==  0 ) */
        if (!IS_NPC(ch) && ch.pcdata.condition[COND_HUNGER] <= 0) {
            send_to_char("You are hungry.\n", ch);
        }
        if (!IS_NPC(ch) && ch.pcdata.condition[COND_BLOODLUST] <= 0) {
            send_to_char("You are hungry for blood.\n", ch);
        }
        if (!IS_NPC(ch) && ch.pcdata.condition[COND_DESIRE] <= 0) {
            send_to_char("You are desiring your home.\n", ch);
        }

        switch (ch.position) {
            case POS_DEAD:
                send_to_char("{rYou are DEAD!!\n", ch);
                break;
            case POS_MORTAL:
                send_to_char("{rYou are mortally wounded.\n", ch);
                break;
            case POS_INCAP:
                send_to_char("{rYou are incapacitated.\n", ch);
                break;
            case POS_STUNNED:
                send_to_char("{rYou are stunned.\n", ch);
                break;
            case POS_SLEEPING:
                send_to_char("{yYou are sleeping.", ch);
                if (ch.last_fight_time != -1 && !IS_IMMORTAL(ch) &&
                        (current_time - ch.last_fight_time) < FIGHT_DELAY_TIME) {
                    send_to_char("Your adrenalin is gushing!\n", ch);
                } else {
                    send_to_char("\n", ch);
                }
                break;
            case POS_RESTING:
                send_to_char("{bYou are resting.", ch);
                if (ch.last_fight_time != -1 && !IS_IMMORTAL(ch) &&
                        (current_time - ch.last_fight_time) < FIGHT_DELAY_TIME) {
                    send_to_char("Your adrenalin is gushing!\n", ch);
                } else {
                    send_to_char("\n", ch);
                }
                break;
            case POS_STANDING:
                send_to_char("{cYou are standing.", ch);
                if (ch.last_fight_time != -1 && !IS_IMMORTAL(ch) &&
                        (current_time - ch.last_fight_time) < FIGHT_DELAY_TIME) {
                    send_to_char("Your adrenalin is gushing!\n", ch);
                } else {
                    send_to_char("\n", ch);
                }
                break;
            case POS_FIGHTING:
                send_to_char("{rYou are fighting.", ch);
                if (ch.last_fight_time != -1 && !IS_IMMORTAL(ch) &&
                        (current_time - ch.last_fight_time) < FIGHT_DELAY_TIME) {
                    send_to_char("Your adrenalin is gushing!\n", ch);
                } else {
                    send_to_char("\n", ch);
                }
                break;
        }
        send_to_char("{x", ch);

        /* print AC values */
        if (ch.level >= 25) {
            buf.sprintf("Armor: pierce: {B%d{x  bash: {B%d{x  slash: {B%d{x  magic: {B%d{x\n",
                    GET_AC(ch, AC_PIERCE), GET_AC(ch, AC_BASH), GET_AC(ch, AC_SLASH), GET_AC(ch, AC_EXOTIC));
            send_to_char(buf, ch);
        }

        TextBuffer temp = new TextBuffer();
        for (int i = 0; i < 4; i++) {
            switch (i) {
                case (AC_PIERCE):
                    temp.sprintf("{rpiercing{x");
                    break;
                case (AC_BASH):
                    temp.sprintf("{rbashing{x");
                    break;
                case (AC_SLASH):
                    temp.sprintf("{rslashing{x");
                    break;
                case (AC_EXOTIC):
                    temp.sprintf("{rmagic{x");
                    break;
                default:
                    temp.sprintf("{rerror{x");
                    break;
            }

            send_to_char("You are ", ch);

            if (GET_AC(ch, i) >= 101) {
                buf.sprintf("hopelessly vulnerable to %s.\n", temp);
            } else if (GET_AC(ch, i) >= 80) {
                buf.sprintf("defenseless against %s.\n", temp);
            } else if (GET_AC(ch, i) >= 60) {
                buf.sprintf("barely protected from %s.\n", temp);
            } else if (GET_AC(ch, i) >= 40) {
                buf.sprintf("slightly armored against %s.\n", temp);
            } else if (GET_AC(ch, i) >= 20) {
                buf.sprintf("somewhat armored against %s.\n", temp);
            } else if (GET_AC(ch, i) >= 0) {
                buf.sprintf("armored against %s.\n", temp);
            } else if (GET_AC(ch, i) >= -20) {
                buf.sprintf("well-armored against %s.\n", temp);
            } else if (GET_AC(ch, i) >= -40) {
                buf.sprintf("very well-armored against %s.\n", temp);
            } else if (GET_AC(ch, i) >= -60) {
                buf.sprintf("heavily armored against %s.\n", temp);
            } else if (GET_AC(ch, i) >= -80) {
                buf.sprintf("superbly armored against %s.\n", temp);
            } else if (GET_AC(ch, i) >= -100) {
                buf.sprintf("almost invulnerable to %s.\n", temp);
            } else {
                buf.sprintf("divinely armored against %s.\n", temp);
            }

            send_to_char(buf, ch);
        }

        /* RT wizinvis and holy light */
        if (IS_IMMORTAL(ch)) {
            send_to_char("{gHoly Light: ", ch);
            if (IS_SET(ch.act, PLR_HOLYLIGHT)) {
                send_to_char("on", ch);
            } else {
                send_to_char("off", ch);
            }
            send_to_char("{x", ch);

            if (ch.invis_level != 0) {
                buf.sprintf("  Invisible: level %d", ch.invis_level);
                send_to_char(buf, ch);
            }

            if (ch.incog_level != 0) {
                buf.sprintf("  Incognito: level %d", ch.invis_level);
                send_to_char(buf, ch);
            }
            send_to_char("\n", ch);

        }
        if (ch.level >= 20) {
            buf.sprintf("Hitroll: {Y%d{x  Damroll: {Y%d{x.\n", GET_HITROLL(ch), GET_DAMROLL(ch));
            send_to_char(buf, ch);
        }

        send_to_char("You are ", ch);
        if (IS_GOOD(ch)) {
            send_to_char("{Wgood.  ", ch);
        } else if (IS_EVIL(ch)) {
            send_to_char("{revil.  ", ch);
        } else {
            send_to_char("{cneutral.  ", ch);
        }
        send_to_char("{x", ch);

        buf.sprintf("You have a %s ethos.", ethos_table[ch.ethos]);
        send_to_char(buf, ch);
        if (IS_NPC(ch)) {
            ch.religion = 0;
        }
        if ((ch.religion <= RELIGION_NONE) || (ch.religion > MAX_RELIGION)) {
            send_to_char("You don't believe any religion.\n", ch);
        } else {
            buf.sprintf("Your religion is the way of {B%s{x.\n", religion_table[ch.religion].leader);
            send_to_char(buf, ch);
        }
        if (ch.affected != null && IS_SET(ch.comm, COMM_SHOW_AFFECTS)) {
            send_to_char("You are affected by:\n", ch);
            for (AFFECT_DATA paf = ch.affected; paf != null; paf = paf.next) {
                buf.sprintf("{rSpell{x: '{Y%s{x'", paf.type.name);
                send_to_char(buf, ch);

                if (ch.level >= 20) {
                    if (paf.duration != -1 && paf.duration != -2) {
                        buf.sprintf(" modifies {m%s{x by {m%d{x for {m%d{x hours", affect_loc_name(paf.location), paf.modifier, paf.duration);
                    } else {
                        buf.sprintf(" modifies {m%s{x by {m%d{x {cpermanently{x", affect_loc_name(paf.location), paf.modifier);
                        send_to_char(buf, ch);
                    }

                    send_to_char(".\n", ch);
                }
            }

        }
    }


    static void do_affects(CHAR_DATA ch) {
        if (ch.affected != null) {
            send_to_char("You are affected by the following spells:\n", ch);
            TextBuffer buf = new TextBuffer();
            for (AFFECT_DATA paf = ch.affected, paf_last = null; paf != null; paf = paf.next) {
                if (paf_last != null && paf.type == paf_last.type) {
                    if (ch.level >= 20) {
                        buf.sprintf("                      ");
                    } else {
                        continue;
                    }
                } else {
                    buf.sprintf("{rSpell{x: {Y%-15s{x", paf.type.name);
                }
                send_to_char(buf, ch);

                if (ch.level >= 20) {
                    buf.sprintf(": modifies {m%s{x by {m%d{x ", affect_loc_name(paf.location), paf.modifier);
                    send_to_char(buf, ch);
                    if (paf.duration == -1 || paf.duration == -2) {
                        buf.sprintf("{cpermanently{x");
                    } else {
                        buf.sprintf("for {m%d{x hours", paf.duration);
                    }
                    send_to_char(buf, ch);
                }
                send_to_char("\n", ch);
                paf_last = paf;
            }
        } else {
            send_to_char("You are not affected by any spells.\n", ch);
        }

    }


    static void do_lion_call(CHAR_DATA ch) {
        CHAR_DATA gch;
        CHAR_DATA bear;
        CHAR_DATA bear2;
        int i;

        if (skill_failure_check(ch, gsn_lion_call, true, 0, null)) {
            return;
        }

        send_to_char("You call for lions help you.\n", ch);
        act("$n shouts a lion call.", ch, null, null, TO_ROOM);

        if (is_affected(ch, gsn_lion_call)) {
            send_to_char("You cannot summon the strength to handle more lions right now.\n", ch);
            return;
        }
        for (gch = char_list; gch != null; gch = gch.next) {
            if (IS_NPC(gch) && IS_AFFECTED(gch, AFF_CHARM) && gch.master == ch &&
                    gch.pIndexData.vnum == MOB_VNUM_LION) {
                send_to_char("What's wrong with the lion you've got?", ch);
                return;
            }
        }

        if (ch.in_room != null && IS_SET(ch.in_room.room_flags, ROOM_NO_MOB)) {
            send_to_char("No lions can listen you.\n", ch);
            return;
        }

        if (number_percent() > get_skill(ch, gsn_lion_call)) {
            send_to_char("No lions listen you.\n", ch);
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
            send_to_char("No lions come to your rescue.\n", ch);
            return;
        }

        if (ch.mana < 125) {
            send_to_char("You don't have enough mana to shout a lion call.\n", ch);
            return;
        }
        ch.mana -= 125;

        bear = create_mobile(get_mob_index(MOB_VNUM_LION));

        for (i = 0; i < MAX_STATS; i++) {
            bear.perm_stat[i] = UMIN(25, 2 * ch.perm_stat[i]);
        }

        bear.max_hit = IS_NPC(ch) ? ch.max_hit : ch.pcdata.perm_hit;
        bear.hit = bear.max_hit;
        bear.max_mana = IS_NPC(ch) ? ch.max_mana : ch.pcdata.perm_mana;
        bear.mana = bear.max_mana;
        bear.alignment = ch.alignment;
        bear.level = UMIN(100, ch.level - 2);
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
        send_to_char("Two lions come to your rescue!\n", ch);
        act("Two bears come to $n's rescue!", ch, null, null, TO_ROOM);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = gsn_lion_call;
        af.level = ch.level;
        af.duration = 24;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

    }

/* object condition aliases */

    static String get_cond_alias(OBJ_DATA obj) {
        String stat;
        int istat;

        istat = obj.condition;

        if (istat > 99) {
            stat = "excellent";
        } else if (istat >= 80) {
            stat = "good";
        } else if (istat >= 60) {
            stat = "fine";
        } else if (istat >= 40) {
            stat = "average";
        } else if (istat >= 20) {
            stat = "poor";
        } else {
            stat = "fragile";
        }

        return stat;
    }

/* room affects */

    static void do_raffects(CHAR_DATA ch) {
        if (ch.in_room.affected != null) {
            TextBuffer buf = new TextBuffer();
            send_to_char("The room is affected by the following spells:\n", ch);
            for (AFFECT_DATA paf = ch.in_room.affected, paf_last = null; paf != null; paf = paf.next) {
                if (paf_last != null && paf.type == paf_last.type) {
                    if (ch.level >= 20) {
                        buf.sprintf("                      ");
                    } else {
                        continue;
                    }
                } else {
                    buf.sprintf("Spell: %-15s", paf.type.name);
                }

                send_to_char(buf, ch);

                if (ch.level >= 20) {
                    buf.sprintf(
                            ": modifies %s by %d ",
                            raffect_loc_name(paf.location),
                            paf.modifier);
                    send_to_char(buf, ch);
                    if (paf.duration == -1 || paf.duration == -2) {
                        buf.sprintf("permanently");
                    } else {
                        buf.sprintf("for %d hours", paf.duration);
                    }
                    send_to_char(buf, ch);
                }
                send_to_char("\n", ch);
                paf_last = paf;
            }
        } else {
            send_to_char("The room is not affected by any spells.\n", ch);
        }

    }

/* new practice */

    static void do_pracnew(CHAR_DATA ch, String argument) {
        if (IS_NPC(ch)) {
            return;
        }

        if (argument.length() == 0) {
            int col;

            col = 0;
            StringBuilder buf2 = new StringBuilder();
            TextBuffer buf = new TextBuffer();
            for (Skill sn : Skill.skills) {
                if (sn.name == null) {
                    break;
                }
                if (skill_failure_nomessage(ch, sn, 0) != 0) {
                    continue;
                }

                buf.sprintf("%-18s %3d%%  ", sn.name, ch.pcdata.learned[sn.ordinal()]);
                buf2.append(buf.toString());
                if (++col % 3 == 0) {
                    buf2.append("\n");
                }
            }

            if (col % 3 != 0) {
                buf2.append("\n");
            }

            buf.sprintf("You have {w%d{x practice sessions left.\n", ch.practice);
            buf2.append(buf.toString());
            page_to_char(buf2, ch);
        } else {
            CHAR_DATA mob;
            int adept;

            if (!IS_AWAKE(ch)) {
                send_to_char("In your dreams, or what?\n", ch);
                return;
            }

            if (ch.practice <= 0) {
                send_to_char("You have no practice sessions left.\n", ch);
                return;
            }
            Skill sn = find_spell(ch, argument);
            if (sn == null || skill_failure_nomessage(ch, sn, 0) != 0) {
                send_to_char("You can't practice that.\n", ch);
                return;
            }

            if (!str_cmp("vampire", sn.name)) {
                send_to_char("You can't practice that, only available at questor.\n", ch);
                return;
            }

            for (mob = ch.in_room.people; mob != null; mob = mob.next_in_room) {
                if (IS_NPC(mob) && IS_SET(mob.act, ACT_PRACTICE)) {
                    if (sn.cabal == CABAL_NONE) {
                        if ((mob.pIndexData.practicer == 0 &&
                                (sn.group == GROUP_NONE
                                        || sn.group == GROUP_CREATION
                                        || sn.group == GROUP_HARMFUL
                                        || sn.group == GROUP_PROTECTIVE
                                        || sn.group == GROUP_DETECTION
                                        || sn.group == GROUP_WEATHER))
                                || (mob.pIndexData.practicer & (1 << prac_table[sn.group].number)) != 0) {
                            break;
                        }
                    } else {
                        if (ch.cabal == mob.cabal) {
                            break;
                        }
                    }
                }
            }

            if (mob == null) {
                send_to_char("You can't do that here. USE glist ,slook for more info.\n", ch);
                return;
            }

            adept = IS_NPC(ch) ? 100 : ch.clazz.skill_adept;

            if (ch.pcdata.learned[sn.ordinal()] >= adept) {
                TextBuffer buf = new TextBuffer();
                buf.sprintf("You are already learned at %s.\n", sn.name);
                send_to_char(buf, ch);
            } else {
                if (ch.pcdata.learned[sn.ordinal()] == 0) {
                    ch.pcdata.learned[sn.ordinal()] = 1;
                }
                ch.practice--;
                ch.pcdata.learned[sn.ordinal()] += int_app[get_curr_stat(ch, STAT_INT)].learn / UMAX(sn.rating[ch.clazz.id], 1);
                if (ch.pcdata.learned[sn.ordinal()] < adept) {
                    act("You practice $T.", ch, null, sn.name, TO_CHAR);
                    act("$n practices $T.", ch, null, sn.name, TO_ROOM);
                } else {
                    ch.pcdata.learned[sn.ordinal()] = adept;
                    act("You are now learned at $T.", ch, null, sn.name, TO_CHAR);
                    act("$n is now learned at $T.", ch, null, sn.name, TO_ROOM);
                }
            }
        }
    }

/*
* New 'who_col' command by chronos
*/

    static void do_who(CHAR_DATA ch, String argument) {
        DESCRIPTOR_DATA d;
        int iLevelLower = 0;
        int iLevelUpper = MAX_LEVEL;
        boolean fClassRestrict = false;
        boolean fRaceRestrict = false;
        boolean fImmortalOnly = false;
        boolean fPKRestrict = false;
        boolean fRulerRestrict = false;
        boolean fChaosRestrict = false;
        boolean fShalafiRestrict = false;
        boolean fInvaderRestrict = false;
        boolean fBattleRestrict = false;
        boolean fKnightRestrict = false;
        boolean fLionsRestrict = false;
        boolean fTattoo = false;

        boolean rgfClass[] = new boolean[MAX_CLASS];
        Set<Race> rgfRaces = null;

        /*
        * Parse arguments.
        */
        int nNumber = 0;
        int vnum = 0;
        for (; ; ) {
            StringBuilder argb = new StringBuilder();
            argument = one_argument(argument, argb);
            if (argb.length() == 0) {
                break;
            }

            String arg = argb.toString();
            if (!str_cmp(arg, "pk")) {
                fPKRestrict = true;
                break;
            }

            if (!str_cmp(arg, "ruler")) {
                if (ch.cabal != CABAL_RULER && !IS_IMMORTAL(ch)) {
                    send_to_char("You are not in that cabal!\n", ch);
                    return;
                } else {
                    fRulerRestrict = true;
                    break;
                }
            }
            if (!str_cmp(arg, "shalafi")) {
                if (ch.cabal != CABAL_SHALAFI && !IS_IMMORTAL(ch)) {
                    send_to_char("You are not in that cabal!\n", ch);
                    return;
                } else {
                    fShalafiRestrict = true;
                    break;
                }
            }
            if (!str_cmp(arg, "battle")) {
                if (ch.cabal != CABAL_BATTLE && !IS_IMMORTAL(ch)) {
                    send_to_char("You are not in that cabal!\n", ch);
                    return;
                } else {
                    fBattleRestrict = true;
                    break;
                }
            }
            if (!str_cmp(arg, "invader")) {
                if (ch.cabal != CABAL_INVADER && !IS_IMMORTAL(ch)) {
                    send_to_char("You are not in that cabal!\n", ch);
                    return;
                } else {
                    fInvaderRestrict = true;
                    break;
                }
            }
            if (!str_cmp(arg, "chaos")) {
                if (ch.cabal != CABAL_CHAOS && !IS_IMMORTAL(ch)) {
                    send_to_char("You are not in that cabal!\n", ch);
                    return;
                } else {
                    fChaosRestrict = true;
                    break;
                }
            }
            if (!str_cmp(arg, "knight")) {
                if (ch.cabal != CABAL_KNIGHT && !IS_IMMORTAL(ch)) {
                    send_to_char("You are not in that cabal!\n", ch);
                    return;
                } else {
                    fKnightRestrict = true;
                    break;
                }
            }
            if (!str_cmp(arg, "lions")) {
                if (ch.cabal != CABAL_LIONS && !IS_IMMORTAL(ch)) {
                    send_to_char("You are not in that cabal!\n", ch);
                    return;
                } else {
                    fLionsRestrict = true;
                    break;
                }
            }

            if (!str_cmp(arg, "tattoo")) {
                if (get_eq_char(ch, WEAR_TATTOO) == null) {
                    send_to_char("You haven't got a tattoo yetl!\n", ch);
                    return;
                } else {
                    fTattoo = true;
                    vnum = get_eq_char(ch, WEAR_TATTOO).pIndexData.vnum;
                    break;
                }
            }


            if (is_number(arg) && IS_IMMORTAL(ch)) {
                switch (++nNumber) {
                    case 1:
                        iLevelLower = atoi(arg);
                        break;
                    case 2:
                        iLevelUpper = atoi(arg);
                        break;
                    default:
                        send_to_char("This function of who is for immortals.\n", ch);
                        return;
                }
            } else {

                /*
                * Look for classes to turn on.
                */
                if (arg.charAt(0) == 'i') {
                    fImmortalOnly = true;
                } else {
                    Clazz iClass = Clazz.lookupClass(arg, false);
                    if (iClass == null || !IS_IMMORTAL(ch)) {
                        Race race = Race.lookupRace(arg);
                        if (race == null || race.pcRace == null) {
                            send_to_char("That's not a valid race.\n", ch);
                            return;
                        } else {
                            fRaceRestrict = true;
                            if (rgfRaces == null) {
                                rgfRaces = new HashSet<Race>();
                            }
                            rgfRaces.add(race);
                        }
                    } else {
                        fClassRestrict = true;
                        rgfClass[iClass.id] = true;
                    }
                }
            }
        }

        /*
         * Now show matching chars.
         */
        int nMatch = 0;
        TextBuffer buf = new TextBuffer();
        StringBuilder output = new StringBuilder();
        for (d = descriptor_list; d != null; d = d.next) {
            CHAR_DATA wch;

            /*
            * Check for match against restrictions.
            * Don't use trust as that exposes trusted mortals.
            */
            if (d.connected != CON_PLAYING || !can_see(ch, d.character)) {
                continue;
            }

            if (d.connected != CON_PLAYING || (IS_VAMPIRE(d.character) && !IS_IMMORTAL(ch) && (ch != d.character))) {
                continue;
            }

            wch = (d.original != null) ? d.original : d.character;
            if (!can_see(ch, wch)) /* can't see switched wizi imms */ {
                continue;
            }

            if (wch.level < iLevelLower
                    || wch.level > iLevelUpper
                    || (fImmortalOnly && wch.level < LEVEL_HERO)
                    || (fClassRestrict && !rgfClass[wch.clazz.id])
                    || (fRaceRestrict && !rgfRaces.contains(wch.race))
                    || (fPKRestrict && is_safe_nomessage(ch, wch))
                    || (fTattoo && (vnum == get_eq_char(wch, WEAR_TATTOO).pIndexData.vnum))
                    || (fRulerRestrict && wch.cabal != CABAL_RULER)
                    || (fChaosRestrict && wch.cabal != CABAL_CHAOS)
                    || (fBattleRestrict && wch.cabal != CABAL_BATTLE)
                    || (fInvaderRestrict && wch.cabal != CABAL_INVADER)
                    || (fShalafiRestrict && wch.cabal != CABAL_SHALAFI)
                    || (fKnightRestrict && wch.cabal != CABAL_KNIGHT)
                    || (fLionsRestrict && wch.cabal != CABAL_LIONS)) {
                continue;
            }

            nMatch++;

            /*
            * Figure out what to print for class.
            */
            String clazz;
            switch (wch.level) {
                default:
                    clazz = wch.clazz.who_name;
                    break;
                case MAX_LEVEL:
                    clazz = "IMP";
                    break;
                case MAX_LEVEL - 1:
                    clazz = "CRE";
                    break;
                case MAX_LEVEL - 2:
                    clazz = "SUP";
                    break;
                case MAX_LEVEL - 3:
                    clazz = "DEI";
                    break;
                case MAX_LEVEL - 4:
                    clazz = "GOD";
                    break;
                case MAX_LEVEL - 5:
                    clazz = "IMM";
                    break;
                case MAX_LEVEL - 6:
                    clazz = "DEM";
                    break;
                case MAX_LEVEL - 7:
                    clazz = "ANG";
                    break;
                case MAX_LEVEL - 8:
                    clazz = "AVA";
                    break;
            }

            /* for cabals
            if ((wch.cabal && (wch.cabal == ch.cabal ||
                       IS_TRUSTED(ch,LEVEL_IMMORTAL))) ||
                                   wch.level >= LEVEL_HERO)
            */
            String cabalbuf = "";
            if ((wch.cabal != 0 && ch.cabal == wch.cabal) || IS_IMMORTAL(ch)
                    || (IS_SET(wch.act, PLR_CANINDUCT) && wch.cabal == 1)
                    || wch.cabal == CABAL_HUNTER
                    || (wch.cabal == CABAL_RULER
                    && is_equiped_n_char(wch, OBJ_VNUM_RULER_BADGE, WEAR_NECK))) {
                cabalbuf = "{c" + cabal_table[wch.cabal].short_name + "{x";
            }
            if (wch.cabal == 0) {
                cabalbuf = "";
            }

            String pk_buf = "";
            if (!((ch == wch && ch.level < PK_MIN_LEVEL) || is_safe_nomessage(ch, wch))) {
                pk_buf = "{r(PK){x";
            }

            String act_buf = IS_SET(ch.act, PLR_WANTED) ? "{W(WANTED){x " : "";
            String titlebuf = IS_NPC(wch) ? "Believer of Chronos." : wch.pcdata.title;
            /*
            * Format it up.
            */
            TextBuffer level_buf = new TextBuffer();
            level_buf.sprintf("{c%2d{x", wch.level);
            String classbuf = "{Y" + clazz + "{x";

            if (IS_TRUSTED(ch, LEVEL_IMMORTAL) || ch == wch || wch.level >= LEVEL_HERO)

            {
                buf.sprintf("[%2d %s %s] %s %s %s %s %s\n",
                        wch.level,
                        wch.race.pcRace != null ? wch.race.pcRace.who_name : "     ",
                        classbuf,
                        pk_buf,
                        cabalbuf,
                        act_buf,
                        wch.name,
                        titlebuf);
            } else
/*    buf.sprintf( "[%s %s %s] %s%s%s%s%s\n",    */ {
                buf.sprintf("[%s %s    ] %s %s %s %s %s\n",
                        (get_curr_stat(wch, STAT_CHA) < 18) ? level_buf : "  ",
                        wch.race.pcRace != null ? wch.race.pcRace.who_name : "     ",
/*      classbuf,   */
                        pk_buf,
                        cabalbuf,
                        act_buf,
                        wch.name,
                        titlebuf);
            }

            output.append(buf);
        }

        int count = 0;
        for (d = descriptor_list; d != null; d = d.next) {
            if (d.connected == CON_PLAYING) {
                count++;
            }
        }

        max_on = UMAX(count, max_on);
        buf.sprintf("\nPlayers found: %d. Most so far today: %d.\n", nMatch, max_on);
        output.append(buf);
        page_to_char(output, ch);
    }


    static void do_camp(CHAR_DATA ch) {

        if (skill_failure_check(ch, gsn_camp, false, 0, null)) {
            return;
        }

        if (is_affected(ch, gsn_camp)) {
            send_to_char("You don't have enough power to handle more camp areas.\n", ch);
            return;
        }

        if (number_percent() > get_skill(ch, gsn_camp)) {
            send_to_char("You failed to make your camp.\n", ch);
            check_improve(ch, gsn_camp, true, 4);
            return;
        }

        if (IS_SET(ch.in_room.room_flags, ROOM_SAFE) ||
                IS_SET(ch.in_room.room_flags, ROOM_PRIVATE) ||
                IS_SET(ch.in_room.room_flags, ROOM_SOLITARY) ||
                (ch.in_room.sector_type != SECT_FIELD &&
                        ch.in_room.sector_type != SECT_FOREST &&
                        ch.in_room.sector_type != SECT_MOUNTAIN &&
                        ch.in_room.sector_type != SECT_HILLS)) {
            send_to_char("There are not enough leaves to camp here.\n", ch);
            return;
        }

        if (ch.mana < 150) {
            send_to_char("You don't have enough mana to make a camp.\n", ch);
            return;
        }

        check_improve(ch, gsn_camp, true, 4);
        ch.mana -= 150;

        WAIT_STATE(ch, gsn_camp.beats);

        send_to_char("You succeeded to make your camp.\n", ch);
        act("$n succeeded to make $s camp.", ch, null, null, TO_ROOM);

        AFFECT_DATA af = new AFFECT_DATA();
        af.where = TO_AFFECTS;
        af.type = gsn_camp;
        af.level = ch.level;
        af.duration = 12;
        af.bitvector = 0;
        af.modifier = 0;
        af.location = APPLY_NONE;
        affect_to_char(ch, af);

        AFFECT_DATA af2 = new AFFECT_DATA();
        af2.where = TO_ROOM_CONST;
        af2.type = gsn_camp;
        af2.level = ch.level;
        af2.duration = ch.level / 20;
        af2.bitvector = 0;
        af2.modifier = 2 * ch.level;
        af2.location = APPLY_ROOM_HEAL;
        affect_to_room(ch.in_room, af2);

        af2.modifier = ch.level;
        af2.location = APPLY_ROOM_MANA;
        affect_to_room(ch.in_room, af2);

    }


    static void do_demand(CHAR_DATA ch, String argument) {
        int chance;
        StringBuilder arg1b = new StringBuilder();
        StringBuilder arg2b = new StringBuilder();
        argument = one_argument(argument, arg1b);
        one_argument(argument, arg2b);

        if (IS_NPC(ch)) {
            return;
        }

        if (skill_failure_check(ch, gsn_demand, false, 0, "You can't do that.\n")) {
            return;
        }

        if (arg1b.length() == 0 || arg2b.length() == 0) {
            send_to_char("Demand what from whom?\n", ch);
            return;
        }
        String arg2 = arg2b.toString();
        CHAR_DATA victim = get_char_room(ch, arg2);
        if (victim == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (!IS_NPC(victim)) {
            send_to_char("Why don't you just want that directly from the player?\n", ch);
            return;
        }

        WAIT_STATE(ch, PULSE_VIOLENCE);

        chance = IS_EVIL(victim) ? 10 : IS_GOOD(victim) ? -5 : 0;
        chance += (get_curr_stat(ch, STAT_CHA) - 15) * 10;
        chance += ch.level - victim.level;

        if (victim.level >= ch.level + 10 || victim.level >= ch.level * 2) {
            chance = 0;
        }

        if (number_percent() > chance) {
            do_say(victim, "I'm not about to give you anything!");
            do_murder(victim, ch.name);
            return;
        }

        String arg1 = arg1b.toString();
        OBJ_DATA obj;
        if (((obj = get_obj_carry(victim, arg1)) == null
                && (obj = get_obj_wear(victim, arg1)) == null)
                || IS_SET(obj.extra_flags, ITEM_INVENTORY)) {
            do_say(victim, "Sorry, I don't have that.");
            return;
        }


        if (obj.wear_loc != WEAR_NONE) {
            unequip_char(victim, obj);
        }

        if (!can_drop_obj(ch, obj)) {
            do_say(victim,
                    "It's cursed so, I can't let go of it. Forgive me, my master");
            return;
        }

        if (ch.carry_number + get_obj_number(obj) > can_carry_n(ch)) {
            send_to_char("Your hands are full.\n", ch);
            return;
        }

        if (ch.carry_weight + get_obj_weight(obj) > can_carry_w(ch)) {
            send_to_char("You can't carry that much weight.\n", ch);
            return;
        }

        if (!can_see_obj(ch, obj)) {
            act("You don't see that.", ch, null, victim, TO_CHAR);
            return;
        }

        obj_from_char(obj);
        obj_to_char(obj, ch);
        act("$n demands $p from $N.", ch, obj, victim, TO_NOTVICT);
        act("You demand $p from $N.", ch, obj, victim, TO_CHAR);
        act("$n demands $p from you.", ch, obj, victim, TO_VICT);


        if (IS_SET(obj.progtypes, OPROG_GIVE)) {
            obj.pIndexData.oprogs.give_prog.run(obj, ch, victim);
        }

        if (IS_SET(victim.progtypes, MPROG_GIVE)) {
            victim.pIndexData.mprogs.give_prog.run(victim, ch, obj);
        }

        send_to_char("Your power makes all around the world shivering.\n", ch);

    }


    static void do_control(CHAR_DATA ch, String argument) {
        CHAR_DATA victim;
        int chance;
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (skill_failure_check(ch, gsn_control_animal, true, 0, null)) {
            return;
        }

        if (arg.length() == 0) {
            send_to_char("Charm what?\n", ch);
            return;
        }

        if ((victim = get_char_room(ch, arg.toString())) == null) {
            send_to_char("They aren't here.\n", ch);
            return;
        }

        if (ORG_RACE(victim).pcRace != null) {
            send_to_char("You should try this on monsters?\n", ch);
            return;
        }

        if (is_safe(ch, victim)) {
            return;
        }

        if (count_charmed(ch) != 0) {
            return;
        }

        WAIT_STATE(ch, PULSE_VIOLENCE);

        chance = get_skill(ch, gsn_control_animal);

        chance += (get_curr_stat(ch, STAT_CHA) - 20) * 5;
        chance += (ch.level - victim.level) * 3;
        chance +=
                (get_curr_stat(ch, STAT_INT) - get_curr_stat(victim, STAT_INT)) * 5;

        if (IS_AFFECTED(victim, AFF_CHARM)
                || IS_AFFECTED(ch, AFF_CHARM)
                || number_percent() > chance
                || ch.level < (victim.level + 2)
                || IS_SET(victim.imm_flags, IMM_CHARM)
                || (IS_NPC(victim) && victim.pIndexData.pShop != null)) {
            check_improve(ch, gsn_control_animal, false, 2);
            do_say(victim, "I'm not about to follow you!");
            do_murder(victim, ch.name);
            return;
        }

        check_improve(ch, gsn_control_animal, true, 2);

        if (victim.master != null) {
            stop_follower(victim);
        }
        victim.affected_by = SET_BIT(victim.affected_by, AFF_CHARM);
        victim.master = victim.leader = ch;

        act("Isn't $n just so nice?", ch, null, victim, TO_VICT);
        if (ch != victim) {
            act("$N looks at you with adoring eyes.", ch, null, victim, TO_CHAR);
        }

    }


    static void do_nscore(CHAR_DATA ch) {
        int ekle = 0;
        TextBuffer buf = new TextBuffer();
        String buf2 = "";
        buf.sprintf("{G\n\n      /~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~/~~\\\n");

        send_to_char(buf, ch);
        String titlebuf = IS_NPC(ch) ? "Believer of Chronos." : ch.pcdata.title;

        buf.sprintf("     {G|   {R%-12s{w%-33s {y%3d{x years old   {G|{g____|{G\n", ch.name, titlebuf, get_age(ch));
        send_to_char(buf, ch);
        buf.sprintf("     |{C+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+{G|\n");
        send_to_char(buf, ch);
        buf.sprintf("     | {RLevel:{x  %3d          {C|  {RStr:{x  %2d(%2d)  {C| {RReligion  :{x  %-10s {G|\n",
                ch.level, ch.perm_stat[STAT_STR], get_curr_stat(ch, STAT_STR), religion_table[ch.religion].leader);
        send_to_char(buf, ch);
        buf.sprintf("     | {RRace :{x  %-11s  {C|  {RInt:{x  %2d(%2d)  {C| {RPractice  :{x   %3d       {G|\n",
                ORG_RACE(ch).name, ch.perm_stat[STAT_INT], get_curr_stat(ch, STAT_INT), ch.practice);
        send_to_char(buf, ch);

        buf.sprintf("     | {RSex  :{x  %-11s  {C|  {RWis:{x  %2d(%2d)  {C| {RTrain     :{x   %3d       {G|\n",
                ch.sex == 0 ? "sexless" : ch.sex == 1 ? "male" : "female",
                ch.perm_stat[STAT_WIS], get_curr_stat(ch, STAT_WIS), ch.train);
        send_to_char(buf, ch);

        buf.sprintf("     | {RClass:{x  %-12s {C|  {RDex:{x  %2d(%2d)  {C| {RQuest Pnts:{x  %4d       {G|\n",
                IS_NPC(ch) ? "mobile" : ch.clazz.name, ch.perm_stat[STAT_DEX],
                get_curr_stat(ch, STAT_DEX), IS_NPC(ch) ? 0 : ch.pcdata.questpoints);
        send_to_char(buf, ch);

        buf.sprintf("     | {RHome :{x  %-12s {C|  {RCon:{x  %2d(%2d)  {C| {RQuest Time:{x   %3d       {G|\n",
                IS_NPC(ch) ? "Midgaard" : hometown_table[ch.hometown].name,
                ch.perm_stat[STAT_CON], get_curr_stat(ch, STAT_CON),
                IS_NPC(ch) ? 0 : ch.pcdata.nextquest);
        send_to_char(buf, ch);
        buf.sprintf("     | {REthos:{x  %-11s  {C|  {RCha:{x  %2d(%2d)  {C| {R%s     :{x   %3d       {G|\n",
                IS_NPC(ch) ? "mobile" : ethos_table[ch.ethos].name,
                ch.perm_stat[STAT_CHA], get_curr_stat(ch, STAT_CHA),
                ch.clazz == Clazz.SAMURAI ? "Death" : "Wimpy", ch.clazz == Clazz.SAMURAI ? ch.pcdata.death : ch.wimpy);
        send_to_char(buf, ch);
        switch (ch.position) {
            case POS_DEAD:
                buf2 = "You are DEAD!!";
                break;
            case POS_MORTAL:
                buf2 = "You're fatally wounded.";
                break;
            case POS_INCAP:
                buf2 = "You are incapacitated.";
                break;
            case POS_STUNNED:
                buf2 = "You are stunned.";
                break;
            case POS_SLEEPING:
                buf2 = "You are sleeping.";
                break;
            case POS_RESTING:
                buf2 = "You are resting.";
                break;
            case POS_STANDING:
                buf2 = "You are standing.";
                break;
            case POS_FIGHTING:
                buf2 = "You are fighting.";
                break;
        }

        buf.sprintf("     | {RAlign:{x  %-11s  {C|                | {y%-23s {G|\n",
                IS_GOOD(ch) ? "good" : IS_EVIL(ch) ? "evil" : "neutral", buf2);
        send_to_char(buf, ch);

        buf.sprintf("     |{C+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+{G|\n");
        send_to_char(buf, ch);

        if (ch.guarding != null) {
            ekle = 1;
            buf.sprintf("     | {WYou are guarding:{x %-10s                                    {G|\n", ch.guarding.name);
            send_to_char(buf, ch);
        }

        if (ch.guarded_by != null) {
            ekle = 1;
            buf.sprintf("     | {WYou are guarded by:{x %-10s                                  {G|\n", ch.guarded_by.name);
            send_to_char(buf, ch);
        }

        if (!IS_NPC(ch) && ch.pcdata.condition[COND_DRUNK] > 10) {
            ekle = 1;
            buf.sprintf("     | {WYou are drunk.                                                  {G|\n");
            send_to_char(buf, ch);
        }

        if (!IS_NPC(ch) && ch.pcdata.condition[COND_THIRST] <= 0) {
            ekle = 1;
            buf.sprintf("     | {WYou are thirsty.                                                {G|\n");
            send_to_char(buf, ch);
        }
/*    if ( !IS_NPC(ch) && ch.pcdata.condition[COND_FULL]   ==  0 ) */
        if (!IS_NPC(ch) && ch.pcdata.condition[COND_HUNGER] <= 0) {
            ekle = 1;
            buf.sprintf("     | {WYou are hungry.                                                 {G|\n");
            send_to_char(buf, ch);
        }

        if (!IS_NPC(ch) && ch.pcdata.condition[COND_BLOODLUST] <= 0) {
            ekle = 1;
            buf.sprintf("     | {WYou are hungry for blood.                                       {G|\n");
            send_to_char(buf, ch);
        }

        if (!IS_NPC(ch) && ch.pcdata.condition[COND_DESIRE] <= 0) {
            ekle = 1;
            buf.sprintf("     | {WYou are desiring your home.                                     {G|\n");
            send_to_char(buf, ch);
        }

        if (ch.last_fight_time != -1 && !IS_IMMORTAL(ch) &&
                (current_time - ch.last_fight_time) < FIGHT_DELAY_TIME) {
            ekle = 1;
            buf.sprintf("     | {WYour adrenalin is gushing!                                      {G|\n");
            send_to_char(buf, ch);
        }

        if (ekle != 0) {
            buf.sprintf("     |{c+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+{G|\n");
            send_to_char(buf, ch);
        }

        buf.sprintf("     | {RItems Carried :{x     %3d/%-4d        {RArmor vs magic  :{x %4d      {G|\n",
                ch.carry_number, can_carry_n(ch), GET_AC(ch, AC_EXOTIC));
        send_to_char(buf, ch);

        buf.sprintf("     | {RWeight Carried:{x  %6d/%-8d    {RArmor vs bash   :{x %4d      {G|\n",
                get_carry_weight(ch), can_carry_w(ch), GET_AC(ch, AC_BASH));
        send_to_char(buf, ch);

        buf.sprintf("     | {RGold          :{x   %-10d        {RArmor vs pierce :{x %4d      {G|\n",
                ch.gold, GET_AC(ch, AC_PIERCE));
        send_to_char(buf, ch);

        buf.sprintf("     | {RSilver        :{x   %-10d        {RArmor vs slash  :{x %4d      {G|\n",
                ch.silver, GET_AC(ch, AC_SLASH));
        send_to_char(buf, ch);

        buf.sprintf("     | {RCurrent exp   :{x   %-6d            {RSaves vs Spell  :{x %4d      {G|\n",
                ch.exp, ch.saving_throw);
        send_to_char(buf, ch);

        buf.sprintf("     | {RExp to level  :{x   %-6d                                        {G|\n",
                IS_NPC(ch) ? 0 : exp_to_level(ch, ch.pcdata.points));
        send_to_char(buf, ch);

        buf.sprintf("     |                                     {RHitP:{x %5d / %5d         {G|\n",
                ch.hit, ch.max_hit);
        send_to_char(buf, ch);
        buf.sprintf("     | {RHitroll       :{x   %-3d               {RMana:{x %5d / %5d         {G|\n",
                GET_HITROLL(ch), ch.mana, ch.max_mana);
        send_to_char(buf, ch);
        buf.sprintf("     | {RDamroll       :{x   %-3d               {RMove:{x %5d / %5d         {G|\n",
                GET_DAMROLL(ch), ch.move, ch.max_move);
        send_to_char(buf, ch);
        buf.sprintf("  /~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~/   |\n");
        send_to_char(buf, ch);
        buf.sprintf("  \\________________________________________________________________\\__/{x\n");
        send_to_char(buf, ch);
        if (ch.affected != null && IS_SET(ch.comm, COMM_SHOW_AFFECTS)) {
            do_affects(ch);
        }
    }


    static void do_make_arrow(CHAR_DATA ch, String argument) {
        OBJ_DATA arrow;
        String str;

        if (skill_failure_check(ch, gsn_make_arrow, false, 0, "You don't know how to make arrows.\n")) {
            return;
        }

        if (ch.in_room.sector_type != SECT_FIELD &&
                ch.in_room.sector_type != SECT_FOREST &&
                ch.in_room.sector_type != SECT_HILLS) {
            send_to_char("You couldn't find enough wood.\n", ch);
            return;
        }

        int mana = gsn_make_arrow.min_mana;
        int wait = gsn_make_arrow.beats;

        StringBuilder argb = new StringBuilder();
        one_argument(argument, argb);
        String arg = argb.toString();
        Skill color;
        if (arg.length() == 0) {
            color = null;
        } else if (!str_prefix(arg, "green")) {
            color = gsn_green_arrow;
        } else if (!str_prefix(arg, "red")) {
            color = gsn_red_arrow;
        } else if (!str_prefix(arg, "white")) {
            color = gsn_white_arrow;
        } else if (!str_prefix(arg, "blue")) {
            color = gsn_blue_arrow;
        } else {
            send_to_char("You don't know how to make that kind of arrow.\n", ch);
            return;
        }

        if (color != null) {
            mana += color.min_mana;
            wait += color.beats;
        }

        if (ch.mana < mana) {
            send_to_char("You don't have enough energy to make that kind of arrows.\n", ch);
            return;
        }
        ch.mana -= mana;
        WAIT_STATE(ch, wait);

        send_to_char("You start to make arrows!\n", ch);
        act("$n starts to make arrows!", ch, null, null, TO_ROOM);
        TextBuffer buf = new TextBuffer();
        for (int count = 0; count < (ch.level / 5); count++) {
            if (number_percent() > get_skill(ch, gsn_make_arrow)) {
                send_to_char("You failed to make the arrow, and broke it.\n", ch);
                check_improve(ch, gsn_make_arrow, false, 3);
                continue;
            }
            send_to_char("You successfully make an arrow.\n", ch);
            check_improve(ch, gsn_make_arrow, true, 3);

            arrow = create_object(get_obj_index(OBJ_VNUM_RANGER_ARROW), ch.level);
            arrow.level = ch.level;
            arrow.value[1] = ch.level / 10;
            arrow.value[2] = ch.level / 10;

            AFFECT_DATA tohit = new AFFECT_DATA();
            tohit.where = TO_OBJECT;
            tohit.type = gsn_make_arrow;
            tohit.level = ch.level;
            tohit.duration = -1;
            tohit.location = APPLY_HITROLL;
            tohit.modifier = ch.level / 10;
            tohit.bitvector = 0;
            affect_to_obj(arrow, tohit);

            AFFECT_DATA todam = new AFFECT_DATA();
            todam.where = TO_OBJECT;
            todam.type = gsn_make_arrow;
            todam.level = ch.level;
            todam.duration = -1;
            todam.location = APPLY_DAMROLL;
            todam.modifier = ch.level / 10;
            todam.bitvector = 0;
            affect_to_obj(arrow, todam);

            AFFECT_DATA saf = null;
            if (color != null) {
                saf = new AFFECT_DATA();
                saf.where = TO_WEAPON;
                saf.type = color;
                saf.level = ch.level;
                saf.duration = -1;
                saf.location = 0;
                saf.modifier = 0;

                if (color == gsn_green_arrow) {
                    saf.bitvector = WEAPON_POISON;
                    str = "green";
                } else if (color == gsn_red_arrow) {
                    saf.bitvector = WEAPON_FLAMING;
                    str = "red";
                } else if (color == gsn_white_arrow) {
                    saf.bitvector = WEAPON_FROST;
                    str = "white";
                } else {
                    saf.bitvector = WEAPON_SHOCKING;
                    str = "blue";
                }
            } else {
                str = "wooden";
            }

            buf.sprintf(arrow.name, str);
            arrow.name = buf.toString();

            buf.sprintf(arrow.short_descr, str);
            arrow.short_descr = buf.toString();

            buf.sprintf(arrow.description, str);
            arrow.description = buf.toString();

            if (color != null) {
                affect_to_obj(arrow, saf);
            }
            obj_to_char(arrow, ch);
        }
    }


    static void do_make_bow(CHAR_DATA ch) {
        OBJ_DATA bow;
        int mana, wait;

        if (skill_failure_check(ch, gsn_make_bow, false, 0, "You don't know how to make bows.\n")) {
            return;
        }

        if (ch.in_room.sector_type != SECT_FIELD &&
                ch.in_room.sector_type != SECT_FOREST &&
                ch.in_room.sector_type != SECT_HILLS) {
            send_to_char("You couldn't find enough wood.\n", ch);
            return;
        }

        mana = gsn_make_bow.min_mana;
        wait = gsn_make_bow.beats;

        if (ch.mana < mana) {
            send_to_char("You don't have enough energy to make a bow.\n", ch);
            return;
        }
        ch.mana -= mana;
        WAIT_STATE(ch, wait);

        if (number_percent() > get_skill(ch, gsn_make_bow)) {
            send_to_char("You failed to make the bow, and broke it.\n", ch);
            check_improve(ch, gsn_make_bow, false, 1);
            return;
        }
        send_to_char("You successfully make bow.\n", ch);
        check_improve(ch, gsn_make_bow, true, 1);

        bow = create_object(get_obj_index(OBJ_VNUM_RANGER_BOW), ch.level);
        bow.level = ch.level;
        bow.value[1] = 3 + ch.level / 12;
        bow.value[2] = 4 + ch.level / 12;

        AFFECT_DATA tohit = new AFFECT_DATA();
        tohit.where = TO_OBJECT;
        tohit.type = gsn_make_arrow;
        tohit.level = ch.level;
        tohit.duration = -1;
        tohit.location = APPLY_HITROLL;
        tohit.modifier = ch.level / 10;
        tohit.bitvector = 0;
        affect_to_obj(bow, tohit);

        AFFECT_DATA todam = new AFFECT_DATA();
        todam.where = TO_OBJECT;
        todam.type = gsn_make_arrow;
        todam.level = ch.level;
        todam.duration = -1;
        todam.location = APPLY_DAMROLL;
        todam.modifier = ch.level / 10;
        todam.bitvector = 0;
        affect_to_obj(bow, todam);

        obj_to_char(bow, ch);
    }


    static void do_make(CHAR_DATA ch, String argument) {
        StringBuilder argb = new StringBuilder();
        argument = one_argument(argument, argb);
        if (argb.length() == 0) {
            send_to_char("You can make either bow or arrow.\n", ch);
            return;
        }

        String arg = argb.toString();
        if (!str_prefix(arg, "arrow")) {
            do_make_arrow(ch, argument);
        } else if (!str_prefix(arg, "bow")) {
            do_make_bow(ch);
        } else {
            do_make(ch, "");
        }
    }


    static void do_nocancel(CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }

        if (IS_SET(ch.act, PLR_NOCANCEL)) {
            send_to_char("You now accept others' cancellation spells.\n", ch);
            ch.act = REMOVE_BIT(ch.act, PLR_NOCANCEL);
        } else {
            send_to_char("You no longer accept others' cancellation spells.\n", ch);
            ch.act = SET_BIT(ch.act, PLR_NOCANCEL);
        }
    }
}
