package com.knightgost.knighttowns.utils;

import java.util.Map;

public class PlaceholderUtil {

    public static String applyPlaceholders(String text, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return text;
    }
}
