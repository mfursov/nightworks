package net.sf.nightworks.util;

public class TextUtils {
    public static final boolean[] spaces = new boolean[256];

    static {
        spaces[' '] = spaces['\t'] = spaces['\n'] = spaces['\r'] = true; //spaces['\''] = spaces['"']
    }

    public static boolean isSpace(int c) {
        return spaces[c];
    }

    public static boolean isDigit(int c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Removes the tildes from a string.
     * Used for player-entered strings that go into disk files.
     */

    public static String smash_tilde(String str) {
        return str.replace('~', '-');
    }

    /**
     * Compare strings, case insensitive.
     * Return true if different
     * (compatibility with historical functions).
     */

    public static boolean str_cmp(String s1, String s2) {
        return !s1.equalsIgnoreCase(s2);
    }

    /**
     * Compare strings, case insensitive, for prefix matching.
     * Return true if p not a prefix of str
     * (compatibility with historical functions).
     */

    //TODO: all such methods . CharSequence?!
    public static boolean str_prefix(String p, String str) {
        boolean prefix = !p.isEmpty() && p.length() <= str.length();
        prefix = prefix && LOWER(p.charAt(0)) == LOWER(str.charAt(0)) && str.subSequence(0, p.length()).toString().equalsIgnoreCase(p);
        return !prefix;
    }


    /**
     * Compare strings, case insensitive, for suffix matching.
     * Return true if s not a suffix of str
     * (compatibility with historical functions).
     */

    public static boolean str_suffix(String s, String str) {
        int sLen = s.length();
        int strLen = str.length();
        return !(sLen <= strLen && !str_cmp(s, str.substring(strLen - sLen)));
    }

    /**
     * Returns an initial-capped string.
     */

    public static String capitalize(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + (str.length() > 1 ? str.substring(1).toLowerCase() : "");
    }

    public static char LOWER(char c) {
        return (char) (c >= 'A' && c <= 'Z' ? c + 'a' - 'A' : c);
    }

    public static char UPPER(char c) {
        return (char) (c >= 'a' && c <= 'z' ? c + 'A' - 'a' : c);
    }

    /**
     * Return true if an argument is completely numeric.
     */

    public static boolean is_number(CharSequence arg) {
        if (arg.length() == 0) {
            return false;
        }
        int i = 0;
        if (arg.charAt(i) == '+' || arg.charAt(i) == '-') {
            i++;
        }
        for (; i < arg.length(); i++) {
            if (!isDigit(arg.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Pick off one argument from a string and return the rest.
     * Understands quotes.
     */

    public static String one_argument(String argument, StringBuilder arg_first) {
        if (argument.length() == 0) {
            return argument;
        }
        argument = trimSpaces(argument, 0);
        if (argument.length() == 0) {
            return argument;
        }

        char cEnd = argument.charAt(0);
        if (cEnd != '\'' && cEnd != '"') {
            arg_first.append(cEnd);
            cEnd = ' ';
        }

        int pos = 1;
        for (; pos < argument.length(); pos++) {
            char c = argument.charAt(pos);
            if (c == cEnd) {
                pos++;
                break;
            }
            arg_first.append(c);
        }
        argument = pos >= argument.length() ? "" : trimSpaces(argument, pos);
        return argument;
    }

    public static String trimSpaces(String argument, int fromPos) {
        int pos = fromPos;
        int len = argument.length();
        while ((pos < len) && (argument.charAt(pos) <= ' ')) {
            pos++;
        }
        if (pos > 0) {
            argument = argument.substring(pos);
        }
        return argument;
    }

    public static String[] arr(String ... args) {
        return args;
    }

}
