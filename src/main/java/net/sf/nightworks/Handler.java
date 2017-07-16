package net.sf.nightworks;

import java.util.Formatter;

import static net.sf.nightworks.ActComm.die_follower;
import static net.sf.nightworks.ActComm.do_say;
import static net.sf.nightworks.ActComm.is_same_group;
import static net.sf.nightworks.ActComm.nuke_pets;
import static net.sf.nightworks.ActInfo.do_raffects;
import static net.sf.nightworks.ActMove.do_track;
import static net.sf.nightworks.ActMove.do_wake;
import static net.sf.nightworks.ActWiz.do_return;
import static net.sf.nightworks.ActWiz.find_location;
import static net.sf.nightworks.Comm.act;
import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.Const.attack_table;
import static net.sf.nightworks.Const.hometown_table;
import static net.sf.nightworks.Const.item_table;
import static net.sf.nightworks.Const.liq_table;
import static net.sf.nightworks.Const.str_app;
import static net.sf.nightworks.Const.weapon_table;
import static net.sf.nightworks.Const.wiznet_table;
import static net.sf.nightworks.DB.bug;
import static net.sf.nightworks.DB.create_object;
import static net.sf.nightworks.DB.dice;
import static net.sf.nightworks.DB.get_obj_index;
import static net.sf.nightworks.DB.get_room_index;
import static net.sf.nightworks.DB.number_bits;
import static net.sf.nightworks.DB.number_fuzzy;
import static net.sf.nightworks.DB.number_range;
import static net.sf.nightworks.DB.weather_info;
import static net.sf.nightworks.Fight.damage;
import static net.sf.nightworks.Fight.stop_fighting;
import static net.sf.nightworks.Interp.number_argument;
import static net.sf.nightworks.Magic.saves_spell;
import static net.sf.nightworks.Nightworks.ACT_AGGRESSIVE;
import static net.sf.nightworks.Nightworks.ACT_CLERIC;
import static net.sf.nightworks.Nightworks.ACT_GAIN;
import static net.sf.nightworks.Nightworks.ACT_HUNTER;
import static net.sf.nightworks.Nightworks.ACT_IS_CHANGER;
import static net.sf.nightworks.Nightworks.ACT_IS_HEALER;
import static net.sf.nightworks.Nightworks.ACT_IS_NPC;
import static net.sf.nightworks.Nightworks.ACT_MAGE;
import static net.sf.nightworks.Nightworks.ACT_NOALIGN;
import static net.sf.nightworks.Nightworks.ACT_NOPURGE;
import static net.sf.nightworks.Nightworks.ACT_PET;
import static net.sf.nightworks.Nightworks.ACT_PRACTICE;
import static net.sf.nightworks.Nightworks.ACT_SCAVENGER;
import static net.sf.nightworks.Nightworks.ACT_SENTINEL;
import static net.sf.nightworks.Nightworks.ACT_STAY_AREA;
import static net.sf.nightworks.Nightworks.ACT_THIEF;
import static net.sf.nightworks.Nightworks.ACT_TRAIN;
import static net.sf.nightworks.Nightworks.ACT_UNDEAD;
import static net.sf.nightworks.Nightworks.ACT_UPDATE_ALWAYS;
import static net.sf.nightworks.Nightworks.ACT_WARRIOR;
import static net.sf.nightworks.Nightworks.ACT_WIMPY;
import static net.sf.nightworks.Nightworks.AFFECT_DATA;
import static net.sf.nightworks.Nightworks.AFF_ACUTE_VISION;
import static net.sf.nightworks.Nightworks.AFF_BERSERK;
import static net.sf.nightworks.Nightworks.AFF_BLIND;
import static net.sf.nightworks.Nightworks.AFF_BLOODTHIRST;
import static net.sf.nightworks.Nightworks.AFF_CALM;
import static net.sf.nightworks.Nightworks.AFF_CAMOUFLAGE;
import static net.sf.nightworks.Nightworks.AFF_CHARM;
import static net.sf.nightworks.Nightworks.AFF_CURSE;
import static net.sf.nightworks.Nightworks.AFF_DARK_VISION;
import static net.sf.nightworks.Nightworks.AFF_DETECT_EVIL;
import static net.sf.nightworks.Nightworks.AFF_DETECT_FADE;
import static net.sf.nightworks.Nightworks.AFF_DETECT_GOOD;
import static net.sf.nightworks.Nightworks.AFF_DETECT_HIDDEN;
import static net.sf.nightworks.Nightworks.AFF_DETECT_IMP_INVIS;
import static net.sf.nightworks.Nightworks.AFF_DETECT_INVIS;
import static net.sf.nightworks.Nightworks.AFF_DETECT_LIFE;
import static net.sf.nightworks.Nightworks.AFF_DETECT_MAGIC;
import static net.sf.nightworks.Nightworks.AFF_DETECT_SNEAK;
import static net.sf.nightworks.Nightworks.AFF_EARTHFADE;
import static net.sf.nightworks.Nightworks.AFF_FADE;
import static net.sf.nightworks.Nightworks.AFF_FAERIE_FIRE;
import static net.sf.nightworks.Nightworks.AFF_FEAR;
import static net.sf.nightworks.Nightworks.AFF_FLYING;
import static net.sf.nightworks.Nightworks.AFF_FORM_GRASS;
import static net.sf.nightworks.Nightworks.AFF_FORM_TREE;
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
import static net.sf.nightworks.Nightworks.AFF_REGENERATION;
import static net.sf.nightworks.Nightworks.AFF_ROOM_CURSE;
import static net.sf.nightworks.Nightworks.AFF_ROOM_L_SHIELD;
import static net.sf.nightworks.Nightworks.AFF_ROOM_PLAGUE;
import static net.sf.nightworks.Nightworks.AFF_ROOM_POISON;
import static net.sf.nightworks.Nightworks.AFF_ROOM_SHOCKING;
import static net.sf.nightworks.Nightworks.AFF_ROOM_SLEEP;
import static net.sf.nightworks.Nightworks.AFF_ROOM_SLOW;
import static net.sf.nightworks.Nightworks.AFF_ROOM_THIEF_TRAP;
import static net.sf.nightworks.Nightworks.AFF_SANCTUARY;
import static net.sf.nightworks.Nightworks.AFF_SCREAM;
import static net.sf.nightworks.Nightworks.AFF_SLEEP;
import static net.sf.nightworks.Nightworks.AFF_SLOW;
import static net.sf.nightworks.Nightworks.AFF_SNEAK;
import static net.sf.nightworks.Nightworks.AFF_STUN;
import static net.sf.nightworks.Nightworks.AFF_SWIM;
import static net.sf.nightworks.Nightworks.AFF_WEAKEN;
import static net.sf.nightworks.Nightworks.AFF_WEB;
import static net.sf.nightworks.Nightworks.ALIGN_OK;
import static net.sf.nightworks.Nightworks.APPLY_AC;
import static net.sf.nightworks.Nightworks.APPLY_AGE;
import static net.sf.nightworks.Nightworks.APPLY_CHA;
import static net.sf.nightworks.Nightworks.APPLY_CLASS;
import static net.sf.nightworks.Nightworks.APPLY_CON;
import static net.sf.nightworks.Nightworks.APPLY_DAMROLL;
import static net.sf.nightworks.Nightworks.APPLY_DEX;
import static net.sf.nightworks.Nightworks.APPLY_EXP;
import static net.sf.nightworks.Nightworks.APPLY_GOLD;
import static net.sf.nightworks.Nightworks.APPLY_HEIGHT;
import static net.sf.nightworks.Nightworks.APPLY_HIT;
import static net.sf.nightworks.Nightworks.APPLY_HITROLL;
import static net.sf.nightworks.Nightworks.APPLY_INT;
import static net.sf.nightworks.Nightworks.APPLY_LEVEL;
import static net.sf.nightworks.Nightworks.APPLY_MANA;
import static net.sf.nightworks.Nightworks.APPLY_MOVE;
import static net.sf.nightworks.Nightworks.APPLY_NONE;
import static net.sf.nightworks.Nightworks.APPLY_ROOM_HEAL;
import static net.sf.nightworks.Nightworks.APPLY_ROOM_MANA;
import static net.sf.nightworks.Nightworks.APPLY_ROOM_NONE;
import static net.sf.nightworks.Nightworks.APPLY_SAVES;
import static net.sf.nightworks.Nightworks.APPLY_SAVING_BREATH;
import static net.sf.nightworks.Nightworks.APPLY_SAVING_PETRI;
import static net.sf.nightworks.Nightworks.APPLY_SAVING_ROD;
import static net.sf.nightworks.Nightworks.APPLY_SAVING_SPELL;
import static net.sf.nightworks.Nightworks.APPLY_SIZE;
import static net.sf.nightworks.Nightworks.APPLY_SPELL_AFFECT;
import static net.sf.nightworks.Nightworks.APPLY_STR;
import static net.sf.nightworks.Nightworks.APPLY_WEIGHT;
import static net.sf.nightworks.Nightworks.APPLY_WIS;
import static net.sf.nightworks.Nightworks.ASSIST_ALIGN;
import static net.sf.nightworks.Nightworks.ASSIST_ALL;
import static net.sf.nightworks.Nightworks.ASSIST_GUARD;
import static net.sf.nightworks.Nightworks.ASSIST_PLAYERS;
import static net.sf.nightworks.Nightworks.ASSIST_RACE;
import static net.sf.nightworks.Nightworks.ASSIST_VNUM;
import static net.sf.nightworks.Nightworks.CABAL_NONE;
import static net.sf.nightworks.Nightworks.CABAL_OK;
import static net.sf.nightworks.Nightworks.CAN_WEAR;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.CLEVEL_OK;
import static net.sf.nightworks.Nightworks.COMM_BRIEF;
import static net.sf.nightworks.Nightworks.COMM_COMBINE;
import static net.sf.nightworks.Nightworks.COMM_COMPACT;
import static net.sf.nightworks.Nightworks.COMM_DEAF;
import static net.sf.nightworks.Nightworks.COMM_NOAUCTION;
import static net.sf.nightworks.Nightworks.COMM_NOCHANNELS;
import static net.sf.nightworks.Nightworks.COMM_NOEMOTE;
import static net.sf.nightworks.Nightworks.COMM_NOGOSSIP;
import static net.sf.nightworks.Nightworks.COMM_NOMUSIC;
import static net.sf.nightworks.Nightworks.COMM_NOQUESTION;
import static net.sf.nightworks.Nightworks.COMM_NOQUOTE;
import static net.sf.nightworks.Nightworks.COMM_NOSHOUT;
import static net.sf.nightworks.Nightworks.COMM_NOTELL;
import static net.sf.nightworks.Nightworks.COMM_NOWIZ;
import static net.sf.nightworks.Nightworks.COMM_PROMPT;
import static net.sf.nightworks.Nightworks.COMM_QUIET;
import static net.sf.nightworks.Nightworks.COMM_true_TRUST;
import static net.sf.nightworks.Nightworks.COND_DRUNK;
import static net.sf.nightworks.Nightworks.CONT_CLOSEABLE;
import static net.sf.nightworks.Nightworks.CONT_CLOSED;
import static net.sf.nightworks.Nightworks.CONT_LOCKED;
import static net.sf.nightworks.Nightworks.CONT_PICKPROOF;
import static net.sf.nightworks.Nightworks.DAM_ACID;
import static net.sf.nightworks.Nightworks.DAM_BASH;
import static net.sf.nightworks.Nightworks.DAM_CHARM;
import static net.sf.nightworks.Nightworks.DAM_COLD;
import static net.sf.nightworks.Nightworks.DAM_DISEASE;
import static net.sf.nightworks.Nightworks.DAM_DROWNING;
import static net.sf.nightworks.Nightworks.DAM_ENERGY;
import static net.sf.nightworks.Nightworks.DAM_FIRE;
import static net.sf.nightworks.Nightworks.DAM_HOLY;
import static net.sf.nightworks.Nightworks.DAM_LIGHT;
import static net.sf.nightworks.Nightworks.DAM_LIGHTNING;
import static net.sf.nightworks.Nightworks.DAM_MENTAL;
import static net.sf.nightworks.Nightworks.DAM_NEGATIVE;
import static net.sf.nightworks.Nightworks.DAM_NONE;
import static net.sf.nightworks.Nightworks.DAM_PIERCE;
import static net.sf.nightworks.Nightworks.DAM_POISON;
import static net.sf.nightworks.Nightworks.DAM_SLASH;
import static net.sf.nightworks.Nightworks.DAM_SOUND;
import static net.sf.nightworks.Nightworks.DAM_TRAP_ROOM;
import static net.sf.nightworks.Nightworks.EXIT_DATA;
import static net.sf.nightworks.Nightworks.EX_CLOSED;
import static net.sf.nightworks.Nightworks.FIGHT_DELAY_TIME;
import static net.sf.nightworks.Nightworks.FORM_AMPHIBIAN;
import static net.sf.nightworks.Nightworks.FORM_ANIMAL;
import static net.sf.nightworks.Nightworks.FORM_BIPED;
import static net.sf.nightworks.Nightworks.FORM_BIRD;
import static net.sf.nightworks.Nightworks.FORM_BLOB;
import static net.sf.nightworks.Nightworks.FORM_CENTAUR;
import static net.sf.nightworks.Nightworks.FORM_COLD_BLOOD;
import static net.sf.nightworks.Nightworks.FORM_CONSTRUCT;
import static net.sf.nightworks.Nightworks.FORM_CRUSTACEAN;
import static net.sf.nightworks.Nightworks.FORM_DRAGON;
import static net.sf.nightworks.Nightworks.FORM_EDIBLE;
import static net.sf.nightworks.Nightworks.FORM_FISH;
import static net.sf.nightworks.Nightworks.FORM_INSECT;
import static net.sf.nightworks.Nightworks.FORM_INSTANT_DECAY;
import static net.sf.nightworks.Nightworks.FORM_INTANGIBLE;
import static net.sf.nightworks.Nightworks.FORM_MAGICAL;
import static net.sf.nightworks.Nightworks.FORM_MAMMAL;
import static net.sf.nightworks.Nightworks.FORM_MIST;
import static net.sf.nightworks.Nightworks.FORM_OTHER;
import static net.sf.nightworks.Nightworks.FORM_POISON;
import static net.sf.nightworks.Nightworks.FORM_REPTILE;
import static net.sf.nightworks.Nightworks.FORM_SENTIENT;
import static net.sf.nightworks.Nightworks.FORM_SNAKE;
import static net.sf.nightworks.Nightworks.FORM_SPIDER;
import static net.sf.nightworks.Nightworks.FORM_UNDEAD;
import static net.sf.nightworks.Nightworks.FORM_WORM;
import static net.sf.nightworks.Nightworks.IMM_ACID;
import static net.sf.nightworks.Nightworks.IMM_BASH;
import static net.sf.nightworks.Nightworks.IMM_CHARM;
import static net.sf.nightworks.Nightworks.IMM_COLD;
import static net.sf.nightworks.Nightworks.IMM_DISEASE;
import static net.sf.nightworks.Nightworks.IMM_DROWNING;
import static net.sf.nightworks.Nightworks.IMM_ENERGY;
import static net.sf.nightworks.Nightworks.IMM_FIRE;
import static net.sf.nightworks.Nightworks.IMM_HOLY;
import static net.sf.nightworks.Nightworks.IMM_LIGHT;
import static net.sf.nightworks.Nightworks.IMM_LIGHTNING;
import static net.sf.nightworks.Nightworks.IMM_MAGIC;
import static net.sf.nightworks.Nightworks.IMM_MENTAL;
import static net.sf.nightworks.Nightworks.IMM_NEGATIVE;
import static net.sf.nightworks.Nightworks.IMM_PIERCE;
import static net.sf.nightworks.Nightworks.IMM_POISON;
import static net.sf.nightworks.Nightworks.IMM_SLASH;
import static net.sf.nightworks.Nightworks.IMM_SOUND;
import static net.sf.nightworks.Nightworks.IMM_SUMMON;
import static net.sf.nightworks.Nightworks.IMM_WEAPON;
import static net.sf.nightworks.Nightworks.IS_AFFECTED;
import static net.sf.nightworks.Nightworks.IS_EVIL;
import static net.sf.nightworks.Nightworks.IS_GOOD;
import static net.sf.nightworks.Nightworks.IS_IMMORTAL;
import static net.sf.nightworks.Nightworks.IS_IMMUNE;
import static net.sf.nightworks.Nightworks.IS_NEUTRAL;
import static net.sf.nightworks.Nightworks.IS_NORMAL;
import static net.sf.nightworks.Nightworks.IS_NPC;
import static net.sf.nightworks.Nightworks.IS_OBJ_STAT;
import static net.sf.nightworks.Nightworks.IS_RESISTANT;
import static net.sf.nightworks.Nightworks.IS_ROOM_AFFECTED;
import static net.sf.nightworks.Nightworks.IS_SET;
import static net.sf.nightworks.Nightworks.IS_VAMPIRE;
import static net.sf.nightworks.Nightworks.IS_VULNERABLE;
import static net.sf.nightworks.Nightworks.IS_WATER;
import static net.sf.nightworks.Nightworks.ITEM_ANTI_EVIL;
import static net.sf.nightworks.Nightworks.ITEM_ANTI_GOOD;
import static net.sf.nightworks.Nightworks.ITEM_ANTI_NEUTRAL;
import static net.sf.nightworks.Nightworks.ITEM_ARMOR;
import static net.sf.nightworks.Nightworks.ITEM_BLESS;
import static net.sf.nightworks.Nightworks.ITEM_BOAT;
import static net.sf.nightworks.Nightworks.ITEM_BURIED;
import static net.sf.nightworks.Nightworks.ITEM_BURN_PROOF;
import static net.sf.nightworks.Nightworks.ITEM_CLOTHING;
import static net.sf.nightworks.Nightworks.ITEM_CONTAINER;
import static net.sf.nightworks.Nightworks.ITEM_CORPSE_NPC;
import static net.sf.nightworks.Nightworks.ITEM_CORPSE_PC;
import static net.sf.nightworks.Nightworks.ITEM_DARK;
import static net.sf.nightworks.Nightworks.ITEM_DRINK_CON;
import static net.sf.nightworks.Nightworks.ITEM_EVIL;
import static net.sf.nightworks.Nightworks.ITEM_FOOD;
import static net.sf.nightworks.Nightworks.ITEM_FOUNTAIN;
import static net.sf.nightworks.Nightworks.ITEM_FURNITURE;
import static net.sf.nightworks.Nightworks.ITEM_GEM;
import static net.sf.nightworks.Nightworks.ITEM_GLOW;
import static net.sf.nightworks.Nightworks.ITEM_HOLD;
import static net.sf.nightworks.Nightworks.ITEM_HUM;
import static net.sf.nightworks.Nightworks.ITEM_INVENTORY;
import static net.sf.nightworks.Nightworks.ITEM_INVIS;
import static net.sf.nightworks.Nightworks.ITEM_JEWELRY;
import static net.sf.nightworks.Nightworks.ITEM_JUKEBOX;
import static net.sf.nightworks.Nightworks.ITEM_KEY;
import static net.sf.nightworks.Nightworks.ITEM_LIGHT;
import static net.sf.nightworks.Nightworks.ITEM_LOCK;
import static net.sf.nightworks.Nightworks.ITEM_MAGIC;
import static net.sf.nightworks.Nightworks.ITEM_MAP;
import static net.sf.nightworks.Nightworks.ITEM_MONEY;
import static net.sf.nightworks.Nightworks.ITEM_NODROP;
import static net.sf.nightworks.Nightworks.ITEM_NOLOCATE;
import static net.sf.nightworks.Nightworks.ITEM_NOPURGE;
import static net.sf.nightworks.Nightworks.ITEM_NOREMOVE;
import static net.sf.nightworks.Nightworks.ITEM_NOUNCURSE;
import static net.sf.nightworks.Nightworks.ITEM_PILL;
import static net.sf.nightworks.Nightworks.ITEM_PORTAL;
import static net.sf.nightworks.Nightworks.ITEM_POTION;
import static net.sf.nightworks.Nightworks.ITEM_ROT_DEATH;
import static net.sf.nightworks.Nightworks.ITEM_SCROLL;
import static net.sf.nightworks.Nightworks.ITEM_SELL_EXTRACT;
import static net.sf.nightworks.Nightworks.ITEM_STAFF;
import static net.sf.nightworks.Nightworks.ITEM_TAKE;
import static net.sf.nightworks.Nightworks.ITEM_TATTOO;
import static net.sf.nightworks.Nightworks.ITEM_TRASH;
import static net.sf.nightworks.Nightworks.ITEM_TREASURE;
import static net.sf.nightworks.Nightworks.ITEM_VIS_DEATH;
import static net.sf.nightworks.Nightworks.ITEM_WAND;
import static net.sf.nightworks.Nightworks.ITEM_WARP_STONE;
import static net.sf.nightworks.Nightworks.ITEM_WEAPON;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_ABOUT;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_ARMS;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_BODY;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_FEET;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_FINGER;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_FLOAT;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_HANDS;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_HEAD;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_LEGS;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_NECK;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_SHIELD;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_TATTOO;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_WAIST;
import static net.sf.nightworks.Nightworks.ITEM_WEAR_WRIST;
import static net.sf.nightworks.Nightworks.ITEM_WIELD;
import static net.sf.nightworks.Nightworks.LEFT_HANDER;
import static net.sf.nightworks.Nightworks.LEVEL_HERO;
import static net.sf.nightworks.Nightworks.LEVEL_IMMORTAL;
import static net.sf.nightworks.Nightworks.LIQ_WATER;
import static net.sf.nightworks.Nightworks.MAX_CABAL;
import static net.sf.nightworks.Nightworks.MAX_CHARM;
import static net.sf.nightworks.Nightworks.MAX_FINGER;
import static net.sf.nightworks.Nightworks.MAX_LEVEL;
import static net.sf.nightworks.Nightworks.MAX_NECK;
import static net.sf.nightworks.Nightworks.MAX_STUCK_IN;
import static net.sf.nightworks.Nightworks.MAX_TATTOO;
import static net.sf.nightworks.Nightworks.MAX_WEAR;
import static net.sf.nightworks.Nightworks.MAX_WRIST;
import static net.sf.nightworks.Nightworks.MOUNTED;
import static net.sf.nightworks.Nightworks.OBJ_DATA;
import static net.sf.nightworks.Nightworks.OBJ_INDEX_DATA;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_COINS;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_GOLD_ONE;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_GOLD_SOME;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_MAGIC_JAR;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_PIT;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SILVER_ONE;
import static net.sf.nightworks.Nightworks.OBJ_VNUM_SILVER_SOME;
import static net.sf.nightworks.Nightworks.OFF_AREA_ATTACK;
import static net.sf.nightworks.Nightworks.OFF_BACKSTAB;
import static net.sf.nightworks.Nightworks.OFF_BASH;
import static net.sf.nightworks.Nightworks.OFF_BERSERK;
import static net.sf.nightworks.Nightworks.OFF_CRUSH;
import static net.sf.nightworks.Nightworks.OFF_DISARM;
import static net.sf.nightworks.Nightworks.OFF_DODGE;
import static net.sf.nightworks.Nightworks.OFF_FADE;
import static net.sf.nightworks.Nightworks.OFF_FAST;
import static net.sf.nightworks.Nightworks.OFF_KICK;
import static net.sf.nightworks.Nightworks.OFF_KICK_DIRT;
import static net.sf.nightworks.Nightworks.OFF_PARRY;
import static net.sf.nightworks.Nightworks.OFF_RESCUE;
import static net.sf.nightworks.Nightworks.OFF_TAIL;
import static net.sf.nightworks.Nightworks.OFF_TRIP;
import static net.sf.nightworks.Nightworks.OPROG_REMOVE;
import static net.sf.nightworks.Nightworks.OPROG_WEAR;
import static net.sf.nightworks.Nightworks.ORG_RACE;
import static net.sf.nightworks.Nightworks.PART_ARMS;
import static net.sf.nightworks.Nightworks.PART_BRAINS;
import static net.sf.nightworks.Nightworks.PART_CLAWS;
import static net.sf.nightworks.Nightworks.PART_EAR;
import static net.sf.nightworks.Nightworks.PART_EYE;
import static net.sf.nightworks.Nightworks.PART_EYESTALKS;
import static net.sf.nightworks.Nightworks.PART_FANGS;
import static net.sf.nightworks.Nightworks.PART_FEET;
import static net.sf.nightworks.Nightworks.PART_FINGERS;
import static net.sf.nightworks.Nightworks.PART_FINS;
import static net.sf.nightworks.Nightworks.PART_GUTS;
import static net.sf.nightworks.Nightworks.PART_HANDS;
import static net.sf.nightworks.Nightworks.PART_HEAD;
import static net.sf.nightworks.Nightworks.PART_HEART;
import static net.sf.nightworks.Nightworks.PART_HORNS;
import static net.sf.nightworks.Nightworks.PART_LEGS;
import static net.sf.nightworks.Nightworks.PART_LONG_TONGUE;
import static net.sf.nightworks.Nightworks.PART_SCALES;
import static net.sf.nightworks.Nightworks.PART_TAIL;
import static net.sf.nightworks.Nightworks.PART_TENTACLES;
import static net.sf.nightworks.Nightworks.PART_WINGS;
import static net.sf.nightworks.Nightworks.PLR_AUTOASSIST;
import static net.sf.nightworks.Nightworks.PLR_AUTOEXIT;
import static net.sf.nightworks.Nightworks.PLR_AUTOGOLD;
import static net.sf.nightworks.Nightworks.PLR_AUTOLOOT;
import static net.sf.nightworks.Nightworks.PLR_AUTOSAC;
import static net.sf.nightworks.Nightworks.PLR_AUTOSPLIT;
import static net.sf.nightworks.Nightworks.PLR_BLINK_ON;
import static net.sf.nightworks.Nightworks.PLR_CANINDUCT;
import static net.sf.nightworks.Nightworks.PLR_CANLOOT;
import static net.sf.nightworks.Nightworks.PLR_CANREMORT;
import static net.sf.nightworks.Nightworks.PLR_CHANGED_AFF;
import static net.sf.nightworks.Nightworks.PLR_COLOR;
import static net.sf.nightworks.Nightworks.PLR_FREEZE;
import static net.sf.nightworks.Nightworks.PLR_GHOST;
import static net.sf.nightworks.Nightworks.PLR_HARA_KIRI;
import static net.sf.nightworks.Nightworks.PLR_HOLYLIGHT;
import static net.sf.nightworks.Nightworks.PLR_LEFTHAND;
import static net.sf.nightworks.Nightworks.PLR_LOG;
import static net.sf.nightworks.Nightworks.PLR_NOCANCEL;
import static net.sf.nightworks.Nightworks.PLR_NOFOLLOW;
import static net.sf.nightworks.Nightworks.PLR_NOSUMMON;
import static net.sf.nightworks.Nightworks.PLR_NO_EXP;
import static net.sf.nightworks.Nightworks.PLR_NO_TITLE;
import static net.sf.nightworks.Nightworks.PLR_PERMIT;
import static net.sf.nightworks.Nightworks.PLR_QUESTOR;
import static net.sf.nightworks.Nightworks.PLR_REMORTED;
import static net.sf.nightworks.Nightworks.PLR_VAMPIRE;
import static net.sf.nightworks.Nightworks.PLR_WANTED;
import static net.sf.nightworks.Nightworks.POS_DEAD;
import static net.sf.nightworks.Nightworks.RACE_OK;
import static net.sf.nightworks.Nightworks.REMOVE_BIT;
import static net.sf.nightworks.Nightworks.RES_MAGIC;
import static net.sf.nightworks.Nightworks.RES_WEAPON;
import static net.sf.nightworks.Nightworks.RIDDEN;
import static net.sf.nightworks.Nightworks.RIGHT_HANDER;
import static net.sf.nightworks.Nightworks.ROOM_BANK;
import static net.sf.nightworks.Nightworks.ROOM_DARK;
import static net.sf.nightworks.Nightworks.ROOM_GODS_ONLY;
import static net.sf.nightworks.Nightworks.ROOM_HEROES_ONLY;
import static net.sf.nightworks.Nightworks.ROOM_HISTORY_DATA;
import static net.sf.nightworks.Nightworks.ROOM_IMP_ONLY;
import static net.sf.nightworks.Nightworks.ROOM_INDEX_DATA;
import static net.sf.nightworks.Nightworks.ROOM_INDOORS;
import static net.sf.nightworks.Nightworks.ROOM_LAW;
import static net.sf.nightworks.Nightworks.ROOM_NEWBIES_ONLY;
import static net.sf.nightworks.Nightworks.ROOM_NOSUMMON;
import static net.sf.nightworks.Nightworks.ROOM_NOWHERE;
import static net.sf.nightworks.Nightworks.ROOM_NO_MAGIC;
import static net.sf.nightworks.Nightworks.ROOM_NO_MOB;
import static net.sf.nightworks.Nightworks.ROOM_NO_RECALL;
import static net.sf.nightworks.Nightworks.ROOM_PET_SHOP;
import static net.sf.nightworks.Nightworks.ROOM_PRIVATE;
import static net.sf.nightworks.Nightworks.ROOM_REGISTRY;
import static net.sf.nightworks.Nightworks.ROOM_SAFE;
import static net.sf.nightworks.Nightworks.ROOM_SOLITARY;
import static net.sf.nightworks.Nightworks.ROOM_VNUM_LIMBO;
import static net.sf.nightworks.Nightworks.ROOM_VNUM_TEMPLE;
import static net.sf.nightworks.Nightworks.SECT_CITY;
import static net.sf.nightworks.Nightworks.SECT_INSIDE;
import static net.sf.nightworks.Nightworks.SET_BIT;
import static net.sf.nightworks.Nightworks.STAT_CHA;
import static net.sf.nightworks.Nightworks.STAT_CON;
import static net.sf.nightworks.Nightworks.STAT_DEX;
import static net.sf.nightworks.Nightworks.STAT_INT;
import static net.sf.nightworks.Nightworks.STAT_STR;
import static net.sf.nightworks.Nightworks.STAT_WIS;
import static net.sf.nightworks.Nightworks.SUN_DARK;
import static net.sf.nightworks.Nightworks.SUN_LIGHT;
import static net.sf.nightworks.Nightworks.SUN_RISE;
import static net.sf.nightworks.Nightworks.SUN_SET;
import static net.sf.nightworks.Nightworks.TO_ACT_FLAG;
import static net.sf.nightworks.Nightworks.TO_AFFECTS;
import static net.sf.nightworks.Nightworks.TO_CHAR;
import static net.sf.nightworks.Nightworks.TO_IMMUNE;
import static net.sf.nightworks.Nightworks.TO_OBJECT;
import static net.sf.nightworks.Nightworks.TO_RACE;
import static net.sf.nightworks.Nightworks.TO_RESIST;
import static net.sf.nightworks.Nightworks.TO_ROOM;
import static net.sf.nightworks.Nightworks.TO_ROOM_AFFECTS;
import static net.sf.nightworks.Nightworks.TO_ROOM_CONST;
import static net.sf.nightworks.Nightworks.TO_ROOM_FLAGS;
import static net.sf.nightworks.Nightworks.TO_VULN;
import static net.sf.nightworks.Nightworks.TO_WEAPON;
import static net.sf.nightworks.Nightworks.UMAX;
import static net.sf.nightworks.Nightworks.UMIN;
import static net.sf.nightworks.Nightworks.URANGE;
import static net.sf.nightworks.Nightworks.VULN_IRON;
import static net.sf.nightworks.Nightworks.VULN_MAGIC;
import static net.sf.nightworks.Nightworks.VULN_SILVER;
import static net.sf.nightworks.Nightworks.VULN_WEAPON;
import static net.sf.nightworks.Nightworks.VULN_WOOD;
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
import static net.sf.nightworks.Nightworks.WEAPON_TWO_HANDS;
import static net.sf.nightworks.Nightworks.WEAPON_VAMPIRIC;
import static net.sf.nightworks.Nightworks.WEAPON_VORPAL;
import static net.sf.nightworks.Nightworks.WEAPON_WHIP;
import static net.sf.nightworks.Nightworks.WEAR_ABOUT;
import static net.sf.nightworks.Nightworks.WEAR_ARMS;
import static net.sf.nightworks.Nightworks.WEAR_BODY;
import static net.sf.nightworks.Nightworks.WEAR_BOTH;
import static net.sf.nightworks.Nightworks.WEAR_FEET;
import static net.sf.nightworks.Nightworks.WEAR_FINGER;
import static net.sf.nightworks.Nightworks.WEAR_HANDS;
import static net.sf.nightworks.Nightworks.WEAR_HEAD;
import static net.sf.nightworks.Nightworks.WEAR_LEFT;
import static net.sf.nightworks.Nightworks.WEAR_LEGS;
import static net.sf.nightworks.Nightworks.WEAR_NECK;
import static net.sf.nightworks.Nightworks.WEAR_NONE;
import static net.sf.nightworks.Nightworks.WEAR_RIGHT;
import static net.sf.nightworks.Nightworks.WEAR_STUCK_IN;
import static net.sf.nightworks.Nightworks.WEAR_TATTOO;
import static net.sf.nightworks.Nightworks.WEAR_WAIST;
import static net.sf.nightworks.Nightworks.WEAR_WRIST;
import static net.sf.nightworks.Nightworks.WEIGHT_MULT;
import static net.sf.nightworks.Nightworks.char_list;
import static net.sf.nightworks.Nightworks.currentTimeSeconds;
import static net.sf.nightworks.Nightworks.current_time;
import static net.sf.nightworks.Nightworks.limit_time;
import static net.sf.nightworks.Nightworks.nw_config;
import static net.sf.nightworks.Nightworks.object_list;
import static net.sf.nightworks.Nightworks.top_affected_room;
import static net.sf.nightworks.Recycle.free_char;
import static net.sf.nightworks.Skill.gsn_arrow;
import static net.sf.nightworks.Skill.gsn_axe;
import static net.sf.nightworks.Skill.gsn_backstab;
import static net.sf.nightworks.Skill.gsn_bash;
import static net.sf.nightworks.Skill.gsn_berserk;
import static net.sf.nightworks.Skill.gsn_bow;
import static net.sf.nightworks.Skill.gsn_dagger;
import static net.sf.nightworks.Skill.gsn_disarm;
import static net.sf.nightworks.Skill.gsn_dodge;
import static net.sf.nightworks.Skill.gsn_doppelganger;
import static net.sf.nightworks.Skill.gsn_flail;
import static net.sf.nightworks.Skill.gsn_fourth_attack;
import static net.sf.nightworks.Skill.gsn_grip;
import static net.sf.nightworks.Skill.gsn_hand_to_hand;
import static net.sf.nightworks.Skill.gsn_hide;
import static net.sf.nightworks.Skill.gsn_kick;
import static net.sf.nightworks.Skill.gsn_lance;
import static net.sf.nightworks.Skill.gsn_mace;
import static net.sf.nightworks.Skill.gsn_parry;
import static net.sf.nightworks.Skill.gsn_plague;
import static net.sf.nightworks.Skill.gsn_polearm;
import static net.sf.nightworks.Skill.gsn_recall;
import static net.sf.nightworks.Skill.gsn_rescue;
import static net.sf.nightworks.Skill.gsn_second_attack;
import static net.sf.nightworks.Skill.gsn_second_weapon;
import static net.sf.nightworks.Skill.gsn_settraps;
import static net.sf.nightworks.Skill.gsn_shield_block;
import static net.sf.nightworks.Skill.gsn_sneak;
import static net.sf.nightworks.Skill.gsn_spear;
import static net.sf.nightworks.Skill.gsn_sword;
import static net.sf.nightworks.Skill.gsn_third_attack;
import static net.sf.nightworks.Skill.gsn_trip;
import static net.sf.nightworks.Skill.gsn_whip;
import static net.sf.nightworks.Skill.gsn_x_hunger;
import static net.sf.nightworks.Tables.cabal_table;
import static net.sf.nightworks.util.TextUtils.is_number;
import static net.sf.nightworks.util.TextUtils.one_argument;
import static net.sf.nightworks.util.TextUtils.str_cmp;
import static net.sf.nightworks.util.TextUtils.str_prefix;

class Handler {
/* friend stuff -- for NPC's mostly */

    static boolean is_friend(CHAR_DATA ch, CHAR_DATA victim) {
        if (is_same_group(ch, victim)) {
            return true;
        }

        if (!IS_NPC(ch)) {
            return false;
        }

        if (!IS_NPC(victim)) {
            return IS_SET(ch.off_flags, ASSIST_PLAYERS);
        }

        if (IS_AFFECTED(ch, AFF_CHARM)) {
            return false;
        }

        if (IS_SET(ch.off_flags, ASSIST_ALL)) {
            return true;
        }

        if (ch.group != 0 && ch.group == victim.group) {
            return true;
        }

        if (IS_SET(ch.off_flags, ASSIST_VNUM)
                && ch.pIndexData == victim.pIndexData) {
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (IS_SET(ch.off_flags, ASSIST_RACE) && ch.race == victim.race) {
            return true;
        }

        return IS_SET(ch.off_flags, ASSIST_ALIGN)
                && !IS_SET(ch.act, ACT_NOALIGN) && !IS_SET(victim.act, ACT_NOALIGN)
                && ((IS_GOOD(ch) && IS_GOOD(victim))
                || (IS_EVIL(ch) && IS_EVIL(victim))
                || (IS_NEUTRAL(ch) && IS_NEUTRAL(victim)));

    }

    /**
     * Room record:
     * For less than 5 people in room create a new record.
     * Else use the oldest one.
     */
    static void room_record(String name, ROOM_INDEX_DATA room, int door) {
        ROOM_HISTORY_DATA rh;
        int i = 0;

        for (rh = room.history; i < 5 && rh != null; i++, rh = rh.next) ;

        if (i < 5) {
            rh = new ROOM_HISTORY_DATA();
        } else {
            rh = room.history.next.next.next.next;
            room.history.next.next.next.next = null;
        }

        rh.next = room.history;
        room.history = rh;
        rh.name = name;
        rh.went = door;
    }

/* returns number of people on an object */

    static int count_users(OBJ_DATA obj) {
        CHAR_DATA fch;
        int count = 0;

        if (obj.in_room == null) {
            return 0;
        }

        for (fch = obj.in_room.people; fch != null; fch = fch.next_in_room) {
            if (fch.on == obj) {
                count++;
            }
        }

        return count;
    }

/* returns material number */

    static int material_lookup(String name) {
        return 0;
    }

/* returns race number */


    static int liq_lookup(String name) {
        int liq;

        for (liq = 0; liq_table[liq].liq_name != null; liq++) {
            if (!str_prefix(name, liq_table[liq].liq_name)) {
                return liq;
            }
        }

        return LIQ_WATER;
    }

    static int weapon_lookup(String name) {
        int type;

        for (type = 0; weapon_table[type].name != null; type++) {
            if (!str_prefix(name, weapon_table[type].name)) {
                return type;
            }
        }

        return -1;
    }

    static boolean cabal_ok(CHAR_DATA ch, Skill sn) {
        int i;

        if (IS_NPC(ch) || sn.cabal == CABAL_NONE ||
                cabal_table[ch.cabal].obj_ptr == null ||
                cabal_table[ch.cabal].obj_ptr.in_room == null ||
                cabal_table[ch.cabal].obj_ptr.in_room.vnum ==
                        cabal_table[ch.cabal].room_vnum) {
            return true;
        }

        for (i = 1; i < MAX_CABAL; i++) {
            if (cabal_table[ch.cabal].obj_ptr.in_room.vnum ==
                    cabal_table[i].room_vnum) {
                send_to_char("You cannot find the Cabal Power within you.\n", ch);
                return false;
            }
        }

        return true;
    }

    static int weapon_type(String name) {
        int type;

        for (type = 0; weapon_table[type].name != null; type++) {
            if (str_prefix(name, weapon_table[type].name)) {
                return weapon_table[type].type;
            }
        }

        return WEAPON_EXOTIC;
    }


    static int item_lookup(String name) {
        int type;
        for (type = 0; item_table[type].name != null; type++) {
            if (!str_prefix(name, item_table[type].name)) {
                return item_table[type].type;
            }
        }

        return -1;
    }

    static String item_name(int item_type) {
        int type;
        for (type = 0; item_table[type].name != null; type++) {
            if (item_type == item_table[type].type) {
                return item_table[type].name;
            }
        }
        return "none";
    }

    static String weapon_name(int weapon_type) {
        int type;
        for (type = 0; weapon_table[type].name != null; type++) {
            if (weapon_type == weapon_table[type].type) {
                return weapon_table[type].name;
            }
        }
        return "exotic";
    }


    /**
     * Check the material
     */
    static boolean check_material(OBJ_DATA obj, String material) {
        return obj.material.contains(material);

    }

    static boolean is_metal(OBJ_DATA obj) {

        return check_material(obj, "silver") ||
                check_material(obj, "gold") ||
                check_material(obj, "iron") ||
                check_material(obj, "mithril") ||
                check_material(obj, "adamantite") ||
                check_material(obj, "steel") ||
                check_material(obj, "lead") ||
                check_material(obj, "bronze") ||
                check_material(obj, "copper") ||
                check_material(obj, "brass") ||
                check_material(obj, "platinium") ||
                check_material(obj, "titanium") ||
                check_material(obj, "aliminum");

    }

    static boolean may_float(OBJ_DATA obj) {

        return check_material(obj, "wood") ||
                check_material(obj, "ebony") ||
                check_material(obj, "ice") ||
                check_material(obj, "energy") ||
                check_material(obj, "hardwood") ||
                check_material(obj, "softwood") ||
                check_material(obj, "flesh") ||
                check_material(obj, "silk") ||
                check_material(obj, "wool") ||
                check_material(obj, "cloth") ||
                check_material(obj, "fur") ||
                check_material(obj, "water") ||
                check_material(obj, "ice") ||
                check_material(obj, "oak") || obj.item_type == ITEM_BOAT;

    }


    static boolean cant_float(OBJ_DATA obj) {
        return check_material(obj, "steel") ||
                check_material(obj, "iron") ||
                check_material(obj, "brass") ||
                check_material(obj, "silver") ||
                check_material(obj, "gold") ||
                check_material(obj, "ivory") ||
                check_material(obj, "copper") ||
                check_material(obj, "diamond") ||
                check_material(obj, "pearl") ||
                check_material(obj, "gem") ||
                check_material(obj, "platinium") ||
                check_material(obj, "ruby") ||
                check_material(obj, "bronze") ||
                check_material(obj, "titanium") ||
                check_material(obj, "mithril") ||
                check_material(obj, "obsidian") ||
                check_material(obj, "lead");

    }

    static int floating_time(OBJ_DATA obj) {
        int ftime = 0;
        switch (obj.item_type) {
            default:
                break;
            case ITEM_KEY:
                ftime = 1;
                break;
            case ITEM_ARMOR:
                ftime = 2;
                break;
            case ITEM_TREASURE:
                ftime = 2;
                break;
            case ITEM_PILL:
                ftime = 2;
                break;
            case ITEM_POTION:
                ftime = 3;
                break;
            case ITEM_TRASH:
                ftime = 3;
                break;
            case ITEM_FOOD:
                ftime = 4;
                break;
            case ITEM_CONTAINER:
                ftime = 5;
                break;
            case ITEM_CORPSE_NPC:
                ftime = 10;
                break;
            case ITEM_CORPSE_PC:
                ftime = 10;
                break;
        }
        ftime = number_fuzzy(ftime);

        return (ftime < 0 ? 0 : ftime);
    }

    static int attack_lookup(String name) {
        for (int att = 0; att < attack_table.length; att++) {
            if (!str_prefix(name, attack_table[att].name)) {
                return att;
            }
        }

        return 0;
    }

/* returns a flag for wiznet */

    static int wiznet_lookup(String name) {
        int flag;

        for (flag = 0; wiznet_table[flag].name != null; flag++) {
            if (!str_prefix(name, wiznet_table[flag].name)) {
                return flag;
            }
        }

        return -1;
    }

    /**
     * for immunity, vulnerabiltiy, and resistant
     * the 'globals' (magic and weapons) may be overriden
     * three other cases -- wood, silver, and iron -- are checked in fight.c
     */
    static int check_immune(CHAR_DATA ch, int dam_type) {

        int immune = -1;
        int def = IS_NORMAL;

        if (dam_type == DAM_NONE) {
            return immune;
        }

        if (dam_type <= 3) {
            if (IS_SET(ch.imm_flags, IMM_WEAPON)) {
                def = IS_IMMUNE;
            } else if (IS_SET(ch.res_flags, RES_WEAPON)) {
                def = IS_RESISTANT;
            } else if (IS_SET(ch.vuln_flags, VULN_WEAPON)) {
                def = IS_VULNERABLE;
            }
        } else /* magical attack */ {
            if (IS_SET(ch.imm_flags, IMM_MAGIC)) {
                def = IS_IMMUNE;
            } else if (IS_SET(ch.res_flags, RES_MAGIC)) {
                def = IS_RESISTANT;
            } else if (IS_SET(ch.vuln_flags, VULN_MAGIC)) {
                def = IS_VULNERABLE;
            }
        }

        /* set bits to check -- VULN etc. must ALL be the same or this will fail */
        int bit;
        switch (dam_type) {
            case (DAM_BASH):
                bit = IMM_BASH;
                break;
            case (DAM_PIERCE):
                bit = IMM_PIERCE;
                break;
            case (DAM_SLASH):
                bit = IMM_SLASH;
                break;
            case (DAM_FIRE):
                bit = IMM_FIRE;
                break;
            case (DAM_COLD):
                bit = IMM_COLD;
                break;
            case (DAM_LIGHTNING):
                bit = IMM_LIGHTNING;
                break;
            case (DAM_ACID):
                bit = IMM_ACID;
                break;
            case (DAM_POISON):
                bit = IMM_POISON;
                break;
            case (DAM_NEGATIVE):
                bit = IMM_NEGATIVE;
                break;
            case (DAM_HOLY):
                bit = IMM_HOLY;
                break;
            case (DAM_ENERGY):
                bit = IMM_ENERGY;
                break;
            case (DAM_MENTAL):
                bit = IMM_MENTAL;
                break;
            case (DAM_DISEASE):
                bit = IMM_DISEASE;
                break;
            case (DAM_DROWNING):
                bit = IMM_DROWNING;
                break;
            case (DAM_LIGHT):
                bit = IMM_LIGHT;
                break;
            case (DAM_CHARM):
                bit = IMM_CHARM;
                break;
            case (DAM_SOUND):
                bit = IMM_SOUND;
                break;
            default:
                return def;
        }

        if (IS_SET(ch.imm_flags, bit)) {
            immune = IS_IMMUNE;
        } else if (IS_SET(ch.res_flags, bit)) {
            immune = IS_RESISTANT;
        }
        if (IS_SET(ch.vuln_flags, bit)) {
            if (immune == IS_IMMUNE) {
                immune = IS_RESISTANT;
            } else if (immune == IS_RESISTANT) {
                immune = IS_NORMAL;
            } else {
                immune = IS_VULNERABLE;
            }
        }

        if (!IS_NPC(ch) && get_curr_stat(ch, STAT_CHA) < 18 && dam_type == DAM_CHARM) {
            immune = IS_VULNERABLE;
        }

        return immune == -1 ? def : immune;
    }

    /**
     * checks mob format
     */

    static boolean is_old_mob(CHAR_DATA ch) {
        if (ch.pIndexData == null) {
            return false;
        } else if (ch.pIndexData.new_format) {
            return false;
        }
        return true;
    }

    /**
     * for returning skill information
     */

    static int get_skill(CHAR_DATA ch, Skill sn) {
        int skill;

        if (sn == null) /* shorthand for level based skills */ {
            skill = ch.level * 5 / 2;
        } else if (!IS_NPC(ch)) {
            if (ch.level < sn.skill_level[ch.clazz.id]) {
                skill = 0;
            } else {
                skill = ch.pcdata.learned[sn.ordinal()];
            }
        } else /* mobiles */ {

            if (sn.isSpell()) {
                skill = 40 + 2 * ch.level;
            } else if (sn == gsn_sneak || sn == gsn_hide) {
                skill = ch.level + 20;
            } else if ((sn == gsn_dodge && IS_SET(ch.off_flags, OFF_DODGE))
                    || (sn == gsn_parry && IS_SET(ch.off_flags, OFF_PARRY))) {
                skill = ch.level * 2;
            } else if (sn == gsn_shield_block) {
                skill = 10 + 2 * ch.level;
            } else if (sn == gsn_second_attack) {
                skill = 30 + ch.level;
            } else if (sn == gsn_third_attack
                    && (IS_SET(ch.act, ACT_WARRIOR) || IS_SET(ch.act, ACT_THIEF))) {
                skill = 30 + ch.level / 2;
            } else if (sn == gsn_fourth_attack && IS_SET(ch.act, ACT_WARRIOR)) {
                skill = 20 + ch.level / 2;
            } else if (sn == gsn_second_weapon && IS_SET(ch.act, ACT_WARRIOR)) {
                skill = 30 + ch.level / 2;
            } else if (sn == gsn_hand_to_hand) {
                skill = 40 + 2 * ch.level;
            } else if (sn == gsn_trip && IS_SET(ch.off_flags, OFF_TRIP)) {
                skill = 10 + 3 * ch.level;
            } else if (sn == gsn_bash && IS_SET(ch.off_flags, OFF_BASH)) {
                skill = 10 + 3 * ch.level;
            } else if (sn == gsn_disarm
                    && (IS_SET(ch.off_flags, OFF_DISARM)
                    || IS_SET(ch.act, ACT_WARRIOR)
                    || IS_SET(ch.act, ACT_THIEF))) {
                skill = 20 + 3 * ch.level;
            } else if (sn == gsn_grip
                    && (IS_SET(ch.act, ACT_WARRIOR)
                    || IS_SET(ch.act, ACT_THIEF))) {
                skill = ch.level;
            } else if (sn == gsn_berserk && IS_SET(ch.off_flags, OFF_BERSERK)) {
                skill = 3 * ch.level;
            } else if (sn == gsn_kick) {
                skill = 10 + 3 * ch.level;
            } else if (sn == gsn_backstab && IS_SET(ch.act, ACT_THIEF)) {
                skill = 20 + 2 * ch.level;
            } else if (sn == gsn_rescue) {
                skill = 40 + ch.level;
            } else if (sn == gsn_recall) {
                skill = 40 + ch.level;
            } else if (sn == gsn_sword
                    || sn == gsn_dagger
                    || sn == gsn_spear
                    || sn == gsn_mace
                    || sn == gsn_axe
                    || sn == gsn_flail
                    || sn == gsn_whip
                    || sn == gsn_polearm
                    || sn == gsn_bow
                    || sn == gsn_arrow
                    || sn == gsn_lance) {
                skill = 40 + 5 * ch.level / 2;
            } else {
                skill = 0;
            }
        }

        if (ch.daze > 0) {
            if (sn != null && sn.isSpell()) {
                skill /= 2;
            } else {
                skill = 2 * skill / 3;
            }
        }

        if (!IS_NPC(ch) && ch.pcdata.condition[COND_DRUNK] > 10) {
            skill = 9 * skill / 10;
        }

        if (ch.hit < (ch.max_hit * 0.6)) {
            skill = 9 * skill / 10;
        }

        skill = URANGE(0, skill, 100);

        if (skill != 0 && !IS_NPC(ch)) {
            if (sn != null && !sn.isSpell()) {
                skill += sn.mod[ch.clazz.id];
            }
        }
        return skill;

    }

/* for returning weapon information */

    static Skill get_weapon_sn(CHAR_DATA ch, boolean second) {
        Skill sn;

        OBJ_DATA wield = get_wield_char(ch, second);

        if (wield == null || wield.item_type != ITEM_WEAPON) {
            sn = gsn_hand_to_hand;
        } else {
            switch (wield.value[0]) {
                default:
                    sn = null;
                    break;
                case WEAPON_SWORD:
                    sn = gsn_sword;
                    break;
                case WEAPON_DAGGER:
                    sn = gsn_dagger;
                    break;
                case WEAPON_SPEAR:
                    sn = gsn_spear;
                    break;
                case WEAPON_MACE:
                    sn = gsn_mace;
                    break;
                case WEAPON_AXE:
                    sn = gsn_axe;
                    break;
                case WEAPON_FLAIL:
                    sn = gsn_flail;
                    break;
                case WEAPON_WHIP:
                    sn = gsn_whip;
                    break;
                case WEAPON_POLEARM:
                    sn = gsn_polearm;
                    break;
                case WEAPON_BOW:
                    sn = gsn_bow;
                    break;
                case WEAPON_ARROW:
                    sn = gsn_arrow;
                    break;
                case WEAPON_LANCE:
                    sn = gsn_lance;
                    break;
            }
        }
        return sn;
    }

    static int get_weapon_skill(CHAR_DATA ch, Skill sn) {
        int skill;

        /* -1 is exotic */
        if (IS_NPC(ch)) {
            if (sn == null) {
                skill = 3 * ch.level;
            } else if (sn == gsn_hand_to_hand) {
                skill = 40 + 2 * ch.level;
            } else {
                skill = 40 + 5 * ch.level / 2;
            }
        } else {
            if (sn == null) {
                skill = UMIN(3 * ch.level, 100);
            } else {
                skill = ch.pcdata.learned[sn.ordinal()];
            }
        }

        if (ch.hit < (ch.max_hit * 0.6)) {
            skill = 9 * skill / 10;
        }

        return URANGE(0, skill, 100);
    }

/* used to de-screw characters */

    static void reset_char(CHAR_DATA ch) {
        int loc;
        OBJ_DATA obj, obj_next;
        AFFECT_DATA af;

        if (IS_NPC(ch) || ch.in_room == null) {
            return;
        }

        if (ch.pcdata.true_sex < 0 || ch.pcdata.true_sex > 2) {
            ch.pcdata.true_sex = 0;
        }

        ch.sex = ch.pcdata.true_sex;
        ch.max_hit = ch.pcdata.perm_hit;
        ch.max_mana = ch.pcdata.perm_mana;
        ch.max_move = ch.pcdata.perm_move;
/*
    ch.hit     = ch.max_hit;
    ch.mana        = ch.max_mana;
    ch.move        = ch.max_move;
*/

/* a little hack */

        ch.extracted = true;
        /* now add back spell effects */
        for (af = ch.affected; af != null; af = af.next) {
            affect_modify(ch, af, true);
        }

        /* now start adding back the effects */
        for (obj = ch.carrying; obj != null; obj = obj_next) {
            obj_next = obj.next_content;
            if ((loc = obj.wear_loc) != WEAR_NONE) {
                obj.wear_loc = WEAR_NONE;
                equip_char(ch, obj, loc);
            }
        }

        ch.extracted = false;
        /* make sure sex is RIGHT! */
        if (ch.sex < 0 || ch.sex > 2) {
            ch.sex = ch.pcdata.true_sex;
        }

    }

/*
* Retrieve a character's trusted level for permission checking.
*/

    static int get_trust(CHAR_DATA ch) {
        if (ch.desc != null && ch.desc.original != null) {
            ch = ch.desc.original;
        }

        if (ch.trust != 0 && IS_SET(ch.comm, COMM_true_TRUST)) {
            return ch.trust;
        }

        if (IS_NPC(ch) && ch.level >= LEVEL_HERO) {
            return LEVEL_HERO - 1;
        } else {
            return ch.level;
        }
    }

/*
* Retrieve a character's age.
*/

    static int get_age(CHAR_DATA ch) {
        return 17 + (ch.played + (int) (current_time - ch.logon)) / 72000;
    }

    static int age_to_num(int age) {
        return age * 72000;
    }

/* command for retrieving stats */

    static int get_curr_stat(CHAR_DATA ch, int stat) {
        int max;

        if (IS_NPC(ch) || ch.level > LEVEL_IMMORTAL) {
            max = 25;
        } else {
            max = get_max_train(ch, stat);
            max = UMIN(max, 25);
        }

        return URANGE(3, ch.perm_stat[stat] + ch.mod_stat[stat], max);
    }

/* command for returning max training score */

    static int get_max_train(CHAR_DATA ch, int stat) {
        int max;

        if (IS_NPC(ch) || ch.level > LEVEL_IMMORTAL) {
            return 25;
        }

        max = (20 + ORG_RACE(ch).pcRace.stats[stat] + /* ORG_RACE && RACE serdar*/
                ch.clazz.stats[stat]);

        return UMIN(max, 25);
    }

/*
 * command for returning max training score
 * for do_train and stat2train in comm.c
 */

    static int get_max_train2(CHAR_DATA ch, int stat) {
        int max;

        if (IS_NPC(ch) || ch.level > LEVEL_IMMORTAL) {
            return 25;
        }

        max = (20 + ORG_RACE(ch).pcRace.stats[stat] +
                ch.clazz.stats[stat]);

        return UMIN(max, 25);
    }

/*
* Retrieve a character's carry capacity.
*/

    static int can_carry_n(CHAR_DATA ch) {
        if (!IS_NPC(ch) && ch.level >= LEVEL_IMMORTAL) {
            return 1000;
        }

        if (IS_NPC(ch) && IS_SET(ch.act, ACT_PET)) {
            return 0;
        }

        return MAX_WEAR + get_curr_stat(ch, STAT_DEX) - 10 + ch.size;
    }

/*
* Retrieve a character's carry capacity.
*/

    static int can_carry_w(CHAR_DATA ch) {
        if (!IS_NPC(ch) && ch.level >= LEVEL_IMMORTAL) {
            return 10000000;
        }

        if (IS_NPC(ch) && IS_SET(ch.act, ACT_PET)) {
            return 0;
        }

        return str_app[get_curr_stat(ch, STAT_STR)].carry * 10 + ch.level * 25;
    }

    /**
     * See if a string is one of the names of an object.
     */
    static boolean is_name(String str, String nameList) {
        String wholeString = str;
        StringBuilder part = new StringBuilder();
        /* we need ALL parts of wholeString to match part of namelist */
        for (; ; )  /* start parsing wholeString */ {
            part.setLength(0);
            str = one_argument(str, part);

            if (part.length() == 0) {
                return false;
            }

            /* check to see if this is part of namelist */
            String subStr = part.toString();

            String list = nameList;
            for (; ; )  /* start parsing namelist */ {
                part.setLength(0);
                list = one_argument(list, part);
                if (part.length() == 0)  /* this name was not found */ {
                    return false;
                }

                String nameStr = part.toString();
                if (!str_prefix(wholeString, nameStr)) {
                    return true; /* full pattern match */
                }

                if (!str_prefix(subStr, nameStr)) {
                    break;
                }
            }
        }
    }

/* enchanted stuff for eq */

    static void affect_enchant(OBJ_DATA obj) {
        /* okay, move all the old flags into new vectors if we have to */
        if (!obj.enchanted) {
            AFFECT_DATA paf, af_new;
            obj.enchanted = true;

            for (paf = obj.pIndexData.affected;
                 paf != null; paf = paf.next) {
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
    }

    private static int depth;
/*
 * Apply or remove an affect to a character.
 */

    static void affect_modify(CHAR_DATA ch, AFFECT_DATA paf, boolean fAdd) {
        OBJ_DATA hold;
        int mod, i;

        mod = paf.modifier;

        if (fAdd) {
            switch (paf.where) {
                case TO_AFFECTS:
                    ch.affected_by = SET_BIT(ch.affected_by, paf.bitvector);
                    if (IS_SET(paf.bitvector, AFF_FLYING) && !IS_NPC(ch)) {
                        ch.act = REMOVE_BIT(ch.act, PLR_CHANGED_AFF);
                    }
                    break;
                case TO_IMMUNE:
                    ch.imm_flags = SET_BIT(ch.imm_flags, paf.bitvector);
                    break;
                case TO_RESIST:
                    ch.res_flags = SET_BIT(ch.res_flags, paf.bitvector);
                    break;
                case TO_ACT_FLAG:
                    ch.act = SET_BIT(ch.act, paf.bitvector);
                    break;
                case TO_VULN:
                    ch.vuln_flags = SET_BIT(ch.vuln_flags, paf.bitvector);
                    break;
                case TO_RACE:
                    ch.race = (Race) paf.objModifier;
                    ch.affected_by = REMOVE_BIT(ch.affected_by, ORG_RACE(ch).aff);
                    ch.affected_by = SET_BIT(ch.affected_by, ch.race.aff);
                    ch.imm_flags = REMOVE_BIT(ch.imm_flags, ORG_RACE(ch).imm);
                    ch.imm_flags = SET_BIT(ch.imm_flags, ch.race.imm);
                    ch.res_flags = REMOVE_BIT(ch.res_flags, ORG_RACE(ch).res);
                    ch.res_flags = SET_BIT(ch.res_flags, ch.race.res);
                    ch.vuln_flags = REMOVE_BIT(ch.vuln_flags, ORG_RACE(ch).vuln);
                    ch.vuln_flags = SET_BIT(ch.vuln_flags, ch.race.vuln);
                    ch.form = ch.race.form;
                    ch.parts = ch.race.parts;
                    break;
            }
        } else {
            switch (paf.where) {
                case TO_AFFECTS:
                    ch.affected_by = REMOVE_BIT(ch.affected_by, paf.bitvector);
                    break;
                case TO_IMMUNE:
                    ch.imm_flags = REMOVE_BIT(ch.imm_flags, paf.bitvector);
                    break;
                case TO_RESIST:
                    ch.res_flags = REMOVE_BIT(ch.res_flags, paf.bitvector);
                    break;
                case TO_ACT_FLAG:
                    ch.act = REMOVE_BIT(ch.act, paf.bitvector);
                    break;
                case TO_VULN:
                    ch.vuln_flags = REMOVE_BIT(ch.vuln_flags, paf.bitvector);
                    break;
                case TO_RACE:
                    ch.affected_by = REMOVE_BIT(ch.affected_by, ch.race.aff);
                    ch.affected_by = SET_BIT(ch.affected_by, ORG_RACE(ch).aff);
                    ch.imm_flags = REMOVE_BIT(ch.imm_flags, ch.race.imm);
                    ch.imm_flags = SET_BIT(ch.imm_flags, ORG_RACE(ch).imm);
                    ch.res_flags = REMOVE_BIT(ch.res_flags, ch.race.res);
                    ch.res_flags = SET_BIT(ch.res_flags, ORG_RACE(ch).res);
                    ch.vuln_flags = REMOVE_BIT(ch.vuln_flags, ch.race.vuln);
                    ch.vuln_flags = SET_BIT(ch.vuln_flags, ORG_RACE(ch).vuln);
                    ch.form = ORG_RACE(ch).form;
                    ch.parts = ORG_RACE(ch).parts;
                    ch.race = ORG_RACE(ch);
                    break;
            }
            mod = 0 - mod;
        }

        switch (paf.location) {
            default:
                bug("Affect_modify: unknown location %d.", paf.location);
                return;

            case APPLY_NONE:
                break;
            case APPLY_STR:
                ch.mod_stat[STAT_STR] += mod;
                break;
            case APPLY_DEX:
                ch.mod_stat[STAT_DEX] += mod;
                break;
            case APPLY_INT:
                ch.mod_stat[STAT_INT] += mod;
                break;
            case APPLY_WIS:
                ch.mod_stat[STAT_WIS] += mod;
                break;
            case APPLY_CON:
                ch.mod_stat[STAT_CON] += mod;
                break;
            case APPLY_CHA:
                ch.mod_stat[STAT_CHA] += mod;
                break;
            case APPLY_CLASS:
                break;
            case APPLY_LEVEL:
                break;
            case APPLY_AGE:
                ch.played += age_to_num(mod);
                break;
            case APPLY_HEIGHT:
                break;
            case APPLY_WEIGHT:
                break;
            case APPLY_MANA:
                ch.max_mana += mod;
                break;
            case APPLY_HIT:
                ch.max_hit += mod;
                break;
            case APPLY_MOVE:
                ch.max_move += mod;
                break;
            case APPLY_GOLD:
                break;
            case APPLY_EXP:
                break;
            case APPLY_AC:
                for (i = 0; i < 4; i++) {
                    ch.armor[i] += mod;
                }
                break;
            case APPLY_HITROLL:
                ch.hitroll += mod;
                break;
            case APPLY_DAMROLL:
                ch.damroll += mod;
                break;
            case APPLY_SIZE:
                ch.size += mod;
                break;
            case APPLY_SAVES:
                ch.saving_throw += mod;
                break;
            case APPLY_SAVING_ROD:
                ch.saving_throw += mod;
                break;
            case APPLY_SAVING_PETRI:
                ch.saving_throw += mod;
                break;
            case APPLY_SAVING_BREATH:
                ch.saving_throw += mod;
                break;
            case APPLY_SAVING_SPELL:
                ch.saving_throw += mod;
                break;
            case APPLY_SPELL_AFFECT:
                break;
        }

        /*
         * Check for weapon wielding.
         * Guard against recursion (for weapons with affects).
         */
        if (!IS_NPC(ch) && !ch.extracted) {


            if ((hold = get_eq_char(ch, WEAR_BOTH)) != null
                    && get_obj_weight(hold) > str_app[get_curr_stat(ch, STAT_STR)].carry) {
                if (depth == 0) {
                    depth++;
                    act("You drop $p.", ch, hold, null, TO_CHAR);
                    act("$n drops $p.", ch, hold, null, TO_ROOM);
                    obj_from_char(hold);
                    obj_to_room(hold, ch.in_room);
                    depth--;
                }
            }

            if ((hold = get_eq_char(ch, WEAR_RIGHT)) != null
                    && get_obj_weight(hold) > str_app[get_curr_stat(ch, STAT_STR)].carry) {
                if (depth == 0) {
                    depth++;
                    act("You drop $p.", ch, hold, null, TO_CHAR);
                    act("$n drops $p.", ch, hold, null, TO_ROOM);
                    obj_from_char(hold);
                    obj_to_room(hold, ch.in_room);
                    depth--;
                }
            }

            if ((hold = get_eq_char(ch, WEAR_LEFT)) != null
                    && get_obj_weight(hold) > str_app[get_curr_stat(ch, STAT_STR)].carry) {
                if (depth == 0) {
                    depth++;
                    act("You drop $p.", ch, hold, null, TO_CHAR);
                    act("$n drops $p.", ch, hold, null, TO_ROOM);
                    obj_from_char(hold);
                    obj_to_room(hold, ch.in_room);
                    depth--;
                }
            }
        }

    }

/* find an effect in an affect list */

    static AFFECT_DATA affect_find(AFFECT_DATA paf, Skill sn) {
        AFFECT_DATA paf_find;

        for (paf_find = paf; paf_find != null; paf_find = paf_find.next) {
            if (paf_find.type == sn) {
                return paf_find;
            }
        }

        return null;
    }

/* fix object affects when removing one */

    static void affect_check(CHAR_DATA ch, int where, long vector) {
        AFFECT_DATA paf;
        OBJ_DATA obj;

        if (where == TO_OBJECT || where == TO_WEAPON || vector == 0) {
            return;
        }

        for (paf = ch.affected; paf != null; paf = paf.next) {
            if (paf.where == where && paf.bitvector == vector) {
                switch (where) {
                    case TO_AFFECTS:
                        ch.affected_by = SET_BIT(ch.affected_by, vector);
                        break;
                    case TO_IMMUNE:
                        ch.imm_flags = SET_BIT(ch.imm_flags, vector);
                        break;
                    case TO_RESIST:
                        ch.res_flags = SET_BIT(ch.res_flags, vector);
                        break;
                    case TO_ACT_FLAG:
                        ch.act = SET_BIT(ch.act, paf.bitvector);
                        break;
                    case TO_VULN:
                        ch.vuln_flags = SET_BIT(ch.vuln_flags, vector);
                        break;
                    case TO_RACE:
                        if (ch.race == ORG_RACE(ch)) {
                            ch.race = (Race) paf.objModifier;
                            ch.affected_by = REMOVE_BIT(ch.affected_by, ORG_RACE(ch).aff);
                            ch.affected_by = SET_BIT(ch.affected_by, ch.race.aff);
                            ch.imm_flags = REMOVE_BIT(ch.imm_flags, ORG_RACE(ch).imm);
                            ch.imm_flags = SET_BIT(ch.imm_flags, ch.race.imm);
                            ch.res_flags = REMOVE_BIT(ch.res_flags, ORG_RACE(ch).res);
                            ch.res_flags = SET_BIT(ch.res_flags, ch.race.res);
                            ch.vuln_flags = REMOVE_BIT(ch.vuln_flags, ORG_RACE(ch).vuln);
                            ch.vuln_flags = SET_BIT(ch.vuln_flags, ch.race.vuln);
                            ch.form = ch.race.form;
                            ch.parts = ch.race.parts;
                        }
                        break;
                }
                return;
            }
        }

        for (obj = ch.carrying; obj != null; obj = obj.next_content) {
            if (obj.wear_loc == -1 || obj.wear_loc == WEAR_STUCK_IN) {
                continue;
            }

            for (paf = obj.affected; paf != null; paf = paf.next) {
                if (paf.where == where && paf.bitvector == vector) {
                    switch (where) {
                        case TO_AFFECTS:
                            ch.affected_by = SET_BIT(ch.affected_by, vector);
                            break;
                        case TO_IMMUNE:
                            ch.imm_flags = SET_BIT(ch.imm_flags, vector);
                            break;
                        case TO_ACT_FLAG:
                            ch.act = SET_BIT(ch.act, paf.bitvector);
                            break;
                        case TO_RESIST:
                            ch.res_flags = SET_BIT(ch.res_flags, vector);
                            break;
                        case TO_VULN:
                            ch.vuln_flags = SET_BIT(ch.vuln_flags, vector);
                            break;
                        case TO_RACE:
                            if (ch.race == ORG_RACE(ch)) {
                                ch.race = (Race) paf.objModifier;
                                ch.affected_by = REMOVE_BIT(ch.affected_by, ORG_RACE(ch).aff);
                                ch.affected_by = SET_BIT(ch.affected_by, ch.race.aff);
                                ch.imm_flags = REMOVE_BIT(ch.imm_flags, ORG_RACE(ch).imm);
                                ch.imm_flags = SET_BIT(ch.imm_flags, ch.race.imm);
                                ch.res_flags = REMOVE_BIT(ch.res_flags, ORG_RACE(ch).res);
                                ch.res_flags = SET_BIT(ch.res_flags, ch.race.res);
                                ch.vuln_flags = REMOVE_BIT(ch.vuln_flags, ORG_RACE(ch).vuln);
                                ch.vuln_flags = SET_BIT(ch.vuln_flags, ch.race.vuln);
                                ch.form = ch.race.form;
                                ch.parts = ch.race.parts;
                            }
                            break;
                    }
                    return;
                }
            }

            if (obj.enchanted) {
                continue;
            }

            for (paf = obj.pIndexData.affected; paf != null; paf = paf.next) {
                if (paf.where == where && paf.bitvector == vector) {
                    switch (where) {
                        case TO_AFFECTS:
                            ch.affected_by = SET_BIT(ch.affected_by, vector);
                            break;
                        case TO_IMMUNE:
                            ch.imm_flags = SET_BIT(ch.imm_flags, vector);
                            break;
                        case TO_ACT_FLAG:
                            ch.act = SET_BIT(ch.act, paf.bitvector);
                            break;
                        case TO_RESIST:
                            ch.res_flags = SET_BIT(ch.res_flags, vector);
                            break;
                        case TO_VULN:
                            ch.vuln_flags = SET_BIT(ch.vuln_flags, vector);
                            break;
                        case TO_RACE:
                            if (ch.race == ORG_RACE(ch)) {
                                ch.race = (Race) paf.objModifier;
                                ch.affected_by = REMOVE_BIT(ch.affected_by, ORG_RACE(ch).aff);
                                ch.affected_by = SET_BIT(ch.affected_by, ch.race.aff);
                                ch.imm_flags = REMOVE_BIT(ch.imm_flags, ORG_RACE(ch).imm);
                                ch.imm_flags = SET_BIT(ch.imm_flags, ch.race.imm);
                                ch.res_flags = REMOVE_BIT(ch.res_flags, ORG_RACE(ch).res);
                                ch.res_flags = SET_BIT(ch.res_flags, ch.race.res);
                                ch.vuln_flags = REMOVE_BIT(ch.vuln_flags, ORG_RACE(ch).vuln);
                                ch.vuln_flags = SET_BIT(ch.vuln_flags, ch.race.vuln);
                                ch.form = ch.race.form;
                                ch.parts = ch.race.parts;
                            }
                            break;
                    }
                    return;
                }
            }
        }
    }

/*
 * Give an affect to a char.
 */

    //TODO: avoid copying?

    static void affect_to_char(CHAR_DATA ch, AFFECT_DATA paf) {
        AFFECT_DATA paf_new = new AFFECT_DATA();

        paf_new.assignValuesFrom(paf);
        paf_new.next = ch.affected;
        ch.affected = paf_new;

        affect_modify(ch, paf_new, true);
    }

/* give an affect to an object */

    static void affect_to_obj(OBJ_DATA obj, AFFECT_DATA paf) {
        AFFECT_DATA paf_new = new AFFECT_DATA();

        paf_new.assignValuesFrom(paf);
        paf_new.next = obj.affected;
        obj.affected = paf_new;

        /* apply any affect vectors to the object's extra_flags */
        if (paf.bitvector != 0) {
            switch (paf.where) {
                case TO_OBJECT:
                    obj.extra_flags = SET_BIT(obj.extra_flags, paf.bitvector);
                    break;
                case TO_WEAPON:
                    if (obj.item_type == ITEM_WEAPON) {
                        obj.value[4] = (int) SET_BIT(obj.value[4], paf.bitvector);
                    }
                    break;
            }
        }
    }

/*
* Remove an affect from a char.
*/

    static void affect_remove(CHAR_DATA ch, AFFECT_DATA paf) {
        if (ch.affected == null) {
            bug("Affect_remove: no affect.");
            return;
        }

        affect_modify(ch, paf, false);
        int where = paf.where;
        long vector = paf.bitvector;

        if (paf == ch.affected) {
            ch.affected = paf.next;
        } else {
            AFFECT_DATA prev;

            for (prev = ch.affected; prev != null; prev = prev.next) {
                if (prev.next == paf) {
                    prev.next = paf.next;
                    break;
                }
            }

            if (prev == null) {
                bug("Affect_remove: cannot find paf.");
                return;
            }
        }

        affect_check(ch, where, vector);
    }

    static void affect_remove_obj(OBJ_DATA obj, AFFECT_DATA paf) {
        if (obj.affected == null) {
            bug("Affect_remove_object: no affect.");
            return;
        }

        if (obj.carried_by != null && obj.wear_loc != -1) {
            affect_modify(obj.carried_by, paf, false);
        }

        int where = paf.where;
        long vector = paf.bitvector;

        /* remove flags from the object if needed */
        if (paf.bitvector != 0) {
            switch (paf.where) {
                case TO_OBJECT:
                    obj.extra_flags = REMOVE_BIT(obj.extra_flags, paf.bitvector);
                    break;
                case TO_WEAPON:
                    if (obj.item_type == ITEM_WEAPON) {
                        obj.value[4] = (int) REMOVE_BIT(obj.value[4], paf.bitvector);
                    }
                    break;
            }
        }

        if (paf == obj.affected) {
            obj.affected = paf.next;
        } else {
            AFFECT_DATA prev;

            for (prev = obj.affected; prev != null; prev = prev.next) {
                if (prev.next == paf) {
                    prev.next = paf.next;
                    break;
                }
            }

            if (prev == null) {
                bug("Affect_remove_object: cannot find paf.");
                return;
            }
        }

        if (obj.carried_by != null && obj.wear_loc != -1) {
            affect_check(obj.carried_by, where, vector);
        }
    }

/*
* Strip all affects of a given sn.
*/

    static void affect_strip(CHAR_DATA ch, Skill sn) {
        AFFECT_DATA paf;
        AFFECT_DATA paf_next;

        for (paf = ch.affected; paf != null; paf = paf_next) {
            paf_next = paf.next;
            if (paf.type == sn) {
                affect_remove(ch, paf);
            }
        }
    }

/*
* Return true if a char is affected by a spell.
*/

    static boolean is_affected(CHAR_DATA ch, Skill sn) {
        AFFECT_DATA paf;

        for (paf = ch.affected; paf != null; paf = paf.next) {
            if (paf.type == sn) {
                return true;
            }
        }

        return false;
    }

/*
* Add or enhance an affect.
*/

    static void affect_join(CHAR_DATA ch, AFFECT_DATA paf) {
        AFFECT_DATA paf_old;
        for (paf_old = ch.affected; paf_old != null; paf_old = paf_old.next) {
            if (paf_old.type == paf.type) {
                paf.level = ((paf.level += paf_old.level) / 2);
                paf.duration += paf_old.duration;
                paf.modifier += paf_old.modifier;
                affect_remove(ch, paf_old);
                break;
            }
        }

        affect_to_char(ch, paf);
    }

/*
* Move a char out of a room.
*/

    static void char_from_room(CHAR_DATA ch) {
        ROOM_INDEX_DATA prev_room = ch.in_room;

        if (ch.in_room == null) {
            bug("Char_from_room: null.");
            return;
        }

        if (!IS_NPC(ch)) {
            --ch.in_room.area.nplayer;
        }

        OBJ_DATA obj = get_light_char(ch);
        if (obj != null
/*
    &&   obj.item_type == ITEM_LIGHT
    &&   obj.value[2] != 0
*/
                && ch.in_room.light > 0) {
            --ch.in_room.light;
        }

        if (ch == ch.in_room.people) {
            ch.in_room.people = ch.next_in_room;
        } else {
            CHAR_DATA prev;

            for (prev = ch.in_room.people; prev != null; prev = prev.next_in_room) {
                if (prev.next_in_room == ch) {
                    prev.next_in_room = ch.next_in_room;
                    break;
                }
            }

            if (prev == null) {
                bug("Char_from_room: ch not found.");
            }
        }

        ch.in_room = null;
        ch.next_in_room = null;
        ch.on = null;  /* sanity check! */

        if (MOUNTED(ch) != null) {
            ch.mount.riding = false;
            ch.riding = false;
        }

        if (RIDDEN(ch) != null) {
            ch.mount.riding = false;
            ch.riding = false;
        }

        if (prev_room != null && prev_room.affected_by != 0) {
            raffect_back_char(prev_room, ch);
        }

    }

/*
* Move a char into a room.
*/

    static void char_to_room(CHAR_DATA ch, ROOM_INDEX_DATA pRoomIndex) {


        if (pRoomIndex == null) {
            ROOM_INDEX_DATA room;

            bug("Char_to_room: null.");

            if ((room = get_room_index(ROOM_VNUM_TEMPLE)) != null) {
                char_to_room(ch, room);
            }

            return;
        }

        ch.in_room = pRoomIndex;
        ch.next_in_room = pRoomIndex.people;
        pRoomIndex.people = ch;

        if (!IS_NPC(ch)) {
            if (ch.in_room.area.empty) {
                ch.in_room.area.empty = false;
                ch.in_room.area.age = 0;
            }
            ++ch.in_room.area.nplayer;
        }
        OBJ_DATA obj = get_light_char(ch);
        if (obj != null) // &&   obj.item_type == ITEM_LIGHT &&   obj.value[2] != 0 )
        {
            ++ch.in_room.light;
        }

        if (IS_AFFECTED(ch, AFF_PLAGUE)) {
            AFFECT_DATA af;
            CHAR_DATA vch;

            for (af = ch.affected; af != null; af = af.next) {
                if (af.type == gsn_plague) {
                    break;
                }
            }

            if (af == null) {
                ch.affected_by = REMOVE_BIT(ch.affected_by, AFF_PLAGUE);
            } else {
                if (af.level != 1) {
                    AFFECT_DATA plague = new AFFECT_DATA();
                    plague.where = TO_AFFECTS;
                    plague.type = gsn_plague;
                    plague.level = (af.level - 1);
                    plague.duration = number_range(1, 2 * plague.level);
                    plague.location = APPLY_STR;
                    plague.modifier = -5;
                    plague.bitvector = AFF_PLAGUE;

                    for (vch = ch.in_room.people; vch != null; vch = vch.next_in_room) {
                        if (!saves_spell(plague.level - 2, vch, DAM_DISEASE)
                                && !IS_IMMORTAL(vch) &&
                                !IS_AFFECTED(vch, AFF_PLAGUE) && number_bits(6) == 0) {
                            send_to_char("You feel hot and feverish.\n", vch);
                            act("$n shivers and looks very ill.", vch, null, null, TO_ROOM);
                            affect_join(vch, plague);
                        }
                    }
                }
            }
        }

        if (ch.in_room.affected_by != 0) {
            if (IS_IMMORTAL(ch)) {
                do_raffects(ch);
            } else {
                raffect_to_char(ch.in_room, ch);
            }
        }

    }

/*
* Give an obj to a char.
*/

    static void obj_to_char(OBJ_DATA obj, CHAR_DATA ch) {
        obj.next_content = ch.carrying;
        ch.carrying = obj;
        obj.carried_by = ch;
        obj.in_room = null;
        obj.in_obj = null;
        ch.carry_number += get_obj_number(obj);
        ch.carry_weight += get_obj_weight(obj);
    }

/*
* Take an obj from its character.
*/

    static void obj_from_char(OBJ_DATA obj) {
        CHAR_DATA ch;

        if ((ch = obj.carried_by) == null) {
            bug("Obj_from_char: null ch.");
            return;
        }

        if (obj.wear_loc != WEAR_NONE) {
            unequip_char(ch, obj);
        }

        if (ch.carrying == obj) {
            ch.carrying = obj.next_content;
        } else {
            OBJ_DATA prev;

            for (prev = ch.carrying; prev != null; prev = prev.next_content) {
                if (prev.next_content == obj) {
                    prev.next_content = obj.next_content;
                    break;
                }
            }

            if (prev == null) {
                bug("Obj_from_char: obj not in list.");
            }
        }

        obj.carried_by = null;
        obj.next_content = null;
        ch.carry_number -= get_obj_number(obj);
        ch.carry_weight -= get_obj_weight(obj);

    }

/*
* Find the ac value of an obj, including position effect.
*/

    static int apply_ac(OBJ_DATA obj, int iWear, int type) {
        if (obj.item_type != ITEM_ARMOR) {
            return 0;
        }

        switch (iWear) {
            case WEAR_BODY:
                return 3 * obj.value[type];
            case WEAR_HEAD:
                return 2 * obj.value[type];
            case WEAR_LEGS:
                return 2 * obj.value[type];
            case WEAR_FEET:
                return obj.value[type];
            case WEAR_HANDS:
                return obj.value[type];
            case WEAR_ARMS:
                return obj.value[type];
            case WEAR_FINGER:
                return obj.value[type];
            case WEAR_NECK:
                return obj.value[type];
            case WEAR_ABOUT:
                return 2 * obj.value[type];
            case WEAR_WAIST:
                return obj.value[type];
            case WEAR_WRIST:
                return obj.value[type];
            case WEAR_LEFT:
                return obj.value[type];
            case WEAR_RIGHT:
                return obj.value[type];
            case WEAR_BOTH:
                return obj.value[type];
        }

        return 0;
    }

/*
* Find a piece of eq on a character.
*/

    static OBJ_DATA get_eq_char(CHAR_DATA ch, int iWear) {
        OBJ_DATA obj;

        if (ch == null) {
            return null;
        }

        for (obj = ch.carrying; obj != null; obj = obj.next_content) {
            if (obj.wear_loc == iWear) {
                return obj;
            }
        }

        return null;
    }

/*
* Equip a char with an obj.
*/

    static void equip_char(CHAR_DATA ch, OBJ_DATA obj, int iWear) {
        AFFECT_DATA paf;
        int i;

        if (iWear == WEAR_STUCK_IN) {
            obj.wear_loc = iWear;
            return;
        }

        if (count_worn(ch, iWear) >= max_can_wear(ch, iWear)) {
            bug("Equip_char: already equipped (%d).", iWear);
            return;
        }

        if ((IS_OBJ_STAT(obj, ITEM_ANTI_EVIL) && IS_EVIL(ch))
                || (IS_OBJ_STAT(obj, ITEM_ANTI_GOOD) && IS_GOOD(ch))
                || (IS_OBJ_STAT(obj, ITEM_ANTI_NEUTRAL) && IS_NEUTRAL(ch))) {
            /*
            * Thanks to Morgenes for the bug fix here!
            */
            act("You are zapped by $p and drop it.", ch, obj, null, TO_CHAR);
            act("$n is zapped by $p and drops it.", ch, obj, null, TO_ROOM);
            obj_from_char(obj);
            obj_to_room(obj, ch.in_room);
            return;
        }


        for (i = 0; i < 4; i++) {
            ch.armor[i] -= apply_ac(obj, iWear, i);
        }

        if (get_light_char(ch) == null && ch.in_room != null
                && ((obj.item_type == ITEM_LIGHT && obj.value[2] != 0)
                || (iWear == WEAR_HEAD && IS_OBJ_STAT(obj, ITEM_GLOW)))) {
            ++ch.in_room.light;
        }

        obj.wear_loc = iWear;

        if (!obj.enchanted) {
            for (paf = obj.pIndexData.affected; paf != null; paf = paf.next) {
                if (paf.location != APPLY_SPELL_AFFECT) {
                    affect_modify(ch, paf, true);
                }
            }
        }
        for (paf = obj.affected; paf != null; paf = paf.next) {
            if (paf.location == APPLY_SPELL_AFFECT) {
                affect_to_char(ch, paf);
            } else {
                affect_modify(ch, paf, true);
            }
        }

        if (IS_SET(obj.progtypes, OPROG_WEAR)) {
            obj.pIndexData.oprogs.wear_prog.run(obj, ch);
        }

    }

/*
* Unequip a char with an obj.
*/

    static void unequip_char(CHAR_DATA ch, OBJ_DATA obj) {
        AFFECT_DATA paf;
        AFFECT_DATA lpaf;
        AFFECT_DATA lpaf_next;
        int i, old_wear;

        if (obj.wear_loc == WEAR_NONE) {
            bug("Unequip_char: already unequipped.");
            return;
        }

        if (obj.wear_loc == WEAR_STUCK_IN) {
            obj.wear_loc = WEAR_NONE;
            return;
        }

        for (i = 0; i < 4; i++) {
            ch.armor[i] += apply_ac(obj, obj.wear_loc, i);
        }
        old_wear = obj.wear_loc;
        obj.wear_loc = -1;

        if (!obj.enchanted) {
            for (paf = obj.pIndexData.affected; paf != null; paf = paf.next) {
                if (paf.location == APPLY_SPELL_AFFECT) {
                    for (lpaf = ch.affected; lpaf != null; lpaf = lpaf_next) {
                        lpaf_next = lpaf.next;
                        if ((lpaf.type == paf.type) &&
                                (lpaf.level == paf.level) &&
                                (lpaf.location == APPLY_SPELL_AFFECT)) {
                            affect_remove(ch, lpaf);
                            lpaf_next = null;
                        }
                    }
                } else {
                    affect_modify(ch, paf, false);
                    affect_check(ch, paf.where, paf.bitvector);
                }
            }
        }

        for (paf = obj.affected; paf != null; paf = paf.next) {
            if (paf.location == APPLY_SPELL_AFFECT) {
                bug("Norm-Apply: %d", 0);
                for (lpaf = ch.affected; lpaf != null; lpaf = lpaf_next) {
                    lpaf_next = lpaf.next;
                    if ((lpaf.type == paf.type) &&
                            (lpaf.level == paf.level) &&
                            (lpaf.location == APPLY_SPELL_AFFECT)) {
                        bug("location = %d", lpaf.location);
                        bug("type = " + lpaf.type);
                        affect_remove(ch, lpaf);
                        lpaf_next = null;
                    }
                }
            } else {
                affect_modify(ch, paf, false);
                affect_check(ch, paf.where, paf.bitvector);
            }
        }

        if (get_light_char(ch) == null && ch.in_room != null
                && ((obj.item_type == ITEM_LIGHT && obj.value[2] != 0)
                || (old_wear == WEAR_HEAD && IS_OBJ_STAT(obj, ITEM_GLOW)))
                && ch.in_room.light > 0) {
            --ch.in_room.light;
        }

        if (IS_SET(obj.progtypes, OPROG_REMOVE)) {
            obj.pIndexData.oprogs.remove_prog.run(obj, ch);
        }


    }

/*
* Count occurrences of an obj in a list.
*/

    static int count_obj_list(OBJ_INDEX_DATA pObjIndex, OBJ_DATA list) {
        OBJ_DATA obj;
        int nMatch;

        nMatch = 0;
        for (obj = list; obj != null; obj = obj.next_content) {
            if (obj.pIndexData == pObjIndex) {
                nMatch++;
            }
        }

        return nMatch;
    }

/*
* Move an obj out of a room.
*/

    static void obj_from_room(OBJ_DATA obj) {
        ROOM_INDEX_DATA in_room;
        CHAR_DATA ch;

        if ((in_room = obj.in_room) == null) {
            bug("obj_from_room: null.");
            return;
        }

        for (ch = in_room.people; ch != null; ch = ch.next_in_room) {
            if (ch.on == obj) {
                ch.on = null;
            }
        }

        if (obj == in_room.contents) {
            in_room.contents = obj.next_content;
        } else {
            OBJ_DATA prev;

            for (prev = in_room.contents; prev != null; prev = prev.next_content) {
                if (prev.next_content == obj) {
                    prev.next_content = obj.next_content;
                    break;
                }
            }

            if (prev == null) {
                bug("Obj_from_room: obj not found.");
                return;
            }
        }

        obj.in_room = null;
        obj.next_content = null;
    }

/*
* Move an obj into a room.
*/

    static void obj_to_room(OBJ_DATA obj, ROOM_INDEX_DATA pRoomIndex) {
        int i;

        obj.next_content = pRoomIndex.contents;
        pRoomIndex.contents = obj;
        obj.in_room = pRoomIndex;
        obj.carried_by = null;
        obj.in_obj = null;

        if (IS_WATER(pRoomIndex)) {
            if (may_float(obj)) {
                obj.water_float = -1;
            } else {
                obj.water_float = floating_time(obj);
            }
        }

        if (obj.pIndexData.vnum < 600) {
            for (i = 1; i < MAX_CABAL; i++) {
                if (cabal_table[i].obj_vnum == obj.pIndexData.vnum) {
                    break;
                }
            }

            if (i < MAX_CABAL) {
                for (i = 1; i < MAX_CABAL; i++) {
                    if (cabal_table[i].room_vnum == pRoomIndex.vnum) {
                        break;
                    }
                }

                if (i < MAX_CABAL) {
                    obj.timer = -1;
                    if (pRoomIndex.people != null) {
                        act("$p loses its transparency and becomes solid.",
                                pRoomIndex.people, obj, null, TO_CHAR);
                        act("$p loses its transparency and becomes solid.",
                                pRoomIndex.people, obj, null, TO_ROOM);
                    }
                }
            }
        }
    }

/*
* Move an object into an object.
*/

    static void obj_to_obj(OBJ_DATA obj, OBJ_DATA obj_to) {

        obj.next_content = obj_to.contains;
        obj_to.contains = obj;
        obj.in_obj = obj_to;
        obj.in_room = null;
        obj.carried_by = null;
        if (obj_to.pIndexData.vnum == OBJ_VNUM_PIT) {
            obj.cost = 0;
        }

        for (; obj_to != null; obj_to = obj_to.in_obj) {
            if (obj_to.carried_by != null) {
/*      obj_to.carried_by.carry_number += get_obj_number( obj ); */
                obj_to.carried_by.carry_weight += get_obj_weight(obj)
                        * WEIGHT_MULT(obj_to) / 100;
            }
        }

    }

/*
* Move an object out of an object.
*/

    static void obj_from_obj(OBJ_DATA obj) {
        OBJ_DATA obj_from;

        if ((obj_from = obj.in_obj) == null) {
            bug("Obj_from_obj: null obj_from.");
            return;
        }

        if (obj == obj_from.contains) {
            obj_from.contains = obj.next_content;
        } else {
            OBJ_DATA prev;

            for (prev = obj_from.contains; prev != null; prev = prev.next_content) {
                if (prev.next_content == obj) {
                    prev.next_content = obj.next_content;
                    break;
                }
            }

            if (prev == null) {
                bug("Obj_from_obj: obj not found.");
                return;
            }
        }

        obj.next_content = null;
        obj.in_obj = null;

        for (; obj_from != null; obj_from = obj_from.in_obj) {
            if (obj_from.carried_by != null) {
/*      obj_from.carried_by.carry_number -= get_obj_number( obj ); */
                obj_from.carried_by.carry_weight -= get_obj_weight(obj)
                        * WEIGHT_MULT(obj_from) / 100;
            }
        }

    }

/*
 * Extract an object consider limit
 */

    static void extract_obj(OBJ_DATA obj) {
        extract_obj_1(obj, true);
    }

/*
 * Extract an object consider limit
 */

    static void extract_obj_nocount(OBJ_DATA obj) {
        extract_obj_1(obj, false);
    }

/*
 * Extract an obj from the world.
 */

    static void extract_obj_1(OBJ_DATA obj, boolean count) {
        OBJ_DATA obj_content;
        OBJ_DATA obj_next;
        int i;


        if (obj.extracted)  /* if the object has already been extracted once */ {
            bug("Warning! Extraction of " + obj.name + ", vnum " + obj.pIndexData.vnum + ".");
            return; /* if it's already been extracted, something bad is going on */
        } else {
            obj.extracted = true;
        }  /* if it hasn't been extracted yet, now
                                   * it's being extracted. */

        if (obj.in_room != null) {
            obj_from_room(obj);
        } else if (obj.carried_by != null) {
            obj_from_char(obj);
        } else if (obj.in_obj != null) {
            obj_from_obj(obj);
        }

        for (i = 1; i < MAX_CABAL; i++) {
            if (obj.pIndexData.vnum == cabal_table[i].obj_vnum && cabal_table[i].obj_ptr == obj) {
                obj.pIndexData.count--;
                cabal_table[i].obj_ptr = null;
            }
        }

        for (obj_content = obj.contains; obj_content != null; obj_content = obj_next) {
            obj_next = obj_content.next_content;
            extract_obj_1(obj_content, count);
        }

        if (obj.pIndexData.vnum == OBJ_VNUM_MAGIC_JAR) {
            CHAR_DATA wch;

            for (wch = char_list; wch != null; wch = wch.next) {
                if (IS_NPC(wch)) {
                    continue;
                }
                if (is_name(obj.name, wch.name)) {
                    wch.act = REMOVE_BIT(wch.act, PLR_NO_EXP);
                    send_to_char("Now you catch your spirit.\n", wch);
                    break;
                }
            }
        }
        if (object_list == obj) {
            object_list = obj.next;
        } else {
            OBJ_DATA prev;

            for (prev = object_list; prev != null; prev = prev.next) {
                if (prev.next == obj) {
                    prev.next = obj.next;
                    break;
                }
            }

            if (prev == null) {
                bug("Extract_obj: obj %d not found.", obj.pIndexData.vnum);
                return;
            }
        }
        if (count) {
            --obj.pIndexData.count;
        }
    }

    static void extract_char(CHAR_DATA ch, boolean fPull) {
        extract_char_org(ch, fPull, true);
    }

    static void extract_char_nocount(CHAR_DATA ch, boolean fPull) {
        extract_char_org(ch, fPull, false);
    }

/*
* Extract a char from the world.
*/

    static void extract_char_org(CHAR_DATA ch, boolean fPull, boolean Count) {
        CHAR_DATA wch;
        OBJ_DATA obj;
        OBJ_DATA obj_next;
        int i;

        if (fPull) /* only for total extractions should it check */ {
            if (ch.extracted)  /* if the char has already been extracted once */ {
                bug("Warning! Extraction of " + ch.name + ".");
                return; /* if it's already been extracted, something bad is going on */
            } else {
                ch.extracted = true;
            }  /* if it hasn't been extracted yet, now
                                   * it's being extracted. */
        }


        if (ch.in_room == null) {
            bug("Extract_char: null.");
            return;
        }

        nuke_pets(ch);
        ch.pet = null; /* just in case */

        if (fPull)

        {
            die_follower(ch);
        }

        stop_fighting(ch, true);

        {
            char_from_room(ch);
        }
        char_to_room(ch, get_room_index(ROOM_VNUM_LIMBO));

        for (obj = ch.carrying; obj != null; obj = obj_next) {
            obj_next = obj.next_content;
            if (Count) {
                extract_obj(obj);
            } else {
                extract_obj_nocount(obj);
            }
        }

        char_from_room(ch);

        if (!fPull) {
            if (IS_GOOD(ch)) {
                i = 0;
            } else if (IS_EVIL(ch)) {
                i = 2;
            } else {
                i = 1;
            }
            char_to_room(ch, get_room_index(hometown_table[ch.hometown].altar[i]));
            return;
        }

        if (IS_NPC(ch)) {
            --ch.pIndexData.count;
        }

        if (ch.desc != null && ch.desc.original != null) {
            do_return(ch);
            ch.desc = null;
        }

        for (wch = char_list; wch != null; wch = wch.next) {
            if (wch.reply == ch) {
                wch.reply = null;
            }
        }

        if (ch == char_list) {
            char_list = ch.next;
        } else {
            CHAR_DATA prev;

            for (prev = char_list; prev != null; prev = prev.next) {
                if (prev.next == ch) {
                    prev.next = ch.next;
                    break;
                }
            }

            if (prev == null) {
                bug("Extract_char: char not found.");
                return;
            }
        }

        if (ch.desc != null) {
            ch.desc.character = null;
        }
        free_char(ch);
    }

/*
* Find a char in the room.
*/

    static CHAR_DATA get_char_room(CHAR_DATA ch, String argument) {
        CHAR_DATA rch;
        int number;
        int count;
        int ugly;

        StringBuilder argB = new StringBuilder();
        number = number_argument(argument, argB);
        String arg = argB.toString();
        count = 0;
        ugly = 0;
        if (!str_cmp(arg, "self")) {
            return ch;
        }
        if (!str_cmp(arg, "ugly")) {
            ugly = 1;
        }

        for (rch = ch.in_room.people; rch != null; rch = rch.next_in_room) {
            if (!can_see(ch, rch)) {
                continue;
            }

            if (ugly != 0 && (count + 1) == number && IS_VAMPIRE(rch)) {
                return rch;
            }

            if ((is_affected(rch, gsn_doppelganger)
                    && !IS_SET(ch.act, PLR_HOLYLIGHT)) ?
                    !is_name(arg, rch.doppel.name) : !is_name(arg, rch.name)) {
                continue;
            }

            if (++count == number) {
                return rch;
            }
        }

        return null;
    }

/*
* Find a char in the room.
* Chronos uses in act_move.c
*/

    static CHAR_DATA get_char_room2(CHAR_DATA ch, ROOM_INDEX_DATA room, String argument, int[] number) {
        CHAR_DATA rch;
        int count;
        int ugly;

        if (room == null) {
            return null;
        }
        count = 0;
        ugly = 0;

        if (!str_cmp(argument, "ugly")) {
            ugly = 1;
        }

        for (rch = room.people; rch != null; rch = rch.next_in_room) {
            if (!can_see(ch, rch)) {
                continue;
            }

            if (ugly != 0 && (count + 1) == number[0] && IS_VAMPIRE(rch)) {
                return rch;
            }

            if ((is_affected(rch, gsn_doppelganger)
                    && !IS_SET(ch.act, PLR_HOLYLIGHT)) ?
                    !is_name(argument, rch.doppel.name) : !is_name(argument, rch.name)) {
                continue;
            }

            if (++count == number[0]) {
                return rch;
            }
        }

        number[0] -= count;
        return null;
    }

/*
* Find a char in the world.
*/

    static CHAR_DATA get_char_world(CHAR_DATA ch, String argument) {
        CHAR_DATA wch;
        int number;
        int count;

        if ((wch = get_char_room(ch, argument)) != null) {
            return wch;
        }

        StringBuilder arg = new StringBuilder();
        number = number_argument(argument, arg);
        String argstr = arg.toString();
        count = 0;
        for (wch = char_list; wch != null; wch = wch.next) {
            if (wch.in_room == null || !can_see(ch, wch)
                    || !is_name(argstr, wch.name)) {
                continue;
            }

            if (++count == number) {
                return wch;
            }
        }

        return null;
    }

/*
* Find some object with a given index data.
* Used by area-reset 'P' command.
*/

    static OBJ_DATA get_obj_type(OBJ_INDEX_DATA pObjIndex) {
        OBJ_DATA obj;

        for (obj = object_list; obj != null; obj = obj.next) {
            if (obj.pIndexData == pObjIndex) {
                return obj;
            }
        }

        return null;
    }

/*
* Find an obj in a list.
*/

    static OBJ_DATA get_obj_list(CHAR_DATA ch, String argument, OBJ_DATA list) {
        OBJ_DATA obj;
        int number;
        int count;

        StringBuilder arg = new StringBuilder();
        number = number_argument(argument, arg);
        String argstr = arg.toString();
        count = 0;
        for (obj = list; obj != null; obj = obj.next_content) {
            if (can_see_obj(ch, obj) && is_name(argstr, obj.name)) {
                if (++count == number) {
                    return obj;
                }
            }
        }

        return null;
    }

/*
* Find an obj in player's inventory.
*/

    static OBJ_DATA get_obj_carry(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        int number;
        int count;

        StringBuilder arg = new StringBuilder();
        number = number_argument(argument, arg);
        String argstr = arg.toString();
        count = 0;
        for (obj = ch.carrying; obj != null; obj = obj.next_content) {
            if (obj.wear_loc == WEAR_NONE && (can_see_obj(ch, obj)) && is_name(argstr, obj.name)) {
                if (++count == number) {
                    return obj;
                }
            }
        }

        return null;
    }

/*
* Find an obj in player's equipment.
*/

    static OBJ_DATA get_obj_wear(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        int number;
        int count;

        StringBuilder arg = new StringBuilder();
        number = number_argument(argument, arg);
        String argstr = arg.toString();
        count = 0;
        for (obj = ch.carrying; obj != null; obj = obj.next_content) {
            if (obj.wear_loc != WEAR_NONE
                    && can_see_obj(ch, obj)
                    && is_name(argstr, obj.name)) {
                if (++count == number) {
                    return obj;
                }
            }
        }

        return null;
    }

/*
* Find an obj in the room or in inventory.
*/

    static OBJ_DATA get_obj_here(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;

        obj = get_obj_list(ch, argument, ch.in_room.contents);
        if (obj != null) {
            return obj;
        }

        if ((obj = get_obj_carry(ch, argument)) != null) {
            return obj;
        }

        if ((obj = get_obj_wear(ch, argument)) != null) {
            return obj;
        }

        return null;
    }

/*
* Find an obj in the world.
*/

    static OBJ_DATA get_obj_world(CHAR_DATA ch, String argument) {
        OBJ_DATA obj;
        int number;
        int count;

        if ((obj = get_obj_here(ch, argument)) != null) {
            return obj;
        }

        StringBuilder arg = new StringBuilder();
        number = number_argument(argument, arg);
        String argstr = arg.toString();
        count = 0;
        for (obj = object_list; obj != null; obj = obj.next) {
            if (can_see_obj(ch, obj) && is_name(argstr, obj.name)) {
                if (++count == number) {
                    return obj;
                }
            }

        }

        return null;
    }

/* deduct cost from a character */

    static void deduct_cost(CHAR_DATA ch, int cost) {
        int silver, gold = 0;

        silver = UMIN(ch.silver, cost);

        if (silver < cost) {
            gold = ((cost - silver + 99) / 100);
            silver = cost - 100 * gold;
        }

        ch.gold -= gold;
        ch.silver -= silver;

        if (ch.gold < 0) {
            bug("deduct costs: gold %d < 0", ch.gold);
            ch.gold = 0;
        }
        if (ch.silver < 0) {
            bug("deduct costs: silver %d < 0", ch.silver);
            ch.silver = 0;
        }
    }
/*
 * Create a 'money' obj.
 */

    static OBJ_DATA create_money(int gold, int silver) {
        OBJ_DATA obj;

        if (gold < 0 || silver < 0 || (gold == 0 && silver == 0)) {
            bug("Create_money: zero or negative money.", UMIN(gold, silver));
            gold = UMAX(1, gold);
            silver = UMAX(1, silver);
        }

        if (gold == 0 && silver == 1) {
            obj = create_object(get_obj_index(OBJ_VNUM_SILVER_ONE), 0);
        } else if (gold == 1 && silver == 0) {
            obj = create_object(get_obj_index(OBJ_VNUM_GOLD_ONE), 0);
        } else if (silver == 0) {
            obj = create_object(get_obj_index(OBJ_VNUM_GOLD_SOME), 0);
            Formatter f = new Formatter();
            f.format(obj.short_descr, gold);
            obj.short_descr = f.toString();
            obj.value[1] = gold;
            obj.cost = gold;
            obj.weight = (gold / 5);
        } else if (gold == 0) {
            obj = create_object(get_obj_index(OBJ_VNUM_SILVER_SOME), 0);
            Formatter f = new Formatter();
            f.format(obj.short_descr, silver);
            obj.short_descr = f.toString();
            obj.value[0] = silver;
            obj.cost = silver;
            obj.weight = (silver / 20);
        } else {
            obj = create_object(get_obj_index(OBJ_VNUM_COINS), 0);
            Formatter f = new Formatter();
            f.format(obj.short_descr, silver, gold);
            obj.short_descr = f.toString();
            obj.value[0] = silver;
            obj.value[1] = gold;
            obj.cost = 100 * gold + silver;
            obj.weight = (gold / 5 + silver / 20);
        }

        return obj;
    }

/*
* Return # of objects which an object counts as.
* Thanks to Tony Chamberlain for the correct recursive code here.
*/

    static int get_obj_number(OBJ_DATA obj) {
        int number;
/*
    if (obj.item_type == ITEM_CONTAINER || obj.item_type == ITEM_MONEY
    ||  obj.item_type == ITEM_GEM || obj.item_type == ITEM_JEWELRY)
        number = 0;
*/
        if (obj.item_type == ITEM_MONEY) {
            number = 0;
        } else {
            number = 1;
        }

/*
    for ( obj = obj.contains; obj != null; obj = obj.next_content )
        number += get_obj_number( obj );
*/
        return number;
    }

    static int get_obj_realnumber(OBJ_DATA obj) {
        int number = 1;

        for (obj = obj.contains; obj != null; obj = obj.next_content) {
            number += get_obj_number(obj);
        }

        return number;
    }

/*
 * Return weight of an object, including weight of contents.
 */

    static int get_obj_weight(OBJ_DATA obj) {
        int weight;
        OBJ_DATA tobj;

        weight = obj.weight;
        for (tobj = obj.contains; tobj != null; tobj = tobj.next_content) {
            weight += get_obj_weight(tobj) * WEIGHT_MULT(obj) / 100;
        }

        return weight;
    }

    static int get_true_weight(OBJ_DATA obj) {
        int weight;

        weight = obj.weight;
        for (obj = obj.contains; obj != null; obj = obj.next_content) {
            weight += get_obj_weight(obj);
        }

        return weight;
    }

/*
 * true if room is dark.
 */

    static boolean room_is_dark(CHAR_DATA ch) {
        ROOM_INDEX_DATA pRoomIndex = ch.in_room;

        if (IS_VAMPIRE(ch)) {
            return false;
        }

        if (pRoomIndex.light > 0) {
            return false;
        }

        if (IS_SET(pRoomIndex.room_flags, ROOM_DARK)) {
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (pRoomIndex.sector_type == SECT_INSIDE || pRoomIndex.sector_type == SECT_CITY) {
            return false;
        }

        return weather_info.sunlight == SUN_SET || weather_info.sunlight == SUN_DARK;

    }

    static boolean room_dark(ROOM_INDEX_DATA pRoomIndex) {
        if (pRoomIndex.light > 0) {
            return false;
        }

        if (IS_SET(pRoomIndex.room_flags, ROOM_DARK)) {
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (pRoomIndex.sector_type == SECT_INSIDE
                || pRoomIndex.sector_type == SECT_CITY) {
            return false;
        }

        return weather_info.sunlight == SUN_SET || weather_info.sunlight == SUN_DARK;

    }


    static boolean is_room_owner(CHAR_DATA ch, ROOM_INDEX_DATA room) {
        return room.owner != null && !room.owner.isEmpty() && is_name(ch.name, room.owner);

    }

/*
 * true if room is private.
 */

    static boolean room_is_private(ROOM_INDEX_DATA pRoomIndex) {
        CHAR_DATA rch;
        int count;

/*
    if (pRoomIndex.owner != null && pRoomIndex.owner.length()!=0)
    return true;
*/
        count = 0;
        for (rch = pRoomIndex.people; rch != null; rch = rch.next_in_room) {
            count++;
        }

        if (IS_SET(pRoomIndex.room_flags, ROOM_PRIVATE) && count >= 2) {
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (IS_SET(pRoomIndex.room_flags, ROOM_SOLITARY) && count >= 1) {
            return true;
        }

        return IS_SET(pRoomIndex.room_flags, ROOM_IMP_ONLY);

    }

/* visibility on a room -- for entering and exits */

    static boolean can_see_room(CHAR_DATA ch, ROOM_INDEX_DATA pRoomIndex) {
        if (IS_SET(pRoomIndex.room_flags, ROOM_IMP_ONLY)
                && get_trust(ch) < MAX_LEVEL) {
            return false;
        }

        if (IS_SET(pRoomIndex.room_flags, ROOM_GODS_ONLY)
                && !IS_IMMORTAL(ch)) {
            return false;
        }

        //noinspection SimplifiableIfStatement
        if (IS_SET(pRoomIndex.room_flags, ROOM_HEROES_ONLY)
                && !IS_IMMORTAL(ch)) {
            return false;
        }

        return !(IS_SET(pRoomIndex.room_flags, ROOM_NEWBIES_ONLY)
                && ch.level > 5 && !IS_IMMORTAL(ch));

    }

/*
* true if char can see victim.
*/

    static boolean can_see(CHAR_DATA ch, CHAR_DATA victim) {
/* RT changed so that WIZ_INVIS has levels */
        assert (ch != null && victim != null);

        if (ch == victim) {
            return true;
        }


        if (get_trust(ch) < victim.invis_level) {
            return false;
        }


        if (get_trust(ch) < victim.incog_level && ch.in_room != victim.in_room) {
            return false;
        }

        if ((!IS_NPC(ch) && IS_SET(ch.act, PLR_HOLYLIGHT))
                || (IS_NPC(ch) && IS_IMMORTAL(ch))) {
            return true;
        }

        if (IS_AFFECTED(ch, AFF_BLIND)) {
            return false;
        }

        if (ch.in_room == null) {
            return false;
        }

        if (room_is_dark(ch) && !IS_AFFECTED(ch, AFF_INFRARED)) {
            return false;
        }

        if (IS_AFFECTED(victim, AFF_INVISIBLE)
                && !IS_AFFECTED(ch, AFF_DETECT_INVIS)) {
            return false;
        }

        if (IS_AFFECTED(victim, AFF_IMP_INVIS)
                && !IS_AFFECTED(ch, AFF_DETECT_IMP_INVIS)) {
            return false;
        }

/* sneaking

    if ( IS_AFFECTED(victim, AFF_SNEAK)
    &&   !IS_AFFECTED(ch,AFF_DETECT_HIDDEN)
    &&   victim.fighting == null)
    {
    int chance;
    chance = get_skill(victim,gsn_sneak);
    chance += get_curr_stat(ch,STAT_DEX) * 3/2;
    chance -= get_curr_stat(ch,STAT_INT) * 2;
    chance += ch.level - victim.level * 3/2;

    if (number_percent() < chance)
        return false;
    }
*/

        if (IS_AFFECTED(victim, AFF_CAMOUFLAGE) && !IS_AFFECTED(ch, AFF_ACUTE_VISION)) {
            return false;
        }

        if (IS_AFFECTED(victim, AFF_HIDE)
                && !IS_AFFECTED(ch, AFF_DETECT_HIDDEN)
                && victim.fighting == null) {
            return false;
        }

        //noinspection SimplifiableIfStatement
        if (IS_AFFECTED(victim, AFF_FADE)
                && !IS_AFFECTED(ch, AFF_DETECT_FADE)
                && victim.fighting == null) {
            return false;
        }

        return !IS_AFFECTED(victim, AFF_EARTHFADE);
    }

/*
* true if char can see obj.
*/

    static boolean can_see_obj(CHAR_DATA ch, OBJ_DATA obj) {
        if (!IS_NPC(ch) && IS_SET(ch.act, PLR_HOLYLIGHT)) {
            return true;
        }

        if (IS_SET(obj.extra_flags, ITEM_VIS_DEATH)) {
            return false;
        }

        if (IS_AFFECTED(ch, AFF_BLIND) && obj.item_type != ITEM_POTION) {
            return false;
        }

        if (obj.item_type == ITEM_LIGHT && obj.value[2] != 0) {
            return true;
        }

        if (IS_SET(obj.extra_flags, ITEM_INVIS)
                && !IS_AFFECTED(ch, AFF_DETECT_INVIS)) {
            return false;
        }

        if (IS_SET(obj.extra_flags, ITEM_BURIED) && !IS_IMMORTAL(ch)) {
            return false;
        }

        if (IS_OBJ_STAT(obj, ITEM_GLOW)) {
            return true;
        }

        if (room_is_dark(ch) && !IS_AFFECTED(ch, AFF_INFRARED)) {
            return false;
        }

        if (obj.item_type == ITEM_TATTOO) {
            return true;
        }

        return true;
    }

/*
* true if char can drop obj.
*/

    static boolean can_drop_obj(CHAR_DATA ch, OBJ_DATA obj) {
        //noinspection SimplifiableIfStatement
        if (!IS_SET(obj.extra_flags, ITEM_NODROP)) {
            return true;
        }

        return !IS_NPC(ch) && ch.level >= LEVEL_IMMORTAL;

    }

/*
* Return ascii name of an item type.
*/

    static String item_type_name(OBJ_DATA obj) {
        switch (obj.item_type) {
            case ITEM_LIGHT:
                return "light";
            case ITEM_SCROLL:
                return "scroll";
            case ITEM_WAND:
                return "wand";
            case ITEM_STAFF:
                return "staff";
            case ITEM_WEAPON:
                return "weapon";
            case ITEM_TREASURE:
                return "treasure";
            case ITEM_ARMOR:
                return "armor";
            case ITEM_CLOTHING:
                return "clothing";
            case ITEM_POTION:
                return "potion";
            case ITEM_FURNITURE:
                return "furniture";
            case ITEM_TRASH:
                return "trash";
            case ITEM_CONTAINER:
                return "container";
            case ITEM_DRINK_CON:
                return "drink container";
            case ITEM_KEY:
                return "key";
            case ITEM_FOOD:
                return "food";
            case ITEM_MONEY:
                return "money";
            case ITEM_BOAT:
                return "boat";
            case ITEM_CORPSE_NPC:
                return "npc corpse";
            case ITEM_CORPSE_PC:
                return "pc corpse";
            case ITEM_FOUNTAIN:
                return "fountain";
            case ITEM_PILL:
                return "pill";
            case ITEM_MAP:
                return "map";
            case ITEM_PORTAL:
                return "portal";
            case ITEM_WARP_STONE:
                return "warp stone";
            case ITEM_GEM:
                return "gem";
            case ITEM_JEWELRY:
                return "jewelry";
            case ITEM_JUKEBOX:
                return "juke box";
            case ITEM_TATTOO:
                return "tattoo";
        }

        bug("Item_type_name: unknown type %d.", obj.item_type);
        return "(unknown)";
    }

/*
* Return ascii name of an affect location.
*/

    static String affect_loc_name(int location) {
        switch (location) {
            case APPLY_NONE:
                return "none";
            case APPLY_STR:
                return "strength";
            case APPLY_DEX:
                return "dexterity";
            case APPLY_INT:
                return "intelligence";
            case APPLY_WIS:
                return "wisdom";
            case APPLY_CON:
                return "constitution";
            case APPLY_CHA:
                return "charisma";
            case APPLY_CLASS:
                return "class";
            case APPLY_LEVEL:
                return "level";
            case APPLY_AGE:
                return "age";
            case APPLY_MANA:
                return "mana";
            case APPLY_HIT:
                return "hp";
            case APPLY_MOVE:
                return "moves";
            case APPLY_GOLD:
                return "gold";
            case APPLY_EXP:
                return "experience";
            case APPLY_AC:
                return "armor class";
            case APPLY_HITROLL:
                return "hit roll";
            case APPLY_DAMROLL:
                return "damage roll";
            case APPLY_SIZE:
                return "size";
            case APPLY_SAVES:
                return "saves";
            case APPLY_SAVING_ROD:
                return "save vs rod";
            case APPLY_SAVING_PETRI:
                return "save vs petrification";
            case APPLY_SAVING_BREATH:
                return "save vs breath";
            case APPLY_SAVING_SPELL:
                return "save vs spell";
            case APPLY_SPELL_AFFECT:
                return "none";
        }

        bug("Affect_location_name: unknown location %d.", location);
        return "(unknown)";
    }

    /*
    * Return ascii name of an affect bit vector.
    */
    private static StringBuilder stat_buf = new StringBuilder();

    static String affect_bit_name(long vector) {
        stat_buf.setLength(0);
        if ((vector & AFF_BLIND) != 0) {
            stat_buf.append(" blind");
        }
        if ((vector & AFF_INVISIBLE) != 0) {
            stat_buf.append(" invisible");
        }
        if ((vector & AFF_IMP_INVIS) != 0) {
            stat_buf.append(" imp_invis");
        }
        if ((vector & AFF_FADE) != 0) {
            stat_buf.append(" fade");
        }
        if ((vector & AFF_SCREAM) != 0) {
            stat_buf.append(" scream");
        }
        if ((vector & AFF_BLOODTHIRST) != 0) {
            stat_buf.append(" bloodthirst");
        }
        if ((vector & AFF_STUN) != 0) {
            stat_buf.append(" stun");
        }
        if ((vector & AFF_SANCTUARY) != 0) {
            stat_buf.append(" sanctuary");
        }
        if ((vector & AFF_FAERIE_FIRE) != 0) {
            stat_buf.append(" faerie_fire");
        }
        if ((vector & AFF_INFRARED) != 0) {
            stat_buf.append(" infrared");
        }
        if ((vector & AFF_CURSE) != 0) {
            stat_buf.append(" curse");
        }
        if ((vector & AFF_POISON) != 0) {
            stat_buf.append(" poison");
        }
        if ((vector & AFF_PROTECT_EVIL) != 0) {
            stat_buf.append(" prot_evil");
        }
        if ((vector & AFF_PROTECT_GOOD) != 0) {
            stat_buf.append(" prot_good");
        }
        if ((vector & AFF_SLEEP) != 0) {
            stat_buf.append(" sleep");
        }
        if ((vector & AFF_SNEAK) != 0) {
            stat_buf.append(" sneak");
        }
        if ((vector & AFF_HIDE) != 0) {
            stat_buf.append(" hide");
        }
        if ((vector & AFF_CHARM) != 0) {
            stat_buf.append(" charm");
        }
        if ((vector & AFF_FLYING) != 0) {
            stat_buf.append(" flying");
        }
        if ((vector & AFF_PASS_DOOR) != 0) {
            stat_buf.append(" pass_door");
        }
        if ((vector & AFF_BERSERK) != 0) {
            stat_buf.append(" berserk");
        }
        if ((vector & AFF_CALM) != 0) {
            stat_buf.append(" calm");
        }
        if ((vector & AFF_HASTE) != 0) {
            stat_buf.append(" haste");
        }
        if ((vector & AFF_SLOW) != 0) {
            stat_buf.append(" slow");
        }
        if ((vector & AFF_WEAKEN) != 0) {
            stat_buf.append(" weaken");
        }
        if ((vector & AFF_PLAGUE) != 0) {
            stat_buf.append(" plague");
        }
        if ((vector & AFF_REGENERATION) != 0) {
            stat_buf.append(" regeneration");
        }
        if ((vector & AFF_CAMOUFLAGE) != 0) {
            stat_buf.append(" camouflage");
        }
        if ((vector & AFF_SWIM) != 0) {
            stat_buf.append(" swim");
        }
        return stat_buf.length() == 0 ? stat_buf.toString() : "none";
    }

/*
* Return ascii name of an affect bit vector.
*/

    static String detect_bit_name(int vector) {

        stat_buf.setLength(0);
        if ((vector & AFF_DETECT_IMP_INVIS) != 0) {
            stat_buf.append(" detect_imp_inv");
        }
        if ((vector & AFF_DETECT_EVIL) != 0) {
            stat_buf.append(" detect_evil");
        }
        if ((vector & AFF_DETECT_GOOD) != 0) {
            stat_buf.append(" detect_good");
        }
        if ((vector & AFF_DETECT_INVIS) != 0) {
            stat_buf.append(" detect_invis");
        }
        if ((vector & AFF_DETECT_MAGIC) != 0) {
            stat_buf.append(" detect_magic");
        }
        if ((vector & AFF_DETECT_HIDDEN) != 0) {
            stat_buf.append(" detect_hidden");
        }
        if ((vector & AFF_DARK_VISION) != 0) {
            stat_buf.append(" dark_vision");
        }
        if ((vector & AFF_ACUTE_VISION) != 0) {
            stat_buf.append(" acute_vision");
        }
        if ((vector & AFF_FEAR) != 0) {
            stat_buf.append(" fear");
        }
        if ((vector & AFF_FORM_TREE) != 0) {
            stat_buf.append(" form_tree");
        }
        if ((vector & AFF_FORM_GRASS) != 0) {
            stat_buf.append(" form_grass");
        }
        if ((vector & AFF_WEB) != 0) {
            stat_buf.append(" web");
        }
        if ((vector & AFF_DETECT_LIFE) != 0) {
            stat_buf.append(" life");
        }
        if ((vector & AFF_DETECT_SNEAK) != 0) {
            stat_buf.append(" detect_sneak");
        }
        return stat_buf.length() != 0 ? stat_buf.toString() : "none";
    }

/*
* Return ascii name of extra flags vector.
*/

    static String extra_bit_name(long extra_flags) {

        stat_buf.setLength(0);
        if ((extra_flags & ITEM_GLOW) != 0) {
            stat_buf.append(" glow");
        }
        if ((extra_flags & ITEM_HUM) != 0) {
            stat_buf.append(" hum");
        }
        if ((extra_flags & ITEM_DARK) != 0) {
            stat_buf.append(" dark");
        }
        if ((extra_flags & ITEM_LOCK) != 0) {
            stat_buf.append(" lock");
        }
        if ((extra_flags & ITEM_EVIL) != 0) {
            stat_buf.append(" evil");
        }
        if ((extra_flags & ITEM_INVIS) != 0) {
            stat_buf.append(" invis");
        }
        if ((extra_flags & ITEM_MAGIC) != 0) {
            stat_buf.append(" magic");
        }
        if ((extra_flags & ITEM_NODROP) != 0) {
            stat_buf.append(" nodrop");
        }
        if ((extra_flags & ITEM_BLESS) != 0) {
            stat_buf.append(" bless");
        }
        if ((extra_flags & ITEM_ANTI_GOOD) != 0) {
            stat_buf.append(" anti-good");
        }
        if ((extra_flags & ITEM_ANTI_EVIL) != 0) {
            stat_buf.append(" anti-evil");
        }
        if ((extra_flags & ITEM_ANTI_NEUTRAL) != 0) {
            stat_buf.append(" anti-neutral");
        }
        if ((extra_flags & ITEM_NOREMOVE) != 0) {
            stat_buf.append(" noremove");
        }
        if ((extra_flags & ITEM_INVENTORY) != 0) {
            stat_buf.append(" inventory");
        }
        if ((extra_flags & ITEM_NOPURGE) != 0) {
            stat_buf.append(" nopurge");
        }
        if ((extra_flags & ITEM_VIS_DEATH) != 0) {
            stat_buf.append(" vis_death");
        }
        if ((extra_flags & ITEM_ROT_DEATH) != 0) {
            stat_buf.append(" rot_death");
        }
        if ((extra_flags & ITEM_NOLOCATE) != 0) {
            stat_buf.append(" no_locate");
        }
        if ((extra_flags & ITEM_SELL_EXTRACT) != 0) {
            stat_buf.append(" sell_extract");
        }
        if ((extra_flags & ITEM_BURN_PROOF) != 0) {
            stat_buf.append(" burn_proof");
        }
        if ((extra_flags & ITEM_NOUNCURSE) != 0) {
            stat_buf.append(" no_uncurse");
        }
        if ((extra_flags & ITEM_BURIED) != 0) {
            stat_buf.append(" buried");
        }
        return stat_buf.length() != 0 ? stat_buf.toString() : "none";
    }

/* return ascii name of an act vector */

    static String act_bit_name(long act_flags) {
        stat_buf.setLength(0);


        if (IS_SET(act_flags, ACT_IS_NPC)) {
            stat_buf.append(" npc");
            if ((act_flags & ACT_SENTINEL) != 0) {
                stat_buf.append(" sentinel");
            }
            if ((act_flags & ACT_SCAVENGER) != 0) {
                stat_buf.append(" scavenger");
            }
            if ((act_flags & ACT_AGGRESSIVE) != 0) {
                stat_buf.append(" aggressive");
            }
            if ((act_flags & ACT_STAY_AREA) != 0) {
                stat_buf.append(" stay_area");
            }
            if ((act_flags & ACT_WIMPY) != 0) {
                stat_buf.append(" wimpy");
            }
            if ((act_flags & ACT_PET) != 0) {
                stat_buf.append(" pet");
            }
            if ((act_flags & ACT_TRAIN) != 0) {
                stat_buf.append(" train");
            }
            if ((act_flags & ACT_PRACTICE) != 0) {
                stat_buf.append(" practice");
            }
            if ((act_flags & ACT_UNDEAD) != 0) {
                stat_buf.append(" undead");
            }
            if ((act_flags & ACT_HUNTER) != 0) {
                stat_buf.append(" hunter");
            }
            if ((act_flags & ACT_CLERIC) != 0) {
                stat_buf.append(" cleric");
            }
            if ((act_flags & ACT_MAGE) != 0) {
                stat_buf.append(" mage");
            }
            if ((act_flags & ACT_THIEF) != 0) {
                stat_buf.append(" thief");
            }
            if ((act_flags & ACT_WARRIOR) != 0) {
                stat_buf.append(" warrior");
            }
            if ((act_flags & ACT_NOALIGN) != 0) {
                stat_buf.append(" no_align");
            }
            if ((act_flags & ACT_NOPURGE) != 0) {
                stat_buf.append(" no_purge");
            }
            if ((act_flags & ACT_IS_HEALER) != 0) {
                stat_buf.append(" healer");
            }
            if ((act_flags & ACT_IS_CHANGER) != 0) {
                stat_buf.append(" changer");
            }
            if ((act_flags & ACT_GAIN) != 0) {
                stat_buf.append(" skill_train");
            }
            if ((act_flags & ACT_UPDATE_ALWAYS) != 0) {
                stat_buf.append(" update_always");
            }
        } else {
            stat_buf.append(" player");
            if ((act_flags & PLR_AUTOASSIST) != 0) {
                stat_buf.append(" autoassist");
            }
            if ((act_flags & PLR_AUTOEXIT) != 0) {
                stat_buf.append(" autoexit");
            }
            if ((act_flags & PLR_AUTOLOOT) != 0) {
                stat_buf.append(" autoloot");
            }
            if ((act_flags & PLR_AUTOSAC) != 0) {
                stat_buf.append(" autosac");
            }
            if ((act_flags & PLR_AUTOGOLD) != 0) {
                stat_buf.append(" autogold");
            }
            if ((act_flags & PLR_AUTOSPLIT) != 0) {
                stat_buf.append(" autosplit");
            }
            if ((act_flags & PLR_COLOR) != 0) {
                stat_buf.append(" color_on");
            }
            if ((act_flags & PLR_WANTED) != 0) {
                stat_buf.append(" wanted");
            }
            if ((act_flags & PLR_NO_TITLE) != 0) {
                stat_buf.append(" no_title");
            }
            if ((act_flags & PLR_NO_EXP) != 0) {
                stat_buf.append(" no_exp");
            }
            if ((act_flags & PLR_HOLYLIGHT) != 0) {
                stat_buf.append(" holy_light");
            }
            if ((act_flags & PLR_NOCANCEL) != 0) {
                stat_buf.append(" no_cancel");
            }
            if ((act_flags & PLR_CANLOOT) != 0) {
                stat_buf.append(" loot_corpse");
            }
            if ((act_flags & PLR_NOSUMMON) != 0) {
                stat_buf.append(" no_summon");
            }
            if ((act_flags & PLR_NOFOLLOW) != 0) {
                stat_buf.append(" no_follow");
            }
            if ((act_flags & PLR_CANINDUCT) != 0) {
                stat_buf.append(" Cabal_LEADER");
            }
            if ((act_flags & PLR_GHOST) != 0) {
                stat_buf.append(" ghost");
            }
            if ((act_flags & PLR_PERMIT) != 0) {
                stat_buf.append(" permit");
            }
            if ((act_flags & PLR_REMORTED) != 0) {
                stat_buf.append(" remorted");
            }
            if ((act_flags & PLR_LOG) != 0) {
                stat_buf.append(" log");
            }
            if ((act_flags & PLR_FREEZE) != 0) {
                stat_buf.append(" frozen");
            }
            if ((act_flags & PLR_LEFTHAND) != 0) {
                stat_buf.append(" lefthand");
            }
            if ((act_flags & PLR_CANREMORT) != 0) {
                stat_buf.append(" canremort");
            }
            if ((act_flags & PLR_QUESTOR) != 0) {
                stat_buf.append(" questor");
            }
            if ((act_flags & PLR_VAMPIRE) != 0) {
                stat_buf.append(" VAMPIRE");
            }
            if ((act_flags & PLR_HARA_KIRI) != 0) {
                stat_buf.append(" harakiri");
            }
            if ((act_flags & PLR_BLINK_ON) != 0) {
                stat_buf.append(" blink_on");
            }
        }
        return stat_buf.length() != 0 ? stat_buf.toString() : "none";
    }

    static String comm_bit_name(long comm_flags) {
        stat_buf.setLength(0);
        if ((comm_flags & COMM_QUIET) != 0) {
            stat_buf.append(" quiet");
        }
        if ((comm_flags & COMM_DEAF) != 0) {
            stat_buf.append(" deaf");
        }
        if ((comm_flags & COMM_NOWIZ) != 0) {
            stat_buf.append(" no_wiz");
        }
        if ((comm_flags & COMM_NOAUCTION) != 0) {
            stat_buf.append(" no_auction");
        }
        if ((comm_flags & COMM_NOGOSSIP) != 0) {
            stat_buf.append(" no_gossip");
        }
        if ((comm_flags & COMM_NOQUESTION) != 0) {
            stat_buf.append(" no_question");
        }
        if ((comm_flags & COMM_NOMUSIC) != 0) {
            stat_buf.append(" no_music");
        }
        if ((comm_flags & COMM_NOQUOTE) != 0) {
            stat_buf.append(" no_quote");
        }
        if ((comm_flags & COMM_COMPACT) != 0) {
            stat_buf.append(" compact");
        }
        if ((comm_flags & COMM_BRIEF) != 0) {
            stat_buf.append(" brief");
        }
        if ((comm_flags & COMM_PROMPT) != 0) {
            stat_buf.append(" prompt");
        }
        if ((comm_flags & COMM_COMBINE) != 0) {
            stat_buf.append(" combine");
        }
        if ((comm_flags & COMM_NOEMOTE) != 0) {
            stat_buf.append(" no_emote");
        }
        if ((comm_flags & COMM_NOSHOUT) != 0) {
            stat_buf.append(" no_shout");
        }
        if ((comm_flags & COMM_NOTELL) != 0) {
            stat_buf.append(" no_tell");
        }
        if ((comm_flags & COMM_NOCHANNELS) != 0) {
            stat_buf.append(" no_channels");
        }


        return stat_buf.length() != 0 ? stat_buf.toString() : "none";
    }

    static String imm_bit_name(long imm_flags) {
        stat_buf.setLength(0);
        if ((imm_flags & IMM_SUMMON) != 0) {
            stat_buf.append(" summon");
        }
        if ((imm_flags & IMM_CHARM) != 0) {
            stat_buf.append(" charm");
        }
        if ((imm_flags & IMM_MAGIC) != 0) {
            stat_buf.append(" magic");
        }
        if ((imm_flags & IMM_WEAPON) != 0) {
            stat_buf.append(" weapon");
        }
        if ((imm_flags & IMM_BASH) != 0) {
            stat_buf.append(" blunt");
        }
        if ((imm_flags & IMM_PIERCE) != 0) {
            stat_buf.append(" piercing");
        }
        if ((imm_flags & IMM_SLASH) != 0) {
            stat_buf.append(" slashing");
        }
        if ((imm_flags & IMM_FIRE) != 0) {
            stat_buf.append(" fire");
        }
        if ((imm_flags & IMM_COLD) != 0) {
            stat_buf.append(" cold");
        }
        if ((imm_flags & IMM_LIGHTNING) != 0) {
            stat_buf.append(" lightning");
        }
        if ((imm_flags & IMM_ACID) != 0) {
            stat_buf.append(" acid");
        }
        if ((imm_flags & IMM_POISON) != 0) {
            stat_buf.append(" poison");
        }
        if ((imm_flags & IMM_NEGATIVE) != 0) {
            stat_buf.append(" negative");
        }
        if ((imm_flags & IMM_HOLY) != 0) {
            stat_buf.append(" holy");
        }
        if ((imm_flags & IMM_ENERGY) != 0) {
            stat_buf.append(" energy");
        }
        if ((imm_flags & IMM_MENTAL) != 0) {
            stat_buf.append(" mental");
        }
        if ((imm_flags & IMM_DISEASE) != 0) {
            stat_buf.append(" disease");
        }
        if ((imm_flags & IMM_DROWNING) != 0) {
            stat_buf.append(" drowning");
        }
        if ((imm_flags & IMM_LIGHT) != 0) {
            stat_buf.append(" light");
        }
        if ((imm_flags & VULN_IRON) != 0) {
            stat_buf.append(" iron");
        }
        if ((imm_flags & VULN_WOOD) != 0) {
            stat_buf.append(" wood");
        }
        if ((imm_flags & VULN_SILVER) != 0) {
            stat_buf.append(" silver");
        }

        return stat_buf.length() != 0 ? stat_buf.toString() : "none";
    }

    static String wear_bit_name(int wear_flags) {
        stat_buf.setLength(0);
        if ((wear_flags & ITEM_TAKE) != 0) {
            stat_buf.append(" take");
        }
        if ((wear_flags & ITEM_WEAR_FINGER) != 0) {
            stat_buf.append(" finger");
        }
        if ((wear_flags & ITEM_WEAR_NECK) != 0) {
            stat_buf.append(" neck");
        }
        if ((wear_flags & ITEM_WEAR_BODY) != 0) {
            stat_buf.append(" torso");
        }
        if ((wear_flags & ITEM_WEAR_HEAD) != 0) {
            stat_buf.append(" head");
        }
        if ((wear_flags & ITEM_WEAR_LEGS) != 0) {
            stat_buf.append(" legs");
        }
        if ((wear_flags & ITEM_WEAR_FEET) != 0) {
            stat_buf.append(" feet");
        }
        if ((wear_flags & ITEM_WEAR_HANDS) != 0) {
            stat_buf.append(" hands");
        }
        if ((wear_flags & ITEM_WEAR_ARMS) != 0) {
            stat_buf.append(" arms");
        }
        if ((wear_flags & ITEM_WEAR_SHIELD) != 0) {
            stat_buf.append(" shield");
        }
        if ((wear_flags & ITEM_WEAR_ABOUT) != 0) {
            stat_buf.append(" body");
        }
        if ((wear_flags & ITEM_WEAR_WAIST) != 0) {
            stat_buf.append(" waist");
        }
        if ((wear_flags & ITEM_WEAR_WRIST) != 0) {
            stat_buf.append(" wrist");
        }
        if ((wear_flags & ITEM_WIELD) != 0) {
            stat_buf.append(" wield");
        }
        if ((wear_flags & ITEM_HOLD) != 0) {
            stat_buf.append(" hold");
        }
        if ((wear_flags & ITEM_WEAR_FLOAT) != 0) {
            stat_buf.append(" float");
        }
        if ((wear_flags & ITEM_WEAR_TATTOO) != 0) {
            stat_buf.append(" tattoo");
        }

        return stat_buf.length() != 0 ? stat_buf.toString() : "none";
    }

    static String form_bit_name(int form_flags) {
        stat_buf.setLength(0);
        if ((form_flags & FORM_POISON) != 0) {
            stat_buf.append(" poison");
        } else if ((form_flags & FORM_EDIBLE) != 0) {
            stat_buf.append(" edible");
        }
        if ((form_flags & FORM_MAGICAL) != 0) {
            stat_buf.append(" magical");
        }
        if ((form_flags & FORM_INSTANT_DECAY) != 0) {
            stat_buf.append(" instant_rot");
        }
        if ((form_flags & FORM_OTHER) != 0) {
            stat_buf.append(" other");
        }
        if ((form_flags & FORM_ANIMAL) != 0) {
            stat_buf.append(" animal");
        }
        if ((form_flags & FORM_SENTIENT) != 0) {
            stat_buf.append(" sentient");
        }
        if ((form_flags & FORM_UNDEAD) != 0) {
            stat_buf.append(" undead");
        }
        if ((form_flags & FORM_CONSTRUCT) != 0) {
            stat_buf.append(" construct");
        }
        if ((form_flags & FORM_MIST) != 0) {
            stat_buf.append(" mist");
        }
        if ((form_flags & FORM_INTANGIBLE) != 0) {
            stat_buf.append(" intangible");
        }
        if ((form_flags & FORM_BIPED) != 0) {
            stat_buf.append(" biped");
        }
        if ((form_flags & FORM_CENTAUR) != 0) {
            stat_buf.append(" centaur");
        }
        if ((form_flags & FORM_INSECT) != 0) {
            stat_buf.append(" insect");
        }
        if ((form_flags & FORM_SPIDER) != 0) {
            stat_buf.append(" spider");
        }
        if ((form_flags & FORM_CRUSTACEAN) != 0) {
            stat_buf.append(" crustacean");
        }
        if ((form_flags & FORM_WORM) != 0) {
            stat_buf.append(" worm");
        }
        if ((form_flags & FORM_BLOB) != 0) {
            stat_buf.append(" blob");
        }
        if ((form_flags & FORM_MAMMAL) != 0) {
            stat_buf.append(" mammal");
        }
        if ((form_flags & FORM_BIRD) != 0) {
            stat_buf.append(" bird");
        }
        if ((form_flags & FORM_REPTILE) != 0) {
            stat_buf.append(" reptile");
        }
        if ((form_flags & FORM_SNAKE) != 0) {
            stat_buf.append(" snake");
        }
        if ((form_flags & FORM_DRAGON) != 0) {
            stat_buf.append(" dragon");
        }
        if ((form_flags & FORM_AMPHIBIAN) != 0) {
            stat_buf.append(" amphibian");
        }
        if ((form_flags & FORM_FISH) != 0) {
            stat_buf.append(" fish");
        }
        if ((form_flags & FORM_COLD_BLOOD) != 0) {
            stat_buf.append(" cold_blooded");
        }

        return stat_buf.length() != 0 ? stat_buf.toString() : "none";
    }

    static String part_bit_name(int part_flags) {
        stat_buf.setLength(0);
        if ((part_flags & PART_HEAD) != 0) {
            stat_buf.append(" head");
        }
        if ((part_flags & PART_ARMS) != 0) {
            stat_buf.append(" arms");
        }
        if ((part_flags & PART_LEGS) != 0) {
            stat_buf.append(" legs");
        }
        if ((part_flags & PART_HEART) != 0) {
            stat_buf.append(" heart");
        }
        if ((part_flags & PART_BRAINS) != 0) {
            stat_buf.append(" brains");
        }
        if ((part_flags & PART_GUTS) != 0) {
            stat_buf.append(" guts");
        }
        if ((part_flags & PART_HANDS) != 0) {
            stat_buf.append(" hands");
        }
        if ((part_flags & PART_FEET) != 0) {
            stat_buf.append(" feet");
        }
        if ((part_flags & PART_FINGERS) != 0) {
            stat_buf.append(" fingers");
        }
        if ((part_flags & PART_EAR) != 0) {
            stat_buf.append(" ears");
        }
        if ((part_flags & PART_EYE) != 0) {
            stat_buf.append(" eyes");
        }
        if ((part_flags & PART_LONG_TONGUE) != 0) {
            stat_buf.append(" long_tongue");
        }
        if ((part_flags & PART_EYESTALKS) != 0) {
            stat_buf.append(" eyestalks");
        }
        if ((part_flags & PART_TENTACLES) != 0) {
            stat_buf.append(" tentacles");
        }
        if ((part_flags & PART_FINS) != 0) {
            stat_buf.append(" fins");
        }
        if ((part_flags & PART_WINGS) != 0) {
            stat_buf.append(" wings");
        }
        if ((part_flags & PART_TAIL) != 0) {
            stat_buf.append(" tail");
        }
        if ((part_flags & PART_CLAWS) != 0) {
            stat_buf.append(" claws");
        }
        if ((part_flags & PART_FANGS) != 0) {
            stat_buf.append(" fangs");
        }
        if ((part_flags & PART_HORNS) != 0) {
            stat_buf.append(" horns");
        }
        if ((part_flags & PART_SCALES) != 0) {
            stat_buf.append(" scales");
        }

        return stat_buf.length() != 0 ? stat_buf.toString() : "none";
    }

    static String weapon_bit_name(long weapon_flags) {
        stat_buf.setLength(0);

        if ((weapon_flags & WEAPON_FLAMING) != 0) {
            stat_buf.append(" flaming");
        }
        if ((weapon_flags & WEAPON_FROST) != 0) {
            stat_buf.append(" frost");
        }
        if ((weapon_flags & WEAPON_VAMPIRIC) != 0) {
            stat_buf.append(" vampiric");
        }
        if ((weapon_flags & WEAPON_SHARP) != 0) {
            stat_buf.append(" sharp");
        }
        if ((weapon_flags & WEAPON_VORPAL) != 0) {
            stat_buf.append(" vorpal");
        }
        if ((weapon_flags & WEAPON_TWO_HANDS) != 0) {
            stat_buf.append(" two-handed");
        }
        if ((weapon_flags & WEAPON_SHOCKING) != 0) {
            stat_buf.append(" shocking");
        }
        if ((weapon_flags & WEAPON_POISON) != 0) {
            stat_buf.append(" poison");
        }
        if ((weapon_flags & WEAPON_HOLY) != 0) {
            stat_buf.append(" holy");
        }

        return stat_buf.length() != 0 ? stat_buf.toString() : "none";
    }

    static String cont_bit_name(int cont_flags) {
        stat_buf.setLength(0);

        if ((cont_flags & CONT_CLOSEABLE) != 0) {
            stat_buf.append(" closable");
        }
        if ((cont_flags & CONT_PICKPROOF) != 0) {
            stat_buf.append(" pickproof");
        }
        if ((cont_flags & CONT_CLOSED) != 0) {
            stat_buf.append(" closed");
        }
        if ((cont_flags & CONT_LOCKED) != 0) {
            stat_buf.append(" locked");
        }

        return stat_buf.length() != 0 ? stat_buf.toString() : "none";
    }


    static String off_bit_name(long off_flags) {
        stat_buf.setLength(0);

        if ((off_flags & OFF_AREA_ATTACK) != 0) {
            stat_buf.append(" area attack");
        }
        if ((off_flags & OFF_BACKSTAB) != 0) {
            stat_buf.append(" backstab");
        }
        if ((off_flags & OFF_BASH) != 0) {
            stat_buf.append(" bash");
        }
        if ((off_flags & OFF_BERSERK) != 0) {
            stat_buf.append(" berserk");
        }
        if ((off_flags & OFF_DISARM) != 0) {
            stat_buf.append(" disarm");
        }
        if ((off_flags & OFF_DODGE) != 0) {
            stat_buf.append(" dodge");
        }
        if ((off_flags & OFF_FADE) != 0) {
            stat_buf.append(" fade");
        }
        if ((off_flags & OFF_FAST) != 0) {
            stat_buf.append(" fast");
        }
        if ((off_flags & OFF_KICK) != 0) {
            stat_buf.append(" kick");
        }
        if ((off_flags & OFF_KICK_DIRT) != 0) {
            stat_buf.append(" kick_dirt");
        }
        if ((off_flags & OFF_PARRY) != 0) {
            stat_buf.append(" parry");
        }
        if ((off_flags & OFF_RESCUE) != 0) {
            stat_buf.append(" rescue");
        }
        if ((off_flags & OFF_TAIL) != 0) {
            stat_buf.append(" tail");
        }
        if ((off_flags & OFF_TRIP) != 0) {
            stat_buf.append(" trip");
        }
        if ((off_flags & OFF_CRUSH) != 0) {
            stat_buf.append(" crush");
        }
        if ((off_flags & ASSIST_ALL) != 0) {
            stat_buf.append(" assist_all");
        }
        if ((off_flags & ASSIST_ALIGN) != 0) {
            stat_buf.append(" assist_align");
        }
        if ((off_flags & ASSIST_RACE) != 0) {
            stat_buf.append(" assist_race");
        }
        if ((off_flags & ASSIST_PLAYERS) != 0) {
            stat_buf.append(" assist_players");
        }
        if ((off_flags & ASSIST_GUARD) != 0) {
            stat_buf.append(" assist_guard");
        }
        if ((off_flags & ASSIST_VNUM) != 0) {
            stat_buf.append(" assist_vnum");
        }

        return stat_buf.length() != 0 ? stat_buf.toString() : "none";
    }

    static int cabal_lookup(String argument) {
        int cabal;

        for (cabal = 0; cabal < MAX_CABAL; cabal++) {
            if (!str_prefix(argument, cabal_table[cabal].short_name)) {
                return cabal;
            }
        }

        return -1;
    }


    static int isn_dark_safe(CHAR_DATA ch) {
        CHAR_DATA rch;
        OBJ_DATA light;
        int light_exist;

        if (!IS_VAMPIRE(ch)) {
            return 0;
        }

        if (IS_SET(ch.in_room.room_flags, ROOM_DARK)) {
            return 0;
        }

        if (weather_info.sunlight == SUN_LIGHT
                || weather_info.sunlight == SUN_RISE) {
            return 2;
        }

        light_exist = 0;
        for (rch = ch.in_room.people; rch != null; rch = rch.next_in_room) {
            if ((light = get_light_char(rch)) != null
                    && light.item_type == ITEM_LIGHT
                    && IS_OBJ_STAT(light, ITEM_MAGIC)) {
                light_exist = 1;
                break;
            }
        }

        return light_exist;
    }

/*
* Return types:
* 0: success
* 1: general failure
* 2: no cabal powers
*/

    static int skill_failure_nomessage(CHAR_DATA ch, Skill skill, int npcOffFlag) {
        int i;

        if (IS_NPC(ch) && npcOffFlag != 0 && CABAL_OK(ch, skill) && (RACE_OK(ch, skill)) || IS_SET(ch.off_flags, npcOffFlag)) {
            return 0;
        }

        if (!IS_NPC(ch) && CLEVEL_OK(ch, skill) && RACE_OK(ch, skill) && CABAL_OK(ch, skill) && ALIGN_OK(ch, skill)) {
            if (skill.cabal == CABAL_NONE
                    || cabal_table[ch.cabal].obj_ptr == null
                    || cabal_table[ch.cabal].obj_ptr.in_room == null
                    || cabal_table[ch.cabal].obj_ptr.in_room.vnum ==
                    cabal_table[ch.cabal].room_vnum) {
                return 0;
            }

            for (i = 1; i < MAX_CABAL; i++) {
                if (cabal_table[ch.cabal].obj_ptr.in_room.vnum == cabal_table[i].room_vnum) {
                    return 2;
                }
            }
        }

        return 1;
    }

    static boolean skill_failure_check(CHAR_DATA ch, Skill skill, boolean mount_ok, int npcOffFlag, String msg) {
        if (!mount_ok && MOUNTED(ch) != null) {
            send_to_char("You can't do that while riding!\n", ch);
            return true;
        }

        int r = skill_failure_nomessage(ch, skill, npcOffFlag);
        if (r != 0) {
            if (r == 2) {
                send_to_char("You cannot find the Cabal Power within you.\n", ch);
            } else if (msg != null && msg.length() != 0) {
                send_to_char(msg, ch);
            } else {
                send_to_char("Huh?\n", ch);
            }
            return true;
        }

        return false;
    }

/*
 * Apply or remove an affect to a room.
 */

    static void affect_modify_room(ROOM_INDEX_DATA room, AFFECT_DATA paf, boolean fAdd) {
        int mod;

        mod = paf.modifier;

        if (fAdd) {
            switch (paf.where) {
                case TO_ROOM_AFFECTS:
                    room.affected_by = SET_BIT(room.affected_by, paf.bitvector);
                    break;
                case TO_ROOM_FLAGS:
                    room.room_flags = SET_BIT(room.room_flags, paf.bitvector);
                    break;
                case TO_ROOM_CONST:
                    break;
            }
        } else {
            switch (paf.where) {
                case TO_ROOM_AFFECTS:
                    room.affected_by = REMOVE_BIT(room.affected_by, paf.bitvector);
                    break;
                case TO_ROOM_FLAGS:
                    room.room_flags = REMOVE_BIT(room.room_flags, paf.bitvector);
                    break;
                case TO_ROOM_CONST:
                    break;
            }
            mod = 0 - mod;
        }

        switch (paf.location) {
            default:
                bug("Affect_modify_room: unknown location %d.", paf.location);
                return;

            case APPLY_ROOM_NONE:
                break;
            case APPLY_ROOM_HEAL:
                room.heal_rate += mod;
                break;
            case APPLY_ROOM_MANA:
                room.mana_rate += mod;
                break;
        }

    }

/*
 * Give an affect to a room.
 */

    static void affect_to_room(ROOM_INDEX_DATA room, AFFECT_DATA paf) {
        AFFECT_DATA paf_new;
        ROOM_INDEX_DATA pRoomIndex;

        if (room.affected == null) {
            if (top_affected_room != null) {
                for (pRoomIndex = top_affected_room; pRoomIndex.aff_next != null; pRoomIndex = pRoomIndex.aff_next) {
                }
                pRoomIndex.aff_next = room;
            } else {
                top_affected_room = room;
            }
            room.aff_next = null;
        }

        paf_new = new AFFECT_DATA();

        paf_new.assignValuesFrom(paf);
        paf_new.next = room.affected;
        room.affected = paf_new;

        affect_modify_room(room, paf_new, true);
    }

    static void affect_check_room(ROOM_INDEX_DATA room, int where, long vector) {
        AFFECT_DATA paf;

        if (vector == 0) {
            return;
        }

        for (paf = room.affected; paf != null; paf = paf.next) {
            if (paf.where == where && paf.bitvector == vector) {
                switch (where) {
                    case TO_ROOM_AFFECTS:
                        room.affected_by = SET_BIT(room.affected_by, vector);
                        break;
                    case TO_ROOM_FLAGS:
                        room.room_flags = SET_BIT(room.room_flags, vector);
                        break;
                    case TO_ROOM_CONST:
                        break;
                }
                return;
            }
        }
    }

/*
 * Remove an affect from a room.
 */

    static void affect_remove_room(ROOM_INDEX_DATA room, AFFECT_DATA paf) {
        if (room.affected == null) {
            bug("Affect_remove_room: no affect.");
            return;
        }

        affect_modify_room(room, paf, false);
        int where = paf.where;
        long vector = paf.bitvector;

        if (paf == room.affected) {
            room.affected = paf.next;
        } else {
            AFFECT_DATA prev;

            for (prev = room.affected; prev != null; prev = prev.next) {
                if (prev.next == paf) {
                    prev.next = paf.next;
                    break;
                }
            }

            if (prev == null) {
                bug("Affect_remove_room: cannot find paf.");
                return;
            }
        }

        if (room.affected == null) {
            ROOM_INDEX_DATA prev;

            if (top_affected_room == room) {
                top_affected_room = room.aff_next;
            } else {
                for (prev = top_affected_room; prev.aff_next != null; prev = prev.aff_next) {
                    if (prev.aff_next == room) {
                        prev.aff_next = room.aff_next;
                        break;
                    }
                }
            }
            room.aff_next = null;

        }

        affect_check_room(room, where, vector);
    }

/*
 * Strip all affects of a given sn.
 */

    static void affect_strip_room(ROOM_INDEX_DATA room, Skill sn) {
        AFFECT_DATA paf;
        AFFECT_DATA paf_next;

        for (paf = room.affected; paf != null; paf = paf_next) {
            paf_next = paf.next;
            if (paf.type == sn) {
                affect_remove_room(room, paf);
            }
        }

    }

/*
* Return true if a room is affected by a spell.
*/

    static boolean is_affected_room(ROOM_INDEX_DATA room, Skill sn) {
        AFFECT_DATA paf;

        for (paf = room.affected; paf != null; paf = paf.next) {
            if (paf.type == sn) {
                return true;
            }
        }

        return false;
    }

/*
* Add or enhance an affect.
*/

    static void affect_join_room(ROOM_INDEX_DATA room, AFFECT_DATA paf) {
        AFFECT_DATA paf_old;

        for (paf_old = room.affected; paf_old != null; paf_old = paf_old.next) {
            if (paf_old.type == paf.type) {
                paf.level = ((paf.level += paf_old.level) / 2);
                paf.duration += paf_old.duration;
                paf.modifier += paf_old.modifier;
                affect_remove_room(room, paf_old);
                break;
            }
        }

        affect_to_room(room, paf);
    }

/*
 * Return ascii name of an raffect location.
 */

    static String raffect_loc_name(int location) {
        switch (location) {
            case APPLY_ROOM_NONE:
                return "none";
            case APPLY_ROOM_HEAL:
                return "heal rate";
            case APPLY_ROOM_MANA:
                return "mana rate";
        }
        bug("Affect_location_name: unknown location %d.", location);
        return "(unknown)";
    }

/*
* Return ascii name of an affect bit vector.
*/

    static String raffect_bit_name(long vector) {
        stat_buf.setLength(0);
        if ((vector & AFF_ROOM_SHOCKING) != 0) {
            stat_buf.append(" shocking");
        }
        if ((vector & AFF_ROOM_L_SHIELD) != 0) {
            stat_buf.append(" lightning_shield");
        }
        if ((vector & AFF_ROOM_THIEF_TRAP) != 0) {
            stat_buf.append(" thief_trap");
        }
        if ((vector & AFF_ROOM_CURSE) != 0) {
            stat_buf.append(" curse");
        }
        if ((vector & AFF_ROOM_POISON) != 0) {
            stat_buf.append(" poison");
        }
        if ((vector & AFF_ROOM_PLAGUE) != 0) {
            stat_buf.append(" plague");
        }
        if ((vector & AFF_ROOM_SLEEP) != 0) {
            stat_buf.append(" sleep");
        }
        if ((vector & AFF_ROOM_SLOW) != 0) {
            stat_buf.append(" slow");
        }
        return stat_buf.length() != 0 ? stat_buf.toString() : "none";
    }


    static boolean is_safe_rspell_nom(int level, CHAR_DATA victim) {
        /* ghosts are safe */
        if (!IS_NPC(victim) && IS_SET(victim.act, PLR_GHOST)) {
            return true;
        }

        /* link dead players who do not have rushing adrenalin are safe */
        if (!IS_NPC(victim) && ((victim.last_fight_time == -1) ||
                ((current_time - victim.last_fight_time) > FIGHT_DELAY_TIME)) &&
                victim.desc == null) {
            return true;
        }

        if (victim.level < 5 && !IS_NPC(victim)) {
            return true;
        }

        if (!IS_NPC(victim) &&
                (victim.last_death_time != -1 && current_time - victim.last_death_time < 600)) {
            return true;
        }


        return !IS_NPC(victim) &&
                ((level >= victim.level + 5) || (victim.level >= level + 5));

    }


    static boolean is_safe_rspell(int level, CHAR_DATA victim) {
        if (is_safe_rspell_nom(level, victim)) {
            act("The gods protect $n.", victim, null, null, TO_CHAR);
            act("The gods protect $n from the spell of room.", victim, null, null, TO_ROOM);
            return true;
        } else {
            return false;
        }
    }


    static void raffect_to_char(ROOM_INDEX_DATA room, CHAR_DATA ch) {
        AFFECT_DATA paf;

        if (IS_ROOM_AFFECTED(room, AFF_ROOM_L_SHIELD)) {
            Skill sn = Skill.gsn_lightning_shield;
            CHAR_DATA vch;

            for (vch = room.people; vch != null; vch = vch.next_in_room) {
                if (is_room_owner(vch, room)) {
                    break;
                }
            }

            if (vch == null) {
                bug("Owner of lightning shield left the room.");
                room.owner = "";
                affect_strip_room(room, sn);
            } else {
                send_to_char("The protective shield of room blocks you.\n", ch);
                act("$N has entered the room.", vch, null, ch, TO_CHAR);
                do_wake(vch, "");

                if ((paf = affect_find(room.affected, sn)) == null) {
                    bug("Bad paf for lightning shield");
                    return;
                }

                if (!is_safe_rspell(paf.level, ch)) {

                    if (IS_AFFECTED(ch, AFF_GROUNDING)) {
                        send_to_char("The electricity fizzles at your foes.\n", ch);
                        act("A lightning bolt fizzles at $N's foes.\n", ch, null, ch, TO_ROOM);
                    } else {
                        damage(vch, ch, dice(paf.level, 4) + 12, sn, DAM_LIGHTNING, true);
                        room.owner = "";
                        affect_remove_room(room, paf);
                    }
                }
            }
        }

        if (IS_ROOM_AFFECTED(room, AFF_ROOM_SHOCKING)) {
            Skill sn = Skill.gsn_shocking_trap;
            send_to_char("The shocking waves of room shocks you.\n", ch);

            if ((paf = affect_find(room.affected, sn)) == null) {
                bug("Bad paf for shocking shield");
                return;
            }

            if (!is_safe_rspell(paf.level, ch)) {
                if (check_immune(ch, DAM_LIGHTNING) != IS_IMMUNE) {
                    damage(ch, ch, dice(paf.level, 4) + 12, gsn_x_hunger, DAM_TRAP_ROOM, true);
                }
                {
                    affect_remove_room(room, paf);
                }
            }
        }

        if (IS_ROOM_AFFECTED(room, AFF_ROOM_THIEF_TRAP)) {
            send_to_char("The trap ,set by someone, blocks you.\n", ch);

            if ((paf = affect_find(room.affected, gsn_settraps)) == null) {
                bug("Bad paf for settraps");
                return;
            }

            if (!is_safe_rspell(paf.level, ch)) {
                if (check_immune(ch, DAM_PIERCE) != IS_IMMUNE) {
                    damage(ch, ch, dice(paf.level, 5) + 12, gsn_x_hunger, DAM_TRAP_ROOM, true);
                }
                {
                    affect_remove_room(room, paf);
                }
            }
        }

        if (IS_ROOM_AFFECTED(room, AFF_ROOM_SLOW)
                || IS_ROOM_AFFECTED(room, AFF_ROOM_SLEEP)) {
            send_to_char("{YThere is some mist flowing in the air.{x\n", ch);
        }
    }

    static void raffect_back_char(ROOM_INDEX_DATA room, CHAR_DATA ch) {
        if (IS_ROOM_AFFECTED(room, AFF_ROOM_L_SHIELD)) {
            if (is_room_owner(ch, room)) {
                room.owner = "";
                affect_strip_room(room, Skill.gsn_lightning_shield);
            }
        }
    }

/*
* Return ascii name of an affect bit vector.
*/

    static String flag_room_name(long vector) {
        stat_buf.setLength(0);
        if ((vector & ROOM_DARK) != 0) {
            stat_buf.append(" dark");
        }
        if ((vector & ROOM_NO_MOB) != 0) {
            stat_buf.append(" nomob");
        }
        if ((vector & ROOM_INDOORS) != 0) {
            stat_buf.append(" indoors");
        }
        if ((vector & ROOM_PRIVATE) != 0) {
            stat_buf.append(" private");
        }
        if ((vector & ROOM_SAFE) != 0) {
            stat_buf.append(" safe");
        }
        if ((vector & ROOM_SOLITARY) != 0) {
            stat_buf.append(" solitary");
        }
        if ((vector & ROOM_PET_SHOP) != 0) {
            stat_buf.append(" petshop");
        }
        if ((vector & ROOM_NO_RECALL) != 0) {
            stat_buf.append(" norecall");
        }
        if ((vector & ROOM_IMP_ONLY) != 0) {
            stat_buf.append(" imp_only");
        }
        if ((vector & ROOM_GODS_ONLY) != 0) {
            stat_buf.append(" god_only");
        }
        if ((vector & ROOM_HEROES_ONLY) != 0) {
            stat_buf.append(" heroes");
        }
        if ((vector & ROOM_NEWBIES_ONLY) != 0) {
            stat_buf.append(" newbies");
        }
        if ((vector & ROOM_LAW) != 0) {
            stat_buf.append(" law");
        }
        if ((vector & ROOM_NOWHERE) != 0) {
            stat_buf.append(" nowhere");
        }
        if ((vector & ROOM_BANK) != 0) {
            stat_buf.append(" bank");
        }
        if ((vector & ROOM_NO_MAGIC) != 0) {
            stat_buf.append(" nomagic");
        }
        if ((vector & ROOM_NOSUMMON) != 0) {
            stat_buf.append(" nosummon");
        }
        if ((vector & ROOM_REGISTRY) != 0) {
            stat_buf.append(" registry");
        }
        return stat_buf.length() != 0 ? stat_buf.toString() : "none";
    }


    static boolean affect_check_obj(CHAR_DATA ch, long vector) {
        AFFECT_DATA paf;
        OBJ_DATA obj;

        if (vector == 0) {
            return false;
        }

        for (obj = ch.carrying; obj != null; obj = obj.next_content) {
            if (obj.wear_loc == -1 || obj.wear_loc == WEAR_STUCK_IN) {
                continue;
            }

            for (paf = obj.affected; paf != null; paf = paf.next) {
                if (paf.bitvector == vector) {
                    return true;
                }
            }

            for (paf = obj.pIndexData.affected; paf != null; paf = paf.next) {
                if (paf.bitvector == vector) {
                    return true;
                }
            }
        }
        return false;
    }

    static int count_charmed(CHAR_DATA ch) {
        CHAR_DATA gch;
        int count = 0;

        for (gch = char_list; gch != null; gch = gch.next) {
            if (IS_AFFECTED(gch, AFF_CHARM)
                    && gch.master == ch
                    && ch.pet != gch) {
                count++;
            }
        }

        if (count >= MAX_CHARM(ch)) {
            send_to_char("You are already controlling as many charmed mobs as you can!\n", ch);
            return count;
        }

        return 0;
    }

    static void add_mind(CHAR_DATA ch, String str) {

        if (!IS_NPC(ch) || ch.in_room == null) {
            return;
        }

        if (ch.in_mind == null) {
            ch.in_mind = String.valueOf(ch.in_room.vnum);
        }
        if (!is_name(str, ch.in_mind)) {
            ch.in_mind = ch.in_mind + " " + str;
        }

    }

    static void remove_mind(CHAR_DATA ch, String str) {
        String mind = ch.in_mind;

        if (!IS_NPC(ch) || ch.in_room == null
                || mind == null || !is_name(str, mind)) {
            return;
        }

        StringBuilder arg = new StringBuilder();
        StringBuilder buf = new StringBuilder();
        do {
            arg.setLength(0);
            mind = one_argument(mind, arg);
            if (!is_name(str, arg.toString())) {
                if (buf.length() != 0) {
                    buf.append(" ");
                }
                buf.append(arg);
            }
        }
        while (mind.length() != 0);

        do_say(ch, "At last, I took my revenge!");
        ch.in_mind = buf.toString();
        if (is_number(ch.in_mind)) {
            back_home(ch);
        }
    }

    static int opposite_door(int door) {
        int opdoor;

        switch (door) {
            case 0:
                opdoor = 2;
                break;
            case 1:
                opdoor = 3;
                break;
            case 2:
                opdoor = 0;
                break;
            case 3:
                opdoor = 1;
                break;
            case 4:
                opdoor = 5;
                break;
            case 5:
                opdoor = 4;
                break;
            default:
                opdoor = -1;
                break;
        }
        return opdoor;
    }

    static void back_home(CHAR_DATA ch) {
        ROOM_INDEX_DATA location;

        if (!IS_NPC(ch) || ch.in_mind == null) {
            return;
        }

        StringBuilder arg = new StringBuilder();
        one_argument(ch.in_mind, arg);
        if ((location = find_location(ch, arg.toString())) == null) {
            bug("Mob cannot return to reset place", 0);
            return;
        }

        if (ch.fighting == null && location != ch.in_room) {
            act("$n prays for transportation.", ch, null, null, TO_ROOM);
            char_from_room(ch);
            char_to_room(ch, location);
            act("$n appears in the room.", ch, null, null, TO_ROOM);
            if (is_number(ch.in_mind)) {
                ch.in_mind = null;
            }
        }
    }

    private static int _n[] = new int[1];

    static CHAR_DATA find_char(CHAR_DATA ch, String argument, int door, int range) {
        EXIT_DATA pExit, bExit;
        ROOM_INDEX_DATA dest_room, back_room;
        CHAR_DATA target;
        int opdoor;

        StringBuilder arg = new StringBuilder();
        _n[0] = number_argument(argument, arg);
        dest_room = ch.in_room;
        if ((target = get_char_room2(ch, dest_room, arg.toString(), _n)) != null) {
            return target;
        }
        if ((opdoor = opposite_door(door)) == -1) {
            bug("In find_char wrong door: %d", door);
            send_to_char("You don't see that there.\n", ch);
            return null;
        }
        if (range > 0) {
            //todo: range--;
            /* find target room */
            back_room = dest_room;
            pExit = dest_room.exit[door];
            if (pExit != null && (dest_room = pExit.to_room) != null && !IS_SET(pExit.exit_info, EX_CLOSED)) {
                if ((bExit = dest_room.exit[opdoor]) == null || bExit.to_room != back_room) {
                    send_to_char("The path you choose prevents your power to pass.\n", ch);
                    return null;
                }
                if ((target = get_char_room2(ch, dest_room, arg.toString(), _n)) != null) {
                    return target;
                }
            }
        }
        send_to_char("You don't see that there.\n", ch);
        return null;
    }

    static int check_exit(String arg) {
        int door = -1;

        if (!str_cmp(arg, "n") || !str_cmp(arg, "north")) {
            door = 0;
        } else if (!str_cmp(arg, "e") || !str_cmp(arg, "east")) {
            door = 1;
        } else if (!str_cmp(arg, "s") || !str_cmp(arg, "south")) {
            door = 2;
        } else if (!str_cmp(arg, "w") || !str_cmp(arg, "west")) {
            door = 3;
        } else if (!str_cmp(arg, "u") || !str_cmp(arg, "up")) {
            door = 4;
        } else if (!str_cmp(arg, "d") || !str_cmp(arg, "down")) {
            door = 5;
        }

        return door;
    }

/*
 * Find a char for spell usage.
 */

    static CHAR_DATA get_char_spell(CHAR_DATA ch, String argument, int[] door, int range) {
        int i = argument.indexOf('.');
        String buf = i >= 0 ? argument.substring(0, i) : argument;
        if (i == 0 || (door[0] = check_exit(buf)) == -1) {
            return get_char_room(ch, argument);
        }

        return find_char(ch, argument.substring(i + 1), door[0], range);
    }

    static void path_to_track(CHAR_DATA ch, CHAR_DATA victim, int door) {
        ROOM_INDEX_DATA temp;
        EXIT_DATA pExit;
        int opdoor;
        int range = 0;
        int i;

        ch.last_fight_time = current_time;
        if (!IS_NPC(victim)) {
            victim.last_fight_time = current_time;
        }

        if (IS_NPC(victim) && victim.position != POS_DEAD) {
            victim.last_fought = ch;

            if ((opdoor = opposite_door(door)) == -1) {
                bug("In path_to_track wrong door: %d", door);
                return;
            }
            temp = ch.in_room;
            for (i = 0; i < 1000; i++) {
                range++;
                if (victim.in_room == temp) {
                    break;
                }
                if ((pExit = temp.exit[door]) == null
                        || (temp = pExit.to_room) == null) {
                    bug("In path_to_track: couldn't calculate range %d", range);
                    return;
                }
                if (range > 100) {
                    bug("In path_to_track: range exceeded 100");
                    return;
                }
            }

            temp = victim.in_room;
            while (--range > 0) {
                room_record(ch.name, temp, opdoor);
                if ((pExit = temp.exit[opdoor]) == null
                        || (temp = pExit.to_room) == null) {
                    bug("Path to track: Range: " + range + " Room: " + (temp == null ? null : temp.vnum) + " opdoor:" + opdoor);
                    return;
                }
            }
            do_track(victim, "");
        }
    }

/* new staff */

    static OBJ_DATA get_wield_char(CHAR_DATA ch, boolean second) {
        OBJ_DATA obj;

        if (ch == null) {
            return null;
        }

        for (obj = ch.carrying; obj != null; obj = obj.next_content) {
            if (obj.item_type == ITEM_WEAPON) {
                if (second) {
                    if ((obj.wear_loc == WEAR_RIGHT && LEFT_HANDER(ch))
                            || (obj.wear_loc == WEAR_LEFT && RIGHT_HANDER(ch))) {
                        return obj;
                    }
                } else {
                    if ((obj.wear_loc == WEAR_RIGHT && RIGHT_HANDER(ch))
                            || (obj.wear_loc == WEAR_LEFT && LEFT_HANDER(ch))
                            || obj.wear_loc == WEAR_BOTH) {
                        return obj;
                    }
                }
            }
        }

        return null;
    }


    static OBJ_DATA get_shield_char(CHAR_DATA ch) {
        OBJ_DATA obj;

        if (ch == null) {
            return null;
        }

        for (obj = ch.carrying; obj != null; obj = obj.next_content) {
            if ((obj.wear_loc == WEAR_LEFT || obj.wear_loc == WEAR_RIGHT ||
                    obj.wear_loc == WEAR_BOTH) && CAN_WEAR(obj, ITEM_WEAR_SHIELD)) {
                return obj;
            }
        }

        return null;
    }


    static OBJ_DATA get_hold_char(CHAR_DATA ch) {
        OBJ_DATA obj;

        if (ch == null) {
            return null;
        }

        for (obj = ch.carrying; obj != null; obj = obj.next_content) {
            if ((obj.wear_loc == WEAR_LEFT || obj.wear_loc == WEAR_RIGHT ||
                    obj.wear_loc == WEAR_BOTH) && CAN_WEAR(obj, ITEM_HOLD)) {
                return obj;
            }
        }

        return null;
    }


    static OBJ_DATA get_light_char(CHAR_DATA ch) {
        OBJ_DATA obj;

        if (ch == null) {
            return null;
        }

        for (obj = ch.carrying; obj != null; obj = obj.next_content) {
            if ((obj.item_type == ITEM_LIGHT
                    && obj.value[2] != 0
                    && (obj.wear_loc == WEAR_LEFT
                    || obj.wear_loc == WEAR_RIGHT
                    || obj.wear_loc == WEAR_BOTH))
                    || (obj.wear_loc == WEAR_HEAD
                    && IS_OBJ_STAT(obj, ITEM_GLOW)))

            {
                return obj;
            }
        }

        return null;
    }


    static boolean is_wielded_char(CHAR_DATA ch, OBJ_DATA obj) {
        OBJ_DATA w;

        if (ch == null) {
            return false;
        }

        for (w = ch.carrying; w != null; w = w.next_content) {
            if ((w.wear_loc == WEAR_LEFT || w.wear_loc == WEAR_RIGHT ||
                    w.wear_loc == WEAR_BOTH) && CAN_WEAR(w, ITEM_WIELD)
                    && w == obj) {
                return true;
            }
        }

        return false;
    }


    static boolean is_equiped_n_char(CHAR_DATA ch, int vnum, int iWear) {
        OBJ_DATA e;

        if (ch == null) {
            return false;
        }

        for (e = ch.carrying; e != null; e = e.next_content) {
            if (e.wear_loc == iWear && e.pIndexData.vnum == vnum) {
                return true;
            }
        }

        return false;
    }


    static boolean is_equiped_char(CHAR_DATA ch, OBJ_DATA obj, int iWear) {
        OBJ_DATA e;

        if (ch == null) {
            return false;
        }

        for (e = ch.carrying; e != null; e = e.next_content) {
            if (e.wear_loc == iWear && e == obj) {
                return true;
            }
        }

        return false;
    }


    static int count_worn(CHAR_DATA ch, int iWear) {
        OBJ_DATA obj;
        int count;

        if (ch == null) {
            return 0;
        }

        count = 0;
        for (obj = ch.carrying; obj != null; obj = obj.next_content) {
            if (obj.wear_loc == iWear) {
                count++;
            }
        }

        return count;
    }


    static int max_can_wear(CHAR_DATA ch, int i) {
        if (IS_NPC(ch) || !IS_SET(ch.act, PLR_REMORTED)) {
            return (i == WEAR_FINGER ? MAX_FINGER :
                    i == WEAR_STUCK_IN ? MAX_STUCK_IN :
                            i == WEAR_WRIST ? MAX_WRIST :
                                    i == WEAR_TATTOO ? MAX_TATTOO :
                                            i == WEAR_NECK ? MAX_NECK : 1);
        } else {
            return (i == WEAR_FINGER ? (MAX_FINGER + 2) :
                    i == WEAR_STUCK_IN ? MAX_STUCK_IN :
                            i == WEAR_WRIST ? MAX_WRIST :
                                    i == WEAR_TATTOO ? MAX_TATTOO :
                                            i == WEAR_NECK ? MAX_NECK : 1);
        }
    }

    static int get_played_day(int t) {
        return (currentTimeSeconds() - t) / (60 * 24);

    }

    static int get_played_time(CHAR_DATA ch, int l) {
        long ref_time;
        int played;

        if (IS_NPC(ch)) {
            return 0;
        }
        if (l != 0) {
            return ch.pcdata.log_time[l];
        }

        /* fix if it is passed midnight */
        ref_time = (ch.logon > limit_time) ? ch.logon : limit_time;
        played = ch.pcdata.log_time[0] + (int) ((current_time - ref_time) / 60);

        return played;
    }

    static int get_total_played(CHAR_DATA ch) {
        int l, sum = 0;

        if (IS_NPC(ch)) {
            return 0;
        }

        /* now calculate the rest */
        for (l = 0; l < nw_config.max_time_log; l++) {
            sum += get_played_time(ch, l);
        }

        return sum;
    }

    static boolean check_time_sync() {
        /*TODO: struct tm *am_time;
        int now_day, lim_day;

        am_time = localtime( &current_time );
        now_day = am_time.tm_mday;
        am_time = localtime( &limit_time );
        lim_day = am_time.tm_mday;

        if ( now_day == lim_day )
          return false;
        else
          return true;*/
        return false;
    }

}
