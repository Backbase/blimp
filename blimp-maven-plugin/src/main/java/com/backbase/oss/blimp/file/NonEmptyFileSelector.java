package com.backbase.oss.blimp.file;

import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nonnull;
import org.codehaus.plexus.components.io.fileselectors.FileInfo;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;

public class NonEmptyFileSelector implements FileSelector {

    @Override
    public boolean isSelected(@Nonnull FileInfo fileInfo) throws IOException {
        if (!fileInfo.isFile()) {
            return false;
        }
        byte[] buffer = new byte[10];
        try (InputStream content = fileInfo.getContents()) {
            if (content.read(buffer) <= 1) {
                // Liquibase has generated a file containing just an empty line
                return false;
            }
        }
        return true;
    }
}
