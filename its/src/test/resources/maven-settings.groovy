
import groovy.io.FileType
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.apache.maven.project.MavenProject

Path relativize(Path path) {
    Path root = project.basedir.toPath()

    path.startsWith(root) ? root.relativize(path) : path
}

void copySettings(String source, String target) {
    Path t = Paths.get(target).resolveSibling('settings.xml')

    if( !Files.exists(t) ) {
        Path s = Paths.get(source)

        println "${relativize(s)} -> ${relativize(t)}"

        Files.createDirectories(t.parent)
        Files.copy(s, t)
    }
}

boolean isRootPOM(File file) {
    file.name == 'pom.xml' && !new File(file.parentFile.parentFile, 'pom.xml').exists()
}

new File("${project.build.testOutputDirectory}/com/backbase/oss/blimp/BlimpIT")
        .eachFileRecurse FileType.FILES, { file ->
            if( isRootPOM(file) ) {
                copySettings("${project.build.testOutputDirectory}/maven-settings.xml", file.path)
            }
        }

String settings = System.env.MVN_SETTINGS ?: "${System.getProperty('user.home')}/.m2/settings.xml"

new File("${project.build.testOutputDirectory}/com/backbase/oss/blimp/BackbaseIT")
        .eachFileRecurse FileType.FILES, { file ->
            if( isRootPOM(file) ) {
                copySettings(settings, file.path)
            }
        }
