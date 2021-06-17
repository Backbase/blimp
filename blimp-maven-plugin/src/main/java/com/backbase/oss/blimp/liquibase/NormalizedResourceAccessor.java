package com.backbase.oss.blimp.liquibase;

import static java.lang.Thread.currentThread;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.Set;
import liquibase.resource.ClassLoaderResourceAccessor;

public final class NormalizedResourceAccessor extends ClassLoaderResourceAccessor {
    public NormalizedResourceAccessor() {
        super(currentThread().getContextClassLoader());
    }

    public NormalizedResourceAccessor(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public Set<InputStream> getResourcesAsStream(String path) throws IOException {
        return super.getResourcesAsStream(
            Paths.get(path.replace("/", FileSystems.getDefault().getSeparator()))
                .normalize()
                .toString());
    }
}
