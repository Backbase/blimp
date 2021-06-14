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

### Available parameters:

    addResource (Default: false)
      Whether to add the SQL scripts as a resource of the project.
      User property: blimp.addResource

    addTestResource (Default: false)
      Whether to add the SQL scripts as a test resource of the project.
      Use it when the generated SQL should be visible to the testing classpath,
      but not to the artifact classpath.
      User property: blimp.addTestResource

    changeLogDirectory (Default: ${project.basedir}/src/main/resources)
      The base directory of the changelog files.
      Required: Yes
      User property: blimp.changeLogDirectory

    changeLogFile (Default: db.changelog-main.xml)
      The location of the changelog to execute.
      Usually a file name relative to the input directory but it can also point
      to a classpath resource.
      Required: Yes
      User property: blimp.changeLogFile

    databases (Default: mysql)
      The list of the databases for which to generate the SQL scripts.
      Required: Yes
      User property: blimp.databases

    encoding (Default: UTF-8)
      The file encoding used for SQL files.
      User property: blimp.encoding

    groupingStrategy (Default: AUTO)
      Controls how to group the changesets to generate one SQL script for a
      given context or label.
      The following options are available
      
      - CONTEXTS: use the changeset context to group changes.
      - LABELS: use the changeset label to group changes.
      - AUTO: tries to identify if the changes use contexts or labels; if both
        are present, then contexts is preferred.
      Note that when a context or label contains multiple values, only the first
      one is considered.
      User property: blimp.groupingStrategy

    inputPatterns (Default: **/*.sql,**/db.changelog*.xml,**/db.changelog*.yml)
      List of glob patterns specifing the changelog files.
      Not needed by Liquibase, but used by the plugin to avoid unnecessary
      executions of the goal.
      Required: Yes
      User property: blimp.inputPatterns

    properties
      Specifies a map of properties you want to pass to Liquibase.
      User property: blimp.properties

    scriptsDirectory (Default:
    ${project.build.directory}/generated-resources/blimp)
      The location of the output directory.
      Required: Yes
      User property: blimp.scriptsDirectory

    serviceName (Default: ${project.artifactId})
      The name of the service.
      Required: Yes
      User property: blimp.serviceName

    skip
      Skip the execution.
      User property: blimp.skip

    sqlFileNameFormat (Default: @{database}/@{group}/@{service}.sql)
      Specifies how to generate the name of SQL script.
      The following placeholders are available:
      
      - database: the database type
      - group: the name of the group for which the goal generates the SQL
        script.
      - service: the service name taken from the MojoBase.serviceName.
      - For full creation SQL scripts, the group name is set as create.
      User property: blimp.sqlFileNameFormat

    {#stripComments}stripComments (Default: false)
      Set to true to remove comments from SQL scripts.
      User property: blimp.stripComments

    {#withInitialVersion}withInitialVersion (Default: false)
    Generates a script for the initial version when there is more than one
    group. Having more than one group means a database has been already created for
    the initial version, so only the upgrade scripts should be generated.
    User property: blimp.withInitialVersion

### Full Configuration with default values

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

## Goal blimp:test-generate

The equivalent of `blimp:generate`, but bound to the Maven testing lifecycle.

### Available parameters:

    addTestResource (Default: false)
      Whether to add the SQL scripts as a test resource of the project.
      User property: blimp.addTestResource

    databases (Default: mysql)
      The list of the databases for which to generate the SQL scripts.
      Required: Yes
      User property: blimp.databases

    encoding (Default: UTF-8)
      The file encoding used for SQL files.
      User property: blimp.encoding

    groupingStrategy (Default: AUTO)
      Controls how to group the changesets to generate one SQL script for a
      given context or label.
      The following options are available
      
      - CONTEXTS: use the changeset context to group changes.
      - LABELS: use the changeset label to group changes.
      - AUTO: tries to identify if the changes use contexts or labels; if both
        are present, then contexts is preferred.
      Note that when a context or label contains multiple values, only the first
      one is considered.
      User property: blimp.groupingStrategy

    inputPatterns (Default: **/*.sql,**/db.changelog*.xml,**/db.changelog*.yml)
      List of glob patterns specifing the changelog files.
      Not needed by Liquibase, but used by the plugin to avoid unnecessary
      executions of the goal.
      Required: Yes
      User property: blimp.inputPatterns

    properties
      Specifies a map of properties you want to pass to Liquibase.
      User property: blimp.properties

    serviceName (Default: ${project.artifactId})
      The name of the service.
      Required: Yes
      User property: blimp.serviceName

    skip
      Skip the execution.
      User property: blimp.skip

    sqlFileNameFormat (Default: @{database}/@{group}/@{service}.sql)
      Specifies how to generate the name of SQL script.
      The following placeholders are available:
      
      - database: the database type
      - group: the name of the group for which the goal generates the SQL
        script.
      - service: the service name taken from the MojoBase.serviceName.
      - For full creation SQL scripts, the group name is set as create.
      User property: blimp.sqlFileNameFormat

    stripComments (Default: false)
      Set to true to remove comments from SQL scripts.
      User property: blimp.stripComments

    testChangeLogDirectory (Default: ${project.basedir}/src/test/resources)
      The base directory of the changelog files.
      Required: Yes
      User property: blimp.testChangeLogDirectory

    testChangeLogFile (Default: db.changelog-test.xml)
      The location of the changelog to execute.
      Usually a file name relative to the input directory but it can also point
      to a classpath resource.
      Required: Yes
      User property: blimp.testChangeLogFile

    testScriptsDirectory (Default:
    ${project.build.directory}/generated-test-resources/blimp)
      The location of the test output directory.
      Required: Yes
      User property: blimp.testScriptsDirectory

    withInitialVersion (Default: false)
    Generates a script for the initial version when there is more than one
    group. Having more than one group means a database has been already created for
    the initial version, so only the upgrade scripts should be generated.
    User property: blimp.withInitialVersion

### Full Configuration with default values

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

## Goal blimp:assemble

Creates an archive containing the generated SQL files.

###  Available parameters:

    attach (Default: true)
      Whether to attach the produced archives as artifacts.
      User property: blimp.attach

    classifier (Default: sql)
      The classifier of the archive artifact.
      User property: blimp.classifier

    formats (Default: zip)
      Specifies the formats of the archive.
      Multiple formats can be supplied and the goal assemble will generate an
      archive for each desired formats.
      
      A format is specified by supplying one of the following values in a
      <format> subelement:
      
      - zip creates a ZIP file format
      - tar creates a TAR file format
      - tar.gz creates a Gzip TAR file format
      - tar.xz creates a Xz TAR file format
      - tar.bz2 creates a Bzip2 TAR file format
      User property: blimp.formats

    scriptsDirectory (Default:
    ${project.build.directory}/generated-resources/blimp)
      Location of the output directory.
      Required: Yes
      User property: blimp.scriptsDirectory

    serviceName (Default: ${project.artifactId})
      The name of the service.
      Required: Yes
      User property: blimp.serviceName

    skip
      Skip the execution.
      User property: blimp.skip

### Full Configuration with default values

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

## Goal "blimp:help"

Display help information on backbase-blimp-plugin.

Call mvn blimp:help -Ddetail=true -Dgoal=<goal-name> to display parameter details.

### Available parameters

    detail (Default: false)
      If true, display all settable properties for each goal.
      User property: detail

    goal
      The name of the goal for which to show help. If unspecified, all goals
      will be displayed.
      User property: goal

    indentSize (Default: 2)
      The number of spaces per indentation level, should be positive.
      User property: indentSize

    lineLength (Default: 80)
      The maximum length of a display line, should be positive.
      User property: lineLength

## SQL Formatting

The plugin doesn't provide any SQL formatter per se, but it uses a bridge to the Hibernate formatter implemented as a Liquibase extension.

The extension is activated only when Hibernate is declared as a dependency of the plugin, allowing other Liquibase extensions to provide their own formatters.

    <pluginManagement>
        <plugin>
            <groupId>com.backbase.oss</groupId>
            <artifactId>blimp-maven-plugin</artifactId>
            <version>.....</version>
            <dependencies>
                <dependency>
                    <groupId>org.hibernate</groupId>
                    <artifactId>hibernate-core</artifactId>
                    <version>5.4.32.Final</version>
                </dependency>
            </dependencies>
        </plugin>
    </pluginManagement>

## Configuration Examples

The most concise example:

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

More examples can be found in the integration test resources folder.
