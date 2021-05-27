package com.backbase.oss.blimp;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import com.soebes.itf.jupiter.maven.MavenExecutionResult;
import com.soebes.itf.jupiter.maven.MavenProjectResult;
import java.io.File;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.maven.model.Model;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestUtils {

    static File installedArchive(MavenExecutionResult result, String artifactId, String classifier, String format) {
        final MavenProjectResult project = result.getMavenProjectResult();
        final Model model = project.getModel();
        final File cache = project.getTargetCacheDirectory();
        final File installed = new File(cache, format("%1$s/%2$s/%3$s/%2$s-%3$s-%4$s.%5$s",
            model.getGroupId().replace('.', File.separatorChar),
            ofNullable(artifactId).orElse(model.getArtifactId()), model.getVersion(),
            classifier, format));

        return installed;
    }

}


