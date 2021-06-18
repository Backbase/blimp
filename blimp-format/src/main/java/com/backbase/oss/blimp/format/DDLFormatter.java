package com.backbase.oss.blimp.format;

import java.util.Locale;
import java.util.StringTokenizer;
import liquibase.util.StreamUtil;

/**
 * Formatter that modifies SQL string.
 */
final class DDLFormatter {

    public static final DDLFormatter INSTANCE = new DDLFormatter();
    private static final String INITIAL_LINE = StreamUtil.getLineSeparator() + "";
    private static final String OTHER_LINES = StreamUtil.getLineSeparator() + "   ";

    private static boolean isBreak(String token) {
        switch (token.toLowerCase()) {
            case "drop":
            case "add":
            case "references":
            case "foreign":
            case "on":
            case "modify":
                return true;

            default:
                return false;
        }
    }

    private static boolean isQuote(String tok) {
        switch (tok.toLowerCase()) {
            case "\"":
            case "`":
            case "]":
            case "[":
            case "'":
                return true;

            default:
                return false;
        }
    }

    private static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    /**
     * Alter sql statement according to internal rules.
     */
    String format(String sql) {
        if (isEmpty(sql)) {
            return sql;
        }

        if (sql.toLowerCase(Locale.ROOT).startsWith("create table")) {
            return formatCreateTable(sql);
        }
        if (sql.toLowerCase(Locale.ROOT).startsWith("create")) {
            return sql;
        }
        if (sql.toLowerCase(Locale.ROOT).startsWith("alter table")) {
            return formatAlterTable(sql);
        }
        if (sql.toLowerCase(Locale.ROOT).startsWith("comment on")) {
            return formatCommentOn(sql);
        } else {
            return INITIAL_LINE + sql;
        }
    }

    private String formatCommentOn(String sql) {
        final StringBuilder result = new StringBuilder(60).append(INITIAL_LINE);
        final StringTokenizer tokens = new StringTokenizer(sql, " '[]\"", true);

        boolean quoted = false;
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken();
            result.append(token);
            if (isQuote(token)) {
                quoted = !quoted;
            } else if (!quoted && "is".equalsIgnoreCase(token)) {
                result.append(OTHER_LINES);
            }
        }

        return result.toString();
    }

    private String formatAlterTable(String sql) {
        final StringBuilder result = new StringBuilder(60).append(INITIAL_LINE);
        final StringTokenizer tokens = new StringTokenizer(sql, " (,)'[]\"", true);

        boolean quoted = false;
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken();
            if (isQuote(token)) {
                quoted = !quoted;
            } else if (!quoted && isBreak(token)) {
                result.append(OTHER_LINES);
            }
            result.append(token);
        }

        return result.toString();
    }

    private String formatCreateTable(String sql) {
        final StringBuilder result = new StringBuilder(60).append(INITIAL_LINE);
        final StringTokenizer tokens = new StringTokenizer(sql, "(,)'[]\"", true);

        int depth = 0;
        boolean quoted = false;
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken();

            quoted = isQuote(token) ? !quoted : quoted;

            if (")".equals(token) && !quoted && --depth == 0) {
                result.append(INITIAL_LINE);
            }

            result.append(token);

            if (",".equals(token) && !quoted && depth == 1) {
                result.append(OTHER_LINES);
            }

            if ("(".equals(token) && !quoted && ++depth == 1) {
                result.append(OTHER_LINES);
            }
        }

        return result.toString();
    }
}
