package net.sf.nightworks;

import net.sf.nightworks.Nightworks.CHAR_DATA;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.sf.nightworks.Nightworks.CMD_GHOST;
import static net.sf.nightworks.Nightworks.CMD_KEEP_HIDE;
import static net.sf.nightworks.Nightworks.POS_DEAD;
import static net.sf.nightworks.Nightworks.POS_FIGHTING;
import static net.sf.nightworks.Nightworks.POS_RESTING;
import static net.sf.nightworks.Nightworks.POS_SLEEPING;
import static net.sf.nightworks.Nightworks.POS_STANDING;
import static net.sf.nightworks.util.TextUtils.arr;

@SuppressWarnings("SpellCheckingInspection")
public enum CmdType {
    /*
     * Common movement commands.
     */
    do_north("north", ActMove::do_north, POS_STANDING, 0, Interp.LOG_NEVER, CMD_KEEP_HIDE | CMD_GHOST),
    do_east("east", ActMove::do_east, POS_STANDING, 0, Interp.LOG_NEVER, CMD_KEEP_HIDE | CMD_GHOST),
    do_south("south", ActMove::do_south, POS_STANDING, 0, Interp.LOG_NEVER, CMD_KEEP_HIDE | CMD_GHOST),
    do_west("west", ActMove::do_west, POS_STANDING, 0, Interp.LOG_NEVER, CMD_KEEP_HIDE | CMD_GHOST),
    do_up("up", ActMove::do_up, POS_STANDING, 0, Interp.LOG_NEVER, CMD_KEEP_HIDE | CMD_GHOST),
    do_down("down", ActMove::do_down, POS_STANDING, 0, Interp.LOG_NEVER, CMD_KEEP_HIDE | CMD_GHOST),
    do_run("run", ActMove::do_run, POS_STANDING, Interp.ML, Interp.LOG_NEVER, CMD_KEEP_HIDE | CMD_GHOST),

    /*
     * Common other commands.
     * Placed here as one and two letter abbreviations work.
     */
    do_at("at", ActWiz::do_at, POS_DEAD, Interp.L6, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_cast("cast", Magic::do_cast, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_claw("claw", MartialArt::do_claw, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_crecall("crecall", ActMove::do_crecall, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_auction("auction", ActHera::do_auction, POS_SLEEPING, 0, Interp.LOG_NORMAL, CMD_GHOST),
    do_buy("buy", ActObj::do_buy, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_channels("channels", ActComm::do_channels, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_exits("exits", ActInfo::do_exits, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_estimate("estimate", ActHera::do_estimate, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_GHOST),

    do_goto("goto", ActWiz::do_goto, POS_DEAD, Interp.L8, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_glist("glist", ActSkill::do_glist, POS_DEAD, 0, Interp.LOG_NEVER, 0),
    do_group("group", ActComm::do_group, POS_SLEEPING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE),
    do_inventory("inventory", ActInfo::do_inventory, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_kill(arr("kill", "hit"), Fight::do_kill, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_look("look", ActInfo::do_look, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_order("order", ActComm::do_order, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_pracnew("practice", ActInfo::do_pracnew, POS_SLEEPING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE),
    do_practice("prac_old", ActInfo::do_practice, POS_SLEEPING, Interp.ML, Interp.LOG_NORMAL, CMD_KEEP_HIDE),
    do_rest("rest", ActMove::do_rest, POS_SLEEPING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE),
    do_repair("repair", ActHera::do_repair, POS_SLEEPING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE),
    do_sit("sit", ActMove::do_sit, POS_SLEEPING, 0, Interp.LOG_NORMAL, 0),
    do_smithing("smithing", ActHera::do_smithing, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_stand("stand", ActMove::do_stand, POS_SLEEPING, 0, Interp.LOG_NORMAL, 0),
    do_tell("tell", ActComm::do_tell, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_GHOST),
    do_unlock("unlock", ActMove::do_unlock, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_wizhelp("wizhelp", Interp::do_wizhelp, POS_DEAD, Interp.IM, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),

    /*
     * Informational commands.
     */
    do_affects("affects", ActInfo::do_affects, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_areas("areas", DB::do_areas, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_balance("balance", ActObj::do_balance, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_bug("bug", ActComm::do_bug, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_changes("changes", Note::do_changes, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_commands("commands", Interp::do_commands, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_compare("compare", ActInfo::do_compare, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE),
    do_consider("consider", ActInfo::do_consider, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE),
    do_concentrate("concentrate", MartialArt::do_concentrate, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_count("count", ActInfo::do_count, POS_SLEEPING, Interp.HE, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_credits("credits", ActInfo::do_credits, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_deposit("deposit", ActObj::do_deposit, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_equipment("equipment", ActInfo::do_equipment, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_escape("escape", ActMove::do_escape, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_examine("examine", ActInfo::do_examine, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_GHOST),
    do_help("help", ActInfo::do_help, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_idea("idea", Note::do_idea, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_motd("motd", ActInfo::do_motd, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_news("news", Note::do_news, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_raffects("raffects", ActInfo::do_raffects, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_read("read", ActInfo::do_read, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_report("report", ActInfo::do_report, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_rules("rules", ActInfo::do_rules, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_scan("scan", ActInfo::do_scan, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_nscore("score", ActInfo::do_nscore, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_skills("skills", ActSkill::do_skills, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_speak("speak", ActComm::do_speak, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_socials("socials", ActInfo::do_socials, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_show("show", ActInfo::do_show, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_spells("spells", ActSkill::do_spells, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_time("time", ActInfo::do_time, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_typo("typo", ActComm::do_typo, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_weather("weather", ActInfo::do_weather, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_who("who", ActInfo::do_who, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_whois("whois", ActInfo::do_whois, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_withdraw("withdraw", ActObj::do_withdraw, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_wizlist("wizlist", ActInfo::do_wizlist, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_worth("worth", ActInfo::do_worth, POS_SLEEPING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),

    /*
     * Configuration commands.
     */
    do_alia("alia", Interp::do_alia, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_alias("alias", Interp::do_alias, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_clear("clear", ActInfo::do_clear, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    //        do_clear("cls",  ActInfo::do_clear, POS_DEAD, 0, LOG_NORMAL, 1, CMD_KEEP_HIDE | CMD_GHOST),
    do_autolist("autolist", ActInfo::do_autolist, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_color("color", ActInfo::do_color, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_autoassist("autoassist", ActInfo::do_autoassist, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_autoexit("autoexit", ActInfo::do_autoexit, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_autogold("autogold", ActInfo::do_autogold, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_autoloot("autoloot", ActInfo::do_autoloot, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_autosac("autosac", ActInfo::do_autosac, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_autosplit("autosplit", ActInfo::do_autosplit, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_brief("brief", ActInfo::do_brief, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_combine("combine", ActInfo::do_combine, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_compact("compact", ActInfo::do_compact, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_description("description", ActInfo::do_description, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_delet("delet", ActComm::do_delet, POS_DEAD, 0, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_delete("delete", ActComm::do_delete, POS_STANDING, 0, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_identify("identify", ActInfo::do_identify, POS_STANDING, 0, Interp.LOG_NORMAL, CMD_GHOST),
    do_nocancel("nocancel", ActInfo::do_nocancel, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_nofollow("nofollow", ActInfo::do_nofollow, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_noloot("noloot", ActInfo::do_noloot, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_nosummon("nosummon", ActInfo::do_nosummon, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_outfit("outfit", ActWiz::do_outfit, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_tick("tick", ActWiz::do_tick, POS_DEAD, Interp.ML, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_password("password", ActInfo::do_password, POS_DEAD, 0, Interp.LOG_NEVER, CMD_KEEP_HIDE | CMD_GHOST),
    do_prompt("prompt", ActInfo::do_prompt, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_quest("quest", Quest::do_quest, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_GHOST),
    do_scroll("scroll", ActInfo::do_scroll, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_title("title", ActInfo::do_title, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_unalias("unalias", Interp::do_unalias, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_wimpy("wimpy", ActInfo::do_wimpy, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),

    /*
     * Communication commands.
     */
    do_bear_call("bearcall", ActInfo::do_bear_call, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_cb("cb", ActComm::do_cb, POS_SLEEPING, 0, Interp.LOG_NORMAL, CMD_GHOST),
    do_deaf("deaf", ActComm::do_deaf, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_emote(arr("emote", ","), ActComm::do_emote, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_GHOST),
    do_pmote("pmote", ActComm::do_pmote, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_GHOST),
    do_gtell(arr("gtell", ";"), ActComm::do_gtell, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_GHOST),
    do_note("note", Note::do_note, POS_SLEEPING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_pose("pose", ActComm::do_pose, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_GHOST),
    do_pray("pray", ActComm::do_pray, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_quiet("quiet", ActComm::do_quiet, POS_SLEEPING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_reply("reply", ActComm::do_reply, POS_SLEEPING, 0, Interp.LOG_NORMAL, CMD_GHOST),
    do_replay("replay", ActComm::do_replay, POS_SLEEPING, 0, Interp.LOG_NORMAL, CMD_GHOST),
    do_say(arr("say", "'"), ActComm::do_say, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_GHOST),
    do_shout("shout", ActComm::do_shout, POS_RESTING, 3, Interp.LOG_NORMAL, CMD_GHOST),
    do_warcry("warcry", MartialArt::do_warcry, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_unread("unread", Note::do_unread, POS_SLEEPING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_yell("yell", ActComm::do_yell, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_GHOST),

    /*
     * Object manipulation commands.
     */
    do_brandish("brandish", ActObj::do_brandish, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_bury("bury", ActObj::do_bury, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_butcher("butcher", ActObj::do_butcher, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_close("close", ActMove::do_close, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_detect_hidden("detect", ActInfo::do_detect_hidden, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_drag("drag", ActObj::do_drag, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_drink("drink", ActObj::do_drink, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_drop("drop", ActObj::do_drop, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_eat("eat", ActObj::do_eat, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_enchant("enchant", ActObj::do_enchant, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_envenom("envenom", ActObj::do_envenom, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE),
    do_fill("fill", ActObj::do_fill, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_fly("fly", ActMove::do_fly, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_give("give", ActObj::do_give, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_heal("heal", Healer::do_heal, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_layhands("layhands", ActMove::do_layhands, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_list("list", ActObj::do_list, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE),
    do_lock("lock", ActMove::do_lock, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_lore("lore", ActObj::do_lore, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_open("open", ActMove::do_open, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_pick("pick", ActMove::do_pick, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_pour("pour", ActObj::do_pour, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE),
    do_put("put", ActObj::do_put, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_quaff("quaff", ActObj::do_quaff, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_recite("recite", ActObj::do_recite, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_remove("remove", ActObj::do_remove, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_request("request", ActInfo::do_request, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_sell("sell", ActObj::do_sell, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_get(arr("get", "take"), ActObj::do_get, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE),
    do_sacrifice(arr("sacrifice", "junk"), ActObj::do_sacrifice, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_trophy("trophy", MartialArt::do_trophy, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_value("value", ActObj::do_value, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_wear(arr("wear", "hold", "wield"), ActObj::do_wear, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_zap("zap", ActObj::do_zap, POS_RESTING, 0, Interp.LOG_NORMAL, 0),

    /*
     * Combat commands.
     */
    do_ambush("ambush", MartialArt::do_ambush, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_assassinate("assassinate", MartialArt::do_assassinate, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_bash("bash", MartialArt::do_bash, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_bash_door("bashdoor", ActMove::do_bash_door, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_backstab(arr("backstab", "bs"), MartialArt::do_backstab, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_vbite("bite", ActMove::do_vbite, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_blindness_dust("blindness", MartialArt::do_blindness_dust, POS_FIGHTING, 0, Interp.LOG_ALWAYS, 0),
    do_vtouch("touch", ActMove::do_vtouch, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_berserk("berserk", MartialArt::do_berserk, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_bloodthirst("bloodthirst", MartialArt::do_bloodthirst, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_blackjack("blackjack", MartialArt::do_blackjack, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_caltraps("caltraps", MartialArt::do_caltraps, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_explode("explode", MartialArt::do_explode, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_camouflage("camouflage", ActMove::do_camouflage, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_circle("circle", MartialArt::do_circle, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_cleave("cleave", MartialArt::do_cleave, POS_STANDING, 0, Interp.LOG_NORMAL, 0),

    do_dirt("dirt", MartialArt::do_dirt, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_disarm("disarm", MartialArt::do_disarm, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_dishonor("dishonor", Fight::do_dishonor, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_dismount("dismount", ActMove::do_dismount, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_flee("flee", Fight::do_flee, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_guard("guard", MartialArt::do_guard, POS_STANDING, 0, Interp.LOG_NORMAL, 0),

    do_kick("kick", MartialArt::do_kick, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_lash("lash", MartialArt::do_lash, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_lion_call("lioncall", ActInfo::do_lion_call, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_make("make", ActInfo::do_make, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_mount("mount", ActMove::do_mount, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_murde("murde", Fight::do_murde, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_murder("murder", Fight::do_murder, POS_FIGHTING, 0, Interp.LOG_ALWAYS, 0),
    do_nerve("nerve", MartialArt::do_nerve, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_poison_smoke("poison", MartialArt::do_poison_smoke, POS_FIGHTING, 0, Interp.LOG_ALWAYS, 0),
    do_rescue("rescue", MartialArt::do_rescue, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_resistance("resistance", MartialArt::do_resistance, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_truesight("truesight", MartialArt::do_truesight, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_shield("shield", MartialArt::do_shield, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_spellbane("spellbane", MartialArt::do_spellbane, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_strangle("strangle", MartialArt::do_strangle, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_tame("tame", MartialArt::do_tame, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_throw("throw", MartialArt::do_throw, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_tiger("tiger", MartialArt::do_tiger, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_trip("trip", MartialArt::do_trip, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_target("target", MartialArt::do_target, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_vampire("vampire", ActMove::do_vampire, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_vanish("vanish", ActMove::do_vanish, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_weapon("weapon", MartialArt::do_weapon, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_blink("blink", ActMove::do_blink, POS_FIGHTING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE),

    /*
     * Miscellaneous commands.
     */
    do_endure("endure", MartialArt::do_endure, POS_STANDING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE),
    do_follow("follow", ActComm::do_follow, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE),
    do_gain("gain", ActSkill::do_gain, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_enter(arr("enter", "leave", "go"), ActHera::do_enter, POS_STANDING, 0, Interp.LOG_NORMAL, CMD_GHOST),
    do_fade("fade", ActMove::do_fade, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE),
    do_herbs("herbs", ActObj::do_herbs, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_hara("hara", MartialArt::do_hara, POS_STANDING, 0, Interp.LOG_NORMAL, 0),

    do_hide("hide", ActMove::do_hide, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE),
    do_human("human", ActMove::do_human, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_hunt("hunt", ActHera::do_hunt, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_qui("qui", ActComm::do_qui, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_quit("quit", ActComm::do_quit, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_recall("recall", ActMove::do_recall, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_rent("rent", ActComm::do_rent, POS_DEAD, 0, Interp.LOG_NORMAL, 0),
    do_save("save", ActComm::do_save, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_GHOST),
    do_sleep("sleep", ActMove::do_sleep, POS_SLEEPING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE),
    do_slist("slist", ActSkill::do_slist, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_sneak("sneak", ActMove::do_sneak, POS_STANDING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE),
    do_split("split", ActComm::do_split, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_steal("steal", ActObj::do_steal, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_train("train", ActMove::do_train, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_visible("visible", ActMove::do_visible, POS_SLEEPING, 0, Interp.LOG_NORMAL, 0),
    do_wake("wake", ActMove::do_wake, POS_SLEEPING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE),
    do_wanted("wanted", ActObj::do_wanted, POS_STANDING, 0, Interp.LOG_ALWAYS, 0),
    do_where("where", ActInfo::do_where, POS_RESTING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),

    /*
     * Immortal commands.
     */
    do_advance("advance", ActWiz::do_advance, POS_DEAD, Interp.ML, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_set("set", ActWiz::do_set, POS_DEAD, Interp.ML, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_dump("dump", DB::do_dump, POS_DEAD, Interp.ML, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_rename("rename", ActWiz::do_rename, POS_DEAD, Interp.L5, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_violate("violate", ActWiz::do_violate, POS_DEAD, Interp.ML, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_track("track", ActMove::do_track, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_trust("trust", ActWiz::do_trust, POS_DEAD, Interp.ML, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),

    do_allow("allow", Ban::do_allow, POS_DEAD, Interp.L2, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_ban("ban", Ban::do_ban, POS_DEAD, Interp.L2, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_deny("deny", ActWiz::do_deny, POS_DEAD, Interp.L1, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_disconnect("disconnect", ActWiz::do_disconnect, POS_DEAD, Interp.L3, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_flag("flag", Flags::do_flag, POS_DEAD, Interp.L4, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_freeze("freeze", ActWiz::do_freeze, POS_DEAD, Interp.L7, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_permban("permban", Ban::do_permban, POS_DEAD, Interp.L1, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_premort("premort", ActWiz::do_premort, POS_DEAD, Interp.L8, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_protect("protect", ActWiz::do_protect, POS_DEAD, Interp.L1, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_reboo("reboo", ActWiz::do_reboo, POS_DEAD, Interp.L1, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_reboot("reboot", ActWiz::do_reboot, POS_DEAD, Interp.L1, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_induct("induct", ActWiz::do_induct, POS_DEAD, 0, Interp.LOG_ALWAYS, 0),
    do_grant("grant", ActWiz::do_grant, POS_DEAD, Interp.L2, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_smite("smite", ActWiz::do_smite, POS_DEAD, Interp.L7, Interp.LOG_ALWAYS, 0),
    do_limited("limited", ActWiz::do_limited, POS_DEAD, Interp.L4, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_slookup("lookupRace", ActWiz::do_slookup, POS_DEAD, Interp.L2, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_popularity("popularity", ActWiz::do_popularity, POS_DEAD, Interp.L2, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_shutdow("shutdow", ActWiz::do_shutdow, POS_DEAD, Interp.L1, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_shutdown("shutdown", ActWiz::do_shutdown, POS_DEAD, Interp.L1, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_sockets("sockets", ActWiz::do_sockets, POS_DEAD, Interp.L4, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_wizlock("wizlock", ActWiz::do_wizlock, POS_DEAD, Interp.L2, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_affrooms("affrooms", ActWiz::do_affrooms, POS_DEAD, Interp.L4, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_force("force", ActWiz::do_force, POS_DEAD, Interp.L5, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_load("load", ActWiz::do_load, POS_DEAD, Interp.L4, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_newlock("newlock", ActWiz::do_newlock, POS_DEAD, Interp.L4, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_noaffect("noaffect", ActWiz::do_noaffect, POS_DEAD, Interp.L5, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_nochannels("nochannels", ActWiz::do_nochannels, POS_DEAD, Interp.L5, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_notitle("notitle", ActWiz::do_notitle, POS_DEAD, Interp.L7, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_noemote("noemote", ActWiz::do_noemote, POS_DEAD, Interp.L5, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_noshout("noshout", ActWiz::do_noshout, POS_DEAD, Interp.L5, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_notell("notell", ActWiz::do_notell, POS_DEAD, Interp.L5, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_pecho("pecho", ActWiz::do_pecho, POS_DEAD, Interp.L4, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_purge("purge", ActWiz::do_purge, POS_DEAD, Interp.L4, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_restore("restore", ActWiz::do_restore, POS_DEAD, Interp.L4, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_sla("sla", Fight::do_sla, POS_DEAD, Interp.L3, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_slay("slay", Fight::do_slay, POS_DEAD, Interp.L3, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_transfer(arr("teleport", "transfer"), ActWiz::do_transfer, POS_DEAD, Interp.L7, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_bamfin("poofin", ActWiz::do_bamfin, POS_DEAD, Interp.L8, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_bamfout("poofout", ActWiz::do_bamfout, POS_DEAD, Interp.L8, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_echo("gecho", ActWiz::do_echo, POS_DEAD, Interp.L4, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_holylight("holylight", ActWiz::do_holylight, POS_DEAD, Interp.IM, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_incognito("incognito", ActWiz::do_incognito, POS_DEAD, Interp.IM, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_invis(arr("invis", "wizinvis"), ActWiz::do_invis, POS_DEAD, Interp.IM, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_log("log", ActWiz::do_log, POS_DEAD, Interp.L1, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_memory("memory", DB::do_memory, POS_DEAD, Interp.IM, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_mwhere("mwhere", ActWiz::do_mwhere, POS_DEAD, Interp.IM, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_owhere("owhere", ActWiz::do_owhere, POS_DEAD, Interp.IM, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_peace("peace", ActWiz::do_peace, POS_DEAD, Interp.L5, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_penalty("penalty", Note::do_penalty, POS_DEAD, Interp.L7, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_recho("echo", ActWiz::do_recho, POS_DEAD, Interp.L6, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_return("return", ActWiz::do_return, POS_DEAD, Interp.L6, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_snoop("snoop", ActWiz::do_snoop, POS_DEAD, Interp.L5, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_stat("stat", ActWiz::do_stat, POS_DEAD, Interp.IM, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_string("string", ActWiz::do_string, POS_DEAD, Interp.L5, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_switch("switch", ActWiz::do_switch, POS_DEAD, Interp.L6, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_vnum("vnum", ActWiz::do_vnum, POS_DEAD, Interp.L4, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_zecho("zecho", ActWiz::do_zecho, POS_DEAD, Interp.L4, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_cabal_scan("cabal_scan", ActWiz::do_cabal_scan, POS_STANDING, 0, Interp.LOG_NEVER, CMD_KEEP_HIDE | CMD_GHOST),
    do_clone("clone", ActWiz::do_clone, POS_DEAD, Interp.L5, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_wiznet("wiznet", ActWiz::do_wiznet, POS_DEAD, Interp.IM, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_immtalk(arr("immtalk", ":"), ActComm::do_immtalk, POS_DEAD, Interp.IM, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_imotd("imotd", ActInfo::do_imotd, POS_DEAD, Interp.IM, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_smote("smote", ActWiz::do_smote, POS_DEAD, Interp.IM, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_prefi("prefi", ActWiz::do_prefi, POS_DEAD, Interp.IM, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_prefix("prefix", ActWiz::do_prefix, POS_DEAD, Interp.IM, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_objlist("objlist", ActWiz::do_objlist, POS_DEAD, Interp.ML, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_settraps("settraps", ActHera::do_settraps, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_slook("slook", ActSkill::do_slook, POS_SLEEPING, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_learn("learn", ActSkill::do_learn, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_teach("teach", ActSkill::do_teach, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_camp("camp", ActInfo::do_camp, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_dig("dig", ActObj::do_dig, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_tail("tail", MartialArt::do_tail, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_push("push", ActMove::do_push, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_demand("demand", ActInfo::do_demand, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_bandage("bandage", MartialArt::do_bandage, POS_FIGHTING, 0, Interp.LOG_NORMAL, 0),
    do_shoot("shoot", ActMove::do_shoot, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_maximum("maximum", ActWiz::do_maximum, POS_DEAD, Interp.ML, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_find("find", ActWiz::do_find, POS_DEAD, Interp.ML, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_score("nscore", ActInfo::do_score, POS_DEAD, 0, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_katana("katana", MartialArt::do_katana, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_control("control", ActInfo::do_control, POS_STANDING, 0, Interp.LOG_NORMAL, 0),
    do_ititle("ititle", ActWiz::do_ititle, POS_DEAD, Interp.IM, Interp.LOG_NORMAL, CMD_KEEP_HIDE | CMD_GHOST),
    do_sense("sense", MartialArt::do_sense, POS_RESTING, 0, Interp.LOG_NORMAL, 0),
    do_judge("judge", ActComm::do_judge, POS_RESTING, 0, Interp.LOG_ALWAYS, CMD_KEEP_HIDE),
    do_remor("remor", ActComm::do_remor, POS_STANDING, 0, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST),
    do_remort("remort", ActComm::do_remort, POS_STANDING, 0, Interp.LOG_ALWAYS, CMD_KEEP_HIDE | CMD_GHOST);

    public final String[] names;
    public final BiConsumer<CHAR_DATA, String> do_fun;
    public final int position;
    public final int level;
    public final int log;
    public final int extra;

    CmdType(String name, Consumer<CHAR_DATA> do_fun, int position, int level, int log, int extra) {
        this(name, (ch, arg) -> do_fun.accept(ch), position, level, log, extra);
    }

    CmdType(String name, BiConsumer<CHAR_DATA, String> do_fun, int position, int level, int log, int extra) {
        this(new String[]{name}, do_fun, position, level, log, extra);
    }

    CmdType(String[] names, BiConsumer<CHAR_DATA, String> do_fun, int position, int level, int log, int extra) {
        this.names = names;
        this.do_fun = do_fun;
        this.position = position;
        this.level = level;
        this.log = log;
        this.extra = extra;
    }
}
