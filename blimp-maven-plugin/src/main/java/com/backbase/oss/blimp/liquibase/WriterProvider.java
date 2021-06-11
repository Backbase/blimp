package com.backbase.oss.blimp.liquibase;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

public interface WriterProvider {

    Writer create(Path path) throws IOException;
}
