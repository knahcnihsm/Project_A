package com.rgcet.admission.common;

public final class TextUtil {

    private TextUtil() {
    }

    public static String upper(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? trimmed : trimmed.toUpperCase();
    }
}
