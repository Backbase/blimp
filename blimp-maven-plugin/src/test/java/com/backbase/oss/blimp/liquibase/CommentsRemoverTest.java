package com.backbase.oss.blimp.liquibase;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class CommentsRemoverTest {

    static Arguments[] dashes() {
        return new Arguments[] {
            Arguments.of("ana\n-- are\nmere\n", "ana\nmere\n"),
            Arguments.of("ana\n  -- are\nmere\n", "ana\nmere\n"),
            Arguments.of("ana\n  ar --e\nmere\n", "ana\nar\nmere\n"),
            Arguments.of("--ana\nare\n  -- mere\n", "are\n"),
            Arguments.of("' -- '", "' -- '\n"),
            Arguments.of("' -- ' -- ", "' -- '\n"),

            Arguments.of("ana\n/* -- are */\nmere\n", "ana\n\nmere\n"),
            Arguments.of("ana\n/* -- are \n*/mere\n", "ana\nmere\n"),
            // nested comments not supported
            // Arguments.of("ana\n/* a /* re */ */\nmere\n", "ana\nmere\n"),
        };
    }

    @ParameterizedTest
    @MethodSource("dashes")
    void removeDashDash(String input, String expected) {
        final String actual = CommentsRemover.apply(input);

        LOG.info("input\n<{}>\nexpected\n<{}>\nactual\n<{}>\n", input, expected, actual);

        assertThat(actual).isEqualTo(expected);
    }

}

