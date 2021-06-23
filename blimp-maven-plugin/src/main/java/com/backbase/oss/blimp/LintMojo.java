package com.backbase.oss.blimp;

import static java.lang.String.format;

import com.backbase.oss.blimp.core.PropertiesConfigProvider;
import com.backbase.oss.blimp.lint.BlimpLinter;
import com.backbase.oss.blimp.lint.LintRuleSeverity;
import com.backbase.oss.blimp.lint.LintRuleViolation;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import liquibase.configuration.ConfigurationValueProvider;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.configuration.SystemPropertyProvider;
import liquibase.resource.FileSystemResourceAccessor;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Verifies the changelog compliance with a predefined set of rules.
 */
@Mojo(name = "lint", requiresProject = true, defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public class LintMojo extends MojoBase {

    /**
     * The location of the <i>changelog</i> to execute.
     * <p>
     * Usually a file name relative to the input directory but it can also point to a classpath
     * resource.
     * </p>
     */
    @Parameter(property = "blimp.changeLogFile", required = true,
        defaultValue = "db.changelog-main.xml")
    private String changeLogFile;

    /**
     * The base directory of the <i>changelog</i> files.
     */
    @Parameter(property = "blimp.changeLogDirectory", required = true,
        defaultValue = "${project.basedir}/src/main/resources")
    private File changeLogDirectory;

    /**
     * Causes an build failure if a rule with the specified severity is violated.
     */
    @Parameter(property = "blimp.lint.failOnSeverity")
    private LintRuleSeverity failOnSeverity;

    /**
     * The location of the lint report file.
     */
    @Parameter(property = "blimp.lint.reportFile", required = true,
        defaultValue = "${project.reporting.outputDirectory}/blimp.csv")
    private File reportFile;

    /**
     * Specifies a map of properties you want to pass to Liquibase.
     */
    @Parameter(property = "blimp.lint.roperties")
    private PropertiesConfigProvider lintProperties;

    /**
     * The location of an optional rules resource in {@code yaml} format; it can be the full path of a
     * local file or a classpath resource.
     */
    @Parameter(property = "blimp.lint.rules")
    private String rules;

    @Override
    protected void doExecute() throws Exception {
        if (!this.changeLogDirectory.exists()) {
            return;
        }
        if (!new File(this.changeLogDirectory, this.changeLogFile).exists()) {
            return;
        }

        final BlimpLinter linter = BlimpLinter.builder()
            .changeLogFile(this.changeLogFile)
            .accessor(new FileSystemResourceAccessor(this.changeLogDirectory.getPath()))
            .build();

        final Collection<ConfigurationValueProvider> cvps = new ArrayList<>();

        cvps.add(new SystemPropertyProvider());

        if (this.lintProperties != null) {
            cvps.add(this.lintProperties);
        }

        final URL rulesURL = this.rules != null ? findRules() : null;

        if (rulesURL != null) {
            final PropertiesConfigProvider cvp = new PropertiesConfigProvider(this.rules);

            try (InputStream input = rulesURL.openStream()) {
                if (this.rules.matches(".+\\.y(a?)ml")) {
                    cvp.loadYaml(input);
                } else if (this.rules.endsWith(".properties")) {
                    cvp.load(input);
                } else if (this.rules.endsWith(".xml")) {
                    cvp.loadFromXML(input);
                } else {
                    throw new MojoFailureException("Unknown format of rules file " + this.rules);
                }
            }

            cvps.add(cvp);

            linter.withProperties(cvp);
        }
        if (this.lintProperties != null) {
            linter.withProperties(this.lintProperties);
        }

        LiquibaseConfiguration.getInstance().init(cvps.toArray(new ConfigurationValueProvider[0]));

        final List<LintRuleViolation> violations = linter.run();

        if (violations.isEmpty()) {
            return;
        }

        this.reportFile.getParentFile().mkdirs();

        final LintRuleSeverity severity = violations.stream()
            .map(LintRuleViolation::getSeverity)
            .max(LintRuleSeverity::compareTo)
            .get();

        try (PrintWriter out = new PrintWriter(this.reportFile)) {
            out.println("id,rule,property,severity,message");

            log(severity, "Found rule violations in " + this.changeLogFile);
            if (rulesURL != null) {
                log(severity, "Rules found in %s", rulesURL);
            }

            for (final LintRuleViolation vio : violations) {
                out.printf("%s,%s,%s,%s,\"%s\"\n",
                    vio.getId(), vio.getRule(), vio.getProperty(), vio.getSeverity(), vio.getMessage());

                log(vio.getSeverity(), "%10s: %s", vio.getId(), vio.getMessage());
            }
        }

        if ((this.failOnSeverity != null) && severity.compareTo(this.failOnSeverity) >= 0) {
            throw new MojoExecutionException("Found rule violations with severity " + this.failOnSeverity);
        }
    }

    public URL findRules() throws MojoFailureException, MalformedURLException {
        final File rulesFile = new File(this.rules);

        if (rulesFile.isFile()) {
            return rulesFile.toURI().toURL();
        }

        final URL res = classLoader().getResource(this.rules);

        if (res != null) {
            return res;
        }

        throw new MojoFailureException("Cannot find rules at location " + this.rules);
    }

    private ClassLoader classLoader() throws MojoFailureException {
        try {
            final Stream<URI> classpath = this.project.getRuntimeClasspathElements().stream()
                .map(Paths::get)
                .map(Path::toUri);

            final URL[] urls = Stream.concat(Stream.of(this.changeLogDirectory.toURI()), classpath)
                .map(MojoBase::toURL)
                .toArray(URL[]::new);

            return new URLClassLoader(urls, getClass().getClassLoader());
        } catch (final DependencyResolutionRequiredException e) {
            throw new MojoFailureException("Cannot construct Liquibase classpath", e);
        }
    }

    private void log(LintRuleSeverity sev, String format, Object... objects) {
        switch (sev) {
            case INFO:
                getLog().info(format(format, objects));

                break;

            case WARN:
                getLog().warn(format(format, objects));

                break;

            case ERROR:
                getLog().error(format(format, objects));

                break;
        }
    }
}


