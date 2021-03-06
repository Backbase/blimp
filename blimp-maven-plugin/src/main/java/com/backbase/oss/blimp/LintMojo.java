package com.backbase.oss.blimp;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static liquibase.util.StringUtils.trimToNull;

import com.backbase.oss.blimp.core.LiquibaseEngine;
import com.backbase.oss.blimp.core.LiquibaseEngine.LiquibaseEngineBuilder;
import com.backbase.oss.blimp.core.PropertiesConfigProvider;
import com.backbase.oss.blimp.lint.BlimpLinter;
import com.backbase.oss.blimp.lint.LintRuleSeverity;
import com.backbase.oss.blimp.lint.LintRuleViolation;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import liquibase.resource.FileSystemResourceAccessor;
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
     * The path of changelog file relative to the input directory.
     */
    @Parameter(property = "blimp.changeLogFile", required = true,
        defaultValue = "db.changelog-main.xml")
    private String changeLogFile;

    /**
     * The database list for which changelogs are checked.
     */
    @Parameter(property = "blimp.databases", defaultValue = "mysql", required = true)
    private List<String> databases;

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
     * Verifies the changelogs only for the specified database; if this configuration is missing, all
     * changelogs specified by &lt;databases/&gt; are checked.
     * <p>
     * If the changelogs are not database dependent, specify one of the supported databases here.
     * </p>
     */
    @Parameter(property = "blimp.lint.database", required = false)
    private String lintDatabase;

    /**
     * Specifies a map of properties you want to pass to Liquibase.
     */
    @Parameter(property = "blimp.lint.properties")
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

        final File changeLog = new File(this.changeLogDirectory, this.changeLogFile);

        if (!changeLog.exists()) {
            return;
        }

        getLog().info("Checking " + changeLog);

        final LiquibaseEngineBuilder<?, ?> builder = LiquibaseEngine.builder()
            .changeLogFile(this.changeLogFile)
            .accessor(new FileSystemResourceAccessor(this.changeLogDirectory.getPath()))
            .configProvider(this.lintProperties);

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

            builder.configProvider(cvp);
        }

        final BlimpLinter linter = new BlimpLinter();
        final List<String> dbs = ofNullable(trimToNull(this.lintDatabase))
            .map(Collections::singletonList)
            .orElse(this.databases);

        for (final String db : dbs) {
            builder.build().toBuilder().database(db).build().visit(linter);
        }

        final List<LintRuleViolation> violations = linter.getResult();

        if (violations.isEmpty()) {
            return;
        }

        this.reportFile.getParentFile().mkdirs();

        final LintRuleSeverity severity = violations.stream()
            .map(LintRuleViolation::getSeverity)
            .max(LintRuleSeverity::compareTo)
            .get();

        try (PrintWriter out = new PrintWriter(this.reportFile)) {
            out.println("database,id,rule,property,severity,message");

            log(severity, "Found rule violations in " + this.changeLogFile);
            if (rulesURL != null) {
                log(severity, "Rules found in %s", rulesURL);
            }

            for (final LintRuleViolation vio : violations) {
                out.printf("%s,%s,%s,%s,%s,\"%s\"\n",
                    vio.getDatabase(), vio.getId(), vio.getRule(), vio.getProperty(), vio.getSeverity(),
                    vio.getMessage());

                log(vio.getSeverity(), "%s(%s): %s", vio.getDatabase(), vio.getId(), vio.getMessage());
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

        final URL res = getClass().getClassLoader().getResource(this.rules);

        if (res != null) {
            return res;
        }

        throw new MojoFailureException("Cannot find rules at location " + this.rules);
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
