# Backbase Liquibase Integration Maven Plugin

           _..--=--..._
        .-'            '-.  .-.
       /.'              '.\/  /
      |=-                -=| (
       \'.              .'/\  \
        '-.,_____ _____.-'  '-'
             [_____]=8

## blimp:assemble

Generate an archive containing the generated SQL files.

Available parameters:

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

    outputDirectory (Default:
    ${project.build.directory}/generated-resources/liquibase)
      Location of the output directory.
      Required: Yes
      User property: blimp.outputDirectory

    serviceName (Default: ${project.artifactId})
      The name of the service.
      Required: Yes
      User property: blimp.serviceName

    skip
      Skip the execution.
      User property: blimp.skip

## blimp:generate

Generate all SQL scripts for all specified databases.

This mojo executes the following actions

  1.  generates the full creation script
  2.  collects all groups of changesets
  3.  for each group, generates one script containing all changes in that group

What is a group?
A group is a collection of changesets that are supposed to included in a
release they can be either the labels of the changes or the contexts.

Available parameters:

    addResource
      Whether to add the SQL scripts as a resource of the project.
      User property: blimp.addResource

    addTestResource
      Whether to add the SQL scripts as a resource of the project.
      User property: blimp.addTestResource

    changeLogFile (Default: db.changelog-persistence.xml)
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

    inputDirectory (Default: ${project.basedir}/src/main/resources)
      The base directory of the changelog files.
      Required: Yes
      User property: blimp.inputDirectory

    inputPatterns (Default: **/*.sql,**/db.changelog*.xml,**/db.changelog*.yml)
      List of glob patterns specifing the changelog files.
      Not needed by Liquibase, but used by the plugin to avoid unnecessary
      executions of the goal.
      Required: Yes
      User property: blimp.inputPatterns

    outputDirectory (Default:
    ${project.build.directory}/generated-resources/liquibase)
      The destination directory of the generated SQL files.
      Required: Yes
      User property: blimp.outputDirectory

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
      - For full creation SQL scripts, the group is set as create.
      User property: blimp.sqlFileNameFormat

## blimp:help

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

## Configuration Examples

The following is a complete configuration example showing the default values.

    <plugin>
        <groupId>com.backbase.oss</groupId>
        <artifactId>blimp-maven-plugin</artifactId>
        <configuration>
            <skip>false</skip>
            <outputDirectory>${project.build.directory}/generated-resources/liquibase</outputDirectory>
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
                    <databases>
                        <database>mysql</database>
                    </databases>
                    <encoding>UTF-8</encoding>
                    <groupingStrategy>AUTO</groupingStrategy>
                    <inputDirectory>${project.basedir}/src/main/resources</inputDirectory>
                    <inputPatterns>
                        <inputPattern>**/*.sql</inputPattern>
                        <inputPattern>**/db.changelog*.xml</inputPattern>
                        <inputPattern>**/db.changelog*.yml</inputPattern>
                    </inputPatterns>
                    <sqlFileNameFormat>@{database}/@{group}/@{service}.sql</sqlFileNameFormat>
                </configuration>
            </execution>
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

A shorter and more concise example:

    <plugin>
        <groupId>com.backbase.oss</groupId>
        <artifactId>blimp-maven-plugin</artifactId>
        <executions>
            <execution>
                <goals>
                    <goal>generate</goal>
                    <goal>assemble</goal>
                </goals>
                <configuration>
                    <databases>
                        <database>mariadb</database>
                        <database>mssql</database>
                        <database>mysql</database>
                        <database>oracle</database>
                    </databases>
                    <addTestResource>true</addTestResource>
                </configuration>
            </execution>
        </executions>
    </plugin>

More examples can be found in the integration test resources folder.
