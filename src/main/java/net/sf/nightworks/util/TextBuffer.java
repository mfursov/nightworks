package net.sf.nightworks.util;

import java.util.Formatter;

public final class TextBuffer implements CharSequence {

    private final StringBuilder sb = new StringBuilder();
    private Formatter f;


    public TextBuffer append(CharSequence str) {
        sb.append(str);
        return this;
    }

    /* clears old content of buffer! */
    public TextBuffer sprintf(String str, Object... args) {
        return sprintf(true, str, args);
    }

    public TextBuffer sprintf(boolean clearOld, String str, Object... args) {
        if (clearOld) {
            clear();
        }
        if (args.length == 0) {
            sb.append(str);
        } else {
            if (f == null) {
                f = new Formatter(sb);
            }
            f.format(str, args);
        }
        return this;
    }

    public @NotNull String toString() {
        return sb.toString();
    }

    public StringBuilder getBuffer() {
        return sb;
    }

    public void clear() {
        sb.setLength(0);
    }

    public int length() {
        return sb.length();
    }

    public void capitalize() {
        if (sb.isEmpty()) {
            return;
        }
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
    }

    public char charAt(int index) {
        return sb.charAt(index);
    }

    public @NotNull CharSequence subSequence(int start, int end) {
        return sb.subSequence(start, end);
    }
}
