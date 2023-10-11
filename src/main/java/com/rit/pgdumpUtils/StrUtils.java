package com.rit.pgdumpUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtils {

    public static final String CLR_RESET = "\u001B[0m";
    public static final String CLR_GREEN = "\u001B[32m";
    public static final String CLR_YELLOW = "\u001B[33m";
    public static final String CLR_PURPLE = "\u001B[35m";
    public static final String CLR_CYAN = "\u001B[36m";
    public static final String bom = "efbbbf";
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    public static boolean isEmptyStr(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static String getNonEmptyStr(String s, boolean doTrim) {
        String res = s;
        if (isEmptyStr(res)) res=""; else
        if (doTrim) res = res.trim();
        return res;
    }

    public static String getNonEmptyStr(String s) {
        return getNonEmptyStr(s, false);
    }

    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    public static String regexSubstring(String value, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    public static boolean containsIgnoreCase(List<String> matchList, String elem) {
        for (String s: matchList)
            if (s.equalsIgnoreCase(elem)) return true;
        return false;
    }
}
