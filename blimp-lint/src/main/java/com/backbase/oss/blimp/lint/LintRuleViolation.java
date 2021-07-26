package com.backbase.oss.blimp.lint;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class LintRuleViolation {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static public class Builder {
        private String id;
        private String rule;
        private String property;
        private String database;
        private LintRuleSeverity severity;
        private String message;
        private final List<Object> values = new ArrayList<>();

        private boolean used;

        public Builder id(String id) {
            checkUsed();

            this.id = id;

            return this;
        }

        public Builder rule(String rule) {
            checkUsed();

            this.rule = rule;

            return this;
        }

        public Builder property(String property) {
            checkUsed();

            this.property = property;

            return this;
        }

        public Builder database(String database) {
            checkUsed();

            this.database = database;

            return this;
        }

        public Builder severity(LintRuleSeverity severity) {
            checkUsed();

            this.severity = severity;

            return this;
        }

        public Builder message(String message) {
            checkUsed();

            this.message = message;

            this.values.clear();

            return this;
        }

        public Builder values(Object... values) {
            checkUsed();

            this.values.addAll(asList(values));

            return this;
        }

        public LintRuleViolation build() {
            this.values.addAll(0, asList(this.id, this.rule, this.property));

            try {
                return new LintRuleViolation(
                    this.id,
                    this.rule,
                    this.property,
                    this.database,
                    this.severity,
                    format(this.message, this.values.toArray(new Object[0])));
            } finally {
                this.used = true;
            }
        }

        private void checkUsed() {
            if (this.used) {
                throw new IllegalStateException("This builder has been already used");
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @NonNull
    @ToString.Include
    private final String id;
    @NonNull
    @ToString.Include
    private final String rule;
    @NonNull
    @ToString.Include
    private final String property;
    @NonNull
    @ToString.Include
    private final String database;
    @NonNull
    @ToString.Include
    private final LintRuleSeverity severity;
    @NonNull
    @ToString.Include
    private final String message;
}
