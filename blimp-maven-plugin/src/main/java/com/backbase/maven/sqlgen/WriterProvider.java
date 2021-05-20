package com.backbase.maven.sqlgen;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

interface WriterProvider {

    Writer create(Path path) throws IOException;
}
