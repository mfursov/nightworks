package net.sf.nightworks;

class Configuration {
    /*
     * Data files used by the server.
     *
     * AREA_LIST contains a list of areas to boot.
     * All files are read in completely at bootup.
     * Most output files (bug, idea, typo, shutdown) are append-only.
     *
     * The null_FILE is held open so that we have a stream handle in reserve,
     *   so players can go ahead and telnet to all the other descriptors.
     * Then we close it whenever we need to open a file (e.g. a save file).
     */

    Configuration() {
        //TODO: read from system properties, use hardcoded values as default
        port_num = 4004;

        lib_area_dir = "./lib/areas";
        lib_races_dir = "./lib/races";
        lib_classes_dir = "./lib/classes";
        lib_player_dir = "./lib/players";
        lib_god_dir = "./lib/gods";

        pl_temp_file = "./lib/players/player.tmp";

        var_astat_file = "./lib/area_stat.txt";
        var_ban_file = "./lib/ban.txt";


        etc_area_list = "./etc/areas.list";
        etc_races_list = "./etc/races.list";
        etc_classes_list = "./etc/classes.list";

        note_bug_file = "./lib/notes/bugs.txt";
        note_typo_file = "./lib/notes/typos.txt";
        note_note_file = "./lib/notes/notes.not";
        note_idea_file = "./lib/notes/ideas.not";
        note_news_file = "./lib/notes/news.not";
        note_penalty_file = "./lib/notes/penalty.not";
        note_changes_file = "./lib/notes/changes.not";

        max_alias = 20;
        max_time_log = 14;
        min_time_limit = 600;
    }

    String home_dir;
    int port_num;


    String lib_area_dir;
    String lib_races_dir;
    String lib_classes_dir;
    String lib_player_dir;
    String lib_god_dir;

    String pl_temp_file;

    String etc_area_list;
    String etc_races_list;
    String etc_classes_list;

    String var_astat_file;
    String var_ban_file;

    String note_bug_file;
    String note_typo_file;
    String note_note_file;
    String note_idea_file;
    String note_news_file;
    String note_penalty_file;
    String note_changes_file;

    int max_alias;
    int max_time_log;
    int min_time_limit;
}


