package net.sf.nightworks;

import static net.sf.nightworks.Comm.act;
import static net.sf.nightworks.Comm.send_to_char;
import static net.sf.nightworks.DB.number_percent;
import static net.sf.nightworks.DB.number_range;
import static net.sf.nightworks.Handler.affect_enchant;
import static net.sf.nightworks.Handler.affect_join;
import static net.sf.nightworks.Handler.affect_to_char;
import static net.sf.nightworks.Handler.check_material;
import static net.sf.nightworks.Handler.extract_obj;
import static net.sf.nightworks.Handler.obj_from_obj;
import static net.sf.nightworks.Handler.obj_to_room;
import static net.sf.nightworks.Magic.saves_spell;
import static net.sf.nightworks.Nightworks.AFFECT_DATA;
import static net.sf.nightworks.Nightworks.AFF_BLIND;
import static net.sf.nightworks.Nightworks.AFF_POISON;
import static net.sf.nightworks.Nightworks.AFF_SCREAM;
import static net.sf.nightworks.Nightworks.APPLY_AC;
import static net.sf.nightworks.Nightworks.APPLY_HITROLL;
import static net.sf.nightworks.Nightworks.APPLY_NONE;
import static net.sf.nightworks.Nightworks.APPLY_STR;
import static net.sf.nightworks.Nightworks.CHAR_DATA;
import static net.sf.nightworks.Nightworks.COND_HUNGER;
import static net.sf.nightworks.Nightworks.COND_THIRST;
import static net.sf.nightworks.Nightworks.DAM_COLD;
import static net.sf.nightworks.Nightworks.DAM_FIRE;
import static net.sf.nightworks.Nightworks.DAM_LIGHTNING;
import static net.sf.nightworks.Nightworks.DAM_POISON;
import static net.sf.nightworks.Nightworks.DAM_SOUND;
import static net.sf.nightworks.Nightworks.DAZE_STATE;
import static net.sf.nightworks.Nightworks.IS_AFFECTED;
import static net.sf.nightworks.Nightworks.IS_NPC;
import static net.sf.nightworks.Nightworks.IS_OBJ_STAT;
import static net.sf.nightworks.Nightworks.ITEM_ARMOR;
import static net.sf.nightworks.Nightworks.ITEM_BLESS;
import static net.sf.nightworks.Nightworks.ITEM_BURN_PROOF;
import static net.sf.nightworks.Nightworks.ITEM_CLOTHING;
import static net.sf.nightworks.Nightworks.ITEM_CONTAINER;
import static net.sf.nightworks.Nightworks.ITEM_CORPSE_NPC;
import static net.sf.nightworks.Nightworks.ITEM_CORPSE_PC;
import static net.sf.nightworks.Nightworks.ITEM_DRINK_CON;
import static net.sf.nightworks.Nightworks.ITEM_FOOD;
import static net.sf.nightworks.Nightworks.ITEM_JEWELRY;
import static net.sf.nightworks.Nightworks.ITEM_NOPURGE;
import static net.sf.nightworks.Nightworks.ITEM_PILL;
import static net.sf.nightworks.Nightworks.ITEM_POTION;
import static net.sf.nightworks.Nightworks.ITEM_SCROLL;
import static net.sf.nightworks.Nightworks.ITEM_STAFF;
import static net.sf.nightworks.Nightworks.ITEM_WAND;
import static net.sf.nightworks.Nightworks.OBJ_DATA;
import static net.sf.nightworks.Nightworks.ROOM_INDEX_DATA;
import static net.sf.nightworks.Nightworks.TARGET_CHAR;
import static net.sf.nightworks.Nightworks.TARGET_OBJ;
import static net.sf.nightworks.Nightworks.TARGET_ROOM;
import static net.sf.nightworks.Nightworks.TO_AFFECTS;
import static net.sf.nightworks.Nightworks.TO_ALL;
import static net.sf.nightworks.Nightworks.TO_CHAR;
import static net.sf.nightworks.Nightworks.TO_ROOM;
import static net.sf.nightworks.Nightworks.UMAX;
import static net.sf.nightworks.Nightworks.URANGE;
import static net.sf.nightworks.Nightworks.WEAR_NONE;
import static net.sf.nightworks.Skill.gsn_poison;
import static net.sf.nightworks.Skill.gsn_scream;
import static net.sf.nightworks.Update.gain_condition;

class Effects {
    static void acid_effect(Object vo, int level, int dam, int target) {
        if (target == TARGET_ROOM) /* nail objects on the floor */ {
            var room = (ROOM_INDEX_DATA) vo;
            OBJ_DATA obj, obj_next;

            for (obj = room.contents; obj != null; obj = obj_next) {
                obj_next = obj.next_content;
                acid_effect(obj, level, dam, TARGET_OBJ);
            }
            return;
        }

        if (target == TARGET_CHAR)  /* do the effect on a victim */ {
            var victim = (CHAR_DATA) vo;
            OBJ_DATA obj, obj_next;

            /* let's toast some gear */
            for (obj = victim.carrying; obj != null; obj = obj_next) {
                obj_next = obj.next_content;
                acid_effect(obj, level, dam, TARGET_OBJ);
            }
            return;
        }

        if (target == TARGET_OBJ) /* toast an object */ {
            var obj = (OBJ_DATA) vo;
            OBJ_DATA t_obj, n_obj;
            int chance;

            if (IS_OBJ_STAT(obj, ITEM_BURN_PROOF)
                    || IS_OBJ_STAT(obj, ITEM_NOPURGE)
                    || obj.pIndexData.limit != -1
                    || number_range(0, 4) == 0) {
                return;
            }

            chance = level / 4 + dam / 10;

            if (chance > 25) {
                chance = (chance - 25) / 2 + 25;
            }
            if (chance > 50) {
                chance = (chance - 50) / 2 + 50;
            }

            if (IS_OBJ_STAT(obj, ITEM_BLESS)) {
                chance -= 5;
            }

            chance -= obj.level * 2;

            String msg;
            switch (obj.item_type) {
                default -> {
                    return;
                }
                case ITEM_CONTAINER, ITEM_CORPSE_PC, ITEM_CORPSE_NPC -> msg = "$p fumes and dissolves.";
                case ITEM_ARMOR -> msg = "$p is pitted and etched.";
                case ITEM_CLOTHING -> msg = "$p is corroded into scrap.";
                case ITEM_STAFF, ITEM_WAND -> {
                    chance -= 10;
                    msg = "$p corrodes and breaks.";
                }
                case ITEM_SCROLL -> {
                    chance += 10;
                    msg = "$p is burned into waste.";
                }
            }

            chance = URANGE(5, chance, 95);

            if (number_percent() > chance) {
                return;
            }

            if (obj.carried_by != null) {
                act(msg, obj.carried_by, obj, null, TO_ALL);
            } else if (obj.in_room != null && obj.in_room.people != null) {
                act(msg, obj.in_room.people, obj, null, TO_ALL);
            }

            if (obj.item_type == ITEM_ARMOR)  /* etch it */ {
                AFFECT_DATA paf;
                var af_found = false;
                int i;

                affect_enchant(obj);

                for (paf = obj.affected; paf != null; paf = paf.next) {
                    if (paf.location == APPLY_AC) {
                        af_found = true;
                        paf.type = null;
                        paf.modifier += 1;
                        paf.level = UMAX(paf.level, level);
                        break;
                    }
                }

                if (!af_found)
                    /* needs a new affect */ {
                    paf = new AFFECT_DATA();

                    paf.type = null;
                    paf.level = level;
                    paf.duration = -1;
                    paf.location = APPLY_AC;
                    paf.modifier = 1;
                    paf.bitvector = 0;
                    paf.next = obj.affected;
                    obj.affected = paf;
                }

                if (obj.carried_by != null && obj.wear_loc != WEAR_NONE) {
                    for (i = 0; i < 4; i++) {
                        obj.carried_by.armor[i] += 1;
                    }
                }
                return;
            }

            /* get rid of the object */
            if (obj.contains != null)  /* dump contents */ {
                for (t_obj = obj.contains; t_obj != null; t_obj = n_obj) {
                    n_obj = t_obj.next_content;
                    obj_from_obj(t_obj);
                    if (obj.in_room != null) {
                        obj_to_room(t_obj, obj.in_room);
                    } else if (obj.carried_by != null) {
                        obj_to_room(t_obj, obj.carried_by.in_room);
                    } else {
                        extract_obj(t_obj);
                        continue;
                    }

                    acid_effect(t_obj, level / 2, dam / 2, TARGET_OBJ);
                }
            }

            extract_obj(obj);
        }
    }


    static void cold_effect(Object vo, int level, int dam, int target) {
        if (target == TARGET_ROOM) /* nail objects on the floor */ {
            var room = (ROOM_INDEX_DATA) vo;
            OBJ_DATA obj, obj_next;

            for (obj = room.contents; obj != null; obj = obj_next) {
                obj_next = obj.next_content;
                cold_effect(obj, level, dam, TARGET_OBJ);
            }
            return;
        }

        if (target == TARGET_CHAR) /* whack a character */ {
            var victim = (CHAR_DATA) vo;
            OBJ_DATA obj, obj_next;

            /* chill touch effect */
            if (!saves_spell(level / 4 + dam / 20, victim, DAM_COLD)) {
                var af = new AFFECT_DATA();

                act("$n turns blue and shivers.", victim, null, null, TO_ROOM);
                act("A chill sinks deep into your bones.", victim, null, null, TO_CHAR);
                af.where = TO_AFFECTS;
                af.type = Skill.gsn_chill_touch;
                af.level = level;
                af.duration = 6;
                af.location = APPLY_STR;
                af.modifier = -1;
                af.bitvector = 0;
                affect_join(victim, af);
            }

            /* hunger! (warmth sucked out */
            if (!IS_NPC(victim)) {
                gain_condition(victim, COND_HUNGER, dam / 20);
            }

            /* let's toast some gear */
            for (obj = victim.carrying; obj != null; obj = obj_next) {
                obj_next = obj.next_content;
                cold_effect(obj, level, dam, TARGET_OBJ);
            }
            return;
        }

        if (target == TARGET_OBJ) /* toast an object */ {
            var obj = (OBJ_DATA) vo;
            int chance;

            if (IS_OBJ_STAT(obj, ITEM_BURN_PROOF)
                    || IS_OBJ_STAT(obj, ITEM_NOPURGE)
                    || obj.pIndexData.limit != -1
                    || number_range(0, 4) == 0) {
                return;
            }

            chance = level / 4 + dam / 10;

            if (chance > 25) {
                chance = (chance - 25) / 2 + 25;
            }
            if (chance > 50) {
                chance = (chance - 50) / 2 + 50;
            }

            if (IS_OBJ_STAT(obj, ITEM_BLESS)) {
                chance -= 5;
            }

            chance -= obj.level * 2;

            String msg;
            switch (obj.item_type) {
                default -> {
                    return;
                }
                case ITEM_POTION -> {
                    msg = "$p freezes and shatters!";
                    chance += 25;
                }
                case ITEM_DRINK_CON -> {
                    msg = "$p freezes and shatters!";
                    chance += 5;
                }
            }

            chance = URANGE(5, chance, 95);

            if (number_percent() > chance) {
                return;
            }

            if (obj.carried_by != null) {
                act(msg, obj.carried_by, obj, null, TO_ALL);
            } else if (obj.in_room != null && obj.in_room.people != null) {
                act(msg, obj.in_room.people, obj, null, TO_ALL);
            }

            extract_obj(obj);
        }
    }


    static void fire_effect(Object vo, int level, int dam, int target) {

        if (target == TARGET_ROOM)  /* nail objects on the floor */ {
            var room = (ROOM_INDEX_DATA) vo;
            OBJ_DATA obj, obj_next;
            for (obj = room.contents; obj != null; obj = obj_next) {
                obj_next = obj.next_content;
                fire_effect(obj, level, dam, TARGET_OBJ);
            }
            return;
        }

        if (target == TARGET_CHAR)   /* do the effect on a victim */ {
            var victim = (CHAR_DATA) vo;
            OBJ_DATA obj, obj_next;

            /* chance of blindness */
            if (!IS_AFFECTED(victim, AFF_BLIND)
                    && !saves_spell(level / 4 + dam / 20, victim, DAM_FIRE)) {
                var af = new AFFECT_DATA();
                act("$n is blinded by smoke!", victim, null, null, TO_ROOM);
                act("Your eyes tear up from smoke...you can't see a thing!",
                        victim, null, null, TO_CHAR);

                af.where = TO_AFFECTS;
                af.type = Skill.gsn_fire_breath;
                af.level = level;
                af.duration = number_range(0, level / 10);
                af.location = APPLY_HITROLL;
                af.modifier = -4;
                af.bitvector = AFF_BLIND;

                affect_to_char(victim, af);
            }

            /* getting thirsty */
            if (!IS_NPC(victim)) {
                gain_condition(victim, COND_THIRST, dam / 20);
            }

            /* let's toast some gear! */
            for (obj = victim.carrying; obj != null; obj = obj_next) {
                obj_next = obj.next_content;

                fire_effect(obj, level, dam, TARGET_OBJ);
            }
            return;
        }

        if (target == TARGET_OBJ)  /* toast an object */ {
            var obj = (OBJ_DATA) vo;
            OBJ_DATA t_obj, n_obj;
            int chance;

            if (IS_OBJ_STAT(obj, ITEM_BURN_PROOF)
                    || IS_OBJ_STAT(obj, ITEM_NOPURGE)
                    || obj.pIndexData.limit != -1
                    || number_range(0, 4) == 0) {
                return;
            }

            chance = level / 4 + dam / 10;

            if (chance > 25) {
                chance = (chance - 25) / 2 + 25;
            }
            if (chance > 50) {
                chance = (chance - 50) / 2 + 50;
            }

            if (IS_OBJ_STAT(obj, ITEM_BLESS)) {
                chance -= 5;
            }
            chance -= obj.level * 2;

            String msg;
            if (check_material(obj, "ice")) {
                chance += 30;
                msg = "$p melts and evaporates!";
            } else {
                switch (obj.item_type) {
                    default -> {
                        return;
                    }
                    case ITEM_CONTAINER -> msg = "$p ignites and burns!";
                    case ITEM_POTION -> {
                        chance += 25;
                        msg = "$p bubbles and boils!";
                    }
                    case ITEM_SCROLL -> {
                        chance += 50;
                        msg = "$p crackles and burns!";
                    }
                    case ITEM_STAFF -> {
                        chance += 10;
                        msg = "$p smokes and chars!";
                    }
                    case ITEM_WAND -> msg = "$p sparks and sputters!";
                    case ITEM_FOOD -> msg = "$p blackens and crisps!";
                    case ITEM_PILL -> msg = "$p melts and drips!";
                }
            }

            chance = URANGE(5, chance, 95);

            if (number_percent() > chance) {
                return;
            }

            if (obj.carried_by != null) {
                act(msg, obj.carried_by, obj, null, TO_ALL);
            } else if (obj.in_room != null && obj.in_room.people != null) {
                act(msg, obj.in_room.people, obj, null, TO_ALL);
            }

            if (obj.contains != null) {
                /* dump the contents */

                for (t_obj = obj.contains; t_obj != null; t_obj = n_obj) {
                    n_obj = t_obj.next_content;
                    obj_from_obj(t_obj);
                    if (obj.in_room != null) {
                        obj_to_room(t_obj, obj.in_room);
                    } else if (obj.carried_by != null) {
                        obj_to_room(t_obj, obj.carried_by.in_room);
                    } else {
                        extract_obj(t_obj);
                        continue;
                    }
                    fire_effect(t_obj, level / 2, dam / 2, TARGET_OBJ);
                }
            }

            extract_obj(obj);
        }
    }

    static void poison_effect(Object vo, int level, int dam, int target) {
        if (target == TARGET_ROOM)  /* nail objects on the floor */ {
            var room = (ROOM_INDEX_DATA) vo;
            OBJ_DATA obj, obj_next;

            for (obj = room.contents; obj != null; obj = obj_next) {
                obj_next = obj.next_content;
                poison_effect(obj, level, dam, TARGET_OBJ);
            }
            return;
        }

        if (target == TARGET_CHAR)   /* do the effect on a victim */ {
            var victim = (CHAR_DATA) vo;
            OBJ_DATA obj, obj_next;

            /* chance of poisoning */
            if (!saves_spell(level / 4 + dam / 20, victim, DAM_POISON)) {
                var af = new AFFECT_DATA();

                send_to_char("You feel poison coursing through your veins.\n", victim);
                act("$n looks very ill.", victim, null, null, TO_ROOM);

                af.where = TO_AFFECTS;
                af.type = gsn_poison;
                af.level = level;
                af.duration = level / 2;
                af.location = APPLY_STR;
                af.modifier = -1;
                af.bitvector = AFF_POISON;
                affect_join(victim, af);
            }

            /* equipment */
            for (obj = victim.carrying; obj != null; obj = obj_next) {
                obj_next = obj.next_content;
                poison_effect(obj, level, dam, TARGET_OBJ);
            }
            return;
        }

        if (target == TARGET_OBJ)  /* do some poisoning */ {
            var obj = (OBJ_DATA) vo;
            int chance;


            if (IS_OBJ_STAT(obj, ITEM_BURN_PROOF)
                    || IS_OBJ_STAT(obj, ITEM_BLESS)
                    || obj.pIndexData.limit != -1
                    || number_range(0, 4) == 0) {
                return;
            }

            chance = level / 4 + dam / 10;
            if (chance > 25) {
                chance = (chance - 25) / 2 + 25;
            }
            if (chance > 50) {
                chance = (chance - 50) / 2 + 50;
            }

            chance -= obj.level * 2;

            switch (obj.item_type) {
                default -> {
                    return;
                }
                case ITEM_FOOD -> {
                }
                case ITEM_DRINK_CON -> {
                    if (obj.value[0] == obj.value[1]) {
                        return;
                    }
                }
            }

            chance = URANGE(5, chance, 95);

            if (number_percent() > chance) {
                return;
            }

            obj.value[3] = 1;
        }
    }


    static void shock_effect(Object vo, int level, int dam, int target) {
        if (target == TARGET_ROOM) {
            var room = (ROOM_INDEX_DATA) vo;
            OBJ_DATA obj, obj_next;

            for (obj = room.contents; obj != null; obj = obj_next) {
                obj_next = obj.next_content;
                shock_effect(obj, level, dam, TARGET_OBJ);
            }
            return;
        }

        if (target == TARGET_CHAR) {
            var victim = (CHAR_DATA) vo;
            OBJ_DATA obj, obj_next;

            /* daze and confused? */
            if (!saves_spell(level / 4 + dam / 20, victim, DAM_LIGHTNING)) {
                send_to_char("Your muscles stop responding.\n", victim);
                DAZE_STATE(victim, UMAX(12, level / 4 + dam / 20));
            }

            /* toast some gear */
            for (obj = victim.carrying; obj != null; obj = obj_next) {
                obj_next = obj.next_content;
                shock_effect(obj, level, dam, TARGET_OBJ);
            }
            return;
        }

        if (target == TARGET_OBJ) {
            var obj = (OBJ_DATA) vo;
            int chance;

            if (IS_OBJ_STAT(obj, ITEM_BURN_PROOF)
                    || IS_OBJ_STAT(obj, ITEM_NOPURGE)
                    || obj.pIndexData.limit != -1
                    || number_range(0, 4) == 0) {
                return;
            }

            chance = level / 4 + dam / 10;

            if (chance > 25) {
                chance = (chance - 25) / 2 + 25;
            }
            if (chance > 50) {
                chance = (chance - 50) / 2 + 50;
            }

            if (IS_OBJ_STAT(obj, ITEM_BLESS)) {
                chance -= 5;
            }

            chance -= obj.level * 2;

            String msg;
            switch (obj.item_type) {
                default -> {
                    return;
                }
                case ITEM_WAND, ITEM_STAFF -> {
                    chance += 10;
                    msg = "$p overloads and explodes!";
                }
                case ITEM_JEWELRY -> {
                    chance -= 10;
                    msg = "$p is fused into a worthless lump.";
                }
            }

            chance = URANGE(5, chance, 95);

            if (number_percent() > chance) {
                return;
            }

            if (obj.carried_by != null) {
                act(msg, obj.carried_by, obj, null, TO_ALL);
            } else if (obj.in_room != null && obj.in_room.people != null) {
                act(msg, obj.in_room.people, obj, null, TO_ALL);
            }

            extract_obj(obj);
        }
    }

    static void sand_effect(Object vo, int level, int dam, int target) {
        if (target == TARGET_ROOM) /* nail objects on the floor */ {
            var room = (ROOM_INDEX_DATA) vo;
            OBJ_DATA obj, obj_next;

            for (obj = room.contents; obj != null; obj = obj_next) {
                obj_next = obj.next_content;
                sand_effect(obj, level, dam, TARGET_OBJ);
            }
            return;
        }

        if (target == TARGET_CHAR)  /* do the effect on a victim */ {
            var victim = (CHAR_DATA) vo;
            OBJ_DATA obj, obj_next;

            if (!IS_AFFECTED(victim, AFF_BLIND)
                    && !saves_spell(level / 4 + dam / 20, victim, DAM_COLD)) {
                var af = new AFFECT_DATA();
                act("$n is blinded by flying sands!", victim, null, null, TO_ROOM);
                act("Your eyes tear up from sands...you can't see a thing!",
                        victim, null, null, TO_CHAR);

                af.where = TO_AFFECTS;
                af.type = Skill.gsn_sand_storm;
                af.level = level;
                af.duration = number_range(0, level / 10);
                af.location = APPLY_HITROLL;
                af.modifier = -4;
                af.bitvector = AFF_BLIND;

                affect_to_char(victim, af);
            }

            /* let's toast some gear */
            for (obj = victim.carrying; obj != null; obj = obj_next) {
                obj_next = obj.next_content;
                sand_effect(obj, level, dam, TARGET_OBJ);
            }
            return;
        }

        if (target == TARGET_OBJ) /* toast an object */ {
            var obj = (OBJ_DATA) vo;
            OBJ_DATA t_obj, n_obj;
            int chance;

            if (IS_OBJ_STAT(obj, ITEM_BURN_PROOF)
                    || IS_OBJ_STAT(obj, ITEM_NOPURGE)
                    || obj.pIndexData.limit != -1
                    || number_range(0, 4) == 0) {
                return;
            }

            chance = level / 4 + dam / 10;

            if (chance > 25) {
                chance = (chance - 25) / 2 + 25;
            }
            if (chance > 50) {
                chance = (chance - 50) / 2 + 50;
            }

            if (IS_OBJ_STAT(obj, ITEM_BLESS)) {
                chance -= 5;
            }

            chance -= obj.level * 2;

            String msg;
            switch (obj.item_type) {
                default -> {
                    return;
                }
                case ITEM_CONTAINER, ITEM_CORPSE_PC, ITEM_CORPSE_NPC -> {
                    chance += 50;
                    msg = "$p is filled with sand and evaporates.";
                }
                case ITEM_ARMOR -> {
                    chance -= 10;
                    msg = "$p is etched by sand";
                }
                case ITEM_CLOTHING -> msg = "$p is corroded by sands.";
                case ITEM_WAND -> {
                    chance = 50;
                    msg = "$p mixes with crashing sands.";
                }
                case ITEM_SCROLL -> {
                    chance += 20;
                    msg = "$p is surrouned by sand.";
                }
                case ITEM_POTION -> {
                    chance += 10;
                    msg = "$p is broken into peace by crashing sands.";
                }
            }

            chance = URANGE(5, chance, 95);

            if (number_percent() > chance) {
                return;
            }

            if (obj.carried_by != null) {
                act(msg, obj.carried_by, obj, null, TO_ALL);
            } else if (obj.in_room != null && obj.in_room.people != null) {
                act(msg, obj.in_room.people, obj, null, TO_ALL);
            }

            if (obj.item_type == ITEM_ARMOR)  /* etch it */ {
                AFFECT_DATA paf;
                var af_found = false;
                int i;

                affect_enchant(obj);

                for (paf = obj.affected; paf != null; paf = paf.next) {
                    if (paf.location == APPLY_AC) {
                        af_found = true;
                        paf.type = null;
                        paf.modifier += 1;
                        paf.level = UMAX(paf.level, level);
                        break;
                    }
                }

                if (!af_found)
                    /* needs a new affect */ {
                    paf = new AFFECT_DATA();

                    paf.type = null;
                    paf.level = level;
                    paf.duration = level;
                    paf.location = APPLY_AC;
                    paf.modifier = 1;
                    paf.bitvector = 0;
                    paf.next = obj.affected;
                    obj.affected = paf;
                }

                if (obj.carried_by != null && obj.wear_loc != WEAR_NONE) {
                    for (i = 0; i < 4; i++) {
                        obj.carried_by.armor[i] += 1;
                    }
                }
                return;
            }

            /* get rid of the object */
            if (obj.contains != null)  /* dump contents */ {
                for (t_obj = obj.contains; t_obj != null; t_obj = n_obj) {
                    n_obj = t_obj.next_content;
                    obj_from_obj(t_obj);
                    if (obj.in_room != null) {
                        obj_to_room(t_obj, obj.in_room);
                    } else if (obj.carried_by != null) {
                        obj_to_room(t_obj, obj.carried_by.in_room);
                    } else {
                        extract_obj(t_obj);
                        continue;
                    }

                    sand_effect(t_obj, level / 2, dam / 2, TARGET_OBJ);
                }
            }

            extract_obj(obj);
        }
    }

    static void scream_effect(Object vo, int level, int dam, int target) {

        if (target == TARGET_ROOM)  /* nail objects on the floor */ {
            var room = (ROOM_INDEX_DATA) vo;
            OBJ_DATA obj, obj_next;
            for (obj = room.contents; obj != null; obj = obj_next) {
                obj_next = obj.next_content;
                scream_effect(obj, level, dam, TARGET_OBJ);
            }
            return;
        }

        if (target == TARGET_CHAR)   /* do the effect on a victim */ {
            var victim = (CHAR_DATA) vo;
            OBJ_DATA obj, obj_next;

            if (!saves_spell(level / 4 + dam / 20, victim, DAM_SOUND)) {
                var af = new AFFECT_DATA();
                act("$n can't hear anything!", victim, null, null, TO_ROOM);
                act("You can't hear a thing!", victim, null, null, TO_CHAR);

                af.where = TO_AFFECTS;
                af.type = gsn_scream;
                af.level = level;
                af.duration = 0;
                af.location = APPLY_NONE;
                af.modifier = 0;
                af.bitvector = AFF_SCREAM;

                affect_to_char(victim, af);
            }

            /* daze and confused? */
            if (!saves_spell(level / 4 + dam / 20, victim, DAM_SOUND)) {
                send_to_char("You can't hear anything!.\n", victim);
                DAZE_STATE(victim, UMAX(12, level / 4 + dam / 20));
            }

            /* getting thirsty */
            if (!IS_NPC(victim)) {
                gain_condition(victim, COND_THIRST, dam / 20);
            }

            /* let's toast some gear! */
            for (obj = victim.carrying; obj != null; obj = obj_next) {
                obj_next = obj.next_content;

                scream_effect(obj, level, dam, TARGET_OBJ);
            }
            return;
        }

        if (target == TARGET_OBJ)  /* toast an object */ {
            var obj = (OBJ_DATA) vo;
            OBJ_DATA t_obj, n_obj;
            int chance;

            if (IS_OBJ_STAT(obj, ITEM_BURN_PROOF)
                    || IS_OBJ_STAT(obj, ITEM_NOPURGE)
                    || number_range(0, 4) == 0) {
                return;
            }

            chance = level / 4 + dam / 10;

            if (chance > 25) {
                chance = (chance - 25) / 2 + 25;
            }
            if (chance > 50) {
                chance = (chance - 50) / 2 + 50;
            }

            if (IS_OBJ_STAT(obj, ITEM_BLESS)) {
                chance -= 5;
            }
            chance -= obj.level * 2;
            String msg;
            if (check_material(obj, "ice")) {
                chance += 30;
                msg = "$p breaks and evaporates!";
            } else if (check_material(obj, "glass")) {
                chance += 30;
                msg = "$p breaks into tiny small peaces";
            } else {
                switch (obj.item_type) {
                    default -> {
                        return;
                    }
                    case ITEM_POTION -> {
                        chance += 25;
                        msg = "Vial of $p breaks and liquid spoils!";
                    }
                    case ITEM_SCROLL -> {
                        chance += 50;
                        msg = "$p breaks into tiny peaces!";
                    }
                    case ITEM_DRINK_CON -> {
                        msg = "$p breaks and liquid spoils!";
                        chance += 5;
                    }
                    case ITEM_PILL -> msg = "$p breaks into peaces!";
                }
            }

            chance = URANGE(5, chance, 95);

            if (number_percent() > chance) {
                return;
            }

            if (obj.carried_by != null) {
                act(msg, obj.carried_by, obj, null, TO_ALL);
            } else if (obj.in_room != null && obj.in_room.people != null) {
                act(msg, obj.in_room.people, obj, null, TO_ALL);
            }

            if (obj.contains != null) {
                /* dump the contents */

                for (t_obj = obj.contains; t_obj != null; t_obj = n_obj) {
                    n_obj = t_obj.next_content;
                    obj_from_obj(t_obj);
                    if (obj.in_room != null) {
                        obj_to_room(t_obj, obj.in_room);
                    } else if (obj.carried_by != null) {
                        obj_to_room(t_obj, obj.carried_by.in_room);
                    } else {
                        extract_obj(t_obj);
                        continue;
                    }
                    scream_effect(t_obj, level / 2, dam / 2, TARGET_OBJ);
                }
            }

            extract_obj(obj);
        }
    }
}
