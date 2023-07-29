package net.sf.nightworks;

import java.util.Formatter;

import static net.sf.nightworks.ActComm.do_say;
import static net.sf.nightworks.Comm.act;
import static net.sf.nightworks.Comm.page_to_char;
import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.Const.int_app;
import static net.sf.nightworks.DB.number_percent;
import static net.sf.nightworks.DB.number_range;
import static net.sf.nightworks.Handler.can_see;
import static net.sf.nightworks.Handler.get_char_room;
import static net.sf.nightworks.Handler.get_curr_stat;
import static net.sf.nightworks.Handler.get_skill;
import static net.sf.nightworks.Nightworks.ACT_GAIN;
import static net.sf.nightworks.Nightworks.ACT_PRACTICE;
import static net.sf.nightworks.Nightworks.ACT_TRAIN;
import static net.sf.nightworks.Nightworks.CABAL_NONE;
import static net.sf.nightworks.Nightworks.CABAL_OK;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.CLEVEL_OK;
import static net.sf.nightworks.Nightworks.GROUP_NONE;
import static net.sf.nightworks.Nightworks.HERO;
import static net.sf.nightworks.Nightworks.IS_AWAKE;
import static net.sf.nightworks.Nightworks.IS_NPC;
import static net.sf.nightworks.Nightworks.IS_SET;
import static net.sf.nightworks.Nightworks.LEVEL_HERO;
import static net.sf.nightworks.Nightworks.LEVEL_IMMORTAL;
import static net.sf.nightworks.Nightworks.ORG_RACE;
import static net.sf.nightworks.Nightworks.POS_DEAD;
import static net.sf.nightworks.Nightworks.RACE_OK;
import static net.sf.nightworks.Nightworks.STAT_INT;
import static net.sf.nightworks.Nightworks.TO_CHAR;
import static net.sf.nightworks.Nightworks.TO_ROOM;
import static net.sf.nightworks.Nightworks.UMAX;
import static net.sf.nightworks.Nightworks.UMIN;
import static net.sf.nightworks.Nightworks.URANGE;
import static net.sf.nightworks.Skill.find_spell;
import static net.sf.nightworks.Skill.gsn_vampire;
import static net.sf.nightworks.Tables.prac_table;
import static net.sf.nightworks.Update.gain_exp;
import static net.sf.nightworks.util.TextUtils.one_argument;
import static net.sf.nightworks.util.TextUtils.str_prefix;

class ActSkill {
/* used to converter of prac and train */

    static void do_gain(CHAR_DATA ch, String argument) {
        CHAR_DATA trainer;

        if (IS_NPC(ch)) {
            return;
        }

        /* find a trainer */
        for (trainer = ch.in_room.people;
             trainer != null;
             trainer = trainer.next_in_room) {
            if (IS_NPC(trainer) && (IS_SET(trainer.act, ACT_PRACTICE) ||
                    IS_SET(trainer.act, ACT_TRAIN) || IS_SET(trainer.act, ACT_GAIN))) {
                break;
            }
        }

        if (trainer == null || !can_see(ch, trainer)) {
            send_to_char("You can't do that here.\n", ch);
            return;
        }

        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.isEmpty()) {
            do_say(trainer, "You may convert 10 practices into 1 train.");
            do_say(trainer, "You may revert 1 train into 10 practices.");
            do_say(trainer, "Simply type 'gain convert' or 'gain revert'.");
            return;
        }

        if (!str_prefix(arg.toString(), "revert")) {
            if (ch.train < 1) {
                act("$N tells you 'You are not yet ready.'",
                        ch, null, trainer, TO_CHAR);
                return;
            }

            act("$N helps you apply your training to practice",
                    ch, null, trainer, TO_CHAR);
            ch.practice += 10;
            ch.train -= 1;
            return;
        }

        if (!str_prefix(arg.toString(), "convert")) {
            if (ch.practice < 10) {
                act("$N tells you 'You are not yet ready.'",
                        ch, null, trainer, TO_CHAR);
                return;
            }

            act("$N helps you apply your practice to training",
                    ch, null, trainer, TO_CHAR);
            ch.practice -= 10;
            ch.train += 1;
            return;
        }

        act("$N tells you 'I do not understand...'", ch, null, trainer, TO_CHAR);

    }

/* RT spells and skills show the players spells (or skills) */

    static void do_spells(CHAR_DATA ch) {
        if (IS_NPC(ch)) {
            return;
        }

        StringBuilder[] spell_list = new StringBuilder[LEVEL_HERO];
        char[] spell_columns = new char[LEVEL_HERO];
        boolean found = false;
        StringBuilder buf = new StringBuilder();
        Formatter f = new Formatter(buf);
        Skill[] skills = Skill.skills;
        for (Skill sn : skills) {
            if (sn.skill_level[ch.clazz.id] < LEVEL_HERO && sn.isSpell() && RACE_OK(ch, sn) &&
                    (sn.cabal == ch.cabal || sn.cabal == CABAL_NONE)
                    ) {
                buf.setLength(0);
                found = true;
                int lev = sn.skill_level[ch.clazz.id];
                if (ch.level < lev) {
                    f.format("%-18s  n/a      ", sn.name);
                } else {
                    int mana = UMAX(sn.min_mana, 100 / (2 + ch.level - lev));
                    f.format("%-18s  %3d mana  ", sn.name, mana);
                }
                StringBuilder sb = spell_list[lev];
                if (sb == null) {
                    sb = new StringBuilder();
                    spell_list[lev] = sb;
                }
                if (sb.isEmpty()) {
                    sb.append(new Formatter().format("\nLevel %2d: %s", lev, buf));
                } else /* append */ {
                    if (++spell_columns[lev] % 2 == 0) {
                        sb.append("\n          ");
                    }
                    sb.append(buf);
                }
                buf.setLength(0);
            }
        }

        /* return results */
        if (!found) {
            send_to_char("You know no spells.\n", ch);
            return;
        }

        StringBuilder output = new StringBuilder();
        for (int lev = 0; lev < LEVEL_HERO; lev++) {
            StringBuilder sb = spell_list[lev];
            if (sb != null) {
                output.append(sb);
            }
        }
        output.append("\n");
        page_to_char(output.toString(), ch);
    }

    static void do_skills(CHAR_DATA ch) {

        if (IS_NPC(ch)) {
            return;
        }

        StringBuilder[] skill_list = new StringBuilder[LEVEL_HERO];
        char[] skill_columns = new char[LEVEL_HERO];
        StringBuilder buf = new StringBuilder();
        Formatter f = new Formatter(buf);
        boolean found = false;
        Skill[] skills = Skill.skills;
        for (Skill sn : skills) {
            if (sn.skill_level[ch.clazz.id] < LEVEL_HERO && !sn.isSpell() && RACE_OK(ch, sn) &&
                    (sn.cabal == ch.cabal || sn.cabal == CABAL_NONE)) {
                found = true;
                int lev = sn.skill_level[ch.clazz.id];
                if (ch.level < lev) {
                    f.format("%-18s n/a      ", sn.name);
                } else {
                    f.format("%-18s %3d%%      ", sn.name, ch.pcdata.learned[sn.ordinal()]);
                }

                StringBuilder sb = skill_list[lev];
                if (sb == null) {
                    sb = new StringBuilder();
                    skill_list[lev] = sb;
                }
                if (sb.isEmpty()) {
                    sb.append(new Formatter().format("\nLevel %2d: %s", lev, buf));
                } else /* append */ {
                    if (++skill_columns[lev] % 2 == 0) {
                        sb.append("\n          ");
                    }
                    sb.append(buf);
                }
                buf.setLength(0);
            }
        }

        /* return results */

        if (!found) {
            send_to_char("You know no skills.\n", ch);
            return;
        }

        StringBuilder output = new StringBuilder();
        for (int lev = 0; lev < LEVEL_HERO; lev++) {
            StringBuilder sb = skill_list[lev];
            if (sb != null) {
                output.append(sb);
            }
        }
        output.append("\n");
        send_to_char(output.toString(), ch);
    }


    static int base_exp(CHAR_DATA ch, int points) {
        if (IS_NPC(ch)) {
            return 1500;
        }
        int expl = 1000 + ORG_RACE(ch).pcRace.points + ch.clazz.points;
        return (expl * ORG_RACE(ch).pcRace.getClassModifier(ch.clazz).expMult / 100);
    }

    static int exp_to_level(CHAR_DATA ch, int points) {
        int base = base_exp(ch, points);
        return (base - exp_this_level(ch, ch.level, points));
    }

    static int exp_this_level(CHAR_DATA ch, int level, int points) {
        int base = base_exp(ch, points);
        return (ch.exp - (ch.level * base));
    }


    static int exp_per_level(CHAR_DATA ch, int points) {
        if (IS_NPC(ch)) {
            return 1000;
        }
        int expl = 1000 + ORG_RACE(ch).pcRace.points + ch.clazz.points;
        return expl * ORG_RACE(ch).pcRace.getClassModifier(ch.clazz).expMult / 100;
    }

/* checks for skill improvement */

    static void check_improve(CHAR_DATA ch, Skill sn, boolean success, int multiplier) {
        int chance;

        if (IS_NPC(ch)) {
            return;
        }

        if (ch.level < sn.skill_level[ch.clazz.id]
                || sn.rating[ch.clazz.id] == 0
                || ch.pcdata.learned[sn.ordinal()] == 0
                || ch.pcdata.learned[sn.ordinal()] == 100) {
            return;  /* skill is not known */
        }

        /* check to see if the character has a chance to learn */
        chance = 10 * int_app[get_curr_stat(ch, STAT_INT)].learn;
        chance /= (multiplier * sn.rating[ch.clazz.id] * 4);
        chance += ch.level;

        if (number_range(1, 1000) > chance) {
            return;
        }

        /* now that the character has a CHANCE to learn, see if they really have */

        if (success) {
            chance = URANGE(5, 100 - ch.pcdata.learned[sn.ordinal()], 95);
            if (number_percent() < chance) {
                act("{gYou have become better at " + sn.name + "!{x", ch, null, null, TO_CHAR, POS_DEAD);
                ch.pcdata.learned[sn.ordinal()]++;
                gain_exp(ch, 2 * sn.rating[ch.clazz.id]);
            }
        } else {
            chance = URANGE(5, ch.pcdata.learned[sn.ordinal()] / 2, 30);
            if (number_percent() < chance) {
                act("{gYou learn from your mistakes, and your " + sn.name + " skill improves.{x", ch, null, null, TO_CHAR, POS_DEAD);
                ch.pcdata.learned[sn.ordinal()] += number_range(1, 3);
                ch.pcdata.learned[sn.ordinal()] = UMIN(ch.pcdata.learned[sn.ordinal()], 100);
                gain_exp(ch, 2 * sn.rating[ch.clazz.id]);
            }
        }
    }

/* use for adding all skills available for that ch  */

    static void group_add(CHAR_DATA ch) {
        if (IS_NPC(ch)) /* NPCs do not have skills */ {
            return;
        }
        Skill[] skills = Skill.skills;
        for (int i = 0; i < skills.length; i++) {
            Skill sn = skills[i];
            if (sn.cabal == 0 && RACE_OK(ch, sn) && ch.pcdata.learned[i] < 1 && sn.skill_level[ch.clazz.id] < LEVEL_IMMORTAL) {
                ch.pcdata.learned[i] = 1;
            }
        }

    }


    static void do_slist(CHAR_DATA ch, String argument) {
        if (IS_NPC(ch)) {
            return;
        }
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);
        if (arg.isEmpty()) {
            send_to_char("syntax: slist <class name>.\n", ch);
            return;
        }
        Clazz clazz = Clazz.lookupClass(arg.toString(), false);
        if (clazz == null) {
            send_to_char("That is not a valid class.\n", ch);
            return;
        }
        StringBuilder[] skill_list = new StringBuilder[LEVEL_HERO];
        char[] skill_columns = new char[LEVEL_HERO];
        StringBuilder buf = new StringBuilder();
        Formatter f = new Formatter(buf);
        boolean found = false;
        Skill[] skills = Skill.skills;
        for (Skill sn : skills) {

            if (sn.skill_level[clazz.id] < LEVEL_HERO && sn.cabal == CABAL_NONE && sn.race == null) {
                found = true;
                int lev = sn.skill_level[clazz.id];
                f.format("%-18s          ", sn.name);
                StringBuilder sb = skill_list[lev];
                if (sb == null) {
                    sb = new StringBuilder();
                    skill_list[lev] = sb;
                }

                if (sb.isEmpty()) {
                    sb.append(new Formatter().format("\nLevel %2d: %s", lev, buf));
                } else /* append */ {
                    if (++skill_columns[lev] % 2 == 0) {
                        sb.append("\n          ");
                    }
                    sb.append(buf);
                }
                buf.setLength(0);
            }
        }

        /* return results */

        if (!found) {
            send_to_char("That class know no skills.\n", ch);
            return;
        }

        StringBuilder output = new StringBuilder();
        for (int lev = 0; lev < LEVEL_HERO; lev++) {
            StringBuilder sb = skill_list[lev];
            if (sb != null) {
                output.append(sb);
            }
        }
        output.append("\n");
        page_to_char(output.toString(), ch);
    }

/* returns group number */

    static int group_lookup(String name) {
        int gr;

        for (gr = 0; prac_table[gr].sh_name != null; gr++) {
            if (!str_prefix(name, prac_table[gr].sh_name)) {
                return gr;
            }
        }

        return -1;
    }

    static void do_glist(CHAR_DATA ch, String argument) {
        int group;

        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);

        if (arg.isEmpty()) {
            send_to_char("Syntax : glist <group>\n", ch);
            return;
        }

        if ((group = group_lookup(arg.toString())) == -1) {
            send_to_char("That is not a valid group.\n", ch);
            return;
        }

        send_to_char("Now listing group " + prac_table[group].sh_name + " :\n", ch);
        StringBuilder buf = new StringBuilder();
        Formatter f = new Formatter(buf);
        Skill[] skills = Skill.skills;
        for (Skill sn : skills) {
            if ((group == GROUP_NONE && !CLEVEL_OK(ch, sn) && sn.group == GROUP_NONE) ||
                    (group != sn.group) || !CABAL_OK(ch, sn)) {
                continue;
            }
            if (!buf.isEmpty()) {
                f.format("%-18s%-18s\n", buf, sn.name);
                send_to_char(buf, ch);
                buf.setLength(0);
            } else {
                f.format("%-18s", sn.name);
            }
        }

    }

    static void do_slook(CHAR_DATA ch, String argument) {
        StringBuilder arg = new StringBuilder();
        one_argument(argument, arg);
        if (arg.isEmpty()) {
            send_to_char("Syntax : slook <skill or spell name>.\n", ch);
            return;
        }
        Skill sn;
        if ((sn = Skill.lookupSkill(arg.toString())) == null) {
            send_to_char("That is not a spell or skill.\n", ch);
            return;
        }
        send_to_char("Skill :" + sn.name + " in group " + prac_table[sn.group].sh_name + ".\n", ch);

    }

    private static int PC_PRACTICER = 123;

    static void do_learn(CHAR_DATA ch, String argument) {
        CHAR_DATA mob;
        int adept;

        if (IS_NPC(ch)) {
            return;
        }

        if (!IS_AWAKE(ch)) {
            send_to_char("In your dreams, or what?\n", ch);
            return;
        }

        if (argument.isEmpty()) {
            send_to_char("Syntax: learn <skill | spell> <player>.\n", ch);
            return;
        }

        if (ch.practice <= 0) {
            send_to_char("You have no practice sessions left.\n", ch);
            return;
        }

        StringBuilder arg = new StringBuilder();
        argument = one_argument(argument, arg);

        Skill sn;
        if ((sn = find_spell(ch, arg.toString())) == null
                || (!IS_NPC(ch)
                && (ch.level < sn.skill_level[ch.clazz.id]
                || !RACE_OK(ch, sn) ||
                (sn.cabal != ch.cabal && sn.cabal != CABAL_NONE)))) {
            send_to_char("You can't practice that.\n", ch);
            return;
        }

        if (sn == gsn_vampire) {
            send_to_char("You can't practice that, only available at questor.\n", ch);
            return;
        }

        one_argument(argument, arg);

        if ((mob = get_char_room(ch, arg.toString())) == null) {
            send_to_char("Your hero is not here.\n", ch);
            return;
        }

        if (IS_NPC(mob) || mob.level != HERO) {
            send_to_char("You must find a hero , not an ordinary one.\n", ch);
            return;
        }

        if (mob.status != PC_PRACTICER) {
            send_to_char("Your hero doesn't want to teach you anything.\n", ch);
            return;
        }

        if (get_skill(mob, sn) < 100) {
            send_to_char("Your hero doesn't know that skill enough to teach you.\n", ch);
            return;
        }

        adept = ch.clazz.skill_adept;

        if (ch.pcdata.learned[sn.ordinal()] >= adept) {
            send_to_char("You are already learned at " + sn.name + ".\n", ch);
        } else {
            if (ch.pcdata.learned[sn.ordinal()] == 0) {
                ch.pcdata.learned[sn.ordinal()] = 1;
            }
            ch.practice--;
            ch.pcdata.learned[sn.ordinal()] += int_app[get_curr_stat(ch, STAT_INT)].learn / UMAX(sn.rating[ch.clazz.id], 1);
            mob.status = 0;
            act("You teach $T.", mob, null, sn.name, TO_CHAR);
            act("$n teachs $T.", mob, null, sn.name, TO_ROOM);
            if (ch.pcdata.learned[sn.ordinal()] < adept) {
                act("You learn $T.", ch, null, sn.name, TO_CHAR);
                act("$n learn $T.", ch, null, sn.name, TO_ROOM);
            } else {
                ch.pcdata.learned[sn.ordinal()] = adept;
                act("You are now learned at $T.", ch, null, sn.name, TO_CHAR);
                act("$n is now learned at $T.", ch, null, sn.name, TO_ROOM);
            }
        }
    }


    static void do_teach(CHAR_DATA ch) {
        if (IS_NPC(ch) || ch.level != LEVEL_HERO) {
            send_to_char("You must be a hero.\n", ch);
            return;
        }
        ch.status = PC_PRACTICER;
        send_to_char("Now , you can teach youngsters your 100% skills.\n", ch);
    }

}
