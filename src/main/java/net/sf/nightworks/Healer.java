package net.sf.nightworks;

import net.sf.nightworks.util.NotNull;
import static net.sf.nightworks.ActComm.do_say;
import static net.sf.nightworks.Comm.act;
import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.DB.dice;
import static net.sf.nightworks.Handler.deduct_cost;
import static net.sf.nightworks.Handler.is_name;
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
import static net.sf.nightworks.Tables.cabal_table;
import static net.sf.nightworks.util.TextUtils.one_argument;
import static net.sf.nightworks.util.TextUtils.str_prefix;

class Healer {
    @SuppressWarnings("SpellCheckingInspection")
    static void do_heal(@NotNull CHAR_DATA ch, String argument) {
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
        var argb = new StringBuilder();
        one_argument(argument, argb);

        if (argb.isEmpty()) {
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
        var expl = 0;
        var arg = argb.toString();
        if (!str_prefix(arg, "light")) {
            sn = Skill.gsn_cure_light;
            words = "judicandus dies";
            cost = 1000;
        } else if (!str_prefix(arg, "serious")) {
            sn = Skill.gsn_cure_serious;
            words = "judicandus gzfuajg";
            cost = 1600;
        } else if (!str_prefix(arg, "critical")) {
            sn = Skill.gsn_cure_critical;
            words = "judicandus qfuhuqar";
            cost = 2500;
        } else if (!str_prefix(arg, "heal")) {
            sn = Skill.gsn_heal;
            words = "pzar";
            cost = 5000;
        } else if (!str_prefix(arg, "blindness")) {
            sn = Skill.gsn_cure_blindness;
            words = "judicandus noselacri";
            cost = 2000;
        } else if (!str_prefix(arg, "disease")) {
            sn = Skill.gsn_cure_disease;
            words = "judicandus eugzagz";
            cost = 1500;
        } else if (!str_prefix(arg, "poison")) {
            sn = Skill.gsn_cure_poison;
            words = "judicandus sausabru";
            cost = 2500;
        } else if (!str_prefix(arg, "uncurse") || !str_prefix(arg, "curse")) {
            sn = Skill.gsn_remove_curse;
            words = "candussido judifgz";
            cost = 5000;
        } else if (!str_prefix(arg, "mana")) {
            expl = -3;
            words = "candamira";
            cost = 1000;
        } else if (!str_prefix(arg, "refresh") || !str_prefix(arg, "moves")) {
            sn = Skill.gsn_refresh;
            words = "candusima";
            cost = 500;
        } else if (!str_prefix(arg, "master")) {
            sn = Skill.gsn_master_healing;
            words = "candastra nikazubra";
            cost = 20000;
        } else if (!str_prefix(arg, "energize")) {
            expl = -2;
            words = "energizer";
            cost = 20000;
        } else {
            act("Healer does not offer that spell.  Type 'heal' for a list.", ch, null, mob, TO_CHAR);
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
        if (!IS_AFFECTED(ch, AFF_BLIND) && !IS_AFFECTED(ch, AFF_PLAGUE) && !IS_AFFECTED(ch, AFF_POISON) && !IS_AFFECTED(ch, AFF_CURSE)) {
            do_say(mob, "You don't need my help, my dear. But in case!");
            Skill.gsn_remove_curse.spell_fun(mob.level, mob, ch, TARGET_CHAR);
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
            Skill.gsn_cure_blindness.spell_fun(mob.level, mob, ch, TARGET_CHAR);
        }

        if (IS_AFFECTED(ch, AFF_PLAGUE)) {
            Skill.gsn_cure_disease.spell_fun(mob.level, mob, ch, TARGET_CHAR);
        }

        if (IS_AFFECTED(ch, AFF_POISON)) {
            Skill.gsn_cure_poison.spell_fun(mob.level, mob, ch, TARGET_CHAR);
        }

        if (IS_AFFECTED(ch, AFF_CURSE)) {
            Skill.gsn_remove_curse.spell_fun(mob.level, mob, ch, TARGET_CHAR);
        }
    }

}
