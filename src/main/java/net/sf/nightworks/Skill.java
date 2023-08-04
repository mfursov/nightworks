package net.sf.nightworks;

import net.sf.nightworks.util.NotNull;

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
            "gsn_reserved",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    /*
     * Magic spells.
     */
    gsn_absorb(
            "absorb", Magic::spell_absorb,
            TAR_CHAR_SELF, POS_STANDING, SLOT(707), 100, 12,
            "", "The energy field around you fades!",
            "$p's energy field fades.",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_acetum_primus(
            "acetum primus", Magic::spell_acetum_primus,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(654), 20, 12, "acetum primus", "!acetum primus!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_acid_arrow(
            "acid arrow", Magic::spell_acid_arrow,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(644), 20, 12, "acid arrow", "!Acid Arrow!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_acid_blast(
            "acid blast", Magic::spell_acid_blast,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(70), 40, 12,
            "acid blast", "!Acid Blast!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_acute_vision(
            "acute vision", Magic2::spell_acute_vision,
            TAR_CHAR_SELF, POS_STANDING, SLOT(514), 10, 0, "", "Your vision seems duller.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_adamantite_golem(
            "adamantite golem", Magic2::spell_adamantite_golem,
            TAR_IGNORE, POS_STANDING, SLOT(665), 500, 30, "", "You gained enough mana to make more golems now.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_aid(
            "aid", Magic2::spell_aid,
            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(680), 100, 12, "", "You can aid more people.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_HEALING
    ),

    gsn_amnesia(
            "amnesia", Magic2::spell_amnesia,
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(538), 100, 12, "", "!amnesia!", "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_animate_dead(
            "animate dead", Magic2::spell_animate_dead,
            TAR_OBJ_CHAR_OFF, POS_STANDING, SLOT(581), 50, 12, "", "You gain energy to animate new deads.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_animate_object(
            "animate object", Magic::spell_animate_object,
            TAR_OBJ_CHAR_OFF, POS_STANDING, SLOT(709), 50, 12, "", "You gain energy to animate new objects.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_armor(
            "armor", Magic::spell_armor,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(1), 5, 12, "", "You feel less armored.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_assist(
            "assist", Magic2::spell_assist,
            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(670), 100, 12, "", "You can assist more, now.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HEALING
    ),

    gsn_astral_walk(
            "astral walk", Magic::spell_astral_walk,
            TAR_IGNORE, POS_FIGHTING, SLOT(622), 80, 12, "", "!Astral Walk!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_attract_other(
            "attract other", Magic2::spell_attract_other,
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(580), 5, 12, "", "You feel your master leaves you.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BEGUILING
    ),

    gsn_bark_skin(
            "bark skin", Magic2::spell_bark_skin,
            TAR_CHAR_SELF, POS_STANDING, SLOT(515), 40, 0, "", "The bark on your skin flakes off.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_black_death(
            "black death", Magic2::spell_black_death,
            TAR_IGNORE, POS_STANDING, SLOT(677), 200, 24,
            "", "!black death!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_blade_barrier(
            "blade barrier", Magic2::spell_blade_barrier,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(679), 40, 12, "blade barrier", "!Blade Barrier!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ATTACK
    ),

    gsn_bless(
            "bless", Magic::spell_bless,
            TAR_OBJ_CHAR_DEF, POS_STANDING, SLOT(3), 5, 12,
            "", "You feel less righteous.",
            "$p's holy aura fades.", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_bless_weapon(
            "bless weapon", Magic2::spell_bless_weapon,
            TAR_OBJ_INV, POS_STANDING, SLOT(637), 100, 24, "", "!Bless Weapon!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ENCHANTMENT
    ),

    gsn_blindness(
            "blindness", Magic::spell_blindness,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(4), 5, 12,
            "", "You can see again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_bluefire(
            "bluefire", Magic::spell_bluefire,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(660), 20, 12,
            "torments", "!Bluefire!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ATTACK
    ),

    gsn_burning_hands(
            "burning hands", Magic::spell_burning_hands,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(5), 15, 12,
            "burning hands", "!Burning Hands!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_call_lightning(
            "call lightning", Magic::spell_call_lightning,
            TAR_IGNORE, POS_FIGHTING, SLOT(6), 15, 12, "lightning bolt", "!Call Lightning!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEATHER
    ),

    gsn_calm(
            "calm", Magic::spell_calm,
            TAR_IGNORE, POS_FIGHTING,
            SLOT(509), 30, 12,
            "", "You have lost your peace of mind.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_cancellation(
            "cancellation", Magic::spell_cancellation,
            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(507), 20, 12, "", "!cancellation!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_cause_critical(
            "cause critical", Magic::spell_cause_critical,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(63), 20, 12, "spell", "!Cause Critical!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HARMFUL
    ),

    gsn_cause_light(
            "cause light", Magic::spell_cause_light,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(62), 15, 12, "spell", "!Cause Light!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HARMFUL
    ),

    gsn_cause_serious(
            "cause serious", Magic::spell_cause_serious,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(64), 17, 12, "spell", "!Cause Serious!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HARMFUL
    ),

    gsn_caustic_font(
            "caustic font", Magic::spell_caustic_font,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(653), 20, 12, "caustic font", "!caustic font!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_chain_lightning(
            "chain lightning", Magic::spell_chain_lightning,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(500), 25, 12, "lightning", "!Chain Lightning!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_charm_person(
            "charm person", Magic::spell_charm_person,
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(7), 5, 12,
            "", "You feel more self-confident.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_BEGUILING
    ),

    gsn_chromatic_orb(
            "chromatic orb", Magic2::spell_chromatic_orb,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(714), 50, 12, "chromatic orb", "!Chromatic Orb!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_control_undead(
            "control undead", Magic2::spell_control_undead,
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(669), 20, 12, "", "You feel more self confident.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_chill_touch(
            "chill touch", Magic::spell_chill_touch,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(8), 15, 12,
            "chilling touch", "You feel less cold.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_colour_spray(
            "colour spray", Magic::spell_colour_spray,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(10), 15, 12, "colour spray", "!Colour Spray!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_continual_light(
            "continual light", Magic::spell_continual_light,
            TAR_IGNORE, POS_STANDING, SLOT(57), 7, 12, "", "!Continual Light!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_CREATION
    ),

    gsn_control_weather(
            "control weather", Magic::spell_control_weather,
            TAR_IGNORE, POS_STANDING, SLOT(11), 25, 12, "", "!Control Weather!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEATHER
    ),

    gsn_corruption(
            "corruption", Magic::spell_corruption,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(671), 20, 12, "corruption", "You feel yourself healthy again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_create_food(
            "create food", Magic::spell_create_food,
            TAR_IGNORE, POS_STANDING, SLOT(12), 5, 12, "", "!Create Food!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_CREATION
    ),

    gsn_create_rose(
            "create rose", Magic::spell_create_rose,
            TAR_IGNORE, POS_STANDING, SLOT(511), 30, 12, "", "!Create Rose!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_CREATION
    ),

    gsn_create_spring(
            "create spring", Magic::spell_create_spring,
            TAR_IGNORE, POS_STANDING, SLOT(80), 20, 12, "", "!Create Spring!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_CREATION
    ),

    gsn_create_water(
            "create water", Magic::spell_create_water,
            TAR_OBJ_INV, POS_STANDING, SLOT(13), 5, 12, "", "!Create Water!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_CREATION
    ),

    gsn_cure_blindness(
            "cure blindness", Magic::spell_cure_blindness,
            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(14), 5, 12, "", "!Cure Blindness!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_CURATIVE
    ),

    gsn_cure_critical(
            "cure critical", Magic::spell_cure_critical,
            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(15), 20, 12,
            "", "!Cure Critical!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HEALING
    ),

    gsn_cure_disease(
            "cure disease", Magic::spell_cure_disease,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(501), 20, 12, "", "!Cure Disease!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_CURATIVE
    ),

    gsn_cure_light(
            "cure light", Magic::spell_cure_light,
            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(16), 10, 12,
            "", "!Cure Light!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HEALING
    ),

    gsn_cure_poison(
            "cure poison", Magic::spell_cure_poison,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(43), 5, 12,
            "", "!Cure Poison!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_CURATIVE
    ),

    gsn_cure_serious(
            "cure serious", Magic::spell_cure_serious,
            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(61), 15, 12,
            "", "!Cure Serious!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HEALING
    ),

    gsn_curse(
            "curse", Magic::spell_curse,
            TAR_OBJ_CHAR_OFF, POS_FIGHTING, SLOT(17), 20, 12,
            "curse", "The curse wears off.",
            "$p is no longer impure.", CABAL_NONE, null,
            ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_cursed_lands(
            "cursed lands", Magic2::spell_cursed_lands,
            TAR_IGNORE, POS_STANDING, SLOT(675), 200, 24,
            "", "!cursed lands!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_deadly_venom(
            "deadly venom", Magic2::spell_deadly_venom,
            TAR_IGNORE, POS_STANDING, SLOT(674), 200, 24,
            "", "!deadly venom!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_deafen(
            "deafen", Magic2::spell_deafen,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(570), 40, 12,
            "deafen", "The ringing in your ears finally stops.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_demonfire(
            "demonfire", Magic::spell_demonfire,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(505), 20, 12,
            "torments", "!Demonfire!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ATTACK
    ),

    gsn_desert_fist(
            "desert fist", Magic2::spell_desert_fist,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(681), 50, 12, "desert fist", "!desert fist!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_detect_evil(
            "detect evil", Magic::spell_detect_evil,
            TAR_CHAR_SELF, POS_STANDING, SLOT(18), 5, 12, "", "The red in your vision disappears.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_detect_good(
            "detect good", Magic::spell_detect_good,
            TAR_CHAR_SELF, POS_STANDING, SLOT(513), 5, 12, "", "The gold in your vision disappears.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_detect_hide_spell(
            "detect hide", Magic::spell_detect_hidden,
            TAR_CHAR_SELF, POS_STANDING, SLOT(0), 5, 12,
            "", "You feel less aware of your surroundings.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_detect_invis(
            "detect invis", Magic::spell_detect_invis,
            TAR_CHAR_SELF, POS_STANDING, SLOT(19), 5, 12, "", "You no longer see invisible objects.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_detect_magic(
            "detect magic", Magic::spell_detect_magic,
            TAR_CHAR_SELF, POS_STANDING, SLOT(20), 5, 12, "", "The detect magic wears off.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_detect_poison(
            "detect poison", Magic::spell_detect_poison,
            TAR_OBJ_INV, POS_STANDING, SLOT(21), 5, 12, "", "!Detect Poison!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_detect_undead(
            "detect undead", Magic::spell_detect_undead,
            TAR_CHAR_SELF, POS_STANDING, SLOT(594), 5, 12, "", "You can't detect undeads anymore.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_disenchant_armor(
            "disenchant armor", Magic::spell_disenchant_armor,
            TAR_OBJ_INV, POS_STANDING, SLOT(705), 50, 24, "", "!disenchant armor!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_disenchant_weapon(
            "disenchant weapon", Magic::spell_disenchant_weapon,
            TAR_OBJ_INV, POS_STANDING, SLOT(706), 50, 24, "", "!disenchant weapon!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_disintegrate(
            "disintegrate", Magic2::spell_disintegrate,
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(574), 100, 18, "thin light ray", "!disintegrate!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    /*TODO: gsn_dismantle(
            "dismantle", Magic2::spell_dismantle,
            TAR_CHAR_SELF, POS_STANDING, SLOT(621), 200, 24, "", "!621!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),*/

    gsn_dispel_evil(
            "dispel evil", Magic::spell_dispel_evil,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(22), 15, 12, "dispel evil", "!Dispel Evil!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ATTACK
    ),

    gsn_dispel_good(
            "dispel good", Magic::spell_dispel_good,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(512), 15, 12, "dispel good", "!Dispel Good!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ATTACK
    ),

    gsn_dispel_magic(
            "dispel magic", Magic::spell_dispel_magic,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(59), 15, 12, "", "!Dispel Magic!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_disruption(
            "disruption", Magic::spell_disruption,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(648), 20, 12, "disruption", "!disruption!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_dragon_breath(
            "dragon breath", Magic2::spell_dragon_breath,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(563), 75, 12,
            "blast of fire", "!dragon breath!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_dragon_skin(
            "dragon skin", Magic2::spell_dragon_skin,
            TAR_CHAR_SELF, POS_STANDING, SLOT(612), 50, 24, "", "Your skin becomes softer.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_dragon_strength(
            "dragon strength", Magic2::spell_dragon_strength,
            TAR_CHAR_SELF, POS_FIGHTING, SLOT(562), 75, 12,
            "", "You feel the strength of the dragon leave you.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_dragons_breath(
            "dragons breath", Magic2::spell_dragons_breath,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(576), 200, 24, "dragon breath", "Your get healtier again.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_DRACONIAN
    ),

    gsn_drain(
            "drain", Magic::spell_drain,
            TAR_OBJ_INV, POS_STANDING, SLOT(704), 5, 12, "", "!drain!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_earthfade(
            "earthfade", Magic::spell_earthfade,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(702), 100, 12,
            "", "You slowly fade to your neutral form.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_earthmaw(
            "earthmaw", Magic::spell_earthmaw,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(703), 30, 12, "earthmaw", "!earthmaw!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_earthquake(
            "earthquake", Magic::spell_earthquake,
            TAR_IGNORE, POS_FIGHTING, SLOT(23), 15, 12, "earthquake", "!Earthquake!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ATTACK
    ),

    gsn_elemental_sphere(
            "elemental sphere", Magic2::spell_elemental_sphere,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(719), 75, 12, "", "The protecting elemental sphere around you fades.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_enchant_armor(
            "enchant armor", Magic::spell_enchant_armor,
            TAR_OBJ_INV, POS_STANDING, SLOT(510), 100, 24, "", "!Enchant Armor!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ENCHANTMENT
    ),

    gsn_enchant_weapon(
            "enchant weapon", Magic::spell_enchant_weapon,
            TAR_OBJ_INV, POS_STANDING, SLOT(24), 100, 24, "", "!Enchant Weapon!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ENCHANTMENT
    ),

    gsn_energy_drain(
            "energy drain", Magic::spell_energy_drain,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(25), 35, 12, "energy drain", "!Energy Drain!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_enhanced_armor(
            "enhanced armor", Magic2::spell_enhanced_armor,
            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(583), 20, 12,
            "", "You feel yourself unprotected.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_enlarge(
            "enlarge", Magic2::spell_enlarge,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(711), 50, 12, "", "You return to your orginal size.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_etheral_fist(
            "etheral fist", Magic::spell_etheral_fist,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(645), 20, 12, "etheral fist", "!Etheral Fist!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_faerie_fire(
            "faerie fire", Magic::spell_faerie_fire,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(72), 5, 12,
            "faerie fire", "The pink aura around you fades away.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_WEATHER
    ),

    gsn_faerie_fog(
            "faerie fog", Magic::spell_faerie_fog,
            TAR_IGNORE, POS_STANDING, SLOT(73), 12, 12, "faerie fog", "!Faerie Fog!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEATHER
    ),

    gsn_farsight(
            "farsight", Magic2::spell_farsight,
            TAR_IGNORE, POS_STANDING, SLOT(521), 20, 12, "farsight", "!Farsight!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_fear(
            "fear", Magic2::spell_fear,
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(598), 50, 12,
            "", "You feel more brave.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ILLUSION
    ),

    gsn_fire_and_ice(
            "fire and ice", Magic::spell_fire_and_ice,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(699), 40, 12, "fire and ice", "!fire and ice!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_fireball(
            "fireball", Magic::spell_fireball,
            TAR_IGNORE, POS_FIGHTING, SLOT(26), 25, 12, "fireball", "!Fireball!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_fireproof(
            "fireproof", Magic::spell_fireproof,
            TAR_OBJ_INV, POS_STANDING, SLOT(523), 10, 12, "", "", "$p's protective aura fades.", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ENCHANTMENT
    ),

    gsn_firestream(
            "firestream", Magic::spell_firestream,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(692), 20, 12, "", "", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_fire_shield(
            "fire shield", Magic2::spell_fire_shield,
            TAR_IGNORE, POS_STANDING, SLOT(601), 200, 24,
            "", "!fire shield!",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_flamestrike(
            "flamestrike", Magic::spell_flamestrike,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(65), 20, 12, "flamestrike", "!Flamestrike!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ATTACK
    ),

    gsn_fly(
            "fly", Magic::spell_fly,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(56), 10, 18,
            "", "You slowly float to the ground.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_floating_disc(
            "floating disc", Magic::spell_floating_disc,
            TAR_IGNORE, POS_STANDING, SLOT(522), 40, 24, "", "!Floating disc!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_forcecage(
            "forcecage", Magic2::spell_forcecage,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(717), 75, 12, "", "The forcecage around you fades.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_frenzy(
            "frenzy", Magic::spell_frenzy,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(504), 30, 24, "", "Your rage ebbs.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_frostbolt(
            "frostbolt", Magic::spell_frostbolt,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(695), 20, 12, "frostbolt", "!frostbolt!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_fumble(
            "fumble", Magic::spell_fumble,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(712), 25, 18, "", "You speed up and regain your strength!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_galvanic_whip(
            "galvanic whip", Magic::spell_galvanic_whip,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(655), 20, 12, "galvanic whip", "!galvanic whip!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_gate(
            "gate", Magic::spell_gate,
            TAR_IGNORE, POS_FIGHTING, SLOT(83), 80, 12, "", "!Gate!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_giant_strength(
            "giant strength", Magic::spell_giant_strength,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(39), 20, 12,
            "", "You feel weaker.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ENHANCEMENT
    ),

    gsn_grounding(
            "grounding", Magic::spell_grounding,
            TAR_CHAR_SELF, POS_FIGHTING, SLOT(700), 50, 12, "", "You lost your grounding against electricity", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_group_defense(
            "group defense", Magic2::spell_group_defense,
            TAR_IGNORE, POS_STANDING, SLOT(586), 100, 36, "", "You feel less protected.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_group_heal(
            "group heal", Magic2::spell_group_heal,
            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(642), 500, 24, "", "!Group Heal!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HEALING
    ),

    gsn_hallucination(
            "hallucination", Magic2::spell_hallucination,
            TAR_CHAR_SELF, POS_STANDING, SLOT(606), 200, 12, "", "You are again defenseless to magic.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_ILLUSION
    ),

    gsn_hand_of_undead(
            "hand of undead", Magic::spell_hand_of_undead,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(647), 20, 24, "hand of undead", "!hand of undead!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_harm(
            "harm", Magic::spell_harm,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(27), 35, 12, "harm spell", "!Harm!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HARMFUL
    ),

    gsn_haste(
            "haste", Magic::spell_haste,
            TAR_CHAR_DEFENSIVE, POS_FIGHTING,
            SLOT(502), 30, 12,
            "", "You feel yourself slow down.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ENHANCEMENT
    ),

    gsn_heal(
            "heal", Magic::spell_heal,
            TAR_CHAR_DEFENSIVE, POS_FIGHTING,
            SLOT(28), 50, 12,
            "", "!Heal!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HEALING
    ),

    gsn_healing_light(
            "healing light", Magic::spell_healing_light,
            TAR_IGNORE, POS_STANDING, SLOT(613), 200, 24, "", "You can light more rooms now.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_heat_metal(
            "heat metal", Magic::spell_heat_metal,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(516), 25, 18, "spell", "!Heat Metal!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_helical_flow(
            "helical flow", Magic::spell_helical_flow,
            TAR_IGNORE, POS_FIGHTING, SLOT(661), 80, 12, "", "!Helical Flow!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_hellfire(
            "hellfire", Magic::spell_hellfire,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(520), 20, 12, "hellfire", "!hellfire!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ATTACK
    ),

    gsn_holy_aura(
            "holy aura", Magic2::spell_holy_aura,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(617), 75, 12, "", "Your holy aura vanishes.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_holy_fury(
            "holy fury", Magic2::spell_holy_fury,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(682), 50, 24, "", "You become more tolerable.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_holy_word(
            "holy word", Magic::spell_holy_word,
            TAR_IGNORE, POS_FIGHTING, SLOT(506), 200, 24, "divine wrath", "!Holy Word!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_hurricane(
            "hurricane", Magic::spell_hurricane,
            TAR_IGNORE, POS_FIGHTING, SLOT(672), 200, 24, "helical flow", "!Hurricane!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_hydroblast(
            "hydroblast", Magic2::spell_hydroblast,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(684), 50, 12, "water fist", "!Hydroblast!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_iceball(
            "iceball", Magic::spell_iceball,
            TAR_IGNORE, POS_FIGHTING, SLOT(513), 25, 12, "iceball", "!Iceball!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_identify(
            "identify", Magic::spell_identify,
            TAR_OBJ_INV, POS_STANDING, SLOT(53), 12, 24, "", "!Identify!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_improved_detect(
            "improved detect", Magic2::spell_improved_detection,
            TAR_CHAR_SELF, POS_STANDING, SLOT(626), 20, 12, "", "You feel less aware of your surroundings.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_imp_invis(
            "improved invis", Magic2::spell_improved_invis,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(627), 20, 12,
            "", "You are no longer invisible.",
            "$p fades into view.", CABAL_NONE, null, ALIGN_NONE, GROUP_ILLUSION
    ),

    gsn_infravision(
            "infravision", Magic::spell_infravision,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(77), 5, 18, "", "You no longer see in the dark.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ENHANCEMENT
    ),

    gsn_insanity(
            "insanity", Magic2::spell_insanity,
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(616), 100, 24, "", "Now you feel yourself calm down.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_inspire(
            "inspire", Magic2::spell_inspire,
            TAR_IGNORE, POS_STANDING, SLOT(587), 75, 24, "", "You feel less inspired", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_invis(
            "invisibility", Magic::spell_invis,
            TAR_OBJ_CHAR_DEF, POS_STANDING, SLOT(29), 5, 12,
            "", "You are no longer invisible.",
            "$p fades into view.", CABAL_NONE, null, ALIGN_NONE, GROUP_ILLUSION
    ),

    gsn_iron_body(
            "iron body", Magic2::spell_iron_body,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(718), 75, 12, "", "The skin regains its softness.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_iron_golem(
            "iron golem", Magic2::spell_iron_golem,
            TAR_IGNORE, POS_STANDING, SLOT(664), 400, 24, "", "You gained enough mana to make more golems now.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_knock(
            "knock", Magic2::spell_knock,
            TAR_IGNORE, POS_STANDING, SLOT(603), 20, 24, "", "!knock!",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_know_alignment(
            "know alignment", Magic::spell_know_alignment,
            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(58), 9, 12, "", "!Know Alignment!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_lesser_golem(
            "lesser golem", Magic2::spell_lesser_golem,
            TAR_IGNORE, POS_STANDING, SLOT(662), 200, 12, "", "You gained enough mana to make more golems now.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_lethargic_mist(
            "lethargic mist", Magic2::spell_lethargic_mist,
            TAR_IGNORE, POS_STANDING, SLOT(676), 200, 24,
            "", "!lethargic mist!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_light_arrow(
            "light arrow", Magic2::spell_light_arrow,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(683), 40, 12, "light arrow", "!light arrow!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_lightning_bolt(
            "lightning bolt", Magic::spell_lightning_bolt,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(30), 15, 12,
            "lightning bolt", "!Lightning Bolt!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_lightning_shield(
            "lightning shield", Magic::spell_lightning_shield,
            TAR_IGNORE, POS_STANDING, SLOT(614), 150, 24, "lightning shield", "Now you can shield your room again.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_link(
            "link", Magic2::spell_link,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(588), 125, 18, "", "!link!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_MEDITATION
    ),

    gsn_lion_help(
            "lion help", Magic2::spell_lion_help,
            TAR_IGNORE, POS_STANDING, SLOT(595), 100, 12, "", "Once again, you may send a slayer lion.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_locate_object(
            "locate object", Magic::spell_locate_object,
            TAR_IGNORE, POS_STANDING, SLOT(31), 20, 18, "", "!Locate Object!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_love_potion(
            "love potion", Magic2::spell_love_potion,
            TAR_CHAR_SELF, POS_STANDING, SLOT(666), 10, 0,
            "", "You feel less dreamy-eyed.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_magic_jar(
            "magic jar", Magic2::spell_magic_jar,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(596), 20, 12, "", "!magic jar!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BEGUILING
    ),

    gsn_magic_missile(
            "magic missile", Magic::spell_magic_missile,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(32), 15, 12,
            "magic missile", "!Magic Missile!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_magic_resistance(
            "magic resistance", Magic2::spell_magic_resistance,
            TAR_CHAR_SELF, POS_STANDING, SLOT(605), 200, 24, "", "You are again defenseless to magic.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_magnetic_trust(
            "magnetic trust", Magic::spell_magnetic_trust,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(656), 20, 12, "magnetic trust", "!magnetic trust!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_mass_healing(
            "mass healing", Magic::spell_mass_healing,
            TAR_IGNORE, POS_STANDING, SLOT(508), 100, 36, "", "!Mass Healing!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HEALING
    ),

    gsn_mass_invis(
            "mass invis", Magic::spell_mass_invis,
            TAR_IGNORE, POS_STANDING, SLOT(69), 20, 24,
            "", "You are no longer invisible.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ILLUSION
    ),

    gsn_mass_sanctuary(
            "mass sanctuary", Magic2::spell_mass_sanctuary,
            TAR_IGNORE, POS_STANDING, SLOT(589), 200, 24, "", "The white aura around your body fades.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_master_healing(
            "master healing", Magic2::spell_master_heal,
            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(641), 300, 12, "", "!Master Heal!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HEALING
    ),

    gsn_meld_into_stone(
            "meld into stone", Magic2::spell_meld_into_stone,
            TAR_CHAR_SELF, POS_STANDING, SLOT(584), 12, 18, "", "The stones on your skin crumble into dust.", "",
            CABAL_NONE, Race.ROCKSEER, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_mend(
            "mend", Magic2::spell_mend,
            TAR_OBJ_INV, POS_STANDING, SLOT(590), 150, 24,
            "", "!mend!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ENCHANTMENT
    ),

    gsn_mind_light(
            "mind light", Magic2::spell_mind_light,
            TAR_IGNORE, POS_STANDING, SLOT(82), 200, 24, "", "You can booster more rooms now.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_mind_wrack(
            "mind wrack", Magic::spell_mind_wrack,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(650), 20, 12, "mind wrack", "!mind wrack!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_mind_wrench(
            "mind wrench", Magic::spell_mind_wrench,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(651), 20, 12, "mind wrench", "!mind wrench!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_mist_walk(
            "mist walk", Magic::spell_mist_walk,
            TAR_IGNORE, POS_FIGHTING, SLOT(658), 80, 12, "", "!Mist Walk!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_mummify(
            "mummify", Magic2::spell_mummify,
            TAR_OBJ_CHAR_OFF, POS_STANDING, SLOT(715), 50, 12, "", "You gain energy to give live to new corpses.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_mysterious_dream(
            "mysterious dream", Magic2::spell_mysterious_dream,
            TAR_IGNORE, POS_STANDING, SLOT(678), 200, 24,
            "", "!mysterous dream!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_BEGUILING
    ),

    gsn_nexus(
            "nexus", Magic2::spell_nexus,
            TAR_IGNORE, POS_STANDING, SLOT(520), 150, 36, "", "!Nexus!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_pass_door(
            "pass door", Magic::spell_pass_door,
            TAR_CHAR_SELF, POS_STANDING, SLOT(74), 20, 12, "", "You feel solid again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_plague(
            "plague", Magic::spell_plague,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(503), 20, 12,
            "sickness", "Your sores vanish.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_poison(
            "poison", Magic::spell_poison,
            TAR_OBJ_CHAR_OFF, POS_FIGHTING, SLOT(33), 10, 12,
            "poison", "You feel less sick.",
            "The poison on $p dries up.", CABAL_NONE,
            null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_polymorph(
            "polymorph", Magic2::spell_polymorph,
            TAR_IGNORE, POS_STANDING, SLOT(639), 250, 24, "", "You return to your own race.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_portal(
            "portal", Magic2::spell_portal,
            TAR_IGNORE, POS_STANDING, SLOT(519), 100, 24, "", "!Portal!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_protection_cold(
            "protection cold", Magic2::spell_protection_cold,
            TAR_CHAR_SELF, POS_STANDING, SLOT(600), 5, 12,
            "", "You feel less protected",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_protection_evil(
            "protection evil", Magic::spell_protection_evil,
            TAR_CHAR_SELF, POS_STANDING, SLOT(34), 5, 12, "", "You feel less protected.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_protection_good(
            "protection good", Magic::spell_protection_good,
            TAR_CHAR_SELF, POS_STANDING, SLOT(666), 5, 12, "", "You feel less protected.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_protection_heat(
            "protection heat", Magic2::spell_protection_heat,
            TAR_CHAR_SELF, POS_STANDING, SLOT(599), 5, 12,
            "", "You feel less protected",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_protection_negative(
            "protection negative", Magic2::spell_protection_negative,
            TAR_CHAR_SELF, POS_STANDING, SLOT(636), 20, 12, "", "You feel less protected from your own attacks.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_protective_shield(
            "protective shield", Magic2::spell_protective_shield,
            TAR_CHAR_SELF, POS_STANDING, SLOT(572), 70, 12,
            "", "Your shield fades away.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_power_word_kill(
            "power word kill", Magic2::spell_power_kill,
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(604), 200, 18, "powerful word", "You gain back your durability.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_power_stun(
            "power word stun", Magic2::spell_power_stun,
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(625), 200, 24,
            "", "You can move now.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_quantum_spike(
            "quantum spike", Magic::spell_quantum_spike,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(657), 20, 12, "quantum spike", "!quantum spike!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_ranger_staff(
            "ranger staff", Magic2::spell_ranger_staff,
            TAR_IGNORE, POS_FIGHTING, SLOT(519), 75, 0, "", "!ranger staff!", "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_ray_of_truth(
            "ray of truth", Magic::spell_ray_of_truth,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(518), 20, 12,
            "ray of truth", "!Ray of Truth!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_recharge(
            "recharge", Magic::spell_recharge,
            TAR_OBJ_INV, POS_STANDING, SLOT(517), 60, 24, "", "!Recharge!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ENCHANTMENT
    ),

    gsn_refresh(
            "refresh", Magic::spell_refresh,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(81), 12, 18, "refresh", "!Refresh!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HEALING
    ),

    /* TODO: gsn_reincarnation(
            "reincarnation", Magic2::spell_reincarnation,
            TAR_IGNORE, POS_STANDING, SLOT(668), 0, 0, "", "!!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),*/

    gsn_remove_curse(
            "remove curse", Magic::spell_remove_curse,
            TAR_OBJ_CHAR_DEF, POS_STANDING, SLOT(35), 5, 12,
            "", "!Remove Curse!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_CURATIVE
    ),

    gsn_remove_fear(
            "remove fear", Magic2::spell_remove_fear,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(582), 5, 12, "", "!Remove Fear!", "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_remove_tattoo(
            "remove tattoo", Magic2::spell_remove_tattoo,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(552), 10, 0, "", "!remove tattoo!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_resilience(
            "resilience", Magic2::spell_resilience,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(638), 50, 12, "", "You feel less armored to draining attacks.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_restoring_light(
            "restoring light", Magic2::spell_restoring_light,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(643), 50, 24, "", "!restoring light!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_sanctify_lands(
            "sanctify lands", Magic2::spell_sanctify_lands,
            TAR_IGNORE, POS_STANDING, SLOT(673), 200, 24, "", "!sanctify lands!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_sanctuary(
            "sanctuary", Magic::spell_sanctuary,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(36), 75, 12,
            "", "The white aura around your body fades.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_sand_storm(
            "sand storm", Magic2::spell_sand_storm,
            TAR_IGNORE, POS_FIGHTING, SLOT(577), 200, 24,
            "storm of sand", "The sands melts in your eyes.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_scream(
            "scream", Magic2::spell_scream,
            TAR_IGNORE, POS_FIGHTING, SLOT(578), 200, 24,
            "scream", "You can hear again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_severity_force(
            "severity force", Magic2::spell_severity_force,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(622), 20, 12, "severity force", "!severity force!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_shield(
            "shield", Magic::spell_shield,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(67), 12, 18,
            "", "Your force shield shimmers then fades away.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_shielding(
            "shielding", Magic2::spell_shielding,
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(591), 250, 12,
            "", "You feel the glow of the true source in the distance",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_shocking_grasp(
            "shocking grasp", Magic::spell_shocking_grasp,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(53), 15, 12, "shocking grasp", "!Shocking Grasp!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_shocking_trap(
            "shocking trap", Magic::spell_shocking_trap,
            TAR_IGNORE, POS_STANDING, SLOT(615), 150, 24, "shocking trap", "Now you can trap more rooms with shocks.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_sleep(
            "sleep", Magic::spell_sleep,
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(38), 15, 12,
            "", "You feel less tired.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BEGUILING
    ),

    gsn_slow(
            "slow", Magic::spell_slow,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(515), 30, 12,
            "", "You feel yourself speed up.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_soften(
            "soften", Magic::spell_soften,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(713), 75, 12, "soften", "Your skin regains its robustness.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_WEATHER
    ),

    gsn_solar_flight(
            "solar flight", Magic::spell_solar_flight,
            TAR_IGNORE, POS_FIGHTING, SLOT(659), 80, 12, "", "!Solar Flight!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_sonic_resonance(
            "sonic resonance", Magic::spell_sonic_resonance,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(649), 20, 12, "sonic resonance", "!sonic resonance!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_soul_bind(
            "soul bind", Magic2::spell_soul_bind,
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(716), 5, 12, "", "You feel more self-confident.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_BEGUILING
    ),

    gsn_spectral_furor(
            "spectral furor", Magic::spell_spectral_furor,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(646), 20, 12, "spectral furor", "!spectral furor!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_stone_golem(
            "stone golem", Magic2::spell_stone_golem,
            TAR_IGNORE, POS_STANDING, SLOT(663), 300, 18, "", "You gained enough mana to make more golems now.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_stone_skin(
            "stone skin", Magic::spell_stone_skin,
            TAR_CHAR_SELF, POS_STANDING, SLOT(66), 12, 18, "", "Your skin feels soft again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_PROTECTIVE
    ),

    gsn_suffocate(
            "suffocate", Magic2::spell_suffocate,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(714), 50, 12, "breathlessness", "You can breath again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_sulfurus_spray(
            "sulfurus spray", Magic::spell_sulfurus_spray,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(652), 20, 12, "sulfurus spray", "!sulfurus spray!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_summon(
            "summon", Magic::spell_summon,
            TAR_IGNORE, POS_STANDING, SLOT(40), 50, 12, "", "!Summon!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_summon_air_elemental(
            "summon air elemental", Magic::spell_summon_air_elm,
            TAR_IGNORE, POS_STANDING, SLOT(696), 50, 12, "", "You gain back the energy to summon another air elemental.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_summon_earth_elemental(
            "summon earth elemental", Magic::spell_summon_earth_elm,
            TAR_IGNORE, POS_STANDING, SLOT(693), 50, 12, "", "You gain back the energy to summon another earth elemental.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_summon_fire_elemental(
            "summon fire elemental", Magic::spell_summon_fire_elm,
            TAR_IGNORE, POS_STANDING, SLOT(697), 50, 12, "", "You gain back the energy to summon another fire elemental.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_summon_lightning_elemental(
            "summon lightning elemental", Magic::spell_summon_light_elm,
            TAR_IGNORE, POS_STANDING, SLOT(710), 50, 12, "", "You gain back the energy to summon another lightning elemental.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_summon_water_elemental(
            "summon water elemental", Magic::spell_summon_water_elm,
            TAR_IGNORE, POS_STANDING, SLOT(698), 50, 12, "", "You gain back the energy to summon another water elemental.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_summon_shadow(
            "summon shadow", Magic2::spell_summon_shadow,
            TAR_CHAR_SELF, POS_STANDING, SLOT(620), 200, 24, "", "You can summon more shadows, now.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_superior_heal(
            "superior heal", Magic2::spell_super_heal,
            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(640), 100, 12, "", "!Super Heal!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_HEALING
    ),

    gsn_tattoo(
            "tattoo", Magic2::spell_tattoo,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(551), 10, 0, "", "!tattoo!", "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_teleport(
            "teleport", Magic::spell_teleport,
            TAR_CHAR_SELF, POS_FIGHTING, SLOT(2), 35, 12,
            "", "!Teleport!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    gsn_transfer_object(
            "transfer object", Magic::spell_transfer_object,
            TAR_IGNORE, POS_STANDING, SLOT(708), 40, 12, "", "!transfer object!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_tsunami(
            "tsunami", Magic::spell_tsunami,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(701), 50, 12, "raging tidal wave", "!tsunami!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_turn(
            "turn", Magic2::spell_turn,
            TAR_IGNORE, POS_FIGHTING, SLOT(597), 50, 12, "", "You can handle turn spell again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_vampiric_blast(
            "vampiric blast", Magic2::spell_vam_blast,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(611), 20, 12, "vampiric blast", "!Vampiric Blast!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_ventriloquate(
            "ventriloquate", Magic::spell_ventriloquate,
            TAR_IGNORE, POS_STANDING, SLOT(41), 5, 12, "", "!Ventriloquate!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_ILLUSION
    ),

    gsn_web(
            "web", Magic2::spell_web,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(592), 50, 12,
            "", "The webs around you dissolve.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_ATTACK
    ),

    gsn_windwall(
            "windwall", Magic::spell_windwall,
            TAR_IGNORE, POS_FIGHTING, SLOT(694), 20, 12, "air blast", "Your eyes feel better.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_COMBAT
    ),

    gsn_witch_curse(
            "witch curse", Magic2::spell_witch_curse,
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(602), 150, 24,
            "", "You gain back your durability.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_wrath(
            "wrath", Magic2::spell_wrath,
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(553), 20, 12,
            "heavenly wrath", "The curse wears off.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BENEDICTIONS
    ),

    gsn_weaken(
            "weaken", Magic::spell_weaken,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(68), 20, 12,
            "spell", "You feel stronger.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_MALADICTIONS
    ),

    gsn_word_of_recall(
            "word of recall", Magic::spell_word_of_recall,
            TAR_CHAR_SELF, POS_RESTING, SLOT(42), 5, 12, "", "!Word of Recall!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_TRANSPORTATION
    ),

    /*
     * Dragon breath
     **/

    gsn_acid_breath(
            "acid breath", Magic::spell_acid_breath,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(200), 100, 24,
            "blast of acid", "!Acid Breath!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),

    /* TODO gsn_desert_heat(
            "desert heat", Magic2::spell_desert_heat,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(629), 200, 24, "cloud of blistering desert heat", "The smoke leaves your eyes.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),*/

    gsn_fire_breath(
            "fire breath", Magic::spell_fire_breath,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(201), 200, 24,
            "blast of flame", "The smoke leaves your eyes.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),

    gsn_frost_breath(
            "frost breath", Magic::spell_frost_breath,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(202), 125, 24,
            "blast of frost", "!Frost Breath!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),

    gsn_gas_breath(
            "gas breath", Magic::spell_gas_breath,
            TAR_IGNORE, POS_FIGHTING, SLOT(203), 175, 24,
            "blast of gas", "!Gas Breath!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),

    gsn_lightning_breath(
            "lightning breath", Magic::spell_lightning_breath,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(204), 150, 24,
            "blast of lightning", "!Lightning Breath!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),

    /* TODO gsn_lightning_stroke(
            "lightning stroke", Magic::spell_light_stroke,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(632), 200, 24, "stroke of lightning", "!lightning stroke!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),
     */

    /* TODO gsn_luck_bonus(
            "luck bonus", Magic2::spell_luck_bonus,
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(630), 20, 12, "", "You feel less armored against magic.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_PROTECTIVE
    ),*/

    /* TODO: gsn_paralyzation(
            "paralyzation", Magic2::spell_paralyzation,
            TAR_IGNORE, POS_FIGHTING, SLOT(631), 200, 24, "gas of paralyzation", "You feel you can move again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),*/

    /* TODO gsn_repulsion(
            "repulsion", Magic2::spell_repulsion,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(633), 200, 24, "repulsion", "!repulsion!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),*/

    /* TODO
    gsn_sleep_gas(
            "sleep gas", Magic2::spell_sleep_gas,

            TAR_IGNORE, POS_FIGHTING, SLOT(628), 200, 24, "sleep gas", "You feel drained.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),
    */

    /* TODO
    gsn_slow_gas(
            "slow gas", Magic2::spell_slow_gas,
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(634), 200, 24, "slow gas", "You can move faster now.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DRACONIAN
    ),
    */

    /*
     * ActSkill for mobiles. (general purpose and high explosive from
     * Glop/Erkenbrand
     **/
    gsn_crush(
            "crush",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 18,
            "crush", "!crush!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_general_purpose(
            "general purpose",
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(401), 0, 12, "general purpose ammo", "!General Purpose Ammo!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_high_explosive(
            "high explosive",
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(402), 0, 12, "high explosive ammo", "!High Explosive Ammo!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_tail(
            "tail",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 18,
            "tail", "!Tail!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    /* combat and weapons skills*/

    gsn_arrow(
            "arrow",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "arrow", "!arrow!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_axe(
            "axe",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Axe!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_bow(
            "bow",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "bow", "!bow!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_dagger(
            "dagger",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Dagger!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_flail(
            "flail",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Flail!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_lance(
            "lance",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "lance", "!lance!", "",
            CABAL_KNIGHT, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_mace(
            "mace",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Mace!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_polearm(
            "polearm",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Polearm!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_shield_block(
            "shield block",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Shield!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DEFENSIVE
    ),

    gsn_spear(
            "spear",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 12,
            "spear", "!Spear!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_sword(
            "sword",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!sword!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_whip(
            "whip",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Whip!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_second_weapon(
            "second weapon",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!second weapon!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_ambush(
            "ambush",
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(0), 0, 12,
            "surprise attack", "!Ambush!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_area_attack(
            "area attack",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Area Attack!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_assassinate(
            "assassinate",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 18,
            "assassination attempt", "!assassinate!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_backstab(
            "backstab",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "backstab", "!Backstab!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_bash(
            "bash",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 18,
            "bash", "!Bash!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_bash_door(
            "bash door",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 18,
            "bash", "!Bash Door!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_bear_call(
            "bear call",
            TAR_IGNORE, POS_FIGHTING, SLOT(518), 50, 0,
            "", "You feel you can handle more bears now.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_berserk(
            "berserk",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 24,
            "", "You feel your pulse slow down.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_blackguard(
            "blackguard",
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(0), 0, 12,
            "blackguard", "Your blackguard fades away.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_blackjack(
            "blackjack",
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(0), 0, 8,
            "blackjack", "Your head feels better.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_blind_fighting(
            "blind fighting",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "!blind fighting!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_blindness_dust(
            "blindness dust",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 20, 18,
            "", "!blindness dust!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_blink(
            "blink",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Blink!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_butcher(
            "butcher",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "!butcher!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_caltraps(
            "caltraps",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 18,
            "caltraps", "Your feet feel less sore.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_camouflage(
            "camouflage",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 24,
            "", "!Camouflage!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_move_camf(
            "camouflage move",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "", "!move camouflaged!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_camp(
            "camp",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 24,
            "camp", "You can handle more camps now.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_circle(
            "circle",
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(0), 0, 18,
            "circle stab", "!Circle!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_control_animal(
            "control animal",
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(0), 5, 12,
            "", "You feel more self-confident.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_BEGUILING
    ),

    gsn_cleave(
            "cleave",
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(0), 0, 24,
            "cleave", "!Cleave!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_concentrate(
            "concentrate",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 18,
            "", "You can concentrate on new fights.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_counter(
            "counter",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "!Counter!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_critical(
            "critical strike",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 18,
            "", "!critical strike!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_cross_block(
            "cross block",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!cross block!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DEFENSIVE
    ),

    gsn_detect_hidden(
            "detect hidden",
            TAR_CHAR_SELF, POS_STANDING, SLOT(44), 5, 12, "", "You feel less aware of your surroundings.",
            "", CABAL_NONE, null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_detect_sneak(
            "detect sneak",
            TAR_CHAR_SELF, POS_STANDING, SLOT(0), 20, 18,
            "", "!detect sneak!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DETECTION
    ),

    gsn_dirt(
            "dirt kicking",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 12,
            "kicked dirt", "You rub the dirt out of your eyes.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_disarm(
            "disarm",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 18,
            "", "!Disarm!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_dodge(
            "dodge",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Dodge!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DEFENSIVE
    ),

    gsn_dual_backstab(
            "dual backstab",
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(0), 0, 0,
            "second backstab", "!dual backstab!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_enchant_sword(
            "enchant sword",
            TAR_OBJ_INV, POS_STANDING, SLOT(0), 100, 24,
            "", "!Enchant sword!", "", CABAL_NONE, null,
            ALIGN_NONE, GROUP_NONE
    ),

    gsn_endure(
            "endure",
            TAR_CHAR_SELF, POS_FIGHTING, SLOT(0), 0, 24,
            "", "You feel susceptible to magic again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_enhanced_damage(
            "enhanced damage",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Enhanced Damage!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_entangle(
            "entangle",
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(568), 40, 12,
            "entanglement", "You feel less entangled.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_envenom(
            "envenom",
            TAR_IGNORE, POS_RESTING, SLOT(0), 0, 12,
            "", "!Envenom!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_escape(
            "escape",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 24,
            "", "!escape!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_explode(
            "explode",
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(0), 100, 24,
            "flame", "The smoke leaves your eyes.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_ground_strike(
            "ground strike",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 18,
            "", "!ground strike!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_hand_block(
            "hand block",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!hand block!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DEFENSIVE
    ),

    gsn_hand_to_hand(
            "hand to hand",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Hand to Hand!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_hara_kiri(
            "hara kiri",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 50, 12,
            "", "You feel you gain your life again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_headguard(
            "headguard",
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(0), 0, 12,
            "headguard", "Your headguard fades away.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_herbs(
            "herbs",
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(0), 0, 30,
            "", "The herbs look more plentiful here.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_kick(
            "kick",
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(0), 0, 12,
            "kick", "!Kick!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_lash(
            "lash",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 4,
            "lash", "!Lash!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_light_res(
            "light resistance",
            TAR_IGNORE, POS_SLEEPING, SLOT(0), 0, 0,
            "", "Light Resistance", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_lion_call(
            "lion call",
            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(0), 50, 12,
            "", "!lion call!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_make_arrow(
            "make arrow",
            TAR_IGNORE, POS_STANDING, SLOT(0), 50, 24,
            "", "!make arrow!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_make_bow(
            "make bow",
            TAR_IGNORE, POS_STANDING, SLOT(0), 200, 24,
            "", "!make bow!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_mental_attack(
            "mental attack",
            TAR_CHAR_SELF, POS_STANDING, SLOT(0), 200, 24,
            "", "!mental attack!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_neckguard(
            "neckguard",
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(0), 0, 12,
            "neckguard", "Your neckguard fades away.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_nerve(
            "nerve",
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(0), 0, 18,
            "", "Your nerves feel better.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_parry(
            "parry",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Parry!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DEFENSIVE
    ),

    gsn_perception(
            "perception",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "!perception!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_push(
            "push",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 18,
            "push", "!push!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_rescue(
            "rescue",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 12,
            "", "!Rescue!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_DEFENSIVE
    ),

    gsn_sense_life(
            "sense life",
            TAR_CHAR_SELF, POS_STANDING, SLOT(623), 20, 12,
            "", "You lost the power to sense life.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_settraps(
            "settraps",
            TAR_CHAR_SELF, POS_STANDING, SLOT(0), 200, 24,
            "trap", "You can set more traps now.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_shield_cleave(
            "shield cleave",
            TAR_CHAR_SELF, POS_STANDING, SLOT(0), 200, 24,
            "", "!shield cleave!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_smithing(
            "smithing",
            TAR_IGNORE, POS_STANDING, SLOT(0), 10, 18,
            "", "!smithing!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_spell_craft(
            "spell craft",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "spell craft", "!spell craft!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_strangle(
            "strangle",
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(0), 0, 8,
            "strangulation", "Your neck feels better.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_swimming(
            "swimming",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "!swimming!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_target(
            "target",
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(0), 0, 12,
            "target", "!Kick!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_throw(
            "throw",
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(0), 0, 18,
            "throw", "!throw!", "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_tiger_power(
            "tiger power",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 12,
            "", "You feel your tigers escaped.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_track(
            "track",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "", "!track!", "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_trip(
            "trip",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 12,
            "trip", "!Trip!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_vampire(
            "vampire",
            TAR_IGNORE, POS_STANDING, SLOT(0), 100, 12,
            "", "Now you are familer to other creatures.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_vampiric_bite(
            "vampiric bite",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "vampiric bite", "!vampiric bite!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_vampiric_touch(
            "vampiric touch",
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(0), 0, 12,
            "vampiric touch", "You wake up from nightmares.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_vanish(
            "vanish",
            TAR_CHAR_SELF, POS_FIGHTING, SLOT(521), 25, 18,
            "", "!vanish!", "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_warcry(
            "warcry",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 30, 12,
            "", "Your warcry has worn off.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_weapon_cleave(
            "weapon cleave",
            TAR_CHAR_SELF, POS_STANDING, SLOT(0), 200, 24,
            "", "!weapon cleave!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_second_attack(
            "second attack",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Second Attack!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_secondary_attack(
            "secondary attack",
            TAR_CHAR_SELF, POS_STANDING, SLOT(0), 200, 24,
            "", "!secondary attack!", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_third_attack(
            "third attack",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Third Attack!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_fourth_attack(
            "fourth attack",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Fourth Attack!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_fifth_attack(
            "fifth attack",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Fifth Attack!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    /* non-combat skills*/

    gsn_blue_arrow(
            "blue arrow",
            TAR_IGNORE, POS_STANDING, SLOT(0), 50, 12,
            "", "!blue arrow!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_fast_healing(
            "fast healing",
            TAR_IGNORE, POS_SLEEPING, SLOT(0), 0, 0,
            "", "!Fast Healing!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_MEDITATION
    ),

    gsn_green_arrow(
            "green arrow",
            TAR_IGNORE, POS_STANDING, SLOT(0), 50, 12,
            "", "!green arrow!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_grip(
            "grip",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 18,
            "", "!Grip!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_haggle(
            "haggle",
            TAR_IGNORE, POS_RESTING, SLOT(0), 0, 0,
            "", "!Haggle!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_hide(
            "hide",
            TAR_IGNORE, POS_RESTING, SLOT(0), 0, 18,
            "", "!Hide!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_katana(
            "katana",
            TAR_OBJ_INV, POS_STANDING, SLOT(0), 100, 24,
            "", "You can now make another katana.", "",
            CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_lay_hands(
            "lay hands",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 12,
            "", "You may heal more innocents now.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_lore(
            "lore",
            TAR_IGNORE, POS_RESTING, SLOT(0), 0, 36,
            "", "!Lore!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_master_hand(
            "mastering pound",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Master Hand!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_FIGHTMASTER
    ),

    gsn_master_sword(
            "mastering sword",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!master sword!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WEAPONSMASTER
    ),

    gsn_meditation(
            "meditation",
            TAR_IGNORE, POS_SLEEPING, SLOT(0), 0, 0,
            "", "Meditation", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_MEDITATION
    ),

    gsn_peek(
            "peek",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "!Peek!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_pick_lock(
            "pick lock",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "", "!Pick!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_poison_smoke(
            "poison smoke",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 20, 18,
            "", "!poison smoke!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_quiet_movement(
            "quiet movement",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "!quiet movement!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_recall(
            "recall",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 24,
            "", "!Recall!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_red_arrow(
            "red arrow",
            TAR_IGNORE, POS_STANDING, SLOT(0), 50, 12,
            "", "!red arrow!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_sneak(
            "sneak",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "", "You no longer feel stealthy.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_steal(
            "steal",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "", "!Steal!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_scrolls(
            "scrolls",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "", "!Scrolls!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WIZARD
    ),

    gsn_staves(
            "staves",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "", "!Staves!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WIZARD
    ),

    gsn_tame(
            "tame",
            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(0), 0, 24,
            "", "!tame!", "", CABAL_NONE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_trance(
            "trance",
            TAR_IGNORE, POS_SLEEPING, SLOT(0), 0, 0,
            "", "", "", CABAL_NONE, null, ALIGN_NONE, GROUP_MEDITATION
    ),

    gsn_white_arrow(
            "white arrow",
            TAR_IGNORE, POS_STANDING, SLOT(0), 50, 12,
            "", "!white arrow!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_wands(
            "wands",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "", "!Wands!", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_WIZARD
    ),

    /* cabals*/

    gsn_mortal_strike(
            "mortal strike",
            TAR_CHAR_SELF, POS_STANDING, SLOT(0), 200, 24,
            "mortal strike", "!mortal strike!", "",
            CABAL_BATTLE, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_disgrace(
            "disgrace",
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(619), 200, 12, "", "You feel yourself getting prouder.", "",
            CABAL_CHAOS, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_evil_spirit(
            "evil spirit",
            TAR_IGNORE, POS_STANDING, SLOT(618), 800, 36,
            "evil spirit", "Your body regains its full spirit.", "",
            CABAL_INVADER, null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_ruler_aura(
            "ruler aura",
            TAR_CHAR_SELF, POS_STANDING, SLOT(667), 20, 12, "", "Your ruler aura fades.", "", CABAL_RULER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_sword_of_justice(
            "sword of justice",
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(686), 50, 12, "sword of justice", "!sword of justice!", "", CABAL_RULER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_bandage(
            "bandage",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "You feel less healthy.", "", CABAL_BATTLE,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_cabal_recall(
            "cabal recall",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 24,
            "", "You may pray for transportation again.", "", CABAL_BATTLE,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_wanted(
            "wanted",
            TAR_IGNORE, POS_DEAD, SLOT(0), 0, 0,
            "", "!Wanted!", "", CABAL_RULER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_judge(
            "judge",
            TAR_IGNORE, POS_DEAD, SLOT(0), 0, 0,
            "", "!Judge!", "", CABAL_RULER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_bloodthirst(
            "bloodthirst",
            TAR_CHAR_SELF, POS_FIGHTING, SLOT(0), 0, 12,
            "", "Your bloody rage fades away.", "", CABAL_BATTLE,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_spellbane(
            "spellbane",
            TAR_CHAR_SELF, POS_STANDING, SLOT(0), 0, 12,
            "spellbane", "You feel less resistant to magic.", "", CABAL_BATTLE,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_resistance(
            "resistance",
            TAR_CHAR_SELF, POS_FIGHTING, SLOT(0), 0, 24,
            "", "You feel less tough.", "", CABAL_BATTLE,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_deathblow(
            "deathblow",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "!deathblow!", "", CABAL_BATTLE,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_transform(
            "transform",
            TAR_CHAR_SELF, POS_STANDING, SLOT(522), 100, 24, "", "You feel less healthy.", "", CABAL_SHALAFI,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_mental_knife(
            "mental knife",
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(524), 35, 12,
            "mental knife", "Your mental pain dissipates.", "", CABAL_SHALAFI,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_demon_summon(
            "demon summon",
            TAR_CHAR_SELF, POS_FIGHTING, SLOT(525), 100, 12,
            "", "You feel your summoning power return.", "", CABAL_SHALAFI,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_scourge(
            "scourge",
            TAR_IGNORE, POS_FIGHTING, SLOT(526), 50, 18, "Scourge of the Violet Spider", "!scourge!", "", CABAL_SHALAFI,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_manacles(
            "manacles",
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(528), 75, 12, "", "Your shackles dissolve.", "", CABAL_RULER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_shield_of_ruler(
            "shield of ruler",
            TAR_IGNORE, POS_FIGHTING, SLOT(529), 100, 12, "", "!shield!", "", CABAL_RULER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_guard(
            "guard",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 12,
            "", "", "", CABAL_KNIGHT,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_guard_call(
            "guard call",
            TAR_IGNORE, POS_FIGHTING, SLOT(530), 75, 12,
            "", "You may call more guards now.", "", CABAL_RULER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_nightwalker(
            "nightwalker",
            TAR_IGNORE, POS_FIGHTING, SLOT(531), 75, 12,
            "", "You feel your summoning power return.", "", CABAL_INVADER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_eyes_of_intrigue(
            "eyes of intrigue",
            TAR_IGNORE, POS_FIGHTING, SLOT(532), 75, 12, "", "!eyes of intrigue!", "", CABAL_INVADER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_fade(
            "fade",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 24,
            "", "!fade!", "", CABAL_INVADER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_shadow_cloak(
            "shadow cloak",
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(533), 10, 12, "", "The shadows no longer protect you.", "", CABAL_INVADER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_nightfall(
            "nightfall",
            TAR_IGNORE, POS_STANDING, SLOT(534), 50, 12, "", "You are now able to control lights.", "", CABAL_INVADER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_aura_of_chaos(
            "aura of chaos",
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(720), 20, 12, "", "The gods of chaos no longer protect you.", "",
            CABAL_CHAOS, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_garble(
            "garble",
            TAR_CHAR_DEFENSIVE, POS_FIGHTING, SLOT(535), 30, 12,
            "", "Your tongue untwists.", "", CABAL_CHAOS,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_mirror(
            "mirror",
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(536), 40, 12,
            "", "You fade away.", "", CABAL_CHAOS,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_confuse(
            "confuse",
            TAR_CHAR_OFFENSIVE, POS_FIGHTING, SLOT(537), 20, 12,
            "", "You feel less confused.", "", CABAL_CHAOS,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_doppelganger(
            "doppelganger",
            TAR_CHAR_DEFENSIVE, POS_STANDING, SLOT(527), 75, 12,
            "", "You return to your native form.", "", CABAL_CHAOS,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_chaos_blade(
            "chaos blade",
            TAR_IGNORE, POS_STANDING, SLOT(550), 60, 12,
            "", "!chaos blade!", "", CABAL_CHAOS,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_stalker(
            "stalker",
            TAR_IGNORE, POS_STANDING, SLOT(554), 100, 12,
            "", "You feel up to summoning another stalker.", "",
            CABAL_RULER, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_randomizer(
            "randomizer",
            TAR_IGNORE, POS_STANDING, SLOT(555), 200, 24,
            "", "You feel your randomness regenerating.", "", CABAL_CHAOS,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_tesseract(
            "tesseract",
            TAR_IGNORE, POS_STANDING, SLOT(556), 150, 12,
            "", "!tesseract!", "", CABAL_SHALAFI,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_trophy(
            "trophy",
            TAR_IGNORE, POS_STANDING, SLOT(0), 30, 12,
            "", "You feel up to making another trophy.", "", CABAL_BATTLE,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_truesight(
            "truesight",
            TAR_IGNORE, POS_STANDING, SLOT(0), 50, 12,
            "", "Your eyes see less truly.", "", CABAL_BATTLE,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_brew(
            "brew",
            TAR_OBJ_INV, POS_STANDING, SLOT(557), 25, 12,
            "", "You feel like you can start brewing again.", "",
            CABAL_SHALAFI, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_shadowlife(
            "shadowlife",
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(558), 80, 12,
            "", "Your feel more shadowy.", "", CABAL_INVADER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_ruler_badge(
            "ruler badge",
            TAR_CHAR_SELF, POS_STANDING, SLOT(560), 50, 12,
            "", "!ruler badge!", "", CABAL_RULER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_remove_badge(
            "remove badge",
            TAR_CHAR_SELF, POS_STANDING, SLOT(561), 100, 12,
            "", "!remove badge!", "", CABAL_RULER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_golden_aura(
            "golden aura",
            TAR_IGNORE, POS_STANDING, SLOT(564), 25, 12, "", "You feel the golden aura dissipate.", "", CABAL_KNIGHT,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_dragonplate(
            "dragonplate",
            TAR_IGNORE, POS_STANDING, SLOT(565), 60, 12, "", "", "", CABAL_KNIGHT,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_squire(
            "squire",
            TAR_IGNORE, POS_STANDING, SLOT(566), 100, 12,
            "", "You feel up to worrying about a new squire.", "", CABAL_KNIGHT,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_dragonsword(
            "dragonsword",
            TAR_IGNORE, POS_STANDING, SLOT(567), 70, 12,
            "", "", "", CABAL_KNIGHT, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_knight(
            "holy armor",
            TAR_CHAR_SELF, POS_RESTING, SLOT(569), 20, 12,
            "", "You are less protected from harm.", "", CABAL_KNIGHT,
            null, ALIGN_NONE, GROUP_CABAL
    ),


    gsn_disperse(
            "disperse",
            TAR_IGNORE, POS_FIGHTING, SLOT(573), 100, 24,
            "", "You feel up to doing more dispersing.", "", CABAL_CHAOS,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_hunt(
            "hunt",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 6,
            "", "!hunt!", "", CABAL_HUNTER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_find_object(
            "find object",
            TAR_IGNORE, POS_STANDING, SLOT(585), 20, 18, "", "!Find Object!", "", CABAL_HUNTER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_path_find(
            "path find",
            TAR_IGNORE, POS_SLEEPING, SLOT(0), 0, 0,
            "", "!endur!", "", CABAL_HUNTER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_riding(
            "riding",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 6,
            "", "!riding!", "", CABAL_KNIGHT,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_wolf(
            "wolf",
            TAR_IGNORE, POS_STANDING, SLOT(593), 100, 12, "", "You feel you can handle more wolfs now.", "", CABAL_HUNTER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_wolf_spirit(
            "wolf spirit",
            TAR_CHAR_SELF, POS_STANDING, SLOT(685), 50, 12, "", "The blood in your vains start to flow as normal.", "",
            CABAL_HUNTER, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_armor_use(
            "armor use",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "", "!Armor Use!", "", CABAL_HUNTER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_world_find(
            "world find",
            TAR_IGNORE, POS_SLEEPING, SLOT(0), 0, 0,
            "", "!world find!", "", CABAL_HUNTER,
            null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_take_revenge(
            "take revenge",
            TAR_IGNORE, POS_STANDING, SLOT(624), 50, 12, "", "!take revenge!", "",
            CABAL_HUNTER, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_mastering_spell(
            "mastering spell",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 0, 0,
            "mastering spell", "!mastering spell!", "",
            CABAL_SHALAFI, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_guard_dogs(
            "guard dogs",
            TAR_IGNORE, POS_FIGHTING, SLOT(687), 100, 12, "", "", "",
            CABAL_LIONS, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_eyes_of_tiger(
            "eyes of tiger",
            TAR_IGNORE, POS_FIGHTING, SLOT(688), 20, 12, "", "", "",
            CABAL_LIONS, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_lion_shield(
            "lion shield",
            TAR_IGNORE, POS_FIGHTING, SLOT(689), 200, 12, "", "", "",
            CABAL_LIONS, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_evolve_lion(
            "evolve lion",
            TAR_IGNORE, POS_FIGHTING, SLOT(690), 50, 12, "", "", "",
            CABAL_LIONS, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_claw(
            "claw",
            TAR_IGNORE, POS_FIGHTING, SLOT(0), 50, 24,
            "claw", "", "",
            CABAL_LIONS, null, ALIGN_NONE, GROUP_CABAL
    ),

    gsn_prevent(
            "prevent",
            TAR_IGNORE, POS_FIGHTING, SLOT(691), 75, 12, "", "", "",
            CABAL_LIONS, null, ALIGN_NONE, GROUP_CABAL
    ),

    /* object spells*/

    gsn_terangreal(
            "terangreal", Magic2::spell_terangreal,
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(607), 5, 12,
            "terangreal", "You are awake again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_kassandra(
            "kassandra", Magic2::spell_kassandra,
            TAR_CHAR_SELF, POS_STANDING, SLOT(608), 5, 12,
            "", "You can heal yourself again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_sebat(
            "sebat", Magic2::spell_sebat,
            TAR_CHAR_SELF, POS_STANDING, SLOT(609), 5, 12,
            "", "You can protect yourself again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_matandra(
            "matandra", Magic2::spell_matandra,
            TAR_CHAR_OFFENSIVE, POS_STANDING, SLOT(610), 5, 12,
            "holy word", "You can use kassandra again.", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_demand(
            "demand",
            TAR_IGNORE, POS_STANDING, SLOT(0), 5, 12,
            "", "", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_bury(
            "bury",
            TAR_IGNORE, POS_STANDING, SLOT(0), 5, 12,
            "", "", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),

    gsn_x_hit(
            "gsn_x_hit",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    ),
    gsn_x_hunger(
            "gsn_x_hunger",
            TAR_IGNORE, POS_STANDING, SLOT(0), 0, 0,
            "", "", "", CABAL_NONE,
            null, ALIGN_NONE, GROUP_NONE
    );


    static int MAX_SKILLS = Skill.values().length;

    /**
     * Name of skill
     */
    public final String name;
    /**
     * Level needed by class
     */
    public final int[] skill_level = new int[MAX_CLASS];
    /**
     * How hard it is to learn
     */
    public final int[] rating = new int[MAX_CLASS];
    public final int[] mod = new int[MAX_CLASS];
    /**
     * Legal targets
     */
    public final int target;
    /**
     * Position for caster / user
     */
    public final int minimum_position;
    /**
     * Slot for #OBJECT loading
     */
    public final int slot;
    /**
     * Minimum mana used
     */
    public final int min_mana;
    /**
     * Waiting time after use
     */
    public final int beats;
    /**
     * Damage message
     */
    public final String noun_damage;
    /**
     * Wear off message
     */
    public final String msg_off;
    /**
     * Wear off message for obects
     */
    public final String msg_obj;
    /**
     * Cabal spells
     */
    public final int cabal;
    /**
     * Race spells
     */
    public final Race race;
    /**
     * alignment of spells
     */
    public final int align;
    /**
     * skill group for practicing
     */
    public final int group;

    public final SpellFun spellFun;

    /**
     * list of all skills. do not modify this list
     */
    static final Skill[] skills = Skill.values();

    private static final Map<String, Skill> skillMap = new HashMap<>();

    static {
        for (Skill skill : skills) {
            skillMap.put(skill.name, skill);
        }
    }

    Skill(String name, SpellFun spellFun, int target, int minimum_position, int slot, int min_mana, int beats, String noun_damage, String msg_off, String msg_obj, int cabal, Race race, int align, int group) {
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
        this.spellFun = spellFun;
        Arrays.fill(skill_level, 100);
    }

    Skill(String name, int target, int minimum_position, int slot, int min_mana, int beats, String noun_damage, String msg_off, String msg_obj, int cabal, Race race, int align, int group) {
        this(name, (SpellFun) null, target, minimum_position, slot, min_mana, beats, noun_damage, msg_off, msg_obj, cabal, race, align, group);
    }

    Skill(String name, SpellFun123 spellFun, int target, int minimum_position, int slot, int min_mana, int beats, String noun_damage, String msg_off, String msg_obj, int cabal, Race race, int align, int group) {
        this(name, (sn, level, ch, vo, target1) -> spellFun.do_spell(sn, level, ch),
                target, minimum_position, slot, min_mana, beats, noun_damage, msg_off, msg_obj, cabal, race, align, group);
    }

    Skill(String name, SpellFun134 spellFun, int target, int minimum_position, int slot, int min_mana, int beats, String noun_damage, String msg_off, String msg_obj, int cabal, Race race, int align, int group) {
        this(name, (sn, level, ch, vo, target1) -> spellFun.do_spell(sn, ch, vo),
                target, minimum_position, slot, min_mana, beats, noun_damage, msg_off, msg_obj, cabal, race, align, group);
    }

    Skill(String name, SpellFun1234 spellFun, int target, int minimum_position, int slot, int min_mana, int beats, String noun_damage, String msg_off, String msg_obj, int cabal, Race race, int align, int group) {
        this(name, (sn, level, ch, vo, target1) -> spellFun.do_spell(sn, level, ch, vo),
                target, minimum_position, slot, min_mana, beats, noun_damage, msg_off, msg_obj, cabal, race, align, group);
    }

    Skill(String name, SpellFun13 spellFun, int target, int minimum_position, int slot, int min_mana, int beats, String noun_damage, String msg_off, String msg_obj, int cabal, Race race, int align, int group) {
        this(name, (sn, level, ch, vo, target1) -> spellFun.do_spell(sn, ch),
                target, minimum_position, slot, min_mana, beats, noun_damage, msg_off, msg_obj, cabal, race, align, group);
    }

    Skill(String name, SpellFun23 spellFun, int target, int minimum_position, int slot, int min_mana, int beats, String noun_damage, String msg_off, String msg_obj, int cabal, Race race, int align, int group) {
        this(name, (sn, level, ch, vo, target1) -> spellFun.do_spell(level, ch),
                target, minimum_position, slot, min_mana, beats, noun_damage, msg_off, msg_obj, cabal, race, align, group);
    }

    Skill(String name, SpellFun34 spellFun, int target, int minimum_position, int slot, int min_mana, int beats, String noun_damage, String msg_off, String msg_obj, int cabal, Race race, int align, int group) {
        this(name, (sn, level, ch, vo, target1) -> spellFun.do_spell(ch, vo),
                target, minimum_position, slot, min_mana, beats, noun_damage, msg_off, msg_obj, cabal, race, align, group);
    }

    Skill(String name, SpellFun234 spellFun, int target, int minimum_position, int slot, int min_mana, int beats, String noun_damage, String msg_off, String msg_obj, int cabal, Race race, int align, int group) {
        this(name, (sn, level, ch, vo, target1) -> spellFun.do_spell(level, ch, vo),
                target, minimum_position, slot, min_mana, beats, noun_damage, msg_off, msg_obj, cabal, race, align, group);
    }

    Skill(String name, SpellFun2345 spellFun, int target, int minimum_position, int slot, int min_mana, int beats, String noun_damage, String msg_off, String msg_obj, int cabal, Race race, int align, int group) {
        this(name, (sn, level, ch, vo, target1) -> spellFun.do_spell(level, ch, vo, target1),
                target, minimum_position, slot, min_mana, beats, noun_damage, msg_off, msg_obj, cabal, race, align, group);
    }


    Skill(String name, SpellFun3 spellFun, int target, int minimum_position, int slot, int min_mana, int beats, String noun_damage, String msg_off, String msg_obj, int cabal, Race race, int align, int group) {
        this(name, (sn, level, ch, vo, target1) -> spellFun.do_spell(ch),
                target, minimum_position, slot, min_mana, beats, noun_damage, msg_off, msg_obj, cabal, race, align, group);
    }


    public boolean isSpell() {
        return spellFun != null;
    }

    static int SLOT(int n) {
        return n;
    }

    void spell_fun(int level, CHAR_DATA ch, Object vo, int target) {
        if (spellFun != null) {
            spellFun.do_spell(this, level, ch, vo, target);
        }
    }

    static int skill_num_lookup(String name) {
        Skill skill = lookupSkill(name);
        return skill == null ? -1 : skill.ordinal();
    }

    /*
     * Lookup a skill by name.
     */
    static Skill lookupSkill(String name) {
        if (name.isEmpty()) {
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

    static Skill find_spell(@NotNull CHAR_DATA ch, String name) {
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

    interface SpellFun {
        void do_spell(Skill sn, int level, CHAR_DATA ch, Object vo, int target);
    }

    private interface SpellFun123 {
        void do_spell(Skill sn, int level, CHAR_DATA ch);
    }

    private interface SpellFun1234 {
        void do_spell(Skill sn, int level, CHAR_DATA ch, Object vo);
    }

    interface SpellFun13 {
        void do_spell(Skill sn, CHAR_DATA ch);
    }

    interface SpellFun134 {
        void do_spell(Skill sn, CHAR_DATA ch, Object vo);
    }

    private interface SpellFun23 {
        void do_spell(int level, CHAR_DATA ch);
    }

    private interface SpellFun234 {
        void do_spell(int level, CHAR_DATA ch, Object vo);
    }

    private interface SpellFun2345 {
        void do_spell(int level, CHAR_DATA ch, Object vo, int target);
    }

    private interface SpellFun3 {
        void do_spell(@NotNull CHAR_DATA ch);
    }

    private interface SpellFun34 {
        void do_spell(@NotNull CHAR_DATA ch, Object vo);
    }
}
