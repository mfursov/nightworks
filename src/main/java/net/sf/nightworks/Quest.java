package net.sf.nightworks;

import net.sf.nightworks.util.NotNull;

import java.util.Formatter;

import static net.sf.nightworks.Comm.act;
import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.Const.hometown_table;
import static net.sf.nightworks.Const.religion_table;
import static net.sf.nightworks.DB.*;
import static net.sf.nightworks.Handler.*;
import static net.sf.nightworks.Nightworks.*;
import static net.sf.nightworks.Skill.gsn_reserved;
import static net.sf.nightworks.Special.spec_lookup;
import static net.sf.nightworks.util.TextUtils.one_argument;
import static net.sf.nightworks.util.TextUtils.str_prefix;

class Quest {

    private static final int QUEST_OBJQUEST1 = 84;
    private static final int QUEST_OBJQUEST2 = 85;
    private static final int QUEST_OBJQUEST3 = 86;
    private static final int QUEST_OBJQUEST4 = 97;

/* CHANCE function. I use this everywhere in my code, very handy :> */

    static boolean chance(int num) {
        return number_range(1, 100) <= num;
    }

/* The main quest function */

    static void do_quest(@NotNull CHAR_DATA ch, String argument) {
        CHAR_DATA questman;
        OBJ_DATA obj = null, obj_next;
        OBJ_INDEX_DATA questinfoobj;
        MOB_INDEX_DATA questinfo;
        var bufvampire = new StringBuilder();
        var bufsamurai = new StringBuilder();
        int trouble_vnum = 0, trouble_n;
        Skill sn;

        var arg1 = new StringBuilder();
        var arg2 = new StringBuilder();
        argument = one_argument(argument, arg1);
        one_argument(argument, arg2);

        if (IS_NPC(ch)) {
            return;
        }

        var arg1Str = arg1.toString();
        if (!str_prefix(arg1Str, "info")) {
            if (IS_SET(ch.act, PLR_QUESTOR)) {
                if (ch.pcdata.questmob == -1) {
                    send_to_char("Your quest is ALMOST complete!\nGet back to Questor before your time runs out!\n", ch);
                } else if (ch.pcdata.questobj > 0) {
                    questinfoobj = get_obj_index(ch.pcdata.questobj);
                    if (questinfoobj != null) {
                        send_to_char("You are on a quest to recover the fabled " + questinfoobj.name + "!\n", ch);
                    } else {
                        send_to_char("You aren't currently on a quest.\n", ch);
                    }
                    return;
                } else if (ch.pcdata.questmob > 0) {
                    questinfo = get_mob_index(ch.pcdata.questmob);
                    if (questinfo != null) {
                        send_to_char("You are on a quest to slay the dreaded " + questinfo.short_descr + "!\n", ch);
                    } else {
                        send_to_char("You aren't currently on a quest.\n", ch);
                    }
                    return;
                }
            } else {
                send_to_char("You aren't currently on a quest.\n", ch);
            }
            return;
        }
        if (!str_prefix(arg1Str, "points")) {
            send_to_char("You have " + ch.pcdata.questpoints + " quest points.\n", ch);
            return;
        }
        if (!str_prefix(arg1Str, "time")) {
            if (!IS_SET(ch.act, PLR_QUESTOR)) {
                send_to_char("You aren't currently on a quest.\n", ch);
                if (ch.pcdata.nextquest > 1) {
                    send_to_char("There are " + ch.pcdata.nextquest + " minutes remaining until you can go on another quest.\n", ch);
                } else if (ch.pcdata.nextquest == 1) {
                    send_to_char("There is less than a minute remaining until you can go on another quest.\n", ch);
                }
            } else if (ch.pcdata.countdown > 0) {
                send_to_char("Time left for current quest: " + ch.pcdata.countdown + "\n", ch);
            }
            return;
        }

/* Checks for a character in the room with spec_questmaster set. This special
   procedure must be defined in special.c. You could instead use an 
   ACT_QUESTMASTER flag instead of a special procedure. */

        for (questman = ch.in_room.people; questman != null; questman = questman.next_in_room) {
            if (!IS_NPC(questman)) {
                continue;
            }
            if (questman.spec_fun == spec_lookup("spec_questmaster")) {
                break;
            }
        }

        if (questman == null || questman.spec_fun != spec_lookup("spec_questmaster")) {
            send_to_char("You can't do that here.\n", ch);
            return;
        }

        if (questman.fighting != null) {
            send_to_char("Wait until the fighting stops.\n", ch);
            return;
        }

        ch.pcdata.questgiver = questman.pIndexData.vnum;

/* And, of course, you will need to change the following lines for YOUR
   quest item information. Quest items on Moongate are unbalanced, very
   very nice items, and no one has one yet, because it takes awhile to
   build up quest points :> Make the item worth their while. */

        if (!str_prefix(arg1Str, "list")) {
            act("$n asks $N for a list of quest items.", ch, null, questman, TO_ROOM);
            act("You ask $N for a list of quest items.", ch, null, questman, TO_CHAR);
/*
1000qp.........The COMFY CHAIR!!!!!!\n\
850qp..........Sword of Vassago\n\
750qp..........Amulet of Vassago\n\
750qp..........Shield of Vassago\n\
550qp..........Decanter of Endless Water\n\
*/
            if (ch.clazz == Clazz.VAMPIRE) {
                bufvampire.append("    50qp.........Vampire skill (vampire)\n");
            }
            if (ch.clazz == Clazz.SAMURAI) {
                bufsamurai.append("   100qp.........Katana quest (katana)\n").
                        append("   100qp.........Second katana quest(sharp)\n").
                        append("    50qp.........Decrease number of death (death)\n");
            }
            var buf = "Current Quest Items available for Purchase:\n"
                    + "5000qp.........the silk-adamantite backpack (backpack)\n"
                    + "1000qp.........the Girth of Real Heroism (girth)\n"
                    + "1000qp.........the Ring of Real Heroism (ring)\n"
                    + "1000qp.........the Real Hero's Weapon (weapon)\n"
                    + "1000qp.........100 Practices (practice)\n"
                    + " 500qp.........Decanter of Endless Water (decanter)\n"
                    + " 500qp.........350,000 gold pieces (gold)\n"
                    + " 250qp.........1 constitution (con)\n"
                    + " 200qp.........tattoo of your religion (tattoo)\n"
                    + bufsamurai + bufvampire
                    + "  50qp.........remove tattoo of your religion (remove)\n"
                    + "  50qp.........set religion to none (set)\n"
                    + "To buy an item, type 'QUEST BUY <item>'.\n";
            send_to_char(buf, ch);
            return;
        } else if (!str_prefix(arg1Str, "buy")) {
            if (arg2.isEmpty()) {
                send_to_char("To buy an item, type 'QUEST BUY <item>'.\n", ch);
                return;
            } else if (is_name(arg2.toString(), "backpack")) {
                if (ch.pcdata.questpoints >= 5000) {
                    ch.pcdata.questpoints -= 5000;
                    obj = create_object(get_obj_index(QUEST_ITEM4), ch.level);
                    if (IS_SET(ch.quest, QUEST_BACKPACK) ||
                            IS_SET(ch.quest, QUEST_BACKPACK2) ||
                            IS_SET(ch.quest, QUEST_BACKPACK3)) {
                        do_tell_quest(ch, questman, "This quest item is beyond the trouble option.");
                    } else {
                        ch.quest = SET_BIT(ch.quest, QUEST_BACKPACK);
                    }
                } else {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you don't have enough quest points for that.");
                    return;
                }
            } else if (is_name(arg2.toString(), "decanter")) {
                if (ch.pcdata.questpoints >= 500) {
                    ch.pcdata.questpoints -= 500;
                    obj = create_object(get_obj_index(QUEST_ITEM5), ch.level);
                    if (IS_SET(ch.quest, QUEST_DECANTER) ||
                            IS_SET(ch.quest, QUEST_DECANTER2) ||
                            IS_SET(ch.quest, QUEST_DECANTER3)) {
                        do_tell_quest(ch, questman, "This quest item is beyond the trouble option.");
                    } else {
                        ch.quest = SET_BIT(ch.quest, QUEST_DECANTER);
                    }
                } else {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you don't have enough quest points for that.");
                    return;
                }
            } else if (is_name(arg2.toString(), "girth")) {
                if (ch.pcdata.questpoints >= 1000) {
                    ch.pcdata.questpoints -= 1000;
                    obj = create_object(get_obj_index(QUEST_ITEM1), ch.level);
                    if (IS_SET(ch.quest, QUEST_GIRTH) ||
                            IS_SET(ch.quest, QUEST_GIRTH2) ||
                            IS_SET(ch.quest, QUEST_GIRTH3)) {
                        do_tell_quest(ch, questman, "This quest item is beyond the trouble option.");
                    } else {
                        ch.quest = SET_BIT(ch.quest, QUEST_GIRTH);
                    }
                } else {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you don't have enough quest points for that.");
                    return;
                }
            } else if (is_name(arg2.toString(), "ring")) {
                if (ch.pcdata.questpoints >= 1000) {
                    ch.pcdata.questpoints -= 1000;
                    obj = create_object(get_obj_index(QUEST_ITEM2), ch.level);
                    if (IS_SET(ch.quest, QUEST_RING) ||
                            IS_SET(ch.quest, QUEST_RING2) ||
                            IS_SET(ch.quest, QUEST_RING3)) {
                        do_tell_quest(ch, questman, "This quest item is beyond the trouble option.");
                    } else {
                        ch.quest = SET_BIT(ch.quest, QUEST_RING);
                    }
                } else {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you don't have enough quest points for that.");
                    return;
                }
            } else if (is_name(arg2.toString(), "weapon")) {
                if (ch.pcdata.questpoints >= 1000) {
                    ch.pcdata.questpoints -= 1000;
                    obj = create_object(get_obj_index(QUEST_ITEM3), ch.level);
                    if (IS_SET(ch.quest, QUEST_WEAPON) ||
                            IS_SET(ch.quest, QUEST_WEAPON2) ||
                            IS_SET(ch.quest, QUEST_WEAPON3)) {
                        do_tell_quest(ch, questman, "This quest item is beyond the trouble option.");
                    } else {
                        ch.quest = SET_BIT(ch.quest, QUEST_WEAPON);
                    }
                } else {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you don't have enough quest points for that.");
                    return;
                }
            } else if (is_name(arg2.toString(), "practices pracs prac practice")) {
                if (IS_SET(ch.quest, QUEST_PRACTICE)) {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you had already got enough practices!");
                    return;
                }

                if (ch.pcdata.questpoints >= 1000) {
                    ch.pcdata.questpoints -= 1000;
                    ch.practice += 100;
                    act("$N gives 100 practices to $n.", ch, null, questman, TO_ROOM);
                    act("$N gives you 100 practices.", ch, null, questman, TO_CHAR);
                    ch.quest = SET_BIT(ch.quest, QUEST_PRACTICE);
                    return;
                } else {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you don't have enough quest points for that.");
                    return;
                }
            } else if (is_name(arg2.toString(), "vampire")) {
                if (ch.clazz != Clazz.VAMPIRE) {
                    do_tell_quest(ch, questman, "You cannot gain this skill, " + ch.name + ".");
                    return;
                }
                if (ch.pcdata.questpoints >= 50) {
                    ch.pcdata.questpoints -= 50;
                    sn = Skill.gsn_vampire;
                    ch.pcdata.learned[sn.ordinal()] = 100;
                    act("$N gives secret of undead to $n.", ch, null, questman, TO_ROOM);
                    act("$N gives you SECRET of undead.", ch, null, questman, TO_CHAR);
                    act("{bLightning flashes in the sky.{x", ch, null, questman, TO_ALL, POS_SLEEPING);
                    return;
                } else {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you don't have enough quest points for that.");
                    return;
                }
            } else if (is_name(arg2.toString(), "con constitution")) {
                if (ch.perm_stat[STAT_CON] >= get_max_train2(ch, STAT_CON)) {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", you have already sufficient constitution.");
                    return;
                }

                if (ch.pcdata.questpoints >= 250) {
                    ch.pcdata.questpoints -= 250;
                    ch.perm_stat[STAT_CON] += 1;
                } else {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you don't have enough quest points for that.");
                    return;
                }
            } else if (is_name(arg2.toString(), "dead samurai death")) {
                if (ch.clazz != Clazz.SAMURAI) {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you are not a samurai.");
                    return;
                }

                if (ch.pcdata.death < 1) {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", you haven't god any death yet.");
                    return;
                }

                if (ch.pcdata.questpoints >= 50) {
                    ch.pcdata.questpoints -= 50;
                    ch.pcdata.death -= 1;
                } else {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you don't have enough quest points for that.");
                    return;
                }
            } else if (is_name(arg2.toString(), "katana sword")) {
                OBJ_DATA katana;
                if (ch.clazz != Clazz.SAMURAI) {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you are not a samurai.");
                    return;
                }

                if ((katana = get_obj_list(ch, "katana", ch.carrying)) == null) {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you don't have your katana with you.");
                    return;
                }

                if (IS_WEAPON_STAT(katana, WEAPON_KATANA)) {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but your katana has already passed the first quest.");
                    return;
                }

                if (ch.pcdata.questpoints >= 100) {
                    ch.pcdata.questpoints -= 100;
                    var af = new AFFECT_DATA();
                    af.where = TO_WEAPON;
                    af.type = gsn_reserved;
                    af.level = 100;
                    af.duration = -1;
                    af.modifier = 0;
                    af.bitvector = WEAPON_KATANA;
                    af.location = APPLY_NONE;
                    affect_to_obj(katana, af);
                    do_tell_quest(ch, questman, "As you wield it, you will feel that it is power will increase, continuously.");
                } else {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you don't have enough quest points for that.");
                    return;
                }
            } else if (is_name(arg2.toString(), "sharp second")) {
                OBJ_DATA katana;

                if (ch.clazz != Clazz.SAMURAI) {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you are not a samurai.");
                    return;
                }

                if ((katana = get_obj_list(ch, "katana", ch.carrying)) == null) {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you don't have your katana with you.");
                    return;
                }

                if (!IS_WEAPON_STAT(katana, WEAPON_KATANA)) {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but your katana hasn't passed the first quest.");
                    return;
                }

                if (ch.pcdata.questpoints >= 100) {
                    ch.pcdata.questpoints -= 100;
                    var af = new AFFECT_DATA();
                    af.where = TO_WEAPON;
                    af.type = gsn_reserved;
                    af.level = 100;
                    af.duration = -1;
                    af.modifier = 0;
                    af.bitvector = WEAPON_SHARP;
                    af.location = APPLY_NONE;
                    affect_to_obj(katana, af);
                    do_tell_quest(ch, questman, "From now on, your katana will be as sharp as blades of titans.");
                } else {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you don't have enough quest points for that.");
                    return;
                }
            } else if (is_name(arg2.toString(), "tattoo religion")) {
                OBJ_DATA tattoo;
                if (ch.religion == 0) {
                    send_to_char("You don't have a religion to have a tattoo.\n", ch);
                    return;
                }
                tattoo = get_eq_char(ch, WEAR_TATTOO);
                if (tattoo != null) {
                    send_to_char("But you have already your tattoo!.\n", ch);
                    return;
                }

                if (ch.pcdata.questpoints >= 200) {
                    ch.pcdata.questpoints -= 200;

                    tattoo = create_object(get_obj_index(religion_table[ch.religion].tattoo_vnum), 100);

                    obj_to_char(tattoo, ch);
                    equip_char(ch, tattoo, WEAR_TATTOO);
                    act("$N tattoos $n with $p!.", ch, tattoo, questman, TO_ROOM);
                    act("$N tattoos you with $p!.", ch, tattoo, questman, TO_CHAR);
                    return;
                } else {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you don't have enough quest points for that.");
                    return;
                }
            } else if (is_name(arg2.toString(), "gold gp")) {
                if (ch.pcdata.questpoints >= 500) {
                    ch.pcdata.questpoints -= 500;
                    ch.gold += 350000;
                    act("$N gives 350,000 gold pieces to $n.", ch, null, questman, TO_ROOM);
                    act("$N has 350,000 in gold transfered from $s Swiss account to your balance.", ch, null, questman, TO_CHAR);
                    return;
                } else {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you don't have enough quest points for that.");
                    return;
                }
            } else if (is_name(arg2.toString(), "remove")) {
                OBJ_DATA tattoo;

                if (ch.pcdata.questpoints >= 50) {
                    if ((tattoo = get_eq_char(ch, WEAR_TATTOO)) == null) {
                        do_tell_quest(ch, questman, "But you don't have any tattoo!");
                        return;
                    }

                    ch.pcdata.questpoints -= 50;
                    extract_obj(tattoo);
                    act("Through a painful process, your tattoo has been destroyed by $n.", questman, null, ch, TO_VICT);
                    act("$N's tattoo is destroyed by $n.", questman, null, ch, TO_NOTVICT);
                    return;
                } else {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you don't have enough quest points for that.");
                    return;
                }
            } else if (is_name(arg2.toString(), "set")) {
                if (ch.pcdata.questpoints >= 50) {
                    if (get_eq_char(ch, WEAR_TATTOO) != null) {
                        do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you have to remove your tattoo first!");
                        return;
                    }
                    if (ch.religion == RELIGION_NONE) {
                        do_tell_quest(ch, questman, "But you are already an atheist!");
                        return;
                    }
                    ch.pcdata.questpoints -= 50;
                    ch.religion = RELIGION_NONE;
                    send_to_char("You don't believe any religion now.\n", ch);
                    act("$N's religion is set to NONE!.", questman, null, ch, TO_NOTVICT);
                } else {
                    do_tell_quest(ch, questman, "Sorry, " + ch.name + ", but you don't have enough quest points for that.");
                    return;
                }
            } else {
                do_tell_quest(ch, questman, "I don't have that item, " + ch.name + ".");
            }
            if (obj != null) {
                if (obj.pIndexData.vnum == QUEST_ITEM4
                        || obj.pIndexData.vnum == QUEST_ITEM5) {
                    var f = new Formatter();
                    f.format(obj.pIndexData.extra_descr.description, ch.name);
                    obj.extra_descr = new EXTRA_DESCR_DATA();
                    obj.extra_descr.keyword = obj.pIndexData.extra_descr.keyword;
                    obj.extra_descr.description = f.toString();
                    obj.extra_descr.next = null;
                }
                if (obj.pIndexData.vnum == QUEST_ITEM1
                        || obj.pIndexData.vnum == QUEST_ITEM2
                        || obj.pIndexData.vnum == QUEST_ITEM3) {
                    var f = new Formatter();
                    f.format(obj.short_descr, IS_GOOD(ch) ? "holy" : IS_NEUTRAL(ch) ? "blue-green" : "evil", ch.name);
                    obj.short_descr = f.toString();
                }
                act("$N gives $p to $n.", ch, obj, questman, TO_ROOM);
                act("$N gives you $p.", ch, obj, questman, TO_CHAR);
                obj_to_char(obj, ch);
            }
            return;
        } else if (!str_prefix(arg1Str, "request")) {
            act("$n asks $N for a quest.", ch, null, questman, TO_ROOM);
            act("You ask $N for a quest.", ch, null, questman, TO_CHAR);
            if (IS_SET(ch.act, PLR_QUESTOR)) {
                do_tell_quest(ch, questman, "But you're already on a quest!");
                return;
            }
            if (ch.pcdata.nextquest > 0) {
                do_tell_quest(ch, questman, "You're very brave, " + ch.name + ", but let someone else have a chance.");
                do_tell_quest(ch, questman, "Come back later.");
                return;
            }

            do_tell_quest(ch, questman, "Thank you, brave " + ch.name + "!");

            generate_quest(ch, questman);

            if (ch.pcdata.questmob > 0 || ch.pcdata.questobj > 0) {
                ch.pcdata.countdown = number_range(15, 30);
                ch.act = SET_BIT(ch.act, PLR_QUESTOR);
                do_tell_quest(ch, questman, "You have {c" + ch.pcdata.countdown + "{x minutes to complete this quest.");
                do_tell_quest(ch, questman, "May the gods go with you!");
            }
            return;
        } else if (!str_prefix(arg1Str, "complete")) {
            act("$n informs $N $e has completed $s quest.", ch, null, questman, TO_ROOM);
            act("You inform $N you have completed $s quest.", ch, null, questman, TO_CHAR);
            if (ch.pcdata.questgiver != questman.pIndexData.vnum) {
                do_tell_quest(ch, questman, "I never sent you on a quest! Perhaps you're thinking of someone else.");
                return;
            }

            if (IS_SET(ch.act, PLR_QUESTOR)) {
                if (ch.pcdata.questmob == -1 && ch.pcdata.countdown > 0) {
                    int reward, pointreward, pracreward, level;

                    level = ch.level;
                    reward = 100 + dice(level, 20);
                    reward = UMAX(180, reward);
                    pointreward = number_range(20, 40);

                    do_tell_quest(ch, questman, "Congratulations on completing your quest!");
                    do_tell_quest(ch, questman, "As a reward, I am giving you {w" + pointreward + "{x quest points, and {Y" + reward + "{x gold.");
                    if (chance(2)) {
                        pracreward = number_range(1, 6);
                        send_to_char("You gain " + pracreward + " practices!\n", ch);
                        ch.practice += pracreward;
                    }

                    ch.act = REMOVE_BIT(ch.act, PLR_QUESTOR);
                    ch.pcdata.questgiver = 0;
                    ch.pcdata.countdown = 0;
                    ch.pcdata.questmob = 0;
                    ch.pcdata.questobj = 0;
                    ch.pcdata.nextquest = 5;
                    ch.gold += reward;
                    ch.pcdata.questpoints += pointreward;

                    return;
                } else if (ch.pcdata.questobj > 0 && ch.pcdata.countdown > 0) {
                    var obj_found = false;

                    for (obj = ch.carrying; obj != null; obj = obj_next) {
                        obj_next = obj.next_content;

                        if (obj.pIndexData.vnum == ch.pcdata.questobj && obj.extra_descr.description.contains(ch.name)) {
                            obj_found = true;
                            break;
                        }
                    }
                    if (obj_found) {
                        int reward, pointreward, pracreward;

                        reward = 200 + number_range(1, 20 * ch.level);
                        pointreward = number_range(15, 40);

                        act("You hand $p to $N.", ch, obj, questman, TO_CHAR);
                        act("$n hands $p to $N.", ch, obj, questman, TO_ROOM);

                        do_tell_quest(ch, questman, "Congratulations on completing your quest!");
                        do_tell_quest(ch, questman, "As a reward, I am giving you " + pointreward + " quest points, and " + reward + " gold.");
                        if (chance(15)) {
                            pracreward = number_range(1, 6);
                            send_to_char("You gain " + pracreward + " practices!\n", ch);
                            ch.practice += pracreward;
                        }

                        ch.act = REMOVE_BIT(ch.act, PLR_QUESTOR);
                        ch.pcdata.questgiver = 0;
                        ch.pcdata.countdown = 0;
                        ch.pcdata.questmob = 0;
                        ch.pcdata.questobj = 0;
                        ch.pcdata.nextquest = 5;
                        ch.gold += reward;
                        ch.pcdata.questpoints += pointreward;
                        extract_obj(obj);
                    } else {
                        do_tell_quest(ch, questman, "You haven't completed the quest yet, but there is still time!");
                    }
                    return;
                } else if ((ch.pcdata.questmob > 0 || ch.pcdata.questobj > 0) && ch.pcdata.countdown > 0) {
                    do_tell_quest(ch, questman, "You haven't completed the quest yet, but there is still time!");
                    return;
                }
            }
            if (ch.pcdata.nextquest > 0) {
                do_tell_quest(ch, questman, "But you didn't complete your quest in time!");
            } else {
                do_tell_quest(ch, questman, "You have to REQUEST a quest first, " + ch.name + ".");
            }
            return;
        } else if (!str_prefix(arg1Str, "trouble")) {
            if (arg2.isEmpty()) {
                send_to_char("To correct a quest award's trouble, type: quest trouble <award>'.\n", ch);
                return;
            }

            trouble_n = 0;
            if (is_name(arg2.toString(), "girth")) {
                if (IS_SET(ch.quest, QUEST_GIRTH)) {
                    ch.quest = REMOVE_BIT(ch.quest, QUEST_GIRTH);
                    ch.quest = SET_BIT(ch.quest, QUEST_GIRTH2);
                    trouble_n = 1;
                } else if (IS_SET(ch.quest, QUEST_GIRTH2)) {
                    ch.quest = REMOVE_BIT(ch.quest, QUEST_GIRTH2);
                    ch.quest = SET_BIT(ch.quest, QUEST_GIRTH3);
                    trouble_n = 2;
                } else if (IS_SET(ch.quest, QUEST_GIRTH3)) {
                    ch.quest = REMOVE_BIT(ch.quest, QUEST_GIRTH3);
                    trouble_n = 3;
                }
                if (trouble_n != 0) {
                    trouble_vnum = QUEST_ITEM1;
                }
            } else if (is_name(arg2.toString(), "backpack")) {
                if (IS_SET(ch.quest, QUEST_BACKPACK)) {
                    ch.quest = REMOVE_BIT(ch.quest, QUEST_BACKPACK);
                    ch.quest = SET_BIT(ch.quest, QUEST_BACKPACK2);
                    trouble_n = 1;
                } else if (IS_SET(ch.quest, QUEST_BACKPACK2)) {
                    ch.quest = REMOVE_BIT(ch.quest, QUEST_BACKPACK2);
                    ch.quest = SET_BIT(ch.quest, QUEST_BACKPACK3);
                    trouble_n = 2;
                } else if (IS_SET(ch.quest, QUEST_BACKPACK3)) {
                    ch.quest = REMOVE_BIT(ch.quest, QUEST_BACKPACK3);
                    trouble_n = 3;
                }
                if (trouble_n != 0) {
                    trouble_vnum = QUEST_ITEM4;
                }
            } else if (is_name(arg2.toString(), "decanter")) {
                if (IS_SET(ch.quest, QUEST_DECANTER)) {
                    ch.quest = REMOVE_BIT(ch.quest, QUEST_DECANTER);
                    ch.quest = SET_BIT(ch.quest, QUEST_DECANTER2);
                    trouble_n = 1;
                } else if (IS_SET(ch.quest, QUEST_DECANTER2)) {
                    ch.quest = REMOVE_BIT(ch.quest, QUEST_DECANTER2);
                    ch.quest = SET_BIT(ch.quest, QUEST_DECANTER3);
                    trouble_n = 2;
                } else if (IS_SET(ch.quest, QUEST_DECANTER3)) {
                    ch.quest = REMOVE_BIT(ch.quest, QUEST_DECANTER3);
                    trouble_n = 3;
                }
                if (trouble_n != 0) {
                    trouble_vnum = QUEST_ITEM5;
                }
            } else if (is_name(arg2.toString(), "weapon")) {
                if (IS_SET(ch.quest, QUEST_WEAPON)) {
                    ch.quest = REMOVE_BIT(ch.quest, QUEST_WEAPON);
                    ch.quest = SET_BIT(ch.quest, QUEST_WEAPON2);
                    trouble_n = 1;
                } else if (IS_SET(ch.quest, QUEST_WEAPON2)) {
                    ch.quest = REMOVE_BIT(ch.quest, QUEST_WEAPON2);
                    ch.quest = SET_BIT(ch.quest, QUEST_WEAPON3);
                    trouble_n = 2;
                } else if (IS_SET(ch.quest, QUEST_WEAPON3)) {
                    ch.quest = REMOVE_BIT(ch.quest, QUEST_WEAPON3);
                    trouble_n = 3;
                }
                if (trouble_n != 0) {
                    trouble_vnum = QUEST_ITEM3;
                }
            } else if (is_name(arg2.toString(), "ring")) {
                if (IS_SET(ch.quest, QUEST_RING)) {
                    ch.quest = REMOVE_BIT(ch.quest, QUEST_RING);
                    ch.quest = SET_BIT(ch.quest, QUEST_RING2);
                    trouble_n = 1;
                } else if (IS_SET(ch.quest, QUEST_RING2)) {
                    ch.quest = REMOVE_BIT(ch.quest, QUEST_RING2);
                    ch.quest = SET_BIT(ch.quest, QUEST_RING3);
                    trouble_n = 2;
                } else if (IS_SET(ch.quest, QUEST_RING3)) {
                    ch.quest = REMOVE_BIT(ch.quest, QUEST_RING3);
                    trouble_n = 3;
                }
                if (trouble_n != 0) {
                    trouble_vnum = QUEST_ITEM2;
                }
            }
            if (trouble_n != 0) {
                do_tell_quest(ch, questman, "Sorry " + ch.name + ", but you haven't bought that quest award, yet.\n");
                return;
            }

            for (obj = object_list; obj != null; obj = obj_next) {
                obj_next = obj.next;
                if (obj.pIndexData.vnum == trouble_vnum &&
                        obj.short_descr.contains(ch.name)) {
                    extract_obj(obj);
                    break;
                }
            }
            obj = create_object(get_obj_index(trouble_vnum), ch.level);
            if (obj.pIndexData.vnum == QUEST_ITEM4
                    || obj.pIndexData.vnum == QUEST_ITEM5) {
                var f = new Formatter();
                f.format(obj.pIndexData.extra_descr.description, ch.name);
                obj.extra_descr = new EXTRA_DESCR_DATA();
                obj.extra_descr.keyword = obj.pIndexData.extra_descr.keyword;
                obj.extra_descr.description = f.toString();
                obj.extra_descr.next = null;
            }
            if (obj.pIndexData.vnum == QUEST_ITEM1 || obj.pIndexData.vnum == QUEST_ITEM2 || obj.pIndexData.vnum == QUEST_ITEM3) {
                var f = new Formatter();
                f.format(obj.short_descr, IS_GOOD(ch) ? "holy" : IS_NEUTRAL(ch) ? "blue-green" : "evil", ch.name);
                obj.short_descr = f.toString();
            }
            act("$N gives $p to $n.", ch, obj, questman, TO_ROOM);
            act("$N gives you $p.", ch, obj, questman, TO_CHAR);
            obj_to_char(obj, ch);
            do_tell_quest(ch, questman, "This is the " + trouble_n + ((trouble_n == 1) ? "st" : (trouble_n == 2) ? "nd" : "rd") + " time that i am giving that award back.");
            if (trouble_n == 3) {
                do_tell_quest(ch, questman, "And I won't give you that again, with trouble option.\n");
            }
            return;
        }

        send_to_char("QUEST COMMANDS: points info time request complete list buy trouble.\n", ch);
        send_to_char("For more information, type: help quests.\n", ch);
    }

    static void generate_quest(@NotNull CHAR_DATA ch, CHAR_DATA questman) {
        CHAR_DATA victim;
        MOB_INDEX_DATA vsearch = null;
        ROOM_INDEX_DATA room;
        OBJ_DATA eyed;
        int level_diff, i;
        int mob_count;
        int found;

        //room	=	new ROOM_INDEX_DATA();
        var mob_buf = new int[300];

        mob_count = 0;
        for (i = 0; i < MAX_KEY_HASH; i++) {
            if ((vsearch = mob_index_hash[i]) == null) {
                continue;
            }
            level_diff = vsearch.level - ch.level;
            if ((ch.level < 51 && (level_diff > 4 || level_diff < -1))
                    || (ch.level > 50 && (level_diff > 6 || level_diff < 0))
                    || vsearch.pShop != null
                    || IS_SET(vsearch.act, ACT_TRAIN)
                    || IS_SET(vsearch.act, ACT_PRACTICE)
                    || IS_SET(vsearch.act, ACT_IS_HEALER)
                    || IS_SET(vsearch.act, ACT_NOTRACK)
                    || IS_SET(vsearch.imm_flags, IMM_SUMMON)) {
                continue;
            }
            mob_buf[mob_count] = vsearch.vnum;
            mob_count++;
            if (mob_count > 299) {
                break;
            }
        }

        if (chance(40)) {
            var objvnum = 0;

            if (mob_count > 0) {
                found = number_range(0, mob_count - 1);
                for (i = 0; i < mob_count; i++) {
                    if ((vsearch = get_mob_index(mob_buf[found])) == null) {
                        bug("Unknown mob in generate_obj_quest: %d", mob_buf[found]);
                        found++;
                        if (found > (mob_count - 1)) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            } else {
                vsearch = null;
            }

            if (vsearch == null || (victim = get_quest_world(ch, vsearch)) == null) {
                do_tell_quest(ch, questman, "I'm sorry, but I don't have any quests for you at this time.");
                do_tell_quest(ch, questman, "Try again later.");
                ch.pcdata.nextquest = 5;
                return;
            }

            if ((room = victim.in_room) == null) {
                do_tell_quest(ch, questman, "I'm sorry, but I don't have any quests for you at this time.");
                do_tell_quest(ch, questman, "Try again later.");
                ch.pcdata.nextquest = 5;
                return;
            }

            switch (number_range(0, 3)) {
                case 0 -> objvnum = QUEST_OBJQUEST1;
                case 1 -> objvnum = QUEST_OBJQUEST2;
                case 2 -> objvnum = QUEST_OBJQUEST3;
                case 3 -> objvnum = QUEST_OBJQUEST4;
            }


            if (IS_GOOD(ch)) {
                i = 0;
            } else if (IS_EVIL(ch)) {
                i = 2;
            } else {
                i = 1;
            }

            eyed = create_object(get_obj_index(objvnum), ch.level);
            eyed.owner = ch.name;
            eyed.from = ch.name;
            eyed.altar = hometown_table[ch.hometown].altar[i];
            eyed.pit = hometown_table[ch.hometown].pit[i];
            eyed.level = ch.level;

            var f1 = new Formatter();
            f1.format(eyed.description, ch.name);
            eyed.description = f1.toString();

            var f2 = new Formatter();
            f2.format(eyed.pIndexData.extra_descr.description, ch.name);
            eyed.extra_descr = new EXTRA_DESCR_DATA();
            eyed.extra_descr.keyword = eyed.pIndexData.extra_descr.keyword;
            eyed.extra_descr.description = f2.toString();
            eyed.extra_descr.next = null;

            eyed.cost = 0;
            eyed.timer = 30;

            obj_to_room(eyed, room);
            ch.pcdata.questobj = eyed.pIndexData.vnum;

            do_tell_quest(ch, questman, "Vile pilferers have stolen {w" + eyed.short_descr + "{x from the royal treasury!");
            do_tell_quest(ch, questman, "My court wizardess, with her magic mirror, has pinpointed its location.");

            /* I changed my area names so that they have just the name of the area
         and none of the level stuff. You may want to comment these next two
         lines. - Vassago */

            do_tell_quest(ch, questman, "Look in the general area of {w" + room.area.name + "{x for " + room.name + "!");
        } else {         /* Quest to kill a mob */
            if (mob_count > 0) {
                found = number_range(0, mob_count - 1);
                for (i = 0; i < mob_count; i++) {
                    if ((vsearch = get_mob_index(mob_buf[found])) == null
                            || (IS_EVIL(vsearch) && IS_EVIL(ch))
                            || (IS_GOOD(vsearch) && IS_GOOD(ch))
                            || (IS_NEUTRAL(vsearch) && IS_NEUTRAL(ch))) {
                        if (vsearch == null) {
                            bug("Unknown mob in mob_quest: %d", mob_buf[found]);
                        }
                        found++;
                        if (found > (mob_count - 1)) {
                            vsearch = null;
                            break;
                        }
                    } else {
                        break;
                    }
                }
            } else {
                vsearch = null;
            }

            if (vsearch == null
                    || (victim = get_quest_world(ch, vsearch)) == null
                    || (room = victim.in_room) == null
                    || IS_SET(room.area.area_flag, AREA_HOMETOWN)) {
                do_tell_quest(ch, questman, "I'm sorry, but I don't have any quests for you at this time.");
                do_tell_quest(ch, questman, "Try again later.");
                ch.pcdata.nextquest = 5;
                return;
            }

            if (IS_GOOD(ch)) {
                do_tell_quest(ch, questman, "Rune's most heinous criminal, {w" + victim.short_descr + "{x,	has escaped from the dungeon!");
                do_tell_quest(ch, questman, "Since the escape, " + victim.short_descr + " has murdered " + number_range(2, 20) + " civillians!");
                do_tell_quest(ch, questman, "The penalty for this crime is death, and you are to deliver the sentence!");
            } else {
                do_tell_quest(ch, questman, "An enemy of mine, {x" + victim.short_descr + "{x, is making vile threats against the crown.");
                do_tell_quest(ch, questman, "This threat must be eliminated!");
            }

            if (room.name != null) {
                do_tell_quest(ch, questman, "Seek " + victim.short_descr + " out in vicinity of {w" + room.name + "{x!");

                /* I changed my area names so that they have just the name of the area
             and none of the level stuff. You may want to comment these next two
             lines. - Vassago */

                do_tell_quest(ch, questman, "That location is in the general area of " + room.area.name + ".");
            }
            ch.pcdata.questmob = victim.pIndexData.vnum;
        }
    }

/* Called from update_handler() by pulse_area */

    static void quest_update() {
        CHAR_DATA ch, ch_next;

        for (ch = char_list; ch != null; ch = ch_next) {
            ch_next = ch.next;
            if (IS_NPC(ch)) {
                continue;
            }

            if (ch.pcdata.nextquest > 0) {
                ch.pcdata.nextquest--;

                if (ch.pcdata.nextquest == 0) {
                    send_to_char("You may now quest again.\n", ch);
                }
            } else if (IS_SET(ch.act, PLR_QUESTOR)) {
                if (--ch.pcdata.countdown <= 0) {
                    ch.pcdata.nextquest = 0;
                    send_to_char("You have run out of time for your quest!\nYou may now quest again.\n", ch);
                    ch.act = REMOVE_BIT(ch.act, PLR_QUESTOR);
                    ch.pcdata.questgiver = 0;
                    ch.pcdata.countdown = 0;
                    ch.pcdata.questmob = 0;
                    ch.pcdata.questobj = 0;
                }
                if (ch.pcdata.countdown > 0 && ch.pcdata.countdown < 6) {
                    send_to_char("Better hurry, you're almost out of time for your quest!\n", ch);
                }
            }
        }
    }

    static void do_tell_quest(@NotNull CHAR_DATA ch, CHAR_DATA victim, String argument) {
        send_to_char(victim.name + " tells you " + argument + "\n", ch);
    }

    static CHAR_DATA get_quest_world(@NotNull CHAR_DATA ch, MOB_INDEX_DATA victim) {
        for (var wch = char_list; wch != null; wch = wch.next) {
            if (wch.in_room == null || wch.pIndexData != victim) {
                continue;
            }
            return wch;
        }
        return null;
    }

}
