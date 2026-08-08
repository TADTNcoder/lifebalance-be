package com.lifebalance.task.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SlugGenerator {

    private static final int MAX_SLUG_LENGTH = 100;
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_HYPHENS = Pattern.compile("^-+|-+$");
    private static final String FALLBACK_SLUG = "tag";

    private SlugGenerator() {
    }

    public static String from(String value) {
        if (value == null) {
            return FALLBACK_SLUG;
        }

        String normalized = value.trim()
                .replace('Đ', 'D')
                .replace('đ', 'd');
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        normalized = DIACRITICS.matcher(normalized).replaceAll("");
        normalized = normalized.toLowerCase(Locale.ROOT);
        normalized = NON_ALPHANUMERIC.matcher(normalized).replaceAll("-");
        normalized = EDGE_HYPHENS.matcher(normalized).replaceAll("");

        if (normalized.isBlank()) {
            return FALLBACK_SLUG;
        }
        if (normalized.length() <= MAX_SLUG_LENGTH) {
            return normalized;
        }

        String truncated = normalized.substring(0, MAX_SLUG_LENGTH);
        truncated = EDGE_HYPHENS.matcher(truncated).replaceAll("");
        return truncated.isBlank() ? FALLBACK_SLUG : truncated;
    }
}
