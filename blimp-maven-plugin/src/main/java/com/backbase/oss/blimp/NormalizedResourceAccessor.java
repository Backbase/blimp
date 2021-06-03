package com.backbase.oss.blimp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.Set;
import liquibase.resource.ClassLoaderResourceAccessor;

final class NormalizedResourceAccessor extends ClassLoaderResourceAccessor {
    NormalizedResourceAccessor(ClassLoader classLoader) {
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
