package com.backbase.oss.blimp.format;

import java.util.Locale;
import java.util.StringTokenizer;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

/**
 * Formatter that modifies SQL string. Code borrowed from
 * {@link org.hibernate.engine.jdbc.internal.DDLFormatterImpl}.
 */
final class DDLFormatter {

    public static final DDLFormatter INSTANCE = new DDLFormatter();
    private static final String INITIAL_LINE = StreamUtil.getLineSeparator() + "";
    private static final String INDENT_LINE = StreamUtil.getLineSeparator() + "\1";

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

        final String start = StringUtils.substring(sql, 0, 16).toLowerCase(Locale.ROOT);

        if (start.startsWith("create table")) {
            return formatCreateTable(sql);
        }
        if (start.startsWith("create")) {
            return sql;
        }
        if (start.startsWith("alter table")) {
            return formatAlterTable(sql);
        }
        if (start.startsWith("comment on")) {
            return formatCommentOn(sql);
        }

        return INITIAL_LINE + sql;
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
                result.append(INDENT_LINE);
            }
        }

        return align(result);
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
                result.append(INDENT_LINE);
            }
            result.append(token);
        }

        return align(result);
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
                result.append(INDENT_LINE);
            }

            if ("(".equals(token) && !quoted && ++depth == 1) {
                result.append(INDENT_LINE);
            }
        }

        return align(result);
    }

    private String align(StringBuilder sb) {
        return sb.toString().replace("\1 ", "    ").replace("\1", "    ");
    }
}
