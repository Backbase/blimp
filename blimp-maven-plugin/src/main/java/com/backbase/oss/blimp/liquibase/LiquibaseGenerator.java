package com.backbase.oss.blimp.liquibase;

import static java.util.Collections.emptySet;
import com.backbase.oss.blimp.ScriptGroupingStrategy;
import com.backbase.oss.blimp.core.LiquibaseEngine;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.exception.LiquibaseException;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
public final class LiquibaseGenerator extends LiquibaseEngine {

    private final WriterProvider writerProvider;

    @lombok.Builder.Default
    @Getter
    private final ScriptGroupingStrategy strategy = ScriptGroupingStrategy.AUTO;
    @lombok.Builder.Default
    @Getter
    private final Set<String> groups = emptySet();

    private final Path output;
    private final String select;

    private final boolean stripComments;

    public void generateSQL() throws LiquibaseException {
        Objects.requireNonNull(this.output, "The attribute 'output' is required");
        Objects.requireNonNull(this.changeLogCache, "The attribute 'changeLogCache' is required");
        Objects.requireNonNull(this.writerProvider, "The attribute 'writerProvider' is required");
        Objects.requireNonNull(this.strategy, "The attribute 'strategy' is required");

        final Contexts contexts;
        final LabelExpression labels;

        if (this.select == null) {
            contexts = new Contexts();
            labels = new LabelExpression();
        } else {
            switch (this.strategy) {
                case CONTEXTS:
                    contexts = new Contexts(this.select);
                    labels = new LabelExpression();

                    break;

                case LABELS:
                    contexts = new Contexts();
                    labels = new LabelExpression(this.select);

                    break;

                default:
                    throw new AssertionError("unreachable code");
            }
        }

        run(liquibase -> {
            try (Writer out = createOutput()) {
                liquibase.update(contexts, labels, out);
            } catch (final IOException e) {
                discardCache();

                throw new LiquibaseException(this.output.toString(), e);
            }

            return null;
        });
    }

    public LiquibaseGenerator discardCache() throws LiquibaseException {
        Objects.requireNonNull(this.changeLogCache, "The attribute 'changeLogCache' is required");

        final Path clc = Paths.get(this.changeLogCache);

        if (Files.exists(clc)) {
            try {
                final Path failed = clc.getParent()
                    .resolve("old-" + clc.getFileName());

                Files.deleteIfExists(failed);
                Files.move(clc, failed);
            } catch (final IOException e) {
                throw new LiquibaseException(this.changeLogCache.toString(), e);
            }
        }

        return this;
    }

    public String[] groups() {
        return this.groups.toArray(new String[0]);
    }

    private Writer createOutput() throws IOException {
        final Writer writer = this.writerProvider.create(this.output);

        return this.stripComments ? new StripCommentsWriter(writer) : writer;
    }
}
