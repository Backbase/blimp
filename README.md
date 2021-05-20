# SQL generator

# Description

How it works

- collects all contexts from the changeset, then for each context it generates the upgrade script for that context
- generaters full create script
- discussed to fetch the previous version of the artefact and generate only the latest version - I have some concerns here

# Configuration

## sqlgen::generate

    <plugin>
        <groupId>com.backbase.oss</groupId>
        <artifactId>blimp-maven-plugin</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <executions>
            <execution>
                <goals>
                    <goal>generate</goal>
                </goals>
                <configuration>
                    <databases>
                        <database>mariadb</database>
                        <database>mssql</database>
                        <database>mysql</database> <!-- default -->
                        <database>oracle</database>
                    </databases>
                    <changeLogFile>db.changelog-persistence.xml</changeLogFile>
                    <inputDirectory>${project.basedir}/src/main/resources</inputDirectory>
                    <outputDirectory>${project.build.directory}/generated-resources/liquibase</outputDirectory>
                    <serviceName>${project.artifactId}</serviceName>
                    <addResource>false</addResource>
                    <formats>
                        <format>zip</format>
                    </format>
                    <classifier>sql</classifier>
                </configuration>
            </execution>
        </executions>
    </plugin>
