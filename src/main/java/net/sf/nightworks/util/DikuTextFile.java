package net.sf.nightworks.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Formatter;

import static net.sf.nightworks.Tables.flag_type;
import static net.sf.nightworks.util.TextUtils.is_number;
import static net.sf.nightworks.util.TextUtils.isDigit;
import static net.sf.nightworks.util.TextUtils.isSpace;
import static net.sf.nightworks.util.TextUtils.str_cmp;

public class DikuTextFile {

    private static final int MAX_WORD_LENGTH = 256;
    private static final char END_OF_STREAM_CHAR = (char) -1;

    final char[] data;
    final File file;
    private int currentPos = 0;
    private final StringBuilder tmpBuf = new StringBuilder();
    public boolean fMatch;

    public DikuTextFile(String fileName) throws IOException {
        this(new File(fileName));
    }

    public DikuTextFile(File file) throws IOException {
        this.file = file;
        int len = (int) file.length();
        data = new char[len];
        FileReader reader = new FileReader(file);
        try {
            int pos = 0;
            do {
                int dpos = reader.read(data, pos, len - pos);
                assert (dpos >= 0);
                pos += dpos;
            } while (pos != len);
        } finally {
            reader.close();
        }
    }


    public int fread_flag() {
        int number;
        boolean negative = false;

        int c;
        do {
            c = read();
        } while (isSpace(c));

        if (c == '-') {
            negative = true;
            c = read();
        }
        number = 0;
        if (!isDigit(c)) {
            while (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z')) {
                number += flag_convert((char) c);
                c = read();
            }
        }

        while (isDigit(c)) {
            number = number * 10 + c - '0';
            c = read();
        }

        if (c == '|') {
            number += fread_flag();
        } else if (c != ' ') {
            ungetc();
        }

        if (negative) {
            return -1 * number;
        }
        return number;
    }

    public char read() {
        if (currentPos == data.length) {
            return END_OF_STREAM_CHAR;
        }
        return data[currentPos++];
    }

    public int flag_convert(char letter) {
        int bitsum = 0;
        char i;

        if ('A' <= letter && letter <= 'Z') {
            bitsum = 1;
            for (i = letter; i > 'A'; i--) {
                bitsum *= 2;
            }
        } else if ('a' <= letter && letter <= 'z') {
            bitsum = 67108864; /* 2^26 */
            for (i = letter; i > 'a'; i--) {
                bitsum *= 2;
            }
        }

        return bitsum;
    }

    /**
     * Read and allocate space for a string from a file.
     * These strings are read-only and shared.
     * Strings are hashed:
     * each string prepended with hash pointer to prev string,
     * hash code is simply the string length.
     * this function takes 40% to 50% of boot-up time.
     */
    public String fread_string() {
        tmpBuf.setLength(0);

        int c;
        /* Skip blanks.* Read first char. */
        do {
            c = read();
        } while (isSpace(c));
        do {
            /*
             * Back off the char type lookup,
             *   it was too dirty for portability.
             *   -- Furey
             */
            if (c == '~') {
                return tmpBuf.toString();
            }
            if (c != '\r') {
                tmpBuf.append((char) c);
            }
            c = read();
        } while (true);
    }

    public String buildCurrentStateInfo() {
        int line = 0;
        String lineStr = "";
        if (data.length > 0) {
            int pos = currentPos;
            currentPos = 0;
            int end;
            while (true) {
                line++;
                for (end = currentPos; end < data.length; end++) {
                    if (data[end] == '\n') {
                        break;
                    }
                }
                if (end >= pos - 1) {
                    break;
                }
                currentPos = end + 1;//new line char
            }
            lineStr = new String(data, currentPos, end - currentPos);
        }
        Formatter f = new Formatter();
        f.format("FILE: %s LINE: %d TEXT:%s", file.getAbsolutePath(), line, lineStr);
        return f.toString();
    }

    public String fread_string_eol() {
        int c;
        /* Skip blanks.* Read first char. */
        do {
            c = read();
        } while (isSpace(c));
        int pos = currentPos - 1;
        fread_to_eol();
        return new String(data, pos, currentPos - pos).trim();
    }

/*
* Read to end of line (for comments).
*/

    public void fread_to_eol() {
        char c;
        do {
            c = read();
        } while (c != '\n' && c != '\r' && c != END_OF_STREAM_CHAR);
        do {
            c = read();
        } while (c == '\n' || c == '\r');
        ungetc();
    }


    public String fread_word() {
        tmpBuf.setLength(0);
        char cEnd;
        do {
            cEnd = read();
        } while (isSpace(cEnd));

        if (cEnd != '\'' && cEnd != '"') {
            tmpBuf.append(cEnd);
            cEnd = ' ';
        }

        while (tmpBuf.length() < MAX_WORD_LENGTH) {
            char c = read();
            if (c == END_OF_STREAM_CHAR || (cEnd == ' ' ? isSpace(c) : (c == cEnd))) {
//                System.err.println("Word '"+word+"'");
                if (isSpace(c)) {
                    ungetc();
                }
                return tmpBuf.toString();
            }
            tmpBuf.append(c);
        }
        throw new RuntimeException("Fread_word: word too long." + buildCurrentStateInfo());
    }
/*
* Read a letter from a file.
*/

    public char fread_letter() {
        while (!feof()) {
            char c = read();
            if (!isSpace(c)) {
                return c;
            }
        }
        return END_OF_STREAM_CHAR;
    }

/*
* Read a number from a file.
*/

    public int fread_number() {
        char c;
        do {
            c = read();
        } while (isSpace(c));

        int number = 0;
        boolean sign = false;
        if (c == '+') {
            c = read();
        } else if (c == '-') {
            sign = true;
            c = read();
        }

        if (!isDigit(c)) {
            throw new RuntimeException("fread_number: bad format." + buildCurrentStateInfo());

        }
        while (isDigit(c)) {
            number = number * 10 + c - '0';
            c = read();
        }

        if (sign) {
            number = 0 - number;
        }

        if (c == '|') {
            number += fread_number();
        } else if (c != ' ') {
            ungetc();
        }

        return number;
    }

    public void ungetc() {
        assert (currentPos > 0);
        currentPos--;
    }


    public boolean feof() {
        return currentPos >= data.length;
    }

    public String WKEY(String literal, String word, String defaultValue) {
        if (fMatch || str_cmp(word, literal)) {
            return defaultValue;
        }
        fMatch = true;
        return fread_word();
    }


    public String SKEY(String literal, String word, String defaultValue) {
        if (fMatch || str_cmp(word, literal)) {
            return defaultValue;
        }
        fMatch = true;
        return fread_string();
    }

    public int NKEY(String literal, String word, int defaultValue) {
        if (fMatch || str_cmp(word, literal)) {
            return defaultValue;
        }
        fMatch = true;
        return fread_number();
    }

    public long FLAG64_OLD(String literal, String word, long defaultValue) {
        if (fMatch || str_cmp(word, literal)) {
            return defaultValue;
        }
        fMatch = true;
        return fread_flag();
    }

    public long FLAG64_KEY(String literal, String word, long defaultValue, flag_type[] table, boolean multiFlag) {
        if (fMatch || str_cmp(word, literal)) {
            return defaultValue;
        }
        fMatch = true;
        String str = multiFlag ? fread_string() : fread_word();
        long val;
        if (is_number(str)) {
            val = Integer.valueOf(str);
        } else {
            val = flag_type.parseFlagsValue(str, table);
        }
        return val;
    }

    public long FLAG64_WKEY(String literal, String word, long defaultValue, flag_type[] table) {
        return (int) FLAG64_KEY(literal, word, defaultValue, table, false);
    }

    public long FLAG64_SKEY(String literal, String word, long defaultValue, flag_type[] table) {
        return (int) FLAG64_KEY(literal, word, defaultValue, table, true);
    }

    public int FLAG32_SKEY(String literal, String word, int defaultValue, flag_type[] flags) {
        return (int) FLAG64_KEY(literal, word, defaultValue, flags, true);
    }

    public int FLAG32_WKEY(String literal, String word, int defaultValue, flag_type[] flags) {
        return (int) FLAG64_KEY(literal, word, defaultValue, flags, false);
    }

    public File getFile() {
        return file;
    }

}
