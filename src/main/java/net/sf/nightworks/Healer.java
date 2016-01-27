package net.sf.nightworks;

import static net.sf.nightworks.ActComm.do_say;
import static net.sf.nightworks.Comm.act;
import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.DB.dice;
import static net.sf.nightworks.Handler.deduct_cost;
import static net.sf.nightworks.Handler.is_name;
import static net.sf.nightworks.Magic.spell_cure_blindness;
import static net.sf.nightworks.Magic.spell_cure_disease;
import static net.sf.nightworks.Magic.spell_cure_poison;
import static net.sf.nightworks.Magic.spell_remove_curse;
import static net.sf.nightworks.Nightworks.ACT_IS_HEALER;
import static net.sf.nightworks.Nightworks.AFF_BLIND;
import static net.sf.nightworks.Nightworks.AFF_CURSE;
import static net.sf.nightworks.Nightworks.AFF_PLAGUE;
import static net.sf.nightworks.Nightworks.AFF_POISON;
import static net.sf.nightworks.Nightworks.CABAL_BATTLE;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.IS_AFFECTED;
import static net.sf.nightworks.Nightworks.IS_NPC;
import static net.sf.nightworks.Nightworks.IS_SET;
import static net.sf.nightworks.Nightworks.PULSE_VIOLENCE;
import static net.sf.nightworks.Nightworks.TARGET_CHAR;
import static net.sf.nightworks.Nightworks.TO_CHAR;
import static net.sf.nightworks.Nightworks.TO_NOTVICT;
import static net.sf.nightworks.Nightworks.TO_ROOM;
import static net.sf.nightworks.Nightworks.TO_VICT;
import static net.sf.nightworks.Nightworks.UMIN;
import static net.sf.nightworks.Nightworks.WAIT_STATE;
import static net.sf.nightworks.Skill.lookupSkill;
import static net.sf.nightworks.Tables.cabal_table;
import static net.sf.nightworks.util.TextUtils.one_argument;
import static net.sf.nightworks.util.TextUtils.str_prefix;

class Healer {
    static void do_heal(CHAR_DATA ch, String argument) {
        CHAR_DATA mob;
        int cost;
        Skill sn = null;
        String words;

        /* check for healer */
        for (mob = ch.in_room.people; mob != null; mob = mob.next_in_room) {
            if (IS_NPC(mob) && IS_SET(mob.act, ACT_IS_HEALER)) {
                if (ch.cabal != 0 && is_name("cabal", mob.name)) {
                    if (is_name(cabal_table[ch.cabal].short_name, mob.name)) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        if (mob == null) {
            send_to_char("You can't do that here.\n", ch);
            return;
        }

        if (ch.cabal == CABAL_BATTLE) {
            send_to_char("You are BattleRager, not a filthy magician.\n", ch);
            return;
        }
        StringBuilder argb = new StringBuilder();
        one_argument(argument, argb);

        if (argb.length() == 0) {
            /* display price list */
            act("Healer offers the following spells.", ch, null, mob, TO_CHAR);
            send_to_char("  light   : cure light wounds     10 gold\n", ch);
            send_to_char("  serious : cure serious wounds   15 gold\n", ch);
            send_to_char("  critic  : cure critical wounds  25 gold\n", ch);
            send_to_char("  heal    : healing spell         50 gold\n", ch);
            send_to_char("  blind   : cure blindness        20 gold\n", ch);
            send_to_char("  disease : cure disease          15 gold\n", ch);
            send_to_char("  poison  : cure poison           25 gold\n", ch);
            send_to_char("  uncurse : remove curse          50 gold\n", ch);
            send_to_char("  refresh : restore movement       5 gold\n", ch);
            send_to_char("  mana    : restore mana          10 gold\n", ch);
            send_to_char("  master heal: master heal spell 200 gold\n", ch);
            send_to_char("  energize : restore 300 mana    200 gold\n", ch);
            send_to_char(" Type heal <type> to be healed.\n", ch);
            return;
        }
        int expl = 0;
        String arg = argb.toString();
        if (!str_prefix(arg, "light")) {
            sn = lookupSkill("cure light");
            words = "judicandus dies";
            cost = 1000;
        } else if (!str_prefix(arg, "serious")) {
            sn = lookupSkill("cure serious");
            words = "judicandus gzfuajg";
            cost = 1600;
        } else if (!str_prefix(arg, "critical")) {
            sn = lookupSkill("cure critical");
            words = "judicandus qfuhuqar";
            cost = 2500;
        } else if (!str_prefix(arg, "heal")) {
            sn = lookupSkill("heal");
            words = "pzar";
            cost = 5000;
        } else if (!str_prefix(arg, "blindness")) {
            sn = lookupSkill("cure blindness");
            words = "judicandus noselacri";
            cost = 2000;
        } else if (!str_prefix(arg, "disease")) {
            sn = lookupSkill("cure disease");
            words = "judicandus eugzagz";
            cost = 1500;
        } else if (!str_prefix(arg, "poison")) {
            sn = lookupSkill("cure poison");
            words = "judicandus sausabru";
            cost = 2500;
        } else if (!str_prefix(arg, "uncurse") || !str_prefix(arg, "curse")) {
            sn = lookupSkill("remove curse");
            words = "candussido judifgz";
            cost = 5000;
        } else if (!str_prefix(arg, "mana")) {
            expl = -3;
            words = "candamira";
            cost = 1000;
        } else if (!str_prefix(arg, "refresh") || !str_prefix(arg, "moves")) {
            sn = lookupSkill("refresh");
            words = "candusima";
            cost = 500;
        } else if (!str_prefix(arg, "master")) {
            sn = lookupSkill("master healing");
            words = "candastra nikazubra";
            cost = 20000;
        } else if (!str_prefix(arg, "energize")) {
            expl = -2;
            words = "energizer";
            cost = 20000;
        } else {
            act("Healer does not offer that spell.  Type 'heal' for a list.",
                    ch, null, mob, TO_CHAR);
            return;
        }

        if (cost > (ch.gold * 100 + ch.silver)) {
            act("You do not have that much gold.",
                    ch, null, mob, TO_CHAR);
            return;
        }

        WAIT_STATE(ch, PULSE_VIOLENCE);

        deduct_cost(ch, cost);
        mob.gold += cost / 100;

        act("$n utters the words '$T'.", mob, null, words, TO_ROOM);
        if (expl == -2) {
            ch.mana += 300;
            ch.mana = UMIN(ch.mana, ch.max_mana);
            send_to_char("A warm glow passes through you.\n", ch);
        }
        if (expl == -3) {
            ch.mana += dice(2, 8) + mob.level / 3;
            ch.mana = UMIN(ch.mana, ch.max_mana);
            send_to_char("A warm glow passes through you.\n", ch);
        }

        if (sn == null) {
            return;
        }

        sn.spell_fun(mob.level, mob, ch, TARGET_CHAR);
    }


    static void heal_battle(CHAR_DATA mob, CHAR_DATA ch) {

        if (is_name(cabal_table[ch.cabal].short_name, mob.name)) {
            return;
        }

        if (IS_NPC(ch) || ch.cabal != CABAL_BATTLE) {
            do_say(mob, "I won't help you.");
            return;
        }
        Skill sn;
        if (!IS_AFFECTED(ch, AFF_BLIND) && !IS_AFFECTED(ch, AFF_PLAGUE)
                && !IS_AFFECTED(ch, AFF_POISON) && !IS_AFFECTED(ch, AFF_CURSE)) {
            do_say(mob, "You don't need my help, my dear. But in case!");
            sn = lookupSkill("remove curse");
            spell_remove_curse(sn, mob.level, mob, ch, TARGET_CHAR);
            return;
        }

        act("$n gives you some herbs to eat.", mob, null, ch, TO_VICT);
        act("You eat that herbs.", mob, null, ch, TO_VICT);
        act("You give the herbs to $N.", mob, null, ch, TO_CHAR);
        act("$N eats the herbs that you give.", mob, null, ch, TO_CHAR);
        act("$n gives the herbs to $N.", mob, null, ch, TO_NOTVICT);
        act("$n eats the herbs that $N gave $m.", mob, null, ch, TO_NOTVICT);

        WAIT_STATE(ch, PULSE_VIOLENCE);

        if (IS_AFFECTED(ch, AFF_BLIND)) {
            sn = lookupSkill("cure blindness");
            spell_cure_blindness(sn, mob.level, mob, ch);
        }

        if (IS_AFFECTED(ch, AFF_PLAGUE)) {
            sn = lookupSkill("cure disease");
            spell_cure_disease(sn, mob.level, mob, ch);
        }
        if (IS_AFFECTED(ch, AFF_POISON)) {
            sn = lookupSkill("cure poison");
            spell_cure_poison(sn, mob.level, mob, ch);
        }
        if (IS_AFFECTED(ch, AFF_CURSE)) {
            sn = lookupSkill("remove curse");
            spell_remove_curse(sn, mob.level, mob, ch, TARGET_CHAR);
        }
    }

}
