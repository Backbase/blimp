# BLIMP

           _..--=--..._
        .-'            '-.  .-.
       /.'              '.\/  /
      |=-                -=| (
       \'.              .'/\  \
        '-.,_____ _____.-'  '-'
             [_____]=8

## Description

The Backbase Liquibase Integration Maven Plugin is here to eliminate the boilerplate `<execution/>` elements needed to generate the SQL create/update scripts.

---

## Goal "blimp:generate"

Generates all SQL scripts for all specified databases.

This mojo executes the following actions

  1.  generates the full creation script
  2.  collects all groups of changesets
  3.  for each group, generates one script containing all changes in that group

What is a group?

A group is a collection of changesets that are supposed to included in a
release; they can be either the labels of the changes or the contexts depending on
the `groupingStrategy` configuration.

### Available parameters

##### addResource

Whether to add the SQL scripts as a resource of the project.

*Default*: `false`

*User property*: `blimp.addResource`

##### addTestResource

Whether to add the SQL scripts as a test resource of the project.
Use it when the generated SQL should be visible to the testing classpath, but not to the artifact classpath.

*Default*: `false`

*User property*: `blimp.addTestResource`

##### changeLogDirectory

The base directory of the changelog files.

*Default*: `${project.basedir}/src/main/resources`

*Required*: Yes

*User property*: `blimp.changeLogDirectory`

##### changeLogFile

The location of the changelog to execute.
Usually a file name relative to the input directory but it can also point to a classpath resource.

*User property*: `blimp.changeLogFile`

*Required*: Yes

*Default*: `db.changelog-main.xml`

##### databases

The database list for which SQL scripts are generated.

*Default*: `mysql`

*Required*: Yes

*User property*: `blimp.databases`

##### encoding

The file encoding used for SQL files.

*Default*: `UTF-8`

*User property*: `blimp.encoding`

##### groupingStrategy

Controls how to group the changesets to generate one SQL script for a given context or label.

The following options are available

- `CONTEXTS`: use the changeset context to group changes.
- `LABELS`: use the changeset label to group changes.
- `AUTO`: tries to identify if the changes use contexts or labels; if both are present, then contexts is preferred.

Note that when a context or label contains multiple values, only the first one is considered.

*Default*: `AUTO`

*User property*: blimp.groupingStrategy

##### inputPatterns

List of glob patterns specifing the changelog files. Not needed by Liquibase, but used by the plugin to avoid unnecessary executions of the goal.

*Default*: `**/*.sql,**/db.changelog*.xml,**/db.changelog*.yml`

*Required*: Yes

*User property*: `blimp.inputPatterns`

##### properties

Specifies a map of properties you want to pass to Liquibase.

*User property*: `blimp.properties`

##### scriptsDirectory

The location of the output directory.

*Default*: `${project.build.directory}/generated-resources/blimp`

*Required*: Yes

*User property*: `blimp.scriptsDirectory`

##### serviceName

The name of the service.

*Default*: `${project.artifactId}`

*Required*: Yes

*User property*: `blimp.serviceName`

##### skip

Skip the execution.

*Default*: `false`

*User property*: `blimp.skip`

##### sqlFileNameFormat

Specifies how to generate the name of SQL script.

The following placeholders are available:

- `database`: the database type
- `group`: the name of the group for which the goal generates the SQL script.
- `service`: the service name taken from the [serviceName](#serviceName-1) parameter.

The group name of the creation script is `create`.

*Default*: `@{database}/@{group}/@{service}.sql`

*User property*: `blimp.sqlFileNameFormat`

##### stripComments

Set to true to remove comments from SQL scripts.

*Default*: `false`

*User property*: `blimp.stripComments`

##### withInitialVersion

Generates a script for the initial version when there is more than one
group. Having more than one group means a database has been already created for
the initial version, so only the upgrade scripts should be generated.

*Default*: `false`

*User property*: `blimp.withInitialVersion`

### Full Configuration with default values

```xml
<plugin>
    <groupId>com.backbase.oss</groupId>
    <artifactId>blimp-maven-plugin</artifactId>
    <configuration>
        <skip>false</skip>
        <serviceName>${project.artifactId}</serviceName>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <addResource>false</addResource>
                <addTestResource>false</addTestResource>
                <changeLogFile>db.changelog-persistence.xml</changeLogFile>
                <changeLogDirectory>${project.basedir}/src/main/resources</changeLogDirectory>
                <databases>
                    <database>mysql</database>
                </databases>
                <encoding>UTF-8</encoding>
                <groupingStrategy>AUTO</groupingStrategy>
                <inputPatterns>
                    <inputPattern>**/*.sql</inputPattern>
                    <inputPattern>**/db.changelog*.xml</inputPattern>
                    <inputPattern>**/db.changelog*.yml</inputPattern>
                </inputPatterns>
                <scriptsDirectory>${project.build.directory}/generated-resources/blimp</scriptsDirectory>
                <sqlFileNameFormat>@{database}/@{group}/@{service}.sql</sqlFileNameFormat>
                <stripComments>false</stripComments>
                <withInitialVersion>false</withInitialVersion>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## Goal blimp:lint

Verifies the changelog compliance with a predefined set of rules.

By default, the linter comes with a very relaxed set of rules that can be customised using a file described in the [Lint Rules](blimp-lint/README.md] page..

### Available parameters

##### changeLogDirectory

The base directory of the changelog files.

*Default*: `${project.basedir}/src/main/resources`

*Required*: Yes

*User property*: `blimp.changeLogDirectory`

##### changeLogFile

The location of the changelog to execute.
Usually a file name relative to the input directory but it can also point to a classpath resource.

*Default*: `db.changelog-main.xml`

*User property*: `blimp.changeLogFile`

*Required*: Yes

##### databases

The database list for which changelogs are checked.

*Default*: `mysql`

*User property*: `blimp.databases`

*Required*: Yes

##### failOnSeverity

Causes an build failure if a rule with the specified severity is violated.

*User property*: `blimp.lint.failOnSeverity`

##### lintDatabase

Verifies the changelogs only for the specified database; if this configuration is missing, all changelogs specified by the `<databases/>` configuration are checked.

If the changelogs are not database dependent, specify one of the supported databases here to avoid running lint for each database.

*User property*: `blimp.lint.database`

##### lintProperties

Specifies a map of properties you want to pass to Liquibase.

*User property*: `blimp.lint.properties`

##### reportFile

The location of the lint report file.

*User property*: `blimp.lint.reportFile`

*Required*: Yes

*Default*: `${project.reporting.outputDirectory}/blimp.csv`

##### rules

The location of the rules definitions; it can be the full path of a local file or a classpath resource.

*User property*: `blimp.lint.rules`

### Overriding rules

There are cases when the rules provided by the [rules](#rules) parameter must be overriden. In such cases the behaviour
of any rule can be modified by specifying the associated Liquibase properties.

For instance, a team may consider that the rule for foreign key names enforced by `rules` parameter is too
restrictive for a legacy project and may want to relax it:

```yml
blimp:
  lint:
    foreign-key-name:
      severity: ERROR
      required: true
      matches: FK_.+
```

There are two options

- disable the rule

```xml
    <configuration>
        <lintProperties>
            <blimp.lint.foreign-key-name.enabled>false</blimp.lint.foreign-key-name.enabled>
        </lintProperties>
        <failOnSeverity>ERROR</failOnSeverity>
    </configuration>
```

- change the rule severity

```xml
    <configuration>
        <lintProperties>
            <blimp.lint.foreign-key-name.severity>INFO</blimp.lint.foreign-key-name.severity>
        </lintProperties>
        <failOnSeverity>ERROR</failOnSeverity>
    </configuration>
```

---

## Goal blimp:test-generate

The equivalent of `blimp:generate`, but bound to the Maven testing lifecycle.

### Available parameters

##### addTestResource

Whether to add the SQL scripts as a test resource of the project.

*Default*: `false`

*User property*: `blimp.addTestResource`

##### databases

The database list for which SQL scripts are generated.

*Default*: `mysql`

*Required*: Yes

*User property*: `blimp.databases`

##### encoding

The file encoding used for SQL files.

*Default*: `UTF-8`

*User property*: `blimp.encoding`

##### groupingStrategy

Controls how to group the changesets to generate one SQL script for a given context or label.

The following options are available

- `CONTEXTS`: use the changeset context to group changes.
- `LABELS`: use the changeset label to group changes.
- `AUTO`: tries to identify if the changes use contexts or labels; if both are present, then contexts is preferred.

Note that when a context or label contains multiple values, only the first one is considered.

*Default*: `AUTO`

*User property*: `blimp.groupingStrategy`

##### inputPatterns

List of glob patterns specifing the changelog files. Not needed by Liquibase, but used by the plugin to avoid unnecessary executions of the goal.

*Default*: `**/*.sql,**/db.changelog*.xml,**/db.changelog*.yml`

*Required*: Yes

*User property*: `blimp.inputPatterns`

##### properties

Specifies a map of properties you want to pass to Liquibase.

*User property*: `blimp.properties`

##### serviceName

The name of the service.

*Default*: `${project.artifactId}`

*Required*: Yes

*User property*: `blimp.serviceName`

##### skip

Skip the execution.

*Default*: `false`

*User property*: `blimp.skip`

##### sqlFileNameFormat

Specifies how to generate the name of SQL script.

The following placeholders are available:

- `database`: the database type
- `group`: the name of the group for which the goal generates the SQL script.
- `service`: the service name taken from the [serviceName](#serviceName-2) parameter.

The group name of the creation script is `create`.

*Default*: `@{database}/@{group}/@{service}.sql`

*User property*: `blimp.sqlFileNameFormat`

##### stripComments

Set to true to remove comments from SQL scripts.

*Default*: `false`

*User property*: `blimp.stripComments`

##### testChangeLogDirectory

The base directory of the changelog files.

*Default*: `${project.basedir}/src/test/resources`

*Required*: Yes

*User property*: `blimp.testChangeLogDirectory`

##### testChangeLogFile

The location of the changelog to execute.
Usually a file name relative to the input directory but it can also point
to a classpath resource.

*Default*: `db.changelog-test.xml`

*Required*: Yes

*User property*: `blimp.testChangeLogFile`

##### testScriptsDirectory

The location of the test output directory.

*Default*: `${project.build.directory}/generated-test-resources/blimp`

*Required*: Yes

*User property*: `blimp.testScriptsDirectory`

##### withInitialVersion

Generates a script for the initial version when there is more than one
group. Having more than one group means a database has been already created for
the initial version, so only the upgrade scripts should be generated.

*Default*: `false`

*User property*: `blimp.withInitialVersion`

### Full Configuration with default values

```xml
<plugin>
    <groupId>com.backbase.oss</groupId>
    <artifactId>blimp-maven-plugin</artifactId>
    <configuration>
        <skip>false</skip>
        <serviceName>${project.artifactId}</serviceName>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>test-generate</goal>
            </goals>
            <configuration>
                <addTestResource>false</addTestResource>
                <databases>
                    <database>mysql</database>
                </databases>
                <encoding>UTF-8</encoding>
                <groupingStrategy>AUTO</groupingStrategy>
                <inputPatterns>
                    <inputPattern>**/*.sql</inputPattern>
                    <inputPattern>**/db.changelog*.xml</inputPattern>
                    <inputPattern>**/db.changelog*.yml</inputPattern>
                </inputPatterns>
                <sqlFileNameFormat>@{database}/@{group}/@{service}.sql</sqlFileNameFormat>
                <stripComments>false</stripComments>
                <testChangeLogFile>db.changelog-persistence.xml</testChangeLogFile>
                <testChangeLogDirectory>${project.basedir}/src/test/resources</testChangeLogDirectory>
                <testScriptsDirectory>${project.build.directory}/generated-test-resources/blimp</testScriptsDirectory>
                <withInitialVersion>false</withInitialVersion>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## Goal blimp:assemble

Creates an archive containing the generated SQL files.

###  Available parameters

##### attach

Whether to attach the produced archives as artifacts.

*Default*: `true`

*User property*: `blimp.attach`

##### classifier

The classifier of the archive artifact.

*Default*: `sql`

*User property*: `blimp.classifier`

##### formats

Specifies the formats of the archive.

Multiple formats can be supplied and the goal assemble will generate an
archive for each desired formats.

A format is specified by supplying one of the following values in a `<format/>` subelement:

- zip creates a ZIP file format
- tar creates a TAR file format
- tar.gz creates a Gzip TAR file format
- tar.xz creates a Xz TAR file format
- tar.bz2 creates a Bzip2 TAR file format

*Default*: `zip`

*User property*: `blimp.formats`

##### scriptsDirectory

Location of the output directory.

*Default*: `${project.build.directory}/generated-resources/blimp`

*Required*: Yes

*User property*: `blimp.scriptsDirectory`

##### serviceName

The name of the service.

*Default*: `${project.artifactId}`

*Required*: Yes

*User property*: `blimp.serviceName`

##### skip

Skip the execution.

*Default*: `false`

*User property*: `blimp.skip`

### Full Configuration with default values

```xml
<plugin>
    <groupId>com.backbase.oss</groupId>
    <artifactId>blimp-maven-plugin</artifactId>
    <configuration>
        <skip>false</skip>
        <serviceName>${project.artifactId}</serviceName>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>assemble</goal>
            </goals>
            <configuration>
                <formats>
                    <format>zip</format>
                </formats>
                <classifier>sql</classifier>
                <attach>true</attach>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## Goal "blimp:help"

Display help information on backbase-blimp-plugin.

Call `mvn blimp:help -Ddetail=true -Dgoal=<goal-name>` to display parameter details.

### Available parameters

##### detail

If `true`, display all settable properties for each goal.

*Default*: `false`

*User property*: `detail`

##### goal

The name of the goal for which to show help. If unspecified, all goals will be displayed.

*User property*: `goal`

##### indentSize

The number of spaces per indentation level, should be positive.

*Default*: `2`

*User property*: i`ndentSize`

##### lineLength

The maximum length of a display line, should be positive.

*Default*: `80`

*User property*: `lineLength`

---

## SQL Formatting

The plugin by default formats the SQL generated by Liquibase. The formatter can disabled by setting the property `blimp.formatter.enabled` to `false` in the [properties[(#properties-1) configuration of the plugin.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.backbase.oss</groupId>
            <artifactId>blimp-maven-plugin</artifactId>
            <configuration>
                <properties>
                    <blimp.formatter.enabled>false</blimp.formatter.enabled>
                <properties>
            <configuration>
        </plugin>
    </plugins>
</build>
```

## Configuration Examples

The most concise example:

```xml
<plugin>
    <groupId>com.backbase.oss</groupId>
    <artifactId>blimp-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
                <goal>test-generate</goal>
                <goal>assemble</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

More examples can be found in the integration test resources folder.
