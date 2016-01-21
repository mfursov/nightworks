package net.sf.nightworks;

import static net.sf.nightworks.Const.language_table;
import static net.sf.nightworks.Nightworks.MAX_LANGUAGE;
import static net.sf.nightworks.Tables.flag_type;
import static net.sf.nightworks.Tables.position_table;
import static net.sf.nightworks.Tables.sex_table;
import static net.sf.nightworks.Tables.size_table;
import static net.sf.nightworks.util.TextUtils.str_prefix;

class Lookup {

    static int flag_lookup(String name, flag_type[] flag_table) {
        for (flag_type ft : flag_table) {
            if (!str_prefix(name, ft.name)) {
                return ft.bit;
            }
        }
        return 0;
    }


    static int position_lookup(String name) {
        for (int pos = 0; pos < position_table.length; pos++) {
            if (!str_prefix(name, position_table[pos].name)) {
                return pos;
            }
        }

        return -1;
    }

    static int sex_lookup(String name) {
        for (int sex = 0; sex < sex_table.length; sex++) {
            if (!str_prefix(name, sex_table[sex].name)) {
                return sex;
            }
        }

        return -1;
    }

    static int size_lookup(String name) {
        for (int size = 0; size < size_table.length; size++) {
            if (!str_prefix(name, size_table[size].name)) {
                return size;
            }
        }

        return 0;
    }

    static int lang_lookup(String name) {
        if ((!str_prefix(name, "mothertongue") || !str_prefix(name, "motherlanguage"))) {
            return MAX_LANGUAGE;
        }

        for (int lang = 0; lang < MAX_LANGUAGE; lang++) {
            if (!str_prefix(name, language_table[lang].name)) {
                return lang;
            }
        }

        return -1;
    }
}

