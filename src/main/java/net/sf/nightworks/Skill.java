package net.sf.nightworks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static net.sf.nightworks.Handler.skill_failure_nomessage;
import static net.sf.nightworks.Nightworks.ALIGN_NONE;
import static net.sf.nightworks.Nightworks.CABAL_BATTLE;
import static net.sf.nightworks.Nightworks.CABAL_CHAOS;
import static net.sf.nightworks.Nightworks.CABAL_HUNTER;
import static net.sf.nightworks.Nightworks.CABAL_INVADER;
import static net.sf.nightworks.Nightworks.CABAL_KNIGHT;
import static net.sf.nightworks.Nightworks.CABAL_LIONS;
import static net.sf.nightworks.Nightworks.CABAL_NONE;
import static net.sf.nightworks.Nightworks.CABAL_RULER;
import static net.sf.nightworks.Nightworks.CABAL_SHALAFI;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.GROUP_ATTACK;
import static net.sf.nightworks.Nightworks.GROUP_BEGUILING;
import static net.sf.nightworks.Nightworks.GROUP_BENEDICTIONS;
import static net.sf.nightworks.Nightworks.GROUP_CABAL;
import static net.sf.nightworks.Nightworks.GROUP_COMBAT;
import static net.sf.nightworks.Nightworks.GROUP_CREATION;
import static net.sf.nightworks.Nightworks.GROUP_CURATIVE;
import static net.sf.nightworks.Nightworks.GROUP_DEFENSIVE;
import static net.sf.nightworks.Nightworks.GROUP_DETECTION;
import static net.sf.nightworks.Nightworks.GROUP_DRACONIAN;
import static net.sf.nightworks.Nightworks.GROUP_ENCHANTMENT;
import static net.sf.nightworks.Nightworks.GROUP_ENHANCEMENT;
import static net.sf.nightworks.Nightworks.GROUP_FIGHTMASTER;
import static net.sf.nightworks.Nightworks.GROUP_HARMFUL;
import static net.sf.nightworks.Nightworks.GROUP_HEALING;
import static net.sf.nightworks.Nightworks.GROUP_ILLUSION;
import static net.sf.nightworks.Nightworks.GROUP_MALADICTIONS;
import static net.sf.nightworks.Nightworks.GROUP_MEDITATION;
import static net.sf.nightworks.Nightworks.GROUP_NONE;
import static net.sf.nightworks.Nightworks.GROUP_PROTECTIVE;
import static net.sf.nightworks.Nightworks.GROUP_TRANSPORTATION;
import static net.sf.nightworks.Nightworks.GROUP_WEAPONSMASTER;
import static net.sf.nightworks.Nightworks.GROUP_WEATHER;
import static net.sf.nightworks.Nightworks.GROUP_WIZARD;
import static net.sf.nightworks.Nightworks.IS_NPC;
import static net.sf.nightworks.Nightworks.MAX_CLASS;
import static net.sf.nightworks.Nightworks.POS_DEAD;
import static net.sf.nightworks.Nightworks.POS_FIGHTING;
import static net.sf.nightworks.Nightworks.POS_RESTING;
import static net.sf.nightworks.Nightworks.POS_SLEEPING;
import static net.sf.nightworks.Nightworks.POS_STANDING;
import static net.sf.nightworks.Nightworks.TAR_CHAR_DEFENSIVE;
import static net.sf.nightworks.Nightworks.TAR_CHAR_OFFENSIVE;
import static net.sf.nightworks.Nightworks.TAR_CHAR_SELF;
import static net.sf.nightworks.Nightworks.TAR_IGNORE;
import static net.sf.nightworks.Nightworks.TAR_OBJ_CHAR_DEF;
import static net.sf.nightworks.Nightworks.TAR_OBJ_CHAR_OFF;
import static net.sf.nightworks.Nightworks.TAR_OBJ_INV;
import static net.sf.nightworks.util.TextUtils.str_prefix;

enum Skill {
    gsn_reserved(
            "gsn_reserved", false,
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    /*
     * Magic spells.
    */
    gsn_absorb(
            "absorb", true,
            TAR_CHAR_SELF, POS_STANDING, SLOT(707), 100, 12,
            "", "The energy field around you fades!",
            "$p's energy field fades.",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_acetum_primus(
            "acetum primus", true,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(654), 20, 12, "acetum primus", "!acetum primus!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_acid_arrow(
            "acid arrow", true,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(644), 20, 12, "acid arrow", "!Acid Arrow!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_acid_blast(
            "acid blast", true,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(70), 40, 12,
            "acid blast", "!Acid Blast!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_acute_vision(
            "acute vision", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(514), 10, 0, "", "Your vision seems duller.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_adamantite_golem(
            "adamantite golem", true,

            TAR_IGNORE, POS_STANDING, SLOT(665), 500, 30, "", "You gained enough mana to make more golems now.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_aid(
            "aid", true,

            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(680), 100, 12, "", "You can aid more people.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_HEALING
    ),

    gsn_amnesia(
            "amnesia", true,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(538), 100, 12, "", "!amnesia!", "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_animate_dead(
            "animate dead", true,

            TAR_OBJ_CHAR_OFF, POS_STANDING, SLOT(581), 50, 12, "", "You gain energy to animate new deads.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_animate_object(
            "animate object", true,
            TAR_OBJ_CHAR_OFF, POS_STANDING, SLOT(709), 50, 12, "", "You gain energy to animate new objects.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_armor(
            "armor", true,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(1), 5, 12, "", "You feel less armored.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_assist(
            "assist", true,

            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(670), 100, 12, "", "You can assist more, now.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HEALING
    ),

    gsn_astral_walk(
            "astral walk", true,

            TAR_IGNORE, POS_FIGHTING, SLOT(622), 80, 12, "", "!Astral Walk!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_attract_other(
            "attract other", true,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(580), 5, 12, "", "You feel your master leaves you.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BEGUILING
    ),

    gsn_bark_skin(
            "bark skin", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(515), 40, 0, "", "The bark on your skin flakes off.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_black_death(
            "black death", true,

            TAR_IGNORE, POS_STANDING, SLOT(677), 200, 24,
            "", "!black death!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_blade_barrier(
            "blade barrier", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(679), 40, 12, "blade barrier", "!Blade Barrier!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ATTACK
    ),

    gsn_bless(
            "bless", true,

            TAR_OBJ_CHAR_DEF, POS_STANDING, SLOT(3), 5, 12,
            "", "You feel less righteous.",
            "$p's holy aura fades.", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_bless_weapon(
            "bless weapon", true,

            TAR_OBJ_INV, POS_STANDING, SLOT(637), 100, 24, "", "!Bless Weapon!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ENCHANTMENT
    ),

    gsn_blindness(
            "blindness", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(4), 5, 12,
            "", "You can see again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_bluefire(
            "bluefire", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(660), 20, 12,
            "torments", "!Bluefire!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ATTACK
    ),

    gsn_burning_hands(
            "burning hands", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(5), 15, 12,
            "burning hands", "!Burning Hands!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_call_lightning(
            "call lightning", true,

            TAR_IGNORE, POS_FIGHTING, SLOT(6), 15, 12, "lightning bolt", "!Call Lightning!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEATHER
    ),

    gsn_calm(
            "calm", true,

            TAR_IGNORE, POS_FIGHTING,
            SLOT(509), 30, 12,
            "", "You have lost your peace of mind.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_cancellation(
            "cancellation", true,

            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(507), 20, 12, "", "!cancellation!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_cause_critical(
            "cause critical", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(63), 20, 12, "spell", "!Cause Critical!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HARMFUL
    ),

    gsn_cause_light(
            "cause light", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(62), 15, 12, "spell", "!Cause Light!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HARMFUL
    ),

    gsn_cause_serious(
            "cause serious", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(64), 17, 12, "spell", "!Cause Serious!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HARMFUL
    ),

    gsn_caustic_font(
            "caustic font", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(653), 20, 12, "caustic font", "!caustic font!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_chain_lightning(
            "chain lightning", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(500), 25, 12, "lightning", "!Chain Lightning!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_charm_person(
            "charm person", true,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(7), 5, 12,
            "", "You feel more self-confident.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_BEGUILING
    ),

    gsn_chromatic_orb(
            "chromatic orb", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(714), 50, 12, "chromatic orb", "!Chromatic Orb!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_control_undead(
            "control undead", true,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(669), 20, 12, "", "You feel more self confident.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_chill_touch(
            "chill touch", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(8), 15, 12,
            "chilling touch", "You feel less cold.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_colour_spray(
            "colour spray", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(10), 15, 12, "colour spray", "!Colour Spray!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_continual_light(
            "continual light", true,

            TAR_IGNORE, POS_STANDING, SLOT(57), 7, 12, "", "!Continual Light!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_CREATION
    ),

    gsn_control_weather(
            "control weather", true,

            TAR_IGNORE, POS_STANDING, SLOT(11), 25, 12, "", "!Control Weather!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEATHER
    ),

    gsn_corruption(
            "corruption", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(671), 20, 12, "corruption", "You feel yourself healthy again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_create_food(
            "create food", true,

            TAR_IGNORE, POS_STANDING, SLOT(12), 5, 12, "", "!Create Food!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_CREATION
    ),

    gsn_create_rose(
            "create rose", true,

            TAR_IGNORE, POS_STANDING, SLOT(511), 30, 12, "", "!Create Rose!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_CREATION
    ),

    gsn_create_spring(
            "create spring", true,

            TAR_IGNORE, POS_STANDING, SLOT(80), 20, 12, "", "!Create Spring!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_CREATION
    ),

    gsn_create_water(
            "create water", true,

            TAR_OBJ_INV, POS_STANDING, SLOT(13), 5, 12, "", "!Create Water!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_CREATION
    ),

    gsn_cure_blindness(
            "cure blindness", true,

            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(14), 5, 12, "", "!Cure Blindness!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_CURATIVE
    ),

    gsn_cure_critical(
            "cure critical", true,

            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(15), 20, 12,
            "", "!Cure Critical!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HEALING
    ),

    gsn_cure_disease(
            "cure disease", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(501), 20, 12, "", "!Cure Disease!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_CURATIVE
    ),

    gsn_cure_light(
            "cure light", true,

            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(16), 10, 12,
            "", "!Cure Light!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HEALING
    ),

    gsn_cure_poison(
            "cure poison", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(43), 5, 12,
            "", "!Cure Poison!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_CURATIVE
    ),

    gsn_cure_serious(
            "cure serious", true,

            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(61), 15, 12,
            "", "!Cure Serious!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HEALING
    ),

    gsn_curse(
            "curse", true,

            TAR_OBJ_CHAR_OFF, POS_FIGHTING, SLOT(17), 20, 12,
            "curse", "The curse wears off.",
            "$p is no longer impure.", CABAL_NONE, null,
            ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_cursed_lands(
            "cursed lands", true,

            TAR_IGNORE, POS_STANDING, SLOT(675), 200, 24,
            "", "!cursed lands!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_deadly_venom(
            "deadly venom", true,

            TAR_IGNORE, POS_STANDING, SLOT(674), 200, 24,
            "", "!deadly venom!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_deafen(
            "deafen", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(570), 40, 12,
            "deafen", "The ringing in your ears finally stops.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_demonfire(
            "demonfire", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(505), 20, 12,
            "torments", "!Demonfire!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ATTACK
    ),

    gsn_desert_fist(
            "desert fist", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(681), 50, 12, "desert fist", "!desert fist!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_detect_evil(
            "detect evil", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(18), 5, 12, "", "The red in your vision disappears.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_detect_good(
            "detect good", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(513), 5, 12, "", "The gold in your vision disappears.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_detect_hide_spell(
            "detect hide", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(0), 5, 12,
            "", "You feel less aware of your surroundings.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_detect_invis(
            "detect invis", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(19), 5, 12, "", "You no longer see invisible objects.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_detect_magic(
            "detect magic", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(20), 5, 12, "", "The detect magic wears off.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_detect_poison(
            "detect poison", true,

            TAR_OBJ_INV, POS_STANDING, SLOT(21), 5, 12, "", "!Detect Poison!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_detect_undead(
            "detect undead", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(594), 5, 12, "", "You can't detect undeads anymore.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_disenchant_armor(
            "disenchant armor", true,

            TAR_OBJ_INV, POS_STANDING, SLOT(705), 50, 24, "", "!disenchant armor!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_disenchant_weapon(
            "disenchant weapon", true,

            TAR_OBJ_INV, POS_STANDING, SLOT(706), 50, 24, "", "!disenchant weapon!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_disintegrate(
            "disintegrate", true,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(574), 100, 18, "thin light ray", "!disintegrate!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_dismantle(
            "dismantle", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(621), 200, 24, "", "!621!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_dispel_evil(
            "dispel evil", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(22), 15, 12, "dispel evil", "!Dispel Evil!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ATTACK
    ),

    gsn_dispel_good(
            "dispel good", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(512), 15, 12, "dispel good", "!Dispel Good!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ATTACK
    ),

    gsn_dispel_magic(
            "dispel magic", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(59), 15, 12, "", "!Dispel Magic!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_disruption(
            "disruption", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(648), 20, 12, "disruption", "!disruption!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_dragon_breath(
            "dragon breath", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(563), 75, 12,
            "blast of fire", "!dragon breath!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_dragon_skin(
            "dragon skin", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(612), 50, 24, "", "Your skin becomes softer.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_dragon_strength(
            "dragon strength", true,

            TAR_CHAR_SELF, POS_FIGHTING, SLOT(562), 75, 12,
            "", "You feel the strength of the dragon leave you.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_dragons_breath(
            "dragons breath", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(576), 200, 24, "dragon breath", "Your get healtier again.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_DRACONIAN
    ),

    gsn_drain(
            "drain", true,

            TAR_OBJ_INV, POS_STANDING, SLOT(704), 5, 12, "", "!drain!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_earthfade(
            "earthfade", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(702), 100, 12,
            "", "You slowly fade to your neutral form.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_earthmaw(
            "earthmaw", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(703), 30, 12, "earthmaw", "!earthmaw!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_earthquake(
            "earthquake", true,

            TAR_IGNORE, POS_FIGHTING, SLOT(23), 15, 12, "earthquake", "!Earthquake!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ATTACK
    ),

    gsn_elemental_sphere(
            "elemental sphere", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(719), 75, 12, "", "The protecting elemental sphere around you fades.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_enchant_armor(
            "enchant armor", true,

            TAR_OBJ_INV, POS_STANDING, SLOT(510), 100, 24, "", "!Enchant Armor!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ENCHANTMENT
    ),

    gsn_enchant_weapon(
            "enchant weapon", true,

            TAR_OBJ_INV, POS_STANDING, SLOT(24), 100, 24, "", "!Enchant Weapon!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ENCHANTMENT
    ),

    gsn_energy_drain(
            "energy drain", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(25), 35, 12, "energy drain", "!Energy Drain!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_enhanced_armor(
            "enhanced armor", true,

            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(583), 20, 12,
            "", "You feel yourself unprotected.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_enlarge(
            "enlarge", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(711), 50, 12, "", "You return to your orginal size.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_etheral_fist(
            "etheral fist", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(645), 20, 12, "etheral fist", "!Etheral Fist!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_faerie_fire(
            "faerie fire", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(72), 5, 12,
            "faerie fire", "The pink aura around you fades away.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_WEATHER
    ),

    gsn_faerie_fog(
            "faerie fog", true,

            TAR_IGNORE, POS_STANDING, SLOT(73), 12, 12, "faerie fog", "!Faerie Fog!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEATHER
    ),

    gsn_farsight(
            "farsight", true,

            TAR_IGNORE, POS_STANDING, SLOT(521), 20, 12, "farsight", "!Farsight!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_fear(
            "fear", true,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(598), 50, 12,
            "", "You feel more brave.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ILLUSION
    ),

    gsn_fire_and_ice(
            "fire and ice", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(699), 40, 12, "fire and ice", "!fire and ice!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_fireball(
            "fireball", true,

            TAR_IGNORE, POS_FIGHTING, SLOT(26), 25, 12, "fireball", "!Fireball!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_fireproof(
            "fireproof", true,

            TAR_OBJ_INV, POS_STANDING, SLOT(523), 10, 12, "", "", "$p's protective aura fades.", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ENCHANTMENT
    ),

    gsn_firestream(
            "firestream", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(692), 20, 12, "", "", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_fire_shield(
            "fire shield", true,

            TAR_IGNORE, POS_STANDING, SLOT(601), 200, 24,
            "", "!fire shield!",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_flamestrike(
            "flamestrike", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(65), 20, 12, "flamestrike", "!Flamestrike!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ATTACK
    ),

    gsn_fly(
            "fly", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(56), 10, 18,
            "", "You slowly float to the ground.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_floating_disc(
            "floating disc", true,

            TAR_IGNORE, POS_STANDING, SLOT(522), 40, 24, "", "!Floating disc!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_forcecage(
            "forcecage", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(717), 75, 12, "", "The forcecage around you fades.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_frenzy(
            "frenzy", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(504), 30, 24, "", "Your rage ebbs.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_frostbolt(
            "frostbolt", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(695), 20, 12, "frostbolt", "!frostbolt!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_fumble(
            "fumble", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(712), 25, 18, "", "You speed up and regain your strength!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_galvanic_whip(
            "galvanic whip", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(655), 20, 12, "galvanic whip", "!galvanic whip!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_gate(
            "gate", true,

            TAR_IGNORE, POS_FIGHTING, SLOT(83), 80, 12, "", "!Gate!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_giant_strength(
            "giant strength", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(39), 20, 12,
            "", "You feel weaker.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ENHANCEMENT
    ),

    gsn_grounding(
            "grounding", true,

            TAR_CHAR_SELF, POS_FIGHTING, SLOT(700), 50, 12, "", "You lost your grounding against electricity", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_group_defense(
            "group defense", true,

            TAR_IGNORE, POS_STANDING, SLOT(586), 100, 36, "", "You feel less protected.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_group_heal(
            "group heal", true,

            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(642), 500, 24, "", "!Group Heal!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HEALING
    ),

    gsn_hallucination(
            "hallucination", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(606), 200, 12, "", "You are again defenseless to magic.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_ILLUSION
    ),

    gsn_hand_of_undead(
            "hand of undead", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(647), 20, 24, "hand of undead", "!hand of undead!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_harm(
            "harm", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(27), 35, 12, "harm spell", "!Harm!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HARMFUL
    ),

    gsn_haste(
            "haste", true,

            TAR_CHAR_DEFENSIVE, POS_FIGHTING,
            SLOT(502), 30, 12,
            "", "You feel yourself slow down.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ENHANCEMENT
    ),

    gsn_heal(
            "heal", true,

            TAR_CHAR_DEFENSIVE, POS_FIGHTING,
            SLOT(28), 50, 12,
            "", "!Heal!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HEALING
    ),

    gsn_healing_light(
            "healing light", true,

            TAR_IGNORE, POS_STANDING, SLOT(613), 200, 24, "", "You can light more rooms now.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_heat_metal(
            "heat metal", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(516), 25, 18, "spell", "!Heat Metal!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_helical_flow(
            "helical flow", true,

            TAR_IGNORE, POS_FIGHTING, SLOT(661), 80, 12, "", "!Helical Flow!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_hellfire(
            "hellfire", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(520), 20, 12, "hellfire", "!hellfire!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ATTACK
    ),

    gsn_holy_aura(
            "holy aura", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(617), 75, 12, "", "Your holy aura vanishes.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_holy_fury(
            "holy fury", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(682), 50, 24, "", "You become more tolerable.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_holy_word(
            "holy word", true,

            TAR_IGNORE, POS_FIGHTING, SLOT(506), 200, 24, "divine wrath", "!Holy Word!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_hurricane(
            "hurricane", true,

            TAR_IGNORE, POS_FIGHTING, SLOT(672), 200, 24, "helical flow", "!Hurricane!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_hydroblast(
            "hydroblast", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(684), 50, 12, "water fist", "!Hydroblast!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_iceball(
            "iceball", true,

            TAR_IGNORE, POS_FIGHTING, SLOT(513), 25, 12, "iceball", "!Iceball!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_identify(
            "identify", true,

            TAR_OBJ_INV, POS_STANDING, SLOT(53), 12, 24, "", "!Identify!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_improved_detect(
            "improved detect", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(626), 20, 12, "", "You feel less aware of your surroundings.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_imp_invis(
            "improved invis", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(627), 20, 12,
            "", "You are no longer invisible.",
            "$p fades into view.", CABAL_NONE, null, ALIGN_NONE, GROUP_ILLUSION
    ),

    gsn_infravision(
            "infravision", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(77), 5, 18, "", "You no longer see in the dark.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ENHANCEMENT
    ),

    gsn_insanity(
            "insanity", true,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(616), 100, 24, "", "Now you feel yourself calm down.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_inspire(
            "inspire", true,

            TAR_IGNORE, POS_STANDING, SLOT(587), 75, 24, "", "You feel less inspired", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_invis(
            "invisibility", true,

            TAR_OBJ_CHAR_DEF, POS_STANDING, SLOT(29), 5, 12,
            "", "You are no longer invisible.",
            "$p fades into view.", CABAL_NONE, null, ALIGN_NONE, GROUP_ILLUSION
    ),

    gsn_iron_body(
            "iron body", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(718), 75, 12, "", "The skin regains its softness.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_iron_golem(
            "iron golem", true,

            TAR_IGNORE, POS_STANDING, SLOT(664), 400, 24, "", "You gained enough mana to make more golems now.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_knock(
            "knock", true,

            TAR_IGNORE, POS_STANDING, SLOT(603), 20, 24, "", "!knock!",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_know_alignment(
            "know alignment", true,

            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(58), 9, 12, "", "!Know Alignment!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_lesser_golem(
            "lesser golem", true,

            TAR_IGNORE, POS_STANDING, SLOT(662), 200, 12, "", "You gained enough mana to make more golems now.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_lethargic_mist(
            "lethargic mist", true,

            TAR_IGNORE, POS_STANDING, SLOT(676), 200, 24,
            "", "!lethargic mist!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_light_arrow(
            "light arrow", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(683), 40, 12, "light arrow", "!light arrow!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_lightning_bolt(
            "lightning bolt", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(30), 15, 12,
            "lightning bolt", "!Lightning Bolt!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_lightning_shield(
            "lightning shield", true,

            TAR_IGNORE, POS_STANDING, SLOT(614), 150, 24, "lightning shield", "Now you can shield your room again.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_link(
            "link", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(588), 125, 18, "", "!link!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_MEDITATION
    ),

    gsn_lion_help(
            "lion help", true,

            TAR_IGNORE, POS_STANDING, SLOT(595), 100, 12, "", "Once again, you may send a slayer lion.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_locate_object(
            "locate object", true,

            TAR_IGNORE, POS_STANDING, SLOT(31), 20, 18, "", "!Locate Object!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_love_potion(
            "love potion", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(666), 10, 0,
            "", "You feel less dreamy-eyed.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_magic_jar(
            "magic jar", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(596), 20, 12, "", "!magic jar!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BEGUILING
    ),

    gsn_magic_missile(
            "magic missile", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(32), 15, 12,
            "magic missile", "!Magic Missile!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_magic_resistance(
            "magic resistance", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(605), 200, 24, "", "You are again defenseless to magic.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_magnetic_trust(
            "magnetic trust", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(656), 20, 12, "magnetic trust", "!magnetic trust!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_mass_healing(
            "mass healing", true,

            TAR_IGNORE, POS_STANDING, SLOT(508), 100, 36, "", "!Mass Healing!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HEALING
    ),

    gsn_mass_invis(
            "mass invis", true,

            TAR_IGNORE, POS_STANDING, SLOT(69), 20, 24,
            "", "You are no longer invisible.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ILLUSION
    ),

    gsn_mass_sanctuary(
            "mass sanctuary", true,

            TAR_IGNORE, POS_STANDING, SLOT(589), 200, 24, "", "The white aura around your body fades.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_master_healing(
            "master healing", true,

            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(641), 300, 12, "", "!Master Heal!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HEALING
    ),

    gsn_meld_into_stone(
            "meld into stone", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(584), 12, 18, "", "The stones on your skin crumble into dust.", "",
            CABAL_NONE, Race.ROCKSEER, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_mend(
            "mend", true,

            TAR_OBJ_INV, POS_STANDING, SLOT(590), 150, 24,
            "", "!mend!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ENCHANTMENT
    ),

    gsn_mind_light(
            "mind light", true,

            TAR_IGNORE, POS_STANDING, SLOT(82), 200, 24, "", "You can booster more rooms now.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_mind_wrack(
            "mind wrack", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(650), 20, 12, "mind wrack", "!mind wrack!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_mind_wrench(
            "mind wrench", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(651), 20, 12, "mind wrench", "!mind wrench!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_mist_walk(
            "mist walk", true,

            TAR_IGNORE, POS_FIGHTING, SLOT(658), 80, 12, "", "!Mist Walk!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_mummify(
            "mummify", true,

            TAR_OBJ_CHAR_OFF, POS_STANDING, SLOT(715), 50, 12, "", "You gain energy to give live to new corpses.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_mysterious_dream(
            "mysterious dream", true,

            TAR_IGNORE, POS_STANDING, SLOT(678), 200, 24,
            "", "!mysterous dream!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_BEGUILING
    ),

    gsn_nexus(
            "nexus", true,

            TAR_IGNORE, POS_STANDING, SLOT(520), 150, 36, "", "!Nexus!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_pass_door(
            "pass door", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(74), 20, 12, "", "You feel solid again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_plague(
            "plague", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(503), 20, 12,
            "sickness", "Your sores vanish.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_poison(
            "poison", true,

            TAR_OBJ_CHAR_OFF, POS_FIGHTING, SLOT(33), 10, 12,
            "poison", "You feel less sick.",
            "The poison on $p dries up.", CABAL_NONE,
            null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_polymorph(
            "polymorph", true,

            TAR_IGNORE, POS_STANDING, SLOT(639), 250, 24, "", "You return to your own race.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_portal(
            "portal", true,

            TAR_IGNORE, POS_STANDING, SLOT(519), 100, 24, "", "!Portal!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_protection_cold(
            "protection cold", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(600), 5, 12,
            "", "You feel less protected",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_protection_evil(
            "protection evil", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(34), 5, 12, "", "You feel less protected.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_protection_good(
            "protection good", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(666), 5, 12, "", "You feel less protected.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_protection_heat(
            "protection heat", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(599), 5, 12,
            "", "You feel less protected",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_protection_negative(
            "protection negative", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(636), 20, 12, "", "You feel less protected from your own attacks.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_protective_shield(
            "protective shield", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(572), 70, 12,
            "", "Your shield fades away.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_power_word_kill(
            "power word kill", true,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(604), 200, 18, "powerful word", "You gain back your durability.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_power_stun(
            "power word stun", true,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(625), 200, 24,
            "", "You can move now.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_quantum_spike(
            "quantum spike", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(657), 20, 12, "quantum spike", "!quantum spike!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_ranger_staff(
            "ranger staff", true,

            TAR_IGNORE, POS_FIGHTING, SLOT(519), 75, 0, "", "!ranger staff!", "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_ray_of_truth(
            "ray of truth", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(518), 20, 12,
            "ray of truth", "!Ray of Truth!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_recharge(
            "recharge", true,

            TAR_OBJ_INV, POS_STANDING, SLOT(517), 60, 24, "", "!Recharge!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ENCHANTMENT
    ),

    gsn_refresh(
            "refresh", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(81), 12, 18, "refresh", "!Refresh!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HEALING
    ),

    gsn_reincarnation(
            "reincarnation", true,

            TAR_IGNORE, POS_STANDING, SLOT(668), 0, 0, "", "!!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_remove_curse(
            "remove curse", true,

            TAR_OBJ_CHAR_DEF, POS_STANDING, SLOT(35), 5, 12,
            "", "!Remove Curse!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_CURATIVE
    ),

    gsn_remove_fear(
            "remove fear", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(582), 5, 12, "", "!Remove Fear!", "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_remove_tattoo(
            "remove tattoo", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(552), 10, 0, "", "!remove tattoo!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_resilience(
            "resilience", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(638), 50, 12, "", "You feel less armored to draining attacks.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_restoring_light(
            "restoring light", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(643), 50, 24, "", "!restoring light!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_sanctify_lands(
            "sanctify lands", true,

            TAR_IGNORE, POS_STANDING, SLOT(673), 200, 24, "", "!sanctify lands!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_sanctuary(
            "sanctuary", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(36), 75, 12,
            "", "The white aura around your body fades.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_sand_storm(
            "sand storm", true,

            TAR_IGNORE, POS_FIGHTING, SLOT(577), 200, 24,
            "storm of sand", "The sands melts in your eyes.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_scream(
            "scream", true,

            TAR_IGNORE, POS_FIGHTING, SLOT(578), 200, 24,
            "scream", "You can hear again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_severity_force(
            "severity force", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(622), 20, 12, "severity force", "!severity force!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_shield(
            "shield", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(67), 12, 18,
            "", "Your force shield shimmers then fades away.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_shielding(
            "shielding", true,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(591), 250, 12,
            "", "You feel the glow of the true source in the distance",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_shocking_grasp(
            "shocking grasp", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(53), 15, 12, "shocking grasp", "!Shocking Grasp!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_shocking_trap(
            "shocking trap", true,

            TAR_IGNORE, POS_STANDING, SLOT(615), 150, 24, "shocking trap", "Now you can trap more rooms with shocks.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_sleep(
            "sleep", true,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(38), 15, 12,
            "", "You feel less tired.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BEGUILING
    ),

    gsn_slow(
            "slow", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(515), 30, 12,
            "", "You feel yourself speed up.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_soften(
            "soften", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(713), 75, 12, "soften", "Your skin regains its robustness.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_WEATHER
    ),

    gsn_solar_flight(
            "solar flight", true,

            TAR_IGNORE, POS_FIGHTING, SLOT(659), 80, 12, "", "!Solar Flight!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_sonic_resonance(
            "sonic resonance", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(649), 20, 12, "sonic resonance", "!sonic resonance!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_soul_bind(
            "soul bind", true,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(716), 5, 12, "", "You feel more self-confident.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_BEGUILING
    ),

    gsn_spectral_furor(
            "spectral furor", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(646), 20, 12, "spectral furor", "!spectral furor!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_stone_golem(
            "stone golem", true,

            TAR_IGNORE, POS_STANDING, SLOT(663), 300, 18, "", "You gained enough mana to make more golems now.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_stone_skin(
            "stone skin", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(66), 12, 18, "", "Your skin feels soft again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_suffocate(
            "suffocate", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(714), 50, 12, "breathlessness", "You can breath again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_sulfurus_spray(
            "sulfurus spray", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(652), 20, 12, "sulfurus spray", "!sulfurus spray!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_summon(
            "summon", true,

            TAR_IGNORE, POS_STANDING, SLOT(40), 50, 12, "", "!Summon!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_summon_air_elemental(
            "summon air elemental", true,

            TAR_IGNORE, POS_STANDING, SLOT(696), 50, 12, "", "You gain back the energy to summon another air elemental.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_summon_earth_elemental(
            "summon earth elemental", true,

            TAR_IGNORE, POS_STANDING, SLOT(693), 50, 12, "", "You gain back the energy to summon another earth elemental.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_summon_fire_elemental(
            "summon fire elemental", true,

            TAR_IGNORE, POS_STANDING, SLOT(697), 50, 12, "", "You gain back the energy to summon another fire elemental.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_summon_lightning_elemental(
            "summon lightning elemental", true,

            TAR_IGNORE, POS_STANDING, SLOT(710), 50, 12, "", "You gain back the energy to summon another lightning elemental.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_summon_water_elemental(
            "summon water elemental", true,

            TAR_IGNORE, POS_STANDING, SLOT(698), 50, 12, "", "You gain back the energy to summon another water elemental.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_summon_shadow(
            "summon shadow", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(620), 200, 24, "", "You can summon more shadows, now.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_superior_heal(
            "superior heal", true,

            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(640), 100, 12, "", "!Super Heal!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HEALING
    ),

    gsn_tattoo(
            "tattoo", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(551), 10, 0, "", "!tattoo!", "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_teleport(
            "teleport", true,

            TAR_CHAR_SELF, POS_FIGHTING, SLOT(2), 35, 12,
            "", "!Teleport!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_transfer_object(
            "transfer object", true,

            TAR_IGNORE, POS_STANDING, SLOT(708), 40, 12, "", "!transfer object!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_tsunami(
            "tsunami", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(701), 50, 12, "raging tidal wave", "!tsunami!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_turn(
            "turn", true,

            TAR_IGNORE, POS_FIGHTING, SLOT(597), 50, 12, "", "You can handle turn spell again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_vampiric_blast(
            "vampiric blast", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(611), 20, 12, "vampiric blast", "!Vampiric Blast!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_ventriloquate(
            "ventriloquate", true,

            TAR_IGNORE, POS_STANDING, SLOT(41), 5, 12, "", "!Ventriloquate!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ILLUSION
    ),

    gsn_web(
            "web", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(592), 50, 12,
            "", "The webs around you dissolve.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_ATTACK
    ),

    gsn_windwall(
            "windwall", true,

            TAR_IGNORE, POS_FIGHTING, SLOT(694), 20, 12, "air blast", "Your eyes feel better.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_witch_curse(
            "witch curse", true,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(602), 150, 24,
            "", "You gain back your durability.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_wrath(
            "wrath", true,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(553), 20, 12,
            "heavenly wrath", "The curse wears off.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_weaken(
            "weaken", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(68), 20, 12,
            "spell", "You feel stronger.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_word_of_recall(
            "word of recall", true,

            TAR_CHAR_SELF, POS_RESTING, SLOT(42), 5, 12, "", "!Word of Recall!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

/*
 * Dragon breath
**/

    gsn_acid_breath(
            "acid breath", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(200), 100, 24,
            "blast of acid", "!Acid Breath!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),

    gsn_desert_heat(
            "desert heat", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(629), 200, 24, "cloud of blistering desert heat", "The smoke leaves your eyes.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),

    gsn_fire_breath(
            "fire breath", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(201), 200, 24,
            "blast of flame", "The smoke leaves your eyes.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),

    gsn_frost_breath(
            "frost breath", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(202), 125, 24,
            "blast of frost", "!Frost Breath!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),

    gsn_gas_breath(
            "gas breath", true,

            TAR_IGNORE, POS_FIGHTING, SLOT(203), 175, 24,
            "blast of gas", "!Gas Breath!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),

    gsn_lightning_breath(
            "lightning breath", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(204), 150, 24,
            "blast of lightning", "!Lightning Breath!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),

    gsn_lightning_stroke(
            "lightning stroke", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(632), 200, 24, "stroke of lightning", "!lightning stroke!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),

    gsn_luck_bonus(
            "luck bonus", true,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(630), 20, 12, "", "You feel less armored against magic.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_paralyzation(
            "paralyzation", true,

            TAR_IGNORE, POS_FIGHTING, SLOT(631), 200, 24, "gas of paralyzation", "You feel you can move again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),

    gsn_repulsion(
            "repulsion", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(633), 200, 24, "repulsion", "!repulsion!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),

    gsn_sleep_gas(
            "sleep gas", true,

            TAR_IGNORE, POS_FIGHTING, SLOT(628), 200, 24, "sleep gas", "You feel drained.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),

    gsn_slow_gas(
            "slow gas", true,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(634), 200, 24, "slow gas", "You can move faster now.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),

    /*
     * ActSkill for mobiles. (general purpose and high explosive from
     * Glop/Erkenbrand
    **/
    gsn_crush(
            "crush", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 18,
            "crush", "!crush!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_general_purpose(
            "general purpose", false,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(401), 0, 12, "general purpose ammo", "!General Purpose Ammo!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_high_explosive(
            "high explosive", false,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(402), 0, 12, "high explosive ammo", "!High Explosive Ammo!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_tail(
            "tail", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 18,
            "tail", "!Tail!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

/* combat and weapons skills*/

    gsn_arrow(
            "arrow", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "arrow", "!arrow!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_axe(
            "axe", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Axe!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_bow(
            "bow", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "bow", "!bow!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_dagger(
            "dagger", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Dagger!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_flail(
            "flail", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Flail!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_lance(
            "lance", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "lance", "!lance!", "",
            CABAL_KNIGHT, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_mace(
            "mace", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Mace!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_polearm(
            "polearm", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Polearm!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_shield_block(
            "shield block", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Shield!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DEFENSIVE
    ),

    gsn_spear(
            "spear", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 12,
            "spear", "!Spear!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_sword(
            "sword", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!sword!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_whip(
            "whip", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Whip!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_second_weapon(
            "second weapon", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!second weapon!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_ambush(
            "ambush", false,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(0), 0, 12,
            "surprise attack", "!Ambush!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_area_attack(
            "area attack", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Area Attack!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_assassinate(
            "assassinate", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 18,
            "assassination attempt", "!assassinate!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_backstab(
            "backstab", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "backstab", "!Backstab!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_bash(
            "bash", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 18,
            "bash", "!Bash!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_bash_door(
            "bash door", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 18,
            "bash", "!Bash Door!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_bear_call(
            "bear call", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(518), 50, 0,
            "", "You feel you can handle more bears now.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_berserk(
            "berserk", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 24,
            "", "You feel your pulse slow down.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_blackguard(
            "blackguard", false,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(0), 0, 12,
            "blackguard", "Your blackguard fades away.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_blackjack(
            "blackjack", false,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(0), 0, 8,
            "blackjack", "Your head feels better.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_blind_fighting(
            "blind fighting", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "!blind fighting!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_blindness_dust(
            "blindness dust", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 20, 18,
            "", "!blindness dust!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_blink(
            "blink", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Blink!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_butcher(
            "butcher", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "!butcher!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_caltraps(
            "caltraps", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 18,
            "caltraps", "Your feet feel less sore.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_camouflage(
            "camouflage", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 24,
            "", "!Camouflage!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_move_camf(
            "camouflage move", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "", "!move camouflaged!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_camp(
            "camp", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 24,
            "camp", "You can handle more camps now.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_circle(
            "circle", false,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(0), 0, 18,
            "circle stab", "!Circle!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_control_animal(
            "control animal", false,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(0), 5, 12,
            "", "You feel more self-confident.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BEGUILING
    ),

    gsn_cleave(
            "cleave", false,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(0), 0, 24,
            "cleave", "!Cleave!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_concentrate(
            "concentrate", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 18,
            "", "You can concentrate on new fights.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_counter(
            "counter", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "!Counter!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_critical(
            "critical strike", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 18,
            "", "!critical strike!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_cross_block(
            "cross block", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!cross block!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DEFENSIVE
    ),

    gsn_detect_hidden(
            "detect hidden", false,

            TAR_CHAR_SELF, POS_STANDING, SLOT(44), 5, 12, "", "You feel less aware of your surroundings.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_detect_sneak(
            "detect sneak", false,

            TAR_CHAR_SELF, POS_STANDING, SLOT(0), 20, 18,
            "", "!detect sneak!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_dirt(
            "dirt kicking", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 12,
            "kicked dirt", "You rub the dirt out of your eyes.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_disarm(
            "disarm", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 18,
            "", "!Disarm!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_dodge(
            "dodge", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Dodge!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DEFENSIVE
    ),

    gsn_dual_backstab(
            "dual backstab", false,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(0), 0, 0,
            "second backstab", "!dual backstab!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_enchant_sword(
            "enchant sword", false,

            TAR_OBJ_INV, POS_STANDING, SLOT(0), 100, 24,
            "", "!Enchant sword!", "", CABAL_NONE, null,
            ALIGN_NONE, GROUP_NONE
    ),

    gsn_endure(
            "endure", false,

            TAR_CHAR_SELF, POS_FIGHTING, SLOT(0), 0, 24,
            "", "You feel susceptible to magic again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_enhanced_damage(
            "enhanced damage", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Enhanced Damage!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_entangle(
            "entangle", false,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(568), 40, 12,
            "entanglement", "You feel less entangled.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_envenom(
            "envenom", false,

            TAR_IGNORE, POS_RESTING, SLOT(0), 0, 12,
            "", "!Envenom!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_escape(
            "escape", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 24,
            "", "!escape!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_explode(
            "explode", false,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(0), 100, 24,
            "flame", "The smoke leaves your eyes.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_ground_strike(
            "ground strike", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 18,
            "", "!ground strike!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_hand_block(
            "hand block", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!hand block!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DEFENSIVE
    ),

    gsn_hand_to_hand(
            "hand to hand", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Hand to Hand!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_hara_kiri(
            "hara kiri", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 50, 12,
            "", "You feel you gain your life again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_headguard(
            "headguard", false,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(0), 0, 12,
            "headguard", "Your headguard fades away.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_herbs(
            "herbs", false,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(0), 0, 30,
            "", "The herbs look more plentiful here.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_kick(
            "kick", false,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(0), 0, 12,
            "kick", "!Kick!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_lash(
            "lash", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 4,
            "lash", "!Lash!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_light_res(
            "light resistance", false,

            TAR_IGNORE, POS_SLEEPING, SLOT(0), 0, 0,
            "", "Light Resistance", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_lion_call(
            "lion call", false,

            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(0), 50, 12,
            "", "!lion call!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_make_arrow(
            "make arrow", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 50, 24,
            "", "!make arrow!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_make_bow(
            "make bow", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 200, 24,
            "", "!make bow!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_mental_attack(
            "mental attack", false,

            TAR_CHAR_SELF, POS_STANDING, SLOT(0), 200, 24,
            "", "!mental attack!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_neckguard(
            "neckguard", false,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(0), 0, 12,
            "neckguard", "Your neckguard fades away.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_nerve(
            "nerve", false,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(0), 0, 18,
            "", "Your nerves feel better.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_parry(
            "parry", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Parry!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DEFENSIVE
    ),

    gsn_perception(
            "perception", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "!perception!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_push(
            "push", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 18,
            "push", "!push!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_rescue(
            "rescue", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 12,
            "", "!Rescue!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DEFENSIVE
    ),

    gsn_sense_life(
            "sense life", false,

            TAR_CHAR_SELF, POS_STANDING, SLOT(623), 20, 12,
            "", "You lost the power to sense life.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_settraps(
            "settraps", false,

            TAR_CHAR_SELF, POS_STANDING, SLOT(0), 200, 24,
            "trap", "You can set more traps now.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_shield_cleave(
            "shield cleave", false,

            TAR_CHAR_SELF, POS_STANDING, SLOT(0), 200, 24,
            "", "!shield cleave!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_smithing(
            "smithing", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 10, 18,
            "", "!smithing!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_spell_craft(
            "spell craft", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "spell craft", "!spell craft!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_strangle(
            "strangle", false,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(0), 0, 8,
            "strangulation", "Your neck feels better.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_swimming(
            "swimming", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "!swimming!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_target(
            "target", false,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(0), 0, 12,
            "target", "!Kick!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_throw(
            "throw", false,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(0), 0, 18,
            "throw", "!throw!", "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_tiger_power(
            "tiger power", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 12,
            "", "You feel your tigers escaped.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_track(
            "track", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "", "!track!", "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_trip(
            "trip", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 12,
            "trip", "!Trip!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_vampire(
            "vampire", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 100, 12,
            "", "Now you are familer to other creatures.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_vampiric_bite(
            "vampiric bite", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "vampiric bite", "!vampiric bite!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_vampiric_touch(
            "vampiric touch", false,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(0), 0, 12,
            "vampiric touch", "You wake up from nightmares.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_vanish(
            "vanish", false,

            TAR_CHAR_SELF, POS_FIGHTING, SLOT(521), 25, 18,
            "", "!vanish!", "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_warcry(
            "warcry", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 30, 12,
            "", "Your warcry has worn off.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_weapon_cleave(
            "weapon cleave", false,

            TAR_CHAR_SELF, POS_STANDING, SLOT(0), 200, 24,
            "", "!weapon cleave!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_second_attack(
            "second attack", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Second Attack!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_secondary_attack(
            "secondary attack", false,

            TAR_CHAR_SELF, POS_STANDING, SLOT(0), 200, 24,
            "", "!secondary attack!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_third_attack(
            "third attack", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Third Attack!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_fourth_attack(
            "fourth attack", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Fourth Attack!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_fifth_attack(
            "fifth attack", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Fifth Attack!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

/* non-combat skills*/

    gsn_blue_arrow(
            "blue arrow", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 50, 12,
            "", "!blue arrow!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_fast_healing(
            "fast healing", false,

            TAR_IGNORE, POS_SLEEPING, SLOT(0), 0, 0,
            "", "!Fast Healing!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_MEDITATION
    ),

    gsn_green_arrow(
            "green arrow", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 50, 12,
            "", "!green arrow!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_grip(
            "grip", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 18,
            "", "!Grip!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_haggle(
            "haggle", false,

            TAR_IGNORE, POS_RESTING, SLOT(0), 0, 0,
            "", "!Haggle!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_hide(
            "hide", false,

            TAR_IGNORE, POS_RESTING, SLOT(0), 0, 18,
            "", "!Hide!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_katana(
            "katana", false,

            TAR_OBJ_INV, POS_STANDING, SLOT(0), 100, 24,
            "", "You can now make another katana.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_lay_hands(
            "lay hands", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 12,
            "", "You may heal more innocents now.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_lore(
            "lore", false,

            TAR_IGNORE, POS_RESTING, SLOT(0), 0, 36,
            "", "!Lore!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_master_hand(
            "mastering pound", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Master Hand!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_master_sword(
            "mastering sword", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!master sword!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_meditation(
            "meditation", false,

            TAR_IGNORE, POS_SLEEPING, SLOT(0), 0, 0,
            "", "Meditation", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_MEDITATION
    ),

    gsn_peek(
            "peek", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "!Peek!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_pick_lock(
            "pick lock", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "", "!Pick!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_poison_smoke(
            "poison smoke", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(0), 20, 18,
            "", "!poison smoke!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_quiet_movement(
            "quiet movement", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "!quiet movement!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_recall(
            "recall", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 24,
            "", "!Recall!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_red_arrow(
            "red arrow", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 50, 12,
            "", "!red arrow!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_sneak(
            "sneak", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "", "You no longer feel stealthy.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_steal(
            "steal", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "", "!Steal!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_scrolls(
            "scrolls", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "", "!Scrolls!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WIZARD
    ),

    gsn_staves(
            "staves", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "", "!Staves!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WIZARD
    ),

    gsn_tame(
            "tame", false,

            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(0), 0, 24,
            "", "!tame!", "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_trance(
            "trance", false,

            TAR_IGNORE, POS_SLEEPING, SLOT(0), 0, 0,
            "", "", "", CABAL_NONE, null, ALIGN_NONE, GROUP_MEDITATION
    ),

    gsn_white_arrow(
            "white arrow", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 50, 12,
            "", "!white arrow!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_wands(
            "wands", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "", "!Wands!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WIZARD
    ),

/* cabals*/

    gsn_mortal_strike(
            "mortal strike", false,

            TAR_CHAR_SELF, POS_STANDING, SLOT(0), 200, 24,
            "mortal strike", "!mortal strike!", "",
            CABAL_BATTLE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_disgrace(
            "disgrace", false,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(619), 200, 12, "", "You feel yourself getting prouder.", "",
            CABAL_CHAOS, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_evil_spirit(
            "evil spirit", false,

            TAR_IGNORE, POS_STANDING, SLOT(618), 800, 36,
            "evil spirit", "Your body regains its full spirit.", "",
            CABAL_INVADER, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_ruler_aura(
            "ruler aura", false,

            TAR_CHAR_SELF, POS_STANDING, SLOT(667), 20, 12, "", "Your ruler aura fades.", "", CABAL_RULER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_sword_of_justice(
            "sword of justice", false,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(686), 50, 12, "sword of justice", "!sword of justice!", "", CABAL_RULER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_bandage(
            "bandage", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "You feel less healthy.", "", CABAL_BATTLE,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_cabal_recall(
            "cabal recall", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 24,
            "", "You may pray for transportation again.", "", CABAL_BATTLE,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_wanted(
            "wanted", false,

            TAR_IGNORE, POS_DEAD, SLOT(0), 0, 0,
            "", "!Wanted!", "", CABAL_RULER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_judge(
            "judge", false,

            TAR_IGNORE, POS_DEAD, SLOT(0), 0, 0,
            "", "!Judge!", "", CABAL_RULER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_bloodthirst(
            "bloodthirst", false,

            TAR_CHAR_SELF, POS_FIGHTING, SLOT(0), 0, 12,
            "", "Your bloody rage fades away.", "", CABAL_BATTLE,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_spellbane(
            "spellbane", false,

            TAR_CHAR_SELF, POS_STANDING, SLOT(0), 0, 12,
            "spellbane", "You feel less resistant to magic.", "", CABAL_BATTLE,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_resistance(
            "resistance", false,

            TAR_CHAR_SELF, POS_FIGHTING, SLOT(0), 0, 24,
            "", "You feel less tough.", "", CABAL_BATTLE,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_deathblow(
            "deathblow", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "!deathblow!", "", CABAL_BATTLE,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_transform(
            "transform", false,

            TAR_CHAR_SELF, POS_STANDING, SLOT(522), 100, 24, "", "You feel less healthy.", "", CABAL_SHALAFI,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_mental_knife(
            "mental knife", false,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(524), 35, 12,
            "mental knife", "Your mental pain dissipates.", "", CABAL_SHALAFI,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_demon_summon(
            "demon summon", false,

            TAR_CHAR_SELF, POS_FIGHTING, SLOT(525), 100, 12,
            "", "You feel your summoning power return.", "", CABAL_SHALAFI,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_scourge(
            "scourge", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(526), 50, 18, "Scourge of the Violet Spider", "!scourge!", "", CABAL_SHALAFI,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_manacles(
            "manacles", false,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(528), 75, 12, "", "Your shackles dissolve.", "", CABAL_RULER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_shield_of_ruler(
            "shield of ruler", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(529), 100, 12, "", "!shield!", "", CABAL_RULER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_guard(
            "guard", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "", "", "", CABAL_KNIGHT,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_guard_call(
            "guard call", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(530), 75, 12,
            "", "You may call more guards now.", "", CABAL_RULER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_nightwalker(
            "nightwalker", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(531), 75, 12,
            "", "You feel your summoning power return.", "", CABAL_INVADER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_eyes_of_intrigue(
            "eyes of intrigue", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(532), 75, 12, "", "!eyes of intrigue!", "", CABAL_INVADER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_fade(
            "fade", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 24,
            "", "!fade!", "", CABAL_INVADER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_shadow_cloak(
            "shadow cloak", false,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(533), 10, 12, "", "The shadows no longer protect you.", "", CABAL_INVADER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_nightfall(
            "nightfall", false,

            TAR_IGNORE, POS_STANDING, SLOT(534), 50, 12, "", "You are now able to control lights.", "", CABAL_INVADER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_aura_of_chaos(
            "aura of chaos", false,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(720), 20, 12, "", "The gods of chaos no longer protect you.", "",
            CABAL_CHAOS, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_garble(
            "garble", false,

            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(535), 30, 12,
            "", "Your tongue untwists.", "", CABAL_CHAOS,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_mirror(
            "mirror", false,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(536), 40, 12,
            "", "You fade away.", "", CABAL_CHAOS,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_confuse(
            "confuse", false,

            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(537), 20, 12,
            "", "You feel less confused.", "", CABAL_CHAOS,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_doppelganger(
            "doppelganger", false,

            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(527), 75, 12,
            "", "You return to your native form.", "", CABAL_CHAOS,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_chaos_blade(
            "chaos blade", false,

            TAR_IGNORE, POS_STANDING, SLOT(550), 60, 12,
            "", "!chaos blade!", "", CABAL_CHAOS,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_stalker(
            "stalker", false,

            TAR_IGNORE, POS_STANDING, SLOT(554), 100, 12,
            "", "You feel up to summoning another stalker.", "",
            CABAL_RULER, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_randomizer(
            "randomizer", false,

            TAR_IGNORE, POS_STANDING, SLOT(555), 200, 24,
            "", "You feel your randomness regenerating.", "", CABAL_CHAOS,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_tesseract(
            "tesseract", false,

            TAR_IGNORE, POS_STANDING, SLOT(556), 150, 12,
            "", "!tesseract!", "", CABAL_SHALAFI,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_trophy(
            "trophy", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 30, 12,
            "", "You feel up to making another trophy.", "", CABAL_BATTLE,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_truesight(
            "truesight", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 50, 12,
            "", "Your eyes see less truly.", "", CABAL_BATTLE,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_brew(
            "brew", false,

            TAR_OBJ_INV, POS_STANDING, SLOT(557), 25, 12,
            "", "You feel like you can start brewing again.", "",
            CABAL_SHALAFI, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_shadowlife(
            "shadowlife", false,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(558), 80, 12,
            "", "Your feel more shadowy.", "", CABAL_INVADER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_ruler_badge(
            "ruler badge", false,

            TAR_CHAR_SELF, POS_STANDING, SLOT(560), 50, 12,
            "", "!ruler badge!", "", CABAL_RULER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_remove_badge(
            "remove badge", false,

            TAR_CHAR_SELF, POS_STANDING, SLOT(561), 100, 12,
            "", "!remove badge!", "", CABAL_RULER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_golden_aura(
            "golden aura", false,

            TAR_IGNORE, POS_STANDING, SLOT(564), 25, 12, "", "You feel the golden aura dissipate.", "", CABAL_KNIGHT,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_dragonplate(
            "dragonplate", false,

            TAR_IGNORE, POS_STANDING, SLOT(565), 60, 12, "", "", "", CABAL_KNIGHT,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_squire(
            "squire", false,

            TAR_IGNORE, POS_STANDING, SLOT(566), 100, 12,
            "", "You feel up to worrying about a new squire.", "", CABAL_KNIGHT,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_dragonsword(
            "dragonsword", false,

            TAR_IGNORE, POS_STANDING, SLOT(567), 70, 12,
            "", "", "", CABAL_KNIGHT, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_knight(
            "holy armor", false,

            TAR_CHAR_SELF, POS_RESTING, SLOT(569), 20, 12,
            "", "You are less protected from harm.", "", CABAL_KNIGHT,
            null, ALIGN_NONE, GROUP_CABAL
    ),


    gsn_disperse(
            "disperse", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(573), 100, 24,
            "", "You feel up to doing more dispersing.", "", CABAL_CHAOS,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_hunt(
            "hunt", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 6,
            "", "!hunt!", "", CABAL_HUNTER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_find_object(
            "find object", false,

            TAR_IGNORE, POS_STANDING, SLOT(585), 20, 18, "", "!Find Object!", "", CABAL_HUNTER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_path_find(
            "path find", false,

            TAR_IGNORE, POS_SLEEPING, SLOT(0), 0, 0,
            "", "!endur!", "", CABAL_HUNTER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_riding(
            "riding", false,

            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 6,
            "", "!riding!", "", CABAL_KNIGHT,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_wolf(
            "wolf", false,

            TAR_IGNORE, POS_STANDING, SLOT(593), 100, 12, "", "You feel you can handle more wolfs now.", "", CABAL_HUNTER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_wolf_spirit(
            "wolf spirit", false,

            TAR_CHAR_SELF, POS_STANDING, SLOT(685), 50, 12, "", "The blood in your vains start to flow as normal.", "",
            CABAL_HUNTER, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_armor_use(
            "armor use", false,
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Armor Use!", "", CABAL_HUNTER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_world_find(
            "world find", false,
            TAR_IGNORE, POS_SLEEPING, SLOT(0), 0, 0,
            "", "!world find!", "", CABAL_HUNTER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_take_revenge(
            "take revenge", false,

            TAR_IGNORE, POS_STANDING, SLOT(624), 50, 12, "", "!take revenge!", "",
            CABAL_HUNTER, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_mastering_spell(
            "mastering spell", false,
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "mastering spell", "!mastering spell!", "",
            CABAL_SHALAFI, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_guard_dogs(
            "guard dogs", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(687), 100, 12, "", "", "",
            CABAL_LIONS, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_eyes_of_tiger(
            "eyes of tiger", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(688), 20, 12, "", "", "",
            CABAL_LIONS, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_lion_shield(
            "lion shield", false,

            TAR_IGNORE, POS_FIGHTING, SLOT(689), 200, 12, "", "", "",
            CABAL_LIONS, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_evolve_lion(
            "evolve lion", false,
            TAR_IGNORE, POS_FIGHTING, SLOT(690), 50, 12, "", "", "",
            CABAL_LIONS, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_claw(
            "claw", false,
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 50, 24,
            "claw", "", "",
            CABAL_LIONS, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_prevent(
            "prevent", false,
            TAR_IGNORE, POS_FIGHTING, SLOT(691), 75, 12, "", "", "",
            CABAL_LIONS, null, ALIGN_NONE, GROUP_CABAL
    ),

/* object spells*/

    gsn_terangreal(
            "terangreal", true,

            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(607), 5, 12,
            "terangreal", "You are awake again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_kassandra(
            "kassandra", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(608), 5, 12,
            "", "You can heal yourself again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_sebat(
            "sebat", true,

            TAR_CHAR_SELF, POS_STANDING, SLOT(609), 5, 12,
            "", "You can protect yourself again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_matandra(
            "matandra", true,
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(610), 5, 12,
            "holy word", "You can use kassandra again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_demand(
            "demand", false,
            TAR_IGNORE, POS_STANDING, SLOT(0), 5, 12,
            "", "", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_bury(
            "bury", false,
            TAR_IGNORE, POS_STANDING, SLOT(0), 5, 12,
            "", "", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_x_hit(
            "gsn_x_hit", false,
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),
    gsn_x_hunger(
            "gsn_x_hunger", false,
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    );


    static int MAX_SKILLS = Skill.values().length;

    final String name;           /* Name of skill        */
    final int skill_level[] = new int[MAX_CLASS]; /* Level needed by class    */
    final int rating[] = new int[MAX_CLASS];  /* How hard it is to learn  */
    public int[] mod = new int[MAX_CLASS];
    final int target;         /* Legal targets        */
    final int minimum_position;   /* Position for caster / user   */
    final int slot;           /* Slot for #OBJECT loading */
    final int min_mana;       /* Minimum mana used        */
    final int beats;          /* Waiting time after use   */
    final String noun_damage;        /* Damage message       */
    final String msg_off;        /* Wear off message     */
    final String msg_obj;        /* Wear off message for obects  */
    final int cabal;          /* Cabal spells         */
    Race race;           /* Race spells          */
    final int align;          /* alignment of spells      */
    final int group;          /* skill group for practicing   */
    final boolean is_spell;
    /**
     * list of all skills. do not modify this list
     */
    static final Skill[] skills = Skill.values();
    private static final Map<String, Skill> skillMap = new HashMap<String, Skill>();

    static {
        for (Skill skill : skills) {
            skillMap.put(skill.name, skill);
        }
    }

    Skill(String name, boolean isSpell, int target, int minimum_position, int slot, int min_mana, int beats, String noun_damage, String msg_off, String msg_obj, int cabal, Race race, int align, int group) {
        this.name = name;
        this.target = target;
        this.minimum_position = minimum_position;
        this.slot = slot;
        this.min_mana = min_mana;
        this.beats = beats;
        this.noun_damage = noun_damage;
        this.msg_off = msg_off;
        this.msg_obj = msg_obj;
        this.cabal = cabal;
        this.race = race;
        this.align = align;
        this.group = group;
        this.is_spell = isSpell;
        Arrays.fill(skill_level, 100);
    }

    static int SLOT(int n) {
        return n;
    }

    void spell_fun(int level, Nightworks.CHAR_DATA ch, Object vo, int target) {
        //TODO:
    }

    static int skill_num_lookup(String name) {
        Skill skill = lookupSkill(name);
        return skill == null ? -1 : skill.ordinal();
    }

    /*
    * Lookup a skill by name.
    */
    static Skill lookupSkill(String name) {
        if (name.length() == 0) {
            return null;
        }
        return lookupSkill(name, false);
    }

    static Skill lookupSkill(String name, boolean throwExceptionIfNotFound) {
        Skill s = skillMap.get(name);
        if (s != null) {
            return s;
        }
        Skill[] skills = Skill.skills;
        for (Skill sn : skills) {
            if (!str_prefix(name, sn.name)) {
                return sn;
            }
        }
        if (throwExceptionIfNotFound) {
            throw new RuntimeException("Skill not found:" + name);
        }
        return null;
    }

    static Skill find_spell(CHAR_DATA ch, String name) {
        /* finds a spell the character can cast if possible */

        if (IS_NPC(ch)) {
            return lookupSkill(name);
        }

        Skill found = null;
        Skill[] skills = Skill.skills;
        for (Skill sn : skills) {
            if (!str_prefix(name, sn.name)) {
                if (found == null) {
                    found = sn;
                }
                if (skill_failure_nomessage(ch, sn, 0) == 0) {
                    return sn;
                }
            }
        }
        return found;
    }
}
