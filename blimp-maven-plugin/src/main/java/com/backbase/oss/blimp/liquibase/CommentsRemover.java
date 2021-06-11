package com.backbase.oss.blimp.liquibase;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static liquibase.util.StringUtils.trimRight;
import static liquibase.util.StringUtils.trimToNull;

import java.io.StringReader;
import java.util.Objects;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.util.grammar.SimpleCharStream;
import liquibase.util.grammar.SimpleSqlGrammar;
import liquibase.util.grammar.SimpleSqlGrammarConstants;
import liquibase.util.grammar.SimpleSqlGrammarTokenManager;
import liquibase.util.grammar.Token;

final class CommentsRemover {
    private static final Logger LOG = LogService.getLog(CommentsRemover.class);

    static String apply(String sql) {
        LOG.debug(format("Removing comments: %s", sql));

        final StringBuilder clauses = new StringBuilder();
        final SimpleSqlGrammar grammar =
            new SimpleSqlGrammar(
                new SimpleSqlGrammarTokenManager(
                    new SimpleCharStream(
                        new StringReader(sql))));

        Token token = grammar.getNextToken();

        while (!"".equals(token.toString())) {
            if ((token.kind != SimpleSqlGrammarConstants.MULTI_LINE_COMMENT)) {
                clauses.append(token.image);
            }

            token = grammar.getNextToken();
        }

        return stream(clauses.toString().split("\\r?\\n"))
            .map(line -> removeDashes(line, 0))
            .filter(Objects::nonNull)
            .collect(joining("\n", "", "\n"));
    }

    static private String removeDashes(String line, int start) {
        if (line == null) {
            return null;
        }

        final int index = line.indexOf("--", start);

        if (index < 0) {
            return trimRight(line);
        }

        final int quote1 = line.indexOf('\'', start);

        if (quote1 < 0) {
            return trimToNull(line.substring(0, index));
        }

        final int quote2 = line.indexOf('\'', quote1 + 1);

        if (quote1 < index && index < quote2) {
            return removeDashes(line, quote2 + 1);
        }

        return trimToNull(line.substring(0, index));
    }
}

