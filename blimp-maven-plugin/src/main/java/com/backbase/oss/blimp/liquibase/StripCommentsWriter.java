package com.backbase.oss.blimp.liquibase;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class StripCommentsWriter extends StringWriter {

    final Writer delegate;

    @Override
    public void close() throws IOException {
        this.delegate.write(CommentsRemover.apply(toString()));
        this.delegate.close();
    }
}
